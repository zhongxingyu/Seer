 package de.bit.camel.security;
 
 import de.bit.camel.security.services.EmpInfoService;
 
 public class TestBob extends TestUtils {
     public static void main(String[] args) {
         EmpInfoService client = createClient();
        String bob = client.getEmployeeInformation("10002");
         
         System.out.println("EMP " + bob);
     }
 }
