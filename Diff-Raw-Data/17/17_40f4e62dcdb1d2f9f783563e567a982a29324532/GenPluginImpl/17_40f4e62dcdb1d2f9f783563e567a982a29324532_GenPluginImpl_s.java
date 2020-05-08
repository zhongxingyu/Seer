 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.gmf.codegen.gmfgen.impl;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.gmfgen.GenExpressionProviderBase;
 import org.eclipse.gmf.codegen.gmfgen.GenPlugin;
 import org.eclipse.gmf.codegen.gmfgen.Viewmap;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Gen Plugin</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenPluginImpl#getEditorGen <em>Editor Gen</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenPluginImpl#getID <em>ID</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenPluginImpl#getName <em>Name</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenPluginImpl#getProvider <em>Provider</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenPluginImpl#getVersion <em>Version</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenPluginImpl#isPrintingEnabled <em>Printing Enabled</em>}</li>
  *   <li>{@link org.eclipse.gmf.codegen.gmfgen.impl.GenPluginImpl#getActivatorClassName <em>Activator Class Name</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class GenPluginImpl extends EObjectImpl implements GenPlugin {
 	/**
 	 * The default value of the '{@link #getID() <em>ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getID()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ID_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getID() <em>ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getID()
 	 * @generated
 	 * @ordered
 	 */
 	protected String iD = ID_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String name = NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getProvider() <em>Provider</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getProvider()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PROVIDER_EDEFAULT = "Sample Plugin Provider, Inc";
 
 	/**
 	 * The cached value of the '{@link #getProvider() <em>Provider</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getProvider()
 	 * @generated
 	 * @ordered
 	 */
 	protected String provider = PROVIDER_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String VERSION_EDEFAULT = "1.0.0.qualifier";
 
 	/**
 	 * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected String version = VERSION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isPrintingEnabled() <em>Printing Enabled</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isPrintingEnabled()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean PRINTING_ENABLED_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isPrintingEnabled() <em>Printing Enabled</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isPrintingEnabled()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean printingEnabled = PRINTING_ENABLED_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getActivatorClassName() <em>Activator Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getActivatorClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ACTIVATOR_CLASS_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getActivatorClassName() <em>Activator Class Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getActivatorClassName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String activatorClassName = ACTIVATOR_CLASS_NAME_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected GenPluginImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return GMFGenPackage.eINSTANCE.getGenPlugin();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public GenEditorGenerator getEditorGen() {
 		if (eContainerFeatureID != GMFGenPackage.GEN_PLUGIN__EDITOR_GEN) return null;
 		return (GenEditorGenerator)eContainer();
 	}
 
 	public String getID() {
 		String value = getIDGen();
 		if (value == null || value.length() == 0) {
 			return getEditorGen().getDomainGenModel().getModelPluginID() + ".diagram";
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getIDGen() {
 		return iD;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setID(String newID) {
 		String oldID = iD;
 		iD = newID;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_PLUGIN__ID, oldID, iD));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setName(String newName) {
 		String oldName = name;
 		name = newName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_PLUGIN__NAME, oldName, name));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getProvider() {
 		return provider;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setProvider(String newProvider) {
 		String oldProvider = provider;
 		provider = newProvider;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_PLUGIN__PROVIDER, oldProvider, provider));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getVersion() {
 		return version;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setVersion(String newVersion) {
 		String oldVersion = version;
 		version = newVersion;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_PLUGIN__VERSION, oldVersion, version));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isPrintingEnabled() {
 		return printingEnabled;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPrintingEnabled(boolean newPrintingEnabled) {
 		boolean oldPrintingEnabled = printingEnabled;
 		printingEnabled = newPrintingEnabled;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_PLUGIN__PRINTING_ENABLED, oldPrintingEnabled, printingEnabled));
 	}
 
 	public String getActivatorClassName() {
 		String value = getActivatorClassNameGen();
 		if (value == null || value.trim().length() == 0) {
 			// FIXME is it really required to have domainPackageCapName there?
 			value = ((GenDiagramImpl) getDiagram()).getDomainPackageCapName() + "DiagramEditorPlugin";
 		}
 		return value;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getActivatorClassNameGen() {
 		return activatorClassName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setActivatorClassName(String newActivatorClassName) {
 		String oldActivatorClassName = activatorClassName;
 		activatorClassName = newActivatorClassName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, GMFGenPackage.GEN_PLUGIN__ACTIVATOR_CLASS_NAME, oldActivatorClassName, activatorClassName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList getRequiredPluginIDs() {
 		Collection requiredPlugins = new LinkedHashSet();
 		
 		requiredPlugins.addAll(getExpressionsRequiredPluginIDs());
 		requiredPlugins.addAll(getValidationRequiredPluginIDs());
 		requiredPlugins.addAll(getViewmapRequiredPluginIDs());
 		return new BasicEList(requiredPlugins);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getActivatorQualifiedClassName() {
 		return getEditorGen().getEditor().getPackageName() + '.' + getActivatorClassName();
 	}
 
 	private Set getExpressionsRequiredPluginIDs() {
 		Set requiredIDs = new HashSet();
 		if(getEditorGen().getExpressionProviders() != null) {
 			for (Iterator it = getEditorGen().getExpressionProviders().getProviders().iterator(); it.hasNext();) {
 				GenExpressionProviderBase nextProvider = (GenExpressionProviderBase) it.next();
 				requiredIDs.addAll(nextProvider.getRequiredPluginIDs());
 			}
 		}
 		return requiredIDs;
 	}
 	
 	/**
 	 * @generated NOT
 	 */
 	private Collection getViewmapRequiredPluginIDs() {
 		Collection result = null;
 		for (TreeIterator contents = EcoreUtil.getAllContents(getDiagram().getAllNodes()); contents.hasNext();){
 			EObject next = (EObject) contents.next();
 			if (next instanceof Viewmap && next.eIsSet(GMFGenPackage.eINSTANCE.getViewmap_RequiredPluginIDs())){
 				if (result == null){
 					result = new HashSet();
 				}
 				result.addAll(((Viewmap)next).getRequiredPluginIDs());
 				contents.prune();
 			}
 		}
 		return result == null ? Collections.EMPTY_LIST : result;
 	}
 	
 	private Set getValidationRequiredPluginIDs() {
 		if(getDiagram().isValidationEnabled() || (getEditorGen().getAudits() != null && !getEditorGen().getAudits().getAllAuditRules().isEmpty())) {
 			return Collections.singleton("org.eclipse.emf.validation"); //$NON-NLS-1$ 
 		}
 		return Collections.EMPTY_SET;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case GMFGenPackage.GEN_PLUGIN__EDITOR_GEN:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return eBasicSetContainer(otherEnd, GMFGenPackage.GEN_PLUGIN__EDITOR_GEN, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case GMFGenPackage.GEN_PLUGIN__EDITOR_GEN:
 				return eBasicSetContainer(null, GMFGenPackage.GEN_PLUGIN__EDITOR_GEN, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
 		switch (eContainerFeatureID) {
 			case GMFGenPackage.GEN_PLUGIN__EDITOR_GEN:
 				return eInternalContainer().eInverseRemove(this, GMFGenPackage.GEN_EDITOR_GENERATOR__PLUGIN, GenEditorGenerator.class, msgs);
 		}
 		return super.eBasicRemoveFromContainerFeature(msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case GMFGenPackage.GEN_PLUGIN__EDITOR_GEN:
 				return getEditorGen();
 			case GMFGenPackage.GEN_PLUGIN__ID:
 				return getID();
 			case GMFGenPackage.GEN_PLUGIN__NAME:
 				return getName();
 			case GMFGenPackage.GEN_PLUGIN__PROVIDER:
 				return getProvider();
 			case GMFGenPackage.GEN_PLUGIN__VERSION:
 				return getVersion();
 			case GMFGenPackage.GEN_PLUGIN__PRINTING_ENABLED:
 				return isPrintingEnabled() ? Boolean.TRUE : Boolean.FALSE;
 			case GMFGenPackage.GEN_PLUGIN__ACTIVATOR_CLASS_NAME:
 				return getActivatorClassName();
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
 			case GMFGenPackage.GEN_PLUGIN__ID:
 				setID((String)newValue);
 				return;
 			case GMFGenPackage.GEN_PLUGIN__NAME:
 				setName((String)newValue);
 				return;
 			case GMFGenPackage.GEN_PLUGIN__PROVIDER:
 				setProvider((String)newValue);
 				return;
 			case GMFGenPackage.GEN_PLUGIN__VERSION:
 				setVersion((String)newValue);
 				return;
 			case GMFGenPackage.GEN_PLUGIN__PRINTING_ENABLED:
 				setPrintingEnabled(((Boolean)newValue).booleanValue());
 				return;
 			case GMFGenPackage.GEN_PLUGIN__ACTIVATOR_CLASS_NAME:
 				setActivatorClassName((String)newValue);
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
 			case GMFGenPackage.GEN_PLUGIN__ID:
 				setID(ID_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_PLUGIN__NAME:
 				setName(NAME_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_PLUGIN__PROVIDER:
 				setProvider(PROVIDER_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_PLUGIN__VERSION:
 				setVersion(VERSION_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_PLUGIN__PRINTING_ENABLED:
 				setPrintingEnabled(PRINTING_ENABLED_EDEFAULT);
 				return;
 			case GMFGenPackage.GEN_PLUGIN__ACTIVATOR_CLASS_NAME:
 				setActivatorClassName(ACTIVATOR_CLASS_NAME_EDEFAULT);
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
 			case GMFGenPackage.GEN_PLUGIN__EDITOR_GEN:
 				return getEditorGen() != null;
 			case GMFGenPackage.GEN_PLUGIN__ID:
 				return ID_EDEFAULT == null ? iD != null : !ID_EDEFAULT.equals(iD);
 			case GMFGenPackage.GEN_PLUGIN__NAME:
 				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
 			case GMFGenPackage.GEN_PLUGIN__PROVIDER:
 				return PROVIDER_EDEFAULT == null ? provider != null : !PROVIDER_EDEFAULT.equals(provider);
 			case GMFGenPackage.GEN_PLUGIN__VERSION:
 				return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
 			case GMFGenPackage.GEN_PLUGIN__PRINTING_ENABLED:
 				return printingEnabled != PRINTING_ENABLED_EDEFAULT;
 			case GMFGenPackage.GEN_PLUGIN__ACTIVATOR_CLASS_NAME:
 				return ACTIVATOR_CLASS_NAME_EDEFAULT == null ? activatorClassName != null : !ACTIVATOR_CLASS_NAME_EDEFAULT.equals(activatorClassName);
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (iD: ");
 		result.append(iD);
 		result.append(", name: ");
 		result.append(name);
 		result.append(", provider: ");
 		result.append(provider);
 		result.append(", version: ");
 		result.append(version);
 		result.append(", printingEnabled: ");
 		result.append(printingEnabled);
 		result.append(", activatorClassName: ");
 		result.append(activatorClassName);
 		result.append(')');
 		return result.toString();
 	}
 
 	private GenDiagram getDiagram() {
 		return getEditorGen().getDiagram();
 	}
 } //GenPluginImpl
