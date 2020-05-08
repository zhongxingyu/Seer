 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package sgde.dialogue;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  *
  * @author kdsweenx
  */
 public class DialogueMap {
     public static int END=Integer.MAX_VALUE;
     
     ArrayList<DialogueOption> map;
     
     public DialogueMap(String text) throws FileNotFoundException, IncorrectFormatException{
         Scanner reader=new Scanner(new File(text));
         DialogueOption DO=null;
         while(reader.hasNext()){
             String line=reader.next();
             
             if(line.startsWith("$")){
                 DO=new DialogueOption(grabNum(line));
             }
             else if(line.contains("#")){
                 DO.addText(getMText(line));
             }else if(line.contains("@")){
                 DO.addPC(getGoTo(line), getPCString(line));
             }
             
             //Need something to make it look not bad
         }
         
         //done?
     }
     
     private int grabNum(String n){
         int s=n.indexOf("$");
         if(n.contains("START")){
             return 0;
         }
         return Integer.parseInt(n.substring(s+1,n.indexOf("=")));
     }
     
     private String getMText(String ln){
         return ln.substring(ln.indexOf("="+1),ln.indexOf(";"));
     }
     
     private int getGoTo(String ln){
         String line=ln.substring(ln.indexOf(">")+1,ln.indexOf(";"));
         if(line.contains("END")){
             return END;
         }
         return Integer.parseInt(line);
     }
     
     private String getPCString(String ln){
         return ln.substring(ln.indexOf("{")+1,ln.indexOf(";"));
     }
     
 }
