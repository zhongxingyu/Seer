 package org.eclipse.emf.compare.examples.export.xslt;
 
 import org.eclipse.core.runtime.Plugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class XSLTExportPlugin extends Plugin {
 	/** This plug-in's ID. */
 	public static final String PLUGIN_ID = "org.eclipse.emf.compare.examples.export.xslt"; //$NON-NLS-1$
 
 	/** This plug-in's shared instance. */
 	private static XSLTExportPlugin plugin;
	
 	/**
 	 * Default constructor.
 	 */
 	public XSLTExportPlugin() {
 		// nothing to initialize
 	}
 
 	/**
 	 * {@inheritDoc}
	 *
 	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	/**
 	 * {@inheritDoc}
	 *
 	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
	 *
 	 * @return the shared instance
 	 */
 	public static XSLTExportPlugin getDefault() {
 		return plugin;
 	}
 }
