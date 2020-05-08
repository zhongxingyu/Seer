 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
  
 package org.infoglue.cms.io;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.RandomAccessFile;
 import java.io.Writer;
 
 
 public class FileHelper
 {
 	
 	/**
 	 * Writes the file to the hard disk. If the file doesn't exist a new file is created.
 	 * @author Mattias Bogeblad
 	 * @param text The text you want to write to the file.
 	 * @param file The file to save to
 	 * @param is_append Dictates if the text should be appended to the existing file. 
 	 * If is_append == true; The text will be added to the existing file.
 	 * If is_append == false; The text will overwrite the existing contents of the file.
 	 *
 	 * @exception java.lang.Exception
 	 * @since 2002-12-12
 	 */
  
 	public synchronized static void writeToFile(File file, String text, boolean isAppend) throws Exception
 	{
		PrintWriter pout = new PrintWriter(new FileWriter(file, isAppend));
 		pout.println(text);    
 		pout.close();
 	}   
 	
 	/**
 	 * Writes the file to the hard disk. If the file doesn't exist a new file is created.
 	 * @author Mattias Bogeblad
 	 * @param text The text you want to write to the file.
 	 * @param file The file to save to
 	 * @param is_append Dictates if the text should be appended to the existing file. 
 	 * If is_append == true; The text will be added to the existing file.
 	 * If is_append == false; The text will overwrite the existing contents of the file.
 	 *
 	 * @exception java.lang.Exception
 	 * @since 2002-12-12
 	 */
  
  	//TODO - this is not right.
 	public synchronized static void writeUTF8ToFileSpecial(File file, String text, boolean isAppend) throws Exception
 	{
 		/*
 		FileOutputStream fos = new FileOutputStream(file, isAppend);
 		Writer out = new OutputStreamWriter(fos, "UTF-8");
 		out.write(text);
 		out.flush();
 		out.close();
 		*/
 		
 		DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, isAppend));
 		dos.writeBytes(text);
 		dos.flush();
 		dos.close();
 		
 	}   
 	
 	//TODO - this is not right.
 	public synchronized static void writeUTF8(File file, String text, boolean isAppend) throws Exception
 	{
 		FileOutputStream fos = new FileOutputStream(file, isAppend);
 		Writer out = new OutputStreamWriter(fos, "UTF-8");
 		out.write(text);
 		out.flush();
 		out.close();
 	}   
 	
 	
 	public synchronized static void writeUTF8ToFile(File file, String text, boolean isAppend) throws Exception
 	{
         Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
         out.write(text);
         out.flush();
         out.close();
 	}
 	
 	/**
 	 * Writes the file to the hard disk. If the file doesn't exist a new file is created.
 	 * @author Mattias Bogeblad
 	 * @param text The text you want to write to the file.
 	 * @param file The file to save to
 	 * @param is_append Dictates if the text should be appended to the existing file. 
 	 * If is_append == true; The text will be added to the existing file.
 	 * If is_append == false; The text will overwrite the existing contents of the file.
 	 *
 	 * @exception java.lang.Exception
 	 * @since 2002-12-12
 	 */
  
 	public synchronized static String readUTF8FromFile(File file) throws Exception
 	{
 	    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
 	    String str = in.readLine();
 	    
 		StringBuffer sb = new StringBuffer();
 		
 		int ch;
 		while ((ch = in.read()) > -1) {
 			sb.append((char)ch);
 		}
 		in.close();
 		
 		return sb.toString();
 	}   
 	
 	/**
 	 * This method reads a file from the disk and converts it to an byte[].
 	 * @author Mattias Bogeblad
 	 * @param file The file read bytes from
 	 *
 	 * @exception java.lang.Exception
 	 * @since 2002-12-12
 	 */
 	
 	public static byte[] getFileBytes(File file) throws Exception
 	{
 		FileInputStream fis = new FileInputStream(file);
 		byte[] fileBytes = new byte[(int)file.length()];
 		fis.read(fileBytes);
 		fis.close();
  
 		return fileBytes;
 	}
 	
 	
 	/**
 	 * This method reads a file from the disk into a string.
 	 * @author Mattias Bogeblad
 	 * @param file The file reads from
 	 *
 	 * @exception java.lang.Exception
 	 * @since 2002-12-12
 	 */
 	
 	public static String getFileAsString(File file) throws Exception
 	{
 		StringBuffer sb = new StringBuffer();
 		
 		FileInputStream fis = new FileInputStream(file);
 		int c;
 		while((c = fis.read()) != -1)
 		{
 			sb.append((char)c);
 		}
 	    
 		fis.close();
     	
 		return sb.toString();
 	}
 
 
 	/**
 	 * This method reads a file from the disk into a string.
 	 * @author Mattias Bogeblad
 	 * @param file The file reads from
 	 *
 	 * @exception java.lang.Exception
 	 * @since 2002-12-12
 	 */
 	
 	public static String getStreamAsString(InputStream inputStream) throws Exception
 	{
 		StringBuffer sb = new StringBuffer();
 		
 		if(inputStream != null)
 		{
 			int c;
 			while((c = inputStream.read()) != -1)
 			{
 				sb.append((char)c);
 			}
 		    
 			inputStream.close();
 		}
 		    	
 		return sb.toString();
 	}
 
 	
 	/**
 	 * This method writes a file with data from a byte[].
 	 * @author Mattias Bogeblad
 	 * @param file The file to save to
 	 *
 	 * @exception java.lang.Exception
 	 * @since 2002-12-12
 	 */
 	
 	public static void writeToFile(File file, byte[] data) throws Exception
 	{
 		FileOutputStream fos = new FileOutputStream(file);
 		BufferedOutputStream bos = new BufferedOutputStream(fos);
 		for(int i=0; i < data.length; i++)
 		{ 
 			bos.write(data[i]);
 		}
     	
 		bos.flush();
 		bos.close();
 		fos.close();
 	}
 	
 	/**
 	 * Reading the x last lines of a file
 	 */
 	public static String tail(File file, int numberOfLines) throws Exception
 	{
 		StringBuffer result = new StringBuffer("");
 		
 		if(file.length() == 0)
 			return "The log file was empty";
 		
         RandomAccessFile raf = new RandomAccessFile(file, "r");
     
         // Read a character
         char ch = raf.readChar();
     
         // Seek to end of file
         if(file.length() > numberOfLines * 150)
         	raf.seek(file.length() - (numberOfLines * 150));
     
         raf.readLine();
         
         // Append to the end
         String lineData = "";
         while((lineData = raf.readLine()) != null)
         {
         	result.append(lineData).append('\n');
         }
         
         raf.close();
         
         return result.toString();
 	}
 
 }
