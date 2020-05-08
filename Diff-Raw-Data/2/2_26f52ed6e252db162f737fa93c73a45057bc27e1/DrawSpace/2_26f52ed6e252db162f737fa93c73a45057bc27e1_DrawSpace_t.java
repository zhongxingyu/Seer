 /*
  * Copyright (c) 2007 Scott Lembcke, (c) 2011 Jürgen Obernolte
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package org.physics.jipmunk.examples;
 
 import java.awt.*;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import org.physics.jipmunk.Arbiter;
 import org.physics.jipmunk.Body;
 import org.physics.jipmunk.CircleShape;
 import org.physics.jipmunk.Constraint;
 import org.physics.jipmunk.PolyShape;
 import org.physics.jipmunk.SegmentShape;
 import org.physics.jipmunk.Shape;
 import org.physics.jipmunk.Space;
 import org.physics.jipmunk.Util;
 import org.physics.jipmunk.Vector2f;
 import org.physics.jipmunk.constraints.DampedSpring;
 import org.physics.jipmunk.constraints.GrooveJoint;
 import org.physics.jipmunk.constraints.PinJoint;
 import org.physics.jipmunk.constraints.PivotJoint;
 import org.physics.jipmunk.constraints.SlideJoint;
 
 /** @author jobernolte */
 public class DrawSpace {
 	public DrawSpace(GL2 gl) {
 		this.gl = gl;
 		circleVARBuffer = BufferUtils.createFloatBuffer(circleVAR.length);
 		circleVARBuffer.put(circleVAR);
 		circleVARBuffer.flip();
 		pillVARBuffer = BufferUtils.createFloatBuffer(pillVAR.length);
 		pillVARBuffer.put(pillVAR);
 		pillVARBuffer.flip();
 		springVARBuffer = BufferUtils.createFloatBuffer(springVAR.length);
 		springVARBuffer.put(springVAR);
 		springVARBuffer.flip();
 		polyShapeBuffer = BufferUtils.createFloatBuffer(1024);
 	}
 
 	public static class Options {
 		boolean drawHash;
 		boolean drawBBs;
 		boolean drawShapes;
 		float collisionPointSize;
 		float bodyPointSize;
 		float lineThickness;
 
 		public Options() {
 
 		}
 
 		public Options(boolean drawHash,
 				boolean drawBBs,
 				boolean drawShapes,
 				float collisionPointSize,
 				float bodyPointSize,
 				float lineThickness) {
 			this.drawHash = drawHash;
 			this.drawBBs = drawBBs;
 			this.drawShapes = drawShapes;
 			this.collisionPointSize = collisionPointSize;
 			this.bodyPointSize = bodyPointSize;
 			this.lineThickness = lineThickness;
 		}
 	}
 
 	static final Color LINE_COLOR = new Color(0.0f, 0.0f, 0.0f);
 	static final Color COLLISION_COLOR = new Color(1.0f, 0.0f, 0.0f);
 	static final Color BODY_COLOR = new Color(0.0f, 0.0f, 1.0f);
 
 	static final float circleVAR[] = {
 			0.0000f, 1.0000f,
 			0.2588f, 0.9659f,
 			0.5000f, 0.8660f,
 			0.7071f, 0.7071f,
 			0.8660f, 0.5000f,
 			0.9659f, 0.2588f,
 			1.0000f, 0.0000f,
 			0.9659f, -0.2588f,
 			0.8660f, -0.5000f,
 			0.7071f, -0.7071f,
 			0.5000f, -0.8660f,
 			0.2588f, -0.9659f,
 			0.0000f, -1.0000f,
 			-0.2588f, -0.9659f,
 			-0.5000f, -0.8660f,
 			-0.7071f, -0.7071f,
 			-0.8660f, -0.5000f,
 			-0.9659f, -0.2588f,
 			-1.0000f, -0.0000f,
 			-0.9659f, 0.2588f,
 			-0.8660f, 0.5000f,
 			-0.7071f, 0.7071f,
 			-0.5000f, 0.8660f,
 			-0.2588f, 0.9659f,
 			0.0000f, 1.0000f,
 			0.0f, 0.0f, // For an extra line to see the rotation.
 	};
 	static final float pillVAR[] = {
 			0.0000f, 1.0000f, 1.0f,
 			0.2588f, 0.9659f, 1.0f,
 			0.5000f, 0.8660f, 1.0f,
 			0.7071f, 0.7071f, 1.0f,
 			0.8660f, 0.5000f, 1.0f,
 			0.9659f, 0.2588f, 1.0f,
 			1.0000f, 0.0000f, 1.0f,
 			0.9659f, -0.2588f, 1.0f,
 			0.8660f, -0.5000f, 1.0f,
 			0.7071f, -0.7071f, 1.0f,
 			0.5000f, -0.8660f, 1.0f,
 			0.2588f, -0.9659f, 1.0f,
 			0.0000f, -1.0000f, 1.0f,
 
 			0.0000f, -1.0000f, 0.0f,
 			-0.2588f, -0.9659f, 0.0f,
 			-0.5000f, -0.8660f, 0.0f,
 			-0.7071f, -0.7071f, 0.0f,
 			-0.8660f, -0.5000f, 0.0f,
 			-0.9659f, -0.2588f, 0.0f,
 			-1.0000f, -0.0000f, 0.0f,
 			-0.9659f, 0.2588f, 0.0f,
 			-0.8660f, 0.5000f, 0.0f,
 			-0.7071f, 0.7071f, 0.0f,
 			-0.5000f, 0.8660f, 0.0f,
 			-0.2588f, 0.9659f, 0.0f,
 			0.0000f, 1.0000f, 0.0f,
 	};
 	static final float springVAR[] = {
 			0.00f, 0.0f,
 			0.20f, 0.0f,
 			0.25f, 3.0f,
 			0.30f, -6.0f,
 			0.35f, 6.0f,
 			0.40f, -6.0f,
 			0.45f, 6.0f,
 			0.50f, -6.0f,
 			0.55f, 6.0f,
 			0.60f, -6.0f,
 			0.65f, 6.0f,
 			0.70f, -3.0f,
 			0.75f, 6.0f,
 			0.80f, 0.0f,
 			1.00f, 0.0f,
 	};
 
 	private final GL2 gl;
 
 	private FloatBuffer circleVARBuffer;
 	private FloatBuffer pillVARBuffer;
 	private FloatBuffer springVARBuffer;
 	private FloatBuffer polyShapeBuffer;
 
 	void glColorForShape(Shape shape, Space space) {
 		Body body = shape.getBody();
 		if (body != null) {
 			/*
 					 if(body->node.next){
 						 GLfloat v = 0.25f;
 						 glColor3f(v,v,v);
 						 return;
 					 } else if(body->node.idleTime > space->sleepTimeThreshold) {
 						 GLfloat v = 0.9f;
 						 glColor3f(v,v,v);
 						 return;
 					 }
 					 */
 			if (body.isSleeping()) {
 				float v = 0.25f;
 				gl.glColor4f(v, v, v, 1);
 				return;
 			} else if (body.isIdle()) {
 				float v = 0.9f;
 				gl.glColor4f(v, v, v, 1);
 				return;
 			}
 		}
 
 		glColorFromPointer(shape.hashCode());
 	}
 
 	void glColorFromPointer(int ptr) {
 		int val = ptr;
 
 		// hash the pointer up nicely
 		val = (val + 0x7ed55d16) + (val << 12);
 		val = (val ^ 0xc761c23c) ^ (val >> 19);
 		val = (val + 0x165667b1) + (val << 5);
 		val = (val + 0xd3a2646c) ^ (val << 9);
 		val = (val + 0xfd7046c5) + (val << 3);
 		val = (val ^ 0xb55a4f09) ^ (val >> 16);
 
 		int r = (int) ((val) & 0xFF);
 		int g = (int) ((val >> 8) & 0xFF);
 		int b = (int) ((val >> 16) & 0xFF);
 
 		int max = r > g ? (r > b ? r : b) : (g > b ? g : b);
 
 		int mult = 127;
 		int add = 63;
 		r = (r * mult) / max + add;
 		g = (g * mult) / max + add;
 		b = (b * mult) / max + add;
 
 		gl.glColor3f(r / 255.0f, g / 255.0f, b / 255.0f);
 	}
 
 	void drawCircleShape(Body body, CircleShape circle, Space space) {
 //        glVertexPointer(2, GL_FLOAT, 0, circleVAR);
 		gl.glVertexPointer(2, GL2.GL_FLOAT, 0, circleVARBuffer);
 
 		gl.glPushMatrix();
 		{
 			Vector2f center = circle.getTransformedCenter(); // circle - > tc;
 			gl.glTranslatef(center.getX(), center.getY(), 0.0f);
 			gl.glRotatef((float) (body.getAngleInRadians() * 180.0f / Math.PI), 0.0f, 0.0f, 1.0f);
 			float radius = circle.getRadius();
 			gl.glScalef(radius, radius, 1.0f);
 			if (!circle.isSensor()) {
 				glColorForShape(circle, space);
 				//glDrawArrays(GL_TRIANGLE_FAN, 0, circleVAR_count - 1);
 				gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, (circleVAR.length / 2) - 1);
 			}
 
 			gl.glColor4i(LINE_COLOR.getRed(), LINE_COLOR.getGreen(), LINE_COLOR.getBlue(), LINE_COLOR.getAlpha());
 			//glDrawArrays(GL_LINE_STRIP, 0, circleVAR_count);
 			gl.glDrawArrays(GL.GL_LINE_STRIP, 0, circleVAR.length / 2);
 		}
 		gl.glPopMatrix();
 	}
 
 	void drawSegmentShape(Body body, SegmentShape seg, Space space) {
 		Vector2f a = seg.getTa(); // seg->ta;
 		Vector2f b = seg.getTb(); // seg->tb;
 
 		if (seg.getRadius() != 0) {
 			gl.glVertexPointer(3, GL2.GL_FLOAT, 0, pillVARBuffer);
 			gl.glPushMatrix();
 			{
 				Vector2f d = Util.cpvsub(b, a); // cpvsub(b, a);
 				Vector2f r = Util.cpvmult(d, seg.getRadius() / Util.cpvlength(d)); // cpvmult(d, seg->r/cpvlength(d));
 
 				float matrix[] = {
 						r.getX(), r.getY(), 0.0f, 0.0f,
 						-r.getY(), r.getX(), 0.0f, 0.0f,
 						d.getX(), d.getY(), 0.0f, 0.0f,
 						a.getX(), a.getY(), 0.0f, 1.0f,
 				};
				gl.glMultMatrixf(matrix, 0);
 
 				if (!seg.isSensor()) {
 					glColorForShape(seg, space);
 					gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, pillVAR.length / 3);
 				}
 
 				gl.glColor4i(LINE_COLOR.getRed(), LINE_COLOR.getGreen(), LINE_COLOR.getBlue(), LINE_COLOR.getAlpha());
 				gl.glDrawArrays(GL.GL_LINE_LOOP, 0, pillVAR.length / 3);
 			}
 			gl.glPopMatrix();
 		} else {
 			gl.glColor4i(LINE_COLOR.getRed(), LINE_COLOR.getGreen(), LINE_COLOR.getBlue(), LINE_COLOR.getAlpha());
 			gl.glBegin(GL2.GL_LINES);
 			{
 				gl.glVertex2f(a.getX(), a.getY());
 				gl.glVertex2f(b.getX(), b.getY());
 			}
 			gl.glEnd();
 		}
 	}
 
 	void drawPolyShape(Body body, PolyShape poly, Space space) {
 		int count = poly.getNumVertices(); //poly->numVerts;
 		polyShapeBuffer.clear();
 		for (Vector2f vert : poly.getVertices()) {
 			polyShapeBuffer.put(vert.getX()).put(vert.getY());
 		}
 		polyShapeBuffer.flip();
 		gl.glVertexPointer(2, GL2.GL_FLOAT, 0, polyShapeBuffer);
 
 		gl.glPushMatrix();
 		Vector2f center = body.getPosition();
 		gl.glTranslatef(center.getX(), center.getY(), 0.0f);
 		gl.glRotatef((float) (body.getAngleInRadians() * 180.0f / Math.PI), 0.0f, 0.0f, 1.0f);
 
 		if (!poly.isSensor()) {
 			glColorForShape(poly, space);
 			gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, count);
 		}
 
 		gl.glColor4i(LINE_COLOR.getRed(), LINE_COLOR.getGreen(), LINE_COLOR.getBlue(), LINE_COLOR.getAlpha());
 		gl.glDrawArrays(GL.GL_LINE_LOOP, 0, count);
 		gl.glPopMatrix();
 	}
 
 	void drawObject(Shape shape, Space space) {
 		Body body = shape.getBody();
 
 		if (shape instanceof CircleShape) {
 			drawCircleShape(body, (CircleShape) shape, space);
 		} else if (shape instanceof SegmentShape) {
 			drawSegmentShape(body, (SegmentShape) shape, space);
 		} else if (shape instanceof PolyShape) {
 			drawPolyShape(body, (PolyShape) shape, space);
 		}
 	}
 
 	void drawSpring(DampedSpring spring, Body body_a, Body body_b) {
 		Vector2f a = Util.cpvadd(body_a.getPosition(), Util.cpvrotate(spring.getAnchr1(),
 				body_a.getRotation())); //   cpvadd(body_a->p, cpvrotate(spring->anchr1, body_a->rot));
 		Vector2f b = Util.cpvadd(body_b.getPosition(), Util.cpvrotate(spring.getAnchr2(),
 				body_b.getRotation())); //  cpvadd(body_b->p, cpvrotate(spring->anchr2, body_b->rot));
 
 		gl.glPointSize(5.0f);
 		gl.glBegin(GL.GL_POINTS);
 		{
 			gl.glVertex2f(a.getX(), a.getY());
 			gl.glVertex2f(b.getX(), b.getY());
 		}
 		gl.glEnd();
 
 		Vector2f delta = Util.cpvsub(b, a); // cpvsub(b, a);
 
 		gl.glVertexPointer(2, GL2.GL_FLOAT, 0, springVARBuffer);
 		gl.glPushMatrix();
 		{
 			float x = a.getX();
 			float y = a.getY();
 			float cos = delta.getX();
 			float sin = delta.getY();
 			float s = 1.0f / Util.cpvlength(delta);
 
 			float matrix[] = {
 					cos, sin, 0.0f, 0.0f,
 					-sin * s, cos * s, 0.0f, 0.0f,
 					0.0f, 0.0f, 1.0f, 0.0f,
 					x, y, 0.0f, 1.0f,
 			};
 
 			gl.glMultMatrixf(matrix, matrix.length);
 			gl.glDrawArrays(GL.GL_LINE_STRIP, 0, springVAR.length / 2);
 		}
 		gl.glPopMatrix();
 		gl.glPointSize(1.0f);
 	}
 
 	void drawConstraint(Constraint constraint) {
 		Body body_a = constraint.getBodyA();
 		Body body_b = constraint.getBodyB();
 
 		if (PinJoint.class.isInstance(constraint)) {
 			PinJoint joint = (PinJoint) constraint;
 
 			Vector2f a = Util.cpvadd(body_a.getPosition(), Util.cpvrotate(joint.getAnchr1(),
 					body_a.getRotation())); // cpvadd(body_a->p, cpvrotate(joint->anchr1, body_a->rot));
 			Vector2f b = Util.cpvadd(body_b.getPosition(), Util.cpvrotate(joint.getAnchr2(),
 					body_b.getRotation())); // cpvadd(body_b->p, cpvrotate(joint->anchr2, body_b->rot));
 
 			gl.glPointSize(5.0f);
 			gl.glBegin(GL.GL_POINTS);
 			{
 				gl.glVertex2f(a.getX(), a.getY());
 				gl.glVertex2f(b.getX(), b.getY());
 			}
 			gl.glEnd();
 
 			gl.glBegin(GL.GL_LINES);
 			{
 				gl.glVertex2f(a.getX(), a.getY());
 				gl.glVertex2f(b.getX(), b.getY());
 			}
 			gl.glEnd();
 		} else if (SlideJoint.class.isInstance(constraint)) {
 			SlideJoint joint = (SlideJoint) constraint;
 
 			//cpVect a = cpvadd(body_a->p, cpvrotate(joint->anchr1, body_a->rot));
 			//cpVect b = cpvadd(body_b->p, cpvrotate(joint->anchr2, body_b->rot));
 			Vector2f a = Util.cpvadd(body_a.getPosition(), Util.cpvrotate(joint.getAnchr1(),
 					body_a.getRotation())); // cpvadd(body_a->p, cpvrotate(joint->anchr1, body_a->rot));
 			Vector2f b = Util.cpvadd(body_b.getPosition(), Util.cpvrotate(joint.getAnchr2(),
 					body_b.getRotation())); // cpvadd(body_b->p, cpvrotate(joint->anchr2, body_b->rot));
 
 			gl.glPointSize(5.0f);
 			gl.glBegin(GL.GL_POINTS);
 			{
 				gl.glVertex2f(a.getX(), a.getY());
 				gl.glVertex2f(b.getX(), b.getY());
 			}
 			gl.glEnd();
 
 			gl.glBegin(GL.GL_LINES);
 			{
 				gl.glVertex2f(a.getX(), a.getY());
 				gl.glVertex2f(b.getX(), b.getY());
 			}
 			gl.glEnd();
 		} else if (PivotJoint.class.isInstance(constraint)) {
 			PivotJoint joint = (PivotJoint) constraint;
 
 			//cpVect a = cpvadd(body_a->p, cpvrotate(joint->anchr1, body_a->rot));
 			//cpVect b = cpvadd(body_b->p, cpvrotate(joint->anchr2, body_b->rot));
 			Vector2f a = Util.cpvadd(body_a.getPosition(), Util.cpvrotate(joint.getAnchr1(),
 					body_a.getRotation())); // cpvadd(body_a->p, cpvrotate(joint->anchr1, body_a->rot));
 			Vector2f b = Util.cpvadd(body_b.getPosition(), Util.cpvrotate(joint.getAnchr2(),
 					body_b.getRotation())); // cpvadd(body_b->p, cpvrotate(joint->anchr2, body_b->rot));
 
 			gl.glPointSize(10.0f);
 			gl.glBegin(GL.GL_POINTS);
 			{
 				gl.glVertex2f(a.getX(), a.getY());
 				gl.glVertex2f(b.getX(), b.getY());
 			}
 			gl.glEnd();
 		} else if (GrooveJoint.class.isInstance(constraint)) {
 			GrooveJoint joint = (GrooveJoint) constraint;
 
 			Vector2f a = Util.cpvadd(body_a.getPosition(), Util.cpvrotate(joint.getGrooveA(),
 					body_a.getRotation())); // cpvadd(body_a->p, cpvrotate(joint->grv_a, body_a->rot));
 			Vector2f b = Util.cpvadd(body_a.getPosition(), Util.cpvrotate(joint.getGrooveB(),
 					body_a.getRotation())); // cpvadd(body_a->p, cpvrotate(joint->grv_b, body_a->rot));
 			Vector2f c = Util.cpvadd(body_b.getPosition(), Util.cpvrotate(joint.getAnchr2(),
 					body_b.getRotation())); // cpvadd(body_b->p, cpvrotate(joint->anchr2, body_b->rot));
 
 			gl.glPointSize(5.0f);
 			gl.glBegin(GL.GL_POINTS);
 			{
 				gl.glVertex2f(c.getX(), c.getY());
 			}
 			gl.glEnd();
 
 			gl.glBegin(GL.GL_LINES);
 			{
 				gl.glVertex2f(a.getX(), a.getY());
 				gl.glVertex2f(b.getX(), b.getY());
 			}
 			gl.glEnd();
 		} else if (DampedSpring.class.isInstance(constraint)) {
 			drawSpring((DampedSpring) constraint, body_a, body_b);
 		} else {
 //		printf("Cannot draw constraint\n");
 		}
 	}
 
 	public void drawSpace(Space space, Options options) {
 		if (options.drawHash) {
 			/*
 					 glColorMask(GL_FALSE, GL_TRUE, GL_FALSE, GL_TRUE);
 					 drawSpatialHash(space->activeShapes);
 					 glColorMask(GL_TRUE, GL_FALSE, GL_FALSE, GL_FALSE);
 					 drawSpatialHash(space->staticShapes);
 					 glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
 					 */
 		}
 
 		if (options.lineThickness != 0) {
 			gl.glLineWidth(options.lineThickness);
 		}
 		if (options.drawShapes) {
 			//cpSpaceHashEach(space->activeShapes, (cpSpaceHashIterator)drawObject, space);
 			//cpSpaceHashEach(space->staticShapes, (cpSpaceHashIterator)drawObject, space);
 			for (Shape shape : space.getShapes()) {
 				drawObject(shape, space);
 			}
 			for (Shape shape : space.getStaticShapes()) {
 				drawObject(shape, space);
 			}
 		}
 
 		gl.glLineWidth(1.0f);
 		if (options.drawBBs) {
 			/*
 					 glColor3f(0.3f, 0.5f, 0.3f);
 					 cpSpaceHashEach(space->activeShapes, (cpSpaceHashIterator)drawBB, NULL);
 					 cpSpaceHashEach(space->staticShapes, (cpSpaceHashIterator)drawBB, NULL);
 					 */
 		}
 
 		//cpArray *constraints = space->constraints;
 
 		gl.glColor4f(0.5f, 1.0f, 0.5f, 1.0f);
 		for (Constraint constraint : space.getConstraints()) {
 			drawConstraint(constraint);
 		}
 
 		if (options.bodyPointSize != 0.0f) {
 			gl.glPointSize(options.bodyPointSize);
 
 			gl.glBegin(GL.GL_POINTS);
 			{
 				gl.glColor4i(LINE_COLOR.getRed(), LINE_COLOR.getGreen(), LINE_COLOR.getBlue(), LINE_COLOR.getAlpha());
 				for (Body body : space.getBodies()) {
 					Vector2f position = body.getPosition();
 					gl.glVertex2f(position.getX(), position.getY());
 				}
 			}
 
 //			glColor3f(0.5f, 0.5f, 0.5f);
 //			cpArray *components = space->components;
 //			for(int i=0; i<components->num; i++){
 //				cpBody *root = components->arr[i];
 //				cpBody *body = root, *next;
 //				do {
 //					next = body->node.next;
 //					glVertex2f(body->p.getX(), body->p.getY());
 //				} while((body = next) != root);
 //			}
 			gl.glEnd();
 		}
 
 		if (options.collisionPointSize != 0.0f) {
 			gl.glPointSize(options.collisionPointSize);
 			gl.glBegin(GL.GL_POINTS);
 			{
 				for (Arbiter arb : space.getArbiters()) {
 					gl.glColor4i(COLLISION_COLOR.getRed(), COLLISION_COLOR.getGreen(), COLLISION_COLOR.getBlue(),
 							COLLISION_COLOR.getAlpha());
 					for (int i = 0; i < arb.getNumContacts(); i++) {
 						Vector2f contactPoint = arb.getPoint(i);
 						gl.glVertex2f(contactPoint.getX(), contactPoint.getY());
 					}
 				}
 			}
 			gl.glEnd();
 		}
 	}
 }
