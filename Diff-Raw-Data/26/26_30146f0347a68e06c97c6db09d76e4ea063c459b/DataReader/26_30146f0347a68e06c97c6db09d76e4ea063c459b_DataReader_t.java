 package com.example.xpsearchliu.util;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class DataReader {
 
 
     public static List<Map> readXML(String name) {
     	URL dataPath = DataReader.class.getClassLoader().getResource("xpsearchliu/sampledata/"+name);
         List<Map> list = new ArrayList();
         try {
         	 SAXParserFactory factory = SAXParserFactory.newInstance();  
              SAXParser parser = factory.newSAXParser();  
              DataReader.SaxParseService handler = new DataReader().new SaxParseService();  
              parser.parse(dataPath.toString(),handler);
              list =  handler.getResult();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         }
         return list;
     }
     
 	class SaxParseService extends DefaultHandler {
 		private  List<Map> list = new ArrayList();
 		@Override
 		public void startElement(String uri, String localName, String qName,
 				Attributes attributes) throws SAXException {
 			Map map = new HashMap();
 			if(qName.equals("row")){
 				for(int i = 0,j=attributes.getLength();i<j;i++){
 					map.put(attributes.getQName(i).toLowerCase(),getString(attributes.getValue(i)));
 				}
 				list.add(map);
 			}
 		}
 		
 		public List<Map> getResult(){
 			return list;
 		}
 	}
 	
    private static String getString(Object o){
 	   if(o==null||o.equals("null")){
 		   return "";
 	   }
 	   return o.toString().replaceAll("\'", "''");
    }
    
 }
