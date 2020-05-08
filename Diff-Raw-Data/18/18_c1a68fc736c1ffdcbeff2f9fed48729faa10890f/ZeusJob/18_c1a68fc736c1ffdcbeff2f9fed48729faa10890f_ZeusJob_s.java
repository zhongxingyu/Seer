 package com.uc.test;
 
 import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
 
 public class ZeusJob {
	private static Logger log = Logger.getLogger(ZeusJob.class);
 	public static void main(String[] args){
 		
 		try {
			log.info("i sleep");
 			Thread.sleep(1000);
			log.info("i sleep again");
 			Thread.sleep(1000);
 			Configuration conf=new Configuration(false);
 			String temp = conf.get("a");
 			log.info("a is:"+temp);
 			int time = conf.getInt("sleep",0);
 			log.info("time is :"+time);
 			Thread.sleep(time*1000);
 			
 		} catch (InterruptedException e) {
			log.error("sleep is intxxxx",e);
 		}
		log.info("i die");
 		
 	}
 }
