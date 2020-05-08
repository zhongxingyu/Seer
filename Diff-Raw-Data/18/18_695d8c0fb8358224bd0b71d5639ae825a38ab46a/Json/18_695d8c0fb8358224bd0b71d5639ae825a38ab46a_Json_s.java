 /*
  * Copyright (c) 2012, someone All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1.Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2.Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3.Neither the name of the Happyelements Ltd. nor the
  * names of its contributors may be used to endorse or promote products derived
  * from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.github.zero.json;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * parse json
  * 
  * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
  * @since 1.0
  */
 public class Json {
 
 	private int tracker;
 	private String raw;
 	private JsonMap map;
 	private Exception exception;
 
 	/**
 	 * utility map for toString()
 	 * 
 	 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 	 * @version 1.0
 	 * @since 1.0 2011-5-1
 	 */
 	public static class JsonMap extends HashMap<String, Object> {
 		private static final long serialVersionUID = 3262225980784044459L;
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public String toString() {
 			StringBuilder builder = new StringBuilder("{");
 			for (Map.Entry<String, Object> entry : this.entrySet()) {
 				builder.append('"').append(entry.getKey()).append("\":");
 				Object value = entry.getValue();
 
 				if (value == null) {
 					builder.append("null,");
 				} else if (value instanceof Object[]) {
 					builder.append('[');
 					for (Object object : (Object[]) value) {
 						if (object instanceof String) {
 							builder.append('"').append(object).append("\",");
 						} else {
 							builder.append(object).append(',');
 						}
 					}
 
 					if (builder.charAt(builder.length() - 1) == ',') {
 						builder.deleteCharAt(builder.length() - 1);
 					}
 
 					builder.append("],");
 				} else if (value instanceof Number || value instanceof Boolean
 						|| value.getClass().isPrimitive()) {
 					builder.append(value).append(',');
 				} else if (value instanceof String) {
 					builder.append('"').append(value).append("\",");
 				} else {
 					builder.append(value.toString()).append(',');
 				}
 			}
 
 			if (builder.charAt(builder.length() - 1) == ',') {
 				builder.deleteCharAt(builder.length() - 1);
 			}
 
 			return builder.append("}").toString();
 		}
 	}
 
 	/**
 	 * JSON parse execption
 	 * 
 	 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 	 * @since 1.0
 	 * 
 	 */
 	public static class JsonParseException extends Exception {
 		private static final long serialVersionUID = -8711342992518822721L;
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public JsonParseException() {
 			super();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public JsonParseException(String message) {
 			super(message);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public JsonParseException(String message, Throwable cause) {
 			super(message, cause);
 		}
 	}
 
 	/**
 	 * it always return null unless there is any pares failure
 	 * 
 	 * @return the failure exception
 	 */
 	public Exception tellFailCause() {
 		return this.exception;
 	}
 
 	/**
 	 * parse a JSON string to map.
 	 * 
 	 * @param raw
 	 *            the JSON string
 	 * @return the ValueSelector the can select JSON values
 	 * @throws JsonParseException
 	 *             throw when parser could not parse raw
 	 */
 	public static Json parse(String raw) {
 		return new Json(raw);
 	}
 
 	/**
 	 * select a value from the parsed JSON
 	 * 
 	 * @param <Type>
 	 *            the type
 	 * @param path
 	 *            the path
 	 * @param type
 	 *            the type
 	 * @return the object if exist
 	 */
 	@SuppressWarnings("unchecked")
 	protected <Type> Type select(String paths, Class<Type> type) {
 		if (paths != null) {
 			// split the path
 			int start = 0;
 			int end = start;
 			Map<String, Object> current = this.map;
 			while (end < paths.length()) {
 				if (paths.charAt(end) != '.') {
 					// not key break
 					if (end == paths.length() - 1) {
 						// the end
 						Object object = current.get(paths.substring(start,
 								end + 1));
 						if (object != null
 								&& type.isAssignableFrom(object.getClass())) {
 							return (Type) object;
 						} else {
 							return null;
 						}
 					} else {
 						// move next
 						end++;
 						continue;
 					}
 				} else {
 					// potential key break
 					Object object = current.get(paths.substring(start, end));
 					if (end != paths.length() - 1) {
 						// not the end , ok
 						if (object instanceof Map) {
 							current = (Map<String, Object>) object;
 							start = ++end;
 							continue;
 						} else {
 							// should be map
 							return null;
 						}
 					} else {
 						// illegal
 						return null;
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * select a Object array from the parse result.
 	 * the only accept object type is String,Number,Boolean,JsonMap,null.
 	 * @param path
 	 *            the path expression
 	 * @return the array
 	 */
 	public Object[] selectArray(String path) {
 		return this.select(path, Object[].class);
 	}
 
 	/**
 	 * select a Number from the parse result.
 	 * 
 	 * @param path
 	 *            the path expression
 	 * @return the Number
 	 */
 	public Number selectNumber(String path) {
 		return this.select(path, Number.class);
 	}
 
 	/**
 	 * select a Map object from the parse result.
 	 * 
 	 * @param path
 	 *            the path expression
 	 * @return the Map object
 	 */
 	public JsonMap selectObject(String path) {
 		return this.select(path, JsonMap.class);
 	}
 
 	/**
 	 * select a Map object from the parse result.
 	 * 
 	 * @param path
 	 *            the path expression
 	 * @return the Map object
 	 */
 	public Boolean selectBoolean(String path) {
 		return this.select(path, Boolean.class);
 	}
 
 	/**
 	 * select a String from the parse result.
 	 * 
 	 * @param path
 	 *            the path expression
 	 * @return the String
 	 */
 	public String selectString(String path) {
 		return this.select(path, String.class);
 	}
 
 	/**
 	 * get the root object(the whole json)
 	 * @return
 	 * 		the JsonMap represent the object
 	 */
 	public JsonMap selectRoot() {
 		return this.map;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String toString() {
 		return this.map.toString();
 	}
 
 	/**
 	 * constructor,
 	 * 
 	 * @param raw
 	 * @throws JsonParseException
 	 */
 	private Json(String raw) {
 		this.tracker = 0;
 		if (raw != null) {
 			this.raw = raw;
 		} else {
 			this.raw = "";
 		}
 
 		// do real parse
 		this.doParse();
 
 		// help GC?
 		this.raw = null;
 	}
 
 	/**
 	 * do the parse job
 	 * 
 	 * @return the parsed object
 	 * @throws JsonParseException
 	 *             throw when fail to parse
 	 */
 	private void doParse() {
 		JsonMap result = null;
 		if (this.skip() && this.raw.charAt(tracker) == '{') {
 			this.tracker++;
 			result = this.parseObject();
 		} else {
 			this.exception = new JsonParseException(
 					"expected { or [ , but got " + raw.charAt(this.tracker)
 							+ " at position:" + this.tracker + " the raw json:"
 							+ this.raw);
 		}
 
 		// almost done
 		// pass through the left char to check JSON syntax
 		// mainly for tailing 'spaces'
 		if (exception == null) {
 			skip();
 			if (tracker == raw.length()) {
 				// clear exceptions
 				this.exception = null;
 				this.map = result;
 				return;
 			}
 		}
 
 		this.exception = new JsonParseException(
 				"unexpected parse error. unrecognise end of character:"
 						+ raw.charAt(this.tracker) + " at position:"
 						+ this.tracker + " the raw json:" + this.raw);
 		this.map = new JsonMap();
 	}
 
 	/**
 	 * parse JSON array
 	 * 
 	 * @return the array parsed
 	 * @throws JsonParseException
 	 *             throw when fail to parse
 	 */
 	private Object[] parseArray() {
 		List<Object> result = new ArrayList<Object>();
 
 		final int length = raw.length();
 		while (this.tracker < length) {
 			if (this.skip()) {
 				switch (this.raw.charAt(this.tracker)) {
 				case ']':
 					this.tracker++;
 					return result.toArray();
 				case ',':
 					this.tracker++;
 					continue;
 				default:
 					Object parsed = this.parseValue();
 					if (this.exception == null) {
 						result.add(parsed);
 						continue;
 					} else {
 						return null;
 					}
 				}
 			} else {
 				// EOF
 				break;
 			}
 		}
 
 		this.exception = new JsonParseException(
 				"parsing array fail at position :" + this.tracker
 						+ " the raw json:" + this.raw);
 		return null;
 	}
 
 	/**
 	 * parse JSON value part
 	 * 
 	 * @return the value part of JSON string
 	 * @throws JsonParseException
 	 *             throw when fail to parse
 	 */
 	private Object parseValue() {
 		if (this.skip()) {
 			switch (this.raw.charAt(this.tracker)) {
 			case '"':
 				this.tracker++;
 				return this.parseString();
 			case '[':
 				this.tracker++;
 				return this.parseArray();
 			case '{':
 				this.tracker++;
 				return this.parseObject();
 			default:
 				return this.parsePrimitive();
 			}
 		} else {
 			this.exception = new JsonParseException("fail to parse value at:"
 					+ this.tracker + " for raw:" + this.raw);
 			return null;
 		}
 	}
 
 	/**
 	 * parse value null
 	 * 
 	 * @return null if parse done
 	 * @throws JsonParseException
 	 *             throw when fail to parse
 	 */
 	private Object parseValueNull() {
 		if ((this.raw.charAt(tracker) == 'n' || this.raw.charAt(tracker) == 'N') //
 				&& (this.raw.charAt(++tracker) == 'u' || this.raw
 						.charAt(tracker) == 'U') //
 				&& (this.raw.charAt(++tracker) == 'l' || this.raw
 						.charAt(tracker) == 'L') //
 				&& (this.raw.charAt(++tracker) == 'l' || this.raw
 						.charAt(tracker) == 'L') //
 		) {
 			tracker++;
 			return null;
 		} else {
 			this.exception = new JsonParseException(
 					"parsing value:null fail at position " + this.tracker
 							+ " the raw json:" + this.raw);
 			return null;
 		}
 	}
 
 	/**
 	 * parse value true
 	 * 
 	 * @return ture if parse done
 	 * @throws JsonParseException
 	 *             throw when fail to parse
 	 */
 	private Boolean parseValueTrue() {
 		if ((this.raw.charAt(tracker) == 't' || this.raw.charAt(tracker) == 'T') //
 				&& (this.raw.charAt(++tracker) == 'r' || this.raw
 						.charAt(tracker) == 'R') //
 				&& (this.raw.charAt(++tracker) == 'u' || this.raw
 						.charAt(tracker) == 'U') //
 				&& (this.raw.charAt(++tracker) == 'e' || this.raw
 						.charAt(tracker) == 'E') //
 		) {
 			tracker++;
 			return true;
 		} else {
 			this.exception = new JsonParseException(
 					"parsing value:true fail at position " + this.tracker
 							+ " the raw json:" + new String(this.raw));
 			return null;
 		}
 	}
 
 	/**
 	 * parse value false
 	 * 
 	 * @return false if parse done
 	 * @throws JsonParseException
 	 *             throw when fail to parse
 	 */
 	private Boolean parseValueFalse() {
 		if ((raw.charAt(tracker) == 'f' || raw.charAt(tracker) == 'F') //
 				&& (raw.charAt(++tracker) == 'a' || raw.charAt(tracker) == 'A')//
 				&& (raw.charAt(++tracker) == 'l' || raw.charAt(tracker) == 'L')//
 				&& (raw.charAt(++tracker) == 's' || raw.charAt(tracker) == 'S')//
 				&& (raw.charAt(++tracker) == 'e' || raw.charAt(tracker) == 'E')//
 		) {
 			tracker++;
 			return false;
 		} else {
 			this.exception = new JsonParseException(
 					"parsing value:false fail at position " + this.tracker
 							+ " the raw json:" + new String(this.raw));
 			return null;
 		}
 	}
 
 	/**
 	 * parse numbers
 	 * 
 	 * @return the parsed number
 	 * @throws JsonParseException
 	 *             throw when fail to parse
 	 */
 	private Number parseValueNumber() {
 		long integral = 0;
 		long decimal = 0;
 		int precise = 0;
 		long e_integral = 0;
 		boolean negative = false;
 		boolean e_negative = false;
 		NumberPhase phase = NumberPhase.Sign;
 
 		final int length = raw.length();
 		while (tracker < length) {
 			switch (raw.charAt(tracker)) {
 			case '-':
 				switch (phase) {
 				case Sign:
 					negative = true;
 					phase = NumberPhase.Integral;
 					tracker++;
 					continue;
 				case E_Sign:
 					e_negative = true;
 					phase = NumberPhase.E_Integral;
 					tracker++;
 					continue;
 				default:
 					this.exception = new JsonParseException(
 							"fail to parse number , encounter unexpetet '-' at position"
 									+ this.tracker + " raw value is:"
 									+ new String(this.raw));
 					return null;
 				}
 			case '0':
 			case '1':
 			case '2':
 			case '3':
 			case '4':
 			case '5':
 			case '6':
 			case '7':
 			case '8':
 			case '9':
 				switch (phase) {
 				case Sign:
 					phase = NumberPhase.Integral;
 				case Integral:
 					integral = integral * 10 + (raw.charAt(tracker) - '0');
 					tracker++;
 					continue;
 				case Decimal:
 					precise++;
 					decimal = decimal * 10 + (raw.charAt(tracker) - '0');
 					tracker++;
 					continue;
 				case E_Sign:
 					phase = NumberPhase.E_Integral;
 				case E_Integral:
 					e_integral = e_integral * 10 + (raw.charAt(tracker) - '0');
 					tracker++;
 					continue;
 				default:
 					exception = new JsonParseException(
 							"unexpectd number , should not digit when parsing a E number at position:"
 									+ this.tracker + " raw:" + this.raw);
 					return null;
 				}
 			case '.':
 				if (phase == NumberPhase.Integral) {
 					phase = NumberPhase.Decimal;
 					tracker++;
 					continue;
 				} else {
 					this.exception = new JsonParseException(
 							"unexpected . when parsing number, fail at position:"
 									+ this.tracker + " for raw:" + this.raw);
 					return null;
 				}
 			case 'e':
 			case 'E':
 				if (phase == NumberPhase.Integral
 						|| phase == NumberPhase.Decimal) {
 					phase = NumberPhase.E_Sign;
 					tracker++;
 					continue;
 				} else {
 					this.exception = new JsonParseException(
 							"unxepected E ,when parsing for number at position:"
 									+ this.tracker + " raw:" + this.raw);
 					return null;
 				}
 			case '+':
 				if (phase == NumberPhase.E_Sign) {
 					tracker++;
 					continue;
 				} else {
 					this.exception = new JsonParseException(
 							"unexpected + when parsing number at position:"
 									+ this.tracker + " raw:" + this.raw);
 					return null;
 				}
 			}
 			break;
 		}
 
 		if (tracker >= length) {
 			exception = new JsonParseException(
 					"unexpected eof when parsing number end at position "
 							+ tracker + " raw:" + this.raw);
 		} else {
 			double result = integral;
 
 			// process decimal part
 			if (precise != 0) {
 				double decimal_part = 0.0;
 				while (precise > 0) {
 					if (decimal > 0) {
 						// add to decimal
 						decimal_part = decimal_part / 10 + (decimal % 10) * 0.1;
 						// shift
 						decimal /= 10;
 					} else {
 						decimal_part = decimal_part / 10;
 					}
 					precise--;
 				}
 
 				result += decimal_part;
 			}
 
 			// process e part
 			if (e_integral != 0) {
 				long multipy = 10;
 				if (e_negative) {
 					multipy = -10;
 				}
 
 				while (e_integral-- > 0) {
 					result *= multipy;
 				}
 			}
 
 			// process sign
 			if (negative) {
 				result = -result;
 			}
 
 			return result;
 		}
 
 		return null;
 	}
 
 	/**
 	 * internal phase for number parsing
 	 * 
 	 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 	 * @version 1.0
 	 * @since 1.0 2011-5-1
 	 */
 	private static enum NumberPhase {
 		Sign, Integral, Decimal, E_Sign, E_Integral;
 	}
 
 	/**
 	 * parse value , of true, false, numbers and nulls
 	 * 
 	 * @return the value parsed
 	 * @throws JsonParseException
 	 *             throw when fail to parse
 	 */
 	private Object parsePrimitive() {
 		switch (this.raw.charAt(this.tracker)) {
 		case 'N':
 		case 'n':
 			return this.parseValueNull();
 		case 'T':
 		case 't':
 			return this.parseValueTrue();
 		case 'F':
 		case 'f':
 			return this.parseValueFalse();
 		default:
 			return this.parseValueNumber();
 		}
 	}
 
 	/**
 	 * parse string
 	 * 
 	 * @return the parse string
 	 */
 	private String parseString() {
 		int begin = this.tracker;
 
 		// loop until meet '"'
 		final int length = this.raw.length();
 		while (this.tracker < length) {
			if (this.raw.charAt(this.tracker) == '"'
					&& this.raw.charAt(this.tracker - 1) != '\\') {
 				return this.raw.substring(begin, tracker++);
 			} else {
 				this.tracker++;
 			}
 		}
 
 		this.exception = new JsonParseException(
 				"parsing string fail at position " + this.tracker
 						+ " the raw json:" + new String(this.raw));
 		return null;
 	}
 
 	/**
 	 * parse JSON object like {...}
 	 * 
 	 * @return a map with json key.value
 	 */
 	private JsonMap parseObject() {
 		JsonMap result = new JsonMap();
 
 		final int length = this.raw.length();
 		while (this.tracker < length) {
 			if (this.skip()) {
 				switch (this.raw.charAt(this.tracker)) {
 				case '"':
 					this.tracker++;
 					String key = this.parseString();
 					if (this.skip() && this.raw.charAt(this.tracker++) == ':'
 							&& this.skip()) {
 						result.put(key, this.parseValue());
 						continue;
 					} else {
 						this.exception = new JsonParseException(
 								"parsing fail , expected colon at position "
 										+ this.tracker + " the raw json:"
 										+ this.raw);
 						return null;
 					}
 				case ',':
 					this.tracker++;
 					continue;
 				case '}':
 					this.tracker++;
 					return result;
 				default:
 					this.exception = new JsonParseException(
 							"fail when parsing object , unexpected char at position "
 									+ this.tracker + " the raw json:"
 									+ this.raw);
 					return null;
 				}
 			}
 		}
 
 		this.exception = new JsonParseException("unexpected eof");
 		return null;
 	}
 
 	/**
 	 * advance by skip space
 	 */
 	protected boolean skip() {
 		final int length = this.raw.length();
 		while (this.tracker < length) {
 			switch (this.raw.charAt(this.tracker)) {
 			case '\t':
 			case '\n':
 			case '\r':
 			case ' ':
 				this.tracker++;
 				break;
 			default:
 				return true;
 			}
 		}
 		exception = new JsonParseException("unexcected EOF while skiping space");
 		return false;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) {
 		String test = "\r\n\r\n\r{'ret':'0','msg':'ok','t':1,'content':[{'72000009316417':'7200100301,000000000000000000000000039B5EF8,1000000015,9BD6738A98F8D75723C2B8EC12022A94746E11ADE068ED75'},{'72000017020373':'7200100301,0000000000000000000000000B765F28,1000000015,D7CEC92B09A118FE1E5C83E47DAC3F9BA76878A82233E17B'},{'72000010187934':'7200100301,0000000000000000000000000680933D,1000000015,34CD0B85851B01065246F17FA0D6B5CD53F38011AE7CFF84'},{'72000001220700':'7200100301,000000000000000000000000045CAF52,1000000015,9B0BAA52CA46755D7673BAF6254B19D223C3BEDE30B42740'},{'72000001674056':'7200100301,000000000000000000000000023D6EA0,1000000015,C986025DF37268239D0D46B51ABA162D8BB3D5FEB3E9A041'},{'72000003469305':'7200100301,0000000000000000000000000122CA48,1000000015,A037AD767F8B75EC94C72CE6B4FA8AEF42DDC4DFFFAEFC95'},{'72000000234227':'7200100301,00000000000000000000000004BAD047,1000000015,7165854B79341BB11FC203793FEDF21B1BA9EA9F97C1656A'},{'72000012432588':'7200100301,00000000000000000000000002550650,1000000015,DA522A977E3C10D0F52454D6D560E4718D321F1DC32A6AF2'},{'72000024932903':'7200100301,00000000000000000000000001038077,1000000015,FB4B85E5AF0F84EADFFFD90A5DE8E726AD9672AE22428A88'},{'72000018851627':'7200100301,00000000000000000000000003EE01AD,1000000015,DB241F92B960099E7FD455445C3F28864A2BF7BF9A801FEF'},{'72000025240687':'7200100301,000000000000000000000000019CB686,1000000015,83F98A09075549F0EE1275F556D339DBAF8632512F161BFA'},{'72000001328887':'7200100301,00000000000000000000000004171B95,1000000015,F31140F655E8230761F8C5685D122CB6695A4648C1F1F512'},{'72000002076572':'7200100301,00000000000000000000000004617C9F,1000000015,FCB1F6C73EEBE3A826469AEEC46454A06E2A6D2ED498782D'},{'72000025368521':'7200100301,000000000000000000000000025ED44F,1000000015,5D9401C76F589FDD2B3CC8555354C7DA0E87F976FABB69D0'},{'72000001149573':'7200100301,00000000000000000000000004CBAA73,1000000015,1622F305903CF349F3317088DC3AA11098566B539335E74A'},{'72000016160862':'7200100301,000000000000000000000000003DE43B,1000000015,C05B2CB9844BF6EDDBC2E3FB11978ED95BD92C8205A60ABF'},{'72000004180309':'7200100301,00000000000000000000000000606A4D,1000000015,52C53BB0B9EE93BF2C1A72C0E43EE8FD32D36214D8C3D292'},{'72000000799925':'7200100301,00000000000000000000000000385F9C,1000000015,DD36B99601CF7185A73C576D161B6CCA670E09617487CD7F'},{'72000025432870':'7200100301,00000000000000000000000010647996,1000000015,4CAA506C396AA259BAF5CA065BC7C18985E8B70099D55D72'},{'72000014613589':'7200100301,0000000000000000000000000037A361,1000000015,EB7A57935A2134749F00217A5068A0F2111E5BACB4195B2C'},{'72000000228818':'7200100301,00000000000000000000000000B202EB,1000000015,51E79241AF124B9111C3C581708483D1D811792950BD86AB'},{'72000025174139':'7200100301,000000000000000000000000039998EE,1000000015,CA689C40BA92B6FD3B6783A7B7899AB84E6107A49FE76446'},{'72000018282989':'7200100301,0000000000000000000000000D348475,1000000015,8B0F91B789037D66FC1886636503CEB907405A8408DD72A8'},{'72000025069542':'7200100301,000000000000000000000000071F1186,1000000015,71902E73291C36F0D56D3E5B5455EE336DFD68D6AC239CC0'},{'72000001326713':'7200100301,000000000000000000000000007223E5,1000000015,884BC3E8D17365D1A6A5580A6BDE658EE534FE6D10E9B651'},{'72000008513988':'7200100301,000000000000000000000000002620A2,1000000015,846B6FA54EEA775A975F9B69659218B74F21388A81E8A509'},{'72000004481422':'7200100301,000000000000000000000000003942F2,1000000015,6D20FC8C3CEC7AD9F79343DB56B7D357F3E38BE361B1A0DE'},{'72000005358740':'7200100301,00000000000000000000000004B6143C,1000000015,B3774791992942D182628A85E053395DF7E6862A7F1D59FD'},{'72000008481483':'7200100301,00000000000000000000000005583A2B,1000000015,C36A72B2B7A3C36129DC51E12CE2C42A945F019E7EE11223'},{'72000017708377':'7200100301,000000000000000000000000077C7B02,1000000015,DB64B1500600F24D53AA57D6FF6F888FA2CEEFDEB9576F08'},{'72000025477691':'7200100301,00000000000000000000000006505B2D,1000000015,9E6DD67C3472B45AB7CB11F6589CC291C317CA2A1740D5DA'},{'72000025477814':'7200100301,00000000000000000000000011EAFDCD,1000000015,FEEACC35635319CF73C6C106BBB657202A1C5157CFFC57A8'},{'72000015682459':'7200100301,000000000000000000000000075FBAEE,1000000015,851357195811D7688EE73998122EA0A4870109FB061AD01B'},{'72000016915206':'7200100301,0000000000000000000000000440A852,1000000015,6D4F33BCDCCB32CA8107F4010D17697FEEB65FEA6EF1C1DF'},{'72000002962154':'7200100301,00000000000000000000000004E5144C,1000000015,8E48FCFD9A02C27836C66F07D9B9E5E6DE1AA15BB8E355DB'},{'72000000695325':'7200100301,0000000000000000000000000090BACD,1000000015,1E7A32373F63BCCD86CECB55FEDC60DF1B6525A62BDAE1CE'},{'72000013911329':'7200100301,000000000000000000000000079F2F85,1000000015,BB8CD2EC953B79A0DF1DBBA69043C0EEC16B9C3A0C9FF2BE'},{'72000025097617':'7200100301,000000000000000000000000077721F3,1000000015,8221FCC4C6B5940B2EEA429B42A77521D44B85CF8F184B44'},{'72000024965632':'7200100301,0000000000000000000000000548FF87,1000000015,4EE118114468440DB42BFAFA31AD485B86E0A0CDDDB68343'},{'72000010759383':'7200100301,0000000000000000000000000305D2BA,1000000015,838C8B7C29953DCCE720C661937BFCA280F57529E6956AD1'},{'72000025069306':'7200100301,00000000000000000000000003AF2A63,1000000015,F0DEC878CD731A9D06EBB235BC00B8444FB8290A208C912C'},{'72000025079513':'7200100301,00000000000000000000000009CA8367,1000000015,BB35411DB5E126FDA2C10C0F5A3F534D5C1DD38E0ADAF49D'},{'72000025170247':'7200100301,000000000000000000000000063EF1F3,1000000015,7727FBD52348C09B55C541C63D17F60D0AAFE41CB851010E'},{'72000002471484':'7200100301,00000000000000000000000000C9C717,1000000015,B3C59B96BE028CC8A2A2BB0CF59BF54DB1E556D5DE25E777'},{'72000025355071':'7200100301,00000000000000000000000011D603A9,1000000015,1582E8503C93F6675DDD9273A3555AD540D1491FEACACA1F'},{'72000001727941':'7200100301,0000000000000000000000000049FB6D,1000000015,31E69273388D78C5BD127E8749A64DC239CBB6562CC52426'},{'72000017933434':'7200100301,00000000000000000000000003EAE59B,1000000015,899AC4445B404F86ACED898C396AB70A4D729B65C4E07196'},{'72000022076407':'7200100301,0000000000000000000000000150F258,1000000015,D3571189D451F65394B51F3E52A5AF45F026344F317FAC4C'},{'72000021728200':'7200100301,00000000000000000000000000337FB3,1000000015,3192AFF71696A35C677CBC62D63C4984EF476E17B2955DDC'},{'72000007039053':'7200100301,00000000000000000000000003401646,1000000015,5C9EF666BC2A8F42044B193E9DDA4590AFD77940FB5A359C'},{'72000005137558':'7200100301,00000000000000000000000000D867B9,1000000015,1579710CA1257271FED85AA7095044FBE647952939AD0FC1'},{'72000024984172':'7200100301,0000000000000000000000000FD32DA1,1000000015,B589A080033D4BC4E7C62D20CC06C902E8B4D6FEE378A10F'},{'72000025477645':'7200100301,00000000000000000000000005C2B9B2,1000000015,D0E4F776E26B8F84918C196DEE32AE7E70703C8129EC875D'},{'72000025390440':'7200100301,00000000000000000000000000BE8ED0,1000000015,A619FBEC62829584AAFFD27F0636691C1ED931F835F76F04'},{'72000018833078':'7200100301,000000000000000000000000008B647F,1000000015,5BD5CE4BABEBE4DC48DEC4EA4CAB55CED23B1BAAAF9D120B'},{'72000025401329':'7200100301,00000000000000000000000005D77C36,1000000015,6E887C4D22990B8D763304C34F3E3A5C0A51ABD2CE55A96D'},{'72000025360747':'7200100301,00000000000000000000000011413B95,1000000015,096D0850CDFFD3B296F525D1D3DB1BDB46FE82964C49B3DF'},{'72000025357822':'7200100301,00000000000000000000000000B6D04C,1000000015,2C91E5AF93ECA5C8D1F45F82575092CC892479A546D37EF8'},{'72000025322969':'7200100301,000000000000000000000000013C5E46,1000000015,4EC948D34BBA61298C035B232722B6CC480C2F572626FB6A'},{'72000025301183':'7200100301,000000000000000000000000037FC7B2,1000000015,82C8D4DAA3DF94F099BE1E490C5259DA040F18EA94BC9B04'},{'72000004411622':'7200100301,0000000000000000000000000098F2A1,1000000015,59C15B0AA6BD2F210630BA86ED51E8467B1B95F54CE96D24'},{'72000025477691':'7200100301,00000000000000000000000006505B2D,1000000015,9E6DD67C3472B45AB7CB11F6589CC291C317CA2A1740D5DA'},{'72000012382454':'7200100301,000000000000000000000000019C943B,1000000015,880F0C3FC113A04596ADC09220D238489418E0354EE9D6B1'},{'72000025134566':'7200100301,00000000000000000000000003B30F5B,1000000015,3B008AD2F7CB309143CE219FAD4A80ADBCD4033065CF0F0E'},{'72000021042065':'7200100301,00000000000000000000000004164061,1000000015,E15A0DE9AEE57E3C03BE84C29D199B5EF4EE28269D382228'},{'72000021072773':'7200100301,00000000000000000000000007A64DFD,1000000015,C4DB539A09B89656447465466F41E2A91FD73B173EB11C71'},{'72000008902320':'7200100301,000000000000000000000000029C4CE5,1000000015,1FFA9F7B79464AA6C6A428609531132F7D0C72322047C5F0'},{'72000024982596':'7200100301,000000000000000000000000002A3ACB,1000000015,4EA77144F6E07C0F5E384979D822C613BE1C1EA15BFC9399'},{'72000004041453':'7200100301,000000000000000000000000022FCB72,1000000015,99D5878E481102324D350034FD9EB84F29AFBDC25B72B542'},{'72000005041074':'7200100301,000000000000000000000000038BB0E3,1000000015,D8D1236FEECDFEFAF9FC05ABDEFF017D4681A8325631FF3D'},{'72000025075697':'7200100301,0000000000000000000000000258EE99,1000000015,F18C2248A52770C37236582FA4E59756B34625ECC5B07493'},{'72000007955403':'7200100301,00000000000000000000000004A7B86D,1000000015,375147FA3D8824A10CDB632D98EF327264EF1CF4BA2A71B1'},{'72000022242272':'7200100301,00000000000000000000000004DBFD6F,1000000015,06FD97C04EAB6A6B5F6D8364BBA4072E7B7615866C517CC4'},{'72000014608280':'7200100301,0000000000000000000000000927879F,1000000015,F36060D4583B5BC110185BFD61E2C8CB108FF09BA276A606'},{'72000018546693':'7200100301,00000000000000000000000002CB5812,1000000015,F16D143B7ECE65D5B4576C57F4DCFC04843D98AAFE26AB8D'},{'72000003837172':'7200100301,00000000000000000000000006BFB128,1000000015,9E48A3B835E6A96B3C8C54A35CB34D6E4E46FB54F48458F3'},{'72000025316415':'7200100301,00000000000000000000000005B980E9,1000000015,6D1AA2FDA4BE4231055C4EF90362AE554084A1D64385E21D'},{'72000025400057':'7200100301,0000000000000000000000000136252D,1000000015,87B78B54E12FB3D8591B2AAE9B5F7A90B5E507765A1B76A0'},{'72000004933384':'7200100301,00000000000000000000000004058B2A,1000000015,1067C71C0DD2F9CBC8B23A93CB3E9D6F6E467D635F88CDC6'},{'72000025113632':'7200100301,0000000000000000000000000106225C,1000000015,F98DE3F095EBE52E1535333FE5BEFE1E5F07B4B0012C578E'}]} \r\n"
 				.replace("'", "\"");
 		Json json = Json.parse(test);
 		System.out.println("parse done");
 		System.out.println(json.selectString("ret").equals("0"));
 		String[] splits = ((Map<String, Object>) json.selectArray("content")[0])
 				.get("72000009316417").toString().split(",");
 		System.out.println(splits[0]);
 		for (String string : ((Map<String, Object>) json.selectArray("content")[0])
 				.get("72000009316417").toString().split(",")) {
 			System.out.println(string);
 		}
 		System.out.println(json.selectNumber("t").intValue() == 1);
 		System.out.println(json.selectString("ret"));
 		System.out.println(json.selectRoot());
 		System.out.println();
 	}
 }
