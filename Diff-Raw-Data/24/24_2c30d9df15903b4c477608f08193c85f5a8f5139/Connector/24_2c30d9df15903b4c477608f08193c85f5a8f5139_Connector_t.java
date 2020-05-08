 package edu.rit.smartFridge.util;
 
 public class Connector
 {
 	/**
 	 * The instance of the {@code DataConnect} we want to use
 	 */
 	private static DataConnect instance = null;
 
 	/**
 	 * Connnector constructor
 	 */
 	public Connector()
 	{
 	}
 
 	/**
 	 * Used to get the instance of the {@code DataConnect} we're using
 	 * 
 	 * @return The Instance of the {@code DataConnect} we're using
 	 */
 	public static DataConnect getInstance()
 	{
 		if (instance == null)
 		{
			instance = new RestConnect();
 		}
 		return instance;
 	}
 }
