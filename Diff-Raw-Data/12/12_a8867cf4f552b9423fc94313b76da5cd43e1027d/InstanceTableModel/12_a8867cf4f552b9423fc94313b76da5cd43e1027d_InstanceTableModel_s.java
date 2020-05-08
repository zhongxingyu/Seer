 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edacc.manageDB;
 
 import edacc.model.Instance;
 import edacc.model.InstanceDAO;
 import edacc.model.InstanceHasInstanceClassDAO;
 import edacc.model.InstanceHasProperty;
 import edacc.model.InstanceHasPropertyDAO;
 import edacc.model.Property;
 import edacc.model.PropertyDAO;
 import edacc.model.PropertyType;
 import edacc.satinstances.ConvertException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.table.AbstractTableModel;
 
 /**
  *
  * @author rretz
  */
 public class InstanceTableModel extends edacc.experiment.InstanceTableModel {
 
     public static final int COL_PROP = 2;
     private boolean[] visible;
     private ArrayList<Property> properties;
     private String[] CONST_COLUMNS = {"Name", "MD5"};
     private boolean[] CONST_VISIBLE = {true, true};
     private String[] columns;
     protected Vector<Instance> instances;
     protected HashMap<Instance, LinkedList<Integer>> instanceClassIds;
 
     public String[] getAllColumnNames() {
         return columns;
     }
 
     public InstanceTableModel() {
         this.instances = new Vector<Instance>();
         columns = new String[CONST_COLUMNS.length];
         visible = new boolean[columns.length];
         for (int i = 0; i < columns.length; i++) {
             columns[i] = CONST_COLUMNS[i];
             visible[i] = CONST_VISIBLE[i];
         }
         instanceClassIds = new HashMap<Instance, LinkedList<Integer>>();
     }
 
     public boolean isEmpty() {
         if (instances.isEmpty()) {
             return true;
         }
         return false;
     }
 
     public Vector<Instance> getInstances() {
         return instances;
     }
 
     public void addInstances(Vector<Instance> instances) {
         for (int i = 0; i < instances.size(); i++) {
             addInstance(instances.get(i));
         }
 
 
         for (Instance i : instances) {
             instanceClassIds.put(i, new LinkedList<Integer>());
         }
         try {
             InstanceHasInstanceClassDAO.fillInstanceClassIds(instanceClassIds);
         } catch (SQLException ex) {
             // TODO: error
         }
     }
 
     private void addInstance(Instance in) {
         if (!instances.contains(in)) {
             instances.add(in);
         }
     }
 
     public void clearTable() {
         instances.removeAllElements();
     }
 
     public void remove(Instance instance) {
         instances.remove(instance);
         instanceClassIds.remove(instance);
     }
 
     public void removeInstances(Vector<Instance> instances) {
         this.instances.removeAll(instances);
     }
 
     @Override
     public int getRowCount() {
         return instances == null ? 0 : instances.size();
     }
 
     @Override
     public int getColumnCount() {
         int res = 0;
         for (int i = 0; i < visible.length; i++) {
             if (visible[i]) {
                 res++;
             }
         }
         return res;
     }
 
     @Override
     public String getColumnName(int col) {
         return columns[col];
     }
 
     @Override
     public Class getColumnClass(int col) {
         if (this.getRowCount() == 0) {
             return String.class;
         } else {
             if (col >= COL_PROP) {
                 int propertyIdx = col - COL_PROP;
                 if (propertyIdx < properties.size()) {
                     return properties.get(propertyIdx).getPropertyValueType().getJavaType();
                 } else {
                     return String.class;
                 }
             }
             return getValueAt(0, col).getClass();
         }
     }
 
     /**
      *
      * @param rowIndex
      * @param columnIndex 
      * @return the Object from the choosen cell; return == "" when columnIndex is out of columnRange
      */
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         if (columnIndex != -1) {
             columnIndex = getIndexForColumn(columnIndex);
         }
 
         switch (columnIndex) {
             case 0:
                 return instances.get(rowIndex).getName();
             case 1:
                 return instances.get(rowIndex).getMd5();
             default:
                 int propertyIdx = columnIndex - COL_PROP;
                 Property prop = properties.get(propertyIdx);
                 if (prop.getType().equals(PropertyType.InstanceProperty)) {
                     InstanceHasProperty ihp = null;
                     try {
                         ihp = InstanceHasPropertyDAO.getByInstanceAndProperty(instances.get(rowIndex), prop);
                     } catch (Exception e) {
                     }
                     if (ihp == null) {
                         return null;
                     } else {
                         try {
                             return prop.getPropertyValueType().getJavaTypeRepresentation(ihp.getValue());
                         } catch (ConvertException ex) {
                             return "";
                         }
                     }
                 } else {
                     return "";
                 }
         }
     }
 
     public Instance getInstance(int rowIndex) {
         return instances.elementAt(rowIndex);
     }
 
     public boolean[] getColumnVisibility() {
         return visible;
     }
 
     public void setColumnVisibility(boolean[] visibility, boolean updateTable) {
         if (columns.length != visible.length) {
             return;
         }
         this.visible = visibility;
         if (updateTable) {
             this.fireTableStructureChanged();
         }
     }
 
     public void updateProperties() {
         ArrayList<Property> tmp = new ArrayList<Property>();
         try {
             tmp.addAll(PropertyDAO.getAllInstanceProperties());
         } catch (Exception e) {
             if (edacc.ErrorLogger.DEBUG) {
                 e.printStackTrace();
             }
         }
         if (!tmp.equals(properties)) {
             properties = tmp;
 
             for (int i = properties.size() - 1; i >= 0; i--) {
                 if (properties.get(i).isMultiple()) {
                     properties.remove(i);
                 }
             }
             columns = java.util.Arrays.copyOf(columns, CONST_COLUMNS.length + properties.size());
             visible = java.util.Arrays.copyOf(visible, CONST_VISIBLE.length + properties.size());
             int j = 0;
             for (int i = CONST_COLUMNS.length; i < columns.length; i++) {
                 columns[i] = properties.get(j).getName();
                 j++;
             }
             this.resetColumnVisibility();
             this.fireTableStructureChanged();
         }
 
 
     }
 
     public void resetColumnVisibility() {
         System.arraycopy(CONST_VISIBLE, 0, visible, 0, CONST_VISIBLE.length);
         for (int i = CONST_VISIBLE.length; i < visible.length; i++) {
             visible[i] = false;
         }
         fireTableStructureChanged();
     }
 
     private int getIndexForColumn(int columnIndex) {
         for (int i = 0; i < visible.length; i++) {
             if (visible[i]) {
                 columnIndex--;
             }
             if (columnIndex == -1) {
                 return i;
             }
         }
         return 0;
     }
 
     public LinkedList<Integer> getInstanceClassIdsForRow(int rowIndex) {
         LinkedList<Integer> res = instanceClassIds.get(instances.get(rowIndex));
         return res == null ? new LinkedList<Integer>() : res;
     }
 
     void addNewInstances(Vector<Instance> instances) {
         for (int i = 0; i < instances.size(); i++) {
             addNewInstance(instances.get(i));
         }
 
 
         for (Instance i : instances) {
             instanceClassIds.put(i, new LinkedList<Integer>());
         }
         try {
             InstanceHasInstanceClassDAO.fillInstanceClassIds(instanceClassIds);
         } catch (SQLException ex) {
             // TODO: error
         }
     }
 
     private void addNewInstance(Instance in) {
         if (!instances.contains(in)) {
             try {
                 instances.add(in);
                 LinkedList<Integer> classes = InstanceHasInstanceClassDAO.getRelatedInstanceClasses(in.getId());
                 instanceClassIds.put(in, classes);
             } catch (SQLException ex) {
                 Logger.getLogger(InstanceTableModel.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 }
