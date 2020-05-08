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
 
 import de.matthiasmann.twl.GUI;
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import de.matthiasmann.twl.theme.ThemeManager;
 import de.matthiasmann.twl.*;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.EXTTextureArray;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL13;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.opengl.GLContext;
 import org.lwjgl.util.glu.GLU;
 import org.lwjgl.util.vector.Vector3f;
 
 public class Voxicity
 {
 
 	long last_fps_update = 0;
 	int fps_count = 0;
 
 	static int fps = 0;
 
 	long last_frame = 0;
 
 	long last_block_change = 0;
 
 	float rot_x;
 	float rot_y;
 	float mouse_speed = 2.0f;
 	float camera_offset = 0.75f;
 
 	static Vector3f camera;
 
 	Vector3f place_loc = new Vector3f( 0, 0, 0 );
 	Vector3f look_vec = new Vector3f();
 
 	boolean is_close_requested = false;
 
 	Server server;
 	Client client;
 	Config config;
 	Arguments args;
 	LWJGLRenderer gui_renderer;
 
 	public Voxicity( Arguments args )
 	{
 		this.args = args;
 	}
 
 	public void init()
 	{
 		// Load the specified config file
 		config = new Config( args.get_value( "config", "voxicity.properties" ) );
 
 		String mode = args.get_value( "mode", "client" );
 
 		if ( mode.equals( "server" ) )
 		{
 			server = new Server( config );
 			server.run();
 		}
 		else if ( mode.equals( "client" ) )
 		{
 			try
 			{
 				System.out.println( "Intializing display" );
 				Display.setDisplayMode( new DisplayMode( 1200, 720 ) );
 				Display.create();
 				System.out.println( "Display created" );
 			}
 			catch ( LWJGLException e )
 			{
 				e.printStackTrace();
 				System.exit(0);
 			}
 
 			TextRenderer.init();
 
 
 			System.out.println( "Setting up OpenGL states" );
 			GL11.glShadeModel( GL11.GL_SMOOTH );
 			GL11.glEnable( GL11.GL_DEPTH_TEST );
 			GL11.glEnable( GL11.GL_TEXTURE_2D );
 			GL11.glEnable( GL11.GL_CULL_FACE );
 			GL11.glEnable( GL11.GL_BLEND );
 			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 			GL11.glClearColor( 126.0f / 255.0f, 169.0f / 255.0f, 254.0f / 255.0f, 1.0f );
 
 			System.out.println( "Number of texture units: " + GL11.glGetInteger( GL13.GL_MAX_TEXTURE_UNITS ) );
 			System.out.println( "Number of image texture units: " + GL11.glGetInteger( GL20.GL_MAX_TEXTURE_IMAGE_UNITS ) );
 			System.out.println( "Number of vertex texture units: " + GL11.glGetInteger( GL20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS ) );
 			System.out.println( "Number of combined vertex/image texture units: " + GL11.glGetInteger( GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS ) );
 
 			try
 			{
 				gui_renderer = new LWJGLRenderer();
 				LoginGUI login_gui = new LoginGUI();
 				ThemeManager theme = ThemeManager.createThemeManager( Voxicity.class.getResource( "/login.xml" ), gui_renderer );
 				GUI gui = new GUI( login_gui, gui_renderer );
 				gui.applyTheme( theme );
 
 				while ( !login_gui.is_login_pressed() )
 				{
 					GL11.glClear( GL11.GL_COLOR_BUFFER_BIT );
 					gui.update();
 					Display.update();
 
 					if ( Display.isCloseRequested() )
 					{
 						Display.destroy();
 						System.exit( 0 );
 					}
 				}
 
 				Mouse.setGrabbed( true );
 
 				Socket client_s = new Socket( login_gui.get_server_name(), 11000 );
 				client = new Client( config, new NetworkConnection( client_s ) );
 
 				load_texture_pack();
 
 				last_fps_update = Time.get_time_ms();
 				get_time_delta();
 
 				client.init();
 				camera = client.player.pos;
 
 				setup_camera();
 
 				while ( !is_close_requested )
 				{
 					System.out.println( "Update at " + Time.get_time_µs() );
 					client.update();
 					update( get_time_delta() / 1000.0f, client.world );
 					System.out.println( "Load new chunks at " + Time.get_time_µs() );
 					System.out.println( "Render at " + Time.get_time_µs() );
 					client.renderer.render();
 					System.out.println( "Loop done" );
 
 					is_close_requested |= Display.isCloseRequested();
 				}
 
 				client.shutdown();
 				System.out.println( "Destroying display" );
 				Display.destroy();
 			}
 
 			// Catch exceptions from the game init and main game-loop
 
 			catch ( LWJGLException e )
 			{
 				System.out.println( e );
 				e.printStackTrace();
 				System.exit(1);
 			}
 			catch ( IOException e )
 			{
 				System.out.println( e );
 				e.printStackTrace();
 				System.exit(1);
 			}
 			catch ( Exception e )
 			{
 				System.out.println( e );
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			System.out.println( "Invalid mode: " + mode );
 		}
 	}
 
 	int get_time_delta()
 	{
 		// Get the time in milliseconds
 		long new_time = Time.get_time_ms();
 		int delta = (int) ( new_time - last_frame );
 		last_frame = new_time;
 		return delta;
 	}
 
 	void update( float delta, World world )
 	{
 		//Store the new last position
 		Vector3f last_pos = new Vector3f( client.player.pos );
 
 		if ( Keyboard.isKeyDown( Keyboard.KEY_ESCAPE ) )
 			is_close_requested = true;
 
 		if ( Keyboard.isKeyDown( Keyboard.KEY_Q ) )
 			is_close_requested = true;
 
 		while ( Keyboard.next() )
 		{
 			if ( Keyboard.getEventKey() == Keyboard.KEY_E && Keyboard.getEventKeyState() )
 				toggle_mouse_grab();
 
 			if ( Keyboard.getEventKey() == Keyboard.KEY_G && Keyboard.getEventKeyState() )
 			{
 				toggle_flying();
 			}
 		}
 
 		if ( Mouse.isGrabbed() )
 		{
 			float x_move = 0;
 			float z_move = 0;
 
 			if ( Keyboard.isKeyDown( Keyboard.KEY_A ) )
 				x_move -= 5;
 
 			if ( Keyboard.isKeyDown( Keyboard.KEY_D ) )
 				x_move += 5;
 
 			if ( Keyboard.isKeyDown( Keyboard.KEY_W ) )
 				z_move -= 5;
 
 			if ( Keyboard.isKeyDown( Keyboard.KEY_S ) )
 				z_move += 5;
 
 			if ( client.player.flying )
 			{
 				if ( Keyboard.isKeyDown( Keyboard.KEY_SPACE ) ) camera.y += 5 * delta;
 				if ( Keyboard.isKeyDown( Keyboard.KEY_C ) ) camera.y -= 5 * delta;
 			}
 			else
 			{
 				if ( Keyboard.isKeyDown( Keyboard.KEY_SPACE ) && !client.player.jumping )
 				{
 					client.player.velocity.y = 8.0f;
 					client.player.jumping = true;
 				}
 
 				if ( client.player.jumping )
 					client.player.accel.y = -23f;
 
 			}
 
 			if ( Mouse.isButtonDown( 0 ) )
 				place_block( world );
 
 			if ( Mouse.isButtonDown( 1 ) )
 				remove_block( world );
 
 			int x_delta = Mouse.getDX();
 			int y_delta = Mouse.getDY();
 
 			rot_x += ( x_delta / 800.0f ) * 45.0f * mouse_speed;
 			rot_y += ( y_delta / 800.0f ) * 45.0f * mouse_speed;
 
 			// Make sure spinning idiots don't get ludicrously high rotations
 			rot_x = rot_x > 361.0f ? rot_x - 360.0f : rot_x;
 			rot_x = rot_x < -361.0 ? rot_x + 360.0f : rot_x;
 
 			// Avoid NaN in the frustum calculations
 			rot_y = Math.min( rot_y, 89.9999f );
 			rot_y = Math.max( rot_y, -89.9999f );
 
 			float cos_rot_x = ( float ) Math.cos( Math.toRadians( rot_x ) );
 			float sin_rot_x = ( float ) Math.sin( Math.toRadians( rot_x ) );
 			float cos_rot_y = ( float ) Math.cos( Math.toRadians( rot_y ) );
 			float sin_rot_y = ( float ) Math.sin( Math.toRadians( rot_y ) );
 
 			float corr_x = ( x_move * cos_rot_x ) - ( z_move * sin_rot_x );
 			float corr_z = ( x_move * sin_rot_x ) + ( z_move * cos_rot_x );
 
 			client.player.accel.x = corr_x;
 			client.player.accel.z = corr_z;
 
 			client.player.velocity.x = client.player.accel.x; 
 			client.player.velocity.y += client.player.accel.y * delta;
 			client.player.velocity.z = client.player.accel.z;
 
 			camera.x += client.player.velocity.x * delta;
 			camera.y+= client.player.velocity.y * delta;
 			camera.z += client.player.velocity.z * delta;
 
 			// Set the look vector
 			look_vec.set( sin_rot_x * cos_rot_y * 4, sin_rot_y * 4, cos_rot_x * cos_rot_y * -4 );
 		}
 
 		System.out.println( "Check collisions at " + Time.get_time_µs() );
 		check_collisions( last_pos, new Vector3f( camera.x, camera.y, camera.z ), world, client.player );
 		System.out.println( "Done checking collisions at " + Time.get_time_µs() );
 
 		calc_place_loc( world );
 
 		client.renderer.camera.set_pos( new Vector3f( camera.x, camera.y + camera_offset, camera.z ), new Vector3f( camera.x + look_vec.x, camera.y + camera_offset + look_vec.y, camera.z + look_vec.z ), new Vector3f( 0, 1, 0 ) );
 
 		update_fps();
 	}
 
 	void update_fps()
 	{
 		if ( Time.get_time_ms() - last_fps_update > 250 )
 		{
 			fps = fps_count * 4;
 			fps_count = 0;
 			last_fps_update += 250;
 			Display.setTitle( "Voxicity - FPS: " + fps );
 		}
 		fps_count++;
 	}
 
 
 	void setup_camera()
 	{
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GLU.gluPerspective( 45.0f, 1200 / 720.0f, 0.1f, 10000f );
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 
 		camera.x = 0;
 		camera.y = 3;
 		camera.z = 0;
 		rot_x = 0;
 		rot_y = 0;
 
 		client.renderer.camera.set_attribs( 45.0f, 1200 / 720.0f, 0.1f, 1000.0f );
 	}
 
 	void toggle_mouse_grab()
 	{
 		Mouse.setGrabbed( !Mouse.isGrabbed() );
 	}
 
 	void toggle_flying()
 	{
 		if ( client.player.flying == false )
 		{
 			client.player.flying = true;
 			client.player.accel.y = 0;
 			client.player.velocity.y = 0;
 			System.out.println( "Flying is " + client.player.flying );
 		}
 		else
 		{
 			client.player.flying = false;
 			client.player.jumping = true;
 			System.out.println( "Flying is " + client.player.flying );
 		}
 	}
 
 	Vector3f[] check_collisions( Vector3f last_pos, Vector3f new_pos, World world, Player p )
 	{
 		int slice_num = 10;
 
 		boolean collided_x = false;
 		boolean collided_y = false;
 		boolean collided_z = false;
 
 		Vector3f[] result = { new Vector3f(), new Vector3f(), new Vector3f() };
 
 		AABB player = new AABB( 0.5f, 1.7f, 0.5f );
 
 		Vector3f slice_distance = new Vector3f();
 		Vector3f.sub( new_pos, last_pos, slice_distance );
 		slice_distance.scale( 1.0f / ( slice_num * 1.0f ) );
 
 		Vector3f slice_pos = new Vector3f( last_pos );
 		player.pos = slice_pos;
 
 		Vector3f corrected_pos = new Vector3f( new_pos );
 
 		// Check y axis for collisions
 		for ( int i = 0 ; i < slice_num ; i++ )
 		{
 			
 			Vector3f.add( slice_pos, slice_distance, slice_pos );
 
 			AABB above = world.get_hit_box( Math.round( player.pos.x ), Math.round( player.top() ), Math.round( player.pos.z ) );
 			if ( above != null )
 			{
 				if ( player.collides( above ) )
 				{
 					collided_y = true;
 					player.pos.y += above.bottom_intersect( player );
 					p.velocity.y = -p.velocity.y;
 					p.accel.y = 0;
 				}
 			}
 
 			AABB beneath = world.get_hit_box( Math.round( player.pos.x), Math.round( player.bottom() - 0.01f ), Math.round(player.pos.z) );
 			if ( beneath != null )
 			{
 				if ( player.collides( beneath ) && p.velocity.y < 0 )
 				{
 					collided_y = true;
 					player.pos.y += beneath.top_intersect( player ) + 0.0001f;
 					p.velocity.y = 0;
 					p.accel.y = 0;
 					p.jumping = false;
 				}
 			}
 			else
 			{
 				p.jumping = true;
 			}
 
 			if ( collided_y )
 				break;
 		}
 
 		corrected_pos.y = slice_pos.y;
 		slice_pos = new Vector3f( last_pos );
 		player.pos = slice_pos;
 
 		// Check x axis for collisions
 		for ( int i = 0 ; i < slice_num ; i++ )
 		{
 
 			Vector3f.add( slice_pos, slice_distance, slice_pos );
 
 			AABB upper_neg_x = world.get_hit_box( Math.round(player.left()), Math.round(player.top()), Math.round(player.pos.z) );
 			if ( upper_neg_x != null )
 			{
 				if ( player.collides( upper_neg_x ) )
 				{
 					collided_x = true;
 					p.velocity.x = 0;
 					player.pos.x += upper_neg_x.right_intersect( player ) + 0.0001f;
 				}
 			}
 
 			AABB upper_pos_x = world.get_hit_box( Math.round(player.right()), Math.round(player.top()), Math.round(player.pos.z) );
 			if ( upper_pos_x != null )
 			{
 				if ( player.collides( upper_pos_x ) )
 				{
 					collided_x = true;
 					p.velocity.x = 0;
 					player.pos.x += upper_pos_x.left_intersect( player ) - 0.0001f;
 				}
 			}
 
 			AABB lower_neg_x = world.get_hit_box( Math.round(player.left()), Math.round(player.bottom()), Math.round(player.pos.z) );
 			if ( lower_neg_x != null )
 			{
 				if ( player.collides( lower_neg_x ) )
 				{
 					collided_x = true;
 					p.velocity.x = 0;
 
 					if ( Math.abs( lower_neg_x.right_intersect( player ) ) < Math.abs( lower_neg_x.top_intersect( player ) ) )
 						player.pos.x += lower_neg_x.right_intersect( player ) + 0.0001f;
 				}
 			}
 
 			AABB lower_pos_x = world.get_hit_box( Math.round(player.right()), Math.round(player.bottom()), Math.round(player.pos.z) );
 			if ( lower_pos_x != null )
 			{
 				if ( player.collides( lower_pos_x ) )
 				{
 					collided_x = true;
 					p.velocity.x = 0;
 					if ( Math.abs( lower_pos_x.left_intersect( player ) ) < Math.abs( lower_pos_x.top_intersect( player ) ) )
 						player.pos.x += lower_pos_x.left_intersect( player ) - 0.0001f;
 				}
 			}
 
 			if ( collided_x )
 				break;
 		}
 
 		corrected_pos.x = slice_pos.x;
 		slice_pos = new Vector3f( last_pos );
 		player.pos = slice_pos;
 
 		for ( int i = 0 ; i < slice_num ; i++ )
 		{
 			Vector3f.add( slice_pos, slice_distance, slice_pos );
 
 			AABB upper_neg_z = world.get_hit_box( Math.round(player.pos.x), Math.round(player.top()), Math.round(player.back()) );
 			if ( upper_neg_z != null )
 			{
 				if ( player.collides( upper_neg_z ) )
 				{
 					collided_z = true;
 					p.velocity.z = 0;
 					player.pos.z += upper_neg_z.front_intersect( player ) + 0.0001f;
 				}
 			}
 
 			AABB upper_pos_z = world.get_hit_box( Math.round(player.pos.x), Math.round(player.top()), Math.round(player.front()) );
 			if ( upper_pos_z != null )
 			{
 				if ( player.collides( upper_pos_z ) )
 				{
 					collided_z = true;
 					p.velocity.z = 0;
 					player.pos.z += upper_pos_z.back_intersect( player ) - 0.0001f;
 				}
 			}
 
 			AABB lower_neg_z = world.get_hit_box( Math.round(player.pos.x), Math.round(player.bottom()), Math.round(player.back()) );
 			if ( lower_neg_z != null )
 			{
 				if ( player.collides( lower_neg_z ) )
 				{
 					collided_z = true;
 					p.velocity.z = 0;
 					if ( Math.abs( lower_neg_z.front_intersect( player ) ) < Math.abs( lower_neg_z.top_intersect( player ) ) )
 						player.pos.z += lower_neg_z.front_intersect( player ) + 0.0001f;
 				}
 			}
 
 			AABB lower_pos_z = world.get_hit_box( Math.round(player.pos.x), Math.round(player.bottom()), Math.round(player.front()) );
 			if ( lower_pos_z != null )
 			{
 				if ( player.collides( lower_pos_z ) )
 				{
 					collided_z = true;
 					p.velocity.z = 0;
 					if ( Math.abs( lower_pos_z.back_intersect( player ) ) < Math.abs( lower_pos_z.top_intersect( player ) ) )
 						player.pos.z += lower_pos_z.back_intersect( player ) - 0.0001f;
 				}
 			}
 
 			if ( collided_z )
 				break;
 		}
 
 		corrected_pos.z = slice_pos.z;
 
 
 		if ( collided_x || collided_y || collided_z )
 		{
 			// Set the player's new position after collision checking/handling
 			p.pos.x = corrected_pos.x;
 			p.pos.y = corrected_pos.y;
 			p.pos.z = corrected_pos.z;
 		}
 
 		return result;
 	}
 
 	void calc_place_loc( World world )
 	{
 		float nearest_distance = Float.POSITIVE_INFINITY;
 		BlockLoc nearest_block = new BlockLoc( Math.round( camera.x + look_vec.x ), Math.round( camera.y + camera_offset + look_vec.y ), Math.round( camera.z + look_vec.z ), world );
 
 		int x_incr = (int)Math.signum( look_vec.x );
 		int y_incr = (int)Math.signum( look_vec.y );
 		int z_incr = (int)Math.signum( look_vec.z );
 
 		for ( int x = Math.round( camera.x ) ; x != Math.round( camera.x + look_vec.x ) + x_incr ; x += x_incr)
 			for ( int y = Math.round( camera.y + camera_offset ) ; y != Math.round( camera.y + camera_offset + look_vec.y ) + y_incr ; y += y_incr )
 				for ( int z = Math.round( camera.z ) ; z != Math.round( camera.z + look_vec.z ) + z_incr ; z += z_incr )
 				{
 					AABB box = world.get_hit_box( x, y, z );
 
 					if ( box != null )
 					{
 						Float distance = box.collision_distance( new Vector3f( camera.x, camera.y + camera_offset, camera.z ), look_vec );
 						
 						if ( distance < nearest_distance )
 						{
 							nearest_distance = distance;
 							nearest_block.x = x;
 							nearest_block.y = y;
 							nearest_block.z = z;
 						}
 					}
 				}
 
 		place_loc.set( nearest_block.x, nearest_block.y, nearest_block.z );
 	}
 
 	boolean can_change_block()
 	{
 		return Time.get_time_ms() - last_block_change > ( 1000 / 5 );
 	}
 
 	void place_block( World world )
 	{
 		if ( can_change_block() )
 			last_block_change = Time.get_time_ms();
 		else
 			return;
 
 		int id = world.get_block( place_loc.x, place_loc.y, place_loc.z );
 		if ( id == Constants.Blocks.air )
 		{
 			client.tell_use_action( new BlockLoc( place_loc.x, place_loc.y, place_loc.z, client.world ), Constants.Direction.None );
 		}
 		else
 		{
 			Constants.Direction collision_side = world.get_hit_box( Math.round(place_loc.x), Math.round(place_loc.y), Math.round(place_loc.z) ).collision_side( new Vector3f( camera.x, camera.y + camera_offset, camera.z ), look_vec );
 
 			client.tell_use_action( new BlockLoc( place_loc.x, place_loc.y, place_loc.z, client.world ), collision_side );
 		}
 	}
 
 	void remove_block( World world )
 	{
 		if ( can_change_block() )
 			last_block_change = Time.get_time_ms();
 		else
 			return;
 
 		client.tell_hit_action( Math.round( place_loc.x ), Math.round( place_loc.y ), Math.round( place_loc.z ) );
 	}
 
 	void get_system_info()
 	{
 		
 	}
 
 	void load_texture_pack()
 	{
 		TextureManager.get_texture( "textures/stone.png" );
 		TextureManager.get_texture( "textures/dirt.png" );
 		TextureManager.get_texture( "textures/grass.png" );
 	}
 
 	static void print_usage()
 	{
 		System.out.println( "Usage: voxicity [OPTION]..." );
 		System.out.println( "The following options are valid\n" );
 		System.out.println( "  --mode <value>        Sets the mode of the program( server, client, server-client )" );
 		System.out.println( "  --config <filename>   Sets the name of the properties file to be loaded( voxicity.properties )" );
 		
 	}
 
 	public static void main( String[] args )
 	{
 		try
 		{
 			Arguments cmd_args = new Arguments( args );
 			File new_out = new File( "voxicity.log" );
 			System.setOut( new PrintStream( new_out ) );
 
 			Voxicity voxy = new Voxicity( cmd_args );
 			voxy.init();
 		}
 		catch ( Exception e )
 		{
 			System.out.println( e );
 			e.printStackTrace();
 			print_usage();
 		}
 	}
 }
