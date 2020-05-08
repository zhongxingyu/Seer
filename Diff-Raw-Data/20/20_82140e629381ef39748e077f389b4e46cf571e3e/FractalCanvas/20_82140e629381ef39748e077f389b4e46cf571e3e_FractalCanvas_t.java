 /*
 * MandelBrotViewer
 * Copyright (C) 2012 Pierre-Henri Symoneaux
 * 
 * This work is licensed under the Creative Commons 
 * Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0)
 * 
 * To view a copy of this license,
 * visit http://creativecommons.org/licenses/by-nc-sa/3.0/legalcode .
 */
 
 package phsym.mandelbrot;
 
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 import phsym.mandelbrot.ColorMap.ColorMode;
 import phsym.mandelbrot.menu.PopMenu;
 
 /**
 *	@author Pierre-Henri Symoneaux
 */
 public class FractalCanvas extends Canvas implements Runnable, KeyListener, MouseWheelListener, MouseMotionListener, MouseListener {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private JFrame frame;
 	
 	private BufferStrategy strategy;
 	
 	private PopMenu pop_m = null;
 	
 	private ColorMode colorMode = ColorMode.FIRE;
 	
 	private boolean running = false;
 	
 	private boolean screenshotting = false;
 	
 	private BufferedImage im;
 	
 	private int max_iteration = 300;
 	
 	private double zoom = 1;
 	private double ty = 0;
 	private double tx = 0;
 	private int drag_or_x;
 	private int drag_or_y;
 
 	public FractalCanvas(JFrame frame) {
 		this.frame = frame;
 		setBackground(Color.WHITE);
 		addKeyListener(this);
 		addMouseWheelListener(this);
 		addMouseMotionListener(this);
 		addMouseListener(this);
 		pop_m = new PopMenu(this);
 	}
 	
 	public void init(){
 		setIgnoreRepaint(true);
 		createBufferStrategy(2);
 		strategy = getBufferStrategy();
 		
 		im = (BufferedImage) createImage(getWidth(), getHeight());
 	}
 	
 	public void render(){
 		Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		g.setBackground(Color.WHITE);
 		
 		g.drawImage(im, 0, 0, getWidth(), getHeight(), null);
 		g.setColor(Color.WHITE);
 		
 		g.fillOval(getWidth()/2-3, getHeight()/2-3, 6, 6);
 		
 		if(screenshotting)
 			g.drawString("Sreenshotting ...", getWidth()/2-30, getHeight()/2-10);
 		
 		g.drawString("Iterations = " + max_iteration + " zoom = " + zoom + " tx = " + tx + " ty = " + ty, 5, 10);
 		strategy.show();
 	}
 	
 	public void updateImage(){
 		for(int i = 0; i < im.getHeight(); i++)
 		{
 			double y0 = -1d + 2d * (double)(i+zoom*2d/3.5d)/(double)(im.getHeight()+2*zoom*2d/3.5d) -3.5d*ty/(double)im.getHeight();
 			for (int j = 0; j < im.getWidth(); j++)
 			{
 				double x0 = -2.5d + 3.5d * (double)(j+zoom)/(double)(im.getWidth()+2*zoom) - 3.5d*tx/(double)im.getWidth();
 				
 				double x = 0;
 				double y = 0;
 				int iteration = 0;
 				
 				while((x*x + y*y < 2*2) && (iteration < max_iteration) && running)
 				{
 					double xtemp = x*x - y*y + x0;
 					y = 2*x*y + y0;
 					
 					x = xtemp;
 					iteration++;
 				}
 				im.setRGB(j, i, ColorMap.getColor(iteration, max_iteration, colorMode).getRGB());
 
 				if(!running)
 					return;
 			}
 		}
 
 	}
 	
 	@Override
 	public void run() {
 		if(!running)
 		{
 			running = true;
 			init();
 			
 			new Thread(new Runnable() {
 				
 				@Override
 				public void run() {
 					while(running)
 					{
 						updateImage();
 					}
 				}
 			}).start();
 			
 			while(running)
 			{
 				render();
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				//Notify end of rendering
 				synchronized(this){
 					notifyAll();
 				}
 			}
 		}
 	}
 	
 	public void stop(){
 		running = false;
 		
 		//wait for end of rendering
 		synchronized(this){
 			try {
 				wait();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public JFrame getFrame() {
 		return frame;
 	}
 	
 	public void takeScreenShot(){
 		screenshotting = true;
 		try {
 			ImageIO.write(im, "PNG", new File("./mandelbrot"+System.currentTimeMillis()+".png"));
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 		screenshotting = false;
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		if(e.getKeyChar() == '+') // Zoom in
 			zoom*=1.1;
 		else if(e.getKeyChar() == '-') //Zoom out
 			zoom/=1.1;
 		else if(e.getKeyChar() == 'q') // Left
 			tx += 1;
 		else if(e.getKeyChar() == 'd') // Right
 			tx -= 1;
 		else if(e.getKeyChar() == 'z') // Up
 			ty += 1;
 		else if(e.getKeyChar() == 's') // Down
 			ty -= 1;
 		else if (e.getKeyChar() == 'i') // increase max itarations
 			max_iteration++;
 		else if (e.getKeyChar() == 'k') // Decrease max iterations
 			max_iteration--;
 		else if(e.getKeyChar() == 'r') // Reset position
 		{
 			tx = 0;
 			ty = 0;
 			zoom = 1;
 		}
 		
 		else if(e.getKeyChar() == 'p') // Screenshot
 			takeScreenShot();
 			
 	}
 	
 	/**
 	 * @return the max_iteration
 	 */
 	public int getMax_iteration() {
 		return max_iteration;
 	}
 
 	/**
 	 * @param max_iteration the max_iteration to set
 	 */
 	public void setMax_iteration(int max_iteration) {
 		this.max_iteration = max_iteration;
 	}
 	
 	public ColorMode getColorMode() {
 		return colorMode;
 	}
 
 	public void setColorMode(ColorMode colorMode) {
 		this.colorMode = colorMode;
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		int val = -e.getWheelRotation();
 		if(val > 0)
 			zoom *= val*1.1;
 		else
 			zoom /= -val*1.1;
 	}
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if(e.getButton() == MouseEvent.BUTTON1)
 		{
 			drag_or_x = e.getX();
 			drag_or_y = e.getY();
 		}
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		if((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
 		{
 			ty -= drag_or_y - e.getY();
 			tx -= drag_or_x - e.getX();
 			drag_or_x = e.getX();
 			drag_or_y = e.getY();
 		}
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if(e.getButton() == MouseEvent.BUTTON3)
 		{
			pop_m.show(this, e.getX(), e.getY());
 		}
 	}
 	
 	
 	/* UNUSED IMPLEMENTED METHODS */
 	
 	@Override
 	public void keyPressed(KeyEvent evt) {}
 	@Override
 	public void keyReleased(KeyEvent e) {}
 	@Override
 	public void mouseMoved(MouseEvent e) {}
 	@Override
 	public void mouseEntered(MouseEvent e) {}
 	@Override
 	public void mouseExited(MouseEvent e) {}
 	@Override
 	public void mouseReleased(MouseEvent e) {}
 }
