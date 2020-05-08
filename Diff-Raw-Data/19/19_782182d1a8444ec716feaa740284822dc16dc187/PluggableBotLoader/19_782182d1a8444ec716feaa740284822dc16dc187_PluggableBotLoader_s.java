 package AndrewCassidy.PluggableBot;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 
 /**
  * 
  * @author m.ellis (ellism88@gmail.com)
  */
 class PluggableBotLoader implements Runnable {
 	private String name;
 	private URL[] urls;
 
 	public PluggableBotLoader(String name, URL[] urls) {
 		this.name = name;
 		this.urls = urls;
 	}
 
 	@Override
 	public void run() {
 		try {
 			URLClassLoader newLoader = new URLClassLoader(urls);
 			Plugin p = (Plugin) newLoader.loadClass(name).newInstance();
 			PluggableBot.addPlugin(name, p);// loadedPlugins.put(name, p);
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 }
