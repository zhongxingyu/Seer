 package com.vuzzz.android;
 
 public abstract class VuzZzConfig {
 	
	public static final boolean RELEASE = false;
 	
 	public static final boolean LOG_TO_CONSOLE = true && !RELEASE;
 	
 	public static final boolean MUI = false;
 	
 	private VuzZzConfig() {
 	}
 	
 }
