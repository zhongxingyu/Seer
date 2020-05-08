 package view;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import domain.Customer;
 import domain.Library;
 
 public class CustomerEdit extends JFrame {
 
 	private JPanel contentPane;
 	private JLabel lblName;
 	private JTextField txtName;
 	private JLabel lblSurename;
 	private JTextField txtSurname;
 	private JLabel lblStreet;
 	private JTextField txtStreet;
 	private JLabel lblZip;
 	private JTextField txtZip;
 	private JLabel lblCity;
 	private JTextField txtCity;
 	private JButton btnMutateCustomer;
 
 	Library library;
 	Customer customer;
 	private JLabel lblStatus;
 
 	public CustomerEdit(Customer customer, Library library) {
 		this.library = library;
 		this.customer = customer;
 		initialize();
 		updateFields();
 	}
 
 	private void initialize() {
 		setTitle("Kundenangaben editieren");
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		contentPane = new JPanel();
 		contentPane.setBorder(new TitledBorder(null, "Kundendaten", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		setContentPane(contentPane);
 		GridBagLayout gbl_contentPane = new GridBagLayout();
 		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0 };
 		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
 		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
 		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
 		contentPane.setLayout(gbl_contentPane);
 
 		lblName = new JLabel("Name:");
 		GridBagConstraints gbc_lblName = new GridBagConstraints();
 		gbc_lblName.anchor = GridBagConstraints.WEST;
 		gbc_lblName.insets = new Insets(0, 0, 5, 5);
 		gbc_lblName.gridx = 0;
 		gbc_lblName.gridy = 0;
 		contentPane.add(lblName, gbc_lblName);
 
 		txtName = new JTextField();
 		GridBagConstraints gbc_txtName = new GridBagConstraints();
 		gbc_txtName.gridwidth = 2;
 		gbc_txtName.insets = new Insets(0, 0, 5, 0);
 		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtName.gridx = 1;
 		gbc_txtName.gridy = 0;
 		contentPane.add(txtName, gbc_txtName);
 		txtName.setColumns(10);
 
 		lblSurename = new JLabel("Vorname:");
 		GridBagConstraints gbc_lblSurename = new GridBagConstraints();
 		gbc_lblSurename.anchor = GridBagConstraints.WEST;
 		gbc_lblSurename.insets = new Insets(0, 0, 5, 5);
 		gbc_lblSurename.gridx = 0;
 		gbc_lblSurename.gridy = 1;
 		contentPane.add(lblSurename, gbc_lblSurename);
 
 		txtSurname = new JTextField();
 		GridBagConstraints gbc_txtSurname = new GridBagConstraints();
 		gbc_txtSurname.gridwidth = 2;
 		gbc_txtSurname.insets = new Insets(0, 0, 5, 0);
 		gbc_txtSurname.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtSurname.gridx = 1;
 		gbc_txtSurname.gridy = 1;
 		contentPane.add(txtSurname, gbc_txtSurname);
 		txtSurname.setColumns(10);
 
 		lblStreet = new JLabel("Strasse:");
 		GridBagConstraints gbc_lblStreet = new GridBagConstraints();
 		gbc_lblStreet.anchor = GridBagConstraints.WEST;
 		gbc_lblStreet.insets = new Insets(0, 0, 5, 5);
 		gbc_lblStreet.gridx = 0;
 		gbc_lblStreet.gridy = 2;
 		contentPane.add(lblStreet, gbc_lblStreet);
 
 		txtStreet = new JTextField();
 		GridBagConstraints gbc_txtStreet = new GridBagConstraints();
 		gbc_txtStreet.gridwidth = 2;
 		gbc_txtStreet.insets = new Insets(0, 0, 5, 0);
 		gbc_txtStreet.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtStreet.gridx = 1;
 		gbc_txtStreet.gridy = 2;
 		contentPane.add(txtStreet, gbc_txtStreet);
 		txtStreet.setColumns(10);
 
 		lblZip = new JLabel("PLZ:");
 		GridBagConstraints gbc_lblZip = new GridBagConstraints();
 		gbc_lblZip.anchor = GridBagConstraints.WEST;
 		gbc_lblZip.insets = new Insets(0, 0, 5, 5);
 		gbc_lblZip.gridx = 0;
 		gbc_lblZip.gridy = 3;
 		contentPane.add(lblZip, gbc_lblZip);
 
 		txtZip = new JTextField();
 		GridBagConstraints gbc_txtZip = new GridBagConstraints();
 		gbc_txtZip.gridwidth = 2;
 		gbc_txtZip.insets = new Insets(0, 0, 5, 0);
 		gbc_txtZip.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtZip.gridx = 1;
 		gbc_txtZip.gridy = 3;
 		contentPane.add(txtZip, gbc_txtZip);
 		txtZip.setColumns(10);
 
 		lblCity = new JLabel("Ort:");
 		GridBagConstraints gbc_lblCity = new GridBagConstraints();
 		gbc_lblCity.anchor = GridBagConstraints.WEST;
 		gbc_lblCity.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCity.gridx = 0;
 		gbc_lblCity.gridy = 4;
 		contentPane.add(lblCity, gbc_lblCity);
 
 		txtCity = new JTextField();
 		GridBagConstraints gbc_txtCity = new GridBagConstraints();
 		gbc_txtCity.gridwidth = 2;
 		gbc_txtCity.insets = new Insets(0, 0, 5, 0);
 		gbc_txtCity.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtCity.gridx = 1;
 		gbc_txtCity.gridy = 4;
 		contentPane.add(txtCity, gbc_txtCity);
 		txtCity.setColumns(10);
 
 		btnMutateCustomer = new JButton("Änderungen speichern");
 		btnMutateCustomer.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (verifyFields()) {
 					customer.setName(txtName.getText());
 					customer.setSurname(txtSurname.getText());
 					customer.setStreet(txtStreet.getText());
 					customer.setZip(Integer.valueOf(txtZip.getText()));
 					customer.setCity(txtCity.getText());
 				}
 			}
 		});
 
 		lblStatus = new JLabel();
 		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
 		gbc_lblStatus.anchor = GridBagConstraints.WEST;
 		gbc_lblStatus.insets = new Insets(0, 0, 0, 5);
		gbc_lblStatus.gridx = 1;
 		gbc_lblStatus.gridy = 5;
 		contentPane.add(lblStatus, gbc_lblStatus);
 
 		GridBagConstraints gbc_btnMutateCustomer = new GridBagConstraints();
 		gbc_btnMutateCustomer.anchor = GridBagConstraints.EAST;
 		gbc_btnMutateCustomer.gridx = 2;
 		gbc_btnMutateCustomer.gridy = 5;
 		contentPane.add(btnMutateCustomer, gbc_btnMutateCustomer);
 
 		setVisible(true);
 	}
 
 	private void updateFields() {
 		txtSurname.setText(customer.getSurname());
 		txtName.setText(customer.getName());
 		txtStreet.setText(customer.getStreet());
 		txtZip.setText(String.valueOf(customer.getZip()));
 		txtCity.setText(customer.getCity());
 	}
 
 	private boolean verifyFields() {
 		boolean ok = true;
         if (txtName.getText().equals("")) {
             lblStatus.setForeground(Color.RED);
             lblName.setText("Name*");
             lblName.setForeground(Color.RED);
             ok = false;
         } else if (lblName.getForeground().equals(Color.RED)) {
             lblName.setForeground(Color.BLACK);
             lblName.setText("Name");
         }
         if (txtSurname.getText().equals("")) {
             lblSurename.setText("Vorname*");
             lblSurename.setForeground(Color.RED);
             ok = false;
         } else if (lblSurename.getForeground().equals(Color.RED)) {
             lblSurename.setForeground(Color.BLACK);
             lblSurename.setText("Vorname");
         }
         if (txtStreet.getText().equals("")) {
             lblStreet.setText("Strasse*");
             lblStreet.setForeground(Color.RED);
             ok = false;
         } else if (lblStreet.getForeground().equals(Color.RED)) {
             lblStreet.setForeground(Color.BLACK);
             lblStreet.setText("Strasse");
         }
         if (txtZip.getText().equals("")) {
             lblZip.setText("PLZ*");
             lblZip.setForeground(Color.RED);
             ok = false;
         } else if (!checkZip()) {
             return false;
         } else if (lblZip.getForeground().equals(Color.RED)) {
             lblZip.setForeground(Color.BLACK);
             lblZip.setText("PLZ");
         }
         if (txtCity.getText().equals("")) {
             lblCity.setText("Ort*");
             lblCity.setForeground(Color.RED);
             ok = false;
         } else if (lblCity.getForeground().equals(Color.RED)) {
             lblCity.setForeground(Color.BLACK);
             lblCity.setText("Ort");
         }
         if (ok) {
             lblStatus.setText(" ");
         } else {
             lblStatus.setText("Bitte füllen Sie die Markierten Felder aus");
             lblStatus.setForeground(Color.RED);
         }
         return ok;
 	}
 
 	private boolean checkZip() {
		if (Integer.parseInt(txtZip.getText()) < 0 || Integer.parseInt(txtZip.getText()) > 10000
				|| txtZip.getText().length() != 4) {
			lblStatus.setText("Die PLZ muss eine vierstellige Zahl sein");
			lblZip.setText("PLZ*");
			lblZip.setForeground(new Color(255, 0, 0));
			return false;
		}
		return true;
 	}
 
 }
