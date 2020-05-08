 import java.util.Arrays;
 import fj.F;  
 import java.awt.image.*;
 import java.util.Random;
 
 public class Dna {
     private Letter[] dna;
     private double cost;
     private BufferedImage image;
     private static Random rng;
 
     public Dna(Letter[] letters, BufferedImage img) {
         this.cost = -1;
         this.image = img;
         rng = new Random();
         this.dna = letters;
 
     }
 
     public Dna(int length, BufferedImage img) {
         this.cost = -1;
         this.image = img;
         rng = new Random();
         this.dna = this.random(length);
     }
 
     /**
      * Make a copy of mother but randomize some letters
      */
     public Dna mutate(int mutations) {
 
         Letter[] ret = Arrays.copyOf(dna , dna.length);
 
         for(int i = 0; i < mutations; i++) {
             int ind = Math.abs(rng.nextInt()%dna.length);
             ret[i] = Letter.randomLetter(image.getWidth(), image.getHeight());
         }
         return new Dna(ret, image);
     }
 
     // return the cost, if it hasn't been calculated, calculate it
     public double cost() {
         if(this.cost < 0) {
             this.cost = this.calculateCost();
         }
         return this.cost;
     }
 
     private double calculateCost() {
        return Ascii.approxCost(image, Arrays.asList(dna));
     }
 
     private Letter[] random(int letters) {
         Letter[] res = new Letter[letters];
         for(int i = 0; i < letters; i++) {
             res[i] = Letter.randomLetter(image.getWidth(), image.getHeight());
         }
         return res;
     }
 
     public String toString() {
         String s = "";
         for(Letter l : dna) {
             s += l.toString() + "\n";
         }
         return s;
     }
 
     
 
 
 
 
 
 
     // dark dwelling of abandoned functions
 
 
     // private static Letter[] modifyMutate(Letter[] mother, int mutations) {
     //     Letter[] ret = new Letter[mother.length];
 
     //     for(int i = 0; i < mother.length; i++) {
     //         ret[i] = mother[i];
     //     }
 
     //     for(int i = 0; i < mutations; i++) {
     //         int ind = Math.abs(rng.nextInt()%mother.length);
     //         int r,g,b,a;
     //         double x,y,rot,size;
 
     //         r = mother[ind].color.getRed() + (rng.nextInt()%100) - 50;
     //         g = mother[ind].color.getGreen() + (rng.nextInt()%100) - 50;
     //         b = mother[ind].color.getBlue() + (rng.nextInt()%100) - 50;
     //         a = mother[ind].color.getTransparency() + (rng.nextInt()%100) - 50;
 
     //         x = mother[ind].x * ((rng.nextDouble() * 2 + 9) / 10);
     //         x = mother[ind].x * ((rng.nextDouble() * 2 + 9) / 10);
     //         x = mother[ind].x * ((rng.nextDouble() * 2 + 9) / 10);
 
     //     }
     //     return ret;
     // }
 
 }
