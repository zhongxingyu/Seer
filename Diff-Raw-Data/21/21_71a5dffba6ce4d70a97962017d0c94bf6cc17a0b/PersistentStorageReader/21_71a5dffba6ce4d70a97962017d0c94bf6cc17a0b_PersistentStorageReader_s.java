 package edu.washington.cs.cse490h.lib;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
import java.io.FileReader;
 
 /**
  * File reader abstraction. This is basically a wrapped BufferedReader except it
  * will store things in the correct place. Students should only use the provided
  * classes to access the disc.
  */
 public class PersistentStorageReader extends BufferedReader {
 	PersistentStorageReader(Node n, String filename)
 			throws FileNotFoundException {
		super(new FileReader(Utility.realFilename(n.addr, filename)));
 	}
 }
