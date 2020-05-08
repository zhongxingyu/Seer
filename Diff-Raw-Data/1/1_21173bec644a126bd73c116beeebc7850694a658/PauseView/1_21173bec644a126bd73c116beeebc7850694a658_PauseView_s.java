 package se.chalmers.kangaroo.view;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 
 /**
  * A view to show when the game is paused. Shows when the game is paused and
  * shows some of the alternatives that can be shown in the menu
  * 
  * @author arvidk
  * 
  */
 public class PauseView extends JPanelWithBackground implements MouseListener {
 	private Menubutton resume, highScore, options, exitGame;
 	private String viewName = "gameview";
 	private ChangeView cv;
 	private GameView gv;
 
 	/**
 	 * The constructor for the PauseView. Takes a string as a paramether where
 	 * the background image is located. Also takes a changeView so it can change
 	 * the veiw to options, hiscore etc.
 	 * 
 	 * @param bgpath
 	 *            , cv
 	 */
 	public PauseView(String bgpath, ChangeView cv, GameView gv) {
 		super(bgpath);
 		this.cv = cv;
 		this.gv = gv;
 		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
 		this.setLayout(layout);
 		this.setSize(1024, 576);
 		this.setBackground(Color.BLACK);
 		resume = new Menubutton("resources/images/resume.png");
 		highScore = new Menubutton("resources/images/highscore.png");
 		options = new Menubutton("resources/images/options.png");
 		exitGame = new Menubutton("resources/images/exitgame.png");
 		
 		this.add(new Menubutton("resources/images/menu_logo.gif"));
 		this.add(new Menubutton("resources/images/transparent.png"));
 		this.add(resume);
 		this.add(new Menubutton("resources/images/transparent.png"));
 		this.add(highScore);
 		this.add(new Menubutton("resources/images/transparent.png"));
 		this.add(options);
 		this.add(new Menubutton("resources/images/transparent.png"));
 		this.add(exitGame);
 		this.add(new Menubutton("resources/images/transparent.png"));
 		this.add(new Menubutton("resources/images/transparent.png"));
 		this.add(new Menubutton("resources/images/transparent.png"));
 		this.add(new Menubutton("resources/images/transparent.png"));
 		this.add(new Menubutton("resources/images/transparent.png"));
 		this.add(new Menubutton("resources/images/transparent.png"));
 		this.add(new Menubutton("resources/images/stretchbar.png"));
 
 		resume.addMouseListener(this);
 		highScore.addMouseListener(this);
 		options.addMouseListener(this);
 		exitGame.addMouseListener(this);
 	}
 
 	/**
 	 * Overrides the paintComponent. The firsl line makes the
 	 * background transparent. The float value in the first line decides how
 	 * transparent the background shall be. 0 is 100%, 1 is 0%. The second
 	 * decides how transparent the other objects shall be.
 	 */
 	@Override
 	public void paintComponent(Graphics g) {
 		((Graphics2D) g).setComposite(AlphaComposite.getInstance(
 				AlphaComposite.SRC_OVER, 0.2f)); // draw transparent background
 		super.paintComponent(g);
 		((Graphics2D) g).setComposite(AlphaComposite.getInstance(
 				AlphaComposite.SRC_OVER, 1.0f)); // turn on opacity
 	}
 
 	/**
 	 * Changes the button when the mouse hovers over the button. Changes the
 	 * image on the button to the given string.
 	 */
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		if (e.getSource() == resume)
 			resume.setIcon(new ImageIcon("resources/images/resume_onHover.png"));
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
 
 	/**
 	 * Changes the button when the mouse exits the button. Changes the image on
 	 * the button to the given string.
 	 */
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
 
 	/**
 	 * Changes the button when you press the button. Changes the image on the
 	 * button to the given string.
 	 */
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if (e.getSource() == resume)
 			resume.setIcon(new ImageIcon("resources/images/resume_onSelect.png"));
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
 
 	/**
 	 * Changes the button when you release the button. Changes the image on the
 	 * btton to the given string and also changes the view to the one that says
 	 * on the button or exits the game.
 	 */
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (e.getSource() == resume) {
 			resume.setIcon(new ImageIcon("resources/images/resume.png"));
 			gv.togglePause();
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
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		//Not needed
 	}
 
 }
