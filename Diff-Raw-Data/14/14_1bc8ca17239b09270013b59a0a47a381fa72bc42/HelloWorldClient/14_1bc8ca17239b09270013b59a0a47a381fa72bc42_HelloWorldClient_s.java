 package demo;
 
 import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
 
 public class HelloWorldClient {
	public static void main(String args[]){
 		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
 		factory.setServiceClass(HelloWorld.class);
 		factory.setAddress("http://localhost:9000/helloWorld");
 		HelloWorld client = (HelloWorld) factory.create();
		String reply = client.sayHello("hello world"); 
 		System.out.println(reply);
 	}
 
 }
