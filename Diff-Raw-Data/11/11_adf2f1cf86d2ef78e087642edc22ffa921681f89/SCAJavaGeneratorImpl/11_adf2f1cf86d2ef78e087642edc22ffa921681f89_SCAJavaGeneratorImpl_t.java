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
 package org.savara.tools.sca.java.generator;
 
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
 import org.osgi.framework.Bundle;
 import org.savara.common.logging.FeedbackHandler;
 import org.savara.common.logging.MessageFormatter;
 import org.savara.common.util.XMLUtils;
 import org.savara.contract.model.Contract;
 import org.savara.contract.model.Interface;
 import org.savara.contract.model.Namespace;
 import org.savara.protocol.contract.generator.ContractGenerator;
 import org.savara.protocol.contract.generator.ContractGeneratorFactory;
 import org.savara.sca.java.generator.SCAJavaGenerator;
 import org.savara.tools.common.ArtifactType;
 import org.savara.tools.common.generation.AbstractGenerator;
 import org.savara.tools.common.generation.GeneratorUtil;
 import org.savara.wsdl.generator.WSDLGeneratorFactory;
 import org.savara.wsdl.generator.soap.SOAPDocLitWSDLBinding;
 import org.scribble.protocol.model.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 
 /**
  * This class provides the mechanism for generating SCA Java
  * service artefacts.
  */
 public class SCAJavaGeneratorImpl extends AbstractGenerator {
 
 	private static final String GENERATOR_NAME = "SCA Java";
 	private static final String SCHEMA_LOCATION_ATTR = "schemaLocation";
 	private static final String INCLUDE_ELEMENT = "include";
 	private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
 	private static final String JAVA_PATH = "src/main/java";
 	private static final String WSDL_FOLDER = "wsdl";
	private static final String RESOURCE_PATH = "src/main/resources/";
	private static final String WSDL_PATH = RESOURCE_PATH+WSDL_FOLDER;
 
 	private static Logger logger = Logger.getLogger(SCAJavaGeneratorImpl.class.getName());
 
 	/**
 	 * This is the constructor for the generator.
 	 * 
 	 */
 	public SCAJavaGeneratorImpl() {
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
 			try {
 				final IProject proj=createProject(modelResource, projectName, handler);
 				
 				SCAJavaGenerator gen=new SCAJavaGenerator();
 				
 				// Write the WSDL files
 				IFile wsdlFile=generateWSDL(model, role, proj, local, modelResource, handler);
 				
 				if (wsdlFile != null) {
 					String wsdlLocation=WSDL_FOLDER+"/"+wsdlFile.getName();
 					
 					logger.info("Generate Java service interface from wsdl '"+wsdlFile.getLocation().toOSString()+
 							"' to source folder '"+proj.getFolder(JAVA_PATH).getLocation().toOSString()+"'");
 					
 					gen.createServiceInterfaceFromWSDL(wsdlFile.getLocation().toOSString(),
 							wsdlLocation, proj.getFolder(JAVA_PATH).getLocation().toOSString());
 					
 					logger.info("Generate Java service implementation from wsdl '"+wsdlFile.getLocation().toOSString()+
 							"' to source folder '"+proj.getFolder(JAVA_PATH).getLocation().toOSString()+"'");
 					
 					gen.createServiceImplementationFromWSDL(wsdlFile.getLocation().toOSString(),
 							wsdlLocation, proj.getFolder(JAVA_PATH).getLocation().toOSString());

					// Generate composite for role
					gen.createServiceComposite(role, wsdlFile.getLocation().toOSString(),
							proj.getFolder(RESOURCE_PATH).getLocation().toOSString());
 				}
 				
 				java.util.List<Role> roles=local.getProtocol().getRoles();
 				
 				for (int i=0; i < roles.size(); i++) {
 					wsdlFile = generateWSDL(model, roles.get(i), proj, local, modelResource, handler);
 					
 					if (wsdlFile != null) {
 						String wsdlLocation=WSDL_FOLDER+"/"+wsdlFile.getName();
 						
 						logger.info("Generate referenced Java service interface from wsdl '"+wsdlFile.getLocation().toOSString()+
 								"' to source folder '"+proj.getFolder(JAVA_PATH).getLocation().toOSString()+"'");
 						
 						gen.createServiceInterfaceFromWSDL(wsdlFile.getLocation().toOSString(),
 								wsdlLocation, proj.getFolder(JAVA_PATH).getLocation().toOSString());
 					}
 				}
 				
				proj.refreshLocal(IResource.DEPTH_INFINITE,
 									new NullProgressMonitor());
 			} catch(Exception e) {
 				org.savara.tools.sca.java.osgi.Activator.logError("Failed to create SCA Java project '"+
 										projectName+"'", e);
 				
 				handler.error(MessageFormatter.format(java.util.PropertyResourceBundle.getBundle(
 						"org.savara.tools.sca.java.Messages"), "SAVARA-SCAJAVATOOLS-00001",
 									projectName), null);
 			}
 		}
 	}
 	
 	protected IFile generateWSDL(ProtocolModel model, Role role, IProject proj, ProtocolModel localcm,
 						IResource resource, FeedbackHandler journal) throws Exception {		
 		IFile ret=null;
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
 
 			// Generate WSDL folder
 			IPath wsdlFolderPath=proj.getFullPath().append(
 					new Path(WSDL_PATH));
 
 			IFolder wsdlFolder=proj.getProject().getWorkspace().getRoot().getFolder(wsdlFolderPath);
 			
 			GeneratorUtil.createFolder(wsdlFolder);
 			
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
 						
 						importSchemas(resource, contract, wsdlFolderPath,
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
 					
 					IPath wsdlPath=wsdlFolderPath.append(filename);
 					
 					IFile wsdlFile=proj.getProject().getWorkspace().getRoot().getFile(wsdlPath);
 					
 					if (wsdlFile.getParent() instanceof IFolder) {
 						GeneratorUtil.createFolder((IFolder)wsdlFile.getParent());
 					}
 					
 					if (wsdlFile.exists() == false) {
 						wsdlFile.create(null, true,
 								new org.eclipse.core.runtime.NullProgressMonitor());
 					}
 					
 					wsdlFile.setContents(new java.io.ByteArrayInputStream(b), true, false,
 								new org.eclipse.core.runtime.NullProgressMonitor());
 					
 					ret = wsdlFile;
 				}
 			}
 		}
 		
 		return(ret);
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
 			if (targetXSDFile.getParent() instanceof IFolder) {
 				GeneratorUtil.createFolder((IFolder)targetXSDFile.getParent());
 			}
 
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
 		//WtpUtils.addNatures(project);
 		
 		// Update the project description
 		IProjectDescription description = project.getDescription();
 		
 		// Setup project reference to CDM project
 		IProject[] projects=new IProject[1];
 		projects[0] = resource.getProject();
 		description.setReferencedProjects(projects);
 		
 		// Set the description
 		project.setDescription(description,
 				new org.eclipse.core.runtime.NullProgressMonitor());
 				
 		IJavaProject jproject=JavaCore.create(project);
 		
 		// Create the java project
 		createJavaProject(jproject);
 		
 		return(project);
 	}
 	
 	protected void createJavaProject(IJavaProject project) {
 
 		try {
 			IProjectDescription description = project.getProject().getDescription();
 			String[] prevNatures= description.getNatureIds();
 			String[] newNatures= new String[prevNatures.length + 1];
 			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
 			newNatures[prevNatures.length]= JavaCore.NATURE_ID;
 			description.setNatureIds(newNatures);
 
 			project.getProject().setDescription(description,
 						new org.eclipse.core.runtime.NullProgressMonitor());
 			
 			IClasspathEntry[] classpaths=new IClasspathEntry[4];
 
 			classpaths[0] = JavaCore.newContainerEntry(
 					new Path("org.eclipse.jdt.launching.JRE_CONTAINER"));
 			
 			IFolder srcFolder=project.getProject().getFolder("src/main/java");
 			GeneratorUtil.createFolder(srcFolder);
 
 			classpaths[1] = JavaCore.newSourceEntry(srcFolder.getFullPath());
 
 			IFolder resFolder=project.getProject().getFolder("src/main/resources");
 			GeneratorUtil.createFolder(resFolder);
 
 			classpaths[2] = JavaCore.newSourceEntry(resFolder.getFullPath());
 
 			// Install SCA API library
 			Bundle bundle=
 					org.eclipse.core.runtime.Platform.getBundle("org.savara.sca.java");
 				
 			java.net.URL url=bundle.getEntry("/lib/tuscany-sca-api.jar");
 			
 			if (url != null) {
 				url = org.eclipse.core.runtime.FileLocator.toFileURL(url);
 			} else {
 				url = bundle.getResource("lib/tuscany-sca-api.jar");
 			}
 			
 			java.io.InputStream is=url.openStream();
 			
 			byte[] b=new byte[is.available()];
 			is.read(b);
 						
 			is.close();
 			
 			IFolder libFolder=project.getProject().getFolder("lib");
 			GeneratorUtil.createFolder(libFolder);
 			
 			IFile scaapijar=libFolder.getFile("sca-api.jar");
 			
 			is = new java.io.ByteArrayInputStream(b);
 			
 			scaapijar.create(is, true, null);
 			
 			is.close();
 			
 
 			classpaths[3] = JavaCore.newLibraryEntry(scaapijar.getFullPath(), null, null);
 
 			// Set classpath
 			project.setRawClasspath(classpaths,
 					new org.eclipse.core.runtime.NullProgressMonitor());
 			
 		} catch(Exception e) {
 			logger.severe("Failed to create Java project: "+e);
 		}
 	} 
 }
