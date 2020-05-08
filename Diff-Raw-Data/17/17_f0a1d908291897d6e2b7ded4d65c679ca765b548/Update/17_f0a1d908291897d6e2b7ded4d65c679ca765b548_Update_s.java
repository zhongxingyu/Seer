 package com.Jessy1237.renamer;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.swing.JOptionPane;
 
 public class Update {
 
 	public static String userHome = System.getProperty("user.home");
 	public static File dir = new File(userHome, "/McVSTemp");
 	public static File ver = new File(dir, "/version.txt");
 	static Properties prop = new Properties();
 	static String dUrl;
 	static double version;
 	static int b = 0;
 
 	
 	public static void download(){
 		try{
 			dir.mkdir();
 			BufferedInputStream in = new BufferedInputStream(new java.net.URL("http://dl.dropbox.com/u/49659014/McVS/version.txt").openStream());
 			FileOutputStream fos = new FileOutputStream(ver);
 			BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
 			byte[] data = new byte[1024];
 			int x=0;
 			while((x=in.read(data,0,1024))>=0){
 				bout.write(data,0,x);
 			}
 			bout.close();
 			in.close();
 			readUpdate();
 		}catch(IOException e){
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(null, "Unable to check server for update, is your internet working?", "Minecraft Version Switcher", 1);
 		}
 	}
 	
 	public static void readUpdate() throws IOException{
 		FileInputStream in = new FileInputStream(ver);
 		prop.load(in);
 		version = Double.parseDouble(prop.getProperty("Version"));
 		dUrl = prop.getProperty("dLink");
 		in.close();
 		if(version > Opener.vernum){
 			Updatep();
 		}else{
 			JOptionPane.showMessageDialog(null, "No update is available, you are up to date.", "Minecraft Version Switcher", 1);
 		}
 	}
 	
 	public static void Updatep() throws IOException{
 		int pane = JOptionPane.showConfirmDialog(null, "Update is available, Would you like to update to version " + version + "?", "Minecraft Version Switcher", JOptionPane.YES_NO_OPTION);
 		if(pane ==  0){
 			dLoadJars.v = "McVS";
 			Download p = new Download();
 			p.setVisible(true);
			JOptionPane.showMessageDialog(null, "Finished downloading the update, Delete this version and use the new one on your desktop.", "Minecraft Versions Switcher", 1);
 		}
 	}
 }
