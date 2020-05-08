 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.aggregator.p2.impl;
 
 import java.lang.reflect.Method;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.b3.aggregator.p2.MetadataRepository;
 import org.eclipse.b3.aggregator.p2.P2Factory;
 import org.eclipse.b3.aggregator.p2.P2Package;
 import org.eclipse.b3.aggregator.p2.RepositoryReference;
 import org.eclipse.b3.util.ExceptionUtils;
 import org.eclipse.core.internal.runtime.AdapterManager;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.EcoreEMap;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository;
 import org.eclipse.equinox.internal.p2.metadata.repository.LocalMetadataRepository;
 import org.eclipse.equinox.p2.core.IProvisioningAgent;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.query.IQuery;
 import org.eclipse.equinox.p2.query.IQueryResult;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Metadata Repository</b></em>'. <!--
  * end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>{@link org.eclipse.b3.aggregator.p2.impl.MetadataRepositoryImpl#getProvisioningAgent <em>Provisioning Agent</em>}
  * </li>
  * <li>{@link org.eclipse.b3.aggregator.p2.impl.MetadataRepositoryImpl#getLocation <em>Location</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.p2.impl.MetadataRepositoryImpl#getName <em>Name</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.p2.impl.MetadataRepositoryImpl#getType <em>Type</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.p2.impl.MetadataRepositoryImpl#getVersion <em>Version</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.p2.impl.MetadataRepositoryImpl#getDescription <em>Description</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.p2.impl.MetadataRepositoryImpl#getProvider <em>Provider</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.p2.impl.MetadataRepositoryImpl#isModifiable <em>Modifiable</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.p2.impl.MetadataRepositoryImpl#getInstallableUnits <em>Installable Units</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.p2.impl.MetadataRepositoryImpl#getRepositoryReferences <em>Repository References
  * </em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.p2.impl.MetadataRepositoryImpl#getPropertyMap <em>Property Map</em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public class MetadataRepositoryImpl extends MinimalEObjectImpl.Container implements MetadataRepository {
 	private static final Method LocalMetadataRepository_createRepositoriesSnapshot;
 
 	static {
 		try {
 			LocalMetadataRepository_createRepositoriesSnapshot = LocalMetadataRepository.class.getDeclaredMethod("createRepositoriesSnapshot");
 			LocalMetadataRepository_createRepositoriesSnapshot.setAccessible(true);
 		}
 		catch(Exception e) {
 			throw new ExceptionInInitializerError(e);
 		}
 	}
 
 	/**
 	 * A set of bit flags representing the values of boolean attributes and whether unsettable features have been set.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 * @ordered
 	 */
 	protected int eFlags = 0;
 
 	/**
 	 * The cached value of the '{@link #getProvisioningAgent() <em>Provisioning Agent</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getProvisioningAgent()
 	 * @generated
 	 * @ordered
 	 */
 	protected IProvisioningAgent provisioningAgent;
 
 	/**
 	 * The default value of the '{@link #getLocation() <em>Location</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getLocation()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final URI LOCATION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLocation() <em>Location</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getLocation()
 	 * @generated
 	 * @ordered
 	 */
 	protected URI location = LOCATION_EDEFAULT;
 
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
 	 * The default value of the '{@link #getType() <em>Type</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String TYPE_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected String type = TYPE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String VERSION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected String version = VERSION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DESCRIPTION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected String description = DESCRIPTION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getProvider() <em>Provider</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getProvider()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PROVIDER_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getProvider() <em>Provider</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getProvider()
 	 * @generated
 	 * @ordered
 	 */
 	protected String provider = PROVIDER_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isModifiable() <em>Modifiable</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #isModifiable()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean MODIFIABLE_EDEFAULT = false;
 
 	/**
 	 * The flag representing the value of the '{@link #isModifiable() <em>Modifiable</em>}' attribute. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #isModifiable()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int MODIFIABLE_EFLAG = 1 << 0;
 
 	/**
 	 * The cached value of the '{@link #getInstallableUnits() <em>Installable Units</em>}' containment reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getInstallableUnits()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<IInstallableUnit> installableUnits;
 
 	/**
 	 * The cached value of the '{@link #getRepositoryReferences() <em>Repository References</em>}' containment reference
 	 * list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see #getRepositoryReferences()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<RepositoryReference> repositoryReferences;
 
 	/**
 	 * The cached value of the '{@link #getPropertyMap() <em>Property Map</em>}' map.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getPropertyMap()
 	 * @generated
 	 * @ordered
 	 */
 	protected EMap<String, String> propertyMap;
 
 	private static final Pattern categoryRefPattern = Pattern.compile("^(@installableUnits\\[id='.*'),version='0\\.0\\.0\\.[0-9]{14}'\\]$");
 
 	private static final Pattern proxyFragmentPattern = Pattern.compile("^[^:]+:(.*)#\\/\\/@metadataRepository$");
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected MetadataRepositoryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void addInstallableUnits(Collection<IInstallableUnit> installableUnits) {
 		EList<IInstallableUnit> iuList = getInstallableUnits();
 		for(IInstallableUnit iu : installableUnits)
 			iuList.add(InstallableUnitImpl.importToModel(iu));
 	}
 
 	/**
 	 * Excluded from the model; this is deprecated in the new API
 	 */
 	@Deprecated
 	public void addInstallableUnits(IInstallableUnit[] installableUnits) {
 		addInstallableUnits(Arrays.asList(installableUnits));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void addReference(URI location, String nickname, int type, int options) {
 		RepositoryReference ref = P2Factory.eINSTANCE.createRepositoryReference();
 		ref.setLocation(location);
 		ref.setNickname(nickname);
 		ref.setType(type);
 		ref.setOptions(options);
 		getRepositoryReferences().add(ref);
 	}
 
 	public void addRepositoryReferences(IMetadataRepositoryManager mdrMgr, IMetadataRepository mdr)
 			throws CoreException {
 		if(mdr instanceof LocalMetadataRepository) {
 			try {
 				@SuppressWarnings("unchecked")
 				List<org.eclipse.equinox.p2.repository.spi.RepositoryReference> refs = (List<org.eclipse.equinox.p2.repository.spi.RepositoryReference>) LocalMetadataRepository_createRepositoriesSnapshot.invoke(mdr);
 				for(org.eclipse.equinox.p2.repository.spi.RepositoryReference ref : refs)
 					addReference(ref.Location, ref.Nickname, ref.Type, ref.Options);
 			}
 			catch(Exception e) {
 				throw ExceptionUtils.wrap(e);
 			}
 		}
 		else if(mdr instanceof CompositeMetadataRepository) {
 			List<URI> children = ((CompositeMetadataRepository) mdr).getChildren();
 			for(URI child : children)
 				addRepositoryReferences(mdrMgr, mdrMgr.loadRepository(child, null));
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IProvisioningAgent basicGetProvisioningAgent() {
 		return provisioningAgent;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch(featureID) {
 		case P2Package.METADATA_REPOSITORY__PROVISIONING_AGENT:
 			if(resolve)
 				return getProvisioningAgent();
 			return basicGetProvisioningAgent();
 		case P2Package.METADATA_REPOSITORY__LOCATION:
 			return getLocation();
 		case P2Package.METADATA_REPOSITORY__NAME:
 			return getName();
 		case P2Package.METADATA_REPOSITORY__TYPE:
 			return getType();
 		case P2Package.METADATA_REPOSITORY__VERSION:
 			return getVersion();
 		case P2Package.METADATA_REPOSITORY__DESCRIPTION:
 			return getDescription();
 		case P2Package.METADATA_REPOSITORY__PROVIDER:
 			return getProvider();
 		case P2Package.METADATA_REPOSITORY__MODIFIABLE:
 			return isModifiable();
 		case P2Package.METADATA_REPOSITORY__INSTALLABLE_UNITS:
 			return getInstallableUnits();
 		case P2Package.METADATA_REPOSITORY__REPOSITORY_REFERENCES:
 			return getRepositoryReferences();
 		case P2Package.METADATA_REPOSITORY__PROPERTY_MAP:
 			if(coreType)
 				return getPropertyMap();
 			else
 				return getPropertyMap().map();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch(featureID) {
 		case P2Package.METADATA_REPOSITORY__INSTALLABLE_UNITS:
 			return ((InternalEList<?>) getInstallableUnits()).basicRemove(otherEnd, msgs);
 		case P2Package.METADATA_REPOSITORY__REPOSITORY_REFERENCES:
 			return ((InternalEList<?>) getRepositoryReferences()).basicRemove(otherEnd, msgs);
 		case P2Package.METADATA_REPOSITORY__PROPERTY_MAP:
 			return ((InternalEList<?>) getPropertyMap()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch(featureID) {
 		case P2Package.METADATA_REPOSITORY__PROVISIONING_AGENT:
 			return provisioningAgent != null;
 		case P2Package.METADATA_REPOSITORY__LOCATION:
 			return LOCATION_EDEFAULT == null
 					? location != null
 					: !LOCATION_EDEFAULT.equals(location);
 		case P2Package.METADATA_REPOSITORY__NAME:
 			return NAME_EDEFAULT == null
 					? name != null
 					: !NAME_EDEFAULT.equals(name);
 		case P2Package.METADATA_REPOSITORY__TYPE:
 			return TYPE_EDEFAULT == null
 					? type != null
 					: !TYPE_EDEFAULT.equals(type);
 		case P2Package.METADATA_REPOSITORY__VERSION:
 			return VERSION_EDEFAULT == null
 					? version != null
 					: !VERSION_EDEFAULT.equals(version);
 		case P2Package.METADATA_REPOSITORY__DESCRIPTION:
 			return DESCRIPTION_EDEFAULT == null
 					? description != null
 					: !DESCRIPTION_EDEFAULT.equals(description);
 		case P2Package.METADATA_REPOSITORY__PROVIDER:
 			return PROVIDER_EDEFAULT == null
 					? provider != null
 					: !PROVIDER_EDEFAULT.equals(provider);
 		case P2Package.METADATA_REPOSITORY__MODIFIABLE:
 			return ((eFlags & MODIFIABLE_EFLAG) != 0) != MODIFIABLE_EDEFAULT;
 		case P2Package.METADATA_REPOSITORY__INSTALLABLE_UNITS:
 			return installableUnits != null && !installableUnits.isEmpty();
 		case P2Package.METADATA_REPOSITORY__REPOSITORY_REFERENCES:
 			return repositoryReferences != null && !repositoryReferences.isEmpty();
 		case P2Package.METADATA_REPOSITORY__PROPERTY_MAP:
 			return propertyMap != null && !propertyMap.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	@Override
 	public EObject eObjectForURIFragmentSegment(String uriFragmentSegment) {
 		// Strip off the version part of the fragment if this is a category
 		// reference. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=286736
 		//
 		Matcher m = categoryRefPattern.matcher(uriFragmentSegment);
 		if(m.matches())
 			uriFragmentSegment = m.group(1) + ']';
 		return super.eObjectForURIFragmentSegment(uriFragmentSegment);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if(this == obj)
 			return true;
 		if(!(obj instanceof MetadataRepository))
 			return false;
 		return getSafeLocation().equals(((MetadataRepository) obj).getSafeLocation());
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch(featureID) {
 		case P2Package.METADATA_REPOSITORY__PROVISIONING_AGENT:
 			setProvisioningAgent((IProvisioningAgent) newValue);
 			return;
 		case P2Package.METADATA_REPOSITORY__LOCATION:
 			setLocation((URI) newValue);
 			return;
 		case P2Package.METADATA_REPOSITORY__NAME:
 			setName((String) newValue);
 			return;
 		case P2Package.METADATA_REPOSITORY__TYPE:
 			setType((String) newValue);
 			return;
 		case P2Package.METADATA_REPOSITORY__VERSION:
 			setVersion((String) newValue);
 			return;
 		case P2Package.METADATA_REPOSITORY__DESCRIPTION:
 			setDescription((String) newValue);
 			return;
 		case P2Package.METADATA_REPOSITORY__PROVIDER:
 			setProvider((String) newValue);
 			return;
 		case P2Package.METADATA_REPOSITORY__MODIFIABLE:
 			setModifiable((Boolean) newValue);
 			return;
 		case P2Package.METADATA_REPOSITORY__INSTALLABLE_UNITS:
 			getInstallableUnits().clear();
 			getInstallableUnits().addAll((Collection<? extends IInstallableUnit>) newValue);
 			return;
 		case P2Package.METADATA_REPOSITORY__REPOSITORY_REFERENCES:
 			getRepositoryReferences().clear();
 			getRepositoryReferences().addAll((Collection<? extends RepositoryReference>) newValue);
 			return;
 		case P2Package.METADATA_REPOSITORY__PROPERTY_MAP:
 			((EStructuralFeature.Setting) getPropertyMap()).set(newValue);
 			return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch(featureID) {
 		case P2Package.METADATA_REPOSITORY__PROVISIONING_AGENT:
 			setProvisioningAgent((IProvisioningAgent) null);
 			return;
 		case P2Package.METADATA_REPOSITORY__LOCATION:
 			setLocation(LOCATION_EDEFAULT);
 			return;
 		case P2Package.METADATA_REPOSITORY__NAME:
 			setName(NAME_EDEFAULT);
 			return;
 		case P2Package.METADATA_REPOSITORY__TYPE:
 			setType(TYPE_EDEFAULT);
 			return;
 		case P2Package.METADATA_REPOSITORY__VERSION:
 			setVersion(VERSION_EDEFAULT);
 			return;
 		case P2Package.METADATA_REPOSITORY__DESCRIPTION:
 			setDescription(DESCRIPTION_EDEFAULT);
 			return;
 		case P2Package.METADATA_REPOSITORY__PROVIDER:
 			setProvider(PROVIDER_EDEFAULT);
 			return;
 		case P2Package.METADATA_REPOSITORY__MODIFIABLE:
 			setModifiable(MODIFIABLE_EDEFAULT);
 			return;
 		case P2Package.METADATA_REPOSITORY__INSTALLABLE_UNITS:
 			getInstallableUnits().clear();
 			return;
 		case P2Package.METADATA_REPOSITORY__REPOSITORY_REFERENCES:
 			getRepositoryReferences().clear();
 			return;
 		case P2Package.METADATA_REPOSITORY__PROPERTY_MAP:
 			getPropertyMap().clear();
 			return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@SuppressWarnings("rawtypes")
 	public Object getAdapter(Class adapter) {
 		return AdapterManager.getDefault().getAdapter(this, adapter);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<IInstallableUnit> getInstallableUnits() {
 		if(installableUnits == null) {
 			installableUnits = new EObjectContainmentEList.Resolving<IInstallableUnit>(IInstallableUnit.class, this,
 					P2Package.METADATA_REPOSITORY__INSTALLABLE_UNITS);
 		}
 		return installableUnits;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public URI getLocation() {
 		return location;
 	}
 
 	public URI getLocationFromProxy() {
 		Matcher m = proxyFragmentPattern.matcher(eProxyURI().toString());
 		try {
 			return m.matches()
 					? new URI(m.group(1))
 					: null;
 		}
 		catch(URISyntaxException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getName() {
 		return name;
 	}
 
 	public String getNameFromProxy() {
 		// no way to retrieve name from location
 		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public Map<String, String> getProperties() {
 		return getPropertyMap().map();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EMap<String, String> getPropertyMap() {
 		if(propertyMap == null) {
 			propertyMap = new EcoreEMap<String, String>(P2Package.Literals.PROPERTY, PropertyImpl.class, this,
 					P2Package.METADATA_REPOSITORY__PROPERTY_MAP);
 		}
 		return propertyMap;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getProvider() {
 		return provider;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
	 * @generated
 	 */
 	public IProvisioningAgent getProvisioningAgent() {
		if(provisioningAgent != null && ((EObject) provisioningAgent).eIsProxy()) {
			InternalEObject oldProvisioningAgent = (InternalEObject) provisioningAgent;
			provisioningAgent = (IProvisioningAgent) eResolveProxy(oldProvisioningAgent);
			if(provisioningAgent != oldProvisioningAgent) {
				if(eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE,
							P2Package.METADATA_REPOSITORY__PROVISIONING_AGENT, oldProvisioningAgent, provisioningAgent));
			}
		}
		return provisioningAgent;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<RepositoryReference> getRepositoryReferences() {
 		if(repositoryReferences == null) {
 			repositoryReferences = new EObjectContainmentEList.Resolving<RepositoryReference>(
 					RepositoryReference.class, this, P2Package.METADATA_REPOSITORY__REPOSITORY_REFERENCES);
 		}
 		return repositoryReferences;
 	}
 
 	public URI getSafeLocation() {
 		return location != null
 				? location
 				: getLocationFromProxy();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getVersion() {
 		return version;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public boolean isModifiable() {
 		return (eFlags & MODIFIABLE_EFLAG) != 0;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public IQueryResult<IInstallableUnit> query(IQuery<IInstallableUnit> query, IProgressMonitor progress) {
 		return query.perform(getInstallableUnits().iterator());
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void removeAll() {
 		getInstallableUnits().clear();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean removeInstallableUnits(Collection<IInstallableUnit> installableUnits) {
 		if(installableUnits == null)
 			return false;
 
 		List<IInstallableUnit> units = getInstallableUnits();
 
 		return units.removeAll(installableUnits);
 	}
 
 	/**
 	 * Excluded from the model; this is deprecated in the new API
 	 */
 	@Deprecated
 	public boolean removeInstallableUnits(IInstallableUnit[] installableUnits, IProgressMonitor monitor) {
 		return removeInstallableUnits(Arrays.asList(installableUnits));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setDescription(String newDescription) {
 		String oldDescription = description;
 		description = newDescription;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.METADATA_REPOSITORY__DESCRIPTION,
 					oldDescription, description));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setLocation(URI newLocation) {
 		URI oldLocation = location;
 		location = newLocation;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.METADATA_REPOSITORY__LOCATION, oldLocation,
 					location));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setModifiable(boolean newModifiable) {
 		boolean oldModifiable = (eFlags & MODIFIABLE_EFLAG) != 0;
 		if(newModifiable)
 			eFlags |= MODIFIABLE_EFLAG;
 		else
 			eFlags &= ~MODIFIABLE_EFLAG;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.METADATA_REPOSITORY__MODIFIABLE,
 					oldModifiable, newModifiable));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setName(String newName) {
 		String oldName = name;
 		name = newName;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.METADATA_REPOSITORY__NAME, oldName, name));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public String setProperty(String key, String value) {
 		return getPropertyMap().put(key, value);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setProvider(String newProvider) {
 		String oldProvider = provider;
 		provider = newProvider;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.METADATA_REPOSITORY__PROVIDER, oldProvider,
 					provider));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setProvisioningAgent(IProvisioningAgent newProvisioningAgent) {
 		IProvisioningAgent oldProvisioningAgent = provisioningAgent;
 		provisioningAgent = newProvisioningAgent;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.METADATA_REPOSITORY__PROVISIONING_AGENT,
 					oldProvisioningAgent, provisioningAgent));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setType(String newType) {
 		String oldType = type;
 		type = newType;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.METADATA_REPOSITORY__TYPE, oldType, type));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setVersion(String newVersion) {
 		String oldVersion = version;
 		version = newVersion;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, P2Package.METADATA_REPOSITORY__VERSION, oldVersion,
 					version));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if(eIsProxy())
 			return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (location: ");
 		result.append(location);
 		result.append(", name: ");
 		result.append(name);
 		result.append(", type: ");
 		result.append(type);
 		result.append(", version: ");
 		result.append(version);
 		result.append(", description: ");
 		result.append(description);
 		result.append(", provider: ");
 		result.append(provider);
 		result.append(", modifiable: ");
 		result.append((eFlags & MODIFIABLE_EFLAG) != 0);
 		result.append(')');
 		return result.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return P2Package.Literals.METADATA_REPOSITORY;
 	}
 
 } // MetadataRepositoryImpl
