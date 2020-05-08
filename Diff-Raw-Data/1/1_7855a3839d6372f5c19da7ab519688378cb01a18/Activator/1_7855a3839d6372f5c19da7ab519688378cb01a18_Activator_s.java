 package com.buglabs.bug.module.gps;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.Filter;
 import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.bug.module.gps.pub.IPositionProvider;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.bug.module.pub.IModletFactory;
 import com.buglabs.status.IStatusBarProvider;
 
 public class Activator implements BundleActivator, IModletFactory {
 	private BundleContext context;
 	private ServiceRegistration sr;
 	
 	private static Activator instance;
 	
 	public Activator() {
 		instance = this;
 	}
 	
 
 	public void start(BundleContext context) throws Exception {
 		this.context = context;
 		sr = context.registerService(IModletFactory.class.getName(), this, null);	
 		
 		Filter f = context.createFilter("(| (" + Constants.OBJECTCLASS + "=" + IStatusBarProvider.class.getName() + ") (" + Constants.OBJECTCLASS + "=" + IPositionProvider.class.getName() + "))");
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		sr.unregister();
 	}
 
 	public IModlet createModlet(BundleContext context, int slotId) {
 		GPSModlet modlet = new GPSModlet(context, slotId, getModuleId(), "GPS");
 		
 		return modlet;
 	}
 
 	public String getModuleId() {
 		return (String) context.getBundle().getHeaders().get("Bug-Module-Id");
 	}
 
 	public String getName() {		
 		return (String) context.getBundle().getHeaders().get("Bundle-SymbolicName");
 	}
 
 	public String getVersion() {		
 		return (String) context.getBundle().getHeaders().get("Bundle-Version");
 	}
 
 	public BundleContext getBundleContext() {
 		return context;
 	}
 	
 	public static Activator getInstance() {
 		return instance;
 	}
 
 }
