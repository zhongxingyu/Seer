 package studentview;
 
 //Andy Carle, Berkeley Institute of Design, UC Berkeley
 
 
 import java.util.ArrayList;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 import studentview.controller.NavigationListener;
 import studentview.model.Step;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class NavigatorActivator extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "StudentView"; //$NON-NLS-1$
 
 	// The shared instance
 	private static NavigatorActivator plugin;
 	
 	/**
 	 * The constructor
 	 */
 	public NavigatorActivator() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static NavigatorActivator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given
 	 * plug-in relative path
 	 *
 	 * @param path the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 	
 	
 	/////////////////////
 	
 	//EduRideJunitView 
 
	ArrayList<NavigationListener> listeners;
 	
 	public boolean registerListener(NavigationListener l) {
 		return(listeners.add(l));
 	}
 	
 	public boolean removeListener(NavigationListener l) {
 		return (listeners.remove(l));
 	}
 	
 	// called when the step is changed in the view
 	public void stepChanged(Step newstep){
 		for (NavigationListener l : listeners) {
 			l.stepChanged(newstep);
 		}
 	}
 	
 	// called when the junit test should be run
 	public  void invokeTest(Class<?> testclass){
 		for (NavigationListener l : listeners) {
 			l.invokeTest(testclass);
 		}
 	}
 	
 	
 }
