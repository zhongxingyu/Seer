 /*
  * Copyright 2011, Erik Lund
  *
  * This file is part of Voxicity.
  *
  *  Voxicity is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Voxicity is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Voxicity.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package voxicity;
 
 import org.lwjgl.util.vector.Vector3f;
 
 public class Frustum
 {
 	enum Side
 	{
 		top,
 		bottom,
 		left,
 		right,
 		near,
 		far,
 	}
 
 	public enum Status
 	{
 		outside,
 		partial,
 		inside,
 	}
 
 	private class Plane
 	{
 		Vector3f normal;
 		Vector3f point;
 		float d;
 
 		Plane( Vector3f normal, Vector3f point )
 		{
 			this.normal = new Vector3f( normal );
 			this.point = new Vector3f( point );
 			this.d = -Vector3f.dot( normal, point );
 		}
 
 		Plane( Vector3f p1, Vector3f p2, Vector3f p3 )
 		{
 			Vector3f dir1 = Vector3f.sub( p1, p2, null );
 			Vector3f dir2 = Vector3f.sub( p3, p2, null );
 
 			this.normal = Vector3f.cross( dir1, dir2, null );
 			this.normal = this.normal.normalise( null );
 
 			this.point = new Vector3f( p2 );
 			this.d = -Vector3f.dot( this.normal, this.point );
 		}
 
 		public float distance( Vector3f point )
 		{
 			return d + Vector3f.dot( normal, point );
 		}
 
 		public String toString()
 		{
 			return "Plane normal: " + normal.toString() + " Plane point: " + point.toString();
 		}
 	}
 
 	Vector3f pos = new Vector3f();
 	Vector3f look, right, up;
 
 	Plane[] planes = new Plane[6];
 
 	float ratio;
 	float vert_angle_rads;
 	float near_dist;
 	float far_dist;
 
 	float tan_vert_angle;
 	float near_width, near_height;
 	float far_width, far_height;
 
 	Vector3f near_top_left, near_top_right, near_bottom_left, near_bottom_right;
 	Vector3f far_top_left, far_top_right, far_bottom_left, far_bottom_right;
 
 	void set_attribs( float vert_angle, float ratio, float near_dist, float far_dist )
 	{
 		this.ratio = ratio;
 		this.vert_angle_rads = (float)Math.toRadians( vert_angle );
 		this.near_dist = near_dist;
 		this.far_dist = far_dist;
 
 		this.tan_vert_angle = (float)Math.tan( this.vert_angle_rads );
 
 		this.near_height = near_dist * tan_vert_angle;
 		this.near_width = near_height * ratio;
 
 		this.far_height = far_dist * tan_vert_angle;
 		this.far_width = far_height * ratio;
 	}
 
 	void set_pos( Vector3f pos, Vector3f look, Vector3f up )
 	{
 		this.pos.set( pos );
 
 		this.look = Vector3f.sub( look, pos, null );
 		this.look = this.look.normalise( null );
 
 		this.right = Vector3f.cross( this.look, up, null );
 		this.right = this.right.normalise( null );
 
 		this.up = Vector3f.cross( this.right, this.look, null );
 
 		Vector3f near_center = new Vector3f( this.look );
 		near_center.scale( near_dist );
 
 		Vector3f far_center = new Vector3f( this.look );
 		far_center.scale( far_dist );
 
 /*
 		Vector3f temp1 = new Vector3f( this.up );
 		Vector3f temp2 = new Vector3f( this.right );
 		temp1.scale( near_height );
 		temp2.scale( near_width );
 		temp1 = Vector3f.sub( temp1, temp2, null );
 		near_top_left = Vector3f.add( near_center, temp1, null );
 
 		temp1.set( this.up );
 		temp2.set( this.right );
 		temp1.scale( near_height );
 		temp2.scale( near_width );
 		temp1 = Vector3f.add( temp1, temp2, null );
 		near_top_right = Vector3f.add( near_center, temp1, null );
  
 		temp1.set( this.up );
 		temp2.set( this.right );
 		temp1.scale( near_height );
 		temp2.scale( near_width );
 		temp1 = Vector3f.sub( temp1, temp2, null );
 		near_bottom_left = Vector3f.sub( near_center, temp1, null );
  
 		temp1.set( this.up );
 		temp2.set( this.right );
 		temp1.scale( near_height );
 		temp2.scale( near_width );
 		temp1 = Vector3f.add( temp1, temp2, null );
 		near_bottom_right = Vector3f.sub( far_center, temp1, null );
 
 		temp1.set( this.up );
 		temp2.set( this.right );
 		temp1.scale( far_height );
 		temp2.scale( far_width );
 		temp1 = Vector3f.sub( temp1, temp2, null );
 		far_top_left = Vector3f.add( far_center, temp1, null );
 
 		temp1.set( this.up );
 		temp2.set( this.right );
 		temp1.scale( far_height );
 		temp2.scale( far_width );
 		temp1 = Vector3f.add( temp1, temp2, null );
 		far_top_right = Vector3f.add( far_center, temp1, null );
 
 		temp1.set( this.up );
 		temp2.set( this.right );
 		temp1.scale( far_height );
 		temp2.scale( far_width );
 		temp1 = Vector3f.sub( temp1, temp2, null );
 		far_bottom_left = Vector3f.sub( far_center, temp1, null );
 
 		temp1.set( this.up );
 		temp2.set( this.right );
 		temp1.scale( far_height );
 		temp2.scale( far_width );
 		temp1 = Vector3f.add( temp1, temp2, null );
 		far_bottom_right = Vector3f.sub( far_center, temp1, null );
 
 		planes[Side.near.ordinal()] = new Plane( look, near_center );
 		planes[Side.far.ordinal()] = new Plane( look.negate( null), far_center );
 */
 	}
 
 	public boolean contains_point( Vector3f point )
 	{
 		Vector3f v = Vector3f.sub( point, pos, null );
 
 
 		float x_dot = Vector3f.dot( v, right );
 		float y_dot = Vector3f.dot( v, up );
 		float z_dot = Vector3f.dot( v, look );
 
 
 		if ( z_dot > far_dist || z_dot < near_dist )
 			return false;
 
 		float y_extreme = z_dot * tan_vert_angle;
 		if ( y_dot > y_extreme || y_dot < -y_extreme )
 			return false;
 
 		float x_extreme = y_extreme * ratio;
 		if ( x_dot > x_extreme || x_dot < -x_extreme )
 			return false;
 
 		return true;
 	}
 
 
 	// Check if an AABB collides with this frustum by checking all the points
 	// in turn against each set of planes
 	public boolean collides( AABB box )
 	{
 		// Create an array of all the points, their x_dot, y_dot and z_dot values
 		Vector3f[] points = { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
 		float[][] dots = new float[8][3];
 
 		points[0] = Vector3f.sub( new Vector3f( box.left(), box.top(), box.front() ), pos, null );
 		points[1] = Vector3f.sub( new Vector3f( box.right(), box.top(), box.front() ), pos, null );
 		points[2] = Vector3f.sub( new Vector3f( box.left(), box.bottom(), box.front() ), pos, null );
 		points[3] = Vector3f.sub( new Vector3f( box.right(), box.bottom(), box.front() ), pos, null );
 		points[4] = Vector3f.sub( new Vector3f( box.left(), box.top(), box.back() ), pos, null );
 		points[5] = Vector3f.sub( new Vector3f( box.right(), box.top(), box.back() ), pos, null );
 		points[6] = Vector3f.sub( new Vector3f( box.left(), box.bottom(), box.back() ), pos, null );
 		points[7] = Vector3f.sub( new Vector3f( box.right(), box.bottom(), box.back() ), pos, null );
 
 		for ( int i = 0 ; i < 8 ; i++ )
 		{
 			dots[i][0] = Vector3f.dot( points[i], right );
 			dots[i][1] = Vector3f.dot( points[i], up );
 			dots[i][2] = Vector3f.dot( points[i], look );
 		}
 
 		boolean z_check = false;
 
 		for ( int i = 0 ; i < 8 ; i++ )
			z_check |= ( dots[i][2] < far_dist );

		if ( !z_check )
			return false;

		for ( int i = 0 ; i < 8 ; i++ )
			z_check |= ( dots[i][2] > near_dist );
 
 		if ( !z_check )
 			return false;
 
 		boolean y_check = false;
 
 		for ( int i = 0 ; i < 8 ; i++ )
 		{
 			float y_extreme = dots[i][2] * tan_vert_angle;
			y_check |= ( dots[i][1] < y_extreme );
		}

		if ( !y_check )
			return false;

		for ( int i = 0 ; i < 8 ; i++ )
		{
			float y_extreme = dots[i][2] * tan_vert_angle;
			y_check |= ( dots[i][1] > -y_extreme );
 		}
 
 		if ( !y_check )
 			return false;
 
 		boolean x_check = false;
 
 		for ( int i = 0 ; i < 8 ; i++ )
 		{
 			float x_extreme = ( dots[i][2] * tan_vert_angle ) * ratio;
			x_check |= ( dots[i][0] < x_extreme );
		}

		if ( !x_check )
			return false;

		for ( int i = 0 ; i < 8 ; i++ )
		{
			float x_extreme = ( dots[i][2] * tan_vert_angle ) * ratio;
			x_check |= ( dots[i][0] > -x_extreme );
 		}
 
 		if ( !x_check )
 			return false;
 
 		return true;
 	}
 
 	public boolean collides_plane_check( AABB box )
 	{
 		boolean collides = true;
 
 		int out, in;
 
 		for ( int i = 0 ; i < 6 ; i++ )
 		{
 			out = 0;
 			in = 0;
 
 			for ( int j = 0 ; j < 8 && ( in == 0 || out == 0 ) ; j++ )
 			{
 				if ( planes[i].distance( box.get_vert( j ) ) < 0 )
 					out++;
 				else
 					in++;
 			}
 
 			if ( in == 0 )
 				return false;
 		}
 
 		return collides;
 	}
 
 }
