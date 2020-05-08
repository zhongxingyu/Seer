 package org.finomnis.mcdaemon.tools;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import com.twmacinta.util.MD5;
 
 public class FileTools {
 
 	public static FileWriter openFileWriteText(File f, boolean append) throws IOException
 	{
 		File dir = f.getParentFile();
 		if (dir != null) {
 			dir.mkdirs();
 		}
 		return new FileWriter(f, append);
 	}
 	
 	public static FileWriter openFileWriteText(String path, boolean append)
 			throws IOException {
 		File f = new File(path);
 		return openFileWriteText(f, append);		
 	}
 
 	public static OutputStream openFileWrite(File f, boolean append) throws FileNotFoundException
 	{
 		return new FileOutputStream(f, append);
 	}
 	
 	public static OutputStream openFileWrite(String path, boolean append) throws FileNotFoundException
 	{
 		File f = new File(path);
 		return openFileWrite(f, append);
 	}
 	
 	public static void writeFromStream(InputStream iStream, OutputStream fWriter) throws IOException
 	{
 		
 		byte[] buf = new byte[524288];
 		
 		while(true)
 		{
 		
 			int len = iStream.read(buf);
 			if(len < 0) break;
 			fWriter.write(buf, 0, len);
 					
 		}
 		
 		
 	}
 	
 	public static long fileSize(File f)
 	{
 		
 		return f.length();
 		
 	}
 	
 	public static long fileSize(String filename)
 	{
 		
 		return fileSize(new File(filename));
 		
 	}
 	
 	public static InputStream openFileRead(File file) throws FileNotFoundException
 	{
 		return new FileInputStream(file);
 	}
 	
 	public static InputStream openFileRead(String path) throws FileNotFoundException
 	{
 		File f = new File(path);
 		return openFileRead(f);
 	}
 	
 	public static String md5(byte[] data) throws CriticalException {
 
 		/*
 		MessageDigest md5Calculator;
 		try {
 			md5Calculator = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			throw new CriticalException("Unable to initialize MD5 calculator!");
 		}
 
 		BigInteger md5Value = new BigInteger(1, md5Calculator.digest(data));
 
 		return md5Value.toString(16);
 		*/
 		
 		MD5 md5 = new MD5();
 		md5.Init();
 		
 		md5.Update(data);
 		
 		return md5.asHex();
 		
 	}
 	
 	public static String md5(String str) throws CriticalException {
 
 		return md5(str.getBytes());
 
 	}
 	
 	public static String md5(File f) throws IOException, CriticalException{
 		
 		/*
 		InputStream iStream = openFileRead(f);
 		String md5String = md5(iStream);
 		iStream.close();
 		return md5String;
 		*/
 		return MD5.asHex(MD5.getHash(f));
 	}
 	
 	public static void copyFile(String inFile, String outFile) throws IOException
 	{
 	
 		InputStream iStream = openFileRead(inFile);
 		OutputStream oStream = openFileWrite(outFile, false);
 		writeFromStream(iStream, oStream);
 		iStream.close();
 		oStream.close();
 		
 	}
 	
 	
 	public static String md5(InputStream iStream) throws IOException, CriticalException{
 		
 		/*MessageDigest md5Calculator;
 		try {
 			md5Calculator = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			throw new CriticalException("Unable to initialize MD5 calculator!");
 		}
 				
 		byte[] input = new byte[524288];
 		
 		while(true)
 		{
 		
 			int len = iStream.read(input);
 			if(len < 0) break;
 			md5Calculator.update(input, 0, len);
 					
 		}
 		
 		BigInteger md5Value = new BigInteger(1, md5Calculator.digest());
 
 		return md5Value.toString(16);
 		*/
 		
 		MD5 md5 = new MD5();
 		md5.Init();
 		
 		byte[] input = new byte[524288];
 		
 		while(true)
 		{
 		
 			int len = iStream.read(input);
 			if(len < 0) break;
 			md5.Update(input, len);
 					
 		}
 		
 		return md5.asHex();
 		
 	}
 
 	public static boolean fileExists(String path) {
 		try {
 			File f = new File(path);
 			return f.exists() && !f.isDirectory();
 		} catch (Exception e) {
 			Log.warn(e);
 			return false;
 		}
 	}
 
 	public static boolean folderExists(String path) {
 		try {
 			File f = new File(path);
 			return f.exists() && f.isDirectory();
 		} catch (Exception e) {
 			Log.warn(e);
 			return false;
 		}
 	}
 
 	public static void createFolder(String path) {
 		
 		File f = new File(path);
 		f.mkdirs();
 		
 	}
 
 }
