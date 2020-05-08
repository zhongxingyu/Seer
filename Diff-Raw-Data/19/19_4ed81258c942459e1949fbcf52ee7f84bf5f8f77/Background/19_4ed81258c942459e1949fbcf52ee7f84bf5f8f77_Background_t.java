 package net.qmat.qmhh.models;
 
 import net.qmat.qmhh.Main;
 import net.qmat.qmhh.utils.CPoint2;
 import net.qmat.qmhh.utils.PPoint2;
 import net.qmat.qmhh.utils.Settings;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 
 import processing.core.PGraphics;
 import processing.core.PImage;
 
 public class Background extends ProcessingObject {
 	
 	private static int NR_SECTIONS = 16;
 	
 	private float centerX, centerY;
 	private float innerDiameter, outerDiameter;
 	private float innerRadius, outerRadius;
 	private Body body;
 	private Vec2[] vertices;
 	private PImage backdrop, backdropGradient;
 	private PGraphics backdropMask;
 	
 	public Background() {
 		centerX = Settings.getInteger(Settings.PR_CENTER_X);
 		centerY = Settings.getInteger(Settings.PR_CENTER_Y);
 		innerDiameter = Settings.getInteger(Settings.PR_RING_INNER_RADIUS) * 2.0f;
 		outerDiameter = Settings.getInteger(Settings.PR_RING_OUTER_RADIUS) * 2.0f;
 		innerRadius = innerDiameter / 2.0f;
 		outerRadius = outerDiameter / 2.0f;
 		
 		backdrop = p.loadImage(Settings.getString(Settings.PR_BACKDROP_FILE));
 		backdropGradient = p.loadImage("backdrop_gradient.png");
 		createBackdropMask();
 		backdrop.mask(backdropMask);
 		
 		createRingBox2D();
 	}
 	
 	private void createBackdropMask() {
 		backdropMask = p.createGraphics(backdrop.width, backdrop.height, Main.P3D);
 		backdropMask.beginDraw();
 		backdropMask.ellipseMode(Main.CENTER);
 		backdropMask.background(0);
 		backdropMask.fill(255);
 		backdropMask.translate(backdropMask.width/2, backdropMask.height/2);
 		backdropMask.ellipse(0, 0, innerDiameter, innerDiameter);
 		backdropMask.endDraw();
 	}
 	
 	@SuppressWarnings("static-access")
 	public void draw() {
 		p.background(0);
 		p.pushMatrix();
 		p.translate(centerX, centerY);
 		
 		// draw backdrop
 		p.imageMode(Main.CENTER);
 		p.image(backdrop, 0, 0); //, innerRadius*2, innerRadius*2);
 		// draw gradient over it
 		p.image(backdropGradient, 0, 0, innerRadius*2, innerRadius*2);
 		
 		p.popMatrix();
 		
 		
 		/* Debug the ring boundary.
 		int steps = 60;
 		float stepSize = Main.TWO_PI / steps;
 		p.rectMode(p.CENTER);
 		float w = box2d.scalarWorldToPixels(0.1f);
 		float h = box2d.scalarWorldToPixels(2.0f);
 		for(int i=0; i<vertices.length; i++) {
 			p.pushMatrix();
 			Vec2 pos = box2d.coordWorldToPixels(vertices[i]);
 			p.translate(pos.x, pos.y);
 			float angle = stepSize * i;
 			p.rotate(angle);
 			p.rect(0, 0, w, h);
 			p.popMatrix();
 		}
 		*/
 	}
 	
 	public void destroy() {
 		box2d.destroyBody(body);
 	}
 	
 	private Vec2 calculateChainPosition(float angle) {
 		PPoint2 pos1 = new PPoint2(innerDiameter / 2.0f, angle);
 		Vec2 pos2 = pos1.toVec2();
 		Vec2 pos3 = new Vec2(pos2.x, pos2.y);
 		Vec2 pos4 = box2d.coordPixelsToWorld(pos3);
 		return pos4;
 	}
 	
 	private void createRingBox2D() {
 		
 		int steps = 60;
 		float stepSize = Main.TWO_PI / steps;
 		vertices = new Vec2[steps];
 		body = box2d.getGroundBody();
 		// N.B. link the only static body to the background.
 		body.setUserData(this);
 		for(int i=0; i<steps; i++) {
 			float angle = stepSize * i;
 			vertices[i] = calculateChainPosition(angle);
 			PolygonShape shape = new PolygonShape();
 			shape.setAsBox(0.1f, 5.0f, vertices[i], angle);
 			body.createFixture(shape, 0.0f);
 		}
 	}
 
 }
