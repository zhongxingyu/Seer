 package chalmers.dax021308.ecosystem.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCanvas;
 import javax.media.opengl.GLEventListener;
 import javax.swing.JFrame;
 
 import com.sun.opengl.util.FPSAnimator;
 
 
 import chalmers.dax021308.ecosystem.model.agent.IAgent;
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.environment.IModel;
 import chalmers.dax021308.ecosystem.model.environment.IObstacle;
 import chalmers.dax021308.ecosystem.model.population.AbstractPopulation;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.CircleShape;
 import chalmers.dax021308.ecosystem.model.util.IShape;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.Vector;
 
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
  * For Javadoc add the Jogl Javadoc jar as Javadoc refernce to the selected JOGL jar.
  * <p>
  * @author Erik Ramqvist
  *
  */
 public class OpenGLSimulationView extends GLCanvas implements IView {
 	
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
 	private JOGLListener glListener;
 	//private GLCanvas canvas;
 	private IShape shape;
 	
 	/**
 	 * Create the panel.
 	 */
 	public OpenGLSimulationView(IModel model, Dimension size, boolean showFPS) {
 		this.size = size;
 		model.addObserver(this);
 		
 		//setVisible(true);
 		//setSize(size);
 
         //canvas = new GLCanvas();
         //canvas.setSize(size);
         //canvas.addGLEventListener(new JOGLListener());
 		glListener = new JOGLListener();
 		addGLEventListener(glListener);
 		FPSAnimator animator = new FPSAnimator(this, 60);
 		animator.start();
         //add();
         
 		this.showFPS = showFPS;
 		if(showFPS) {
 			fpsTimer = new Timer();
 			fpsTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					int fps = getUpdate();
 					/*if(fps + lastFps != 0) {
 						fps = ( fps + lastFps ) / 2;
 					} */
 					setNewFps(fps);
 					lastFps = fps;
 					setUpdateValue(0);
 				}
 			}, 1000, 1000);
 		}
 	}
 	
 	private int getUpdate() {
 		synchronized (OpenGLSimulationView.class) {
 			return updates;
 		}
 	}
 
 	private void setUpdateValue(int newValue) {
 		synchronized (OpenGLSimulationView.class) {
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
 		synchronized (OpenGLSimulationView.class) {
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
 				this.newPops = AbstractPopulation.clonePopulationList((List<IPopulation>) event.getNewValue());
 			}
 			if(event.getOldValue() instanceof List<?>) {
 				this.newObs = (List<IObstacle>) event.getOldValue();
 			}
 			/*if(canvas != null) {
 				canvas.repaint();
 			}*/
 			//repaint();
 			//display();
 			//removeAll();
 			//repaint();
 			//revalidate();
 		} else if(eventName == EcoWorld.EVENT_DIMENSIONCHANGED) {
 			Object o = event.getNewValue();
 			if(o instanceof Dimension) {
 				this.size = (Dimension) o;
 			}
 		} else if(eventName == EcoWorld.EVENT_SHAPE_CHANGED) {
 			Object o = event.getNewValue();
 			if(o instanceof IShape) {
 				this.shape = (IShape) o;
 			}
 		}
 	}
 	
 	/**
 	 * Clones the given list with {@link IPopulation#clonePopulation()} method.
 	 */
 	/*private List<IPopulation> clonePopulationList(List<IPopulation> popList) {
 		List<IPopulation> list = new ArrayList<IPopulation>(popList.size());
 		for(IPopulation p : popList) {
 			list.add(p.clonePopulation());
 		}
 		return list;
 	}*/
 	
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
 	
 	/**
 	 * JOGL Listener, listenes to commands from the GLCanvas.
 	 * 
 	 * @author Erik
 	 *
 	 */
     private class JOGLListener implements GLEventListener {
     	
     		//Number of edges in each created circle.
     		private final double VERTEXES_PER_CIRCLE = 6;
     		private final double PI_TIMES_TWO        = 2*Math.PI;
         	private final double increment           = PI_TIMES_TWO/VERTEXES_PER_CIRCLE;
         	private final float  COLOR_FACTOR        = (1.0f/255);
         	
         	GL gl = getGL();
     		
         	/**
         	 * Called each frame to redraw all the 3D elements.
         	 * 
         	 */
             @Override
             public void display(GLAutoDrawable drawable) {
             	increaseUpdateValue();
             	long start = System.currentTimeMillis();
             	
                 double frameHeight = (double)getHeight();
                 double frameWidth  = (double)getWidth();
                 
                 double scaleX = frameWidth / size.width;
                 double scaleY = frameHeight / size.height;
 
                 gl.glColor3d(0.9, 0.9, 0.9);
           		gl.glBegin(GL.GL_POLYGON);
           		gl.glVertex2d(0, 0);
           		gl.glVertex2d(0, frameHeight);
           		gl.glVertex2d(frameWidth, frameHeight);
           		gl.glVertex2d(frameWidth, 0);
           		gl.glEnd();
           		
           		if(shape != null && shape instanceof CircleShape) {
           				double increment = 2.0*Math.PI/50.0;
 		                double cx = frameWidth / 2.0;
 		                double cy = frameHeight/ 2.0;
 		                double radius = cx;
 		                gl.glColor3d(0.545098, 0.270588, 0.0745098);
 		          	for(double angle = 0; angle < 2.0*Math.PI; angle+=increment){
 		          		gl.glLineWidth(2.5F);
 		          		gl.glBegin(GL.GL_LINES); 
 		          		gl.glVertex2d(cx + Math.cos(angle)* radius, cy + Math.sin(angle)*radius);
		          		radius = (frameWidth / 2.0) + Math.abs(Math.sin(angle+increment) * ( (frameHeight - frameWidth ) / 2.0));
 		          		gl.glVertex2d(cx + Math.cos(angle + increment)*radius, cy + Math.sin(angle + increment)*radius);
 		          		gl.glEnd();
 		          	}
           		}
 	          	
           		int popSize = newPops.size();
           		for(int i = 0; i < popSize; i ++) {
         			List<IAgent> agents = newPops.get(i).getAgents();
         			int size = agents.size();
         			IAgent a;
         			for(int j = 0; j < size; j++) {
         				a = agents.get(j);
                         Color c = a.getColor();
         				gl.glColor4f((1.0f/255)*c.getRed(), COLOR_FACTOR*c.getGreen(), COLOR_FACTOR*c.getBlue(), COLOR_FACTOR*c.getAlpha());
 
         				Position p = a.getPosition();
                         /*double cx = p.getX();
                         double cy = getHeight() - p.getY();
                         double radius = a.getWidth()/2 + 5;*/
                         double height = (double)a.getHeight();
                         double width = (double)a.getWidth();
     	          		
                         double originalX = a.getVelocity().x;
                         double originalY = a.getVelocity().y;
                         double originalNorm = getNorm(originalX, originalY);
                         //if(v.getX() != 0 && v.getY() != 0) {
                   		gl.glBegin(GL.GL_TRIANGLES);
                   		
                   		double x = originalX * 2.0*height/(3.0*originalNorm);
                   		double y = originalY * 2.0*height/(3.0*originalNorm);
       	          		 
           				//Vector bodyCenter = new Vector(p.getX(), p.getY());
       	      	        double xBodyCenter = p.getX();
       	      	        double yBodyCenter = p.getY();
       	          		//Vector nose = new Vector(x+xBodyCenter, y+yBodyCenter);
       	          		double noseX = x+xBodyCenter;
   	          			double noseY = y+yBodyCenter;
       	          		
       	          		double bottomX = (originalX * -1.0*height/(3.0*originalNorm)) + xBodyCenter;
       	          		double bottomY = (originalY * -1.0*height/(3.0*originalNorm)) + yBodyCenter ;
       	          		
       	          		//Vector legLengthVector = new Vector(-originalY/originalX,1);
       	          		double legLengthX1 = -originalY/originalX;
       	          		double legLengthY1 = 1;
       	          		double legLenthVectorNorm2 = width/(2*getNorm(legLengthX1,legLengthY1 ));
       	          		//legLengthVector = legLengthVector.multiply(legLenthVectorNorm2);
       	          		legLengthX1 = legLengthX1 * legLenthVectorNorm2;
       	          		legLengthY1 = legLengthY1 * legLenthVectorNorm2;
       	          		//Vector rightLeg = legLengthVector;
       	          		double rightLegX = legLengthX1 + bottomX;
       	          		double rightLegY = legLengthY1 + bottomY;
       	          		
       	          		//v = new Vector(a.getVelocity());
       	          	    double legLengthX2 = originalY/originalX * legLenthVectorNorm2;
       	          	    double legLengthY2 = -1 * legLenthVectorNorm2;
       	          	    //legLengthVector = new Vector(originalY/originalX,-1);
       	          	   // legLengthVector = legLengthVector.multiply(legLenthVectorNorm2);
       	          		//Vector leftLeg = legLengthVector.add(bottom);
       	          		//Vector leftLeg = legLengthVector;
   	          			double leftLegX = legLengthX2 + bottomX;
       	          		double leftLegY = legLengthY2 + bottomY;
       	          		
       	          		gl.glVertex2d(scaleX*noseX, frameHeight - scaleY*noseY);
       	          		gl.glVertex2d(scaleX*rightLegX, frameHeight - scaleY*rightLegY);
       	          		gl.glVertex2d(scaleX*leftLegX, frameHeight - scaleY*leftLegY);
       	          		gl.glEnd();
 	      	          	/*} else {
 	        	          	for(double angle = 0; angle < PI_TIMES_TWO; angle+=increment){
 	        	          		gl.glBegin(GL.GL_TRIANGLES);
 	        	          		gl.glVertex2d(cx, cy);
 	        	          		gl.glVertex2d(cx + Math.cos(angle)* radius, cy + Math.sin(angle)*radius);
 	        	          		gl.glVertex2d(cx + Math.cos(angle + increment)*radius, cy + Math.sin(angle + increment)*radius);
 	        	          		gl.glEnd();
 	        	          	}
                         }*/
         			}
         		}      
 //        		
 //        		/* Information print, comment out to increase performance. */
 //        		Long totalTime = System.currentTimeMillis() - start;
 //        		StringBuffer sb = new StringBuffer("OpenGL Redraw! Fps: ");
 //        		sb.append(getNewFps());
 //        		//sb.append(" Rendertime in ms: ");
 //        		//sb.append(totalTime);
 //            	System.out.println(sb.toString());	
         		/* End Information print. */
             }
             
         	public double getNorm(double x, double y){
         		return Math.sqrt((x*x)+(y*y));
         	}
 
  
             @Override
             public void init(GLAutoDrawable drawable) {
 //                    System.out.println("INIT CALLED");
                     //Projection mode is for setting camera
                 	gl.glMatrixMode(GL.GL_PROJECTION);
                   //This will set the camera for orthographic projection and allow 2D view
                   //Our projection will be on 400 X 400 screen
                     gl.glLoadIdentity();
 //                    Log.v("getWidth(): " + getWidth());
 //                    Log.v("getHeight(): " + getHeight());
 //                    Log.v("size.width: " + size.width);
 //                    Log.v("size.height: " + size.height);
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
