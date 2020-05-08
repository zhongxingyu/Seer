 package ee.ut.math.tvt.salessystem.ui.tabs;
 
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import ee.ut.math.tvt.salessystem.domain.data.HistoryItem;
 import ee.ut.math.tvt.salessystem.domain.data.SoldItem;
 import ee.ut.math.tvt.salessystem.ui.model.PurchaseInfoTableModel;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 
 /**
  * Encapsulates everything that has to do with the purchase tab (the tab
  * labelled "History" in the menu).
  */
 public class HistoryTab implements SelectableTab {
 
 	SalesSystemModel model;
 	
 	PurchaseInfoTableModel purchase;
 	
 	JTable soldItemTable;
 	JTable purchasesTable;
 	
     public HistoryTab(SalesSystemModel model) {
         this.model = model;
     }
     
     public Component draw() {
         JPanel panel = new JPanel();
         
         panel.setLayout(new GridBagLayout());
         
         purchase = new PurchaseInfoTableModel();
 
 		purchasesTable = new JTable(model.getPurchaseHistoryTableModel());
 		soldItemTable = new JTable(purchase);
 		
 		JScrollPane purchasesScrollPane = new JScrollPane(purchasesTable);
 		purchasesScrollPane.setBorder(BorderFactory.createTitledBorder("Order History"));
 		
 		JScrollPane soldItemScrollPane = new JScrollPane(soldItemTable);
 		soldItemScrollPane.setBorder(BorderFactory.createTitledBorder("Order details"));
 		
 		purchasesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				PurchaseSelected();
 			}
 		});
 
 		panel.add(purchasesScrollPane, getPurchasesScrollPaneConstraints1());
 		panel.add(soldItemScrollPane, getItemScrollPaneConstraints());
         
         return panel;
     }
     
     private void PurchaseSelected() {
     	purchase.clear();
     	
     	int selected = purchasesTable.getSelectedRow();
     	
     	if(selected == -1) return;
     	
     	HistoryItem cur = model.getPurchaseHistoryTableModel().getTableRows().get(selected);
     	for(SoldItem item : cur.getSoldItems()) {
     		purchase.addItem(item);
     	}
     }
     
     private GridBagConstraints getPurchasesScrollPaneConstraints1() {
 		GridBagConstraints gc = new GridBagConstraints();
 
 		gc.fill = GridBagConstraints.BOTH;
 		gc.weightx = 1.0;
 		gc.weighty = 0.5;
 
 		return gc;
 	}
     
     private GridBagConstraints getItemScrollPaneConstraints() {
 		GridBagConstraints gc = new GridBagConstraints();
 
 		gc.fill = GridBagConstraints.BOTH;
 		gc.gridy = 1;
 		gc.weightx = 1.0;
 		gc.weighty = 0.5;
 
 		return gc;
 	}
 
 	@Override
 	public void onSelected() {
		purchasesTable.setModel(model.getPurchaseHistoryTableModel());
 	}
 }
