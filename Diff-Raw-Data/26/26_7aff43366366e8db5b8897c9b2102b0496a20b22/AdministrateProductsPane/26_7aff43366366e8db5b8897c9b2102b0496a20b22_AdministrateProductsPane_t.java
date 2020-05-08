 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
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
 
 import org.apache.log4j.Logger;
 
 import services.ProductService;
 import services.ProductServiceException;
 import services.StatisticService;
 
 import entities.Product;
 
 public class AdministrateProductsPane extends BasePane {
 
 	private static final long serialVersionUID = -5874966514832873507L;
 	private Logger logger = Logger.getLogger("gui.AdministrateProductsPane.class");
 	private ProductService ps;
 	private StatisticService stats;
 	private JButton newProduct;
 	private JButton search;
 	private JButton increasePriceOfTopsellers;
 	private JButton showTopsellers;
 	private JComboBox purchaseRelation;
 	private JComboBox retailRelation;
 	private JLabel pnr;
 	private JLabel pname;
 	private JLabel supplier;
 	private JLabel pprice;
 	private JLabel rprice;
 	private JTextField pnrField;
 	private JTextField pnameField;
 	private JTextField supplierField;
 	private JTextField ppriceField;
 	private JTextField rpriceField;
 	private DefaultTableModel productTableModel;
 	private JScrollPane resultTablePane;
 	private JTable results;
 	private TableRowSorter<TableModel> sorter;
 	
 	public AdministrateProductsPane(ProductService ps, StatisticService stats){
 		super();
 		this.ps = ps;
 		this.stats = stats;
 		
 		createButtons();
 		createDropDowns();
 		createLabels();
 		createTextFields();
 		initialiseTableModel();
 		createResultPane();
 		setDoubleClickDetectionOnTable();
 		addEverythingToInterface();
 		
 	}
 
 	private void createButtons() {
 		logger.info("Creating Buttons");
 		newProduct = new JButton("<html>Neuer<br>Artikel</html>");
 		newProduct.addActionListener(new newProductListener());
 		search = new JButton("Suchen");
 		search.addActionListener(new searchListener());
 		increasePriceOfTopsellers = new JButton("Preis der Top 3 um 5% erhöhen");
 		increasePriceOfTopsellers.addActionListener(new top3increaserListener());
 		showTopsellers = new JButton("Top 3 der letzten 30 Tage anzeigen");
 		showTopsellers.addActionListener(new top3listListener());
 		
 	}
 
 	private void createDropDowns() {
 		logger.info("Creating DropDowns");
 		String [] relation = {"=","<",">"};
 		purchaseRelation = new JComboBox(relation);
 		retailRelation = new JComboBox(relation);
 	}
 
 	private void createLabels() {
 		logger.info("Creating Labels");
 		pnr = new JLabel("Artikelnummer: ");
 		pname = new JLabel("Artikelbezeichnung: ");
 		supplier = new JLabel("Lieferant: ");
 		pprice = new JLabel("Einkaufspreis: ");
 		rprice = new JLabel("Verkaufspreis: ");
 		
 	}
 
 	private void createTextFields() {
 		logger.info("Creating TextFields");
 		pnrField = new JTextField();
 		pnameField = new JTextField();
 		supplierField = new JTextField();
 		ppriceField = new JTextField();
 		rpriceField = new JTextField();
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
 		Object[] columnNames = new Object[5];
 		columnNames[0] = "Waren ID";
 		columnNames[1] = "Bezeichnung";
 		columnNames[2] = "Preis - Verkauf";
 		columnNames[3] = "Preis - Einkauf";
 		columnNames[4] = "Lieferant";
 		
 		productTableModel.setColumnIdentifiers(columnNames);
 	}
 
 	private void createResultPane() {
 		logger.info("Creating ResultPane");
 		results = new JTable(productTableModel);
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
 		        	showPopupForProduct(id);
 		        }
 		    }
 		});
 	}
 	
 	private void showPopupForProduct(int id) {
 		ProductDetailsPopup productDetails = new ProductDetailsPopup(ps);
 		productDetails.forProduct(id);
 		JDialog j = new JDialog();
 		j.add(productDetails);
 		j.pack();
 		j.setVisible(true);
 		
 	}
 	
 	private void showPopupForProduct(Product p){
 		ProductDetailsPopup productDetails = new ProductDetailsPopup(ps);
 		productDetails.forProduct(p);
 		JDialog j = new JDialog();
 		j.add(productDetails);
 		j.pack();
 		j.setVisible(true);
 	}
 
 	private void addEverythingToInterface() {
 		logger.info("Adding everything together");
 		JPanel wf = super.westField;
 		JPanel eb = super.eastButtons;
 		
 		eb.add(newProduct,"wrap, grow");
 		eb.add(search,"span 2 3, grow");
 		
 		wf.add(pnr);
 		wf.add(pnrField,"w 100, wrap");
 		wf.add(pname);
 		wf.add(pnameField,"w 300");
 		wf.add(pprice);
 		wf.add(purchaseRelation);
 		wf.add(ppriceField,"w 100, wrap");
 		wf.add(supplier);
 		wf.add(supplierField,"w 300");
 		wf.add(rprice);
 		wf.add(retailRelation);
 		wf.add(rpriceField,"w 100, wrap");
 		wf.add(resultTablePane,"span 5,grow, wrap");
 		wf.add(showTopsellers,"span 2,grow");
 		wf.add(increasePriceOfTopsellers,"span 3, grow");
 		
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
 		logger.debug("Resetting table model");
 		productTableModel.getDataVector().removeAllElements();
 		results.getRowSorter().allRowsChanged();
 	}
 
 	private void fillTableWithNewEntries(List<Product> products) {
 		logger.debug("Filling tabel with new entries");
 		Object[] newRow = new Object[5];
 		Iterator<Product> productIterator = products.iterator();
 		
 		while(productIterator.hasNext()){
 			logger.debug("Entering Loop");
 			Product p = productIterator.next();
 			newRow[0] = p.getId(); 
 			newRow[1] = p.getLabel();
 			newRow[2] = p.getRetailPrice();
 			newRow[3] = p.getPurchasePrice();
 			newRow[4] = p.getSupplier();
 			logger.debug("Will now add Row");
 			productTableModel.addRow(newRow);
 		}
 	}
 	
 	private void updateDisplayWithNewData() {
 		logger.debug("Updateing display with new data");
 		results.revalidate();
 	}
 	
 
 	
 	private class newProductListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			Product p;
 			try {
 				p = ps.generateNewProduct();
 			} catch (ProductServiceException e1) {
 				logger.error("Could not create Product");
 				JOptionPane.showMessageDialog(null, "Scheinbar gibt es ein Datenbank Problem. Bitte mal nen Techniker holen");
 				return;
 			}
 			showPopupForProduct(p);
 		}
 		
 	}
 	
 	private class top3listListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			updateResultsOfProductSearch(stats.getTopThreeProductsOfLastThirtyDays());
 		}
 		
 	}
 	
 	private class top3increaserListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			List<Product> l = stats.getTopThreeProductsOfLastThirtyDays();
 			try {
 				ps.increasePriceByFivePercent(l);
 			} catch (ProductServiceException e1) {
 				logger.error("Could not increase price of Products");
 				JOptionPane.showMessageDialog(null, "Scheinbar gibt es ein Datenbank Problem. Bitte mal nen Techniker holen");
 			}
 		}
 		
 	}
 
 	private class searchListener implements ActionListener{
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			filterTable();
 		}
 		
 		private void filterTable() {
 			RowFilter<? super TableModel, Object> rf = createFilter();
 			sorter.setRowFilter(null);
 			fillTableWithAllProducts();
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
 			filters.add(createLabelFilter());
 			filters.add(createSupplierFilter());
 			filters.add(createRPriceFilter());
 			filters.add(createPPriceFilter());
 			
 			filters.removeAll(Collections.singletonList(null));
 			
 			return filters;
 			
 		}
 		private RowFilter<? super TableModel, Object> createIdFilter() {
 			try{
				//TODO ask if warning should be given
 				logger.debug("Creating id filter");
 				return RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, Integer.parseInt(pnrField.getText()), 0);
 			}
 			catch(Exception e){}
 			return null;
 		}
 		private RowFilter<? super TableModel, Object> createLabelFilter() {
 			try{
 				logger.debug("Creating label filter");
 				return RowFilter.regexFilter(pnameField.getText(), 1);
 			}
 			catch(Exception e){}
 			return null;
 		}
 		private RowFilter<? super TableModel, Object> createSupplierFilter() {
 			try{
 				logger.debug("Creating supplier filter");
 				return RowFilter.regexFilter(supplierField.getText(), 4);
 			}
 			catch(Exception e){}
 			return null;
 		}
 		
 		@SuppressWarnings("static-access")
 		private RowFilter<? super TableModel, Object> createRPriceFilter() {
 			try{
 				logger.debug("Creating RPrice filter");
 				RowFilter.ComparisonType ct = null;
 				int v = retailRelation.getSelectedIndex();
 				if(v == 0){ct = ct.EQUAL;}
 				if(v == 1){ct = ct.BEFORE;}
 				if(v == 2){ct = ct.AFTER;}
 				return RowFilter.numberFilter(ct, Double.parseDouble(rpriceField.getText()), 2);
 			}
 			catch(Exception e){}
 			return null;
 		}
 		
 		@SuppressWarnings("static-access")
 		private RowFilter<? super TableModel, Object> createPPriceFilter() {
 			try{
 				logger.debug("Creating PPrice filter");
 				RowFilter.ComparisonType ct = null;
 				int v = purchaseRelation.getSelectedIndex();
 				if(v == 0){ct = ct.EQUAL;}
 				if(v == 1){ct = ct.BEFORE;}
 				if(v == 2){ct = ct.AFTER;}
 				return RowFilter.numberFilter(ct, Double.parseDouble(ppriceField.getText()), 3);
 			}
 			catch(Exception e){}
 			return null;
 		}
 		
 		private void fillTableWithAllProducts() {
 			List<Product> result = ps.getAllProducts();
 			updateResultsOfProductSearch(result);
 		}
 		
 	}
 
 }
