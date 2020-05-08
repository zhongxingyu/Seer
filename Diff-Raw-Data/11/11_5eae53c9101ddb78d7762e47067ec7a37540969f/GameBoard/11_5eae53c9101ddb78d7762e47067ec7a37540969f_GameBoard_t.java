 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 public class GameBoard extends Applet {
 
 	private static final long serialVersionUID = 944417097790033967L;
     JButton identButton;
     JButton passwordButton;
     JButton hPortButton;
     JButton aliveButton;
     JButton gameIdentsButton;
     JButton changePasswordButton;
     JButton statusButton;
     JButton signOffButton;
     JButton quitButton;
     JButton makeCertButton;
     JButton playerHostPortButton;
     JButton randomHostPortButton;
     JButton declareWarButton;
     JButton warTruceOfferButton;
     JButton warStatusButton;
     JButton warTruceResponseButton;
     JButton warDefendButton;
     JButton getCertButton;
     JButton tradeRequestButton;
     JButton tradeResponeButton;
     JButton synthesizeButton;
     JButton serverConnectButton;
     JButton serverDisconnectButton;
     JButton clientConnectButton;
     JButton clientDisconnectButton;
     JButton autoRunButton;
     JButton statusCrack;
     JButton passwordCrack;
     JTextField identBlank1;
     JTextField identBlank2;
     JTextField passwordBlank;
     JTextField hPortArg1;
     JTextField hPortArg2;
     JTextField certBlank1;
     JTextField certBlank2;
     JTextField passwordArg;
     JTextField playerHostPortArg;
     JTextField getCertArg;
     JTextField tradeRequestArg;
     JTextField statusCrackIDArg;
     JTextField statusCrackResourcesArg;
     JTextField passwordCrackIDArg;
     JTextField passwordCrackResourcesArg;
     JTextField declareWarArg;
     JTextField warStatusArg;
     JTextField warDefendWeaponsArg;
     JTextField warDefendVehiclesArg;
     JTextField warTruceOfferArg;
     JComboBox truceResponseBox;
     JComboBox tradeResponseBox;
     JComboBox monitorBox;
     JComboBox monitorPortBox;
     JComboBox synthesizeResourceBox;
     JTextArea hostPortArg;
     JTextArea usernameArg;
     JTextArea loginPasswordArg;
     JTextArea clientLog;
     JTextArea serverLog;
     JTextField portTextField;
     JLabel appGlobalMessage;
     
     ActiveClient ac = null;
     Server server = null;
     CommandHandler ch = null;
     
     class Frame extends JFrame implements ActionListener {
 
         GameBoard gb;
         private static final long serialVersionUID = 1264061647495656599L;
         
 
         Frame(GameBoard gb) {
             this.gb = gb;
             setLayout(new BorderLayout());
         }
 
         void setUpControlPanel() {
             
             // Add login information to control panel
             JPanel tempPanel = new JPanel();
             tempPanel.setLayout(new GridLayout(5, 4));
             tempPanel.add(hostPortArg = new JTextArea());
             tempPanel.add(autoRunButton = new JButton("Auto Run"));
             autoRunButton.setToolTipText("Start program in automatic mode using selected monitor and login parameters. NOTE: requires no manual interaction.");
             tempPanel.add(monitorBox = new JComboBox());
             monitorBox.addItem("helios.ececs.uc.edu");
             monitorBox.addItem("gauss.ececs.uc.edu");
             monitorBox.addItem("localhost");
             monitorBox.setSelectedIndex(2);
             tempPanel.add(usernameArg = new JTextArea());
             tempPanel.add(new JLabel("Host Port", JLabel.CENTER));
             tempPanel.add(new JLabel("  "));
             tempPanel.add(new JLabel("Monitor Name", JLabel.CENTER));
             tempPanel.add(new JLabel("User Name", JLabel.CENTER));
             tempPanel.add(serverConnectButton = new JButton("Connect"));
             serverConnectButton.setBackground(Color.red);
             tempPanel.add(clientConnectButton = new JButton("Connect"));
             clientConnectButton.setBackground(Color.red);
             tempPanel.add(new JLabel("  "));
             tempPanel.add(new JLabel("  "));
             tempPanel.add(serverDisconnectButton = new JButton("Disconnect"));
             tempPanel.add(clientDisconnectButton = new JButton("Disconnect"));
             tempPanel.add(monitorPortBox = new JComboBox());
             monitorPortBox.addItem("8180");
             monitorPortBox.addItem("8160");
             tempPanel.add(loginPasswordArg = new JTextArea());
             tempPanel.add(new JLabel("Server", JLabel.CENTER));
             tempPanel.add(new JLabel("Client", JLabel.CENTER));
             tempPanel.add(new JLabel("Monitor Port", JLabel.CENTER));
             tempPanel.add(new JLabel("Password", JLabel.CENTER));
             add("North", tempPanel);
             
             // Add command components to the control panel
             tempPanel = new JPanel();
             tempPanel.setLayout(new GridLayout(20, 3));
             //tempPanel.add(encryptButton = new JButton("Start Encryption"));
             //tempPanel.add(new JLabel("  "));
             //tempPanel.add(new JLabel("  "));
             tempPanel.add(identButton = new JButton("IDENT"));
             tempPanel.add(identBlank1 = new JTextField(15));
             identBlank1.setEditable(false);
             tempPanel.add(identBlank2 = new JTextField(15));
             identBlank2.setEditable(false);
             tempPanel.add(passwordButton = new JButton("PASSWORD"));
             tempPanel.add(passwordBlank = new JTextField(15));
             passwordBlank.setEditable(false);
             tempPanel.add(statusButton = new JButton("PLAYER_STATUS"));
             tempPanel.add(hPortButton = new JButton("HOST_PORT"));
             tempPanel.add(hPortArg1 = new JTextField(15));
             tempPanel.add(hPortArg2 = new JTextField(15));
             hPortArg1.setEditable(true);
             hPortArg2.setEditable(true);
             tempPanel.add(changePasswordButton = new JButton("CHANGE_PASSWORD"));
             tempPanel.add(passwordArg = new JTextField(15));
             tempPanel.add(quitButton = new JButton("QUIT"));
             tempPanel.add(aliveButton = new JButton("ALIVE"));
             tempPanel.add(gameIdentsButton = new JButton("GET_GAME_IDENTS"));
             tempPanel.add(signOffButton = new JButton("SIGN_OFF"));
             tempPanel.add(new JLabel("  "));
             tempPanel.add(new JLabel("  "));
             tempPanel.add(new JLabel("  "));
             tempPanel.add(synthesizeButton = new JButton("SYNTHESIZE"));
             tempPanel.add(synthesizeResourceBox = new JComboBox());
             synthesizeResourceBox.addItem("WEAPONS");
             synthesizeResourceBox.addItem("COMPUTERS");
             synthesizeResourceBox.addItem("VEHICLES");
            tempPanel.add(new JLabel(" "));
             tempPanel.add(getCertButton = new JButton("GET_CERTIFICATE"));
             tempPanel.add(getCertArg = new JTextField(15));
             tempPanel.add(makeCertButton = new JButton("MAKE_CERTIFICATE"));
             tempPanel.add(playerHostPortButton = new JButton("PLAYER_HOST_PORT"));
             tempPanel.add(playerHostPortArg = new JTextField(15));
             tempPanel.add(randomHostPortButton = new JButton("RANDOM_PLAYER_HOST_PORT"));
             tempPanel.add(tradeRequestButton = new JButton("TRADE_REQUEST"));
             tempPanel.add(tradeRequestArg = new JTextField(15));
             tempPanel.add(new JLabel("  "));
             tradeRequestArg.setToolTipText("Format: <resource_to> <resource_to_amount> for <Player 2> <resource_from> <resource_from_amount>");
             tempPanel.add(tradeResponeButton = new JButton("TRADE_RESPONSE"));
             tempPanel.add(tradeResponseBox = new JComboBox());
             tradeResponseBox.addItem("Accept");
             tradeResponseBox.addItem("Decline");
             tempPanel.add(new JLabel("  "));
             tempPanel.add(new JLabel("  "));
             tempPanel.add(new JLabel("  "));
             tempPanel.add(new JLabel("  "));
             tempPanel.add(declareWarButton = new JButton("WAR_DECLARE"));
             tempPanel.add(declareWarArg = new JTextField(15));
             declareWarArg.setToolTipText("<Player to wage war against> <Enemy Hostname> <Enemy Port> <No. Weapons to Commit> <No. Vehicles to Commit>");
             tempPanel.add(new JLabel(" " ));
             tempPanel.add(warStatusButton = new JButton("WAR_STATUS"));
             tempPanel.add(warStatusArg = new JTextField(15));
             tempPanel.add(new JLabel(" "));
             warStatusButton.setToolTipText("Get status of current war");
             tempPanel.add(warDefendButton = new JButton("WAR_DEFEND"));
             tempPanel.add(warDefendWeaponsArg = new JTextField(15));
             tempPanel.add(warDefendVehiclesArg = new JTextField(15));
             warDefendWeaponsArg.setToolTipText("Number of Weapons to commit");
             warDefendVehiclesArg.setToolTipText("Number of Vehicles to commit");
             
             tempPanel.add(warTruceOfferButton = new JButton("WAR_TRUCE_OFFER"));
             tempPanel.add(warTruceOfferArg = new JTextField(15));
             warTruceOfferArg.setToolTipText("<Player 1> to <Player 2> <resource 1> <resource 1 amount> ...");
             tempPanel.add(new JLabel("  "));
             tempPanel.add(warTruceResponseButton = new JButton("WAR_TRUCE_RESPONSE"));
             tempPanel.add(truceResponseBox = new JComboBox());
             truceResponseBox.addItem("Accept");
             truceResponseBox.addItem("Decline");
             tempPanel.add(new JLabel("  "));
             tempPanel.add(new JLabel("  "));
             tempPanel.add(new JLabel("  "));
             tempPanel.add(new JLabel("  "));
             tempPanel.add(statusCrack = new JButton("Player Status Crack"));
             tempPanel.add(statusCrackIDArg = new JTextField(15));
             tempPanel.add(statusCrackResourcesArg = new JTextField(15));
             statusCrackIDArg.setToolTipText("Player ID");
             statusCrackResourcesArg.setToolTipText("Number of computer resources to commit (will not get resources back no matter status of crack)");
             tempPanel.add(passwordCrack = new JButton("Player Monitor Password Crack"));
             tempPanel.add(passwordCrackIDArg = new JTextField(15));
             tempPanel.add(passwordCrackResourcesArg = new JTextField(15));
             passwordCrackIDArg.setToolTipText("Player ID");
             passwordCrackResourcesArg.setToolTipText("Number of computer resources to commit (will not get resources back no matter status of crack)");
             
             JPanel tempPanel2 = new JPanel();
             tempPanel2.setLayout(new BorderLayout());
             tempPanel2.add("Center", tempPanel);
             tempPanel2.add("South", appGlobalMessage = new JLabel(" "));
             appGlobalMessage.setForeground(Color.RED);
             add("South", tempPanel2);
             
             // Add client and server log windows
             tempPanel = new JPanel();
             tempPanel.setLayout(new BorderLayout());
             tempPanel.add("Center", new JScrollPane(clientLog = new JTextArea(18,40)));
             clientLog.setEnabled(false);
             tempPanel.add("South", new JLabel("Active Client Log\n\n\n", 0));
             add("West", tempPanel);
             tempPanel = new JPanel();
             tempPanel.setLayout(new BorderLayout());
             tempPanel.add("Center", new JScrollPane(serverLog = new JTextArea(18,40)));
             serverLog.setEnabled(false);
             tempPanel.add("South", new JLabel("Passive Server Log\n\n\n", 0));
             add("East", tempPanel);
 
             // Add action listeners for all buttons
             //encryptButton.addActionListener(this);
             identButton.addActionListener(this);
             passwordButton.addActionListener(this);
             hPortButton.addActionListener(this);
             aliveButton.addActionListener(this);
             gameIdentsButton.addActionListener(this);
             changePasswordButton.addActionListener(this);
             statusButton.addActionListener(this);
             signOffButton.addActionListener(this);
             quitButton.addActionListener(this);
             makeCertButton.addActionListener(this);
             playerHostPortButton.addActionListener(this);
             randomHostPortButton.addActionListener(this);
             declareWarButton.addActionListener(this);
             warTruceOfferButton.addActionListener(this);
             warStatusButton.addActionListener(this);
             warTruceResponseButton.addActionListener(this);
             warDefendButton.addActionListener(this);
             getCertButton.addActionListener(this);
             tradeRequestButton.addActionListener(this);
             tradeResponeButton.addActionListener(this);
             synthesizeButton.addActionListener(this);
             serverConnectButton.addActionListener(this);
             serverDisconnectButton.addActionListener(this);
             clientConnectButton.addActionListener(this);
             clientDisconnectButton.addActionListener(this);
             autoRunButton.addActionListener(this);
             statusCrack.addActionListener(this);
             passwordCrack.addActionListener(this);
 
             ch = new CommandHandler();
         }
         
         public void actionPerformed(ActionEvent e) {
             appGlobalMessage.setText( " " );
         	try
         	{
                 if ( e.getSource() == autoRunButton ) {
                     // TODO: start auto run of program
                 } else if ( e.getSource() == serverConnectButton ) {
                     Integer hostPort = Integer.valueOf(hostPortArg.getText());
                     Integer monitorPort = Integer.valueOf(monitorPortBox.getSelectedItem().toString());
                     if ( server == null || server.mbServerRunning == false ) {
                         System.out.println("Starting server: " + monitorPort + " <-> " + hostPort);
                         server = new Server(gb, monitorPort, hostPort, usernameArg.getText(), loginPasswordArg.getText());
                     }
                     if ( !server.connected ) {
                         server.start();
                         server.connected = true;
                         serverConnectButton.setBackground(Color.green);
                     }
                 } else if ( e.getSource() == clientConnectButton ) {
                     if ( ac == null ) {
                         Integer hostPort = Integer.valueOf(hostPortArg.getText());
                         Integer monitorPort = Integer.valueOf(monitorPortBox.getSelectedItem().toString());
 
                         ac = new ActiveClient(gb, monitorBox.getSelectedItem().toString(), monitorPort, hostPort, 0, usernameArg.getText(), loginPasswordArg.getText());
                     }
                     if ( !ac.connected ) {
                         ac.start();
                         ac.connected = true;
                         clientConnectButton.setBackground(Color.green);
                     }
                 } else if ( e.getSource() == serverDisconnectButton ) {
                     if ( server.connected ) {
                         serverConnectButton.setBackground(Color.red);
                         server.mbServerRunning = false;
                     }
                 } else if ( e.getSource() == clientDisconnectButton ) {
                     if ( ac != null && ac.connected ) {
                         ch.SetCommand("QUIT");
                         ac.running = false;
                         ac = null;
                         clientConnectButton.setBackground(Color.red);
                     }
                 } else if ( e.getSource() == identButton ) {
                     ch.SetCommand("IDENT");
                 } else if ( e.getSource() == passwordButton ) {
                     ch.SetCommand("PASSWORD");
                     if ( GlobalData.GetPassword() == null ) {
                         GlobalData.SetPassword(loginPasswordArg.getText());
                     }
                     passwordBlank.setText(GlobalData.GetPassword());
                 } else if ( e.getSource() == hPortButton ) {
                     ch.SetCommand("HOST_PORT");
                 } else if ( e.getSource() == aliveButton ) {
                     ch.SetCommand("ALIVE");
                 } else if ( e.getSource() == gameIdentsButton ) {
                     ch.SetCommand("GET_GAME_IDENTS");
                 } else if ( e.getSource() == changePasswordButton ) {
                     passwordBlank.setText(passwordArg.getText());
                     ch.SetCommand("CHANGE_PASSWORD");
                 } else if ( e.getSource() == statusButton ) {
                     ch.SetCommand("PLAYER_STATUS");
                 } else if ( e.getSource() == signOffButton ) {
                     ch.SetCommand("SIGN_OFF");
                 } else if ( e.getSource() == quitButton ) {
                     ch.SetCommand("QUIT");
                 } else if ( e.getSource() == makeCertButton ) {
                     ch.SetCommand("MAKE_CERTIFICATE");
                 } else if ( e.getSource() == playerHostPortButton ) {
                     ch.SetCommand("HOST_PORT");
                 } else if ( e.getSource() == randomHostPortButton ) {
                     ch.SetCommand("RANDOM_PLAYER_HOST_PORT");
                 } else if ( e.getSource() == declareWarButton ) {
                     ch.SetCommand("WAR_DECLARE");
                 } else if ( e.getSource() == warTruceOfferButton ) {
                     ch.SetCommand("WAR_TRUCE_OFFER");
                 } else if ( e.getSource() == warStatusButton ) {
                     ch.SetCommand("WAR_STATUS");
                 } else if ( e.getSource() == warTruceResponseButton ) {
                     ch.SetCommand("WAR_TRUCE_RESPONSE");
                 } else if ( e.getSource() == warDefendButton ) {
                     ch.SetCommand("WAR_DEFEND");
                     warDefendButton.setBackground(Color.LIGHT_GRAY);
                 } else if ( e.getSource() == getCertButton ) {
                     ch.SetCommand("GET_CERTIFICATE");
                 } else if ( e.getSource() == tradeRequestButton ) {
                     ch.SetCommand("TRADE_REQUEST");
                 } else if ( e.getSource() == tradeResponeButton ) {
                     ch.SetCommand("TRADE_RESPONSE");
                 } else if ( e.getSource() == synthesizeButton ) {
                     ch.SetCommand("SYNTHESIZE");
                 }
             }
             catch ( Exception ex )
             {
                 ex.printStackTrace();
                 appGlobalMessage.setText( ex.toString() );
             }
         }
     }
 
     public String GetCommand(String command) {
         String commandString = "none";
         //TODO: finish remaining commands.
         if ( command.equals("IDENT") ) {
             //TODO: (deferred) sPull out special case stuff for the IDENT command from Execute() (in MessageParser.java) and put it here
             commandString = command;
         } else if ( command.equals("PASSWORD")) {
             commandString = command + " " + GlobalData.GetPassword();
         } else if ( command.equals("ALIVE") ) {
             System.out.println("About to send: " + GlobalData.GetCookie());
             commandString = command + " " + GlobalData.GetCookie();
         } else if ( command.equals("HOST_PORT") ) {
             commandString = command + " " + hPortArg1.getText() + " " + hPortArg2.getText();
         } else if ( command.equals("MAKE_CERTIFICATE") ) {
             // TODO
         } else if ( command.equals("QUIT") ) {
             commandString = command;
         } else if ( command.equals("PLAYER_STATUS") ) {
             commandString = command;
         } else if ( command.equals("SIGN_OFF") ) {
             commandString = command;
         } else if ( command.equals("CHANGE_PASSWORD") ) {
             commandString = command + " " + GlobalData.GetPassword() + " " + passwordArg.getText();
         } else if ( command.equals("GET_CERTIFICATE") ) {
             commandString = command + " " + getCertArg.getText();
         } else if ( command.equals("SYNTHESIZE") ) {
            commandString = command + " " + synthesizeResourceBox.getSelectedItem();
         } else if ( command.equals("TRADE_REQUEST") ) {
            commandString = command + " " + usernameArg.getText() + " " + tradeRequestArg.getText();
         } else if ( command.equals("TRADE_RESPONSE") ) {
             commandString = command + " " + tradeResponseBox.getSelectedItem();
         } else if ( command.equals("WAR_DECLARE") ) {
             commandString = command + " " + declareWarArg.getText();
         } else if ( command.equals("WAR_DEFEND") ) {
             commandString = command + " " + warDefendWeaponsArg.getText() + " " + warDefendVehiclesArg.getText();
         } else if ( command.equals("WAR_TRUCE_OFFER") ) {
             commandString = command + " " + warTruceOfferArg.getText();
         } else if ( command.equals("WAR_TRUCE_RESPONSE") ) {
             commandString = command + " " + truceResponseBox.getSelectedItem();
         } else if ( command.equals("WAR_STATUS") ) {
             commandString = command + " " + warStatusArg.getText();
         } else if ( command.equals("GET_GAME_IDENTS") ) {
             commandString = command;
         } else if ( command.equals("RANDOM_PLAYER_HOST_PORT") ) {
             commandString = command;
         } else if ( command.equals("PLAYER_STATUS_CRACK") ) {
             commandString = command + " " + statusCrackIDArg.getText() + " " + statusCrackResourcesArg.getText();
         } else if ( command.equals("PLAYER_MONITOR_PASSWORD_CRACK") ) {
             commandString = command + " " + passwordCrackIDArg.getText() + " " + passwordCrackResourcesArg.getText();
         }
         return commandString;
     }
 
     public void init() {
         Frame frame = new Frame(this);
         frame.setUpControlPanel();
         frame.pack();
         frame.setVisible(true);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     }
 }
