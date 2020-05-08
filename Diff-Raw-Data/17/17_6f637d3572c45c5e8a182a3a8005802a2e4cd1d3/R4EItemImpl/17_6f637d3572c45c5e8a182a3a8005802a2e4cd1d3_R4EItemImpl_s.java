 /**
  * Copyright (c) 2010, 2012 Ericsson
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * Contributors:
  * Alvaro Sanchez-Leon  - Initial API and implementation
  * 
  */
 package org.eclipse.mylyn.reviews.r4e.core.model.impl;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
 import org.eclipse.emf.ecore.util.EcoreEMap;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.mylyn.reviews.core.model.ILocation;
 import org.eclipse.mylyn.reviews.core.model.IReview;
 import org.eclipse.mylyn.reviews.core.model.IReviewItem;
 import org.eclipse.mylyn.reviews.core.model.ITopic;
 import org.eclipse.mylyn.reviews.core.model.IUser;
 import org.eclipse.mylyn.reviews.internal.core.model.ReviewsPackage;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileContext;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EItem;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelPackage;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>R4E Item</b></em>'. <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getAddedBy <em>Added By</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getReview <em>Review</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getName <em>Name</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getTopics <em>Topics</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getId <em>Id</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getDescription <em>Description</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getAddedById <em>Added By Id</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getFileContextList <em>File Context List</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getRepositoryRef <em>Repository Ref</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getProjectURIs <em>Project UR Is</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getAuthorRep <em>Author Rep</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getSubmitted <em>Submitted</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EItemImpl#getInfoAtt <em>Info Att</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class R4EItemImpl extends R4EIDComponentImpl implements R4EItem {
 	/**
 	 * The cached value of the '{@link #getAddedBy() <em>Added By</em>}' reference.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getAddedBy()
 	 * @generated
 	 * @ordered
 	 */
 	protected IUser addedBy;
 
 	/**
 	 * The cached value of the '{@link #getReview() <em>Review</em>}' reference.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getReview()
 	 * @generated
 	 * @ordered
 	 */
 	protected IReview review;
 
 	/**
 	 * The default value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String name = NAME_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getTopics() <em>Topics</em>}' reference list.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getTopics()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<ITopic> topics;
 
 	/**
 	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getId()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ID_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getId()
 	 * @generated
 	 * @ordered
 	 */
 	protected String id = ID_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DESCRIPTION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected String description = DESCRIPTION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getAddedById() <em>Added By Id</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getAddedById()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ADDED_BY_ID_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getAddedById() <em>Added By Id</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getAddedById()
 	 * @generated
 	 * @ordered
 	 */
 	protected String addedById = ADDED_BY_ID_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getFileContextList() <em>File Context List</em>}' containment reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getFileContextList()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<R4EFileContext> fileContextList;
 
 	/**
 	 * The default value of the '{@link #getRepositoryRef() <em>Repository Ref</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRepositoryRef()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String REPOSITORY_REF_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getRepositoryRef() <em>Repository Ref</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRepositoryRef()
 	 * @generated
 	 * @ordered
 	 */
 	protected String repositoryRef = REPOSITORY_REF_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getProjectURIs() <em>Project UR Is</em>}' attribute list.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @see #getProjectURIs()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<String> projectURIs;
 
 	/**
 	 * The default value of the '{@link #getAuthorRep() <em>Author Rep</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getAuthorRep()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String AUTHOR_REP_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getAuthorRep() <em>Author Rep</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getAuthorRep()
 	 * @generated
 	 * @ordered
 	 */
 	protected String authorRep = AUTHOR_REP_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getSubmitted() <em>Submitted</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getSubmitted()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Date SUBMITTED_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getSubmitted() <em>Submitted</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getSubmitted()
 	 * @generated
 	 * @ordered
 	 */
 	protected Date submitted = SUBMITTED_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getInfoAtt() <em>Info Att</em>}' map. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @see #getInfoAtt()
 	 * @generated
 	 * @ordered
 	 */
 	protected EMap<String, String> infoAtt;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected R4EItemImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return RModelPackage.Literals.R4E_ITEM;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IUser getAddedBy() {
 		if (addedBy != null && addedBy.eIsProxy()) {
 			InternalEObject oldAddedBy = (InternalEObject)addedBy;
 			addedBy = (IUser)eResolveProxy(oldAddedBy);
 			if (addedBy != oldAddedBy) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, RModelPackage.R4E_ITEM__ADDED_BY, oldAddedBy, addedBy));
 			}
 		}
 		return addedBy;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IUser basicGetAddedBy() {
 		return addedBy;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAddedBy(IUser newAddedBy) {
 		IUser oldAddedBy = addedBy;
 		addedBy = newAddedBy;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, RModelPackage.R4E_ITEM__ADDED_BY, oldAddedBy, addedBy));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IReview getReview() {
 		if (review != null && review.eIsProxy()) {
 			InternalEObject oldReview = (InternalEObject)review;
 			review = (IReview)eResolveProxy(oldReview);
 			if (review != oldReview) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, RModelPackage.R4E_ITEM__REVIEW, oldReview, review));
 			}
 		}
 		return review;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IReview basicGetReview() {
 		return review;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setReview(IReview newReview) {
 		IReview oldReview = review;
 		review = newReview;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, RModelPackage.R4E_ITEM__REVIEW, oldReview, review));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setName(String newName) {
 		String oldName = name;
 		name = newName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, RModelPackage.R4E_ITEM__NAME, oldName, name));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<ITopic> getTopics() {
 		if (topics == null) {
 			topics = new EObjectWithInverseResolvingEList<ITopic>(ITopic.class, this, RModelPackage.R4E_ITEM__TOPICS, ReviewsPackage.TOPIC__ITEM);
 		}
 		return topics;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setId(String newId) {
 		String oldId = id;
 		id = newId;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, RModelPackage.R4E_ITEM__ID, oldId, id));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDescription(String newDescription) {
 		String oldDescription = description;
 		description = newDescription;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, RModelPackage.R4E_ITEM__DESCRIPTION, oldDescription, description));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getAddedById() {
 		return addedById;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAddedById(String newAddedById) {
 		String oldAddedById = addedById;
 		addedById = newAddedById;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, RModelPackage.R4E_ITEM__ADDED_BY_ID, oldAddedById, addedById));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<R4EFileContext> getFileContextList() {
 		if (fileContextList == null) {
 			fileContextList = new EObjectContainmentEList.Resolving<R4EFileContext>(R4EFileContext.class, this, RModelPackage.R4E_ITEM__FILE_CONTEXT_LIST);
 		}
 		return fileContextList;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getRepositoryRef() {
 		return repositoryRef;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRepositoryRef(String newRepositoryRef) {
 		String oldRepositoryRef = repositoryRef;
 		repositoryRef = newRepositoryRef;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, RModelPackage.R4E_ITEM__REPOSITORY_REF, oldRepositoryRef, repositoryRef));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<String> getProjectURIs() {
 		if (projectURIs == null) {
 			projectURIs = new EDataTypeUniqueEList<String>(String.class, this, RModelPackage.R4E_ITEM__PROJECT_UR_IS);
 		}
 		return projectURIs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getAuthorRep() {
 		return authorRep;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAuthorRep(String newAuthorRep) {
 		String oldAuthorRep = authorRep;
 		authorRep = newAuthorRep;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, RModelPackage.R4E_ITEM__AUTHOR_REP, oldAuthorRep, authorRep));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Date getSubmitted() {
 		return submitted;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSubmitted(Date newSubmitted) {
 		Date oldSubmitted = submitted;
 		submitted = newSubmitted;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, RModelPackage.R4E_ITEM__SUBMITTED, oldSubmitted, submitted));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Map<String, String> getInfoAtt() {
 		if (infoAtt == null) {
 			infoAtt = new EcoreEMap<String,String>(RModelPackage.Literals.MAP_KEY_TO_INFO_ATTRIBUTES, MapKeyToInfoAttributesImpl.class, this, RModelPackage.R4E_ITEM__INFO_ATT);
 		}
 		return infoAtt.map();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ITopic createTopicComment(ILocation initalLocation, String commentText) {
 		// TODO: implement this method
 		// Ensure that you remove @generated or mark it @generated NOT
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case RModelPackage.R4E_ITEM__TOPICS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getTopics()).basicAdd(otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case RModelPackage.R4E_ITEM__TOPICS:
 				return ((InternalEList<?>)getTopics()).basicRemove(otherEnd, msgs);
 			case RModelPackage.R4E_ITEM__FILE_CONTEXT_LIST:
 				return ((InternalEList<?>)getFileContextList()).basicRemove(otherEnd, msgs);
 			case RModelPackage.R4E_ITEM__INFO_ATT:
 				return ((InternalEList<?>)((EMap.InternalMapView<String, String>)getInfoAtt()).eMap()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case RModelPackage.R4E_ITEM__ADDED_BY:
 				if (resolve) return getAddedBy();
 				return basicGetAddedBy();
 			case RModelPackage.R4E_ITEM__REVIEW:
 				if (resolve) return getReview();
 				return basicGetReview();
 			case RModelPackage.R4E_ITEM__NAME:
 				return getName();
 			case RModelPackage.R4E_ITEM__TOPICS:
 				return getTopics();
 			case RModelPackage.R4E_ITEM__ID:
 				return getId();
 			case RModelPackage.R4E_ITEM__DESCRIPTION:
 				return getDescription();
 			case RModelPackage.R4E_ITEM__ADDED_BY_ID:
 				return getAddedById();
 			case RModelPackage.R4E_ITEM__FILE_CONTEXT_LIST:
 				return getFileContextList();
 			case RModelPackage.R4E_ITEM__REPOSITORY_REF:
 				return getRepositoryRef();
 			case RModelPackage.R4E_ITEM__PROJECT_UR_IS:
 				return getProjectURIs();
 			case RModelPackage.R4E_ITEM__AUTHOR_REP:
 				return getAuthorRep();
 			case RModelPackage.R4E_ITEM__SUBMITTED:
 				return getSubmitted();
 			case RModelPackage.R4E_ITEM__INFO_ATT:
 				if (coreType) return ((EMap.InternalMapView<String, String>)getInfoAtt()).eMap();
 				else return getInfoAtt();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case RModelPackage.R4E_ITEM__ADDED_BY:
 				setAddedBy((IUser)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__REVIEW:
 				setReview((IReview)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__NAME:
 				setName((String)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__TOPICS:
 				getTopics().clear();
 				getTopics().addAll((Collection<? extends ITopic>)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__ID:
 				setId((String)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__DESCRIPTION:
 				setDescription((String)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__ADDED_BY_ID:
 				setAddedById((String)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__FILE_CONTEXT_LIST:
 				getFileContextList().clear();
 				getFileContextList().addAll((Collection<? extends R4EFileContext>)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__REPOSITORY_REF:
 				setRepositoryRef((String)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__PROJECT_UR_IS:
 				getProjectURIs().clear();
 				getProjectURIs().addAll((Collection<? extends String>)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__AUTHOR_REP:
 				setAuthorRep((String)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__SUBMITTED:
 				setSubmitted((Date)newValue);
 				return;
 			case RModelPackage.R4E_ITEM__INFO_ATT:
 				((EStructuralFeature.Setting)((EMap.InternalMapView<String, String>)getInfoAtt()).eMap()).set(newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case RModelPackage.R4E_ITEM__ADDED_BY:
 				setAddedBy((IUser)null);
 				return;
 			case RModelPackage.R4E_ITEM__REVIEW:
 				setReview((IReview)null);
 				return;
 			case RModelPackage.R4E_ITEM__NAME:
 				setName(NAME_EDEFAULT);
 				return;
 			case RModelPackage.R4E_ITEM__TOPICS:
 				getTopics().clear();
 				return;
 			case RModelPackage.R4E_ITEM__ID:
 				setId(ID_EDEFAULT);
 				return;
 			case RModelPackage.R4E_ITEM__DESCRIPTION:
 				setDescription(DESCRIPTION_EDEFAULT);
 				return;
 			case RModelPackage.R4E_ITEM__ADDED_BY_ID:
 				setAddedById(ADDED_BY_ID_EDEFAULT);
 				return;
 			case RModelPackage.R4E_ITEM__FILE_CONTEXT_LIST:
 				getFileContextList().clear();
 				return;
 			case RModelPackage.R4E_ITEM__REPOSITORY_REF:
 				setRepositoryRef(REPOSITORY_REF_EDEFAULT);
 				return;
 			case RModelPackage.R4E_ITEM__PROJECT_UR_IS:
 				getProjectURIs().clear();
 				return;
 			case RModelPackage.R4E_ITEM__AUTHOR_REP:
 				setAuthorRep(AUTHOR_REP_EDEFAULT);
 				return;
 			case RModelPackage.R4E_ITEM__SUBMITTED:
 				setSubmitted(SUBMITTED_EDEFAULT);
 				return;
 			case RModelPackage.R4E_ITEM__INFO_ATT:
 				getInfoAtt().clear();
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case RModelPackage.R4E_ITEM__ADDED_BY:
 				return addedBy != null;
 			case RModelPackage.R4E_ITEM__REVIEW:
 				return review != null;
 			case RModelPackage.R4E_ITEM__NAME:
 				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
 			case RModelPackage.R4E_ITEM__TOPICS:
 				return topics != null && !topics.isEmpty();
 			case RModelPackage.R4E_ITEM__ID:
 				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
 			case RModelPackage.R4E_ITEM__DESCRIPTION:
 				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
 			case RModelPackage.R4E_ITEM__ADDED_BY_ID:
 				return ADDED_BY_ID_EDEFAULT == null ? addedById != null : !ADDED_BY_ID_EDEFAULT.equals(addedById);
 			case RModelPackage.R4E_ITEM__FILE_CONTEXT_LIST:
 				return fileContextList != null && !fileContextList.isEmpty();
 			case RModelPackage.R4E_ITEM__REPOSITORY_REF:
 				return REPOSITORY_REF_EDEFAULT == null ? repositoryRef != null : !REPOSITORY_REF_EDEFAULT.equals(repositoryRef);
 			case RModelPackage.R4E_ITEM__PROJECT_UR_IS:
 				return projectURIs != null && !projectURIs.isEmpty();
 			case RModelPackage.R4E_ITEM__AUTHOR_REP:
 				return AUTHOR_REP_EDEFAULT == null ? authorRep != null : !AUTHOR_REP_EDEFAULT.equals(authorRep);
 			case RModelPackage.R4E_ITEM__SUBMITTED:
 				return SUBMITTED_EDEFAULT == null ? submitted != null : !SUBMITTED_EDEFAULT.equals(submitted);
 			case RModelPackage.R4E_ITEM__INFO_ATT:
 				return infoAtt != null && !infoAtt.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
 		if (baseClass == IReviewItem.class) {
 			switch (derivedFeatureID) {
 				case RModelPackage.R4E_ITEM__ADDED_BY: return ReviewsPackage.REVIEW_ITEM__ADDED_BY;
 				case RModelPackage.R4E_ITEM__REVIEW: return ReviewsPackage.REVIEW_ITEM__REVIEW;
 				case RModelPackage.R4E_ITEM__NAME: return ReviewsPackage.REVIEW_ITEM__NAME;
 				case RModelPackage.R4E_ITEM__TOPICS: return ReviewsPackage.REVIEW_ITEM__TOPICS;
 				case RModelPackage.R4E_ITEM__ID: return ReviewsPackage.REVIEW_ITEM__ID;
 				default: return -1;
 			}
 		}
 		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
 		if (baseClass == IReviewItem.class) {
 			switch (baseFeatureID) {
 				case ReviewsPackage.REVIEW_ITEM__ADDED_BY: return RModelPackage.R4E_ITEM__ADDED_BY;
 				case ReviewsPackage.REVIEW_ITEM__REVIEW: return RModelPackage.R4E_ITEM__REVIEW;
 				case ReviewsPackage.REVIEW_ITEM__NAME: return RModelPackage.R4E_ITEM__NAME;
 				case ReviewsPackage.REVIEW_ITEM__TOPICS: return RModelPackage.R4E_ITEM__TOPICS;
 				case ReviewsPackage.REVIEW_ITEM__ID: return RModelPackage.R4E_ITEM__ID;
 				default: return -1;
 			}
 		}
 		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (name: ");
 		result.append(name);
 		result.append(", id: ");
 		result.append(id);
 		result.append(", description: ");
 		result.append(description);
 		result.append(", addedById: ");
 		result.append(addedById);
 		result.append(", repositoryRef: ");
 		result.append(repositoryRef);
 		result.append(", ProjectURIs: ");
 		result.append(projectURIs);
 		result.append(", authorRep: ");
 		result.append(authorRep);
 		result.append(", submitted: ");
 		result.append(submitted);
 		result.append(')');
 		return result.toString();
 	}
 
 } //R4EItemImpl
