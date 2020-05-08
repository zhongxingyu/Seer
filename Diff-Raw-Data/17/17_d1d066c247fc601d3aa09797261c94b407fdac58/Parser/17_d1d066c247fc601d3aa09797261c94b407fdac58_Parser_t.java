 package blatt8;
 
 import java.net.InterfaceAddress;
 import java.util.ArrayList;
 import java.util.List;
import java.util.Stack;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Julian
  * Date: 07.06.13
  * Time: 18:48
  * To change this template use File | Settings | File Templates.
  */
 public class Parser {
 
     private static List<String> output = new ArrayList<String>() ;
 
     private static int position = 0;
 
     private static void set(String text){
         if (output.size() <= position ){
             output.add(text);
         }else {
             String current = output.get(position);
             current += text;
             output.set(position, current);
         }
     }
 
     private static void openS(){
         set("<S>");
         position += 1;
     }
 
     private static void closeS(){
         position -= 1;
         set("</S>");
     }
 
     private static void openE(){
         set("<E>");
         position += 1;
     }
 
     private static void closeE(){
         position -= 1;
         set("</E>");
     }
 
     private static void V(){
         set("<V>");
     }
 
     private static void E(){
         set("<E>");
     }
 
    /*private static Stack<Integer> tokens = new Stack<Integer>();

    public static void put(int token){
        tokens.push(token);
    }

    public static void build(){

        while (!tokens.empty()){
            int token = tokens.pop();

        }

    } */

     public static void put(int token){
 
         switch (token){
             case Scanner.LETTER:
                 V();
                 break;
             case Scanner.IF:
                 openS();
                 break;
             case Scanner.ELSE :
                 break;
             case Scanner.FI:
                 closeS();
                 break;
             case Scanner.PLUS :
             case Scanner.MINUS :
                 break;
             case Scanner.ONE :
             case Scanner.ZERO :
                     E();
                     break;
             case Scanner.DO:
                 break;
             case Scanner.OD:
                 break;
             case Scanner.WHILE :
                 openS();
                 break;
             case Scanner.END:
                 closeS();
                 break;
             case Scanner.BEGIN :
                 openS();
                 break;
             case Scanner.SEMI:
                 break;
             case Scanner.ASSIGN:
                 break;
 
         }
     }
 
 
     public static void print(){
         for(String line : output){
             System.out.println(line);
         }
     }
 
 
 
 }
