 /**
  * RmiSessionServer.java
  *
  * Created on January 10, 2000, 4:47 PM
  */
  
 package org.jbundle.base.screen.control.swing;
 
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.jbundle.model.BaseAppletReference;
 import org.jbundle.model.Task;
 import org.jbundle.thin.base.util.Application;
 import org.jbundle.thin.base.util.Util;
 import org.jbundle.util.osgi.bundle.BaseBundleService;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceEvent;
 
 public class AppletActivator extends BaseBundleService
 {
 	protected BaseAppletReference applet = null;
 	
     String[] args = {
//x    		"menu=dev.tourapp.com",
//x    		"background=backgrounds/worldmapalpha.gif",
//x    		"backgroundcolor=#EEEEFF",
//x    		"mainSharedDBName=main_base",
//x    		"local=Jdbc",
//x    		"remote=Jdbc",
//x    		"table=Jdbc",
     		};	// TODO(don) Fix this
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 	}
 
     @Override
     public void serviceChanged(ServiceEvent event) {
         if (event.getType() == ServiceEvent.REGISTERED)
         { // Osgi Service is up, Okay to start the server
             System.out.println("Starting Applet");
     		if (applet == null)
     		{
     	        Map<String,Object> propertiesTemp = new Hashtable<String,Object>();
     	        Util.parseArgs(propertiesTemp, args);
 
     	        //?server = new SApplet(args);
     	        SApplet.main(args);
     	        applet = Application.getRootApplet();
 
 //?    	        Environment env = new Environment(propertiesTemp);
     	        // Note the order that I do this... this is because MainApplication may need access to the remoteapp during initialization
     		}
         }
         if (event.getType() == ServiceEvent.UNREGISTERING)
         {
             if (applet != null)
             {
             	if (((Task)(applet)).getApplication() != null)
             		((Task)(applet)).getApplication().free();
             	else
             		applet.free();
             }
             applet = null;
         }        
     }
 }
