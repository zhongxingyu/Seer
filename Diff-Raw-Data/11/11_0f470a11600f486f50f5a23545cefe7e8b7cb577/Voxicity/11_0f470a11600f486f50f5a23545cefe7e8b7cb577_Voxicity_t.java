 package voxicity;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.glu.GLU;
 
 public class Voxicity
 {
	// Set the library path for lwjgl
	static
	{
		System.setProperty( "java.library.path", "native" );
	}

 	float rot = 0;
 
 	public void init()
 	{
 		try
 		{
 			Display.setDisplayMode( new DisplayMode( 800, 600 ) );
 			Display.create();
 		}
 		catch ( LWJGLException e )
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 //		GL11.glOrtho(0, 800, 600, 0, 1, -1);
 		GLU.gluPerspective( 45.0f, 1.333f, 1f, 1000f );
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 
 		GL11.glShadeModel( GL11.GL_SMOOTH );
 		GL11.glEnable( GL11.GL_DEPTH_TEST );
 
 		while ( !Display.isCloseRequested() )
 		{
			rot += 0.5;
 
 			GL11.glLoadIdentity();
			GLU.gluLookAt( 0, 50, 0, 0,0, -90, 0,1,0 );
 			GL11.glTranslatef( 0, 0, -90 );
 			GL11.glRotatef( rot, 0, 1, 1 );
 			GL11.glTranslatef( 0, 0, 90 );
 
 			// Clear the screen and depth buffer
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);	
 
 			// set the color of the quad (R,G,B,A)
 			GL11.glColor3f(0.5f,0.5f,1.0f);
 
 /*			// draw quad
 			GL11.glBegin(GL11.GL_QUADS);
 			   GL11.glVertex2f(100,100);
 			   GL11.glVertex2f(100+200,100);
 			   GL11.glVertex2f(100+200,100+200);
 			   GL11.glVertex2f(100,100+200);
 			GL11.glEnd();
 */
 		GL11.glColor3f( 1.0f, 0.0f, 0.0f );
 		GL11.glBegin(GL11.GL_QUADS);
 		   // Front
 		   GL11.glVertex3f( -10, -10, -90 );
 		   GL11.glColor3f( 0.0f, 1.0f, 0.0f );
 		   GL11.glVertex3f( 10, -10, -90 );
 			GL11.glColor3f(0.5f,0.5f,1.0f);
 		   GL11.glVertex3f( 10, 10, -90 );
 		   GL11.glColor3f( 0.0f, 0.0f, 1.0f );
 		   GL11.glVertex3f( -10, 10, -90 );
 
 			// left
 		  GL11.glVertex3f( -10, -10, -90 );
 			GL11.glVertex3f( -10, -10, -70 );
 			GL11.glVertex3f( -10, 10, -70 );
 			GL11.glVertex3f( -10, 10, -90 );
 
 			// right
 			GL11.glVertex3f( 10, -10, -90 );
 			GL11.glVertex3f( 10, -10, -70 );
 			GL11.glVertex3f( 10, 10, -70 );
 			GL11.glVertex3f( 10, 10, -90 );
 
 			// front
 			GL11.glColor3f( 0.5f, 1.0f, 0.0f );
 			GL11.glVertex3f( -10, -10, -70 );
 			GL11.glVertex3f( 10, -10, -70 );
 			GL11.glVertex3f( 10, 10, -70 );
 			GL11.glVertex3f( -10, 10, -70 );
 
 			// top
 			GL11.glColor3f( 1.0f, 0.5f, 0.0f );
 			GL11.glVertex3f( -10, 10, -90 );
 			GL11.glVertex3f( -10, 10, -70 );
 			GL11.glVertex3f( 10, 10, -70 );
 			GL11.glVertex3f( 10, 10, -90 );
 
 			// bottom
 			GL11.glColor3f( 0.0f, 0.5f, 1.0f );
 			GL11.glVertex3f( -10, -10, -90 );
 			GL11.glVertex3f( -10, -10, -70 );
 			GL11.glVertex3f( 10, -10, -70 );
 			GL11.glVertex3f( 10, -10, -90 );
 		GL11.glEnd();
 			Display.update();
 		}
 
 		Display.destroy();
 	}
 
 	public static void main( String[] args )
 	{
 		System.out.println( "Hello world! I'm Voxicity!" );
 
 		System.out.println( "Making a display now" );
 		Voxicity voxy = new Voxicity();
 		voxy.init();
 	}
 }
