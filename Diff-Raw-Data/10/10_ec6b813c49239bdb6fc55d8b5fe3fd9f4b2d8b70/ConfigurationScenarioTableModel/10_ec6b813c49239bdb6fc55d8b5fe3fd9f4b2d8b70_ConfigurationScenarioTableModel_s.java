 package edacc.experiment;
 
 import edacc.model.ConfigurationScenario;
 import edacc.model.ConfigurationScenarioParameter;
 import edacc.model.Parameter;
 import edacc.model.ParameterDAO;
 import edacc.model.Solver;
 import edacc.model.SolverBinaries;
 import edacc.model.SolverDAO;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Vector;
 
 /**
  *
  * @author simon
  */
 public class ConfigurationScenarioTableModel extends ThreadSafeDefaultTableModel {
 
     public static final int COL_PARAMETER = 0;
     public static final int COL_SELECTED = 1;
     public static final int COL_FIXEDVALUE = 2;
     public static final int COL_VALUE = 3;
     private static final String[] columns = {"Parameter", "Selected", "Fixed Value", "Value"};
     private Vector<Parameter> parameters;
     private SolverBinaries solverBinary;
     private HashMap<Integer, ConfigurationScenarioParameter> configScenarioParameters;
 
     public void setConfigurationScenario(SolverBinaries solverBinary, ConfigurationScenario configurationScenario) throws SQLException {
         parameters = ParameterDAO.getParameterFromSolverId(solverBinary.getIdSolver());
         configScenarioParameters = new HashMap<Integer, ConfigurationScenarioParameter>();
         this.solverBinary = solverBinary;
         Solver solver = SolverDAO.getById(solverBinary.getIdSolver());
         boolean contains = false;
         if (configurationScenario != null) {
             for (SolverBinaries sb : solver.getSolverBinaries()) {
                 if (sb.getIdSolverBinary() == configurationScenario.getIdSolverBinary()) {
                     contains = true;
                     break;
                 }
             }
             if (contains) {
                 for (ConfigurationScenarioParameter param : configurationScenario.getParameters()) {
                     configScenarioParameters.put(param.getIdParameter(), param);
                 }
             }
         }
 
         for (Parameter param : parameters) {
             if (param.isMandatory()) {
                 if (!configScenarioParameters.containsKey(param.getId())) {
                     configScenarioParameters.put(param.getId(), new ConfigurationScenarioParameter(true, null, param));
                 }
             }
         }
 
         fireTableDataChanged();
     }
 
     @Override
     public int getColumnCount() {
         return columns.length;
     }
 
     @Override
     public String getColumnName(int column) {
         return columns[column];
     }
 
     @Override
     public int getRowCount() {
         return parameters == null ? 0 : parameters.size();
     }
 
     @Override
     public Object getValueAt(int row, int column) {
         switch (column) {
             case COL_PARAMETER:
                 return parameters.get(row).getName();
             case COL_SELECTED:
                 return configScenarioParameters.containsKey(parameters.get(row).getId());
             case COL_FIXEDVALUE:
                 ConfigurationScenarioParameter p = configScenarioParameters.get(parameters.get(row).getId());
                 return p == null ? false : !p.isConfigurable();
             case COL_VALUE:
                 p = configScenarioParameters.get(parameters.get(row).getId());
                 return p == null ? "" : (p.getFixedValue() == null ? "" : p.getFixedValue());
             default:
                 return "";
         }
     }
 
     @Override
     public boolean isCellEditable(int row, int column) {
         if (column == COL_SELECTED) {
             if (parameters.get(row).isMandatory()) {
                 return false;
             }
             return true;
         }
         if ((Boolean) getValueAt(row, COL_SELECTED) && column == COL_FIXEDVALUE) {
             if ("instance".equals(parameters.get(row).getName()) || "seed".equals(parameters.get(row).getName())) {
                 return false;
             }
             return true;
         }
         if ((Boolean) getValueAt(row, COL_FIXEDVALUE) && column == COL_VALUE) {
             if (!parameters.get(row).getHasValue()) {
                 return false;
             }
             return true;
         }
         return false;
     }
 
     @Override
     public void setValueAt(Object aValue, int row, int column) {
         if (column == COL_SELECTED) {
             ConfigurationScenarioParameter param = configScenarioParameters.get(parameters.get(row).getId());
             if ((Boolean) aValue) {
                 if (!configScenarioParameters.containsKey(parameters.get(row).getId())) {
                     configScenarioParameters.put(parameters.get(row).getId(), new ConfigurationScenarioParameter(true, null, parameters.get(row)));
                 }
             } else {
                 configScenarioParameters.remove(parameters.get(row).getId());
             }
         } else if (column == COL_FIXEDVALUE) {
             if ((Boolean) aValue) {
                 configScenarioParameters.get(parameters.get(row).getId()).setFixedValue("");
             } else {
                 configScenarioParameters.get(parameters.get(row).getId()).setFixedValue(null);
             }
             configScenarioParameters.get(parameters.get(row).getId()).setConfigurable(!(Boolean) aValue);
         } else if (column == COL_VALUE) {
             configScenarioParameters.get(parameters.get(row).getId()).setFixedValue((String) aValue);
         }
         fireTableRowsUpdated(row, row);
     }
 
     @Override
     public Class<?> getColumnClass(int columnIndex) {
         return getRowCount() == 0 ? String.class : getValueAt(0, columnIndex).getClass();
     }
 
     public SolverBinaries getSolverBinary() {
         return solverBinary;
     }
 
     public HashMap<Integer, ConfigurationScenarioParameter> getConfigScenarioParameters() {
         return configScenarioParameters;
     }
 }
