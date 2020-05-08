 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.neethi;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.impl.builder.StAXOMBuilder;
 
 /**
  * PolicyReference is a wrapper that holds explict PolicyReferences.
  */
 public class PolicyReference implements PolicyComponent {
 
     private String uri;
 
     /**
      * Sets the Policy URI
      * @param uri the Policy URI
      */
     public void setURI(String uri) {
         this.uri = uri;
     }
 
     /**
      * Gets the Policy URI that is refered by self.
      * @return a String that is the Policy URI refered by self
      */
     public String getURI() {
         return uri;
     }
 
     public boolean equal(PolicyComponent policyComponent) {
         if (Constants.TYPE_POLICY_REF != policyComponent.getType()) {
             return false;
         }
         
         String URI = ((PolicyReference) policyComponent).getURI();
         if (URI != null && URI.length() != 0) {
             return URI.equals(this.uri);
         } 
         
         return false;
     }
 
 
     /**
      * Returns short value of Constants.TYPE_POLICY_REF 
      */
     public short getType() {
         return Constants.TYPE_POLICY_REF;
     }
 
     /**
      * Throws an UnsupportedOperationException since PolicyReference.normalize()
      * can't resolve the Policy that it refers to unless a PolicyRegistry is
      * provided.
      */
     public PolicyComponent normalize() {
         throw new UnsupportedOperationException("PolicyReference.normalize() is meaningless");
     }
     
     /**
      * Returns normalized version of the Policy that is refered by self. The specified 
      * PolicyRegistry is used to lookup for the Policy that is refered and <tt>dee</tt> 
      * indicates the level of normalization fo the returning Policy.
      * 
      * @param reg the PolicyRegistry that is used to resolved the Policy refered by self
      * @param deep the falg to indicate whether returning Policy should be fully normailized
      * @return the normalized version fo the Policy refered by self
      */
     public PolicyComponent normalize(PolicyRegistry reg, boolean deep) {
         String key = getURI();
         int pos = key.indexOf("#");
         if (pos == 0) {
             key = key.substring(1);
         }else if(pos > 0){
         	key = key.substring(0, pos);
         }
         
         Policy policy = reg.lookup(key);        
         
         if (policy == null) {
         	policy = getRemoteReferencedPolicy(key);
         	
         	if(policy == null){
         		throw new RuntimeException(key + " can't be resolved" );
         	}
         	
         	reg.register(key, policy);
             
         }
         
         return policy.normalize(reg, deep);
     }
 
     public void serialize(XMLStreamWriter writer) throws XMLStreamException {
 
         String wspPrefix = writer.getPrefix(Constants.URI_POLICY_NS);
         
         if (wspPrefix == null) {
             wspPrefix = Constants.ATTR_WSP;
             writer.setPrefix(wspPrefix, Constants.URI_POLICY_NS);
         }
         
         writer.writeStartElement(wspPrefix, Constants.ELEM_POLICY_REF, Constants.URI_POLICY_NS);
        writer.writeNamespace(Constants.ATTR_WSP, Constants.URI_POLICY_NS);
         writer.writeAttribute(Constants.ATTR_URI, getURI());
         
         writer.writeEndElement();
     }
     
     public OMElement getRemoteReferedElement(String uri){
     	OMElement documentElement = null;
     	
     	try{    		    		
     		
 	    	//create java.net URL pointing to remote resource			
 			URL url = new URL(uri);
 			URLConnection connection = url.openConnection();
 			connection.setDoInput(true);
 			
 			//create stax parser with the url content
 			XMLStreamReader parser = XMLInputFactory.newInstance().
 			                                         createXMLStreamReader(connection.getInputStream()); 
 			
 	        //get the root element (in this case the envelope)
 			StAXOMBuilder builder = new StAXOMBuilder(parser); 
 			documentElement = builder.getDocumentElement();	
 			
         }catch(XMLStreamException se){        	        	
         	throw new RuntimeException("Bad policy content: " + uri);
         }catch(MalformedURLException mue){        	
         	throw new RuntimeException("Malformed uri: " + uri);
         }catch(IOException ioe){        
         	throw new RuntimeException("Cannot reach remote resource: " + uri);
         }       
 		
 		return documentElement;
     }
     
     
     
     public Policy getRemoteReferencedPolicy(String uri) {
     	Policy policy = null;
     	
     	//extract the remote resource contents
     	OMElement policyElement = getRemoteReferedElement(uri);
     	
     	//call the policy engine with already extracted content
     	policy = PolicyEngine.getPolicy(policyElement);
     	
 		return policy;
     }
 }
