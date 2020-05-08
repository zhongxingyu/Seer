 package com.petergeng.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.alibaba.fastjson.JSON;
 import com.alibaba.fastjson.serializer.SerializerFeature;
 import com.petergeng.data.DataType;
 
 public class Util {
 	public static String cgiBegin = "(";
 	public static String cgiEnd = ")";
 	public static String configPrix = ".config";
 	public static String dataPrix = ".htm";
	private static StringBuffer sb=new StringBuffer();
 	public static String getLineSeparator(){
 		String desktop = System.getProperty("sun.desktop");
 		if("windows".equals(desktop)){
 			return "\r\n";
 		}
 		return "\n";
 	}
 	
 	public static String readFile(String path){
 		try{
 		  File file=new File(path);
 		  if(!file.exists()){
 			  return "";
 		  }
 		  BufferedReader br=new BufferedReader(new FileReader(file));  
 		  String line=null;
 		  while((line=br.readLine())!=null){
 			  sb.append(line);
 		  }
 		}catch(Exception e){
 			e.printStackTrace();
 			return "";
 		}
 		return sb.toString();
 	}
 	
 	public static void writeFile(String path,String filename, String data){
 		try{
 			FileWriter fw = new FileWriter(path + filename);  
 			fw.write(data,0,data.length());  
 			fw.flush();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static String objectToJson(Object data){		
 		return JSON.toJSONString(data, SerializerFeature.UseSingleQuotes);
 	}
 	
 	public static DataType jsonToObject(String json){
 		return JSON.toJavaObject(JSON.parseObject(json), DataType.class);
 	}
 	
 	public static Map jsonToMap(String json){
 		return JSON.toJavaObject(JSON.parseObject(json), Map.class);
 	}
 	
 	public static void main(String[] args) {
 		Util.writeFile("D:/", "test", "hhh");
 		System.out.println(Util.readFile("D:/test.config"));
 		DataType d = new DataType();
 		System.out.println(Util.objectToJson(d));
 		Map m = new HashMap();
 		List list = new ArrayList();
 		Map m2 = new HashMap();
 		m2.put("test", "value");
 		m2.put("test2", 3);
 		list.add(m2);
 		m.put("retcode", 0);
 		m.put("data", list);
 		String txt = Util.objectToJson(m);
 		Map mtt = Util.jsonToMap(txt);
 		System.out.println();
 	}
 }
