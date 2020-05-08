 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dip.lab2.student.solution1;
 
 import java.text.NumberFormat;
 
 /**
  *
  * @author User
  */
 public class Runner {
     
     public static void main(String[] args) {
 
         TipCalculator tc1 = new BaggageServiceTipCalculator(BaggageServiceTipCalculator.ServiceQuality.GOOD, 5);
         TipCalculator tc2 = new BaggageServiceTipCalculator(BaggageServiceTipCalculator.ServiceQuality.POOR, 3);
         TipCalculator tc3 = new FoodServiceTipCalculator(FoodServiceTipCalculator.ServiceQuality.FAIR, 50);
         
         
         TipCalculator[] tipCalculator = {tc1, tc2, tc3};
 
         CalculatorService cs = new CalculatorService();
 
         NumberFormat nf = NumberFormat.getCurrencyInstance();

        for(int i=0; i < tipCalculator.length; i++) {
            System.out.println("Employee " + (i+1) + " compensation:");
            cs.getTipTotal(tipCalculator[i]);
        }
         
        System.out.println("Your total is $" +
             nf.format(cs.getTipTotal(tc1)));
        System.out.println("Your total is $" +
             nf.format(cs.getTipTotal(tc2)));
        System.out.println("Your total is $" +
             nf.format(cs.getTipTotal(tc3)));
     
     }
 }
