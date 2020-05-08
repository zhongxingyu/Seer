 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package planegame;
 
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
     private int r;
     /** Acceleration vector */
     protected Vector m_acceleration;
     /** Draft force vector */
     protected Vector m_draft;
     /** Uplifting force vector */
     protected Vector m_uplifting_force;
     /** Gravity vector */
     protected Vector m_gravity;
     /** Plane wright */
     protected int m_weight;
     
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
         r = (int)Math.sqrt(m_length*m_length/4 + m_height*m_height/4);
         m_acceleration = new Vector(0, 0);
         m_draft = new Vector(0, 0);
         m_uplifting_force = new Vector(0, 0);
         m_gravity = new Vector(0, Physics.GRAVITY);
        m_weight = 1;
     }
     
     @Override
     public int getX(){
         return m_x;
     }
     
     @Override
     public int getY(){
         return Physics.MAX_Y - m_y;
     }
     
     @Override
     public Vector getDirectionVector(){
         return new Vector(m_velocity.X(), -m_velocity.Y());
     }
     
     /**
      * Computes new state of Plane accordingly player event
      * @param e player's event
      */
     @Override
     public void Compute(Event e){
         Event.PlaneEvents event = e.getEvent();
         switch(event){
             case accelerate:{
                 Acceleration();
                 break;
             }
             case clockwiseRotate:{
                 MinClockwiseRotation();
                 break;
             }
             case counterClockwiseRotate:{
                 MinCounterClockwiseRotation();
                 break;
             }
             case fire:{
                 m_room.AddBullet(new Bullet(m_x, m_y, m_velocity, this));
             } 
             case slowdown:{
                 Slowdown();
                 break;
             }
             case none:{
                 SimpleMotion();
                 break;
             }
         }
     }
     
     public Room getRoom(){
         return m_room;
     }
     
     public void ExitRoom(){
         m_room = null;
         m_player.Disconnect();
     }
     
     private void Bump(){
         
     }    
     
     public void NewFrag(Plane p){
         m_player.NewFrag(p);
     }
     
     private void MinClockwiseRotation(){
         m_draft.ClockwiseRotation(Physics.MIN_ROTATE_ANGLE);
         SimpleMotion();
     }
     
     private void MinCounterClockwiseRotation(){
         m_draft.CounterClockwiseRotation(Physics.MIN_ROTATE_ANGLE);
         SimpleMotion();
     }
     
     private void Acceleration(){    //simple acceleration. Change coordinates and increase speed
        m_draft.setX(m_draft.X()*(1 + Physics.ACCELERATION/10));
        m_draft.setY(m_draft.Y()*(1 + Physics.ACCELERATION/10));
         SimpleMotion();
     }
     
     private void Slowdown(){    //simple slowdown. Change coordinatees and decrease speed
        m_draft.setX(m_draft.X()*(1 - Physics.ACCELERATION/10));
        m_draft.setY(m_draft.Y()*(1 - Physics.ACCELERATION/10));
         SimpleMotion();
     }
     
     private void SimpleMotion(){    //uniform motion along X and accelereted motion along Y
         m_acceleration.setX((m_draft.X() + m_uplifting_force.X())/m_weight);
         m_acceleration.setY((m_draft.Y() + m_uplifting_force.Y() + m_gravity.Y())/m_weight);
         m_velocity.setX(m_velocity.X() + m_acceleration.X());
         m_velocity.setY(m_velocity.Y() + m_acceleration.Y());
         m_x += m_velocity.X();
         m_y += m_velocity.Y();
         m_uplifting_force.setX(m_velocity.SLength()*m_draft.X()/m_draft.SLength());
     }
     
 }
