 ﻿package rps.client.ui;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import rps.game.Game;
 import rps.game.data.Figure;
 import rps.game.data.FigureKind;
 import rps.game.data.Move;
 import rps.game.data.Player;
 
 /**
  * Creates the pane for the game core.
  */
 public class GamePane {
 
 	private final JPanel gamePane = new JPanel();
 	private final JTextField chatInput = new JTextField();
 	private final JTextArea chat = new JTextArea(4, 30);
 	private final JTextArea log = new JTextArea(4, 30);
 	private final JScrollPane scrollPane = new JScrollPane(chat);
 	private final JScrollPane logPane = new JScrollPane(log);
 	private final JPanel boardBackground = new JPanel();
 	private final JPanel boardFigures = new JPanel();
 	private final JPanel boardArrows = new JPanel();
 	private final JPanel boardDiscovered = new JPanel();
 	private final JPanel boardButtons = new JPanel();
 
 	private final JFrame frame = new JFrame();
 	private final JFrame frame2 = new JFrame();
 
 	private Game game;
 	private Player player;
 
 	public boolean myTurn;
 
 	private ImageIcon iconWhite;
 	private ImageIcon iconBlack;
 	private ImageIcon emptyIcon;
 	private ImageIcon discoveredIcon;
 	private ImageIcon unknownIcon;
 	private ImageIcon boarderIcon;
 	private ImageIcon arrowUp;
 	private ImageIcon arrowDown;
 	private ImageIcon arrowLeft;
 	private ImageIcon arrowRight;
 
 	private ImageIcon redTrap;
 	private ImageIcon redFlag;
 	private ImageIcon redRock;
 	private ImageIcon redPaper;
 	private ImageIcon redScissors;
 
 	private ImageIcon blueTrap;
 	private ImageIcon blueFlag;
 	private ImageIcon blueRock;
 	private ImageIcon bluePaper;
 	private ImageIcon blueScissors;
 
 	private Figure[] board = new Figure[42];
 	private FigureKind[] initialAssignment = new FigureKind[42];
 
 	public String themePath = "img/default/";
 
 	public String gamePhase = "initFlag";
 	private boolean pick = false;
 	private int pickedPosition;
 	private int positionFlag;
 	private int positionTrap;
 
 	private Figure[] oldBoard;
 
 	public boolean chosen = false;
 	public int choosenPosition;
 
 	private GridBagConstraints gbcBackground = new GridBagConstraints();
 	private GridBagConstraints gbcFigures = new GridBagConstraints();
 	private GridBagConstraints gbcArrows = new GridBagConstraints();
 	private GridBagConstraints gbcDiscovered = new GridBagConstraints();
 	private GridBagConstraints gbcButtons = new GridBagConstraints();
 	private JLabel[] backgroundTiles = new JLabel[42];
 	private JLabel[] arrows = new JLabel[42];
 	private JLabel[] figures = new JLabel[42];
 	private JLabel[] discovered = new JLabel[42];
 	private JButton[] fieldButtons = new JButton[42];
 	private JButton acceptLineUp = new JButton("Aufstellung akzeptieren");
 	private JButton mixLineUp = new JButton("Neu mischen");
 
 	private JLabel turnInfo = new JLabel("");
 
 	private JFrame memePane = new JFrame();
 	private JLabel picture;
 	private AePlayWave sndTrap;
 
 	/**
 	 * Adds several labels and buttons to the game pane and adds itself to the
 	 * parent container.
 	 * 
 	 * @param parent
 	 */
 	public GamePane(Container parent) {
 		gamePane.setLayout(null);
 
 		boardBackground.setBounds(20, 15, 700, 600);
 		boardFigures.setBounds(20, 15, 700, 600);
 		boardDiscovered.setBounds(20, 15, 700, 600);
 		boardArrows.setBounds(20, 15, 700, 600);
 		boardButtons.setBounds(20, 15, 700, 600);
 
 		turnInfo.setBounds(740, 5, 225, 40);
 		turnInfo.setVisible(true);
 
 		logPane.setBounds(740, 35, 225, 580);
 
 		scrollPane.setBounds(20, 630, 700, 80);
 		chatInput.setBounds(20, 710, 700, 20);
 
 		acceptLineUp.setBounds(740, 630, 225, 40);
 		mixLineUp.setBounds(740, 690, 225, 40);
 
 		gamePane.add(boardArrows);
 		gamePane.add(boardDiscovered);
 		gamePane.add(boardFigures);
 		gamePane.add(boardBackground);
 		gamePane.add(boardButtons);
 
 		gamePane.add(logPane);
 		gamePane.add(scrollPane);
 		gamePane.add(chatInput);
 		gamePane.add(acceptLineUp);
 		gamePane.add(mixLineUp);
 		gamePane.add(turnInfo);
 
 		sndTrap = new AePlayWave("snd/trap.wav");
 		try {
 			picture = new JLabel(new ImageIcon(ImageIO.read(new File(
 					"img/aTrap.jpg"))));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		memePane.add(picture);
 		memePane.setLocationRelativeTo(null);
 		memePane.pack();
 		memePane.setVisible(false);
 
 		log.setLineWrap(true);
 		log.setEditable(false);
 		chat.setLineWrap(true);
 		chat.setEditable(false);
 		acceptLineUp.setVisible(false);
 		mixLineUp.setVisible(false);
 
 		// the actionlistener for the "mixing-the-line-up" button
 		mixLineUp.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				manualShuffle();
 				arrows[pickedPosition].setIcon(emptyIcon);
 				pick = false;
 				redrawInitialAssignment();
 			}
 		});
 
 		// the actionlistener for the "accept-the-line-up" button
 		acceptLineUp.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				acceptLineUp.setVisible(false);
 				mixLineUp.setVisible(false);
 				arrows[pickedPosition].setIcon(emptyIcon);
 				pick = false;
 				try {
 					game.setInitialAssignment(player, initialAssignment);
 				} catch (RemoteException re) {
 					// RemoteException
 				}
 				gamePhase = "gamePhase";
 				redraw();
 			}
 		});
 
 		this.loadPictures();
 
 		this.drawBackground();
 		this.drawFigures();
 		this.drawArrows();
 		this.drawDiscovered();
 		this.drawButtons();
 
 		gamePane.setVisible(false);
 
 		parent.add(gamePane);
 		bindButtons();
 	}
 
 	/**
 	 * Creates the discovered board layout.
 	 */
 	private void drawDiscovered() {
 		GridBagLayout gbl = new GridBagLayout();
 		this.boardDiscovered.setLayout(gbl);
 		this.boardDiscovered.setOpaque(false);
 		this.gbcDiscovered.fill = GridBagConstraints.HORIZONTAL;
 
 		for (int i = 0; i < 42; i++) {
 			this.discovered[i] = new JLabel(this.emptyIcon);
 			this.discovered[i].setOpaque(false);
 
 			this.gbcDiscovered.gridy = Math.round(i / 7);
 			this.gbcDiscovered.gridx = i % 7;
 			this.gbcDiscovered.gridheight = 1;
 
 			gbl.setConstraints(this.discovered[i], this.gbcDiscovered);
 			this.boardDiscovered.add(this.discovered[i]);
 		}
 
 	}
 
 	/**
 	 * Creates an option dialog of "rock, paper, scissors" to decide, who starts
 	 * the match.
 	 */
 	public void askInitial() {
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Object[] options = { "Schere", "Stein", "Papier" };
 		int n = JOptionPane.showOptionDialog(frame, "Kampf um den Start. "
 				+ "Womit kämpfst du?", "Startkampf",
 				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
 				null, options, options[1]);
 		switch (n) {
 		case 0:
 			try {
 				this.game.setInitialChoice(this.player, FigureKind.SCISSORS);
 			} catch (RemoteException e) {
 				// RemoteException
 			}
 			break;
 		case 1:
 			try {
 				this.game.setInitialChoice(this.player, FigureKind.ROCK);
 			} catch (RemoteException e) {
 				// RemoteException
 			}
 			break;
 		case 2:
 			try {
 				this.game.setInitialChoice(this.player, FigureKind.PAPER);
 			} catch (RemoteException e) {
 				// RemoteException
 			}
 			break;
 		default:
 			try {
 				this.game.setInitialChoice(this.player, FigureKind.ROCK);
 			} catch (RemoteException e) {
 				// RemoteException
 			}
 			break;
 		}
 	}
 
 	/**
 	 * Creates an option dialog of "rock, paper, scissors" after a draw on the
 	 * board.
 	 */
 	public void askAfterDraw() {
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Object[] options = { "Schere", "Stein", "Papier" };
 		int n = JOptionPane.showOptionDialog(frame, "Unentschieden!"
 				+ "Womit kämpfst du?", "Unentschieden",
 				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
 				null, options, options[1]);
 		switch (n) {
 		case 0:
 			try {
 				this.game.setUpdatedKindAfterDraw(this.player,
 						FigureKind.SCISSORS);
 			} catch (RemoteException e) {
 				// RemoteException
 			}
 			break;
 		case 1:
 			try {
 				this.game.setUpdatedKindAfterDraw(this.player, FigureKind.ROCK);
 			} catch (RemoteException e) {
 				// RemoteException
 			}
 			break;
 		case 2:
 			try {
 				this.game
 						.setUpdatedKindAfterDraw(this.player, FigureKind.PAPER);
 			} catch (RemoteException e) {
 				// RemoteException
 			}
 			break;
 		default:
 			try {
 				this.game.setUpdatedKindAfterDraw(this.player, FigureKind.ROCK);
 			} catch (RemoteException e) {
 				// RemoteException
 			}
 			break;
 		}
 	}
 
 	/**
 	 * Gives the option to choose a line up.
 	 */
 	public void askLineup() {
 		this.frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Object[] options = { "Manuell", "Zufällig" };
 		int n = JOptionPane.showOptionDialog(frame, "Kampf um den Start. "
 				+ "Womit kämpfst du?", "Startkampf",
 				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
 				null, options, options[1]);
 
 		switch (n) {
 		case 0:
 			this.printLog("Manuelle Aufstellung");
 			printLog("Setze die Flagge");
 			printLog("---");
 			break;
 		case 1:
 			this.printLog("Zufällige Aufstellung");
 			printLog("---");
 			this.createRandomLineup();
 			redrawInitialAssignment();
 			int i = 0;
 			while (i == 0) {
 				this.createRandomLineup();
 				redrawInitialAssignment();
 				Object[] options2 = { "Neu generieren",
 						"Aufstellung akzeptieren" };
 				i = JOptionPane.showOptionDialog(frame, "Kampf um den Start. "
 						+ "Womit kämpfst du?", "Startkampf",
 						JOptionPane.YES_NO_OPTION,
 						JOptionPane.QUESTION_MESSAGE, null, options2,
 						options2[1]);
 			}
 			try {
 				this.game.setInitialAssignment(this.player, initialAssignment);
 			} catch (RemoteException e) {
 				// RemoteException
 			}
 			gamePhase = "gamePhase";
 			this.redraw();
 			break;
 		default:
 			this.printLog("Zufällige Aufstellung");
 			this.createRandomLineup();
 			redrawInitialAssignment();
 			try {
 				this.game.setInitialAssignment(this.player, initialAssignment);
 			} catch (RemoteException e) {
 				// RemoteException
 			}
 			gamePhase = "gamePhase";
 			this.redraw();
 			break;
 		}
 		this.redraw();
 	}
 
 	/**
 	 * Creates a random line up.
 	 */
 	private void createRandomLineup() {
 		ArrayList<FigureKind> list = new ArrayList<FigureKind>();
 
 		list.add(FigureKind.TRAP);
 		list.add(FigureKind.FLAG);
 
 		for (int i = 0; i < 4; i++) {
 			list.add(FigureKind.PAPER);
 			list.add(FigureKind.ROCK);
 			list.add(FigureKind.SCISSORS);
 		}
 
 		Collections.shuffle(list); // Liste mischen -> zufällige Anordnung
 
 		for (int i = 0; i < list.size(); i++) {
 			initialAssignment[i + 28] = list.get(i);
 		}
 	}
 
 	/**
 	 * Creates and sends a combat log.
 	 */
 	public void printFight() {
 		FigureKind to;
 		try {
 			oldBoard = game.getLastMove().getOldField();
 			if (this.oldBoard[game.getLastMove().getTo()] != null){
 				if(this.oldBoard[game.getLastMove().getTo()].getKind() == FigureKind.TRAP){
 					memePane.setVisible(true);
					sndTrap.run();
 					to = FigureKind.TRAP;
 				}
 				else{
 					to = this.oldBoard[game.getLastMove().getTo()].getKind();
 				}
 				
 				FigureKind from = this.oldBoard[game.getLastMove().getFrom()].getKind();
 				
 				if(this.oldBoard[game.getLastMove().getFrom()].belongsTo(this.player)){
 					printLog(this.player.getNick() + " greift an");
 					printLog(from+ " gegen " + to);
 					printLog("---");
 				}
 				else{
 					printLog(game.getOpponent(this.player).getNick() + " greift an");
 					printLog(from+ " gegen " + to);
 					printLog("---");
 				}
 			}
 		} catch (RemoteException re) {
 			// RemoteException
 		}
 	}
 
 	/**
 	 * Displays, who is next.
 	 * 
 	 * @param text
 	 */
 	public void printTurnInfo(String text) {
 		this.turnInfo.setText(text);
 	}
 
 	/**
 	 * Redraws the initial assignment.
 	 */
 	private void redrawInitialAssignment() {
 		for (int i = 28; i < 42; i++) {
 			if (this.initialAssignment[i] == null)
 				continue;
 			switch (this.initialAssignment[i]) {
 			case TRAP:
 				this.figures[i].setIcon(blueTrap);
 				break;
 			case FLAG:
 				this.figures[i].setIcon(blueFlag);
 				break;
 			case ROCK:
 				this.figures[i].setIcon(blueRock);
 				break;
 			case PAPER:
 				this.figures[i].setIcon(bluePaper);
 				break;
 			case SCISSORS:
 				this.figures[i].setIcon(blueScissors);
 				break;
 			case HIDDEN:
 				this.figures[i].setIcon(unknownIcon);
 				break;
 			default:
 				this.figures[i].setIcon(unknownIcon);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Loads all needed pictures.
 	 */
 	private void loadPictures() {
 		try {
 			this.iconWhite = new ImageIcon(ImageIO.read(new File(this.themePath
 					+ "field_white.png")));
 			this.iconBlack = new ImageIcon(ImageIO.read(new File(this.themePath
 					+ "field_black.png")));
 
 			this.emptyIcon = new ImageIcon(ImageIO.read(new File(
 					"img/empty.png")));
 			this.discoveredIcon = new ImageIcon(ImageIO.read(new File(
 					"img/discovered.png")));
 			this.boarderIcon = new ImageIcon(ImageIO.read(new File(
 					"img/boarder.png")));
 
 			this.arrowUp = new ImageIcon(ImageIO.read(new File(
 					"img/arrow_up.png")));
 			this.arrowDown = new ImageIcon(ImageIO.read(new File(
 					"img/arrow_down.png")));
 			this.arrowLeft = new ImageIcon(ImageIO.read(new File(
 					"img/arrow_left.png")));
 			this.arrowRight = new ImageIcon(ImageIO.read(new File(
 					"img/arrow_right.png")));
 
 			this.unknownIcon = new ImageIcon(ImageIO.read(new File(
 					this.themePath + "unknown.png")));
 			this.redTrap = new ImageIcon(ImageIO.read(new File(this.themePath
 					+ "red_trap.png")));
 			this.redFlag = new ImageIcon(ImageIO.read(new File(this.themePath
 					+ "red_flag.png")));
 			this.redRock = new ImageIcon(ImageIO.read(new File(this.themePath
 					+ "red_rock.png")));
 			this.redPaper = new ImageIcon(ImageIO.read(new File(this.themePath
 					+ "red_paper.png")));
 			this.redScissors = new ImageIcon(ImageIO.read(new File(
 					this.themePath + "red_scissor.png")));
 			this.blueTrap = new ImageIcon(ImageIO.read(new File(this.themePath
 					+ "blue_trap.png")));
 			this.blueFlag = new ImageIcon(ImageIO.read(new File(this.themePath
 					+ "blue_flag.png")));
 			this.blueRock = new ImageIcon(ImageIO.read(new File(this.themePath
 					+ "blue_rock.png")));
 			this.bluePaper = new ImageIcon(ImageIO.read(new File(this.themePath
 					+ "blue_paper.png")));
 			this.blueScissors = new ImageIcon(ImageIO.read(new File(
 					this.themePath + "blue_scissor.png")));
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Draws the background layout.
 	 */
 	private void drawBackground() {
 		GridBagLayout gbl = new GridBagLayout();
 		this.boardBackground.setLayout(gbl);
 		this.boardBackground.setOpaque(false);
 		gbcBackground.fill = GridBagConstraints.HORIZONTAL;
 
 		for (int i = 0; i < 42; i++) {
 			this.gbcBackground.gridy = Math.round(i / 7);
 			this.gbcBackground.gridx = i % 7;
 			this.gbcBackground.gridheight = 1;
 
 			if (i % 2 == 0) {
 				this.backgroundTiles[i] = new JLabel(this.iconWhite);
 				gbl.setConstraints(this.backgroundTiles[i], this.gbcBackground);
 				this.boardBackground.add(this.backgroundTiles[i]);
 			} else {
 				this.backgroundTiles[i] = new JLabel(this.iconBlack);
 				gbl.setConstraints(this.backgroundTiles[i], this.gbcBackground);
 				this.boardBackground.add(this.backgroundTiles[i]);
 			}
 		}
 	}
 
 	/**
 	 * Creates the arrows layout.
 	 */
 	private void drawArrows() {
 		GridBagLayout gbl = new GridBagLayout();
 		this.boardArrows.setLayout(gbl);
 		this.boardArrows.setOpaque(false);
 		this.gbcArrows.fill = GridBagConstraints.HORIZONTAL;
 
 		for (int i = 0; i < 42; i++) {
 			this.arrows[i] = new JLabel(this.emptyIcon);
 			this.arrows[i].setOpaque(false);
 
 			this.gbcArrows.gridy = Math.round(i / 7);
 			this.gbcArrows.gridx = i % 7;
 			this.gbcArrows.gridheight = 1;
 
 			gbl.setConstraints(this.arrows[i], this.gbcArrows);
 			this.boardArrows.add(this.arrows[i]);
 		}
 	}
 
 	/**
 	 * Creates the figures layout.
 	 */
 	private void drawFigures() {
 		GridBagLayout gbl = new GridBagLayout();
 		this.boardFigures.setLayout(gbl);
 		this.boardFigures.setOpaque(false);
 		this.gbcFigures.fill = GridBagConstraints.HORIZONTAL;
 
 		for (int i = 0; i < 42; i++) {
 			this.figures[i] = new JLabel(this.emptyIcon);
 			this.figures[i].setOpaque(false);
 
 			this.gbcFigures.gridy = Math.round(i / 7);
 			this.gbcFigures.gridx = i % 7;
 			this.gbcFigures.gridheight = 1;
 
 			gbl.setConstraints(this.figures[i], gbcFigures);
 			this.boardFigures.add(this.figures[i]);
 		}
 	}
 
 	/**
 	 * Creates the board buttons.
 	 */
 	private void drawButtons() {
 		GridBagLayout gbl = new GridBagLayout();
 		this.boardButtons.setLayout(gbl);
 		this.boardButtons.setOpaque(false);
 		gbcButtons.fill = GridBagConstraints.HORIZONTAL;
 
 		for (int i = 0; i < 42; i++) {
 			this.fieldButtons[i] = new JButton();
 			this.fieldButtons[i].setActionCommand(Integer.toString(i));
 			this.fieldButtons[i].addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent ae) {
 					int position = Integer.parseInt(ae.getActionCommand());
 
 					switch (gamePhase) {
 					case "initFlag":
 						if (position > 27) {
 							initialAssignment[position] = FigureKind.FLAG;
 							redrawInitialAssignment();
 							positionFlag = position;
 							gamePhase = "initTrap";
 							printLog("Setze die Falle");
 							printLog("---");
 						} else {
 							printLog("Nicht im Startgebiet");
 							printLog("---");
 						}
 						break;
 					case "initTrap":
 						if ((position > 27) && (position != positionFlag)) {
 							positionTrap = position;
 							manualShuffle();
 							redrawInitialAssignment();
 							acceptLineUp.setVisible(true);
 							mixLineUp.setVisible(true);
 							gamePhase = "initLineUpChange";
 							printLog("Passe die Startaufstellung an");
 							printLog("---");
 						} else {
 							if (position == positionFlag)
 								printLog("Nicht auf die Flagge setzbar");
 							else
 								printLog("Nicht im Startgebiet");
 							printLog("---");
 						}
 						break;
 					case "initLineUpChange":
 						lineUpChange(position);
 						break;
 					case "gamePhase":
 						moveFigure(position, choosenPosition);
 						break;
 					default:
 						System.err.println("Button broken");
 					}
 				}
 			});
 			this.fieldButtons[i].setOpaque(false);
 			this.fieldButtons[i].setContentAreaFilled(false);
 			this.fieldButtons[i].setBorderPainted(false);
 			this.fieldButtons[i].setRolloverEnabled(false);
 			this.fieldButtons[i].setPreferredSize(new Dimension(100, 100));
 			this.gbcButtons.gridy = Math.round(i / 7);
 			this.gbcButtons.gridx = i % 7;
 			this.gbcButtons.gridheight = 1;
 
 			gbl.setConstraints(this.fieldButtons[i], gbcButtons);
 			this.boardButtons.add(this.fieldButtons[i]);
 		}
 	}
 
 	/**
 	 * Clears all arrows on the board.
 	 */
 	public void cleanArrows() {
 		for (int i = 0; i < 42; i++) {
 			this.arrows[i].setIcon(emptyIcon);
 		}
 	}
 
 	/**
 	 * If a figure is chosen, the possible moves are shown and the figure can be
 	 * moved. Else, one of the own figures can be chosen.
 	 * 
 	 * @param pos1
 	 * @param pos2
 	 */
 	private void moveFigure(int pos1, int pos2) {
 		try {
 			this.board = this.game.getField();
 		} catch (RemoteException re) {
 			// RemoteException
 		}
 		if (chosen) {
 
 			if (pos1 != pos2) {
 				try {
 					game.move(this.player, pos2, pos1);
 				} catch (RemoteException re) {
 					// RemoteException
 				} catch (IllegalStateException ise) {
 					// 2movesException
 				} finally {
 					this.lastMoveArrow();
 					chosen = false;
 					this.myTurn = false;
 				}
 			}
 			this.lastMoveArrow();
 			chosen = false;
 		} else {
 			if (this.board[pos1] != null) {
 				if ((this.board[pos1].belongsTo(this.player))
 						&& (this.board[pos1].getKind() != FigureKind.FLAG)
 						&& (this.board[pos1].getKind() != FigureKind.TRAP)) {
 					int counter = 0;
 					try {
 						if (((pos1 + 1) % 7 != 0)
 								&& ((this.board[pos1 + 1] == null) || !this.board[pos1 + 1]
 										.belongsTo(this.player))) {
 							this.arrows[pos1 + 1].setIcon(arrowRight);
 							counter++;
 						}
 						if ((pos1 % 7 != 0)
 								&& ((this.board[pos1 - 1] == null) || !this.board[pos1 - 1]
 										.belongsTo(this.player))) {
 							this.arrows[pos1 - 1].setIcon(arrowLeft);
 							counter++;
 						}
 						if ((pos1 <= 34)
 								&& ((this.board[pos1 + 7] == null) || !this.board[pos1 + 7]
 										.belongsTo(this.player))) {
 							this.arrows[pos1 + 7].setIcon(arrowDown);
 							counter++;
 						}
 						if ((pos1 >= 7)
 								&& ((this.board[pos1 - 7] == null) || !this.board[pos1 - 7]
 										.belongsTo(this.player))) {
 							this.arrows[pos1 - 7].setIcon(arrowUp);
 							counter++;
 						}
 					} catch (IndexOutOfBoundsException ioobe) {
 						ioobe.printStackTrace();
 					}
 					if (counter > 0) {
 						choosenPosition = pos1;
 						chosen = true;
 						this.arrows[pos1].setIcon(boarderIcon);
 					} else {
 						printLog("Keine Züge für dieses Feld.");
 						printLog("---");
 					}
 				} else {
 					printLog("Keine Züge für die " + this.board[pos1].getKind());
 					printLog("---");
 				}
 			} else {
 				printLog("Nicht möglich");
 				printLog("---");
 			}
 		}
 	}
 
 	/**
 	 * Creates an arrow on the position of the last move.
 	 */
 	public void lastMoveArrow() {
 		this.cleanArrows();
 		try {
 			if(this.game.getLastMove() != null){
 				Move last = this.game.getLastMove();
 				if (last.getTo() == last.getFrom() + 7)
 					this.arrows[last.getFrom()].setIcon(arrowDown);
 				if (last.getTo() == last.getFrom() - 7)
 					this.arrows[last.getFrom()].setIcon(arrowUp);
 				if (last.getTo() == last.getFrom() + 1)
 					this.arrows[last.getFrom()].setIcon(arrowRight);
 				if (last.getTo() == last.getFrom() - 1)
 					this.arrows[last.getFrom()].setIcon(arrowLeft);
 			}
 		} catch (RemoteException re) {
 			// RemoteException
 		}
 		this.redraw();
 	}
 
 	/**
 	 * Shuffles the line up, but lets the flag and trap on their positions.
 	 */
 	private void manualShuffle() {
 		createRandomLineup();
 		int flagBuffer = 0;
 		int trapBuffer = 1;
 		for (int i = 28; i < 42; i++) {
 			if (this.initialAssignment[i] == FigureKind.FLAG)
 				flagBuffer = i;
 			if (this.initialAssignment[i] == FigureKind.TRAP)
 				trapBuffer = i;
 		}
 		switchField(positionFlag, flagBuffer);
 		if (positionFlag != trapBuffer)
 			switchField(positionTrap, trapBuffer);
 		else
 			switchField(positionTrap, flagBuffer);
 	}
 
 	/**
 	 * Handles, whether something get changed in the line up.
 	 * 
 	 * @param pos1
 	 */
 	private void lineUpChange(int pos1) {
 
 		if ((this.initialAssignment[pos1] != null)) {
 			if (pick) {
 				if (this.initialAssignment[pos1] == FigureKind.FLAG)
 					positionFlag = pickedPosition;
 				if (this.initialAssignment[pickedPosition] == FigureKind.FLAG)
 					positionFlag = pos1;
 				if (this.initialAssignment[pos1] == FigureKind.TRAP)
 					positionTrap = pickedPosition;
 				if (this.initialAssignment[pickedPosition] == FigureKind.TRAP)
 					positionTrap = pos1;
 				switchField(pos1, pickedPosition);
 				pick = false;
 				this.arrows[pickedPosition].setIcon(emptyIcon);
 			} else {
 				pickedPosition = pos1;
 				pick = true;
 				this.arrows[pickedPosition].setIcon(boarderIcon);
 			}
 		} else {
 			printLog("Nicht deine Figur");
 			printLog("---");
 		}
 	}
 
 	/**
 	 * Exchanges two figures in the line up.
 	 * 
 	 * @param pos1
 	 * @param pos2
 	 */
 	private void switchField(int pos1, int pos2) {
 		FigureKind figureBuffer = this.initialAssignment[pos1];
 		this.initialAssignment[pos1] = this.initialAssignment[pos2];
 		this.initialAssignment[pos2] = figureBuffer;
 		redrawInitialAssignment();
 	}
 
 	/**
 	 * Binds the enter key with the function to send a message to the game
 	 * server.
 	 */
 	private void bindButtons() {
 		chatInput.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				boolean isEnter = e.getKeyCode() == KeyEvent.VK_ENTER;
 				if (isEnter) {
 					addToChat();
 				}
 			}
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 			}
 		});
 	}
 
 	/**
 	 * Sends the input of the chat to the game server.
 	 */
 	private void addToChat() {
 		String message = chatInput.getText().trim();
 		if (message.length() > 0) {
 			try {
 				game.sendMessage(player, message);
 				chatInput.setText("");
 			} catch (RemoteException e) {
 				// RemoteException
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Hides the pane for the main game.
 	 */
 	public void hide() {
 		gamePane.setVisible(false);
 	}
 
 	/**
 	 * Starts a new game.
 	 * 
 	 * @param player
 	 * @param game
 	 */
 	public void startGame(Player player, Game game) {
 		this.player = player;
 		this.game = game;
 		reset();
 		gamePane.setVisible(true);
 		this.askLineup();
 	}
 
 	/**
 	 * Prints a received message in the chat.
 	 * 
 	 * @param sender
 	 * @param message
 	 */
 	public void receivedMessage(Player sender, String message) {
 
 		if (chat.getText().length() != 0) {
 			chat.append("\n");
 		}
 		String formatted = sender.getNick() + ": " + message;
 		chat.append(formatted);
 		chat.setCaretPosition(chat.getDocument().getLength());
 	}
 
 	/**
 	 * Prints a message in the log.
 	 * 
 	 * @param message
 	 */
 	public void printLog(String message) {
 		log.append("> " + message + "\n");
 		log.setCaretPosition(log.getDocument().getLength());
 	}
 
 	/**
 	 * Resets the game for a new match
 	 */
 	private void reset() {
 		chat.setText(null);
 		for (int i = 0; i < 42; i++) {
 			this.discovered[i].setIcon(emptyIcon);
 			this.arrows[i].setIcon(emptyIcon);
 			this.figures[i].setIcon(emptyIcon);
 			initialAssignment[i] = null;
 		}
 		this.gamePhase = "initFlag";
 	}
 
 	/**
 	 * Redraws the board.
 	 */
 	public void redraw() {
 		this.loadPictures();
 		try {
 			board = this.game.getField();
 		} catch (RemoteException e) {
 			// RemoteException
 		}
 
 		for (int i = 0; i < 42; i++) {
 			if (this.board[i] != null && this.board[i].belongsTo(this.player)
 					&& !this.board[i].isDiscovered()) {
 				this.discovered[i].setIcon(discoveredIcon);
 			} else {
 				this.discovered[i].setIcon(emptyIcon);
 			}
 			if (i % 2 == 0) {
 				this.backgroundTiles[i].setIcon(this.iconWhite);
 			} else {
 				this.backgroundTiles[i].setIcon(this.iconBlack);
 			}
 		}
 
 		for (int i = 0; i < 42; i++) {
 
 			if (this.board[i] != null) {
 				if (this.board[i].belongsTo(this.player)) {
 					switch (this.board[i].getKind()) {
 					case TRAP:
 						this.figures[i].setIcon(blueTrap);
 						break;
 					case FLAG:
 						this.figures[i].setIcon(blueFlag);
 						break;
 					case ROCK:
 						this.figures[i].setIcon(blueRock);
 						break;
 					case PAPER:
 						this.figures[i].setIcon(bluePaper);
 						break;
 					case SCISSORS:
 						this.figures[i].setIcon(blueScissors);
 						break;
 					case HIDDEN:
 						this.figures[i].setIcon(unknownIcon);
 						break;
 					default:
 						this.figures[i].setIcon(unknownIcon);
 						break;
 					}
 				} else {
 					switch (this.board[i].getKind()) {
 					case TRAP:
 						this.figures[i].setIcon(redTrap);
 						break;
 					case FLAG:
 						this.figures[i].setIcon(redFlag);
 						break;
 					case ROCK:
 						this.figures[i].setIcon(redRock);
 						break;
 					case PAPER:
 						this.figures[i].setIcon(redPaper);
 						break;
 					case SCISSORS:
 						this.figures[i].setIcon(redScissors);
 						break;
 					case HIDDEN:
 						this.figures[i].setIcon(unknownIcon);
 						break;
 					default:
 						this.figures[i].setIcon(unknownIcon);
 						break;
 					}
 				}
 			} else {
 				this.figures[i].setIcon(emptyIcon);
 			}
 		}
 		
 		if (this.gamePhase != "gamePhase") {
 			this.redrawInitialAssignment();
 		}
 	}
 }
