 package com.jadekler.app;
 
 import java.util.Random;
 
 /**
  * Hello world!
  *
  */
 public class ProportionalCountries 
 {
    public String[] countryNames = {"USA", "China", "Vatican", "SA"};
    public int[] countryPops = {31300, 1351000000, 798, 511900000};
     public int[] popProportionalBuckets;
     public int popSum;
 
     public static void main( String[] args )
     {
         ProportionalCountries pc = new ProportionalCountries();
        
        for (int i = 0; i < 10; i++) {
            String country = pc.getRandomCountry();
            
            System.out.println(country);
        }
     }
 
     public ProportionalCountries() {
         int popSum = 0;
 
        for (int i = 0; i < this.countryPops.length; i++) {
             popSum += this.countryPops[i];
         }
 
         this.popSum = popSum;
 
         this.popProportionalBuckets = new int[this.countryPops.length];
 
        for (int i = 0; i < this.popProportionalBuckets.length; i++) {
            int proportion = (int)((1.0*this.countryPops[i]/popSum)*100);
            this.popProportionalBuckets[i] = proportion;
         }
     }
 
     public String getRandomCountry() {
         int randomNumber = getRandNum(1, 100);
         int propSum = 0;
 
        for (int i = 0; i < this.popProportionalBuckets.length; i++) {
             if (randomNumber < propSum) {
                return this.countryNames[i-1];
             } else {
                 propSum += this.popProportionalBuckets[i];
             }
         }
 
        if (randomNumber < propSum) {
            return this.countryNames[this.popProportionalBuckets.length-1];
        }

         return "Nothing found! Weird..";
     }
 
     public int getRandNum(int min, int max) {
         Random rand = new Random();
         return rand.nextInt(max-min)+min;
     }
 }
