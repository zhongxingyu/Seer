 package com.avona.games.towerdefence.awt;
 
 import java.awt.Color;
 import java.awt.Frame;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 
 import com.avona.games.towerdefence.Game;
 import com.avona.games.towerdefence.GraphicsEngine;
 import com.avona.games.towerdefence.TimeTrack;
 import com.sun.opengl.util.FPSAnimator;
 
 public class MainLoop implements GLEventListener {
 	final private int EXPECTED_FPS = 60;
 
 	public Game game;
 	public GraphicsEngine ge;
 
 	private TimeTrack time;
 	private FPSAnimator animator;
 
 	public void performLoop() {
 		time.update(System.nanoTime() * Math.pow(10, -9));
 
 		// Update the world.
 		game.updateWorld(time.tick);
 		
 		// Show the world.
 		ge.render(time.tick);
 	}
 
 	public void exit() {
 		animator.stop();
 		System.exit(0);
 	}
 
 	public MainLoop() {
 		time = new TimeTrack();
 		
 		game = new Game(this);
 		ge = new GraphicsEngine(this, game);
 		ge.canvas.addGLEventListener(this);
 		game.init(ge);
 
 		final Frame frame = new Frame("Towerdefence");
 		frame.add(ge.canvas);
 		frame.setSize(ge.defaultWidth, ge.defaultHeight);
 		frame.setBackground(Color.WHITE);
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				exit();
 			}
 		});
 		frame.setVisible(true);
 
 		animator = new FPSAnimator(ge.canvas, EXPECTED_FPS);
 		animator.setRunAsFastAsPossible(false);
 		animator.start();
 	}
 	
 	public static void main(String[] args) {
		new MainLoop();
 	}
 
 	@Override
 	public void display(GLAutoDrawable arg0) {
 		performLoop();
 	}
 
 	@Override
 	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
 		// Unused.
 	}
 
 	@Override
 	public void init(GLAutoDrawable arg0) {
 		// Unused.
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
 			int arg4) {
 		// Unused.
 	}
 }
