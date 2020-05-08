 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: FileTransfer.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: FileTransfer
  */
package edu.columbia.mipl.runtime;
 
 import java.io.*;
 import java.nio.channels.*;
 import java.util.*;
 
 import org.apache.hadoop.hdfs.DFSClient;
 
 public class FileTransfer {
 	private static final int BLOCK_SIZE = 8192;
 
 	DFSClient dfsc;
 
 	FileTransfer() {
 		// dfsc = new DFSClient(Configration.getProperty(Configuration.NAMENODE));
 	}
 
 	void copy(InputStream in, OutputStream out) throws IOException {
 		try {
 			byte[] buf = new byte[BLOCK_SIZE];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 		}
 		finally {
 			in.close();
 			out.close();
 		}
 	}
 
 	void upload(String local, String remote) throws IOException {
 		try {
 			copy(new FileInputStream(new File(local)), dfsc.create(remote, true));
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new IOException();
 		}
 	}
 
 	void download(String local, String remote) throws IOException {
 		try {
 			copy(dfsc.open(remote), new FileOutputStream(new File(local)));
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new IOException();
 		}
 	}
 }
