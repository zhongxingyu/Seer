 /*
 
   (c) Copyright 2011 Telefonica, I+D. Printed in Spain (Europe). All Righ
   Reserved.
 
   The copyright to the software program(s) is property of Telefonica I+D.
   The program(s) may be used and or copied only with the express written
   consent of Telefonica I+D or in accordance with the terms and conditions
   stipulated in the agreement/contract under which the program(s) have
   been supplied.
 
   */
 package com.telefonica.claudia.smi;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public abstract class TCloudConstants {
 	
	
	
	public static final String  TAG_NETWORK_MAC_ENABLED = "MAC_ENABLE";
	

	
	public static final String  TAG_NETWORK_IPLEASES="LEASE";
	
	public static final String  TAG_NETWORK_IP="IP";
	
	
	public static final String  TAG_NETWORK_MAC="MAC";

	
 	public static final String TAG_NETWORK_ROOT = "Network";
 	public static final String ATTR_NETWORK_NAME = "name";
 	
 	
 	public static final String TAG_NETWORK_NETMASK = "Netmask";
 	public static final String TAG_NETWORK_BASE_ADDRESS = "BaseAddress";
 	
 	
 	public static final String TAG_INSTANTIATE_OVF = "InstantiateOvfParams";
 	public static final String ATTR_INSTANTIATE_OVF_NAME = "name";
 	
 	
 	public static final String TAG_INSTANTIATION_PARAMS = "InstantiationParams";
 	public static final String ATTR_INSTANTIATION_PARAMS_NAME = "name";
 	
 	
 	public static final String TAG_NETWORK_CONFIG = "NetworkConfig";
 	public static final String TAG_NETWORK_CONFIG_SECTION = "NetworkConfigSection";
 	public static final String TAG_NETWORK_ASSOCIATION = "NetworkAssociation";
 	
 	
 	public static final String TAG_ASPECTS_SECTION = "AspectsSection";
 	public static final String TAG_ASPECT = "Aspect";
 	public static final String TAG_ASPECT_PROPERTY = "Property";
 	public static final String TAG_ASPECT_KEY = "Key";
 	public static final String TAG_ASPECT_VALUE = "Value";
 	public static final String ATTR_ASPECT_VSYSTEM="vsystem";
 	
 	public static final String ATTR_NETWORK_ASSOCIATION_HREF= "href";
 	
 	public static final String TAG_TASKS="Tasks";
 	public static final String TAG_TASK="Task";
 	
 	public static final String TAG_ENVELOPE = "Envelope";
 		
 	public static final String NAMESPACE_TCLOUD="http://schemas.tcloud.telefonica.com/tcloud/0.1";
 	public static final String NAMESPACE_OVF="http://schemas.dmtf.org/ovf/envelope/1";
 	public static final String NAMESPACE_RASD="http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_ResourceAllocationSettingData";
 	public static final String NAMESPACE_IEP="http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_IPProtocolEndpoint";
 	public static final String NAMESPACE_VSSD="http://schemas.dmtf.org/wbem/wscim/1/cim-schema/2/CIM_VirtualSystemSettingData";
 	
 	public static final String DEFAULT_STORAGE_UNIT="bytes * 2 ^ 20";
 	
 	public static enum StateType {initial, deploying, active, running, poweredOff, suspended, unactive, error, unknown};
 	
 	public static enum ErrorType {UNKNOWN_ELEMENTS,ELEMENT_NOT_FOUND};
 	
 	public static String[] aggregatedMeasureDescriptors = new String[]{"cpuUsage", "diskUsage", "memoryUsage"};
 	
 	public static Document createErrorMessage(String uri, String elementType, ErrorType errorType) {
         try {
 			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 	        DocumentBuilder docBuilder;
 
 			docBuilder = dbfac.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			
             Element r = doc.createElement("ErrorSet");
             doc.appendChild(r);
             
             if (errorType==ErrorType.UNKNOWN_ELEMENTS) {
                 Element unknown= doc.createElement("UnknownElements");
                 r.appendChild(unknown);
                 
                 Element element= doc.createElement("element");
                 unknown.appendChild(element);
                 
                 element.setAttribute("type", "vapp");
                 element.setAttribute("ref", uri);
             }
             
             return doc;
         } catch (IllegalArgumentException iae) {
         	return null;
         } catch (ParserConfigurationException pe) {
         	return null;
 		}
 	}
 }
