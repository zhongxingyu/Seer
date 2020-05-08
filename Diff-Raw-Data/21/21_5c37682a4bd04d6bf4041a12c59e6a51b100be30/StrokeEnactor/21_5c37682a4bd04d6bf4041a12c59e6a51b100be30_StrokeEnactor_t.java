 package com.scarytom.collider.physics;
 
 import static com.scarytom.collider.model.Position.at;
import static java.lang.Math.abs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jbox2d.callbacks.ContactImpulse;
 import org.jbox2d.callbacks.ContactListener;
 import org.jbox2d.collision.Manifold;
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.Fixture;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 import org.jbox2d.dynamics.contacts.Contact;
 
 import com.scarytom.collider.model.Ball;
 import com.scarytom.collider.model.BallInPlay;
 import com.scarytom.collider.model.Hoop;
 import com.scarytom.collider.model.Peg;
 import com.scarytom.collider.model.Pitch;
 import com.scarytom.collider.model.Stroke;
 
 public final class StrokeEnactor {
 
    private static final float TIME_STEP = 0.001f;
     private static final int VELOCITY_ITERATIONS_PER_STEP = 3;
     private static final int POSITION_ITERATIONS_PER_STEP = 8;
 
     private static final float REST_VELOCITY_THRESHOLD = 0.01f;
 
     private final World world = new World(new Vec2(0.0f, 0.0f), true);
 
     public StrokeEnactor(Pitch pitch) {
         world.setContactListener(new CustomContactListener());
         
         for (Hoop hoop : pitch.hoops) {
             createHoop(hoop);
         }
         createPeg(pitch.peg);
     }
 
     public Transition makeStroke(List<BallInPlay> balls, Stroke stroke) {
        setupStroke(balls, stroke);
        return simulate();
    }

    public World debugStroke(List<BallInPlay> balls, Stroke stroke) {
        setupStroke(balls, stroke);
        return world;
    }

    private void setupStroke(List<BallInPlay> balls, Stroke stroke) {
         clearBalls();
         for (BallInPlay ballInPlay : balls) {
             if (stroke.ball.equals(ballInPlay.ball)) {
                 createBall(ballInPlay, 9f, -2.28f);
             }
             else {
                 createStillBall(ballInPlay);
             }
         }
     }
 
     private void createStillBall(BallInPlay ballInPlay) {
         createBall(ballInPlay, 0.0f, 0.0f);
     }
 
     private void createBall(BallInPlay ballInPlay, float vx, float vy) {
         final CircleShape shape = new CircleShape();
         shape.m_radius = ballInPlay.ball.radius;
         FixtureDef definition = new FixtureDef();
         definition.shape = shape;
         definition.density = 1.0f;
         definition.restitution = 0.66f;
         
         final BodyDef bd = new BodyDef();
         bd.type = BodyType.DYNAMIC;
         bd.position.set(ballInPlay.position.x, ballInPlay.position.y);
         bd.linearDamping = 0.8f;
         bd.fixedRotation = true;
         bd.linearVelocity.set(vx, vy);
         
         final Body body = world.createBody(bd);
         body.setUserData(ballInPlay.ball);
         body.createFixture(definition);
     }
 
     private void createHoop(Hoop hoop) {
         final CircleShape shape = new CircleShape();
         shape.m_radius = hoop.legRadius;
         FixtureDef definition = new FixtureDef();
         definition.shape = shape;
         
         final BodyDef bd = new BodyDef();
         bd.type = BodyType.STATIC;
         bd.position.set(hoop.leg1Position.x, hoop.leg1Position.y);
         world.createBody(bd).createFixture(definition);
         bd.position.set(hoop.leg2Position.x, hoop.leg2Position.y);
         world.createBody(bd).createFixture(definition);
     }
 
     private void createPeg(Peg peg) {
         final CircleShape shape = new CircleShape();
         shape.m_radius = peg.radius;
         FixtureDef definition = new FixtureDef();
         definition.shape = shape;
         
         final BodyDef bd = new BodyDef();
         bd.type = BodyType.STATIC;
         bd.position.set(peg.position.x, peg.position.y);
         world.createBody(bd).createFixture(definition);
     }
 
     private Transition simulate() {
         List<List<BallInPlay>> ballPositions = new ArrayList<List<BallInPlay>>();
         do {
             world.step(TIME_STEP, VELOCITY_ITERATIONS_PER_STEP, POSITION_ITERATIONS_PER_STEP);
             ballPositions.add(currentBallPositions());
         } while (!isFinished());
         return new Transition(TIME_STEP, ballPositions);
     }
 
     private List<BallInPlay> currentBallPositions() {
         final List<BallInPlay> result = new ArrayList<BallInPlay>();
         
         Body body = world.getBodyList();
         while (body != null) {
             if (body.getUserData() instanceof Ball) {
                 result.add(new BallInPlay((Ball)body.getUserData(), at(body.getWorldCenter().x,  body.getWorldCenter().y)));
             }
             body = body.getNext();
         }
         return result;
     }
 
     private boolean isFinished() {
         Body body = world.getBodyList();
         while (body != null) {
            if (abs(body.m_linearVelocity.x) > REST_VELOCITY_THRESHOLD ||
                abs(body.m_linearVelocity.y) > REST_VELOCITY_THRESHOLD) {
                 return false;
             }
             body = body.getNext();
         }
         return true;
     }
 
     private void clearBalls() {
         Body body = world.getBodyList();
         while (body != null) {
             if (body.getUserData() instanceof Ball) {
                 world.destroyBody(body);
             }
             body = body.getNext();
         }
     }
 
     private static final class CustomContactListener implements ContactListener {
         public void beginContact(Contact contact) { }
         public void endContact(Contact contact) { }
         public void postSolve(Contact contact, ContactImpulse impulse) { }
         public void preSolve(Contact contact, Manifold oldManifold) {
             Fixture fixtureA = contact.getFixtureA();
             Fixture fixtureB = contact.getFixtureB();
             System.out.println(fixtureA.getBody().getUserData() + " hit " + fixtureB.getBody().getUserData());
         }
     }
 }
 
