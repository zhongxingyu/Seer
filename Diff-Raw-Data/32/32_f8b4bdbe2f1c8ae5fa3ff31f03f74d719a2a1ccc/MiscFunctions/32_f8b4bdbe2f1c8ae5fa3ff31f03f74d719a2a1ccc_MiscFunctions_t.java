 package org.fuzzyslippers.ToDoCraft.commands;
 
 import java.io.BufferedWriter;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Logger;
 //import lib.PatPeter.SQLibrary.*;
 import lib.PatPeter.SQLibrary.SQLite;
 
 import org.bukkit.command.CommandSender;
 
 public class MiscFunctions {
 	static Logger log = Logger.getLogger("Minecraft");
 	public static void report(String string){
 		String newstring = "[ToDoCraft] " + string;
 		log.info(newstring);
 	}
 	// convert args to string
 	public static String argsToString(String[] args, int startingarg, String separator){
 		String arraystring = "";
 		for(int i = startingarg; i<args.length; i++){
 			arraystring = arraystring + separator + args[i];
 		}
 		return arraystring;
 	}
 	//write tasks
 	Charset chars = Charset.forName("US-ASCII");
 	public static void writeTasks(String task, File filename){
 		
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(filename,true));
 			out.write(task);
 			out.close();
 		} catch (IOException x) {
 			report("IOError!");
 		}
 	}
 	//read tasks
 	public static void readTasks(int linenumber, File file, CommandSender sender){
 		try{
 			BufferedReader in = new BufferedReader(new FileReader(file));
 			String filecontents = in.toString();
 			sender.sendMessage(filecontents);
 			in.close();
 		} catch (IOException e){
 			report("IOError!");
 		}
 	}
 	//Database start
 	public static File dbFolder = new File("plugins/ToDoCraft");
 	public static SQLite dbManage = new SQLite(log, "[ToDoCraft]", "tasks", dbFolder.getPath());
 	public static void enableDB(){
 		dbManage.initialize();
		dbManage.open();
 	}
 	//Database close
 	public static void disableDB(){
 		dbManage.close();
 	}
 	// Create Tables
 	
 	public static void createTable(String username){
		dbManage.createTable("CREATE TABLE "+username +"('id' INTEGER PRIMARY KEY, 'IMPORTANCE' text NOT NULL, 'TASK' text NOT NULL, 'DONE' INT NOT NULL DEFAULT 0)");
 	}
 	// Database Write tasks
	public static void writeDBTasks(String priority, String username, String task){
		try{
			ResultSet count = dbManage.query("SELECT COUNT(*) as count FROM " + username );
			int countint = count.getInt("count");
			dbManage.query("INSERT INTO "+username + "(id, IMPORTANCE, TASK, DONE) VALUES("+countint+", '"+priority+"', '"+task+"', 0)");
		} catch (Exception e){
			report("Something went wrong writing the DataBase!");
			report(e.getMessage());
		}
 	}
 }
