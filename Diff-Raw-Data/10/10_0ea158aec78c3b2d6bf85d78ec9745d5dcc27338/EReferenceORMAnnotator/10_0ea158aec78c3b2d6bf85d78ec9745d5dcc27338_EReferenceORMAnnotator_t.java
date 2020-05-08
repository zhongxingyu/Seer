 /**
  * <copyright>
  *
  * Copyright (c) 2009, 2010 Springsite BV (The Netherlands) and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Martin Taal - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: EReferenceORMAnnotator.java,v 1.20 2011/08/26 05:34:12 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.orm.annotator;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.texo.generator.Annotator;
 import org.eclipse.emf.texo.generator.GeneratorUtils;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.EClassModelGenAnnotation;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.EClassifierModelGenAnnotation;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.EReferenceModelGenAnnotation;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.ModelcodegeneratorPackage;
 import org.eclipse.emf.texo.orm.annotations.model.orm.CollectionTable;
 import org.eclipse.emf.texo.orm.annotations.model.orm.ElementCollection;
 import org.eclipse.emf.texo.orm.annotations.model.orm.JoinColumn;
 import org.eclipse.emf.texo.orm.annotations.model.orm.JoinTable;
 import org.eclipse.emf.texo.orm.annotations.model.orm.ManyToMany;
 import org.eclipse.emf.texo.orm.annotations.model.orm.ManyToOne;
 import org.eclipse.emf.texo.orm.annotations.model.orm.MapKeyClass;
 import org.eclipse.emf.texo.orm.annotations.model.orm.MapKeyColumn;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OneToMany;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OneToOne;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OrderColumn;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OrmFactory;
 import org.eclipse.emf.texo.orm.annotations.model.orm.UniqueConstraint;
 import org.eclipse.emf.texo.orm.ormannotations.EPackageORMAnnotation;
 import org.eclipse.emf.texo.orm.ormannotations.EReferenceORMAnnotation;
 import org.eclipse.emf.texo.orm.ormannotations.OrmannotationsPackage;
 import org.eclipse.emf.texo.utils.ModelUtils;
 
 /**
  * Responsible for creating the {@link EReferenceORMAnnotation}.
  * 
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.20 $
  */
 
 public class EReferenceORMAnnotator extends EStructuralFeatureORMAnnotator implements
     Annotator<EReferenceORMAnnotation> {
 
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.emf.texo.generator.Annotator#annotate(org.eclipse.emf.texo.annotations.annotationsmodel
    * .ENamedElementAnnotation)
    */
   public void setAnnotationFeatures(EReferenceORMAnnotation annotation) {
     // don't do anything anymore if transient
     if (annotation.getTransient() != null) {
       if (GeneratorUtils.isEmptyOrNull(annotation.getTransient().getName())) {
         annotation.getTransient().setName(getName(annotation.getEReference()));
       }
       return;
     }
 
     final EReference eReference = annotation.getEReference();
     final EPackage ePackage = eReference.getEContainingClass().getEPackage();
     final EPackageORMAnnotation ePackageORMAnnotation = (EPackageORMAnnotation) getAnnotationManager().getAnnotation(
         ePackage, OrmannotationsPackage.eINSTANCE.getEPackageORMAnnotation());
 
     // features which are part of a featuremap are never mapped as many features
     final boolean isPartOfFeatureMap = GeneratorUtils.isPartOfGroup(annotation.getEReference());
 
     final EReference eOpposite = eReference.getEOpposite();
     if (!isPartOfFeatureMap && eReference.isMany()) {
 
       if (ModelUtils.isEMap(annotation.getEReference().getEReferenceType())) {
         mapMap(annotation);
       } else if (eOpposite != null && eOpposite.isMany()) {
         annotateManyToMany(annotation);
       } else if (eOpposite != null && !eOpposite.isMany()) {
         annotateOneToMany(annotation);
       } else if (eReference.isContainment() || annotation.getOneToMany() != null
           || !ePackageORMAnnotation.isUseJoinTablesForNonContainment()) {
         annotateOneToMany(annotation);
       } else {
         annotateManyToMany(annotation);
       }
     } else {
       if (eOpposite != null && !eOpposite.isMany()) {
         annotateOneToOne(annotation);
       } else {
         annotateManyToOne(annotation);
       }
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.emf.texo.generator.Annotator#postAnnotating(org.eclipse.emf.texo.annotations.
    * annotationsmodel.ENamedElementAnnotation)
    */
   public void postAnnotating(EReferenceORMAnnotation annotation) {
   }
 
   protected void annotateOneToMany(EReferenceORMAnnotation annotation) {
     final EReference eReference = annotation.getEReference();
     final EPackage ePackage = eReference.getEContainingClass().getEPackage();
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(ePackage);
     final EPackageORMAnnotation ePackageORMAnnotation = (EPackageORMAnnotation) getAnnotationManager().getAnnotation(
         ePackage, OrmannotationsPackage.eINSTANCE.getEPackageORMAnnotation());
 
     final OneToMany oneToMany;
     if (annotation.getOneToMany() != null) {
       oneToMany = annotation.getOneToMany();
     } else {
       oneToMany = OrmFactory.eINSTANCE.createOneToMany();
       annotation.setOneToMany(oneToMany);
     }
     if (oneToMany.getCascade() == null) {
       if (eReference.isContainment()) {
         oneToMany.setCascade(ePackageORMAnnotation.getDefaultCascadeContainment());
       } else {
         oneToMany.setCascade(ePackageORMAnnotation.getDefaultCascadeNonContainment());
       }
 
       // no defaults set, do something smart...
       if (oneToMany.getCascade() == null) {
         oneToMany.setCascade(OrmFactory.eINSTANCE.createCascadeType());
         if (eReference.isContainment()) {
           oneToMany.getCascade().setCascadeAll(OrmFactory.eINSTANCE.createEmptyType());
         } else {
           oneToMany.getCascade().setCascadeMerge(OrmFactory.eINSTANCE.createEmptyType());
           oneToMany.getCascade().setCascadePersist(OrmFactory.eINSTANCE.createEmptyType());
           oneToMany.getCascade().setCascadeRefresh(OrmFactory.eINSTANCE.createEmptyType());
         }
       }
     }
 
     if (GeneratorUtils.isEmptyOrNull(oneToMany.getName())) {
       oneToMany.setName(getName(eReference));
     }
 
     // the map entry value is an entity
     if (ModelUtils.isEMap(eReference.getEReferenceType())) {
       if (oneToMany.getJoinTable() == null) {
         final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
         oneToMany.setJoinTable(joinTable);
       }
       // explicitly set the join table name
       if (oneToMany.getJoinTable().getName() == null) {
         oneToMany.getJoinTable().setName(namingStrategy.getJoinTableName(eReference));
       }
 
       if (namingStrategy.isGenerateAllDBSchemaNames()) {
         addColumnsToJoinTable(namingStrategy, oneToMany.getJoinTable(), annotation);
       }
 
       final EStructuralFeature eFeature = eReference.getEReferenceType().getEStructuralFeature("key"); //$NON-NLS-1$
       final EClassifier referedClassifier = eFeature.getEType();
       final EClassifierModelGenAnnotation modelGenAnnotation = (EClassifierModelGenAnnotation) getAnnotationManager()
           .getAnnotation(referedClassifier, ModelcodegeneratorPackage.eINSTANCE.getEClassifierModelGenAnnotation());
 
       if (oneToMany.getMapKeyClass() == null) {
         final MapKeyClass mapKeyClass = OrmFactory.eINSTANCE.createMapKeyClass();
         mapKeyClass.setClass(modelGenAnnotation.getQualifiedClassName());
         oneToMany.setMapKeyClass(mapKeyClass);
       }
 
       if (oneToMany.getMapKeyColumn() == null) {
         final MapKeyColumn mapKeyColumn = OrmFactory.eINSTANCE.createMapKeyColumn();
         mapKeyColumn.setTable(oneToMany.getJoinTable().getName());
         if (namingStrategy.isGenerateAllDBSchemaNames()) {
           mapKeyColumn.setName(namingStrategy.getIndexColumnName(eReference));
         }
         oneToMany.setMapKeyColumn(mapKeyColumn);
       }
 
       // no need to do the rest
       return;
     }
 
     final EReferenceModelGenAnnotation eReferenceModelGenAnnotation = (EReferenceModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eReference, ModelcodegeneratorPackage.eINSTANCE.getEReferenceModelGenAnnotation());
 
     // set the order column, is always set on this side
     if (!ModelUtils.isEMap(eReference.getEReferenceType()) && oneToMany.getOrderBy() == null
         && eReferenceModelGenAnnotation.isUseList() && oneToMany.getOrderColumn() == null) {
       final OrderColumn orderColumn = OrmFactory.eINSTANCE.createOrderColumn();
       if (namingStrategy.isGenerateAllDBSchemaNames()) {
         orderColumn.setName(namingStrategy.getIndexColumnName(eReference));
       }
       oneToMany.setOrderColumn(orderColumn);
     }
 
     final boolean isOwner = eReferenceModelGenAnnotation.isUseList() || isOwner(eReference);
 
     // set mapped by
     // if a list then set the mapped by on the other side
     if (!isOwner && eReference.getEOpposite() != null && GeneratorUtils.isEmptyOrNull(oneToMany.getMappedBy())) {
       oneToMany.setMappedBy(getMappedBy(eReference));
     }
 
     // set target entity
     if (GeneratorUtils.isEmptyOrNull(oneToMany.getTargetEntity())) {
       oneToMany.setTargetEntity(getTargetEntity(eReference));
     }
 
     // now work on jointable or joincolumn
     if (isOwner && oneToMany.getJoinColumn().isEmpty() && oneToMany.getJoinTable() == null) {
       if (eReference.isContainment()) {
         if (ePackageORMAnnotation.isUseJoinTablesForContainment()) {
           final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
           oneToMany.setJoinTable(joinTable);
         } else {
           final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
           oneToMany.getJoinColumn().add(joinColumn);
         }
       } else {
         if (ePackageORMAnnotation.isUseJoinTablesForNonContainment()) {
           // if there is an opposite let the jointable be set on the other side
           final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
           oneToMany.setJoinTable(joinTable);
         } else {
           final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
           oneToMany.getJoinColumn().add(joinColumn);
         }
       }
     }
     if (oneToMany.getJoinTable() != null && namingStrategy.isGenerateAllDBSchemaNames()) {
       addColumnsToJoinTable(namingStrategy, oneToMany.getJoinTable(), annotation);
     }
 
     // set a unique name
     if (oneToMany.getJoinTable() != null && GeneratorUtils.isEmptyOrNull(oneToMany.getJoinTable().getName())) {
       oneToMany.getJoinTable().setName(namingStrategy.getJoinTableName(eReference));
     }
   }
 
   protected void mapMap(EReferenceORMAnnotation annotation) {
     final EClass mapEClass = annotation.getEReference().getEReferenceType();
     final EStructuralFeature valueEFeature = mapEClass.getEStructuralFeature("value"); //$NON-NLS-1$
     if (valueEFeature instanceof EReference) {
       annotateOneToMany(annotation);
     } else {
       mapPrimitiveCollectionMap(annotation);
     }
   }
 
   protected void mapPrimitiveCollectionMap(EReferenceORMAnnotation annotation) {
     final EReference eReference = annotation.getEReference();
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(eReference.getEContainingClass().getEPackage());
     final ElementCollection elementCollection;
     if (annotation.getElementCollection() == null) {
       elementCollection = OrmFactory.eINSTANCE.createElementCollection();
       annotation.setElementCollection(elementCollection);
     } else {
       elementCollection = annotation.getElementCollection();
     }
 
     if (elementCollection.getCollectionTable() == null) {
       final CollectionTable collectionTable = OrmFactory.eINSTANCE.createCollectionTable();
       elementCollection.setCollectionTable(collectionTable);
     }
     if (GeneratorUtils.isEmptyOrNull(elementCollection.getCollectionTable().getName())) {
       elementCollection.getCollectionTable().setName(namingStrategy.getJoinTableName(eReference));
     }
     elementCollection.setName(getName(eReference));
     annotation.setElementCollection(elementCollection);
   }
 
   protected void annotateOneToOne(EReferenceORMAnnotation annotation) {
     final EReference eReference = annotation.getEReference();
     final EPackage ePackage = eReference.getEContainingClass().getEPackage();
     final EPackageORMAnnotation ePackageORMAnnotation = (EPackageORMAnnotation) getAnnotationManager().getAnnotation(
         ePackage, OrmannotationsPackage.eINSTANCE.getEPackageORMAnnotation());
     boolean generatedOneToOne = false;
     final OneToOne oneToOne;
     if (annotation.getOneToOne() != null) {
       oneToOne = annotation.getOneToOne();
     } else {
       oneToOne = OrmFactory.eINSTANCE.createOneToOne();
       annotation.setOneToOne(oneToOne);
       generatedOneToOne = true;
     }
 
     if (GeneratorUtils.isEmptyOrNull(oneToOne.getName())) {
       oneToOne.setName(getName(eReference));
     }
 
     if (oneToOne.getCascade() == null) {
       if (eReference.isContainment()) {
         oneToOne.setCascade(ePackageORMAnnotation.getDefaultCascadeContainment());
       } else {
         oneToOne.setCascade(ePackageORMAnnotation.getDefaultCascadeNonContainment());
       }
 
       // no defaults set, do something smart...
       if (oneToOne.getCascade() == null) {
         oneToOne.setCascade(OrmFactory.eINSTANCE.createCascadeType());
         if (eReference.isContainment()) {
           oneToOne.getCascade().setCascadeAll(OrmFactory.eINSTANCE.createEmptyType());
         } else {
           oneToOne.getCascade().setCascadeMerge(OrmFactory.eINSTANCE.createEmptyType());
           oneToOne.getCascade().setCascadePersist(OrmFactory.eINSTANCE.createEmptyType());
           oneToOne.getCascade().setCascadeRefresh(OrmFactory.eINSTANCE.createEmptyType());
         }
       }
     }
 
     // set mapped by
     if (generatedOneToOne && !isOwner(eReference)) {
       oneToOne.setMappedBy(getMappedBy(eReference));
     }
 
     // set target entity
     if (GeneratorUtils.isEmptyOrNull(oneToOne.getTargetEntity())) {
       oneToOne.setTargetEntity(getTargetEntity(eReference));
     }
 
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(eReference.getEContainingClass().getEPackage());
     if (oneToOne.getJoinColumn() == null && namingStrategy.isGenerateAllDBSchemaNames()) {
       oneToOne.getJoinColumn().add(OrmFactory.eINSTANCE.createJoinColumn());
       oneToOne.getJoinColumn().get(0).setName(namingStrategy.getJoinColumnName(eReference));
     }
 
     oneToOne.setOptional(!eReference.isRequired());
   }
 
   protected void annotateManyToOne(EReferenceORMAnnotation annotation) {
     final EReference eReference = annotation.getEReference();
     final EPackage ePackage = eReference.getEContainingClass().getEPackage();
     final EPackageORMAnnotation ePackageORMAnnotation = (EPackageORMAnnotation) getAnnotationManager().getAnnotation(
         ePackage, OrmannotationsPackage.eINSTANCE.getEPackageORMAnnotation());
     final ManyToOne manyToOne;
     if (annotation.getManyToOne() != null) {
       manyToOne = annotation.getManyToOne();
     } else {
       manyToOne = OrmFactory.eINSTANCE.createManyToOne();
       annotation.setManyToOne(manyToOne);
     }
 
     if (GeneratorUtils.isEmptyOrNull(manyToOne.getName())) {
       manyToOne.setName(getName(eReference));
     }
 
     if (manyToOne.getCascade() == null) {
       if (eReference.isContainment()) {
         manyToOne.setCascade(ePackageORMAnnotation.getDefaultCascadeContainment());
       } else {
         manyToOne.setCascade(ePackageORMAnnotation.getDefaultCascadeNonContainment());
       }
 
       // no defaults set, do something smart...
       if (manyToOne.getCascade() == null) {
         manyToOne.setCascade(OrmFactory.eINSTANCE.createCascadeType());
         if (eReference.isContainment()) {
           manyToOne.getCascade().setCascadeAll(OrmFactory.eINSTANCE.createEmptyType());
         } else {
           manyToOne.getCascade().setCascadeMerge(OrmFactory.eINSTANCE.createEmptyType());
           manyToOne.getCascade().setCascadePersist(OrmFactory.eINSTANCE.createEmptyType());
           manyToOne.getCascade().setCascadeRefresh(OrmFactory.eINSTANCE.createEmptyType());
         }
       }
     }
 
     // set target entity
     if (GeneratorUtils.isEmptyOrNull(manyToOne.getTargetEntity())) {
       manyToOne.setTargetEntity(getTargetEntity(eReference));
     }
 
     manyToOne.setOptional(!eReference.isRequired());
 
     // now work on jointable or joincolumn
     if (manyToOne.getJoinColumn().isEmpty() && manyToOne.getJoinTable() == null) {
       // if the opposite is containment
       final EReference eOpposite = eReference.getEOpposite();
       if (eOpposite != null) {
         // if the opposite is a list then don't do anything
 
         final EReferenceModelGenAnnotation eOppositeModelGenAnnotation = (EReferenceModelGenAnnotation) getAnnotationManager()
             .getAnnotation(eOpposite, ModelcodegeneratorPackage.eINSTANCE.getEReferenceModelGenAnnotation());
         final boolean isOwner = !eOppositeModelGenAnnotation.isUseList() && isOwner(eReference);
 
         if (isOwner) {
           if (eOpposite.isContainment()) {
             if (ePackageORMAnnotation.isUseJoinTablesForContainment()) {
               final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
               manyToOne.setJoinTable(joinTable);
             } else {
               final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
               manyToOne.getJoinColumn().add(joinColumn);
             }
           } else if (ePackageORMAnnotation.isUseJoinTablesForNonContainment()) {
             final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
             manyToOne.setJoinTable(joinTable);
           } else {
             final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
             manyToOne.getJoinColumn().add(joinColumn);
           }
         } else if (eOppositeModelGenAnnotation.isUseList()) {
           final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
           manyToOne.getJoinColumn().add(joinColumn);
         }
       } else {
         final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
         manyToOne.getJoinColumn().add(joinColumn);
       }
     }
 
     // set a unique name
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(eReference.getEContainingClass().getEPackage());
     if (manyToOne.getJoinTable() != null && GeneratorUtils.isEmptyOrNull(manyToOne.getJoinTable().getName())) {
       manyToOne.getJoinTable().setName(namingStrategy.getJoinTableName(eReference));
       if (namingStrategy.isGenerateAllDBSchemaNames()) {
         addColumnsToJoinTable(namingStrategy, manyToOne.getJoinTable(), annotation);
       }
     }
     if (!manyToOne.getJoinColumn().isEmpty() && namingStrategy.isGenerateAllDBSchemaNames()) {
       manyToOne.getJoinColumn().get(0).setName(namingStrategy.getForeignKeyColumnName(eReference));
     }
   }
 
   protected void annotateManyToMany(EReferenceORMAnnotation annotation) {
     final EReference eReference = annotation.getEReference();
     final EPackage ePackage = eReference.getEContainingClass().getEPackage();
     final EPackageORMAnnotation ePackageORMAnnotation = (EPackageORMAnnotation) getAnnotationManager().getAnnotation(
         ePackage, OrmannotationsPackage.eINSTANCE.getEPackageORMAnnotation());
     final ManyToMany manyToMany;
     if (annotation.getManyToMany() != null) {
       manyToMany = annotation.getManyToMany();
     } else {
       manyToMany = OrmFactory.eINSTANCE.createManyToMany();
       annotation.setManyToMany(manyToMany);
     }
 
     if (GeneratorUtils.isEmptyOrNull(manyToMany.getName())) {
       manyToMany.setName(getName(eReference));
     }
 
     if (manyToMany.getCascade() == null) {
       if (eReference.isContainment()) {
         manyToMany.setCascade(ePackageORMAnnotation.getDefaultCascadeContainment());
       } else {
         manyToMany.setCascade(ePackageORMAnnotation.getDefaultCascadeNonContainment());
       }
 
       // no defaults set, do something smart...
       if (manyToMany.getCascade() == null) {
         manyToMany.setCascade(OrmFactory.eINSTANCE.createCascadeType());
         if (eReference.isContainment()) {
           manyToMany.getCascade().setCascadeAll(OrmFactory.eINSTANCE.createEmptyType());
         } else {
           manyToMany.getCascade().setCascadeMerge(OrmFactory.eINSTANCE.createEmptyType());
           manyToMany.getCascade().setCascadePersist(OrmFactory.eINSTANCE.createEmptyType());
           manyToMany.getCascade().setCascadeRefresh(OrmFactory.eINSTANCE.createEmptyType());
         }
       }
     }
 
     // set target entity
     if (GeneratorUtils.isEmptyOrNull(manyToMany.getTargetEntity())) {
       manyToMany.setTargetEntity(getTargetEntity(eReference));
     }
 
     // different cases:
     // 1) both sides are lists: 2 separate join tables
     // 2) one side is a list and one side a set: the list side is the owner
     // 3) both sides use a set, choose the owner
     final EReferenceModelGenAnnotation eReferenceModelGenAnnotation = (EReferenceModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eReference, ModelcodegeneratorPackage.eINSTANCE.getEReferenceModelGenAnnotation());
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(eReference.getEContainingClass().getEPackage());
 
     if (eReference.getEOpposite() != null) {
       final EReferenceModelGenAnnotation eOppositeModelGenAnnotation = (EReferenceModelGenAnnotation) getAnnotationManager()
           .getAnnotation(eReference.getEOpposite(),
               ModelcodegeneratorPackage.eINSTANCE.getEReferenceModelGenAnnotation());
 
       // if both sides have a list then use 2 join tables
       // or this is the owner
       if (eReferenceModelGenAnnotation.isUseList()) {
         if (manyToMany.getJoinTable() == null) {
           final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
           manyToMany.setJoinTable(joinTable);
         }
         if (manyToMany.getOrderColumn() == null && manyToMany.getOrderBy() == null) {
           final OrderColumn orderColumn = OrmFactory.eINSTANCE.createOrderColumn();
           if (namingStrategy.isGenerateAllDBSchemaNames()) {
             orderColumn.setName(namingStrategy.getIndexColumnName(eReference));
           }
           manyToMany.setOrderColumn(orderColumn);
         }
       } else if (eOppositeModelGenAnnotation.isUseList()) {
         // opposite is the owner
         manyToMany.setMappedBy(getMappedBy(eReference));
       } else if (isOwner(eReference)) {
         // this side is the owner add the join table
         if (manyToMany.getJoinTable() == null) {
           // create a join table
           final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
           manyToMany.setJoinTable(joinTable);
         }
       } else {
         // opposite is the owner
         manyToMany.setMappedBy(getMappedBy(eReference));
       }
     } else {
       if (manyToMany.getJoinTable() == null) {
         // create a join table
         final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
         manyToMany.setJoinTable(joinTable);
       }
       if (eReferenceModelGenAnnotation.isUseList() && manyToMany.getOrderBy() == null
           && manyToMany.getOrderColumn() == null) {
         final OrderColumn orderColumn = OrmFactory.eINSTANCE.createOrderColumn();
         if (namingStrategy.isGenerateAllDBSchemaNames()) {
           orderColumn.setName(namingStrategy.getIndexColumnName(eReference));
         }
         manyToMany.setOrderColumn(orderColumn);
       }
     }
 
     // set a unique name
     if (manyToMany.getJoinTable() != null && GeneratorUtils.isEmptyOrNull(manyToMany.getJoinTable().getName())) {
       manyToMany.getJoinTable().setName(namingStrategy.getJoinTableName(eReference));
       if (namingStrategy.isGenerateAllDBSchemaNames()) {
         addColumnsToJoinTable(namingStrategy, manyToMany.getJoinTable(), annotation);
       }
     }
   }
 
   private void addColumnsToJoinTable(ORMNamingStrategy namingStrategy, JoinTable joinTable,
       EReferenceORMAnnotation annotation) {
     EReference eReference = annotation.getEReference();
     final EReferenceModelGenAnnotation eReferenceModelGenAnnotation = (EReferenceModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eReference, ModelcodegeneratorPackage.eINSTANCE.getEReferenceModelGenAnnotation());
     final boolean isOwner = eReferenceModelGenAnnotation.isUseList() || isOwner(eReference);
     // build the join columns from the other side
     if (!isOwner) {
       eReference = eReference.getEOpposite();
     }
     if (joinTable.getJoinColumn().isEmpty()) {
       joinTable.getJoinColumn().add(OrmFactory.eINSTANCE.createJoinColumn());
       joinTable.getJoinColumn().get(0).setName(namingStrategy.getJoinColumnName(eReference));
     }
     if (joinTable.getInverseJoinColumn().isEmpty()) {
       joinTable.getInverseJoinColumn().add(OrmFactory.eINSTANCE.createJoinColumn());
       joinTable.getInverseJoinColumn().get(0).setName(namingStrategy.getInverseJoinColumnName(eReference));
     }
     if (eReference.isUnique()) {
       final UniqueConstraint uniqueConstraint = OrmFactory.eINSTANCE.createUniqueConstraint();
       for (JoinColumn joinColumn : joinTable.getJoinColumn()) {
         uniqueConstraint.getColumnName().add(joinColumn.getName());
       }
       for (JoinColumn joinColumn : joinTable.getInverseJoinColumn()) {
         uniqueConstraint.getColumnName().add(joinColumn.getName());
       }
     }
   }
 
   // makes sure that always the same side of an association is considered to be the owner.
   private boolean isOwner(EReference eReference) {
 
     final EReference eOpposite = eReference.getEOpposite();
     if (eOpposite == null) {
       return true;
     }
 
     // the many side of a bi-directional many-to-one/one-to-many is the owner
     if (!eReference.isMany() && eOpposite.isMany()) {
       return true;
     }
     if (eReference.isMany() && !eOpposite.isMany()) {
       return false;
     }
 
     if (eOpposite.isContainment()) {
       return false;
     }
 
     if (eReference.isContainment()) {
       return true;
     }
 
     final String sideOne = eReference.getEContainingClass().getEPackage().getName() + "_" //$NON-NLS-1$
         + eReference.getEContainingClass().getName() + "_" + eReference.getName(); //$NON-NLS-1$
     final String sideTwo = eOpposite.getEContainingClass().getEPackage().getName() + "_" //$NON-NLS-1$
         + eOpposite.getEContainingClass().getName() + "_" + eOpposite.getName(); //$NON-NLS-1$
     return sideOne.compareTo(sideTwo) > 0;
   }
 
   private String getTargetEntity(EReference eReference) {
     final EClassModelGenAnnotation targetEntityModelGen = (EClassModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eReference.getEReferenceType(),
             ModelcodegeneratorPackage.eINSTANCE.getEClassModelGenAnnotation());
     return targetEntityModelGen.getQualifiedClassName();
   }
 
   private String getMappedBy(EReference eReference) {
     final EReferenceModelGenAnnotation oppositeModelGenAnnotation = (EReferenceModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eReference.getEOpposite(), ModelcodegeneratorPackage.eINSTANCE.getEReferenceModelGenAnnotation());
     return oppositeModelGenAnnotation.getValidJavaMemberName();
   }
 
   private String getName(EReference eReference) {
     final EReferenceModelGenAnnotation modelGenAnnotation = (EReferenceModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eReference, ModelcodegeneratorPackage.eINSTANCE.getEReferenceModelGenAnnotation());
     return modelGenAnnotation.getValidJavaMemberName();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.emf.texo.generator.Annotator#getAnnotationEClass()
    */
   public EClass getAnnotationEClass() {
     return OrmannotationsPackage.eINSTANCE.getEReferenceORMAnnotation();
   }
 }
