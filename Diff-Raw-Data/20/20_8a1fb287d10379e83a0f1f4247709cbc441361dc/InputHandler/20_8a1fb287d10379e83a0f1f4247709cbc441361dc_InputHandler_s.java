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
 
 import java.util.List;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.util.vector.Vector3f;
 
 public class InputHandler
 {
 	Client client;
 	Player player;
 	World world;
 
 	long last_block_change;
 	long last_frame = 0;
 
 	float yaw;
 	float pitch;
 
 	float mouse_speed = 2.0f;
 	float camera_offset = 0.75f;
 
 	Vector3f place_loc = new Vector3f( 0, 0, 0 );
 
 	public InputHandler( Client client )
 	{
 		this.client = client;
 		this.player = client.player;
 		this.world = client.world;
 		get_time_delta();
 	}
 
 	void init()
 	{
 		Mouse.setGrabbed( true );
 	}
 
 	void update( World world )
 	{
 		// Get the number of seconds as a float since the last update
 		float delta = get_time_delta() / 1000.0f;
 
 		// Get the input state
 		InputState in_state = new InputState();
 
 		if ( in_state.quit )
 			client.quit();
 
 		if ( in_state.toggle_mouse )
 			toggle_mouse_grab();
 
 		if ( in_state.toggle_flying )
 			set_flying( !player.flying );
 
 		update_movement( delta, in_state );
 		handle_collisions( delta );
 
 		if ( in_state.use_action )
 			place_block();
 
 		if ( in_state.hit_action )
 			remove_block();
 	}
 
 	void update_movement( float delta, InputState in_state )
 	{
 		// Set friction factor
 		float friction = 0.99f;
 
 		//Store the last_position
 		player.last_pos.set( player.pos );
 
 		if ( Mouse.isGrabbed() )
 		{
 			int x_delta = in_state.x_delta;
 			int y_delta = in_state.y_delta;
 
 			Vector3f move = new Vector3f();
 
 			if ( in_state.move_left )
 			{
 				if ( player.flying )
 					move.x -= 80;
 				else
 				{
 					if ( player.jumping )
 						move.x -= 35;
 					else
 						move.x -= 60;
 				}
 			}
 
 			if ( in_state.move_right )
 			{
 				if ( player.flying )
 					move.x += 80;
 				else
 				{
 					if ( player.jumping )
 						move.x += 35;
 					else
 						move.x += 60;
 				}
 			}
 
 			if ( in_state.move_forward )
 			{
 				if ( player.flying )
 					move.z -= 80;
 				else
 				{
 					if ( player.jumping )
 						move.z -= 35;
 					else
 						move.z -= 60;
 				}
 			}
 
 			if ( in_state.move_backward )
 			{
 				if ( player.flying )
 					move.z += 80;
 				else
 				{
 					if ( player.jumping )
 						move.z += 35;
 					else
 						move.z += 60;
 				}
 			}
 
 			if ( player.flying )
 			{
 				if ( in_state.ascend )
 					move.y += 80;
 
 				if ( in_state.descend )
 					move.y -= 80;
 			}
 			else
 			{
 				if ( in_state.ascend && !player.jumping )
 				{
 					player.velocity.y = 10.0f;
 					player.jumping = true;
 				}
 
 				if ( player.jumping )
 					move.y = -30f;
 			}
 
 			yaw += ( x_delta / 800.0f ) * 45.0f * mouse_speed;
 			pitch += ( y_delta / 800.0f ) * 45.0f * mouse_speed;
 
 			// Make sure spinning idiots don't get ludicrously high rotations
 			yaw = yaw > 361.0f ? yaw - 360.0f : yaw;
 			yaw = yaw < -361.0 ? yaw + 360.0f : yaw;
 
 			// Avoid NaN in the frustum calculations
 			pitch = Math.min( pitch, 89.9999f );
 			pitch = Math.max( pitch, -89.9999f );
 
 			float cos_yaw = ( float ) Math.cos( Math.toRadians( yaw ) );
 			float sin_yaw = ( float ) Math.sin( Math.toRadians( yaw ) );
 			float cos_pitch = ( float ) Math.cos( Math.toRadians( pitch ) );
 			float sin_pitch = ( float ) Math.sin( Math.toRadians( pitch ) );
 
 			float corr_x = ( move.x * cos_yaw ) + ( move.z * -sin_yaw );
 			float corr_z = ( move.x * sin_yaw ) + ( move.z * cos_yaw );
 
 			player.accel.x = corr_x;
 			player.accel.y = move.y;
 			player.accel.z = corr_z;
 
 			player.velocity.x += player.accel.x * delta; 
 			player.velocity.y += player.accel.y * delta;
 			player.velocity.z += player.accel.z * delta;
 
 			if ( player.velocity.x != 0.0f || player.velocity.y != 0.0f || player.velocity.z != 0.0f )
 			{
 				Vector3f friction_vec = new Vector3f( player.velocity );
 				friction_vec = friction_vec.normalise( null );
 				friction_vec.negate();
 				friction_vec.scale( 30 * friction * delta );
 
 				if ( player.velocity.lengthSquared() < friction_vec.lengthSquared() )
 				{
 					player.velocity.x = 0;
 					player.velocity.z = 0;
 
 					if ( player.flying )
 						player.velocity.y = 0;
 				}
 				else
 				{
 					if ( !Float.isNaN( friction_vec.lengthSquared() ) )
 					{
 						player.velocity.x += friction_vec.x;
 						player.velocity.z += friction_vec.z;
 
 						if ( player.flying )
 							player.velocity.y += friction_vec.y;
 					}
 				}
 			}
 
 			if ( player.velocity.lengthSquared() < 0.001 )
 			{
 				player.velocity.x = 0;
 				player.velocity.z = 0;
 
 				if ( player.flying )
 					player.velocity.y = 0;
 			}
 
 			if ( player.flying )
 			{
 				if ( player.velocity.lengthSquared() > 100.0f )
 				{
 					float ratio = 10.0f / player.velocity.length();
 					player.velocity.scale( ratio );
 				}
 			}
 			else
 			{
 				Vector3f horiz_vel = new Vector3f( player.velocity.x, 0, player.velocity.z );
 				if ( horiz_vel.lengthSquared() > 25.0f )
 				{
 					float ratio = 5.0f / horiz_vel.length();
 					horiz_vel.scale( ratio );
 					player.velocity.x = horiz_vel.x;
 					player.velocity.z = horiz_vel.z;
 				}
 			}
 
 			// Set the look vector
 			player.look.set( sin_yaw * cos_pitch * 4, sin_pitch * 4, cos_yaw * cos_pitch * -4 );
 		}
 	}
 
 	void toggle_mouse_grab()
 	{
 		Mouse.setGrabbed( !Mouse.isGrabbed() );
 	}
 
 	void set_flying( boolean fly )
 	{
 		if ( fly )
 		{
 			player.flying = true;
 			player.jumping = false;
 			player.accel.y = 0;
 			player.velocity.y = 0;
 			System.out.println( "Flying is " + player.flying + " Jumping is " + player.jumping );
 		}
 		else
 		{
 			player.flying = false;
 			player.jumping = true;
 			System.out.println( "Flying is " + player.flying + " Jumping is " + player.jumping );
 		}
 	}
 
 	boolean can_change_block()
 	{
 		return Time.get_time_ms() - last_block_change > ( 1000 / 5 );
 	}
 
 	void place_block()
 	{
 		if ( can_change_block() )
 			last_block_change = Time.get_time_ms();
 		else
 			return;
 
 		int id = world.get_block( place_loc.x, place_loc.y, place_loc.z );
 		if ( id == Constants.Blocks.air )
 		{
 			client.tell_use_action( new BlockLoc( place_loc.x, place_loc.y, place_loc.z, world ), Direction.None );
 		}
 		else
 		{
 			Direction collision_side = world.get_hit_box( Math.round(place_loc.x), Math.round(place_loc.y), Math.round(place_loc.z) ).collision_side( new Vector3f( player.pos.x, player.pos.y + camera_offset, player.pos.z ), player.look );
 
 			client.tell_use_action( new BlockLoc( place_loc.x, place_loc.y, place_loc.z, world ), collision_side );
 		}
 	}
 
 	void remove_block()
 	{
 		if ( can_change_block() )
 			last_block_change = Time.get_time_ms();
 		else
 			return;
 
 		client.tell_hit_action( Math.round( place_loc.x ), Math.round( place_loc.y ), Math.round( place_loc.z ) );
 	}
 
 	void update_camera()
 	{
 		Vector3f pos = new Vector3f( player.pos );
 		pos.y += camera_offset;
 
 		Vector3f look = Vector3f.add( pos, player.look, null );
 
 		Vector3f up = new Vector3f( 0, 1, 0 );
 
 		client.renderer.camera.set_pos( pos, look, up );
 	}
 
 	Vector3f calc_place_loc()
 	{
 		float nearest_distance = Float.POSITIVE_INFINITY;
 		BlockLoc nearest_block = new BlockLoc( Math.round( player.pos.x + player.look.x ), Math.round( player.pos.y + camera_offset + player.look.y ), Math.round( player.pos.z + player.look.z ), world );
 
 		int x_incr = (int)Math.signum( player.look.x );
 		int y_incr = (int)Math.signum( player.look.y );
 		int z_incr = (int)Math.signum( player.look.z );
 
 		for ( int x = Math.round( player.pos.x ) ; x != Math.round( player.pos.x + player.look.x ) + x_incr ; x += x_incr)
 			for ( int y = Math.round( player.pos.y + camera_offset ) ; y != Math.round( player.pos.y + camera_offset + player.look.y ) + y_incr ; y += y_incr )
 				for ( int z = Math.round( player.pos.z ) ; z != Math.round( player.pos.z + player.look.z ) + z_incr ; z += z_incr )
 				{
 					AABB box = world.get_hit_box( x, y, z );
 
 					if ( box != null )
 					{
 						Float distance = box.collision_distance( new Vector3f( player.pos.x, player.pos.y + camera_offset, player.pos.z ), player.look );
 						
 						if ( distance < nearest_distance )
 						{
 							nearest_distance = distance;
 							nearest_block.x = x;
 							nearest_block.y = y;
 							nearest_block.z = z;
 						}
 					}
 				}
 
 		return new Vector3f( nearest_block.x, nearest_block.y, nearest_block.z );
 	}
 
 	int get_time_delta()
 	{
 		// Get the time in milliseconds
 		long new_time = Time.get_time_ms();
 		int delta = (int) ( new_time - last_frame );
 		last_frame = new_time;
 		return delta;
 	}
 
 	// Handle the player collisions by moving the player out of solid volumes
 	// and disabling jumping if needed
 	void handle_collisions( float delta )
 	{
 		// Epsilon used for small differences
 		float epsilon = 0.0001f;
 
 		// Copy the player velocity and scale it to the movement in this update
 		Vector3f velocity = new Vector3f( player.velocity );
 		velocity.scale( delta );
 
 		// Create two bounding boxes, one at the start and one at the end of the path
 		AABB p = new AABB( 0.5f, 1.75f, 0.5f );
 		p.center_on( player.last_pos );
 		AABB q = new AABB( p );
 		q.translate( velocity );
 
 		// Find all the volumes possibly intersected by this path by combining one large bounding box
 		List<AABB> boxes = WorldUtil.get_intersecting_volumes( world, new AABB( p, q ) );
 
 		// Translate the bounding volume as corrections are found in y, x, and z axes
 		float y = correct_y_velocity( boxes, p, velocity.y );
 		p.translate( 0, y, 0 );
 		float x = correct_x_velocity( boxes, p, velocity.x );
 		p.translate( x, 0, 0 );
 		float z = correct_z_velocity( boxes, p, velocity.z );
 		p.translate( 0, 0, z );
 
 //		Vector3f.add( player.last_pos, new Vector3f( x, y, z ), player.pos );a
 		player.pos.set( p.position() );
 		//p.center_on( player.pos );
 
 		// If collisions happen in any direction, set that velocity to 0
 		if ( x != velocity.x )
 			player.velocity.x = 0f;
 		if ( y != velocity.y )
 			player.velocity.y = 0f;
 		if ( z != velocity.z )
 			player.velocity.z = 0;
 
 		// Create a new box to test for solid volumes under the player
 		AABB standing_box = new AABB( p );
 		// Translate the standing box slightly down to check for surfaces to
 		// stand on
 		standing_box.translate( 0, -epsilon, 0 );
 
 		// Check if the player is standing on anything after having set y movement to 0
 		// Disable jumping if so
 		for ( AABB box : WorldUtil.get_intersecting_volumes( world, standing_box ) )
 			if ( standing_box.intersects( box ) && y == 0f )
 				player.jumping = false;
 	}
 
 	// Correct X-axis velocity to the nearest box that collides
 	// with the YZ-corridor in the direction of travel
 	float correct_x_velocity( List<AABB> boxes, AABB box, float dist )
 	{
 		for ( AABB hit : boxes )
 		{
 			if ( !box.intersects_yz( hit ) )
 				continue;
 
 			if ( dist < 0 && box.min_x() >= hit.max_x() )
 			{
 				float delta = hit.max_x() - box.min_x();
 				dist = Math.max( dist, delta );
 			}
 			else if ( dist > 0 && box.max_x() <= hit.min_x() )
 			{
 				float delta = hit.min_x() - box.max_x();
 				dist = Math.min( dist, delta );
 			}
 		}
 
 		return dist;
 	}
 
 	// Correct Y-axis velocity to the nearest box that collides
 	// with the XZ-corridor in the direction of travel
 	float correct_y_velocity( List<AABB> boxes, AABB box, float y_delta )
 	{
 		for ( AABB hit : boxes )
 		{
 			if ( !box.intersects_xz( hit ) )
 				continue;
 
 			// Check if we're descending on to the box
 			if ( y_delta < 0 && ( box.min_y() >= hit.max_y() ) )
 			{
 				float delta = hit.max_y() - box.min_y();
 				y_delta = Math.max( y_delta, delta );
 			}
 			else if ( y_delta > 0 && box.max_y() <= hit.min_y() )
 			{
 				float delta = hit.min_y() - box.max_y();
 				y_delta = Math.min( y_delta, delta );
 			}
 		}
 
 		return y_delta;
 	}
 
 	// Correct Z-axis velocity to the nearest box that collides
 	// with the XY-corridor in the direction of travel
 	float correct_z_velocity( List<AABB> boxes, AABB box, float z_delta )
 	{
 		for ( AABB hit : boxes )
 		{
 			if ( !box.intersects_xy( hit ) )
 				continue;
 
 			if ( z_delta < 0 && box.min_z() >= hit.max_z() )
 			{
 				float delta = hit.max_z() - box.min_z();
 				z_delta = Math.max( z_delta, delta );
 			}
 			else if ( z_delta > 0 && box.max_z() <= hit.min_z() )
 			{
 				float delta = hit.min_z() - box.max_z();
 				z_delta = Math.min( z_delta, delta );
 			}
 		}
 
 		return z_delta;
 	}
 }
