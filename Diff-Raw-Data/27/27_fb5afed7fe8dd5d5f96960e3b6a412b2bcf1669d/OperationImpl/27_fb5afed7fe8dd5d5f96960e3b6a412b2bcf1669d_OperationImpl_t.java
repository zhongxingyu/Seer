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
  * $Id: OperationImpl.java,v 1.7 2011/05/19 16:55:39 ewillink Exp $
  */
 package org.eclipse.ocl.examples.pivot.internal.impl;
 
import java.lang.Iterable;
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
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 import org.eclipse.emf.ecore.util.EObjectValidator;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.elements.DomainInheritance;
 import org.eclipse.ocl.examples.domain.elements.DomainParameterTypes;
 import org.eclipse.ocl.examples.domain.elements.DomainStandardLibrary;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.elements.DomainTypeParameters;
 import org.eclipse.ocl.examples.domain.evaluation.DomainEvaluator;
 import org.eclipse.ocl.examples.domain.ids.IdManager;
 import org.eclipse.ocl.examples.domain.ids.OperationId;
 import org.eclipse.ocl.examples.domain.ids.ParametersId;
 import org.eclipse.ocl.examples.domain.library.LibraryFeature;
 import org.eclipse.ocl.examples.domain.library.UnsupportedOperation;
 import org.eclipse.ocl.examples.domain.messages.EvaluatorMessages;
 import org.eclipse.ocl.examples.domain.types.IdResolver;
 import org.eclipse.ocl.examples.domain.values.OrderedSetValue;
 import org.eclipse.ocl.examples.domain.values.OrderedSetValue.Accumulator;
 import org.eclipse.ocl.examples.domain.values.impl.InvalidValueException;
 import org.eclipse.ocl.examples.domain.values.util.ValuesUtil;
 import org.eclipse.ocl.examples.library.collection.CollectionNotEmptyOperation;
 import org.eclipse.ocl.examples.library.ecore.EcoreExecutorManager;
 import org.eclipse.ocl.examples.library.logical.BooleanAndOperation;
 import org.eclipse.ocl.examples.library.logical.BooleanImpliesOperation;
 import org.eclipse.ocl.examples.library.oclany.OclAnyEqualOperation;
 import org.eclipse.ocl.examples.library.oclany.OclAnyNotEqualOperation;
 import org.eclipse.ocl.examples.library.oclany.OclAnyOclIsKindOfOperation;
 import org.eclipse.ocl.examples.library.oclany.OclAnyOclTypeOperation;
 import org.eclipse.ocl.examples.pivot.Annotation;
 import org.eclipse.ocl.examples.pivot.Comment;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.ElementExtension;
 import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.Namespace;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.Parameter;
 import org.eclipse.ocl.examples.pivot.ParameterableElement;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.PivotTables;
 import org.eclipse.ocl.examples.pivot.Precedence;
 import org.eclipse.ocl.examples.pivot.TemplateBinding;
 import org.eclipse.ocl.examples.pivot.TemplateParameter;
 import org.eclipse.ocl.examples.pivot.TemplateSignature;
 import org.eclipse.ocl.examples.pivot.TemplateableElement;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypedMultiplicityElement;
 import org.eclipse.ocl.examples.pivot.UMLReflection;
 import org.eclipse.ocl.examples.pivot.ValueSpecification;
 import org.eclipse.ocl.examples.pivot.library.ConstrainedOperation;
 import org.eclipse.ocl.examples.pivot.library.EInvokeOperation;
 import org.eclipse.ocl.examples.pivot.util.PivotValidator;
 import org.eclipse.ocl.examples.pivot.util.Visitor;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Operation</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#getTemplateBinding <em>Template Binding</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#getOwnedTemplateSignature <em>Owned Template Signature</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#getUnspecializedElement <em>Unspecialized Element</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#getTemplateParameter <em>Template Parameter</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#getOwningTemplateParameter <em>Owning Template Parameter</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#getRaisedException <em>Raised Exception</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#getOwnedParameter <em>Owned Parameter</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#getOwningType <em>Owning Type</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#isInvalidating <em>Is Invalidating</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#isValidating <em>Is Validating</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#getPrecedence <em>Precedence</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#getRedefinedOperation <em>Redefined Operation</em>}</li>
  *   <li>{@link org.eclipse.ocl.examples.pivot.internal.impl.OperationImpl#getClass_ <em>Class</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 @SuppressWarnings("cast")
 public class OperationImpl
 		extends FeatureImpl
 		implements Operation {
 
 	/**
 	 * The cached value of the '{@link #getTemplateBinding() <em>Template Binding</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTemplateBinding()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<TemplateBinding> templateBinding;
 
 	/**
 	 * The cached value of the '{@link #getOwnedTemplateSignature() <em>Owned Template Signature</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOwnedTemplateSignature()
 	 * @generated
 	 * @ordered
 	 */
 	protected TemplateSignature ownedTemplateSignature;
 
 	/**
 	 * The cached value of the '{@link #getUnspecializedElement() <em>Unspecialized Element</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getUnspecializedElement()
 	 * @generated
 	 * @ordered
 	 */
 	protected TemplateableElement unspecializedElement;
 
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
 	 * The cached value of the '{@link #getRaisedException() <em>Raised Exception</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRaisedException()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Type> raisedException;
 
 	/**
 	 * The cached value of the '{@link #getOwnedParameter() <em>Owned Parameter</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOwnedParameter()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Parameter> ownedParameter;
 
 	/**
 	 * The default value of the '{@link #isInvalidating() <em>Is Invalidating</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isInvalidating()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IS_INVALIDATING_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isInvalidating() <em>Is Invalidating</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isInvalidating()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int IS_INVALIDATING_EFLAG = 1 << 10;
 
 	/**
 	 * The default value of the '{@link #isValidating() <em>Is Validating</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isValidating()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IS_VALIDATING_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isValidating() <em>Is Validating</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isValidating()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int IS_VALIDATING_EFLAG = 1 << 11;
 
 	/**
 	 * The cached value of the '{@link #getPrecedence() <em>Precedence</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPrecedence()
 	 * @generated
 	 * @ordered
 	 */
 	protected Precedence precedence;
 
 	/**
 	 * The cached value of the '{@link #getRedefinedOperation() <em>Redefined Operation</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRedefinedOperation()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Operation> redefinedOperation;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected OperationImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return PivotPackage.Literals.OPERATION;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<TemplateBinding> getTemplateBinding()
 	{
 		if (templateBinding == null)
 		{
 			templateBinding = new EObjectContainmentWithInverseEList<TemplateBinding>(TemplateBinding.class, this, PivotPackage.OPERATION__TEMPLATE_BINDING, PivotPackage.TEMPLATE_BINDING__BOUND_ELEMENT);
 		}
 		return templateBinding;
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
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, PivotPackage.OPERATION__TEMPLATE_PARAMETER, oldTemplateParameter, templateParameter));
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
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PivotPackage.OPERATION__TEMPLATE_PARAMETER, oldTemplateParameter, newTemplateParameter);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
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
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.OPERATION__TEMPLATE_PARAMETER, newTemplateParameter, newTemplateParameter));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetTemplateParameter() {
 		return templateParameter != null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TemplateParameter getOwningTemplateParameter() {
 		if (eContainerFeatureID() != PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER) return null;
 		return (TemplateParameter)eInternalContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOwningTemplateParameter(
 			TemplateParameter newOwningTemplateParameter, NotificationChain msgs) {
 		msgs = eBasicSetContainer((InternalEObject)newOwningTemplateParameter, PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER, msgs);
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
 		if (newOwningTemplateParameter != eInternalContainer() || (eContainerFeatureID() != PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER && newOwningTemplateParameter != null))
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
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER, newOwningTemplateParameter, newOwningTemplateParameter));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<Type> getRaisedException()
 	{
 		if (raisedException == null)
 		{
 			raisedException = new EObjectResolvingEList<Type>(Type.class, this, PivotPackage.OPERATION__RAISED_EXCEPTION);
 		}
 		return raisedException;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("null")
 	public @NonNull List<Parameter> getOwnedParameter()
 	{
 		if (ownedParameter == null)
 		{
 			ownedParameter = new EObjectContainmentWithInverseEList<Parameter>(Parameter.class, this, PivotPackage.OPERATION__OWNED_PARAMETER, PivotPackage.PARAMETER__OPERATION);
 		}
 		return ownedParameter;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TemplateBinding createTemplateBinding() {
 		TemplateBinding newTemplateBinding = (TemplateBinding) create(PivotPackage.Literals.TEMPLATE_BINDING);
 		getTemplateBinding().add(newTemplateBinding);
 		return newTemplateBinding;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TemplateSignature getOwnedTemplateSignature() {
 		return ownedTemplateSignature;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOwnedTemplateSignature(
 			TemplateSignature newOwnedTemplateSignature, NotificationChain msgs) {
 		TemplateSignature oldOwnedTemplateSignature = ownedTemplateSignature;
 		ownedTemplateSignature = newOwnedTemplateSignature;
 		if (eNotificationRequired())
 		{
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PivotPackage.OPERATION__OWNED_TEMPLATE_SIGNATURE, oldOwnedTemplateSignature, newOwnedTemplateSignature);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOwnedTemplateSignature(
 			TemplateSignature newOwnedTemplateSignature) {
 		if (newOwnedTemplateSignature != ownedTemplateSignature)
 		{
 			NotificationChain msgs = null;
 			if (ownedTemplateSignature != null)
 				msgs = ((InternalEObject)ownedTemplateSignature).eInverseRemove(this, PivotPackage.TEMPLATE_SIGNATURE__TEMPLATE, TemplateSignature.class, msgs);
 			if (newOwnedTemplateSignature != null)
 				msgs = ((InternalEObject)newOwnedTemplateSignature).eInverseAdd(this, PivotPackage.TEMPLATE_SIGNATURE__TEMPLATE, TemplateSignature.class, msgs);
 			msgs = basicSetOwnedTemplateSignature(newOwnedTemplateSignature, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.OPERATION__OWNED_TEMPLATE_SIGNATURE, newOwnedTemplateSignature, newOwnedTemplateSignature));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TemplateSignature createOwnedTemplateSignature() {
 		TemplateSignature newOwnedTemplateSignature = (TemplateSignature) create(PivotPackage.Literals.TEMPLATE_SIGNATURE);
 		setOwnedTemplateSignature(newOwnedTemplateSignature);
 		return newOwnedTemplateSignature;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public TemplateableElement getUnspecializedElement()
 	{
 //		throw new UnsupportedOperationException();	// FIXME Eliminate this feature once Acceleo bug 349278 fixed
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setUnspecializedElement(TemplateableElement newUnspecializedElement)
 	{
 		throw new UnsupportedOperationException();	// FIXME Eliminate this feature once Acceleo bug 349278 fixed
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Precedence getPrecedence() {
 		if (precedence != null && ((EObject)precedence).eIsProxy())
 		{
 			InternalEObject oldPrecedence = (InternalEObject)precedence;
 			precedence = (Precedence)eResolveProxy(oldPrecedence);
 			if (precedence != oldPrecedence)
 			{
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, PivotPackage.OPERATION__PRECEDENCE, oldPrecedence, precedence));
 			}
 		}
 		return precedence;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Precedence basicGetPrecedence() {
 		return precedence;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPrecedence(Precedence newPrecedence) {
 		Precedence oldPrecedence = precedence;
 		precedence = newPrecedence;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.OPERATION__PRECEDENCE, oldPrecedence, precedence));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<Operation> getRedefinedOperation()
 	{
 		if (redefinedOperation == null)
 		{
 			redefinedOperation = new EObjectResolvingEList<Operation>(Operation.class, this, PivotPackage.OPERATION__REDEFINED_OPERATION);
 		}
 		return redefinedOperation;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Parameter createOwnedParameter() {
 		Parameter newOwnedParameter = (Parameter) create(PivotPackage.Literals.PARAMETER);
 		getOwnedParameter().add(newOwnedParameter);
 		return newOwnedParameter;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Type getOwningType()
 	{
 		if (eContainerFeatureID() != PivotPackage.OPERATION__OWNING_TYPE) return null;
 		return (Type)eInternalContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOwningType(Type newOwningType, NotificationChain msgs)
 	{
 		msgs = eBasicSetContainer((InternalEObject)newOwningType, PivotPackage.OPERATION__OWNING_TYPE, msgs);
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOwningType(Type newOwningType)
 	{
 		if (newOwningType != eInternalContainer() || (eContainerFeatureID() != PivotPackage.OPERATION__OWNING_TYPE && newOwningType != null))
 		{
 			if (EcoreUtil.isAncestor(this, (EObject)newOwningType))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
 			NotificationChain msgs = null;
 			if (eInternalContainer() != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newOwningType != null)
 				msgs = ((InternalEObject)newOwningType).eInverseAdd(this, PivotPackage.TYPE__OWNED_OPERATION, Type.class, msgs);
 			msgs = basicSetOwningType(newOwningType, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.OPERATION__OWNING_TYPE, newOwningType, newOwningType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isInvalidating()
 	{
 		return (eFlags & IS_INVALIDATING_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIsInvalidating(boolean newIsInvalidating)
 	{
 		boolean oldIsInvalidating = (eFlags & IS_INVALIDATING_EFLAG) != 0;
 		if (newIsInvalidating) eFlags |= IS_INVALIDATING_EFLAG; else eFlags &= ~IS_INVALIDATING_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.OPERATION__IS_INVALIDATING, oldIsInvalidating, newIsInvalidating));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isValidating()
 	{
 		return (eFlags & IS_VALIDATING_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIsValidating(boolean newIsValidating)
 	{
 		boolean oldIsValidating = (eFlags & IS_VALIDATING_EFLAG) != 0;
 		if (newIsValidating) eFlags |= IS_VALIDATING_EFLAG; else eFlags &= ~IS_VALIDATING_EFLAG;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PivotPackage.OPERATION__IS_VALIDATING, oldIsValidating, newIsValidating));
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
 	public boolean validateCompatibleReturn(final DiagnosticChain diagnostics, final Map<Object, Object> context)
 	{
 		/**
 		 * 
 		 * let
 		 *   bodyConstraints : Collection(Constraint) = ownedRule->select(stereotype = 'body')
 		 * in
 		 *   bodyConstraints->notEmpty() implies
 		 *   let
 		 *     bodySpecification : ValueSpecification = bodyConstraints->any(true).specification
 		 *   in bodySpecification <> null and
 		 *     bodySpecification.oclIsKindOf(ExpressionInOCL) implies
 		 *     CompatibleBody(bodySpecification)
 		 */
 		final @NonNull /*@NonInvalid*/ Object self = this;
 		final @NonNull /*@NonInvalid*/ DomainEvaluator evaluator = new EcoreExecutorManager(self, PivotTables.LIBRARY);
 		final @NonNull /*@NonInvalid*/ IdResolver idResolver = evaluator.getIdResolver();
 		final @NonNull /*@NonInvalid*/ DomainType TYP_pivot_c_c_ExpressionInOCL = idResolver.getType(PivotTables.CLSSid_ExpressionInOCL, null);
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
 		         * stereotype = 'body'
 		         */
 		        if (_49__ == null) throw new InvalidValueException("Null Literal");
 		        final @Nullable /*@Thrown*/ String stereotype = ((Constraint)_49__).getStereotype();
 		        final @NonNull /*@Thrown*/ Boolean _q = OclAnyEqualOperation.INSTANCE.evaluate(stereotype, PivotTables.STR_body);
 		        /**/
 		        if (_q == ValuesUtil.TRUE_VALUE) {
 		            ownedRule_accumulator.add(_49__);
 		        }
 		    }
 		} catch (Exception e) { select = ValuesUtil.createInvalidValue(e); }
 		@Nullable /*@Caught*/ Object implies;
 		try {
 		    @NonNull /*@Caught*/ Object notEmpty;
 		    try {
 		        if (select instanceof InvalidValueException) throw (InvalidValueException)select;
 		        notEmpty = CollectionNotEmptyOperation.INSTANCE.evaluate(select);
 		    } catch (Exception e_0) { notEmpty = ValuesUtil.createInvalidValue(e_0); }
 		    @Nullable /*@Caught*/ Object specification;
 		    try {
 		        final @NonNull /*@NonInvalid*/ Iterator<?> select_iterator = ((Iterable<?>)select).iterator();
 		        @Nullable /*@Thrown*/ Object any;
 		        while (true) {
 		            if (!select_iterator.hasNext()) {
 		                throw new InvalidValueException("No matching content for 'any'");
 		            }
 		            final @Nullable /*@NonInvalid*/ Object _49___0 = select_iterator.next();
 		            /**
 		             * true
 		             */
 		            /**/
 		            if (ValuesUtil.TRUE_VALUE != ValuesUtil.FALSE_VALUE) {			// Carry on till something found
 		                any = _49___0;
 		                break;
 		            }
 		        }
 		        if (any == null) throw new InvalidValueException("Null Literal");
 		        specification = ((Constraint)any).getSpecification();
 		    } catch (Exception e_1) { specification = ValuesUtil.createInvalidValue(e_1); }
 		    @Nullable /*@Caught*/ Object implies_0;
 		    try {
 		        @Nullable /*@Caught*/ Object and;
 		        try {
 		            @NonNull /*@Caught*/ Object _l_g;
 		            try {
 		                if (specification instanceof InvalidValueException) throw (InvalidValueException)specification;
 		                _l_g = OclAnyNotEqualOperation.INSTANCE.evaluate(specification, null);
 		            } catch (Exception e_2) { _l_g = ValuesUtil.createInvalidValue(e_2); }
 		            @NonNull /*@Caught*/ Object oclIsKindOf;
 		            try {
 		                if (specification instanceof InvalidValueException) throw (InvalidValueException)specification;
 		                oclIsKindOf = OclAnyOclIsKindOfOperation.INSTANCE.evaluate(evaluator, specification, TYP_pivot_c_c_ExpressionInOCL);
 		            } catch (Exception e_3) { oclIsKindOf = ValuesUtil.createInvalidValue(e_3); }
 		            and = BooleanAndOperation.INSTANCE.evaluate(_l_g, oclIsKindOf);
 		        } catch (Exception e_4) { and = ValuesUtil.createInvalidValue(e_4); }
 		        @Nullable /*@Caught*/ Object CompatibleBody;
 		        try {
 		            if (specification instanceof InvalidValueException) throw (InvalidValueException)specification;
 		            CompatibleBody = ((TypedMultiplicityElement)self).CompatibleBody((ValueSpecification)specification);
 		        } catch (Exception e_5) { CompatibleBody = ValuesUtil.createInvalidValue(e_5); }
 		        implies_0 = BooleanImpliesOperation.INSTANCE.evaluate(and, CompatibleBody);
 		    } catch (Exception e_6) { implies_0 = ValuesUtil.createInvalidValue(e_6); }
 		    implies = BooleanImpliesOperation.INSTANCE.evaluate(notEmpty, implies_0);
 		} catch (Exception e_7) { implies = ValuesUtil.createInvalidValue(e_7); }
 		if (implies == ValuesUtil.TRUE_VALUE) {
 		    return true;
 		}
 		if (diagnostics != null) {
 		    int severity = implies == null ? Diagnostic.ERROR : Diagnostic.WARNING;
 		    String message = NLS.bind(EvaluatorMessages.ValidationConstraintIsNotSatisfied_ERROR_, new Object[]{"Operation", "CompatibleReturn", EObjectValidator.getObjectLabel(this, context)});
 		    diagnostics.add(new BasicDiagnostic(severity, PivotValidator.DIAGNOSTIC_SOURCE, PivotValidator.OPERATION__COMPATIBLE_RETURN, message, new Object [] { this }));
 		}
 		return false;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateLoadableImplementation(final DiagnosticChain diagnostics, final Map<Object, Object> context)
 	{
 		/**
 		 * true
 		 */
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public List<ParameterableElement> parameterableElements() {
 		throw new UnsupportedOperationException();  // FIXME Unimplemented http://www.eclipse.org/ocl/3.1.0/Pivot!TemplateableElement!parameterableElements()
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isTemplate() {
 		throw new UnsupportedOperationException();  // FIXME Unimplemented http://www.eclipse.org/ocl/3.1.0/Pivot!TemplateableElement!isTemplate()
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
 			case PivotPackage.OPERATION__EXTENSION:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getExtension()).basicAdd(otherEnd, msgs);
 			case PivotPackage.OPERATION__OWNED_RULE:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getOwnedRule()).basicAdd(otherEnd, msgs);
 			case PivotPackage.OPERATION__TEMPLATE_BINDING:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getTemplateBinding()).basicAdd(otherEnd, msgs);
 			case PivotPackage.OPERATION__OWNED_TEMPLATE_SIGNATURE:
 				if (ownedTemplateSignature != null)
 					msgs = ((InternalEObject)ownedTemplateSignature).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PivotPackage.OPERATION__OWNED_TEMPLATE_SIGNATURE, null, msgs);
 				return basicSetOwnedTemplateSignature((TemplateSignature)otherEnd, msgs);
 			case PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return basicSetOwningTemplateParameter((TemplateParameter)otherEnd, msgs);
 			case PivotPackage.OPERATION__TEMPLATE_PARAMETER:
 				if (templateParameter != null)
 					msgs = ((InternalEObject)templateParameter).eInverseRemove(this, PivotPackage.TEMPLATE_PARAMETER__PARAMETERED_ELEMENT, TemplateParameter.class, msgs);
 				return basicSetTemplateParameter((TemplateParameter)otherEnd, msgs);
 			case PivotPackage.OPERATION__OWNED_PARAMETER:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getOwnedParameter()).basicAdd(otherEnd, msgs);
 			case PivotPackage.OPERATION__OWNING_TYPE:
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
 			case PivotPackage.OPERATION__OWNED_COMMENT:
 				return ((InternalEList<?>)getOwnedComment()).basicRemove(otherEnd, msgs);
 			case PivotPackage.OPERATION__EXTENSION:
 				return ((InternalEList<?>)getExtension()).basicRemove(otherEnd, msgs);
 			case PivotPackage.OPERATION__OWNED_RULE:
 				return ((InternalEList<?>)getOwnedRule()).basicRemove(otherEnd, msgs);
 			case PivotPackage.OPERATION__OWNED_ANNOTATION:
 				return ((InternalEList<?>)getOwnedAnnotation()).basicRemove(otherEnd, msgs);
 			case PivotPackage.OPERATION__TEMPLATE_BINDING:
 				return ((InternalEList<?>)getTemplateBinding()).basicRemove(otherEnd, msgs);
 			case PivotPackage.OPERATION__OWNED_TEMPLATE_SIGNATURE:
 				return basicSetOwnedTemplateSignature(null, msgs);
 			case PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER:
 				return basicSetOwningTemplateParameter(null, msgs);
 			case PivotPackage.OPERATION__TEMPLATE_PARAMETER:
 				return basicSetTemplateParameter(null, msgs);
 			case PivotPackage.OPERATION__OWNED_PARAMETER:
 				return ((InternalEList<?>)getOwnedParameter()).basicRemove(otherEnd, msgs);
 			case PivotPackage.OPERATION__OWNING_TYPE:
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
 			case PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER:
 				return eInternalContainer().eInverseRemove(this, PivotPackage.TEMPLATE_PARAMETER__OWNED_PARAMETERED_ELEMENT, TemplateParameter.class, msgs);
 			case PivotPackage.OPERATION__OWNING_TYPE:
 				return eInternalContainer().eInverseRemove(this, PivotPackage.TYPE__OWNED_OPERATION, Type.class, msgs);
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
 			case PivotPackage.OPERATION__OWNED_COMMENT:
 				return getOwnedComment();
 			case PivotPackage.OPERATION__EXTENSION:
 				return getExtension();
 			case PivotPackage.OPERATION__NAME:
 				return getName();
 			case PivotPackage.OPERATION__OWNED_RULE:
 				return getOwnedRule();
 			case PivotPackage.OPERATION__IS_STATIC:
 				return isStatic();
 			case PivotPackage.OPERATION__OWNED_ANNOTATION:
 				return getOwnedAnnotation();
 			case PivotPackage.OPERATION__TYPE:
 				if (resolve) return getType();
 				return basicGetType();
 			case PivotPackage.OPERATION__IS_REQUIRED:
 				return isRequired();
 			case PivotPackage.OPERATION__IMPLEMENTATION_CLASS:
 				return getImplementationClass();
 			case PivotPackage.OPERATION__IMPLEMENTATION:
 				return getImplementation();
 			case PivotPackage.OPERATION__TEMPLATE_BINDING:
 				return getTemplateBinding();
 			case PivotPackage.OPERATION__OWNED_TEMPLATE_SIGNATURE:
 				return getOwnedTemplateSignature();
 			case PivotPackage.OPERATION__UNSPECIALIZED_ELEMENT:
 				return getUnspecializedElement();
 			case PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER:
 				return getOwningTemplateParameter();
 			case PivotPackage.OPERATION__TEMPLATE_PARAMETER:
 				if (resolve) return getTemplateParameter();
 				return basicGetTemplateParameter();
 			case PivotPackage.OPERATION__RAISED_EXCEPTION:
 				return getRaisedException();
 			case PivotPackage.OPERATION__OWNED_PARAMETER:
 				return getOwnedParameter();
 			case PivotPackage.OPERATION__OWNING_TYPE:
 				return getOwningType();
 			case PivotPackage.OPERATION__IS_INVALIDATING:
 				return isInvalidating();
 			case PivotPackage.OPERATION__IS_VALIDATING:
 				return isValidating();
 			case PivotPackage.OPERATION__PRECEDENCE:
 				if (resolve) return getPrecedence();
 				return basicGetPrecedence();
 			case PivotPackage.OPERATION__REDEFINED_OPERATION:
 				return getRedefinedOperation();
 			case PivotPackage.OPERATION__CLASS:
 				if (resolve) return getClass_();
 				return basicGetClass_();
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
 			case PivotPackage.OPERATION__OWNED_COMMENT:
 				getOwnedComment().clear();
 				getOwnedComment().addAll((Collection<? extends Comment>)newValue);
 				return;
 			case PivotPackage.OPERATION__EXTENSION:
 				getExtension().clear();
 				getExtension().addAll((Collection<? extends ElementExtension>)newValue);
 				return;
 			case PivotPackage.OPERATION__NAME:
 				setName((String)newValue);
 				return;
 			case PivotPackage.OPERATION__OWNED_RULE:
 				getOwnedRule().clear();
 				getOwnedRule().addAll((Collection<? extends Constraint>)newValue);
 				return;
 			case PivotPackage.OPERATION__IS_STATIC:
 				setIsStatic((Boolean)newValue);
 				return;
 			case PivotPackage.OPERATION__OWNED_ANNOTATION:
 				getOwnedAnnotation().clear();
 				getOwnedAnnotation().addAll((Collection<? extends Annotation>)newValue);
 				return;
 			case PivotPackage.OPERATION__TYPE:
 				setType((Type)newValue);
 				return;
 			case PivotPackage.OPERATION__IS_REQUIRED:
 				setIsRequired((Boolean)newValue);
 				return;
 			case PivotPackage.OPERATION__IMPLEMENTATION_CLASS:
 				setImplementationClass((String)newValue);
 				return;
 			case PivotPackage.OPERATION__IMPLEMENTATION:
 				setImplementation((LibraryFeature)newValue);
 				return;
 			case PivotPackage.OPERATION__TEMPLATE_BINDING:
 				getTemplateBinding().clear();
 				getTemplateBinding().addAll((Collection<? extends TemplateBinding>)newValue);
 				return;
 			case PivotPackage.OPERATION__OWNED_TEMPLATE_SIGNATURE:
 				setOwnedTemplateSignature((TemplateSignature)newValue);
 				return;
 			case PivotPackage.OPERATION__UNSPECIALIZED_ELEMENT:
 				setUnspecializedElement((TemplateableElement)newValue);
 				return;
 			case PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER:
 				setOwningTemplateParameter((TemplateParameter)newValue);
 				return;
 			case PivotPackage.OPERATION__TEMPLATE_PARAMETER:
 				setTemplateParameter((TemplateParameter)newValue);
 				return;
 			case PivotPackage.OPERATION__RAISED_EXCEPTION:
 				getRaisedException().clear();
 				getRaisedException().addAll((Collection<? extends Type>)newValue);
 				return;
 			case PivotPackage.OPERATION__OWNED_PARAMETER:
 				getOwnedParameter().clear();
 				getOwnedParameter().addAll((Collection<? extends Parameter>)newValue);
 				return;
 			case PivotPackage.OPERATION__OWNING_TYPE:
 				setOwningType((Type)newValue);
 				return;
 			case PivotPackage.OPERATION__IS_INVALIDATING:
 				setIsInvalidating((Boolean)newValue);
 				return;
 			case PivotPackage.OPERATION__IS_VALIDATING:
 				setIsValidating((Boolean)newValue);
 				return;
 			case PivotPackage.OPERATION__PRECEDENCE:
 				setPrecedence((Precedence)newValue);
 				return;
 			case PivotPackage.OPERATION__REDEFINED_OPERATION:
 				getRedefinedOperation().clear();
 				getRedefinedOperation().addAll((Collection<? extends Operation>)newValue);
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
 			case PivotPackage.OPERATION__OWNED_COMMENT:
 				getOwnedComment().clear();
 				return;
 			case PivotPackage.OPERATION__EXTENSION:
 				getExtension().clear();
 				return;
 			case PivotPackage.OPERATION__NAME:
 				setName(NAME_EDEFAULT);
 				return;
 			case PivotPackage.OPERATION__OWNED_RULE:
 				getOwnedRule().clear();
 				return;
 			case PivotPackage.OPERATION__IS_STATIC:
 				setIsStatic(IS_STATIC_EDEFAULT);
 				return;
 			case PivotPackage.OPERATION__OWNED_ANNOTATION:
 				getOwnedAnnotation().clear();
 				return;
 			case PivotPackage.OPERATION__TYPE:
 				setType((Type)null);
 				return;
 			case PivotPackage.OPERATION__IS_REQUIRED:
 				setIsRequired(IS_REQUIRED_EDEFAULT);
 				return;
 			case PivotPackage.OPERATION__IMPLEMENTATION_CLASS:
 				setImplementationClass(IMPLEMENTATION_CLASS_EDEFAULT);
 				return;
 			case PivotPackage.OPERATION__IMPLEMENTATION:
 				setImplementation(IMPLEMENTATION_EDEFAULT);
 				return;
 			case PivotPackage.OPERATION__TEMPLATE_BINDING:
 				getTemplateBinding().clear();
 				return;
 			case PivotPackage.OPERATION__OWNED_TEMPLATE_SIGNATURE:
 				setOwnedTemplateSignature((TemplateSignature)null);
 				return;
 			case PivotPackage.OPERATION__UNSPECIALIZED_ELEMENT:
 				setUnspecializedElement((TemplateableElement)null);
 				return;
 			case PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER:
 				setOwningTemplateParameter((TemplateParameter)null);
 				return;
 			case PivotPackage.OPERATION__TEMPLATE_PARAMETER:
 				setTemplateParameter((TemplateParameter)null);
 				return;
 			case PivotPackage.OPERATION__RAISED_EXCEPTION:
 				getRaisedException().clear();
 				return;
 			case PivotPackage.OPERATION__OWNED_PARAMETER:
 				getOwnedParameter().clear();
 				return;
 			case PivotPackage.OPERATION__OWNING_TYPE:
 				setOwningType((Type)null);
 				return;
 			case PivotPackage.OPERATION__IS_INVALIDATING:
 				setIsInvalidating(IS_INVALIDATING_EDEFAULT);
 				return;
 			case PivotPackage.OPERATION__IS_VALIDATING:
 				setIsValidating(IS_VALIDATING_EDEFAULT);
 				return;
 			case PivotPackage.OPERATION__PRECEDENCE:
 				setPrecedence((Precedence)null);
 				return;
 			case PivotPackage.OPERATION__REDEFINED_OPERATION:
 				getRedefinedOperation().clear();
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
 			case PivotPackage.OPERATION__OWNED_COMMENT:
 				return ownedComment != null && !ownedComment.isEmpty();
 			case PivotPackage.OPERATION__EXTENSION:
 				return extension != null && !extension.isEmpty();
 			case PivotPackage.OPERATION__NAME:
 				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
 			case PivotPackage.OPERATION__OWNED_RULE:
 				return ownedRule != null && !ownedRule.isEmpty();
 			case PivotPackage.OPERATION__IS_STATIC:
 				return isSetIsStatic();
 			case PivotPackage.OPERATION__OWNED_ANNOTATION:
 				return ownedAnnotation != null && !ownedAnnotation.isEmpty();
 			case PivotPackage.OPERATION__TYPE:
 				return type != null;
 			case PivotPackage.OPERATION__IS_REQUIRED:
 				return ((eFlags & IS_REQUIRED_EFLAG) != 0) != IS_REQUIRED_EDEFAULT;
 			case PivotPackage.OPERATION__IMPLEMENTATION_CLASS:
 				return IMPLEMENTATION_CLASS_EDEFAULT == null ? implementationClass != null : !IMPLEMENTATION_CLASS_EDEFAULT.equals(implementationClass);
 			case PivotPackage.OPERATION__IMPLEMENTATION:
 				return IMPLEMENTATION_EDEFAULT == null ? implementation != null : !IMPLEMENTATION_EDEFAULT.equals(implementation);
 			case PivotPackage.OPERATION__TEMPLATE_BINDING:
 				return templateBinding != null && !templateBinding.isEmpty();
 			case PivotPackage.OPERATION__OWNED_TEMPLATE_SIGNATURE:
 				return ownedTemplateSignature != null;
 			case PivotPackage.OPERATION__UNSPECIALIZED_ELEMENT:
 				return unspecializedElement != null;
 			case PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER:
 				return getOwningTemplateParameter() != null;
 			case PivotPackage.OPERATION__TEMPLATE_PARAMETER:
 				return isSetTemplateParameter();
 			case PivotPackage.OPERATION__RAISED_EXCEPTION:
 				return raisedException != null && !raisedException.isEmpty();
 			case PivotPackage.OPERATION__OWNED_PARAMETER:
 				return ownedParameter != null && !ownedParameter.isEmpty();
 			case PivotPackage.OPERATION__OWNING_TYPE:
 				return getOwningType() != null;
 			case PivotPackage.OPERATION__IS_INVALIDATING:
 				return ((eFlags & IS_INVALIDATING_EFLAG) != 0) != IS_INVALIDATING_EDEFAULT;
 			case PivotPackage.OPERATION__IS_VALIDATING:
 				return ((eFlags & IS_VALIDATING_EFLAG) != 0) != IS_VALIDATING_EDEFAULT;
 			case PivotPackage.OPERATION__PRECEDENCE:
 				return precedence != null;
 			case PivotPackage.OPERATION__REDEFINED_OPERATION:
 				return redefinedOperation != null && !redefinedOperation.isEmpty();
 			case PivotPackage.OPERATION__CLASS:
 				return basicGetClass_() != null;
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
 		if (baseClass == Namespace.class)
 		{
 			switch (derivedFeatureID)
 			{
 				default: return -1;
 			}
 		}
 		if (baseClass == TemplateableElement.class)
 		{
 			switch (derivedFeatureID)
 			{
 				case PivotPackage.OPERATION__TEMPLATE_BINDING: return PivotPackage.TEMPLATEABLE_ELEMENT__TEMPLATE_BINDING;
 				case PivotPackage.OPERATION__OWNED_TEMPLATE_SIGNATURE: return PivotPackage.TEMPLATEABLE_ELEMENT__OWNED_TEMPLATE_SIGNATURE;
 				case PivotPackage.OPERATION__UNSPECIALIZED_ELEMENT: return PivotPackage.TEMPLATEABLE_ELEMENT__UNSPECIALIZED_ELEMENT;
 				default: return -1;
 			}
 		}
 		if (baseClass == ParameterableElement.class)
 		{
 			switch (derivedFeatureID)
 			{
 				case PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER: return PivotPackage.PARAMETERABLE_ELEMENT__OWNING_TEMPLATE_PARAMETER;
 				case PivotPackage.OPERATION__TEMPLATE_PARAMETER: return PivotPackage.PARAMETERABLE_ELEMENT__TEMPLATE_PARAMETER;
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
 		if (baseClass == Namespace.class)
 		{
 			switch (baseFeatureID)
 			{
 				default: return -1;
 			}
 		}
 		if (baseClass == TemplateableElement.class)
 		{
 			switch (baseFeatureID)
 			{
 				case PivotPackage.TEMPLATEABLE_ELEMENT__TEMPLATE_BINDING: return PivotPackage.OPERATION__TEMPLATE_BINDING;
 				case PivotPackage.TEMPLATEABLE_ELEMENT__OWNED_TEMPLATE_SIGNATURE: return PivotPackage.OPERATION__OWNED_TEMPLATE_SIGNATURE;
 				case PivotPackage.TEMPLATEABLE_ELEMENT__UNSPECIALIZED_ELEMENT: return PivotPackage.OPERATION__UNSPECIALIZED_ELEMENT;
 				default: return -1;
 			}
 		}
 		if (baseClass == ParameterableElement.class)
 		{
 			switch (baseFeatureID)
 			{
 				case PivotPackage.PARAMETERABLE_ELEMENT__OWNING_TEMPLATE_PARAMETER: return PivotPackage.OPERATION__OWNING_TEMPLATE_PARAMETER;
 				case PivotPackage.PARAMETERABLE_ELEMENT__TEMPLATE_PARAMETER: return PivotPackage.OPERATION__TEMPLATE_PARAMETER;
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
 		if (baseClass == Namespace.class)
 		{
 			switch (baseOperationID)
 			{
 				default: return -1;
 			}
 		}
 		if (baseClass == TemplateableElement.class)
 		{
 			switch (baseOperationID)
 			{
 				case PivotPackage.TEMPLATEABLE_ELEMENT___PARAMETERABLE_ELEMENTS: return PivotPackage.OPERATION___PARAMETERABLE_ELEMENTS;
 				case PivotPackage.TEMPLATEABLE_ELEMENT___IS_TEMPLATE: return PivotPackage.OPERATION___IS_TEMPLATE;
 				default: return -1;
 			}
 		}
 		if (baseClass == ParameterableElement.class)
 		{
 			switch (baseOperationID)
 			{
 				case PivotPackage.PARAMETERABLE_ELEMENT___IS_TEMPLATE_PARAMETER: return PivotPackage.OPERATION___IS_TEMPLATE_PARAMETER;
 				case PivotPackage.PARAMETERABLE_ELEMENT___IS_COMPATIBLE_WITH__PARAMETERABLEELEMENT: return PivotPackage.OPERATION___IS_COMPATIBLE_WITH__PARAMETERABLEELEMENT;
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
 			case PivotPackage.OPERATION___ALL_OWNED_ELEMENTS:
 				return allOwnedElements();
 			case PivotPackage.OPERATION___GET_VALUE__TYPE_STRING:
 				return getValue((Type)arguments.get(0), (String)arguments.get(1));
 			case PivotPackage.OPERATION___VALIDATE_NOT_OWN_SELF__DIAGNOSTICCHAIN_MAP:
 				return validateNotOwnSelf((DiagnosticChain)arguments.get(0), (Map<Object, Object>)arguments.get(1));
 			case PivotPackage.OPERATION___COMPATIBLE_BODY__VALUESPECIFICATION:
 				return CompatibleBody((ValueSpecification)arguments.get(0));
 			case PivotPackage.OPERATION___MAKE_PARAMETER:
 				return makeParameter();
 			case PivotPackage.OPERATION___PARAMETERABLE_ELEMENTS:
 				return parameterableElements();
 			case PivotPackage.OPERATION___IS_TEMPLATE:
 				return isTemplate();
 			case PivotPackage.OPERATION___IS_TEMPLATE_PARAMETER:
 				return isTemplateParameter();
 			case PivotPackage.OPERATION___IS_COMPATIBLE_WITH__PARAMETERABLEELEMENT:
 				return isCompatibleWith((ParameterableElement)arguments.get(0));
 			case PivotPackage.OPERATION___VALIDATE_COMPATIBLE_RETURN__DIAGNOSTICCHAIN_MAP:
 				return validateCompatibleReturn((DiagnosticChain)arguments.get(0), (Map<Object, Object>)arguments.get(1));
 			case PivotPackage.OPERATION___VALIDATE_LOADABLE_IMPLEMENTATION__DIAGNOSTICCHAIN_MAP:
 				return validateLoadableImplementation((DiagnosticChain)arguments.get(0), (Map<Object, Object>)arguments.get(1));
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
 		return visitor.visitOperation(this);
 	}
 
 	private LibraryFeature bodyImplementation = null;
 	
 	@Override
 	public @NonNull LibraryFeature getImplementation() {
 		LibraryFeature implementation = super.getImplementation();
 		if (implementation != null) {
 			return implementation;
 		}
 		LibraryFeature bodyImplementation2 = bodyImplementation;
 		if (bodyImplementation2 == null) {
 			for (Constraint rule : getOwnedRule()) {
 				String stereotype = rule.getStereotype();
 				if (UMLReflection.BODY.equals(stereotype)) {
 					ValueSpecification specification = rule.getSpecification();
 					if (specification instanceof ExpressionInOCL) {
 						bodyImplementation2 = new ConstrainedOperation((ExpressionInOCL) specification);
 					}
 				}
 			}
 		}
 		if (bodyImplementation2 == null) {
 			EObject eTarget = getETarget();
 			if ((eTarget instanceof EOperation) && (((EOperation)eTarget).getEType() != null)) {
 				bodyImplementation2 = new EInvokeOperation((EOperation)eTarget);
 			}
 			else {
 				bodyImplementation2 = UnsupportedOperation.INSTANCE;
 			}
 		}
 		bodyImplementation = bodyImplementation2;
 		return bodyImplementation2;
 	}
 
 	public int getIndex() {
 		return -1;		// WIP
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
 	
 	public @NonNull ParametersId getParametersId() {
 		return getOperationId().getParametersId();
 	}
 
 	public @NonNull DomainParameterTypes getParameterTypes() {
 		List<Parameter> ownedParameter = getOwnedParameter();
 		int iMax = ownedParameter.size();
 		DomainType[] types = new DomainType[iMax];
 		for (int i = 0; i < iMax; i++) {
 			types[i] = ownedParameter.get(i).getType();
 		}
 		return new DomainParameterTypes(types);
 	}
 
 	public @NonNull DomainTypeParameters getTypeParameters() {
 		return TemplateSignatureImpl.getTypeParameters(getOwnedTemplateSignature());
 	}
 
 	private OperationId operationId = null;
 	
 	public final @NonNull OperationId getOperationId() {
 		OperationId operationId2 = operationId;
 		if (operationId2 == null) {
 			synchronized (this) {
 				operationId2 = operationId;
 				if (operationId2 == null) {
 					operationId = operationId2 = IdManager.getOperationId(this);
 				}
 			}
 		}
 		return operationId2;
 	}
 } //OperationImpl
