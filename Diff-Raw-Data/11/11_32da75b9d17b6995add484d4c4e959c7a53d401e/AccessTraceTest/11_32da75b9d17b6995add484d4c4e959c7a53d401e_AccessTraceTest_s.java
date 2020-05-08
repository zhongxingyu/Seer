 package racefix;
 
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TestName;
 
 import sabazios.A;
 import sabazios.tests.DataRaceAnalysisTest;
 import sabazios.util.U;
 
 import com.ibm.wala.analysis.pointers.HeapGraph;
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.ipa.callgraph.CallGraph;
 import com.ibm.wala.ipa.cha.ClassHierarchyException;
 import com.ibm.wala.util.CancelException;
 import com.ibm.wala.util.collections.Filter;
 import com.ibm.wala.util.graph.Graph;
 import com.ibm.wala.util.graph.GraphSlicer;
 
 public class AccessTraceTest extends DataRaceAnalysisTest {
 
   @Rule
   public TestName name = new TestName();
   
   private final boolean printGraphs = false;
 
   public AccessTraceTest() {
     super();
     this.addBinaryDependency("racefix");
     this.addBinaryDependency("../lib/parallelArray.mock");
   }
   
   private void runTest(String startVariableName, String expected) throws ClassHierarchyException, CancelException, IOException {
     runTest(startVariableName, name.getMethodName(), expected);    
   }  
 
   private void runTest(String startVariableName, String raceMethod, String expected) throws ClassHierarchyException, CancelException,
       IOException {
     String testString;
     final String methodName = name.getMethodName();
 
     setup("Lracefix/Foo", methodName + "()V");
     A a = new A(callGraph, pointerAnalysis);
     a.precompute();
     CGNode cgNode = a.findNodes(".*" + raceMethod + ".*").get(0);
     int value = U.getValueForVariableName(cgNode, startVariableName);
     System.out.println(cgNode + "" + value);
     AccessTrace trace = new AccessTrace(a, cgNode, value);
     trace.compute();
     testString = trace.getTestString();
 
     if (printGraphs) {
 
       Graph<Object> prunedHP = GraphSlicer.prune(a.heapGraph, new Filter<Object>() {
         @Override
         public boolean accepts(Object o) {
           return o.toString().contains("Foo");
         }
 
       });
       
       Graph<CGNode> prunedCG = GraphSlicer.prune(a.callGraph, new Filter<CGNode>() {
 
         @Override
         public boolean accepts(CGNode o) {
           return o.toString().contains("Foo");
         }
       });
 
       a.dotGraph(prunedHP, methodName + "_HP", null);
       a.dotGraph(prunedCG, methodName + "_CG", null);
     }
 
     Assert.assertEquals(expected, testString);
   }
 
   @Test
   public void simple1() throws Exception {
     String startVariableName = "b";
     String expected = "O:Foo.simple1-new Foo$Dog\n";
     runTest(startVariableName, expected);
   }
 
   @Test
   public void simpleLabel() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + "O:Foo.simpleLabel-new Foo$Cat\n" + "O:Foo.simpleLabel-new Foo$Dog\n";
     runTest(startVariableName, expected);
   }
 
   @Test
   public void simpleLabel1() throws Exception {
     String startVariableName = "pufi";
     String expected = "O:Foo.simpleLabel1-new Foo$Cat\n";
     runTest(startVariableName, expected);
   }
 
   @Test
   public void simpleTwoLabelsDeep() throws Exception {
     String startVariableName = "fifi";
     String expected = "IFK:Foo$Cat.follows\n" + "IFK:Foo$Dog.chases\n" + "O:Foo.simpleTwoLabelsDeep-new Foo$Cat\n"
         + "O:Foo.simpleTwoLabelsDeep-new Foo$Cat\n" + "O:Foo.simpleTwoLabelsDeep-new Foo$Dog\n";
 
     runTest(startVariableName, expected);
   }
 
   @Test
   public void simpleWithUninteresting() throws Exception {
     String startVariableName = "fifi";
     String expected = "IFK:Foo$Dog.chases\n" + "O:Foo.simpleWithUninteresting-new Foo$Cat\n"
         + "O:Foo.simpleWithUninteresting-new Foo$Dog\n";
     runTest(startVariableName, expected);
   }
 
   @Test
   public void simplePhi() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + "IFK:Foo$Dog.loves\n" + "O:Foo.simplePhi-new Foo$Cat\n"
         + "O:Foo.simplePhi-new Foo$Dog\n";
     runTest(startVariableName, expected);
   }
 
   @Test
   public void notSoSimplePhi() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + "IFK:Foo$Dog.loves\n" + "O:Foo.notSoSimplePhi-new Foo$Cat\n"
         + "O:Foo.notSoSimplePhi-new Foo$Dog\n" + "O:Foo.notSoSimplePhi-new Foo$Cat\n";
     runTest(startVariableName, expected);
   }
 
   @Test
   public void simpleCalls() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + 
     		"O:Foo.simpleCalls-new Foo$Cat\n" + 
     		"O:Foo.simpleCalls-new Foo$Dog\n";
 
     runTest(startVariableName, "writeField", expected);
   }
   
   @Test
   public void simpleCalls2() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + 
         "O:Foo.simpleCalls2-new Foo$Cat\n" + 
         "O:Foo.simpleCalls2-new Foo$Dog\n";
     
     runTest(startVariableName, "writeField2", expected);
   }
   
   @Test
   public void simpleCalls3() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + 
         "O:Foo.simpleCalls3-new Foo$Cat\n" + 
         "O:Foo.simpleCalls3-new Foo$Dog\n";
     
     runTest(startVariableName, "writeField", expected);
   }
   
   @Test
   public void simpleCalls4() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + 
     		"O:Foo.simpleCalls4-new Foo$Cat\n" + 
     		"O:Foo.simpleCalls4-new Foo$Dog\n";
     
     runTest(startVariableName, "writeField4", expected);
   }
   
   @Test
   public void simpleWithReturn() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + 
     		"O:Foo.simpleWithReturn-new Foo$Cat\n" + 
     		"O:Foo.makeDog-new Foo$Dog\n";
     
     runTest(startVariableName, expected);
   }
   
   @Test
   public void simpleWithReturn2() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + 
         "O:Foo.simpleWithReturn2-new Foo$Cat\n" + 
        "O:Foo.makeDog-new Foo$Dog\n" +
         "O:Foo.makeDog-new Foo$Dog\n";
     
     runTest(startVariableName, expected);
   }
   
   @Test
   public void simpleWithReturn3() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + 
         "O:Foo.simpleWithReturn3-new Foo$Cat\n" + 
         "O:Foo.simpleWithReturn3-new Foo$Dog\n";
     
     runTest(startVariableName, expected);
   }
   
   @Test
   public void simpleWithFieldWrites() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + 
         "O:Foo.simpleWithFieldWrites-new Foo$Cat\n" + 
         "O:Foo.simpleWithFieldWrites-new Foo$Dog\n" +
         "O:Foo.simpleWithFieldWrites-new Foo$Cat\n";
     
     runTest(startVariableName, expected);
   }
   
   @Test
   public void simpleWithFieldWrites2() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Dog.chases\n" + 
         "O:Foo.simpleWithFieldWrites2-new Foo$Cat\n" + 
         "O:Foo.simpleWithFieldWrites2-new Foo$Dog\n";
     
     runTest(startVariableName, expected);
   }
   
   @Test
   public void simpleWithFieldWrites3() throws Exception {
     String startVariableName = "pufi";
     String expected = "IFK:Foo$Cat.follows\n" + 
     		"IFK:Foo$Dog.chases\n" + 
     		"O:Foo.simpleWithFieldWrites3-new Foo$Cat\n" + 
     		"O:Foo.simpleWithFieldWrites3-new Foo$Cat\n" + 
     		"O:Foo.simpleWithFieldWrites3-new Foo$Dog\n" + 
     		"O:Foo.simpleWithFieldWrites3-new Foo$Cat\n";
     
     runTest(startVariableName, expected);
   }
   
 }
