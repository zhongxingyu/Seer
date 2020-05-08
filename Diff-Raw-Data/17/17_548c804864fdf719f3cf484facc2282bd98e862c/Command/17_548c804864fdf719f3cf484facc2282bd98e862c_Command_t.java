 package com.game.rania.userdata;
 
 public class Command
 {
 	//commands list
 	public static int none          = 0;
 	public static int login         = 1;
	public static int faillogin     = 2;
 	public static int disconnect    = 5;
 	public static int password      = 6;
 	public static int player        = 7;
 	public static int locations     = 8;
 	public static int planets       = 9;
 	public static int touchPlayer   = 10;
 	public static int users         = 11;
 	public static int addUser       = 12;
 	public static int touchUser     = 13;
 	public static int removeUser    = 14;
 	
 	public int idCommand  = none;
 	public int length     = 0;
 	public byte[] data 	  = new byte[0];
 
 	public Command(int com)
 	{
 		this.idCommand = com;
 	}
 	
 	public Command(int com, int len, byte[] data)
 	{
 		this.idCommand = com;
 		this.length = len;
 		this.data = data;
 	}
 	
 	public Command(Command command){
 		this.idCommand = command.idCommand;
 		this.length = command.length;
 		this.data = command.data;
 	}
 }
