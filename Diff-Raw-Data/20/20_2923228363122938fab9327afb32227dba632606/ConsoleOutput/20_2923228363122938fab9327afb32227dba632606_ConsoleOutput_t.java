 package com.phunware.alerts.sample;
 
 import java.io.Serializable;
 
 import android.os.Bundle;
 import android.util.Log;
 
 /**
  * Simple class to manage the state of the console output
  *
  */
 public class ConsoleOutput implements Serializable{
 
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static final String TAG = "ConsoleOutput";
 	/**
 	 * actual text
 	 */
 	private String mText = "";
 	
 	/**
 	 * Get the console text
 	 * @return
 	 */
 	public String getConsole()
 	{
 		return mText;
 	}
 
 	/**
 	 * Erase everything in the console
 	 */
 	public void clearConsole()
 	{
 		mText = "";
 	}
 	
 	/**
 	 * Set the console to exactly what the parameter is	 
 	 * @param newConsole
 	 */
 	public void setConsole(String newConsole)
 	{
 		mText = newConsole;
 	}
 	
 	/**
 	 * Append text to the console and get the entire console back
 	 * @param text
 	 * @return
 	 */
 	public String appendToConsole(String text)
 	{
 		String timestamp = com.phunware.core.internal.Utils.getCurrentTimeFormatted();
 		
 		mText += "\n"
 				+ "\n+++++++++++++++++++++++++++++\n"
 				+ timestamp
 				+ "\n"
 				+ text
 				+ "\n-----------------------------";
 		return mText;
 	}
 	
 	/**
 	 * Save the state to the bundle
 	 * @param outState
 	 */
 	public void saveInstanceState(Bundle outState)
 	{
 		outState.putSerializable(ConsoleFragment.ARG_CONSOLE_TEXT, mText);		
 	}
 	
 	/**
 	 * Restore the state from the bundle
 	 * @param savedInstanceState
 	 */
 	public ConsoleOutput(Bundle savedInstanceState)
 	{
 		if(savedInstanceState != null)
 		{
 			//load saved console output
 			String tStr = savedInstanceState.getString(ConsoleFragment.ARG_CONSOLE_TEXT);
 			if(tStr != null)
 				mText = tStr;
 			else
 			{
 				mText = "";
 				Log.i(TAG, "No console output to restore");
 			}
 		}
 		else
 		{
 			mText = "";
 			Log.i(TAG, "No console output to restore");
 		}
 	}
 	
 	/**
 	 * Check if the console is empty or not
 	 * @return True if empty, false if not
 	 */
 	public boolean isEmpty()
 	{
 		if(mText == null)
 			return true;
 		return mText.trim().length() == 0;
 	}
 	
 	/**
 	 * Interface for communication between an activity managing this console and fragments
 	 */
 	public interface ConsoleLogger{
 		/**
 		 * This should be called at anypoint to update the console.
 		 * @param text
 		 */
 		public void printToConsole(String text);
 		/**
 		 * Called when the console should be cleared
 		 */
 		public void clearConsole();
		/**
		 * 
		 */
		public void onFragmentStarted();
 	}
 }
