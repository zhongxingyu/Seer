 package org.frustra.tetrus;
 
 import java.applet.Applet;
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.Transparency;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.UIManager;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.text.StyledDocument;
 
 import org.frustra.jp33r.PacketListener;
 import org.frustra.jp33r.PeerPacket;
 import org.frustra.jp33r.PeerSocket;
 
 
public class TetrUs extends Applet implements KeyListener, MouseListener {
 	private static final long serialVersionUID = 1L;
 
 	public static final int WIDTH = 425;
 	public static final int HEIGHT = 530;
 
 	public static final byte PACKET_LOC = 0x05;
 	public static final byte PACKET_PLACE = 0x06;
 	public static final byte PACKET_BOARD = 0x07;
 	public static final byte PACKET_SCORE = 0x08;
 	
 	Image logo;
 	BufferedImage canvas;
 	Graphics2D g2;
 
 	JPanel loginPane;
 	JLabel errorLabel;
 	JLabel userLabel;
 	JLabel passLabel;
 	JTextField userField;
 	JPasswordField passField;
 	JButton loginButton;
 	JButton backButton;
 
 	JPanel modeSelectPane;
 	JLabel selectLabel;
 	JButton readyButton;
 	JButton inviteButton;
 	JButton exitButton;
 	JButton singleButton;
 	JButton multiplayerButton;
 
 	JPanel lobbyChatPane;
 	StyleContext chatStyleContext;
     StyledDocument chatDocument;
 	JTextPane chatListArea;
 	JTextArea chatArea;
 	
 	JPanel lobbyPlayersPane;
 	JPanel lobbyButtonsPane;
	JList<SortedPlayer> lobbyPlayersList;
 
 	String remoteName = null;
 	String remoteIP = null;
 	int remotePort = -1;
 	SortedPlayer localPlayer;
 	SortedPlayer invitedPlayer;
 	SortedPlayer[] lobbyPlayers = new SortedPlayer[0];
 
 	boolean loggedIn = false;
 	boolean modeSelected = false;
 	boolean inLobby = false;
 	boolean waiting = false;
 	boolean singlePlayer = false;
 	String user;
 	String pass;
 	
 	boolean[] keys = new boolean[5];
 	
 	int logicSpeed = 5;
 	int fallSpeed = 750;
 	int placeWaitTime = 750;
 	long keyTimer = 0;
 	long scoreTimer = 0;
 	boolean errorTrigger = false;
 	boolean drawTrigger = false;
 
 	int nextBlock = -1;
 	int currBlock = -1;
 	int currX = -1;
 	int currY = -1;
 	boolean[][] currPiece = new boolean[0][0];
 	int[][] blocks = new int[10][18];
 
 	int score = 0;
 	int lines = 0;
 
 	int p2Block = -1;
 	int p2X = -1;
 	int p2Y = -1;
 	boolean[][] p2Piece = new boolean[0][0];
 	
 	PeerSocket socket;
 	byte lastRow = 0;
 	@SuppressWarnings("unchecked")
 	ArrayList<Integer>[] rows = new ArrayList[256];
 	boolean connecting = true;
 	long timeOffset = 0;
 	
 	Color[] colors = new Color[] {
 		new Color(0, 255, 0),
 		new Color(102, 204, 255),
 		new Color(255, 0, 0),
 		new Color(0, 0, 255),
 		new Color(204, 0, 255),
 		new Color(255, 102, 0),
 		new Color(255, 255, 0),
 	};
 	BufferedImage[] blockCache = new BufferedImage[7];
 	boolean[][][] pieces = new boolean[][][] {
 		{
 			{true, true, false},
 			{false, true, true}
 		},
 		{
 			{false, true, true},
 			{true, true, false}
 		},
 		{
 			{true, true},
 			{true, true}
 		},
 		{
 			{false, false, true},
 			{true, true, true}
 		},
 		{
 			{true, false, false},
 			{true, true, true}
 		},
 		{
 			{true, true, true, true}
 		},
 		{
 			{false, true, false},
 			{true, true, true}
 		}
 	};
 	
 	public void paint(Graphics g) {
 		update(g);
 	}
 	
 	public void update(Graphics g) {
 		g2.setFont(new Font("Arial", Font.BOLD, 24));
 		g2.setBackground(Color.WHITE);
 		g2.clearRect(0, 0, WIDTH, HEIGHT);
 		g2.setColor(Color.BLACK);
 		g2.fillRect(15, 15, 250, 500);
 		g2.setColor(Color.RED);
 		g2.fillRect(15, 63, 250, 4);
 		g2.setColor(Color.WHITE);
 		if (connecting) {
 			g2.drawImage(logo, 15, 15, null);
 			g2.setColor(Color.WHITE);
 			g2.drawString("Connecting...", 25, 95);
 		} else if (!modeSelected || inLobby) {
 			g2.drawImage(logo, 15, 15, null);
 		} else if (!loggedIn && !singlePlayer) {
 			g2.drawImage(logo, 15, 15, null);
 		} else {
 			g2.setColor(Color.BLACK);
 			g2.fillRect(280, 15, 130, 80);
 			try {
 				int level = 11 - (fallSpeed / 50);
 				g2.drawString("Level: " + level, 280, 140);
 				g2.drawString("Score: " + score, 280, 170);
 				g2.drawString("Lines: " + lines, 280, 200);
 				
 				synchronized (pieces[nextBlock]) {
 					for (int y = 0; y < pieces[nextBlock].length; y++) {
 						for (int x = 0; x < pieces[nextBlock][y].length; x++) {
 							if (pieces[nextBlock][y][x]) {
 								drawBlockABS(g2, nextBlock, 345 + (int) ((x - (pieces[nextBlock][y].length / 2.0)) * 25.0), 55 + (int) ((y - (pieces[nextBlock].length / 2.0)) * 25.0), 180);
 							}
 						}
 					}
 				}
 				synchronized (currPiece) {
 					for (int x = 0; x < currPiece.length; x++) {
 						for (int y = 0; y < currPiece[x].length; y++) {
 							if (currPiece[x][y]) {
 								drawBlock(g2, currBlock, currX + x, currY + y, 255);
 							}
 						}
 					}
 				}
 				synchronized (p2Piece) {
 					for (int x = 0; x < p2Piece.length; x++) {
 						for (int y = 0; y < p2Piece[x].length; y++) {
 							if (p2Piece[x][y]) {
 								drawBlock(g2, p2Block, p2X + x, p2Y + y, 50);
 							}
 						}
 					}
 				}
 				synchronized (blocks) {
 					for (int x = 0; x < 10; x++) {
 						for (int y = 0; y < 18; y++) {
 							if (blocks[x][y] > -1 && blocks[x][y] < colors.length) {
 								drawBlock(g2, blocks[x][y], x, y, 180);
 							}
 						}
 					}
 				}
 				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
 			} catch (Exception e) {
 				g2.setColor(Color.RED);
 				g2.drawString("Drawing error: " + e, 25, 80);
 				if (!errorTrigger) {
 					e.printStackTrace();
 					errorTrigger = true;
 				}
 			}
 		}
 		super.paint(g2);
 		g.drawImage(canvas, 0, 0, this);
 		drawTrigger = false;
 		//g.drawString("Fps: " + Painter.fps, 50, 50);
 	}
 	
