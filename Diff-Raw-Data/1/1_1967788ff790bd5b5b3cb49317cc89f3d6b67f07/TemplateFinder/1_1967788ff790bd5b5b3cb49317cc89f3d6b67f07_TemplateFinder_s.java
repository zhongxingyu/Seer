 package com.ii2d.genthemall.template;
 
 import groovy.util.ConfigObject;
 import groovy.util.ConfigSlurper;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.io.IOUtils;
 
 import com.ii2d.dbase.util.DResourceFinder;
 import com.ii2d.dbase.util.DResourceUtils;
 
 public class TemplateFinder {
 	/**
 	 * the .gt file must have a head like this <%/...../%>
 	 */
 	public static final String HEAD_RE_STR = "^\\s*<%/\\*(.+?)\\*/%>";
 	public static final Pattern HEAD_RE = Pattern.compile(HEAD_RE_STR, Pattern.DOTALL);
 	
 	public static TemplateHolder findToHodler(String path) throws IOException {
 		return findToHodler(DResourceUtils.getDefaultClassLoad(), path);
 	}
 	
 	public static TemplateHolder findToHodler(ClassLoader cl, String path) throws IOException {
 		return new TemplateHolder(find(cl, path));
 	}
 
 	public static List<Template> find(String path) throws IOException {
 		return find(DResourceUtils.getDefaultClassLoad(), path);
 	}
 	public static List<Template> find(ClassLoader cl, String path) throws IOException {
 		List<Template> tList = new ArrayList<Template>();
 		List<String> templatePaths = DResourceFinder.find(cl, path, new String[]{".*\\.gt"}, null);
 		for(String p: templatePaths) {
 			Template tmp = _makeTemplate(p);
 			if(tmp != null) {
 				tList.add(tmp);
 			}
 		}
 		return tList;
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private static Template _makeTemplate(String path) throws IOException {
 		Template t = new Template();
 		String templateText = IOUtils.toString(DResourceUtils.getResourceAsStream(path));
 		Matcher m = HEAD_RE.matcher(templateText);
 		if(m.find()) {
 			ConfigObject co = new ConfigSlurper().parse(m.group(1));
 			try {
 				Iterator<Map.Entry> it = co.entrySet().iterator();
 				while(it.hasNext()) {
 					Map.Entry e = it.next();
 					if(e.getKey() instanceof String) {
 						BeanUtils.setProperty(t, (String)e.getKey(), e.getValue());
 					}
 				}
 				t.setTemplateText(templateText.substring(m.end()));
 				return t;
 			}catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 }
