 package edu.washington.cs.cse490h.lib;
 
 import java.io.BufferedWriter;
 import java.io.File;
import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 /**
  * Class that nodes use to write to persistent storage. This class can be used
  * exactly the same way as a BufferedWriter, and students should use only this
  * and the PersistentStorageReader to access disc
  * 
  * The key difference is that any action that modifies a file can potentially
  * crash before the modification is made. This is to model cases where the node
  * fails between calls to onReceive.
  * 
  * Note that ANY modification can cause a crash with equal probability, so
  * write('a'); write('b'); write('c'); newLine(); has a higher chance of causing
  * a crash than write("abc\n");
  * 
  */
 public class PersistentStorageWriter extends BufferedWriter {
 	private File f;
 	private Node n;
 
 	PersistentStorageWriter(Node n, File f, boolean append) throws IOException{
		super(new FileWriter(f, append));
 		this.n = n;
 		this.f = f;
 	}
 
 	// methods for the file writer
 	@Override
 	public void write(int c) throws IOException {
 		n.checkWriteCrash("write(" + c + ")");
 		
 		super.write(c);
 		super.flush();
 	}
 
 	@Override
 	public void write(char[] cbuf, int off, int len) throws IOException {
 		n.checkWriteCrash("write(cbuf, " + off +  ", " + len + ")");
 		
 		super.write(cbuf, off, len);
 		super.flush();
 	}
 
 	@Override
 	public void write(String s, int off, int len) throws IOException {
 		n.checkWriteCrash("write(s, " + off +  ", " + len + ")");
 		
 		super.write(s, off, len);
 		super.flush();
 	}
 
 	@Override
 	public void newLine() throws IOException {
 		n.checkWriteCrash("newLine()");
 		
 		super.newLine();
 		super.flush();
 	}
 
 	@Override
 	public void write(char[] cbuf) throws IOException {
 		n.checkWriteCrash("write(cbuf)");
 		
 		super.write(cbuf);
 		super.flush();
 	}
 
 	@Override
 	public Writer append(CharSequence csq) throws IOException {
 		n.checkWriteCrash("append(csq)");
 		
 		Writer ret = super.append(csq);
 		super.flush();
 		
 		return ret;
 	}
 
 	@Override
 	public Writer append(CharSequence csq, int start, int end)
 			throws IOException {
 		n.checkWriteCrash("append(csq, " + start + ", " + end + ")");
 		
 		Writer ret = super.append(csq, start, end);
 		super.flush();
 		
 		return ret;
 	}
 
 	@Override
 	public Writer append(char c) throws IOException{
 		n.checkWriteCrash("append(" + c + ")");
 		
 		Writer ret = super.append(c);
 		super.flush();
 		
 		return ret;
 	}
 
 	@Override
 	public void write(String str) throws IOException{
 		n.checkWriteCrash("write(str)");
 		
 		super.write(str);
 		super.flush();
 	}
 
 	public boolean delete() throws IOException{
 		n.checkWriteCrash("delete of" + f.getName());
 		
 		this.close();
 		return f.delete();
 	}
 }
