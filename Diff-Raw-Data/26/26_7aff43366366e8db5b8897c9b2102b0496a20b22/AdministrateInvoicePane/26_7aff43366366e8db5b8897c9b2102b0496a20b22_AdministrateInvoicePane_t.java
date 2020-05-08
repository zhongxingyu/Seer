 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.sql.Date;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.RowFilter;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.log4j.Logger;
 
 import services.InvoiceService;
 import services.InvoiceServiceException;
 import services.ProductService;
 
 import entities.Invoice;
 
 public class AdministrateInvoicePane extends BasePane {
 
 	private static final long serialVersionUID = 3523355764154359405L;
 	private Logger logger = Logger.getLogger("gui.AdministrateInvoicePane.class");
 	private InvoiceService is;
 	private ProductService ps;
 	private JButton search;
 	private JLabel inr;
 	private JLabel datefrom;
 	private JLabel datetill;
 	private JLabel waiter;
 	private JLabel dateFormat;
 	private JTextField inrField;
 	private JTextField datefromField;
 	private JTextField datetillField;
 	private JTextField waiterField;
 	private DefaultTableModel invoiceTableModel;
 	private JScrollPane resultTablePane;
 	private JTable results;
 	private TableRowSorter<TableModel> sorter;
 	
 	public AdministrateInvoicePane(InvoiceService is, ProductService ps){
 		super();
 		
 		this.ps =ps;
 		this.is = is;
 		createButtons();
 		createLabels();
 		createTextFields();
 		initialiseTableModel();
 		createResultPane();
 		setDoubleClickDetectionOnTable();
 		addEverythingToInterface();
 		
 	}
 
 	private void createButtons() {
 		search = new JButton("Suchen");
 		search.addActionListener(new searchListener());
 		
 	}
 
 	private void createLabels() {
 		inr = new JLabel("Rechnungsnummer: ");
 		datefrom = new JLabel("Rechnugsdatum von: ");
 		datetill = new JLabel("bis: ");
 		waiter = new JLabel("Kellner: ");
 		dateFormat = new JLabel("(JJJJ-MM-TT)");
 	}
 
 	private void createTextFields() {
 		inrField = new JTextField();
 		datefromField = new JTextField();
 		datetillField = new JTextField();
 		waiterField = new JTextField();
 	}	
 	private void initialiseTableModel(){
 		logger.info("initialising TableModel");
 		createUneditableTableModel();
 		setColumnNames();
 	}
 
 	@SuppressWarnings("serial")
 	private void createUneditableTableModel() {
 		invoiceTableModel = new DefaultTableModel(){
 			@Override
 			public boolean isCellEditable(int row, int column) {
 				return false;
 			}
 		};
 	}
 
 	private void setColumnNames() {
 		Object[] columnNames = new Object[5];
 		columnNames[0] = "Rechnungsnummer";
 		columnNames[1] = "Summe";
 		columnNames[2] = "Datum";
 		columnNames[3] = "Uhrzeit";
 		columnNames[4] = "Kellner";
 		
 		invoiceTableModel.setColumnIdentifiers(columnNames);
 	}
 
 	private void createResultPane() {
 		logger.info("Creating ResultPane");
 		results = new JTable(invoiceTableModel);
 		resultTablePane = new JScrollPane(results);
 		sorter = new TableRowSorter<TableModel>(results.getModel());
 		results.setRowSorter(sorter);
 		
 	}
 	
 	private void setDoubleClickDetectionOnTable(){
 		results.addMouseListener(new MouseAdapter(){
 			public void mouseClicked(MouseEvent e)
 		    {
 		        if (e.getComponent().isEnabled() && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
 		        {
 		        	int id = (Integer) results.getValueAt(results.getSelectedRow(), 0);
 		        	showDialogWithInvoiceDeatils(id);
 		        }
 		    }
 
 
 		});
 	}
 	
 	private void showDialogWithInvoiceDeatils(int id) {
 		InvoiceDetailsPopup invoiceDetails = new InvoiceDetailsPopup(is, ps);
 		invoiceDetails.forInvoice(id);
 		JDialog j = new JDialog();
 		j.add(invoiceDetails);
 		j.pack();
 		j.setVisible(true);
 	}
 
 	private void addEverythingToInterface() {
 		JPanel wf = super.westField;
 		JPanel eb = super.eastButtons;
 		
 		reconfigureButtonPanelLayout();
 		
 		eb.add(search);
 		
 		wf.add(inr);
 		wf.add(inrField, "w 100, wrap");
 		wf.add(datefrom);
 		wf.add(datefromField,"w 100");
 		wf.add(dateFormat);
 		wf.add(datetill);
 		wf.add(datetillField,"w 100, wrap");
 		wf.add(waiter);
 		wf.add(waiterField,"w 300, span 3, wrap");
 		wf.add(resultTablePane,"span 4, grow");
 		
 	}
 
 	private void reconfigureButtonPanelLayout() {
 		super.eastButtons.setLayout(new MigLayout("","grow",":push[]"));
 	}
 	
 	public void updateResultsOfInvoiceSearch(List<Invoice> invoices){
 		logger.info("Updating Result Table");
 		resetTableModel();
 		if(invoices == null || invoices.isEmpty()){}
 		else{
 			fillTableWithNewEntries(invoices);
 		}
 		updateDisplayWithNewData();
 	}
 
 	private void resetTableModel() {
 		invoiceTableModel.getDataVector().removeAllElements();
		results.getRowSorter().allRowsChanged();
 	}
 
 	private void fillTableWithNewEntries(List<Invoice> invoices) {
 		Object[] newRow = new Object[5];
 		Iterator<Invoice> invoiceIterator = invoices.iterator();
 		while(invoiceIterator.hasNext()){
 			Invoice i = invoiceIterator.next();
 			newRow[0] = i.getId(); 
 			newRow[1] = i.getSum();
 			newRow[2] = i.getDate();
 			newRow[3] = i.getTime();
 			newRow[4] = i.getWaiter();
 			invoiceTableModel.addRow(newRow);
 		}
 	}
 	
 	private void updateDisplayWithNewData() {
 		results.revalidate();
 	}
 	
 	protected searchListener getSearchListener(){
 		return new searchListener();
 	}
 	
 	protected class searchListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			String datefrom = datefromField.getText();
 			String datetill = datetillField.getText();
 			try{
 				checkDateFormat(datefrom);
 				checkDateFormat(datetill);
 			}
 			catch(Exception e){
 				logger.warn("User tried invalid date");
 				JOptionPane.showMessageDialog(null, "Bitte überprüfen Sie Ihre Datumseingabe auf korrekte Formatierung.");
 				return;
 			}
 			filterTable();
 		}
 		
 		private void filterTable() {
 			RowFilter<? super TableModel, Object> rf = createFilter();
 			sorter.setRowFilter(null);
 			fillTableWithAllInvoices();
 			sorter.setRowFilter(rf);
 		}
 		
 		private RowFilter<? super TableModel, Object> createFilter() {
 			List<RowFilter<? super TableModel, Object>> filters = new LinkedList<RowFilter<? super TableModel, Object>>();
 			filters = createAndAddSubfilters(filters);
 			if(filters.isEmpty()){return null;}
 			return RowFilter.andFilter(filters);
 		}
 		
 		private List<RowFilter<? super TableModel, Object>> createAndAddSubfilters(
 				List<RowFilter<? super TableModel, Object>> filters) {
 			filters.add(createIdFilter());
 			filters.add(createDateFromFilter());
 			filters.add(createDateTillFilter());
 			filters.add(createWaiterFilter());
 			
 			filters.removeAll(Collections.singletonList(null));
 			
 			return filters;
 			
 		}
 		
 		private RowFilter<? super TableModel, Object> createIdFilter() {
 			try{
				//TODO ask if warning should be given
 				logger.debug("Creating id filter");
 				return RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, Integer.parseInt(inrField.getText()), 0);
 			}
 			catch(Exception e){}
 			return null;
 		}
 
 		private RowFilter<? super TableModel, Object> createDateFromFilter() {
 			try{
 				String date = datefromField.getText();
 				logger.debug("Got Date: "+date);
 				return RowFilter.dateFilter(RowFilter.ComparisonType.AFTER, Date.valueOf(date), 2);
 			}
 			catch(Exception e){
 				logger.debug("Got an exception creating Date Filter "+e.toString());
 			}
 			return null;
 		}
 
 		private RowFilter<? super TableModel, Object> createDateTillFilter() {
 			try{
 				String date = datetillField.getText();
 				logger.debug("Got Date: "+date);
 				return RowFilter.dateFilter(RowFilter.ComparisonType.BEFORE, Date.valueOf(date), 2);
 			}
 			catch(Exception e){
 				logger.debug("Got an exception creating Date Filter "+e.toString());
 			}
 			return null;
 		}
 
 		private RowFilter<? super TableModel, Object> createWaiterFilter() {
 			try{
 				return RowFilter.regexFilter(waiterField.getText(), 4);
 			}
 			catch(Exception e){}
 			return null;
 		}
 		private void fillTableWithAllInvoices() {
 			List<Invoice> result;
 			try {
 				result = is.getAllInvoices();
 				updateResultsOfInvoiceSearch(result);
 			} catch (InvoiceServiceException e) {
 				logger.error("Could not load Invoices From Database");
 			}
 			
 		}
 
 		protected void checkDateFormat(String s) throws IllegalArgumentException{
 			String dateRegex = "((19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01]))|";
 			if(s.matches(dateRegex)){
 				return;
 			}
 			else{
 				throw new IllegalArgumentException();
 			}
 		}
 		
 	}
 
 
 }
