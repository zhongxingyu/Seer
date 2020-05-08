 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package alfredproject;
 
 /**
  *
  * @author Ben
  */
 public class BasicWebCommands {
      public static String url = "";
      public static void BasicWebCommands(String command){
         
         determiner(command);
         //initializes browser
         BareBonesBrowserLauncher bbbl = new BareBonesBrowserLauncher();
         //opens url
         bbbl.openURL(url);
        url="";
         
         
     }
     private static void determiner(String command){
         if(command.contains("google")){
             Google(command);
         }else{
             if(command.contains("wikipedia")){
                 Wikipedia(command);
             }else{
                 //wolfram alpha
             }
         }
 
     }
     private static void Google(String command){
         //cut command
         int index = command.indexOf("google ");
         //compensate for google
         command = command.substring(index + 6);
         String[] words = new String[100];
         words = command.split("\\s+");
         //turn into url
         //to create google url: www.google.com/search?as_q=keyword+keyword2+keyword3
         url = "www.google.com/search?as_q=";
         
         int i = 0;
         try{
         while(words[i]!=null){
             url = url + words[i];
             url = url + "+";
             i++;
         }
         }catch(ArrayIndexOutOfBoundsException a){
             System.err.print("");
         }
         
                 //responds with finishing statement
         Alfredproject ap = new Alfredproject();
         ap.OutputString= ap.FinishingStatement;
         
     }
     private static void Wikipedia(String command){
         //cut command
         
         int index = command.indexOf("wikipedia ");
         //compensate for wikipedia
         command = command.substring(index + 10);
         String[] words = new String[100];
         words = command.split("\\s+");
         //turn into url
         //to create wikipedia url
         //www.wikipedia.org/wiki/keyword_keyword2_keyword3_
         url = "www.wikipedia.org/wiki/";
         int i = 0;
         try{
         while(words[i]!=null){
             url = url + words[i];
             url = url + "_";
             i++;
         }
         }catch(ArrayIndexOutOfBoundsException a){
             System.err.print("");
         }
                 //responds with finishing statement
         Alfredproject ap = new Alfredproject();
         ap.OutputString= ap.FinishingStatement;
     }
     
 }
