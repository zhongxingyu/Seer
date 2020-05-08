 package suggest;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.JViewport;
 import javax.swing.KeyStroke;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import networking.ClientInfo;
 import networking.Networking;
 import whiteboard.Backend;
 import GUI.MainFrame;
 import GUI.ResultsPanel;
 import edu.stanford.ejalbert.BrowserLauncher;
 import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
 import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
 
 public class SuggestGUI extends JPanel {
     private MainFrame mainFrame;
 	private JTextField input;
 	private JTextArea duckoutput;
 	private JTextArea dictoutput;
 	private QueryService queryService;
 	private JPanel _suggestPanel;
 	private JPanel _networkPanel;
 	public JTextField _usernameField;
 	private JTextField _ipField, _portField;
 	private JPanel _findPanel;
 	private Networking _net;
 	private JTextPane _chatPane, _wikiPane;
 	private JTextArea _chatMessage;
 	private JScrollPane _chatScrollPane, _userScrollPane, _wikiScrollPane, _dictScrollPane, _duckScrollPane;
 	private BlockingQueue<String> _bqueue;
 	private SuggestThread _suggestThread;
 	private JButton _beHostButton, _joinButton, _sendMessageButton, _leaveButton, _backButton, _sourceButton;
 	private int _role, _defaultPort, _chatHeight;
 	private ArrayList<ClickText> _textList;
 	private Stack<String> _back;
 	private ResultsPanel resultsPanel;
 	private Backend _backend;
 	private JTextField searchField;
 	private boolean _browse;
 	private java.awt.Desktop _desktop;
 	public JTabbedPane tabbedPane;
 	private LinkedList<ClientInfo> activeUsers;
 	private JTextArea activeUserList;
 	private Double _originalSize;
 	private JLabel _ipLabel, _portLabel;
 	
 	final int CHAT_WIDTH = 340;
 	final int CHAT_HEIGHT = 480;
 	final int SUGGEST_HEIGHT = 600;
 	
 	public SuggestGUI(Dimension interfaceSize, MainFrame main) {
 		super(new java.awt.BorderLayout());
 		
 		mainFrame = main;
 		_originalSize = mainFrame.getSize().getHeight();
 		
		_chatHeight = (int) (_originalSize - 450);
 		
 		buildSuggestTab();
 		buildNetworkTab();
 		buildFindTab();
 		
 		tabbedPane = new JTabbedPane();
 		
 		ImageIcon suggest = new ImageIcon("./lib/question.jpeg");
 		tabbedPane.addTab("Suggestions", suggest, _suggestPanel, "Get Suggestions");
 		
 		ImageIcon network = new ImageIcon("./lib/web.jpeg");
 		tabbedPane.addTab("Networking", network, _networkPanel, "Set Up Networking");
 		
 		ImageIcon find = new ImageIcon("./lib/find.jpeg");
 		tabbedPane.addTab("Find", find, _findPanel, "Find Stuff");
 		
 		this.add(tabbedPane);
 		this.setPreferredSize(interfaceSize);
 		this.setSize(interfaceSize);
 		queryService = new QueryService();
 		_bqueue = new LinkedBlockingQueue<String>();
 		_suggestThread = new SuggestThread();
 		_suggestThread.start();
 		_role = 0;
 		_back = new Stack<String>();
 		_browse = false;
 		if (java.awt.Desktop.isDesktopSupported()) {
 			_desktop = java.awt.Desktop.getDesktop();
 			if (_desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
 				_browse = true;
 				System.out.println(_browse);
 			}
 		}
 		
 	}
 	
 	public void setNetworking(Networking net) {
 		_net = net;
 		_net.setSuggestPanel(this);
 		_defaultPort = _net.DEFAULT_PORT;
 		_portField.setText(String.valueOf(_defaultPort));
 	}
 	
 	public void setBackend(Backend b) {
 		_backend = b;
 		resultsPanel.setBackend(_backend);
 	}
 	
 	private void buildFindTab() {
 		_findPanel = new JPanel();
 		resultsPanel = new ResultsPanel();
 		JPanel searchPanel = new JPanel();
 		searchField = new JTextField(25);
 		JPanel buttonPanel = new JPanel();
 		JButton searchButton = new JButton("Find it");
 		searchButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				resultsPanel.setResults(_backend.search(searchField.getText()), searchField.getText());
 			}
 		});
 		searchPanel.add(searchField, BorderLayout.CENTER);
 		buttonPanel.add(searchButton);
 		_findPanel.setLayout(new FlowLayout());
 		_findPanel.add(searchPanel);
 		_findPanel.add(buttonPanel);
 		_findPanel.add(resultsPanel);
 	}
 
 	public boolean networkingSet() {
 		return _net!=null;
 	}
 
 	private void buildNetworkTab() {
 	    activeUsers = new LinkedList<ClientInfo>();
 		_networkPanel = new JPanel();
 		JPanel hostPanel = new JPanel();
 		JPanel clientPanel = new JPanel();
 		JPanel usernamePanel = new JPanel();
 		JLabel usernamelabel = new JLabel("Username: ");
 		_usernameField = new JTextField(15);
 		_usernameField.setEditable(true);
 		JPanel ipPanel = new JPanel();
 		_ipLabel = new JLabel("");
 		JPanel portPanel = new JPanel();
 		_portLabel = new JLabel("");
 		_beHostButton = new JButton("Host a Brainstorm");
 		_beHostButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(_usernameField.getText().isEmpty()) {
 					// no username specified
 					JOptionPane.showMessageDialog(_networkPanel, "You must have a username.", "No Username", JOptionPane.ERROR_MESSAGE);
 				}
 				else {
 					// call networking becomehost method
 					//_usernameField.getText for parameter
 					// check for bad connection boolean
 					if (_net != null) {
 						_role = 1;
 						if (_net.becomeHost(_usernameField.getText())) {
 							mainFrame._load.setEnabled(false);
 							_chatMessage.setEnabled(true);
 							_chatPane.setEnabled(true);
 							_userScrollPane.setEnabled(true);
 							try {
 								_ipLabel.setText("IP Address:  " + InetAddress.getLocalHost().toString());
 							} catch (UnknownHostException e1) {
 								e1.printStackTrace();
 							}
 							_sendMessageButton.setEnabled(true);
 							_leaveButton.setEnabled(true);
 							_chatMessage.grabFocus();
 							_usernameField.setEnabled(false);
 							_portField.setEnabled(false);
 							_ipField.setEnabled(false);
 							_beHostButton.setEnabled(false);
 							_joinButton.setEnabled(false);
 							SimpleAttributeSet set = new SimpleAttributeSet();
 							StyleConstants.setFontSize(set, 18);
 							StyleConstants.setForeground(set, Color.CYAN);
 							StyleConstants.setFontFamily(set, "Veranda");
 							StyledDocument doc = _chatPane.getStyledDocument();
 							StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 							doc.setParagraphAttributes(doc.getLength(), 15, set, true);
 							try {
 								doc.insertString(doc.getLength(), "You just joined the Brainstorm!\n", set);
 							} catch (BadLocationException e2) {
 								e2.printStackTrace();
 							}
 						}
 						else {
 							// handle port error
 							connectionError();
 						}
 					} else {
 						System.out.println("suggest: networking is null, has not be set");
 					}
 				}
 			}
 		});
 		
 		JLabel ipLabel = new JLabel("IP Address: ");
 		_ipField = new JTextField(10);
 		_ipField.setEditable(true);
 		JLabel portLabel = new JLabel("Port: ");
 		_portField = new JTextField(4);
 		_portField.setEditable(true);
 		_portField.setText(String.valueOf(_defaultPort));
 		
 		JPanel joinPanel = new JPanel();
 		_joinButton = new JButton("Join a Brainstorm");
 		_joinButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(_ipField.getText().isEmpty() || _portField.getText().isEmpty()) {
 					// no ip address specified
 					JOptionPane.showMessageDialog(_networkPanel, "You must enter the host's IP address and port to connect to.", "Invalid Connection", JOptionPane.ERROR_MESSAGE);
 				} else if(_usernameField.getText().isEmpty()) {
 					// no username specified
 					JOptionPane.showMessageDialog(_networkPanel, "You must enter a username before connecting.", "No Username ", JOptionPane.ERROR_MESSAGE);
 				}
 				else {
 					// call networking becomeclient method
 					// _ipField.getText and _usernameField.getText for parameters
 					// check for bad connection boolean
 					//
 					if (_net != null) {
 						_role = 2;                
 						if (!_backend.pastActions.empty()) {
 		                    int ret = JOptionPane.showConfirmDialog(null, "You have made changes to the current brainStorm would you like to save?");
 		                    if (ret == JOptionPane.YES_OPTION) {
 		                        /* Call save */
 		                        mainFrame._save.getActionListeners()[0].actionPerformed(null);
 		                    } else if (ret == JOptionPane.CANCEL_OPTION || ret == JOptionPane.CLOSED_OPTION) {
 		                        return;
 		                    }
 		                }
 						
 						if(_net.becomeClient(_ipField.getText(), _usernameField.getText(), Integer.valueOf(_portField.getText()))) {
 						    mainFrame._load.setEnabled(false);
 							_chatMessage.setEnabled(true);
 							_chatPane.setEnabled(true);
 							_userScrollPane.setEnabled(true);
 							_leaveButton.setEnabled(true);
 							_portField.setEnabled(false);
 							_sendMessageButton.setEnabled(true);
 							_chatMessage.grabFocus();
 							_usernameField.setEnabled(false);
 							_ipField.setEnabled(false);
 							_beHostButton.setEnabled(false);
 							_joinButton.setEnabled(false);
 							SimpleAttributeSet set = new SimpleAttributeSet();
 							StyleConstants.setFontSize(set, 18);
 							StyleConstants.setForeground(set, Color.CYAN);
 							StyleConstants.setFontFamily(set, "Veranda");
 							StyledDocument doc = _chatPane.getStyledDocument();
 							StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 							doc.setParagraphAttributes(doc.getLength(), 15, set, true);
 							try {
 								doc.insertString(doc.getLength(), "You just joined the Brainstorm!\n", set);
 							} catch (BadLocationException e2) {
 								e2.printStackTrace();
 							}
 						}
 						else {
 							// handle connection error
 							connectionError();
 						}
 					} else {
 						System.out.println("suggest: networking is null, has not be set");
 					}
 				}
 			}
 		});
 		
 		JPanel leavePanel = new JPanel();
 		_leaveButton = new JButton("Leave Brainstorm");
 		_leaveButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 			    mainFrame._load.setEnabled(true);
 			    activeUserList.setText("");
 				_chatMessage.setEnabled(false);
 				_chatPane.setEnabled(false);
 				_userScrollPane.setEnabled(false);
 				_ipLabel.setText("");
 				_portLabel.setText("");
 				_portField.setText(String.valueOf(_defaultPort));
 				_portField.setEnabled(true);
 				_sendMessageButton.setEnabled(false);
 				_leaveButton.setEnabled(false);
 				_usernameField.grabFocus();
 				_usernameField.setEnabled(true);
 				_ipField.setEnabled(true);
 				_beHostButton.setEnabled(true);
 				_joinButton.setEnabled(true);
 				_role = 0;
 				try {
 					StyledDocument document = _chatPane.getStyledDocument();
 					document.remove(0, document.getLength());
 					document.insertString(0, "Chat:\n", null);
 				} catch (BadLocationException e1) {
 					e1.printStackTrace();
 				}
 				// tell networking you are leaving
 				_net.signOff();
 			}
 			
 		});
 		_leaveButton.setEnabled(false);
 		leavePanel.add(_leaveButton);
         
 		JPanel activeUserPanel = new JPanel();
 		activeUserPanel.setLayout(new FlowLayout());
 		JLabel users = new JLabel("Active Users:");
 		activeUserList = new JTextArea(4, 18);
 		activeUserList.setEditable(false);
 		_userScrollPane = new JScrollPane(activeUserList);
 		
 		activeUserPanel.add(users, BorderLayout.WEST);
 		activeUserPanel.add(_userScrollPane, BorderLayout.EAST);
 		
 		JPanel chatPanel = new JPanel();
 		_chatPane = createChatPane();
 		_chatScrollPane = new JScrollPane(_chatPane);
 		_chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		_chatScrollPane.setPreferredSize(new Dimension(CHAT_WIDTH, _chatHeight));
 		chatPanel.add(_chatScrollPane);
 		JPanel messagePanel = new JPanel();
 		_chatMessage = new JTextArea(5, 20);
 		_chatMessage.setEditable(true);
 		_chatMessage.setLineWrap(true);
 		_chatMessage.setWrapStyleWord(true);
 		_sendMessageButton = new JButton("Send");
 		JScrollPane chatMessageScrollPane = new JScrollPane(_chatMessage, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
 		Action action = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addMessage();
 			}};
 		KeyStroke keyStroke = KeyStroke.getKeyStroke("ENTER");
 		InputMap im = _chatMessage.getInputMap();
 		_chatMessage.getActionMap().put(im.get(keyStroke), action);
 		_chatMessage.setEnabled(false);
 		_chatPane.setEnabled(false);
 		
 		
 		_sendMessageButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addMessage();
 				_chatMessage.grabFocus();
 			}
 		});
 		messagePanel.add(chatMessageScrollPane,BorderLayout.WEST);
 		messagePanel.add(_sendMessageButton, BorderLayout.EAST);
 		
 		_networkPanel.setLayout(new FlowLayout());
 		usernamePanel.add(usernamelabel,BorderLayout.WEST);
 		usernamePanel.add(_usernameField, BorderLayout.EAST);
 		hostPanel.add(_beHostButton, BorderLayout.CENTER);
 		ipPanel.add(_ipLabel, BorderLayout.CENTER);
 		portPanel.add(_portLabel, BorderLayout.CENTER);
 		clientPanel.add(ipLabel, BorderLayout.WEST);
 		clientPanel.add(_ipField, BorderLayout.WEST);
 		clientPanel.add(portLabel, BorderLayout.EAST);
 		clientPanel.add(_portField, BorderLayout.EAST);
 		joinPanel.add(_joinButton, BorderLayout.CENTER);
 		
 		_networkPanel.add(usernamePanel);
 		_networkPanel.add(hostPanel);
 		_networkPanel.add(ipPanel);
 		_networkPanel.add(portPanel);
 		_networkPanel.add(clientPanel);
 		_networkPanel.add(joinPanel);
 		_networkPanel.add(leavePanel);
 		_networkPanel.add(activeUserPanel);
 		_networkPanel.add(chatPanel);
 		_networkPanel.add(messagePanel);
 	}
 	
 	// networking should call me
 	public String retryUsername() {
 		String ret = JOptionPane.showInputDialog(_networkPanel, "The username, "+ _usernameField.getText() + ", you choose is already being used. Please pick another.", "");
 		if (ret != null) _usernameField.setText(ret);
 		return ret;
 	}
 	
 	// networking should call me maybe
 	public void setPortLabel(int port) {
 		_portLabel.setText("Port:  " + port);
 	}
 	
 	// Customized entering and exiting methods below
 	
 //	// networking needs to call me please
 //	public void newUser(String username) {
 //		SimpleAttributeSet set = new SimpleAttributeSet();
 //		StyleConstants.setFontSize(set, 18);
 //		StyleConstants.setForeground(set, Color.CYAN);
 //		StyledDocument doc = _chatPane.getStyledDocument();
 //		try {
 //			doc.insertString(doc.getLength(), username + "just joined the Brainstrom!\n", set);
 //		} catch (BadLocationException e) {
 //			e.printStackTrace();
 //		}
 //	}
 	
 //	// networking needs to call me please
 //	public void userExited(String username) {
 //		SimpleAttributeSet set = new SimpleAttributeSet();
 //		StyleConstants.setFontSize(set, 18);
 //		StyleConstants.setForeground(set, Color.RED);
 //		StyledDocument doc = _chatPane.getStyledDocument();
 //		try {
 //			doc.insertString(doc.getLength(), username + "just left the Brainstrom!\n", set);
 //		} catch (BadLocationException e) {
 //			e.printStackTrace();
 //		}
 //	}
 	
 	public void connectionError() {
 		Object[] options = {"Ok", "Retry Connection"};
 		int n = JOptionPane.showOptionDialog(_networkPanel, "A connection error has disrupted the Brainstorm.", "Connection Error", JOptionPane.CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
 		if (n == 1) {
 			System.out.println("retried");
 			// retry
 			if (_role == 1) {
 				if (_net.becomeHost(_usernameField.getText())) {
 	                _userScrollPane.setEnabled(true);
 					_chatMessage.setEnabled(true);
 					_chatPane.setEnabled(true);
 					try {
 						_ipLabel.setText("IP Address:  " + InetAddress.getLocalHost().toString());
 					} catch (UnknownHostException e1) {
 						e1.printStackTrace();
 					}
 					_portField.setEnabled(false);
 					_sendMessageButton.setEnabled(true);
 					_leaveButton.setEnabled(true);
 					_chatMessage.grabFocus();
 					_usernameField.setEnabled(false);
 					_ipField.setEnabled(false);
 					_beHostButton.setEnabled(false);
 					_joinButton.setEnabled(false);
 					SimpleAttributeSet set = new SimpleAttributeSet();
 					StyleConstants.setFontSize(set, 18);
 					StyleConstants.setForeground(set, Color.MAGENTA);
 					StyleConstants.setFontFamily(set, "Veranda");
 					StyledDocument doc = _chatPane.getStyledDocument();
 					StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 					doc.setParagraphAttributes(doc.getLength(), 10, set, false);
 					try {
 						doc.insertString(doc.getLength(), "Connection Restored!\n", set);
 					} catch (BadLocationException e) {
 						e.printStackTrace();
 					}
 				} else {
 					connectionError();
 				}
 				
 			} else if (_role == 2) {
 				// retry
 				if (_net.becomeClient(_ipField.getText(), _usernameField.getText(), Integer.valueOf(_portField.getText()))) {
 					_chatMessage.setEnabled(true);
 					_chatPane.setEnabled(true);
 					_userScrollPane.setEnabled(true);
 					_sendMessageButton.setEnabled(true);
 					_leaveButton.setEnabled(true);
 					_chatMessage.grabFocus();
 					_usernameField.setEnabled(false);
 					_portField.setEnabled(false);
 					_ipField.setEnabled(false);
 					_beHostButton.setEnabled(false);
 					_joinButton.setEnabled(false);
 					SimpleAttributeSet set = new SimpleAttributeSet();
 					StyleConstants.setFontSize(set, 18);
 					StyleConstants.setForeground(set, Color.MAGENTA);
 					StyleConstants.setFontFamily(set, "Veranda");
 					StyledDocument doc = _chatPane.getStyledDocument();
 					StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 					doc.setParagraphAttributes(doc.getLength(), 10, set, false);
 					try {
 						doc.insertString(doc.getLength(), "Connection Restored!\n", set);
 					} catch (BadLocationException e) {
 						e.printStackTrace();
 					}
 				}
 				else {
 					connectionError();
 				}
 					
 			}
 		}
 		else {
 			_role = 0;
 			_chatMessage.setEnabled(false);
 			try {
 				StyledDocument document = _chatPane.getStyledDocument();
 				document.remove(0, document.getLength());
 				document.insertString(0, "Chat:\n", null);
 			} catch (BadLocationException e1) {
 				e1.printStackTrace();
 			}
             mainFrame._load.setEnabled(true);
             activeUserList.setText("");
 			_chatPane.setEnabled(false);
 			_userScrollPane.setEnabled(false);
 			_ipLabel.setText("");
 			_portLabel.setText("");
 			_portField.setText(String.valueOf(_defaultPort));
 			_portField.setEnabled(true);
 			_sendMessageButton.setEnabled(false);
 			_leaveButton.setEnabled(false);
 			_usernameField.grabFocus();
 			_usernameField.setEnabled(true);
 			_ipField.setEnabled(true);
 			_beHostButton.setEnabled(true);
 			_joinButton.setEnabled(true);
 		}
 	}
 	
 	private JTextPane createChatPane() {
 		JTextPane pane = new JTextPane();
 		pane.setSize(20, 50);
 		pane.setEditable(false);
 		// Chat Header
 		SimpleAttributeSet set = new SimpleAttributeSet();
 		StyleConstants.setBold(set, true);
 		StyleConstants.setFontSize(set, 26);
 		StyleConstants.setFontFamily(set, "Veranda");
 		StyleConstants.setAlignment(set, StyleConstants.ALIGN_CENTER);
 		StyledDocument doc = pane.getStyledDocument();
 		try {
 			doc.insertString(doc.getLength(), "Chat:\n", set);
 			doc.setParagraphAttributes(0, doc.getLength(), set, true);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 		
 		return pane;
 	}
 	
 	public void textResize(Double height) {
 		int delta = (int) (_originalSize-height);
 		System.out.println("height:  " + height);
 		System.out.println("delta:  " + delta);
 		_wikiScrollPane.setPreferredSize(new Dimension(CHAT_WIDTH, SUGGEST_HEIGHT-delta));
 		_wikiPane.setSize(CHAT_WIDTH, SUGGEST_HEIGHT-delta);
 		_duckScrollPane.setPreferredSize(new Dimension(CHAT_WIDTH, SUGGEST_HEIGHT-delta));
 		duckoutput.setSize(CHAT_WIDTH, SUGGEST_HEIGHT-delta);
 		_dictScrollPane.setPreferredSize(new Dimension(CHAT_WIDTH, SUGGEST_HEIGHT-delta));
 		dictoutput.setSize(CHAT_WIDTH, SUGGEST_HEIGHT-delta);
 		_chatScrollPane.setPreferredSize(new Dimension(CHAT_WIDTH, _chatHeight-delta));
 		_chatPane.setSize(CHAT_WIDTH, _chatHeight-delta);
 		_suggestPanel.repaint();
 		_networkPanel.repaint();
 		repaint();
 	}
 
 	public void addMessage(){
 		String text = _chatMessage.getText();
 		if(!text.equals("")){
 			_chatMessage.setText("");
 			SimpleAttributeSet set = new SimpleAttributeSet();
 			StyledDocument doc = _chatPane.getStyledDocument();
 			StyleConstants.setFontFamily(set, "Veranda");
 			StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 			doc.setParagraphAttributes(doc.getLength(), text.length(), set, true);
 			StyleConstants.setBold(set, true);
 			StyleConstants.setForeground(set, Color.BLUE);
 			StyleConstants.setFontSize(set, 16);
 			try {
 				doc.insertString(doc.getLength(), "Me:   ", set);
 				set = new SimpleAttributeSet();
 				StyleConstants.setFontSize(set, 14);
 				doc.insertString(doc.getLength(), text + "\n", set);
 			} catch (BadLocationException e) {
 				e.printStackTrace();
 			}
 			_chatMessage.grabFocus();
 			_net.sendMessage(text);
 			JViewport vport = _chatScrollPane.getViewport();
 			Point vp = vport.getViewPosition();
 			vp.translate(0, _chatScrollPane.getSize().height);
 			_chatPane.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
 			_chatPane.repaint();
 		}
 	}
 	
 	public void updateUsers(LinkedList<ClientInfo> users) {
 	    activeUsers = users;
 	    activeUserList.setText("");
 	    for (ClientInfo ci : activeUsers) {
 	        activeUserList.append(ci.username + "\n");
 	    }
 	}
 	
 	public void newMessage(String username, String message) {
 		System.out.println("newmessage called");
 		SimpleAttributeSet set = new SimpleAttributeSet();
 		StyledDocument doc = _chatPane.getStyledDocument();
 		StyleConstants.setFontFamily(set, "Veranda");
 		StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
 		doc.setParagraphAttributes(doc.getLength(), message.length(), set, true);
 		StyleConstants.setBold(set, true);
 		StyleConstants.setForeground(set, Color.GREEN);
 		StyleConstants.setFontSize(set, 16);
 		try {
 			doc.insertString(doc.getLength(), username + ":   ", set);
 			set = new SimpleAttributeSet();
 			StyleConstants.setFontSize(set, 14);
 			doc.insertString(doc.getLength(), message + "\n", set);
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 		JViewport vport = _chatScrollPane.getViewport();
 		Point vp = vport.getViewPosition();
 		vp.translate(0, _chatScrollPane.getSize().height);
 		_chatPane.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
 		_chatPane.repaint();
 	}
 	
 	private void buildSuggestTab() {
 		_suggestPanel = new JPanel();
 		JPanel inputpanel = new JPanel();
 		input = new JTextField(25);
 		input.setEditable(true);
 		inputpanel.add(input);
 		
 		Action action = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String query = input.getText();
 				if (!query.isEmpty()) {
 					suggest(query);
 				}
 			}};
 		KeyStroke keyStroke = KeyStroke.getKeyStroke("ENTER");
 		InputMap im = input.getInputMap();
 		input.getActionMap().put(im.get(keyStroke), action);
 		
 		JPanel buttonPanel = new JPanel();
 		JButton suggestButton = new JButton("Suggestions?");
 		suggestButton.addActionListener(new SuggestionListener());
 		buttonPanel.add(suggestButton);
 		
 		JPanel wikiPanel = new JPanel();
 		
 		_wikiPane = new JTextPane();
 		_wikiPane.setSize(20, 50);
 		_wikiPane.setEditable(false);
 		_wikiPane.addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseReleased(MouseEvent e) {
 			}
 			
 			@Override
 			public void mousePressed(MouseEvent e) {
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 			}
 			
 			@Override
 			public void mouseEntered(MouseEvent e) {
 			}
 			
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				Point point = e.getPoint();
 				int viewToModel = _wikiPane.viewToModel(point);
 				searchIt(viewToModel);
 				System.out.println("VIEW: " + viewToModel);
 			}
 		});
 		_textList = new ArrayList<ClickText>();
 		
 		_wikiScrollPane = new JScrollPane(_wikiPane);
 		_wikiScrollPane.setVerticalScrollBarPolicy(
 		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		_wikiScrollPane.setPreferredSize(new Dimension(340, 600));
 		wikiPanel.add(_wikiScrollPane);
 		JPanel backPanel = new JPanel();
 		_backButton = new JButton("Back");
 		_backButton.setEnabled(false);
 		_backButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				_back.pop();
 				String popBack = _back.pop();
 				suggest(popBack);
 				input.setText(popBack);
 				if(_back.size() <= 1) {
 					_backButton.setEnabled(false);
 					if (_back.size() == 0) {
 						_sourceButton.setEnabled(false);
 					}
 				}
 			}
 		});
 		backPanel.add(_backButton);
 		_sourceButton = new JButton("View Source");
 		_sourceButton.setEnabled(false);
 		_sourceButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (_browse) {
 					String top = _back.peek();
 					String open = "http://en.wikipedia.org/wiki/" + top;
 					for (int i = 0; i < open.length(); i++) {
 						if (open.charAt(i) == ' ') {
 							open = open.substring(0, i) + '_' + open.substring(i+1, open.length());
 						}
 					}
 					System.out.println(open);
 					BrowserLauncher launcher;
 					try {
 						launcher = new BrowserLauncher();
 						launcher.setNewWindowPolicy(true);
 						launcher.openURLinBrowser(open);
 					} catch (BrowserLaunchingInitializingException e1) {
 						e1.printStackTrace();
 					} catch (UnsupportedOperatingSystemException e1) {
 						e1.printStackTrace();
 					}
 				}
 				else {
 					JOptionPane.showMessageDialog(_suggestPanel, "No Browser Supported. Sorry.", "Browser Issue", JOptionPane.WARNING_MESSAGE);
 				}
 			}
 		});
 		backPanel.add(_sourceButton);
 		
 		JPanel dictPanel = new JPanel();
 		dictoutput = new JTextArea(20, 50);
 		dictoutput.setEditable(false);
 		dictoutput.setLineWrap(true);
 		dictoutput.setWrapStyleWord(true);
 		_dictScrollPane = new JScrollPane(dictoutput);
 		_dictScrollPane.setVerticalScrollBarPolicy(
 		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		_dictScrollPane.setPreferredSize(new Dimension(340, 600));
 		dictPanel.add(_dictScrollPane);
 		
 		JPanel duckPanel = new JPanel();
 		duckoutput = new JTextArea(20, 50);
 		duckoutput.setEditable(false);
 		duckoutput.setLineWrap(true);
 		duckoutput.setWrapStyleWord(true);
 		_duckScrollPane = new JScrollPane(duckoutput);
 		_duckScrollPane.setVerticalScrollBarPolicy(
 		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		_duckScrollPane.setPreferredSize(new Dimension(340, 600));
 		duckPanel.add(_duckScrollPane);
 		
 		JTabbedPane tabbedPane = new JTabbedPane();
 		
 		ImageIcon wiki = new ImageIcon("./lib/wiki.jpeg");
 		tabbedPane.addTab("Wikipedia", wiki, wikiPanel,
 		"Wikipedia");
 		
 		ImageIcon google = new ImageIcon("./lib/dict.jpeg");
 		tabbedPane.addTab("Dictionary", google, dictPanel,
 		"Google Dictionary");
 		
 		ImageIcon duck = new ImageIcon("./lib/duckduck.jpeg");
 		tabbedPane.addTab("DuckDuckGo", duck, duckPanel,
 		"DuckDuckGo");
 		
 		_suggestPanel.setLayout(new FlowLayout());
 		_suggestPanel.add(inputpanel);
 		_suggestPanel.add(buttonPanel);
 		_suggestPanel.add(tabbedPane);
 		_suggestPanel.add(backPanel);
 	}
 	
 	public void searchIt(int cursor) {
 		for (ClickText text:_textList) {
 			if (cursor >= text.getStart() && cursor <= text.getEnd()) {
 				System.out.println(text.getQuery());
 				input.setText(text.getQuery());
 				suggest(text.getQuery());
 				break;
 			}
 		}
 	}
 
 	public void suggest(String query) {
 		try {
 			_bqueue.put(query);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void submitQuery(String query) {
 		_back.push(query);
 		_sourceButton.setEnabled(true);
 		if(_back.size() > 1) {
 			_backButton.setEnabled(true);
 		}
 		_textList = new ArrayList<ClickText>();
 		List<Future<String>> futures = new ArrayList<Future<String>>();
 		for (int i = 0; i < 3; i++) {
 			futures.add(i, queryService.submit(query, i));
 		}
 		int j =0;
 		for (Future<String> future:futures) {
 			try {
 				String result = future.get();
 				if (j == 0) {
 					if (result.startsWith("#REDIRECT") || result.startsWith("#redirect")) {
 						int index = result.indexOf("[");
 						int index2 = result.indexOf("]", index);
 						String requery = result.substring(index+2, index2);
 						result = queryService.submit(requery, 0).get();
 					}
 					try {
 						styleWiki(result);
 					} catch (BadLocationException e) {
 						e.printStackTrace();
 					}
 				}
 				else if (j==1) {
 					dictoutput.setText(result);
 				}
 				else if (j==2) {
 					duckoutput.setText(result);
 				}
 				j++;
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			} catch (ExecutionException e3) {
 				e3.printStackTrace();
 			}
 		}
 		
 	}
 	
 	private void styleWiki(String result) throws BadLocationException {
 		StyledDocument doc = _wikiPane.getStyledDocument();
 		doc.remove(0, doc.getLength());
 		SimpleAttributeSet set = new SimpleAttributeSet();
 		SimpleAttributeSet click = new SimpleAttributeSet();
 		StyleConstants.setForeground(click, Color.BLUE);
 		StyleConstants.setItalic(click, true);
 		int end = 0;
 		int index = result.indexOf("[[");
 		int index2 = result.indexOf("]]", index);
 		int middle = -1;
 		String query = "";
 		String text = "";
 		while (index >= 0 && index2 >= 0) {
 			doc.insertString(doc.getLength(), result.substring(end, index), set);
 			middle = result.indexOf("|", index);
 			if (middle > index && middle < index2) {
 				query = result.substring(index+2, middle);
 				text = result.substring(middle+1, index2);
 				end = index2-3-query.length();
 				doc.insertString(doc.getLength(), text, click);
 				ClickText clickText = new ClickText(query, index, end);
 				_textList.add(clickText);
 				result = result.substring(0, index) + result.substring(middle+1, index2) + result.substring(index2+2, result.length());
 			} else {
 				query = result.substring(index+2, index2);
 				end = index2-2;
 				doc.insertString(doc.getLength(), query, click);
 				ClickText clickText = new ClickText(query, index, end);
 				_textList.add(clickText);
 				result = result.substring(0, index) + result.substring(index+2, index2) + result.substring(index2+2, result.length());
 			}
 			if (doc.getLength() > 33000) {
 				doc.insertString(doc.getLength(), "Content stops here.  See source for more content.", set);
 				return;
 			}
 			index = result.indexOf("[[");
 			index2 = result.indexOf("]]", index);
 		}
 		
 		doc.insertString(doc.getLength(), result.substring(end, result.length()), set);
 		
 	}
 
 	private class SuggestThread extends Thread {
 		
 		@Override
 		public void run() {
 			while (true) {
 				try {
 					String query = _bqueue.take();
 					submitQuery(query);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private class SuggestionListener implements ActionListener {
 		
 		public void actionPerformed(ActionEvent e) {
 			String query = input.getText();
 			if (!query.isEmpty()) {
 				suggest(query);
 			}
 		}
 		
 		
 	}
 }
