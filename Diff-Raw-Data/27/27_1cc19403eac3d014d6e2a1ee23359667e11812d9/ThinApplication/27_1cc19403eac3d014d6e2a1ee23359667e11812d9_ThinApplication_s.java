 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.thin.base.screen;
 
import java.net.URL;
import java.util.Hashtable;
 import java.util.Map;
 
 import org.jbundle.model.message.MessageManager;
import org.jbundle.model.screen.BaseAppletReference;
import org.jbundle.thin.base.db.Constants;
import org.jbundle.thin.base.db.Params;
 import org.jbundle.thin.base.util.Application;
import org.jbundle.thin.base.util.ThinMenuConstants;
 import org.jbundle.thin.base.util.message.RemoteMessageManager;
 
 public class ThinApplication extends Application {
 
     /**
      * Default constructor.
      */
     public ThinApplication()
     {
         super();
     }
     /**
      * Constructor.
      * Pass in the possible initial parameters.
      * @param env Environment is ignored in the thin context.
      * @param strURL The application parameters as a URL.
      * @param args The application parameters as an initial arg list.
      * @param applet The application parameters coming from an applet.
      */
     public ThinApplication(Object env, Map<String,Object> properties, Object applet)
     {
         this();
         this.init(env, properties, applet); // The one and only
     }
     /**
      * Initialize the Application.
      * @param env Environment is ignored in the thin context.
      * @param strURL The application parameters as a URL.
      * @param args The application parameters as an initial arg list.
      * @param applet The application parameters coming from an applet.
      */
     public void init(Object env, Map<String,Object> properties, Object applet)
     {
     	super.init(env, properties, applet);
     }
     /**
      * Free all the resources belonging to this application.
      */
     public void free()
     {
     	super.free();
     }
     /**
      * Get this Message Queue (or create one if this name doesn't exist).
      * @param bCreateIfNone
      * NOTE: This is overridden for anything but thin.
      */
     public MessageManager getMessageManager(boolean bCreateIfNone)
     {
         return RemoteMessageManager.getMessageManager(this, null, null, bCreateIfNone);
     }
 }
