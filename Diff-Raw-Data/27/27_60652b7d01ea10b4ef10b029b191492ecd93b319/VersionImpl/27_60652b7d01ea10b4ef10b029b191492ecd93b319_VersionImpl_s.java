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
 package org.eclipse.emf.emfstore.server.model.versioning.impl;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.BasicEMap;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.ModelFactory;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.impl.ProjectImpl;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.Version;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningPackage;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object ' <em><b>Version</b></em>'. <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>{@link org.eclipse.emf.emfstore.server.model.versioning.impl.VersionImpl#getProjectState <em>Project State</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.server.model.versioning.impl.VersionImpl#getPrimarySpec <em>Primary Spec</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.server.model.versioning.impl.VersionImpl#getTagSpecs <em>Tag Specs</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.server.model.versioning.impl.VersionImpl#getNextVersion <em>Next Version</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.server.model.versioning.impl.VersionImpl#getPreviousVersion <em>Previous Version
  * </em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.server.model.versioning.impl.VersionImpl#getChanges <em>Changes</em>}</li>
  * <li>{@link org.eclipse.emf.emfstore.server.model.versioning.impl.VersionImpl#getLogMessage <em>Log Message</em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public class VersionImpl extends EObjectImpl implements Version {
 	/**
 	 * The cached value of the '{@link #getProjectState() <em>Project State</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getProjectState()
 	 * @generated
 	 * @ordered
 	 */
 	protected Project projectState;
 
 	/**
 	 * The cached value of the '{@link #getPrimarySpec() <em>Primary Spec</em>}' containment reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getPrimarySpec()
 	 * @generated
 	 * @ordered
 	 */
 	protected PrimaryVersionSpec primarySpec;
 
 	/**
 	 * The cached value of the '{@link #getTagSpecs() <em>Tag Specs</em>}' containment reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getTagSpecs()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<TagVersionSpec> tagSpecs;
 
 	/**
 	 * The cached value of the '{@link #getNextVersion() <em>Next Version</em>}' reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getNextVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected Version nextVersion;
 
 	/**
 	 * The cached value of the '{@link #getPreviousVersion() <em>Previous Version</em>}' reference.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getPreviousVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected Version previousVersion;
 
 	/**
 	 * The cached value of the '{@link #getChanges() <em>Changes</em>}' containment reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getChanges()
 	 * @generated
 	 * @ordered
 	 */
 	protected ChangePackage changes;
 
 	/**
 	 * The cached value of the '{@link #getLogMessage() <em>Log Message</em>}' containment reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getLogMessage()
 	 * @generated
 	 * @ordered
 	 */
 	protected LogMessage logMessage;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected VersionImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return VersioningPackage.Literals.VERSION;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Project getProjectStateGen() {
 		if (projectState != null && projectState.eIsProxy()) {
 			InternalEObject oldProjectState = (InternalEObject) projectState;
 			projectState = (Project) eResolveProxy(oldProjectState);
 			if (projectState != oldProjectState) {
 				InternalEObject newProjectState = (InternalEObject) projectState;
 				NotificationChain msgs = oldProjectState.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__PROJECT_STATE, null, null);
 				if (newProjectState.eInternalContainer() == null) {
 					msgs = newProjectState.eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 						- VersioningPackage.VERSION__PROJECT_STATE, null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, VersioningPackage.VERSION__PROJECT_STATE,
 						oldProjectState, projectState));
 			}
 		}
 		return projectState;
 	}
 
 	// begin of custom code
 	/**
 	 * Retrieve the corresponding project state and init its ids. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @return the project representing this version or null if there is no full
 	 *         representation at this version (will not recalculate the state
 	 *         from previous versions)
 	 * @generated NOT
 	 */
 	public Project getProjectState() {
 		if ((projectState != null && projectState.eIsProxy())) {
 			ProjectImpl project = (ProjectImpl) getProjectStateGen();
 
 			Resource resource = project.eResource();
 			if (resource instanceof XMIResource) {
				EMap<EObject, ModelElementId> eObjectToIdMap = loadIdsFromResource((XMIResource) resource);
 
 				// create reverse mapping
 				Map<ModelElementId, EObject> idToEObjectMap = new HashMap<ModelElementId, EObject>(
 					eObjectToIdMap.size());
 
 				for (Map.Entry<EObject, ModelElementId> entry : eObjectToIdMap.entrySet()) {
 					idToEObjectMap.put(entry.getValue(), entry.getKey());
 				}
 
 				project.initCaches(eObjectToIdMap.map(), idToEObjectMap);
 			}
 
 			return project;
 		}
 
 		return getProjectStateGen();
 	}
 
 	// end of custom code
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Project basicGetProjectState() {
 		return projectState;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetProjectState(Project newProjectState, NotificationChain msgs) {
 		Project oldProjectState = projectState;
 		projectState = newProjectState;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				VersioningPackage.VERSION__PROJECT_STATE, oldProjectState, newProjectState);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setProjectState(Project newProjectState) {
 		if (newProjectState != projectState) {
 			NotificationChain msgs = null;
 			if (projectState != null)
 				msgs = ((InternalEObject) projectState).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__PROJECT_STATE, null, msgs);
 			if (newProjectState != null)
 				msgs = ((InternalEObject) newProjectState).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__PROJECT_STATE, null, msgs);
 			msgs = basicSetProjectState(newProjectState, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, VersioningPackage.VERSION__PROJECT_STATE,
 				newProjectState, newProjectState));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public PrimaryVersionSpec getPrimarySpec() {
 		if (primarySpec != null && primarySpec.eIsProxy()) {
 			InternalEObject oldPrimarySpec = (InternalEObject) primarySpec;
 			primarySpec = (PrimaryVersionSpec) eResolveProxy(oldPrimarySpec);
 			if (primarySpec != oldPrimarySpec) {
 				InternalEObject newPrimarySpec = (InternalEObject) primarySpec;
 				NotificationChain msgs = oldPrimarySpec.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__PRIMARY_SPEC, null, null);
 				if (newPrimarySpec.eInternalContainer() == null) {
 					msgs = newPrimarySpec.eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 						- VersioningPackage.VERSION__PRIMARY_SPEC, null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, VersioningPackage.VERSION__PRIMARY_SPEC,
 						oldPrimarySpec, primarySpec));
 			}
 		}
 		return primarySpec;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public PrimaryVersionSpec basicGetPrimarySpec() {
 		return primarySpec;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetPrimarySpec(PrimaryVersionSpec newPrimarySpec, NotificationChain msgs) {
 		PrimaryVersionSpec oldPrimarySpec = primarySpec;
 		primarySpec = newPrimarySpec;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				VersioningPackage.VERSION__PRIMARY_SPEC, oldPrimarySpec, newPrimarySpec);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setPrimarySpec(PrimaryVersionSpec newPrimarySpec) {
 		if (newPrimarySpec != primarySpec) {
 			NotificationChain msgs = null;
 			if (primarySpec != null)
 				msgs = ((InternalEObject) primarySpec).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__PRIMARY_SPEC, null, msgs);
 			if (newPrimarySpec != null)
 				msgs = ((InternalEObject) newPrimarySpec).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__PRIMARY_SPEC, null, msgs);
 			msgs = basicSetPrimarySpec(newPrimarySpec, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, VersioningPackage.VERSION__PRIMARY_SPEC,
 				newPrimarySpec, newPrimarySpec));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<TagVersionSpec> getTagSpecs() {
 		if (tagSpecs == null) {
 			tagSpecs = new EObjectContainmentEList.Resolving<TagVersionSpec>(TagVersionSpec.class, this,
 				VersioningPackage.VERSION__TAG_SPECS);
 		}
 		return tagSpecs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Version getNextVersion() {
 		if (nextVersion != null && nextVersion.eIsProxy()) {
 			InternalEObject oldNextVersion = (InternalEObject) nextVersion;
 			nextVersion = (Version) eResolveProxy(oldNextVersion);
 			if (nextVersion != oldNextVersion) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, VersioningPackage.VERSION__NEXT_VERSION,
 						oldNextVersion, nextVersion));
 			}
 		}
 		return nextVersion;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Version basicGetNextVersion() {
 		return nextVersion;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetNextVersion(Version newNextVersion, NotificationChain msgs) {
 		Version oldNextVersion = nextVersion;
 		nextVersion = newNextVersion;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				VersioningPackage.VERSION__NEXT_VERSION, oldNextVersion, newNextVersion);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setNextVersion(Version newNextVersion) {
 		if (newNextVersion != nextVersion) {
 			NotificationChain msgs = null;
 			if (nextVersion != null)
 				msgs = ((InternalEObject) nextVersion).eInverseRemove(this,
 					VersioningPackage.VERSION__PREVIOUS_VERSION, Version.class, msgs);
 			if (newNextVersion != null)
 				msgs = ((InternalEObject) newNextVersion).eInverseAdd(this,
 					VersioningPackage.VERSION__PREVIOUS_VERSION, Version.class, msgs);
 			msgs = basicSetNextVersion(newNextVersion, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, VersioningPackage.VERSION__NEXT_VERSION,
 				newNextVersion, newNextVersion));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Version getPreviousVersion() {
 		if (previousVersion != null && previousVersion.eIsProxy()) {
 			InternalEObject oldPreviousVersion = (InternalEObject) previousVersion;
 			previousVersion = (Version) eResolveProxy(oldPreviousVersion);
 			if (previousVersion != oldPreviousVersion) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE,
 						VersioningPackage.VERSION__PREVIOUS_VERSION, oldPreviousVersion, previousVersion));
 			}
 		}
 		return previousVersion;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Version basicGetPreviousVersion() {
 		return previousVersion;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetPreviousVersion(Version newPreviousVersion, NotificationChain msgs) {
 		Version oldPreviousVersion = previousVersion;
 		previousVersion = newPreviousVersion;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				VersioningPackage.VERSION__PREVIOUS_VERSION, oldPreviousVersion, newPreviousVersion);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setPreviousVersion(Version newPreviousVersion) {
 		if (newPreviousVersion != previousVersion) {
 			NotificationChain msgs = null;
 			if (previousVersion != null)
 				msgs = ((InternalEObject) previousVersion).eInverseRemove(this,
 					VersioningPackage.VERSION__NEXT_VERSION, Version.class, msgs);
 			if (newPreviousVersion != null)
 				msgs = ((InternalEObject) newPreviousVersion).eInverseAdd(this,
 					VersioningPackage.VERSION__NEXT_VERSION, Version.class, msgs);
 			msgs = basicSetPreviousVersion(newPreviousVersion, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, VersioningPackage.VERSION__PREVIOUS_VERSION,
 				newPreviousVersion, newPreviousVersion));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ChangePackage getChanges() {
 		if (changes != null && changes.eIsProxy()) {
 			InternalEObject oldChanges = (InternalEObject) changes;
 			changes = (ChangePackage) eResolveProxy(oldChanges);
 			if (changes != oldChanges) {
 				InternalEObject newChanges = (InternalEObject) changes;
 				NotificationChain msgs = oldChanges.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__CHANGES, null, null);
 				if (newChanges.eInternalContainer() == null) {
 					msgs = newChanges.eInverseAdd(this, EOPPOSITE_FEATURE_BASE - VersioningPackage.VERSION__CHANGES,
 						null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, VersioningPackage.VERSION__CHANGES,
 						oldChanges, changes));
 			}
 		}
 		return changes;
 	}
 
 	// begin of custom code
 	/**
 	 * Loads the XMI IDs from the given resource and returns them in a map
 	 * together with the object each ID belongs to.
 	 * 
 	 * @param resource
 	 *            the resource from which to load the ID mappings
 	 * @return a map consisting of object/id mappings, if the resource doesn't
 	 *         contain an eobject/id mapping null will be returned
 	 */
	private EMap<EObject, ModelElementId> loadIdsFromResource(XMIResource xmiResource) {
 
 		EMap<EObject, ModelElementId> eObjectToIdMap;
 
 		if (xmiResource != null) {
 			// guess a rough initial size by looking at the size of the contents
 			eObjectToIdMap = new BasicEMap<EObject, ModelElementId>(xmiResource.getContents().size());
 
			TreeIterator<EObject> it = xmiResource.getAllContents();
			while (it.hasNext()) {
				EObject obj = it.next();
				String objId = xmiResource.getID(obj);
 				if (objId != null) {
 					ModelElementId modelElementId = ModelFactory.eINSTANCE.createModelElementId();
 					modelElementId.setId(objId);
					eObjectToIdMap.put(obj, modelElementId);
 				}
 			}
 
			return eObjectToIdMap; // .map();
 		}
 
 		return null;
 	}
 
 	// end of custom code
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ChangePackage basicGetChanges() {
 		return changes;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetChanges(ChangePackage newChanges, NotificationChain msgs) {
 		ChangePackage oldChanges = changes;
 		changes = newChanges;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				VersioningPackage.VERSION__CHANGES, oldChanges, newChanges);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setChanges(ChangePackage newChanges) {
 		if (newChanges != changes) {
 			NotificationChain msgs = null;
 			if (changes != null)
 				msgs = ((InternalEObject) changes).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__CHANGES, null, msgs);
 			if (newChanges != null)
 				msgs = ((InternalEObject) newChanges).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__CHANGES, null, msgs);
 			msgs = basicSetChanges(newChanges, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, VersioningPackage.VERSION__CHANGES, newChanges,
 				newChanges));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public LogMessage getLogMessage() {
 		if (logMessage != null && logMessage.eIsProxy()) {
 			InternalEObject oldLogMessage = (InternalEObject) logMessage;
 			logMessage = (LogMessage) eResolveProxy(oldLogMessage);
 			if (logMessage != oldLogMessage) {
 				InternalEObject newLogMessage = (InternalEObject) logMessage;
 				NotificationChain msgs = oldLogMessage.eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__LOG_MESSAGE, null, null);
 				if (newLogMessage.eInternalContainer() == null) {
 					msgs = newLogMessage.eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 						- VersioningPackage.VERSION__LOG_MESSAGE, null, msgs);
 				}
 				if (msgs != null)
 					msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, VersioningPackage.VERSION__LOG_MESSAGE,
 						oldLogMessage, logMessage));
 			}
 		}
 		return logMessage;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public LogMessage basicGetLogMessage() {
 		return logMessage;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetLogMessage(LogMessage newLogMessage, NotificationChain msgs) {
 		LogMessage oldLogMessage = logMessage;
 		logMessage = newLogMessage;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET,
 				VersioningPackage.VERSION__LOG_MESSAGE, oldLogMessage, newLogMessage);
 			if (msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setLogMessage(LogMessage newLogMessage) {
 		if (newLogMessage != logMessage) {
 			NotificationChain msgs = null;
 			if (logMessage != null)
 				msgs = ((InternalEObject) logMessage).eInverseRemove(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__LOG_MESSAGE, null, msgs);
 			if (newLogMessage != null)
 				msgs = ((InternalEObject) newLogMessage).eInverseAdd(this, EOPPOSITE_FEATURE_BASE
 					- VersioningPackage.VERSION__LOG_MESSAGE, null, msgs);
 			msgs = basicSetLogMessage(newLogMessage, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, VersioningPackage.VERSION__LOG_MESSAGE,
 				newLogMessage, newLogMessage));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 		case VersioningPackage.VERSION__NEXT_VERSION:
 			if (nextVersion != null)
 				msgs = ((InternalEObject) nextVersion).eInverseRemove(this,
 					VersioningPackage.VERSION__PREVIOUS_VERSION, Version.class, msgs);
 			return basicSetNextVersion((Version) otherEnd, msgs);
 		case VersioningPackage.VERSION__PREVIOUS_VERSION:
 			if (previousVersion != null)
 				msgs = ((InternalEObject) previousVersion).eInverseRemove(this,
 					VersioningPackage.VERSION__NEXT_VERSION, Version.class, msgs);
 			return basicSetPreviousVersion((Version) otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 		case VersioningPackage.VERSION__PROJECT_STATE:
 			return basicSetProjectState(null, msgs);
 		case VersioningPackage.VERSION__PRIMARY_SPEC:
 			return basicSetPrimarySpec(null, msgs);
 		case VersioningPackage.VERSION__TAG_SPECS:
 			return ((InternalEList<?>) getTagSpecs()).basicRemove(otherEnd, msgs);
 		case VersioningPackage.VERSION__NEXT_VERSION:
 			return basicSetNextVersion(null, msgs);
 		case VersioningPackage.VERSION__PREVIOUS_VERSION:
 			return basicSetPreviousVersion(null, msgs);
 		case VersioningPackage.VERSION__CHANGES:
 			return basicSetChanges(null, msgs);
 		case VersioningPackage.VERSION__LOG_MESSAGE:
 			return basicSetLogMessage(null, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 		case VersioningPackage.VERSION__PROJECT_STATE:
 			if (resolve)
 				return getProjectState();
 			return basicGetProjectState();
 		case VersioningPackage.VERSION__PRIMARY_SPEC:
 			if (resolve)
 				return getPrimarySpec();
 			return basicGetPrimarySpec();
 		case VersioningPackage.VERSION__TAG_SPECS:
 			return getTagSpecs();
 		case VersioningPackage.VERSION__NEXT_VERSION:
 			if (resolve)
 				return getNextVersion();
 			return basicGetNextVersion();
 		case VersioningPackage.VERSION__PREVIOUS_VERSION:
 			if (resolve)
 				return getPreviousVersion();
 			return basicGetPreviousVersion();
 		case VersioningPackage.VERSION__CHANGES:
 			if (resolve)
 				return getChanges();
 			return basicGetChanges();
 		case VersioningPackage.VERSION__LOG_MESSAGE:
 			if (resolve)
 				return getLogMessage();
 			return basicGetLogMessage();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 		case VersioningPackage.VERSION__PROJECT_STATE:
 			setProjectState((Project) newValue);
 			return;
 		case VersioningPackage.VERSION__PRIMARY_SPEC:
 			setPrimarySpec((PrimaryVersionSpec) newValue);
 			return;
 		case VersioningPackage.VERSION__TAG_SPECS:
 			getTagSpecs().clear();
 			getTagSpecs().addAll((Collection<? extends TagVersionSpec>) newValue);
 			return;
 		case VersioningPackage.VERSION__NEXT_VERSION:
 			setNextVersion((Version) newValue);
 			return;
 		case VersioningPackage.VERSION__PREVIOUS_VERSION:
 			setPreviousVersion((Version) newValue);
 			return;
 		case VersioningPackage.VERSION__CHANGES:
 			setChanges((ChangePackage) newValue);
 			return;
 		case VersioningPackage.VERSION__LOG_MESSAGE:
 			setLogMessage((LogMessage) newValue);
 			return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 		case VersioningPackage.VERSION__PROJECT_STATE:
 			setProjectState((Project) null);
 			return;
 		case VersioningPackage.VERSION__PRIMARY_SPEC:
 			setPrimarySpec((PrimaryVersionSpec) null);
 			return;
 		case VersioningPackage.VERSION__TAG_SPECS:
 			getTagSpecs().clear();
 			return;
 		case VersioningPackage.VERSION__NEXT_VERSION:
 			setNextVersion((Version) null);
 			return;
 		case VersioningPackage.VERSION__PREVIOUS_VERSION:
 			setPreviousVersion((Version) null);
 			return;
 		case VersioningPackage.VERSION__CHANGES:
 			setChanges((ChangePackage) null);
 			return;
 		case VersioningPackage.VERSION__LOG_MESSAGE:
 			setLogMessage((LogMessage) null);
 			return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 		case VersioningPackage.VERSION__PROJECT_STATE:
 			return projectState != null;
 		case VersioningPackage.VERSION__PRIMARY_SPEC:
 			return primarySpec != null;
 		case VersioningPackage.VERSION__TAG_SPECS:
 			return tagSpecs != null && !tagSpecs.isEmpty();
 		case VersioningPackage.VERSION__NEXT_VERSION:
 			return nextVersion != null;
 		case VersioningPackage.VERSION__PREVIOUS_VERSION:
 			return previousVersion != null;
 		case VersioningPackage.VERSION__CHANGES:
 			return changes != null;
 		case VersioningPackage.VERSION__LOG_MESSAGE:
 			return logMessage != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 } // VersionImpl
