 package blueMaggot;
 
 import entity.Tank;
 import gfx.GBC;
 import gfx.GBC.Align;
 import gfx.MenuAbout;
 import gfx.MenuBackground;
 import gfx.MenuField;
 import gfx.MenuKeys;
 import gfx.MenuLevelSelect;
 import gfx.MenuOptions;
 import gfx.MenuOptionsLan;
 import gfx.MenuScoreBoard;
 import gfx.MenuTitle;
 import inputhandler.InputHandler;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 /**
  * @author Habitats * this motherfucker starts the game
  */
 public class BlueMaggot extends JFrame implements GameListener {
 
 	public InputHandler inputReal = new InputHandler();
 
 	private JLayeredPane layeredPane = new JLayeredPane();
 	private JPanel gamePanel;
 
 	public MenuScoreBoard menuScore;
 	public MenuLevelSelect menuLevelSelect;
 	public MenuOptions menuOptions;
 	public MenuOptionsLan menuOptionsLan;
 	public MenuAbout menuAbout;
 	public MenuTitle menuTitle;
 	public MenuKeys menuKeys;
 	public static Exception e;
 
 	Game game;
 
 	private MenuBackground menuBackground;
 
 	public BlueMaggot() {
 		GameState.getInstance().init();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		setPreferredSize(new Dimension(GameState.getInstance().getWidth(), GameState.getInstance().getHeight() + 28));
 		setFocusable(true);
 		setResizable(false);
 
 		layeredPane.setBounds(0, 0, GameState.getInstance().getWidth(), GameState.getInstance().getHeight());
 		layeredPane.setOpaque(false);
 		game = new blueMaggot.Game(this);
 		menuTitle = new MenuTitle(game, this);
 		menuOptions = new MenuOptions(game);
 		menuOptionsLan = new MenuOptionsLan(game);
 		menuAbout = new MenuAbout(game);
 		menuScore = new MenuScoreBoard(game);
 		menuLevelSelect = new MenuLevelSelect(game);
 		menuBackground = new MenuBackground(menuTitle);
 		menuKeys = new MenuKeys(game);
 		gamePanel = new JPanel();
 		gamePanel.setBackground(Color.DARK_GRAY);
 
 		layeredPane.add(gamePanel, new Integer(0));
 		layeredPane.add(menuBackground, new Integer(9));
 		layeredPane.add(menuTitle, new Integer(10));
 		layeredPane.add(menuOptions, new Integer(11));
 		layeredPane.add(menuOptionsLan, new Integer(11));
 		layeredPane.add(menuLevelSelect, new Integer(11));
 		layeredPane.add(menuAbout, new Integer(11));
 		layeredPane.add(menuKeys, new Integer(11));
 		layeredPane.add(menuScore, new Integer(12));
 
		inputReal.resetLan();
 		try {
 			inputReal.readConfig();
 			System.out.println("reading keybinds from config");
 		} catch (Exception e) {
 			System.out.println("no config found, setting defaults");
 		}
 		for (MenuField menuField : MenuField.menuFields) {
 			menuField.reset();
 		}
 
 		add(layeredPane);
 		pack();
 		setLocationRelativeTo(null);
 		setVisible(true);
 		repaint();
 
 	}
 
 	private void setUpGame() {
 		game.setPreferredSize(GameState.getInstance().dimension);
 		gamePanel.setLayout(new BorderLayout());
 		gamePanel.setBounds(0, 0, GameState.getInstance().getWidth(), GameState.getInstance().getHeight());
 
 		gamePanel.add(game);
 	}
 
 	public static void main(String[] args) {
 		try {
 			(new BlueMaggot()).setUpGame();
 		} catch (Exception exception) {
 			e = exception;
 		} finally {
 			if (e != null) {
 				JFrame warning = new JFrame();
 				JTextArea content = new JTextArea();
 				warning.setLayout(new GridBagLayout());
 				content.append("FATAL MALVISIOUS ERROR!!11\n");
 				content.append("(╯°□°）╯︵ ┻━┻\n");
 				content.append("Protip:\nMake sure your \"lvl\" directory is in the same folder as your blueMaggot.jar file!\n\n");
 				content.append("Error:\n " + e.toString() + "\n\n");
 				content.append("StackTrace:\n");
 				for (StackTraceElement stack : e.getStackTrace()) {
 					content.append(stack.toString() + "\n");
 				}
 				warning.setTitle("ERROR");
 				e.printStackTrace();
 				JButton exit = new JButton("exit");
 				exit.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						System.exit(1);
 					}
 				});
 				warning.add(content, new GBC(0, 0, Align.MID));
 				warning.add(exit, new GBC(0, 1, Align.MID));
 				warning.pack();
 				warning.setVisible(true);
 				warning.setAlwaysOnTop(true);
 				warning.setLocationRelativeTo(null);
 
 			}
 		}
 	}
 
 	public void tick(Graphics2D g) {
 		for (Tank tank : GameState.getInstance().getPlayers()) {
 			if (tank.getNick() == null)
 				tank.setNick("Player");
 		}
 
 		if (inputReal.menu.clicked) {
 			inputReal.menu.clicked = false;
 			inputReal.releaseAll();
 			if (!menuTitle.isVisible()) {
 				menuTitle.setVisible(true);
 				menuBackground.setVisible(true);
 				GameState.getInstance().setPaused(true);
 			}
 		}
 
 		if (GameState.getInstance().isGameOver()) {
 			menuTitle.setVisible(true);
 			menuScore.setVisible(true);
 			menuScore.repaint();
 			GameState.getInstance().setRunning(false);
 			GameState.getInstance().setPaused(true);
 			GameState.getInstance().setGameOver(false);
 			menuBackground.setVisible(true);
 
 		}
 	}
 
 	@Override
 	public void ConnectionFailed(String msg) {
 		GameState.getInstance().setPaused(true);
 		menuTitle.setVisible(true);
 		System.out.println("game crashed, retreat!");
 	}
 }
