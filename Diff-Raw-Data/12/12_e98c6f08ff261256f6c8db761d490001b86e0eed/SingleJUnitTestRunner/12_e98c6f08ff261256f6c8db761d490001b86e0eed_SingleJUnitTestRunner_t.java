 package org.junit.runner;
 
import org.junit.runner.notification.Failure;

 public class SingleJUnitTestRunner {
 	public static void main(String[] args) throws Exception {
 		String[] classAndMethod = args[0].split("#");
 		System.out.println("class: " + classAndMethod[0] + ", method: " + classAndMethod[1]);
 		Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);
 		Result result = new JUnitCore().run(request);
		if (result.wasSuccessful()) { 
            System.out.println("Success");
        } else { 
            System.out.println("Failure");
        }  
        for(Failure failure : result.getFailures()) {
            System.out.println(failure);
        }
 		System.exit(result.wasSuccessful() ? 0 : 1);
 	}
 }
