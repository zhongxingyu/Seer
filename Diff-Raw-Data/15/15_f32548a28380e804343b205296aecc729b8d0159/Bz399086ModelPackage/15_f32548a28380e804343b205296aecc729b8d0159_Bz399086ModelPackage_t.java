 package org.eclipse.emf.texo.test.model.issues.bz399086;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.texo.model.ModelFactory;
 import org.eclipse.emf.texo.model.ModelPackage;
 import org.eclipse.emf.texo.model.ModelResolver;
 import org.eclipse.emf.texo.server.store.DaoRegistry;
 import org.eclipse.emf.texo.test.model.base.identifiable.IdentifiableModelPackage;
 import org.eclipse.emf.texo.test.model.issues.bz399086.dao.MapElementDao;
 import org.eclipse.emf.texo.test.model.issues.bz399086.dao.TheMapDao;
 import org.eclipse.emf.texo.utils.ModelUtils;
 
 /**
  * The <b>Package</b> for the model '<em><b>bz399086</b></em>'. It contains initialization code and access to the
  * Factory to instantiate types of this package.
  * 
  * <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @generated
  */
 public class Bz399086ModelPackage extends ModelPackage {
 
   /**
    * Is set when the package has been initialized.
    * 
    * @generated
    */
   private static boolean isInitialized = false;
 
   /**
    * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final String NS_URI = "http://www.eclipse.org/emf/texo/test/model/issues/bz399086";
 
   /**
    * The {@link ModelFactory} for this package. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final Bz399086ModelFactory MODELFACTORY = new Bz399086ModelFactory();
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final int MAPELEMENT_CLASSIFIER_ID = 0;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final int MAPELEMENT_NAME_FEATURE_ID = 2;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final int MAPREFENTRY_CLASSIFIER_ID = 1;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final int MAPREFENTRY_KEY_FEATURE_ID = 0;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final int MAPREFENTRY_VALUE_FEATURE_ID = 1;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final int MAPVALUEENTRY_CLASSIFIER_ID = 2;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final int MAPVALUEENTRY_KEY_FEATURE_ID = 0;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final int MAPVALUEENTRY_VALUE_FEATURE_ID = 1;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final int THEMAP_CLASSIFIER_ID = 3;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final int THEMAP_REFCONTENT_FEATURE_ID = 2;
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final int THEMAP_VALUECONTENT_FEATURE_ID = 3;
 
   /**
    * The static member with the instance of this {@link ModelPackage}. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   public static final Bz399086ModelPackage INSTANCE = initialize();
 
   /**
    * Initializes this {@link ModelPackage}. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return an initialized instance of this class
    * 
    * @generated
    */
   public static Bz399086ModelPackage initialize() {
 
     if (isInitialized) {
       return (Bz399086ModelPackage) ModelResolver.getInstance().getModelPackage(NS_URI);
     }
 
     final Bz399086ModelPackage modelPackage = new Bz399086ModelPackage();
 
     ModelResolver.getInstance().registerModelPackage(modelPackage);
 
     // read the model from the ecore file, the EPackage is registered in the EPackage.Registry
     // see the ModelResolver getEPackageRegistry method
     ModelUtils.readEPackagesFromFile(modelPackage);
 
     isInitialized = true;
 
     IdentifiableModelPackage.initialize();
 
     // force the initialization of the EFactory proxy
     modelPackage.getEPackage();
 
     // register the relation between a Class and its EClassifier
     ModelResolver.getInstance().registerClassModelMapping(MapElement.class, modelPackage.getMapElementEClass(),
         modelPackage);
     ModelResolver.getInstance().registerClassModelMapping(TheMap.class, modelPackage.getTheMapEClass(), modelPackage);
 
     DaoRegistry.getInstance().registerDao(MapElement.class, MapElementDao.class);
     DaoRegistry.getInstance().registerDao(TheMap.class, TheMapDao.class);
 
     // and return ourselves
     return modelPackage;
   }
 
   /**
    * Returns the {@link ModelFactory} of this ModelPackage. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the {@link Bz399086ModelFactory} instance.
    * @generated
    */
   @Override
   public Bz399086ModelFactory getModelFactory() {
     return MODELFACTORY;
   }
 
   /**
    * Returns the nsUri of the {@link EPackage} managed by this Package instance. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @return the nsUri of the EPackage
    * @generated
    */
   @Override
   public String getNsURI() {
     return NS_URI;
   }
 
   /**
    * Returns the name of the ecore file containing the ecore model of the {@link EPackage} managed here. <!--
    * begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return the name of the ecore file
    * @generated
    */
   @Override
   public String getEcoreFileName() {
     return "bz399086.ecore";
   }
 
   /**
    * Returns the {@link EClass} '<em><b>MapElement</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return an instance of the {@link EClass} '<em><b>MapElement</b></em>'
    * @generated
    */
   public EClass getMapElementEClass() {
     return (EClass) getEPackage().getEClassifiers().get(MAPELEMENT_CLASSIFIER_ID);
   }
 
   /**
    * Returns the {@link EStructuralFeature} '<em><b>MapElement.name</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc
    * -->
    * 
    * @return an instance of the {@link EStructuralFeature}: '<em><b>MapElement.name</b></em>'.
    * @generated
    */
   public EAttribute getMapElement_Name() {
     return (EAttribute) getMapElementEClass().getEAllStructuralFeatures().get(MAPELEMENT_NAME_FEATURE_ID);
   }
 
   /**
    * Returns the {@link EClass} '<em><b>MapRefEntry</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return an instance of the {@link EClass} '<em><b>MapRefEntry</b></em>'
    * @generated
    */
   public EClass getMapRefEntryEClass() {
     return (EClass) getEPackage().getEClassifiers().get(MAPREFENTRY_CLASSIFIER_ID);
   }
 
   /**
    * Returns the {@link EStructuralFeature} '<em><b>MapRefEntry.key</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc
    * -->
    * 
    * @return an instance of the {@link EStructuralFeature}: '<em><b>MapRefEntry.key</b></em>'.
    * @generated
    */
   public EAttribute getMapRefEntry_Key() {
     return (EAttribute) getMapRefEntryEClass().getEAllStructuralFeatures().get(MAPREFENTRY_KEY_FEATURE_ID);
   }
 
   /**
    * Returns the {@link EStructuralFeature} '<em><b>MapRefEntry.value</b></em>'. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @return an instance of the {@link EStructuralFeature}: '<em><b>MapRefEntry.value</b></em>'.
    * @generated
    */
   public EReference getMapRefEntry_Value() {
     return (EReference) getMapRefEntryEClass().getEAllStructuralFeatures().get(MAPREFENTRY_VALUE_FEATURE_ID);
   }
 
   /**
    * Returns the {@link EClass} '<em><b>MapValueEntry</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return an instance of the {@link EClass} '<em><b>MapValueEntry</b></em>'
    * @generated
    */
   public EClass getMapValueEntryEClass() {
     return (EClass) getEPackage().getEClassifiers().get(MAPVALUEENTRY_CLASSIFIER_ID);
   }
 
   /**
    * Returns the {@link EStructuralFeature} '<em><b>MapValueEntry.key</b></em>'. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @return an instance of the {@link EStructuralFeature}: '<em><b>MapValueEntry.key</b></em>'.
    * @generated
    */
   public EAttribute getMapValueEntry_Key() {
     return (EAttribute) getMapValueEntryEClass().getEAllStructuralFeatures().get(MAPVALUEENTRY_KEY_FEATURE_ID);
   }
 
   /**
    * Returns the {@link EStructuralFeature} '<em><b>MapValueEntry.value</b></em>'. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @return an instance of the {@link EStructuralFeature}: '<em><b>MapValueEntry.value</b></em>'.
    * @generated
    */
   public EAttribute getMapValueEntry_Value() {
     return (EAttribute) getMapValueEntryEClass().getEAllStructuralFeatures().get(MAPVALUEENTRY_VALUE_FEATURE_ID);
   }
 
   /**
    * Returns the {@link EClass} '<em><b>TheMap</b></em>'. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @return an instance of the {@link EClass} '<em><b>TheMap</b></em>'
    * @generated
    */
   public EClass getTheMapEClass() {
     return (EClass) getEPackage().getEClassifiers().get(THEMAP_CLASSIFIER_ID);
   }
 
   /**
    * Returns the {@link EStructuralFeature} '<em><b>TheMap.refContent</b></em>'. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @return an instance of the {@link EStructuralFeature}: '<em><b>TheMap.refContent</b></em>'.
    * @generated
    */
   public EReference getTheMap_RefContent() {
     return (EReference) getTheMapEClass().getEAllStructuralFeatures().get(THEMAP_REFCONTENT_FEATURE_ID);
   }
 
   /**
    * Returns the {@link EStructuralFeature} '<em><b>TheMap.valueContent</b></em>'. <!-- begin-user-doc --> <!--
    * end-user-doc -->
    * 
    * @return an instance of the {@link EStructuralFeature}: '<em><b>TheMap.valueContent</b></em>'.
    * @generated
    */
   public EReference getTheMap_ValueContent() {
     return (EReference) getTheMapEClass().getEAllStructuralFeatures().get(THEMAP_VALUECONTENT_FEATURE_ID);
   }
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param eClassifier
    *          the {@link EClassifier}
    * @return the class implementing a specific {@link EClass}.
    * @generated
    */
   @Override
   public Class<?> getEClassifierClass(EClassifier eClassifier) {
     switch (eClassifier.getClassifierID()) {
     case MAPELEMENT_CLASSIFIER_ID:
       return MapElement.class;
     case THEMAP_CLASSIFIER_ID:
       return TheMap.class;
     default:
       throw new IllegalArgumentException("The EClassifier '" + eClassifier + "' is not defined in this EPackage");
     }
   }
 }