 	public void drawBlock(Graphics2D g, int type, int x, int y, int alpha) {
 		if (x >= 0 && y >= -2 && x < 10 && y < 18) {
 			drawBlockABS(g, type, 15 + (25 * x), 65 + (25 * y), alpha);
 		}
 	}
 	
 	public void drawBlockABS(Graphics2D g, int type, int x, int y, int alpha) {
 		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, new Float(alpha / 255.0)));
 		g.drawImage(blockCache[type], x, y, this);
 	}
 	
 	public void cacheBlocks() {
 		Graphics2D g;
 		for (int i = 0; i < blockCache.length; i++) {
 			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
 			GraphicsDevice device = env.getDefaultScreenDevice();
 			GraphicsConfiguration config = device.getDefaultConfiguration();
 			blockCache[i] = config.createCompatibleImage(25, 25, Transparency.TRANSLUCENT);
 			g = blockCache[i].createGraphics();
 			Color c = colors[i];
 			g.setColor(new Color(Math.min(255, c.getRed() + 100), Math.min(255, c.getGreen() + 100), Math.min(255, c.getBlue() + 100), 255));
 			g.fillPolygon(new int[] {0, 25, 0}, new int[] {0, 0, 25}, 3);
 			g.setColor(new Color(Math.max(0, c.getRed() - 100), Math.max(0, c.getGreen() - 100), Math.max(0, c.getBlue() - 100), 255));
 			g.fillPolygon(new int[] {25, 25, 0}, new int[] {0, 25, 25}, 3);
 			g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 180));
 			g.fillRect(3, 3, 19, 19);
 		}
 	}
 	
 	public void checkRows() {
 		synchronized (blocks) {
 			lastRow++;
 			rows[lastRow & 0x00FF] = new ArrayList<Integer>();
 			int row = 0;
 			while (row >= 0) {
 				row = -1;
 				row: for (int y = 17; y >= 0; y--) {
 					for (int x = 0; x < 10; x++) {
 						if (blocks[x][y] < 0 && row < 0) {
 							continue row;
 						} else if (row >= 0) {
 							blocks[x][y + 1] = blocks[x][y];
 							blocks[x][y] = -1;
 						}
 					}
 					row = y;
 				}
 				if (row >= 0) rows[lastRow & 0x00FF].add(row);
 			}
 			
 			if (rows[lastRow & 0x00FF].size() > 0) {
 				fallSpeed -= 7;
 				lines += rows[lastRow & 0x00FF].size();
 				score += rows[lastRow & 0x00FF].size() * 10;
 				//System.out.println(fallSpeed);
 			}
 			if (!singlePlayer) {
 				try {
 					byte[] data = new byte[10];
 					data[0] = (byte) (fallSpeed >> 8 & 0x00FF);
 					data[1] = (byte) (fallSpeed & 0x00FF);
 	
 					data[2] = (byte) (score >> 24 & 0x00FF);
 					data[3] = (byte) (score >> 16 & 0x00FF);
 					data[4] = (byte) (score >> 8 & 0x00FF);
 					data[5] = (byte) (score & 0x00FF);
 	
 					data[6] = (byte) (lines >> 24 & 0x00FF);
 					data[7] = (byte) (lines >> 16 & 0x00FF);
 					data[8] = (byte) (lines >> 8 & 0x00FF);
 					data[9] = (byte) (lines & 0x00FF);
 					socket.send(TetrUs.PACKET_SCORE, (byte) 0x00, data);
 				} catch (IOException e) {
 					if (!(e instanceof SocketException) || !e.getMessage().equals("Socket is closed")) e.printStackTrace();
 					return;
 				}
 			}
 		}
 	}
 	
 	public void placeBlock(boolean[][] piece, int type, int x, int y) {
 		synchronized (blocks) {
 			for (int x2 = 0; x2 < piece.length; x2++) {
 				for (int y2 = 0; y2 < piece[x2].length; y2++) {
 					if (piece[x2][y2]) {
 						if ((x + x2) >= 0 && (x + x2) < 10 && (y + y2) < 18 && (y + y2) >= 0) {
 							blocks[x + x2][y + y2] = type;
 						} else {
 							// Lose game
 							if (!singlePlayer) {
 								joinLobby("Game Over");
 							} else {
 								fallSpeed = 500;
 								nextBlock = -1;
 								score = 0;
 								lines = 0;
 								getNewBlock();
 								for (int x3 = 0; x3 < 10; x3++) {
 									for (int y3 = 0; y3 < 18; y3++) {
 										blocks[x3][y3] = -1;
 									}
 								}
 								
 								modeSelectPane.setVisible(true);
 								modeSelected = false;
 							}
 							return;
 						}
 					}
 				}
 			}
 		}
 		if (socket.isServerSocket() || singlePlayer) {
 			score++;
 			checkRows();
 		}
 		drawTrigger = true;
 	}
 	
 	public void getNewBlock() {
 		if (nextBlock < 0) nextBlock = (int) (Math.random() * 7);
 		synchronized (currPiece) {
 			synchronized (pieces[nextBlock]) {
 				currBlock = nextBlock;
 				nextBlock = (int) (Math.random() * 7);
 				currX = 5 - (pieces[currBlock][0].length / 2);
 				currY = -pieces[currBlock].length;
 				currPiece = new boolean[pieces[currBlock][0].length][pieces[currBlock].length];
 				for (int x = 0; x < currPiece.length; x++) {
 					for (int y = 0; y < currPiece[x].length; y++) {
 						if (pieces[currBlock][y][x]) currPiece[x][y] = true;
 					}
 				}
 			}
 		}
 		drawTrigger = true;
 	}
 	
 	public boolean collideBlock(boolean[][] piece, int x, int y) {
 		for (int x2 = 0; x2 < piece.length; x2++) {
 			for (int y2 = 0; y2 < piece[x2].length; y2++) {
 				if (piece[x2][y2] && ((x + x2) < 0 || (x + x2) >= 10 || (y + y2) >= 18 || ((y + y2) >= 0 && blocks[x + x2][y + y2] >= 0))) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public void init() {
 		setSize(new Dimension(WIDTH, HEIGHT));
 		setPreferredSize(new Dimension(WIDTH, HEIGHT));
 		
 		try {
 			logo = ImageIO.read(new URL("http://www.frustra.org/TetrUsLogo.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		GraphicsDevice device = env.getDefaultScreenDevice();
 		GraphicsConfiguration config = device.getDefaultConfiguration();
 		canvas = config.createCompatibleImage(WIDTH, HEIGHT, Transparency.OPAQUE);
 		g2 = canvas.createGraphics();
 		
 		cacheBlocks();
 
 		setLayout(null);
 		buildModeSelect();
 		buildNick();
 		//buildLogin();
 		buildLobby();
 
 		fallSpeed = 500;
 		nextBlock = -1;
 		score = 0;
 		lines = 0;
 		for (int x = 0; x < 10; x++) {
 			for (int y = 0; y < 18; y++) {
 				blocks[x][y] = -1;
 			}
 		}
 		getNewBlock();
 	}
 	
 	public void buildLobby() {
 		lobbyChatPane = new JPanel();
 		lobbyChatPane.setLayout(null);
 		lobbyChatPane.setBounds(15, 67, 250, 448);
 		
 		chatStyleContext = new StyleContext();
 		chatDocument = new DefaultStyledDocument(chatStyleContext);
 		
 		chatListArea = new JTextPane(chatDocument);
 		chatListArea.setEditable(false);
 		JScrollPane scrollPane = new JScrollPane(chatListArea);
 		scrollPane.setBounds(0, 0, 250, 398);
 		lobbyChatPane.add(scrollPane);
 		
 		chatArea = new JTextArea();
 		chatArea.setFont(new Font("Courier New", Font.PLAIN, 11));
 		chatArea.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent key) {
 				if (key.getKeyCode() == KeyEvent.VK_ENTER) {
 					if (!key.isShiftDown()) {
 						if (chatArea.getText().length() > 0) {
 							if (chatArea.getText().length() > 150) {
 								addChatMessage(Color.RED, "Message is too long!");
 							} else {
 								addChatMessage(user, chatArea.getText());
 								final String msg = chatArea.getText();
 								new Thread() {
 									public void run() {
 										try {
 											String result = excutePost("http://www.frustra.org/tetrus.php", "action=sendchat&n=" + URLEncoder.encode(user, "UTF-8") + /*"&p=" + URLEncoder.encode(pass, "UTF-8") + */"&m=" + URLEncoder.encode(msg, "UTF-8"));
 											if (result == null || !result.contains("SENT")) System.out.println(result);
 										} catch (UnsupportedEncodingException e) {
 											e.printStackTrace();
 										}
 									}
 								}.start();
 								chatArea.setText("");
 							}
 						}
 					} else {
 						String text = chatArea.getText();
 						int pos = chatArea.getCaretPosition();
 						chatArea.setText(text.substring(0, pos) + "\n" + text.substring(pos));
 						chatArea.setCaretPosition(pos + 1);
 					}
 					key.consume();
 				}
 			}
 			
 			public void keyReleased(KeyEvent key) {}
 			public void keyTyped(KeyEvent key) {}
 		});
 		chatArea.setLineWrap(true);
 		chatArea.setWrapStyleWord(true);
 		JScrollPane scrollPane2 = new JScrollPane(chatArea);
 		scrollPane2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
 		scrollPane2.setBounds(0, 398, 250, 50);
 		lobbyChatPane.add(scrollPane2);
 		
 		lobbyPlayersPane = new JPanel();
 		lobbyPlayersPane.setLayout(null);
 		lobbyPlayersPane.setBounds(280, 15, 130, 385);
 		
		lobbyPlayersList = new JList<SortedPlayer>();
 		lobbyPlayersList.setListData(new SortedPlayer[0]);
 		lobbyPlayersList.setCellRenderer(new DefaultListCellRenderer() {
 			private static final long serialVersionUID = 1L;
 			
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean iss, boolean chf) {
 				super.getListCellRendererComponent(list, value, index, iss, chf);
 				try {
 					setBackground(lobbyPlayers[index].backColor);
 				} catch (ArrayIndexOutOfBoundsException e) {}
 				return this;
 			}
 		});
 		lobbyPlayersList.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent event) {
 				SortedPlayer tmp = (SortedPlayer) lobbyPlayersList.getSelectedValue();
 				if (tmp == null || tmp.name.equalsIgnoreCase(localPlayer.name)) {
 					inviteButton.setText("Invite Player");
 					inviteButton.setEnabled(false);
 				} else if (invitedPlayer != null && tmp.name.equalsIgnoreCase(invitedPlayer.name)) {
 					inviteButton.setText("Cancel Invite");
 					inviteButton.setEnabled(true);
 				} else if (tmp.status != 2) {
 					inviteButton.setText("Invite Player");
 					inviteButton.setEnabled(true);
 				} else {
 					inviteButton.setText("Accept Invite");
 					inviteButton.setEnabled(true);
 				}
 			}
 		});
 		JScrollPane scrollPane3 = new JScrollPane(lobbyPlayersList);
 		scrollPane3.setBounds(0, 0, 130, 385);
 		lobbyPlayersPane.add(scrollPane3);
 		
 		lobbyButtonsPane = new JPanel();
 		lobbyButtonsPane.setLayout(null);
 		lobbyButtonsPane.setBounds(280, 415, 130, 100);
 
 		readyButton = new JButton("Quick Join");
 		readyButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent action) {
 				if (localPlayer.status == 1) {
 					localPlayer.setStatus(0);
 					readyButton.setText("Quick Join");
 				} else {
 					localPlayer.setStatus(1);
 					readyButton.setText("Waiting...");
 				}
 			}
 		});
 		readyButton.setBounds(10, 8, 110, 25);
 		lobbyButtonsPane.add(readyButton);
 		
 		inviteButton = new JButton("Invite Player");
 		inviteButton.setEnabled(false);
 		inviteButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent action) {
 				new Thread() {
 					public void run() {
 						try {
 							SortedPlayer old = invitedPlayer;
 							invitedPlayer = (SortedPlayer) lobbyPlayersList.getSelectedValue();
 							if (invitedPlayer != null && !invitedPlayer.name.equals(localPlayer.name) && (old == null || !old.name.equals(invitedPlayer.name))) {
 								String result = excutePost("http://www.frustra.org/tetrus.php", "action=invite&n=" + URLEncoder.encode(user, "UTF-8") + "&p=" + URLEncoder.encode(invitedPlayer.name, "UTF-8"));
 								if (result == null || !result.contains("SENT")) {
 									System.out.println(result);
 								} else if (result.contains(":")) {
 									String[] msginfo = result.split(":");
 									remoteName = msginfo[1];
 									remoteIP = msginfo[2];
 									remotePort = Integer.valueOf(msginfo[3]);
 									lobbyChatPane.setVisible(false);
 									lobbyButtonsPane.setVisible(false);
 									lobbyPlayersPane.setVisible(false);
 									inLobby = false;
 									connecting = true;
 								} else {
 									inviteButton.setText("Waiting...");
 								}
 							} else {
 								String result = excutePost("http://www.frustra.org/tetrus.php", "action=invite&n=" + URLEncoder.encode(user, "UTF-8") + "&p=");
 								if (result == null || !result.contains("SENT")) {
 									System.out.println(result);
 								} else {
 									if (invitedPlayer != null) {
 										inviteButton.setEnabled(!invitedPlayer.name.equalsIgnoreCase(localPlayer.name));
 										if (invitedPlayer.status == 2) {
 											inviteButton.setText("Accept Invite");
 										} else {
 											inviteButton.setText("Invite Player");
 										}
 										invitedPlayer = null;
 									} else {
 										inviteButton.setEnabled(false);
 										inviteButton.setText("Invite Player");
 									}
 								}
 							}
 						} catch (UnsupportedEncodingException e) {
 							e.printStackTrace();
 						}
 					}
 				}.start();
 			}
 		});
 		inviteButton.setBounds(10, 38, 110, 25);
 		lobbyButtonsPane.add(inviteButton);
 		
 		exitButton = new JButton("Exit Lobby");
 		exitButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent action) {
 				inLobby = false;
 				loggedIn = false;
 				modeSelected = false;
 				modeSelectPane.setVisible(true);
 				lobbyButtonsPane.setVisible(false);
 				lobbyChatPane.setVisible(false);
 				lobbyPlayersPane.setVisible(false);
 			}
 		});
 		exitButton.setBounds(10, 68, 110, 25);
 		lobbyButtonsPane.add(exitButton);
 		
 		lobbyChatPane.setVisible(false);
 		lobbyButtonsPane.setVisible(false);
 		lobbyPlayersPane.setVisible(false);
 		add(lobbyChatPane);
 		add(lobbyButtonsPane);
 		add(lobbyPlayersPane);
 	}
 	
 	public AttributeSet getChatStyle(StyleContext context, boolean bold, Color color) {
 		Style style = context.getStyle(StyleContext.DEFAULT_STYLE);
 	    StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
 	    StyleConstants.setFontSize(style, 11);
     	StyleConstants.setFontFamily(style, "Courier New");
     	StyleConstants.setForeground(style, color);
         StyleConstants.setBold(style, bold);
 	    return style;
 	}
 	
 	public void addChatMessage(Color color, String message) {
 		try {
 			chatDocument.insertString(chatDocument.getLength(), message + "\n", getChatStyle(chatStyleContext, true, color));
 			chatListArea.setCaretPosition(chatListArea.getDocument().getLength());
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void addChatMessage(String user, String message) {
 		try {
 			chatDocument.insertString(chatDocument.getLength(), user + ": ", getChatStyle(chatStyleContext, true, Color.GREEN));
 			chatDocument.insertString(chatDocument.getLength(), message + "\n", getChatStyle(chatStyleContext, false, Color.WHITE));
 			chatListArea.setCaretPosition(chatListArea.getDocument().getLength());
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String excutePost(String targetURL, String urlParameters) {
 		HttpURLConnection connection = null;
 		try {
 			URL url = new URL(targetURL);
 			connection = (HttpURLConnection) url.openConnection();
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 
 			connection.setRequestProperty("Content-Length", "" + urlParameters.getBytes().length);
 			connection.setRequestProperty("Content-Language", "en-US");
 
 			connection.setUseCaches(false);
 			connection.setDoInput(true);
 			connection.setDoOutput(true);
 
 			PrintStream out = new PrintStream(connection.getOutputStream());
 			out.print(urlParameters);
 			out.close();
 
 			InputStream is = connection.getInputStream();
 			Scanner scan = new Scanner(is);
 
 			String response = "";
 			while (scan.hasNextLine()) {
 				response += scan.nextLine();
 				response += '\r';
 			}
 			scan.close();
 			return response;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			if (connection != null) connection.disconnect();
 		}
 	}
 
 	public void buildModeSelect() {
 		int menuX = 25;
 		int menuY = 80;
 		
 		modeSelectPane = new JPanel();
 		modeSelectPane.setLayout(null);
 		modeSelectPane.setBounds(15, 67, 250, 448);
 		
 		selectLabel = new JLabel("Select a game mode:");
 		selectLabel.setHorizontalAlignment(JLabel.CENTER);
 		selectLabel.setBounds(menuX, menuY, 200, 25);
 		modeSelectPane.add(selectLabel);
 		
 		singleButton = new JButton("Single-player");
 		singleButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent action) {
 				System.out.println("Single-player");
 				modeSelectPane.setVisible(false);
 				singlePlayer = true;
 				modeSelected = true;
 
 				fallSpeed = 500;
 				nextBlock = -1;
 				score = 0;
 				lines = 0;
 				p2Block = -1;
 				p2X = -1;
 				p2Y = -1;
 				p2Piece = new boolean[0][0];
 				getNewBlock();
 				for (int x3 = 0; x3 < 10; x3++) {
 					for (int y3 = 0; y3 < 18; y3++) {
 						blocks[x3][y3] = -1;
 					}
 				}
 			}
 		});
 		singleButton.setBounds(menuX, menuY + 30, 200, 25);
 		modeSelectPane.add(singleButton);
 		
 		multiplayerButton = new JButton("Multi-player");
 		multiplayerButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent action) {
 				System.out.println("Multi-player");
 				modeSelectPane.setVisible(false);
 				singlePlayer = false;
 				modeSelected = true;
 				loginPane.setVisible(true);
 				userField.requestFocus();
 			}
 		});
 		multiplayerButton.setBounds(menuX, menuY + 60, 200, 25);
 		modeSelectPane.add(multiplayerButton);
 		add(modeSelectPane);
 	}
 	
 	public void buildNick() {
 		int loginX = 40;
 		int loginY = 80;
 		loginPane = new JPanel();
 		loginPane.setLayout(null);
 		loginPane.setBounds(15, 67, 250, 448);
 		
 		errorLabel = new JLabel();
 		errorLabel.setForeground(Color.RED);
 		errorLabel.setHorizontalAlignment(JLabel.CENTER);
 		errorLabel.setBounds(loginX, loginY, 170, 25);
 		loginPane.add(errorLabel);
 		
 		userLabel = new JLabel("Name:");
 		userLabel.setBounds(loginX, loginY + 30, 70, 25);
 		loginPane.add(userLabel);
 		
 		userField = new JTextField();
 		userField.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent key) {
 				if (key.getKeyCode() == KeyEvent.VK_ENTER) {
 					loginButton.doClick();
 				}
 			}
 			public void keyReleased(KeyEvent key) {}
 			public void keyTyped(KeyEvent key) {}
 		});
 		userField.setBounds(loginX + 70, loginY + 30, 100, 25);
 		loginPane.add(userField);
 		
 		loginButton = new JButton("Join");
 		loginButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent action) {
 				new Thread() {
 					public void run() {
 						if (!loggedIn) {
 							user = userField.getText();
 							loginPane.setVisible(false);
 							if (joinLobby(null)) {
 								loggedIn = true;
 							} else loginPane.setVisible(true);
 						}
 					}
 				}.start();
 			}
 		});
 		loginButton.setBounds(loginX + 87, loginY + 90, 65, 25);
 		loginPane.add(loginButton);
 		
 		backButton = new JButton("Back");
 		backButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent action) {
 				singlePlayer = false;
 				modeSelected = false;
 				loggedIn = false;
 				loginPane.setVisible(false);
 				modeSelectPane.setVisible(true);
 			}
 		});
 		backButton.setBounds(loginX + 17, loginY + 90, 65, 25);
 		loginPane.add(backButton);
 		
 		loginPane.setVisible(false);
 		add(loginPane);
 	}
 	
 	public void buildLogin() {
 		int loginX = 40;
 		int loginY = 80;
 		loginPane = new JPanel();
 		loginPane.setLayout(null);
 		loginPane.setBounds(15, 67, 250, 448);
 		
 		errorLabel = new JLabel();
 		errorLabel.setForeground(Color.RED);
 		errorLabel.setHorizontalAlignment(JLabel.CENTER);
 		errorLabel.setBounds(loginX, loginY, 170, 25);
 		loginPane.add(errorLabel);
 		
 		userLabel = new JLabel("Username:");
 		userLabel.setBounds(loginX, loginY + 30, 70, 25);
 		loginPane.add(userLabel);
 
 		passLabel = new JLabel("Password:");
 		passLabel.setBounds(loginX, loginY + 60, 70, 25);
 		loginPane.add(passLabel);
 		
 		userField = new JTextField();
 		userField.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent key) {
 				if (key.getKeyCode() == KeyEvent.VK_ENTER) {
 					passField.requestFocus();
 				}
 			}
 			public void keyReleased(KeyEvent key) {}
 			public void keyTyped(KeyEvent key) {}
 		});
 		userField.setBounds(loginX + 70, loginY + 30, 100, 25);
 		loginPane.add(userField);
 		
 		passField = new JPasswordField();
 		passField.addKeyListener(new KeyListener() {
 			public void keyPressed(KeyEvent key) {
 				if (key.getKeyCode() == KeyEvent.VK_ENTER) {
 					loginButton.doClick();
 				}
 			}
 			public void keyReleased(KeyEvent key) {}
 			public void keyTyped(KeyEvent key) {}
 		});
 		passField.setBounds(loginX + 70, loginY + 60, 100, 25);
 		loginPane.add(passField);
 		
 		loginButton = new JButton("Login");
 		loginButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent action) {
 				new Thread() {
 					public void run() {
 						if (!loggedIn) {
 							user = userField.getText();
 							pass = md5(new String(passField.getPassword()));
 							loginPane.setVisible(false);
 							if (joinLobby(null)) {
 								loggedIn = true;
 							} else loginPane.setVisible(true);
 						}
 					}
 				}.start();
 			}
 		});
 		loginButton.setBounds(loginX + 87, loginY + 90, 65, 25);
 		loginPane.add(loginButton);
 		
 		backButton = new JButton("Back");
 		backButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent action) {
 				singlePlayer = false;
 				modeSelected = false;
 				loggedIn = false;
 				loginPane.setVisible(false);
 				modeSelectPane.setVisible(true);
 			}
 		});
 		backButton.setBounds(loginX + 17, loginY + 90, 65, 25);
 		loginPane.add(backButton);
 		
 		loginPane.setVisible(false);
 		add(loginPane);
 	}
 	
 	public boolean joinLobby(String joinMsg) {
 		if (inLobby) return true;
 		
 		chatListArea.setText("");
 		lobbyChatPane.setVisible(true);
 		lobbyButtonsPane.setVisible(true);
 		lobbyPlayersPane.setVisible(true);
 
 		readyButton.setText("Quick Join");
 		inviteButton.setText("Invite Player");
 		inLobby = true;
 		
 		try {
 			String ip = InetAddress.getLocalHost().getHostAddress();
 			int port = socket.getPort();
 			while (true) {
 				String message = excutePost("http://www.frustra.org/tetrus.php", "action=joinlobby&n=" + URLEncoder.encode(user, "UTF-8") + /*"&p=" + URLEncoder.encode(pass, "UTF-8") + */"&l=" + ip + "&r=" + port);
 				if (message.contains("SUCCESS")) {
 					String[] msginfo = message.split(":");
 					try {
 						timeOffset = (System.currentTimeMillis() / 1000) - Long.valueOf(msginfo[1].replaceAll("[^0-9]*", ""));
 					} catch (Exception e) {
 						timeOffset = 0;
 					}
 					localPlayer = new SortedPlayer(user, 0);
 					localPlayer.setStatus(0);
 					break;
 				} else if (message.contains("BAD LOGIN")) {
 					//errorLabel.setText("Failed to login");
 					if (joinMsg != null) break;
 					errorLabel.setText("Name already in use");
 					lobbyChatPane.setVisible(false);
 					lobbyPlayersPane.setVisible(false);
 					lobbyButtonsPane.setVisible(false);
 					inLobby = false;
 					return false;
 				} else {
 					if (joinMsg != null) break;
 					errorLabel.setText("Error joining lobby");
 					lobbyChatPane.setVisible(false);
 					lobbyPlayersPane.setVisible(false);
 					lobbyButtonsPane.setVisible(false);
 					inLobby = false;
 					return false;
 				}
 			}
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		
 		System.out.println("Joined lobby");
 		
 		addChatMessage(Color.RED, "Welcome to the TetrUs Lobby!");
 		if (joinMsg != null) addChatMessage(Color.RED, joinMsg);
 		addChatMessage(Color.RED, "");
 		new Thread() {
 			{ this.setDaemon(true); }
 			
 			public void run() {
 				long lastChatTime = (System.currentTimeMillis() / 1000) - timeOffset;
 				while (inLobby) {
 					try {
 						String messages = excutePost("http://www.frustra.org/tetrus.php", "action=refreshlobby&n=" + URLEncoder.encode(user, "UTF-8") + /*"&p=" + URLEncoder.encode(pass, "UTF-8") + */"&r=" + (localPlayer.status == 1 ? "on" : "off") + "&t=" + lastChatTime);
 						Scanner scan = new Scanner(messages);
 						ArrayList<SortedPlayer> players = new ArrayList<SortedPlayer>();
 						players.add(localPlayer);
 						while (scan.hasNextLine()) {
 							String line = scan.nextLine();
 							if (line.equals("NO MESSAGES")) {
 								break;
 							} else if (line.contains(":")) {
 								String[] msginfo = line.split(":");
 								while (!msginfo[msginfo.length - 1].equals("end") && scan.hasNextLine()) {
 									line += "\n" + scan.nextLine();
 									msginfo = line.split(":");
 								}
 								if (!msginfo[msginfo.length - 1].equals("end")) { // Bad message
 									System.out.println("Bad1: " + line);
 									break;
 								} else if (msginfo[0].equals("chat")) {
 									lastChatTime = Long.valueOf(msginfo[3]);
 									if (msginfo[2].length() <= 0) { // Server message
 										addChatMessage(Color.RED, msginfo[4]);
 									} else {
 										addChatMessage(msginfo[2], msginfo[4]);
 									}
 								} else if (msginfo[0].equals("player")) {
 									if (msginfo[3].equals("1")) {
 										players.add(new SortedPlayer(msginfo[1], 2));
 									} else {
 										int status = Integer.valueOf(msginfo[2]);
 										players.add(new SortedPlayer(msginfo[1], status));
 									}
 								} else if (msginfo[0].equals("connect")) {
 									remoteName = msginfo[1];
 									remoteIP = msginfo[2];
 									remotePort = Integer.valueOf(msginfo[3]);
 									lobbyChatPane.setVisible(false);
 									lobbyButtonsPane.setVisible(false);
 									lobbyPlayersPane.setVisible(false);
 									inLobby = false;
 									connecting = true;
 									break;
 								} else {
 									System.out.println(line);
 								}
 							}
 						}
						scan.close();
 						int sel = lobbyPlayersList.getSelectedIndex();
 						SortedPlayer[] players2 = players.toArray(new SortedPlayer[0]);
 						Arrays.sort(players2, new SortedPlayer());
 						lobbyPlayers = players2;
 						lobbyPlayersList.setListData(lobbyPlayers);
 						lobbyPlayersList.setSelectedIndex(sel);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						return;
 					}
 				}
 			}
 		}.start();
 		return true;
 	}
 	
 	public void start() {
 		connecting = false;
 		loggedIn = false;
 		modeSelected = false;
 		inLobby = false;
 
 		new Painter(this);
 		new GameController(this);
 		addKeyListener(this);
		addMouseListener(this);
 		
 		try {
 			socket = new PeerSocket();
 
 			socket.addPacketListener(new PacketListener() {
 				public void receive(PeerPacket p) {
 					if (p == null) {
 						System.out.println("Disconnected");
 						return;
 					}
 					byte[] data = p.getData();
 					if (p.getType() != PACKET_BOARD) {
 						if (p.getType() != PACKET_SCORE) {
 							if (p2Block != (int) data[0] || p2X != (int) data[1] || p2Y != (int) data[2] || p2Piece.length != (int) data[3] || p2Piece[0].length != (int) data[4]) drawTrigger = true;
 							p2Block = (int) data[0];
 							p2X = (int) data[1];
 							p2Y = (int) data[2];
 							int i = 5;
 							synchronized (p2Piece) {
 								p2Piece = new boolean[(int) data[3]][(int) data[4]];
 								for (int x = 0; x < p2Piece.length; x++) {
 									for (int y = 0; y < p2Piece[x].length; y++) {
 										if (data[i++] == 1) p2Piece[x][y] = true;
 									}
 								}
 							}
 							if (p.getType() == PACKET_PLACE) {
 								int received = data[i] & 0x00FF;
 								for (int j = received; j < lastRow; j++) {
 									if (rows[j] != null) {
 										for (Integer rw : rows[j]) {
 											if (rw >= p2Y + p2Piece[0].length) {
 												p2Y++;
 											} else if (rw >= p2Y) {
 												int rw2 = rw - p2Y;
 												for (int y = rw2; y >= 0; y--) {
 													for (int x = 0; x < p2Piece.length; x++) {
 														if (y == rw2) {
 															p2Piece[x][y] = false;
 														} else {
 															p2Piece[x][y + 1] = p2Piece[x][y];
 														}
 													}
 												}
 											}
 										}
 									}
 								}
 								if (p2Y + p2Piece[0].length >= 18) p2Y = 18 - p2Piece[0].length;
 								placeBlock(p2Piece, p2Block, p2X, p2Y);
 								synchronized (blocks) {
 									try {
 										byte[] data2 = new byte[blocks.length * blocks[0].length];
 										i = 0;
 										for (int x = 0; x < blocks.length; x++) {
 											for (int y = 0; y < blocks[x].length; y++) {
 												data2[i++] = (byte) (blocks[x][y]);
 											}
 										}
 										socket.send(PACKET_BOARD, (byte) 0x00, data2);
 									} catch (IOException e) {
 										if (!(e instanceof SocketException) || !e.getMessage().equals("Socket is closed")) e.printStackTrace();
 										return;
 									}
 								}
 							}
 						} else {
 							int tmp = ((int) (data[0] << 8) & 0x00FF00);
 							tmp |= ((int) (data[1]) & 0x00FF);
 							fallSpeed = tmp;
 							tmp = ((int) (data[2] << 24) & 0xFF000000);
 							tmp |= ((int) (data[3] << 16) & 0x00FF0000);
 							tmp |= ((int) (data[4] << 8) & 0x00FF00);
 							tmp |= ((int) (data[5]) & 0x00FF);
 							score = tmp;
 							tmp = ((int) (data[6] << 24) & 0xFF000000);
 							tmp |= ((int) (data[7] << 16) & 0x00FF0000);
 							tmp |= ((int) (data[8] << 8) & 0x00FF00);
 							tmp |= ((int) (data[9]) & 0x00FF);
 							lines = tmp;
 							drawTrigger = true;
 						}
 					} else {
 						synchronized (blocks) {
 							int i = 0;
 							if ((p.getFlags() & 1) == 1) {
 								lastRow = data[0];
 								i = 1;
 							}
 							for (int x = 0; x < blocks.length; x++) {
 								for (int y = 0; y < blocks[x].length; y++) {
 									blocks[x][y] = (int) data[i++];
 								}
 							}
 						}
 						drawTrigger = true;
 					}
 				}
 			});
 			
 			System.out.println("Local info: " + InetAddress.getLocalHost().getHostAddress() + ":" + socket.getPort());
 			
 			login: while (true) {
 				if (!modeSelected) {
 					Thread.sleep(100);
 					continue login;
 				} else if (singlePlayer || !loggedIn || inLobby) {
 					Thread.sleep(100);
 					continue login;
 				}
 				System.out.println("Starting connection...");
 				System.out.println("Playing with " + remoteName + ": " + remoteIP + ":" + remotePort);
 				requestFocusInWindow();
 				
 				while (true) {
 					try {
 						if (socket.connect(remoteIP, remotePort, 5000)) {
 							System.out.println("Connected to " + remoteIP + ":" + remotePort);
 							connecting = false;
 							
 							fallSpeed = 500;
 							nextBlock = -1;
 							score = 0;
 							lines = 0;
 							getNewBlock();
 							for (int x3 = 0; x3 < 10; x3++) {
 								for (int y3 = 0; y3 < 18; y3++) {
 									blocks[x3][y3] = -1;
 								}
 							}
 							
 							if (!singlePlayer) {
 								while (modeSelected && loggedIn && !inLobby) {
 									synchronized (currPiece) {
 										byte[] data = new byte[5 + (currPiece.length * currPiece[0].length)];
 										data[0] = (byte) currBlock;
 										data[1] = (byte) currX;
 										data[2] = (byte) currY;
 										data[3] = (byte) currPiece.length;
 										data[4] = (byte) currPiece[0].length;
 										int i = 5;
 										for (int x = 0; x < currPiece.length; x++) {
 											for (int y = 0; y < currPiece[x].length; y++) {
 												data[i++] = (byte) (currPiece[x][y] ? 1 : 0);
 											}
 										}
 										socket.send(PACKET_LOC, (byte) 0x00, data);
 									}
 									Thread.sleep(100);
 								}
 								socket.close();
 							} else {
 								while (modeSelected && loggedIn && !inLobby) {
 									Thread.sleep(100);
 								}
 								socket.close();
 							}
 						} else {
 							System.out.println("Failed to connect...");
 						}
 					} catch (IOException e) {
 						if (!(e instanceof SocketException) || !e.getMessage().toLowerCase().contains("closed")) {
 							e.printStackTrace();
 						}
 					}
 					joinLobby("The other player has left the game.");
 					continue login;
 				}
 			}
 		} catch (IOException e) {
 			if (!(e instanceof SocketException) || !e.getMessage().toLowerCase().contains("closed")) {
 				e.printStackTrace();
 				System.exit(1);
 			} else System.exit(0);
 		} catch (InterruptedException e) {
 			System.exit(0);
 		}
 	}
 	
 	private char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
 	public String md5(String encrypt) {
 		try {
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			md.update(encrypt.getBytes());
 			byte[] hash = md.digest();
 			char buf[] = new char[hash.length * 2];
 			for (int i = 0, x = 0; i < hash.length; i++) {
 				buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
 				buf[x++] = HEX_CHARS[hash[i] & 0xf];
 			}
 			return new String(buf);
 		} catch (NoSuchAlgorithmException e) {
 			return "MD5 ERROR - " + e;
 		}
 	}
 
 	public static void main(String[] args) throws Exception {
 		UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
 		final TetrUs app = new TetrUs();
 		JFrame frame = new JFrame("TetrUs");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		app.init();
 		frame.add(app);
 		frame.pack();
 		frame.setSize(frame.getWidth() - 4, frame.getHeight() - 2);
 		frame.setVisible(true);
 		frame.setResizable(false);
 		app.start();
 	}
 	
 	private class SortedPlayer implements Comparator<SortedPlayer> {
 		
 		String name;
 		Color backColor;
 		int status;
 		
 		public SortedPlayer() {}
 		
 		public SortedPlayer(String name, int status) {
 			this.setStatus(status);
 			this.name = name;
 		}
 		
 		public String toString() {
 			return name;
 		}
 		
 		public void setStatus(int status) {
 			this.status = status;
 			if (status == 1) {
 				backColor = new Color(0, 0, 50);
 			} else if (status == 2) {
 				backColor = new Color(0, 0, 100);
 			} else {
 				backColor = Color.BLACK;
 				this.status = 0;
 			}
 		}
 
 		public int compare(SortedPlayer a, SortedPlayer b) {
 			if (a.status == b.status) {
 				return 0;
 			} else {
 				return a.status < b.status ? 1 : -1;
 			}
 		}
 	}
 
 	public void keyPressed(KeyEvent key) {
 		if (key.getKeyCode() == KeyEvent.VK_LEFT && !keys[0]) {
 			keyTimer = System.currentTimeMillis() + 75;
 			keys[0] = true;
 			if (!collideBlock(currPiece, currX - 1, currY)) {
 				drawTrigger = true;
 				currX--;
 			}
 		} else if (key.getKeyCode() == KeyEvent.VK_RIGHT && !keys[1]) {
 			keyTimer = System.currentTimeMillis() + 75;
 			keys[1] = true;
 			if (!collideBlock(currPiece, currX + 1, currY)) {
 				drawTrigger = true;
 				currX++;
 			}
 		} else if (key.getKeyCode() == KeyEvent.VK_UP && !keys[2]) {
 			drawTrigger = true;
 			keys[2] = true;
 			
 			boolean[][] tmpPiece = new boolean[currPiece[0].length][currPiece.length];
 			for (int x = 0; x < currPiece.length; x++) {
 				for (int y = 0; y < currPiece[x].length; y++) {
 					if (currPiece[x][y]) tmpPiece[currPiece[x].length - y - 1][x] = true;
 				}
 			}
 			int newX = (int) ((currPiece.length / 2.0) - (tmpPiece.length / 2.0));
 			int newY = (int) ((tmpPiece.length / 2.0) - (currPiece.length / 2.0));
 			if (!collideBlock(tmpPiece, currX + newX, currY + newY)) {
 				currPiece = tmpPiece;
 				currX += newX;
 				currY += newY;
 			} else {
 				int radius = 2;
 				for (int yOff = 0; yOff >= -radius; yOff--) {
 					for (int xOff = 1; xOff <= radius; xOff++) {
 						if (!collideBlock(tmpPiece, currX + newX + xOff, currY + newY - yOff)) {
 							currPiece = tmpPiece;
 							currX += newX + xOff;
 							currY += newY - yOff;
 							return;
 						}
 						if (!collideBlock(tmpPiece, currX + newX - xOff, currY + newY - yOff)) {
 							currPiece = tmpPiece;
 							currX += newX - xOff;
 							currY += newY - yOff;
 							return;
 						}
 						if (yOff != 0) {
 							if (!collideBlock(tmpPiece, currX + newX + xOff, currY + newY + yOff)) {
 								currPiece = tmpPiece;
 								currX += newX + xOff;
 								currY += newY + yOff;
 								return;
 							}
 							if (!collideBlock(tmpPiece, currX + newX - xOff, currY + newY + yOff)) {
 								currPiece = tmpPiece;
 								currX += newX - xOff;
 								currY += newY + yOff;
 								return;
 							}
 						}
 					}
 				}
 			}
 		} else if (key.getKeyCode() == KeyEvent.VK_DOWN && !keys[3]) {
 			keys[3] = true;
 		} else if (key.getKeyCode() == KeyEvent.VK_SPACE && !keys[4]) {
 			keys[4] = true;
 			drawTrigger = true;
 			while (true) {
 				if (collideBlock(currPiece, currX, currY + 1)) {
 					break;
 				} else currY++;
 			}
 		} else if (key.getKeyCode() == KeyEvent.VK_ESCAPE) {
 			if (!inLobby && modeSelected) {
 				if (singlePlayer) {
 					singlePlayer = false;
 					modeSelected = false;
 					modeSelectPane.setVisible(true);
 				} else {
 					joinLobby("Game cancelled.");
 				}
 			}
 			drawTrigger = true;
 		}
 	}
 
 	public void keyReleased(KeyEvent key) {
 		if (key.getKeyCode() == KeyEvent.VK_LEFT) {
 			keys[0] = false;
 		} else if (key.getKeyCode() == KeyEvent.VK_RIGHT) {
 			keys[1] = false;
 		} else if (key.getKeyCode() == KeyEvent.VK_UP) {
 			keys[2] = false;
 		} else if (key.getKeyCode() == KeyEvent.VK_DOWN) {
 			keys[3] = false;
 		} else if (key.getKeyCode() == KeyEvent.VK_SPACE) {
 			keys[4] = false;
 		}
 	}
 
 	public void keyTyped(KeyEvent key) {}
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		e.getComponent().requestFocus();
	}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
 
 }
