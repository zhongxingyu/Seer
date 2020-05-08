 package edacc.model;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 
 public class ConfigurationScenarioDAO {
 
     private final static String table = "ConfigurationScenario";
     private final static String table_params = "ConfigurationScenario_has_Parameters";
 
     public static void save(ConfigurationScenario cs) throws SQLException {
         boolean autocommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         try {
             DatabaseConnector.getInstance().getConn().setAutoCommit(false);
             if (cs.isNew()) {
                 PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("INSERT INTO ConfigurationScenario (SolverBinaries_idSolverBinary, Experiment_idExperiment) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                 st.setInt(1, cs.getIdSolverBinary());
                 st.setInt(2, cs.getIdExperiment());
                 st.executeUpdate();
                 ResultSet generatedKeys = st.getGeneratedKeys();
                 if (generatedKeys.next()) {
                     cs.setId(generatedKeys.getInt(1));
                 }
                 generatedKeys.close();
                 st.close();
             } else if (cs.isModified()) {
                 PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("UPDATE ConfigurationScenario SET SolverBinaries_idSolverBinary = ? WHERE Experiment_idExperiment = ?");
                 st.setInt(1, cs.getIdSolverBinary());
                 st.setInt(2, cs.getIdExperiment());
                 st.executeUpdate();
                 st.close();
             }
             cs.setSaved();
             Statement st = DatabaseConnector.getInstance().getConn().createStatement();
             st.executeUpdate("DELETE FROM ConfigurationScenario_has_Parameters WHERE ConfigurationScenario_idConfigurationScenario = " + cs.getId());
             st.close();
             PreparedStatement ps = DatabaseConnector.getInstance().getConn().prepareStatement("INSERT INTO ConfigurationScenario_has_Parameters (ConfigurationScenario_idConfigurationScenario, Parameters_idParameter, configurable, fixedValue) VALUES (?, ?, ?, ?)");
             for (ConfigurationScenarioParameter param : cs.getParameters()) {
                     ps.setInt(1, cs.getId());
                     param.setIdConfigurationScenario(cs.getId());
                     ps.setInt(2, param.getIdParameter());
                     ps.setBoolean(3, param.isConfigurable());
                     ps.setString(4, param.getFixedValue());
                     ps.addBatch();
                     param.setSaved();
             }
             ps.executeBatch();
             ps.close();
             if (cs.getCourse() != null && !cs.getCourse().isSaved()) {
                 PreparedStatement st2 = DatabaseConnector.getInstance().getConn().prepareStatement("INSERT INTO Course (ConfigurationScenario_idConfigurationScenario, Instances_idInstance, seed, `order`) VALUES (?, ?, ?, ?)");
                 for (int i = cs.getCourse().getModifiedIndex(); i < cs.getCourse().getLength(); i++) {
                     st2.setInt(1, cs.getId());
                     InstanceSeed is = cs.getCourse().get(i);
                     st2.setInt(2, is.instance.getId());
                     st2.setInt(3, is.seed);
                     st2.setInt(4, i);
                     st2.addBatch();
                 }
                 st2.executeBatch();
                 st2.close();
                 if (cs.getCourse().getInitialLength() == 0) {
                     Statement stmnt = DatabaseConnector.getInstance().getConn().createStatement();
                     stmnt.executeUpdate("UPDATE ConfigurationScenario SET initial_course_length = " + cs.getCourse().getLength() + " WHERE idConfigurationScenario = " + cs.getId());
                     stmnt.close();
                     cs.getCourse().setInitialLength(cs.getCourse().getLength());
                 }
                 cs.getCourse().setSaved();
            } else {
                 cs.setCourse(new Course());
             }
         } catch (Exception ex) {
             DatabaseConnector.getInstance().getConn().rollback();
             if (ex instanceof SQLException) {
                 throw (SQLException) ex;
             }
         } finally {
             DatabaseConnector.getInstance().getConn().setAutoCommit(autocommit);
         }
     }
 
     private static ConfigurationScenario getConfigurationScenarioFromResultSet(ResultSet rs) throws SQLException {
         ConfigurationScenario cs = new ConfigurationScenario();
         cs.setId(rs.getInt("idConfigurationScenario"));
         cs.setIdExperiment(rs.getInt("Experiment_idExperiment"));
         cs.setIdSolverBinary(rs.getInt("SolverBinaries_idSolverBinary"));
         return cs;
     }
 
     public static ConfigurationScenario getConfigurationScenarioByExperimentId(int idExperiment) throws SQLException {
         PreparedStatement st = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT idConfigurationScenario, SolverBinaries_idSolverBinary, Experiment_idExperiment, idSolver, initial_course_length FROM " + table + " JOIN SolverBinaries ON SolverBinaries.idSolverBinary=SolverBinaries_idSolverBinary WHERE Experiment_idExperiment=?");
         st.setInt(1, idExperiment);
         ResultSet rs = st.executeQuery();
         ConfigurationScenario cs = null;
         int idSolver;
         int initialCourseLength;
         if (rs.next()) {
             cs = getConfigurationScenarioFromResultSet(rs);
             cs.setSaved();
             idSolver = rs.getInt("idSolver");
             initialCourseLength = rs.getInt("initial_course_length");
             rs.close();
             st.close();
         } else {
             rs.close();
             st.close();
             return null;
         }
         Map<Integer, Parameter> solver_parameters = new HashMap<Integer, Parameter>();
         for (Parameter p : ParameterDAO.getParameterFromSolverId(idSolver)) {
             solver_parameters.put(p.getId(), p);
         }
 
         PreparedStatement st2 = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT * FROM " + table_params + " WHERE ConfigurationScenario_idConfigurationScenario=?");
         st2.setInt(1, cs.getId());
         ResultSet rs2 = st2.executeQuery();
         while (rs2.next()) {
             ConfigurationScenarioParameter csp = new ConfigurationScenarioParameter();
             csp.setIdConfigurationScenario(cs.getId());
             csp.setFixedValue(rs2.getString("fixedValue"));
             csp.setIdParameter(rs2.getInt("Parameters_idParameter"));
             csp.setConfigurable(rs2.getBoolean("configurable"));
             csp.setParameter(solver_parameters.get(rs2.getInt("Parameters_idParameter")));
             csp.setSaved();
             cs.getParameters().add(csp);
         }
         rs2.close();
         st2.close();
         
         if (initialCourseLength > 0) {
             PreparedStatement st3 = DatabaseConnector.getInstance().getConn().prepareStatement("SELECT ConfigurationScenario_idConfigurationScenario, Instances_idInstance, seed, `order` FROM Course WHERE ConfigurationScenario_idConfigurationScenario = ? ORDER BY `order` ASC");
             st3.setInt(1, cs.getId());
             ResultSet rs3 = st3.executeQuery();
             Course course = new Course();
             course.setInitialLength(initialCourseLength);
             while (rs3.next()) {
                 course.add(new InstanceSeed(InstanceDAO.getById(rs3.getInt("Instances_idInstance")), rs3.getInt("seed")));
             }
             course.setSaved();
             cs.setCourse(course);
         } else {
             cs.setCourse(new Course());
         }
         
         return cs;
     }
     
     public static boolean configurationScenarioParameterIsSaved(ConfigurationScenarioParameter param) {
         return param.isSaved();
     }
 }
