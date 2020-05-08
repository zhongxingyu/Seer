 package final_project.view;
 
 import java.awt.EventQueue;
 
 import java.util.*;
 import javax.swing.JFrame;
 import java.awt.GridBagLayout;
 import javax.swing.JLabel;
 import java.awt.GridBagConstraints;
 import javax.swing.JPanel;
 import javax.swing.JInternalFrame;
 import java.awt.Insets;
 import javax.swing.JTextField;
 import javax.swing.JPasswordField;
 import javax.swing.JButton;
 import javax.swing.border.TitledBorder;
 import java.awt.FlowLayout;
 import javax.swing.BoxLayout;
 import javax.swing.border.EtchedBorder;
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.UIManager;
 import java.io.File;
 import final_project.control.*;
 import final_project.model.*;
import final_project.model.store.*;
 import final_project.input.data.*;
 import final_project.input.*;
 import javax.swing.*;
 import java.awt.*;
 import javax.swing.border.*;
 import java.awt.event.*;
 import java.io.*;
 
 public class SetupWindow {
 
 	private JFrame frame;
 	private JTextField textField;
 	private JPasswordField passwordField;
 	private JTextField textField_1;
 	private JTextField textField_2;
 	private JFileChooser fileChooser;
     private File xmlFile;
     private JLabel fileLabel;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					SetupWindow window = new SetupWindow();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public SetupWindow() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 379, 300);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{315, 0, 0};
 		gridBagLayout.rowHeights = new int[]{97, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
 		frame.getContentPane().setLayout(gridBagLayout);
 		
 		JPanel smsLoginPanel = new JPanel();
 		smsLoginPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "SMS Login", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
 		GridBagConstraints gbc_smsLoginPanel = new GridBagConstraints();
 		gbc_smsLoginPanel.gridwidth = 2;
 		gbc_smsLoginPanel.insets = new Insets(0, 0, 5, 5);
 		gbc_smsLoginPanel.fill = GridBagConstraints.BOTH;
 		gbc_smsLoginPanel.gridx = 0;
 		gbc_smsLoginPanel.gridy = 0;
 		frame.getContentPane().add(smsLoginPanel, gbc_smsLoginPanel);
 		GridBagLayout gbl_smsLoginPanel = new GridBagLayout();
 		gbl_smsLoginPanel.columnWidths = new int[]{93, 14, 0};
 		gbl_smsLoginPanel.rowHeights = new int[]{28, 0, 0};
 		gbl_smsLoginPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		gbl_smsLoginPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 		smsLoginPanel.setLayout(gbl_smsLoginPanel);
 		
 		JLabel lblUsername = new JLabel("Username:");
 		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
 		gbc_lblUsername.anchor = GridBagConstraints.EAST;
 		gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
 		gbc_lblUsername.gridx = 0;
 		gbc_lblUsername.gridy = 0;
 		smsLoginPanel.add(lblUsername, gbc_lblUsername);
 		
 		textField = new JTextField();
 		GridBagConstraints gbc_textField = new GridBagConstraints();
 		gbc_textField.insets = new Insets(0, 0, 5, 0);
 		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField.gridx = 1;
 		gbc_textField.gridy = 0;
 		smsLoginPanel.add(textField, gbc_textField);
 		textField.setColumns(10);
 		
 		JLabel lblPassword = new JLabel("Password:");
 		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
 		gbc_lblPassword.anchor = GridBagConstraints.EAST;
 		gbc_lblPassword.insets = new Insets(0, 0, 0, 5);
 		gbc_lblPassword.gridx = 0;
 		gbc_lblPassword.gridy = 1;
 		smsLoginPanel.add(lblPassword, gbc_lblPassword);
 		
 		passwordField = new JPasswordField();
 		GridBagConstraints gbc_passwordField = new GridBagConstraints();
 		gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_passwordField.gridx = 1;
 		gbc_passwordField.gridy = 1;
 		smsLoginPanel.add(passwordField, gbc_passwordField);
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Tournament Info", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.gridwidth = 2;
 		gbc_panel.insets = new Insets(0, 0, 5, 5);
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 1;
 		frame.getContentPane().add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{85, 315, 0};
 		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
 		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 		
 		JLabel lblTournamentName = new JLabel("Name:");
 		GridBagConstraints gbc_lblTournamentName = new GridBagConstraints();
 		gbc_lblTournamentName.insets = new Insets(0, 0, 5, 5);
 		gbc_lblTournamentName.anchor = GridBagConstraints.EAST;
 		gbc_lblTournamentName.gridx = 0;
 		gbc_lblTournamentName.gridy = 0;
 		panel.add(lblTournamentName, gbc_lblTournamentName);
 		
 		textField_1 = new JTextField();
 		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
 		gbc_textField_1.insets = new Insets(0, 0, 5, 0);
 		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_1.gridx = 1;
 		gbc_textField_1.gridy = 0;
 		panel.add(textField_1, gbc_textField_1);
 		textField_1.setColumns(10);
 		
 		JLabel lblWeapon = new JLabel("Weapon:");
 		GridBagConstraints gbc_lblWeapon = new GridBagConstraints();
 		gbc_lblWeapon.anchor = GridBagConstraints.EAST;
 		gbc_lblWeapon.insets = new Insets(0, 0, 5, 5);
 		gbc_lblWeapon.gridx = 0;
 		gbc_lblWeapon.gridy = 1;
 		panel.add(lblWeapon, gbc_lblWeapon);
 		
 		textField_2 = new JTextField();
 		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
 		gbc_textField_2.insets = new Insets(0, 0, 5, 0);
 		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_2.gridx = 1;
 		gbc_textField_2.gridy = 1;
 		panel.add(textField_2, gbc_textField_2);
 		textField_2.setColumns(10);
         textField_2.setText("Saber");
 		
 		JLabel lblor = new JLabel("-OR-");
 		GridBagConstraints gbc_lblor = new GridBagConstraints();
 		gbc_lblor.insets = new Insets(0, 0, 5, 0);
 		gbc_lblor.gridwidth = 2;
 		gbc_lblor.gridx = 0;
 		gbc_lblor.gridy = 2;
 		panel.add(lblor, gbc_lblor);
 		
 		final JButton btnImportXml = new JButton("Import XML");
 		btnImportXml.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				fileChooser = new JFileChooser();
 				int returnValue = fileChooser.showOpenDialog(btnImportXml);
 				
 				if (returnValue == JFileChooser.APPROVE_OPTION) {
 					xmlFile = fileChooser.getSelectedFile();
 					fileLabel.setText(xmlFile.getPath());
 				}
 			}
 		});
 		GridBagConstraints gbc_btnImportXml = new GridBagConstraints();
 		gbc_btnImportXml.insets = new Insets(0, 0, 5, 0);
 		gbc_btnImportXml.gridwidth = 2;
 		gbc_btnImportXml.gridx = 0;
 		gbc_btnImportXml.gridy = 3;
 		panel.add(btnImportXml, gbc_btnImportXml);
 		
 		fileLabel = new JLabel("");
 		GridBagConstraints gbc_fileLabel = new GridBagConstraints();
 		gbc_fileLabel.gridwidth = 2;
 		gbc_fileLabel.gridx = 0;
 		gbc_fileLabel.gridy = 4;
 		panel.add(fileLabel, gbc_fileLabel);
 		
 		JButton btnStart = new JButton("Start");
 		btnStart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
                 ITournamentInfo i;
                 if (xmlFile != null) {
                     IDataInput in = new XmlReader();
                     i=in.getTournamentInfo(xmlFile);
                 } else {
                     String weapon = textField_2.getText();
                     IEventInfo e = new EventInfo(weapon,new ArrayList<Integer>());
                     Collection<IEventInfo> events = new ArrayList<IEventInfo>();
                     events.add(e);
                     final IDataStore store = new DataStore();
                     store.runTransaction(new Runnable() {
                             public void run() {
                                 store.putData(store.createPlayer("1234567891", "Jon", "Leavitt",
                                                                  "","Fencer",1));
                                 store.putData(store.createPlayer("1234561291", "William", "Zimrin",
                                                                  "","Fencer",1));
                                 store.putData(store.createPlayer("1235467892", "Josh", "Grill",
                                                                  "","Fencer",1));
                                 store.putData(store.createPlayer("5432167893", "John", "Connuck",
                                                                  "","Fencer",1));
                                 store.putData(store.createSpectator("8132987766", "Miranda", "Steele",
                                                                     "","Spectator"));
                             }
                         });
                     i = new TournamentInfo(store,events); 
                 }
 				MainWindow mainWindow = new MainWindow(i);
 
 				frame.setVisible(false);
 				//mainWindow.setVisible(true);
 			}
 		});
 		GridBagConstraints gbc_btnStart = new GridBagConstraints();
 		gbc_btnStart.insets = new Insets(0, 0, 0, 5);
 		gbc_btnStart.anchor = GridBagConstraints.EAST;
 		gbc_btnStart.gridx = 0;
 		gbc_btnStart.gridy = 2;
 		frame.getContentPane().add(btnStart, gbc_btnStart);
 	}
 
 }
