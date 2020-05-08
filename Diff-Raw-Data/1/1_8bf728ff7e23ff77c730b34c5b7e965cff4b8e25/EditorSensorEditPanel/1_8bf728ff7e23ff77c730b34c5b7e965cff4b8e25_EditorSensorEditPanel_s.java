 /**
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.vis.ui;
 
 import org.nees.rpi.vis.model.DVModel;
 import org.nees.rpi.vis.model.DVShape;
 import org.nees.rpi.vis.PresetShapeType;
 import org.nees.rpi.validation.AbstractPopupValidator;
 import org.nees.rpi.validation.Validation;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.text.JTextComponent;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableCellEditor;
 import javax.swing.plaf.basic.BasicComboBoxRenderer;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 
 /**
  * Encapsulates the shape table display logic within
  * this panel. Actions and listeners of interest are
  * settable by the parent frame so that the logic is
  * seperated from the display related methods within
  * this class.
  */
 public class EditorSensorEditPanel extends JPanel
 {
 	/** the shape model used to manage shapes added to the model table */
 	ModelTableModel modelTableModel;
 
 	/** The model table used to list the sensor list editable by the user */
 	JTable modelTable = new KeepSelectionWhileEditingJTable();
 
 	/** button to remove the selected shapes from the model */
 	VisLinkButton removeSelectedButton = new VisLinkButton("Remove Selected Sensors", getClass().getResource("/images/model-editor/sensor-remove.png"));
 
 	/**
 	 * a tablemodellistener that the parent associates with this panel so that it
 	 * can add it when relevant to the table model object (for instance when a new
 	 * model is created)
 	 */
 	TableModelListener parentTableModelListener = null;
 
 	public EditorSensorEditPanel()
 	{
 		super();
 		initPanel();
 		initHeader();
 		initTableView();
 		initFooter();
 	}
 
 	public void addShape(DVShape shape)
 	{
 		// remove the listener to suppress event notifications when a
 		// a new shape is added
 		modelTableModel.removeTableModelListener(parentTableModelListener);
 		modelTableModel.fireShapeAdded(shape);
 		modelTableModel.addTableModelListener(parentTableModelListener);
 	}
 
 	public void removeShape(DVShape shape)
 	{
 		// remove the listener to suppress event notifications when a
 		// a shape is removed
 		modelTableModel.removeTableModelListener(parentTableModelListener);
 		modelTableModel.fireShapeRemoved(shape);
 		modelTableModel.addTableModelListener(parentTableModelListener);
 	}
 
 	public java.util.List<DVShape> getSelectedShapes()
 	{
 		ArrayList<DVShape> shapes = new ArrayList<DVShape>();
 		for (int rownum : modelTable.getSelectedRows())
 			shapes.add(modelTableModel.getShapeAt(rownum));
 
 		return shapes;
 	}
 
 	public void clearSelections()
 	{
 		modelTable.clearSelection();
 	}
 
 	public void resetShapeList()
 	{
 		modelTableModel.fireDataChanged();
 	}
 
 	public void cancelEditing()
 	{
 		if (modelTable.isEditing())
 			modelTable.getCellEditor().cancelCellEditing();
 	}
 
 	public void setModel(DVModel model)
 	{
 		modelTableModel = new ModelTableModel(model);
 		modelTable.setModel(modelTableModel);
 		if (parentTableModelListener != null)
 			modelTableModel.addTableModelListener(parentTableModelListener);
 
 		for (int i=0; i<modelTableModel.getColumnCount(); i++ )
 		{
 			TableColumn column = modelTable.getColumnModel().getColumn(i);
 			if (modelTableModel.getColumnClass(i) == PresetShapeType.class)
 			{
 				column.setCellRenderer(new PresetShapeCellRenderer());
 				column.setCellEditor(new PresetShapeCellEditor());
 			}
 			else if (modelTableModel.getColumnClass(i) == Float.class)
 			{
 				column.setCellRenderer(new StandardCellRenderer());
 				column.setCellEditor(new NumberCellEditor());
 			}
 			else if (modelTableModel.getColumnName(i).equalsIgnoreCase("name"))
 			{
 				column.setCellRenderer(new StandardCellRenderer());
 				column.setCellEditor(new NonBlankCellEditor());
 			}
 		}
 	}
 
 	public void setTableModelListener(TableModelListener listener)
 	{
 		parentTableModelListener = listener;
 	}
 
 	public void addShapeSelectionListener(ListSelectionListener listener)
 	{
 		modelTable.getSelectionModel().addListSelectionListener(listener);
 	}
 
 	public void setRemoveSelectedAction(AbstractAction action)
 	{
 		removeSelectedButton.addActionListener(action);
 	}
 
 	private void initPanel()
 	{
 		setOpaque(false);
 		setLayout(new BorderLayout());
 	}
 
 	private void initHeader()
 	{
 		add(new VisTitleLabel("Sensor List"), BorderLayout.NORTH);
 	}
 
 	private void initTableView()
 	{
 		JScrollPane modelTableScrollPane = new JScrollPane(modelTable);
 		modelTableScrollPane.setOpaque(false);
 		modelTableScrollPane.setBorder(BorderFactory.createLineBorder(Color.decode("#CCCCCC")));
 
 		modelTable.setGridColor(Color.decode("#CCCCCC"));
 		modelTable.setOpaque(false);
 		modelTable.setShowGrid(true);
 
 		JPanel paddingPanel = new JPanel();
 		paddingPanel.setOpaque(false);
 		paddingPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
 		paddingPanel.setLayout(new BorderLayout());
 		paddingPanel.add(modelTableScrollPane, BorderLayout.CENTER);
 
 		add(paddingPanel, BorderLayout.CENTER);
 	}
 
 	private void initFooter()
 	{
 		JPanel footer = new JPanel();
 		footer.setOpaque(false);
 		footer.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
 		footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
 
 		footer.add(removeSelectedButton);
 
 		add(footer, BorderLayout.SOUTH);
 	}
 
 	class KeepSelectionWhileEditingJTable extends JTable
 	{
 		KeepSelectionWhileEditingJTable()
 		{
 			super();
 			//this.setSurrendersFocusOnKeystroke(true);
 		}
 
 		public void changeSelection(int row, int column, boolean toggle, boolean extend)
 		{
 			if (! isEditing())
 			{
 				super.changeSelection(row, column, toggle, extend);
 			}
 			else
 			{
 				DefaultCellEditor editor = (DefaultCellEditor) getCellEditor();
 				editor.getComponent().requestFocus();
 			}
 		}
 	}
 
 	class StandardCellRenderer extends DefaultTableCellRenderer
 	{
   		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
 		{
 			Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 			// put cell in edit mode if necessary
 			// note isTableEditable is my control value
 			if (hasFocus && isSelected)
 			{
 				table.editCellAt(row, column);
 				table.getEditorComponent().requestFocus();
 				TableCellEditor tableCellEditor = table.getCellEditor(row, column);
 				Component c = tableCellEditor.getTableCellEditorComponent(table, table.getValueAt(row, column), isSelected, row, column);
 				try {((JTextComponent)c).selectAll();}
 				// ignore selectAll failure
 				catch (Exception notTextEx) {}
 			}
 			return renderer;
 		}
 	}
 
 	/** An extended DefaultCellEditor to handle Custom Validation */
 	class ValidatorCellEditor extends DefaultCellEditor
 	{
 		JTextField textField;
 		AbstractPopupValidator validator;
 		ValidatorCellEditor()
 		{
 			super(new JTextField());
 			textField = (JTextField) getComponent();
 			textField.setBorder(null);
 			/*
 				this onfocus validation is to address when a user tries to leave an invalid
 				cell and the focus is redirected by the KeepSelectionWhileEditingJTable class
 			*/
 			textField.addFocusListener(new FocusListener(){
 				public void focusGained(FocusEvent focusEvent)
 				{
 					validate();
 				}
 				public void focusLost(FocusEvent focusEvent) { }
 			});
 		}
 
 		public void setValidator(AbstractPopupValidator validator)
 		{
 			this.validator = validator;
 		}
 
 		public boolean validate()
 		{
 			return validator.validate();
 		}
 
 		public boolean stopCellEditing()
 		{
 			boolean validated = validate();
 
 			if (validated)
 				return super.stopCellEditing();
 			else
 				return false;
 		}
 	}
 
 	class NonBlankCellEditor extends ValidatorCellEditor
 	{
 		NonBlankCellEditor()
 		{
 			super();
 			setValidator(new PresencePopupValidator(textField));
 		}
 	}
 
 	class NumberCellEditor extends ValidatorCellEditor
 	{
 		NumberCellEditor()
 		{
 			super();
 			setValidator(new NumberPopupValidator(textField));
 		}
 	}
 
 	class PresencePopupValidator extends AbstractPopupValidator
 	{
 		public PresencePopupValidator(JTextComponent c)
 		{
 			super(c, "Cannot be blank");
 		}
 
 		protected boolean validateRules(JTextComponent c)
 		{
 			return Validation.isPresent(c.getText());
 		}
 	}
 
 	class NumberPopupValidator extends AbstractPopupValidator
 	{
 		public NumberPopupValidator(JTextComponent c)
 		{
 			super(c, "Must contain a valid number and cannot be blank");
 		}
 
 		protected boolean validateRules(JTextComponent c)
 		{
 			return Validation.isNumber(c.getText());
 		}
 	}
 
 	class PresetShapeCellRenderer extends DefaultTableCellRenderer
 	{
 		public Component getTableCellRendererComponent(
 				JTable table,
 				Object value,
 				boolean isSelected,
 				boolean hasFocus,
 				int row,
 				int column)
 		{
 			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
 			if (value instanceof PresetShapeType)
 				setText(((PresetShapeType)value).toDisplayString());
 
 			return c;
 		}
 	}
 
 	class PresetShapeCellEditor extends DefaultCellEditor
 	{
 		JComboBox comboEditor;
 		PresetShapeCellEditor()
 		{
 			super(new JComboBox());
 			comboEditor = (JComboBox) getComponent();
 			comboEditor.setModel(new DefaultComboBoxModel(PresetShapeType.values()));
 			comboEditor.setRenderer(new PresetShapeComboBoxEditRenderer());
 		}
 	}
 
 	class PresetShapeComboBoxEditRenderer extends BasicComboBoxRenderer
 	{
 		public PresetShapeComboBoxEditRenderer()
 		{
 			super();
 		}
 
 		public Component getListCellRendererComponent(
 										   JList list,
 										   Object value,
 										   int index,
 										   boolean isSelected,
 										   boolean cellHasFocus)
 		{
 			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 
 			if (value instanceof PresetShapeType)
 				setText(((PresetShapeType)value).toDisplayString());
 
 			return c;
 		}
 	}
 }
