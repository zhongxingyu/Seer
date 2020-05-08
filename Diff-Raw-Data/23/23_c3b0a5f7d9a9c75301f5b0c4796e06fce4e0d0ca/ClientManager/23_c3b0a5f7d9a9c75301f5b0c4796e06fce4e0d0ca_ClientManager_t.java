 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package clientmanager;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  *
  * @author Scare
  */
 public class ClientManager {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws InvalidClientNumberException{
         // TODO code application logic here
         
         ArrayList<Client> allClients = new ArrayList<>();
         
         PhoneService.setMonthlyCostPhoneService(10);
         InternetService.setMonthlyCostM20(20);
         InternetService.setMonthlyCostM100(100);
         
         InternetService i1 = new InternetService("joaquim@sapo.pt", InternetService.internetOptions.M20);
         InternetService i2 = new InternetService("maria@sapo.pt", InternetService.internetOptions.M100);
         
         PhoneService p1 = new PhoneService("PT", 123456789);
         
         Client c1 = new Client("Joaquim",12221234);
         Client c2 = new Client("Maria",12221234);
         
         c1.addClientService(i1);
         c1.addClientService(p1);
         c2.addClientService(i2);
         
         allClients.add(c1);
         allClients.add(c2);
         
         SortArrayList NAME = new SortArrayList();
         
         Collections.sort(allClients, NAME); 
         
         System.out.println("Profits: ");
         for(Client c:allClients)
             System.out.println("Client name: " + c.getName() + ";\nCLient Number: " + c.getClientNumber() + ";\nMonthly Payment: " + c.calculateMonthlyPayment() + ";\n\n");
         
         
         
     }
 }
