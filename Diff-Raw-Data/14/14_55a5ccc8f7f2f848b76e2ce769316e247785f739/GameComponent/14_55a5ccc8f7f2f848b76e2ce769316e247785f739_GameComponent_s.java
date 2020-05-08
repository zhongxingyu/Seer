 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle.game;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.*;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.swing.*;
 import java.beans.*;
 
 import com.jpii.dagen.*;
 import com.jpii.navalbattle.data.Helper;
 import com.jpii.navalbattle.game.entity.Entity;
 import com.jpii.navalbattle.gui.cotton.NavButton;
 import com.jpii.navalbattle.util.Stopwatch;
 
 /**
  * @author MKirkby
  * 
  */
 @SuppressWarnings("serial")
 public class GameComponent extends JComponent {
 	JFrame frame;
 	ArrayList<Entity> entities;
 	Timer ticker;
 	
 	Random rand;
 	
 	boolean hitTime = false;
 
 	BufferedImage grid, shadow, map, blood;
 
 	int test;
 	JSlider slider;
 	NavButton nav0;
 
 	Engine eng;
 	
 	double zoomx, zoomy;
 	
 	double visualx,visualy;
 	double colorShift = 1.0;
 	
 	boolean invokeFlag = false;
 	private JButton button_1;
 	private JButton button_2;
 	private JButton button_3;
 	
 	public GameComponent(JFrame frame) {
 		this.frame = frame;
 		rand = new Random();
 		
 		//Lets use Nimbus for now.
 		try {
 			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
 		} catch (Exception e) {}
 		
 		nav0 = new NavButton();
 		nav0.setLocation(128, 159);
 		nav0.setSize(100,81);
 		
 		slider = new JSlider();
 		slider.setMajorTickSpacing(1);
 		slider.setMinorTickSpacing(1);
 		slider.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				onZoom();
 			}
 		});
 		slider.setForeground(new Color(0,0,0,127));
 		slider.addPropertyChangeListener(new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent arg0) {
 				onZoom();
 			}
 		});
 		slider.setPaintTicks(true);
 		slider.setOrientation(SwingConstants.VERTICAL);
 		slider.setBounds(10, 63, 31, 177);
 		slider.setValue(0);
 		add(slider);
 		
 		add(nav0);
 		
 		JButton button = new JButton("►");
 		button.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent arg0) {
 				moveX(-1);
 			}
 		});
 		button.setBounds(137, 28, 54, 23);
 		add(button);
 		
 		button_1 = new JButton("▼");
 		button_1.setBounds(85, 48, 54, 23);
 		button_1.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent arg0) {
 				moveY(-1);
 			}
 		});
 		add(button_1);
 		
 		button_2 = new JButton("▲");
 		button_2.setBounds(85, 11, 54, 23);
 		button_2.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent arg0) {
 				moveY(1);
 			}
 		});
 		add(button_2);
 		
 		button_3 = new JButton("◄");
 		button_3.setBounds(32, 28, 54, 23);
 		button_3.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent arg0) {
 				moveX(1);
 			}
 		});
 		add(button_3);
 		
 		JButton btnActLikeThe = new JButton("Act like the current player got hit by a shot");
 		btnActLikeThe.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				hitVisual();
 			}
 		});
 		btnActLikeThe.setBounds(32, 266, 313, 23);
 		add(btnActLikeThe);
 		ActionListener al = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				tick();
 			}
 		};
		this.setFocusable(true);		
 		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
 		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
 		
 		eng = new Engine(w,h);
 		eng.generate(MapType.Hills, (int)(Math.random() * 99999999999.9), 1.0);
 		
 		grid = Helper.genGrid(w, h, getGridSize()*3);
 		shadow = Helper.genInnerShadow(w, h);
 		map = Helper.genMap(eng,0,0,w,h, w, h, 1);
 		blood = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
 
 		entities = new ArrayList<Entity>();
 
 		ticker = new Timer(100, al);
 
 		start();
 		
 
 	}
 	
 	public void moveX(int ammount){
 		if(ammount>0)
 			zoomx += getGridSize();
 		else if(ammount<0)
 			zoomx -= getGridSize();
 		onZoom();
 	}
 	
 	public void moveY(int ammount){
 		if(ammount>0)
 			zoomy += getGridSize();
 		else if(ammount<0)
 			zoomy -= getGridSize();
 		onZoom();
 	}
 	
 	public void addEntity(Entity entity) {
 		if (entity != null) {
 			entities.add(entity);
 		}
 	}
 
 	public Entity getEntity(String tag) {
 		for (int x = 0; x < entities.size(); x++) {
 			if (entities.get(x).getTag().toLowerCase() == tag.toLowerCase()) {
 				return entities.get(x);
 			}
 		}
 		return null;
 	}
 
 	public Entity getEntity(int index) {
 		return entities.get(index);
 	}
 	double cman = 1.0;
 	private void tick() {
 		if (!invokeFlag) {
 			for (int x = 0; x < entities.size(); x++) {
 				if (entities.get(x) != null) {
 					entities.get(x).tick();
 				}
 			}
 			test += 1;
 			
 			if (hitTime)
 			{
 				if (visualx > 1.5)
 					visualx -= random(0.5,1.5);
 
 				if (visualy > 1.5)
 					visualy -= random(0.5,1.5);
 				
 				if (cman > 0.01)
 					cman -= 0.01;
 				
 				visualx *= Math.sin(visualx);
 				visualy *= Math.sin(visualy);
 				
 				if (cman > 0.05)
 					cman -= 0.05;
 				
 				blood = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
 				Graphics g = blood.getGraphics();
 				Color b = Lerp(new Color(1,1,1,1),new Color(127,25,25,125),cman);
 				g.setColor(b);
 				g.fillRect(0, 0, getWidth(),getHeight());
 			}
 			
 			repaint();
 		}
 		invokeFlag = false;
 	}
 	
 	/** Also stolen from my blog...*/
 	public double Lerp(int num0, int num1, double amount)
 	{
 		return num0 + (amount*(num1-num0));
 	}
 	
 	/** Stolen from my blog.*/
 	public Color Lerp(Color color0, Color color1, double amount)
 	{
 		int r = (int)Lerp(color0.getRed(), color1.getRed(), amount);
 		int g2 = (int)Lerp(color0.getGreen(), color1.getGreen(), amount);
 		int b = (int)Lerp(color0.getBlue(), color1.getBlue(), amount);
 		int a = (int)Lerp(color0.getAlpha(), color1.getAlpha(), amount);
 		System.out.println("alhpa="+a);
 		return new Color(snap(r),snap(g2),snap(b),snap(a));
 	}
 	
 	public int snap(int v)
 	{
 		if (v > 255)
 			return 255;
 		if (v < 0)
 			return 0;
 		return v;
 	}
 	
 	private void onZoom() {
 		Stopwatch watch = new Stopwatch();
 		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
 		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
 		
 		watch.start();
 		grid = Helper.genGrid(w,h, getGridSize()*3);
 		watch.stop();
 		
 		//System.out.println("rid formulation took: " + watch.getString());
 		
 		watch.start();
 		//was zoomx and zoomy
 		map = Helper.genMap(eng,0,0, w/getGridSize(), h/getGridSize(), w,h, getGridSize());
 		watch.stop();
 		
 		//System.out.println("Map formulation took: " + watch.getString());
 		
 		invokeFlag = true;
 		
 		repaint();
 	}
 	public int getGridSize() {
 		int c = (slider.getValue() * 40)/100;
 		if (c > 5)
 			return c;
 		else
 			return 5;
 	}
 
 	public void paintComponent(Graphics g) {
 		g.setColor(Color.black);
 		g.fillRect(0, 0, getWidth() + 1, getHeight() + 1);
 		
 		
 		g.drawImage(map,(int)(zoomx+visualx),(int)(zoomy+visualy),null);
 
 		g.drawImage(grid, (int)visualx,(int)visualy, null);
 		g.drawImage(shadow, 0, 0, null);
 		
 		g.drawImage(blood,0,0,null);
 
 		g.setColor(Color.white);
 		g.drawString("This is just a test.", 100, 100);
 		g.drawString(
 				"In the future, this is where the map, enitities, and GUI would go.",
 				100, 120);
 		g.drawString(
 				"Just to make sure that the ticker/updater is working, here is how many ticks performed:"
 						+ test, 100, 140);
 	}
 
 	public void setTimeTick(int interval) {
 		ticker.setDelay(interval);
 	}
 	
 	public void hitVisual() {
 		hitTime = true;
 		visualx = 75;
 		visualy = 75;
 		cman = 1.0;
 	}
 
 	public int getTimeTick() {
 		return ticker.getDelay();
 	}
 
 	public void start() {
 		ticker.start();
 	}
 
 	public void stop() {
 		ticker.stop();
 	}
 	
 	public double random(double min, double max) {
 		return (rand.nextDouble() * (max-min)) + min;
 	}
 	
 	public void repaint() {
 		frame.repaint();
 	}
 }
