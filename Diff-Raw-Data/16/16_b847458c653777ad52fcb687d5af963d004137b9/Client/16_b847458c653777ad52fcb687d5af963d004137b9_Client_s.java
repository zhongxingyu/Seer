 /* @formatter:off */
 /******************************************************************************
  * ELEC5616
  * Computer and Network Security, The University of Sydney
  * Copyright (C) 2002-2004, Matt Barrie, Stephen Gould and Ryan Junee
  *
  * PACKAGE:         StealthNet
  * FILENAME:        Client.java
  * AUTHORS:         Matt Barrie, Stephen Gould, Ryan Junee and Joshua Spence
  * DESCRIPTION:     Implementation of StealthNet Client for ELEC5616
  *                  programming assignment. Debug code has been added to this
  *                  class.
  *
  *****************************************************************************/
 /* @formatter:on */
 
 package StealthNet;
 
 /* Import Libraries ******************************************************** */
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FileDialog;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.Security;
 import java.util.Hashtable;
 import java.util.Stack;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.UIManager;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.text.DefaultCaret;
 
 import org.apache.commons.codec.binary.Base64;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 
 import StealthNet.Security.AsymmetricEncryption;
 import StealthNet.Security.AsymmetricVerification;
 import StealthNet.Security.EncryptedFileException;
 import StealthNet.Security.RSAAsymmetricEncryption;
 import StealthNet.Security.SHA1withRSAAsymmetricVerification;
 
 /* StealthNet.Client Class Definition ************************************** */
 
 /**
  * A client for the StealthNet chat program. Receives information about other
  * clients and secrets from a StealthNet {@link Server}.
  * 
  * <p> If the {@link Client} wants to start a chat session with a user, then the
  * source {@link Client} sends a command to the {@link Server}, containing an IP
  * address and port number on which the source {@link Client} is waiting to
  * accept a connection from the destination {@link Client}. The {@link Server}
  * relays this information to the destination {@link Client}, which should then
  * connect with the source {@link Client} to start the {@link Chat} session.
  * 
  * <p> Similarly, if the {@link Client} wants to send a file to another
  * {@link Client}, then the source {@link Client} sends a command to the
  * {@link Server}, containing an IP address and port number on which the source
  * {@link Client} is waiting to accept a connection from the destination
  * {@link Client}. The {@link Server} relays this information to the destination
  * {@link Client}, which should then connect with the source {@link Client} to
  * start the {link FileTransfer}.
  * 
  * <p> A client also communications with a StealthNet {@link Bank} in order to
  * withdraw funds from the user's bank account.
  * 
  * @author Matt Barrie
  * @author Stephen Gould
  * @author Ryan Junee
  * @author Joshua Spence
  */
 public class Client {
 	/* Debug options. */
 	private static final boolean DEBUG_GENERAL = Debug.isDebug("StealthNet.Client.General");
 	private static final boolean DEBUG_ERROR_TRACE = Debug.isDebug("StealthNet.Client.ErrorTrace") || Debug.isDebug("ErrorTrace");
 	private static final boolean DEBUG_COMMANDS_MSG = Debug.isDebug("StealthNet.Client.Commands.Msg");
 	private static final boolean DEBUG_COMMANDS_CHAT = Debug.isDebug("StealthNet.Client.Commands.Chat");
 	private static final boolean DEBUG_COMMANDS_FTP = Debug.isDebug("StealthNet.Client.Commands.FTP");
 	private static final boolean DEBUG_COMMANDS_LIST = Debug.isDebug("StealthNet.Client.Commands.List");
 	private static final boolean DEBUG_COMMANDS_SECRETLIST = Debug.isDebug("StealthNet.Client.Commands.SecretList");
 	private static final boolean DEBUG_COMMANDS_GETSECRET = Debug.isDebug("StealthNet.Client.Commands.GetSecret");
 	private static final boolean DEBUG_COMMANDS_GETPUBLICKEY = Debug.isDebug("StealthNet.Client.Commands.GetPublicKey");
 	private static final boolean DEBUG_COMMANDS_PAYMENT = Debug.isDebug("StealthNet.Client.Commands.Payment");
 	private static final boolean DEBUG_COMMANDS_REQUESTPAYMENT = Debug.isDebug("StealthNet.Client.Commands.RequestPayment");
 	private static final boolean DEBUG_COMMANDS_GETBALANCE = Debug.isDebug("StealthNet.Client.Commands.GetBalance");
 	private static final boolean DEBUG_COMMANDS_HASHCHAIN = Debug.isDebug("StealthNet.Client.Commands.HashChain");
 	private static final boolean DEBUG_ASYMMETRIC_ENCRYPTION = Debug.isDebug("StealthNet.Client.AsymmetricEncryption");
 	
 	/** The hostname of the StealthNet {@link Server}. */
 	private final String serverHostname;
 	
 	/** The port number of the StealthNet {@link Server}. */
 	private final int serverPort;
 	
 	/** The location of the server's {@link PublicKey} file. */
 	private static final String SERVER_PUBLIC_KEY_FILE = "keys/server/public.key";
 	
 	/** The hostname of the StealthNet {@link Bank}. */
 	private final String bankHostname;
 	
 	/** The port number of the StealthNet {@link Bank}. */
 	private final int bankPort;
 	
 	/** The location of the bank's {@link PublicKey} file. */
 	private static final String BANK_PUBLIC_KEY_FILE = "keys/bank/public.key";
 	
 	/** The main frame for this client. */
 	private static JFrame clientFrame;
 	
 	/** A text box used to display messages to the user. */
 	private JTextArea msgTextBox;
 	
 	/** A button to log into StealthNet. */
 	private JButton loginBtn;
 	
 	/** A button to withdraw credits from the bank. */
 	private JButton bankWithdrawBtn;
 	
 	/** A button to withdraw credits from the server. */
 	private JButton serverWithdrawBtn;
 	
 	/** To communicate with the StealthNet {@link Server}. */
 	private Comms serverComms = null;
 	
 	/** To communicate with the StealthNet {@link Bank}. */
 	private Comms bankComms = null;
 	
 	/**
 	 * An {@link AsymmetricVerification} class to verify that the {@link Bank}
 	 * signed the hash chain.
 	 */
 	private static AsymmetricVerification bankVerification = null;
 	
 	/** Public-private {@link KeyPair} to identify this client. */
 	private KeyPair clientKeys;
 	
 	/**
 	 * A {@link Timer} to periodically process incoming packets from the
 	 * {@link Bank}.
 	 */
 	private final Timer bankTimer;
 	
 	/**
 	 * A {@link Timer} to periodically process incoming packets from the
 	 * {@link Server}.
 	 */
 	private final Timer serverTimer;
 	
 	/** The ID of this (client) user. */
 	private String userID = null;
 	
 	/* Buddy list. */
 	private JTable buddyTable = null;
 	private DefaultTableModel buddyListData = null;
 	
 	/** List of secrets. */
 	private DefaultTableModel secretListData = null;
 	
 	/** Graphical representation of the secret list. */
 	private JTable secretTable = null;
 	
 	/** Field to show the remaining number of credits. */
 	JTextField creditsBox;
 	
 	/** Field to show the {@link Server} credit balance. */
 	JTextField serverBalanceBox;
 	
 	/** Field to show the {@link Bank} credit balance. */
 	JTextField bankBalanceBox;
 	
 	/** The current {@link CryptoCreditHashChain} in use for payment. */
 	private CryptoCreditHashChain hashChain = new CryptoCreditHashChain();
 	
 	/**
 	 * The number of credits to request from the {@link Bank} when generating a
 	 * new {@link CryptoCreditHashChain}. Note that if the user attempts to
 	 * purchase more expensive than the default length, then a
 	 * {@link CryptoCreditHashChain} will be generated with a length equal to
 	 * the cost of the secret.
 	 */
 	private static final int DEFAULT_HASHCHAIN_LENGTH = 100;
 	
 	/** Secret data for our secrets. */
 	private class SecretData {
 		String description = null;
 		String filename = null;
 	}
 	
 	/** A list of secret data, indexed by secret name. */
 	static private Hashtable<String, SecretData> secretDescriptions = new Hashtable<String, SecretData>();
 	
 	/* Use the BouncyCastle API. */
 	static {
 		Security.addProvider(new BouncyCastleProvider());
 	}
 	
 	/** Constructor. Sets up a timer to process packets. */
 	public Client() {
 		/* Create a timer to process packets from the bank every 100ms. */
 		bankTimer = new Timer(100, new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				/* Process incoming packets. */
 				processPackets(bankComms, bankTimer, bankBalanceBox);
 			}
 		});
 		
 		/* Create a timer to process packets from the server every 100ms. */
 		serverTimer = new Timer(100, new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				/* Process incoming packets. */
 				processPackets(serverComms, serverTimer, serverBalanceBox);
 			}
 		});
 		
 		serverHostname = Comms.DEFAULT_SERVERNAME;
 		serverPort = Comms.DEFAULT_SERVERPORT;
 		
 		bankHostname = Comms.DEFAULT_BANKNAME;
 		bankPort = Comms.DEFAULT_BANKPORT;
 	}
 	
 	/**
 	 * Constructor. Sets up a timer to process packets.
 	 * 
 	 * @param server The hostname of the StealthNet {@link Server}.
 	 * @param serverPort The port that the StealthNet {@link Server} is
 	 *        listening on.
 	 * @param bank The hostname of the StealthNet {@link Bank}.
 	 * @param bankPort The port that the StealthNet {@link Bank} is listening
 	 *        on.
 	 */
 	public Client(final String server, final int serverPort, final String bank, final int bankPort) {
 		/* Create a timer to process packets from the bank every 100ms. */
 		bankTimer = new Timer(100, new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				/* Process incoming packets. */
 				processPackets(bankComms, bankTimer, bankBalanceBox);
 			}
 		});
 		
 		/* Create a timer to process packets from the server every 100ms. */
 		serverTimer = new Timer(100, new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				/* Process incoming packets. */
 				processPackets(serverComms, serverTimer, serverBalanceBox);
 			}
 		});
 		
 		serverHostname = server;
 		this.serverPort = serverPort;
 		
 		bankHostname = bank;
 		this.bankPort = bankPort;
 	}
 	
 	/**
 	 * Create the GUI for the client instance.
 	 * 
 	 * @return An AWT component containing the client GUI.
 	 */
 	public Component createGUI() {
 		final JPanel pane = new JPanel();
 		
 		/* Create user list. */
 		buddyListData = new DefaultTableModel() {
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public boolean isCellEditable(final int row, final int col) {
 				return false;
 			};
 		};
 		buddyListData.addColumn("User ID");
 		buddyListData.addColumn("Online");
 		buddyTable = new JTable(buddyListData);
 		buddyTable.setPreferredScrollableViewportSize(new Dimension(200, 100));
 		buddyTable.getColumnModel().getColumn(0).setPreferredWidth(180);
 		
 		final JScrollPane buddyScrollPane = new JScrollPane(buddyTable);
 		buddyScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("User List"), BorderFactory.createEmptyBorder(0, 0, 0, 0)), buddyScrollPane.getBorder()));
 		
 		/* Add mouse listen for popup windows. Act on JTable row right-click. */
 		MouseListener ml = new MouseAdapter() {
 			private JPopupMenu popup;
 			private int row;
 			
 			@Override
 			public void mousePressed(final MouseEvent e) {
 				if (SwingUtilities.isRightMouseButton(e))
 					mouseReleased(e);
 			}
 			
 			@Override
 			public void mouseClicked(final MouseEvent e) {
 				if (SwingUtilities.isRightMouseButton(e))
 					mouseReleased(e);
 			}
 			
 			@Override
 			public void mouseReleased(final MouseEvent e) {
 				if (e.isShiftDown() || e.isControlDown() || e.isAltDown())
 					return;
 				
 				if (e.isPopupTrigger()) {
 					JMenuItem item;
 					
 					row = buddyTable.rowAtPoint(e.getPoint());
 					
 					popup = new JPopupMenu("Action");
 					popup.setLabel("Action");
 					
 					item = new JMenuItem("Chat");
 					item.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(final ActionEvent e) {
 							startChat(row);
 						}
 					});
 					popup.add(item);
 					
 					item = new JMenuItem("Send File");
 					item.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(final ActionEvent e) {
 							sendFile(row);
 						}
 					});
 					popup.add(item);
 					
 					popup.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 		};
 		buddyTable.addMouseListener(ml);
 		
 		/* Create secret window. */
 		secretListData = new DefaultTableModel() {
 			private static final long serialVersionUID = 1L;
 			
 			@Override
 			public boolean isCellEditable(final int row, final int col) {
 				return false;
 			};
 		};
 		secretListData.addColumn("Secret");
 		secretListData.addColumn("Cost");
 		
 		secretTable = new JTable(secretListData);
 		secretTable.setPreferredScrollableViewportSize(new Dimension(200, 100));
 		secretTable.getColumnModel().getColumn(0).setPreferredWidth(180);
 		
 		ml = new MouseAdapter() {
 			private JPopupMenu popup;
 			private int row;
 			
 			@Override
 			public void mousePressed(final MouseEvent e) {
 				if (SwingUtilities.isRightMouseButton(e))
 					mouseReleased(e);
 			}
 			
 			@Override
 			public void mouseClicked(final MouseEvent e) {
 				if (SwingUtilities.isRightMouseButton(e))
 					mouseReleased(e);
 			}
 			
 			@Override
 			public void mouseReleased(final MouseEvent e) {
 				if (e.isShiftDown() || e.isControlDown() || e.isAltDown())
 					return;
 				
 				if (e.isPopupTrigger()) {
 					JMenuItem item;
 					
 					row = buddyTable.rowAtPoint(e.getPoint());
 					
 					popup = new JPopupMenu("Action");
 					popup.setLabel("Action");
 					
 					item = new JMenuItem("Details");
 					item.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(final ActionEvent e) {
 							secretDetails(row);
 						}
 					});
 					popup.add(item);
 					
 					item = new JMenuItem("Purchase");
 					item.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(final ActionEvent e) {
 							purchaseSecret(row);
 						}
 					});
 					popup.add(item);
 					
 					popup.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 		};
 		secretTable.addMouseListener(ml);
 		
 		final JScrollPane secretScrollPane = new JScrollPane(secretTable);
 		secretScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Secrets List"), BorderFactory.createEmptyBorder(0, 0, 0, 0)), secretScrollPane.getBorder()));
 		
 		/* Create instant message window. */
 		msgTextBox = new JTextArea("Authentication required.\n");
 		msgTextBox.setLineWrap(true);
 		msgTextBox.setWrapStyleWord(true);
 		msgTextBox.setEditable(false);
 		final DefaultCaret caret = (DefaultCaret) msgTextBox.getCaret();
 		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
 		final JScrollPane msgScrollPane = new JScrollPane(msgTextBox);
 		msgScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		msgScrollPane.setPreferredSize(new Dimension(200, 100));
 		msgScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Console"), BorderFactory.createEmptyBorder(0, 0, 0, 0)), msgScrollPane.getBorder()));
 		
 		/** Create split pane for buddy list and messages. */
 		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buddyScrollPane, secretScrollPane);
 		splitPane.setOneTouchExpandable(true);
 		splitPane.setDividerLocation(150);
 		
 		final JSplitPane topPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, msgScrollPane);
 		topPane.setOneTouchExpandable(true);
 		
 		/* Credits display. */
 		final JPanel creditsPane = new JPanel();
 		creditsPane.setLayout(new GridLayout(3, 0));
 		
 		final JPanel bankBalanceSubpane = new JPanel();
 		bankBalanceSubpane.setLayout(new GridLayout(1, 0));
 		bankBalanceSubpane.setPreferredSize(new Dimension(180, 30));
 		bankBalanceSubpane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
 		bankBalanceSubpane.add(new JLabel("Bank:  ", SwingConstants.RIGHT));
 		bankBalanceBox = new JTextField();
 		bankBalanceBox.setEditable(false);
 		bankBalanceSubpane.add(bankBalanceBox);
 		creditsPane.add(bankBalanceSubpane);
 		
 		final JPanel creditsSubpane = new JPanel();
 		creditsSubpane.setLayout(new GridLayout(1, 0));
 		creditsSubpane.setPreferredSize(new Dimension(180, 30));
 		creditsSubpane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
 		creditsSubpane.add(new JLabel("Credits:  ", SwingConstants.RIGHT));
 		creditsBox = new JTextField();
 		creditsBox.setEditable(false);
 		creditsSubpane.add(creditsBox);
 		creditsPane.add(creditsSubpane);
 		
 		final JPanel serverBalanceSubpane = new JPanel();
 		serverBalanceSubpane.setLayout(new GridLayout(1, 0));
 		serverBalanceSubpane.setPreferredSize(new Dimension(180, 30));
 		serverBalanceSubpane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
 		serverBalanceSubpane.add(new JLabel("Server:  ", SwingConstants.RIGHT));
 		serverBalanceBox = new JTextField();
 		serverBalanceBox.setEditable(false);
 		serverBalanceSubpane.add(serverBalanceBox);
 		creditsPane.add(serverBalanceSubpane);
 		
 		/* Create buttons (login, send message, chat, ftp) */
 		loginBtn = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("img/login.gif")));
 		loginBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
 		loginBtn.setHorizontalTextPosition(SwingConstants.CENTER);
 		loginBtn.setMnemonic(KeyEvent.VK_N);
 		loginBtn.setToolTipText("Login");
 		loginBtn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				if (serverComms == null || bankComms == null)
 					login();
 				else
 					logout();
 			}
 		});
 		
 		final JButton msgBtn = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("img/msg.gif")));
 		msgBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
 		msgBtn.setHorizontalTextPosition(SwingConstants.CENTER);
 		msgBtn.setMnemonic(KeyEvent.VK_M);
 		msgBtn.setToolTipText("Create Secret");
 		msgBtn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				createSecret();
 			}
 		});
 		
 		bankWithdrawBtn = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("img/bankWithdraw.gif")));
 		bankWithdrawBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
 		bankWithdrawBtn.setHorizontalTextPosition(SwingConstants.CENTER);
 		bankWithdrawBtn.setMnemonic(KeyEvent.VK_B);
 		bankWithdrawBtn.setToolTipText("Withdraw from bank");
 		bankWithdrawBtn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				withdrawFromBank();
 			}
 		});
 		
 		serverWithdrawBtn = new JButton(new ImageIcon(this.getClass().getClassLoader().getResource("img/serverWithdraw.gif")));
 		serverWithdrawBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
 		serverWithdrawBtn.setHorizontalTextPosition(SwingConstants.CENTER);
 		serverWithdrawBtn.setMnemonic(KeyEvent.VK_S);
 		serverWithdrawBtn.setToolTipText("Withdraw from server");
 		serverWithdrawBtn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				withdrawFromServer();
 			}
 		});
 		
 		final JPanel btnPane = new JPanel();
 		btnPane.setLayout(new GridLayout(2, 0));
 		btnPane.setPreferredSize(new Dimension(180, 80));
 		btnPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
 		btnPane.add(loginBtn);
 		btnPane.add(msgBtn);
 		btnPane.add(bankWithdrawBtn);
 		btnPane.add(serverWithdrawBtn);
 		
 		final JPanel bottomPane = new JPanel();
 		bottomPane.setLayout(new BorderLayout());
 		bottomPane.add(creditsPane, BorderLayout.NORTH);
 		bottomPane.add(btnPane, BorderLayout.SOUTH);
 		
 		/* Create top-level panel and add components. */
 		pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
 		pane.setLayout(new BorderLayout());
 		pane.add(topPane, BorderLayout.NORTH);
 		pane.add(bottomPane, BorderLayout.SOUTH);
 		
 		return pane;
 	}
 	
 	/**
 	 * Login to StealthNet. This will establish {@link Comms} to the StealthNet
 	 * {@link Server} and to the StealthNet {@link Bank}. The communications
 	 * will be encrypted using the {@link Server}'s {@link PublicKey} and the
 	 * {@link Bank}'s {@link PublicKey} respectively. This means that if another
 	 * party is masquerading as the {@link Server} or as the {@link Bank}, then
 	 * (without the {link PrivateKey}s) they are unable to decrypt the packets.
 	 * 
 	 * <p> When a user attempts to login, the application will check if that
 	 * user already has a public-private {@link KeyPair}. If they do, then the
 	 * application will attempt to use the existing {@link KeyPair}, assuming
 	 * that the user provides the correct password to decrypt the
 	 * {@link PrivateKey} file.
 	 */
 	private synchronized void login() {
 		if (DEBUG_GENERAL)
 			System.out.println("Logging in to StealthNet.");
 		
 		if (serverComms != null && bankComms != null) {
 			System.err.println("Already logged in!");
 			msgTextBox.append("[*ERR*] Already logged in.\n");
 			return;
 		}
 		
 		AsymmetricEncryption serverEncryption = null;
 		AsymmetricEncryption bankEncryption = null;
 		
 		/* Prompt the user for the user ID. */
 		userID = JOptionPane.showInputDialog("Login:", userID);
 		if (userID == null)
 			return;
 		
 		/*
 		 * Construct the path to the public/private keys for this user. Note
 		 * that the keys may not exist yet.
 		 */
 		final String publicKeyPath = "keys/clients/" + userID + "/public.key";
 		final String privateKeyPath = "keys/clients/" + userID + "/private.key";
 		
 		do {
 			/*
 			 * Prompt the user for the password for the private key. If the
 			 * private key file exists, then this password will be used to
 			 * decrypt the file. If the private key file does not yet exist,
 			 * then this password will be used to encrypt the file once it is
 			 * generated.
 			 */
 			String password = null;
 			password = JOptionPane.showInputDialog("Password:", password);
 			if (password == null)
 				return;
 			
 			try {
 				/*
 				 * Retrieve the public/private key from the file system. If the
 				 * public/private keys don't exist then create new
 				 * public/private keys. Then enable asymmetric encryption to the
 				 * server and to the bank.
 				 */
 				clientKeys = Utility.getPublicPrivateKeys(publicKeyPath, privateKeyPath, password);
 				if (clientKeys == null) {
 					System.err.println("Unable to determine client keys.");
 					msgTextBox.append("[*ERR*] Unable to determine client keys.\n");
 					return;
 				}
 				serverEncryption = new RSAAsymmetricEncryption(clientKeys);
 				bankEncryption = new RSAAsymmetricEncryption(clientKeys);
 			} catch (final EncryptedFileException e) {
 				JOptionPane.showMessageDialog(null, "The password you entered was incorrect.", "Invalid password", JOptionPane.ERROR_MESSAGE);
 				continue;
 			} catch (final Exception e) {
 				System.err.println("Unable to determine client keys.");
 				if (DEBUG_ERROR_TRACE)
 					e.printStackTrace();
 				msgTextBox.append("[*ERR*] Unable to determine client keys.\n");
 				return;
 			}
 		} while (clientKeys == null);
 		
 		if (DEBUG_ASYMMETRIC_ENCRYPTION) {
 			final String publicKeyString = Utility.getHexValue(clientKeys.getPublic().getEncoded());
 			final String privateKeyString = Utility.getHexValue(clientKeys.getPrivate().getEncoded());
 			System.out.println("Public key: " + publicKeyString);
 			System.out.println("Private key: " + privateKeyString);
 		}
 		
 		/*
 		 * Set up asymmetric encryption for the server connection. Get server
 		 * public key from JAR file.
 		 */
 		try {
 			final PublicKey serverPublicKey = Utility.getPublicKey(SERVER_PUBLIC_KEY_FILE);
 			if (serverPublicKey == null) {
 				System.err.println("Unable to determine server public key.");
 				msgTextBox.append("[*ERR*] Unable to determine server public key.\n");
 				return;
 			}
 			serverEncryption.setPeerPublicKey(serverPublicKey);
 		} catch (final Exception e) {
 			System.err.println("Unable to set peer public key.");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			msgTextBox.append("[*ERR*] Error initialising connection to server.\n");
 			return;
 		}
 		
 		/*
 		 * Set up asymmetric encryption for the bank connection. Get bank public
 		 * key from JAR file.
 		 */
 		try {
 			final PublicKey bankPublicKey = Utility.getPublicKey(BANK_PUBLIC_KEY_FILE);
 			if (bankPublicKey == null) {
 				System.err.println("Unable to determine bank public key.");
 				msgTextBox.append("[*ERR*] Unable to determine bank public key.\n");
 				return;
 			}
 			bankEncryption.setPeerPublicKey(bankPublicKey);
 			bankVerification = new SHA1withRSAAsymmetricVerification(clientKeys, bankPublicKey);
 		} catch (final Exception e) {
 			System.err.println("Unable to set peer public key.");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			msgTextBox.append("[*ERR*] Error initialising connection to bank.\n");
 			return;
 		}
 		
 		/* Initiate a connection with the StealthNet server. */
 		try {
 			if (DEBUG_GENERAL)
 				System.out.println("Initiating a connection with StealthNet server \"" + serverHostname + "\" on port " + serverPort + ".");
 			serverComms = new Comms(serverEncryption);
 			serverComms.initiateSession(new Socket(serverHostname, serverPort));
 		} catch (final UnknownHostException e) {
 			System.err.println("Unknown host for StealthNet server: \"" + serverHostname + "\".");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			msgTextBox.append("[*ERR*] Unknown host: \"" + serverHostname + "\".\n");
 			return;
 		} catch (final IOException e) {
 			System.err.println("Could not connect to StealthNet server on port " + serverPort + ".");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			msgTextBox.append("[*ERR*] Could not connect to server on port " + serverPort + ".\n");
 			return;
 		}
 		
 		/* Initiate a connection with the StealthNet bank. */
 		try {
 			if (DEBUG_GENERAL)
 				System.out.println("Initiating a connection with StealthNet bank \"" + bankHostname + "\" on port " + bankPort + ".");
 			bankComms = new Comms(bankEncryption);
 			bankComms.initiateSession(new Socket(bankHostname, bankPort));
 		} catch (final UnknownHostException e) {
 			System.err.println("Unknown host for StealthNet bank: \"" + bankHostname + "\".");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			msgTextBox.append("[*ERR*] Unknown host: \"" + bankHostname + "\".\n");
 			return;
 		} catch (final IOException e) {
 			System.err.println("Could not connect to StealthNet bank on port " + bankPort + ".");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			msgTextBox.append("[*ERR*] Could not connect to bank on port " + bankPort + ".\n");
 			return;
 		}
 		
 		/* Send the server a login command. */
 		if (DEBUG_GENERAL)
 			System.out.println("Sending the server a login packet for user \"" + userID + "\".");
 		serverComms.sendPacket(DecryptedPacket.CMD_LOGIN, userID);
 		if (DEBUG_GENERAL)
 			System.out.println("Connected to StealthNet server.");
 		msgTextBox.append("[INFO] Connected to StealthNet server.\n");
 		
 		/* Request server balance. */
 		serverComms.sendPacket(DecryptedPacket.CMD_GETBALANCE);
 		
 		/* Send the bank a login command. */
 		if (DEBUG_GENERAL)
 			System.out.println("Sending the bank a login packet for user \"" + userID + "\".");
 		bankComms.sendPacket(DecryptedPacket.CMD_LOGIN, userID);
 		if (DEBUG_GENERAL)
 			System.out.println("Connected to StealthNet bank.");
 		msgTextBox.append("[INFO] Connected to StealthNet bank.\n");
 		
 		/* Request bank balance. */
 		bankComms.sendPacket(DecryptedPacket.CMD_GETBALANCE);
 		
 		/* Start periodically checking for packets. */
 		serverTimer.start();
 		bankTimer.start();
 		
 		/* =================================================================== */
 		/* NOTE: We should now be connected to the StealthNet server and bank. */
 		/* =================================================================== */
 		
 		/* Set the frame title. */
 		clientFrame.setTitle("stealthnet [" + userID + "]");
 		
 		/* Update the credits box. */
 		creditsBox.setText(Integer.toString(hashChain.getLength()));
 		
 		/* Change the login button to a logout button. */
 		loginBtn.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("img/logout.gif")));
 		loginBtn.setToolTipText("Logout");
 	}
 	
 	/**
 	 * Logout of StealthNet. This will send a <code>CMD_LOGOUT</code> packet to
 	 * both the StealthNet {@link Server} and the StealthNet {@link Bank}.
 	 */
 	private synchronized void logout() {
 		if (serverComms != null && bankComms != null) {
 			if (DEBUG_GENERAL)
 				System.out.println("Logging out of StealthNet.");
 			
 			if (serverComms != null) {
 				/* Stop periodically checking for packets. */
 				serverTimer.stop();
 				
 				/* Send the server a logout command. */
 				serverComms.sendPacket(DecryptedPacket.CMD_LOGOUT);
 				if (DEBUG_GENERAL)
 					System.out.println("Logged out of StealthNet server.");
 				msgTextBox.append("[INFO] Logged out of StealthNet server.\n");
 				
 				/* Terminate session. */
 				serverComms.terminateSession();
 				serverComms = null;
 				
 				/* Forget balance. */
 				serverBalanceBox.setText("");
 				
 				/* Hide user and secret list. */
 				buddyListData.setRowCount(0);
 				secretListData.setRowCount(0);
 				
 				if (DEBUG_GENERAL)
 					System.out.println("Disconnected from StealthNet server.");
 				msgTextBox.append("[INFO] Disconnected from StealthNet server.\n");
 			}
 			
 			if (bankComms != null) {
 				/* Stop periodically checking for packets. */
 				bankTimer.stop();
 				
 				/* Send the bank a logout command. */
 				bankComms.sendPacket(DecryptedPacket.CMD_LOGOUT);
 				msgTextBox.append("[INFO] Logged out of StealthNet bank.\n");
 				if (DEBUG_GENERAL)
 					System.out.println("Logged out of StealthNet bank.");
 				
 				/* Terminate session. */
 				bankComms.terminateSession();
 				bankComms = null;
 				
 				/* Forget balance. */
 				bankBalanceBox.setText("");
 				
 				msgTextBox.append("[INFO] Disconnected from StealthNet bank.\n");
 				if (DEBUG_GENERAL)
 					System.out.println("Disconnected from StealthNet bank.");
 			}
 			
 			/* Destroy the current hash chain. */
 			hashChain = new CryptoCreditHashChain();
 			creditsBox.setText("");
 			
 			/* Change the logout button back to a login button. */
 			loginBtn.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("img/login.gif")));
 			loginBtn.setToolTipText("Login");
 		}
 	}
 	
 	/**
 	 * Withdraw additional credits from the {@link Bank} and generate a new
 	 * (larger) {@link CryptoCreditHashChain}.
 	 */
 	private void withdrawFromBank() {
 		if (bankComms == null) {
 			msgTextBox.append("[*ERR*] Not logged in.\n");
 			return;
 		}
 		
 		/* Prompt the user for the number of credits to withdraw. */
 		String credits = null;
 		while (true) {
 			credits = JOptionPane.showInputDialog("Credits to withdraw:", credits);
 			
			if (credits.length() == 0)
 				return;
 			
 			try {
 				final int dummy = Integer.parseInt(credits);
 				
 				if (dummy < 0)
 					throw new NumberFormatException();
 				
 				break;
 			} catch (final NumberFormatException e) {
 				JOptionPane.showMessageDialog(null, "Credits must be a non-negative integer.", "Invalid credits", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		
 		/* Refund the unspent credits from the current hash chain. */
 		final int currentCredits = hashChain.getLength();
 		if (currentCredits > 0) {
 			final Stack<byte[]> payment = hashChain.getNextCredits(currentCredits);
 			bankComms.sendPacket(DecryptedPacket.CMD_PAYMENT, payment.size() + ";" + Base64.encodeBase64String(payment.peek()));
 		}
 		
 		/* Request new hash chain */
 		getNewHashChain(Integer.parseInt(credits) + currentCredits);
 	}
 	
 	/**
 	 * Withdraw additional credits from the {@link Server} and deposit them into
 	 * the {@link Bank}.
 	 */
 	private void withdrawFromServer() {
 		if (serverComms == null) {
 			msgTextBox.append("[*ERR*] Not logged in.\n");
 			return;
 		}
 		
 		/* Prompt the user for the number of credits to withdraw. */
 		String credits = null;
 		while (true) {
 			credits = JOptionPane.showInputDialog("Credits to withdraw:", credits);
 			
 			if (credits.length() == 0)
 				return;
 			
 			try {
 				final int dummy = Integer.parseInt(credits);
 				
 				if (dummy < 0)
 					throw new NumberFormatException();
 				
 				break;
 			} catch (final NumberFormatException e) {
 				JOptionPane.showMessageDialog(null, "Credits must be a non-negative integer.", "Invalid credits", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		
 		/* Request the credits from the server. */
 		if (DEBUG_COMMANDS_REQUESTPAYMENT)
 			System.out.println("Requesting withdrawal of " + credits + " credits from server.");
 		serverComms.sendPacket(DecryptedPacket.CMD_REQUESTPAYMENT, credits);
 	}
 	
 	/** Create a secret on the {@link Server}. */
 	private void createSecret() {
 		if (serverComms == null) {
 			msgTextBox.append("[*ERR*] Not logged in.\n");
 			return;
 		}
 		
 		if (DEBUG_GENERAL)
 			System.out.println("Creating a secret.");
 		
 		String name = "";
 		String description = "";
 		String cost = "";
 		
 		/* Prompt the user for the secret name, description and cost. */
 		name = JOptionPane.showInputDialog("Secret Name:", name);
 		description = JOptionPane.showInputDialog("Secret Description:", description);
 		while (true) {
 			cost = JOptionPane.showInputDialog("Secret Cost (credits):", cost);
 			if (cost.length() == 0)
 				return;
 			
 			try {
 				final int dummy = Integer.parseInt(cost);
 				if (dummy < 0)
 					throw new NumberFormatException();
 				break;
 			} catch (final NumberFormatException e) {
 				JOptionPane.showMessageDialog(null, "Cost must be a non-negative integer.", "Invalid cost", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		
 		/* Prompt the user for the secret file. */
 		final FileDialog fileOpen = new FileDialog(clientFrame, "Select Secret File....", FileDialog.LOAD);
 		fileOpen.setVisible(true);
 		if (fileOpen.getFile() == null || fileOpen.getFile().length() == 0)
 			return;
 		else if (!new File(fileOpen.getFile()).exists()) {
 			JOptionPane.showMessageDialog(null, "No such file exists.", "File not found.", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		
 		/* Create the secret on the server. */
 		final String secret = name + ";" + description + ";" + cost + ";" + fileOpen.getDirectory() + ";" + fileOpen.getFile();
 		if (secret != null) {
 			if (DEBUG_GENERAL)
 				System.out.println("Sending secret details to server. Secret name is \"" + name + "\". Secret cost is " + cost + ". Secret description is \"" + description + "\". Secret file is \"" + fileOpen.getDirectory() + fileOpen.getFile() + "\".");
 			serverComms.sendPacket(DecryptedPacket.CMD_CREATESECRET, secret);
 		}
 	}
 	
 	/**
 	 * Display details of a secret.
 	 * 
 	 * @param row The row of the secret to be displayed.
 	 */
 	private void secretDetails(final int row) {
 		final String name = (String) secretTable.getValueAt(row, 0);
 		final SecretData data = secretDescriptions.get(name);
 		if (data != null)
 			JOptionPane.showMessageDialog(null, data.description, "Details of Secret: " + name, JOptionPane.PLAIN_MESSAGE);
 	}
 	
 	/**
 	 * Purchase the details of a secret.
 	 * 
 	 * @param row The secret to be purchased.
 	 */
 	private void purchaseSecret(final int row) {
 		final String name = (String) secretTable.getValueAt(row, 0);
 		final SecretData data = secretDescriptions.get(name);
 		
 		if (DEBUG_GENERAL)
 			System.out.println("Attempting to purchase secret \"" + name + "\".");
 		
 		if (data == null)
 			return;
 		
 		/* Set up socket on a free port for file transfer of the secret file. */
 		ServerSocket ftpSocket = null;
 		try {
 			ftpSocket = new ServerSocket(0);
 		} catch (final IOException e) {
 			System.err.println("Could not set up listening port for file transfer.");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			msgTextBox.append("[*ERR*] Transfer failed.\n");
 			return;
 		}
 		
 		if (DEBUG_GENERAL)
 			System.out.println("Set up socket on port " + ftpSocket.getLocalPort() + " for transfer of secret file \"" + name + "\".");
 		
 		/* Discover our own IP address. */
 		String iAddr;
 		try {
 			iAddr = InetAddress.getLocalHost().toString();
 			if (iAddr.lastIndexOf("/") > 0)
 				iAddr = iAddr.substring(0, iAddr.lastIndexOf("/"));
 		} catch (final UnknownHostException e) {
 			iAddr = "localhost";
 		}
 		iAddr += ":" + Integer.toString(ftpSocket.getLocalPort());
 		
 		/*
 		 * Send the server the name of the secret and the IP address and port
 		 * number for the file transfer.
 		 */
 		if (DEBUG_GENERAL)
 			System.out.println("Sending get secret message to server. Target client should connect on \"" + iAddr + ":" + ftpSocket.getLocalPort() + "\".");
 		serverComms.sendPacket(DecryptedPacket.CMD_GETSECRET, name + "@" + iAddr);
 		
 		/*
 		 * Wait for the server to respond with the payment required to purchase
 		 * the secret. Note that if no payment is required, then the server
 		 * should explicitly send a request for zero payment.
 		 */
 		if (DEBUG_GENERAL)
 			System.out.println("Waiting for server to respond with payment required to purchase the secret.");
 		
 		boolean sufficientCredit = false;
 		while (!sufficientCredit)
 			try {
 				final DecryptedPacket pckt = serverComms.recvPacket();
 				
 				if (pckt == null) {
 					/* Clean up and return. */
 					ftpSocket.close();
 					return;
 				}
 				
 				switch (pckt.command) {
 /* @formatter:off */
 					/***********************************************************
 					 * Message command
 					 **********************************************************/
 /* @formatter:on */
 					case DecryptedPacket.CMD_MSG: {
 						final String msg = new String(pckt.data);
 						if (DEBUG_COMMANDS_MSG)
 							System.out.println("Received a message command. Message: \"" + msg + "\".");
 						msgTextBox.append(msg + "\n");
 						JOptionPane.showMessageDialog(null, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
 						return;
 					}
 					
 					/***********************************************************
 					 * Request Payment command
 					 **********************************************************/
 					case DecryptedPacket.CMD_REQUESTPAYMENT:
 						final String pcktData = new String(pckt.data);
 						final int amountRequested = Integer.parseInt(pcktData);
 						
 						if (amountRequested <= 0) {
 							if (DEBUG_COMMANDS_REQUESTPAYMENT)
 								System.out.println("There is sufficient credit on the server to pay for the purchase of the secret.");
 							sufficientCredit = true;
 							break;
 						} else {
 							if (DEBUG_COMMANDS_REQUESTPAYMENT)
 								System.out.println("The server requested a payment of " + amountRequested + " credits.");
 							
 							boolean sentPayment = false;
 							while (!sentPayment)
 								if (hashChain == null) {
 									/*
 									 * We don't have a hash chain... Generate a
 									 * new hash chain.
 									 */
 									if (DEBUG_COMMANDS_REQUESTPAYMENT)
 										System.out.println("No hash chain found. Generating a new hash chain.");
 									getNewHashChain(Math.max(DEFAULT_HASHCHAIN_LENGTH, amountRequested));
 									creditsBox.setText(Integer.toString(hashChain.getLength()));
 									
 									/*
 									 * If the hash chain is still null, then the
 									 * bank refused to sign the hash chain. Give
 									 * up on the file transfer.
 									 */
 									if (hashChain == null) {
 										if (DEBUG_GENERAL)
 											System.out.println("Unable to generate hash chain. Giving up on purchase of secret.");
 										serverComms.sendPacket(DecryptedPacket.CMD_PAYMENT, Integer.toString(0) + ";" + Base64.encodeBase64String(new byte[0]));
 										sentPayment = true;
 										
 										/* Clean up and return. */
 										ftpSocket.close();
 										return;
 									}
 								} else {
 									/*
 									 * Get amountRequested credits from the hash
 									 * chain. If there aren't enough available
 									 * credits on the hash chain then send all
 									 * available credits to the server and then
 									 * a new hash chain will be generated.
 									 */
 									final Stack<byte[]> payment = hashChain.getNextCredits(amountRequested);
 									creditsBox.setText(Integer.toString(hashChain.getLength()));
 									
 									if (payment != null && payment.size() > 0) {
 										if (DEBUG_COMMANDS_REQUESTPAYMENT)
 											System.out.println("Sending a payment of " + payment.size() + " credits to the server with hash \"" + Utility.getHexValue(payment.peek()) + "\".");
 										serverComms.sendPacket(DecryptedPacket.CMD_PAYMENT, payment.size() + ";" + Base64.encodeBase64String(payment.peek()));
 										sentPayment = true;
 									} else {
 										if (DEBUG_COMMANDS_REQUESTPAYMENT)
 											System.out.println("CryptoCredit hash chain is empty. Generating a new hash chain.");
 										getNewHashChain(Math.max(DEFAULT_HASHCHAIN_LENGTH, amountRequested));
 										creditsBox.setText(Integer.toString(hashChain.getLength()));
 										
 										/*
 										 * Check that hash chain is not null.
 										 * Hash chain should be null if the bank
 										 * refused to sign the new hash chain.
 										 * In this case we tell the server that
 										 * we are no longer interested in
 										 * purchasing the secret. The server
 										 * does NOT refund the money that we
 										 * have already transferred towards the
 										 * secret. This will stay in our server
 										 * account.
 										 */
 										if (hashChain == null) {
 											if (DEBUG_GENERAL)
 												System.out.println("Unable to generate hash chain. Giving up on purchase of secret.");
 											serverComms.sendPacket(DecryptedPacket.CMD_PAYMENT, Integer.toString(0) + ";" + Base64.encodeBase64String(new byte[0]));
 											sentPayment = true;
 											
 											/* Clean up and return. */
 											ftpSocket.close();
 											return;
 										}
 									}
 								}
 						}
 						
 						break;
 					
 					/***********************************************************
 					 * Get Balance command
 					 **********************************************************/
 					case DecryptedPacket.CMD_GETBALANCE:
 						if (DEBUG_COMMANDS_GETBALANCE)
 							System.out.println("Received account balance from server.");
 						serverBalanceBox.setText(Integer.toString(Integer.parseInt(new String(pckt.data))));
 						break;
 					
 					/***********************************************************
 					 * Other command
 					 **********************************************************/
 					default:
						System.err.println("Unrecognised or unexpected command received from server.");
 						
 				}
 			} catch (final Exception e) {
 				System.err.println("Error reading packet. Discarding...");
 				if (DEBUG_ERROR_TRACE)
 					e.printStackTrace();
 			}
 		
 		if (DEBUG_GENERAL)
 			System.out.println("Purchased secret \"" + name + "\".");
 		msgTextBox.append("[INFO] Purchased secret \"" + name + "\".\n");
 		
 		/* Choose where to save the secret file. */
 		final FileDialog fileSave = new FileDialog(clientFrame, "Save As...", FileDialog.SAVE);
 		fileSave.setFile(data.filename);
 		fileSave.setVisible(true);
 		
 		/*
 		 * Note that we don't yet have the public key of the owner of the
 		 * secret. They will, however, have our public key and so can send us
 		 * their public key in encrypted form.
 		 */
 		
 		if (fileSave.getFile() != null && fileSave.getFile().length() > 0) {
 			if (DEBUG_GENERAL)
 				System.out.println("Will save secret file \"" + name + "\" to \"" + fileSave.getDirectory() + fileSave.getFile() + "\".");
 			
 			/* Wait for user to connect, then start file transfer. */
 			try {
 				if (DEBUG_GENERAL)
 					System.out.println("Waiting for target client to connect for file transfer.");
 				
 				/* Set a 2 second timeout on the socket. */
 				ftpSocket.setSoTimeout(2000);
 				final Comms snComms = new Comms(new RSAAsymmetricEncryption(clientKeys), true);
 				final Socket conn = ftpSocket.accept();
 				snComms.acceptSession(conn);
 				
 				if (DEBUG_GENERAL)
 					System.out.println("Accepted connection from \"" + conn.getInetAddress() + ":" + conn.getPort() + "\" for transfer of secret.");
 				final FileTransfer ft = new FileTransfer(snComms, fileSave.getDirectory() + fileSave.getFile(), false);
 				ft.start();
 				if (DEBUG_GENERAL)
 					System.out.println("Started an FTP session with \"" + iAddr + "\".");
 			} catch (final Exception e) {
 				System.err.println("Transfer failed.");
 				msgTextBox.append("[*ERR*] Transfer failed.\n");
 				if (DEBUG_ERROR_TRACE)
 					e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Check if we are able to send a message to a specified user.
 	 * 
 	 * @param row The user to check.
 	 * @return True if we are able to chat with the specified user, otherwise
 	 *         false.
 	 */
 	private boolean isOKtoSendtoRow(final int row) {
 		final String myid = (String) buddyTable.getValueAt(row, 0);
 		final String mystatus = (String) buddyTable.getValueAt(row, 1);
 		
 		/* Make sure we aren't trying to send a file to our self. */
 		if (myid.equals(userID)) {
 			System.err.println("Can't send to self.");
 			msgTextBox.append("[*ERR*] Can't send to self.\n");
 			return false;
 		}
 		
 		/* Check if the user is logged in. */
 		if (mystatus.equals("false")) {
 			System.err.println("User \"" + myid + "\" is not online.");
 			msgTextBox.append("[*ERR*] User is not online.\n");
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Start a chat session with the selected user.
 	 * 
 	 * @param row The user to chat with.
 	 */
 	private void startChat(final int row) {
 		if (!isOKtoSendtoRow(row))
 			return;
 		
 		/* Get the ID of the target user. */
 		final String myid = buddyTable.getValueAt(row, 0).toString().trim();
 		
 		/* Set up socket on a free port for the chat session. */
 		ServerSocket chatSocket = null;
 		try {
 			chatSocket = new ServerSocket(0);
 		} catch (final IOException e) {
 			System.err.println("Chat failed. Failed to create ServerSocket.");
 			msgTextBox.append("[*ERR*] Chat failed.\n");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			return;
 		}
 		
 		if (DEBUG_GENERAL)
 			System.out.println("Set up socket on port " + chatSocket.getLocalPort() + " for chat session with \"" + myid + "\".");
 		
 		/*
 		 * Send message to server with target user and listening address and
 		 * port for the chat session.
 		 */
 		String iAddr;
 		try {
 			iAddr = InetAddress.getLocalHost().toString();
 			if (iAddr.lastIndexOf("/") > 0)
 				iAddr = iAddr.substring(0, iAddr.lastIndexOf("/"));
 		} catch (final UnknownHostException e) {
 			iAddr = "localhost";
 		}
 		iAddr += ":" + Integer.toString(chatSocket.getLocalPort());
 		
 		/* Request the public key of the other client from the server. */
 		final PublicKey peer = requestPublicKey(myid);
 		if (peer == null) {
 			System.err.println("Unable to determine peer public key.");
 			msgTextBox.append("[*ERR*] Unable to determine peer public key.\n");
 			return;
 		}
 		
 		/*
 		 * Send the server the name of the user that we wish to chat to, as well
 		 * as the IP address and port number for the chat session.
 		 */
 		if (DEBUG_GENERAL)
 			System.out.println("Sending chat message to server. Target client should connect on \"" + iAddr + ":" + chatSocket.getLocalPort() + "\".");
 		serverComms.sendPacket(DecryptedPacket.CMD_CHAT, myid + "@" + iAddr);
 		
 		/* Wait for user to connect and open chat window. */
 		try {
 			if (DEBUG_GENERAL)
 				System.out.println("Waiting for target client to connect for chat session.");
 			
 			/* Set 2 second timeout on socket. */
 			chatSocket.setSoTimeout(2000);
 			
 			/*
 			 * Create communications to the peer. Note that the peer will have
 			 * our public key, and hence can encrypt the communications
 			 * immediately, using asymmetric encryption with our public key.
 			 */
 			final Comms snComms = new Comms(new RSAAsymmetricEncryption(clientKeys, peer), true);
 			final Socket conn = chatSocket.accept();
 			snComms.acceptSession(conn);
 			
 			if (DEBUG_GENERAL)
 				System.out.println("Accepted connection from \"" + conn.getInetAddress() + ":" + conn.getPort() + "\" for chat session.");
 			final Chat chat = new Chat(userID, snComms);
 			chat.start();
 			if (DEBUG_GENERAL)
 				System.out.println("Started a chat session with \"" + iAddr + "\".");
 		} catch (final SocketTimeoutException ste) {
 			System.err.println("Chat failed... operation timed out.");
 			if (DEBUG_ERROR_TRACE)
 				ste.printStackTrace();
 			msgTextBox.append("[*ERR*] Chat timed out.\n");
 			try {
 				chatSocket.close();
 			} catch (final Exception e) {
 				System.err.println("Failed to close chat socket.");
 				if (DEBUG_ERROR_TRACE)
 					e.printStackTrace();
 			}
 		} catch (final Exception e) {
 			System.err.println("Chat failed.");
 			msgTextBox.append("[*ERR*] Chat failed.\n");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Send a file to the selected user.
 	 * 
 	 * @param row The user to send the file to.
 	 */
 	private void sendFile(final int row) {
 		if (!isOKtoSendtoRow(row))
 			return;
 		
 		/* Get the user ID. */
 		final String myid = (String) buddyTable.getValueAt(row, 0);
 		
 		/* Get the public key of the other client. */
 		final PublicKey peer = requestPublicKey(myid);
 		if (peer == null) {
 			System.err.println("Unable to determine peer public key.");
 			msgTextBox.append("[*ERR*] Unable to determine details of user \"" + myid + "\".");
 			return;
 		}
 		
 		/* Select the file to send. */
 		final FileDialog fileOpen = new FileDialog(clientFrame, "Open...", FileDialog.LOAD);
 		fileOpen.setVisible(true);
 		if (fileOpen.getFile().length() == 0)
 			return;
 		
 		/* Set up socket on a free port. */
 		ServerSocket ftpSocket = null;
 		try {
 			ftpSocket = new ServerSocket(0);
 		} catch (final IOException e) {
 			System.err.println("Could not set up listening port.");
 			msgTextBox.append("[*ERR*] FTP failed.\n");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			return;
 		}
 		
 		if (DEBUG_GENERAL)
 			System.out.println("Set up socket on port " + ftpSocket.getLocalPort() + " for transfer of file \"" + fileOpen.getFile() + "\" to \"" + myid + "\".");
 		
 		/*
 		 * Send message to server with target user and listening address and
 		 * port for file transfer.
 		 */
 		String iAddr;
 		try {
 			iAddr = InetAddress.getLocalHost().toString();
 			if (iAddr.lastIndexOf("/") > 0)
 				iAddr = iAddr.substring(0, iAddr.lastIndexOf("/"));
 		} catch (final UnknownHostException e) {
 			iAddr = "localhost";
 		}
 		iAddr += ":" + Integer.toString(ftpSocket.getLocalPort());
 		
 		if (DEBUG_GENERAL)
 			System.out.println("Sending FTP message to server. Target client should connect on \"" + iAddr + ":" + ftpSocket.getLocalPort() + "\".");
 		serverComms.sendPacket(DecryptedPacket.CMD_FTP, myid + "@" + iAddr + "#" + fileOpen.getFile());
 		
 		/* Wait for user to connect, then start file transfer. */
 		try {
 			if (DEBUG_GENERAL)
 				System.out.println("Waiting for target client to connect for file transfer.");
 			
 			/* Set 2 second timeout on socket. */
 			ftpSocket.setSoTimeout(2000);
 			
 			/*
 			 * Create communications to the peer. Note that the peer will have
 			 * our public key, and hence can encrypt the communications
 			 * immediately, using asymmetric encryption with our public key.
 			 */
 			final Comms snComms = new Comms(new RSAAsymmetricEncryption(clientKeys, peer), true);
 			final Socket conn = ftpSocket.accept();
 			snComms.acceptSession(conn);
 			
 			if (DEBUG_GENERAL)
 				System.out.println("Accepted connection from \"" + conn.getInetAddress() + ":" + conn.getPort() + "\" for FTP transfer.");
 			final FileTransfer ft = new FileTransfer(snComms, fileOpen.getDirectory() + fileOpen.getFile(), true);
 			ft.start();
 			if (DEBUG_GENERAL)
 				System.out.println("Started an FTP session with \"" + iAddr + "\".");
 		} catch (final SocketTimeoutException ste) {
 			System.err.println("File transfer failed... operation timed out.");
 			if (DEBUG_ERROR_TRACE)
 				ste.printStackTrace();
 			msgTextBox.append("[*ERR*] File transfer timed out.\n");
 			try {
 				ftpSocket.close();
 			} catch (final Exception e) {
 				System.err.println("Failed to close FTP socket.");
 				if (DEBUG_ERROR_TRACE)
 					e.printStackTrace();
 			}
 		} catch (final Exception e) {
 			System.err.println("FTP failed.");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			msgTextBox.append("[*ERR*] FTP failed.\n");
 		}
 	}
 	
 	/**
 	 * Request the {@link PublicKey} of a peer client from the {@link Server}.
 	 * Blocks and discards packets until the {@link Server} sends a
 	 * <code>CMD_GETPUBLICKEY</code> packet.
 	 * 
 	 * @param id The ID of the requested user.
 	 * @return The {@link PublicKey} belonging to the requested user, or null if
 	 *         it could not determined.
 	 */
 	private PublicKey requestPublicKey(final String id) {
 		/* Request the public key of the peer. */
 		if (DEBUG_GENERAL)
 			System.out.println("Requesting the public key of user \"" + id + "\" from server.");
 		serverComms.sendPacket(DecryptedPacket.CMD_GETPUBLICKEY, id);
 		
 		/* Wait for server to send the public key of the other party. */
 		return waitForPublicKey();
 	}
 	
 	/**
 	 * Wait for the {@link Server} to send a {@link PublicKey}. Blocks and
 	 * discards packets until the {@link Server} sends a
 	 * <code>CMD_GETPUBLICKEY</code> packet.
 	 * 
 	 * @return The {@link PublicKey} sent by the {@link Server}.
 	 */
 	private PublicKey waitForPublicKey() {
 		if (DEBUG_COMMANDS_GETPUBLICKEY)
 			System.out.println("Waiting for the server to send a public key.");
 		
 		while (true)
 			try {
 				final DecryptedPacket pckt = serverComms.recvPacket();
 				
 				if (pckt == null)
 					return null;
 				
 				switch (pckt.command) {
 /* @formatter:off */
 					/***********************************************************
 					 * Get public key command
 					 **********************************************************/
 /* @formatter:on */
 					case DecryptedPacket.CMD_GETPUBLICKEY:
 						/* Convert packet contents to public key. */
 						return RSAAsymmetricEncryption.stringToPublicKey(Base64.decodeBase64(pckt.data));
 						
 /* @formatter:off */
 					/***********************************************************
 					 * Other command
 					 **********************************************************/
 /* @formatter:on */
 					default:
						System.err.println("Unrecognised or unexpected command received from server.");
 				}
 			} catch (final Exception e) {
 				System.err.println("Error running client thread.");
 				if (DEBUG_ERROR_TRACE)
 					e.printStackTrace();
 			}
 	}
 	
 	/**
 	 * Generates a new {@link CryptoCreditHashChain} and requests that the
 	 * {@link Bank} signs the new {@link CryptoCreditHashChain}. Finally, sends
 	 * the new {@link CryptoCreditHashChain} and the new signature to the
 	 * {@link Server}.
 	 * 
 	 * <p> Once this has completed, we can proceed to make purchases with the
 	 * hash chain.
 	 * 
 	 * @param length The length of the new {@link CryptoCreditHashChain}.
 	 */
 	private void getNewHashChain(int length) {
 		/*
 		 * Don't try to generate a hash chain with more credits than we have
 		 * available.
 		 */
 		bankComms.sendPacket(DecryptedPacket.CMD_GETBALANCE);
 		final int balance = waitForBalance(bankComms);
 		if (length > balance)
 			length = balance;
 		
 		if (DEBUG_GENERAL)
 			System.out.println("Generating a new hash chain of size " + length + ".");
 		
 		hashChain = new CryptoCreditHashChain(userID, length);
 		creditsBox.setText(Integer.toString(hashChain.getLength()));
 		
 		/* Get the bank to sign the hash chain. */
 		final byte[] identifier = hashChain.getIdentifier();
 		if (DEBUG_GENERAL)
 			System.out.println("Requesting that the bank sign the new CryptoCreditHashChain with identifier \"" + Utility.getHexValue(identifier) + "\".");
 		if (!hashChain.getSigned(bankComms)) {
 			/* The bank refused to sign the hash chain. */
 			System.err.println("Bank refused to sign hash chain. Insufficient funds in account.");
 			JOptionPane.showMessageDialog(null, "Bank refused to sign CryptoCredit hash chain. Insufficient funds in account.", "Insufficient funds", JOptionPane.ERROR_MESSAGE);
 			hashChain = null;
 			return;
 		}
 		final byte[] signature = hashChain.getSignature();
 		
 		if (DEBUG_GENERAL) {
 			System.out.println("Generated new hash chain of length " + length + ".");
 			System.out.println("Hash chain identifier: \"" + Utility.getHexValue(identifier) + "\".");
 			System.out.println("Hash chain signature: \"" + Utility.getHexValue(signature) + "\".");
 		}
 		
 		/* Receive new balance from bank. */
 		bankComms.sendPacket(DecryptedPacket.CMD_GETBALANCE);
 		
 		/*
 		 * Send the identifier and the signature of the new hash chain to the
 		 * server.
 		 */
 		if (DEBUG_COMMANDS_HASHCHAIN)
 			System.out.println("Sending the identifier and signature of the new hash chain to the server.");
 		serverComms.sendPacket(DecryptedPacket.CMD_HASHCHAIN, Base64.encodeBase64(hashChain.getIdentifierAndSignture()));
 		
 	}
 	
 	/**
 	 * Wait for the {@link Bank} or the {@link Server} to send the account
 	 * balance.
 	 * 
 	 * @param comms The {@link Comms} instance to wait for the account balance
 	 *        on.
 	 * @return The client's account balance.
 	 */
 	private static int waitForBalance(final Comms comms) {
 		while (true)
 			try {
 				final DecryptedPacket pckt = comms.recvPacket();
 				
 				switch (pckt.command) {
 /* @formatter:off */
 					/***********************************************************
 					 * Get Balance command
 					 **********************************************************/
 /* @formatter:on */
 					case DecryptedPacket.CMD_GETBALANCE:
 						return Integer.parseInt(new String(pckt.data));
 						
 /* @formatter:off */
 					/***********************************************************
 					 * Other command
 					 **********************************************************/
 /* @formatter:on */
 					default:
						System.err.println("Unrecognised or unexpected command received from server.");
 						
 				}
 			} catch (final Exception e) {
 				System.err.println("Error running client thread.");
 				if (DEBUG_ERROR_TRACE)
 					e.printStackTrace();
 			}
 	}
 	
 	/**
 	 * Process incoming packets.
 	 * 
 	 * @param comms The {@link Comms} class on which to receive a packet.
 	 * @param timer The {@link Timer} that activated the function call.
 	 * @param balanceField The {@link JTextField} to be updated if a
 	 *        <code>CMD_GETBALANCE</code> packet is received.
 	 */
 	private void processPackets(final Comms comms, final Timer timer, final JTextField balanceField) {
 		try {
 			if (comms == null || !comms.recvReady())
 				return;
 		} catch (final IOException e) {
 			System.err.println("The host appears to be down.");
 			msgTextBox.append("[*ERR*] The host appears to be down.\n");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			return;
 		}
 		
 		/* No need to process packets while we are already processing packets. */
 		timer.stop();
 		
 		try {
 			/* Check for packets from the server. */
 			while (comms.recvReady()) {
 				final DecryptedPacket pckt = comms.recvPacket();
 				
 				if (pckt == null)
 					break;
 				
 				switch (pckt.command) {
 /* @formatter:off */
 					/***********************************************************
 					 * Message command
 					 **********************************************************/
 /* @formatter:on */
 					case DecryptedPacket.CMD_MSG: {
 						final String msg = new String(pckt.data);
 						if (DEBUG_COMMANDS_MSG)
 							System.out.println("Received a message command. Message: \"" + msg + "\".");
 						msgTextBox.append(msg + "\n");
 						JOptionPane.showMessageDialog(null, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
 						break;
 					}
 					
 					/***********************************************************
 					 * Chat command
 					 **********************************************************/
 					case DecryptedPacket.CMD_CHAT: {
 						final String data = new String(pckt.data);
 						final String iAddr = data.split("@")[1].split(":")[0];
 						final int iPort = Integer.parseInt(data.split("@")[1].split(":")[1]);
 						
 						/* Get the name of the user requesting the chat session. */
 						final String sourceUser = data.split("@")[0];
 						
 						/*
 						 * Request the public key of the user initiating the
 						 * chat session from the server.
 						 */
 						final PublicKey peer = requestPublicKey(sourceUser);
 						if (peer == null) {
 							System.err.println("Unable to determine peer public key.");
 							msgTextBox.append("[*ERR*] Failed to start chat session with user \"" + sourceUser + "\".\n");
 							break;
 						}
 						
 						if (DEBUG_COMMANDS_CHAT)
 							System.out.println("Received a chat command. Target host: \"" + iAddr + ":" + iPort + "\". Public key of peer is \"" + Utility.getHexValue(peer.getEncoded()) + "\".");
 						
 						/*
 						 * Create communications to the peer. Note that the peer
 						 * will already have our public key (as the server
 						 * would've sent it on forwarding the chat packet), and
 						 * hence can encrypt the communications immediately,
 						 * using asymmetric encryption with our public key.
 						 */
 						final Comms snComms = new Comms(new RSAAsymmetricEncryption(clientKeys, peer), true);
 						snComms.initiateSession(new Socket(iAddr, iPort));
 						if (DEBUG_GENERAL)
 							System.out.println("Opened a communications session with \"" + iAddr + "\".");
 						
 						final Chat chat = new Chat(userID, snComms);
 						chat.start();
 						if (DEBUG_GENERAL)
 							System.out.println("Started a chat session with \"" + iAddr + "\".");
 						break;
 					}
 					
 					/***********************************************************
 					 * FTP command
 					 **********************************************************/
 					case DecryptedPacket.CMD_FTP: {
 						final String data = new String(pckt.data);
 						final String fName = data.split("@")[1].split("#")[1];
 						final String iAddr = data.split("@")[1].split("#")[0].split(":")[0];
 						final int iPort = Integer.parseInt(data.split("@")[1].split("#")[0].split(":")[1]);
 						final String sourceUser = data.split("@")[0];
 						
 						/*
 						 * Request the public key of the user initiating the
 						 * file transfer from the server.
 						 */
 						final PublicKey peer = requestPublicKey(sourceUser);
 						if (peer == null) {
 							System.err.println("Unable to determine peer public key.");
 							msgTextBox.append("[*ERR*] Failed to start file transfer with user \"" + sourceUser + "\".\n");
 							return;
 						}
 						
 						if (DEBUG_COMMANDS_FTP)
 							System.out.println("Received a file transfer command. Target host: \"" + iAddr + ":" + iPort + "\". Public key of peer is \"" + Utility.getHexValue(peer.getEncoded()) + "\".");
 						
 						/*
 						 * Create communications to the peer. Note that the peer
 						 * will already have our public key (as the server
 						 * would've sent it on forwarding the FTP packet), and
 						 * hence can encrypt the communications immediately,
 						 * using asymmetric encryption with our public key.
 						 */
 						final Comms snComms = new Comms(new RSAAsymmetricEncryption(clientKeys, peer), true);
 						snComms.initiateSession(new Socket(iAddr, iPort));
 						if (DEBUG_GENERAL)
 							System.out.println("Opened a communications session with \"" + iAddr + "\".");
 						
 						final FileDialog fileSave = new FileDialog(clientFrame, "Save As...", FileDialog.SAVE);
 						fileSave.setFile(fName);
 						fileSave.setVisible(true);
 						if (fileSave.getFile() != null && fileSave.getFile().length() > 0) {
 							if (DEBUG_GENERAL)
 								System.out.println("File will be saved to \"" + fileSave.getDirectory() + fileSave.getFile() + "\". Starting file transfer.");
 							final FileTransfer ft = new FileTransfer(snComms, fileSave.getDirectory() + fileSave.getFile(), false);
 							ft.start();
 							if (DEBUG_GENERAL)
 								System.out.println("Started an FTP session with \"" + iAddr + "\".");
 						}
 						break;
 					}
 					
 					/***********************************************************
 					 * List command
 					 **********************************************************/
 					case DecryptedPacket.CMD_LIST: {
 						/*
 						 * Replace the contents of the buddy list with the data
 						 * received from the server.
 						 */
 						String userTable = new String(pckt.data);
 						buddyListData.setRowCount(0);
 						
 						if (DEBUG_COMMANDS_LIST)
 							System.out.println("Received a user list: \"" + userTable.replaceAll("\n", "; ") + "\".");
 						
 						/*
 						 * Recreate the user list with the user list received
 						 * from the server.
 						 */
 						while (userTable.length() > 0) {
 							final int indx = userTable.indexOf("\n");
 							String row;
 							
 							if (indx > 0) {
 								row = userTable.substring(0, indx);
 								userTable = userTable.substring(indx + 1);
 							} else {
 								row = userTable;
 								userTable = "";
 							}
 							
 							/* Add the user to the user list. */
 							final String userID = row.split(";")[0].trim();
 							final boolean online = row.split(";")[1].trim().compareTo("true") == 0;
 							
 							/* Add the user to the GUI list. */
 							buddyListData.addRow(new Object[] { userID, online ? "true" : "false" });
 						}
 						break;
 					}
 					
 					/***********************************************************
 					 * Secret List command
 					 **********************************************************/
 					case DecryptedPacket.CMD_SECRETLIST: {
 						/*
 						 * Replace the contents of the graphical secret list
 						 * with the data received from the server.
 						 */
 						String secretTable = new String(pckt.data);
 						secretListData.setRowCount(0);
 						
 						if (DEBUG_COMMANDS_SECRETLIST)
 							System.out.println("Received a secret list: \"" + secretTable.replaceAll("\n", "; ") + "\".");
 						
 						/*
 						 * Recreate the user list with the user list received
 						 * from the server.
 						 */
 						while (secretTable.length() > 0) {
 							final int indx = secretTable.indexOf("\n");
 							String row;
 							
 							if (indx > 0) {
 								row = secretTable.substring(0, indx);
 								secretTable = secretTable.substring(indx + 1);
 							} else {
 								row = secretTable;
 								secretTable = "";
 							}
 							
 							final String values[] = row.split(";");
 							secretListData.addRow(values);
 							
 							final SecretData data = new SecretData();
 							data.description = values[2];
 							data.filename = values[3];
 							secretDescriptions.put(values[0], data);
 						}
 						break;
 					}
 					
 					/***********************************************************
 					 * Get Secret command
 					 **********************************************************/
 					case DecryptedPacket.CMD_GETSECRET: {
 						final String data = new String(pckt.data);
 						final String fileName = data.split("@")[0];
 						final String iAddr = data.split("@")[1].split(":")[0];
 						final int iPort = Integer.parseInt(data.split("@")[1].split(":")[1]);
 						
 						if (DEBUG_COMMANDS_GETSECRET)
 							System.out.println("Received a get secret command. Target host: \"" + iAddr + ":" + iPort + "\". The filename is \"" + fileName + "\".");
 						
 						/*
 						 * Wait for the server to send the public key of the
 						 * user requesting the secret.
 						 * 
 						 * Note that the user requesting the secret will NOT
 						 * have our public key. We will transmit it to then when
 						 * we begin the communications.
 						 */
 						final PublicKey receiver = waitForPublicKey();
 						
 						final Comms snComms = new Comms(new RSAAsymmetricEncryption(clientKeys, receiver), false);
 						snComms.initiateSession(new Socket(iAddr, iPort));
 						if (DEBUG_GENERAL)
 							System.out.println("Opened a communications session with \"" + iAddr + "\".");
 						
 						if (DEBUG_GENERAL)
 							System.out.println("Starting file transfer.");
 						msgTextBox.append("[INFO] Sending out a secret.\n");
 						final FileTransfer ft = new FileTransfer(snComms, fileName, true);
 						
 						/* Start the file transfer. */
 						ft.start();
 						if (DEBUG_GENERAL)
 							System.out.println("Started an FTP session with \"" + iAddr + "\".");
 						break;
 					}
 					
 					/***********************************************************
 					 * Get Balance command
 					 **********************************************************/
 					case DecryptedPacket.CMD_GETBALANCE: {
 						if (DEBUG_COMMANDS_GETBALANCE)
 							System.out.println("Received a get balance command.");
 						balanceField.setText(Integer.toString(Integer.parseInt(new String(pckt.data))));
 						break;
 					}
 					
 					/*******************************************************
 					 * Payment command
 					 ******************************************************/
 					case DecryptedPacket.CMD_PAYMENT: {
						/* Receieve a withdrawal from the server. */
 						if (comms.equals(serverComms)) {
 							final String data = new String(pckt.data);
 							final int creditsSent = Integer.parseInt(data.split(";")[0]);
 							byte[] cryptoCreditHash;
 							if (data.split(";").length < 2)
 								cryptoCreditHash = null;
 							else
 								cryptoCreditHash = Base64.decodeBase64(data.split(";")[1]);
 							
 							if (DEBUG_COMMANDS_PAYMENT)
 								System.out.println("Received withdrawal of " + creditsSent + " credits from server with hash \"" + Utility.getHexValue(cryptoCreditHash) + "\".");
 							CryptoCreditHashChain.processPayment(bankComms, Utility.getServerUserName(serverComms.getPeerPublicKey()), creditsSent, cryptoCreditHash);
 							break;
 						}
 					}
 					
 					/*******************************************************
 					 * Hashchain command
 					 ******************************************************/
 					case DecryptedPacket.CMD_HASHCHAIN: {
 						CryptoCreditHashChain.verifyHashChain(bankVerification, Base64.decodeBase64(pckt.data));
 						break;
 					}
 					
 					/***********************************************************
 					 * Other command
 					 **********************************************************/
 					default:
 						System.err.println("Unrecognised or unexpected command received.");
 				}
 			}
 		} catch (final Exception e) {
 			System.err.println("Error running client thread.");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 		}
 		
 		/* Start processing packets again. */
 		timer.start();
 	}
 	
 	/**
 	 * Main client function to execute.
 	 * 
 	 * @param args The command line arguments. The command lines arguments take
 	 *        the following format. <ul> <li> <code>args[0]</code> ::=
 	 *        <code>serverHostname:serverPort</code> </li> <li>
 	 *        <code>args[1]</code> ::= <code>bankHostname:bankPort</code> </li>
 	 *        </ul>
 	 */
 	public static void main(final String[] args) {
 		/* Details for the server. */
 		String serverHostname = Comms.DEFAULT_SERVERNAME;
 		int serverPort = Comms.DEFAULT_SERVERPORT;
 		
 		/* Details for the bank. */
 		String bankHostname = Comms.DEFAULT_BANKNAME;
 		int bankPort = Comms.DEFAULT_BANKPORT;
 		
 		/* Check if a server host and port was specified at the command line. */
 		if (args.length > 0) {
 			try {
 				final String[] input = args[0].split(":", 2);
 				
 				serverHostname = input[0];
 				if (input.length > 1)
 					serverPort = Integer.parseInt(input[1]);
 				
 				if (serverPort <= 0 || serverPort > 65535)
 					throw new NumberFormatException("Invalid port number: " + serverPort);
 			} catch (final NumberFormatException e) {
 				System.err.println(e.getMessage());
 				if (DEBUG_ERROR_TRACE)
 					e.printStackTrace();
 				System.exit(1);
 			}
 			
 			/* Check if a bank host and port was specified at the command line. */
 			if (args.length > 1)
 				try {
 					final String[] input = args[1].split(":", 2);
 					
 					bankHostname = input[0];
 					if (input.length > 1)
 						bankPort = Integer.parseInt(input[1]);
 					
 					if (bankPort <= 0 || bankPort > 65535)
 						throw new NumberFormatException("Invalid port number: " + bankPort);
 				} catch (final NumberFormatException e) {
 					System.err.println(e.getMessage());
 					if (DEBUG_ERROR_TRACE)
 						e.printStackTrace();
 					System.exit(1);
 				}
 		}
 		
 		try {
 			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
 		} catch (final Exception e) {}
 		
 		/* Create the top-level container and contents. */
 		clientFrame = new JFrame("stealthnet");
 		Client app = null;
 		try {
 			app = new Client(serverHostname, serverPort, bankHostname, bankPort);
 		} catch (final Exception e) {
 			System.out.println("Unable to create StealthNet client.");
 			if (DEBUG_ERROR_TRACE)
 				e.printStackTrace();
 			System.exit(1);
 		}
 		final Component contents = app.createGUI();
 		clientFrame.getContentPane().add(contents, BorderLayout.CENTER);
 		
 		/*
 		 * Finish setting up the GUI - add a window listener such that closing
 		 * the GUI closes the application properly.
 		 */
 		clientFrame.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(final WindowEvent e) {
 				System.exit(0);
 			};
 		});
 		clientFrame.pack();
 		clientFrame.setVisible(true);
 	}
 }
 
 /******************************************************************************
  * END OF FILE: Client.java
  *****************************************************************************/
