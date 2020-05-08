 // Copyright 2010 DEF
 //
 // This file is part of V3dScene.
 //
 // V3dScene is free software: you can redistribute it and/or modify
 // it under the terms of the GNU Lesser General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // V3dScene is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public License
 // along with V3dScene.  If not, see <http://www.gnu.org/licenses/>.
 package fr.def.iss.vd2.lib_v3d;
 
 import java.awt.Point;
 import java.awt.event.HierarchyEvent;
 import java.awt.event.HierarchyListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.fenggui.binding.render.lwjgl.LWJGLBinding;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 import fr.def.iss.vd2.lib_v3d.camera.V3DCameraBinding;
 import fr.def.iss.vd2.lib_v3d.element.V3DElement;
 
 /**
  *
  * @author fberto
  */
 public class V3DCanvas {
 
     /** Serial version UID. */
     private static final long serialVersionUID = 1L;
     /** The OpenGL animator. */
     private static V3DAnimator animator = new V3DAnimator();
     private V3DContext context;
     private int mouseX = 0;
     private int mouseY = 0;
     private boolean select = false;
     private V3DCameraBinding focusCamera;
     private List<ResizeListener> resizeListenerList = new ArrayList<ResizeListener>();
     private boolean enabled = false;
     private boolean initied = false;
     private boolean showFps = false;
     private boolean polygonOffset = false;
     private boolean done=false;
     
     /**
      * A new mini starter.
      *
      * @param capabilities The GL capabilities.
      * @param width The window width.
      * @param height The window height.
      */
     public V3DCanvas(final V3DContext context, int width, int height) {
         this.context = context;
         //TODO repai
        init();
         //initListeners();
     }
    
     V3DElement map;
 	private int width;
 	private int height;
 
     /**
      * Sets up the screen.
      *
      * @see javax.media.openGL11.GLEventListener#init(javax.media.openGL11.GLAutoDrawable)
      */
     public void init() {
     	width=1024;
         height=768;
     
         try{
             Display.setDisplayMode(new DisplayMode(width, height));
             Display.setVSyncEnabled(true);
             Display.setTitle("Shader Setup");
             Display.create();
         }catch(Exception e){
             System.out.println("Error setting up display");
             System.exit(0);
         }
 
         /*GL11.glViewport(0,0,w,h);
         GL11.glMatrixMode(GL11.GL_PROJECTION);
         GL11.glLoadIdentity();
         GLU.gluPerspective(45.0f, ((float)w/(float)h),0.1f,100.0f);
         GL11.glMatrixMode(GL11.GL_MODELVIEW);
         GL11.glLoadIdentity();
         GL11.glShadeModel(GL11.GL_SMOOTH);
         GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
         GL11.glClearDepth(1.0f);
         GL11.glEnable(GL11.GL_DEPTH_TEST);
         GL11.glDepthFunc(GL11.GL_LEQUAL);
         GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT,
         GL11.GL_NICEST);
         
         box  = new Box();*/
     	
     	
 
         // Enable z- (depth) buffer for hidden surface removal.
         GL11.glEnable(GL11.GL_DEPTH_TEST);
         GL11.glDepthFunc(GL11.GL_LEQUAL);
 
         GL11.glEnable(GL11.GL_LINE_SMOOTH);
         GL11.glEnable(GL11.GL_POINT_SMOOTH);
 
         if (polygonOffset) {
             GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
             GL11.glPolygonOffset(1.0f, 1.0f);
         }
         GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
         GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
 
         GL11.glClearDepth(1.0); // Enables Clearing Of The Depth Buffer
         GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
         GL11.glEnable(GL11.GL_BLEND);
 
         // Enable smooth shading.
         GL11.glShadeModel(GL11.GL_SMOOTH);
 
         // Define "clear" color.
         GL11.glClearColor(0f, 0f, 0.4f, 0f);
 
         // We want a nice perspective.
         GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
         GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
         GL11.glFlush();
 
         context.getTextureManager().clearCache();
         initied = true;
         
         new LWJGLBinding();
     }
 
     private int frameMesureCount = 0;
     private float lastFps = 0;
     private long lastFpsMesureTime = 0;
     
     public void start() throws LWJGLException {
 		
 		
 		final long NANO_IN_MILLI = 1000000;
         long currentTime = System.nanoTime()/NANO_IN_MILLI;
         lastFpsMesureTime = currentTime;
        
 		
 		while(!done){
 			
 			if(Display.isCloseRequested())
 			done=true;
 			display();
 			Display.update();
 			frameMesureCount++;
 			currentTime = System.nanoTime()/NANO_IN_MILLI;
             updateFpsCounter(currentTime);
 		}
 
 		Display.destroy();
 
 	}
     
     private void updateFpsCounter(long currentTime) {
         if(currentTime > lastFpsMesureTime+1000) {
             lastFps = (float) (((double) frameMesureCount) / ((double) (currentTime-lastFpsMesureTime) / 1000.0));
             System.out.println("Fps : "+ lastFps);
             frameMesureCount = 0;
             lastFpsMesureTime = currentTime;
 
         }            
     }
     
     
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     public boolean isEnabled() {
         return enabled;
     }
 
     public boolean isInitied() {
         return initied;
     }
 
     /**
      * The only method that you should implement by yourself.
      *
      * @see javax.media.openGL11.GLEventListener#display(javax.media.openGL11.GLAutoDrawable)
      */
     public void display() {
 
         if (showFps) {
             //int fps = (int) animator.getFps();
             //Graphics g = canvas.getGraphics();
             //g.setColor(Color.yellow);
             //g.drawString("" + fps + " fps", 10, 20);*/
         	//TODO repair
             
             //fps
         }
 
 
         GL11.glClear(GL11.GL_ACCUM_BUFFER_BIT);
 
         int i = 0;
         for (V3DCameraBinding binding : cameraList) {
 
             GL11.glViewport(binding.x, binding.y, binding.width, binding.height);
 
             //Clean Background
             V3DColor color = binding.camera.getBackgroundColor();
             GL11.glClearColor(color.r, color.g, color.b, color.a);
 
             GL11.glScissor(binding.x, binding.y, binding.width, binding.height);
             GL11.glEnable(GL11.GL_SCISSOR_TEST);
             if (color.a == 1.0f) {
                 GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
             } else {
                 GL11.glMatrixMode(GL11.GL_PROJECTION);
                 GL11.glLoadIdentity();
                 GL11.glMatrixMode(GL11.GL_MODELVIEW);
                 GL11.glLoadIdentity();
                 GL11.glOrtho(0, binding.width, 0, binding.height, -2000.0, 2000.0);
 
                 GL11.glDisable(GL11.GL_DEPTH_TEST);
                 GL11.glColor4f(color.r, color.g, color.b, color.a);
                 GL11.glBegin(GL11.GL_QUADS);
                 GL11.glVertex3f(0, 0, 0);
                 GL11.glVertex3f(binding.width, 0, 0);
                 GL11.glVertex3f(binding.width, binding.height, 0);
                 GL11.glVertex3f(0, binding.height, 0);
                 GL11.glEnd();
                 GL11.glEnable(GL11.GL_DEPTH_TEST);
                 GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
             }
             GL11.glDisable(GL11.GL_SCISSOR_TEST);
 
             binding.camera.display( binding.width, binding.height);
 
             GL11.glDisable(GL11.GL_DEPTH_TEST);
             binding.getGui().display();
             GL11.glEnable(GL11.GL_DEPTH_TEST);
 
             if (select && binding == focusCamera) {
                 GL11.glMatrixMode(GL11.GL_PROJECTION);
                 GL11.glLoadIdentity();
                 //glu.gluPerspective(45.0f, h, 0.1, 2000.0);
                 GL11.glMatrixMode(GL11.GL_MODELVIEW);
                 GL11.glLoadIdentity();
                 binding.camera.select( mouseX, mouseY);
                 context.setMouseOverCameraBinding(binding);
             }
             i++;
         }
         GL11.glFlush();
         select = false;
     }
 
     /**
      * Resizes the screen.
      *
      * @see javax.media.openGL11.GLEventListener#reshape(javax.media.openGL11.GLAutoDrawable,
      *      int, int, int, int)
      */
     public void reshape(int x, int y, int width, int height) {
         for (V3DCameraBinding binding : cameraList) {
             binding.compute(width, height);
         }
         fireResized();
         select(mouseX, mouseY);
     }
 
     /**
      * Changing devices is not supported.
      *
      * @see javax.media.openGL11.GLEventListener#displayChanged(javax.media.openGL11.GLAutoDrawable,
      *      boolean, boolean)
      */
     public void displayChanged( boolean modeChanged, boolean deviceChanged) {
         throw new UnsupportedOperationException("Changing display is not supported.");
     }
     List<V3DCameraBinding> cameraList = new ArrayList<V3DCameraBinding>();
 
     public void addCamera(V3DCameraBinding camera) {
         cameraList.add(camera);
         camera.compute(getWidth(), getHeight());
         select(mouseX, mouseY);
     }
 
     private int getWidth() {
 		return width;
 	}
 
 	private int getHeight() {
 		return height;
 	}
 
 	public void select(int x, int y) {
         mouseX = x;
         mouseY = getHeight() - y;
 
         for (int i = cameraList.size() - 1; i >= 0; i--) {
             V3DCameraBinding binding = cameraList.get(i);
             if (binding.contains(mouseX, mouseY)) {
                 focusCamera = binding;
                 break;
             }
         }
         select = true;
     }
 
     /*private void initListeners() {
 
         canvas.addHierarchyListener(new HierarchyListener() {
 
             @Override
             public void hierarchyChanged(HierarchyEvent e) {
                 if (canvas.isDisplayable()) {
                     animator.addCanvas(V3DCanvas.this);
                 } else {
                     animator.removeCanvas(V3DCanvas.this);
                 }
             }
         });
 
         canvas.addKeyListener(new KeyListener() {
 
             private void onKeyEvent(KeyEvent e) {
 
                 for (V3DCameraBinding binding : cameraList) {
                     if (binding.isFocused()) {
                         binding.camera.onEvent(e);
                         return;
                     }
                 }
                 // If no camera has the focus, send the event to all camera
                 for (V3DCameraBinding binding : cameraList) {
                     binding.camera.onEvent(e);
                 }
             }
 
             @Override
             public void keyTyped(KeyEvent e) {
                 onKeyEvent(e);
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
                 onKeyEvent(e);
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
                 onKeyEvent(e);
             }
         });
 
         canvas.addMouseWheelListener(new MouseWheelListener() {
 
             @Override
             public void mouseWheelMoved(MouseWheelEvent e) {
                 for (int i = cameraList.size() - 1; i >= 0; i--) {
                     V3DCameraBinding binding = cameraList.get(i);
                     if (binding.containsMouse(e.getX(), e.getY())) {
                         binding.camera.onEvent(e);
                         break;
                     }
                 }
                 select(e.getX(), e.getY());
             }
         });
 
         canvas.addMouseListener(new MouseListener() {
 
             private void dispatchIfContains(MouseEvent e) {
                 for (int i = cameraList.size() - 1; i >= 0; i--) {
                     V3DCameraBinding binding = cameraList.get(i);
 
                     MouseEvent localEvent = new MouseEvent(
                             e.getComponent(),
                             e.getID(),
                             e.getWhen(),
                             e.getModifiers(),
                             e.getX() - binding.mouseX,
                             e.getY() - binding.mouseY,
                             e.getClickCount(),
                             e.isPopupTrigger(),
                             e.getButton());
 
                     if (e.isConsumed()) {
                         localEvent.consume();
                     }
 
                     if (binding.containsMouse(e.getX(), e.getY())) {
                         if (!binding.getGui().containsPoint(e.getX(), e.getY())) {
 
                             binding.camera.onEvent(localEvent);
                         }
                         break;
                     }
 
                     if (localEvent.isConsumed()) {
                         e.consume();
                     }
                 }
             }
 
             private void dispatch(MouseEvent e) {
                 for (V3DCameraBinding binding : cameraList) {
 
                     MouseEvent localEvent = new MouseEvent(
                             e.getComponent(),
                             e.getID(),
                             e.getWhen(),
                             e.getModifiers(),
                             e.getX() - binding.mouseX,
                             e.getY() - binding.mouseY,
                             e.getClickCount(),
                             e.isPopupTrigger(),
                             e.getButton());
 
                     if (e.isConsumed()) {
                         localEvent.consume();
                     }
 
                     binding.camera.onEvent(localEvent);
 
                     if (localEvent.isConsumed()) {
                         e.consume();
                     }
                 }
             }
 
             @Override
             public void mouseClicked(MouseEvent e) {
                 dispatchIfContains(e);
             }
 
             @Override
             public void mousePressed(MouseEvent e) {
                 dispatchIfContains(e);
                 select(e.getX(), e.getY());
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
                 dispatch(e);
                 select(e.getX(), e.getY());
             }
 
             @Override
             public void mouseEntered(MouseEvent e) {
                 dispatch(e);
             }
 
             @Override
             public void mouseExited(MouseEvent e) {
                 dispatch(e);
             }
         });
 
         canvas.addMouseMotionListener(new MouseMotionListener() {
 
             private void dispatch(MouseEvent e) {
                 for (V3DCameraBinding binding : cameraList) {
                     MouseEvent localEvent = new MouseEvent(
                             e.getComponent(),
                             e.getID(),
                             e.getWhen(),
                             e.getModifiers(),
                             e.getX() - binding.mouseX,
                             e.getY() - binding.mouseY,
                             e.getClickCount(),
                             e.isPopupTrigger(),
                             e.getButton());
 
                     if (e.isConsumed()) {
                         localEvent.consume();
                     }
                     binding.setLastMousePosition(new Point(localEvent.getX(), localEvent.getY()));
                     binding.camera.onEvent(localEvent);
 
                     if (localEvent.isConsumed()) {
                         e.consume();
                     }
                 }
             }
 
             @Override
             public void mouseDragged(MouseEvent e) {
                 dispatch(e);
                 select(e.getX(), e.getY());
             }
 
             @Override
             public void mouseMoved(MouseEvent e) {
                 dispatch(e);
                 select(e.getX(), e.getY());
             }
         });
     }
     */
 	//TODO repair
 
     public void removeCamera(V3DCameraBinding binding) {
         cameraList.remove(binding);
     }
 
     public void addResizeListener(ResizeListener listener) {
         resizeListenerList.add(listener);
     }
 
     private void fireResized() {
         for (ResizeListener listener : resizeListenerList) {
             listener.resized(this);
         }
     }
 
     public boolean isShowFps() {
         return showFps;
     }
 
     public void setShowFps(boolean showFps) {
         this.showFps = showFps;
         animator.setComputeFps(showFps);
     }
 
     public boolean isPolygonOffset() {
         return polygonOffset;
     }
 
     /**
      * Si polygon offset est à true, les polygons seront dessinés légèrement
      * derrière leur possition normale. Cela permet de dessiner des lignes au
      * même niveau que les polygones sans bug graphique.
      * @param polygonOffset
      */
     public void setPolygonOffset(boolean polygonOffset) {
         this.polygonOffset = polygonOffset;
     }
 }
