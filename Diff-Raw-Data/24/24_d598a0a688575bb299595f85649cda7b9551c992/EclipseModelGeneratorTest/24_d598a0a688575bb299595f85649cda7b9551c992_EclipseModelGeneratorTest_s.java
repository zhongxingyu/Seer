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
  * $Id: EclipseModelGeneratorTest.java,v 1.24 2011/10/25 13:19:13 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.modelgenerator.test;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.xcore.XcoreStandaloneSetup;
 import org.eclipse.emf.texo.component.ComponentProvider;
 import org.eclipse.emf.texo.generator.AnnotationManager;
 import org.eclipse.emf.texo.generator.ArtifactGenerator;
 import org.eclipse.emf.texo.generator.EclipseGeneratorUtils;
 import org.eclipse.emf.texo.generator.GeneratorUtils;
 import org.eclipse.emf.texo.generator.ModelAnnotator;
 import org.eclipse.emf.texo.generator.ModelAnnotatorRegistry;
 import org.eclipse.emf.texo.generator.ModelController;
 import org.eclipse.emf.texo.generator.TexoResourceManager;
 import org.eclipse.emf.texo.modelgenerator.modelannotations.EPackageModelGenAnnotation;
 import org.eclipse.emf.texo.modelgenerator.xtend.ModelTemplate;
 import org.eclipse.emf.texo.orm.annotator.ORMMappingOptions;
 import org.eclipse.emf.texo.provider.IdProvider;
 import org.eclipse.emf.texo.provider.TitleProvider;
 import org.eclipse.emf.texo.utils.ModelUtils;
 import org.eclipse.xtext.EcoreUtil2;
 import org.eclipse.xtext.util.CancelIndicator;
 
 import com.google.inject.Injector;
 
 /**
  * Test the generation of model code through the Eclipse plugin. Needs to be run as a junit plugin test.
  * 
  * Generates all models present in the test model project in the org.eclipse.emf.texo.test.model package in the src
  * directory.
  * 
  * @see TestModel
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.24 $
  */
 
 public class EclipseModelGeneratorTest extends TestCase {
   private static final String IDENTIFIABLE_NSURI = "http://www.eclipse.org/emf/texo/test/model/base/identifiable"; //$NON-NLS-1$
   private static final String IDENTIFIABLE_ECLASS_NAME = "Identifiable"; //$NON-NLS-1$
 
   private static final String DEFAULT_EXTENDS = "org.eclipse.emf.texo.test.model.base.identifiable.Identifiable"; //$NON-NLS-1$
 
   private static final String TEST_MODEL_PROJECT = "org.eclipse.emf.texo.test.model"; //$NON-NLS-1$
 
   private static final EPackage.Registry SHARED_REGISTRY = GeneratorUtils.createEPackageRegistry();
 
   private boolean generateTexoModels = false;
 
   private ORMMappingOptions testORMOptions = new ORMMappingOptions();
   private ORMMappingOptions safeORMOptions = new ORMMappingOptions();
 
   private XcoreStandaloneSetup xcoreStandaloneSetup = new XcoreStandaloneSetup();
   private Injector injector = xcoreStandaloneSetup.createInjectorAndDoEMFRegistration();
 
   public void testGenerateModels() throws Exception {
     final IProject testProject = EclipseGeneratorUtils.getProject(TEST_MODEL_PROJECT);
     // testProject.refreshLocal(100, new NullProgressMonitor());
     // testProject.build(IncrementalProjectBuilder.CLEAN_BUILD, "org.eclipse.xtext.ui.shared.xtextBuilder",
     // Collections.EMPTY_MAP, new NullProgressMonitor());
     // testProject.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
 
     testORMOptions.setAddOrderColumnToListMappings(true);
     testORMOptions.setEnforceUniqueNames(true);
     testORMOptions.setTestRun(true);
 
     safeORMOptions.setAddOrderColumnToListMappings(true);
     safeORMOptions.setRenameSQLReservedNames(true);
     safeORMOptions.setEnforceUniqueNames(true);
     safeORMOptions.setGenerateFullDbSchemaNames(true);
     safeORMOptions.setTestRun(true);
 
     // force initialization
     ModelAnnotatorRegistry.getInstance().getModelAnnotators();
     // let everyone have orm annotations..
     if (!isGenerateTexoModels()) {
       AnnotationManager.enableAnnotationSystem(AnnotationManager.JPA_ANNOTATION_SYSTEM_ID);
     }
 
     for (String modelFile : getModelFileRelativePaths()) {
       System.err.println("Generating modelfile " + modelFile); //$NON-NLS-1$
       if (modelFile.contains("employee.xsd")) { //$NON-NLS-1$
         testProject.setPersistentProperty(TexoResourceManager.TEMPLATE_FOLDER_PROPERTY, "templates"); //$NON-NLS-1$
       } else {
         testProject.setPersistentProperty(TexoResourceManager.TEMPLATE_FOLDER_PROPERTY, null);
       }
       generate(new String[] { modelFile });
     }
   }
 
   protected boolean useSharedEPackageRegistry() {
     return false;
   }
 
   private void generate(final String[] ecoreFileNames) throws Exception {
 
     try {
       ORMMappingOptions.setDefaultOptions(testORMOptions);
       final List<String> safelyMappedModels = TestModel.getSafelyMappedModels();
       for (String ecoreFileName : ecoreFileNames) {
         if (safelyMappedModels.contains(ecoreFileName)) {
           ORMMappingOptions.setDefaultOptions(safeORMOptions);
           break;
         }
       }
 
       final EPackage.Registry packageRegistry = useSharedEPackageRegistry() ? SHARED_REGISTRY : GeneratorUtils
           .createEPackageRegistry();
       final ResourceSet resourceSet = GeneratorUtils.createGenerationResourceSet(injector, packageRegistry);
 
       final List<URI> uris = new ArrayList<URI>();
       for (final String ecoreFileName : ecoreFileNames) {
         final URI uri = TestModel.getModelPlatformUri(ecoreFileName);
         uris.add(uri);
 
         // read the deps
         final List<String> deps = TestModel.getModelDependencies(ecoreFileName);
         if (deps.size() > 0) {
           final List<URI> depUris = new ArrayList<URI>();
           for (String dep : deps) {
             depUris.add(TestModel.getModelPlatformUri(dep));
           }
           // register all the dependent epackages also using the resource uri and the platform uri, this is needed in
           // case of xcore which resolves using platform uris
           final List<EPackage> depEPackages = GeneratorUtils
               .readEPackages(depUris, resourceSet, packageRegistry, false);
           for (EPackage depEPackage : depEPackages) {
             if (depEPackage.eResource() != null && depEPackage.eResource().getURI() != null) {
               final String uriStr = depEPackage.eResource().getURI().toString();
               packageRegistry.put(uriStr, depEPackage);
               packageRegistry.put(org.eclipse.emf.common.util.URI.createPlatformResourceURI(uriStr, true).toString(),
                   depEPackage);
             }
           }
         }
       }
 
       // and read the epackages we are generating for
       final List<EPackage> ePackages = GeneratorUtils.readEPackages(uris, resourceSet, packageRegistry, false);
 
       // for (String key : packageRegistry.keySet()) {
       // System.err.println(key + " --> " + ((EPackage) packageRegistry.get(key)).getName());
       // }
 
       if (true || !isGenerateTexoModels()) {
         // also reads the identifiable.xcore in memory
         addSuperType(resourceSet, ePackages, packageRegistry);
 
         boolean hasIdentifiable = false;
         for (EPackage ePackage : ePackages) {
           hasIdentifiable = hasIdentifiable
               || ePackage.getNsURI().equals("http://www.eclipse.org/emf/texo/test/model/base/identifiable"); //$NON-NLS-1$
           if (ePackage.eResource() != null) {
             EcoreUtil2.resolveLazyCrossReferences(ePackage.eResource(), CancelIndicator.NullImpl);
           }
         }
 
         // give everyone the identifiable as super, except the identifiable
         // package
         if (hasIdentifiable) {
           EPackageModelGenAnnotation.setDefaultExtends(null);
         } else {
           EPackageModelGenAnnotation.setDefaultExtends(DEFAULT_EXTENDS);
         }
       }
 
       try {
         final ModelController modelController = new ModelController();
         modelController.setEPackages(ePackages);
         modelController.getAnnotationManager().getAnnotatedModel().setGeneratingSources(true);
         modelController.annotate(new ArrayList<ModelAnnotator>());
 
         final ArtifactGenerator artifactGenerator = new ArtifactGenerator();
         artifactGenerator.setModelController(modelController);
         artifactGenerator.setOutputFolder("src-test-gen"); //$NON-NLS-1$
         artifactGenerator.setProjectName(TEST_MODEL_PROJECT);
         if (!isGenerateTexoModels()) {
           artifactGenerator.setDoDao(true);
         }
         artifactGenerator.setXTendTemplate(new ModelTemplate());
         artifactGenerator.run();
       } finally {
         EPackageModelGenAnnotation.setDefaultExtends(null);
       }
     } catch (final Exception e) {
       throw new IllegalStateException(e);
     } finally {
       ORMMappingOptions.setDefaultOptions(null);
     }
   }
 
   protected void addSuperType(final ResourceSet resourceSet, final List<EPackage> ePackages,
       final EPackage.Registry packageRegistry) throws Exception {
 
     // first check if there is already an identifiable, if so use that one
     EClass identifiableEClass = getIdentifiableSuperEClass(ePackages);
 
     // read it from the package registry
     if (identifiableEClass == null) {
       EPackage identifiableEPackage = packageRegistry.getEPackage(IDENTIFIABLE_NSURI);
       if (identifiableEPackage != null) {
         identifiableEClass = getIdentifiableSuperEClass(Collections.singletonList(identifiableEPackage));
       }
     }
 
     // not found then read it
     if (identifiableEClass == null) {
       final List<EPackage> identifiableEPackages = GeneratorUtils
           .readEPackages(
               Collections.singletonList(TestModel.getModelPlatformUri("base/identifiable.ecore")), resourceSet, packageRegistry, false); //$NON-NLS-1$
 
       for (EPackage ePackage : identifiableEPackages) {
         if (ePackage.getNsURI().equals(IDENTIFIABLE_NSURI)) {
           identifiableEClass = (EClass) ePackage.getEClassifier(IDENTIFIABLE_ECLASS_NAME);
           break;
         }
       }
     }
 
     for (EPackage ePackage : ePackages) {
       addSuperType(ePackage, identifiableEClass);
     }
     // reset the id and title providers
     IdProvider.setInstance(ComponentProvider.getInstance().newInstance(IdProvider.class));
     TitleProvider.setInstance(ComponentProvider.getInstance().newInstance(TitleProvider.class));
   }
 
   protected EClass getIdentifiableSuperEClass(final List<EPackage> ePackages) {
     for (EPackage ePackage : ePackages) {
       for (EClassifier eClassifier : ePackage.getEClassifiers()) {
         if (eClassifier instanceof EClass) {
           final EClass eClass = (EClass) eClassifier;
           final EClass identifiableEClass = getIdentifiableSuperEClass(eClass);
           if (identifiableEClass != null) {
             return identifiableEClass;
           }
         }
       }
     }
     return null;
   }
 
   protected EClass getIdentifiableSuperEClass(final EClass eClass) {
     if (eClass.getEPackage().getNsURI().equals(IDENTIFIABLE_NSURI) && eClass.getName().equals(IDENTIFIABLE_ECLASS_NAME)) {
       return eClass;
     }
     for (EClass eSuperClass : eClass.getESuperTypes()) {
       final EClass identifiableEClass = getIdentifiableSuperEClass(eSuperClass);
       if (identifiableEClass != null) {
         return identifiableEClass;
       }
     }
     return null;
   }
 
   // add the identifiable super type to each ecore
   protected void addSuperType(final EPackage ePackage, final EClass identifiableEClass) {
 
     if (identifiableEClass.getEPackage() == ePackage) {
       return;
     }
 
     for (EClassifier eClassifier : ePackage.getEClassifiers()) {
       if (eClassifier instanceof EClass) {
         final EClass eClass = (EClass) eClassifier;
         if (eClass.isInterface() || ModelUtils.isEMap(eClass)) {
           continue;
         }
         addSuperType(eClass, identifiableEClass);
       }
     }
     for (EPackage subEPackage : ePackage.getESubpackages()) {
       addSuperType(subEPackage, identifiableEClass);
     }
   }
 
   protected void addSuperType(final EClass eClass, final EClass identifiableEClass) {
     if (eClass == identifiableEClass) {
       return;
     }
     if (eClass.getEPackage().getNsURI().equals(IDENTIFIABLE_NSURI)) {
       return;
     }
     for (EClass eSuperClass : eClass.getESuperTypes()) {
       if (!eSuperClass.isInterface()) {
         addSuperType(eSuperClass, identifiableEClass);
         return;
       }
     }
     eClass.getESuperTypes().add(0, identifiableEClass);
   }
 
   protected List<String> getModelFileRelativePaths() {
     return TestModel.getAllSpecifiedModelRelativePaths();
   }
 
   protected boolean isGenerateTexoModels() {
     return generateTexoModels;
   }
 
   protected void setGenerateTexoModels(final boolean generateTexoModels) {
     this.generateTexoModels = generateTexoModels;
   }
 }
