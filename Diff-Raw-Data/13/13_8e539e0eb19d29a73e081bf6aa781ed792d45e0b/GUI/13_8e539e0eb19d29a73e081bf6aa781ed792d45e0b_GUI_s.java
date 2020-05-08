 package pieces.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.io.IOException;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import pieces.Game;
 import pieces.gui.menu.Menu;
 import pieces.gui.menu.MenuBar;
 import pieces.gui.utils.Bank;
 import pieces.gui.utils.Themes;
 import pieces.gui.utils.Bank.SequenceNotFoundException;
 import pieces.utils.Config;
 import pieces.utils.Matrix.MatrixPiece;
 
 /**
  * GUI class.
  * 
  * @author Cyriaque Skrapits
  * 
  */
 public class GUI extends JFrame {
 	private static final long serialVersionUID = 1L;
 	
 	private Game game;
 	public Themes theme;
 	private Bank bank = null;
 
 	private MenuBar menu;
 	private Grid grid;
 	private Message message;
 	private Scores scores;
 	private Options options;
 	private Multiplayer multiplayer;
 	private Board board;
 
 	private Menu menuNewGame;
 	private Menu menuMultiplayer;
 	private Menu menuOptions;
 	private Menu menuAbout;
 
 	public GUI(Game game) {
 
 		this.game = game;
 
 		// Load config.
 		Config.load();
 
 		// Load theme.
 		Themes.load();
 		Themes.setCurrentTheme(Config.get(Config.THEME, "Pieces"));
 		try {
 			bank = new Bank("themes/" + Themes.getCurrentTheme().getName() + "/sequence.png", 10);
 		} catch (SequenceNotFoundException snfe) {
 			String message = "Cannot load current theme : " + Themes.getCurrentTheme().getName();
 			System.err.println(message);
 			JOptionPane.showMessageDialog(this, message, Game.APP_NAME, JOptionPane.ERROR_MESSAGE, null);
 			return;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 		setBackground(Themes.getCurrentTheme().getGridBackground());
 
 		// Components initialization.
 		menu = new MenuBar();
 		message = new Message();
 		grid = new Grid(game, bank);
 		scores = new Scores(game, bank);
 		options = new Options(game, this);
 		multiplayer = new Multiplayer(game);
 		board = new Board(grid, message, options, multiplayer);
 
 		// Ugly layout hack.
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout());
 		panel.add(multiplayer, BorderLayout.NORTH);
 		panel.add(board, BorderLayout.CENTER);
 
 		createMenus();
 
 		// Display.
 		setLayout(new BorderLayout());
 		add(menu, BorderLayout.NORTH);
 		add(panel, BorderLayout.CENTER);
 		add(scores, BorderLayout.SOUTH);
 		add(options, BorderLayout.EAST);
 
 		// NEEDED FOR WINDOWS ?
 		revalidate();
 		repaint();
 
 		ImageIcon icon = new ImageIcon("icon.png");
 		setIconImage(icon.getImage());
 		setTitle(Game.APP_NAME);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setSize(600, 600);
 		setVisible(true);
 	}
 
 	public void about() {
 		StringBuilder message = new StringBuilder();
 		message.append("<html>");
 		message.append("<h1>" + Game.APP_NAME + "</h1>");
 		message.append("Version " + Game.APP_VERSION + "<br/>");
 		message.append("<br/>");
 		message.append("<p>Authors : ");
 		message.append("<br/>");
 		message.append("Guillaume Simon-Gentil (AI)<br/>");
 		message.append("Cyriaque Skrapits (core developer)");
 		message.append("</p></html>");
 		Icon icone = new Icon() {
 
 			@Override
 			public void paintIcon(Component c, Graphics g, int x, int y) {
 				g.drawImage(bank.getImage(0), 0, 0, 60, 60, null);
 			}
 
 			@Override
 			public int getIconWidth() {
 				return 60;
 			}
 
 			@Override
 			public int getIconHeight() {
 				return 60;
 			}
 		};
 		JOptionPane.showMessageDialog(this, message.toString(), "About " + Game.APP_NAME + "...", JOptionPane.INFORMATION_MESSAGE, icone);
 	}
 
 	public void changeRound() {
 		// Shows which player is allowed to play.
 		String m;
 		m = "It's time for the " + (game.round() ? "whites" : "blacks") + " to play.";
 		System.out.println(m);
 		message.message(m, Message.NORMAL, false, true);
 	}
 
 	private void createMenus() {
 		menuNewGame = new Menu("New game") {
 			@Override
 			public void action() {
 				super.action();
 				game.newGame();
 			}
 		};
 
 		menuMultiplayer = new Menu("Multiplayer") {
 			@Override
 			public void action() {
 				super.action();
 				if (game.isMultiplayer())
 					game.logout();
 				else
 					multiplayer.toggle();
 				multiplayer();
 			}
 		};
 
 		menuOptions = new Menu("Options") {
 			@Override
 			public void action() {
 				super.action();
 				options.toggle();
 			}
 		};
 
 		menuAbout = new Menu("?") {
 			@Override
 			public void action() {
 				super.action();
 				about();
 			}
 		};
 
 		menu.addMenu(menuNewGame);
 		menu.addMenu(menuMultiplayer);
 		menu.addMenu(menuOptions);
 		menu.addMenu(menuAbout);
 	}
 
 	public void endingAnimation() {
 		if (!Config.get(Config.ENDING_ANIMATION, true))
 			return;
 
 		Thread animation = new Thread(new Runnable() {
 			int pieces = game.getMatrix().pieces();
 			int noirs = game.getMatrix().score(MatrixPiece.BLACK);
 
 			@Override
 			public void run() {
 				for (int i = 0; i < 64; i++) {
 					Piece p = grid.getPiece(i);
 					if (i < pieces) {
 						if (!p.isVisible())
 							p.setVisible(true);
 						if (p.isReversed() == noirs > 0)
 							p.reverse();
 						noirs--;
 						try {
 							Thread.sleep(20);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						repaint();
 						revalidate();
 					} else {
 						p.setVisible(false);
 					}
 				}
 			}
 		});
 		animation.start();
 	}
 
 	public Bank getBank() {
 		return this.bank;
 	}
 
 	public Grid getGrid() {
 		return this.grid;
 	}
 
 	public Scores getScores() {
 		return this.scores;
 	}
 
 	public void message(String texte, Color couleur, boolean persistant, boolean urgent) {
 		message.message(texte, couleur, persistant, urgent);
 		revalidate();
 		repaint();
 	}
 
 	public void multiplayer() {
 		menuMultiplayer.setText(game.isMultiplayer() ? "Disconnect" : "Multiplayer");
 	}
 
 	public void newGame() {
 		grid.initialize();
 		message.toggle();
 		scores.update();
 	}
 
 	public void play(int piece) {
 		grid.put(piece, game.round());
 	}
 
 	public void redraw() {
 		multiplayer.redraw();
 		setBackground(Themes.getCurrentTheme().getGridBackground());
 		repaint();
 	}
 }
