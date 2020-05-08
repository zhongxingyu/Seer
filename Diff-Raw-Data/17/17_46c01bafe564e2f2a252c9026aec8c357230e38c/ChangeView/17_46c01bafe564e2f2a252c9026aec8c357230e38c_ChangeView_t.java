 package se.chalmers.kangaroo.view;
 
 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import se.chalmers.kangaroo.model.GameModel;
 
 /**
  * A class that lets you change between the different views in the game. The
  * changeview is a JFrame.
  * 
  * @author Arvid
  * 
  */
 public class ChangeView extends JFrame {
 
 	private JPanel jp;
 	private OptionView ov;
 	private HighscoreView hv;
 	private MenuView mv;
 	private GameView gv;
 	private ShowHighscoreView shv;
 	private FinishedView fv;
 	private String prevView;
 	private GameModel gm;
 
 	/**
 	 * Adds a panel to itself and places the diffrent views in itself. The
 	 * ChangeView frame contains a JPanel that has a cardlayout. ChangeView then
 	 * swwitches between these cards when the right metods are called upon.
 	 */
 
 	public ChangeView(GameModel gm) {
 		this.gm = gm;
 		ov = new OptionView("resources/images/background.gif", this);
 		hv = new HighscoreView("resources/images/background.gif", this);
 		mv = new MenuView("resources/images/background.gif", this);
 		fv = new FinishedView(this);
 		shv = new ShowHighscoreView("resources/images/background.gif", this, 0);
 
 		jp = new JPanel(new CardLayout());
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(1024, 576);
 		setResizable(false);
 		Dimension win = Toolkit.getDefaultToolkit().getScreenSize();
 		int width = getSize().width;
 		int height = getSize().height;
 		int xPos = (win.width - width) / 2;
 		int yPos = (win.height - height) / 2;
 		setLocation(xPos, yPos);
 		jp.add(mv, "menuview");
 		jp.add(ov, "optionview");
 		jp.add(hv, "highscoreview");
 		jp.add(fv, "finishedview");
 		jp.add(shv, "showHighscoreView");
 		add(jp);
 		setVisible(true);
 		jp.setVisible(true);
 	}
 	/**
 	 * Returns the gamemodel
 	 * @return
 	 */
 	public GameModel getGameModel(){
 		return gm;
 	}
 	
 	public void setGameView(GameView gameview){
 		this.gv = gameview;		
 		jp.add(gv, "gameview");
 	}
 
 	/**
 	 * A method to change to the previous view.
 	 */
 	public void back() {
 		CardLayout cl = (CardLayout) jp.getLayout();
 		cl.show(jp, prevView);
 	}
 
 	/**
 	 * A method to change to the MenuView.
 	 */
 	public void menuView() {
 		CardLayout cl = (CardLayout) jp.getLayout();
 		cl.show(jp, "menuview");
 		mv.requestFocus();
 	}
 
 	/**
 	 * A method to change to the GameView. Also starts the game.
 	 */
 	public void gameView() {
 		gv.start();
 		CardLayout cl = (CardLayout) jp.getLayout();
 		cl.show(jp, "gameview");
 		gv.requestFocus();
 
 	}
 
 	/**
 	 * A method to change to the OptionView. Takes a String as a parameter so
 	 * the ChangeView knows where the request to change the view is coming from.
 	 * 
 	 * @param prevView
 	 */
 	public void optionView(String prevView) {
 		this.prevView = prevView;
 		CardLayout cl = (CardLayout) jp.getLayout();
 		cl.show(jp, "optionview");
 	}
 
 	/**
 	 * A method to change to the HighscoreView. Takes a String as a parameter so
 	 * the ChangeView knows where the request to change the view is coming from.
 	 * 
 	 * @param prevView
 	 */
 	public void highscoreView(String prevView) {
 		this.prevView = prevView;
 		CardLayout cl = (CardLayout) jp.getLayout();
 		cl.show(jp, "highscoreview");
 	}
 	
	/**
	 * A method ro change to the highscoreview.
	 * This method doesn't take a string from the previous view.
	 * This is because it's not always nessesary.
	 */
	public void highscoreView() {
		CardLayout cl = (CardLayout) jp.getLayout();
		cl.show(jp, "highscoreview");
	}
	
 	public void finishedView(){
 		CardLayout cl = (CardLayout) jp.getLayout();
 		cl.show(jp, "finishedview");
 	}
 	
 	//TODO: Fix crypt
 	public void showHighscoreView(int level) {
 		shv = new ShowHighscoreView("resources/images/background.gif", this, level);
 		CardLayout cl = (CardLayout) jp.getLayout();
 		jp.add(shv, "showhighscoreview");
 		cl.show(jp, "showhighscoreview");
 	}
 }
