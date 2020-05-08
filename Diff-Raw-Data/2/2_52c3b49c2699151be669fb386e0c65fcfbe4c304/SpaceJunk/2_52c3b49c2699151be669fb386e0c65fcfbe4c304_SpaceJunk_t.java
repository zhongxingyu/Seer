 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 
 package com.spacejunk;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.Random;
 import java.util.List;
 import java.util.ArrayList;
 import java.net.URL;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.font.effects.*;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.UnicodeFont;
 import de.matthiasmann.twl.utils.PNGDecoder;
 import com.spacejunk.sprites.*;
 import com.spacejunk.particles.*;
 import com.spacejunk.util.*;
 import com.spacejunk.pause.*;
 import org.newdawn.slick.openal.SoundStore;
 
 /**
  * Actual SpaceJunk game object, contains the main code that makes the game work.
  * @author Techjar
  */
 public class SpaceJunk {
     private static int DISPLAY_WIDTH, DISPLAY_HEIGHT, DIFFICULTY;
     private static boolean FULLSCREEN;
     private static DisplayMode DISPLAY_MODE;
     private int score, deaths, curLevel, nextRand, lastMouseX, lastMouseY, pauseScreen;
     private long lastAsteroid, time, startTime;
     private boolean mouseClicked;
     private String pauseHover;
     private UnicodeFont batmfa20, batmfa60;
     private SoundManager soundManager;
     private Random random = new Random();
     private Texture bg;
     private Texture[] atex;
     private List<Pause> pauseMenu;
     private List<Sprite> sprites;
     private List<Sprite2Asteroid> asteroids;
     private List<Sprite4Powerup> powerups;
     private List<Particle> particles;
     private TickCounter tc;
 
 
     /**
      * Creates a new SpaceJunk game instance.
      * @param width display width
      * @param height display height
      */
     public SpaceJunk(int difficulty, DisplayMode mode, boolean fullscreen, boolean vSync, float musicVolume, float soundVolume) throws LWJGLException, SlickException, FileNotFoundException, IOException {
         // Setup sound manager
         soundManager = new SoundManager();
         soundManager.setMusicVolume(musicVolume);
         soundManager.setSoundVolume(soundVolume);
 
         // Store these
         DIFFICULTY = difficulty;
         DISPLAY_WIDTH = mode.getWidth();
         DISPLAY_HEIGHT = mode.getHeight();
         FULLSCREEN = fullscreen;
         DISPLAY_MODE = mode;
 
         // Default stuff
         score = 0; deaths = 0; curLevel = 1; nextRand = 0; pauseScreen = 0;
         lastAsteroid = 0; time = 0; startTime = 0; pauseHover = "";
         sprites = new ArrayList<Sprite>(); asteroids = new ArrayList<Sprite2Asteroid>();
         particles = new ArrayList<Particle>();
         atex = new Texture[5];
         tc = new TickCounter(mode.getFrequency());
 
         // Display
         Display.setDisplayMode(mode);
         Display.setFullscreen(fullscreen);
         Display.setVSyncEnabled(vSync);
         Display.setTitle("Junk from Outer Space");
 
         // Setup icons
         ByteBuffer[] icons = new ByteBuffer[4];
         icons[0] = createIcon(new File("resources/icons/16.png").toURI().toURL());
         icons[1] = createIcon(new File("resources/icons/32.png").toURI().toURL());
         icons[2] = createIcon(new File("resources/icons/32.png").toURI().toURL());
         icons[3] = createIcon(new File("resources/icons/128.png").toURI().toURL());
         Display.setIcon(icons);
 
         // Create display
         Display.create();
 
         // Load fonts
         batmfa20 = new UnicodeFont("resources/fonts/batmfa_.ttf", 20, false, false);
         batmfa20.addAsciiGlyphs();
         batmfa20.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
         batmfa20.getEffects().add(new OutlineEffect(1, java.awt.Color.LIGHT_GRAY));
         //batmfa20.getEffects().add(new ShadowEffect(java.awt.Color.DARK_GRAY, 3, 3, 0.3F));
         batmfa20.loadGlyphs();
 
         batmfa60 = new UnicodeFont("resources/fonts/batmfa_.ttf", 60, false, false);
         batmfa60.addAsciiGlyphs();
         batmfa60.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
         batmfa60.getEffects().add(new OutlineEffect(2, java.awt.Color.LIGHT_GRAY));
         batmfa60.loadGlyphs();
 
         // Keyboard
         Keyboard.create();
 
         // Mouse
         Mouse.setGrabbed(true);
         Mouse.create();
 
         /*batmfa20 = new UnicodeFont("resources/fonts/batmfa_.ttf", 20, false, false);
         batmfa20.addAsciiGlyphs();
         batmfa20.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
         batmfa20.loadGlyphs();*/
 
         // Setup pause menu
         pauseMenu = new ArrayList<Pause>();
         pauseMenu.add(new Pause2Resume(batmfa20, Color.white, soundManager, this));
         pauseMenu.add(new Pause0Options(batmfa20, Color.white, soundManager, this));
         pauseMenu.add(new Pause1Quit(batmfa20, Color.white, soundManager, this));
     }
 
     /**
      * Sets up this SpaceJunk instance.
      */
     public void create() {
         // Init
         this.init();
 
         // OpenGL
         this.initGL();
         this.resizeGL();
     }
 
     /**
      * Destroys this SpaceJunk instance.
      */
     public void destroy() {
         Mouse.destroy();
         Keyboard.destroy();
         Display.destroy();
     }
 
     /**
      * Runs this SpaceJunk instance.
      */
     public void run() {
         while(!Display.isCloseRequested()) {
             if(Display.isVisible()) {
                 this.processKeyboard();
                 this.processMouse();
                 this.update();
                 this.render();
             }
             else {
                 if(Display.isDirty()) {
                     this.render();
                 }
                 try {
                     Thread.sleep(100);
                 }
                 catch(InterruptedException e) {
                 }
             }
             Display.update();
            Display.sync(Display.getDisplayMode().getFrequency() <= 0 ? 60 : Display.getDisplayMode().getFrequency());
         }
     }
 
     private void init() {
         try {
             bg = TextureLoader.getTexture("JPG", new FileInputStream("resources/textures/bg.jpg"), GL_LINEAR);
             atex[0] = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/asteroid0.png"), GL_LINEAR);
             atex[1] = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/asteroid1.png"), GL_LINEAR);
             atex[2] = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/asteroid2.png"), GL_LINEAR);
             atex[3] = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/asteroid3.png"), GL_LINEAR);
             atex[4] = TextureLoader.getTexture("PNG", new FileInputStream("resources/textures/asteroid4.png"), GL_LINEAR);
             sprites.add(new Sprite0Ship(sprites, particles, 48, 0, soundManager, this));
             soundManager.playRandomMusic();
             startTime = tc.getTickMillis();
         }
         catch(Exception e) {
             e.printStackTrace();
             System.exit(0);
         }
     }
 
     private void initGL() {
         // 2D Initialization
         glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
         glDisable(GL_DEPTH_TEST);
         glDisable(GL_LIGHTING);
         glEnable(GL_TEXTURE_2D);
         glEnable(GL_ALPHA_TEST);
         glEnable(GL_BLEND);
         glAlphaFunc(GL_GREATER, 0);
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
     }
 
     private void resizeGL() {
         // 2D Scene
         glViewport(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);
 
         glMatrixMode(GL_PROJECTION);
         glLoadIdentity();
         gluOrtho2D(0.0f, DISPLAY_WIDTH, DISPLAY_HEIGHT, 0.0f);
         glPushMatrix();
 
         glMatrixMode(GL_MODELVIEW);
         glLoadIdentity();
         glPushMatrix();
     }
 
     private void processKeyboard() {
         while(Keyboard.next() && Keyboard.getEventKeyState()) {
             if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                 if(Mouse.isGrabbed()) {
                     lastMouseX = Mouse.getX();
                     lastMouseY = Mouse.getY();
                 }
                 else {
                     Mouse.setCursorPosition(lastMouseX, lastMouseY);
                 }
 
                 Mouse.setGrabbed(!Mouse.isGrabbed());
                 if(Mouse.isGrabbed()) {
                     pauseScreen = 0;
                 }
             }
             // Old F-key volume code, replaced by much nicer options GUI.
             /*if(Keyboard.getEventKey() == Keyboard.KEY_F5) soundManager.setSoundVolume(soundManager.getSoundVolume() - 0.1F);
             if(Keyboard.getEventKey() == Keyboard.KEY_F6) soundManager.setSoundVolume(soundManager.getSoundVolume() + 0.1F);
             if(Keyboard.getEventKey() == Keyboard.KEY_F7) soundManager.setMusicVolume(soundManager.getMusicVolume() - 0.1F);
             if(Keyboard.getEventKey() == Keyboard.KEY_F8) soundManager.setMusicVolume(soundManager.getMusicVolume() + 0.1F);
             if(Keyboard.getEventKey() == Keyboard.KEY_F9) soundManager.playRandomMusic();
             if(Keyboard.getEventKey() == Keyboard.KEY_F10) soundManager.stopMusic();
             if(Keyboard.getEventKey() == Keyboard.KEY_F11) changeDisplayMode(FULLSCREEN ? DISPLAY_MODE : Display.getDesktopDisplayMode(), !FULLSCREEN);*/
             if(Keyboard.getEventKey() == Keyboard.KEY_F1) soundManager.playRandomMusic();
         }
     }
 
     private void processMouse() {
     }
 
     private void render() {
         glClear(GL_COLOR_BUFFER_BIT);
         glLoadIdentity();
 
         drawBg();
         Sprite sprite = null;
         for(int i = 0; i < sprites.size(); i++) {
             sprite = sprites.get(i);
             sprite.render();
         }
 
         Particle particle = null;
         for(int i = 0; i < particles.size(); i++) {
             particle = particles.get(i);
             particle.render();
         }
 
         try {
             glPushMatrix();
             batmfa20.drawString(5, 1, "Time: " + time, Color.yellow);
             batmfa20.drawString(5, 21, "Level: " + curLevel, Color.yellow);
             batmfa20.drawString(5, 41, "Score: " + score, Color.yellow);
             batmfa20.drawString(5, 61, "Deaths: " + deaths, Color.yellow);
             glPopMatrix();
         }
         catch(Exception e) {
             e.printStackTrace();
         }
 
         if(!Mouse.isGrabbed()) processPause();
     }
 
     private void update() {
         if(!SoundStore.get().isMusicPlaying()) soundManager.playRandomMusic();
         if(Mouse.isGrabbed()) {
             tc.incTicks();
             time = (tc.getTickMillis() - startTime) / 1000;
             generateAsteroid();
             generatePowerup();
 
             Sprite sprite = null;
             for(int i = 0; i < sprites.size(); i++) {
                 sprite = sprites.get(i);
                 sprite.update();
                 processCollision(sprite);
             }
             for(int i = 0; i < sprites.size(); i++) {
                 sprite = sprites.get(i);
                 if(!(sprite instanceof Sprite0Ship)) {
                     if(!sprite.isVisible()) sprites.remove(sprite);
                     if(!sprite.isVisible() && sprite instanceof Sprite2Asteroid) asteroids.remove((Sprite2Asteroid)sprite);
                 }
             }
 
             Particle particle = null;
             for(int i = 0; i < particles.size(); i++) {
                 particle = particles.get(i);
                 particle.update();
             }
             for(int i = 0; i < particles.size(); i++) {
                 particle = particles.get(i);
                 if(!(particle instanceof Particle1Jet)) {
                     if(!particle.isVisible()) particles.remove(particle);
                 }
             }
         }
         
         if(time >= 2960) curLevel = 50;
         else if(time >= 2900) curLevel = 49;
         else if(time >= 2840) curLevel = 48;
         else if(time >= 2780) curLevel = 47;
         else if(time >= 2720) curLevel = 46;
         else if(time >= 2660) curLevel = 45;
         else if(time >= 2600) curLevel = 44;
         else if(time >= 2540) curLevel = 43;
         else if(time >= 2480) curLevel = 42;
         else if(time >= 2420) curLevel = 41;
         else if(time >= 2360) curLevel = 40;
         else if(time >= 2300) curLevel = 39;
         else if(time >= 2240) curLevel = 38;
         else if(time >= 2160) curLevel = 37;
         else if(time >= 2100) curLevel = 36;
         else if(time >= 2040) curLevel = 35;
         else if(time >= 1980) curLevel = 34;
         else if(time >= 1920) curLevel = 33;
         else if(time >= 1860) curLevel = 32;
         else if(time >= 1800) curLevel = 31;
         else if(time >= 1740) curLevel = 30;
         else if(time >= 1680) curLevel = 29;
         else if(time >= 1620) curLevel = 28;
         else if(time >= 1560) curLevel = 27;
         else if(time >= 1500) curLevel = 26;
         else if(time >= 1440) curLevel = 25;
         else if(time >= 1380) curLevel = 24;
         else if(time >= 1320) curLevel = 23;
         else if(time >= 1260) curLevel = 22;
         else if(time >= 1200) curLevel = 21;
         else if(time >= 1140) curLevel = 20;
         else if(time >= 1020) curLevel = 19;
         else if(time >= 960) curLevel = 18;
         else if(time >= 900) curLevel = 17;
         else if(time >= 840) curLevel = 16;
         else if(time >= 780) curLevel = 15;
         else if(time >= 720) curLevel = 14;
         else if(time >= 660) curLevel = 13;
         else if(time >= 600) curLevel = 12;
         else if(time >= 540) curLevel = 11;
         else if(time >= 480) curLevel = 10;
         else if(time >= 420) curLevel = 9;
         else if(time >= 360) curLevel = 8;
         else if(time >= 300) curLevel = 7;
         else if(time >= 240) curLevel = 6;
         else if(time >= 180) curLevel = 5;
         else if(time >= 120) curLevel = 4;
         else if(time >= 60) curLevel = 3;
         else if(time >= 30) curLevel = 2;
 
         soundManager.poll();
     }
 
     private void drawBg() {
         // store the current model matrix
 	glPushMatrix();
 
 	// bind to the appropriate texture for this sprite
         bg.bind();
 
 	// translate to the right location and prepare to draw
 	glTranslatef(0, 0, 0);
     	glColor3f(1, 1, 1);
 
 	// draw a quad textured to match the sprite
     	glBegin(GL_QUADS);
             glTexCoord2f(0, 0); glVertex2f(0, 0);
 	    glTexCoord2f(0, bg.getHeight()); glVertex2f(0, DISPLAY_HEIGHT);
 	    glTexCoord2f(bg.getWidth(), bg.getHeight()); glVertex2f(DISPLAY_WIDTH, DISPLAY_HEIGHT);
 	    glTexCoord2f(bg.getWidth(), 0); glVertex2f(DISPLAY_WIDTH, 0);
 	glEnd();
 
 	// restore the model view matrix to prevent contamination
 	glPopMatrix();
     }
 
     private void processPause() {
         // store the current model matrix
 	glPushMatrix();
 
 	// disable textures
         glDisable(GL_TEXTURE_2D);
 
 	// translate to the right location and prepare to draw
 	glTranslatef(0, 0, 0);
     	glColor4f(0, 0, 0, 0.8F);
 
 	// draw a quad textured to match the sprite
     	glBegin(GL_QUADS);
             glTexCoord2f(0, 0); glVertex2f(0, 0);
 	    glTexCoord2f(0, 1); glVertex2f(0, DISPLAY_HEIGHT);
 	    glTexCoord2f(1, 1); glVertex2f(DISPLAY_WIDTH, DISPLAY_HEIGHT);
 	    glTexCoord2f(1, 0); glVertex2f(DISPLAY_WIDTH, 0);
 	glEnd();
 
         // enable textures
         glEnable(GL_TEXTURE_2D);
         
         // mouse processing
         Bounds mouse = new Bounds(Mouse.getX(), DISPLAY_HEIGHT - Mouse.getY(), 1, 1);
 
         // draw text
         batmfa60.drawString((DISPLAY_WIDTH - batmfa60.getWidth("PAUSED")) / 2, 25, "PAUSED", Color.red);
         Pause pause = null;
         if(pauseScreen == 0) {
             int iCount = 0;
             for(int i = 0; i < pauseMenu.size(); i++) {
                 pause = pauseMenu.get(i);
                 pause.setTextY(120 + (30 * i));
                 if(mouse.intersects(pause.getBounds())) {
                     iCount++;
                     if(!pauseHover.equals(pause.getText())) {
                         soundManager.playSoundEffect("ui.button.rollover", false);
                         pauseHover = pause.getText();
                     }
                     if(Mouse.isButtonDown(0) && !mouseClicked) {
                         soundManager.playSoundEffect("ui.button.click", false);
                         pauseScreen = i + 1;
                         if(pause instanceof Pause2Resume) ((Pause2Resume)pause).setLastMouse(lastMouseX, lastMouseY);
                         pause.setMouseClicked(true);
                         pause.setActive(true);
                         pause.renderScreen();
                         break;
                     }
                     pause.setColor(Color.red.addToCopy(new Color(0, 50, 50)));
                     pause.render();
                 }
                 else {
                     pause.setColor(Color.red);
                     pause.render();
                 }
             }
             if(iCount <= 0) pauseHover = "";
         }
         else {
             pause = pauseMenu.get(pauseScreen - 1);
             if(!pause.isActive()) {
                 pauseScreen = 0;
                 mouseClicked = true;
             }
             else pause.renderScreen();
         }
         if(!Mouse.isButtonDown(0) && mouseClicked) mouseClicked = false;
 
 	// restore the model view matrix to prevent contamination
 	glPopMatrix();
     }
 
     private void generateAsteroid() {
         if((tc.getTickMillis() - lastAsteroid) >= nextRand) {
             Sprite2Asteroid newSprite = new Sprite2Asteroid(sprites, particles, soundManager, DISPLAY_WIDTH + 64, random.nextInt(DISPLAY_HEIGHT), this, atex[random.nextInt(5)]);
             sprites.add(newSprite); asteroids.add(newSprite);
             lastAsteroid = tc.getTickMillis();
             nextRand = random.nextInt(MathHelper.clamp(10000 / DIFFICULTY, 1, 10000)) / curLevel;
         }
     }
 
     private void generatePowerup() {
         if(random.nextInt(1000) == random.nextInt(1000)) {
             //Sprite newSprite = new Sprite4Powerup();
             //sprites.add(newSprite);
         }
     }
 
     private void processCollision(Sprite sprite) {
         if(sprite instanceof Sprite2Asteroid || sprite instanceof Sprite4Powerup) return;
         for(int i = 0; i < asteroids.size(); i++) {
             Sprite2Asteroid asteroid = asteroids.get(i);
             Bounds b1 = sprite.getBounds(); Bounds b2 = asteroid.getBounds();
             if(sprite instanceof Sprite0Ship) {
                 if(!((Sprite0Ship)sprite).isHit() && !((Sprite0Ship)sprite).isInvincible() && b1.intersects(b2)) {
                     deaths++;
                     this.decScore(DIFFICULTY * 2);
                     ((Sprite0Ship)sprite).hit();
                 }
             }
             else if(sprite instanceof Sprite1Gunfire) {
                 if(sprite.isVisible()) {
                     if(b1.intersects(b2)) {
                         asteroid.hit(1);
                         sprite.setVisible(false);
                     }
                 }
             }
         }
         /*for(int i = 0; i < powerups.size(); i++) {
             Sprite4Powerup powerup = powerups.get(i);
             Bounds b1 = sprite.getBounds(); Bounds b2 = powerup.getBounds();
             if(sprite instanceof Sprite0Ship) {
                 if(!((Sprite0Ship)sprite).isHit() && !((Sprite0Ship)sprite).isInvincible() && b1.intersects(b2)) {
                     deaths++;
                     this.decScore(DIFFICULTY * 2);
                     ((Sprite0Ship)sprite).hit();
                 }
             }
         }*/
     }
 
     public int getScore() {
         return score;
     }
 
     public void setScore(int score) {
         this.score = score;
     }
 
     public void incScore(int i) {
         score += i;
     }
 
     public void decScore(int i) {
         score -= i;
         if(score < 0) score = 0;
     }
 
     private static ByteBuffer createIcon(URL url) throws IOException {
         InputStream is = url.openStream();
         try {
             PNGDecoder decoder = new PNGDecoder(is);
             ByteBuffer bb = ByteBuffer.allocateDirect(decoder.getWidth() * decoder.getHeight() * 4);
             decoder.decode(bb, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
             bb.flip();
             return bb;
         } finally {
             is.close();
         }
     }
 
     public TickCounter getTickCounter() {
         return this.tc;
     }
 
     public int getDifficulty() {
         return DIFFICULTY;
     }
 
     public void setDifficulty(int difficulty) {
         DIFFICULTY = difficulty;
     }
 
     public void changeDisplayMode(DisplayMode mode, boolean fullscreen) {
         try {
             if(fullscreen) {
                 Display.setDisplayMode(mode);
                 Display.setFullscreen(true);
             }
             else {
                 Display.setFullscreen(false);
                 Display.setDisplayMode(mode);
             }
             DISPLAY_WIDTH = Display.getDisplayMode().getWidth();
             DISPLAY_HEIGHT = Display.getDisplayMode().getHeight();
             FULLSCREEN = fullscreen;
             resizeGL();
         }
         catch(Exception e) {
             e.printStackTrace();
         }
     }
 
     public void clearAsteroids() {
         Sprite sprite;
         for(int i = 0; i < sprites.size(); i++) {
             sprite = sprites.get(i);
             if(sprite instanceof Sprite2Asteroid) sprite.setVisible(false);
         }
     }
 }
