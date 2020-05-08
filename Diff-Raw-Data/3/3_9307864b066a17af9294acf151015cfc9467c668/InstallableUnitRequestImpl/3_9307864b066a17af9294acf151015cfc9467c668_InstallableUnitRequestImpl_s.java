 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  *
  * $Id$
  */
 package org.eclipse.b3.aggregator.impl;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.ConcurrentModificationException;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.eclipse.b3.aggregator.AggregatorFactory;
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.AggregatorPlugin;
 import org.eclipse.b3.aggregator.AvailableVersion;
 import org.eclipse.b3.aggregator.AvailableVersionsHeader;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.DescriptionProvider;
 import org.eclipse.b3.aggregator.EnabledStatusProvider;
 import org.eclipse.b3.aggregator.InfosProvider;
 import org.eclipse.b3.aggregator.InstallableUnitRequest;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.Status;
 import org.eclipse.b3.aggregator.StatusCode;
 import org.eclipse.b3.aggregator.VersionMatch;
 import org.eclipse.b3.aggregator.p2.util.MetadataRepositoryResourceImpl;
 import org.eclipse.b3.aggregator.util.GeneralUtils;
 import org.eclipse.b3.p2.InstallableUnit;
 import org.eclipse.b3.p2.MetadataRepository;
 import org.eclipse.b3.p2.P2Factory;
 import org.eclipse.b3.p2.P2Package;
 import org.eclipse.b3.p2.util.P2ResourceImpl;
 import org.eclipse.b3.util.StringUtils;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
 import org.eclipse.equinox.p2.query.IQuery;
 import org.eclipse.equinox.p2.query.IQueryResult;
 import org.eclipse.equinox.p2.query.QueryUtil;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Installable Unit Reference</b></em>'. <!--
  * end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>{@link org.eclipse.b3.aggregator.impl.InstallableUnitRequestImpl#getStatus <em>Status</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.impl.InstallableUnitRequestImpl#getErrors <em>Errors</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.impl.InstallableUnitRequestImpl#getWarnings <em>Warnings</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.impl.InstallableUnitRequestImpl#getInfos <em>Infos</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.impl.InstallableUnitRequestImpl#getDescription <em>Description</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.impl.InstallableUnitRequestImpl#getName <em>Name</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.impl.InstallableUnitRequestImpl#getVersionRange <em>Version Range</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.impl.InstallableUnitRequestImpl#getAvailableVersionsHeader <em>Available Versions Header</em>}</li>
  * <li>{@link org.eclipse.b3.aggregator.impl.InstallableUnitRequestImpl#getAvailableVersions <em>Available Versions </em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public abstract class InstallableUnitRequestImpl extends MinimalEObjectImpl.Container implements InstallableUnitRequest {
 	/**
 	 * This looks up a string in the plugin's plugin.properties file. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	private static String getString(String key) {
 		return AggregatorPlugin.INSTANCE.getString(key);
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
 	 * The cached value of the '{@link #getErrors() <em>Errors</em>}' attribute list.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getErrors()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<String> errors;
 
 	/**
 	 * The cached value of the '{@link #getWarnings() <em>Warnings</em>}' attribute list.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getWarnings()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<String> warnings;
 
 	/**
 	 * The cached value of the '{@link #getInfos() <em>Infos</em>}' attribute list.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @see #getInfos()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<String> infos;
 
 	/**
 	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DESCRIPTION_EDEFAULT = "";
 
 	/**
 	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected String description = DESCRIPTION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String name = NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getVersionRange() <em>Version Range</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getVersionRange()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final VersionRange VERSION_RANGE_EDEFAULT = (VersionRange) P2Factory.eINSTANCE.createFromString(
 		P2Package.eINSTANCE.getVersionRange(), "0.0.0");
 
 	/**
 	 * The cached value of the '{@link #getVersionRange() <em>Version Range</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getVersionRange()
 	 * @generated
 	 * @ordered
 	 */
 	protected VersionRange versionRange = VERSION_RANGE_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getAvailableVersionsHeader() <em>Available Versions Header</em>}' containment
 	 * reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getAvailableVersionsHeader()
 	 * @generated
 	 * @ordered
 	 */
 	protected AvailableVersionsHeader availableVersionsHeader;
 
 	/**
 	 * The cached value of the '{@link #getAvailableVersions() <em>Available Versions</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getAvailableVersions()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<AvailableVersion> availableVersions;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected InstallableUnitRequestImpl() {
 		super();
 		setAvailableVersionsHeader(AggregatorFactory.eINSTANCE.createAvailableVersionsHeader());
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public NotificationChain basicSetAvailableVersionsHeader(AvailableVersionsHeader newAvailableVersionsHeader,
 			NotificationChain msgs) {
 		AvailableVersionsHeader oldAvailableVersionsHeader = availableVersionsHeader;
 		availableVersionsHeader = newAvailableVersionsHeader;
 		if(eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(
 				this, Notification.SET, AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS_HEADER,
 				oldAvailableVersionsHeader, newAvailableVersionsHeader);
 			if(msgs == null)
 				msgs = notification;
 			else
 				msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
 		if(baseClass == InfosProvider.class) {
 			switch(derivedFeatureID) {
 				case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__ERRORS:
 					return AggregatorPackage.INFOS_PROVIDER__ERRORS;
 				case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__WARNINGS:
 					return AggregatorPackage.INFOS_PROVIDER__WARNINGS;
 				case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__INFOS:
 					return AggregatorPackage.INFOS_PROVIDER__INFOS;
 				default:
 					return -1;
 			}
 		}
 		if(baseClass == DescriptionProvider.class) {
 			switch(derivedFeatureID) {
 				case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__DESCRIPTION:
 					return AggregatorPackage.DESCRIPTION_PROVIDER__DESCRIPTION;
 				default:
 					return -1;
 			}
 		}
 		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
 		if(baseClass == InfosProvider.class) {
 			switch(baseFeatureID) {
 				case AggregatorPackage.INFOS_PROVIDER__ERRORS:
 					return AggregatorPackage.INSTALLABLE_UNIT_REQUEST__ERRORS;
 				case AggregatorPackage.INFOS_PROVIDER__WARNINGS:
 					return AggregatorPackage.INSTALLABLE_UNIT_REQUEST__WARNINGS;
 				case AggregatorPackage.INFOS_PROVIDER__INFOS:
 					return AggregatorPackage.INSTALLABLE_UNIT_REQUEST__INFOS;
 				default:
 					return -1;
 			}
 		}
 		if(baseClass == DescriptionProvider.class) {
 			switch(baseFeatureID) {
 				case AggregatorPackage.DESCRIPTION_PROVIDER__DESCRIPTION:
 					return AggregatorPackage.INSTALLABLE_UNIT_REQUEST__DESCRIPTION;
 				default:
 					return -1;
 			}
 		}
 		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch(featureID) {
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__STATUS:
 				return getStatus();
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__ERRORS:
 				return getErrors();
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__WARNINGS:
 				return getWarnings();
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__INFOS:
 				return getInfos();
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__DESCRIPTION:
 				return getDescription();
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__NAME:
 				return getName();
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__VERSION_RANGE:
 				return getVersionRange();
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS_HEADER:
 				return getAvailableVersionsHeader();
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS:
 				return getAvailableVersions();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch(featureID) {
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS_HEADER:
 				if(availableVersionsHeader != null)
 					msgs = ((InternalEObject) availableVersionsHeader).eInverseRemove(this, EOPPOSITE_FEATURE_BASE -
 							AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS_HEADER, null, msgs);
 				return basicSetAvailableVersionsHeader((AvailableVersionsHeader) otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch(featureID) {
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS_HEADER:
 				return basicSetAvailableVersionsHeader(null, msgs);
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS:
 				return ((InternalEList<?>) getAvailableVersions()).basicRemove(otherEnd, msgs);
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
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__STATUS:
 				return getStatus() != null;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__ERRORS:
 				return errors != null && !errors.isEmpty();
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__WARNINGS:
 				return warnings != null && !warnings.isEmpty();
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__INFOS:
 				return infos != null && !infos.isEmpty();
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__DESCRIPTION:
 				return DESCRIPTION_EDEFAULT == null
 						? description != null
 						: !DESCRIPTION_EDEFAULT.equals(description);
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__NAME:
 				return NAME_EDEFAULT == null
 						? name != null
 						: !NAME_EDEFAULT.equals(name);
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__VERSION_RANGE:
 				return VERSION_RANGE_EDEFAULT == null
 						? versionRange != null
 						: !VERSION_RANGE_EDEFAULT.equals(versionRange);
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS_HEADER:
 				return availableVersionsHeader != null;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS:
 				return availableVersions != null && !availableVersions.isEmpty();
 		}
 		return super.eIsSet(featureID);
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
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__ERRORS:
 				getErrors().clear();
 				getErrors().addAll((Collection<? extends String>) newValue);
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__WARNINGS:
 				getWarnings().clear();
 				getWarnings().addAll((Collection<? extends String>) newValue);
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__INFOS:
 				getInfos().clear();
 				getInfos().addAll((Collection<? extends String>) newValue);
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__DESCRIPTION:
 				setDescription((String) newValue);
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__NAME:
 				setName((String) newValue);
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__VERSION_RANGE:
 				setVersionRange((VersionRange) newValue);
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS_HEADER:
 				setAvailableVersionsHeader((AvailableVersionsHeader) newValue);
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS:
 				getAvailableVersions().clear();
 				getAvailableVersions().addAll((Collection<? extends AvailableVersion>) newValue);
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
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__ERRORS:
 				getErrors().clear();
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__WARNINGS:
 				getWarnings().clear();
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__INFOS:
 				getInfos().clear();
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__DESCRIPTION:
 				setDescription(DESCRIPTION_EDEFAULT);
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__NAME:
 				setName(NAME_EDEFAULT);
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__VERSION_RANGE:
 				setVersionRange(VERSION_RANGE_EDEFAULT);
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS_HEADER:
 				setAvailableVersionsHeader((AvailableVersionsHeader) null);
 				return;
 			case AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS:
 				getAvailableVersions().clear();
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public EList<AvailableVersion> getAvailableVersions() {
 		if(availableVersions == null) {
 			resolveAvailableVersions(false);
 		}
 		return availableVersions;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public AvailableVersionsHeader getAvailableVersionsHeader() {
 		return availableVersionsHeader;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public EList<String> getErrors() {
 		errors = new BasicEList<String>();
 
 		if(!isMappedRepositoryBroken()) {
 			if(resolveAsSingleton() == null)
 				errors.add(getString("_UI_ErrorMessage_NoInstallableUnitIsAvailable"));
 		}
 
 		return errors;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public EList<String> getInfos() {
 		infos = new BasicEList<String>();
 
 		// TODO Informing about new versions is deprecated for now
 
 		return infos;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	synchronized public Status getStatus() {
 		if(!isBranchDisabledOrMappedRepositoryBroken()) {
 			if(resolveAsSingleton() == null)
 				return AggregatorFactory.eINSTANCE.createStatus(
 					StatusCode.BROKEN, getString("_UI_ErrorMessage_NoInstallableUnitIsAvailable"));
 		}
 
 		return AggregatorFactory.eINSTANCE.createStatus(StatusCode.OK);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public VersionRange getVersionRange() {
 		return versionRange;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EList<String> getWarnings() {
 		if(warnings == null) {
 			warnings = new EDataTypeUniqueEList<String>(
 				String.class, this, AggregatorPackage.INSTALLABLE_UNIT_REQUEST__WARNINGS);
 		}
 		return warnings;
 	}
 
 	public boolean isBranchDisabledOrMappedRepositoryBroken() {
 		return !isBranchEnabled() || isMappedRepositoryBroken();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean isBranchEnabled() {
 		if(this instanceof EnabledStatusProvider && !((EnabledStatusProvider) this).isEnabled())
 			return false;
 
 		MappedRepository mappedRepository = (MappedRepository) eContainer();
 
 		// a new MappedUnit without any container is enabled - used by commands that add MappedUnits
 		if(mappedRepository == null)
 			return true;
 
 		if(!mappedRepository.isEnabled())
 			return false;
 
 		Contribution contribution = (Contribution) ((EObject) mappedRepository).eContainer();
 		if(contribution != null && !contribution.isEnabled())
 			return false;
 
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean isMappedRepositoryBroken() {
 		MappedRepository repo = (MappedRepository) eContainer();
 		return repo == null || repo.getMetadataRepository(false) == null ||
 				((EObject) repo.getMetadataRepository()).eIsProxy();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public IInstallableUnit resolveAsSingleton() {
 		return resolveAsSingleton(false);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public IInstallableUnit resolveAsSingleton(boolean forceResolve) {
 		String id = getName();
 
 		if(id == null)
 			return null;
 
 		IQuery<IInstallableUnit> query = QueryUtil.createIUQuery(id, versionRange);
 
 		MetadataRepository mdr = ((MappedRepository) eContainer()).getMetadataRepository(forceResolve);
 		if(mdr == null || ((EObject) mdr).eIsProxy())
 			return null;
 
		IQueryResult<IInstallableUnit> ius = mdr.query(QueryUtil.createCompoundQuery(
			query, QueryUtil.createLatestIUQuery(), true), new NullProgressMonitor());
 
 		if(ius.isEmpty())
 			// TODO Why this? When does it happen that the latest IU query does not return a result?
 			ius = mdr.query(query, new NullProgressMonitor());
 
 		if(!ius.isEmpty()) {
 			InstallableUnit iu = (InstallableUnit) ius.toArray(IInstallableUnit.class)[0];
 			return iu;
 		}
 		return null;
 	}
 
 	synchronized public void resolveAvailableVersions(boolean updateOnly) {
 
 		if(availableVersions == null) {
 			if(updateOnly)
 				return;
 			availableVersions = new EObjectContainmentEList<AvailableVersion>(
 				AvailableVersion.class, this, AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS);
 		}
 		else
 			availableVersions.clear();
 
 		Map<Version, IMatchExpression<IInstallableUnit>> versionMap = new TreeMap<Version, IMatchExpression<IInstallableUnit>>(
 			Collections.reverseOrder());
 		IQuery<IInstallableUnit> query = QueryUtil.createIUQuery(name);
 
 		while(true) {
 			try {
 				versionMap.clear();
 				for(Resource resource : GeneralUtils.getAggregatorResource(this).getResourceSet().getResources()) {
 
 					MetadataRepository mdr = null;
 					if(resource instanceof MetadataRepositoryResourceImpl)
 						mdr = ((MetadataRepositoryResourceImpl) resource).getMetadataRepository();
 					else if(resource instanceof P2ResourceImpl && resource.getContents().size() == 1)
 						mdr = (MetadataRepository) resource.getContents().get(0);
 
 					if(mdr == null)
 						continue;
 
 					if(StringUtils.trimmedOrNull(name) != null && mdr != null && !((EObject) mdr).eIsProxy()) {
 						IQueryResult<IInstallableUnit> ius = mdr.query(query, null);
 
 						for(IInstallableUnit iu : ius.toSet())
 							versionMap.put(iu.getVersion(), iu.getFilter());
 					}
 				}
 				break;
 			}
 			catch(ConcurrentModificationException e) {
 				// wait a while and try again
 				try {
 					Thread.sleep(100);
 				}
 				catch(InterruptedException e1) {
 					// ignore
 				}
 			}
 			catch(IllegalArgumentException e) {
 				// the aggregator resource is probably temporarily unavailable (e.g. during drag&drop)
 				availableVersions = null;
 				return;
 			}
 		}
 
 		if(versionMap.size() == 0) {
 			AvailableVersion av = AggregatorFactory.eINSTANCE.createAvailableVersion();
 			av.setVersionMatch(VersionMatch.MATCHES);
 			availableVersions.add(av);
 		}
 		else {
 			for(Version version : versionMap.keySet()) {
 				AvailableVersion av = AggregatorFactory.eINSTANCE.createAvailableVersion();
 
 				if(versionRange == null || versionRange.isIncluded(version))
 					av.setVersionMatch(VersionMatch.MATCHES);
 				else {
 					int result = versionRange.getMinimum().compareTo(version);
 
 					if(result >= 0)
 						av.setVersionMatch(VersionMatch.BELOW);
 					else
 						av.setVersionMatch(VersionMatch.ABOVE);
 				}
 				av.setVersion(version);
 				av.setFilter(versionMap.get(version));
 				availableVersions.add(av);
 			}
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public void setAvailableVersionsHeader(AvailableVersionsHeader newAvailableVersionsHeader) {
 		if(newAvailableVersionsHeader != availableVersionsHeader) {
 			NotificationChain msgs = null;
 			if(availableVersionsHeader != null)
 				msgs = ((InternalEObject) availableVersionsHeader).eInverseRemove(
 					this, AggregatorPackage.AVAILABLE_VERSIONS_HEADER__INSTALLABLE_UNIT_REQUEST,
 					AvailableVersionsHeader.class, msgs);
 			if(newAvailableVersionsHeader != null)
 				msgs = ((InternalEObject) newAvailableVersionsHeader).eInverseAdd(
 					this, AggregatorPackage.AVAILABLE_VERSIONS_HEADER__INSTALLABLE_UNIT_REQUEST,
 					AvailableVersionsHeader.class, msgs);
 			msgs = basicSetAvailableVersionsHeader(newAvailableVersionsHeader, msgs);
 			if(msgs != null)
 				msgs.dispatch();
 		}
 		else if(eNotificationRequired())
 			eNotify(new ENotificationImpl(
 				this, Notification.SET, AggregatorPackage.INSTALLABLE_UNIT_REQUEST__AVAILABLE_VERSIONS_HEADER,
 				newAvailableVersionsHeader, newAvailableVersionsHeader));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setDescription(String newDescription) {
 		String oldDescription = description;
 		description = newDescription;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(
 				this, Notification.SET, AggregatorPackage.INSTALLABLE_UNIT_REQUEST__DESCRIPTION, oldDescription,
 				description));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setName(String newName) {
 		String oldName = name;
 		name = newName;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(
 				this, Notification.SET, AggregatorPackage.INSTALLABLE_UNIT_REQUEST__NAME, oldName, name));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setVersionRange(VersionRange newVersionRange) {
 		VersionRange oldVersionRange = versionRange;
 		versionRange = newVersionRange;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(
 				this, Notification.SET, AggregatorPackage.INSTALLABLE_UNIT_REQUEST__VERSION_RANGE, oldVersionRange,
 				versionRange));
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
 		result.append(" (errors: ");
 		result.append(errors);
 		result.append(", warnings: ");
 		result.append(warnings);
 		result.append(", infos: ");
 		result.append(infos);
 		result.append(", description: ");
 		result.append(description);
 		result.append(", name: ");
 		result.append(name);
 		result.append(", versionRange: ");
 		result.append(versionRange);
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
 		return AggregatorPackage.Literals.INSTALLABLE_UNIT_REQUEST;
 	}
 
 } // InstallableUnitReferenceImpl
