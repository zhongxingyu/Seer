 package rxr;
 
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 import javax.swing.*;
 
 import rxr.action.*;
 import rxr.ui.*;
 import rxr.util.*;
 
 public class RXR extends JApplet
 {
 	private static final long serialVersionUID = 1L;
 
 	public static final Properties props = new Properties();
 
 	public static JFrame window;
 
 	static boolean applet = false;
 
 	public static String userRoot;
 
 	public static PrintWriter log;
 
 	@Override
 	public void start()
 	{
		RXR.applet = true;
 		main(null);
 	}
 
 	/**
 	 * Entry point into regexerator.
 	 * 
 	 * @param args
 	 *            not used
 	 */
 	public static void main(String[] args)
 	{
 		if(!applet)
 		{
 			userRoot = System.getProperty("user.home") + "/.rxr/";
 			try
 			{
 				log = new PrintWriter(new FileWriter(userRoot + "rxr.log"), true);
 			}
 			catch(Exception e)
 			{
 				WindowUtil.error(e, "Cannot open log file", true);
 			}
 		}
 
 		log("Starting Regexerator");
 
 		ArrayList<String> defaultPropUrls = new ArrayList<String>();
 
 		defaultPropUrls.add("res/props/rxr.properties");
 		defaultPropUrls.add("res/props/rxr.settings.properties");
 
 		ArrayList<String> userPropUrls = new ArrayList<String>();
 		userPropUrls.add(userRoot + "rxr.settings.properties");
 
 		for(String s : defaultPropUrls)
 		{
 			try
 			{
 				props.load(load(s).openStream());
 			}
 			catch(Exception e)
 			{
 				log("Failed to load properties file: " + s);
 				WindowUtil.error(e, "Cannot load properties file: " + s, true);
 			}
 		}
 
 		if(!applet)
 		{
 			for(String s : userPropUrls)
 			{
 				try
 				{
 					File f = new File(s);
 					SystemUtil.createFile(f);
					FileReader fr = new FileReader(f);
					props.load(fr);
					fr.close();
 				}
 				catch(Exception e)
 				{
 					log("Failed to load user properties file: " + s);
 					WindowUtil.error(e, "Cannot load user properties file: " + s, true);
 				}
 			}
 		}
 
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch(Exception e)
 		{
 			//don't bother with PLAF then
 		}
 
 		MainPanel main = new MainPanel();
 
 		window = new JFrame("Regexerator");
 		if(!applet)
 		{
 			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			window.addWindowListener(new WindowAdapter()
 			{
 				@Override
 				public void windowClosing(WindowEvent e)
 				{
 					//cleanup
 					log("Stopping Regexerator");
 					log.close();
 				}
 			});
 		}
 
 		window.setJMenuBar(createMenuBar());
 
 		window.add(main);
 
 		window.setSize(600, 400);
 		WindowUtil.center(window);
 		window.setVisible(true);
 	}
 
 	public static JMenuBar createMenuBar()
 	{
 		JMenuBar menu = new JMenuBar();
 
 		//top level
 		JMenu file = new JMenu("File");
 		JMenu help = new JMenu("Help");
 
 		//file
 		JMenuItem quit = new JMenuItem(new QuitAction());
 
 		//file - add
 		file.add(quit);
 
 		//help
 		JMenuItem helpItem = new JMenuItem(new HelpAction());
 		JMenuItem about = new JMenuItem(new AboutAction());
 
 		//help - add
 		help.add(helpItem);
 		help.add(new JSeparator());
 		help.add(about);
 
 		//add top
 		menu.add(file);
 		menu.add(help);
 
 		return menu;
 	}
 
 	/**
 	 * Performs a call to getResouce() that occurs in a predictable manner. In a
 	 * jar, the root will be the jar root. Outside of a jar, it will be the root
 	 * of the class/package hierarchy.
 	 * 
 	 * @param path
 	 *            the resource path
 	 * @return a URL to the requested resource
 	 */
 	public static URL load(String path)
 	{
 		return RXR.class.getClassLoader().getResource(path);
 	}
 
 	public static String get(String prop)
 	{
 		return props.getProperty(prop);
 	}
 
 	public static void log(String s)
 	{
 		if(!applet)
 		{
 			log.print('[');
 			log.print(new Date());
 			log.print("] ");
 			log.println(s);
 		}
 	}
 }
