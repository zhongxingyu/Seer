 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.aggregator.impl;
 
 import java.net.URI;
 
 import org.eclipse.b3.aggregator.AggregateType;
 import org.eclipse.b3.aggregator.Aggregator;
 import org.eclipse.b3.aggregator.AggregatorFactory;
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.Architecture;
 import org.eclipse.b3.aggregator.Bundle;
 import org.eclipse.b3.aggregator.Category;
 import org.eclipse.b3.aggregator.Configuration;
 import org.eclipse.b3.aggregator.Contact;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.CustomCategory;
 import org.eclipse.b3.aggregator.DescriptionProvider;
 import org.eclipse.b3.aggregator.ExclusionRule;
 import org.eclipse.b3.aggregator.Feature;
 import org.eclipse.b3.aggregator.InfosProvider;
 import org.eclipse.b3.aggregator.InstallableUnitType;
 import org.eclipse.b3.aggregator.MapRule;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.MappedUnit;
 import org.eclipse.b3.aggregator.MavenItem;
 import org.eclipse.b3.aggregator.MavenMapping;
 import org.eclipse.b3.aggregator.MetadataRepositoryReference;
 import org.eclipse.b3.aggregator.OperatingSystem;
 import org.eclipse.b3.aggregator.PackedStrategy;
 import org.eclipse.b3.aggregator.Product;
 import org.eclipse.b3.aggregator.Property;
 import org.eclipse.b3.aggregator.Status;
 import org.eclipse.b3.aggregator.StatusCode;
 import org.eclipse.b3.aggregator.ValidConfigurationsRule;
 import org.eclipse.b3.aggregator.WindowSystem;
 import org.eclipse.b3.aggregator.p2.InstallableUnit;
 import org.eclipse.b3.aggregator.p2.MetadataRepository;
 import org.eclipse.b3.aggregator.util.InstallableUnitUtils;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.impl.EFactoryImpl;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.equinox.internal.provisional.p2.metadata.Version;
 import org.eclipse.equinox.internal.provisional.p2.metadata.VersionRange;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
  * 
  * @generated
  */
 public class AggregatorFactoryImpl extends EFactoryImpl implements AggregatorFactory {
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @deprecated
 	 * @generated
 	 */
 	@Deprecated
 	public static AggregatorPackage getPackage() {
 		return AggregatorPackage.eINSTANCE;
 	}
 
 	/**
 	 * Creates the default factory implementation.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public static AggregatorFactory init() {
 		try {
 			AggregatorFactory theAggregatorFactory = (AggregatorFactory) EPackage.Registry.INSTANCE.getEFactory("http://www.eclipse.org/b3/2010/aggregator/1.0.0");
 			if(theAggregatorFactory != null) {
 				return theAggregatorFactory;
 			}
 		}
 		catch(Exception exception) {
 			EcorePlugin.INSTANCE.log(exception);
 		}
 		return new AggregatorFactoryImpl();
 	}
 
 	/**
 	 * Creates an instance of the factory.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public AggregatorFactoryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertAggregateTypeToString(EDataType eDataType, Object instanceValue) {
 		return instanceValue == null
 				? null
 				: instanceValue.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertArchitectureToString(EDataType eDataType, Object instanceValue) {
 		return instanceValue == null
 				? null
 				: instanceValue.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertInstallableUnitTypeToString(EDataType eDataType, Object instanceValue) {
 		return instanceValue == null
 				? null
 				: instanceValue.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertOperatingSystemToString(EDataType eDataType, Object instanceValue) {
 		return instanceValue == null
 				? null
 				: instanceValue.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertPackedStrategyToString(EDataType eDataType, Object instanceValue) {
 		return instanceValue == null
 				? null
 				: instanceValue.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertStatusCodeToString(EDataType eDataType, Object instanceValue) {
 		return instanceValue == null
 				? null
 				: instanceValue.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public String convertToString(EDataType eDataType, Object instanceValue) {
 		switch(eDataType.getClassifierID()) {
 		case AggregatorPackage.AGGREGATE_TYPE:
 			return convertAggregateTypeToString(eDataType, instanceValue);
 		case AggregatorPackage.OPERATING_SYSTEM:
 			return convertOperatingSystemToString(eDataType, instanceValue);
 		case AggregatorPackage.WINDOW_SYSTEM:
 			return convertWindowSystemToString(eDataType, instanceValue);
 		case AggregatorPackage.ARCHITECTURE:
 			return convertArchitectureToString(eDataType, instanceValue);
 		case AggregatorPackage.PACKED_STRATEGY:
 			return convertPackedStrategyToString(eDataType, instanceValue);
 		case AggregatorPackage.INSTALLABLE_UNIT_TYPE:
 			return convertInstallableUnitTypeToString(eDataType, instanceValue);
 		case AggregatorPackage.STATUS_CODE:
 			return convertStatusCodeToString(eDataType, instanceValue);
 		case AggregatorPackage.URI:
 			return convertURIToString(eDataType, instanceValue);
 		default:
 			throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertURIToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(eDataType, instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertWindowSystemToString(EDataType eDataType, Object instanceValue) {
 		return instanceValue == null
 				? null
 				: instanceValue.toString();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public EObject create(EClass eClass) {
 		switch(eClass.getClassifierID()) {
 		case AggregatorPackage.AGGREGATOR:
 			return (EObject) createAggregator();
 		case AggregatorPackage.MAPPED_REPOSITORY:
 			return (EObject) createMappedRepository();
 		case AggregatorPackage.CONFIGURATION:
 			return (EObject) createConfiguration();
 		case AggregatorPackage.CONTRIBUTION:
 			return (EObject) createContribution();
 		case AggregatorPackage.CONTACT:
 			return (EObject) createContact();
 		case AggregatorPackage.FEATURE:
 			return (EObject) createFeature();
 		case AggregatorPackage.BUNDLE:
 			return (EObject) createBundle();
 		case AggregatorPackage.PRODUCT:
 			return (EObject) createProduct();
 		case AggregatorPackage.PROPERTY:
 			return (EObject) createProperty();
 		case AggregatorPackage.CATEGORY:
 			return (EObject) createCategory();
 		case AggregatorPackage.CUSTOM_CATEGORY:
 			return (EObject) createCustomCategory();
 		case AggregatorPackage.EXCLUSION_RULE:
 			return (EObject) createExclusionRule();
 		case AggregatorPackage.VALID_CONFIGURATIONS_RULE:
 			return (EObject) createValidConfigurationsRule();
 		case AggregatorPackage.METADATA_REPOSITORY_REFERENCE:
 			return (EObject) createMetadataRepositoryReference();
 		case AggregatorPackage.DESCRIPTION_PROVIDER:
 			return (EObject) createDescriptionProvider();
 		case AggregatorPackage.MAVEN_MAPPING:
 			return (EObject) createMavenMapping();
 		case AggregatorPackage.MAVEN_ITEM:
 			return (EObject) createMavenItem();
 		case AggregatorPackage.STATUS:
 			return (EObject) createStatus();
 		case AggregatorPackage.INFOS_PROVIDER:
 			return (EObject) createInfosProvider();
 		default:
 			throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public AggregateType createAggregateTypeFromString(EDataType eDataType, String initialValue) {
 		AggregateType result = AggregateType.get(initialValue);
 		if(result == null)
 			throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '"
 					+ eDataType.getName() + "'");
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Aggregator createAggregator() {
 		AggregatorImpl aggregator = new AggregatorImpl();
 		return aggregator;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Architecture createArchitectureFromString(EDataType eDataType, String initialValue) {
 		Architecture result = Architecture.get(initialValue);
 		if(result == null)
 			throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '"
 					+ eDataType.getName() + "'");
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Bundle createBundle() {
 		BundleImpl bundle = new BundleImpl();
 		return bundle;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Category createCategory() {
 		CategoryImpl category = new CategoryImpl();
 		return category;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Configuration createConfiguration() {
 		ConfigurationImpl configuration = new ConfigurationImpl();
 		return configuration;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Contact createContact() {
 		ContactImpl contact = new ContactImpl();
 		return contact;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Contribution createContribution() {
 		ContributionImpl contribution = new ContributionImpl();
 		return contribution;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public CustomCategory createCustomCategory() {
 		CustomCategoryImpl customCategory = new CustomCategoryImpl();
 		return customCategory;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public DescriptionProvider createDescriptionProvider() {
 		DescriptionProviderImpl descriptionProvider = new DescriptionProviderImpl();
 		return descriptionProvider;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ExclusionRule createExclusionRule() {
 		ExclusionRuleImpl exclusionRule = new ExclusionRuleImpl();
 		return exclusionRule;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Feature createFeature() {
 		FeatureImpl feature = new FeatureImpl();
 		return feature;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object createFromString(EDataType eDataType, String initialValue) {
 		switch(eDataType.getClassifierID()) {
 		case AggregatorPackage.AGGREGATE_TYPE:
 			return createAggregateTypeFromString(eDataType, initialValue);
 		case AggregatorPackage.OPERATING_SYSTEM:
 			return createOperatingSystemFromString(eDataType, initialValue);
 		case AggregatorPackage.WINDOW_SYSTEM:
 			return createWindowSystemFromString(eDataType, initialValue);
 		case AggregatorPackage.ARCHITECTURE:
 			return createArchitectureFromString(eDataType, initialValue);
 		case AggregatorPackage.PACKED_STRATEGY:
 			return createPackedStrategyFromString(eDataType, initialValue);
 		case AggregatorPackage.INSTALLABLE_UNIT_TYPE:
 			return createInstallableUnitTypeFromString(eDataType, initialValue);
 		case AggregatorPackage.STATUS_CODE:
 			return createStatusCodeFromString(eDataType, initialValue);
 		case AggregatorPackage.URI:
 			return createURIFromString(eDataType, initialValue);
 		default:
 			throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public InfosProvider createInfosProvider() {
 		InfosProviderImpl infosProvider = new InfosProviderImpl();
 		return infosProvider;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public InstallableUnitType createInstallableUnitTypeFromString(EDataType eDataType, String initialValue) {
 		InstallableUnitType result = InstallableUnitType.get(initialValue);
 		if(result == null)
 			throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '"
 					+ eDataType.getName() + "'");
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public MappedRepository createMappedRepository() {
 		MappedRepositoryImpl mappedRepository = new MappedRepositoryImpl();
 		return mappedRepository;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public MappedRepository createMappedRepository(MetadataRepository mdr) {
 		MappedRepository mappedRepo = createMappedRepository();
 		mappedRepo.setMetadataRepository(mdr);
 		mappedRepo.setLocation(mdr.getLocation().toString());
 
 		return mappedRepo;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public MappedUnit createMappedUnit(InstallableUnit iu) {
 		MappedUnit mu = null;
 
 		switch(InstallableUnitUtils.getType(iu)) {
 		case FEATURE:
 			mu = createFeature();
 			break;
 		case CATEGORY:
 			mu = createCategory();
 			break;
 		case BUNDLE:
 		case FRAGMENT:
 			mu = createBundle();
 			break;
 		case PRODUCT:
 			mu = createProduct();
 			break;
 		default:
 			throw new IllegalArgumentException("Unknown IU type");
 		}
 
 		mu.setName(iu.getId());
		mu.setVersionRange(new VersionRange(iu.getVersion(), true, Version.MAX_VERSION, true));
 
 		return mu;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public MapRule createMapRule(InstallableUnit iu, Class<? extends MapRule> ruleClass) {
 		MapRule mr = null;
 
 		if(ruleClass.isAssignableFrom(ExclusionRule.class))
 			mr = createExclusionRule();
 		else if(ruleClass.isAssignableFrom(ValidConfigurationsRule.class))
 			mr = createValidConfigurationsRule();
 		else
 			throw new IllegalArgumentException("Unknown rule class " + ruleClass);
 
 		mr.setName(iu.getId());
 		mr.setVersionRange(new VersionRange(iu.getVersion(), true, Version.MAX_VERSION, false));
 
 		return mr;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public MavenItem createMavenItem() {
 		MavenItemImpl mavenItem = new MavenItemImpl();
 		return mavenItem;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public MavenMapping createMavenMapping() {
 		MavenMappingImpl mavenMapping = new MavenMappingImpl();
 		return mavenMapping;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public MetadataRepositoryReference createMetadataRepositoryReference() {
 		MetadataRepositoryReferenceImpl metadataRepositoryReference = new MetadataRepositoryReferenceImpl();
 		return metadataRepositoryReference;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public OperatingSystem createOperatingSystemFromString(EDataType eDataType, String initialValue) {
 		OperatingSystem result = OperatingSystem.get(initialValue);
 		if(result == null)
 			throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '"
 					+ eDataType.getName() + "'");
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public PackedStrategy createPackedStrategyFromString(EDataType eDataType, String initialValue) {
 		PackedStrategy result = PackedStrategy.get(initialValue);
 		if(result == null)
 			throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '"
 					+ eDataType.getName() + "'");
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Product createProduct() {
 		ProductImpl product = new ProductImpl();
 		return product;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Property createProperty() {
 		PropertyImpl property = new PropertyImpl();
 		return property;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public Property createProperty(String key, String value) {
 		PropertyImpl property = new PropertyImpl(key, value);
 		return property;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Status createStatus() {
 		StatusImpl status = new StatusImpl();
 		return status;
 	}
 
 	public Status createStatus(StatusCode statusCode) {
 		return StatusImpl.createStatus(statusCode);
 	}
 
 	public Status createStatus(StatusCode statusCode, String message) {
 		return StatusImpl.createStatus(statusCode, message);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public StatusCode createStatusCodeFromString(EDataType eDataType, String initialValue) {
 		StatusCode result = StatusCode.get(initialValue);
 		if(result == null)
 			throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '"
 					+ eDataType.getName() + "'");
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public URI createURIFromString(EDataType eDataType, String initialValue) {
 		return (URI) super.createFromString(eDataType, initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ValidConfigurationsRule createValidConfigurationsRule() {
 		ValidConfigurationsRuleImpl validConfigurationsRule = new ValidConfigurationsRuleImpl();
 		return validConfigurationsRule;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public WindowSystem createWindowSystemFromString(EDataType eDataType, String initialValue) {
 		WindowSystem result = WindowSystem.get(initialValue);
 		if(result == null)
 			throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '"
 					+ eDataType.getName() + "'");
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public AggregatorPackage getAggregatorPackage() {
 		return (AggregatorPackage) getEPackage();
 	}
 
 } // AggregatorFactoryImpl
