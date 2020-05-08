 package com.xiuhao.commons.lang;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 
 public class HtmlKit {
 	private static final char space = ' ';
 
 	public static String addHtmlATag(String content) {
 		StringBuilder sb = new StringBuilder();
 		int startIndex = 0;
 		while (startIndex < content.length()) {
 			int indexOfUrl = StringUtils.indexOf(content, "http", startIndex);
 			if (indexOfUrl < 0) { // can't find url next, break and return;
 				sb.append(content.substring(startIndex));
 				break;
 			}
 			if (indexOfUrl != 0) {
 				sb.append(content.substring(startIndex, indexOfUrl));
 			}
 			int indexOfSpace = StringUtils.indexOf(content, space, indexOfUrl);
 			if (indexOfSpace < 0) { // exists url, and reach content end
 				String url = content.substring(indexOfUrl);
 				appendUrl(sb, url);
 				break;
 			} else {
 				String url = content.substring(indexOfUrl, indexOfSpace); // contain url & space
 				appendUrl(sb, url);
 				sb.append(space);
 				startIndex = indexOfSpace + 1;
 			}
 		}
 		return sb.toString();
 	}
 
 	private static void appendUrl(StringBuilder contentBuilder, String url) {
 		try {
 			new URL(url);
 		} catch (MalformedURLException e) {
 			contentBuilder.append(url);
 			return;
 		}
 		contentBuilder.append("<a href=\"");
 		contentBuilder.append(url);
 		contentBuilder.append("\">");
 		contentBuilder.append(url);
 		contentBuilder.append("</a>");
 	}
 
 	/**
 	 * 得到网页中图片的地址
 	 */
 	public static List<String> getImageSrcs(String htmlStr) {
 		return getImageSrcs(htmlStr, null, null);
 	}
 
 	/**
 	 * 得到网页中图片的地址
 	 */
 	public static List<String> getImageSrcs(String htmlStr, String hostname, String path) {
 		if (hostname == null) {
 			hostname = "";
 		}
 
 		if (path == null) {
 			path = "";
 		}
 
 		String img = "";
 		Pattern p_image;
 		Matcher m_image;
 		List<String> pics = null;
 
 		String regEx_img = "<img.*src=(.*?)[^>]*?>"; // 图片链接地址
 		p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
 		m_image = p_image.matcher(htmlStr);
 		while (m_image.find()) {
 			img = img + "," + m_image.group();
 			Matcher m = Pattern.compile("src=\"?(.*?)(\"|>|\\s+)").matcher(img); // 匹配src
 			while (m.find()) {
 				if (pics == null) {
 					pics = new ArrayList<String>();
 				}
 				String image = m.group(1);
 				if (StringUtils.startsWith(image, "http://") || StringUtils.startsWith(image, "https://")) {
 				} else {
 					if (StringUtils.startsWith(image, "/")) {
 						image = hostname + image;
 					} else if (StringUtils.isNotEmpty(hostname) || StringUtils.isNotEmpty(path)) {
 						image = hostname + path + '/' + image;
 					}
 				}
 				pics.add(image);
 			}
 		}
 		return pics;
 	}
 
 }
