 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ebayopensource.turmeric.tools.annoparser.context.Context;
 
 
 /**
  * The Class HtmlUtils.
  *
  * @author goraman
  */
 public class HtmlUtils {
 
 	/**
 	 * Gets the anchor tag.
 	 *
 	 * @param name the name
 	 * @param href the href
 	 * @param title the title
 	 * @param value the value
 	 * @return the anchor tag
 	 */
 	public static String getAnchorTag(String name, String href, String title, String value) {
 		String tag = "<a ";
 		if (name != null) {
 			tag += "name='" + name + "' ";
 		} 
 		if (href != null) {
 			tag += "href='" + href + "' ";
 		}
 		if (title != null) {
 			tag += "title='" + title + "' ";
 		}
 		tag += ">";
 		if (value != null) {
 			tag += value;
 		}		
 		tag += "</a>";
 		return tag;
 	}
 	
 	/**
 	 * Gets the anchor tag.
 	 *
 	 * @param name the name
 	 * @param href the href
 	 * @param title the title
 	 * @param value the value
 	 * @param target the target
 	 * @return the anchor tag
 	 * @deprecated Use {@link #getAnchorTag(String,String,String,String,String,String)} instead
 	 */
 	public static String getAnchorTag(String name, String href, String title, String value, String target) {
 		return getAnchorTag(name, href, title, value, target, null);
 	}
 
 	/**
 	 * Gets the anchor tag.
 	 *
 	 * @param name the name
 	 * @param href the href
 	 * @param title the title
 	 * @param value the value
 	 * @param target the target
 	 * @param cssClass the css class
 	 * @return the anchor tag
 	 */
 	public static String getAnchorTag(String name, String href, String title, String value, String target, String cssClass) {
 		String tag = "<a ";
 		if (name != null) {
 			tag += "name='" + name + "' ";
 		} 
 		
 		if(cssClass != null) {
 			tag += "class='" + cssClass + "' ";
 		}
 		if (href != null) {
 			tag += "href='" + href + "' ";
 		}
 		if (title != null) {
 			tag += "title='" + title + "' ";
 		}
 		if (target != null) {
 			tag += "target='" + target + "' ";
 		}		
 		tag += ">";
 		if (value != null) {
 			tag += value;
 		}		
 		tag += "</a>";
 		return tag;		
 	}
 	
 	/**
 	 * Gets the start tags.
 	 *
 	 * @param title the title
 	 * @param currLocFromBase the curr loc from base
 	 * @return the start tags
 	 */
 	public static String getStartTags(String title,String currLocFromBase) {
 		List<String> cssList=new ArrayList<String>();
 		cssList.add("JavaDocDefaultStyle.css");
 		if(Context.getContext().getCssFilePath()!=null && !Utils.isEmpty(Context.getContext().getCssFilePath())){
 			cssList.add("CustomStyle.css");
 		}
		String relPath="./";
 		if(currLocFromBase!=null){
 			String [] folders=currLocFromBase.split("/");
 			for(String folder:folders){
 				relPath=relPath+"../";
 			}
 		}
 		String tag = "<html><head><title>" + title + "</title>";
 		for(String css:cssList){
 			tag += "<link href='"+relPath+"css/"+css+ "' rel='stylesheet' type='text/css'>";
 		}
 		tag += "</head><body>";
 		return tag;
 	}
 	
 	/**
 	 * Gets the end tags.
 	 *
 	 * @return the end tags
 	 */
 	public static String getEndTags() {
 		String tag = "</body></html>";
 		return tag;
 	}
 }
