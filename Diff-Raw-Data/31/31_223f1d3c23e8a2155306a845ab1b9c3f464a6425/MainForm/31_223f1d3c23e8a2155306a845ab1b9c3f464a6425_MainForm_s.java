 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreePath;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.GroupLayout;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTree;
 import javax.swing.JTable;
 import javax.swing.JButton;
 
 import core.DBManager;
 import core.DataBase;
 import core.RMIInterface;
 import core.RMIWrapper;
 import core.Table;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.net.InetAddress;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 public class MainForm extends JFrame implements ActionListener, MouseListener, TableModelListener {
 
 	private JPanel contentPane;
 	private JTable table;
 	private JPanel panel;
 	private static JButton saveBtn;
 	private static JButton cancelBtn;
 	private static JButton createDBBtn;
 	private static JButton createTableBtn;
 	private static JButton removeTableBtn;
 	private static JButton projectionBtn;
 	private static JButton joinTablesBtn;
 	private static JButton removeDB;
 	private static JButton addColumnBtn;
 	private static JButton deleteColumnBtn;
 	private static JButton addRowBtn;
 	private static JButton deleteRowBtn;
 	private static JButton exitButton;
 	private static DBManager dbManager;
 	private JPanel panel_1;
 	private JPanel panel_2;
 	private JTree tree;
 	
 	private String selectedTable;
 	private String selectedDB;
 
 	private enum BUTTON_SET_LEVEL {MANAGER_LEVEL, DATABASE_LEVEL, TABLE_LEVEL};
 	
 	/**
 	 * Create the frame.
 	 */
 	public MainForm() {
 		dbManager = new DBManager();
 		
 		/*
 		 * RMI JNDI implementation
 		 * 
 		dbManager = null;
 		Registry registry;
 		try {
 			registry = LocateRegistry.getRegistry("127.0.1.1",12345);
 			
 			RMIInterface wr = (RMIInterface)(registry.lookup("RMIWrapper"));
 			dbManager = wr.getManager();
 			if(dbManager == null)
 				System.out.println("ERROR");
 			else
 				System.out.println("SUCCESS");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	    */
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//setBounds(50, 50, 950, 700);
 		//contentPane = new JPanel();
 		//contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		//contentPane.setLayout(new BorderLayout(0, 0));
 		//setContentPane(contentPane);
 		
 		Integer height = new Integer(600);
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridLayout(1, 1, 0, 0));
 		panel.setBorder(BorderFactory.createTitledBorder("Databases:"));
 		panel.setBackground(Color.white);
 		tree = new JTree(MainFormMng.createTreeNodes(dbManager));
 		tree.setMinimumSize(new Dimension(75,height));
 		panel.add(tree);
 		tree.addMouseListener(this);
 		
 		panel_1 = new JPanel();
 		panel_1.setBorder(BorderFactory.createLoweredBevelBorder());
 		//contentPane.add(panel_1, BorderLayout.CENTER);
 		
 		table = new JTable();
 		table.setMinimumSize(new Dimension(700, height));
 		table.setFillsViewportHeight(true);
 		table.getModel().addTableModelListener(this);
 		table.setColumnSelectionAllowed(true);
 		panel_1.add(new JScrollPane(table));
 		panel_1.setLayout(new GridLayout(1, 1, 0, 0));
 		
 		//contentPane.add(panel, BorderLayout.EAST);
 		
 		panel_2 = new JPanel();
 		panel_2.setBorder(BorderFactory.createTitledBorder("Control panel:"));
 		
 		GridBagConstraints separatorConstraint = new GridBagConstraints();
 		separatorConstraint.weightx = 1.0;
 		separatorConstraint.fill = GridBagConstraints.HORIZONTAL;
 		separatorConstraint.gridwidth = GridBagConstraints.REMAINDER;
 
 		JSeparator separator  = new JSeparator(JSeparator.HORIZONTAL);
 		JSeparator separator2 = new JSeparator(JSeparator.HORIZONTAL);
 		JSeparator separator3 = new JSeparator(JSeparator.HORIZONTAL);
 		JSeparator separator4 = new JSeparator(JSeparator.HORIZONTAL);
 		JSeparator separator5 = new JSeparator(JSeparator.HORIZONTAL);
 		Dimension separatorDimension = new Dimension(
 				Integer.MAX_VALUE,
 				separator.getPreferredSize().height);
 		separator.setMaximumSize(separatorDimension);
 		separator2.setMaximumSize(separatorDimension);
 		separator3.setMaximumSize(separatorDimension);
 		separator4.setMaximumSize(separatorDimension);
 		separator5.setMaximumSize(separatorDimension);
 		
 		exitButton = new JButton("Exit");
 		exitButton.setActionCommand("EXIT");
 		exitButton.addActionListener(this);	
 
 		saveBtn = new JButton("Save");
 		saveBtn.setActionCommand("SAVE");
 		saveBtn.addActionListener(this);
 		
 		cancelBtn = new JButton("Cancel");
 		cancelBtn.setActionCommand("CANCEL");
 		cancelBtn.addActionListener(this);
 		
 		createDBBtn = new JButton("Create DB");
 		createDBBtn.setActionCommand("CREATE_DB");
 		createDBBtn.addActionListener(this);
 		
 		removeDB = new JButton("Remove DB");
 		removeDB.setActionCommand("DELETE_DB");
 		removeDB.addActionListener(this);
 				
 		createTableBtn = new JButton("Create Table");
 		createTableBtn.setActionCommand("CREATE_TABLE");
 		createTableBtn.addActionListener(this);
 				
 		removeTableBtn = new JButton("Remove Table");
 		removeTableBtn.setActionCommand("DELETE_TABLE");
 		removeTableBtn.addActionListener(this);
 		
 		addColumnBtn = new JButton("Add column");
 		addColumnBtn.setActionCommand("CREATE_COLUMN");
 		addColumnBtn.addActionListener(this);
 		
 		deleteColumnBtn = new JButton("Remove column");
 		deleteColumnBtn.setActionCommand("DELETE_COLUMN");
 		deleteColumnBtn.addActionListener(this);
 		
 		addRowBtn = new JButton("Add row");
 		addRowBtn.setActionCommand("ADD_ROW");
 		addRowBtn.addActionListener(this);
 				
 		deleteRowBtn = new JButton("Remove row");
 		deleteRowBtn.setActionCommand("DELETE_ROW");
 		deleteRowBtn.addActionListener(this); 
 				
 		projectionBtn = new JButton("Tables Projection");
 		projectionBtn.setActionCommand("PROJECTION");
 		projectionBtn.addActionListener(this);
 		
 		joinTablesBtn = new JButton("Tables Join");
 		joinTablesBtn.setActionCommand("JOIN");
 		joinTablesBtn.addActionListener(this);
 		
 		GroupLayout panel2Layout = new GroupLayout(panel_2);
 		panel_2.setLayout(panel2Layout);
 		panel2Layout.setAutoCreateContainerGaps(true);
 		panel2Layout.setAutoCreateGaps(true);
 		
 		panel2Layout.setVerticalGroup(
 				panel2Layout.createSequentialGroup()
 					.addComponent(saveBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(cancelBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(separator2)
 					.addComponent(createDBBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(removeDB, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(separator3)
 					.addComponent(createTableBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(removeTableBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(separator4)
 					.addComponent(addColumnBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(deleteColumnBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(addRowBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(deleteRowBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(separator5)
 					.addComponent(joinTablesBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(projectionBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(separator)
 					.addComponent(exitButton, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				);
 		
 		panel2Layout.setHorizontalGroup(
 				panel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 					.addComponent(saveBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(cancelBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(separator2)
 					.addComponent(createDBBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(removeDB, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(separator3)
 					.addComponent(createTableBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(removeTableBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(separator4)
 					.addComponent(addColumnBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(deleteColumnBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(addRowBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(deleteRowBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(separator5)
 					.addComponent(joinTablesBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(projectionBtn, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 					.addComponent(separator)
 					.addComponent(exitButton, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				);
 		
 		AutorizationWindow dialog = new AutorizationWindow(dbManager);
 		
 		dialog.setVisible(true);
 		dialog.addWindowListener(new WindowAdapter(){
 			@Override
 			public void windowClosed(WindowEvent e){
 				System.exit(NORMAL);
 			}
 		}
 		);
 
 		GroupLayout mainLayout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(mainLayout);
 		mainLayout.setAutoCreateGaps(true);
 		mainLayout.setAutoCreateContainerGaps(true);
 		
 		mainLayout.setHorizontalGroup(
 				mainLayout.createSequentialGroup()
 					.addComponent(panel)
 					.addComponent(panel_1)
 					.addComponent(panel_2)
 				);
 		
 		mainLayout.setVerticalGroup(
 				mainLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 					.addComponent(panel)
 					.addComponent(panel_1)
 					.addComponent(panel_2)
 				);
 		
 		setTitle("Database manager v.0.6.9");
 		setButtonsSetLevel(BUTTON_SET_LEVEL.MANAGER_LEVEL);
 		pack();
 		setLocationRelativeTo(null);
 	}
 	
 	public static void disableButtons() {
 		createDBBtn.setEnabled(false);
 		saveBtn.setEnabled(false);
 		removeTableBtn.setEnabled(false);
 		createTableBtn.setEnabled(false);
 		removeDB.setEnabled(false);
 		cancelBtn.setEnabled(false);
 	}
 	
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainForm dbBaseMainForm = new MainForm();
 					dbBaseMainForm.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent event) {
 		TreePath path = tree.getPathForLocation(event.getX(), event.getY());
 		if(path != null)
 		{
 			if(path.getPathCount() == 3) // level of table in a tree
 			{
 				selectedTable = path.getLastPathComponent().toString();
 				selectedDB = path.getPathComponent(1).toString();
 				
 				setButtonsSetLevel(BUTTON_SET_LEVEL.TABLE_LEVEL);
 				
 				DataBase db = null;
 				for(int i = 0; i < dbManager.getDataBaseList().size(); i++)
 					if(dbManager.getDataBaseList().get(i).getName().equals(selectedDB))
 					{
 						db = dbManager.getDataBaseList().get(i);
 						break;
 					}
 				if(db == null)
 					return;
 				Table t = null;
 				for(int j = 0; j < db.getTableList().size(); j++)
 					if(db.getTableList().get(j).getTableName().equals(selectedTable))
 					{
 						t = db.getTableList().get(j);
 						break;
 					}
 				if(t == null)
 					return;
 				table.setModel(TableConverter.convertTableToJTable(t).getModel());
 				table.getModel().addTableModelListener(this);
 				table.repaint();
 			} else if(path.getPathCount() == 2) { // level of database in a tree
 				selectedTable = null;
 				selectedDB = path.getLastPathComponent().toString();
 				setButtonsSetLevel(BUTTON_SET_LEVEL.DATABASE_LEVEL);
 			} else { // level of manager in a tree
 				selectedTable = null;
 				selectedDB = null;
 				setButtonsSetLevel(BUTTON_SET_LEVEL.MANAGER_LEVEL);
 			}
 		}
 	}
 
 	public void setButtonsSetLevel(BUTTON_SET_LEVEL setLevel)
 	{
 		if(saveBtn.isEnabled())
 			if(setLevel == BUTTON_SET_LEVEL.MANAGER_LEVEL)
 			{
 				createDBBtn.setEnabled(true);
 				removeDB.setEnabled(false);
 				createTableBtn.setEnabled(false);
 				removeTableBtn.setEnabled(false);
 				addColumnBtn.setEnabled(false);
 				deleteColumnBtn.setEnabled(false);
 				addRowBtn.setEnabled(false);
 				deleteRowBtn.setEnabled(false);
 				joinTablesBtn.setEnabled(false);
 				projectionBtn.setEnabled(false);
 			} else if(setLevel == BUTTON_SET_LEVEL.DATABASE_LEVEL) {
 				createDBBtn.setEnabled(true);
 				removeDB.setEnabled(true);
 				createTableBtn.setEnabled(true);
 				removeTableBtn.setEnabled(false);
 				addColumnBtn.setEnabled(false);
 				deleteColumnBtn.setEnabled(false);
 				addRowBtn.setEnabled(false);
 				deleteRowBtn.setEnabled(false);
 				joinTablesBtn.setEnabled(false);
 				projectionBtn.setEnabled(false);
 			} else if(setLevel == BUTTON_SET_LEVEL.TABLE_LEVEL) {
 				createDBBtn.setEnabled(false);
 				removeDB.setEnabled(false);
 				createTableBtn.setEnabled(true);
 				removeTableBtn.setEnabled(true);
 				addColumnBtn.setEnabled(true);
 				deleteColumnBtn.setEnabled(true);
 				addRowBtn.setEnabled(true);
 				deleteRowBtn.setEnabled(true);
 				joinTablesBtn.setEnabled(true);
 				projectionBtn.setEnabled(true);
 			}
 		getContentPane().repaint();
 	}
 	
 	@Override
 	public void mouseEntered(MouseEvent arg0) 
 	{
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) 
 	{
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) 
 	{
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) 
 	{
 		
 	}
 
 	@Override
 	public void tableChanged(TableModelEvent e) {
 		//TODO refresh table when user puts invalid data
 		if(e.getType() == TableModelEvent.UPDATE) {
 			int row = e.getFirstRow();
 	        int col = e.getColumn();
 	        String columnName = table.getModel().getColumnName(col);
 	        
 	        String data = (String)table.getModel().getValueAt(row, col);
 	        MainFormMng.setNewCellToTable(tree, table, row, col, data);
 	        table.setModel(table.getModel());
 			table.repaint();
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if(event.getActionCommand().equals("SAVE"))
 		{
 			dbManager.save();
 			DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
 			model.setRoot(MainFormMng.createTreeNodes(dbManager));
 		} else if(event.getActionCommand().equals("CANCEL")) {
 			dbManager.cancelAllActions();
 		} else if(event.getActionCommand().equals("CREATE_DB")) {
 			MainFormMng.createDB(dbManager, tree);
 		} else if(event.getActionCommand().equals("DELETE_DB")) {
 			MainFormMng.removeDB(tree, dbManager);
 		} else if(event.getActionCommand().equals("CREATE_TABLE")) {
 			MainFormMng.createTable(dbManager, tree);
 		} else if(event.getActionCommand().equals("DELETE_TABLE")) {
 			MainFormMng.removeTable(tree, dbManager);
 		} else if(event.getActionCommand().equals("CREATE_COLUMN")) {
			//TODO fix bug
			//when we create 2 equal columns and save it 
			//dbManager creates two equal columns 
			//But when we create 1 column, save it
			//then create the same column
			//dbManager doesn't create the last one
 			table.setModel(MainFormMng.addColumnToTable(dbManager, tree).getModel());
 			table.getModel().addTableModelListener(this);
 		} else if(event.getActionCommand().equals("DELETE_COLUMN")) {
 			
 			if(table.getSelectedColumn() != -1) {
 				//	TODO bug fix
 				//	when deleting column model must be updated
 				//	because of that indexOutOfBounds error rises
 				TableColumn col = table.getColumnModel().getColumn(table.getSelectedColumn());
 				MainFormMng.deleteColumn(dbManager, selectedDB, selectedTable, col.getHeaderValue().toString());
 				table.getColumnModel().removeColumn(col);
 				table.setColumnModel(table.getColumnModel());
 			}
 		} else if(event.getActionCommand().equals("ADD_ROW")) {
 			table.setModel(MainFormMng.addRowToTable(dbManager, table, tree).getModel());
 			table.getModel().addTableModelListener(this);
 		} else if(event.getActionCommand().equals("DELETE_ROW")) {
 			table.setModel(MainFormMng.deleteRowFromTable(dbManager, table, tree).getModel());
 			table.getModel().addTableModelListener(this);
 		} else if(event.getActionCommand().equals("JOIN")) {
 			tree = MainFormMng.createJoinDialog(dbManager, selectedDB, selectedTable, tree);
 		} else if(event.getActionCommand().equals("PROJECTION")) {
 			MainFormMng.projectTable(dbManager, tree);
 		} else if(event.getActionCommand().equals("EXIT")) {
			this.dispose();
 		}
 		table.repaint();
 	} 
 }
