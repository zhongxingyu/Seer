 package com.beartronics.jschema;
 
 // The Nature of Code
 // <http://www.shiffman.net/teaching/nature>
 // Spring 2010
 // PBox2D example
 
 import pbox2d.*;
 import org.jbox2d.collision.shapes.*;
 import org.jbox2d.common.*;
 import org.jbox2d.dynamics.*;
 
 import processing.core.PApplet;
 import processing.core.*;
 import processing.core.PConstants.*; 
 import processing.data.*; 
 import processing.event.*; 
 import processing.opengl.*; 
 
 
 // A rectangular box
 public class Box extends Object2D {
 
     // We need to keep track of a Body and a width and height
     float w;
     float h;
     float alpha = 255;
     final static int MAX_DENSITY = 10;
 
     // Constructor
     Box(Plane p, float x, float y, float w, float h, float density) {
         super(p);
         _Box(p, x,y,w,h,density);
     }
 
 
     Box(Plane p, float x, float y, float w, float h, float density, int color) {
         super(p);
         _Box(p, x,y,w,h,density);
         this.color = color;
     }
 
     void _Box(Plane p, float x, float y, float w, float h, float density) {
         this.color = app.color(0,0,0);
         this.density = density;
         this.w = w;
         this.h = h;
 
         this.alpha = app.map(density, 0, MAX_DENSITY, 0, 255);
 
         // Add the box to the box2d world
         makeBody(box2d, new Vec2(x, y), w, h, density);
     }
     
     void setColor(int c) {
         this.color = c;
     }
 
     // Drawing the box
     void display() {
         // We look at each body and get its screen position
         Vec2 pos = box2d.getBodyPixelCoord(body);
         // Get its angle of rotation
         float a = body.getAngle();
 
         app.pushStyle();
         app.rectMode(PConstants.CENTER);
         app.pushMatrix();
         app.translate(pos.x, pos.y);
         app.rotate(-a);
         app.fill(app.red(color), app.green(color), app.blue(color), (float)(plane.alpha/255.0) * alpha);
         app.strokeWeight(2);
         app.stroke(plane.borderColor);
         app.rect(0, 0, w, h);
         app.popMatrix();
         app.popStyle();
     }
 
     boolean isHollow() {
         return alpha < 128;
     }
 
     boolean isSolid() {
         return alpha >= 128;
     }
 
     // To move to another plane, we have to destroy the object in this plane, and
     // create a copy of it in the target plane.
     // Returns the new Box that was created in the target plane.
     void moveToPlane(Plane newPlane) {
         super.moveToPlane(newPlane);
         float a = getAngle();
         Vec2 pos = box2d.getBodyPixelCoord(body);
         // delete old body from plane box2d world
         removeAllJoints();
         killBody();
 
         // Switch our box2d pointers to the new plane
         plane = newPlane;
         box2d = newPlane.box2d;
         makeBody(box2d, pos, w, h, density);
         setAngle(a);
     }
 
     // This function adds the rectangle to the box2d world
     void makeBody(PBox2D world, Vec2 center, float w_, float h_, float density) {
 
         // Define a polygon (this is what we use for a rectangle)
         PolygonShape sd = new PolygonShape();
         float box2dW = world.scalarPixelsToWorld(w_/2);
         float box2dH = world.scalarPixelsToWorld(h_/2);
         sd.setAsBox(box2dW, box2dH);
 
         // Define a fixture
         FixtureDef fd = new FixtureDef();
         fd.shape = sd;
         // Parameters that affect physics
         //fd.density = density;
         fd.density = 1;
         fd.friction = 0.6f;
         fd.restitution = 0.3f;
 
         // Define the body and make it from the shape
         BodyDef bd = new BodyDef();
         bd.type = BodyType.DYNAMIC;
         bd.position.set(world.coordPixelsToWorld(center));
 
         body = world.createBody(bd);
         fixture = body.createFixture(fd);
 
         // Give it some initial random velocity
         //body.setLinearVelocity(new Vec2(app.random(-5, 5), app.random(2, 5)));
         //body.setAngularVelocity(app.random(-5, 5));
         super.makeBody();
     }
 
     public String toString() {
         // We look at each body and get its screen position
         Vec2 pos = box2d.getBodyPixelCoord(body);
         // Get its angle of rotation
         float a = body.getAngle();
         float alpha = app.map(density, 0, MAX_DENSITY, 0, 255);
         return String.format("{BOX %d x,y=(%2f, %2f) w,h=(%2f, %2f) rot=%2f density=%2f color=%x, alpha=%2f}",index, pos.x,pos.y,w,h,a,density,color,alpha);
     }
 
 
 }
 
 
