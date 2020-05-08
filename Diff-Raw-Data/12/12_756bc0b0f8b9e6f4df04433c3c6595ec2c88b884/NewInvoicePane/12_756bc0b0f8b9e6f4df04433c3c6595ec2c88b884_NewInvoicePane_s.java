 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.DefaultTableModel;
 
 import org.apache.log4j.Logger;
 
 import services.InvoiceService;
 import services.ProductService;
 
 import entities.Invoice;
 import entities.Product;
 
 
 public class NewInvoicePane extends BasePane{
 
 	private static final long serialVersionUID = 4539993258343834799L;
 	private Logger logger = Logger.getLogger("gui.NewInvoicePane.class");
 	private InvoiceService is;
 	private ProductService ps;
 	private JButton newInvoice;
 	private JButton addProduct;
 	private JButton closeInvoice;
 	private JButton search;
 	private JComboBox openInvoices;
 	private JLabel pnr;
 	private JLabel pname;
 	private JLabel qty;
 	private JTextField pnrField;
 	private JTextField pnameField;
 	private JTextField qtyField;
 	private DefaultTableModel productTableModel;
 	private JScrollPane resultTablePane;
 	private JTable results;
 	private List<Invoice> openInvoiceList = new LinkedList<Invoice>(); 
 	
 	
 	public NewInvoicePane(InvoiceService is, ProductService ps){
 		super();
 		this.is = is;
 		this.ps = ps;
 		
 		init();
 	}
 
 	private void init() {
 		createButtons();
 		createDropDown();
 		createLabels();
 		createTextFields();
 		initialiseTableModel();
 		createResultPane();
 		addEverythingToInterface();
 	}
 	
 	public NewInvoicePane(InvoiceService is, ProductService ps, List<Invoice> openInvoices){
 		super();
 		if(openInvoices != null){
 			this.openInvoiceList = openInvoices;
 		}
 		this.is = is;
 		this.ps = ps;
 		
 		init();
 	}
 
 	private void createButtons() {
 		logger.info("Creating Buttons");
 		newInvoice = new JButton("<html>Neue<br>Rechnung</html>");
 		newInvoice.addActionListener(new newInvoiceListener());
 		addProduct = new JButton("Hinzufügen");
 		addProduct.addActionListener(new addProductListener());
 		closeInvoice = new JButton("<html>Rechnung<br>abschließen<html>");
 		closeInvoice.addActionListener(new closeInvoiceListener());
 		search = new JButton("Suchen");
 		search.addActionListener(new searchListener());
 	}
 	
 	private void createDropDown(){
 		logger.info("Creating Dropdown");
 		openInvoices = new JComboBox();
 	}
 
 	private void createLabels() {
 		logger.info("Creating Labels");
 		pnr = new JLabel("Artikelnummer: ");
 		pname = new JLabel("Artikelbezeichnung: ");
 		qty = new JLabel("Anzahl: ");
 		
 	}
 
 	private void createTextFields() {
 		logger.info("Creating TextFields");
 		pnrField = new JTextField();
 		pnameField = new JTextField();
 		qtyField = new JTextField();
 		
 	}
 	
 	private void initialiseTableModel(){
 		logger.info("initialising TableModel");
 		createUneditableTableModel();
 		setColumnNames();
 	}
 
 	@SuppressWarnings("serial")
 	private void createUneditableTableModel() {
 		productTableModel = new DefaultTableModel(){
 			@Override
 			public boolean isCellEditable(int row, int column) {
 				return false;
 			}
 		};
 	}
 
 	private void setColumnNames() {
 		Object[] columnNames = new Object[3];
 		columnNames[0] = "Waren ID";
 		columnNames[1] = "Bezeichnung";
 		columnNames[2] = "Preis";
 		productTableModel.setColumnIdentifiers(columnNames);
 	}
 
 	private void createResultPane() {
 		logger.info("Creating ResultPane");
 		results = new JTable(productTableModel);
 		resultTablePane = new JScrollPane(results);
 		
 	}
 
 	private void addEverythingToInterface() {
 		logger.info("Adding everything to the Interface");
 		JPanel wf = super.westField;
 		JPanel eb = super.eastButtons;
 		
 		eb.add(newInvoice,"wrap, grow");
 		eb.add(search,"wrap, gapbottom 730, grow");
 		eb.add(closeInvoice,"wrap, south");
 		eb.add(addProduct,"south");
 		
 		wf.add(openInvoices,"wrap, wmin 300, span 2");
 		
 		wf.add(pnr);
 		wf.add(pnrField,"wrap, w 100");
 		wf.add(pname);
 		wf.add(pnameField,"wrap,w 500");
 		
 		wf.add(resultTablePane, "span 2, grow");
 		
 		wf.add(qty,"cell 1 4,split , right");
 		wf.add(qtyField,"right, w 100");
 	}
 	
 	public void addInvoiceToOpenInvoices(String invoiceName){
 		logger.info("Adding to open Invoices");
 		openInvoices.addItem(invoiceName);
 	}
 	
 	
 	public void updateResultsOfProductSearch(List<Product> products){
 		logger.info("Updating Result Table");
 		resetTableModel();
 		if(products == null || products.isEmpty()){}
 		else{
 			fillTableWithNewEntries(products);
 		}
 		updateDisplayWithNewData();
 	}
 
 	private void resetTableModel() {
 		productTableModel.getDataVector().removeAllElements();
 	}
 
 	private void fillTableWithNewEntries(List<Product> products) {
 		Object[] newRow = new Object[3];
 		Iterator<Product> productIterator = products.iterator();
 		while(productIterator.hasNext()){
 			Product p = productIterator.next();
 			newRow[0] = p.getId(); 
 			newRow[1] = p.getLabel();
 			newRow[2] = p.getRetailPrice();
 			productTableModel.addRow(newRow);
 		}
 	}
 	
 	private void updateDisplayWithNewData() {
 		results.revalidate();
 	}	
 	
 	
 	private class newInvoiceListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			logger.info("Creating new Invoice");
 			Invoice i = is.generateNewInvoice();
 			openInvoiceList.add(i);
 			addInvoiceToOpenInvoices(""+i.getId());
 		}
 		
 	}
 	
 	private class addProductListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			logger.info("Adding product to invoice");
 			int iid = Integer.parseInt((String)openInvoices.getSelectedItem());
 			int pid = (Integer) results.getValueAt(results.getSelectedRow(), 1);
 			String qtyString = qtyField.getText();
 			int qty = 0;
 			try{
 				qty = Integer.parseInt(qtyString);
 				if(qty < 1){throw new Exception();}
 			}
 			catch(Exception e1){
 				logger.warn("Someone just tryed adding a product to a bill with NAN or Qty <1");
				//TODO inform user
 				return;
 			}
 			
 			is.addProductToInvoice(pid, iid, qty);
 		}
 		
 	}
 	
 	private class closeInvoiceListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			logger.info("Closing invoice");
 			is.closeInvoice((Integer)openInvoices.getSelectedItem());
 			
 		}
 		
 	}
 	
 	private class searchListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String pidString = pnrField.getText();
 			String labelString = pnameField.getText();
 			if(pidString.isEmpty() && labelString.isEmpty()){
 				List<Product>  all = ps.getAllProducts();
 				updateResultsOfProductSearch(all);
 			}
 			if(pidString.isEmpty() && !labelString.isEmpty()){
 				List<Product> found = ps.getProductsbyLabel(labelString);
 				updateResultsOfProductSearch(found);
 			}
 			if(!pidString.isEmpty() && labelString.isEmpty()){
 				int id = 0;
 				try{
 					Integer.parseInt(pidString);
 					if(id < 1){throw new Exception();}
 				}
 				catch(Exception e1){
					//TODO inform user
 					logger.warn("Invalid transform for id");
 					return;
 				}
 				List<Product> result = new LinkedList<Product>();
 				result.add(ps.getProductbyId(id));
 				updateResultsOfProductSearch(result);
 			}
 			else{
				//TODO Tell user that he can enter either or
 			}
 		}
 		
 	}
 
 }
