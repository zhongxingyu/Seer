 package com.mycompany.app;
 
 public class App 
 {
     public static void main( String[] args )
     {
   	  String str1 = "str";
   	  String str2 = "str";
   	  System.out.println("Hello World! "+str1.equalsIgnoreCase(str2));
 
   	  String test1 = "123";
   	  char[] test2 = {'3','2','1'};
   	  test1 = String.copyValueOf(test2);
 
   	  System.out.println("Hello World! "+test1);
  	  System.out.println("Hello World! "+test2);
     }
 }
