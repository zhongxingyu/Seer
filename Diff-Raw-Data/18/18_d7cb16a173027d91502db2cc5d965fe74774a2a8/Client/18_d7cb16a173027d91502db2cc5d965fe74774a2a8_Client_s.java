 package com.chat.somechat;
 
 import java.awt.GridBagLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.SystemColor;
 
 public class Client extends JFrame {
	
 	private static final long serialVersionUID = 1L;
 
 	private JPanel contentPane;
 
 	private String name, address;
 	private int port;
 	
 	public Client(String name, String address, int port) {
 		setTitle("Chat Client");
 		this.name = name;
 		this.address = address;
 		this.port = port;
 		createWindow();
 	}
 	
 	private void createWindow() {	
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(880,550);
 		setLocationRelativeTo(null);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		GridBagLayout gbl_contentPane = new GridBagLayout();
 		gbl_contentPane.columnWidths = new int[]{0, 880, 0};
 		gbl_contentPane.rowHeights = new int[]{0, 500, 50};
 		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gbl_contentPane.rowWeights = new double[]{1.0, Double.MIN_VALUE};
 		contentPane.setLayout(gbl_contentPane);
 		
 		JTextArea txtrHistory = new JTextArea();
 		txtrHistory.setBackground(SystemColor.inactiveCaptionBorder);
 		GridBagConstraints gbc_txtrHistory = new GridBagConstraints();
 		gbc_txtrHistory.fill = GridBagConstraints.BOTH;
 		gbc_txtrHistory.gridx = 1;
 		gbc_txtrHistory.gridy = 1;
 		contentPane.add(txtrHistory, gbc_txtrHistory);
 		setVisible(true);
 	}
 
 }
