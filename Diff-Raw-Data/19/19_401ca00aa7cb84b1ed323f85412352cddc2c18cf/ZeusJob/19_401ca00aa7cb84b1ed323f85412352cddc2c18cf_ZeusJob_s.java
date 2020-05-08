 package com.uc.test;
 
 import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.zeus.schedule.mvc.ScheduleInfoLog;
 
 public class ZeusJob {
	private static Logger log=LoggerFactory.getLogger(ScheduleInfoLog.class);
 	public static void main(String[] args){
 		
 		try {
 			Thread.sleep(1000);
			log.info("sleep");
 			Thread.sleep(1000);
 			Configuration conf=new Configuration(false);
 			String temp = conf.get("a");
			log.info("a is:"+temp);
 			int time = conf.getInt("sleep",0);
			log.info("time is :"+time);
 			Thread.sleep(time*1000);
 			
 		} catch (InterruptedException e) {
			log.info("catch exception",e);
 		}
 		
 	}
 }
