 /**
  * 
  */
 package erp;
 
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFormattedTextField;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 import java.text.NumberFormat;
 import javax.swing.Box;
 import javax.swing.JLabel;
 import javax.swing.text.NumberFormatter;
 
 import staff.Employee;
 import staff.HourlyEmployee;
 import staff.Manager;
 import staff.SalesMan;
 
 import java.awt.Component;
 import java.awt.Dimension;
 
 /**
  * @author C. Gerber & O.Gudat
  * 
  */
 public class AddModifyEmployeeDialog extends JDialog implements ActionListener
 {
 
 	private static final long serialVersionUID = 1L;
 
 	/*
 	 * All the element of the GUI
 	 */
 	private JButton okButton;
 	private JTextField textFieldName;
 	private JTextField textFieldID;
 	private JFormattedTextField textFieldHourlyRate;
 	private JFormattedTextField textFieldHours;
 	private JFormattedTextField textFieldCommission;
 	private JFormattedTextField textFieldSales;
 	private JFormattedTextField textFieldSalary;
 	private String employeeType;
 	private Box verticalBoxTextFields;
 	private JLabel labelIndex;
 	private Box verticalBoxLabel;
 	private Box horizontalBoxIndex;
 	private Box horizontalBoxEmployeeType;
 	private JLabel labelEmployeeType;
 	private Component horizontalStrut;
 	private Component horizontalStrut_1;
 	private Box horizontalBoxName;
 	private Box horizontalBoxCommission;
 	private Box horizontalBoxSales;
 	private Box horizontalBoxSalary;
 	private Box horizontalBoxHourlyRate;
 	private Box horizontalBoxHours;
 	private Component horizontalStrut_2;
 	private Component horizontalStrut_3;
 	private Component horizontalStrut_4;
 	private Component horizontalStrut_5;
 	private Component horizontalStrut_6;
 	private JLabel labelName;
 	private JLabel labelCommission;
 	private JLabel labelSales;
 	private JLabel labelSalary;
 	private JLabel labelHourlyRate;
 	private JLabel labelHours;
 	private Component horizontalStrut_7;
 	private JComboBox<String> comboBoxEmployeeType;
 
 	// Boolean used to check if the dialog has been exited through pressing the
 	// OK button
 	private boolean okPressed = false;
 
 	/**
 	 * Constructor used to add an element
 	 * 
 	 * @param id
 	 *            The id of the element to add
 	 */
 	public AddModifyEmployeeDialog(int id)
 	{
 		// Invoke the constructor who get all field explicitly
 		this(Employee.CLASS_NAME, id, "", HourlyEmployee.DEFAULT_WAGERATE,
 				HourlyEmployee.DEFAULT_HOURS, SalesMan.DEFAULT_COMMISSION,
 				SalesMan.DEFAULT_SALES, Manager.DEFAULT_SALARY);
 	}
 
 	/**
 	 * Constructor used to edit an employee
 	 * 
 	 * @param employeeType
 	 * @param name
 	 * @param id
 	 * @param wageRate
 	 * @param hours
 	 * @param commission
 	 * @param sales
 	 * @param salary
 	 * @wbp.parser.constructor
 	 */
 	public AddModifyEmployeeDialog(String employeeType, int id, String name,
 			double wageRate, double hours, double commission, double sales,
 			double salary)
 	{
 		super((Frame) null, "Mon dialogue", true);
 
 		/*
 		 * use a NumberFormat and NumberFormatter object to force the user to
 		 * fill only valid number in JFormattedTextField dedicated for numbers
 		 */
 		NumberFormat numberFormat = NumberFormat.getNumberInstance();
 		numberFormat.setMaximumFractionDigits(2);
 		numberFormat.setGroupingUsed(false);
 		NumberFormatter nf = new NumberFormatter(numberFormat);
 		nf.setMinimum(0.0);
 		nf.setMaximum(999999.9);
 		nf.setAllowsInvalid(false);
 
 		setSize(300, 238);
 		setLocationRelativeTo(null);
 
 		java.awt.Container content = getContentPane();
 		content.setLayout(new FlowLayout());
 
 		verticalBoxLabel = Box.createVerticalBox();
 		getContentPane().add(verticalBoxLabel);
 
 		verticalBoxTextFields = Box.createVerticalBox();
 		getContentPane().add(verticalBoxTextFields);
 
 		horizontalBoxEmployeeType = Box.createHorizontalBox();
 		verticalBoxTextFields.add(horizontalBoxEmployeeType);
 
 		labelEmployeeType = new JLabel("Type d'employ");
 		labelEmployeeType.setPreferredSize(new Dimension(100, 20));
 		horizontalBoxEmployeeType.add(labelEmployeeType);
 
 		horizontalStrut_1 = Box.createHorizontalStrut(20);
 		horizontalBoxEmployeeType.add(horizontalStrut_1);
 
 		comboBoxEmployeeType = new JComboBox<String>();
 
 		horizontalBoxEmployeeType.add(comboBoxEmployeeType);
 
 		comboBoxEmployeeType.setModel(new DefaultComboBoxModel<String>(
 				new String[]
 				{
 						HourlyEmployee.CLASS_NAME, SalesMan.CLASS_NAME,
 						Manager.CLASS_NAME
 				}));
 		
 		// This updates the GUI when the comboBox value is changed
 		comboBoxEmployeeType.addItemListener(new ItemListener()
 		{
 			@Override
 			public void itemStateChanged(ItemEvent e)
 			{
 				updateGUI();
 			}
 		});
 
 		horizontalBoxIndex = Box.createHorizontalBox();
 		verticalBoxTextFields.add(horizontalBoxIndex);
 
 		labelIndex = new JLabel("Index");
 		labelIndex.setPreferredSize(new Dimension(100, 14));
 		horizontalBoxIndex.add(labelIndex);
 
 		horizontalStrut_7 = Box.createHorizontalStrut(20);
 		horizontalBoxIndex.add(horizontalStrut_7);
 
 		textFieldID = new JTextField();
 		horizontalBoxIndex.add(textFieldID);
 		textFieldID.setEditable(false);
 		textFieldID.setText(String.valueOf(id));
 
 		horizontalBoxName = Box.createHorizontalBox();
 		verticalBoxTextFields.add(horizontalBoxName);
 
 		labelName = new JLabel("Nom");
 		labelName.setPreferredSize(new Dimension(100, 14));
 		horizontalBoxName.add(labelName);
 
 		horizontalStrut = Box.createHorizontalStrut(20);
 		horizontalBoxName.add(horizontalStrut);
 
 		textFieldName = new JTextField();
 		horizontalBoxName.add(textFieldName);
 		textFieldName.setText(name);
 		textFieldName.setColumns(10);
 
 		horizontalBoxCommission = Box.createHorizontalBox();
 		verticalBoxTextFields.add(horizontalBoxCommission);
 
 		labelCommission = new JLabel("Commission");
 		labelCommission.setPreferredSize(new Dimension(100, 14));
 		horizontalBoxCommission.add(labelCommission);
 
 		horizontalStrut_2 = Box.createHorizontalStrut(20);
 		horizontalBoxCommission.add(horizontalStrut_2);
 
 		textFieldCommission = new JFormattedTextField(nf);
 		horizontalBoxCommission.add(textFieldCommission);
 		textFieldCommission.setValue(commission);
 		textFieldCommission.setColumns(10);
 
 		horizontalBoxSales = Box.createHorizontalBox();
 		verticalBoxTextFields.add(horizontalBoxSales);
 
 		labelSales = new JLabel("Ventes");
 		labelSales.setPreferredSize(new Dimension(100, 14));
 		horizontalBoxSales.add(labelSales);
 
 		horizontalStrut_3 = Box.createHorizontalStrut(20);
 		horizontalBoxSales.add(horizontalStrut_3);
 
 		textFieldSales = new JFormattedTextField(nf);
 		horizontalBoxSales.add(textFieldSales);
 		textFieldSales.setValue(sales);
 		textFieldSales.setColumns(10);
 
 		horizontalBoxHourlyRate = Box.createHorizontalBox();
 		verticalBoxTextFields.add(horizontalBoxHourlyRate);
 
 		labelHourlyRate = new JLabel("Tarif horaire");
 		labelHourlyRate.setPreferredSize(new Dimension(100, 14));
 		horizontalBoxHourlyRate.add(labelHourlyRate);
 
 		horizontalStrut_5 = Box.createHorizontalStrut(20);
 		horizontalBoxHourlyRate.add(horizontalStrut_5);
 
 		textFieldHourlyRate = new JFormattedTextField(nf);
 		horizontalBoxHourlyRate.add(textFieldHourlyRate);
 		textFieldHourlyRate.setValue(wageRate);
 		textFieldHourlyRate.setColumns(10);
 
 		horizontalBoxHours = Box.createHorizontalBox();
 		verticalBoxTextFields.add(horizontalBoxHours);
 
 		labelHours = new JLabel("Heure");
 		labelHours.setPreferredSize(new Dimension(100, 14));
 		horizontalBoxHours.add(labelHours);
 
 		horizontalStrut_6 = Box.createHorizontalStrut(20);
 		horizontalBoxHours.add(horizontalStrut_6);
 
 		textFieldHours = new JFormattedTextField(nf);
 
 		horizontalBoxHours.add(textFieldHours);
 		textFieldHours.setValue(hours);
 		textFieldHours.setColumns(10);
 
 		horizontalBoxSalary = Box.createHorizontalBox();
 		verticalBoxTextFields.add(horizontalBoxSalary);
 
 		labelSalary = new JLabel("Salaire");
 		labelSalary.setPreferredSize(new Dimension(100, 14));
 		horizontalBoxSalary.add(labelSalary);
 
 		horizontalStrut_4 = Box.createHorizontalStrut(20);
 		horizontalBoxSalary.add(horizontalStrut_4);
 
 		textFieldSalary = new JFormattedTextField(nf);
 		horizontalBoxSalary.add(textFieldSalary);
 		textFieldSalary.setValue(salary);
 		textFieldSalary.setColumns(10);
 
 		okButton = new JButton("OK");
 
 		verticalBoxTextFields.add(okButton);
 		okButton.addActionListener(this);
 
 		//Update the GUI in order to show the correct fields and value
 		updateGUI();
 	}
 
 	// this private method manage the field to show depending on the kind of
 	// employee selected in the comboBoxEmployeeType
 	private void updateGUI()
 	{
 		// Use the benefit of the new functionality in Java 7 (-> String in
 		// switch statement) ;-)
 		// We enable/disable the different field corresponding to the type of
 		// employee selected
 		switch ((String) comboBoxEmployeeType.getSelectedItem())
 		{
 			case HourlyEmployee.CLASS_NAME:
 				horizontalBoxSales.setVisible(false);
 				horizontalBoxCommission.setVisible(false);
 				horizontalBoxHourlyRate.setVisible(true);
 				horizontalBoxHours.setVisible(true);
 				horizontalBoxSalary.setVisible(false);
 				employeeType = HourlyEmployee.CLASS_NAME;
 				break;
 
 			case SalesMan.CLASS_NAME:
 				horizontalBoxSales.setVisible(true);
 				horizontalBoxCommission.setVisible(true);
 				horizontalBoxHourlyRate.setVisible(true);
 				horizontalBoxHours.setVisible(true);
 				horizontalBoxSalary.setVisible(false);
 				employeeType = SalesMan.CLASS_NAME;
 				break;
 			case Manager.CLASS_NAME:
 				horizontalBoxSales.setVisible(false);
 				horizontalBoxCommission.setVisible(false);
 				horizontalBoxHourlyRate.setVisible(false);
 				horizontalBoxHours.setVisible(false);
 				horizontalBoxSalary.setVisible(true);
 				employeeType = Manager.CLASS_NAME;
 				break;
 			default:
 				assert (false); // should never occur !
 		}
 	}
 
 	/**
 	 * @param event
 	 */
 	public void actionPerformed(ActionEvent e)
 	{
 		if (e.getSource() == okButton)
 		{
 			// Check if name doesn't exist and show
 			// a dialog box to inform the user the fill it if it's the case
 			// else hide the AddModifyEmployeeDialog and go back to erp JFrame
 
 			if (textFieldName.getText().equals(""))
 			{
 				JOptionPane.showMessageDialog(this, "Please enter a name",
 						"Warning", JOptionPane.WARNING_MESSAGE);
 			}
 			else
 			{
 				okPressed = true;
 				setVisible(false);
 			}
 		}
 	}
 
 	/**
 	 * @return hours
 	 */
 	public double getTextFieldHours()
 	{
 		return (double) textFieldHours.getValue();
 	}
 
 	/**
 	 * @return name
 	 */
 	public String getTextFieldName()
 	{
 		return textFieldName.getText();
 	}
 
 	/**
 	 * @return index
 	 */
 	public String getTextFieldID()
 	{
 		return textFieldID.getText();
 	}
 
 	/**
 	 * @return Hourly rate
 	 */
 	public double getTextFieldHourlyRate()
 	{
 		return (double) textFieldHourlyRate.getValue();
 	}
 
 	/**
 	 * @return Commission
 	 */
 	public double getTextFieldCommission()
 	{
 		return (double) textFieldCommission.getValue();
 	}
 
 	/**
 	 * @return Sales
 	 */
 	public Double getTextFieldSales()
 	{
 		return (double) textFieldSales.getValue();
 	}
 
 	/**
 	 * @return Salary
 	 */
 	public double getTextFieldSalary()
 	{
 		return (double) textFieldSalary.getValue();
 	}
 
 	/**
 	 * @return Employee Type
 	 */
 	public String getEmployeeType()
 	{
 		return employeeType;
 	}
 
 	/**
 	 * @return if ok was pressed
 	 */
 	public boolean getOkPressed()
 	{
 		return okPressed;
 	}
 }
