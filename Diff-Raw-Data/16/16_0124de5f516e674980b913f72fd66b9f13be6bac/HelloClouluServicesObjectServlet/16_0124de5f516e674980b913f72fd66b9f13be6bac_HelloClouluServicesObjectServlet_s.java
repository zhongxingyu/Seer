 package com.cloulu.services.samples;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import com.cloulu.services.BackEndServiceObject;
import com.cloulu.services.BackEndServices;
import com.cloulu.services.ClouluBackEndServices;
 
 public class HelloClouluServicesObjectServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/plain");
 		response.setStatus(200);
 		PrintWriter writer = response.getWriter();
 		writer.println("Hello Cloulu from " + System.getenv("VCAP_APP_HOST")
 				+ ":" + System.getenv("VCAP_APP_PORT"));
 		String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
 		writer.print("VCAP_SERVICES = ");
 		writer.println(VCAP_SERVICES);
 
		BackEndServices backEndServices = new ClouluBackEndServices();
		backEndServices.initBackEndServices(VCAP_SERVICES);
 
 		writer.println();
 		writer.println("************ Services Info ************");
 
		for (String serviceName : backEndServices.getServiceNamesList()) {
			BackEndServiceObject backEndServiceObject = backEndServices
 					.getBackEndService(serviceName);
 			writer.println();
 			writer.println();
 			writer.print("* serviceName=" + serviceName);
 			writer.println(" *");
 			writer.println("- Using getXXX() Method");
 			writer.print("getHost() : " + backEndServiceObject.getHost());
 			writer.print("\t");
 			writer.print("getPort() : " + backEndServiceObject.getPort());
 			writer.print("\t");
 			writer.print("getUsername() : " + backEndServiceObject.getUsername());
 			writer.print("\t");
 			writer.print("getPassword() : " + backEndServiceObject.getPassword());
 			writer.print("\t");
 			writer.println("getDBName() : " + backEndServiceObject.getDBName());
 			writer.println("- Using getInfo(key) Method");
 			for (String key : backEndServiceObject.getKeys()) {
 				writer.print(key);
 				writer.print(" : ");
 				writer.print(backEndServiceObject.getInfo(key));
 				writer.print("\t");
 			}
 			writer.println();
 		}
 		writer.close();
 	}
 }
