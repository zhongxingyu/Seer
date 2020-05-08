 package com.test9.irc.display;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 
 import com.test9.irc.engine.ReadServerConfig;
 
 @SuppressWarnings("unused")
 public class NewServerWindow extends JFrame implements ActionListener{
 	
 	private static final long serialVersionUID = 285475674231316918L;
 	private static final Toolkit KIT = Toolkit.getDefaultToolkit();	
 	private static double screenHeight = KIT.getScreenSize().getHeight();
 	private static double screenWidth = KIT.getScreenSize().getWidth();
 	
 	// ** General Tab ** //
 	private static JPanel generalPanel = new JPanel();
 
 	private static JTabbedPane tabbedPane = new JTabbedPane();
 	private static JTextField networkName = new JTextField();
 	private static JTextField server = new JTextField();
 	private static JTextField port = new JTextField();
 	private static JPasswordField serverPassword = new JPasswordField();
 	private static JTextField nickName = new JTextField();
 	private static JTextField loginName = new JTextField();
 	private static JTextField realName = new JTextField();
 	private static JPasswordField nickservPassword = new JPasswordField();
 	private static JTextField altNicks = new JTextField();
 	private static JPanel buttonPanel = new JPanel();
 	private static JCheckBox onStartupCheck = new JCheckBox("Connect on start up");
 	private static JCheckBox sslCheck = new JCheckBox("Use SSL");
 	// ** End General Tab ** //
 	
 	// ** Details Tab ** //
 	private static JPanel detailsPanel = new JPanel();
 	private static String[] encodingOptions = new String[]{
 		"UTF-8", "ISO-8859-1", "ISO-8859-15", "ISO-8859-2", "ISO-8859-7 (Greek)",
 		"ISO-8859-8 (Hebrew)", "ISO-8859-9 (Turkish)", "ISO-8859-11 (Thai)",
 		"ISO-2022-JP (Japanese)", "CP949 (Korean)", "EUC-KR (Korean)", "GBK (Chinese)",
 		"GB18030 (Chinese)", "BIG5 (Chinese)", "KOI8-U (Ukrainian)", "KOI8-R (Cyrillic)",
 		"CP1251 (Cyrillic)", "CP1256 (Arabic)", "CP1257 (Baltic)"
 	};
 	private static JTextField leavingComment = new JTextField();
 	private static JTextField ctcpUserInfo = new JTextField();
	private static JComboBox<String> encoding = new JComboBox<String>();
	private static JComboBox<String> fallbackEncoding = new JComboBox<String>();
 	// ** End Details Tab ** //
 	
 	private static JPanel onLoginPanel = new JPanel();
 	private static JPanel ignorePanel = new JPanel();
 	private static JButton cancel = new JButton("Cancel");
 	private static JButton ok = new JButton("OK");
 
 
 	
 	
 	public static void main(String args[])
 	{
 		NewServerWindow w = new NewServerWindow();
 	}
 	public NewServerWindow()
 	{
 		initGeneralPanel();
 		initDetailsPanel();
 		initOnLoginPanel();
 		initIgnorePanel();
 		
 		tabbedPane.add("General", generalPanel);
 		tabbedPane.add("Details", detailsPanel);
 		tabbedPane.add("On Login", onLoginPanel);
 		tabbedPane.add("Ignore", ignorePanel);
 
 		setTitle("New Server");
 		setResizable(false);
 		setSize(400,450);
 		setLayout(new BorderLayout());
 		cancel.addActionListener(this);
 		ok.addActionListener(this);
 		buttonPanel.add(cancel);
 		buttonPanel.add(ok);
 		add(buttonPanel, BorderLayout.SOUTH);
 		add(tabbedPane, BorderLayout.CENTER);
 		
 		//pack();
 		setVisible(true);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 	
 	private void initGeneralPanel() {
 		generalPanel.setLayout(new GridLayout(11,2));
 		generalPanel.setOpaque(true);
 		generalPanel.add(new JLabel("Network Name: ", JLabel.RIGHT));
 		generalPanel.add(networkName);
 		generalPanel.add(new JLabel());
 		generalPanel.add(onStartupCheck);
 		generalPanel.add(new JLabel("Server: ", JLabel.RIGHT));
 		generalPanel.add(server);
 		generalPanel.add(new JLabel("Port: ", JLabel.RIGHT));
 		generalPanel.add(port);
 		generalPanel.add(new JLabel());
 		generalPanel.add(sslCheck);
 		generalPanel.add(new JLabel("Server Password: ", JLabel.RIGHT));
 		generalPanel.add(serverPassword);
 		generalPanel.add(new JLabel("Nickname: ", JLabel.RIGHT));
 		generalPanel.add(nickName);
 		generalPanel.add(new JLabel("Login Name: ", JLabel.RIGHT));
 		generalPanel.add(loginName);
 		generalPanel.add(new JLabel("Real Name: ", JLabel.RIGHT));
 		generalPanel.add(realName);
 		generalPanel.add(new JLabel("Nickserv Password: ", JLabel.RIGHT));
 		generalPanel.add(nickservPassword);
 		generalPanel.add(new JLabel("Ald. Nicknames: ", JLabel.RIGHT));
 		generalPanel.add(altNicks);
 	}
 	
 	private void initDetailsPanel() {
 		
 		detailsPanel.setLayout(new GridLayout(4,2));
 		detailsPanel.add(new JLabel("Leaving Comment:", JLabel.RIGHT));
 		detailsPanel.add(leavingComment);
 		detailsPanel.add(new JLabel("CTCP User Info:", JLabel.RIGHT));
 		detailsPanel.add(ctcpUserInfo);
 		for(String e : encodingOptions)
 		{
 			encoding.addItem(e);
 			fallbackEncoding.addItem(e);
 		}
 		detailsPanel.add(new JLabel("Encoding:", JLabel.RIGHT));
 		detailsPanel.add(encoding);
 		detailsPanel.add(new JLabel("Fallback Encoding:", JLabel.RIGHT));
 		detailsPanel.add(fallbackEncoding);
 	}
 	
 	private void initOnLoginPanel() {
 		
 	}
 	
 	private void initIgnorePanel() {
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		ReadServerConfig rsc = new ReadServerConfig(networkName.getText()+".txt");
 		if(e.getSource() == ok) {
 			
 		} else if (e.getSource() == cancel) {
 			
 		}
 	}
 }
