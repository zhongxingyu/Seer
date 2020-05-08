 package edu.wpi.first.wpilibj.templates;
 
 public class Deadband{
     
     /*
     static int flag = 0;
     
     public static void Range (double targetValue, double bottomNumber, double value, double increase){
         while (targetValue > value && flag != 1){
             value += increase;
             if (targetValue <= value){
                 flag += 1;
             }
         }
     }*/
     
    public boolean joyStickDeadBand (double topNumber, double bottomNumber, double value){
         boolean a = false;
         if (bottomNumber > value && value > topNumber){
          a = true;
          return a;
         } else {
             return a;
         }
     }
 }
