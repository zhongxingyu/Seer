 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Startup;
 
 import Facebook.FacebookInterface;
 
 /**
  *
  * @author mgauto504
  */
 public class NewMain {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         FacebookInterface fb=new FacebookInterface("AAACx4m11Mv4BAOlxeJECgGNcVjHlaVr2PCU7i5iNZC0jdDCeZBNhPRYPbrLAn7X8R3EJz7H73MZCpP9qn3Gvjsif5SXe8UZD");
         fb.whoami();
        fb.PostStatus("ew PVI");
     }
 }
