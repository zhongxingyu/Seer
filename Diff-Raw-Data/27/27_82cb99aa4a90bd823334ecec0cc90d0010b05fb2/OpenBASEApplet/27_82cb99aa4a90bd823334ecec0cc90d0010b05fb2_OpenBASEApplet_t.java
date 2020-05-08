 /*
  * This file is part of OpenBASE.
  *
  * Copyright © 2012, Kyle Repinski
  * OpenBASE is licensed under the GNU Lesser General Public License.
  *
  * OpenBASE is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OpenBASE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package mwisbest.openbase;
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import mwisbest.openbase.opengl.RenderPriority;
 import mwisbest.openbase.opengl.Widget;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.openal.AL;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.openal.SoundStore;
 
 public abstract class OpenBASEApplet extends Applet
 {
 	private static final long serialVersionUID = -3837899243800598866L;
 	private int canvasWidth = 640;
 	private int canvasHeight = 360;
 	@SuppressWarnings( "unused" )
 	private String canvasTitle = "OpenBASE";
 	@SuppressWarnings( "unused" )
 	private String canvasIcon16Loc = "GLicon16.png";
 	@SuppressWarnings( "unused" )
 	private String canvasIcon32Loc = "GLicon32.png";
 	@SuppressWarnings( "unused" )
 	private boolean canvasVSync = false;
 	private Canvas displayParent = null;
 	private Thread theThread = null;
 	private boolean running = false;
 	
 	public OpenBASEApplet()
 	{
 		this( 640, 360, "OpenBASE", "GLicon16.png", "GLicon32.png", false );
 	}
 	
 	public OpenBASEApplet( int width, int height )
 	{
 		this( width, height, "OpenBASE", "GLicon16.png", "GLicon32.png", false );
 	}
 	
 	public OpenBASEApplet( int width, int height, boolean vSync )
 	{
 		this( width, height, "OpenBASE", "GLicon16.png", "GLicon32.png", vSync );
 	}
 	
 	public OpenBASEApplet( int width, int height, String title )
 	{
 		this( width, height, title, "GLicon16.png", "GLicon32.png", false );
 	}
 	
 	public OpenBASEApplet( int width, int height, String title, boolean vSync )
 	{
 		this( width, height, title, "GLicon16.png", "GLicon32.png", vSync );
 	}
 	
 	public OpenBASEApplet( int width, int height, String icon16Loc, String icon32Loc )
 	{
 		this( width, height, "OpenBASE", icon16Loc, icon32Loc, false );
 	}
 	
 	public OpenBASEApplet( int width, int height, String icon16Loc, String icon32Loc, boolean vSync )
 	{
 		this( width, height, "OpenBASE", icon16Loc, icon32Loc, vSync );
 	}
 	
 	public OpenBASEApplet( int width, int height, String title, String icon16Loc, String icon32Loc )
 	{
 		this( width, height, title, icon16Loc, icon32Loc, false );
 	}
 	
 	public OpenBASEApplet( int width, int height, String title, String icon16Loc, String icon32Loc, boolean vSync )
 	{
 		this.canvasWidth = width;
 		this.canvasHeight = height;
 		this.canvasTitle = title;
 		this.canvasIcon16Loc = icon16Loc;
 		this.canvasIcon32Loc = icon32Loc;
 		this.canvasVSync = vSync;
 	}
 	
 	public void startLWJGL()
 	{
 		theThread = new Thread()
 			{
 				@Override
 				public void run()
 				{
 					running = true;
 					try
 					{
 						Display.setParent( displayParent );
 						Display.create();
 						
 						GL11.glEnable( GL11.GL_TEXTURE_2D );
 						GL11.glDisable( GL11.GL_DEPTH_TEST );
 						GL11.glMatrixMode( GL11.GL_PROJECTION );
 						GL11.glLoadIdentity();
 						GL11.glOrtho( 0, canvasWidth, canvasHeight, 0, 1, -1 );
 					}
 					catch( LWJGLException e )
 					{
 						e.printStackTrace();
 						System.exit( 0 );
 					}
 					loadResources();
 					mainLoop();
 				}
 			};
 		theThread.start();
 	}
 	
 	public void stopLWJGL()
 	{
 		running = false;
 		try
 		{
 			theThread.join();
 		}
 		catch( InterruptedException e )
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void start()
 	{
 	}
 	
 	@Override
 	public void stop()
 	{
 	}
 	
 	@Override
 	public void destroy()
 	{
 		remove( displayParent );
 		super.destroy();
 	}
 	
 	@Override
 	public void init()
 	{
 		setLayout( new BorderLayout() );
 		try
 		{
 			displayParent = new Canvas()
 				{
 					private static final long serialVersionUID = 2601628115991718459L;
					
 					@Override
 					public final void addNotify()
 					{
 						super.addNotify();
 						startLWJGL();
 					}
 					
 					@Override
 					public final void removeNotify()
 					{
 						stopLWJGL();
 						super.removeNotify();
 					}
 				};
 			displayParent.setSize( this.canvasWidth, this.canvasHeight );
 			add( displayParent );
 			displayParent.setFocusable( true );
 			displayParent.requestFocus();
 			displayParent.setIgnoreRepaint( true );
 			setVisible( true );
 		}
 		catch( Exception e )
 		{
 			System.err.println( e );
 			throw new RuntimeException( "Unable to create the display!" );
 		}
 	}
 	
 	public void mainLoop()
 	{
 		while( running )
 		{
 			input();
 			render();
 			audio();
 		}
 		Display.destroy();
 		AL.destroy();
 	}
 	
 	public void render()
 	{
 		GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );
 		GL11.glMatrixMode( GL11.GL_MODELVIEW );
 		GL11.glLoadIdentity();
 		Map<String, Widget> theWidgets = ResourceManager.getWidgets();
 		RenderPriority[] rvalues = RenderPriority.values();
 		for( RenderPriority priority : rvalues )
 		{
 			for( Entry<String, Widget> widget : theWidgets.entrySet() )
 			{
 				if( widget.getValue().getRenderPriority() == priority && widget.getValue().getVisible() ) widget.getValue().render();
 			}
 		}
 		
 		customRender();
 		
 		Display.update();
 		
 		if( Display.isCloseRequested() ) this.running = false;
 	}
 	
 	public void audio()
 	{
 		SoundStore.get().poll( 0 );
 		customAudio();
 	}
 	
 	public void input()
 	{
 		while( Mouse.next() )
 		{
 		}
 		while( Keyboard.next() )
 		{
 		}
 		customInput();
 	}
 	
 	public abstract void loadResources();
 	
 	public abstract void customRender();
 	
 	public abstract void customAudio();
 	
 	public abstract void customInput();
 }
