 
 package controller;
 
 import javax.swing.JSlider;
 import javax.swing.event.*;
 import javax.swing.JLabel;
 import model.Metronomo;
 
 /**
  * TimerAction.java
  * Implementa el listener que seteará el tiempo del metrónomo en orden de décimas de segundo
  * mediante el valor pasado por el Slider
  *
  * @author Mª Dolores priego Porcuna y Daniel Caballero Bellot
  * https://github.com/lolapriego/puzzle
  * @version 27/04/2012
  */
 
 public class TimerAction implements ChangeListener {
   private int timer = 1;
   private JSlider slider;
   private JLabel label;
 
   public void setTimerAtr (JSlider slider, JLabel label){
       this.slider = slider;
       this.label = label;
   }
 
   public void stateChanged(ChangeEvent ce){
   int value = slider.getValue();
   this.timer = value ;
   label.setText(Integer.toString(value));
   }
 
   public int getTimer(){
       return this.timer;
   }
 }
