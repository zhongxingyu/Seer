 package com.az;
 import java.awt.*;
 import java.awt.event.*;
 
 
 public class Yard extends Frame {
 
 	public static final int ROWS = 30;
 	public static final int COLS = 50;
 	public static final int CELL_SIZE = 15;
 	
 	private Snake s = new Snake(this);
 	private Egg e = new Egg();
 	
 	PaintThread paintThread = new PaintThread();
 	Image offScreenImage = null;
 
 	private boolean flag = true;
 	public int score = 0;
 	
 	public static void main(String[] args) {
 		Yard y = new Yard();
 		y.launch();
 	}
 
 
 	private void launch() {
 		this.setBounds(300, 50, COLS*CELL_SIZE, ROWS*CELL_SIZE);
 		this.addWindowListener(new WindowAdapter() {
 
 			@Override
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 			
 		});
 		this.setVisible(true);
 		
 		//thread
 		this.addKeyListener(new KeyMonitor());
 		new Thread(paintThread).start();
 		
 	}
 
 
 	public void paint(Graphics g) {
 		Color c = g.getColor();
 		g.setColor(Color.BLACK);
 		
 		//
 		for (int i=0; i<ROWS; i++) {
 			g.drawLine(0, CELL_SIZE*i, COLS*CELL_SIZE, CELL_SIZE*i);
 		}
 		//
 		for (int i=0; i<COLS; i++) {
 			g.drawLine(CELL_SIZE*i, 0, CELL_SIZE*i, ROWS*CELL_SIZE);
 		}
 		
 		
 		
 		//snake  & egg
 		s.eat(e);
 		
 		s.draw(g);
 		e.draw(g);
 		
 		//
 		g.setColor(Color.RED);
 		this.setTitle("Egg: " + e.getRect().x/CELL_SIZE + ", " + e.getRect().y/CELL_SIZE + " " +
				       "      Snake: " + s.getRect().x/CELL_SIZE + ", " + s.getRect().y/CELL_SIZE);
 		g.drawString("Score :   " + score, 30, 43);
 		
 		g.setColor(c);
 	}
 	
 	
 	@Override
 	public void update(Graphics g) {
 		if (null == offScreenImage) {
 			offScreenImage = this.createImage(COLS*CELL_SIZE, ROWS*CELL_SIZE);
 		}
 		Graphics gOff = offScreenImage.getGraphics();
 		gOff.clearRect(0, 0, COLS*CELL_SIZE, ROWS*CELL_SIZE);
 		paint(gOff);
 		g.drawImage(offScreenImage, 0, 0, null);
 	}
 
 
 	private class PaintThread implements Runnable {
 
 		@Override
 		public void run() {
 			while(flag) {
 				repaint();
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 
 
 	private class KeyMonitor extends KeyAdapter {
 
 		@Override
 		public void keyPressed(KeyEvent e) {
 			s.keyPressed(e);
 		}
 		
 	}
 
 
 	public void stop() {
 		flag = false;
 	}
 	
 	
 	public int getScore() {
 		return this.score;
 	}
 	public void setScore(int s) {
 		this.score = s;
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
