 package com.jpii.navalbattle.gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.swing.JComponent;
 import javax.swing.Timer;
 
 import com.jpii.navalbattle.renderer.particles.*;
 
 @SuppressWarnings("serial")
 public class CreditsBackground extends JComponent implements MouseListener{
 	int width, height, pixel;
 	BufferedImage buffer;
 	ParticleEngine system;
 	Random r;
 	Timer timer;
 	ArrayList<star> stars;
 	
 	public CreditsBackground(int width, int height) {
 		system = new ParticleEngine(50,width,height);
 		system.setMaxParticles(500);
 		stars = new ArrayList<star>();
 		r = new Random();
 		addMouseListener(this);
 		this.width = width;
 		this.height = height;
 		setSize(width,height);
 		for (int c = 0; c < r.nextInt(4) + 5; c++) {
 			stars.add(new star(width,height));
 		}
 		ActionListener al = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				tick();
 				repaint();
 			}
 		};
 		timer = new Timer(50,al);
		if(isVisible()){
			timer.start();
		}
	}
	
	public void setVisible(boolean visible){
		super.setVisible(visible);
		if(isVisible()){
			timer.start();
		}
		else{
			if(timer!=null)
				timer.stop();
		}
 	}
 	
 	public void tick() {
 		buffer = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = (Graphics2D)buffer.getGraphics();
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setColor(new Color(5,5,15));
 		g.fillRect(0,0,getWidth(),getHeight()/2);
 		GradientPaint paint = new GradientPaint(0, (getHeight()/2)-45, new Color(5,5,15),0, getHeight(), new Color(30,51,108));
         g.setPaint(paint);
         g.fillRect(0,(getHeight()/2)-45,getWidth(),(getHeight()/2)+45);
         g.setPaint(null);
         
         for (int c = 0; c < stars.size(); c++) {
         	stars.get(c).draw(g);
         }
         
         SmokeParticle sp = new SmokeParticle(r,(float) (10+(r.nextDouble() * 8)));
         sp.setX(getWidth()/2);
         sp.setY((getHeight()/2)+45);
         system.addParticle(sp);
         
         g.drawImage(system.getBuffer(),0,0,null);
 	}
 	
 	public BufferedImage getImage() {
 		return buffer;
 	}
 	
 	public void paintComponent(Graphics g) {
 		g.drawImage(buffer, 0, 0, null);
 	}
 
 
 	public void mouseClicked(MouseEvent arg0) {	
 	}
 	public void mouseEntered(MouseEvent arg0) {	
 	}
 	public void mouseExited(MouseEvent arg0) {
 	}
 	public void mousePressed(MouseEvent arg0) {
 	}
 	public void mouseReleased(MouseEvent arg0) {
 	}
 }
 class star {
 	int x, y, stage;
 	public star(int width, int height) {
 		x = (int)(width*Math.random());
 		y = (int)((height/2)*Math.random());
 		stage = 0;
 	}
 	public void draw(Graphics2D g) {
 		g.setColor(new Color(225,225,240));
 		if (stage == 0) {
 			g.fillRect(x-2,y-2,4,4);
 		}
 		else if (stage == 1) {
 			g.fillRect(x-2,y-2,4,4);
 			g.fillRect(x-2,y-6,4,4);
 			g.fillRect(x-2,y+2,4,4);
 			g.fillRect(x-6,y-2,4,4);
 			g.fillRect(x+2,y-2,4,4);
 		}
 		if (stage != 1)
 			stage++;
 		else
 			stage = 0;
 	}
 }
