 package csv;
 
 import eu.cloudtm.autonomicManager.commons.EvaluatedParam;
 import eu.cloudtm.autonomicManager.commons.ForecastParam;
 import eu.cloudtm.autonomicManager.commons.Param;
 import eu.cloudtm.autonomicManager.oracles.InputOracle;
 import parse.radargun.Ispn5_2CsvParser;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * @author Diego Didona, didona@gsd.inesc-id.pt
  *         Date: 27/08/13
  */
 public class CsvInputOracle implements InputOracle {
 
    Ispn5_2CsvParser csvParser;
 
    public CsvInputOracle(Ispn5_2CsvParser csvParser) {
       this.csvParser = csvParser;
    }
 
    public CsvInputOracle(String path) {
       try {
          this.csvParser = new Ispn5_2CsvParser(path);
       } catch (IOException e) {
          throw new IllegalArgumentException("Path " + path + " is nonexistent");
       }
    }
 
    public CsvInputOracle(File f) {
       try {
          this.csvParser = new Ispn5_2CsvParser(f.getAbsolutePath());
       } catch (IOException e) {
          throw new IllegalArgumentException("Path " + f.getAbsolutePath() + " is nonexistent");
       }
    }
 
    @Override
    public Object getParam(Param param) {
       switch (param) {
          case NumNodes:
             return numNodes();
          case ReplicationDegree:
             return replicationDegree();
          case AvgPutsPerWrTransaction:
             return putsPerWrXact();
          case AvgPrepareCommandSize:
             return prepareCommandSize();
          case MemoryInfo_used:
             return memory();
          case AvgGetsPerROTransaction:
             return getsPerRoXact();
          case AvgGetsPerWrTransaction:
             return getsPerWrXact();
          case LocalUpdateTxLocalServiceTime:
             return localUpdateTxLocalServiceTime();
          case LocalUpdateTxPrepareServiceTime:
             return localUpdateTxPrepareServiceTime();
          case LocalUpdateTxCommitServiceTime:
             return localUpdateTxCommitServiceTime();
          case LocalUpdateTxLocalRollbackServiceTime:
             return localUpdateTxLocalRollbackServiceTime();
          case LocalUpdateTxRemoteRollbackServiceTime:
             return localUpdateTxRemoteRollbackServiceTime();
          case RemoteGetServiceTime:
             return remoteGetServiceTime();
          case GMUClusteredGetCommandServiceTime:
             return gmuClusterGetCommandServiceTime();
          case RemoteUpdateTxPrepareServiceTime:
             return remoteUpdateTxPrepareServiceTime();
          case RemoteUpdateTxCommitServiceTime:
             return remoteUpdateTxCommitServiceTime();
          case RemoteUpdateTxRollbackServiceTime:
             return remoteUpateTxRollbackServiceTime();
          case ReadOnlyTxTotalCpuTime:
             return localReadOnlyTxTotalCpuTime();
          case PercentageSuccessWriteTransactions:
             return writePercentage();
          default:
             throw new IllegalArgumentException("Param " + param + " is not present");
       }
 
    }
 
    @Override
    public Object getEvaluatedParam(EvaluatedParam evaluatedParam) {
       switch (evaluatedParam) {
          case MAX_ACTIVE_THREADS:
             return numThreadsPerNode();
          case ACF:
             return acf();
          case CORE_PER_CPU:
             return cpus();
          default:
             throw new IllegalArgumentException("Param " + evaluatedParam + " is not present");
       }
    }
 
    @Override
    public Object getForecastParam(ForecastParam forecastParam) {
       switch (forecastParam) {
          case ReplicationProtocol:
             return replicationProtocol();
          case ReplicationDegree:
            return replicationProtocol();
          default:
             throw new IllegalArgumentException("Param " + forecastParam + " is not present");
       }
    }
 
    /**
     * AD HOC METHODS *
     */
 
    private double numNodes() {
       return csvParser.getNumNodes();
    }
 
    private double replicationDegree() {
       return csvParser.replicationDegree();
    }
 
    private double putsPerWrXact() {
       return csvParser.putsPerWrXact();
    }
 
    private double numThreadsPerNode() {
       return csvParser.numThreads();
    }
 
    private double prepareCommandSize() {
       return csvParser.sizePrepareMsg();
    }
 
    private double acf() {
       return 1.0D / csvParser.numKeys();
    }
 
    private double memory() {
       return 1e-6 * csvParser.mem();
    }
 
    private double cpus() {
       return 2;
    }
 
    private String replicationProtocol() {
       return csvParser.getReplicationProtocol();
    }
 
    private double getsPerRoXact() {
       return csvParser.readsPerROXact();
    }
 
    private double getsPerWrXact() {
       return csvParser.readsPerWrXact();
    }
 
    private double localUpdateTxLocalServiceTime() {
       return csvParser.localServiceTimeWrXact();
    }
 
    private double localUpdateTxPrepareServiceTime() {
       return csvParser.getAvgParam("LocalUpdateTxPrepareServiceTime");
    }
 
    private double localUpdateTxCommitServiceTime() {
       return csvParser.getAvgParam("LocalUpdateTxCommitServiceTime");
    }
 
    private double localUpdateTxLocalRollbackServiceTime() {
       return csvParser.getAvgParam("LocalUpdateTxLocalRollbackServiceTime");
    }
 
    private double localUpdateTxRemoteRollbackServiceTime() {
       return csvParser.getAvgParam("LocalUpdateTxRemoteRollbackServiceTime");
    }
 
    private double localReadOnlyTxTotalCpuTime() {
       return csvParser.localServiceTimeROXact();
    }
 
    private double remoteGetServiceTime() {
       return csvParser.localRemoteGetServiceTime();
    }
 
    private double remoteUpdateTxPrepareServiceTime() {
       return csvParser.remotePrepareServiceTime();
    }
 
    private double remoteUpdateTxCommitServiceTime() {
       return csvParser.remoteCommitCommandServiceTime();
    }
 
    private double remoteUpateTxRollbackServiceTime() {
       return csvParser.remoteRollbackServiceTime();
    }
 
    private double gmuClusterGetCommandServiceTime() {
       return csvParser.remoteRemoteGetServiceTime();
    }
 
    private double writePercentage() {
       return csvParser.writePercentageXact();
    }
 
 
 }
