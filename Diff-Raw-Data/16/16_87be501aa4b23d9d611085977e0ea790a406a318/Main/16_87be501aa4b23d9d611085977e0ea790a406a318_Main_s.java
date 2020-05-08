 /**
  * Main
  *
  * @author Julien SOSIN
  *
  */
 package com.domaintoolsapi;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.codehaus.jackson.JsonNode;
 
 import com.domaintoolsapi.exceptions.DomainToolsException;
 
 
 /**
  * Sample class to use DomainAPI java library.
  * 
  * @author Julien SOSIN
  */
 public class Main {
 	
 	public static void main(String[] args) {
 		//args[0] your username, args[1] : your api_key
 		DomainTools domainTools = new DomainTools(args[0], args[1]);
 		
 		JsonNode jsonNode;
 		try {
 			//A Whois in XML on domaintools.com
 			DTRequest dtRequest = domainTools.use("whois");
 			dtRequest.on("domaintools.com").toXML();
 			String xml_response = dtRequest.getXML();
 			System.out.println(xml_response);
 			
 			//A reverse ip on nameintel.com
 			dtRequest.clear();
 			dtRequest.use("reverse-ip");
 			dtRequest.on("nameintel.com").where("limit=2").toJSON();
 			jsonNode = dtRequest.getObject();
 			Iterator<JsonNode> it = jsonNode.get("response").get("ip_addresses").get("domain_names").getElements();
 			System.out.println("Domain names :");
 			while(it.hasNext()){
 				System.out.println(it.next());
 			}

 		} catch (DomainToolsException e) {
 			e.printStackTrace();
 		}

 	}
 }
