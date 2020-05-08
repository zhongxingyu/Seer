 package erp;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JTable;
 import javax.swing.JScrollPane;
 import java.awt.BorderLayout;
 
 import javax.swing.JFileChooser;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JOptionPane;
 
 import java.awt.Dimension;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.File;
 import javax.swing.ListSelectionModel;
 
 import staff.*;
 import utility.LinkedList;
 
 /**
  * @author C. Gerber & O.Gudat
  * @definition ERP: means Enterprise ressource planning
 * @version
  * 
  */
 
 public class Erp extends JFrame
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1272665389579959888L;
 
 	/**
 	 * Define the default extension of the save file
 	 */
 	public static final String ERP_EXTENSION = "erp";
 	/**
 	 * Define if the debug mode is activated
 	 */
 	public final static boolean DEBUG = true;
 
 	/**
 	 * The list which will the employee
 	 */
 	private LinkedList<Employee> employeeList;
 
 	/**
 	 * The various elements of the GUI
 	 */
 	private static JTable table;
 	private static DefaultTableModel tableModel;
 	private JButton btnShow;
 	private JFileChooser chooser;
 	private JButton btnAdd;
 	private JButton btnDelete;
 	private JMenu mnFile;
 	private JMenuBar menuBar;
 	private JMenuItem menuSaveFile;
 	private JScrollPane pane;
 
 	/**
 	 * Launch the application by opening the main frame
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		EventQueue.invokeLater(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
 					Erp frame = new Erp();
 					frame.setVisible(true);
 				}
 				catch (Exception e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public Erp()
 	{
 		setTitle("Gestionnaire d'employ\u00E9s");
 		setMinimumSize(new Dimension(300, 300));
 		getContentPane().setMinimumSize(new Dimension(150, 150));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 567, 588);
 
 		JPanel GestionPanel = new JPanel();
 		getContentPane().add(GestionPanel, BorderLayout.SOUTH);
 		GestionPanel.setMaximumSize(new Dimension(32767, 600));
 
 		/*
 		 * Definition of the default saving location and extension
 		 */
 		String defaultLocation = getClass().getProtectionDomain()
 				.getCodeSource().getLocation().getFile();
 		chooser = new JFileChooser(defaultLocation);
 		FileNameExtensionFilter filter = new FileNameExtensionFilter(
 				"Employee database", ERP_EXTENSION, ERP_EXTENSION);
 		chooser.setFileFilter(filter);
 		chooser.setAcceptAllFileFilterUsed(false);
 
 		/*
 		 * Add the menu elements
 		 */
 		menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		mnFile = new JMenu("fichier");
 		menuBar.add(mnFile);
 
 		JMenuItem menuOpenFile = new JMenuItem("Ouvrir Fichier");
 		menuOpenFile.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				openFile();
 			}
 		});
 
 		mnFile.add(menuOpenFile);
 
 		menuSaveFile = new JMenuItem("Sauver fichier");
 
 		menuSaveFile.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 
 				saveFile();
 			}
 		});
 
 		mnFile.add(menuSaveFile);
 
 		/*
 		 * Creates the list which will contains all the employe
 		 */
 		employeeList = new LinkedList<Employee>();
 
 		table = new JTable();
 		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		table.setBounds(0, 0, 300, 0);
 
 		pane = new JScrollPane(table);
 		getContentPane().add(pane, BorderLayout.CENTER);
 
 		// Defines the names of the column to be shown in the GUI table
 		String[] columnNames =
 		{
 				"Index", "nom", "Type", "Salaire"
 		};
 
 		// TableModel used to manage the data in the table
 		// it needs to override the method isCellEditable in order to prevent
 		// the editing of cells
 		tableModel = new DefaultTableModel(columnNames, 0)
 		{
 
 			private static final long serialVersionUID = -4957135629083872096L;
 
 			@Override
 			public boolean isCellEditable(int row, int column)
 			{
 				// all cells are false/non editable
 				return false;
 			}
 		};
 
 		table.setModel(tableModel);
 
 		/*
 		 * Set the double click on an element to edit the line clicked
 		 */
 		table.addMouseListener(new MouseAdapter()
 		{
 			public void mouseClicked(MouseEvent e)
 			{
 				if (e.getClickCount() == 2)
 				{
 					JTable target = (JTable) e.getSource();
 					int row = target.getSelectedRow();
 
 					editEmployee(row);
 				}
 			}
 		});
 
 		// Adds the add button
 		btnAdd = new JButton("Ajouter");
 		btnAdd.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 				addEmployee();
 			}
 		});
 
 		GestionPanel.add(btnAdd);
 
 		// Adds the show/edit button
 		btnShow = new JButton("Editer");
 		btnShow.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent arg0)
 			{
 
 				if (table.getSelectedRows().length != 1) JOptionPane
 						.showMessageDialog(getContentPane(),
 								"Erreur, il faut slectionner une ligne");
 				else
 				{
 					editEmployee(table.getSelectedRow());
 				}
 			}
 		});
 		GestionPanel.add(btnShow);
 
 		// Adds the Delete button
 		btnDelete = new JButton("Effacer");
 		btnDelete.addMouseListener(new MouseAdapter()
 		{
 			@Override
 			public void mouseClicked(MouseEvent arg0)
 			{
 				// Show an error if less than 1 line is selected
 				if (table.getSelectedRows().length < 1) JOptionPane
 						.showMessageDialog(getContentPane(),
 								"Erreur, vous devez slectionner 1 ligne ou plus");
 
 				// Delete the selected rows
 				int[] selection;
 				int index;
 				while (table.getSelectedRows().length > 0)
 				{
 					selection = table.getSelectedRows();
 					index = ((int) tableModel.getValueAt(selection[0], 0));
 
 					// Delete the item in the gui
 					tableModel.removeRow(selection[0]);
 					// And in the List
 					employeeList.delete(index);
 				}
 				table.clearSelection();
 
 				// Reattribute the index in the GUI (The list does not need to
 				// be updated)
 				for (int i = 0; i < table.getRowCount(); i++)
 				{
 					tableModel.setValueAt(i + 1, i, 0);
 				}
 			}
 		});
 
 		GestionPanel.add(btnDelete);
 	}
 
 	/**
 	 * Create the dialog to show and edit the data
 	 * 
 	 * @param row
 	 *            the row to be shown/edited
 	 */
 	private void editEmployee(int row)
 	{
 		// store the index of the element to edit
 		int index = (int) tableModel.getValueAt(table.getSelectedRow(), 0);
 
 		// Store the type the employe list
 		Employee employee = employeeList.get(index);
 
 		// A variable of each fields is created with the default value
 		String name = employee.getName();
 		double hourlyRate = 0;
 		double hours = 0;
 		double commission = 0;
 		double sales = 0;
 		double salary = 0;
 		String employeeType = Employee.CLASS_NAME;
 
 		/*
 		 * Set the variables accordingly to the employe type
 		 */
 		if (employee instanceof HourlyEmployee)
 		{
 			name = employee.getName();
 			hourlyRate = ((HourlyEmployee) employee).getWageRate();
 			hours = ((HourlyEmployee) employee).getHours();
 			employeeType = HourlyEmployee.CLASS_NAME;
 		}
 		else if (employee instanceof Manager)
 		{
 			name = employee.getName();
 			salary = ((Manager) employee).getSalary();
 			employeeType = Manager.CLASS_NAME;
 		}
 		else if (employee instanceof SalesMan)
 		{
 			name = employee.getName();
 			hourlyRate = ((SalesMan) employee).getWageRate();
 			hours = ((SalesMan) employee).getHours();
 			commission = ((SalesMan) employee).getCommission();
 			sales = ((SalesMan) employee).getSales();
 			employeeType = SalesMan.CLASS_NAME;
 		}
 
 		// Opens the modal dialog
 		AddModifyEmployeeDialog editDialog = new AddModifyEmployeeDialog(
 				employeeType, index, name, hourlyRate, hours, commission,
 				sales, salary);
 		editDialog.setVisible(true);
 
 		// Check if the dialog has been exited with OK button or exit
 		if (editDialog.getOkPressed())
 		{
 
 			// Read the value of all fields of the dialog
 			hourlyRate = editDialog.getTextFieldHourlyRate();
 			employeeType = editDialog.getEmployeeType();
 			name = editDialog.getTextFieldName();
 			hours = editDialog.getTextFieldHours();
 			hourlyRate = editDialog.getTextFieldHourlyRate();
 			commission = editDialog.getTextFieldCommission();
 			sales = editDialog.getTextFieldSales();
 			salary = editDialog.getTextFieldSalary();
 
 			Employee newEmployee;
 
 			// Create an employee according to the type returned
 			switch (employeeType)
 			{
 				case HourlyEmployee.CLASS_NAME:
 					newEmployee = new HourlyEmployee(name, hourlyRate, hours);
 					break;
 
 				case SalesMan.CLASS_NAME:
 					newEmployee = new SalesMan(name, hourlyRate, hours,
 							commission, sales);
 					break;
 
 				case Manager.CLASS_NAME:
 				default:
 					newEmployee = new Manager(name, salary);
 					break;
 			}
 
 			/**
 			 * If the new employee is not the same as the initial one, it means
 			 * it changed and needs to be updated in the gui and the list
 			 */
 			if (!newEmployee.equals(employee))
 			{
 				employeeList.replace(index, newEmployee);
 				tableModel.setValueAt(newEmployee.getName(), index - 1, 1);
 				tableModel.setValueAt(employeeType, index - 1, 2);
 				tableModel.setValueAt(newEmployee.getPay(), index - 1, 3);
 			}
 		}
 	}
 
 	/**
 	 * Opens the dialog to add a new employee Takes no parameter
 	 */
 	private void addEmployee()
 	{
 
 		// Open the modal dialog which is used to add an employee
 		AddModifyEmployeeDialog addEmployeeDialog = new AddModifyEmployeeDialog(
 				employeeList.getSize() + 1);
 		addEmployeeDialog.setVisible(true);
 
 		// Checks if the dialog was exited using OK
 		if (addEmployeeDialog.getOkPressed())
 		{
 			// A variable of each fields is created and set to the value
 			// returned by the dialog
 			String name = addEmployeeDialog.getTextFieldName();
 			double hours = addEmployeeDialog.getTextFieldHours();
 			double hourlyRate = addEmployeeDialog.getTextFieldHourlyRate();
 			double commission = addEmployeeDialog.getTextFieldCommission();
 			double sales = addEmployeeDialog.getTextFieldSales();
 			double salary = addEmployeeDialog.getTextFieldSalary();
 
 			String employeeType = addEmployeeDialog.getEmployeeType();
 
 			Employee employee;
 
 			// An employee is created according to the type returned by the
 			// dialog
 			switch (employeeType)
 			{
 				case HourlyEmployee.CLASS_NAME:
 					employee = new HourlyEmployee(name, hourlyRate, hours);
 					break;
 
 				case SalesMan.CLASS_NAME:
 					employee = new SalesMan(name, hourlyRate, hours,
 							commission, sales);
 					break;
 
 				case Manager.CLASS_NAME:
 				default:
 					employee = new Manager(name, salary);
 					break;
 			}
 
			// Add the new employee created to the list
 			employeeList.addAtEnd(employee);
 
			// and add it in the GUI
 			Object[] data =
 			{
					employeeList.getSize(), name, employeeType,
 					employee.getPay()
 			};
 
 			tableModel.addRow(data);
 		}
 
 	}
 
 	private void saveFile()
 	{
 		// First we test if there are any data to save. If it's not the case,
 		// we show a pop-up to inform the user
 		if (employeeList.isEmpty())
 		{
 			JOptionPane.showMessageDialog(this, "Aucune donne a sauvegard !",
 					"Information", JOptionPane.INFORMATION_MESSAGE);
 		}
 		// Else, we checks if the OK button in SaveDialog has been pressed
 		else if (chooser.showSaveDialog(getContentPane()) == JFileChooser.APPROVE_OPTION)
 		{
 			// Stores the path
 			File file = chooser.getSelectedFile();
 
 			/*
 			 * Checks the extension of the file, if not equal to the one defined
 			 * for the application, it is added at the end.
 			 */
 			String fileName = file.getName();
 			int position = fileName.lastIndexOf('.');
 			String extension = "";
 			if (position >= 0) extension = fileName.substring(position + 1);
 			if (!extension.equals(ERP_EXTENSION)) file = new File(
 					file.getPath() + ".erp");
 
 			// Used for debug purpose
 			if (DEBUG) System.out.println("You chose to save this file: "
 					+ file.getPath());
 			try
 			{
 				/*
 				 * Save the whole employee list in the file
 				 */
 				if (!file.exists()) file.createNewFile();
 				FileOutputStream fos = new FileOutputStream(file);
 				ObjectOutputStream oos = new ObjectOutputStream(fos);
 				oos.writeObject(employeeList);
 				oos.close();
 			}
 			catch (IOException e)
 			{
 				if (DEBUG) System.out.println("Error while saving the file");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// Suppress the warning caused by the casting of the deserialized element in
 	// a generic list
 	@SuppressWarnings("unchecked")
 	/**
 	 * Function used to open the file containing the serialized employee list
 	 * All the element of the actual list and GUI are erased when opening a file
 	 */
 	private void openFile()
 	{
 		// Open the dialog which allows choosing the file to open
 		int returnVal = chooser.showOpenDialog(getContentPane());
 
 		// Check if the OK button was pressed
 		if (returnVal == JFileChooser.APPROVE_OPTION)
 		{
 			// Used for debug purposes
 			if (DEBUG) System.out.println("You chose to open this file: "
 					+ chooser.getSelectedFile().getPath());
 
 			File file = chooser.getSelectedFile();
 
 			// Checks if the file exists
 			if (file.exists())
 			{
 				try
 				{
 					// Reads the file and put it in the employee list
 					FileInputStream fis = new FileInputStream(file);
 					ObjectInputStream ois = new ObjectInputStream(fis);
 					employeeList = (LinkedList<Employee>) ois.readObject();
 					ois.close();
 				}
 				catch (Exception e)
 				{
 					// Used for debug purpose only
 					if (DEBUG) System.out
 							.println("Error while opening the file");
 					e.printStackTrace();
 					return;
 				}
 
 				// Erase all rows of the GUI
 				while (tableModel.getRowCount() > 0)
 				{
 					tableModel.removeRow(0);
 				}
 
 				// Fill the table according to the new employee list
 				int i = 1;
 				Employee employee;
 				while (i <= employeeList.getSize())
 				{
 					employee = employeeList.get(i);
 					String employeeType = Employee.CLASS_NAME;
 
 					if (employee instanceof HourlyEmployee) employeeType = HourlyEmployee.CLASS_NAME;
 
 					if (employee instanceof Manager) employeeType = Manager.CLASS_NAME;
 
 					if (employee instanceof SalesMan) employeeType = SalesMan.CLASS_NAME;
 
 					Object[] data =
 					{
 							i, employee.getName(), employeeType,
 							employee.getPay()
 					};
 					tableModel.addRow(data);
 
 					i++;
 				}
 			}
 			else
 			{
 				// used for debug purpose only
 				if (DEBUG) System.out.println("The file does not exist");
 			}
 		}
 	}
 
 }
