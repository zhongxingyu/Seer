 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.common.operations;
 
 import java.lang.reflect.Modifier;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaConventions;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.j2ee.internal.common.J2EECommonMessages;
 import org.eclipse.jst.j2ee.internal.common.operations.JavaModelUtil;
 import org.eclipse.jst.j2ee.internal.common.operations.NewJavaClassOperation;
 import org.eclipse.wst.common.frameworks.internal.operations.ProjectCreationDataModel;
 import org.eclipse.wst.common.frameworks.operations.WTPOperation;
 import org.eclipse.wst.common.internal.emfworkbench.operation.EditModelOperationDataModel;
 import org.eclispe.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 import com.ibm.wtp.common.logger.proxy.Logger;
 import com.ibm.wtp.emf.workbench.ProjectUtilities;
 
 /**
  * This data model is a subclass of WTPOperationDataModel and follows the WTP Operation and WTP Wizard frameworks.
  * @see org.eclipse.wst.common.frameworks.operations.WTPOperationDataModel
  * 
  * This data model extends the EditModelOperationDataModel to get project name and edit model ID so
  * that during the operation, the edit model can be used to save changes.
  * @see org.eclipse.wst.common.internal.emfworkbench.operation.EditModelOperationDataModel
  * 
  * The NewJavaClassDataModel is used to store all the base properties which would be needed to generate
  * a new instance of a java class.  Validations for these properties such as class name, package name,
  * superclass, and modifiers are also provided.  
  * 
  * Clients must subclass this data model to use it and to cache and provide their own specific attributes.  They should also provide their
  * own validation methods and default values for the properties they add.
  * 
  * The use of this class is EXPERIMENTAL and is subject to substantial changes.
  */
 public class NewJavaClassDataModel extends EditModelOperationDataModel {
 
 	/**
 	 * Required, String property used to set the unqualified java class name for the new java class.
 	 */
 	public static final String CLASS_NAME = "NewJavaClassDataModel.CLASS_NAME"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, String property used to set the source folder location for the new java class.  The first
 	 * source folder found in the project will be used if one is not specified.
 	 */
 	public static final String SOURCE_FOLDER = "NewJavaClassDataModel.SOURCE_FOLDER"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, String property used to to set the java package for the new java class.  The default
 	 * package is used if one is not specified.
 	 */
 	public static final String JAVA_PACKAGE = "NewJavaClassDataModel.JAVA_PACKAGE"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, String property used to set the qualified java class name of the superclass of the
 	 * new java class.
 	 */
 	public static final String SUPERCLASS = "NewJavaClassDataModel.SUPERCLASS"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to set the visibility of the new java class. This is true
 	 * by default.
 	 */
 	public static final String MODIFIER_PUBLIC = "NewJavaClassDataModel.MODIFIER_PUBLIC"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to set whether the new java class is abstract.  This is false
 	 * by default.
 	 */
 	public static final String MODIFIER_ABSTRACT = "NewJavaClassDataModel.MODIFIER_ABSTRACT"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to set whether the new java class is declared final.  This is false
 	 * by default.
 	 */
 	public static final String MODIFIER_FINAL = "NewJavaClassDataModel.MODIFIER_FINAL"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, List property of all the qualified names of interfaces the new java class should implement.
 	 */
 	public static final String INTERFACES = "NewJavaClassDataModel.INTERFACES"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to set whether the new java class should generate a main method.  This
 	 * is false by default.
 	 */
 	public static final String MAIN_METHOD = "NewJavaClassDataModel.MAIN_METHOD"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to set whether or not the constructor from the superclass should be
 	 * generated in the new java class.  The default value is true.
 	 */
 	public static final String CONSTRUCTOR = "NewJavaClassDataModel.CONSTRUCTOR"; //$NON-NLS-1$
 	
 	/**
 	 * Optional, boolean property used to set whether the new java class should add method stubs for unimplemented
 	 * methods defined in the interfaces of the interface list.  This is true by default.
 	 */
 	public static final String ABSTRACT_METHODS = "NewJavaClassDataModel.ABSTRACT_METHODS"; //$NON-NLS-1$
 	
 	/**
 	 * Subclasses may extend this method to perform their own validation.  This method should
 	 * not return null.  It does not accept null as a parameter.
 	 * @see NewJavaClassDataModel#doValidateProperty(String)
 	 * 
 	 * @param folderFullPath
 	 * @return IStatus
 	 */
 	protected IStatus validateJavaSourceFolder(String folderFullPath) {
 		IProject project = getTargetProject();
 		// Ensure project is not closed
 		if (project == null) {
 			String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_FOLDER_NOT_EXIST);
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Ensure project is accessible.
 		if (!project.isAccessible()) {
 			String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_FOLDER_NOT_EXIST);
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Ensure the project is a java project.
 		try {
 			if (!project.hasNature(JavaCore.NATURE_ID)) {
 				String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_NOT_JAVA_PROJECT);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		} catch (CoreException e) {
 			Logger.getLogger().log(e);
 		}
 		IFolder sourcefolder = getJavaSourceFolder();
 		// Ensure the selected folder is a valid java source folder for the project
 		if (sourcefolder == null || (sourcefolder != null && !sourcefolder.getFullPath().equals(new Path(folderFullPath)))) {
 			String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_FOLDER_NOT_SOURCE, new String[]{folderFullPath});
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Valid source is selected
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * Subclasses may extend this method to perform their own retrieval of a default java source folder.
 	 * This implementation returns the output folder if it is also a source folder, otherwise it returns
 	 * the first source folder in the project.  This method may return null.
 	 * 
 	 * @return IFolder instance of default java source folder
 	 */
 	protected IFolder getDefaultJavaSourceFolder() {
 		IProject project = getTargetProject();
 		if (project == null)
 			return null;
 		IContainer output = ProjectUtilities.getJavaProjectOutputContainer(project);
 		List sources = ProjectUtilities.getSourceContainers(project);
 		//TODO: We need to be able to support the project as the source, but this would be a breaking change
 		if (sources == null || sources.isEmpty() || ((IContainer) sources.get(0)).getType() != IResource.FOLDER)
 			return null;
 		// If the source folder and output folder are the same, return that folder
 		if (output != null && sources.contains(output))
 			return (IFolder) output;
 		// Otherwise, return the first source folder
 		return (IFolder) sources.get(0);
 	}
 	
 	/**
 	 * Subclasses may extend this method to create their own specialized default WTP operation.
 	 * This implementation creates an instance of the NewJavaClassOperation to create a new
 	 * java class.  This method will not return null.
 	 * @see WTPOperationDataModel#getDefaultOperation()
 	 * @see NewJavaClassOperation
 	 * 
 	 * @return WTPOperation
 	 */
 	public WTPOperation getDefaultOperation() {
 		return new NewJavaClassOperation(this);
 	}
 
 	/**
 	 * Subclasses may extend this method to perform their own retrieval mechanism.
 	 * This implementation simply returns the JDT package fragment root for the selected source
 	 * folder.  This method may return null.
 	 * @see IJavaProject#getPackageFragmentRoot(org.eclipse.core.resources.IResource)
 	 * 
 	 * @return IPackageFragmentRoot
 	 */
 	public IPackageFragmentRoot getJavaPackageFragmentRoot() {
 		IProject project = getTargetProject();
 		IJavaProject aJavaProject = ProjectUtilities.getJavaProject(project);
 		// Return the source folder for the java project of the selected project
 		if (aJavaProject != null) {
 			IFolder sourcefolder = getJavaSourceFolder();
 			if (sourcefolder != null)
 				return aJavaProject.getPackageFragmentRoot(sourcefolder);
 		}
 		return null;
 	}
 
 	/**
 	 * Subclasses may extend this method to add their own data model's properties as valid base properties.
 	 * @see org.eclipse.wst.common.frameworks.operations.WTPOperationDataModel#initValidBaseProperties()
 	 */
 	protected void initValidBaseProperties() {
 		super.initValidBaseProperties();
 		addValidBaseProperty(SOURCE_FOLDER);
 		addValidBaseProperty(JAVA_PACKAGE);
 		addValidBaseProperty(CLASS_NAME);
 		addValidBaseProperty(SUPERCLASS);
 		addValidBaseProperty(MODIFIER_PUBLIC);
 		addValidBaseProperty(MODIFIER_ABSTRACT);
 		addValidBaseProperty(MODIFIER_FINAL);
 		addValidBaseProperty(INTERFACES);
 		addValidBaseProperty(MAIN_METHOD);
 		addValidBaseProperty(CONSTRUCTOR);
 		addValidBaseProperty(ABSTRACT_METHODS);
 	}
 
 	/**
 	 * Subclasses may extend this method to add the default values for their own specific data
 	 * model properties.  This declares the default values for the new java class.
 	 * This method does not accept null.  It may return null.
 	 * @see WTPOperationDataModel#getDefaultProperty(String)
 	 * 
 	 * @param propertyName
 	 * @return default object value of the property
 	 */
 	protected Object getDefaultProperty(String propertyName) {
 		// Get the default source folder for the project
 		if (propertyName.equals(SOURCE_FOLDER)) {
 			IFolder sourceFolder = getDefaultJavaSourceFolder();
 			if (sourceFolder != null && sourceFolder.exists())
 				return sourceFolder.getFullPath().toOSString();
 		}
 		// Use Object as the default superclass if one is not specified
 		if (propertyName.equals(SUPERCLASS)) {
 			return new String("java.lang.Object"); //$NON-NLS-1$
 		}
 		// Use public as default visibility
 		if (propertyName.equals(MODIFIER_PUBLIC)) {
 			return new Boolean(true);
 		}
 		// Generate constructors from the superclass by default
 		if (propertyName.equals(CONSTRUCTOR)) {
 			return new Boolean(true);
 		}
 		// Generate unimplemented methods from declared interfaces by default
 		if (propertyName.equals(ABSTRACT_METHODS)) {
 			return new Boolean(true);
 		}
 		return super.getDefaultProperty(propertyName);
 	}
 
 	/**
 	 * Subclasses may override this method to provide their own validation of any of the data model's
 	 * properties.  This implementation ensures that a java class can be properly generated from
 	 * the values as specified.
 	 * This method will not return null.  This method will not accept null as a parameter.
 	 * @see WTPOperationDataModel#doValidateProperty(java.lang.String)
 	 * 
 	 * @param propertyName
 	 * @return IStatus of the validity of the specifiec property
 	 */
 	protected IStatus doValidateProperty(String propertyName) {
 		if (propertyName.equals(SOURCE_FOLDER))
 			return validateFolder(getStringProperty(propertyName));
 		if (propertyName.equals(JAVA_PACKAGE))
 			return validateJavaPackage(getStringProperty(propertyName));
 		if (propertyName.equals(CLASS_NAME))
 			return validateJavaClassName(getStringProperty(propertyName));
 		if (propertyName.equals(SUPERCLASS))
 			return validateSuperclass(getStringProperty(propertyName));
 		if (propertyName.equals(MODIFIER_ABSTRACT) || propertyName.equals(MODIFIER_FINAL))
 			return validateModifier(propertyName,getBooleanProperty(propertyName));
 		return super.doValidateProperty(propertyName);
 	}
 
 	/**
 	 * This method will ensure the source folder is not empty and forward the validation
 	 * of the source folder.  Subclasses may override the forwarded method.
 	 * This method does accept null.  It will not return null.
 	 * @see NewJavaClassDataModel#validateJavaSourceFolder(String)
 	 * @see NewJavaClassDataModel#doValidateProperty(String)
 	 * 
 	 * @param folderFullPath
 	 * @return IStatus
 	 */
 	private IStatus validateFolder(String folderFullPath) {
 		// Ensure that the source folder path is not empty
 		if (folderFullPath == null || folderFullPath.length() == 0) {
 			String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_FOLDER_NAME_EMPTY);
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Validate the java source folder
 		return validateJavaSourceFolder(folderFullPath);
 	}
 
 	/**
 	 * This method will validate whether the specified package name is a valid java package name.  It will
 	 * be called during the doValidateProperty(String).
 	 * This method will accept null.  It will not return null.
 	 * @see NewJavaClassDataModel#doValidateProperty(String)
 	 * 
 	 * @param packName -- the package name
 	 * @return IStatus of if the package name is valid
 	 */
 	private IStatus validateJavaPackage(String packName) {
 		if (packName != null && packName.trim().length() > 0) {
 			// Use standard java conventions to validate the package name
 			IStatus javaStatus = JavaConventions.validatePackageName(packName);
 			if (javaStatus.getSeverity() == IStatus.ERROR) {
 				String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_PACAKGE_NAME_INVALID) + javaStatus.getMessage();
 				return WTPCommonPlugin.createErrorStatus(msg);
 			} else if (javaStatus.getSeverity() == IStatus.WARNING) {
 				String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_PACKAGE_NAME_WARNING) + javaStatus.getMessage();
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		}
 		// java package name is valid
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * Subclasses may override this method to provide their own validation of the class name.
 	 * This implementation will verify if the specified class name is a valid UNQUALIFIED java class name.
 	 * This method will not accept null.  It will not return null.
 	 * @see NewJavaClassDataModel#doValidateProperty(String)
 	 * 
 	 * @param className
 	 * @return IStatus of if java class name is valid
 	 */
 	protected IStatus validateJavaClassName(String className) {
 		// Do not allow qualified name
 		if (className.lastIndexOf('.') != -1) {
 			String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_NAME_QUALIFIED);
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Check Java class name by standard java conventions
 		IStatus javaStatus = JavaConventions.validateJavaTypeName(className);
 		if (javaStatus.getSeverity() == IStatus.ERROR) {
 			String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_NAME_INVALID) + javaStatus.getMessage();
 			return WTPCommonPlugin.createErrorStatus(msg);
 		} else if (javaStatus.getSeverity() == IStatus.WARNING) {
 			String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_NAME_WARNING) + javaStatus.getMessage();
 			return WTPCommonPlugin.createWarningStatus(msg);
 		}
 		// Make sure the class does not already exist
 		else if (findTypeInClasspath(getQualifiedClassName())!=null) {
 		 	String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_NAME_EXIST);
 		 	return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		
 		// The java class name is valid
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * This method will verify the specified superclass can be subclassed.  It ensures the
 	 * superclass is a valid java class, that it exists, and that it is not final.
 	 * This method will accept null.  It will not return null.
 	 * @see NewJavaClassDataModel#doValidateProperty(String)
 	 * 
 	 * @param superclassName
 	 * @return IStatus of if the superclass can be subclassed
 	 */
 	private IStatus validateSuperclass(String superclassName) {
 		// Ensure the superclass name is not empty
 		if (superclassName == null || superclassName.trim().length() == 0) {
 			String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_NAME_EMPTY);
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Ensure the unqualified java class name of the superclass is valid
 		String className = superclassName;
 		int index = superclassName.lastIndexOf("."); //$NON-NLS-1$
 		if (index != -1) {
 			className = superclassName.substring(index + 1);
 		}
 		IStatus status = validateJavaClassName(className);
 		// If unqualified super class name is valid, ensure validity of superclass itself
 		if (status.isOK()) {
 			// Ensure the superclass exists
 			IType superClassType = findTypeInClasspath(superclassName);
 			if (superClassType == null) {
 				String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_SUPERCLASS_NOT_EXIST);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 			// Ensure the superclass is not final
 			int flags = -1;
 			try {
 				flags = superClassType.getFlags();
 			} catch (JavaModelException e) {
 				Logger.getLogger().log(e);
 			}
 			if (Modifier.isFinal(flags)) {
 				String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_SUPERCLASS_FINAL);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		}
 		// Return status of specified superclass
 		return status;
 	}
 
 	/**
 	 * This method will ensure that if the abstract modifier is set, that final is not set,
 	 * and vice-versa, as this is not supported either way.
 	 * This method will not accept null.  It will not return null.
 	 * @see NewJavaClassDataModel#doValidateProperty(String)
 	 * 
 	 * @param prop
 	 * @return IStatus of whether abstract value is valid
 	 */
 	private IStatus validateModifier(String propertyName, boolean prop) {
 		// Throw an error if both Abstract and Final are checked
 		if (prop) {
 			// Ensure final is not also checked
 			if (propertyName.equals(MODIFIER_ABSTRACT) && getBooleanProperty(MODIFIER_FINAL)) {
 				String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_BOTH_FINAL_AND_ABSTRACT);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 			// Ensure abstract is not also checked
 			if (propertyName.equals(MODIFIER_FINAL) && getBooleanProperty(MODIFIER_ABSTRACT)) {
 				String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_BOTH_FINAL_AND_ABSTRACT);
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		}
 		// Abstract and final settings are valid
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * This method will return the qualified java class name as specified by the class name
 	 * and package name properties in the data model.
 	 * This method should not return null.
 	 * @see NewJavaClassDataModel#CLASS_NAME
 	 * @see NewJavaClassDataModel#JAVA_PACKAGE
 	 * 
 	 * @return String qualified java classname
 	 */
 	public final String getQualifiedClassName() {
 		// Use the java package name and unqualified class name to create a qualified java class name
 		String packageName = getStringProperty(JAVA_PACKAGE);
 		String className = getStringProperty(CLASS_NAME);
 		//Ensure the class is not in the default package before adding package name to qualified name
 		if (packageName != null && packageName.trim().length() > 0)
 			return packageName + "." + className; //$NON-NLS-1$
 		return className;
 	}
 
 	/**
 	 * This method is intended for internal use only.  This will check the java model for the specified
 	 * javaproject in the data model for the existence of the passed in qualified classname.
 	 * This method does not accept null.  It may return null.
 	 * @see NewJavaClassDataModel#getTargetProject()
 	 * @see JavaModelUtil#findType(IJavaProject, String)
 	 * 
 	 * @param fullClassName
 	 * @return IType for the specified classname
 	 */
 	private IType findTypeInClasspath(String fullClassName) {
 		// Retrieve the java project for the cached project
 		IJavaProject javaProject = ProjectUtilities.getJavaProject(getTargetProject());
 		try {
 			//Use the java model to try and find the IType for the qualified class name
 			IType type = JavaModelUtil.findType(javaProject, fullClassName);
 			return type;
 		} catch (JavaModelException e) {
 			Logger.getLogger().log(e);
 		}
 		return null;
 	}
 
 	/**
 	 * This will return the IFolder instance for the specified folder name in the data model.
 	 * This method may return null.
 	 * @see NewJavaClassDataModel#SOURCE_FOLDER
 	 * @see NewJavaClassDataModel#getAllSourceFolders()
 	 * 
 	 * @return IFolder java source folder
 	 */
 	protected final IFolder getJavaSourceFolder() {
 		List sources = getAllSourceFolders();
 		//Ensure there is valid source folder(s)
 		if (sources == null || sources.isEmpty() || ((IContainer) sources.get(0)).getType() != IResource.FOLDER)
 			return null;
 		String folderFullPath = getStringProperty(SOURCE_FOLDER);
 		// Get the source folder whose path matches the source folder name value in the data model
 		for (int i = 0; i < sources.size(); i++) {
 			IFolder folder = (IFolder) sources.get(i);
 			if (folder.getFullPath().equals(new Path(folderFullPath))) {
 				return folder;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method is intended for internal use only.  This will retrieve the list of
 	 * all source folders in the target project as specified in the data model.
 	 * This method may return null.
 	 * @see NewJavaClassDataModel#getTargetProject()
 	 * @see ProjectUtilities#getSourceContainers(org.eclipse.core.resources.IProject)
 	 * 
 	 * @return List of all source folders in the target project
 	 */
 	private List getAllSourceFolders() {
 		// Retrieve target project
 		IProject project = getTargetProject();
 		// If project is null, return null
 		if (project == null)
 			return null;
 		// Return all source containers in the specified project
 		List sources = ProjectUtilities.getSourceContainers(project);
 		return sources;
 	}
 	
 	/**
 	 * This method will return the target project for the new java class based on the project name set
 	 * in the data model.  It uses static API on ProjectCreationDataModel.
 	 * This method may return null.
 	 * @see NewJavaClassDataModel#PROJECT_NAME
 	 * @see ProjectCreationDataModel#getProjectHandleFromProjectName(String)
 	 * 
 	 * @return IProject target project
 	 */
 	public final IProject getTargetProject() {
 		return ProjectCreationDataModel.getProjectHandleFromProjectName(getStringProperty(PROJECT_NAME));
 	}
 }
