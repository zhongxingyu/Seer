 package com.vloxlands.settings;
 
 import java.io.File;
 import java.util.Arrays;
 
 import com.vloxlands.util.Assistant;
 
 public class CFG
 {
 	// -- UniVersion -- //
 	public static final int VERSION = 2013090102;
 	public static final int PHASE = 0;
 	
 	// -- debug -- //
 	public static boolean DEBUG = true;
 	public static boolean INTERNET;
 	
 	// -- settings -- //
 	public static int FOV = 30;
 	public static int FPS = 60;
 	public static boolean LIGHTING = false;
 	public static boolean FULLSCREEN = false;
 	public static boolean SHOW_CHUNK_BOUNDRIES = false;
 	public static boolean SHOW_DIRECTIONS = false;
 	public static boolean SHOW_WIREFRAME = false;
 	
 	public static final File DIR = new File(System.clearProperty("user.home") + "/.dakror/Vloxlands");
 	
 	static
 	{
 		DIR.mkdir();
 		String[] fs = {};
 		for (String f : fs)
 		{
 			String cs = Assistant.getFolderChecksum(new File(DIR, f));
 			p(f + ": " + cs);
 		}
 	}
 	
 	static long time = 0;
 	
 	// -- debug profiling -- //
 	public static void u()
 	{
 		if (time == 0) time = System.currentTimeMillis();
 		else
 		{
 			CFG.p(System.currentTimeMillis() - time);
 			time = 0;
 		}
 	}
 	
 	public static void p(Object... p)
 	{
		if (p.length == 1) System.out.println(p[0]);
 		System.out.println(Arrays.toString(p));
 	}
 	
 	public static void b(Object... b)
 	{
 		String s = "";
 		for (int i = 0; i < b.length; i += 2)
 		{
 			s += b[i] + ": " + b[i + 1] + ",";
 		}
 		p(s.substring(0, s.length() - 1));
 	}
 }
