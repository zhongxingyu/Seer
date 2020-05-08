 /*
  * Test.java
  *
  * Created on July 29, 2007, 10:13 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package edu.gatech.statics.modes.equation.parser;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class Test {
     
     public static void main(String args[]) {
         
        String expression = "-1+(-1-2 + (.1-1))-3";
         System.out.println(Parser.evaluate(expression));
         System.out.println(new Parser().parse(expression).printout());
        float test = (float) -1+(-1-2 + (.1f-1))-3;
         
        System.out.println(test);
     }
     
 }
