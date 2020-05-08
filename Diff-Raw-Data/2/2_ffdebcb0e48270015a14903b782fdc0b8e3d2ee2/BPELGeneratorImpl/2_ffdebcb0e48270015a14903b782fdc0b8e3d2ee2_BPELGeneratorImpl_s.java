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
 import org.eclipse.core.resources.IResource;
 import org.savara.bpel.generator.ProtocolToBPELModelGenerator;
 import org.savara.bpel.model.*;
 import org.savara.bpel.util.BPELGeneratorUtil;
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
 import org.savara.tools.bpel.osgi.Activator;
 import org.savara.tools.common.ArtifactType;
 import org.savara.tools.common.generation.AbstractGenerator;
 import org.savara.wsdl.generator.WSDLGeneratorFactory;
 import org.savara.wsdl.generator.soap.SOAPDocLitWSDLBinding;
 import org.savara.wsdl.util.WSDLGeneratorUtil;
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
 	private static final String SCHEMA_LOCATION_ATTR = "schemaLocation";
 	private static final String INCLUDE_ELEMENT = "include";
 	private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
 	private static final String BPEL_DEPLOY_DESCRIPTOR_FILENAME = "deploy.xml";
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
 		java.util.List<javax.wsdl.Definition> wsdls=
 				new java.util.Vector<javax.wsdl.Definition>();
 		
 		final IProject proj=createProject(resource, projectName, journal);
 		
 		if (proj != null && bpelProcess != null) {
 
 			// Store BPEL configuration
 			IPath bpelPath=proj.getFullPath().append(
 					new Path(BPEL_PATH)).
 						append(localcm.getProtocol().getName()+"_"+
 							localcm.getProtocol().getLocatedRole().getName()+".bpel");
 			
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
 			generateWSDL(model, role, proj, localcm, resource, journal, wsdls);		
 			
 			java.util.List<Role> roles=localcm.getProtocol().getRoles();
 			
 			for (int i=0; i < roles.size(); i++) {
 				generateWSDL(model, roles.get(i), proj, localcm, resource, journal, wsdls);
 			}
 			
 			// Generate WSDL with partner link types
 			org.w3c.dom.Document pty=generatePartnerLinkTypes(model, role, proj, localcm,
 								bpelProcess, wsdls, journal);
 			
 			// Generate BPEL deployment descriptor
 			generateBPELDeploy(model, role, proj, localcm, bpelProcess, wsdls,
 							pty.getDocumentElement(), journal);
 		}
 	}
 	
 	protected void generateWSDL(ProtocolModel model, Role role, IProject proj, ProtocolModel localcm,
 						IResource resource, FeedbackHandler journal,
 						java.util.List<javax.wsdl.Definition> wsdls) throws Exception {		
 		
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
 
 					// Add definition to returned wsdl list
 					wsdls.add(defn);
 
 					String filename=WSDLGeneratorUtil.getWSDLFileName(role, localcm.getProtocol().getName(), num);
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
 	
 	protected org.w3c.dom.Document generatePartnerLinkTypes(ProtocolModel model, Role role, IProject proj, ProtocolModel localcm,
 			TProcess bpelProcess, java.util.List<javax.wsdl.Definition> wsdls,
 			FeedbackHandler journal) throws Exception {	
 		
 		org.w3c.dom.Document doc=BPELGeneratorUtil.generatePartnerLinkTypes(model,
 							role, localcm, bpelProcess, journal);
 		
 		// Write partner link types to file
 		String filename=WSDLGeneratorUtil.getWSDLFileName(role, localcm.getProtocol().getName(), "Artifacts");
 		
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
 		
 		return (doc);
 	}	
 	
 	protected void generateBPELDeploy(ProtocolModel model, Role role, IProject proj, ProtocolModel localcm,
 							TProcess bpelProcess, java.util.List<javax.wsdl.Definition> wsdls,
 							org.w3c.dom.Element partnerLinkTypes,
 							FeedbackHandler journal) throws Exception {	
 		
 		org.w3c.dom.Document doc=BPELGeneratorUtil.generateDeploymentDescriptor(model, role, localcm,
 				bpelProcess, wsdls, partnerLinkTypes, journal);
 
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
 		try {
 			IProjectFacet bpelFacet =
						ProjectFacetsManager.getProjectFacet("jbt.bpel.facet.core");
 			IProjectFacetVersion ipfv = bpelFacet.getVersion("2.0");
 			IFacetedProject ifp = ProjectFacetsManager.create(project, true, null);
 			ifp.installProjectFacet(ipfv, null,
 						new org.eclipse.core.runtime.NullProgressMonitor());
 		} catch(Exception e) {
 			Activator.logError("Failed to add BPEL facet to project", e);
 		}
 
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
