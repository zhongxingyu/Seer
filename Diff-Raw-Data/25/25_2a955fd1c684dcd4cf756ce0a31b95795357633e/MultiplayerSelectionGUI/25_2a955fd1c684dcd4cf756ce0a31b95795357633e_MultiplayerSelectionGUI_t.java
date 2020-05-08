 package ReactorEE.swing;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Font;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JOptionPane;
 import java.awt.BorderLayout;
 import javax.swing.JButton;
 
 import ReactorEE.Networking.HandshakeListener;
 import ReactorEE.Networking.HandshakeRequest;
 import ReactorEE.Networking.Message;
 import ReactorEE.Networking.SocketUtil;
 import ReactorEE.sound.Sound;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.net.Inet4Address;
 import java.net.UnknownHostException;
 
 import javax.swing.JTextField;
 
 public class MultiplayerSelectionGUI {
 
 	private JFrame frmMultiplayerConnection;
 	private JTextField txtOperatorIP;
 	boolean connectionActive = false;
 	//protected Component btnCancel;
 	//protected Component btnSabateur;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MultiplayerSelectionGUI window = new MultiplayerSelectionGUI();
 					window.frmMultiplayerConnection.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public MultiplayerSelectionGUI() {
 		initialize();
 		frmMultiplayerConnection.setVisible(true);
 	}
 
 	/**
 	 * Initialise the contents of the frame.
 	 */
 	private void initialize() {
 		frmMultiplayerConnection = new JFrame();
 		frmMultiplayerConnection.setTitle("Multiplayer connection");
 		frmMultiplayerConnection.setBounds(100, 100, 450, 300);
 		frmMultiplayerConnection.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JLayeredPane layeredPane = new JLayeredPane();
 		frmMultiplayerConnection.getContentPane().add(layeredPane, BorderLayout.CENTER);
 		
 		
 		java.net.URL imageURL = this.getClass().getClassLoader().getResource("ReactorEE/graphics/plantBackground.png");
         ImageIcon backgroundImageIcon = new ImageIcon(imageURL);
         
 		final JButton btnOperator = new JButton("Operator");
 		final JButton btnSabateur = new JButton("Saboteur");
 		final JButton btnCancel = new JButton("Cancel");
 		
 		btnOperator.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Sound.play(Sound.DEFAULT_MENU_PUTTON_CLICK);
 				try{
 				new HandshakeListener(getThis()).start();
 				btnOperator.setEnabled(false);
 				btnSabateur.setEnabled(false);
 				btnCancel.setEnabled(true);
 				connectionActive = true;
 				}catch(Exception e1){
 					e1.printStackTrace();}
 			}
 		});
 		btnOperator.setBounds(310, 84, 117, 29);
 		layeredPane.add(btnOperator);
         
         
         
         btnSabateur.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		btnOperator.setEnabled(false);
 				btnSabateur.setEnabled(false);
 				btnCancel.setEnabled(true);
 				Sound.play(Sound.DEFAULT_MENU_PUTTON_CLICK);
         		try {
         			String suppliedIP = txtOperatorIP.getText();
         			if(ReactorEE.Networking.SocketUtil.validateIP(suppliedIP) == true)
         			{
         				btnOperator.setEnabled(false);
         				btnSabateur.setEnabled(false);
         				btnCancel.setEnabled(true);
         				new HandshakeRequest(getThis(),suppliedIP).start();
         				connectionActive = true;
         				
         			}
         			else 
         			{
         				JOptionPane.showMessageDialog(frmMultiplayerConnection, suppliedIP + " is an invalid IP Address. Please enter a valid IPv4 address.");
         			}
         		} catch (Exception e1) {
         			e1.printStackTrace();
         		}
         	}
         });
         btnSabateur.setBounds(310, 124, 117, 29);
         layeredPane.add(btnSabateur);
         
         btnCancel.setEnabled(false);
         
         
         btnCancel.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		Sound.play(Sound.DEFAULT_MENU_PUTTON_CLICK);
         		try {
         			btnOperator.setEnabled(true);
         			btnSabateur.setEnabled(true);
         			btnCancel.setEnabled(false);
         			connectionActive = false;
         			new Message().run("ANCHOVY KILL", Inet4Address.getLocalHost().getHostAddress(), SocketUtil.HANDSHAKE_PORT_NO);
         		} catch (Exception e1) {
         		}
         	}
         });
         btnCancel.setBounds(310, 165, 117, 29);
         layeredPane.add(btnCancel);
         
         JButton btnBack = new JButton("Back");
         btnBack.setBounds(310, 205, 117, 28);
         layeredPane.add(btnBack);
         
         btnBack.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		Sound.play(Sound.DEFAULT_MENU_PUTTON_CLICK);
         		frmMultiplayerConnection.dispose();
         		try {
         			if(connectionActive)
         			{
         				new Message().run("ANCHOVY KILL", Inet4Address.getLocalHost().getHostAddress(), SocketUtil.HANDSHAKE_PORT_NO);
         				connectionActive = false;
         			}
         		} catch (Exception e1) 
         		{
         		}
         		new GameTypeSelectionGUI();
         	}
         });
         
         txtOperatorIP = new JTextField();
         txtOperatorIP.setBounds(164, 123, 134, 28);
         txtOperatorIP.setColumns(10);
         layeredPane.add(txtOperatorIP);
         
         try {
         	txtOperatorIP.setText(Inet4Address.getLocalHost().getHostAddress());
 		} catch (UnknownHostException e1) {
 			e1.printStackTrace();
 			txtOperatorIP.setText("OpIP");
 		}
         
         JLabel lblNewLabel = new JLabel("I.P. address");
         lblNewLabel.setForeground(new Color(240, 255, 255));
         lblNewLabel.setFont(new Font("Serif", Font.PLAIN, 18));
         lblNewLabel.setLabelFor(txtOperatorIP);
         lblNewLabel.setBounds(70, 113, 94, 47);
         layeredPane.add(lblNewLabel);
         JLabel backgroundImageLabel = new JLabel(backgroundImageIcon);
         backgroundImageLabel.setBackground(new Color(0, 153, 0));
         backgroundImageLabel.setBounds(0, 0, 450, 300);
         layeredPane.add(backgroundImageLabel);
 	}
 	
 	/**
 	 * Closes this window.
 	 */
 	public void close(){
 		frmMultiplayerConnection.dispose();
 	}
 	private MultiplayerSelectionGUI getThis(){
 		return this;
 	}
	public JFrame getFrame(){
		return frmMultiplayerConnection;
	}
 }
