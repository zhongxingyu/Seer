 package com.trifork.hrclient;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.trifork.hrclient.representation.Employee;
 import com.trifork.hrclient.representation.EmployeeResponse;
 import com.trifork.hrclient.representation.Link;
 import org.w3c.dom.Document;
 
 import java.net.URI;
 
 import static com.sun.jersey.api.client.ClientResponse.*;
 
 public class HrClient {
     public static final String BASE_URI = "http://localhost:8080/";
     public static final String REL_CREATE_EMPLOYEE = "http://com.trifork/hr/createemployee";
 
     public static void main( String[] args ) {
         final URI employeeUri = createEmployee("Client Himself");
         System.out.println("Employee created, uri is " + employeeUri);
 
         Employee employee = getEmployee(employeeUri);
         System.out.println("Retrived new employee from server: " + employee);
 
	    employee.name = "Client has a new name";
	    updateEmployee(employee, employeeUri);

	    Employee updatedEmployee = getEmployee(employeeUri);
	    System.out.println("Retrived updated employee from server: " + updatedEmployee);
	    if (!updatedEmployee.name.equals(employee.name)) {
		    throw new RuntimeException("Updated employee name was not as expected " + employee.name + ", but rather " + updatedEmployee.name);
	    }

	    deleteEmployee(employeeUri);
 
         try {
             getEmployee(employeeUri);
            throw new RuntimeException("This is strange! Employee " + employeeUri + " exists after delete");
         } catch (UniformInterfaceException e) {
             if (e.getResponse().getClientResponseStatus() != Status.NOT_FOUND) {
                 throw new RuntimeException("Expected 404 for employee after deletion, " + describeStatus(e.getResponse()));
             } else {
                 System.out.println("As expected, " + e.getResponse().toString());
             }
         }
     }
 
     private static Employee getEmployee(URI employeeUri) {
         return Client.create().resource(employeeUri).get(EmployeeResponse.class).employee;
     }
 
     private static URI createEmployee(String name) {
         final ClientResponse resp = Client.create().resource(BASE_URI).get(ClientResponse.class);
         System.out.println(resp);
         final Document entryPoint = resp.getEntity(Document.class);
 
         final Link createLink = XmlUtil.findLink(entryPoint, REL_CREATE_EMPLOYEE);
         if (createLink != null) {
             System.out.println("Found create link " + createLink);
             return doCreateEmployee(name, createLink);
         } else {
             throw new IllegalStateException("Unable to find link to create employee in server's entry point");
         }
     }
 
     private static URI doCreateEmployee(String name, Link createLink) {
         final Employee employee = new Employee(name);
         final ClientResponse resp = Client.create().resource(createLink.href).post(ClientResponse.class, employee);
         System.out.println(resp);
         if (resp.getClientResponseStatus() != Status.CREATED) {
             throw new RuntimeException("Unable to create employee " + employee +
                                                ", " + describeStatus(resp));
         }
         return resp.getLocation();
     }
 
     private static String describeStatus(ClientResponse createResp) {
         return "server returned status " + createResp.getStatus() + " " + createResp.getClientResponseStatus().getReasonPhrase();
     }
 
     private static void deleteEmployee(URI employeeUri) {
         final ClientResponse resp = Client.create().resource(employeeUri).delete(ClientResponse.class);
         System.out.println(resp);
         if (resp.getClientResponseStatus() != Status.OK && resp.getClientResponseStatus() != Status.NO_CONTENT) {
             throw new RuntimeException("Unable to delete employee at " + employeeUri +
                                                ", " + describeStatus(resp));
         }
     }
 
     private static void updateEmployee(Employee employee, URI employeeUri) {
         final ClientResponse resp = Client.create().resource(employeeUri).put(ClientResponse.class, employee);
         System.out.println(resp);
         if (resp.getClientResponseStatus() != Status.OK && resp.getClientResponseStatus() != Status.NO_CONTENT) {
             throw new RuntimeException("Unable to update employee at " + employeeUri +
                                                ", " + describeStatus(resp));
         }
     }
 
 }
