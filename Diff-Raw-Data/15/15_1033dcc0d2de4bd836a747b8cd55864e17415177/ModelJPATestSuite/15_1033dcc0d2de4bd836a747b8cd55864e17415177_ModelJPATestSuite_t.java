 /**
  * <copyright>
  *
  * Copyright (c) 2010 Springsite BV (The Netherlands) and others
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
  * $Id: TestModelPackageProvider.java,v 1.7 2011/08/25 12:42:40 mtaal Exp $
  */
 package org.eclipse.emf.texo.orm.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.emf.texo.model.ModelPackage;
 import org.eclipse.emf.texo.test.model.TestModelPackageProvider;
 import org.eclipse.emf.texo.test.model.issues.bz391624.Bz391624ModelPackage;
 import org.eclipse.emf.texo.test.model.samples.extlibrary.ExtlibraryModelPackage;
 import org.eclipse.emf.texo.test.model.samples.librarymodelclasses.model.LibraryModelPackage;
 import org.eclipse.emf.texo.test.model.samples.schoollibrary.SchoollibraryModelPackage;
 import org.eclipse.emf.texo.test.model.schemaconstructs.anytype.AnytypeModelPackage;
 import org.eclipse.emf.texo.test.model.schemaconstructs.attributes.AttributesModelPackage;
 import org.eclipse.emf.texo.test.model.schemaconstructs.duration.DurationModelPackage;
 import org.eclipse.emf.texo.test.model.schemaconstructs.listunion.ListunionModelPackage;
 import org.eclipse.graphiti.mm.MmModelPackage;
 
 /**
  * Creates the JPA test cases for several model packages.
  * 
  * @author mtaal
  */
 public class ModelJPATestSuite {
 
   private static String[] persistenceUnitPostFixes = new String[] { "-hsqldb", "-class-hsqldb" }; //  , "-class-hsqldb"{"-hsqldb", "-mysql"}; //$NON-NLS-1$
 
   public static Test suite() {
     TestSuite suite = new TestSuite("Model JPA Test Suite"); //$NON-NLS-1$
 
     final List<ModelPackage> modelPackages = TestModelPackageProvider.getModelPackages();
 
     // modelPackages.clear();
    // modelPackages.add(TypesModelPackage.INSTANCE);
     // modelPackages.add(AnytypeModelPackage.INSTANCE);
 
     // anytype is not supported, result in Object as item/target entity
     modelPackages.remove(AttributesModelPackage.INSTANCE);
     modelPackages.remove(AnytypeModelPackage.INSTANCE);
     // union not supported, results in Object as item/target entity
     modelPackages.remove(ListunionModelPackage.INSTANCE);
     // duration can't be persisted
     modelPackages.remove(DurationModelPackage.INSTANCE);
     // has an ereference with type object
     modelPackages.remove(MmModelPackage.INSTANCE);
 
     modelPackages.remove(Bz391624ModelPackage.INSTANCE);
 
     // extlibrary works with interfaces and multiple inheritance
     // this does not work nicely with JPA/ORM
     modelPackages.remove(ExtlibraryModelPackage.INSTANCE);
 
     for (ModelPackage modelPackage : modelPackages) {
       if (modelPackage == SchoollibraryModelPackage.INSTANCE) {
         final List<ModelPackage> testModelPackages = new ArrayList<ModelPackage>();
         testModelPackages.add(SchoollibraryModelPackage.INSTANCE);
         testModelPackages.add(LibraryModelPackage.INSTANCE);
         addTest(testModelPackages, suite, "special-schoollibrary"); //$NON-NLS-1$        
       } else {
         addTest(modelPackage, suite);
       }
     }
 
     return suite;
   }
 
   private static void addTest(ModelPackage modelPackage, TestSuite suite) {
     for (String postFix : persistenceUnitPostFixes) {
       final DataGenCompareModelJPATest test = new DataGenCompareModelJPATest(modelPackage, postFix);
       suite.addTest(test);
     }
   }
 
   private static void addTest(List<ModelPackage> modelPackages, TestSuite suite, String persistenceXMLPrefix) {
     for (String postFix : persistenceUnitPostFixes) {
       final DataGenCompareModelJPATest test = new DataGenCompareModelJPATest(modelPackages);
       test.setPersistenceUnitPostFix(postFix);
       test.setPersistenceXMLPrefix(persistenceXMLPrefix);
       suite.addTest(test);
     }
   }
 }
