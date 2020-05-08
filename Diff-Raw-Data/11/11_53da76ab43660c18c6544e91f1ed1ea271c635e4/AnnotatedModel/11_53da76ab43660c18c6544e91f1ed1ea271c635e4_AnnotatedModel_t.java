 /**
  * <copyright>
  * </copyright>
  *
  * $Id: AnnotatedModel.java,v 1.15 2011/09/21 09:37:39 mtaal Exp $
  */
 package org.eclipse.emf.texo.annotations.annotationsmodel;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.texo.annotations.AnnotationModelSuffixHandler;
 import org.eclipse.emf.texo.annotations.annotationsmodel.util.AnnotationsModelRegistry;
 
 /**
  * <!-- begin-user-doc --> A representation of the model object '<em><b>Annotated Model</b></em>'. <!-- end-user-doc -->
  * 
  * <p>
  * The following features are supported:
  * <ul>
  * <li>{@link org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedModel#getAnnotatedEPackages <em>Annotated
  * EPackages</em>}</li>
  * <li>{@link org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedModel#isGeneratingSources <em>Generating
  * Sources</em>}</li>
  * </ul>
  * </p>
  * 
  * @see org.eclipse.emf.texo.annotations.annotationsmodel.AnnotationsmodelPackage#getAnnotatedModel()
  * @model kind="class"
  * @generated
  */
 public class AnnotatedModel extends EObjectImpl implements EObject {
   /**
    * The cached value of the '{@link #getAnnotatedEPackages() <em>Annotated EPackages</em>}' containment reference list.
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #getAnnotatedEPackages()
    * @generated
    * @ordered
    */
   protected EList<AnnotatedEPackage> annotatedEPackages;
 
   /**
    * The default value of the '{@link #isGeneratingSources() <em>Generating Sources</em>}' attribute. <!--
    * begin-user-doc --> <!-- end-user-doc -->
    * 
    * @see #isGeneratingSources()
    * @generated
    * @ordered
    */
   protected static final boolean GENERATING_SOURCES_EDEFAULT = false;
 
   /**
    * The cached value of the '{@link #isGeneratingSources() <em>Generating Sources</em>}' attribute. <!-- begin-user-doc
    * --> <!-- end-user-doc -->
    * 
    * @see #isGeneratingSources()
    * @generated
    * @ordered
    */
   protected boolean generatingSources = GENERATING_SOURCES_EDEFAULT;
 
   // keeps track for which epackages the annotation model files have already been read
   protected List<EPackage> readAnnotationModelEPackages = new ArrayList<EPackage>();
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   protected AnnotatedModel() {
     super();
   }
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   protected EClass eStaticClass() {
     return AnnotationsmodelPackage.Literals.ANNOTATED_MODEL;
   }
 
   /**
    * Returns the value of the '<em><b>Annotated EPackages</b></em>' containment reference list. The list contents are of
    * type {@link org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedEPackage}. It is bidirectional and its
    * opposite is '{@link org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedEPackage#getAnnotatedModel
    * <em>Annotated Model</em>}'. <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Annotated EPackages</em>' containment reference list isn't clear, there really should be
    * more of a description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Annotated EPackages</em>' containment reference list.
    * @see org.eclipse.emf.texo.annotations.annotationsmodel.AnnotationsmodelPackage#getAnnotatedModel_AnnotatedEPackages()
    * @see org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedEPackage#getAnnotatedModel
    * @model opposite="annotatedModel" containment="true"
    * @generated
    */
   public EList<AnnotatedEPackage> getAnnotatedEPackages() {
     if (annotatedEPackages == null) {
       annotatedEPackages = new EObjectContainmentWithInverseEList<AnnotatedEPackage>(AnnotatedEPackage.class, this,
           AnnotationsmodelPackage.ANNOTATED_MODEL__ANNOTATED_EPACKAGES,
           AnnotationsmodelPackage.ANNOTATED_EPACKAGE__ANNOTATED_MODEL);
     }
     return annotatedEPackages;
   }
 
   /**
    * Returns the value of the '<em><b>Generating Sources</b></em>' attribute. <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Generating Sources</em>' attribute isn't clear, there really should be more of a
    * description here...
    * </p>
    * <!-- end-user-doc -->
    * 
    * @return the value of the '<em>Generating Sources</em>' attribute.
    * @see #setGeneratingSources(boolean)
    * @see org.eclipse.emf.texo.annotations.annotationsmodel.AnnotationsmodelPackage#getAnnotatedModel_GeneratingSources()
    * @model
    * @generated
    */
   public boolean isGeneratingSources() {
     return generatingSources;
   }
 
   /**
    * Sets the value of the '{@link org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedModel#isGeneratingSources
    * <em>Generating Sources</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @param value
    *          the new value of the '<em>Generating Sources</em>' attribute.
    * @see #isGeneratingSources()
    * @generated
    */
   public void setGeneratingSources(boolean newGeneratingSources) {
     boolean oldGeneratingSources = generatingSources;
     generatingSources = newGeneratingSources;
     if (eNotificationRequired()) {
       eNotify(new ENotificationImpl(this, Notification.SET,
           AnnotationsmodelPackage.ANNOTATED_MODEL__GENERATING_SOURCES, oldGeneratingSources, generatingSources));
     }
   }
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @SuppressWarnings("unchecked")
   @Override
   public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
     switch (featureID) {
     case AnnotationsmodelPackage.ANNOTATED_MODEL__ANNOTATED_EPACKAGES:
       return ((InternalEList<InternalEObject>) (InternalEList<?>) getAnnotatedEPackages()).basicAdd(otherEnd, msgs);
     }
     return super.eInverseAdd(otherEnd, featureID, msgs);
   }
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
     switch (featureID) {
     case AnnotationsmodelPackage.ANNOTATED_MODEL__ANNOTATED_EPACKAGES:
       return ((InternalEList<?>) getAnnotatedEPackages()).basicRemove(otherEnd, msgs);
     }
     return super.eInverseRemove(otherEnd, featureID, msgs);
   }
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public Object eGet(int featureID, boolean resolve, boolean coreType) {
     switch (featureID) {
     case AnnotationsmodelPackage.ANNOTATED_MODEL__ANNOTATED_EPACKAGES:
       return getAnnotatedEPackages();
     case AnnotationsmodelPackage.ANNOTATED_MODEL__GENERATING_SOURCES:
       return isGeneratingSources();
     }
     return super.eGet(featureID, resolve, coreType);
   }
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @SuppressWarnings("unchecked")
   @Override
   public void eSet(int featureID, Object newValue) {
     switch (featureID) {
     case AnnotationsmodelPackage.ANNOTATED_MODEL__ANNOTATED_EPACKAGES:
       getAnnotatedEPackages().clear();
       getAnnotatedEPackages().addAll((Collection<? extends AnnotatedEPackage>) newValue);
       return;
     case AnnotationsmodelPackage.ANNOTATED_MODEL__GENERATING_SOURCES:
       setGeneratingSources((Boolean) newValue);
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
     switch (featureID) {
     case AnnotationsmodelPackage.ANNOTATED_MODEL__ANNOTATED_EPACKAGES:
       getAnnotatedEPackages().clear();
       return;
     case AnnotationsmodelPackage.ANNOTATED_MODEL__GENERATING_SOURCES:
       setGeneratingSources(GENERATING_SOURCES_EDEFAULT);
       return;
     }
     super.eUnset(featureID);
   }
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public boolean eIsSet(int featureID) {
     switch (featureID) {
     case AnnotationsmodelPackage.ANNOTATED_MODEL__ANNOTATED_EPACKAGES:
       return annotatedEPackages != null && !annotatedEPackages.isEmpty();
     case AnnotationsmodelPackage.ANNOTATED_MODEL__GENERATING_SOURCES:
       return generatingSources != GENERATING_SOURCES_EDEFAULT;
     }
     return super.eIsSet(featureID);
   }
 
   /**
    * <!-- begin-user-doc --> <!-- end-user-doc -->
    * 
    * @generated
    */
   @Override
   public String toString() {
     if (eIsProxy()) {
       return super.toString();
     }
 
     StringBuffer result = new StringBuffer(super.toString());
     result.append(" (generatingSources: ");
     result.append(generatingSources);
     result.append(')');
     return result.toString();
   }
 
   public AnnotatedENamedElement getAnnotatedENamedElement(ENamedElement eNamedElement, boolean create) {
     if (eNamedElement instanceof EPackage) {
       return getAnnotatedEPackage((EPackage) eNamedElement, create);
     } else if (eNamedElement instanceof EClass) {
       return getAnnotatedEClass((EClass) eNamedElement, create);
     } else if (eNamedElement instanceof EEnum) {
       return getAnnotatedEEnum((EEnum) eNamedElement, create);
     } else if (eNamedElement instanceof EEnumLiteral) {
       return getAnnotatedEEnumLiteral((EEnumLiteral) eNamedElement, create);
     } else if (eNamedElement instanceof EDataType) {
       return getAnnotatedEDataType((EDataType) eNamedElement, create);
     } else if (eNamedElement instanceof EReference) {
       return getAnnotatedEReference((EReference) eNamedElement, create);
     } else if (eNamedElement instanceof EAttribute) {
       return getAnnotatedEAttribute((EAttribute) eNamedElement, create);
     } else {
       throw new IllegalArgumentException("EModelElement " + eNamedElement + " not supported here");
     }
 
   }
 
   public AnnotatedEPackage getAnnotatedEPackage(EPackage ePackage, boolean create) {
     if (ePackage == null) {
       return null;
     }
     for (AnnotatedEPackage aPackage : getAnnotatedEPackages()) {
       if (aPackage.getEPackage() == ePackage) {
         return aPackage;
       }
     }
 
     // not there, try to find an annotation model somewhere out there...
     if (!readAnnotationModelEPackages.contains(ePackage) && ePackage.eResource() != null
         && ePackage.eResource().getURI() != null) {
       readAnnotationModelEPackages.add(ePackage);
       final ResourceSet resourceSet = ePackage.eResource().getResourceSet();
 
       if (resourceSet != null) {
         // make sure that all annotation model epackages are known
         for (EPackage annotationModelPackage : AnnotationsModelRegistry.getInstance().getAnnotationModels()) {
           resourceSet.getPackageRegistry().put(annotationModelPackage.getNsURI(), annotationModelPackage);
         }
 
         readAnnotatedModel(ePackage, resourceSet);
 
         // search again, won't get into an infinite loop because of the
         // readAnnotationModelEPackages list
         return getAnnotatedEPackage(ePackage, create);
       }
     }
 
     if (create) {
       final AnnotatedEPackage aPackage = AnnotationsmodelFactory.eINSTANCE.createAnnotatedEPackage();
       aPackage.setEPackage(ePackage);
       getAnnotatedEPackages().add(aPackage);
       return aPackage;
     }
     return null;
   }
 
   protected void readAnnotatedModel(EPackage ePackage, ResourceSet resourceSet) {
 
     final URI uri = ePackage.eResource().getURI();
     for (String suffix : AnnotationModelSuffixHandler.getInstance().getSuffixes()) {
      boolean resourcePresent = false;
       try {
         final URI annotationsModelURI = AnnotationModelSuffixHandler.createAnnotationsModelURIWithSuffix(uri, suffix);
 
         final Resource res = resourceSet.getResource(annotationsModelURI, true);
 
         final TreeIterator<EObject> iterator = res.getAllContents();
        resourcePresent = true;
         while (iterator.hasNext()) {
           final EObject eObject = iterator.next();
           if (eObject instanceof AnnotatedEPackage) {
             mergeAnnotatedEPackage((AnnotatedEPackage) EcoreUtil.copy(eObject));
           }
         }
       } catch (Exception e) {
        // rethrow if the exception occurred after resource loading
        if (resourcePresent) {
           throw new IllegalStateException(e);
         }
       }
 
     }
 
   }
 
   protected void mergeAnnotatedEPackage(AnnotatedEPackage sourceAPackage) {
     final AnnotatedEPackage targetAPackage = getAnnotatedEPackage(sourceAPackage.getEPackage(), true);
     // do new arraylist to prevent concurrent modification
     for (EPackageAnnotation ePackageAnnotation : new ArrayList<EPackageAnnotation>(
         sourceAPackage.getEPackageAnnotations())) {
       if (!hasAnnotation(targetAPackage.getEPackageAnnotations(), ePackageAnnotation)) {
         targetAPackage.getEPackageAnnotations().add(ePackageAnnotation);
       }
     }
 
     // now move on to the annotated eclasses/edatatypes etc.
     for (AnnotatedEClassifier aClassifier : sourceAPackage.getAnnotatedEClassifiers()) {
       if (aClassifier instanceof AnnotatedEClass) {
         mergeAnnotatedEClass((AnnotatedEClass) aClassifier);
       } else if (aClassifier instanceof AnnotatedEEnum) {
         mergeAnnotatedEEnum((AnnotatedEEnum) aClassifier);
       } else {
         mergeAnnotatedEDataType((AnnotatedEDataType) aClassifier);
       }
     }
   }
 
   private void mergeAnnotatedEEnum(AnnotatedEEnum sourceAEnum) {
     final AnnotatedEEnum targetAEnum = getAnnotatedEEnum(sourceAEnum.getEEnum(), true);
     for (EEnumAnnotation eEnumAnnotation : new ArrayList<EEnumAnnotation>(sourceAEnum.getEEnumAnnotations())) {
       if (!hasAnnotation(targetAEnum.getAllAnnotations(), eEnumAnnotation)) {
         targetAEnum.getEEnumAnnotations().add(eEnumAnnotation);
       }
     }
 
     // copy the literals
     for (AnnotatedEEnumLiteral sourceALiteral : sourceAEnum.getAnnotatedEEnumLiterals()) {
       final AnnotatedEEnumLiteral targetALiteral = getAnnotatedEEnumLiteral(sourceALiteral.getEEnumLiteral(), true);
       for (EEnumLiteralAnnotation eLiteralAnnotation : new ArrayList<EEnumLiteralAnnotation>(
           sourceALiteral.getEEnumLiteralAnnotations())) {
         if (!hasAnnotation(targetALiteral.getAllAnnotations(), eLiteralAnnotation)) {
           targetALiteral.getEEnumLiteralAnnotations().add(eLiteralAnnotation);
         }
       }
     }
   }
 
   private void mergeAnnotatedEDataType(AnnotatedEDataType sourceADataType) {
     final AnnotatedEDataType targetADataType = getAnnotatedEDataType(sourceADataType.getEDataType(), true);
 
     for (EDataTypeAnnotation eDataTypeAnnotation : new ArrayList<EDataTypeAnnotation>(
         sourceADataType.getEDataTypeAnnotations())) {
       if (!hasAnnotation(targetADataType.getAllAnnotations(), eDataTypeAnnotation)) {
         targetADataType.getEDataTypeAnnotations().add(eDataTypeAnnotation);
       }
     }
   }
 
   private void mergeAnnotatedEClass(AnnotatedEClass sourceAClass) {
     final AnnotatedEClass targetAClass = getAnnotatedEClass(sourceAClass.getEClass(), true);
 
     for (EClassAnnotation eClassAnnotation : new ArrayList<EClassAnnotation>(sourceAClass.getEClassAnnotations())) {
       if (!hasAnnotation(targetAClass.getAllAnnotations(), eClassAnnotation)) {
         targetAClass.getEClassAnnotations().add(eClassAnnotation);
       }
     }
 
     for (AnnotatedEStructuralFeature aFeature : sourceAClass.getAnnotatedEStructuralFeatures()) {
       if (aFeature instanceof AnnotatedEReference) {
         final AnnotatedEReference sourceAReference = (AnnotatedEReference) aFeature;
         final AnnotatedEReference targetAReference = getAnnotatedEReference(sourceAReference.getEReference(), true);
         // can happen if the annotations model is not up-to-date and contains illegal references to the model
         if (targetAReference == null) {
           continue;
         }
         for (EReferenceAnnotation eReferenceAnnotation : new ArrayList<EReferenceAnnotation>(
             sourceAReference.getEReferenceAnnotations())) {
           if (!hasAnnotation(targetAReference.getEReferenceAnnotations(), eReferenceAnnotation)) {
             targetAReference.getEReferenceAnnotations().add(eReferenceAnnotation);
           }
         }
       } else {
         final AnnotatedEAttribute sourceAAttribute = (AnnotatedEAttribute) aFeature;
         final AnnotatedEAttribute targetAAttribute = getAnnotatedEAttribute(sourceAAttribute.getEAttribute(), true);
         // can happen if the annotations model is not up-to-date and contains illegal references to the model
         if (targetAAttribute == null) {
           continue;
         }
         for (EAttributeAnnotation eAttributeAnnotation : new ArrayList<EAttributeAnnotation>(
             sourceAAttribute.getEAttributeAnnotations())) {
           if (!hasAnnotation(targetAAttribute.getEAttributeAnnotations(), eAttributeAnnotation)) {
             targetAAttribute.getEAttributeAnnotations().add(eAttributeAnnotation);
           }
         }
       }
     }
   }
 
   private boolean hasAnnotation(List<?> annotations, ENamedElementAnnotation annotation) {
     for (Object annObject : annotations) {
       final ENamedElementAnnotation elementAnnotation = (ENamedElementAnnotation) annObject;
       if (elementAnnotation.eClass() == annotation.eClass()) {
         return true;
       }
     }
     return false;
   }
 
   public AnnotatedEClass getAnnotatedEClass(EClass eClass, boolean create) {
     if (eClass == null) {
       return null;
     }
     final AnnotatedEPackage aPackage = getAnnotatedEPackage(eClass.getEPackage(), create);
     if (aPackage == null) {
       return null;
     }
     for (AnnotatedEClassifier aClassifier : aPackage.getAnnotatedEClassifiers()) {
       if (aClassifier instanceof AnnotatedEClass) {
         final AnnotatedEClass aClass = (AnnotatedEClass) aClassifier;
         if (aClass.getEClass() == eClass) {
           return aClass;
         }
       }
     }
     if (create) {
       final AnnotatedEClass aClass = AnnotationsmodelFactory.eINSTANCE.createAnnotatedEClass();
       aPackage.getAnnotatedEClassifiers().add(aClass);
       aClass.setEClass(eClass);
       return aClass;
     }
     return null;
   }
 
   public AnnotatedEClassifier getAnnotatedEClassifier(EClassifier eClassifier, boolean create) {
     if (eClassifier instanceof EClass) {
       return getAnnotatedEClass((EClass) eClassifier, create);
     } else if (eClassifier instanceof EEnum) {
       return getAnnotatedEEnum((EEnum) eClassifier, create);
     }
     return getAnnotatedEDataType((EDataType) eClassifier, create);
   }
 
   public AnnotatedEDataType getAnnotatedEDataType(EDataType eDataType, boolean create) {
     if (eDataType == null) {
       return null;
     }
     final AnnotatedEPackage aPackage = getAnnotatedEPackage(eDataType.getEPackage(), create);
     if (aPackage == null) {
       return null;
     }
     for (AnnotatedEClassifier aClassifier : aPackage.getAnnotatedEClassifiers()) {
       if (aClassifier instanceof AnnotatedEDataType) {
         final AnnotatedEDataType aDataType = (AnnotatedEDataType) aClassifier;
         if (aDataType.getEDataType() == eDataType) {
           return aDataType;
         }
       }
     }
     if (create) {
       final AnnotatedEDataType aDataType = AnnotationsmodelFactory.eINSTANCE.createAnnotatedEDataType();
       aPackage.getAnnotatedEClassifiers().add(aDataType);
       aDataType.setEDataType(eDataType);
       return aDataType;
     }
     return null;
   }
 
   public AnnotatedEEnum getAnnotatedEEnum(EEnum eEnum, boolean create) {
     if (eEnum == null) {
       return null;
     }
     final AnnotatedEPackage aPackage = getAnnotatedEPackage(eEnum.getEPackage(), create);
     if (aPackage == null) {
       return null;
     }
     for (AnnotatedEClassifier aClassifier : aPackage.getAnnotatedEClassifiers()) {
       if (aClassifier instanceof AnnotatedEEnum) {
         final AnnotatedEEnum aEnum = (AnnotatedEEnum) aClassifier;
         if (aEnum.getEEnum() == eEnum) {
           return aEnum;
         }
       }
     }
     if (create) {
       final AnnotatedEEnum aEnum = AnnotationsmodelFactory.eINSTANCE.createAnnotatedEEnum();
       aPackage.getAnnotatedEClassifiers().add(aEnum);
       aEnum.setEEnum(eEnum);
       return aEnum;
     }
     return null;
   }
 
   public AnnotatedEEnumLiteral getAnnotatedEEnumLiteral(EEnumLiteral eEnumLiteral, boolean create) {
     if (eEnumLiteral == null) {
       return null;
     }
     final AnnotatedEEnum aEnum = getAnnotatedEEnum(eEnumLiteral.getEEnum(), create);
     for (AnnotatedEEnumLiteral aLiteral : aEnum.getAnnotatedEEnumLiterals()) {
       if (aLiteral.getEEnumLiteral().equals(eEnumLiteral)) {
         return aLiteral;
       }
     }
     if (create) {
       final AnnotatedEEnumLiteral aLiteral = AnnotationsmodelFactory.eINSTANCE.createAnnotatedEEnumLiteral();
       aEnum.getAnnotatedEEnumLiterals().add(aLiteral);
       aLiteral.setEEnumLiteral(eEnumLiteral);
       return aLiteral;
     }
     return null;
   }
 
   public AnnotatedEReference getAnnotatedEReference(EReference eReference, boolean create) {
     if (eReference.getEContainingClass() == null) {
       return null;
     }
     final AnnotatedEClass aClass = getAnnotatedEClass(eReference.getEContainingClass(), create);
     if (aClass == null) {
       return null;
     }
     for (AnnotatedEStructuralFeature aFeature : aClass.getAnnotatedEStructuralFeatures()) {
       if (aFeature instanceof AnnotatedEReference) {
         final AnnotatedEReference aReference = (AnnotatedEReference) aFeature;
         if (aReference.getEReference() == eReference) {
           return aReference;
         }
       }
     }
     if (create) {
       final AnnotatedEReference aReference = AnnotationsmodelFactory.eINSTANCE.createAnnotatedEReference();
       aClass.getAnnotatedEStructuralFeatures().add(aReference);
       aReference.setEReference(eReference);
       return aReference;
     }
     return null;
   }
 
   public AnnotatedEAttribute getAnnotatedEAttribute(EAttribute eAttribute, boolean create) {
     if (eAttribute.getEContainingClass() == null) {
       return null;
     }
     final AnnotatedEClass aClass = getAnnotatedEClass(eAttribute.getEContainingClass(), create);
     if (aClass == null) {
       return null;
     }
     for (AnnotatedEStructuralFeature aFeature : aClass.getAnnotatedEStructuralFeatures()) {
       if (aFeature instanceof AnnotatedEAttribute) {
         final AnnotatedEAttribute aAttribute = (AnnotatedEAttribute) aFeature;
         if (aAttribute.getEAttribute() == eAttribute) {
           return aAttribute;
         }
       }
     }
     if (create) {
       final AnnotatedEAttribute aAttribute = AnnotationsmodelFactory.eINSTANCE.createAnnotatedEAttribute();
       aClass.getAnnotatedEStructuralFeatures().add(aAttribute);
       aAttribute.setEAttribute(eAttribute);
       return aAttribute;
     }
     return null;
   }
 
 } // AnnotatedModel
