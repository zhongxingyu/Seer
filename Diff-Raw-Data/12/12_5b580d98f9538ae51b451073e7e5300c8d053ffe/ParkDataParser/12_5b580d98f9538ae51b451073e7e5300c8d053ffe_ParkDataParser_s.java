 package com.cs310.ubc.meetupscheduler.server;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.cs310.ubc.meetupscheduler.server.Advisory.AdvisoryField;
 import com.cs310.ubc.meetupscheduler.server.Facility.FacilityField;
 import com.cs310.ubc.meetupscheduler.server.Park.ParkField;
 import com.cs310.ubc.meetupscheduler.server.Washroom.WashroomField;
 
 import javax.jdo.JDOHelper;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 public class ParkDataParser {
 	
 	private Document doc;
 	
 	public void parseXML(InputStream xmlFile) {
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			doc = builder.parse(xmlFile);
 			
 			if (doc == null) {
 				//TODO: CAROLINE error handling
 				return;
 			}
 			NodeList elementList = doc.getElementsByTagName("*");
 			if (elementList == null) {
 				//TODO: CAROLINE error handling
 				return;
 			}
 			
 			boolean areNoParks = true;
 			for (int i = 0; i < elementList.getLength(); i++) {
 				Element element = (Element) elementList.item(i);
 				if (element.getNodeName().equals("Park")) {
 					createPark(element);
 					areNoParks = false;
 				}
 			}
 			if (areNoParks) {
 				//TODO: CAROLINE error handling
 			}
 
 	} catch (Exception e) {
 		//TODO: CAROLINE error handling
 		System.err.println(e);	
 	}
 	}
 	
 	//Creates a Park Object. Only Park ID is guaranteed to exist.
 	private void createPark(Element element) {
 		Map<String, String> parkDataMap = new HashMap<String, String>();
 		
 		String ID  = element.getAttribute("ID");
 		if (ID == null || ID.isEmpty()) {
 			//TODO: CAROLINE error handling
 			return;
 		}
 		parkDataMap.put(ParkField.ID.toString(), ID);
 		
 		NodeList parkElements = element.getElementsByTagName("*");
 		
 		if (parkElements != null ) {
 			for (int i = 0; i < parkElements.getLength(); i++) {
 				Element parkElement = (Element) parkElements.item(i);
 				String elementName = parkElement.getNodeName();
 				
 				if (elementName == null) {
 					//TODO: Caroline error handling
 				}
 				else if (elementName.equals("Name")) {
 					parkDataMap.put(ParkField.NAME.toString(), parkElement.getTextContent());
 				}
 				/** Do nothing?
 				else if (elementName.equals("Official")) {
 					parkDataMap.put("Official", new Integer(parkElement.getTextContent()));
 				}**/
 				else if (elementName.equals("StreetNumber")) {
 					parkDataMap.put(ParkField.STRTNUM.toString(), parkElement.getTextContent());
 				}
 				else if (elementName.equals("StreetName")) {
					parkDataMap.put(ParkField.STRTNUM.toString(), parkElement.getTextContent());
 				}
 				else if (elementName.equals("EWStreet")) {
 					parkDataMap.put(ParkField.EWST.toString(), parkElement.getTextContent());
 				}
 				else if (elementName.equals("NSStreet")) {
 					parkDataMap.put(ParkField.NSST.toString(), parkElement.getTextContent());
 				}
 				//LatLng object from the maps API can be created directly from this string
 				else if (elementName.equals("GoogleMapDest")) {
 					parkDataMap.put(ParkField.MAPSTR.toString(), parkElement.getTextContent());
 				}
 				else if (elementName.equals("Hectare")) {
 					parkDataMap.put(ParkField.HECT.toString(), parkElement.getTextContent());
 				}
 				else if (elementName.equals("NeighbourhoodName")) {
 					parkDataMap.put(ParkField.NNAME.toString(), parkElement.getTextContent());
 				}
 				else if (elementName.equals("NeighbourhoodURL")) {
 					parkDataMap.put(ParkField.NURL.toString(), parkElement.getTextContent());
 				}
 				else if (elementName.equals("Advisories")) {
 					parkDataMap.put(ParkField.HASADVS.toString(), "Y");
 				}
 				else if (elementName.equals("Advisory")) {
 					createAdvisory(parkElement, ID);
 				}
 				else if (elementName.equals("Facilities")) {
 					parkDataMap.put(ParkField.HASFAC.toString(), "Y");
 				}
 				else if (elementName.equals("Facility")) {
 					createFacility(parkElement, ID);
 				}
				else if (elementName.equals("Washroom")) {
 					createWashroom(parkElement, ID);
 				}
 			}
 			PersistenceManager pm = getPersistenceManager();
 			try {
 				pm.makePersistent(new Park(parkDataMap));
 				System.out.println("new park created ID:" + ID.toString());
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchFieldException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				pm.close();
 			}
 			
 		}		
 	}
 
 	private void createWashroom(Element washroomElement, String pID) {
 		Map<String, String> washroomDataMap = new HashMap<String, String>();
		washroomDataMap.put(AdvisoryField.PID.toString(), pID);
 		
 		NodeList children = washroomElement.getElementsByTagName("*");
 		if (children != null) {
 			for (int i = 0; i < children.getLength(); i++) {
 				Element wshElement = (Element) children.item(i);
 				String elementName = wshElement.getNodeName();
 				
 				if (elementName == null) {
 					//TODO: Caroline error handling
 				}
 				else if (elementName.equals("Location")) {
 					//TODO need to make this a date?
 					washroomDataMap.put(WashroomField.LOC.toString(), wshElement.getTextContent());
 				}
 				else if (elementName.equals("Notes")) {
 					washroomDataMap.put(WashroomField.NOTES.toString(), wshElement.getTextContent());
 				}
 				else if (elementName.equals("SummerHours")) {
 					washroomDataMap.put(WashroomField.SUMHR.toString(), wshElement.getTextContent());
 				}
 				else if (elementName.equals("WinterHours")) {
 					washroomDataMap.put(WashroomField.WINHR.toString(), wshElement.getTextContent());
 				}
 			}
 			PersistenceManager pm = getPersistenceManager();
 			try {
 				pm.makePersistent(new Washroom(washroomDataMap));
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchFieldException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				pm.close();
 			}
 			System.out.println("new washroom created PID:" + pID.toString());
 			
 		}
 		
 	}
 
 	private void createAdvisory(Element advisoryElement, String iD) {
 		Map<String, String> advisoryDataMap = new HashMap<String, String>();
 		advisoryDataMap.put(AdvisoryField.PID.toString(), iD);
 		
 		NodeList children = advisoryElement.getElementsByTagName("*");
 		if (children != null) {
 			for (int i = 0; i < children.getLength(); i++) {
 				Element advElement = (Element) children.item(i);
 				String elementName = advElement.getNodeName();
 				
 				if (elementName == null) {
 					//TODO: Caroline error handling
 				}
 				else if (elementName.equals("DateLast")) {
 					//TODO need to make this a date?
 					advisoryDataMap.put(AdvisoryField.DATE.toString(), advElement.getTextContent());
 				}
 				else if (elementName.equals("AdvisoryText")) {
 					advisoryDataMap.put(AdvisoryField.TEXT.toString(), advElement.getTextContent());
 				}
 				else if (elementName.equals("URL")) {
 					advisoryDataMap.put(AdvisoryField.URL.toString(), advElement.getTextContent());
 				}
 			}
 			PersistenceManager pm = getPersistenceManager();
 			try {
 				pm.makePersistent(new Advisory(advisoryDataMap));
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchFieldException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				pm.close();
 			}
 			System.out.println("new advisory created PID:" + iD.toString());
 			
 		}
 		
 	}
 
 	private void createFacility(Element facilityElement, String iD) {
 		Map<String, String> facilityDataMap = new HashMap<String, String>();
 		facilityDataMap.put(FacilityField.PID.toString(), iD);
 		String specialFeatures = "";
 		
 		NodeList children = facilityElement.getElementsByTagName("*");
 		if (children != null) {
 			for (int i = 0; i < children.getLength(); i++) {
 				Element facElement = (Element) children.item(i);
 				String elementName = facElement.getNodeName();
 				
 				if (elementName == null) {
 					//TODO: Caroline error handling
 				}
 				else if (elementName.equals("FacilityCount")) {
 					facilityDataMap.put(FacilityField.COUNT.toString(), facElement.getTextContent());
 				}
 				else if (elementName.equals("FacilityType")) {
 					facilityDataMap.put(FacilityField.TYPE.toString(), facElement.getTextContent());
 				}
 				else if (elementName.equals("FacilityURL")) {
 					facilityDataMap.put(FacilityField.URL.toString(), facElement.getTextContent());
 				}
 				else if (elementName.equals("SpecialFeature")) {
 					if (specialFeatures.isEmpty())
 						specialFeatures = facElement.getTextContent();
 					else
 						specialFeatures += (", " + facElement.getTextContent());
 				}
 			}
 			if (!specialFeatures.isEmpty()) {
 				facilityDataMap.put(FacilityField.SPECIALFEAT.toString(), specialFeatures);
 			}
 			PersistenceManager pm = getPersistenceManager();
 			try {
 				pm.makePersistent(new Facility(facilityDataMap));
 			} catch (SecurityException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoSuchFieldException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				pm.close();
 			}
 			System.out.println("new facility created PID:" + iD.toString());
 			
 		}
 		
 	}
 
 	private PersistenceManager getPersistenceManager() {
 		return PersistenceManagerSingleton.getInstance();
 	}
 }
