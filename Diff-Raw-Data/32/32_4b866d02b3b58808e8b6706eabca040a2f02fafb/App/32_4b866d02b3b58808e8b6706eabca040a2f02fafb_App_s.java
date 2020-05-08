 package com.stephenn.problems.knapsack.genetic;
 
 
 public class App 
 {
     public static void main( String[] args )
     {
        Tournament t = new Tournament(5, new float[]{1,2,3}, 20);
         
         for (int i=0; i< 3; i++){
         	t.display();
         	t.cycle();
         }
     }
 }
