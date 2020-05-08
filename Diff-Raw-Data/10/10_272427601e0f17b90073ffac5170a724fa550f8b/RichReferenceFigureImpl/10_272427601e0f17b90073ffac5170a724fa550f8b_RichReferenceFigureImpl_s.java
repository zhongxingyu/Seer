 /**
  * Copyright (c) 2010-2011, Jean-Francois Brazeau. All rights reserved.
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
  * 
  */
 package org.emftools.emf2gv.graphdesc.impl;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.emftools.emf2gv.graphdesc.ArrowStyle;
 import org.emftools.emf2gv.graphdesc.ClassFigure;
 import org.emftools.emf2gv.graphdesc.GraphdescPackage;
 import org.emftools.emf2gv.graphdesc.ReferenceFigure;
 import org.emftools.emf2gv.graphdesc.RichReferenceFigure;
 import org.emftools.emf2gv.graphdesc.util.GraphdescValidator;
 import org.emftools.validation.utils.EMFConstraintsHelper;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Rich Reference Figure</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.RichReferenceFigureImpl#getTargetEReference <em>Target EReference</em>}</li>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.RichReferenceFigureImpl#getSourceLabelEAttribute <em>Source Label EAttribute</em>}</li>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.RichReferenceFigureImpl#getStandardLabelEAttribute <em>Standard Label EAttribute</em>}</li>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.RichReferenceFigureImpl#getTargetLabelEAttribute <em>Target Label EAttribute</em>}</li>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.RichReferenceFigureImpl#getLabelDistance <em>Label Distance</em>}</li>
  *   <li>{@link org.emftools.emf2gv.graphdesc.impl.RichReferenceFigureImpl#getLabelAngle <em>Label Angle</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class RichReferenceFigureImpl extends ReferenceFigureImpl implements RichReferenceFigure {
 	/**
 	 * The cached value of the '{@link #getTargetEReference() <em>Target EReference</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTargetEReference()
 	 * @generated
 	 * @ordered
 	 */
 	protected EReference targetEReference;
 
 	/**
 	 * The cached value of the '{@link #getSourceLabelEAttribute() <em>Source Label EAttribute</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSourceLabelEAttribute()
 	 * @generated
 	 * @ordered
 	 */
 	protected EAttribute sourceLabelEAttribute;
 
 	/**
 	 * The cached value of the '{@link #getStandardLabelEAttribute() <em>Standard Label EAttribute</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getStandardLabelEAttribute()
 	 * @generated
 	 * @ordered
 	 */
 	protected EAttribute standardLabelEAttribute;
 
 	/**
 	 * The cached value of the '{@link #getTargetLabelEAttribute() <em>Target Label EAttribute</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getTargetLabelEAttribute()
 	 * @generated
 	 * @ordered
 	 */
 	protected EAttribute targetLabelEAttribute;
 
 	/**
 	 * The default value of the '{@link #getLabelDistance() <em>Label Distance</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLabelDistance()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double LABEL_DISTANCE_EDEFAULT = 1.0;
 
 	/**
 	 * The cached value of the '{@link #getLabelDistance() <em>Label Distance</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLabelDistance()
 	 * @generated
 	 * @ordered
 	 */
 	protected double labelDistance = LABEL_DISTANCE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getLabelAngle() <em>Label Angle</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLabelAngle()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double LABEL_ANGLE_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getLabelAngle() <em>Label Angle</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLabelAngle()
 	 * @generated
 	 * @ordered
 	 */
 	protected double labelAngle = LABEL_ANGLE_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected RichReferenceFigureImpl() {
 		super();
 		// Default attribute values for a rich reference figure
 		// (the minimum edge length and label distance must
 		// be adapted as a rich reference figure usually has 
 		// labels)
 		setMinimumEdgeLength(3);
 		setLabelDistance(5.0d);
 		setStyle(ArrowStyle.DASHED);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return GraphdescPackage.Literals.RICH_REFERENCE_FIGURE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getTargetEReference() {
 		if (targetEReference != null && targetEReference.eIsProxy()) {
 			InternalEObject oldTargetEReference = (InternalEObject)targetEReference;
 			targetEReference = (EReference)eResolveProxy(oldTargetEReference);
 			if (targetEReference != oldTargetEReference) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_EREFERENCE, oldTargetEReference, targetEReference));
 			}
 		}
 		return targetEReference;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference basicGetTargetEReference() {
 		return targetEReference;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
	 * @generated
 	 */
 	public void setTargetEReference(EReference newTargetEReference) {
 		EReference oldTargetEReference = targetEReference;
 		targetEReference = newTargetEReference;
		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_EREFERENCE, oldTargetEReference, targetEReference));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSourceLabelEAttribute() {
 		if (sourceLabelEAttribute != null && sourceLabelEAttribute.eIsProxy()) {
 			InternalEObject oldSourceLabelEAttribute = (InternalEObject)sourceLabelEAttribute;
 			sourceLabelEAttribute = (EAttribute)eResolveProxy(oldSourceLabelEAttribute);
 			if (sourceLabelEAttribute != oldSourceLabelEAttribute) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GraphdescPackage.RICH_REFERENCE_FIGURE__SOURCE_LABEL_EATTRIBUTE, oldSourceLabelEAttribute, sourceLabelEAttribute));
 			}
 		}
 		return sourceLabelEAttribute;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute basicGetSourceLabelEAttribute() {
 		return sourceLabelEAttribute;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSourceLabelEAttribute(EAttribute newSourceLabelEAttribute) {
 		EAttribute oldSourceLabelEAttribute = sourceLabelEAttribute;
 		sourceLabelEAttribute = newSourceLabelEAttribute;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.RICH_REFERENCE_FIGURE__SOURCE_LABEL_EATTRIBUTE, oldSourceLabelEAttribute, sourceLabelEAttribute));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getStandardLabelEAttribute() {
 		if (standardLabelEAttribute != null && standardLabelEAttribute.eIsProxy()) {
 			InternalEObject oldStandardLabelEAttribute = (InternalEObject)standardLabelEAttribute;
 			standardLabelEAttribute = (EAttribute)eResolveProxy(oldStandardLabelEAttribute);
 			if (standardLabelEAttribute != oldStandardLabelEAttribute) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GraphdescPackage.RICH_REFERENCE_FIGURE__STANDARD_LABEL_EATTRIBUTE, oldStandardLabelEAttribute, standardLabelEAttribute));
 			}
 		}
 		return standardLabelEAttribute;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute basicGetStandardLabelEAttribute() {
 		return standardLabelEAttribute;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setStandardLabelEAttribute(EAttribute newStandardLabelEAttribute) {
 		EAttribute oldStandardLabelEAttribute = standardLabelEAttribute;
 		standardLabelEAttribute = newStandardLabelEAttribute;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.RICH_REFERENCE_FIGURE__STANDARD_LABEL_EATTRIBUTE, oldStandardLabelEAttribute, standardLabelEAttribute));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getTargetLabelEAttribute() {
 		if (targetLabelEAttribute != null && targetLabelEAttribute.eIsProxy()) {
 			InternalEObject oldTargetLabelEAttribute = (InternalEObject)targetLabelEAttribute;
 			targetLabelEAttribute = (EAttribute)eResolveProxy(oldTargetLabelEAttribute);
 			if (targetLabelEAttribute != oldTargetLabelEAttribute) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_LABEL_EATTRIBUTE, oldTargetLabelEAttribute, targetLabelEAttribute));
 			}
 		}
 		return targetLabelEAttribute;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute basicGetTargetLabelEAttribute() {
 		return targetLabelEAttribute;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTargetLabelEAttribute(EAttribute newTargetLabelEAttribute) {
 		EAttribute oldTargetLabelEAttribute = targetLabelEAttribute;
 		targetLabelEAttribute = newTargetLabelEAttribute;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_LABEL_EATTRIBUTE, oldTargetLabelEAttribute, targetLabelEAttribute));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getLabelDistance() {
 		return labelDistance;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLabelDistance(double newLabelDistance) {
 		double oldLabelDistance = labelDistance;
 		labelDistance = newLabelDistance;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.RICH_REFERENCE_FIGURE__LABEL_DISTANCE, oldLabelDistance, labelDistance));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getLabelAngle() {
 		return labelAngle;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLabelAngle(double newLabelAngle) {
 		double oldLabelAngle = labelAngle;
 		labelAngle = newLabelAngle;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GraphdescPackage.RICH_REFERENCE_FIGURE__LABEL_ANGLE, oldLabelAngle, labelAngle));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_EREFERENCE:
 				if (resolve) return getTargetEReference();
 				return basicGetTargetEReference();
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__SOURCE_LABEL_EATTRIBUTE:
 				if (resolve) return getSourceLabelEAttribute();
 				return basicGetSourceLabelEAttribute();
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__STANDARD_LABEL_EATTRIBUTE:
 				if (resolve) return getStandardLabelEAttribute();
 				return basicGetStandardLabelEAttribute();
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_LABEL_EATTRIBUTE:
 				if (resolve) return getTargetLabelEAttribute();
 				return basicGetTargetLabelEAttribute();
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__LABEL_DISTANCE:
 				return getLabelDistance();
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__LABEL_ANGLE:
 				return getLabelAngle();
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
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_EREFERENCE:
 				setTargetEReference((EReference)newValue);
 				return;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__SOURCE_LABEL_EATTRIBUTE:
 				setSourceLabelEAttribute((EAttribute)newValue);
 				return;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__STANDARD_LABEL_EATTRIBUTE:
 				setStandardLabelEAttribute((EAttribute)newValue);
 				return;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_LABEL_EATTRIBUTE:
 				setTargetLabelEAttribute((EAttribute)newValue);
 				return;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__LABEL_DISTANCE:
 				setLabelDistance((Double)newValue);
 				return;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__LABEL_ANGLE:
 				setLabelAngle((Double)newValue);
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
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_EREFERENCE:
 				setTargetEReference((EReference)null);
 				return;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__SOURCE_LABEL_EATTRIBUTE:
 				setSourceLabelEAttribute((EAttribute)null);
 				return;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__STANDARD_LABEL_EATTRIBUTE:
 				setStandardLabelEAttribute((EAttribute)null);
 				return;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_LABEL_EATTRIBUTE:
 				setTargetLabelEAttribute((EAttribute)null);
 				return;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__LABEL_DISTANCE:
 				setLabelDistance(LABEL_DISTANCE_EDEFAULT);
 				return;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__LABEL_ANGLE:
 				setLabelAngle(LABEL_ANGLE_EDEFAULT);
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
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_EREFERENCE:
 				return targetEReference != null;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__SOURCE_LABEL_EATTRIBUTE:
 				return sourceLabelEAttribute != null;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__STANDARD_LABEL_EATTRIBUTE:
 				return standardLabelEAttribute != null;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__TARGET_LABEL_EATTRIBUTE:
 				return targetLabelEAttribute != null;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__LABEL_DISTANCE:
 				return labelDistance != LABEL_DISTANCE_EDEFAULT;
 			case GraphdescPackage.RICH_REFERENCE_FIGURE__LABEL_ANGLE:
 				return labelAngle != LABEL_ANGLE_EDEFAULT;
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
 		result.append(" (labelDistance: ");
 		result.append(labelDistance);
 		result.append(", labelAngle: ");
 		result.append(labelAngle);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	@Override
 	public String getName() {
 		StringWriter buf = new StringWriter();
 		buf.append(getEReference() != null ? getEReference().getName()
 				: "<unset>");
 		buf.append('.');
 		buf.append(getTargetEReference() != null ? getTargetEReference()
 				.getName() : "<unset>");
 		return buf.toString();
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	@Override
 	public boolean isContainment() {
 		return (getEReference() != null) && (getEReference().isContainment())
 				&& (getTargetEReference() != null)
 				&& (getTargetEReference().isContainment());
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	@Override
 	public EClass basicGetTargetEType() {
 		EClass result = null;
 		if (getTargetEReference() != null) {
 			result = getTargetEReference().getEReferenceType();
 		}
 		return result;
 	}
 	
 	@Override
 	public boolean validate(DiagnosticChain diagnostic,
 			Map<Object, Object> context) {
 		boolean valid = super.validate(diagnostic, context);
 		if (valid) {
 			EMFConstraintsHelper constraintsHelper = EMFConstraintsHelper
 					.getInstance(GraphdescValidator.DIAGNOSTIC_SOURCE);
 			ClassFigure classFigure = getClassFigure();
 			List<ReferenceFigure> referenceFigures = new ArrayList<ReferenceFigure>();
 			List<RichReferenceFigure> richReferenceFigures = new ArrayList<RichReferenceFigure>();
 			for (ReferenceFigure referenceFigure : classFigure.getReferenceFigures()) {
 				boolean isRichReference = referenceFigure instanceof RichReferenceFigure;
 				if (isRichReference) {
 					if (!richReferenceFigures.contains(referenceFigure))
 						richReferenceFigures.add((RichReferenceFigure) referenceFigure);
 				}
 				else {
 					if (!referenceFigures.contains(referenceFigure))
 						referenceFigures.add(referenceFigure);
 				}
 			}
 			// The rich reference figure musn't use a reference that is already used
 			// by a normal reference figure (getEReference cannot be null
 			// as the referenceFigure validation has been processed)
 			for (ReferenceFigure referenceFigure : referenceFigures) {
 				if (getEReference().equals(referenceFigure.getEReference())) {
 					constraintsHelper
 					.addError(
 							diagnostic,
 							this,
 							0,
 							"The rich reference figure is associated to an EReference ({0}) that is already used by a normal refence figure.",
 							eReference.getName());
 					valid = false;
 					break;
 				}
 			}
 			
 			// Target EReference Check
 			EReference targetEReference = getTargetEReference();
 			if (targetEReference == null) {
 				constraintsHelper
 						.addError(diagnostic, this, 0,
 								"The rich reference figure must be associated to a target EReference");
 				valid = false;
 			}
 			else {
 				// Check unique rich reference
 				for (RichReferenceFigure richReferenceFigure : richReferenceFigures) {
 					// A test is made in order not to create a marker for
 					// the first occurrence of the same { ref, target ref } couple
 					if (richReferenceFigure == this) {
 						break;
 					} else if (getEReference().equals(
 							richReferenceFigure.getEReference())
 							&& getTargetEReference().equals(
 									richReferenceFigure.getTargetEReference())) {
 						constraintsHelper
 								.addError(
 										diagnostic,
 										this,
 										0,
 										"The rich reference uses a [EReference, Target EReference] couple that is already used");
 						valid = false;
 						break;
 	
 					}
 				}
 	
 				// Labels validation
 				EClass eClass = targetEReference.getEContainingClass();
 				if (eClass != null) {
 					EAttribute srcLabelAttr = getSourceLabelEAttribute();
 					if (srcLabelAttr != null && !eClass.getEAllAttributes().contains(srcLabelAttr)) {
 						constraintsHelper
 								.addError(
 										diagnostic,
 										this,
 										0,
 										"The source label EAttribute ({0}) of the rich reference figure is not a member of the EClass ({1})",
 										srcLabelAttr.getName(), eClass.getName());
 						valid = false;
 					}
 					EAttribute stdLabelAttr = getStandardLabelEAttribute();
 					if (stdLabelAttr != null && !eClass.getEAllAttributes().contains(stdLabelAttr)) {
 						constraintsHelper
 								.addError(
 										diagnostic,
 										this,
 										0,
 										"The standard label EAttribute ({0}) of the rich reference figure is not a member of the EClass ({1})",
 										stdLabelAttr.getName(), eClass.getName());
 						valid = false;
 					}
 					EAttribute targetLabelAttr = getStandardLabelEAttribute();
 					if (targetLabelAttr != null && !eClass.getEAllAttributes().contains(targetLabelAttr)) {
 						constraintsHelper
 								.addError(
 										diagnostic,
 										this,
 										0,
 										"The target label EAttribute ({0}) of the rich reference figure is not a member of the EClass ({1})",
 										targetLabelAttr.getName(), eClass.getName());
 						valid = false;
 					}
 				}
 			}
 	
 		}
 		return valid;
 	}
 
 } //RichReferenceFigureImpl
