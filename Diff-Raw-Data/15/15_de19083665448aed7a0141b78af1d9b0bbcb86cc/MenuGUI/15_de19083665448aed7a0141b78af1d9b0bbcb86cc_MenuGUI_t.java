 package myClasses;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.GraphicsEnvironment;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.table.DefaultTableCellRenderer;
 
 public class MenuGUI implements MenuInterface {
 
 	// Static
 	private static final String basics = "<html><center>BASICS</center><br>"
 			+ "THE NINE MEN'S MORRIS BOARD CONTAINS 24 INTERSECTIONS<br>"
 			+ "ARRANGED IN THREE CONSECUTIVE SQUARES. EACH PLAYER TAKES<br>"
 			+ "TURNS TRYING TO GET THEIR PIECES INTO A LINE OF THREE (OR<br>"
 			+ " MILL). WHEN A MILL HAS BEEN FORMED, THE PLAYER CAN TAKE<br>"
 			+ "AN OPPONENT'S PIECE FROM THE BOARD. A PLAYER WINS BY<br>"
 			+ "REDUCING THE NUMBER OF OPPONENT'S PIECES TO TWO.<br><br>"
 			+ "NINE MEN'S MORRIS CAN BE SPLIT INTO THREE PHASES<br>"
 			+ "<ul><li>PLACEMENT PHASE</li><li>MOEVEMENT PHASE</li><li>FLYING "
 			+ "MODE</li></ul><br>EACH SECTION WILL BE INTRODUCED IN THE NEXT FEW"
 			+ "SLIDES<br>";
 	private static final String placement = "<html><center>PLACEMENT PHASE</center><br>"
 			+ "THE GAME BEGINS WITH AN EMPTY BOARD AND A RANDOM FIRST<br>"
 			+ "PLAYER IS CHOSEN. PLAYERS TAKE TURNS PLACING THEIR PIECES<br>"
 			+ "ON THE BOARD UNTIL THEY HAVE NO MORE. MILLS CAN NOT BE<br>"
 			+ "TAKEN DURING THE PLACEMENT PHASE. MILLS CAN BE FORMED<br>"
 			+ "ONLY ONCE THE NEXT PHASE HAS STARTED.";
 	private static final String movement = "<html><center>MOVEMENT PHASE</center><br>"
 			+ "THE MOVEMENT PHASE BEGINS ONCE BOTH PLAYER HAVE ALL<br>"
 			+ "THEIR PIECES PLACED ON THE BOARD. PLAYERS TAKE TURNS<br>"
 			+ "MOVING THEIR PIECES AROND ON THE BOARD AND THE OBJECT<br>"
 			+ "OF THIS PHASE IS TO CREATE MILLS AND REMOVE THE<br>"
 			+ "OPPONENT'S PIECES. ONCE A MILL HAS BEEN FORMED, THE<br>"
 			+ "PLAYER CAN REMOVE ANY OF THE OPPOSING PLAYER'S PIECES<br>"
 			+ "EXCEPT ONE THAT IS IN A MILL. IF A PLAYER BECOMES<br>"
 			+ "TRAPPED, THEIR TURN IS SKIPPED UNTIL THEY CAN MOVE.";
 	private static final String flying = "<html><center>FLYING MODE</center><br>"
 			+ "THE FLYING MODE BEGINS AT EITHER 3 OR 4 (SPECIFIED<br>"
 			+ "IN THE MENU WHERE IT CAN BE TURNED OFF AS WELL).<br>"
 			+ "SINCE THE PLAYER THAT IS IN FLYING MODE HAS ONLY<br>"
 			+ "A FEW PIECES LEFT, THEY ARE ABLE TO MOVE THEIR PIECES<br>"
 			+ "TO ANY OPEN SPOT ON THE BOARD. THE POINT OF THIS IS<br>"
 			+ "SO THE LOSING PLAYER HAS A CHANCE TO GET BACK INTO<br>"
 			+ "THE GAME.";
 
 	// Variables
 	private Options myOptions;
 	private JFrame contentPane;
 	private boolean isGameReady;
 	private int slideNum;
 	private int flyMode;
 	private boolean timer;
 	private boolean resolution;
 	private boolean isLoaded;
 
 	// Custom Font
 	private Font coalition;
 
 	private Thread loadingThread;
 	private ArrayList<Component> components;
 
 	// Image Locations
 	// private String backL = "src/images/backgroundLarge.jpg";
 	private String backS = "images/backgroundSmall.jpg";
 	private ImageIcon off = new ImageIcon(getClass().getClassLoader()
 			.getResource("images/off.png"));
 	private ImageIcon on = new ImageIcon(getClass().getClassLoader()
 			.getResource("images/on.png"));
 
 	// Leaderboard
 	// private static String lbLoc = System.getProperty("user.home")
 	// + "/Library/Application Support/NineMensMorris/leaderboard.txt";
 	private String lbLoc = "files/leaderboard.txt";
 
 	public MenuGUI(JFrame contentPane, String leaderboardLocation) {
 		// Initialize variables
 		this.contentPane = contentPane;
 		this.myOptions = new Options();
 		lbLoc = leaderboardLocation;
 		isGameReady = false;
 		slideNum = 1;
 		flyMode = 1;
 		this.myOptions.setFlyRule(flyMode);
 		timer = false;
 		resolution = false;
 		components = new ArrayList<Component>();
 		isLoaded = false;
 		loadingThread = new Thread() {
 			public void run() {
 				drawMenu();
 				drawOptions();
 				for (int i = 0; i < components.size(); i++) {
 					while (!components.get(i).isShowing())
 						;
 				}
 			}
 		};
 
 		// Setup Custom Font
 		try {
 			coalition = Font.createFont(Font.PLAIN, getClass().getClassLoader()
 					.getResourceAsStream("font/Coalition_v2.ttf"));
 			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(
 					coalition);
 			// Font.createFont(Font.PLAIN, fontLoc);
 		} catch (FontFormatException e) {
 			e.printStackTrace();
 			System.out.println("Font Format not Accepted");
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("File not loaded");
 		}
 
 		// Begin Loading
 		loading();
 
 	}
 
 	public MenuGUI(JFrame contentPane, Options lastOptions, String leaderboardLocation) {
 		// Initialize variables
 		this.contentPane = contentPane;
 		this.myOptions = new Options();
 		lbLoc = leaderboardLocation;
 		isGameReady = false;
 		slideNum = 1;
 		flyMode = lastOptions.getFlyRule();
 		timer = lastOptions.getTimer();
 		resolution = lastOptions.getGameRes();
 		components = new ArrayList<Component>();
 		isLoaded = true;
 
 		// Setup Custom Font
 		try {
 			coalition = Font.createFont(Font.PLAIN, getClass().getClassLoader()
 					.getResourceAsStream("font/Coalition_v2.ttf"));
 			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(
 					coalition);
 		} catch (FontFormatException e) {
 			e.printStackTrace();
 			System.out.println("Font Format not Accepted");
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("File not loaded");
 		}
 
 		// Begin Loading
 		drawMenu();
 	}
 
 	public void loading() {
 		// Create Background JPanel & Add to LayeredPane on Layer 1
 		JPanel background = new JPanel();
 		background.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		background.setVisible(true);
 		background.setBackground(Color.black);
 		background.setName("Background");
 		contentPane.getContentPane().add(background);
 		contentPane.getContentPane().repaint();
 
 		JLabel loadingText = new JLabel("<html><center>LOADING..");
 		loadingText.setFont(coalition.deriveFont((float) 100));
 		loadingText.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		loadingText.setForeground(Color.gray);
 		loadingText.setVisible(true);
 		loadingText.setOpaque(false);
 		loadingText.setName("loadingText");
 		loadingText.setHorizontalAlignment(SwingConstants.CENTER);
 		loadingText.setVerticalAlignment(SwingConstants.CENTER);
 		background.add(loadingText);
 
 		loadingThread.start();
 		while (loadingThread.isAlive())
 			;
 
 		isLoaded = true;
 		contentPane.getContentPane().removeAll();
 		drawMenu();
 	}
 
 	public void drawMenu() {
 		// Initialize LayeredPane
 		final JLayeredPane layeredPane = new JLayeredPane();
 		layeredPane.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		layeredPane.setLayout(null);
 		if (!isLoaded)
 			components.add(layeredPane);
 		contentPane.getContentPane().add(layeredPane);
 
 		// Create Background JPanel & Add to LayeredPane on Layer 1
 		JPanel background;
 		try {
 			background = new JPanelWithBackground(backS);
 		} catch (IOException e) {
 			e.printStackTrace();
 			background = null;
 		}
 		background.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		background.setVisible(true);
 		if (!isLoaded)
 			components.add(background);
 		layeredPane.add(background);
 		layeredPane.setLayer(background, 1);
 
 		// Create Buttons JPanel & Add to LayerdPane on Layer 1
 		JPanel buttons = new JPanel();
 		buttons.setOpaque(false);
 		buttons.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		buttons.setVisible(true);
 		buttons.setLayout(null);
 		buttons.setName("Buttons");
 		if (!isLoaded)
 			components.add(buttons);
 		layeredPane.add(buttons);
 		layeredPane.setLayer(buttons, 2);
 
 		// Create Title
 		JLabel title = new JLabel("NINE MEN'S MORRIS");
 		title.setForeground(Color.LIGHT_GRAY);
 		title.setHorizontalAlignment(SwingConstants.CENTER);
 		title.setFont(coalition.deriveFont((float) 70));
 		title.setBounds(0, 86, contentPane.getContentPane().getWidth(), 127);
 		if (!isLoaded)
 			components.add(title);
 		buttons.add(title);
 
 		// Create Human vs. Computer Button
 		final JButton hvc = new JButton("HUMAN VS. COMPUTER");
 		hvc.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				hvc.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				hvc.setForeground(Color.WHITE);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Clear contentPane.getContentPane()
 				contentPane.getContentPane().remove(layeredPane);
 
 				// Update Options
 				myOptions.setComputerPlayer(true);
 
 				// Get playerNames
 				drawPlayerNames(1);
 			}
 		});
 		hvc.setFont(coalition.deriveFont((float) 40));
 		hvc.setForeground(Color.WHITE);
 		hvc.setBounds(0, title.getY() + 145, contentPane.getContentPane()
 				.getWidth(), 65);
 		hvc.setOpaque(false);
 		hvc.setContentAreaFilled(false);
 		hvc.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(hvc);
 		buttons.add(hvc);
 
 		// Create Human vs. Human Button
 		final JButton hvh = new JButton("HUMAN VS. HUMAN");
 		hvh.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				hvh.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				hvh.setForeground(Color.WHITE);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Clear contentPane.getContentPane()
 				contentPane.getContentPane().remove(layeredPane);
 
 				// Update Options
 				myOptions.setComputerPlayer(false);
 
 				// Get playerNames
 				drawPlayerNames(2);
 
 			}
 		});
 		hvh.setFont(coalition.deriveFont((float) 40));
 		hvh.setForeground(Color.WHITE);
 		hvh.setBounds(0, hvc.getY() + 70, contentPane.getContentPane()
 				.getWidth(), 65);
 		hvh.setOpaque(false);
 		hvh.setContentAreaFilled(false);
 		hvh.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(hvh);
 		buttons.add(hvh);
 
 		// Create Leaderboard Button
 		final JButton lb = new JButton("LEADERBOARD");
 		lb.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				lb.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				lb.setForeground(Color.WHITE);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Clear contentPane.getContentPane()
 				contentPane.getContentPane().remove(layeredPane);
 
 				// Draw the Leaderboards
 				drawLeaderboards();
 			}
 		});
 		lb.setFont(coalition.deriveFont((float) 40));
 		lb.setForeground(Color.WHITE);
 		lb.setBounds(0, hvh.getY() + 70, contentPane.getContentPane()
 				.getWidth(), 65);
 		lb.setOpaque(false);
 		lb.setContentAreaFilled(false);
 		lb.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(lb);
 		buttons.add(lb);
 
 		// Create Options Button
 		final JButton options = new JButton("OPTIONS");
 		options.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				options.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				options.setForeground(Color.WHITE);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Remove Current Screen
 				contentPane.getContentPane().remove(layeredPane);
 
 				// Open Options Menu
 				drawOptions();
 			}
 		});
 		options.setFont(coalition.deriveFont((float) 40));
 		options.setForeground(Color.WHITE);
 		options.setBounds(0, lb.getY() + 70, contentPane.getContentPane()
 				.getWidth(), 65);
 		options.setOpaque(false);
 		options.setContentAreaFilled(false);
 		options.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(options);
 		buttons.add(options);
 
 		// Create Exit Button
 		final JButton exit = new JButton("EXIT");
 		exit.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				exit.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				exit.setForeground(Color.WHITE);
 			}
 
 			@SuppressWarnings("static-access")
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				System.exit(contentPane.getContentPane().ABORT);
 			}
 		});
 		exit.setFont(coalition.deriveFont((float) 40));
 		exit.setForeground(Color.WHITE);
 		exit.setBounds(0, options.getY() + 70, contentPane.getContentPane()
 				.getWidth(), 65);
 		exit.setOpaque(false);
 		exit.setContentAreaFilled(false);
 		exit.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(exit);
 		buttons.add(exit);
 		
 		contentPane.getContentPane().repaint();
 	}
 
 	public void drawOptions() {
 		// Initialize LayeredPane
 		final JLayeredPane layeredPane = new JLayeredPane();
 		layeredPane.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		layeredPane.setLayout(null);
 		this.contentPane.getContentPane().add(layeredPane);
 
 		// Create Background JPanel & Add to LayeredPane on Layer 1
 		JPanel background;
 		try {
 			background = new JPanelWithBackground(backS);
 		} catch (IOException e) {
 			e.printStackTrace();
 			background = null;
 		}
 		background.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		background.setVisible(true);
 		background.setLayout(null);
 		if (!isLoaded)
 			components.add(background);
 		layeredPane.add(background);
 		layeredPane.setLayer(background, 1);
 
 		// Create Buttons JPanel & Add to LayerdPane on Layer 2
 		JPanel buttons = new JPanel();
 		buttons.setOpaque(false);
 		buttons.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		buttons.setVisible(true);
 		buttons.setLayout(null);
 		if (!isLoaded)
 			components.add(buttons);
 		layeredPane.add(buttons);
 		layeredPane.setLayer(buttons, 2);
 
 		// Create Title and add to Layer
 		JLabel title = new JLabel("OPTIONS");
 		title.setForeground(Color.LIGHT_GRAY);
 		title.setHorizontalAlignment(SwingConstants.CENTER);
 		title.setFont(coalition.deriveFont((float) 70));
 		title.setBounds(0, 86, contentPane.getContentPane().getWidth(), 127);
 		if (!isLoaded)
 			components.add(title);
 		buttons.add(title);
 
 		// Create How to Play button & add to Layer
 		final JButton howTo = new JButton("HOW TO PLAY");
 		howTo.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				howTo.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				howTo.setForeground(Color.WHITE);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Create howToPanel JPanel & Add to LayerdPane on Layer 3
 				final JPanel howToPanel = new JPanel();
 				howToPanel.setOpaque(false);
 				layeredPane.add(howToPanel);
 				layeredPane.setLayer(howToPanel, 3);
 				// Open How To Play
 				drawHowTo(howToPanel);
 
 			}
 		});
 		howTo.setFont(coalition.deriveFont((float) 40));
 		howTo.setForeground(Color.WHITE);
 		howTo.setBounds(0, title.getY() + 145, contentPane.getContentPane()
 				.getWidth(), 65);
 		howTo.setOpaque(false);
 		howTo.setContentAreaFilled(false);
 		howTo.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(howTo);
 		buttons.add(howTo);
 
 		// Create Fly Mode Title & add to Layer
 		JLabel fly = new JLabel("FLY MODE:      3      4      OFF");
 		fly.setForeground(Color.WHITE);
 		fly.setHorizontalAlignment(SwingConstants.CENTER);
 		fly.setFont(coalition.deriveFont((float) 40));
 		fly.setBounds(0, howTo.getY() + 70, contentPane.getContentPane()
 				.getWidth(), 65);
 		if (!isLoaded)
 			components.add(fly);
 		buttons.add(fly);
 
 		// Create Fly Mode 4 Radio button & add to Layer
 		final JButton fMThree = new JButton(off);
 		fMThree.setBounds(490, fly.getY() + 14, 30, 30);
 		fMThree.setVisible(true);
 		fMThree.setOpaque(false);
 		fMThree.setContentAreaFilled(false);
 		fMThree.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(fMThree);
 		buttons.add(fMThree);
 
 		// Create Fly Mode 3 Radio button & add to Layer
 		final JButton fMFour = new JButton(off);
 		fMFour.setBounds(640, fly.getY() + 14, 30, 30);
 		fMFour.setVisible(true);
 		fMFour.setOpaque(false);
 		fMFour.setContentAreaFilled(false);
 		fMFour.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(fMFour);
 		buttons.add(fMFour);
 
 		// Create Fly Mode Off Radio button & add to Layer
 		final JButton fMOff = new JButton(off);
 		fMOff.setBounds(790, fly.getY() + 14, 30, 30);
 		fMOff.setVisible(true);
 		fMOff.setOpaque(false);
 		fMOff.setContentAreaFilled(false);
 		fMOff.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(fMOff);
 		buttons.add(fMOff);
 
 		// If Fly Mode buttons are clicked...
 		fMThree.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Set all others to off
 				fMFour.setIcon(off);
 				fMOff.setIcon(off);
 				// Set to this one to on
 				fMThree.setIcon(on);
 				// Update global flyMode var
 				flyMode = 1;
 				myOptions.setFlyRule(1);
 			}
 		});
 
 		fMFour.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Set all others to off
 				fMThree.setIcon(off);
 				fMOff.setIcon(off);
 				// Set to this one to on
 				fMFour.setIcon(on);
 				// Update global flyMode var
 				flyMode = 2;
 				myOptions.setFlyRule(2);
 			}
 		});
 
 		fMOff.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Set all others to off
 				fMFour.setIcon(off);
 				fMThree.setIcon(off);
 				// Set to this one to on
 				fMOff.setIcon(on);
 				// Update global flyMode var
 				flyMode = 3;
 				myOptions.setFlyRule(3);
 			}
 		});
 
 		// Create Timer Title & add to Layer
 		JLabel timerTitle = new JLabel("TIMER:      ON      OFF");
 		timerTitle.setForeground(Color.WHITE);
 		timerTitle.setHorizontalAlignment(SwingConstants.CENTER);
 		timerTitle.setFont(coalition.deriveFont((float) 40));
 		timerTitle.setBounds(0, fly.getY() + 70, contentPane.getContentPane()
 				.getWidth(), 65);
 		if (!isLoaded)
 			components.add(timerTitle);
 		buttons.add(timerTitle);
 
 		// Create Timer On Radio button & add to Layer
 		final JButton tOn = new JButton(off);
 		tOn.setBounds(490, timerTitle.getY() + 14, 30, 30);
 		tOn.setVisible(true);
 		tOn.setOpaque(false);
 		tOn.setContentAreaFilled(false);
 		tOn.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(tOn);
 		buttons.add(tOn);
 
 		// Create Timer Off Radio button & add to Layer
 		final JButton tOff = new JButton(off);
 		tOff.setBounds(690, timerTitle.getY() + 14, 30, 30);
 		tOff.setVisible(true);
 		tOff.setOpaque(false);
 		tOff.setContentAreaFilled(false);
 		tOff.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(tOff);
 		buttons.add(tOff);
 
 		// If timer buttons are pressed...
 		tOn.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Set other to off
 				tOff.setIcon(off);
 				// Set to this one to on
 				tOn.setIcon(on);
 				// Update global timer var
 				timer = true;
 				myOptions.setTimer(true);
 			}
 		});
 
 		tOff.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Set all others to off
 				tOn.setIcon(off);
 				// Set to this one to on
 				tOff.setIcon(on);
 				// Update global timer var
 				timer = false;
 				myOptions.setTimer(false);
 			}
 		});
 
 		// Create Resolution Title & add to Layer
 		JLabel res = new JLabel("GAME RESOLUTION:      HIGH      LOW");
 		res.setForeground(Color.WHITE);
 		res.setHorizontalAlignment(SwingConstants.CENTER);
 		res.setFont(coalition.deriveFont((float) 40));
 		res.setBounds(0, timerTitle.getY() + 70, contentPane.getContentPane()
 				.getWidth(), 65);
 		if (!isLoaded)
 			components.add(res);
 		buttons.add(res);
 
 		// Create Timer On Radio button & add to Layer
 		final JButton rOn = new JButton(off);
 		rOn.setBounds(620, res.getY() + 14, 30, 30);
 		rOn.setVisible(true);
 		rOn.setOpaque(false);
 		rOn.setContentAreaFilled(false);
 		rOn.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(rOn);
 		buttons.add(rOn);
 
 		// Create Timer Off Radio button & add to Layer
 		final JButton rOff = new JButton(off);
 		rOff.setBounds(865, res.getY() + 14, 30, 30);
 		rOff.setVisible(true);
 		rOff.setOpaque(false);
 		rOff.setContentAreaFilled(false);
 		rOff.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(rOff);
 		buttons.add(rOff);
 
 		// If timer buttons are pressed...
 		rOn.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Set other to off
 				rOff.setIcon(off);
 				// Set to this one to on
 				rOn.setIcon(on);
 				// Update global timer var
 				resolution = true;
 				myOptions.setGameRes(true);
 			}
 		});
 
 		rOff.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Set all others to off
 				rOn.setIcon(off);
 				// Set to this one to on
 				rOff.setIcon(on);
 				// Update global timer var
 				resolution = false;
 				myOptions.setGameRes(false);
 			}
 		});
 
 		// Create Main Menu button & add to Layer
 		final JButton main = new JButton("MAIN MENU");
 		main.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				main.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				main.setForeground(Color.WHITE);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Remove Current Screen
 				contentPane.getContentPane().remove(layeredPane);
 
 				// Open Main Menu
 				drawMenu();
 			}
 		});
 		main.setFont(coalition.deriveFont((float) 40));
 		main.setForeground(Color.WHITE);
 		main.setBounds(0, res.getY() + 70, contentPane.getContentPane()
 				.getWidth(), 65);
 		main.setOpaque(false);
 		main.setContentAreaFilled(false);
 		main.setBorderPainted(false);
 		if (!isLoaded)
 			components.add(main);
 		buttons.add(main);
 
 		// Set Default Settings for Options Menu
 		if (timer) {
 			tOn.setIcon(on);
 		} else {
 			tOff.setIcon(on);
 		}
 		if (flyMode == 1) {
 			fMThree.setIcon(on);
 		} else if (flyMode == 2) {
 			fMFour.setIcon(on);
 		} else {
 			fMOff.setIcon(on);
 		}
 		if (resolution) {
 			rOn.setIcon(on);
 		} else {
 			rOff.setIcon(on);
 		}
 		
 		contentPane.getContentPane().repaint();
 	}
 
 	public void drawHowTo(final JPanel panel) {
 		slideNum = 1;
 
 		JPanel[] slides = new JPanel[4];
 
 		// Initialize LayeredPane
 		final JLayeredPane layeredPane = new JLayeredPane();
 		layeredPane.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		layeredPane.setLayout(null);
 		panel.setOpaque(true);
 		panel.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		panel.setLayout(null);
 		panel.setVisible(true);
 		panel.add(layeredPane);
 
 		// Create Background JPanel & Add to LayeredPane on Layer 1
 		JPanel background;
 		try {
 			background = new JPanelWithBackground(backS);
 		} catch (IOException e) {
 			e.printStackTrace();
 			background = null;
 		}
 		background.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		background.setVisible(true);
 		layeredPane.add(background);
 		layeredPane.setLayer(background, 1);
 
 		// Create Glass Panel and Add to LayeredPane on Layer 2
 		final JPanel glass = new JPanel();
 		glass.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		glass.setOpaque(false);
 		glass.setVisible(true);
 		glass.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Do Nothing
 			}
 		});
 		layeredPane.add(glass);
 		layeredPane.setLayer(glass, 2);
 
 		// Create Buttons JPanel and Add to LayeredPane on Layer 3
 		JPanel buttons = new JPanel();
 		buttons.setLayout(null);
 		buttons.setOpaque(false);
 		buttons.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		buttons.setVisible(true);
 		layeredPane.add(buttons);
 		layeredPane.setLayer(buttons, 3);
 
 		// Create Title and add to Layer
 		JLabel title = new JLabel("HOW TO PLAY");
 		title.setForeground(Color.LIGHT_GRAY);
 		title.setHorizontalAlignment(SwingConstants.CENTER);
 		title.setFont(coalition.deriveFont((float) 70));
 		title.setBounds(0, 60, contentPane.getContentPane().getWidth(), 125);
 		buttons.add(title);
 
 		// Add previous button to JPanel
 		final JButton previous = new JButton("PREVIOUS");
 		previous.setFont(coalition.deriveFont((float) 40));
 		previous.setForeground(Color.WHITE);
 		previous.setBounds(20, contentPane.getContentPane().getHeight() - 100,
 				325, 65);
 		previous.setOpaque(false);
 		previous.setContentAreaFilled(false);
 		previous.setBorderPainted(false);
 		buttons.add(previous);
 
 		// Add back button to JPanel
 		final JButton back = new JButton("BACK");
 		back.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				back.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				back.setForeground(Color.WHITE);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Delete Layer
 				panel.setVisible(false);
 			}
 		});
 		back.setFont(coalition.deriveFont((float) 40));
 		back.setForeground(Color.WHITE);
 		back.setBounds((contentPane.getContentPane().getWidth() / 2) - 100,
 				contentPane.getContentPane().getHeight() - 100, 200, 65);
 		back.setOpaque(false);
 		back.setContentAreaFilled(false);
 		back.setBorderPainted(false);
 		buttons.add(back);
 
 		// Add next button to JPanel
 		final JButton next = new JButton("NEXT");
 		next.setFont(coalition.deriveFont((float) 40));
 		next.setForeground(Color.WHITE);
 		next.setBounds(contentPane.getContentPane().getWidth() - 220,
 				contentPane.getContentPane().getHeight() - 100, 200, 65);
 		next.setContentAreaFilled(false);
 		next.setBorderPainted(false);
 		buttons.add(next);
 
 		// Create cardPanel and Add to Layered Pane on Layer 3
 		final JPanel cardPanel = new JPanel();
 		cardPanel.setVisible(true);
 		cardPanel.setOpaque(false);
 		cardPanel.setBounds(60, 200, 1000, 375);
 		final CardLayout cl = new CardLayout(10, 10);
 		cardPanel.setLayout(cl);
 		layeredPane.add(cardPanel);
 		layeredPane.setLayer(cardPanel, 3);
 
 		for (int i = 0; i < slides.length; i++) {
 			slides[i] = new JPanel();
 			slides[i].setVisible(true);
 			slides[i].setOpaque(false);
 			slides[i].setBounds(25, 25, cardPanel.getWidth() - 50,
 					cardPanel.getHeight() - 50);
 			cardPanel.add(slides[i], "" + (i + 1));
 		}
 
 		JLabel basicsText = new JLabel(basics);
 		basicsText.setHorizontalAlignment(SwingConstants.LEFT);
 		basicsText.setFont(coalition.deriveFont((float) 19));
 		basicsText.setBounds(0, 0, slides[0].getWidth(), slides[0].getHeight());
 		basicsText.setForeground(Color.WHITE);
 		slides[0].add(basicsText);
 
 		JLabel placeText = new JLabel(placement);
 		placeText.setHorizontalAlignment(SwingConstants.LEFT);
 		placeText.setBounds(0, 0, slides[0].getWidth(), slides[0].getHeight());
 		placeText.setFont(coalition.deriveFont((float) 19));
 		placeText.setForeground(Color.WHITE);
 		slides[1].add(placeText);
 
 		JLabel moveText = new JLabel(movement);
 		moveText.setHorizontalAlignment(SwingConstants.LEFT);
 		moveText.setBounds(0, 0, slides[0].getWidth(), slides[0].getHeight());
 		moveText.setFont(coalition.deriveFont((float) 19));
 		moveText.setForeground(Color.WHITE);
 		slides[2].add(moveText);
 
 		JLabel flyText = new JLabel(flying);
 		flyText.setHorizontalAlignment(SwingConstants.LEFT);
 		flyText.setBounds(0, 0, slides[0].getWidth(), slides[0].getHeight());
 		flyText.setFont(coalition.deriveFont((float) 19));
 		flyText.setForeground(Color.WHITE);
 		slides[3].add(flyText);
 
 		// Show first slide on start
 		cl.show(cardPanel, "" + slideNum);
 
 		// Add Next and Previous Button Listeners
 		next.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				next.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				next.setForeground(Color.WHITE);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				if (slideNum < 4) {
 					slideNum++;
 					cl.show(cardPanel, "" + slideNum);
 				}
 			}
 		});
 		previous.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				previous.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				previous.setForeground(Color.WHITE);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				if (slideNum > 1) {
 					slideNum--;
 					cl.show(cardPanel, "" + slideNum);
 				}
 			}
 		});
 
 		contentPane.getContentPane().repaint();
 	}
 
 	public void drawLeaderboards() {
 		// Initialize LayeredPane
 		final JLayeredPane layeredPane = new JLayeredPane();
 		layeredPane.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		layeredPane.setLayout(null);
 		this.contentPane.getContentPane().add(layeredPane);
 
 		// Create Background JPanel & Add to LayeredPane on Layer 1
 		JPanel background;
 		try {
 			background = new JPanelWithBackground(backS);
 		} catch (IOException e) {
 			e.printStackTrace();
 			background = null;
 		}
 		background.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		background.setVisible(true);
 		layeredPane.add(background);
 		layeredPane.setLayer(background, 1);
 
 		// Create Buttons JPanel and Add to LayeredPane on Layer 2
 		JPanel buttons = new JPanel();
 		buttons.setLayout(null);
 		buttons.setOpaque(false);
 		buttons.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		buttons.setVisible(true);
 		layeredPane.add(buttons);
 		layeredPane.setLayer(buttons, 2);
 
 		// Create Title and add to Layer
 		JLabel title = new JLabel("LEADERBOARDS");
 		title.setForeground(Color.LIGHT_GRAY);
 		title.setHorizontalAlignment(SwingConstants.CENTER);
 		title.setFont(coalition.deriveFont((float) 70));
 		title.setBounds(0, 60, contentPane.getContentPane().getWidth(), 125);
 		buttons.add(title);
 
 		// Add back button to JPanel
 		final JButton back = new JButton("BACK");
 		back.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				back.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				back.setForeground(Color.WHITE);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				// Remove Current Screen
 				contentPane.getContentPane().remove(layeredPane);
 
 				// Open Main Menu
 				drawMenu();
 			}
 		});
 		back.setFont(coalition.deriveFont((float) 40));
 		back.setForeground(Color.WHITE);
 		back.setBounds((contentPane.getContentPane().getWidth() / 2) - 100,
 				contentPane.getContentPane().getHeight() - 100, 200, 65);
 		back.setOpaque(false);
 		back.setContentAreaFilled(false);
 		back.setBorderPainted(false);
 		buttons.add(back);
 
 		// Read Leaderboards File
 		FileReader fr;
 		BufferedReader br;
 		int lbLength;
 		String[][] data;
 
 		try {
 			fr = new FileReader(lbLoc);
 		} catch (FileNotFoundException e) {
 			fr = null;
 			System.out.println("File Not Read.... Incorrect File Name" + lbLoc);
 		}
 		br = new BufferedReader(fr);
 		try {
 			lbLength = Integer.parseInt(br.readLine());
 		} catch (NumberFormatException e) {
 			System.out.println("Number Format Incorrect in MenuGui");
 			lbLength = 0;
 		} catch (IOException e) {
 			System.out.println("Error with Number of Entries");
 			lbLength = 0;
 		}
 
 		// Run through the entries in Text File & Check against Winner Num Turns
 		data = new String[lbLength][2];
 		for (int i = 0; i < lbLength; i++) {
 			try {
 				data[i] = br.readLine().split(",");
 				data[i][0].toUpperCase();
 				data[i][1].toUpperCase();
 			} catch (IOException e) {
 				System.out.println("Could not read line Number: " + (i + 1));
 				data[i] = null;
 			}
 		}
 		try {
 			fr.close();
 			br.close();
 		} catch (IOException e) {
 			System.out.println("Could not Close File from Reading");
 		}
 
 		// Draw JPanel board to display names
 		String columnNames[] = { "NAME", "TURN NUMBER" };
 		JTable table = new JTable(data, columnNames);
 		table.setForeground(Color.white);
 		table.setVisible(true);
 		table.setOpaque(false);
 		((DefaultTableCellRenderer) table.getDefaultRenderer(Object.class))
 				.setOpaque(false);
 		table.setRowHeight(35);
 		table.setFont(coalition.deriveFont((float) 30));
 		table.setBounds((contentPane.getContentPane().getWidth() / 2) - 400,
 				165, 800, 425);
 		table.getColumn("NAME").setWidth(table.getWidth() / 2);
 		table.getColumn("TURN NUMBER").setWidth(table.getWidth() / 2);
 		table.setShowGrid(false);
 		table.getTableHeader().setFont(coalition.deriveFont((float) 35));
 		table.getTableHeader().setForeground(Color.GRAY);
 		table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
 		table.getTableHeader().setBackground(Color.BLACK);
 		JScrollPane scroll = new JScrollPane(table,
 				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
 		scroll.setOpaque(false);
 		scroll.getViewport().setOpaque(false);
 		scroll.setWheelScrollingEnabled(true);
 		scroll.setBounds(contentPane.getContentPane().getWidth() / 2 - 400,
 				165, 800, 425);
 		scroll.setBorder(BorderFactory.createEmptyBorder());
 		buttons.add(scroll);
 
 		// Repaint
 		contentPane.getContentPane().repaint();
 
 	}
 
 	private void drawPlayerNames(final int numHumans) {
 		// Initialize LayeredPane
 		final JLayeredPane layeredPane = new JLayeredPane();
 		layeredPane.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		layeredPane.setLayout(null);
 		contentPane.getContentPane().add(layeredPane);
 
 		// Create Background JPanel & Add to LayeredPane on Layer 1
 		JPanel background;
 		try {
 			background = new JPanelWithBackground(backS);
 		} catch (IOException e) {
 			e.printStackTrace();
 			background = null;
 		}
 		background.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		background.setVisible(true);
 		layeredPane.add(background);
 		layeredPane.setLayer(background, 1);
 
 		// Create info JPanel & Add to LayerdPane on Layer 2
 		JPanel info = new JPanel();
 		info.setOpaque(false);
 		info.setBounds(0, 0, contentPane.getContentPane().getWidth(),
 				contentPane.getContentPane().getHeight());
 		info.setVisible(true);
 		info.setLayout(null);
 		layeredPane.add(info);
 		layeredPane.setLayer(info, 2);
 
 		JLabel titleOne = new JLabel("<html><center>PLAYER 1<br>NAME</center>");
 		titleOne.setHorizontalAlignment(SwingConstants.CENTER);
 		titleOne.setVisible(true);
 		titleOne.setBounds(0, 100, info.getWidth() / numHumans, 200);
 		titleOne.setFont(coalition.deriveFont((float) 50));
 		titleOne.setForeground(Color.white);
 		info.add(titleOne);
 
 		final JTextField nameOne = new JTextField();
 		nameOne.setBounds(100, titleOne.getY() + 250,
 				(info.getWidth() / numHumans) - 200, 50);
 		nameOne.setFont(coalition.deriveFont((float) 30));
 		nameOne.setVisible(true);
 		nameOne.setHorizontalAlignment(SwingConstants.CENTER);
		nameOne.setText("");
 		info.add(nameOne);
 
 		final JTextField nameTwo;
 		if (numHumans == 2) {
 			JLabel titleTwo = new JLabel(
 					"<html><center>PLAYER 2<br>NAME</center>");
 			titleTwo.setHorizontalAlignment(SwingConstants.CENTER);
 			titleTwo.setVisible(true);
 			titleTwo.setBounds((info.getWidth() / 2), 100,
 					(info.getWidth() / numHumans), 200);
 			titleTwo.setFont(coalition.deriveFont((float) 50));
 			titleTwo.setForeground(Color.white);
 			info.add(titleTwo);
 
 			nameTwo = new JTextField();
 			nameTwo.setBounds((info.getWidth() / 2) + 100,
 					titleTwo.getY() + 250, (info.getWidth() / numHumans) - 200,
 					50);
 			nameTwo.setFont(coalition.deriveFont((float) 30));
 			nameTwo.setVisible(true);
 			nameTwo.setHorizontalAlignment(SwingConstants.CENTER);
			nameTwo.setText("");
 			info.add(nameTwo);
 		} else {
 			nameTwo = null;
 		}
 
 		final JButton ready = new JButton("Ready");
 		ready.setContentAreaFilled(false);
 		ready.setBorderPainted(false);
 		ready.setVisible(true);
 		ready.setBounds(0, info.getHeight() - 150, info.getWidth(), 50);
 		ready.setFont(coalition.deriveFont((float) 40));
 		ready.setForeground(Color.white);
 		ready.setOpaque(false);
 		info.add(ready);
 		ready.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				ready.setForeground(Color.LIGHT_GRAY);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				ready.setForeground(Color.WHITE);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (numHumans == 1) {
 					if (nameOne.getText().length() != 0) {
 						// Some names for the computer player just for fun
 						Random randomNum = new Random();
 						String[] pcNames = { "Bill", "Steve", "Berners-Lee",
 								"Turing", "von Neumann", "Wozniak" };
 
 						// Set Names
 						myOptions.setPlayerNames(nameOne.getText(),
 								pcNames[randomNum.nextInt(6)]);
 
 						// Remove Layered Pane
 						contentPane.getContentPane().remove(layeredPane);
						
						// Set isGameReady
						isGameReady = true;
 					}
 				} else {
 					if (nameOne.getText().length() != 0
 							&& nameTwo.getText().length() != 0) {
 						// Set Name
 						myOptions.setPlayerNames(nameOne.getText(),
 								nameTwo.getText());
 
 						// Remove Layered Pane
 						contentPane.getContentPane().remove(layeredPane);
						
						// Set isGameReady
						isGameReady = true;
 					}
 				}
 
				
 			}
 		});
 
 		info.repaint();
 
 	}
 
 	@Override
 	public Options getOptions() {
 		return myOptions;
 	}
 
 	@Override
 	public void setOptions(Options myOptions) {
 		this.myOptions = myOptions;
 	}
 
 	@Override
 	public boolean isGameReady() {
 		return isGameReady;
 	}
 
 }
