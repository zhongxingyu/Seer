 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.validation;
 
 
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.core.runtime.jobs.MultiRule;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.jem.util.emf.workbench.WorkbenchURIConverter;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.application.EjbModule;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.application.WebModule;
 import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
 import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
 import org.eclipse.jst.j2ee.common.EjbRef;
 import org.eclipse.jst.j2ee.common.MessageDestinationRef;
 import org.eclipse.jst.j2ee.common.ResourceEnvRef;
 import org.eclipse.jst.j2ee.common.ResourceRef;
 import org.eclipse.jst.j2ee.common.SecurityRoleRef;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.File;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.ModuleFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.ValidateXmlCommand;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.ManifestException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveConstants;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.model.internal.validation.EARValidationMessageResourceHandler;
 import org.eclipse.jst.j2ee.model.internal.validation.EarValidator;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.j2ee.webservice.wsclient.ServiceRef;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.operations.IWorkbenchContext;
 import org.eclipse.wst.validation.internal.operations.LocalizedMessage;
 import org.eclipse.wst.validation.internal.provisional.core.IMessage;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
 import org.eclipse.wst.validation.internal.provisional.core.IValidatorJob;
 
 
 /**
  * Insert the type's description here. Creation date: (9/10/2001 2:11:02 PM)
  * 
  * @author: Administrator
  */
 public class UIEarValidator extends EarValidator {
 	public static final String VALIDATOR_ID = "org.eclipse.jst.j2ee.internal.validation.UIEarValidator"; //$NON-NLS-1$
 	public static final String MANIFEST_GROUP_NAME = "WSAD.EAR.MANIFEST"; //$NON-NLS-1$
 	public static final String DOCTYPE_1_2 = "1.2"; //$NON-NLS-1$
 	public static final String DOCTYPE_1_3 = "1.3"; //$NON-NLS-1$
 	
 	protected UIEarHelper earHelper;
 //	private EARArtifactEdit earEdit = null;
 	private IProject project = null;
 
 	/**
 	 * UIEarValidator constructor comment.
 	 */
 	public UIEarValidator() {
 		super();
 	}
 
 	public Command createValidateXMLCommand() {
 
 		ValidateXmlCommand cmd = (ValidateXmlCommand) super.createValidateXMLCommand();
 		cmd.setValidateNested(false);
 		return cmd;
 	}
 
 	protected String getResourceName() {
 		return ((EarHelper) _helper).getProject().getName();
 	}
 
 	protected void duplicateProjectMapError(String earProjectName, String moduleUri, String projectName) {
 
 		String[] params = new String[3];
 		params[0] = projectName;
 		params[1] = earProjectName;
 		params[2] = moduleUri;
 		String msg = NLS.bind(EARValidationMessageResourceHandler.DUPLICATE_MODULE_FOR_PROJECT_NAME_ERROR_, params);
 	
 		addLocalizedError(msg, appDD);
 	}
 
 	/**
 	 *  
 	 */
 	protected void cleanUpSubTaskMessages(EObject ref) {
 		String groupName = EJB_REF_GROUP_NAME;
 		if (ref instanceof EjbRef)
 		{
 //			ref = (EjbRef) ref;
 		}
 		else if (ref instanceof ResourceRef) {
 //			ref = (ResourceRef) ref;
 			groupName = RES_REF_GROUP_NAME;
 		} else if (ref instanceof ServiceRef) {
 //			ref = (ServiceRef) ref;
 			groupName = SERVICE_REF_GROUP_NAME;
 		} else if (ref instanceof ResourceEnvRef) {
 //			ref = (ResourceEnvRef) ref;
 			groupName = RES_ENV_REF_GROUP_NAME;
 		} else if (ref instanceof SecurityRoleRef) {
 //			ref = (SecurityRoleRef) ref;
 			groupName = SEC_ROLE_REF_GROUP_NAME;
 		} else if (ref instanceof MessageDestinationRef) {
 //			ref = (MessageDestinationRef) ref;
 			groupName = MESSAGE_REF_GROUP_NAME;
 		}
 		Resource res = ref.eResource();
 		if (res != null) {
 			IFile file = WorkbenchResourceHelper.getFile(res);
 			if (file != null)
 				_reporter.removeMessageSubset(this, file, groupName);
 		}
 	}
 
 	protected void cleanUpAllRefSubTaskMessages(Resource res) {
 		if (res != null) {
 			IFile file = WorkbenchResourceHelper.getFile(res);
 			if (file != null)
 				_reporter.removeMessageSubset(this, file, EJB_REF_GROUP_NAME);
 			_reporter.removeMessageSubset(this, file, RES_REF_GROUP_NAME);
 			_reporter.removeMessageSubset(this, file, SERVICE_REF_GROUP_NAME);
 			_reporter.removeMessageSubset(this, file, SEC_ROLE_REF_GROUP_NAME);
 			_reporter.removeMessageSubset(this, file, MESSAGE_REF_GROUP_NAME);
 			_reporter.removeMessageSubset(this, file, RES_ENV_REF_GROUP_NAME);
 		}
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (9/10/2001 2:56:32 PM)
 	 * 
 	 * @return org.eclipse.wst.validation.internal.core.core.ear.workbenchimpl.UIEarHelper
 	 */
 	public org.eclipse.jst.j2ee.internal.validation.UIEarHelper getEarHelper() {
 		return earHelper;
 	}
 
 	protected void invalidClassPathEntryWarning(String entry, Archive anArchive) {
 		String[] params = new String[2];
 		params[0] = anArchive.getURI();
 		params[1] = entry;
 		String msg = NLS.bind(EARValidationMessageResourceHandler.INVALID_MANIFEST_CLASSPATH_ONE_WARN_, params);
 		addLocalizedWarning(msg, getManifestFile(anArchive), MANIFEST_GROUP_NAME);
 	}
 
 	protected void invalidClassPathEntryWarning(String entry, String resolvedEntry, Archive anArchive) {
 		String[] params = new String[3];
 		params[0] = anArchive.getURI();
 		params[1] = entry;
 		params[2] = resolvedEntry;
 		String msg = NLS.bind(EARValidationMessageResourceHandler.INVALID_MANIFEST_CLASSPATH_TWO_WARN_, params);		
 		addLocalizedWarning(msg, getManifestFile(anArchive), MANIFEST_GROUP_NAME);
 	}
 
 	protected void invalidDepedencyWarning(String entry, Archive anArchive, ModuleFile m) {
 		String[] params = new String[3];
 		params[0] = m.getURI();
 		params[1] = entry;
 		params[2] = anArchive.getURI();
 		String msg = NLS.bind(EARValidationMessageResourceHandler.INVALID_MANIFEST_CLASSPATH_DEPENDENCY_WARN_, params);
 		
 		addLocalizedWarning(msg, getManifestFile(anArchive), MANIFEST_GROUP_NAME);
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (9/10/2001 2:56:32 PM)
 	 * 
 	 * @param newEarHelper
 	 *            org.eclipse.wst.validation.internal.core.core.ear.workbenchimpl.UIEarHelper
 	 */
 	public void setEarHelper(org.eclipse.jst.j2ee.internal.validation.UIEarHelper newEarHelper) {
 		earHelper = newEarHelper;
 	}
 
 	public IStatus validateInJob(IValidationContext inHelper, IReporter inReporter) throws org.eclipse.wst.validation.internal.core.ValidationException {
 		IStatus status = IValidatorJob.OK_STATUS;
 		IProject earProj = ((IWorkbenchContext) inHelper).getProject();
 		IVirtualComponent earModule = ComponentCore.createComponent(earProj);
             if(J2EEProjectUtilities.isEARProject(earProj)){
 				IVirtualFile ddFile = earModule.getRootFolder().getFile(J2EEConstants.APPLICATION_DD_URI);
 				if( ddFile.exists()) {
 					inReporter.removeAllMessages(this);
 					
 					IVirtualReference[] earReferences;
 					boolean isMixedEAR = false;
 					boolean isJavaEEFiveProject = false;
 					boolean isLegacyEAR = J2EEProjectUtilities.isLegacyJ2EEProject(earProj);
 					
 					//because of [224484] 5.0 EARs may get to this validator when they should not, need to protect against this
 					if(isLegacyEAR) {
 						earReferences = earModule.getReferences();
 						IVirtualComponent referencedComponenet;
 						IProject earRefedProj;
 						//[Bug  241525] need to use referenced components because referenced projects return the EAR for referenced binary archives
 						for(IVirtualReference earReference : earReferences) {
 							referencedComponenet = earReference.getReferencedComponent();
 							
 							//[Bug  241525]if its a VirtualArchiveComponent then we need to use
 							//	components to get version, otherwise use IProject
 							if(referencedComponenet instanceof VirtualArchiveComponent) {
 								isJavaEEFiveProject = JavaEEProjectUtilities.isJEEComponent(referencedComponenet);
 							} else {
 								earRefedProj = referencedComponenet.getProject();
 								isJavaEEFiveProject = J2EEProjectUtilities.isJEEProject(earRefedProj);
 							}
 							
 							if(isJavaEEFiveProject) {
 								 //HACK: this is normally done by the call to super.validateInJob but in this case we are purposely avoiding that call
 								_reporter = inReporter;
 								
 								String[] params = {earProj.getName(), referencedComponenet.getName()};
 								String msg = NLS.bind(EARValidationMessageResourceHandler.JEE5_PROJECT_REFERENCED_BY_PRE_JEE5_EAR, params);
								addLocalizedWarning(msg,earProj);
 							
 							}
 							
 							//if any referenced project is a JEE 5 project then ear is mixed
 							if(!isMixedEAR) {
 								isMixedEAR = isJavaEEFiveProject;
 							}
 						}
 						
 					}
 					
 					//should only continue validation if this is not an invalid mixed EAR
 					//isLegacyEAR check needed because of [224484] 5.0 EARs may get to this validator when they should not, need to protect against this
 					if(isLegacyEAR && !isMixedEAR) {
 						status = super.validateInJob(inHelper, inReporter);
 						validateModuleMaps(earModule);
 						validateManifests();
 						validateDuplicateClasspathComponentURIs(earModule);
 		//				validateUtilJarMaps(earEdit,earModule);
 		//				validateUriAlreadyExistsInEar(earEdit,earModule);
 		//				validateDocType(earEdit,earModule);
 					}
 				}
             }
 		return status;
 	}	
 
 	public ISchedulingRule getSchedulingRule(IValidationContext helper) {
 		ISchedulingRule combinedRule = null;
 		IProject project = ((IWorkbenchContext) helper).getProject();
 		IVirtualComponent comp = ComponentCore.createComponent( project );
 		IFile appDeploymentDescriptor = null;
 		if( comp != null ){
 			IVirtualFile vf = comp.getRootFolder().getFile(new Path(J2EEConstants.APPLICATION_DD_URI));
 			if( vf!= null ){
 				appDeploymentDescriptor = vf.getUnderlyingFile();
 				combinedRule = MultiRule.combine(appDeploymentDescriptor, combinedRule);
 			}
 			IVirtualReference[] refs = comp.getReferences();
 			for( int i=0; i< refs.length; i++ ){
 				IVirtualComponent refComp = refs[i].getReferencedComponent();
 				if( refComp != null && !refComp.isBinary() ){
 					String type = J2EEProjectUtilities.getJ2EEComponentType( refComp );
 					IVirtualFile refDDFile = null;
 					if( type.equals(IModuleConstants.JST_WEB_MODULE)){
 						refDDFile = refComp.getRootFolder().getFile(new Path(J2EEConstants.WEBAPP_DD_URI));
 					}else if ( type.equals(IModuleConstants.JST_CONNECTOR_MODULE)){
 						refDDFile = refComp.getRootFolder().getFile(new Path(J2EEConstants.RAR_DD_URI));
 					}else if( type.equals(IModuleConstants.JST_EJB_MODULE)){
 						refDDFile = refComp.getRootFolder().getFile(new Path(J2EEConstants.EJBJAR_DD_URI));
 					}else if( type.equals(IModuleConstants.JST_APPCLIENT_MODULE)){
 						refDDFile = refComp.getRootFolder().getFile(new Path(J2EEConstants.APPLICATION_DD_URI));
 					}
 					if( refDDFile!= null ){
 						IFile dd = refDDFile.getUnderlyingFile();
 						combinedRule = MultiRule.combine(dd, combinedRule);
 					}						
 				}
 			}
 			
 		}
 		return combinedRule;
 	}
 
 	public void validateDuplicateClasspathComponentURIs(final IVirtualComponent earComponent) {
 		if (earFile == null) {
 			return;
 		}
 		final Set moduleURIs = new HashSet();
 		final List archives = earFile.getArchiveFiles();
 		for (int i = 0; i < archives.size(); i++) {
 			final Archive anArchive = (Archive) archives.get(i);
 			moduleURIs.add(anArchive.getURI());
 		}
 
 		final Map archiveToPath = new HashMap();
 		final IVirtualReference[] components = earComponent.getReferences();
 		for (int i = 0; i < components.length; i++) {
 			IVirtualReference reference = components[i];
 			IVirtualComponent referencedComponent = reference.getReferencedComponent();
 
 			// retrieve all Java classpath component dependencies
 			if (referencedComponent instanceof J2EEModuleVirtualComponent) {
 				final IVirtualReference[] cpRefs = ((J2EEModuleVirtualComponent) referencedComponent).getJavaClasspathReferences();
 				for (int j = 0; j < cpRefs.length; j++) {
 					final IVirtualReference ref = cpRefs[j];
 					// only ../ runtime paths contribute to the EAR
 					if (ref.getRuntimePath().equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH)) {
 						String archiveName = ref.getArchiveName();
 						String[] params = {referencedComponent.getProject().getName(), archiveName};
 						if (moduleURIs.contains(archiveName)) {
 							String msg = NLS.bind(EARValidationMessageResourceHandler.CLASSPATH_COMPONENT_URI_MATCHES_ARCHIVE_URI, params);
 							addLocalizedError(msg, project);
 						} else {
 							IPath cpEntryPath= ClasspathDependencyUtil.getClasspathVirtualReferenceLocation(ref);
 							if (cpEntryPath != null) {
 								IPath existingPath = (IPath) archiveToPath.get(archiveName);
 								if (existingPath != null && !existingPath.equals(cpEntryPath)) {
 									String msg = NLS.bind(EARValidationMessageResourceHandler.DUPLICATE_CLASSPATH_COMPONENT_URI, params);
 									addLocalizedError(msg, project);
 								} else {
 									archiveToPath.put(archiveName, cpEntryPath);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void validateManifests() throws ValidationException {
 		if (earFile == null)
 			return;
 		List archives = earFile.getArchiveFiles();
 		for (int i = 0; i < archives.size(); i++) {
 
 			final Archive anArchive = (Archive) archives.get(i);
 			IFile target = getManifestFile(anArchive);
 			if (target != null)
 				_reporter.removeMessageSubset(this, target, MANIFEST_GROUP_NAME);
 			validateManifestCase(anArchive);
 			validateManifestLines(anArchive);
 			validateManifestClasspath(anArchive);
 		}
 	}
 
 	public void validateManifestCase(Archive anArchive) {
 		String mfuri = J2EEConstants.MANIFEST_URI;
 
 		//Indicates a manifest file with the valid name exists,
 		//nothing left to do
 		if (anArchive.containsFile(mfuri))
 			return;
 
 		//Otherwise iterate the list of files
 		//Ensure the archive is read-only first
 		anArchive.getOptions().setIsReadOnly(true);
 		List files = anArchive.getFiles();
 		String uri = null;
 		for (int i = 0; i < files.size(); i++) {
 			File aFile = (File) files.get(i);
 			uri = aFile.getURI();
 			if (mfuri.equalsIgnoreCase(uri) && !mfuri.equals(uri)) {
 				String[] params = {uri, anArchive.getURI()};
 				String msg = NLS.bind(EARValidationMessageResourceHandler.INVALID_CASE_FOR_MANIFEST_ERROR_, params);
 				addLocalizedError(msg, null);				
 			}
 		}
 
 	}
 
 
 	public void validateManifestClasspath(Archive anArchive) throws ValidationException {
 		ArchiveManifest manifest = null;
 		try{
 			manifest = anArchive.getManifest();
 		}catch( ManifestException mf){
 			//mf.printStackTrace();
 			mf.getMessage();
 			String[] args = new String[]{anArchive.getURI()};
 			String tmp = NLS.bind(EARValidationMessageResourceHandler.ERROR_READING_MANIFEST_ERROR_, args);
 			
 			addLocalizedError(tmp, args);
 		}
 		
 		if(manifest == null)
 			return;
 
 		String[] cp = manifest.getClassPathTokenized();
 		
 		for (int i = 0; i < cp.length; i++) {
 			String uri = ArchiveUtil.deriveEARRelativeURI(cp[i], anArchive);
 			if (uri == null) {
 				invalidClassPathEntryWarning(cp[i], anArchive);
 				continue;
 			}
 			File f = null;
 			//IFile rf = null;
 			try {
 //					if (uri.endsWith(J2EEImportConstants.IMPORTED_JAR_SUFFIX)) {
 						//TODO Needs work here to initialize rf as rf is an IFile and there is no way to get an IFile currently
 //					IVirtualResource resource = component.getRootFolder().findMember(new Path(uri));
 //						if (resource == null || !resource.exists()) {
 //							invalidClassPathEntryWarning(cp[i], uri, anArchive);
 //						}
 //					}
 //				 else
 					f = earFile.getFile(uri);
 			} catch (java.io.FileNotFoundException ex) {
 				invalidClassPathEntryWarning(cp[i], earFile.getURI(), anArchive);
 				continue;
 			}
 			if (f != null && f.isArchive() && anArchive.isModuleFile()) {
 				Archive archive = (Archive) f;
 				ModuleFile m = (ModuleFile) anArchive;
 				if (!ArchiveUtil.isValidDependency(archive, m))
 					invalidDepedencyWarning(cp[i], archive, m);
 			}
 		}
 	}
 
 
 	protected void validateManifestLines(Archive anArchive) throws ValidationException {
 		if (anArchive == null)
 			return;
 		InputStream is = null;
 		try {
 			is = anArchive.getInputStream(J2EEConstants.MANIFEST_URI);
 			ManifestLineValidator lineVal = new ManifestLineValidator(is);
 			lineVal.validate();
 			addErrorsIfNecessary(anArchive, lineVal);
 		} catch (FileNotFoundException ex) {
 			return;
 		} catch (IOException ex) {
 			handleManifestException(ex, anArchive);
 		} finally {
 			if (is != null)
 				try {
 					is.close();
 				} catch (IOException ex) {
 					handleManifestException(ex, anArchive);
 				}
 		}
 	}
 
 	protected void addErrorsIfNecessary(Archive anArchive, ManifestLineValidator mfVal) {
 		if (!mfVal.hasErrors())
 			return;
 		IFile target = getManifestFile(anArchive);
 		if (!mfVal.endsWithLineBreak())
 			addFileEndError(anArchive, mfVal, target);
 		int[] lines = mfVal.getLineNumbersExceedingLimit();
 		for (int i = 0; i < lines.length; i++) {
 			addLineLengthError(anArchive, target, lines[i]);
 		}
 	}
 
 	protected void addLineLengthError(Archive anArchive, IFile target, int lineNo) {
 		String[] args = new String[2];
 		args[0] = Integer.toString(lineNo);
 		args[1] = anArchive.getURI();
 		
 		String tmp = NLS.bind(EARValidationMessageResourceHandler.MANIFEST_LINE_EXCEEDS_LENGTH_ERROR_, args);
 		
 		if( lineNo >= 0 ){
 			addLocalizedError(tmp, target, MANIFEST_GROUP_NAME, lineNo );
 		}else{
 			addLocalizedError(tmp, target, MANIFEST_GROUP_NAME );
 		}
 
 	}
 
 	protected void addFileEndError(Archive anArchive, ManifestLineValidator mfVal, IFile target) {
 		String[] args = new String[]{anArchive.getURI()};
 		
 		String tmp = NLS.bind(EARValidationMessageResourceHandler.MANIFEST_LINE_END_ERROR_, args);
 		
 		if (target != null)
 			addLocalizedError(tmp, getManifestFile(anArchive), MANIFEST_GROUP_NAME, mfVal.getLineCount());
 		else
 			addLocalizedError(tmp, null, MANIFEST_GROUP_NAME);
 	}
 
 	protected void handleManifestException(IOException ex, Archive anArchive) throws ValidationException {
 		Logger.getLogger().logError(ex);
 		String tmp = NLS.bind(EARValidationMessageResourceHandler.ERROR_READING_MANIFEST_ERROR_, new String[]{anArchive.getURI()});		
 		IMessage message = new LocalizedMessage(IMessage.HIGH_SEVERITY, tmp);
 		throw new ValidationException(message, ex);
 	}
 
 	/**
 	 * Validates utiljar maps
 	 */
 //	public void validateUtilJarMaps(EARArtifactEdit edit, IVirtualComponent workbenchModule) {
 //		List utilJarModules = edit.getUtilityModuleReferences();
 //		if (!utilJarModules.isEmpty() || !utilJarModules.isEmpty()) {
 //			for (int i = 0; i < utilJarModules.size(); i++) {
 //				IVirtualComponent aUtilJar = ((IVirtualReference) utilJarModules.get(i)).getReferencedComponent();
 //				if (aUtilJar != null) {
 //					IProject project = J2EEPlugin.getWorkspace().getRoot().getProject(aUtilJar.getProject().getName());
 //					if (project != null) {
 //						if (!project.exists()) {
 //							String[] params = new String[]{project.getName(), aUtilJar.getRuntimePath().toString(), earHelper.getProject().getName()};
 //							addWarning(getBaseName(), PROJECT_DOES_NOT_EXIST_WARN_, params);
 //						} else {
 //							//validateModuleProjectForValidServerTarget(project);
 //							if (!project.isOpen()) {
 //								String[] params = new String[]{project.getName()};
 //								addWarning(getBaseName(), PROJECT_IS_CLOSED_WARN_, params);
 //							}
 //						}
 //					}
 //				}
 //			}
 //		} 
 //		validateDuplicateUtilJars(edit,workbenchModule);
 //		validateUtilJarNameCollision(edit,workbenchModule);
 //		validateUtilJarContainsNoSpaces(edit,workbenchModule);
 //		
 //	}// validateUtilJarMaps
 
 	/**
 	 * Checks if the util jar contains spaces or not.
 	 * 
 	 * @param EAREditModel
 	 *            earEditModel - The ear editmodel.
 	 */
 	protected void validateUtilJarContainsNoSpaces(EARArtifactEdit edit, IVirtualComponent module) {
 		IVirtualReference[] utilJars = edit.getUtilityModuleReferences();
 
 		if (utilJars == null)
 			return;
 
 		for (int i = 0; i < utilJars.length; i++) {
 			IVirtualReference utilModule = utilJars[i];
 			if (utilModule != null) {
 				String uri = ModuleURIUtil.fullyQualifyURI(project).toString();
 				if (uri != null && uri.indexOf(" ") != -1) { //$NON-NLS-1$
 					String[] params = new String[1];
 					params[0] = uri;
 					String tmp = NLS.bind(EARValidationMessageResourceHandler.URI_CONTAINS_SPACES_ERROR_, params);
 					addLocalizedError(tmp, appDD);
 				}// if
 			}// if
 		}// for
 
 	}// validateUtilJarContainsNoSpaces
 
 	/**
 	 * Validates if the a util jar has the same name as another module.
 	 * 
 	 * @param EAREditModel
 	 *            earEditModel - The ear editmodel.
 	 */
 //	protected void validateUtilJarNameCollision(EARArtifactEdit edit, IVirtualComponent module) {
 //		List utilJars = edit.getUtilityModuleReferences();
 //		if (utilJars == null)
 //			return;
 //		for (int i = 0; i < utilJars.size(); i++) {
 //			UtilityJARMapping utilModule = (UtilityJARMapping) utilJars.get(i);
 //
 //			if (utilModule != null) {
 //				if (edit.uriExists(utilModule.getUri())) {
 //
 //					String[] params = new String[]{utilModule.getUri(), module.getName()};
 //					addError(getBaseName(), MESSAGE_UTIL_URI_NAME_COLLISION_ERROR_, params);
 //
 //				} else if (utilModule.getProjectName() != null || utilModule.getProjectName().length() != 0) {
 //					if (edit.uriExists(utilModule.getUri())) {
 //						String[] params = new String[]{utilModule.getUri(), utilModule.getProjectName()};
 //						addError(getBaseName(), MESSAGE_UTIL_PROJECT_NAME_COLLISION_ERROR_, params);
 //					}
 //				}
 //			}
 //		}
 //	} 
 		
 
 
 	/**
 	 * validate is there are duplicate util jars.
 	 * 
 	 * @param EAREditModel
 	 *            earEditModel - The ear editmodel
 	 */
 	protected void validateDuplicateUtilJars(EARArtifactEdit edit, IVirtualComponent module) {
 		IVirtualReference[] utilJars = edit.getUtilityModuleReferences();
 		Set visitedUtilUri = new HashSet();
 		if (utilJars == null)
 			return;
 		for (int i = 0; i < utilJars.length; i++) {
 			IVirtualReference utilModule = utilJars[i];
 			if (utilModule != null) {
 				String uri = ModuleURIUtil.fullyQualifyURI(project).toString();
 				if (visitedUtilUri.contains(uri)) {
 					String compName = module.getName();
 					duplicateUtilError(module.getName(),uri, compName);
 				} else
 					visitedUtilUri.add(uri);
 			} // if
 		} // for
 	} // validateModuleMapsDuplicateUtil
 
 	/**
 	 * Creates an error for duplicate util jars.
 	 * 
 	 * @param String
 	 *            earProjectName - The ears project name.
 	 * @param String
 	 *            moduleUri - The modules uri.
 	 * @param String
 	 *            projectName - The project name.
 	 */
 	protected void duplicateUtilError(String earProjectName, String moduleUri, String projectName) {
 		String[] params = new String[3];
 		params[0] = projectName;
 		params[1] = earProjectName;
 		params[2] = moduleUri;
 		String tmp = NLS.bind(EARValidationMessageResourceHandler.DUPLICATE_UTILJAR_FOR_PROJECT_NAME_ERROR_, params);
 		
 		addLocalizedError(tmp, null);
 	}// duplicateUtilError
 
 	public void validateModuleMaps(IVirtualComponent component) {
 		IVirtualFile ddFile = component.getRootFolder().getFile(J2EEConstants.APPLICATION_DD_URI);
 		if( ddFile.exists()){
 			EList modules = appDD.getModules();
 			if (!modules.isEmpty()) {
 				EARArtifactEdit edit = null;
 				try{
 					edit = EARArtifactEdit.getEARArtifactEditForRead( component.getProject() );
 					
 					for (int i = 0; i < modules.size(); i++) {
 						Module module = (Module) modules.get(i);
 						String uri = module.getUri();
 						IVirtualComponent referencedComp = edit.getModuleByManifestURI( uri );
 						if( referencedComp == null ){
 							String[] params = new String[]{uri, component.getProject().getName()};
 							String tmp = NLS.bind(EARValidationMessageResourceHandler.MISSING_PROJECT_FORMODULE_WARN_, params);
 							
 							addLocalizedWarning(tmp, null);							
 						}
 						validateModuleURIExtension(module);
 					}
 				}finally{
 					if( edit != null )
 						edit.dispose();					
 				}
 			}
 		}
 	}
 
 
 	protected void validateModuleURIExtension(Module module) {
 		String newUri = module.getUri();
 		if (newUri != null && newUri.length() > 0) {
 			if (module instanceof EjbModule && !newUri.endsWith(".jar")) { //$NON-NLS-1$
 				String[] params = new String[1];
 				params[0] = module.getUri();
 				IResource target = earHelper.getProject().getFile(ArchiveConstants.APPLICATION_DD_URI);
 				String tmp = NLS.bind(EARValidationMessageResourceHandler.INVALID_URI_FOR_MODULE_ERROR_, params);
 				addLocalizedWarning(tmp, target);
 			} else if (module instanceof WebModule && !newUri.endsWith(".war")) { //$NON-NLS-1$
 				String[] params = new String[1];
 				params[0] = module.getUri();
 				IResource target = earHelper.getProject().getFile(ArchiveConstants.APPLICATION_DD_URI);
 				String tmp = NLS.bind(EARValidationMessageResourceHandler.INVALID_URI_FOR_MODULE_ERROR_, params);				
 				addLocalizedWarning(tmp, target);
 			}
 		}
 	}
 	
 
 	public void cleanup(IReporter reporter) {
 		if (earHelper != null)
 			earHelper.closeEARFile();
 		super.cleanup(reporter);
 	}
 
 	public void cleanup() {
 
 	}
 	
 	protected IFile getManifestFile(Archive anArchive) {
 		URIConverter conv = anArchive.getResourceSet().getURIConverter();
 		if (conv instanceof WorkbenchURIConverter) {
 			WorkbenchURIConverter wbConv = (WorkbenchURIConverter) conv;
 			IContainer input = wbConv.getInputContainer();
 			if (input == null)
 				return null;
 			IProject p = input.getProject();
 			if (p == null || JemProjectUtilities.isBinaryProject(p))
 				return null;
 			IFile result = J2EEProjectUtilities.getManifestFile(p, false);
 			if (result != null && result.exists())
 				return result;
 		}
 		return null;
 	}
 
 	/**
 	 * Checks if the nature is consistent with doc type.
 	 */
 	protected void validateDocType(EnterpriseArtifactEdit edit,IVirtualComponent module) {
 		if (edit == null)
 			return;
 		if (edit.getJ2EEVersion() >= J2EEVersionConstants.J2EE_1_3_ID && appDD.getVersionID() < J2EEVersionConstants.J2EE_1_3_ID) {
 			String[] params = new String[3];
 			params[0] = DOCTYPE_1_2;
 			params[1] = getResourceName();
 			params[2] = DOCTYPE_1_3;
 			String tmp = NLS.bind(EARValidationMessageResourceHandler.EAR_INVALID_DOC_TYPE_ERROR_, params);			
 			addLocalizedError(tmp, appDD);
 		} else if (edit.getJ2EEVersion() < J2EEVersionConstants.J2EE_1_3_ID && appDD.getVersionID() >= J2EEVersionConstants.J2EE_1_3_ID) {
 			String[] params = new String[3];
 			params[0] = DOCTYPE_1_3;
 			params[1] = getResourceName();
 			params[2] = DOCTYPE_1_2;
 			String tmp = NLS.bind(EARValidationMessageResourceHandler.EAR_INVALID_DOC_TYPE_ERROR_, params);			
 			addLocalizedError(tmp, appDD);
 		}
 	}
 
 	/**
 	 * Validates that conflicting jar does not exist in the ear project.
 	 */
 	public void validateUriAlreadyExistsInEar(EARArtifactEdit edit,IVirtualComponent component) {
 		IVirtualReference[] modules = edit.getJ2EEModuleReferences();
 		if (modules == null)
 			return;
 		for (int i = 0; i < modules.length; i++) {
 			IVirtualReference reference = modules[i];
 			IVirtualComponent module = reference.getReferencedComponent();
 			if (module != null && module.getRootFolder().getRuntimePath() != null) {
 				IProject currentEARProject = earHelper.getProject();
 				try {
 					IFile exFile = currentEARProject.getFile(module.getRootFolder().getRuntimePath());
 					if (exFile != null && exFile.exists()) {
 						String[] params = new String[2];
 						params[0] = module.getRootFolder().getRuntimePath().toString();
 						params[1] = currentEARProject.getName();
 						String tmp = NLS.bind(EARValidationMessageResourceHandler.URI_ALREADY_EXISTS_IN_EAR_WARN_, params);						
 						addLocalizedWarning(tmp, appDD);
 					}
 				} catch (IllegalArgumentException iae) {
 					Logger.getLogger().logError(iae);
 				}
 			}
 		}
 	}
 
 	protected void removeAllMessages(EObject eObject, String groupName) {
 		Resource res = eObject.eResource();
 		if(res != null) {
 			IFile file = WorkbenchResourceHelper.getFile(res);
 			if(file != null)
 				_reporter.removeMessageSubset(this,file,groupName);
 		}
 	}
 	
 }// UIEarValidator
