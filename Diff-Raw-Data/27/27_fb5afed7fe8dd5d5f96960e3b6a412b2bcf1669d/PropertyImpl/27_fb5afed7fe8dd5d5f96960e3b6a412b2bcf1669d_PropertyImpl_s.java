 /**
  * <copyright>
  *
  * Copyright (c) 2010,2011 E.D.Willink and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   E.D.Willink - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: PropertyImpl.java,v 1.9 2011/05/19 16:55:39 ewillink Exp $
  */
 package org.eclipse.ocl.examples.pivot.internal.impl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 import org.eclipse.emf.ecore.util.EObjectValidator;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.elements.DomainInheritance;
 import org.eclipse.ocl.examples.domain.elements.DomainStandardLibrary;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.evaluation.DomainEvaluator;
 import org.eclipse.ocl.examples.domain.ids.PropertyId;
 import org.eclipse.ocl.examples.domain.ids.TypeId;
 import org.eclipse.ocl.examples.domain.library.LibraryFeature;
 import org.eclipse.ocl.examples.domain.messages.EvaluatorMessages;
 import org.eclipse.ocl.examples.domain.types.IdResolver;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.domain.values.OrderedSetValue;
 import org.eclipse.ocl.examples.domain.values.OrderedSetValue.Accumulator;
 import org.eclipse.ocl.examples.domain.values.impl.InvalidValueException;
 import org.eclipse.ocl.examples.domain.values.util.ValuesUtil;
 import org.eclipse.ocl.examples.library.classifier.ClassifierOclContainerOperation;
 import org.eclipse.ocl.examples.library.collection.CollectionIncludesOperation;
 import org.eclipse.ocl.examples.library.collection.CollectionNotEmptyOperation;
 import org.eclipse.ocl.examples.library.ecore.EcoreExecutorManager;
 import org.eclipse.ocl.examples.library.logical.BooleanAndOperation;
 import org.eclipse.ocl.examples.library.logical.BooleanImpliesOperation;
 import org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation;
 import org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation;
 import org.eclipse.ocl.examples.library.oclany.OclAnyOclAsTypeOperation;
 import org.eclipse.ocl.examples.library.oclany.OclAnyOclIsKindOfOperation;
 import org.eclipse.ocl.examples.library.oclany.OclAnyOclTypeOperation;
 import org.eclipse.ocl.examples.pivot.Annotation;
 import org.eclipse.ocl.examples.pivot.AssociationClass;
 import org.eclipse.ocl.examples.pivot.Comment;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.ElementExtension;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.ParameterableElement;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.PivotTables;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.TemplateParameter;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypedMultiplicityElement;
 import org.eclipse.ocl.examples.pivot.ValueSpecification;
 import org.eclipse.ocl.examples.pivot.util.PivotValidator;
 import org.eclipse.ocl.examples.pivot.util.Visitor;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Property</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#getTemplateParameter <em>Template Parameter</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#getOwningTemplateParameter <em>Owning Template Parameter</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#isReadOnly <em>Is Read Only</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#getDefault <em>Default</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#isComposite <em>Is Composite</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#isDerived <em>Is Derived</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#getClass_ <em>Class</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#getOpposite <em>Opposite</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#getAssociation <em>Association</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#isImplicit <em>Implicit</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#isID <em>Is ID</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#getKeys <em>Keys</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#isResolveProxies <em>Is Resolve Proxies</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#isTransient <em>Is Transient</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#isUnsettable <em>Is Unsettable</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#isVolatile <em>Is Volatile</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#getRedefinedProperty <em>Redefined Property</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#getSubsettedProperty <em>Subsetted Property</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#getReferredProperty <em>Referred Property</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.PropertyImpl#getOwningType <em>Owning Type</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 @SuppressWarnings("cast")
 public class PropertyImpl
 		extends FeatureImpl
 		implements Property {
 
 	/**
 	 * The cached value of the '{@link #getTemplateParameter() <em>Template Parameter</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTemplateParameter()
 	 * @generated
 	 * @ordered
 	 */
 	protected TemplateParameter templateParameter;
 
 	/**
 	 * The default value of the '{@link #isReadOnly() <em>Is Read Only</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isReadOnly()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IS_READ_ONLY_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isReadOnly() <em>Is Read Only</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isReadOnly()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int IS_READ_ONLY_EFLAG = 1 << 10;
 
 	/**
 	 * The default value of the '{@link #getDefault() <em>Default</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDefault()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DEFAULT_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDefault() <em>Default</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDefault()
 	 * @generated
 	 * @ordered
 	 */
 	protected String default_ = DEFAULT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isComposite() <em>Is Composite</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isComposite()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IS_COMPOSITE_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isComposite() <em>Is Composite</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isComposite()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int IS_COMPOSITE_EFLAG = 1 << 11;
 
 	/**
 	 * The default value of the '{@link #isDerived() <em>Is Derived</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isDerived()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IS_DERIVED_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isDerived() <em>Is Derived</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isDerived()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int IS_DERIVED_EFLAG = 1 << 12;
 
 	/**
 	 * The cached value of the '{@link #getOpposite() <em>Opposite</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOpposite()
 	 * @generated
 	 * @ordered
 	 */
 	protected Property opposite;
 
 	/**
 	 * The cached value of the '{@link #getAssociation() <em>Association</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAssociation()
 	 * @generated
 	 * @ordered
 	 */
 	protected AssociationClass association;
 
 	/**
 	 * The default value of the '{@link #isImplicit() <em>Implicit</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isImplicit()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IMPLICIT_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isImplicit() <em>Implicit</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isImplicit()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int IMPLICIT_EFLAG = 1 << 13;
 
 	/**
 	 * The default value of the '{@link #isID() <em>Is ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isID()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IS_ID_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isID() <em>Is ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isID()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int IS_ID_EFLAG = 1 << 14;
 
 	/**
 	 * The cached value of the '{@link #getKeys() <em>Keys</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getKeys()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Property> keys;
 
 	/**
 	 * The default value of the '{@link #isResolveProxies() <em>Is Resolve Proxies</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isResolveProxies()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IS_RESOLVE_PROXIES_EDEFAULT = true;
 
 	/**
 	 * The flag representing the value of the '{@link #isResolveProxies() <em>Is Resolve Proxies</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isResolveProxies()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int IS_RESOLVE_PROXIES_EFLAG = 1 << 15;
 
 	/**
 	 * The default value of the '{@link #isTransient() <em>Is Transient</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isTransient()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IS_TRANSIENT_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isTransient() <em>Is Transient</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isTransient()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int IS_TRANSIENT_EFLAG = 1 << 16;
 
 	/**
 	 * The default value of the '{@link #isUnsettable() <em>Is Unsettable</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isUnsettable()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IS_UNSETTABLE_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isUnsettable() <em>Is Unsettable</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isUnsettable()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int IS_UNSETTABLE_EFLAG = 1 << 17;
 
 	/**
 	 * The default value of the '{@link #isVolatile() <em>Is Volatile</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isVolatile()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IS_VOLATILE_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isVolatile() <em>Is Volatile</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isVolatile()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int IS_VOLATILE_EFLAG = 1 << 18;
 
 	/**
 	 * The cached value of the '{@link #getRedefinedProperty() <em>Redefined Property</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRedefinedProperty()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Property> redefinedProperty;
 
 	/**
 	 * The cached value of the '{@link #getSubsettedProperty() <em>Subsetted Property</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSubsettedProperty()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Property> subsettedProperty;
 
 	/**
 	 * The cached value of the '{@link #getReferredProperty() <em>Referred Property</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getReferredProperty()
 	 * @generated
 	 * @ordered
 	 */
 	protected Property referredProperty;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected PropertyImpl() {
 		super();
 		eFlags |= IS_RESOLVE_PROXIES_EFLAG;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return PivotPackage.Literals.PROPERTY;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TemplateParameter getTemplateParameter() {
 		if (templateParameter != null && ((EObject)templateParameter).eIsProxy())
 		{
 			InternalEObject oldTemplateParameter = (InternalEObject)templateParameter;
 			templateParameter = (TemplateParameter)eResolveProxy(oldTemplateParameter);
 			if (templateParameter != oldTemplateParameter)
 			{
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, PivotPackage.PROPERTY__TEMPLATE_PARAMETER, oldTemplateParameter, templateParameter));
 			}
 		}
 		return templateParameter;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TemplateParameter basicGetTemplateParameter() {
 		return templateParameter;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetTemplateParameter(
 			TemplateParameter newTemplateParameter, NotificationChain msgs) {
 		TemplateParameter oldTemplateParameter = templateParameter;
 		templateParameter = newTemplateParameter;
 		if (eNotificationRequired())
 		{
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__TEMPLATE_PARAMETER, oldTemplateParameter, newTemplateParameter);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		Resource.Internal eInternalResource = eInternalResource();
 		if (eInternalResource == null || !eInternalResource.isLoading()) {
 			TemplateParameter owningTemplateParameter = getOwningTemplateParameter();
 			if (owningTemplateParameter != null && owningTemplateParameter != newTemplateParameter)
 			{
 				setOwningTemplateParameter(null);
 			}
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTemplateParameter(TemplateParameter newTemplateParameter) {
 		if (newTemplateParameter != templateParameter)
 		{
 			NotificationChain msgs = null;
 			if (templateParameter != null)
 				msgs = ((InternalEObject)templateParameter).eInverseRemove(this, PivotPackage.TEMPLATE_PARAMETER__PARAMETERED_ELEMENT, TemplateParameter.class, msgs);
 			if (newTemplateParameter != null)
 				msgs = ((InternalEObject)newTemplateParameter).eInverseAdd(this, PivotPackage.TEMPLATE_PARAMETER__PARAMETERED_ELEMENT, TemplateParameter.class, msgs);
 			msgs = basicSetTemplateParameter(newTemplateParameter, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__TEMPLATE_PARAMETER, newTemplateParameter, newTemplateParameter));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TemplateParameter getOwningTemplateParameter() {
 		if (eContainerFeatureID() != PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER) return null;
 		return (TemplateParameter)eInternalContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOwningTemplateParameter(
 			TemplateParameter newOwningTemplateParameter, NotificationChain msgs) {
 		msgs = eBasicSetContainer((InternalEObject)newOwningTemplateParameter, PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER, msgs);
 		Resource.Internal eInternalResource = eInternalResource();
 		if (eInternalResource == null || !eInternalResource.isLoading()) {
 			if (newOwningTemplateParameter != null)
 			{
 				if (newOwningTemplateParameter != templateParameter)
 				{
 					setTemplateParameter(newOwningTemplateParameter);
 				}
 			}
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOwningTemplateParameter(
 			TemplateParameter newOwningTemplateParameter) {
 		if (newOwningTemplateParameter != eInternalContainer() || (eContainerFeatureID() != PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER && newOwningTemplateParameter != null))
 		{
 			if (EcoreUtil.isAncestor(this, (EObject)newOwningTemplateParameter))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
 			NotificationChain msgs = null;
 			if (eInternalContainer() != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newOwningTemplateParameter != null)
 				msgs = ((InternalEObject)newOwningTemplateParameter).eInverseAdd(this, PivotPackage.TEMPLATE_PARAMETER__OWNED_PARAMETERED_ELEMENT, TemplateParameter.class, msgs);
 			msgs = basicSetOwningTemplateParameter(newOwningTemplateParameter, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER, newOwningTemplateParameter, newOwningTemplateParameter));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isReadOnly() {
 		return (eFlags & IS_READ_ONLY_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIsReadOnly(boolean newIsReadOnly) {
 		boolean oldIsReadOnly = (eFlags & IS_READ_ONLY_EFLAG) != 0;
 		if (newIsReadOnly) eFlags |= IS_READ_ONLY_EFLAG; else eFlags &= ~IS_READ_ONLY_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__IS_READ_ONLY, oldIsReadOnly, newIsReadOnly));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDefault() {
 		return default_;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDefault(String newDefault) {
 		String oldDefault = default_;
 		default_ = newDefault;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__DEFAULT, oldDefault, default_));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isComposite() {
 		return (eFlags & IS_COMPOSITE_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIsComposite(boolean newIsComposite) {
 		boolean oldIsComposite = (eFlags & IS_COMPOSITE_EFLAG) != 0;
 		if (newIsComposite) eFlags |= IS_COMPOSITE_EFLAG; else eFlags &= ~IS_COMPOSITE_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__IS_COMPOSITE, oldIsComposite, newIsComposite));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isDerived() {
 		return (eFlags & IS_DERIVED_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIsDerived(boolean newIsDerived) {
 		boolean oldIsDerived = (eFlags & IS_DERIVED_EFLAG) != 0;
 		if (newIsDerived) eFlags |= IS_DERIVED_EFLAG; else eFlags &= ~IS_DERIVED_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__IS_DERIVED, oldIsDerived, newIsDerived));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Property getOpposite() {
 		if (opposite != null && ((EObject)opposite).eIsProxy())
 		{
 			InternalEObject oldOpposite = (InternalEObject)opposite;
 			opposite = (Property)eResolveProxy(oldOpposite);
 			if (opposite != oldOpposite)
 			{
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, PivotPackage.PROPERTY__OPPOSITE, oldOpposite, opposite));
 			}
 		}
 		return opposite;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Property basicGetOpposite() {
 		return opposite;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOpposite(Property newOpposite) {
 		Property oldOpposite = opposite;
 		opposite = newOpposite;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__OPPOSITE, oldOpposite, opposite));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AssociationClass getAssociation() {
 		if (association != null && ((EObject)association).eIsProxy())
 		{
 			InternalEObject oldAssociation = (InternalEObject)association;
 			association = (AssociationClass)eResolveProxy(oldAssociation);
 			if (association != oldAssociation)
 			{
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, PivotPackage.PROPERTY__ASSOCIATION, oldAssociation, association));
 			}
 		}
 		return association;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AssociationClass basicGetAssociation() {
 		return association;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetAssociation(
 			AssociationClass newAssociation, NotificationChain msgs) {
 		AssociationClass oldAssociation = association;
 		association = newAssociation;
 		if (eNotificationRequired())
 		{
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__ASSOCIATION, oldAssociation, newAssociation);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAssociation(AssociationClass newAssociation) {
 		if (newAssociation != association)
 		{
 			NotificationChain msgs = null;
 			if (association != null)
 				msgs = ((InternalEObject)association).eInverseRemove(this, PivotPackage.ASSOCIATION_CLASS__UNOWNED_ATTRIBUTE, AssociationClass.class, msgs);
 			if (newAssociation != null)
 				msgs = ((InternalEObject)newAssociation).eInverseAdd(this, PivotPackage.ASSOCIATION_CLASS__UNOWNED_ATTRIBUTE, AssociationClass.class, msgs);
 			msgs = basicSetAssociation(newAssociation, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__ASSOCIATION, newAssociation, newAssociation));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isImplicit()
 	{
 		return (eFlags & IMPLICIT_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setImplicit(boolean newImplicit)
 	{
 		boolean oldImplicit = (eFlags & IMPLICIT_EFLAG) != 0;
 		if (newImplicit) eFlags |= IMPLICIT_EFLAG; else eFlags &= ~IMPLICIT_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__IMPLICIT, oldImplicit, newImplicit));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isID()
 	{
 		return (eFlags & IS_ID_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIsID(boolean newIsID)
 	{
 		boolean oldIsID = (eFlags & IS_ID_EFLAG) != 0;
 		if (newIsID) eFlags |= IS_ID_EFLAG; else eFlags &= ~IS_ID_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__IS_ID, oldIsID, newIsID));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<Property> getKeys()
 	{
 		if (keys == null)
 		{
 			keys = new EObjectResolvingEList<Property>(Property.class, this, PivotPackage.PROPERTY__KEYS);
 		}
 		return keys;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isResolveProxies()
 	{
 		return (eFlags & IS_RESOLVE_PROXIES_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIsResolveProxies(boolean newIsResolveProxies)
 	{
 		boolean oldIsResolveProxies = (eFlags & IS_RESOLVE_PROXIES_EFLAG) != 0;
 		if (newIsResolveProxies) eFlags |= IS_RESOLVE_PROXIES_EFLAG; else eFlags &= ~IS_RESOLVE_PROXIES_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__IS_RESOLVE_PROXIES, oldIsResolveProxies, newIsResolveProxies));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isTransient()
 	{
 		return (eFlags & IS_TRANSIENT_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIsTransient(boolean newIsTransient)
 	{
 		boolean oldIsTransient = (eFlags & IS_TRANSIENT_EFLAG) != 0;
 		if (newIsTransient) eFlags |= IS_TRANSIENT_EFLAG; else eFlags &= ~IS_TRANSIENT_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__IS_TRANSIENT, oldIsTransient, newIsTransient));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isUnsettable()
 	{
 		return (eFlags & IS_UNSETTABLE_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIsUnsettable(boolean newIsUnsettable)
 	{
 		boolean oldIsUnsettable = (eFlags & IS_UNSETTABLE_EFLAG) != 0;
 		if (newIsUnsettable) eFlags |= IS_UNSETTABLE_EFLAG; else eFlags &= ~IS_UNSETTABLE_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__IS_UNSETTABLE, oldIsUnsettable, newIsUnsettable));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isVolatile()
 	{
 		return (eFlags & IS_VOLATILE_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIsVolatile(boolean newIsVolatile)
 	{
 		boolean oldIsVolatile = (eFlags & IS_VOLATILE_EFLAG) != 0;
 		if (newIsVolatile) eFlags |= IS_VOLATILE_EFLAG; else eFlags &= ~IS_VOLATILE_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__IS_VOLATILE, oldIsVolatile, newIsVolatile));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<Property> getRedefinedProperty()
 	{
 		if (redefinedProperty == null)
 		{
 			redefinedProperty = new EObjectResolvingEList<Property>(Property.class, this, PivotPackage.PROPERTY__REDEFINED_PROPERTY);
 		}
 		return redefinedProperty;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<Property> getSubsettedProperty()
 	{
 		if (subsettedProperty == null)
 		{
 			subsettedProperty = new EObjectResolvingEList<Property>(Property.class, this, PivotPackage.PROPERTY__SUBSETTED_PROPERTY);
 		}
 		return subsettedProperty;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Property getReferredProperty()
 	{
 		if (referredProperty != null && ((EObject)referredProperty).eIsProxy())
 		{
 			InternalEObject oldReferredProperty = (InternalEObject)referredProperty;
 			referredProperty = (Property)eResolveProxy(oldReferredProperty);
 			if (referredProperty != oldReferredProperty)
 			{
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, PivotPackage.PROPERTY__REFERRED_PROPERTY, oldReferredProperty, referredProperty));
 			}
 		}
 		return referredProperty;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Property basicGetReferredProperty()
 	{
 		return referredProperty;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setReferredProperty(Property newReferredProperty)
 	{
 		Property oldReferredProperty = referredProperty;
 		referredProperty = newReferredProperty;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__REFERRED_PROPERTY, oldReferredProperty, referredProperty));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Type getOwningType()
 	{
 		if (eContainerFeatureID() != PivotPackage.PROPERTY__OWNING_TYPE) return null;
 		return (Type)eInternalContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOwningType(Type newOwningType, NotificationChain msgs)
 	{
 		msgs = eBasicSetContainer((InternalEObject)newOwningType, PivotPackage.PROPERTY__OWNING_TYPE, msgs);
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOwningType(Type newOwningType)
 	{
 		if (newOwningType != eInternalContainer() || (eContainerFeatureID() != PivotPackage.PROPERTY__OWNING_TYPE && newOwningType != null))
 		{
 			if (EcoreUtil.isAncestor(this, (EObject)newOwningType))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
 			NotificationChain msgs = null;
 			if (eInternalContainer() != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newOwningType != null)
 				msgs = ((InternalEObject)newOwningType).eInverseAdd(this, PivotPackage.TYPE__OWNED_ATTRIBUTE, Type.class, msgs);
 			msgs = basicSetOwningType(newOwningType, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.PROPERTY__OWNING_TYPE, newOwningType, newOwningType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public org.eclipse.ocl.examples.pivot.Class getClass_() {
 		org.eclipse.ocl.examples.pivot.Class class_ = basicGetClass_();
 		return class_ != null && ((EObject)class_).eIsProxy() ? (org.eclipse.ocl.examples.pivot.Class)eResolveProxy((InternalEObject)class_) : class_;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public org.eclipse.ocl.examples.pivot.Class basicGetClass_()
 	{
 		return null;		// FIXME Eliminate this field altogether
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isTemplateParameter() {
 		throw new UnsupportedOperationException();  // FIXME Unimplemented http://www.eclipse.org/ocl/3.1.0/Pivot!ParameterableElement!isTemplateParameter()
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isCompatibleWith(final ParameterableElement p)
 	{
 		/**
 		 * p.oclIsKindOf(self.oclType())
 		 */
 		final @NonNull /*@NonInvalid*/ Object self = this;
 		final @NonNull /*@NonInvalid*/ DomainEvaluator evaluator = new EcoreExecutorManager(self, PivotTables.LIBRARY);
 		final @NonNull /*@Thrown*/ DomainType oclType = OclAnyOclTypeOperation.INSTANCE.evaluate(evaluator, self);
 		final @NonNull /*@Thrown*/ Boolean oclIsKindOf = OclAnyOclIsKindOfOperation.INSTANCE.evaluate(evaluator, p, oclType);
 		return oclIsKindOf.booleanValue();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isAttribute(final Property p)
 	{
 		/**
 		 * 
 		 * let container : OclElement = oclContainer()
 		 * in
 		 *   container.oclIsKindOf(Type) and
 		 *   container.oclAsType(Type)
 		 *   .ownedAttribute->includes(self)
 		 */
 		final @NonNull /*@NonInvalid*/ Object self = this;
 		final @NonNull /*@NonInvalid*/ DomainEvaluator evaluator = new EcoreExecutorManager(self, PivotTables.LIBRARY);
 		final @NonNull /*@NonInvalid*/ IdResolver idResolver = evaluator.getIdResolver();
 		final @NonNull /*@NonInvalid*/ DomainType TYP_Type = idResolver.getType(PivotTables.CLSSid_Type, null);
 		@Nullable /*@Caught*/ Object oclContainer;
 		try {
 		    oclContainer = ClassifierOclContainerOperation.INSTANCE.evaluate(self);
 		} catch (Exception e) { oclContainer = ValuesUtil.createInvalidValue(e); }
 		@NonNull /*@Caught*/ Object oclIsKindOf;
 		try {
 		    if (oclContainer instanceof InvalidValueException) throw (InvalidValueException)oclContainer;
 		    oclIsKindOf = OclAnyOclIsKindOfOperation.INSTANCE.evaluate(evaluator, oclContainer, TYP_Type);
 		} catch (Exception e_0) { oclIsKindOf = ValuesUtil.createInvalidValue(e_0); }
 		@NonNull /*@Caught*/ Object includes;
 		try {
 		    if (oclContainer instanceof InvalidValueException) throw (InvalidValueException)oclContainer;
 		    final @Nullable /*@Thrown*/ Object oclAsType = OclAnyOclAsTypeOperation.INSTANCE.evaluate(evaluator, oclContainer, TYP_Type);
 		    if (oclAsType == null) throw new InvalidValueException("Null Literal");
 		    final @SuppressWarnings("null")@NonNull /*@Thrown*/ Iterable<?> ownedAttribute = ((DomainType)oclAsType).getOwnedAttribute();
 		    final @NonNull /*@Thrown*/ OrderedSetValue BOXED_ownedAttribute = idResolver.createOrderedSetOfAll(PivotTables.ORD_CLSSid_Property, ownedAttribute);
 		    includes = CollectionIncludesOperation.INSTANCE.evaluate(BOXED_ownedAttribute, self);
 		} catch (Exception e_1) { includes = ValuesUtil.createInvalidValue(e_1); }
 		final @Nullable /*@Thrown*/ Boolean and = BooleanAndOperation.INSTANCE.evaluate(oclIsKindOf, includes);
 		if (and == null) throw new InvalidValueException("Null Literal");
 		return and.booleanValue();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCompatibleInitialiser(final DiagnosticChain diagnostics, final Map<Object, Object> context)
 	{
 		/**
 		 * isDerived implies
 		 * let
 		 *   derivedConstraints : Collection(Constraint) = ownedRule->select(stereotype = 'derivation')
 		 * in
 		 *   let
 		 *     initialConstraints : Collection(Constraint) = ownedRule->select(stereotype = 'initial')
 		 *   in
 		 *     let
 		 *       derivedSpecification : ValueSpecification = if
 		 *         derivedConstraints->notEmpty()
 		 *       then derivedConstraints->any(true).specification
 		 *       else null
 		 *       endif
 		 *     in
 		 *       let
 		 *         initialSpecification : ValueSpecification = if
 		 *           initialConstraints->notEmpty()
 		 *         then initialConstraints->any(true).specification
 		 *         else null
 		 *         endif
 		 *       in
 		 *         let
 		 *           initialiser : ValueSpecification = if derivedSpecification <> null
 		 *           then derivedSpecification
 		 *           else initialSpecification
 		 *           endif
 		 *         in initialiser <> null and
 		 *           initialiser.oclIsKindOf(ExpressionInOCL) implies
 		 *           CompatibleBody(initialiser)
 		 */
 		final @NonNull /*@NonInvalid*/ Object self = this;
 		final @NonNull /*@NonInvalid*/ DomainEvaluator evaluator = new EcoreExecutorManager(self, PivotTables.LIBRARY);
 		final @NonNull /*@NonInvalid*/ IdResolver idResolver = evaluator.getIdResolver();
 		final @NonNull /*@NonInvalid*/ DomainType TYP_pivot_c_c_ExpressionInOCL = idResolver.getType(PivotTables.CLSSid_ExpressionInOCL, null);
 		@Nullable /*@Caught*/ Object implies;
 		try {
 		    @Nullable /*@Caught*/ Object isDerived;
 		    try {
 		        isDerived = ((Property)self).isDerived();
 		    } catch (Exception e) { isDerived = ValuesUtil.createInvalidValue(e); }
 		    @NonNull /*@Caught*/ Object select;
 		    try {
 		        final @SuppressWarnings("null")@NonNull /*@Thrown*/ List<?> ownedRule = ((NamedElement)self).getOwnedRule();
 		        final @NonNull /*@Thrown*/ OrderedSetValue BOXED_ownedRule = idResolver.createOrderedSetOfAll(PivotTables.ORD_CLSSid_Constraint, ownedRule);
 		        @NonNull /*@Thrown*/ Accumulator ownedRule_accumulator = ValuesUtil.createOrderedSetAccumulatorValue(PivotTables.ORD_CLSSid_Constraint);
 		        final @NonNull /*@NonInvalid*/ Iterator<?> ownedRule_iterator = BOXED_ownedRule.iterator();
 		        ;
 		        while (true) {
 		            if (!ownedRule_iterator.hasNext()) {
 		                select = ownedRule_accumulator;
 		                break;
 		            }
 		            final @Nullable /*@NonInvalid*/ Object _49__ = ownedRule_iterator.next();
 		            /**
 		             * stereotype = 'derivation'
 		             */
 		            if (_49__ == null) throw new InvalidValueException("Null Literal");
 		            final @Nullable /*@Thrown*/ String stereotype = ((Constraint)_49__).getStereotype();
 		            final @NonNull /*@Thrown*/ Boolean _q = OclAnyEqualOperation.INSTANCE.evaluate(stereotype, PivotTables.STR_derivation);
 		            /**/
 		            if (_q == ValuesUtil.TRUE_VALUE) {
 		                ownedRule_accumulator.add(_49__);
 		            }
 		        }
 		    } catch (Exception e_0) { select = ValuesUtil.createInvalidValue(e_0); }
 		    @NonNull /*@Caught*/ Object select_0;
 		    try {
 		        final @SuppressWarnings("null")@NonNull /*@Thrown*/ List<?> ownedRule_0 = ((NamedElement)self).getOwnedRule();
 		        final @NonNull /*@Thrown*/ OrderedSetValue BOXED_ownedRule_0 = idResolver.createOrderedSetOfAll(PivotTables.ORD_CLSSid_Constraint, ownedRule_0);
 		        @NonNull /*@Thrown*/ Accumulator ownedRule_0_accumulator = ValuesUtil.createOrderedSetAccumulatorValue(PivotTables.ORD_CLSSid_Constraint);
 		        final @NonNull /*@NonInvalid*/ Iterator<?> ownedRule_0_iterator = BOXED_ownedRule_0.iterator();
 		        ;
 		        while (true) {
 		            if (!ownedRule_0_iterator.hasNext()) {
 		                select_0 = ownedRule_0_accumulator;
 		                break;
 		            }
 		            final @Nullable /*@NonInvalid*/ Object _49___0 = ownedRule_0_iterator.next();
 		            /**
 		             * stereotype = 'initial'
 		             */
 		            if (_49___0 == null) throw new InvalidValueException("Null Literal");
 		            final @Nullable /*@Thrown*/ String stereotype_0 = ((Constraint)_49___0).getStereotype();
 		            final @NonNull /*@Thrown*/ Boolean _q_0 = OclAnyEqualOperation.INSTANCE.evaluate(stereotype_0, PivotTables.STR_initial);
 		            /**/
 		            if (_q_0 == ValuesUtil.TRUE_VALUE) {
 		                ownedRule_0_accumulator.add(_49___0);
 		            }
 		        }
 		    } catch (Exception e_1) { select_0 = ValuesUtil.createInvalidValue(e_1); }
 		    @Nullable /*@Caught*/ Object symbol_0;
 		    try {
 		        if (select instanceof InvalidValueException) throw (InvalidValueException)select;
 		        final @NonNull /*@Thrown*/ Boolean notEmpty = CollectionNotEmptyOperation.INSTANCE.evaluate(select);
 		        if (notEmpty == ValuesUtil.TRUE_VALUE) {
 		            final @NonNull /*@NonInvalid*/ Iterator<?> select_iterator = ((Iterable<?>)select).iterator();
 		            @Nullable /*@Thrown*/ Object any;
 		            while (true) {
 		                if (!select_iterator.hasNext()) {
 		                    throw new InvalidValueException("No matching content for 'any'");
 		                }
 		                final @Nullable /*@NonInvalid*/ Object _49___1 = select_iterator.next();
 		                /**
 		                 * true
 		                 */
 		                /**/
 		                if (ValuesUtil.TRUE_VALUE != ValuesUtil.FALSE_VALUE) {			// Carry on till something found
 		                    any = _49___1;
 		                    break;
 		                }
 		            }
 		            if (any == null) throw new InvalidValueException("Null Literal");
 		            final @Nullable /*@Thrown*/ ValueSpecification specification = ((Constraint)any).getSpecification();
 		            symbol_0 = specification;
 		        }
 		        else if (notEmpty == ValuesUtil.FALSE_VALUE) {
 		            symbol_0 = null;
 		        }
 		        else {
 		            throw ValuesUtil.INVALID_VALUE;
 		        }
 		        ;
 		    } catch (Exception e_2) { symbol_0 = ValuesUtil.createInvalidValue(e_2); }
 		    @Nullable /*@Thrown*/ Object symbol_1;
 		    if (select_0 instanceof InvalidValueException) throw (InvalidValueException)select_0;
 		    final @NonNull /*@Thrown*/ Boolean notEmpty_0 = CollectionNotEmptyOperation.INSTANCE.evaluate(select_0);
 		    if (notEmpty_0 == ValuesUtil.TRUE_VALUE) {
 		        final @NonNull /*@NonInvalid*/ Iterator<?> select_0_iterator = ((Iterable<?>)select_0).iterator();
 		        @Nullable /*@Thrown*/ Object any_0;
 		        while (true) {
 		            if (!select_0_iterator.hasNext()) {
 		                throw new InvalidValueException("No matching content for 'any'");
 		            }
 		            final @Nullable /*@NonInvalid*/ Object _49___2 = select_0_iterator.next();
 		            /**
 		             * true
 		             */
 		            /**/
 		            if (ValuesUtil.TRUE_VALUE != ValuesUtil.FALSE_VALUE) {			// Carry on till something found
 		                any_0 = _49___2;
 		                break;
 		            }
 		        }
 		        if (any_0 == null) throw new InvalidValueException("Null Literal");
 		        final @Nullable /*@Thrown*/ ValueSpecification specification_0 = ((Constraint)any_0).getSpecification();
 		        symbol_1 = specification_0;
 		    }
 		    else if (notEmpty_0 == ValuesUtil.FALSE_VALUE) {
 		        symbol_1 = null;
 		    }
 		    else {
 		        throw ValuesUtil.INVALID_VALUE;
 		    }
 		    @Nullable /*@Caught*/ Object symbol_2;
 		    try {
 		        if (symbol_0 instanceof InvalidValueException) throw (InvalidValueException)symbol_0;
 		        final @NonNull /*@Thrown*/ Boolean _l_g = OclAnyNotEqualOperation.INSTANCE.evaluate(symbol_0, null);
 		        if (_l_g == ValuesUtil.TRUE_VALUE) {
 		            symbol_2 = symbol_0;
 		        }
 		        else if (_l_g == ValuesUtil.FALSE_VALUE) {
 		            symbol_2 = symbol_1;
 		        }
 		        else {
 		            throw ValuesUtil.INVALID_VALUE;
 		        }
 		        ;
 		    } catch (Exception e_3) { symbol_2 = ValuesUtil.createInvalidValue(e_3); }
 		    @Nullable /*@Caught*/ Object implies_0;
 		    try {
 		        @Nullable /*@Caught*/ Object and;
 		        try {
 		            @NonNull /*@Caught*/ Object _l_g_0;
 		            try {
 		                if (symbol_2 instanceof InvalidValueException) throw (InvalidValueException)symbol_2;
 		                _l_g_0 = OclAnyNotEqualOperation.INSTANCE.evaluate(symbol_2, null);
 		            } catch (Exception e_4) { _l_g_0 = ValuesUtil.createInvalidValue(e_4); }
 		            @NonNull /*@Caught*/ Object oclIsKindOf;
 		            try {
 		                if (symbol_2 instanceof InvalidValueException) throw (InvalidValueException)symbol_2;
 		                oclIsKindOf = OclAnyOclIsKindOfOperation.INSTANCE.evaluate(evaluator, symbol_2, TYP_pivot_c_c_ExpressionInOCL);
 		            } catch (Exception e_5) { oclIsKindOf = ValuesUtil.createInvalidValue(e_5); }
 		            and = BooleanAndOperation.INSTANCE.evaluate(_l_g_0, oclIsKindOf);
 		        } catch (Exception e_6) { and = ValuesUtil.createInvalidValue(e_6); }
 		        @Nullable /*@Caught*/ Object CompatibleBody;
 		        try {
 		            if (symbol_2 instanceof InvalidValueException) throw (InvalidValueException)symbol_2;
 		            CompatibleBody = ((TypedMultiplicityElement)self).CompatibleBody((ValueSpecification)symbol_2);
 		        } catch (Exception e_7) { CompatibleBody = ValuesUtil.createInvalidValue(e_7); }
 		        implies_0 = BooleanImpliesOperation.INSTANCE.evaluate(and, CompatibleBody);
 		    } catch (Exception e_8) { implies_0 = ValuesUtil.createInvalidValue(e_8); }
 		    implies = BooleanImpliesOperation.INSTANCE.evaluate(isDerived, implies_0);
 		} catch (Exception e_9) { implies = ValuesUtil.createInvalidValue(e_9); }
 		if (implies == ValuesUtil.TRUE_VALUE) {
 		    return true;
 		}
 		if (diagnostics != null) {
 		    int severity = implies == null ? Diagnostic.ERROR : Diagnostic.WARNING;
 		    String message = NLS.bind(EvaluatorMessages.ValidationConstraintIsNotSatisfied_ERROR_, new Object[]{"Property", "CompatibleInitialiser", EObjectValidator.getObjectLabel(this, context)});
 		    diagnostics.add(new BasicDiagnostic(severity, PivotValidator.DIAGNOSTIC_SOURCE, PivotValidator.PROPERTY__COMPATIBLE_INITIALISER, message, new Object [] { this }));
 		}
 		return false;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd,
 			int featureID, NotificationChain msgs) {
 		switch (featureID)
 		{
 			case PivotPackage.PROPERTY__EXTENSION:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getExtension()).basicAdd(otherEnd, msgs);
 			case PivotPackage.PROPERTY__OWNED_RULE:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getOwnedRule()).basicAdd(otherEnd, msgs);
 			case PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return basicSetOwningTemplateParameter((TemplateParameter)otherEnd, msgs);
 			case PivotPackage.PROPERTY__TEMPLATE_PARAMETER:
 				if (templateParameter != null)
 					msgs = ((InternalEObject)templateParameter).eInverseRemove(this, PivotPackage.TEMPLATE_PARAMETER__PARAMETERED_ELEMENT, TemplateParameter.class, msgs);
 				return basicSetTemplateParameter((TemplateParameter)otherEnd, msgs);
 			case PivotPackage.PROPERTY__ASSOCIATION:
 				if (association != null)
 					msgs = ((InternalEObject)association).eInverseRemove(this, PivotPackage.ASSOCIATION_CLASS__UNOWNED_ATTRIBUTE, AssociationClass.class, msgs);
 				return basicSetAssociation((AssociationClass)otherEnd, msgs);
 			case PivotPackage.PROPERTY__OWNING_TYPE:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return basicSetOwningType((Type)otherEnd, msgs);
 		}
 		return eDynamicInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd,
 			int featureID, NotificationChain msgs) {
 		switch (featureID)
 		{
 			case PivotPackage.PROPERTY__OWNED_COMMENT:
 				return ((InternalEList<?>)getOwnedComment()).basicRemove(otherEnd, msgs);
 			case PivotPackage.PROPERTY__EXTENSION:
 				return ((InternalEList<?>)getExtension()).basicRemove(otherEnd, msgs);
 			case PivotPackage.PROPERTY__OWNED_RULE:
 				return ((InternalEList<?>)getOwnedRule()).basicRemove(otherEnd, msgs);
 			case PivotPackage.PROPERTY__OWNED_ANNOTATION:
 				return ((InternalEList<?>)getOwnedAnnotation()).basicRemove(otherEnd, msgs);
 			case PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER:
 				return basicSetOwningTemplateParameter(null, msgs);
 			case PivotPackage.PROPERTY__TEMPLATE_PARAMETER:
 				return basicSetTemplateParameter(null, msgs);
 			case PivotPackage.PROPERTY__ASSOCIATION:
 				return basicSetAssociation(null, msgs);
 			case PivotPackage.PROPERTY__OWNING_TYPE:
 				return basicSetOwningType(null, msgs);
 		}
 		return eDynamicInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eBasicRemoveFromContainerFeature(
 			NotificationChain msgs) {
 		switch (eContainerFeatureID())
 		{
 			case PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER:
 				return eInternalContainer().eInverseRemove(this, PivotPackage.TEMPLATE_PARAMETER__OWNED_PARAMETERED_ELEMENT, TemplateParameter.class, msgs);
 			case PivotPackage.PROPERTY__OWNING_TYPE:
 				return eInternalContainer().eInverseRemove(this, PivotPackage.TYPE__OWNED_ATTRIBUTE, Type.class, msgs);
 		}
 		return eDynamicBasicRemoveFromContainer(msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID)
 		{
 			case PivotPackage.PROPERTY__OWNED_COMMENT:
 				return getOwnedComment();
 			case PivotPackage.PROPERTY__EXTENSION:
 				return getExtension();
 			case PivotPackage.PROPERTY__NAME:
 				return getName();
 			case PivotPackage.PROPERTY__OWNED_RULE:
 				return getOwnedRule();
 			case PivotPackage.PROPERTY__IS_STATIC:
 				return isStatic();
 			case PivotPackage.PROPERTY__OWNED_ANNOTATION:
 				return getOwnedAnnotation();
 			case PivotPackage.PROPERTY__TYPE:
 				if (resolve) return getType();
 				return basicGetType();
 			case PivotPackage.PROPERTY__IS_REQUIRED:
 				return isRequired();
 			case PivotPackage.PROPERTY__IMPLEMENTATION_CLASS:
 				return getImplementationClass();
 			case PivotPackage.PROPERTY__IMPLEMENTATION:
 				return getImplementation();
 			case PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER:
 				return getOwningTemplateParameter();
 			case PivotPackage.PROPERTY__TEMPLATE_PARAMETER:
 				if (resolve) return getTemplateParameter();
 				return basicGetTemplateParameter();
 			case PivotPackage.PROPERTY__IS_READ_ONLY:
 				return isReadOnly();
 			case PivotPackage.PROPERTY__DEFAULT:
 				return getDefault();
 			case PivotPackage.PROPERTY__IS_COMPOSITE:
 				return isComposite();
 			case PivotPackage.PROPERTY__IS_DERIVED:
 				return isDerived();
 			case PivotPackage.PROPERTY__CLASS:
 				if (resolve) return getClass_();
 				return basicGetClass_();
 			case PivotPackage.PROPERTY__OPPOSITE:
 				if (resolve) return getOpposite();
 				return basicGetOpposite();
 			case PivotPackage.PROPERTY__ASSOCIATION:
 				if (resolve) return getAssociation();
 				return basicGetAssociation();
 			case PivotPackage.PROPERTY__IMPLICIT:
 				return isImplicit();
 			case PivotPackage.PROPERTY__IS_ID:
 				return isID();
 			case PivotPackage.PROPERTY__KEYS:
 				return getKeys();
 			case PivotPackage.PROPERTY__IS_RESOLVE_PROXIES:
 				return isResolveProxies();
 			case PivotPackage.PROPERTY__IS_TRANSIENT:
 				return isTransient();
 			case PivotPackage.PROPERTY__IS_UNSETTABLE:
 				return isUnsettable();
 			case PivotPackage.PROPERTY__IS_VOLATILE:
 				return isVolatile();
 			case PivotPackage.PROPERTY__REDEFINED_PROPERTY:
 				return getRedefinedProperty();
 			case PivotPackage.PROPERTY__SUBSETTED_PROPERTY:
 				return getSubsettedProperty();
 			case PivotPackage.PROPERTY__REFERRED_PROPERTY:
 				if (resolve) return getReferredProperty();
 				return basicGetReferredProperty();
 			case PivotPackage.PROPERTY__OWNING_TYPE:
 				return getOwningType();
 		}
 		return eDynamicGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID)
 		{
 			case PivotPackage.PROPERTY__OWNED_COMMENT:
 				getOwnedComment().clear();
 				getOwnedComment().addAll((Collection<? extends Comment>)newValue);
 				return;
 			case PivotPackage.PROPERTY__EXTENSION:
 				getExtension().clear();
 				getExtension().addAll((Collection<? extends ElementExtension>)newValue);
 				return;
 			case PivotPackage.PROPERTY__NAME:
 				setName((String)newValue);
 				return;
 			case PivotPackage.PROPERTY__OWNED_RULE:
 				getOwnedRule().clear();
 				getOwnedRule().addAll((Collection<? extends Constraint>)newValue);
 				return;
 			case PivotPackage.PROPERTY__IS_STATIC:
 				setIsStatic((Boolean)newValue);
 				return;
 			case PivotPackage.PROPERTY__OWNED_ANNOTATION:
 				getOwnedAnnotation().clear();
 				getOwnedAnnotation().addAll((Collection<? extends Annotation>)newValue);
 				return;
 			case PivotPackage.PROPERTY__TYPE:
 				setType((Type)newValue);
 				return;
 			case PivotPackage.PROPERTY__IS_REQUIRED:
 				setIsRequired((Boolean)newValue);
 				return;
 			case PivotPackage.PROPERTY__IMPLEMENTATION_CLASS:
 				setImplementationClass((String)newValue);
 				return;
 			case PivotPackage.PROPERTY__IMPLEMENTATION:
 				setImplementation((LibraryFeature)newValue);
 				return;
 			case PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER:
 				setOwningTemplateParameter((TemplateParameter)newValue);
 				return;
 			case PivotPackage.PROPERTY__TEMPLATE_PARAMETER:
 				setTemplateParameter((TemplateParameter)newValue);
 				return;
 			case PivotPackage.PROPERTY__IS_READ_ONLY:
 				setIsReadOnly((Boolean)newValue);
 				return;
 			case PivotPackage.PROPERTY__DEFAULT:
 				setDefault((String)newValue);
 				return;
 			case PivotPackage.PROPERTY__IS_COMPOSITE:
 				setIsComposite((Boolean)newValue);
 				return;
 			case PivotPackage.PROPERTY__IS_DERIVED:
 				setIsDerived((Boolean)newValue);
 				return;
 			case PivotPackage.PROPERTY__OPPOSITE:
 				setOpposite((Property)newValue);
 				return;
 			case PivotPackage.PROPERTY__ASSOCIATION:
 				setAssociation((AssociationClass)newValue);
 				return;
 			case PivotPackage.PROPERTY__IMPLICIT:
 				setImplicit((Boolean)newValue);
 				return;
 			case PivotPackage.PROPERTY__IS_ID:
 				setIsID((Boolean)newValue);
 				return;
 			case PivotPackage.PROPERTY__KEYS:
 				getKeys().clear();
 				getKeys().addAll((Collection<? extends Property>)newValue);
 				return;
 			case PivotPackage.PROPERTY__IS_RESOLVE_PROXIES:
 				setIsResolveProxies((Boolean)newValue);
 				return;
 			case PivotPackage.PROPERTY__IS_TRANSIENT:
 				setIsTransient((Boolean)newValue);
 				return;
 			case PivotPackage.PROPERTY__IS_UNSETTABLE:
 				setIsUnsettable((Boolean)newValue);
 				return;
 			case PivotPackage.PROPERTY__IS_VOLATILE:
 				setIsVolatile((Boolean)newValue);
 				return;
 			case PivotPackage.PROPERTY__REDEFINED_PROPERTY:
 				getRedefinedProperty().clear();
 				getRedefinedProperty().addAll((Collection<? extends Property>)newValue);
 				return;
 			case PivotPackage.PROPERTY__SUBSETTED_PROPERTY:
 				getSubsettedProperty().clear();
 				getSubsettedProperty().addAll((Collection<? extends Property>)newValue);
 				return;
 			case PivotPackage.PROPERTY__REFERRED_PROPERTY:
 				setReferredProperty((Property)newValue);
 				return;
 			case PivotPackage.PROPERTY__OWNING_TYPE:
 				setOwningType((Type)newValue);
 				return;
 		}
 		eDynamicSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID)
 		{
 			case PivotPackage.PROPERTY__OWNED_COMMENT:
 				getOwnedComment().clear();
 				return;
 			case PivotPackage.PROPERTY__EXTENSION:
 				getExtension().clear();
 				return;
 			case PivotPackage.PROPERTY__NAME:
 				setName(NAME_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__OWNED_RULE:
 				getOwnedRule().clear();
 				return;
 			case PivotPackage.PROPERTY__IS_STATIC:
 				setIsStatic(IS_STATIC_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__OWNED_ANNOTATION:
 				getOwnedAnnotation().clear();
 				return;
 			case PivotPackage.PROPERTY__TYPE:
 				setType((Type)null);
 				return;
 			case PivotPackage.PROPERTY__IS_REQUIRED:
 				setIsRequired(IS_REQUIRED_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__IMPLEMENTATION_CLASS:
 				setImplementationClass(IMPLEMENTATION_CLASS_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__IMPLEMENTATION:
 				setImplementation(IMPLEMENTATION_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER:
 				setOwningTemplateParameter((TemplateParameter)null);
 				return;
 			case PivotPackage.PROPERTY__TEMPLATE_PARAMETER:
 				setTemplateParameter((TemplateParameter)null);
 				return;
 			case PivotPackage.PROPERTY__IS_READ_ONLY:
 				setIsReadOnly(IS_READ_ONLY_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__DEFAULT:
 				setDefault(DEFAULT_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__IS_COMPOSITE:
 				setIsComposite(IS_COMPOSITE_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__IS_DERIVED:
 				setIsDerived(IS_DERIVED_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__OPPOSITE:
 				setOpposite((Property)null);
 				return;
 			case PivotPackage.PROPERTY__ASSOCIATION:
 				setAssociation((AssociationClass)null);
 				return;
 			case PivotPackage.PROPERTY__IMPLICIT:
 				setImplicit(IMPLICIT_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__IS_ID:
 				setIsID(IS_ID_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__KEYS:
 				getKeys().clear();
 				return;
 			case PivotPackage.PROPERTY__IS_RESOLVE_PROXIES:
 				setIsResolveProxies(IS_RESOLVE_PROXIES_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__IS_TRANSIENT:
 				setIsTransient(IS_TRANSIENT_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__IS_UNSETTABLE:
 				setIsUnsettable(IS_UNSETTABLE_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__IS_VOLATILE:
 				setIsVolatile(IS_VOLATILE_EDEFAULT);
 				return;
 			case PivotPackage.PROPERTY__REDEFINED_PROPERTY:
 				getRedefinedProperty().clear();
 				return;
 			case PivotPackage.PROPERTY__SUBSETTED_PROPERTY:
 				getSubsettedProperty().clear();
 				return;
 			case PivotPackage.PROPERTY__REFERRED_PROPERTY:
 				setReferredProperty((Property)null);
 				return;
 			case PivotPackage.PROPERTY__OWNING_TYPE:
 				setOwningType((Type)null);
 				return;
 		}
 		eDynamicUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID)
 		{
 			case PivotPackage.PROPERTY__OWNED_COMMENT:
 				return ownedComment != null && !ownedComment.isEmpty();
 			case PivotPackage.PROPERTY__EXTENSION:
 				return extension != null && !extension.isEmpty();
 			case PivotPackage.PROPERTY__NAME:
 				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
 			case PivotPackage.PROPERTY__OWNED_RULE:
 				return ownedRule != null && !ownedRule.isEmpty();
 			case PivotPackage.PROPERTY__IS_STATIC:
 				return isSetIsStatic();
 			case PivotPackage.PROPERTY__OWNED_ANNOTATION:
 				return ownedAnnotation != null && !ownedAnnotation.isEmpty();
 			case PivotPackage.PROPERTY__TYPE:
 				return type != null;
 			case PivotPackage.PROPERTY__IS_REQUIRED:
 				return ((eFlags & IS_REQUIRED_EFLAG) != 0) != IS_REQUIRED_EDEFAULT;
 			case PivotPackage.PROPERTY__IMPLEMENTATION_CLASS:
 				return IMPLEMENTATION_CLASS_EDEFAULT == null ? implementationClass != null : !IMPLEMENTATION_CLASS_EDEFAULT.equals(implementationClass);
 			case PivotPackage.PROPERTY__IMPLEMENTATION:
 				return IMPLEMENTATION_EDEFAULT == null ? implementation != null : !IMPLEMENTATION_EDEFAULT.equals(implementation);
 			case PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER:
 				return getOwningTemplateParameter() != null;
 			case PivotPackage.PROPERTY__TEMPLATE_PARAMETER:
 				return templateParameter != null;
 			case PivotPackage.PROPERTY__IS_READ_ONLY:
 				return ((eFlags & IS_READ_ONLY_EFLAG) != 0) != IS_READ_ONLY_EDEFAULT;
 			case PivotPackage.PROPERTY__DEFAULT:
 				return DEFAULT_EDEFAULT == null ? default_ != null : !DEFAULT_EDEFAULT.equals(default_);
 			case PivotPackage.PROPERTY__IS_COMPOSITE:
 				return ((eFlags & IS_COMPOSITE_EFLAG) != 0) != IS_COMPOSITE_EDEFAULT;
 			case PivotPackage.PROPERTY__IS_DERIVED:
 				return ((eFlags & IS_DERIVED_EFLAG) != 0) != IS_DERIVED_EDEFAULT;
 			case PivotPackage.PROPERTY__CLASS:
 				return basicGetClass_() != null;
 			case PivotPackage.PROPERTY__OPPOSITE:
 				return opposite != null;
 			case PivotPackage.PROPERTY__ASSOCIATION:
 				return association != null;
 			case PivotPackage.PROPERTY__IMPLICIT:
 				return ((eFlags & IMPLICIT_EFLAG) != 0) != IMPLICIT_EDEFAULT;
 			case PivotPackage.PROPERTY__IS_ID:
 				return ((eFlags & IS_ID_EFLAG) != 0) != IS_ID_EDEFAULT;
 			case PivotPackage.PROPERTY__KEYS:
 				return keys != null && !keys.isEmpty();
 			case PivotPackage.PROPERTY__IS_RESOLVE_PROXIES:
 				return ((eFlags & IS_RESOLVE_PROXIES_EFLAG) != 0) != IS_RESOLVE_PROXIES_EDEFAULT;
 			case PivotPackage.PROPERTY__IS_TRANSIENT:
 				return ((eFlags & IS_TRANSIENT_EFLAG) != 0) != IS_TRANSIENT_EDEFAULT;
 			case PivotPackage.PROPERTY__IS_UNSETTABLE:
 				return ((eFlags & IS_UNSETTABLE_EFLAG) != 0) != IS_UNSETTABLE_EDEFAULT;
 			case PivotPackage.PROPERTY__IS_VOLATILE:
 				return ((eFlags & IS_VOLATILE_EFLAG) != 0) != IS_VOLATILE_EDEFAULT;
 			case PivotPackage.PROPERTY__REDEFINED_PROPERTY:
 				return redefinedProperty != null && !redefinedProperty.isEmpty();
 			case PivotPackage.PROPERTY__SUBSETTED_PROPERTY:
 				return subsettedProperty != null && !subsettedProperty.isEmpty();
 			case PivotPackage.PROPERTY__REFERRED_PROPERTY:
 				return referredProperty != null;
 			case PivotPackage.PROPERTY__OWNING_TYPE:
 				return getOwningType() != null;
 		}
 		return eDynamicIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
 		if (baseClass == ParameterableElement.class)
 		{
 			switch (derivedFeatureID)
 			{
 				case PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER: return PivotPackage.PARAMETERABLE_ELEMENT__OWNING_TEMPLATE_PARAMETER;
 				case PivotPackage.PROPERTY__TEMPLATE_PARAMETER: return PivotPackage.PARAMETERABLE_ELEMENT__TEMPLATE_PARAMETER;
 				default: return -1;
 			}
 		}
 		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
 		if (baseClass == ParameterableElement.class)
 		{
 			switch (baseFeatureID)
 			{
 				case PivotPackage.PARAMETERABLE_ELEMENT__OWNING_TEMPLATE_PARAMETER: return PivotPackage.PROPERTY__OWNING_TEMPLATE_PARAMETER;
 				case PivotPackage.PARAMETERABLE_ELEMENT__TEMPLATE_PARAMETER: return PivotPackage.PROPERTY__TEMPLATE_PARAMETER;
 				default: return -1;
 			}
 		}
 		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public int eDerivedOperationID(int baseOperationID, Class<?> baseClass) {
 		if (baseClass == ParameterableElement.class)
 		{
 			switch (baseOperationID)
 			{
 				case PivotPackage.PARAMETERABLE_ELEMENT___IS_TEMPLATE_PARAMETER: return PivotPackage.PROPERTY___IS_TEMPLATE_PARAMETER;
 				case PivotPackage.PARAMETERABLE_ELEMENT___IS_COMPATIBLE_WITH__PARAMETERABLEELEMENT: return PivotPackage.PROPERTY___IS_COMPATIBLE_WITH__PARAMETERABLEELEMENT;
 				default: return -1;
 			}
 		}
 		return super.eDerivedOperationID(baseOperationID, baseClass);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	public Object eInvoke(int operationID, EList<?> arguments)
 			throws InvocationTargetException {
 		switch (operationID)
 		{
 			case PivotPackage.PROPERTY___ALL_OWNED_ELEMENTS:
 				return allOwnedElements();
 			case PivotPackage.PROPERTY___GET_VALUE__TYPE_STRING:
 				return getValue((Type)arguments.get(0), (String)arguments.get(1));
 			case PivotPackage.PROPERTY___VALIDATE_NOT_OWN_SELF__DIAGNOSTICCHAIN_MAP:
 				return validateNotOwnSelf((DiagnosticChain)arguments.get(0), (Map<Object, Object>)arguments.get(1));
 			case PivotPackage.PROPERTY___COMPATIBLE_BODY__VALUESPECIFICATION:
 				return CompatibleBody((ValueSpecification)arguments.get(0));
 			case PivotPackage.PROPERTY___MAKE_PARAMETER:
 				return makeParameter();
 			case PivotPackage.PROPERTY___IS_TEMPLATE_PARAMETER:
 				return isTemplateParameter();
 			case PivotPackage.PROPERTY___IS_COMPATIBLE_WITH__PARAMETERABLEELEMENT:
 				return isCompatibleWith((ParameterableElement)arguments.get(0));
 			case PivotPackage.PROPERTY___IS_ATTRIBUTE__PROPERTY:
 				return isAttribute((Property)arguments.get(0));
 			case PivotPackage.PROPERTY___VALIDATE_COMPATIBLE_INITIALISER__DIAGNOSTICCHAIN_MAP:
 				return validateCompatibleInitialiser((DiagnosticChain)arguments.get(0), (Map<Object, Object>)arguments.get(1));
 		}
 		return eDynamicInvoke(operationID, arguments);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public String toString() {
 		return super.toString();
 	}
 
 	@Override
 	public <R> R accept(@NonNull Visitor<R> visitor) {
 		return visitor.visitProperty(this);
 	}
 
 	public @Nullable DomainInheritance getInheritance(@NonNull DomainStandardLibrary standardLibrary) {
 		Type owningType = getOwningType();
 		if (owningType != null) {
 			return standardLibrary.getInheritance(owningType);
 		}
 		else {
 			return null;
 		}
 	}
 
 	private PropertyId propertyId = null;
 	
 	public @NonNull PropertyId getPropertyId() {
 		PropertyId propertyId2 = propertyId;
 		if (propertyId2 == null) {
 			synchronized (this) {
 				propertyId2 = propertyId;
 				if (propertyId2 == null) {
 					String name = DomainUtil.nonNullModel(getName());
 					TypeId typeId = getOwningType().getTypeId();
 					propertyId = propertyId2 = typeId.getPropertyId(name);
 				}
 			}
 		}
 		return propertyId2;
 	}
 
 	public void initValue(@NonNull Object objectValue, @Nullable Object unboxedValue) {
 		assert ValuesUtil.isUnboxed(unboxedValue);
 		EObject eTarget = getETarget();
 		if (eTarget instanceof EStructuralFeature) {
 			EStructuralFeature eFeature = (EStructuralFeature) eTarget;
 			EObject eObject = ValuesUtil.asNavigableObject(objectValue);
 			eObject.eSet(eFeature, unboxedValue);
 			return;
 		}
 		throw new UnsupportedOperationException();
 	}
 } //PropertyImpl
