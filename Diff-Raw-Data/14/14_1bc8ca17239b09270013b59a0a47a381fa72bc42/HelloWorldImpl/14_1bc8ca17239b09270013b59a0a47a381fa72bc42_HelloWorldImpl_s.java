 package demo;
 
 import javax.jws.WebService;
 
 @WebService(endpointInterface = "demo.HelloWorld", serviceName = "HelloWorld")
 public class HelloWorldImpl implements HelloWorld {
 
 	public String sayHello(String text) {
		System.out.println("Client says: " + text); 
		return "Hello Client"; 
 	}
 
 }
