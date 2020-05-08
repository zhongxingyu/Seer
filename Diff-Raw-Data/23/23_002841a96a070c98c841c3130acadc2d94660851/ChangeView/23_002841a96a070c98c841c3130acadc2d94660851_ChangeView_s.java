 package se.chalmers.kangaroo.view;
 
 import java.awt.CardLayout;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import se.chalmers.kangaroo.controller.GameController;
 import se.chalmers.kangaroo.utils.Sound;
 import se.chalmers.kangaroo.view.*;
 
 public class ChangeView extends JFrame implements KeyListener{
 
 	private JPanel jp;
 	private GameController gc;
 	private OptionView ov;
 	private HighscoreView hv;
 	private MenuView mv;
 	
 	public ChangeView() {
 		gc = new GameController();
 		ov = new OptionView("resources/images/background.gif", this);
 		hv = new HighscoreView("resources/images/background.gif", this);
 		mv = new MenuView("resources/images/background.gif", this);
 		
 		jp = new JPanel(new CardLayout());
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(1024, 576);
 		Dimension win = Toolkit.getDefaultToolkit().getScreenSize();
 		int width = getSize().width;
 		int height = getSize().height;
 		int xPos = (win.width-width)/2;
 		int yPos = (win.height-height)/2;
 		setLocation(xPos,yPos);
 		jp.add(mv , "menuview");
 		jp.add(gc.getGameView(), "gameview");
 		jp.add(ov, "optionview");
 		jp.add(hv, "highscoreview");
 		jp.add(mv, "menuview");
 		//TODO fix so you can go back to the menu
 //		jp.add(gc.getMenuView(), "menuview");
 		add(jp);
 		setVisible(true);
 		jp.setVisible(true);
 	}
 
 	public void menuView() {
		Sound s = new Sound("resources/music/menuemusic.wav");
		s.play();
 		CardLayout cl = (CardLayout) jp.getLayout();
 		cl.show(jp, "menuview");
		//TODO fix so you can go back to the menu
//		gc.start();
 	}
 
 	public void gameView() {
 		CardLayout cl = (CardLayout) jp.getLayout();
 		cl.show(jp, "gameview");
		addKeyListener(this);
 		gc.start();
 	}
 	
 	public void optionView() {
 		CardLayout cl = (CardLayout) jp.getLayout();
 		cl.show(jp, "optionview");
		addKeyListener(this);
 	}
 	
 	public void highscoreView(){
 		CardLayout cl = (CardLayout) jp.getLayout();
 		cl.show(jp, "highscoreview");
		addKeyListener(this);
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		gc.keyPressed(e);
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		gc.keyReleased(e);
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		//Do nothing rly	
 	}
 }
