 package edacc.experiment;
 
 import edacc.model.Instance;
 import edacc.model.InstanceClassMustBeSourceException;
 import edacc.model.InstanceDAO;
 import edacc.model.SolverConfiguration;
 import edacc.model.SolverConfigurationDAO;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import javax.swing.table.DefaultTableModel;
 
 /**
  *
  * @author simon
  */
 public class GenerateJobsTableModel extends DefaultTableModel {
 
     private ArrayList<SolverConfiguration> solverConfigs;
     private ArrayList<Instance> instances;
     private HashMap<Integer, Integer> rowMap;
     private HashMap<Integer, Integer> colMap;
     private int[][] numRuns;
     private ExperimentController expController;
     
     public GenerateJobsTableModel(ExperimentController expController) {
         this.expController = expController;
     }
 
 
     public void updateNumRuns() throws SQLException, InstanceClassMustBeSourceException, IOException {
         solverConfigs = SolverConfigurationDAO.getSolverConfigurationByExperimentId(expController.getActiveExperiment().getId());
         instances = new ArrayList<Instance>();
         instances.addAll(InstanceDAO.getAllByExperimentId(expController.getActiveExperiment().getId()));
         numRuns = new int[instances.size()][solverConfigs.size()+1];
         rowMap = new HashMap<Integer, Integer>();
         colMap = new HashMap<Integer, Integer>();
         for (int row = 0; row < instances.size(); row++) {
             rowMap.put(instances.get(row).getId(), row);
         }
         for (int col = 1; col <= solverConfigs.size(); col++) {
             colMap.put(solverConfigs.get(col-1).getId(), col);
         }
         for (SolverConfiguration sc : solverConfigs) {
             for (Instance i : instances) {
              //  setNumRuns(i, sc, expController.getExperimentResults().getResults(sc.getId(), i.getId()).size());
                 setNumRuns(i, sc, expController.getExperimentResults().getNumRuns(sc.getId(), i.getId()));
             }
         }
         this.fireTableStructureChanged();
     }
 
 
     @Override
     public int getColumnCount() {
         return solverConfigs==null?0:solverConfigs.size() +1;
     }
 
     @Override
     public String getColumnName(int column) {
         if (column == 0) {
             return "";
         } else {
             return solverConfigs.get(column-1).getName();
         }
     }
 
     @Override
     public int getRowCount() {
         return instances==null?0:instances.size();
     }
 
     @Override
     public Object getValueAt(int row, int column) {
         if (column == 0) {
             return instances.get(row).getName();
         } else {
             return numRuns[row][column];
         }
     }
 
     @Override
     public void setValueAt(Object aValue, int row, int column) {
         numRuns[row][column] = (Integer)aValue;
         this.fireTableCellUpdated(row, column);
     }
 
     @Override
     public boolean isCellEditable(int row, int column) {
         return column >= 1;
     }
 
     @Override
     public Class<?> getColumnClass(int columnIndex) {
         if (this.getRowCount() == 0) {
             return String.class;
         } else {
             return getValueAt(0,columnIndex).getClass();
         }
     }
 
     public void setNumRuns(Instance instance, SolverConfiguration solverConfig, Integer value) {
         numRuns[rowMap.get(instance.getId())][colMap.get(solverConfig.getId())] = value;
     }
 
     public Integer getNumRuns(Instance instance, SolverConfiguration solverConfig) {
         return numRuns[rowMap.get(instance.getId())][colMap.get(solverConfig.getId())];
     }
 
     public Instance getInstance(int row) {
         return instances.get(row);
     }
 
     public SolverConfiguration getSolverConfiguration(int col) {
         return solverConfigs.get(col-1);
     }
 }
