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
  * $Id: TestModel.java,v 1.13 2011/09/04 20:12:20 mtaal Exp $
  */
 
 package org.eclipse.emf.texo.modelgenerator.test;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.texo.utils.Check;
 
 /**
  * Convenience class to get access to available test models.
  * 
  * @author <a href="mailto:mtaal@elver.org">Martin Taal</a>
  * @version $Revision: 1.13 $
  */
 public class TestModel {
   public static final String MODELGENERATOR_TEST_PROJECT = "org.eclipse.emf.texo.modelgenerator.test"; //$NON-NLS-1$
 
   /**
    * Convenience method to load a model located in this plugin.
    * 
    * @param modelFileName
    *          the file name within the src/model directory without preceding / (so just library.ecore for example)
    * @return the URL to that file
    */
   public static URL getModelUrl(final String modelFileName) {
     final URL url = TestModel.class.getResource(modelFileName);
     Check.isNotNull(url, "Test model " + modelFileName + " not found in /models directory"); //$NON-NLS-1$//$NON-NLS-2$
     return url;
   }
 
   public static URI getModelPlatformUri(final String fileName) {
     if (fileName.startsWith("platform") || fileName.startsWith("classpath")) { //$NON-NLS-1$ //$NON-NLS-2$
       return URI.create(fileName);
     }
     final String path = "platform:/resource/" + MODELGENERATOR_TEST_PROJECT + "/models/" + fileName; //$NON-NLS-1$ //$NON-NLS-2$
     return URI.create(path);
   }
 
   /**
    * @return the urls of all models (in ecore and xsd files) in the /model directory
    */
   public static List<String> getAllSpecifiedModelRelativePaths() {
     final List<String> urls = new ArrayList<String>();
     urls.add("base/identifiable.ecore");//$NON-NLS-1$
     urls.add("samples/accounting.ecore");//$NON-NLS-1$
     urls.add("samples/Catalog.xsd");//$NON-NLS-1$
     urls.add("samples/capa.xsd");//$NON-NLS-1$
     urls.add("samples/Claim.xsd");//$NON-NLS-1$
     urls.add("samples/emap.ecore");//$NON-NLS-1$
     urls.add("samples/employee.xsd");//$NON-NLS-1$
     urls.add("samples/epo2.ecore");//$NON-NLS-1$
     urls.add("samples/extlibrary.ecore");//$NON-NLS-1$
     urls.add("samples/graphiti.ecore");//$NON-NLS-1$
     urls.add("samples/interfaces.ecore");//$NON-NLS-1$
     urls.add("samples/inventory.ecore");//$NON-NLS-1$
     urls.add("samples/kdm.ecore");//$NON-NLS-1$
     urls.add("samples/library.ecore");//$NON-NLS-1$
     urls.add("samples/music.ecore");//$NON-NLS-1$
     urls.add("samples/play.xsd");//$NON-NLS-1$
     urls.add("samples/rental.ecore");//$NON-NLS-1$
     urls.add("samples/SchemaPrimerPO.xsd");//$NON-NLS-1$
     urls.add("samples/sun_books.xsd");//$NON-NLS-1$
     urls.add("samples/schoollibrary.ecore");//$NON-NLS-1$
     urls.add("samples/jpamixed.ecore");//$NON-NLS-1$
     urls.add("samples/types.ecore");//$NON-NLS-1$
     urls.add("samples/Workflow.ecore");//$NON-NLS-1$
     urls.add("samples/FeatureMapTest.ecore");//$NON-NLS-1$ 
     urls.add("samples/sport-society.xcore");//$NON-NLS-1$
     urls.add("samples/sport-club.xcore");//$NON-NLS-1$
     urls.add("samples/sport-competition.xcore");//$NON-NLS-1$
     urls.add("samples/travel.xcore");//$NON-NLS-1$
     urls.add("schemaconstructs/AnyType.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/attributes.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/datetime.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/documentroot.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/duration.ecore");//$NON-NLS-1$
     urls.add("schemaconstructs/EcoreAttrs.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/emap.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/GroupAll.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/include.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/List.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/ListUnion.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/Mixed.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/NestedGroup.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/qname.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/Restriction.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/Simplechoice.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/SimpleFeatureMap.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/simpletypes.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/SubstitutionGroup2.xsd");//$NON-NLS-1$
     urls.add("schemaconstructs/SubstitutionZvon.xsd");//$NON-NLS-1$
     urls.add("issues/instanceclassset.ecore"); //$NON-NLS-1$
     urls.add("issues/bz331009.ecore"); //$NON-NLS-1$
     urls.add("issues/bz325429.ecore"); //$NON-NLS-1$
     urls.add("issues/bz352285.ecore"); //$NON-NLS-1$
     urls.add("issues/bz325427.ecore"); //$NON-NLS-1$
     urls.add("issues/bz369931.ecore"); //$NON-NLS-1$
     urls.add("issues/bz369962.ecore"); //$NON-NLS-1$
     urls.add("issues/bz369476.ecore"); //$NON-NLS-1$
     urls.add("issues/bz371509.ecore"); //$NON-NLS-1$
     urls.add("issues/bz378087.ecore"); //$NON-NLS-1$
     urls.add("issues/bz378642.xsd"); //$NON-NLS-1$
     urls.add("issues/bz379796.xsd"); //$NON-NLS-1$
     urls.add("issues/bz379815.ecore"); //$NON-NLS-1$
     urls.add("issues/bz380279.ecore"); //$NON-NLS-1$
     urls.add("issues/bz386923.ecore"); //$NON-NLS-1$
     urls.add("issues/bz390942.ecore"); //$NON-NLS-1$
     urls.add("issues/bz391624.ecore"); //$NON-NLS-1$
     urls.add("issues/bz397220.ecore"); //$NON-NLS-1$
     urls.add("issues/SubPackage.ecore"); //$NON-NLS-1$
     urls.add("issues/NotRequiredSerialization.ecore"); //$NON-NLS-1$
     urls.add("issues/bz393240.xsd"); //$NON-NLS-1$
     urls.add("issues/bz399086.ecore"); //$NON-NLS-1$
     urls.add("issues/bz403161_1.ecore"); //$NON-NLS-1$
     urls.add("issues/bz403161_2.ecore"); //$NON-NLS-1$
     urls.add("issues/bz403743.ecore"); //$NON-NLS-1$
     urls.add("issues/bz404132.ecore"); //$NON-NLS-1$
     urls.add("issues/bz409157.ecore"); //$NON-NLS-1$
     urls.add("issues/bz411874.ecore"); //$NON-NLS-1$
     urls.add("issues/bz415716.ecore"); //$NON-NLS-1$
     urls.add("issues/bz420913.ecore"); //$NON-NLS-1$
     urls.add("issues/bz420913_NotBiDirectional.ecore"); //$NON-NLS-1$
     urls.add("issues/bz422811.ecore"); //$NON-NLS-1$
     urls.add("issues/bz423760.ecore"); //$NON-NLS-1$
    urls.add("issues/bz444321.ecore"); //$NON-NLS-1$
     urls.add("issues/interface_abstract_class_wrong_featureid.ecore"); //$NON-NLS-1$
 
     urls.add("texo/TexoDataRequest.xsd");//$NON-NLS-1$
     urls.add("texo/TexoDataResponse.xsd");//$NON-NLS-1$
     urls.add("texo/TexoModelExtensions.xsd");//$NON-NLS-1$
 
     // do this one as the last one as it causes the load of the
     // test.model plugin which contains generated EMF epackages
     urls.add("samples/forum.xsd");//$NON-NLS-1$
     // disabled for now
     return urls;
   }
 
   /**
    * These models are mapped with full JPA annotations to ensure correct mapping to the RDB.
    */
   public static List<String> getSafelyMappedModels() {
     final List<String> urls = new ArrayList<String>();
     urls.add("schemaconstructs/EcoreAttrs.xsd");//$NON-NLS-1$
     urls.add("samples/inventory.ecore");//$NON-NLS-1$
     return urls;
   }
 
   /**
    * @return for specific models return their dependencies to other models so they can be loaded also
    */
   public static List<String> getModelDependencies(final String fileName) {
     final List<String> result = new ArrayList<String>();
     if (fileName.equals("base/identifiable-xcore.xcore")) { //$NON-NLS-1$
       result.add("base/XcoreLang.xcore"); //$NON-NLS-1$
     }
     if (fileName.equals("samples/sport-competition.xcore")) { //$NON-NLS-1$
       result.add("base/XcoreLang.xcore"); //$NON-NLS-1$
       result.add("base/identifiable-xcore.xcore");//$NON-NLS-1$
       result.add("samples/sport-society.xcore");//$NON-NLS-1$
       result.add("samples/sport-club.xcore");//$NON-NLS-1$
     }
     if (fileName.equals("samples/sport-club.xcore")) { //$NON-NLS-1$
       result.add("base/XcoreLang.xcore"); //$NON-NLS-1$
       result.add("base/identifiable-xcore.xcore");//$NON-NLS-1$
       result.add("samples/sport-society.xcore");//$NON-NLS-1$
     }
     if (fileName.equals("samples/sport-society.xcore")) { //$NON-NLS-1$
       result.add("base/XcoreLang.xcore"); //$NON-NLS-1$
       result.add("base/identifiable-xcore.xcore");//$NON-NLS-1$
     }
     return result;
   }
 
   /**
    * @return all models located in the TestModel class package and below. The return path is relative from the TestModel
    *         class location.
    */
   public static List<String> getAllModelRelativePaths() {
     try {
       final URL url = TestModel.class.getResource("");//$NON-NLS-1$
       final File file = new File(url.toURI());
       final int length = file.getAbsolutePath().length();
       final List<String> absolutePaths = new ArrayList<String>();
       collectModels(file, absolutePaths);
       final List<String> relativePaths = new ArrayList<String>();
       for (String absolutePath : absolutePaths) {
         relativePaths.add(absolutePath.substring(length + 1));
       }
       return relativePaths;
     } catch (Exception e) {
       throw new IllegalStateException(e);
     }
   }
 
   private static void collectModels(final File file, final List<String> paths) {
     if (file.isFile() && (file.getName().endsWith(".ecore") || file.getName() //$NON-NLS-1$
         .endsWith(".xsd"))) { //$NON-NLS-1$
       paths.add(file.getAbsolutePath());
     } else if (file.isDirectory()) {
       for (File childFile : file.listFiles()) {
         collectModels(childFile, paths);
       }
     }
   }
 }
