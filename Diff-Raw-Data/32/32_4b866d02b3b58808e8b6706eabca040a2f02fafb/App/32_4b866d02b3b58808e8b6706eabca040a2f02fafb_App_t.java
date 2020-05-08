 package com.stephenn.problems.knapsack.genetic;
 
 
 public class App 
 {
     public static void main( String[] args )
     {
    	float[] prices = new float[] {2.15f, 2.75f, 3.35f, 3.55f, 4.20f, 5.80f};
        Tournament t = new Tournament(10, prices, 15.05f);
         
         for (int i=0; i< 3; i++){
         	t.display();
         	t.cycle();
         }
     }
 }
