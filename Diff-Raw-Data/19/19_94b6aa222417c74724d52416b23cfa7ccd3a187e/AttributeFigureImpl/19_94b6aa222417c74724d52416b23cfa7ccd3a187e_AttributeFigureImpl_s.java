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
 
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.emftools.emf2gv.graphdesc.AttributeFigure;
 import org.emftools.emf2gv.graphdesc.ClassFigure;
 import org.emftools.emf2gv.graphdesc.GraphdescPackage;
 import org.emftools.emf2gv.graphdesc.util.GraphdescValidator;
 import org.emftools.validation.utils.EMFConstraintsHelper;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Attribute Figure</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.AttributeFigureImpl#getLabel <em>Label</em>}</li>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.AttributeFigureImpl#getClassFigure <em>Class Figure</em>}</li>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.AttributeFigureImpl#getEAttribute <em>EAttribute</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class AttributeFigureImpl extends AbstractFigureImpl implements AttributeFigure {
 	/**
 	 * The default value of the '{@link #getLabel() <em>Label</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLabel()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String LABEL_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLabel() <em>Label</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLabel()
 	 * @generated
 	 * @ordered
 	 */
 	protected String label = LABEL_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getEAttribute() <em>EAttribute</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEAttribute()
 	 * @generated
 	 * @ordered
 	 */
 	protected EAttribute eAttribute;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected AttributeFigureImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return GraphdescPackage.Literals.ATTRIBUTE_FIGURE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getName() {
 		String result = null;
 		if (getEAttribute() != null) {
 			result = getEAttribute().getName();
 		}
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getLabel() {
 		return label;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLabel(String newLabel) {
 		String oldLabel = label;
 		label = newLabel;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.ATTRIBUTE_FIGURE__LABEL, oldLabel, label));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ClassFigure getClassFigure() {
 		if (eContainerFeatureID() != GraphdescPackage.ATTRIBUTE_FIGURE__CLASS_FIGURE) return null;
 		return (ClassFigure)eContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetClassFigure(ClassFigure newClassFigure, NotificationChain msgs) {
 		msgs = eBasicSetContainer((InternalEObject)newClassFigure, GraphdescPackage.ATTRIBUTE_FIGURE__CLASS_FIGURE, msgs);
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setClassFigure(ClassFigure newClassFigure) {
 		if (newClassFigure != eInternalContainer() || (eContainerFeatureID() != GraphdescPackage.ATTRIBUTE_FIGURE__CLASS_FIGURE && newClassFigure != null)) {
 			if (EcoreUtil.isAncestor(this, newClassFigure))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
 			NotificationChain msgs = null;
 			if (eInternalContainer() != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newClassFigure != null)
 				msgs = ((InternalEObject)newClassFigure).eInverseAdd(this, GraphdescPackage.CLASS_FIGURE__ATTRIBUTE_FIGURES, ClassFigure.class, msgs);
 			msgs = basicSetClassFigure(newClassFigure, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.ATTRIBUTE_FIGURE__CLASS_FIGURE, newClassFigure, newClassFigure));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getEAttribute() {
 		if (eAttribute != null && eAttribute.eIsProxy()) {
 			InternalEObject oldEAttribute = (InternalEObject)eAttribute;
 			eAttribute = (EAttribute)eResolveProxy(oldEAttribute);
 			if (eAttribute != oldEAttribute) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GraphdescPackage.ATTRIBUTE_FIGURE__EATTRIBUTE, oldEAttribute, eAttribute));
 			}
 		}
 		return eAttribute;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute basicGetEAttribute() {
 		return eAttribute;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setEAttribute(EAttribute newEAttribute) {
 		String oldName = getName();
 		EAttribute oldEAttribute = eAttribute;
 		eAttribute = newEAttribute;
 		if (eNotificationRequired()) {
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.ATTRIBUTE_FIGURE__EATTRIBUTE, oldEAttribute, eAttribute));
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.ATTRIBUTE_FIGURE__NAME, oldName, getName()));
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public boolean validate(DiagnosticChain diagnostic, Map<Object, Object> context) {
 		EMFConstraintsHelper constraintsHelper = EMFConstraintsHelper.getInstance(GraphdescValidator.DIAGNOSTIC_SOURCE);
 		boolean valid = true;
 		ClassFigure classFigure = getClassFigure();
 		if (classFigure == null) {
 			constraintsHelper.addError(diagnostic, this, 0, "The attribute figure must be contained in a class figure");
 			valid = false;
 		}
 		else {
 			EAttribute eAttribute = getEAttribute();
 			if (eAttribute == null) {
 				constraintsHelper.addError(diagnostic, this, 0, "The attribute figure must be associated to an EAttribute");
 				valid = false;
 			}
 			else {
 				// Check unique
 				valid = !constraintsHelper.addErrorIfNotUnique(classFigure.getAttributeFigures(),
 						GraphdescPackage.eINSTANCE.getAttributeFigure_EAttribute(), diagnostic, this,
						0, "The EAttribute {0} is referenced twice or more", eAttribute.getName());
 
 				// Check eAttribute eClass
 				if (valid) {
 					EClass eClass = classFigure.getEClass();
 					if (eClass != null) {
 						if (!eClass.getEAllAttributes().contains(eAttribute)) {
 							constraintsHelper.addError(diagnostic, this, 0, "The attribute figure is associated to an EAttribute ({0}) that is not a member of the parent class figure''s EClass ({1})", eAttribute.getName(), eClass.getName());
 							valid = false;
 						}
 					}
 				}
 				
 			}
 		}
 		return valid;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case GraphdescPackage.ATTRIBUTE_FIGURE__CLASS_FIGURE:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return basicSetClassFigure((ClassFigure)otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case GraphdescPackage.ATTRIBUTE_FIGURE__CLASS_FIGURE:
 				return basicSetClassFigure(null, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
 		switch (eContainerFeatureID()) {
 			case GraphdescPackage.ATTRIBUTE_FIGURE__CLASS_FIGURE:
 				return eInternalContainer().eInverseRemove(this, GraphdescPackage.CLASS_FIGURE__ATTRIBUTE_FIGURES, ClassFigure.class, msgs);
 		}
 		return super.eBasicRemoveFromContainerFeature(msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case GraphdescPackage.ATTRIBUTE_FIGURE__LABEL:
 				return getLabel();
 			case GraphdescPackage.ATTRIBUTE_FIGURE__CLASS_FIGURE:
 				return getClassFigure();
 			case GraphdescPackage.ATTRIBUTE_FIGURE__EATTRIBUTE:
 				if (resolve) return getEAttribute();
 				return basicGetEAttribute();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case GraphdescPackage.ATTRIBUTE_FIGURE__LABEL:
 				setLabel((String)newValue);
 				return;
 			case GraphdescPackage.ATTRIBUTE_FIGURE__CLASS_FIGURE:
 				setClassFigure((ClassFigure)newValue);
 				return;
 			case GraphdescPackage.ATTRIBUTE_FIGURE__EATTRIBUTE:
 				setEAttribute((EAttribute)newValue);
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
 			case GraphdescPackage.ATTRIBUTE_FIGURE__LABEL:
 				setLabel(LABEL_EDEFAULT);
 				return;
 			case GraphdescPackage.ATTRIBUTE_FIGURE__CLASS_FIGURE:
 				setClassFigure((ClassFigure)null);
 				return;
 			case GraphdescPackage.ATTRIBUTE_FIGURE__EATTRIBUTE:
 				setEAttribute((EAttribute)null);
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
 			case GraphdescPackage.ATTRIBUTE_FIGURE__LABEL:
 				return LABEL_EDEFAULT == null ? label != null : !LABEL_EDEFAULT.equals(label);
 			case GraphdescPackage.ATTRIBUTE_FIGURE__CLASS_FIGURE:
 				return getClassFigure() != null;
 			case GraphdescPackage.ATTRIBUTE_FIGURE__EATTRIBUTE:
 				return eAttribute != null;
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
 		result.append(" (label: ");
 		result.append(label);
 		result.append(')');
 		return result.toString();
 	}
 
 } //AttributeFigureImpl
