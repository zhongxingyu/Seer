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
 package org.savara.tools.bpel.generator;
 
 import java.io.ByteArrayOutputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.savara.bpel.generator.ProtocolToBPELModelGenerator;
 import org.savara.bpel.model.*;
 import org.savara.bpel.util.BPELModelUtil;
 import org.savara.common.logging.FeedbackHandler;
 import org.savara.common.logging.MessageFormatter;
 import org.savara.common.model.annotation.Annotation;
 import org.savara.common.model.annotation.AnnotationDefinitions;
 import org.savara.common.util.XMLUtils;
 import org.savara.contract.model.Contract;
 import org.savara.contract.model.Interface;
 import org.savara.contract.model.Namespace;
 import org.savara.protocol.contract.generator.ContractGenerator;
 import org.savara.protocol.contract.generator.ContractGeneratorFactory;
 import org.savara.tools.common.ArtifactType;
 import org.savara.tools.common.generation.AbstractGenerator;
 import org.savara.wsdl.generator.WSDLGeneratorFactory;
 import org.savara.wsdl.generator.soap.SOAPDocLitWSDLBinding;
 import org.scribble.protocol.model.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jst.common.project.facet.WtpUtils;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 /**
  * This class provides the mechanism for generating BPEL
  * service artefacts.
  */
 public class BPELGeneratorImpl extends AbstractGenerator {
 
 	private static final String GENERATOR_NAME = "BPEL";
 	private static final String PROCESS_EVENTS_LABEL = "process-events";
 	private static final String SCHEMA_LOCATION_ATTR = "schemaLocation";
 	private static final String INCLUDE_ELEMENT = "include";
 	private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
 	private static final String WSDL_IMPORT = "wsdl:import";
 	private static final String BPEL_DEPLOY_DESCRIPTOR_FILENAME = "deploy.xml";
 	private static final String XMLNS_PREFIX = "xmlns:";
 	private static final String SERVICE_LABEL = "service";
 	private static final String PROVIDE_LABEL = "provide";
 	private static final String PARTNER_LINK_LABEL = "partnerLink";
 	private static final String INVOKE_LABEL = "invoke";
 	private static final String ACTIVE_LABEL = "active";
 	private static final String PROCESS_LABEL = "process";
 	private static final String DEPLOY_LABEL = "deploy";
 	private static final String APACHE_ODE_NAMESPACE = "http://www.apache.org/ode/schemas/dd/2007/03";
 	private static final String PORT_TYPE_LABEL = "portType";
 	private static final String TARGET_NAMESPACE_LABEL = "targetNamespace";
 	private static final String PLNK_ROLE = "plnk:role";
 	private static final String NAME_LABEL = "name";
 	private static final String PLNK_PARTNER_LINK_TYPE = "plnk:partnerLinkType";
 	private static final String WSDL_DEFINITIONS = "wsdl:definitions";
 	private static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
 	private static final String PLNKTYPE_NS = "http://docs.oasis-open.org/wsbpel/2.0/plnktype";
 	private static final String XMLNS_WSDL = "xmlns:wsdl";
 	private static final String XMLNS_PLNK = "xmlns:plnk";	
 	private static final String BPEL_PATH = "bpelContent";
 
 	private static Logger logger = Logger.getLogger(BPELGeneratorImpl.class.getName());
 
 	/**
 	 * This is the constructor for the generator.
 	 * 
 	 */
 	public BPELGeneratorImpl() {
 		super(GENERATOR_NAME);
 	}
 	
 	/**
 	 * This method returns the artifact type that will be generated.
 	 * 
 	 * @return The artifact type
 	 */
 	public ArtifactType getArtifactType() {
 		return(ArtifactType.ServiceImplementation);
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
 	 * @param handler The feedback handler for reporting issues
 	 */
 	public void generate(ProtocolModel model, Role role, String projectName,
 						IResource modelResource, FeedbackHandler handler) {
 		
 		if (logger.isLoggable(Level.FINE)) {
 			logger.fine("Generate local model '"+role+"' for: "+model);
 		}
 		
 		ProtocolModel local=getProtocolModelForRole(model, role, modelResource, handler);
 		
 		if (local != null) {
 			// TODO: Obtain model generator from manager class (SAVARA-156)
 			ProtocolToBPELModelGenerator generator=new ProtocolToBPELModelGenerator();
 			
 			Object target=generator.generate(local, handler, null);
 			
 			if (target instanceof TProcess) {
 				try {
 					generateRoleProject(model, projectName, role, (TProcess)target,
 							local, modelResource, handler);
 				} catch(Exception e) {
 					logger.log(Level.SEVERE, "Failed to create BPEL project '"+projectName+"'", e);
 					
					handler.error(MessageFormatter.format(java.util.PropertyResourceBundle.getBundle(
							"org.savara.tools.bpel.Messages"), "SAVARA-BPELTOOLS-00001",
 										projectName), null);
 				}
 			}
 		}
 	}
 	
 	protected void generateRoleProject(ProtocolModel model, String projectName, Role role,
 			TProcess bpelProcess, ProtocolModel localcm,
 					IResource resource, FeedbackHandler journal) throws Exception {
 		
 		final IProject proj=createProject(resource, projectName, journal);
 		
 		if (proj != null && bpelProcess != null) {
 
 			// Store BPEL configuration
 			IPath bpelPath=proj.getFullPath().append(
 					new Path(BPEL_PATH)).
 						append(localcm.getProtocol().getName()+"_"+
 							localcm.getProtocol().getRole().getName()+".bpel");
 			
 			IFile bpelFile=proj.getProject().getWorkspace().getRoot().getFile(bpelPath);
 			createFolder(bpelFile);
 			
 			bpelFile.create(null, true,
 					new org.eclipse.core.runtime.NullProgressMonitor());
 			
 			// Obtain any namespace prefix map
 			java.util.Map<String, String> prefixes=
 					new java.util.HashMap<String, String>();
 			
 			java.util.List<Annotation> list=
 				AnnotationDefinitions.getAnnotations(localcm.getProtocol().getAnnotations(),
 						AnnotationDefinitions.TYPE);
 			
 			for (Annotation annotation : list) {
 				if (annotation.getProperties().containsKey(AnnotationDefinitions.NAMESPACE_PROPERTY) &&
 						annotation.getProperties().containsKey(AnnotationDefinitions.PREFIX_PROPERTY)) {
 					prefixes.put((String)annotation.getProperties().get(AnnotationDefinitions.NAMESPACE_PROPERTY),
 							(String)annotation.getProperties().get(AnnotationDefinitions.PREFIX_PROPERTY));
 				}
 			}
 			
 			//String bpelText=XMLUtils.toText(bpelProcess.getDOMElement());
 			ByteArrayOutputStream os=new ByteArrayOutputStream();
 			BPELModelUtil.serialize(bpelProcess, os, prefixes);
 			
 			os.close();
 			
 			bpelFile.setContents(new java.io.ByteArrayInputStream(
 						os.toByteArray()), true, false,
 						new org.eclipse.core.runtime.NullProgressMonitor());
 			
 			// Write the WSDL files
 			generateWSDL(model, role, proj, localcm, resource, journal);		
 			
 			java.util.List<Role> roles=localcm.getProtocol().getRoles();
 			
 			for (int i=0; i < roles.size(); i++) {
 				generateWSDL(model, roles.get(i), proj, localcm, resource, journal);
 			}
 			
 			// Generate WSDL with partner link types
 			generatePartnerLinkTypes(model, role, proj, localcm, bpelProcess, journal);
 			
 			// Generate BPEL deployment descriptor
 			generateBPELDeploy(model, role, proj, localcm, bpelProcess, journal);
 		}
 	}
 	
 	protected void generateWSDL(ProtocolModel model, Role role, IProject proj, ProtocolModel localcm,
 						IResource resource, FeedbackHandler journal) throws Exception {		
 		
 		ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
 		Contract contract=null;
 		
 		if (cg != null) {
 			contract=cg.generate(model.getProtocol(), null, role, journal);
 		}
 		
 		if (contract != null) {
 			javax.wsdl.xml.WSDLWriter writer=
 				javax.wsdl.factory.WSDLFactory.newInstance().newWSDLWriter();
 			org.savara.wsdl.generator.WSDLGenerator generator=
 						WSDLGeneratorFactory.getWSDLGenerator();
 
 			// Generate BPEL folder
 			IPath bpelFolderPath=proj.getFullPath().append(
 					new Path(BPEL_PATH));
 
 			IFolder wsdlFolder=proj.getProject().getWorkspace().getRoot().getFolder(bpelFolderPath);
 			
 			createFolder(wsdlFolder);
 			
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
 					String num="";
 					if (i > 0) {
 						num += i;
 					}
 
 					String filename=getWSDLFileName(role, localcm.getProtocol().getName(), num);
 					byte[] b=null;
 					
 					if (i > 0) {
 						javax.wsdl.Import imp=defns.get(0).createImport();
 						
 						imp.setDefinition(defn);
 						imp.setNamespaceURI(defn.getTargetNamespace());
 						imp.setLocationURI(filename);
 						
 						defns.get(0).addImport(imp);					
 
 						java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
 						
 						writer.writeWSDL(defn, baos);
 						
 						b = baos.toByteArray();
 						
 						baos.close();
 						
 					} else {
 						org.w3c.dom.Document doc=writer.getDocument(defn);
 						
 						importSchemas(resource, contract, bpelFolderPath,
 									doc);
 
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
 					
 					IPath wsdlPath=bpelFolderPath.append(filename);
 					
 					IFile wsdlFile=proj.getProject().getWorkspace().getRoot().getFile(wsdlPath);
 					
 					createFolder(wsdlFile);
 					
 					if (wsdlFile.exists() == false) {
 						wsdlFile.create(null, true,
 								new org.eclipse.core.runtime.NullProgressMonitor());
 					}
 					
 					wsdlFile.setContents(new java.io.ByteArrayInputStream(b), true, false,
 								new org.eclipse.core.runtime.NullProgressMonitor());
 				}
 			}
 		}
 	}
 	
 	protected void importSchemas(IResource resource, Contract contract,
 					IPath bpelFolderPath, org.w3c.dom.Document doc) throws Exception {
 		
 		// NOTE: Unfortunate workaround due to issue with WSDLWriter not
 		// generating output for extensible elements created to represent
 		// the xsd:schema/xsd:import elements. So instead had to obtain
 		// the DOM document and insert the relevant elements.
 		
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
 						IFile file=resource.getParent().getFile(new Path(location));
 					
 						org.w3c.dom.Element imp=doc.createElementNS("http://www.w3.org/2001/XMLSchema",
 										"import");
 						
 						imp.setAttribute("namespace", ns.getURI());
 						
 						if (file.exists()) {
 							imp.setAttribute("schemaLocation", file.getProjectRelativePath().toPortableString());
 
 							// Copy schema file into generated BPEL project
 							IPath artifactPath=bpelFolderPath.append(file.getProjectRelativePath());
 							
 							IFile artifactFile=resource.getProject().getWorkspace().getRoot().getFile(artifactPath);
 
 							copySchema(file, artifactFile, bpelFolderPath);
 						} else {
 							imp.setAttribute("schemaLocation", location);
 						}
 						
 						schema.appendChild(imp);					
 					}
 				}
 			}
 
 			defnElem.insertBefore(types, defnElem.getFirstChild());
 		}
 	}
 
 	protected void copySchema(IFile srcXSDFile, IFile targetXSDFile, IPath bpelFolderPath) throws Exception {
 
 		if (targetXSDFile.exists() == false) {
 			createFolder(targetXSDFile.getParent());
 
 			targetXSDFile.create(null, true,
 					new org.eclipse.core.runtime.NullProgressMonitor());
 		}
 		
 		targetXSDFile.setContents(srcXSDFile.getContents(), true, false,
 					new org.eclipse.core.runtime.NullProgressMonitor());
 		
 		// Check XSD for further 'include' statements
 		DocumentBuilderFactory fact=DocumentBuilderFactory.newInstance();
 		fact.setNamespaceAware(true);
 
 		DocumentBuilder builder=fact.newDocumentBuilder();
 		org.w3c.dom.Document doc=builder.parse(srcXSDFile.getContents());
 
 		org.w3c.dom.NodeList nl=doc.getElementsByTagNameNS(XML_SCHEMA, INCLUDE_ELEMENT);
 		
 		for (int i=0; i < nl.getLength(); i++) {
 			org.w3c.dom.Node includeNode=nl.item(i);
 			
 			if (includeNode instanceof org.w3c.dom.Element) {
 				String schemaLocation=((org.w3c.dom.Element)includeNode).getAttribute(SCHEMA_LOCATION_ATTR);
 				
 				// Check if a relative path
 				IFile file=srcXSDFile.getParent().getFile(new Path(schemaLocation));
 				
 				if (file.exists()) {
 					
 					IPath artifactPath=bpelFolderPath.append(file.getProjectRelativePath());
 					
 					IFile artifactFile=file.getProject().getWorkspace().getRoot().getFile(artifactPath);
 
 					copySchema(file, artifactFile, bpelFolderPath);
 				}
 			}
 		}
 	}
 	
 	protected void generatePartnerLinkTypes(ProtocolModel model, Role role, IProject proj, ProtocolModel localcm,
 			TProcess bpelProcess, FeedbackHandler journal) throws Exception {	
 		
 		org.w3c.dom.Document doc=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();;
 		
 		org.w3c.dom.Element defn=doc.createElement(WSDL_DEFINITIONS);
 		doc.appendChild(defn);
 		
 		defn.setAttribute(XMLNS_PLNK, PLNKTYPE_NS);
 		defn.setAttribute(XMLNS_WSDL, WSDL_NS);
 		
 		defn.setAttribute(TARGET_NAMESPACE_LABEL, bpelProcess.getTargetNamespace());
 		
 		// Add import to associated wsdl
 		String wsdlName=getWSDLFileName(role, localcm.getProtocol().getName(), "");
 
 		org.w3c.dom.Element imp=doc.createElement(WSDL_IMPORT);
 		
 		imp.setAttribute("namespace", bpelProcess.getTargetNamespace());
 		imp.setAttribute("location", wsdlName);
 		
 		defn.appendChild(imp);					
 
 		// Add imports for associated roles
 		java.util.ListIterator<Role> roles=localcm.getProtocol().getRoles().listIterator();
 		
 		while (roles.hasNext()) {
 			Role r=roles.next();
 			
 			ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
 			Contract contract=null;
 			
 			if (cg != null) {
 				contract=cg.generate(model.getProtocol(), null, r, journal);
 			}
 			
 			if (contract != null) {
 				boolean gen=false;
 				
 				java.util.Iterator<Interface> iter=contract.getInterfaces().iterator();
 				
 				while (gen == false && iter.hasNext()) {
 					Interface intf=iter.next();
 					
 					if (intf.getMessageExchangePatterns().size() > 0) {
 						gen = true;
 					}
 				}
 				
 				if (gen) {
 					wsdlName = getWSDLFileName(r, localcm.getProtocol().getName(), "");
 		
 					imp = doc.createElement(WSDL_IMPORT);
 					
 					imp.setAttribute("namespace", contract.getNamespace());
 					imp.setAttribute("location", wsdlName);
 					
 					defn.appendChild(imp);
 				}
 			}
 		}
 
 		// Create partner link types
 		java.util.Map<String, String> nsMap=new java.util.HashMap<String, String>();
 		
 		for (TPartnerLink pl : bpelProcess.getPartnerLinks().getPartnerLink()) {
 			org.w3c.dom.Element plt=doc.createElement(PLNK_PARTNER_LINK_TYPE);
 			
 			plt.setAttribute(NAME_LABEL, pl.getPartnerLinkType().getLocalPart());
 			
 			if (pl.getPartnerRole() != null && pl.getPartnerRole().trim().length() > 0) {
 				org.w3c.dom.Element plRole=doc.createElement(PLNK_ROLE);
 				
 				plt.appendChild(plRole);
 				
 				plRole.setAttribute(NAME_LABEL, pl.getPartnerRole());
 				
 				Role useRole=null;
 				
 				for (int i=0; useRole == null &&
 							i < localcm.getProtocol().getRoles().size(); i++) {
 					if (pl.getPartnerRole().startsWith(localcm.getProtocol().getRoles().get(i).getName())) {
 						useRole = localcm.getProtocol().getRoles().get(i);
 					}
 				}
 				
 				ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
 				Contract contract=null;
 				
 				if (cg != null && useRole != null) {
 					contract=cg.generate(model.getProtocol(), null, useRole, journal);
 				}
 				
 				if (contract != null) {
 					Interface intf=null;
 					
 					if (pl.getMyRole() != null) {
 						intf = contract.getInterface(pl.getMyRole());
 					}
 						
 					if (intf == null && contract.getInterfaces().size() > 0) {
 						intf = contract.getInterfaces().iterator().next();
 					}
 					
 					if (intf != null) {
 						String prefix=null;
 						String portType=intf.getName();
 						
 						if (intf.getNamespace() != null) {
 							prefix = XMLUtils.getPrefixForNamespace(intf.getNamespace(), nsMap);
 							
 							portType = prefix+":"+portType;
 						}
 						
 						plRole.setAttribute(PORT_TYPE_LABEL, portType);
 					}
 				}
 			}
 			
 			if (pl.getMyRole() != null && pl.getMyRole().trim().length() > 0) {
 				org.w3c.dom.Element plRole=doc.createElement(PLNK_ROLE);
 				
 				plt.appendChild(plRole);
 				
 				plRole.setAttribute(NAME_LABEL, pl.getMyRole());
 				
 				ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
 				Contract contract=null;
 				
 				if (cg != null) {
 					contract=cg.generate(model.getProtocol(), null, role, journal);
 				}
 				
 				if (contract != null) {
 					Interface intf=null;
 					
 					if (pl.getMyRole() != null) {
 						intf = contract.getInterface(pl.getMyRole());
 					}
 						
 					if (intf == null && contract.getInterfaces().size() > 0) {
 						intf = contract.getInterfaces().iterator().next();
 					}
 					
 					if (intf != null) {
 						String prefix=null;
 						String portType=intf.getName();
 						
 						if (intf.getNamespace() != null) {
 							prefix = XMLUtils.getPrefixForNamespace(intf.getNamespace(), nsMap);
 							
 							portType = prefix+":"+portType;
 						}
 						
 						plRole.setAttribute(PORT_TYPE_LABEL, portType);
 					}
 				}
 			}
 			
 			defn.appendChild(plt);
 		}
 		
 		// Create remaining namespace/prefix mappings
 		java.util.Iterator<String> iter=nsMap.keySet().iterator();
 		while (iter.hasNext()) {
 			String ns=iter.next();
 			String prefix=nsMap.get(ns);
 			
 			defn.setAttribute(XMLNS_PREFIX+prefix, ns);
 		}
 		
 		// Write partner link types to file
 		String filename=getWSDLFileName(role, localcm.getProtocol().getName(), "Artifacts");
 		
 		IPath wsdlPath=proj.getFullPath().append(
 				new Path(BPEL_PATH)).
 					append(filename);
 		
 		IFile wsdlFile=proj.getProject().getWorkspace().getRoot().getFile(wsdlPath);
 		
 		createFolder(wsdlFile);
 		
 		wsdlFile.create(null, true,
 				new org.eclipse.core.runtime.NullProgressMonitor());
 		
 		java.io.ByteArrayOutputStream xmlstr=
 			new java.io.ByteArrayOutputStream();
 		
 		DOMSource source=new DOMSource();
 		source.setNode(doc);
 		
 		StreamResult result=new StreamResult(xmlstr);
 		
 		Transformer trans=
 				TransformerFactory.newInstance().newTransformer();
 		trans.transform(source, result);
 		
 		xmlstr.close();
 		
 		String xml=XMLUtils.format(new String(xmlstr.toByteArray()));
 		
 		wsdlFile.setContents(new java.io.ByteArrayInputStream(xml.getBytes()), true, false,
 					new org.eclipse.core.runtime.NullProgressMonitor());
 	}
 	
 	
 	protected void generateBPELDeploy(ProtocolModel model, Role role, IProject proj, ProtocolModel localcm,
 							TProcess bpelProcess, FeedbackHandler journal) throws Exception {	
 		
 		org.w3c.dom.Document doc=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();;
 		
 		org.w3c.dom.Element defn=doc.createElementNS(APACHE_ODE_NAMESPACE,
 													DEPLOY_LABEL);
 		doc.appendChild(defn);
 		
 		java.util.Map<String, String> nsMap=new java.util.HashMap<String, String>();
 		
 		// Create process element
 		org.w3c.dom.Element proc=doc.createElement(PROCESS_LABEL);
 		defn.appendChild(proc);
 		
 		String name=bpelProcess.getName();
 		
 		if (bpelProcess.getTargetNamespace() != null) {
 			String prefix=XMLUtils.getPrefixForNamespace(bpelProcess.getTargetNamespace(), nsMap);
 			
 			name = prefix+":"+name;
 		}
 		
 		proc.setAttribute(NAME_LABEL, name);
 		
 		org.w3c.dom.Element active=doc.createElement(ACTIVE_LABEL);
 		proc.appendChild(active);
 		
 		org.w3c.dom.Text activeText=doc.createTextNode(Boolean.TRUE.toString());
 		active.appendChild(activeText);
 		
 		org.w3c.dom.Element processEvents=doc.createElement(PROCESS_EVENTS_LABEL);
 		processEvents.setAttribute("generate", "all");
 		proc.appendChild(processEvents);
 		
    	
 		// TODO: Need more info - possibly Contract should have interfaces based on
 		// the relationship between two roles, as this may provide the necessary
 		// information - but this requires the ability to have multiple interfaces
 		// per role, which some other parts of the BPEL generation would currently
 		// not handle.
 		
 		// Work through partner links for now
 		for (TPartnerLink pl : bpelProcess.getPartnerLinks().getPartnerLink()) {
 			if (pl.getPartnerRole() != null && pl.getPartnerRole().trim().length() > 0) {
 				org.w3c.dom.Element invoke=doc.createElement(INVOKE_LABEL);
 				
 				invoke.setAttribute(PARTNER_LINK_LABEL, XMLUtils.getLocalname(pl.getName()));
 				
 				org.w3c.dom.Element service=doc.createElement(SERVICE_LABEL);
 				
 				invoke.appendChild(service);
 				
 				proc.appendChild(invoke);
 			}
 			
 			if (pl.getMyRole() != null && pl.getMyRole().trim().length() > 0) {
 				org.w3c.dom.Element provide=doc.createElement(PROVIDE_LABEL);
 				
 				provide.setAttribute(PARTNER_LINK_LABEL, XMLUtils.getLocalname(pl.getName()));
 				
 				org.w3c.dom.Element service=doc.createElement(SERVICE_LABEL);
 				
 				provide.appendChild(service);
 				
 				proc.appendChild(provide);
 			}
 		}
 		
 		// Create remaining namespace/prefix mappings
 		java.util.Iterator<String> iter=nsMap.keySet().iterator();
 		while (iter.hasNext()) {
 			String ns=iter.next();
 			String prefix=nsMap.get(ns);
 			
 			defn.setAttribute(XMLNS_PREFIX+prefix, ns);
 		}
 		
 		// Write partner link types to file
 		IPath wsdlPath=proj.getFullPath().append(
 				new Path(BPEL_PATH)).
 					append(BPEL_DEPLOY_DESCRIPTOR_FILENAME);
 		
 		IFile wsdlFile=proj.getProject().getWorkspace().getRoot().getFile(wsdlPath);
 		
 		createFolder(wsdlFile);
 		
 		wsdlFile.create(null, true,
 				new org.eclipse.core.runtime.NullProgressMonitor());
 		
 		java.io.ByteArrayOutputStream xmlstr=
 			new java.io.ByteArrayOutputStream();
 		
 		DOMSource source=new DOMSource();
 		source.setNode(doc);
 		
 		StreamResult result=new StreamResult(xmlstr);
 		
 		Transformer trans=
 				TransformerFactory.newInstance().newTransformer();
 		trans.transform(source, result);
 		
 		xmlstr.close();
 		
 		String xml=XMLUtils.format(new String(xmlstr.toByteArray()));
 		
 		wsdlFile.setContents(new java.io.ByteArrayInputStream(xml.getBytes()), true, false,
 					new org.eclipse.core.runtime.NullProgressMonitor());
 	}
 	
 	/**
 	 * This method returns the WSDL file name for the supplied role and local
 	 * conversation model.
 	 * 
 	 * @param role The role
 	 * @param localcm The local conversation model
 	 * @param fileNum The file name (zero being the main wsdl file)
 	 * @return The file name
 	 */
 	public static String getWSDLFileName(Role role, String modelName, String suffix) {
 		return(modelName+"_"+role.getName()+suffix+".wsdl");
 	}
 			
 	protected IProject createProject(IResource resource, String projectName, FeedbackHandler journal)
 								throws Exception {
 		// Create project
 		IProject project = resource.getWorkspace().getRoot().getProject(projectName);
 		project.create(new org.eclipse.core.runtime.NullProgressMonitor());
 		
 		// Open the project
 		project.open(new org.eclipse.core.runtime.NullProgressMonitor());
 		
 		// Add wtp natures
 		WtpUtils.addNatures(project);
 		
 		// Add required project facets	
 		IProjectFacet bpelFacet =
 					ProjectFacetsManager.getProjectFacet("jbt.bpel.facet.core");
 		IProjectFacetVersion ipfv = bpelFacet.getVersion("2.0");
 		IFacetedProject ifp = ProjectFacetsManager.create(project, true, null);
 		ifp.installProjectFacet(ipfv, null,
 					new org.eclipse.core.runtime.NullProgressMonitor());
 
 		// Update the project description
 		IProjectDescription description = project.getDescription();
 		
 		// Setup project reference to CDM project
 		IProject[] projects=new IProject[1];
 		projects[0] = resource.getProject();
 		description.setReferencedProjects(projects);
 		
 		// Set the description
 		project.setDescription(description,
 				new org.eclipse.core.runtime.NullProgressMonitor());
 		
 		return(project);
 	}
 	
 	/**
 	 * This method checks whether the folder exists,
 	 * and if not attempts to create it.
 	 * 
 	 * @param res The current resource
 	 */
 	public static void createFolder(IResource res) {
 		if (res instanceof IFolder) {
 			IFolder folder=(IFolder)res;
 			
 			if (folder.exists() == false) {
 				createFolder(folder.getParent());
 
 				try {
 					folder.create(true, true,
 							new org.eclipse.core.runtime.NullProgressMonitor());
 				} catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 		} else if (res.getParent() != null) {
 			createFolder(res.getParent());
 		}
 	}
 }
