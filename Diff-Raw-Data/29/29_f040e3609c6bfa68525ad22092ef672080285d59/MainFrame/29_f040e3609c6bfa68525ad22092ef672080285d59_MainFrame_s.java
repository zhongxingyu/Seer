 package at.ac.tuwien.secsyseng.sqlinjection.gui;
 
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 import at.ac.tuwien.secsyseng.sqlinjection.exception.PasswordException;
 import at.ac.tuwien.secsyseng.sqlinjection.service.Var1Service;
 import at.ac.tuwien.secsyseng.sqlinjection.service.Var2Service;
 import at.ac.tuwien.secsyseng.sqlinjection.service.Var3Service;
 
 import net.miginfocom.swing.MigLayout;
 
 public class MainFrame implements ActionListener {
 
 	private JFrame frame;
 
 	// var1
 	private JPanel var1;
 	private JPanel var2;
 	private JPanel var3;
 	private JLabel descLbl1;
 	private JLabel secLbl1;
 	private JLabel userLbl1;
 	private JLabel passLbl1;
 	private JTextField userTf1;
 	private JPasswordField passTf1;
 	private JButton loginBtn1;
 	private JTextArea var1DataResponse;
 	private JScrollPane var1DataResponseScPane;
 	private JCheckBox secCheck1;
 	private Var1Service var1service;
 
 	// var2
 	private JLabel descLbl2;
 	private JLabel secLbl2;
 	private JLabel userLbl2;
 	private JLabel passLbl2;
 	private JTextField userTf2;
 	private JPasswordField passTf2;
 	private JButton loginBtn2;
 	private JTextArea var2DataResponse;
 	private JScrollPane var2DataResponseScPane;
 	private JCheckBox secCheck2;
 	private Var2Service var2service;
 	private JLabel searchLbl2;
 	private JTextField searchTf2;
 
 	// var3
 	private JLabel descLbl3;
 	private JLabel secLbl3;

 	private JButton loginBtn3;
 	private JTextArea var3DataResponse;
 	private JScrollPane var3DataResponseScPane;
 	private JCheckBox secCheck3;
 	private Var3Service var3service;
 	private JLabel groupLbl3;
 	private JLabel groupidLbl3;
 	private JTextField groupidTf3;
 	private JLabel groupnameLbl3;
 	private JTextField groupnameTf3;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void launch() {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					UIManager.setLookAndFeel(UIManager
 							.getSystemLookAndFeelClassName());
 					MainFrame window = new MainFrame();
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
 	public MainFrame() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setTitle("app1 - SQL-Injection");
		frame.setBounds(100, 100, 500, 350);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JTabbedPane app1Pane = new JTabbedPane();
 		frame.getContentPane().add(app1Pane);
 
 		var1 = new JPanel(new MigLayout("", "[][grow][][grow][]",
 				"[][][][grow]"));
 		var2 = new JPanel(new MigLayout("", "[][grow][][grow][]",
 				"[][][][][grow]"));
 		var3 = new JPanel(new MigLayout("", "[][grow][][grow][]",
 				"[][][][][grow]"));
 
 		app1Pane.addTab("Variante 1: Login-Fail", var1);
 		app1Pane.addTab("Variante 2: Query-Injection", var2);
 		app1Pane.addTab("Variante 3: SQL Injection in INSERT Query", var3);
 
 		// var1
 		descLbl1 = new JLabel(
 				"Description: Login to display data. Default: User=wong Password=dong");
 		secLbl1 = new JLabel("Security (on): ");
 		userLbl1 = new JLabel("User:");
 		passLbl1 = new JLabel("Password:");
 		secCheck1 = new JCheckBox();
 		userTf1 = new JTextField();
 		passTf1 = new JPasswordField();
 		var1DataResponse = new JTextArea();
 		var1DataResponseScPane = new JScrollPane(var1DataResponse);
 
 		loginBtn1 = new JButton("Login");
 		loginBtn1.addActionListener(this);
 		loginBtn1.setActionCommand("login");
 
 		var1.add(descLbl1, "cell 0 0 5 1");
 		var1.add(secLbl1, "cell 0 1 5 1");
 		var1.add(secCheck1, "cell 0 1 5 1");
 		var1.add(userLbl1, "cell 0 2");
 		var1.add(userTf1, "cell 1 2,growx");
 		var1.add(passLbl1, "cell 2 2");
 		var1.add(passTf1, "cell 3 2,growx");
 		var1.add(loginBtn1, "cell 4 2");
 		var1.add(var1DataResponseScPane, "cell 0 3 5 1,grow");
 
 		// var2
 		descLbl2 = new JLabel(
 				"Description: Search for data of the specified user. Default: User=wong Password=dong");
 		secLbl2 = new JLabel("Security (on): ");
 		userLbl2 = new JLabel("User:");
 		passLbl2 = new JLabel("Password:");
 		secCheck2 = new JCheckBox();
 		userTf2 = new JTextField();
 		passTf2 = new JPasswordField();
 		searchLbl2 = new JLabel("Search:");
 		searchTf2 = new JTextField();
 		var2DataResponse = new JTextArea();
 		var2DataResponseScPane = new JScrollPane(var2DataResponse);
 
 		loginBtn2 = new JButton("Search");
 		loginBtn2.addActionListener(this);
 		loginBtn2.setActionCommand("search");
 
 		var2.add(descLbl2, "cell 0 0 5 1");
 		var2.add(secLbl2, "cell 0 1 5 1");
 		var2.add(secCheck2, "cell 0 1 5 1");
 		var2.add(userLbl2, "cell 0 2");
 		var2.add(userTf2, "cell 1 2,growx");
 		var2.add(passLbl2, "cell 2 2");
 		var2.add(passTf2, "cell 3 2,growx");
 		var2.add(searchLbl2, "cell 0 3");
 		var2.add(searchTf2, "cell 1 3 3 1,growx");
 		var2.add(loginBtn2, "cell 4 3, align right");
 		var2.add(var2DataResponseScPane, "cell 0 4 5 1,grow");
 
 		// var3
 		var3service = new Var3Service();
 		descLbl3 = new JLabel("Description: Create new groups into database");
 		secLbl3 = new JLabel("Security (on): ");
 		secCheck3 = new JCheckBox();
 		groupLbl3 = new JLabel("Create new group: ");
 		groupidLbl3 = new JLabel("group id:");
 		groupidTf3 = new JTextField();
 		groupnameLbl3 = new JLabel("groupname:");
 		groupnameTf3 = new JTextField();
 		var3DataResponse = new JTextArea();
 		var3DataResponseScPane = new JScrollPane(var3DataResponse);
 		try {
 			
 			ResultSet rs = var3service.getvar3Data();
 			boolean nodata = true;
 			if (rs != null) {
 				while (rs.next()) {
 					nodata = false;
 					var3DataResponse.append(rs.getInt("id") + "|"
 							+ rs.getString("name") + "\n");
 				}
 				var3DataResponse.append("\n");
 
 				if (nodata)
 					JOptionPane.showConfirmDialog(null, "No data found",
 							"Info", JOptionPane.CLOSED_OPTION);
 			}
 		} catch (SQLException e1) {
 			JOptionPane.showConfirmDialog(null, e1.getMessage(), "Error",
 					JOptionPane.CLOSED_OPTION);
 		}
 		loginBtn3 = new JButton("Create");
 		loginBtn3.addActionListener(this);
 		loginBtn3.setActionCommand("create");
 
 		var3.add(descLbl3, "cell 0 0 5 1");
 		var3.add(secLbl3, "cell 0 1 5 1");
 		var3.add(secCheck3, "cell 0 1 5 1");
 		var3.add(groupLbl3, "cell 0 2");
 		var3.add(groupidLbl3, "cell 0 3");
 		var3.add(groupidTf3, "cell 1 3 3 1,growx");
 		var3.add(groupnameLbl3, "cell 0 4");
 		var3.add(groupnameTf3, "cell 1 4 4 1,growx");
 		var3.add(loginBtn3, "cell 4 3, align right");
 		var3.add(var3DataResponseScPane, "cell 0 5 6 1,grow");
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("login")) {
 			var1service = new Var1Service();
 			ResultSet rs = null;
 			boolean nodata = true;
 
 			try {
 				rs = var1service.transferVar1Response(userTf1.getText(),
 						passTf1.getText(), secCheck1.isSelected());
 			} catch (SQLException e2) {
 				JOptionPane.showConfirmDialog(null, e2.getMessage(), "Error",
 						JOptionPane.CLOSED_OPTION);
 			} catch (PasswordException e2) {
 				JOptionPane.showConfirmDialog(null, e2.getMessage(), "Error",
 						JOptionPane.CLOSED_OPTION);
 			}
 
 			try {
 				if (rs != null) {
 					while (rs.next()) {
 						nodata = false;
 						var1DataResponse.append(rs.getInt("id") + "|"
 								+ rs.getString("product") + "|"
 								+ rs.getString("location") + "|"
 								+ rs.getDouble("price") + "\n");
 					}
 
 					if (nodata)
 						JOptionPane.showConfirmDialog(null, "No data found",
 								"Info", JOptionPane.CLOSED_OPTION);
 					else
 						var1DataResponse.append("\n");
 				}
 			} catch (SQLException e1) {
 				JOptionPane.showConfirmDialog(null, e1.getMessage(), "Error",
 						JOptionPane.CLOSED_OPTION);
 			}
 		}
 
 		else if (e.getActionCommand().equals("search")) {
 			var2service = new Var2Service();
 			ResultSet rs = null;
 			boolean nodata = true;
 
 			try {
 				rs = var2service.transferVar2Response(userTf2.getText(),
 						passTf2.getText(), searchTf2.getText(),
 						secCheck2.isSelected());
 			} catch (SQLException e2) {
 				JOptionPane.showConfirmDialog(null, e2.getMessage(), "Error",
 						JOptionPane.CLOSED_OPTION);
 			} catch (PasswordException e2) {
 				JOptionPane.showConfirmDialog(null, e2.getMessage(), "Error",
 						JOptionPane.CLOSED_OPTION);
 			}
 
 			try {
 				if (rs != null) {
 					while (rs.next()) {
 						nodata = false;						
 						var2DataResponse.append(rs.getString(1) + "|"
 								+ rs.getString(2) + "|" + rs.getString(3) + "|"
 								+ rs.getString(4) + "\n");
 					}
 
 					if (nodata)
 						JOptionPane.showConfirmDialog(null, "No data found",
 								"Info", JOptionPane.CLOSED_OPTION);
 					else
 						var2DataResponse.append("\n");
 				}
 			} catch (SQLException e1) {
 				JOptionPane.showConfirmDialog(null, e1.getMessage(), "Error",
 						JOptionPane.CLOSED_OPTION);
 			}
 		} else if (e.getActionCommand().equals("create")) {
 
 			int update;
 			ResultSet rs = null;
 			boolean nodata = true;
 
 			try {
 				update = var3service.transferVar3Response(
 						Integer.parseInt(groupidTf3.getText()),
 						groupnameTf3.getText(), secCheck3.isSelected());
 			} catch (SQLException e2) {
 				JOptionPane.showConfirmDialog(null, e2.getMessage(), "Error",
 						JOptionPane.CLOSED_OPTION);
 			}
 
 			try {
 				rs = var3service.getvar3Data();
 
 				if (rs != null) {
 					while (rs.next()) {
 						nodata = false;
 						var3DataResponse.append(rs.getInt("id") + "|"
 								+ rs.getString("name") + "\n");
 					}
 					var3DataResponse.append("\n");
 
 					if (nodata)
 						JOptionPane.showConfirmDialog(null, "No data found",
 								"Info", JOptionPane.CLOSED_OPTION);
 				}
 			} catch (SQLException e1) {
 				JOptionPane.showConfirmDialog(null, e1.getMessage(), "Error",
 						JOptionPane.CLOSED_OPTION);
 			}
 		}
 	}
 }
