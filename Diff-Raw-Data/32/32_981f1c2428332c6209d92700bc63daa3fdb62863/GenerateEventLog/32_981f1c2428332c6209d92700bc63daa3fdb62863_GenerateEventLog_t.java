 package org.mugsandcoffee;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Element;
 
 /**
  * Reads xml file elements and loads into database's table
  * @author Mugs and Coffee
  * @coder Kenneth "digiArtist_ph" P. Vallejos
  * @since Sunday, May 19, 2013
  * @version 1.0
  *
  */
 public final class GenerateEventLog {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		NodeList eventNodes;
 		
 		// instantiates some classes
 		ParseXML pxml = new ParseXML();
 		StreamReader strmdEvents = new StreamReader("run0.0.events.xml");
 		
 		// connects to selected database
 		pxml.connectToDB();
 		
 		// read text file here
 		eventNodes = strmdEvents.getAllObjects();
 		
		// clears the eventlog table
		pxml.clearEventLog();
		
 		// loops each line 
 		for(int i = 0; i < eventNodes.getLength(); i++) {
 			Node nNode = eventNodes.item(i);
 			
 			if (nNode.getNodeType() == Node.ELEMENT_NODE) {		 
 				Element eElement = (Element)nNode;	
 				float l_time = (float) ((eElement.getAttribute("time") != "") ? Float.parseFloat(eElement.getAttribute("time")) : 0.0);
 				String l_type =  ((eElement.getAttribute("type") != "") ? eElement.getAttribute("type"): "");
 				int l_person = (int)((eElement.getAttribute("person") !="") ? Integer.parseInt(eElement.getAttribute("person")) : 0);
 				int l_vehicle = (int)((eElement.getAttribute("vehicle") !="") ? Integer.parseInt(eElement.getAttribute("vehicle")): 0);
 				int l_facility = (int)((eElement.getAttribute("facility") != "") ? Integer.parseInt(eElement.getAttribute("facility")) : 0);
 				int l_link = (int)((eElement.getAttribute("link") !="") ? Integer.parseInt(eElement.getAttribute("link")) : 0);
 				String l_acttype = ((eElement.getAttribute("actType") != "") ? eElement.getAttribute("actType") : "");
 				String l_legmode = ((eElement.getAttribute("legMode") != "") ? eElement.getAttribute("legMode"): "");
 				
 				pxml.insertEvent(l_time, l_type, l_person, l_vehicle, l_facility, l_link, l_acttype, l_legmode);
 			}
 		}
 		
		pxml.closeConnection();
 		System.out.print("Succeded");
 
 	}
 
 }
