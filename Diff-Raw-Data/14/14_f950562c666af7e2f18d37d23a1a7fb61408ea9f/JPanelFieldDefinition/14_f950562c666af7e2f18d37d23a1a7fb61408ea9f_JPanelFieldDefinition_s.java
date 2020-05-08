 package com.iver.cit.gvsig.gui.cad.panels;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.prefs.Preferences;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
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
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 
 import jwizardcomponent.JWizardComponents;
 import jwizardcomponent.JWizardPanel;
 
 import org.gvsig.gui.beans.swing.JFileChooser;
 
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
 import com.iver.cit.gvsig.fmap.edition.IWriter;
 import com.thoughtworks.xstream.XStreamException;
 
 import es.icarto.gvsig.schema.FieldDefinition;
 import es.icarto.gvsig.schema.SchemaSerializator;
 
 /**
  * @author fjp
  * 
  *         Panel para que el usuario seleccione el driver que va a utilizar para
  *         crear un tema desde cero.
  * 
  */
 public class JPanelFieldDefinition extends JWizardPanel {
 
     private JLabel jLabel = null;
     private JScrollPane jScrollPane = null;
     private JTable jTable = null;
     private JPanel jPanelEast = null;
     private JButton jButtonAddField = null;
     private JButton jButtonDeleteField = null;
     private JButton jButtonLoadSchema = null;
     private JButton jButtonSaveSchema = null;
     private int MAX_FIELD_LENGTH = 254;
 
     private IWriter writer = null;
 
     public JPanelFieldDefinition(JWizardComponents wizardComponents) {
 	super(wizardComponents, null);
 	initialize();
 	// TODO Auto-generated constructor stub
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see jwizardcomponent.JWizardPanel#next()
      */
     @Override
     public void next() {
 	DefaultTableModel tm = (DefaultTableModel) jTable.getModel();
 	boolean valid = validateTableModel(tm);
 	if (valid) {
 	    super.next();
 	}
	if (getWizardComponents().getWizardPanel(2) instanceof SpatialFileBasedPanel) {
	    if (!((SpatialFileBasedPanel) getWizardComponents().getWizardPanel(
		    2)).getPath().equals("")) {
		setFinishButtonEnabled(true);
	    } else {
		setFinishButtonEnabled(false);
	    }
 	}
     }
 
     private boolean validateTableModel(DefaultTableModel tm) {
 	boolean valid = true;
 	for (int i = 0; i < tm.getRowCount(); i++) {
 	    String fieldName = (String) tm.getValueAt(i, 0);
 	    String fieldType = (String) tm.getValueAt(i, 1);
 	    String fieldLength = (String) tm.getValueAt(i, 2);
 	    valid = validateFieldName(fieldName)
 		    && validateInteger(fieldLength)
 		    && validateFieldType(fieldType);
 	    if (!valid) {
 		return valid;
 	    }
 	    int length = Integer.parseInt((String) fieldLength);
 	    if (fieldType.equals("String") && length > MAX_FIELD_LENGTH) {
 		JOptionPane.showMessageDialog(
 			this,
 			PluginServices.getText(this, "max_length_is")
 				+ ": "
 				+ MAX_FIELD_LENGTH
 				+ "\n"
 				+ PluginServices.getText(this,
 					"length_of_field")
 				+ " '"
 				+ fieldName
 				+ "' "
 				+ PluginServices.getText(this,
 					"will_be_truncated"));
 		tm.setValueAt(String.valueOf(MAX_FIELD_LENGTH), i, 2);
 	    }
 
 	}
 
 	// ensure no field name is used more than once
 	ArrayList fieldNames = new ArrayList();
 	for (int i = 0; i < jTable.getRowCount(); i++) {
 	    if (fieldNames.contains(tm.getValueAt(i, 0))) {
 		valid = false;
 		JOptionPane.showMessageDialog(this, PluginServices.getText(
 			this, "two_or_more_fields_with_the_same_name"));
 		break;
 	    }
 	    fieldNames.add(tm.getValueAt(i, 0));
 	}
 	return valid;
     }
 
     private boolean validateFieldType(String fieldType) {
 	boolean isValid = true;
 	if (fieldType == null) {
 	    isValid = false;
 	    JOptionPane.showMessageDialog(
 		    (Component) PluginServices.getMainFrame(),
 		    PluginServices.getText(this, "no_puede_continuar")
 			    + "\n"
 			    + PluginServices.getText(this,
 				    "field_type_cannot_be_null"));
 	}
 	return isValid;
     }
 
     private boolean validateSchema(DefaultTableModel tm) {
 	boolean isValid = true;
 	Pattern nonWordCharsPattern = Pattern.compile("[^\\w]");
 	Pattern upperCaseCharsPattern = Pattern.compile("[A-Z]");
 	for (int i = 0; i < tm.getRowCount(); i++) {
 	    String fieldName = (String) tm.getValueAt(i, 0);
 	    List<String> reservedWords = new ArrayList<String>();
 	    reservedWords = readReservedWordsFromFile();
 	    if (reservedWords.contains(fieldName.toUpperCase())) {
 		isValid = false;
 		JOptionPane.showMessageDialog(
 			(Component) PluginServices.getMainFrame(),
 			PluginServices.getText(this, "no_puede_continuar")
 				+ "\n"
 				+ PluginServices.getText(this, "field")
 				+ " : "
 				+ fieldName
 				+ "\n"
 				+ PluginServices.getText(this,
 					"is_reserved_word"));
 	    }
 	    Matcher matcherNonWordChars = nonWordCharsPattern
 		    .matcher(fieldName);
 	    if (matcherNonWordChars.find()) {
 		isValid = false;
 		JOptionPane.showMessageDialog(
 			(Component) PluginServices.getMainFrame(),
 			PluginServices.getText(this, "no_puede_continuar")
 				+ "\n"
 				+ PluginServices.getText(this, "field")
 				+ " : "
 				+ fieldName
 				+ "\n"
 				+ PluginServices.getText(this,
 					"has_non_word_characters"));
 	    }
 	    Matcher matcherUpperCase = upperCaseCharsPattern.matcher(fieldName);
 	    if (matcherUpperCase.find()) {
 		JOptionPane.showMessageDialog(
 			(Component) PluginServices.getMainFrame(),
 			PluginServices.getText(this, "warning_title")
 				+ "\n"
 				+ PluginServices.getText(this, "field")
 				+ " : "
 				+ fieldName
 				+ "\n"
 				+ PluginServices.getText(this,
 					"has_upper_case_characters"));
 	    }
 	}
 	return isValid;
     }
 
     public void setWriter(IWriter writer) {
 	this.writer = writer;
     }
 
     public IWriter getWriter() {
 	return this.writer;
     }
 
     private boolean validateInteger(String size) {
 	boolean valid = true;
 	try {
 	    Integer.parseInt(size);
 	} catch (NumberFormatException e) {
 	    valid = false;
 	    JOptionPane.showMessageDialog(
 		    (Component) PluginServices.getMainFrame(),
 		    PluginServices.getText(this, "no_puede_continuar") + "\n"
 			    + PluginServices.getText(this, "size") + " : "
 			    + size + "\n"
 			    + PluginServices.getText(this, "incorrect_value"));
 	}
 	return valid;
     }
 
     private boolean validateFieldName(String fieldName) {
 	boolean valid = true;
 	if (fieldName == null) {
 	    valid = false;
 	    JOptionPane.showMessageDialog(
 		    (Component) PluginServices.getMainFrame(),
 		    PluginServices.getText(this, "no_puede_continuar")
 			    + "\n"
 			    + PluginServices.getText(this,
 				    "field_name_cannot_be_null"));
 	} else if (fieldName.equals("")) {
 	    valid = false;
 	    JOptionPane.showMessageDialog(
 		    (Component) PluginServices.getMainFrame(),
 		    PluginServices.getText(this, "no_puede_continuar")
 			    + "\n"
 			    + PluginServices.getText(this,
 				    "the_field_name_is_required"));
 	} else if (fieldName.indexOf(" ") != -1) {
 	    valid = false;
 	    JOptionPane.showMessageDialog(
 		    (Component) PluginServices.getMainFrame(),
 		    PluginServices.getText(this, "no_puede_continuar")
 			    + "\n"
 			    + PluginServices.getText(this, "field")
 			    + " : "
 			    + fieldName
 			    + "\n"
 			    + PluginServices.getText(this,
 				    "contiene_espacios_en_blanco"));
 	} else if (this.writer != null
 		&& this.writer.getCapability("FieldNameMaxLength") != null) {
 	    String value = writer.getCapability("FieldNameMaxLength");
 	    int intValue;
 	    try {
 		intValue = Integer.parseInt(value);
 	    } catch (NumberFormatException e) {
 		intValue = 0;
 	    }
 	    if (intValue > 0 && fieldName.length() > intValue) {
 		valid = false;
 		JOptionPane.showMessageDialog(
 			(Component) PluginServices.getMainFrame(),
 			PluginServices.getText(this, "no_puede_continuar")
 				+ "\n"
 				+ PluginServices.getText(this, "field")
 				+ " : "
 				+ fieldName
 				+ "\n"
 				+ PluginServices.getText(this, "too_long_name")
 				+ "\n"
 				+ PluginServices.getText(this,
 					"maximun_name_size") + " : " + intValue
 				+ "\n");
 	    }
 	} else if (fieldName.length() > MAX_FIELD_LENGTH) {
 	    // if the writer has not defined the max length, truncate the field
 	    // name to MAX_FIELD_LENGTH
 	    valid = false;
 	    JOptionPane.showMessageDialog(
 		    (Component) PluginServices.getMainFrame(),
 		    PluginServices.getText(this, "no_puede_continuar") + "\n"
 			    + PluginServices.getText(this, "field") + " : "
 			    + fieldName + "\n"
 			    + PluginServices.getText(this, "too_long_name")
 			    + "\n"
 			    + PluginServices.getText(this, "maximun_name_size")
 			    + " : " + MAX_FIELD_LENGTH + "\n");
 	}
 
 	return valid;
     }
 
     private List<String> readReservedWordsFromFile() {
 	List<String> list = new ArrayList<String>();
 	try {
 	    String file = "gvSIG/extensiones/com.iver.cit.gvsig.cad/restricted.txt";
 	    FileInputStream in = new FileInputStream(file);
 	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
 	    String strLine;
 	    while ((strLine = br.readLine()) != null) {
 		list.add(strLine.toUpperCase());
 	    }
 	} catch (Exception e) {
 	    list.clear();
 	}
 	return list;
     }
 
     /**
      * This method initializes this
      * 
      */
     private void initialize() {
 	jLabel = new JLabel();
 	jLabel.setText(PluginServices.getText(this, "define_fields"));
 	this.setLayout(new BorderLayout(5, 5));
 	this.setSize(new java.awt.Dimension(499, 232));
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
 	    tm.addColumn(PluginServices.getText(this, "field"));
 
 	    // TableColumn fieldTypeColumn = new TableColumn(1);
 	    // fieldTypeColumn.setHeaderValue("Type");
 	    // jTable.addColumn(fieldTypeColumn);
 	    tm.addColumn(PluginServices.getText(this, "type"));
 	    // MIRAR EL CDIGO DEL BOTN DE AADIR CAMPO PARA VER EL CellEditor
 	    // con comboBox
 
 	    /*
 	     * TableColumn fieldLengthColumn = new TableColumn(2);
 	     * fieldLengthColumn.setHeaderValue("Length"); //
 	     * fieldLengthColumn.setCellRenderer(new
 	     * DefaultTableCellRenderer()); jTable.addColumn(fieldLengthColumn);
 	     */
 	    tm.addColumn(PluginServices.getText(this, "length"));
 
 	    tm.addTableModelListener(new TableModelListener() {
 		@Override
 		public void tableChanged(TableModelEvent e) {
 		    if (((TableModel) e.getSource()).getRowCount() > 0) {
 			jButtonSaveSchema.setEnabled(true);
 		    } else {
 			jButtonSaveSchema.setEnabled(false);
 		    }
 		}
 	    });
 
 	    // Ask to be notified of selection changes.
 	    ListSelectionModel rowSM = jTable.getSelectionModel();
 	    rowSM.addListSelectionListener(new ListSelectionListener() {
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 		    // Ignore extra messages.
 		    if (e.getValueIsAdjusting()) {
 			return;
 		    }
 
 		    ListSelectionModel lsm = (ListSelectionModel) e.getSource();
 		    if (lsm.isSelectionEmpty()) {
 			// no rows are selected
 			jButtonDeleteField.setEnabled(false);
 		    } else {
 			// int selectedRow = lsm.getMinSelectionIndex();
 			// selectedRow is selected
 			jButtonDeleteField.setEnabled(true);
 		    }
 		}
 	    });
 	    jTable.getColumn(PluginServices.getText(this, "field")).setWidth(
 		    180);
 
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
 	    jPanelEast.setPreferredSize(new java.awt.Dimension(170, 100));
 	    jPanelEast.add(getJButtonAddField(), null);
 	    jPanelEast.add(getJButtonDeleteField(), null);
 	    jPanelEast.add(getJButtonLoadSchema(), null);
 	    jPanelEast.add(getJButtonSaveSchema(), null);
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
 	    jButtonAddField.setText(PluginServices.getText(this, "add_field"));
 	    jButtonAddField.setLocation(new java.awt.Point(7, 5));
 	    jButtonAddField.setSize(new java.awt.Dimension(145, 23));
 	    jButtonAddField.setPreferredSize(new java.awt.Dimension(100, 26));
 	    jButtonAddField
 		    .addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent e) {
 			    DefaultTableModel tm = (DefaultTableModel) jTable
 				    .getModel();
 
 			    // Figure out a suitable field name
 			    ArrayList fieldNames = new ArrayList();
 			    for (int i = 0; i < jTable.getRowCount(); i++) {
 				fieldNames.add(tm.getValueAt(i, 0));
 			    }
 			    String[] currentFieldNames = (String[]) fieldNames
 				    .toArray(new String[0]);
 			    String newField = PluginServices.getText(this,
 				    "field").replaceAll(" +", "_");
 			    int index = 0;
 			    for (int i = 0; i < currentFieldNames.length; i++) {
 				if (currentFieldNames[i].startsWith(newField)) {
 				    try {
 					index = Integer
 						.parseInt(currentFieldNames[i]
 							.replaceAll(newField,
 								""));
 				    } catch (Exception ex) { /* we don't care */
 				    }
 				}
 			    }
 			    String newFieldName = newField + (++index);
 
 			    // Add a new row
 			    Object[] newRow = new Object[tm.getColumnCount()];
 			    newRow[0] = newFieldName;
 			    newRow[1] = "String";
 			    newRow[2] = "20";
 			    tm.addRow(newRow);
 
 			    setCellEditorForFieldType();
 
 			    TableColumn widthColumn = jTable.getColumnModel()
 				    .getColumn(2);
 
 			    // tm.setValueAt("NewField", tm.getRowCount()-1, 0);
 			}
 
 		    });
 
 	}
 	return jButtonAddField;
     }
 
     private void setCellEditorForFieldType() {
 	TableColumn typeColumn = jTable.getColumnModel().getColumn(1);
 	JComboBox comboBox = new JComboBox();
 	comboBox.addItem("Boolean");
 	comboBox.addItem("Date");
 	comboBox.addItem("Integer");
 	comboBox.addItem("Double");
 	comboBox.addItem("String");
 	typeColumn.setCellEditor(new DefaultCellEditor(comboBox));
     }
 
     /**
      * This method initializes jButton
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButtonDeleteField() {
 	if (jButtonDeleteField == null) {
 	    jButtonDeleteField = new JButton();
 	    jButtonDeleteField.setText(PluginServices.getText(this,
 		    "delete_field"));
 	    jButtonDeleteField.setLocation(new java.awt.Point(7, 33));
 	    jButtonDeleteField.setSize(new java.awt.Dimension(145, 23));
 	    jButtonDeleteField.setEnabled(false);
 	    jButtonDeleteField
 		    .addActionListener(new java.awt.event.ActionListener() {
 			@Override
 			public void actionPerformed(java.awt.event.ActionEvent e) {
 			    int[] selecteds = jTable.getSelectedRows();
 			    DefaultTableModel tm = (DefaultTableModel) jTable
 				    .getModel();
 
 			    for (int i = selecteds.length - 1; i >= 0; i--) {
 				tm.removeRow(selecteds[i]);
 			    }
 			}
 		    });
 	}
 	return jButtonDeleteField;
     }
 
     /**
      * This method initializes jButtonLoadSchema
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButtonLoadSchema() {
 	if (jButtonLoadSchema == null) {
 	    jButtonLoadSchema = new JButton();
 	    jButtonLoadSchema.setText(PluginServices.getText(this,
 		    "load_schema"));
 	    jButtonLoadSchema.setLocation(new java.awt.Point(7, 61));
 	    jButtonLoadSchema.setSize(new java.awt.Dimension(145, 23));
 	    jButtonLoadSchema.setPreferredSize(new java.awt.Dimension(100, 26));
 	    jButtonLoadSchema.addActionListener(new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 		    Preferences prefs = Preferences.userRoot().node(
 			    "gvsig.foldering");
 		    JFileChooser jfc = new JFileChooser("LOAD_SCHEMA_ID", prefs
 			    .get("TemplatesFolder", null));
 		    if (jfc.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
 			File schema = jfc.getSelectedFile();
 			SchemaSerializator serializator = new SchemaSerializator();
 			try {
 			    List<FieldDefinition> fields = serializator
 				    .fromXML(schema);
 			    DefaultTableModel tm = (DefaultTableModel) jTable
 				    .getModel();
 			    for (FieldDefinition field : fields) {
 				Object[] f = new Object[3];
 				f[0] = field.getName();
 				f[1] = field.getType();
 				f[2] = field.getLength();
 				tm.addRow(f);
 			    }
 			    setCellEditorForFieldType();
 			} catch (XStreamException e) {
 			    showXStreamException(e);
 			}
 		    }
 		}
 	    });
 	}
 	return jButtonLoadSchema;
     }
 
     private void showXStreamException(XStreamException e) {
 	// NotificacionManager.add(e);
 	JOptionPane.showMessageDialog(this,
 		PluginServices.getText(this, "xstream_parsing_error"));
     }
 
     /**
      * This method initializes jButtonSaveSchema
      * 
      * @return javax.swing.JButton
      */
     private JButton getJButtonSaveSchema() {
 	if (jButtonSaveSchema == null) {
 	    jButtonSaveSchema = new JButton();
 	    jButtonSaveSchema.setText(PluginServices.getText(this,
 		    "save_schema"));
 	    jButtonSaveSchema.setLocation(new java.awt.Point(7, 89));
 	    jButtonSaveSchema.setSize(new java.awt.Dimension(145, 23));
 	    jButtonSaveSchema.setPreferredSize(new java.awt.Dimension(100, 26));
 	    jButtonSaveSchema.setEnabled(false);
 	    jButtonSaveSchema.addActionListener(new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 		    // in case the user is still editing the table
 		    if (jTable.isEditing()) {
 			if (jTable.getCellEditor() != null) {
 			    jTable.getCellEditor().stopCellEditing();
 			}
 		    }
 		    DefaultTableModel tm = (DefaultTableModel) jTable
 			    .getModel();
 		    if (validateTableModel(tm) && validateSchema(tm)) {
 			Preferences prefs = Preferences.userRoot().node(
 				"gvsig.foldering");
 			JFileChooser jfc = new JFileChooser("SAVE_SCHEMA_ID",
 				prefs.get("TemplatesFolder", null));
 			if (jfc.showSaveDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
 			    List<FieldDefinition> fields = getFieldsFromModel();
 			    SchemaSerializator serializator = new SchemaSerializator();
 			    String xml = serializator.toXML(fields);
 			    File schema = jfc.getSelectedFile();
 			    if (!schema.exists()) {
 				try {
 				    schema.createNewFile();
 				} catch (IOException e1) {
 				    e1.printStackTrace();
 				}
 			    }
 			    FileWriter fw;
 			    try {
 				fw = new FileWriter(schema.getAbsoluteFile());
 				BufferedWriter bw = new BufferedWriter(fw);
 				bw.write(xml);
 				bw.close();
 			    } catch (IOException e1) {
 				e1.printStackTrace();
 			    }
 			}
 		    }
 		}
 	    });
 	}
 	return jButtonSaveSchema;
     }
 
     public List<FieldDefinition> getFieldsFromModel() {
 	DefaultTableModel tm = (DefaultTableModel) jTable.getModel();
 	List<FieldDefinition> fields = new ArrayList<FieldDefinition>();
 
 	for (int i = 0; i < tm.getRowCount(); i++) {
 	    FieldDefinition field = new FieldDefinition();
 	    field.setName((String) tm.getValueAt(i, 0));
 	    field.setType((String) tm.getValueAt(i, 1));
 	    field.setLength((String) tm.getValueAt(i, 2));
 	    fields.add(field);
 	}
 
 	return fields;
     }
 
     /**
      * Convierte lo que hay en la tabla en una definicin de campos adecuada
      * para crear un LayerDefinition
      * 
      * @return
      */
     public FieldDescription[] getFieldsDescription() {
 	DefaultTableModel tm = (DefaultTableModel) jTable.getModel();
 	FieldDescription[] fieldsDesc = new FieldDescription[tm.getRowCount()];
 
 	for (int i = 0; i < tm.getRowCount(); i++) {
 	    fieldsDesc[i] = new FieldDescription();
 	    fieldsDesc[i].setFieldName((String) tm.getValueAt(i, 0));
 	    String strType = (String) tm.getValueAt(i, 1);
 	    if (strType.equals("String")) {
 		fieldsDesc[i].setFieldType(Types.VARCHAR);
 	    }
 	    if (strType.equals("Double")) {
 		fieldsDesc[i].setFieldType(Types.DOUBLE);
 	    }
 	    if (strType.equals("Integer")) {
 		fieldsDesc[i].setFieldType(Types.INTEGER);
 	    }
 	    if (strType.equals("Boolean")) {
 		fieldsDesc[i].setFieldType(Types.BOOLEAN);
 	    }
 	    if (strType.equals("Date")) {
 		fieldsDesc[i].setFieldType(Types.DATE);
 	    }
 	    int fieldLength = Integer.parseInt((String) tm.getValueAt(i, 2));
 	    fieldsDesc[i].setFieldLength(fieldLength);
 
 	    // TODO: HACERLO BIEN
 	    if (strType.equals("Double")) {
 		fieldsDesc[i].setFieldDecimalCount(5);
 	    }
 
 	}
 
 	return fieldsDesc;
     }
 
 } // @jve:decl-index=0:visual-constraint="10,10"
