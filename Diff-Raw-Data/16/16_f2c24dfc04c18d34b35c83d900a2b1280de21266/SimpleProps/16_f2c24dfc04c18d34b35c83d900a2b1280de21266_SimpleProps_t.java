 /**
 Copyright 2008, 2009 Mark Hooijkaas
 
 This file is part of the RelayConnector framework.
 
 The RelayConnector framework is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 The RelayConnector framework is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with the RelayConnector framework.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.kisst.cfg4j;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 
 public class SimpleProps extends PropsBase {
 	private static final long serialVersionUID = 1L;
 
 	private final Map<String, Object> map=new LinkedHashMap<String, Object>();
 
 	public Set<String> keySet() { return map.keySet(); }
 	
 	public void put(String key, Object value) {
 		//System.out.println("put "+key+"="+value);
 		int pos=key.indexOf('.');
 		if (pos<0) {
 			//System.out.println(key+"="+value);
 			map.put(key, value);
 			return;
 		}
 		String keystart=key.substring(0,pos);
 		String keyremainder=key.substring(pos+1);
 		Object o=map.get(keystart);
 		//System.out.print(keystart+"->");
 		if (o instanceof SimpleProps)
 			((SimpleProps)o).put(keyremainder, value);
 		else if (o==null) {
 				SimpleProps props=new SimpleProps();
 				map.put(keystart, props);
 				props.put(keyremainder, value);
 		}
 		else 
 			throw new RuntimeException("key "+key+" already has a value");
 	}
 
 	public Object get(String key, Object defaultValue) {
 		int pos=key.indexOf('.');
 		if (pos<0)
 			return map.get(key);
 		String keystart=key.substring(0,pos);
 		String keyremainder=key.substring(pos+1);
 		Object o=map.get(keystart);
 		//System.out.print(keystart+"->");
 		if (o instanceof SimpleProps)
 			return ((SimpleProps)o).get(keyremainder,null);
 		else if (o==null)
 			return defaultValue;
 		else 
 			throw new RuntimeException("key "+key+" has a value that should be a map");
 	}
 	
 	public void load(String filename)  {
 		FileInputStream inp = null;
 		try {
 			try {
 				inp = new FileInputStream(filename);
 				read(inp);
 			}
 			finally {
 				if (inp!=null) inp.close();
 			}
 		}
 		catch (IOException e) { throw new RuntimeException(e); }
 	}
 
 	public void read(InputStream inp)  {
 		readMap(new BufferedReader(new InputStreamReader(inp)));
 	}
 	
 	private Object readObject(BufferedReader inp)  {
 		int c;
 		while (true){
 			try {
 				c=inp.read();
 			} catch (IOException e) { throw new RuntimeException(e); }
 			if (c<0)
 				return null;
 			char ch=(char) c;
 			if (ch == '{' ) {
 				SimpleProps result=new SimpleProps();
 				result.readMap(inp);
 				return result;
 			}
 			else if (ch == '[' )
 				return readList(inp);
 			else if (ch == ' ' || ch == '\t' || ch == '\n')
 				continue;
 			else if (ch=='"')
				return readDoubleQuotedString(inp);
 			else if (Character.isLetterOrDigit(ch) || ch=='/' || ch=='.' || ch==':')
 				return ch+readUnquotedString(inp);
 		}
 	}
 	private String readDoubleQuotedString(BufferedReader inp) {
		String result=readUntil("\"",inp).trim();
		return result.substring(0, result.length()-1);
 	}
 
 	private String readUnquotedString(BufferedReader inp) {
		String result=readUntil(" \t\n,;}]",inp).trim();
		if (result.endsWith(";") || result.endsWith("}") || result.endsWith("]"))
			return result.substring(0,result.length()-1);
		else
			return result;
 	}
 
 	
 	private Object readList(BufferedReader inp) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private void readMap(BufferedReader input)  {
 		while (true) {
 			String str=readUntil("+=:}\n#",input);
 			if (str==null)
 				return;
 			str=str.trim();
 			if (str.length()==0) 
 				continue;
			else if (str.startsWith("}")) 
				return;
 			else if (str.startsWith("#")) 
 				skipLine(input);
 			else if (str.endsWith("=") || str.endsWith(":") )
 				put(str.substring(0,str.length()-1).trim(), readObject(input));
 			else if (str.endsWith("+")) {
 				char ch;
 				try {
 					ch = (char) input.read();
 				} catch (IOException e) { throw new RuntimeException(e);}
 				if (ch != '=')
 					throw new RuntimeException("+ should only be used in +=");
 				throw new RuntimeException("+= not yet supported");
 			}
 		}
 	}
 
 	
 	
 	private String readUntil(String endchars, Reader inp) {
 		StringBuilder result=new StringBuilder();
 		int c;
 		while (true){
 			try {
 				c=inp.read();
 			} catch (IOException e) { throw new RuntimeException(e); }
 			if (c<0) {
 				if (result.length()==0)
 					return null;
 				break;
 			}
 			char ch=(char) c;
 			result.append(ch);
 			if (endchars.indexOf(ch)>=0)
 				break;
 		}
 		return result.toString();
 	}
 
 	private void skipLine(Reader inp) {
 		int c;
 		while (true){
 			try {
 				c=inp.read();
 			} catch (IOException e) { throw new RuntimeException(e); }
 			if (c<0)
 				break;
 			char ch=(char) c;
 			if (ch=='\n')
 				break;
 		}
 	}
 
 	public String toString() { return toString("");	}
 	public String toString(String indent) {
 		StringBuilder result=new StringBuilder("{\n");
 		for (String key: map.keySet()) {
 			result.append(indent+"\t"+key+": ");
 			Object o=map.get(key);
 			if (o instanceof SimpleProps)
 				result.append(((SimpleProps)o).toString(indent+"\t"));
 			else if (o instanceof String)
 				result.append("\""+o+"\";\n");
 			else
 				result.append(o.toString());
 			//result.append("\n");
 		}
 		result.append(indent+"}\n");
 		return result.toString();
 	}
 }
