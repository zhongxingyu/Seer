 package com.jadekler.app;
 
 import java.util.Random;
 
 /**
  * Hello world!
  *
  */
 public class ProportionalCountries 
 {
    public String[] countryNames = {"USA", "China", "Vatican", "South Africa"};
    public int[] countryPops = {313900000, 1351000000, 798, 51190000};
     public int[] popProportionalBuckets;
     public int popSum;
 
     public static void main( String[] args )
     {
         ProportionalCountries pc = new ProportionalCountries();
        System.out.println(pc.getRandomCountry());
     }
 
     public ProportionalCountries() {
         int popSum = 0;
 
        for (int i = 0; i < this.countryPops.length-1; i++) {
             popSum += this.countryPops[i];
         }
 
         this.popSum = popSum;
 
         this.popProportionalBuckets = new int[this.countryPops.length];
 
        for (int i = 0; i < this.popProportionalBuckets.length-1; i++) {
            this.popProportionalBuckets[i] = (this.countryPops[i]/popSum)*100;
         }
     }
 
     public String getRandomCountry() {
         int randomNumber = getRandNum(1, 100);
         int propSum = 0;
 
        for (int i = 0; i < this.popProportionalBuckets.length-1; i++) {
             if (randomNumber < propSum) {
                return this.countryNames[i];
             } else {
                 propSum += this.popProportionalBuckets[i];
             }
         }
 
         return "Nothing found! Weird..";
     }
 
     public int getRandNum(int min, int max) {
         Random rand = new Random();
         return rand.nextInt(max-min)+min;
     }
 }
