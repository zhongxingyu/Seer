 package main;
 import javax.imageio.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 public class grid extends JPanel implements KeyListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6306113229343973266L;
 	
 	private javax.swing.Timer timer;
 	private ControlManager gameControl;
 	private GraphicsManager gameGraphics;
 	private int w, h;
 	private PlayerEnt player = new PlayerEnt(5, 5);
 	private boolean first = true;
 	
 	private int tick = 0, fpsTick = 0, frames = 0, fps = 0;
 	
 	
 	public grid(Color backColor, int width, int height) {
 		setBackground(backColor);
 		setPreferredSize(new Dimension(width, height));
 		gameControl = new ControlManager(this, player);
 		gameGraphics = new GraphicsManager(this, gameControl, player);
 		System.out.println("Player initialized");
 		timer = new javax.swing.Timer(10, new MoveListener());
 		timer.start();
 		w = width;
 		h = height;
 	}
 	
 	public void drawGrid(Graphics g) {		//Simple grid drawing algorithm.
 		
 		int k=0;
 		Color oldColor = g.getColor();
 		g.setColor(new Color(200, 200, 200));
 		int htOfRow = h / 15;
 		for (k = 0; k <= 15; k++)
 			g.drawLine(0, k * htOfRow , w, k * htOfRow );
 		
 		int wdOfRow = w / 20;
 		for (k = 0; k <= 20; k++) 
 			g.drawLine(k*wdOfRow , 0, k*wdOfRow , h);
 		
 		g.setColor(oldColor);
 	}
 	
 	public static void print(Graphics g, String text, int x, int y) {
 		g.drawString(text, x, y);
 	}
 	
 	
 	public void paintComponent(Graphics g) {	//Called each time it's redrawn.  Send the gamegraphics a message to draw each component.
 		super.paintComponent(g);
 		drawGrid(g);
 		gameGraphics.drawBackground(g);
 		gameGraphics.draw(g, player, this);
 		first = false;
 		frames++;
 		if(fpsTick == 100) {
 			fps = frames;
 			fpsTick = 0;
 			frames = 0;
 		}
 		print(g, "fps: "+fps, 100, 100);
 	}
 	
 	private class MoveListener implements ActionListener{
 		public void actionPerformed(ActionEvent e) {
 			tick++;
 			fpsTick++;
		//	repaint();
 		}
 	}
 
 	public void keyPressed(KeyEvent e) {
 		gameControl.keyDown(e.getKeyCode());
 	}
 	
 	public void keyReleased(KeyEvent e) {
 		gameControl.keyUp(e.getKeyCode());
 		
 	}
 	
 	public void keyTyped(KeyEvent e) {}
 }
