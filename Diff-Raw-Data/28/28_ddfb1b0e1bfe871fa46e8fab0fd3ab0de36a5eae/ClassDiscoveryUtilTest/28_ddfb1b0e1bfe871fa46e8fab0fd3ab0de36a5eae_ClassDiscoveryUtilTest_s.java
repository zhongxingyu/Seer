 /* Copyright 2007 Tacit Knowledge LLC
 * 
  * Licensed under the Tacit Knowledge Open License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License. You may
  * obtain a copy of the License at http://www.tacitknowledge.com/licenses-1.0.
 * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.tacitknowledge.util.discovery;
 
import java.io.File;

 import junit.framework.TestCase;
 import junit.framework.TestResult;
 
 /**
  * Tests the <code>ClassDiscoveryUtil</code> class.
 * 
  * @author  Scott Askew (scott@tacitknowledge.com)
  * @version $Id: ClassDiscoveryUtilTest.java,v 1.2 2006/11/11 00:47:08 mike Exp $
  */
 public class ClassDiscoveryUtilTest extends TestCase
 {

     /**
      * Constructor for ClassDiscoveryUtilTest.
     * 
      * @param name the name of the test to run
      */
     public ClassDiscoveryUtilTest(String name)
     {
         super(name);
     }
    
     /**
      * Basic test of <code>ClassDiscoveryUtil.getResources(String, String)</code>
      * when the resource lives in a directory.
      */
     public void testGetRegexResourcesInDirectory()
     {
         String dir = getClass().getPackage().getName().replace('.', File.separatorChar);
         String regex = "^ClassDiscoveryUtilTest.cl.+";
         String[] names = ClassDiscoveryUtil.getResources(dir, regex);
         for (int i = 0; i < names.length; i++)
         {
            System.err.println("name " + i + " is '" + names[i] + "'");
         }
         assertNotNull(names);
         assertEquals(1, names.length);
         assertEquals(dir + File.separator + "ClassDiscoveryUtilTest.class", names[0]);
     }
 
     /**
      * Basic test of <code>ClassDiscoveryUtil.getResources(String, String)</code>
      * when the resource lives in an archive.
      */
     public void testGetRegexResourcesInArchive()
     {
         String dir = TestCase.class.getPackage().getName().replace('.', File.separatorChar);
         String regex = "^TestCase.+";
         String[] names = ClassDiscoveryUtil.getResources(dir, regex);
         assertNotNull(names);
         assertTrue(names.length >= 1);
         assertEquals(dir + File.separator + "TestCase.class", names[0]);
     }
    
     /**
      * Basic test of <code>ClassDiscoveryUtil.getClasses(String, Class)</code>
      * when the resource lives in a directory.
      */
     public void testGetClassesInDirectory()
     {
         Class[] classes
             = ClassDiscoveryUtil.getClasses(getClass().getPackage().getName(), getClass());
         for (int i = 0; i < classes.length; i++)
         {
             System.err.println("name " + i + " is '" + classes[i].getName() + "'");
         }
         assertNotNull(classes);
         assertEquals(1, classes.length);
         assertEquals(getClass().getName(), classes[0].getName());
     }
 
     /**
      * Basic test of <code>ClassDiscoveryUtil.getClasses(String, Class)</code>
      * when the resource lives in an archive.
      */
     public void testGetClassesInArchive()
     {
         String packageName = TestCase.class.getPackage().getName();
         Class[] classes
             = ClassDiscoveryUtil.getClasses(packageName, TestResult.class);
         assertNotNull(classes);
         assertTrue(classes.length >= 1);
         assertEquals(TestResult.class.getName(), classes[0].getName());
     }
 }
