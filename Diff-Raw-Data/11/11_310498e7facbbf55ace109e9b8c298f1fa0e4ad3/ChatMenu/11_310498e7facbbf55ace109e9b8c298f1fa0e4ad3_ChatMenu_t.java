 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 import java.awt.event.WindowListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 
 public class ChatMenu extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4097860442432321662L;
 	
 	private JLabel statusBox;
 	private JLabel nameStatus;
 	private JButton joinButton;
 	private JTextField nameField;
 	private String name;
 	private MenuAgent agent;
 	private ActionListener joinAction;
 	private boolean joinChatPressed;
 	
 	public ChatMenu() {
 //		new ConsoleWindow().setTitle("CaskChat Client");
 		initialize();
 	}
 	
 	private void initialize() {
 		int padding = 5;
 		joinChatPressed = false;
 		agent = new MenuAgent(this);
 		double ratio = (1.0+Math.sqrt(5.0))/2.0;
 		Dimension screenDims = Toolkit.getDefaultToolkit().getScreenSize();
 		
 		joinAction = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (joinButton.isEnabled())
 					joinChat();
 				else
 					joinChatPressed = true;
 			}
 		};
 		
 		addWindowListener(new WindowListener() {
 			@Override
 			public void windowActivated(WindowEvent e) {
 			}
 			@Override
 			public void windowClosed(WindowEvent e) {
 			}
 			@Override
 			public void windowClosing(WindowEvent e) {
 			}
 			@Override
 			public void windowDeactivated(WindowEvent e) {
 			}
 			@Override
 			public void windowDeiconified(WindowEvent e) {
 			}
 			@Override
 			public void windowIconified(WindowEvent e) {
 			}
 			@Override
 			public void windowOpened(WindowEvent e) {
 				agent.connect();
 			}
 		});
 		addWindowFocusListener(new WindowFocusListener() {
 			@Override
 			public void windowGainedFocus(WindowEvent arg0) {
 				nameField.requestFocusInWindow();
 			}
 			@Override
 			public void windowLostFocus(WindowEvent arg0) {
 				
 			}
 		});
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(null);
 		double width = screenDims.width/4;
 		Dimension panelDims = new Dimension((int)(width),(int)(width*ratio));
 		panel.setPreferredSize(panelDims);
 		panel.setMaximumSize(panelDims);
 		panel.setMinimumSize(panelDims);
 		
 		JLabel title = new JLabel("CaskChat");
 		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
 		title.setSize(140,25);
 		title.setLocation((panelDims.width - title.getWidth())/2,10);
 		panel.add(title);
 		
 		statusBox = new JLabel();
 		statusBox.setHorizontalAlignment(SwingConstants.CENTER);
 		statusBox.setSize(3*panelDims.width/4,20);
 		statusBox.setLocation((panelDims.width - statusBox.getWidth())/2,title.getY() + title.getHeight() + padding);
 		panel.add(statusBox);
 		
 		nameField = new JTextField();
 		nameField.setSize(100,20);
 		nameField.setLocation((panelDims.width - nameField.getWidth())/2,panelDims.height/2);
 		nameField.getDocument().addDocumentListener(new DocumentListener() {
 			public void changedUpdate(DocumentEvent e) {
 				nameChanged();
 			}
 			@Override
 			public void insertUpdate(DocumentEvent e) {
 				nameChanged();
 			}
 			@Override
 			public void removeUpdate(DocumentEvent e) {
 				nameChanged();
 			}
 		});
 		nameField.addActionListener(joinAction);
 		panel.add(nameField);
 		
 		nameStatus = new JLabel();
 		nameStatus.setHorizontalAlignment(SwingConstants.CENTER);
 		nameStatus.setSize(150,20);
 		nameStatus.setLocation((panelDims.width - nameStatus.getWidth())/2,nameField.getY() + nameField.getHeight() + padding);
 		panel.add(nameStatus);
 		
 		joinButton = new JButton("Join Chat");
 		joinButton.setSize(100,20);
 		joinButton.setLocation((panelDims.width - joinButton.getWidth())/2,3*panelDims.height/4);
 		joinButton.addActionListener(joinAction);
 		joinButton.setEnabled(false);
 		panel.add(joinButton);
 		
 		JLabel versionLabel = new JLabel("v"+Parameters.VERSION_ID);
 		versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		versionLabel.setSize(100,20);
 		versionLabel.setLocation((panelDims.width - versionLabel.getWidth())/2,panelDims.height - versionLabel.getHeight());
 		panel.add(versionLabel);
 		
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new BorderLayout());
 		add(panel,BorderLayout.CENTER);
 		pack();
		if (System.getProperty("os.name").contains("Windows"))
			setSize(getWidth()-10,getHeight()-10);
 		setResizable(false);
 		setLocationRelativeTo(null);
 		this.setVisible(true);
 		
 	}
 	
 	public void iconImageReceived(ImageIcon i) {
 		setIconImage(i.getImage());
 	}
 	
 	private void joinChat() {
 		nameField.setEditable(false);
 		agent.joinChat(name);
 	}
 	
 	public void joinDeniedCuzName() {
 		nameField.setEditable(true);
 		nameStatus.setText("Name unavailable");
 	}
 	
 	public void joinDenied() {
 		nameField.setEditable(true);
 		statusBox.setText("Unable to join for unknown reasons");
 	}
 	
 	public void joinGranted() {
 		ChatWindow w = new ChatWindow();
 		Connection c = agent.getConnection();
 		c.removeConnectionListener(agent);
 		ChatAgent a = new ChatAgent(name);
 		a.setConnection(c);
 		w.setAgent(a);
 		a.setChatWindow(w);
 		w.setTitle(name);
 		w.addMessage("Welcome to CaskChat!");
 		w.setIconImage(getIconImage());
 		dispose();
 	}
 	
 	public void connectionStatus(String s) {
 		statusBox.setText(s);
 	}
 	
 	private void nameChanged() {
 		joinButton.setEnabled(false);
 		String text = nameField.getText().trim();
 		name = text;
 		if (text.compareTo("") == 0) {
 			nameStatus.setText("");
 			return;
 		}
 		String message = null;
 		if ((message = checkName(text)) == null) {
 			name = text;
 			nameStatus.setText("");
 			checkNameAvailability(name);
 		} else 
 			nameStatus.setText(message);
 	}
 	
 	public void nameConfirmed(String s,boolean isAvail) {
 		if (s.compareTo(name) == 0) {
 			nameStatus.setText((isAvail)?"Name available!":"Name unavailable");
 			name = s;
 			joinButton.setEnabled(isAvail);
 			if (isAvail && joinChatPressed)
 				joinChat();
 			else
 				joinChatPressed = false;
 		} else 
 			joinButton.setEnabled(false);
 		
 	}
 	
 	private void checkNameAvailability(String s) {
 		if (agent.isConnected())
 			agent.requestNameAvailability(s);
 	}
 	
 	private String checkName(String s) {
 		if (s.compareTo("") == 0)
 			return "";
 		if (s.length() > 15)
 			return "Name is too long";
 		for (int i = 0; i < s.length(); i++) {
 			if (!checkChar(s.charAt(i)))
 				return "Only letters or numbers";
 		}
 		return null;
 	}
 	
 	private boolean checkChar(char c) {
 		return (c >= 48 && c <= 57) || (c >= 65 && c <= 90) || (c >= 97 && c <= 122) || c == ' ';
 	}
 	
 	public void hasConnected() {
 		if (nameField.getText().compareTo("") != 0) {
 			nameChanged();
 		}
 	}
 	
 }
