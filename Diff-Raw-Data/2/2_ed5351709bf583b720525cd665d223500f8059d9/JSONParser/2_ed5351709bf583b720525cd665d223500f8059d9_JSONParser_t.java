 package com.zwitserloot.json;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 class JSONParser {
 	private final String string;
 	private final int len;
 	private int pos = 0;
 	
 	JSONParser(String string) {
 		this.string = stripComments(string);
		this.len = this.string.length();
 	}
 	
 	private static String stripComments(String in) {
 		StringBuilder out = new StringBuilder();
 		int start = 0;
 		int end = 0;
 		while (true) {
 			start = end;
 			if (start >= in.length()) break;
 			end = in.indexOf('\n', start);
 			if (end == -1) end = in.length();
 			else end++;
 			String line = in.substring(start, end).trim();
 			if (line.startsWith("#") || line.startsWith("//")) continue;
 			out.append(line);
 		}
 		return out.toString();
 	}
 	
 	Object parseObject() {
 		while (pos < len) {
 			char c = string.charAt(pos++);
 			if (Character.isWhitespace(c)) continue;
 			if (c == '"') return parseString();
 			if (c == '[') return parseList();
 			if (c == '{') return parseMap();
 			if (pos + 2 < len && string.substring(pos -1, pos +3).equals("true")) {
 				pos += 3;
 				return true;
 			}
 			if (pos + 3 < len && string.substring(pos -1, pos +4).equals("false")) {
 				pos += 4;
 				return false;
 			}
 			if (pos + 2 < len && string.substring(pos -1, pos +3).equals("null")) {
 				pos += 3;
 				return null;
 			}
 			if (c == '-' || c == '0' || (c >= '1' && c <= '9')) return parseNumber();
 			return jsonError("Invalid character - expected a string, array, object, 'true', 'false', 'null', or number.");
 		}
 		
 		return jsonError("Incomplete json object");
 	}
 	
 	private <T> T jsonError(String msg) {
 		throw new JSONException(msg + "(" + pos + ")");
 	}
 	
 	private Number parseNumber() {
 		StringBuilder number = new StringBuilder();
 		StringBuilder fraction = new StringBuilder();
 		StringBuilder exponent = new StringBuilder();
 		boolean exponentSign = true;
 		
 		boolean numberSign = string.charAt(pos -1) != '-';
 		if (numberSign && string.charAt(pos -1) != '+') pos--;
 		
 		//0: first digit
 		//1: potential further digits after first
 		//2: first digit after period
 		//3: potential further digits after period
 		//4: just read e, or E
 		//5: first digit after e/E
 		//6: potential further digits after e/E
 		int state = 0;
 		while (pos < len) {
 			char c = string.charAt(pos++);
 			if (c == '.') {
 				if (state == 0) return jsonError("Expected a digit");
 				else if (state == 1) state = 2;
 				else return jsonError("not a valid number - fractional separator (.) not expected here");
 				continue;
 			} else if (c == 'E' || c == 'e') {
 				if (state == 0 || state == 2) return jsonError("Expected a digit");
 				else if (state == 1 || state == 3) state = 4;
 				else return jsonError("not a valid number - exponential separator (E) not expected here");
 			} else if (c == '+') {
 				if (state == 4) state = 5;
 				else return jsonError("not a valid number - '+' not expected here");
 			} else if (c == '-') {
 				if (state == 4) {
 					exponentSign = false;
 					state = 5;
 				} else return jsonError("not a valid number - '-' not expected here");
 			} else if (c < '0' || c > '9') {
 				pos--;
 				break;
 			} else {
 				if (state == 4 || state == 5 || state == 6) {
 					state = 6;
 					exponent.append(c);
 				} else if (state == 0 || state == 1) {
 					state = 1;
 					number.append(c);
 				} else if (state == 2 || state == 3) {
 					state = 3;
 					fraction.append(c);
 				}
 			}
 		}
 		
 		if (state == 0 || state == 2 || state == 4 || state == 5) return jsonError("digit expected");
 		else {
 			return makeNumber(numberSign, number.toString(), fraction.toString(), exponentSign, exponent.toString());
 		}
 	}
 	
 	private Number makeNumber(boolean numberSign, String number, String fraction, boolean exponentSign, String exponent) {
 		String nr = String.format("%s%s%s%s%s%s%s",
 				numberSign ? "" : "-", number, fraction.length() > 0 ? "." : "",
 				fraction, exponent.length() > 0 ? "E" : "", exponentSign ? "" : "-",
 				exponent);
 		
 		if (fraction.length() != 0 || exponent.length() != 0) {
 			try {
 				return Double.parseDouble(nr);
 			} catch (Exception e) {
 				try {
 					return new BigDecimal(nr);
 				} catch (Exception f) {
 					return jsonError("Not a number: " + nr);
 				}
 			}
 		} else {
 			long val;
 			try {
 				val = Long.parseLong(nr);
 			} catch (Exception e) {
 				try {
 					return new BigInteger(nr);
 				} catch (Exception f) {
 					return jsonError("Not a number: " + nr);
 				}
 			}
 			
 			if (((int)val) == val) return Integer.valueOf((int)val);
 			else return Long.valueOf(val);
 		}
 	}
 	
 	private List<?> parseList() {
 		List<Object> list = new ArrayList<Object>();
 		
 		boolean commaNeeded = false;
 		while (pos < len) {
 			char c = string.charAt(pos++);
 			if (Character.isWhitespace(c)) continue;
 			else if (c == ']') return list;
 			else if (c == ',') {
 				if (commaNeeded) commaNeeded = false;
 				else return jsonError("Comma not expected here");
 			} else {
 				pos--;
 				if (commaNeeded) return jsonError("Comma expected here");
 				else {
 					list.add(parseObject());
 					commaNeeded = true;
 				}
 			}
 		}
 		
 		return jsonError("Array not closed");
 	}
 	
 	private Map<String, ?> parseMap() {
 		Map<String, Object> map = new LinkedHashMap<String, Object>();
 		
 		boolean commaNeeded = false;
 		boolean colonNeeded = false;
 		String key = null;
 		while (pos < len) {
 			char c = string.charAt(pos++);
 			if (Character.isWhitespace(c)) continue;
 			if (colonNeeded && c != ':') return jsonError("Colon expected here");
 			
 			if (c == '}') {
 				if (key != null) return jsonError("value expected here");
 				else return map;
 			}
 			
 			if (commaNeeded && c != ',') return jsonError("Comma expected here");
 			
 			if (c == ',') commaNeeded = false;
 			else if (c == ':') colonNeeded = false;
 			else {
 				if (key == null) {
 					if (c != '"') {
 						pos--;
 						key = parseMapKey();
 					} else key = parseString();
 					if (map.containsKey(key))
 						return jsonError("JSON object contains key " + key + " twice");
 					colonNeeded = true;
 				} else {
 					pos--;
 					map.put(key, parseObject());
 					key = null;
 					commaNeeded = true;
 				}
 			}
 		}
 		
 		return jsonError("Array not closed");
 	}
 	
 	private final String SAFE_MAP_KEY_CHARS =
 		"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-$";
 	
 	private String parseMapKey() {
 		StringBuilder out = new StringBuilder();
 		
 		int state = 0;
 		
 		while (pos < len) {
 			char c = string.charAt(pos++);
 			if ((state == 0 || state == 2) && Character.isWhitespace(c)) continue;
 			if (state == 0) state = 1;
 			if (state == 1 && Character.isWhitespace(c)) {
 				state = 2;
 				continue;
 			}
 			
 			if ((state == 1 || state == 2) && c == ':') {
 				pos--;
 				return out.toString();
 			}
 			
 			if (state == 2) return jsonError("Colon expected after map key.");
 			if (SAFE_MAP_KEY_CHARS.indexOf(c) > -1) out.append(c);
 			else return jsonError("Unexpected symbol in map key: " + c);
 		}
 		
 		return jsonError("Expected colon instead of end-of-stream.");
 	}
 	
 	private String parseString() {
 		StringBuilder out = new StringBuilder();
 		
 		while (pos < len) {
 			char c = string.charAt(pos++);
 			if (c == '\\') {
 				if (pos == len) return jsonError("Dangling string escape");
 				char d = string.charAt(pos++);
 				switch (d) {
 				case '"':
 				case '\\':
 				case '/':
 					out.append(d); break;
 				case 'b': out.append('\b'); break;
 				case 'f': out.append('\f'); break;
 				case 'n': out.append('\n'); break;
 				case 'r': out.append('\r'); break;
 				case 't': out.append('\t'); break;
 				case 'u':
 					if (pos + 4 > len) return jsonError("Dangling hex string escape");
 					char g = 0;
 					for (int i = 0; i < 4; i++) {
 						g <<= 4;
 						int hexDigit = readHexDigit(string.charAt(pos++));
 						g |= (hexDigit & 0x0F);
 					}
 					out.append(g); break;
 				default: return jsonError("Unknown json string escape: \\" + d);
 				}
 			} else if (c == '"') {
 				return out.toString();
 			} else out.append(c);
 		}
 		
 		return jsonError("String not closed");
 	}
 	
 	private static final String HEX_DIGITS = "0123456789ABCDEFabcdef";
 	private int readHexDigit(char c) {
 		int idx = HEX_DIGITS.indexOf(c);
 		if (idx == -1) {
 			jsonError("Not a hex digit: " + c);
 			return -1;
 		}
 		else if (idx > 15) return idx - 6;
 		else return idx;
 	}
 }
