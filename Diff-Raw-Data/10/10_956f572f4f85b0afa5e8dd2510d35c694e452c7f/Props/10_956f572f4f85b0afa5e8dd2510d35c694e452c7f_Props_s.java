 package org.eweb4j.cache;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eweb4j.config.ConfigConstant;
 import org.eweb4j.config.Log;
 import org.eweb4j.config.LogFactory;
 import org.eweb4j.config.bean.ConfigBean;
 import org.eweb4j.config.bean.I18N;
 import org.eweb4j.config.bean.Locale;
 import org.eweb4j.config.bean.Prop;
 import org.eweb4j.i18n.Lang;
 import org.eweb4j.util.CommonUtil;
 import org.eweb4j.util.FileUtil;
 import org.eweb4j.util.RegexList;
 
 @SuppressWarnings("all")
 public class Props {
 
 	private static Log log = LogFactory.getConfigLogger(Props.class);
 
 	private static Set<String> i18nIds = new HashSet<String>();
 
 	/**
 	 * Global properties
 	 */
 	private static Map<String, String> globalMap = new Hashtable<String, String>();
 
 	/**
          * 
          */
 	private static Map<String, Map<String, String>> props = new Hashtable<String, Map<String, String>>();
 
 	private static class MapProxy<K, V> implements Map<K, V> {
 		private String id;
 
 		private Map<K, V> map() {
 			String _id = id + "_" + Lang.get().toString();
 			if (props.containsKey(_id))
 				return (Map<K, V>) props.get(_id);
 			else {
 				_id = id + "_" + Lang.get().getLanguage();
 				if (props.containsKey(_id))
 					return (Map<K, V>) props.get(_id);
 			}
 
 			return (Map<K, V>) props.get(id);
 		}
 
 		public MapProxy(String id) {
 			this.id = id;
 		}
 
 		public int size() {
 			Map map = this.map();
 			return map == null ? null : map.size();
 		}
 
 		public boolean isEmpty() {
 			Map map = this.map();
 			return map == null ? null : map.isEmpty();
 		}
 
 		public boolean containsKey(Object key) {
 			Map map = this.map();
 			return map == null ? null : map.containsKey(key);
 		}
 
 		public boolean containsValue(Object value) {
 			Map map = this.map();
 			return map == null ? null : map.containsValue(value);
 		}
 
 		public V get(Object key) {
 			Map map = this.map();
 			return (V) (map == null ? null : map.get(key));
 		}
 
 		public V put(K key, V value) {
 			Map map = this.map();
 			return (V) (map == null ? null : map.put(key, value));
 		}
 
 		public V remove(Object key) {
 			Map map = this.map();
 			return (V) (map == null ? null : map.remove(key));
 		}
 
 		public void putAll(Map<? extends K, ? extends V> m) {
 			Map map = this.map();
 			if (map != null)
 				map.putAll(m);
 		}
 
 		public void clear() {
 			Map map = this.map();
 			if (map != null)
 				map.clear();
 		}
 
 		public Set<K> keySet() {
 			Map map = this.map();
 			return (Set<K>) (map == null ? null : map.keySet());
 		}
 
 		public Collection<V> values() {
 			Map map = this.map();
 			return (Collection<V>) (map == null ? null : map.values());
 		}
 
 		public Set<java.util.Map.Entry<K, V>> entrySet() {
 			Map map = this.map();
 			return (Set<java.util.Map.Entry<K, V>>) (map == null ? null : map.entrySet());
 		}
 
 	}
 
 	private Props() {
 	}
 
 	public static String get(String key) {
 		return globalMap.get(key);
 	}
 
 	public static Map<String, String> getMap(String id) {
 		if (i18nIds.contains(id))
 			return new MapProxy<String, String>(id);
 
 		return props.get(id);
 	}
 
 	// 读取properties的全部信息
 	public static synchronized String readProperties(Prop f, boolean isCreate)
 			throws IOException {
 		if (f == null || f.getPath().length() == 0)
 			return null;
 
 		String id = f.getId();
 		String path = f.getPath();
 		ConfigBean cb = (ConfigBean) SingleBeanCache.get(ConfigBean.class.getName());
 		I18N i18n = cb.getLocales();
 
 		final String sufPro = ".properties";
 
 		if (i18n != null) {
 			for (Locale l : i18n.getLocale()) {
 
 				String suffix1 = "_" + l.getLanguage() + "_" + l.getCountry();
 				String tmpPath1 = path.replace(sufPro, suffix1 + sufPro);
 				i18nIds.add(id);
 				if (FileUtil.exists(ConfigConstant.CONFIG_BASE_PATH + tmpPath1)) {
 					Prop p = new Prop();
 					p.setGlobal("false");
 					p.setId(id + suffix1);
 					p.setPath(tmpPath1);
 					readProperties(p, false);// 递归，把国际化文件内容加载仅缓存
 					isCreate = false;// 如果存在国际化文件，那么默认的文件允许不存在
 					continue;
 				}
 
 				String suffix2 = "_" + l.getLanguage();
 				String tmpPath2 = path.replace(sufPro, suffix2 + sufPro);
 
 				if (FileUtil.exists(ConfigConstant.CONFIG_BASE_PATH + tmpPath2)) {
 					Prop p = new Prop();
 					p.setGlobal("false");
 					p.setId(id + suffix2);
 					p.setPath(tmpPath2);
 					readProperties(p, false);// 递归，把国际化文件内容加载仅缓存
 					isCreate = false;// 如果存在国际化文件，那么默认的文件允许不存在
 					continue;
 				}
 			}
 		}
 
 		String error = null;
 		String filePath = ConfigConstant.CONFIG_BASE_PATH + path;
 		String global = f.getGlobal();
 
 		Properties properties = new Properties();
 		InputStream in = null;
 		Hashtable<String, String> tmpHt = new Hashtable<String, String>();
 		try {
 			in = new BufferedInputStream(new FileInputStream(filePath));
 			properties.load(in);
 			
 			//第一遍全部加进来
 			Props.loadProperty(properties, tmpHt);
 
 			//渲染 ${} 引用值
 			Pattern pattern = Pattern.compile(RegexList.property_single_regexp);
 			for (Iterator<Entry<String, String>> it = tmpHt.entrySet().iterator(); it.hasNext();) {
 				Entry<String, String> e = it.next();
 				String key = e.getKey();
 				String property = e.getValue();
 				Props.renderVarable(pattern, key, property, tmpHt);
 			}
 
 			if ("true".equalsIgnoreCase(global) || "1".equalsIgnoreCase(global)) {
 				globalMap.putAll(tmpHt);
 				log.debug("global | map -> " + tmpHt.toString());
 			} else if (id != null && id.length() > 0) {
 				props.put(id, tmpHt);
 
 				log.debug("id -> "+id+" | map -> " + tmpHt.toString());
 			}
 
 		} catch (FileNotFoundException e) {
 			log.warn(filePath + ", file not found!");
 			if (isCreate) {
 				boolean flag = FileUtil.createFile(filePath);
 				if (flag) {
 					error = filePath + " create success";
 					Props.writeProperties(filePath, "framework", "eweb4j");
 					log.warn(error);
 				} else {
 					log.warn(filePath + " create fail");
 				}
 			}
 		} finally {
 			if (in != null)
 				in.close();
 		}
 
 		return error;
 
 	}
 	
 	/**
 	 * 递归渲染${}变量
 	 * TODO
 	 * @date 2012-12-5 下午08:04:35
 	 */
 	private static String renderVarable(Pattern pattern, String key, String property, Map<String,String> tmpHt){
 		Matcher matcher = pattern.matcher(property);
		if (matcher.find()) {
 			String g = matcher.group();
 			String _key = g.replace("${", "").replace("}", "");
 			String value = "";
 			if(tmpHt.containsKey(_key)){
 				value = tmpHt.get(_key);
 			}else{
 				value = "[Can't find this variable in the Localization properties]:"+_key;
 				log.error("Variable [ "+ g +" ] not found in the Localization (properties) config file!");
 			}
			String result = renderVarable(pattern, _key, value, tmpHt);
			if (result == null)
				tmpHt.put(key, property.replace(g, value));
 		}
		
 		return null;
 	}
 
 	private static void loadProperty(Properties properties, Hashtable<String, String> tmpHt) throws UnsupportedEncodingException {
 		Enumeration<?> en = properties.propertyNames();
 		
 		while (en.hasMoreElements()) {
 			String key = (String) en.nextElement();
 			String property = properties.getProperty(key);
 			if (property == null)
 				continue;
 			
 			if (!property.matches(RegexList.has_chinese_regexp)) {
 				property = new String(property.getBytes("ISO-8859-1"), "UTF-8");
 			}
 			tmpHt.put(key, property);
 		}
 	}
 
 	public synchronized static void writeProp(String propId, String key,
 			Object value) throws IOException {
 		if (propId == null)
 			return;
 
 		ConfigBean cb = (ConfigBean) SingleBeanCache.get(ConfigBean.class.getName());
 		List<Prop> files = cb.getProperties().getFile();
 		for (Prop f : files) {
 			if (propId.equals(f.getId())) {
 
 				Map<String, String> map = props.get(propId);
 				if (map == null) {
 					map = new Hashtable<String, String>();
 					props.put(propId, map);
 				}
 				String val = String.valueOf(value);
 				map.put(key, val);
 				String filePath = ConfigConstant.CONFIG_BASE_PATH + f.getPath();
 
 				writeProperties(filePath, key, val);
 
 				break;
 			}
 		}
 
 	}
 
 	// 写入properties信息
 	public synchronized static void writeProperties(String filePath,
 			String parameterName, String parameterValue) throws IOException {
 		Properties prop = new Properties();
 		InputStream fis = null;
 		OutputStream fos = null;
 
 		fis = new FileInputStream(filePath);
 		// 从输入流中读取属性列表（键和元素对）
 		prop.load(fis);
 		// 调用 Hashtable 的方法 put。使用 getProperty 方法提供并行性。
 		// 强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
 		fos = new FileOutputStream(filePath);
 		prop.setProperty(parameterName, parameterValue);
 		// 以适合使用 load 方法加载到 Properties 表中的格式，
 		// 将此 Properties 表中的属性列表（键和元素对）写入输出流
 		prop.store(fos, "eweb4j last update '" + parameterName + "' value");
 
 		fos.flush();
 
 		if (fis != null)
 			fis.close();
 		if (fos != null)
 			fos.close();
 
 	}
 
 	public static Map<String, Map<String, String>> getProps() {
 		return props;
 	}
 
 	public static Map<String, String> getGlobalMap() {
 		return globalMap;
 	}
 
 }
