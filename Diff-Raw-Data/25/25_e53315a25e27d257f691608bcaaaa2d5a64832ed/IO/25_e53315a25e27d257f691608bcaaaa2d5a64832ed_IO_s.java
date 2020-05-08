 package com.github.norwae.whatiread.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class IO {
 	public static void copy(OutputStream out, InputStream in)
 			throws IOException {
		byte[] buffer = new byte[8129];
 		int n;
 		while (-1 != (n = in.read(buffer))) {
 			out.write(buffer, 0, n);
 		}
 	}
 
 	public static byte[] read(InputStream in) throws IOException {
 		ByteArrayOutputStream temp = new ByteArrayOutputStream();
 		copy(temp, in);
 		return temp.toByteArray();
 	}
 
 }
