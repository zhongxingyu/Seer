    package org.ashpro.temporal;
    
    import java.io.*;
    import java.awt.*;
    import java.awt.event.*;
    import java.awt.image.*;
    import java.util.*;
    import com.threed.jpct.*;
    import com.threed.jpct.util.*;
    import org.ashpro.temporal.units.*;
    import tmp.pack.*;
 
    public class TemporalCollision {
    
       public static final double VERSION = 0.04;
       public static Random worldRandom = new Random();
       private boolean fullscreen=false;
       private boolean wireframe=false;
       private FrameBuffer buffer=null;
       private World theWorld=null;
       private TextureManager texMan=null;
       private Camera camera=null;
       private Object3D terrain=null;
       private Unit user=null;
       private Texture numbers=null;
       private int width=640;
       private int height=480;
       private Frame frame=null;
       private Graphics gFrame=null;
       private BufferStrategy bufferStrategy=null;
       private GraphicsDevice device=null;
       private int titleBarHeight=0;
       private int leftBorderWidth=0;
       private int fps;
       private int lastFps;
       private long totalFps;
       private int pps;
       private int lastPps;
       private boolean isIdle=false;
       private boolean exit=false;
       private float speed=0;
       private boolean left = false,right = false,forward = false,back = false;
       private KeyMapper keyMapper=null;
       private GLFont glFont = GLFont.getGLFont(new java.awt.Font("Dialog", Font.PLAIN, 12));
    
       public static void main(String[] args) {
          new TemporalCollision(args);
       }
       
       private TemporalCollision(String[] args) {
       
          Config.maxPolysVisible=10000;
       
          for (int i=0; i<args.length; i++) {
             if (args[i].equals("fullscreen")) {
                fullscreen=true;
                Config.glFullscreen=true;
             }
             if (args[i].equals("mipmap")) {
                Config.glMipmap=true;
             }
             if (args[i].equals("trilinear")) {
                Config.glTrilinear=true;
             }
          
             if (args[i].equals("16bit")) {
                Config.glColorDepth=16;
             }
             try {
                if (args[i].startsWith("width=")) {
                   width=Integer.parseInt(args[i].substring(6));
                }
                if (args[i].startsWith("height=")) {
                   height=Integer.parseInt(args[i].substring(7));
                }
                if (args[i].startsWith("refresh=")) {
                   Config.glRefresh=Integer.parseInt(args[i].substring(8));
                }
                if (args[i].startsWith("zbuffer=")) {
                   Config.glZBufferDepth=Integer.parseInt(args[i].substring(8));
                   if (Config.glZBufferDepth==16) {
                      Config.glFixedBlitting=true;
                   }
                }
             
             } 
                catch (Exception e) {
                // We don't care
                }
          }
       
          isIdle=false;
          totalFps=0;
          fps=0;
          lastFps=0;
       
          theWorld=new World();
          texMan=TextureManager.getInstance();
       
          Config.fadeoutLight=false;
          theWorld.getLights().setOverbrightLighting(Lights.OVERBRIGHT_LIGHTING_DISABLED);
          theWorld.getLights().setRGBScale(Lights.RGB_SCALE_2X);
          theWorld.setAmbientLight(255, 255, 255);
       
          theWorld.addLight(new SimpleVector(0, -150, 0), 25, 22, 19);
          theWorld.addLight(new SimpleVector(-1000, -150, 1000), 22, 5, 4);
          theWorld.addLight(new SimpleVector(1000, -150, -1000), 4, 2, 22);
       
          theWorld.setFogging(World.FOGGING_ENABLED);
          theWorld.setFogParameters(1200,-255,-255,-255);
          Config.farPlane=1200;
       
          char c=File.separatorChar;
       
          numbers=new Texture("resrc"+c+"textures"+c+"numbers.jpg");
          texMan.addTexture("numbers", numbers);
       
          Texture rocks=new Texture("resrc"+c+"textures"+c+"rocks.png");
          texMan.addTexture("rocks", rocks);
       
         Object3D[] objs=Loader.load3DS("resrc"+c+"models"+c+"terascene.3ds", 400);
          if (objs.length>0) {
             terrain=objs[0];
             terrain.setTexture("rocks");
          } 
          // terrain = generate(180,180,texMan);
          terrain.enableLazyTransformations();
          theWorld.addObject(terrain);
          user = new Unit();
          user.addToWorld(theWorld);
       
          terrain.build();
       
          SimpleVector pos=terrain.getCenter();
          pos.scalarMul(-1f);
          terrain.translate(pos);
          terrain.rotateX((float)-Math.PI/2f);
          terrain.translateMesh();
          terrain.rotateMesh();
          terrain.setTranslationMatrix(new Matrix()); 
          terrain.setRotationMatrix(new Matrix());
       
         terrain.createTriangleStrips(0);
       
          OcTree oc=new OcTree(terrain,50,OcTree.MODE_OPTIMIZED);
          terrain.setOcTree(oc);
          oc.setCollisionUse(OcTree.COLLISION_USE);
       
          Config.collideOffset=250;
       
          terrain.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
          user.setCollisionMode(Object3D.COLLISION_CHECK_SELF);
          terrain.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
          user.setCollisionOptimization(Object3D.COLLISION_DETECTION_OPTIMIZED);
          
          camera=theWorld.getCamera();
          camera.setPosition(0,-500,0);
          camera.lookAt(user.getTransformedCenter());
       
          Config.tuneForOutdoor();
       
          initializeFrame();
          Sound.loop(Sound.attack);
          gameLoop();
       }
    
       private void initializeFrame() {
          if (fullscreen) {
             GraphicsEnvironment env=GraphicsEnvironment.getLocalGraphicsEnvironment();
             device=env.getDefaultScreenDevice();
             GraphicsConfiguration gc=device.getDefaultConfiguration();
             frame=new Frame(gc);
             frame.setUndecorated(true);
             frame.setIgnoreRepaint(true);
             device.setFullScreenWindow(frame);
             if (device.isDisplayChangeSupported()) {
                device.setDisplayMode(new DisplayMode(width, height, 32, 0));
             }
             frame.createBufferStrategy(2);
             bufferStrategy=frame.getBufferStrategy();
             Graphics g=bufferStrategy.getDrawGraphics();
             bufferStrategy.show();
             g.dispose();
          } 
          else {
             frame=new Frame();
             frame.setTitle("Temporal Collision "+VERSION);
             frame.pack();
             Insets insets = frame.getInsets();
             titleBarHeight=insets.top;
             leftBorderWidth=insets.left;
             frame.setSize(width+leftBorderWidth+insets.right, height+titleBarHeight+insets.bottom);
             frame.setResizable(false);
             frame.show();
             gFrame=frame.getGraphics();
          }
       
          frame.addWindowListener(new WindowEvents());
          keyMapper=new KeyMapper(frame);
       }
    
       private void display() {
          glFont.blitString(buffer, "FPS: " + totalFps, 5, 12, 0, Color.WHITE);
          glFont.blitString(buffer, "PPS: " + lastPps, 5, 22, 0, Color.WHITE);
         //glFont.blitString(buffer, "Health: " + user.getHealth(), 5, 72, 0, Color.WHITE);
       
          if (!fullscreen) {
             buffer.display(gFrame, leftBorderWidth, titleBarHeight);
          } 
          else {
             Graphics g=bufferStrategy.getDrawGraphics();
             g.drawImage(buffer.getOutputBuffer(), 0, 0, null);
             bufferStrategy.show();
             g.dispose();
          }
       }
    
       private void moveUser() {
       
          if (left) {
             user.turnLeft();
          }
          if (right) {
             user.turnRight();
          }
          // if(user.isShieldOn())
             // return;
          Matrix backUpTrans=user.getTranslationMatrix().cloneMatrix();
       
          if (forward) {
             if (speed<10) {speed+=0.1f;}
             user.setSpeed(speed);
             user.moveForward();
          }
          if (back) {
             if (speed>0) {speed=0;}
             if (speed<0) {speed=0;
             }
             user.setSpeed(speed);
             user.moveForward();
          }
       
          if (speed>=0 && !back && !forward) {
             speed=0f;
             user.setSpeed(speed);
             user.moveForward();
          }
          boolean ok=user.place(terrain);
          if (!ok) {
             user.setTranslationMatrix(backUpTrans);
             speed=0;
             user.setSpeed(speed);
          }
       }
       private void moveCamera() {
          SimpleVector oldCamPos=camera.getPosition();
          SimpleVector oldestCamPos=new SimpleVector(oldCamPos);
          oldCamPos.scalarMul(9f);
          SimpleVector userCenter=user.getTransformedCenter();
          SimpleVector camPos=new SimpleVector(userCenter);
          SimpleVector zOffset=user.getZAxis();
          SimpleVector yOffset=new SimpleVector(0, -100, 0);
          zOffset.scalarMul(-250f);
          camPos.add(zOffset);
          camPos.add(yOffset);
          camPos.add(oldCamPos);
          camPos.scalarMul(0.1f);
          SimpleVector delta=camPos.calcSub(oldestCamPos);
          float len=delta.length();
          if (len!=0)
             theWorld.checkCameraCollisionEllipsoid(delta.normalize(), new SimpleVector(20, 20, 20), len, 3);
          camera.lookAt(user.getTransformedCenter());
       }
    
    
       private void gameLoop() {
          World.setDefaultThread(Thread.currentThread());
       
          buffer=new FrameBuffer(width, height, FrameBuffer.SAMPLINGMODE_NORMAL);
          buffer.enableRenderer(IRenderer.RENDERER_SOFTWARE);
          buffer.setBoundingBoxMode(FrameBuffer.BOUNDINGBOX_NOT_USED);
       
          buffer.optimizeBufferAccess();
       
          Timer timer=new Timer(25);
          timer.start();
       
          Timer fpsTimer=new Timer(1000);
          fpsTimer.start();
       
          while (!exit) {
             if (!isIdle) {
             
                long ticks=timer.getElapsedTicks();
                for (int i=0; i<ticks; i++) {
                   moveUser();
                   moveCamera();
                }
                
                poll();
             
                buffer.clear();
                theWorld.renderScene(buffer);
             
                if (!wireframe) {
                   theWorld.draw(buffer);
                }
                else {
                   theWorld.drawWireframe(buffer, Color.white);
                }
                buffer.update();
                display();
             
                fps++;
                pps+=theWorld.getVisibilityList().getSize();
             
                if (fpsTimer.getElapsedTicks()>0) {
                   totalFps=(fps-lastFps);
                   lastFps=fps;
                   lastPps=pps;
                   pps=0;
                }
             
                Thread.yield();
             
             } 
             else {
                try {
                   Thread.sleep(500);
                } 
                   catch (InterruptedException e) {}
             }
          }
       
          if (fullscreen) {
             device.setFullScreenWindow(null);
          }
          Sound.stop(Sound.attack);
          System.exit(0);
       }
    
       private void poll() {
          KeyState state=null;
          do {
             state=keyMapper.poll();
             if (state!=KeyState.NONE) {
                keyAffected(state);
             }
          } while (state!=KeyState.NONE);
       }
    
    
       private void keyAffected(KeyState state) {
          int code=state.getKeyCode();
          boolean event=state.getState();
       
          switch (code) {
             case (KeyEvent.VK_ESCAPE): 
                {
                   exit=event;
                   break;
                }
             case (KeyEvent.VK_LEFT): 
                {
                   left=event;
                   break;
                }
             case (KeyEvent.VK_RIGHT): 
                {
                   right=event;
                   break;
                }
             case (KeyEvent.VK_UP): 
                {
                   forward=event;
                   break;
                }
             case (KeyEvent.VK_DOWN): 
                {
                   back=event;
                   break;
                }
             case (KeyEvent.VK_W): 
                {
                   if (event) {
                      wireframe=!wireframe;
                   }
                   break;
                }
             case (KeyEvent.VK_SPACE) :
                {
                   if(event) {
                      user.toggleShield();
                   }
                   break;
                }
             case (KeyEvent.VK_P) :
                {
                   Sound.stop(Sound.attack);
                   break;
                }
          }
       }
    
    
    
       private class WindowEvents extends WindowAdapter {
       
          public void windowIconified(WindowEvent e) {
             isIdle=true;
          }
       
          public void windowDeiconified(WindowEvent e) {
             isIdle=false;
          }
          public void windowClosing(WindowEvent e) {
             exit=true;
          }
       }
    
    
       private class Timer {
       
          private long ticks=0;
          private long granularity=0;
       
          public Timer(int granularity) {
             this.granularity=granularity;
          }
       
          public void start() {
             ticks=System.currentTimeMillis();
          }
       
          public void reset() {
             start();
          }
       
          public long getElapsedTicks() {
             long cur=System.currentTimeMillis();
             long l=cur-ticks;
          
             if (l>=granularity) {
                ticks=cur-(l%granularity);
                return l/granularity;
             }
             return 0;
          }
       }
    	//World Generation Testing
       public static Object3D generate(final int X_SIZE, final int Z_SIZE, TextureManager texMan)
       {
          float[][] terrain = new float[X_SIZE][Z_SIZE];
          for (int x = 0; x < X_SIZE; x++) {
             for (int z = 0; z < Z_SIZE; z++) {
                terrain[x][z] = -20 + (float)(Math.random() * 40f);
             }
          }
          for (int x = 0; x < X_SIZE - 1; x++) {
             for (int z = 0; z < Z_SIZE - 1; z++) {
                terrain[x][z] = (terrain[x][z] + terrain[x + 1][z] + terrain[x][z + 1] + terrain[x + 1][z + 1]) / 4;
             }
          }
          Object3D ground = new Object3D(X_SIZE * Z_SIZE * 2);
          float xSizeF = (float) X_SIZE;
          float zSizeF = (float) Z_SIZE;
       
          int id = texMan.getTextureID("base");
       
          for (int x = 0; x < X_SIZE - 1; x++) {
             for (int z = 0; z < Z_SIZE - 1; z++) {
                TextureInfo ti = new TextureInfo(id, (x / xSizeF), (z / zSizeF), ((x + 1) / xSizeF), (z / zSizeF), (x / xSizeF), ((z + 1) / zSizeF));
                ground.addTriangle(new SimpleVector(x * 10, terrain[x][z], z * 10), new SimpleVector((x + 1) * 10, terrain[x + 1][z], z * 10),
                   new SimpleVector(x * 10, terrain[x][z + 1], (z + 1) * 10), ti);
             
                ti = new TextureInfo(id, (x / xSizeF), ((z + 1) / zSizeF), ((x + 1) / xSizeF), (z / zSizeF), ((x + 1) / xSizeF), ((z + 1) / zSizeF));
                ground.addTriangle(new SimpleVector(x * 10, terrain[x][z + 1], (z + 1) * 10), new SimpleVector((x + 1) * 10, terrain[x + 1][z], z * 10), new SimpleVector((x + 1) * 10,
                   terrain[x + 1][z + 1], (z + 1) * 10), ti);
             }
          }
          return ground;
       }
    }
