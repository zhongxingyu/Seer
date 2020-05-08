 package BillingServer;
 
 import java.io.Serializable;
import java.rmi.RemoteException;
 import java.util.ArrayList;
import java.util.Iterator;
 import java.util.List;
 
 /**
  *
  * @author Dave
  */
 public class PriceSteps implements Serializable {
 
     public List<PriceStep> priceSteps = null;
 
     public PriceSteps() {
         priceSteps = new ArrayList<PriceStep>();
         initDefaultPriceSteps();
     }
 
     public synchronized void createPriceStep(double min_value, double max_value, double fee_fixed, double fee_variable) {
         PriceStep ps = new PriceStep(min_value, max_value, fee_fixed, fee_variable);
         priceSteps.add(ps);
     }
 
    public synchronized boolean deletePriceStep(double min_value, double max_value){
        Iterator<PriceStep> it = priceSteps.iterator();
        while (it.hasNext()) {
            PriceStep ps = it.next();
             if (ps.getMin_value() == min_value && ps.getMax_value() == max_value) {
                it.remove();
                return true;
            } 
         }
        return false;
     }
 
     private void initDefaultPriceSteps() {
 
         PriceStep level_1 = new PriceStep(0, 100, 3, 0.07);
         PriceStep level_2 = new PriceStep(100, 200, 5, 0.065);
         PriceStep level_3 = new PriceStep(200, 500, 7, 0.06);
         PriceStep level_4 = new PriceStep(500, 1000, 10, 0.055);
 
         priceSteps.add(level_1);
         priceSteps.add(level_2);
         priceSteps.add(level_3);
         priceSteps.add(level_4);
     }
 
     /**
      * this methods checks if there is an overlap inside the price
      * configurations
      *
      * @param min_value
      * @param max_value
      * @return false if overlap or negative, else return true
      */
     public boolean check(double min_value, double max_value) {
         for (PriceStep ps : priceSteps) {
             if (min_value >= ps.getMin_value() && min_value < ps.getMax_value()) {
                 return false;
             }
             if (max_value != 0) {
                 if (min_value >= max_value) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     @Override
     public String toString() {
         String steps = "";
         for (PriceStep ps : priceSteps) {
             steps = steps + ps.toString() + "\n";
         }
         return steps;
     }
 }
