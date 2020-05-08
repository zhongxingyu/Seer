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
  * $Id: AnnotationModelGenerator.java,v 1.9 2011/08/25 12:34:30 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.generator;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.texo.annotations.AnnotationModelSuffixHandler;
 import org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedEAttribute;
 import org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedEClass;
 import org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedEDataType;
 import org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedEEnum;
 import org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedEPackage;
 import org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedEReference;
 import org.eclipse.emf.texo.annotations.annotationsmodel.AnnotatedModel;
 import org.eclipse.emf.texo.annotations.annotationsmodel.AnnotationsmodelFactory;
 import org.eclipse.emf.texo.annotations.annotationsmodel.ENamedElementAnnotation;
 
 /**
  * Generates an AnnotationModel for an epackage and its subpackages. This class can generate an empty annotation model
  * or a completely annotated model.
  * 
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  */
 public class AnnotationModelGenerator {
 
   private List<ModelAnnotator> modelAnnotators = null;
 
   private boolean createOnlyInitialModel = false;
   private AnnotatedModel aModel;
 
   public void createStoreAnnotationModel(EPackage ePackage, String suffix, EPackage annotationEPackage, IFile modelFile) {
     try {
       final ResourceSet resourceSet = ePackage.eResource().getResourceSet();
      final org.eclipse.emf.common.util.URI fileUri = org.eclipse.emf.common.util.URI.createFileURI(modelFile
          .getLocation().toOSString());
      final org.eclipse.emf.common.util.URI fileAnnotationModelUri = AnnotationModelSuffixHandler
          .createAnnotationsModelURIWithSuffix(fileUri, suffix);
      final File file = new File(fileAnnotationModelUri.toFileString());
       final Resource resource;
       if (file.exists()) {
        resource = resourceSet.getResource(fileAnnotationModelUri, true);
       } else {
        resource = resourceSet.createResource(fileAnnotationModelUri);
         final String encoding = ((XMLResource) ePackage.eResource()).getEncoding();
         ((XMLResource) resource).setEncoding(encoding);
       }
 
       if (!resource.getContents().isEmpty() && resource.getContents().get(0) instanceof AnnotatedModel) {
         aModel = (AnnotatedModel) resource.getContents().get(0);
       } else {
         resource.getContents().clear();
         aModel = AnnotationsmodelFactory.eINSTANCE.createAnnotatedModel();
         resource.getContents().add(aModel);
       }
 
       fillAnnotatedModel(ePackage);
       if (annotationEPackage != null) {
         filterAnnotations(annotationEPackage);
       }
       resource.save(Collections.emptyMap());
     } catch (Exception e) {
      throw new IllegalStateException(
          "Exception (" + e.getMessage() + ") for modelFile: " + modelFile.getFullPath().toString() + " epackage " //$NON-NLS-1$ //$NON-NLS-2$
              + ePackage.getName() + " epackage uri : " + ePackage.eResource().getURI(), e); //$NON-NLS-1$
     }
   }
 
   private void filterAnnotations(EPackage annotationEPackage) {
     // iterate over all the content, copy to a list to prevent issues later
     final Iterator<EObject> iterator = aModel.eAllContents();
     final List<EObject> allContents = new ArrayList<EObject>();
     while (iterator.hasNext()) {
       allContents.add(iterator.next());
     }
 
     for (Object obj : allContents) {
       if (obj instanceof AnnotatedEPackage) {
         filterAnnotations(((AnnotatedEPackage) obj).getEPackageAnnotations(), annotationEPackage);
       } else if (obj instanceof AnnotatedEClass) {
         filterAnnotations(((AnnotatedEClass) obj).getEClassAnnotations(), annotationEPackage);
       } else if (obj instanceof AnnotatedEEnum) {
         filterAnnotations(((AnnotatedEEnum) obj).getEEnumAnnotations(), annotationEPackage);
       } else if (obj instanceof AnnotatedEDataType) {
         filterAnnotations(((AnnotatedEDataType) obj).getEDataTypeAnnotations(), annotationEPackage);
       } else if (obj instanceof AnnotatedEAttribute) {
         filterAnnotations(((AnnotatedEAttribute) obj).getEAttributeAnnotations(), annotationEPackage);
       } else if (obj instanceof AnnotatedEReference) {
         filterAnnotations(((AnnotatedEReference) obj).getEReferenceAnnotations(), annotationEPackage);
       }
     }
   }
 
   private void filterAnnotations(List<? extends ENamedElementAnnotation> annotations, EPackage annotationEPackage) {
     final List<ENamedElementAnnotation> toRemove = new ArrayList<ENamedElementAnnotation>();
     for (ENamedElementAnnotation annotation : annotations) {
       if (annotation.eClass().getEPackage() != annotationEPackage) {
         toRemove.add(annotation);
       }
     }
     annotations.removeAll(toRemove);
   }
 
   public void setModelAnnotators(List<ModelAnnotator> modelAnnotators) {
     this.modelAnnotators = modelAnnotators;
   }
 
   public void fillAnnotatedModel(EPackage ePackage) {
     final List<EPackage> ePackages = new ArrayList<EPackage>();
     collectEPackages(ePackage, ePackages);
 
     if (modelAnnotators != null && modelAnnotators.size() > 0) {
       final ModelController modelController = new ModelController();
       modelController.setEPackages(ePackages);
       modelController.getAnnotationManager().setAnnotatedModel(aModel);
       modelController.annotate(modelAnnotators);
     }
 
     // walk over the whole model and ask the aModel for an annotated version
     // this automatically creates it and adds it to the model
     for (EPackage collectedEPackage : ePackages) {
       final AnnotatedEPackage aPackage = aModel.getAnnotatedEPackage(collectedEPackage, true);
 
       // if only the initial model stop at the EPackage level
       if (isCreateOnlyInitialModel()) {
         aPackage.getAllAnnotations().clear();
         aPackage.getAnnotatedEClassifiers().clear();
         aPackage.getEPackageAnnotations().clear();
       } else {
         for (EClassifier eClassifier : collectedEPackage.getEClassifiers()) {
           aModel.getAnnotatedEClassifier(eClassifier, true);
           if (eClassifier instanceof EClass) {
             final EClass eClass = (EClass) eClassifier;
             // on purpose not getting efeatures of super types!
             for (EAttribute eAttribute : eClass.getEAttributes()) {
               aModel.getAnnotatedEAttribute(eAttribute, true);
             }
             for (EReference eReference : eClass.getEReferences()) {
               aModel.getAnnotatedEReference(eReference, true);
             }
           } else if (eClassifier instanceof EEnum) {
             final EEnum eEnum = (EEnum) eClassifier;
             aModel.getAnnotatedEEnum(eEnum, true);
             for (EEnumLiteral literal : eEnum.getELiterals()) {
               aModel.getAnnotatedEEnumLiteral(literal, true);
             }
           } else if (eClassifier instanceof EDataType) {
             final EDataType eDataType = (EDataType) eClassifier;
             aModel.getAnnotatedEDataType(eDataType, true);
           }
         }
       }
     }
 
     // other epackages may have been added during the annotation activities
     // remove those again from the annotated model
     final List<AnnotatedEPackage> toRemove = new ArrayList<AnnotatedEPackage>();
     for (AnnotatedEPackage aPackage : aModel.getAnnotatedEPackages()) {
       if (!ePackages.contains(aPackage.getEPackage())) {
         toRemove.add(aPackage);
       }
     }
     aModel.getAnnotatedEPackages().removeAll(toRemove);
   }
 
   private void collectEPackages(EPackage ePackage, List<EPackage> ePackages) {
     ePackages.add(ePackage);
     for (EPackage subEPackage : ePackage.getESubpackages()) {
       collectEPackages(subEPackage, ePackages);
     }
   }
 
   public boolean isCreateOnlyInitialModel() {
     return createOnlyInitialModel;
   }
 
   public void setCreateOnlyInitialModel(boolean createOnlyInitialModel) {
     this.createOnlyInitialModel = createOnlyInitialModel;
   }
 
 }
