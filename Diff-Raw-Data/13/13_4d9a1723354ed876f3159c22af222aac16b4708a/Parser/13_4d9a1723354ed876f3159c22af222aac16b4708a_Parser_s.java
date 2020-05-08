 package org.kisst.cfg4j;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 import org.kisst.util.FileUtil;
 
 public class Parser {
 	private final BufferedReader inp;
 	private char lastchar;
 	private boolean eof=false;
 	private boolean useLastChar=false;
 	
 	public Parser(Reader inp) {
 		if (inp instanceof BufferedReader)
 			this.inp=(BufferedReader) inp;
 		else
 			this.inp=new BufferedReader(inp);
 	}
 	public Parser(String filename)       { this(new File(filename));	}
 	public Parser(File f)                { this(FileUtil.open(f)); }
 	public Parser(InputStream inpstream) { this(new InputStreamReader(inpstream)); }
 	
 	public char getLastChar() { return lastchar; }
 	public boolean eof() {return eof; }
 	
 	public void unread() { useLastChar=true; }
 	public char read() {
 		if (useLastChar) {
 			useLastChar=false;
 			return lastchar;
 		}
 		int ch;
 		try {
 			ch = inp.read();
 		} catch (IOException e) { throw new RuntimeException(e); }
 		if (ch<0)
 			eof=true;
 		else
 			lastchar=(char)ch;
 		return lastchar;
 	}
 	
 	
 	
 	
 	public String readDoubleQuotedString() { return readUntil("\"").trim(); }
 	public String readUnquotedString() { return readUntil(" \t\n,;}]").trim(); }
 
 	public String readUntil(String endchars) {
 		StringBuilder result=new StringBuilder();
 		while (! eof()){
 			char ch=read();
 			if (eof())
 				break;
 			if (ch=='\\') {
 				ch=read();
 				if (eof())
 					break;
 				result.append(ch);
 			}
 			else {
 				if (endchars.indexOf(ch)>=0)
 					break;
 				result.append(ch);
 			}
 		}
 		if (eof()) {
 			if (result.length()==0)
 				return null;
 		}
 		return result.toString();
 	}
 
 	public void skipLine() {
		System.out.print("SKipping");
 		while (! eof()){
 			char ch=read();
 			if (ch=='\n')
 				break;
			System.out.print(ch);
 		}
 	}
 
 }
