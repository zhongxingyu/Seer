 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package planegame;
 
 import java.util.HashMap;
 
 /**
  * The Bullet class represents bullets on the playing field.
  * @author Vlad Zotov
  * 
  */
 public class Bullet extends FlyingObject{
     
     /** Reference to plane owner */
     private Plane m_plane;
     
     /**
      * Create an instance of Bullet class 
      * @param x The X coordinate of bullet
      * @param y The Y coordinate of bullet
      * @param vector The motion vector of bullet
      * @param p Reference to the plane owner
      */
     public Bullet(int x, int y, Vector vector, Plane p){
         super(x, y, vector);
         m_plane = p;
     }
     
     /**
      * Computes new state of Bullet accordingly event
      * @param e The Event posted by user
      */
     @Override
     public void Compute(HashMap<Integer, Integer>  events){
         m_coordinates.setX(m_coordinates.getX() + (int)m_velocity.X());
         m_coordinates.setY(m_coordinates.getY() + (int)m_velocity.Y());
     }
    
     /**
      * 
      * @return The X coordinate of bullet.
      */
        public int getX(){
         return m_coordinates.getX();
     }
     
     /**
      * 
      * @return The Y coordinate of bullet.
      */
     
     public int getY(){
         return Physics.MAX_Y - m_coordinates.getY();
     }
     
     /**
      * 
      * @return The motion vector of Bullet.
      */
     @Override
     public Vector getDirectionVector(){
         return new Vector(m_velocity.X(), -m_velocity.Y());
     }
     
 }
