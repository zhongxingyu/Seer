 package org.wyona.commons.test;
 
 import org.wyona.commons.io.FileUtil;
 import org.wyona.commons.io.Path;
 import org.wyona.commons.io.PathUtil;
 
 import java.io.File;
 
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
         System.out.println("Parent of parent of parent: " + path.getParent().getParent().getParent());
 
        System.out.println("Concat: " + PathUtil.concat("/home/user/tmp", "../.."));
        System.out.println("Concat: " + PathUtil.concat("/home/user/tmp/", "../../hugo.txt"));
 
        System.out.println("Concat: " + FileUtil.concat("/home/user/tmp", "../.."));
        System.out.println("Resolve: " + FileUtil.resolve(new File("/home/user/tmp"), new File("../..")));
        System.out.println("Resolve: " + FileUtil.resolve(new File("/home/user/tmp/"), new File("../../hugo.txt")));
     }
 }
