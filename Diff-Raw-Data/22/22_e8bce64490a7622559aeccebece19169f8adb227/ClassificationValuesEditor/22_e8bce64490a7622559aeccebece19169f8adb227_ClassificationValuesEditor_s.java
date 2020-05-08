 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
  Copyright (C) 2001-2007 by:
  Department of Geography, University of Bonn
  http://www.giub.uni-bonn.de/deegree/
  lat/lon GmbH
  http://www.lat-lon.de
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  Contact:
 
  Andreas Poth
  lat/lon GmbH
  Aennchenstr. 19
  53177 Bonn
  Germany
  E-Mail: poth@lat-lon.de
 
  Prof. Dr. Klaus Greve
  Department of Geography
  University of Bonn
  Meckenheimer Allee 166
  53115 Bonn
  Germany
  E-Mail: greve@giub.uni-bonn.de
  ---------------------------------------------------------------------------*/
 
 package org.deegree.igeo.views.swing.style.editor;
 
 import java.awt.Component;
 import java.util.Locale;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.igeo.i18n.Messages;
 import org.deegree.igeo.style.model.classification.Intervallable;
 import org.deegree.igeo.style.model.classification.ValueRange;
 
 /**
  * <code>DoubleValuesEditor</code>
  * 
  * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  * 
  */
 public class ClassificationValuesEditor<U extends Comparable<U>> extends DefaultCellEditor {
 
     private static final long serialVersionUID = -3741066851591726896L;
 
     private static final ILogger LOG = LoggerFactory.getLogger( ClassificationValuesEditor.class );
 
     private ValueRange<U> valueRange;
 
     private Intervallable<U> dummy;
 
     /**
      * @param dummy
     *            the dummy value is required to have access to a Intervallable and methods to
     *            create a new intervallable out of a string or get the invalid intervallable
     *            message after editing a value
      */
     public ClassificationValuesEditor( Intervallable<U> dummy ) {
         super( new JTextField() );
         this.dummy = dummy;
     }

     @Override
     public boolean stopCellEditing() {
         String text = ( (JTextField) getComponent() ).getText();
         try {
            valueRange.setMin( dummy.getAsIntervallable( text ) );
         } catch ( Exception e ) {
             String msg = dummy.getInvalidMessage( text );
             JOptionPane.showMessageDialog( getComponent(), msg, Messages.getMessage( Locale.getDefault(), "$MD10751" ),
                                            JOptionPane.ERROR_MESSAGE );
             LOG.logError( e );
             return false;
         }
 
         return super.stopCellEditing();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column ) {
         valueRange = (ValueRange<U>) value;
         JTextField tf = (JTextField) getComponent();
         tf.setText( valueRange.getEditorLabel() );
         return tf;
 
     }
 
     @Override
     public Object getCellEditorValue() {
         return valueRange;
     }
 
     /**
      * 
      * @return the value range edited by this editor
      */
     public ValueRange<U> getValueRange() {
         return valueRange;
     }
 
     /**
      * @param valueRange
      *            the value range to edit
      */
     public void setValueRange( ValueRange<U> valueRange ) {
         this.valueRange = valueRange;
    }
 
 }
