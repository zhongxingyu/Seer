 package org.wyona.commons.test;
 
 import org.wyona.commons.io.Path;
 
 /**
  *
  */
 public class HelloWorld {
 
     /**
      *
      */
     public static void main(String[] args) {
         System.out.println("Hello World!");
 
         Path path = new Path("/hello/world.txt");
         System.out.println("Path: " + path);
         System.out.println("Parent: " + path.getParent());
         System.out.println("Parent of parent: " + path.getParent().getParent());
        System.out.println("Parent of parent or parent: " + path.getParent().getParent().getParent());
     }
 }
