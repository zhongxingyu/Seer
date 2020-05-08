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
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.kisst.util.XmlNode;
 
 
 public class SimpleProps extends PropsBase {
 	private static final long serialVersionUID = 1L;
 
 	private final SimpleProps parent;
 	private final String name; 
 	private final Map<String, Object> map=new LinkedHashMap<String, Object>();
 
 	public SimpleProps() { this(null,""); }
 	public SimpleProps(SimpleProps parent, String name) {
 		this.parent=parent;
 		this.name=name;
 	}
 	public String getName() { return name; }
 	public String getFullName() {
 		if (parent==null)
 			return name;
 		else
 			return parent.getFullName()+"."+name;
 	}
 	
 	public Set<String> keySet() { return map.keySet(); }
 	
 	public void put(String key, Object value) {
 		//System.out.println("put "+key+"="+value);
 		int pos=key.indexOf('.');
 		if (pos<0) {
 			//System.out.println(key+"="+value);
 			if (value==null)
 				map.remove(key);
 			else
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
 	
 	public void load(String filename)  { readMap(new Parser(filename));	}
 	public void load(File file)        { readMap(new Parser(file));	}
 	public void read(Reader inp)       { readMap(new Parser(inp)); }
 	public void read(InputStream inp)  { readMap(new Parser(inp)); }
 
	private Object readObject(Parser inp)  {
 		while (! inp.eof()){
 			char ch=inp.read();
 			if (inp.eof())
 				return null;
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
 				return inp.readDoubleQuotedString();
 			else if (Character.isLetterOrDigit(ch) || ch=='/' || ch=='.' || ch==':')
 				return ch+inp.readUnquotedString();
 			else if (ch=='@')
 				return readSpecialObject(inp);
 		}
 		return null;
 	}
 	private Object readSpecialObject(Parser inp) {
 		String type=inp.readUntil("(;").trim();
 		if (type.equals("file")) {
 			String filename=inp.readUntil(")").trim();
 			return new File(filename);
 		}
 		else if (type.equals("null")) 
 			return null;
 		else
 			throw new RuntimeException("Unknown special object type @"+type);
 	}
 
 	
 	private Object readList(Parser inp) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private void readMap(Parser inp)  {
 		while (! inp.eof()) {
 			String str=inp.readUntil("+=:}\n");
 			if (str==null)
 				return;
 			str=str.trim();
 			if (str.startsWith("#")) { 
 				inp.skipLine();
 			}
 			else if (inp.getLastChar() == '}') 
 				return;
 			else if (str.startsWith("@include")) 
 				include(str.substring(8).trim());
 			else if (inp.getLastChar() == '=' || inp.getLastChar() ==':' )
				put(str.trim(), readObject(inp));
 			else if (inp.getLastChar() == '+') {
 				char ch = (char) inp.read();
 				if (ch != '=')
 					throw new RuntimeException("+ should only be used in +=");
 				throw new RuntimeException("+= not yet supported");
 			}
 		}
 	}
 
 	
 	
 	private void include(String path) {
 		File f=new File(path);
 		if (f.isFile())
 			load(f);
 		else if (f.isDirectory()) {
 			File[] files = f.listFiles(); // TODO: filter
 			for (File f2: files) {
 				if (f2.isFile())
 					load(f2);
 			}
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
 	
 	
 	public void readXml(XmlNode node)  {
 		for (XmlNode child : node.getChildren()) {
 			String name=child.getName();
 			if (child.getChildren().size()>0) {
 				SimpleProps p=(SimpleProps) getProps(name);
 				if (p==null) {
 					p=new SimpleProps(this,name);
 					put(name,p);
 				}
 				p.readXml(child);
 			}
 			else 
 				put(name, child.getText());
 		}
 	}
 
 }
