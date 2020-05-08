 /*******************************************************************************
  * Copyright (c) 2007 BEA Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * BEA Systems, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.classpathdep;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jdt.core.IClasspathAttribute;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jst.j2ee.internal.classpathdep.UpdateClasspathAttributesDataModelProperties;
 import org.eclipse.jst.j2ee.internal.classpathdep.UpdateClasspathAttributesDataModelProvider;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 
 /**
  * Contains utility code for executing the UpdateClasspathAttributeOperation.
  */
 public class UpdateClasspathAttributeUtil implements IClasspathDependencyConstants {
 
 	/**
 	 * Creates an IClasspathAttribute with the special WTP "org.eclipse.jst.component.dependency" name whose runtime path
 	 * (represented by the value) is unspecified and will therefore take the default value for the project in which it is
 	 * added. 
 	 * @return The created IClasspathAttribute.
 	 * @throws CoreException Thrown if a problem is encountered.
 	 */
 	public static IClasspathAttribute createDependencyAttribute() throws CoreException {
 		return createDependencyAttribute("");
 	}
 	
 	/**
 	 * Creates an IClasspathAttribute with the special WTP "org.eclipse.jst.component.dependency" name whose runtime path
 	 * (represented by the value) is set to the default for either a web project (/WEB-INF/lib) or non-web project (../).
 	 * @param isWebApp True if this attribute is being created for a classpath entry on a dynamic web project.
 	 * @param isClassFolder True if this attribute is being created for a class folder.
 	 * @return The created IClasspathAttribute.
 	 * @throws CoreException Thrown if a problem is encountered.
 	 */
 	public static IClasspathAttribute createDependencyAttribute(final boolean isWebApp, final boolean isClassFolder) throws CoreException {
 		return createDependencyAttribute(ClasspathDependencyUtil.getDefaultRuntimePath(isWebApp, isClassFolder));
 	}
 
 	/**
 	 * Creates an IClasspathAttribute with the special WTP "org.eclipse.jst.component.dependency" name with the
 	 * specified runtime path (will be used to set the attribute value).
 	 * @param runtimePath The runtime path in the deployed/exported module where resolved classpath components should
 	 * be added. Must be non-null.
 	 * @return The created IClasspathAttribute.
 	 * @throws CoreException Thrown if a problem is encountered.
 	 */
 	public static IClasspathAttribute createDependencyAttribute(final IPath runtimePath) throws CoreException {
 		return createDependencyAttribute(runtimePath.toString());
 	}
 	
 	private static IClasspathAttribute createDependencyAttribute(final String runtimePath) throws CoreException {
 		return JavaCore.newClasspathAttribute(IClasspathDependencyConstants.CLASSPATH_COMPONENT_DEPENDENCY, runtimePath);
 	}
 	
 	/**
 	 * Creates an IClasspathAttribute with the special WTP "org.eclipse.jst.component.nondependency" name. This attribute is
 	 * used on the resolved entries of classpath containers to prevent them from being exported/published.
 	 * @return The created IClasspathAttribute.
 	 * @throws CoreException Thrown if a problem is encountered.
 	 */
 	public static IClasspathAttribute createNonDependencyAttribute() throws CoreException {
 		return JavaCore.newClasspathAttribute(IClasspathDependencyConstants.CLASSPATH_COMPONENT_NON_DEPENDENCY, "");
 	}
 	
 	/**
 	 * Updates the specified Java project so that only the specified classpath entries have
 	 * the WTP component dependency attribute.
 	 * @param projectName Name of the target Java project.
 	 * @param entryToRuntimePath Map from IClasspathEntry to runtime path for all entries that should 
 	 * have the component dependency attribute.
 	 * @return Status from the operation.
 	 * @throws ExecutionException Thrown if an error is encountered.
 	 */	
 	public static IStatus updateDependencyAttributes(final IProgressMonitor monitor, final String projectName, final Map entryToRuntimePath) 
 		throws ExecutionException {
 		return createUpdateDependencyAttributesOperation(projectName, entryToRuntimePath).execute(monitor, null);
 	}
 	
 	/**
 	 * Creates the IDataModelOperation that will update the classpath for the specified Java project so that
 	 * only the specified list of classpath entries will have WTP component dependency attribute. 
 	 * @param projectName Name of the target Java project.
 	 * @param entryToRuntimePath Map from IClasspathEntry to runtime path for all entries that should 
 	 * have the component dependency attribute.
 	 * @return The operation.
 	 */
 	public static IDataModelOperation createUpdateDependencyAttributesOperation(final String projectName, final Map entryToRuntimePath) {
 		return createOperation(projectName, entryToRuntimePath, UpdateClasspathAttributesDataModelProperties.ENTRIES_WITH_ATTRIBUTE, true);
 	}
 	
 	/**
 	 * Creates the IDataModelOperation that will update the classpath for the specified Java project so that
 	 * the WTP component dependency attribute will be added to the specified list of classpath entries. 
 	 * @param projectName Name of the target Java project.
 	 * @param entryToRuntimePath Map from IClasspathEntry to runtime path for all entries that should have the attribute added.
 	 * @return The operation.
 	 */
 	public static IDataModelOperation createAddDependencyAttributesOperation(final String projectName, final Map entryToRuntimePath) {
 		return createOperation(projectName, entryToRuntimePath, UpdateClasspathAttributesDataModelProperties.ENTRIES_TO_ADD_ATTRIBUTE, true);
 	}
 	
 	/**
 	 * Creates the IDataModelOperation that will update the classpath for the specified Java project so that
 	 * the WTP component dependency attribute will be removed from the specified list of classpath entries. 
 	 * @param projectName Name of the target Java project.
 	 * @param entryToRuntimePath Map from IClasspathEntry to runtime path for all entries that should have the attribute removed.
 	 * @return The operation.
 	 */
 	public static IDataModelOperation createRemoveDependencyAttributesOperation(final String projectName, final Map entryToRuntimePath) {
 		return createOperation(projectName, entryToRuntimePath, UpdateClasspathAttributesDataModelProperties.ENTRIES_TO_REMOVE_ATTRIBUTE, true);
 	}
 	
 	/**
 	 * Adds the WTP component dependency attribute to the specified classpath entry using the default runtime path for the project. Does NOT check that the
 	 * specified entry is a valid entry for the dependency attribute.
 	 * 
 	 * @param monitor Progress monitor. Can be null.
 	 * @param projectName Name of the target Java project.
 	 * @param entry Classpath entry to which the attribute should be added.
 	 * @return Status from the operation.
 	 * @throws ExecutionException Thrown if an error is encountered.
 	 */	
 	public static IStatus addDependencyAttribute(final IProgressMonitor monitor, final String projectName, final IClasspathEntry entry) 
 		throws ExecutionException {
 		return addDependencyAttribute(monitor, projectName, entry, null);
 	}
 	
 	/**
 	 * Adds the WTP component non-dependency attribute to the specified classpath entry using the default runtime path for the project. 
 	 * 
 	 * @param monitor Progress monitor. Can be null.
 	 * @param projectName Name of the target Java project.
 	 * @param entry Classpath entry to which the attribute should be added.
 	 * @return Status from the operation.
 	 * @throws ExecutionException Thrown if an error is encountered.
 	 */	
 	public static IStatus addNonDependencyAttribute(final IProgressMonitor monitor, final String projectName, final IClasspathEntry entry) 
 		throws ExecutionException {
 		final Map entryToRuntimePath = new HashMap();
 		entryToRuntimePath.put(entry, null);
 		return createOperation(projectName, entryToRuntimePath, UpdateClasspathAttributesDataModelProperties.ENTRIES_TO_ADD_ATTRIBUTE, false).execute(monitor, null);
 	}
 	
 	/**
 	 * Adds the WTP component dependency attribute to the specified classpath entry. Does NOT check that the
 	 * specified entry is a valid entry for the dependency attribute.
 	 * 
 	 * @param monitor Progress monitor. Can be null.
 	 * @param projectName Name of the target Java project.
 	 * @param entry Classpath entry to which the attribute should be added.
 	 * @param runtimePath Runtime path to which entry elements should be deployed. Null if the default runtime path for the project should be used.
 	 * @return Status from the operation.
 	 * @throws ExecutionException Thrown if an error is encountered.
 	 */	
 	public static IStatus addDependencyAttribute(final IProgressMonitor monitor, final String projectName, final IClasspathEntry entry, final IPath runtimePath) 
 		throws ExecutionException {
 		final Map entryToRuntimePath = new HashMap();
 		entryToRuntimePath.put(entry, runtimePath);
 		return createOperation(projectName, entryToRuntimePath, UpdateClasspathAttributesDataModelProperties.ENTRIES_TO_ADD_ATTRIBUTE, true).execute(monitor, null);
 	}
 	
 	/**
 	 * Removes the WTP component dependency attribute from the specified classpath entry. Does NOT check that the
 	 * specified entry is a valid entry for the dependency attribute.
 	 * 
 	 * @param monitor Progress monitor. Can be null.
 	 * @param projectName Name of the target Java project.
 	 * @param entry Classpath entry from which the attribute should be removed.
 	 * @return Status from the operation.
 	 * @throws ExecutionException Thrown if an error is encountered.
 	 */	
 	public static IStatus removeDependencyAttribute(final IProgressMonitor monitor, final String projectName, final IClasspathEntry entry) 
 	throws ExecutionException {
 		final Map entryToRuntimePath = new HashMap();
 		entryToRuntimePath.put(entry, null);
 		return createOperation(projectName, entryToRuntimePath, UpdateClasspathAttributesDataModelProperties.ENTRIES_TO_REMOVE_ATTRIBUTE, true).execute(monitor, null);
 	}
 	
 	private static IDataModelOperation createOperation(String projectName, final Map entryToRuntimePath, final String entryMapProperty, final boolean modifyComponentClasspathDependency) { 
 		IDataModel dataModel = DataModelFactory.createDataModel(new UpdateClasspathAttributesDataModelProvider());
 		dataModel.setProperty(UpdateClasspathAttributesDataModelProperties.PROJECT_NAME, projectName);
 		dataModel.setProperty(entryMapProperty, entryToRuntimePath);
 		dataModel.setProperty(UpdateClasspathAttributesDataModelProperties.MODIFY_CLASSPATH_COMPONENT_DEPENDENCY, new Boolean(modifyComponentClasspathDependency));
 		return dataModel.getDefaultOperation();
 	}
 	
 }
