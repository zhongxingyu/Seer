 package runner;
 
 import java.util.Enumeration;
 
 import gov.nasa.jpf.Config;
 import gov.nasa.jpf.JPF;
 
 public class JPFExample {
 	public static void main(String[] args) {
		Enumeration<?> elements = JPF.createConfig(new String[] {})
				.propertyNames();
		while (elements.hasMoreElements()) {
 			System.out.println(elements.nextElement().toString());
 		}
 		Config config = JPFRunner
 				.createConfig(
 						"/home/godfried/dev/go/src/github.com/godfried/impendulo/tool/jpf/example/BoundedBuffer.jpf",
 						"BoundedBuffer",
 						"/home/godfried/dev/go/src/github.com/godfried/impendulo/tool/jpf/bin/");
		System.out.println(JPFRunner.run(config));
 	}
 
 }
