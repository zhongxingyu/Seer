 package de.aidger.view.models;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.table.DefaultTableModel;
 
 import de.aidger.model.AbstractModel;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 
 /**
  * The class represents the abstract table model.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public abstract class TableModel extends DefaultTableModel implements Observer {
 
     /**
      * The static map of models. Each data type has its own list of models.
      */
     @SuppressWarnings("unchecked")
     protected static Map<String, List<AbstractModel>> mapModels = new HashMap<String, List<AbstractModel>>();
 
     /**
      * The data type specific models that are displayed on the table.
      */
     @SuppressWarnings("unchecked")
     protected List<AbstractModel> models;
 
     /**
      * The model before it was edited.
      */
     @SuppressWarnings("unchecked")
     private AbstractModel modelBeforeEdit;
 
     /**
      * Constructs the table model.
      */
     @SuppressWarnings("unchecked")
     public TableModel(String[] columnNames) {
         setColumnIdentifiers(columnNames);
 
         String className = this.getClass().getName();
 
         // get all models just once from database
         if (mapModels.get(className) == null) {
             models = new ArrayList<AbstractModel>();
 
             mapModels.put(className, models);
 
             getAllModels();
         } else {
             // models are already gotten
             models = mapModels.get(className);
         }
 
         refresh();
     }
 
     /**
      * Converts the model to a row.
      * 
      * @param model
      *            the model that will be converted
      * @return the row that is converted from the model
      */
     @SuppressWarnings("unchecked")
     protected abstract Object[] convertModelToRow(AbstractModel model);
 
     /**
      * Gets all models from database and stores them in the table model.
      */
     protected abstract void getAllModels();
 
     /**
      * Returns the model at the given index.
      * 
      * @return the model at the given index
      */
     @SuppressWarnings("unchecked")
     public AbstractModel getModel(int i) {
         return models.get(i);
     }
 
     /**
      * Sets the model before it was edited.
      * 
      * @param m
      *            the model before it was edited
      */
     @SuppressWarnings("unchecked")
     public void setModelBeforeEdit(AbstractModel m) {
         modelBeforeEdit = m;
     }
 
     /**
      * Returns the model before it was edited.
      * 
      * @return the model before it was edited
      */
     @SuppressWarnings("unchecked")
     public AbstractModel getModelBeforeEdit() {
         return modelBeforeEdit;
     }
 
     /**
      * Refreshes the table.
      */
     @SuppressWarnings("unchecked")
     private void refresh() {
         getDataVector().removeAllElements();
 
         fireTableDataChanged();
 
         for (AbstractModel model : models) {
             // each model is observed by the table model
             model.addObserver(this);
 
             // each model is added as a row to the table
             addRow(convertModelToRow(model));
         }
 
         fireTableDataChanged();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
      */
     @SuppressWarnings("unchecked")
     @Override
     public void update(Observable m, Object arg) {
         AbstractModel model = (AbstractModel) m;
         Boolean save = (Boolean) arg;
 
         try {
             if (save) {
                 // the model was added
                if (models.size() != model.size()) {
                     models.add(model);
                 } else {
                     // the model was edited
                     if (!models.contains(model)) {
                         models.remove(modelBeforeEdit);
 
                         models.add(model);
                     }
                 }
             } else {
                 // the model was removed
                 models.remove(model);
             }
 
             // refresh only the table
             refresh();
         } catch (AdoHiveException e) {
 
         }
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
