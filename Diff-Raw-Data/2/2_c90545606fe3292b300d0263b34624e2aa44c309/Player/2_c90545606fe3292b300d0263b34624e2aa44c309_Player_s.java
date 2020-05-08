 package circlesvssquares;
 
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.Fixture;
 import org.jbox2d.dynamics.FixtureDef;
 
 import pbox2d.PBox2D;
 
 
 class Player {
 
     // Magnitude of impulse to apply each frame in the x-direction to make the player move
     private static final float PLAYER_MOVEMENT_IMPULSE = 10;
     // Maximum x-velocity that the player can reach before we stop increasing the velocity
     // due to key presses.
     private static final float PLAYER_MAX_SPEED = 20;
     // Amount to decrease the player's x-velocity each frame if no movement buttons
     // are being pressed.
     private static final float PLAYER_NO_MOVEMENT_DAMPING = 1.3f;
     // Magnitude of impulse in the y-direction to apply to make the player "jump"
     private static final float PLAYER_JUMP_IMPULSE = 60;
 
     public enum MovementDirection {
         LEFT,
         RIGHT,
         NONE,
     }
 
     // We need to keep track of a Body and a width and height
     Body body;
     float r;
     Boolean canMove;
     PBox2D box2d;
 
     // Constructor
     Player(float x_, float y_, PBox2D box2d) {
         float x = x_;
         float y = y_;
         this.box2d = box2d;
     
         r = 12;
         canMove = false;
     
         // Add the box to the box2d world
         makeBody(x, y, r);
         body.setUserData(this);
     }
 
     // This function removes the particle from the box2d world
     public void killBody() {
         box2d.destroyBody(body);
     }
 
     public boolean contains(float x, float y) {
         Vec2 worldPoint = box2d.coordPixelsToWorld(x, y);
         Fixture f = body.getFixtureList();
         boolean inside = f.testPoint(worldPoint);
         return inside;
     }
 
     // Drawing the box
     public void display() {
         // We look at each body and get its screen position
         Vec2 pos = box2d.getBodyPixelCoord(body);
         // Get its angle of rotation
         float a = body.getAngle();
         CirclesVsSquares cvs = CirclesVsSquares.instance();
         cvs.pushMatrix();
         cvs.translate(pos.x, pos.y);
        cvs.rotate(a);
         cvs.fill(0, 255, 0);
         cvs.stroke(0);
         cvs.strokeWeight(1);
         cvs.ellipse(0, 0, r*2, r*2);
         // Let's add a line so we can see the rotation
         cvs.line(0, 0, r, 0);
         cvs.popMatrix();
     }
 
     // This function adds the rectangle to the box2d world
     public void makeBody(float x, float y, float r) {
         // Define a body
         BodyDef bd = new BodyDef();
         
         // Set its position
         bd.position = box2d.coordPixelsToWorld(x, y);
         bd.type = BodyType.DYNAMIC;
         body = box2d.createBody(bd);
 
         // Make the body's shape a circle
         CircleShape cs = new CircleShape();
         cs.m_radius = box2d.scalarPixelsToWorld(r);
 
         FixtureDef fd = new FixtureDef();
         fd.shape = cs;
         
         // Parameters that affect physics
         fd.density = 1;
         fd.friction = 0.05f;
         fd.restitution = 0.1f;
 
         // Attach fixture to body
         body.createFixture(fd);
     }
 
     public void movePlayer(MovementDirection movementDirection) {
         Vec2 vel = this.body.getLinearVelocity();
         if (movementDirection == MovementDirection.LEFT && vel.x > -PLAYER_MAX_SPEED) {
             this.body.applyLinearImpulse(new Vec2(-PLAYER_MOVEMENT_IMPULSE, 0f), this.body.getWorldCenter());
         } else if (movementDirection == MovementDirection.RIGHT && vel.x < PLAYER_MAX_SPEED) {
             this.body.applyLinearImpulse(new Vec2(PLAYER_MOVEMENT_IMPULSE, 0f), this.body.getWorldCenter());
         } else if (movementDirection == MovementDirection.NONE) {
             if (vel.x > PLAYER_NO_MOVEMENT_DAMPING) {
                 this.body.applyLinearImpulse(new Vec2(-PLAYER_NO_MOVEMENT_DAMPING, 0f), this.body.getWorldCenter());
             } else if (vel.x < -PLAYER_NO_MOVEMENT_DAMPING) {
                 this.body.applyLinearImpulse(new Vec2(PLAYER_NO_MOVEMENT_DAMPING, 0f), this.body.getWorldCenter());
             } else {
                 this.body.applyLinearImpulse(new Vec2(-vel.x, 0f), this.body.getWorldCenter());
             }
         }
     }
 
     public void jumpIfPossible() {
         if (this.canMove) {
             this.body.applyLinearImpulse(new Vec2(0, PLAYER_JUMP_IMPULSE), this.body.getWorldCenter());
         }
     }
 }
