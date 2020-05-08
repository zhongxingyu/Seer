 /*
  * Copyright 2009 zaichu xiao
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package zcu.xutil;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TimeZone;
 
 import static zcu.xutil.Constants.*;
 
 /**
  * 工具类
  * 
  * @author <a href="mailto:zxiao@yeepay.com">xiao zaichu</a>
  */
 
 public class Objutil {
 	private static final Integer INT = Integer.valueOf(0);
 	private static final Long LONG = Long.valueOf(0);
 	private static final Double DOUBLE = Double.valueOf(0);
 	private static final Float FLOAT = Float.valueOf(0);
 	private static final Character CHAR = Character.valueOf('\u0000');
 	private static final Short SHORT = Short.valueOf((short) 0);
 	private static final Byte BYTE = Byte.valueOf((byte) 0);
 	private static final Reference nullRef = new WeakReference<Object>(null);
 	private static final int TZoffset = TimeZone.getDefault().getRawOffset();
 
 	public static void validate(boolean expression, Object message) {
 		if (expression == false)
 			throw new IllegalStateException(toString(message));
 	}
 
 	public static void validate(boolean expression, String pattern, Object o) {
 		if (expression == false)
 			throw new IllegalStateException(format(pattern, o));
 	}
 
 	public static <T> T ifNull(T t, T defaultValue) {
 		return t == null ? defaultValue : t;
 	}
 
 	public static <T> T notNull(T t, Object message) {
 		if (t == null)
 			throw new IllegalArgumentException(toString(message));
 		return t;
 	}
 
 	public static <T> T notNull(T t, String pattern, Object o) {
 		if (t == null)
 			throw new IllegalArgumentException(format(pattern, o));
 		return t;
 	}
 
 	public static boolean isEmpty(Object[] array) {
 		return array == null || array.length == 0;
 	}
 
 	public static <T> T[] notEmpty(T[] array, Object message) {
 		if (array == null || array.length == 0)
 			throw new IllegalArgumentException(toString(message));
 		return array;
 	}
 
 	public static boolean isEmpty(String s) {
 		return s == null || s.isEmpty();
 	}
 
 	public static String notEmpty(String s, Object message) {
 		if (s == null || s.isEmpty())
 			throw new IllegalArgumentException(toString(message));
 		return s;
 	}
 
 	public static String trimToNull(String s) {
 		return (s == null || (s = s.trim()).length() == 0) ? null : s;
 	}
 
 	public static boolean equal(Object a, Object b) {
 		return a == b ? true : (a == null ? false : a.equals(b));
 	}
 
 	public static List<String> split(String s, char separator) {
 		if (s == null)
 			return Collections.emptyList();
 		List<String> list = null;
 		int i, pos = 0;
 		while ((i = s.indexOf(separator, pos)) >= 0) {
 			if (i > pos) {
 				if (list == null)
 					list = new ArrayList<String>();
 				list.add(s.substring(pos, i));
 			}
 			pos = i + 1;
 		}
 		if (pos >= s.length())
 			return list == null ? Collections.<String> emptyList() : list;
 		if (list == null)
 			return Collections.singletonList(s.substring(pos));
 		list.add(s.substring(pos));
 		return list;
 	}
 
 	public static Class<?> loadclass(ClassLoader loader, String name) {
 		try {
 			return loader.loadClass(name);
 		} catch (ClassNotFoundException e) {
 			throw new XutilRuntimeException(e);
 		}
 	}
 
 	public static <T> T newInstance(Class<T> clazz) {
 		try {
 			return clazz.newInstance();
 		} catch (Exception e) {
 			throw Objutil.rethrow(e);
 		}
 	}
 
 	public static void closeQuietly(Closeable closeable) {
 		if (closeable != null)
 			try {
 				closeable.close();
 			} catch (Throwable ex) {// ignore close exception
 			}
 	}
 
 	public static String decapitalize(String s) {
 		int len;
 		if (s == null || (len = s.length()) == 0)
 			return s;
 		if (len > 1 && Character.isUpperCase(s.charAt(1)) && Character.isUpperCase(s.charAt(0)))
 			return s;
 		char chars[] = s.toCharArray();
 		chars[0] = Character.toLowerCase(chars[0]);
 		return new String(chars);
 	}
 
 	public static Object defaults(Class type) {
 		if (type.isPrimitive()) {
 			if (type == Integer.TYPE)
 				return INT;
 			if (type == Long.TYPE)
 				return LONG;
 			if (type == Double.TYPE)
 				return DOUBLE;
 			if (type == Boolean.TYPE)
 				return Boolean.FALSE;
 			if (type == Byte.TYPE)
 				return BYTE;
 			if (type == Character.TYPE)
 				return CHAR;
 			if (type == Short.TYPE)
 				return SHORT;
 			if (type == Float.TYPE)
 				return FLOAT;
 		}
 		return null;
 	}
 
 	public static StringBuilder append(StringBuilder out, Object[] array) {
 		if (array == null)
 			return out.append("null");
 		out.append('[');
 		for (int i = 0, len = array.length; i < len; i++)
 			(i > 0 ? out.append(',') : out).append(toString(array[i]));
 		return out.append(']');
 	}
 
 	public static RuntimeException rethrow(Throwable e) {
 		if (e instanceof Error)
 			throw (Error) e;
 		if (e instanceof RuntimeException)
 			throw (RuntimeException) e;
 		throw new XutilRuntimeException(e);
 	}
 
 	public static String toString(Object object) {
 		if (object == null)
 			return "null";
 		try {
 			return object.toString();
 		} catch (RuntimeException e) {
 			return object.getClass() + " toString() " + e;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T> Reference<T> nullRefence() {
 		return nullRef;
 	}
 
 	public static URL toURL(File file) {
 		try {
 			return file.toURI().toURL();
 		} catch (MalformedURLException e) {
 			throw new XutilRuntimeException(e);
 		}
 	}
 
 	public static File toFile(URL url) {
 		return new File(URI.create(url.toString()));
 	}
 
 	public static <T extends Map> T loadProps(URL url, T map) {
 		boolean xml = url.getPath().endsWith(".xml");
 		try {
 			if (map instanceof Properties)
 				ENV.load((Properties) map, url.openStream(), xml);
 			else
 				ENV.env.loadMap(url.openStream(), map, xml);
 			return map;
 		} catch (IOException e) {
 			throw new XutilRuntimeException(url.toString(), e);
 		}
 	}
 
 	public static Properties properties() {
 		return ENV.env;
 	}
 
 	public static String systring(String propertyName) {
 		return ENV.env.getProperty(propertyName);
 	}
 
 	public static String systring(String propertyName, String defaultValue) {
 		return ENV.env.getProperty(propertyName, defaultValue);
 	}
 
 	public static int systring(String propertyName, int defaultValue) {
 		if (isEmpty(propertyName = systring(propertyName)))
 			return defaultValue;
 		return Integer.parseInt(propertyName);
 	}
 
 	public static String placeholder(String s, Replace alias) {
 		if (s.startsWith("OBF:"))
 			return unobfuscate(s.substring(4));
 		int i = s.indexOf("${"), j;
 		if (i < 0 || (j = s.indexOf('}', i + 2)) < 0)
 			return s;
 		int k = 0;
 		StringBuilder sb = new StringBuilder(64);
 		do {
 			sb.append(s.substring(k, i));
 			String defs, key = defs = s.substring(i + 2, j);
 			if (s.charAt(j - 1) == ']' && (k = key.indexOf('[')) >= 0) {
 				defs = key.substring(k + 1, key.length() - 1);
 				key = key.substring(0, k);
 			}
 			sb.append(ifNull(alias.replace(key), defs));
 			i = s.indexOf("${", k = j + 1);
 		} while (i >= 0 && (j = s.indexOf('}', i + 2)) > 0);
 		return sb.append(s.substring(k)).toString();
 	}
 
 	public static String unobfuscate(String src) {
 		char c, chars[] = src.toCharArray();
 		for (int i = chars.length - 1, j = 0; i >= 0; i--) {
 			if (i <= j)
 				c = chars[i];
 			else {
 				c = chars[j];
 				chars[j++] = chars[i];
 				chars[i] = c;
 			}
 			if (c >= 48 && c <= 125)
 				chars[i] = (char) (c < 48 + 39 ? c + 39 : c - 39);
 		}
 		return new String(chars);
 	}
 
 	public static String format(String pattern, Object... objects) {
 		if (pattern == null || objects == null || objects.length == 0)
 			return pattern;
 		StringBuilder buf = new StringBuilder();
 		int j, begin = 0, loc = 0;
 		while ((j = pattern.indexOf("{}", begin)) >= 0) {
 			buf.append(pattern.substring(begin, j));
 			if (objects[loc] instanceof Object[])
 				append(buf, (Object[]) objects[loc]);
 			else
 				buf.append(toString(objects[loc]));
 			begin = j + 2;
 			if (++loc >= objects.length)
 				break;
 		}
 		if (begin < pattern.length())
 			buf.append(pattern.substring(begin));
 		return buf.toString();
 	}
 	
 	public static void main(String[] args) {
 		if(args.length==0)
 			for (Map.Entry e : ENV.env.entrySet())
 				Objutil.log(e.getKey().toString(), "[{}]", null, e.getValue());
 		else
 			for(String s : args)
 				Objutil.log(s, "[{}]", null, unobfuscate(s));
 	}
 
 	static void log(String name, String pattern, Throwable t, Object... argArray) {
 		StringBuilder sb = new StringBuilder(128);
 		int j, i = (int) ((System.currentTimeMillis() + TZoffset) % (24 * 3600000));
 		((j = i / 3600000) < 10 ? sb.append('0') : sb).append(j).append(':');
 		((j = i / 60000 % 60) < 10 ? sb.append('0') : sb).append(j).append(':');
 		((j = i / 1000 % 60) < 10 ? sb.append('0') : sb).append(j).append('.');
 		((j = i % 1000) < 100 ? sb.append(j < 10 ? "00" : "0") : sb).append(j).append(" [")
 				.append(Thread.currentThread().getName()).append("] INFO  ").append(name).append(" - ");
		System.err.println(sb.append(format(pattern, argArray)).toString());
 		if (t != null)
			t.printStackTrace(System.err);
 	}
 
 	private static class ENV extends Properties implements Replace {
 		private static final long serialVersionUID = 1L;
 		static final ENV env = new ENV();
 		static {
 			try {
 				env.put(XUTILS_HOME, new File(env.getProperty(XUTILS_HOME, ".")).getCanonicalPath());
 				if (env.getProperty(XUTILS_LOCALHOST) == null)
 					env.put(XUTILS_LOCALHOST, InetAddress.getLocalHost().getHostName());
 				File file = new File(env.getProperty(XUTILS_HOME), "xutils.xml");
 				if (file.exists())
 					load(env, new FileInputStream(file), true);
 				else {
 					InputStream in = Objutil.class.getClassLoader().getResourceAsStream("xutils.xml");
 					if (in != null)
 						load(env, in, true);
 				}
 			} catch (IOException e) {
 				throw new XutilRuntimeException(e);
 			}
 			log(XUTILS_HOME, "{}    xutils-{}", null, env.getProperty(XUTILS_HOME),Objutil.class.getPackage().getImplementationVersion());
 		}
 
 		static void load(Properties prop, InputStream in, boolean xml) throws IOException {
 			try {
 				if (xml)
 					prop.loadFromXML(in);
 				else
 					prop.load(new InputStreamReader(in));
 			} finally {
 				in.close();
 			}
 		}
 
 		private transient volatile Map delegate = this;
 
 		private ENV() {
 			super(System.getProperties());
 		}
 
 		@Override
 		@SuppressWarnings("unchecked")
 		public synchronized Object put(Object key, Object value) {
 			if (delegate != this)
 				return delegate.put(key, value);
 			if (value instanceof String)
 				value = placeholder((String) value, this);
 			return super.put(key, value);
 		}
 
 		@Override
 		public String replace(String name) {
 			return getProperty(name);
 		}
 
 		private Object readResolve() {
 			return (delegate = this);
 		}
 
 		synchronized void loadMap(InputStream in, Map map, boolean xml) throws IOException {
 			Map prev = delegate;
 			delegate = map;
 			try {
 				load(this, in, xml);
 			} finally {
 				delegate = prev;
 			}
 		}
 	}
 }
