 /* gvSIG. Sistema de Informacin Geogrfica de la Generalitat Valenciana
  *
  * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *  Generalitat Valenciana
  *   Conselleria d'Infraestructures i Transport
  *   Av. Blasco Ibez, 50
  *   46010 VALENCIA
  *   SPAIN
  *
  *      +34 963862235
  *   gvsig@gva.es
  *      www.gvsig.gva.es
  *
  *    or
  *
  *   IVER T.I. S.A
  *   Salamanca 50
  *   46005 Valencia
  *   Spain
  *
  *   +34 963163400
  *   dac@iver.es
  */
 package com.iver.cit.gvsig.project.documents.view.snapping.gui;
 
 import java.awt.Component;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListCellRenderer;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.project.documents.view.snapping.ISnapper;
 
 /**
  * @author fjp
  *
  * Necesitamos un sitio donde estn registrados todos los snappers que
  * se pueden usar. ExtensionPoints es el sitio adecuado.
  * Este dilogo recuperar esa lista para que el usuario marque los
  * snappers con los que desea trabajar.
  */
 public class SnapConfig extends JPanel {
 
 	private JCheckBox jChkBoxRefentActive = null;
 	private JTable jListSnappers = null;
 	private JPanel jPanel = null;
 	private JScrollPane jScrollPane = null;
 
 	private ArrayList snappers;
 
 	/**
 	 * @author fjp
 	 * primera columna editable con un check box para habilitar/deshabilitar el snapper
 	 * segunda columna con el smbolo del snapper
 	 * tercera con el tooltip
 	 * cuarta con un botn para configurar el snapper si es necesario.
 	 */
 	class MyTableModel extends AbstractTableModel {
 
 		public ArrayList mySnappers;
 
 		public MyTableModel(ArrayList snappers)
 		{
 			this.mySnappers = snappers;
 		}
 
 		public int getColumnCount() {
 			return 5;
 		}
 
 		public int getRowCount() {
 			return mySnappers.size();
 		}
 
 		public boolean isCellEditable(int rowIndex, int columnIndex) {
 			if (columnIndex == 0 || columnIndex == 3)
 				return true;
 			return false;
 		}
 
 		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 			ISnapper snap = (ISnapper) mySnappers.get(rowIndex);
 			switch (columnIndex)
 			{
 			case 0://CheckBox
 				snap.setEnabled(((Boolean)aValue).booleanValue());
 				break;
 			case 3://Prioridad
 				snap.setPriority(((Integer)aValue).intValue());
 				break;
 			}
 		}
 
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			ISnapper snap = (ISnapper) mySnappers.get(rowIndex);
 			switch (columnIndex)
 			{
 			case 0:
 				return new Boolean(snap.isEnabled());
 			case 1:
 				return snap.getClass().getName();
 			case 2:
 				return snap.getToolTipText();
 			case 3:
 				return new Integer(snap.getPriority());
 			case 4:
 				return new JButton();
 			}
 			return null;
 		}
 
 		public Class getColumnClass(int columnIndex) {
 			switch (columnIndex)
 			{
 			case 0:
 				return Boolean.class;
 			case 1:
 				return String.class;
 			case 2:
 				return String.class;
 			case 3:
 				return Integer.class;
 			case 4:
 				return JButton.class;
 			}
 			return null;
 		}
 
 		public String getColumnName(int columnIndex) {
 			switch (columnIndex){
 			case 0:
 				return PluginServices.getText(this,"aplicar");
 			case 1:
 				return PluginServices.getText(this,"simbolo");
 			case 2:
 				return PluginServices.getText(this,"tipo");
 			case 3:
 				return PluginServices.getText(this,"prioridad");
 			case 4:
 				return PluginServices.getText(this,"propiedades");
 			}
 			return null;
 		}
 
 	}
 
 	 class MyCellRenderer extends JCheckBox implements ListCellRenderer {
 
 	     // This is the only method defined by ListCellRenderer.
 	     // We just reconfigure the JLabel each time we're called.
 
 	     public Component getListCellRendererComponent(
 	       JList list,
 	       Object value,            // value to display
 	       int index,               // cell index
 	       boolean isSelected,      // is the cell selected
 	       boolean cellHasFocus)    // the list and the cell have the focus
 	     {
 	    	 ISnapper snapper = (ISnapper) value;
 	         String s = snapper.getToolTipText();
 	         setText(s);
 
 	   	   if (isSelected) {
 	             setBackground(list.getSelectionBackground());
 		       setForeground(list.getSelectionForeground());
 		   }
 	         else {
 		       setBackground(list.getBackground());
 		       setForeground(list.getForeground());
 		   }
 		   setEnabled(list.isEnabled());
 		   setFont(list.getFont());
 	         setOpaque(true);
 	         return this;
 	     }
 
 		public void doClick() {
 			super.doClick();
 			System.out.println("Click");
 		}
 
 
 	 }
 
 
 	/**
 	 * This method initializes
 	 *
 	 */
 	public SnapConfig() {
 		super();
 		initialize();
 	}
 
 	/**
 	 * This method initializes this
 	 *
 	 */
 	private void initialize() {
         this.setLayout(null);
         this.setSize(new java.awt.Dimension(463,239));
         this.setPreferredSize(new java.awt.Dimension(463,239));
         this.add(getJChkBoxRefentActive(), null);
         this.add(getJPanel(), null);
 
 	}
 
 	/**
 	 * This method initializes jChkBoxRefentActive
 	 *
 	 * @return javax.swing.JCheckBox
 	 */
 	private JCheckBox getJChkBoxRefentActive() {
 		if (jChkBoxRefentActive == null) {
 			jChkBoxRefentActive = new JCheckBox();
			jChkBoxRefentActive.setText("Referencia a Objetos Activada:");
 			jChkBoxRefentActive.setBounds(new java.awt.Rectangle(26,10,418,23));
 		}
 		return jChkBoxRefentActive;
 	}
 
 	/**
 	 * This method initializes jListSnappers
 	 *
 	 * @return javax.swing.JList
 	 */
 	private JTable getJListSnappers() {
 		if (jListSnappers == null) {
 			jListSnappers = new JTable();
 			// jListSnappers.setCellRenderer(new MyCellRenderer());
 		}
 		return jListSnappers;
 	}
 
 	/**
 	 * This method initializes jPanel
 	 *
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJPanel() {
 		if (jPanel == null) {
 			jPanel = new JPanel();
 			jPanel.setLayout(null);
 			jPanel.setBounds(new java.awt.Rectangle(19,40,423,181));
 			jPanel.add(getJScrollPane(), null);
 		}
 		return jPanel;
 	}
 
 	/**
 	 * This method initializes jScrollPane
 	 *
 	 * @return javax.swing.JScrollPane
 	 */
 	private JScrollPane getJScrollPane() {
 		if (jScrollPane == null) {
 			jScrollPane = new JScrollPane();
 			jScrollPane.setBounds(new java.awt.Rectangle(9,9,402,163));
 			jScrollPane.setViewportView(getJListSnappers());
 		}
 		return jScrollPane;
 	}
 
 	public ArrayList getSnappers() {
 		return snappers;
 	}
 
 	public void setSnappers(ArrayList snappers) {
 		this.snappers = snappers;
 		MyTableModel listModel = new MyTableModel(snappers);
 		getJListSnappers().setModel(listModel);
 		TableColumn tc=getJListSnappers().getColumnModel().getColumn(0);
 		setUpSymbolColumn(getJListSnappers().getColumnModel().getColumn(1));
 		setUpPropertyColumn(getJListSnappers().getColumnModel().getColumn(4));
 		getJListSnappers().setCellSelectionEnabled(false);
 		tc.setMaxWidth(40);
         tc.setMinWidth(20);
 	}
 	public TableModel getTableModel() {
 		return getJListSnappers().getModel();
 	}
 	public boolean applySnappers() {
 		return getJChkBoxRefentActive().isSelected();
 	}
 
 	public void selectSnappers(TreeMap selected) {
 		for (int i=0;i<snappers.size();i++) {
 			Boolean b=(Boolean)selected.get(snappers.get(i));
 			if (b!=null)
 				getTableModel().setValueAt(b,i,0);
 			else
 				getTableModel().setValueAt(new Boolean(false),i,0);
 		}
 
 	}
 	public void setApplySnappers(boolean applySnappers) {
 		getJChkBoxRefentActive().setSelected(applySnappers);
 	}
 	public void setUpSymbolColumn(TableColumn column) {
 	    DrawSnapCellRenderer symbolCellRenderer = new DrawSnapCellRenderer(snappers);
         column.setCellRenderer(symbolCellRenderer);
     }
 	 public void setUpPropertyColumn(TableColumn column) {
 	        PropertySnapCellEditor propertyeditor = new PropertySnapCellEditor(snappers);
 	        column.setCellEditor(propertyeditor);
 
 	        PropertySnapCellRenderer renderer = new PropertySnapCellRenderer(snappers);
 	        column.setCellRenderer(renderer);
 	    }
 }  //  @jve:decl-index=0:visual-constraint="10,10"
 
 
