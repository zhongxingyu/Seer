 /**
  * 
  */
 package com.jpii.navalbattle.game;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 import com.jpii.navalbattle.data.Helper;
 import com.jpii.navalbattle.game.entity.Entity;
 
 /**
  * @author MKirkby
  * 
  */
 @SuppressWarnings("serial")
 public class GameComponent extends JComponent {
 	JFrame frame;
 	ArrayList<Entity> entities;
 	Timer ticker;
 
 	BufferedImage grid, shadow;
 
 	int test;
 
 	public GameComponent(JFrame frame) {
 		this.frame = frame;
 		ActionListener al = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				tick();
 			}
 		};
 		int w = Toolkit.getDefaultToolkit().getScreenSize().width;
 		int h = Toolkit.getDefaultToolkit().getScreenSize().height;
 		grid = Helper.genGrid(w, h, 20);
 		shadow = Helper.genInnerShadow(w, h);
 
 		entities = new ArrayList<Entity>();
 
 		ticker = new Timer(100, al);
 
 		start();
 
 	}
 
 	public void addEntity(Entity entity) {
		if (entity != null || entity.isActive()) {
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
 
 	private void tick() {
 		for (int x = 0; x < entities.size(); x++) {
 			if (entities.get(x) != null) {
 				entities.get(x).tick();
 			}
 		}
 		test += 1;
 		repaint();
 	}
 
 	public void paintComponent(Graphics g) {
 		g.setColor(Color.red);
 		g.fillRect(0, 0, getWidth() + 1, getHeight() + 1);
 
 		g.drawImage(grid, 0, 0, null);
 		g.drawImage(shadow, 0, 0, null);
 
 		g.setColor(Color.black);
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
 
 	public int getTimeTick() {
 		return ticker.getDelay();
 	}
 
 	public void start() {
 		ticker.start();
 	}
 
 	public void stop() {
 		ticker.stop();
 	}
 
 	public void repaint() {
 		frame.repaint();
 	}
 }
