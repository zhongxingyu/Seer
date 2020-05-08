 package com.lesson;
 
 public class Main {
 
     public static void main(String[] args){
         Field field = new Field();
         field.eraseField();
         field.showField();
 
         for (int i = 10; i >= 1; i-= 2) {
             System.out.println(i);
         }
     }
 }
