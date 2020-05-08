 package com.leo.helloworld;
 
 import java.lang.System;
 
 public class Helloworld {
 
 	public Helloworld() {
 		// TODO Auto-generated constructor stub
 	}
 
 	public native String getPrint();
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		// TODO Auto-generated method stub
 		System.out.printf("this is java's printf function");
 		System.out.printf("this is jni's printf function:%s", new Helloworld().getPrint());
 		
 	}
 	
 
 
 }
