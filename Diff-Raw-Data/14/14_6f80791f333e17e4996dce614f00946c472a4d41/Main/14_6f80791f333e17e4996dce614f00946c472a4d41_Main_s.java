 package com.cpcookieman.cookieirc;
 
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 
 public class Main extends com.cpcookieman.common
 {
 	protected static JFrame frame;
 	public static final String title = "CookieIRC";
 	public static final String version = "v2.0";
	public static final String buildTag = "cpcookieman@glidebook";
 	public static String windowTitle = title + " " + version;
 	public static int width = 1024;
 	public static int height = 600;
 	public static GUI gui;
 	public static int i;
 	public static String user = "";
 	public static ArrayList<ServerConnection> servers = new ArrayList<ServerConnection>();
 	public static int servercounter = -1;
 	
 	public static void main(String[] args)
 	{
 		//setDebug(true);
 		nativeLook();
 		frame = new JFrame();
 		frame.setVisible(false);
 		frame.setTitle(windowTitle);
 		if(getDebug())
 		{
 			windowTitle = windowTitle + " - " + buildTag + " - DEBUG";
 			frame.setTitle(windowTitle);
 		}
 		debug("Starting " + windowTitle);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(width, height);
 		debug("Set size to " + width + "x" + height);
 		frame.setLocationRelativeTo(null);
		user = inputBox("Please enter a default nickname:");
 		gui = new GUI();
 		frame.add(gui);
 		debug("Added GUI to Frame");
 		i = gui.addTab(new Tab("CookieIRC Console", false, -1));
 		gui.tabs.get(i).addMessage("Welcome to " + windowTitle + "!");
 		gui.tabs.get(i).addMessage("");
 		gui.tabs.get(i).addMessage(getRandomSaying());
 		gui.tabs.get(i).addMessage("");
 		gui.tabs.get(i).addMessage("/connect to servers");
 		gui.tabs.get(i).addMessage("/join #channels");
 		gui.tabs.get(i).addMessage("/msg people");
 		frame.setVisible(true);
 		debug("Frame has been set to visible");
 		gui.tabs.get(i).input.requestFocus();
 		
 	}
 }
