 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui.utilities.table.invoicetable;
 
 import database.extra.InvoiceItem;
 import java.util.ArrayList;
 import javax.swing.table.AbstractTableModel;
 
 /**
  *
  * @author Warkst
  */
 public class InvoiceItemTableModel extends AbstractTableModel {
 
     private final ArrayList<InvoiceItem> data;
     private String pricecode;
     
 
     public InvoiceItemTableModel(ArrayList<InvoiceItem> data, String pricecode) {
         this.data = data;
         this.pricecode = pricecode;
     }
 
     @Override
     public int getRowCount() {
         return data.size();
     }
 
     @Override
     public int getColumnCount() {
         return 5;
     }
 
     @Override
     public String getColumnName(int column) {
         switch (column) {
             case 0:
                 return "Artikel";
             case 1:
                 return "BTW";
             case 2:
                 return "Hoeveelheid";
             case 3:
                 return "Prijs";
             case 4:
                 return "Totaal";
             default:
                 return "ERROR";
         }
     }
 
     @Override
     public boolean isCellEditable(int arg0, int arg1) {
         return false;
     }
 
     public void addComponent(InvoiceItem item) {
         fireTableRowsInserted(0, data.size()-1);
     }
     
     public void updatePricecode(String pricecode){
         this.pricecode = pricecode;
         fireTableDataChanged();
     }
    
    public String priceCode(){
	return pricecode;
    }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         InvoiceItem item = (InvoiceItem) data.toArray()[rowIndex];
         double price = item.getArticle().getPriceForCode(pricecode);
         switch (columnIndex) {
             case 0:
                 return item.getArticle().getName();
             case 1:
                 return item.getArticle().getTaxes();
             case 2:
                 
                 return item.getAmount();
             case 3:
                 return price;
             case 4:
//                return price * item.getAmount() * (1 + (item.getArticle().getTaxes() / 100));
                return price * item.getAmount();
 
             default:
                 return "Error";
         }
     }
 }
