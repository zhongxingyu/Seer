 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.swing.*;
 
 /**
  * JFrame to display menu options to the user. Used to select game settings then
  * initiate a new game.
  * 
  * @author sturgedl. Created Apr 17, 2013.
  */
 @SuppressWarnings("serial")
 public class MenuFrame extends JFrame {
 	private static final int FRAME_WIDTH = 600;
 	private static final int FRAME_HEIGHT = 800;
 
 	private static final int TITLE_WIDTH = 1000;
 	private static final int TITLE_HEIGHT = 100;
 
 	private JPanel buttonPanel;
 	private ArrayList<JButton> buttons;
 
 	private String language;
 	private String[] labels;
 
 	private JTextField p1;
 	private JTextField p2;
 	private JTextField p3;
 	private JTextField p4;
 
 	private JTextField tfHostIP;
 	private JTextField tfUsername;
 	private JTextField tfPort;
 	private ArrayList<String> player;
 	private int port;
 	private String IP;
 
 	public MenuFrame(String lang) {
 		super("Sorry!");
 		this.language = lang;
 		this.labels = MenuFrame.obtainButtonLabels(lang);
 		this.setVisible(true);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
 		this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 
 		JPanel titleLabel = new JPanel();
 		titleLabel.setBackground(Color.CYAN);
 
 		JLabel title = new JLabel("Sorry!");
 		title.setFont(new Font("Georgia", Font.BOLD, 60));
 		titleLabel.setPreferredSize(new Dimension(TITLE_WIDTH, TITLE_HEIGHT));
 		titleLabel.add(title, BorderLayout.CENTER);
 		this.add(titleLabel, BorderLayout.NORTH);
 
 		this.buttonPanel = new JPanel();
 		this.buttons = new ArrayList<JButton>();
 
 		this.initializeButtons();
 		this.add(buttonPanel);
 	}
 
 	/**
 	 * Fetch the button labels from an appropriate location based on the given
 	 * language.
 	 * 
 	 * @param lang
 	 * @return
 	 */
 	private static String[] obtainButtonLabels(String lang) {
 		String[] ret = new String[6];
 		Scanner in;
 		try {
 			in = new Scanner(new File(lang + ".txt"));
			for (int x = 0; x < 26; x++)
 				in.nextLine();
 
 			ret[0] = in.nextLine();
 			ret[1] = in.nextLine();
 			ret[2] = in.nextLine();
 			ret[3] = in.nextLine();
 			ret[4] = in.nextLine();
 			ret[5] = in.nextLine();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	private void initializeButtons() {
 		if (this.buttonPanel == null)
 			this.buttonPanel = new JPanel();
 		if (this.buttons == null)
 			this.buttons = new ArrayList<JButton>();
 
 		JButton newGame = new JButton(this.labels[0]);
 		this.buttons.add(newGame);
 		JButton loadGame = new JButton(this.labels[1]);
 		this.buttons.add(loadGame);
 		JButton instructions = new JButton(this.labels[2]);
 		this.buttons.add(instructions);
 		JButton connect = new JButton(this.labels[3]);
 		this.buttons.add(connect);
 		JButton host = new JButton(this.labels[4]);
 		this.buttons.add(host);
 		JButton exit = new JButton(this.labels[5]);
 		this.buttons.add(exit);
 		JButton Language = new JButton("Select a new Language \\\\ slectionner une nouvelle langue \\\\ 53l3C7 4 n3w L4N9U493");
 		this.buttons.add(Language);
 		newGame.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				MenuFrame.this.newGameFrame();
 			}
 		});
 
 		loadGame.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MenuFrame.this.loadExistingGame();
 
 			}
 		});
 
 		instructions.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MenuFrame.this.displayInstructions();
 
 			}
 		});
 
 		exit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 
 		connect.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MenuFrame.this.connect();
 			}
 		});
 
 		host.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				MenuFrame.this.host();
 			}
 		});
 		
 		Language.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				MenuFrame.this.dispose();
 				PickLanguage s = new PickLanguage();
 				s.setVisible(true);
 				s.repaint();
 			}
 		});
 
 		for (JButton b : this.buttons)
 			this.buttonPanel.add(b);
 
 	}
 
 	protected void loadExistingGame() {
 		// TODO update to reflect the constructor reading from a file
 		SorryFrame sorry = new SorryFrame(this.language);
 		sorry.load("save.txt");
 		this.dispose();
 		sorry.start();
 	}
 
 	protected void newGameFrame() {
 		MenuFrame.this.getContentPane().removeAll();
 		MenuFrame.this.repaint();
 		Scanner in = null;
 		try {
 			in = new Scanner(new File(MenuFrame.this.language + ".txt"));
 			for (int x = 0; x < 33; x++)
 				in.nextLine();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		JPanel newGamePanel = new JPanel();
 		this.setTitle(in.nextLine());
 		newGamePanel.setLayout(new GridLayout(3, 1));
 		newGamePanel.add(new JLabel(in.nextLine(), JLabel.CENTER));
 		JPanel textBoxPanel = new JPanel();
 		textBoxPanel.setLayout(new GridLayout(4, 1));
 		p1 = new JTextField();
 		p2 = new JTextField();
 		p3 = new JTextField();
 		p4 = new JTextField();
 		p1.setEditable(true);
 		p2.setEditable(true);
 		p3.setEditable(true);
 		p4.setEditable(true);
 		p1.setBackground(Color.RED);
 		p1.setForeground(Color.WHITE);
 		p2.setBackground(Color.BLUE);
 		p2.setForeground(Color.WHITE);
 		p3.setBackground(Color.YELLOW);
 		p4.setBackground(Color.GREEN);
 		textBoxPanel.add(p1);
 		textBoxPanel.add(p2);
 		textBoxPanel.add(p3);
 		textBoxPanel.add(p4);
 		newGamePanel.add(textBoxPanel);
 		JButton btnStart = new JButton(in.nextLine());
 		JPanel start = new JPanel();
 		start.setLayout(new FlowLayout(FlowLayout.CENTER));
 		start.add(btnStart);
 		this.getRootPane().setDefaultButton(btnStart);
 		btnStart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent evt) {
 				MenuFrame.this.createNewGame();
 
 			}
 
 		});
 
 		newGamePanel.add(start);
 		newGamePanel.setVisible(true);
 		newGamePanel.setPreferredSize(new Dimension(600, 800));
 		newGamePanel.repaint();
 		this.add(newGamePanel, BorderLayout.NORTH);
 		this.setVisible(true);
 		this.repaint();
 	}
 
 	/**
 	 * Method to ask the user for names then create a new save file, then load
 	 * the fresh game.
 	 * 
 	 */
 	protected void createNewGame() {
 		// TODO Auto-generated method stub.
 		ArrayList<String> player = new ArrayList<String>();
 		if (!MenuFrame.this.p1.getText().isEmpty())
 			player.add(p1.getText() + "|red");
 		if (!MenuFrame.this.p2.getText().isEmpty())
 			player.add(p2.getText() + "|blue");
 		if (!MenuFrame.this.p3.getText().isEmpty())
 			player.add(p3.getText() + "|yellow");
 		if (!MenuFrame.this.p4.getText().isEmpty())
 			player.add(p4.getText() + "|green");
 		if (player.size() > 1) {
 			SorryFrame sorry = new SorryFrame(this.language);
 			sorry.passPlayers(player);
 			sorry.start();
 			this.dispose();
 		}
 
 	}
 
 	private void displayInstructions() {
 		new InstructionsFrame(this.language);
 	}
 
 	public static void main(String args[]) {
 		new MenuFrame("english");
 	}
 
 	public void connect() {
 		MenuFrame.this.getContentPane().removeAll();
 		MenuFrame.this.repaint();
 		Scanner in = null;
 		try {
 			in = new Scanner(new File(this.language + ".txt"));
 			for (int x = 0; x < 37; x++)
 				in.nextLine();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.setTitle(in.nextLine());
 		JPanel connectPanel = new JPanel();
 		GridLayout gl = new GridLayout(4, 0);
 		gl.setVgap(15);
 		connectPanel.setLayout(new GridLayout(4, 1, 0, 150));
 		this.tfHostIP = new JTextField("", 40);
 		this.tfPort = new JTextField("", 40);
 		this.tfUsername = new JTextField("", 40);
 		tfHostIP.setEditable(true);
 		tfPort.setEditable(true);
 		tfUsername.setEditable(true);
 		JPanel hostIPPanel = new JPanel(new BorderLayout());
 		JPanel portPanel = new JPanel(new BorderLayout());
 		JPanel userPanel = new JPanel(new BorderLayout());
 		hostIPPanel.add(new JLabel(in.nextLine()), BorderLayout.WEST);
 		hostIPPanel.add(tfHostIP, BorderLayout.EAST);
 		connectPanel.add(hostIPPanel);
 		portPanel.add(new JLabel(in.nextLine()), BorderLayout.WEST);
 		portPanel.add(tfPort, BorderLayout.EAST);
 		connectPanel.add(portPanel);
 		userPanel.add(new JLabel(in.nextLine()), BorderLayout.WEST);
 		userPanel.add(tfUsername, BorderLayout.EAST);
 		connectPanel.add(userPanel);
 		JPanel s = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		JButton btnStart = new JButton(in.nextLine());
 		s.add(btnStart);
 		this.getRootPane().setDefaultButton(btnStart);
 		btnStart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				NetworkGameEngine client = new NetworkGameEngine(
 						MenuFrame.this.tfHostIP.getText(), Integer
 								.parseInt(MenuFrame.this.tfPort.getText()),
 						new Player(Piece.COLOR.colorless,
 								MenuFrame.this.tfUsername.getText()),
 						MenuFrame.this.language);
 				client.fetchAllPlayers();
 				client.getUpdatedInfo();
 				SorryFrame sf = new SorryFrame(MenuFrame.this.language, client);
 				sf.start();
 				MenuFrame.this.dispose();
 			}
 		});
 		connectPanel.add(s);
 		connectPanel.setVisible(true);
 		connectPanel.setPreferredSize(new Dimension(600, 800));
 		connectPanel.repaint();
 		MenuFrame.this.add(connectPanel, BorderLayout.NORTH);
 		MenuFrame.this.setVisible(true);
 		MenuFrame.this.repaint();
 
 	}
 
 	public void populateHostPanel(JPanel hostPanel) {
 		JPanel textBoxPanel = new JPanel();
 		textBoxPanel.setLayout(new GridLayout(4, 1));
 		p1 = new JTextField();
 		p2 = new JTextField();
 		p3 = new JTextField();
 		p4 = new JTextField();
 		p1.setEditable(true);
 		p2.setEditable(true);
 		p3.setEditable(true);
 		p4.setEditable(true);
 		p1.setBackground(Color.RED);
 		p1.setForeground(Color.WHITE);
 		p2.setBackground(Color.BLUE);
 		p2.setForeground(Color.WHITE);
 		p3.setBackground(Color.YELLOW);
 		p4.setBackground(Color.GREEN);
 		textBoxPanel.add(p1);
 		textBoxPanel.add(p2);
 		textBoxPanel.add(p3);
 		textBoxPanel.add(p4);
 		hostPanel.add(textBoxPanel);
 	}
 
 	public void host() {
 		MenuFrame.this.getContentPane().removeAll();
 		MenuFrame.this.repaint();
 
 		Scanner in = null;
 		try {
 			in = new Scanner(new File(this.language + ".txt"));
 			for (int x = 0; x < 43; x++)
 				in.nextLine();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.setTitle(in.nextLine());
 		JPanel hostPanel = new JPanel();
 		hostPanel.setLayout(new GridLayout(3, 1));
 		hostPanel.add(new JLabel(in.nextLine(), JLabel.CENTER));
 		this.populateHostPanel(hostPanel);
 
 		JButton btnStart = new JButton(in.nextLine());
 		JButton btnLoad = new JButton(in.nextLine());
 		JPanel start = new JPanel();
 		start.setLayout(new FlowLayout(FlowLayout.CENTER));
 		start.add(btnStart);
 		start.add(btnLoad);
 
 		btnStart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent evt) {
 				player = new ArrayList<String>();
 				Engine EHost = new Engine(new BoardList(),
 						MenuFrame.this.language);
 				EHost.board.newGame();
 				EHost.backupBoard = EHost.board.clone();
 				if (!MenuFrame.this.p1.getText().isEmpty()) {
 					EHost.insertPlayer(new Player(Piece.COLOR.red,
 							MenuFrame.this.p1.getText()));
 					player.add(p1.getText());
 				}
 				if (!MenuFrame.this.p2.getText().isEmpty()) {
 					EHost.insertPlayer(new Player(Piece.COLOR.blue,
 							MenuFrame.this.p2.getText()));
 					player.add(p2.getText());
 				}
 				if (!MenuFrame.this.p3.getText().isEmpty()) {
 					EHost.insertPlayer(new Player(Piece.COLOR.yellow,
 							MenuFrame.this.p3.getText()));
 					player.add(p3.getText());
 				}
 				if (!MenuFrame.this.p4.getText().isEmpty()) {
 					EHost.insertPlayer(new Player(Piece.COLOR.green,
 							MenuFrame.this.p4.getText()));
 					player.add(p4.getText());
 				}
 
 				if (player.size() > 1) {
 					MenuFrame.this.startUpHostServer(EHost);
 				}
 			}
 		});
 		btnLoad.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent evt) {
 				ArrayList<String> fileInput = new ArrayList<String>();
 				Engine EHost = new Engine(new BoardList(),
 						MenuFrame.this.language);
 				SorryServer host;
 				File file = new File("save.txt");
 				BoardList board;
 				Scanner reader;
 				try {
 					reader = new Scanner(file);
 					while (reader.hasNext()) {
 						fileInput.add(reader.nextLine());
 					}
 					int a = passPlayers(fileInput, EHost);
 					board = new BoardList((fileInput.get(a + 1)));
 					EHost.load(board, board.clone(), board.pieceList);
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 				if (player.size() > 1) {
 					MenuFrame.this.startUpHostServer(EHost);
 				}
 
 			}
 		});
 		hostPanel.add(start);
 		hostPanel.setVisible(true);
 		hostPanel.setPreferredSize(new Dimension(600, 800));
 		hostPanel.repaint();
 		MenuFrame.this.add(hostPanel, BorderLayout.NORTH);
 		MenuFrame.this.setVisible(true);
 		MenuFrame.this.repaint();
 
 	}
 
 	/**
 	 * Given a game engine, get a server started ready to host games on the
 	 * local machine.
 	 * 
 	 * @param Engine
 	 *            EHost
 	 */
 	protected void startUpHostServer(Engine EHost) {
 		SorryServer host = new SorryServer(EHost);
 		if (host.attemptServerStartUp(8080))
 			port = 8080;
 		else if (host.attemptServerStartUp(8080))
 			port = 8080;
 		else {
 			port = 4000;
 			while (host.attemptServerStartUp(port) == false) {
 				port++;
 			}
 
 		}
 		EHost.rotatePlayers();
 		EHost.getNextCard();
 		MenuFrame.this.Info();
 
 	}
 
 	protected void Info() {
 		MenuFrame.this.getContentPane().removeAll();
 		MenuFrame.this.repaint();
 
 		Scanner in = null;
 		try {
 			in = new Scanner(new File(this.language + ".txt"));
 			for (int x = 0; x < 48; x++)
 				in.nextLine();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.setTitle(in.nextLine());
 		JPanel infoPanel = new JPanel(new GridLayout(4, 1));
 		Socket s = null;
 		try {
 			s = new Socket("google.com", 80);
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		MenuFrame.this.IP = s.getLocalAddress().getHostAddress();
 		infoPanel.add(new JLabel(in.nextLine()));
 		infoPanel.add(new JLabel(in.nextLine() + MenuFrame.this.IP));
 		infoPanel.add(new JLabel(in.nextLine() + MenuFrame.this.port));
 		JButton btnOkay = new JButton(in.nextLine());
 		btnOkay.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				NetworkGameEngine host = new NetworkGameEngine(
 						MenuFrame.this.IP, MenuFrame.this.port, new Player(
 								Piece.COLOR.red, MenuFrame.this.player.get(0)),
 						MenuFrame.this.language);
 				host.fetchAllPlayers();
 				host.getUpdatedInfo();
 				SorryFrame sf = new SorryFrame(MenuFrame.this.language, host);
 				sf.start();
 				MenuFrame.this.dispose();
 
 			}
 
 		});
 		this.getRootPane().setDefaultButton(btnOkay);
 		JPanel okPanel = new JPanel(new FlowLayout());
 		okPanel.add(btnOkay);
 		infoPanel.add(okPanel);
 		infoPanel.setVisible(true);
 		infoPanel.setPreferredSize(new Dimension(600, 800));
 		infoPanel.repaint();
 		MenuFrame.this.add(infoPanel, BorderLayout.NORTH);
 		MenuFrame.this.setVisible(true);
 		MenuFrame.this.repaint();
 
 	}
 
 	// protected void hostGame() {
 	// // TODO Auto-generated method stub
 	//
 	// }
 	private int passPlayers(ArrayList<String> str, EngineInterface engine) {
 		int ret = 0;
 		for (String string : str) {
 			if (string == "") {
 				break;
 			}
 			String[] players = string.split("\\|");
 			if (players.length != 2) {
 				break;
 			}
 			switch (players[1]) {
 			case "red":
 				engine.insertPlayer(new Player(Piece.COLOR.red, players[0]));
 				player.add(players[0]);
 				break;
 			case "green":
 				engine.insertPlayer(new Player(Piece.COLOR.green, players[0]));
 				player.add(players[0]);
 				break;
 			case "blue":
 				engine.insertPlayer(new Player(Piece.COLOR.blue, players[0]));
 				player.add(players[0]);
 				break;
 			case "yellow":
 				engine.insertPlayer(new Player(Piece.COLOR.yellow, players[0]));
 				player.add(players[0]);
 				break;
 			}
 			ret++;
 		}
 		return ret;
 	}
 }
