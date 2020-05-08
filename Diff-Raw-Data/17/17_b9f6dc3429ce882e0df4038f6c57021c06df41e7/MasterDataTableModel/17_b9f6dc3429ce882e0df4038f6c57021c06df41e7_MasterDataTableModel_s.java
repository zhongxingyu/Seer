 package de.aidger.view.models;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.table.DefaultTableModel;
 
 import de.aidger.model.AbstractModel;
 
 /**
  * The class represents the abstract table model for the master data.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public abstract class MasterDataTableModel extends DefaultTableModel implements
         Observer {
 
     /**
      * The master data that is displayed on the table.
      */
     @SuppressWarnings("unchecked")
     protected List<AbstractModel> masterData = new ArrayList<AbstractModel>();
 
     /**
      * Constructs the abstract master data table model.
      */
     public MasterDataTableModel(String[] columnNames) {
         setColumnIdentifiers(columnNames);
 
         refresh();
     }
 
     /**
      * Refreshs the whole table.
      */
     public void refresh() {
         masterData.clear();
 
         setRowCount(0);
     }
 
     /**
      * Returns the model at the given index.
      * 
      * @return the model at the given index
      */
     @SuppressWarnings("unchecked")
     public AbstractModel getModel(int i) {
         return masterData.get(i);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
      */
     @Override
     public void update(Observable m, Object o) {
         refresh();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
      */
     @Override
     public boolean isCellEditable(int row, int col) {
         return false;
     }
 }
