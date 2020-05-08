 package soot.jimple.infoflow.test.junit;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import soot.jimple.infoflow.Infoflow;
 /**
  * test taint propagation within java.net.URL and java.net.HttpURLConnection classes
  */
 public class HTTPTests extends JUnitTests {
 	
     @Test
     public void testURL() throws IOException{
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.HTTPTestCode: void testURL()>");
 		infoflow.computeInfoflow(path, epoints,sources, sinks);
 		
 		checkInfoflow(infoflow, 1);
     }
     
     @Test
     public void testConnection() throws IOException{
 //    	this.taintWrapper = true;
     	Infoflow infoflow = initInfoflow();
     	List<String> epoints = new ArrayList<String>();
     	epoints.add("<soot.jimple.infoflow.test.HTTPTestCode: void method1()>");
     	infoflow.computeInfoflow(path, epoints,sources, sinks);
 		
 		checkInfoflow(infoflow, 1);
     }
 
 }
