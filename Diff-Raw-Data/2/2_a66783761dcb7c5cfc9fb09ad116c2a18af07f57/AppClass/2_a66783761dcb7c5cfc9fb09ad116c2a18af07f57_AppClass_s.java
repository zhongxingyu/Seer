 package com.github.wolf480pl.test.app;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.Policy;
 
 public class AppClass {
 
 	/**
 	 * @param args
 	 * @throws MalformedURLException 
 	 * @throws ClassNotFoundException 
 	 * @throws IllegalAccessException 
 	 * @throws InstantiationException 
 	 */
 	public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
 		boolean restrict= false;
 		if (args.length > 1) {
 			restrict = Boolean.parseBoolean(args[1]);
 		}
 		Policy.setPolicy(new MyPolicy(restrict));
 		System.setSecurityManager(new SecurityManager());
 		File pluginFile = new File(args[0]);
 		String className;
 		if (args.length > 2) {
 			className = args[2];
 		} else {
 			className = "com.github.wolf480pl.test.plugin.PluginClass";
 		}
 		URLClassLoader loader = new URLClassLoader(new URL[]{pluginFile.toURI().toURL()}, AppClass.class.getClassLoader());
 		try {
 			Class.forName(className, true, loader).newInstance();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		}
		new SecurityManager();	//Check if we are stoll allowed to do this.
 	}
 
 }
