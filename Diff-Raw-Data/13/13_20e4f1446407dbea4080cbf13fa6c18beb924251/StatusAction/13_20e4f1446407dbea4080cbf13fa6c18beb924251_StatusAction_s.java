 
 package controller;
 
 /**
  * StatusAction.java
  * Implementa el listener que reanudará o parará la partida en caso de llamarlo
  *
  * @author Mª Dolores priego Porcuna y Daniel Caballero Bellot
  * https://github.com/lolapriego/puzzle
  * @version 27/04/2012
  */
 
 import java.awt.event.*;
 
   public class StatusAction implements ActionListener {
       private int estado = 0;
       private int flag = 1;
         public void actionPerformed(ActionEvent e) {
             if (flag ==1){
             this.estado = 1;
             this.flag = 0;
             }
             else{
                 this.flag = 1;
                 this.estado = 0;
                 }
         }
         public int getEstado(){
             return this.estado;
         }
         }
