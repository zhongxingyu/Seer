 package com.voracious.dragons.client;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.image.BufferStrategy;
 
 import org.apache.log4j.Logger;
 
 import com.voracious.dragons.client.graphics.Screen;
 import com.voracious.dragons.client.screens.LoginScreen;
 
 public class Game extends Canvas implements Runnable {
     private static final long serialVersionUID = 1L;
 
     public static final String NAME = "Here There Be Dragons";
     public static final int WIDTH = 720;
     public static final int HEIGHT = 480;
     public static final int FPS = 60;
     public static final Color background = Color.white;
     private static final boolean printFPS = true;
 
     private static Logger logger = Logger.getLogger(Game.class);
     private static Thread thread;
     private static boolean running = false;
     private static Screen currentScreen;
     private static Object lock = new Object();
     
     private static ClientConnectionManager ccm;
     
     public Game() {
         this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
     }
 
     public void render() {
         BufferStrategy bs = this.getBufferStrategy();
         if(bs == null){
             this.createBufferStrategy(2);
             return;
         }
         
         Graphics2D g = (Graphics2D) bs.getDrawGraphics();
         
         g.clearRect(0, 0, WIDTH, HEIGHT);
         
         synchronized(lock) {
         	currentScreen.draw(g);
         }
         
         g.dispose();
         bs.show();
     }
 
     public void tick() {
     	synchronized(lock) {
     		currentScreen.tick();
     	}
     }
 
     @Override
     public void run() {
         double unprocessedTicks = 0.0;
         double nsPerTick = 1000000000.0/FPS;
         long lastTime = System.nanoTime();
         boolean needsRender = true;
         
         long lastFpsTime = System.currentTimeMillis();
         int frameCount = 0;
         int tickCount = 0;
         
         while(running){
             long now = System.nanoTime();
             unprocessedTicks += (now - lastTime) / nsPerTick;
             lastTime = now;
             
             if(unprocessedTicks >= 1.0){
                 tick();
                 unprocessedTicks -= 1.0;
                 needsRender = true;
                 tickCount++;
             }
             
             if(needsRender){
                 render();
                 needsRender = false;
                 frameCount++;
             }
             
             if(printFPS && System.currentTimeMillis() - lastFpsTime > 1000){
                 System.out.println(frameCount + " fps; " + tickCount + " tps");
                 frameCount = 0;
                 tickCount = 0;
                 lastFpsTime = System.currentTimeMillis();
             }
         }
     }
     
     /**
      * Sets the screen to draw
      * 
      * @param s the screen to draw
      */
     public static void setCurrentScreen(Screen s){
     	synchronized(lock){
     		if(currentScreen != null){
     			currentScreen.stop();
     		}
     		currentScreen = s;
     		s.start();
     	}
     }
     
     public static void connect(String hostname, int port){
     	ccm = new ClientConnectionManager(hostname, port);
     }
     
     public static ClientConnectionManager getClientConnectionManager(){
     	if(ccm == null){
     		throw new RuntimeException("Connection not yet established");
     	}
     	return ccm;
     }
 
     public void init() {
         logger.debug("init");
         this.setBackground(background);
         Game.setCurrentScreen(new LoginScreen());
         thread = new Thread(this);
     }
 
     public void start() {
         logger.debug("start");
         running = true;
         thread.start();
     }
 
     public void stop() {
         logger.debug("stop");
        if(ccm != null){
        	ccm.close();
        }
         running = false;
     }
 }
