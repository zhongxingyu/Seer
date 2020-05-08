 package com.sin.plugins;
 
 public class Config {
 	public static String name = "Slowloris";
 	public static String version = "1.8";
 	public static String host = "localhost";
 	public static String torPassword = "";
 	public static int port = 80;
 	public static int connections = 2;
 	public static int numThreads = 500;
 	public static int timeout = 60;
 	public static int torChange = 10;
 	public static boolean tor = true;
 	public static boolean verbose = false;
 	
	
 	public static void parseArgs(String[] args)
 	{
 
 		try
 		{
 			host =  args[0];
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 		try
 		{
 			torPassword = args[6];
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 		try
 		{
 			port = Integer.parseInt(args[1]);
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 		try
 		{
 			connections = Integer.parseInt(args[2]);
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 		try
 		{
 			numThreads = Integer.parseInt(args[3]);
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 		try
 		{
 			timeout = Integer.parseInt(args[4]);
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 		try
 		{
 			torChange = Integer.parseInt(args[7]);
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 		try
 		{
 			tor = (args[5].compareTo("1") == 0 ? true : false);
 		}
 		catch(Exception e)
 		{
 			
 		}
 		
 		try
 		{
 			verbose = (args[8].compareTo("1") == 0 ? true : false);
 		}
 		catch(Exception e)
 		{
 			
 		}
 	}
 }
