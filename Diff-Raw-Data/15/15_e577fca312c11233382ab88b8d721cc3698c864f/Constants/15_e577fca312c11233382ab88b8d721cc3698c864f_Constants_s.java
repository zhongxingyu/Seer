 package com.scs.security;
 
 import java.math.BigInteger;
 
 public class Constants {
 	/**
 	 * Set of features
 	 */
 	public static final String[] QUESTIONS = {
 			"How far are you from the campus right now?",
 			"How many websites will you visit?",
 			"How many emails will you exchange?",
 			"How long will your session last?",
 			"How many people will you message?",
 			"How many browser plugins do you have installed?" };
 	
 	/**
 	 * Threshold values of the features 
 	 */
 	public static final long[] THRESHOLD_FEATURE_VALUES = {2, 8, 3, 20, 6, 20};
 
 	/**
 	 * Number of features
 	 */
 	public static final int M = QUESTIONS.length;
 
 	/**
 	 * Number of previous successful logins stored
 	 */
 	public static final int H = 4;
 	
 	/**
	 * System parameter to determine width of non-distinguishing range
 	 */
 	public static final double K = 1;
 
 	/**
 	 * String written to history file to know if decryption was successful or
 	 * not
 	 */
 	public static final String TOTEM = "BECOOLHONEYBUNNY";
 	
 	public static final String HISTORY_FILE_PATH = "./res/history";
 	public static final String INSTRUCTION_TABLE_FILE_PATH = "./res/itable";
 	
 	// public static final BigInteger Q = Generator.LARGE_PRIME;
 	public static final BigInteger Q = new BigInteger(//"123457");
 			"33517498807111931132048208632861495510865379385111444454915448545807671489023");
 	
 	
 	public static final String PREF_INITIALIZED = "com.scs.security.initialized";
 	public static final String PREF_R = "com.scs.security.R";
 }
