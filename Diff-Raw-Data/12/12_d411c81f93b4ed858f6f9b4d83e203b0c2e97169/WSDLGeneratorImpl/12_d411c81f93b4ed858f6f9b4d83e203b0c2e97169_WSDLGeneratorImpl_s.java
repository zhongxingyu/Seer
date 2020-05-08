 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008, Red Hat Middleware LLC, and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.savara.tools.wsdl.generator;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.savara.contract.model.Contract;
 import org.savara.contract.model.Interface;
 import org.savara.contract.model.Namespace;
 import org.savara.protocol.contract.generator.ContractGenerator;
 import org.savara.protocol.contract.generator.ContractGeneratorFactory;
 import org.savara.tools.common.ArtifactType;
 import org.savara.tools.common.generation.AbstractGenerator;
 import org.savara.common.logging.FeedbackHandler;
 import org.savara.common.logging.MessageFormatter;
 import org.savara.common.util.XMLUtils;
 import org.savara.wsdl.generator.WSDLGeneratorFactory;
 import org.savara.wsdl.generator.soap.SOAPDocLitWSDLBinding;
 import org.scribble.protocol.model.ProtocolModel;
 import org.scribble.protocol.model.Role;
 
 /**
  * This class provides the WSDL contract generator.
  */
 public class WSDLGeneratorImpl extends AbstractGenerator {
 
 	private static final String GENERATOR_NAME = "WSDL";
 
 	private static Logger logger = Logger.getLogger(WSDLGeneratorImpl.class.getName());
 	
 	public WSDLGeneratorImpl() {
 		super(GENERATOR_NAME);
 	}
 
 	/**
 	 * This method returns the artifact type that will be generated.
 	 * 
 	 * @return The artifact type
 	 */
 	public ArtifactType getArtifactType() {
 		return(ArtifactType.ServiceContract);
 	}
 	
 	/**
 	 * This method generates some artifacts based on the supplied model and
 	 * role.
 	 * 
 	 * If specified, the optional project name will be used to create a
 	 * new Eclipse project for the generated artifacts. If no project name
 	 * is specified, then the artifacts will be created in the model
 	 * resource's project.
 	 * 
 	 * @param model The protocol model
 	 * @param role The role
 	 * @param projectName The optional project name
 	 * @param modelResource The resource associated with the model
 	 * @param journal The journal for reporting issues
 	 */
 	public void generate(ProtocolModel model, Role role, String projectName,
 						IResource modelResource, FeedbackHandler journal) {
 
 		Contract contract=null;
 		ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
 		
 		if (cg != null) {
 			contract=cg.generate(model.getProtocol(), null, role, journal);
 		}
 		
 		try {
 			if (contract != null) {
 				javax.wsdl.xml.WSDLWriter writer=
 					javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter();
 				org.savara.wsdl.generator.WSDLGenerator generator=
 								WSDLGeneratorFactory.getWSDLGenerator();
 			
 				// Generate WSDL folder
 				IPath wsdlFolderPath=modelResource.getParent().getFullPath();
 	
 				// Generate definition
 				java.util.List<javax.wsdl.Definition> defns=generator.generate(contract,
 							new SOAPDocLitWSDLBinding(), journal);
 			
 				// Check if contract has atleast one message exchange pattern
 				boolean f_hasMEP=false;
 				
 				java.util.Iterator<Interface> iter=contract.getInterfaces().iterator();
 				
 				while (f_hasMEP == false && iter.hasNext()) {
 					Interface intf=iter.next();
 					f_hasMEP = (intf.getMessageExchangePatterns().size() > 0);
 				}
 				
 				for (int i=defns.size()-1; i >= 0; i--) {
 					javax.wsdl.Definition defn=defns.get(i);
 					
 					// Check if definition has a port type
 					if (defn.getPortTypes().size() > 0 || defn.getMessages().size() > 0
 							|| (f_hasMEP && defn.getServices().size() > 0)) {
 						byte[] b=null; // Bytes to write out
 						
 						String num="";
 						if (i > 0) {
 							num += i;
 						}
 				
 						String filename=model.getProtocol().getName()+"_"+role.getName()+num+".wsdl";
 			
 						if (i > 0) {
 							javax.wsdl.Import imp=defns.get(0).createImport();
 							
 							imp.setDefinition(defn);
 							imp.setNamespaceURI(defn.getTargetNamespace());
 							imp.setLocationURI(filename);
 							
 							defns.get(0).addImport(imp);
 							
 							java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
 							
 							writer.writeWSDL(defn, baos);
 							
 							b=baos.toByteArray();
 							
 							baos.close();
 							
 						} else {
 							
 							// NOTE: Unfortunate workaround due to issue with WSDLWriter not
 							// generating output for extensible elements created to represent
 							// the xsd:schema/xsd:import elements. So instead had to obtain
 							// the DOM document and insert the relevant elements.
 							
 							org.w3c.dom.Document doc=writer.getDocument(defn);
 							
 							if (contract.getNamespaces().size() > 0) {
 								org.w3c.dom.Element defnElem=doc.getDocumentElement();
 								
 								// Added types node
 								org.w3c.dom.Element types=doc.createElementNS("http://schemas.xmlsoap.org/wsdl/",
 															"types");
 								
 								org.w3c.dom.Element schema=doc.createElementNS("http://www.w3.org/2001/XMLSchema",
 															"schema");
 								
 								types.appendChild(schema);		
 								
 								// Generate imports for specified message schema
 								for (Namespace ns : contract.getNamespaces()) {
 									
 									if (ns.getSchemaLocation() != null &&
 												ns.getSchemaLocation().trim().length() > 0) {
 		
 										java.util.StringTokenizer st=new java.util.StringTokenizer(ns.getSchemaLocation());
 										
 										while (st.hasMoreTokens()) {
 											String location=st.nextToken();
 											IFile file=modelResource.getParent().getFile(new Path(location));
 										
 											org.w3c.dom.Element imp=doc.createElementNS("http://www.w3.org/2001/XMLSchema",
 															"import");
 											
 											imp.setAttribute("namespace", ns.getURI());
 											
 											if (file.exists()) {
 												IPath relative=file.getFullPath().makeRelativeTo(wsdlFolderPath);
 												imp.setAttribute("schemaLocation", relative.toPortableString());
 											} else {
 												imp.setAttribute("schemaLocation", location);
 											}
 											
 											schema.appendChild(imp);					
 										}
 									}
 								}
 		
 								defnElem.insertBefore(types, defnElem.getFirstChild());
 							}
 							
 							// Create bytearray from DOM
 							java.io.ByteArrayOutputStream xmlstr=
 								new java.io.ByteArrayOutputStream();
 							
 							DOMSource source=new DOMSource();
 							source.setNode(doc);
 							
 							StreamResult result=new StreamResult(xmlstr);
 							
 							Transformer trans=
 									TransformerFactory.newInstance().newTransformer();
 							trans.transform(source, result);
 							
 							xmlstr.close();
 							
 							b = XMLUtils.format(new String(xmlstr.toByteArray())).getBytes();
 						}
 						
 						IPath wsdlPath=wsdlFolderPath.append(filename);
 			
 						IFile wsdlFile=modelResource.getProject().getWorkspace().getRoot().getFile(wsdlPath);
 	
 						wsdlFile.create(null, true,
 								new org.eclipse.core.runtime.NullProgressMonitor());
 						
 						wsdlFile.setContents(new java.io.ByteArrayInputStream(b), true, false,
 									new org.eclipse.core.runtime.NullProgressMonitor());
 					}
 				}
 			}
 		} catch(Exception e) {
 			logger.log(Level.SEVERE, "Failed to create WSDL", e);
 			
 			journal.error(MessageFormatter.format(java.util.PropertyResourceBundle.getBundle(
 					"org.savara.tools.wsdl.Messages"),
								"SAVARA-WDSLTOOLS-00001"), null);
 		}
 	}
 }
