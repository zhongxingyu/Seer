 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * AttributeTable.java
  *
  * Created on 3. Juni 2004, 11:55
  */
 package Sirius.navigator.ui.attributes;
 
 import Sirius.navigator.resource.*;
 import Sirius.navigator.ui.attributes.renderer.*;
 
 import Sirius.server.localserver.attribute.Attribute;
 
 import org.apache.log4j.Logger;
 
 import org.jdesktop.swingx.JXTable;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.net.*;
 
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.table.*;
 
 /**
  * DOCUMENT ME!
  *
  * @author   pascal
  * @version  $Revision$, $Date$
  */
 public class AttributeTable extends JXTable // implements ComplexContainer
 {
 
     //~ Instance fields --------------------------------------------------------
 
     HyperlinkRenderer hyperlinkRenderer = new HyperlinkRenderer();
     private final Logger logger;
     private final HashMap cellEditors;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * protected ComplexContainer complexContainerDelegate;
      */
     public AttributeTable() {
         this(org.openide.util.NbBundle.getMessage(
                 AttributeTable.class,
                 "AttributeTable.AttributeTable().nameColumnName"), // NOI18N
             org.openide.util.NbBundle.getMessage(
                 AttributeTable.class,
                 "AttributeTable.AttributeTable().valueColumnName")); // NOI18N
     }
 
     /**
      * Creates a new AttributeTable object.
      *
      * @param  nameColumnName   DOCUMENT ME!
      * @param  valueColumnName  DOCUMENT ME!
      */
     public AttributeTable(final String nameColumnName, final String valueColumnName) {
         super(new AttributeTableModel(new String[] { nameColumnName, valueColumnName }));
 
         this.logger = Logger.getLogger(this.getClass());
         this.cellEditors = new HashMap();
 
         // XXX
         // this.setDefaultRenderer(Object.class, new HyperlinkRenderer());
         // this.setDefaultEditor(Integer.class, new HyperlinkRenderer());
         // this.setDefaultEditor(Boolean.class, new HyperlinkRenderer());
         // this.setDefaultEditor(Double.class, new HyperlinkRenderer());
         // this.setDefaultEditor(Float.class, new HyperlinkRenderer());
         // this.setDefaultRenderer(String.class, new HyperlinkRenderer());
 
         this.addMouseListener(new CellRendererMouseListener());
 
         if (PropertyManager.getManager().isAdvancedLayout()) {
             this.setTableHeader(null);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  metaAtributes  DOCUMENT ME!
      */
     public void setAttributes(final Collection metaAtributes) {
         this.getAttributeTableModel().setAttributes(metaAtributes);
     }
 
     /**
      * DOCUMENT ME!
      */
     public void clear() {
         // System.out.println("clear attribute table");
         if (this.cellEditor != null) {
             this.cellEditor.cancelCellEditing();
         }
 
         this.getAttributeTableModel().clear();
         this.clearSelection();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     protected AttributeTableModel getAttributeTableModel() {
         return (AttributeTableModel)super.getModel();
     }
 
     @Override
     public TableCellEditor getCellEditor(final int row, final int column) {
         TableCellEditor editor = null;
 
         if (column == 1) {
             editor = this.getAttributeTableModel().getCellEditor(row, column);
 
             if (editor == null) {
                 editor = getDefaultEditor(this.getAttributeTableModel().getClassAt(row, column));
             }
         }
 
         return editor;
     }
 
     @Override
     public TableCellRenderer getCellRenderer(final int row, final int column) {
         final TableCellRenderer renderer = null;
 
         if (column == 1) {
             /*renderer = this.getAttributeTableModel().getCellRenderer(row, column);
              * if(renderer == null) { renderer = getDefaultEditor(this.getAttributeTableModel().getClassAt(row,
              * column));}*/
 
             if (this.getAttributeTableModel().getValueAt(row, column) != null) {
                 final Attribute attribute = ((Attribute)this.getAttributeTableModel().getValueAt(row, column));
                 if (attribute.referencesObject() && (attribute.getValue() != null)) {
                     try {
                         new URL(attribute.getValue().toString());
                         return hyperlinkRenderer;
                     } catch (Throwable exp) {
                         if (logger.isDebugEnabled()) {
                             logger.debug("getCellRenderer() attribute " + attribute.getName() + " is no valid url"); // NOI18N
                         }
                     }
                 }
             }
         }
 
         return super.getCellRenderer(row, column);
     }
 
     /**
      * Getter for property editable.
      *
      * @return  Value of property editable.
      */
     @Override
     public boolean isEditable() {
         return this.getAttributeTableModel().isEditable();
     }
 
     /**
      * Setter for property editable.
      *
      * @param  editable  New value of property editable.
      */
     @Override
     public void setEditable(final boolean editable) {
         this.getAttributeTableModel().setEditable(editable);
 
         if (!editable && (this.cellEditor != null)) {
             this.cellEditor.cancelCellEditing();
         }
     }
 
     @Override
     public String getToolTipText(final MouseEvent e) {
         String tip = null;
         final java.awt.Point p = e.getPoint();
         final int rowIndex = rowAtPoint(p);
         final int colIndex = columnAtPoint(p);
         final int realColumnIndex = convertColumnIndexToModel(colIndex);
 
        final Object value = getValueAt(rowIndex, colIndex);
        if (value != null) {
            tip = value.toString();
         }

        return tip;
     }
 
     // #########################################################################
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * Gibt MouseEvents an Renderer weiter.
      *
      * @version  $Revision$, $Date$
      */
     protected class CellRendererMouseListener extends MouseAdapter {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void mouseClicked(final MouseEvent event) {
             if (logger.isDebugEnabled()) {
                 logger.debug("mouseClick"); // NOI18N
             }
             this.translateMouseEvent(event);
         }
 
         @Override
         public void mousePressed(final MouseEvent event) {
             this.translateMouseEvent(event);
         }
 
         @Override
         public void mouseReleased(final MouseEvent event) {
             this.translateMouseEvent(event);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  event  DOCUMENT ME!
          */
         protected void translateMouseEvent(final MouseEvent event) {
             final Point point = event.getPoint();
             final int column = columnAtPoint(point);
 
             if (column == 1) {
                 final int row = rowAtPoint(point);
                 final TableCellRenderer renderer = getCellRenderer(row, column);
 
                 if (renderer instanceof HyperlinkRenderer) {
                     // Component component = prepareRenderer(renderer, row, column);
                     final Component component = ((HyperlinkRenderer)renderer).getComponent();
                     final Rectangle cellRect = getCellRect(row, column, false);
                     point.translate(-cellRect.x, -cellRect.y);
 
                     // MouseEvent newEvent = new MouseEvent(component, event.getID(), event.getWhen(),
                     // event.getModifiers(), point.x, point.y, event.getClickCount(), event.isPopupTrigger());
                     final MouseEvent newEvent = SwingUtilities.convertMouseEvent(AttributeTable.this, event, component);
                     component.dispatchEvent(newEvent);
                 }
             }
         }
     }
 
     // ComplexContainer implementation
     /*public final boolean addComplexEditor(ComplexEditor complexChildEditor)
      * { this.complexContainerDelegate.addComplexEditor(complexChildEditor); } public final java.util.Map
      * getChildEditors() { return this.complexContainerDelegate.getChildEditors(); } public final java.awt.Component
      * getComponent() { return this.complexContainerDelegate.getComponent(); } public final BasicContainer
      * getParentContainer() { return this.complexContainerDelegate.getParentContainer(); } public final java.lang.Object
      * getValue(java.lang.Object key) { return this.complexContainerDelegate.getValue(key); } public final boolean
      * hideComplexEditorComponentUI(Component complexChildEditorComponent) { return
      * this.complexContainerDelegate.hideComplexEditorComponentUI(complexChildEditorComponent); } public final boolean
      * removeComplexEditor(ComplexEditor complexChildEditor) { return
      * this.complexContainerDelegate.removeComplexEditor(complexChildEditor); } public final void
      * setValue(java.lang.Object key, java.lang.Object value) { this.complexContainerDelegate.setValue(key, value); }
      * public final boolean showComplexEditorComponentUI(Component complexChildEditorComponent) { return
      * this.complexContainerDelegate.showComplexEditorComponentUI(complexChildEditorComponent);}*/
 
     /**
      * Delegate -> keine Mehrfachvererbung! :-(
      */
     /*protected class ComplexTableEditorDelegate extends AbstractComplexEditor
      * {  public java.lang.Object getValue(java.lang.Object key) {     //return AttributeTable.this.g     return null; }
      * public boolean isEditable(java.util.EventObject anEvent) {     AttributeTable.this.isEditable(); }  public void
      * setValue(java.lang.Object key, java.lang.Object value) {  }}*/
 }
