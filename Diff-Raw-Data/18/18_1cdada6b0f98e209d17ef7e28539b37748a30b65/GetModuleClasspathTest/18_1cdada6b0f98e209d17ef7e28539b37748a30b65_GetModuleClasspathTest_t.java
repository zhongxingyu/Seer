 /* Copyright (c) 2013, Dźmitry Laŭčuk
    All rights reserved.
 
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met: 
 
    1. Redistributions of source code must retain the above copyright notice, this
       list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation
       and/or other materials provided with the distribution.
 
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
 package afc.ant.modular;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.PropertyHelper;
 import org.apache.tools.ant.types.Path;
 
 import junit.framework.TestCase;
 
 public class GetModuleClasspathTest extends TestCase
 {
     private GetModuleClasspath task;
     private Project project;
     
     @Override
     protected void setUp()
     {
         task = new GetModuleClasspath();
         project = new Project();
         task.setProject(project);
     }
     
     @Override
     protected void tearDown()
     {
         project = null;
         task = null;
     }
     
     public void testNoModuleProperty()
     {
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         
         try {
             task.execute();
             fail();
         }
         catch (BuildException ex)
         {
             assertEquals("The attribute 'moduleProperty' is undefined.", ex.getMessage());
         }
         
         assertEquals(null, PropertyHelper.getProperty(project, "out"));
     }
     
     public void testNoOutputProperty()
     {
         final Module module = new Module("foo");
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setClasspathAttribute("cp");
         
         try {
             task.execute();
             fail();
         }
         catch (BuildException ex)
         {
             assertEquals("The attribute 'outputProperty' is undefined.", ex.getMessage());
         }
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
     }
     
     public void testNoClasspathAttributes()
     {
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         
         try {
             task.execute();
             fail();
         }
         catch (BuildException ex)
         {
            assertEquals("No classpath attributes are defined.", ex.getMessage());
         }
         
         assertEquals(null, PropertyHelper.getProperty(project, "out"));
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
     }
     
     public void testClasspathAttributeWithUndefinedName_SingleAttribute()
     {
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
         PropertyHelper.setProperty(project, "in", module);
         task.setClasspathAttribute(null);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         
         try {
             task.execute();
             fail();
         }
         catch (BuildException ex)
         {
            assertEquals("A classpath attribute with the undefined name is specified.", ex.getMessage());
         }
         
         assertEquals(null, PropertyHelper.getProperty(project, "out"));
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
     }
     
     public void testClasspathAttributeWithUndefinedName_MultipleAttributes()
     {
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
         PropertyHelper.setProperty(project, "in", module);
         task.createClasspathAttribute().setName("foo");
         task.createClasspathAttribute().setName(null);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         
         try {
             task.execute();
             fail();
         }
         catch (BuildException ex)
         {
            assertEquals("A classpath attribute with the undefined name is specified.", ex.getMessage());
         }
         
         assertEquals(null, PropertyHelper.getProperty(project, "out"));
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
     }
     
     public void testNoModuleUnderThePropery()
     {
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         
         try {
             task.execute();
             fail();
         }
         catch (BuildException ex)
         {
             assertEquals("No module is found under the property 'in'.", ex.getMessage());
         }
         
         assertEquals(null, PropertyHelper.getProperty(project, "out"));
         assertSame(null, PropertyHelper.getProperty(project, "in"));
     }
     
     public void testInvalidModuleType()
     {
         final Object invalidModule = Long.valueOf(0);
         PropertyHelper.setProperty(project, "in", invalidModule);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         
         try {
             task.execute();
             fail();
         }
         catch (BuildException ex)
         {
             assertEquals("Invalid module type is found under the property 'in'. " +
                     "Expected: 'afc.ant.modular.Module', found: 'java.lang.Long'.", ex.getMessage());
         }
         
         assertEquals(null, PropertyHelper.getProperty(project, "out"));
         assertSame(invalidModule, PropertyHelper.getProperty(project, "in"));
     }
     
     public void testSingleClasspathAttribute_ClasspathPropertyWithWrongType()
     {
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", "12345"));
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         
         try {
             task.execute();
             fail();
         }
         catch (BuildException ex) {
             assertEquals("The attribute 'cp' of the module 'foo' is not an Ant path.", ex.getMessage());
         }
         
         assertEquals(null, PropertyHelper.getProperty(project, "out"));
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", "12345"), module.getAttributes());
     }
     
     public void testSingleClasspathAttribute_DependeeModuleWithCPPropertyWithWrongType()
     {
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
         final Module dep = new Module("bar");
         dep.setAttributes(TestUtil.<String, Object>map("cp", Integer.valueOf(2)));
         module.addDependency(dep);
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         task.setIncludeDependencies(true);
         
         try {
             task.execute();
             fail();
         }
         catch (BuildException ex) {
             assertEquals("The attribute 'cp' of the module 'bar' is not an Ant path.", ex.getMessage());
         }
         
         assertEquals(null, PropertyHelper.getProperty(project, "out"));
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
         assertEquals(TestUtil.<String, Object>map("cp", Integer.valueOf(2)), dep.getAttributes());
     }
     
     public void testSingleClasspathAttribute_DependeeModuleWithCPPropertyWithWrongType_DepsNotIncludedImplicitly()
     {
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
         final Module dep = new Module("bar");
         dep.setAttributes(TestUtil.<String, Object>map("cp", Integer.valueOf(2)));
         module.addDependency(dep);
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         final Path classpath = (Path) outObject;
         assertEquals(0, classpath.size());
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
         assertEquals(TestUtil.<String, Object>map("cp", Integer.valueOf(2)), dep.getAttributes());
     }
     
     public void testSingleClasspathAttribute_NoClasspathProperty_NoDependencies()
     {
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         final Path classpath = (Path) outObject;
         assertEquals(0, classpath.size());
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
     }
     
     public void testSingleClasspathAttribute_NoClasspathProperty_NoDependencies_OutputPropertyAlreadyDefined()
     {
         final Object value = new Object();
         PropertyHelper.setProperty(project, "out", value);
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         
         task.execute();
         
         assertSame(value, PropertyHelper.getProperty(project, "out"));
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
     }
     
     public void testSingleClasspathAttribute_NoClasspathProperty_WithDependencies()
     {
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
         final Module dep1 = new Module("bar");
         dep1.setAttributes(TestUtil.<String, Object>map("x", "y"));
         module.addDependency(dep1);
         final Module dep2 = new Module("bar");
         dep2.setAttributes(TestUtil.<String, Object>map("b", "c"));
         module.addDependency(dep2);
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         final Path classpath = (Path) outObject;
         assertEquals(0, classpath.size());
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
         assertEquals(TestUtil.<String, Object>map("x", "y"), dep1.getAttributes());
         assertEquals(TestUtil.<String, Object>map("b", "c"), dep2.getAttributes());
     }
     
     public void testSingleClasspathAttribute_NoClasspathProperty_DependencyLoop()
     {
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"));
         final Module dep1 = new Module("bar");
         dep1.setAttributes(TestUtil.<String, Object>map("x", "y"));
         module.addDependency(dep1);
         final Module dep2 = new Module("bar");
         dep2.setAttributes(TestUtil.<String, Object>map("b", "c"));
         dep1.addDependency(dep2);
         dep2.addDependency(module);
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         final Path classpath = (Path) outObject;
         assertEquals(0, classpath.size());
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3"), module.getAttributes());
         assertEquals(TestUtil.<String, Object>map("x", "y"), dep1.getAttributes());
         assertEquals(TestUtil.<String, Object>map("b", "c"), dep2.getAttributes());
     }
     
     public void testSingleClasspathAttribute_WithClasspathProperty_SingleModule()
     {
         final Path path = new Path(project);
         path.createPathElement().setPath("a");
         path.createPathElement().setPath("b");
         path.createPathElement().setPath("c/d/e");
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", path));
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, new File("a"), new File("b"), new File("c/d/e")); 
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", path), module.getAttributes());
     }
     
     public void testSingleClasspathAttribute_WithClasspathProperty_ModuleWithSingleDependency_DepsIncluded()
     {
         final Path path1 = new Path(project);
         path1.createPathElement().setPath("b");
         path1.createPathElement().setPath("a");
         path1.createPathElement().setPath("c/d/e");
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         path2.createPathElement().setPath("555");
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", path1));
         final Module dep = new Module("bar");
         dep.setAttributes(TestUtil.<String, Object>map("cp", path2, "1", "2"));
         module.addDependency(dep);
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         task.setIncludeDependencies(true);
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, new File("b"), new File("a"), new File("c/d/e"),
                 new File("88/ee"), new File("555")); 
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", path1), module.getAttributes());
         assertEquals(TestUtil.<String, Object>map("cp", path2, "1", "2"), dep.getAttributes());
     }
     
     public void testSingleClasspathAttribute_WithClasspathProperty_ModuleWithSingleDependency_DepsNotIncluded()
     {
         final Path path1 = new Path(project);
         path1.createPathElement().setPath("b");
         path1.createPathElement().setPath("a");
         path1.createPathElement().setPath("c/d/e");
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         path2.createPathElement().setPath("555");
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", path1));
         final Module dep = new Module("bar");
         dep.setAttributes(TestUtil.<String, Object>map("cp", path2, "1", "2"));
         module.addDependency(dep);
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         task.setIncludeDependencies(false);
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, new File("b"), new File("a"), new File("c/d/e")); 
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", path1), module.getAttributes());
         assertEquals(TestUtil.<String, Object>map("cp", path2, "1", "2"), dep.getAttributes());
     }
     
     public void testSingleClasspathAttribute_WithClasspathProperty_ModuleWithSingleDependency_DefaultDepsIncluded()
     {
         final Path path1 = new Path(project);
         path1.createPathElement().setPath("b");
         path1.createPathElement().setPath("a");
         path1.createPathElement().setPath("c/d/e");
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         path2.createPathElement().setPath("555");
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", path1));
         final Module dep = new Module("bar");
         dep.setAttributes(TestUtil.<String, Object>map("cp", path2, "1", "2"));
         module.addDependency(dep);
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, new File("b"), new File("a"), new File("c/d/e")); 
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", path1), module.getAttributes());
         assertEquals(TestUtil.<String, Object>map("cp", path2, "1", "2"), dep.getAttributes());
     }
     
     public void testSingleClasspathAttribute_CyclingDependency()
     {
         final Path path1 = new Path(project);
         path1.createPathElement().setPath("b");
         path1.createPathElement().setPath("a");
         path1.createPathElement().setPath("c/d/e");
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", path1));
         final Module dep = new Module("bar");
         dep.setAttributes(TestUtil.<String, Object>map("cp", path2, "1", "2"));
         module.addDependency(dep);
         dep.addDependency(module);
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         task.setIncludeDependencies(true);
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, new File("b"), new File("a"), new File("c/d/e"), new File("88/ee")); 
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("attrib", "1", "attrib2", "3", "cp", path1), module.getAttributes());
         assertEquals(TestUtil.<String, Object>map("cp", path2, "1", "2"), dep.getAttributes());
     }
     
     public void testSingleClasspathAttribute_DiamondDependencyStructure()
     {
         final Path path1 = new Path(project);
         path1.createPathElement().setPath("b");
         path1.createPathElement().setPath("a");
         path1.createPathElement().setPath("c/d/e");
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         
         final Path path3 = new Path(project);
         path3.createPathElement().setPath("aaa");
         
         final Path path4 = new Path(project);
         path4.createPathElement().setPath("bbb");
         path4.createPathElement().setPath("555");
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("cp", path1));
         final Module dep1 = new Module("bar");
         dep1.setAttributes(TestUtil.<String, Object>map("cp", path2, "1", "2"));
         final Module dep2 = new Module("bar");
         dep2.setAttributes(TestUtil.<String, Object>map("cp", path3));
         final Module dep3 = new Module("bar");
         dep3.setAttributes(TestUtil.<String, Object>map("cp", path4));
         
         module.addDependency(dep1);
         module.addDependency(dep2);
         dep1.addDependency(dep3);
         dep2.addDependency(dep3);
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         task.setIncludeDependencies(true);
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, new File[]{new File("b"), new File("a"), new File("c/d/e")},
                 TestUtil.set(new File("88/ee"), new File("555"), new File("aaa"), new File("bbb"))); 
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("cp", path1), module.getAttributes());
         assertEquals(TestUtil.<String, Object>map("cp", path2, "1", "2"), dep1.getAttributes());
         assertEquals(TestUtil.<String, Object>map("cp", path3), dep2.getAttributes());
         assertEquals(TestUtil.<String, Object>map("cp", path4), dep3.getAttributes());
     }
     
     public void testClasspathAttributeAndElement_SingleModule()
     {
         final Path path1 = new Path(project);
         path1.createPathElement().setPath("b");
         path1.createPathElement().setPath("a");
         path1.createPathElement().setPath("c/d/e");
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2));
         
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         task.createClasspathAttribute().setName("cp3");
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, TestUtil.set(new File("b"), new File("a"),
                 new File("c/d/e"), new File("88/ee")), Collections.<File>emptySet()); 
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2), module.getAttributes());
     }
     
     public void testClasspathAttributeAndElement_SingleModule_RepeatedClasspathAttribute()
     {
         final Path path1 = new Path(project);
         path1.createPathElement().setPath("b");
         path1.createPathElement().setPath("a");
         path1.createPathElement().setPath("c/d/e");
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2));
         
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.setClasspathAttribute("cp");
         task.createClasspathAttribute().setName("cp3");
         task.createClasspathAttribute().setName("cp"); // repeated
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, TestUtil.set(new File("b"), new File("a"),
                 new File("c/d/e"), new File("88/ee")), Collections.<File>emptySet()); 
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2), module.getAttributes());
     }
     
     public void testClasspathElements_SingleModule()
     {
         final Path path1 = new Path(project);
         path1.createPathElement().setPath("b");
         path1.createPathElement().setPath("a");
         path1.createPathElement().setPath("c/d/e");
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2));
         
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.createClasspathAttribute().setName("cp");
         task.createClasspathAttribute().setName("cp3");
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, TestUtil.set(new File("b"), new File("a"),
                 new File("c/d/e"), new File("88/ee")), Collections.<File>emptySet()); 
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2), module.getAttributes());
     }
     
     public void testClasspathElements_SingleModule_EmptyClasspathAttribute()
     {
         final Path path1 = new Path(project);
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2));
         
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.createClasspathAttribute().setName("cp");
         task.createClasspathAttribute().setName("cp3");
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, Collections.singleton(new File("88/ee")), Collections.<File>emptySet()); 
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2), module.getAttributes());
     }
     
     public void testClasspathElements_SingleModule_EmptyClasspath()
     {
         final Path path1 = new Path(project);
         final Path path2 = new Path(project);
         
         final Module module = new Module("foo");
         module.setAttributes(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2));
         
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.createClasspathAttribute().setName("cp");
         task.createClasspathAttribute().setName("cp3");
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, Collections.<File>emptySet(), Collections.<File>emptySet()); 
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2), module.getAttributes());
     }
     
     public void testClasspathElements_ModuleWithDeps_DependenciesIncluded()
     {
         final Path path1 = new Path(project);
         path1.createPathElement().setPath("b");
         path1.createPathElement().setPath("a");
         path1.createPathElement().setPath("c/d/e");
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         
         final Path path3 = new Path(project);
         path3.createPathElement().setPath("111");
         
         final Path path4 = new Path(project);
         path4.createPathElement().setPath("222");
         
         final Module module = new Module("foo/");
         module.setAttributes(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2));
         final Module dep1 = new Module("bar/");
         dep1.setAttributes(TestUtil.<String, Object>map("cp", path3));
         final Module dep2 = new Module("baz/");
         dep2.setAttributes(TestUtil.<String, Object>map("cp3", path4));
         module.addDependency(dep1);
         dep1.addDependency(dep2);
         
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.createClasspathAttribute().setName("cp");
         task.createClasspathAttribute().setName("cp3");
         task.setIncludeDependencies(true);
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, TestUtil.set(new File("b"), new File("a"),
                 new File("c/d/e"), new File("88/ee")), TestUtil.set(new File("111"), new File("222")));
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2), module.getAttributes());
     }
     
     public void testClasspathElements_ModuleWithDeps_DependenciesNotIncluded()
     {
         final Path path1 = new Path(project);
         path1.createPathElement().setPath("b");
         path1.createPathElement().setPath("a");
         path1.createPathElement().setPath("c/d/e");
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         
         final Path path3 = new Path(project);
         path3.createPathElement().setPath("111");
         
         final Path path4 = new Path(project);
         path4.createPathElement().setPath("222");
         
         final Module module = new Module("foo/");
         module.setAttributes(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2));
         final Module dep1 = new Module("bar/");
         dep1.setAttributes(TestUtil.<String, Object>map("cp", path3));
         final Module dep2 = new Module("baz/");
         dep2.setAttributes(TestUtil.<String, Object>map("cp3", path4));
         module.addDependency(dep1);
         dep1.addDependency(dep2);
         
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.createClasspathAttribute().setName("cp");
         task.createClasspathAttribute().setName("cp3");
         task.setIncludeDependencies(false);
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, TestUtil.set(new File("b"), new File("a"),
                 new File("c/d/e"), new File("88/ee")), Collections.<File>emptySet());
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2), module.getAttributes());
     }
     
     public void testClasspathElements_ModuleWithDeps_DependenciesIncludedByDefault()
     {
         final Path path1 = new Path(project);
         path1.createPathElement().setPath("b");
         path1.createPathElement().setPath("a");
         path1.createPathElement().setPath("c/d/e");
         
         final Path path2 = new Path(project);
         path2.createPathElement().setPath("88/ee");
         
         final Path path3 = new Path(project);
         path3.createPathElement().setPath("111");
         
         final Path path4 = new Path(project);
         path4.createPathElement().setPath("222");
         
         final Module module = new Module("foo/");
         module.setAttributes(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2));
         final Module dep1 = new Module("bar/");
         dep1.setAttributes(TestUtil.<String, Object>map("cp", path3));
         final Module dep2 = new Module("baz/");
         dep2.setAttributes(TestUtil.<String, Object>map("cp3", path4));
         module.addDependency(dep1);
         dep1.addDependency(dep2);
         
         PropertyHelper.setProperty(project, "in", module);
         
         task.setModuleProperty("in");
         task.setOutputProperty("out");
         task.createClasspathAttribute().setName("cp");
         task.createClasspathAttribute().setName("cp3");
         
         task.execute();
         
         final Object outObject = PropertyHelper.getProperty(project, "out");
         assertTrue(outObject instanceof Path);
         assertClasspath((Path) outObject, TestUtil.set(new File("b"), new File("a"),
                 new File("c/d/e"), new File("88/ee")), Collections.<File>emptySet());
         
         assertSame(module, PropertyHelper.getProperty(project, "in"));
         assertEquals(TestUtil.<String, Object>map("cp", path1, "cp2", "123", "cp3", path2), module.getAttributes());
     }
     
     private void assertClasspath(final Path classpath, final File... elements)
     {
         assertNotNull(classpath);
         final String[] locations = classpath.list();
         assertEquals(elements.length, locations.length);
         for (int i = 0; i < locations.length; ++i) {
             assertEquals(elements[i].getAbsolutePath(), locations[i]);
         }
     }
     
     // Primary elements are the paths defined in the main module. Other elements are the other paths.
     private void assertClasspath(final Path classpath, final File[] primaryElements, final Set<File> otherElements)
     {
         assertNotNull(classpath);
         final String[] locations = classpath.list();
         assertEquals(primaryElements.length + otherElements.size(), locations.length);
         for (int i = 0; i < primaryElements.length; ++i) {
             assertEquals(primaryElements[i].getAbsolutePath(), locations[i]);
         }
         final HashSet<String> actualOtherElements = new HashSet<String>();
         for (int i = primaryElements.length; i < locations.length; ++i) {
             actualOtherElements.add(locations[i]);
         }
         assertEquals(otherElements.size(), actualOtherElements.size());
         for (final File e : otherElements) {
             assertTrue(actualOtherElements.contains(e.getAbsolutePath()));
         }
     }
     
     // Primary elements are the paths defined in the main module. Other elements are the other paths.
     private void assertClasspath(final Path classpath, final Set<File> primaryElements, final Set<File> otherElements)
     {
         assertNotNull(classpath);
         final String[] locations = classpath.list();
         assertEquals(primaryElements.size() + otherElements.size(), locations.length);
         final HashSet<String> actualPrimaryElements = new HashSet<String>();
         for (int i = 0, n = primaryElements.size(); i < n; ++i) {
             actualPrimaryElements.add(locations[i]);
         }
         for (final File f : primaryElements) {
             assertTrue(actualPrimaryElements.contains(f.getAbsolutePath()));
         }
         final HashSet<String> actualOtherElements = new HashSet<String>();
         for (int i = primaryElements.size(); i < locations.length; ++i) {
             actualOtherElements.add(locations[i]);
         }
         assertEquals(otherElements.size(), actualOtherElements.size());
         for (final File e : otherElements) {
             assertTrue(actualOtherElements.contains(e.getAbsolutePath()));
         }
     }
 }
