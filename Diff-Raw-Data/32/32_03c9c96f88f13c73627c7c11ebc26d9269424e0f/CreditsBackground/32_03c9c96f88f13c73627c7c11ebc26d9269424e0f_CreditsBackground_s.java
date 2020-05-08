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
 	long ticks = 0;
 	
 	public CreditsBackground(int width, int height) {
 		system = new ParticleEngine(50,width,height);
 		system.setMaxParticles(500);
 		stars = new ArrayList<star>();
 		r = new Random();
 		addMouseListener(this);
 		this.width = width;
 		this.height = height;
 		setSize(width,height);
 		for (int c = 0; c < r.nextInt(9) + 10; c++) {
 			stars.add(new star(width,height));
 		}
 		ActionListener al = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				tick();
 				repaint();
 				ticks+= 50;
 				if (ticks % 250 == 0)
 					for (int c = 0; c < stars.size(); c++) {
 						stars.get(c).update();
 					}
 			}
 		};
		timer = new Timer(50,al);
		if(isVisible()){
			timer.start();
		}
 	}
 	
 	public void start() {
		timer.start();
 	}
 	public void stop() {
		timer.stop();
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
 		g.setColor(new Color(25,25,35));
 		g.fillRect(0,0,getWidth(),getHeight()/2);
 		GradientPaint paint = new GradientPaint(0, (getHeight()/2)-45, new Color(25,25,35),0, getHeight(), new Color(30,51,108));
         g.setPaint(paint);
         g.fillRect(0,(getHeight()/2)-45,getWidth(),(getHeight()/2)+45);
         g.setPaint(null);
         
         int s = 8;
         int sx = 10*s;
         int sy = (getHeight()/2)+(s*10);
         for (int z = -4; z < 4; z++) {
         	for (int x = Math.abs(z); x < 10-Math.abs(z); x++) {
         		g.setColor(new Color(66 + (z * 4),89 + (z * 4),40 + (z * 4)));
         		g.fillRect((sx+(s*x)),(sy+(s*z/2)),s,s);
         	}
         }
         sy = (getHeight()/2)+(s*9);
         for (int z = -3; z < 3; z++) {
         	for (int x = Math.abs(z)+(2); x < 6-Math.abs(z)+(2); x++) {
         		g.setColor(new Color(110 + (z * 5),150 + (z * 5),70 + (z * 5)));
         		g.fillRect((sx+(s*x)),(sy+(s*z/2)),s,s);
         	}
         }
         for (int c = 32; c > 0; c-=8) {
         	g.setColor(new Color(116-(c),73-c,35-c));
         	g.fillRect(8+sx,sy-(c-8),8,8);
         }
         
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
 		y = (int)(((height/2)-45)*Math.random());
 		stage = (int)(Math.random() * 3);
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
 		else if (stage == 2) {
 			g.fillRect(x-8,y-8, 4,4);
 			g.fillRect(x-8, y+4, 4,4);
 			g.fillRect(x+4,y-8,4,4);
 			g.fillRect(x+4,y+4,4,4);
 		}
 	}
 	public void update() {
 		if (stage != 3)
 			stage++;
 		else
 			stage = 0;
 	}
 }
