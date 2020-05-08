 package org.eweb4j.util.xml;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import org.eweb4j.util.CommonUtil;
 
 /**
  * 强大的XML标签属性过滤【通过正则】
  * @author weiwei l.weiwei@163.com
  * @date 2013-1-5 下午09:44:24
  */
 public class Attrs {
 
 	public static void main(String[] args){
 		//XML文本
 		String xml = "<div style='width:250; height:auto;'>This is div.<img src='http://www.baidu.com/logo.gif' alt='This is img' /></div><p style='padding:5px;'>This is p.<ul><li>This is li.<a href='http://www.baidu.com'>This is link.</a></li></ul></p>";
 		
 		//删除所有标签的所有属性
 		String rs = Attrs.me().xml(xml).rm().ok();
 		System.out.println("<div >This is div.<img   /></div><p >This is p.<ul><li>This is li.<a >This is link.</a></li></ul></p>".equals(rs));
 		
 		//删除所有标签的style属性和alt属性
 		String rs2 = Attrs.me().xml(xml).rm("style", "alt").exe().Tags().ok();
 		System.out.println("<div >This is div.<img src='http://www.baidu.com/logo.gif'  /></div><p >This is p.<ul><li>This is li.<a href='http://www.baidu.com'>This is link.</a></li></ul></p>".equals(rs2));
 		
 		//删除img标签的src、alt属性，删除div、p标签的style属性
 		String rs3 = Attrs.me().xml(xml).tag("img").rm("src", "alt").exe().tag("div", "p").rm("style").ok();
 		System.out.println("<div >This is div.<img   /></div><p >This is p.<ul><li>This is li.<a href='http://www.baidu.com'>This is link.</a></li></ul></p>".equals(rs3));
 	}
 	
 	private String xml = null;//需要操作的xml文本
 	private Collection<String> tags = new HashSet<String>();
 	private Collection<String> rms = new HashSet<String>();
 	private Collection<String> kps = new HashSet<String>();
 	
 	/**
 	 * 构造一个Attrs实例对象
 	 * @date 2013-1-7 下午03:54:48
 	 * @return
 	 */
 	public static Attrs me(){
 		return new Attrs();
 	}
 	
 	public Attrs filter(Filter filter){
 		final String fmt = "(?<=<%s{1,999999999} [\\s\\S]{0,999999999})%s=([\"'])[^=]*\\1";
 		for (String tag : this.tags) {
 			final String regex = String.format(fmt, tag, "\\w+");
 			List<String> attrs = CommonUtil.findByRegex(xml, regex);
			if (attrs == null) continue;
 			for (String attr : attrs) {
 				String name = CommonUtil.findOneByRegex(attr, ".*(?=\\=)").trim();
 				String value = CommonUtil.findOneByRegex(attr, "(?<=\\=).*").trim();
 				String new_value = filter.onAttr(tag, name, value);
 				xml = xml.replace(attr, " "+name+"="+new_value+" ");
 			}
 		}
 		
 		return this;
 	}
 	
 	/**
 	 * 设置要操作的XML文本
 	 * @date 2013-1-7 下午03:55:05
 	 * @param xml
 	 * @return
 	 */
 	public Attrs xml(String xml){
 		this.xml = xml;
 		return this;
 	}
 	
 	/**
 	 * 切换到Tags
 	 * @date 2013-1-7 下午03:55:14
 	 * @return
 	 */
 	public Tags Tags(){
 		return Tags.me().xml(xml);
 	}
 	
 	/**
 	 * 删除所有标签的所有属性
 	 * @date 2013-1-7 下午03:55:39
 	 * @return
 	 */
 	public Attrs rm(){
 		xml = removeXmlTagAttr(xml, "", null);
 		return this;
 	}
 	
 	public Attrs tag(String tag){
 		this.tags.add(tag);
 		return this;
 	}
 	
 	public Attrs tag(String... tags){
 		this.tags.addAll(Arrays.asList(tags));
 		return this;
 	}
 	
 	/**
 	 * 删除当前标签的指定属性
 	 * @date 2013-1-7 下午03:56:21
 	 * @param attr
 	 * @return
 	 */
 	public Attrs rm(String... attr){
 		this.rms.addAll(Arrays.asList(attr));
 		return this;
 	}
 	
 	/**
 	 * 保留当前标签指定的属性，其他都删除
 	 * @param attr
 	 * @return
 	 */
 	public Attrs kp(String... attr){
 		this.kps.addAll(Arrays.asList(attr));
 		return this;
 	}
 	
 	/**
 	 * 删除当前标签的指定属性
 	 * @date 2013-1-7 下午03:56:21
 	 * @param attr
 	 * @return
 	 */
 	public Attrs rm(String attr){
 		this.rms.add(attr);
 		return this;
 	}
 	
 	/**
 	 * 保留当前标签指定的属性，其他都删除
 	 * @param attr
 	 * @return
 	 */
 	public Attrs kp(String attr){
 		this.kps.add(attr);
 		return this;
 	}
 	
 	public Attrs exe(){
 		if (this.rms != null && !this.rms.isEmpty()) {
 			xml = removeXmlTagAttr(xml, this.tags, this.rms);
 		}
 		if (this.kps != null) {
 			xml = removeOtherXmlTagAttr(xml, this.tags, this.kps);
 		}
 		
 		tags.clear();
 		rms.clear();
 		kps.clear();
 		return this;
 	}
 	
 	/**
 	 * 返回已处理过的XML文本
 	 * @date 2013-1-7 下午03:56:50
 	 * @return
 	 */
 	public String ok(){
 		exe();
 		return xml;
 	}
 	
 	/**
 	 * 删除XML文本里给定标签的属性
 	 * @date 2013-1-7 下午03:57:04
 	 * @param html
 	 * @param tags
 	 * @param attrs
 	 * @return
 	 */
 	public String removeXmlTagAttr(String xml, Collection<String> tags, Collection<String> attrs){
 		if (tags == null || tags.isEmpty())
 			return removeXmlTagAttr(xml, "", attrs);
 		String rs = xml;
 		for (String tag : tags){
 			rs = removeXmlTagAttr(rs, tag, attrs);
 		}
 		return rs;
 	}
 	
 	/**
 	 * 删除XML文本里给定标签的属性
 	 * @date 2013-1-7 下午03:58:04
 	 * @param xml
 	 * @param tag
 	 * @param attrs
 	 * @return
 	 */
 	public String removeXmlTagAttr(String xml, String tag, Collection<String> attrs){
 //		String fmt = "(?<=<%s{1,255})\\s+%s=[\"'][^'\"]*[\"']";
 //		final String fmt = "(?<=<%s{1,255})\\s+%s=([\"'=])[^=]*\\1";
 		final String fmt = "(?<=<%s{1,999999999} [\\s\\S]{0,999999999})%s=([\"'])[^=]*\\1";
 		if (tag == null || tag.trim().length() == 0)
 			tag = "\\w";//all tags
 		
 		if (attrs == null || attrs.size() == 0) {
 			String regex = String.format(fmt, tag, "\\w+");
 			return xml.replaceAll(regex, "");//all attributes
 		}
 		
 		for (String _attr : attrs){
 			String attr = _attr;
 			if (attr == null || attr.trim().length() == 0)
 				continue;
 			String regex = String.format(fmt, tag, attr);
 			List<String> values = CommonUtil.findByRegex(xml, regex);
			if (values == null) continue;
 			for (String _value : values) {
 				String value = _value;
 				xml = xml.replace(value, "");
 			}
 		}
 		
 		return xml;
 	}
 	
 	/**
 	 * 删除除了XML文本里给定标签的属性之外的属性
 	 * @date 2013-1-7 下午03:58:04
 	 * @param xml
 	 * @param tag
 	 * @param attrs
 	 * @return
 	 */
 	public String removeOtherXmlTagAttr(String xml, Collection<String> tags, Collection<String> keeps){
 		final String fmt = "(?<=<%s{1,999999999} [\\s\\S]{0,999999999})%s=([\"'])[^=]*\\1";
 		if (tags == null || tags.isEmpty())
 			tags = Arrays.asList(".");//all tags
 		
 		if (keeps == null || keeps.size() == 0) {
 			return xml;
 		}
 		
 		String regex = String.format(fmt, "\\w", "\\w+");
 		//拿到所有属性
 		List<String> allAttrValues = CommonUtil.findByRegex(xml, regex);
 		if (allAttrValues == null) return xml;
 		for (String attrVal : allAttrValues){
 			if (attrVal == null || attrVal.trim().length() == 0)
 				continue;
 			
 			boolean isKp = false;
 			for (String keep : keeps) {
 				label:for (String tag : tags) {
 					String _regex = String.format(fmt, tag, keep);
 					List<String> keepAttrValues = CommonUtil.findByRegex(xml, _regex);
 					if (keepAttrValues != null && keepAttrValues.contains(attrVal)) {
 						isKp = true;
 						break label;
 					}
 				}
 			}
 			
 			if (isKp)
 				continue;
 			
 			xml = xml.replace(attrVal, "");
 		}
 		
 		return xml;
 	}
 	
 	public static final String regex(String tag, String attr){
 //		String fmt = "(?<=<%s{1,255})\\s+%s=([\"'=])[^=]*\\1";
 		final String fmt = "(?<=<%s{1,999999999} [\\s\\S]{0,999999999})%s=([\"'])[^=]*\\1";
 		String regex = String.format(fmt, tag, attr);
 		return regex;
 	}
 	
 	public static interface Filter{
 		public String onAttr(String tag, String attrName, String attrValue);
 	}
 }
