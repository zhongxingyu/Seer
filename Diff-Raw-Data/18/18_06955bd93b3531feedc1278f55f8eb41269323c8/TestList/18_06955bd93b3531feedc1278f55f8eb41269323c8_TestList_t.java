 package edu.berkeley.eduride.feedbackview.model;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jdt.core.dom.Annotation;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.junit.model.ITestCaseElement;
 import org.eclipse.jdt.junit.model.ITestElement;
 import org.eclipse.jdt.junit.model.ITestElementContainer;
 import org.eclipse.jdt.junit.model.ITestRunSession;
 
 public class TestList {
 	
 	private ASTparse parse;
	private String name;
 	public ArrayList<TestResult> test_results = new ArrayList<TestResult>();
 
 	public TestList(IProject proj, String javaFileName) {
 		parse = new ASTparse(proj,javaFileName);
 		//parse.getSource();
		name = javaFileName.substring(0,javaFileName.length()-5);
 		ArrayList<MethodDeclaration> methods = parse.get_methods_by_annotation("@Test");
 		for(MethodDeclaration method:methods){
 			ArrayList<Annotation> annotations = parse.get_annotations(method);
			test_results.add(new TestResult(annotations, name));
 		}
 	}
	
	public String getName() {
		return name;
	}
 //	public TestList(ITestRunSession session) {
 //		test_results = new ArrayList<TestResult>();
 //		ITestElementContainer container = (ITestElementContainer) session.getChildren()[0];
 //		ITestElement[] testElements = container.getChildren();
 //		for (int i= 0; i < testElements.length; i++){
 //			ITestElement x = testElements[i];
 //			
 //			String name = ((ITestCaseElement)x).getTestMethodName();
 //			String progress_state = x.getProgressState().toString();
 //			String result = x.getTestResult(true).toString();
 //			String failure_trace = "";
 //			if(x.getFailureTrace() != null){
 //				failure_trace = x.getFailureTrace().getTrace();
 //			}
 //			test_results.add(new TestResult(name, progress_state, result, failure_trace));
 //		}
 //		// TODO build test results into here based on constructor
 //	}
 
 }
