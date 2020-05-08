 package edu.washington.cs.cse490h.lib;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 /**
  * File reader abstraction. This is basically a wrapped BufferedReader except it
  * will store things in the correct place. Students should only use the provided
  * classes to access the disc.
  */
 public class PersistentStorageReader extends BufferedReader {
 	private final Node n;
 
 	PersistentStorageReader(Node n, String filename)
 			throws FileNotFoundException {
 		super(new FileReader(Utility.realFilename(n.addr, filename)));
 		this.n = n;
 	}
 
 	@Override
 	public int read() throws IOException {
 		int ret = super.read();
 		char[] chars = new char[] { (char) ret };
 		n.handleDiskReadEvent("cbuf:" + Utility.logEscape(new String(chars)));
 		return ret;
 	}
 
 	@Override
 	public int read(char[] cbuf, int off, int len) throws IOException {
 		int ret = super.read(cbuf, off, len);
 		n.handleDiskReadEvent("cbuf:" + Utility.logEscape(new String(cbuf))
 				+ " offset:" + off + " len:" + len);
 		return ret;
 	}
 
 	@Override
 	public String readLine() throws IOException {
 		String ret = super.readLine();
		if (ret != null)
		{
			n.handleDiskReadEvent("readline: " + Utility.logEscape(ret));
		}
		
 		return ret;
 	}
 }
