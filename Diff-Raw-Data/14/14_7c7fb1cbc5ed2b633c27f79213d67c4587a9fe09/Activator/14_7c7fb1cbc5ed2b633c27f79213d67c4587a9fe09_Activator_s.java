 package net.nansore.cedalion.eclipse;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetSocketAddress;
 import java.net.Proxy;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import net.nansore.cedalion.execution.TermInstantiator;
 import net.nansore.prolog.Compound;
 import net.nansore.prolog.PrologException;
 import net.nansore.prolog.PrologProxy;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "net.nansore.cedalion";
 
 	// The shared instance
 	private static Activator plugin;
 
 	//Resource bundle.
 	private ResourceBundle resourceBundle;
     private BundleContext context;
     
 //    private PackageLoader pkgLoader;
 
 	private PrologProxy prolog; 
 	
 	/**
 	 * The constructor.
 	 */
 	public Activator() {
 		super();
 		plugin = this;
 	}
 
 	/**
 	 * This method is called upon plug-in activation
 	 */
 	public void start(BundleContext context) throws Exception {
         try {
             prolog = new PrologProxy(loadToFileSystem(context, "service.pl"));
     		prolog.getSolution(prolog.createCompound("loadFile", loadToFileSystem(context, "procedure.ced").toString(), "cedalion1"));
 
     		super.start(context);
             this.context = context;
 
             IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
             loadResources(root);
             
             Proxy proxy = null;
             if(getPreferenceStore().getBoolean("useProxy")) {
             	proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getPreferenceStore().getString("proxyHost"), getPreferenceStore().getInt("proxyPort")));
             }
             	
             //pkgLoader = new PackageLoader(getStateLocation().toFile(), proxy);
             //loadNamespaces();
         } catch(Exception e) {
             e.printStackTrace();
             throw e;
         }
 	}
 
 //	public void loadNamespaces() throws PrologException, IOException {
 //		pkgLoader.loadNamespaces();
 //	}
 
     private File loadToFileSystem(BundleContext context, String fileName) throws IOException, FileNotFoundException, PrologException {
 		File tmpFile = File.createTempFile("tmp", ".ced");
 		URL prologFileURL = context.getBundle().getEntry(fileName);
 		URLConnection connection = prologFileURL.openConnection();
 		InputStream input = connection.getInputStream();
 		FileOutputStream output = new FileOutputStream(tmpFile);
 		byte[] data = new byte[1024]; 
 		while(true) {
 		    int numBytes = input.read(data);
 		    if(numBytes < 0)
 		        break;
 			output.write(data, 0, numBytes);
 		}
 		output.close();
 //		Compound q = new Compound("consult", tmpFile.getAbsolutePath());
 //		System.out.println( "consult of " + tmpFile.getAbsolutePath() + (PrologClient.hasSolution(q) ? " succeeded" : " failed"));
 		return tmpFile;
 	}
 
 	private void loadResources(IContainer container) throws CoreException {
 		IResource[] members = container.members(true);
 		for(int i = 0; i < members.length; i++) {
 			String ext = members[i].getFileExtension();
 			if(ext != null && ext.equals("ced")) {
 				loadResource(members[i]);
 			}
 			if(members[i] instanceof IContainer) {
                 if(!(members[i] instanceof IProject && !((IProject)members[i]).isOpen()))
                     loadResources((IContainer)members[i]);
 			}
 		}
 	}
 
	private void loadResource(IResource resource) {
 		String resourcePath = resource.getFullPath().toString();
 		System.out.println("Loading: " + resourcePath);
 		String filePath = resource.getLocation().toString();
 		String pkg = resource.getParent().getFullPath().toString();
 		try {
 			prolog.getSolution(prolog.createCompound("loadFile", filePath, pkg));
 		} catch (PrologException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This method is called when the plug-in is stopped
 	 */
 	public void stop(BundleContext context) throws Exception {
         stopPServer();
 		super.stop(context);
 	}
 
 	private void stopPServer() throws IOException {
         prolog.terminate();
     }
 
     /**
 	 * Returns the shared instance.
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Returns the string from the plugin's resource bundle,
 	 * or 'key' if not found.
 	 */
 	public static String getResourceString(String key) {
 		ResourceBundle bundle = Activator.getDefault().getResourceBundle();
 		try {
 			return (bundle != null) ? bundle.getString(key) : key;
 		} catch (MissingResourceException e) {
 			return key;
 		}
 	}
 
 	/**
 	 * Returns the plugin's resource bundle,
 	 */
 	public ResourceBundle getResourceBundle() {
 		return resourceBundle;
 	}
 
     /**
      * @return
      */
     public BundleContext getContext() {
         return context;
     }
 
     /**
      * @return
      * @throws IOException
      * @throws CoreException
      */
     public Image getZoomInImage(Display display) throws IOException  {
 		URL prologFileURL = context.getBundle().getEntry("icons/increase.gif");
 		URLConnection connection = prologFileURL.openConnection();
 		InputStream input = connection.getInputStream();
         return new Image(display, input);
     }
 
     /**
      * @return
      * @throws IOException
      * @throws CoreException
      */
     public Image getZoomOutImage(Display display) throws IOException {
 		URL prologFileURL = context.getBundle().getEntry("icons/decrease.gif");
 		URLConnection connection = prologFileURL.openConnection();
 		InputStream input = connection.getInputStream();
         return new Image(display, input);
     }
 
     /**
      * @return
      * @throws IOException
      * @throws CoreException
      */
     public Image getRefreshImage(Display display) throws IOException {
 		URL prologFileURL = context.getBundle().getEntry("icons/refresh.gif");
 		URLConnection connection = prologFileURL.openConnection();
 		InputStream input = connection.getInputStream();
         return new Image(display, input);
     }
 
 	public static PrologProxy getProlog() {
 		return getDefault().prolog;
 	}
 
 }
