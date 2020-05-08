 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edacc.manageDB;
 
 import edacc.EDACCManageDBMode;
 import edacc.model.DatabaseConnector;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Parameter;
 import edacc.model.ParameterDAO;
 import edacc.model.Solver;
 import java.sql.SQLException;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Vector;
 
 /**
  *
  * @author gregor
  */
 public class ManageDBParameters implements Observer {
 
     private EDACCManageDBMode gui;
     private ParameterTableModel parameterTableModel;
     private Parameter currentParameter;
 
 
     public ManageDBParameters(EDACCManageDBMode gui, ParameterTableModel parameterTableModel){
         this.gui = gui;
         this.parameterTableModel = parameterTableModel;
         DatabaseConnector.getInstance().addObserver(this);
     }
 
     /**
      * Loads all parameters of the given solvers to the parameter table model.
      * @param solvers
      */
     public void loadParametersOfSolvers(Vector<Solver> solvers) throws SQLException {
         parameterTableModel.clear();
         for (Solver s : solvers) {
             for (Parameter p : ParameterDAO.getParameterFromSolverId(s.getId())) {
                 parameterTableModel.addParameter(s, p);
             }
         }
     }
 
     public void saveParameters(Solver s) throws NoConnectionToDBException, SQLException{
         for(Parameter p : parameterTableModel.getParamtersOfSolver(s)){
             ParameterDAO.saveParameterForSolver(s, p);
         }
     }
 
     public void newParam() {
         parameterTableModel.addParameter(new Parameter());
     }
 
     public void setCurrentSolver(Solver currentSolver) {
         parameterTableModel.setCurrentSolver(currentSolver);      
     }
 
     void showParameter(int index) {
         currentParameter = parameterTableModel.getParameter(index);
         if (currentParameter != null) {
             gui.showParameterDetails(currentParameter);
         }
     }
 
     /**
      * removes the parameters of the current solver.
      */
     public void removeParameters() {
         parameterTableModel.removeParametersOfSolver(parameterTableModel.getCurrentSolver());
         currentParameter = null;
     }
 
     /**
      * Removes the parameters of a specified solver.
      * @param s
      */
     public void removeParameters(Solver s) {
         parameterTableModel.removeParametersOfSolver(s);
         if (s == parameterTableModel.getCurrentSolver())
             currentParameter = null;
     }
 
     public void removeParameter(Parameter param) throws SQLException {
         parameterTableModel.remove(param);
         ParameterDAO.delete(param);
         currentParameter = null;
     }
 
     /**
      * Adds the default parameter "instance" for Solver s.
      * In future, this method will maybe add some more default parameters, each
      * solver should have.
      * @param s
      */
     public void addDefaultParameters(Solver s) {
         Parameter p = new Parameter();
         p.setName("instance");
         p.setPrefix("--instance");
         parameterTableModel.addParameter(s, p);
     }
 
     /**
      * Checks, if a parameter with the given name already exists for the
      * current solver. Won't check the DB for performance reasons!
      * @param name
      * @return
      * @throws NoConnectionToDBException
      * @throws SQLException
      */
     public boolean parameterExists(String name) throws NoConnectionToDBException, SQLException {
         Vector<String> parameterNames = new Vector<String>();
         for (Parameter p : parameterTableModel.getParametersOfCurrentSolver())
             if (p != currentParameter)
                 parameterNames.add(p.getName());
         return parameterNames.contains(name); //|| ParameterDAO.parameterExistsForSolver(name, parameterTableModel.getCurrentSolver());
     }
 
     /**
      * Checks, if a parameter with the given prefix already exists for the
      * current solver. Won't check the DB for performance reasons!
      * @param prefix
      * @return
      * @throws NoConnectionToDBException
      * @throws SQLException
      */
     public boolean parameterPrefixExists(String prefix) throws NoConnectionToDBException, SQLException {
         Vector<String> parameterPrefixes = new Vector<String>();
         for (Parameter p : parameterTableModel.getParametersOfCurrentSolver())
             if (p != currentParameter)
                 parameterPrefixes.add(p.getPrefix());
         return parameterPrefixes.contains(prefix); 
     }
 
     @Override
     public void update(Observable o, Object arg) {
         parameterTableModel.clear();
     }
 
     void rehash(Solver s, Vector<Parameter> params) {
        for (Parameter p : params)
             parameterTableModel.addParameter(s, p);
     }
 
     Vector<Parameter> getParametersOfSolver(Solver s) {
         return parameterTableModel.getParamtersOfSolver(s);
     }
 }
