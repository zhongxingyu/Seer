 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package test;
 
 /**
  *
  * @author Kira
  */
 public class Attributes {
     
     /**
      * @param knowledge                 Knowledge of the students - bound to student
      * @param knowledgeIncreasement     Increase of the knowledge attribute - bound to student
      * @param tiredness                 Shows how tired a student is - bound to student
      * @param motivation                Shows how motivated a student is - bound to student
      * @param airQuality                The quality of the air in the classroom - bound to room
      * @param noise                     Denotes how loud it is in the room - bound to room
      */
     
    private Student[] changeArray;
     
     public double changeTiredness(int i, double factor)
     {
        double tiredness = changeArray[i].getTiredness();
        tiredness = tiredness+1*factor;
        return tiredness;
     }
      
     public double changeMotivation(int i, double factor)
     {
        double motivation = changeArray[i].getMotivation();
        motivation = motivation+1*factor;
        return motivation;              
     }
     
     public double changeAirQuality(int i, double factor)
     {
        double airQuality = changeArray[i].getTiredness();
        airQuality = airQuality+1*factor;
        return airQuality;              
     }
 
     public double changeNoise(int i, double factor)
     {
        double noise = changeArray[i].getTiredness();
        noise = noise+1*factor;
        return noise;              
     }
         
     /* Added by Kira
      * Regularly called with testvalue:
      * double factor1 = 101-classroom.airQuality/200*(-3.3)
      * double factor2 = classroom.noise/200*3.3
      * airQuality changes regularly with -0.5
      * noise with 0.5 
      */   
 }
