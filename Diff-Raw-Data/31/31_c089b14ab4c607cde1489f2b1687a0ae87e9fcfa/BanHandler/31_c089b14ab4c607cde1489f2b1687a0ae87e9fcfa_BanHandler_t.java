 package com.gamezgalaxy.GGS.system;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class BanHandler {
 	static ArrayList<String> banned = new ArrayList<String>();
 	
 	public static void init() {
		banned.clear();

 		try {
 			if (!new File("properties/banned.txt").exists())
 				new File("properties/banned.txt").createNewFile();
 			FileInputStream fstream = new FileInputStream("properties/banned.txt");
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
 			String line;
 			while((line = br.readLine()) != null)
 				banned.add(line);
 
 			fstream.close();
 			in.close();
 			br.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void save() throws FileNotFoundException {
 		if (new File("properties/banned.txt").exists())
 			new File("properties/banned.txt").delete();
 		PrintWriter out = new PrintWriter("properties/banned.txt");
 		for (String s : banned) {
 			out.println(s);
 		}
 		out.close();
 	}
 	
 	public static String getExpire(String name) {
 		for (String s : banned) {
 			if (s.split("\\:")[0].equalsIgnoreCase(name))
 				return s.split("\\:")[1];
 		}
 		return "";
 	}
 	
 	public static void ban(String username, String expire) {
 		banned.add(username + ":" + expire);
 		try {
 			save();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static void ban(String username) {
 		ban(username, "null");
 	}
 	
 	public static void ban(String username, Date date) {
 		ban(username, date.toString());
 	}
 	
 	public static boolean pastDate(String username) {
 		String date = getExpire(username);
 		if (date.equals("null"))
 			return false;
 		Date now = new Date();
 		Date then = new Date(date);
 		return then.before(now);
 	}
 	
 	public static boolean isBanned(String name) {
		init();

 		for (String s : banned) {
 			if (s.split("\\:")[0].equalsIgnoreCase(name))
 				return !pastDate(name);
 		}
 		return false;
 	}
 
 }
