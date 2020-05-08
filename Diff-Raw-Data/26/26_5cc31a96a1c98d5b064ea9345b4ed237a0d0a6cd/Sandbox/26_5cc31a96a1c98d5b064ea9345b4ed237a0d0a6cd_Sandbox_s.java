 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.linfords.sandbox;
 
 /**
  *
  * @author slinford
  */
 public class Sandbox {
     
     public static void pyramid(final int height) {
         final int base = (height + 1) * 2 - 1;
         for (int i = 0; i < height; i++) {
             for (int j = 0; j <= height; j++) {
                if (j <)
                 System.out.print('*');
             }
             System.out.println();
         }
     }
     
     
     public static void main(String[] args) {
         pyramid(10);
     }
     
 }
