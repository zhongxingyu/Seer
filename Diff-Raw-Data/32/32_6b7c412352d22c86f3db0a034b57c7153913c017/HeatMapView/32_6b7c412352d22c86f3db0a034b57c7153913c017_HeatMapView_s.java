 package chalmers.dax021308.ecosystem.view;
 
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCanvas;
 import javax.media.opengl.GLEventListener;
 
 import chalmers.dax021308.ecosystem.model.agent.IAgent;
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.environment.IModel;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
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
  * For Javadoc add the Jogl Javadoc jar as Javadoc refernce to the selected JOGL jar.
  * <p>
  * @author Original class OpelGLSimulationView by Erik Ramqvist. Copied and modified into a heat map by Sebastian Anerud.
  *
  */
 public class HeatMapView extends GLCanvas implements IView {
 	
 	private static final long serialVersionUID = 1585638837620985591L;
 	private List<IPopulation> newPops = new ArrayList<IPopulation>();
 	private int[][] heatMap;
 	private boolean[][] visited;
 	int heatMapWidth;
 	int heatMapHeight;
	private double samplingConstant;
 	int maxVisited = 1;
 	int minVisited = Integer.MAX_VALUE;
 	private Dimension grid;
 	private JOGLListener glListener;
 	private String populationName;
 	//private GLCanvas canvas;
 	
 	/**
 	 * Create the heat map.
 	 */
	public HeatMapView(IModel model, Dimension grid, double samplingConstant, String populationName) {
 		this.grid = grid;
		this.samplingConstant = samplingConstant;
 		this.populationName = populationName;
		heatMapWidth = (int)(grid.getWidth()/samplingConstant+1);
		heatMapHeight = (int)(grid.getHeight()/samplingConstant+1);
 		heatMap = new int[heatMapWidth][heatMapHeight];
 		visited = new boolean[heatMapWidth][heatMapHeight];
 		model.addObserver(this);
      
 		glListener = new JOGLListener();
 		addGLEventListener(glListener);     
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		String eventName = event.getPropertyName();
 		if(eventName == EcoWorld.EVENT_STOP) {
 			maxVisited = 1;
 			minVisited = Integer.MAX_VALUE;
 			heatMap = new int[heatMapWidth][heatMapHeight];
 			visited = new boolean[heatMapWidth][heatMapHeight];
 		} else if(eventName == EcoWorld.EVENT_TICK) {
 			//Tick notification recived from model. Do something with the data.
 			if(event.getNewValue() instanceof List<?>) {
 				this.newPops = (List<IPopulation>) event.getNewValue();
 			}	
 			repaint();
 		} else if(eventName == EcoWorld.EVENT_DIMENSIONCHANGED) {
 			Object o = event.getNewValue();
 			if(o instanceof Dimension) {
 				this.grid = (Dimension) o;
				heatMapWidth = (int)(grid.getWidth()/samplingConstant+1);
				heatMapHeight = (int)(grid.getHeight()/samplingConstant+1);
 				heatMap = new int[heatMapWidth][heatMapHeight];
 				visited = new boolean[heatMapWidth][heatMapHeight];
 			}
 			//Handle dimension change here.
 		}
 	}
 	
 	/**
 	 * JOGL Listener, listenes to commands from the GLCanvas.
 	 * 
 	 * @author Erik
 	 *
 	 */
     private class JOGLListener implements GLEventListener {
     	
         	GL gl = getGL();
     		
         	/**
         	 * @author Sebastian. Credit to Erik for original class.
         	 * Called each time the model updates itself.
         	 * 
         	 */
             @Override
             public void display(GLAutoDrawable drawable) {
             	long start = System.currentTimeMillis();
             	
                 double frameHeight = getHeight();
                 double frameWidth  = getWidth();
 
                 /*
                  * Loops through all agents and truncates their positions to the correct box
                  * in the heat map. It then adds 1 to that box to indicate that an agent visited
                  * that box in this iteration. A pixel can only get +1 per iteration to prevent
                  * the corners to dominate too much.
                  */
                 minVisited = Integer.MAX_VALUE;
                 visited = new boolean[heatMapWidth][heatMapHeight];
                 int popSize = newPops.size();
           		for(int i = 0; i < popSize; i ++) {
           			if(newPops.get(i).getName().equals(populationName)){
 	        			List<IAgent> agents = newPops.get(i).getAgents();
 	        			int size = agents.size();
 	        			IAgent a;
 	        			Position pos;
 	        			int intPosX;
 	        			int intPosY;
 	        			for(int j = 0; j < size; j++) {
 	        				a = agents.get(j);
 	        				pos = a.getPosition();
	        				intPosX = (int)(pos.getX()/samplingConstant);
	    					intPosY = (int)(pos.getY()/samplingConstant);
 	    					
 //	    					if(!visited[intPosX][intPosY]){
 	    						heatMap[intPosX][intPosY]++;
 	    						visited[intPosX][intPosY]=true;
 //	    					}
 	    						
 	    					if(heatMap[intPosX][intPosY]>maxVisited){
 	    						maxVisited = heatMap[intPosX][intPosY];
 	    					} 
 	                    }
           			}
         		} 
           		
           		/*
           		 * Check which pixel is visited the least.
           		 * Must be able to do in a better way?
           		 */
           		for(int j=0;j<heatMapHeight;j++){
           			for(int i=0;i<heatMapWidth;i++){
           				if(heatMap[i][j]<minVisited && heatMap[i][j] > 0){
     						minVisited = heatMap[i][j];
     					} 
           			}
           		}	
           		//System.out.println(minVisited);	
           		
           		/*
           		 * Draw the heat map.
           		 */
           		for(int j=0;j<heatMapHeight;j++){
           			for(int i=0;i<heatMapWidth;i++){
           				
           				/*
           				 * "value" is a value between 0 and 1 and is based on where a pixel lies on the scale between
           				 * minVisited and maxVisited. If a pixel has minVisited visits, it gets value 0. If a pixel has
           				 * maxVisited visits, it gets value 1.
           				 */
           				double value = ((double)((double)(heatMap[i][j]-minVisited))/((double)(maxVisited-minVisited)));
           				
           				/*
           				 * Below does the following re-scaling of colors:
           				 * If 0 <= value < 0.5 the color is 0 <= red < 255 and green = 255.
           				 * If value = 0.5, the color is red = 255 and green = 255.
           				 * If 0.5 < value <= 1, the color is red = 255 and 255 > green >= 0.
           				 */
           				double red = 1;
           				double green = 1;
           				
           				if(value < 0.5){
           					red = 2*value;
           				} else {
           					green = 1 - 2*(value-0.5);
           				}
           				
           				gl.glColor3d(red, green, 0);
                   		gl.glBegin(GL.GL_POLYGON);
                   		
                   		/*
                   		 * Create a box with for corners at the right positions
                   		 */
                   		double xBotLeft = ((double)i)*frameWidth/(double)heatMapWidth;
                   		double yBotLeft = frameHeight - ((double)j)*frameHeight/(double)heatMapHeight;
                   		
                   		double xBotRight = ((double)(i+1))*frameWidth/(double)heatMapWidth;
                   		double yBotRight = yBotLeft;
                   		
                   		double xTopLeft = xBotLeft;
                   		double yTopLeft = frameHeight - ((double)(j+1))*frameHeight/(double)heatMapHeight;
                   		
                   		double xTopRight = xBotRight;
                   		double yTopRight = yTopLeft;
                   		
                   		gl.glVertex2d(xBotLeft, yBotLeft);
                   		gl.glVertex2d(xTopLeft, yTopLeft);
                   		gl.glVertex2d(xTopRight, yTopRight);
                   		gl.glVertex2d(xBotRight, yBotRight);
                   		
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
             
             @Override
             public void init(GLAutoDrawable drawable) {
 //                    System.out.println("INIT CALLED");
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
             public void reshape(GLAutoDrawable drawable, int arg1, int arg2, int arg3,
                     int arg4) {
     		GL gl = drawable.getGL();
         	gl.glMatrixMode(GL.GL_PROJECTION);
             gl.glLoadIdentity();
             gl.glOrtho(0, getWidth(), getHeight(), 0, 0, 1);
             gl.glMatrixMode(GL.GL_MODELVIEW);
             gl.glDisable(GL.GL_DEPTH_TEST);
             gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);  
             gl.glClear(GL.GL_COLOR_BUFFER_BIT);
             gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
             gl.glEnable (GL.GL_BLEND);
             gl.glLoadIdentity();
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
