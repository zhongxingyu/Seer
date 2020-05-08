 package org.tqdev.visarray;
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ServiceLoader;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 
 /**
  * @author James Sweet
  *
  */
 public class VisArrayApplet extends Applet {
 	private final class LWJGLCanvas extends Canvas{
 		RenderThread Renderer = new RenderThread();
 		
 		public final void addNotify() {
 			super.addNotify();
 			Renderer.start();
 		}
 		
 		public final void removeNotify() {
 			try {
 				Renderer.StopRenderer();
 				Renderer.join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			super.removeNotify();
 		}
 	}
 	
 	private final class RenderThread extends Thread{
 		private final VisApplication Renderer;
 		private boolean mRunning = true;
 		
 		public RenderThread(){
 			Iterator<VisApplication> apps = ServiceLoader.load(VisApplication.class).iterator();
 	        if (!apps.hasNext()) {
	        	throw new RuntimeException("No Renderers Found");
 	        }
 	        
 	        Renderer = apps.next();
 		}
 		
 		private final String GetParameter( String name, String defaultVal ) {
 	        String retVal = getParameter( name );
 
 	        if ( retVal == null ) {
 	            retVal = defaultVal;
 	        }
 
 	        return retVal;
 	    }
 		
 		private final void SetupParameters(){
 			List<String> retVal = new ArrayList<String>();
 			
 			for (int i = 0; i < Renderer.Parameters().size(); i++) {
 				retVal.add( GetParameter( Renderer.Parameters().get(i), "" ) );
 			}
 			
 			Renderer.appletInit(retVal);
 		}
 		
 		synchronized public void StopRenderer(){
 			mRunning = false;
 		}
 		
 		public void run() {
 			try {
 				Display.setParent(display_parent);
 				Display.create();
 				SetupParameters();
 				Renderer.resize(display_parent.getWidth(), display_parent.getHeight());
 			} catch (LWJGLException e) {
 				e.printStackTrace();
 				return;
 			}
 			
 			while( mRunning ){
 				Display.sync(60);
 				Renderer.render();
 				Display.update();
 			}
 			Renderer.destroy();
 			Display.destroy();
 		}
 	}
 	
 	Canvas display_parent;
 	
 	public void start() {
 		
 	}
 
 	public void stop() {
 		
 	}
 	
 	public void destroy() {
 		remove(display_parent);
 		super.destroy();
 	}
 	
 	public void init() {
 		setLayout(new BorderLayout());
 		try {
 			display_parent = new LWJGLCanvas();
 			display_parent.setSize(getWidth(),getHeight());
 			display_parent.setFocusable(true);
 			display_parent.requestFocus();
 			display_parent.setIgnoreRepaint(true);
 			
 			add(display_parent);
 			
 			setVisible(true);
 		} catch (Exception e) {
 			System.err.println(e);
 			throw new RuntimeException("Unable to create display");
 		}
 	}
 }
