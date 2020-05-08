 package poker.GUI;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import client.Client;
 
 @SuppressWarnings("serial")
 public class Login extends JFrame implements ActionListener, ItemListener {
     private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     public Client client;
 
     public Login(){
 
         LoginWindow.setLayout(new FlowLayout());
         LoginWindow.setSize(240, 200);
         LoginWindow.setLocation(screenSize.width / 2 - LoginWindow.getSize().width / 2, screenSize.height / 2 - LoginWindow.getSize().height / 2);
         LoginWindow.setResizable(false);
         LoginWindow.setVisible(true);
         LoginWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         LoginWindow.setTitle("Login");
         LoginWindow.getContentPane().setLayout(null);
         
         LoginWindow.add(textName(), null);
         LoginWindow.add(ipAddress(),null);
         LoginWindow.add(port(),null);
         LoginWindow.add(players(),null);
         LoginWindow.add(labelName(), null);
         LoginWindow.add(ipNumber(),null);
         LoginWindow.add(portNumber(),null);
         LoginWindow.add(createServer(), null);
         LoginWindow.add(Players(), null);
         LoginWindow.add(buttonConnect(), null);
         LoginWindow.add(warning(), null);
         
     }
 
 
     // LoginWindow variables
     private JFrame LoginWindow = new JFrame();
     private String PlayerName;
     private String IPaddress;
     private String Port;
     private JLabel labelName = new JLabel();
     private JLabel ipNumber = new JLabel();
     private JLabel portNumber = new JLabel();
     private JLabel players = new JLabel();
     private JLabel warning = new JLabel();
     private JTextField textName = new JTextField();
     private JTextField ipAddress = new JTextField();
     private JTextField port = new JTextField();
     private JTextField playersNum = new JTextField();
     private JCheckBox createServer = new JCheckBox();
     public JButton buttonConnect = new JButton();
 
     private JLabel labelName(){
         labelName.setBounds(10, 15, 90, 25);
         labelName.setText("Your name: ");
         return labelName;
     }
     private JLabel ipNumber(){
         ipNumber.setBounds(10, 50, 90, 25);
         ipNumber.setText("IP Adress: ");
         return ipNumber;
     }
     private JLabel players(){
     	players.setBounds(10, 140, 90, 25);
     	players.setText("Players: ");
     	players.setVisible(false);
         return players;
     }
     private JLabel portNumber(){
     	portNumber.setBounds(10, 85, 90, 25);
     	portNumber.setText("Port: ");
         return portNumber;
     }
     private JLabel warning(){
         warning.setHorizontalAlignment( SwingConstants.CENTER );
         warning.setText("");
         warning.setVisible(false);
         return warning;
     }
     private JTextField textName(){
         textName.setBounds(100, 15, 120, 25);
 	        textName.addKeyListener(new KeyAdapter() {public void keyPressed(KeyEvent e) {
 	            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 	                buttonConnect.doClick();
 	            }
 	        }
         }
         );
         return textName;
     }
     
     private JTextField ipAddress(){
     	ipAddress.setBounds(100, 50, 120, 25);
     	ipAddress.setVisible(true);
     	ipAddress.addKeyListener(new KeyAdapter() {public void keyPressed(KeyEvent e) {
 	            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 	                buttonConnect.doClick();
 	            }
 	        }
      }
      );
     	try {
 			ipAddress.setText("" + InetAddress.getLocalHost().getHostAddress());
 		} catch (UnknownHostException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
     	
         return ipAddress;
     }
     
     private JTextField port(){
     	port.setBounds(100, 85, 120, 25);
     	port.setVisible(true);
     	port.setText("9876");
     	port.addKeyListener(new KeyAdapter() {public void keyPressed(KeyEvent e) {
 	            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 	                buttonConnect.doClick();
 	            }
 	        }
     	 }
     			 );
         return port;
     }
     
     private JTextField Players(){
     	playersNum.setBounds(100, 140, 120, 25);
     	playersNum.setVisible(true);
     	playersNum.setText("");
     	playersNum.addKeyListener(new KeyAdapter() {public void keyPressed(KeyEvent e) {
 	            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 	                buttonConnect.doClick();
 	            }
 	        }
     	 }
     			 );
     	playersNum.setVisible(false);
         return playersNum;
     }
     
     private JButton buttonConnect(){
         
         buttonConnect.setActionCommand("connect");
         buttonConnect.setBounds(70, 140, 100, 25);
         buttonConnect.setText("Connect");
         buttonConnect.addActionListener(this);
         return buttonConnect;
     }
     private JCheckBox createServer() {
     	createServer.setActionCommand("createServer");
     	createServer.setBounds(10,115,150,20);
     	createServer.setText("Create a new server");
     	createServer.addItemListener(this);
         return createServer;
     }
     public void itemStateChanged(ItemEvent e) {
     	if(createServer.isSelected()){ 
     		LoginWindow.setSize(240, 230);
     		
     		try {
 				ipAddress.setText("" + InetAddress.getLocalHost().getHostAddress());
 			} catch (UnknownHostException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
     		ipAddress.setEnabled(false);
     		buttonConnect.setText("Create a server");
     		buttonConnect.setBounds(45, 170, 150, 25);
     		players.setVisible(true);
     		warning.setVisible(false);
     		playersNum.setVisible(true);
     	} else {
     		LoginWindow.setSize(240, 200);
     		buttonConnect.setEnabled(true);
     		ipAddress.setEnabled(true);
     		buttonConnect.setText("Connect");
     		buttonConnect.setBounds(70, 140, 100, 25);
     		players.setVisible(false);
     		warning.setVisible(false);
     		playersNum.setVisible(false);
     	}
     }
     public void actionPerformed(ActionEvent e) {
         System.out.println(e.getActionCommand());
         if("connect".equals(e.getActionCommand())){
         	if(buttonConnect.getText().equals("Connect")){
 	            if(textName.getText().length() >= 3 && textName.getText().length() <= 15){
 	                PlayerName = textName.getText();
                     IPaddress = ipAddress.getText();
                     Port = port.getText();
 	                LoginWindow.dispose();
 	                Client.start(PlayerName, Integer.parseInt(Port), IPaddress);
 	            } else {
 	                LoginWindow.setSize(240, 235);
 	                buttonConnect.setText("Connect");
 	                warning.setText("<html><body align='center'>Name must be between 3 and 15 characters long!</body></html>");
 	                warning.setBounds(5, 170, 220, 30);
 	                warning.setVisible(true);
 	            }
         	} else {
         		if(playersNum.getText().length() > 9 && textName.getText().length() < 2){
                    int PlayersCount = Integer.parseInt(playersNum.getText());
        			Client.startServer(PlayersCount);
                     warning.setVisible(false);
 	            } else {
 	            	LoginWindow.setSize(240, 270);
 	                
 	        		
 	                warning.setBounds(5, 200, 220, 30);
 	                warning.setText("<html><body align='center'>Players count must be<br>between 2 and 9!</body></html>");
 	                warning.setVisible(true);
 	            }
         	}
         }
     }
 
 }
