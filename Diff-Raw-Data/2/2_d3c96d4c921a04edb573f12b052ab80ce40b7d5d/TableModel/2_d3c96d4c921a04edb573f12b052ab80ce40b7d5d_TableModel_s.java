 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2011 The aidGer Team
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.aidger.view.models;
 
 import de.aidger.model.AbstractModel;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import javax.swing.table.AbstractTableModel;
 
 /**
  * The class represents the abstract table model.
  *
  * @author aidGer Team
  */
 public abstract class TableModel extends AbstractTableModel implements Observer {
 
     /**
      * Array containing the names of all columns
      */
     private String[] columnNames;
 
     /**
      * The static map of models. Each data type has its own list of models.
      */
     protected static Map<String, List<AbstractModel>> mapModels = new HashMap<String, List<AbstractModel>>();
 
     /**
      * The data type specific models that are displayed on the table.
      */
     private List<AbstractModel> models;
 
     /**
      * The model before it was edited.
      */
     private AbstractModel modelBeforeEdit;
 
     /**
      * Constructs the tablemodel.
      *
      * @param columnNames
      *              The names of all columns
      */
     protected TableModel(String[] columnNames) {
         this.columnNames = columnNames;
 
         String className = this.getClass().getName();
 
         // get all models just once from database
        if (mapModels.get(className) == null) {
             models = new ArrayList<AbstractModel>();
             mapModels.put(className, models);
 
             models = getModels();
         } else {
             models = mapModels.get(className);
         }
 
         fireTableDataChanged();
     }
 
     /**
      * Get the value of the row for a specified model.
      *
      * @param model
      *          The model for wich the row value is needed
      * @param row
      *          The index of the row
      * @return The value of the row
      */
     protected abstract Object getRowValue(AbstractModel model, int row);
 
     /**
      * Get the model with the specified index.
      *
      * @param idx
      *          The index of the model
      * @return The model or null
      */
     protected abstract AbstractModel getModelFromDB(int idx);
 
     /**
      * Get a list containing all models.
      *
      * @return A list containing all models
      */
     protected abstract List<AbstractModel> getModels();
 
     /**
      * Returns the model at the given index.
      *
      * @return the model at the given index
      */
     public AbstractModel getModel(int i) {
         return models.get(i);
     }
 
     /**
      * Sets the model before it was edited.
      *
      * @param m
      *            the model before it was edited
      */
     public void setModelBeforeEdit(AbstractModel m) {
         modelBeforeEdit = m;
     }
 
     /**
      * Returns the model before it was edited.
      *
      * @return the model before it was edited
      */
     public AbstractModel getModelBeforeEdit() {
         return modelBeforeEdit;
     }
 
     /**
      * Return the count of columns.
      *
      * @return The count of columns
      */
     public int getColumnCount() {
         return columnNames.length;
     }
 
     /**
      * Return the name of the column.
      *
      * @param column
      *          The index of the column
      * @return The name of the column
      */
     @Override
     public String getColumnName(int column) {
         return columnNames[column];
     }
 
     /**
      * Return the value at a specific position.
      *
      * @param col
      *          The column of the data
      * @param row
      *          The row of the data
      * @return The value at the position
      */
     public Object getValueAt(int col, int row) {
         return getRowValue(models.get(col), row);
     }
 
     /**
      * (non-Javadoc)
      *
      * @see javax.swing.table.AbstractTableModel#getRowCount()
      */
     public int getRowCount() {
         return models.size();
     }
 
     /*
      * (non-Javadoc)
      *
      * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
      */
     public void update(Observable m, Object arg) {
         AbstractModel model = (AbstractModel) m;
         Boolean save = (Boolean) arg;
         int index = indexOf(modelBeforeEdit);
 
         if (save && index >= 0) { // the model was saved
             models.set(index, model);
             fireTableRowsUpdated(index, index);
         } else if (save) { // the model was newly created
             models.add(model);
             index = models.indexOf(model);
             System.out.println("Size: " + models.size() + " Index: " + index);
             fireTableDataChanged();
         } else { // the model was removed
             models.remove(index);
             fireTableRowsDeleted(index, index);
         }
     }
 
     private int indexOf(AbstractModel m) {
         if (m == null) {
             return -1;
         }
 
         for (int i = 0; i < models.size(); ++i) {
             if (models.get(i).equals(m)) {
                 return i;
             }
         }
 
         return -1;
     }
 
 }
