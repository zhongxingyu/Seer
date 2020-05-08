 /***************************************************************************************************
  * Copyright (c) 2005, 2006 IBM Corporation and others. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: 
  *   IBM Corporation - initial API and implementation
  *   Oracle Corporation - revision
  **************************************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.emf.impl;
 
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.impl.EStringToStringMapEntryImpl;
 import org.eclipse.emf.ecore.util.BasicFeatureMap;
 import org.eclipse.emf.ecore.util.EcoreEMap;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.jst.jsf.facesconfig.emf.*;
 
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Document Root</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getMixed <em>Mixed</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getXMLNSPrefixMap <em>XMLNS Prefix Map</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getXSISchemaLocation <em>XSI Schema Location</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getAbsoluteOrdering <em>Absolute Ordering</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getActionListener <em>Action Listener</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getApplication <em>Application</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getApplicationFactory <em>Application Factory</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getAttribute <em>Attribute</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getAttributeClass <em>Attribute Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getAttributeExtension <em>Attribute Extension</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getAttributeName <em>Attribute Name</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getBehavior <em>Behavior</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getBehaviorClass <em>Behavior Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getBehaviorId <em>Behavior Id</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getBehaviorExtension <em>Behavior Extension</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getComponent <em>Component</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getComponentClass <em>Component Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getComponentExtension <em>Component Extension</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getComponentFamily <em>Component Family</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getComponentType <em>Component Type</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getConverter <em>Converter</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getConverterClass <em>Converter Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getConverterForClass <em>Converter For Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getConverterId <em>Converter Id</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getDefaultLocale <em>Default Locale</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getDefaultRenderKitId <em>Default Render Kit Id</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getDefaultValidators <em>Default Validators</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getDefaultValue <em>Default Value</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getDescription <em>Description</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getDisplayName <em>Display Name</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getExceptionHandlerFactory <em>Exception Handler Factory</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getExternalContextFactory <em>External Context Factory</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getFacesConfig <em>Faces Config</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getFacesContextFactory <em>Faces Context Factory</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getFacet <em>Facet</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getFacetExtension <em>Facet Extension</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getFacetName <em>Facet Name</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getFactory <em>Factory</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getFromAction <em>From Action</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getFromOutcome <em>From Outcome</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getFromViewId <em>From View Id</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getIcon <em>Icon</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getIf <em>If</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getKey <em>Key</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getKeyClass <em>Key Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getLargeIcon <em>Large Icon</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getLifecycle <em>Lifecycle</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getLifecycleFactory <em>Lifecycle Factory</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getListEntries <em>List Entries</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getLocaleConfig <em>Locale Config</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getManagedBean <em>Managed Bean</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getManagedBeanClass <em>Managed Bean Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getManagedBeanName <em>Managed Bean Name</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getManagedBeanScope <em>Managed Bean Scope</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getManagedProperty <em>Managed Property</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getMapEntries <em>Map Entries</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getMapEntry <em>Map Entry</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getMessageBundle <em>Message Bundle</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getName <em>Name</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getNavigationCase <em>Navigation Case</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getNavigationHandler <em>Navigation Handler</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getNavigationRule <em>Navigation Rule</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getNullValue <em>Null Value</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getOrdering <em>Ordering</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getOrderingOrdering <em>Ordering Ordering</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getOthers <em>Others</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getPartialViewContextFactory <em>Partial View Context Factory</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getPhaseListener <em>Phase Listener</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getProperty <em>Property</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getPropertyClass <em>Property Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getPropertyExtension <em>Property Extension</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getPropertyName <em>Property Name</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getPropertyResolver <em>Property Resolver</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getRedirect <em>Redirect</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getRedirectViewParam <em>Redirect View Param</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getReferencedBean <em>Referenced Bean</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getReferencedBeanClass <em>Referenced Bean Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getReferencedBeanName <em>Referenced Bean Name</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getRenderer <em>Renderer</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getRendererClass <em>Renderer Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getRendererExtension <em>Renderer Extension</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getRendererType <em>Renderer Type</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getRenderKit <em>Render Kit</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getRenderKitClass <em>Render Kit Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getRenderKitFactory <em>Render Kit Factory</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getRenderKitId <em>Render Kit Id</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getResourceHandler <em>Resource Handler</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getSmallIcon <em>Small Icon</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getSourceClass <em>Source Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getStateManager <em>State Manager</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getSuggestedValue <em>Suggested Value</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getSupportedLocale <em>Supported Locale</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getSystemEventClass <em>System Event Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getSystemEventListener <em>System Event Listener</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getSystemEventListenerClass <em>System Event Listener Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getTagHandlerDelegateFactory <em>Tag Handler Delegate Factory</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getToViewId <em>To View Id</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getValidator <em>Validator</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getValidatorClass <em>Validator Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getValidatorId <em>Validator Id</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getValue <em>Value</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getValueClass <em>Value Class</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getVariableResolver <em>Variable Resolver</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getViewDeclarationLanguageFactory <em>View Declaration Language Factory</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getViewHandler <em>View Handler</em>}</li>
  *   <li>{@link org.eclipse.jst.jsf.facesconfig.emf.impl.DocumentRootImpl#getVisitContextFactory <em>Visit Context Factory</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class DocumentRootImpl extends EObjectImpl implements DocumentRoot {
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2005, 2006 IBM Corporation and others"; //$NON-NLS-1$
 
     /**
 	 * The cached value of the '{@link #getMixed() <em>Mixed</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getMixed()
 	 * @generated
 	 * @ordered
 	 */
 	protected FeatureMap mixed;
 
     /**
 	 * The cached value of the '{@link #getXMLNSPrefixMap() <em>XMLNS Prefix Map</em>}' map.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getXMLNSPrefixMap()
 	 * @generated
 	 * @ordered
 	 */
 	protected EMap xMLNSPrefixMap;
 
     /**
 	 * The cached value of the '{@link #getXSISchemaLocation() <em>XSI Schema Location</em>}' map.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getXSISchemaLocation()
 	 * @generated
 	 * @ordered
 	 */
 	protected EMap xSISchemaLocation;
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected DocumentRootImpl() {
 		super();
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return FacesConfigPackage.Literals.DOCUMENT_ROOT;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FeatureMap getMixed() {
 		if (mixed == null) {
 			mixed = new BasicFeatureMap(this, FacesConfigPackage.DOCUMENT_ROOT__MIXED);
 		}
 		return mixed;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EMap getXMLNSPrefixMap() {
 		if (xMLNSPrefixMap == null) {
 			xMLNSPrefixMap = new EcoreEMap(EcorePackage.Literals.ESTRING_TO_STRING_MAP_ENTRY, EStringToStringMapEntryImpl.class, this, FacesConfigPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP);
 		}
 		return xMLNSPrefixMap;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EMap getXSISchemaLocation() {
 		if (xSISchemaLocation == null) {
 			xSISchemaLocation = new EcoreEMap(EcorePackage.Literals.ESTRING_TO_STRING_MAP_ENTRY, EStringToStringMapEntryImpl.class, this, FacesConfigPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION);
 		}
 		return xSISchemaLocation;
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AbsoluteOrderingType getAbsoluteOrdering() {
 		return (AbsoluteOrderingType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__ABSOLUTE_ORDERING, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newAbsoluteOrdering 
 	 * @param msgs 
 	 * @return  NotficationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetAbsoluteOrdering(AbsoluteOrderingType newAbsoluteOrdering, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__ABSOLUTE_ORDERING, newAbsoluteOrdering, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAbsoluteOrdering(AbsoluteOrderingType newAbsoluteOrdering) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__ABSOLUTE_ORDERING, newAbsoluteOrdering);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ActionListenerType getActionListener() {
 		return (ActionListenerType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__ACTION_LISTENER, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newActionListener 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetActionListener(ActionListenerType newActionListener, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__ACTION_LISTENER, newActionListener, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setActionListener(ActionListenerType newActionListener) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__ACTION_LISTENER, newActionListener);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ApplicationType getApplication() {
 		return (ApplicationType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__APPLICATION, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newApplication 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetApplication(ApplicationType newApplication, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__APPLICATION, newApplication, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setApplication(ApplicationType newApplication) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__APPLICATION, newApplication);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ApplicationFactoryType getApplicationFactory() {
 		return (ApplicationFactoryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__APPLICATION_FACTORY, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newApplicationFactory 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetApplicationFactory(ApplicationFactoryType newApplicationFactory, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__APPLICATION_FACTORY, newApplicationFactory, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setApplicationFactory(ApplicationFactoryType newApplicationFactory) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__APPLICATION_FACTORY, newApplicationFactory);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AttributeType getAttribute() {
 		return (AttributeType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newAttribute 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetAttribute(AttributeType newAttribute, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE, newAttribute, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAttribute(AttributeType newAttribute) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE, newAttribute);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AttributeClassType getAttributeClass() {
 		return (AttributeClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newAttributeClass 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetAttributeClass(AttributeClassType newAttributeClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE_CLASS, newAttributeClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAttributeClass(AttributeClassType newAttributeClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE_CLASS, newAttributeClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AttributeExtensionType getAttributeExtension() {
 		return (AttributeExtensionType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE_EXTENSION, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newAttributeExtension 
      * @param msgs 
      * @return the notification chiain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetAttributeExtension(AttributeExtensionType newAttributeExtension, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE_EXTENSION, newAttributeExtension, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAttributeExtension(AttributeExtensionType newAttributeExtension) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE_EXTENSION, newAttributeExtension);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AttributeNameType getAttributeName() {
 		return (AttributeNameType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE_NAME, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newAttributeName 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetAttributeName(AttributeNameType newAttributeName, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE_NAME, newAttributeName, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAttributeName(AttributeNameType newAttributeName) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__ATTRIBUTE_NAME, newAttributeName);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BehaviorType getBehavior() {
 		return (BehaviorType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newBehavior 
 	 * @param msgs NotificationChain
 	 * @return 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetBehavior(BehaviorType newBehavior, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR, newBehavior, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBehavior(BehaviorType newBehavior) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR, newBehavior);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BehaviorClassType getBehaviorClass() {
 		return (BehaviorClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR_CLASS, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newBehaviorClass 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetBehaviorClass(BehaviorClassType newBehaviorClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR_CLASS, newBehaviorClass, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBehaviorClass(BehaviorClassType newBehaviorClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR_CLASS, newBehaviorClass);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BehaviorIdType getBehaviorId() {
 		return (BehaviorIdType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR_ID, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newBehaviorId 
 	 * @param msgs 
 	 * @return NotificationChain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetBehaviorId(BehaviorIdType newBehaviorId, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR_ID, newBehaviorId, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBehaviorId(BehaviorIdType newBehaviorId) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR_ID, newBehaviorId);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BehaviorExtensionType getBehaviorExtension() {
 		return (BehaviorExtensionType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR_EXTENSION, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newBehaviorExtension 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetBehaviorExtension(BehaviorExtensionType newBehaviorExtension, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR_EXTENSION, newBehaviorExtension, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setBehaviorExtension(BehaviorExtensionType newBehaviorExtension) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__BEHAVIOR_EXTENSION, newBehaviorExtension);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ComponentType getComponent() {
 		return (ComponentType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newComponent 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetComponent(ComponentType newComponent, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT, newComponent, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setComponent(ComponentType newComponent) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT, newComponent);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ComponentClassType getComponentClass() {
 		return (ComponentClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newComponentClass 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetComponentClass(ComponentClassType newComponentClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_CLASS, newComponentClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setComponentClass(ComponentClassType newComponentClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_CLASS, newComponentClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ComponentExtensionType getComponentExtension() {
 		return (ComponentExtensionType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_EXTENSION, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newComponentExtension 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetComponentExtension(ComponentExtensionType newComponentExtension, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_EXTENSION, newComponentExtension, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setComponentExtension(ComponentExtensionType newComponentExtension) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_EXTENSION, newComponentExtension);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ComponentFamilyType getComponentFamily() {
 		return (ComponentFamilyType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_FAMILY, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newComponentFamily 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetComponentFamily(ComponentFamilyType newComponentFamily, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_FAMILY, newComponentFamily, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setComponentFamily(ComponentFamilyType newComponentFamily) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_FAMILY, newComponentFamily);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ComponentTypeType getComponentType() {
 		return (ComponentTypeType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_TYPE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newComponentType 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetComponentType(ComponentTypeType newComponentType, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_TYPE, newComponentType, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setComponentType(ComponentTypeType newComponentType) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__COMPONENT_TYPE, newComponentType);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ConverterType getConverter() {
 		return (ConverterType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newConverter 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetConverter(ConverterType newConverter, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER, newConverter, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setConverter(ConverterType newConverter) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER, newConverter);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ConverterClassType getConverterClass() {
 		return (ConverterClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newConverterClass 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetConverterClass(ConverterClassType newConverterClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER_CLASS, newConverterClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setConverterClass(ConverterClassType newConverterClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER_CLASS, newConverterClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ConverterForClassType getConverterForClass() {
 		return (ConverterForClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER_FOR_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newConverterForClass 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetConverterForClass(ConverterForClassType newConverterForClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER_FOR_CLASS, newConverterForClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setConverterForClass(ConverterForClassType newConverterForClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER_FOR_CLASS, newConverterForClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ConverterIdType getConverterId() {
 		return (ConverterIdType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER_ID, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newConverterId 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetConverterId(ConverterIdType newConverterId, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER_ID, newConverterId, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setConverterId(ConverterIdType newConverterId) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__CONVERTER_ID, newConverterId);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DefaultLocaleType getDefaultLocale() {
 		return (DefaultLocaleType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_LOCALE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newDefaultLocale 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetDefaultLocale(DefaultLocaleType newDefaultLocale, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_LOCALE, newDefaultLocale, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDefaultLocale(DefaultLocaleType newDefaultLocale) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_LOCALE, newDefaultLocale);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DefaultRenderKitIdType getDefaultRenderKitId() {
 		return (DefaultRenderKitIdType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_RENDER_KIT_ID, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newDefaultRenderKitId 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetDefaultRenderKitId(DefaultRenderKitIdType newDefaultRenderKitId, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_RENDER_KIT_ID, newDefaultRenderKitId, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDefaultRenderKitId(DefaultRenderKitIdType newDefaultRenderKitId) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_RENDER_KIT_ID, newDefaultRenderKitId);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DefaultValidatorsType getDefaultValidators() {
 		return (DefaultValidatorsType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_VALIDATORS, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newDefaultValidators 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetDefaultValidators(DefaultValidatorsType newDefaultValidators, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_VALIDATORS, newDefaultValidators, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDefaultValidators(DefaultValidatorsType newDefaultValidators) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_VALIDATORS, newDefaultValidators);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DefaultValueType getDefaultValue() {
 		return (DefaultValueType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_VALUE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newDefaultValue 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetDefaultValue(DefaultValueType newDefaultValue, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_VALUE, newDefaultValue, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDefaultValue(DefaultValueType newDefaultValue) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__DEFAULT_VALUE, newDefaultValue);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DescriptionType getDescription() {
 		return (DescriptionType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__DESCRIPTION, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newDescription 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetDescription(DescriptionType newDescription, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__DESCRIPTION, newDescription, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDescription(DescriptionType newDescription) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__DESCRIPTION, newDescription);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DisplayNameType getDisplayName() {
 		return (DisplayNameType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__DISPLAY_NAME, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newDisplayName 
      * @param msgs 
      * @return the notification 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetDisplayName(DisplayNameType newDisplayName, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__DISPLAY_NAME, newDisplayName, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDisplayName(DisplayNameType newDisplayName) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__DISPLAY_NAME, newDisplayName);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ExceptionHandlerFactoryType getExceptionHandlerFactory() {
 		return (ExceptionHandlerFactoryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__EXCEPTION_HANDLER_FACTORY, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newExceptionHandlerFactory 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetExceptionHandlerFactory(ExceptionHandlerFactoryType newExceptionHandlerFactory, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__EXCEPTION_HANDLER_FACTORY, newExceptionHandlerFactory, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setExceptionHandlerFactory(ExceptionHandlerFactoryType newExceptionHandlerFactory) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__EXCEPTION_HANDLER_FACTORY, newExceptionHandlerFactory);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ExternalContextFactoryType getExternalContextFactory() {
 		return (ExternalContextFactoryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__EXTERNAL_CONTEXT_FACTORY, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newExternalContextFactory 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetExternalContextFactory(ExternalContextFactoryType newExternalContextFactory, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__EXTERNAL_CONTEXT_FACTORY, newExternalContextFactory, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setExternalContextFactory(ExternalContextFactoryType newExternalContextFactory) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__EXTERNAL_CONTEXT_FACTORY, newExternalContextFactory);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacesConfigType getFacesConfig() {
 		return (FacesConfigType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACES_CONFIG, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newFacesConfig 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetFacesConfig(FacesConfigType newFacesConfig, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACES_CONFIG, newFacesConfig, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFacesConfig(FacesConfigType newFacesConfig) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACES_CONFIG, newFacesConfig);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacesContextFactoryType getFacesContextFactory() {
 		return (FacesContextFactoryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACES_CONTEXT_FACTORY, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newFacesContextFactory 
      * @param msgs 
      * @return NotificationChain
      * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetFacesContextFactory(FacesContextFactoryType newFacesContextFactory, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACES_CONTEXT_FACTORY, newFacesContextFactory, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFacesContextFactory(FacesContextFactoryType newFacesContextFactory) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACES_CONTEXT_FACTORY, newFacesContextFactory);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacetType getFacet() {
 		return (FacetType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACET, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newFacet 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetFacet(FacetType newFacet, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACET, newFacet, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFacet(FacetType newFacet) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACET, newFacet);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacetExtensionType getFacetExtension() {
 		return (FacetExtensionType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACET_EXTENSION, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newFacetExtension 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetFacetExtension(FacetExtensionType newFacetExtension, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACET_EXTENSION, newFacetExtension, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFacetExtension(FacetExtensionType newFacetExtension) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACET_EXTENSION, newFacetExtension);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FacetNameType getFacetName() {
 		return (FacetNameType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACET_NAME, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newFacetName 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetFacetName(FacetNameType newFacetName, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACET_NAME, newFacetName, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFacetName(FacetNameType newFacetName) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACET_NAME, newFacetName);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FactoryType getFactory() {
 		return (FactoryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACTORY, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newFactory 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetFactory(FactoryType newFactory, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACTORY, newFactory, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFactory(FactoryType newFactory) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__FACTORY, newFactory);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FromActionType getFromAction() {
 		return (FromActionType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__FROM_ACTION, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newFromAction 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetFromAction(FromActionType newFromAction, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__FROM_ACTION, newFromAction, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFromAction(FromActionType newFromAction) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__FROM_ACTION, newFromAction);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FromOutcomeType getFromOutcome() {
 		return (FromOutcomeType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__FROM_OUTCOME, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newFromOutcome 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetFromOutcome(FromOutcomeType newFromOutcome, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__FROM_OUTCOME, newFromOutcome, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFromOutcome(FromOutcomeType newFromOutcome) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__FROM_OUTCOME, newFromOutcome);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FromViewIdType getFromViewId() {
 		return (FromViewIdType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__FROM_VIEW_ID, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newFromViewId 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetFromViewId(FromViewIdType newFromViewId, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__FROM_VIEW_ID, newFromViewId, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFromViewId(FromViewIdType newFromViewId) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__FROM_VIEW_ID, newFromViewId);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IconType getIcon() {
 		return (IconType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__ICON, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newIcon 
      * @param msgs 
      * @return the notification  chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetIcon(IconType newIcon, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__ICON, newIcon, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIcon(IconType newIcon) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__ICON, newIcon);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IfType getIf() {
 		return (IfType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__IF, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newIf 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetIf(IfType newIf, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__IF, newIf, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIf(IfType newIf) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__IF, newIf);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public KeyType getKey() {
 		return (KeyType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__KEY, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newKey 
      * @param msgs 
      * @return the notification chiainn 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetKey(KeyType newKey, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__KEY, newKey, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setKey(KeyType newKey) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__KEY, newKey);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public KeyClassType getKeyClass() {
 		return (KeyClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__KEY_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newKeyClass 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetKeyClass(KeyClassType newKeyClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__KEY_CLASS, newKeyClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setKeyClass(KeyClassType newKeyClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__KEY_CLASS, newKeyClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public LargeIconType getLargeIcon() {
 		return (LargeIconType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__LARGE_ICON, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newLargeIcon 
      * @param msgs 
      * @return the notification  chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetLargeIcon(LargeIconType newLargeIcon, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__LARGE_ICON, newLargeIcon, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLargeIcon(LargeIconType newLargeIcon) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__LARGE_ICON, newLargeIcon);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public LifecycleType getLifecycle() {
 		return (LifecycleType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__LIFECYCLE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newLifecycle 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetLifecycle(LifecycleType newLifecycle, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__LIFECYCLE, newLifecycle, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLifecycle(LifecycleType newLifecycle) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__LIFECYCLE, newLifecycle);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public LifecycleFactoryType getLifecycleFactory() {
 		return (LifecycleFactoryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__LIFECYCLE_FACTORY, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newLifecycleFactory 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetLifecycleFactory(LifecycleFactoryType newLifecycleFactory, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__LIFECYCLE_FACTORY, newLifecycleFactory, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLifecycleFactory(LifecycleFactoryType newLifecycleFactory) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__LIFECYCLE_FACTORY, newLifecycleFactory);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ListEntriesType getListEntries() {
 		return (ListEntriesType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__LIST_ENTRIES, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newListEntries 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetListEntries(ListEntriesType newListEntries, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__LIST_ENTRIES, newListEntries, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setListEntries(ListEntriesType newListEntries) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__LIST_ENTRIES, newListEntries);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public LocaleConfigType getLocaleConfig() {
 		return (LocaleConfigType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__LOCALE_CONFIG, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newLocaleConfig 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetLocaleConfig(LocaleConfigType newLocaleConfig, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__LOCALE_CONFIG, newLocaleConfig, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLocaleConfig(LocaleConfigType newLocaleConfig) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__LOCALE_CONFIG, newLocaleConfig);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ManagedBeanType getManagedBean() {
 		return (ManagedBeanType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newManagedBean 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetManagedBean(ManagedBeanType newManagedBean, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN, newManagedBean, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setManagedBean(ManagedBeanType newManagedBean) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN, newManagedBean);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ManagedBeanClassType getManagedBeanClass() {
 		return (ManagedBeanClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newManagedBeanClass 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetManagedBeanClass(ManagedBeanClassType newManagedBeanClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN_CLASS, newManagedBeanClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setManagedBeanClass(ManagedBeanClassType newManagedBeanClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN_CLASS, newManagedBeanClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ManagedBeanNameType getManagedBeanName() {
 		return (ManagedBeanNameType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN_NAME, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newManagedBeanName 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetManagedBeanName(ManagedBeanNameType newManagedBeanName, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN_NAME, newManagedBeanName, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setManagedBeanName(ManagedBeanNameType newManagedBeanName) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN_NAME, newManagedBeanName);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ManagedBeanScopeType getManagedBeanScope() {
 		return (ManagedBeanScopeType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN_SCOPE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newManagedBeanScope 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetManagedBeanScope(ManagedBeanScopeType newManagedBeanScope, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN_SCOPE, newManagedBeanScope, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setManagedBeanScope(ManagedBeanScopeType newManagedBeanScope) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_BEAN_SCOPE, newManagedBeanScope);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ManagedPropertyType getManagedProperty() {
 		return (ManagedPropertyType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_PROPERTY, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newManagedProperty 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetManagedProperty(ManagedPropertyType newManagedProperty, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_PROPERTY, newManagedProperty, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setManagedProperty(ManagedPropertyType newManagedProperty) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__MANAGED_PROPERTY, newManagedProperty);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public MapEntriesType getMapEntries() {
 		return (MapEntriesType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__MAP_ENTRIES, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newMapEntries 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetMapEntries(MapEntriesType newMapEntries, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__MAP_ENTRIES, newMapEntries, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setMapEntries(MapEntriesType newMapEntries) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__MAP_ENTRIES, newMapEntries);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public MapEntryType getMapEntry() {
 		return (MapEntryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__MAP_ENTRY, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newMapEntry 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetMapEntry(MapEntryType newMapEntry, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__MAP_ENTRY, newMapEntry, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setMapEntry(MapEntryType newMapEntry) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__MAP_ENTRY, newMapEntry);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public MessageBundleType getMessageBundle() {
 		return (MessageBundleType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__MESSAGE_BUNDLE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newMessageBundle 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetMessageBundle(MessageBundleType newMessageBundle, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__MESSAGE_BUNDLE, newMessageBundle, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setMessageBundle(MessageBundleType newMessageBundle) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__MESSAGE_BUNDLE, newMessageBundle);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NameType getName() {
 		return (NameType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAME, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newName 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetName(NameType newName, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAME, newName, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setName(NameType newName) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAME, newName);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NavigationCaseType getNavigationCase() {
 		return (NavigationCaseType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAVIGATION_CASE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newNavigationCase 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetNavigationCase(NavigationCaseType newNavigationCase, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAVIGATION_CASE, newNavigationCase, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setNavigationCase(NavigationCaseType newNavigationCase) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAVIGATION_CASE, newNavigationCase);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NavigationHandlerType getNavigationHandler() {
 		return (NavigationHandlerType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAVIGATION_HANDLER, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newNavigationHandler 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetNavigationHandler(NavigationHandlerType newNavigationHandler, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAVIGATION_HANDLER, newNavigationHandler, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setNavigationHandler(NavigationHandlerType newNavigationHandler) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAVIGATION_HANDLER, newNavigationHandler);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NavigationRuleType getNavigationRule() {
 		return (NavigationRuleType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAVIGATION_RULE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newNavigationRule 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetNavigationRule(NavigationRuleType newNavigationRule, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAVIGATION_RULE, newNavigationRule, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setNavigationRule(NavigationRuleType newNavigationRule) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__NAVIGATION_RULE, newNavigationRule);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NullValueType getNullValue() {
 		return (NullValueType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__NULL_VALUE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newNullValue 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetNullValue(NullValueType newNullValue, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__NULL_VALUE, newNullValue, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setNullValue(NullValueType newNullValue) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__NULL_VALUE, newNullValue);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public OrderingType getOrdering() {
 		return (OrderingType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__ORDERING, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newOrdering 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOrdering(OrderingType newOrdering, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__ORDERING, newOrdering, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOrdering(OrderingType newOrdering) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__ORDERING, newOrdering);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public OrderingOrderingType getOrderingOrdering() {
 		return (OrderingOrderingType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__ORDERING_ORDERING, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newOrderingOrdering 
 	 * @param msgs 
 	 * @return NotificationChain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOrderingOrdering(OrderingOrderingType newOrderingOrdering, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__ORDERING_ORDERING, newOrderingOrdering, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOrderingOrdering(OrderingOrderingType newOrderingOrdering) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__ORDERING_ORDERING, newOrderingOrdering);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public OrderingOthersType getOthers() {
 		return (OrderingOthersType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__OTHERS, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newOthers 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOthers(OrderingOthersType newOthers, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__OTHERS, newOthers, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOthers(OrderingOthersType newOthers) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__OTHERS, newOthers);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PartialViewContextFactoryType getPartialViewContextFactory() {
 		return (PartialViewContextFactoryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__PARTIAL_VIEW_CONTEXT_FACTORY, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newPartialViewContextFactory 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetPartialViewContextFactory(PartialViewContextFactoryType newPartialViewContextFactory, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__PARTIAL_VIEW_CONTEXT_FACTORY, newPartialViewContextFactory, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPartialViewContextFactory(PartialViewContextFactoryType newPartialViewContextFactory) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__PARTIAL_VIEW_CONTEXT_FACTORY, newPartialViewContextFactory);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PhaseListenerType getPhaseListener() {
 		return (PhaseListenerType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__PHASE_LISTENER, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newPhaseListener 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetPhaseListener(PhaseListenerType newPhaseListener, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__PHASE_LISTENER, newPhaseListener, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPhaseListener(PhaseListenerType newPhaseListener) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__PHASE_LISTENER, newPhaseListener);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PropertyType getProperty() {
 		return (PropertyType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newProperty 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetProperty(PropertyType newProperty, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY, newProperty, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setProperty(PropertyType newProperty) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY, newProperty);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PropertyClassType getPropertyClass() {
 		return (PropertyClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newPropertyClass 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetPropertyClass(PropertyClassType newPropertyClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_CLASS, newPropertyClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPropertyClass(PropertyClassType newPropertyClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_CLASS, newPropertyClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PropertyExtensionType getPropertyExtension() {
 		return (PropertyExtensionType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_EXTENSION, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newPropertyExtension 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetPropertyExtension(PropertyExtensionType newPropertyExtension, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_EXTENSION, newPropertyExtension, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPropertyExtension(PropertyExtensionType newPropertyExtension) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_EXTENSION, newPropertyExtension);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PropertyNameType getPropertyName() {
 		return (PropertyNameType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_NAME, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newPropertyName 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetPropertyName(PropertyNameType newPropertyName, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_NAME, newPropertyName, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPropertyName(PropertyNameType newPropertyName) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_NAME, newPropertyName);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PropertyResolverType getPropertyResolver() {
 		return (PropertyResolverType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_RESOLVER, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newPropertyResolver 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetPropertyResolver(PropertyResolverType newPropertyResolver, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_RESOLVER, newPropertyResolver, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPropertyResolver(PropertyResolverType newPropertyResolver) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__PROPERTY_RESOLVER, newPropertyResolver);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RedirectType getRedirect() {
 		return (RedirectType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__REDIRECT, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newRedirect 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetRedirect(RedirectType newRedirect, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__REDIRECT, newRedirect, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRedirect(RedirectType newRedirect) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__REDIRECT, newRedirect);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RedirectViewParamType getRedirectViewParam() {
 		return (RedirectViewParamType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__REDIRECT_VIEW_PARAM, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newRedirectViewParam 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetRedirectViewParam(RedirectViewParamType newRedirectViewParam, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__REDIRECT_VIEW_PARAM, newRedirectViewParam, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRedirectViewParam(RedirectViewParamType newRedirectViewParam) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__REDIRECT_VIEW_PARAM, newRedirectViewParam);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ReferencedBeanType getReferencedBean() {
 		return (ReferencedBeanType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__REFERENCED_BEAN, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newReferencedBean 
      * @param msgs 
      * @return the notification chai 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetReferencedBean(ReferencedBeanType newReferencedBean, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__REFERENCED_BEAN, newReferencedBean, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setReferencedBean(ReferencedBeanType newReferencedBean) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__REFERENCED_BEAN, newReferencedBean);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ReferencedBeanClassType getReferencedBeanClass() {
 		return (ReferencedBeanClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__REFERENCED_BEAN_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newReferencedBeanClass 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetReferencedBeanClass(ReferencedBeanClassType newReferencedBeanClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__REFERENCED_BEAN_CLASS, newReferencedBeanClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setReferencedBeanClass(ReferencedBeanClassType newReferencedBeanClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__REFERENCED_BEAN_CLASS, newReferencedBeanClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ReferencedBeanNameType getReferencedBeanName() {
 		return (ReferencedBeanNameType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__REFERENCED_BEAN_NAME, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newReferencedBeanName 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetReferencedBeanName(ReferencedBeanNameType newReferencedBeanName, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__REFERENCED_BEAN_NAME, newReferencedBeanName, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setReferencedBeanName(ReferencedBeanNameType newReferencedBeanName) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__REFERENCED_BEAN_NAME, newReferencedBeanName);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RendererType getRenderer() {
 		return (RendererType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newRenderer 
      * @param msgs 
      * @return the notification chain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetRenderer(RendererType newRenderer, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER, newRenderer, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRenderer(RendererType newRenderer) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER, newRenderer);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RendererClassType getRendererClass() {
 		return (RendererClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newRendererClass 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetRendererClass(RendererClassType newRendererClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER_CLASS, newRendererClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRendererClass(RendererClassType newRendererClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER_CLASS, newRendererClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RendererExtensionType getRendererExtension() {
 		return (RendererExtensionType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER_EXTENSION, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newRendererExtension 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetRendererExtension(RendererExtensionType newRendererExtension, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER_EXTENSION, newRendererExtension, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRendererExtension(RendererExtensionType newRendererExtension) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER_EXTENSION, newRendererExtension);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RendererTypeType getRendererType() {
 		return (RendererTypeType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER_TYPE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newRendererType 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetRendererType(RendererTypeType newRendererType, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER_TYPE, newRendererType, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRendererType(RendererTypeType newRendererType) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDERER_TYPE, newRendererType);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RenderKitType getRenderKit() {
 		return (RenderKitType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newRenderKit 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetRenderKit(RenderKitType newRenderKit, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT, newRenderKit, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRenderKit(RenderKitType newRenderKit) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT, newRenderKit);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RenderKitClassType getRenderKitClass() {
 		return (RenderKitClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newRenderKitClass 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetRenderKitClass(RenderKitClassType newRenderKitClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT_CLASS, newRenderKitClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRenderKitClass(RenderKitClassType newRenderKitClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT_CLASS, newRenderKitClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RenderKitFactoryType getRenderKitFactory() {
 		return (RenderKitFactoryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT_FACTORY, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newRenderKitFactory 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetRenderKitFactory(RenderKitFactoryType newRenderKitFactory, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT_FACTORY, newRenderKitFactory, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRenderKitFactory(RenderKitFactoryType newRenderKitFactory) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT_FACTORY, newRenderKitFactory);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public RenderKitIdType getRenderKitId() {
 		return (RenderKitIdType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT_ID, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newRenderKitId 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetRenderKitId(RenderKitIdType newRenderKitId, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT_ID, newRenderKitId, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRenderKitId(RenderKitIdType newRenderKitId) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__RENDER_KIT_ID, newRenderKitId);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ResourceHandlerType getResourceHandler() {
 		return (ResourceHandlerType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__RESOURCE_HANDLER, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newResourceHandler 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetResourceHandler(ResourceHandlerType newResourceHandler, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__RESOURCE_HANDLER, newResourceHandler, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setResourceHandler(ResourceHandlerType newResourceHandler) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__RESOURCE_HANDLER, newResourceHandler);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SmallIconType getSmallIcon() {
 		return (SmallIconType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__SMALL_ICON, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newSmallIcon 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetSmallIcon(SmallIconType newSmallIcon, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__SMALL_ICON, newSmallIcon, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSmallIcon(SmallIconType newSmallIcon) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__SMALL_ICON, newSmallIcon);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SourceClassType getSourceClass() {
 		return (SourceClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__SOURCE_CLASS, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newSourceClass 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetSourceClass(SourceClassType newSourceClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__SOURCE_CLASS, newSourceClass, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSourceClass(SourceClassType newSourceClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__SOURCE_CLASS, newSourceClass);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public StateManagerType getStateManager() {
 		return (StateManagerType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__STATE_MANAGER, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newStateManager 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetStateManager(StateManagerType newStateManager, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__STATE_MANAGER, newStateManager, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setStateManager(StateManagerType newStateManager) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__STATE_MANAGER, newStateManager);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SuggestedValueType getSuggestedValue() {
 		return (SuggestedValueType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__SUGGESTED_VALUE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newSuggestedValue 
      * @param msgs 
      * @return the notification chain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetSuggestedValue(SuggestedValueType newSuggestedValue, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__SUGGESTED_VALUE, newSuggestedValue, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSuggestedValue(SuggestedValueType newSuggestedValue) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__SUGGESTED_VALUE, newSuggestedValue);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SupportedLocaleType getSupportedLocale() {
 		return (SupportedLocaleType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__SUPPORTED_LOCALE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newSupportedLocale 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetSupportedLocale(SupportedLocaleType newSupportedLocale, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__SUPPORTED_LOCALE, newSupportedLocale, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSupportedLocale(SupportedLocaleType newSupportedLocale) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__SUPPORTED_LOCALE, newSupportedLocale);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SystemEventClassType getSystemEventClass() {
 		return (SystemEventClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__SYSTEM_EVENT_CLASS, true);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * @param newSystemEventClass 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetSystemEventClass(SystemEventClassType newSystemEventClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__SYSTEM_EVENT_CLASS, newSystemEventClass, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSystemEventClass(SystemEventClassType newSystemEventClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__SYSTEM_EVENT_CLASS, newSystemEventClass);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SystemEventListenerType getSystemEventListener() {
 		return (SystemEventListenerType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newSystemEventListener 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetSystemEventListener(SystemEventListenerType newSystemEventListener, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER, newSystemEventListener, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSystemEventListener(SystemEventListenerType newSystemEventListener) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER, newSystemEventListener);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SystemEventListenerClassType getSystemEventListenerClass() {
 		return (SystemEventListenerClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER_CLASS, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newSystemEventListenerClass 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetSystemEventListenerClass(SystemEventListenerClassType newSystemEventListenerClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER_CLASS, newSystemEventListenerClass, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSystemEventListenerClass(SystemEventListenerClassType newSystemEventListenerClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER_CLASS, newSystemEventListenerClass);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public TagHandlerDelegateFactoryType getTagHandlerDelegateFactory() {
 		return (TagHandlerDelegateFactoryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__TAG_HANDLER_DELEGATE_FACTORY, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newTagHandlerDelegateFactory 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetTagHandlerDelegateFactory(TagHandlerDelegateFactoryType newTagHandlerDelegateFactory, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__TAG_HANDLER_DELEGATE_FACTORY, newTagHandlerDelegateFactory, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setTagHandlerDelegateFactory(TagHandlerDelegateFactoryType newTagHandlerDelegateFactory) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__TAG_HANDLER_DELEGATE_FACTORY, newTagHandlerDelegateFactory);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ToViewIdType getToViewId() {
 		return (ToViewIdType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__TO_VIEW_ID, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newToViewId 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetToViewId(ToViewIdType newToViewId, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__TO_VIEW_ID, newToViewId, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setToViewId(ToViewIdType newToViewId) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__TO_VIEW_ID, newToViewId);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ValidatorType getValidator() {
 		return (ValidatorType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALIDATOR, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newValidator 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetValidator(ValidatorType newValidator, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALIDATOR, newValidator, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValidator(ValidatorType newValidator) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALIDATOR, newValidator);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ValidatorClassType getValidatorClass() {
 		return (ValidatorClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALIDATOR_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newValidatorClass 
      * @param msgs 
      * @return NotificationChain
      * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetValidatorClass(ValidatorClassType newValidatorClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALIDATOR_CLASS, newValidatorClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValidatorClass(ValidatorClassType newValidatorClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALIDATOR_CLASS, newValidatorClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ValidatorIdType getValidatorId() {
 		return (ValidatorIdType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALIDATOR_ID, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newValidatorId 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetValidatorId(ValidatorIdType newValidatorId, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALIDATOR_ID, newValidatorId, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValidatorId(ValidatorIdType newValidatorId) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALIDATOR_ID, newValidatorId);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ValueType getValue() {
 		return (ValueType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALUE, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newValue 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetValue(ValueType newValue, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALUE, newValue, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValue(ValueType newValue) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALUE, newValue);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ValueClassType getValueClass() {
 		return (ValueClassType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALUE_CLASS, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newValueClass 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetValueClass(ValueClassType newValueClass, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALUE_CLASS, newValueClass, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValueClass(ValueClassType newValueClass) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__VALUE_CLASS, newValueClass);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public VariableResolverType getVariableResolver() {
 		return (VariableResolverType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__VARIABLE_RESOLVER, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newVariableResolver 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetVariableResolver(VariableResolverType newVariableResolver, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__VARIABLE_RESOLVER, newVariableResolver, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setVariableResolver(VariableResolverType newVariableResolver) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__VARIABLE_RESOLVER, newVariableResolver);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ViewDeclarationLanguageFactoryType getViewDeclarationLanguageFactory() {
 		return (ViewDeclarationLanguageFactoryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__VIEW_DECLARATION_LANGUAGE_FACTORY, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newViewDeclarationLanguageFactory 
 	 * @param msgs 
 	 * @return NotificationChain
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetViewDeclarationLanguageFactory(ViewDeclarationLanguageFactoryType newViewDeclarationLanguageFactory, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__VIEW_DECLARATION_LANGUAGE_FACTORY, newViewDeclarationLanguageFactory, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setViewDeclarationLanguageFactory(ViewDeclarationLanguageFactoryType newViewDeclarationLanguageFactory) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__VIEW_DECLARATION_LANGUAGE_FACTORY, newViewDeclarationLanguageFactory);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ViewHandlerType getViewHandler() {
 		return (ViewHandlerType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__VIEW_HANDLER, true);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
      * @param newViewHandler 
      * @param msgs 
      * @return the notification chain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetViewHandler(ViewHandlerType newViewHandler, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__VIEW_HANDLER, newViewHandler, msgs);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setViewHandler(ViewHandlerType newViewHandler) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__VIEW_HANDLER, newViewHandler);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public VisitContextFactoryType getVisitContextFactory() {
 		return (VisitContextFactoryType)getMixed().get(FacesConfigPackage.Literals.DOCUMENT_ROOT__VISIT_CONTEXT_FACTORY, true);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * @param newVisitContextFactory 
 	 * @param msgs 
 	 * @return NotificationChain 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetVisitContextFactory(VisitContextFactoryType newVisitContextFactory, NotificationChain msgs) {
 		return ((FeatureMap.Internal)getMixed()).basicAdd(FacesConfigPackage.Literals.DOCUMENT_ROOT__VISIT_CONTEXT_FACTORY, newVisitContextFactory, msgs);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setVisitContextFactory(VisitContextFactoryType newVisitContextFactory) {
 		((FeatureMap.Internal)getMixed()).set(FacesConfigPackage.Literals.DOCUMENT_ROOT__VISIT_CONTEXT_FACTORY, newVisitContextFactory);
 	}
 
 				/**
 	 * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
 	 * @generated
 	 */
     public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case FacesConfigPackage.DOCUMENT_ROOT__MIXED:
 				return ((InternalEList)getMixed()).basicRemove(otherEnd, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP:
 				return ((InternalEList)getXMLNSPrefixMap()).basicRemove(otherEnd, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION:
 				return ((InternalEList)getXSISchemaLocation()).basicRemove(otherEnd, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__ABSOLUTE_ORDERING:
 				return basicSetAbsoluteOrdering(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__ACTION_LISTENER:
 				return basicSetActionListener(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__APPLICATION:
 				return basicSetApplication(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__APPLICATION_FACTORY:
 				return basicSetApplicationFactory(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE:
 				return basicSetAttribute(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_CLASS:
 				return basicSetAttributeClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_EXTENSION:
 				return basicSetAttributeExtension(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_NAME:
 				return basicSetAttributeName(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR:
 				return basicSetBehavior(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_CLASS:
 				return basicSetBehaviorClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_ID:
 				return basicSetBehaviorId(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_EXTENSION:
 				return basicSetBehaviorExtension(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT:
 				return basicSetComponent(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_CLASS:
 				return basicSetComponentClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_EXTENSION:
 				return basicSetComponentExtension(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_FAMILY:
 				return basicSetComponentFamily(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_TYPE:
 				return basicSetComponentType(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER:
 				return basicSetConverter(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_CLASS:
 				return basicSetConverterClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_FOR_CLASS:
 				return basicSetConverterForClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_ID:
 				return basicSetConverterId(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_LOCALE:
 				return basicSetDefaultLocale(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_RENDER_KIT_ID:
 				return basicSetDefaultRenderKitId(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_VALIDATORS:
 				return basicSetDefaultValidators(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_VALUE:
 				return basicSetDefaultValue(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__DESCRIPTION:
 				return basicSetDescription(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__DISPLAY_NAME:
 				return basicSetDisplayName(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__EXCEPTION_HANDLER_FACTORY:
 				return basicSetExceptionHandlerFactory(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__EXTERNAL_CONTEXT_FACTORY:
 				return basicSetExternalContextFactory(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__FACES_CONFIG:
 				return basicSetFacesConfig(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__FACES_CONTEXT_FACTORY:
 				return basicSetFacesContextFactory(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET:
 				return basicSetFacet(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET_EXTENSION:
 				return basicSetFacetExtension(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET_NAME:
 				return basicSetFacetName(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__FACTORY:
 				return basicSetFactory(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_ACTION:
 				return basicSetFromAction(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_OUTCOME:
 				return basicSetFromOutcome(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_VIEW_ID:
 				return basicSetFromViewId(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__ICON:
 				return basicSetIcon(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__IF:
 				return basicSetIf(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__KEY:
 				return basicSetKey(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__KEY_CLASS:
 				return basicSetKeyClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__LARGE_ICON:
 				return basicSetLargeIcon(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__LIFECYCLE:
 				return basicSetLifecycle(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__LIFECYCLE_FACTORY:
 				return basicSetLifecycleFactory(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__LIST_ENTRIES:
 				return basicSetListEntries(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__LOCALE_CONFIG:
 				return basicSetLocaleConfig(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN:
 				return basicSetManagedBean(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_CLASS:
 				return basicSetManagedBeanClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_NAME:
 				return basicSetManagedBeanName(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_SCOPE:
 				return basicSetManagedBeanScope(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_PROPERTY:
 				return basicSetManagedProperty(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__MAP_ENTRIES:
 				return basicSetMapEntries(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__MAP_ENTRY:
 				return basicSetMapEntry(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__MESSAGE_BUNDLE:
 				return basicSetMessageBundle(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__NAME:
 				return basicSetName(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_CASE:
 				return basicSetNavigationCase(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_HANDLER:
 				return basicSetNavigationHandler(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_RULE:
 				return basicSetNavigationRule(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__NULL_VALUE:
 				return basicSetNullValue(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__ORDERING:
 				return basicSetOrdering(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__ORDERING_ORDERING:
 				return basicSetOrderingOrdering(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__OTHERS:
 				return basicSetOthers(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__PARTIAL_VIEW_CONTEXT_FACTORY:
 				return basicSetPartialViewContextFactory(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__PHASE_LISTENER:
 				return basicSetPhaseListener(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY:
 				return basicSetProperty(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_CLASS:
 				return basicSetPropertyClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_EXTENSION:
 				return basicSetPropertyExtension(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_NAME:
 				return basicSetPropertyName(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_RESOLVER:
 				return basicSetPropertyResolver(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__REDIRECT:
 				return basicSetRedirect(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__REDIRECT_VIEW_PARAM:
 				return basicSetRedirectViewParam(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN:
 				return basicSetReferencedBean(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN_CLASS:
 				return basicSetReferencedBeanClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN_NAME:
 				return basicSetReferencedBeanName(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER:
 				return basicSetRenderer(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_CLASS:
 				return basicSetRendererClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_EXTENSION:
 				return basicSetRendererExtension(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_TYPE:
 				return basicSetRendererType(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT:
 				return basicSetRenderKit(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_CLASS:
 				return basicSetRenderKitClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_FACTORY:
 				return basicSetRenderKitFactory(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_ID:
 				return basicSetRenderKitId(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__RESOURCE_HANDLER:
 				return basicSetResourceHandler(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__SMALL_ICON:
 				return basicSetSmallIcon(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__SOURCE_CLASS:
 				return basicSetSourceClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__STATE_MANAGER:
 				return basicSetStateManager(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__SUGGESTED_VALUE:
 				return basicSetSuggestedValue(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__SUPPORTED_LOCALE:
 				return basicSetSupportedLocale(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_CLASS:
 				return basicSetSystemEventClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER:
 				return basicSetSystemEventListener(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER_CLASS:
 				return basicSetSystemEventListenerClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__TAG_HANDLER_DELEGATE_FACTORY:
 				return basicSetTagHandlerDelegateFactory(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__TO_VIEW_ID:
 				return basicSetToViewId(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR:
 				return basicSetValidator(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR_CLASS:
 				return basicSetValidatorClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR_ID:
 				return basicSetValidatorId(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__VALUE:
 				return basicSetValue(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__VALUE_CLASS:
 				return basicSetValueClass(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__VARIABLE_RESOLVER:
 				return basicSetVariableResolver(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__VIEW_DECLARATION_LANGUAGE_FACTORY:
 				return basicSetViewDeclarationLanguageFactory(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__VIEW_HANDLER:
 				return basicSetViewHandler(null, msgs);
 			case FacesConfigPackage.DOCUMENT_ROOT__VISIT_CONTEXT_FACTORY:
 				return basicSetVisitContextFactory(null, msgs);
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
 			case FacesConfigPackage.DOCUMENT_ROOT__MIXED:
 				if (coreType) return getMixed();
 				return ((FeatureMap.Internal)getMixed()).getWrapper();
 			case FacesConfigPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP:
 				if (coreType) return getXMLNSPrefixMap();
				else return getXMLNSPrefixMap().map();
 			case FacesConfigPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION:
 				if (coreType) return getXSISchemaLocation();
				else return getXSISchemaLocation().map();
 			case FacesConfigPackage.DOCUMENT_ROOT__ABSOLUTE_ORDERING:
 				return getAbsoluteOrdering();
 			case FacesConfigPackage.DOCUMENT_ROOT__ACTION_LISTENER:
 				return getActionListener();
 			case FacesConfigPackage.DOCUMENT_ROOT__APPLICATION:
 				return getApplication();
 			case FacesConfigPackage.DOCUMENT_ROOT__APPLICATION_FACTORY:
 				return getApplicationFactory();
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE:
 				return getAttribute();
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_CLASS:
 				return getAttributeClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_EXTENSION:
 				return getAttributeExtension();
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_NAME:
 				return getAttributeName();
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR:
 				return getBehavior();
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_CLASS:
 				return getBehaviorClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_ID:
 				return getBehaviorId();
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_EXTENSION:
 				return getBehaviorExtension();
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT:
 				return getComponent();
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_CLASS:
 				return getComponentClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_EXTENSION:
 				return getComponentExtension();
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_FAMILY:
 				return getComponentFamily();
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_TYPE:
 				return getComponentType();
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER:
 				return getConverter();
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_CLASS:
 				return getConverterClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_FOR_CLASS:
 				return getConverterForClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_ID:
 				return getConverterId();
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_LOCALE:
 				return getDefaultLocale();
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_RENDER_KIT_ID:
 				return getDefaultRenderKitId();
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_VALIDATORS:
 				return getDefaultValidators();
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_VALUE:
 				return getDefaultValue();
 			case FacesConfigPackage.DOCUMENT_ROOT__DESCRIPTION:
 				return getDescription();
 			case FacesConfigPackage.DOCUMENT_ROOT__DISPLAY_NAME:
 				return getDisplayName();
 			case FacesConfigPackage.DOCUMENT_ROOT__EXCEPTION_HANDLER_FACTORY:
 				return getExceptionHandlerFactory();
 			case FacesConfigPackage.DOCUMENT_ROOT__EXTERNAL_CONTEXT_FACTORY:
 				return getExternalContextFactory();
 			case FacesConfigPackage.DOCUMENT_ROOT__FACES_CONFIG:
 				return getFacesConfig();
 			case FacesConfigPackage.DOCUMENT_ROOT__FACES_CONTEXT_FACTORY:
 				return getFacesContextFactory();
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET:
 				return getFacet();
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET_EXTENSION:
 				return getFacetExtension();
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET_NAME:
 				return getFacetName();
 			case FacesConfigPackage.DOCUMENT_ROOT__FACTORY:
 				return getFactory();
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_ACTION:
 				return getFromAction();
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_OUTCOME:
 				return getFromOutcome();
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_VIEW_ID:
 				return getFromViewId();
 			case FacesConfigPackage.DOCUMENT_ROOT__ICON:
 				return getIcon();
 			case FacesConfigPackage.DOCUMENT_ROOT__IF:
 				return getIf();
 			case FacesConfigPackage.DOCUMENT_ROOT__KEY:
 				return getKey();
 			case FacesConfigPackage.DOCUMENT_ROOT__KEY_CLASS:
 				return getKeyClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__LARGE_ICON:
 				return getLargeIcon();
 			case FacesConfigPackage.DOCUMENT_ROOT__LIFECYCLE:
 				return getLifecycle();
 			case FacesConfigPackage.DOCUMENT_ROOT__LIFECYCLE_FACTORY:
 				return getLifecycleFactory();
 			case FacesConfigPackage.DOCUMENT_ROOT__LIST_ENTRIES:
 				return getListEntries();
 			case FacesConfigPackage.DOCUMENT_ROOT__LOCALE_CONFIG:
 				return getLocaleConfig();
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN:
 				return getManagedBean();
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_CLASS:
 				return getManagedBeanClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_NAME:
 				return getManagedBeanName();
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_SCOPE:
 				return getManagedBeanScope();
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_PROPERTY:
 				return getManagedProperty();
 			case FacesConfigPackage.DOCUMENT_ROOT__MAP_ENTRIES:
 				return getMapEntries();
 			case FacesConfigPackage.DOCUMENT_ROOT__MAP_ENTRY:
 				return getMapEntry();
 			case FacesConfigPackage.DOCUMENT_ROOT__MESSAGE_BUNDLE:
 				return getMessageBundle();
 			case FacesConfigPackage.DOCUMENT_ROOT__NAME:
 				return getName();
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_CASE:
 				return getNavigationCase();
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_HANDLER:
 				return getNavigationHandler();
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_RULE:
 				return getNavigationRule();
 			case FacesConfigPackage.DOCUMENT_ROOT__NULL_VALUE:
 				return getNullValue();
 			case FacesConfigPackage.DOCUMENT_ROOT__ORDERING:
 				return getOrdering();
 			case FacesConfigPackage.DOCUMENT_ROOT__ORDERING_ORDERING:
 				return getOrderingOrdering();
 			case FacesConfigPackage.DOCUMENT_ROOT__OTHERS:
 				return getOthers();
 			case FacesConfigPackage.DOCUMENT_ROOT__PARTIAL_VIEW_CONTEXT_FACTORY:
 				return getPartialViewContextFactory();
 			case FacesConfigPackage.DOCUMENT_ROOT__PHASE_LISTENER:
 				return getPhaseListener();
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY:
 				return getProperty();
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_CLASS:
 				return getPropertyClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_EXTENSION:
 				return getPropertyExtension();
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_NAME:
 				return getPropertyName();
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_RESOLVER:
 				return getPropertyResolver();
 			case FacesConfigPackage.DOCUMENT_ROOT__REDIRECT:
 				return getRedirect();
 			case FacesConfigPackage.DOCUMENT_ROOT__REDIRECT_VIEW_PARAM:
 				return getRedirectViewParam();
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN:
 				return getReferencedBean();
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN_CLASS:
 				return getReferencedBeanClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN_NAME:
 				return getReferencedBeanName();
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER:
 				return getRenderer();
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_CLASS:
 				return getRendererClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_EXTENSION:
 				return getRendererExtension();
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_TYPE:
 				return getRendererType();
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT:
 				return getRenderKit();
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_CLASS:
 				return getRenderKitClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_FACTORY:
 				return getRenderKitFactory();
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_ID:
 				return getRenderKitId();
 			case FacesConfigPackage.DOCUMENT_ROOT__RESOURCE_HANDLER:
 				return getResourceHandler();
 			case FacesConfigPackage.DOCUMENT_ROOT__SMALL_ICON:
 				return getSmallIcon();
 			case FacesConfigPackage.DOCUMENT_ROOT__SOURCE_CLASS:
 				return getSourceClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__STATE_MANAGER:
 				return getStateManager();
 			case FacesConfigPackage.DOCUMENT_ROOT__SUGGESTED_VALUE:
 				return getSuggestedValue();
 			case FacesConfigPackage.DOCUMENT_ROOT__SUPPORTED_LOCALE:
 				return getSupportedLocale();
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_CLASS:
 				return getSystemEventClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER:
 				return getSystemEventListener();
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER_CLASS:
 				return getSystemEventListenerClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__TAG_HANDLER_DELEGATE_FACTORY:
 				return getTagHandlerDelegateFactory();
 			case FacesConfigPackage.DOCUMENT_ROOT__TO_VIEW_ID:
 				return getToViewId();
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR:
 				return getValidator();
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR_CLASS:
 				return getValidatorClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR_ID:
 				return getValidatorId();
 			case FacesConfigPackage.DOCUMENT_ROOT__VALUE:
 				return getValue();
 			case FacesConfigPackage.DOCUMENT_ROOT__VALUE_CLASS:
 				return getValueClass();
 			case FacesConfigPackage.DOCUMENT_ROOT__VARIABLE_RESOLVER:
 				return getVariableResolver();
 			case FacesConfigPackage.DOCUMENT_ROOT__VIEW_DECLARATION_LANGUAGE_FACTORY:
 				return getViewDeclarationLanguageFactory();
 			case FacesConfigPackage.DOCUMENT_ROOT__VIEW_HANDLER:
 				return getViewHandler();
 			case FacesConfigPackage.DOCUMENT_ROOT__VISIT_CONTEXT_FACTORY:
 				return getVisitContextFactory();
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
 			case FacesConfigPackage.DOCUMENT_ROOT__MIXED:
 				((FeatureMap.Internal)getMixed()).set(newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP:
 				((EStructuralFeature.Setting)getXMLNSPrefixMap()).set(newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION:
 				((EStructuralFeature.Setting)getXSISchemaLocation()).set(newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ABSOLUTE_ORDERING:
 				setAbsoluteOrdering((AbsoluteOrderingType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ACTION_LISTENER:
 				setActionListener((ActionListenerType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__APPLICATION:
 				setApplication((ApplicationType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__APPLICATION_FACTORY:
 				setApplicationFactory((ApplicationFactoryType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE:
 				setAttribute((AttributeType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_CLASS:
 				setAttributeClass((AttributeClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_EXTENSION:
 				setAttributeExtension((AttributeExtensionType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_NAME:
 				setAttributeName((AttributeNameType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR:
 				setBehavior((BehaviorType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_CLASS:
 				setBehaviorClass((BehaviorClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_ID:
 				setBehaviorId((BehaviorIdType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_EXTENSION:
 				setBehaviorExtension((BehaviorExtensionType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT:
 				setComponent((ComponentType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_CLASS:
 				setComponentClass((ComponentClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_EXTENSION:
 				setComponentExtension((ComponentExtensionType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_FAMILY:
 				setComponentFamily((ComponentFamilyType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_TYPE:
 				setComponentType((ComponentTypeType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER:
 				setConverter((ConverterType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_CLASS:
 				setConverterClass((ConverterClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_FOR_CLASS:
 				setConverterForClass((ConverterForClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_ID:
 				setConverterId((ConverterIdType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_LOCALE:
 				setDefaultLocale((DefaultLocaleType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_RENDER_KIT_ID:
 				setDefaultRenderKitId((DefaultRenderKitIdType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_VALIDATORS:
 				setDefaultValidators((DefaultValidatorsType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_VALUE:
 				setDefaultValue((DefaultValueType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DESCRIPTION:
 				setDescription((DescriptionType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DISPLAY_NAME:
 				setDisplayName((DisplayNameType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__EXCEPTION_HANDLER_FACTORY:
 				setExceptionHandlerFactory((ExceptionHandlerFactoryType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__EXTERNAL_CONTEXT_FACTORY:
 				setExternalContextFactory((ExternalContextFactoryType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACES_CONFIG:
 				setFacesConfig((FacesConfigType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACES_CONTEXT_FACTORY:
 				setFacesContextFactory((FacesContextFactoryType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET:
 				setFacet((FacetType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET_EXTENSION:
 				setFacetExtension((FacetExtensionType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET_NAME:
 				setFacetName((FacetNameType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACTORY:
 				setFactory((FactoryType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_ACTION:
 				setFromAction((FromActionType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_OUTCOME:
 				setFromOutcome((FromOutcomeType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_VIEW_ID:
 				setFromViewId((FromViewIdType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ICON:
 				setIcon((IconType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__IF:
 				setIf((IfType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__KEY:
 				setKey((KeyType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__KEY_CLASS:
 				setKeyClass((KeyClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__LARGE_ICON:
 				setLargeIcon((LargeIconType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__LIFECYCLE:
 				setLifecycle((LifecycleType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__LIFECYCLE_FACTORY:
 				setLifecycleFactory((LifecycleFactoryType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__LIST_ENTRIES:
 				setListEntries((ListEntriesType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__LOCALE_CONFIG:
 				setLocaleConfig((LocaleConfigType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN:
 				setManagedBean((ManagedBeanType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_CLASS:
 				setManagedBeanClass((ManagedBeanClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_NAME:
 				setManagedBeanName((ManagedBeanNameType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_SCOPE:
 				setManagedBeanScope((ManagedBeanScopeType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_PROPERTY:
 				setManagedProperty((ManagedPropertyType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MAP_ENTRIES:
 				setMapEntries((MapEntriesType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MAP_ENTRY:
 				setMapEntry((MapEntryType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MESSAGE_BUNDLE:
 				setMessageBundle((MessageBundleType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAME:
 				setName((NameType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_CASE:
 				setNavigationCase((NavigationCaseType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_HANDLER:
 				setNavigationHandler((NavigationHandlerType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_RULE:
 				setNavigationRule((NavigationRuleType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__NULL_VALUE:
 				setNullValue((NullValueType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ORDERING:
 				setOrdering((OrderingType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ORDERING_ORDERING:
 				setOrderingOrdering((OrderingOrderingType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__OTHERS:
 				setOthers((OrderingOthersType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PARTIAL_VIEW_CONTEXT_FACTORY:
 				setPartialViewContextFactory((PartialViewContextFactoryType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PHASE_LISTENER:
 				setPhaseListener((PhaseListenerType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY:
 				setProperty((PropertyType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_CLASS:
 				setPropertyClass((PropertyClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_EXTENSION:
 				setPropertyExtension((PropertyExtensionType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_NAME:
 				setPropertyName((PropertyNameType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_RESOLVER:
 				setPropertyResolver((PropertyResolverType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__REDIRECT:
 				setRedirect((RedirectType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__REDIRECT_VIEW_PARAM:
 				setRedirectViewParam((RedirectViewParamType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN:
 				setReferencedBean((ReferencedBeanType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN_CLASS:
 				setReferencedBeanClass((ReferencedBeanClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN_NAME:
 				setReferencedBeanName((ReferencedBeanNameType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER:
 				setRenderer((RendererType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_CLASS:
 				setRendererClass((RendererClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_EXTENSION:
 				setRendererExtension((RendererExtensionType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_TYPE:
 				setRendererType((RendererTypeType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT:
 				setRenderKit((RenderKitType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_CLASS:
 				setRenderKitClass((RenderKitClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_FACTORY:
 				setRenderKitFactory((RenderKitFactoryType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_ID:
 				setRenderKitId((RenderKitIdType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RESOURCE_HANDLER:
 				setResourceHandler((ResourceHandlerType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SMALL_ICON:
 				setSmallIcon((SmallIconType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SOURCE_CLASS:
 				setSourceClass((SourceClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__STATE_MANAGER:
 				setStateManager((StateManagerType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SUGGESTED_VALUE:
 				setSuggestedValue((SuggestedValueType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SUPPORTED_LOCALE:
 				setSupportedLocale((SupportedLocaleType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_CLASS:
 				setSystemEventClass((SystemEventClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER:
 				setSystemEventListener((SystemEventListenerType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER_CLASS:
 				setSystemEventListenerClass((SystemEventListenerClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__TAG_HANDLER_DELEGATE_FACTORY:
 				setTagHandlerDelegateFactory((TagHandlerDelegateFactoryType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__TO_VIEW_ID:
 				setToViewId((ToViewIdType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR:
 				setValidator((ValidatorType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR_CLASS:
 				setValidatorClass((ValidatorClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR_ID:
 				setValidatorId((ValidatorIdType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALUE:
 				setValue((ValueType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALUE_CLASS:
 				setValueClass((ValueClassType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VARIABLE_RESOLVER:
 				setVariableResolver((VariableResolverType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VIEW_DECLARATION_LANGUAGE_FACTORY:
 				setViewDeclarationLanguageFactory((ViewDeclarationLanguageFactoryType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VIEW_HANDLER:
 				setViewHandler((ViewHandlerType)newValue);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VISIT_CONTEXT_FACTORY:
 				setVisitContextFactory((VisitContextFactoryType)newValue);
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
 			case FacesConfigPackage.DOCUMENT_ROOT__MIXED:
 				getMixed().clear();
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP:
 				getXMLNSPrefixMap().clear();
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION:
 				getXSISchemaLocation().clear();
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ABSOLUTE_ORDERING:
 				setAbsoluteOrdering((AbsoluteOrderingType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ACTION_LISTENER:
 				setActionListener((ActionListenerType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__APPLICATION:
 				setApplication((ApplicationType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__APPLICATION_FACTORY:
 				setApplicationFactory((ApplicationFactoryType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE:
 				setAttribute((AttributeType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_CLASS:
 				setAttributeClass((AttributeClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_EXTENSION:
 				setAttributeExtension((AttributeExtensionType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_NAME:
 				setAttributeName((AttributeNameType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR:
 				setBehavior((BehaviorType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_CLASS:
 				setBehaviorClass((BehaviorClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_ID:
 				setBehaviorId((BehaviorIdType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_EXTENSION:
 				setBehaviorExtension((BehaviorExtensionType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT:
 				setComponent((ComponentType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_CLASS:
 				setComponentClass((ComponentClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_EXTENSION:
 				setComponentExtension((ComponentExtensionType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_FAMILY:
 				setComponentFamily((ComponentFamilyType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_TYPE:
 				setComponentType((ComponentTypeType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER:
 				setConverter((ConverterType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_CLASS:
 				setConverterClass((ConverterClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_FOR_CLASS:
 				setConverterForClass((ConverterForClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_ID:
 				setConverterId((ConverterIdType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_LOCALE:
 				setDefaultLocale((DefaultLocaleType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_RENDER_KIT_ID:
 				setDefaultRenderKitId((DefaultRenderKitIdType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_VALIDATORS:
 				setDefaultValidators((DefaultValidatorsType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_VALUE:
 				setDefaultValue((DefaultValueType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DESCRIPTION:
 				setDescription((DescriptionType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__DISPLAY_NAME:
 				setDisplayName((DisplayNameType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__EXCEPTION_HANDLER_FACTORY:
 				setExceptionHandlerFactory((ExceptionHandlerFactoryType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__EXTERNAL_CONTEXT_FACTORY:
 				setExternalContextFactory((ExternalContextFactoryType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACES_CONFIG:
 				setFacesConfig((FacesConfigType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACES_CONTEXT_FACTORY:
 				setFacesContextFactory((FacesContextFactoryType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET:
 				setFacet((FacetType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET_EXTENSION:
 				setFacetExtension((FacetExtensionType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET_NAME:
 				setFacetName((FacetNameType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACTORY:
 				setFactory((FactoryType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_ACTION:
 				setFromAction((FromActionType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_OUTCOME:
 				setFromOutcome((FromOutcomeType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_VIEW_ID:
 				setFromViewId((FromViewIdType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ICON:
 				setIcon((IconType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__IF:
 				setIf((IfType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__KEY:
 				setKey((KeyType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__KEY_CLASS:
 				setKeyClass((KeyClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__LARGE_ICON:
 				setLargeIcon((LargeIconType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__LIFECYCLE:
 				setLifecycle((LifecycleType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__LIFECYCLE_FACTORY:
 				setLifecycleFactory((LifecycleFactoryType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__LIST_ENTRIES:
 				setListEntries((ListEntriesType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__LOCALE_CONFIG:
 				setLocaleConfig((LocaleConfigType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN:
 				setManagedBean((ManagedBeanType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_CLASS:
 				setManagedBeanClass((ManagedBeanClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_NAME:
 				setManagedBeanName((ManagedBeanNameType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_SCOPE:
 				setManagedBeanScope((ManagedBeanScopeType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_PROPERTY:
 				setManagedProperty((ManagedPropertyType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MAP_ENTRIES:
 				setMapEntries((MapEntriesType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MAP_ENTRY:
 				setMapEntry((MapEntryType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__MESSAGE_BUNDLE:
 				setMessageBundle((MessageBundleType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAME:
 				setName((NameType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_CASE:
 				setNavigationCase((NavigationCaseType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_HANDLER:
 				setNavigationHandler((NavigationHandlerType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_RULE:
 				setNavigationRule((NavigationRuleType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__NULL_VALUE:
 				setNullValue((NullValueType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ORDERING:
 				setOrdering((OrderingType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__ORDERING_ORDERING:
 				setOrderingOrdering((OrderingOrderingType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__OTHERS:
 				setOthers((OrderingOthersType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PARTIAL_VIEW_CONTEXT_FACTORY:
 				setPartialViewContextFactory((PartialViewContextFactoryType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PHASE_LISTENER:
 				setPhaseListener((PhaseListenerType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY:
 				setProperty((PropertyType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_CLASS:
 				setPropertyClass((PropertyClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_EXTENSION:
 				setPropertyExtension((PropertyExtensionType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_NAME:
 				setPropertyName((PropertyNameType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_RESOLVER:
 				setPropertyResolver((PropertyResolverType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__REDIRECT:
 				setRedirect((RedirectType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__REDIRECT_VIEW_PARAM:
 				setRedirectViewParam((RedirectViewParamType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN:
 				setReferencedBean((ReferencedBeanType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN_CLASS:
 				setReferencedBeanClass((ReferencedBeanClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN_NAME:
 				setReferencedBeanName((ReferencedBeanNameType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER:
 				setRenderer((RendererType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_CLASS:
 				setRendererClass((RendererClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_EXTENSION:
 				setRendererExtension((RendererExtensionType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_TYPE:
 				setRendererType((RendererTypeType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT:
 				setRenderKit((RenderKitType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_CLASS:
 				setRenderKitClass((RenderKitClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_FACTORY:
 				setRenderKitFactory((RenderKitFactoryType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_ID:
 				setRenderKitId((RenderKitIdType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__RESOURCE_HANDLER:
 				setResourceHandler((ResourceHandlerType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SMALL_ICON:
 				setSmallIcon((SmallIconType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SOURCE_CLASS:
 				setSourceClass((SourceClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__STATE_MANAGER:
 				setStateManager((StateManagerType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SUGGESTED_VALUE:
 				setSuggestedValue((SuggestedValueType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SUPPORTED_LOCALE:
 				setSupportedLocale((SupportedLocaleType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_CLASS:
 				setSystemEventClass((SystemEventClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER:
 				setSystemEventListener((SystemEventListenerType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER_CLASS:
 				setSystemEventListenerClass((SystemEventListenerClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__TAG_HANDLER_DELEGATE_FACTORY:
 				setTagHandlerDelegateFactory((TagHandlerDelegateFactoryType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__TO_VIEW_ID:
 				setToViewId((ToViewIdType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR:
 				setValidator((ValidatorType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR_CLASS:
 				setValidatorClass((ValidatorClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR_ID:
 				setValidatorId((ValidatorIdType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALUE:
 				setValue((ValueType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALUE_CLASS:
 				setValueClass((ValueClassType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VARIABLE_RESOLVER:
 				setVariableResolver((VariableResolverType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VIEW_DECLARATION_LANGUAGE_FACTORY:
 				setViewDeclarationLanguageFactory((ViewDeclarationLanguageFactoryType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VIEW_HANDLER:
 				setViewHandler((ViewHandlerType)null);
 				return;
 			case FacesConfigPackage.DOCUMENT_ROOT__VISIT_CONTEXT_FACTORY:
 				setVisitContextFactory((VisitContextFactoryType)null);
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
 			case FacesConfigPackage.DOCUMENT_ROOT__MIXED:
 				return mixed != null && !mixed.isEmpty();
 			case FacesConfigPackage.DOCUMENT_ROOT__XMLNS_PREFIX_MAP:
 				return xMLNSPrefixMap != null && !xMLNSPrefixMap.isEmpty();
 			case FacesConfigPackage.DOCUMENT_ROOT__XSI_SCHEMA_LOCATION:
 				return xSISchemaLocation != null && !xSISchemaLocation.isEmpty();
 			case FacesConfigPackage.DOCUMENT_ROOT__ABSOLUTE_ORDERING:
 				return getAbsoluteOrdering() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__ACTION_LISTENER:
 				return getActionListener() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__APPLICATION:
 				return getApplication() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__APPLICATION_FACTORY:
 				return getApplicationFactory() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE:
 				return getAttribute() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_CLASS:
 				return getAttributeClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_EXTENSION:
 				return getAttributeExtension() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__ATTRIBUTE_NAME:
 				return getAttributeName() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR:
 				return getBehavior() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_CLASS:
 				return getBehaviorClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_ID:
 				return getBehaviorId() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__BEHAVIOR_EXTENSION:
 				return getBehaviorExtension() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT:
 				return getComponent() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_CLASS:
 				return getComponentClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_EXTENSION:
 				return getComponentExtension() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_FAMILY:
 				return getComponentFamily() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__COMPONENT_TYPE:
 				return getComponentType() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER:
 				return getConverter() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_CLASS:
 				return getConverterClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_FOR_CLASS:
 				return getConverterForClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__CONVERTER_ID:
 				return getConverterId() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_LOCALE:
 				return getDefaultLocale() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_RENDER_KIT_ID:
 				return getDefaultRenderKitId() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_VALIDATORS:
 				return getDefaultValidators() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__DEFAULT_VALUE:
 				return getDefaultValue() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__DESCRIPTION:
 				return getDescription() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__DISPLAY_NAME:
 				return getDisplayName() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__EXCEPTION_HANDLER_FACTORY:
 				return getExceptionHandlerFactory() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__EXTERNAL_CONTEXT_FACTORY:
 				return getExternalContextFactory() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACES_CONFIG:
 				return getFacesConfig() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACES_CONTEXT_FACTORY:
 				return getFacesContextFactory() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET:
 				return getFacet() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET_EXTENSION:
 				return getFacetExtension() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACET_NAME:
 				return getFacetName() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__FACTORY:
 				return getFactory() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_ACTION:
 				return getFromAction() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_OUTCOME:
 				return getFromOutcome() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__FROM_VIEW_ID:
 				return getFromViewId() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__ICON:
 				return getIcon() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__IF:
 				return getIf() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__KEY:
 				return getKey() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__KEY_CLASS:
 				return getKeyClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__LARGE_ICON:
 				return getLargeIcon() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__LIFECYCLE:
 				return getLifecycle() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__LIFECYCLE_FACTORY:
 				return getLifecycleFactory() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__LIST_ENTRIES:
 				return getListEntries() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__LOCALE_CONFIG:
 				return getLocaleConfig() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN:
 				return getManagedBean() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_CLASS:
 				return getManagedBeanClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_NAME:
 				return getManagedBeanName() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_BEAN_SCOPE:
 				return getManagedBeanScope() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__MANAGED_PROPERTY:
 				return getManagedProperty() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__MAP_ENTRIES:
 				return getMapEntries() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__MAP_ENTRY:
 				return getMapEntry() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__MESSAGE_BUNDLE:
 				return getMessageBundle() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAME:
 				return getName() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_CASE:
 				return getNavigationCase() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_HANDLER:
 				return getNavigationHandler() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__NAVIGATION_RULE:
 				return getNavigationRule() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__NULL_VALUE:
 				return getNullValue() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__ORDERING:
 				return getOrdering() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__ORDERING_ORDERING:
 				return getOrderingOrdering() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__OTHERS:
 				return getOthers() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__PARTIAL_VIEW_CONTEXT_FACTORY:
 				return getPartialViewContextFactory() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__PHASE_LISTENER:
 				return getPhaseListener() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY:
 				return getProperty() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_CLASS:
 				return getPropertyClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_EXTENSION:
 				return getPropertyExtension() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_NAME:
 				return getPropertyName() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__PROPERTY_RESOLVER:
 				return getPropertyResolver() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__REDIRECT:
 				return getRedirect() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__REDIRECT_VIEW_PARAM:
 				return getRedirectViewParam() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN:
 				return getReferencedBean() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN_CLASS:
 				return getReferencedBeanClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__REFERENCED_BEAN_NAME:
 				return getReferencedBeanName() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER:
 				return getRenderer() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_CLASS:
 				return getRendererClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_EXTENSION:
 				return getRendererExtension() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDERER_TYPE:
 				return getRendererType() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT:
 				return getRenderKit() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_CLASS:
 				return getRenderKitClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_FACTORY:
 				return getRenderKitFactory() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__RENDER_KIT_ID:
 				return getRenderKitId() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__RESOURCE_HANDLER:
 				return getResourceHandler() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__SMALL_ICON:
 				return getSmallIcon() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__SOURCE_CLASS:
 				return getSourceClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__STATE_MANAGER:
 				return getStateManager() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__SUGGESTED_VALUE:
 				return getSuggestedValue() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__SUPPORTED_LOCALE:
 				return getSupportedLocale() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_CLASS:
 				return getSystemEventClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER:
 				return getSystemEventListener() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__SYSTEM_EVENT_LISTENER_CLASS:
 				return getSystemEventListenerClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__TAG_HANDLER_DELEGATE_FACTORY:
 				return getTagHandlerDelegateFactory() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__TO_VIEW_ID:
 				return getToViewId() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR:
 				return getValidator() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR_CLASS:
 				return getValidatorClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALIDATOR_ID:
 				return getValidatorId() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALUE:
 				return getValue() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__VALUE_CLASS:
 				return getValueClass() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__VARIABLE_RESOLVER:
 				return getVariableResolver() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__VIEW_DECLARATION_LANGUAGE_FACTORY:
 				return getViewDeclarationLanguageFactory() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__VIEW_HANDLER:
 				return getViewHandler() != null;
 			case FacesConfigPackage.DOCUMENT_ROOT__VISIT_CONTEXT_FACTORY:
 				return getVisitContextFactory() != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
     /**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (mixed: ");//$NON-NLS-1$
 		result.append(mixed);
 		result.append(')');
 		return result.toString();
 	}
 
 } //DocumentRootImpl
