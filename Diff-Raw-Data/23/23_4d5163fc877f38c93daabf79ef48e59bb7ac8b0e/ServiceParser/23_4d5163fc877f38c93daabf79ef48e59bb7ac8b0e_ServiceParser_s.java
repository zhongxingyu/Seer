 package serviceparser;
 
import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

 import planner.DataType;
 import planner.WebService;
 
 /**
  * Used to parse the services.wsdl
  * @author psyomn
  */
 public class ServiceParser
 {
  
   /**
    * Give a relative file path, to try and parse services
    */
   public static ArrayList<WebService> parse(String iFileName)
   {
 	  ArrayList<WebService> result = new ArrayList<WebService>();
 	  
 	// Initialize available data types
 	DataType a = new DataType("A");
 	DataType b = new DataType("B");
 	DataType c = new DataType("C");
 	DataType d = new DataType("D");
 	DataType e = new DataType("E");
 	DataType f = new DataType("F");
 	DataType g = new DataType("G");
 	DataType h = new DataType("H");
 	DataType j = new DataType("J");
 	DataType k = new DataType("K");
 	
 	// Initialize web services
 	WebService w1 = new WebService("W1");
 	WebService w2 = new WebService("W2");
 	WebService w3 = new WebService("W3");
 	WebService w4 = new WebService("W4");
 	WebService w5 = new WebService("W5");
 	WebService w6 = new WebService("W6");
 	WebService w7 = new WebService("W7");
 	WebService w8 = new WebService("W8");
 	
 	result.add(w1);
 	result.add(w2);
 	result.add(w3);
 	result.add(w4);
 	result.add(w5);
 	result.add(w6);
 	result.add(w7);
 	result.add(w8);
 	
 	// Configure input and output data types of web services
 	w1.getPreConditions().add(a);
 	w1.getPreConditions().add(b);
 	w1.getPreConditions().add(c);
 	w1.getPostConditions().add(j);
 	
 	w2.getPreConditions().add(b);
 	w2.getPreConditions().add(c);
 	w2.getPostConditions().add(e);
 	w2.getPostConditions().add(f);
 	
 	w3.getPreConditions().add(c);
 	w3.getPreConditions().add(e);
 	w3.getPostConditions().add(h);
 	
 	w4.getPreConditions().add(c);
 	w4.getPreConditions().add(f);
 	w4.getPostConditions().add(g);
 	
 	w5.getPreConditions().add(k);
 	w5.getPostConditions().add(h);
 	
 	w6.getPreConditions().add(j);
 	w6.getPostConditions().add(c);
 
 	w7.getPreConditions().add(h);
 	w7.getPostConditions().add(d);
 	
 	w8.getPreConditions().add(g);
 	w8.getPostConditions().add(h);
 	
     return result;
   }
 }
