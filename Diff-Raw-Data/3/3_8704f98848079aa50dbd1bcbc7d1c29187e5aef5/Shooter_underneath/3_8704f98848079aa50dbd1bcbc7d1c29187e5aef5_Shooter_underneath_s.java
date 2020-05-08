 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.templates.Shooter;
 import edu.wpi.first.wpilibj.templates.Team3373;
 /**
  *
  * @author Philip2
  */
 public class Shooter_underneath extends Shooter {
     public void RPMTarget(double a){ //defines target based on input. Appeaers to be better than speed increase. can probbaly be used in place of a bunch of code.
         if (shootA){
             target = ((RPMModifier *ShooterSpeedScale) + currentRPMT2) * a;
            StageTwoTalon.set(target);
         } else if (shootB){
             target = (( -RPMModifier * ShooterSpeedScale) + currentRPMT2) * a;
            StageTwoTalon.set(target);
         }
         
     }
 }
