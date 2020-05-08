 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.screen.view.swing;
 
 /**
  * @(#)ScreenField.java   0.00 12-Feb-97 Don Corley
  *
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 import java.awt.Color;
 import java.awt.Component;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JTable;
 import javax.swing.border.LineBorder;
 
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.screen.model.SButtonBox;
 import org.jbundle.base.screen.model.SImageView;
 import org.jbundle.base.screen.model.ScreenField;
 import org.jbundle.model.db.Field;
 import org.jbundle.model.screen.ScreenComponent;
 import org.jbundle.thin.base.db.Converter;
 import org.jbundle.thin.base.screen.util.SerializableImage;
 
 
 /**
 * Implements a standard button.
  */
 public class VButtonBox extends VBaseButton
 {
     /**
      * Constructor.
      */
     public VButtonBox()
     {
         super();
     }
     /**
      * Constructor.
      * @param model The model object for this view object.
      * @param bEditableControl Is this control editable?
      */
     public VButtonBox(ScreenField model, boolean bEditableControl)
     {
         this();
         this.init(model, bEditableControl);
     }
     /**
      * Constructor.
      * @param model The model object for this view object.
      * @param bEditableControl Is this control editable?
      */
     public void init(ScreenComponent model, boolean bEditableControl)
     {
         super.init(model, bEditableControl);
     }
     /**
      * Free.
      */
     public void free()
     {
         if (this.getControl(DBConstants.CONTROL_TO_FREE) != null)
             if (m_bEditableControl)
         {
             this.getControl(DBConstants.CONTROL_TO_FREE).removeFocusListener(this);
             ((JButton)this.getControl(DBConstants.CONTROL_TO_FREE)).removeActionListener(this);
             m_bEditableControl = false;     // Don't remove more listeners in super
         }
         super.free();
     }
     /**
      * Create the physical control.
      * @param bEditableControl Is this control editable?
      * @return The physical control.
      */
     public Component setupControl(boolean bEditableControl)
     {
         String strDesc = ((SButtonBox)this.getScreenField()).getButtonDesc();
         JButton control = null;
         String strImageButton = ((SButtonBox)this.getScreenField()).getImageButtonName();
         if (strImageButton == null)
             control = new JButton(strDesc);   // Physical control
         else if ((strDesc == null) || (strDesc.length() == 0))
             control = new JButton(this.loadImageIcon(strImageButton, null));    // Get this image, then redisplay me when you're done
         else
             control = new JButton(strDesc, this.loadImageIcon(strImageButton, null)); // Get this image, then redisplay me when you're done
         if (this.getScreenInfo() != null)
             if (this.getScreenInfo().isCustomTheme())
                 control.setBorder(new LineBorder(Color.black, 1));
         String strToolTip = ((SButtonBox)this.getScreenField()).getToolTip();
         if (strToolTip != null)
             control.setToolTipText(strToolTip);
         control.setMargin(NO_INSETS);
         if (bEditableControl)
         {
             control.addFocusListener(this);
             control.addActionListener(this);
         }
         if (this.getScreenInfo() != null)
             if (this.getScreenInfo().isMetalLookAndFeel())
                 control.setOpaque(false);   // Transparent looks cool in metalL&F
         return control;
     }
     /**
      * Get the class of this component's state.
      * The state is an object which represents the state of this control.
      * @return The class of the control's value (Boolean).
      */
     public Class<?> getStateClass()
     {
         return Boolean.class; // By default
     }
     /**
      * Set the converter to this state. State is defined by the component.
      * @param objValue The value to set the control to (Typically Boolean).
      * @param bDisplayOption Display the data on the screen if true.
      * @param iMoveMode INIT, SCREEN, or READ move mode.
      * @return Error code.
      */
     public int setFieldState(Object objValue, boolean bDisplayOption, int iMoveMode)
     {
         if (this.getScreenField().getConverter() == null)
             return DBConstants.NORMAL_RETURN; // Being careful
         boolean bFlag = true;
         if (objValue instanceof Boolean)
             bFlag = ((Boolean)objValue).booleanValue();
         return this.getScreenField().getConverter().setState(bFlag, bDisplayOption, iMoveMode);   // Have the field convert this state to a value in the field
     }
     /**
      * Get this component's current state in an object that can be used to set this
      * component's converter value using setConverterState(obj).
      * @param control The control to get the state from.
      * @return The control's value (Boolean).
      */
     public Object getComponentState(Object control)
     {
         return Boolean.TRUE;    // This will set the value to true for buttons linked to components
     }
     /**
      * Set the component to this state. State is defined by the component.
      * Not implemented for a button.
      * Check the getBitmap method to see if the icon has changed.
      * @param control The control to set the state to.
      * @param objValue The value to set the control to.
      */
     public void setComponentState(Object control, Object objValue)
     {
         if (control != null)
             if (this.getScreenField().getConverter() != null)
         {
         	if (((Converter)this.getScreenField().getConverter()).getBitmap() != null)
         	        ((JButton)control).setIcon(this.loadImageIcon(((Converter)this.getScreenField().getConverter()).getBitmap(), null));
             else
             {
                 Field field = null;
                 if (this.getScreenField().getConverter() != null)
                     field = this.getScreenField().getConverter().getField();
                 for (int i = 0; ; i++)
                 {   // Not happy with this logic - If image is already displayed, this is a button.
                     if (field.getComponent(i) == null)
                         break;
                     if (field.getComponent(i) instanceof SImageView)
                         return;
                 }
                 if (objValue instanceof ImageIcon)
                     ((JButton)control).setIcon((ImageIcon)objValue);
                 else if ((objValue instanceof SerializableImage)
                     && (((SerializableImage)objValue).getImage() != null))
                         ((JButton)control).setIcon(new ImageIcon(((SerializableImage)objValue).getImage()));
                 else if (objValue instanceof URL)
                     ((JButton)control).setIcon(new ImageIcon((URL)objValue));
             }
     	}
     }
     /**
      * Get this field's current state in an object that can be used to set this
      * component's converter value using setConverterState(obj).
      * @return The field's value (class defined by the field type).
      */
     public Object getFieldState()
     {
     	if (this.getScreenField().getConverter() != null)
     		if ((this.getScreenField().getConverter().getData() instanceof ImageIcon)
     				|| (this.getScreenField().getConverter().getData() instanceof SerializableImage)
     		        || (this.getScreenField().getConverter().getData() instanceof URL))
     			return this.getScreenField().getConverter().getData();
         Object state = super.getFieldState();
         if (state == null)
             state = DBConstants.BLANK;   // This is required of enabled control in grid screens (see GridTableModel.getColumnValue).
         return state;
     }
     /**
      * For rendering this component in a grid (TableCellRenderer method).
      * @param table The table.
      * @param value The cell's value.
      * @param isSelected Is the cell selected.
      * @param hasFocus Does the cell have focus?
      * @param row The cell row.
      * @param column The cell column.
      */
     public Component getTableCellRendererComponent(JTable table,
                                                          Object value,
                                                          boolean isSelected,
                                                          boolean hasFocus,
                                                          int row,
                                                          int column)
     {
         Component component = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column);
         if (value == null)
             component.setEnabled(false);
         else
             component.setEnabled(true);
         return component;
     }
     /**
      * Sets an initial value for the editor. This will cause the editor to stopEditing and lose any partially edited value if the editor
      * is editing when this method is called. 
      *
      * Returns the component that should be added to the client's Component hierarchy. Once installed in the client's hierarchy
      * this component will then be able to draw and receive user input. 
      *
      * Parameters: 
      *    table - the JTable that is asking the editor to edit This parameter can be null. 
      *    value - the value of the cell to be edited. It is up to the specific editor to interpret and draw the value. eg. if value is
      *    the String "true", it could be rendered as a string or it could be rendered as a check box that is checked. null is a
      *    valid value. 
      *    isSelected - true if the cell is to be renderer with selection highlighting 
      *    column - the column identifier of the cell being edited 
      *    row - the row index of the cell being edited 
      * Returns: 
      *     the component for editing
      * NOTE: Possible concurrency problem... May want to double-check to be sure the correct record is current.
      * @param table The table.
      * @param value The cell's value.
      * @param isSelected Is the cell selected.
      * @param hasFocus Does the cell have focus?
      * @param row The cell row.
      * @param column The cell column.
      */
     public Component getTableCellEditorComponent(JTable table,
                                                        Object value,
                                                        boolean isSelected,
                                                        int row,
                                                        int column)
     {
         Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
         if (value == null)
             component.setEnabled(false);
         else
             component.setEnabled(true);
         return component;
     }
 }
