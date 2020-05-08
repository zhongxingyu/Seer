 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 
 package org.vfny.geoserver.action.data;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.util.MessageResources;
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.FeatureType;
 import org.vfny.geoserver.action.ConfigAction;
 import org.vfny.geoserver.action.HTMLEncoder;
 import org.vfny.geoserver.config.AttributeTypeInfoConfig;
 import org.vfny.geoserver.config.ConfigRequests;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.config.DataStoreConfig;
 import org.vfny.geoserver.config.FeatureTypeConfig;
 import org.vfny.geoserver.form.data.AttributeForm;
 import org.vfny.geoserver.form.data.TypesEditorForm;
 import org.vfny.geoserver.global.UserContainer;
 import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 
 /**
  * These Action handles all the buttons for the FeatureType Editor.
  * <p>
  * This one is more complicated then usual since not all the actions require
  * the form bean to be validated! I am going to have to hack a little bit
  * to make that happen, I may end up making the form bean validation differ
  * depending on the selected action.
  * </p>
  * <p>
  * Buttons that make this action go:
  * <ul>
  * <li>Submit: update the FeatureTypeConfig held by the user, punt it back into
  *     DataConfig and return to the FeatureTypeSelect screen.
  *     </li>
  * <li>Up and Down (for each attribute): not quite sure how to make these work
  *     yet - I hope I dont have to give them different names.
  *     </li>
  * </ul>
  * As usual we will have to uninternationlize the action name provided to us.
  * </p>
  * @author Richard Gould
  * @author Jody Garnett
  */
 public class TypesEditorAction extends ConfigAction {
     public ActionForward execute(ActionMapping mapping, ActionForm form,
         UserContainer user, HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
         
         System.out.println("form bean:"+form.getClass().getName() );
         TypesEditorForm typeForm = (TypesEditorForm) form;
 
         String action = typeForm.getAction();
         System.out.println("TypesEditorAction is "+action );
         
         Locale locale = (Locale) request.getLocale();
         MessageResources messages = servlet.getResources();
         final String SUBMIT = HTMLEncoder.decode(messages.getMessage(locale, "label.submit"));
         final String ADD    = HTMLEncoder.decode(messages.getMessage(locale, "label.add"));
         
         if( action.equals(SUBMIT)){
             return executeSubmit(mapping, typeForm, user, request);
         }
         
         LinkedList attributes = (LinkedList) typeForm.getAttributes();
         if( action.startsWith("up_")){
             int index = Integer.parseInt(action.substring(3));
             Object attribute = attributes.remove(index);
             attributes.add(index-1, attribute);
         } else if( action.startsWith("down_")){
             int index = Integer.parseInt(action.substring(5));
             Object attribute = attributes.remove(index);
             attributes.add(index+1, attribute);
         } else if( action.startsWith("delete_")){
             int index = Integer.parseInt(action.substring(7));
             attributes.remove(index);
         } else if( action.equals(ADD)) {
             executeAdd( mapping, typeForm, user, request );          
         }
         
         // Update, Up, Down, Add, Remove need to resync
         sync( typeForm, user.getFeatureTypeConfig() );
         form.reset( mapping, request );
         return mapping.findForward("config.data.type.editor");        
     }    
     /**
      * Sync generated attributes with schemaBase.
      * @param mapping
      * @param form
      * @param user
      * @param request
      * @return
      */
     private void sync(TypesEditorForm form, FeatureTypeConfig config ) {
         config.setName(form.getName());
         config.setAbstract(form.getAbstract());
         config.setDefaultStyle( form.getStyleId() );        
         config.setSRS(Integer.parseInt(form.getSRS()));
         config.setTitle(form.getTitle());
         config.setLatLongBBox(getBoundingBox(form));        
         config.setKeywords(keyWords(form));
         
         String schemaBase = form.getSchemaBase();        
         if( schemaBase == null || schemaBase.equals("") || schemaBase.equals("--")){
             config.setSchemaBase( null );
             config.setSchemaAttributes( null );
         }
         else {
             config.setSchemaBase( schemaBase );
             config.setSchemaAttributes( form.toSchemaAttributes() );
         }        
         config.setSchemaAttributes( form.toSchemaAttributes() );                
     }
     private void executeAdd(ActionMapping mapping, TypesEditorForm form, UserContainer user, HttpServletRequest request) {
         String attributeName = form.getNewAttribute();
         FeatureType featureType = null;        
         try {
             DataConfig config = ConfigRequests.getDataConfig(request);                
             DataStoreConfig dataStoreConfig = config.getDataStore( form.getDataStoreId() );                
             DataStore dataStore = dataStoreConfig.findDataStore(getServlet().getServletContext());
             featureType = dataStore.getSchema( form.getName() );                            
         } catch (IOException e) {
             // DataStore unavailable!
         } 
         AttributeType attributeType = featureType.getAttributeType( attributeName );
         AttributeTypeInfoConfig attributeConfig = new AttributeTypeInfoConfig(attributeType );
         AttributeForm newAttribute = new AttributeForm( attributeConfig, attributeType );
         form.getAttributes().add( newAttribute );
     }
     /**
      * Execute Submit Action.
      * 
 	 * @param mapping
 	 * @param form
 	 * @param user
 	 * @param request
 	 * @return
 	 */
 	private ActionForward executeSubmit(ActionMapping mapping, TypesEditorForm form, UserContainer user, HttpServletRequest request) {
         FeatureTypeConfig config = user.getFeatureTypeConfig();
         sync( form, config );
 
         DataConfig dataConfig = (DataConfig) getDataConfig();        
         dataConfig.addFeatureType( config.getName(), config );
 
         // Don't think reset is needed (as me have moved on to new page)
         // form.reset(mapping, request);
         
         getApplicationState().notifyConfigChanged();
         // Feature no longer selected
         user.setFeatureTypeConfig( null );        
         return mapping.findForward("config.data.type");
 	}
 	/**
 	 * @param typeForm
 	 * @return Bounding box in lat long
 	 */
 	private Envelope getBoundingBox(TypesEditorForm typeForm) {
 		return new Envelope(Double.parseDouble(typeForm.getMinX()), 
                             Double.parseDouble(typeForm.getMaxX()),
                            Double.parseDouble(typeForm.getMinY()),
                             Double.parseDouble(typeForm.getMaxY()));
 	}
 
 	/**
 	 * @param typeForm
 	 * @return Set of keywords
 	 */
 	private Set keyWords(TypesEditorForm typeForm) {
 		HashSet keywords = new HashSet();
         String[] array = (typeForm.getKeywords() != null)
             ? typeForm.getKeywords().split(System.getProperty("line.separator")) : new String[0];
 
         for (int i = 0; i < array.length; i++) {
             keywords.add(array[i]);
         }
 		return keywords;
 	}
 
 	DataStore aquireDataStore(String dataStoreID) throws IOException {
         DataConfig dataConfig = getDataConfig();
         DataStoreConfig dataStoreConfig = dataConfig.getDataStore(dataStoreID);
 
         Map params = dataStoreConfig.getConnectionParams();
 
         return DataStoreFinder.getDataStore(params);
     }
 
     FeatureType getSchema(String dataStoreID, String typeName)
         throws IOException {
         DataStore dataStore = aquireDataStore(dataStoreID);
         FeatureType type;
 
         return dataStore.getSchema(typeName);
     }
 }
