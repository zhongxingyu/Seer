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
 package org.eclipse.emf.emfstore.client.properties;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.accesscontrol.AccessControlHelper;
 import org.eclipse.emf.emfstore.common.model.EMFStoreProperty;
 import org.eclipse.emf.emfstore.common.model.EMFStorePropertyType;
 import org.eclipse.emf.emfstore.common.model.PropertyStringValue;
 import org.eclipse.emf.emfstore.server.connection.xmlrpc.util.StaticOperationFactory;
 import org.eclipse.emf.emfstore.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 
 /**
  * This class handles shared and local properties which are bundled to the
  * project space.
  * 
  * @author haunolder
  * 
  **/
 public final class PropertyManager {
 
 	private final ProjectSpace projectSpace;
 	private Map<String, EObject> sharedProperties;
 	private Map<String, EObject> localProperties;
 
 	/**
 	 * PropertyManager constructor.
 	 * 
 	 * @param projectSpace
 	 *            projectSpace for this PropertyManager ProjectSpace
 	 **/
 	public PropertyManager(ProjectSpace projectSpace) {
 		this.projectSpace = projectSpace;
 	}
 
 	/**
 	 * Set a local property. If the property already exists it will be updated.
 	 * 
 	 * @param key
 	 *            of the local property as String
 	 * @param value
 	 *            of the local property as EObject
 	 **/
 	public void setLocalProperty(String key, EObject value) {
 		EMFStoreProperty prop = createProperty(key, value);
 		prop.setType(EMFStorePropertyType.LOCAL);
 		this.projectSpace.getProperties().add(prop);
 
 		if (this.localProperties == null) {
 			this.localProperties = new HashMap<String, EObject>();
 			createMap(this.localProperties, EMFStorePropertyType.LOCAL);
 		}
 
 		this.localProperties.put(key, value);
 
 	}
 
 	/**
 	 * Get a local property.
 	 * 
 	 * @param key
 	 *            of the local property
 	 * @return EObject the local property
 	 **/
 	public EObject getLocalProperty(String key) {
 		if (this.localProperties == null) {
 			this.localProperties = new HashMap<String, EObject>();
 			createMap(this.localProperties, EMFStorePropertyType.LOCAL);
 		}
 		return getPropertyValue(this.localProperties, key);
 	}
 
 	/**
 	 * Set a local string property. If the property already exists it will be
 	 * updated.
 	 * 
 	 * @param key
 	 *            of the local property
 	 * @param value
 	 *            of the local property
 	 **/
 	public void setLocalStringProperty(String key, String value) {
 		PropertyStringValue propertyValue = org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE
 			.createPropertyStringValue();
 		propertyValue.setValue(value);
 		setLocalProperty(key, propertyValue);
 	}
 
 	/**
 	 * Get a local string property.
 	 * 
 	 * @param key
 	 *            of the local property
 	 * @return property value as String
 	 * 
 	 **/
 	public String getLocalStringProperty(String key) {
 		PropertyStringValue propertyValue = (PropertyStringValue) getLocalProperty(key);
 		if (propertyValue != null) {
 			return propertyValue.getValue();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Set a shared string property. It will be transmitted to the server. If
 	 * the property already exists it will be updated.
 	 * 
 	 * @param key
 	 *            of the shared property as String
 	 * @param value
 	 *            of the shared property as String
 	 * 
 	 **/
 	public void setSharedStringProperty(String key, String value) {
 		PropertyStringValue propertyValue = org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE
 			.createPropertyStringValue();
 		propertyValue.setValue(value);
 		setSharedProperty(key, propertyValue);
 	}
 
 	/**
 	 * Get shared string property.
 	 * 
 	 * @param key
 	 *            of the shared property as String
 	 * @return value of the shared property as String
 	 **/
 	public String getSharedStringProperty(String key) {
 		if (key != null) {
 			PropertyStringValue propertyValue = (PropertyStringValue) getSharedProperty(key);
 			if (propertyValue != null) {
 				return propertyValue.getValue();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Set shared property which is transmitted to the server.
 	 * 
 	 * @param key
 	 *            of the shared property as String
 	 * @param value
 	 *            of the shared property as EObject
 	 **/
 	public void setSharedProperty(String key, EObject value) {
 		EMFStoreProperty prop = createProperty(key, value);
 		prop.setType(EMFStorePropertyType.SHARED);
 		this.projectSpace.getProperties().add(prop);
 		this.projectSpace.getChangedSharedProperties().add(prop);
 
 		if (this.sharedProperties == null) {
 			this.sharedProperties = new HashMap<String, EObject>();
 			createMap(this.sharedProperties, EMFStorePropertyType.SHARED);
 		}
 
 		this.sharedProperties.put(key, value);
 	}
 
 	/**
 	 * Get shared property.
 	 * 
 	 * @param key
 	 *            of the shared property as String
 	 * @return value of the shared property as EObject
 	 **/
 	public EObject getSharedProperty(String key) {
 		if (this.sharedProperties == null) {
 			this.sharedProperties = new HashMap<String, EObject>();
 			createMap(this.sharedProperties, EMFStorePropertyType.SHARED);
 		}
 		return getPropertyValue(this.sharedProperties, key);
 	}
 
 	/**
 	 * Transmit changed shared properties to the server. Clears the
 	 * changedSharedProperties List and fills shareProperties with the actual
 	 * properties from the server.
 	 * 
 	 * @throws EmfStoreException
 	 *             if any error occurs in the EmfStore
 	 **/
 	public void transmit() throws EmfStoreException {
 
 		try {
 			new AccessControlHelper(projectSpace.getUsersession()).hasPermission(StaticOperationFactory
				.createWritePropertiesOperation(projectSpace.getProjectId(), null));
 		} catch (AccessControlException e) {
 			// do not transmit properties if user is a reader
 			return;
 		}
 
 		List<EMFStoreProperty> changedProperties = new ArrayList<EMFStoreProperty>();
 
 		for (EMFStoreProperty prop : this.projectSpace.getChangedSharedProperties()) {
 			changedProperties.add(prop);
 		}
 
 		WorkspaceManager
 			.getInstance()
 			.getConnectionManager()
 			.transmitEMFProperties(this.projectSpace.getUsersession().getSessionId(), changedProperties,
 				this.projectSpace.getProjectId());
 
 		this.projectSpace.getChangedSharedProperties().clear();
 
 		List<EMFStoreProperty> sharedProperties = WorkspaceManager.getInstance().getConnectionManager()
 			.getEMFProperties(this.projectSpace.getUsersession().getSessionId(), this.projectSpace.getProjectId());
 
 		for (EMFStoreProperty prop : sharedProperties) {
 			setUpdatedSharedProperty(prop.getKey(), prop.getValue());
 		}
 	}
 
 	private EMFStoreProperty createProperty(String key, EObject value) {
 		EMFStoreProperty prop = org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE.createEMFStoreProperty();
 		prop.setKey(key);
 		prop.setValue(value);
 		return prop;
 	}
 
 	private void createMap(Map<String, EObject> map, EMFStorePropertyType type) {
 		EList<EMFStoreProperty> persistendProperties = this.projectSpace.getProperties();
 		for (EMFStoreProperty prop : persistendProperties) {
 			if (prop.getType() == type) {
 				map.put(prop.getKey(), prop.getValue());
 			}
 		}
 	}
 
 	private EObject getPropertyValue(Map<String, EObject> map, String key) {
 		if (map.containsKey(key)) {
 			return map.get(key);
 		} else {
 			return null;
 		}
 	}
 
 	private void setUpdatedSharedProperty(String key, EObject value) {
 		EMFStoreProperty prop = createProperty(key, value);
 		prop.setType(EMFStorePropertyType.SHARED);
 		this.projectSpace.getProperties().add(prop);
 
 		if (this.sharedProperties == null) {
 			this.sharedProperties = new HashMap<String, EObject>();
 			createMap(this.sharedProperties, EMFStorePropertyType.SHARED);
 		}
 
 		this.sharedProperties.put(key, value);
 	}
 
 }
