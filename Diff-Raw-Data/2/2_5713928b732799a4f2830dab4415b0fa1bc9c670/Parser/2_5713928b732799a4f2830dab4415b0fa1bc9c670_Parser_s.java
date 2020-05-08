 package org.kisst.cfg4j;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
 import org.kisst.util.FileUtil;
 
 public class Parser {
 	private final File file;
 	private final BufferedReader inp;
 	private char lastchar;
 	private boolean eof=false;
 	private boolean useLastChar=false;
 	private int line=1;
 	private int pos=0;
 
 
 	private Parser(Reader inp, File f) {
 		this.file=f; 
 		if (inp instanceof BufferedReader)
 			this.inp=(BufferedReader) inp;
 		else
 			this.inp=new BufferedReader(inp);
 	}
 	public Parser(Reader inp) { this(inp,null); }
 	public Parser(String filename)       { this(new File(filename));	}
 	public Parser(File f)                { this(new InputStreamReader(FileUtil.open(f)), f); }
 	public Parser(InputStream inpstream) { this(new InputStreamReader(inpstream)); }
 	public File getFile() { return file; }
 	public File getPath(String path) {
 		if (file==null)
 			return new File(path);
 		else if (file.isDirectory())
 			return new File(file,path);
 		else
 			return new File(file.getParent(), path);
 	}
 	
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
 			do {
 				ch = inp.read();
 			}
 			while (ch=='\r'); // ignore all carriage returns
 		} catch (IOException e) { throw new ParseException(e); }
 		if (ch=='\n') {
 			line++; pos=0;
 		}
 		else
 			pos++;
 		if (ch<0)
 			eof=true;
 		else
 			lastchar=(char)ch;
 		return lastchar;
 	}
 
 	
 	public String readIdentifier() {
 		skipWhitespaceAndComments();
 		StringBuilder result=new StringBuilder();
 		while (! eof()){
 			char ch=read();
 			if (Character.isLetterOrDigit(ch) || ch=='_')
 				result.append(ch);
 			else {
 				unread();
 				break;
 			}
 		}
 		return result.toString();
 	}
 	public String readIdentifierPath() {
 		skipWhitespaceAndComments();
 		StringBuilder result=new StringBuilder();
 		while (! eof()){
 			char ch=read();
 			if (Character.isLetterOrDigit(ch) || ch=='.' || ch=='_')
 				result.append(ch);
 			else {
 				unread();
 				break;
 			}
 		}
 		return result.toString();
 	}
 	public String readDoubleQuotedString() { return readUntil("\"").trim(); }
 	public String readSingleQuotedString() { return readUntil("\'").trim(); }
 	public String readUnquotedString() { return readUntil("\n").trim(); }
 
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
 				if (ch!='\n')
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
 
 	public void skipWhitespaceAndComments() {
 		while (! eof()){
 			char ch=read();
 			if (ch=='#') {
 				skipLine();
 				continue;
 			}
 			if (ch!=' ' && ch!='\t' && ch!='\n' && ch!='\r') {
 				unread(); 
 				return;
 			}
 		}
 	}
 	public void skipLine() {
 		while (! eof()){
 			char ch=read();
 			if (ch=='\n')
 				break;
 		}
 	}
 	
 	public class ParseException extends RuntimeException {
 		private static final long serialVersionUID = 1L;
 		public ParseException(String message) {
 			super("Parse exception: file "+file+", line:"+line+" pos: "+pos+": "+message);
 		}
 		public ParseException(Exception e) {
 			super("Parse exception: file "+file+", line:"+line+" pos: "+pos+": "+e.getMessage(), e);
 		}
 	}
 	
 
 	private Object readObject()  { return readObject(null,null); }
 
 	private Object readObject(SimpleProps parent, String name)  {
 		skipWhitespaceAndComments();
 		while (! eof()){
 			char ch=read();
 			if (eof())
 				return null;
 			if (ch == '{' ) {
 				return readMap(parent, name);
 			}
 			else if (ch == '[' )
 				return readList();
 			else if (ch == '(' )
 				return readParamList();
 			else if (ch == ' ' || ch == '\t' || ch == '\n')
 				continue;
 			else if (ch=='"')
 				return readDoubleQuotedString();
 			else if (Character.isLetterOrDigit(ch) || ch=='/' || ch=='.' || ch==':')
 				return ch+readUnquotedString();
 			else if (ch=='@')
 				return readSpecialObject();
 		}
 		return null;
 	}
 	private Object readSpecialObject() {
 		String type=readUntil("(;").trim();
 		if (type.equals("file")) {
 			String filename=readUntil(")").trim();
 			return getPath(filename);
 		}
 		else if (type.equals("null")) 
 			return null;
 		else
 			throw new ParseException("Unknown special object type @"+type);
 	}
 
 
 	private Object readList() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	
 	private Object readParamList() {
 		// A paramlist is like a list, but may also contain keyword parameters
 		// TODO: currently it is just like an object with parenthesis
 		Object result=readObject();
 		skipWhitespaceAndComments();
 		if (getLastChar()!=')')
 			throw new ParseException("parameter list should end with )");
 		return result;
 	}
 
 	public SimpleProps readMap(SimpleProps parent, String name)  {
 		SimpleProps map=new SimpleProps(parent, name);
 		fillMap(map);
 		return map;
 	}
 	public void fillMap(SimpleProps map)  {
 		while (! eof()) {
 			skipWhitespaceAndComments();
 			char ch=read();
 			if (ch=='}')
 				break; // map has ended
 			else if (ch=='@'){
 				String cmd=readIdentifierPath();
 				if (cmd.equals("include")) 
 					include(map,this);
 				continue;
 			}
 			else if (ch==';')
 				continue; // ignore
 			else if (Character.isLetter(ch) || ch=='_') {
 				unread();
 				String key=readIdentifierPath();
 				skipWhitespaceAndComments();
 				if (getLastChar() == '=' || getLastChar() ==':' )
 					map.put(key, readObject(map, key));
 				else if (getLastChar() == '+') {
					char ch2 = (char) read();
 					if (ch2 != '=')
 						throw new ParseException("+ should only be used in +=");
 					throw new ParseException("+= not yet supported");
 				}
 				else 
 					throw new ParseException("field assignment "+key+" in map "+map.getFullName()+" should have =, : or +=, not "+ch);
 			}
 			else if (eof())
 				break;
 			else
 				throw new ParseException("when parsing map "+map.getFullName()+" unexpected character "+getLastChar());
 		}
 	}
 
 
 
 	private void include(SimpleProps map, Parser inp) {
 		Object o=readObject();
 		File f=null;
 		if (o instanceof File)
 			f=(File) o;
 		else if (o instanceof String)
 			f=inp.getPath(o.toString());
 		else
 			throw inp.new ParseException("unknown type of object to include "+o);
 		if (f.isFile())
 			map.load(f);
 		else if (f.isDirectory()) {
 			File[] files = f.listFiles(); // TODO: filter
 			for (File f2: files) {
 				if (f2.isFile())
 					map.load(f2);
 			}
 		}
 	}
 
 }
