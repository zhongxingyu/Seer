 /*
  * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
  * <p/>
  * The software source code is proprietary and confidential information of GigaSpaces.
  * You may use the software source code solely under the terms and limitations of
  * the license agreement granted to you by GigaSpaces.
  */
 
 package bootstrap;
 
 import beans.config.Conf;
 import com.google.common.base.Predicate;
 import models.User;
 import models.Widget;
 import models.WidgetIcon;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang3.ObjectUtils;
 import org.apache.commons.lang3.reflect.FieldUtils;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.reflections.ReflectionUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.Play;
 import play.api.libs.MimeTypes;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.io.FileInputStream;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * User: guym
  * Date: 3/31/13
  * Time: 11:50 AM
  */
 public class JsonInitialData implements InitialData {
 
     private static Logger logger = LoggerFactory.getLogger( JsonInitialData.class );
 
     @Inject
     Conf conf;
 
     @Override
     public void load( String dataStr )
     {
         try{
         logger.info( "loading initial data" );
         ObjectMapper mapper = new ObjectMapper(  );
         JsonData data = mapper.readValue( dataStr, JsonData.class );
         loadUsers( data.users );
         }catch(Exception e){
             logger.error( "error while reading initial data",e );
         }
     }
 
     private void loadUsers( Map<String, JsonUser> users )
     {
         logger.info( "loading users" );
         for ( String username : users.keySet() ) {
             logger.info( "loading user : " + username );
             User user = findOrCreateUser( username );
             JsonUser jsonUser = users.get( username );
 
             Map<String, JsonWidget> widgets =jsonUser.widgets;
 
             for ( String widgetName : widgets.keySet() ) {
                 logger.info( updateOrCreateWidget( user, widgetName, widgets.get( widgetName ) ).toString() );
             }
         }
 
     }
 
     public static class InitialDataChange{
         public boolean newItem = false;
         public boolean override = false;
         public String diff = null;
         public String name;
 
         @Override
         public String toString()
         {
             return "InitialDataChange{" +
                     "newItem=" + newItem +
                     ", diff='" + diff + '\'' +
                     ", name='" + name + '\'' +
                     ", override='" + override + '\'' +
                     '}';
         }
     }
 
     public InitialDataChange updateOrCreateWidget( User user, String widgetName, JsonWidget widgetJson ){
         InitialDataChange result = new InitialDataChange();
         result.name = widgetName;
         Widget w = null;
         Widget newWidget = null;
         for ( Widget widget : user.getWidgets() ) {
             if ( widget.getProductName().equals( widgetName )){
                 w = widget;
                 break;
             }
         }
 
         newWidget = new Widget( widgetName, widgetJson.version, widgetJson.title, widgetJson.youtube, widgetJson.provider, widgetJson.recipe, widgetJson.console.title, widgetJson.console.link, widgetJson.path );
         newWidget.setDescription( widgetJson.description );
         // handle requires login
         if ( widgetJson.login != null ) {
             newWidget.setRequireLogin( widgetJson.login.require );
             newWidget.setLoginVerificationUrl( widgetJson.login.url );
             newWidget.setWebServiceKey( widgetJson.login.token );
         }
 
 
         if ( w == null ){
             result.newItem = true;
             w = user.addNewWidget( newWidget );
         } else if ( conf.settings.initialData.override ) {
             result.override = true;
             String apiKey = w.getApiKey();
             w.delete(  );
             newWidget.setApiKey( apiKey );
             w = user.addNewWidget( newWidget );
         } else {
             result.diff =  diffObjects( newWidget, w, new HashSet<String>(  ){ {add( "id" ); add( "apiKey" );} } ).toString();
         }
 
 
         // handle image
         if ( widgetJson.image != null && !StringUtils.isEmpty( widgetJson.image.selected ) ) {
             File file = new File( Play.application().getFile( "conf/initialData/images" ), widgetJson.image.selected );
             if ( !file.exists() ) {
                 logger.warn( "image file does not exist! [{}]", file.getAbsolutePath() );
             } else {
                 WidgetIcon icon = WidgetIcon.findByWidgetApiKey( w.getApiKey() );
                 if ( icon == null ) {
                     logger.info( "creating icon from file [{}]", file.getName() );
                     icon = new WidgetIcon();
                     updateAndSave( file, icon, w );
                 }else if ( conf.settings.initialData.override ){
                     logger.info( "overriding icon" );
                     updateAndSave( file, icon, w );
 
                 }
             }
         }
 
 
         return result;
     }
 
     private void updateAndSave( File file, WidgetIcon icon, Widget w )
     {
         String contentType = MimeTypes.forFileName( file.getName() ).get();
         icon.setContentType( contentType );
         try {
             icon.setData( IOUtils.toByteArray(new FileInputStream(  file ) ) );
         } catch ( Exception e ) {
             logger.error( "unable to read file to byte array", e );
         }
         icon.setName( file.getName() );
         icon.save(  );
 
         w.setIcon( icon );
         w.save(  );
     }
 
     public User findOrCreateUser ( String username ){
         User u = User.find.where(  ).eq( "email", username ).findUnique();
         if ( u == null ){
            u = User.newUser(null, null, username, conf.settings.initialData.defaultPassword);
             u.save(  );
         }
         return u;
     }
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 
 
     public static class JsonConsole{
         public String title;
         public String link;
     }
 
     public static class JsonImage{
         public String selected;
     }
 
     public static class JsonLogin{
         public boolean require = false;
         public String url = null;
         public String token = null;
     }
 
     public static class JsonWidget{
         public String version;
         public String title;
         public String youtube;
         public String description;
         public String provider;
         public String recipe;
         public JsonConsole console = new JsonConsole();
         public String path;
         public JsonImage image = new JsonImage();
         public JsonLogin login = new JsonLogin();
 
     }
     public static class JsonUser{
         public Map<String, JsonWidget> widgets = new HashMap<String, JsonWidget>(  );
     }
 
     public static class JsonData
     {
         public Map<String, JsonUser> users = new HashMap<String, JsonUser>(  );
     }
 
     private List<DiffRecord> diffObjects( Object newObject, Object oldObject, final Set<String> ignoreFields ){
         List<DiffRecord> result = new LinkedList<DiffRecord>(  );
         Set<Field> allFields = ReflectionUtils.getAllFields( newObject.getClass(), new Predicate<Field>() {
             @Override
             public boolean apply( Field field )
             {
                 return !ignoreFields.contains( field.getName() );
             }
         } );
         for ( Field field : allFields ) {
             try{
             Object oldValue = FieldUtils.readField( field, oldObject, true );
             Object newValue = FieldUtils.readField( field, oldObject, true );
             if ( !ObjectUtils.equals(oldValue, newValue) ){
                result.add( new DiffRecord( field.getName(), oldValue, newValue ) );
             }
             }catch(Exception e){
                 logger.error( "unable to get field values [{}]", e );
             }
         }
         return result;
     }
 
     public static class DiffRecord {
         public String fieldName;
         public Object oldValue;
         public Object newValue;
 
         public DiffRecord( String fieldName, Object oldValue, Object newValue )
         {
             this.fieldName = fieldName;
             this.oldValue = oldValue;
             this.newValue = newValue;
         }
     }
 }
