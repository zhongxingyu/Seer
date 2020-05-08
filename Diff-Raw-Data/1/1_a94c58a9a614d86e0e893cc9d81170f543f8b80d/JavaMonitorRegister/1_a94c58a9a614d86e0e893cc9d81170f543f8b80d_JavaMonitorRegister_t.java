 package com.athaydes.osgimonitor.impl;
 
 import com.athaydes.osgimonitor.api.BundleData;
 import com.athaydes.osgimonitor.api.MonitorRegister;
 import com.athaydes.osgimonitor.api.OsgiMonitor;
 import com.athaydes.osgimonitor.api.ServiceData;
 import org.osgi.framework.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class JavaMonitorRegister implements MonitorRegister,
 		BundleListener, ServiceListener {
 
 	private final List<OsgiMonitor> monitors = new ArrayList<>( 2 );
 	private final BundleContext context;
 
 	public JavaMonitorRegister( BundleContext context ) {
 		this.context = context;
 		context.addBundleListener( this );
 		context.addServiceListener( this );
 	}
 
 	@Override
 	public void bundleChanged( BundleEvent bundleEvent ) {
 		BundleData data = new BundleData(
 				bundleEvent.getBundle().getSymbolicName(),
 				toStateString( bundleEvent.getType() ) );
 		synchronized ( monitors ) {
 			for ( OsgiMonitor monitor : monitors ) {
 				monitor.updateBundle( data );
 			}
 		}
 	}
 
 	@Override
 	public void serviceChanged( ServiceEvent serviceEvent ) {
 		ServiceData data = new ServiceData(
 				serviceEvent.getServiceReference().getBundle().getSymbolicName(),
 				usingBundleNames( serviceEvent.getServiceReference() ),
 				serviceState( serviceEvent.getType() ),
 				serviceProperties( serviceEvent.getServiceReference() ) );
 		synchronized ( monitors ) {
 			for ( OsgiMonitor monitor : monitors ) {
 				monitor.updateService( data );
 			}
 		}
 	}
 
 	private static String[] usingBundleNames( ServiceReference serviceReference ) {
 		Bundle[] usingBundles = serviceReference.getUsingBundles();
		if ( usingBundles == null ) usingBundles = new Bundle[0];
 		String[] result = new String[usingBundles.length];
 		for ( int i = 0; i < usingBundles.length; i++ ) {
 			result[i] = usingBundles[i].getSymbolicName();
 		}
 		return result;
 	}
 
 	private ServiceReference[] getAllServiceReferences() {
 		try {
 			return context.getAllServiceReferences( null, null );
 		} catch ( InvalidSyntaxException e ) {
 			return new ServiceReference[0];
 		}
 	}
 
 	@Override
 	public boolean register( OsgiMonitor osgiMonitor ) {
 		provideCurrentBundleDataFor( osgiMonitor );
 		provideCurrentServiceDataFor( osgiMonitor );
 		synchronized ( monitors ) {
 			return monitors.add( osgiMonitor );
 		}
 	}
 
 	private void provideCurrentBundleDataFor( OsgiMonitor osgiMonitor ) {
 		for ( Bundle bundle : context.getBundles() ) {
 			BundleData data = new BundleData(
 					bundle.getSymbolicName(),
 					toStateString( bundle.getState() ) );
 			osgiMonitor.updateBundle( data );
 		}
 	}
 
 	private void provideCurrentServiceDataFor( OsgiMonitor osgiMonitor ) {
 		for ( ServiceReference reference : getAllServiceReferences() ) {
 			ServiceData data = new ServiceData(
 					reference.getBundle().getSymbolicName(),
 					usingBundleNames( reference ),
 					serviceState( ServiceEvent.REGISTERED ),
 					serviceProperties( reference )
 			);
 			osgiMonitor.updateService( data );
 		}
 	}
 
 	private Map<String, Object> serviceProperties( ServiceReference reference ) {
 		Map<String, Object> result = new HashMap<>();
 		for ( String key : reference.getPropertyKeys() ) {
 			result.put( key, reference.getProperty( key ) );
 		}
 		return result;
 
 	}
 
 	@Override
 	public boolean unregister( OsgiMonitor osgiMonitor ) {
 		synchronized ( monitors ) {
 			return monitors.remove( osgiMonitor );
 		}
 	}
 
 	public static String toStateString( int state ) {
 		switch ( state ) {
 			case BundleEvent.LAZY_ACTIVATION:
 				return "Lazy Activation";
 			case BundleEvent.INSTALLED:
 				return "Installed";
 			case BundleEvent.UNINSTALLED:
 				return "Uninstalled";
 			case BundleEvent.RESOLVED:
 				return "Resolved";
 			case BundleEvent.UNRESOLVED:
 				return "Unresolved";
 			case BundleEvent.STARTING:
 				return "Starting";
 			case BundleEvent.STARTED:
 				return "Started";
 			case BundleEvent.STOPPED:
 				return "Stopped";
 			case BundleEvent.STOPPING:
 				return "Stopping";
 			case BundleEvent.UPDATED:
 				return "Updated";
 			default:
 				return "Unknown";
 		}
 	}
 
 	public static String serviceState( int type ) {
 		switch ( type ) {
 			case ServiceEvent.MODIFIED:
 				return "Modified";
 			case ServiceEvent.MODIFIED_ENDMATCH:
 				return "Modified (endmatch)";
 			case ServiceEvent.REGISTERED:
 				return "Registered";
 			case ServiceEvent.UNREGISTERING:
 				return "Unregistering";
 			default:
 				return "Unknown";
 		}
 	}
 
 
 }
