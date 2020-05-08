 /**
  * Copyright (c) 2011 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.tools.osgifyme.core.java.inspect;
 
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.validation.ConstraintViolationException;
 
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.bcel.util.ClassLoaderRepository;
 import org.hamcrest.core.Is;
 import org.hamcrest.core.IsEqual;
 import org.junit.Test;
 import org.sourcepit.tools.osgifyme.test.resources.TypeA;
 
 /**
  * @author Bernd
  */
 public class JavaTypeReferencesCollectorTest
 {
    @Test
    public void test() throws Exception
    {
       try
       {
          JavaTypeReferencesCollector.collect(null);
          fail();
       }
       catch (ConstraintViolationException e)
       { // expected
       }
 
       final File jarFile = new File("target/testResources/osgifyme-core.jar");
       assertTrue(jarFile.exists());
 
       final ClassLoaderRepository classRepo = new ClassLoaderRepository(new URLClassLoader(new URL[] {jarFile.toURL()}));
       
       JavaClass jClass = classRepo.loadClass(TypeA.class.getName());
       
       Set<String> typeRefs = JavaTypeReferencesCollector.collect(jClass);
       assertThat(typeRefs.size(), Is.is(3));
       
      List<String> expectedRefs = new ArrayList<String>();
       expectedRefs.add("java.lang.Object");
       expectedRefs.add("java.lang.String");
       expectedRefs.add("java.lang.Boolean");
       
      assertThat(new ArrayList<String>(typeRefs), IsEqual.equalTo(expectedRefs));
       
       jClass = classRepo.loadClass(TypeA.Hans.class.getName());
       
       typeRefs = JavaTypeReferencesCollector.collect(jClass);
       assertThat(typeRefs.size(), Is.is(2));
       
      expectedRefs = new ArrayList<String>();
       expectedRefs.add("java.lang.Object");
       expectedRefs.add("java.lang.Runnable");
       
      assertThat(new ArrayList<String>(typeRefs), IsEqual.equalTo(expectedRefs));
    }
 }
