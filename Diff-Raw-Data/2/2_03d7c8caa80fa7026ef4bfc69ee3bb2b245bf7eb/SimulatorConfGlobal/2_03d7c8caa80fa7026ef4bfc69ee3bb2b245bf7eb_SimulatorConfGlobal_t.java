 package eu.cloudtm.autonomicManager.simulator;
 
 import eu.cloudtm.autonomicManager.commons.EvaluatedParam;
 import eu.cloudtm.autonomicManager.commons.ForecastParam;
 import eu.cloudtm.autonomicManager.commons.Param;
 import eu.cloudtm.autonomicManager.oracles.InputOracle;
 
 /**
  * @author Sebastiano Peluso
  */
 class SimulatorConfGlobal {
 
    private Integer cacheObjects;
    private Integer numServers;
    private Integer numClients;
    private Integer objectReplicationDegree;
    private Long startStatTime;
    private Long averageServerToServerNetDelay;    //TODO Chiedere a Pierangelo
    private Long averageClientToServerNetDelay;    //TODO Chiedere a Pierangelo
 
    SimulatorConfGlobal(InputOracle inputOracle){
 
       cacheObjects = (Integer) inputOracle.getParam(Param.NumberOfEntries);
       numServers = (Integer) inputOracle.getForecastParam(ForecastParam.NumNodes);
      numClients = (Integer) inputOracle.getEvaluatedParam(EvaluatedParam.MAX_ACTIVE_THREADS) * numServers; 
       objectReplicationDegree = (Integer) inputOracle.getForecastParam(ForecastParam.ReplicationDegree);
 
       startStatTime = 0L;
       averageServerToServerNetDelay = 0L;
       averageClientToServerNetDelay = 0L;
 
 
    }
 
    int getNumberOfClients(){
       return numClients;
    }
 
 
 
    @Override
    public String toString() {
 
       return "[Global]\n\n"+
             "cache_objects = "+cacheObjects+"\n"+
             "num_servers = "+numServers+"\n"+
             "num_clients = "+numClients+"\n"+
             "object_replication_degree = "+objectReplicationDegree+"\n"+
             "start_stat_time = "+startStatTime+"\n"+
             "average_server_to_server_net_delay = "+averageServerToServerNetDelay+"\n"+
             "average_client_to_server_net_delay = "+averageClientToServerNetDelay;
 
 
    }
 
 }
