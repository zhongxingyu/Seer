 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package practica3;
 
 /**
  *
  * @author Jaume
  */
 public class Cavall extends Peça {
 
     public Cavall(int x, int y) {
         super(x, y);
     }
 
     @Override
     public boolean mataPeça(Peça peça) {
         return (this.getX() - 1 == peça.getX() && this.getY() - 2 == peça.getY()) ||
                (this.getX() - 1 == peça.getX() && this.getY() + 2 == peça.getY()) ||
                (this.getX() + 1 == peça.getX() && this.getY() - 2 == peça.getY()) ||
                (this.getX() + 1 == peça.getX() && this.getY() + 2 == peça.getY()) ||
                (this.getX() - 2 == peça.getX() && this.getY() - 1 == peça.getY()) ||
                (this.getX() - 2 == peça.getX() && this.getY() + 1 == peça.getY()) ||
                (this.getX() + 2 == peça.getX() && this.getY() - 1 == peça.getY()) ||
               (this.getX() + 2 == peça.getX() && this.getY() + 1 == peça.getY());
     }
     
 }
