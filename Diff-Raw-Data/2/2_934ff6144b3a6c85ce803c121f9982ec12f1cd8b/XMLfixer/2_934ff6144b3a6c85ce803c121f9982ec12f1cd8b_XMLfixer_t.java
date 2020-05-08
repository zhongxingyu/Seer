 package com.where.utils;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 public class XMLfixer {
 
 	private static StringBuffer convertToString(InputStream ins) throws IOException
 	{
 		BufferedInputStream bis = new BufferedInputStream(ins);
 	    ByteArrayOutputStream buf = new ByteArrayOutputStream();
 	    int result = bis.read();
 	    while(result != -1) {
 	      byte b = (byte)result;
 	      buf.write(b);
 	      result = bis.read();
 	    }        
 	    return new StringBuffer(buf.toString());	
 	}
 	
 	private static InputStream convertToInputStream(String str) throws UnsupportedEncodingException
 	{
			return new ByteArrayInputStream(str.getBytes("US-ASCII"));
 	}
 	
 	private static String replaceProductServices(String buf)
 	{
 		StringBuilder builder = new StringBuilder(buf);
 		
 		while(builder.indexOf("<products_services>") > -1)
 		{
 			builder.delete(builder.indexOf("<products_services>"), builder.indexOf("</products_services>")+20);
 		}
 		
 		return builder.toString();
 	}
 	
 	public static InputStream repairXML(InputStream ins)
 	{
 		String buf;
 		try {
 			buf = convertToString(ins).toString();
 			return convertToInputStream(replaceProductServices(buf.replace("&amp;","&").replace("&", "&amp;")));
 		}
 		catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		return null;
 		
 	}
 
 }
