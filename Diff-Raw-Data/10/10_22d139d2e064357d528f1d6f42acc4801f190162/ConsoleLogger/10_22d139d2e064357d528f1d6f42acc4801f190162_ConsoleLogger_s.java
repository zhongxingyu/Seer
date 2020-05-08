 package com.iolma.studio.log;
 
 public class ConsoleLogger implements ILogger {
 
 	public void info(String message) {
 		System.out.println(message);
 	}
 
 	public void debug(String message) {
 		System.out.println(message);
 	}
 
 }
