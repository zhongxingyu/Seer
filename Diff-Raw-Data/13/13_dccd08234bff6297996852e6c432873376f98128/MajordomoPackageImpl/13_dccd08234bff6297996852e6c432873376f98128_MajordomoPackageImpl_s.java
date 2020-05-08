 /**
  */
 package de.altimos.mdsd.majordomo.impl;
 
 import de.altimos.mdsd.majordomo.Action;
 import de.altimos.mdsd.majordomo.Actor;
 import de.altimos.mdsd.majordomo.BoilerActor;
 import de.altimos.mdsd.majordomo.BooleanAction;
 import de.altimos.mdsd.majordomo.BooleanActor;
 import de.altimos.mdsd.majordomo.BooleanCondition;
 import de.altimos.mdsd.majordomo.BooleanSensor;
 import de.altimos.mdsd.majordomo.ClockSensor;
 import de.altimos.mdsd.majordomo.Comparator;
 import de.altimos.mdsd.majordomo.Condition;
 import de.altimos.mdsd.majordomo.Extendable;
 import de.altimos.mdsd.majordomo.Extension;
 import de.altimos.mdsd.majordomo.FloatAction;
 import de.altimos.mdsd.majordomo.FloatActor;
 import de.altimos.mdsd.majordomo.FloatCondition;
 import de.altimos.mdsd.majordomo.FloatSensor;
 import de.altimos.mdsd.majordomo.House;
 import de.altimos.mdsd.majordomo.HouseMountable;
 import de.altimos.mdsd.majordomo.LampActor;
 import de.altimos.mdsd.majordomo.LightSensor;
 import de.altimos.mdsd.majordomo.Majordomo;
 import de.altimos.mdsd.majordomo.MajordomoFactory;
 import de.altimos.mdsd.majordomo.MajordomoPackage;
 import de.altimos.mdsd.majordomo.NumberSensor;
 import de.altimos.mdsd.majordomo.RadiatorActor;
 import de.altimos.mdsd.majordomo.RainSensor;
 import de.altimos.mdsd.majordomo.RollerActor;
 import de.altimos.mdsd.majordomo.RoofWindowActor;
 import de.altimos.mdsd.majordomo.Room;
 import de.altimos.mdsd.majordomo.RoomMountable;
 import de.altimos.mdsd.majordomo.Rule;
 import de.altimos.mdsd.majordomo.Sensor;
 import de.altimos.mdsd.majordomo.SwitchSensor;
 import de.altimos.mdsd.majordomo.TemperatureSensor;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 
 import org.eclipse.emf.ecore.impl.EPackageImpl;
 import org.eclipse.xtend.typesystem.emf.check.CheckRegistry;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Package</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class MajordomoPackageImpl extends EPackageImpl implements MajordomoPackage {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass majordomoEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass houseEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass extendableEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass roomEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass sensorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass actorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass ruleEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass extensionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass lightSensorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass temperatureSensorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass rainSensorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass switchSensorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass numberSensorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass clockSensorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass lampActorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass rollerActorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass roofWindowActorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass boilerActorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass radiatorActorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass roomMountableEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass houseMountableEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass booleanSensorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass floatSensorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass booleanActorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass floatActorEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass conditionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass actionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass floatConditionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass booleanConditionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass floatActionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass booleanActionEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EEnum comparatorEEnum = null;
 
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
 	 * @see de.altimos.mdsd.majordomo.MajordomoPackage#eNS_URI
 	 * @see #init()
 	 * @generated not
 	 */
 	private MajordomoPackageImpl() {
 		super(eNS_URI, MajordomoFactory.eINSTANCE);
 		CheckRegistry.getInstance();
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
 	 * <p>This method is used to initialize {@link MajordomoPackage#eINSTANCE} when that field is accessed.
 	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #eNS_URI
 	 * @see #createPackageContents()
 	 * @see #initializePackageContents()
 	 * @generated
 	 */
 	public static MajordomoPackage init() {
 		if (isInited) return (MajordomoPackage)EPackage.Registry.INSTANCE.getEPackage(MajordomoPackage.eNS_URI);
 
 		// Obtain or create and register package
 		MajordomoPackageImpl theMajordomoPackage = (MajordomoPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof MajordomoPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new MajordomoPackageImpl());
 
 		isInited = true;
 
 		// Create package meta-data objects
 		theMajordomoPackage.createPackageContents();
 
 		// Initialize created meta-data
 		theMajordomoPackage.initializePackageContents();
 
 		// Mark meta-data to indicate it can't be changed
 		theMajordomoPackage.freeze();
 
   
 		// Update the registry and return the package
 		EPackage.Registry.INSTANCE.put(MajordomoPackage.eNS_URI, theMajordomoPackage);
 		return theMajordomoPackage;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getMajordomo() {
 		return majordomoEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getMajordomo_House() {
 		return (EReference)majordomoEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getMajordomo_Rules() {
 		return (EReference)majordomoEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getMajordomo_Rooms() {
 		return (EReference)majordomoEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getHouse() {
 		return houseEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getExtendable() {
 		return extendableEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getExtendable_Sensors() {
 		return (EReference)extendableEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getExtendable_Actors() {
 		return (EReference)extendableEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRoom() {
 		return roomEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getRoom_Name() {
 		return (EAttribute)roomEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSensor() {
 		return sensorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSensor_Ctx() {
 		return (EReference)sensorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getActor() {
 		return actorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getActor_Ctx() {
 		return (EReference)actorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRule() {
 		return ruleEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRule_Conditions() {
 		return (EReference)ruleEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRule_Actions() {
 		return (EReference)ruleEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getExtension() {
 		return extensionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getExtension_Name() {
 		return (EAttribute)extensionEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLightSensor() {
 		return lightSensorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getTemperatureSensor() {
 		return temperatureSensorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRainSensor() {
 		return rainSensorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSwitchSensor() {
 		return switchSensorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getNumberSensor() {
 		return numberSensorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getClockSensor() {
 		return clockSensorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getLampActor() {
 		return lampActorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRollerActor() {
 		return rollerActorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRoofWindowActor() {
 		return roofWindowActorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getBoilerActor() {
 		return boilerActorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRadiatorActor() {
 		return radiatorActorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRoomMountable() {
 		return roomMountableEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getHouseMountable() {
 		return houseMountableEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getBooleanSensor() {
 		return booleanSensorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFloatSensor() {
 		return floatSensorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getBooleanActor() {
 		return booleanActorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getBooleanActor_EReference0() {
 		return (EReference)booleanActorEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFloatActor() {
 		return floatActorEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getCondition() {
 		return conditionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getCondition_Comparator() {
 		return (EAttribute)conditionEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getAction() {
 		return actionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFloatCondition() {
 		return floatConditionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFloatCondition_Value() {
 		return (EAttribute)floatConditionEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFloatCondition_Sensor() {
 		return (EReference)floatConditionEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getBooleanCondition() {
 		return booleanConditionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBooleanCondition_Value() {
 		return (EAttribute)booleanConditionEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getBooleanCondition_Sensor() {
 		return (EReference)booleanConditionEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getFloatAction() {
 		return floatActionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getFloatAction_Value() {
 		return (EAttribute)floatActionEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getFloatAction_Actor() {
 		return (EReference)floatActionEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getBooleanAction() {
 		return booleanActionEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getBooleanAction_Value() {
 		return (EAttribute)booleanActionEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getBooleanAction_Actor() {
 		return (EReference)booleanActionEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EEnum getComparator() {
 		return comparatorEEnum;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public MajordomoFactory getMajordomoFactory() {
 		return (MajordomoFactory)getEFactoryInstance();
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
 		majordomoEClass = createEClass(MAJORDOMO);
 		createEReference(majordomoEClass, MAJORDOMO__HOUSE);
 		createEReference(majordomoEClass, MAJORDOMO__RULES);
 		createEReference(majordomoEClass, MAJORDOMO__ROOMS);
 
 		houseEClass = createEClass(HOUSE);
 
 		extendableEClass = createEClass(EXTENDABLE);
 		createEReference(extendableEClass, EXTENDABLE__SENSORS);
 		createEReference(extendableEClass, EXTENDABLE__ACTORS);
 
 		roomEClass = createEClass(ROOM);
 		createEAttribute(roomEClass, ROOM__NAME);
 
 		sensorEClass = createEClass(SENSOR);
 		createEReference(sensorEClass, SENSOR__CTX);
 
 		actorEClass = createEClass(ACTOR);
 		createEReference(actorEClass, ACTOR__CTX);
 
 		ruleEClass = createEClass(RULE);
 		createEReference(ruleEClass, RULE__CONDITIONS);
 		createEReference(ruleEClass, RULE__ACTIONS);
 
 		extensionEClass = createEClass(EXTENSION);
 		createEAttribute(extensionEClass, EXTENSION__NAME);
 
 		lightSensorEClass = createEClass(LIGHT_SENSOR);
 
 		temperatureSensorEClass = createEClass(TEMPERATURE_SENSOR);
 
 		rainSensorEClass = createEClass(RAIN_SENSOR);
 
 		switchSensorEClass = createEClass(SWITCH_SENSOR);
 
 		numberSensorEClass = createEClass(NUMBER_SENSOR);
 
 		clockSensorEClass = createEClass(CLOCK_SENSOR);
 
 		lampActorEClass = createEClass(LAMP_ACTOR);
 
 		rollerActorEClass = createEClass(ROLLER_ACTOR);
 
 		roofWindowActorEClass = createEClass(ROOF_WINDOW_ACTOR);
 
 		boilerActorEClass = createEClass(BOILER_ACTOR);
 
 		radiatorActorEClass = createEClass(RADIATOR_ACTOR);
 
 		roomMountableEClass = createEClass(ROOM_MOUNTABLE);
 
 		houseMountableEClass = createEClass(HOUSE_MOUNTABLE);
 
 		booleanSensorEClass = createEClass(BOOLEAN_SENSOR);
 
 		floatSensorEClass = createEClass(FLOAT_SENSOR);
 
 		booleanActorEClass = createEClass(BOOLEAN_ACTOR);
 		createEReference(booleanActorEClass, BOOLEAN_ACTOR__EREFERENCE0);
 
 		floatActorEClass = createEClass(FLOAT_ACTOR);
 
 		conditionEClass = createEClass(CONDITION);
 		createEAttribute(conditionEClass, CONDITION__COMPARATOR);
 
 		actionEClass = createEClass(ACTION);
 
 		floatConditionEClass = createEClass(FLOAT_CONDITION);
 		createEAttribute(floatConditionEClass, FLOAT_CONDITION__VALUE);
 		createEReference(floatConditionEClass, FLOAT_CONDITION__SENSOR);
 
 		booleanConditionEClass = createEClass(BOOLEAN_CONDITION);
 		createEAttribute(booleanConditionEClass, BOOLEAN_CONDITION__VALUE);
 		createEReference(booleanConditionEClass, BOOLEAN_CONDITION__SENSOR);
 
 		floatActionEClass = createEClass(FLOAT_ACTION);
 		createEAttribute(floatActionEClass, FLOAT_ACTION__VALUE);
 		createEReference(floatActionEClass, FLOAT_ACTION__ACTOR);
 
 		booleanActionEClass = createEClass(BOOLEAN_ACTION);
 		createEAttribute(booleanActionEClass, BOOLEAN_ACTION__VALUE);
 		createEReference(booleanActionEClass, BOOLEAN_ACTION__ACTOR);
 
 		// Create enums
 		comparatorEEnum = createEEnum(COMPARATOR);
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
 
 		// Create type parameters
 
 		// Set bounds for type parameters
 
 		// Add supertypes to classes
 		houseEClass.getESuperTypes().add(this.getExtendable());
 		roomEClass.getESuperTypes().add(this.getExtendable());
 		sensorEClass.getESuperTypes().add(this.getExtension());
 		actorEClass.getESuperTypes().add(this.getExtension());
 		lightSensorEClass.getESuperTypes().add(this.getHouseMountable());
 		lightSensorEClass.getESuperTypes().add(this.getRoomMountable());
 		lightSensorEClass.getESuperTypes().add(this.getFloatSensor());
 		temperatureSensorEClass.getESuperTypes().add(this.getHouseMountable());
 		temperatureSensorEClass.getESuperTypes().add(this.getRoomMountable());
 		temperatureSensorEClass.getESuperTypes().add(this.getFloatSensor());
 		rainSensorEClass.getESuperTypes().add(this.getHouseMountable());
 		rainSensorEClass.getESuperTypes().add(this.getBooleanSensor());
 		switchSensorEClass.getESuperTypes().add(this.getRoomMountable());
 		switchSensorEClass.getESuperTypes().add(this.getBooleanSensor());
 		numberSensorEClass.getESuperTypes().add(this.getRoomMountable());
 		numberSensorEClass.getESuperTypes().add(this.getFloatSensor());
 		clockSensorEClass.getESuperTypes().add(this.getHouseMountable());
 		clockSensorEClass.getESuperTypes().add(this.getFloatSensor());
 		lampActorEClass.getESuperTypes().add(this.getHouseMountable());
 		lampActorEClass.getESuperTypes().add(this.getRoomMountable());
 		lampActorEClass.getESuperTypes().add(this.getFloatActor());
 		rollerActorEClass.getESuperTypes().add(this.getRoomMountable());
 		rollerActorEClass.getESuperTypes().add(this.getBooleanActor());
 		roofWindowActorEClass.getESuperTypes().add(this.getRoomMountable());
 		roofWindowActorEClass.getESuperTypes().add(this.getBooleanActor());
 		boilerActorEClass.getESuperTypes().add(this.getHouseMountable());
 		boilerActorEClass.getESuperTypes().add(this.getBooleanActor());
 		radiatorActorEClass.getESuperTypes().add(this.getRoomMountable());
 		radiatorActorEClass.getESuperTypes().add(this.getBooleanActor());
 		booleanSensorEClass.getESuperTypes().add(this.getSensor());
 		floatSensorEClass.getESuperTypes().add(this.getSensor());
 		booleanActorEClass.getESuperTypes().add(this.getActor());
 		floatActorEClass.getESuperTypes().add(this.getActor());
 		floatConditionEClass.getESuperTypes().add(this.getCondition());
 		booleanConditionEClass.getESuperTypes().add(this.getCondition());
 		floatActionEClass.getESuperTypes().add(this.getAction());
 		booleanActionEClass.getESuperTypes().add(this.getAction());
 
 		// Initialize classes and features; add operations and parameters
 		initEClass(majordomoEClass, Majordomo.class, "Majordomo", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getMajordomo_House(), this.getHouse(), null, "house", null, 0, 1, Majordomo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getMajordomo_Rules(), this.getRule(), null, "rules", null, 0, -1, Majordomo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getMajordomo_Rooms(), this.getRoom(), null, "rooms", null, 0, -1, Majordomo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(houseEClass, House.class, "House", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(extendableEClass, Extendable.class, "Extendable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getExtendable_Sensors(), this.getSensor(), this.getSensor_Ctx(), "sensors", null, 0, -1, Extendable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getExtendable_Actors(), this.getActor(), this.getActor_Ctx(), "actors", null, 0, -1, Extendable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(roomEClass, Room.class, "Room", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getRoom_Name(), ecorePackage.getEString(), "name", null, 0, 1, Room.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(sensorEClass, Sensor.class, "Sensor", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSensor_Ctx(), this.getExtendable(), this.getExtendable_Sensors(), "ctx", null, 1, 1, Sensor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(actorEClass, Actor.class, "Actor", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getActor_Ctx(), this.getExtendable(), this.getExtendable_Actors(), "ctx", null, 1, 1, Actor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(ruleEClass, Rule.class, "Rule", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getRule_Conditions(), this.getCondition(), null, "conditions", null, 0, -1, Rule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getRule_Actions(), this.getAction(), null, "actions", null, 0, -1, Rule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(extensionEClass, Extension.class, "Extension", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getExtension_Name(), ecorePackage.getEString(), "name", null, 0, 1, Extension.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(lightSensorEClass, LightSensor.class, "LightSensor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(temperatureSensorEClass, TemperatureSensor.class, "TemperatureSensor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(rainSensorEClass, RainSensor.class, "RainSensor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(switchSensorEClass, SwitchSensor.class, "SwitchSensor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(numberSensorEClass, NumberSensor.class, "NumberSensor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(clockSensorEClass, ClockSensor.class, "ClockSensor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(lampActorEClass, LampActor.class, "LampActor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(rollerActorEClass, RollerActor.class, "RollerActor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(roofWindowActorEClass, RoofWindowActor.class, "RoofWindowActor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(boilerActorEClass, BoilerActor.class, "BoilerActor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(radiatorActorEClass, RadiatorActor.class, "RadiatorActor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(roomMountableEClass, RoomMountable.class, "RoomMountable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(houseMountableEClass, HouseMountable.class, "HouseMountable", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(booleanSensorEClass, BooleanSensor.class, "BooleanSensor", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		addEOperation(booleanSensorEClass, ecorePackage.getEBoolean(), "getValue", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(floatSensorEClass, FloatSensor.class, "FloatSensor", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		addEOperation(floatSensorEClass, ecorePackage.getEFloat(), "getValue", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(booleanActorEClass, BooleanActor.class, "BooleanActor", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getBooleanActor_EReference0(), this.getActor(), null, "EReference0", null, 0, 1, BooleanActor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		EOperation op = addEOperation(booleanActorEClass, null, "setValue", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, ecorePackage.getEBoolean(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(floatActorEClass, FloatActor.class, "FloatActor", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		op = addEOperation(floatActorEClass, null, "setValue", 0, 1, IS_UNIQUE, IS_ORDERED);
 		addEParameter(op, ecorePackage.getEFloat(), "value", 0, 1, IS_UNIQUE, IS_ORDERED);
 
 		initEClass(conditionEClass, Condition.class, "Condition", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getCondition_Comparator(), this.getComparator(), "comparator", null, 0, 1, Condition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(actionEClass, Action.class, "Action", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 
 		initEClass(floatConditionEClass, FloatCondition.class, "FloatCondition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getFloatCondition_Value(), ecorePackage.getEFloat(), "value", null, 0, 1, FloatCondition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFloatCondition_Sensor(), this.getFloatSensor(), null, "sensor", null, 0, 1, FloatCondition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(booleanConditionEClass, BooleanCondition.class, "BooleanCondition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getBooleanCondition_Value(), ecorePackage.getEBoolean(), "value", null, 0, 1, BooleanCondition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getBooleanCondition_Sensor(), this.getBooleanSensor(), null, "sensor", null, 0, 1, BooleanCondition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(floatActionEClass, FloatAction.class, "FloatAction", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getFloatAction_Value(), ecorePackage.getEFloat(), "value", null, 0, 1, FloatAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getFloatAction_Actor(), this.getFloatActor(), null, "actor", null, 0, 1, FloatAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(booleanActionEClass, BooleanAction.class, "BooleanAction", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getBooleanAction_Value(), ecorePackage.getEBoolean(), "value", null, 0, 1, BooleanAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getBooleanAction_Actor(), this.getBooleanActor(), null, "actor", null, 0, 1, BooleanAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		// Initialize enums and add enum literals
 		initEEnum(comparatorEEnum, Comparator.class, "Comparator");
 		addEEnumLiteral(comparatorEEnum, Comparator.EQUALS);
 		addEEnumLiteral(comparatorEEnum, Comparator.GREATER);
 		addEEnumLiteral(comparatorEEnum, Comparator.LESSER);
 
 		// Create resource
 		createResource(eNS_URI);
 	}
 
 } //MajordomoPackageImpl
