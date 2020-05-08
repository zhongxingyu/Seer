 /**
  * Copyright (c) 2010, Jean-Francois Brazeau. All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * 
  *  1. Redistributions of source code must retain the above copyright notice,
  *     this list of conditions and the following disclaimer.
  * 
  *  2. Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  * 
  *  3. The name of the author may not be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  * IMPLIEDWARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
  * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.emftools.emf2gv.graphdesc.impl;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.emftools.emf2gv.graphdesc.ArrowType;
 import org.emftools.emf2gv.graphdesc.ClassFigure;
 import org.emftools.emf2gv.graphdesc.GVFigureDescription;
 import org.emftools.emf2gv.graphdesc.GraphdescPackage;
 import org.emftools.emf2gv.graphdesc.ReferenceFigure;
 import org.emftools.emf2gv.graphdesc.util.GraphdescValidator;
 import org.emftools.validation.utils.EMFConstraintsHelper;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '
  * <em><b>Reference Figure</b></em>'. <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.ReferenceFigureImpl#getClassFigure <em>Class Figure</em>}</li>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.ReferenceFigureImpl#getEReference <em>EReference</em>}</li>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.ReferenceFigureImpl#getTargetArrowType <em>Target Arrow Type</em>}</li>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.ReferenceFigureImpl#getSourceArrowType <em>Source Arrow Type</em>}</li>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.ReferenceFigureImpl#isContainment <em>Containment</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ReferenceFigureImpl extends AbstractFigureImpl implements
 		ReferenceFigure {
 	/**
 	 * The cached value of the '{@link #getEReference() <em>EReference</em>}' reference.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getEReference()
 	 * @generated
 	 * @ordered
 	 */
 	protected EReference eReference;
 
 	/**
 	 * The default value of the '{@link #getTargetArrowType() <em>Target Arrow Type</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getTargetArrowType()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ArrowType TARGET_ARROW_TYPE_EDEFAULT = ArrowType.NORMAL;
 
 	/**
 	 * The cached value of the '{@link #getTargetArrowType() <em>Target Arrow Type</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getTargetArrowType()
 	 * @generated
 	 * @ordered
 	 */
 	protected ArrowType targetArrowType = TARGET_ARROW_TYPE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getSourceArrowType() <em>Source Arrow Type</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getSourceArrowType()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ArrowType SOURCE_ARROW_TYPE_EDEFAULT = ArrowType.NONE;
 
 	/**
 	 * The cached value of the '{@link #getSourceArrowType() <em>Source Arrow Type</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getSourceArrowType()
 	 * @generated
 	 * @ordered
 	 */
 	protected ArrowType sourceArrowType = SOURCE_ARROW_TYPE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isContainment() <em>Containment</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #isContainment()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean CONTAINMENT_EDEFAULT = false;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ReferenceFigureImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return GraphdescPackage.Literals.REFERENCE_FIGURE;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getEReference() {
 		if (eReference != null && eReference.eIsProxy()) {
 			InternalEObject oldEReference = (InternalEObject)eReference;
 			eReference = (EReference)eResolveProxy(oldEReference);
 			if (eReference != oldEReference) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GraphdescPackage.REFERENCE_FIGURE__EREFERENCE, oldEReference, eReference));
 			}
 		}
 		return eReference;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference basicGetEReference() {
 		return eReference;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void setEReference(EReference newEReference) {
 		String oldName = getName();
 		boolean oldContainment = isContainment();
 		EReference oldEReference = eReference;
 		eReference = newEReference;
 		if (eNotificationRequired()) {
 			eNotify(new ENotificationImpl(this, Notification.SET,
 					GraphdescPackage.REFERENCE_FIGURE__EREFERENCE,
 					oldEReference, eReference));
 			eNotify(new ENotificationImpl(this, Notification.SET,
 					GraphdescPackage.REFERENCE_FIGURE__NAME, oldName, getName()));
 			eNotify(new ENotificationImpl(this, Notification.SET,
 					GraphdescPackage.REFERENCE_FIGURE__CONTAINMENT,
 					oldContainment, isContainment()));
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public String getName() {
 		String result = null;
 		if (getEReference() != null) {
 			result = getEReference().getName();
 		}
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ClassFigure getClassFigure() {
 		if (eContainerFeatureID() != GraphdescPackage.REFERENCE_FIGURE__CLASS_FIGURE) return null;
 		return (ClassFigure)eContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetClassFigure(ClassFigure newClassFigure,
 			NotificationChain msgs) {
 		msgs = eBasicSetContainer((InternalEObject)newClassFigure, GraphdescPackage.REFERENCE_FIGURE__CLASS_FIGURE, msgs);
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setClassFigure(ClassFigure newClassFigure) {
 		if (newClassFigure != eInternalContainer() || (eContainerFeatureID() != GraphdescPackage.REFERENCE_FIGURE__CLASS_FIGURE && newClassFigure != null)) {
 			if (EcoreUtil.isAncestor(this, newClassFigure))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
 			NotificationChain msgs = null;
 			if (eInternalContainer() != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newClassFigure != null)
 				msgs = ((InternalEObject)newClassFigure).eInverseAdd(this, GraphdescPackage.CLASS_FIGURE__REFERENCE_FIGURES, ClassFigure.class, msgs);
 			msgs = basicSetClassFigure(newClassFigure, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.REFERENCE_FIGURE__CLASS_FIGURE, newClassFigure, newClassFigure));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ArrowType getTargetArrowType() {
 		return targetArrowType;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTargetArrowType(ArrowType newTargetArrowType) {
 		ArrowType oldTargetArrowType = targetArrowType;
 		targetArrowType = newTargetArrowType == null ? TARGET_ARROW_TYPE_EDEFAULT : newTargetArrowType;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.REFERENCE_FIGURE__TARGET_ARROW_TYPE, oldTargetArrowType, targetArrowType));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ArrowType getSourceArrowType() {
 		return sourceArrowType;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSourceArrowType(ArrowType newSourceArrowType) {
 		ArrowType oldSourceArrowType = sourceArrowType;
 		sourceArrowType = newSourceArrowType == null ? SOURCE_ARROW_TYPE_EDEFAULT : newSourceArrowType;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.REFERENCE_FIGURE__SOURCE_ARROW_TYPE, oldSourceArrowType, sourceArrowType));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean isContainment() {
 		boolean result = false;
 		if (getEReference() != null) {
 			result = getEReference().isContainment();
 		}
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validate(DiagnosticChain diagnostic,
 			Map<Object, Object> context) {
 		EMFConstraintsHelper constraintsHelper = EMFConstraintsHelper
 				.getInstance(GraphdescValidator.DIAGNOSTIC_SOURCE);
 		boolean valid = true;
 		ClassFigure classFigure = getClassFigure();
 		if (classFigure == null) {
 			constraintsHelper.addError(diagnostic, this, 0,
 					"The reference figure must be contained in a class figure");
 			valid = false;
 		} else {
 			EReference eReference = getEReference();
 			if (eReference == null) {
 				constraintsHelper
 						.addError(diagnostic, this, 0,
 								"The reference figure must be associated to an EReference");
 				valid = false;
 			} else {
 				// Check unique
 				valid = !constraintsHelper.addErrorIfNotUnique(classFigure
 						.getReferenceFigures(), GraphdescPackage.eINSTANCE
 						.getReferenceFigure_EReference(), diagnostic, this, 0,
						"The EReference {0} is referenced twice or more",
 						eReference.getName());
 
 				// Check eReference eClass
 				if (valid) {
 					EClass eClass = classFigure.getEClass();
 					if (eClass != null) {
 						if (!eClass.getEAllReferences().contains(eReference)) {
 							constraintsHelper
 									.addError(
 											diagnostic,
 											this,
 											0,
 											"The reference figure is associated to an EReference ({0}) that is not a member of the parent class figure''s EClass ({1})",
 											eReference.getName(),
 											eClass.getName());
 							valid = false;
 						}
 					}
 				}
 
 				// At least on of the EReference target type subclasses must be
 				// declared in the graph description
 				if (valid) {
 					if (!targetClassFigureExists()) {
 						constraintsHelper
 								.addError(
 										diagnostic,
 										this,
 										0,
 										"The is no class figure associated to the reference type ({0}) or one of its sub-EClass",
 										eReference.getEReferenceType().getName());
 						valid = false;
 					}
 				}
 
 			}
 		}
 		return valid;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean targetClassFigureExists() {
 		boolean refTypeEClassOrSubEClassFigureFound = false;
 		EClass refTypeEClass = eReference.getEReferenceType();
 		ClassFigure classFigure = getClassFigure();
 		if (classFigure != null) {
 			GVFigureDescription gvFigureDescription = classFigure
 					.getGvFigureDescription();
 
 			if (gvFigureDescription != null) {
 				// If the ref type has a ClassFigure, that's OK !
 				if (gvFigureDescription.getClassFigure(refTypeEClass) != null) {
 					refTypeEClassOrSubEClassFigureFound = true;
 				}
 				// Else let's see if there is a ClassFigure that is
 				// associated
 				// to an EClass that derives from the refTypeEClass
 				else {
 					List<ClassFigure> classFigures = gvFigureDescription
 							.getClassFigures();
 					for (int i = 0; i < classFigures.size()
 							&& !refTypeEClassOrSubEClassFigureFound; i++) {
 						ClassFigure classFigureCursor = classFigures.get(i);
 						EClass eClass = classFigureCursor.getEClass();
 						if (eClass != null) {
 							refTypeEClassOrSubEClassFigureFound = refTypeEClass
 									.isSuperTypeOf(eClass);
 						}
 					}
 				}
 			}
 		}
 		return refTypeEClassOrSubEClassFigureFound;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd,
 			int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case GraphdescPackage.REFERENCE_FIGURE__CLASS_FIGURE:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return basicSetClassFigure((ClassFigure)otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd,
 			int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case GraphdescPackage.REFERENCE_FIGURE__CLASS_FIGURE:
 				return basicSetClassFigure(null, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eBasicRemoveFromContainerFeature(
 			NotificationChain msgs) {
 		switch (eContainerFeatureID()) {
 			case GraphdescPackage.REFERENCE_FIGURE__CLASS_FIGURE:
 				return eInternalContainer().eInverseRemove(this, GraphdescPackage.CLASS_FIGURE__REFERENCE_FIGURES, ClassFigure.class, msgs);
 		}
 		return super.eBasicRemoveFromContainerFeature(msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case GraphdescPackage.REFERENCE_FIGURE__CLASS_FIGURE:
 				return getClassFigure();
 			case GraphdescPackage.REFERENCE_FIGURE__EREFERENCE:
 				if (resolve) return getEReference();
 				return basicGetEReference();
 			case GraphdescPackage.REFERENCE_FIGURE__TARGET_ARROW_TYPE:
 				return getTargetArrowType();
 			case GraphdescPackage.REFERENCE_FIGURE__SOURCE_ARROW_TYPE:
 				return getSourceArrowType();
 			case GraphdescPackage.REFERENCE_FIGURE__CONTAINMENT:
 				return isContainment();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case GraphdescPackage.REFERENCE_FIGURE__CLASS_FIGURE:
 				setClassFigure((ClassFigure)newValue);
 				return;
 			case GraphdescPackage.REFERENCE_FIGURE__EREFERENCE:
 				setEReference((EReference)newValue);
 				return;
 			case GraphdescPackage.REFERENCE_FIGURE__TARGET_ARROW_TYPE:
 				setTargetArrowType((ArrowType)newValue);
 				return;
 			case GraphdescPackage.REFERENCE_FIGURE__SOURCE_ARROW_TYPE:
 				setSourceArrowType((ArrowType)newValue);
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
 			case GraphdescPackage.REFERENCE_FIGURE__CLASS_FIGURE:
 				setClassFigure((ClassFigure)null);
 				return;
 			case GraphdescPackage.REFERENCE_FIGURE__EREFERENCE:
 				setEReference((EReference)null);
 				return;
 			case GraphdescPackage.REFERENCE_FIGURE__TARGET_ARROW_TYPE:
 				setTargetArrowType(TARGET_ARROW_TYPE_EDEFAULT);
 				return;
 			case GraphdescPackage.REFERENCE_FIGURE__SOURCE_ARROW_TYPE:
 				setSourceArrowType(SOURCE_ARROW_TYPE_EDEFAULT);
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
 			case GraphdescPackage.REFERENCE_FIGURE__CLASS_FIGURE:
 				return getClassFigure() != null;
 			case GraphdescPackage.REFERENCE_FIGURE__EREFERENCE:
 				return eReference != null;
 			case GraphdescPackage.REFERENCE_FIGURE__TARGET_ARROW_TYPE:
 				return targetArrowType != TARGET_ARROW_TYPE_EDEFAULT;
 			case GraphdescPackage.REFERENCE_FIGURE__SOURCE_ARROW_TYPE:
 				return sourceArrowType != SOURCE_ARROW_TYPE_EDEFAULT;
 			case GraphdescPackage.REFERENCE_FIGURE__CONTAINMENT:
 				return isContainment() != CONTAINMENT_EDEFAULT;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (targetArrowType: ");
 		result.append(targetArrowType);
 		result.append(", sourceArrowType: ");
 		result.append(sourceArrowType);
 		result.append(')');
 		return result.toString();
 	}
 
 } // ReferenceFigureImpl
