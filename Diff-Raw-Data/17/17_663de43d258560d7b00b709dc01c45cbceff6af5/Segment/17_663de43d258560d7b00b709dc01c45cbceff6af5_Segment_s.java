 /******************************************************************************* 
  *  Copyright 2010-2011 Axial Exchange Inc., All rights reserved.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  ******************************************************************************* 
  */
 package org.axialproject.axialconnect.hl7.model;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 
 /**
  * @author N. Lee Faus
  * @version 0.1
  */
 public class Segment implements ISegment {
 	private char segment_value_seperator = '|';
 	private char end_of_segment = 0x0d;
 	private String segment;
 	private String segmentType;
 	protected String type;
 	
 	public Segment() {
 	}
 	
 	public Segment(String segment) {
 		this.segment = segment;
 		Pattern p = Pattern.compile("[" + segment_value_seperator + "]");
 		String[] str = p.split(segment);
 		segmentType = str[0];
 	}
 
 	@SuppressWarnings("unchecked")
 	public HashMap<String, String> getValues() {	
 		HashMap<String, String> valueMap = new HashMap<String, String>();
 		try {
 			Class<?> cls = Class.forName("org.axialproject.axialconnect.hl7.model." + segmentType.toUpperCase());
 			Constructor<?> ct = cls.getConstructor(new Class[0]);
 			Object objinst = ct.newInstance(new Object[0]);
 			Method meth2 = cls.getMethod("getFields", null);
 			String[] fields = (String[])meth2.invoke(objinst, null);
 			Class<?> paramTypes[] = new Class[2];
 			paramTypes[0] = String.class;
 			paramTypes[1] = String[].class;
 			Method meth = cls.getMethod("parse", paramTypes);
 			Object arglist[] = new Object[2];
 			arglist[0] = segment;
 			arglist[1] = fields;
 			valueMap = (HashMap<String, String>)meth.invoke(objinst, arglist);			
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		return valueMap;
 	}
 	
 	@Override
 	public HashMap<String, String> parse(String segment, String[] fields) {
 		HashMap<String, String> map = new HashMap<String, String>();
 		Pattern p = Pattern.compile("[" + segment_value_seperator + "]");
 		String[] str = p.split(segment);
 		System.out.println("Fields :: " + fields.length);
 		System.out.println("Fields in Message :: " + str.length);
 		
 		int count = 0;
 		for (String f : str) {
 			System.out.println(fields[count] + " == " + str[count]);
 			map.put(fields[count], f);
 			count++;
 		}
 		return map;
 	}
 	
 	@Override
 	public String create(String[] params) {
 		StringBuffer ack = new StringBuffer();
		ack.append(type);
 		for (String f : params) {
			ack.append(segment_value_seperator);
 			ack.append(f);
 		}
 		ack.append(end_of_segment);
 		return ack.toString();
 	}
 }
