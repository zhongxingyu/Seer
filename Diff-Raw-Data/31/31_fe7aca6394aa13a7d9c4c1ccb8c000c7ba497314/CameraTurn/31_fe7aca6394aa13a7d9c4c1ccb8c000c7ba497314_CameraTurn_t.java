 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.aisd.martin.frc2013;
 
 import edu.wpi.first.wpilibj.Servo;
 
 /**
  *
  * @author Developer
  */
 public class CameraTurn {
     private Servo yAxis;
     private Servo xAxis;
     private double upAngle;
     private double leftAngle;
     
     public CameraTurn(int slot, int yAxis, int xAxis){
         this.yAxis = new Servo(slot, yAxis);
         this.xAxis = new Servo(slot, xAxis);
         upAngle = this.yAxis.getAngle();
         leftAngle = this.xAxis.getAngle();
     }
     
     public void think(double up, double left){
        if(up > .1  ||  up < -.1)
            yAxis.set((up / 2) + .5);
        else
            xAxis.set(.5);
        if(left < -.1 || left > .1)
            xAxis.set((left / 2) + .5);
        else{
            yAxis.set(.5);
        }
    } 
 }
