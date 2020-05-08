 package edacc.experiment;
 
 import edacc.model.ComputationMethodDoesNotExistException;
 import edacc.model.ExpResultHasSolvPropertyNotInDBException;
 import edacc.model.Experiment;
 import edacc.model.ExperimentDAO;
 import edacc.model.ExperimentResult;
 import edacc.model.ExperimentResultDAO;
 import edacc.model.ExperimentResultNotInDBException;
 import edacc.model.ResultCodeNotInDBException;
 import edacc.model.StatusCode;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.PropertyNotInDBException;
 import edacc.model.StatusCodeNotInDBException;
 import edacc.properties.PropertyTypeNotExistException;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 
 /**
  * Provides caching for experiment results. Use the higher level methods to get
  * the experiment results. Those methods are synchronized and therefore can be
  * used in threads.
  * updateExperimentResults() will update the local cache for the current experiment
  * and should be called first.
  * @author simon
  */
 public class ExperimentResultCache {
 
     private HashMap<ResultIdentifier, ExperimentResult> resultMap;
     private HashMap<SolverConfigInstanceIdentifier, Integer> runMap;
     private Timestamp lastUpdated;
     private Experiment experiment;
 
     public ExperimentResultCache(Experiment experiment) {
         this.experiment = experiment;
         resultMap = null;
         lastUpdated = new Timestamp(0);
     }
 
     public synchronized int size() {
         return resultMap == null ? 0 : resultMap.size();
     }
 
     public synchronized Collection<ExperimentResult> values() {
         return resultMap.values();
     }
 
     /**
      * Updates the experiment result cache. The resultMap is then synchronized with the database
      * @throws SQLException
      * @throws IOException
      * @throws PropertyTypeNotExistException
      * @throws PropertyNotInDBException
      * @throws NoConnectionToDBException
      * @throws ComputationMethodDoesNotExistException
      */
     public synchronized void updateExperimentResults() throws SQLException, IOException, PropertyTypeNotExistException, PropertyNotInDBException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException, StatusCodeNotInDBException, ResultCodeNotInDBException {
         Timestamp ts = ExperimentResultDAO.getLastModifiedByExperimentId(experiment.getId());
         ArrayList<ExperimentResult> modified = ExperimentResultDAO.getAllModifiedByExperimentId(experiment.getId(), lastUpdated);
         if (resultMap == null) {
             resultMap = new HashMap<ResultIdentifier, ExperimentResult>();
             runMap = new HashMap<SolverConfigInstanceIdentifier, Integer>();
         }
         for (ExperimentResult result : modified) {
             ResultIdentifier key = new ResultIdentifier(result.getSolverConfigId(), result.getInstanceId(), result.getRun());
             if (resultMap.containsKey(key)) {
                 resultMap.remove(key);
             }
             resultMap.put(key, result);
 
             Integer maxRun = runMap.get(new SolverConfigInstanceIdentifier(result.getSolverConfigId(), result.getInstanceId()));
             if (maxRun == null || maxRun < result.getRun()) {
                 runMap.put(new SolverConfigInstanceIdentifier(result.getSolverConfigId(), result.getInstanceId()), result.getRun());
             }
         }
         int count = ExperimentDAO.getJobCount(experiment);
         if (count != resultMap.size()) {
             // full update
             resultMap.clear();
            runMap.clear();
             ArrayList<ExperimentResult> experimentResults = ExperimentResultDAO.getAllByExperimentId(experiment.getId());
             for (ExperimentResult result : experimentResults) {
                 resultMap.put(new ResultIdentifier(result.getSolverConfigId(), result.getInstanceId(), result.getRun()), result);
 
                 Integer maxRun = runMap.get(new SolverConfigInstanceIdentifier(result.getSolverConfigId(), result.getInstanceId()));
                 if (maxRun == null || maxRun < result.getRun()) {
                     runMap.put(new SolverConfigInstanceIdentifier(result.getSolverConfigId(), result.getInstanceId()), result.getRun());
                 } 
             }
         }
         lastUpdated = ts;
     }
     
     public synchronized Integer getNumRuns(int solverConfigId, int instanceId) {
         Integer numRuns = runMap.get(new SolverConfigInstanceIdentifier(solverConfigId, instanceId));
         return numRuns == null ? 0 : (numRuns + 1);
     }
 
     /**
      * Returns all experiment results with any run in the given list for the active experiment.
      * updateExperimentResults() should be called first.
      * @param runs
      * @return arraylist of experiment results
      */
     public synchronized ArrayList<ExperimentResult> getAllByRun(ArrayList<Integer> runs) {
         HashSet<Integer> set = new HashSet<Integer>();
         set.addAll(runs);
         ArrayList<ExperimentResult> res = new ArrayList<ExperimentResult>();
         for (ExperimentResult er : resultMap.values()) {
             if (set.contains(er.getRun())) {
                 res.add(er);
             }
         }
         return res;
     }
 
     /**
      * Returns all experiment results with the specified run for the active experiment.
      * updateExperimentResults() should be called first.
      * @param runs
      * @return arraylist of experiment results
      */
     public synchronized ArrayList<ExperimentResult> getAllByRun(Integer run) {
         ArrayList<ExperimentResult> res = new ArrayList<ExperimentResult>();
         for (ExperimentResult er : resultMap.values()) {
             if (er.getRun() == run) {
                 res.add(er);
             }
         }
         return res;
     }
 
     /**
      * Returns all disjunct runs in an array list.
      * This array list should contain all integers between 0 and numRuns-1 inclusive.
      * updateExperimentResults() should be called first.
      * @return an array list of all runs
      * @throws SQLException
      * @throws IOException
      * @throws PropertyTypeNotExistException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws ComputationMethodDoesNotExistException
      */
     public synchronized ArrayList<Integer> getAllRuns() throws SQLException, IOException, PropertyTypeNotExistException, NoConnectionToDBException, PropertyNotInDBException, ComputationMethodDoesNotExistException {
         // then look for all disjunct runs and return them in an array list
         HashSet<Integer> runs = new HashSet<Integer>();
         for (ExperimentResult er : resultMap.values()) {
             runs.add(er.getRun());
         }
         ArrayList<Integer> res = new ArrayList<Integer>();
         res.addAll(runs);
         return res;
     }
 
     /**
      * Returns a Vector of all ExperimentResults in the current experiment with the solverConfig id and instance id specified
      * @param solverConfigId the solverConfig id of the ExperimentResults
      * @param instanceId the instance id of the ExperimentResults
      * @return returns an empty vector if there are no such ExperimentResults
      */
     public ArrayList<ExperimentResult> getResults(int solverConfigId, int instanceId) {
         return getResults(solverConfigId, instanceId, null);
     }
 
     /**
      * Returns a Vector of all ExperimentResults in the current experiment with the solverConfig id and instance id specified
      * @param solverConfigId the solverConfig id of the ExperimentResults
      * @param instanceId the instance id of the ExperimentResults
      * @return returns an empty vector if there are no such ExperimentResults
      */
     public ArrayList<ExperimentResult> getResults(int solverConfigId, int instanceId, StatusCode[] status) {
         ArrayList<ExperimentResult> res = new ArrayList<ExperimentResult>();
         ExperimentResult result;
         for (int i = 0; i < getNumRuns(solverConfigId, instanceId); i++) {
             if ((result = getResult(solverConfigId, instanceId, i, status)) != null) {
                 res.add(result);
             }
         }
         return res;
     }
 
     /**
      * Returns an ExperimentResult identified by solverConfig id, instance id and run for the current experiment.
      * @param solverConfigId the solverConfig id for the ExperimentResult
      * @param instanceId the instance id for the ExperimentResult
      * @param run the run
      * @return returns null if there is no such ExperimentResult
      */
     public ExperimentResult getResult(int solverConfigId, int instanceId, int run) {
         return getResult(solverConfigId, instanceId, run, null);
     }
 
     /**
      * Returns an ExperimentResult identified by solverConfig id, instance id and run for the current experiment.
      * @param solverConfigId the solverConfig id for the ExperimentResult
      * @param instanceId the instance id for the ExperimentResult
      * @param run the run
      * @return returns null if there is no such ExperimentResult
      */
     public synchronized ExperimentResult getResult(int solverConfigId, int instanceId, int run, StatusCode[] status) {
         ExperimentResult res = resultMap.get(new ResultIdentifier(solverConfigId, instanceId, run));
         if (status != null && res != null) {
             boolean found = false;
             for (StatusCode s : status) {
                 if (res.getStatus().equals(s)) {
                     found = true;
                     break;
                 }
             }
             if (!found) {
                 return null;
             }
         }
         return res;
     }
 
     public synchronized boolean contains(int solverConfigId, int instanceId, int run) {
         return resultMap.containsKey(new ResultIdentifier(solverConfigId, instanceId, run));
     }
 
     /**
      * Used to identify an experiment result in the local cache.
      */
     class ResultIdentifier {
 
         int solverConfigId;
         int instanceId;
         int run;
 
         public ResultIdentifier(int solverConfigId, int instanceId, int run) {
             this.solverConfigId = solverConfigId;
             this.instanceId = instanceId;
             this.run = run;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final ResultIdentifier other = (ResultIdentifier) obj;
             if (this.solverConfigId != other.solverConfigId) {
                 return false;
             }
             if (this.instanceId != other.instanceId) {
                 return false;
             }
             if (this.run != other.run) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 53 * hash + this.solverConfigId;
             hash = 53 * hash + this.instanceId;
             hash = 53 * hash + this.run;
             return hash;
         }
     }
 
     class SolverConfigInstanceIdentifier {
 
         int solverConfigId;
         int instanceId;
 
         public SolverConfigInstanceIdentifier(int solverConfigId, int instanceId) {
             this.solverConfigId = solverConfigId;
             this.instanceId = instanceId;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final SolverConfigInstanceIdentifier other = (SolverConfigInstanceIdentifier) obj;
             if (this.solverConfigId != other.solverConfigId) {
                 return false;
             }
             if (this.instanceId != other.instanceId) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 53 * hash + this.solverConfigId;
             hash = 53 * hash + this.instanceId;
             return hash;
         }
     }
 }
