 package Klient;
 
 
 import java.awt.EventQueue;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import java.awt.GridBagLayout;
 import javax.swing.JTextField;
 import java.awt.GridBagConstraints;
 import javax.swing.JLabel;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 
 public class GUIConnectMenu extends JFrame {
 	//This class is the window that gets input, either from saved bookmarks, or from manual input about connection info
 	//then firstly starts the Klient backend thread, and optionally opens a window for saving a bookmark.
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 	private JTextField connectEnterIP;
 	private JTextField connectEnterPort;
 	private JCheckBox bookmarkCheckbox;
 	@SuppressWarnings("rawtypes")
 	private JComboBox comboBoxBookmarkSelect;
 	private JLabel lblBookmark;
 	private String tempIP;
 	private int tempPort;
 	BookmarkInput bookmarkMenu = new BookmarkInput();
 	private JLabel lblSessionUsername;
 	private JTextField userNameTextField;
 
 
 	public void windowCloser(){
 		this.setVisible(false);
 	}
 
 	public void BookmarkSetter(){
 		//Helper function for easier access from the BookmarkInput class.
 		tempIP = connectEnterIP.getText();
 		tempPort = Integer.parseInt(connectEnterPort.getText());
 	}
 	public String BookmarkGetIp(){
 		return tempIP;
 	}
 	public int BookmarkGetPort(){
 		return tempPort;
 	}
 
 
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					GUIConnectMenu frame = new GUIConnectMenu();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public GUIConnectMenu() {
 		setTitle("Connect");
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 275, 225);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		GridBagLayout gbl_contentPane = new GridBagLayout();
 		gbl_contentPane.columnWidths = new int[] {5, 15, 50, 50, 20};
 		gbl_contentPane.rowHeights = new int[] {20, 20, 20, 20, 20, 20 ,20};
 		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0};
 		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		contentPane.setLayout(gbl_contentPane);
 
 		lblBookmark = new JLabel("Bookmark:");
 		GridBagConstraints gbc_lblBookmark = new GridBagConstraints();
 		gbc_lblBookmark.insets = new Insets(0, 0, 5, 5);
 		gbc_lblBookmark.anchor = GridBagConstraints.EAST;
 		gbc_lblBookmark.gridx = 2;
 		gbc_lblBookmark.gridy = 1;
 		contentPane.add(lblBookmark, gbc_lblBookmark);
 
 		comboBoxBookmarkSelect = new JComboBox<>();
 		
 		//Denna uppdaterar listan
 		comboBoxBookmarkSelect.addFocusListener(new FocusAdapter() {
 			@SuppressWarnings("unchecked")
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				
 					if (Main.mainData.nickname.isEmpty() != true){
 						
 						//Tï¿½mmer fï¿½r att undvika dubletter
 						comboBoxBookmarkSelect.removeAllItems();
 						
 						for (int i = 0; i < Main.mainData.nickname.size(); i++){
 							comboBoxBookmarkSelect.addItem(Main.mainData.nickname.get(i));
 						}
 
 						bookmarkCheckbox.setSelected(false);
 					}
 				}
 			});
 		//Updates fields with data from the bookmark object.
 		comboBoxBookmarkSelect.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (comboBoxBookmarkSelect.getSelectedIndex() != -1){
 					int sel = comboBoxBookmarkSelect.getSelectedIndex();
 					
 					connectEnterIP.setText(Main.mainData.ipAdress.get(sel));
 					connectEnterPort.setText(Main.mainData.port.get(sel).toString());
 				}	
 			}
 		});
 		
 		comboBoxBookmarkSelect.setToolTipText("Select a server:");
 		GridBagConstraints gbc_comboBoxBookmarkSelect = new GridBagConstraints();
 		gbc_comboBoxBookmarkSelect.gridwidth = 3;
 		gbc_comboBoxBookmarkSelect.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBoxBookmarkSelect.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBoxBookmarkSelect.gridx = 3;
 		gbc_comboBoxBookmarkSelect.gridy = 1;
 		contentPane.add(comboBoxBookmarkSelect, gbc_comboBoxBookmarkSelect);
 				
 				
 						
 				
 						JLabel lblEnterServerIp = new JLabel("Server IP:");
 						GridBagConstraints gbc_lblEnterServerIp = new GridBagConstraints();
 						gbc_lblEnterServerIp.insets = new Insets(0, 0, 5, 5);
 						gbc_lblEnterServerIp.gridx = 2;
 						gbc_lblEnterServerIp.gridy = 2;
 						contentPane.add(lblEnterServerIp, gbc_lblEnterServerIp);
 		
 				connectEnterIP = new JTextField();
 				GridBagConstraints gbc_connectEnterIP = new GridBagConstraints();
 				gbc_connectEnterIP.gridwidth = 2;
 				gbc_connectEnterIP.insets = new Insets(0, 0, 5, 5);
 				gbc_connectEnterIP.fill = GridBagConstraints.HORIZONTAL;
 				gbc_connectEnterIP.gridx = 3;
 				gbc_connectEnterIP.gridy = 2;
 				contentPane.add(connectEnterIP, gbc_connectEnterIP);
 				connectEnterIP.setColumns(10);
 		
 				JLabel lblPort = new JLabel("Port:");
 				GridBagConstraints gbc_lblPort = new GridBagConstraints();
 				gbc_lblPort.insets = new Insets(0, 0, 5, 5);
 				gbc_lblPort.anchor = GridBagConstraints.EAST;
 				gbc_lblPort.gridx = 2;
 				gbc_lblPort.gridy = 3;
 				contentPane.add(lblPort, gbc_lblPort);
 		
 				connectEnterPort = new JTextField();
 				connectEnterPort.setText("54602");
 				GridBagConstraints gbc_connectEnterPort = new GridBagConstraints();
 				gbc_connectEnterPort.insets = new Insets(0, 0, 5, 5);
 				gbc_connectEnterPort.fill = GridBagConstraints.HORIZONTAL;
 				gbc_connectEnterPort.gridx = 3;
 				gbc_connectEnterPort.gridy = 3;
 				contentPane.add(connectEnterPort, gbc_connectEnterPort);
 				connectEnterPort.setColumns(10);
 		
 				bookmarkCheckbox = new JCheckBox("Save this server");
 				bookmarkCheckbox.setSelected(true);
 				GridBagConstraints gbc_bookmarkCheckbox = new GridBagConstraints();
 				gbc_bookmarkCheckbox.gridwidth = 2;
 				gbc_bookmarkCheckbox.insets = new Insets(0, 0, 5, 5);
 				gbc_bookmarkCheckbox.gridx = 3;
 				gbc_bookmarkCheckbox.gridy = 4;
 				contentPane.add(bookmarkCheckbox, gbc_bookmarkCheckbox);
 		
 		lblSessionUsername = new JLabel("Nickname:");
 		GridBagConstraints gbc_lblSessionUsername = new GridBagConstraints();
 		gbc_lblSessionUsername.anchor = GridBagConstraints.EAST;
 		gbc_lblSessionUsername.insets = new Insets(0, 0, 5, 5);
 		gbc_lblSessionUsername.gridx = 2;
 		gbc_lblSessionUsername.gridy = 5;
 		contentPane.add(lblSessionUsername, gbc_lblSessionUsername);
 		
 		userNameTextField = new JTextField();
		userNameTextField.setText("Main.mainData.userName");
 		GridBagConstraints gbc_userNameTextField = new GridBagConstraints();
 		gbc_userNameTextField.gridwidth = 3;
 		gbc_userNameTextField.insets = new Insets(0, 0, 5, 5);
 		gbc_userNameTextField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_userNameTextField.gridx = 3;
 		gbc_userNameTextField.gridy = 5;
 		contentPane.add(userNameTextField, gbc_userNameTextField);
 		userNameTextField.setColumns(10);
 
 		JButton connectMenuConnectBtn = new JButton("Connect");
 		GridBagConstraints gbc_connectMenuConnectBtn = new GridBagConstraints();
 		gbc_connectMenuConnectBtn.gridwidth = 9;
 		gbc_connectMenuConnectBtn.gridx = 0;
 		gbc_connectMenuConnectBtn.gridy = 6;
 		contentPane.add(connectMenuConnectBtn, gbc_connectMenuConnectBtn);
 
 		connectMenuConnectBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				//Helper function, made for easier sending of data to the bookmark save window
 				BookmarkSetter();
 				Main.mainData.userName = userNameTextField.getText();
 				//Currently, whitespace causes a minor display bug. This should replace space and tabs and such with "_".
 				Main.mainData.userName = Main.mainData.userName.replaceAll("\\s","_");
 				
 				//Saves the userName (and as an added effect, all bookmarks are re-saved) to file
 				Main.saveData(Main.mainData);
 				
 				//Starts the Klient backend in a separate thread.
 				KlientThread tre = new KlientThread();
 				Thread t = new Thread(tre);
 				t.start();
 				
 				if (bookmarkCheckbox.isSelected() == true){
 					bookmarkMenu.setVisible(true);
 				}
 				windowCloser();
 
 			}
 		});
 
 		this.revalidate();
 	}
 
 }
