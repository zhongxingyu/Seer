 package com.netxforge.netxstudio.server.metrics.internal;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.eclipse.osgi.service.debug.DebugOptions;
 import org.eclipse.osgi.service.debug.DebugOptionsListener;
 import org.eclipse.osgi.service.debug.DebugTrace;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.util.Modules;
 import com.netxforge.netxstudio.scheduling.MetricSourceJob;
 import com.netxforge.netxstudio.server.internal.ServerModule;
 import com.netxforge.netxstudio.server.job.JobImplementation;
 import com.netxforge.netxstudio.server.job.JobImplementation.JobImplementationFactory;
 import com.netxforge.netxstudio.server.job.internal.JobModule;
 import com.netxforge.netxstudio.server.metrics.MetricSourceImportService;
 import com.netxforge.netxstudio.server.metrics.MetricSourceJobImplementation;
 
 public class MetricsActivator implements BundleActivator, DebugOptionsListener
 	 {
 
 	private static BundleContext context;
 	private static MetricsActivator INSTANCE;
 
 	private static final String PLUGIN_ID = "com.netxforge.netxstudio.server.metrics";
 
 	// fields to cache the debug flags
 	public static boolean DEBUG = false;
 	public static DebugTrace TRACE = null;
 
 	// public tracing options.
 	public static final String TRACE_IMPORT_OPTION = "/trace.import";
 
 	public void optionsChanged(DebugOptions options) {
 		DEBUG = options.getBooleanOption(PLUGIN_ID + "/debug", false);
 		TRACE = options.newDebugTrace(PLUGIN_ID);
 	}
 
 	public static BundleContext getContext() {
 		return context;
 	}
 
 	public static MetricsActivator getInstance() {
 		return INSTANCE;
 	}
 
 	private Injector injector;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void start(BundleContext bundleContext) throws Exception {
 		INSTANCE = this;
 		MetricsActivator.context = bundleContext;
 
 		Module om = new MetricsModule();
 		om = Modules.override(om).with(ServerModule.getModule());
 		om = Modules.override(om).with(new JobModule());
 		injector = Guice.createInjector(om);
 
 		// register our import service.
 		bundleContext.registerService(MetricSourceImportService.class,
 				new MetricSourceImportService(),
 				new Hashtable<String, String>());
 
 		// register our new MetricSourceJob creator
 		JobImplementation.REGISTRY.register(MetricSourceJob.class,
 				new JobImplementationFactory() {
 					@Override
 					public JobImplementation create() {
 						return injector
 								.getInstance(MetricSourceJobImplementation.class);
 					}
 				});
 
 		Dictionary<String, String> props = new Hashtable<String, String>(4);
 		props.put(DebugOptions.LISTENER_SYMBOLICNAME, PLUGIN_ID);
 		context.registerService(DebugOptionsListener.class.getName(), this,
 				props);
 
 	}
 
 	
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		MetricsActivator.context = null;
 	}
 
 	public Injector getInjector() {
 		return injector;
 	}
 }
