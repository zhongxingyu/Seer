 /*
  * DBTableModel.java
  *
  * Created on 11 novembre 2006, 9.41
  *
  */
 package it.unina.lbd.lbd2012;
 
 import java.sql.*;
 import javax.swing.table.*;
 
 /**
  * Modello di JTable basate su un ResultSet. <br> Si preferisce basare il
  * modello su un ResultSet, piuttosto che su una query, in modo da poter
  * condividerlo con il DBFrame.
  *
  * @author Massimo
  */
 public class DBTableModel extends AbstractTableModel {
 
    /**
     * Il Resultset su cui si basa il modello.
     */
    private ResultSet rs;
 
    /**
     * Creates a new instance of DBTableModel
     */
    public DBTableModel() {
       super();
    }
 
    public DBTableModel(ResultSet r) {
       super();
       rs = r;
    }
 
    /**
     * Imposta il Resultset su cui si basa il modello.
     */
    public void setRS(ResultSet r) {
       rs = r;
       fireTableStructureChanged();
 
    }
 
    public String getColumnName(int col) {
       col++;
       if (rs == null) {
          return "";
       }
       try {
          return rs.getMetaData().getColumnName(col);
       } catch (SQLException e) {
          System.out.println(e.getMessage());
          return "";
       }
    }
 
    public int getRowCount() {
       if (rs == null) {
          return 0;
       }
       try {
          int currentPosition, last;
         currentPosition = rs.getRow() <= 0 ? rs.getRow() : 1;
          rs.last();
          last = rs.getRow();
          rs.absolute(currentPosition);
          return last;
      } catch (Exception e) {
          System.out.println(e.getMessage());
         return 1;
       }
    }
 
    public int getColumnCount() {
       if (rs == null) {
          return 0;
       }
       try {
          return rs.getMetaData().getColumnCount();
       } catch (/*
                * SQL
                */Exception e) {
          System.out.println(e.getMessage());
          return 0;
       }
    }
 
    public Object getValueAt(int row, int col) {
       int currentPosition;
       Object ob;
       row++;
       col++;
       try {
          currentPosition = rs.getRow();
          rs.absolute(row);
          ob = rs.getObject(col);
          rs.absolute(currentPosition);
          return ob;
       } catch (SQLException e) {
          System.out.println(e.getMessage());
          return null;
       }
    }
 
    @Override
    public boolean isCellEditable(int row, int col) {
       return false;
    }
 
    @Override
    public void setValueAt(Object value, int row, int col) {
    }
 }
