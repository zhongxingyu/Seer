 package com.tommytony.io;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.util.zip.GZIPInputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 public class Deflate {
 
 	final static int BUFFER_SIZE = 4096;
 	
 	public static void Zip(File inputFile, String outputFile) throws IOException {
 		BufferedOutputStream dest = null;
 		ZipInputStream zipin = new ZipInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
 		ZipEntry entry;
 		while((entry = zipin.getNextEntry()) != null) {
			int count;
 			byte[] data = new byte[Deflate.BUFFER_SIZE];
 			dest = new BufferedOutputStream(new FileOutputStream(entry.getName()), Deflate.BUFFER_SIZE);
 			while((count = zipin.read(data, 0, Deflate.BUFFER_SIZE)) != -1) {
 				dest.write(data, 0, Deflate.BUFFER_SIZE);
 			}
 			dest.flush();
 			dest.close();
 			zipin.close();
 		}
 	}
 	
 	public static void gZip(File inputFile, String outputFile) throws IOException {
 		BufferedOutputStream dest = null;
		int count;
 		GZIPInputStream zipin = new GZIPInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
 		byte[] data = new byte[Deflate.BUFFER_SIZE];
 		dest = new BufferedOutputStream(new FileOutputStream(outputFile), Deflate.BUFFER_SIZE);
 		while((count = zipin.read(data, 0, Deflate.BUFFER_SIZE)) != -1) {
 			dest.write(data, 0, Deflate.BUFFER_SIZE);
 		}
 		dest.close();
 		zipin.close();
 	}
 }
