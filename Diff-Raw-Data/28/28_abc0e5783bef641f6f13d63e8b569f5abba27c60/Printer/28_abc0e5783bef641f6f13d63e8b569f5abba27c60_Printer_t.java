 
 package com.myapp.r;
 
import java.io.PrintStream;

 public class Printer
 {
    public static PrintStream out;

     public static void print(String message)
     {
        out.println(message);
         return;
     }
 }
