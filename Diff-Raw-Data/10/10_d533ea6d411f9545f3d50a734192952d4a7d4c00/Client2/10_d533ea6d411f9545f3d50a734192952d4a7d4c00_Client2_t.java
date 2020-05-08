 
 
 
 package client;
 
 /**
  *
  * @author chenliang
  */
 import api.XTradeAPI;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.rmi.ConnectException;
 import java.rmi.registry.*;
 import object.Record;
 
 public class Client2 {
     private static final String HOST = "localhost";
     private static final int PORT = 1099;
     private static Registry registry;
 
     public static void main(String[] args)  throws Exception{
         
         if (args.length == 0)
         {
             registry = LocateRegistry.getRegistry(HOST, PORT);
         }
         else if (args.length == 1)
         {
             registry = LocateRegistry.getRegistry(args[0], PORT);        
         }
         else
             System.out.println("you can only imput one arguments as server IP Address.");
         //new XTradeAPI instance 
         XTradeAPI remoteXTrade = (XTradeAPI) registry.lookup(XTradeAPI.class.getSimpleName());
         
         System.out.println(remoteXTrade.hello()+": 2");
         
         
         boolean flag1 = true;
         boolean flag2 = true;        
         String inputline = new String();
         String[] loginCom = new String[5];
         String[] Com = new String[5];
         while(flag1 == true){
             System.out.println("Client2, pleaese type command - user username - to login:");
         
             InputStreamReader isr = new InputStreamReader(System.in);
             BufferedReader br= new BufferedReader(isr);
             inputline = br.readLine();
             loginCom = inputline.split(" ");
             
             if(loginCom.length == 1 && loginCom[0].equalsIgnoreCase("quit")){
                 flag2 = false;
                 flag1 = false;
             }
             else if(loginCom.length == 2 || "user".equals(loginCom[0])){
                 System.out.println(remoteXTrade.login(loginCom[1]));
                 System.out.println("Client2 Command List:\n"
                          + "1. buy symbol stock_number"
                          + "\n   ---purchase stock in the certain number"
                          + "\n2. sell symbol stock_number"
                          + "\n   ---sell the stocks you own"
                          + "\n3. query symbol"
                          + "\n   ---search stock infomation"
                          + "\n4. Stocklist (symbol)"
                          + "\n   ---show how many stocks you have"
                          + "\n5. balance"
                          + "\n   ---show your balance"
                          + "\n6. man"
                          + "\n   ---show the command list"
                          + "\n7. quit");
                 flag1 = false;           
             }  
             else 
             {
                 System.out.println("Syntax error (Hints: user ***)! OR type quit");
             }
         }
         while(flag2 == true)
         {      
             System.out.print("\nXTrade > ");
             InputStreamReader isr = new InputStreamReader(System.in);
             BufferedReader br= new BufferedReader(isr);
             inputline = br.readLine();
             Com = inputline.split(" ");
             if(Com.length == 1 && Com[0].equalsIgnoreCase("quit")){
                 flag2 = false;
             }
             else if(Com.length == 1 && Com[0].equalsIgnoreCase("man")){    
                 
                 System.out.println("Client2 Command List:\n"
                          + "1. buy symbol stock_number"
                          + "\n   ---purchase stock in the certain number"
                          + "\n2. sell symbol stock_number"
                          + "\n   ---sell the stocks you own"
                          + "\n3. query symbol"
                          + "\n   ---search stock infomation"
                          + "\n4. Stocklist (symbol)"
                          + "\n   ---show how many stocks you have"
                          + "\n5. balance"
                          + "\n   ---show your balance"
                          + "\n6. man"
                          + "\n   ---show the command list"
                          + "\n7. quit");
             }
             else if(Com.length == 1 && Com[0].equalsIgnoreCase("stocklist")){
                 for(Record r:remoteXTrade.getRecord(loginCom[1]))
                 {
                     System.out.println(r.toString());
                 }
             }
             else if(Com.length == 1 && Com[0].equalsIgnoreCase("balance")){
                 System.out.println(remoteXTrade.queryUser(loginCom[1]));
             }
             else if(Com.length == 2 && Com[0].equalsIgnoreCase("stocklist")){
                 Record r = remoteXTrade.isRecordExisted(loginCom[1], Com[1]);
                if(r!=null)
                {
                   System.out.println(r.toString());

                }
                else
                {
                    System.out.println("No matched record.");
                }
             }
             else if(Com.length == 2 && Com[0].equalsIgnoreCase("query")){
                 System.out.println(remoteXTrade.queryStock(Com[1]));
             }
             else if(Com.length == 2 && Com[0].equalsIgnoreCase("queryRecord")){
                 System.out.println(remoteXTrade.queryRecord(loginCom[1],Com[1]));
             }
             else if(Com.length == 3 && Com[0].equalsIgnoreCase("buy")){
                 try{
                     int buy_shares = Integer.parseInt(Com[2]);
                     System.out.println(remoteXTrade.buy(Com[1],loginCom[1], buy_shares));
                 }
                 catch(NumberFormatException e){
                     System.out.println("Invalid price!");
                     
                 } 
                 
             }
             else if(Com.length == 3 && Com[0].equalsIgnoreCase("sell")){
                 try{
                     int sell_shares = Integer.parseInt(Com[2]);
                     System.out.println(remoteXTrade.sell(Com[1],loginCom[1], sell_shares));
                 }
                 catch(NumberFormatException e){
                     System.out.println("Invalid Share Number!");
                     
                 } 
             }
             else
             {
                 System.out.println("Syntax error");
             }
         }
         
     }
 }
