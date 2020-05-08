 package edu.depaul.cdm.se.sbejbclient;
 
 import edu.depaul.cdm.se.sbejb.LGreeterBeanLocal;
 import edu.depaul.cdm.se.sbejb.RGreeterBeanRemote;
 import edu.depaul.cdm.se.sbejb.impl.SimpleGreeterBean;
 import java.util.Date;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 
 public class App 
 {
     
     public static void main( String[] args ) throws Exception
     {
         InitialContext context = new InitialContext();
         App app = new App();
         // 3 different variation on the client
                 app.runRemote(context);
         //        app.runLocal(context);
         //        app.runBean(context);
     }
     
     private void runRemote(Context context) throws Exception {
         String lookupKey = "java:global/edu.depaul.cdm.se_sb-ejb_ejb_1.0-SNAPSHOT/RGreeterBean";
         RGreeterBeanRemote remote = (RGreeterBeanRemote) context.lookup(lookupKey); 
         System.out.print(new Date());
         System.out.print(" ");
         System.out.println( remote.greetMe("Paul") );
     }
     
     private void runLocal(Context context) throws Exception {
         String lookupKey = "java:global/edu.depaul.cdm.se_sb-ejb_ejb_1.0-SNAPSHOT/LGreeterBean";
         LGreeterBeanLocal remote = (LGreeterBeanLocal) context.lookup(lookupKey); 
         System.out.print(new Date());
         System.out.print(" ");
         System.out.println( remote.greetMe("John") );
     }
     
     private void runBean(Context context) throws Exception {
         String lookupKey = "java:global/edu.depaul.cdm.se_sb-ejb_ejb_1.0-SNAPSHOT/SimpleGreaterBean";
        SimpleGreeterBean remote = (SimpleGreeterBean) context.lookup(lookupKey); 
         System.out.print(new Date());
         System.out.print(" ");
         System.out.println( remote.greetMe("Ringo") );
     }
 }
