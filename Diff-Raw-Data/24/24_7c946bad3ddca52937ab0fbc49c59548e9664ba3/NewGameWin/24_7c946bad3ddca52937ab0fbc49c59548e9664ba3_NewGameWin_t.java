 //import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import java.awt.Font;
 import javax.swing.JSpinner;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 
 
 public class NewGameWin extends JFrame {
 
 	/**
 	 *  
 	 */
 	private static final long serialVersionUID = 137750314898293920L;
 	private JPanel contentPane;
 	private final JButton btnExit = new JButton("Exit");
 	private JComboBox<String> comboLocation;
 	private JLabel lblGameType;
 	private JComboBox<String> comboType;
 	private JLabel lblNewLabel;
 	private JLabel lblRows;
 	private JSpinner spinnerRows;
 	private JLabel lblColumns;
 	private JSpinner spinnerCols;
 	private JRadioButton noTimerButton;
 	private JRadioButton yesTimerButton;
 	private JRadioButton hostAsP1Button;
 	private JRadioButton hostAsP2Button;
 	private JRadioButton hostAsHumanButton;
 	private JRadioButton hostAsCpuButton;
 	private ButtonGroup timerSelection;
 	private ButtonGroup playerSelection;
 	private ButtonGroup humanCpuSelection;
 	private JLabel lblTimer;
 	private JLabel lblSetTime;
 	private JLabel lblMilliseconds;
 	private int timerButtonSelected = -1;
 	private int playerNumber = -1;
 	private int isHuman = -1;
 	private JSpinner spinnerTimer;
 	private JLabel lblPort;
 	private JSpinner spinnerPort;
 	private JLabel lblIpAddress;
 	private JTextField IP1;
 	private JTextField IP2;
 	private JTextField IP3;
 	private JTextField IP4;
 
 	/**
 	 * Launch the application.
 	 */
 	
 
 	/**
 	 * Create the frame.
 	 */
 	public NewGameWin() {
 		initGUI();
 	}
 	private void initGUI() {
 		setTitle("New Game");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);    
 		int width = 475;
 	    int height = 350;
 	    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 	    int x = (screen.width - width) / 2;
 	    int y = (screen.height - height) / 2;
 	    setBounds(x, y, width, height);
 	    this.setResizable(false);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		btnExit.setBounds(16, 35, 428, 29);
 		btnExit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 		contentPane.setLayout(null);
 		
 		contentPane.add(btnExit);
 		
 		JButton btnNewButton_2 = new JButton("Back to Main Menu");
 		btnNewButton_2.setBounds(16, 6, 428, 29);
 		btnNewButton_2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Welcome_Menu win = new Welcome_Menu();
 				win.setVisible(true);
 				dispose();
 			}
 		});
 		contentPane.add(btnNewButton_2);
 
 		String[] gameTypes = new String[] {"--Select--", "Player 1 vs. Player 2", "Player vs. CPU", "CPU vs. CPU"};
 		comboType = new JComboBox<String>(gameTypes);
 		comboType.setBounds(107, 111, 175, 27);
 		comboType.setToolTipText("");
 		contentPane.add(comboType);
 		comboType.setVisible(false);
 		comboType.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)  {
 				if (comboType.getSelectedIndex() == 1) { //P v P
 					hostAsHumanButton.setVisible(false);
 					hostAsCpuButton.setVisible(false);
 					if (comboLocation.getSelectedIndex() == 3) { //host 
 						hostAsP1Button.setVisible(true);
 						hostAsP2Button.setVisible(true);
 					}
 					else {
 						hostAsP1Button.setVisible(false);
 						hostAsP2Button.setVisible(false);
 					}
 				}
 				else if (comboType.getSelectedIndex() == 2) { //P v C
 					hostAsP1Button.setVisible(true);
 					hostAsP2Button.setVisible(true);
 					if (comboLocation.getSelectedIndex() == 3) { //host
 						hostAsHumanButton.setVisible(true);
 						hostAsCpuButton.setVisible(true);
 					}
 					else {
 						hostAsHumanButton.setVisible(false);
 						hostAsCpuButton.setVisible(false);
 					}
 				}
 				else if (comboType.getSelectedIndex() == 3) { //C v C
 					hostAsHumanButton.setVisible(false);
 					hostAsCpuButton.setVisible(false);
 					if (comboLocation.getSelectedIndex() == 3) { //host
 						hostAsP1Button.setVisible(true);
 						hostAsP2Button.setVisible(true);
 					}
 					else {
 						hostAsP1Button.setVisible(false);
 						hostAsP2Button.setVisible(false);
 					}
 				}
 				else {
 					hostAsP1Button.setVisible(false);
 					hostAsP2Button.setVisible(false);
 					hostAsHumanButton.setVisible(false);
 					hostAsCpuButton.setVisible(false);
 				}
 			}
 		});
 		
 		JLabel lblGameLocation = new JLabel("Game Location");
 		lblGameLocation.setBounds(26, 80, 100, 16);
 		lblGameLocation.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 		contentPane.add(lblGameLocation);
 		
 		String[]gameLocations = new String[] {"--Select--", "New Local Game", "Connect to Game", "Host Game"};
 		comboLocation = new JComboBox<String>(gameLocations);
 		comboLocation.setBounds(134, 76, 175, 27);
 		comboLocation.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int selIndexLoc = comboLocation.getSelectedIndex();
 				if (selIndexLoc == 0) {
 					lblGameType.setVisible(false);
 					comboType.setSelectedIndex(0);
 					comboType.setVisible(false);
 					lblNewLabel.setVisible(false);
 					lblRows.setVisible(false);
 					lblColumns.setVisible(false);
 					spinnerRows.setVisible(false);
 					spinnerCols.setVisible(false);
 					lblTimer.setVisible(false);
 					noTimerButton.setVisible(false);
 					yesTimerButton.setVisible(false);
 					//hostAsP1Button.setVisible(false);
 					//hostAsP2Button.setVisible(false);
 					lblSetTime.setVisible(false);
 					lblMilliseconds.setVisible(false);
 					spinnerTimer.setVisible(false);
 					lblPort.setVisible(false);
 					spinnerPort.setVisible(false);
 					lblIpAddress.setVisible(false);
 					IP1.setVisible(false);
 					IP2.setVisible(false);
 					IP3.setVisible(false);
 					IP4.setVisible(false);
 					hostAsP1Button.setVisible(false);
 					hostAsP2Button.setVisible(false);
 					hostAsHumanButton.setVisible(false);
 					hostAsCpuButton.setVisible(false);
 				}
 				else if (selIndexLoc == 1) { //local game
 					lblGameType.setVisible(true);
 					comboType.setSelectedIndex(0);
 					comboType.setVisible(true);
 					lblNewLabel.setVisible(true);
 					lblRows.setVisible(true);
 					lblColumns.setVisible(true);
 					spinnerRows.setVisible(true);
 					spinnerCols.setVisible(true);
 					lblTimer.setVisible(true);
 					noTimerButton.setVisible(true);
 					yesTimerButton.setVisible(true);
 					lblPort.setVisible(false);
 					spinnerPort.setVisible(false);
 					lblIpAddress.setVisible(false);
 					IP1.setVisible(false);
 					IP2.setVisible(false);
 					IP3.setVisible(false);
 					IP4.setVisible(false);
 				}
 				else if (selIndexLoc == 2) { //connect to game
 					lblGameType.setVisible(false);
 					comboType.setSelectedIndex(0);
 					comboType.setVisible(false);
 					lblNewLabel.setVisible(false);
 					lblRows.setVisible(false);
 					lblColumns.setVisible(false);
 					spinnerRows.setVisible(false);
 					spinnerCols.setVisible(false);
 					lblTimer.setVisible(false);
 					noTimerButton.setVisible(false);
 					yesTimerButton.setVisible(false);
 					lblSetTime.setVisible(false);
 					lblMilliseconds.setVisible(false);
 					spinnerTimer.setVisible(false);
 					timerSelection.clearSelection();
 					lblIpAddress.setVisible(true);
 					IP1.setVisible(true);
 					IP2.setVisible(true);
 					IP3.setVisible(true);
 					IP4.setVisible(true);
 					lblPort.setVisible(true);
 					spinnerPort.setVisible(true);
 					hostAsP1Button.setVisible(false);
 					hostAsP2Button.setVisible(false);
 					hostAsHumanButton.setVisible(false);
 					hostAsCpuButton.setVisible(false);
 				}
 				else if (selIndexLoc == 3) { //host game
 					lblGameType.setVisible(true);
 					comboType.setSelectedIndex(0);
 					comboType.setVisible(true);
 					lblNewLabel.setVisible(true);
 					lblRows.setVisible(true);
 					lblColumns.setVisible(true);
 					spinnerRows.setVisible(true);
 					spinnerCols.setVisible(true);
 					lblTimer.setVisible(true);
 					noTimerButton.setVisible(true);
 					yesTimerButton.setVisible(true);
 					lblIpAddress.setVisible(false);
 					IP1.setVisible(false);
 					IP2.setVisible(false);
 					IP3.setVisible(false);
 					IP4.setVisible(false);
 					lblPort.setVisible(true);
 					spinnerPort.setVisible(true);
 				}	
 			}
 		});
 		comboLocation.setToolTipText("");
 		contentPane.add(comboLocation);
 		
 		lblGameType = new JLabel("Game Type");
 		lblGameType.setBounds(26, 115, 79, 16);
 		lblGameType.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 		contentPane.add(lblGameType);
 		lblGameType.setVisible(false);
 		
 		lblNewLabel = new JLabel("Board Size");
 		lblNewLabel.setBounds(26, 159, 79, 16);
 		lblNewLabel.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 		contentPane.add(lblNewLabel);
 		lblNewLabel.setVisible(false);
 		
 		lblRows = new JLabel("Rows");
 		lblRows.setBounds(117, 159, 39, 16);
 		contentPane.add(lblRows);
 		lblRows.setVisible(false);
 		
 		lblColumns = new JLabel("Columns");
 		lblColumns.setBounds(218, 159, 60, 16);
 		contentPane.add(lblColumns);
 		lblColumns.setVisible(false);
 		
 		SpinnerNumberModel modelRows = new SpinnerNumberModel(5,1,14,2);
 		spinnerRows = new JSpinner(modelRows);
 		spinnerRows.setBounds(157, 153, 53, 28);
 		contentPane.add(spinnerRows);
 		spinnerRows.setVisible(false);
 		
 		SpinnerNumberModel modelCols = new SpinnerNumberModel(9,1,14,2);
 		spinnerCols = new JSpinner(modelCols);
 		spinnerCols.setBounds(279, 153, 53, 28);
 		contentPane.add(spinnerCols);
 		spinnerCols.setVisible(false);
 		
 		lblTimer = new JLabel("Timer?");
 		lblTimer.setBounds(26, 208, 61, 16);
 		lblTimer.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 		contentPane.add(lblTimer);
 		lblTimer.setVisible(false);
 		
 		lblSetTime = new JLabel("Set time:");
 		lblSetTime.setBounds(218, 208, 61, 16);
 		contentPane.add(lblSetTime);
 		lblSetTime.setVisible(false);
 		
 		SpinnerNumberModel modelTimer = new SpinnerNumberModel(25000,1000,null,1);
 		spinnerTimer = new JSpinner(modelTimer);
 		spinnerTimer.setBounds(279, 202, 79, 28);
 		contentPane.add(spinnerTimer);
 		spinnerTimer.setVisible(false);
 		
 		lblMilliseconds = new JLabel("milliseconds");
 		lblMilliseconds.setBounds(370, 208, 85, 16);
 		contentPane.add(lblMilliseconds);
 		lblMilliseconds.setVisible(false);
 		
 		noTimerButton = new JRadioButton("No");
 		noTimerButton.setBounds(84, 204, 60, 23);
 		noTimerButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				lblSetTime.setVisible(false);
 				spinnerTimer.setVisible(false);
 				lblMilliseconds.setVisible(false);
 				timerButtonSelected = 0;
 			}
 		});
 		contentPane.add(noTimerButton);
 		noTimerButton.setVisible(false);
 		
 		
 		yesTimerButton = new JRadioButton("Yes");
 		yesTimerButton.setBounds(157, 204, 60, 23);
 		yesTimerButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				lblSetTime.setVisible(true);
 				spinnerTimer.setVisible(true);
 				lblMilliseconds.setVisible(true);
 				timerButtonSelected = 1;
 			}
 		});
 		contentPane.add(yesTimerButton);
 		yesTimerButton.setVisible(false);
 		
 		timerSelection = new ButtonGroup();
 		timerSelection.add(noTimerButton);
 		timerSelection.add(yesTimerButton);
 		
 		hostAsP1Button = new JRadioButton("P1");
 		hostAsP1Button.setBounds(279, 114, 47, 18);
 		contentPane.add(hostAsP1Button);
 		hostAsP1Button.setVisible(false);
 		hostAsP1Button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				playerNumber = 1;
 			}
 		});
 		
 		hostAsP2Button = new JRadioButton("P2");
 		hostAsP2Button.setBounds(323, 108, 47, 29);
 		contentPane.add(hostAsP2Button);
 		hostAsP2Button.setVisible(false);
 		hostAsP2Button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				playerNumber = 2;
 			}
 		});
 		
 		playerSelection = new ButtonGroup(); 
 		playerSelection.add(hostAsP1Button);
 		playerSelection.add(hostAsP2Button);
 		
 		hostAsHumanButton = new JRadioButton("Human");
 		hostAsHumanButton.setBounds(370, 113, 85, 20);
 		contentPane.add(hostAsHumanButton);
 		hostAsHumanButton.setVisible(false);
 		hostAsHumanButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				isHuman = 1;
 			}
 		});
 		
 		hostAsCpuButton = new JRadioButton("CPU");
 		hostAsCpuButton.setBounds(370, 140, 69, 19);
 		contentPane.add(hostAsCpuButton);
 		hostAsCpuButton.setVisible(false);
 		hostAsCpuButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				isHuman = 0;
 			}
 		});
 		
 		humanCpuSelection = new ButtonGroup();
 		humanCpuSelection.add(hostAsHumanButton);
 		humanCpuSelection.add(hostAsCpuButton);
 		
 		JButton btnCreateNewGame = new JButton("Create New Game");
 		btnCreateNewGame.setBounds(71, 273, 319, 29);
 		btnCreateNewGame.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				int selIndexLoc = comboLocation.getSelectedIndex();
 				if (selIndexLoc == 1) { //local game
 					boolean playerNeeded = true;
 					if (comboType.getSelectedIndex() == 2 && playerNumber == -1)
 						playerNeeded = false;
 					if (timerButtonSelected > -1 && comboType.getSelectedIndex() > 0 && playerNeeded) {
 
						int gameType = comboType.getSelectedIndex(); //1 - PvP; 2 - PvC; 3 - CvC
 						int numRows = (int) spinnerRows.getValue();
 						int numCols = (int) spinnerCols.getValue();
 						int timerLength = -1;
 						if (timerButtonSelected == 1) 
 							if (spinnerTimer.isVisible() && (int) spinnerTimer.getValue() > 0) 
 								timerLength = (int) spinnerTimer.getValue();	
 						Fanorona newGame = new Fanorona(playerNumber, gameType, numRows, numCols, timerLength);
 						newGame.setVisible(true);
 						dispose();
 					}
 				}
 				else if (selIndexLoc == 2) { //connect to game (client)
 					String address = IP1.getText() + "." + IP2.getText() + "." + IP3.getText() + "." + IP4.getText();
 					int port = (int) spinnerPort.getValue();
 					Thread cliThread = new Thread(new Client(playerNumber, address, port));
 					cliThread.start();
 					dispose();
 				
 				}
 				else if (selIndexLoc == 3) { //host a game (server)
 					boolean humanCPUNeeded = true;
 					if (comboType.getSelectedIndex() == 2 && isHuman == -1)
 						humanCPUNeeded = false;
 					if (timerButtonSelected > -1 && comboType.getSelectedIndex() > 0 && playerNumber > -1 && humanCPUNeeded) {
						int gameType = comboType.getSelectedIndex(); //1 - PvP; 2- PvC; 3 - CvC
 						int numRows = (int) spinnerRows.getValue();
 						int numCols = (int) spinnerCols.getValue();
 						int timerLength = -1;
 						int port = (int) spinnerPort.getValue();
 						if (timerButtonSelected == 1) 
 							if (spinnerTimer.isVisible() && (int) spinnerTimer.getValue() > 0) 
 								timerLength = (int) spinnerTimer.getValue();
 						Thread servThread = new Thread(new Server(playerNumber, gameType, numRows, numCols, timerLength, port));
 						servThread.start();
 						dispose();
 					}
 				}
 			}
 		});
 		contentPane.add(btnCreateNewGame);	
 		
 		lblPort = new JLabel("Port #:");
 		lblPort.setBounds(26, 245, 61, 16);
 		lblPort.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 		contentPane.add(lblPort);
 		lblPort.setVisible(false);
 		
 		SpinnerNumberModel modelPort = new SpinnerNumberModel(1024,1024,65536,1);
 		spinnerPort = new JSpinner(modelPort);
 		spinnerPort.setBounds(78, 239, 78, 28);
 		contentPane.add(spinnerPort);
 		spinnerPort.setVisible(false);
 		
 		lblIpAddress = new JLabel("I.P. Address             .           .           .");
 		lblIpAddress.setBounds(167, 245, 277, 16);
 		lblIpAddress.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 		contentPane.add(lblIpAddress);
 		lblIpAddress.setVisible(false);
 		
 		IP1 = new JTextField();
 		IP1.setBounds(256, 239, 39, 28);
 		IP1.setText("000");
 		contentPane.add(IP1);
 		IP1.setColumns(10);
 		IP1.setVisible(false);
 		
 		IP2 = new JTextField();
 		IP2.setBounds(304, 239, 39, 28);
 		IP2.setText("000");
 		IP2.setColumns(10);
 		contentPane.add(IP2);
 		IP2.setVisible(false);
 		
 		IP3 = new JTextField();
 		IP3.setBounds(351, 239, 39, 28);
 		IP3.setText("000");
 		IP3.setColumns(10);
 		contentPane.add(IP3);
 		IP3.setVisible(false);
 		
 		IP4 = new JTextField();
 		IP4.setBounds(397, 239, 39, 28);
 		IP4.setText("000");
 		IP4.setColumns(10);
 		contentPane.add(IP4);
 		IP4.setVisible(false); 
 	}
 }
