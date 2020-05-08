 /*
 
 Copyright (c) 2009-2011, AOL Inc.
 All rights reserved.
 
 This code is licensed under a BSD license.
 
 Howard Uman
 
 */
 
 package com.aol.webservice_base.view.serializer;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 public class JSONSerializer extends BaseSerializer {
 
 	protected static final byte[] OPEN_BRACE = "{".getBytes();
 	protected static final byte[] CLOSE_BRACE = "}".getBytes();
 	protected static final byte[] OPEN_SQUARE = "[".getBytes();
 	protected static final byte[] CLOSE_SQUARE = "]".getBytes();
 	protected static final byte[] QUOTES = "\"".getBytes();
 	protected static final byte[] QUOTES_SEPARATOR = "\":".getBytes();
 	protected static final byte[] COMMA = ",".getBytes();
 	
 	@Override
 	protected boolean wantsNulls() {
 		return false;
 	}
 
 	@Override
 	protected void startNode(DataOutputStream os, String name, boolean isSimple, Object object) throws IOException {
 		os.write(QUOTES);
 		os.write(name.getBytes());
 		os.write(QUOTES_SEPARATOR);
 		if (!isSimple) {
 			os.write(OPEN_BRACE);
 		}
 	}
 
 	@Override
 	protected void endNode(DataOutputStream os, String name, boolean isSimple, Object object) throws IOException {
 		if (!isSimple) {
 			os.write(CLOSE_BRACE);
 		}
 	}
 
 	@Override
 	protected void startArrayNode(DataOutputStream os, int index, String name, boolean isSimple, Object object) throws IOException {
 		if (!isSimple) {
 			os.write(OPEN_BRACE);
 		}
 	}
 
 	@Override
 	protected void endArrayNode(DataOutputStream os, String name, boolean isSimple, Object object) throws IOException {
 		if (!isSimple) {
 			os.write(CLOSE_BRACE);
 		}
 	}
 
 	@Override
 	protected void separateNodes(DataOutputStream os) throws IOException {
 		os.write(COMMA);
 	}
 
 	@Override
 	protected void writeHeader(DataOutputStream os) throws IOException {
 		os.write(OPEN_BRACE);
 	}
 
 	@Override
 	protected void writeFooter(DataOutputStream os) throws IOException {
 		os.write(CLOSE_BRACE);
 	}
 
 	@Override
 	protected void startArray(DataOutputStream os, int size) throws IOException {
 		os.write(OPEN_SQUARE);
 	}
 
 	@Override
 	protected void endArray(DataOutputStream os) throws IOException {
 		os.write(CLOSE_SQUARE);
 	}
 
 	protected boolean valueNeedsQuotes(Object value) {
 		return value.getClass().getName().equals("java.lang.String");
 	}
 
 	@Override
 	protected void writeValue(DataOutputStream os, Object value) throws IOException {
 		boolean valueNeedsQuotes = valueNeedsQuotes(value);
 
 		// primitive types don't get quotes
 		if (valueNeedsQuotes) {
 			os.write(QUOTES);
 		}
 
 		// any quotes need to be escaped
		os.write(escape(value.toString()).getBytes());
 
 		if (valueNeedsQuotes) {
 			os.write(QUOTES);
 		}
 	}
 
 	protected static String escape(String s) {
 		if (s == null) {
 			return null;
 		}
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < s.length(); i++) {
 			char ch = s.charAt(i);
 			switch (ch) {
 			case '"':
 				sb.append("\\\"");
 				break;
 			case '\\':
 				sb.append("\\\\");
 				break;
 			case '\b':
 				sb.append("\\b");
 				break;
 			case '\f':
 				sb.append("\\f");
 				break;
 			case '\n':
 				sb.append("\\n");
 				break;
 			case '\r':
 				sb.append("\\r");
 				break;
 			case '\t':
 				sb.append("\\t");
 				break;
 			case '/':
 				sb.append("\\/");
 				break;
 			default:
 				if (ch >= '\u0000' && ch <= '\u001F') {
 					String ss = Integer.toHexString(ch);
 					sb.append("\\u");
 					for (int k = 0; k < 4 - ss.length(); k++) {
 						sb.append('0');
 					}
 					sb.append(ss.toUpperCase());
 				} else {
 					sb.append(ch);
 				}
 			}
 		}//for
 		return sb.toString();
 	}
 }
