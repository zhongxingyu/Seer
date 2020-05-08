 package com.iver.cit.gvsig.gui.cad.panels;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.sql.Types;
 import java.util.ArrayList;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 
 import jwizardcomponent.JWizardComponents;
 import jwizardcomponent.JWizardPanel;
 
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
 import com.iver.cit.gvsig.fmap.edition.IWriter;
 
 /**
  * @author fjp
  *
  * Panel para que el usuario seleccione el driver que va a utilizar para
  * crear un tema desde cero.
  *
  */
 public class JPanelFieldDefinition extends JWizardPanel {
 
 
 	private JLabel jLabel = null;
 	private JScrollPane jScrollPane = null;
 	private JTable jTable = null;
 	private JPanel jPanelEast = null;
 	private JButton jButtonAddField = null;
 	private JButton jButtonDeleteField = null;
 	private int MAX_FIELD_LENGTH = 254;

 	private IWriter writer = null;
 
 
 	public JPanelFieldDefinition(JWizardComponents wizardComponents) {
 		super(wizardComponents, null);
 		initialize();
 		// TODO Auto-generated constructor stub
 	}
 
 
 	/* (non-Javadoc)
 	 * @see jwizardcomponent.JWizardPanel#next()
 	 */
 	public void next() {
 		DefaultTableModel tm=(DefaultTableModel) jTable.getModel();
 		boolean valid=true;
 		for (int i = 0;i<tm.getRowCount();i++) {
 				String s=(String)tm.getValueAt(i,0);
 				valid=validate(s);
 				String size=(String) tm.getValueAt(i,2);
 				valid=valid && validateInteger(size);
 				if (!valid){
 					return;
 				}
 				String type = (String) tm.getValueAt(i,1);
 				int length = Integer.parseInt((String) tm.getValueAt(i,2));
 				if (type.equals("String") && length > MAX_FIELD_LENGTH) {
 					JOptionPane.showMessageDialog(this, PluginServices.getText(this, "max_length_is") + ": " + MAX_FIELD_LENGTH+
 							"\n"+PluginServices.getText(this, "length_of_field")+ " '"+ s + "' " + PluginServices.getText(this, "will_be_truncated"));
 					tm.setValueAt(String.valueOf(MAX_FIELD_LENGTH),i,2);
 				}
 
 		}
 
 		// ensure no field name is used more than once
 		ArrayList fieldNames = new ArrayList();
 		for (int i = 0; i < jTable.getRowCount(); i++) {
 			if (fieldNames.contains(tm.getValueAt(i,0))) {
 				valid = false;
 				JOptionPane.showMessageDialog(this, PluginServices.getText(this, "two_or_more_fields_with_the_same_name"));
 				break;
 			}
 			fieldNames.add(tm.getValueAt(i, 0));
 		}

 		if (valid)
 			super.next();
		if (getWizardComponents().getWizardPanel(2) instanceof  FileBasedPanel) {
			if (!((FileBasedPanel)getWizardComponents().getWizardPanel(2)).getPath().equals(""))
				setFinishButtonEnabled(true);
			else
				setFinishButtonEnabled(false);
		}
 	}
 
 	public void setWriter(IWriter writer) {
 		this.writer = writer;
 	}

 	public IWriter getWriter() {
 		return this.writer;
 	}
 
 	private boolean validateInteger(String size) {
 		boolean valid=true;
 		try{
 		Integer.parseInt(size);
 		}catch (NumberFormatException e) {
 			valid=false;
 			JOptionPane.showMessageDialog((Component)PluginServices.getMainFrame(),
 					PluginServices.getText(this,"no_puede_continuar")+"\n"+
 					PluginServices.getText(this,"size")+" : "+size+"\n"+
 					PluginServices.getText(this,"incorrect_value"));
 		}
 		return valid;
 	}
 
 
 	private boolean validate(String s) {
 		boolean valid=true;
 		if (s.equals("")) {
 			valid=false;
 			JOptionPane.showMessageDialog((Component)PluginServices.getMainFrame(),
					PluginServices.getText(this,"no_puede_continuar")+"\n"+
 					PluginServices.getText(this,"the_field_name_is_required"));
 		}
 		if (s.indexOf(" ")!=-1) {
 			valid=false;
 			JOptionPane.showMessageDialog((Component)PluginServices.getMainFrame(),
 					PluginServices.getText(this,"no_puede_continuar")+"\n"+
 					PluginServices.getText(this,"field")+" : "+s+"\n"+
 					PluginServices.getText(this,"contiene_espacios_en_blanco"));
 		}
 		if (this.writer != null && this.writer.getCapability("FieldNameMaxLength") != null) {
 			String value = writer.getCapability("FieldNameMaxLength");
 			int intValue;
 			try {
 				intValue = Integer.parseInt(value);
 			} catch (NumberFormatException e) {
 				intValue = 0;
 			}
 			if (intValue > 0 && s.length() > intValue) {
 				valid=false;
 				JOptionPane.showMessageDialog((Component)PluginServices.getMainFrame(),
 						PluginServices.getText(this,"no_puede_continuar")+"\n"+
 						PluginServices.getText(this,"field")+" : "+s+"\n"+
 						PluginServices.getText(this,"too_long_name")+"\n"+
 						PluginServices.getText(this,"maximun_name_size")+" : "+intValue+"\n"
						);
 			}
 		}
 		return valid;
 	}
 
 
 	/**
 	 * This method initializes this
 	 *
 	 */
 	private void initialize() {
         jLabel = new JLabel();
         jLabel.setText(PluginServices.getText(this,"define_fields"));
         this.setLayout(new BorderLayout(5,5));
         this.setSize(new java.awt.Dimension(499,232));
         this.add(jLabel, java.awt.BorderLayout.NORTH);
         this.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
         this.add(getJPanelEast(), java.awt.BorderLayout.EAST);
 	}
 
 
 	/**
 	 * This method initializes jScrollPane
 	 *
 	 * @return javax.swing.JScrollPane
 	 */
 	private JScrollPane getJScrollPane() {
 		if (jScrollPane == null) {
 			jScrollPane = new JScrollPane();
 			jScrollPane.setViewportView(getJTable());
 		}
 		return jScrollPane;
 	}
 
 
 	/**
 	 * This method initializes jTable
 	 *
 	 * @return javax.swing.JTable
 	 */
 	private JTable getJTable() {
 		if (jTable == null) {
 			jTable = new JTable();
 
 			DefaultTableModel tm = (DefaultTableModel) jTable.getModel();
 			tm.addColumn(PluginServices.getText(this,"field"));
 
 
 			// TableColumn fieldTypeColumn = new TableColumn(1);
 			// fieldTypeColumn.setHeaderValue("Type");
 			// jTable.addColumn(fieldTypeColumn);
 			tm.addColumn(PluginServices.getText(this,"type"));
 			// MIRAR EL CDIGO DEL BOTN DE AADIR CAMPO PARA VER EL CellEditor con comboBox
 
 
 
 			/* TableColumn fieldLengthColumn = new TableColumn(2);
 			fieldLengthColumn.setHeaderValue("Length");
 			// fieldLengthColumn.setCellRenderer(new DefaultTableCellRenderer());
 			jTable.addColumn(fieldLengthColumn); */
 			tm.addColumn(PluginServices.getText(this,"length"));
 
 //			Ask to be notified of selection changes.
 			ListSelectionModel rowSM = jTable.getSelectionModel();
 			rowSM.addListSelectionListener(new ListSelectionListener() {
 			    public void valueChanged(ListSelectionEvent e) {
 			        //Ignore extra messages.
 			        if (e.getValueIsAdjusting()) return;
 
 			        ListSelectionModel lsm =
 			            (ListSelectionModel)e.getSource();
 			        if (lsm.isSelectionEmpty()) {
 			            //no rows are selected
 			        	jButtonDeleteField.setEnabled(false);
 			        } else {
 			            // int selectedRow = lsm.getMinSelectionIndex();
 			            //selectedRow is selected
 			        	jButtonDeleteField.setEnabled(true);
 			        }
 			    }
 			});
 			jTable.getColumn(PluginServices.getText(this,"field")).setWidth(180);
 
 
 		}
 		return jTable;
 	}
 
 
 	/**
 	 * This method initializes jPanelWest
 	 *
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJPanelEast() {
 		if (jPanelEast == null) {
 			jPanelEast = new JPanel();
 			jPanelEast.setLayout(null);
 			jPanelEast.setPreferredSize(new java.awt.Dimension(170,100));
 			jPanelEast.add(getJButtonAddField(), null);
 			jPanelEast.add(getJButtonDeleteField(), null);
 		}
 		return jPanelEast;
 	}
 
 
 	/**
 	 * This method initializes jButtonAddField
 	 *
 	 * @return javax.swing.JButton
 	 */
 	private JButton getJButtonAddField() {
 		if (jButtonAddField == null) {
 			jButtonAddField = new JButton();
 			jButtonAddField.setText(PluginServices.getText(this,"add_field"));
 			jButtonAddField.setLocation(new java.awt.Point(7,5));
 			jButtonAddField.setSize(new java.awt.Dimension(145,23));
 			jButtonAddField.setPreferredSize(new java.awt.Dimension(100,26));
 			jButtonAddField.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					DefaultTableModel tm = (DefaultTableModel) jTable.getModel();
 
 					// Figure out a suitable field name
 					ArrayList fieldNames = new ArrayList();
 					for (int i = 0; i < jTable.getRowCount(); i++) {
 						fieldNames.add(tm.getValueAt(i, 0));
 					}
 					String[] currentFieldNames = (String[]) fieldNames.toArray(new String[0]);
 					String newField = PluginServices.getText(this, "field").replaceAll(" +", "_");
 					int index=0;
 					for (int i = 0; i < currentFieldNames.length; i++) {
 						if (currentFieldNames[i].startsWith(newField)) {
 							try {
 								index = Integer.parseInt(currentFieldNames[i].replaceAll(newField,""));
 							} catch (Exception ex) { /* we don't care */}
 						}
 					}
 					String newFieldName = newField+(++index);

 
 					// Add a new row
 					Object[] newRow = new Object[tm.getColumnCount()];
 					newRow[0] = newFieldName;
 					newRow[1] = "String";
 					newRow[2] = "20";
 					tm.addRow(newRow);
 
 					// Esto lo aado aqu porque si no tiene registros, no hace caso. (Por eso no
 					// lo pongo en getJTable()
 					TableColumn typeColumn = jTable.getColumnModel().getColumn(1);
 					JComboBox comboBox = new JComboBox();
 					comboBox.addItem("Boolean");
 					comboBox.addItem("Date");
 					comboBox.addItem("Integer");
 					comboBox.addItem("Double");
 					comboBox.addItem("String");
 					typeColumn.setCellEditor(new DefaultCellEditor(comboBox));
 
 					TableColumn widthColumn = jTable.getColumnModel().getColumn(2);
 
 					// tm.setValueAt("NewField", tm.getRowCount()-1, 0);
 				}
 			});
 
 		}
 		return jButtonAddField;
 	}
 
 
 	/**
 	 * This method initializes jButton
 	 *
 	 * @return javax.swing.JButton
 	 */
 	private JButton getJButtonDeleteField() {
 		if (jButtonDeleteField == null) {
 			jButtonDeleteField = new JButton();
 			jButtonDeleteField.setText(PluginServices.getText(this,"delete_field"));
 			jButtonDeleteField.setLocation(new java.awt.Point(7,33));
 			jButtonDeleteField.setSize(new java.awt.Dimension(145,23));
 			jButtonDeleteField.setEnabled(false);
 			jButtonDeleteField.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					int[] selecteds = jTable.getSelectedRows();
 					DefaultTableModel tm = (DefaultTableModel) jTable.getModel();
 
 					for (int i=selecteds.length-1; i >=0; i--)
 						tm.removeRow(selecteds[i]);
 				}
 			});
 		}
 		return jButtonDeleteField;
 	}
 
 
 	/**
 	 * Convierte lo que hay en la tabla en una definicin de campos
 	 * adecuada para crear un LayerDefinition
 	 * @return
 	 */
 	public FieldDescription[] getFieldsDescription() {
 		DefaultTableModel tm = (DefaultTableModel) jTable.getModel();
 		FieldDescription[] fieldsDesc = new FieldDescription[tm.getRowCount()];
 
 		for (int i=0; i < tm.getRowCount(); i++)
 		{
 			fieldsDesc[i] = new FieldDescription();
 			fieldsDesc[i].setFieldName((String) tm.getValueAt(i,0));
 			String strType = (String) tm.getValueAt(i,1);
 			if (strType.equals("String"))
 				fieldsDesc[i].setFieldType(Types.VARCHAR);
 			if (strType.equals("Double"))
 				fieldsDesc[i].setFieldType(Types.DOUBLE);
 			if (strType.equals("Integer"))
 				fieldsDesc[i].setFieldType(Types.INTEGER);
 			if (strType.equals("Boolean"))
 				fieldsDesc[i].setFieldType(Types.BOOLEAN);
 			if (strType.equals("Date"))
 				fieldsDesc[i].setFieldType(Types.DATE);
 			int fieldLength = Integer.parseInt((String) tm.getValueAt(i,2));
 			fieldsDesc[i].setFieldLength(fieldLength);
 
 			// TODO: HACERLO BIEN
 			if (strType.equals("Double"))
 				fieldsDesc[i].setFieldDecimalCount(5);
 
 		}
 
 		return fieldsDesc;
 	}
 
 
 
 }  //  @jve:decl-index=0:visual-constraint="10,10"
