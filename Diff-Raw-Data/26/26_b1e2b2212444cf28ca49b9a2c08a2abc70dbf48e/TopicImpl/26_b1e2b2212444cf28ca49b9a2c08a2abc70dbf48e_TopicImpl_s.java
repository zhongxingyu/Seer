 /*******************************************************************************
  * Copyright (c) 2010 Ericsson
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * Contributors:
  *   Alvaro Sanchez-Leon - Initial Implementation and API
  *******************************************************************************/
 package org.eclipse.mylyn.reviews.frame.core.model.impl;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.mylyn.reviews.frame.core.model.Comment;
 import org.eclipse.mylyn.reviews.frame.core.model.Location;
 import org.eclipse.mylyn.reviews.frame.core.model.ModelPackage;
 import org.eclipse.mylyn.reviews.frame.core.model.Review;
 import org.eclipse.mylyn.reviews.frame.core.model.TaskReference;
 import org.eclipse.mylyn.reviews.frame.core.model.Topic;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Topic</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.mylyn.reviews.frame.core.model.impl.TopicImpl#getTask <em>Task</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.frame.core.model.impl.TopicImpl#getLocation <em>Location</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.frame.core.model.impl.TopicImpl#getComments <em>Comments</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.frame.core.model.impl.TopicImpl#getReview <em>Review</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.frame.core.model.impl.TopicImpl#getTitle <em>Title</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class TopicImpl extends CommentImpl implements Topic {
 	/**
 	 * The cached value of the '{@link #getTask() <em>Task</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTask()
 	 * @generated
 	 * @ordered
 	 */
 	protected TaskReference task;
 
 	/**
 	 * The cached value of the '{@link #getLocation() <em>Location</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLocation()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Location> location;
 
 	/**
 	 * The cached value of the '{@link #getComments() <em>Comments</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getComments()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Comment> comments;
 
 	/**
 	 * The cached value of the '{@link #getReview() <em>Review</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getReview()
 	 * @generated
 	 * @ordered
 	 */
 	protected Review review;
 
 	/**
 	 * The default value of the '{@link #getTitle() <em>Title</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTitle()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String TITLE_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getTitle() <em>Title</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTitle()
 	 * @generated
 	 * @ordered
 	 */
 	protected String title = TITLE_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected TopicImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ModelPackage.Literals.TOPIC;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TaskReference getTask() {
 		if (task != null && task.eIsProxy()) {
 			InternalEObject oldTask = (InternalEObject)task;
 			task = (TaskReference)eResolveProxy(oldTask);
 			if (task != oldTask) {
 				InternalEObject newTask = (InternalEObject)task;
 				NotificationChain msgs = oldTask.eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelPackage.TOPIC__TASK, null, null);
 				if (newTask.eInternalContainer() == null) {
 					msgs = newTask.eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.TOPIC__TASK, null, msgs);
 				}
 				if (msgs != null) msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.TOPIC__TASK, oldTask, task));
 			}
 		}
 		return task;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TaskReference basicGetTask() {
 		return task;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetTask(TaskReference newTask, NotificationChain msgs) {
 		TaskReference oldTask = task;
 		task = newTask;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelPackage.TOPIC__TASK, oldTask, newTask);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTask(TaskReference newTask) {
 		if (newTask != task) {
 			NotificationChain msgs = null;
 			if (task != null)
 				msgs = ((InternalEObject)task).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelPackage.TOPIC__TASK, null, msgs);
 			if (newTask != null)
 				msgs = ((InternalEObject)newTask).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.TOPIC__TASK, null, msgs);
 			msgs = basicSetTask(newTask, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.TOPIC__TASK, newTask, newTask));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Location> getLocation() {
 		if (location == null) {
 			location = new EObjectContainmentEList.Resolving<Location>(Location.class, this, ModelPackage.TOPIC__LOCATION);
 		}
 		return location;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Comment> getComments() {
 		if (comments == null) {
 			comments = new EObjectResolvingEList<Comment>(Comment.class, this, ModelPackage.TOPIC__COMMENTS);
 		}
 		return comments;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Review getReview() {
 		if (review != null && review.eIsProxy()) {
 			InternalEObject oldReview = (InternalEObject)review;
 			review = (Review)eResolveProxy(oldReview);
 			if (review != oldReview) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.TOPIC__REVIEW, oldReview, review));
 			}
 		}
 		return review;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Review basicGetReview() {
 		return review;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setReview(Review newReview) {
 		Review oldReview = review;
 		review = newReview;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.TOPIC__REVIEW, oldReview, review));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getTitle() {
		return title;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTitle(String newTitle) {
 		String oldTitle = title;
 		title = newTitle;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.TOPIC__TITLE, oldTitle, title));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case ModelPackage.TOPIC__TASK:
 				return basicSetTask(null, msgs);
 			case ModelPackage.TOPIC__LOCATION:
 				return ((InternalEList<?>)getLocation()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case ModelPackage.TOPIC__TASK:
 				if (resolve) return getTask();
 				return basicGetTask();
 			case ModelPackage.TOPIC__LOCATION:
 				return getLocation();
 			case ModelPackage.TOPIC__COMMENTS:
 				return getComments();
 			case ModelPackage.TOPIC__REVIEW:
 				if (resolve) return getReview();
 				return basicGetReview();
 			case ModelPackage.TOPIC__TITLE:
 				return getTitle();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case ModelPackage.TOPIC__TASK:
 				setTask((TaskReference)newValue);
 				return;
 			case ModelPackage.TOPIC__LOCATION:
 				getLocation().clear();
 				getLocation().addAll((Collection<? extends Location>)newValue);
 				return;
 			case ModelPackage.TOPIC__COMMENTS:
 				getComments().clear();
 				getComments().addAll((Collection<? extends Comment>)newValue);
 				return;
 			case ModelPackage.TOPIC__REVIEW:
 				setReview((Review)newValue);
 				return;
 			case ModelPackage.TOPIC__TITLE:
 				setTitle((String)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case ModelPackage.TOPIC__TASK:
 				setTask((TaskReference)null);
 				return;
 			case ModelPackage.TOPIC__LOCATION:
 				getLocation().clear();
 				return;
 			case ModelPackage.TOPIC__COMMENTS:
 				getComments().clear();
 				return;
 			case ModelPackage.TOPIC__REVIEW:
 				setReview((Review)null);
 				return;
 			case ModelPackage.TOPIC__TITLE:
 				setTitle(TITLE_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case ModelPackage.TOPIC__TASK:
 				return task != null;
 			case ModelPackage.TOPIC__LOCATION:
 				return location != null && !location.isEmpty();
 			case ModelPackage.TOPIC__COMMENTS:
 				return comments != null && !comments.isEmpty();
 			case ModelPackage.TOPIC__REVIEW:
 				return review != null;
 			case ModelPackage.TOPIC__TITLE:
 				return TITLE_EDEFAULT == null ? title != null : !TITLE_EDEFAULT.equals(title);
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (title: ");
 		result.append(title);
 		result.append(')');
 		return result.toString();
 	}
 
 } //TopicImpl
