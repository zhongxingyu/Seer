 package com.akjava.lib.common.utils;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class TemplateUtils {
 	/**
 	 * very simple way just replace ${key} to value;
 	 * @param template
 	 * @param map
 	 * @return
 	 */
 	public static String createText(String template,Map<String,String> map){
 		String result=template;
 		for(String key:map.keySet()){
 			if(map.get(key)!=null){
 			result=result.replace("${"+key+"}", map.get(key));
 			}else{
 				System.out.println("TemplateUtils.createText()#null value:"+key);
 			}
 		}
 		return result;
 	}
 	/**
 	 * dont insert null
 	 * @param template
 	 * @param map
 	 * @return
 	 */
 	public static String createAdvancedText(String template,Map<String,String> map){
 		String result=template;
 		for(String key:map.keySet()){
 			if(map.get(key)!=null){
 			result=result.replace("${"+key+"}", map.get(key));
 			result=result.replace("${u+"+key+"}", ValuesUtils.toUpperCamel(map.get(key)));
 			result=result.replace("${l+"+key+"}", ValuesUtils.toLowerCamel(map.get(key)));
 			}else{
 				System.out.println("TemplateUtils.createText()#null value:"+key);
 			}
 		}
 		return result;
 	}
 	
 	public static String createText(String template,String value){
 		Map<String,String> map=new HashMap<String, String>();
 		map.put("value", value);
 		return createText(template,map);
 	}
 	
 	
 }
