 /**
  * Copyright 2009-2011 The Australian National University
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package au.edu.anu.portal.portlets.basiclti.support;
 
import java.net.ConnectException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 import javax.xml.rpc.ParameterMode;
 import javax.xml.rpc.ServiceException;
 import javax.xml.soap.SOAPException;
 
 import org.apache.axis.client.Call;
 import org.apache.axis.client.Service;
 import org.apache.axis.encoding.XMLType;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * A set of services for making Web Service calls
  *
  * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
  *
  */
 public class WebServiceSupport {
 
 	private static final Log log = LogFactory.getLog(WebServiceSupport.class.getName());
 
 	/**
 	 * Make a web service call to the given endpoint, calling the method and using the params supplied
 	 * @param endpoint	wsdl url
 	 * @param method	method to call
 	 * @param params	LinkedHashMap of params:
 	 *  1. Must be in order required to be sent
 	 *  2. Must be keyed on the parameter name to be sent, must match the webservice param exactly or it will fail
 	 *  3. Should contained a single Map of items, containing 'value' and 'type' keys
 	 *  4. The type attribute will be converted and supported values are string or boolean, case insensitive
 	 * 
 	 * @return the response, or null if any exception is thrown.
 	 */
 	public static String call(String endpoint, String method, Map<String,Map<String,String>> params) {
 		
 		Service service = new Service();
 		
 		try {
 			Call nc = (Call) service.createCall();
 			
 			nc.setTargetEndpointAddress(endpoint);
 			
 			nc.removeAllParameters();
 			nc.setOperationName(method);
 			
 			List<Object> values = new ArrayList<Object>();
 			
 			for (Map.Entry<String,Map<String,String>> entry : params.entrySet()) {
 
 				//add value
 				values.add(entry.getValue().get("value"));
 								
 				//setup the type
 				QName qname = null;
 				try {
 					qname = getNameForType(entry.getValue().get("type"));
 				} catch (SOAPException e) {
 					e.printStackTrace();
 					return null;
 				}
 
 				//add the parameter
 				nc.addParameter(entry.getKey(), qname, ParameterMode.IN);
 				
 			}
 			
 			nc.setReturnType(XMLType.XSD_STRING);
 		
 			return (String) nc.invoke(values.toArray());
 
 		}
 		catch (RemoteException e) {
 			//e.printStackTrace();
 			log.error("A connection error occurred: " + e.getClass() + ": " + e.getMessage());
 		}
 		catch (ServiceException e) {
 			//e.printStackTrace();
 			log.error("A connection error occurred: " + e.getClass() + ": " + e.getMessage());
 		}
 
 		return null;
 	}
 	
 	/**
 	 * Convert the string type to a qname
 	 * @param type
 	 * @return
 	 */
 	private static QName getNameForType(String type) throws SOAPException{
 		
 
 		if(StringUtils.equalsIgnoreCase(type, "string")) {
 			return XMLType.XSD_STRING;
 		} else if (StringUtils.equalsIgnoreCase(type, "boolean")) {
 			return XMLType.XSD_BOOLEAN;
 		} else {
 			throw new SOAPException("Invalid value for type attribute, must be one of 'string' or 'boolean'.");
 		}
 		
 	}
 }
