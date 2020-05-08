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
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.commons.logging.*;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.savara.bpel.generator.ProtocolToBPELModelGenerator;
 import org.savara.bpel.model.*;
 import org.savara.bpel.util.BPELModelUtil;
 import org.savara.common.model.annotation.Annotation;
 import org.savara.common.model.annotation.AnnotationDefinitions;
 import org.savara.common.util.XMLUtils;
 import org.savara.contract.model.Contract;
 import org.savara.contract.model.Interface;
 import org.savara.contract.model.Namespace;
 import org.savara.protocol.contract.generator.ContractGenerator;
 import org.savara.protocol.contract.generator.ContractGeneratorFactory;
 import org.savara.protocol.util.ProtocolServices;
 import org.savara.wsdl.generator.WSDLGeneratorFactory;
 import org.savara.wsdl.generator.soap.SOAPDocLitWSDLBinding;
 import org.scribble.common.logging.CachedJournal;
 import org.scribble.common.logging.Journal;
 import org.scribble.common.resource.DefaultResourceLocator;
 import org.scribble.protocol.DefaultProtocolContext;
 import org.scribble.protocol.model.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jst.common.project.facet.WtpUtils;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 /**
  * This class provides the mechanism for generating JBossESB
  * service artefacts.
  */
 public class Generator {
 
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
 
 	private static Log logger = LogFactory.getLog(Generator.class);
 
 	private IFile m_file=null;
 	private ProtocolModel m_protocolModel=null;
 
 	/**
 	 * This is the constructor for the generator.
 	 * 
 	 */
 	public Generator(IFile file) {
 		m_file = file;
 		
 		initialize(m_file);
 	}
 	
 	/**
 	 * This method initializes the conversation model associated
 	 * with the supplied file resource.
 	 * 
 	 * @param res The file
 	 */
 	protected void initialize(IFile res) {
 		
 		// Initialize the model
 		/*
 		ModelReference ref=
 			org.scribble.eclipse.model.admin.OSGIModelRepository.createReference(res);
 		
 		if (ref != null) {
 			ModelRepository mrep=(ModelRepository)
 					RegistryFactory.getRegistry().getExtension(
 						ModelRepository.class, null);
 
 			if (mrep != null) {
 				Model model=mrep.getModel(ref,
 						new DefaultModelListener());
 		
 				if (model instanceof ProtocolModel) {
 					m_protocolModel = (ProtocolModel)model;
 						
 					m_localModelRefs = m_protocolModel.getLocalModels(ref);
 				}
 			}
 		}
 		*/
 		
 		// Obtain protocol model
 		// TODO: Need to configure Eclipse based journal for reporting errors?
 		Journal journal=new CachedJournal();
 		
 		try {
 			m_protocolModel = ProtocolServices.getParserManager().parse(res.getFileExtension(),
 								res.getContents(), journal, null);
 			
 			if (m_protocolModel == null) {
 				logger.error("Unable to load model used to generate the BPEL process");
 			}
 		} catch(Exception e) {
 			logger.error("Failed to generate BPEL process", e);
 		}
 	}
 	
 	/**
 	 * This method generates the project associated with the
 	 * supplied role (and optional sub-definition path) contained
 	 * in the local model reference.
 	 * 
 	 * @param localRole The local model reference
 	 * @param projectName The project name
 	 * @param cdmResource The original choreography file
 	 * @throws Exception Failed to generate role
 	 */
 	public void generateRole(Role localRole, String projectName, IFile cdmResource)
 						throws Exception {
 		
 		if (logger.isDebugEnabled()) {
 			logger.debug("Generate local model '"+localRole+"' for: "+
 						m_protocolModel);
 		}
 		
 		/*
 		// Project to role
 		org.scribble.projector.ProtocolProjector projector=
 			(org.scribble.projector.Projector)
 			RegistryFactory.getRegistry().getExtension(
 					org.scribble.projector.Projector.class, null);
 
 		if (projector != null) {
 			Role role=new Role(localRole.getLocatedRole());
 			
 			// Transfer contract details (if defined) from local model
 			// reference to role
 			if (localRole.getProperties().containsKey(org.savara.contract.model.Contract.class.getName())) {
 				role.getProperties().put(org.savara.contract.model.Contract.class.getName(),
 								localRole.getProperties().get(org.savara.contract.model.Contract.class.getName()));
 			}
 			
 			ModelReference srcRef=
 				new ModelReference(ConversationNotation.NOTATION_CODE);
 			
 			Model localModel=projector.project(srcRef, m_protocolModel,
 					localRole.getSubDefinitionPath(), role,
 					new DefaultModelListener());
 			
 			if (localModel instanceof ProtocolModel &&
 					((ProtocolModel)localModel).getProtocol() != null) {
 				ProtocolModel lcm=(ProtocolModel)localModel;
 				
 				ModelReference targetRef=
 					new ModelReference(BPELNotation.NOTATION_CODE);
 				
 				DefaultBPELLanguageModel target=
 					new DefaultBPELLanguageModel(targetRef);
 				
 				ModelGenerator generator=(ModelGenerator)
 					RegistryFactory.getRegistry().getExtension(
 							ModelGenerator.class, null);
 				
 				if (generator != null) {
 					generator.generate(targetRef, role, target,
 								lcm);
 					
 					generateRoleProject(projectName, role,
 								target.getBPELProcess(), lcm, target, cdmResource);
 				} else {
 					logger.error("Unable to find model generator");
 				}
 			}
 		}
 		*/
 		
 		CachedJournal journal=new CachedJournal();
 		
 		// TODO: SAVARA-169 (and related to SAVARA-168)
 		// Currently necessary to generate contracts from global model and
 		// associate with model
 		/* GPB: CONTRACT
 		ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
 		if (cg != null) {
 			cg.generate(m_protocolModel.getProtocol(), journal, null);
 		}
 		*/
 		
 		DefaultProtocolContext context=new DefaultProtocolContext(ProtocolServices.getParserManager(),
 						new DefaultResourceLocator(cdmResource.getParent().getFullPath().toFile()));
 		
 		ProtocolModel local=ProtocolServices.getProtocolProjector().project(m_protocolModel,
 								localRole, journal, context);
 		
 		if (local != null) {
 			// TODO: SAVARA-167 - issue when projection is based on a sub-protocol
 			if (AnnotationDefinitions.getAnnotation(local.getProtocol().getAnnotations(),
 							AnnotationDefinitions.TYPE) == null &&
 					AnnotationDefinitions.getAnnotation(m_protocolModel.getProtocol().getAnnotations(),
 									AnnotationDefinitions.TYPE) != null) {				
 				AnnotationDefinitions.copyAnnotations(m_protocolModel.getProtocol().getAnnotations(),
 						local.getProtocol().getAnnotations(), AnnotationDefinitions.TYPE);
 			}
 			
 			// TODO: Obtain model generator from manager class (SAVARA-156)
 			ProtocolToBPELModelGenerator generator=new ProtocolToBPELModelGenerator();
 			
 			Object target=generator.generate(local, journal, null);
 			
 			if (target instanceof TProcess) {
 				generateRoleProject(projectName, localRole, (TProcess)target, local, cdmResource,
 								journal);
 			}
 		}
 	}
 	
 	protected void generateRoleProject(String projectName, Role role,
 			TProcess bpelProcess, ProtocolModel localcm,
 					IFile cdmResource, Journal journal) throws Exception {
 		
 		final IProject proj=createProject(projectName);
 		
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
 			generateWSDL(role, proj, localcm, cdmResource, journal);		
 			
 			java.util.List<Role> roles=localcm.getProtocol().getRoles();
 			
 			for (int i=0; i < roles.size(); i++) {
 				generateWSDL(roles.get(i), proj, localcm, cdmResource, journal);
 			}
 			
 			// Generate WSDL with partner link types
 			generatePartnerLinkTypes(role, proj, localcm, bpelProcess);
 			
 			// Generate BPEL deployment descriptor
 			generateBPELDeploy(role, proj, localcm, bpelProcess);
 		}
 	}
 	
 	protected void generateWSDL(Role role, IProject proj, ProtocolModel localcm,
 						IFile cdmResource, Journal journal) throws Exception {		
 		
 		ContractGenerator cg=ContractGeneratorFactory.getContractGenerator();
 		Contract contract=null;
 		
 		if (cg != null) {
 			contract=cg.generate(m_protocolModel.getProtocol(), null, role, journal);
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
									new SOAPDocLitWSDLBinding());
 			
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
 						
 						importSchemas(cdmResource, contract, bpelFolderPath,
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
 	
 	protected void importSchemas(IFile cdmResource, Contract contract,
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
 						IFile file=cdmResource.getParent().getFile(new Path(location));
 					
 						org.w3c.dom.Element imp=doc.createElementNS("http://www.w3.org/2001/XMLSchema",
 										"import");
 						
 						imp.setAttribute("namespace", ns.getURI());
 						
 						if (file.exists()) {
 							imp.setAttribute("schemaLocation", file.getProjectRelativePath().toPortableString());
 
 							// Copy schema file into generated BPEL project
 							IPath artifactPath=bpelFolderPath.append(file.getProjectRelativePath());
 							
 							IFile artifactFile=cdmResource.getProject().getWorkspace().getRoot().getFile(artifactPath);
 
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
 	
 	protected void generatePartnerLinkTypes(Role role, IProject proj, ProtocolModel localcm,
 			TProcess bpelProcess) throws Exception {	
 		
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
 				CachedJournal journal=new CachedJournal();
 				contract=cg.generate(m_protocolModel.getProtocol(), null, r, journal);
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
 					CachedJournal journal=new CachedJournal();
 					contract=cg.generate(m_protocolModel.getProtocol(), null, useRole, journal);
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
 					CachedJournal journal=new CachedJournal();
 					contract=cg.generate(m_protocolModel.getProtocol(), null, role, journal);
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
 	
 	
 	protected void generateBPELDeploy(Role role, IProject proj, ProtocolModel localcm,
 							TProcess bpelProcess) throws Exception {	
 		
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
 			
 	protected IProject createProject(String projectName)
 							throws Exception {
 		
 		// Create project
 		IProject project=m_file.getWorkspace().getRoot().getProject(projectName);
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
 		projects[0] = m_file.getProject();
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
