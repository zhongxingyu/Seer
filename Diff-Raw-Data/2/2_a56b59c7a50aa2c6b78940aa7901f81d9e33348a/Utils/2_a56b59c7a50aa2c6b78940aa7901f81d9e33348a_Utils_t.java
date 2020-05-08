 package it.bisi;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 public class Utils {
 
 	/**
 	 * @param args
 	 */
 	//public static HashMap hmPercentages = new HashMap();
 	public static ArrayList alPercentages = new ArrayList();
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		System.out.println(getResource("/it/bisi/resources/logo.png"));
 
 	}
 	public static String removeEol(String input){
 		return input.replace("\r","").replace("\n","");
 	}
 	
 	
 	public static boolean isNum(String s) {
 		try {
 		Double.parseDouble(s);
 		}
 		catch (NumberFormatException nfe) {
 		return false;
 		}
 		return true;
 		}
 	
 	public static URL getResource(String path){
 		return Utils.class.getResource(path);
 	}
 	/**
 	 * @param xformLocation
 	 * @param formName
 	 * @param searchQuestion
 	 * @param searchValue
 	 * @return
 	 */
 	
 	public static String getXformLabel(String xformLocation, String formName, String searchQuestion, String searchValue){
 
 		//read xform info and do something with it 
 		// @TODO determine what to do with it
 		//read file and put it in dom-object
 		File fXmlFile = new File(xformLocation);
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder dBuilder = null;
 		try {
 			dBuilder = dbFactory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Document doc = null;
 		try {
 			doc = dBuilder.parse(fXmlFile);
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		//search the label of the 'question'		
 		XPathFactory factory = XPathFactory.newInstance();
 		XPath xpath = factory.newXPath();
 		//labels are descendants (children,grandchildren of /html/body
 		//attribute (ref) contains the searchInput (needs to have the 'name' of the instance defined under model) 
 		//String  searchString="/html/body/descendant::*[@ref='/"+searchQuestion+"']/item[value='1.0']/label";
 		String searchString=null;
 		if (searchValue != null && !searchValue.equals("")){
 			//TODO test escaping &amp; becomes \&amp;?
 			searchString="/html/body/descendant::*[@ref='"+StringEscapeUtils.escapeXml(searchQuestion)+"']/item[value='"+StringEscapeUtils.escapeXml(searchValue)+"']/label";
 			//System.out.println(searchString);
 		} else {
 			searchString="/html/body/descendant::*[@ref='"+StringEscapeUtils.escapeXml(searchQuestion)+"']/label";
 		}
 
 		XPathExpression expr = null;
 		try {
 			expr = xpath.compile(searchString);
 		} catch (XPathExpressionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		Object result=null;
 		try {
 			result = expr.evaluate(doc, XPathConstants.STRING);
 			if (result.equals("")){
 				if (searchValue != null && !searchValue.equals("")){
 					result=searchValue;
 				}else{
 					result=searchQuestion;
 				}
 			}
 		} catch (XPathExpressionException e) {
 			// TODO Auto-generated catch block
 			result=null;
 			e.printStackTrace();
 		}
 		return (String) result;
 	}
 	
 	/**
 	 * put lowest,highest, color, targetvalue in map to use with recodeColorMap 
 	 * 
 	 * @param lowest
 	 * @param highest
 	 * @param color (red, white, yellow, green)
 	 * @param targetValue
 	 * @return 
 	 */
 	
 	public static Map<String, Object> defineRecodeColorMap(Double lowest, Double highest, String color, Double targetValue) {
 		Map<String, Object> returnMap=new HashMap<String,Object>();
 		returnMap.put("lowest",lowest);
 		returnMap.put("highest",highest);
 		returnMap.put("color",color);
 		returnMap.put("targetValue", targetValue);
 		return returnMap; 
 	}
 	
 	/**
 	 * Create map with question types (mean3 to mean 10) with color and targeValue per range of answers
 	 * 
 	 * @param customer
 	 * @return
	 * 	 * ranges are from x to y (incl x, excl y) (except the last category, target_value_3)
 	 */
 	public static HashMap<String, Map<String, Map<String, Object>>> recodeColorMap(String customer) {
 		HashMap<String, Map<String, Map<String, Object>>> returnMap=new HashMap<String,Map<String,Map<String,Object>>>();
 		
 		Map<String, Map<String, Object>> white=new HashMap<String,Map<String,Object>>();
 		white.put("target_value_1",defineRecodeColorMap(new Double (0.0), new Double(10000.0), "white", null));
 		returnMap.put("white", white);
 
 		Map<String, Map<String, Object>> scale2=new HashMap<String,Map<String,Object>>();
 		scale2.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(1.5), "red", new Double (1.0)));
 		scale2.put("target_value_3",defineRecodeColorMap(new Double (1.5), new Double(2.0), "green", new Double (3.0)));
 		returnMap.put("scale2", scale2);
 
 		
 		Map<String, Map<String, Object>> scale3=new HashMap<String,Map<String,Object>>();
 		scale3.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(1.5), "red", new Double (1.0)));
 		scale3.put("target_value_2",defineRecodeColorMap(new Double (1.5), new Double(2.5), "yellow", new Double (2.0)));
 		scale3.put("target_value_3",defineRecodeColorMap(new Double (2.5), new Double(3.0), "green", new Double (3.0)));
 		returnMap.put("scale3", scale3);
 		
 		Map<String, Map<String, Object>> scale4=new HashMap<String,Map<String,Object>>();
 		scale4.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(1.5), "red", new Double (1.0)));
 		scale4.put("target_value_2",defineRecodeColorMap(new Double (1.5), new Double(3.5), "yellow", new Double (2.0)));
 		scale4.put("target_value_3",defineRecodeColorMap(new Double (3.5), new Double(4.0), "green", new Double (3.0)));
 		returnMap.put("scale4", scale4);
 		
 		Map<String, Map<String, Object>> scale5=new HashMap<String,Map<String,Object>>();
 		scale5.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(2.5), "red", new Double (1.0)));
 		scale5.put("target_value_2",defineRecodeColorMap(new Double (2.5), new Double(3.5), "yellow", new Double (2.0)));
 		scale5.put("target_value_3",defineRecodeColorMap(new Double (3.5), new Double(5.0), "green", new Double (3.0)));
 		returnMap.put("scale5", scale5);
 		
 		Map<String, Map<String, Object>> scale6=new HashMap<String,Map<String,Object>>();
 		scale6.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(2.5), "red", new Double (1.0)));
 		scale6.put("target_value_2",defineRecodeColorMap(new Double (2.5), new Double(4.5), "yellow", new Double (2.0)));
 		scale6.put("target_value_3",defineRecodeColorMap(new Double (4.5), new Double(6.0), "green", new Double (3.0)));
 		returnMap.put("scale6", scale6);
 		
 		Map<String, Map<String, Object>> scale7=new HashMap<String,Map<String,Object>>();
 		scale7.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(3.5), "red", new Double (1.0)));
 		scale7.put("target_value_2",defineRecodeColorMap(new Double (3.5), new Double(4.5), "yellow", new Double (2.0)));
 		scale7.put("target_value_3",defineRecodeColorMap(new Double (4.5), new Double(7.0), "green", new Double (3.0)));
 		returnMap.put("scale7", scale7);
 		
 		Map<String, Map<String, Object>> scale8=new HashMap<String,Map<String,Object>>();;
 		scale8.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(3.5), "red", new Double (1.0)));
 		scale8.put("target_value_2",defineRecodeColorMap(new Double (3.5), new Double(5.5), "yellow", new Double (2.0)));
 		scale8.put("target_value_3",defineRecodeColorMap(new Double (5.5), new Double(8.0), "green", new Double (3.0)));
 		returnMap.put("scale8", scale8);
 		
 		Map<String, Map<String, Object>> scale9=new HashMap<String,Map<String,Object>>();
 		scale9.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(3.5), "red", new Double (1.0)));
 		scale9.put("target_value_2",defineRecodeColorMap(new Double (3.5), new Double(6.5), "yellow", new Double (2.0)));
 		scale9.put("target_value_3",defineRecodeColorMap(new Double (6.5), new Double(9.0), "green", new Double (3.0)));
 		returnMap.put("scale9", scale9);
 		
 		Map<String, Map<String, Object>> open10=new HashMap<String,Map<String,Object>>();
 		open10.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(5.5), "red", new Double (1.0)));
 		open10.put("target_value_2",defineRecodeColorMap(new Double (5.5), new Double(7.5), "yellow", new Double (2.0)));
 		open10.put("target_value_3",defineRecodeColorMap(new Double (7.5), new Double(10.0), "green", new Double (3.0)));
 		returnMap.put("open10", open10);
 		
 		if (customer.equals("departmentA")){
 			scale5.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(2.95), "red", new Double (1.0)));
 			scale5.put("target_value_2",defineRecodeColorMap(new Double (2.95), new Double(3.95), "white", new Double (2.0)));
 			scale5.put("target_value_3",defineRecodeColorMap(new Double (3.95), new Double(5.0), "green", new Double (3.0)));
 			returnMap.put("scale5", scale5);
 			
 			open10.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(5.45), "red", new Double (1.0)));
 			open10.put("target_value_2",defineRecodeColorMap(new Double (5.45), new Double(6.45), "yellow", new Double (2.0)));
 			open10.put("target_value_3",defineRecodeColorMap(new Double (6.45), new Double(10.0), "green", new Double (3.0)));
 			returnMap.put("open10", open10);
 		}		
 		if (customer.equals("departmentB")){
 			scale5.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(2.95), "red", new Double (1.0)));
 			scale5.put("target_value_2",defineRecodeColorMap(new Double (2.95), new Double(3.95), "yellow", new Double (2.0)));
 			scale5.put("target_value_3",defineRecodeColorMap(new Double (3.95), new Double(5.0), "green", new Double (3.0)));
 			returnMap.put("scale5", scale5);
 			
 			open10.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(5.45), "red", new Double (1.0)));
 			open10.put("target_value_2",defineRecodeColorMap(new Double (5.45), new Double(6.45), "yellow", new Double (2.0)));
 			open10.put("target_value_3",defineRecodeColorMap(new Double (6.45), new Double(10.0), "green", new Double (3.0)));
 			returnMap.put("open10", open10);
 		}
 		if (customer.equals("departmentC")) {
 			scale5.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(2.95), "red", new Double (1.0)));
 			scale5.put("target_value_2",defineRecodeColorMap(new Double (2.95), new Double(3.95), "yellow", new Double (2.0)));
 			scale5.put("target_value_3",defineRecodeColorMap(new Double (3.95), new Double(5.0), "green", new Double (3.0)));
 			returnMap.put("scale5", scale5);
 			
 			open10.put("target_value_1",defineRecodeColorMap(new Double (1.0), new Double(5.45), "red", new Double (1.0)));
 			open10.put("target_value_2",defineRecodeColorMap(new Double (5.45), new Double(6.45), "yellow", new Double (2.0)));
 			open10.put("target_value_3",defineRecodeColorMap(new Double (6.45), new Double(10.0), "green", new Double (3.0)));
 			returnMap.put("open10", open10);
 		}
 		return returnMap;
 	}
 
 	/**
 	 * @param customer
 	 * @return
 	 * @deprecated
 	 * ranges are from x to y (incl x, excl y)
 	 */
 	public static HashMap<String, Map<String, Double>> getColorRangeMaps(String customer) {
 		HashMap<String, Map<String,Double>> returnMap=new HashMap<String,Map<String,Double>>();
 		
 		//Map<String, Object> mpMaps=new HashMap<String, Object>();
 		String map_id=null;
 		if (customer.equals("departmentA")){
 			map_id="white";
 			Map<String,Double> color_range_white=new HashMap<String,Double>();
 			color_range_white.put("lowRed",new Double(0.0));
 			color_range_white.put("highRed",new Double(0.0));
 			color_range_white.put("lowYellow",new Double(0.0));
 			color_range_white.put("highYellow",new Double(0.0));
 			color_range_white.put("lowGreen",new Double(0.0));
 			color_range_white.put("highGreen", new Double(0.0));
 			returnMap.put(map_id,color_range_white);
 						
 			map_id="mean5";
 			Map<String,Double> color_range_mean5=new HashMap<String,Double>();
 			color_range_mean5.put("lowRed",new Double(1.0));
 			color_range_mean5.put("highRed",new Double(2.95));
 			color_range_mean5.put("lowYellow",new Double(0.0));
 			color_range_mean5.put("highYellow",new Double(0.0));
 			color_range_mean5.put("lowGreen",new Double(3.95));
 			color_range_mean5.put("highGreen", new Double(5.0));
 			returnMap.put(map_id,color_range_mean5);
 			
 			map_id="mean10";
 			Map<String,Double> color_range_mean10=new HashMap<String,Double>();
 			color_range_mean10.put("lowRed",new Double(1.0));
 			color_range_mean10.put("highRed",new Double(5.45));
 			color_range_mean10.put("lowYellow",new Double(5.45));
 			color_range_mean10.put("highYellow",new Double(6.45));
 			color_range_mean10.put("lowGreen",new Double(6.45));
 			color_range_mean10.put("highGreen", new Double(10.0));
 			returnMap.put(map_id,color_range_mean10);
 			
 		}else if (customer.equals("departmentC")) {
 			map_id="white";
 			Map<String,Double> color_range_white=new HashMap<String,Double>();
 			color_range_white.put("lowRed",new Double(0.0));
 			color_range_white.put("highRed",new Double(0.0));
 			color_range_white.put("lowYellow",new Double(0.0));
 			color_range_white.put("highYellow",new Double(0.0));
 			color_range_white.put("lowGreen",new Double(0.0));
 			color_range_white.put("highGreen", new Double(0.0));
 			returnMap.put(map_id,color_range_white);
 			
 			map_id="mean5";
 			Map<String,Double> color_range_mean5=new HashMap<String,Double>();
 			color_range_mean5.put("lowRed",new Double(1.0));
 			color_range_mean5.put("highRed",new Double(2.95));
 			color_range_mean5.put("lowYellow",new Double(2.95));
 			color_range_mean5.put("highYellow",new Double(3.95));
 			color_range_mean5.put("lowGreen",new Double(3.95));
 			color_range_mean5.put("highGreen", new Double(5.0));
 			returnMap.put(map_id,color_range_mean5);
 			
 			map_id="mean10";
 			Map<String,Double> color_range_mean10=new HashMap<String,Double>();
 			color_range_mean10.put("lowRed",new Double(1.0));
 			color_range_mean10.put("highRed",new Double(5.45));
 			color_range_mean10.put("lowYellow",new Double(5.45));
 			color_range_mean10.put("highYellow",new Double(6.45));
 			color_range_mean10.put("lowGreen",new Double(6.45));
 			color_range_mean10.put("highGreen", new Double(10.0));
 			returnMap.put(map_id,color_range_mean10);
 			
 		}else if (customer.equals("departmentB")){
 			map_id="mean5";
 			//rood < 3 (dus exclusief 3.0
 			//geel 3 tot 4 (dus exclusief 4.0)
 			//groen 4 en hoger
 			Map<String,Double> color_range_mean5=new HashMap<String,Double>();
 			color_range_mean5.put("lowRed",new Double(1.0));
 			color_range_mean5.put("highRed",new Double(2.95));
 			color_range_mean5.put("lowYellow",new Double(2.95));
 			color_range_mean5.put("highYellow",new Double(3.95));
 			color_range_mean5.put("lowGreen",new Double(3.95));
 			color_range_mean5.put("highGreen", new Double(5.0));
 			returnMap.put(map_id,color_range_mean5);
 						
 			map_id="mean10";
 			Map<String,Double> color_range_mean10=new HashMap<String,Double>();
 			color_range_mean10.put("lowRed",new Double(1.0));
 			color_range_mean10.put("highRed",new Double(5.45));
 			color_range_mean10.put("lowYellow",new Double(5.45));
 			color_range_mean10.put("highYellow",new Double(6.45));
 			color_range_mean10.put("lowGreen",new Double(6.45));
 			color_range_mean10.put("highGreen", new Double(10.0));
 			returnMap.put(map_id,color_range_mean10);
 			
 		}else {
 			//default
 			map_id="mean5";
 			Map<String,Double> color_range_mean5=new HashMap<String,Double>();
 			color_range_mean5.put("lowRed",new Double(1.0));
 			color_range_mean5.put("highRed",new Double(2.5));
 			color_range_mean5.put("lowYellow",new Double(2.5));
 			color_range_mean5.put("highYellow",new Double(3.5));
 			color_range_mean5.put("lowGreen",new Double(3.5));
 			color_range_mean5.put("highGreen", new Double(5.0));
 			returnMap.put(map_id,color_range_mean5);
 			
 			
 			map_id="mean10";
 			Map<String,Double> color_range_mean10=new HashMap<String,Double>();
 			color_range_mean10.put("lowRed",new Double(1.0));
 			color_range_mean10.put("highRed",new Double(5.45));
 			color_range_mean10.put("lowYellow",new Double(5.45));
 			color_range_mean10.put("highYellow",new Double(6.45));
 			color_range_mean10.put("lowGreen",new Double(6.45));
 			color_range_mean10.put("highGreen", new Double(10.0));
 			returnMap.put(map_id,color_range_mean10);
 			
 		}
 			
 		return returnMap;
 		
 	}
 	
 	
 }
