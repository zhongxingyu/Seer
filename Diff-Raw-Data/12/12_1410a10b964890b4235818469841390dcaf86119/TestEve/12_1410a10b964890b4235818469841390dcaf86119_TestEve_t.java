 package de.bit.camel.security;
 
 import de.bit.camel.security.services.EmpInfoService;
 
 public class TestEve extends TestUtils {
     public static void main(String[] args) {
         EmpInfoService client = createClient();
        String alice = client.getEmployeeInformation(10001);
         
         System.out.println("EMP " + alice);
         
        String bob = client.getEmployeeInformation(10002);
         
         System.out.println("EMP " + bob);
     }
 }
