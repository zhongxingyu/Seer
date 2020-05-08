 /*
  * Copyright 2007-2011 Hidekatsu Izuno
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package net.arnx.jsonic;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Serializable;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Member;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.sql.Struct;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.MessageFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.RandomAccess;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TimeZone;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.regex.Pattern;
 
 import net.arnx.jsonic.io.AppendableOutputSource;
 import net.arnx.jsonic.io.CharSequenceInputSource;
 import net.arnx.jsonic.io.InputSource;
 import net.arnx.jsonic.io.OutputSource;
 import net.arnx.jsonic.io.ReaderInputSource;
 import net.arnx.jsonic.io.StringBuilderOutputSource;
 import net.arnx.jsonic.io.WriterOutputSource;
 import net.arnx.jsonic.util.BeanInfo;
 import net.arnx.jsonic.util.ClassUtil;
 import net.arnx.jsonic.util.ExtendedDateFormat;
 import net.arnx.jsonic.util.PropertyInfo;
 
 import org.w3c.dom.CharacterData;
 import org.w3c.dom.Comment;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * <p>The JSONIC JSON class provides JSON encoding and decoding as 
  * defined by RFC 4627.</p>
  * 
  * <p>The following example illustrates how to encode and decode. The code:</p>
  * <pre>
  * // encodes a object into a json string.
  * String s = JSON.encode(o);
  * 
  * // decodes a json string into a object.
  * Object o = JSON.decode(s);
  * 
  * // decodes a json string into a typed object.
  * Foo foo = JSON.decode(s, Foo.class);
  * </pre>
  * 
  * <p>Advanced topic:</p>
  * <pre>
  * // formats a object into a json string with indents for debug.
  * JSON json = new JSON();
  * json.setPrettyPrint(true);
  * String pretty = json.format(o);
  * 
  * //uses Reader/InputStream
  * Bar bar = JSON.decode(new FileInputStream("bar.json"), Bar.class);
  * Bar bar = JSON.decode(new FileReader("bar.json"), Bar.class);
  * </pre>
  * 
  * <h4>Summary of encoding rules for java type into json type</h4>
  * <table border="1" cellpadding="1" cellspacing="0">
  * <tr>
  * 	<th bgcolor="#CCCCFF" align="left">java type</th>
  * 	<th bgcolor="#CCCCFF" align="left">json type</th>
  * </tr>
  * <tr><td>java.util.Map</td><td rowspan="2">object</td></tr>
  * <tr><td>java.lang.Object (public property or field)</td></tr>
  * <tr><td>java.lang.Object[]</td><td rowspan="3">array</td></tr>
  * <tr><td>java.util.Collection</td></tr>
  * <tr><td>boolean[], short[], int[], long[], float[], double[]</td></tr>
  * <tr><td>java.lang.CharSequence</td><td rowspan="10">string</td></tr>
  * <tr><td>char[]</td></tr>
  * <tr><td>java.lang.Character</td></tr>
  * <tr><td>char</td></tr>
  * <tr><td>java.util.TimeZone</td></tr>
  * <tr><td>java.util.regex.Pattern</td></tr>
  * <tr><td>java.lang.reflect.Type</td></tr>
  * <tr><td>java.lang.reflect.Member</td></tr>
  * <tr><td>java.net.URI</td></tr>
  * <tr><td>java.net.URL</td></tr>
  * <tr><td>byte[]</td><td>string (base64)</td></tr>
  * <tr><td>java.util.Locale</td><td>string (language-country)</td></tr>
  * <tr><td>java.lang.Number</td><td rowspan="2">number</td></tr>
  * <tr><td>byte, short, int, long, float, double</td></tr>
  * <tr><td>java.util.Date</td><td rowspan="2">number (milliseconds since 1970)</td></tr>
  * <tr><td>java.util.Calendar</td></tr>
  * <tr><td>java.lang.Boolean</td><td rowspan="2">true/false</td></tr>
  * <tr><td>boolean</td></tr>
  * <tr><td>null</td><td>null</td></tr>
  * </table>
  * 
  * <h4>Summary of decoding rules for json type into java type</h4>
  * <table border="1" cellpadding="1" cellspacing="0">
  * <tr>
  * 	<th bgcolor="#CCCCFF" align="left">json type</th>
  * 	<th bgcolor="#CCCCFF" align="left">java type</th>
  * </tr>
  * <tr><td>object</td><td>java.util.LinkedHashMap</td></tr>
  * <tr><td>array</td><td>java.util.ArrayList</td></tr>
  * <tr><td>string</td><td>java.lang.String</td></tr>
  * <tr><td>number</td><td>java.math.BigDecimal</td></tr>
  * <tr><td>true/false</td><td>java.lang.Boolean</td></tr>
  * <tr><td>null</td><td>null</td></tr>
  * </table>
  * 
  * @author Hidekatsu Izuno
  * @version 1.2.8
  * @see <a href="http://www.rfc-editor.org/rfc/rfc4627.txt">RFC 4627</a>
  * @see <a href="http://www.apache.org/licenses/LICENSE-2.0">the Apache License, Version 2.0</a>
  */
 public class JSON {
 	/**
 	 * JSON processing mode
 	 */
 	public enum Mode {
 		/**
 		 * Traditional Mode
 		 */
 		TRADITIONAL,
 		
 		/**
 		 * Strict Mode
 		 */
 		STRICT,
 		
 		/**
 		 * Script(=JavaScript) Mode
 		 */
 		SCRIPT
 	}
 	
 	/**
 	 * Setup your custom class for using static method. default: net.arnx.jsonic.JSON
 	 */
 	public static volatile Class<? extends JSON> prototype = JSON.class;
 	
 	private static final Map<Class<?>, Formatter> FORMAT_MAP = new HashMap<Class<?>, Formatter>(50);
 	private static final Map<Class<?>, Converter> CONVERT_MAP = new HashMap<Class<?>, Converter>(50);
 	
 	static {
 		FORMAT_MAP.put(boolean.class, PlainFormatter.INSTANCE);
 		FORMAT_MAP.put(char.class, StringFormatter.INSTANCE);
 		FORMAT_MAP.put(byte.class, ByteFormatter.INSTANCE);
 		FORMAT_MAP.put(short.class, NumberFormatter.INSTANCE);
 		FORMAT_MAP.put(int.class, NumberFormatter.INSTANCE);
 		FORMAT_MAP.put(long.class, NumberFormatter.INSTANCE);
 		FORMAT_MAP.put(float.class, FloatFormatter.INSTANCE);
 		FORMAT_MAP.put(double.class, FloatFormatter.INSTANCE);
 		
 		FORMAT_MAP.put(boolean[].class, BooleanArrayFormatter.INSTANCE);
 		FORMAT_MAP.put(char[].class, CharArrayFormatter.INSTANCE);
 		FORMAT_MAP.put(byte[].class, ByteArrayFormatter.INSTANCE);
 		FORMAT_MAP.put(short[].class, ShortArrayFormatter.INSTANCE);
 		FORMAT_MAP.put(int[].class, IntArrayFormatter.INSTANCE);
 		FORMAT_MAP.put(long[].class, LongArrayFormatter.INSTANCE);
 		FORMAT_MAP.put(float[].class, FloatArrayFormatter.INSTANCE);
 		FORMAT_MAP.put(double[].class, DoubleArrayFormatter.INSTANCE);
 		FORMAT_MAP.put(Object[].class, ObjectArrayFormatter.INSTANCE);
 		
 		FORMAT_MAP.put(Boolean.class, PlainFormatter.INSTANCE);
 		FORMAT_MAP.put(Character.class, StringFormatter.INSTANCE);
 		FORMAT_MAP.put(Byte.class, ByteFormatter.INSTANCE);
 		FORMAT_MAP.put(Short.class, NumberFormatter.INSTANCE);
 		FORMAT_MAP.put(Integer.class, NumberFormatter.INSTANCE);
 		FORMAT_MAP.put(Long.class, NumberFormatter.INSTANCE);
 		FORMAT_MAP.put(Float.class, FloatFormatter.INSTANCE);
 		FORMAT_MAP.put(Double.class, FloatFormatter.INSTANCE);
 		
 		FORMAT_MAP.put(BigInteger.class, NumberFormatter.INSTANCE);
 		FORMAT_MAP.put(BigDecimal.class, NumberFormatter.INSTANCE);
 		FORMAT_MAP.put(String.class, StringFormatter.INSTANCE);
 		FORMAT_MAP.put(Date.class, DateFormatter.INSTANCE);
 		FORMAT_MAP.put(java.sql.Date.class, DateFormatter.INSTANCE);
 		FORMAT_MAP.put(java.sql.Time.class, DateFormatter.INSTANCE);
 		FORMAT_MAP.put(java.sql.Timestamp.class, DateFormatter.INSTANCE);
 		FORMAT_MAP.put(URI.class, StringFormatter.INSTANCE);
 		FORMAT_MAP.put(URL.class, StringFormatter.INSTANCE);
 		FORMAT_MAP.put(UUID.class, StringFormatter.INSTANCE);
 		FORMAT_MAP.put(Pattern.class, StringFormatter.INSTANCE);
 		FORMAT_MAP.put(Class.class, ClassFormatter.INSTANCE);
 		FORMAT_MAP.put(Locale.class, LocaleFormatter.INSTANCE);
 		
 		FORMAT_MAP.put(ArrayList.class, ListFormatter.INSTANCE);
 		FORMAT_MAP.put(LinkedList.class, IterableFormatter.INSTANCE);
 		FORMAT_MAP.put(HashSet.class, IterableFormatter.INSTANCE);
 		FORMAT_MAP.put(TreeSet.class, IterableFormatter.INSTANCE);
 		FORMAT_MAP.put(LinkedHashSet.class, IterableFormatter.INSTANCE);
 		
 		FORMAT_MAP.put(HashMap.class, MapFormatter.INSTANCE);
 		FORMAT_MAP.put(IdentityHashMap.class, MapFormatter.INSTANCE);
 		FORMAT_MAP.put(Properties.class, MapFormatter.INSTANCE);
 		FORMAT_MAP.put(TreeMap.class, MapFormatter.INSTANCE);
 		FORMAT_MAP.put(LinkedHashMap.class, MapFormatter.INSTANCE);
 		
 		CONVERT_MAP.put(boolean.class, BooleanConverter.INSTANCE);
 		CONVERT_MAP.put(char.class, CharacterConverter.INSTANCE);
 		CONVERT_MAP.put(byte.class, ByteConverter.INSTANCE);
 		CONVERT_MAP.put(short.class, ShortConverter.INSTANCE);
 		CONVERT_MAP.put(int.class, IntegerConverter.INSTANCE);
 		CONVERT_MAP.put(long.class, LongConverter.INSTANCE);
 		CONVERT_MAP.put(float.class, FloatConverter.INSTANCE);
 		CONVERT_MAP.put(double.class, DoubleConverter.INSTANCE);
 		
 		CONVERT_MAP.put(boolean[].class, ArrayConverter.INSTANCE);
 		CONVERT_MAP.put(char[].class, ArrayConverter.INSTANCE);
 		CONVERT_MAP.put(byte[].class, ArrayConverter.INSTANCE);
 		CONVERT_MAP.put(short[].class, ArrayConverter.INSTANCE);
 		CONVERT_MAP.put(int[].class, ArrayConverter.INSTANCE);
 		CONVERT_MAP.put(long[].class, ArrayConverter.INSTANCE);
 		CONVERT_MAP.put(float[].class, ArrayConverter.INSTANCE);
 		CONVERT_MAP.put(double[].class, ArrayConverter.INSTANCE);
 		CONVERT_MAP.put(Object[].class, ArrayConverter.INSTANCE);
 		
 		CONVERT_MAP.put(Boolean.class, BooleanConverter.INSTANCE);
 		CONVERT_MAP.put(Character.class, CharacterConverter.INSTANCE);
 		CONVERT_MAP.put(Byte.class, ByteConverter.INSTANCE);
 		CONVERT_MAP.put(Short.class, ShortConverter.INSTANCE);
 		CONVERT_MAP.put(Integer.class, IntegerConverter.INSTANCE);
 		CONVERT_MAP.put(Long.class, LongConverter.INSTANCE);
 		CONVERT_MAP.put(Float.class, FloatConverter.INSTANCE);
 		CONVERT_MAP.put(Double.class, DoubleConverter.INSTANCE);
 		
 		CONVERT_MAP.put(BigInteger.class, BigIntegerConverter.INSTANCE);
 		CONVERT_MAP.put(BigDecimal.class, BigDecimalConverter.INSTANCE);
 		CONVERT_MAP.put(Number.class, BigDecimalConverter.INSTANCE);
 		
 		CONVERT_MAP.put(Pattern.class, PatternConverter.INSTANCE);
 		CONVERT_MAP.put(TimeZone.class, TimeZoneConverter.INSTANCE);
 		CONVERT_MAP.put(Locale.class, LocaleConverter.INSTANCE);
 		CONVERT_MAP.put(File.class, FileConverter.INSTANCE);
 		CONVERT_MAP.put(URL.class, URLConverter.INSTANCE);
 		CONVERT_MAP.put(URI.class, URIConverter.INSTANCE);
 		CONVERT_MAP.put(UUID.class, UUIDConverter.INSTANCE);
 		CONVERT_MAP.put(Charset.class, CharsetConverter.INSTANCE);
 		CONVERT_MAP.put(Class.class, ClassConverter.INSTANCE);
 
 		CONVERT_MAP.put(Date.class, DateConverter.INSTANCE);
 		CONVERT_MAP.put(java.sql.Date.class, DateConverter.INSTANCE);
 		CONVERT_MAP.put(java.sql.Time.class, DateConverter.INSTANCE);
 		CONVERT_MAP.put(java.sql.Timestamp.class, DateConverter.INSTANCE);
 		CONVERT_MAP.put(Calendar.class, CalendarConverter.INSTANCE);
 		
 		CONVERT_MAP.put(Collection.class, CollectionConverter.INSTANCE);
 		CONVERT_MAP.put(Set.class, CollectionConverter.INSTANCE);
 		CONVERT_MAP.put(List.class, CollectionConverter.INSTANCE);
 		CONVERT_MAP.put(SortedSet.class, CollectionConverter.INSTANCE);
 		CONVERT_MAP.put(LinkedList.class, CollectionConverter.INSTANCE);
 		CONVERT_MAP.put(HashSet.class, CollectionConverter.INSTANCE);
 		CONVERT_MAP.put(TreeSet.class, CollectionConverter.INSTANCE);
 		CONVERT_MAP.put(LinkedHashSet.class, CollectionConverter.INSTANCE);
 		
 		CONVERT_MAP.put(Map.class, MapConverter.INSTANCE);
 		CONVERT_MAP.put(SortedMap.class, MapConverter.INSTANCE);
 		CONVERT_MAP.put(HashMap.class, MapConverter.INSTANCE);
 		CONVERT_MAP.put(IdentityHashMap.class, MapConverter.INSTANCE);
 		CONVERT_MAP.put(TreeMap.class, MapConverter.INSTANCE);
 		CONVERT_MAP.put(LinkedHashMap.class, MapConverter.INSTANCE);
 		CONVERT_MAP.put(Properties.class, PropertiesConverter.INSTANCE);
 	}
 	
 	static JSON newInstance() {
 		JSON instance = null;
 		try {
 			instance = prototype.newInstance();
 		} catch (Exception e) {
 			throw new IllegalStateException(e);
 		}
 		return instance;
 	}
 
 	/**
 	 * Encodes a object into a json string.
 	 * 
 	 * @param source a object to encode.
 	 * @return a json string
 	 * @throws JSONException if error occurred when formating.
 	 */
 	public static String encode(Object source) throws JSONException {
 		return encode(source, false);
 	}
 	
 	/**
 	 * Encodes a object into a json string.
 	 * 
 	 * @param source a object to encode.
 	 * @param prettyPrint output a json string with indent, space or break.
 	 * @return a json string
 	 * @throws JSONException if error occurred when formating.
 	 */
 	public static String encode(Object source, boolean prettyPrint) throws JSONException {		
 		JSON json = newInstance();
 		json.setPrettyPrint(prettyPrint);		
 		return json.format(source);
 	}
 
 	/**
 	 * Encodes a object into a json string.
 	 * 
 	 * @param source a object to encode.
 	 * @param out a destination to output a json string.
 	 * @throws IOException if I/O Error occurred.
 	 * @throws JSONException if error occurred when formating.
 	 */
 	public static void encode(Object source, OutputStream out) throws IOException, JSONException {
 		newInstance().format(source, new OutputStreamWriter(out, "UTF-8"));
 	}
 
 	/**
 	 * Encodes a object into a json string.
 	 * 
 	 * @param source a object to encode.
 	 * @param out a destination to output a json string.
 	 * @param prettyPrint output a json string with indent, space or break.
 	 * @throws IOException if I/O Error occurred.
 	 * @throws JSONException if error occurred when formating.
 	 */
 	public static void encode(Object source, OutputStream out, boolean prettyPrint) throws IOException, JSONException {
 		JSON json = newInstance();
 		json.setPrettyPrint(prettyPrint);		
 		json.format(source, new OutputStreamWriter(out, "UTF-8"));
 	}
 
 	/**
 	 * Encodes a object into a json string.
 	 * 
 	 * @param source a object to encode.
 	 * @param appendable a destination to output a json string.
 	 * @throws IOException if I/O Error occurred.
 	 * @throws JSONException if error occurred when formating.
 	 */
 	public static void encode(Object source, Appendable appendable) throws IOException, JSONException {
 		newInstance().format(source, appendable);
 	}
 
 	/**
 	 * Encodes a object into a json string.
 	 * 
 	 * @param source a object to encode.
 	 * @param appendable a destination to output a json string.
 	 * @param prettyPrint output a json string with indent, space or break.
 	 * @throws IOException if I/O Error occurred.
 	 * @throws JSONException if error occurred when formating.
 	 */
 	public static void encode(Object source, Appendable appendable, boolean prettyPrint) throws IOException, JSONException {
 		JSON json = newInstance();
 		json.setPrettyPrint(prettyPrint);		
 		json.format(source, appendable);
 	}
 	
 	/**
 	 * Escapes a object into JavaScript format.
 	 * 
 	 * @param source a object to encode.
 	 * @return a escaped object
 	 * @throws JSONException if error occurred when formating.
 	 */
 	public static String escapeScript(Object source) throws JSONException {
 		JSON json = newInstance();
 		json.setMode(Mode.SCRIPT);
 		return json.format(source);
 	}
 	
 	/**
 	 * Escapes a object into JavaScript format.
 	 * 
 	 * @param source a object to encode.
 	 * @param out a destination to output a json string.
 	 * @throws IOException if I/O Error occurred.
 	 * @throws JSONException if error occurred when formating.
 	 */
 	public static void escapeScript(Object source, OutputStream out) throws IOException, JSONException {
 		JSON json = newInstance();
 		json.setMode(Mode.SCRIPT);
 		json.format(source, out);
 	}
 	
 	/**
 	 * Escapes a object into JavaScript format.
 	 * 
 	 * @param source a object to encode.
 	 * @param appendable a destination to output a json string.
 	 * @throws IOException if I/O Error occurred.
 	 * @throws JSONException if error occurred when formating.
 	 */
 	public static void escapeScript(Object source, Appendable appendable) throws IOException, JSONException {
 		JSON json = newInstance();
 		json.setMode(Mode.SCRIPT);
 		json.format(source, appendable);
 	}
 	
 	/**
 	 * Decodes a json string into a object.
 	 * 
 	 * @param source a json string to decode
 	 * @return a decoded object
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T decode(String source) throws JSONException {
 		return (T)newInstance().parse(source);
 	}
 	
 	/**
 	 * Decodes a json string into a typed object.
 	 * 
 	 * @param source a json string to decode
 	 * @param cls class for converting
 	 * @return a decoded object
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	public static <T> T decode(String source, Class<? extends T> cls) throws JSONException {
 		return newInstance().parse(source, cls);
 	}
 	
 	/**
 	 * Decodes a json string into a typed object.
 	 * 
 	 * @param source a json string to decode
 	 * @param type type for converting
 	 * @return a decoded object
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T decode(String source, Type type) throws JSONException {
 		return (T)newInstance().parse(source, type);
 	}
 
 	/**
 	 * Decodes a json stream into a object. (character encoding should be Unicode)
 	 * 
 	 * @param in a json stream to decode
 	 * @return a decoded object
 	 * @throws IOException if I/O error occurred.
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T decode(InputStream in) throws IOException, JSONException {
 		return (T)newInstance().parse(in);
 	}
 
 	/**
 	 * Decodes a json stream into a object. (character encoding should be Unicode)
 	 * 
 	 * @param in a json stream to decode
 	 * @param cls class for converting
 	 * @return a decoded object
 	 * @throws IOException if I/O error occurred.
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	public static <T> T decode(InputStream in, Class<? extends T> cls) throws IOException, JSONException {
 		return newInstance().parse(in, cls);
 	}
 
 	/**
 	 * Decodes a json stream into a object. (character encoding should be Unicode)
 	 * 
 	 * @param in a json stream to decode
 	 * @param type type for converting
 	 * @return a decoded object
 	 * @throws IOException if I/O error occurred.
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T decode(InputStream in, Type type) throws IOException, JSONException {
 		return (T)newInstance().parse(in, type);
 	}
 	
 	/**
 	 * Decodes a json stream into a object.
 	 * 
 	 * @param reader a json stream to decode
 	 * @return a decoded object
 	 * @throws IOException if I/O error occurred.
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T decode(Reader reader) throws IOException, JSONException {
 		return (T)newInstance().parse(reader);
 	}
 
 	/**
 	 * Decodes a json stream into a object.
 	 * 
 	 * @param reader a json stream to decode
 	 * @param cls class for converting
 	 * @return a decoded object
 	 * @throws IOException if I/O error occurred.
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	public static <T> T decode(Reader reader, Class<? extends T> cls) throws IOException, JSONException {
 		return newInstance().parse(reader, cls);
 	}
 
 	/**
 	 * Decodes a json stream into a object.
 	 * 
 	 * @param reader a json stream to decode
 	 * @param type type for converting
 	 * @return a decoded object
 	 * @throws IOException if I/O error occurred.
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T decode(Reader reader, Type type) throws IOException, JSONException {
 		return (T)newInstance().parse(reader, type);
 	}
 	
 	/**
 	 * Validates a json text
 	 * 
 	 * @param cs source a json string to decode
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	public static void validate(CharSequence cs) throws JSONException {
 		JSON json = newInstance();
 		json.setMode(Mode.STRICT);
 		json.setMaxDepth(0);
 		json.parse(cs);
 	}
 	
 	/**
 	 * Validates a json stream
 	 * 
 	 * @param in source a json string to decode
 	 * @throws IOException if I/O error occurred.
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	public static void validate(InputStream in) throws IOException, JSONException {
 		JSON json = newInstance();
 		json.setMode(Mode.STRICT);
 		json.setMaxDepth(0);
 		json.parse(in);
 	}
 	
 	/**
 	 * Validates a json stream
 	 * 
 	 * @param reader source a json string to decode
 	 * @throws IOException if I/O error occurred.
 	 * @throws JSONException if error occurred when parsing.
 	 */
 	public static void validate(Reader reader) throws IOException, JSONException {
 		JSON json = newInstance();
 		json.setMode(Mode.STRICT);
 		json.setMaxDepth(0);
 		json.parse(reader);
 	}
 	
 	private Object contextObject;
 	private Locale locale = Locale.getDefault();
 	private TimeZone timeZone = TimeZone.getDefault();
 	private boolean prettyPrint = false;
 	private int maxDepth = 32;
 	private boolean suppressNull = false;
 	private Mode mode = Mode.TRADITIONAL;
 	private String dateFormat;
 	private String numberFormat;
 	private NamingStyle propertyStyle;
 	private NamingStyle enumStyle;
 	
 	public JSON() {
 	}
 	
 	public JSON(int maxDepth) {
 		setMaxDepth(maxDepth);
 	}
 	
 	public JSON(Mode mode) {
 		setMode(mode);
 	}
 	
 	/**
 	 * Sets context for inner class.
 	 * 
 	 * @param value context object
 	 */
 	public void setContext(Object value) {
 		this.contextObject = value;
 	}
 	
 	/**
 	 * Sets locale for formatting, converting and selecting message.
 	 * 
 	 * @param locale locale for formatting, converting and selecting message
 	 */
 	public void setLocale(Locale locale) {
 		if (locale == null) {
 			throw new NullPointerException();
 		}
 		this.locale = locale;
 	}
 	
 	/**
 	 * Sets timeZone for formatting and converting.
 	 * 
 	 * @param timeZone timeZone for formatting and converting.
 	 */
 	public void setTimeZone(TimeZone timeZone) {
 		if (timeZone == null) {
 			throw new NullPointerException();
 		}
 		this.timeZone = timeZone;
 	}
 	
 	/**
 	 * Output json string is to human-readable format.
 	 * 
 	 * @param value true to format human-readable, false to shorten.
 	 */
 	public void setPrettyPrint(boolean value) {
 		this.prettyPrint = value;
 	}
 	
 	/**
 	 * Sets maximum depth for the nest level.
 	 * default value is 32.
 	 * 
 	 * @param value maximum depth for the nest level.
 	 */
 	public void setMaxDepth(int value) {
 		if (value < 0) {
 			throw new IllegalArgumentException(getMessage("json.TooSmallArgumentError", "maxDepth", 0));
 		}
 		this.maxDepth = value;
 	}
 	
 	/**
 	 * Gets maximum depth for the nest level.
 	 * 
 	 * @return a maximum depth
 	 */
 	public int getMaxDepth() {
 		return this.maxDepth;
 	}
 	
 	/**
 	 * If this property is true, the member of null value in JSON object is ignored.
 	 * default value is false.
 	 * 
 	 * @param value true to ignore the member of null value in JSON object.
 	 */
 	public void setSuppressNull(boolean value) {
 		this.suppressNull = value;
 	}
 	
 	/**
 	 * Sets JSON interpreter mode.
 	 * 
 	 * @param mode JSON interpreter mode
 	 */
 	public void setMode(Mode mode) {
 		if (mode == null) {
 			throw new NullPointerException();
 		}
 		this.mode = mode;
 	}
 	
 	/**
 	 * Gets JSON interpreter mode.
 	 * 
 	 * @return JSON interpreter mode
 	 */
 	public Mode getMode() {
 		return mode;
 	}
 	
 	/**
 	 * Sets default Date format. 
 	 * When format is null, Date is formated to JSON number.  
 	 * 
 	 * @param format default Date format
 	 */
 	public void setDateFormat(String format) {
 		this.dateFormat = format;
 	}
 	
 	/**
 	 * Sets default Number format. 
 	 * When format is null, Number is formated to JSON number.  
 	 * 
 	 * @param format default Number format
 	 */
 	public void setNumberFormat(String format) {
 		this.numberFormat = format;
 	}
 	
 	/**
 	 * Sets default Case style for the property name of JSON object. 
 	 * 
 	 * @param style default Case style for keys of JSON object. 
 	 */
 	public void setPropertyStyle(NamingStyle style) {
 		this.propertyStyle = style;
 	}
 	
 	/**
 	 * Sets default Case style for Enum. 
 	 * 
 	 * @param style default Case style for Enum.
 	 */
 	public void setEnumStyle(NamingStyle style) {
 		this.enumStyle = style;
 	}
 	
 	/**
 	 * Format a object into a json string.
 	 * 
 	 * @param source a object to encode.
 	 * @return a json string
 	 */
 	public String format(Object source) {
 		String text = null;
 		try {
 			text = format(source, new StringBuilder(1000)).toString();
 		} catch (IOException e) {
 			// no handle;
 		}
 		return text;
 	}
 	
 	/**
 	 * Format a object into a json string.
 	 * 
 	 * @param source a object to encode.
 	 * @param out a destination to output a json string.
 	 * @return a reference to 'out' object in parameters
 	 */
 	public OutputStream format(Object source, OutputStream out) throws IOException {
 		format(source, new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
 		return out;
 	}
 	
 	/**
 	 * Format a object into a json string.
 	 * 
 	 * @param source a object to encode.
 	 * @param ap a destination. example: StringBuilder, Writer, ...
 	 * @return a json string
 	 */
 	public Appendable format(Object source, Appendable ap) throws IOException {
 		Context context = new Context();
 		
 		OutputSource fs;
 		if (ap instanceof Writer) {
 			fs = new WriterOutputSource((Writer)ap);
 		} else if (ap instanceof StringBuilder) {
 			fs = new StringBuilderOutputSource((StringBuilder)ap);
 		} else {
 			fs = new AppendableOutputSource(ap);
 		}
 		
 		context.enter('$');
 		source = preformatInternal(context, source);
 		formatInternal(context, source, fs);
 		context.exit();
 		fs.flush();
 		return ap;
 	}
 	
 	/**
 	 * Converts Any Java Object to JSON recognizable Java object before format.
 	 * 
 	 * @param context current context.
 	 * @param value source a object to format.
 	 * @return null or the instance of Map, Iterator(or Array, Enumerator), Number, CharSequence or Boolean.
 	 * @throws Exception if conversion failed.
 	 */
 	protected Object preformat(Context context, Object value) throws Exception {
 		return value;
 	}
 	
 	final Object preformatInternal(Context context, Object value) {
 		if (value == null) {
 			return null;
 		} else if (context.getLevel() > context.getMaxDepth()) {
 			return null;
 		} else if (getClass() != JSON.class) {
 			try {
 				return preformat(context, value);
 			} catch (Exception e) {
 				throw new JSONException(getMessage("json.format.ConversionError", value, context),
 					JSONException.PREFORMAT_ERROR, e);
 			}
 		}
 		return value;
 	}
 	
 	final Formatter formatInternal(final Context context, final Object src, final OutputSource ap) throws IOException {
 		Object o = src;
 		
 		Formatter f = null;
 		
 		if (o == null) {
 			f = NullFormatter.INSTANCE;
 		} else {
 			JSONHint hint = context.getHint();
 			if (hint == null) {
 				// no handle
 			} else if (context.skipHint) {
 				context.skipHint = false;
 			} else if (hint.serialized()) {
 				f = PlainFormatter.INSTANCE;
 			} else if (String.class.equals(hint.type())) {
 				f = StringFormatter.INSTANCE;
 			} else if (Serializable.class.equals(hint.type())) {
 				f = SerializableFormatter.INSTANCE;
 			}
 		}
 		
 		if (f == null) f = FORMAT_MAP.get(o.getClass());
 		
 		if (f == null) {
 			if (context.hasMemberCache(o.getClass())) {
 				f = ObjectFormatter.INSTANCE;
 			} else if (o instanceof Map<?, ?>) {
 				f = MapFormatter.INSTANCE;
 			} else if (o instanceof Iterable<?>) {
 				if (o instanceof RandomAccess && o instanceof List<?>) {
 					f = ListFormatter.INSTANCE;
 				} else {
 					f = IterableFormatter.INSTANCE;
 				}
 			} else if (o instanceof Object[]) {
 				f = ObjectArrayFormatter.INSTANCE;
 			} else if (o instanceof Enum<?>) {
 				f = EnumFormatter.INSTANCE;
 			} else if (o instanceof CharSequence) {
 				f = StringFormatter.INSTANCE;
 			} else if (o instanceof Date) {
 				f = DateFormatter.INSTANCE;
 			} else if (o instanceof Calendar) {
 				f = CalendarFormatter.INSTANCE;
 			} else if (o instanceof Number) {
 				f = NumberFormatter.INSTANCE;
 			} else if (o instanceof Iterator<?>) {
 				f = IteratorFormatter.INSTANCE;
 			} else if (o instanceof Enumeration) {
 				f = EnumerationFormatter.INSTANCE;
 			} else if (o instanceof Type || o instanceof Member || o instanceof File) {
 				f = StringFormatter.INSTANCE;
 			} else if (o instanceof TimeZone) {
 				f = TimeZoneFormatter.INSTANCE;
 			} else if (o instanceof Charset) {
 				f = CharsetFormatter.INSTANCE;
 			} else if (o instanceof java.sql.Array) {
 				f = SQLArrayFormatter.INSTANCE;
 			} else if (o instanceof Struct) {
 				f = StructFormmatter.INSTANCE;
 			} else if (o instanceof Node) {
 				if (o instanceof CharacterData && !(o instanceof Comment)) {
 					f = CharacterDataFormatter.INSTANCE;
 				} else if (o instanceof Document) {
 					f = DOMDocumentFormatter.INSTANCE;
 				} else if (o instanceof Element) {
 					f = DOMElementFormatter.INSTANCE;
 				}
 			} else if (isAssignableFrom(ClassUtil.findClass("java.sql.RowId"), o.getClass())) {
 				f = SerializableFormatter.INSTANCE;
 			} else if (isAssignableFrom(ClassUtil.findClass("java.net.InetAddress"), o.getClass())) {
 				f = InetAddressFormatter.INSTANCE;
 			} else if (isAssignableFrom(ClassUtil.findClass("org.apache.commons.beanutils.DynaBean"), o.getClass())) {
 				f = DynaBeanFormatter.INSTANCE;
 			} else {
 				f = ObjectFormatter.INSTANCE;
 			}
 		}
 		
 		boolean isStruct;
 		try {
 			isStruct = f.format(this, context, src, o, ap);
 		} catch (IOException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new JSONException(getMessage("json.format.ConversionError",
 				(src instanceof CharSequence) ? "\"" + src + "\"" : src, context),
 					JSONException.FORMAT_ERROR, e);
 		}
 		
 		if (!isStruct && context.getLevel() == 0 && context.getMode() != Mode.SCRIPT) {
 			throw new JSONException(getMessage("json.format.IllegalRootTypeError"), 
 					JSONException.FORMAT_ERROR);
 		}
 		
 		return f;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T> T parse(CharSequence cs) throws JSONException {
 		Object value = null;
 		try {
 			value = parseInternal(new Context(), new CharSequenceInputSource(cs));
 		} catch (IOException e) {
 			// never occur
 		}
 		return (T)value; 
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T> T parse(CharSequence s, Class<? extends T> cls) throws JSONException {
 		return (T)parse(s, (Type)cls);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T> T parse(CharSequence s, Type type) throws JSONException {
 		T value = null;
 		try {
 			Context context = new Context();
 			value = (T)convert(context, parseInternal(context, new CharSequenceInputSource(s)), type);
 		} catch (IOException e) {
 			// never occur
 		}
 		return value;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T> T parse(InputStream in) throws IOException, JSONException {
 		return (T)parseInternal(new Context(), new ReaderInputSource(in));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T> T parse(InputStream in, Class<? extends T> cls) throws IOException, JSONException {
 		return (T)parse(in, (Type)cls);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T> T parse(InputStream in, Type type) throws IOException, JSONException {
 		Context context = new Context();
 		return (T)convert(context, parseInternal(context, new ReaderInputSource(in)), type);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T> T parse(Reader reader) throws IOException, JSONException {
 		return (T)parseInternal(new Context(), new ReaderInputSource(reader));
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T> T parse(Reader reader, Class<? extends T> cls) throws IOException, JSONException {
 		return (T)parse(reader, (Type)cls);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T> T parse(Reader reader, Type type) throws IOException, JSONException {
 		Context context = new Context();
 		return (T)convert(context, parseInternal(context, new ReaderInputSource(reader)), type);
 	}
 	
 	private Object parseInternal(Context context, InputSource s) throws IOException, JSONException {
 		boolean isEmpty = true;
 		Object o = null;
 		
 		int n = -1;
 		while ((n = s.next()) != -1) {
 			char c = (char)n;
 			switch(c) {
 			case '\r':
 			case '\n':
 			case ' ':
 			case '\t':
 			case 0xFEFF: // BOM
 				continue;
 			case '{':
 				if (isEmpty) {
 					s.back();
 					o = parseObject(context, s, 1);
 					isEmpty = false;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case '[':
 				if (isEmpty) {
 					s.back();
 					o = parseArray(context, s, 1);
 					isEmpty = false;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case '/':
 			case '#':
 				if (context.getMode() == Mode.TRADITIONAL || (context.getMode() == Mode.SCRIPT && c == '/')) {
 					s.back();
 					skipComment(context, s);
 					continue;
 				}
 			case '\'':
 			case '"':
 				if (context.getMode() == Mode.SCRIPT) {
 					if (isEmpty) {
 						s.back();
 						o = parseString(context, s, 1);
 						isEmpty = false;
 					} else {
 						throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 					}
 					continue;
 				}
 			default:
 				if (context.getMode() == Mode.SCRIPT) {
 					if (isEmpty) {
 						s.back();
 						o = ((c == '-') || (c >= '0' && c <= '9')) ? parseNumber(context, s, 1) : parseLiteral(context, s, 1, false);
 						isEmpty = false;
 					} else {
 						throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 					}
 					continue;
 				}
 			}
 			
 			if (context.getMode() == Mode.TRADITIONAL && isEmpty) {
 				s.back();
 				o = parseObject(context, s, 1);
 				isEmpty = false;
 			} else {
 				throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 			}
 		}
 		
 		if (isEmpty) {
 			if (context.getMode() == Mode.TRADITIONAL) {
 				o = new LinkedHashMap<String, Object>();
 			} else {
 				throw createParseException(getMessage("json.parse.EmptyInputError"), s);
 			}
 		}
 		
 		return o;
 	}
 	
 	private Map<Object, Object> parseObject(Context context, InputSource s, int level) throws IOException, JSONException {
 		int point = 0; // 0 '{' 1 'key' 2 ':' 3 '\n'? 4 'value' 5 '\n'? 6 ',' ... '}' E
 		Map<Object, Object> map = (level <= context.getMaxDepth()) ? new LinkedHashMap<Object, Object>() : null;
 		Object key = null;
 		char start = '\0';
 		
 		int n = -1;
 		loop:while ((n = s.next()) != -1) {
 			char c = (char)n;
 			switch(c) {
 			case '\r':
 			case '\n':
 				if (context.getMode() == Mode.TRADITIONAL && point == 5) {
 					point = 6;
 				}
 				continue;
 			case ' ':
 			case '\t':
 			case 0xFEFF: // BOM
 				continue;
 			case '{':
 				if (point == 0) {
 					start = '{';
 					point = 1;
 				} else if (point == 2 || point == 3){
 					s.back();
 					Object value = parseObject(context, s, level+1);
 					if (level < context.getMaxDepth()) map.put(key, value);
 					point = 5;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case ':':
 				if (point == 2) {
 					point = 3;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case ',':
 				if (point == 5 || point == 6 || (context.getMode() == Mode.TRADITIONAL && point == 3)) {
 					if (point == 3 && level < context.getMaxDepth() && !context.isSuppressNull()) {
 						map.put(key, null);
 					}
 					point = 1;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case '}':
 				if (start == '{' && (point == 1 || point == 5 || point == 6 || (context.getMode() == Mode.TRADITIONAL && point == 3))) {
 					if (point == 3 && level < context.getMaxDepth() && !context.isSuppressNull()) {
 						map.put(key, null);
 					}
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				break loop;
 			case '[':
 				if (point == 3) {
 					s.back();
 					List<Object> value = parseArray(context, s, level+1);
 					if (level < context.getMaxDepth()) map.put(key, value);
 					point = 5;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case '\'':
 				if (context.getMode() == Mode.STRICT) {
 					break;
 				}
 			case '"':
 				if (point == 0) {
 					s.back();
 					point = 1;
 				} else if (point == 1 || point == 6) {
 					s.back();
 					key = parseString(context, s, level+1);
 					point = 2;
 				} else if (point == 3) {
 					s.back();
 					String value = parseString(context, s, level+1);
 					if (level < context.getMaxDepth()) map.put(key, value);
 					point = 5;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case '/':
 			case '#':
 				if (context.getMode() == Mode.TRADITIONAL || (context.getMode() == Mode.SCRIPT && c == '/')) {
 					s.back();
 					skipComment(context, s);
 					if (point == 5) {
 						point = 6;
 					}
 					continue;
 				}
 			}
 			
 			if (point == 0) {
 				s.back();
 				point = 1;
 			} else if (point == 1 || point == 6) {
 				s.back();
 				key = ((c == '-') || (c >= '0' && c <= '9')) ? parseNumber(context, s, level+1) : parseLiteral(context, s, level+1, context.getMode() != Mode.STRICT);
 				point = 2;
 			} else if (point == 3) {
 				s.back();
 				Object value = ((c == '-') || (c >= '0' && c <= '9')) ? parseNumber(context, s, level+1) : parseLiteral(context, s, level+1, context.getMode() == Mode.TRADITIONAL);
 				if (level < context.getMaxDepth() && (value != null || !context.isSuppressNull())) {
 					map.put(key, value);
 				}
 				point = 5;
 			} else {
 				throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 			}
 		}
 		
 		if (n == -1) {
 			if (point == 3 || point == 4) {
 				if (level < context.getMaxDepth() && !context.isSuppressNull()) map.put(key, null);
 			} else if (point == 2) {
 				throw createParseException(getMessage("json.parse.ObjectNotClosedError"), s);
 			}
 		}
 		
 		if ((n == -1) ? (start != '\0') : (n != '}')) {
 			throw createParseException(getMessage("json.parse.ObjectNotClosedError"), s);
 		}
 		return map;
 	}
 	
 	private List<Object> parseArray(Context context, InputSource s, int level) throws IOException, JSONException {
 		int point = 0; // 0 '[' 1 'value' 2 '\n'? 3 ',' 4  ... ']' E
 		List<Object> list = (level <= context.getMaxDepth()) ? new ArrayList<Object>() : null;
 		
 		int n = -1;
 		loop:while ((n = s.next()) != -1) {
 			char c = (char)n;
 			switch(c) {
 			case '\r':
 			case '\n':
 				if (context.getMode() == Mode.TRADITIONAL && point == 2) {
 					point = 3;
 				}
 				continue;
 			case ' ':
 			case '\t':
 			case 0xFEFF: // BOM
 				continue;
 			case '[':
 				if (point == 0) {
 					point = 1;
 				} else if (point == 1 || point == 3 || point == 4) {
 					s.back();
 					List<Object> value = parseArray(context, s, level+1);
 					if (level < context.getMaxDepth()) list.add(value);
 					point = 2;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case ',':
 				if (context.getMode() == Mode.TRADITIONAL && (point == 1 || point == 4)) {
 					if (level < context.getMaxDepth()) list.add(null);
 				} else if (point == 2 || point == 3) {
 					point = 4;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case ']':
 				if (point == 1 || point == 2 || point == 3) {
 					// nothing
 				} else if (context.getMode() == Mode.TRADITIONAL && point == 4) {
 					if (level < context.getMaxDepth()) list.add(null);
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				break loop;	
 			case '{':
 				if (point == 1 || point == 3 || point == 4){
 					s.back();
 					Map<Object, Object> value = parseObject(context, s, level+1);
 					if (level < context.getMaxDepth()) list.add(value);
 					point = 2;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case '\'':
 				if (context.getMode() == Mode.STRICT) {
 					break;
 				}
 			case '"':
 				if (point == 1 || point == 3 || point == 4) {
 					s.back();
 					String value = parseString(context, s, level+1);
 					if (level < context.getMaxDepth()) list.add(value);
 					point = 2;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case '/':
 			case '#':
 				if (context.getMode() == Mode.TRADITIONAL || (context.getMode() == Mode.SCRIPT && c == '/')) {
 					s.back();
 					skipComment(context, s);
 					if (point == 2) {
 						point = 3;
 					}
 					continue;
 				}
 			}
 			
 			if (point == 1 || point == 3 || point == 4) {
 				s.back();
 				Object value = ((c == '-') || (c >= '0' && c <= '9')) ? parseNumber(context, s, level+1) : parseLiteral(context, s, level+1, context.getMode() == Mode.TRADITIONAL);
 				if (level < context.getMaxDepth()) list.add(value);
 				point = 2;
 			} else {
 				throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 			}
 		}
 		
 		if (n != ']') {
 			throw createParseException(getMessage("json.parse.ArrayNotClosedError"), s);
 		}
 		return list;
 	}
 	
 	private String parseString(Context context, InputSource s, int level) throws IOException, JSONException {
 		int point = 0; // 0 '"|'' 1 'c' ... '"|'' E
 		StringBuilder sb = (level <= context.getMaxDepth()) ? context.getCachedBuffer() : null;
 		char start = '\0';
 		
 		int n = -1;
 		loop:while ((n = s.next()) != -1) {
 			char c = (char)n;
 			switch(c) {
 			case 0xFEFF: // BOM
 				continue;
 			case '\\':
 				if (point == 1) {
 					if (context.getMode() != Mode.TRADITIONAL || start == '"') {
 						s.back();
 						c = parseEscape(s);
 						if (sb != null) sb.append(c);
 					} else {
 						if (sb != null) sb.append(c);
 					}
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			case '\'':
 				if (context.getMode() == Mode.STRICT) {
 					break;
 				}
 			case '"':
 				if (point == 0) {
 					start = c;
 					point = 1;
 					continue;
 				} else if (point == 1) {
 					if (start == c) {
 						break loop;						
 					} else {
 						if (sb != null) sb.append(c);
 					}
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				continue;
 			}
 			
 			if (point == 1 && (context.getMode() != Mode.STRICT  || c >= 0x20)) {
 				if (sb != null) sb.append(c);
 				continue;
 			}
 			throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 		}
 		
 		if (n != start) {
 			throw createParseException(getMessage("json.parse.StringNotClosedError"), s);
 		}
 		return (sb != null) ? sb.toString() : null;
 	}
 	
 	private Object parseLiteral(Context context, InputSource s, int level, boolean any) throws IOException, JSONException {
 		int point = 0; // 0 'IdStart' 1 'IdPart' ... !'IdPart' E
 		StringBuilder sb = context.getCachedBuffer();
 
 		int n = -1;
 		loop:while ((n = s.next()) != -1) {
 			char c = (char)n;
 			if (c == 0xFEFF) continue;
 			
 			if (c == '\\') {
 				s.back();
 				c = parseEscape(s);
 			}
 			
 			if (point == 0 && Character.isJavaIdentifierStart(c)) {
 				sb.append(c);
 				point = 1;
 			} else if (point == 1 && (Character.isJavaIdentifierPart(c) || c == '.')) {
 				sb.append(c);
 			} else {
 				s.back();
 				break loop;
 			}
 		}
 		
 		String str = sb.toString();
 		
 		if ("null".equals(str)) return null;
 		if ("true".equals(str)) return true;
 		if ("false".equals(str)) return false;
 		
 		if (!any) {
 			throw createParseException(getMessage("json.parse.UnrecognizedLiteral", str), s);
 		}
 		
 		return str;
 	}	
 	
 	private Number parseNumber(Context context, InputSource s, int level) throws IOException, JSONException {
 		int point = 0; // 0 '(-)' 1 '0' | ('[1-9]' 2 '[0-9]*') 3 '(.)' 4 '[0-9]' 5 '[0-9]*' 6 'e|E' 7 '[+|-]' 8 '[0-9]' 9 '[0-9]*' E
 		StringBuilder sb = (level <= context.getMaxDepth()) ? context.getCachedBuffer() : null;
 		
 		int n = -1;
 		loop:while ((n = s.next()) != -1) {
 			char c = (char)n;
 			switch(c) {
 			case 0xFEFF: // BOM
 				break;
 			case '+':
 				if (point == 7) {
 					if (sb != null) sb.append(c);
 					point = 8;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				break;
 			case '-':
 				if (point == 0) {
 					if (sb != null) sb.append(c);
 					point = 1;
 				} else if (point == 7) {
 					if (sb != null) sb.append(c);
 					point = 8;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				break;
 			case '.':
 				if (point == 2 || point == 3) {
 					if (sb != null) sb.append(c);
 					point = 4;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				break;
 			case 'e':
 			case 'E':
 				if (point == 2 || point == 3 || point == 5 || point == 6) {
 					if (sb != null) sb.append(c);
 					point = 7;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				break;
 			default:
 				if (c >= '0' && c <= '9') {
 					if (point == 0 || point == 1) {
 						if (sb != null) sb.append(c);
 						point = (c == '0') ? 3 : 2;
 					} else if (point == 2 || point == 5 || point == 9) {
 						if (sb != null) sb.append(c);
 					} else if (point == 4) {
 						if (sb != null) sb.append(c);
 						point = 5;
 					} else if (point == 7 || point == 8) {
 						if (sb != null) sb.append(c);
 						point = 9;
 					} else {
 						throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 					}
 				} else if (point == 2 || point == 3 || point == 5 || point == 6 || point == 9) {
 					s.back();
 					break loop;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 			}
 		}
 		
 		return (sb != null) ? new BigDecimal(sb.toString()) : null;
 	}
 	
 	private char parseEscape(InputSource s) throws IOException, JSONException {
 		int point = 0; // 0 '\' 1 'u' 2 'x' 3 'x' 4 'x' 5 'x' E
 		char escape = '\0';
 		
 		int n = -1;
 		loop:while ((n = s.next()) != -1) {
 			char c = (char)n;
 			if (c == 0xFEFF) continue; // BOM
 			
 			if (point == 0) {
 				if (c == '\\') {
 					point = 1;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 			} else if (point == 1) {
 				switch(c) {
 				case 'b':
 					escape = '\b';
 					break loop;
 				case 'f':
 					escape = '\f';
 					break loop;
 				case 'n':
 					escape = '\n';
 					break loop;
 				case 'r':
 					escape = '\r';
 					break loop;
 				case 't':
 					escape = '\t';
 					break loop;
 				case 'u':
 					point = 2;
 					break;
 				default:
 					escape = c;
 					break loop;
 				}
 			} else {
 				int hex = (c >= '0' && c <= '9') ? c-48 :
 					(c >= 'A' && c <= 'F') ? c-65+10 :
 					(c >= 'a' && c <= 'f') ? c-97+10 : -1;
 				if (hex != -1) {
 					escape |= (hex << ((5-point)*4));
 					if (point != 5) {
 						point++;
 					} else {
 						break loop;
 					}
 				} else {
 					throw createParseException(getMessage("json.parse.IllegalUnicodeEscape", c), s);
 				}
 			}
 		}
 		
 		return escape;
 	}
 	
 	private void skipComment(Context context, InputSource s) throws IOException, JSONException {
 		int point = 0; // 0 '/' 1 '*' 2  '*' 3 '/' E or  0 '/' 1 '/' 4  '\r|\n|\r\n' E
 		
 		int n = -1;
 		loop:while ((n = s.next()) != -1) {
 			char c = (char)n;
 			switch(c) {
 			case 0xFEFF:
 				break;
 			case '/':
 				if (point == 0) {
 					point = 1;
 				} else if (point == 1) {
 					point = 4;
 				} else if (point == 3) {
 					break loop;
 				} else if (!(point == 2 || point == 4)) {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				break;
 			case '*':
 				if (point == 1) {
 					point = 2;
 				} else if (point == 2) {
 					point = 3;
 				} else if (!(point == 3 || point == 4)) {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				break;
 			case '\n':
 			case '\r':
 				if (point == 2 || point == 3) {
 					point = 2;
 				} else if (point == 4) {
 					break loop;
 				} else {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 				break;
 			case '#':
 				if (context.getMode() == Mode.TRADITIONAL) {
 					if (point == 0) {
 						point = 4;
 					} else if (point == 3) {
 						point = 2;
 					} else if (!(point == 2 || point == 4)) {
 						throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 					}
 					break;
 				}
 			default:
 				if (point == 3) {
 					point = 2;
 				} else if (!(point == 2 || point == 4)) {
 					throw createParseException(getMessage("json.parse.UnexpectedChar", c), s);
 				}
 			}
 		}	
 	}
 	
 	JSONException createParseException(String message, InputSource s) {
 		return new JSONException("" + s.getLineNumber() + ": " + message + "\n" + s.toString() + " <- ?",
 				JSONException.PARSE_ERROR, s.getLineNumber(), s.getColumnNumber(), s.getOffset());
 	}
 	
 	String getMessage(String id, Object... args) {
 		ResourceBundle bundle = ResourceBundle.getBundle("net.arnx.jsonic.Messages", locale);
 		return MessageFormat.format(bundle.getString(id), args);
 	}
 	
 	public Object convert(Object value, Type type)  throws JSONException {
 		return convert(new Context(), value, type);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private <T> T convert(Context context, Object value, Type type) throws JSONException {
 		Class<?> cls = ClassUtil.getRawType(type);
 		
 		T result = null;
 		try {
 			context.enter('$');
 			result = (T)postparse(context, value, cls, type);
 			context.exit();
 		} catch (Exception e) {
 			String text;
 			if (value instanceof CharSequence) {
 				text = "\"" + value + "\"";
 			} else {
 				try {
 					text = value.toString();
 				} catch (Exception e2) {
 					text = value.getClass().toString();
 				}
 			}
 			throw new JSONException(getMessage("json.parse.ConversionError", text, type, context), 
 					JSONException.POSTPARSE_ERROR, e);
 		}
 		return result;
 	}
 		
 	/**
 	 * Converts Map, List, Number, String, Boolean or null to other Java Objects after parsing. 
 	 * 
 	 * @param context current context.
 	 * @param value null or the instance of Map, List, Number, String or Boolean.
 	 * @param cls class for converting
 	 * @param type generics type for converting. type equals to c if not generics.
 	 * @return a converted object
 	 * @throws Exception if conversion failed.
 	 */
 	@SuppressWarnings("unchecked")
 	protected <T> T postparse(Context context, Object value, Class<? extends T> cls, Type type) throws Exception {
 		Converter c = null;
 		
 		if (value == null) {
 			if (!cls.isPrimitive()) {
 				c = NullConverter.INSTANCE;
 			}
 		} else {
 			JSONHint hint = context.getHint();
 			if (hint == null) {
 				// no handle
 			} else if (context.skipHint) {
 				context.skipHint = false;
 			} else if (hint.serialized()) {
 				c = FormatConverter.INSTANCE;
 			} else if (Serializable.class.equals(hint.type())) {
 				c = SerializableConverter.INSTANCE;
 			} else if (String.class.equals(hint.type())) {
 				c = StringSerializableConverter.INSTANCE;
 			}
 		}
 		if (c == null) {
 			if (value != null && cls.equals(type) && cls.isAssignableFrom(value.getClass())) {
 				c = PlainConverter.INSTANCE;
 			} else {
 				c = CONVERT_MAP.get(cls);
 			}
 		}
 		if (c == null) {
 			if (context.hasMemberCache(cls)) {
 				c = ObjectConverter.INSTANCE;
 			} else if (Properties.class.isAssignableFrom(cls)) {
 				c = PropertiesConverter.INSTANCE;
 			} else if (Map.class.isAssignableFrom(cls)) {
 				c = MapConverter.INSTANCE;
 			} else if (Collection.class.isAssignableFrom(cls)) {
 				c = CollectionConverter.INSTANCE;
 			} else if (cls.isArray()) {
 				c = ArrayConverter.INSTANCE;
 			} else if (cls.isEnum()) {
 				c = EnumConverter.INSTANCE;
 			} else if (Date.class.isAssignableFrom(cls)) {
 				c = DateConverter.INSTANCE;
 			} else if (Calendar.class.isAssignableFrom(cls)) {
 				c = CalendarConverter.INSTANCE;
 			} else if (CharSequence.class.isAssignableFrom(cls)) {
 				c = CharSequenceConverter.INSTANCE;
 			} else if (Appendable.class.isAssignableFrom(cls)) {
 				c = AppendableConverter.INSTANCE;
 			} else if (cls.equals(ClassUtil.findClass("java.net.InetAddress"))) {
 				c = InetAddressConverter.INSTANCE;
 			} else if (java.sql.Array.class.isAssignableFrom(cls)
 					|| Struct.class.isAssignableFrom(cls)) {
 				c = NullConverter.INSTANCE;
 			} else {
 				c = ObjectConverter.INSTANCE;
 			}
 		}
 		
 		if (c != null) {
 			return (T)c.convert(this, context, value, cls, type);
 		} else {
 			throw new UnsupportedOperationException();
 		}
 	}
 	
 	protected String normalize(String name) {
 		return name;
 	}
 	
 	/**
 	 * Ignore this property. A default behavior is to ignore transient or declaring method in java.lang.Object.
 	 * You can override this method if you have to change default behavior.
 	 * 
 	 * @param context current context
 	 * @param target target class
 	 * @param member target member
 	 * @return true if this member must be ignored.
 	 */
 	protected boolean ignore(Context context, Class<?> target, Member member) {
 		if (Modifier.isTransient(member.getModifiers())) return true;
 		if (member.getDeclaringClass().equals(Object.class)) return true;
 		return false;
 	}
 	
 	protected <T> T create(Context context, Class<? extends T> c) throws Exception {
 		Object instance = null;
 		
 		JSONHint hint = context.getHint();
 		if (hint != null && hint.type() != Object.class) c = hint.type().asSubclass(c);
 		
 		if (c.isInterface()) {
 			if (SortedMap.class.equals(c)) {
 				instance = new TreeMap<Object, Object>();
 			} else if (Map.class.equals(c)) {
 				instance = new LinkedHashMap<Object, Object>();
 			} else if (SortedSet.class.equals(c)) {
 				instance = new TreeSet<Object>();
 			} else if (Set.class.equals(c)) {
 				instance = new LinkedHashSet<Object>();
 			} else if (List.class.equals(c)) {
 				instance = new ArrayList<Object>();
 			} else if (Collection.class.equals(c)) {
 				instance = new ArrayList<Object>();
 			} else if (Appendable.class.equals(c)) {
 				instance = new StringBuilder();
 			}
 		} else if (Modifier.isAbstract(c.getModifiers())) {
 			if (Calendar.class.equals(c)) {
 				instance = Calendar.getInstance();
 			}
 		} else if ((c.isMemberClass() || c.isAnonymousClass()) && !Modifier.isStatic(c.getModifiers())) {
 			Class<?> eClass = c.getEnclosingClass();
 			Constructor<?> con = c.getDeclaredConstructor(eClass);
 			con.setAccessible(true);
 			if (context.contextObject != null && eClass.isAssignableFrom(context.contextObject.getClass())) {
 				instance = con.newInstance(context.contextObject);
 			} else {
 				instance = con.newInstance((Object)null);
 			}
 		} else {
 			if (Date.class.isAssignableFrom(c)) {
 				try {
 					Constructor<?> con = c.getDeclaredConstructor(long.class);
 					con.setAccessible(true);
 					instance = con.newInstance(0l);
 				} catch (NoSuchMethodException e) {
 					// no handle
 				}
 			}
 			
 			if (instance == null) {
 				Constructor<?> con = c.getDeclaredConstructor();
 				con.setAccessible(true);
 				instance = con.newInstance();
 			}
 		}
 		
 		return c.cast(instance);
 	}
 	
 	private static boolean isAssignableFrom(Class<?> target, Class<?> cls) {
 		return (target != null) && target.isAssignableFrom(cls);
 	}
 	
 	public final class Context {
 		private final Locale locale;
 		private final TimeZone timeZone;
 		private final Object contextObject;
 		private final int maxDepth;
 		private final boolean prettyPrint;
 		private final boolean suppressNull;
 		private final Mode mode;
 		private final String numberFormat;
 		private final String dateFormat;
 		private final NamingStyle propertyStyle;
 		private final NamingStyle enumStyle;
 
 		private Object[] path;
 		private int level = -1;
 		private Map<Class<?>, Object> memberCache;
 		private Map<String, DateFormat> dateFormatCache;
 		private Map<String, NumberFormat> numberFormatCache;
 		private StringBuilder builderCache;
 		
 		boolean skipHint = false;
 		
 		public Context() {
 			synchronized (JSON.this) {
 				locale = JSON.this.locale;
 				timeZone = JSON.this.timeZone;
 				contextObject = JSON.this.contextObject;
 				maxDepth = JSON.this.maxDepth;
 				prettyPrint = JSON.this.prettyPrint;
 				suppressNull = JSON.this.suppressNull;
 				mode = JSON.this.mode;
 				numberFormat = JSON.this.numberFormat;
 				dateFormat = JSON.this.dateFormat;
 				propertyStyle = JSON.this.propertyStyle;
 				enumStyle = JSON.this.enumStyle;
 			}
 		}
 		
 		Context(Context context) {
 			synchronized (context) {
 				locale = context.locale;
 				timeZone = context.timeZone;
 				contextObject = context.contextObject;
 				maxDepth = context.maxDepth;
 				prettyPrint = context.prettyPrint;
 				suppressNull = context.suppressNull;
 				mode = context.mode;
 				numberFormat = context.numberFormat;
 				dateFormat = context.dateFormat;
 				propertyStyle = context.propertyStyle;
 				enumStyle = context.enumStyle;
 				level = context.level;
 				path = context.path.clone();
 			}
 		}
 		
 		public StringBuilder getCachedBuffer() {
 			if (builderCache == null) {
 				builderCache = new StringBuilder();
 			} else {
 				builderCache.setLength(0);
 			}
 			return builderCache;
 		}
 		
 		public Locale getLocale() {
 			return locale;
 		}
 		
 		public TimeZone getTimeZone() {
 			return timeZone;
 		}
 		
 		public int getMaxDepth() {
 			return maxDepth;
 		}
 		
 		public boolean isPrettyPrint() {
 			return prettyPrint;
 		}
 		
 		public boolean isSuppressNull() {
 			return suppressNull;
 		}
 		
 		public Mode getMode() {
 			return mode;
 		}
 		
 		public NamingStyle getPropertyCaseStyle() {
 			return propertyStyle;
 		}
 		
 		public NamingStyle getEnumCaseStyle() {
 			return enumStyle;
 		}
 		
 		/**
 		 * Returns the current level.
 		 * 
 		 * @return level number. 0 is root node.
 		 */
 		public int getLevel() {
 			return level;
 		}
 		
 		/**
 		 * Returns the current key object.
 		 * 
 		 * @return Root node is '$'. When the parent is a array, the key is Integer, otherwise String. 
 		 */
 		public Object getKey() {
 			return path[level*2];
 		}
 		
 		/**
 		 * Returns the key object in any level. the negative value means relative to current level.
 		 * 
 		 * @return Root node is '$'. When the parent is a array, the key is Integer, otherwise String. 
 		 */
 		public Object getKey(int level) {
 			if (level < 0) level = getLevel()+level;
 			return path[level*2];
 		}
 		
 		/**
 		 * Returns the current hint annotation.
 		 * 
 		 * @return the current annotation if present on this context, else null.
 		 */
 		public JSONHint getHint() {
 			return (JSONHint)path[level*2+1];
 		}
 		
 		@SuppressWarnings("unchecked")
 		public <T> T convert(Object key, Object value, Class<? extends T> c) throws Exception {
 			enter(key);
 			T o = JSON.this.postparse(this, value, c, c);
 			exit();
 			return (T)((c.isPrimitive()) ? PlainConverter.getDefaultValue(c).getClass() : c).cast(o);
 		}
 		
 		public Object convert(Object key, Object value, Type t) throws Exception {
 			Class<?> c = ClassUtil.getRawType(t);
 			enter(key);
 			Object o = JSON.this.postparse(this, value, c, t);
 			exit();
 			return ((c.isPrimitive()) ? PlainConverter.getDefaultValue(c).getClass() : c).cast(o);
 		}
 		
 		void enter(Object key, JSONHint hint) {
 			level++;
 			if (path == null) path = new Object[8];
 			if (path.length < level*2+2) {
 				Object[] newPath = new Object[Math.max(path.length*2, level*2+2)];
 				System.arraycopy(path, 0, newPath, 0, path.length);
 				path = newPath;
 			}
 			path[level*2] = key;
 			path[level*2+1] = hint;
 		}
 		
 		void enter(Object key) {
 			enter(key, (JSONHint)((level != -1) ? path[level*2+1] : null));
 		}
 		
 		void exit() {
 			level--;
 		}
 		
 		boolean hasMemberCache(Class<?> c) {
 			return memberCache != null && memberCache.containsKey(c);
 		}
 		
 		@SuppressWarnings("unchecked")
 		List<PropertyInfo> getGetProperties(Class<?> c) {
 			if (memberCache == null) memberCache = new HashMap<Class<?>, Object>();
 			
 			List<PropertyInfo> props = (List<PropertyInfo>)memberCache.get(c);
 			if (props == null) {
 				props = new ArrayList<PropertyInfo>();
 				for (PropertyInfo prop : BeanInfo.get(c).getProperties()) {
 					if (prop.isStatic() || !prop.isReadable()) continue;
 					
 					String mName = null;
 					if (prop.getReadMethod() != null && !ignore(this, c, prop.getReadMethod())) {
 						JSONHint hint = prop.getReadMethod().getAnnotation(JSONHint.class);
 						if (hint != null) {
 							if (!hint.ignore()) {
 								mName = prop.getName();
								if (!(hint.name() == null || hint.name().length() == 0)) { // if(!(hint.name().isEmpty()) {..}
 									mName = hint.name();
 								} else if (getPropertyCaseStyle() != null) {
 									mName = getPropertyCaseStyle().to(mName);
 								}
 								props.add(new PropertyInfo(prop.getBeanClass(), mName, 
 										null, prop.getReadMethod(), prop.getWriteMethod(), prop.isStatic(), hint.ordinal()));
 							}
 						} else if (getPropertyCaseStyle() != null) {
 							mName = getPropertyCaseStyle().to(prop.getName());
 							props.add(new PropertyInfo(prop.getBeanClass(), mName, 
 									prop.getField(), prop.getReadMethod(), prop.getWriteMethod(), prop.isStatic(), prop.getOrdinal()));
 						} else {
 							mName = prop.getName();
 							props.add(prop);
 						}
 					}
 					
 					if (prop.getField() != null && !ignore(this, c, prop.getField())) {
 						JSONHint hint = prop.getField().getAnnotation(JSONHint.class);
 						if (hint != null) {
 							String name = prop.getName();
							if (!(hint.name() == null || hint.name().length() == 0)) { // if(!(hint.name().isEmpty()) {..}
 								name = hint.name();
 							} else if (getPropertyCaseStyle() != null) {
 								name = getPropertyCaseStyle().to(name);
 							}
 							if (!name.equals(mName) && !hint.ignore()) {
 								props.add(new PropertyInfo(prop.getBeanClass(), name, 
 										prop.getField(), null, null, prop.isStatic(), hint.ordinal()));
 							}
 						} else if (mName != null) {
 							String name = prop.getName();
 							if (getPropertyCaseStyle() != null) {
 								name = getPropertyCaseStyle().to(name);
 							}
 							if (!name.equals(mName)) {
 								props.add(new PropertyInfo(prop.getBeanClass(), name, 
 										prop.getField(), null, null, prop.isStatic(), prop.getOrdinal()));
 							}
 						} else if (getPropertyCaseStyle() != null) {
 							props.add(new PropertyInfo(prop.getBeanClass(), getPropertyCaseStyle().to(prop.getName()), 
 									prop.getField(), prop.getReadMethod(), prop.getWriteMethod(), prop.isStatic(), prop.getOrdinal()));
 						} else {
 							props.add(prop);
 						}
 					}
 				}
 				Collections.sort(props);
 				memberCache.put(c, props);				
 			}
 			return props;
 		}
 		
 		@SuppressWarnings("unchecked")
 		Map<String, PropertyInfo> getSetProperties(Class<?> c) {
 			if (memberCache == null) memberCache = new HashMap<Class<?>, Object>();
 			
 			Map<String, PropertyInfo> props = (Map<String, PropertyInfo>)memberCache.get(c);
 			if (props == null) {
 				props = new HashMap<String, PropertyInfo>();
 				for (PropertyInfo prop : BeanInfo.get(c).getProperties()) {
 					if (prop.isStatic() || !prop.isWritable() || ignore(this, c, prop.getWriteMember())) {
 						continue;
 					}
 					
 					String name = null;
 					int order = -1;
 										
 					JSONHint hint = prop.getWriteAnnotation(JSONHint.class);
 					if (hint != null) {
 						if (hint.ignore()) continue;
 						if (hint.name().length() > 0) name = hint.name();
 						order = hint.ordinal();
 					}
 					
 					if (name != null) {
 						if (prop.getWriteMethod() != null && prop.getField() != null && !Modifier.isFinal(prop.getField().getModifiers())) {
 							props.put(name, new PropertyInfo(prop.getBeanClass(), name, 
 									null, prop.getReadMethod(), prop.getWriteMethod(), prop.isStatic(), order));
 							prop = new PropertyInfo(prop.getBeanClass(), prop.getName(), 
 									prop.getField(), null, null, prop.isStatic(), order);
 						} else {
 							props.put(name, new PropertyInfo(prop.getBeanClass(), name, 
 									prop.getField(), prop.getReadMethod(), prop.getWriteMethod(), prop.isStatic(), order));
 							continue;
 						}
 					} else if (order >= 0) {
 						prop = new PropertyInfo(prop.getBeanClass(), prop.getName(), 
 								prop.getField(), prop.getReadMethod(), prop.getWriteMethod(), prop.isStatic(), order);
 					}
 				
 					props.put(prop.getName(), prop);
 					if (getPropertyCaseStyle() != null) {
 						name = getPropertyCaseStyle().to(prop.getName());
 						if (!prop.getName().equals(name)) {
 							props.put(name, prop);
 						}
 					}
 				}
 				
 				memberCache.put(c, props);
 			}
 			return props;
 		}
 		
 		NumberFormat getNumberFormat() {
 			JSONHint hint = getHint();
 			String format = (hint != null && hint.format().length() > 0) ? hint.format() : numberFormat;			
 			if (format != null) {
 				NumberFormat nformat = null;
 				if (numberFormatCache == null) {
 					numberFormatCache = new HashMap<String, NumberFormat>();
 				} else {
 					nformat = numberFormatCache.get(format);
 				}
 				if (nformat == null) {
 					nformat = new DecimalFormat(format, new DecimalFormatSymbols(locale));
 					numberFormatCache.put(format, nformat);
 				}
 				return nformat;
 			} else {
 				return null;
 			}
 		}
 		
 		DateFormat getDateFormat() {
 			JSONHint hint = getHint();
 			String format = (hint != null && hint.format().length() > 0) ? hint.format() : dateFormat;			
 			if (format != null) {
 				DateFormat dformat = null;
 				if (dateFormatCache == null) {
 					dateFormatCache = new HashMap<String, DateFormat>();
 				} else {
 					dformat = dateFormatCache.get(format);
 				}
 				if (dformat == null) {
 					dformat = new ExtendedDateFormat(format, locale);
 					dformat.setTimeZone(timeZone);
 					dateFormatCache.put(format, dformat);
 				}
 				return dformat;
 			} else {
 				return null;
 			}
 		}
 		
 		public String toString() {
 			StringBuilderOutputSource sb = new StringBuilderOutputSource(getCachedBuffer());
 			for (int i = 0; i < path.length; i+=2) {
 				Object key = path[i];
 				if (key == null) {
 					sb.append("[null]");
 				} else if (key instanceof Number) {
 					sb.append('[');
 					sb.append(key.toString());
 					sb.append(']');
 				} else if (key instanceof Character) {
 					sb.append(key.toString());
 				} else {
 					String str = key.toString();
 					boolean escape = false;
 					for (int j = 0; j < str.length(); j++) {
 						if (j == 0) {
 							escape = !Character.isJavaIdentifierStart(str.charAt(j));
 						} else {
 							escape = !Character.isJavaIdentifierPart(str.charAt(j));
 						}
 						if (escape) break;
 					}
 					
 					if (escape) {
 						sb.append('[');
 						try {
 							StringFormatter.serialize(this, str, sb);
 						} catch (Exception e) {
 							// no handle
 						}
 						sb.append(']');
 					} else {
 						sb.append('.');
 						sb.append(str);
 					}
 				}
 			}
 			return sb.toString();
 		}
 	}
 }
