 package org.eweb4j.util.xml;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * 强大的XML标签过滤【通过正则】
  * @author weiwei l.weiwei@163.com
  * @date 2013-1-5 下午09:44:24
  */
 public class Tags {
 	
 	public static void main(String[] args){
 		//XML文本
 		String xml = "<div>This is div.</div><p>This is p.<ul><li>This is li.<a href='http://www.baidu.com'>This is link.</a></li></ul></p>";
 		//删除所有标签
 		String rs = Tags.me().xml(xml).rm().ok();
 		System.out.println("This is div.This is p.This is li.This is link.".equals(rs));
 		
 		//删除a、div标签，并且清空它们标签内的所有内容
 		String rs2 = Tags.me().xml(xml).rm("div", "a").empty().ok();
 		System.out.println("<p>This is p.<ul><li>This is li.</li></ul></p>".equals(rs2));
 		
 		//保留p、a标签，其他都删除
 		String kpRs = Tags.me().xml(xml).kp("p", "a").ok();
 		System.out.println("This is div.<p>This is p.This is li.<a href='http://www.baidu.com'>This is link.</a></p>".equals(kpRs));
 		
 		//删除p、a标签，其他保留
 		String rmRs = Tags.me().xml(xml).rm("p", "a").ok();
 		System.out.println("<div>This is div.</div>This is p.<ul><li>This is li.This is link.</li></ul>".equals(rmRs));
 		
 		//Tags和Attrs两个类是可以同时使用的，切换的时候，上一个的执行结果作为下一个的参数继续处理
 		//删除div、ul、li标签然后删除a标签的href属性
 		String sRs = Tags.me().xml(xml).rm("div", "ul","li").Attrs().tag("a").rm("href").ok();
 		System.out.println("This is div.<p>This is p.This is li.<a>This is link.</a></p>".equals(sRs));
 		
 		//删除所有标签的href属性，然后保留div、a标签，其他标签都删除
 		String sRs2 = Attrs.me().xml(xml).rm("href").Tags().kp("div", "a").ok();
 		System.out.println("<div>This is div.</div>This is p.This is li.<a>This is link.</a>".equals(sRs2));
 
		
		String html = "<dd class=\"frinfo line_blue\">2013-01-07 08:40:03      <a style=\"font-weight:bold;padding:5px 0px 5px 20px;background:url('http://www.2cto.com/statics/images/icon/user_comment.png') left center no-repeat\" href=\"#comment_iframe\">我来说两句 </a>    来源：雨简 的BLOG    </dd>";
		
//		List<String> tag = Tags.findByRegex(html, Tags.xmlTagsRegex("a"));
//		String regex = tag.get(0) + ".*" + tag.get(1);
//		System.out.println(regex);
//		html = html.replaceAll(regex, "");
//		System.out.println(html);
		System.out.println(Attrs.me().xml(html).tag("a").rm().Tags().rm("a").empty().exe().rm("dd").ok());
//		System.out.println(Attrs.regex("a", "style"));
 	}
 	
 	private String xml = null;//需要操作的xml文本
 	private Boolean empty = false;//是否清空标签内的内容
 	private Collection<String> kps = new HashSet<String>();//保留的标签缓存
 	private Collection<String> rms = new HashSet<String>();//删除的标签缓存
 	
 	/**
 	 * 构造一个Tags实例对象
 	 * @date 2013-1-7 下午03:53:27
 	 * @return
 	 */
 	public static Tags me(){
 		return new Tags();
 	}
 	
 	/**
 	 * 设置需要操作的XML文本
 	 * @date 2013-1-7 下午03:53:14
 	 * @param xml
 	 * @return
 	 */
 	public Tags xml(String xml){
 		this.xml = xml;
 		return this;
 	}
 	
 	/**
 	 * 切换到Attrs，切换之前会执行清除标签操作
 	 * @date 2013-1-7 下午03:52:43
 	 * @return
 	 */
 	public Attrs Attrs(){
 		exe();
 		return Attrs.me().xml(xml);
 	}
 	
 	/**
 	 * 清空当前指定标签内的所有内容
 	 * @date 2013-1-7 下午03:52:09
 	 * @return
 	 */
 	public Tags empty(){
 		this.empty = true;
 		return this;
 	}
 	
 	/**
 	 * 删除所有标签【保留标签里的内容】
 	 * @date 2013-1-7 下午03:51:50
 	 * @return
 	 */
 	public Tags rm(){
 		xml = cleanXmlTags(xml, false);
 		return this;
 	}
 	
 	/**
 	 * 
 	 * 删除指定标签
 	 * @date 2013-1-7 下午03:51:31
 	 * @param tag
 	 * @return
 	 */
 	public Tags rm(String tag){
 		this.rms.add(tag);
 		return this;
 	}
 	
 	/**
 	 * 删除标签
 	 * @date 2013-1-7 下午03:51:16
 	 * @param tag 不给定则删除所有
 	 * @return
 	 */
 	public Tags rm(String... tag){
 		this.rms.addAll(Arrays.asList(tag));
 		return this;
 	}
 	
 	/**
 	 * 保留给定标签,其他删除
 	 * @date 2013-1-7 下午03:50:52
 	 * @param tag
 	 * @return
 	 */
 	public Tags kp(String tag){
 		this.kps.add(tag);
 		return this;
 	}
 	
 	/**
 	 * 保留给定标签,其他删除
 	 * @date 2013-1-7 下午03:50:41
 	 * @param tag
 	 * @return
 	 */
 	public Tags kp(String... tag){
 		this.kps.addAll(Arrays.asList(tag));
 		return this;
 	}
 	
 	/**
 	 * 执行标签的清除
 	 * @date 2013-1-7 下午03:50:16
 	 * @return
 	 */
 	public Tags exe(){
 		if (!this.rms.isEmpty()){
 			xml = cleanXmlTags(xml, this.empty, rms.toArray(new String[]{}));
 			this.rms.clear();
 			this.empty = false;
 		} if (!this.kps.isEmpty()){
 			xml = cleanOtherXmlTags(xml, kps.toArray(new String[]{}));
 			this.kps.clear();
 		}
 		
 		return this;
 	}
 	
 	/**
 	 * 返回处理后的字符串
 	 * @date 2013-1-7 下午03:49:58
 	 * @return
 	 */
 	public String ok(){
 		exe();
 		return xml;
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
 				String regex = tag.get(0) + ".*" + tag.get(1);
 				html = html.replaceAll(regex, "");
 			}
 			return html;
 		}
 		
 		return html.replaceAll(xmlTagsRegex(delTags), "");
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
 	
 	public static List<String> findByRegex(String input, String regex){
 		List<String> result = new ArrayList<String>();
 		Pattern p = Pattern.compile(regex);
 		Matcher m = p.matcher(input);
 		while(m.find()){
 			result.add(m.group());
 		}
 		
 		if (result.isEmpty()) return null;
 		
 		return result;
 	}
 }
