 package com.uc.test;
 
 import org.apache.hadoop.conf.Configuration;
 
 public class ZeusJob {
 	public static void main(String[] args){
 		
 		try {
 			Thread.sleep(1000);
			System.out.println("sleep");
//			System.out.println("sleep");
 			Thread.sleep(1000);
 			Configuration conf=new Configuration(false);
 			String temp = conf.get("a");
			System.out.println("a is:"+temp);
 			int time = conf.getInt("sleep",0);
			System.out.println("time is :"+time);
 			Thread.sleep(time*1000);
 			
 		} catch (InterruptedException e) {
			System.out.println("catch exception");
 		}
 		
 	}
 }
