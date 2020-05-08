 package mygame;
 
 public class BladeMain {
     public static void main(String[] args) {
         if(args.length==0){
             System.out.println("Please indicate usage as 'server' or 'client' in first argument.");
         }
         else if(args[0].toLowerCase().equals("server")){
             BladeServer.main(args);
             System.out.println("Starting Server");
         }
         else if(args[0].toLowerCase().equals("client")){
             BladeClient.main(args);
             System.out.println("Starting Client");
         }
         else{
             System.out.println("'"+args[0]+"' is not a proper command.");
         }
 
     }
 }
