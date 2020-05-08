 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.commonarchivecore.internal.impl;
 
 
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.application.ApplicationFactory;
 import org.eclipse.jst.j2ee.application.ApplicationPackage;
 import org.eclipse.jst.j2ee.application.ConnectorModule;
 import org.eclipse.jst.j2ee.application.EjbModule;
 import org.eclipse.jst.j2ee.application.JavaClientModule;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.application.WebModule;
 import org.eclipse.jst.j2ee.client.ApplicationClient;
 import org.eclipse.jst.j2ee.client.internal.impl.ApplicationClientResourceFactory;
 import org.eclipse.jst.j2ee.common.EjbRef;
 import org.eclipse.jst.j2ee.common.SecurityRole;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonArchiveResourceHandler;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonarchivePackage;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Container;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EJBJarFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.File;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.ModuleFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.ModuleRef;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.ArchiveWrappedException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.DeploymentDescriptorLoadException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.DuplicateObjectException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.EmptyResourceException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.ObjectNotFoundException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.ResourceLoadException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveOptions;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.LoadStrategy;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.commonarchivecore.looseconfig.internal.LooseArchive;
 import org.eclipse.jst.j2ee.ejb.AssemblyDescriptor;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.ejb.EJBResource;
 import org.eclipse.jst.j2ee.ejb.EjbPackage;
 import org.eclipse.jst.j2ee.ejb.EnterpriseBean;
 import org.eclipse.jst.j2ee.ejb.internal.impl.EJBJarResourceFactory;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.jca.Connector;
 import org.eclipse.jst.j2ee.jca.internal.impl.ConnectorResourceFactory;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webapplication.internal.impl.WebAppResourceFactory;
 import org.eclipse.wst.common.internal.emf.resource.FileNameResourceFactoryRegistry;
 import org.eclipse.wst.common.internal.emf.utilities.EtoolsCopyUtility;
 
 
 /**
  * @generated
  */
 public class EARFileImpl extends ModuleFileImpl implements EARFile {
 
 	/**
 	 * Internal; clients should use {@link #getModuleRef(Module)}
 	 */
 	public ModuleFile getModuleFile(Module moduleDescriptor) {
 		ModuleRef ref = getModuleRef(moduleDescriptor);
 		return (ref == null) ? null : ref.getModuleFile();
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public ModuleFile addCopy(ModuleFile aModuleFile) throws DuplicateObjectException {
 		Object result = primAddCopyRef(aModuleFile);
 		if (result instanceof ModuleRef)
 			return ((ModuleRef) result).getModuleFile();
 
 		return (ModuleFile) result;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected Application deploymentDescriptor = null;
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected EList moduleRefs = null;
 
 	public EARFileImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return CommonarchivePackage.eINSTANCE.getEARFile();
 	}
 
 	public Archive addCopy(Archive anArchive) throws org.eclipse.jst.j2ee.commonarchivecore.internal.exception.DuplicateObjectException {
 		if (anArchive.isModuleFile())
 			return addCopy((ModuleFile) anArchive);
 		Archive copy = super.addCopy(anArchive);
 		copy.initializeClassLoader();
 		return copy;
 	}
 
 	protected Object primAddCopyRef(ModuleFile aModuleFile) throws DuplicateObjectException {
 		//force this list to get initialized before the add
 		EList refs = getModuleRefs();
 
 		if (aModuleFile.isEARFile())
 			//If it's an ear then just treat it like any other archive
 			return (ModuleFile) super.addCopy(aModuleFile);
 		checkAddValid(aModuleFile);
 		ModuleFile copy = getCommonArchiveFactory().copy(aModuleFile);
 		Module newModule = createModuleForCopying(aModuleFile);
 		getFiles().add(copy);
 		copy.initializeClassLoader();
 		if (!copy.getURI().equals(newModule.getUri()))
 			copy.setURI(newModule.getUri());
 
 		getDeploymentDescriptor().getModules().add(newModule);
 		ModuleRef aRef = createModuleRef(newModule, copy);
 		refs.add(aRef);
 		return aRef;
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public ModuleRef addCopyRef(ModuleFile aModuleFile) throws DuplicateObjectException {
 		Object result = primAddCopyRef(aModuleFile);
 		if (result instanceof ModuleRef)
 			return (ModuleRef) result;
 
 		return null;
 	}
 
 
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public SecurityRole addCopy(SecurityRole aRole, Module aModule) throws DuplicateObjectException {
 		EObject dd = null;
 		try {
 			dd = getDeploymentDescriptor(aModule);
 		} catch (Exception e) {
 			throw new DeploymentDescriptorLoadException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.dd_in_ear_load_EXC_, (new Object[]{aModule.getUri(), getURI()})), e); // = "Exception occurred loading deployment descriptor for module "{0}" in ear file "{1}""
 		}
 		String roleName = aRole.getRoleName();
 		SecurityRole copy = (SecurityRole) EtoolsCopyUtility.createCopy(aRole);
 		if (aModule.isEjbModule()) {
 			EJBJar ejbJar = (EJBJar) dd;
 			if (ejbJar.containsSecurityRole(roleName))
 				throw new DuplicateObjectException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.dup_sec_role_module_EXC_, (new Object[]{aModule.getUri(), roleName})), ejbJar.getAssemblyDescriptor().getSecurityRoleNamed(roleName)); // = "Deployment descriptor for {0} already contains a security role named {1}"
 			getAssemblyDescriptorAddIfNecessary(ejbJar).getSecurityRoles().add(copy);
 		} else if (aModule.isWebModule()) {
 			WebApp webApp = (WebApp) dd;
 			if (webApp.containsSecurityRole(roleName))
 				throw new DuplicateObjectException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.dup_sec_role_module_EXC_, (new Object[]{aModule.getUri(), roleName})), webApp.getSecurityRoleNamed(roleName));// = "Deployment descriptor for {0} already contains a security role named {1}"
 			webApp.getSecurityRoles().add(copy);
 		}
 		addCopyIfNotExists(aRole);
 		return copy;
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public SecurityRole addCopyIfNotExists(SecurityRole aRole) {
 		Application dd = getDeploymentDescriptor();
 		SecurityRole copy = null;
 		if (!dd.containsSecurityRole(aRole.getRoleName())) {
 			copy = (SecurityRole) EtoolsCopyUtility.createCopy(aRole);
 			dd.getSecurityRoles().add(copy);
 		}
 		return copy;
 	}
 
 	protected void checkDuplicate(String aUri) throws DuplicateObjectException {
 		if (isDuplicate(aUri))
 			throw new DuplicateObjectException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.dup_resource_EXC_, (new Object[]{aUri, getURI()}))); // = "Resource named "{0}" already exists in archive "{1}""
 	}
 
 
 	protected Module createModuleForCopying(ModuleFile aModuleFile) {
 		Module aModule = null;
 		if (aModuleFile.isWARFile()) {
 			aModule = getApplicationFactory().createWebModule();
 		} else if (aModuleFile.isEJBJarFile()) {
 			aModule = getApplicationFactory().createEjbModule();
 		} else if (aModuleFile.isApplicationClientFile()) {
 			aModule = getApplicationFactory().createJavaClientModule();
 		} else if (aModuleFile.isRARFile()) {
 			aModule = getApplicationFactory().createConnectorModule();
 		}
 		aModule.setUri(aModuleFile.getURI());
 		return aModule;
 	}
 
 	/**
 	 * Retrieves the deployment descriptor root element for the specified module. Takes into account
 	 * altDD indirection.
 	 * 
 	 * @return Only return null if an altDD is not defined.
 	 */
 	public EObject getAltDeploymentDescriptor(Module aModule) throws FileNotFoundException, ResourceLoadException, EmptyResourceException {
 		// Look for altDD
 		String altDD = aModule.getAltDD();
 		if (ArchiveUtil.isNullOrEmpty(altDD))
 			return null;
 		if (!isMofResourceLoaded(altDD.trim()))
 			registerResourceFactory(aModule, altDD);
 		XMLResource ddRes = (XMLResource) getMofResource(altDD.trim());
 		if (!ddRes.isAlt()) {
 			ddRes.setIsAlt(true);
 			ddRes.setApplication(getDeploymentDescriptor());
 		}
 		return ArchiveUtil.getRoot(ddRes);
 
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public java.util.List getApplicationClientFiles() {
 		List clientFiles = new ArrayList();
 		List fileList = getFiles();
 		for (int i = 0; i < fileList.size(); i++) {
 			File aFile = (File) fileList.get(i);
 			if (aFile.isApplicationClientFile()) {
 				clientFiles.add(aFile);
 			}
 		}
 		return clientFiles;
 	}
 
 	protected ApplicationFactory getApplicationFactory() {
 		return ((ApplicationPackage) EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI)).getApplicationFactory();
 	}
 
 	protected AssemblyDescriptor getAssemblyDescriptorAddIfNecessary(EJBJar ejbJar) {
 		AssemblyDescriptor ad = ejbJar.getAssemblyDescriptor();
 		if (ad == null) {
 			ad = ((EjbPackage) EPackage.Registry.INSTANCE.getEPackage(EjbPackage.eNS_URI)).getEjbFactory().createAssemblyDescriptor();
 			ejbJar.setAssemblyDescriptor(ad);
 		}
 		return ad;
 	}
 
 
 	/**
 	 * @throws DeploymentDescriptorLoadException -
 	 *             is a runtime exception, because we can't override the signature of the generated
 	 *             methods
 	 */
 	public Application getDeploymentDescriptor() throws DeploymentDescriptorLoadException {
 		Application dd = this.getDeploymentDescriptorGen();
 		if (dd == null && canLazyInitialize()) {
 			try {
 				getImportStrategy().importMetaData();
 			} catch (Exception e) {
 				throw new DeploymentDescriptorLoadException(getDeploymentDescriptorUri(), e);
 			}
 		}
 		return this.getDeploymentDescriptorGen();
 	}
 
 	/**
 	 * Retrieves the deployment descriptor root element for the specified module. Takes into account
 	 * altDD indirection.
 	 */
 	public Connector getDeploymentDescriptor(ConnectorModule aModule) throws FileNotFoundException, ResourceLoadException, EmptyResourceException {
 		return (Connector) getDeploymentDescriptor((Module) aModule);
 	}
 
 	/**
 	 * Retrieves the deployment descriptor root element for the specified module. Takes into account
 	 * altDD indirection.
 	 */
 	public EJBJar getDeploymentDescriptor(EjbModule aModule) throws FileNotFoundException, ResourceLoadException, EmptyResourceException {
 		return (EJBJar) getDeploymentDescriptor((Module) aModule);
 	}
 
 	/**
 	 * Retrieves the deployment descriptor root element for the specified module. Takes into account
 	 * altDD indirection.
 	 */
 	public ApplicationClient getDeploymentDescriptor(JavaClientModule aModule) throws FileNotFoundException, ResourceLoadException, EmptyResourceException {
 		return (ApplicationClient) getDeploymentDescriptor((Module) aModule);
 	}
 
 	/**
 	 * Retrieves the deployment descriptor root element for the specified module. Takes into account
 	 * altDD indirection.
 	 */
 	public EObject getDeploymentDescriptor(Module aModule) throws FileNotFoundException, ResourceLoadException, EmptyResourceException {
 		EObject dd = getAltDeploymentDescriptor(aModule);
 		if (dd == null)
 			dd = getModuleFile(aModule).getStandardDeploymentDescriptor();
 		return dd;
 	}
 
 	/**
 	 * Retrieves the deployment descriptor root element for the specified module. Takes into account
 	 * altDD indirection.
 	 */
 	public WebApp getDeploymentDescriptor(WebModule aModule) throws FileNotFoundException, ResourceLoadException, EmptyResourceException {
 		return (WebApp) getDeploymentDescriptor((Module) aModule);
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.ModuleFile
 	 */
 	public java.lang.String getDeploymentDescriptorUri() {
 		return J2EEConstants.APPLICATION_DD_URI;
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public java.util.List getEJBJarFiles() {
 		List ejbJarFiles = new ArrayList();
 		List fileList = getFiles();
 		for (int i = 0; i < fileList.size(); i++) {
 			File aFile = (File) fileList.get(i);
 			if (aFile.isEJBJarFile()) {
 				ejbJarFiles.add(aFile);
 			}
 		}
 		return ejbJarFiles;
 	}
 
 	/**
 	 * Return an enterprise bean referenced by the EjbRef, if one exists. The ejb-link value of the
 	 * ref must equate to a named enterprise bean contained in the jar; otherwise return null.
 	 * Returns the first hit found; assumption that the ejb names are unique within the scope of the
 	 * ear file. This will likely be replaced with a better way for dereferencing ejb refs.
 	 * 
 	 * Can be used with ejb 1.1 references only.
 	 * 
 	 * @deprecated {@link#getEnterpiseBeanFromRef(EjbRef ref, String moduleUri )
 	 * @param EjbRef
 	 *            ref - An ejb reference
 	 * @return EnterpriseBean
 	 */
 	public EnterpriseBean getEnterpiseBeanFromRef(EjbRef ref) {
 		String link = ref.getLink();
 		if (link == null) {
 			return null;
 		}
 		List jarFiles = getEJBJarFiles();
 		for (int i = 0; i < jarFiles.size(); i++) {
 			EnterpriseBean bean = ((EJBJarFile) jarFiles.get(i)).getDeploymentDescriptor().getEnterpriseBeanNamed(link);
 			if (bean != null) {
 				return bean;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Return an enterprise bean referenced by the EjbRef, if one exists. The ejb-link value of the
 	 * ref must equate to a named enterprise bean contained in the jar; otherwise return null.
 	 * Returns the first hit found; assumption that the ejb names are unique within the scope of the
 	 * ear file. This will likely be replaced with a better way for dereferencing ejb refs
 	 * 
 	 * Can be used with ejb 1.1 & ejb 2.0 references.
 	 * 
 	 * @param EjbRef
 	 *            ref - An ejb reference
 	 * @param String
 	 *            moduleUri - The module uri
 	 * @return EnterpriseBean
 	 */
 	public EnterpriseBean getEnterpiseBeanFromRef(EjbRef ref, String moduleUri) {
 		String link = ref.getLink();
 		if (link == null) {
 			return null;
 		}// if
 
 		if (link.indexOf('#') == -1) {
 			EnterpriseBean linkedEJB = null;
 			XMLResource res = (XMLResource) ref.eResource();
 			if (res instanceof EJBResource) {
 				//Must be in an EJB JAR to be here
 				EJBJar jar = ((EJBResource) res).getEJBJar();
 				if (jar != null)
 					linkedEJB = jar.getEnterpiseBeanFromRef(ref);
 			}
 			if (linkedEJB == null) {
 				List jarFiles = getEJBJarFiles();
 				for (int i = 0; i < jarFiles.size(); i++) {
 					linkedEJB = ((EJBJarFile) jarFiles.get(i)).getDeploymentDescriptor().getEnterpriseBeanNamed(link);
 					if (linkedEJB != null) {
 						return linkedEJB;
 					}// if
 				}// for
 			} else
 				return linkedEJB;
 		} else {
 			//Grab the ejb name and ejb jar name from the link
 			String ejbBeanName = link.substring(link.indexOf("#") + 1, link.length()); //$NON-NLS-1$
 			String ejbJarName = link.substring(0, link.indexOf("#")); //$NON-NLS-1$
 
 			if (ejbBeanName != null && ejbJarName != null) {
 				//Get the uri of the jar name.
 				String ejbJarUri = ArchiveUtil.deriveEARRelativeURI(ejbJarName, moduleUri);
 				if (ejbJarUri != null) {
 					try {
 						//Get the actual jar file
 						File aFile = getFile(ejbJarUri);
 						if (aFile == null || !aFile.isEJBJarFile())
 							return null;
 
 						EJBJarFile jarFile = (EJBJarFile) getFile(ejbJarUri);
 						List ejbs = jarFile.getDeploymentDescriptor().getEnterpriseBeans();
 						//Check if any of the beans in the list match the given ejb bean name give
 						// in
 						//the link
 						if (ejbs != null && !ejbs.isEmpty()) {
 							Iterator it = ejbs.iterator();
 							while (it.hasNext()) {
 								EnterpriseBean ejbBean = (EnterpriseBean) it.next();
 								if (ejbBean != null && ejbBean.getName().equals(ejbBeanName)) {
 									return ejbBean;
 								}// if
 							}// while
 						}// if
 					} catch (FileNotFoundException fe) {
 						return null;
 					}// try
 				}// if
 			}// if
 		}// if
 		return null;
 	}// getEnterpiseBeanFromRef
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public Module getModule(String aUri, String altDD) {
 		return getDeploymentDescriptor().getModule(aUri, altDD);
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public ModuleRef getModuleRef(Module moduleDescriptor) {
 		List refs = getModuleRefs();
 		for (int i = 0; i < refs.size(); i++) {
 			ModuleRef ref = (ModuleRef) refs.get(i);
 			if (ref.getModule() == moduleDescriptor)
 				return ref;
 		}
 		return null;
 	}
 
 	/**
 	 * Return a filtered list of the archives
 	 */
 	public List getModuleFiles() {
 		List moduleFiles = new ArrayList();
 		List fileList = getFiles();
 		for (int i = 0; i < fileList.size(); i++) {
 			File aFile = (File) fileList.get(i);
 			if (aFile.isModuleFile()) {
 				moduleFiles.add(aFile);
 			}
 		}
 		return moduleFiles;
 	}
 
 	protected ArchiveOptions getOptionsForOpening(String aUri) throws IOException {
 
 		LoadStrategy strategy = getCommonArchiveFactory().createChildLoadStrategy(aUri, getLoadStrategy());
 		return getOptions().cloneWith(strategy, aUri);
 	}
 
 	protected ArchiveOptions getOptionsForOpening(LooseArchive loose) throws IOException, OpenFailureException{
 		if(loose.getBinariesPath() == null){
 			throw new OpenFailureException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.open_nested_EXC_, (new Object[] {loose.getUri(), getURI()})), null); // = "Could not open the nested archive "{0}" in "{1}""
 		}
 		LoadStrategy strategy = getCommonArchiveFactory().createLoadStrategy(loose.getBinariesPath());
 		strategy.setLooseArchive(loose);
 		return getOptions().cloneWith(strategy, loose.getUri());
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public java.util.List getRARFiles() {
 		List rarFiles = new ArrayList();
 		List fileList = getFiles();
 		for (int i = 0; i < fileList.size(); i++) {
 			File aFile = (File) fileList.get(i);
 			if (aFile.isRARFile()) {
 				rarFiles.add(aFile);
 			}
 		}
 		return rarFiles;
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public EList getRolesFromAllModules() {
 		EList roleList = new org.eclipse.emf.common.util.BasicEList();
 		List modules = getDeploymentDescriptor().getModules();
 		for (int i = 0, n = modules.size(); i < n; i++) {
 			Module aModule = (Module) modules.get(i);
 			EList roles = getRolesFromModule(aModule);
 			for (int j = 0, m = roles.size(); j < m; j++) {
 				roleList.add(roles.get(j));
 			}
 		}
 		return roleList;
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public EList getRolesFromModule(Module aModule) {
 		EList roleList = new org.eclipse.emf.common.util.BasicEList();
 		try {
 			if (aModule.isWebModule())
 				roleList = getDeploymentDescriptor((WebModule) aModule).getSecurityRoles();
 			else if (aModule.isEjbModule())
 				roleList = getAssemblyDescriptorAddIfNecessary(getDeploymentDescriptor((EjbModule) aModule)).getSecurityRoles();
 		} catch (Exception e) {
 			throw new DeploymentDescriptorLoadException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.dd_in_ear_load_EXC_, (new Object[]{aModule.getUri(), getURI()})), e); // = "Exception occurred loading deployment descriptor for module "{0}" in ear file "{1}""
 		}
 		return roleList;
 	}
 
 	/**
 	 * Return the DeployementDescriptor.
 	 */
 	public EObject getStandardDeploymentDescriptor() throws DeploymentDescriptorLoadException {
 		return getDeploymentDescriptor();
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public java.util.List getWARFiles() {
 		List warFiles = new ArrayList();
 		List fileList = getFiles();
 		for (int i = 0; i < fileList.size(); i++) {
 			File aFile = (File) fileList.get(i);
 			if (aFile.isWARFile()) {
 				warFiles.add(aFile);
 			}
 		}
 		return warFiles;
 	}
 
 	public void initializeAfterOpen() {
 		super.initializeAfterOpen();
 	}
 
 	public boolean isDeploymentDescriptorSet() {
 		return deploymentDescriptor != null;
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.File
 	 */
 	public boolean isEARFile() {
 		return true;
 	}
 
 	/**
 	 * Return true if super returns true, or return whether the app dd contains a module having the
 	 * uri
 	 */
 	public boolean isNestedArchive(String aUri) {
 		if (super.isNestedArchive(aUri))
 			return true;
 		return getDeploymentDescriptor().getFirstModule(aUri) != null;
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.impl.ModuleFileImpl
 	 */
 	public org.eclipse.emf.ecore.EObject makeDeploymentDescriptor(XMLResource resource) {
 		Application appl = ((ApplicationPackage) EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI)).getApplicationFactory().createApplication();
 		resource.setID(appl, J2EEConstants.APPL_ID);
 		setDeploymentDescriptorGen(appl);
 		resource.getContents().add(appl);
 		return appl;
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.Archive
 	 */
 	public Archive openNestedArchive(LooseArchive loose) throws OpenFailureException {
 
 		Module m = getDeploymentDescriptor().getFirstModule(loose.getUri());
 		if (m == null)
 			return super.openNestedArchive(loose);
 
 		try {
 			ArchiveOptions archiveOptions = getOptionsForOpening(loose);
 			return openNestedArchive(m, archiveOptions);
 		} catch (java.io.IOException ex) {
 			//Probably the file did not exist; silently fail, per request from WS runtime
 			//More than likely a failure is going to occur down the road when a ModuleFile for a
 			// Module
 			//in the dd is requested
 		}
 		return null;
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.Archive
 	 */
 	public Archive openNestedArchive(String aUri) throws OpenFailureException {
 
 		Module m = getDeploymentDescriptor().getFirstModule(aUri);
 		if (m == null)
 			return super.openNestedArchive(aUri);
 
 		try {
 			ArchiveOptions archiveOptions = getOptionsForOpening(aUri);
 			return openNestedArchive(m, archiveOptions);
 		} catch (java.io.IOException ex) {
 			//Probably the file did not exist; silently fail, per request from WS runtime
 			//More than likely a failure is going to occur down the road when a ModuleFile for a
 			// Module
 			//in the dd is requested
 		}
 		return null;
 	}
 
 	protected Archive openNestedArchive(Module m, ArchiveOptions archiveOptions) throws OpenFailureException {
 		String aUri = m.getUri();
 
 		try {
 			/*
 			 * Since we have a clue about the type of archive to open Use the appropriate open
 			 * method, so if a failure occurrs, we can get a more specific message
 			 */
 			if (m.isWebModule())
 				return getCommonArchiveFactory().openWARFile(archiveOptions, aUri);
 			else if (m.isEjbModule())
 				return getCommonArchiveFactory().openEJBJarFile(archiveOptions, aUri);
 			else if (m.isJavaModule())
 				return getCommonArchiveFactory().openApplicationClientFile(archiveOptions, aUri);
 			else if (m.isConnectorModule())
 				return getCommonArchiveFactory().openRARFile(archiveOptions, aUri);
 
 		} catch (OpenFailureException ex) {
 			throw new OpenFailureException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.nested_open_fail_EXC_, (new Object[]{aUri, getURI()})), ex); // = "Unable to open module file "{0}" in EAR file "{1}""
 		}
 		//Should never reach the code below
 		return null;
 	}
 
 	/**
 	 * @see EARFile
 	 */
 	public void pushDownRole(SecurityRole role) {
 		if (role == null)
 			throw new IllegalArgumentException(CommonArchiveResourceHandler.Parameter_should_not_be_nu_EXC_); // = "Parameter should not be null"
 
 		List modules = getDeploymentDescriptor().getModules();
 		for (int i = 0; i < modules.size(); i++) {
 			Module m = (Module) modules.get(i);
 			pushDownRole(role, m);
 		}
 	}
 
 	/**
 	 * @see EARFile
 	 */
 	public void pushDownRole(SecurityRole role, Module aModule) {
 		try {
 			if (aModule.isWebModule()) {
 				WebApp dd = getDeploymentDescriptor((WebModule) aModule);
 				if (dd.getSecurityRoleNamed(role.getRoleName()) == null)
 					dd.getSecurityRoles().add(EtoolsCopyUtility.createCopy(role));
 			} else if (aModule.isEjbModule()) {
 				AssemblyDescriptor assembly = getAssemblyDescriptorAddIfNecessary(getDeploymentDescriptor((EjbModule) aModule));
 				if (assembly.getSecurityRoleNamed(role.getRoleName()) == null)
 					assembly.getSecurityRoles().add(EtoolsCopyUtility.createCopy(role));
 			}
 		} catch (Exception e) {
 			throw new DeploymentDescriptorLoadException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.dd_in_ear_load_EXC_, (new Object[]{aModule.getUri(), getURI()})), e); // = "Exception occurred loading deployment descriptor for module "{0}" in ear file "{1}""
 		}
 	}
 
 	/**
 	 * An alt-dd is about to be loaded; register the appropriate resource factory for the uri so it
 	 * will get loaded correctly.
 	 */
 	protected void registerResourceFactory(Module aModule, String aUri) {
 		org.eclipse.emf.ecore.resource.Resource.Factory factory = null;
 		if (aModule.isEjbModule())
 			factory = EJBJarResourceFactory.getRegisteredFactory();
 		else if (aModule.isWebModule())
 			factory = WebAppResourceFactory.getRegisteredFactory();
 		else if (aModule.isJavaModule())
 			factory = ApplicationClientResourceFactory.getRegisteredFactory();
 		else if (aModule.isConnectorModule())
 			factory = ConnectorResourceFactory.getRegisteredFactory();
 
 		if (factory != null) {
 			ResourceSet set = getResourceSet();
 			((FileNameResourceFactoryRegistry) set.getResourceFactoryRegistry()).registerLastFileSegment(aUri, factory);
 		}
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public void remove(ModuleRef aModuleRef) {
 		if (aModuleRef == null)
 			return;
 		Module aModule = aModuleRef.getModule();
 
 		if (aModule != null)
 			getDeploymentDescriptor().getModules().remove(aModule);
 
 		getModuleRefs().remove(aModuleRef);
 		if (getModuleRefs(aModuleRef.getModuleFile()).isEmpty())
 			getFiles().remove(aModuleRef.getModuleFile());
 	}
 
 	/**
 	 * @see EARFile
 	 */
 	public List getModuleRefs(ModuleFile aModuleFile) {
 		List refs = getModuleRefs();
 		List result = new ArrayList(1);
 		for (int i = 0; i < refs.size(); i++) {
 			ModuleRef ref = (ModuleRef) refs.get(i);
 			if (ref.getModuleFile() == aModuleFile)
 				result.add(ref);
 		}
 		return result;
 	}
 
 	protected void renameRolesInModuleDDs(String existingRoleName, String newRoleName) {
 		List modules = getDeploymentDescriptor().getModules();
 		for (int i = 0; i < modules.size(); i++) {
 			Module m = (Module) modules.get(i);
 			try {
 				if (m.isEjbModule())
 					getDeploymentDescriptor((EjbModule) m).renameSecurityRole(existingRoleName, newRoleName);
 				else if (m.isWebModule())
 					getDeploymentDescriptor((WebModule) m).renameSecurityRole(existingRoleName, newRoleName);
 			} catch (Exception e) {
 				throw new DeploymentDescriptorLoadException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.dd_in_ear_load_EXC_, (new Object[]{m.getUri(), getURI()})), e); // = "Exception occurred loading deployment descriptor for module "{0}" in ear file "{1}""
 			}
 		}
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public void renameSecurityRole(String existingRoleName, String newRoleName) throws ObjectNotFoundException, DuplicateObjectException {
 		Application app = getDeploymentDescriptor();
 		SecurityRole aRole = app.getSecurityRoleNamed(existingRoleName);
 		if (aRole == null)
 			throw new ObjectNotFoundException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.no_sec_role_EXC_, (new Object[]{getURI(), existingRoleName}))); // = ": EAR File deployment descriptor does not contain a security role named "
 		if (app.getSecurityRoleNamed(newRoleName) != null)
 			throw new DuplicateObjectException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.dup_sec_role_EXC_, (new Object[]{getURI(), newRoleName})), app.getSecurityRoleNamed(newRoleName)); // = ": EAR File deployment descriptor already contains a security role named "
 
 		aRole.setRoleName(newRoleName);
 
 		renameRolesInModuleDDs(existingRoleName, newRoleName);
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public void rollUpRoles() {
 		List modules = getDeploymentDescriptor().getModules();
 		for (int i = 0; i < modules.size(); i++) {
 			Module aModule = (Module) modules.get(i);
 			rollUpRoles(aModule);
 		}
 	}
 
 	/**
 	 * @see com.ibm.etools.commonarchive.EARFile
 	 */
 	public void rollUpRoles(Module aModule) {
 		List securityRoles = null;
 		try {
 			if (aModule.isWebModule())
 				securityRoles = getDeploymentDescriptor((WebModule) aModule).getSecurityRoles();
 			else if (aModule.isEjbModule())
 				securityRoles = getAssemblyDescriptorAddIfNecessary(getDeploymentDescriptor((EjbModule) aModule)).getSecurityRoles();
 		} catch (Exception e) {
 			throw new DeploymentDescriptorLoadException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.dd_in_ear_load_EXC_, (new Object[]{aModule.getUri(), getURI()})), e); // = "Exception occurred loading deployment descriptor for module "{0}" in ear file "{1}""
 		}
 
 		if (securityRoles == null)
 			return;
 		for (int i = 0; i < securityRoles.size(); i++) {
 			addCopyIfNotExists((SecurityRole) securityRoles.get(i));
 		}
 	}
 
 	public void setDeploymentDescriptor(Application l) {
 		this.setDeploymentDescriptorGen(l);
 		replaceRoot(getMofResourceMakeIfNecessary(getDeploymentDescriptorUri()), l);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public boolean eIsSet(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case CommonarchivePackage.EAR_FILE__URI:
 				return URI_EDEFAULT == null ? uri != null : !URI_EDEFAULT.equals(uri);
 			case CommonarchivePackage.EAR_FILE__LAST_MODIFIED:
 				return isSetLastModified();
 			case CommonarchivePackage.EAR_FILE__SIZE:
 				return isSetSize();
 			case CommonarchivePackage.EAR_FILE__DIRECTORY_ENTRY:
 				return isSetDirectoryEntry();
 			case CommonarchivePackage.EAR_FILE__ORIGINAL_URI:
 				return ORIGINAL_URI_EDEFAULT == null ? originalURI != null : !ORIGINAL_URI_EDEFAULT.equals(originalURI);
 			case CommonarchivePackage.EAR_FILE__LOADING_CONTAINER:
 				return loadingContainer != null;
 			case CommonarchivePackage.EAR_FILE__CONTAINER:
 				return getContainer() != null;
 			case CommonarchivePackage.EAR_FILE__FILES:
 				return files != null && !files.isEmpty();
 			case CommonarchivePackage.EAR_FILE__TYPES:
 				return types != null && !types.isEmpty();
 			case CommonarchivePackage.EAR_FILE__MODULE_REFS:
 				return moduleRefs != null && !moduleRefs.isEmpty();
 			case CommonarchivePackage.EAR_FILE__DEPLOYMENT_DESCRIPTOR:
 				return deploymentDescriptor != null;
 		}
 		return eDynamicIsSet(eFeature);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void eSet(EStructuralFeature eFeature, Object newValue) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case CommonarchivePackage.EAR_FILE__URI:
 				setURI((String)newValue);
 				return;
 			case CommonarchivePackage.EAR_FILE__LAST_MODIFIED:
 				setLastModified(((Long)newValue).longValue());
 				return;
 			case CommonarchivePackage.EAR_FILE__SIZE:
 				setSize(((Long)newValue).longValue());
 				return;
 			case CommonarchivePackage.EAR_FILE__DIRECTORY_ENTRY:
 				setDirectoryEntry(((Boolean)newValue).booleanValue());
 				return;
 			case CommonarchivePackage.EAR_FILE__ORIGINAL_URI:
 				setOriginalURI((String)newValue);
 				return;
 			case CommonarchivePackage.EAR_FILE__LOADING_CONTAINER:
 				setLoadingContainer((Container)newValue);
 				return;
 			case CommonarchivePackage.EAR_FILE__CONTAINER:
 				setContainer((Container)newValue);
 				return;
 			case CommonarchivePackage.EAR_FILE__FILES:
 				getFiles().clear();
 				getFiles().addAll((Collection)newValue);
 				return;
 			case CommonarchivePackage.EAR_FILE__TYPES:
 				getTypes().clear();
 				getTypes().addAll((Collection)newValue);
 				return;
 			case CommonarchivePackage.EAR_FILE__MODULE_REFS:
 				getModuleRefs().clear();
 				getModuleRefs().addAll((Collection)newValue);
 				return;
 			case CommonarchivePackage.EAR_FILE__DEPLOYMENT_DESCRIPTOR:
 				setDeploymentDescriptor((Application)newValue);
 				return;
 		}
 		eDynamicSet(eFeature, newValue);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void eUnset(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case CommonarchivePackage.EAR_FILE__URI:
 				setURI(URI_EDEFAULT);
 				return;
 			case CommonarchivePackage.EAR_FILE__LAST_MODIFIED:
 				unsetLastModified();
 				return;
 			case CommonarchivePackage.EAR_FILE__SIZE:
 				unsetSize();
 				return;
 			case CommonarchivePackage.EAR_FILE__DIRECTORY_ENTRY:
 				unsetDirectoryEntry();
 				return;
 			case CommonarchivePackage.EAR_FILE__ORIGINAL_URI:
 				setOriginalURI(ORIGINAL_URI_EDEFAULT);
 				return;
 			case CommonarchivePackage.EAR_FILE__LOADING_CONTAINER:
 				setLoadingContainer((Container)null);
 				return;
 			case CommonarchivePackage.EAR_FILE__CONTAINER:
 				setContainer((Container)null);
 				return;
 			case CommonarchivePackage.EAR_FILE__FILES:
 				getFiles().clear();
 				return;
 			case CommonarchivePackage.EAR_FILE__TYPES:
 				getTypes().clear();
 				return;
 			case CommonarchivePackage.EAR_FILE__MODULE_REFS:
 				getModuleRefs().clear();
 				return;
 			case CommonarchivePackage.EAR_FILE__DEPLOYMENT_DESCRIPTOR:
 				setDeploymentDescriptor((Application)null);
 				return;
 		}
 		eDynamicUnset(eFeature);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation
 	 */
 	public Application getDeploymentDescriptorGen() {
 		if (deploymentDescriptor != null && deploymentDescriptor.eIsProxy()) {
 			Application oldDeploymentDescriptor = deploymentDescriptor;
 			deploymentDescriptor = (Application)eResolveProxy((InternalEObject)deploymentDescriptor);
 			if (deploymentDescriptor != oldDeploymentDescriptor) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, CommonarchivePackage.EAR_FILE__DEPLOYMENT_DESCRIPTOR, oldDeploymentDescriptor, deploymentDescriptor));
 			}
 		}
 		return deploymentDescriptor;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Application basicGetDeploymentDescriptor() {
 		return deploymentDescriptor;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void setDeploymentDescriptorGen(Application newDeploymentDescriptor) {
 		Application oldDeploymentDescriptor = deploymentDescriptor;
 		deploymentDescriptor = newDeploymentDescriptor;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, CommonarchivePackage.EAR_FILE__DEPLOYMENT_DESCRIPTOR, oldDeploymentDescriptor, deploymentDescriptor));
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation
 	 */
 	public EList getModuleRefsGen() {
 		if (moduleRefs == null) {
 			moduleRefs = new EObjectContainmentWithInverseEList(ModuleRef.class, this, CommonarchivePackage.EAR_FILE__MODULE_REFS, CommonarchivePackage.MODULE_REF__EAR_FILE);
 		}
 		return moduleRefs;
 	}
 
 	public EList getModuleRefs() {
 		EList refs = getModuleRefsGen();
 
 		if (refs.isEmpty())
 			initRefs(refs);
 
 		return refs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case CommonarchivePackage.EAR_FILE__CONTAINER:
 					if (eContainer != null)
 						msgs = eBasicRemoveFromContainer(msgs);
 					return eBasicSetContainer(otherEnd, CommonarchivePackage.EAR_FILE__CONTAINER, msgs);
 				case CommonarchivePackage.EAR_FILE__FILES:
 					return ((InternalEList)getFiles()).basicAdd(otherEnd, msgs);
 				case CommonarchivePackage.EAR_FILE__MODULE_REFS:
 					return ((InternalEList)getModuleRefs()).basicAdd(otherEnd, msgs);
 				default:
 					return eDynamicInverseAdd(otherEnd, featureID, baseClass, msgs);
 			}
 		}
 		if (eContainer != null)
 			msgs = eBasicRemoveFromContainer(msgs);
 		return eBasicSetContainer(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case CommonarchivePackage.EAR_FILE__CONTAINER:
 					return eBasicSetContainer(null, CommonarchivePackage.EAR_FILE__CONTAINER, msgs);
 				case CommonarchivePackage.EAR_FILE__FILES:
 					return ((InternalEList)getFiles()).basicRemove(otherEnd, msgs);
 				case CommonarchivePackage.EAR_FILE__MODULE_REFS:
 					return ((InternalEList)getModuleRefs()).basicRemove(otherEnd, msgs);
 				default:
 					return eDynamicInverseRemove(otherEnd, featureID, baseClass, msgs);
 			}
 		}
 		return eBasicSetContainer(null, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eBasicRemoveFromContainer(NotificationChain msgs) {
 		if (eContainerFeatureID >= 0) {
 			switch (eContainerFeatureID) {
 				case CommonarchivePackage.EAR_FILE__CONTAINER:
 					return eContainer.eInverseRemove(this, CommonarchivePackage.CONTAINER__FILES, Container.class, msgs);
 				default:
 					return eDynamicBasicRemoveFromContainer(msgs);
 			}
 		}
 		return eContainer.eInverseRemove(this, EOPPOSITE_FEATURE_BASE - eContainerFeatureID, null, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(EStructuralFeature eFeature, boolean resolve) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case CommonarchivePackage.EAR_FILE__URI:
 				return getURI();
 			case CommonarchivePackage.EAR_FILE__LAST_MODIFIED:
 				return new Long(getLastModified());
 			case CommonarchivePackage.EAR_FILE__SIZE:
 				return new Long(getSize());
 			case CommonarchivePackage.EAR_FILE__DIRECTORY_ENTRY:
 				return isDirectoryEntry() ? Boolean.TRUE : Boolean.FALSE;
 			case CommonarchivePackage.EAR_FILE__ORIGINAL_URI:
 				return getOriginalURI();
 			case CommonarchivePackage.EAR_FILE__LOADING_CONTAINER:
 				if (resolve) return getLoadingContainer();
 				return basicGetLoadingContainer();
 			case CommonarchivePackage.EAR_FILE__CONTAINER:
 				return getContainer();
 			case CommonarchivePackage.EAR_FILE__FILES:
 				return getFiles();
 			case CommonarchivePackage.EAR_FILE__TYPES:
 				return getTypes();
 			case CommonarchivePackage.EAR_FILE__MODULE_REFS:
 				return getModuleRefs();
 			case CommonarchivePackage.EAR_FILE__DEPLOYMENT_DESCRIPTOR:
 				if (resolve) return getDeploymentDescriptor();
 				return basicGetDeploymentDescriptor();
 		}
 		return eDynamicGet(eFeature, resolve);
 	}
 
 	public void initRefs(EList refs) {
 		List modules = getDeploymentDescriptor().getModules();
 		for (int i = 0; i < modules.size(); i++) {
 			Module module = (Module) modules.get(i);
 			refs.add(createModuleRef(module, null));
 		}
 	}
 
 	protected ModuleRef createModuleRef(Module aModule, ModuleFile aFile) {
 		ModuleRef newRef = null;
 
 		if (aModule.isEjbModule())
 			newRef = getCommonArchiveFactory().createEJBModuleRef();
 		else if (aModule.isWebModule())
 			newRef = getCommonArchiveFactory().createWebModuleRef();
 		else if (aModule.isConnectorModule())
 			newRef = getCommonArchiveFactory().createConnectorModuleRef();
 		else if (aModule.isJavaModule())
 			newRef = getCommonArchiveFactory().createClientModuleRef();
 
 		newRef.setModule(aModule);
 		newRef.setModuleFile(aFile);
 
 		return newRef;
 	}
 
 	/*
 	 * @see EARFile#getClientModuleRefs()
 	 */
 	public List getClientModuleRefs() {
 		List result = new ArrayList();
 		List refs = getModuleRefs();
 		for (int i = 0; i < refs.size(); i++) {
 			ModuleRef ref = (ModuleRef) refs.get(i);
 			if (ref.isClient())
 				result.add(ref);
 		}
 		return result;
 	}
 
 	/*
 	 * @see EARFile#getConnectorModuleRefs()
 	 */
 	public List getConnectorModuleRefs() {
 		List result = new ArrayList();
 		List refs = getModuleRefs();
 		for (int i = 0; i < refs.size(); i++) {
 			ModuleRef ref = (ModuleRef) refs.get(i);
 			if (ref.isConnector())
 				result.add(ref);
 		}
 		return result;
 	}
 
 	/*
 	 * @see EARFile#getEJBModuleRefs()
 	 */
 	public List getEJBModuleRefs() {
 		List result = new ArrayList();
 		List refs = getModuleRefs();
 		for (int i = 0; i < refs.size(); i++) {
 			ModuleRef ref = (ModuleRef) refs.get(i);
 			if (ref.isEJB())
 				result.add(ref);
 		}
 		return result;
 	}
 
 	/*
 	 * @see EARFile#getWebModuleRefs()
 	 */
 	public List getWebModuleRefs() {
 		List result = new ArrayList();
 		List refs = getModuleRefs();
 		for (int i = 0; i < refs.size(); i++) {
 			ModuleRef ref = (ModuleRef) refs.get(i);
 			if (ref.isWeb())
 				result.add(ref);
 		}
 		return result;
 	}
 
 	/*
 	 * @see EARFile#getFARFiles()
 	 */
 	public List getFARFiles() {
 		List farFiles = new ArrayList();
 		List fileList = getFiles();
 		for (int i = 0; i < fileList.size(); i++) {
 			File aFile = (File) fileList.get(i);
 			if (aFile.isFARFile()) {
 				farFiles.add(aFile);
 			}
 		}
 		return farFiles;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ibm.etools.commonarchive.EARFile#getArchivesOfType(java.lang.String)
 	 */
 	public List getArchivesOfType(String type) {
 		return Collections.EMPTY_LIST;
 	}
 
 	public Map getEJBReferences(boolean filterLinkedReferences, boolean filterNonLinkedReferences) throws ArchiveWrappedException {
 		if (!filterLinkedReferences || !filterNonLinkedReferences) {
 			Application app = getDeploymentDescriptor();
 			if (app != null) {
 				List modules = app.getModules();
 				Module module;
 				Map references = null;
 				for (int i = 0; i < modules.size(); i++) {
 					module = (Module) modules.get(i);
 					try {
 						references = collectEJBReferences(module, filterLinkedReferences, filterNonLinkedReferences, references);
 					} catch (ResourceLoadException e) {
 						throw new ArchiveWrappedException(e);
 					} catch (FileNotFoundException e) {
 						throw new ArchiveWrappedException(e);
 					} catch (EmptyResourceException e) {
 						throw new ArchiveWrappedException(e);
 					}
 				}
 				return references;
 			}
 		}
 		return Collections.EMPTY_MAP;
 	}
 
 	/**
 	 * @param module
 	 * @param filterLinkedReferences
 	 * @param filterNonLinkedReferences
 	 * @param references
 	 * @return
 	 */
 	private Map collectEJBReferences(Module module, boolean filterLinkedReferences, boolean filterNonLinkedReferences, Map references) throws ResourceLoadException, FileNotFoundException, EmptyResourceException {
 		Map myReferences = references;
 		List moduleReferences = getEJBReferneces(module);
 		if (moduleReferences != null && !moduleReferences.isEmpty()) {
 			EjbRef ref = null;
 			List filteredRefs = null;
 			String link = null;
 			for (int i = 0; i < moduleReferences.size(); i++) {
 				ref = (EjbRef) moduleReferences.get(i);
 				link = ref.getLink();
				if ((link != null && link.length() > 0 && !filterLinkedReferences) || (link == null || link.length() == 0 && !filterNonLinkedReferences)) {
 					if (filteredRefs == null)
 						filteredRefs = new ArrayList(moduleReferences.size());
 					filteredRefs.add(ref);
 				}
 			}
 			if (filteredRefs != null) {
 				if (myReferences == null)
 					myReferences = new HashMap();
 				myReferences.put(module, filteredRefs);
 			}
 		}
 		return myReferences;
 	}
 
 	/**
 	 * @param module
 	 * @return
 	 */
 	private List getEJBReferneces(Module module) throws ResourceLoadException, FileNotFoundException, EmptyResourceException {
 		if (module != null) {
 			if (module.isEjbModule()) {
 				EJBJar ejbJar = getDeploymentDescriptor((EjbModule) module);
 				return getEJBReferences(ejbJar);
 			} else if (module.isWebModule()) {
 				WebApp webApp = getDeploymentDescriptor((WebModule) module);
 				if (webApp != null)
 					return webApp.getEjbRefs();
 			} else if (module.isJavaModule()) {
 				ApplicationClient appClient = getDeploymentDescriptor((JavaClientModule) module);
 				if (appClient != null)
 					return appClient.getEjbReferences();
 			}
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @param ejbJar
 	 * @return
 	 */
 	private List getEJBReferences(EJBJar ejbJar) {
 		if (ejbJar != null) {
 			List ejbs = ejbJar.getEnterpriseBeans();
 			List refs = null;
 			EnterpriseBean ejb = null;
 			for (int i = 0; i < ejbs.size(); i++) {
 				ejb = (EnterpriseBean) ejbs.get(i);
 				if (refs == null)
 					refs = new ArrayList();
 				refs.addAll(ejb.getEjbRefs());
 			}
 			if (refs != null)
 				return refs;
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 }
