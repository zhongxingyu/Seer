 package com.grademaster;
import java.util.HashMap;
 
 import com.grademaster.logging.*;
 import com.grademaster.data.*;
 //Author: Jake Billings
 //Date created: 12-13-2012
 //Desc: Tests functions of the program
 
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
 		log.log("Path: " + loader.getPath());
		XMLInterface xml = new XMLInterface(loader);
		HashMap<Object,Object> map = null;
 		try {
			map = xml.getData();
 		} catch (Exception e) {
 			log.log(e);
 		}
		log.log(map.toString());
		log.log(map.keySet().toString());
 		log.log("Done.",ErrorLevel.INFO);
 	}
 }
