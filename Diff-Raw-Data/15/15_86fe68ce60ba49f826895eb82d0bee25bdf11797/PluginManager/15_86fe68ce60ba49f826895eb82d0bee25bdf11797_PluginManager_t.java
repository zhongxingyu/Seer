 package plugin;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.util.HashMap;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import javax.swing.JFileChooser;
 
 public class PluginManager implements Runnable {
 	private PluginCore core;
 	private WatchDir watchDir;
 	private HashMap<Path, Plugin> pathToPlugin;
 	private HashMap<String, Path> idToPath; 
 	public PluginManager(PluginCore core) throws IOException {
 		this.core = core;
 		this.pathToPlugin = new HashMap<Path, Plugin>();
 		this.idToPath = new HashMap<String, Path>(); 
 		watchDir = new WatchDir(this, FileSystems.getDefault().getPath("plugins"), false);
 	}
 
 	@Override
 	public void run() {
 		// First load existing plugins if any
 		try {
 			Path pluginDir = FileSystems.getDefault().getPath("plugins");
 			File pluginFolder = pluginDir.toFile();
 			File[] files = pluginFolder.listFiles();
 			if(files != null) {
 				for(File f : files) 
 				{
 					this.loadBundle(f.toPath());
 				}
 			}
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		// Listen for newly added plugins
 		watchDir.processEvents();
 	}
 
 	void loadBundle(Path bundlePath) throws Exception {
 		// Get hold of the jar file
 		File jarBundle = bundlePath.toFile();
 		JarFile jarFile;
 		try{
 			jarFile= new JarFile(jarBundle);
 			
 		}catch(Exception e){
 			System.out.println("invalid plugin Detected");
 			return;
 		}
 		
 		// Get the manifest file in the jar file
 		Manifest mf = jarFile.getManifest();
         Attributes mainAttribs = mf.getMainAttributes();
         
         // Get hold of the Plugin-Class attribute and load the class
         String className = mainAttribs.getValue("Plugin-Class");
         if (className==null) {
         	System.out.println("invalid plugin Detected");
         	jarFile.close();
 			return;
 		}
         URL[] urls = new URL[]{bundlePath.toUri().toURL()};
         ClassLoader classLoader = new URLClassLoader(urls);
         Class<?> pluginClass = classLoader.loadClass(className);
         
         // Create a new instance of the plugin class and add to the core
         Plugin plugin = (Plugin)pluginClass.newInstance();
         this.core.addPlugin(plugin);
         this.pathToPlugin.put(bundlePath, plugin);
         this.idToPath.put(plugin.getId(), bundlePath); 
         // Release the jar resources
         jarFile.close();
 	}
 	
 	void unloadBundle(Path bundlePath) 
 	{
 		Plugin plugin = this.pathToPlugin.remove(bundlePath);
 		if(plugin != null) {
 			this.core.removePlugin(plugin.getId());
 		}
 	}
 	
 	public ActionListener AddListener()
 	{
 		return new ActionListener() 
 		{
 			@Override
 			public void actionPerformed(ActionEvent e) 
 			{
 				final JFileChooser choice = new JFileChooser(); 
				choice.showOpenDialog(core.getAddButton()); 
 				File file = choice.getSelectedFile(); 
 				try 
 				{
 					loadBundle(file.toPath());
 				} 
 				catch (Exception e1) 
 				{
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 			
 		};
 	}
 	
 	public ActionListener RemoveListener()
 	{
 		return new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 			 Plugin p = (Plugin) core.pluginMap().get(javax.swing.JOptionPane.showInputDialog("Remove which Plugin?"));
 			 unloadBundle(idToPath.get(p.getId())); 
 			}
 		};
 	}
 }
