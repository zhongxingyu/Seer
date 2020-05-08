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
 package org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.dialogs.admin.acimport;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.ecp.common.util.DialogHandler;
 import org.eclipse.emf.emfstore.client.model.util.EmfStoreInterface;
 import org.eclipse.emf.emfstore.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACGroup;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACOrgUnitId;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACUser;
 
 /**
  * @author deser, karakoc
  */
 public class ImportController {
 
 	private final EmfStoreInterface adminBroker;
 
 	private ImportSource importSource;
 
	private Hashtable<ACOrgUnitId, ImportItemWrapper> importedUnits;
 
 	/**
 	 * @param adminBroker
 	 *            the admin broker.
 	 */
 	public ImportController(EmfStoreInterface adminBroker) {
 		this.adminBroker = adminBroker;
		this.importedUnits = new Hashtable<ACOrgUnitId, ImportItemWrapper>();
 	}
 
 	/**
 	 * @param wrappedOrgUnits
 	 *            a list of wrapped OrgUnits, which should be imported.
 	 */
 	public void importOrgUnits(ArrayList<ImportItemWrapper> wrappedOrgUnits) {
 
 		// first go through the list and add all units of type group
 		importGroups(wrappedOrgUnits);
 
 		// then add all units of type user
 		importUsers(wrappedOrgUnits);
 
 		// finally set the associations on the imported units
 		setAssociations();
 	}
 
 	private void importUsers(ArrayList<ImportItemWrapper> wrappedOrgUnits) {
 		for (int i = 0; i < wrappedOrgUnits.size(); i++) {
 			ImportItemWrapper wrappedOrgUnit = wrappedOrgUnits.get(i);
 			if (wrappedOrgUnit.getOrgUnit() instanceof ACUser) {
 				// add this user to the system
 				try {
 					String username = wrappedOrgUnit.getOrgUnit().getName();
 					if (null == existUser(username)) {
						this.importedUnits.put(adminBroker.createUser(username), wrappedOrgUnit);
 					}
 				} catch (EmfStoreException e) {
 					WorkspaceUtil.logWarning(e.getMessage(), e);
 					DialogHandler.showExceptionDialog(e);
 				}
 			}
 		}
 	}
 
 	private void importGroups(ArrayList<ImportItemWrapper> wrappedOrgUnits) {
 		for (int i = 0; i < wrappedOrgUnits.size(); i++) {
 			ImportItemWrapper wrappedOrgUnit = wrappedOrgUnits.get(i);
 			if (wrappedOrgUnit.getOrgUnit() instanceof ACGroup) {
 				// add this group to the system if it doesn't exist
 				try {
 					String groupname = wrappedOrgUnit.getOrgUnit().getName();
 					if (null == existGroup(groupname)) {
						this.importedUnits.put(adminBroker.createGroup(groupname), wrappedOrgUnit);
 					}
 				} catch (EmfStoreException e) {
 					WorkspaceUtil.logWarning(e.getMessage(), e);
 					DialogHandler.showExceptionDialog(e);
 				}
 			}
 		}
 	}
 
 	private void setAssociations() {
		for (ACOrgUnitId unitId : importedUnits.keySet()) {
 			if (this.importedUnits.get(unitId).getParentOrgUnit() != null) {
 
 				ACOrgUnitId existGroup = existGroup(this.importedUnits.get(unitId).getParentOrgUnit().getOrgUnit()
 					.getName());
 
 				// we do not want self-containment
 				if (existGroup != null && !existGroup.equals(unitId)) {
 					try {
						adminBroker.addMember(existGroup, unitId);
 					} catch (EmfStoreException e) {
 						WorkspaceUtil.logWarning(e.getMessage(), e);
 						DialogHandler.showExceptionDialog(e);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param groupName
 	 *            the name of group.
 	 * @return A ACOrgUnitId object if the group already exists null otherwise.
 	 */
 	private ACOrgUnitId existGroup(final String groupName) {
 		ACOrgUnitId exist = null;
 		try {
			List<ACGroup> groups = getAdminBroker().getGroups();
 			Iterator<ACGroup> iteratorGroup = groups.iterator();
 
 			while (iteratorGroup.hasNext()) {
 				ACGroup gr = iteratorGroup.next();
 				if (gr.getName().equalsIgnoreCase((groupName))) {
 					exist = gr.getId();
 				}
 			}
 		} catch (EmfStoreException e) {
 			DialogHandler.showExceptionDialog(e);
 		}
 		return exist;
 	}
 
 	/**
 	 * @param userName
 	 *            the name of user.
 	 * @return A ACOrgUnitId object if the user already exist null otherwise.
 	 */
 	private ACOrgUnitId existUser(final String userName) {
 		ACOrgUnitId exist = null;
 		try {
			List<ACUser> users = getAdminBroker().getUsers();
 			Iterator<ACUser> iteratorUser = users.iterator();
 
 			while (iteratorUser.hasNext()) {
 				ACUser us = iteratorUser.next();
 				if (us.getName().equalsIgnoreCase((userName))) {
 					exist = us.getId();
 				}
 			}
 		} catch (EmfStoreException e) {
 			DialogHandler.showExceptionDialog(e);
 		}
 		return exist;
 	}
 
 	/**
 	 * @return the admin broker.
 	 */
 	public EmfStoreInterface getAdminBroker() {
 		return adminBroker;
 	}
 
 	/**
 	 * @param importSource
 	 *            the import source, that should be used for the import.
 	 */
 	public void setImportSource(ImportSource importSource) {
 		this.importSource = importSource;
 	}
 
 	/**
 	 * @return the current import source.
 	 */
 	public ImportSource getImportSource() {
 		return importSource;
 	}
 
 	/**
 	 * @return a small title, that can be displayed e.g. in a GUI.
 	 */
 	public String getTitle() {
 		// if importSource isn't initialized yet, return an empty string instead
 		return null == importSource ? "" : importSource.getLabel();
 	}
 
 	/**
 	 * @return Returns a small message to describe where data currently gets
 	 *         imported from.
 	 */
 	public String getMessage() {
 		// if importSource isn't initialized yet, return an empty string instead
 		return null == importSource ? "" : importSource.getMessage();
 	}
 }
