 package se.chalmers.kangaroo.view;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 /**
  * A view to show when the game is paused.
  * 
  * 
  * @author arvidk
  * 
  */
 public class PauseView extends JPanelWithBackground implements MouseListener {
 	private Menuebutton resume, highScore, options, exitGame;
 	private String viewName = "gameview";
 	private ChangeView cv;
 
 	/**
 	 * The constructor for the GameView.
 	 * 
 	 * @param bgpath
 	 */
 	public PauseView(String bgpath, ChangeView cv) {
 		super(bgpath);
 		this.cv = cv;
 		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
 		this.setLayout(layout);
 		this.setSize(1024, 576);
 		this.setBackground(Color.BLACK);
 		resume = new Menuebutton("resources/images/resume.png");
 		highScore = new Menuebutton("resources/images/highscore.png");
 		options = new Menuebutton("resources/images/options.png");
 		exitGame = new Menuebutton("resources/images/exitgame.png");
 
 		this.add(resume);
		this.add(new Menuebutton("resources/images/transparent.png"));
 		this.add(highScore);
		this.add(new Menuebutton("resources/images/transparent.png"));
 		this.add(options);
		this.add(new Menuebutton("resources/images/transparent.png"));
 		this.add(exitGame);
 		this.add(new Menuebutton("resources/images/stretchbar.png"));
 		
 		
 		resume.addMouseListener(this);
 		highScore.addMouseListener(this);
 		options.addMouseListener(this);
 		exitGame.addMouseListener(this);
 	}
 	@Override
 	 public void paintComponent (Graphics g)
 	    { 
	((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.2f)); // draw transparent background
 	     super.paintComponent(g);
 	    ((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f)); // turn on opacity
 	     } 
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		if (e.getSource() == resume)
 			resume.setIcon(new ImageIcon(
 					"resources/images/resuem_onHover.png"));
 		if (e.getSource() == highScore)
 			highScore.setIcon(new ImageIcon(
 					"resources/images/highscore_onHover.png"));
 		if (e.getSource() == options)
 			options.setIcon(new ImageIcon(
 					"resources/images/options_onHover.png"));
 		if (e.getSource() == exitGame)
 			exitGame.setIcon(new ImageIcon(
 					"resources/images/exitgame_onHover.png"));
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		if (e.getSource() == resume)
 			resume.setIcon(new ImageIcon("resources/images/resume.png"));
 		if (e.getSource() == highScore)
 			highScore.setIcon(new ImageIcon("resources/images/highscore.png"));
 		if (e.getSource() == options)
 			options.setIcon(new ImageIcon("resources/images/options.png"));
 		if (e.getSource() == exitGame)
 			exitGame.setIcon(new ImageIcon("resources/images/exitgame.png"));
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if (e.getSource() == resume)
 			resume.setIcon(new ImageIcon(
 					"resources/images/resuem_onSelect.png"));
 		if (e.getSource() == highScore)
 			highScore.setIcon(new ImageIcon(
 					"resources/images/highscore_onSelect.png"));
 		if (e.getSource() == options)
 			options.setIcon(new ImageIcon(
 					"resources/images/options_onSelect.png"));
 		if (e.getSource() == exitGame)
 			exitGame.setIcon(new ImageIcon(
 					"resources/images/exitgame_onSelect.png"));
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (e.getSource() == resume) {
 			resume.setIcon(new ImageIcon("resources/images/resume.png"));
 			//TODO wake the thread that the game is running in.
 			
 			cv.gameView();
 
 		}
 		if (e.getSource() == highScore) {
 			highScore.setIcon(new ImageIcon("resources/images/highscore.png"));
 			cv.highscoreView(viewName);
 		}
 
 		if (e.getSource() == options) {
 			options.setIcon(new ImageIcon("resources/images/options.png"));
 			cv.optionView(viewName);
 
 		}
 		if (e.getSource() == exitGame) {
 			exitGame.setIcon(new ImageIcon("resources/images/exitGame.png"));
 			System.exit(0);
 		}
 	}
 
 }
