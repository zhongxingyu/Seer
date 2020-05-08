 package uk.co.brotherlogic.jarpur;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 public class LinkTable {
 
	public static String add = "/jarpur/";
 	Map<String, String> links = new TreeMap<String, String>();
 
 	private static LinkTable singleton;
 
 	public static LinkTable getLinkTable() {
 		if (singleton == null)
 			singleton = new LinkTable();
 		return singleton;
 	}
 
 	private LinkTable() {
 		Properties properties = new Properties();
 		try {
 			properties
 					.load(new FileInputStream(new File("mapping.properties")));
 			for (Entry<Object, Object> entry : properties.entrySet()) {
				links.put(entry.getKey().toString(), entry.getValue()
						.toString());
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String resolveLink(Object o) {
 		System.err.println("RESOLVING LINK: " + o);
 		String classname = o.getClass().getCanonicalName();
 		System.err.println("CLASSHNAME: " + classname);
 		if (links.containsKey(classname)) {
 			System.err.println("LINK FOUND:" + links.get(classname));
 			return resolveLink(o, links.get(classname));
 		} else
 			System.err.println("NOT FOUND: " + links.keySet());
 		return null;
 	}
 
 	Pattern objPattern = Pattern.compile("\\%\\%(.*?)\\%\\%");
 
 	private String resolveLink(Object o, String ref) {
 		try {
 			Class cls = Class.forName(ref);
 			Page pg = (Page) cls.getConstructor(new Class[0]).newInstance(
 					new Object[0]);
 			String params = pg.linkParams(o);
 
			return JarpurProperties.get("web") + "/"
					+ ref.substring(JarpurProperties.get("base").length() + 1)
					+ "/" + params;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
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
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	protected String resolveMethod(Object obj, String methodName) {
 
 		try {
 			Class cls = obj.getClass();
 			Method m = cls.getMethod(methodName, new Class[0]);
 			Object ret = m.invoke(obj, new Object[0]);
 			return ret.toString();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 }
