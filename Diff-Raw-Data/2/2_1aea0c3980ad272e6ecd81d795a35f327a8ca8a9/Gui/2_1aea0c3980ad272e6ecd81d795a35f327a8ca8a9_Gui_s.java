 package app;
 
 import javax.swing.*;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.EditorKit;

import app.Negotiator.ClientStatus;
 import net.miginfocom.swing.MigLayout;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class Gui {
 	private static Gui instance;
 	private JFrame mainFrame;
 	private JEditorPane chatArea;
 	private JTextField typingArea;
 	private JButton sendButton;
 	private String userName = null;
 	private String address = "localhost";
 	private JTextField addressField;
 	private JTextField nameField;
 	private JLabel statusLabel;
 	private JRadioButton hostRadioButton;
 	private JRadioButton joinRadioButton;
 	private JButton connectButton;
 	private JList<String> onlineUsersList;
 	DefaultListModel<String> listModel = new DefaultListModel<String>();
 
 	private Negotiator negotiatorInstance;
 	private JScrollPane userListScroller;
 
 	private Gui() {
 
 	}
 
 	public static void main(String[] args) throws InterruptedException {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					getInstance().initialize();
 					getInstance().negotiatorInstance = Negotiator.getInstance();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public static Gui getInstance() {
 		if (instance == null)
 			instance = new Gui();
 		return instance;
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public JFrame getMainFrame() {
 		return mainFrame;
 	}
 
 	public JTextField getAddressField() {
 		return addressField;
 	}
 
 	public JTextField getNameField() {
 		return nameField;
 	}
 
 	public void setStatusIcon(String statusIcon) {
 		statusLabel.setIcon(new ImageIcon(Gui.class.getResource(statusIcon)));
 		statusLabel.repaint();
 	}
 
 	public void takeMessage(String messageFromServer) {
 		append(messageFromServer);
 		chatArea.setCaretPosition(chatArea.getDocument().getLength());
 	}
 
 	public void append(String messageFromServer) {
 		EditorKit editor = chatArea.getEditorKit();
 		StringReader reader = new StringReader(messageFromServer);
 		try {
 			editor.read(reader, chatArea.getDocument(), chatArea.getDocument()
 					.getLength());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void isLink(String message) {
 		String originalMessage = message + " ";
 		String copyOfMessage = originalMessage;
 
 		int begin = copyOfMessage.length() - 1;
 
 		while (originalMessage.contains("http:")) {
 			int beginSubstring = copyOfMessage.lastIndexOf("http:", begin);
 			int endSubstring = copyOfMessage.indexOf(" ", beginSubstring);
 			String link = copyOfMessage.substring(beginSubstring, endSubstring);
 			copyOfMessage = copyOfMessage.replace(link, "<a href='" + link
 					+ "'>" + link + "</a>");
 			String secondSubstring = copyOfMessage.substring(copyOfMessage
 					.indexOf("<a"));
 			begin = copyOfMessage.length() - secondSubstring.length() - 1;
 			originalMessage = originalMessage.replaceFirst("http:", "link");
 		}
 		append(copyOfMessage);
 		chatArea.setCaretPosition(chatArea.getDocument().getLength());
 	}
 
 	public void hideUserInterface() {
 		nameField.setEnabled(false);
 		addressField.setEnabled(false);
 		connectButton.setText("Disconnect");
 		hostRadioButton.setEnabled(false);
 		joinRadioButton.setEnabled(false);
 	}
 
 	public void showUserInterface() {
 		nameField.setEnabled(true);
 		addressField.setEnabled(true);
 		connectButton.setText("Connect");
 		hostRadioButton.setEnabled(true);
 		joinRadioButton.setEnabled(true);
 	}
 
 	class hostListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getActionCommand().equals("Connect")) {
 				if (validateNameField()) {
 					hideUserInterface();
 					if (hostRadioButton.isSelected()) {
 						userName = nameField.getText();
 						negotiatorInstance.startServer();
 						negotiatorInstance.startClient();
 					} else {
 						address = addressField.getText();
 						userName = nameField.getText();
 						negotiatorInstance.startClient();
 					}
 				}
 			}
 			if (e.getActionCommand().equals("Disconnect")) {
 				showUserInterface();
 				listModel.clear();
 				onlineUsersList.repaint();
 				negotiatorInstance.sendDisconnectedMessage();
 				negotiatorInstance.stopClient();
 				negotiatorInstance.stopServer();
 			}
 		}
 	}
 
 	public Boolean validateNameField() {
 		if (nameField.getText().length() > 0) {
 			return true;
 		} else
 			ClientErrorHandler.nameIsEmptyError();
 			return false;
 	}
 
 	class joinListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (negotiatorInstance.clientStatus.equals("disconnected")) {
 				address = JOptionPane.showInputDialog(mainFrame,
 						"Enter server IP:", "", JOptionPane.QUESTION_MESSAGE);
 				userName = JOptionPane.showInputDialog(mainFrame,
 						"Enter your name:", "", JOptionPane.QUESTION_MESSAGE);
 				address = addressField.getText();
 				userName = nameField.getText();
 				negotiatorInstance.startClient();
 
 			} else
 				JOptionPane.showMessageDialog(mainFrame,
 						"You already connected to server", "",
 						JOptionPane.ERROR_MESSAGE);
 		}
 
 	}
 
 	class sendButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String messageFromGui = typingArea.getText();
 			negotiatorInstance.sendMessage(messageFromGui);
 			typingArea.setText("");
 		}
 	}
 
 	class enterListener implements KeyListener {
 		@Override
 		public void keyPressed(KeyEvent ke) {
 			switch (ke.getKeyCode()) {
 			case KeyEvent.VK_ENTER:
 				sendButton.doClick();
 				break;
 			}
 		}
 
 		@Override
 		public void keyReleased(KeyEvent e) {
 		}
 
 		@Override
 		public void keyTyped(KeyEvent e) {
 		}
 	}
 
 	class changeNameListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			e.getActionCommand().equals("Name");
 			String lastName = userName;
 			userName = JOptionPane.showInputDialog(mainFrame,
 					"Enter your name:", "title", JOptionPane.QUESTION_MESSAGE);
 
 			String systemMessage = "<b>" + lastName + "</b>"
 					+ " changed name to: " + getUserName();
 			// ClientSystemMessanger.changeNameMessage(systemMessage);
 		}
 	}
 
 	class radioButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// TODO Auto-generated method stub
 			if (e.getActionCommand().equals("host")) {
 				addressField.setEnabled(false);
 			} else if (e.getActionCommand().equals("join")) {
 				addressField.setEnabled(true);
 			}
 		}
 
 	}
 
 	class closeWindowListener implements WindowListener {
 
 		@Override
 		public void windowActivated(WindowEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void windowClosed(WindowEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void windowClosing(WindowEvent e) {
 			if (negotiatorInstance.clientStatus.toString().equals("CONNECTED")) {
 				negotiatorInstance.sendDisconnectedMessage();
 			}
 		}
 
 		@Override
 		public void windowDeactivated(WindowEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void windowDeiconified(WindowEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void windowIconified(WindowEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void windowOpened(WindowEvent e) {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 
 	private void initialize() {
 		try {
 			UIManager
 					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 		} catch (ClassNotFoundException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		} catch (InstantiationException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		} catch (IllegalAccessException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 
 		mainFrame = new JFrame("Simple Chat");
 		mainFrame.setMinimumSize(new Dimension(550, 300));
 		mainFrame.setBounds(100, 100, 450, 300);
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainFrame
 				.getContentPane()
 				.setLayout(
 						new MigLayout(
 								"",
 								"[:120.00:120px,grow,left][35.00][80.00,grow][30.00][34.00][93.00,grow][59.00]",
 								"[][grow][][][][][][grow][]"));
 		mainFrame.addWindowListener(new closeWindowListener());
 
 		JLabel onlineLabel = new JLabel("Online");
 		mainFrame.getContentPane().add(onlineLabel, "cell 0 0,alignx center");
 
 		JLabel nameLabel = new JLabel("Name");
 		nameLabel.setHorizontalTextPosition(SwingConstants.LEFT);
 		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		mainFrame.getContentPane().add(nameLabel, "cell 1 0,growx");
 
 		nameField = new JTextField();
 		mainFrame.getContentPane().add(nameField, "cell 2 0,growx");
 		nameField.setColumns(10);
 
 		hostRadioButton = new JRadioButton("host");
 		mainFrame.getContentPane()
 				.add(hostRadioButton, "cell 3 0,alignx right");
 		hostRadioButton.addActionListener(new radioButtonListener());
 
 		joinRadioButton = new JRadioButton("join");
 		mainFrame.getContentPane()
 				.add(joinRadioButton, "cell 4 0,alignx right");
 		joinRadioButton.addActionListener(new radioButtonListener());
 		joinRadioButton.setSelected(true);
 
 		addressField = new JTextField();
 		mainFrame.getContentPane().add(addressField, "cell 5 0,growx");
 		addressField.setColumns(10);
 
 		connectButton = new JButton("Connect");
 		connectButton.setMinimumSize(new Dimension(85, 23));
 		mainFrame.getContentPane().add(connectButton, "cell 6 0,alignx left");
 		connectButton.addActionListener(new hostListener());
 
 		userListScroller = new JScrollPane();
 		userListScroller
 				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		mainFrame.getContentPane().add(userListScroller, "cell 0 1 1 7,grow");
 
 		onlineUsersList = new JList<String>(listModel);
 		userListScroller.setViewportView(onlineUsersList);
 		onlineUsersList.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		onlineUsersList.setModel(listModel);
 
 		JScrollPane scrollPane = new JScrollPane();
 		mainFrame.getContentPane().add(scrollPane, "cell 1 1 6 7,grow");
 
 		chatArea = new JEditorPane();
 		chatArea.setContentType("text/html");
 		chatArea = new JEditorPane("text/html", null);
 		chatArea.setEditable(false);
 		scrollPane.setViewportView(chatArea);
 		scrollPane.setAutoscrolls(true);
 
 		statusLabel = new JLabel();
 		statusLabel.setIcon(new ImageIcon(Gui.class.getResource("/resources/offline.png")));
 		mainFrame.getContentPane().add(statusLabel, "cell 0 8,alignx center");
 
 		typingArea = new JTextField();
 		typingArea.setMinimumSize(new Dimension(305, 20));
 		typingArea.setPreferredSize(new Dimension(0, 0));
 		mainFrame.getContentPane().add(typingArea, "cell 1 8 5 1,grow");
 		// typingArea.setColumns(50);
 		typingArea.addActionListener(new sendButtonListener());
 
 		JButton sendButton = new JButton("Send");
 		sendButton.setMinimumSize(new Dimension(85, 23));
 		mainFrame.getContentPane().add(sendButton, "cell 6 8,alignx left");
 		sendButton.addActionListener(new sendButtonListener());
 
 		ButtonGroup bg = new ButtonGroup();
 		bg.add(hostRadioButton);
 		bg.add(joinRadioButton);
 
 		mainFrame.setVisible(true);
 
 		chatArea.addHyperlinkListener(new HyperlinkListener() {
 			public void hyperlinkUpdate(HyperlinkEvent e) {
 				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 
 					try {
 						Desktop.getDesktop().browse(e.getURL().toURI());
 					} catch (IOException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					} catch (URISyntaxException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 				}
 			}
 		});
 	}
 
 	public void setCurrentUserList(ArrayList<String> userList) {
 		Iterator<String> iterator = userList.iterator();
 		while (iterator.hasNext()) {
 			listModel.addElement(iterator.next());
 			onlineUsersList.repaint();
 		}
 	}
 
 }
