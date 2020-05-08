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
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL13;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.opengl.GLContext;
 
 public class Voxicity
 {
 	static void init( Arguments args )
 	{
 		// Load the specified config file
 		Config config = new Config( args.get_value( "config", "voxicity.properties" ) );
 
 		String mode = args.get_value( "mode", "client" );
 
 		if ( mode.equals( "server" ) )
 		{
 			// Start the server, it spawns its own thread
 			// and takes over from here
 			new Server( config ).run();
 		}
 		else if ( mode.equals( "client" ) )
 		{
 			try
 			{
 				System.out.println( "Intializing display" );
 				Display.setDisplayMode( new DisplayMode( 1200, 720 ) );
 				Display.create();
 				Display.setTitle( "Voxicity" );
 				System.out.println( "Display created" );
 			}
 			catch ( LWJGLException e )
 			{
 				System.out.println( "Unable to open Display" );
 				e.printStackTrace();
 				System.exit(1);
 			}
 
 			System.out.println( "Setting up OpenGL states" );
 			GL11.glShadeModel( GL11.GL_SMOOTH );
 			GL11.glEnable( GL11.GL_DEPTH_TEST );
 			GL11.glEnable( GL11.GL_TEXTURE_2D );
 			GL11.glEnable( GL11.GL_CULL_FACE );
 			GL11.glEnable( GL11.GL_BLEND );
 			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 			GL11.glClearColor( 126.0f / 255.0f, 169.0f / 255.0f, 254.0f / 255.0f, 1.0f );
 			GL11.glEnableClientState( GL11.GL_VERTEX_ARRAY );
 			GL11.glEnableClientState( GL11.GL_TEXTURE_COORD_ARRAY );
 
 			System.out.println( "Number of texture units: " + GL11.glGetInteger( GL13.GL_MAX_TEXTURE_UNITS ) );
 			System.out.println( "Number of image texture units: " + GL11.glGetInteger( GL20.GL_MAX_TEXTURE_IMAGE_UNITS ) );
 			System.out.println( "Number of vertex texture units: " + GL11.glGetInteger( GL20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS ) );
 			System.out.println( "Number of combined vertex/image texture units: " + GL11.glGetInteger( GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS ) );
 
 			try
 			{
 				LWJGLRenderer gui_renderer = new LWJGLRenderer();
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
 
 				Socket client_s = new Socket( login_gui.get_server_name(), 11000 );
 				Client client = new Client( config, new NetworkConnection( client_s ) );
 				client.run();
 
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
 
 			init( cmd_args );
 		}
 		catch ( Exception e )
 		{
 			System.out.println( e );
 			e.printStackTrace();
 			print_usage();
 		}
 	}
 }
