 package com.github.dreadslicer.tekkitrestrict;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
import java.util.Collection;
 import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
 
 public class FileLog {
 	private BufferedWriter out;
 	private String type = "";
 	private String date = "";
 	private int day = 0;
 	private int counter = 0;
	private static ConcurrentHashMap<String, FileLog> Logs = new ConcurrentHashMap<String, FileLog>();
 	private boolean alternate;
 	
 	@SuppressWarnings("deprecation")
 	public FileLog(String type, boolean alternate){
 		this.alternate = alternate;
 		String sep = File.separator;
 		if (type == null) type = "null";
 		this.type = type;
 		Date curdate = new Date(System.currentTimeMillis());
 		this.day = curdate.getDay();
 		DateFormat formatter = new SimpleDateFormat("dd-MM-yy");
 		this.date = formatter.format(curdate);
 		
 		File log;
 		File folder;
 		if (!alternate){
 			log = new File("."+sep+TRConfigCache.LogFilter.logLocation+sep+type+sep+type+"-"+date+".log");
 			folder = new File("."+sep+TRConfigCache.LogFilter.logLocation+sep+type+sep);
 		} else {
 			log = new File("plugins"+sep+"tekkitrestrict"+sep+"log"+sep+type+sep+type+"-"+date+".log");
 			folder = new File("plugins"+sep+"tekkitrestrict"+sep+"log"+sep+type+sep);
 		}
 
 		if (!folder.exists()) folder.mkdirs();
 		
 		if (!log.exists()){
 			try {
 				log.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			//out = new BufferedWriter(new FileWriter(log,true));
 			out = new BufferedWriter(new FileWriter(log, true));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		Logs.put(type, this);
 	}
 	
 	@SuppressWarnings("deprecation")
 	public FileLog(String type){
 		this.alternate = false;
 		String sep = File.separator;
 		if (type == null) type = "null";
 		this.type = type;
 		Date curdate = new Date(System.currentTimeMillis());
 		this.day = curdate.getDay();
 		DateFormat formatter = new SimpleDateFormat("dd-MM-yy");
 		this.date = formatter.format(curdate);
 		
 		File log = new File("."+sep+TRConfigCache.LogFilter.logLocation+sep+type+sep+type+"-"+date+".log");
 		File folder = new File("."+sep+TRConfigCache.LogFilter.logLocation+sep+type+sep);
 		if (!folder.exists()) folder.mkdirs();
 		
 		if (!log.exists()){
 			try {
 				log.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			//out = new BufferedWriter(new FileWriter(log,true));
 			out = new BufferedWriter(new FileWriter(log, true));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		Logs.put(type, this);
 	}
 	
 	public static FileLog getLog(String type){
 		return Logs.get(type);
 	}
 	
 	public static FileLog getLogOrMake(String type){
 		if (type == null) type = "null";
 		FileLog tbr = Logs.get(type);
 		if (tbr == null) return new FileLog(type);
 		return tbr;
 	}
 	
 	public static FileLog getLogOrMake(String type, boolean alternate){
 		FileLog tbr = Logs.get(type);
 		if (type == null) type = "null";
 		if (tbr == null) return new FileLog(type, alternate);
 		return tbr;
 	}
 	
 	public void log(String msg){
 		DateFormat formatter = new SimpleDateFormat("kk:mm:ss");
 		String time = formatter.format(new Date(System.currentTimeMillis()));
 		StringBuilder msgToWrite = new StringBuilder(time).append(" ").append(msg);
 		try {
 			if (type.equals("Chat"))
 				out.write(replacecolors(msgToWrite.toString()));
 			else
 				out.write(replaceshort(msgToWrite.toString()));
 			out.newLine();
 		} catch (IOException ex) {
 		}
 		
 		counter++;
 
 		if (counter>=10){
 			counter = 0;
 			try {
 				out.flush();
 			} catch (IOException ex) {
 				ex.printStackTrace();
 			}
 			changeDate();
 		}
 	}
 	
 	public boolean close(){
 		try {
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 			Logs.remove(type);
 			return false;
 		}
 		Logs.remove(type);
 		return true;
 	}
 	
 	public static void closeAll(){
		Collection<FileLog> allLogs = Logs.values();
		for (FileLog filelog : allLogs){
			if (!filelog.close()){
 				tekkitrestrict.log.warning("Unable to close all logs. Some might not save properly.");
 			}
 		}
 		Logs = null;
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void changeDate(){
 		Date curdate = new Date(System.currentTimeMillis());
 		int day = curdate.getDay();
 		if (day == this.day) return;
 		
 		if (!close()){
 			tekkitrestrict.log.warning("Unable to close the old log!");
 			return;
 		}
 		String sep = File.separator;
 		this.day = day;
 		DateFormat formatter = new SimpleDateFormat("dd-MM-yy");
 		this.date = formatter.format(curdate);
 		
 		File log;
 		File folder;
 		if (!alternate){
 			log = new File("."+sep+TRConfigCache.LogFilter.logLocation+sep+type+sep+type+"-"+date+".log");
 			folder = new File("."+sep+TRConfigCache.LogFilter.logLocation+sep+type+sep);
 		} else {
 			log = new File("plugins"+sep+"tekkitrestrict"+sep+"log"+sep+type+sep+type+"-"+date+".log");
 			folder = new File("plugins"+sep+"tekkitrestrict"+sep+"log"+sep+type+sep);
 		}
 		
 		if (!folder.exists()){
 			folder.mkdir();
 		}
 		if (!log.exists()){
 			try {
 				log.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			out = new BufferedWriter(new FileWriter(log, true));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		Logs.put(type, this);
 	}
 	
 	private String replacecolors(String input){
 		input = input.replace("\033[30;22m", "0");
 		input = input.replace("\033[34;22m", "1");
 		input = input.replace("\033[32;22m", "2");
 		input = input.replace("\033[36;22m", "3");
 		input = input.replace("\033[31;22m", "4");
 		input = input.replace("\033[35;22m", "5");
 		input = input.replace("\033[33;22m", "6");
 		input = input.replace("\033[37;22m", "7");
 		input = input.replace("\033[30;1m", "8");
 		input = input.replace("\033[34;1m", "9");
 		input = input.replace("\033[32;1m", "a");
 		input = input.replace("\033[36;1m", "b");
 		input = input.replace("\033[31;1m", "c");
 		input = input.replace("\033[35;1m", "d");
 		input = input.replace("\033[33;1m", "e");
 		input = input.replace("\033[37;1m", "f");
 
 		/*input = input.replace("\033[0;30;22m", "0");
 		input = input.replace("\033[0;34;22m", "1");
 		input = input.replace("\033[0;32;22m", "2");
 		input = input.replace("\033[0;36;22m", "3");
 		input = input.replace("\033[0;31;22m", "4");
 		input = input.replace("\033[0;35;22m", "5");
 		input = input.replace("\033[0;33;22m", "6");
 		input = input.replace("\033[0;37;22m", "7");
 		input = input.replace("\033[0;30;1m", "8");
 		input = input.replace("\033[0;34;1m", "9");
 		input = input.replace("\033[0;32;1m", "a");
 		input = input.replace("\033[0;36;1m", "b");
 		input = input.replace("\033[0;31;1m", "c");
 		input = input.replace("\033[0;35;1m", "d");
 		input = input.replace("\033[0;33;1m", "e");
 		input = input.replace("\033[0;37;1m", "f");
 
 		input = input.replace("\033[30m", "0");
 		input = input.replace("\033[32m", "2");
 		input = input.replace("\033[36m", "3");
 		input = input.replace("\033[31m", "4");
 		input = input.replace("\033[35m", "5");
 		input = input.replace("\033[33m", "6");
 		input = input.replace("\033[37m", "7");
 		input = input.replace("\033[30m", "8");
 		input = input.replace("\033[34m", "9");
 		input = input.replace("\033[32m", "a");
 		input = input.replace("\033[36m", "b");
 		input = input.replace("\033[31m", "c");
 		input = input.replace("\033[35m", "d");
 		input = input.replace("\033[33m", "e");
 		input = input.replace("\033[37m", "f");*/
 
 		input = input.replace("\033[5m", "k");
 		input = input.replace("\033[21m", "l");
 		input = input.replace("\033[9m", "m");
 		input = input.replace("\033[4m", "n");
 		input = input.replace("\033[3m", "o");
 		input = input.replace("\033[0;39m", "r");
 
 		input = input.replace("\033[0m", "r");
 
 		input = input.replace("\033[m", "");
 		return input;
 	}
 
 	private String replaceshort(String input){
 		return input.replace("\033[m", "");
 	}
 }
