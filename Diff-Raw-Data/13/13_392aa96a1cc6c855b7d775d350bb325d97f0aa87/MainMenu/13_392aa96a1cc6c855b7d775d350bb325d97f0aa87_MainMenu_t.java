 package org.br.game;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 /**
  * Paints Main Menu and its buttons
  * 
  * @author Lone Wolf
  */
 public class MainMenu extends JPanel implements KeyListener,MouseListener {
 
 	private Image bgimg = (new ImageIcon("img/title.png").getImage());
 
 	public MainMenu() {
 		super();

 		setFocusable(true);
 		addKeyListener(this);
 		addMouseListener(this);
 	}
 
 	 
 	/** Paints the panel and its components. **/
 	public void paint(Graphics g) {
 		g.drawImage(bgimg, 0, 0, getWidth(), getHeight(), this);
 	}
 
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 	 
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
		Log.info(getClass(),"keyReleased:"+arg0.getKeyCode());
 		if (arg0.getKeyCode() == 10 || arg0.getKeyCode() == 32) { // 10 = Enter, 32 = spacebar
 			Game.GAME.setStartGame(true);
 			Game.GAME.startGame();
 		}
 		else if (arg0.getKeyCode() == 72) { // 72 = H
 			// ADD HELP PANEL HERE
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 	 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		Game.GAME.setStartGame(true);
 		Game.GAME.startGame();
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 }
