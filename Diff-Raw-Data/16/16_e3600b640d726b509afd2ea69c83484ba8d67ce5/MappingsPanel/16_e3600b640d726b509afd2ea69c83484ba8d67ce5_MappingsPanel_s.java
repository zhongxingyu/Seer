 package eu.europeana.sip.gui;
 
import eu.europeana.core.querymodel.annotation.AnnotationProcessorImpl;
import eu.europeana.core.querymodel.annotation.EuropeanaField;
 import eu.europeana.core.querymodel.beans.AllFieldBean;
 import eu.europeana.core.querymodel.beans.BriefBean;
 import eu.europeana.core.querymodel.beans.FullBean;
 import eu.europeana.core.querymodel.beans.IdBean;
 
 import javax.swing.*;
 import javax.swing.table.AbstractTableModel;
 import java.awt.*;
 import java.util.ArrayList;
 
 /**
  * Provides a set of GUI elements to create basic mappings. Detected ESE elements can
  * be shown in a JTable. Each element can be enabled or disabled using a checbox.
  *
  * @author Serkan Demirel <serkan@blackbuilt.nl>
  */
 public class MappingsPanel extends JPanel implements AnalyzerPanel.Listener {
 
     private Object[][] data = new Object[][]{};
     java.util.List<Class<?>> list = new ArrayList<Class<?>>();
     AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
 
     {
         list.add(IdBean.class);
         list.add(BriefBean.class);
         list.add(FullBean.class);
         list.add(AllFieldBean.class);
         annotationProcessor.setClasses(list);
     }
 
     public MappingsPanel() {
         super(new BorderLayout());
         init();
     }
 
     /**
      * Construct a 3-column table with detected ESE fields on the left side and target ESE fields
      * in the middle. The third column is containing a checkbox where the mapping can be accepted
      * or ignored during normalization.
      *
      * @return The table
      */
     private JTable constructTable() {
         JTable jTable = new JTable(new AbstractTableModel() {
 
             String[] columnNames = {"ESE", "ESE+", "Normalize?"};
 
             @Override
             public int getRowCount() {
                 return data.length;
             }
 
             @Override
             public int getColumnCount() {
                 return columnNames.length;
             }
 
             @Override
             public String getColumnName(int column) {
                 return columnNames[column];
             }
 
             @Override
             public Object getValueAt(int rowIndex, int columnIndex) {
                 return data[rowIndex][columnIndex];
             }
 
             @Override
             public void setValueAt(Object value, int rowIndex, int columnIndex) {
                 data[rowIndex][columnIndex] = value;
                 fireTableCellUpdated(rowIndex, columnIndex);
             }
 
             @Override
             public Class<?> getColumnClass(int columnIndex) {
                 return getValueAt(0, columnIndex).getClass();
             }
 
             @Override
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return getValueAt(rowIndex, columnIndex) instanceof Boolean;
             }
 
             @Override
             public void fireTableCellUpdated(int row, int column) {
                 String value = getValueAt(row, 0).toString();
                 // todo: create snippet from GroovyService.createGroovyLoop() and store in GroovyMappingFile
                 String variable = value.substring(value.lastIndexOf("_") + 1);
                 String snippet = String.format("for ($%s in %s) {%n    %s $%s;%n}%n%n", variable, value, variable, variable);
                 if ((Boolean) getValueAt(row, column)) {
                     JOptionPane.showMessageDialog(null, snippet, "Adding code", JOptionPane.YES_OPTION);
                 }
                 else {
                     JOptionPane.showMessageDialog(null, snippet, "Removing code", JOptionPane.YES_OPTION);
                 }
             }
         });
         jTable.setFillsViewportHeight(true);
         return jTable;
     }
 
     private boolean isMappable(String v) {
         v = v.substring(v.lastIndexOf(".") + 1); // stripping prefix
         for (Object value : annotationProcessor.getMappableFields()) {
             EuropeanaField field = (EuropeanaField) value;
             if (v.equals(field.getFacetName()) && field.isMappable()) {
                 return true;
             }
         }
         return false;
     }
 
     private void init() {
         JScrollPane foundElementsContainer = new JScrollPane(constructTable());
         add(foundElementsContainer);
         add(foundElementsContainer, BorderLayout.CENTER);
     }
 
     @Override
     public void updateAvailableNodes(java.util.List<String> nodes) {
         int counter = 0;
         data = new Object[nodes.size()][];
         for (String s : nodes) {
             data[counter] = new Object[]{s, s, isMappable(s)};
             counter++;
         }
     }
 }
