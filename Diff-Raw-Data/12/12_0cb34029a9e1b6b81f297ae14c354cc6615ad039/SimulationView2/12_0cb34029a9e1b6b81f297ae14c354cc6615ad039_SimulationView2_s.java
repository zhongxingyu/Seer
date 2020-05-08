 package chalmers.dax021308.ecosystem.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCanvas;
 import javax.media.opengl.GLEventListener;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 
 import chalmers.dax021308.ecosystem.model.agent.IAgent;
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.environment.IObstacle;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.model.util.Position;
 
 /**
  * OpenGL version of SimulationView.
  * <p>
  * Uses JOGL library.
  * <p>
  * Install instructions:
  * <p>
  * Download: http://download.java.net/media/jogl/builds/archive/jsr-231-1.1.1a/
  * Select the version of your choice, i.e. windows-amd64.zip
  * Extract the files to a folder.
  * Add the extracted files jogl.jar and gluegen-rt.jar to build-path.
  * Add path to jogl library to VM-argument in Run Configurations
  * <p>
  * @author Erik Ramqvist
  *
  */
 public class SimulationView2 extends GLCanvas implements IView {
 	
 	private static final long serialVersionUID = 1585638837620985591L;
 	private List<IPopulation> newPops = new ArrayList<IPopulation>();
 	private List<IObstacle> newObs = new ArrayList<IObstacle>();
 	private Timer fpsTimer;
 	private int updates;
 	private int lastFps;
 	private boolean showFPS;
 	private int newFps;
 	private Object fpsSync = new Object();
 	private Dimension size;
 	private JFrame frame;
 	//private GLCanvas canvas;
 	/**
 	 * Create the panel.
 	 */
 	public SimulationView2(EcoWorld model, Dimension size, boolean showFPS) {
 		frame = new JFrame("Simulation View");
 		this.size = size;
 		model.addObserver(this);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setVisible(true);
 		frame.add(this);
 		frame.setSize(size);
 		//setVisible(true);
 		//setSize(size);
 
         //canvas = new GLCanvas();
         //canvas.setSize(size);
         //canvas.addGLEventListener(new JOGLListener());
 		addGLEventListener(new JOGLListener());
         //add();
         
 		this.showFPS = showFPS;
 		if(showFPS) {
 			fpsTimer = new Timer();
 			fpsTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					int fps = getUpdate();
					if(fps + lastFps != 0) {
 						fps = ( fps + lastFps ) / 2;
					} 
 					setNewFps(fps);
 					lastFps = fps;
 					setUpdateValue(0);
 				}
 			}, 1000, 1000);
 		}
 	}
 	
 	private int getUpdate() {
 		synchronized (SimulationView2.class) {
 			return updates;
 		}
 	}
 
 	private void setUpdateValue(int newValue) {
 		synchronized (SimulationView2.class) {
 			updates = newValue;
 		}
 	}
 	
 
 	private int getNewFps() {
 		synchronized (fpsSync) {
 			return newFps;
 		}
 	}
 
 	private void setNewFps(int newValue) {
 		synchronized (fpsSync) {
 			newFps = newValue;
 		}
 	}
 	
 	private void increaseUpdateValue() {
 		synchronized (SimulationView2.class) {
 			updates++;
 		}
 	}
 
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		String eventName = event.getPropertyName();
 		if(eventName == EcoWorld.EVENT_STOP) {
 			//Model has stopped. Maybe hide view?
 			//frame.setVisible(false);
 		} else if(eventName == EcoWorld.EVENT_TICK) {
 			//Tick notification recived from model. Do something with the data.
 			if(event.getNewValue() instanceof List<?>) {
 				this.newPops = (List<IPopulation>) event.getNewValue();
 			}
 			if(event.getOldValue() instanceof List<?>) {
 				this.newObs = (List<IObstacle>) event.getOldValue();
 			}
 			/*if(canvas != null) {
 				canvas.repaint();
 			}*/
 			repaint();
 			//removeAll();
 			//repaint();
 			//revalidate();
 		}
 	}
 	
 	/**
 	 * Sets the FPS counter visible or not visible
 	 * 
 	 * @param visible
 	 */
 	public void setFPSCounterVisible(boolean visible) {
 		if(showFPS && !visible) {
 				fpsTimer.cancel();
 				showFPS = visible;
 		} else if(!showFPS && visible) {
 			fpsTimer = new Timer();
 			fpsTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					newFps = getUpdate();
 					int temp = newFps;
 					if(newFps + lastFps != 0) {
 						newFps = ( newFps + lastFps ) / 2;
 					} 
 					lastFps = temp;
 					setUpdateValue(0);
 				}
 			}, 1000, 1000);
 			showFPS = true;
 		}
 	}
 	
 
     private class JOGLListener implements GLEventListener {
     		//The last number is the number of edges the created circle.
        	double increment = 2*Math.PI/4;
         	GL gl = getGL();
         	double PI_TIMES_TWO = 2*Math.PI;
     		
             @Override
             public void display(GLAutoDrawable drawable) {
             	increaseUpdateValue();
             	Log.v("OpenGL Redraw! Fps: " + getNewFps());
 
                 //Background drawing
                 //Color of the background.
                 gl.glColor4f(1, 1, 1, 1);
           		gl.glBegin(GL.GL_POLYGON);
           		gl.glVertex2d(getWidth(), 0);
           		gl.glVertex2d(0, 0);
           		gl.glVertex2d(getWidth(), getHeight());
           		gl.glEnd();
           		gl.glBegin(GL.GL_POLYGON);
           		gl.glVertex2d(0, getHeight());
           		gl.glVertex2d(0, 0);
           		gl.glVertex2d(getWidth(), getHeight());
           		gl.glEnd();
                 boolean flipBoolean = false;
         		for(IPopulation pop : newPops) {
         			if(flipBoolean) {
                         gl.glColor4f(0, 1, 0, 1);
         				flipBoolean = false;
         			} else {
                         gl.glColor4f(1, 0, 0, 1);
         				flipBoolean = true;
         			}
         			for(IAgent a : pop.getAgents()) {
         				Position p = a.getPosition();
                         double cx = p.getX();
                         double cy = getHeight() - p.getY();
                         double radius = a.getWidth()/2 - 2;
         	          	for(double angle = 0; angle < PI_TIMES_TWO; angle+=increment){
         	          		gl.glBegin(GL.GL_POLYGON);
         	              //One vertex of each triangle is at center of circle
         	          		gl.glVertex2d(cx, cy);
         	              //Other two vertices form the periphery of the circle		
         	          		gl.glVertex2d(cx + Math.cos(angle)* radius, cy + Math.sin(angle)*radius);
         	          		gl.glVertex2d(cx + Math.cos(angle + increment)*radius, cy + Math.sin(angle + increment)*radius);
         	          		gl.glEnd();
         	          	}
         			}
         		}        		
 
             }
  
             @Override
             public void init(GLAutoDrawable drawable) {
                     System.out.println("INIT CALLED");
                     //Projection mode is for setting camera
                 	gl.glMatrixMode(GL.GL_PROJECTION);
                   //This will set the camera for orthographic projection and allow 2D view
                   //Our projection will be on 400 X 400 screen
                     gl.glLoadIdentity();
                  //   Log.v("Width: " + getWidth());
                  //   Log.v("Height: " + getHeight());
                     gl.glOrtho(0, getWidth(), getHeight(), 0, 0, 1);
                   //Modelview is for drawing
                     gl.glMatrixMode(GL.GL_MODELVIEW);
                   //Depth is disabled because we are drawing in 2D
                     gl.glDisable(GL.GL_DEPTH_TEST);
                   //Setting the clear color (in this case black)
                   //and clearing the buffer with this set clear color
                     gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);  
                     gl.glClear(GL.GL_COLOR_BUFFER_BIT);
                   //This defines how to blend when a transparent graphics
                   //is placed over another (here we have blended colors of
                   //two consecutively overlapping graphic objects)
                     gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                     gl.glEnable (GL.GL_BLEND);
                     gl.glLoadIdentity();
                   //After this we start the drawing of object  
                   //We want to draw a triangle which is a type of polygon
             }
             
            /**
             * Called by the drawable during the first repaint after the component has been resized. The
             * client can update the viewport and view volume of the window appropriately, for example by a
             * call to GL.glViewport(int, int, int, int); note that for convenience the component has
             * already called GL.glViewport(int, int, int, int)(x, y, width, height) when this method is
             * called, so the client may not have to do anything in this method.
 		    *
 		    * @param gLDrawable The GLDrawable object.
 		    * @param x The X Coordinate of the viewport rectangle.
 		    * @param y The Y coordinate of the viewport rectanble.
 		    * @param width The new width of the window.
 		    * @param height The new height of the window.
 		    */
             @Override
             public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
                             int arg4) {
                     System.out.println("RESHAPE CALLED");
  
             }
 
 			@Override
 			public void displayChanged(GLAutoDrawable arg0, boolean arg1,
 					boolean arg2) {
 				
 			}     
     }
 
 	@Override
 	public void init() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void addController(ActionListener controller) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onTick() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void release() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
