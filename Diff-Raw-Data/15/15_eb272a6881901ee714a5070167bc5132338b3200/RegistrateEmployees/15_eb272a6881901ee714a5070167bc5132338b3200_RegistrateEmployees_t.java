 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.*;
 import static javax.swing.JOptionPane.*;
 
 public class RegistrateEmployees extends BasicDialog {
 	private JTextField phone = new JTextField();
 	private JTextField name = new JTextField();
 	private JTextField monthlySalary = new JTextField();
 	private JRadioButton Salesman = new JRadioButton("Salesman");
 	private JRadioButton Chef = new JRadioButton("Chef");
 	private JRadioButton Driver = new JRadioButton("Driver");
 	private JRadioButton CEO = new JRadioButton("CEO");
 	private JRadioButton Secretary = new JRadioButton("Secretary");
 	
 
 	public RegistrateEmployees(JFrame parent) {
 		super(parent, "Employee registration");
 		add(new JPanel(), BorderLayout.NORTH);  // litt "luft"
 	    add(new EmployeeDatapanel(), BorderLayout.CENTER);
 	    add(getButtonpanel(), BorderLayout.SOUTH);
 	    pack();
 	}
 	private class EmployeeDatapanel extends JPanel {
 		public EmployeeDatapanel() {
 			setLayout(new GridLayout(5, 2, 5, 5));
 		    add(new JLabel("Employee phone:", JLabel.RIGHT));
 		    add(phone);
 		    add(new JLabel("Employee name:", JLabel.RIGHT));
 		    add(name);
 		    add(new JLabel("Monthly Salary: ", JLabel.RIGHT));
 		    add(monthlySalary);
 		    
 		    ButtonGroup group = new ButtonGroup();
 		    group.add(Salesman);
 		    group.add(Chef);
 		    group.add(Driver);
 		    group.add(CEO);
 		    group.add(Secretary);
 		    
 		    add(Salesman);
 		    add(Chef);
 		    add(Driver);
 		    add(CEO);
 		    add(Secretary);
 		}
	}  	
 	public Employee regEmployee() {
 	    setOk(false);
 	    int type;
 	    String sql = "";
 	    phone.requestFocusInWindow();
 	    setVisible(true);
 	    Employee employee;
 	    if (isOk()) {
 	    	if(Salesman.isSelected()) {
 	    		employee = new Salesman(Integer.parseInt(phone.getText()), name.getText(), Double.parseDouble(monthlySalary.getText())); 
 	    		return employee;
 	    	}else if(Chef.isSelected()) {
 	    		employee = new Chef(Integer.parseInt(phone.getText()), name.getText(), Double.parseDouble(monthlySalary.getText())); 
 	    		return employee;
 	    	}else if(Driver.isSelected()) {
 	    		employee = new Driver(Integer.parseInt(phone.getText()), name.getText(), Double.parseDouble(monthlySalary.getText())); 
 	    		return employee;
 	    	}else if(CEO.isSelected()) {
 	    		employee = new CEO(Integer.parseInt(phone.getText()), name.getText(), Double.parseDouble(monthlySalary.getText())); 
 	    		return employee;
	    	}else {
 	    		employee = new Secretary(Integer.parseInt(phone.getText()), name.getText(), Double.parseDouble(monthlySalary.getText())); 
 	    		return employee;
 	    	}
	    } else {
	    	return null;
 	    }
 	}
 	protected boolean okData() { //trenger bedre kontroll av data
 		String phone2 = phone.getText();
 		String name2 = name.getText().trim();
 		String monthlySalary2 = monthlySalary.getText();
 		boolean salesman2 = Salesman.isSelected();
 		boolean chef2 = Chef.isSelected();
 		boolean driver2 = Driver.isSelected();
 		boolean ceo2 = CEO.isSelected();
 		boolean secretary2 = Secretary.isSelected();
	    if (phone2.equals("")||name2.equals("") || monthlySalary2.equals("")|| (!salesman2 && !chef2 && !driver2 && !ceo2 && !secretary2)){
	    	if (phone2.equals("")) {
 	    		showMessageDialog(RegistrateEmployees.this, "You have to input phone!");
 	    		phone.requestFocusInWindow();
 	    	} else if (name2.equals("")) {
 	    		showMessageDialog(RegistrateEmployees.this, "You have to input name!");
 	    		name.requestFocusInWindow();
 	    	} else if(monthlySalary2 == null) {
 	    		showMessageDialog(RegistrateEmployees.this, "You have to input salary!");
 	    		monthlySalary.requestFocusInWindow();
 	    	} else if(!salesman2 && !chef2 && !driver2 && !ceo2 && !secretary2) {
 	    		showMessageDialog(RegistrateEmployees.this, "You have to choose a employee type!");
 	    		Salesman.requestFocusInWindow();
 	    	}
 	    	return false;
 	    } else {
 	    	return true;
 	    }
 	}
 
 	
 	    
 	
 	
 	
 	
 
 	}
