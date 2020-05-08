 package com.thoughtworks.go;
 
 public class HelloWorld
 {
     public static void main(String[] args)
     {
         HelloWorld helloWorld = new HelloWorld();
         System.out.println(helloWorld.doesItWork());
     }
 
     public String doesItWork()
     {
         return "It works!";
     }
     
     public String getFoo()
     {
     	return "foo";
     }
     
     public String getBar()
     {
     	return "baz";
     }
 }

