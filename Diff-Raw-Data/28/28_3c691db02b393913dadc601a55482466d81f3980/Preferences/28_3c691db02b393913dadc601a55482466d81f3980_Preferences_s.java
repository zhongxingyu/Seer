 package jmbs.client.Graphics;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.ButtonGroup;
 import javax.swing.JTabbedPane;
 import javax.swing.ImageIcon;
 import javax.swing.JRadioButton;
 import javax.swing.JLabel;
 import javax.swing.JCheckBox;
 import javax.swing.JSlider;
 import java.awt.Font;
 import javax.swing.JSeparator;
 import javax.swing.JButton;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 
 import com.sun.awt.AWTUtilities;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JTextField;
 
 import jmbs.client.ClientRequests;
 import jmbs.client.SysConf;
 
 public class Preferences extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4493110241913884584L;
 	private JPanel contentPane;
 	private JLabel labelprecent;
 	private JSlider slider;
 	private JCheckBox chckbxTransparency;
 	private JLabel lblThemes;
 	private JRadioButton rdbtnDark;
 	private JRadioButton rdbtnGray;
 	private JRadioButton rdbtnLight;
 	private JFrame frame = new MainWindow().getFrame();
 	private static boolean colorChanged = false;
 	private JLabel lblNetwork;
 	private JLabel lblAdresseIpserver;
 	private JLabel lblServerName;
 	private JLabel lblPort;
 	private JTextField ipTextField;
 	private JTextField serverNameTextField;
 	private JTextField portTextField;
 	private JButton btnOk;
 	private JButton btnApply;
 	private JButton btnCancel_1;
 	private JButton btnRestoreAsDefault;
 	private JLabel lblMessages;
 	private JTextField limitedMsgTextField;
 	private ClientRequests serverConfig;
 
 	/**
 	 * Create the frame.
 	 */
 	public Preferences() {
 		
 		serverConfig = new ClientRequests();
 		ButtonGroup groupDefaultOptions = new ButtonGroup();
 		ButtonGroup groupThemes = new ButtonGroup();
 		setTitle("JMBS: Preferences");
 		
 		setAlwaysOnTop(true);
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 400, 360);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		contentPane.add(tabbedPane, BorderLayout.CENTER);
 		new SysConf().centerThisFrame(this);
 		
 		JPanel generalPanel = new JPanel();
 		tabbedPane.addTab("General", new ImageIcon("/Users/ycheikh/Dev/JMB/src/jmbs/client/img/General-Alt.png"), generalPanel, null);
 		
 		lblNetwork = new JLabel("Network:");
 		lblNetwork.setBounds(6, 6, 64, 17);
 		lblNetwork.setFont(new Font("Lucida Grande", Font.BOLD, 14));
 		
 		lblAdresseIpserver = new JLabel("Adresse IP (Server): ");
 		lblAdresseIpserver.setBounds(6, 35, 123, 16);
 		
 		lblServerName = new JLabel("Server name:");
 		lblServerName.setBounds(6, 69, 80, 16);
 		
 		lblPort = new JLabel("Port:");
 		lblPort.setBounds(6, 103, 29, 16);
 		
 		ipTextField = new JTextField();
 		ipTextField.setBounds(135, 29, 134, 28);
 		ipTextField.setColumns(10);
 		ipTextField.setText(serverConfig.getAddressIP());
 		
 		serverNameTextField = new JTextField();
 		serverNameTextField.setBounds(135, 63, 134, 28);
 		serverNameTextField.setColumns(10);
 		serverNameTextField.setText(serverConfig.getName());
 		
 		portTextField = new JTextField();
 		portTextField.setBounds(135, 97, 134, 28);
 		portTextField.setColumns(10);
 		portTextField.setText(serverConfig.getPort());
 		
 		btnOk = new JButton("OK");
 		btnOk.setEnabled(false);
 		btnOk.setBounds(288, 253, 75, 29);
 		
 		btnApply = new JButton("Apply");
 		btnApply.setEnabled(false);
 		btnApply.setBounds(202, 253, 80, 29);
 		
 		btnCancel_1 = new JButton("Cancel");
 		btnCancel_1.setBounds(6, 253, 86, 29);
 		btnCancel_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 		
 		btnRestoreAsDefault = new JButton("Reset To Default");
 		btnRestoreAsDefault.setEnabled(false);
 		btnRestoreAsDefault.setBounds(215, 218, 148, 29);
 		
 		JLabel labelMax = new JLabel("Limite Received Messages:");
 		labelMax.setBounds(6, 173, 177, 16);
 		
 		lblMessages = new JLabel("Messages:");
 		lblMessages.setBounds(6, 150, 73, 17);
 		lblMessages.setFont(new Font("Lucida Grande", Font.BOLD, 14));
 		
 		limitedMsgTextField = new JTextField();
 		limitedMsgTextField.setBounds(189, 173, 80, 28);
 		limitedMsgTextField.setColumns(10);
 		limitedMsgTextField.setText(""+ClientRequests.maxReceivedMsgs);
 		
 		JSeparator separator_1 = new JSeparator();
 		separator_1.setBounds(44, 131, 273, 12);
 		generalPanel.setLayout(null);
 		generalPanel.add(btnCancel_1);
 		generalPanel.add(btnApply);
 		generalPanel.add(btnOk);
 		generalPanel.add(btnRestoreAsDefault);
 		generalPanel.add(lblNetwork);
 		generalPanel.add(labelMax);
 		generalPanel.add(limitedMsgTextField);
 		generalPanel.add(lblAdresseIpserver);
 		generalPanel.add(lblServerName);
 		generalPanel.add(lblPort);
 		generalPanel.add(ipTextField);
 		generalPanel.add(serverNameTextField);
 		generalPanel.add(portTextField);
 		generalPanel.add(lblMessages);
 		generalPanel.add(separator_1);
 		
 		JPanel appearancePanel = new JPanel();
 		tabbedPane.addTab("appearance", new ImageIcon("/Users/ycheikh/Dev/JMB/src/jmbs/client/img/appearance.png"), appearancePanel, "appearance");
 		
 		JRadioButton rdbtnUseDefaultOptions = new JRadioButton("Use default options");
 		rdbtnUseDefaultOptions.setBounds(16, 19, 155, 23);
 		rdbtnUseDefaultOptions.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				chckbxTransparency.setEnabled(false);
 				chckbxTransparency.setSelected(false);
 				labelprecent.setEnabled(false);
 				slider.setEnabled(false);
 				lblThemes.setEnabled(false);
 				rdbtnDark.setEnabled(false);
 				rdbtnGray.setEnabled(false);
 				rdbtnGray.setSelected(true);
 				rdbtnLight.setEnabled(false);
 				if(colorChanged)
 					new MainWindow().setColors("");
 				
 				AWTUtilities.setWindowOpacity(frame, 1.0f);
 			}
 		});
 		rdbtnUseDefaultOptions.setSelected(true);
 		
 		JRadioButton rdbtnDontUseThe = new JRadioButton("Don't use the default options");
 		rdbtnDontUseThe.setBounds(16, 68, 216, 23);
 		rdbtnDontUseThe.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				chckbxTransparency.setEnabled(true);
 				lblThemes.setEnabled(true);
 				rdbtnDark.setEnabled(true);
 				rdbtnGray.setEnabled(true);
 				rdbtnLight.setEnabled(true);
 			}
 		});
 		
 		groupDefaultOptions.add(rdbtnUseDefaultOptions);
 		groupDefaultOptions.add(rdbtnDontUseThe);
 		
 		chckbxTransparency = new JCheckBox("Opacity:");
 		chckbxTransparency.setBounds(60, 103, 84, 23);
 		chckbxTransparency.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(chckbxTransparency.isSelected()) {
 					labelprecent.setEnabled(true);
 					slider.setEnabled(true);
 					AWTUtilities.setWindowOpacity(frame, slider.getValue()/100f);
 				}
 				else {
 					labelprecent.setEnabled(false);
 					slider.setEnabled(false);
 					AWTUtilities.setWindowOpacity(frame, 1.0f);
 				}
 			}
 		});
 		chckbxTransparency.setEnabled(false);
 		
 		slider = new JSlider();
 		slider.setBounds(50, 127, 260, 38);
 		slider.setEnabled(false);
 		slider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				labelprecent.setText(slider.getValue()+"%");
 				AWTUtilities.setWindowOpacity(frame, slider.getValue()/100f);
 			}
 		});
 		slider.setPaintTicks(true);
 		slider.setPaintLabels(true);
 		
 		labelprecent = new JLabel(slider.getValue()+"%");
		labelprecent.setBounds(164, 107, 25, 16);
 		labelprecent.setEnabled(false);
 		
 		lblThemes = new JLabel("Themes:");
 		lblThemes.setBounds(50, 172, 57, 16);
 		lblThemes.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 		
 		rdbtnDark = new JRadioButton("Dark");
 		
 		rdbtnDark.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				new MainWindow().setColors("dark");
 				colorChanged = true;
 			}
 		});
 		
 		rdbtnDark.setBounds(50, 200, 62, 23);
 		rdbtnDark.setEnabled(false);
 		
 		rdbtnGray = new JRadioButton("Gray");
 		rdbtnGray.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				new MainWindow().setColors("");
 			}
 		});
 		rdbtnGray.setBounds(129, 200, 60, 23);
 		rdbtnGray.setEnabled(false);
 		rdbtnGray.setSelected(true);
 		
 		rdbtnLight = new JRadioButton("Light");
 		
 		rdbtnLight.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				new MainWindow().setColors("light");
 				colorChanged = true;
 			}
 		});
 		
 		rdbtnLight.setBounds(214, 200, 64, 23);
 		rdbtnLight.setEnabled(false);
 		
 		groupThemes.add(rdbtnDark);
 		groupThemes.add(rdbtnGray);
 		groupThemes.add(rdbtnLight);
 		
 		JSeparator separator = new JSeparator();
 		separator.setBounds(16, 43, 330, 12);
 		
 		JButton btnCancel = new JButton("Close");
 		btnCancel.setBounds(145, 247, 86, 29);
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 		appearancePanel.setLayout(null);
 		appearancePanel.add(rdbtnUseDefaultOptions);
 		appearancePanel.add(rdbtnDontUseThe);
 		appearancePanel.add(chckbxTransparency);
 		appearancePanel.add(slider);
 		appearancePanel.add(labelprecent);
 		appearancePanel.add(lblThemes);
 		appearancePanel.add(rdbtnDark);
 		appearancePanel.add(rdbtnGray);
 		appearancePanel.add(rdbtnLight);
 		appearancePanel.add(separator);
 		appearancePanel.add(btnCancel);
 	}
 }
