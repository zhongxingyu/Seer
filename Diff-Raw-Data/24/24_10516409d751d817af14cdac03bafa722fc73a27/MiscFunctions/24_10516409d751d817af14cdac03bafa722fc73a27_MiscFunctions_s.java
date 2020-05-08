 package org.fuzzyslippers.ToDoCraft.commands;
 
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

 import java.nio.charset.Charset;
 import java.sql.ResultSet;
 import java.util.logging.Logger;
 import lib.PatPeter.SQLibrary.SQLite;

 import org.bukkit.ChatColor;
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
 			arraystring = arraystring + args[i] + separator;
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
 		dbManage.createTable("CREATE TABLE "+username +" ('id' INTEGER PRIMARY KEY, 'IMPORTANCE' text, 'TASK' text, 'DONE' INT DEFAULT 0)");
 	}
 	// Database Write tasks
 	public static void writeDBTasks(String priority, String username, String task){
 		try{
 			int zero = 0;
 			if(zero==0){
 				dbManage.query("INSERT INTO " +username + " (IMPORTANCE, TASK, DONE) VALUES ('"+priority+"', '"+task+"', 0)");
 			}
 			else {
 				report("Couldn't count tasks!");
 			}
 			
 		} catch (Exception e){
 			report("Something went wrong writing the DataBase!");
 			report(e.getMessage());
 			report(e.getStackTrace().toString());
 		}
 	}
 	public static void readDBTasks(CommandSender sender){
 		try{
 			String username = sender.getName();
 			ResultSet result = dbManage.query("Select id, IMPORTANCE, TASK, DONE from " + username);
 			ResultSet totaltasks = dbManage.query("Select count(id) as counted from " +username);
 			int totaltaskint = totaltasks.getInt("counted");
 			for(int i = 0; i != totaltaskint; i++){
 				result.next();
 				int tasknumber = result.getInt("id");
 				String taskimportance = result.getString("IMPORTANCE");
 				String tasktask = result.getString("TASK");
 				int taskdone = result.getInt("DONE");
 				String taskdonestring;
 				if(taskdone==0){
 					taskdonestring = "not done";
 				} else {
 					taskdonestring = "done";
 				}
 				sender.sendMessage(ChatColor.GREEN + ""+ tasknumber + ": " + ChatColor.BLUE+""+ taskimportance + " - " +ChatColor.LIGHT_PURPLE +""+ tasktask + ChatColor.WHITE+" is " + ChatColor.GREEN+""+ taskdonestring + ".");
 			}
 			
 		} catch (Exception e){
 			report(e.toString());
 		}
 	}
 	public static void finishTask(CommandSender sender, String task){
 		try{
 			String sendername = sender.getName();
                                                         report(sendername);
                                                         report(task);
 			dbManage.query("UPDATE "+sendername + " SET DONE=1 WHERE TASK='" +task+"'");
 			sender.sendMessage(ChatColor.GREEN + "Successfully  completed " + ChatColor.LIGHT_PURPLE + "" + task);
 		} catch (Exception e){
 			report(e.toString());
 			Throwable cause = e.getCause();
 			report(cause.toString());
 			
 		}
 	}
 }
