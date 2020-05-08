 package org.eweb4j.util;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.text.Normalizer;
 import java.text.Normalizer.Form;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eweb4j.cache.ORMConfigBeanCache;
 import org.eweb4j.cache.Props;
 import org.eweb4j.orm.PropType;
 import org.eweb4j.orm.config.bean.ORMConfigBean;
 import org.eweb4j.orm.config.bean.Property;
 import org.w3c.dom.Node;
 
 import com.alibaba.fastjson.JSON;
 import com.alibaba.fastjson.serializer.SerializerFeature;
 
 public class CommonUtil {
 	
 	public static void main(String[] args){
 		System.out.println(CommonUtil.formatTime(new Date(1357660800000L)));
 		String source = "2013-01-17 11:00";
 		Date date = CommonUtil.parse("yyyy-MM-dd HH:mm", source);
 		System.out.println(String.valueOf(date.getTime()).substring(0, 10));
 		
 		long time = System.currentTimeMillis() + CommonUtil.toSeconds("dh 7m 42s").longValue()*1000l;
 		System.out.println(time);
 		System.out.println((""+time).substring(0, 10));
 		System.out.println(CommonUtil.formatTime(new Date(time)));
 		
 //		double min = 0.01;
 //		double max = 0.99;
 //		for (int i = 0; i < 20; i++){
 //			System.out.println(CommonUtil.random(min, max).doubleValue());
 //		}
 	}
 	
 	public static Number random(double min, double max){
 		return (min+(max-min)*Math.random());
 	}
 	
 	public static String cleanLF(String str) {
 		return str.replace("\n", "");
 	}
 	
 	public static Double addDouble(Object d1, Object d2) {
 		return toDouble(String.valueOf(d1))+toDouble(String.valueOf(d2));
 	}
 	
 	public static Float addFloat(Object d1, Object d2) {
 		return toFloat(String.valueOf(d1))+toFloat(String.valueOf(d2));
 	}
 	
 	public static Integer addInteger(Object d1, Object d2) {
 		return toInt(String.valueOf(d1))+toInt(String.valueOf(d2));
 	}
 	
 	public static Long addLong(Object d1, Object d2) {
 		return toLong(String.valueOf(d1))+toLong(String.valueOf(d2));
 	}
 	
 	public static Integer toInt(String str) {
 		return Integer.parseInt(str);
 	}
 	
 	public static Long toLong(String str) {
 		return Long.parseLong(str);
 	}
 	
 	public static Float toFloat(String str) {
 		return Float.parseFloat(str);
 	}
 	
 	public static Double toDouble(String str) {
 		return Double.parseDouble(str);
 	}
 	
 	public static Boolean toBoolean(String str) {
 		return Boolean.parseBoolean(str);
 	}
 	
 	public static String String(){
 		return String("");
 	}
 	
 	public static String String(Object obj){
 		return String.valueOf(obj);
 	}
 	
 	public static String toXml(Node node, boolean keepHeader) throws Exception{
 		Transformer transformer;
     	DOMSource xmlSource = new DOMSource(node);
     	transformer = TransformerFactory.newInstance().newTransformer();
     	StringWriter writer = new StringWriter();
     	StreamResult outputTarget = new StreamResult(writer);
     	transformer.transform(xmlSource, outputTarget);
     	String str = writer.getBuffer().toString();
     	
     	if (!keepHeader)
     		return str.substring(str.indexOf("?>")+2);
     	else
     		return str;
 	}
 	
 	/**
 	 * 删除标签
 	 * @date 2013-1-5 下午05:24:06
 	 * @param html
 	 * @param keepTags 保留的标签，如果不给定则删除所有标签
 	 * @return
 	 */
 	public static String cleanOtherXmlTags(String html, String... keepTags) {
 		return html.replaceAll(inverseXmlTagsRegex(keepTags), "");
 	}
 	
 	/**
 	 * 删除标签
 	 * @date 2013-1-5 下午05:35:27
 	 * @param html
 	 * @param isRMCnt 是否删除标签内的所有内容 <p>This is p.<a href="#">This is a.</a></p>如果干掉a标签，就变成=><p>This is p.</p>
 	 * @param delTags 需要删除的Tag，如果不给定则删除所有标签
 	 * @return
 	 */
 	public static String cleanXmlTags(String html, boolean isRMCnt, String... delTags) {
 		if (isRMCnt){
 			for (String delTag : delTags){
 				List<String> tag = findByRegex(html, xmlTagsRegex(delTag));
 				if (tag == null || tag.isEmpty() || tag.size() != 2)
 					continue;
 				String regex = resolveRegex(tag.get(0)) + ".*" + resolveRegex(tag.get(1));
 				html = html.replaceAll(regex, "");
 			}
 			return html;
 		}
 		
 		return html.replaceAll(xmlTagsRegex(delTags), "");
 	}
 	
 	public static String resolveRegex(String regex){
 		List<String> cc = Arrays.asList("\\", "^", "$", "*", "+", "?", "{", "}", "(", ")", ".", "[", "]", "|");
 		for (String c : cc) {
 			regex = regex.replace(c, "\\"+c);
 		}
 		return regex;
 	}
 	
 	/**
 	 * 匹配除了给定标签意外其他标签的正则表达式
 	 * @date 2013-1-7 下午03:45:29
 	 * @param keepTags 如果不给定则匹配所有标签
 	 * @return
 	 */
 	public static String inverseXmlTagsRegex(String... keepTags) {
 		if (keepTags == null || keepTags.length == 0)
 			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
 		String fmt = "\\b%s\\b";
 		StringBuilder sb = new StringBuilder();
 		for (String kt : keepTags){
 			if (kt == null || kt.trim().length() == 0)
 				continue;
 			
 			if (sb.length() > 0)
 				sb.append("|");
 			sb.append(String.format(fmt, kt));
 		}
 		if (sb.length() == 0)
 			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
 		
 		String pattern = "<[!/]?\\b(?!("+sb.toString()+"))+\\b\\s*[^>]*>";
 		
 		return pattern;
 	}
 	
 	/**
 	 * 匹配给定标签的正则表达式
 	 * @date 2013-1-7 下午03:47:11
 	 * @param tags 如果不给定则匹配所有标签
 	 * @return
 	 */
 	public static String xmlTagsRegex(String... tags) {
 		if (tags == null || tags.length == 0)
 			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
 		String fmt = "\\b%s\\b";
 		StringBuilder sb = new StringBuilder();
 		for (String kt : tags){
 			if (kt == null || kt.trim().length() == 0)
 				continue;
 			
 			if (sb.length() > 0)
 				sb.append("|");
 			sb.append(String.format(fmt, kt));
 		}
 		if (sb.length() == 0)
 			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
 		
 		String pattern = "<[!/]?("+sb.toString()+")\\s*[^>]*>";
 		
 		return pattern;
 	}
 	
 	public static <T> T mappingPojo(Map<String, Object> data, Class<T> cls) throws Exception {
 		if (data == null)
 			return null;
 		
 		List<Map<String, Object>> _list = new ArrayList<Map<String, Object>>(1);
 		_list.add(data);
 		List<T> list = mappingPojo(_list, cls);
 		return list == null ? null : list.get(0);
 	}
 	
 	public static <T> List<T> mappingPojo(List<Map<String, Object>> datas, Class<T> cls) throws Exception {
 		if (datas == null || datas.isEmpty()) 
 			return null;
 		
 		List<String> columns = new ArrayList<String>();
 		for (String col : datas.get(0).keySet()) 
 			columns.add(col);
 		
 		List<T> list = new ArrayList<T>();
 		T t = null;
 		for (Map<String, Object> data : datas) {
 			t = cls.newInstance();
 			ReflectUtil ru = new ReflectUtil(t);
 			ORMConfigBean ormBean = ORMConfigBeanCache.get(cls.getName());
 
 			for (Iterator<Property> it = ormBean.getProperty().iterator(); it.hasNext();) {
 				Property p = it.next();
 				String type = p.getType();
 				if (type == null)
 					continue;
 
 				// 如果查询出来的字段名字没有,则不进行值注入
 				boolean flag = false;
 				for (String col : columns) {
 					if (col.equalsIgnoreCase(p.getColumn())) {
 						flag = true;
 						continue;
 					}
 				}
 
 				if (!flag)
 					continue;
 
 				Method m = ru.getSetter(p.getName());
 				if (m == null)
 					continue;
 
 				Object value = data.get(p.getColumn());
 				if (value == null)
 					continue;
 
 				String v = String.valueOf(value);
 				if (v == null) {
 					v = "";
 				}
 				
 				if ("int".equalsIgnoreCase(type) || "java.lang.Integer".equalsIgnoreCase(type)) {
 					if ("".equals(v.trim())) {
 						v = "0";
 					}
 					if (value instanceof Boolean)
 						v = ((Boolean)value ? "1" : "0");
 					
 					m.invoke(t, Integer.parseInt(v));
 				} else if ("long".equalsIgnoreCase(type) || "java.lang.Long".equalsIgnoreCase(type)) {
 					if ("".equals(v.trim())) {
 						v = "0";
 					}
 					if (value instanceof Boolean)
 						v = ((Boolean)value ? "1" : "0");
 					
 					m.invoke(t, Long.parseLong(v));
 				} else if ("float".equalsIgnoreCase(type) || "java.lang.Float".equalsIgnoreCase(type)) {
 					if ("".equals(v.trim())) {
 						v = "0.0";
 					}
 					if (value instanceof Boolean)
 						v = ((Boolean)value ? "1.0" : "0.0");
 					
 					m.invoke(t, Float.parseFloat(v));
 				} else if ("double".equalsIgnoreCase(type) || "java.lang.Double".equalsIgnoreCase(type)) {
 					if ("".equals(v.trim())) {
 						v = "0.0";
 					}
 					if (value instanceof Boolean)
 						v = ((Boolean)value ? "1.0" : "0.0");
 					m.invoke(t, Double.parseDouble(v));
 				} else if ("string".equalsIgnoreCase(type) || "java.lang.String".equalsIgnoreCase(type)) {
 					m.invoke(t, v);
 				} else if ("boolean".equalsIgnoreCase(type) || "java.lang.Boolean".equalsIgnoreCase(type)){
 					if ("1".equals(v.trim()) || "true".equals(v.trim())){
 						m.invoke(t, true);
 					}else if ("0".equals(v.trim()) || "false".equals(v.trim())){
 						m.invoke(t, false);
 					}
 				} else if ("date".equalsIgnoreCase(type) || "java.sql.Date".equalsIgnoreCase(type) || "java.util.Date".equalsIgnoreCase(type)) {
 					m.invoke(t, value);
 				} else if ("timestamp".equalsIgnoreCase(type) || "java.sql.Timestamp".equalsIgnoreCase(type)) {
 					m.invoke(t, value);
 				} else if ("time".equalsIgnoreCase(type) || "java.sql.Time".equalsIgnoreCase(type)) {
 					m.invoke(t, value);
 				} else if ("byte[]".equalsIgnoreCase(type) || "[Ljava.lang.Byte;".equalsIgnoreCase(type)) {
 					m.invoke(t, value);
 				} else if (PropType.ONE_ONE.equalsIgnoreCase(type) || PropType.MANY_ONE.equalsIgnoreCase(type)) {
 					if ("".equals(v))
 						continue;
 
 					Field field = ru.getField(p.getName());
 					Class<?> tarClass = field.getType();
 
 					String refField = p.getRelProperty();
 					Object tarObj = tarClass.newInstance();
 					
 					tarObj = ClassUtil.injectFieldValue(tarObj, refField, new String[] { v });
 					
 					m.invoke(t, tarObj);
 
 				} else if (PropType.ONE_MANY.equalsIgnoreCase(type)) {
 
 				} else if (PropType.MANY_MANY.equalsIgnoreCase(type)) {
 
 				} else if (!"".equals(type)) {
 					m.invoke(t, String.valueOf(value));
 				}
 
 			}
 			
 			list.add(t);
 		}
 
 		return list.isEmpty() ? null : list;
 	}
 	
 	public static String calculateTime(long start){
 		return calculateTime(start, System.currentTimeMillis(), "${d}d ${h}h${m}m${s}s");
 	}
 	
 	public static String calculateTime(long start, String format) {
 		return calculateTime(start, System.currentTimeMillis(), format);
 	}
 	
 	public static String calculateTime(long start,long end){
 		return calculateTime(start, end, "${d}d ${h}h${m}m${s}s");
 	}
 	
 	public static String calculateTime(long start, long end, String format){
 		if (format == null)
 			format = "${d}d ${h}h${m}m${s}s";
         long between = (end - start);// 得到两者的毫秒数
         long d = between / (24 * 60 * 60 * 1000);
         long h = (between / (60 * 60 * 1000) - d * 24);
         long m = ((between / (60 * 1000)) - d * 24 * 60 - h * 60);
         long s = (between / 1000 - d * 24 * 60 * 60 - h * 60 * 60 - m * 60);
         
 		return format
 				.replace("${d}", String.valueOf(d))
 				.replace("${h}", String.valueOf(h))
 				.replace("${m}", String.valueOf(m))
 				.replace("${s}", String.valueOf(s));
 	}
 	
 	public static Float toSeconds(String strTime){
 		Float time = 0F;
 		for (String s : strTime.split(" ")){
 			time += _toSeconds(s);
 		}
 		
 		return time;
 	}
 	
 	private static Float _toSeconds(String strTime){
 		Float time = 0F;
 		try {
 			if (strTime.endsWith("s")){
 				time = Float.parseFloat(strTime.replace("s", "")) * 1;
 			}else if (strTime.endsWith("m")){
 				time = Float.parseFloat(strTime.replace("m", "")) * 60;
 			}else if (strTime.endsWith("h")){
 				time = Float.parseFloat(strTime.replace("h", "")) * 60 * 60;
 			}else if (strTime.endsWith("d")){
 				time = Float.parseFloat(strTime.replace("d", "")) * 60 * 60 * 24;
 			}else
 				time = Float.parseFloat(strTime);
 		} catch (Throwable e) {
 			
 		}
 		
 		return time;
 	}
 	
 	public static boolean isSameHost(String hostUrl, String url) throws Exception{
 		URL siteURL = new URL(hostUrl);
 		URL currURL = new URL(url);
 		String siteHost = siteURL.getHost();
 		String currHost = currURL.getHost();
 		return siteHost.equals(currHost);
 	}
 	
 	public static String findOneByRegex(String input, String regex){
 		List<String> list = findByRegex(input, regex);
 		if (list == null)
 			return null;
 		
 		return list.get(0);
 	}
 	
 	public static List<String> findByRegex(String input, String regex){
 		List<String> result = new ArrayList<String>();
 		Pattern p = Pattern.compile(regex, Pattern.DOTALL);
 		Matcher m = p.matcher(input);
 		while(m.find()){
 			result.add(m.group());
 		}
 		
 		if (result.isEmpty()) return null;
 		
 		return result;
 	}
 	
 	public static byte[] long2ByteArray(long l) {
 		byte[] array = new byte[8];
 		int i, shift;
 		for (i = 0, shift = 56; i < 8; i++, shift -= 8) {
 			array[i] = (byte) (0xFF & (l >> shift));
 		}
 		return array;
 	}
 
 	public static byte[] int2ByteArray(int value) {
 		byte[] b = new byte[4];
 		for (int i = 0; i < 4; i++) {
 			int offset = (3 - i) * 8;
 			b[i] = (byte) ((value >>> offset) & 0xFF);
 		}
 		return b;
 	}
 
 	public static void putIntInByteArray(int value, byte[] buf, int offset) {
 		for (int i = 0; i < 4; i++) {
 			int valueOffset = (3 - i) * 8;
 			buf[offset + i] = (byte) ((value >>> valueOffset) & 0xFF);
 		}
 	}
 
 	public static int byteArray2Int(byte[] b) {
 		int value = 0;
 		for (int i = 0; i < 4; i++) {
 			int shift = (4 - 1 - i) * 8;
 			value += (b[i] & 0x000000FF) << shift;
 		}
 		return value;
 	}
 
 	public static long byteArray2Long(byte[] b) {
 		int value = 0;
 		for (int i = 0; i < 8; i++) {
 			int shift = (8 - 1 - i) * 8;
 			value += (b[i] & 0x000000FF) << shift;
 		}
 		return value;
 	}
 
 	public static boolean hasBinaryContent(String contentType) {
 		if (contentType != null) {
 			String typeStr = contentType.toLowerCase();
 			if (typeStr.contains("image") || typeStr.contains("audio")
 					|| typeStr.contains("video")
 					|| typeStr.contains("application")) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean hasPlainTextContent(String contentType) {
 		if (contentType != null) {
 			String typeStr = contentType.toLowerCase();
 			if (typeStr.contains("text/plain")) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static String toFriendlySeoTitle(String url){
 		return Normalizer.normalize(url.toLowerCase(), Form.NFD)
 				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
 		        .replaceAll("[^\\p{Alnum}]+", "-")
 		        .replaceAll("[^a-zA-Z0-9]+$", "")
 		        .replaceAll("^[^a-zA-Z0-9]+", "");
 	}
 
 	public static Long getNow(){
 		return System.currentTimeMillis();
 	}
 	
 	public static Long getNow(int length){
 		return getTime(length, new Date());
 	}
 	
 	public static Long getTime(int length, Date date){
 		String time = String.valueOf(date.getTime()).substring(0, length);
 		return Long.parseLong(time);
 	}
 	
 	public static String md5(final String input) {
 		try {
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			md.update(input.getBytes());
 			byte[] output = md.digest();
 			return bytesToHex(output);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return input;
 	}
 
 	public static String bytesToHex(byte[] b) {
 		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
 		StringBuffer buf = new StringBuffer();
 		for (int j = 0; j < b.length; j++) {
 			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
 			buf.append(hexDigit[b[j] & 0x0f]);
 		}
 		
 		return buf.toString();
 	}
 
 	public static boolean isBlank(final String str) {
 		if (null == str)
 			return true;
 		if (str.isEmpty())
 			return true;
 
 		return str.trim().isEmpty();
 	}
 
 	public static String uuid() {
 		return UUID.randomUUID().toString();
 	}
 
 	public static String resoveTime(final String time) {
 		String[] array = time.split(":");
 		StringBuilder sb = new StringBuilder();
 		for (String a : array) {
 			if (sb.length() > 0)
 				sb.append(":");
 
 			if (a.length() == 1)
 				a = new StringBuilder("0").append(a).toString();
 
 			sb.append(a);
 		}
 
 		return sb.toString() + ":00";
 	}
 
 	public static Date resoveDate(final String date) throws Exception {
 		Date d = null;
 		try {
 			d = parse("yyyy-MM-dd", date);
 		} catch (Throwable e1) {
 			try {
 				d = parse("yyyy-M-dd", date);
 			} catch (Throwable e2) {
 				try {
 					d = parse("yyyy-MM-d", date);
 				} catch (Throwable e3) {
 					try {
 						d = parse("yyyy-M-d", date);
 					} catch (Throwable e4) {
 						try {
 							d = parse("MM/dd/yyyy", date);
 						} catch (Throwable e5) {
 							try {
 								d = parse("MM/d/yyyy", date);
 							} catch (Throwable e6) {
 								try {
 									d = parse("M/dd/yyyy", date);
 								} catch (Throwable e7) {
 									try {
 										d = parse("M/d/yyyy", date);
 									} catch (Throwable e8) {
 										throw new Exception(e8);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		return d;
 	}
 
 	public static boolean isValidTime(String str) {
 		return str.matches("^\\d{2}:\\d{2}:\\d{2}$");
 	}
 
 	public static boolean isValidDate(String str) {
 		return str != null ? str
 				.matches("^\\d{4}(\\-|\\/|\\.)\\d{1,2}\\1\\d{1,2}$") : false;
 	}
 
 	public static boolean isValidDateTime(String source) {
 		return isValidDateTime(source, "yyyy-MM-dd HH:mm:ss");
 	}
 
 	public static boolean isValidDateTime(String source, String format) {
 		try {
 			Date date = parse(format, source);
 			return date != null;
 		} catch (Throwable e) {
 			return false;
 		}
 	}
 
 	public static <T> List<T> parseArray(String json, Class<T> clazz) {
 		return JSON.parseArray(json, clazz);
 	}
 
 	public static <T> T parse(String json, Class<T> clazz) {
 		return JSON.parseObject(json, clazz);
 	}
 
 	public static String toJson(Object object){
 		return toJson(object, null);
 	}
 	
 	public static String toJson(Object object, SerializerFeature[] features) {
 		if (features == null || features.length == 0){
 			features = new SerializerFeature[]{SerializerFeature.WriteNullBooleanAsFalse, SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullNumberAsZero, SerializerFeature.WriteNullStringAsEmpty};
 		}
 		return JSON.toJSONString(object, features);
 	}
 
 	public static String percent(long a, long b) {
 		double k = (double) a / b * 100;
 		java.math.BigDecimal big = new java.math.BigDecimal(k);
 		return big.setScale(2, java.math.BigDecimal.ROUND_HALF_UP)
 				.doubleValue() + "%";
 	}
 
 	public static long[] changeSecondsToTime(long seconds) {
 		long hour = seconds / 3600;
 		long minute = (seconds - hour * 3600) / 60;
 		long second = (seconds - hour * 3600 - minute * 60);
 
 		return new long[] { hour, minute, second };
 	}
 
 
 	public static int getDayOfYear(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 
 		return c.get(Calendar.DAY_OF_YEAR);
 	}
 
 	public static int getLastDayOfYear(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 
 		return c.getActualMaximum(Calendar.DAY_OF_YEAR);
 	}
 
 	public static int getDayOfMonth(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 
 		return c.get(Calendar.DAY_OF_MONTH);
 	}
 
 	public static int getLastDayOfMonth(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 
 		return c.getActualMaximum(Calendar.DAY_OF_MONTH);
 	}
 
 	// 判断日期为星期几,0为星期六,依此类推
 	public static int getDayOfWeek(Date date) {
 		// 首先定义一个calendar，必须使用getInstance()进行实例化
 		Calendar aCalendar = Calendar.getInstance();
 		// 里面野可以直接插入date类型
 		aCalendar.setTime(date);
 		// 计算此日期是一周中的哪一天
 		int x = aCalendar.get(Calendar.DAY_OF_WEEK);
 		return x;
 	}
 
 	public static int getLastDayOfWeek(Date date) {
 		Calendar c = Calendar.getInstance();
 		c.setTime(date);
 
 		return c.getActualMaximum(Calendar.DAY_OF_WEEK);
 	}
 
 	public static long difference(Date date1, Date date2) {
 		Calendar cal1 = Calendar.getInstance();
 		cal1.setTime(date1);
 
 		Calendar cal2 = Calendar.getInstance();
 		cal2.setTime(date2);
 
 		if (cal2.after(cal1)) {
 			return cal2.getTimeInMillis() - cal1.getTimeInMillis();
 		}
 
 		return cal1.getTimeInMillis() - cal2.getTimeInMillis();
 	}
 
 	public static Date addSecond(Date source, int s) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(source);
 		cal.add(Calendar.SECOND, s);
 
 		return cal.getTime();
 	}
 
 	public static Date addMinute(Date source, int min) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(source);
 		cal.add(Calendar.MINUTE, min);
 
 		return cal.getTime();
 	}
 
 	public static Date addHour(Date source, int hour) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(source);
 		cal.add(Calendar.HOUR_OF_DAY, hour);
 
 		return cal.getTime();
 	}
 
 	public static Date addDay(Date source, int day){
 		return addDate(source, day);
 	}
 	
 	public static Date addDate(Date source, int day) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(source);
 		cal.add(Calendar.DAY_OF_MONTH, day);
 
 		return cal.getTime();
 	}
 
 	public static Date addMonth(Date source, int month) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(source);
 		cal.add(Calendar.MONTH, month);
 
 		return cal.getTime();
 	}
 
 	public static Date addYear(Date source, int year) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(source);
 		cal.add(Calendar.YEAR, year);
 
 		return cal.getTime();
 	}
 
 	public static Date parse(String format, String source) {
 		int aaIndex = format.indexOf(" aa");
 		if (aaIndex > -1){
 			format = format.replace(" aa", "");
 			String apm = source.substring(aaIndex+1, aaIndex+1+2);
 			return parse(format, source.substring(0, aaIndex), apm);
 		}
 		
 		SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
 		try {
 			return sdf.parse(source);
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public static Date parse(String format, String source, String amOrPm) {
 		SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
 		try {
 			Date date = sdf.parse(source);
 			if ("PM".equalsIgnoreCase(amOrPm)){
 				date = CommonUtil.addHour(date, 12);
 			}
 			return date;
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static Date parse(String source) {
 		return parse("yyyy-MM-dd HH:mm:ss", source);
 	}
 
 	public static String upperFirst(String s) {
 		return s.replaceFirst(s.substring(0, 1), s.substring(0, 1)
 				.toUpperCase());
 	}
 
 	public static Map<String, Object> map(String k, Object v) {
 		Map<String, Object> map = new HashMap<String, Object>(1);
 		map.put(k, v);
 		return map;
 	}
 
 	public static Map<String, Object> map(String[] keys, Object[] values) {
 		Map<String, Object> map = new HashMap<String, Object>(keys.length);
 		for (int i = 0; i < keys.length; i++) {
 			map.put(keys[i], values[i]);
 		}
 
 		return map;
 	}
 
 	/**
 	 * 按照给定的 by 分割字符串，然后转化成Long数组。
 	 * 
 	 * @param source
 	 * @param by
 	 * @return
 	 */
 	public static long[] splitToLong(String source, String by) {
 
 		if (source == null || source.trim().length() == 0 || by == null
 				|| by.trim().length() == 0)
 			return null;
 
 		String[] strs = source.split(by);
 		long[] longs = new long[strs.length];
 		for (int i = 0; i < strs.length; i++) {
 			longs[i] = Long.parseLong(strs[i]);
 		}
 
 		return longs;
 	}
 
 	/**
 	 * 按照给定的 by 分割字符串，然后转化成int数组。
 	 * 
 	 * @param source
 	 * @param by
 	 * @return
 	 */
 	public static int[] splitToInt(String source, String by) {
 
 		if (source == null || by == null)
 			return null;
 
 		String[] strs = source.split(by);
 		int[] ints = new int[strs.length];
 		for (int i = 0; i < strs.length; i++)
 			ints[i] = Integer.parseInt(strs[i]);
 
 		return ints;
 	}
 
 	/**
 	 * 格式化时间 yyyy-MM-dd HH:mm:ss
 	 * 
 	 * @param date
 	 * @return
 	 */
 	public static String formatTime(Date date) {
 		return formatTime(null, date);
 	}
 
 	/**
 	 * 格式化时间
 	 * 
 	 * @param format
 	 *            格式，默认yyyy-MM-dd HH:mm:ss
 	 * @param date
 	 * @return
 	 */
 	public static String formatTime(String format, Date date) {
 		if (format == null) {
 			format = "yyyy-MM-dd HH:mm:ss";
 		}
 
 		String time = new java.text.SimpleDateFormat(format).format(date);
 		return time;
 	}
 
 	public static Date newDate(String pattern, String time) {
 		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
 		try {
 			return sdf.parse(time);
 		} catch (ParseException e) {
 			throw new RuntimeException();
 		}
 	}
 
 	public static String formatStr(String format, Object... args) {
 		return String.format(format, args);
 	}
 
 	public static boolean isValidEmail(String mail) {
 		String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
 		Pattern p = Pattern.compile(regex);
 		Matcher m = p.matcher(mail);
 
 		return m.find();
 	}
 	
 	/**
 	 * 
 	 * 1204,K => 1K
 	 * @date 2012-12-16 上午10:13:01
 	 * @param size
 	 * @param format K,M,G,T
 	 * @return
 	 */
 	public static String formatFileSize(long size, String format) {
 		
 		if (format.equals("K")){
 			return size / 1024.0 + "K";
 		}
 		
 		if (format.equals("M")){
 			return size / 1024.0 / 1024.0 + "M";
 		}
 		
 		if (format.equals("G")){
 			return size / 1024.0 / 1024.0 / 1024.0 + "G";
 		}
 		
 		if (format.equals("T")){
 			return size / 1024.0 / 1024.0 / 1024.0 / 1024.0 + "T";
 		}
 		
 		return size + "B";
 	}
 	
 	public static long parseFileSize(String _size){
 		if (_size.toUpperCase().endsWith("K")){
 			long size = Long.parseLong(_size.toUpperCase().replace("K", ""));
 			return size * 1024;
 		}
 		
 		if (_size.toUpperCase().endsWith("M")){
 			long size = Long.parseLong(_size.toUpperCase().replace("M", ""));
 			return size * 1024 * 1024;
 		}
 		
 		if (_size.toUpperCase().endsWith("G")){
 			long size = Long.parseLong(_size.toUpperCase().replace("G", ""));
 			return size * 1024 * 1024 * 1024;
 		}
 		
 		return Long.parseLong(_size);
 	}
 	
 	public static String replaceChinese2Utf8(String source) {
 		String str = source;
 		try {
 			Pattern pattern = Pattern.compile(RegexList.has_chinese_regexp);
 
 			Matcher matcher = pattern.matcher(source);
 			while (matcher.find()) {
 				String g = matcher.group();
 				str = source.replace(g, URLEncoder.encode(g, "utf-8"));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return str;
 	}
 	
 
 	public static String parsePropValue(String source) {
 		return parsePropValue(source, null);
 	}
 
 	public static String parsePropValue(String source, String _propId) {
 		if (_propId != null) 
 			return parseSinglePropVarable(source, _propId);
 		
 		Pattern pattern = Pattern.compile(RegexList.property_regexp);
 		Matcher matcher = pattern.matcher(source);
 		if (!matcher.find())
 			return source;
 		
 		String g = matcher.group();
 		String suffix = source.replace(g, "");
 		String[] props = g.replace("${", "").replace("}", "").split("\\.");
 		String prefix = null;
 		if (props.length == 2) {
 			String propId = props[0];
 			String key = props[1];
 			if ("global".equals(propId)) {
 				prefix = Props.get(key);
 			} else {
 				prefix = Props.getMap(propId).get(key);
 			}
 
 			source = prefix + suffix;
 		}
 
 		return source;
 
 	}
 
 	public static String parseSinglePropVarable(String source, String propId) {
 		Pattern pattern = Pattern.compile(RegexList.property_single_regexp);
 		Matcher matcher = pattern.matcher(source);
 		if (!matcher.find())
 			return source;
 
 		String g = matcher.group();
 		String suffix = source.replace(g, "");
 		String key = g.replace("${", "").replace("}", "");
 		String prefix = null;
 		if ("global".equals(propId)) {
 			prefix = Props.get(key);
 		} else {
 			prefix = Props.getMap(propId).get(key);
 		}
 
 		if (prefix != null)
 			source = prefix + suffix;
 
 		return source;
 	}
 
 	public static Date strToDate(String source, String pattern) {
 		Date date = null;
 		SimpleDateFormat format = new SimpleDateFormat(pattern);
 		try {
 			date = format.parse(source);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		return date;
 	}
 
 	public static String dateToStr(Date source, String pattern) {
 		String result = null;
 		SimpleDateFormat format = new SimpleDateFormat(pattern);
 		result = format.format(source);
 		return result;
 	}
 
 	/**
 	 * 将字符串转换为数字
 	 * 
 	 * @param source
 	 *            被转换的字符串
 	 * @return int 型值
 	 */
 	public static int strToInt(String source) {
 		int result = 0;
 		try {
 			result = Integer.parseInt(source);
 		} catch (NumberFormatException e) {
 			result = 0;
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	/**
 	 * 判断是否是数字
 	 * 
 	 * @param str
 	 * @return
 	 */
 	public static boolean isNumeric(String str) {
 		try {
 			Integer.parseInt(str);
 			return true;
 		} catch (Exception ex) {
 			return false;
 		}
 	}
 
 	/**
 	 * @功能 将字符串首字母转为大写
 	 * @param str
 	 *            要转换的字符串
 	 * @return String 型值
 	 */
 	public static String toUpCaseFirst(String str) {
 		if (str == null || "".equals(str)) {
 			return str;
 		} else {
 			char[] temp = str.toCharArray();
 			temp[0] = str.toUpperCase().toCharArray()[0];
 			str = String.valueOf(temp);
 		}
 
 		return str;
 	}
 
 	public static String toLowCaseFirst(String str) {
 		if (str == null || "".equals(str)) {
 			return str;
 		} else {
 			char[] temp = str.toCharArray();
 			temp[0] = str.toLowerCase().toCharArray()[0];
 			str = String.valueOf(temp);
 		}
 
 		return str;
 	}
 
 	/**
 	 * 批量将英文字符串首字母转为大写
 	 * 
 	 * @param str
 	 *            要转换的字符串数组
 	 * @return 字符数组
 	 */
 	public static String[] toUpCaseFirst(String[] str) {
 		if (str == null || str.length == 0) {
 			return str;
 		} else {
 			String[] result = new String[str.length];
 			for (int i = 0; i < result.length; ++i) {
 				result[i] = CommonUtil.toUpCaseFirst(str[i]);
 			}
 
 			return result;
 		}
 	}
 
 	public static String[] toLowCaseFirst(String[] str) {
 		if (str == null || str.length == 0) {
 			return str;
 		} else {
 			String[] result = new String[str.length];
 			for (int i = 0; i < result.length; ++i) {
 				result[i] = CommonUtil.toLowCaseFirst(str[i]);
 			}
 
 			return result;
 		}
 	}
 
 	public static String hump2ohter(String param, String aother) {
 		char other = aother.toCharArray()[0];
 		Pattern p = Pattern.compile("[A-Z]");
 		if (param == null || param.equals("")) {
 			return "";
 		}
 		StringBuilder builder = new StringBuilder(param);
 		Matcher mc = p.matcher(param);
 		int i = 0;
 		while (mc.find()) {
 			builder.replace(mc.start() + i, mc.end() + i, other
 					+ mc.group().toLowerCase());
 			i++;
 		}
 
 		if (other == builder.charAt(0)) {
 			builder.deleteCharAt(0);
 		}
 
 		return builder.toString();
 	}
 
 	/**
 	 * @功能 根据给定的regex正则表达式，验证给定的字符串input是否符合
 	 * @param input
 	 *            需要被验证的字符串
 	 * @param regex
 	 *            正则表达式
 	 * @return boolean 型值
 	 */
 	public static boolean verifyWord(String input, String regex) {
 		if (input == null) {
 			input = "";
 		}
 
 		if (regex == null) {
 			regex = "";
 		}
 
 		Pattern p = Pattern.compile(regex);
 		Matcher m = p.matcher(input);
 		boolean flag = m.matches();
 
 		return flag;
 	}
 
 	/**
 	 * @功能 转换字符串中属于HTML语言中的特殊字符
 	 * @param source
 	 *            为要转换的字符串
 	 * @return String型值
 	 */
 	public static String changeHTML(String source) {
 		String s0 = source.replace("\t\n", "<br />"); // 转换字符串中的回车换行
 		String s1 = s0.replace("&", "&amp;"); // 转换字符串中的"&"符号
 		String s2 = s1.replace(" ", "&nbsp;"); // 转换字符串中的空格
 		String s3 = s2.replace("<", "&lt;"); // 转换字符串中的"<"符号
 		String s4 = s3.replace(">", "&gt;"); // 转换字符串中的">"符号
 		String s5 = s4.replace("\"", "&quot;"); // 转换字符串中的"\""符号
 		String s6 = s5.replace("'", "&apos;"); // 转换字符串中的"'"符号
 		return s6;
 	}
 
 	/**
 	 * 将某些字符转为HTML标签。
 	 * 
 	 * @param source
 	 * @return
 	 */
 	public static String toHTML(String source) {
 		String s1 = source.replace("&amp;", "&"); // 转换字符串中的"&"符号
 		String s2 = s1.replace("&nbsp;", " "); // 转换字符串中的空格
 		String s3 = s2.replace("&lt;", "<"); // 转换字符串中的"<"符号
 		String s4 = s3.replace("&gt;", ">"); // 转换字符串中的">"符号
 		String s5 = s4.replace("<br />", "\t\n"); // 转换字符串中的回车换行
 		String s6 = s5.replace("&quot;", "\""); // 转换字符串中的"\""符号
		String s7 = s6.replace("&apos;", "'"); // 转换字符串中的"'"符号
 
 		return s7;
 	}
 
 	/**
 	 * @功能 取得当前时间,给定格式
 	 * @return
 	 */
 	public static String getNowTime(String format, Locale loc) {
 		if (format == null) {
 			format = "yyyy-MM-dd HH:mm:ss";
 		}
 
 		if (loc == null)
 			return new java.text.SimpleDateFormat(format).format(java.util.Calendar.getInstance().getTime());
 		
 		return new java.text.SimpleDateFormat(format, loc).format(java.util.Calendar.getInstance().getTime());
 	}
 	
 	public static String getNowTime(String format){
 		return getNowTime(format, null);
 	}
 
 	/**
 	 * @功能 取得当前时间
 	 * @return
 	 */
 	public static String getNowTime() {
 		return getNowTime(null);
 	}
 
 	/**
 	 * @功能 转换字符编码
 	 * @param str
 	 *            为要转换的字符串
 	 * @return String 型值
 	 */
 	public static String toEncoding(String str, String encoding) {
 		if (str == null) {
 			str = "";
 		}
 		try {
 			str = new String(str.getBytes("ISO-8859-1"), encoding);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 
 		return str;
 	}
 
 	/**
 	 * 使一个数组的所有元素被一个“分隔符”串联起来组成一条字符串
 	 * 
 	 * @param format
 	 * @return
 	 */
 	public static String cutArrayBySepara(String[] source, String separator) {
 		if (source == null || source.length == 0 || separator == null) {
 			return null;
 		}
 		StringBuffer result = new StringBuffer();
 		for (int i = 0; i < source.length; ++i) {
 			if (i == source.length - 1) {
 				result.append(source[i]);
 			} else {
 				result.append(source[i]).append(separator);
 			}
 		}
 
 		return result.toString();
 	}
 
 	public static boolean isNullOrEmpty(Object obj) {
 		return obj == null || "".equals(obj.toString());
 	}
 
 	public static String toString(Object obj) {
 		if (obj == null)
 			return "null";
 		return obj.toString();
 	}
 
 	public static String join(Collection<?> s, String delimiter) {
 		StringBuffer buffer = new StringBuffer();
 		Iterator<?> iter = s.iterator();
 		while (iter.hasNext()) {
 			buffer.append(iter.next());
 			if (iter.hasNext()) {
 				buffer.append(delimiter);
 			}
 		}
 		return buffer.toString();
 	}
 
 	/**
 	 * 将文件名中的汉字转为UTF8编码的串,以便下载时能正确显示另存的文件名.
 	 * 
 	 * @param s
 	 *            原文件名
 	 * @return 重新编码后的文件名
 	 */
 	public static String toUtf8String(String s) {
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < s.length(); i++) {
 			char c = s.charAt(i);
 			if (c >= 0 && c <= 255) {
 				sb.append(c);
 			} else {
 				byte[] b;
 				try {
 					b = Character.toString(c).getBytes("utf-8");
 				} catch (Exception ex) {
 					b = new byte[0];
 				}
 				for (int j = 0; j < b.length; j++) {
 					int k = b[j];
 					if (k < 0)
 						k += 256;
 					sb.append("%" + Integer.toHexString(k).toUpperCase());
 				}
 			}
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * 将utf-8编码的汉字转为中文
 	 * 
 	 * @param str
 	 * @return
 	 */
 	public static String uriDecoding(String str) {
 		String result = str;
 		try {
 			result = URLDecoder.decode(str, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	/**
 	 * 获取客户端IP
 	 */
 	public static String getIpAddr(HttpServletRequest request) {
 		String ip = request.getHeader("X-Forwarded-For");
 		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
 			ip = request.getHeader("Proxy-Client-IP");
 		}
 		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
 			ip = request.getHeader("WL-Proxy-Client-IP");
 		}
 		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
 			ip = request.getHeader("HTTP_CLIENT_IP");
 		}
 		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
 			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
 		}
 		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
 			ip = request.getRemoteAddr();
 		}
 		return ip;
 	}
 
 	public static String getExceptionString(Throwable e) {
 		if (e == null)
 			return "";
 		
 		StringWriter strWriter = new StringWriter();
 		PrintWriter writer = new PrintWriter(strWriter, true);
 		e.printStackTrace(writer);
 		StringBuffer sb = strWriter.getBuffer();
 		return "cause by: \n\t" + sb.toString();
 	}
 
 	@Deprecated
 	public static String getStack(StackTraceElement[] stes) {
 		StringBuilder sb = new StringBuilder();
 		for (StackTraceElement ste : stes) {
 			if (ste != null)
 				sb.append("\n").append(ste.toString());
 		}
 
 		return sb.toString();
 	}
 }
