 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.gmf.bridge.internal.trace.impl;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.ocl.query.Query;
 import org.eclipse.emf.ocl.query.QueryFactory;
 
 import org.eclipse.gmf.bridge.internal.trace.GenLinkLabelTrace;
 import org.eclipse.gmf.bridge.internal.trace.GenLinkTrace;
 import org.eclipse.gmf.bridge.internal.trace.TracePackage;
 
 import org.eclipse.gmf.codegen.gmfgen.FeatureModelFacet;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.GenLink;
 import org.eclipse.gmf.codegen.gmfgen.TypeLinkModelFacet;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Gen Link Trace</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.gmf.bridge.internal.trace.impl.GenLinkTraceImpl#getLinkLabelTraces <em>Link Label Traces</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class GenLinkTraceImpl extends MatchingTraceImpl implements GenLinkTrace {
 	/**
 	 * The cached value of the '{@link #getLinkLabelTraces() <em>Link Label Traces</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLinkLabelTraces()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList linkLabelTraces = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected GenLinkTraceImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return TracePackage.Literals.GEN_LINK_TRACE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getLinkLabelTraces() {
 		if (linkLabelTraces == null) {
 			linkLabelTraces = new EObjectContainmentEList(GenLinkLabelTrace.class, this, TracePackage.GEN_LINK_TRACE__LINK_LABEL_TRACES);
 		}
 		return linkLabelTraces;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setContext(GenLink genLink) {
 		StringBuffer result = new StringBuffer();
 		result.append("modelFacet.oclIsKindOf(");
 		if (genLink.getModelFacet() instanceof FeatureModelFacet) {
 			EStructuralFeature feature = ((FeatureModelFacet) genLink.getModelFacet()).getMetaFeature().getEcoreFeature();
 			result.append("gmfgen::FeatureLinkModelFacet) and ");
 			result.append("(let _feature_:ecore::EStructuralFeature = modelFacet.oclAsType(gmfgen::FeatureLinkModelFacet).metaFeature.ecoreFeature in ");
 			result.append(getEStructuralFeatureComparison("_feature_", feature));
 			result.append(")");
 		} else if (genLink.getModelFacet() instanceof TypeLinkModelFacet) {
 			EClass eClass = ((TypeLinkModelFacet) genLink.getModelFacet()).getMetaClass().getEcoreClass();
 			result.append("gmfgen::TypeLinkModelFacet) and ");
 			result.append("(let _eClass_:ecore::EClass = modelFacet.oclAsType(gmfgen::TypeLinkModelFacet).metaClass.ecoreClass in ");
 			result.append(getEClassComparision("_eClass_", eClass));
 			result.append(")");
 		} else {
			throw new IllegalArgumentException("Incorrect gen link passed - Feature/TypeLinkModelFacet should be used");
 		}
 		setQueryText(result.toString());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case TracePackage.GEN_LINK_TRACE__LINK_LABEL_TRACES:
 				return ((InternalEList)getLinkLabelTraces()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case TracePackage.GEN_LINK_TRACE__LINK_LABEL_TRACES:
 				return getLinkLabelTraces();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case TracePackage.GEN_LINK_TRACE__LINK_LABEL_TRACES:
 				getLinkLabelTraces().clear();
 				getLinkLabelTraces().addAll((Collection)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case TracePackage.GEN_LINK_TRACE__LINK_LABEL_TRACES:
 				getLinkLabelTraces().clear();
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case TracePackage.GEN_LINK_TRACE__LINK_LABEL_TRACES:
 				return linkLabelTraces != null && !linkLabelTraces.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	public Query createQuery() {
 		return QueryFactory.eINSTANCE.createQuery(getQueryText(), GMFGenPackage.eINSTANCE.getGenLink());
 	}
 	
 } //GenLinkTraceImpl
