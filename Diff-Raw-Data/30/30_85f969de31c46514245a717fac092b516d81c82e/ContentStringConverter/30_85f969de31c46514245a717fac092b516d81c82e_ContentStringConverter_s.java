 //
 //  ContentStringConverter.java
 //  ChatServer
 //
 //  Created by John McKisson on 11/16/10.
 //  Copyright 2010 Jefferson Lab. All rights reserved.
 //
 import com.thoughtworks.xstream.converters.basic.StringConverter;
 
 public class ContentStringConverter extends StringConverter {
 	//Unmarshall
 	public Object fromString(String str) {
		str = str.replaceAll("&#x0D;", "\r");
 		
		str = str.replaceAll("&quot;", "\"");
 	
		return str.replaceAll("&#x0A;", "\n");
 	}
 	
 	//Marshall
 	public String toString(Object obj) {
 	
 		String output = (String)obj;
 		
		output = output.replaceAll("\n", "&#x0A;\n");
 				
		return output.replaceAll("\r", "&#x0D;\r");
 	}
 }
