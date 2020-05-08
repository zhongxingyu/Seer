 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  * 
  * 
  * 
  */
 package org.eclipse.emf.emfstore.server.model.accesscontrol.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACGroup;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACOrgUnit;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACUser;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.AccesscontrolPackage;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.PermissionSet;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.PermissionType;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.Role;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Permission Set</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>{@link org.eclipse.emf.emfstore.server.model.accesscontrol.impl.PermissionSetImpl#getPermissionTypes <em>
  * Permission Types</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.server.model.accesscontrol.impl.PermissionSetImpl#getRoles <em>Roles</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.server.model.accesscontrol.impl.PermissionSetImpl#getGroups <em>Groups</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.server.model.accesscontrol.impl.PermissionSetImpl#getUsers <em>Users</em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public class PermissionSetImpl extends EObjectImpl implements PermissionSet {
 	/**
 	 * The cached value of the '{@link #getPermissionTypes() <em>Permission Types</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getPermissionTypes()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<PermissionType> permissionTypes;
 
 	/**
 	 * The cached value of the '{@link #getRoles() <em>Roles</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getRoles()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Role> roles;
 
 	/**
 	 * The cached value of the '{@link #getGroups() <em>Groups</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getGroups()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<ACGroup> groups;
 
 	/**
 	 * The cached value of the '{@link #getUsers() <em>Users</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getUsers()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<ACUser> users;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected PermissionSetImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return AccesscontrolPackage.Literals.PERMISSION_SET;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<PermissionType> getPermissionTypes() {
 		if (permissionTypes == null) {
 			permissionTypes = new EObjectContainmentEList.Resolving<PermissionType>(PermissionType.class, this,
 				AccesscontrolPackage.PERMISSION_SET__PERMISSION_TYPES);
 		}
 		return permissionTypes;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<Role> getRoles() {
 		if (roles == null) {
 			roles = new EObjectContainmentEList.Resolving<Role>(Role.class, this,
 				AccesscontrolPackage.PERMISSION_SET__ROLES);
 		}
 		return roles;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<ACGroup> getGroups() {
 		if (groups == null) {
 			groups = new EObjectContainmentEList.Resolving<ACGroup>(ACGroup.class, this,
 				AccesscontrolPackage.PERMISSION_SET__GROUPS);
 		}
 		return groups;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<ACUser> getUsers() {
 		if (users == null) {
 			users = new EObjectContainmentEList.Resolving<ACUser>(ACUser.class, this,
 				AccesscontrolPackage.PERMISSION_SET__USERS);
 		}
 		return users;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 		case AccesscontrolPackage.PERMISSION_SET__PERMISSION_TYPES:
 			return ((InternalEList<?>) getPermissionTypes()).basicRemove(otherEnd, msgs);
 		case AccesscontrolPackage.PERMISSION_SET__ROLES:
 			return ((InternalEList<?>) getRoles()).basicRemove(otherEnd, msgs);
 		case AccesscontrolPackage.PERMISSION_SET__GROUPS:
 			return ((InternalEList<?>) getGroups()).basicRemove(otherEnd, msgs);
 		case AccesscontrolPackage.PERMISSION_SET__USERS:
 			return ((InternalEList<?>) getUsers()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 		case AccesscontrolPackage.PERMISSION_SET__PERMISSION_TYPES:
 			return getPermissionTypes();
 		case AccesscontrolPackage.PERMISSION_SET__ROLES:
 			return getRoles();
 		case AccesscontrolPackage.PERMISSION_SET__GROUPS:
 			return getGroups();
 		case AccesscontrolPackage.PERMISSION_SET__USERS:
 			return getUsers();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 		case AccesscontrolPackage.PERMISSION_SET__PERMISSION_TYPES:
 			getPermissionTypes().clear();
 			getPermissionTypes().addAll((Collection<? extends PermissionType>) newValue);
 			return;
 		case AccesscontrolPackage.PERMISSION_SET__ROLES:
 			getRoles().clear();
 			getRoles().addAll((Collection<? extends Role>) newValue);
 			return;
 		case AccesscontrolPackage.PERMISSION_SET__GROUPS:
 			getGroups().clear();
 			getGroups().addAll((Collection<? extends ACGroup>) newValue);
 			return;
 		case AccesscontrolPackage.PERMISSION_SET__USERS:
 			getUsers().clear();
 			getUsers().addAll((Collection<? extends ACUser>) newValue);
 			return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 		case AccesscontrolPackage.PERMISSION_SET__PERMISSION_TYPES:
 			getPermissionTypes().clear();
 			return;
 		case AccesscontrolPackage.PERMISSION_SET__ROLES:
 			getRoles().clear();
 			return;
 		case AccesscontrolPackage.PERMISSION_SET__GROUPS:
 			getGroups().clear();
 			return;
 		case AccesscontrolPackage.PERMISSION_SET__USERS:
 			getUsers().clear();
 			return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 		case AccesscontrolPackage.PERMISSION_SET__PERMISSION_TYPES:
 			return permissionTypes != null && !permissionTypes.isEmpty();
 		case AccesscontrolPackage.PERMISSION_SET__ROLES:
 			return roles != null && !roles.isEmpty();
 		case AccesscontrolPackage.PERMISSION_SET__GROUPS:
 			return groups != null && !groups.isEmpty();
 		case AccesscontrolPackage.PERMISSION_SET__USERS:
 			return users != null && !users.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.server.model.accesscontrol.PermissionSet#getOrgUnit(java.lang.String)
 	 * @generated NOT
 	 */
 	public ACOrgUnit getOrgUnit(String name) {
 		for (ACUser user : getUsers()) {
 			if (user.getName().equals(name)) {
 				return user;
 			}
 		}
 		for (ACGroup group : getGroups()) {
 			if (group.getName().equals(name)) {
 				return group;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.server.model.accesscontrol.PermissionSet#getRole(java.lang.String)
 	 * @generated NOT
 	 */
 	public Role getRole(String roleName) {
 		for (Role role : getRoles()) {
 			if (role.getId().equals(roleName)) {
 				return role;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.server.model.accesscontrol.PermissionSet#getProjectRoles()
 	 * @generated NOT
 	 */
 	public Collection<Role> getProjectRoles() {
 		List<Role> projectRoles = new ArrayList<Role>();
 
 		ROLES: for (Role role : getRoles()) {
 			if (role.isSystemRole()) {
 				continue;
 			}
 			for (PermissionType type : role.getPermissionTypes()) {
				if (type.isProjectRole()) {
 					projectRoles.add(role);
 					continue ROLES;
 				}
 			}
 		}
 
 		return Collections.unmodifiableList(projectRoles);
 	}
 } // PermissionSetImpl
