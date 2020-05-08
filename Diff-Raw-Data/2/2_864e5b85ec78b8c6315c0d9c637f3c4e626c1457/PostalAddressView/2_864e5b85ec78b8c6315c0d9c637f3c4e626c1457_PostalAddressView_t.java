 package view;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import model.PostalAddress;
 
 public class PostalAddressView extends AbstractAddressView {
 
 	
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6429235778070722644L;
 	
 	private PostalAddress address;
 	
 	private JTextField nameTextField;
 	private JTextField emailaddressTextField;
 	private JTextField straßeTextfield;
 	private JTextField hausnummerTextfield;
 	private JTextField plzTextField;
 	private JTextField ortTextField;
 	
 	public PostalAddressView(AddressListView alv, PostalAddress address) {
 		super(alv, address);
 		System.out.println("constructing PostalAddressView..");
 		this.address = address;
 		
 		populateFields();
 		
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.setVisible(true);
 		
 	}
 
 	@Override
 	protected void init() {
 		// TODO Auto-generated method stub
		System.out.println("initializing PostalAddressView");
 		// Setting Jframe-title and layout (The Window)
 		setTitle("Email Address");
 		upperPanel.setLayout(new GridLayout(6, 2 , 5, 5 ));
 						
 		// creating labels and textfields for the Address-member-variables
 		JLabel nameLabel = new JLabel("Name");
 		nameTextField = new JTextField();
 
 		JLabel emailaddressLabel = new JLabel("Emailadresse");
 		emailaddressTextField = new JTextField();
 		
 		JLabel straßeLabel = new JLabel("Straße");
 		straßeTextfield = new JTextField();
 		
 		JLabel hausnummerLabel = new JLabel("Hausnummer");
 		hausnummerTextfield = new JTextField();
 		
 		JLabel plzLabel = new JLabel("PLZ");
 		plzTextField = new JTextField();
 		
 		JLabel ortLabel = new JLabel("Ort");
 		ortTextField = new JTextField();		
 		
 		// adding it all to the panel
 		upperPanel.add(nameLabel);
 		upperPanel.add(nameTextField);
 		upperPanel.add(emailaddressLabel);
 		upperPanel.add(emailaddressTextField);
 		upperPanel.add(straßeLabel);
 		upperPanel.add(straßeTextfield);
 		upperPanel.add(hausnummerLabel);
 		upperPanel.add(hausnummerTextfield);
 		upperPanel.add(plzLabel);
 		upperPanel.add(plzTextField);
 		upperPanel.add(ortLabel);
 		upperPanel.add(ortTextField);
 		
 		
 		// adding the panel to the JFrame
 		add(upperPanel, BorderLayout.CENTER);			
 		
 	}
 
 	@Override
 	protected void populateFields() {
 		nameTextField.setText(address.getName());
 		emailaddressTextField.setText(address.getEmailaddress());
 		straßeTextfield.setText(address.getStraße());
 		hausnummerTextfield.setText(address.getHausnummer());
 		plzTextField.setText(address.getPlz());
 		ortTextField.setText(address.getOrt());
 
 	}
 
 	@Override
 	public void retrieveFields() {
 		address.setName(nameTextField.getText());
 		address.setEmailaddress(emailaddressTextField.getText());
 		address.setStraße(straßeTextfield.getText());
 		address.setHausnummer(hausnummerTextfield.getText());
 		address.setPlz(plzTextField.getText());
 		address.setOrt(ortTextField.getText());
 
 	}
 
 }
