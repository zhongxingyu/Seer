 package org.skullforge.asteroidpush.appearances;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.MathUtils;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.World;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.Before;
 import org.junit.Test;
 import org.newdawn.slick.geom.Shape;
 import org.skullforge.asteroidpush.ClassMockery;
 import org.skullforge.asteroidpush.parts.Part;
 import org.skullforge.asteroidpush.ui.Renderer;
 
 public class SimpleAppearanceTest {
    Mockery context;
    Part partMock;
    Renderer rendererMock;
    SimpleAppearance testAppearance;
    World testWorld;
    Body testBody;
 
    @Before
    public void setUp() throws Exception {
       context = new ClassMockery();
       partMock = context.mock(Part.class);
       rendererMock = context.mock(Renderer.class);
       testAppearance = new SimpleAppearance(partMock);
 
       Vec2 gravity = new Vec2();
       testWorld = new World(gravity, true);
       BodyDef bodyDef = new BodyDef();
       bodyDef.position = new Vec2(5.0f, 10.0f);
       bodyDef.angle = MathUtils.QUARTER_PI;
       testBody = testWorld.createBody(bodyDef);
    }
 
    @Test
    public void testGetPolygonSilhouette() {
       PolygonShape shape = new PolygonShape();
       shape.setAsBox(5.0f, 5.0f);
       testBody.createFixture(shape, 1.0f);
       ArrayList<Body> bodies = new ArrayList<Body>();
       bodies.add(testBody);
       final ArrayList<Body> bodyList = new ArrayList<Body>(bodies);
 
       context.checking(new Expectations() {
          {
             oneOf(partMock).getBodies();
             will(returnValue(bodyList));
          }
       });
 
       ArrayList<Shape> silhouette = testAppearance.getSilhouette();
       assertEquals(1, silhouette.size());
       Shape mappedShape = silhouette.get(0);
 
       final float diagonal = MathUtils.sqrt(50.0f);
       float[][] expectedPoints = { { 5.00f, 10.00f - diagonal },
             { 5.00f + diagonal, 10.00f }, { 5.00f, 10.00f + diagonal },
            { 5.00f - diagonal, 10.00f } };
 
       assertEquals(expectedPoints.length, mappedShape.getPointCount());
       for (int i = 0; i < mappedShape.getPointCount(); ++i) {
          StringBuffer message = new StringBuffer();
          message.append("Point ").append(i);
          assertArrayEquals(message.toString(),
                            expectedPoints[i],
                            mappedShape.getPoint(i),
                            0.01f);
      }

       context.assertIsSatisfied();
    }
 
    @Test
    public void testGetCircleSilhouette() {
       CircleShape shape = new CircleShape();
       shape.m_p.set(0.0f, 0.0f);
       shape.m_radius = 5.0f;
       testBody.createFixture(shape, 1.0f);
       ArrayList<Body> bodies = new ArrayList<Body>();
       bodies.add(testBody);
       final ArrayList<Body> bodyList = new ArrayList<Body>(bodies);
 
       context.checking(new Expectations() {
          {
             oneOf(partMock).getBodies();
             will(returnValue(bodyList));
          }
       });
 
       ArrayList<Shape> silhouette = testAppearance.getSilhouette();
       assertEquals(1, silhouette.size());
       Shape mappedShape = silhouette.get(0);
 
       final float expectedRadius = 4.99f;
       for (float alpha = 0.0f; alpha < 2 * MathUtils.PI; alpha += MathUtils.PI / 16.0f) {
          StringBuffer message = new StringBuffer();
          message.append("Angle ").append(alpha);
          assertTrue(message.toString(),
                     mappedShape.contains(MathUtils.sin(alpha) * expectedRadius
                           + 5.0f, MathUtils.cos(alpha) * expectedRadius + 10.0f));
       }
 
       context.assertIsSatisfied();
    }
 
    @Test
    public void testGetUnknownSilhouette() {
       Shape mappedShape = testAppearance.convertToSlickShape(null);
       assertNotNull(mappedShape);
    }
 }
