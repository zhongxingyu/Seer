 /*
  * Copyright 2012 FishGames.
  * FishGames Proprietary and Confidential.
  *
  */
 package fishgames.asteroids;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.collision.shapes.Shape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.*;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.input.Keyboard;
 import static org.lwjgl.opengl.GL11.glPopMatrix;
 import static org.lwjgl.opengl.GL11.glPushMatrix;
 import org.lwjgl.util.vector.Vector3f;
 
 /**
  * Procedurally-generated starship (constructed as a quadrilateral); modeled as 
  * a pair of triangles.
  * 
  * @author Matt Fichman <matt.fichman@gmail.com>
  */
 public class Starship extends OutlinedObject implements Renderable {
    
     private OutlinedObject mainThruster = new OutlinedObject();
     private OutlinedObject leftThruster = new OutlinedObject();
     private OutlinedObject rightThruster = new OutlinedObject();
     private Body body;
     
     private static float SCALE = 0.8f;
     private static float DENSITY = 1.0f;
     private static float THRUST = 4.f;
     private static float SPIN = 4.f;
     public static int TYPE = 0x2;
     public static int MASK = Starship.TYPE | Rock.TYPE; // | Projectile.TYPE | Upgrade.TYPE
     
     /**
      * Creates a new starship with the given color.
      * @param color 
      */
     public Starship(Vector3f color) {
         this.fillColor = color;
         this.outlineColor = new Vector3f(1.f, 1.f, 1.f);
        this.outlineScale = new Vector3f(1.08f, 1.08f, 1.08f);
         // This is just fudged to make the outline look good.
         
         this.polygon = getHullPolygon();
 
         this.mainThruster.polygon = getMainThrusterPolygon();
         this.mainThruster.fillColor = new Vector3f(1.0f, .85f, 0.2f);
 
         BodyDef bodyDef = new BodyDef();
         bodyDef.type = BodyType.DYNAMIC;
         this.body = Asteroids.world.createBody(bodyDef);
         for (Shape shape : this.polygon.getShapes()) {
             Fixture fixture = this.body.createFixture(shape, DENSITY);
             Filter filter = new Filter();
             filter.categoryBits = TYPE;
             filter.maskBits = MASK;
             fixture.setFilterData(filter);
         }
         body.setTransform(Asteroids.getWorldSize().mul(.5f), .0f);
     }
     
     /**
      * Applies forces to the starship, and updates the transform.
      */
     @Override
     public void update() {
         Vec2 forward = this.body.getWorldVector(new Vec2(0.f, THRUST));
         if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
             this.body.applyLinearImpulse(forward.negate(), this.body.getWorldCenter());
         }
         if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
             this.body.applyLinearImpulse(forward, this.body.getWorldCenter());
         }
         if (Keyboard.isKeyDown(Keyboard.KEY_J) && !Keyboard.isKeyDown(Keyboard.KEY_L)) {
             this.body.setAngularVelocity(-SPIN);
         } else if (Keyboard.isKeyDown(Keyboard.KEY_L) && !Keyboard.isKeyDown(Keyboard.KEY_J)) {
             this.body.setAngularVelocity(SPIN);
         } else {
             this.body.setAngularVelocity(0.f);
         }
         Asteroids.wrapTransform(this.body);
     }
     
     /**
      * Renders the starship, with interpolation factor 'alpha'.
      * @param alpha 
      */
     @Override
     public void render(float alpha) {
         glPushMatrix();
         Asteroids.setTransform(this.body, alpha);
         super.render(alpha);
         if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
             this.mainThruster.render(alpha);
         }
         glPopMatrix();
     }
     
     /**
      * Returns the polygon shape for the hull of the starship.
      * @return 
      */
     static Polygon getHullPolygon() {
         if (hullPolygon == null) {
             FloatBuffer vert = BufferUtils.createFloatBuffer(2 * 4);
             vert.put(0, SCALE * 0.f); // Aft # 0
             vert.put(1, SCALE * 1.f);
             vert.put(2, SCALE * -1.8f); // Right wing # 1
             vert.put(3, SCALE * 2.f);
             vert.put(4, SCALE * 0.f); // Bow # 2
             vert.put(5, SCALE * -2.f);
             vert.put(6, SCALE * 1.8f); // Left wing # 3
             vert.put(7, SCALE * 2.f);
 
             IntBuffer ind = BufferUtils.createIntBuffer(2 * 3);
             ind.put(0, 0); // Right side
             ind.put(1, 1);
             ind.put(2, 2);
             ind.put(3, 0); // Left side
             ind.put(4, 2);
             ind.put(5, 3);
             hullPolygon = new Polygon(vert, ind, true);
             
             Vec2[] triangle1 = new Vec2[3];
             triangle1[0] = new Vec2(vert.get(0), vert.get(1));
             triangle1[1] = new Vec2(vert.get(2), vert.get(3));
             triangle1[2] = new Vec2(vert.get(4), vert.get(5));
             PolygonShape shape1 = new PolygonShape();
             shape1.set(triangle1, triangle1.length);
             hullPolygon.addShape(shape1);
             
             Vec2[] triangle2 = new Vec2[3];
             triangle2[0] = new Vec2(vert.get(0), vert.get(1));
             triangle2[1] = new Vec2(vert.get(4), vert.get(5));
             triangle2[2] = new Vec2(vert.get(6), vert.get(7));
             PolygonShape shape2 = new PolygonShape();
             shape2.set(triangle2, triangle2.length);
             hullPolygon.addShape(shape2);
         }
         return hullPolygon;
     }
     
     /**
      * Returns the shape for the main thruster of the starship.
      * @return 
      */
     static Polygon getMainThrusterPolygon() {
         if (mainThrusterPolygon == null) {
             FloatBuffer vert = BufferUtils.createFloatBuffer(2 * 4);
             vert.put(0, SCALE * 0.f);
             vert.put(1, SCALE * 1.3f);
             vert.put(2, SCALE * -.8f);
             vert.put(3, SCALE * 1.8f);
             vert.put(4, SCALE * 0.f);
             vert.put(5, SCALE * 3.2f);
             vert.put(6, SCALE * .8f);
             vert.put(7, SCALE * 1.8f);
 
             IntBuffer ind = BufferUtils.createIntBuffer(2 * 3);
             ind.put(0, 0); // Right side
             ind.put(1, 1);
             ind.put(2, 2);
             ind.put(3, 0); // Left side
             ind.put(4, 3);
             ind.put(5, 2);
             mainThrusterPolygon = new Polygon(vert, ind, false);
         }
         return mainThrusterPolygon;
     }
     
     static Polygon hullPolygon;
     static Polygon mainThrusterPolygon;
 }
