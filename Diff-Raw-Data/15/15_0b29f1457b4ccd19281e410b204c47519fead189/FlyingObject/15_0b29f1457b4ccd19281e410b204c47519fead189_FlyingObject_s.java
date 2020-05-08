 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package planegame;
 
 import java.util.HashMap;
 
 
 /**
  * The abstract class for flying objects in game.
  * @author Vlad
  */
 public abstract class FlyingObject {
     
     /** Coordinates of plane */ 
     protected Point m_coordinates;
     /** Velocity vector aka motion vector */
     protected Vector m_velocity;         
     
     /** 
      * 
      * @return X coordinate
      */
     public Point getCoordinates(){
         return m_coordinates;
     }
     
     /**
      * 
      * @return motion vector of FlyingObject
      */
     public Vector getDirectionVector(){
         return m_velocity;
     }
     
     public FlyingObject(int x, int y, Vector vec){
        m_coordinates = new Point(x, y);
         m_velocity = vec.Clone();
         
     }
     
     /**
      * Computes new state of Flying object accordingly player's event. 
      * @param e event posted by user
      */
     public abstract void Compute(HashMap<Integer, Integer> events);
     
 }
