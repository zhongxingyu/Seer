 package com.zachnickell.platform;
 
 import javax.sound.sampled.AudioInputStream;
 //import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 //import javax.sound.sampled.LineUnavailableException;
 //import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.*;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.*;
 import org.lwjgl.util.glu.GLU;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import com.zachnickell.platform.entity.Entity;
 import com.zachnickell.platform.gfx.Sprites;
 import com.zachnickell.platform.level.IntroCell;
 import com.zachnickell.platform.level.Level;
 import com.zachnickell.platform.level.creator.LevelCreator;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Graphics;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 //import java.io.IOException;
 
 public class Platform extends Canvas{//implements Runnable {
 	private static final long serialVersionUID = 1L;
 	public static boolean running = false;
 	public static final String NAME = "Platform-r2";
	public static final String VERSION = "Alpha 0.0.1a";
 	public static final int WIDTH = 320;
 	public static final int HEIGHT = 240;
 	public static final int SCALE = 2;
 	public static int FRAMES = 0;
 	private Graphics dbg;
 	private BufferedImage img = new BufferedImage(WIDTH, HEIGHT,
 			BufferedImage.TYPE_INT_RGB);
 	public static long lastDeltaTime;
 
 	private Input input;
 	private Level level;
 	Sprites sprites;
 
 	public boolean songPlaying = true;
 
 	AudioInputStream audioIn;
 	Clip clip;
 
 	public static String getTitle() {
 		String title = NAME + " " + VERSION;
 		return title;
 	}
 
 	public int getDelta() {
 		int delta = (int) (System.currentTimeMillis() - lastDeltaTime);
 		lastDeltaTime = System.currentTimeMillis();
 		return delta;
 	}
 
 	public void start() {
 		if (!running) {
 			/*try {
 				audioIn = AudioSystem.getAudioInputStream(Platform.class
 						.getResource("/music.wav"));
 				clip = AudioSystem.getClip();
 				clip.open(audioIn);
 				clip.loop(Clip.LOOP_CONTINUOUSLY);
 				clip.start();
 			} catch (LineUnavailableException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (UnsupportedAudioFileException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}*/
 			getDelta();
 			try {
 				Display.setDisplayMode(new DisplayMode(WIDTH * SCALE, HEIGHT * SCALE));
 				Display.setTitle(getTitle());
 				Display.setVSyncEnabled(false);
 				Display.create();
 			} catch (LWJGLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				System.exit(0);
 			}
 			
 			input = new Input();
 			//addKeyListener(input);
 			//addMouseListener(input);
 			//addMouseMotionListener(input);
 			running = true;
 			//requestFocus();
 			sprites = new Sprites();
 			LevelCreator levelCreator = new LevelCreator();
 			level = new Level(levelCreator);//new IntroCell(0, 0);
 			//new LevelCreator();
 			new Entity().init(level);
 			//Mouse.setGrabbed(true);
 			//new Thread(this).start();
 			GL11.glEnable(GL11.GL_TEXTURE_2D);
 			
 			GL11.glClearColor(0f, 0f, 0f, 0f);
 				GL11.glEnable(GL11.GL_BLEND);
 				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 			GL11.glMatrixMode(GL11.GL_PROJECTION);
 			GL11.glLoadIdentity();
 			GL11.glOrtho(0, WIDTH, HEIGHT, 0, 1, -1);
 			GL11.glMatrixMode(GL11.GL_MODELVIEW);
 			
 			//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
 			
 			long lastTime = System.currentTimeMillis();
 			while (!Display.isCloseRequested()) {
 				int delta = getDelta();
 				//if (!hasFocus()) {
 				//	Input.releaseKeys();
 				//}
 				Input.checkInput();
 				update(delta);
 				render();
 				//drawScreen();
 				Display.update();
 				Display.sync(30);
 				if (Input.mutePressed) {
 					muteSong();
 				}
 				//if (System.currentTimeMillis() - lastTime >= 1000) {
 					//System.out.printf("fps: %d\n", FRAMES);
 					//lastTime += 1000;
 					//FRAMES = 0;
 				//}
 				try {
 					Thread.sleep(10);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			Display.destroy();
 		}
 	}
 
 	public void run() {
 		
 		try {
 			Display.makeCurrent();
 		} catch (LWJGLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		GL11.glEnable(GL11.GL_TEXTURE_2D);
 		
 		GL11.glClearColor(0f, 0f, 0f, 0f);
 			GL11.glEnable(GL11.GL_BLEND);
 			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 			GL11.glViewport(0, 0, WIDTH, HEIGHT);
 		
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GL11.glOrtho(0, WIDTH, 0, HEIGHT, 1, -1);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		
 		long lastTime = System.currentTimeMillis();
 		while (!Display.isCloseRequested()) {
 			int delta = getDelta();
 			//if (!hasFocus()) {
 			//	Input.releaseKeys();
 			//}
 			update(delta);
 			render();
 			//drawScreen();
 			Display.update();
 			Display.sync(80);
 			if (Input.mutePressed) {
 				muteSong();
 			}
 			//if (System.currentTimeMillis() - lastTime >= 1000) {
 				//System.out.printf("fps: %d\n", FRAMES);
 				//lastTime += 1000;
 				//FRAMES = 0;
 			//}
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		Display.destroy();
 	}
 
 	public void update(int delta) {
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 		level.update(delta);
 	}
 
 	public void render() {
 		//dbg = img.createGraphics();
 		//dbg.clearRect(0, 0, WIDTH, HEIGHT);
 		glLoadIdentity();
 		level.render();
 		//Toolkit.getDefaultToolkit().sync();
 		//dbg.dispose();
 	}
 
 	public void drawScreen() {
 		FRAMES++;
 		//Graphics g = getGraphics();
 		//g.drawImage(img, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);
 		//g.dispose();
 	}
 
 	public static void gameOver() {
 		System.out.println("dead.");
 		running = false;
 	}
 
 	long lastPress = System.currentTimeMillis();
 
 	public void muteSong() {
 		if (System.currentTimeMillis() - lastPress > 250) {
 			 lastPress = System.currentTimeMillis();
 			if (songPlaying) {
 				clip.stop();
 				songPlaying = false;
 			} else if (!songPlaying) {
 				clip.start();
 				songPlaying = true;
 			}
 		}
 
 	}
 
 	public static void main(String[] args) {
 		/*Platform platform = new Platform();
 		JFrame win = new JFrame(getTitle());
 		win.setLayout(new BorderLayout());
 		win.add(platform, BorderLayout.CENTER);
 		win.setSize(WIDTH * SCALE, HEIGHT * SCALE);
 		win.setResizable(false);
 		win.setLocationRelativeTo(null);
 		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		win.setVisible(true);*/
 		Platform platform = new Platform();
 		platform.start();
 	}
 }
