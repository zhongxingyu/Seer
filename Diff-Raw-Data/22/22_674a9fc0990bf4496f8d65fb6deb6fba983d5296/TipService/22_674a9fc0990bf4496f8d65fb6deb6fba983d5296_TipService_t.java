 package dip.lab2.student.solution1;
 
 /**
  *
  * @author Mark Van Weelden
  */
 public class TipService {
     
    public double getTipAmount(TipStrategy tip){
        
        double tipAmount = tip.getTip();
        
        return tipAmount;
    }
 
 }
