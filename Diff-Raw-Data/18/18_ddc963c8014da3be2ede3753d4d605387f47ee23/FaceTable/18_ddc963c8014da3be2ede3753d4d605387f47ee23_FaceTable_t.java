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
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
 
 import org.geotools.swing.table.FeatureCollectionTableModel;
 
 import com.lisasoft.face.Listeners.SelectionListener;
 import com.lisasoft.face.data.Face;
 import com.lisasoft.face.map.MapComponentImpl;
 
 /**
  * This is a FaceTable designed to work in collaboration with a MapComponent in order to
  * allow tabular selection of Faces.
  * 
  * @author Jody Garnett (LISAsoft)
  * @author Scott Henderson (LISAsoft)
  */
 public class FaceTable extends JTable {
 
     /** serialVersionUID */
     private static final long serialVersionUID = 3960247151794781103L;
     
     /**
      * This is the MapComponent we are working.
      */
     private MapComponentImpl map;
     
     public FaceTable( MapComponentImpl map ){
     	this.map = map;
         initi();
     }
 
     /**
      * Responsible for adding the table model; table header model and table selection model
      * to work with MapComponentImpl.
      */
     private void initi() {
         // SH: Please configure table here; you can inner classed or break out seperate classes
     	
     	if (map.getFaces() != null) {
             FaceTableModel model = new FaceTableModel(map);
             
             this.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
             this.setPreferredScrollableViewportSize(new Dimension(800, 100));
             this.setModel(model);
             
             SelectionListener listener = new SelectionListener(this, map);
             this.getSelectionModel().addListSelectionListener(listener);
 //            this.getColumnModel().getSelectionModel()
 //                .addListSelectionListener(listener);
             this.map.addMapSelectionListener(listener);
             
         }
     }  
     
 }
