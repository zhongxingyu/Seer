 package org.junit.runner;
 
 public class SingleJUnitTestRunner {
 	public static void main(String[] args) throws Exception {
 		String[] classAndMethod = args[0].split("#");
 		System.out.println("class: " + classAndMethod[0] + ", method: " + classAndMethod[1]);
 		Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);
 		Result result = new JUnitCore().run(request);
		System.out.println(result.wasSuccessful());
 		System.exit(result.wasSuccessful() ? 0 : 1);
 	}
 }
