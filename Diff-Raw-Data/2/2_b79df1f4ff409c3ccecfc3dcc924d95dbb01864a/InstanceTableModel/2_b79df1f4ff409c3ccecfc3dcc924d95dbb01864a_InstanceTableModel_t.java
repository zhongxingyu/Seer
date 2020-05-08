 package edacc.experiment;
 
 import edacc.model.DatabaseConnector;
 import edacc.model.ExperimentHasInstance;
 import edacc.model.Instance;
 import edacc.model.InstanceDAO;
 import edacc.model.InstanceHasProperty;
 import edacc.model.Property;
 import edacc.model.PropertyDAO;
 import edacc.satinstances.ConvertException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import javax.swing.table.AbstractTableModel;
 import java.util.Vector;
 
 /**
  *
  * @author daniel
  */
 public class InstanceTableModel extends AbstractTableModel {
 
     public static int COL_NAME = 0;
     public static int COL_BENCHTYPE = 1;
     public static int COL_SELECTED = 1;
     public static int COL_PROP = 2;
     private static final String[] columns_competition = {"Name", "Benchmark Type", "selected"};
     private static final String[] columns_noCompetition = {"Name", "selected"};
     private String[] columns = columns_noCompetition;
     ArrayList<Property> properties;
     protected ArrayList<Instance> instances;
     protected Vector<ExperimentHasInstance> experimentHasInstances;
     protected HashMap<Integer, ExperimentHasInstance> selectedInstances;
     protected Vector<ExperimentHasInstance> savedExperimentInstances;
     protected String[] benchmarkTypes;
 
     public void setInstances(ArrayList<Instance> instances) {
         boolean isCompetition;
 
         try {
             isCompetition = DatabaseConnector.getInstance().isCompetitionDB();
         } catch (Exception e) {
             isCompetition = false;
         }
         if (isCompetition) {
             columns = columns_competition;
             COL_BENCHTYPE = 1;
             COL_SELECTED = 2;
             COL_PROP = 3;
         } else {
             columns = columns_noCompetition;
             COL_BENCHTYPE = -1;
             COL_SELECTED = 1;
             COL_PROP = 2;
         }
         updateProperties();
         this.instances = instances;
         experimentHasInstances = new Vector<ExperimentHasInstance>();
         experimentHasInstances.setSize(instances.size());
         if (instances == null) {
             benchmarkTypes = null;
         } else if (isCompetition) {
             benchmarkTypes = new String[instances.size()];
             try {
                 HashMap<Integer, String> types = InstanceDAO.getBenchmarkTypes();
                 for (int i = 0; i < instances.size(); i++) {
                     benchmarkTypes[i] = types.get(instances.get(i).getId());
 
                 }
             } catch (Exception e) {
             }
 
         }
         this.fireTableStructureChanged();
     }
 
     private void updateProperties() {
         properties = new ArrayList<Property>();
         // TODO: fix!
         try {
             properties.addAll(PropertyDAO.getAllInstanceProperties());
             if (properties.size() > 0) {
                 columns = java.util.Arrays.copyOf(columns, COL_PROP + properties.size());
                 for (int i = COL_PROP; i < columns.length; i++) {
                     columns[i] = properties.get(i - COL_PROP).getName();
                 }
             }
         } catch (Exception e) {
             if (edacc.ErrorLogger.DEBUG) {
                 e.printStackTrace();
             }
         }
 
     }
 
     /**
      * Deselects all selected instances and selects all instances which are specified
      * by the given parameter experimentHasInstances.
      * @param experimentHasInstances
      */
     public void setExperimentHasInstances(Vector<ExperimentHasInstance> experimentHasInstances) {
         this.savedExperimentInstances = experimentHasInstances;
         selectedInstances.clear();
         for (ExperimentHasInstance ehi : experimentHasInstances) {
             selectedInstances.put(ehi.getInstances_id(), ehi);
         }
         for (int i = 0; i < this.experimentHasInstances.size(); i++) {
             this.experimentHasInstances.set(i, null);
         }
         for (int i = 0; i < experimentHasInstances.size(); i++) {
             for (int j = 0; j < this.experimentHasInstances.size(); j++) {
                 if (instances.get(j).getId() == experimentHasInstances.get(i).getInstances_id()) {
                     this.experimentHasInstances.set(j, experimentHasInstances.get(i));
                     break;
                 }
             }
         }
     }
 
     public void undo() {
         setExperimentHasInstances(this.savedExperimentInstances);
         this.fireTableDataChanged();
     }
 
     /**
      * Returns a vector with all instance ids for which there is no
      * corresponding ExperimentHasInstance.
      * @return Vector of the new instance ids
      */
     public Vector<Integer> getNewInstanceIds() {
         Vector<Integer> res = new Vector<Integer>();
         for (Integer instanceId : selectedInstances.keySet()) {
             ExperimentHasInstance ehi = selectedInstances.get(instanceId);
             if (ehi == null) {
                 res.add(instanceId);
             }
         }
         return res;
     }
 
     public Instance getInstanceAt(int rowIndex) {
         return instances.get(rowIndex);
     }
 
     /**
      * Returns a vector with all deselected ExperimentHasInstance objects which
      * were specified by setExperimentHasInstances()
      * @return arraylist of the ExperimentHasInstance objects
      */
     public ArrayList<ExperimentHasInstance> getDeletedExperimentHasInstances() {
         ArrayList<ExperimentHasInstance> res = new ArrayList<ExperimentHasInstance>();
         if (savedExperimentInstances == null) {
             return res;
         }
         for (ExperimentHasInstance ehi : savedExperimentInstances) {
             if (!selectedInstances.containsKey(ehi.getInstances_id())) {
                 res.add(ehi);
             }
         }
         return res;
     }
 
     public boolean isModified() {
         return getDeletedExperimentHasInstances().size() > 0 || getNewInstanceIds().size() > 0;
     }
 
     public void setExperimentHasInstance(ExperimentHasInstance e, int row) {
         this.experimentHasInstances.set(row, e);
     }
 
     public InstanceTableModel() {
         this.instances = new ArrayList<Instance>();
         this.experimentHasInstances = new Vector<ExperimentHasInstance>();
         selectedInstances = new HashMap<Integer, ExperimentHasInstance>();
     }
 
     public ArrayList<Instance> getSelectedInstances() {
         ArrayList<Instance> res = new ArrayList<Instance>();
         for (Instance instance : instances) {
             if (selectedInstances.containsKey(instance.getId())) {
                 res.add(instance);
             }
         }
         return res;
     }
 
     @Override
     public int getRowCount() {
         return instances.size();
     }
 
     @Override
     public int getColumnCount() {
         return columns.length;
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
 
     @Override
     public boolean isCellEditable(int row, int col) {
         return col == COL_SELECTED;
     }
 
     @Override
     public void setValueAt(Object value, int row, int col) {
         if (col == COL_SELECTED) {
             if ((Boolean) value) {
                 selectedInstances.put(instances.get(row).getId(), experimentHasInstances.get(row));
             } else {
                 selectedInstances.remove(instances.get(row).getId());
             }
         }
         this.fireTableCellUpdated(row, col);
     }
 
     public ExperimentHasInstance getExperimentHasInstance(int rowIndex) {
         return experimentHasInstances.get(rowIndex);
     }
 
     public Instance getInstance(int rowIndex) {
         return instances.get(rowIndex);
     }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         if (columnIndex == COL_NAME) {
             return instances.get(rowIndex).getName();
         } else if (columnIndex == COL_BENCHTYPE) {
             return benchmarkTypes[rowIndex]==null?"":benchmarkTypes[rowIndex];
         } else if (columnIndex == COL_SELECTED) {
             return selectedInstances.containsKey(instances.get(rowIndex).getId());
         } else {
             int propertyIdx = columnIndex - COL_PROP;
             if (properties.size() <= propertyIdx || instances.get(rowIndex).getPropertyValues() == null) {
                 return null;
             }
             InstanceHasProperty ip = instances.get(rowIndex).getPropertyValues().get(properties.get(propertyIdx).getId());
            if (ip == null || ip.getValue() == null) {
                 return null;
             }
             try {
                 return properties.get(propertyIdx).getPropertyValueType().getJavaTypeRepresentation(ip.getValue());
             } catch (ConvertException ex) {
                 return null;
             }
         }
     }
 }
