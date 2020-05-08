 /*******************************************************************************
  * This file is protected by Copyright. 
  * Please refer to the COPYRIGHT file distributed with this source distribution.
  *
  * This file is part of REDHAWK IDE.
  *
  * All rights reserved.  This program and the accompanying materials are made available under 
  * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
  // BEGIN GENERATED CODE
 package mil.jpeojtrs.sca.partitioning.impl;
 
 import mil.jpeojtrs.sca.partitioning.ComponentFile;
 import mil.jpeojtrs.sca.partitioning.ComponentFileRef;
 import mil.jpeojtrs.sca.partitioning.ComponentFiles;
 import mil.jpeojtrs.sca.partitioning.ComponentInstantiation;
 import mil.jpeojtrs.sca.partitioning.ComponentInstantiationRef;
 import mil.jpeojtrs.sca.partitioning.ComponentPlacement;
 import mil.jpeojtrs.sca.partitioning.ComponentProperties;
 import mil.jpeojtrs.sca.partitioning.ComponentSupportedInterface;
 import mil.jpeojtrs.sca.partitioning.ComponentSupportedInterfaceStub;
 import mil.jpeojtrs.sca.partitioning.ConnectInterface;
 import mil.jpeojtrs.sca.partitioning.ConnectionTarget;
 import mil.jpeojtrs.sca.partitioning.Connections;
 import mil.jpeojtrs.sca.partitioning.DevComponentFile;
 import mil.jpeojtrs.sca.partitioning.DeviceThatLoadedThisComponentRef;
 import mil.jpeojtrs.sca.partitioning.DeviceUsedByThisComponentRef;
 import mil.jpeojtrs.sca.partitioning.DomComponentFile;
 import mil.jpeojtrs.sca.partitioning.DomainFinder;
 import mil.jpeojtrs.sca.partitioning.DomainFinderType;
 import mil.jpeojtrs.sca.partitioning.FindBy;
 import mil.jpeojtrs.sca.partitioning.FindByStub;
 import mil.jpeojtrs.sca.partitioning.FindByStubContainer;
 import mil.jpeojtrs.sca.partitioning.LocalFile;
 import mil.jpeojtrs.sca.partitioning.NamingService;
 import mil.jpeojtrs.sca.partitioning.Partitioning;
 import mil.jpeojtrs.sca.partitioning.PartitioningFactory;
 import mil.jpeojtrs.sca.partitioning.PartitioningPackage;
 import mil.jpeojtrs.sca.partitioning.ProvidesPort;
 import mil.jpeojtrs.sca.partitioning.ProvidesPortStub;
 import mil.jpeojtrs.sca.partitioning.UsesPort;
 import mil.jpeojtrs.sca.partitioning.UsesPortStub;
 import mil.jpeojtrs.sca.partitioning.util.PartitioningValidator;
 import mil.jpeojtrs.sca.prf.PrfPackage;
 import mil.jpeojtrs.sca.scd.ScdPackage;
 import mil.jpeojtrs.sca.spd.SpdPackage;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EGenericType;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.ETypeParameter;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.impl.EPackageImpl;
 import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Package</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class PartitioningPackageImpl extends EPackageImpl implements PartitioningPackage {
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass componentFileEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass domComponentFileEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass devComponentFileEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass componentFileRefEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass componentFilesEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass componentInstantiationEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass componentInstantiationRefEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass componentPlacementEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass componentPropertiesEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass componentSupportedInterfaceEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass componentSupportedInterfaceStubEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass connectInterfaceEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass connectionsEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass connectionTargetEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass deviceThatLoadedThisComponentRefEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass deviceUsedByThisComponentRefEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass domainFinderEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass findByEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass findByStubEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass findByStubContainerEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass localFileEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass namingServiceEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass partitioningEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass providesPortEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass providesPortStubEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass usesPortEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass usesPortStubEClass = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum domainFinderTypeEEnum = null;
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EDataType domainFinderTypeObjectEDataType = null;
 
 	/**
 	 * Creates an instance of the model <b>Package</b>, registered with
 	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
 	 * package URI value.
 	 * <p>Note: the correct way to create the package is via the static
 	 * factory method {@link #init init()}, which also performs
 	 * initialization of the package, or returns the registered package,
 	 * if one already exists.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.ecore.EPackage.Registry
 	 * @see mil.jpeojtrs.sca.partitioning.PartitioningPackage#eNS_URI
 	 * @see #init()
 	 * @generated
 	 */
 	private PartitioningPackageImpl() {
 		super(eNS_URI, PartitioningFactory.eINSTANCE);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private static boolean isInited = false;
 
 	/**
 	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
 	 * 
 	 * <p>This method is used to initialize {@link PartitioningPackage#eINSTANCE} when that field is accessed.
 	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #eNS_URI
 	 * @see #createPackageContents()
 	 * @see #initializePackageContents()
 	 * @generated
 	 */
 	public static PartitioningPackage init() {
 		if (isInited) return (PartitioningPackage)EPackage.Registry.INSTANCE.getEPackage(PartitioningPackage.eNS_URI);
 
 		// Obtain or create and register package
 		PartitioningPackageImpl thePartitioningPackage = (PartitioningPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof PartitioningPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new PartitioningPackageImpl());
 
 		isInited = true;
 
 		// Initialize simple dependencies
 		SpdPackage.eINSTANCE.eClass();
 		XMLTypePackage.eINSTANCE.eClass();
 
 		// Create package meta-data objects
 		thePartitioningPackage.createPackageContents();
 
 		// Initialize created meta-data
 		thePartitioningPackage.initializePackageContents();
 
 		// Register package validator
 		EValidator.Registry.INSTANCE.put
 			(thePartitioningPackage, 
 			 new EValidator.Descriptor() {
 				 public EValidator getEValidator() {
 					 return PartitioningValidator.INSTANCE;
 				 }
 			 });
 
 		// Mark meta-data to indicate it can't be changed
 		thePartitioningPackage.freeze();
 
   
 		// Update the registry and return the package
 		EPackage.Registry.INSTANCE.put(PartitioningPackage.eNS_URI, thePartitioningPackage);
 		return thePartitioningPackage;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getComponentFile() {
 		return componentFileEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentFile_LocalFile() {
 		return (EReference)componentFileEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getComponentFile_Id() {
 		return (EAttribute)componentFileEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentFile_SoftPkg() {
 		return (EReference)componentFileEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getComponentFile_Type() {
 		return (EAttribute)componentFileEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDomComponentFile() {
 		return domComponentFileEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDevComponentFile() {
 		return devComponentFileEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getComponentFileRef() {
 		return componentFileRefEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentFileRef_File() {
 		return (EReference)componentFileRefEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getComponentFileRef_Refid() {
 		return (EAttribute)componentFileRefEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getComponentFiles() {
 		return componentFilesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentFiles_ComponentFile() {
 		return (EReference)componentFilesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getComponentInstantiation() {
 		return componentInstantiationEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getComponentInstantiation_UsageName() {
 		return (EAttribute)componentInstantiationEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentInstantiation_ComponentProperties() {
 		return (EReference)componentInstantiationEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentInstantiation_Provides() {
 		return (EReference)componentInstantiationEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentInstantiation_Uses() {
 		return (EReference)componentInstantiationEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentInstantiation_InterfaceStub() {
 		return (EReference)componentInstantiationEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getComponentInstantiation_Id() {
 		return (EAttribute)componentInstantiationEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentInstantiation_Placement() {
 		return (EReference)componentInstantiationEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getComponentInstantiation_ImplID() {
 		return (EAttribute)componentInstantiationEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getComponentInstantiationRef() {
 		return componentInstantiationRefEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentInstantiationRef_Instantiation() {
 		return (EReference)componentInstantiationRefEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getComponentInstantiationRef_Refid() {
 		return (EAttribute)componentInstantiationRefEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getComponentPlacement() {
 		return componentPlacementEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentPlacement_ComponentFileRef() {
 		return (EReference)componentPlacementEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentPlacement_ComponentInstantiation() {
 		return (EReference)componentPlacementEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getComponentPlacement_Name() {
 		return (EAttribute)componentPlacementEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getComponentProperties() {
 		return componentPropertiesEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getComponentProperties_Properties() {
 		return (EAttribute)componentPropertiesEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentProperties_SimpleRef() {
 		return (EReference)componentPropertiesEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentProperties_SimpleSequenceRef() {
 		return (EReference)componentPropertiesEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentProperties_StructRef() {
 		return (EReference)componentPropertiesEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentProperties_StructSequenceRef() {
 		return (EReference)componentPropertiesEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getComponentSupportedInterface() {
 		return componentSupportedInterfaceEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getComponentSupportedInterface_SupportedIdentifier() {
 		return (EAttribute)componentSupportedInterfaceEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentSupportedInterface_ComponentInstantiationRef() {
 		return (EReference)componentSupportedInterfaceEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentSupportedInterface_FindBy() {
 		return (EReference)componentSupportedInterfaceEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getComponentSupportedInterface_Interface() {
 		return (EReference)componentSupportedInterfaceEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getComponentSupportedInterfaceStub() {
 		return componentSupportedInterfaceStubEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getConnectInterface() {
 		return connectInterfaceEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConnectInterface_UsesPort() {
 		return (EReference)connectInterfaceEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConnectInterface_ProvidesPort() {
 		return (EReference)connectInterfaceEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConnectInterface_ComponentSupportedInterface() {
 		return (EReference)connectInterfaceEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConnectInterface_FindBy() {
 		return (EReference)connectInterfaceEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getConnectInterface_Id() {
 		return (EAttribute)connectInterfaceEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConnectInterface_Source() {
 		return (EReference)connectInterfaceEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConnectInterface_Target() {
 		return (EReference)connectInterfaceEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getConnections() {
 		return connectionsEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getConnections_ConnectInterface() {
 		return (EReference)connectionsEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getConnectionTarget() {
 		return connectionTargetEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDeviceThatLoadedThisComponentRef() {
 		return deviceThatLoadedThisComponentRefEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDeviceThatLoadedThisComponentRef_Refid() {
 		return (EAttribute)deviceThatLoadedThisComponentRefEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDeviceUsedByThisComponentRef() {
 		return deviceUsedByThisComponentRefEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDeviceUsedByThisComponentRef_RefId() {
 		return (EAttribute)deviceUsedByThisComponentRefEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDeviceUsedByThisComponentRef_UsesRefId() {
 		return (EAttribute)deviceUsedByThisComponentRefEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getDomainFinder() {
 		return domainFinderEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDomainFinder_Name() {
 		return (EAttribute)domainFinderEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getDomainFinder_Type() {
 		return (EAttribute)domainFinderEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFindBy() {
 		return findByEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFindBy_NamingService() {
 		return (EReference)findByEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFindBy_DomainFinder() {
 		return (EReference)findByEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFindByStub() {
 		return findByStubEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFindByStub_NamingService() {
 		return (EReference)findByStubEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFindByStub_DomainFinder() {
 		return (EReference)findByStubEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFindByStub_Provides() {
 		return (EReference)findByStubEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFindByStub_Uses() {
 		return (EReference)findByStubEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFindByStub_Interface() {
 		return (EReference)findByStubEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFindByStubContainer() {
 		return findByStubContainerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFindByStubContainer_Stubs() {
 		return (EReference)findByStubContainerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLocalFile() {
 		return localFileEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getLocalFile_Name() {
 		return (EAttribute)localFileEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getNamingService() {
 		return namingServiceEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getNamingService_Name() {
 		return (EAttribute)namingServiceEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getPartitioning() {
 		return partitioningEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getPartitioning_Parts() {
 		return (EAttribute)partitioningEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getPartitioning_ComponentPlacement() {
 		return (EReference)partitioningEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProvidesPort() {
 		return providesPortEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProvidesPort_ProvidesIdentifier() {
 		return (EAttribute)providesPortEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProvidesPort_ComponentInstantiationRef() {
 		return (EReference)providesPortEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProvidesPort_DeviceThatLoadedThisComponentRef() {
 		return (EReference)providesPortEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProvidesPort_DeviceUsedByThisComponentRef() {
 		return (EReference)providesPortEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProvidesPort_FindBy() {
 		return (EReference)providesPortEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getProvidesPortStub() {
 		return providesPortStubEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getProvidesPortStub_Name() {
 		return (EAttribute)providesPortStubEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getProvidesPortStub_Provides() {
 		return (EReference)providesPortStubEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getUsesPort() {
 		return usesPortEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getUsesPort_UsesIndentifier() {
 		return (EAttribute)usesPortEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getUsesPort_ComponentInstantiationRef() {
 		return (EReference)usesPortEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getUsesPort_DeviceThatLoadedThisComponentRef() {
 		return (EReference)usesPortEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getUsesPort_DeviceUsedByThisComponentRef() {
 		return (EReference)usesPortEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getUsesPort_FindBy() {
 		return (EReference)usesPortEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getUsesPortStub() {
 		return usesPortStubEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getUsesPortStub_Name() {
 		return (EAttribute)usesPortStubEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getUsesPortStub_Uses() {
 		return (EReference)usesPortStubEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getUsesPortStub_SupportedIdentifier() {
 		return (EAttribute)usesPortStubEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getDomainFinderType() {
 		return domainFinderTypeEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EDataType getDomainFinderTypeObject() {
 		return domainFinderTypeObjectEDataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PartitioningFactory getPartitioningFactory() {
 		return (PartitioningFactory)getEFactoryInstance();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isCreated = false;
 
 	/**
 	 * Creates the meta-model objects for the package.  This method is
 	 * guarded to have no affect on any invocation but its first.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void createPackageContents() {
 		if (isCreated) return;
 		isCreated = true;
 
 		// Create classes and their features
 		componentFileEClass = createEClass(COMPONENT_FILE);
 		createEReference(componentFileEClass, COMPONENT_FILE__LOCAL_FILE);
 		createEAttribute(componentFileEClass, COMPONENT_FILE__ID);
 		createEReference(componentFileEClass, COMPONENT_FILE__SOFT_PKG);
 		createEAttribute(componentFileEClass, COMPONENT_FILE__TYPE);
 
 		domComponentFileEClass = createEClass(DOM_COMPONENT_FILE);
 
 		devComponentFileEClass = createEClass(DEV_COMPONENT_FILE);
 
 		componentFileRefEClass = createEClass(COMPONENT_FILE_REF);
 		createEReference(componentFileRefEClass, COMPONENT_FILE_REF__FILE);
 		createEAttribute(componentFileRefEClass, COMPONENT_FILE_REF__REFID);
 
 		componentFilesEClass = createEClass(COMPONENT_FILES);
 		createEReference(componentFilesEClass, COMPONENT_FILES__COMPONENT_FILE);
 
 		componentInstantiationEClass = createEClass(COMPONENT_INSTANTIATION);
 		createEAttribute(componentInstantiationEClass, COMPONENT_INSTANTIATION__USAGE_NAME);
 		createEReference(componentInstantiationEClass, COMPONENT_INSTANTIATION__COMPONENT_PROPERTIES);
 		createEReference(componentInstantiationEClass, COMPONENT_INSTANTIATION__PROVIDES);
 		createEReference(componentInstantiationEClass, COMPONENT_INSTANTIATION__USES);
 		createEReference(componentInstantiationEClass, COMPONENT_INSTANTIATION__INTERFACE_STUB);
 		createEAttribute(componentInstantiationEClass, COMPONENT_INSTANTIATION__ID);
 		createEReference(componentInstantiationEClass, COMPONENT_INSTANTIATION__PLACEMENT);
 		createEAttribute(componentInstantiationEClass, COMPONENT_INSTANTIATION__IMPL_ID);
 
 		componentInstantiationRefEClass = createEClass(COMPONENT_INSTANTIATION_REF);
 		createEReference(componentInstantiationRefEClass, COMPONENT_INSTANTIATION_REF__INSTANTIATION);
 		createEAttribute(componentInstantiationRefEClass, COMPONENT_INSTANTIATION_REF__REFID);
 
 		componentPlacementEClass = createEClass(COMPONENT_PLACEMENT);
 		createEReference(componentPlacementEClass, COMPONENT_PLACEMENT__COMPONENT_FILE_REF);
 		createEReference(componentPlacementEClass, COMPONENT_PLACEMENT__COMPONENT_INSTANTIATION);
 		createEAttribute(componentPlacementEClass, COMPONENT_PLACEMENT__NAME);
 
 		componentPropertiesEClass = createEClass(COMPONENT_PROPERTIES);
 		createEAttribute(componentPropertiesEClass, COMPONENT_PROPERTIES__PROPERTIES);
 		createEReference(componentPropertiesEClass, COMPONENT_PROPERTIES__SIMPLE_REF);
 		createEReference(componentPropertiesEClass, COMPONENT_PROPERTIES__SIMPLE_SEQUENCE_REF);
 		createEReference(componentPropertiesEClass, COMPONENT_PROPERTIES__STRUCT_REF);
 		createEReference(componentPropertiesEClass, COMPONENT_PROPERTIES__STRUCT_SEQUENCE_REF);
 
 		componentSupportedInterfaceEClass = createEClass(COMPONENT_SUPPORTED_INTERFACE);
 		createEAttribute(componentSupportedInterfaceEClass, COMPONENT_SUPPORTED_INTERFACE__SUPPORTED_IDENTIFIER);
 		createEReference(componentSupportedInterfaceEClass, COMPONENT_SUPPORTED_INTERFACE__COMPONENT_INSTANTIATION_REF);
 		createEReference(componentSupportedInterfaceEClass, COMPONENT_SUPPORTED_INTERFACE__FIND_BY);
 		createEReference(componentSupportedInterfaceEClass, COMPONENT_SUPPORTED_INTERFACE__INTERFACE);
 
 		componentSupportedInterfaceStubEClass = createEClass(COMPONENT_SUPPORTED_INTERFACE_STUB);
 
 		connectInterfaceEClass = createEClass(CONNECT_INTERFACE);
 		createEReference(connectInterfaceEClass, CONNECT_INTERFACE__USES_PORT);
 		createEReference(connectInterfaceEClass, CONNECT_INTERFACE__PROVIDES_PORT);
 		createEReference(connectInterfaceEClass, CONNECT_INTERFACE__COMPONENT_SUPPORTED_INTERFACE);
 		createEReference(connectInterfaceEClass, CONNECT_INTERFACE__FIND_BY);
 		createEAttribute(connectInterfaceEClass, CONNECT_INTERFACE__ID);
 		createEReference(connectInterfaceEClass, CONNECT_INTERFACE__SOURCE);
 		createEReference(connectInterfaceEClass, CONNECT_INTERFACE__TARGET);
 
 		connectionsEClass = createEClass(CONNECTIONS);
 		createEReference(connectionsEClass, CONNECTIONS__CONNECT_INTERFACE);
 
 		connectionTargetEClass = createEClass(CONNECTION_TARGET);
 
 		deviceThatLoadedThisComponentRefEClass = createEClass(DEVICE_THAT_LOADED_THIS_COMPONENT_REF);
 		createEAttribute(deviceThatLoadedThisComponentRefEClass, DEVICE_THAT_LOADED_THIS_COMPONENT_REF__REFID);
 
 		deviceUsedByThisComponentRefEClass = createEClass(DEVICE_USED_BY_THIS_COMPONENT_REF);
 		createEAttribute(deviceUsedByThisComponentRefEClass, DEVICE_USED_BY_THIS_COMPONENT_REF__REF_ID);
 		createEAttribute(deviceUsedByThisComponentRefEClass, DEVICE_USED_BY_THIS_COMPONENT_REF__USES_REF_ID);
 
 		domainFinderEClass = createEClass(DOMAIN_FINDER);
 		createEAttribute(domainFinderEClass, DOMAIN_FINDER__NAME);
 		createEAttribute(domainFinderEClass, DOMAIN_FINDER__TYPE);
 
 		findByEClass = createEClass(FIND_BY);
 		createEReference(findByEClass, FIND_BY__NAMING_SERVICE);
 		createEReference(findByEClass, FIND_BY__DOMAIN_FINDER);
 
 		findByStubEClass = createEClass(FIND_BY_STUB);
 		createEReference(findByStubEClass, FIND_BY_STUB__NAMING_SERVICE);
 		createEReference(findByStubEClass, FIND_BY_STUB__DOMAIN_FINDER);
 		createEReference(findByStubEClass, FIND_BY_STUB__PROVIDES);
 		createEReference(findByStubEClass, FIND_BY_STUB__USES);
 		createEReference(findByStubEClass, FIND_BY_STUB__INTERFACE);
 
 		findByStubContainerEClass = createEClass(FIND_BY_STUB_CONTAINER);
 		createEReference(findByStubContainerEClass, FIND_BY_STUB_CONTAINER__STUBS);
 
 		localFileEClass = createEClass(LOCAL_FILE);
 		createEAttribute(localFileEClass, LOCAL_FILE__NAME);
 
 		namingServiceEClass = createEClass(NAMING_SERVICE);
 		createEAttribute(namingServiceEClass, NAMING_SERVICE__NAME);
 
 		partitioningEClass = createEClass(PARTITIONING);
 		createEAttribute(partitioningEClass, PARTITIONING__PARTS);
 		createEReference(partitioningEClass, PARTITIONING__COMPONENT_PLACEMENT);
 
 		providesPortEClass = createEClass(PROVIDES_PORT);
 		createEAttribute(providesPortEClass, PROVIDES_PORT__PROVIDES_IDENTIFIER);
 		createEReference(providesPortEClass, PROVIDES_PORT__COMPONENT_INSTANTIATION_REF);
 		createEReference(providesPortEClass, PROVIDES_PORT__DEVICE_THAT_LOADED_THIS_COMPONENT_REF);
 		createEReference(providesPortEClass, PROVIDES_PORT__DEVICE_USED_BY_THIS_COMPONENT_REF);
 		createEReference(providesPortEClass, PROVIDES_PORT__FIND_BY);
 
 		providesPortStubEClass = createEClass(PROVIDES_PORT_STUB);
 		createEAttribute(providesPortStubEClass, PROVIDES_PORT_STUB__NAME);
 		createEReference(providesPortStubEClass, PROVIDES_PORT_STUB__PROVIDES);
 
 		usesPortEClass = createEClass(USES_PORT);
 		createEAttribute(usesPortEClass, USES_PORT__USES_INDENTIFIER);
 		createEReference(usesPortEClass, USES_PORT__COMPONENT_INSTANTIATION_REF);
 		createEReference(usesPortEClass, USES_PORT__DEVICE_THAT_LOADED_THIS_COMPONENT_REF);
 		createEReference(usesPortEClass, USES_PORT__DEVICE_USED_BY_THIS_COMPONENT_REF);
 		createEReference(usesPortEClass, USES_PORT__FIND_BY);
 
 		usesPortStubEClass = createEClass(USES_PORT_STUB);
 		createEAttribute(usesPortStubEClass, USES_PORT_STUB__NAME);
 		createEReference(usesPortStubEClass, USES_PORT_STUB__USES);
 		createEAttribute(usesPortStubEClass, USES_PORT_STUB__SUPPORTED_IDENTIFIER);
 
 		// Create enums
 		domainFinderTypeEEnum = createEEnum(DOMAIN_FINDER_TYPE);
 
 		// Create data types
 		domainFinderTypeObjectEDataType = createEDataType(DOMAIN_FINDER_TYPE_OBJECT);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isInitialized = false;
 
 	/**
 	 * Complete the initialization of the package and its meta-model.  This
 	 * method is guarded to have no affect on any invocation but its first.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void initializePackageContents() {
 		if (isInitialized) return;
 		isInitialized = true;
 
 		// Initialize package
 		setName(eNAME);
 		setNsPrefix(eNS_PREFIX);
 		setNsURI(eNS_URI);
 
 		// Obtain other dependent packages
 		XMLTypePackage theXMLTypePackage = (XMLTypePackage)EPackage.Registry.INSTANCE.getEPackage(XMLTypePackage.eNS_URI);
 		SpdPackage theSpdPackage = (SpdPackage)EPackage.Registry.INSTANCE.getEPackage(SpdPackage.eNS_URI);
 		EcorePackage theEcorePackage = (EcorePackage)EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);
 		PrfPackage thePrfPackage = (PrfPackage)EPackage.Registry.INSTANCE.getEPackage(PrfPackage.eNS_URI);
 		ScdPackage theScdPackage = (ScdPackage)EPackage.Registry.INSTANCE.getEPackage(ScdPackage.eNS_URI);
 
 		// Create type parameters
 		ETypeParameter componentInstantiationRefEClass_R = addETypeParameter(componentInstantiationRefEClass, "R");
 		ETypeParameter componentPlacementEClass_I = addETypeParameter(componentPlacementEClass, "I");
 		ETypeParameter connectInterfaceEClass_CIR = addETypeParameter(connectInterfaceEClass, "CIR");
 		ETypeParameter connectInterfaceEClass_UP = addETypeParameter(connectInterfaceEClass, "UP");
 		ETypeParameter connectInterfaceEClass_PP = addETypeParameter(connectInterfaceEClass, "PP");
 		ETypeParameter connectionsEClass_CI = addETypeParameter(connectionsEClass, "CI");
 		ETypeParameter partitioningEClass_C = addETypeParameter(partitioningEClass, "C");
 		ETypeParameter providesPortEClass_P = addETypeParameter(providesPortEClass, "P");
 		ETypeParameter usesPortEClass_U = addETypeParameter(usesPortEClass, "U");
 
 		// Set bounds for type parameters
 		EGenericType g1 = createEGenericType(this.getComponentInstantiation());
 		componentInstantiationRefEClass_R.getEBounds().add(g1);
 		g1 = createEGenericType(this.getComponentInstantiation());
 		componentPlacementEClass_I.getEBounds().add(g1);
 		g1 = createEGenericType(this.getComponentInstantiationRef());
 		EGenericType g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		connectInterfaceEClass_CIR.getEBounds().add(g1);
 		g1 = createEGenericType(this.getUsesPort());
 		g2 = createEGenericType(connectInterfaceEClass_CIR);
 		g1.getETypeArguments().add(g2);
 		connectInterfaceEClass_UP.getEBounds().add(g1);
 		g1 = createEGenericType(this.getProvidesPort());
 		g2 = createEGenericType(connectInterfaceEClass_CIR);
 		g1.getETypeArguments().add(g2);
 		connectInterfaceEClass_PP.getEBounds().add(g1);
 		g1 = createEGenericType(this.getConnectInterface());
 		g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		connectionsEClass_CI.getEBounds().add(g1);
 		g1 = createEGenericType(this.getComponentPlacement());
 		g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		partitioningEClass_C.getEBounds().add(g1);
 		g1 = createEGenericType(this.getComponentInstantiationRef());
 		g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		providesPortEClass_P.getEBounds().add(g1);
 		g1 = createEGenericType(this.getComponentInstantiationRef());
 		g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		usesPortEClass_U.getEBounds().add(g1);
 
 		// Add supertypes to classes
 		domComponentFileEClass.getESuperTypes().add(this.getComponentFile());
 		devComponentFileEClass.getESuperTypes().add(this.getComponentFile());
 		componentPropertiesEClass.getESuperTypes().add(thePrfPackage.getPropertyRefContainer());
 		componentSupportedInterfaceStubEClass.getESuperTypes().add(this.getConnectionTarget());
 		findByStubEClass.getESuperTypes().add(this.getConnectionTarget());
 		providesPortStubEClass.getESuperTypes().add(this.getConnectionTarget());
 
 		// Initialize classes and features; add operations and parameters
 		initEClass(componentFileEClass, ComponentFile.class, "ComponentFile", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getComponentFile_LocalFile(), this.getLocalFile(), null, "localFile", null, 1, 1, ComponentFile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getComponentFile_Id(), theXMLTypePackage.getString(), "id", null, 1, 1, ComponentFile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getComponentFile_SoftPkg(), theSpdPackage.getSoftPkg(), null, "softPkg", null, 0, 1, ComponentFile.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEAttribute(getComponentFile_Type(), theXMLTypePackage.getString(), "type", null, 0, 1, ComponentFile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(domComponentFileEClass, DomComponentFile.class, "DomComponentFile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(devComponentFileEClass, DevComponentFile.class, "DevComponentFile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(componentFileRefEClass, ComponentFileRef.class, "ComponentFileRef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getComponentFileRef_File(), this.getComponentFile(), null, "file", null, 0, 1, ComponentFileRef.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEAttribute(getComponentFileRef_Refid(), theXMLTypePackage.getString(), "refid", null, 1, 1, ComponentFileRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(componentFilesEClass, ComponentFiles.class, "ComponentFiles", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getComponentFiles_ComponentFile(), this.getComponentFile(), null, "componentFile", null, 1, -1, ComponentFiles.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(componentInstantiationEClass, ComponentInstantiation.class, "ComponentInstantiation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getComponentInstantiation_UsageName(), theXMLTypePackage.getString(), "usageName", null, 0, 1, ComponentInstantiation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getComponentInstantiation_ComponentProperties(), this.getComponentProperties(), null, "componentProperties", null, 0, 1, ComponentInstantiation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getComponentInstantiation_Provides(), this.getProvidesPortStub(), null, "provides", null, 0, -1, ComponentInstantiation.class, IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEReference(getComponentInstantiation_Uses(), this.getUsesPortStub(), null, "uses", null, 0, -1, ComponentInstantiation.class, IS_TRANSIENT, !IS_VOLATILE, !IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEReference(getComponentInstantiation_InterfaceStub(), this.getComponentSupportedInterfaceStub(), null, "interfaceStub", null, 0, 1, ComponentInstantiation.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getComponentInstantiation_Id(), theXMLTypePackage.getName_(), "id", null, 1, 1, ComponentInstantiation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		g1 = createEGenericType(this.getComponentPlacement());
 		g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		initEReference(getComponentInstantiation_Placement(), g1, this.getComponentPlacement_ComponentInstantiation(), "placement", null, 0, 1, ComponentInstantiation.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getComponentInstantiation_ImplID(), theEcorePackage.getEString(), "implID", null, 0, 1, ComponentInstantiation.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(componentInstantiationRefEClass, ComponentInstantiationRef.class, "ComponentInstantiationRef", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		g1 = createEGenericType(componentInstantiationRefEClass_R);
 		initEReference(getComponentInstantiationRef_Instantiation(), g1, null, "instantiation", null, 0, 1, ComponentInstantiationRef.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEAttribute(getComponentInstantiationRef_Refid(), theXMLTypePackage.getString(), "refid", null, 1, 1, ComponentInstantiationRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(componentPlacementEClass, ComponentPlacement.class, "ComponentPlacement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getComponentPlacement_ComponentFileRef(), this.getComponentFileRef(), null, "componentFileRef", null, 1, 1, ComponentPlacement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		g1 = createEGenericType(componentPlacementEClass_I);
 		initEReference(getComponentPlacement_ComponentInstantiation(), g1, this.getComponentInstantiation_Placement(), "componentInstantiation", null, 1, -1, ComponentPlacement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getComponentPlacement_Name(), theEcorePackage.getEString(), "name", null, 0, 1, ComponentPlacement.class, IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		initEClass(componentPropertiesEClass, ComponentProperties.class, "ComponentProperties", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getComponentProperties_Properties(), ecorePackage.getEFeatureMapEntry(), "properties", null, 0, -1, ComponentProperties.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getComponentProperties_SimpleRef(), thePrfPackage.getSimpleRef(), null, "simpleRef", null, 0, -1, ComponentProperties.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEReference(getComponentProperties_SimpleSequenceRef(), thePrfPackage.getSimpleSequenceRef(), null, "simpleSequenceRef", null, 0, -1, ComponentProperties.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEReference(getComponentProperties_StructRef(), thePrfPackage.getStructRef(), null, "structRef", null, 0, -1, ComponentProperties.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEReference(getComponentProperties_StructSequenceRef(), thePrfPackage.getStructSequenceRef(), null, "structSequenceRef", null, 0, -1, ComponentProperties.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		initEClass(componentSupportedInterfaceEClass, ComponentSupportedInterface.class, "ComponentSupportedInterface", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getComponentSupportedInterface_SupportedIdentifier(), theXMLTypePackage.getString(), "supportedIdentifier", null, 1, 1, ComponentSupportedInterface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		g1 = createEGenericType(this.getComponentInstantiationRef());
 		g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		initEReference(getComponentSupportedInterface_ComponentInstantiationRef(), g1, null, "componentInstantiationRef", null, 0, 1, ComponentSupportedInterface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getComponentSupportedInterface_FindBy(), this.getFindBy(), null, "findBy", null, 0, 1, ComponentSupportedInterface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getComponentSupportedInterface_Interface(), theScdPackage.getInterface(), null, "interface", null, 0, 1, ComponentSupportedInterface.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		initEClass(componentSupportedInterfaceStubEClass, ComponentSupportedInterfaceStub.class, "ComponentSupportedInterfaceStub", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(connectInterfaceEClass, ConnectInterface.class, "ConnectInterface", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		g1 = createEGenericType(connectInterfaceEClass_UP);
 		initEReference(getConnectInterface_UsesPort(), g1, null, "usesPort", null, 1, 1, ConnectInterface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		g1 = createEGenericType(connectInterfaceEClass_PP);
 		initEReference(getConnectInterface_ProvidesPort(), g1, null, "providesPort", null, 0, 1, ConnectInterface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getConnectInterface_ComponentSupportedInterface(), this.getComponentSupportedInterface(), null, "componentSupportedInterface", null, 0, 1, ConnectInterface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getConnectInterface_FindBy(), this.getFindBy(), null, "findBy", null, 0, 1, ConnectInterface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getConnectInterface_Id(), theXMLTypePackage.getString(), "id", null, 0, 1, ConnectInterface.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getConnectInterface_Source(), this.getUsesPortStub(), null, "source", null, 0, 1, ConnectInterface.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 		initEReference(getConnectInterface_Target(), this.getConnectionTarget(), null, "target", null, 0, 1, ConnectInterface.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		initEClass(connectionsEClass, Connections.class, "Connections", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		g1 = createEGenericType(connectionsEClass_CI);
 		initEReference(getConnections_ConnectInterface(), g1, null, "connectInterface", null, 0, -1, Connections.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(connectionTargetEClass, ConnectionTarget.class, "ConnectionTarget", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(deviceThatLoadedThisComponentRefEClass, DeviceThatLoadedThisComponentRef.class, "DeviceThatLoadedThisComponentRef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getDeviceThatLoadedThisComponentRef_Refid(), theXMLTypePackage.getString(), "refid", null, 1, 1, DeviceThatLoadedThisComponentRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(deviceUsedByThisComponentRefEClass, DeviceUsedByThisComponentRef.class, "DeviceUsedByThisComponentRef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getDeviceUsedByThisComponentRef_RefId(), theXMLTypePackage.getString(), "refId", null, 1, 1, DeviceUsedByThisComponentRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getDeviceUsedByThisComponentRef_UsesRefId(), theXMLTypePackage.getString(), "usesRefId", null, 1, 1, DeviceUsedByThisComponentRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(domainFinderEClass, DomainFinder.class, "DomainFinder", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getDomainFinder_Name(), theXMLTypePackage.getString(), "name", null, 0, 1, DomainFinder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getDomainFinder_Type(), this.getDomainFinderType(), "type", null, 1, 1, DomainFinder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(findByEClass, FindBy.class, "FindBy", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getFindBy_NamingService(), this.getNamingService(), null, "namingService", null, 0, 1, FindBy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFindBy_DomainFinder(), this.getDomainFinder(), null, "domainFinder", null, 0, 1, FindBy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(findByStubEClass, FindByStub.class, "FindByStub", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getFindByStub_NamingService(), this.getNamingService(), null, "namingService", null, 0, 1, FindByStub.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFindByStub_DomainFinder(), this.getDomainFinder(), null, "domainFinder", null, 0, 1, FindByStub.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFindByStub_Provides(), this.getProvidesPortStub(), null, "provides", null, 0, -1, FindByStub.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFindByStub_Uses(), this.getUsesPortStub(), null, "uses", null, 0, -1, FindByStub.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFindByStub_Interface(), this.getComponentSupportedInterfaceStub(), null, "interface", null, 0, 1, FindByStub.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(findByStubContainerEClass, FindByStubContainer.class, "FindByStubContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getFindByStubContainer_Stubs(), this.getFindByStub(), null, "stubs", null, 0, -1, FindByStubContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(localFileEClass, LocalFile.class, "LocalFile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getLocalFile_Name(), theXMLTypePackage.getString(), "name", null, 1, 1, LocalFile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(namingServiceEClass, NamingService.class, "NamingService", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getNamingService_Name(), theXMLTypePackage.getString(), "name", null, 1, 1, NamingService.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(partitioningEClass, Partitioning.class, "Partitioning", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getPartitioning_Parts(), ecorePackage.getEFeatureMapEntry(), "parts", null, 0, -1, Partitioning.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		g1 = createEGenericType(partitioningEClass_C);
 		initEReference(getPartitioning_ComponentPlacement(), g1, null, "componentPlacement", null, 0, -1, Partitioning.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		initEClass(providesPortEClass, ProvidesPort.class, "ProvidesPort", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getProvidesPort_ProvidesIdentifier(), theXMLTypePackage.getString(), "providesIdentifier", null, 1, 1, ProvidesPort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		g1 = createEGenericType(providesPortEClass_P);
 		initEReference(getProvidesPort_ComponentInstantiationRef(), g1, null, "componentInstantiationRef", null, 0, 1, ProvidesPort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProvidesPort_DeviceThatLoadedThisComponentRef(), this.getDeviceThatLoadedThisComponentRef(), null, "deviceThatLoadedThisComponentRef", null, 0, 1, ProvidesPort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProvidesPort_DeviceUsedByThisComponentRef(), this.getDeviceUsedByThisComponentRef(), null, "deviceUsedByThisComponentRef", null, 0, 1, ProvidesPort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProvidesPort_FindBy(), this.getFindBy(), null, "findBy", null, 0, 1, ProvidesPort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(providesPortStubEClass, ProvidesPortStub.class, "ProvidesPortStub", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getProvidesPortStub_Name(), theEcorePackage.getEString(), "name", null, 0, 1, ProvidesPortStub.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getProvidesPortStub_Provides(), theScdPackage.getProvides(), null, "provides", null, 0, 1, ProvidesPortStub.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(usesPortEClass, UsesPort.class, "UsesPort", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getUsesPort_UsesIndentifier(), theXMLTypePackage.getString(), "usesIndentifier", null, 1, 1, UsesPort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		g1 = createEGenericType(usesPortEClass_U);
 		initEReference(getUsesPort_ComponentInstantiationRef(), g1, null, "componentInstantiationRef", null, 0, 1, UsesPort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getUsesPort_DeviceThatLoadedThisComponentRef(), this.getDeviceThatLoadedThisComponentRef(), null, "deviceThatLoadedThisComponentRef", null, 0, 1, UsesPort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getUsesPort_DeviceUsedByThisComponentRef(), this.getDeviceUsedByThisComponentRef(), null, "deviceUsedByThisComponentRef", null, 0, 1, UsesPort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getUsesPort_FindBy(), this.getFindBy(), null, "findBy", null, 0, 1, UsesPort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(usesPortStubEClass, UsesPortStub.class, "UsesPortStub", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getUsesPortStub_Name(), theEcorePackage.getEString(), "name", null, 0, 1, UsesPortStub.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getUsesPortStub_Uses(), theScdPackage.getUses(), null, "uses", null, 0, 1, UsesPortStub.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getUsesPortStub_SupportedIdentifier(), theEcorePackage.getEString(), "supportedIdentifier", null, 0, 1, UsesPortStub.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		// Initialize enums and add enum literals
 		initEEnum(domainFinderTypeEEnum, DomainFinderType.class, "DomainFinderType");
 		addEEnumLiteral(domainFinderTypeEEnum, DomainFinderType.FILEMANAGER);
 		addEEnumLiteral(domainFinderTypeEEnum, DomainFinderType.LOG);
 		addEEnumLiteral(domainFinderTypeEEnum, DomainFinderType.EVENTCHANNEL);
 		addEEnumLiteral(domainFinderTypeEEnum, DomainFinderType.NAMINGSERVICE);
 		addEEnumLiteral(domainFinderTypeEEnum, DomainFinderType.DOMAINMANAGER);
 		addEEnumLiteral(domainFinderTypeEEnum, DomainFinderType.SERVICENAME);
 		addEEnumLiteral(domainFinderTypeEEnum, DomainFinderType.SERVICETYPE);
 
 		// Initialize data types
 		initEDataType(domainFinderTypeObjectEDataType, DomainFinderType.class, "DomainFinderTypeObject", IS_SERIALIZABLE, IS_GENERATED_INSTANCE_CLASS);
 
 		// Create resource
 		createResource(eNS_URI);
 
 		// Create annotations
 		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
 		createExtendedMetaDataAnnotations();
 		// http://www.eclipse.org/emf/2002/Ecore
 		createEcoreAnnotations();
 	}
 
 	/**
 	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void createExtendedMetaDataAnnotations() {
 		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";		
 		addAnnotation
 		  (componentFileEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "componentfile",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getComponentFile_LocalFile(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "localfile"
 		   });		
 		addAnnotation
 		  (getComponentFile_Id(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "id"
 		   });		
 		addAnnotation
 		  (getComponentFile_SoftPkg(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "softPkg"
 		   });		
 		addAnnotation
 		  (getComponentFile_Type(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "type"
 		   });		
 		addAnnotation
 		  (domComponentFileEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "componentfile",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (devComponentFileEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "componentfile",
 			 "qualified", "false"
 		   });			
 		addAnnotation
 		  (componentFileRefEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "componentfileref",
 			 "qualified", "false"
 		   });			
 		addAnnotation
 		  (getComponentFileRef_File(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "file"
 		   });		
 		addAnnotation
 		  (getComponentFileRef_Refid(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "refid"
 		   });		
 		addAnnotation
 		  (componentFilesEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "componentfiles",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getComponentFiles_ComponentFile(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "componentfile"
 		   });				
 		addAnnotation
 		  (getComponentInstantiation_UsageName(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "usagename"
 		   });			
 		addAnnotation
 		  (getComponentInstantiation_ComponentProperties(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "componentproperties"
 		   });		
 		addAnnotation
 		  (getComponentInstantiation_Provides(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "provides"
 		   });		
 		addAnnotation
 		  (getComponentInstantiation_Uses(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "uses"
 		   });		
 		addAnnotation
 		  (getComponentInstantiation_InterfaceStub(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "interfaceports"
 		   });			
 		addAnnotation
 		  (getComponentInstantiation_Id(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "id"
 		   });		
 		addAnnotation
 		  (componentInstantiationRefEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "componentinstantiationref",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getComponentInstantiationRef_Instantiation(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "instantiation"
 		   });		
 		addAnnotation
 		  (getComponentInstantiationRef_Refid(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "refid"
 		   });		
 		addAnnotation
 		  (componentPlacementEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "componentplacement",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getComponentPlacement_ComponentFileRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "componentfileref"
 		   });		
 		addAnnotation
 		  (getComponentPlacement_ComponentInstantiation(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "componentinstantiation"
 		   });		
 		addAnnotation
 		  (getComponentPlacement_Name(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "name"
 		   });		
 		addAnnotation
 		  (componentPropertiesEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "componentproperties",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getComponentProperties_Properties(), 
 		   source, 
 		   new String[] {
 			 "kind", "group",
 			 "name", "properties:0"
 		   });		
 		addAnnotation
 		  (getComponentProperties_SimpleRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "simpleref",
 			 "group", "#properties:0"
 		   });		
 		addAnnotation
 		  (getComponentProperties_SimpleSequenceRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "simplesequenceref",
 			 "group", "#properties:0"
 		   });		
 		addAnnotation
 		  (getComponentProperties_StructRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "structref",
 			 "group", "#properties:0"
 		   });		
 		addAnnotation
 		  (getComponentProperties_StructSequenceRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "structsequenceref",
 			 "group", "#properties:0"
 		   });		
 		addAnnotation
 		  (componentSupportedInterfaceEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "componentsupportedinterface",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getComponentSupportedInterface_SupportedIdentifier(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "supportedidentifier"
 		   });		
 		addAnnotation
 		  (getComponentSupportedInterface_ComponentInstantiationRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "componentinstantiationref"
 		   });		
 		addAnnotation
 		  (getComponentSupportedInterface_FindBy(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "findby"
 		   });		
 		addAnnotation
 		  (getComponentSupportedInterface_Interface(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "interface"
 		   });			
 		addAnnotation
 		  (connectInterfaceEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "connectinterface",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getConnectInterface_UsesPort(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "usesport"
 		   });		
 		addAnnotation
 		  (getConnectInterface_ProvidesPort(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "providesport"
 		   });		
 		addAnnotation
 		  (getConnectInterface_ComponentSupportedInterface(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "componentsupportedinterface"
 		   });		
 		addAnnotation
 		  (getConnectInterface_FindBy(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "findby"
 		   });		
 		addAnnotation
 		  (getConnectInterface_Id(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "id"
 		   });		
 		addAnnotation
 		  (getConnectInterface_Source(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "source"
 		   });		
 		addAnnotation
 		  (getConnectInterface_Target(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "target"
 		   });		
 		addAnnotation
 		  (connectionsEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "connections",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getConnections_ConnectInterface(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "connectinterface"
 		   });		
 		addAnnotation
 		  (deviceThatLoadedThisComponentRefEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "devicethatloadedthiscomponentref",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getDeviceThatLoadedThisComponentRef_Refid(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "refid"
 		   });		
 		addAnnotation
 		  (deviceUsedByThisComponentRefEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "deviceusedbythiscomponentref",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getDeviceUsedByThisComponentRef_RefId(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "refid"
 		   });		
 		addAnnotation
 		  (getDeviceUsedByThisComponentRef_UsesRefId(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "usesrefid"
 		   });		
 		addAnnotation
 		  (domainFinderEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "domainfinder",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getDomainFinder_Name(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "name"
 		   });		
 		addAnnotation
 		  (getDomainFinder_Type(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "type"
 		   });		
 		addAnnotation
 		  (domainFinderTypeEEnum, 
 		   source, 
 		   new String[] {
 			 "name", "domainfindertype",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (domainFinderTypeObjectEDataType, 
 		   source, 
 		   new String[] {
 			 "name", "domainfindertype:object",
 			 "baseType", "domainfindertype",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (findByEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "findby",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getFindBy_NamingService(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "namingservice"
 		   });		
 		addAnnotation
 		  (getFindBy_DomainFinder(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "domainfinder"
 		   });			
 		addAnnotation
 		  (getFindByStub_NamingService(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "namingservice"
 		   });		
 		addAnnotation
 		  (getFindByStub_DomainFinder(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "domainfinder"
 		   });		
 		addAnnotation
 		  (getFindByStub_Provides(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "provides"
 		   });		
 		addAnnotation
 		  (getFindByStub_Uses(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "uses"
 		   });		
 		addAnnotation
 		  (getFindByStub_Interface(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "interfaceports"
 		   });		
 		addAnnotation
 		  (getFindByStubContainer_Stubs(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "stubs"
 		   });		
 		addAnnotation
 		  (localFileEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "localfile",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getLocalFile_Name(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "name"
 		   });		
 		addAnnotation
 		  (namingServiceEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "namingservice",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getNamingService_Name(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "name"
 		   });		
 		addAnnotation
 		  (partitioningEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "partitioning",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getPartitioning_Parts(), 
 		   source, 
 		   new String[] {
 			 "kind", "group",
 			 "name", "parts:0"
 		   });		
 		addAnnotation
 		  (getPartitioning_ComponentPlacement(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "componentplacement",
 			 "group", "#parts:0"
 		   });		
 		addAnnotation
 		  (providesPortEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "providesport",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getProvidesPort_ProvidesIdentifier(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "providesidentifier"
 		   });		
 		addAnnotation
 		  (getProvidesPort_ComponentInstantiationRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "componentinstantiationref"
 		   });		
 		addAnnotation
 		  (getProvidesPort_DeviceThatLoadedThisComponentRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "devicethatloadedthiscomponentref"
 		   });		
 		addAnnotation
 		  (getProvidesPort_DeviceUsedByThisComponentRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "deviceusedbythiscomponentref"
 		   });		
 		addAnnotation
 		  (getProvidesPort_FindBy(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "findby"
 		   });		
 		addAnnotation
 		  (providesPortStubEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "providesportstub",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getProvidesPortStub_Name(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "name"
 		   });		
 		addAnnotation
 		  (getProvidesPortStub_Provides(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "provides"
 		   });		
 		addAnnotation
 		  (usesPortEClass, 
 		   source, 
 		   new String[] {
 			 "kind", "elementOnly",
 			 "name", "usesport",
 			 "qualified", "false"
 		   });		
 		addAnnotation
 		  (getUsesPort_UsesIndentifier(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "usesidentifier"
 		   });		
 		addAnnotation
 		  (getUsesPort_ComponentInstantiationRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "componentinstantiationref"
 		   });		
 		addAnnotation
 		  (getUsesPort_DeviceThatLoadedThisComponentRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "devicethatloadedthiscomponentref"
 		   });		
 		addAnnotation
 		  (getUsesPort_DeviceUsedByThisComponentRef(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "deviceusedbythiscomponentref"
 		   });		
 		addAnnotation
 		  (getUsesPort_FindBy(), 
 		   source, 
 		   new String[] {
 			 "kind", "element",
 			 "name", "findby"
 		   });		
 		addAnnotation
 		  (getUsesPortStub_Name(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "name"
 		   });		
 		addAnnotation
 		  (getUsesPortStub_Uses(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "uses"
 		   });		
 		addAnnotation
 		  (getUsesPortStub_SupportedIdentifier(), 
 		   source, 
 		   new String[] {
 			 "kind", "attribute",
 			 "name", "supportedIdentifier"
 		   });
 	}
 
 	/**
 	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/Ecore</b>.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected void createEcoreAnnotations() {
 		String source = "http://www.eclipse.org/emf/2002/Ecore";																																												
 		addAnnotation
 		  (connectInterfaceEClass, 
 		   source, 
 		   new String[] {
 			 "constraints", "ValidSourceReference ValidTargetReference"
 		   });																									
 		addAnnotation
 		  (findByStubEClass, 
 		   source, 
 		   new String[] {
 			 "constraints", "ValidFindByStub"
 		   });																															
 	}
 
 } //PartitioningPackageImpl
