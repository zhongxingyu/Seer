 package de.unifr.acp.trafo;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
import java.util.Set;
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.NotFoundException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TransClassTest {
 
     private static CtClass objectClass;
     private static final boolean verbose = true;
     private static final String TEST_BASICS_NAME = TestBasics.class
             .getCanonicalName();
     private static final String TEST_ARRAY_CLASS_NAME = TestArrayClass.class
             .getCanonicalName();
     private static final String TEST_EMPTY_CLASS_NAME = TestEmptyClass.class
             .getCanonicalName();
     private static final String TEST_INT_CLASS_NAME = TestIntClass.class
             .getCanonicalName();
     private static final String TEST_COMPOUND_CLASS_NAME = TestCompoundClass.class
             .getCanonicalName();
     private static final String TREE_NODE_NAME = TreeNode.class
             .getCanonicalName();
     
     // test data
     
     
     @BeforeClass
     public static void oneTimeSetup() throws Exception {
         objectClass = ClassPool.getDefault().get("java.lang.Object");
     }
 
     @Before
     public void setUp() throws Exception {
 
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
     @Test
     public void testTransClass() {
         fail("Not yet implemented");
     }
 
     @Test
     public void testTransformHierarchy() {
         fail("Not yet implemented");
     }
 
     @Test
     public void testStartTransform() {
         fail("Not yet implemented");
     }
 
     @Test
     public void testDoTransformDoesNotThrow() throws NotFoundException,
             ClassNotFoundException, IOException, CannotCompileException {
         ClassPool defaultPool = ClassPool.getDefault();
         CtClass target = defaultPool.get(TEST_BASICS_NAME);
         TransClass.doTransform(target, !target.getSuperclass().equals(objectClass));
         
         target = defaultPool.get(
                 TEST_ARRAY_CLASS_NAME);
         TransClass.doTransform(target, !target.getSuperclass().equals(objectClass));
     }
     
     @Test
     public void testDoTransformAndRun() throws Throwable {
         ClassPool defaultPool = ClassPool.getDefault();
         CtClass target = defaultPool.get(TEST_BASICS_NAME);
         TransClass.doTransform(target, !target.getSuperclass().equals(objectClass));
 //        if (target.isModified()) {
 //            target.debugWriteFile("bin");
 //        }
         javassist.Loader cl = new javassist.Loader(defaultPool);
         
         // run with javassist's class loader to enable 'reloading' of test class
         cl.run(TEST_BASICS_NAME, new String[] {});
     }
     
     @Test
     public void testDoTransformTreeNode() throws Throwable {
         ClassPool defaultPool = ClassPool.getDefault();
         CtClass target = defaultPool.get(TREE_NODE_NAME);
 //        TransClass.doTransform(target, !target.getSuperclass().equals(objectClass));
         TransClass.transformHierarchy(TREE_NODE_NAME);
 //        if (target.isModified()) {
 //            target.writeFile("bin");
 //        }
         javassist.Loader cl = new javassist.Loader(defaultPool);
         
         // run with javassist's class loader to enable 'reloading' of test class
         cl.run(TREE_NODE_NAME, new String[] { "280", "degenerate", "delegator" });
         //TreeNode.main(new String[] { "1280", "degenerate", "delegator" });
     }
 
     @Test
     public void testComputeReachableClasses() throws NotFoundException,
             IOException, CannotCompileException {
         TransClass tc = new TransClass(TEST_EMPTY_CLASS_NAME);
         List<String> expected = Arrays
                 .asList(TEST_EMPTY_CLASS_NAME);
         checkMap(tc, expected);
 
         tc = new TransClass(TEST_INT_CLASS_NAME);
         expected = Arrays.asList(TEST_INT_CLASS_NAME);
         checkMap(tc, expected);
 
         tc = new TransClass(TEST_COMPOUND_CLASS_NAME);
         expected = Arrays.asList(TEST_COMPOUND_CLASS_NAME,
                 TEST_EMPTY_CLASS_NAME,
                 TEST_INT_CLASS_NAME);
         checkMap(tc, expected);
 
         tc = new TransClass("de.unifr.acp.trafo.TestEmptySubclass");
         expected = Arrays.asList("de.unifr.acp.trafo.TestEmptySubclass",
                 TEST_EMPTY_CLASS_NAME);
         checkMap(tc, expected);
 
         tc = new TransClass("de.unifr.acp.trafo.TestCompoundSubclass");
         expected = Arrays.asList("de.unifr.acp.trafo.TestCompoundSubclass",
                 TEST_EMPTY_CLASS_NAME,
                 TEST_INT_CLASS_NAME,
                 TEST_COMPOUND_CLASS_NAME);
         checkMap(tc, expected);
 
     }
 
     /**
      * @param tc
      * @param expected
      * @throws NotFoundException
      * @throws IOException
      * @throws CannotCompileException
      */
     private void checkMap(TransClass tc, List<String> expected)
             throws NotFoundException, IOException, CannotCompileException {
         tc.computeReachableClasses();
        Set<CtClass> visited = tc.getVisited();
         if (verbose) {
             System.out.println("<<<");
            for (CtClass cls : visited) {
                 System.out.println(cls.getName());
             }
         }
         assertEquals(expected.size(), visited.size());
        for (CtClass cls : visited) {
             assertTrue(expected.contains(cls.getName()));
         }
     }
     
     private String contentFromFileName(String filename) throws FileNotFoundException {
         String path = "testoutputs/" + filename + ".out";
         Scanner scanner = null;
         try {
             scanner = new Scanner(new File(path));
             return scanner.useDelimiter("\\A").next();
         } finally {
             if (scanner != null) {
                 scanner.close();
             }
         }
     }
 
     @Test
     public void testCreateBody() throws NotFoundException,
             FileNotFoundException {
         CtClass target = ClassPool.getDefault().get(
                 TEST_EMPTY_CLASS_NAME);
         String result = TransClass.createBody(target, false);
         if (verbose)
             System.out.println(result);
         org.junit.Assert
                 .assertEquals(
                         "public void traverse__(de.unifr.acp.templates.Traversal__ t) {\n}",
                         result);
 
         target = ClassPool.getDefault().get(TEST_INT_CLASS_NAME);
         result = TransClass.createBody(target, false);
         if (verbose)
             System.out.println(result);
         org.junit.Assert
                 .assertEquals(
                         "public void traverse__(de.unifr.acp.templates.Traversal__ t) {\nt.visitPrimitive__(this, \"de.unifr.acp.trafo.TestIntClass.myField\");\n}",
                         result);
 
         result = TransClass.createBody(target, true);
         if (verbose)
             System.out.println(result);
         org.junit.Assert
                 .assertEquals(
                         "public void traverse__(de.unifr.acp.templates.Traversal__ t) {\nt.visitPrimitive__(this, \"de.unifr.acp.trafo.TestIntClass.myField\");\nsuper.traverse__(t);\n}",
                         result);
 
         target = ClassPool.getDefault().get(
                 TEST_COMPOUND_CLASS_NAME);
         result = TransClass.createBody(target, false);
         if (verbose)
             System.out.println(result);
         assertEquals(
                 "public void traverse__(de.unifr.acp.templates.Traversal__ t) {\nt.visit__(this, \"de.unifr.acp.trafo.TestCompoundClass.x1\", this.x1);\nt.visit__(this, \"de.unifr.acp.trafo.TestCompoundClass.x2\", this.x2);\n}",
                 result);
 
         target = ClassPool.getDefault()
                 .get(TEST_ARRAY_CLASS_NAME);
         result = TransClass.createBody(target, false);
         if (verbose)
             System.out.println(result);
         assertEquals(result, contentFromFileName("createBody-TestArrayClass"));
 
         target = ClassPool.getDefault().get("java.lang.String");
         result = TransClass.createBody(target, false);
         if (verbose)
             System.out.println(result);
         assertEquals(result, contentFromFileName("createBody-String"));
     }
 
 }
