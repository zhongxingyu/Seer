 /**
  * GeoTools Example
  * 
  *  (C) 2011 LISAsoft
  *  
  *  This library is free software; you can redistribute it and/or modify it under
  *  the terms of the GNU Lesser General Public License as published by the Free
  *  Software Foundation; version 2.1 of the License.
  *  
  *  This library is distributed in the hope that it will be useful, but WITHOUT
  *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  */
 package com.lisasoft.face.table;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.SwingWorker;
 import javax.swing.table.AbstractTableModel;
 
 import com.lisasoft.face.data.FaceImpl;
 import com.lisasoft.face.map.MapComponentImpl;
 
 /**
  * This is a FaceTableModel designed to work in collaboration with FaceTable in order to
  * allow tabular selection of Faces.
  * 
  * @author Scott Henderson (LISAsoft)
  */
 public class FaceTableModel extends AbstractTableModel {
 	
 	/** serialVersionUID */
     private static final long serialVersionUID = 3514815125584103L;
 	
     /*String[] columnNames = {"Nummer",
             "Typ",
             "Flchenart",
             "PF",
             "Status",
             "montiert am",
             "Aushang-Periodizitt",
             "Gebiet",
             "Strasse",
             "Haus-Nr.",
             "West-Ost",
             "Sd-Nord",
             "Drehwinkel",
             "category"};*/
     
     String[] columnNames = {"Identifier",
             "Type",
             "Face Format",
             "Product Face Format",
             "Status",
             "Installed on",
             "Posting Period",
             "Area",
             "Street",
             "House Number",
             "West-Ost",
             "Sd-Nord",
             "Angle",
             "Category"};
     
     List<Object[]> cache = new ArrayList<Object[]>();
 	
 	public IOException exception;
 	
 	/**
      * A worker class to get the attributes of each feature and load 
      * them into the {@code TableModel}. The work is performed on a
      * background thread.
      */
     class TableWorker extends SwingWorker<List<Object[]>, Object[]> {
         List<FaceImpl> faces;
 
         /**
          * Constructor
          *
          * @param features the feature collection to be loaded into the table
          */
         TableWorker(List<FaceImpl> faces ) { 
             this.faces = faces;  
             System.out.println("in table worker constructor");
         }
 
         /**
          * {@code SwingWorker} method to visit each feature and retrieve
          * its attributes
          */
         public List<Object[]> doInBackground() {
         	System.out.println("in doInBackground");
             List<Object[]> list = new ArrayList<Object[]>();
             
             for(FaceImpl face : faces){
             	
             	ArrayList<Object> row = new ArrayList<Object>();
                 row.add(face.getNummer());
                 row.add(face.getType());
                 row.add(face.getFaceFormat());
                 row.add(face.getProductFormat());
                 row.add(face.getStatus());
                 row.add(face.getInstalled());
                 row.add(face.getPeriod());
 //                row.add(face.getPosting());
                 row.add(face.getArea());
                 row.add(face.getStreet());
                 row.add(face.getNumber());                
                 row.add(face.getWestOstKoordinate());
                 row.add(face.getSuedNordKoordinate());
                 row.add(face.getAngle());
                 row.add(face.getCategory());
                 
                 publish( row.toArray() );
             }
             
             return list;
         }
 
         /**
          * Add a batch of feature data to the table
          *
          * @param chunks batch of feature data
          */
         @Override
         protected void process(List<Object[]> chunks) {            
             int from = cache.size();
             cache.addAll( chunks );
             int to = cache.size();
             System.out.println("Processing: " + from + " to " + to);
             fireTableRowsInserted( from, to );
         }        
     }
 	
 	TableWorker load;
 	
 	List<FaceImpl> faces;
 	
 	MapComponentImpl map;
 	
     /**
      * Constructor
      *
      * @param features the feature collection to load into the table
      */
     public FaceTableModel(MapComponentImpl map){
     	this.map = map;
 		this.faces = map.getFaces();
     	this.load = new TableWorker(faces);
         load.execute();
     }
 
     /**
      * Get the number of columns in the table
      *
      * @return the number of columns
      */
     public int getColumnCount() {
         if( exception != null ){
             return 1;
         }
         return columnNames.length;
     }
     
     public String getColumnName(int col) {
         return columnNames[col];
     }
 
     /**
      * Get the number of rows in the table
      *
      * @return the number of rows
      */
     public int getRowCount() {
         if( exception != null ){
             return 1;
         }
         return cache.size();
     }
     
     /**
      * Cells 10 and 11 are editable
      *
      * @return true if editable
      */
     public boolean isCellEditable(int row, int col){
     	return (col == 10 || col == 11) ? true : false; 
     }
     
     public void setValueAt(Object value, int row, int col) {
         System.out.println("+++++++++++++setValueAt+++++++++++++++");
     	System.out.println("value: " + value + " row: " + row + " col " + col);
     	
     	FaceImpl editface = faces.get(row);
     	System.out.println("Numberf1: " + editface.getNummer() + " Sued: " + editface.getSuedNordKoordinate());
     	
     	String s = String.valueOf(value);
     	
     	if(col == 10){
     		editface.setWestOstKoordinate(new BigDecimal(s));
     	} else if(col == 11){
     		editface.setSuedNordKoordinate(new BigDecimal(s));
     	}
         
         faces.remove(row);
         faces.add(row, editface);
         
         FaceImpl newFace = faces.get(row);
     	System.out.println("New Face nummer: " + newFace.getNummer() + " West: " + newFace.getWestOstKoordinate() + " Sued: " + newFace.getSuedNordKoordinate());
     	
    	cache.clear();
    	load = new TableWorker(faces);
    	load.run();
    	
     	for(FaceImpl f : faces){
     		System.out.println("face: " + f.getNummer() + " West: " + f.getWestOstKoordinate() + " Sued: " + f.getSuedNordKoordinate());
     	}
     	
    	map.setFaces(faces);
    	List<FaceImpl> newSelected = faces.subList(row,row + 1);
    	map.setSelection(newSelected);
    	this.fireTableDataChanged();
     }
 
     /**
      * Get the value of a specified table entry
      *
      * @param rowIndex the row index
      * @param columnIndex the column index
      *
      * @return the table entry
      */
     public Object getValueAt(int rowIndex, int columnIndex) {    	
         if ( rowIndex < cache.size() ){
             Object row[] = cache.get( rowIndex );
             return row[ columnIndex ];
         }
         return null;
     }
 
 }
