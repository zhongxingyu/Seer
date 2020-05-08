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
  * $Id: EClassORMAnnotator.java,v 1.10 2011/09/21 14:03:48 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.orm.annotator;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.texo.generator.Annotator;
 import org.eclipse.emf.texo.generator.GeneratorUtils;
 import org.eclipse.emf.texo.modelgenerator.annotator.GenUtils;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.EClassModelGenAnnotation;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.ModelcodegeneratorPackage;
 import org.eclipse.emf.texo.orm.annotations.model.orm.AccessType;
 import org.eclipse.emf.texo.orm.annotations.model.orm.DiscriminatorColumn;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Embeddable;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Entity;
 import org.eclipse.emf.texo.orm.annotations.model.orm.MappedSuperclass;
 import org.eclipse.emf.texo.orm.annotations.model.orm.OrmFactory;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Table;
 import org.eclipse.emf.texo.orm.annotations.model.orm.Transient;
 import org.eclipse.emf.texo.orm.ormannotations.EClassORMAnnotation;
 import org.eclipse.emf.texo.orm.ormannotations.EPackageORMAnnotation;
 import org.eclipse.emf.texo.orm.ormannotations.OrmannotationsPackage;
 
 /**
  * Responsible for creating the {@link EClassORMAnnotation}.
  * 
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.10 $
  */
 
 public class EClassORMAnnotator extends ETypeElementORMAnnotator implements Annotator<EClassORMAnnotation> {
 
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.emf.texo.generator.Annotator#annotate(org.eclipse.emf.texo.annotations.annotationsmodel
    * .ENamedElementAnnotation)
    */
   public void setAnnotationFeatures(EClassORMAnnotation annotation) {
     final EClass eClass = annotation.getEClass();
 
     if (GenUtils.isDocumentRoot(eClass)) {
       return;
     }
 
     final ORMNamingStrategy namingStrategy = getOrmNamingStrategy(eClass.getEPackage());
     if (annotation.getTransient() != null) {
       final Transient transnt = annotation.getTransient();
       if (GeneratorUtils.isEmptyOrNull(transnt.getName())) {
         transnt.setName(namingStrategy.getEntityName(eClass));
       }
       return;
     }
 
     final EClassModelGenAnnotation modelGenAnnotation = (EClassModelGenAnnotation) getAnnotationManager()
         .getAnnotation(eClass, ModelcodegeneratorPackage.eINSTANCE.getEClassModelGenAnnotation());
     final EPackageORMAnnotation ePackageAnnotation = (EPackageORMAnnotation) getAnnotationManager().getAnnotation(
         eClass.getEPackage(), OrmannotationsPackage.eINSTANCE.getEPackageORMAnnotation());
 
     if (annotation.getMappedSuperclass() != null) {
       final MappedSuperclass mappedSuperclass = annotation.getMappedSuperclass();
       if (GeneratorUtils.isEmptyOrNull(mappedSuperclass.getClass_())) {
         mappedSuperclass.setClass(modelGenAnnotation.getQualifiedClassName());
       }
       return;
     }
 
    // only add entity if not embeddable
    final Embeddable embeddable = annotation.getEmbeddable();
    if (embeddable == null && annotation.getEntity() == null) {
      annotation.setEntity(OrmFactory.eINSTANCE.createEntity());
    }

     // set the inheritance mapping, copy the annotation from epackage level
     if (isRoot(annotation) && annotation.getInheritance() == null && ePackageAnnotation.getInheritance() != null
         && ePackageAnnotation.getInheritance().isSetStrategy()) {
       annotation.setInheritance(EcoreUtil.copy(ePackageAnnotation.getInheritance()));
     }
    if (annotation.getInheritance() != null && annotation.getEntity() != null) {
      annotation.getEntity().setInheritance(EcoreUtil.copy(annotation.getInheritance()));
     }
 
     if (ORMMappingOptions.getDefaultOptions().isTestRun()) {
       if (annotation.getEntity() != null) {
         annotation.getEntity().getConverter().add(ORMUtils.createDefaultConverter());
         annotation.getEntity().getConverter().add(ORMUtils.createQNameConverter());
       } else if (annotation.getMappedSuperclass() != null) {
         annotation.getMappedSuperclass().getConverter().add(ORMUtils.createDefaultConverter());
         annotation.getMappedSuperclass().getConverter().add(ORMUtils.createQNameConverter());
       }
     }
 
     final Entity entity = annotation.getEntity();
     if (entity != null) {
       if (namingStrategy.isGenerateAllDBSchemaNames()) {
         if (hasItsOwnTable(annotation)) {
           // set the tablename
           if (entity.getTable() == null) {
             entity.setTable(OrmFactory.eINSTANCE.createTable());
           }
           final Table table = entity.getTable();
           if (table.getName() == null) {
             table.setName(namingStrategy.getTableName(eClass));
           }
           if (!isRoot(annotation) && entity.getPrimaryKeyJoinColumn().isEmpty()) {
             // create a join column to the parent
             entity.getPrimaryKeyJoinColumn().add(OrmFactory.eINSTANCE.createPrimaryKeyJoinColumn());
             entity.getPrimaryKeyJoinColumn().get(0).setName(namingStrategy.getPrimaryKeyJoinColumn(eClass));
           }
         }
       }
 
       if (isRoot(annotation) && ORMMappingOptions.getDefaultOptions().isTestRun()) {
         final DiscriminatorColumn dc = OrmFactory.eINSTANCE.createDiscriminatorColumn();
         dc.setLength(255);
         entity.setDiscriminatorColumn(dc);
       }
 
       // with interfaces always access through the property
       if (eClass.isInterface()) {
         entity.setAccess(AccessType.PROPERTY);
       }
 
       if (GeneratorUtils.isEmptyOrNull(entity.getClass_())) {
         entity.setClass(modelGenAnnotation.getQualifiedClassName());
       }
 
       if (GeneratorUtils.isEmptyOrNull(entity.getName())) {
         entity.setName(namingStrategy.getEntityName(eClass));
       }
     } else if (embeddable != null) {
       // with interfaces always access through the property
       if (eClass.isInterface()) {
         embeddable.setAccess(AccessType.PROPERTY);
       }
 
       if (GeneratorUtils.isEmptyOrNull(embeddable.getClass_())) {
         embeddable.setClass(modelGenAnnotation.getQualifiedClassName());
       }
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @seeorg.eclipse.emf.texo.generator.Annotator#postAnnotating(org.eclipse.emf.texo.annotations.
    * annotationsmodel.ENamedElementAnnotation)
    */
   public void postAnnotating(EClassORMAnnotation annotation) {
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.emf.texo.generator.Annotator#getAnnotationEClass()
    */
   public EClass getAnnotationEClass() {
     return OrmannotationsPackage.eINSTANCE.getEClassORMAnnotation();
   }
 }
