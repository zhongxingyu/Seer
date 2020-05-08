 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package app.navigps.gui.borders;
 
 import java.awt.Component;
 import java.awt.Graphics;
 
 /**
  * Created on 2008-12-15, 23:17:16
  * @author vara
  */
 public class EmptyOvalBorder extends OvalBorder{
 
     /**
      *
      */
     public EmptyOvalBorder(){
     }
     /**
      *
      * @param recW
      * @param recH
      */
     public EmptyOvalBorder(double recW,double recH){
         super(recH, recH);
     }

    public EmptyOvalBorder(int top, int left, int bottom, int right){
        super(top, left, bottom, right);
    }

    public EmptyOvalBorder(int top, int left, int bottom, int right,double recW,double recH){
        super(top, left, bottom, right, recW, recH);
    }

     /**
      *
      * @param c
      * @param g
      * @param x
      * @param y
      * @param w
      * @param h
      */
     @Override
      public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
         
      }
 }
