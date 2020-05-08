 package com.rmatcher.core.matcher;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ameen
  * Date: 4/18/13
  * Time: 8:19 PM
  */
import com.rmatcher.core.sentiment.SWN3;

 import java.io.*;
 
 public class Matcher {
     public static void main(String [] args) throws IOException {
        SWN3 swn3 = new SWN3();
        System.out.println(swn3.extract("bad","a"));
        System.out.println(swn3.extract("good","a"));
     }
 }
