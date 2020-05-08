 package com.googlecode.connect4java.swing;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 import javax.swing.JPanel;
 
 import com.googlecode.connect4java.Main;
 import com.googlecode.connect4java.game.Player;
 
 /**
  * 
  * @author richard.stromer
 * @version 0.8.20
  * @since 0.8.17
  *
  */
public class JRoundPanel extends JPanel {
 	private static final long serialVersionUID = -5398739716808518120L;
 	
 	private Player playertop;
 	private Player playerbottom;
 	
 	public JRoundPanel() {
 		super();
 		setOpaque(false);
 		setPreferredSize(new Dimension(200, 51));
 		
 		updatePlayers();
 	}
 	
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D) g;
 		
 		paintRound(g2, getWidth(), getHeight());
 	}
 	
 	
 	private void paintRound(Graphics2D g2, int width, int height) {
 		final Color border = new Color(128, 128, 128);
 		g2.setColor(border);
 		g2.fillRect(0, 0, width, height);
 		g2.setColor(new Color(0, 0, 0, 128));
 		g2.fillRect(1, 1, width-2, height-2);
 		
 		g2.setColor(border);
 		g2.drawRect(2, 2, width-5, 22);
 		g2.setColor(playertop.getColor());
 		g2.fillRect(3, 3, width-6, 21);
 		
 		g2.setColor(border);
 		g2.drawRect(2, 26, width-5, 22);
 		g2.setColor(playerbottom.getColor());
 		g2.fillRect(3, 27, width-6, 21);
 		
 		g2.setColor(Color.BLACK);
 		g2.drawString(playertop.getName(), 8, 17);
 		g2.drawString(playerbottom.getName(), 8, 42);
 	}
 	private void updatePlayers() {
 		playertop = new Player(Main.pref.get("player.name", "Player"), new Color(Main.pref.getInt("player.color", 255)));
 		playerbottom = new Player(Main.pref.get("computer.name", "Computer"), new Color(Main.pref.getInt("computer.color", -65536)));
 	}
 	public void update() {
 		updatePlayers();
 		repaint();
 	}
 	public void switchPlayer() {
 		Player tmp = playerbottom;
 		playerbottom = playertop;
 		playertop = tmp;
 		repaint();
 	}
 	public void setPlayerTop(String name, Color color) {
 		playertop = new Player(name, color);
 		repaint();
 	}
 	public void setPlayerBottom(String name, Color color) {
 		playerbottom = new Player(name, color);
 		repaint();
 	}
 }
