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
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.texo.generator.Annotator;
 import org.eclipse.emf.texo.generator.GeneratorUtils;
 import org.eclipse.emf.texo.modelgenerator.annotator.GenUtils;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.EReferenceModelGenAnnotation;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.ModelcodegeneratorPackage;
 import org.eclipse.emf.texo.orm.annotations.model.orm.AccessType;
 import org.eclipse.emf.texo.orm.annotations.model.orm.CollectionTable;
 import org.eclipse.emf.texo.orm.annotations.model.orm.ElementCollection;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Embedded;
 import org.eclipse.emf.texo.orm.annotations.model.orm.JoinColumn;
 import org.eclipse.emf.texo.orm.annotations.model.orm.JoinTable;
 import org.eclipse.emf.texo.orm.annotations.model.orm.ManyToMany;
 import org.eclipse.emf.texo.orm.annotations.model.orm.ManyToOne;
 import org.eclipse.emf.texo.orm.annotations.model.orm.MapKeyColumn;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OneToMany;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OneToOne;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OrderColumn;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OrmFactory;
 import org.eclipse.emf.texo.orm.annotations.model.orm.UniqueConstraint;
 import org.eclipse.emf.texo.orm.ormannotations.EClassORMAnnotation;
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
     final EClass eClass = eReference.getEContainingClass();
     if (GenUtils.isDocumentRoot(eClass)) {
       return;
     }
 
     final EPackage ePackage = eReference.getEContainingClass().getEPackage();
     final EPackageORMAnnotation ePackageORMAnnotation = (EPackageORMAnnotation) getAnnotationManager().getAnnotation(
         ePackage, OrmannotationsPackage.eINSTANCE.getEPackageORMAnnotation());
     final EReferenceModelGenAnnotation eReferenceModelGenAnnotation = (EReferenceModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eReference, ModelcodegeneratorPackage.eINSTANCE.getEReferenceModelGenAnnotation());
 
     // features which are part of a featuremap are never mapped as many features
     final boolean isPartOfFeatureMap = GeneratorUtils.isPartOfGroup(annotation.getEReference());
 
     final EReference eOpposite = eReference.getEOpposite();
     if (!isPartOfFeatureMap && eReference.isMany()) {
 
       if (doAddConverter(eReferenceModelGenAnnotation)) {
         addElementCollection(annotation);
       } else if (annotation.getElementCollection() != null) {
         annotateElementCollection(annotation);
       } else if (ModelUtils.isEMap(annotation.getEReference().getEReferenceType())) {
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
       final EClass referencedEClass = eReference.getEReferenceType();
       final EClassORMAnnotation referencedEClassORMAnnotation = (EClassORMAnnotation) getAnnotationManager()
           .getAnnotation(referencedEClass, OrmannotationsPackage.eINSTANCE.getEClassORMAnnotation());
 
       if (doAddConverter(eReferenceModelGenAnnotation)) {
         // an object map as a basic...
         annotation.setBasic(OrmFactory.eINSTANCE.createBasic());
         annotation.getBasic().setConvert(ORMUtils.OBJECT_CONVERTER_NAME);
       } else if (annotation.getEmbedded() != null || referencedEClassORMAnnotation.getEmbeddable() != null
           && referencedEClassORMAnnotation.getEntity() == null) {
         annotateEmbedded(annotation);
       } else if (eOpposite != null && !eOpposite.isMany()) {
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
 
   protected void annotateElementCollection(EReferenceORMAnnotation annotation) {
     final EReference eReference = annotation.getEReference();
     final EPackage ePackage = eReference.getEContainingClass().getEPackage();
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(ePackage);
     final EReferenceModelGenAnnotation eReferenceModelGenAnnotation = (EReferenceModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eReference, ModelcodegeneratorPackage.eINSTANCE.getEReferenceModelGenAnnotation());
 
     final ElementCollection elementCollection = annotation.getElementCollection();
 
     // make the access field if not changeable, as there won't be a setter
     if (!eReference.isChangeable()) {
       elementCollection.setAccess(AccessType.FIELD);
     } else if (GeneratorUtils.setPropertyAccess(annotation.getAnnotatedEFeature())) {
       elementCollection.setAccess(AccessType.PROPERTY);
     }
 
     if (GeneratorUtils.isEmptyOrNull(elementCollection.getName())) {
       elementCollection.setName(getName(eReference));
     }
 
     if (doAddConverter(eReferenceModelGenAnnotation)) {
       elementCollection.setConvert(ORMUtils.OBJECT_CONVERTER_NAME);
     }
 
    if (elementCollection.getOrderColumn() == null) {
       final OrderColumn orderColumn = OrmFactory.eINSTANCE.createOrderColumn();
       if (namingStrategy.isGenerateAllDBSchemaNames()) {
         orderColumn.setName(namingStrategy.getIndexColumnName(eReference));
       }
       elementCollection.setOrderColumn(orderColumn);
     }
 
     if (elementCollection.getCollectionTable() == null && namingStrategy.isGenerateAllDBSchemaNames()) {
       final CollectionTable collectionTable = OrmFactory.eINSTANCE.createCollectionTable();
       elementCollection.setCollectionTable(collectionTable);
     }
 
     if (eReference.isContainment()) {
       elementCollection.setCascadeOnDelete(true);
     }
 
     if (namingStrategy.isGenerateAllDBSchemaNames()) {
       if (elementCollection.getCollectionTable() != null) {
         if (GeneratorUtils.isEmptyOrNull(elementCollection.getCollectionTable().getName())) {
           elementCollection.getCollectionTable().setName(namingStrategy.getJoinTableName(eReference));
         }
       }
 
       if (elementCollection.getOrderColumn() != null
           && GeneratorUtils.isEmptyOrNull(elementCollection.getOrderColumn().getName())) {
         elementCollection.getOrderColumn().setName(namingStrategy.getIndexColumnName(eReference));
       }
     }
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
 
     // make the access field if not changeable, as there won't be a setter
     if (!eReference.isChangeable()) {
       oneToMany.setAccess(AccessType.FIELD);
     } else if (GeneratorUtils.setPropertyAccess(annotation.getAnnotatedEFeature())) {
       oneToMany.setAccess(AccessType.PROPERTY);
     }
 
     if (oneToMany.getCascade() == null) {
       if (eReference.isContainment()) {
         oneToMany.setCascade(EcoreUtil.copy(ePackageORMAnnotation.getDefaultCascadeContainment()));
       } else {
         oneToMany.setCascade(EcoreUtil.copy(ePackageORMAnnotation.getDefaultCascadeNonContainment()));
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
       // for maps we always need a join table otherwise eclipselink will fail with a NPE
       if (oneToMany.getJoinTable() == null) {
         final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
         oneToMany.setJoinTable(joinTable);
       }
 
       // explicitly set the join table name
       // is needed because otherwise eclipselink fails with a npe
       if (oneToMany.getJoinTable() != null && GeneratorUtils.isEmptyOrNull(oneToMany.getJoinTable().getName())) {
         oneToMany.getJoinTable().setName(namingStrategy.getJoinTableName(eReference));
       }
 
       if (namingStrategy.isGenerateAllDBSchemaNames()) {
         addColumnsToJoinTable(namingStrategy, oneToMany.getJoinTable(), annotation);
       }
 
       // note mapkeyclass not needed as we generate
       // fully parameterized types
       //      final EStructuralFeature eFeature = eReference.getEReferenceType().getEStructuralFeature("key"); //$NON-NLS-1$
       // final EClassifier referedClassifier = eFeature.getEType();
       // final EClassifierModelGenAnnotation modelGenAnnotation = (EClassifierModelGenAnnotation) getAnnotationManager()
       // .getAnnotation(referedClassifier, ModelcodegeneratorPackage.eINSTANCE.getEClassifierModelGenAnnotation());
       // if (oneToMany.getMapKeyClass() == null) {
       // final MapKeyClass mapKeyClass = OrmFactory.eINSTANCE.createMapKeyClass();
       // mapKeyClass.setClass(modelGenAnnotation.getQualifiedClassName());
       // oneToMany.setMapKeyClass(mapKeyClass);
       // }
 
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
 
     if (doAddConverter(eReferenceModelGenAnnotation)) {
       oneToMany.setConverter(ORMUtils.createDefaultConverter());
     }
 
     // set the order column, is always set on this side
     if (!ModelUtils.isEMap(eReference.getEReferenceType()) && oneToMany.getOrderBy() == null
         && ePackageORMAnnotation.isAddOrderColumnToListMappings() && eReferenceModelGenAnnotation.isUseList()
         && oneToMany.getOrderColumn() == null) {
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
     // not needed we generate fully qualified/parameterised code
     // if (GeneratorUtils.isEmptyOrNull(oneToMany.getTargetEntity())) {
     // oneToMany.setTargetEntity(getTargetEntity(eReference));
     // }
 
     // copy any join columns over
     oneToMany.getJoinColumn().addAll(annotation.getJoinColumn());
 
     // now work on jointable or joincolumn
     if (isOwner && oneToMany.getJoinColumn().isEmpty() && oneToMany.getJoinTable() == null) {
       if (eReference.isContainment()) {
         if (ePackageORMAnnotation.isUseJoinTablesForContainment()) {
           final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
           oneToMany.setJoinTable(joinTable);
         } else if (namingStrategy.isGenerateAllDBSchemaNames()) {
           final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
           oneToMany.getJoinColumn().add(joinColumn);
         }
       } else {
         if (ePackageORMAnnotation.isUseJoinTablesForNonContainment()) {
           // if there is an opposite let the jointable be set on the other side
           final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
           oneToMany.setJoinTable(joinTable);
         } else if (namingStrategy.isGenerateAllDBSchemaNames()) {
           final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
           oneToMany.getJoinColumn().add(joinColumn);
         }
       }
     }
 
     if (namingStrategy.isGenerateAllDBSchemaNames()) {
       if (oneToMany.getJoinTable() != null) {
         // set a unique name
         if (GeneratorUtils.isEmptyOrNull(oneToMany.getJoinTable().getName())) {
           oneToMany.getJoinTable().setName(namingStrategy.getJoinTableName(eReference));
         }
         addColumnsToJoinTable(namingStrategy, oneToMany.getJoinTable(), annotation);
       }
 
       if (oneToMany.getOrderColumn() != null && GeneratorUtils.isEmptyOrNull(oneToMany.getOrderColumn().getName())) {
         oneToMany.getOrderColumn().setName(namingStrategy.getIndexColumnName(eReference));
       }
       for (JoinColumn jc : oneToMany.getJoinColumn()) {
         if (GeneratorUtils.isEmptyOrNull(jc.getName())) {
           jc.setName(namingStrategy.getJoinColumnName(eReference));
         }
       }
     }
   }
 
   protected void mapMap(EReferenceORMAnnotation annotation) {
     final EClass mapEClass = annotation.getEReference().getEReferenceType();
     final EStructuralFeature valueEFeature = mapEClass.getEStructuralFeature("value"); //$NON-NLS-1$
     if (valueEFeature instanceof EReference) {
       annotateOneToMany(annotation);
     } else {
       addElementCollection(annotation);
     }
   }
 
   protected void addElementCollection(EReferenceORMAnnotation annotation) {
     final ElementCollection elementCollection;
     if (annotation.getElementCollection() == null) {
       elementCollection = OrmFactory.eINSTANCE.createElementCollection();
       annotation.setElementCollection(elementCollection);
     } else {
       elementCollection = annotation.getElementCollection();
     }
 
     annotation.setElementCollection(elementCollection);
 
     annotateElementCollection(annotation);
   }
 
   protected void annotateEmbedded(EReferenceORMAnnotation annotation) {
     final EReference eReference = annotation.getEReference();
     final Embedded embedded;
     if (annotation.getEmbedded() != null) {
       embedded = annotation.getEmbedded();
     } else {
       embedded = OrmFactory.eINSTANCE.createEmbedded();
       annotation.setEmbedded(embedded);
     }
 
     if (GeneratorUtils.isEmptyOrNull(embedded.getName())) {
       embedded.setName(getName(eReference));
     }
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
 
     // make the access field if not changeable, as there won't be a setter
     if (!eReference.isChangeable()) {
       oneToOne.setAccess(AccessType.FIELD);
     } else if (GeneratorUtils.setPropertyAccess(annotation.getAnnotatedEFeature())) {
       oneToOne.setAccess(AccessType.PROPERTY);
     }
 
     if (GeneratorUtils.isEmptyOrNull(oneToOne.getName())) {
       oneToOne.setName(getName(eReference));
     }
 
     if (oneToOne.getCascade() == null) {
       if (eReference.isContainment()) {
         oneToOne.setCascade(EcoreUtil.copy(ePackageORMAnnotation.getDefaultCascadeContainment()));
       } else {
         oneToOne.setCascade(EcoreUtil.copy(ePackageORMAnnotation.getDefaultCascadeNonContainment()));
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
     // not needed we generate fully qualified/parameterised code
     // if (GeneratorUtils.isEmptyOrNull(oneToOne.getTargetEntity())) {
     // oneToOne.setTargetEntity(getTargetEntity(eReference));
     // }
 
     // copy any join columns over
     oneToOne.getJoinColumn().addAll(annotation.getJoinColumn());
 
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(eReference.getEContainingClass().getEPackage());
     if (oneToOne.getJoinColumn().isEmpty() && namingStrategy.isGenerateAllDBSchemaNames()) {
       oneToOne.getJoinColumn().add(OrmFactory.eINSTANCE.createJoinColumn());
     }
     if (namingStrategy.isGenerateAllDBSchemaNames()) {
       if (oneToOne.getJoinTable() != null) {
         if (GeneratorUtils.isEmptyOrNull(oneToOne.getJoinTable().getName())) {
           oneToOne.getJoinTable().setName(namingStrategy.getJoinTableName(eReference));
         }
         addColumnsToJoinTable(namingStrategy, oneToOne.getJoinTable(), annotation);
       }
       for (JoinColumn jc : oneToOne.getJoinColumn()) {
         if (GeneratorUtils.isEmptyOrNull(jc.getName())) {
           jc.setName(namingStrategy.getForeignKeyColumnName(eReference));
         }
       }
     }
 
     if (!GeneratorUtils.isOptional(eReference)) {
       oneToOne.setOptional(false);
     }
   }
 
   protected void annotateManyToOne(EReferenceORMAnnotation annotation) {
     final EReference eReference = annotation.getEReference();
     final EPackage ePackage = eReference.getEContainingClass().getEPackage();
     final EPackageORMAnnotation ePackageORMAnnotation = (EPackageORMAnnotation) getAnnotationManager().getAnnotation(
         ePackage, OrmannotationsPackage.eINSTANCE.getEPackageORMAnnotation());
 
     final EClassORMAnnotation eClassORMAnnotation = (EClassORMAnnotation) getAnnotationManager().getAnnotation(
         eReference.getEContainingClass(), OrmannotationsPackage.eINSTANCE.getEClassORMAnnotation());
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(eReference.getEContainingClass().getEPackage());
 
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
 
     // make the access field if not changeable, as there won't be a setter
     if (!eReference.isChangeable()) {
       manyToOne.setAccess(AccessType.FIELD);
     } else if (GeneratorUtils.setPropertyAccess(annotation.getAnnotatedEFeature())) {
       manyToOne.setAccess(AccessType.PROPERTY);
     }
 
     if (manyToOne.getCascade() == null) {
       if (eReference.isContainment()) {
         manyToOne.setCascade(EcoreUtil.copy(ePackageORMAnnotation.getDefaultCascadeContainment()));
       } else {
         manyToOne.setCascade(EcoreUtil.copy(ePackageORMAnnotation.getDefaultCascadeNonContainment()));
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
     // not needed we generate fully qualified/parameterised code
     // if (GeneratorUtils.isEmptyOrNull(manyToOne.getTargetEntity())) {
     // manyToOne.setTargetEntity(getTargetEntity(eReference));
     // }
 
     if (!GeneratorUtils.isOptional(eReference)) {
       manyToOne.setOptional(false);
     }
 
     // copy any join columns over
     manyToOne.getJoinColumn().addAll(EcoreUtil.copyAll(annotation.getJoinColumn()));
 
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
             } else if (namingStrategy.isGenerateAllDBSchemaNames()) {
               final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
               manyToOne.getJoinColumn().add(joinColumn);
             }
           } else if (ePackageORMAnnotation.isUseJoinTablesForNonContainment()) {
             final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
             manyToOne.setJoinTable(joinTable);
           } else if (namingStrategy.isGenerateAllDBSchemaNames()) {
             final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
             manyToOne.getJoinColumn().add(joinColumn);
           }
         } else if (eOppositeModelGenAnnotation.isUseList()) {
           final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
           manyToOne.getJoinColumn().add(joinColumn);
         }
       } else if (namingStrategy.isGenerateAllDBSchemaNames()) {
         final JoinColumn joinColumn = OrmFactory.eINSTANCE.createJoinColumn();
         manyToOne.getJoinColumn().add(joinColumn);
       }
     }
 
     // set a unique name
     if (namingStrategy.isGenerateAllDBSchemaNames()) {
       if (manyToOne.getJoinTable() != null) {
         if (GeneratorUtils.isEmptyOrNull(manyToOne.getJoinTable().getName())) {
           manyToOne.getJoinTable().setName(namingStrategy.getJoinTableName(eReference));
         }
         addColumnsToJoinTable(namingStrategy, manyToOne.getJoinTable(), annotation);
       }
       for (JoinColumn jc : manyToOne.getJoinColumn()) {
         if (GeneratorUtils.isEmptyOrNull(jc.getName())) {
           jc.setName(namingStrategy.getForeignKeyColumnName(eReference));
         }
       }
     }
 
     // force nullable/optional if stored in another table
     if (!hasItsOwnTable(eClassORMAnnotation)) {
       if (manyToOne.getJoinColumn().isEmpty()) {
         if (!manyToOne.isOptional()) {
           manyToOne.setOptional(true);
         }
       } else {
         for (JoinColumn jc : manyToOne.getJoinColumn()) {
           jc.setNullable(true);
         }
       }
     }
 
   }
 
   protected void annotateManyToMany(EReferenceORMAnnotation annotation) {
     final EReference eReference = annotation.getEReference();
     final EPackage ePackage = eReference.getEContainingClass().getEPackage();
     final EPackageORMAnnotation ePackageORMAnnotation = (EPackageORMAnnotation) getAnnotationManager().getAnnotation(
         ePackage, OrmannotationsPackage.eINSTANCE.getEPackageORMAnnotation());
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(ePackage);
 
     final ManyToMany manyToMany;
     if (annotation.getManyToMany() != null) {
       manyToMany = annotation.getManyToMany();
     } else {
       manyToMany = OrmFactory.eINSTANCE.createManyToMany();
       annotation.setManyToMany(manyToMany);
     }
 
     // make the access field if not changeable, as there won't be a setter
     if (!eReference.isChangeable()) {
       manyToMany.setAccess(AccessType.FIELD);
     } else if (GeneratorUtils.setPropertyAccess(annotation.getAnnotatedEFeature())) {
       manyToMany.setAccess(AccessType.PROPERTY);
     }
 
     if (GeneratorUtils.isEmptyOrNull(manyToMany.getName())) {
       manyToMany.setName(getName(eReference));
     }
 
     if (manyToMany.getCascade() == null) {
       if (eReference.isContainment()) {
         manyToMany.setCascade(EcoreUtil.copy(ePackageORMAnnotation.getDefaultCascadeContainment()));
       } else {
         manyToMany.setCascade(EcoreUtil.copy(ePackageORMAnnotation.getDefaultCascadeNonContainment()));
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
     // not needed we generate fully qualified/parameterised code
     // if (GeneratorUtils.isEmptyOrNull(manyToMany.getTargetEntity())) {
     // manyToMany.setTargetEntity(getTargetEntity(eReference));
     // }
 
     // different cases:
     // 1) both sides are lists: 2 separate join tables
     // 2) one side is a list and one side a set: the list side is the owner
     // 3) both sides use a set, choose the owner
     final EReferenceModelGenAnnotation eReferenceModelGenAnnotation = (EReferenceModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eReference, ModelcodegeneratorPackage.eINSTANCE.getEReferenceModelGenAnnotation());
 
     if (doAddConverter(eReferenceModelGenAnnotation)) {
       manyToMany.setConverter(ORMUtils.createDefaultConverter());
     }
 
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
         if (ePackageORMAnnotation.isAddOrderColumnToListMappings() && eReferenceModelGenAnnotation.isUseList()
             && manyToMany.getOrderColumn() == null && manyToMany.getOrderBy() == null) {
           final OrderColumn orderColumn = OrmFactory.eINSTANCE.createOrderColumn();
           if (namingStrategy.isGenerateAllDBSchemaNames()) {
             orderColumn.setName(namingStrategy.getIndexColumnName(eReference));
           }
           manyToMany.setOrderColumn(orderColumn);
         }
       } else if (eOppositeModelGenAnnotation.isUseList()) {
         // opposite is the owner
         manyToMany.setMappedBy(getMappedBy(eReference));
       } else if (!isOwner(eReference)) {
         // opposite is the owner
         manyToMany.setMappedBy(getMappedBy(eReference));
       }
     } else {
       if (manyToMany.getJoinTable() == null && namingStrategy.isGenerateAllDBSchemaNames()) {
         // create a join table
         final JoinTable joinTable = OrmFactory.eINSTANCE.createJoinTable();
         manyToMany.setJoinTable(joinTable);
       }
       if (ePackageORMAnnotation.isAddOrderColumnToListMappings() && eReferenceModelGenAnnotation.isUseList()
           && manyToMany.getOrderBy() == null && manyToMany.getOrderColumn() == null) {
         final OrderColumn orderColumn = OrmFactory.eINSTANCE.createOrderColumn();
         if (namingStrategy.isGenerateAllDBSchemaNames()) {
           orderColumn.setName(namingStrategy.getIndexColumnName(eReference));
         }
         manyToMany.setOrderColumn(orderColumn);
       }
     }
 
     // set a unique name
     if (namingStrategy.isGenerateAllDBSchemaNames()) {
       if (manyToMany.getJoinTable() != null) {
         if (GeneratorUtils.isEmptyOrNull(manyToMany.getJoinTable().getName())) {
           manyToMany.getJoinTable().setName(namingStrategy.getJoinTableName(eReference));
         }
         addColumnsToJoinTable(namingStrategy, manyToMany.getJoinTable(), annotation);
       }
       if (manyToMany.getOrderColumn() != null && GeneratorUtils.isEmptyOrNull(manyToMany.getOrderColumn().getName())) {
         manyToMany.getOrderColumn().setName(namingStrategy.getIndexColumnName(eReference));
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
 
     if (joinTable.getJoinColumn().isEmpty() && namingStrategy.isGenerateAllDBSchemaNames()) {
       joinTable.getJoinColumn().add(OrmFactory.eINSTANCE.createJoinColumn());
       joinTable.getJoinColumn().get(0).setName(namingStrategy.getJoinColumnName(eReference));
     }
     if (joinTable.getInverseJoinColumn().isEmpty() && namingStrategy.isGenerateAllDBSchemaNames()) {
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
       // TODO: add uniqueconstraint naming
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
 
   // for later usage
   // private String getTargetEntity(EReference eReference) {
   // final EClassModelGenAnnotation targetEntityModelGen = (EClassModelGenAnnotation) getAnnotationManager()
   // .getAnnotation(eReference.getEReferenceType(),
   // ModelcodegeneratorPackage.eINSTANCE.getEClassModelGenAnnotation());
   // return targetEntityModelGen.getQualifiedClassName();
   // }
 
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
