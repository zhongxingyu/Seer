 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.laukvik.photogallery;
 
 import java.util.Vector;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableModel;
 
 /**
  *
  * @author morten
  */
 public class PhotoTableModel implements TableModel{
 
    Vector<Photo> projects;
     Vector<TableModelListener> listeners;
     
     public PhotoTableModel() {
        this.projects = new Vector<Photo>();
         this.listeners = new Vector<TableModelListener>();
     }
     
     @Override
     public int getRowCount() {
        return projects.size();
     }
 
     @Override
     public int getColumnCount() {
         return 1;
     }
 
     @Override
     public String getColumnName(int i) {
         return "Photo";
     }
 
     @Override
     public Class<?> getColumnClass(int i) {
        return Project.class;
     }
 
     @Override
     public boolean isCellEditable(int i, int i1) {
         return false;
     }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex ) {
        return projects.get( rowIndex );
     }
 
     @Override
     public void setValueAt(Object o, int i, int i1) {
         
     }
 
     @Override
     public void addTableModelListener(TableModelListener tl) {
         listeners.add( tl );
     }
 
     @Override
     public void removeTableModelListener(TableModelListener tl) {
         listeners.remove( tl );
    }   
     
 }
