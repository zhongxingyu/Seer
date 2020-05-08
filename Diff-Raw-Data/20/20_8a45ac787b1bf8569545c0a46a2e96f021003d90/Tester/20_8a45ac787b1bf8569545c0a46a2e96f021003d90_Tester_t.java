 package com.grademaster;
 
 import com.grademaster.logging.*;
 import com.grademaster.data.*;
import com.grademaster.data.objects.Config;
 //Author: Jake Billings
 //Date created: 12-13-2012
 //Desc: Tests functions of the program
import com.grademaster.data.xml.XMLConfigAdapter;
 
 public class Tester {
 	public static void main(String[] args) {
 		Logger log=new Logger();
 		log.log("Testing log with test method...", ErrorLevel.DEBUG);
 		Logger.test_log();
 		log.log("This is not a real error.", ErrorLevel.ERROR);
 		log.log("This is not real either.", ErrorLevel.CRITICAL);
 		log.log("Done", ErrorLevel.DEBUG);
 		log.log("Testing Data...",ErrorLevel.INFO);
 		RawLocalLoader loader = new RawLocalLoader("Config.xml");
		XMLConfigAdapter a = new XMLConfigAdapter();
 		log.log("Path: " + loader.getPath());
		XMLInterface xml = new XMLInterface(loader,a);
		Config con = null;
 		try {
			con = (Config) xml.getData();
 		} catch (Exception e) {
 			log.log(e);
 		}
		log.log(con.toString());
		log.log(con.author);
 		log.log("Done.",ErrorLevel.INFO);
 	}
 }
