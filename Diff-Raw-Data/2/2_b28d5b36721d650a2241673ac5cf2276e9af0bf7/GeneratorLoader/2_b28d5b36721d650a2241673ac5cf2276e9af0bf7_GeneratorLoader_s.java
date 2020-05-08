 package com.wolvencraft.prison.mines.util;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.List;
 import java.util.logging.Level;
 
 import com.wolvencraft.prison.mines.CommandManager;
 import com.wolvencraft.prison.mines.generation.BaseGenerator;
 
 public class GeneratorLoader {
 	@SuppressWarnings("resource")
 	public static List<BaseGenerator> load(List<BaseGenerator> generators) {
		File dir = new File(CommandManager.getPlugin().getDataFolder() + "/generators");
 	
 		if (!dir.exists()) {
 		    dir.mkdir();
 		    return generators;
 		}
 		
 		ClassLoader loader;
 		try {
 			loader = new URLClassLoader(new URL[] { dir.toURI().toURL() }, BaseGenerator.class.getClassLoader());
 		} catch (MalformedURLException ex) {
 			Message.log(Level.SEVERE, "Error while configuring generator class loader [MalformedURILException]");
 			return generators;
 		}
 			
 		for (File file : dir.listFiles()) {
 		    if (!file.getName().endsWith(".class")) continue;
 		    
 		    String name = file.getName().substring(0, file.getName().lastIndexOf("."));
 	
 		    try {
 				Class<?> clazz = loader.loadClass(name);
 				Object object = clazz.newInstance();
 				if (!(object instanceof BaseGenerator)) {
 				    Message.log(clazz.getSimpleName() + " is not a generator class");
 				    continue;
 				}
 				BaseGenerator generator = (BaseGenerator) object;
 				generators.add(generator);
 				Message.log("Loaded generator: " + generator.getClass().getSimpleName());
 		    }
 		    catch(ClassNotFoundException cnfe) 			{ Message.log(Level.WARNING, "Error loading " + name + "! Generator disabled. [ClassNotFoundException]"); }
 		    catch(InstantiationException ie) 			{ Message.log(Level.WARNING, "Error loading " + name + "! Generator disabled. [InstantiationException]"); }
 		    catch(IllegalAccessException iae) 			{ Message.log(Level.WARNING, "Error loading " + name + "! Generator disabled. [IllegalAccessException]"); }
 		    catch (Exception ex) 						{ Message.log(Level.WARNING, "Error loading " + name + "! Generator disabled. [Exception]"); }
 		    catch (ExceptionInInitializerError eiie) 	{ Message.log(Level.WARNING, "Error loading " + name + "! Generator disabled. [ExceptionInInitializer]"); }
 		    catch (Error ex) 							{ Message.log(Level.WARNING, "Error loading " + name + "! Generator disabled. [Error]"); }
 		}
 		
 		return generators;
     }
 }
