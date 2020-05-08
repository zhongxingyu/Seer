 /**
  * Main
  *
  * @author Julien SOSIN
  *
  */
 package com.domaintoolsapi;
 
 import java.util.HashMap;
 
import org.codehaus.jackson.JsonNode;

 
 /**
  * Sample class to use DomainAPI java library.
  * 
  * @author Julien SOSIN
  */
 public class Main {
 	
 	public static void main(String[] args) {
 		DomainTools domainTools = new DomainTools(args[0], args[1]);
 		try {
			// Example : we request an xml response when using the whois service on domaintools with signed method
			String s = domainTools.use("whois").on("domaintools.com").signed(true).toXML();
 			System.out.println(s);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 }
