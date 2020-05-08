 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.server.core;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.AdminEmfStore;
 import org.eclipse.emf.emfstore.internal.server.ServerConfiguration;
 import org.eclipse.emf.emfstore.internal.server.accesscontrol.AuthorizationControl;
 import org.eclipse.emf.emfstore.internal.server.accesscontrol.PAPrivileges;
 import org.eclipse.emf.emfstore.internal.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.internal.server.exceptions.FatalESException;
 import org.eclipse.emf.emfstore.internal.server.exceptions.InvalidInputException;
 import org.eclipse.emf.emfstore.internal.server.exceptions.StorageException;
 import org.eclipse.emf.emfstore.internal.server.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectHistory;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectId;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.internal.server.model.ServerSpace;
 import org.eclipse.emf.emfstore.internal.server.model.SessionId;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACGroup;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACOrgUnit;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACOrgUnitId;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACUser;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.AccesscontrolFactory;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.roles.Role;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.roles.RolesFactory;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.roles.RolesPackage;
 import org.eclipse.emf.emfstore.internal.server.model.dao.ACDAOFacade;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 
 /**
  * Implementation of {@link AdminEmfStore} interface.
  * 
  * @author wesendon
  */
 // TODO: bring this interface in new subinterface structure and refactor it
 public class AdminEmfStoreImpl extends AbstractEmfstoreInterface implements AdminEmfStore {
 
 	private final ACDAOFacade daoFacade;
 	private SessionId sessionIdThatCreatedAUser;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param daoFacade
 	 *            provider facade for access control related DAOs
 	 * @param serverSpace
 	 *            the server space
 	 * @param authorizationControl
 	 *            the authorization control
 	 * @throws FatalESException
 	 *             in case of failure
 	 */
 	public AdminEmfStoreImpl(ACDAOFacade daoFacade, ServerSpace serverSpace, AuthorizationControl authorizationControl)
 		throws FatalESException {
 		super(serverSpace, authorizationControl);
 		this.daoFacade = daoFacade;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<ACGroup> getGroups(SessionId sessionId) throws ESException {
 		checkForNulls(sessionId);
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, null);
 		final List<ACGroup> result = new ArrayList<ACGroup>();
 		for (final ACGroup group : daoFacade.getGroups()) {
 			// quickfix
 			final ACGroup copy = ModelUtil.clone(group);
 			clearMembersFromGroup(copy);
 			result.add(copy);
 		}
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<ACGroup> getGroups(SessionId sessionId, ACOrgUnitId orgUnitId) throws ESException {
 		checkForNulls(sessionId, orgUnitId);
 		getAuthorizationControl().checkProjectAdminAccessForOrgUnit(sessionId, orgUnitId);
 		final List<ACGroup> result = new ArrayList<ACGroup>();
 		final ACOrgUnit orgUnit = getOrgUnit(orgUnitId);
 		for (final ACGroup group : daoFacade.getGroups()) {
 			if (group.getMembers().contains(orgUnit)) {
 
 				// quickfix
 				final ACGroup copy = ModelUtil.clone(group);
 				clearMembersFromGroup(copy);
 				result.add(copy);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ACOrgUnitId createGroup(SessionId sessionId, String name) throws ESException {
 		checkForNulls(sessionId, name);
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, null, PAPrivileges.CreateGroup);
 		if (groupExists(name)) {
 			throw new InvalidInputException(Messages.AdminEmfStoreImpl_Group_Already_Exists);
 		}
 		final ACGroup acGroup = AccesscontrolFactory.eINSTANCE.createACGroup();
 		acGroup.setName(name);
 		acGroup.setDescription(StringUtils.EMPTY);
 		daoFacade.add(acGroup);
 		save();
 		return ModelUtil.clone(acGroup.getId());
 	}
 
 	private boolean groupExists(String name) {
 		for (final ACGroup group : daoFacade.getGroups()) {
 			if (group.getName().equals(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeGroup(SessionId sessionId, ACOrgUnitId user, ACOrgUnitId group) throws ESException {
 		checkForNulls(sessionId, user, group);
 		final boolean isServerAdmin = getAuthorizationControl().checkProjectAdminAccess(sessionId, null,
 			PAPrivileges.DeleteOrgUnit);
 
 		if (!isServerAdmin) {
 			getAuthorizationControl().checkProjectAdminAccessForOrgUnit(sessionId, group);
 		}
 
 		getGroup(group).getMembers().remove(getOrgUnit(user));
 		save();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void deleteGroup(SessionId sessionId, ACOrgUnitId groupId) throws ESException {
 		checkForNulls(sessionId, groupId);
 		getAuthorizationControl().checkProjectAdminAccessForOrgUnit(sessionId, groupId);
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, null, PAPrivileges.DeleteOrgUnit);
 		for (final Iterator<ACGroup> iter = daoFacade.getGroups().iterator(); iter.hasNext();) {
 			final ACGroup next = iter.next();
 			if (next.getId().equals(groupId)) {
 				daoFacade.remove(next);
 				EcoreUtil.delete(next);
 				save();
 				return;
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<ACOrgUnit> getMembers(SessionId sessionId, ACOrgUnitId groupId) throws ESException {
 		checkForNulls(sessionId, groupId);
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, null);
 
 		// quickfix
 		final List<ACOrgUnit> result = new ArrayList<ACOrgUnit>();
 		for (final ACOrgUnit orgUnit : getGroup(groupId).getMembers()) {
 			result.add(ModelUtil.clone(orgUnit));
 		}
 		clearMembersFromGroups(result);
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void addMember(SessionId sessionId, ACOrgUnitId group, ACOrgUnitId member) throws ESException {
 
 		checkForNulls(sessionId, group, member);
 
 		final boolean isServerAdmin = getAuthorizationControl().checkProjectAdminAccess(
 			sessionId, null, PAPrivileges.ChangeAssignmentsOfOrgUnits);
 
 		if (!isServerAdmin) {
 			getAuthorizationControl().checkProjectAdminAccessForOrgUnit(sessionId, group);
 
 		}
 
 		addToGroup(group, member);
 	}
 
 	private void addToGroup(ACOrgUnitId group, ACOrgUnitId member) throws ESException {
 		final ACGroup acGroup = getGroup(group);
 		final ACOrgUnit acMember = getOrgUnit(member);
 		acGroup.getMembers().add(acMember);
 		save();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeMember(SessionId sessionId, ACOrgUnitId group, ACOrgUnitId member) throws ESException {
 
 		if (sessionId == null || group == null || member == null) {
 			throw new InvalidInputException();
 		}
 
 		final boolean isServerAdmin = getAuthorizationControl().checkProjectAdminAccess(
 			sessionId, null, PAPrivileges.ChangeAssignmentsOfOrgUnits);
 
 		if (!isServerAdmin) {
 			getAuthorizationControl().checkProjectAdminAccessForOrgUnit(sessionId, group);
 		}
 
 		removeFromGroup(group, member);
 	}
 
 	/**
 	 * @param group
 	 * @param member
 	 * @throws ESException
 	 */
 	private void removeFromGroup(ACOrgUnitId group, ACOrgUnitId member) throws ESException {
 		final ACGroup acGroup = getGroup(group);
 		final ACOrgUnit acMember = getOrgUnit(member);
 		if (acGroup.getMembers().contains(acMember)) {
 			acGroup.getMembers().remove(acMember);
 			save();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<ACOrgUnit> getParticipants(SessionId sessionId, ProjectId projectId) throws ESException {
 		if (sessionId == null) {
 			throw new InvalidInputException();
 		}
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, projectId);
 		final List<ACOrgUnit> result = new ArrayList<ACOrgUnit>();
 		for (final ACOrgUnit orgUnit : daoFacade.getUsers()) {
 			for (final Role role : orgUnit.getRoles()) {
 				if (isServerAdmin(role) || role.getProjects().contains(projectId)) {
 					result.add(ModelUtil.clone(orgUnit));
 				}
 			}
 		}
 		for (final ACOrgUnit orgUnit : daoFacade.getGroups()) {
 			for (final Role role : orgUnit.getRoles()) {
 				if (isServerAdmin(role) || role.getProjects().contains(projectId)) {
 					result.add(ModelUtil.clone(orgUnit));
 				}
 			}
 		}
 		// quickfix
 		clearMembersFromGroups(result);
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void addParticipant(SessionId sessionId, ProjectId projectId, ACOrgUnitId participant, EClass roleClass)
 		throws ESException {
 		if (sessionId == null || projectId == null || participant == null || roleClass == null) {
 			throw new InvalidInputException();
 		}
 		getAuthorizationControl()
 			.checkProjectAdminAccess(sessionId, projectId, PAPrivileges.AssignRoleToOrgUnit);
 		projectId = getProjectId(projectId);
 		final ACOrgUnit orgUnit = getOrgUnit(participant);
 		for (final Role role : orgUnit.getRoles()) {
 			if (role.getProjects().contains(projectId)) {
 				return;
 			}
 		}
 		// check whether role exists
 		for (final Role role : orgUnit.getRoles()) {
 			if (areEqual(role, roleClass)) {
 				role.getProjects().add(ModelUtil.clone(projectId));
 				save();
 				return;
 			}
 		}
 
 		final Role newRole = (Role) RolesPackage.eINSTANCE.getEFactoryInstance().create(
 			(EClass) RolesPackage.eINSTANCE.getEClassifier(roleClass.getName()));
 
 		newRole.getProjects().add(ModelUtil.clone(projectId));
 		orgUnit.getRoles().add(newRole);
 		save();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void addInitialParticipant(SessionId sessionId, ProjectId projectId, ACOrgUnitId participant,
 		EClass roleClass)
 		throws ESException {
 		if (sessionId == null || projectId == null || participant == null || roleClass == null) {
 			throw new InvalidInputException();
 		}
		getAuthorizationControl()
			.checkProjectAdminAccess(sessionId, projectId, PAPrivileges.AssignRoleToOrgUnit);
 		projectId = getProjectId(projectId);
 		final ACOrgUnit orgUnit = getOrgUnit(participant);
 
 		final Role newRole = (Role) RolesPackage.eINSTANCE.getEFactoryInstance().create(
 			(EClass) RolesPackage.eINSTANCE.getEClassifier(roleClass.getName()));
 
 		// check whether role exists
 		for (final Role role : orgUnit.getRoles()) {
 			if (areEqual(role, roleClass)) {
 				role.getProjects().add(ModelUtil.clone(projectId));
 				save();
 				return;
 			}
 		}
 
 		newRole.getProjects().add(ModelUtil.clone(projectId));
 		orgUnit.getRoles().add(newRole);
 		save();
 	}
 
 	private ProjectId getProjectId(ProjectId projectId) throws ESException {
 		for (final ProjectHistory projectHistory : getServerSpace().getProjects()) {
 			if (projectHistory.getProjectId().equals(projectId)) {
 				return projectHistory.getProjectId();
 			}
 		}
 		throw new ESException(Messages.AdminEmfStoreImpl_Unknown_ProjectID);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void removeParticipant(SessionId sessionId, ProjectId projectId, ACOrgUnitId participantId)
 		throws ESException {
 		checkForNulls(sessionId, projectId, participantId);
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, projectId,
 			PAPrivileges.ChangeAssignmentsOfOrgUnits);
 
 		projectId = getProjectId(projectId);
 		final ACOrgUnit orgUnit = getOrgUnit(participantId);
 		for (final Role role : orgUnit.getRoles()) {
 			if (role.getProjects().contains(projectId)) {
 				role.getProjects().remove(projectId);
 				save();
 				return;
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public Role getRole(SessionId sessionId, ProjectId projectId, ACOrgUnitId orgUnitId) throws ESException {
 		if (sessionId == null || projectId == null || orgUnitId == null) {
 			throw new InvalidInputException();
 		}
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, projectId);
 		projectId = getProjectId(projectId);
 		final ACOrgUnit oUnit = getOrgUnit(orgUnitId);
 		for (final Role role : oUnit.getRoles()) {
 			if (isServerAdmin(role) || role.getProjects().contains(projectId)) {
 				return role;
 			}
 		}
 		throw new ESException(Messages.AdminEmfStoreImpl_Could_Not_Find_OrgUnit);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void changeRole(SessionId sessionId, ProjectId projectId, ACOrgUnitId orgUnitId, EClass roleClass)
 		throws ESException {
 
 		if (sessionId == null || projectId == null || orgUnitId == null || roleClass == null) {
 			throw new InvalidInputException();
 		}
 
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, projectId);
 		if (!ServerConfiguration.isProjectAdminPrivileg(PAPrivileges.AssignRoleToOrgUnit)) {
 			throw new AccessControlException();
 		}
 
 		projectId = getProjectId(projectId);
 		final ACOrgUnit orgUnit = getOrgUnit(orgUnitId);
 		// delete old role first
 		final Role role = getRole(projectId, orgUnit);
 		if (role != null) {
 			role.getProjects().remove(projectId);
 			if (role.getProjects().size() == 0) {
 				orgUnit.getRoles().remove(role);
 			}
 		}
 
 		// if server admin
 		if (roleClass.getName().equals(RolesPackage.Literals.SERVER_ADMIN.getName())) {
 			orgUnit.getRoles().add(RolesFactory.eINSTANCE.createServerAdmin());
 			save();
 			return;
 		}
 		// add project to role if it exists
 		for (final Role role1 : orgUnit.getRoles()) {
 			if (role1.eClass().getName().equals(roleClass.getName())) {
 				role1.getProjects().add(ModelUtil.clone(projectId));
 				save();
 				return;
 			}
 		}
 		// create role if does not exists
 		final Role newRole = (Role) RolesPackage.eINSTANCE.getEFactoryInstance().create(
 			(EClass) RolesPackage.eINSTANCE.getEClassifier(roleClass.getName()));
 		newRole.getProjects().add(ModelUtil.clone(projectId));
 		orgUnit.getRoles().add(newRole);
 		save();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void assignRole(SessionId sessionId, ACOrgUnitId orgUnitId, EClass roleClass)
 		throws ESException {
 
 		if (sessionId == null || orgUnitId == null) {
 			throw new InvalidInputException();
 		}
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, null,
 			PAPrivileges.AssignRoleToOrgUnit);
 
 		final ACOrgUnit orgUnit = getOrgUnit(orgUnitId);
 
 		// check if org unit alrady has role
 		for (final Role role : orgUnit.getRoles()) {
 			if (areEqual(role, roleClass)) {
 				return;
 			}
 		}
 
 		final Role newRole = (Role) RolesPackage.eINSTANCE.getEFactoryInstance().create(
 			(EClass) RolesPackage.eINSTANCE.getEClassifier(roleClass.getName()));
 
 		orgUnit.getRoles().add(newRole);
 		save();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<ACUser> getUsers(SessionId sessionId) throws ESException {
 		checkForNulls(sessionId);
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, null);
 		final List<ACUser> result = new ArrayList<ACUser>();
 		for (final ACUser user : daoFacade.getUsers()) {
 			result.add(user);
 		}
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<ACOrgUnit> getOrgUnits(SessionId sessionId) throws ESException {
 		if (sessionId == null) {
 			throw new InvalidInputException();
 		}
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, null);
 		final List<ACOrgUnit> result = new ArrayList<ACOrgUnit>();
 		for (final ACOrgUnit user : daoFacade.getUsers()) {
 			result.add(ModelUtil.clone(user));
 		}
 		for (final ACOrgUnit group : daoFacade.getGroups()) {
 			result.add(ModelUtil.clone(group));
 		}
 		// quickfix
 		clearMembersFromGroups(result);
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public List<ProjectInfo> getProjectInfos(SessionId sessionId) throws ESException {
 		checkForNulls(sessionId);
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, null);
 		final List<ProjectInfo> result = new ArrayList<ProjectInfo>();
 		for (final ProjectHistory ph : getServerSpace().getProjects()) {
 			result.add(getProjectInfo(ph));
 		}
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ACOrgUnitId createUser(SessionId sessionId, String name) throws ESException {
 		if (sessionId == null || name == null) {
 			throw new InvalidInputException();
 		}
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, null, PAPrivileges.CreateUser);
 		sessionIdThatCreatedAUser = sessionId;
 		if (userExists(name)) {
 			throw new InvalidInputException("username '" + name + "' already exists."); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		final ACUser acUser = AccesscontrolFactory.eINSTANCE.createACUser();
 		// acUser.setId(AccesscontrolFactory.eINSTANCE.createACOrgUnitId());
 		acUser.setName(name);
 		acUser.setDescription(" "); //$NON-NLS-1$
 		daoFacade.add(acUser);
 		save();
 		return ModelUtil.clone(acUser.getId());
 	}
 
 	private boolean userExists(String name) {
 		for (final ACUser user : daoFacade.getUsers()) {
 			if (user.getName().equals(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void deleteUser(SessionId sessionId, ACOrgUnitId userId) throws ESException {
 		if (sessionId == null || userId == null) {
 			throw new InvalidInputException();
 		}
 		getAuthorizationControl()
 			.checkProjectAdminAccessForOrgUnit(sessionId, userId);
 		getAuthorizationControl()
 			.checkProjectAdminAccess(sessionId, null, PAPrivileges.DeleteOrgUnit);
 		for (final Iterator<ACUser> iter = daoFacade.getUsers().iterator(); iter.hasNext();) {
 			final ACUser next = iter.next();
 			if (next.getId().equals(userId)) {
 				daoFacade.remove(next);
 				// TODO: move ecore delete into ServerSpace#deleteUser implementation
 				EcoreUtil.delete(next);
 				save();
 				return;
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void changeOrgUnit(SessionId sessionId, ACOrgUnitId orgUnitId, String name, String description)
 		throws ESException {
 		checkForNulls(sessionId, orgUnitId, name, description);
 		getAuthorizationControl().checkProjectAdminAccessForOrgUnit(sessionId, orgUnitId);
 		final ACOrgUnit ou = getOrgUnit(orgUnitId);
 		ou.setName(name);
 		ou.setDescription(description);
 		save();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.server.AdminEmfStore#changeUser(org.eclipse.emf.emfstore.internal.server.model.SessionId,
 	 *      org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACOrgUnitId, java.lang.String,
 	 *      java.lang.String)
 	 */
 	public void changeUser(SessionId sessionId, ACOrgUnitId userId, String name, String password) throws ESException {
 		if (sessionId == null || userId == null || name == null || password == null) {
 			throw new InvalidInputException();
 		}
 		final boolean isServerAdmin = getAuthorizationControl()
 			.checkProjectAdminAccess(sessionId, null, PAPrivileges.ChangeUserPassword);
 
 		if (!isServerAdmin) {
 			// if newly created user OR if executing project admin has
 			// user id under control
 
 			if (sessionId.equals(sessionIdThatCreatedAUser)) {
 				updateUser(userId, name, password);
 			} else {
 				getAuthorizationControl().checkProjectAdminAccessForOrgUnit(sessionId, userId);
 				updateUser(userId, name, password);
 			}
 		} else {
 			// executing user is server admin
 			updateUser(userId, name, password);
 		}
 	}
 
 	private void updateUser(ACOrgUnitId userId, String name, String password) throws ESException {
 
 		final ACUser user = (ACUser) getOrgUnit(userId);
 		user.setName(name);
 		user.setPassword(password);
 		save();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ACOrgUnit getOrgUnit(SessionId sessionId, ACOrgUnitId orgUnitId) throws ESException {
 		checkForNulls(sessionId, orgUnitId);
 		getAuthorizationControl().checkProjectAdminAccess(sessionId, null);
 		// quickfix
 		final ACOrgUnit orgUnit = ModelUtil.clone(getOrgUnit(orgUnitId));
 		clearMembersFromGroup(orgUnit);
 		return orgUnit;
 	}
 
 	/**
 	 * This method is used as fix for the containment issue of group.
 	 */
 	private void clearMembersFromGroups(Collection<ACOrgUnit> orgUnits) {
 		for (final ACOrgUnit orgUnit : orgUnits) {
 			clearMembersFromGroup(orgUnit);
 		}
 	}
 
 	/**
 	 * This method is used as fix for the containment issue of group.
 	 */
 	private void clearMembersFromGroup(ACOrgUnit orgUnit) {
 		if (orgUnit instanceof ACGroup) {
 			((ACGroup) orgUnit).getMembers().clear();
 		}
 	}
 
 	private boolean isServerAdmin(Role role) {
 		return role.eClass().getName().equals(RolesPackage.Literals.SERVER_ADMIN.getName());
 	}
 
 	private boolean areEqual(Role role, EClass roleClass) {
 		return role.eClass().getName().equals(roleClass.getName());
 	}
 
 	private ProjectInfo getProjectInfo(ProjectHistory project) {
 		final ProjectInfo info = ModelFactory.eINSTANCE.createProjectInfo();
 		info.setName(project.getProjectName());
 		info.setDescription(project.getProjectDescription());
 		info.setProjectId(ModelUtil.clone(project.getProjectId()));
 		info.setVersion(project.getLastVersion().getPrimarySpec());
 		return info;
 	}
 
 	private ACGroup getGroup(ACOrgUnitId orgUnitId) throws ESException {
 		for (final ACGroup group : daoFacade.getGroups()) {
 			if (group.getId().equals(orgUnitId)) {
 				return group;
 			}
 		}
 		throw new ESException(Messages.AdminEmfStoreImpl_Group_Does_Not_Exist);
 	}
 
 	private ACOrgUnit getOrgUnit(ACOrgUnitId orgUnitId) throws ESException {
 		for (final ACOrgUnit unit : daoFacade.getUsers()) {
 			if (unit.getId().equals(orgUnitId)) {
 				return unit;
 			}
 		}
 		for (final ACOrgUnit unit : daoFacade.getGroups()) {
 			if (unit.getId().equals(orgUnitId)) {
 				return unit;
 			}
 		}
 		throw new ESException(Messages.AdminEmfStoreImpl_OrgUnit_Does_Not_Exist);
 	}
 
 	private Role getRole(ProjectId projectId, ACOrgUnit orgUnit) {
 		for (final Role role : orgUnit.getRoles()) {
 			if (isServerAdmin(role) || role.getProjects().contains(projectId)) {
 				// return (Role) ModelUtil.clone(role);
 				return role;
 			}
 		}
 		return null;
 	}
 
 	private void save() throws ESException {
 		try {
 			daoFacade.save();
 		} catch (final IOException e) {
 			throw new StorageException(StorageException.NOSAVE, e);
 		} catch (final NullPointerException e) {
 			throw new StorageException(StorageException.NOSAVE, e);
 		}
 	}
 
 	private void checkForNulls(Object... objects) throws InvalidInputException {
 		for (final Object obj : objects) {
 			if (obj == null) {
 				throw new InvalidInputException();
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}.
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.server.core.AbstractEmfstoreInterface#initSubInterfaces()
 	 */
 	@Override
 	protected void initSubInterfaces() throws FatalESException {
 	}
 
 }
