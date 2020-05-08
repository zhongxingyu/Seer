 package entity;
 
 import camera.Camera;
 import powerup.Orbital;
 import shape.Polygon2D;
 
 import java.awt.*;
 
 public class Bullet extends Entity {
     private Color color;
     public float radius;    //radius of bullet
 
     public double speed;    //Constant speed.  No FRICTION for bullets.
     Player parent;    //Sets parent player.  Will not interact with parent.
     boolean alive = true;
     int explosionTimer = 200; //explosion will show for explosionTimer milliseconds
 
     static final float HOMING_FORCE = .005f;
 
     public int bounces = 2;    //How many times it will bounce before going away
     public float size;
 
     public Bullet(Player p, float si, float sp) {
         parent = p;
         radius = si;
         speed = sp;
 
         color = parent.color;
 
         xPos = (float) (parent.xPos + parent.radius * Math.cos(parent.direction));    //sets position to just in front of parent player
         yPos = (float) (parent.yPos + parent.radius * Math.sin(parent.direction));
 
         xVel = (float) (speed * Math.cos(parent.direction) + parent.xVel);    //sets velocities relative to player
         yVel = (float) (speed * Math.sin(parent.direction) + parent.yVel);
 
         speed = Math.sqrt(Math.pow(xVel, 2) + Math.pow(yVel, 2));    //changes speed variable for use in later calculations
     }
 
     long lastDownscale = System.currentTimeMillis();
     int downscaleDelay = 15;
     long nextDownscale = System.currentTimeMillis() + downscaleDelay;
     boolean initialGrowth = false;
 
     public void draw(Graphics2D g) {
         if (alive) {
             //draw bullet if alive
             g.setColor(color);
             Camera.fillCenteredOval(xPos, yPos, radius * 2, radius * 2, g);
             g.setColor(Color.black);
             Camera.drawCenteredOval(xPos, yPos, radius * 2, radius * 2, g);
         } else {
             if (!initialGrowth) {
                 radius *= 2.7;
                 initialGrowth = true;
             }
 
             boolean downscale = false;
             if (System.currentTimeMillis() >= nextDownscale) {
                 downscale = true;
                 lastDownscale = System.currentTimeMillis();
                 nextDownscale = lastDownscale + downscaleDelay;
             }
 
             //draw explosion if dead
             if (downscale)
                 radius *= .9;    //same as player explosion
             g.setColor(Color.red);
             Camera.fillCenteredOval(xPos, yPos, radius * 2, radius * 2, g);
             float radius2 = 2 / 3f * radius;    //scales next part of explosion down a bit
             g.setColor(Color.orange);
             Camera.fillCenteredOval(xPos, yPos, radius2 * 2, radius2 * 2, g);
             float radius3 = 1 / 2f * radius2;    //scales next part of explosion down a bit
             g.setColor(Color.yellow);
             Camera.fillCenteredOval(xPos, yPos, radius3 * 2, radius3 * 2, g);
         }
     }
 
     public void tick(int delta) {
         if (!alive)    //handles explosion
         {
             if (explosionTimer < 0)
                 removeFromList();    //remove if done exploding
             else
                 explosionTimer -= delta;    //count down to removal
             return;
         }
 
         gravitate(delta);
 
         xPos += xVel * delta;    //apply movement
         yPos += yVel * delta;
 
         updateBoundingBox();
     }
 
     public void updateBoundingBox() {
         boundingBox = new Polygon2D();
         int numPoints = 12;
         for (int i = 0; i < numPoints; i++) {
             double angle = i * Math.PI * 2 / numPoints;
             float pointX = (float) (radius * Math.cos(angle) + xPos);
             float pointY = (float) (radius * Math.sin(angle) + yPos);
             boundingBox.addPoint(pointX, pointY);
         }
     }
 
     public void onCollide(Entity other) {
         if (alive)
             if (other instanceof Bullet)
                 onCollide((Bullet) other);
             else if (other instanceof Orbital)
                 onCollide((Orbital) other);
             else if (other instanceof Player)
                 onCollide((Player) other);
             else if (other instanceof Wall)
                 onCollide((Wall) other);
     }
 
     public void onCollide(Bullet other) {
        alive = false;
     }
 
     public void onCollide(Orbital other) {
         if (other.parent != this.parent) {    //make sure to not hit its compadre
             other.health--;
             alive = false;
         }
     }
 
     public void onCollide(Player other) {
         if (other.alive && other != parent) {
             hit(other);
             alive = false;
         }
     }
 
     public void onCollide(Wall other) {
         double collideAngle = Math.atan2(yPos - other.yPos, xPos - other.xPos);
 
         if (collideAngle >= other.ULangle) {
             xVel = -Math.abs(xVel);
             xPos = other.xMin - radius;
         } else if (collideAngle >= other.URangle) {
             yVel = Math.abs(yVel);
             yPos = other.yMax + radius;
         } else if (collideAngle >= other.LRangle) {
             xVel = Math.abs(xVel);
             xPos = other.xMax + radius;
         } else if (collideAngle >= other.LLangle) {
             yVel = -Math.abs(yVel);
             yPos = other.yMin - radius;
         } else {
             xVel = -Math.abs(xVel);
             xPos = other.xMin - radius;
         }
         bounces--;
         if (bounces < 0)
             alive = false;
     }
 
     public void gravitate(int delta) {
         for (Entity e : Entity.entities) {
             if (e instanceof Player)
                 if (e != parent) {
                     Player p = (Player) e;
                     double angle = Math.atan2(p.yPos - yPos, p.xPos - xPos);
                     double force = HOMING_FORCE * Math.pow(Math.PI, 2) * Math.pow(radius, 2) * Math.pow(p.radius, 2) / Math.pow(distance(xPos, yPos, p.xPos, p.yPos), 2);
                     double mass = Math.PI * Math.pow(radius, 2);
                     double pmass = Math.PI * Math.pow(p.radius, 2);
 
                     xVel += (force * Math.cos(angle) / mass) * delta;
                     yVel += (force * Math.sin(angle) / mass) * delta;
                     speed = Math.sqrt(Math.pow(xVel, 2) + Math.pow(yVel, 2));
 
                     p.xVel -= (force * Math.cos(angle) / pmass) * delta;
                     p.yVel -= (force * Math.sin(angle) / pmass) * delta;
                 }
             if (e instanceof Bullet)
                 if (e != this) {
                     Bullet b = (Bullet) e;
                     if (b.parent != this.parent) {
                         double angle = Math.atan2(b.yPos - yPos, b.xPos - xPos);
                         double force = HOMING_FORCE * Math.pow(Math.PI, 2) * Math.pow(radius, 2) * Math.pow(b.radius, 2) / Math.pow(distance(xPos, yPos, b.xPos, b.yPos), 2);
                         double mass = Math.PI * radius * radius;
 
                         xVel += (force * Math.cos(angle) / mass) * delta;
                         yVel += (force * Math.sin(angle) / mass) * delta;
                         speed = Math.sqrt(Math.pow(xVel, 2) + Math.pow(yVel, 2));
                     }
                 }
         }
     }
 
     public void hit(Player targ)    //bullet hits player
     {
         if (!targ.shielded) {
             parent.hits++;    //add a hit to its parent
             targ.health--;    //lower victim's health
         }
 
         double direction = Math.atan2(targ.yPos - yPos, targ.xPos - xPos);    //get collision angle
 
         targ.xVel += speed * Math.cos(direction) * (Math.pow(radius, 2) / Math.pow(targ.radius, 2));    //apply force to player for knockback
         targ.yVel += speed * Math.sin(direction) * (Math.pow(radius, 2) / Math.pow(targ.radius, 2));
     }
 
     //simple distance formula.  Shouldn't really be in here.
     double distance(double x1, double y1, double x2, double y2) {
         return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
     }
 }
