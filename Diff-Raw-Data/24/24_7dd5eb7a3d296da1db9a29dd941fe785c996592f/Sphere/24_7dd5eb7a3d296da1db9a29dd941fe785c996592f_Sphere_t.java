 /**
  * Copyright (c) 2003-2007, Xith3D Project Group all rights reserved.
  * 
  * Portions based on the Java3D interface, Copyright by Sun Microsystems.
  * Many thanks to the developers of Java3D and Sun Microsystems for their
  * innovation and design.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * Neither the name of the 'Xith3D Project Group' nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
  * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE
  */
 /*
  * Copyright (c) 2002 Shaven Puppy Ltd
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * * Neither the name of 'Shaven Puppy' nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jagatoo.spatial.bodies;
 
import org.jagatoo.util.heaps.VectorHeap;
 import org.openmali.FastMath;
 import org.openmali.vecmath.Point3f;
 import org.openmali.vecmath.Tuple3f;
import org.openmali.vecmath.Vector3f;
 
 /**
  * An efficient Sphere.
  * 
  * Creation date: (06/02/2001 00:08:09)
  * 
  * @author Jack Ritter from "Graphics Gems", Academic Press, 1990
  * @author cas
  * @author Marvin Froehlich (aka Qudus)
  */
 public class Sphere implements java.io.Serializable, Body
 {
 	private static final long serialVersionUID = 5096988865873236385L;
     
     private Point3f center;
     private Point3f center2 = new Point3f();
     private float radius;
     private float radius_sq;
     
     /**
      * Sets this Sphere's center point.
      * 
      * @param x
      * @param y
      * @param z
      */
     public void setCenter( float cx, float cy, float cz )
     {
         this.center.set( cx, cy, cz );
     }
     
     /**
      * Sets this Sphere's center point.
      * 
      * @param center
      */
     public void setCenter( Tuple3f center )
     {
         setCenter( center.x, center.y, center.z );
     }
     
     /**
      * @return this Sphere's center point.
      */
     public Point3f getCenter()
     {
         center2.set( center );
         
         return( center2 );
     }
     
     /**
      * Gets this Sphere's center point.
      * 
      * @param center
      */
     public Tuple3f getCenter( Tuple3f center )
     {
         assert ( center != null );
         
         center.set( this.center );
         
         return( center );
     }
     
     /**
      * @return the Sphere's center point's x coordinate.
      */
     public float getCenterX()
     {
         return( center.x );
     }
     
     /**
      * @return the Sphere's center point's y coordinate.
      */
     public float getCenterY()
     {
         return( center.y );
     }
     
     /**
      * @return the Sphere's center point's z coordinate.
      */
     public float getCenterZ()
     {
         return( center.z );
     }
     
     /**
      * Sets the Sphere's radius.
      * 
      * @param radius
      */
     public void setRadius( float radius )
     {
         this.radius = radius;
         this.radius_sq = radius * radius;
     }
     
     /**
      * @return the Sphere's radius.
      */
     public float getRadius()
     {
         return( radius );
     }
     
     /**
      * @return the square of the Sphere's radius.
      */
     public float getRadiusSquared()
     {
         return( radius_sq );
     }
     
     /**
      * Checks, if the 2D-point is in the XY-projected circle.
      * 
      * @param px
      * @param py
      * @param distance the distance to (virtually) expand the radius by for this check
      */
     public boolean containsXYPlus( float px, float py, float distance )
     {
         final float dx = px - getCenterX();
         final float dy = py - getCenterY();
         
         final float dist = FastMath.sqrt( (dx * dx) + (dy * dy) );
         
         if ( dist < ( getRadius() + distance ) )
         {
             return( true );
         }
         
         return( false );
     }
     
     /**
      * Checks, if the 2D-point is in the XY-projected circle.
      * 
      * @param px
      * @param py
      * @param distance the distance to (virtually) expand the radius by for this check
      */
     public boolean containsPlus( float px, float py, float pz, float distance )
     {
         final float dx = px - getCenterX();
         final float dy = py - getCenterY();
         final float dz = pz - getCenterZ();
         
         final float dist = FastMath.sqrt( (dx * dx) + (dy * dy) + (dz * dz) );
         
         if ( dist < ( getRadius() + distance ) )
         {
             return( true );
         }
         
         return( false );
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean contains( float px, float py, float pz )
     {
         return( Classifier.classifySpherePoint( this, px, py, pz ) == Classifier.Classification.INSIDE );
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean contains( Point3f point )
     {
         return( contains( point.x, point.y, point.z ) );
     }
     
     private void combineWithSphere( float xx, float yy, float zz, float rr ) 
     {
         final float dx = xx - getCenterX();
         final float dy = yy - getCenterY();
         final float dz = zz - getCenterZ();
         final float d = FastMath.sqrt( (dx * dx) + (dy * dy) + (dz * dz) );
         
         if ( getRadius() >= d + rr )
         {
             // we enclose other sphere
             return;
         }
         
         if ( rr >= d + getRadius() )
         {
             // other sphere encloses us
             this.setCenter( xx, yy, zz );
             this.setRadius( rr );
             
             return;
         }
         
        Vector3f trans = VectorHeap.alloc();
         
        trans.set( dx, dy, dz );
        trans.normalize();
        trans.scale( ( d - getRadius() ) / 2.0f );
        
        setCenter( getCenterX() + trans.x,
                   getCenterY() + trans.y,
                   getCenterZ() + trans.z
                 );
         
        /*
         setCenter( getCenterX() + (dx / 2.0f),
                    getCenterY() + (dy / 2.0f),
                    getCenterZ() + (dz / 2.0f)
                  );
        */
        
        final float dia = getRadius() + d + rr;
        
        setRadius( dia / 2.0f );
     }
     
     private void combineWithSphere( Sphere sphere )
     {
         combineWithSphere( sphere.getCenterX(), sphere.getCenterY(), sphere.getCenterZ(), sphere.getRadius() );
     }
     
     private void combineWithPoint( float px, float py, float pz )
     {
         combineWithSphere( px, py, pz, 0.0f );
     }
     
     private void combineWithPoint( Tuple3f p )
     {
         combineWithPoint( p.x, p.y, p.z );
     }
     
     private void combineWithBox( Box box )
     {
         combineWithPoint( box.getLower() );
         combineWithPoint( box.getUpper() );
     }
     
     /**
      * {@inheritDoc}
      */
     public void combine( Body body )
     {
         if ( body instanceof Sphere )
             combineWithSphere( (Sphere)body );
         else if ( body instanceof Box )
             combineWithBox( (Box)body );
         else if ( body instanceof ConvexHull )
             throw( new Error( "ConvexHull not supported yet" ) );
         else
             throw( new Error( "unknown Body type" ) );
     }
     
     /**
      * {@inheritDoc}
      */
     public void combine( Body[] bos )
     {
         for ( int i = 0; i < bos.length;i++ )
             combine( bos[ i ] );
     }
     
     /**
      * {@inheritDoc}
      */
     public void combine( float px, float py, float pz )
     {
         combineWithPoint( px, py, pz );
     }
     
     /**
      * {@inheritDoc}
      */
     public void combine( Point3f point )
     {
         combine( point.x, point.y, point.z );
     }
     
     /**
      * {@inheritDoc}
      */
     public void combine( Point3f[] points )
     {
         for ( int i = 0; i < points.length; i++ )
         {
             combine( points[ i ] );
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString()
     {
         return( "Sphere [ center: " + getCenter() + ", radius: " + getRadius() + " ]" );
     }
     
     /**
      * Creates a BoundingSphere.
      */
     public Sphere( float x, float y, float z, float radius )
     {
         super();
         
         this.center = new Point3f( x, y, z );
         this.radius = radius;
         this.radius_sq = radius * radius;
     }
     
     /**
      * Creates a BoundingSphere.
      */
     public Sphere( Tuple3f center, float radius )
     {
         this( center.x, center.y, center.z, radius );
     }
     
     /**
      * Creates a BoundingSphere.
      */
     public Sphere()
     {
         this( 0f, 0f, 0f, 0f );
     }
 }
