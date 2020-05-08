 package org.rescore.gui.models;
 
 import javax.swing.table.AbstractTableModel;
 import java.util.List;
 
 import org.rescore.dao.DAOFactory;
 import org.rescore.dao.YachtDAO;
 import org.rescore.domain.*;
 import org.rescore.persitence.HibernateUtil;
 
 /** Holds data for the Swing JTable of yachts */
 
 public class YachtTableModel extends AbstractTableModel {
 
     private String[] columnNames = { "Burės nr.", "Metai", "Kapitonas", "Savininkas" };
     private Object[][] data = getTableData();
 
    /** 
     * Utility method
     * converts a list of yachts to an array of arrays
     */
     private static Object[][] getTableData(){
     	HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
     	DAOFactory factory = DAOFactory.instance(DAOFactory.HIBERNATE);
     	YachtDAO yachtDAO = factory.getYachtDAO();
     	
     	List<Yacht> yachts = yachtDAO.findAll();
     	
         Object[][] data = new Object[yachts.size()][];
         int i = 0;
         for(Yacht yacht : yachts){
             data[i] = new Object[] { yacht.getSailNumber(), yacht.getBuildYear(), yacht.getCaptain(), yacht.getOwner() };
             i++;
         }
         return data;
     }
 
     public int getColumnCount() {
         return columnNames.length;
     }
 
     public int getRowCount() {
         return data.length;
     }
 
     public String getColumnName(int col) {
         return columnNames[col];
     }
 
     public Object getValueAt(int row, int col) {
         return data[row][col];
     }
 
     public Class getColumnClass(int c) {
    	Object value = getValueAt(0, c);
    	return (value==null?Object.class:value.getClass());  
     }
 
 }
