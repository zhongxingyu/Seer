 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package util.misc;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 
 /** I/O related utility methods */
 public abstract class IO
 {
 	/** Size of buffer used in copying */
 	private final static int COPYBUFFER_LENGTH=32768;
 
 	/**
 	 * Copies from an input stream (until EOF) to output stream.
 	 * @param is Input stream (will be closed at end)
 	 * @param os Output stream
 	 * @param bCloseOutput If true, will close output stream at end
 	 * @throws IOException If there's any I/O error in process
 	 */
 	public static void copy(InputStream is,OutputStream os,boolean bCloseOutput)
 		throws IOException
 	{
 		byte[] abBuffer=new byte[COPYBUFFER_LENGTH];
 		while(true)
 		{
 			int iRead=is.read(abBuffer);
 			if(iRead<=0) break;
 			os.write(abBuffer,0,iRead);
 		}
 		is.close(); // Was at EOF anyway
 		if(bCloseOutput) os.close();
 	}
 
 	/**
 	 * Reads all data to EOF from an input stream, discarding it.
 	 * @param is Input stream (will be closed at end)
 	 * @throws IOException If there's any I/O error in process
 	 */
 	public static void eat(InputStream is) throws IOException
 	{
 		byte[] abBuffer=new byte[COPYBUFFER_LENGTH];
 		while(true)
 		{
 			int iRead=is.read(abBuffer);
 			if(iRead<=0) break;
 		}
 		is.close(); // Was at EOF anyway
 	}
 
 	/**
 	 * Loads from an input stream (assumed to be UTF-8) into a string.
 	 * @param is Input stream to read from (will be closed at end)
 	 * @return String version of read data
 	 * @throws IOException If there's any I/O error in process
 	 */
 	public static String loadString(InputStream is) throws IOException
 	{
 		return new String(loadBytes(is),"UTF-8");
 	}
 
 	/**
 	 * Loads from an input stream into a byte array.
 	 * @param is Input stream to read from (will be closed at end)
 	 * @return Byte array containing read data
 	 * @throws IOException If there's any I/O error in process
 	 */
 	public static byte[] loadBytes(InputStream is) throws IOException
 	{
 		ByteArrayOutputStream baos=new ByteArrayOutputStream();
 		copy(is,baos,true);
 		return baos.toByteArray();
 	}
 
 
 	/**
 	 * Safely lists files in a given folder, without crashing your program with
 	 * NullPointerExceptions.
 	 * @param fFolder Folder in which to look
 	 * @return Array of files (zero-length if there aren't any, and never null)
 	 */
 	public static File[] listFiles(File fFolder)
 	{
 		File[] af=fFolder.listFiles();
 		if(af==null) af=new File[0];
 		return af;
 	}
 
 	/**
 	 * Loads a resource relative to a specific class into a byte array.
 	 * @param c Class (resource must come from same classloader)
 	 * @param sName Name of resource (relative path to class)
 	 * @return Byte array containing file in memory
 	 * @throws IOException If it doesn't exist or there is any other error loading
 	 */
	public static byte[] loadResource(Class<?> c,String sName) throws IOException
 	{
 		return loadResource(c.getClassLoader(),
 			"/"+c.getName().replace('.','/').replaceFirst("/[^/]*$","")+"/"+sName);
 	}
 
 	/**
 	 * Loads a resource from the classloader (e.g. the .jar file) into a byte array.
 	 * @param cl Classloader resource comes from
 	 * @param sPath Path of resource (must begin with /)
 	 * @return Byte array containing file in memory
 	 * @throws IOException If it doesn't exist or there is any other error loading
 	 */
 	public static byte[] loadResource(ClassLoader cl,String sPath) throws IOException
 	{
 		if(!sPath.startsWith("/")) throw new FileNotFoundException(
 			"Resource must be absolute path: "+sPath);
 		sPath=sPath.substring(1);
 		URL u=cl.getResource(sPath);
 		if(u==null) throw new FileNotFoundException("Resource not found: "+sPath);
 		URLConnection uc=u.openConnection();
 		uc.connect();
 		int iLength=uc.getContentLength();
 		if(iLength>-1) // Sometimes getContentLength returns -1, sometimes it doesn't
 		{
 			byte[] abContent=new byte[iLength];
 			InputStream is=uc.getInputStream();
 			int iStart=0;
 			while(true)
 			{
 				int iRead=is.read(abContent,iStart,abContent.length-iStart);
 				if(iRead==0)
 					throw new IOException("Resource length mismatch");
 				iStart+=iRead;
 				if(iStart==abContent.length) break;
 			}
 			is.close();
 			return abContent;
 		}
 		else
 		{
 			ByteArrayOutputStream baos=new ByteArrayOutputStream();
 			copy(uc.getInputStream(),baos,false);
 			return baos.toByteArray();
 		}
 	}
 
 }
