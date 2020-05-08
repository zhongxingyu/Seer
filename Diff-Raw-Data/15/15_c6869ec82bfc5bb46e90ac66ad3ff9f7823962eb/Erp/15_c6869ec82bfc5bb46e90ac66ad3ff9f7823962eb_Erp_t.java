 package erp;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
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
 
 /**
  * @author sirde
  * 
  */
 
 public class Erp extends JFrame {
 
 	/**
 	 * Define the default extension of the save file
 	 */
 	public static final String	ERP_EXTENSION	= "erp";
 	/**
 	 * Define if the debug mode is activated
 	 */
 	public final static boolean	DEBUG			= true;
 
 	/**
 	 * @author sirde
 	 * 
 	 */
 	public enum EmployeType
 	{
 		HOURLY_EMPLOYE, SALESMAN, MANAGER;
 	}
 
 	/**
 	 * 
 	 */
 	private static JTable				table;
 	private static DefaultTableModel	tableModel;
 
 	private LinkedList					employeList;
 	private JButton						btnShow;
 	private JFileChooser				chooser;
 	private JButton						btnAdd;
 	private JButton						btnDelete;
 	private JMenu						mnFile;
 	private JMenuBar					menuBar;
 	private JMenuItem					menuSaveFile;
 	private JScrollPane					pane;
 
 	/**
 	 * Launch the application by opening the main frame
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Erp frame = new Erp();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public Erp()
 	{
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 567, 588);
 
 		JPanel GestionPanel = new JPanel();
 		getContentPane().add(GestionPanel, BorderLayout.SOUTH);
 		GestionPanel.setMaximumSize(new Dimension(32767, 600));
 
 		menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 
 		/*
 		 * Definition of the default saving location and extension
 		 */
 		String defaultLocation = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
 		chooser = new JFileChooser(defaultLocation);
 		FileNameExtensionFilter filter = new FileNameExtensionFilter("Employe database", ERP_EXTENSION, ERP_EXTENSION);
 		chooser.setFileFilter(filter);
 		chooser.setAcceptAllFileFilterUsed(false);
 
 		mnFile = new JMenu(Messages.getString("Erp.mnFile.text"));
 		menuBar.add(mnFile);
 
 		JMenuItem menuOpenFile = new JMenuItem(Messages.getString("Erp.mntmOpenFile.text"));
 		menuOpenFile.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				openFile();
 			}
 		});
 
 		mnFile.add(menuOpenFile);
 
 		menuSaveFile = new JMenuItem(Messages.getString("Erp.menuSaveFile.arg0"));
 
 		menuSaveFile.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
 				saveFile();
 			}
 		});
 
 		mnFile.add(menuSaveFile);
 
 		employeList = new LinkedList();
 
 		table = new JTable();
 		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		table.setBounds(0, 0, 300, 0);
 
 		pane = new JScrollPane(table);
 		getContentPane().add(pane, BorderLayout.CENTER);
 
 		String[] columnNames = { Messages.getString("erp.index.text"),
 				Messages.getString("erp.name.text"),
 				Messages.getString("erp.type.text"),
 				Messages.getString("erp.salary.text") };
 
 		tableModel = new DefaultTableModel(columnNames, 0) {
 
 			@Override
 			public boolean isCellEditable(int row, int column) {
 				// all cells false
 				return false;
 			}
 		};
 
 		table.setModel(tableModel);
 
 		table.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					JTable target = (JTable) e.getSource();
 					int row = target.getSelectedRow();
 
 					openEditDialog(row);
 				}
 			}
 		});
 
 		btnAdd = new JButton(Messages.getString("erp.btnAdd.text"));
 
 		btnAdd.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 				if (arg0.getKeyChar() == KeyEvent.VK_ENTER)
 					openAddDialog();
 			}
 		});
 
 		btnAdd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				openAddDialog();
 			}
 		});
 
 		GestionPanel.add(btnAdd);
 
 		btnShow = new JButton(Messages.getString("erp.btnEdit.text"));
 		btnShow.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 
 				if (table.getSelectedRows().length != 1)
 				JOptionPane.showMessageDialog(getContentPane(), "Error, you should select 1 line.");
 				else {
 					openEditDialog(table.getSelectedRow());
 				}
 			}
 		});
 		GestionPanel.add(btnShow);
 
 		btnDelete = new JButton(Messages.getString("erp.btnDelete.text"));
 		btnDelete.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				if (table.getSelectedRows().length < 1)
 					JOptionPane.showMessageDialog(getContentPane(), "Error, you should select at least 1 line.");
 				int[] selection;
 				int index;
 				while (table.getSelectedRows().length > 0) {
 					selection = table.getSelectedRows();
					index = ((int) tableModel.getValueAt(selection[0], 0));
 
 					tableModel.removeRow(selection[0]);
 					employeList.delete(index);
 				}
 				table.clearSelection();
 
 				for (int i = 0; i < table.getRowCount(); i++) {
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
 	private void openEditDialog(int row) {
 
		int index = (int) tableModel.getValueAt(table.getSelectedRow(), 0);
 
 		Employe employe = employeList.get(index);
 		String name = employe.getName();
 		double hourlyRate = 0;
 		double hours = 0;
 		double commission = 0;
 		double sales = 0;
 		double salary = 0;
 		EmployeType employeType = EmployeType.HOURLY_EMPLOYE;
 
 		if (employe instanceof HourlyEmploye) {
 			name = employe.getName();
 			hourlyRate = ((HourlyEmploye) employe).getRate();
 			hours = ((HourlyEmploye) employe).getHours();
 			employeType = EmployeType.HOURLY_EMPLOYE;
 		}
 		if (employe instanceof Manager) {
 			name = employe.getName();
 			salary = ((Manager) employe).getSalary();
 			employeType = EmployeType.MANAGER;
 		}
 		if (employe instanceof Salesman) {
 			name = employe.getName();
 			hourlyRate = ((Salesman) employe).getRate();
 			hours = ((Salesman) employe).getHours();
 			commission = ((Salesman) employe).getCommission();
 			sales = ((Salesman) employe).getSales();
 			employeType = EmployeType.SALESMAN;
 		}
 
 		// Opens the modal dialog
 		DetailDialog editDialog = new DetailDialog(employeType, index, name, hourlyRate, hours, commission, sales,
 				salary);
 		editDialog.setVisible(true);
 
 		// Check if the dialog has been exited with ok or exit
 		if (editDialog.getOkPressed()) {
 
 			hourlyRate = Double.valueOf(editDialog.getTextFieldHourlyRate());
 			employeType = editDialog.getEmployeType();
 			name = editDialog.getTextFieldName();
 			hours = Double.valueOf(editDialog.getTextFieldHours());
 			hourlyRate = Double.valueOf(editDialog.getTextFieldHourlyRate());
 			commission = Double.valueOf(editDialog.getTextFieldCommission());
 			sales = Double.valueOf(editDialog.getTextFieldSales());
 			salary = Double.valueOf(editDialog.getTextFieldSalary());
 
 			Employe newEmploye;
 
 			switch (employeType) {
 			case HOURLY_EMPLOYE:
 				newEmploye = new HourlyEmploye(name, hourlyRate, hours);
 				break;
 
 			case SALESMAN:
 				newEmploye = new Salesman(name, hourlyRate, hours, commission,
 						sales);
 				break;
 
 			case MANAGER:
 			default:
 				newEmploye = new Manager(name, salary);
 				break;
 			}
 
 			if (!newEmploye.equals(employe)) {
 				employeList.replace(index, newEmploye);
				tableModel.setValueAt(newEmploye.getName(), index - 1, 1);
				tableModel.setValueAt(employeType, index - 1, 2);
				tableModel.setValueAt(newEmploye.getPay(), index - 1, 3);
 			}
 		}
 	}
 
 	/**
 	 * Opens the dialog to add a new employee Takes no parameter
 	 */
 	private void openAddDialog() {
 
 		DetailDialog addDialog = new DetailDialog(employeList.getSize() + 1);
 		addDialog.setVisible(true);
 
 		// Checks if the dialog was exited using OK
 		if (addDialog.getOkPressed()) {
 
 			Employe employe;
 			String name = addDialog.getTextFieldName();
 			double hours = Double.valueOf(addDialog.getTextFieldHours());
 			double hourlyRate = Double.valueOf(addDialog.getTextFieldHourlyRate());
 			double commission = Double.valueOf(addDialog.getTextFieldCommission());
 			double sales = Double.valueOf(addDialog.getTextFieldSales());
 			double salary = Double.valueOf(addDialog.getTextFieldSalary());
 
 			EmployeType employeType = addDialog.getEmployeType();
 
 			switch (employeType) {
 			case HOURLY_EMPLOYE:
 				employe = new HourlyEmploye(name, hourlyRate, hours);
 				break;
 
 			case SALESMAN:
 				employe = new Salesman(name, hourlyRate, hours, commission, sales);
 				break;
 
 			case MANAGER:
 			default:
 				employe = new Manager(name, salary);
 				break;
 			}
 
 			Object[] data = { employeList.getSize() + 1, name, employeType, employe.getPay() };
 
 			employeList.add(employe);
 			tableModel.addRow(data);
 		}
 
 	}
 
 	private void saveFile()
 	{
 		int returnVal = chooser.showSaveDialog(getContentPane());
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File file = chooser.getSelectedFile();
 
 			String fileName = file.getName();
 			int position = fileName.lastIndexOf('.');
 			String extension = "";
 			if (position >= 0)
 				extension = fileName.substring(position + 1);
 
 			if (!extension.equals(ERP_EXTENSION))
 				file = new File(file.getPath() + ".erp");
 
 			if (DEBUG)
 				System.out.println("You chose to save this file: " + file.getPath());
 			try {
 				if (!file.exists())
 					file.createNewFile();
 				FileOutputStream fos = new FileOutputStream(file);
 				ObjectOutputStream oos = new ObjectOutputStream(fos);
 				oos.writeObject(employeList);
 				oos.close();
 			} catch (IOException e) {
 				if (DEBUG)
 					System.out.println("Error while saving the file");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void openFile()
 	{
 		int returnVal = chooser.showOpenDialog(getContentPane());
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			System.out.println("You chose to open this file: " + chooser.getSelectedFile().getPath());
 			File file = chooser.getSelectedFile();
 
 			if (file.exists()) {
 				try {
 					FileInputStream fis = new FileInputStream(file);
 					ObjectInputStream ois = new ObjectInputStream(fis);
 					employeList = (LinkedList) ois.readObject();
 					ois.close();
 				} catch (Exception e) {
 					if (DEBUG)
 						System.out.println("Error while opening the file");
 					e.printStackTrace();
 					return;
 				}
 
 				while (tableModel.getRowCount() > 0) {
 					tableModel.removeRow(0);
 				}
 
 				int i = 1;
 				Employe employe;
 				while (i <= employeList.getSize()) {
 					employe = employeList.get(i);
 					EmployeType employeType = EmployeType.MANAGER;
 
 					if (employe instanceof HourlyEmploye)
 						employeType = EmployeType.HOURLY_EMPLOYE;
 
 					if (employe instanceof Manager)
 						employeType = EmployeType.MANAGER;
 
 					if (employe instanceof Salesman)
 						employeType = EmployeType.SALESMAN;
 
 					Object[] data = { i, employe.getName(),
 							employeType, employe.getPay() };
 					tableModel.addRow(data);
 
 					i++;
 				}
 			}
 			else {
 				if (DEBUG)
 					System.out.println("The file does not exist");
 			}
 		}
 	}
 
 }
