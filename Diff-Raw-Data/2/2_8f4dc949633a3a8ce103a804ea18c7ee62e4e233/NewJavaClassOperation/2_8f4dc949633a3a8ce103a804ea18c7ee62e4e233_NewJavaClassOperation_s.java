 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.common.operations;
 
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.ABSTRACT_METHODS;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.CLASS_NAME;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.CONSTRUCTOR;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.INTERFACES;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.JAVA_PACKAGE;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.JAVA_PACKAGE_FRAGMENT_ROOT;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.MODIFIER_ABSTRACT;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.MODIFIER_FINAL;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.MODIFIER_PUBLIC;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.SOURCE_FOLDER;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.SUPERCLASS;
 
 import java.io.ByteArrayInputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.operations.AbstractOperation;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.Flags;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.Signature;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.wst.common.componentcore.internal.operation.IArtifactEditOperationDataModelProperties;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 /**
  * NewJavaClassOperation is a data model operation that is used to create a default instance of a new java class
  * based on the input and properties set in the NewJavaClassDataModelProvider.  
  * @see org.eclipse.jst.j2ee.internal.common.operations.NewJavaClassDataModelProvider
  * 
  * It is a subclass of ArtifactEditProviderOperation and clients can invoke this operation as is or it may be subclassed to
  * add additional or modify behaviour.  The execute() method can be extended to drive this behaviour.
  * @see org.eclipse.wst.common.componentcore.internal.operation.ArtifactEditProviderOperation
  * 
  * The new java class is generated through the use of adding a series of static tokens defined within to
  * an ongoing string buffer.
  * 
  * The use of this class is EXPERIMENTAL and is subject to substantial changes.
  * 
  * This needs to be removed as it is legacy inherited from another team
  */
 public class NewJavaClassOperation extends AbstractDataModelOperation {
 
 	// Tokens for string buffer creation of default java class
 	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
 	protected static final String TAB = "\t"; //$NON-NLS-1$
 	protected static final String SPACE = " "; //$NON-NLS-1$
 	protected static final String DOT = "."; //$NON-NLS-1$
 	protected static final String COMMA = ","; //$NON-NLS-1$
 	protected static final String SEMICOLON = ";"; //$NON-NLS-1$
 	protected static final String POUND = "#"; //$NON-NLS-1$
 	protected static final String OPEN_PAR = "("; //$NON-NLS-1$
 	protected static final String CLOSE_PAR = ")"; //$NON-NLS-1$
 	protected static final String OPEN_BRA = "{"; //$NON-NLS-1$
 	protected static final String CLOSE_BRA = "}"; //$NON-NLS-1$
 	protected static final String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
 
 	protected static final String JAVA_LANG_OBJECT = "java.lang.Object"; //$NON-NLS-1$
 	protected static final String PACKAGE = "package "; //$NON-NLS-1$
 	protected static final String CLASS = "class "; //$NON-NLS-1$
 	protected static final String IMPORT = "import "; //$NON-NLS-1$
 	protected static final String EXTENDS = "extends "; //$NON-NLS-1$
 	protected static final String IMPLEMENTS = "implements "; //$NON-NLS-1$
 	protected static final String THROWS = "throws "; //$NON-NLS-1$
 	protected static final String SUPER = "super"; //$NON-NLS-1$
 	protected static final String PUBLIC = "public "; //$NON-NLS-1$
 	protected static final String PROTECTED = "protected "; //$NON-NLS-1$
 	protected static final String PRIVATE = "private "; //$NON-NLS-1$
 	protected static final String STATIC = "static "; //$NON-NLS-1$
 	protected static final String ABSTRACT = "abstract "; //$NON-NLS-1$
 	protected static final String FINAL = "final "; //$NON-NLS-1$
 	protected static final String VOID = "void"; //$NON-NLS-1$
 	protected static final String INT = "int"; //$NON-NLS-1$
 	protected static final String BOOLEAN = "boolean"; //$NON-NLS-1$
 	protected static final String MAIN_METHOD = "\tpublic static void main(String[] args) {"; //$NON-NLS-1$
 	protected static final String TODO_COMMENT = "\t\t// TODO Auto-generated method stub"; //$NON-NLS-1$
 	protected static final String RETURN_NULL = "\t\treturn null;"; //$NON-NLS-1$
 	protected static final String RETURN_0 = "\t\treturn 0;"; //$NON-NLS-1$
 	protected static final String RETURN_FALSE = "\t\treturn false;"; //$NON-NLS-1$
 
 	/**
 	 * This is a list of all the calculated import statements that will need to be added
 	 */
 	private List importStatements;
 
 	/**
 	 * This is a NewJavaClassOperation constructor.  Data models passed in should be instances
 	 * of NewJavaClassDataModel.  This method does not accept null.  It will not return null.
 	 * @see NewJavaClassDataModelProvider
 	 * 
 	 * @param dataModel
 	 * @return NewJavaClassOperation
 	 */
 	public NewJavaClassOperation(IDataModel dataModel) {
 		super(dataModel);
 		importStatements = new ArrayList();
 	}
 
 	/**
 	 * Subclasses may extend this method to add their own actions during execution.
 	 * The implementation of the execute method drives the running of the operation.  This
 	 * method will create the source folder, the java package, and then create the java file.
 	 * This method will accept null.
 	 * @see AbstractOperation#execute(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
 	 * 
 	 * @param monitor ProgressMonitor
 	 * @throws CoreException
 	 * @throws InterruptedException
 	 * @throws InvocationTargetException
 	 */
 	public IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		// Ensure source folder exists
 		IFolder sourceFolder = createJavaSourceFolder();
 		// Ensure java package exists
 		IPackageFragment pack = createJavaPackage();
 		// Create java class
 		createJavaFile(sourceFolder, pack);
 		return OK_STATUS;
 	}
 
 	/**
 	 * This method will return the java source folder as specified in the java class data model. 
 	 * It will create the java source folder if it does not exist.  This method may return null.
 	 * @see #SOURCE_FOLDER
 	 * @see IFolder#create(boolean, boolean, org.eclipse.core.runtime.IProgressMonitor)
 	 * 
 	 * @return IFolder the java source folder
 	 */
 	protected final IFolder createJavaSourceFolder() {
 		// Get the source folder name from the data model
 		String folderFullPath = model.getStringProperty(SOURCE_FOLDER);
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IFolder folder = root.getFolder(new Path(folderFullPath));
 		// If folder does not exist, create the folder with the specified path
 		if (!folder.exists()) {
 			try {
 				folder.create(true, true, null);
 			} catch (CoreException e) {
 				Logger.getLogger().log(e);
 			}
 		}
 		// Return the source folder
 		return folder;
 	}
 
 	/**
 	 * This method will return the java package as specified by the new java class data model.
 	 * If the package does not exist, it will create the package.  This method should not return
 	 * null.
 	 * @see #JAVA_PACKAGE
 	 * @see IPackageFragmentRoot#createPackageFragment(java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
 	 * 
 	 * @return IPackageFragment the java package
 	 */
 	protected final IPackageFragment createJavaPackage() {
 		// Retrieve the package name from the java class data model
 		String packageName = model.getStringProperty(JAVA_PACKAGE);
 		IPackageFragmentRoot packRoot = (IPackageFragmentRoot) model.getProperty(JAVA_PACKAGE_FRAGMENT_ROOT);
 		IPackageFragment pack =	packRoot.getPackageFragment(packageName);
 		// Handle default package
 		if (pack == null) {
 			pack = packRoot.getPackageFragment(""); //$NON-NLS-1$
 		}
 		// Create the package fragment if it does not exist
 		if (!pack.exists()) {
 			String packName = pack.getElementName();
 			try {
 				pack = packRoot.createPackageFragment(packName, true, null);
 			} catch (JavaModelException e) {
 				Logger.getLogger().log(e);
 			}
 		}
 		// Return the package
 		return pack;
 	}
 
 	/**
 	 * Subclasses may extend this method to provide their own java file creation path.
 	 * This implementation will use the properties specified in the data model to create
 	 * a default java class.  The class will be built using pre-defined tokens and will be
 	 * built up using a string buffer.  The method getJavaFileContent will handle the building
 	 * of the string buffer while this method will write those contents to the file.
 	 * This method does not accept null parameters.
 	 * @see #CLASS_NAME
 	 * @see NewJavaClassOperation#getJavaFileContent(IPackageFragment, String)
 	 * 
 	 * @param sourceFolder
 	 * @param pack
 	 */
 	protected void createJavaFile(IFolder sourceFolder, IPackageFragment pack) {
 		// Retrieve properties from the java class data model
 		String packageName = model.getStringProperty(JAVA_PACKAGE);
 		String className = model.getStringProperty(CLASS_NAME);
 		String fileName = className + ".java"; //$NON-NLS-1$
 		//ICompilationUnit cu = null;
 		try {
 			// Get the java file content from the string buffer
 			String content = getJavaFileContent(pack, className);
 			// Create the compilation unit
 			pack.createCompilationUnit(fileName, content, true, null);
 			byte[] contentBytes = content.getBytes();
 			IPath packageFullPath = new Path(packageName.replace('.', IPath.SEPARATOR));
 			IPath javaFileFullPath = packageFullPath.append(fileName);
 			IFile file = sourceFolder.getFile(javaFileFullPath);
 			// Set the contents in the file if it already exists
 			if (file != null && file.exists()) {
 				file.setContents(new ByteArrayInputStream(contentBytes), false, true, null);
 			} // If the file does not exist, create it with the contents
 			else if (file != null) {
 				file.create(new ByteArrayInputStream(contentBytes), false, null);
 			}
 			// editModel.getWorkingCopy(cu, true); //Track CU.
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * This is intended for internal use only.  This is where the string buffer for the contents
 	 * of the java file is built up using the properties in the data model and the predefined tokens.
 	 * This method will not accept null parameters. It will not return null.
 	 * @see NewJavaClassDataModelProvider
 	 * @see NewJavaClassOperation#createJavaFile(IFolder, IPackageFragment)
 	 * 
 	 * @param pack
 	 * @param className
 	 * @return String java file contents
 	 */
 	private String getJavaFileContent(IPackageFragment pack, String className) {
 		// Create the superclass name
 		String superclassName = model.getStringProperty(SUPERCLASS);
 		List interfaces = (List) model.getProperty(INTERFACES);
 		String packageStatement = getPackageStatement(pack);
 		// Create the import statements
 		setupImportStatements(pack, superclassName, interfaces);
 		// Create the class declaration
 		String classDeclaration = getClassDeclaration(superclassName, className, interfaces);
 		// Create the fields
 		String fields = getFields();
 		// Create the methods
 		String methods = getMethodStubs(superclassName, className);
 
 		StringBuffer contents = new StringBuffer();
 		// Add the package statement to the buffer
 		contents.append(packageStatement);
 		// Add all the import statements to the buffer
 		for (int i = 0; i < importStatements.size(); i++) {
 			contents.append(IMPORT + importStatements.get(i) + SEMICOLON);
 			contents.append(lineSeparator);
 		}
 		contents.append(lineSeparator);
 		// Add the class declaration to the buffer
 		contents.append(classDeclaration);
 		// Add the fields to the buffer
 		contents.append(fields);
 		// Add the method bodies to the buffer
 		contents.append(methods);
 		contents.append(CLOSE_BRA);
 		// Return the contents of the buffer
 		return contents.toString();
 	}
 
 	/**
 	 * This is intended for internal use only.  This method will return a package string for
 	 * the class.  It will not accept a null parameter.  It will not return null.
 	 * @see NewJavaClassOperation#getJavaFileContent(IPackageFragment, String)
 	 * 
 	 * @param pack
 	 * @return String package statement
 	 */
 	private String getPackageStatement(IPackageFragment pack) {
 		StringBuffer sb = new StringBuffer();
 		// If it is not default package, add package statement
 		if (!pack.isDefaultPackage()) {
 			sb.append(PACKAGE + pack.getElementName() + SEMICOLON);
 			sb.append(lineSeparator);
 			sb.append(lineSeparator);
 		}
 		// Return contents of buffer
 		return sb.toString();
 	}
 
 	/**
 	 * This method is intended for internal use.  It checks to see if the qualified class name
 	 * belongs to the specified package. This method will not accept nulls.  It will not return null.
 	 * @see NewJavaClassOperation#setupImportStatements(IPackageFragment, String, List)
 	 *
 	 * @param packageFragment
 	 * @param className
 	 * @return boolean is class in this package?
 	 */
 	private boolean isSamePackage(IPackageFragment packageFragment, String className) {
 		if (className != null && className.length() > 0) {
 			String sPackageName = packageFragment.getElementName();
 			String classPackageName = Signature.getQualifier(className);
 			// Does the qualified class's package name match the passed in package's name?
 			if (classPackageName.equals(sPackageName))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * This method is intended for internal use only.  This method will set up the required import
 	 * statements and cache to the importStatements list.
 	 * This method does not accept null parameters.
 	 * @see NewJavaClassOperation#importStatements
 	 * @see NewJavaClassOperation#getJavaFileContent(IPackageFragment, String)
 	 * 
 	 * @param pack
 	 * @param superclassName
 	 * @param interfaces
 	 */
 	private void setupImportStatements(IPackageFragment pack, String superclassName, List interfaces) {
 		// If there is a superclass and it is not in the same package, add an import for it
 		if (superclassName != null && superclassName.length() > 0) {
 			if (!superclassName.equals(JAVA_LANG_OBJECT) && !isSamePackage(pack, superclassName)) {
 				importStatements.add(superclassName);
 			}
 		}
 		// Add an import the list of implemented interfaces
 		if (interfaces != null && interfaces.size() > 0) {
 			int size = interfaces.size();
 			for (int i = 0; i < size; i++) {
 				String interfaceName = (String) interfaces.get(i);
 				if(!interfaceName.equals(JAVA_LANG_OBJECT) && !isSamePackage(pack, interfaceName)){
 					importStatements.add(interfaceName);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This class is intended for internal use only.  This will build up the class declartion
 	 * statement based off the properties set in the java class data model.
 	 * This method does not accept null parameters.  It will not return null.
 	 * @see NewJavaClassOperation#getJavaFileContent(IPackageFragment, String)
 	 * 
 	 * @param superclassName
 	 * @param className
 	 * @param interfaces
 	 * @return String class declaration string
 	 */
 	private String getClassDeclaration(String superclassName, String className, List interfaces) {
 		StringBuffer sb = new StringBuffer();
 		// Append appropriate modifiers
 		if (model.getBooleanProperty(MODIFIER_PUBLIC))
 			sb.append(PUBLIC);
 		if (model.getBooleanProperty(MODIFIER_ABSTRACT))
 			sb.append(ABSTRACT);
 		if (model.getBooleanProperty(MODIFIER_FINAL))
 			sb.append(FINAL);
 		// Add the class token 
 		sb.append(CLASS);
 		// Add the class name
 		sb.append(className + SPACE);
 		// If there is a superclass, add the extends and super class name
 		if (superclassName != null && superclassName.length() > 0 && !superclassName.equals(JAVA_LANG_OBJECT)) {
 			int index = superclassName.lastIndexOf(DOT);
 			if (index != -1)
 				superclassName = superclassName.substring(index + 1);
 			sb.append(EXTENDS + superclassName + SPACE);
 		}
 		// If there are interfaces, add the implements and then interate over the interface list
 		if (interfaces != null && interfaces.size() > 0) {
 			sb.append(IMPLEMENTS);
 			int size = interfaces.size();
 			for (int i = 0; i < size; i++) {
 				String interfaceName = (String) interfaces.get(i);
 				int index = interfaceName.lastIndexOf(DOT);
 				if (index != -1)
 					interfaceName = interfaceName.substring(index + 1);
 				sb.append(interfaceName);
 				if (i < size - 1)
 					sb.append(COMMA);
 				sb.append(SPACE);
 			}
 		}
 		sb.append(OPEN_BRA);
 		sb.append(lineSeparator);
 		// Return the finished class declaration string
 		return sb.toString();
 	}
 
 	/**
 	 * Subclasses may extend this method to add their own fields.  The default implementation
 	 * is not to have any fields.  This method will not return null.
 	 * @see NewJavaClassOperation#getJavaFileContent(IPackageFragment, String)
 	 * 
 	 * @return String fields string
 	 */
 	protected String getFields() {
 		return EMPTY_STRING;
 	}
 
 	/**
 	 * This method is intended for internal use only.  This will build up a string with the
 	 * contents of all the method stubs for the unimplemented methods defined in the interfaces.
 	 * It will also add inherited constructors from the superclass as appropriate.
 	 * This method does not accept null parameters.  It will not return null.
 	 * @see NewJavaClassOperation#getJavaFileContent(IPackageFragment, String)
 	 * 
 	 * @param superclassName
 	 * @param className
 	 * @return String method stubs string
 	 */
 	private String getMethodStubs(String superclassName, String className) {
 		StringBuffer sb = new StringBuffer();
 		IJavaProject javaProj = JemProjectUtilities.getJavaProject(getTargetProject());
		if (model.getBooleanProperty(MAIN_METHOD)) {
 			// Add main method
 			sb.append(MAIN_METHOD);
 			sb.append(lineSeparator);
 			sb.append(TODO_COMMENT);
 			sb.append(lineSeparator);
 			sb.append(TAB + CLOSE_BRA);
 			sb.append(lineSeparator);
 			sb.append(lineSeparator);
 		}
 
 		IType superClassType = null;
 		if (model.getBooleanProperty(CONSTRUCTOR) || model.getBooleanProperty(ABSTRACT_METHODS)) {
 			// Find super class type
 			try {
 				superClassType = javaProj.findType(superclassName);
 			} catch (JavaModelException e) {
 				Logger.getLogger().log(e);
 			}
 		}
 		if (model.getBooleanProperty(CONSTRUCTOR)) {
 			// Implement constructors from superclass
 			try {
 				if (superClassType != null) {
 					IMethod[] methods = superClassType.getMethods();
 					for (int j = 0; j < methods.length; j++) {
 						if (methods[j].isConstructor() && !Flags.isPrivate(methods[j].getFlags()) && !hasGenericParams(methods[j])) {
 							String methodStub = getMethodStub(methods[j], superclassName, className);
 							sb.append(methodStub);
 						}
 					}
 				}
 			} catch (JavaModelException e) {
 				Logger.getLogger().log(e);
 			}
 		}
 		// Add unimplemented methods defined in the interfaces list
 		if (model.getBooleanProperty(ABSTRACT_METHODS) && superClassType != null) {
 			String methodStub = getUnimplementedMethodsFromSuperclass(superClassType, className);
 			if (methodStub != null && methodStub.trim().length() > 0)
 				sb.append(methodStub);
 			methodStub = getUnimplementedMethodsFromInterfaces(superClassType, className, javaProj);
 			if (methodStub != null && methodStub.trim().length() > 0)
 				sb.append(methodStub);
 		}
 		// Add any user defined method stubs
 		if (superClassType != null) {
 			String userDefined = getUserDefinedMethodStubs(superClassType);
 			if (userDefined != null && userDefined.trim().length() > 0)
 				sb.append(userDefined);
 		}
 		// Return the methods string
 		return sb.toString();
 	}
 
 	private boolean hasGenericParams(IMethod method) {
 		try {
 			IType parentType = method.getDeclaringType();
 			String[] paramTypes = method.getParameterTypes();
 			
 			int nP = paramTypes.length;
 			for (int i = 0; i < nP; i++) {
 				String type = paramTypes[i];
 				if (!isPrimitiveType(type)) {
 					type = JavaModelUtil.getResolvedTypeName(type, parentType);
 					if(type.indexOf(Signature.C_GENERIC_START, 0) != -1){
 						return true;
 					}
 				} 
 			}
 		} catch (JavaModelException e) {
 			Logger.getLogger().log(e);
 		}
 		return false;
 	}
 	
 	/**
 	 * This method is intended for internal use only.  This will retrieve method stubs for
 	 * unimplemented methods in the superclass that will need to be created in the new class.
 	 * This method does not accept null parameters. It will not return null.
 	 * @see NewJavaClassOperation#getMethodStubs(String, String)
 	 * 
 	 * @param superClassType
 	 * @param className
 	 * @return String unimplemented methods defined in superclass
 	 */
 	private String getUnimplementedMethodsFromSuperclass(IType superClassType, String className) {
 		StringBuffer sb = new StringBuffer();
 		try {
 			// Implement abstract methods from superclass
 			IMethod[] methods = superClassType.getMethods();
 			for (int j = 0; j < methods.length; j++) {
 				IMethod method = methods[j];
 				int flags = method.getFlags();
 				// Add if the method is abstract, not private or static, and the option is selected in data model
 				if ((Flags.isAbstract(flags) && !Flags.isStatic(flags) && !Flags.isPrivate(flags)) || implementImplementedMethod(methods[j])) {
 					String methodStub = getMethodStub(methods[j], superClassType.getFullyQualifiedName(), className);
 					sb.append(methodStub);
 				}
 			}
 		} catch (JavaModelException e) {
 			Logger.getLogger().log(e);
 		}
 		// Return method stubs string
 		return sb.toString();
 	}
 
 	/**
 	 * This method is intended for internal use only.  This will retrieve method stubs for
 	 * unimplemented methods defined in the interfaces that will need to be created in the new class.
 	 * This method does not accept null parameters. It will not return null.
 	 * @see NewJavaClassOperation#getMethodStubs(String, String)
 	 * 
 	 * @param superClassType
 	 * @param className
 	 * @param javaProj
 	 * @return String unimplemented methods defined in interfaces
 	 */
 	private String getUnimplementedMethodsFromInterfaces(IType superClassType, String className, IJavaProject javaProj) {
 		StringBuffer sb = new StringBuffer();
 		try {
 			// Implement defined methods from interfaces
 			List interfaces = (List) model.getProperty(INTERFACES);
 			if (interfaces != null) {
 				for (int i = 0; i < interfaces.size(); i++) {
 					String qualifiedClassName = (String) interfaces.get(i);
 					IType interfaceType = javaProj.findType(qualifiedClassName);
 					IMethod[] methodArray = interfaceType.getMethods();
 					// Make sure the method isn't already defined in the heirarchy
 					for (int j = 0; j < methodArray.length; j++) {
 						if (isMethodImplementedInHierarchy(methodArray[j], superClassType))
 							continue;
 						String methodStub = getMethodStub(methodArray[j], qualifiedClassName, className);
 						sb.append(methodStub);
 					}
 				}
 			}
 		} catch (JavaModelException e) {
 			Logger.getLogger().log(e);
 		}
 		// Return method stubs string
 		return sb.toString();
 	}
 
 	/**
 	 * This method is intended for internal use only.  Checks to see if the passed type name
 	 * is of a primitive type.  This method does not accept null.  It will not return null.
 	 * @see Signature#getElementType(java.lang.String)
 	 * 
 	 * @param typeName
 	 * @return boolean is type Primitive?
 	 */
 	private boolean isPrimitiveType(String typeName) {
 		char first = Signature.getElementType(typeName).charAt(0);
 		return (first != Signature.C_RESOLVED && first != Signature.C_UNRESOLVED);
 	}
 
 	/**
 	 * This method is intended for internal use only.  This will add import statements for the specified
 	 * type if it is determined to be necessary. This does accept null parameters.  It will not return null.
 	 * @see NewJavaClassOperation#getMethodStub(IMethod, String, String)
 	 * 
 	 * @param refTypeSig
 	 * @param declaringType
 	 * @return String type signature
 	 * @throws JavaModelException
 	 */
 	private String resolveAndAdd(String refTypeSig, IType declaringType) throws JavaModelException {
 		if(refTypeSig.indexOf(Signature.C_GENERIC_START, 0) != -1){
 			getImportStatements(refTypeSig, declaringType);
 		} else {
 			String resolvedTypeName = JavaModelUtil.getResolvedTypeName(refTypeSig, declaringType);
 			// Could type not be resolved and is import statement missing?
 			if (resolvedTypeName != null && !importStatements.contains(resolvedTypeName) && !resolvedTypeName.startsWith("java.lang")) { //$NON-NLS-1$
 				importStatements.add(resolvedTypeName);
 			}
 		}
 		return Signature.toString(refTypeSig);
 	}
 	
 	private void getImportStatements(String signature, IType declaringType) throws JavaModelException{
 		String erasure = Signature.getTypeErasure(signature);
 		String resolvedTypeName = JavaModelUtil.getResolvedTypeName(erasure, declaringType);
 		if (resolvedTypeName != null && !importStatements.contains(resolvedTypeName) && !resolvedTypeName.startsWith("java.lang")) { //$NON-NLS-1$
 			importStatements.add(resolvedTypeName);
 		}
 		String [] params = Signature.getTypeArguments(signature);
 		for(int i=0;i<params.length; i++){
 			getImportStatements(params[i], declaringType);
 		}
 	}
 
 	/**
 	 * This method is intended for internal use only.  This will use the predefined tokens to generate the
 	 * actual method stubs.  This method does not accept null parameters.  It will not return null.
 	 * 
 	 * @param method
 	 * @param superClassName
 	 * @param className
 	 * @return String method stub
 	 */
 	private String getMethodStub(IMethod method, String superClassName, String className) {
 		StringBuffer sb = new StringBuffer();
 		try {
 			IType parentType = method.getDeclaringType();
 			String name = method.getElementName();
 			String[] paramTypes = method.getParameterTypes();
 			String[] paramNames = method.getParameterNames();
 			String[] exceptionTypes = method.getExceptionTypes();
 
 			// Parameters String
 			String paramString = EMPTY_STRING;
 			int nP = paramTypes.length;
 			for (int i = 0; i < nP; i++) {
 				String type = paramTypes[i];
 				// update import statements
 				if (!isPrimitiveType(type)) {
 					type = resolveAndAdd(type, parentType);
 				} else {
 					type = Signature.toString(type);
 				}
 
 				int index = type.lastIndexOf(DOT);
 				if (index != -1)
 					type = type.substring(index + 1);
 				paramString += type + SPACE + paramNames[i];
 				if (i < nP - 1)
 					paramString += COMMA + SPACE;
 			}
 			// Java doc
 			sb.append("\t/* (non-Java-doc)"); //$NON-NLS-1$
 			sb.append(lineSeparator);
 			sb.append("\t * @see "); //$NON-NLS-1$
 			sb.append(superClassName + POUND + name + OPEN_PAR);
 			sb.append(paramString);
 			sb.append(CLOSE_PAR);
 			sb.append(lineSeparator);
 			sb.append("\t */"); //$NON-NLS-1$
 			sb.append(lineSeparator);
 			// access
 			sb.append(TAB);
 			if (Flags.isPublic(method.getFlags()))
 				sb.append(PUBLIC);
 			else if (Flags.isProtected(method.getFlags()))
 				sb.append(PROTECTED);
 			else if (Flags.isPrivate(method.getFlags()))
 				sb.append(PRIVATE);
 			String returnType = null;
 			if (method.isConstructor()) {
 				sb.append(className);
 			} else {
 				// return type
 				returnType = method.getReturnType();
 				if (!isPrimitiveType(returnType)) {
 					returnType = resolveAndAdd(returnType, parentType);
 				} else {
 					returnType = Signature.toString(returnType);
 				}
 				int idx = returnType.lastIndexOf(DOT);
 				if (idx == -1)
 					sb.append(returnType);
 				else
 					sb.append(returnType.substring(idx + 1));
 				sb.append(SPACE);
 				// name
 				sb.append(name);
 			}
 			// Parameters
 			sb.append(OPEN_PAR + paramString + CLOSE_PAR);
 			// exceptions
 			int nE = exceptionTypes.length;
 			if (nE > 0) {
 				sb.append(SPACE + THROWS);
 				for (int i = 0; i < nE; i++) {
 					String type = exceptionTypes[i];
 					if (!isPrimitiveType(type)) {
 						type = resolveAndAdd(type, parentType);
 					} else {
 						type = Signature.toString(type);
 					}
 					int index = type.lastIndexOf(DOT);
 					if (index != -1)
 						type = type.substring(index + 1);
 					sb.append(type);
 					if (i < nE - 1)
 						sb.append(COMMA + SPACE);
 				}
 			}
 			sb.append(SPACE + OPEN_BRA);
 			sb.append(lineSeparator);
 			if (method.isConstructor()) {
 				sb.append(TAB + TAB + SUPER + OPEN_PAR);
 				for (int i = 0; i < nP; i++) {
 					sb.append(paramNames[i]);
 					if (i < nP - 1)
 						sb.append(COMMA + SPACE);
 				}
 				sb.append(CLOSE_PAR + SEMICOLON);
 				sb.append(lineSeparator);
 			} else {
 				String methodBody = getMethodBody(method, returnType);
 				sb.append(methodBody);
 			}
 			sb.append(TAB + CLOSE_BRA);
 			sb.append(lineSeparator);
 			sb.append(lineSeparator);
 		} catch (JavaModelException e) {
 			Logger.getLogger().log(e);
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * This method is intended for internal use only.  It checks to see whether or not the
 	 * method is already implemented in the class heirarchy.
 	 * It does not accept null parameters.  It will not return null.
 	 * @see NewJavaClassOperation#getUnimplementedMethodsFromInterfaces(IType, String, IJavaProject)
 	 * 
 	 * @param method
 	 * @param superClass
 	 * @return boolean is method already in heirarchy?
 	 */
 	private boolean isMethodImplementedInHierarchy(IMethod method, IType superClass) {
 		boolean ret = false;
 		IMethod foundMethod = findMethodImplementationInHierarchy(method, superClass);
 		// if the method exists and the property is set on the data model, then return true
 		if (foundMethod != null && foundMethod.exists() && !implementImplementedMethod(method))
 			ret = true;
 		return ret;
 	}
 
 	/**
 	 * This method is intended for internal use only.  This will recursively check the supertype heirarchy for
 	 * the passed in method.  This will not accept null parameters.  It will return null if the method does
 	 * not already exist in the heirarchy.
 	 * @see NewJavaClassOperation#isMethodImplementedInHierarchy(IMethod, IType)
 	 * 
 	 * @param method
 	 * @param superClass
 	 * @return IMethod the method from the heirarchy
 	 */
 	private IMethod findMethodImplementationInHierarchy(IMethod method, IType superClass) {
 		IMethod implementedMethod = null;
 		try {
 			if (superClass != null && superClass.exists()) {
 				ITypeHierarchy tH = superClass.newSupertypeHierarchy(new NullProgressMonitor());
 				implementedMethod = findMethodImplementationInHierarchy(tH, superClass, method.getElementName(), method.getParameterTypes(), method.isConstructor());
 			}
 		} catch (JavaModelException e) {
 			//Ignore
 		}
 		return implementedMethod;
 	}
 
 	/**
 	 * This method is intended for internal use only.  This will recursively check the supertype heirarchy for
 	 * the passed in method.  This will not accept null parameters.  It will return null if the method does
 	 * not already exist in the heirarchy.
 	 * @see NewJavaClassOperation#findMethodImplementationInHierarchy(IMethod, IType)
 	 * @see JavaModelUtil#findMethodImplementationInHierarchy(ITypeHierarchy, IType, String, String[], boolean)
 	 * 
 	 * @param tH
 	 * @param thisType
 	 * @param methodName
 	 * @param parameterTypes
 	 * @param isConstructor
 	 * @return IMethod
 	 * @throws JavaModelException
 	 */
 	private IMethod findMethodImplementationInHierarchy(ITypeHierarchy tH, IType thisType, String methodName, String parameterTypes[], boolean isConstructor) throws JavaModelException {
 		IMethod found = JavaModelUtil.findMethod(methodName, parameterTypes, isConstructor, thisType);
 		// If method exists make sure it is not abstract
 		if (found != null && !Flags.isAbstract(found.getFlags())) {
 			return found;
 		}
 		// Check recursively
 		return JavaModelUtil.findMethodInHierarchy(tH, thisType, methodName, parameterTypes, isConstructor);
 	}
 
 	/**
 	 * Subclasses may extend this method to provide their own specific method body definitions.
 	 * The default implementation is to add a todo, and to return the appropriate type.
 	 * This method does not accept null parameters.  It will not return null.
 	 * @see NewJavaClassOperation#getMethodStub(IMethod, String, String)
 	 * 
 	 * @param method
 	 * @param returnType
 	 * @return String method body
 	 */
 	protected String getMethodBody(IMethod method, String returnType) {
 		// Add a todo comment
 		String body = TODO_COMMENT;
 		body += lineSeparator;
 		// Add the appropriate default return type
 		if (returnType == null || returnType.equals(VOID))
 			return body;
 		if (returnType.equals(INT))
 			body += RETURN_0;
 		else if (returnType.equals(BOOLEAN))
 			body += RETURN_FALSE;
 		else
 			body += RETURN_NULL;
 		body += lineSeparator;
 		// Return the method body
 		return body;
 	}
 
 	/**
 	 * Subclasses may extend this method to provide their own user defined method stubs.  The
 	 * default implementation to just return an empty string.  This method will not accept
 	 * null parameter.  It will not return null.
 	 * @see NewJavaClassOperation#getMethodStubs(String, String)
 	 * 
 	 * @param superClassType
 	 * @return String user defined methods
 	 */
 	protected String getUserDefinedMethodStubs(IType superClassType) {
 		return EMPTY_STRING;
 	}
 
 	/**
 	 * Subclasses may extend this method to provide their own specialized return on which nonimplemented
 	 * methods to implement.  This does not accept a null parameter.  This will not return null.
 	 * The default implementation is to always return false.
 	 * 
 	 * @param method
 	 * @return boolean should implement method?
 	 */
 	protected boolean implementImplementedMethod(IMethod method) {
 		return false;
 	}
 
 	@Override
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
 			throws ExecutionException {
 		// TODO Auto-generated method stub
 		return doExecute(monitor, info);
 	}
 	
 	public IProject getTargetProject() {
 		String projectName = model.getStringProperty(IArtifactEditOperationDataModelProperties.PROJECT_NAME);
 		return ProjectUtilities.getProject(projectName);
 	}		
 }
