 package net.GUIpsp.GuiBot;
 
 import java.io.*;
 
 import java.net.*;
 import java.util.*;
 import org.jibble.pircbot.*;
 
 public class Main {
 	public static Map classmap = new HashMap();
 	public static Map cmdmap = new HashMap();
 	public static Map<String, String> helpmap = new HashMap<String, String>();
 	static String plugdir = "/home/guipsp/Desktop/GuiBot/plugins/";
 	static File directory = new File(plugdir);
 	public static GuiBot bot = new GuiBot();

 	public static void main(String[] args) throws Throwable {
 		reLoad();
 		bot.setVerbose(true);
 		try {
 			bot.connect("irc.esper.net");
 		} catch (NickAlreadyInUseException e) {
 			System.out.println("Nickname is in use.");
 		}
 		// bot.identify("");
 		bot.joinChannel("##crow");
 	}
	
 	public static void reLoad() throws Throwable {
 		classmap.clear();
 		cmdmap.clear();
 		helpmap.clear();
 		URL classUrl;
 		classUrl = new URL("file://" + plugdir);
 		URL[] classUrls = { classUrl };
 		URLClassLoader ucl = new URLClassLoader(classUrls);
 		File files[] = directory.listFiles();
 		int count = 0;
 		for (File f : files) {
 			if (f.getName().endsWith(".class")) {
 				String safename = f.getName().replaceAll(".class", "");
 				Class<?> cls = ucl.loadClass(safename);
 				BasePlugin inst = (BasePlugin) ((Class<?>) cls).newInstance();
 				inst.getClass().getMethod("main").invoke(inst);
 				System.out.println("Loaded " + inst.pluginName() + " "
 						+ inst.version() + " by " + inst.author());
 				count++;
 			}
 		}
 		System.out.println(count + " plugins");
 	}
 }
