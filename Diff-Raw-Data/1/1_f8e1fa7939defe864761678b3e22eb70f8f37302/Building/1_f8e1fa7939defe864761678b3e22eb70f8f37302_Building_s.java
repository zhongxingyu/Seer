 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package adastra.engine.planet;
 
import java.awt.Polygon;
 import javax.swing.JComponent;
 
 /**
  *
  * @author webpigeon
  */
 public abstract class Building {
     private String name;
     
     public Building(String name){
         this.name = name;
     } 
     
     public String getName(){
         return name;
     }
     
     public abstract void gameTick();
     public abstract JComponent getSettings();
     public abstract JComponent getIcon();
 
     @Override
     public String toString(){
         return name;
     }
 }
