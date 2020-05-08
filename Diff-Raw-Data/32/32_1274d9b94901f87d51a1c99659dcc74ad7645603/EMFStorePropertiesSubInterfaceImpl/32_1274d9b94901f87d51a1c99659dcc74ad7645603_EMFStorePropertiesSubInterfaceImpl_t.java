 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 
 package org.eclipse.emf.emfstore.server.core.subinterfaces;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.emfstore.common.model.EMFStoreProperty;
 import org.eclipse.emf.emfstore.server.core.AbstractEmfstoreInterface;
 import org.eclipse.emf.emfstore.server.core.AbstractSubEmfstoreInterface;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.FatalEmfStoreException;
 import org.eclipse.emf.emfstore.server.model.ProjectHistory;
 import org.eclipse.emf.emfstore.server.model.ProjectId;
 
 /**
  * The {@link EMFStorePropertiesSubInterfaceImpl} class is responsible for
  * handling modifications of EMFStore properties.
  * 
  * @author groeber
  */
 public class EMFStorePropertiesSubInterfaceImpl extends AbstractSubEmfstoreInterface {
 
 	/**
 	 * @param parentInterface
 	 *            the parent interface
 	 * @throws FatalEmfStoreException
 	 *             if any fatal error occurs
 	 */
 	public EMFStorePropertiesSubInterfaceImpl(AbstractEmfstoreInterface parentInterface) throws FatalEmfStoreException {
 		super(parentInterface);
 	}
 
 	/**
 	 * Set the Shared Properties from client on server.
 	 * 
 	 * @param property
 	 *            Property to be set
 	 * @param projectId
 	 *            Project where the properties should be saved
 	 * @throws EmfStoreException
 	 *             if the specified project does not exist
 	 */
 	public void setProperties(List<EMFStoreProperty> properties, ProjectId projectId) throws EmfStoreException {
 		EList<ProjectHistory> serverProjects = getServerSpace().getProjects();
		boolean projectFound = false;
 
		for (ProjectHistory currentHistory : serverProjects) {
			if (currentHistory.getProjectId().equals(projectId)) {
				projectFound = true;
				for (EMFStoreProperty prop : properties) {
 					currentHistory.getSharedProperties().add(prop);
 				}
				break;
 			}
 		}
 
		if (projectFound) {
			try {
				getServerSpace().save();
			} catch (IOException e) {
				throw new EmfStoreException("Cannot set the properties on the server.");
			}
		} else {
			throw new EmfStoreException("The Project does not exist on the server. Cannot set the properties.");
 		}
 	}
 
 	/**
 	 * Return the Properties for a specific Project.
 	 * 
 	 * @param projectId
 	 *            ProjectId for the properties
 	 * @return EMap containing the Key string and the property value
 	 * @throws EmfStoreException
 	 *             if specified property does not exist
 	 */
 	public List<EMFStoreProperty> getProperties(ProjectId projectId) throws EmfStoreException {
 		EList<ProjectHistory> serverProjects = getServerSpace().getProjects();
 
 		ProjectHistory currentHistory = null;
 		for (ProjectHistory history : serverProjects) {
 			if (history.getProjectId().equals(projectId)) {
 				currentHistory = history;
 				break;
 			}
 		}
 
 		if (currentHistory != null) {
 			List<EMFStoreProperty> temp = new ArrayList<EMFStoreProperty>();
 			for (EMFStoreProperty prop : currentHistory.getSharedProperties()) {
 				temp.add(prop);
 			}
 			return temp;
 		}
 		throw new EmfStoreException("The Project does not exist on the server. Cannot set the properties.");
 	}
 
 }
