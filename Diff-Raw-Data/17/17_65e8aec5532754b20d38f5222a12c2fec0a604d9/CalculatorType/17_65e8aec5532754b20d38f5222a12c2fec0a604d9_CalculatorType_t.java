 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dip.lab2.student.solution1;
 
 /**
  *
  * @author Michael
  */
 public interface CalculatorType {
     static final double GOOD_RATE = 0.20;
     static final double FAIR_RATE = 0.15;
     static final double POOR_RATE = 0.10;
 
    public abstract double getTip();
     
    enum ServiceQuality {
        GOOD,
        FAIR,
        POOR        
     }
     
    public abstract ServiceQuality getServiceQuaity();

     
 }
