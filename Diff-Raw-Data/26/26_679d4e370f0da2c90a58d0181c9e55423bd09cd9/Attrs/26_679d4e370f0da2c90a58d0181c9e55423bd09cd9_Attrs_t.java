 package org.eweb4j.util.xml;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 
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
 		System.out.println("<div>This is div.<img /></div><p>This is p.<ul><li>This is li.<a>This is link.</a></li></ul></p>".equals(rs));
 		
 		//删除所有标签的style属性和alt属性
 		String rs2 = Attrs.me().xml(xml).rm("style", "alt").Tags().ok();
 		System.out.println("<div>This is div.<img src='http://www.baidu.com/logo.gif' /></div><p>This is p.<ul><li>This is li.<a href='http://www.baidu.com'>This is link.</a></li></ul></p>".equals(rs2));
 		
 		//删除img标签的src、alt属性，删除div、p标签的style属性
 		String rs3 = Attrs.me().xml(xml).tag("img").rm("src", "alt").tag("div", "p").rm("style").ok();
 		System.out.println("<div>This is div.<img /></div><p>This is p.<ul><li>This is li.<a href='http://www.baidu.com'>This is link.</a></li></ul></p>".equals(rs3));
 	}
 	
 	private String xml = null;//需要操作的xml文本
 	private Collection<String> currentTag = new HashSet<String>();//当前指定的标签
 	
 	/**
 	 * 构造一个Attrs实例对象
 	 * @date 2013-1-7 下午03:54:48
 	 * @return
 	 */
 	public static Attrs me(){
 		return new Attrs();
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
 	
 	/**
 	 * 指定当前的标签
 	 * @date 2013-1-7 下午03:55:53
 	 * @param tag
 	 * @return
 	 */
 	public Attrs tag(String tag){
 		this.currentTag.add(tag);
 		return this;
 	}
 	
 	/**
 	 * 指定当前的标签，多个
 	 * @date 2013-1-7 下午03:56:11
 	 * @param tag
 	 * @return
 	 */
 	public Attrs tag(String... tag){
 		this.currentTag.addAll(Arrays.asList(tag));
 		return this;
 	}
 	
 	/**
 	 * 删除当前标签的指定属性
 	 * @date 2013-1-7 下午03:56:21
 	 * @param attr
 	 * @return
 	 */
 	public Attrs rm(String attr){
 		xml = removeXmlTagAttr(xml, currentTag, Arrays.asList(attr));
 		currentTag.clear();
 		return this;
 	}
 	
 	/**
 	 * 删除当前标签的指定属性
 	 * @date 2013-1-7 下午03:56:41
 	 * @param attr
 	 * @return
 	 */
 	public Attrs rm(String... attr){
 		xml = removeXmlTagAttr(xml, currentTag, Arrays.asList(attr));
 		currentTag.clear();
 		return this;
 	}
 	
 	/**
 	 * 返回已处理过的XML文本
 	 * @date 2013-1-7 下午03:56:50
 	 * @return
 	 */
 	public String ok(){
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
 	public static String removeXmlTagAttr(String xml, Collection<String> tags, Collection<String> attrs){
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
 	public static String removeXmlTagAttr(String xml, String tag, Collection<String> attrs){
//		String fmt = "(?<=<%s{1,255})\\s+%s=[\"'][^'\"]*[\"']";
		final String fmt = "(?<=<%s{1,255})\\s+%s=([\"'=])[^=]*\\1";
 		if (tag == null || tag.trim().length() == 0)
 			tag = ".";//all tags
 		
 		if (attrs == null || attrs.size() == 0)
 			return xml.replaceAll(String.format(fmt, tag, "\\w+"), "");//all attributes
 		
 		for (String attr : attrs){
 			if (attr == null || attr.trim().length() == 0)
 				continue;
 			
 			String regex = String.format(fmt, tag, attr);
 			xml = xml.replaceAll(regex, "");
 		}
 		
 		return xml;
 	}
 	
	public static final String regex(String tag, String attr){
		String fmt = "(?<=<%s{1,255})\\s+%s=([\"'=])[^=]*\\1";
		String regex = String.format(fmt, tag, attr);
		return regex;
	}
	
 }
