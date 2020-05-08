 import java.awt.*;
 import javax.swing.*;
 import static javax.swing.JOptionPane.*;
 
 public class CustomerRegistration extends BasicDialog {
 	private JTextField kid = new JTextField();
 	private JTextField name = new JTextField();
 	private JTextField phone = new JTextField();
 	private JTextField adress = new JTextField();
 	private JRadioButton privateCustomer = new JRadioButton("Private customer", false);
 	private JRadioButton enterpriseCustomer = new JRadioButton("Enterprise customer", false);
 
 	public CustomerRegistration(JFrame parent) {
 		super(parent, "Customer registration");
 		add(new JPanel(), BorderLayout.NORTH);  // litt "luft"
 	    add(new CustomerDatapanel(), BorderLayout.CENTER);
 	    add(getButtonpanel(), BorderLayout.SOUTH);
 	    pack();
 	}
 	private class CustomerDatapanel extends JPanel {
 		public CustomerDatapanel() {
 			setLayout(new GridLayout(5, 2, 5, 5));
 			add(new JLabel("Customer ID (automatic): ", JLabel.RIGHT));
 		    add(kid);
 		    kid.setEditable(false); // brukeren kan ikke endre nummeret
 		    add(new JLabel("Full name:", JLabel.RIGHT));
 		    add(name);
 		    add(new JLabel("Phone number:", JLabel.RIGHT));
 		    add(phone);
 		    add(new JLabel("Adress: ", JLabel.RIGHT));
 		    add(adress);
 		    
 		    ButtonGroup group = new ButtonGroup();
 		    group.add(privateCustomer);
 		    group.add(enterpriseCustomer);
 		    add(privateCustomer);
 		    add(enterpriseCustomer);
 		}
 	}
 	public String[] editCustomer(Customer customer2) {
 		enterpriseCustomer.setFocusable(false); //skal ikke kunne endre kundetypen, BUGGED
 		privateCustomer.setFocusable(false);
 		String tekstNrFelt = (customer2.getKid() < 0)
 	                                  ? "ikke registrert" : "" + customer2.getKid();
 	    kid.setText(tekstNrFelt);
 	    name.setText(customer2.getName());
 	    phone.setText(Integer.toString(customer2.getPhone()));
 	    adress.setText(customer2.getAdress());
 	    if(customer2.getType() == 0) {
 	    	privateCustomer.setSelected(true);
 	    } else {
 	    	enterpriseCustomer.setSelected(true);
 	    	
 	    }
 	    setOk(false);
 	    name.requestFocusInWindow();
 	    String[] sql = new String[3];
 	    if (isOk()) {
 	    	sql[0] = name.getText();
 	    	sql[1] = phone.getText();
 	    	sql[2] = adress.getText();
 	    	return sql;
 	    } else {
 	      return sql;
 	    }
 	}
 	public String regCustomer() {
 	    setOk(false);
 	    int type;
 	    String sql = "";
 	    name.requestFocusInWindow();
 	    if (isOk()) {
 	    	if(enterpriseCustomer.isSelected()) {
 	    		type = 1;
 	    	} else {
 	    		type = 0;
 	    	}
 	    	sql = "INSERT INTO customer(name, phone, adress, type) values('" + name.getText() + "'," + Integer.parseInt(phone.getText()) + ", '" + adress.getText() + "'," + type + ")";
 	    	return sql;
 	    } else {
 	    	return sql;
 	    }
 	}
 	protected boolean okData() { //trenger bedre kontroll av data
 		String name2 = name.getText().trim();
 		String phone2 = phone.getText().trim();
 		String adress2 = adress.getText().trim();
 		boolean enterpriseButton = enterpriseCustomer.isSelected();
 		boolean privateButton = privateCustomer.isSelected();
 	    if (name2.equals("") || phone2.equals("") || adress2.equals("") || (!enterpriseButton && !privateButton)) {
 	    	if (name2.equals("")) {
 	    		showMessageDialog(CustomerRegistration.this, "You have to input a customer name!");
 	    		name.requestFocusInWindow();
 	    	} else if(phone2.equals("")) {
 	    		showMessageDialog(CustomerRegistration.this, "You have to input a phone number!");
 	    		phone.requestFocusInWindow();
 	    	} else if(adress2.equals("")) {
 	    		showMessageDialog(CustomerRegistration.this, "You have to input an adress!");
 	    		adress.requestFocusInWindow();
 	    	} else if(!enterpriseButton && !privateButton) {
 	    		showMessageDialog(CustomerRegistration.this, "You have to choose a customer type!");
 	    		privateCustomer.requestFocusInWindow();
 	    	}
 	    	return false;
 	    } else {
 	    	return true;
 	    }
 	}
 }
