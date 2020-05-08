 package org.cichonski.ontviewer.servlet;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * 
  * @author paulcichonski
  *
  */
 public class DynamicPathBuilderTest extends TestCase {
 	private static final String SERVLET_PATH = "/ont-viewer/vocabs";
 	
     public DynamicPathBuilderTest(String testName) {
         super(testName);
     }
 
     /**
      * @return the suite of tests being tested
      */
     public static Test suite() {
         // adds all methods beginning with 'test' to the suite.
         return new TestSuite(DynamicPathBuilderTest.class);
     }
     
     public void testBuildRootPath(){
     	final DynamicPathBuilder pathBuilder = new DynamicPathBuilder(SERVLET_PATH);
     	String fakeResource = "css/resource.css";
     	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../" + fakeResource);
     	pathBuilder.pushLocalPath("local1");
     	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../" + fakeResource);
     	pathBuilder.pushLocalPath("local2");
     	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../../" + fakeResource);
     	pathBuilder.pushLocalPath("local3");
     	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../../../" + fakeResource);
     	//now pop
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../../" + fakeResource);
     	// push once more just for good measure
     	pathBuilder.pushLocalPath("local3");
     	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../../../" + fakeResource);
     	// pop all
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../../" + fakeResource);
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../../" + fakeResource);
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../" + fakeResource);
     	pathBuilder.popLocalPath(); // shouldn't do anything
     	assertEquals(pathBuilder.buildRootPath(fakeResource),  "../" + fakeResource);
     }
     	
     public void testBuildPath(){
     	final DynamicPathBuilder pathBuilder = new DynamicPathBuilder(SERVLET_PATH);
     	String classLabel = "/a bad class"; // can't allow '/'
     	Exception expectedException = null;
     	try {
     		pathBuilder.buildPath(classLabel);
     	} catch (IllegalStateException e){
     		expectedException = e;
     	}
     	assertNotNull(expectedException);
     	classLabel = "a good class";
     	String classLabelNoSpaces = "agoodclass";
     	assertEquals(pathBuilder.buildPath(classLabel), SERVLET_PATH + "/" +  classLabelNoSpaces);
     	
     	// now try with some local contexts
     	pathBuilder.pushLocalPath("local1");
     	assertEquals(pathBuilder.buildPath(classLabel), SERVLET_PATH + "/local1/" +  classLabelNoSpaces);
     	pathBuilder.pushLocalPath("local2");
     	assertEquals(pathBuilder.buildPath(classLabel), SERVLET_PATH + "/local1/local2/" +  classLabelNoSpaces);
     	pathBuilder.pushLocalPath("local3");
     	assertEquals(pathBuilder.buildPath(classLabel), SERVLET_PATH + "/local1/local2/local3/" +  classLabelNoSpaces);
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildPath(classLabel), SERVLET_PATH + "/local1/local2/" +  classLabelNoSpaces);
     	pathBuilder.pushLocalPath("blah");
     	assertEquals(pathBuilder.buildPath(classLabel), SERVLET_PATH + "/local1/local2/blah/" +  classLabelNoSpaces);
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildPath(classLabel), SERVLET_PATH + "/local1/local2/" +  classLabelNoSpaces);
     	pathBuilder.popLocalPath();
      	assertEquals(pathBuilder.buildPath(classLabel), SERVLET_PATH + "/local1/" +  classLabelNoSpaces);
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildPath(classLabel), SERVLET_PATH + "/" +  classLabelNoSpaces);
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildPath(classLabel), SERVLET_PATH + "/" +  classLabelNoSpaces);
     }
     
     public void testBuildIncomingRequestPath(){
     	final DynamicPathBuilder pathBuilder = new DynamicPathBuilder(SERVLET_PATH);
     	String classLabel = "/a bad class"; // can't allow '/'
     	Exception expectedException = null;
     	try {
     		pathBuilder.buildIncomingRequestPath(classLabel);
     	} catch (IllegalStateException e){
     		expectedException = e;
     	}
     	assertNotNull(expectedException);
        	classLabel = "a good class";
     	String classLabelNoSpaces = "agoodclass";
     	assertEquals(pathBuilder.buildIncomingRequestPath(classLabel), "/" +  classLabelNoSpaces);
     	
     	// now try with some local contexts
     	pathBuilder.pushLocalPath("local1");
     	assertEquals(pathBuilder.buildIncomingRequestPath(classLabel), "/local1/" +  classLabelNoSpaces);
     	pathBuilder.pushLocalPath("local2");
     	assertEquals(pathBuilder.buildIncomingRequestPath(classLabel), "/local1/local2/" +  classLabelNoSpaces);
     	pathBuilder.pushLocalPath("local3");
     	assertEquals(pathBuilder.buildIncomingRequestPath(classLabel), "/local1/local2/local3/" +  classLabelNoSpaces);
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildIncomingRequestPath(classLabel), "/local1/local2/" +  classLabelNoSpaces);
     	pathBuilder.pushLocalPath("blah");
     	assertEquals(pathBuilder.buildIncomingRequestPath(classLabel), "/local1/local2/blah/" +  classLabelNoSpaces);
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildIncomingRequestPath(classLabel), "/local1/local2/" +  classLabelNoSpaces);
     	pathBuilder.popLocalPath();
      	assertEquals(pathBuilder.buildIncomingRequestPath(classLabel), "/local1/" +  classLabelNoSpaces);
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildIncomingRequestPath(classLabel), "/" +  classLabelNoSpaces);
     	pathBuilder.popLocalPath();
     	assertEquals(pathBuilder.buildIncomingRequestPath(classLabel), "/" +  classLabelNoSpaces);
     }
     
     public void testFileExtension(){
     	
     }
 }
