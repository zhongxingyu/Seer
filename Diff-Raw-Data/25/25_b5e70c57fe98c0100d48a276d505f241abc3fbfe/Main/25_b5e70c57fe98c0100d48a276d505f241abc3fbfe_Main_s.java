 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package campassignment;
 
 
 
 /**
  *
  * @author tocas
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         
         Camp camp = new Camp(Integer.valueOf(args[1]));
         camp.importData(args[0]);
        //camp.setupLeaders();
        //camp.assignmentCampers();
         System.out.println(camp.toString());
         
        System.out.println(Camp.optimization(camp).toString());
 
     }
 
 }
 
