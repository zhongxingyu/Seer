 
 package org.eclipse.virgo.web.enterprise.applistener.internal;
 
 import java.io.File;
 
 import javax.servlet.ServletContext;
 
 import org.apache.catalina.Container;
 import org.apache.catalina.Lifecycle;
 import org.apache.catalina.LifecycleEvent;
 import org.apache.catalina.LifecycleListener;
 import org.apache.catalina.Loader;
 import org.apache.catalina.core.StandardContext;
 import org.eclipse.virgo.web.enterprise.openejb.deployer.VirgoDeployerEjb;
 
 public class OpenEjbApplicationListener implements LifecycleListener {
 
     public void deploy(StandardContext standardContext) throws Exception {
     	ServletContext context = standardContext.getServletContext();
         String contextPath = context.getContextPath();
         if (contextPath.equals("")) {
         	return;
         }
         VirgoDeployerEjb deployer = new VirgoDeployerEjb(contextPath, context.getClassLoader());
         try {
         	
             String realPath = context.getRealPath("");
             if (realPath != null) {
                 deployer.deploy(realPath, standardContext);
             } else {
                deployer.deploy(getAppModuleId(standardContext.getDocBase()), standardContext);              
             }
         } catch (Exception e) {
             // failing to initialise enterprise container should not kill the app's deployment
             // it might not need enterprise handling 
         }
        
     }
     
     public void undeploy(StandardContext standardContext) throws Exception {
     	ServletContext context = standardContext.getServletContext();
         String contextPath = context.getContextPath();
         VirgoDeployerEjb deployer = new VirgoDeployerEjb(contextPath, context.getClassLoader());
         try {
             String realPath = context.getRealPath("");
             if (realPath != null) {
                 deployer.undeploy(realPath);
             } else {
                 deployer.undeploy(getAppModuleId(standardContext.getDocBase()));
             }
         } catch (Exception e) {
             // failing to destroy enterprise container should not kill the app's undeployment
         }
     }
     
     private String getAppModuleId(String docBase) {
         String appModuleId;
 
         File appLocation = new File (docBase);
         if(!appLocation.isAbsolute()) {
             appModuleId = System.getProperty("org.eclipse.virgo.kernel.home") + File.separator + docBase;
         } else {
             appModuleId = docBase;
         }
         
         return appModuleId;
     }
 
 	@Override
 	public void lifecycleEvent(LifecycleEvent event) {
 		Object source = event.getSource();
         if (source instanceof StandardContext) {
 			StandardContext standardContext = (StandardContext) source;
 			if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
 			    Loader loader = standardContext.getLoader();
 			    if (loader != null && loader instanceof Lifecycle) {
 			        ((Lifecycle) loader).addLifecycleListener(this);
 			    }
 			} else if (Lifecycle.CONFIGURE_START_EVENT.equals(event.getType())) {
     			try {
     				deploy(standardContext);
     			} catch (Exception e) {
     				standardContext.setConfigured(false);
     			}
     		} else if (Lifecycle.CONFIGURE_STOP_EVENT.equals(event.getType())) {
     			try {
     				undeploy(standardContext);
     			} catch (Exception e) {
     				standardContext.setConfigured(false);
     			}
     		}
     	} else if (source instanceof Loader) {
     	    if (Lifecycle.AFTER_START_EVENT.equals(event.getType())) {
     	        // This event is very important
     	        // It reorders the lifecycle listeners
     	        // so that this listener appears after NamingContextListener
     	        Container container = ((Loader) source).getContainer();
     	        LifecycleListener[] listeners = container.findLifecycleListeners();
     	        for (int i = 0; listeners != null && i < listeners.length; i++) {
     	            if (listeners[i].equals(this)) {
     	                container.removeLifecycleListener(this);// remove the listener from its current position
     	            }
     	        }
     	        container.addLifecycleListener(this);// add the listener at the end
     	    }
     	}
 	}
 }
