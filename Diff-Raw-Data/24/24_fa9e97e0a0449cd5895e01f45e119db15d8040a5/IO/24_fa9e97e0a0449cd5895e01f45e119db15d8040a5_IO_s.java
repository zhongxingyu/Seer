 package com.earlofmarch.reach.model;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 /**
  *	The static methods in this class allow text  
  *	to be written to OR read from a file.
  *
  *	@author  Ms Cianci
  *	@since	 November 2006
  */
 public class IO
 {
 	
 	/* VARIABLE AND METHODS NEEDED FOR WRITING TO A FILE */
 	
 	private static PrintWriter fileOut;
 	
 	
 	/**
 	 * Creates a new file (fileName) in the current
 	 * folder and places a reference to it in fileOut
 	 * @param fileName Represents the name of the file
 	 */	
 	public static void createOutputFile(String fileName)
 	{
 		try
 		{
			if(fileName.contains("g"))
				System.out.println(fileName);
 			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
 		}
 		catch(IOException e)
 		{
 			System.out.println("*** Cannot create file: " + fileName + " ***");
 		}
 	}
 	
 	/**
 	 * Creates a new file (fileName) in the current
 	 * folder and places a reference to it in fileOut
 	 * @param fileName Represents the name of the file
 	 * @param append True if you want to add to the existing information
 	 * 					false if you want to re-rewrite the entire file
 	 */	
 	public static void createOutputFile(String fileName,boolean append)
 	{
 		try
 		{
 			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(fileName,append)));
 		}
 		catch(IOException e)
 		{e.printStackTrace();
 			System.out.println("*** Cannot create file: " + fileName + " ***");
 			
 		}
 	}
 	
 	/**
 	 * Text is added to the current file
 	 * @param text The characters that will be added to the file
 	 */
 	public static void print(String text)
 	{
 		fileOut.print(text);
 	}
 
 
 	/**
 	 * Text is added to the current file and a new line
 	 * is inserted at the end of the characters
 	 * @param text The characters that will be added to the file
 	 */
 	public static void println(String text)
 	{
 		fileOut.println(text);
 	}
 
 	
 	/**
 	 * Close the file that is currently being written to
 	 * NOTE: This method MUST be called when you are finished
 	 *		 writing to a file in order to have your changes saved
 	 */
 	public static void closeOutputFile()
 	{
 		fileOut.close();
 	}
 	
 	
 	
 	
 	
 	/* VARIABLE AND METHODS NEEDED FOR READING FROM A FILE */
 	
 	private static BufferedReader fileIn;
 	
 	
 	/**
 	 * Opens a file called fileName (that must be
 	 * stored in the current folder) and places a
 	 * reference to it in fileIn
 	 * @param fileName The name of a file that already exists
 	 */
 	public static boolean openInputFile(String fileName)
 	{
 		try
 		{
 			fileIn = new BufferedReader(new FileReader(fileName));
 			return true;
 		}
 		catch(FileNotFoundException e)
 		{
 			System.out.println("***Cannot open " + fileName + "***");
 			return false;
 		}
 	}
 	
 	
 	/**
 	 * Read the next line from the file and return it
 	 */
 	public static String readLine() throws IOException
 	{
 		try
 		{
 			return fileIn.readLine();
 		}
 		catch(IOException e){}
 		
 		return null;
 	}
 	
 	
 	/**
 	 * Close the file that is currently being read from
 	 */
 	public static void closeInputFile() throws IOException
 	{
 		fileIn.close();
 	}
 	
 } // end class
