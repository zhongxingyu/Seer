 package ReactorEE.swing;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 
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
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.net.Inet4Address;
 import java.net.UnknownHostException;
 
 import javax.swing.JTextField;
 
 public class MultiplayerSelectionGUI {
 
 	private JFrame frmMultiplayerConnection;
 	private JTextField txtOperatorIP;
	protected Component btnCancel;
	protected Component btnSabateur;
 
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
 		
 		btnOperator.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try{
 				new HandshakeListener().start();
 				btnOperator.setEnabled(false);
 				btnSabateur.setEnabled(false);
 				btnCancel.setEnabled(true);
 				}catch(Exception e1){
 					e1.printStackTrace();}
 			}
 		});
 		btnOperator.setBounds(310, 66, 117, 29);
 		layeredPane.add(btnOperator);
        final JButton btnSabateur = new JButton("Saboteur");
         
         
         btnSabateur.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		try {
         			String suppliedIP = txtOperatorIP.getText();
         			if(ReactorEE.Networking.SocketUtil.validateIP(suppliedIP) == true)
         			{
         				new HandshakeRequest().run(suppliedIP);
         				
         				btnOperator.setEnabled(false);
         				btnSabateur.setEnabled(false);
         				btnCancel.setEnabled(true);
         				
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
        final JButton btnCancel = new JButton("Cancel");
         btnCancel.setEnabled(false);
         
         
         btnCancel.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		try {
         			new Message().run("ANCHOVY KILL", Inet4Address.getLocalHost().getHostAddress(), SocketUtil.HANDSHAKE_PORT_NO);
         			btnOperator.setEnabled(true);
         			btnSabateur.setEnabled(true);
         			btnCancel.setEnabled(false);
         		} catch (Exception e1) {
         			e1.printStackTrace();
         		}
         	}
         });
         btnCancel.setBounds(310, 165, 117, 29);
         layeredPane.add(btnCancel);
 
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
         JLabel backgroundImageLabel = new JLabel(backgroundImageIcon);
         backgroundImageLabel.setBackground(new Color(0, 153, 0));
         backgroundImageLabel.setBounds(0, 0, 450, 300);
         layeredPane.add(backgroundImageLabel);
 	}
 }
