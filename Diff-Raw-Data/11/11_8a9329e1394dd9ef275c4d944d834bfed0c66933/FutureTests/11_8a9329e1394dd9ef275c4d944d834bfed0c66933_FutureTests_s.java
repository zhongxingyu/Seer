 package soot.jimple.infoflow.test.junit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import soot.jimple.infoflow.Infoflow;
 
 /**
  * ontains test for functionality which is currently not supported by FlowDroid.
  * 
  */
 public class FutureTests extends JUnitTests {
 
 	// Subtraktions-Operator wird von soot nicht ausgewertet
 	@Test
 	public void mathTest() {
 		Infoflow infoflow = initInfoflow();
 		List<String> epoints = new ArrayList<String>();
 		epoints.add("<soot.jimple.infoflow.test.OperationSemanticTestCode: void mathTestCode()>");
 		infoflow.computeInfoflow(path, epoints, sources, sinks);
 		negativeCheckInfoflow(infoflow);
 	}
 
 	// static initialization is not performed correctly on Soot
 	@Test
 	public void staticInit1Test() {
 		Infoflow infoflow = initInfoflow();
 		List<String> epoints = new ArrayList<String>();
 		epoints.add("<soot.jimple.infoflow.test.StaticTestCode: void staticInitTest()>");
 		infoflow.computeInfoflow(path, epoints, sources, sinks);
 		checkInfoflow(infoflow, 1);
 	}
 
 	//deleting tainted aliases of memory locations requires must-alias analysis, which is not used by FlowDroid
 	@Test
 	public void returnOverwriteTest7() {
 		Infoflow infoflow = initInfoflow();
 		List<String> epoints = new ArrayList<String>();
 		epoints.add("<soot.jimple.infoflow.test.OverwriteTestCode: void returnOverwrite7()>");
 		infoflow.computeInfoflow(path, epoints, sources, sinks);
 		checkInfoflow(infoflow, 1);
 		Assert.assertEquals(1, infoflow.getResults().size());
 	}
 	
 	//converts a String to an Integer and back to String again
 	//is not working because of implicit flows
     @Test
     public void testStringConvert(){
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConvert()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		checkInfoflow(infoflow, 1);
     }
 
 }
