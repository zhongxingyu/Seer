 package application;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import java.awt.GridBagLayout;
 import javax.swing.JPanel;
 import java.awt.GridBagConstraints;
 import javax.swing.UIManager;
 import javax.swing.JLabel;
 import java.awt.Insets;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import java.awt.Dimension;
 
 public class NewCustomer {
 
 	private JFrame frame;
 	private JTextField textField;
 	private JTextField textField_2;
 	private JTextField txtNoting;
 	private JTextField textField_4;
 	private JTextField txtBla;
 	private JTextField textField_1;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					NewCustomer window = new NewCustomer();
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
 	public NewCustomer() {
 		initialize();
 	}
	public void setVisible(){
		frame.setVisible(true);
	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setResizable(false);
 		frame.setBounds(100, 100, 416, 262);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
 		frame.getContentPane().setLayout(gridBagLayout);
 		
 		JPanel panel = new JPanel();
 		panel.setMinimumSize(new Dimension(60, 30));
 		panel.setBorder(UIManager.getBorder("InsetBorder.aquaVariant"));
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 0;
 		frame.getContentPane().add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{0, 170, 0, 82, 0, 0};
 		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 		
 		JLabel lblNewCustomer = new JLabel("New Customer");
 		GridBagConstraints gbc_lblNewCustomer = new GridBagConstraints();
 		gbc_lblNewCustomer.gridwidth = 5;
 		gbc_lblNewCustomer.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNewCustomer.gridx = 0;
 		gbc_lblNewCustomer.gridy = 0;
 		panel.add(lblNewCustomer, gbc_lblNewCustomer);
 		
 		JLabel lblName = new JLabel("Name");
 		GridBagConstraints gbc_lblName = new GridBagConstraints();
 		gbc_lblName.insets = new Insets(0, 0, 5, 5);
 		gbc_lblName.anchor = GridBagConstraints.EAST;
 		gbc_lblName.gridx = 0;
 		gbc_lblName.gridy = 2;
 		panel.add(lblName, gbc_lblName);
 		
 		textField = new JTextField();
 		GridBagConstraints gbc_textField = new GridBagConstraints();
 		gbc_textField.insets = new Insets(0, 0, 5, 5);
 		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField.gridx = 1;
 		gbc_textField.gridy = 2;
 		panel.add(textField, gbc_textField);
 		textField.setColumns(10);
 		
 		textField_1 = new JTextField();
 		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
 		gbc_textField_1.gridwidth = 2;
 		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
 		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_1.gridx = 2;
 		gbc_textField_1.gridy = 2;
 		panel.add(textField_1, gbc_textField_1);
 		textField_1.setColumns(10);
 		
 		JLabel lblStreet = new JLabel("Street");
 		GridBagConstraints gbc_lblStreet = new GridBagConstraints();
 		gbc_lblStreet.anchor = GridBagConstraints.EAST;
 		gbc_lblStreet.insets = new Insets(0, 0, 5, 5);
 		gbc_lblStreet.gridx = 0;
 		gbc_lblStreet.gridy = 3;
 		panel.add(lblStreet, gbc_lblStreet);
 		
 		textField_2 = new JTextField();
 		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
 		gbc_textField_2.insets = new Insets(0, 0, 5, 5);
 		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_2.gridx = 1;
 		gbc_textField_2.gridy = 3;
 		panel.add(textField_2, gbc_textField_2);
 		textField_2.setColumns(10);
 		
 		txtNoting = new JTextField();
 		txtNoting.setMaximumSize(new Dimension(40, 2147483647));
 		txtNoting.setMinimumSize(new Dimension(40, 28));
 		GridBagConstraints gbc_txtNoting = new GridBagConstraints();
 		gbc_txtNoting.anchor = GridBagConstraints.WEST;
 		gbc_txtNoting.insets = new Insets(0, 0, 5, 5);
 		gbc_txtNoting.gridx = 2;
 		gbc_txtNoting.gridy = 3;
 		panel.add(txtNoting, gbc_txtNoting);
 		txtNoting.setColumns(10);
 		
 		JLabel lblCity = new JLabel("City");
 		GridBagConstraints gbc_lblCity = new GridBagConstraints();
 		gbc_lblCity.anchor = GridBagConstraints.WEST;
 		gbc_lblCity.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCity.gridx = 0;
 		gbc_lblCity.gridy = 4;
 		panel.add(lblCity, gbc_lblCity);
 		
 		textField_4 = new JTextField();
 		GridBagConstraints gbc_textField_4 = new GridBagConstraints();
 		gbc_textField_4.insets = new Insets(0, 0, 5, 5);
 		gbc_textField_4.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_4.gridx = 1;
 		gbc_textField_4.gridy = 4;
 		panel.add(textField_4, gbc_textField_4);
 		textField_4.setColumns(10);
 		
 		txtBla = new JTextField();
 		txtBla.setMaximumSize(new Dimension(60, 2147483647));
 		txtBla.setMinimumSize(new Dimension(60, 28));
 		txtBla.setText("\n");
 		txtBla.setPreferredSize(new Dimension(60, 28));
 		GridBagConstraints gbc_txtBla = new GridBagConstraints();
 		gbc_txtBla.anchor = GridBagConstraints.WEST;
 		gbc_txtBla.insets = new Insets(0, 0, 5, 5);
 		gbc_txtBla.gridx = 2;
 		gbc_txtBla.gridy = 4;
 		panel.add(txtBla, gbc_txtBla);
 		txtBla.setColumns(10);
 		
 		JButton btnReset = new JButton("Reset");
 		GridBagConstraints gbc_btnReset = new GridBagConstraints();
 		gbc_btnReset.anchor = GridBagConstraints.EAST;
 		gbc_btnReset.insets = new Insets(0, 0, 0, 5);
 		gbc_btnReset.gridx = 1;
 		gbc_btnReset.gridy = 6;
 		panel.add(btnReset, gbc_btnReset);
 		
 		JButton btnSave = new JButton("Save");
 		GridBagConstraints gbc_btnSave = new GridBagConstraints();
 		gbc_btnSave.insets = new Insets(0, 0, 0, 5);
 		gbc_btnSave.gridwidth = 2;
 		gbc_btnSave.anchor = GridBagConstraints.WEST;
 		gbc_btnSave.gridx = 2;
 		gbc_btnSave.gridy = 6;
 		panel.add(btnSave, gbc_btnSave);
 	}
 
 }
