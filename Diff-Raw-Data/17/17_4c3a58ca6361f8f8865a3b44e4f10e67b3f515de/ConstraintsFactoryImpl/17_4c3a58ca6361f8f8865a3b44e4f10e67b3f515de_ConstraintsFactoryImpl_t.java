 /**
  * <copyright>
  * </copyright>
  *
 * $Id: ConstraintsFactoryImpl.java,v 1.2 2007/03/22 23:56:16 cbateman Exp $
  */
 package org.eclipse.jst.jsf.validation.internal.constraints.impl;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 
 import org.eclipse.emf.ecore.impl.EFactoryImpl;
 
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 
 import org.eclipse.jst.jsf.validation.internal.constraints.*;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Factory</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class ConstraintsFactoryImpl extends EFactoryImpl implements ConstraintsFactory {
     /**
      * Creates the default factory implementation.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
     * @return the constraints factory
      * @generated
      */
     public static ConstraintsFactory init() {
         try {
             ConstraintsFactory theConstraintsFactory = (ConstraintsFactory)EPackage.Registry.INSTANCE.getEFactory("http://org.eclipse.jst.jsf.core/constraints.ecore"); 
             if (theConstraintsFactory != null) {
                 return theConstraintsFactory;
             }
         }
         catch (Exception exception) {
             EcorePlugin.INSTANCE.log(exception);
         }
         return new ConstraintsFactoryImpl();
     }
 
     /**
      * Creates an instance of the factory.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     public ConstraintsFactoryImpl() {
         super();
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
     * @param eClass 
     * @return the eobject for the class
      * @generated
      */
     public EObject create(EClass eClass) {
         switch (eClass.getClassifierID()) {
             case ConstraintsPackage.TAG_ID: return createTagId();
             case ConstraintsPackage.TAG_SET: return createTagSet();
             case ConstraintsPackage.CONTAINS_TAG_CONSTRAINT: return createContainsTagConstraint();
             default:
                 throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
         }
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
     * @return a new tag id
      * @generated
      */
     public TagId createTagId() {
         TagIdImpl tagId = new TagIdImpl();
         return tagId;
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
     * @return a new tag set
      * @generated
      */
     public TagSet createTagSet() {
         TagSetImpl tagSet = new TagSetImpl();
         return tagSet;
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
     * @return a new Contains Tag Constraint
      * @generated
      */
     public ContainsTagConstraint createContainsTagConstraint() {
         ContainsTagConstraintImpl containsTagConstraint = new ContainsTagConstraintImpl();
         return containsTagConstraint;
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
     * @return the constraints package
      * @generated
      */
     public ConstraintsPackage getConstraintsPackage() {
         return (ConstraintsPackage)getEPackage();
     }
 
     /**
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
     * @return the package
      * @deprecated
      * @generated
      */
     public static ConstraintsPackage getPackage() {
         return ConstraintsPackage.eINSTANCE;
     }
 
 } //ConstraintsFactoryImpl
