 package edu.washington.cs.cse490h.lib;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.nio.charset.Charset;
 
 /**
  * File reader abstraction. This is basically a wrapped BufferedReader except it
  * will store things in the correct place. Students should only use the provided
  * classes to access the disc.
  */
 public class PersistentStorageReader extends BufferedReader {
 	PersistentStorageReader(Node n, String filename)
 			throws FileNotFoundException {
                super(new InputStreamReader(new FileInputStream(Utility.realFilename(n.addr, filename)), Charset.forName("UTF-8")));
 	}
 }
