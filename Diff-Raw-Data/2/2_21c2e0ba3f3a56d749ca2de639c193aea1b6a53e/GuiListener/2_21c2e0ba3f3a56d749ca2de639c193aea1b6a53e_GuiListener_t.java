 package org.paxle.se.provider.rsssearch.impl;
 
 import java.util.Hashtable;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceRegistration;
 import org.paxle.se.provider.rsssearch.impl.gui.ConfigServlet;
 
 public class GuiListener implements BundleListener {
 	private ServiceRegistration serviceReg = null;
 	private final BundleContext bc;
 	
 	public GuiListener(BundleContext bc) {
 		this.bc = bc;
 	}
 	
 	public void bundleChanged(BundleEvent event) {
 		if (event.getBundle().getHeaders().get(Constants.BUNDLE_SYMBOLICNAME).equals("org.paxle.gui")) {
 			if (event.getType() == BundleEvent.STARTED) {
 				/*
 				 * Registering the servlet
 				 */
 				ConfigServlet servlet=new ConfigServlet();
				servlet.setBundleLocation(bc.getBundle().getEntry("/").toString());
 				Hashtable<String, String> props = new Hashtable<String, String>();
 				props.put("path", "/rsssearchconfig");
 				props.put("menu", "RSS search sources");
 				this.serviceReg = bc.registerService("javax.servlet.Servlet", servlet, props);
 			} else if (event.getType() == BundleEvent.STOPPED && this.serviceReg != null) {
 				this.serviceReg.unregister();
 				this.serviceReg = null;
 			}
 		}
 	}
 }
