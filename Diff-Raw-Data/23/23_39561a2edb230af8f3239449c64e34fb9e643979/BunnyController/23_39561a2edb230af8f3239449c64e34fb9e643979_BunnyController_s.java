 package dab.bigBunny;
 
 import dab.engine.newsim.interfaces.FailableComponentView;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.Point2D;
 
 public class BunnyController {
     // number of degrees to rotate per step
     private final int ROTATION_AMOUNT = 4;
     
     // default ammount to accelerate
     private final static double DEF_ACCELERATION = 0.4;
     
     private final static double BRAKING_ACCELERATION = -1;
     
     // ammount we slow down when nothing is happenning (it's basically usual drag)
     private final static double NORMAL_STOPPING = -0.5;
     
     //if the bunny is sliding off the surface(not next to it, but in the direction of it),
     //its speed is decreased by this amount
     private final static double FRICTION_SLOWDOWN = 3;
     
     //the angle at which the bunny slids, not bounces
     private final static int SLIDE_ANGLE = 30;
     
     //the speed from which the bunny bouces of things
     private final static int BOUNCE_SPEED = 4;
     
     //the amount of bouncing 
     private final static double BOUNCE_AMOUNT = -0.7;
       
     //the amout to minimize the damage to the component
 
     private final double HIT_DAMAGE = 0.3;
     
     //max amount of damage that a bunny can do
     private final int MAX_DAMAGE = 5;
     
     private int orientation, tempOrientation, direction,perpendicularAngle;
     private double x, y, speed;
     private boolean movingForward, rotatingLeft, rotatingRight, braking;
     private boolean softwareFailure;
 
     private Environment environment;
     private HitBoundsController hitController;
     private int radius;
     private int health;
     private Rectangle bounds;   
      
 
     /* 
      * BunnyController: Environment, position, size
     */    
     public BunnyController(Environment e, HitBoundsController h, Point p) {
         this.environment = e;
         this.hitController = h;
         this.radius = 15;
         
         this.x = p.x;
         this.y = p.y;
         
         movingForward = false;
         braking = false;
         rotatingLeft = false;
         rotatingRight = false;
         
         orientation = 0;
         speed = 0;
         
         health = 100;           
     }
     
     BunnyController(Environment environment, HitBoundsController hitBoundsController, int radius) {
         this(environment, hitBoundsController, new Point(100, 100));
     }
 
     public boolean hasHeadache() {
         return environment.getHeadache();
     }
     
     public void step() {
         softwareFailure = environment.getSoftwareFailure();
         updateMovement();    
     }
     
     /*
      * calculate acceleration depending on the state
      * (running, braking, intersecting slime)
      */    
     private double computeAcceleration() {
         double acceleration;
        if (movingForward && !braking && speed >=0) {
             acceleration = DEF_ACCELERATION;
         } else if (braking) {
             acceleration = BRAKING_ACCELERATION * Math.signum(speed);   //math.signum - if moving backwards control
         } else if (movingForward && speed <0) {
             acceleration = -NORMAL_STOPPING + DEF_ACCELERATION;
         } else { 
             acceleration = NORMAL_STOPPING * Math.signum(speed);        
         } 
         
        //<<<<<ToDo: after this is fixed, add for the backwards speed control>>>>>
         Slime intersected = environment.intersectWithSlime(new Point(getX(), getY()), radius);
         if (intersected != null) {
            // System.out.println("Intersected with " + intersected.toString());
             // BAD CODE!!!
             if (speed > 2)
                 speed = speed * (1 - intersected.getFreshness() * 0.2);
                 /*{
                 acceleration = -1 * (intersected.getFreshness() + 0.5);
             } else {
                 acceleration /= 2;
             }*/
         }
         
         return acceleration;
     }
     
     protected void updateMovement() {
         Point2D.Double newLocation;
         double acceleration = computeAcceleration();       
                        
         speed += acceleration;
         if((speed>-1)&& speed<0){speed =0;}        //just a cheat for stabilising the speed if it's close to 0
         
         if(speed>=0) {
             tempOrientation = orientation;          
             newLocation = updateXY(orientation);   
         } else {                    
             newLocation = updateXY(direction);      //if speed is <0, that means the bunny is spinning and its direction is different from orientation       
             spin(); 
         }
            
         //to check if intersectig with something - be that bounds or components       
         newLocation = checkIntersects(newLocation);
         
         //let the bunny rotate if it was trying to (i.e. if arrows were pressed)
         rotate();
         
         // give the new newLocation to the bunny
         x = newLocation.getX();
         y = newLocation.getY();
     }
 
     public void startForward()     { movingForward = true;  }
     public void stopForward()      { movingForward = false; }
     
     public void startBrake()       { braking       = true;  }
     public void stopBrake()        { braking       = false; }
 
     public void startRotateLeft()  { rotatingLeft  = true;  }
     public void stopRotateLeft()   { rotatingLeft  = false; }
 
     public void startRotateRight() { rotatingRight = true;  }
     public void stopRotateRight()  { rotatingRight = false; }
     
     public void rotate(){
         if (rotatingLeft) { 
             if(!softwareFailure){ rotateLeft(); }
             else { rotateRight(); }
         }
         if (rotatingRight) {
             if(!softwareFailure){ rotateRight(); }
             else { rotateLeft(); }
         } 
     }
     
     private void rotateRight(){
         orientation = fixOrientation((int)(orientation + ROTATION_AMOUNT +speed*0.2));
     }
     
     private void rotateLeft(){
         orientation = fixOrientation((int)(orientation - ROTATION_AMOUNT -speed*0.2));            
     }
 
     private Point2D.Double updateXY(int thisDirection){
         double newX,newY;
         newX = x + speed * Math.cos(Math.toRadians(thisDirection));
         newY = y + speed * Math.sin(Math.toRadians(thisDirection));
         return (new Point2D.Double(newX, newY) );
     }
      
     private Point2D.Double checkIntersects(Point2D.Double point){
        Point2D.Double newLocation = point;  
        for (HittableComponent h : hitController.getHittableComponents()){
             if(h instanceof Circle){
                 newLocation = checkIntersectsCircle(newLocation, h);
             } else if (h instanceof RecCircle){     
                 newLocation = checkIntersectsSquare(newLocation, (RecCircle)h);     
             }
        }
           
        newLocation = checkInBounds(newLocation);  //check if doesn't hit the walls
     return newLocation;
     }
     
     private Point2D.Double checkInBounds(double newX, double newY ){
             
         if(!bounds.contains(newX, newY)){    
             if(bounds.getMinX() > newX){
                 newX = bounds.getMinX();
                 newY = adjustBunnyWhenHit(180, newX, newY).getY();
             } else if (bounds.getMaxX() < newX){
                 newX = bounds.getMaxX();
                 newY = adjustBunnyWhenHit(0,newX, newY).getY();
             }
             
             if(bounds.getMinY() > newY){
                 newY = bounds.getMinY();
                 newX = adjustBunnyWhenHit(270, newX, newY).getX();
             } else if (bounds.getMaxY() < newY){
                 newY = bounds.getMaxY();
                 newX = adjustBunnyWhenHit(90, newX, newY).getX();
             }        
         orientation=fixOrientation(tempOrientation);
         }  
        
         return (new Point2D.Double(newX, newY));       
     }
     
     private Point2D.Double checkInBounds(Point2D.Double point){
         return (checkInBounds(point.getX(), point.getY()));
     }
        
     public Point2D.Double checkIntersectsCircle(Point2D.Double newLocation, HittableComponent h){
       double dx,dy,dr,D, r, x1, x2, y1, y2, xa,  xb, ya, yb, discriminant,centreX, centreY;
       double newX, newY;
       newX = newLocation.getX();
       newY = newLocation.getY();
            
       Rectangle hitableCircle = h.getHittableBounds(radius);
       
       centreX= hitableCircle.getCenterX();
       centreY = hitableCircle.getCenterY();
       
       //change newLocation so that the centre of the circle (centreX, centreY) would be at (0;0)
       x1 = x - centreX;                           // (x1;y1) is the point from where the bunny starts moving
       y1 = y - centreY;              
       x2 = newX - centreX;                        // (x2;y2) is the predicted newLocation of the bunny     
       y2 = newY - centreY;
       r = hitableCircle.getHeight()/2;
       
       dx = x2 - x1;
       dy = y2 - y1;
       dr = Math.sqrt(dx*dx + dy*dy);
       D = (x1*y2) - (x2*y1);
       discriminant = r*r*dr*dr - D*D;
       
       //distance between the new newLocation and the centre of the circle smaller than radius
       if(Point.distance(x2, y2, 0, 0)<r){
          
           //break the component         
           breakComponent(h.getComponent());
                    
          if(discriminant > 0) {
             int thisDirection;
             thisDirection = adjustThisDirection(); 
   
             xa = (D * dy + sgn(dy) * dx * Math.sqrt(discriminant))/(dr*dr);
             xb = (D * dy - sgn(dy) * dx * Math.sqrt(discriminant))/(dr*dr);
             ya = (-D * dx + modulus(dy) * Math.sqrt(discriminant))/(dr*dr);
             yb = (-D * dx - modulus(dy) * Math.sqrt(discriminant))/(dr*dr);
           
             if((thisDirection> 90)&& (thisDirection <270)){
                 x2 = Math.max(xa, xb);
             } else {
                 x2 = Math.min(xa, xb);
             }
             
             if (thisDirection >180) {
                 y2 = Math.max(ya,yb);
             } else {
                 y2 = Math.min(ya,yb);
             }
             
             //switch back to the proper coordinate system
             newX = x2 + centreX; 
             newY = y2 + centreY;              
             
             //get the perpendicular angle to the hitpoint
             perpendicularAngle = calculateAngle(newX, newY,centreX, centreY, centreX+1,centreY);                   
             newLocation = adjustBunnyWhenHit(perpendicularAngle, newX, newY);
         }          
          orientation = fixOrientation(tempOrientation);         
       }  
       return newLocation;    
     }  
     
     private int calculateAngle(double x0,  double y0, double x1, double y1, double x2, double y2){
         int angle;
         //adust coordintes so that the point at (x1,y1) would be shifted to (0,0) and everything else shifted too.
         x0 -= x1;       
         y0 -=y1;        
         x2 -= x1;        
         y2 -= y1;       
 
         angle = (int) Math.toDegrees(Math.atan2(y0, x0) - Math.atan2(y2, x2));
         angle = fixOrientation(angle + 180);
         return angle;       
     }
     
     public Point2D.Double checkIntersectsSquare (Point2D.Double newLocation, RecCircle h){
         double centreX, centreY, halfHeight, halfWidth, newX, newY;
         int thisDirection;
         Rectangle hitBounds  = h.getRectangle().getHittableBounds(radius);
         Circle circle1 = h.getCircle1();
         Circle circle2 = h.getCircle2();
         
         newX = newLocation.getX();
         newY = newLocation.getY();
         centreX = hitBounds.getCenterX();                    
         centreY = hitBounds.getCenterY();
         halfHeight = hitBounds.getHeight() / 2;
         halfWidth = hitBounds.getWidth() / 2;
         
         if (hit(newX, newY, centreY, centreX, halfHeight, halfWidth)) {              
             
             thisDirection = adjustThisDirection();
 
             if (hitFromBelowOrAbove(x, centreX, y, centreY, halfHeight, halfWidth, thisDirection)) {      
                           
                 if (thisDirection > 180) {
                     if(circle2 !=null) {
                         newLocation = checkIntersectsCircle(newLocation, circle2);
                         newX = newLocation.getX();
                         newY = newLocation.getY();
                         
                     } else {
                         breakComponent(h.getComponent());
                         perpendicularAngle = 270;
                         newY = handleHitWall(centreY, halfHeight, perpendicularAngle);              
                         newX = adjustX(newY, thisDirection);
                         newX = adjustBunnyWhenHit(perpendicularAngle, newX, newY).getX();
                     }
                         
                 } else {
                     if(circle1 != null){
                         newLocation = checkIntersectsCircle(newLocation, circle1);
                         newX = newLocation.getX();
                         newY = newLocation.getY();
                         
                     }else {
                         breakComponent(h.getComponent());
                         perpendicularAngle = 90;
                         newY = handleHitWall(centreY, halfHeight, perpendicularAngle);              
                         newX = adjustX(newY, thisDirection);
                         newX = adjustBunnyWhenHit(perpendicularAngle, newX, newY).getX();
                                             }
                 }
                
                
              
             } else {
                 if ((thisDirection < 270) && (thisDirection > 90)) {
                     perpendicularAngle = 180;
                 } else {
                     perpendicularAngle = 0;
                 }
                 breakComponent(h.getComponent());
                 newX = handleHitWall(centreX, halfWidth, perpendicularAngle);               
                 newY = adjustY(newX, sgn(179 - perpendicularAngle), thisDirection);
                 newY = adjustBunnyWhenHit(perpendicularAngle, newX, newY).getY();
             }
 
             orientation = fixOrientation(tempOrientation);
         } else {
             if(circle1 != null){
                 newLocation = checkIntersectsCircle(newLocation, circle1);
                 newX = newLocation.getX();
                 newY = newLocation.getY();
             } 
             if (circle2 != null){
                 newLocation = checkIntersectsCircle(newLocation, circle2);
                 newX = newLocation.getX();
                 newY = newLocation.getY();
             }       
         }
   
         newLocation.setLocation(newX, newY);
         return newLocation;    
     }
        
     private double handleHitWall(double z, double distance, int thisDirection){
         double newZ = z + distance * sgn(thisDirection - 179);
         return newZ;
     }
   
     /*a check that the circle(bunny) has hit the the hittable object */
     private boolean hit(double newX, double newY,double y2, double x2, double halfHeight, double halfWidth){
         return((hitOnHeight(newY, y2,halfHeight))&&hitOnWidth(newX, x2, halfWidth));
     }
     
     /*a check if the circle is between the height newLocation of the hittable object */
     private boolean hitOnHeight(double y1, double y2, double halfHeight){
         return ((y1 > y2 - halfHeight)&&(y1 < y2 + halfHeight));
     }
     
     /*a check if the circle is between the width newLocation of the hittable object */
     private boolean hitOnWidth(double x1,double x2, double halfWidth){
         return((x1 > x2 - halfWidth)&&(x1 < x2 + halfWidth));
     }
         
     /* 
      * A check if the bunny hits the object from above or below. It gets the newLocation of 
      * y at which the circle would have hit the object. Then it gets the newLocation of x
      * at that point, those depend on orientation of the bunny and current position. then it checks 
      * if the x is in the bounds of the width of object. If it is not - the object is still not hit
      * and would get hit from the side.
      */
     private boolean hitFromBelowOrAbove(double x0, double x2,double y0, double y2, double halfHeight, double halfWidth, int thisDirection){
        
         if(thisDirection >= 180){
             double deltaY = y0 - (y2 + halfHeight);
             double newX;
             newX = x0 + (deltaY * tangent(thisDirection-90));        
             return (hitOnWidth(newX, x2, halfWidth));
        } else {
             double deltaY = (y2 - halfHeight) - y0;
             double newX;
             newX = x0 + (deltaY * tangent(90-thisDirection));
             return (hitOnWidth(newX, x2, halfWidth));
        }
     }
     
     /* 
      * when object is hit from above or below, x for the place where it was hit is calculated. 
      * it is calculated using dX = dY * tan(alfa), where dX and dY is the difference between bunny starting point 
      * and hit-point (i.e. the edges of triangle), and alfa is the angle of the corner oposite to x.  
      */
     private double adjustX(double newY, int thisDirection){
         return (x + ((newY-y)*tangent(90-thisDirection)));
     }
     
     /* 
      * when object is hit from left or right, y for the place where it was hit is calculated. 
      * it is calculated using dY = dX * tan(alfa), where dX and dY is the difference between bunny starting point 
      * and hit-point (i.e. the edges of triangle), and alfa is the angle of the corner oposite to y. i is either 1
      * or -1 used only for angle direction adjustment (hit from left / right). 
      */
     private double adjustY(double newX, int i, int thisDirection){
         return (y + ((newX-x)* tangent(thisDirection*i)));    
     }
     
     private Point2D.Double adjustBunnyWhenHit(int thisAngle, double newX, double newY){
         return (adjustBunnyWhenHit(thisAngle, new Point2D.Double(newX,newY)));
     }
     
     private Point2D.Double adjustBunnyWhenHit(int thisAngle, Point2D.Double newCoordinates){
         int thisDirection, spinDirection;
         perpendicularAngle = thisAngle;
         boolean wouldBounce = (speed > BOUNCE_SPEED || speed< -BOUNCE_SPEED);
                
         thisDirection = adjustThisDirection();       
         ajustOrientation(perpendicularAngle, thisDirection);
         spinDirection = calculateSpinDirection(perpendicularAngle, thisDirection);     //either 1 or -1 depending on wheather rotates left (1) or right (-1)
 
         //System.out.println("this direction " + thisDirection + " perpAngle " + perpendicularAngle);
         if (modulus(90 - modulus(thisDirection - perpendicularAngle)) < SLIDE_ANGLE) { 
             tempOrientation = perpendicularAngle + 90 * spinDirection ;
             tempOrientation = fixOrientation(tempOrientation);
         } else if (wouldBounce) {
             bounce();       
         } else {  
             newCoordinates = slide(spinDirection, newCoordinates, perpendicularAngle);
         }
         
         if (speed > FRICTION_SLOWDOWN) {
             speed -= FRICTION_SLOWDOWN ;
         }
         
         return newCoordinates;
     }
  
     private void ajustOrientation(int perpendicularDirection, int thisDirection){
        int newDirection = (thisDirection - (thisDirection - perpendicularDirection)*2);   //current direction shifted to the opposite side of the perpendicular angle by the angle between them
        direction = fixOrientation(newDirection);              
     }
     
     private int calculateSpinDirection (int perpendicularDirection, int thisDirection){ 
         int diff = (thisDirection - perpendicularDirection);
         if (modulus(diff) >90) {       //this handles if one of the directions is in one side of 0/360, and another - on the other
             diff = -1*diff;
         }
         return sgn(diff);
     }
     
     private void bounce(){     
          speed = BOUNCE_AMOUNT * speed * sgn(speed);      
     }
     
     //<<<<Play around with numbers to make for the most fun bouncing.>>>>>   
     private void spin(){
         double spinCoef;
         int angle, spinDirection;
         double a,b,c,d;
         a=0.0001;        
         b=1;
         c= 3;
         d = 5;
         
         double speedCoef = b * speed * speed + c * speed + d;
        
         spinDirection = calculateSpinDirection(perpendicularAngle, tempOrientation);     //1 or -1 depending on spinning left or right
         angle = (360 + (perpendicularAngle - direction) * spinDirection) % 360;
         if (angle > 180) {
             angle = 360 - angle;
         }
         spinCoef = a * angle * angle;
         orientation = orientation - (int) (speedCoef * spinCoef) * spinDirection;
         orientation = fixOrientation(orientation);
     }
          
     private Point2D.Double slide(int i, Point2D.Double newCoordinates, int theAngle){       
         newCoordinates = updateXY(fixOrientation(theAngle + 90 * i));  
         return newCoordinates;                                                     
     }
     
     private int adjustThisDirection(){
         if(speed <0) {
                return (180 + direction)%360;
            } else {
                return orientation;
            }
     }
     
     private int fixOrientation (int thisOrientation) {
         if (thisOrientation < 0) {
             thisOrientation += 360;
             fixOrientation(thisOrientation);
         } else if(thisOrientation >= 360){
             thisOrientation = thisOrientation%360;
         }
         return thisOrientation;
     }
     
     private void breakComponent(FailableComponentView c){
         if(!environment.getHeadache()){       
             int damage = (int)(speed * HIT_DAMAGE);
             if (damage>MAX_DAMAGE) {damage = MAX_DAMAGE;}         
             c.fail(damage);
             environment.startHeadache();
         }
     }
     
     
            
     public void hasBeenShot(){
         health --;
     }
     
  
     
     public int getX() { return (int) x; }
 
     public int getY() { return (int) y; }
     
     public Point getCoordinates() { return (new Point(getX(), getY())); }
     
     public int getOrientation()   { return orientation; }
     
     public int getHealth()        { return health; }
     
     /*
      * why would we need to get back the radius, it doesn't change does it?
      * <<<<it is needed in the bunnyInterface. for now. Before the proper setting of bunnys' radius is done>>>> 
      */
     @Deprecated
     public int getRadius()        { return radius; }
 
     public void setBounds(Rectangle rectangle) {
         bounds = rectangle;
     }    
 
     private double tangent(int alfa){
         return Math.tan(Math.toRadians(alfa));
     }
     
     private double modulus (double i){
         if (i < 0) {return -i;}
         else { return i;}
     }
     
     private int modulus (int i){
         if (i < 0) {return -i;}
         else { return i;}
     }
     
      private int sgn(double i){
         if(i>0) { return 1; }
         else { return -1; }
     }
    
 }
