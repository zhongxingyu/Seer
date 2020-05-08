 package chalmers.dax021308.ecosystem.view.mapeditor;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
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
 import javax.media.opengl.GLJPanel;
 
 import chalmers.dax021308.ecosystem.model.agent.IAgent;
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.environment.IModel;
 import chalmers.dax021308.ecosystem.model.environment.mapeditor.MapEditorModel;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.EllipticalObstacle;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.RectangularObstacle;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.TriangleObstacle;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.Log;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.shape.CircleShape;
 import chalmers.dax021308.ecosystem.model.util.shape.IShape;
 import chalmers.dax021308.ecosystem.model.util.shape.SquareShape;
 import chalmers.dax021308.ecosystem.model.util.shape.TriangleShape;
 import chalmers.dax021308.ecosystem.view.IView;
 
 import com.sun.opengl.util.FPSAnimator;
 
 /**
  * View class for displaying obstacles in the Map editor.
  * <p>
  * @author Erik Ramqvist, Sebastian Anerud
  *
  */
 public class MapEditorGLView extends GLCanvas implements IView {
 	
 	private static final long serialVersionUID = 158552837620985591L;
 	private List<IObstacle> newObs = new ArrayList<IObstacle>();
 	public final Dimension size;
 	public final JOGLListener glListener;
 	private IShape shape;
 	private boolean isZoomed;
 	
 	private MouseEvent lastZoomEvent;
 	
 	private IObstacle selectedObstacle = null;
 	private Position startClick = null;
 	
 	/**
 	 * Create the panel.
 	 */
 	public MapEditorGLView(IModel model, final Dimension size) {
 		this.size = size;
 		model.addObserver(this);
 		glListener = new JOGLListener();
 		addGLEventListener(glListener);
 		addMouseWheelListener(new MouseWheelListener() {
 			@Override
 			public void mouseWheelMoved(MouseWheelEvent e) {
 				if(e.getWheelRotation() < 0) {
 					glListener.zoomIn();
 				} else {
 					glListener.zoomOut();
 				}
 				e.consume();
 			}
 		});
 		addMouseMotionListener(new MouseMotionListener() {
 			
 			@Override
 			public void mouseMoved(MouseEvent e) {
 			}
 			
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				if(selectedObstacle != null) {
 					double x = size.width*(e.getX())/getWidth();
 					double y = size.height - size.height*(e.getY())/getHeight();
 					double dx = x - startClick.getX();
 					double dy = y - startClick.getY();
 					startClick = new Position(x, y);
 					selectedObstacle.moveObstacle(dx, dy);
 				}
 			}
 		});
 		addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				selectedObstacle = null;
 			}
 			
 			@Override
 			public void mousePressed(MouseEvent e) {
 				
 				double x = size.width*(e.getX())/getWidth();
 				double y = size.height - size.height*(e.getY())/getHeight();
 				
 				selectedObstacle = getObstacleFromCoordinates(x, y);
 				if(selectedObstacle != null) {
 					startClick = new Position(x, y);
 					Random ran = new Random();
 					selectedObstacle.setColor(new Color(ran.nextInt(255), ran.nextInt(255), ran.nextInt(255)));
 					e.consume();
 				}
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 				selectedObstacle = null;
 			}
 			
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				selectedObstacle = null;
 			}
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {
 			}
 		});
 		FPSAnimator animator = new FPSAnimator(this, 30);
 		animator.start();
 	}
 	
 	
 	public IObstacle getObstacleFromCoordinates(double x, double y) {
 		if(newObs == null ) {
 			return null;
 		}
 		if(newObs.isEmpty()) {
 			return null;
 		}
 		Position p = new Position(x, y);
		for(IObstacle o : newObs) {
 			if(o.isInObstacle(p)) {
 				return o;
 			}
 		}
 		return null;
 	}
 	
 	private void moveObstacle(double dx, double dy, IObstacle o) {
 		if(o != null) {
 			o.moveObstacle(dx, dy);
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		String eventName = event.getPropertyName();
 		if(eventName == MapEditorModel.EVENT_OBSTACLES_CHANGED) {
 			//Tick notification recived from model. Do something with the data.
 			if(event.getNewValue() instanceof List<?>) {
 				this.newObs = (List<IObstacle>) event.getNewValue();
 			}
 		} else if(eventName == EcoWorld.EVENT_SHAPE_CHANGED) {
 			Object o = event.getNewValue();
 			if(o instanceof IShape) {
 				this.shape = (IShape) o;
 			}
 		}
 	}
 	
 	
 	/**
 	 * JOGL Listener, listenes to commands from the GLCanvas.
 	 * 
 	 * @author Erik
 	 *
 	 */
     public class JOGLListener implements GLEventListener {
     	
         	private final float  COLOR_FACTOR        = (1.0f/255);
         	private int zoomValue = 0;
         	private int currentScrollZoom = 0;
         	private int ZOOM_ACCELERATION = 10;
         	
     		
         	/**
         	 * Called each frame to redraw all the 3D elements.
         	 * 
         	 */
             @Override
             public void display(GLAutoDrawable drawable) {
             	GL gl = drawable.getGL();
         		if(lastZoomEvent != null) {
         			/*int pointOfInterestX = 0;
         			if(currentZoomX == lastZoomEvent.getX()) {
         				pointOfInterestX = lastZoomEvent.getX();
         			} else if(currentZoomX < lastZoomEvent.getX()) {
         				pointOfInterestX++;
         			} else if(currentZoomX > lastZoomEvent.getX()) {
         				pointOfInterestX--;
         			}
         			int pointOfInterestY = 0;
         			if(currentZoomY == lastZoomEvent.getY()) {
         				pointOfInterestY = lastZoomEvent.getY();
         			} else if(currentZoomY < lastZoomEvent.getY()) {
         				pointOfInterestY++;
         			} else if(currentZoomY > lastZoomEvent.getY()) {
         				pointOfInterestY--;
         			}*/
         			
         			int pointOfInterestX = lastZoomEvent.getX();
         			int pointOfInterestY = lastZoomEvent.getY();
         			int zoomLevel = 3;
             		
             		gl.glViewport(0, 0, getWidth(), getWidth());
             		gl.glMatrixMode(GL.GL_PROJECTION);
             		gl.glLoadIdentity();
             		 double left = (0 - pointOfInterestX) / zoomLevel + pointOfInterestX;
             		 double right = (getWidth() - pointOfInterestX) / zoomLevel + pointOfInterestX;
             		 double bottom = (getWidth() - pointOfInterestY) / zoomLevel + pointOfInterestY;
             		 double top = (0 - pointOfInterestY) / zoomLevel + pointOfInterestY;
             		 gl.glOrtho(left, right, bottom, top, -1, 1);
         		} else {
         			if(currentScrollZoom == zoomValue) {
         				currentScrollZoom = zoomValue;
         			} else if(currentScrollZoom < zoomValue) {
         				currentScrollZoom += ZOOM_ACCELERATION;
         			} else if(currentScrollZoom > zoomValue) {
         				currentScrollZoom -= ZOOM_ACCELERATION;
         			}
             		gl.glViewport(-currentScrollZoom, -currentScrollZoom, getWidth()+currentScrollZoom*2, getHeight()+currentScrollZoom*2);
             		gl.glMatrixMode(GL.GL_PROJECTION);
             		gl.glLoadIdentity();
         			gl.glOrtho(0, getWidth(), getHeight(), 0, 0, 1);
         		}
 
        
 //            	long start = System.currentTimeMillis();
             	
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
 	                gl.glColor3d(0.545098, 0.270588, 0.0745098);
 		          	for(double angle = 0; angle < 2.0*Math.PI; angle+=increment){
 		          		gl.glLineWidth(2.5F);
 		          		gl.glBegin(GL.GL_LINES); 
 		          		gl.glVertex2d(cx*(1+Math.cos(angle)), cy*(1+Math.sin(angle)));
 		          		gl.glVertex2d(cx*(1+Math.cos(angle+increment)), cy*(1+Math.sin(angle+increment)));
 		          		gl.glEnd();
 		          	}
           		} else if (shape != null && shape instanceof TriangleShape){
           			gl.glColor3d(0.545098, 0.270588, 0.0745098);
           			gl.glLineWidth(2.5F);
 	          		gl.glBegin(GL.GL_LINES); 
 	          		gl.glVertex2d(0, frameHeight);
 	          		gl.glVertex2d(frameWidth/2.0, 0);
 	          		gl.glEnd();
 	          		
 	          		gl.glLineWidth(2.5F);
 	          		gl.glBegin(GL.GL_LINES); 
 	          		gl.glVertex2d(frameWidth/2.0, 0);
 	          		gl.glVertex2d(frameWidth, frameHeight);
 	          		gl.glEnd();
 	          		
 	          		gl.glLineWidth(2.5F);
 	          		gl.glBegin(GL.GL_LINES); 
 	          		gl.glVertex2d(frameWidth, frameHeight);
 	          		gl.glVertex2d(0, frameHeight);
 	          		gl.glEnd();
           		} else if (shape != null && shape instanceof SquareShape){
           			gl.glColor3d(0.545098, 0.270588, 0.0745098);
           			gl.glLineWidth(2.5F);
 	          		gl.glBegin(GL.GL_LINES); 
 	          		gl.glVertex2d(0, 0);
 	          		gl.glVertex2d(frameWidth, 0);
 	          		gl.glEnd();
 	          		
 	          		gl.glLineWidth(2.5F);
 	          		gl.glBegin(GL.GL_LINES); 
 	          		gl.glVertex2d(0, 0);
 	          		gl.glVertex2d(0, frameHeight);
 	          		gl.glEnd();
 	          		
 	          		gl.glLineWidth(2.5F);
 	          		gl.glBegin(GL.GL_LINES); 
 	          		gl.glVertex2d(frameWidth, 0);
 	          		gl.glVertex2d(frameWidth, frameHeight);
 	          		gl.glEnd();
 	          		
 	          		gl.glLineWidth(2.5F);
 	          		gl.glBegin(GL.GL_LINES); 
 	          		gl.glVertex2d(frameWidth, frameHeight);
 	          		gl.glVertex2d(0, frameHeight);
 	          		gl.glEnd();
           		}
 	          	
           		/*
           		 * Draw Obstacles
           		 */
           		for(IObstacle o: newObs){
           			if(o != null && o instanceof EllipticalObstacle){
           				double increment = 2.0*Math.PI/50.0;
     	                double w = frameWidth*o.getWidth()/size.width;
     	                double h = frameHeight*o.getHeight()/size.height;
     	                double x = frameWidth*o.getPosition().getX()/size.width;
     	                double y = frameHeight*o.getPosition().getY()/size.height;
     	                Color c = o.getColor();
     	                gl.glColor3d((double)c.getRed()/(double)255, (double)c.getGreen()/(double)255, (double)c.getBlue()/(double)255);
     	                gl.glLineWidth(2.5F);
     	          		gl.glBegin(GL.GL_POLYGON); 
     		          	for(double angle = 0; angle < 2.0*Math.PI; angle+=increment){
     		          		gl.glVertex2d(x + w*Math.cos(angle),frameHeight - (y + h*Math.sin(angle)));
     		          	}
     		          	gl.glEnd();
           			} else if (o != null && o instanceof RectangularObstacle){
           				double x = o.getPosition().getX();
           				double y = o.getPosition().getY();
           				double w = o.getWidth();
           				double h = o.getHeight();
           				Color c = o.getColor();
           				gl.glColor3d((double)c.getRed()/(double)255, (double)c.getGreen()/(double)255, (double)c.getBlue()/(double)255);
           				gl.glLineWidth(2.5F);
     	          		gl.glBegin(GL.GL_POLYGON); 
     	          		gl.glVertex2d(frameWidth*(x-w)/size.width,
     	          				frameHeight - frameHeight*(y-h)/size.height);
     	          		
     	          		gl.glVertex2d(frameWidth*(x+w)/size.width, 
     	          				frameHeight - frameHeight*(y-h)/size.height);
     	          		
     	          		gl.glVertex2d(frameWidth*(x+w)/size.width, 
     	          				frameHeight - frameHeight*(y+h)/size.height);
     	          		
     	          		gl.glVertex2d(frameWidth*(x-w)/size.width, 
     	          				frameHeight - frameHeight*(y+h)/size.height);
     	          		gl.glEnd();
           			} else if(o != null && o instanceof TriangleObstacle){
           				double x = o.getPosition().getX();
           				double y = o.getPosition().getY();
           				double w = o.getWidth();
           				double h = o.getHeight();
           				Color c = o.getColor();
           				gl.glColor3d((double)c.getRed()/(double)255, (double)c.getGreen()/(double)255, (double)c.getBlue()/(double)255);
           				gl.glLineWidth(2.5F);
     	          		gl.glBegin(GL.GL_TRIANGLES); 
     	          		gl.glVertex2d(frameWidth*(x+w)/size.width,
     	          				frameHeight - frameHeight*(y-h)/size.height);
     	          		
     	          		gl.glVertex2d(frameWidth*(x-w)/size.width, 
     	          				frameHeight - frameHeight*(y-h)/size.height);
     	          		
     	          		gl.glVertex2d(frameWidth*(x)/size.width, 
     	          				frameHeight - frameHeight*(y+h)/size.height);
     	          		
     	          		gl.glEnd();
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
             
         	public void clearZoom() {
     			zoomValue = 0;
 			}
 
 			public void zoomOut() {
         		zoomValue = zoomValue - 20;
         		if(zoomValue < 0) {
         			zoomValue = 0;
         		}
 			}
         	
 			public void zoomIn() {
         		zoomValue = zoomValue + 20;
 			}
 
 			public double getNorm(double x, double y){
         		return Math.sqrt((x*x)+(y*y));
         	}
 
  
             @Override
             public void init(GLAutoDrawable drawable) {
 
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
             public void reshape(GLAutoDrawable drawable, int x, int y, int width,
                             int height) {
                     //System.out.println("RESHAPE CALLED Frame size:" + getSize().toString());
                     //Projection mode is for setting camera
             		GL gl = drawable.getGL();
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
 
 			@Override
 			public void displayChanged(GLAutoDrawable arg0, boolean arg1,
 					boolean arg2) {
 				
 			}     
     }
 
 	@Override
 	public void init() {
 	}
 
 	@Override
 	public void addController(ActionListener controller) {
 		
 	}
 
 	@Override
 	public void onTick() {
 		
 	}
 
 	@Override
 	public void release() {
 		
 	}
 
 }
