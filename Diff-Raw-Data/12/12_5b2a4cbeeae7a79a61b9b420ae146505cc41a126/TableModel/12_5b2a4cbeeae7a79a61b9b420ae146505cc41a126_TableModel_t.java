 /*
  * This file is part of the aidGer project.
  *
  * Copyright (C) 2010-2013 The aidGer Team
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
 import de.aidger.model.Observer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
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
         if (mapModels.get(className) == null || mapModels.get(className).isEmpty()) {
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
     
     /**
      * Removes a model from the tableModel identified by its id.
      * 
      * @param id 
      *          The id of the model to be removed.
      */
     public void removeModelById(long id) {
         for (int i = 0; i < models.size(); ++i) {
             if (models.get(i).getId() == id) {
                 models.remove(i);
             }
         }
     }
     
     /**
      * Adds a model to the tableModel. Does not add if a model with the model's id already exists in the tableModel.
      * 
      * @param model
      *          The model to be added to the tableModel.
      */
     public void addModel(AbstractModel model) {
         int index = indexOf(model);
         
         if(index == -1) {
             models.add(model);
         }
         
         fireTableDataChanged();
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.model.Observer#onSave()
      */
     public void onSave(AbstractModel model) {
         int index = indexOf(model);
 
         if (index >= 0) { // the model was saved
             models.set(index, model);            
         } else { // the model was newly created
             models.add(model);       
         }
         
         fireTableDataChanged();
     }
 
     
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.model.Observer#onRemove()
      */
     public void onRemove(AbstractModel model) {
         int index = indexOf(model);
         
         if (index == -1) {
             index = models.indexOf(model);
         }
         models.remove(index);
         
         fireTableDataChanged();
     }
 
     /**
      * Determines the index of the specified model in the list of models of this tableModel.
      * 
      * @param m
      *          The model of which the index is sought.
      * @return
      *          The index of the model. -1 if model is not found or null.
      */
     private int indexOf(AbstractModel m) {
         if (m == null) {
             return -1;
         }
 
         for (int i = 0; i < models.size(); ++i) {
            if (models.get(i).getId().equals(m.getId())) {
                 return i;
             }
         }
 
         return -1;
     }
 
 }
