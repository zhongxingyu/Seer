 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 package org.eclipse.b3.p2.impl;
 
 //import java.net.URI;
 import java.io.File;
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.Map;
 
 import org.eclipse.b3.p2.ArtifactDescriptor;
 import org.eclipse.b3.p2.ArtifactKey;
 import org.eclipse.b3.p2.ArtifactRepository;
 import org.eclipse.b3.p2.Copyright;
 import org.eclipse.b3.p2.InstallableUnit;
 import org.eclipse.b3.p2.InstallableUnitFragment;
 import org.eclipse.b3.p2.InstallableUnitPatch;
 import org.eclipse.b3.p2.License;
 import org.eclipse.b3.p2.MappingRule;
 import org.eclipse.b3.p2.MetadataRepository;
 import org.eclipse.b3.p2.P2Factory;
 import org.eclipse.b3.p2.P2Package;
 import org.eclipse.b3.p2.ProcessingStepDescriptor;
 import org.eclipse.b3.p2.ProvidedCapability;
 import org.eclipse.b3.p2.RepositoryReference;
 import org.eclipse.b3.p2.RequiredCapability;
 import org.eclipse.b3.p2.Requirement;
 import org.eclipse.b3.p2.RequirementChange;
 import org.eclipse.b3.p2.SimpleArtifactDescriptor;
 import org.eclipse.b3.p2.SimpleArtifactRepository;
 import org.eclipse.b3.p2.TouchpointData;
 import org.eclipse.b3.p2.TouchpointInstruction;
 import org.eclipse.b3.p2.TouchpointType;
 import org.eclipse.b3.p2.UpdateDescriptor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.impl.EFactoryImpl;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.equinox.p2.core.IProvisioningAgent;
 import org.eclipse.equinox.p2.metadata.IArtifactKey;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.IInstallableUnitFragment;
 import org.eclipse.equinox.p2.metadata.ILicense;
 import org.eclipse.equinox.p2.metadata.IProvidedCapability;
 import org.eclipse.equinox.p2.metadata.IRequirement;
 import org.eclipse.equinox.p2.metadata.ITouchpointData;
 import org.eclipse.equinox.p2.metadata.ITouchpointInstruction;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
 import org.eclipse.equinox.p2.query.IQueryResult;
 import org.eclipse.equinox.p2.repository.IRunnableWithProgress;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRequest;
 import org.eclipse.equinox.p2.repository.artifact.IProcessingStepDescriptor;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Factory</b>.
  * <!-- end-user-doc -->
  * 
  * @generated
  */
 public class P2FactoryImpl extends EFactoryImpl implements P2Factory {
 	private static final String MDR_PROXY_URI_FORMATTER = "b3:%s:%s#/";
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @deprecated
 	 * @generated
 	 */
 	@Deprecated
 	public static P2Package getPackage() {
 		return P2Package.eINSTANCE;
 	}
 
 	/**
 	 * Creates the default factory implementation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public static P2Factory init() {
 		try {
 			P2Factory theP2Factory = (P2Factory) EPackage.Registry.INSTANCE.getEFactory("http://www.eclipse.org/b3/2010/p2/1.0.0");
 			if(theP2Factory != null) {
 				return theP2Factory;
 			}
 		}
 		catch(Exception exception) {
 			EcorePlugin.INSTANCE.log(exception);
 		}
 		return new P2FactoryImpl();
 	}
 
 	/**
 	 * Creates an instance of the factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public P2FactoryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertCollectionToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertFileToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(eDataType, instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIArtifactDescriptorArrayToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIArtifactKeyArrayToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIArtifactRequestArrayToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIInstallableUnitArrayToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIInstallableUnitFragmentArrayToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertILicenseArrayToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public String convertIMatchExpressionToString(EDataType eDataType, Object instanceValue) {
 		return instanceValue != null
 				? instanceValue.toString()
 				: null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIProcessingDescriptorArrayToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIProvidedCapabilityArrayToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIProvisioningAgentToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(eDataType, instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIQueryResultToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIRequirementArrayArrayToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIRunnableWithProgressToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(eDataType, instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertIStatusToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(eDataType, instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertITouchpointDataArrayToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertMapToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertOutputStreamToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(eDataType, instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertStringArrayToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public String convertToString(EDataType eDataType, Object instanceValue) {
 		switch(eDataType.getClassifierID()) {
 			case P2Package.COLLECTION:
 				return convertCollectionToString(eDataType, instanceValue);
 			case P2Package.FILE:
 				return convertFileToString(eDataType, instanceValue);
 			case P2Package.IARTIFACT_DESCRIPTOR_ARRAY:
 				return convertIArtifactDescriptorArrayToString(eDataType, instanceValue);
 			case P2Package.IARTIFACT_KEY_ARRAY:
 				return convertIArtifactKeyArrayToString(eDataType, instanceValue);
 			case P2Package.IARTIFACT_REQUEST_ARRAY:
 				return convertIArtifactRequestArrayToString(eDataType, instanceValue);
 			case P2Package.IINSTALLABLE_UNIT_ARRAY:
 				return convertIInstallableUnitArrayToString(eDataType, instanceValue);
 			case P2Package.IINSTALLABLE_UNIT_FRAGMENT_ARRAY:
 				return convertIInstallableUnitFragmentArrayToString(eDataType, instanceValue);
 			case P2Package.ILICENSE_ARRAY:
 				return convertILicenseArrayToString(eDataType, instanceValue);
 			case P2Package.IMATCH_EXPRESSION:
 				return convertIMatchExpressionToString(eDataType, instanceValue);
 			case P2Package.IQUERY_RESULT:
 				return convertIQueryResultToString(eDataType, instanceValue);
 			case P2Package.IPROVIDED_CAPABILITY_ARRAY:
 				return convertIProvidedCapabilityArrayToString(eDataType, instanceValue);
 			case P2Package.IPROVISIONING_AGENT:
 				return convertIProvisioningAgentToString(eDataType, instanceValue);
 			case P2Package.IREQUIREMENT_ARRAY_ARRAY:
 				return convertIRequirementArrayArrayToString(eDataType, instanceValue);
 			case P2Package.IPROCESSING_DESCRIPTOR_ARRAY:
 				return convertIProcessingDescriptorArrayToString(eDataType, instanceValue);
 			case P2Package.IRUNNABLE_WITH_PROGRESS:
 				return convertIRunnableWithProgressToString(eDataType, instanceValue);
 			case P2Package.ISTATUS:
 				return convertIStatusToString(eDataType, instanceValue);
 			case P2Package.ITOUCHPOINT_DATA_ARRAY:
 				return convertITouchpointDataArrayToString(eDataType, instanceValue);
 			case P2Package.MAP:
 				return convertMapToString(eDataType, instanceValue);
 			case P2Package.OUTPUT_STREAM:
 				return convertOutputStreamToString(eDataType, instanceValue);
 			case P2Package.STRING_ARRAY:
 				return convertStringArrayToString(eDataType, instanceValue);
 			case P2Package.UNTYPED_MAP:
 				return convertUntypedMapToString(eDataType, instanceValue);
 			case P2Package.URI:
 				return convertURIToString(eDataType, instanceValue);
 			case P2Package.VERSION:
 				return convertVersionToString(eDataType, instanceValue);
 			case P2Package.VERSION_RANGE:
 				return convertVersionRangeToString(eDataType, instanceValue);
 			default:
 				throw new IllegalArgumentException("The datatype '" + eDataType.getName() +
 						"' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertUntypedMapToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(eDataType, instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertURIToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(eDataType, instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertVersionRangeToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(eDataType, instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String convertVersionToString(EDataType eDataType, Object instanceValue) {
 		return super.convertToString(eDataType, instanceValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public EObject create(EClass eClass) {
 		switch(eClass.getClassifierID()) {
 			case P2Package.ARTIFACT_KEY:
 				return (EObject) createArtifactKey();
 			case P2Package.ARTIFACT_DESCRIPTOR:
 				return (EObject) createArtifactDescriptor();
 			case P2Package.ARTIFACT_REPOSITORY:
 				return (EObject) createArtifactRepository();
 			case P2Package.ARTIFACTS_BY_KEY:
 				return (EObject) createArtifactsByKey();
 			case P2Package.COPYRIGHT:
 				return (EObject) createCopyright();
 			case P2Package.INSTALLABLE_UNIT:
 				return (EObject) createInstallableUnit();
 			case P2Package.INSTALLABLE_UNIT_FRAGMENT:
 				return (EObject) createInstallableUnitFragment();
 			case P2Package.INSTALLABLE_UNIT_PATCH:
 				return (EObject) createInstallableUnitPatch();
 			case P2Package.INSTRUCTION_MAP:
 				return (EObject) createInstructionMap();
 			case P2Package.LICENSE:
 				return (EObject) createLicense();
 			case P2Package.MAPPING_RULE:
 				return (EObject) createMappingRule();
 			case P2Package.METADATA_REPOSITORY:
 				return (EObject) createMetadataRepository();
 			case P2Package.PROCESSING_STEP_DESCRIPTOR:
 				return (EObject) createProcessingStepDescriptor();
 			case P2Package.PROPERTY:
 				return (EObject) createProperty();
 			case P2Package.PROVIDED_CAPABILITY:
 				return (EObject) createProvidedCapability();
 			case P2Package.REPOSITORY_REFERENCE:
 				return (EObject) createRepositoryReference();
 			case P2Package.REQUIRED_CAPABILITY:
 				return (EObject) createRequiredCapability();
 			case P2Package.REQUIREMENT:
 				return (EObject) createRequirement();
 			case P2Package.REQUIREMENT_CHANGE:
 				return (EObject) createRequirementChange();
 			case P2Package.SIMPLE_ARTIFACT_REPOSITORY:
 				return (EObject) createSimpleArtifactRepository();
 			case P2Package.SIMPLE_ARTIFACT_DESCRIPTOR:
 				return (EObject) createSimpleArtifactDescriptor();
 			case P2Package.TOUCHPOINT_DATA:
 				return (EObject) createTouchpointData();
 			case P2Package.TOUCHPOINT_INSTRUCTION:
 				return (EObject) createTouchpointInstruction();
 			case P2Package.TOUCHPOINT_TYPE:
 				return (EObject) createTouchpointType();
 			case P2Package.UPDATE_DESCRIPTOR:
 				return (EObject) createUpdateDescriptor();
 			default:
 				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ArtifactDescriptor createArtifactDescriptor() {
 		ArtifactDescriptorImpl artifactDescriptor = new ArtifactDescriptorImpl();
 		return artifactDescriptor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ArtifactKey createArtifactKey() {
 		ArtifactKeyImpl artifactKey = new ArtifactKeyImpl();
 		return artifactKey;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ArtifactRepository createArtifactRepository() {
 		ArtifactRepositoryImpl artifactRepository = new ArtifactRepositoryImpl();
 		return artifactRepository;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Map.Entry<IArtifactKey, EList<IArtifactDescriptor>> createArtifactsByKey() {
 		ArtifactsByKeyImpl artifactsByKey = new ArtifactsByKeyImpl();
 		return artifactsByKey;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Collection<?> createCollectionFromString(EDataType eDataType, String initialValue) {
 		return (Collection<?>) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Copyright createCopyright() {
 		CopyrightImpl copyright = new CopyrightImpl();
 		return copyright;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public File createFileFromString(EDataType eDataType, String initialValue) {
 		return (File) super.createFromString(eDataType, initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object createFromString(EDataType eDataType, String initialValue) {
 		switch(eDataType.getClassifierID()) {
 			case P2Package.COLLECTION:
 				return createCollectionFromString(eDataType, initialValue);
 			case P2Package.FILE:
 				return createFileFromString(eDataType, initialValue);
 			case P2Package.IARTIFACT_DESCRIPTOR_ARRAY:
 				return createIArtifactDescriptorArrayFromString(eDataType, initialValue);
 			case P2Package.IARTIFACT_KEY_ARRAY:
 				return createIArtifactKeyArrayFromString(eDataType, initialValue);
 			case P2Package.IARTIFACT_REQUEST_ARRAY:
 				return createIArtifactRequestArrayFromString(eDataType, initialValue);
 			case P2Package.IINSTALLABLE_UNIT_ARRAY:
 				return createIInstallableUnitArrayFromString(eDataType, initialValue);
 			case P2Package.IINSTALLABLE_UNIT_FRAGMENT_ARRAY:
 				return createIInstallableUnitFragmentArrayFromString(eDataType, initialValue);
 			case P2Package.ILICENSE_ARRAY:
 				return createILicenseArrayFromString(eDataType, initialValue);
 			case P2Package.IMATCH_EXPRESSION:
 				return createIMatchExpressionFromString(eDataType, initialValue);
 			case P2Package.IQUERY_RESULT:
 				return createIQueryResultFromString(eDataType, initialValue);
 			case P2Package.IPROVIDED_CAPABILITY_ARRAY:
 				return createIProvidedCapabilityArrayFromString(eDataType, initialValue);
 			case P2Package.IPROVISIONING_AGENT:
 				return createIProvisioningAgentFromString(eDataType, initialValue);
 			case P2Package.IREQUIREMENT_ARRAY_ARRAY:
 				return createIRequirementArrayArrayFromString(eDataType, initialValue);
 			case P2Package.IPROCESSING_DESCRIPTOR_ARRAY:
 				return createIProcessingDescriptorArrayFromString(eDataType, initialValue);
 			case P2Package.IRUNNABLE_WITH_PROGRESS:
 				return createIRunnableWithProgressFromString(eDataType, initialValue);
 			case P2Package.ISTATUS:
 				return createIStatusFromString(eDataType, initialValue);
 			case P2Package.ITOUCHPOINT_DATA_ARRAY:
 				return createITouchpointDataArrayFromString(eDataType, initialValue);
 			case P2Package.MAP:
 				return createMapFromString(eDataType, initialValue);
 			case P2Package.OUTPUT_STREAM:
 				return createOutputStreamFromString(eDataType, initialValue);
 			case P2Package.STRING_ARRAY:
 				return createStringArrayFromString(eDataType, initialValue);
 			case P2Package.UNTYPED_MAP:
 				return createUntypedMapFromString(eDataType, initialValue);
 			case P2Package.URI:
 				return createURIFromString(eDataType, initialValue);
 			case P2Package.VERSION:
 				return createVersionFromString(eDataType, initialValue);
 			case P2Package.VERSION_RANGE:
 				return createVersionRangeFromString(eDataType, initialValue);
 			default:
 				throw new IllegalArgumentException("The datatype '" + eDataType.getName() +
 						"' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IArtifactDescriptor[] createIArtifactDescriptorArrayFromString(EDataType eDataType, String initialValue) {
 		return (IArtifactDescriptor[]) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IArtifactKey[] createIArtifactKeyArrayFromString(EDataType eDataType, String initialValue) {
 		return (IArtifactKey[]) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IArtifactRequest[] createIArtifactRequestArrayFromString(EDataType eDataType, String initialValue) {
 		return (IArtifactRequest[]) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IInstallableUnit[] createIInstallableUnitArrayFromString(EDataType eDataType, String initialValue) {
 		return (IInstallableUnit[]) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IInstallableUnitFragment[] createIInstallableUnitFragmentArrayFromString(EDataType eDataType,
 			String initialValue) {
 		return (IInstallableUnitFragment[]) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ILicense[] createILicenseArrayFromString(EDataType eDataType, String initialValue) {
 		return (ILicense[]) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public IMatchExpression<?> createIMatchExpressionFromString(EDataType eDataType, String initialValue) {
 		throw new UnsupportedOperationException();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public InstallableUnit createInstallableUnit() {
 		InstallableUnitImpl installableUnit = new InstallableUnitImpl();
 		return installableUnit;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public InstallableUnitFragment createInstallableUnitFragment() {
 		InstallableUnitFragmentImpl installableUnitFragment = new InstallableUnitFragmentImpl();
 		return installableUnitFragment;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public InstallableUnitPatch createInstallableUnitPatch() {
 		InstallableUnitPatchImpl installableUnitPatch = new InstallableUnitPatchImpl();
 		return installableUnitPatch;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Map.Entry<String, ITouchpointInstruction> createInstructionMap() {
 		InstructionMapImpl instructionMap = new InstructionMapImpl();
 		return instructionMap;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IProcessingStepDescriptor[] createIProcessingDescriptorArrayFromString(EDataType eDataType,
 			String initialValue) {
 		return (IProcessingStepDescriptor[]) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IProvidedCapability[] createIProvidedCapabilityArrayFromString(EDataType eDataType, String initialValue) {
 		return (IProvidedCapability[]) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IProvisioningAgent createIProvisioningAgentFromString(EDataType eDataType, String initialValue) {
 		return (IProvisioningAgent) super.createFromString(eDataType, initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IQueryResult<?> createIQueryResultFromString(EDataType eDataType, String initialValue) {
 		return (IQueryResult<?>) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IRequirement[][] createIRequirementArrayArrayFromString(EDataType eDataType, String initialValue) {
 		return (IRequirement[][]) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IRunnableWithProgress createIRunnableWithProgressFromString(EDataType eDataType, String initialValue) {
 		return (IRunnableWithProgress) super.createFromString(eDataType, initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public IStatus createIStatusFromString(EDataType eDataType, String initialValue) {
 		return (IStatus) super.createFromString(eDataType, initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ITouchpointData[] createITouchpointDataArrayFromString(EDataType eDataType, String initialValue) {
 		return (ITouchpointData[]) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public License createLicense() {
 		LicenseImpl license = new LicenseImpl();
 		return license;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Map<?, ?> createMapFromString(EDataType eDataType, String initialValue) {
 		return (Map<?, ?>) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public MappingRule createMappingRule() {
 		MappingRuleImpl mappingRule = new MappingRuleImpl();
 		return mappingRule;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public MetadataRepository createMetadataRepository() {
 		MetadataRepositoryImpl metadataRepository = new MetadataRepositoryImpl();
 		return metadataRepository;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public MetadataRepository createMetadataRepositoryProxy(String repoNature, String repoLocation) {
 		MetadataRepositoryImpl mdr = (MetadataRepositoryImpl) createMetadataRepository();
 		URI proxyURI = URI.createURI(String.format(MDR_PROXY_URI_FORMATTER, repoNature, repoLocation));
 		mdr.eSetProxyURI(proxyURI);
 
 		return mdr;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public OutputStream createOutputStreamFromString(EDataType eDataType, String initialValue) {
 		return (OutputStream) super.createFromString(eDataType, initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ProcessingStepDescriptor createProcessingStepDescriptor() {
 		ProcessingStepDescriptorImpl processingStepDescriptor = new ProcessingStepDescriptorImpl();
 		return processingStepDescriptor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Map.Entry<String, String> createProperty() {
 		PropertyImpl property = new PropertyImpl();
 		return property;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public ProvidedCapability createProvidedCapability() {
 		ProvidedCapabilityImpl providedCapability = new ProvidedCapabilityImpl();
 		return providedCapability;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public RepositoryReference createRepositoryReference() {
 		RepositoryReferenceImpl repositoryReference = new RepositoryReferenceImpl();
 		return repositoryReference;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public RequiredCapability createRequiredCapability() {
 		RequiredCapabilityImpl requiredCapability = new RequiredCapabilityImpl();
 		return requiredCapability;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public Requirement createRequirement() {
 		RequirementImpl requirement = new RequirementImpl();
 		return requirement;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public RequirementChange createRequirementChange() {
 		RequirementChangeImpl requirementChange = new RequirementChangeImpl();
 		return requirementChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public SimpleArtifactDescriptor createSimpleArtifactDescriptor() {
 		SimpleArtifactDescriptorImpl simpleArtifactDescriptor = new SimpleArtifactDescriptorImpl();
 		return simpleArtifactDescriptor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public SimpleArtifactRepository createSimpleArtifactRepository() {
 		SimpleArtifactRepositoryImpl simpleArtifactRepository = new SimpleArtifactRepositoryImpl();
 		return simpleArtifactRepository;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String[] createStringArrayFromString(EDataType eDataType, String initialValue) {
 		return (String[]) super.createFromString(initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public TouchpointData createTouchpointData() {
 		TouchpointDataImpl touchpointData = new TouchpointDataImpl();
 		return touchpointData;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public TouchpointInstruction createTouchpointInstruction() {
 		TouchpointInstructionImpl touchpointInstruction = new TouchpointInstructionImpl();
 		return touchpointInstruction;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public TouchpointType createTouchpointType() {
 		TouchpointTypeImpl touchpointType = new TouchpointTypeImpl();
 		return touchpointType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated NOT (because of the warning annotation)
 	 */
 	@SuppressWarnings("rawtypes")
 	public Map createUntypedMapFromString(EDataType eDataType, String initialValue) {
 		return (Map) super.createFromString(eDataType, initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public UpdateDescriptor createUpdateDescriptor() {
 		UpdateDescriptorImpl updateDescriptor = new UpdateDescriptorImpl();
 		return updateDescriptor;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
	 * @generated
 	 */
	public URI createURIFromString(EDataType eDataType, String initialValue) {
		return (URI) super.createFromString(eDataType, initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
	 * @generated
 	 */
 	public Version createVersionFromString(EDataType eDataType, String initialValue) {
		return (Version) super.createFromString(eDataType, initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public VersionRange createVersionRangeFromString(EDataType eDataType, String initialValue) {
 		return (VersionRange) super.createFromString(eDataType, initialValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public P2Package getP2Package() {
 		return (P2Package) getEPackage();
 	}
 
 } // P2FactoryImpl
