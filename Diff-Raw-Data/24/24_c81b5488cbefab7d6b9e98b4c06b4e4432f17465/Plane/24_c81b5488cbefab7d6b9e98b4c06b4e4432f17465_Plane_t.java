 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package planegame;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 
 /**
  * The Plane class represents planes on the playing field.
  * @author Vlad Zotov
  */
 public class Plane extends FlyingObject{
     
     /** Reference to player owner of the plane */
     private Player m_player;                  
     /** Reference to the Room in which plane is located */
     private Room m_room;
     /** Identifier of plane */
     private int m_id;
     private int m_length = Physics.PLANE_WIDTH;
     private int m_height = Physics.PLANE_HEIGT;
     /** Radius of the circle around plane*/
     private int m_r;
     /** Acceleration vector */
     protected Vector m_acceleration;
     /** Draft force vector */
     protected Vector m_draft;
     /** Uplifting force vector */
     //protected Vector m_uplifting_force;
     /** Gravity vector */
     protected Vector m_gravity;
     /** Plane wright */
     protected int m_weight;
     /** Flag that determine alive plane or not  */
     private boolean m_is_alive;
     
     public Player getPlayer(){
         return m_player;
     }
     
     /**
      * Create new instance of Plane.
      * @param x X coordinate of plane
      * @param y Y coordinate of Plane
      * @param v Motion Vector of plane
      * @param p Player owner of plane
      * @param room Room in which plane is located
      */
     public Plane(int x, int y, Vector v, Player p, Room room){
         super(x, y, v);               
         m_player = p;
         m_room = room;
         m_id = m_player.getID();
         m_r = (int)Math.sqrt(m_length*m_length/4 + m_height*m_height/4);
         m_acceleration = new Vector(0, 0);
         m_draft = new Vector(0, 0);
 //        m_uplifting_force = new Vector(0, 0);
         m_gravity = new Vector(0, -Physics.GRAVITY);
         m_weight = 1;
         m_is_alive = true;
     }
     
     public int getX(){
         return m_coordinates.getX();
     }
     
     public int getY(){
         return Physics.MAX_Y - m_coordinates.getY();
     }
     
     public Point getCoordinates(){
         return m_coordinates;
     }
     
     public boolean IsAlive(){
         return m_is_alive;
     }
     
     @Override
     public Vector getDirectionVector(){
         return new Vector(m_velocity.X(), -m_velocity.Y());
     }
     
     //@Override
     public boolean equals(Object obj){
         if(obj instanceof Plane){
             Plane p = (Plane)obj;        
             if(p.getX() == m_coordinates.getX() && p.getY() == m_coordinates.getY() && p.getDirectionVector().X() == this.getDirectionVector().X() 
                     && p.getDirectionVector().Y() == this.getDirectionVector().Y() && p.m_draft.X() == this.m_draft.X()
                     && p.m_draft.Y() == this.m_draft.Y())
                 return true;
         }
         else
             return false;
         return false;
     }
 
     public int hashCode() {
         int hash = 7;
         hash = 83 * hash + this.m_coordinates.getX() + this.m_coordinates.getY() + (int)this.m_velocity.X()*hash + (int)this.m_velocity.Y()*hash;
         
         return hash;
     }
         
     /**
      * Computes new state of Plane accordingly player event
      * @param e player's event
      */
     @Override
     public void Compute(HashMap<Integer, Integer>  events){
         if(!m_is_alive){
            ResetPlane();
            m_is_alive = !m_is_alive;
         }
         int event = events.get(m_id);
                 
         switch(event){
             case 4:{
                 System.out.println("acceleration");
                 Acceleration();
                 break;
             }
             case 1:{
                 System.out.println("Clockwise");
                 MinClockwiseRotation();
                 break;
             }
             case 2:{
                 System.out.println("CounterClockwise");
                 MinCounterClockwiseRotation();
                 break;
             }
             case 3:{
                Vector bulletVelocity = m_velocity.getUnitVector();
                 bulletVelocity.setX(bulletVelocity.X()*Physics.MAX_SPEED);
                 bulletVelocity.setY(bulletVelocity.Y()*Physics.MAX_SPEED);
                 m_room.AddBullet(new Bullet(m_coordinates.getX(), m_coordinates.getY(), bulletVelocity, this));
             } 
             case 5:{
                 System.out.println("slowdown");
                 Slowdown();
                 break;
             }
             case 0:{
                 System.out.println("simple motion");
                 SimpleMotion();
                 break;
             }
         }
         System.out.println("Event = " + event + " x = " + this.getX() + " y = " + this.getY() + " DraftX = " + this.m_draft.X() + 
                 " DraftY = " +  this.m_draft.Y() + " AccX = " + this.m_acceleration.X() + " AccY = " + this.m_acceleration.Y() + 
                 " velocityX = " + this.m_velocity.X() + " velocityY + " + this.m_velocity.Y());
         if(Bump())
             m_is_alive = false;
     }
     
     public Room getRoom(){
         return m_room;
     }
     
     public void ExitRoom(){
         m_room = null;
         m_player.Disconnect();
     }
     
     private void ResetPlane(){
          m_coordinates = new Point(Physics.PLANE_WIDTH, Physics.PLANE_HEIGT);
          m_acceleration.setX(0);
          m_acceleration.setY(0);
          m_draft.setX(0);
          m_draft.setY(0);
 //       m_uplifting_force.setY(0);
          m_velocity.setX(1);
          m_velocity.setY(0);
     }
     
     private boolean IsIntercept(Interval i1, Interval i2){
         return true;
     }
     
     private List<Interval> GetSides(Vector v, Point x){
         Vector unit_v = m_velocity.getUnitVector();
         Vector perp_one = new Vector(unit_v.Y() , -unit_v.X()); 
         Point O1 = new Point(m_coordinates.getX() + (int)(unit_v.X()*0.5*Physics.PLANE_WIDTH), m_coordinates.getY() + (int)(0.5*Physics.PLANE_WIDTH*unit_v.Y()));
         Point C = new Point(O1.getX() + (int)(0.5*Physics.PLANE_HEIGT*perp_one.X()), O1.getY() + (int)(0.5*Physics.PLANE_HEIGT*perp_one.Y()));
         Point D = new Point(O1.getX() + (int)(-0.5*Physics.PLANE_HEIGT*perp_one.X()), O1.getY() + (int)(-0.5*Physics.PLANE_HEIGT*perp_one.Y()));
         unit_v.setX(-unit_v.X());
         unit_v.setY(-unit_v.Y());
         Point O2 = new Point(m_coordinates.getX() + (int)(-0.5*Physics.PLANE_WIDTH*unit_v.X()), m_coordinates.getY() + (int)(-0.5*Physics.PLANE_WIDTH*unit_v.Y()));
         Point B = new Point(O2.getX() + (int)(0.5*Physics.PLANE_HEIGT*perp_one.X()), O2.getY() + (int)(0.5*Physics.PLANE_HEIGT*perp_one.Y()));
         Point A = new Point(O1.getX() + (int)(-0.5*Physics.PLANE_HEIGT*perp_one.X()), O1.getY() + (int)(-0.5*Physics.PLANE_HEIGT*perp_one.Y()));
         List<Interval> sides = new ArrayList<>();
         sides.add(new Interval(A, B));
         sides.add(new Interval(B, C));
         sides.add(new Interval(C, D));
         sides.add(new Interval(D, A));
         return sides;
     }
     
     private boolean Bump(){
         for(Plane p : m_room.getPlanes()){
            if(Math.sqrt((this.getX() - p.getX())*(this.getX() - p.getX()) + (this.getY() - p.getY())*(this.getY() - p.getY())) <= m_r*2 ){
                 /*List<Interval> my_sides = GetSides(m_velocity, m_coordinates);
                 List<Interval> enemy_sides = GetSides(p.getDirectionVector(),p.getCoordinates());
                 for(Interval my : my_sides){
                     for(Interval en : enemy_sides){
                         boolean f = IsIntercept(my, en);
                         if(f)
                             return true;
                     }
 					
                 }*/
                return false;
             }
            return true;
         }
	for(Bullet b : m_room.getBullets()){
             if(Math.sqrt((this.getX() - b.getX())*(this.getX() - b.getX()) + (this.getY() - b.getY())*(this.getY() - b.getY())) <= m_r){
                 b.NewFrag(this);
                return false;
             }
	}
        return true;
     }
     
     public void NewFrag(Plane p){
         m_player.NewFrag(p);
     }
     
     private void MinClockwiseRotation(){
         //не работает
         m_draft.ClockwiseRotation(Physics.MIN_ROTATE_ANGLE);
         SimpleMotion();
     }
     
     private void MinCounterClockwiseRotation(){
         //не работает
         m_draft.CounterClockwiseRotation(Physics.MIN_ROTATE_ANGLE);
         SimpleMotion();
     }
     
     private void Acceleration(){    //simple acceleration. Change coordinates and increase speed
         //не работает
         if(m_draft.X() == 0 && m_draft.Y() == 0){
             m_draft.setX(0.5f);
             m_draft.setY(0);
         }
         else{
             m_draft.setX(m_draft.X() + m_draft.X()/Physics.ACCELERATION);
             m_draft.setY(m_draft.Y() + m_draft.Y()/Physics.ACCELERATION);
         }
         //m_velocity.setX(m_draft.X()+10);
         SimpleMotion();
     }
     
     private void Slowdown(){    //simple slowdown. Change coordinatees and decrease speed
         //не работает
         m_draft.setX(m_draft.X() - m_draft.X()/Physics.ACCELERATION);
         m_draft.setY(m_draft.Y() - m_draft.Y()/Physics.ACCELERATION);
         if(m_draft.X() < 0.3 && m_draft.Y() < 0.3){
             m_draft.setX(0);
             m_draft.setY(0);
         }
         //m_velocity.setX(m_draft.X()+3);
         SimpleMotion();
     }
     
     private void SimpleMotion(){    //uniform motion along X and accelereted motion along Y
         m_acceleration.setX((m_draft.X() /*+ m_uplifting_force.X()*/)/m_weight);
         m_acceleration.setY((m_draft.Y() /*+ m_uplifting_force.Y() */+ m_gravity.Y())/m_weight);
         if(m_acceleration.Length() > Physics.MAX_ACCELERATION){
             m_acceleration = m_acceleration.getUnitVector();
             m_acceleration.setX(m_acceleration.X()*Physics.MAX_ACCELERATION);
             m_acceleration.setY(m_acceleration.Y()*Physics.MAX_ACCELERATION);
         }
         
         m_velocity.setX(m_velocity.X() + m_acceleration.X());
         m_velocity.setY(m_velocity.Y() + m_acceleration.Y());
         if(m_velocity.Length() > Physics.MAX_SPEED){
             m_velocity = m_velocity.getUnitVector();
             m_velocity.setX(m_velocity.X()*Physics.MAX_SPEED);
             m_velocity.setY(m_velocity.Y()*Physics.MAX_SPEED);
         }
        
         m_coordinates.setX(m_coordinates.getX() + Math.round(m_velocity.X()));
         m_coordinates.setY(m_coordinates.getY() + Math.round(m_velocity.Y()));
         m_coordinates.setX((m_coordinates.getX() >= 0 )?(m_coordinates.getX()):(Physics.MAX_X + m_coordinates.getX()));
         m_coordinates.setX((m_coordinates.getX() < Physics.MAX_X)?(m_coordinates.getX()):(m_coordinates.getX() - Physics.MAX_X));
         m_coordinates.setY((m_coordinates.getY() >= 0 )?(m_coordinates.getY()):(Physics.MAX_Y + m_coordinates.getY()));
         m_coordinates.setY((m_coordinates.getY() < Physics.MAX_Y)?(m_coordinates.getY()):(m_coordinates.getY() - Physics.MAX_Y));
         //m_uplifting_force.setY(m_velocity.X()*m_velocity.X()/m_velocity.Length());
     }
     
 }
