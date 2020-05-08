 /**
  * Main
  *
  * @author Julien SOSIN
  *
  */
 package com.domaintoolsapi;
 
 import java.util.HashMap;
 
 
 /**
  * Sample class to use DomainAPI java library.
  * 
  * @author Julien SOSIN
  */
 public class Main {
 	
 	public static void main(String[] args) {
 		DomainTools domainTools = new DomainTools(args[0], args[1]);
 		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("query", "domain%20tools");
			// Example : we request an xml response when using the domain-search service on domaintools with signed method
			String s = domainTools.use("domain-search").on("domaintools.com").where(params).toXML();
 			System.out.println(s);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 }
