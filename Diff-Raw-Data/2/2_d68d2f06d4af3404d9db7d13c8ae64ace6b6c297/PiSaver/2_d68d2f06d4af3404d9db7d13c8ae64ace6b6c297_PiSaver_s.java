 package net.marcuswatkins.pisaver;
 
 //Original copyright header from jogl demo:
 
 /**
  * Copyright 2012 JogAmp Community. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and should not be interpreted as representing official policies, either expressed
  * or implied, of JogAmp Community.
  */
 
 /**
  * <pre>
  *   __ __|_  ___________________________________________________________________________  ___|__ __
  *  //    /\                                           _                                  /\    \\
  * //____/  \__     __ _____ _____ _____ _____ _____  | |     __ _____ _____ __        __/  \____\\
  *  \    \  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\ \  /    /
  *   \____\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   &quot;  \_\/____/
  *  /\    \     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\
  * /  \____\                       http://jogamp.org  |_|                              /____/  \
  * \  /   &quot;' _________________________________________________________________________ `&quot;   \  /
  *  \/____.                                                                             .____\/
  * </pre>
  * 
  * <p>
  * JOGL2 OpenGL ES 2 demo to expose and learn what the RAW OpenGL ES 2 API looks
  * like.
  * 
  * Compile, run and enjoy: wget
  * http://jogamp.org/deployment/jogamp-current/archive/jogamp-all-platforms.7z
  * 7z x jogamp-all-platforms.7z cd jogamp-all-platforms wget
  * https://raw.github.com
  * /xranby/jogl-demos/master/src/demos/es2/RawGL2ES2demo.java javac -cp
  * jar/jogl-all.jar:jar/gluegen-rt.jar RawGL2ES2demo.java java -cp
  * jar/jogl-all.jar:jar/gluegen-rt.jar:. RawGL2ES2demo
  * </p>
  * 
  * 
  * @author Xerxes Rånby (xranby)
  */
 
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.Properties;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2ES2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLProfile;
 
 import net.marcuswatkins.pisaver.filters.ImageMetaDataFilter;
 import net.marcuswatkins.pisaver.gl.GLImage;
 import net.marcuswatkins.pisaver.gl.GLImagePreparer;
 import net.marcuswatkins.pisaver.gl.GLScreen;
 import net.marcuswatkins.pisaver.gl.GLTextureData;
 import net.marcuswatkins.pisaver.sources.FileImageSource;
 import net.marcuswatkins.pisaver.util.Util;
 
 import com.jogamp.newt.opengl.GLWindow;
 import com.jogamp.opengl.util.Animator;
 
 
 public class PiSaver implements GLEventListener {
 
 	public static final String PROPS_FILE = "pisaver.prop";
 	
 	static String filename;
 	
 	private static int width = 640;
 	private static int height = 480;
 
 	private static Saver<GL2ES2, GLTextureData, GLScreen> saver;
 	
 	private static GLScreen screen;
 	
 	private static ImageMetaDataFilter filter;
 	
 	private static File folders[];
 	
 	private static File cacheDir;
 	
 	public static void main(String[] args) {
 
 		
 		Properties props = new Properties( );
 		File propsFile = new File( PROPS_FILE );
 		if( propsFile.exists() && propsFile.canRead() ) {
 			try {
 				InputStream is = new FileInputStream( propsFile );
 				props.load( is );
 				is.close();
 			}
 			catch( Exception e ) {
 				System.err.println( "Error reading " + PROPS_FILE + ":" );
 				e.printStackTrace();
 			}
 		}
 		
 		String folderStr = props.getProperty( "folders", args.length > 0 ? args[0] : "" );
 		System.out.println( "Using folders: " + folderStr );
 		String folderStrAr[] = folderStr.split( ";" );
 		folders = new File[folderStrAr.length];
 		for( int i = 0; i < folders.length; i++ ) {
 			folders[i] = new File( folderStrAr[i] );
 		}
 		
 		float minRating = Util.safeParseFloat( props.getProperty( "minRating" ), -1.0f );
 		float maxRating = Util.safeParseFloat( props.getProperty( "maxRating" ), -1.0f );
 		
 		String includedTags = props.getProperty( "includeTags" );
 		String includedTagsAr[] = null;
 		
 		if( includedTags != null ) {
 			includedTags = includedTags.trim().toLowerCase();
 			if( includedTags.length() > 0 ) {
 				includedTagsAr = includedTags.split( ";" );
 			}
 		}
 		
 		String excludedTags = props.getProperty( "excludeTags" );
 		String excludedTagsAr[] = null;
 		if( excludedTags != null ) {
 			excludedTags = excludedTags.trim().toLowerCase();
 			if( excludedTags.length() > 0 ) {
 				excludedTagsAr = excludedTags.split( ";" );
 			}
 		}
 		
 		String cacheDirStr = props.getProperty( "cacheDir" );
 		if( cacheDirStr != null && cacheDirStr.length() > 0 ) {
 			cacheDir = new File( cacheDirStr );
 		}
 		
 		int cfgWidth = Util.safeParseInt(props.getProperty( "width" ), -1);
 		int cfgHeight = Util.safeParseInt(props.getProperty( "height" ), -1);
 		
 		filter = new ImageMetaDataFilter( minRating, maxRating, includedTagsAr, excludedTagsAr );
 		
 		
 		
 		saver = new Saver<GL2ES2,GLTextureData,GLScreen>( );
 		
 		
 		
 		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2ES2));
 		// We may at this point tweak the caps and request a translucent
 		// drawable
 		caps.setNumSamples( 8 );
 		caps.setSampleBuffers( true );
 		caps.setBackgroundOpaque(true);
 		GLWindow glWindow = GLWindow.create(caps);
 		// In this demo we prefer to setup and view the GLWindow directly
 		// this allows the demo to run on -Djava.awt.headless=true systems
 		
 		boolean fs = false;
 		if( cfgWidth == -1 || cfgHeight == -1 ) {
 			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); 
 			width = (int)dim.getWidth(); 
 			height = (int)dim.getHeight();
 		}
 		else {
 			width = cfgWidth;
 			height = cfgHeight;
 			fs = true;
 		}
 		
 		glWindow.setTitle("Collage Saver");
 		glWindow.setSize(width, height);
 		glWindow.setUndecorated(false);
 		glWindow.setPointerVisible(true);
 		glWindow.setVisible(true);
 		glWindow.setFullscreen( fs );
 
 		
 		//glWindow.setSize( glWindow.getScreen().getWidth(), glWindow.getScreen().getHeight() );
 		//glWindow.getScreen().getWidth();
 		
 		// Finally we connect the GLEventListener application code to the NEWT
 		// GLWindow.
 		// GLWindow will call the GLEventListener init, reshape, display and
 		// dispose
 		// functions when needed.
 		
 		glWindow.addGLEventListener(new PiSaver() /* GLEventListener */);
 		Animator animator = new Animator(glWindow);
		animator.add(glWindow);
 		animator.start();
 		
 	}
 
 
 	public void init(GLAutoDrawable drawable) {
 		GL2ES2 gl = drawable.getGL().getGL2ES2();
 
 		
 		System.err.println("Chosen GLCapabilities: "
 				+ drawable.getChosenGLCapabilities());
 		System.err.println("INIT GL IS: " + gl.getClass().getName());
 		System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
 		System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
 		System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
 		
 		
 		try {
 			GLImage.init(gl);
 			screen = new GLScreen( gl );
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit( 0 );
 		}
 		gl.glEnable( GL2ES2.GL_BLEND );
 		gl.glBlendFunc( GL2ES2.GL_SRC_ALPHA, GL2ES2.GL_ONE_MINUS_SRC_ALPHA );
 		ImagePreparer<GLTextureData> preparer = new GLImagePreparer( gl );
 		
 		if( cacheDir != null && cacheDir.isDirectory() ) {
 			preparer = new CachingImagePreparer<GLTextureData>( cacheDir, preparer, new GLTextureData() );
 		}
 		saver.init( new FileImageSource<GLTextureData>( folders, filter, preparer ), screen );
 	}
 
 	public void reshape(GLAutoDrawable drawable, int x, int y, int z, int h) {
 		System.out.println("Window resized to width=" + z + " height=" + h);
 		width = z;
 		height = h;
 		screen.reshape( width, height );
 		saver.screenChanged();
 	}
 
 	public void display(GLAutoDrawable drawable) {
 		//drawable.setGL( new DebugGL2ES2( drawable.getGL().getGL2ES2() ) );
 		GL2ES2 gl = drawable.getGL().getGL2ES2();
 		gl.glClearColor(0, 0, 0, 1.0f); //Black
 		gl.glClear(GL2ES2.GL_STENCIL_BUFFER_BIT | GL2ES2.GL_COLOR_BUFFER_BIT
 				| GL2ES2.GL_DEPTH_BUFFER_BIT);
 
 		
 		saver.draw( gl );
 	}
 	public void dispose(GLAutoDrawable drawable) {
 		System.out.println("cleanup, remember to release shaders");
 		GL2ES2 gl = drawable.getGL().getGL2ES2();
 		gl.glUseProgram(0);
 		System.out.println( "TODO: cleanup properly" );
 		saver.dispose( gl );
 		/*
 		gl.glDetachShader(shaderProgram, vertShader);
 		gl.glDeleteShader(vertShader);
 		gl.glDetachShader(shaderProgram, fragShader);
 		gl.glDeleteShader(fragShader);
 		gl.glDeleteProgram(shaderProgram);
 		*/
 		System.exit(0);
 	}
 
 }
