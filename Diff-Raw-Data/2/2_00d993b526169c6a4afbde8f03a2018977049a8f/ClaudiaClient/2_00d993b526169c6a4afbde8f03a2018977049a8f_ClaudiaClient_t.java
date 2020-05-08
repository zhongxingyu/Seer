 /*
 * Claudia Project
 * http://claudia.morfeo-project.org
 *
 * (C) Copyright 2010 Telefonica Investigacion y Desarrollo
 * S.A.Unipersonal (Telefonica I+D)
 *
 * See CREDITS file for info about members and contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Affero GNU General Public License (AGPL) as 
 * published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the Affero GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * If you want to use this software an plan to distribute a
 * proprietary application in any way, and you are not licensing and
 * distributing your source code under AGPL, you probably need to
 * purchase a commercial license of the product. Please contact
 * claudia-support@lists.morfeo-project.org for more information.
 */
 
 package com.telefonica.claudia.slm.test;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.rmi.NotBoundException;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.restlet.Client;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Protocol;
 import org.restlet.data.Reference;
 import org.restlet.data.Response;
 import org.restlet.resource.DomRepresentation;
 import org.restlet.resource.Representation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 
 public class ClaudiaClient {
 
     public static final String ROOT_MONITORING_TAG_NAME = "MonitoringInformation";
     public static final String EVENT_TYPE_TAG_NAME = "EventType";
     public static final String T_0_TAG_NAME = "EpochTimestamp";
     public static final String T_DELTA_TAG_NAME = "TimeDelta";
     public static final String FQN_TAG_NAME = "FQN";
     public static final String VALUE_TAG_NAME = "Value";
     
     public static final String RESERVOIR_PATH="/api/org/es_tid/vdc";
 	
 	private final static String PATH_TO_PROPERTIES_FILE="conf/claudiaClient.properties";
 	private final static String PATH_TO_ACD_TEST="conf/";
 
 	private final static String CUSTOMER_NAME="customer.name";
 	private static final String HOST = "smi.host";
 	private static final String KEY_PORT = "smi.port";
 	private static final String KEY_INSTANTIATE = "smi.instantiationURI";	
 	private static final String SERVER_TIMEOUT="connection.timeout";
 	private static final String REST_PATH = "rest.path";
 	private static final String REST_SERVER_PORT = "rest.port";
 	private static final String REST_HOST = "rest.host";
 	
 	private static final String ACD_HOST = "acd.host";
 	private static final String ACD_PORT = "acd.port";
 	
 	private static final String SECURITY_FILE= "security.path";
 
 	private final static int INITIALIZATION_ERROR= 1;
 	private final static int CONNECTION_ERROR=2;
 	private final static int INTERNAL_ERROR=3;
 	
 	private static final String PROMPT = "\n Claudia > ";
 	private static final String SEPARATOR = "---------------------------------------------------------------------------------------------";
 	private static final String WRONG_PARAMETER_FORMAT = "Wrong parameter: ";
 
 	public static Client client;
 	private static HttpClient httpClient = new HttpClient();
 	
 	private static int serverTimeout;
 	private static String restPath;
 	private static String restServerPort;
 	private static String restServerHost;
 
 	private static String acdHost;
 	private static int acdPort;
 	
 	private static String securityCookieFile;
 	private static HashMap<String, String> cookies = new HashMap<String, String>();
 	
 	static Properties prop;
 	
 	private static List<String> history = new ArrayList<String>();
 	
 	private static String serviceName=null;
 	private static String ovfUrl=null;
 	
 	private static Reference customerURI= null; 
 
 	private static Reference itemUri= null;
 	private static String customerName =null;
 	private static boolean scriptmode = false;
 	
 	public static void main(String[] args){
 
 		// Load the properties
 		prop = new Properties();
 		
    	 	try {
 			prop.load(new FileInputStream(PATH_TO_PROPERTIES_FILE));			
 			serverTimeout = Integer.parseInt(prop.getProperty(SERVER_TIMEOUT));
 			
 		} catch (FileNotFoundException e) {
 			System.out.println("Properties file not found. Expected path: " + PATH_TO_PROPERTIES_FILE);
 			System.exit(INITIALIZATION_ERROR);
 		} catch (IOException e) {
 			System.out.println("Error reading properties file. Expected path: " + PATH_TO_PROPERTIES_FILE);
 			System.exit(INITIALIZATION_ERROR);
 		} catch (NumberFormatException nfe) {
 			serverTimeout = 30;
 		}
 		
 		restPath = prop.getProperty(REST_PATH);
 		restServerPort = prop.getProperty(REST_SERVER_PORT);
 		restServerHost = prop.getProperty(REST_HOST);	
 		securityCookieFile = prop.getProperty(SECURITY_FILE);
 		acdHost = prop.getProperty(ACD_HOST);
 		try {
 		acdPort = Integer.parseInt(prop.getProperty(ACD_PORT));
 		} catch(NumberFormatException nfe) {
 			System.out.println("Format error reading config file: acd.port should be an integer");
 			return;
 		}
 		
 		client = new Client(Protocol.HTTP);
 
 		customerName = prop.getProperty(CUSTOMER_NAME);		
 
 		// If there are any parameters, the client will be executed in script mode.
 		if (args.length >0) {
 			scriptmode = true;
 			
 			//System.out.println("Running in script mode");
 			
 			String scriptCommand = "";
 			for (String com: args) {
 				scriptCommand += com;
 			}
 			
 			runCommand(scriptCommand);
 			System.exit(0);
 		}
 		
 		// Wait for the service application to be deployed. The aplication may take 
 		// between five and ten minutes to be deployed, so wait in the standard input.
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 	    String s;
 	    
 	    System.out.println("\n\nType \"help\" to see the list of commands\n\n");
 
 	    
 	    System.out.print(PROMPT);
 	    try {
 			while ((s = in.readLine()) != null) {
 				
 				s = s.trim().replace("\\s+", " ");
 				
 				if (s.trim().length()>0 && s.trim().charAt(0) != '!')
 					history.add(s);
 				
 				if (s.trim().length()>1 && s.charAt(0)=='!') {
 					
 					try {
 						int index = Integer.parseInt(s.trim().substring(1));
 						s = history.get(index);
 						
 					} catch (NumberFormatException nfe) {
 						System.out.println("Wrong number in command repetition");
 					}
 				}
 				
 				runCommand(s);
 				System.out.print(PROMPT);
 			}
 			
 		} catch (IOException e) {
 			System.out.println("I/O Exception waiting for input: " + e.getMessage());
 		}
 	}
 	 
 	private static void runCommand(String s) {
 		
 		if (s.equals("quit")) {
 			System.out.println("Exiting...");
 			System.exit(0);
 		} else if (s.matches("shutdown")) {
 			
 		
 		} else if (s.matches("history")) {
 			
 	
 			System.out.println("\n\nHistory\n--------------------------");
 			
 			for (int i=0; i < history.size(); i++) {
 				System.out.println("\t" + i + " - " + history.get(i));
 			}
 			
 
 			System.out.println("\n\n");
 			
 		} else if (s.matches("createCustomer\\(.*\\)")) {
 			
 			String[] params = getParams(s);
 			
 			if (params.length==1) {
 				customerName = params[0].trim();
 			} else {
 				System.out.println(WRONG_PARAMETER_FORMAT + "UserName string expected");
 				return;
 			}
 			
 			customerURI = createCustomerCommand(customerName); 
 			
 			try {Thread.sleep(serverTimeout);} catch (InterruptedException ie) {}
 			
 		} else if (s.matches("deploy\\(.*\\)")) {
 			
 			
 			
 			String[] params = getParams(s);
 			
 			if (params.length == 3) {
 				ovfUrl= params[2].trim();
 			} else {
 				System.out.println("Wrong parameter number. Use 'help' to display usage.");
 				return;
 			}
 			
 			if (!params[0].equals("")) {
 				customerName= params[0].trim();
 			} else {
 				System.out.println("Wrong customer name.");
 				return;
 			}
 			
 			if (!params[1].equals("")) {
 				serviceName= params[1].trim();
 			} else {
 				System.out.println("Wrong service name.");
 				return;
 			}
 			String respo = null;			
 			try {
 				 respo = createServiceCommand(customerName, serviceName, ovfUrl);
 			} catch(UnknownHostException uhe) {
 				System.out.println("Could not connect. Unknown host: " + prop.getProperty(HOST)+prop.getProperty(KEY_PORT));
 			} catch (IOException e) {
 				System.out.println("Unknown error: " + e.getMessage());
 			}
 			
 			if (respo==null)
 				System.out.println("\n There has been an error deploying the service. Check that the deployment parameters are ok and all the needed software is up.");
 			else
 			/*	if (!scriptmode)
 				    System.out.println("\n Service URI Task: " + respo);
 				else
 					System.out.println(respo);*/
 			 if (!scriptmode)
                  System.out.println("\n Service URI Task: " + respo);
              else
                     System.out.println(respo);
                      
 			 
 		} else if (s.matches("info\\(.*\\)")) {
 
 			String[] params = getParams(s);
 			
 			if (params.length < 1 || params[0].trim().equals("")) {
 				System.out.println("Wrong parameters. Use 'help' to display usage.");
 				return;
 			}
 			
 			if (params.length == 1) {
 				System.out.println(infoCustomerCommand(getCustomerURI(params[0])));
 			} else if (params.length == 2 && !params[1].trim().equals("")) {
 				System.out.println(infoServiceCommand(getServiceURI(params[0].trim(), params[1].trim())));
 			}
 			
 		} else if (s.matches("deleteCustomer\\(.*\\)")) {
 
 			String[] params = getParams(s);
 			
 			if (params.length != 1) {
 				
 				System.out.println("Wrong parameter number. Use 'help' to display usage.");
 				return;
 			}
 			
 			if (!params[0].equals("")) {
 				customerName= params[0].trim();
 			} else {
 				System.out.println("Wrong customer name.");
 				return;
 			}
 			
 			deleteCustomerCommand(getCustomerURI(customerName));
 			
 		} else if (s.matches("undeploy\\(.*\\)")) {
 			
 			String[] params = getParams(s);
 			
 			if (params.length != 2) {
 				System.out.println("Wrong parameter number. Use 'help' to display usage.");
 				return;
 			}
 			
 			if (!params[0].equals("")) {
 				customerName= params[0].trim();
 			} else {
 				System.out.println("Wrong customer name.");
 				return;
 			}
 			
 			if (!params[1].equals("")) {
 				serviceName= params[1].trim();
 			} else {
 				System.out.println("Wrong service name.");
 				return;
 			}
 			
 			deleteServiceCommand(getServiceURI(customerName, serviceName));
 			
 		} else if (s.matches("addCookie\\(.*\\)")) {
 			
 			String[] params = getParams(s);
 			String cookieName;
 			String cookieValue;
 			
 			if (params.length==2) {
 				cookieName = params[0].trim();
 				cookieValue = params[1].trim();
 			} else {
 				System.out.println(WRONG_PARAMETER_FORMAT + "Pair (name, value) expected");
 				return;
 			}
 			
 			addCookies(cookieName, cookieValue);
 			
 		} else if (s.matches("removeCookie\\(.*\\)")) {
 			
 			String[] params = getParams(s);
 			String cookieName;
 			
 			if (params.length==1) {
 				cookieName = params[0].trim();
 			} else {
 				System.out.println(WRONG_PARAMETER_FORMAT + "Pair (name, value) expected");
 				return;
 			}
 			
 			removeCookie(cookieName);
 			
 		} else if (s.matches("listCookies\\(.*\\)")) {
 			
 			printCookies();
 			
 		} else if (s.matches("writeCookies\\(.*\\)")) {
 			
 			writeCookies();
 			
 		} else if (s.matches("removeCookies\\(.*\\)")) {
 			
 			removeCookies();
 			
 		} else if (s.matches("(\\d+:)?(\\d+:)?(\\d+:)?event\\((.*,)+.*\\)")) {
 		
 			int loops =1;
 			int delay =0;
 			int error =0;
 
 			if (s.indexOf(":") > 0) {
 				try {
 					String[] parameters = s.substring(0, s.lastIndexOf(':')).split(":");
 					
 					if (parameters.length> 0) {
 						loops = Integer.parseInt(parameters[0]);
 					}
 					
 					if (parameters.length> 1) {
 						delay = Integer.parseInt(parameters[1]);
 					}
 					
 					if (parameters.length> 2) {
 						error = Integer.parseInt(parameters[2]);
 					}							
 					
 				} catch (NumberFormatException nfe) {
 					System.out.println("Parse error in the event parameters");
 				}
 			}
 			
 			String[] params = getParams(s);
 			
 			if (params.length != 3) {
 				System.out.println("Usage:\n\tevent(event_type, fqn, value)\n\n");
 				return;
 			}
 			
 			double value;
 			try {
 				value = Double.parseDouble(params[2].trim());
 			} catch (NumberFormatException nfe) {
 				System.out.println("Error parsing the event value. Should be a double, got: " + params[2]);
 				return;
 			}
 			
 			for (int i=0; i < loops; i++) {
 				
 				// Calculate error
 				double dValue= ((Math.random()-0.5)*(error*value))/50.0;
 				
 				if (params[0].equals("hw")) {
 					sendRESTMessage("VEEHW", new Date().getTime(), 4, params[1].trim(), value + dValue);
 				} else if (params[0].equals("agent")) {
 					sendRESTMessage("AGENT", new Date().getTime(), 4, params[1].trim(), value + dValue);
 				}
 				
 				System.out.println("\tSending "+ ((params[0].equals("hw"))?"infrastructure":"kpi") +" measure to [" + params[1].trim() + "]: " + (value + dValue));
 				
 				try { Thread.sleep(delay*1000); } catch (InterruptedException e) {}
 			}
 		} else if (s.equals("help")) {
 			System.out.println("\n Command list:\n");
 			System.out.println("\tcreateCustomer(customerName)\t\t\t- Create a new customer and load it as the actual customer.");
 			System.out.println("\tdeleteCustomer(customerFQN)\t\t\t- Delete the actual customer.");
 			System.out.println("\tdeploy(customerName, serviceName, ovfUrl)\t- Deploy a new service for the given customer.");
 			System.out.println("\tundeploy(customerName, serviceName)\t\t- Delete the actual service.");
 			System.out.println("\tinfo(customer [, service])\t\t\t- Retrieves info about the given customer or about one of its services if given.");
 			System.out.println("\n\n\taddCookie(name, value)\t\t\t\t- Add the given cookie to the cookie list.");
 			System.out.println("\tremoveCookie(name)\t\t\t\t- Remove the given cookie from the cookie list.");
 			System.out.println("\tlistCookies()\t\t\t\t\t- Retrieves the list of cookies that will be written.");
 			System.out.println("\twriteCookies()\t\t\t\t\t- Write the cookies file.");
 			System.out.println("\tremoveCookies()\t\t\t\t\t- Remove the cookies file.");
 			System.out.println("\t[n:][t:][e:]event(eventType, fqn, value)\t- Send n events to the deployed service, with a delay of t seconds, and a e error.\n\t\t\t\t\t\t\t " +
 							   " Event type should be one of (agent, hw).");
 			
 			System.out.println("\tshutdown\t\t\t\t\t\t- Shutdown the server.");
 			System.out.println("\tquit\t\t\t\t\t\t- Close the client.");
 			System.out.println();
 		} else {
 			if (!s.trim().equals(""))
 				System.out.println("\tCommand not recognized.");
 		}
 	}
 	
 	public static void addCookies(String name, String value) {
 		cookies.put(name, value);
 	}
 	
 	public static void removeCookie(String name) {
 		cookies.remove(name);
 	}
 	
 	public static void printCookies() {
 		
 		System.out.println("\n\n\tCookie List\n---------------------------------------------");
 		
 		for (String cookie: cookies.keySet()) {
 			 System.out.println("\t* " + cookie + "= " + cookies.get(cookie) + "\n");
 		}
 	}
 	
 	public static void writeCookies() {
 		File cookieFile = new File(securityCookieFile);
 		
 		try {
 			
 			System.out.println("Writing to: " + securityCookieFile);
 			cookieFile.createNewFile();
 			FileWriter fw = new FileWriter(cookieFile);
 			
 			for (String cookie: cookies.keySet()) {
 				fw.write(cookie + "=" + cookies.get(cookie) + "\n");
 			}
 			
 			fw.flush();
 			fw.close();
 			
 		} catch (IOException e) {
 			System.out.println("I/O Exception writing the cookie: " + e.getMessage());
 		}
 		
 	}
 	
 	public static void removeCookies() {
 		File cookieFile = new File(securityCookieFile);
 		
 		cookieFile.delete();
 	}
 	
 	public static String[] getParams(String s) {
 		return s.substring(s.indexOf('(')+1, s.indexOf(')')).split(",");
 	}
 	
 	
 	public static Reference getServiceURI(String customerName, String serviceName) {
 		return new Reference(prop.getProperty(HOST)+prop.getProperty(KEY_PORT) + RESERVOIR_PATH + "/" + customerName + "/vapp/" + serviceName);
 	}
 	
 	public static Reference createServiceURI(String customerName, String serviceName) {
 		return new Reference(prop.getProperty(HOST)+prop.getProperty(KEY_PORT) + RESERVOIR_PATH + "/" + customerName + prop.getProperty(KEY_INSTANTIATE));
 	}
 	
 	public static String createServiceCommand(String customerName, String serviceName, String ovfUrl) throws IOException {
 		
 		if (!scriptmode)
 		  System.out.println("\nDeploying service [" + serviceName + "] for customer [" + customerName + "] defined in [" + ovfUrl + "]\n\n");
 		
 		Reference serviceItemsUri= createServiceURI(customerName, serviceName);
 		
 		String taskId =null;
    	    try {
    	    	taskId = createServiceXML(serviceName, ovfUrl, client, serviceItemsUri);
 		} catch (SAXException e) {
 			System.out.println("XML error in service creation: " + e.getMessage());
 		} catch (ParserConfigurationException e) {
 			System.out.println("TEST ERROR: service not created");
 		}
 		
 		return taskId;
 	}
 	
 	public static Reference getCustomerURI(String customerName) {
 		return new Reference(prop.getProperty(HOST)+prop.getProperty(KEY_PORT) + RESERVOIR_PATH + "/" + customerName);
 	}
 	
 	public static Reference createCustomerCommand(String customerName) {
 		
 		System.out.println("\nCreating customer [" + customerName +"]");
 		
 		Reference customerURI = new Reference(prop.getProperty(HOST)+prop.getProperty(KEY_PORT) + RESERVOIR_PATH);
 		try {
 			createCustomerXML(customerName, client, customerURI);
 		} catch (IOException e) {
 			System.out.println("I/O Exception creating the customer: " + e.getMessage());
 			return customerURI;
 		}
 		
 		// Retrieve the customer list and check its there
 		customerURI = getCustomerURI(customerName);
    	 	try {
 			get(client, customerURI);
 		} catch (IOException e) {
 			System.out.println("I/O Exception retrieving customer data: " + e.getMessage());
 			return customerURI;
 		}
 		
 		return customerURI;
 	}
 	
 	public static String infoCustomerCommand(Reference customerUri) {
 		try {
 			return get(client, customerUri);
 		} catch (IOException e) {
 			System.out.println("I/O Exception retrieving customer data: " + e.getMessage());
 			return "ERROR";
 		} 
 	}
 	
 	public static String infoServiceCommand(Reference itemUri) {
 		try {
 			return get(client, itemUri);
 		} catch (IOException e) {
 			System.out.println("I/O Exception retrieving service data: " + e.getMessage());
 			return "ERROR";
 		} 
 	}
 	
 	
 	public static  void deleteServiceCommand(Reference itemUri) {
 		if (!delete(client, itemUri))		
 			System.out.println("The service could not be deleted");
 		else
 			System.out.println("Deleted service");
 	}
 	
 	public static  void deleteCustomerCommand(Reference customerURI) {
         if (!delete(client, customerURI))
         	System.out.println("DELETE request didn't succeed");
         else
         	System.out.println("Deleted customer");
 	}
 	
     public static Reference createCustomer(String customerName, Client client,
             Reference customerURI) {
         	    	   	
             // Gathering informations into a Web form.
             Form form = new Form();
             form.add("customerName", customerName);   
             Representation rep = form.getWebRepresentation();
 
             // Launch the request
             Response response = client.post(customerURI, rep);
             if (response.getStatus().isSuccess()) {
                 return response.getEntity().getIdentifier();
             }
 
             return null;
         }
     
     public static Reference createCustomerXML(String customerName, Client client, Reference customerURI) throws IOException {
     	
     	DomRepresentation domr = new DomRepresentation(MediaType.APPLICATION_XML);
     	
     	Document doc = domr.getDocument();
     	
     	Element root= doc.createElement("customer");
     	doc.appendChild(root);
     	
     	Element name= doc.createElement("name");
     	name.appendChild(doc.createTextNode(customerName));
     	root.appendChild(name);
 
         Response response = client.post(customerURI, domr);
         if (response.getStatus().isSuccess()) {
             return response.getEntity().getIdentifier();
         }    	
     	
     	return null;
     }
     /**
      * Try to create a new SMI compute item.
      * 
      * @param item
      *                the new item.
      * @param client
      *                the Restlet HTTP client.
      * @param computeItemsUri
      *                where to POST the data.
      * @return the Reference of the new resource if the creation succeeds, null
      *         otherwise.
      */
     public static Reference createService(String serviceName, String ovf, Client client,
         Reference serviceItemsUri) {
     	    	   	
         // Gathering informations into a Web form.
         Form form = new Form();
         form.add("serviceName", serviceName);
         form.add("ovf", ovf);        
         Representation rep = form.getWebRepresentation();
         
         // Launch the request
         Response response = client.post(serviceItemsUri, rep);
         if (response.getStatus().isSuccess()) {
             return response.getEntity().getIdentifier();
         }
 
         return null;
     }
     
     public static String createServiceXML(String serviceName, String ovf, Client client,
             Reference serviceItemsUri) throws IOException, SAXException, ParserConfigurationException {
     	
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 
         String s;
         StringBuffer completeOVF = new StringBuffer();
         URL ovfURL = new URL(ovf);
         InputStream is;
         
         try {
             is = ovfURL.openStream();
             
             BufferedReader buf = new BufferedReader(new InputStreamReader(is));
 			while ((s = buf.readLine()) != null) {                 
             	completeOVF.append(s);                            
             }
             is.close();
         } catch (IOException ex) {
         	
         	System.out.println("It was imposible to get an OVF from that URL");
         	return null;
         }
         
         Document doc = db.parse(ovf);
         
         DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		
 		try {
 			docBuilder = dbfac.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			System.out.println("Error configuring a XML Builder.");
 			return null;
 		}
 		
 		Document docOvf = docBuilder.newDocument();
 		Element root = docOvf.createElement("InstantiateOVFParams");
 		docOvf.appendChild(root);
 		root.setAttribute("name", serviceName);
 		
 		root.appendChild(docOvf.importNode(doc.getDocumentElement(), true));
         
         DomRepresentation domrep = new DomRepresentation(MediaType.APPLICATION_XML, docOvf);
  
         // Launch the request
         Response response = client.post(serviceItemsUri + "?serviceName=" + serviceName, domrep);
         if (response.getStatus().isSuccess()) {
         	String text =  response.getEntity().getText();
         	String idTask = text.substring(text.indexOf("href=\"")+"href=\"".length(),
         			text.indexOf("\" startTime"));
         	
             return idTask;
           
         }        
     	
     	return null;
     }
     
     /**
      * Prints the resource's representation.
      * 
      * 
      * @param client
      *                client Restlet.
      * @param reference
      *                the resource's URI.
      *                	if the reference is to a resource pool (e.g. /compute) is provides the list of all the available resources (e.g. ONE's occi-compute/occi-storage list command)
      *                  if the reference is to a specific resource, then it is equivalent to occi-compute/occi-storage show 
      * @throws IOException
      */
     public static String get(Client client, Reference reference)
             throws IOException {
         Response response = client.get(reference);
         if (response.getStatus().isSuccess()) {
             if (response.isEntityAvailable()) {
             	return response.getEntity().getText();
             } else {
             	return "No response from the server";
             }
         } else {
         	System.out.println("GET request didn't succeed");
         	return "ERROR";
         }
     }
     
     /**
      * Try to update an SMI Service resource
      * 
      * @param item
      *                the resource.
      * @param client
      *                the Restlet HTTP client.
      * @param itemUri
      *                the resource's URI.
      */
     public static boolean update(String serviceName, String ovf, Client client, Reference itemUri) {
         // Gathering informations into a Web form.
         Form form = new Form();
         form.add("serviceName", serviceName);
         form.add("ovf", ovf);
         
         Representation rep = form.getWebRepresentation();
 
         // Launch the request
         Response response = client.put(itemUri, rep);
         return response.getStatus().isSuccess();
     }  
     
     /**
      * Try to delete a resource regardless of its specific type
      * 
      * @param client
      *                the Restlet HTTP client.
      * @param itemUri
      *                the resource's URI.
      */
     public static boolean delete(Client client, Reference itemUri) {
         // Launch the request
         Response response = client.delete(itemUri);
         return response.getStatus().isSuccess();
     }
 	
 
     public static void sendRESTMessage(String eventType, long t_0, long delta_t, String fqn, double value) {
     	
     	String message =	"<" + ROOT_MONITORING_TAG_NAME + ">" +
     					 		"<" + EVENT_TYPE_TAG_NAME + ">" + eventType +
     					 		"</" + EVENT_TYPE_TAG_NAME + ">" +
     					 		"<" + T_0_TAG_NAME + ">" + t_0 +
     					 		"</" + T_0_TAG_NAME + ">" +
     					 		"<" + T_DELTA_TAG_NAME + ">" + delta_t +
     					 		"</" + T_DELTA_TAG_NAME + ">" +
     					 		"<" + FQN_TAG_NAME + ">" + fqn +
     					 		"</" + FQN_TAG_NAME + ">" +
     					 		"<" + VALUE_TAG_NAME + ">" + value +
     					 		"</" + VALUE_TAG_NAME + ">" +
     					 	"</" + ROOT_MONITORING_TAG_NAME + ">" ;
         		
 		PostMethod post = new PostMethod("http://" + restServerHost + ":" + restServerPort + restPath);
 		
 		RequestEntity request = null;		
 		try {
 			request = new StringRequestEntity(message, "text/xml", null);
 		} catch (UnsupportedEncodingException ex) {
 			System.out.println("This should never happen? Cannot create a String request entity with null char encoding");
 			return;
 		}
 		
 		post.setRequestEntity(request);
 		
 		try {			
 			httpClient.executeMethod(post);
 			System.out.println("\n\tResult status: " + post.getStatusText() + "\n");
 		} catch (HttpException ex) {
 			System.out.println("HTTPException caught when trying to send POST message" + ex.getMessage());
 			return;
 		} catch (IOException ex) {
 			System.out.println("IOException caught when trying to send POST message: " + ex.getMessage());
 			return;
 		} finally {
 			post.releaseConnection();
 		}
 		
     }
 }
