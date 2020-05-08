 /**
  *  @(#)SwitchDatabaseMenuScreen.
  *  Copyright © 2010 tourapp.com. All rights reserved.
  */
 package org.jbundle.app.program.screen;
 
 import java.awt.*;
 import java.util.*;
 
 import org.jbundle.base.db.*;
 import org.jbundle.thin.base.util.*;
 import org.jbundle.thin.base.db.*;
 import org.jbundle.base.db.event.*;
 import org.jbundle.base.db.filter.*;
 import org.jbundle.base.field.*;
 import org.jbundle.base.field.convert.*;
 import org.jbundle.base.field.event.*;
 import org.jbundle.base.screen.model.*;
 import org.jbundle.base.screen.model.util.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 
 /**
  *  SwitchDatabaseMenuScreen - Menu screen.
  */
 public class SwitchDatabaseMenuScreen extends MenuScreen
 {
     /**
      * Default constructor.
      */
     public SwitchDatabaseMenuScreen()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The main record for this screen.
      * @param itsLocation The location of this component within the parent.
      * @param parentScreen The parent screen.
      * @param fieldConverter The field this screen field is linked to.
      * @param iDisplayFieldDesc Do I display the field desc?
      * @param properties Addition properties to pass to the screen.
      */
     public SwitchDatabaseMenuScreen(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
     {
         this();
         this.init(record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * Open the files and setup the screen.
      * @param record The main record for this screen.
      * @param itsLocation The location of this component within the parent.
      * @param parentScreen The parent screen.
      * @param fieldConverter The field this screen field is linked to.
      * @param iDisplayFieldDesc Do I display the field desc?
      * @param properties Additional properties to pass to the screen.
      */
     public void init(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
     {
         if (properties != null)
         {
             Task task = parentScreen.getTask();
             BaseApplication app = (BaseApplication)task.getApplication();
         
             properties.remove(DBParams.SCREEN);
             boolean isSameProperties = true;
             for (String key : properties.keySet())
             {
                 if ((properties.get(key) == null) || (DBConstants.BLANK.equals(properties.get(key))))
                     if (app.getProperty(key) != null)
                         properties.put(key, app.getProperty(key));  // Blank property means use old property
                 if (properties.get(key).equals(app.getProperty(key)))
                     continue;
                 isSameProperties = false;
             }
             if (!isSameProperties)
             {
                 app.removeTask(task);
                 task.setApplication(null); 
                 Environment env = app.getEnvironment();
                Map<String,Object> appProps = env.getDefaultApplication().getProperties();
                 if ((appProps != null) && (properties != null))
                 { // Merge starting properties
                    String[] propNames = {DBParams.CONNECTION_TYPE, DBParams.CODEBASE, DBParams.REMOTE_HOST, DBParams.USER_NAME, DBParams.USER_ID, DBParams.LOCAL, DBParams.REMOTE, DBParams.TABLE};
                     Iterator<String> i = appProps.keySet().iterator();
                     while (i.hasNext())
                     {
                         String key = i.next();
                         for (String s : propNames)
                         {
                             if (s.equalsIgnoreCase(key))
                                 properties.put(key, appProps.get(key));
                         }
                         
                     }
                 }
                 env.removeApplication(app);
                 env.free();
                 env = new Environment(properties);
                 app = new MainApplication(env, properties, null);
                 app.addTask(task, null);
                 task.setApplication(app);
             }
         }
         super.init(record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * Get the screen display title.
      */
     public String getTitle()
     {
         return "Menu screen";
     }
     /**
      * Does the current user have permission to access this screen.
      * @return NORMAL_RETURN if access is allowed, ACCESS_DENIED or LOGIN_REQUIRED otherwise.
      */
     public int checkSecurity()
     {
         if ((this.getClass() == SwitchDatabaseMenuScreen.class) &&
                 (!Application.LOGIN_REQUIRED.equalsIgnoreCase(this.getProperty(Params.SECURITY_MAP))) && (!Application.CREATE_USER_REQUIRED.equalsIgnoreCase(this.getProperty(Params.SECURITY_MAP))))
             return DBConstants.NORMAL_RETURN;        // For now... allow access to all menus (unless I'm asking for a login)
         else if (this.getProperty(DBParams.HELP) != null)
             return DBConstants.NORMAL_RETURN;   // If this is a help (menu) screen, allow access
         return super.checkSecurity();           // If you override this class, use the standard security
     }
 
 }
