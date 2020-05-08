 package com.mathieubolla.io;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.nio.channels.ReadableByteChannel;
 import java.security.MessageDigest;
 
 public class Md5Summer {
 	public String hash(File file) {
 		try {
 			MessageDigest md5 = MessageDigest.getInstance("MD5");
 			
 		    ReadableByteChannel channel = new FileInputStream(file).getChannel();
 		    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
 
 		    int numRead = 0;
 		    while (numRead >= 0) {
 		    	buffer.rewind();
 		        numRead = channel.read(buffer);
 		        buffer.rewind();
 		        if (numRead == 1024) {
 		        	md5.update(buffer);
 		        } else if (numRead >= 0) {
 		        	byte[] endOfFile = new byte[numRead];
 		        	buffer.get(endOfFile);
 		        	md5.update(endOfFile);
 		        }
 		    }
 		    
 		    byte[] digest = md5.digest();
 		    
 		    return toHex(digest);
 		} catch (Exception e) {
 			throw new RuntimeException("Error hash "+file, e);
 		}
 	}
 	
 	private static String toHex(byte[] bytes) {
 		BigInteger bi = new BigInteger(1, bytes);
 	    String result = bi.toString(16);
 	    if (result.length() != 32) {
	    	for (int i = 0; i <= (32 - result.length()); i++) {
 	    		result = "0" + result;
 	    	}
 	        return result;
 	    }
 	    return result;
 	}
 }
