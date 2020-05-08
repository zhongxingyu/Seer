 package backgammon.view;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 
 import backgammon.controller.IControllerDelegate;
 import backgammon.event.CheckerMoveEvent;
 import backgammon.event.DiceEvent;
 import backgammon.event.DiceEvent.diceType;
 import backgammon.event.ActivePlayerInfoEvent;
 import backgammon.event.ExceptionEvent;
 import backgammon.listener.IModelEventListener;
 import backgammon.model.player.Move;
 import backgammon.view.helpers.ImageBoard;
 import backgammon.view.helpers.TopPanel;
 
 public class BackgammonViewGUI implements IModelEventListener, ActionListener {
 
 	/**
 	 * Represents the controller
 	 */
 	protected IControllerDelegate controller;
 
 	/**
 	 * The JFram to draw on
 	 */
 	private JFrame board;
 
 	/**
 	 * The JFrame for the history
 	 */
 	@SuppressWarnings("unused")
 	private JFrame hist;
 
 	private Image pl1_checker;
 	private Image pl2_checker;
 
 	private ImageBoard imageBoard;
 
 	private JButton newGame;
 
 	private JButton showHistory;
 
 	private JButton startGame;
 
 	private JButton exitProgram;
 
 	/**
 	 * Normal Constructor
 	 * 
 	 * @param controller
 	 *            The controller instance
 	 */
 	public BackgammonViewGUI(IControllerDelegate controller) {
 		super();
 		this.controller = controller;
 
 		this.loadChecker();
 	}
 
 	private void loadChecker() {
 
 		this.pl1_checker = new ImageIcon(getClass().getResource(
 				"/img/"
 						+ this.controller.getCurrentGameSettings()
 								.getPathCheckerPlayer1())).getImage();
 		this.pl2_checker = new ImageIcon(getClass().getResource(
 				"/img/"
 						+ this.controller.getCurrentGameSettings()
 								.getPathCheckerPlayer2())).getImage();
 	}
 	
 	public void destroyGUI()
 	{
 		this.imageBoard.destroyThreads();
 		//Aufrumen
 	}
 	
 	public void initGUI(String title) {
 		// Init MainFrame
 		JFrame temp = new JFrame(title);
 		this.board = temp;
 
 		// Get Board
 		this.imageBoard = this.drawBoard();
 
 		this.board.getContentPane().setLayout(new BorderLayout());
 		this.board.setResizable(false);
 		// Add Board
 		this.board.add(this.imageBoard, BorderLayout.CENTER);
 
 		// Add TOP menu bar
 		this.board.add(this.drawTopBar(), BorderLayout.NORTH);
 
 		// Set Frame
 		// this.board.setLocationRelativeTo(null);
 
 		// Make all visible
 		this.board.pack();
 		this.board.setVisible(true);
 
 		/*
 		 * JFrame hist = new JFrame("History");
 		 * 
 		 * hist.setSize(300,600);
 		 * hist.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
 		 * hist.setResizable(false); hist.setLocationRelativeTo(null);
 		 * hist.setVisible(false); this.hist = hist;
 		 */
 		
 		//Testexecption
 		//this.showException(null);
 	}
 
 	/**
 	 * 
 	 * This function draws a checker from one prime to another. The function
 	 * will NOT check if there are other checkers at the stopping prime
 	 * (opponents one) and will take the first checker from the starting prime.
 	 * 
 	 * @param from
 	 *            The starting prime as integer.
 	 * @param to
 	 *            The prime the checker should be moved to.
 	 * @return Boolean Indicates whether the move has been drawn successfully or
 	 *         not
 	 */
 	private boolean moveChecker(Move move) {
 		this.imageBoard.moveChecker(move);
 		return false;
 	}
 
 	/**
 	 * This function will render ONLY the Board on the internal Frame
 	 * 
 	 * @return Boolean whether the Board has been drawn successfully or not
 	 */
 	private ImageBoard drawBoard() {
 
 		ImageBoard panel = new ImageBoard(this, "/img/"
 				+ this.controller.getCurrentGameSettings().getPathBoard());
 
 		return panel;
 	}
 
 	private JPanel drawTopBar() {
 		JPanel bar = new JPanel();
 		JPanel pl1_bar = this.drawPl1Bar();
 		JPanel pl2_bar = this.drawPl2Bar();
 		JPanel button_bar = this.drawButtonBar();
 
 		// Set Border
 		button_bar.setBorder(new EtchedBorder());
 
 		// Set dimensions
 		Dimension m_bars = new Dimension();
 		m_bars.height = 70;
 		m_bars.width = 265;
 
 		// Add Dimensions
 		button_bar.setPreferredSize(m_bars);
 
 		// Add to bar
 		bar.add(pl1_bar, BorderLayout.WEST);
 		bar.add(pl2_bar, BorderLayout.CENTER);
 		bar.add(button_bar, BorderLayout.EAST);
 
 		return bar;
 	}
 
 	private JPanel drawPl1Bar() {
 		return new TopPanel(this.controller.getCurrentGameSettings()
 				.getPathCheckerPlayer1(), this.controller
 				.getCurrentGameSettings().getNamePlayer1());
 	}
 
 	private JPanel drawPl2Bar() {
 		return new TopPanel(this.controller.getCurrentGameSettings()
 				.getPathCheckerPlayer2(), this.controller
 				.getCurrentGameSettings().getNamePlayer2());
 	}
 
 	private JPanel drawButtonBar() {
 		JPanel tmp = new JPanel();
 
 		this.newGame = new JButton("Neues Spiel");
 		this.newGame.setPreferredSize(new Dimension(125, 25));
 		tmp.add(newGame, BorderLayout.WEST);
 		newGame.addActionListener(this);
 
 		this.showHistory = new JButton("History");
 		this.showHistory.setPreferredSize(new Dimension(125, 25));
 		tmp.add(showHistory, BorderLayout.EAST);
 
 		this.exitProgram = new JButton("Spiel Beenden");
 		this.exitProgram.setPreferredSize(new Dimension(125, 25));
 		tmp.add(this.exitProgram, BorderLayout.SOUTH);
 		this.exitProgram.addActionListener(this);
 
 		this.startGame = new JButton("Spiel starten");
 		this.startGame.setPreferredSize(new Dimension(125, 25));
 		tmp.add(startGame, BorderLayout.SOUTH);
		startGame.addActionListener(this);
 
 		return tmp;
 	}
 
 	public Image getChecker(int pl) {
 
 		if (pl == 1)
 			return pl1_checker;
 		else
 			return pl2_checker;
 	}
 
 	public Image getDice(int pl, int i) {
 
 		if (pl == 1)
 			return new ImageIcon(getClass().getResource(
 					"/img/"
 							+ this.controller.getCurrentGameSettings()
 									.getPathDicePlayer1() + i + ".png"))
 					.getImage();
 		else
 			return new ImageIcon(getClass().getResource(
 					"/img/"
 							+ this.controller.getCurrentGameSettings()
 									.getPathDicePlayer2() + i + ".png"))
 					.getImage();
 	}
 
 	public IControllerDelegate getController() {
 		return this.controller;
 	}
 
 	@Override
 	public int handleCheckerMoveEvent(CheckerMoveEvent event) {
 
 		if (event.getMove().isSetMove())
 			this.imageBoard.addChecker(event.getMove().getID(), event.getMove()
 					.getFromPoint(), event.getMove().getFromIndex());
 		else {
 			// this.imageBoard.addChecker(event.getMove().getID(),
 			// event.getMove().getFromPoint(), event.getMove().getFromIndex());
 			this.moveChecker(event.getMove());
 		}
 		return 0;
 	}
 
 	@Override
 	public int handleDiceEvent(DiceEvent event) {
 		
 		//Wrfel leeren
 		this.imageBoard.getDices().clear();
 		
 		if (event.getDiceType() == diceType.DICE) {
 			//System.out.println("Ich wrfel");
 			if (event.getPlayerID() == 0) {
 				this.imageBoard.addDice(1, event.getDiceResult().get(0),1);
 				this.imageBoard.addDice(2, event.getDiceResult().get(1),1);
 			} else 
 			{
 				this.imageBoard.addDice(event.getPlayerID(), event.getDiceResult().get(0),1);
 				this.imageBoard.addDice(event.getPlayerID(), event.getDiceResult().get(1),2);
 			}
 		}
 		else if(event.getDiceType() == diceType.DOUBLE_DICE)
 		{
 			this.imageBoard.addDice(event.getPlayerID(), event.getDiceResult().get(0),0);
 		}
 		return 0;
 	}
 
 	@Override
 	public int handleActivePlayerInfoEvent(ActivePlayerInfoEvent event) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		//System.out.println("Test1");
 
 		if (e.getSource() == this.newGame) {
 			this.board.dispose();
 			this.controller.exitGame();
 		}
 		if (e.getSource() == this.startGame) {
 					
 //			this.board.dispose();
 			this.controller.initGame();
 		}
 		if (e.getSource() == this.exitProgram) {
 			
 			this.destroyGUI();
			this.controller.initGame();
 		}
 
 	}
 
 	@Override
 	public int handleExceptionEvent(ExceptionEvent event) {
 		
 		this.showException(event);
 		
 		return 0;
 	}
 
 	private void showException(ExceptionEvent event) {
 
 		String msg = "";
 		if(event == null)
 		{
 			msg = "Es ist ein TestFehler aufgetreten.";
 		}
 		else if(event.getError() == ExceptionEvent.errorType.CHECKER_MOVE)
 		{
 			msg = "Es ist ein Fehler beim Ziehen eines Checkers aufgetreten.";
 		}
 		else if(event.getError() == ExceptionEvent.errorType.DICE)
 		{
 			msg = "Es ist ein Fehler Wrfeln aufgetreten.";
 		}
 		else if(event.getError() == ExceptionEvent.errorType.INIT)
 		{
 			msg = "Es ist ein Fehler bei der Initialisierung aufgetreten.";
 		}
 		
 		
 		Object[] options = 
 			{
 				"Neues Spiel",
 				"Programm beenden"
 			};
 		int n = JOptionPane.showOptionDialog
 		(
 			this.board,
 			msg,
 			"Es ist ein Fehler aufgetreten",
 			JOptionPane.YES_NO_OPTION,
 			JOptionPane.ERROR_MESSAGE,
 			null,     //do not use a custom Icon
 			options,  //the titles of buttons
 			options[0]
 		); //default button title
 		
 		if(n == 0)
 		{
 			//Spiel neu starten
 			this.board.dispose();
 			this.controller.exitGame();
 		}
 		else
 		{
 			//Programm beenden
 			this.board.dispose();
 			System.exit(1);
 		}
 		
 	}
 
 }
