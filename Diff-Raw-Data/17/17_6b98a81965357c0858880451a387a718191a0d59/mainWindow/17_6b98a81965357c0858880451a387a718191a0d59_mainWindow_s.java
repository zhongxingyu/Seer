 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Insets;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 //import java.awt.event.WindowListener;
 import javax.swing.DefaultComboBoxModel;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.ActionEvent;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.AbstractTableModel;
 
 import jtables_database_management.Tables_db_manager;
 import combobox_database_management.ComboBox_Products_db_manager;
 import combobox_database_management.ComboBox_Operators_db_manager;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 
 public class mainWindow extends JFrame {
 	
 	private final JPanel inquiriesFiltersPanel = new JPanel();
 	private final JLabel inquiriesFiltersPanelOrderNumberLabel = new JLabel();
 	private final JLabel inquiriesFiltersPanelDateAndTimeLabel = new JLabel();
 	private final JLabel inquiriesFiltersPanelOperatorLabel = new JLabel();
 	private final JLabel inquiriesFiltersPanelOrderTotalPriceLabel = new JLabel();
 	private final JSpinner inquiriesFiltersPanelOrderNumberSpinner = new JSpinner();
 	private final JSpinner inquiriesFiltersPanelOrderTotalPriceSpinner = new JSpinner();
 	private final JComboBox inquiriesFiltersPanelOrderOperatorComboBox = new JComboBox();
 	private final JSpinner inquiriesFiltersPanelOrderDateSpinner = new JSpinner();
 	private final JButton inquiriesFiltersPanelOrderNumberAndButton = new JButton();
 	private final JButton inquiriesFiltersPanelOrderNumberOrButton = new JButton();
 	private final JButton inquiriesFiltersPanelDateAndTimeAndButton = new JButton();
 	private final JButton inquiriesFiltersPanelDateAndTimeOrButton = new JButton();
 	private final JButton inquiriesFiltersPanelOrderOperatorAndButton = new JButton();
 	private final JButton inquiriesFiltersPanelOrderOperatorOrButton = new JButton();
 	private final JButton inquiriesFiltersPanelOrderTotalPriceAndButton = new JButton();
 	private final JButton inquiriesFiltersPanelOrderTotalPriceOrButton = new JButton();
 	private final JTextArea statementsTextArea = new JTextArea();
 	private final JButton inquiriesFiltersPanelOrderFilterButton = new JButton();
 	private final JButton inquiriesFiltersPanelOrderClearFilterStatementsButton = new JButton();
 	private final JScrollPane scrollPane_4 = new JScrollPane();
 	private final JComboBox inquiriesFiltersPanelOrderNumberStatementsComboBox = new JComboBox();
 	private final JComboBox inquiriesFiltersPanelDateAndTimeStatementsComboBox = new JComboBox();
 	private final JComboBox inquiriesFiltersPanelOperatorLabelStatementsComboBox = new JComboBox();
 	private final JComboBox inquiriesFiltersPanelOrderTotalPriceStatementsComboBox = new JComboBox();
 	private final JLabel mainWindowStatusPanelConnectionInfoLabel = new JLabel();
 	private final JMenuItem FileInformationForDbconnection = new JMenuItem();
 	class InquiriesTableTableModel extends AbstractTableModel {
 		
 		private static final long serialVersionUID = 3008L;
 		private Tables_db_manager tdm = null;
 		
 		private final String[] COLUMNS = new String[] {
 			" ", "  ", " ", " "
 		};
 		public String[][] CELLS = new String[][] {
 			{"", "", "", " ", "order_operator_id"}
 			
 		};
 		public int getRowCount() {
 			return CELLS.length;
 		}
 		public int getColumnCount() {
 			return COLUMNS.length;
 		}
 		public String getColumnName(int column) {
 			return COLUMNS[column];
 		}
 		public Object getValueAt(int row, int column) {
 			return CELLS[row].length > column ? CELLS[row][column] : (column + " - " + row);
 		}
 		
 		public boolean isTableEmpty() {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			return tdm.areCellsEmpty();			
 		}
 		
 		public void Init() {
 			
 			tdm = new Tables_db_manager(this, getRowCount(), getColumnCount() + 1, databaseConnectWindow.dbPortal, CELLS);
 			
 			String[] noDataWords = new String[5];
 			noDataWords[0] = "";
 			noDataWords[1] = "";
 			noDataWords[2] = "";
 			noDataWords[3] = " ";
 			noDataWords[4] = "order_operator_id";
 			
 			tdm.setNoDataInTheCellsMessage(noDataWords);						
 		}
 		
 		public void prepareDbView() {
 			
 			databaseConnectWindow.dbPortal.freeQueryNonQueryTemporaryResults();
 			databaseConnectWindow.dbPortal.executeNonQuery("DROP VIEW inquiries");
 			
 			databaseConnectWindow.dbPortal.freeQueryNonQueryTemporaryResults();
 			databaseConnectWindow.dbPortal.executeNonQuery("CREATE VIEW inquiries as " +
 					"SELECT order_id, order_time, order_operator_id, CONCAT_WS(SPACE(1), operator_first_name, operator_last_name) as order_operator_name," + 
 					"(SELECT " +
 					"SUM(order_detail_product_quantity * (SELECT product_price FROM products WHERE product_id=order_detail_product_id)) " +
 					"FROM " +
 					"order_details WHERE order_detail_order_id=order_id) AS order_total_price " +
 					"FROM " +
 					"orders JOIN operators ON operator_id=order_operator_id");
 		}
 		
 		public void populateTableWithDatabaseData() {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			String query = "SELECT order_id, order_time, order_operator_name, order_total_price, order_operator_id FROM inquiries";
 			
 			tdm.setPopulateQuery(query);
 			
 			CELLS = tdm.performPopulate();				
 		}
 		
 		public void populateTableWithDatabaseData(String statement) {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			String query = "SELECT order_id, order_time, order_operator_name, order_total_price, order_operator_id FROM inquiries WHERE " + statement;
 			
 			tdm.setPopulateQuery(query);
 			
 			CELLS = tdm.performPopulate();				
 		}
 	}
 
 	private final JPanel ordersManagementOperationsPanel = new JPanel();
 	private final JLabel ordersManagementOperationsPanelProductLabel = new JLabel();
 	private final JLabel ordersManagementOperationsPanelProductQuantityLabel = new JLabel();
 	private final JComboBox ordersManagementOperationsPanelProductsComboBox = new JComboBox();
 	private final JSpinner ordersManagementOperationsPanelProductQuantitySpinner = new JSpinner();
 	private final JButton ordersManagementOperationsPanelProductAddButton = new JButton();
 	private final JButton ordersManagementOperationsPanelProductEditButton = new JButton();
 	private final JButton ordersManagementOperationsPanelProductDeleteButton = new JButton();
 	private final JLabel ordersManagementOperationsPanelProductOrderTotalPrice = new JLabel();
 	private final JButton ordersManagementOperationsPanelNewOrderButton = new JButton();
 	private final JButton ordersManagementOperationsPanelDeleteOrderButton = new JButton();
 	private ComboBox_Products_db_manager cbpDbManager = null;
 	private ComboBox_Operators_db_manager cboDbManager = null;
 	
 	private final JPanel inquiriesPanel = new JPanel();
 	private final JScrollPane scrollPane_3 = new JScrollPane();
 	private final JTable inquiriesTable = new JTable();
 	class OrderDetailsTableTableModel extends AbstractTableModel {
 		
 		private static final long serialVersionUID = 3007L;
 		private Tables_db_manager tdm = null;
 		
 		private final String[] COLUMNS = new String[] {
 			"", "", ". "
 		};
 		public String[][] CELLS = new String[][] {
 			{"", "", "", "order_detail_id", "order_detail_order_id", "order_detail_product_id" },
 		};
 		public int getRowCount() {
 			return CELLS.length;
 		}
 		public int getColumnCount() {
 			return COLUMNS.length;
 		}
 		public String getColumnName(int column) {
 			return COLUMNS[column];
 		}
 		public Object getValueAt(int row, int column) {
 			return CELLS[row].length > column ? CELLS[row][column] : (column + " - " + row);
 		}
 		
 		public boolean isTableEmpty() {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			return tdm.areCellsEmpty();
 		}
 		
 		public boolean isProductAlreadyInTheTable(int productId) {
 			
 			if (isTableEmpty() == true) {
 				return false;
 			}
 			
 			for (int i = 0; i < CELLS.length; i++) {
 				
 				try {
 					if (Integer.parseInt(CELLS[i][5].toString()) == productId) {
 						return true;
 					}
 				}
 				catch (Exception ex) {					
 				}
 			}
 			
 			return false;
 		}
 		
 		public void Init() {
 			
 			tdm = new Tables_db_manager(this, getRowCount(), getColumnCount() + 3, databaseConnectWindow.dbPortal, CELLS);
 			
 			String[] noDataWords = new String[6];
 			noDataWords[0] = "";
 			noDataWords[1] = "";
 			noDataWords[2] = "";
 			noDataWords[3] = "order_detail_id";
 			noDataWords[4] = "order_detail_order_id";
 			noDataWords[5] = "order_detail_product_id";
 			
 			tdm.setNoDataInTheCellsMessage(noDataWords);			
 		}
 		
 		public void populateTableWithDatabaseData(Integer orderId) {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			tdm.setPopulateQuery("SELECT product_name, order_detail_product_quantity, product_price, order_detail_id, order_detail_order_id, order_detail_product_id " + 
 			"FROM order_details JOIN products ON order_detail_product_id=product_id WHERE order_detail_order_id=" + orderId.intValue());
 			
 			CELLS = tdm.performPopulate();				
 		}
 		
 		public void insertNewRow(Integer orderId, Integer productId, Integer productQuantity) {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			tdm.setInsertQuery("INSERT INTO order_details (order_detail_order_id, order_detail_product_id, order_detail_product_quantity) VALUES(?,?,?)",
 					orderId, productId, productQuantity);
 			tdm.setPopulateQuery("SELECT product_name, order_detail_product_quantity, product_price, order_detail_id, order_detail_order_id, order_detail_product_id " + 
 					"FROM order_details JOIN products ON order_detail_product_id=product_id WHERE order_detail_order_id=" + orderId.intValue() + 
 					" AND order_detail_id=");			
 			
 			CELLS = tdm.performRowInsert();
 			
 			if (tdm.getLastError() != null) {
 				JOptionPane.showMessageDialog(null, tdm.getLastError(), "", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		
 		public void removeSelectedRow(int rowNumber) { 
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			tdm.setDeleteQuery("DELETE FROM order_details WHERE order_detail_id=" + CELLS[rowNumber][3]);
 			CELLS = tdm.performRowDelete(rowNumber);
 			
 			if (tdm.getLastError() != null) {
 				JOptionPane.showMessageDialog(null, tdm.getLastError(), "", JOptionPane.ERROR_MESSAGE);
 			}			
 		}
 		
 		public void updateSelectedRow(int rowNumber, Integer productId, Integer productQuantity) {
 			
 			if (tdm == null) {
 				Init();
 			}
 		
 			tdm.setUpdateQuery("UPDATE order_details SET order_detail_product_id=?, order_detail_product_quantity=? WHERE order_detail_id=?",
 					productId, productQuantity, Integer.parseInt(CELLS[rowNumber][3]));
 			
 			tdm.setPopulateQuery("SELECT product_name, order_detail_product_quantity, product_price, order_detail_id, order_detail_order_id, order_detail_product_id " + 
 					"FROM order_details JOIN products ON order_detail_product_id=product_id WHERE order_detail_order_id=" + CELLS[rowNumber][4] + 
 					" AND order_detail_id=" + CELLS[rowNumber][3]);
 			
 			CELLS = tdm.performRowUpdate(rowNumber);
 			
 			if (tdm.getLastError() != null) {
 				JOptionPane.showMessageDialog(null, tdm.getLastError(), "", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	
 		public void removeAllRowsVisualEffect() {
 			
 			CELLS = new String[][] {
 					{"", "", "", "order_detail_id", "order_detail_order_id", "order_detail_product_id" },
 				};			
 		}
 	}
 
 	class OrdersInfoTableTableModel extends AbstractTableModel {
 	
 		private static final long serialVersionUID = 3006L;
 		private Tables_db_manager tdm = null;
 				
 		private final String[] COLUMNS = new String[] {
 			" ", "  "
 		};
 		private String[][] CELLS = new String[][] {
 			{"", ""},			
 		};
 		public int getRowCount() {
 			return CELLS.length;
 		}
 		public int getColumnCount() {
 			return COLUMNS.length;
 		}
 		public String getColumnName(int column) {
 			return COLUMNS[column];
 		}
 		public Object getValueAt(int row, int column) {
 			return CELLS[row].length > column ? CELLS[row][column] : (column + " - " + row);
 		}
 		
 		public boolean isTableEmpty() {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			return tdm.areCellsEmpty();
 		}
 		
 		public void Init() {
 			
 			tdm = new Tables_db_manager(this, getRowCount(), getColumnCount(), databaseConnectWindow.dbPortal, CELLS);
 			
 			String[] noDataWords = new String[2];
 			noDataWords[0] = "";
 			noDataWords[1] = "";
 			
 			tdm.setNoDataInTheCellsMessage(noDataWords);			
 		}
 		
 		public void populateTableWithDatabaseData() {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			tdm.setPopulateQuery("SELECT order_id, order_time FROM orders WHERE order_operator_id=" + operatorUserLoginWindow.loggedUserId);
 			CELLS = tdm.performPopulate();				
 		}
 		
 		public void insertNewRow() {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			tdm.setInsertQuery("INSERT INTO orders (order_time, order_operator_id) VALUES(?,?)", new java.sql.Timestamp(new java.util.Date().getTime()),
 					new Integer(operatorUserLoginWindow.loggedUserId)
 			);
 			tdm.setPopulateQuery("SELECT order_id, order_time FROM orders WHERE order_id=");
 			
 			CELLS = tdm.performRowInsert();
 			
 			if (tdm.getLastError() != null) {
 				JOptionPane.showMessageDialog(null, tdm.getLastError(), "", JOptionPane.ERROR_MESSAGE);
 			}		
 		}
 		
 		public void removeSelectedRow(int rowNumber) {
 			
 			if (tdm == null) {
 				Init();
 			}
 			tdm.setDeleteQuery("DELETE FROM orders WHERE order_id=" + CELLS[rowNumber][0]);
 			CELLS = tdm.performRowDelete(rowNumber);
 			
 			if (tdm.getLastError() != null) {
 				JOptionPane.showMessageDialog(null, tdm.getLastError(), "", JOptionPane.ERROR_MESSAGE);
 			}			
 		}
 		
 		public void updateSelectedRow(int rowNumber) {
 			
 			if (tdm == null) {
 				Init();
 			}
 			tdm.setUpdateQuery("UPDATE orders SET order_time=? WHERE order_id=?", new java.sql.Timestamp(new java.util.Date().getTime()),
 			Integer.parseInt(CELLS[rowNumber][0]));
 			
 			tdm.setPopulateQuery("SELECT order_id, order_time FROM orders WHERE order_id=" + CELLS[rowNumber][0]);
 			
 			CELLS = tdm.performRowUpdate(rowNumber);
 			
 			if (tdm.getLastError() != null) {
 				JOptionPane.showMessageDialog(null, tdm.getLastError(), "", JOptionPane.ERROR_MESSAGE);
 			}			
 		}
 	}
 
 	private final JPanel productsManagementToolsPanel = new JPanel();
 	private final JLabel productsManagementToolsPanelProductNameLabel = new JLabel();
 	private final JLabel productsManagementToolsPanelProductQuantityLabel = new JLabel();
 	private final JLabel productsManagementToolsPanelProductPriceLabel = new JLabel();
 	private final JTextField productsManagementToolsPanelProductNameTextField = new JTextField();
 	private final JSpinner productsManagementToolsPanelProductPriceSpinner = new JSpinner();
 	private final JSpinner productsManagementToolsPanelProductQuantitySpinner = new JSpinner();
 	private final JButton productsManagementToolsPanelProductAddButton = new JButton();
 	private final JButton productsManagementToolsPanelProductEditButton = new JButton();	
 	private final JButton productsManagementToolsPanelProductRemoveButton = new JButton();
 	
 	private final JMenuItem operationsInquiries = new JMenuItem();
 	private final JPanel ordersManagementPanel = new JPanel();
 	private final JPanel ordersManagementPanelOrdersPanel = new JPanel();
 	private final JScrollPane scrollPane_1 = new JScrollPane();
 	private final JTable ordersInfoTable = new JTable();
 	private final JScrollPane scrollPane_2 = new JScrollPane();
 	private final JTable orderDetailsTable = new JTable();
 	
 	class ProductsTableTableModel extends AbstractTableModel {
 		
 		private static final long serialVersionUID = 3005L;
 		
 		private final String[] COLUMNS = new String[] {
 			"  ", " ", "  "
 		};
 		
 		private String[][] CELLS = new String[][] {
 			{"", "", "","id"},			
 		};
 		
 		private Tables_db_manager tdm = null;
 		
 		public int getRowCount() {
 			return CELLS.length;
 		}
 		public int getColumnCount() {
 			return COLUMNS.length;
 		}
 		public String getColumnName(int column) {
 			return COLUMNS[column];
 		}
 		public Object getValueAt(int row, int column) {
 			return CELLS[row].length > column ? CELLS[row][column] : (column + " - " + row);
 		}
 		
 		public void Init() {
 			
 			tdm = new Tables_db_manager(this, getRowCount(), getColumnCount() + 1, databaseConnectWindow.dbPortal, CELLS);
 			
 			String[] noDataWords = new String[4];
 			noDataWords[0] = "";
 			noDataWords[1] = "";
 			noDataWords[2] = "";
 			noDataWords[3] = "id";
 			
 			tdm.setNoDataInTheCellsMessage(noDataWords);						
 		}
 		
 		public void populateTableWithDatabaseData() {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			tdm.setPopulateQuery("SELECT product_name, product_quantity, product_price, product_id FROM products");
 			
 			CELLS = tdm.performPopulate();				
 		}
 		
 		public void insertNewRow(Object name, Object quantity, Object price) {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			tdm.setInsertQuery("INSERT INTO products (product_name, product_quantity, product_price) VALUES(?,?,?)", name, quantity, price);
 			tdm.setPopulateQuery("SELECT product_name, product_quantity, product_price, product_id FROM products WHERE product_id=");
 			
 			CELLS = tdm.performRowInsert();
 			
 			if (tdm.getLastError() != null) {
 				JOptionPane.showMessageDialog(null, tdm.getLastError(), "", JOptionPane.ERROR_MESSAGE);
 			}		
 			
 		}
 		
 		public void removeSelectedRow(int rowNumber) {
 			
 			if (tdm == null) {
 				Init();
 			}
 			tdm.setDeleteQuery("DELETE FROM products WHERE product_id=" + CELLS[rowNumber][3]);
 			CELLS = tdm.performRowDelete(rowNumber);
 			
 			if (tdm.getLastError() != null) {
 				JOptionPane.showMessageDialog(null, tdm.getLastError(), "", JOptionPane.ERROR_MESSAGE);
 			}			
 		}
 		
 		public void updateSelectedRow(int rowNumber, Object name, Object quantity, Object price) {
 			
 			if (tdm == null) {
 				Init();
 			}
 			
 			// safe quantify modification:
 			
 			Integer quantityDifference = ((Integer)quantity).intValue() - (new Integer(CELLS[rowNumber][1]));
 			
 			tdm.setUpdateQuery("UPDATE products SET product_name=?, product_quantity=product_quantity+?, product_price=? WHERE product_id=?",
 					name, quantityDifference, price, Integer.parseInt(CELLS[rowNumber][3]));
 			tdm.setPopulateQuery("SELECT product_name, product_quantity, product_price, product_id FROM products WHERE product_id=" + 
 					CELLS[rowNumber][3]);
 			
 			CELLS = tdm.performRowUpdate(rowNumber);
 			
 			if (Integer.parseInt(CELLS[rowNumber][1]) < 0) { // someone has already reduced the maximum available quantity of that product
 				
 				tdm.setUpdateQuery("UPDATE products SET product_name=?, product_quantity=product_quantity+?, product_price=? WHERE product_id=?",
 						name, quantityDifference * (-1), price, Integer.parseInt(CELLS[rowNumber][3]));
 				tdm.setPopulateQuery("SELECT product_name, product_quantity, product_price, product_id FROM products WHERE product_id=" + 
 						CELLS[rowNumber][3]);
 				
 				CELLS = tdm.performRowUpdate(rowNumber);
 				
 				JOptionPane.showMessageDialog(null, "       !\n !", 
 						"    ", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			
 			if (tdm.getLastError() != null) {
 				JOptionPane.showMessageDialog(null, tdm.getLastError(), "", JOptionPane.ERROR_MESSAGE);
 			}			
 		}
 	}
 
 	private static final long serialVersionUID = 3001L;
 
 	private final JMenuBar mainWindowMenu = new JMenuBar();
 	private final JMenu File = new JMenu();
 	private final JMenuItem FileConnectToDb = new JMenuItem();
 	private final JMenu Operations = new JMenu();
 	private final JMenu Help = new JMenu();
 	private final JMenuItem HelpAbout = new JMenuItem();
 	private final JMenuItem FileExit = new JMenuItem();
 	private static mainWindow mainWindowPointer;
 	
 	private final JMenuItem FileDisconnectFromDb = new JMenuItem();
 	private final JPanel mainWindowStatusPanel = new JPanel();
 	private final JLabel mainWindowStatusPanelLoggedUserLabel = new JLabel();
 	private final JButton mainWindowStatusPanelLoggedUserLogin = new JButton();
 	private final JButton mainWindowStatusPanelLoggedUserSettings = new JButton();
 
 	private final JButton mainWindowStatusPanelLoggedUserLogout = new JButton();
 	
 	private final JMenuItem operationsProductsManagement = new JMenuItem();
 	private final JMenuItem operationsOrdersManagement = new JMenuItem();
 	
 	private final JPanel productsManagementPanel = new JPanel();
 	private final JScrollPane scrollPane = new JScrollPane();
 	private final JTable productsTable = new JTable();
 	
 	
 	/**
 	 * Launch the application
 	 * @param args
 	 */
 	public static void main(String args[]) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					mainWindow frame = new mainWindow();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame
 	 */
 	public mainWindow() {
 		super();
 		setBounds(100, 100, 840, 438);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainWindowPointer = this;
 		try {
 			jbInit();
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		//		
 	}
 	private void jbInit() throws Exception {
 		addWindowListener(new ThisWindowListener());
 		getContentPane().setLayout(new BorderLayout());
 		setTitle("eShop 1.01 (C) 2013  ");
 		setName("mainWindow");
 		
 		setJMenuBar(mainWindowMenu);
 		
 		mainWindowMenu.add(File);
 		File.setMnemonic(KeyEvent.VK_B);
 		File.setText("");
 		File.setDisplayedMnemonicIndex(0);
 		
 		File.add(FileConnectToDb);
 		FileConnectToDb.setAccelerator(KeyStroke.getKeyStroke(''));
 		FileConnectToDb.addActionListener(new FileConnectToDbActionListener());
 		FileConnectToDb.setText("   ");
 		FileConnectToDb.setDisplayedMnemonicIndex(0);
 		
 		File.add(FileDisconnectFromDb);
 		FileDisconnectFromDb.setAccelerator(KeyStroke.getKeyStroke(''));
 		FileDisconnectFromDb.addActionListener(new FileDisconnectFromDbActionListener());
 		FileDisconnectFromDb.setText("   ");
 		FileDisconnectFromDb.setDisplayedMnemonicIndex(0);
 		FileDisconnectFromDb.setEnabled(false);
 		
 		File.add(FileInformationForDbconnection);
 		FileInformationForDbconnection.addActionListener(new FileInformationForDbconnectionActionListener());
 		FileInformationForDbconnection.setAccelerator(KeyStroke.getKeyStroke(''));
 		FileInformationForDbconnection.setText("  ");
 		FileInformationForDbconnection.setDisplayedMnemonicIndex(2);
 
 		File.addSeparator();
 		
 		File.add(FileExit);
 		FileExit.setAccelerator(KeyStroke.getKeyStroke(''));
 		FileExit.addActionListener(new FileExitActionListener());
 		FileExit.setText("");
 		FileExit.setDisplayedMnemonicIndex(2);
 		
 		mainWindowMenu.add(Operations);
 		Operations.setMnemonic(KeyEvent.VK_F);
 		Operations.setText("");
 		Operations.setDisplayedMnemonicIndex(0);
 		Operations.setEnabled(false);
 		
 		Operations.add(operationsInquiries);
 		operationsInquiries.setAccelerator(KeyStroke.getKeyStroke(''));
 		operationsInquiries.addActionListener(new OperationsInquiriesActionListener());
 		operationsInquiries.setText("");
 		operationsInquiries.setDisplayedMnemonicIndex(0);
 
 		Operations.addSeparator();
 		
 		Operations.add(operationsProductsManagement);
 		operationsProductsManagement.setAccelerator(KeyStroke.getKeyStroke(''));
 		operationsProductsManagement.addActionListener(new OperationsProductsManagementActionListener());
 		operationsProductsManagement.setText("  ");
 		operationsProductsManagement.setDisplayedMnemonicIndex(0);
 		
 		Operations.add(operationsOrdersManagement);
 		operationsOrdersManagement.setAccelerator(KeyStroke.getKeyStroke(''));
 		operationsOrdersManagement.addActionListener(new OperationsOrdersManagementActionListener());
 		operationsOrdersManagement.setText("  ");
 		operationsOrdersManagement.setDisplayedMnemonicIndex(1);
 		
 		mainWindowMenu.add(Help);
 		Help.setMnemonic(KeyEvent.VK_M);
 		Help.setText("");
 		Help.setDisplayedMnemonicIndex(0);
 		
 		Help.add(HelpAbout);
 		HelpAbout.setAccelerator(KeyStroke.getKeyStroke(''));
 		HelpAbout.addActionListener(new HelpAboutActionListener());
 		HelpAbout.setText("");
 		HelpAbout.setDisplayedMnemonicIndex(0);
 		
 		getContentPane().add(mainWindowStatusPanel, BorderLayout.SOUTH);
 		mainWindowStatusPanel.setLayout(new FormLayout(
 			new ColumnSpec[] {
 				ColumnSpec.decode("149dlu"),
 				ColumnSpec.decode("72px"),
 				ColumnSpec.decode("8px"),
 				FormFactory.DEFAULT_COLSPEC,
 				ColumnSpec.decode("8px"),
 				FormFactory.DEFAULT_COLSPEC,
 				ColumnSpec.decode("16px"),
 				ColumnSpec.decode("right:123px:grow(1.0)")},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("16px")}));
 		mainWindowStatusPanel.setSize(666, 25);
 		mainWindowStatusPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
 		mainWindowStatusPanel.setMinimumSize(new Dimension(666, 20));
 		
 		mainWindowStatusPanelLoggedUserLabel.setText(":");
 		mainWindowStatusPanel.add(mainWindowStatusPanelLoggedUserLabel, new CellConstraints("1, 1, 1, 2, fill, fill"));
 		
 		mainWindowStatusPanel.add(mainWindowStatusPanelLoggedUserLogin, new CellConstraints(2, 1, 1, 2));
 		mainWindowStatusPanelLoggedUserLogin.setMnemonic(KeyEvent.VK_L);
 		mainWindowStatusPanelLoggedUserLogin.addActionListener(new MainWindowStatusPanelLoggedUserLoginActionListener());
 		mainWindowStatusPanelLoggedUserLogin.setText("");
 		mainWindowStatusPanelLoggedUserLogin.setDisplayedMnemonicIndex(0);
 		
 		mainWindowStatusPanel.add(mainWindowStatusPanelLoggedUserSettings, new CellConstraints(4, 1, 1, 2, CellConstraints.CENTER, CellConstraints.DEFAULT));
 		mainWindowStatusPanelLoggedUserSettings.setMnemonic(KeyEvent.VK_K);
 		mainWindowStatusPanelLoggedUserSettings.addActionListener(new MainWindowStatusPanelLoggedUserSettingsActionListener());
 		mainWindowStatusPanelLoggedUserSettings.setMargin(new Insets(2, 14, 2, 14));
 		mainWindowStatusPanelLoggedUserSettings.setText("");
 		mainWindowStatusPanelLoggedUserSettings.setDisplayedMnemonicIndex(0);
 		mainWindowStatusPanelLoggedUserSettings.setEnabled(false);
 		
 		mainWindowStatusPanel.add(mainWindowStatusPanelLoggedUserLogout, new CellConstraints(6, 1, 1, 2));
 		mainWindowStatusPanelLoggedUserLogout.setMnemonic(KeyEvent.VK_R);
 		mainWindowStatusPanelLoggedUserLogout.addActionListener(new MainWindowStatusPanelLoggedUserLogoutActionListener());
 		mainWindowStatusPanelLoggedUserLogout.setText("");
 		mainWindowStatusPanelLoggedUserLogout.setDisplayedMnemonicIndex(0);
 		mainWindowStatusPanelLoggedUserLogout.setEnabled(false);
 		
 		mainWindowStatusPanel.add(mainWindowStatusPanelConnectionInfoLabel, new CellConstraints(8, 1, 1, 2, CellConstraints.RIGHT, CellConstraints.FILL));
 		mainWindowStatusPanelConnectionInfoLabel.setForeground(Color.GRAY);
 		mainWindowStatusPanelConnectionInfoLabel.setText(":");
 		mainWindowStatusPanelSetEnabled(false);
 		
 		getContentPane().add(productsManagementPanel);
 		productsManagementPanel.setLayout(new BorderLayout());		
 		productsManagementPanel.add(scrollPane, BorderLayout.CENTER);
 		scrollPane.setPreferredSize(new Dimension(0, 0));
 		scrollPane.setBorder(new TitledBorder(new TitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null), "   :", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
 		productsManagementPanel.setVisible(false);
 		
 		scrollPane.setViewportView(productsTable);
 		productsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		productsTable.addMouseListener(new ProductsTableMouseListener());
 		productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		productsTable.setShowGrid(true);
 		productsTable.setModel(new ProductsTableTableModel());
 		
 		productsManagementPanel.add(productsManagementToolsPanel, BorderLayout.EAST);
 		productsManagementToolsPanel.setLayout(new FormLayout(
 			new ColumnSpec[] {
 				FormFactory.UNRELATED_GAP_COLSPEC,
 				ColumnSpec.decode("92px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("92px"),
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				ColumnSpec.decode("52px"),
 				ColumnSpec.decode("92px")},
 			new RowSpec[] {
 				RowSpec.decode("12dlu"),
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				RowSpec.decode("15px"),
 				FormFactory.DEFAULT_ROWSPEC,
 				RowSpec.decode("15px"),
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC}));
 		productsManagementToolsPanel.setBorder(new TitledBorder(new TitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null), "/  :", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
 		
 		productsManagementToolsPanel.add(productsManagementToolsPanelProductNameLabel, new CellConstraints("2, 2, center, fill"));
 		productsManagementToolsPanelProductNameLabel.setText("  :");
 		
 		productsManagementToolsPanel.add(productsManagementToolsPanelProductPriceLabel, new CellConstraints("2, 3, 3, 1, fill, fill"));
 		productsManagementToolsPanelProductPriceLabel.setText(" .");
 		
 		productsManagementToolsPanel.add(productsManagementToolsPanelProductQuantityLabel, new CellConstraints("2, 4, 3, 1, fill, fill"));
 		productsManagementToolsPanelProductQuantityLabel.setText(" :");
 		
 		productsManagementToolsPanel.add(productsManagementToolsPanelProductNameTextField, new CellConstraints(4, 2, 4, 1, CellConstraints.FILL, CellConstraints.FILL));
 		
 		productsManagementToolsPanel.add(productsManagementToolsPanelProductPriceSpinner, new CellConstraints(6, 3, 2, 1, CellConstraints.FILL, CellConstraints.FILL));
 		 
 		SpinnerNumberModel productsManagementToolsPanelProductPriceSpinnerNumberModel = new SpinnerNumberModel(0.00, 0.00, 1000000.00, 0.01);
 		productsManagementToolsPanelProductPriceSpinner.setModel(productsManagementToolsPanelProductPriceSpinnerNumberModel);		
 		
 		productsManagementToolsPanel.add(productsManagementToolsPanelProductQuantitySpinner, new CellConstraints(6, 4, 2, 1));
 		SpinnerNumberModel productsManagementToolsPanelProductQuantitySpinnerNumberModel = new SpinnerNumberModel(0, 0, 1000000, 1);
 		productsManagementToolsPanelProductQuantitySpinner.setModel(productsManagementToolsPanelProductQuantitySpinnerNumberModel);
 		
 		productsManagementToolsPanel.add(productsManagementToolsPanelProductAddButton, new CellConstraints(2, 6, 3, 1, CellConstraints.FILL, CellConstraints.FILL));
 		productsManagementToolsPanelProductAddButton.addActionListener(new ProductsManagementToolsPanelProductAddButtonActionListener());
 		productsManagementToolsPanelProductAddButton.setText("  ");
 		
 		productsManagementToolsPanel.add(productsManagementToolsPanelProductEditButton, new CellConstraints(6, 6, 2, 1, CellConstraints.FILL, CellConstraints.FILL));
 		productsManagementToolsPanelProductEditButton.addActionListener(new ProductsManagementToolsPanelProductEditButtonActionListener());
 		productsManagementToolsPanelProductEditButton.setText("");
 		
 		productsManagementToolsPanel.add(productsManagementToolsPanelProductRemoveButton, new CellConstraints(4, 8, 3, 1));
 		productsManagementToolsPanelProductRemoveButton.addActionListener(new ProductsManagementToolsPanelProductRemoveButtonActionListener());
 		productsManagementToolsPanelProductRemoveButton.setText(" ");
 		
 		getContentPane().add(ordersManagementPanel);
 		ordersManagementPanel.setBorder(new TitledBorder(new TitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null), "  :", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
 		ordersManagementPanel.setLayout(new BorderLayout());
 		ordersManagementPanel.setVisible(false);
 		
 		ordersManagementPanel.add(ordersManagementPanelOrdersPanel, BorderLayout.CENTER);
 		ordersManagementPanelOrdersPanel.setLayout(new BorderLayout());
 		ordersManagementPanelOrdersPanel.setBorder(new TitledBorder(new TitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null), "    :", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
 		
 		ordersManagementPanelOrdersPanel.add(scrollPane_1, BorderLayout.WEST);
 		scrollPane_1.setPreferredSize(new Dimension(200, 0));
 		
 		scrollPane_1.setViewportView(ordersInfoTable);
 		ordersInfoTable.addMouseListener(new OrdersInfoTableMouseListener());
 		ordersInfoTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		ordersInfoTable.setShowGrid(true);
 		ordersInfoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		ordersInfoTable.setModel(new OrdersInfoTableTableModel());
 		
 		ordersManagementPanelOrdersPanel.add(scrollPane_2, BorderLayout.CENTER);
 		scrollPane_2.setPreferredSize(new Dimension(300, 0));
 		
 		scrollPane_2.setViewportView(orderDetailsTable);
 		orderDetailsTable.addMouseListener(new OrderDetailsTableMouseListener());
 		orderDetailsTable.setShowGrid(true);
 		orderDetailsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		orderDetailsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		orderDetailsTable.setModel(new OrderDetailsTableTableModel());
 		
 		ordersManagementPanel.add(ordersManagementOperationsPanel, BorderLayout.EAST);
 		ordersManagementOperationsPanel.setBorder(new TitledBorder(null, " :", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
 		ordersManagementOperationsPanel.setLayout(new FormLayout(
 			new ColumnSpec[] {
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC},
 			new RowSpec[] {
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("20px"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC}));
 		
 		ordersManagementOperationsPanel.add(ordersManagementOperationsPanelProductLabel, new CellConstraints());
 		ordersManagementOperationsPanelProductLabel.setText(":");
 		
 		ordersManagementOperationsPanel.add(ordersManagementOperationsPanelProductQuantityLabel, new CellConstraints(1, 3));
 		ordersManagementOperationsPanelProductQuantityLabel.setText(":");
 		
 		ordersManagementOperationsPanel.add(ordersManagementOperationsPanelProductsComboBox, new CellConstraints(3, 1));
 		ordersManagementOperationsPanelProductsComboBox.addItemListener(new OrdersManagementOperationsPanelProductsComboBoxItemListener());
 		
 		ordersManagementOperationsPanel.add(ordersManagementOperationsPanelProductQuantitySpinner, new CellConstraints(3, 3));
 		SpinnerNumberModel ordersManagementOperationsPanelProuctQuantitySpinnerNumberModel = new SpinnerNumberModel(0, 0, 1000000, 1);
 		ordersManagementOperationsPanelProductQuantitySpinner.setModel(ordersManagementOperationsPanelProuctQuantitySpinnerNumberModel);
 		
 		ordersManagementOperationsPanel.add(ordersManagementOperationsPanelProductAddButton, new CellConstraints(3, 5));
 		ordersManagementOperationsPanelProductAddButton.addActionListener(new OrdersManagementOperationsPanelProductAddButtonActionListener());
 		ordersManagementOperationsPanelProductAddButton.setText("");
 		
 		ordersManagementOperationsPanel.add(ordersManagementOperationsPanelProductEditButton, new CellConstraints(3, 7));
 		ordersManagementOperationsPanelProductEditButton.addActionListener(new OrdersManagementOperationsPanelProductEditButtonActionListener());
 		ordersManagementOperationsPanelProductEditButton.setText("");
 		
 		ordersManagementOperationsPanel.add(ordersManagementOperationsPanelProductDeleteButton, new CellConstraints(3, 9));
 		ordersManagementOperationsPanelProductDeleteButton.addActionListener(new OrdersManagementOperationsPanelProductDeleteButtonActionListener());
 		ordersManagementOperationsPanelProductDeleteButton.setText("");
 		
 		ordersManagementOperationsPanel.add(ordersManagementOperationsPanelProductOrderTotalPrice, new CellConstraints(1, 13, 3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
 		ordersManagementOperationsPanelProductOrderTotalPrice.setText(" :");
 		
 		ordersManagementOperationsPanel.add(ordersManagementOperationsPanelNewOrderButton, new CellConstraints(1, 17, 3, 1));
 		ordersManagementOperationsPanelNewOrderButton.addActionListener(new OrdersManagementOperationsPanelNewOrderButtonActionListener());
 		ordersManagementOperationsPanelNewOrderButton.setText(" ");
 		
 		ordersManagementOperationsPanel.add(ordersManagementOperationsPanelDeleteOrderButton, new CellConstraints(1, 19, 3, 1));
 		ordersManagementOperationsPanelDeleteOrderButton.addActionListener(new OrdersManagementOperationsPanelDeleteOrderButtonActionListener());
 		ordersManagementOperationsPanelDeleteOrderButton.setText(" ");
 		
 		getContentPane().add(inquiriesPanel);
 		inquiriesPanel.setBorder(new TitledBorder(new TitledBorder(null, ":", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
 		inquiriesPanel.setLayout(new BorderLayout());
 		inquiriesPanel.setVisible(false);
 		
 		inquiriesPanel.add(scrollPane_3);
 		
 		scrollPane_3.setViewportView(inquiriesTable);
 		inquiriesTable.addMouseListener(new InquiriesTableMouseListener());
 		inquiriesTable.setModel(new InquiriesTableTableModel());
 		
 		inquiriesPanel.add(inquiriesFiltersPanel, BorderLayout.EAST);
 		inquiriesFiltersPanel.setBorder(new TitledBorder(null, ":", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
 		inquiriesFiltersPanel.setLayout(new FormLayout(
 			new ColumnSpec[] {
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC},
 			new RowSpec[] {
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("90px"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC}));
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderNumberLabel, new CellConstraints());
 		inquiriesFiltersPanelOrderNumberLabel.setText(" :");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelDateAndTimeLabel, new CellConstraints(1, 3));
 		inquiriesFiltersPanelDateAndTimeLabel.setText("  :");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOperatorLabel, new CellConstraints(1, 5));
 		inquiriesFiltersPanelOperatorLabel.setText(" :");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderTotalPriceLabel, new CellConstraints(1, 7));
 		inquiriesFiltersPanelOrderTotalPriceLabel.setText(" :");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderNumberSpinner, new CellConstraints(5, 1));
 		final SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel();
 		spinnerNumberModel.setValue(new Integer(1));
 		spinnerNumberModel.setStepSize(new Integer(1));
 		spinnerNumberModel.setMinimum(new Integer(0));
 		inquiriesFiltersPanelOrderNumberSpinner.setModel(spinnerNumberModel);
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderTotalPriceSpinner, new CellConstraints(5, 7));
 		SpinnerNumberModel inquiriesFiltersPanelOrderTotalPriceSpinnerNumberModel = new SpinnerNumberModel(0.00, 0.00, 1000000.00, 0.01);
 		inquiriesFiltersPanelOrderTotalPriceSpinner.setModel(inquiriesFiltersPanelOrderTotalPriceSpinnerNumberModel);		
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderOperatorComboBox, new CellConstraints(5, 5));
		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderDateSpinner, new CellConstraints(5, 3));
 		SpinnerDateModel sdm = new SpinnerDateModel();
 		inquiriesFiltersPanelOrderDateSpinner.setModel(sdm);
 		((JSpinner.DateEditor)inquiriesFiltersPanelOrderDateSpinner.getEditor()).getFormat().applyPattern("yyyy-MM-dd HH:mm:ss");
 		sdm.setValue(GregorianCalendar.getInstance().getTime());
 			
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderNumberAndButton, new CellConstraints(7, 1));
 		inquiriesFiltersPanelOrderNumberAndButton.addActionListener(new InquiriesFiltersPanelOrderNumberAndButtonActionListener());
 		inquiriesFiltersPanelOrderNumberAndButton.setText("");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderNumberOrButton, new CellConstraints(9, 1));
 		inquiriesFiltersPanelOrderNumberOrButton.addActionListener(new InquiriesFiltersPanelOrderNumberOrButtonActionListener());
 		inquiriesFiltersPanelOrderNumberOrButton.setText("");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelDateAndTimeAndButton, new CellConstraints(7, 3));
 		inquiriesFiltersPanelDateAndTimeAndButton.addActionListener(new InquiriesFiltersPanelDateAndTimeAndButtonActionListener());
 		inquiriesFiltersPanelDateAndTimeAndButton.setText("");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelDateAndTimeOrButton, new CellConstraints(9, 3));
 		inquiriesFiltersPanelDateAndTimeOrButton.addActionListener(new InquiriesFiltersPanelDateAndTimeOrButtonActionListener());
 		inquiriesFiltersPanelDateAndTimeOrButton.setText("");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderOperatorAndButton, new CellConstraints(7, 5));
 		inquiriesFiltersPanelOrderOperatorAndButton.addActionListener(new InquiriesFiltersPanelOrderOperatorAndButtonActionListener());
 		inquiriesFiltersPanelOrderOperatorAndButton.setText("");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderOperatorOrButton, new CellConstraints(9, 5));
 		inquiriesFiltersPanelOrderOperatorOrButton.addActionListener(new InquiriesFiltersPanelOrderOperatorOrButtonActionListener());
 		inquiriesFiltersPanelOrderOperatorOrButton.setText("");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderTotalPriceAndButton, new CellConstraints(7, 7));
 		inquiriesFiltersPanelOrderTotalPriceAndButton.addActionListener(new InquiriesFiltersPanelOrderTotalPriceAndButtonActionListener());
 		inquiriesFiltersPanelOrderTotalPriceAndButton.setText("");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderTotalPriceOrButton, new CellConstraints(9, 7));
 		inquiriesFiltersPanelOrderTotalPriceOrButton.addActionListener(new InquiriesFiltersPanelOrderTotalPriceOrButtonActionListener());
 		inquiriesFiltersPanelOrderTotalPriceOrButton.setText("");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderFilterButton, new CellConstraints(1, 11, 3, 1));
 		inquiriesFiltersPanelOrderFilterButton.addActionListener(new InquiriesFiltersPanelOrderFilterButtonActionListener());
 		inquiriesFiltersPanelOrderFilterButton.setText("");
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderClearFilterStatementsButton, new CellConstraints(5, 11, 5, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
 		inquiriesFiltersPanelOrderClearFilterStatementsButton.addActionListener(new InquiriesFiltersPanelOrderClearFilterStatementsButtonActionListener());
 		inquiriesFiltersPanelOrderClearFilterStatementsButton.setText(" ");
 		
 		inquiriesFiltersPanel.add(scrollPane_4, new CellConstraints(1, 9, 9, 1));
 		scrollPane_4.setAutoscrolls(true);
 		
 		scrollPane_4.setViewportView(statementsTextArea);
 		statementsTextArea.setEditable(false);
 		statementsTextArea.setRows(5);
 		statementsTextArea.setLineWrap(true);				
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderNumberStatementsComboBox, new CellConstraints(3, 1, CellConstraints.CENTER, CellConstraints.DEFAULT));
 		inquiriesFiltersPanelOrderNumberStatementsComboBox.setModel(new DefaultComboBoxModel(new String[] {">", "<", "=", "<>", ">=", "<="}));
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelDateAndTimeStatementsComboBox, new CellConstraints(3, 3));
 		inquiriesFiltersPanelDateAndTimeStatementsComboBox.setModel(new DefaultComboBoxModel(new String[] {">", "<", "=", "<>", ">=", "<="}));
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOperatorLabelStatementsComboBox, new CellConstraints(3, 5));
 		inquiriesFiltersPanelOperatorLabelStatementsComboBox.setModel(new DefaultComboBoxModel(new String[] {">", "<", "=", "<>", ">=", "<="}));
 		
 		inquiriesFiltersPanel.add(inquiriesFiltersPanelOrderTotalPriceStatementsComboBox, new CellConstraints(3, 7));
 		inquiriesFiltersPanelOrderTotalPriceStatementsComboBox.setModel(new DefaultComboBoxModel(new String[] {">", "<", "=", "<>", ">=", "<="}));
 	}
 	
 	protected void mainWindowStatusPanelSetEnabled(boolean enable) {
 		
 		Component[] comps = mainWindowStatusPanel.getComponents();
 		
 		for (int i = 0; i < comps.length; i++) {
 		
 			comps[i].setEnabled(enable);	
 		}
 		
 		mainWindowStatusPanel.setEnabled(enable);
 	}
 	
 	private class ThisWindowListener extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			this_windowClosing(e);
 		}
 	}
 	private class FileExitActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			fileExit_actionPerformed(e);
 		}
 	}
 	private class FileConnectToDbActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			fileConnectToDb_actionPerformed(e);
 		}
 	}
 	private class FileDisconnectFromDbActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			fileDisconnectFromDb_actionPerformed(e);
 		}
 	}
 	private class MainWindowStatusPanelLoggedUserLoginActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			mainWindowStatusPanelLoggedUserLogin_actionPerformed(e);
 		}
 	}
 	private class MainWindowStatusPanelLoggedUserSettingsActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			mainWindowStatusPanelLoggedUserSettings_actionPerformed(e);
 		}
 	}
 	private class MainWindowStatusPanelLoggedUserLogoutActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			mainWindowStatusPanelLoggedUserLogout_actionPerformed(e);
 		}
 	}
 	private class HelpAboutActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			helpAbout_actionPerformed(e);
 		}
 	}
 	private class OperationsProductsManagementActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			operationsProductsManagement_actionPerformed(e);
 		}
 	}
 	private class OperationsOrdersManagementActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			operationsOrdersManagement_actionPerformed(e);
 		}
 	}
 	private class ProductsTableMouseListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			productsTable_mouseClicked(e);
 		}
 	}
 	private class ProductsManagementToolsPanelProductAddButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			productsManagementToolsPanelProductAddButton_actionPerformed(e);
 		}
 	}
 	private class ProductsManagementToolsPanelProductEditButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			productsManagementToolsPanelProductEditButton_actionPerformed(e);
 		}
 	}
 	private class ProductsManagementToolsPanelProductRemoveButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			productsManagementToolsPanelProductRemoveButton_actionPerformed(e);
 		}
 	}
 	private class OperationsInquiriesActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			operationsInquiries_actionPerformed(e);
 		}
 	}
 	private class OrdersInfoTableMouseListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			ordersInfoTable_mouseClicked(e);
 		}
 	}
 	private class OrderDetailsTableMouseListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			orderDetailsTable_mouseClicked(e);
 		}
 	}
 	private class OrdersManagementOperationsPanelNewOrderButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			ordersManagementOperationsPanelNewOrderButton_actionPerformed(e);
 		}
 	}
 	private class OrdersManagementOperationsPanelDeleteOrderButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			ordersManagementOperationsPanelDeleteOrderButton_actionPerformed(e);
 		}
 	}
 	private class OrdersManagementOperationsPanelProductsComboBoxItemListener implements ItemListener {
 		public void itemStateChanged(ItemEvent e) {
 			ordersManagementOperationsPanelProductsComboBox_itemStateChanged(e);
 		}
 	}
 	private class OrdersManagementOperationsPanelProductAddButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			ordersManagementOperationsPanelProductAddButton_actionPerformed(e);
 		}
 	}
 	private class OrdersManagementOperationsPanelProductEditButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			ordersManagementOperationsPanelProductEditButton_actionPerformed(e);
 		}
 	}
 	private class OrdersManagementOperationsPanelProductDeleteButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			ordersManagementOperationsPanelProductDeleteButton_actionPerformed(e);
 		}
 	}
 	private class InquiriesFiltersPanelOrderNumberAndButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			inquiriesFiltersPanelOrderNumberAndButton_actionPerformed(e);
 		}
 	}
 	private class InquiriesFiltersPanelOrderNumberOrButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			inquiriesFiltersPanelOrderNumberOrButton_actionPerformed(e);
 		}
 	}
 	private class InquiriesFiltersPanelDateAndTimeAndButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			inquiriesFiltersPanelDateAndTimeAndButton_actionPerformed(e);
 		}
 	}
 	private class InquiriesFiltersPanelDateAndTimeOrButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			inquiriesFiltersPanelDateAndTimeOrButton_actionPerformed(e);
 		}
 	}
 	private class InquiriesFiltersPanelOrderOperatorAndButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			inquiriesFiltersPanelOrderOperatorAndButton_actionPerformed(e);
 		}
 	}
 	private class InquiriesFiltersPanelOrderOperatorOrButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			inquiriesFiltersPanelOrderOperatorOrButton_actionPerformed(e);
 		}
 	}
 	private class InquiriesFiltersPanelOrderTotalPriceAndButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			inquiriesFiltersPanelOrderTotalPriceAndButton_actionPerformed(e);
 		}
 	}
 	private class InquiriesFiltersPanelOrderTotalPriceOrButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			inquiriesFiltersPanelOrderTotalPriceOrButton_actionPerformed(e);
 		}
 	}
 	private class InquiriesFiltersPanelOrderClearFilterStatementsButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			inquiriesFiltersPanelOrderClearFilterStatementsButton_actionPerformed(e);
 		}
 	}
 	private class InquiriesFiltersPanelOrderFilterButtonActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			inquiriesFiltersPanelOrderFilterButton_actionPerformed(e);
 		}
 	}
 	private class InquiriesTableMouseListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			inquiriesTable_mouseClicked(e);
 		}
 	}
 	private class FileInformationForDbconnectionActionListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			fileInformationForDbconnection_actionPerformed(e);
 		}
 	}
 
 	protected void this_windowClosing(WindowEvent e) {
 		
 		if (databaseConnectWindow.dbPortal != null) {
 			
 			if (databaseConnectWindow.dbPortal.isConnected()) {
 				
 				databaseConnectWindow.dbPortal.finalize();
 			}
 		}
 		this.dispose();
 		//System.exit(0);
 	}
 	
 	protected void fileExit_actionPerformed(ActionEvent e) {
 
 		this.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
 	}
 	
 	////////////////////////////////////////////////////////////////////////
 	
 	protected void fileConnectToDb_actionPerformed(ActionEvent e) {
 		
 		//non modal new frame
 		//databaseConnectWindow dcw = new databaseConnectWindow(this, true);
 		//dcw.setVisible(true);
 		//dcw.addWindowListener(databaseConnectWindowClosing);
 		
 		//new modal dialog:
 		
 		new databaseConnectWindow(this, true);
 		
 		if (databaseConnectWindow.dbPortal != null) { 
 			
 			if (databaseConnectWindow.dbPortal.isConnected()) {
 				
 				mainWindowPointer.mainWindowStatusPanelSetEnabled(true);
 				mainWindowPointer.mainWindowStatusPanelLoggedUserSettings.setEnabled(false);
 				mainWindowPointer.mainWindowStatusPanelLoggedUserLogout.setEnabled(false);
 				mainWindowPointer.FileConnectToDb.setEnabled(false);
 				mainWindowPointer.FileDisconnectFromDb.setEnabled(true);
 				
 				mainWindowPointer.mainWindowStatusPanelConnectionInfoLabel.setText(": " + 
 						databaseConnectWindow.dbPortal.getMySqlActiveDatabase() + "@" + 
 						databaseConnectWindow.dbPortal.getMySqlServerAddress());				
 				
 				JOptionPane.showMessageDialog(mainWindowPointer, "    !\n" + 
 						"     .", " ", JOptionPane.INFORMATION_MESSAGE);
 			}
 			else {    				
 				mainWindowPointer.mainWindowStatusPanelConnectionInfoLabel.setText(": ");
 				mainWindowPointer.mainWindowStatusPanelSetEnabled(false);
 			}
 		} 
 	}
 	
 	/*private static WindowListener databaseConnectWindowClosing = new WindowAdapter() {
 		
         public void windowClosing(WindowEvent e) {
             
         	e.getWindow().dispose(); 
         	
         	if (databaseConnectWindow.dbPortal != null) {
     			
     			if (databaseConnectWindow.dbPortal.isConnected()) {
     				
     				mainWindowPointer.mainWindowStatusPanelSetEnabled(true);
     				mainWindowPointer.FileConnectToDb.setEnabled(false);
     				mainWindowPointer.FileDisconnectFromDb.setEnabled(true);
     			}
     			else {    				
     				mainWindowPointer.mainWindowStatusPanelSetEnabled(false);
     			}
     		}            
         }
     };*/
     
     ////////////////////////////////////////////////////////////////////////
     
 	protected void fileDisconnectFromDb_actionPerformed(ActionEvent e) {
 		
 		if (databaseConnectWindow.dbPortal != null) {
 			
 			databaseConnectWindow.dbPortal.finalize();
 			
 			this.FileConnectToDb.setEnabled(true);
 			this.FileDisconnectFromDb.setEnabled(false);
 			this.Operations.setEnabled(false);
 			this.mainWindowStatusPanelLoggedUserSettings.setEnabled(false);
 			this.mainWindowStatusPanelLoggedUserLogout.setEnabled(false);
 			this.mainWindowStatusPanelSetEnabled(false);
 			this.productsManagementPanel.setVisible(false);
 			this.ordersManagementPanel.setVisible(false);
 			this.inquiriesPanel.setVisible(false);
 			this.mainWindowStatusPanelLoggedUserLabel.setText(": ");
 			this.mainWindowStatusPanelConnectionInfoLabel.setText(": ");
 			operatorUserLoginWindow.loggedUserId = -1;
 		}
 	}
 	
 	////////////////////////////////////////////////////////////////////////
 	
 	protected void fileInformationForDbconnection_actionPerformed(ActionEvent e) {
 		
 		String info = "    .";
 		
 		if (databaseConnectWindow.dbPortal != null) {
 			
 			if (databaseConnectWindow.dbPortal.isConnected()) {
 				
 				info = "eShop     :\n" + ": " + databaseConnectWindow.dbPortal.getMySqlServerAddress() + ":" +
 				databaseConnectWindow.dbPortal.getMySqlServerPort() + "\n: " + databaseConnectWindow.dbPortal.getMySqlUsername() + "\n" +
 				" : " + databaseConnectWindow.dbPortal.getMySqlActiveDatabase();
 			}
 		}
 		
 		JOptionPane.showMessageDialog(this, info, "    ", JOptionPane.INFORMATION_MESSAGE);	
 	}
 	
 	////////////////////////////////////////////////////////////////////////
 	
 	protected void mainWindowStatusPanelLoggedUserLogin_actionPerformed(ActionEvent e) {
 		
 		//non modal new window:
 		//operatorUserLoginWindow oul = new operatorUserLoginWindow();
 		//oul.setVisible(true);
 		//oul.addWindowListener(operatorUserLoginWindowClosing);
 		
 		//modal dialog:
 		new operatorUserLoginWindow(this, true);
 		if (operatorUserLoginWindow.loggedUserId != -1) {
 			
 			mainWindowStatusPanelLoggedUserSettings.setEnabled(true);
 			mainWindowStatusPanelLoggedUserLogout.setEnabled(true);			
 			mainWindowPointer.Operations.setEnabled(true);
 			mainWindowPointer.mainWindowStatusPanelLoggedUserLabel.setText(": " + operatorUserLoginWindow.loggedUserNames);
 			
 			mainWindowPointer.cboDbManager = null; // we clear inquiries operators combo box in order to force reloading if new operator was created/edited/removed
 			mainWindowPointer.ordersManagementPanel.setVisible(false); // we need to refresh this panel (in case another operator logged we don't want to orders the previous operator made inside the orders management panel)
 			
 			// tries to refresh panels data if some of them are visible:
 			// (useful when logged user is changed)
 			
 			if (mainWindowPointer.productsManagementPanel.isVisible() == true) {
 				
 				ActionEvent ae = new ActionEvent(mainWindowPointer, 0, "ProductsManagementPanelRefresh");
 				mainWindowPointer.operationsProductsManagement_actionPerformed(ae);
 			}			
 			else if (mainWindowPointer.ordersManagementPanel.isVisible() == true) {
 				
 				ActionEvent ae = new ActionEvent(mainWindowPointer, 0, "OrdersManagementPanelRefresh");
 				mainWindowPointer.operationsOrdersManagement_actionPerformed(ae);
 				
 			}
 			else if (mainWindowPointer.inquiriesPanel.isVisible() == true) {
 
 				ActionEvent ae = new ActionEvent(mainWindowPointer, 0, "InquiriesPanelRefresh");
 				mainWindowPointer.operationsInquiries_actionPerformed(ae);
 			}
 		}
 		else {
 
 			mainWindowStatusPanelLoggedUserSettings.setEnabled(false);
 			mainWindowStatusPanelLoggedUserLogout.setEnabled(false);
 			mainWindowPointer.Operations.setEnabled(false);
 			mainWindowPointer.productsManagementPanel.setVisible(false);
 			mainWindowPointer.ordersManagementPanel.setVisible(false);
 			mainWindowPointer.inquiriesPanel.setVisible(false);
 			mainWindowPointer.mainWindowStatusPanelLoggedUserLabel.setText(": ");
 		}
 	}
 	
 	/*private static WindowListener operatorUserLoginWindowClosing = new WindowAdapter() {
 
 		public void windowClosing(WindowEvent e) {
 			
 			if (operatorUserLoginWindow.loggedUserId != -1) {
 				
 				mainWindowPointer.Operations.setEnabled(true);
 				mainWindowPointer.mainWindowStatusPanelLoggedUserLabel.setText(": " + operatorUserLoginWindow.loggedUserNames);
 				
 				// tries to refresh panels data if some of them are visible:
 				//(useful when logged user is changed)
 				
 				if (mainWindowPointer.productsManagementPanel.isVisible() == true) {
 					
 					ActionEvent ae = new ActionEvent(mainWindowPointer, 0, "ProductsManagementPanelRefresh");
 					mainWindowPointer.operationsProductsManagement_actionPerformed(ae);
 				}			
 				else if (mainWindowPointer.ordersManagementPanel.isVisible() == true) {
 					
 					ActionEvent ae = new ActionEvent(mainWindowPointer, 0, "OrdersManagementPanelRefresh");
 					mainWindowPointer.operationsOrdersManagement_actionPerformed(ae);
 					
 				}
 				else if (mainWindowPointer.inquiriesPanel.isVisible() == true) {
 
 					ActionEvent ae = new ActionEvent(mainWindowPointer, 0, "InquiriesPanelRefresh");
 					mainWindowPointer.operationsInquiries_actionPerformed(ae);
 				}
 			}
 			else {
 
 				mainWindowPointer.Operations.setEnabled(false);
 				mainWindowPointer.productsManagementPanel.setVisible(false);
 				mainWindowPointer.ordersManagementPanel.setVisible(false);
 				mainWindowPointer.inquiriesPanel.setVisible(false);
 				mainWindowPointer.mainWindowStatusPanelLoggedUserLabel.setText(": ");
 			}
 		}
 	};*/
 		
 	////////////////////////////////////////////////////////////////////////
 	
 	protected void mainWindowStatusPanelLoggedUserSettings_actionPerformed(ActionEvent e) {
 		
 		if (operatorUserLoginWindow.loggedUserId == -1) {
 			return;
 		}
 		
 		//non modal frame:
 		//operatorUserSettingsWindow ousw = new operatorUserSettingsWindow();
 		//ousw.setVisible(true);
 		//ousw.addWindowListener(operatorUserSettingsWindowClosing);
 		
 		//modal dialog:
 		new operatorUserSettingsWindow(this, true);
 		if (operatorUserLoginWindow.loggedUserId != -1) {
 			
 			mainWindowPointer.mainWindowStatusPanelLoggedUserLabel.setText(": " + operatorUserSettingsWindow.operatorFirstNameLastNameCombination);
 			
 			mainWindowPointer.cboDbManager = null; //we clear inquiries operators combo box in order to force reloading if new operator was created/edited/removed
 			mainWindowPointer.inquiriesPanel.setVisible(false); // we must refresh those
 			mainWindowPointer.ordersManagementPanel.setVisible(false); // 2 panels
 		}
 		else { //if operator was deleted
 			
 			mainWindowPointer.mainWindowStatusPanelLoggedUserSettings.setEnabled(false);
 			mainWindowPointer.mainWindowStatusPanelLoggedUserLogout.setEnabled(false);
 			mainWindowPointer.Operations.setEnabled(false);
 			mainWindowPointer.productsManagementPanel.setVisible(false);
 			mainWindowPointer.ordersManagementPanel.setVisible(false);
 			mainWindowPointer.inquiriesPanel.setVisible(false);
 			mainWindowPointer.mainWindowStatusPanelLoggedUserLabel.setText(": ");
 
 			mainWindowPointer.cboDbManager = null; //we clear inquiries operators combo box in order to force reloading if new operator was created/edited/removed
 		}					
 	}
 	
 	/*private static WindowListener operatorUserSettingsWindowClosing = new WindowAdapter() {
 		
 		public void windowClosing(WindowEvent e) {
 			if (operatorUserLoginWindow.loggedUserId != -1) {
 				
 				mainWindowPointer.mainWindowStatusPanelLoggedUserLabel.setText(": " + operatorUserSettingsWindow.operatorFirstNameLastNameCombination);
 			}
 			else { //if operator was deleted
 				
 				mainWindowPointer.Operations.setEnabled(false);
 				mainWindowPointer.productsManagementPanel.setVisible(false);
 				mainWindowPointer.ordersManagementPanel.setVisible(false);
 				mainWindowPointer.inquiriesPanel.setVisible(false);
 				mainWindowPointer.mainWindowStatusPanelLoggedUserLabel.setText(": ");
 			}
 		}
 	};*/
 	
 	////////////////////////////////////////////////////////////////////////
 	
 	protected void mainWindowStatusPanelLoggedUserLogout_actionPerformed(ActionEvent e) {
 
 		this.mainWindowStatusPanelLoggedUserSettings.setEnabled(false);
 		this.mainWindowStatusPanelLoggedUserLogout.setEnabled(false);
 		this.Operations.setEnabled(false);
 		this.productsManagementPanel.setVisible(false);
 		this.ordersManagementPanel.setVisible(false);
 		this.inquiriesPanel.setVisible(false);
 		this.mainWindowStatusPanelLoggedUserLabel.setText(": ");
 		operatorUserLoginWindow.loggedUserId = -1;
 	}
 	
 	protected void helpAbout_actionPerformed(ActionEvent e) {
 		
 		JOptionPane.showMessageDialog(this, "eShop ver.-1.01\n(C) 2013  \n\n  \" \"," +
 				" \nUniversity of Plovdiv \"Paisii Hilendarski\", Bulgaria\n\n      :\n\n                        http://github.com/zhgzhg/eshop\n\n" + 
 				"      !!!", "", JOptionPane.INFORMATION_MESSAGE);
 	}
 	
 	public void operationsProductsManagement_actionPerformed(ActionEvent e) {
 
 		ordersManagementPanel.setVisible(false);
 		inquiriesPanel.setVisible(false);
 		
 		if ((productsManagementPanel.isVisible() == true) && (e.getActionCommand().compareTo("ProductsManagementPanelRefresh") != 0)) { // if opened on second click just close it
 			
 			productsManagementPanel.setVisible(false);
 			return;
 		}
 		
 		((ProductsTableTableModel)productsTable.getModel()).fireTableDataChanged();
 		((ProductsTableTableModel)productsTable.getModel()).populateTableWithDatabaseData();
 		productsTable.getColumnModel().getColumn(1).setPreferredWidth(productsTable.getColumnModel().getColumn(1).getPreferredWidth() + 7); // tune column   little bit
 		productsManagementPanel.setVisible(true);
 		getContentPane().add(productsManagementPanel); // we are using BorderLayout so we need to add this panel again in order to get visible
 		
 	}
 	public void operationsOrdersManagement_actionPerformed(ActionEvent e) {
 
 		productsManagementPanel.setVisible(false);
 		inquiriesPanel.setVisible(false);
 		
 		if ((ordersManagementPanel.isVisible() == true)  && (e.getActionCommand().compareTo("OrdersManagementPanelRefresh") != 0)) { //if opened on second click just close it
 			
 			cbpDbManager = null;
 			ordersManagementPanel.setVisible(false);
 			return;
 		}
 		
 		ordersInfoTable.getColumnModel().getColumn(0).setPreferredWidth(66); //resize column  
 		ordersInfoTable.getColumnModel().getColumn(1).setPreferredWidth(129); //resize column   
 		
 		orderDetailsTable.getColumnModel().getColumn(0).setPreferredWidth(177); //resize column 
 		orderDetailsTable.getColumnModel().getColumn(1).setPreferredWidth(70); //resize column 
 		orderDetailsTable.getColumnModel().getColumn(2).setPreferredWidth(55); //resize column . 
 		
 		if (cbpDbManager == null) { // populate products combobox control with products names
 			
 			cbpDbManager = new ComboBox_Products_db_manager(databaseConnectWindow.dbPortal);
 			cbpDbManager.loadAllProducts();
 			
 			if (ordersManagementOperationsPanelProductsComboBox.getItemCount() > 0) {
 				ordersManagementOperationsPanelProductsComboBox.removeAllItems();
 			}
 			
 			String[] items = cbpDbManager.getAllLoadedProductsNames();
 			
 			if (items != null) {
 				
 				for (int i = 0; i < items.length; i++) {
 					ordersManagementOperationsPanelProductsComboBox.addItem(items[i]);
 				}			
 			}
 		}
 		
 		((OrdersInfoTableTableModel)ordersInfoTable.getModel()).fireTableDataChanged();
 		((OrderDetailsTableTableModel)orderDetailsTable.getModel()).fireTableDataChanged();
 		
 		((OrdersInfoTableTableModel)ordersInfoTable.getModel()).populateTableWithDatabaseData();
 		ordersManagementPanel.setVisible(true);
 		getContentPane().add(ordersManagementPanel); // we are using BorderLayout so we need to add this panel again in order to get visible
 	}
 	
 	public void operationsInquiries_actionPerformed(ActionEvent e) {
 		
 		productsManagementPanel.setVisible(false);
 		ordersManagementPanel.setVisible(false);
 		
 		if ((inquiriesPanel.isVisible() == true) && (e.getActionCommand().compareTo("InquiriesPanelRefresh") != 0)) { // if opened on second click just close it
 			
 			inquiriesPanel.setVisible(false);
 			return;
 		}	
 		
 		// in case - to prevent SQL injection
 		inquiriesFiltersPanelOrderNumberSpinner.validate();
 		inquiriesFiltersPanelOrderDateSpinner.validate();
 		inquiriesFiltersPanelOrderTotalPriceSpinner.validate();
 		
 		if (cboDbManager == null) {
 			
 			cboDbManager = new ComboBox_Operators_db_manager(databaseConnectWindow.dbPortal);
 			cboDbManager.loadAllOperators();
 			
 			if (inquiriesFiltersPanelOrderOperatorComboBox.getItemCount() > 0) {
 				
 				inquiriesFiltersPanelOrderOperatorComboBox.removeAllItems();
 			}
 			
 			for (String operator:cboDbManager.getAllLoadedOperatorsNames()) {
 				
 				inquiriesFiltersPanelOrderOperatorComboBox.addItem(operator);
 			}
 		}
 		
 		((InquiriesTableTableModel)inquiriesTable.getModel()).prepareDbView();
 		((InquiriesTableTableModel)inquiriesTable.getModel()).populateTableWithDatabaseData();
 		((InquiriesTableTableModel)inquiriesTable.getModel()).fireTableDataChanged();
 		
 		inquiriesPanel.setVisible(true);
 		getContentPane().add(inquiriesPanel); // we are using BorderLayout so we need to add this panel again in order to get visible		
 	}
 	
 	////////////////////////////////////////////////////////////////////////
 	
 	protected void productsTable_mouseClicked(MouseEvent e) {
 		
 		int quantity = 0;
 		double price = 0.00;
 		
 		try {
 			quantity = Integer.parseInt(productsTable.getValueAt(productsTable.getSelectedRow(), 1).toString());
 		}
 		catch (Exception ex) {			
 		}
 		
 		try {
 			 price = Double.parseDouble(productsTable.getValueAt(productsTable.getSelectedRow(), 2).toString());
 		}
 		catch (Exception ex2) {			
 		}
 		
 		productsManagementToolsPanelProductNameTextField.setText(productsTable.getValueAt(productsTable.getSelectedRow(), 0).toString());
 		productsManagementToolsPanelProductQuantitySpinner.setValue(quantity);		
 		productsManagementToolsPanelProductPriceSpinner.setValue(price);		
 	}	
 	
 	protected void productsManagementToolsPanelProductAddButton_actionPerformed(ActionEvent e) {
 		
 		if (productsManagementToolsPanelProductNameTextField.getText().length() > 0) {
 			
 			((ProductsTableTableModel)productsTable.getModel()).insertNewRow(productsManagementToolsPanelProductNameTextField.getText(),
 					Integer.parseInt(productsManagementToolsPanelProductQuantitySpinner.getValue().toString()),
 					Double.parseDouble(productsManagementToolsPanelProductPriceSpinner.getValue().toString())
 					);
 			
 			((ProductsTableTableModel)productsTable.getModel()).fireTableDataChanged();	
 			
 			cbpDbManager = null; // we clear the content in orders management products combobox in order to force it if called to reload
 		}
 		else {
 			JOptionPane.showMessageDialog(this, "      !", "", JOptionPane.ERROR_MESSAGE);
 		}		
 	}
 	
 	protected void productsManagementToolsPanelProductEditButton_actionPerformed(ActionEvent e) {
 		
 		if (productsManagementToolsPanelProductNameTextField.getText().length() > 0) {
 			
 			int selectedTableRow = productsTable.getSelectedRow();
 			
 			if (selectedTableRow == -1) {
 				
 				JOptionPane.showMessageDialog(this, " !", "", JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 			
 			((ProductsTableTableModel)productsTable.getModel()).updateSelectedRow(selectedTableRow,
 					productsManagementToolsPanelProductNameTextField.getText(),
 					Integer.parseInt(productsManagementToolsPanelProductQuantitySpinner.getValue().toString()),
 					Double.parseDouble(productsManagementToolsPanelProductPriceSpinner.getValue().toString())
 					);
 			((ProductsTableTableModel)productsTable.getModel()).fireTableDataChanged();
 			
 			productsTable.changeSelection(selectedTableRow, 0, true, false);
 			
 			cbpDbManager = null; // we clear the content in orders management products combobox in order to force it if called to reload
 		}
 		else {
 			JOptionPane.showMessageDialog(this, "      !", "", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	protected void productsManagementToolsPanelProductRemoveButton_actionPerformed(ActionEvent e) {
 		
 		if (productsTable.getSelectedRow() == -1) {
 			
 			JOptionPane.showMessageDialog(this, " !", "", JOptionPane.WARNING_MESSAGE);
 			return;
 		}
 		
 		if (JOptionPane.showConfirmDialog(this, "           !\n  ?", "  ", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 			
 			((ProductsTableTableModel)productsTable.getModel()).removeSelectedRow(productsTable.getSelectedRow());
 			((ProductsTableTableModel)productsTable.getModel()).fireTableDataChanged();
 			cbpDbManager = null; // we clear the content in orders management products combobox in order to force it if called to reload
 		}
 	}	
 	
 	////////////////////////////////////////////////////////////////////////	
 	
 	protected void calculateOrderTotalPrice() {
 		
 		double tp = 0.00, p = 0.00;
 		int j = 0;
 		
 		if (ordersInfoTable.getSelectedRow() != -1) {
 				
 			j = orderDetailsTable.getRowCount();
 		}
 		
 		if (j <= 0) {
 			ordersManagementOperationsPanelProductOrderTotalPrice.setText(" : 0.00");
 			return;
 		}
 		
 		for (int i = 0; i < j; i++) {
 			
 			try {
 				p = (Integer.parseInt(orderDetailsTable.getValueAt(i, 1).toString()) * Double.parseDouble(orderDetailsTable.getValueAt(i, 2).toString()));
 				tp += p;
 			}
 			catch (Exception ex) {
 				
 			}
 		}
 		
 		ordersManagementOperationsPanelProductOrderTotalPrice.setText(" : " + tp);
 	}
 	
 	protected void ordersInfoTable_mouseClicked(MouseEvent e) {
 		
 		int orderId = -1;
 		
 		try {
 			orderId = Integer.parseInt(ordersInfoTable.getValueAt(ordersInfoTable.getSelectedRow(), 0).toString());
 		}
 		catch (Exception ex) {			
 		}
 		
 		((OrderDetailsTableTableModel)orderDetailsTable.getModel()).populateTableWithDatabaseData(orderId) ;
 		((OrderDetailsTableTableModel)orderDetailsTable.getModel()).fireTableDataChanged();
 		
 		calculateOrderTotalPrice();		
 	}
 	
 	protected void orderDetailsTable_mouseClicked(MouseEvent e) {
 				
 		if (((OrderDetailsTableTableModel)orderDetailsTable.getModel()).isTableEmpty() == false) {			
 		
 			int id = cbpDbManager.getProductStringArrayIdByProductDbId(
 					Integer.parseInt(((OrderDetailsTableTableModel)orderDetailsTable.getModel()).CELLS[orderDetailsTable.getSelectedRow()][5]));
 	
 			if (id != -1) {
 				
 				//change current selected quantity spinner:
 				
 				ordersManagementOperationsPanelProductsComboBox.setSelectedIndex(id);
 				
 				Integer orderedQuantity = new Integer(orderDetailsTable.getValueAt(orderDetailsTable.getSelectedRow(), 1).toString());
 				ordersManagementOperationsPanelProductQuantitySpinner.setValue(orderedQuantity);
 				
 				//change the maximum editable quantity for the selected product:
 				 
 				Integer totalProductAvailableQuantity = orderedQuantity + cbpDbManager.getProductQuantityByProductStringArrayId(id); //ordered plus current maximum available quantity of the selected product 
 				((SpinnerNumberModel)ordersManagementOperationsPanelProductQuantitySpinner.getModel()).setMaximum(totalProductAvailableQuantity);
 			}		
 		}
 	}
 	
 	protected void ordersManagementOperationsPanelNewOrderButton_actionPerformed(ActionEvent e) {
 		
 		((OrdersInfoTableTableModel)ordersInfoTable.getModel()).insertNewRow();
 		((OrdersInfoTableTableModel)ordersInfoTable.getModel()).fireTableDataChanged();
 		
 		//new row inserted maybe. now select it:
 		ordersInfoTable.changeSelection((ordersInfoTable.getRowCount() - 1), 0, true, false);
 		ordersInfoTable_mouseClicked(null);
 		
 		//calculateOrderTotalPrice(); no need to call it because ordersInfoTable_mouseClicked calls it
 	}
 	
 	protected void ordersManagementOperationsPanelDeleteOrderButton_actionPerformed(ActionEvent e) {
 		
 		int selectedRow = ordersInfoTable.getSelectedRow();
 		if (selectedRow == -1) {
 			
 			JOptionPane.showMessageDialog(this, "        !", "    ", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		
 		if (JOptionPane.showConfirmDialog(this, "  ?", "  ", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 		
 			//before proceed first increase the available product quantities
 			
 			if (((OrderDetailsTableTableModel)orderDetailsTable.getModel()).isTableEmpty() == false) {
 			
 				for (int i = 0; i < orderDetailsTable.getRowCount(); i++) {
 					int psarid = cbpDbManager.getProductStringArrayIdByProductDbId( 
 							Integer.parseInt(((OrderDetailsTableTableModel)orderDetailsTable.getModel()).CELLS[i][5]));
 					cbpDbManager.increaseProductQuantityFromProductStringArrayId(psarid, 
 							Integer.parseInt(((OrderDetailsTableTableModel)orderDetailsTable.getModel()).CELLS[i][1]));
 					
 					//refresh maximum quantity spinner in this item was selected
 					
 					if (psarid == ordersManagementOperationsPanelProductsComboBox.getSelectedIndex()) {					
 						
 						ItemEvent ie = new ItemEvent(ordersManagementOperationsPanelProductsComboBox, 0, null, ItemEvent.SELECTED);
 						ordersManagementOperationsPanelProductsComboBox_itemStateChanged(ie);
 					}
 				}
 			}
 			
 			((OrdersInfoTableTableModel)ordersInfoTable.getModel()).removeSelectedRow(selectedRow);
 			((OrdersInfoTableTableModel)ordersInfoTable.getModel()).fireTableDataChanged();
 			
 			((OrderDetailsTableTableModel)orderDetailsTable.getModel()).removeAllRowsVisualEffect();
 			((OrderDetailsTableTableModel)orderDetailsTable.getModel()).fireTableDataChanged();
 			
 			calculateOrderTotalPrice();
 		}		
 	}
 	
 	protected void ordersManagementOperationsPanelProductsComboBox_itemStateChanged(ItemEvent e) {
 		
 		if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
 			
 			if (cbpDbManager != null) {
 				
 				Integer quantity = cbpDbManager.getProductQuantityByProductStringArrayId(ordersManagementOperationsPanelProductsComboBox.getSelectedIndex());				
 				
 				if (quantity != null) {
 					((SpinnerNumberModel)ordersManagementOperationsPanelProductQuantitySpinner.getModel()).setMaximum(quantity.intValue());
 										
 					if (Integer.parseInt(ordersManagementOperationsPanelProductQuantitySpinner.getValue().toString()) > 
 					quantity.intValue()) { // if the current quantity is out of the new maximum now reduce it
 						
 						ordersManagementOperationsPanelProductQuantitySpinner.setValue(quantity);
 					}
 				}
 				else {
 					((SpinnerNumberModel)ordersManagementOperationsPanelProductQuantitySpinner.getModel()).setMaximum(0);
 				}
 			}			
 		}
 	}
 	
 	protected void ordersManagementOperationsPanelProductAddButton_actionPerformed(ActionEvent e) {
 		
 		// raise again ordersManagementOperationsPanelProductsComboBox_itemStateChanged event
 		// if case that the user has clicked on the order detail table on specific product which is out of stock
 		// but the quantity spinner automatically changes it's value because it's more comfortable for editing purposes
 		// AND then the user has changed the current order with new one where the out of stock product is not in the
 		// orders details list so we MUST reduce the current selected quality to the maximum available
 		
 		ItemEvent ie = new ItemEvent(ordersManagementOperationsPanelProductsComboBox, 0, null, ItemEvent.SELECTED);
 		ordersManagementOperationsPanelProductsComboBox_itemStateChanged(ie);
 		
 		//////////
 			
 		if (ordersInfoTable.getSelectedRow() == -1) {
 			JOptionPane.showMessageDialog(this, "  ,     !", 
 					"    ", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		
 		if (((OrdersInfoTableTableModel)ordersInfoTable.getModel()).isTableEmpty() == true) {
 			
 			JOptionPane.showMessageDialog(this, "   !", "    ", 
 					JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		
 		int orderId = Integer.parseInt(ordersInfoTable.getValueAt(ordersInfoTable.getSelectedRow(), 0).toString());
 		
 		if (ordersManagementOperationsPanelProductsComboBox.getSelectedIndex() != -1) {
 			
 			if (Integer.parseInt(ordersManagementOperationsPanelProductQuantitySpinner.getValue().toString()) > 0) {
 		
 				// first check if selected product has been added before
 				
 				int productId = -1;
 				
 				productId = cbpDbManager.getProductIdByProductStringArrayId(
 						ordersManagementOperationsPanelProductsComboBox.getSelectedIndex());
 				
 				if (((OrderDetailsTableTableModel)orderDetailsTable.getModel()).isProductAlreadyInTheTable(productId) == true) {
 					
 					JOptionPane.showMessageDialog(this, "     .\n" + 
 							"       .", "    ", 
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				
 				Integer quantity = new Integer(ordersManagementOperationsPanelProductQuantitySpinner.getValue().toString());
 				
 				((OrderDetailsTableTableModel)orderDetailsTable.getModel()).insertNewRow(orderId, productId, quantity);
 				
 				if (cbpDbManager.decreaseProductQuantityFromProductStringArrayId(
 						ordersManagementOperationsPanelProductsComboBox.getSelectedIndex(),	quantity) == false) {
 					
 					((OrderDetailsTableTableModel)orderDetailsTable.getModel()).removeSelectedRow(orderDetailsTable.getRowCount() - 1);					
 				}
 				
 				((OrderDetailsTableTableModel)orderDetailsTable.getModel()).fireTableDataChanged();
 				
 				// now update the date and time of the last changes of the current order in orders list table
 				((OrdersInfoTableTableModel)ordersInfoTable.getModel()).updateSelectedRow(ordersInfoTable.getSelectedRow());
 				
 				calculateOrderTotalPrice();
 			}
 			else {
 				JOptionPane.showMessageDialog(this, "      .", "    ", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		else {
 			
 			JOptionPane.showMessageDialog(this, "    !", "    ", JOptionPane.ERROR_MESSAGE);
 		}
 		
 	}
 	
 	protected void ordersManagementOperationsPanelProductEditButton_actionPerformed(ActionEvent e) {
 		
 		if (ordersInfoTable.getSelectedRow() == -1) {
 			JOptionPane.showMessageDialog(this, "  ,    !", 
 					"    ", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		
 		if (((OrdersInfoTableTableModel)ordersInfoTable.getModel()).isTableEmpty() == true) {
 			
 			JOptionPane.showMessageDialog(this, "   !", "    ", 
 					JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		
 		if (orderDetailsTable.getSelectedRow() == -1) {
 			JOptionPane.showMessageDialog(this, "  ,   !", 
 					"    ", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		
 		if (((OrderDetailsTableTableModel)orderDetailsTable.getModel()).isTableEmpty() == true) {
 			
 			JOptionPane.showMessageDialog(this, "  !", "    ", 
 					JOptionPane.ERROR_MESSAGE);
 			return;
 		} 
 		
 		int orderId = Integer.parseInt(ordersInfoTable.getValueAt(ordersInfoTable.getSelectedRow(), 0).toString());
 		int currentProductId = Integer.parseInt(((OrderDetailsTableTableModel)orderDetailsTable.getModel()).CELLS[orderDetailsTable.getSelectedRow()][5]);
 		int currentProductQuantity = Integer.parseInt(((OrderDetailsTableTableModel)orderDetailsTable.getModel()).CELLS[orderDetailsTable.getSelectedRow()][1]);
 				
 		if (ordersManagementOperationsPanelProductsComboBox.getSelectedIndex() != -1) {
 						
 			Integer npid = cbpDbManager.getProductIdByProductStringArrayId(ordersManagementOperationsPanelProductsComboBox.getSelectedIndex()).intValue();
 			int newProductId = -1;
 			
 			if (npid != null) {
 				newProductId = npid.intValue();
 			}			
 			
 			if (newProductId == -1) {
 				JOptionPane.showMessageDialog(this, " (1)    .", " ", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			
 			// now we check if the selected (edited) product is already in the ordered products table and if so inform the user
 			
 			if (currentProductId != newProductId) {
 			
 				String[][] lCELLS = ((OrderDetailsTableTableModel)orderDetailsTable.getModel()).CELLS;
 				String lnProductId = new String("" + newProductId);
 				
 				for (int i = 0; i < lCELLS.length; i++) {
 					
 					if (lCELLS[i][5].compareTo(lnProductId) == 0) {
 						
 						JOptionPane.showMessageDialog(this, "     !\n" + 
 								"    ,     \n" + 
 								"   .", "", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 				}
 			}
 				
 			
 			int newProductQuantity = -1;
 			newProductQuantity = Integer.parseInt(ordersManagementOperationsPanelProductQuantitySpinner.getValue().toString());
 			
 			if (newProductQuantity == -1) {
 				
 				JOptionPane.showMessageDialog(this, " (2)    .", " ", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			
 			if (newProductQuantity == 0) {
 				
 				JOptionPane.showMessageDialog(this, "  !", "    ", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			
 			if (newProductId == currentProductId) { // modify only the quantity
 				
 				if (newProductQuantity == currentProductQuantity) {
 					return;
 				}
 				
 				if (newProductQuantity > currentProductQuantity) { //decrease the quantity in the database; increase in the order
 					
 					int newDiffQuantity = newProductQuantity - currentProductQuantity;
 					
 					((OrderDetailsTableTableModel)orderDetailsTable.getModel()).updateSelectedRow(orderDetailsTable.getSelectedRow(), 
 							currentProductId, newProductQuantity);
 					
 					if (cbpDbManager.decreaseProductQuantityFromProductStringArrayId(ordersManagementOperationsPanelProductsComboBox.getSelectedIndex(), 
 							newDiffQuantity) == false) {
 						
 						((OrderDetailsTableTableModel)orderDetailsTable.getModel()).removeSelectedRow(orderDetailsTable.getSelectedRow());
 						return;
 					}
 				}
 				else { // increase the quantity in the database; decrease in the order
 					
 					int newDiffQuantity = currentProductQuantity - newProductQuantity;
 					
 					((OrderDetailsTableTableModel)orderDetailsTable.getModel()).updateSelectedRow(orderDetailsTable.getSelectedRow(), 
 							currentProductId, newProductQuantity);
 
 					if (cbpDbManager.increaseProductQuantityFromProductStringArrayId(ordersManagementOperationsPanelProductsComboBox.getSelectedIndex(), 
 							newDiffQuantity) == false) {
 						
 						((OrderDetailsTableTableModel)orderDetailsTable.getModel()).removeSelectedRow(orderDetailsTable.getSelectedRow());
 						return;
 					}
 				}
 			}
 			else { // modify the product and the quantity too
 			
 				// first remove the old product from the program table and database and increase it's available quantity in the database and the program
 				
 				int psaid = cbpDbManager.getProductStringArrayIdByProductDbId(currentProductId);
 				
 				if (cbpDbManager.increaseProductQuantityFromProductStringArrayId(psaid,	currentProductQuantity) == true) {
 				
 					((OrderDetailsTableTableModel)orderDetailsTable.getModel()).removeSelectedRow(orderDetailsTable.getSelectedRow());
 				}
 				else { 
 					return; 
 				}
 				
 				// then insert the new product in the program table and database and decrease it's available quantity in the database and the program
 				((OrderDetailsTableTableModel)orderDetailsTable.getModel()).insertNewRow(orderId, newProductId, newProductQuantity);
 					
 				if (cbpDbManager.decreaseProductQuantityFromProductStringArrayId(ordersManagementOperationsPanelProductsComboBox.getSelectedIndex(), 
 					newProductQuantity) == false) {
 				
 				((OrderDetailsTableTableModel)orderDetailsTable.getModel()).removeSelectedRow(orderDetailsTable.getSelectedRow());
 				}
 				
 			}
 			
 			((OrderDetailsTableTableModel)orderDetailsTable.getModel()).fireTableDataChanged();
 			
 			// simulate comboBox click on the current item (item change with the same item) in order to update the maximum quantity limit of the spinner
 			ItemEvent ie = new ItemEvent(ordersManagementOperationsPanelProductsComboBox, 0, null, ItemEvent.SELECTED);
 			ordersManagementOperationsPanelProductsComboBox_itemStateChanged(ie);
 			
 			// now update the date and time of the last changes of the current order in orders list table
 			((OrdersInfoTableTableModel)ordersInfoTable.getModel()).updateSelectedRow(ordersInfoTable.getSelectedRow());
 			
 			calculateOrderTotalPrice();
 		}
 		else {
 			
 			JOptionPane.showMessageDialog(this, "    !", "    ", JOptionPane.ERROR_MESSAGE);
 		}
 		
 	}
 	
 	protected void ordersManagementOperationsPanelProductDeleteButton_actionPerformed(ActionEvent e) {
 		
 		if (ordersInfoTable.getSelectedRow() == -1) {
 			JOptionPane.showMessageDialog(this, "  ,     !", 
 					"    ", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		
 		if (((OrdersInfoTableTableModel)ordersInfoTable.getModel()).isTableEmpty() == true) {
 			
 			JOptionPane.showMessageDialog(this, "   ,     !", "    ", 
 					JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		
 		if (orderDetailsTable.getSelectedRow() == -1) {
 			JOptionPane.showMessageDialog(this, "  ,   !", 
 					"    ", JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		
 		if (JOptionPane.showConfirmDialog(this, "  ?", "    ", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
 			return;
 		}
 		
 		int currentProductId = Integer.parseInt(((OrderDetailsTableTableModel)orderDetailsTable.getModel()).CELLS[orderDetailsTable.getSelectedRow()][5]);
 		int currentProductQuantity = Integer.parseInt(((OrderDetailsTableTableModel)orderDetailsTable.getModel()).CELLS[orderDetailsTable.getSelectedRow()][1]);
 		
 		// remove the product from the database information about the current order		
 		
 		((OrderDetailsTableTableModel)orderDetailsTable.getModel()).removeSelectedRow(orderDetailsTable.getSelectedRow());
 		
 		//increase the product available quantity in the database and program
 		int psaid = cbpDbManager.getProductStringArrayIdByProductDbId(currentProductId);
 		if (cbpDbManager.increaseProductQuantityFromProductStringArrayId(psaid, currentProductQuantity) == false) {
 			
 			JOptionPane.showMessageDialog(this, "        !\n" + 
 					"   !\n,  !", " ", JOptionPane.WARNING_MESSAGE);
 		}
 		
 		((OrderDetailsTableTableModel)orderDetailsTable.getModel()).fireTableDataChanged();
 		
 		// if the removed product was selected in the comboBox before that we need to update spinner maximum quantity limit value
 		if (psaid == ordersManagementOperationsPanelProductsComboBox.getSelectedIndex()) {
 			
 			// so we simulate click on in (item change with the same item)
 			ItemEvent ie = new ItemEvent(ordersManagementOperationsPanelProductsComboBox, 0, null, ItemEvent.SELECTED);
 			ordersManagementOperationsPanelProductsComboBox_itemStateChanged(ie);
 		}
 		
 		// now update the date and time of the last changes of the current order in orders list table
 		((OrdersInfoTableTableModel)ordersInfoTable.getModel()).updateSelectedRow(ordersInfoTable.getSelectedRow());
 		
 		calculateOrderTotalPrice();
 	}
 	
 	/////////////////////////////////////////////////////////////////////
 	
 	protected void inquiriesFiltersPanelOrderNumberAndButton_actionPerformed(ActionEvent e) {
 		
 		String sign = inquiriesFiltersPanelOrderNumberStatementsComboBox.getSelectedItem().toString();
 		String text = statementsTextArea.getText();
 		
 		if (text.length() > 0) {
 			
 			text += " AND order_id" + sign + inquiriesFiltersPanelOrderNumberSpinner.getValue().toString();
 		}
 		else {
 			text += "order_id" + sign + inquiriesFiltersPanelOrderNumberSpinner.getValue().toString();
 		}	
 		
 		statementsTextArea.setText(text);
 	}
 	
 	protected void inquiriesFiltersPanelOrderNumberOrButton_actionPerformed(ActionEvent e) {
 		
 		String sign = inquiriesFiltersPanelOrderNumberStatementsComboBox.getSelectedItem().toString();
 		String text = statementsTextArea.getText();
 		
 		if (text.length() > 0) {
 			
 			text += " OR order_id" + sign + inquiriesFiltersPanelOrderNumberSpinner.getValue().toString();
 		}
 		else {
 			text += "order_id" + sign + inquiriesFiltersPanelOrderNumberSpinner.getValue().toString();
 		}	
 		
 		statementsTextArea.setText(text);
 	}
 	
 	protected void inquiriesFiltersPanelDateAndTimeAndButton_actionPerformed(ActionEvent e) {
 		
 		String sign = inquiriesFiltersPanelDateAndTimeStatementsComboBox.getSelectedItem().toString();
 		String text = statementsTextArea.getText();
 		
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		
 		if (text.length() > 0) {
 			
 			text += " AND order_time" + sign + "'" + df.format((Date)inquiriesFiltersPanelOrderDateSpinner.getValue()) + "'";
 		}
 		else {
 			text += "order_time" + sign + "'" + df.format((Date)inquiriesFiltersPanelOrderDateSpinner.getValue()) + "'";
 		}	
 		
 		statementsTextArea.setText(text);
 	}
 	
 	protected void inquiriesFiltersPanelDateAndTimeOrButton_actionPerformed(ActionEvent e) {
 		
 		String sign = inquiriesFiltersPanelDateAndTimeStatementsComboBox.getSelectedItem().toString();
 		String text = statementsTextArea.getText();
 		
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		
 		if (text.length() > 0) {
 			
 			text += " OR order_time" + sign + "'" + df.format((Date)inquiriesFiltersPanelOrderDateSpinner.getValue()) + "'";
 		}
 		else {
 			text += "order_time" + sign + "'" + df.format((Date)inquiriesFiltersPanelOrderDateSpinner.getValue()) + "'";
 		}	
 		
 		statementsTextArea.setText(text);
 	}
 	
 	protected void inquiriesFiltersPanelOrderOperatorAndButton_actionPerformed(ActionEvent e) {
 		
 		String sign = inquiriesFiltersPanelOperatorLabelStatementsComboBox.getSelectedItem().toString();
 		String text = statementsTextArea.getText();
 		
 		if (text.length() > 0) {
 			
 			text += " AND order_operator_id" + sign + cboDbManager.getOperatorIdByOperatorStringArrayId(
					inquiriesFiltersPanelOperatorLabelStatementsComboBox.getSelectedIndex());
 		}
 		else {
 			text += "order_operator_id" + sign + cboDbManager.getOperatorIdByOperatorStringArrayId(
					inquiriesFiltersPanelOperatorLabelStatementsComboBox.getSelectedIndex());
 		}	
 		
 		statementsTextArea.setText(text);
 	}	
 	protected void inquiriesFiltersPanelOrderOperatorOrButton_actionPerformed(ActionEvent e) {
 		
 		String sign = inquiriesFiltersPanelOperatorLabelStatementsComboBox.getSelectedItem().toString();
 		String text = statementsTextArea.getText();
 		
 		if (text.length() > 0) {
 			
 			text += " OR order_operator_id" + sign + cboDbManager.getOperatorIdByOperatorStringArrayId(
					inquiriesFiltersPanelOperatorLabelStatementsComboBox.getSelectedIndex());
 		}
 		else {
 			text += "order_operator_id" + sign + cboDbManager.getOperatorIdByOperatorStringArrayId(
					inquiriesFiltersPanelOperatorLabelStatementsComboBox.getSelectedIndex());
 		}	
 		
 		statementsTextArea.setText(text);
 	}
 	
 	protected void inquiriesFiltersPanelOrderTotalPriceAndButton_actionPerformed(ActionEvent e) {
 		
 		String sign = inquiriesFiltersPanelOrderTotalPriceStatementsComboBox.getSelectedItem().toString();
 		String text = statementsTextArea.getText();
 		
 		if (text.length() > 0) {
 			
 			text += " AND order_total_price" + sign + inquiriesFiltersPanelOrderTotalPriceSpinner.getValue().toString();
 		}
 		else {
 			text += "order_total_price" + sign + inquiriesFiltersPanelOrderTotalPriceSpinner.getValue().toString();
 		}	
 		
 		statementsTextArea.setText(text);
 	}
 	
 	protected void inquiriesFiltersPanelOrderTotalPriceOrButton_actionPerformed(ActionEvent e) {
 		
 		String sign = inquiriesFiltersPanelOrderTotalPriceStatementsComboBox.getSelectedItem().toString();
 		String text = statementsTextArea.getText();
 		
 		if (text.length() > 0) {
 			
 			text += " OR order_total_price" + sign + inquiriesFiltersPanelOrderTotalPriceSpinner.getValue().toString();
 		}
 		else {
 			text += "order_total_price" + sign + inquiriesFiltersPanelOrderTotalPriceSpinner.getValue().toString();
 		}	
 		
 		statementsTextArea.setText(text);
 	}
 	
 	protected void inquiriesFiltersPanelOrderClearFilterStatementsButton_actionPerformed(ActionEvent e) {
 		
 		statementsTextArea.setText("");
 	}
 	
 	protected void inquiriesFiltersPanelOrderFilterButton_actionPerformed(ActionEvent e) {
 		
 		String text = statementsTextArea.getText();
 		
 		if (text.length() > 0) {	
 			
 			((InquiriesTableTableModel)inquiriesTable.getModel()).populateTableWithDatabaseData(text);
 		}
 		else {
 			((InquiriesTableTableModel)inquiriesTable.getModel()).populateTableWithDatabaseData();
 		}
 		
 		((InquiriesTableTableModel)inquiriesTable.getModel()).fireTableDataChanged();
 		inquiriesTable.repaint();
 	}
 	
 	protected void inquiriesTable_mouseClicked(MouseEvent e) {
 		
 		//populate the filter controls with the selected data for ease
 				
 		if (((InquiriesTableTableModel)inquiriesTable.getModel()).isTableEmpty() == true) {
 			return;
 		}
 		
 		int selectedRow = inquiriesTable.getSelectedRow();
 		
 		inquiriesFiltersPanelOrderNumberSpinner.setValue(new Integer(inquiriesTable.getValueAt(selectedRow, 0).toString()));
 		
 		try {
 			Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(inquiriesTable.getValueAt(selectedRow, 1).toString());
 			inquiriesFiltersPanelOrderDateSpinner.setValue(d);
 		}
 		catch (Exception ex) {
 			
 		}
 		
 		inquiriesFiltersPanelOrderOperatorComboBox.setSelectedIndex(cboDbManager.getOperatorStringArrayIdByOperatorDbId(
 				Integer.parseInt(((InquiriesTableTableModel)inquiriesTable.getModel()).CELLS[selectedRow][4])
 		));
 		
 		double totalPrice = 0.00;
 		
 		try {
 			
 			totalPrice = Double.parseDouble(inquiriesTable.getValueAt(selectedRow, 3).toString());
 		}
 		catch (Exception ex) {			
 		}		
		
 		inquiriesFiltersPanelOrderTotalPriceSpinner.setValue(totalPrice);
 	}
 	
 }
