 package org.juoksu.data;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Logger {
 	
 	private File logfile;
 	
 	public Logger(String logfile) {
 		this.logfile = new File(logfile);
 	}
 	
 	public Logger(File logfile) {
 		this.logfile = logfile;
 	}
 	
 	public void log(String s) {
 		try {
			FileWriter fstream = new FileWriter(logfile);
 			BufferedWriter out = new BufferedWriter(fstream);
 			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH-mm-ss");
 			Date date = new Date();
 			out.write(dateFormat.format(date) + ": " + s + "\n");
 			out.close();
 		}
 		catch(IOException e) {
 			System.out.println(e.getStackTrace());
 		}
 	}
 
 }
