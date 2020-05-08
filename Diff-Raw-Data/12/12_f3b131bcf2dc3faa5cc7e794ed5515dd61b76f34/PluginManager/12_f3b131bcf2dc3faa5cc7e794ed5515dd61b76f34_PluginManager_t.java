 package mozeq.irc.bot;
 import java.io.File;
 import java.io.FileFilter;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 /**
  * A class that implements the Java FileFilter interface.
  */
 class PluginFileFilter implements FileFilter
 {
   private final String[] pluginExtensions =
     new String[] {"class"};
 
   public boolean accept(File file)
   {
     for (String extension : pluginExtensions)
     {
       if (file.getName().toLowerCase().endsWith(extension))
       {
         return true;
       }
     }
     return false;
   }
 }
 
 public class PluginManager {
 
 
 	String dirname = "";
 	ClassLoader pluginLoader = PluginManager.class.getClassLoader();
 	PluginTable pluginTable = new PluginTable();
 
 	PluginManager(String pluginDir)
 	{
 		dirname = pluginDir;
 		//System.out.println("Searching for plugins in: '" + pluginDir + "'");
 
 	}
 
 	void enablePlugin(IrcBotPlugin plugin) {
 		pluginTable.registerPlugin(plugin);
 	}
 
 	void loadPlugins()
 	{
 		File dir = new File(dirname);
 	    File[] plugins = dir.listFiles(new PluginFileFilter());
 
 
 	    for (File f : plugins)
 	    {
 	    	String filename = f.getName();
 	    	String pluginname = filename.substring(0, filename.length() - ".class".length());
 
 	    	//workaround for loading classes which are not plugins - FIXME
 	    	if (!pluginname.contains("Plugin"))
 	    		continue;
 
 			//if (!pluginname.equals("U3OpicPlugin"))
 			//	continue;
 
 	    	//System.out.println(pluginname);
 	    	try {
 
 	    		System.out.println("Loading plugin " + pluginname);
 
 				@SuppressWarnings("unchecked")
 				Class<IrcBotPlugin> cls = (Class<IrcBotPlugin>) pluginLoader.loadClass("mozeq.irc.bot.plugins." + pluginname);
 				IrcBotPlugin plugin = null;
 				try {
 					plugin = (IrcBotPlugin) cls.getConstructor().newInstance();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				plugin.init();
 
 				enablePlugin(plugin);
 
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    }
 
 	}
 
 	ArrayList<String> runPluginActions(IrcMessage message){
 
 		ArrayList<String> retval = new ArrayList<String>();
 
 		for(String command: pluginTable.getCommands()) {
 
 			if (message.body.contains(command)) {
 				for (IrcBotPlugin plugin : pluginTable.getActions(command)) {
 					ArrayList<String> res = plugin.run(message, command);
 
 					if(res == null)
 						continue;
 
 					for(String s: res) {
 						retval.add(s);
 					}
 				}
 			}
 		}
 		return retval;
 	}
 }
