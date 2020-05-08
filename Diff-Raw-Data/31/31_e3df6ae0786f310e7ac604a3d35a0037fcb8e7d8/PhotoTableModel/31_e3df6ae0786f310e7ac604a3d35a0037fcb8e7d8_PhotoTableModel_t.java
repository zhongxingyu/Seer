 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.laukvik.photogallery;
 
 import java.util.Vector;
import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableModel;
 
 /**
  *
  * @author morten
  */
 public class PhotoTableModel implements TableModel{
 
    Project project;
     Vector<TableModelListener> listeners;
     
     public PhotoTableModel() {
        this.project = new Project();
         this.listeners = new Vector<TableModelListener>();
     }
     
     @Override
     public int getRowCount() {
        return project.photos.size();
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
        return Photo.class;
     }
 
     @Override
     public boolean isCellEditable(int i, int i1) {
         return false;
     }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex ) {
        return project.photos.get( rowIndex );
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
    
    public void tableChanged(){
        TableModelEvent evt = new TableModelEvent( this );
        for (TableModelListener l : listeners){
            l.tableChanged( evt );
        }
    }

    void setProject(Project project) {
        this.project = project;
        tableChanged();
    }
     
 }
