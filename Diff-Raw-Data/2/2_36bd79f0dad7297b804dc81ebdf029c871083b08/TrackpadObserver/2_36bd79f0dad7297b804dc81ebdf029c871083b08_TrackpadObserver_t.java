 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.epita.mti.plic.opensource.controlibserversample.observer;
 
 import com.epita.mti.plic.opensource.controlibutility.beans.CLVector;
 import com.epita.mti.plic.opensource.controlibutility.serialization.CLSerializable;
 import java.awt.AWTException;
 import java.awt.MouseInfo;
 import java.awt.Robot;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  *
  * @author Benoit "KIDdAe" Vasseur
  */
 public class TrackpadObserver implements Observer
 {
   private Robot robot;
   private int mouseX;
   private int mouseY;
 
   public TrackpadObserver() throws AWTException
   {
     this.robot = new Robot();
   }
 
   @Override
   public void update(Observable o, Object arg)
   {
    if (((CLSerializable) arg).getType().equals("vector"))
     {
       mouseX = MouseInfo.getPointerInfo().getLocation().x;
       mouseY = MouseInfo.getPointerInfo().getLocation().y;
 
       float x = ((CLVector) arg).getX();
       float y = ((CLVector) arg).getY();
       robot.mouseMove(Math.round(mouseX + x), Math.round(mouseY + y));
     }
   }
 }
