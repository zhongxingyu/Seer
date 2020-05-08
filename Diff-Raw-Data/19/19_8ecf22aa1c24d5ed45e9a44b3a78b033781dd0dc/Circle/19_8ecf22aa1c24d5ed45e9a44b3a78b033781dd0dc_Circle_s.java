 /*
 * Owl Platform
 * Copyright (C) 2012 Robert Moore and the Owl Platform
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *  
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package edu.rutgers.winlab.junsim;
 
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 
 /**
  * @author Robert Moore
  * 
  */
 public class Circle implements Drawable {
   public float radius = 0f;
   public Point2D.Float center = new Point2D.Float();
 
   @Override
   public void draw(Graphics2D g) {
     AffineTransform origTransform = g.getTransform();
 
     g.drawOval((int) (this.center.getX() - this.radius),
         (int) (this.center.getY() - radius), (int) (this.radius * 2),
         (int) (this.radius * 2));
 
     g.setTransform(origTransform);
   }
 
   public boolean contains(Point2D p) {
     float dist = (float) Math.sqrt(Math.pow(p.getX() - this.center.getX(), 2)
         - Math.pow(p.getY() - this.center.getY(), 2));
     return this.radius >= dist;
   }
   
   public double getCenterX(){
     return this.center.getX();
   }
   
   public double getCenterY(){
     return this.center.getY();
   }
 
 }
