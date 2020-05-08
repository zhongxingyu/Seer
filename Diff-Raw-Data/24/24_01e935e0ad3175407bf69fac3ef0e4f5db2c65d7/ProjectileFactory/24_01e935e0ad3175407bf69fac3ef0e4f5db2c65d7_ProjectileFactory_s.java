 package org.skullforge.asteroidpush.entities;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.collision.shapes.Shape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 
 public class ProjectileFactory implements EntityFactory {
    final World world;
    final Vec2 velocity;
 
   public ProjectileFactory(World world, Vec2 velocity) {
       this.world = world;
       this.velocity = velocity;
    }
 
    @Override
    public Entity createEntity(Vec2 location) {
       Body body = world.createBody(getBodyDef(location));
       body.createFixture(getFixtureDef());
       PassiveObject entity = new PassiveObject(body);
       return entity;
    }
 
    private BodyDef getBodyDef(Vec2 location) {
       BodyDef def = new BodyDef();
       def.type = BodyType.DYNAMIC;
       def.position.set(location);
       def.linearDamping = 0.05f;
       def.angularDamping = 0.01f;
       def.fixedRotation = false;
       def.linearVelocity = velocity;
       return def;
    }
 
    private FixtureDef getFixtureDef() {
       FixtureDef def = new FixtureDef();
       def.density = 500.0f;
       def.friction = 0.1f;
       def.restitution = 0.8f;
       def.shape = getShape();
       return def;
    }
 
    private Shape getShape() {
       PolygonShape shape = new PolygonShape();
       Vec2 vertices[] = new Vec2[] {
             new Vec2(-0.1f, 0.1f),
             new Vec2(-0.1f, -0.1f),
             new Vec2(0.2f, 0.0f)
       };
       shape.set(vertices, vertices.length);
       return shape;
    }
 
 }
