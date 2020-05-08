 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package neural.network;
 
 import java.util.Random;
 
 /**
  *
  * @author heraclio
  */
 public class ExampleGenerator {
     int size;
     double rxmin;
     double rxmax;
     double rymin;
     double rymax;
     double cxmin;
     double cxmax;
     double cymin;
     double cymax;
     
     public ExampleGenerator(int size, double rxmin, double rxmax,
             double rymin, double rymax, double cxmin, double cxmax,
             double cymin, double cymax) {
         this.size = size;
         this.rxmin = rxmin;
         this.rxmax = rxmax;
         this.rymin = rymin;
         this.rymax = rymax;
         this.cxmin = cxmin;
         this.cxmax = cxmax;
         this.cymin = cymin;
         this.cymax = cymax;
         
     }
     
     public void generate(){
         Random randomGenerator = new Random();
         double[] xrectangle = new double[this.size/2];
         double[] yrectangle = new double[this.size/2];
         double[] xcircle = new double[this.size/2];
         double[] ycircle = new double[this.size/2];
         // Generate points for rectangle
         int i = 0;
         double x;
         double y;
         double check;
         while (i < this.size/2){
             while(true){
                x = randomGenerator.nextDouble() % this.rxmax + 1;
                y = randomGenerator.nextDouble() % this.rymax + 1;
                check = Math.pow(x - 15, 2) + Math.pow(y - 6, 2);
                 if (check != 9.0) {
                     xrectangle[i] = x;
                     yrectangle[i] = y;
                     break;
                 }
             }
             i++;
         }
         
         // Generate points for rectangle
         i = 0;
         while (i < this.size/2) {
             while(true){
                x = randomGenerator.nextDouble() % (this.cxmax - this.cxmin + 1)
                         + this.cxmin;
                y = randomGenerator.nextDouble() % (this.cymax - this.cymin + 1)
                         + this.cymin;
                 check = Math.pow(x - 15, 2) + Math.pow(y - 6, 2);
                 if (check <= 9.0) {
                     xcircle[i] = x;
                     ycircle[i] = y;
                     break;
                 }
             }
             i++;
         }
         System.out.println("Rectangle");
         for(int k = 0; k < this.size/2; k++){
             System.out.println("x = "+xrectangle[k]+", y = "+yrectangle[k]);
         }
         System.out.println("Circle");
         for(int k = 0; k < this.size/2; k++){
             System.out.println("x = "+xcircle[k]+", y = "+ycircle[k]);
         }
     }
 
 }
