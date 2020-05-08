 package de.htwg.se.dog.view;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Event;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import com.google.inject.Inject;
 
 import de.htwg.se.dog.controller.GameTableInterface;
 import de.htwg.se.dog.models.PlayerInterface;
 import de.htwg.se.dog.models.impl.Card;
 import de.htwg.se.dog.util.IOEvent;
 import de.htwg.se.dog.util.IOMsgEvent;
 import de.htwg.se.dog.util.IObserver;
 import de.htwg.se.dog.view.modules.ColorMap;
 import de.htwg.se.dog.view.modules.GuiDrawFigures;
 import de.htwg.se.dog.view.modules.GuiDrawGameField;
 import de.htwg.se.dog.view.modules.OverlapLayout;
 
 /**
  * Builds the graphical userinterface
  * 
  * @author Michael
  * 
  */
 public class GraphicalUserInterface extends JFrame implements IObserver {
 
 	private static final int THIRTEEN = 13;
 	private static final String TAHOMA = "Tahoma";
 	private static final int SEVENHUNDRET = 701;
 	private static final int THTHIRTYSIX = 336;
 	private static final int CARD4 = 4;
 	private static final int CARD11 = 11;
 	private static final int NINETY = 90;
 	private static final int SHSEVENTYEIGHT = 678;
 	private static final int THTHIRTYSEVEN = 337;
 	private static final long serialVersionUID = 1L;
 	private final ImageIcon icon = new ImageIcon(this.getClass().getResource(
 			"/dog_icon.png"));
 	private final Container contentPane;
 	private final GameTableInterface controller;
 	private final ColorMap col = new ColorMap();
 	private final JLabel tFieldCurrentPlayer, tFieldRound;
 	private final JLabel[] cards;
 	private final OverlapLayout layout;
 	private Component up;
 	private final GuiDrawFigures figures;
 	private final GuiDrawGameField gameField;
 	private final JTextArea tAreaStatus;
 	// statics for findbugs
 	private static final int CARD1 = 1;
 	private static final int SIX = 6;
 	private static final int TWELVE = 12;
 	private static final int THIRTEEEN = THIRTEEN;
 	private static final int CARD13 = THIRTEEN;
 	private static final int CARD14 = 14;
 	private static final int FIFTEEN = 15;
 	private static final int SIXTEEN = 16;
 	private static final int TWENTY = 20;
 	private static final int TWENTYTWO = 22;
 	private static final int TWENTYFIVE = 25;
 	private static final int THIRTY = 30;
 	private static final int FOURTYFIVE = 45;
 	private static final int SEVENTYFIVE = 75;
 	private static final int EIGHTY = 80;
 	private static final int NINETYFIVE = 95;
 	private static final int NINETYSEVEN = 97;
 	private static final int HUNDRET = 100;
 	private static final int HUNDRETTEN = 110;
 	private static final int HUNDRETTWENTY = 120;
 	private static final int HUNDRETTHIRTY = 130;
 	private static final int TWOHUNDRETTEN = 210;
 	private static final int THREEHUNDRET = 300;
 	private static final int SIXHUNDRET = 600;
 	private static final int SIXHUNDRETEIGHTEEN = 618;
 	private static final int TEXTFIELDY = 633;
 	private static final int SIXHUNDRETFOURTY = 640;
 	private static final int SIXHUNDRETSIXTY = 660;
 	private static final int GAMEFIELDY = 750;
 	private static final int WINDOWY = 800;
 	private static final int NHFIFTY = 950;
 	private static final int GAMEFIELDX = 1270;
 	private static final int WINDOWX = 1280;
 
 	/**
 	 * Create the frame.
 	 * 
 	 * @throws InterruptedException
 	 */
 	public GraphicalUserInterface(final GameTableInterface controller) {
 		controller.addObserver(this);
 		setResizable(false);
 		contentPane = this.getContentPane();
 		this.setIconImage(icon.getImage());
 		this.setVisible(true);
 		this.controller = controller;
 		this.setTitle("DogGame");
 		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		this.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				int quit = JOptionPane.showConfirmDialog(contentPane,
 						"Wirklich beenden?", "Beenden",
 						JOptionPane.YES_NO_OPTION);
 				if (quit == JOptionPane.YES_OPTION) {
 					System.exit(0);
 				}
 			}
 		});
 		this.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 			}
 
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
 					moveToDestination();
 				}
 
 			}
 		});
 		setBounds(HUNDRET, HUNDRET, WINDOWX, WINDOWY);
 
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 
 		JMenu mnGame = new JMenu("Game");
 		menuBar.add(mnGame);
 
 		JMenuItem mnExit = new JMenuItem("Dog beenden?");
 		mnExit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (JOptionPane.showConfirmDialog(contentPane,
 						"Wirklich beenden?", "Beenden",
 						JOptionPane.YES_NO_OPTION) == 0) {
 					System.exit(0);
 				}
 			}
 		});
 		mnExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
 				Event.ALT_MASK));
 		mnExit.setIcon(new ImageIcon(this.getClass().getResource("/off.png")
 				.getPath()));
 		mnGame.addSeparator();
 		mnGame.add(mnExit);
 
 		gameField = new GuiDrawGameField(controller);
 		gameField.setBounds(0, 0, GAMEFIELDX, GAMEFIELDY);
 		this.add(gameField);
 		gameField.setBackground(Color.WHITE);
 		gameField.setLayout(null);
 
 		tFieldCurrentPlayer = new JLabel();
 		tFieldCurrentPlayer.setBorder(BorderFactory
 				.createLineBorder(Color.white));
 		tFieldCurrentPlayer.setBounds(TWELVE, TEXTFIELDY, NINETYSEVEN,
 				GraphicalUserInterface.TWENTYTWO);
 		gameField.add(tFieldCurrentPlayer);
 		tFieldCurrentPlayer.setFont(new Font(TAHOMA, Font.BOLD, FIFTEEN));
 		tFieldCurrentPlayer.setBackground(Color.WHITE);
 
 		JLabel lbCurrentPlayer = new JLabel("CurrentPlayer");
 		lbCurrentPlayer.setBounds(TWELVE,
 				GraphicalUserInterface.SIXHUNDRETEIGHTEEN, HUNDRET, SIXTEEN);
 		gameField.add(lbCurrentPlayer);
 		lbCurrentPlayer.setFont(new Font(TAHOMA, Font.BOLD, THIRTEEEN));
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setEnabled(false);
 		scrollPane.setBorder(null);
 		scrollPane.setBounds(HUNDRETTWENTY, SIXHUNDRET, TWOHUNDRETTEN,
 				HUNDRETTHIRTY);
 		gameField.add(scrollPane);
 
 		layout = new OverlapLayout(new Point(TWENTYFIVE, 0));
 		layout.setPopupInsets(new Insets(TWENTY, 0, 0, 0));
 		JPanel cardHand = new JPanel(layout);
 		scrollPane.setViewportView(cardHand);
 		cardHand.setBackground(Color.WHITE);
 		figures = new GuiDrawFigures();
 		figures.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				int card = getValueForCardIcon();
 				if ((card == CARD1 || card == CARD13 || card == CARD14)
 						&& !controller.isPlayerStartfieldBlocked()) {
 					int quit = JOptionPane.showConfirmDialog(contentPane,
 							"Spielfigur aufs Spielfeld setzen?", "Rausgehen?",
 							JOptionPane.YES_NO_OPTION);
 					if (quit == JOptionPane.YES_OPTION
 							&& controller.moveFigureToStart(card)) {
 						controller.nextPlayer();
 						controller.notifyObservers();
 					}
 				}
 			}
 		});
 		figures.setBounds(THIRTY, SIXHUNDRETSIXTY, FOURTYFIVE, FOURTYFIVE);
 		gameField.add(figures);
 		figures.setBackground(Color.WHITE);
 		figures.setLayout(null);
 
 		JScrollPane panetAreaStatus = new JScrollPane();
 		panetAreaStatus.setEnabled(false);
 		panetAreaStatus.setBorder(BorderFactory.createLineBorder(Color.white));
 		panetAreaStatus
 				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
 		panetAreaStatus
 				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		panetAreaStatus.setBounds(NHFIFTY, SIXHUNDRETFOURTY, THREEHUNDRET,
 				EIGHTY);
 		gameField.add(panetAreaStatus);
 
 		tAreaStatus = new JTextArea();
 		tAreaStatus.setLineWrap(true);
 		tAreaStatus.setFont(new Font(TAHOMA, Font.PLAIN, THIRTEEEN));
 		tAreaStatus.setEditable(false);
 		panetAreaStatus.setViewportView(tAreaStatus);
 		cards = new JLabel[SIX];
 		for (int i = 0; i < cards.length; i++) {
 			cards[i] = new JLabel();
 			cards[i].setBounds(0, 0, SEVENTYFIVE, NINETYFIVE);
 			cards[i].setPreferredSize(new Dimension(EIGHTY, HUNDRETTEN));
 			cards[i].setName(String.valueOf(i));
 			cards[i].addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent e) {
 					// joker was recently used the highlighted card hast to be
 					// played
 					cardOut(e.getComponent());
 				}
 			});
 			cardHand.add(cards[i]);
 		}
 		JButton btnGo = new JButton("GO!");
 		btnGo.setFocusable(false);
 		btnGo.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				moveToDestination();
 			}
 		});
 		btnGo.setBounds(THTHIRTYSIX, SEVENHUNDRET, HUNDRET, TWENTYFIVE);
 		gameField.add(btnGo);
 
 		tFieldRound = new JLabel();
 		gameField.add(tFieldRound);
 		tFieldRound.setFont(new Font(TAHOMA, Font.BOLD, THIRTEEEN));
 		tFieldRound.setBorder(BorderFactory.createLineBorder(Color.white));
 		tFieldRound.setBackground(Color.WHITE);
 		tFieldRound.setBounds(THTHIRTYSEVEN, SHSEVENTYEIGHT, NINETY, TWENTY);
 	}
 
 	@Override
 	public void update(IOEvent e) {
 		if (e instanceof IOMsgEvent) {
 			SimpleDateFormat dateF = new SimpleDateFormat("HH:mm");
 			String date = dateF.format(new Date());
 			tAreaStatus.append(String.format("[%s] %s\n", date,
 					((IOMsgEvent) e).getMessage()));
 			tAreaStatus.setCaretPosition(tAreaStatus.getDocument().getLength());
 
 		} else {
 			int playerID = controller.getCurrentPlayerID();
 			tFieldCurrentPlayer.setForeground(col.getColor(playerID));
 			tFieldCurrentPlayer.setText(controller.getPlayerString());
 			tFieldRound.setText(String.format("Round: %d",
 					controller.getRound()));
 			// update the figures sysmbol
 			figures.changePlayer(playerID, controller.getCurrentPlayer()
 					.getFigureList().size());
 			// clear gamefield highlighters
 			gameField.clearField();
 			// reset highlighted card
 			clearHighlightedCard();
 			/* reset the labels */
 			repaintCardLabels();
 			this.repaint();
 		}
 	}
 
 	/**
 	 * removes the card highlighted
 	 */
 	private void clearHighlightedCard() {
 		up = null;
 		layout.resetHighlighters();
 		cards[1].getParent().invalidate();
 		cards[1].getParent().validate();
 	}
 
 	/**
 	 * repaint the label icons and make unessesary icons invisible
 	 */
 	private void repaintCardLabels() {
 
 		int cardListSize = controller.getCurrentPlayer().getCardList().size();
 		for (int i = 0; i < cards.length; i++) {
 			if (i < cardListSize) {
 				int card = controller.getCurrentPlayer().getCardList().get(i)
 						.getValue();
 				cards[i].setIcon(new ImageIcon(getClass().getResource(
 						String.format("/%d.gif", card))));
 				cards[i].setVisible(true);
 				continue;
 			}
 			cards[i].setVisible(false);
 		}
 	}
 
 	/**
 	 * resets the position of the cardlabel, so it becomes
 	 * highlighted/dehighlighted
 	 * 
 	 * @param c
 	 *            the component that will be highlighted
 	 */
 	private void cardOut(Component c) {
 		Boolean constraint = layout.getConstraints(c);
 		if (constraint == null || constraint == OverlapLayout.POPDOWN) {
 			clearHighlightedCard();
 			layout.addLayoutComponent(c, OverlapLayout.POPUP);
 			up = c;
 		} else {
 			layout.addLayoutComponent(c, OverlapLayout.POPDOWN);
 			up = null;
 		}
 		allowSecondHighlighter();
 		c.getParent().invalidate();
 		c.getParent().validate();
 	}
 
 	/**
 	 * allows a second field to be highlighted in the gamfield
 	 */
 	private void allowSecondHighlighter() {
 		int cardval = getValueForCardIcon();
 		gameField.allowSecond(false);
 		if (cardval == CARD11) {
 			gameField.allowSecond(true);
 		}
 	}
 
 	/**
 	 * returns the value for the card that was highlighted
 	 * 
 	 * @return value for the card, or -1 if no card was highlighted
 	 */
 	private int getValueForCardIcon() {
 		int ret = -1;
 		if (up != null) {
 			int index = Integer.parseInt(up.getName());
 			int card = controller.getCurrentPlayer().getCardList().get(index)
 					.getValue();
 			ret = card;
 		}
 		return ret;
 	}
 
 	/**
 	 * moves figure to destination
 	 */
 	private void moveToDestination() {
 		final PlayerInterface current = controller.getCurrentPlayer();
 		Integer from = gameField.getFromFieldID();
 		Integer to = gameField.getToFieldID();
 		int steps = 0;
 		int cardval = getValueForCardIcon();
 		if (from != null && cardval != -1) {
 			if (cardval == CARD1) {
 				steps = cardAceDialog();
 			} else if (cardval == CARD4) {
 				steps = card4Dialog(cardval);
 			} else if (cardval == CARD11) {
 				if (to != null) {
 					steps = to;
 				}
 			} else if (cardval == CARD14) {
 				jokerSpinnerDialog(current);
				return;
 			} else {
 				steps = cardval;
 			}
 			if (controller.isValidMove(cardval, steps, from)) {
 				controller.playCard(cardval, steps, from);
 				winnerDialog();
 				controller.nextPlayer();
 				controller.notifyObservers();
 			}
 		}
 	}
 
 	/**
 	 * Displays a winning dialog
 	 */
 	private void winnerDialog() {
 		if (controller.currentPlayerHaswon()) {
 			Object[] options = { "Exit" };
 			String won = String.format(
 					"Glckwunsch Spieler %d du hast gewonnen!!",
 					controller.getCurrentPlayerID());
 			int decision = JOptionPane.showOptionDialog(this, won, "SIEG",
 					JOptionPane.YES_OPTION, JOptionPane.INFORMATION_MESSAGE,
 					icon, options, options[0]);
 			if (decision < 1) {
 				System.exit(0);
 			}
 		}
 	}
 
 	/**
 	 * creates a dialog for card ass (ace)
 	 * 
 	 * @param from
 	 * @param move
 	 */
 	private int cardAceDialog() {
 		int retVal = CARD11;
 		Object[] options = { "1", "11" };
 		int decision = JOptionPane.showOptionDialog(this,
 				"Wieviel mchtest du laufen?", "Laufwert?",
 				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
 				icon, options, options[1]);
 
 		if (decision == JOptionPane.YES_OPTION) {
 			retVal = CARD1;
 		}
 		return retVal;
 	}
 
 	/**
 	 * creates the dialog for card 4 whether to run forward or backward
 	 * 
 	 * @param from
 	 * @param cardval
 	 * @param move
 	 */
 	private int card4Dialog(int cardval) {
 		int retVal = -cardval;
 		Object[] options = { "Vorwrts", "Rckwrts" };
 		int decision = JOptionPane.showOptionDialog(this,
 				"In welche Richtung mchtest du laufen?", "Welche Richtung?",
 				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
 				icon, options, options[1]);
 
 		if (decision == JOptionPane.YES_OPTION) {
 			retVal = cardval;
 		}
 		return retVal;
 	}
 
 	/**
 	 * @param current
 	 * @param cardval
 	 */
 	@Inject
 	private void jokerSpinnerDialog(final PlayerInterface current) {
 		JPanel input = new JPanel();
 		SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, THIRTEEN, 1);
 		final JSpinner spinner = new JSpinner(sModel);
 		final JLabel spinnerLabel = new JLabel("Ass");
 
 		spinner.addChangeListener(new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				spinnerLabel.setText(String.format(" %s",
 						Card.CARDNAMES[(Integer) spinner.getValue() - 1]));
 			}
 		});
 		input.add(spinner);
 		input.add(spinnerLabel);
 		int decision = JOptionPane.showOptionDialog(contentPane, input,
 				"Karte auswhlen", JOptionPane.OK_CANCEL_OPTION,
 				JOptionPane.QUESTION_MESSAGE, icon, null, null);
 		if (decision == JOptionPane.YES_OPTION) {
 			// clear old highlighter
 			clearHighlightedCard();
 			// add the "new card"
 			int newCard = (Integer) spinner.getValue();
 			cardOut(cards[current.getCardList().lastIndexOf(
 					controller.playJoker(newCard))]);
 			// repaint the labels and highlight the card
 			repaintCardLabels();
 		}
 	}
 }
