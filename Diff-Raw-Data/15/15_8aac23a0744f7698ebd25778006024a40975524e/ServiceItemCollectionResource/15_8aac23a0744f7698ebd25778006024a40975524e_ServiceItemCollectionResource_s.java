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
 
 package com.telefonica.claudia.smi.deployment;
 
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.log4j.Logger;
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.DomRepresentation;
 import org.restlet.resource.Representation;
 import org.restlet.resource.Resource;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.StringRepresentation;
 import org.restlet.resource.Variant;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.telefonica.claudia.smi.URICreation;
 import com.telefonica.claudia.smi.task.TaskManager;
 
 
 /**
  * Resource that manages a list of items.
  * 
  */
 public class ServiceItemCollectionResource extends Resource {	
 	
 	private static Logger log = Logger.getLogger("com.telefonica.claudia.smi.deployment.ServiceItemsResource");
 	
 	private static final String STORAGE_FOLDER="./storage";
 	
 	String orgId;
 	String vdcId;
 	
     public ServiceItemCollectionResource(Context context, Request request, Response response) {
         super(context, request, response);
 
         // Allow modifications of this resource via POST requests.
         setModifiable(true);
         
         this.vdcId = (String) getRequest().getAttributes().get("vdc-id");
         
 		this.orgId = new String((String) getRequest().getAttributes().get("org-id"));
         
         if (vdcId != null && orgId != null) {
         	
             // Define the supported variant.
             getVariants().add(new Variant(MediaType.TEXT_XML));
             // By default a resource cannot be updated.
             setModifiable(true);
             
         } else {
             // This resource is not available.
             setAvailable(false);
         } 
         
        log.info("Services list created");
         
     }
 
     private String loadOVF(String ovf) throws MalformedURLException {
     	
     	log.info("Getting manifest file from URL " + ovf);
     	
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
         	log.error("Could not read remote file " + ovf, ex);
         	return null;
         }
         
         return completeOVF.toString();
     }
     
     private void storeOVF(String serviceName, String ovfContents) {
     	
     	File ovfFile = new File(STORAGE_FOLDER + "/" + serviceName + ".ovf");
     	
     	
     	FileOutputStream fis;
 		try {
 			fis = new FileOutputStream(ovfFile);
 			
 			DataOutputStream dos = new DataOutputStream(fis);
 			dos.writeBytes(ovfContents);
 			dos.close();
 			
 		} catch (FileNotFoundException e) {
 			log.error("File folder could not be found: " + e.getMessage());
 		} catch (IOException e) {
 			log.error("I/O error on ovf storage: " + e.getMessage());
 		}
     }	
     
     /**
      * Handle POST requests: create a new item.
      */
     public void acceptRepresentation(Representation entity)
             throws ResourceException {
     	
     	log.debug("Handling Service creation request for customer: " + vdcId);
     	
     	long taskId;
     	
         if (entity.getMediaType().equals(MediaType.APPLICATION_XML)) {
     		
         	log.debug("XML request received");
         	
     		// Create the VEEReplica from the received data.
     		DomRepresentation domR = new DomRepresentation(entity);
 			
 			// Invoke SM plugin to perform the actual deployment
     		DeploymentDriver actualDriver= (DeploymentDriver) getContext().getAttributes().get(DeploymentApplication.ATTR_PLUGIN_DEPLOYMENT);
 			String serviceName = "";
 			
 			if (serviceName == null || serviceName.equals("")) {
 				serviceName = "AnonymousService";
 			}
 			
 			try {
 				Document doc = domR.getDocument();
 				
 				NodeList nl = doc.getElementsByTagName("InstantiateOVFParams");
 				
 				if (nl==null || nl.getLength() <1) {
 					log.error("Element <InstantiateOVFParams> not found.");
 					getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 					return;
 				}
 					
 				serviceName = nl.item(0).getAttributes().getNamedItem("name").getTextContent();
 				log.debug("Service name parsed: " + serviceName);
 				
 				NodeList envelopeItems = doc.getElementsByTagName("Envelope");
 				
 				if (envelopeItems.getLength() != 1) {
 					log.error("Envelope items not found.");
 					getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
 					return;
 				}
 				
 				Node ovfNode = envelopeItems.item(0);
 				
 				DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
 				DocumentBuilder docBuilder;
 				try {
 					docBuilder = dbfac.newDocumentBuilder();
 				} catch (ParserConfigurationException e) {
 					log.error("Error configuring a XML Builder.");
 					getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
 					return;
 				}
 				
 				Document docOvf = docBuilder.newDocument();
 				docOvf.appendChild(docOvf.importNode(ovfNode, true));
 				
 				StringWriter sw = new StringWriter();
 				DOMSource domSource = new DOMSource(docOvf);
 				StreamResult streamResult = new StreamResult(sw);
 				TransformerFactory tf = TransformerFactory.newInstance();
 				Transformer serializer;
 				try {
 					serializer = tf.newTransformer();
 					serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
 					serializer.setOutputProperty(OutputKeys.INDENT,"yes");
 					
 					serializer.transform(domSource, streamResult);
 					
 				} catch (TransformerConfigurationException e) {
 					log.error("Error transforming XML content. The OVF document may be malformed: " + e.getMessage());
 					getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
 					return;
 				} catch (TransformerException e) {
 					log.error("Error transforming XML content. The OVF document may be malformed." + e.getMessage());
 					getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
 					return;
 				}
 				
 				storeOVF(serviceName, sw.toString());
 				
 				log.debug("Deploying to the SM.");
 				
 				taskId = actualDriver.deploy(URICreation.getFQN(orgId, vdcId), serviceName, sw.toString());
 				
				log.debug("FQN got: " + serviceName);
 
 	            // Set the response's status and entity
 	            getResponse().setStatus(Status.SUCCESS_CREATED);
 	            //DomRepresentation rep = new DomRepresentation(MediaType.APPLICATION_XML, TaskManager.getInstance().getTask(taskId).getXmlDescription());
 	           
 	            StringRepresentation rep = new StringRepresentation(TaskManager.getInstance().getTask(taskId).getStringDescription().replace("{org-id}", orgId).replace("{vdc-id}", vdcId), MediaType.TEXT_XML);
 				
 	            // Indicates where is located the new resource.
 	            rep.setIdentifier(URICreation.getFQN(orgId, vdcId, serviceName));
 
 	            getResponse().setEntity(rep);
 			} catch (IOException e1) {
 				log.error("OVF of the service was not well formed or it contained some errors.");
 				
 				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 				return;
 			}
         	            
             log.debug("Service registered successfully");
         }else{
         	
         	log.debug("URL-encoded request received");
         	
         	Form form = new Form(entity);  
         	String serviceName = form.getFirstValue("serviceName");  
         	String ovf= form.getFirstValue("ovf");
         	        	
         	log.debug("Service name: " + serviceName);
         	log.debug("OFV URL: " + ovf);
         	
         	// Get the OVF from the given URL
         	String ovfContents= null;
         	try {
 				ovfContents = loadOVF(ovf);
 			} catch (MalformedURLException e) {
 				log.error("The given url: "+ ovf + " was not a well-formed URL");
 				
 				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 				return;
 			}
 			
 			log.debug("OVF loaded");
 
 			DeploymentDriver smConnector= (DeploymentDriver) getContext().getAttributes().get(DeploymentApplication.ATTR_PLUGIN_DEPLOYMENT);
         	
         	// Deploy the service
         	try {
         		taskId = smConnector.deploy(URICreation.getFQN(orgId, vdcId), serviceName, ovfContents);
 			
 				log.debug("FQN got: " + serviceName);
 				
 				storeOVF(serviceName, ovfContents);
 			} catch (IOException e) {
 				log.error("Time out waiting for the Lifecycle Controller.");
 				
 				getResponse().setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
 				return;
 			}
         	
             // Set the response's status and entity
             getResponse().setStatus(Status.SUCCESS_CREATED);
             Representation rep = new StringRepresentation(serviceName,MediaType.TEXT_PLAIN);
             // Indicates where is located the new resource.
             rep.setIdentifier(URICreation.getFQN(orgId, vdcId, serviceName));
             
             getResponse().setEntity(rep);
             log.debug("Service registered successfully");
         }
     }
 
     /**
      * Returns a listing of all registered items.
      */
     @Override
     public Representation represent(Variant variant) throws ResourceException {
         // Generate the right representation according to its media type.
         if (MediaType.TEXT_XML.equals(variant.getMediaType())) {
             try {
             	DeploymentDriver actualDriver= (DeploymentDriver) getContext().getAttributes().get(DeploymentApplication.ATTR_PLUGIN_DEPLOYMENT);
                 StringRepresentation representation = new StringRepresentation(actualDriver.getVdc(URICreation.getFQN(orgId, vdcId)), MediaType.TEXT_XML);
                 
                 log.info("List of services returned");
                 
                 return representation;
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         return null;
     }
 
 }
 
