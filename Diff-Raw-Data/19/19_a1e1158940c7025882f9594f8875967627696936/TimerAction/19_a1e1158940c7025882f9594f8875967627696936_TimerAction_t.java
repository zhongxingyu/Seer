 
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
 

   /*
    * setTimerAtr setter del slider y la label de la vista y lo asocia al listener
    *
    * @param JSlider le permite escuchar el nivel seleccionado en el Slider
    * @param JLabel etiqueta que marca el valor del slider
    */
   public void setTimerAtr (JSlider slider, JLabel label){
       this.slider = slider;
       this.label = label;
   }
 
         /*
       * stateChanged acción que realiza al escuchar, al cambiar el valor del Slider
       *
       * @params ChangeEvent recibe el evento
       */
   public void stateChanged(ChangeEvent ce){
   int value = slider.getValue();
   this.timer = value ;
   label.setText(Integer.toString(value));
   }
 
         /*
        * getTimer getter del tiempo deleccionado por el Slider
        *
        * @return int tiempo deleccionado por el Slider
        */
   public int getTimer(){
       return this.timer;
   }
 }
