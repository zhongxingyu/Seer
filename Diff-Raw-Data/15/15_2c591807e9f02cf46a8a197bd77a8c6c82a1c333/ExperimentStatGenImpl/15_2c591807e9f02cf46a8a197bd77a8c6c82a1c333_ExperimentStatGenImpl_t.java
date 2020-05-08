 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.wsnusbcollect.experiment.results;
 
 import com.csvreader.CsvWriter;
 import fi.wsnusbcollect.db.ExperimentDataNoise;
 import fi.wsnusbcollect.db.ExperimentDataRevokedCycles;
 import fi.wsnusbcollect.db.ExperimentMetadata;
 import fi.wsnusbcollect.db.ExperimentMultiPingRequest;
 import fi.wsnusbcollect.experiment.ExperimentInit;
 import fi.wsnusbcollect.utils.stats.BoxAndWhiskerCalculator;
 import fi.wsnusbcollect.utils.stats.BoxAndWhiskerItem;
 import fi.wsnusbcollect.utils.stats.Statistics;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import javax.annotation.PostConstruct;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceUnit;
 import javax.persistence.TypedQuery;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.transaction.support.TransactionTemplate;
 
 /**
  * Generates statistics from experiment
  * @author ph4r05
  */
 @Transactional
 @Repository
 public class ExperimentStatGenImpl implements ExperimentStatGen {
     private static final Logger log = LoggerFactory.getLogger(ExperimentStatGenImpl.class);
     
     @PersistenceContext
     private EntityManager em;
     
     @PersistenceUnit
     private EntityManagerFactory emf;
     
     @Autowired
     private JdbcTemplate template;
     
     //@Autowired
     //@Resource(name="experimentInit")
     protected ExperimentInit expInit;
 
     private TransactionTemplate transactionTemplate;
     
     /**
      * One RSSI experiment block duration in ms
      */
     private long experimentBlockDuration=13000;
     
     private DecimalFormat experimentDataFormat;
     
     public static final String CSVDIR="csv/";
     
     @PostConstruct
     public void init(){
         this.experimentDataFormat = new DecimalFormat("0.000000");
     }
     
     /**
      * Generates RSSI stats (CSV files + gnuplot scripts + gnuplot graphs)
      * @param experiment_id 
      */
     
     /**
      * Loads experiment from database, returns entity 
      * 
      * @param experiment_id
      * @return 
      */
     @Override
     public ExperimentMetadata loadExperiment(long experiment_id){
         // load experiment with given id
         return this.em.find(ExperimentMetadata.class, experiment_id);
     }
     
     /**
      * Generates noise reading data to CSV for further processing + 
      * jitter graphs
      * 
      * @param experiment_id 
      */
     @Override
     public void generateNoiseStats(long experiment_id){
         // load experiment with given id
         ExperimentMetadata exp = this.loadExperiment(experiment_id);
         if (exp==null){
             log.error("Cannot find experiment with given ID");
             return;
         }
         log.info("Experiment metadata loaded");
         
         // load all nodes in db
         log.info("Loading rxnodes for noise readings from experiment...");
         List<Integer> rxnodes = this.template.queryForList(
                 "SELECT connectedNode FROM experimentDataNoise WHERE experiment_id=? GROUP BY connectedNode ORDER BY connectedNode", 
                 new Object[] { experiment_id }, 
                 Integer.class);
         log.info("All nodes for noise reads: " + this.listToString(rxnodes, true));
         
         // iterate over all rxnodes and generate data separately
         for(Integer curNode : rxnodes){
             // CSV file generate
             String outputFile = CSVDIR + "noise-E"+experiment_id+"-N"+curNode+".csv";
             log.info("Output file for this record: " + outputFile);
             
             // before we open the file check to see if it already exists
             boolean alreadyExists = new File(outputFile).exists();
             
             // critical job, load incrementaly, get count
             final int recStep = 5000;
             int lastRecord = 0;
             int numOfRecords = this.template.queryForInt("SELECT count(*) FROM experimentDataNoise dn "
                                                           + "WHERE dn.experiment_id=? AND dn.connectedNode=?", new Object[] {experiment_id, curNode});
             
             // LIMIT recStep , lastRecord
             List<ExperimentDataNoise> noise = new ArrayList<ExperimentDataNoise>(numOfRecords);
             for(;lastRecord+1 < numOfRecords;){
                 // now load directly all noise readings from database for particular node
                 List<ExperimentDataNoise> noise2 = 
                     this.template.query("SELECT * FROM experimentDataNoise dn "
                          + "WHERE dn.experiment_id=? AND dn.connectedNode=? "
                          + "ORDER BY miliFromStart "
                              + "LIMIT " + lastRecord + ", " + recStep, 
                              new Object[] {experiment_id, curNode}, 
                              new RowMapper<ExperimentDataNoise>() {
                                 @Override
                                 public ExperimentDataNoise mapRow(ResultSet rs, int i) throws SQLException {
                                     ExperimentDataNoise r = new ExperimentDataNoise();
                                     r.setCounter(rs.getLong("counter"));
                                     r.setMiliFromStart(rs.getLong("miliFromStart"));
                                     r.setNoise(rs.getLong("noise"));                                  
                                     return r;
                                 }
                             });
 
                 log.info("Loaded noise measurements for node: " + curNode + ", csize: "+noise2.size()+", tsize: " + noise.size());                
                 if (noise2.isEmpty()){
                     // nothing to do anymore
                     break;
                 }
                 
                 noise.addAll(noise2);
                 lastRecord += noise2.size();
             }
             
             if (noise.isEmpty()){
                 log.warn("Empty data returned, continue");
                 continue;
             }
             
             List<Integer> noiseVals = new ArrayList<Integer>(noise.size());
             try {
                 // use FileWriter constructor that specifies open for appending
                 CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ';');
                 
                 // headers
                 csvOutput.write("militime");
                 csvOutput.write("counter");
 		csvOutput.write("noise");
 		csvOutput.endRecord();
                 
                 // iterate over records
                 for(ExperimentDataNoise dn : noise){
                     csvOutput.write(String.valueOf(dn.getMiliFromStart()));
                     csvOutput.write(String.valueOf(dn.getCounter()));
                     csvOutput.write(String.valueOf(dn.getNoise()));
                     csvOutput.endRecord();
                     
                     noiseVals.add(Integer.valueOf((int) dn.getNoise()));
                 }
                 
                 // compute stats for noise
                 BoxAndWhiskerItem statItem = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(noiseVals);
                 double stdDev = Statistics.getStdDev(noiseVals.toArray(new Integer[0]));
                 log.info("Noise records for node " + curNode 
                         + " statitem: " + statItem.toString() 
                         + "; stddev=" + stdDev
                         + "; min=" + statItem.getMinRegularValue()
                         + "; max=" + statItem.getMaxRegularValue());
                 
                 csvOutput.close();
                 log.info("CSV file closed, write OK");
             } catch (IOException e) {
                 log.error("Cannot write CSV file, exception occurred", e);
             }
         }
     }
     
     @Override
     public void generateRSSIStats(long experiment_id){
         this.generateRSSITotalStats(experiment_id, true);
     }    
     
     @Override
     public void generateRSSITotalStats(long experiment_id, boolean joinTime){
         /**
          * Main strategy of data loading from database is that we load set of all
          * nodes in experiment to which was send MultiPingRequest. Next we will
          * iterate over transmitting node ID.
          *
          * Next we select all possible configurations from experiment.
          * One configuration is tuple: (txnode, txpower, packetlen).
          *
          * We remove revoked experiments where [time_of_request, time_of_request]
          * overlaps with revoked experiment intervals. Thus we get all requests for
          * one configuration in memory.
          *
          * For every configuration we next iterate over nodes heard this transmission.
          *
          * For complete data descriptor (txnode, txpower, packetlen, rxnode) we
          * load all RSSI values from database to list, calculate statistics and generate
          * store computed values
          */
 
         // bucket to store computed statistical data for each TxRx configuration
         HashMap<ExperimentRSSITxRxConfiguration, BoxAndWhiskerItem> computedStatData = new HashMap<ExperimentRSSITxRxConfiguration, BoxAndWhiskerItem>();
         
         // load experiment with given id
         ExperimentMetadata exp = this.loadExperiment(experiment_id);
         if (exp==null){
             log.error("Cannot find experiment with given ID");
             return;
         }
         log.info("Experiment metadata loaded: " + exp.toString());
         
         // load all revoked experiments
         // strong assumption: result is ordered by miliStart!!!
         TypedQuery<ExperimentDataRevokedCycles> tq = 
                 this.em.createQuery("SELECT rc FROM ExperimentDataRevokedCycles rc "
                                     + "WHERE rc.experiment=:experiment ORDER BY miliStart", ExperimentDataRevokedCycles.class);
         List<ExperimentDataRevokedCycles> revoked = tq.setParameter("experiment", exp).getResultList();
         log.info("Loaded revoked cycles, size: " + revoked.size());
         
         // get all nodes in given experiment
         log.info("Loading txnodes id attended experiment...");
         List<Integer> txnodes = this.template.queryForList(
                 "SELECT node FROM ExperimentMultiPingRequest WHERE experiment_id=? GROUP BY node ORDER BY node", 
                 new Object[] { experiment_id }, 
                 Integer.class);
 
         log.info("Loaded txnodes from experiment, count: " + txnodes.size());
         log.info("Nodes loaded: " + listToString(txnodes, true));
         
         // iterate over nodes and process them
         for (Integer nodeId : txnodes){
             log.info("Loading distinct configurations for txNode: " + nodeId);                    
             
             // now get all possible configurations
             List<ExperimentRSSITxConfiguration> configurations = this.template.query(
             "SELECT DISTINCT node, txpower, packetSize FROM ExperimentMultiPingRequest "
                     + "WHERE node=? AND experiment_id=? ORDER BY node, txpower, packetSize",
             new Object[] { nodeId, experiment_id }, 
             new BeanPropertyRowMapper(ExperimentRSSITxConfiguration.class));
             log.info("Loaded all possible configurations: " + listToString(configurations, false));
             
             // load all requests for particular experiment and txnode
             log.info("Loading requests for txNode: " + nodeId);
             TypedQuery<ExperimentMultiPingRequest> tq2 = 
                 this.em.createQuery("SELECT er FROM ExperimentMultiPingRequest er "
                                     + "WHERE er.experiment=:experiment AND node=:node "
                                     + "ORDER BY node, txpower, packetSize, miliFromStart", ExperimentMultiPingRequest.class);
             List<ExperimentMultiPingRequest> requests = 
                     tq2.setParameter("experiment", exp).setParameter("node", nodeId).getResultList();
             log.info("Loaded all requests from given node, size: " + requests.size());
             
             // buckets categorized by TxCategory. One entry contains list of requests
             HashMap<ExperimentRSSITxConfiguration, LinkedList<ExperimentMultiPingRequest>> expBucket = new HashMap<ExperimentRSSITxConfiguration, LinkedList<ExperimentMultiPingRequest>>();
             
             // 1. delete revoked ones O(M*N) complexity - overkill, but easy programmed
             // I am lazy to implement fast interval overlap decision algorithm..
             // But it don't mind since this is not performance critical section
             // 
             // 2. delete delim requests (delay=0)
             //
             // 3. categorize request to buckets
             Iterator<ExperimentMultiPingRequest> iterator = requests.iterator();
             while(iterator.hasNext()){
                 ExperimentMultiPingRequest cReq = iterator.next();
                 long cReqMiliEnd = cReq.getMiliFromStart() + this.experimentBlockDuration;
                 
                 // delay=0?
                 if (cReq.getDelay()==0 || cReq.getPackets() <=1){
                     iterator.remove();
                     continue;
                 }
                 
                 // iterate over revoked cycles and determine it this one is revoked
                 boolean isRevoked=false;
                 for(ExperimentDataRevokedCycles cRev : revoked){
                     // if cRev.miliend < cReq.start  SKIP
                     if (cRev.getMiliEnd() < cReq.getMiliFromStart()){
                         continue;
                     }
                     
                     // if cRev.miliStart > (cReq.start + duration) =>
                     // => break, sorted increasingly by start, no next record can 
                     // overlap
                     if (cRev.getMiliStart() > (cReq.getMiliFromStart() + this.experimentBlockDuration)){
                         break;                        
                     }
                     
                     // finally test if revoked cycle overlaps, if YES then delete this
                     // request and inform about it
                     // Overlap <=> 
                     //  (left)  (cRev.miliStart >= cReq.miliStart && cRev.miliStart<=cReq.miliEnd)
                     //  (inside) || (cRev.miliStart <= cReqMiliStart && cReqMiliEnd<=cRev.miliEnd)
                     //  (right)  || cRev.miliEnd >= cReq.miliStart && cRev.miliEnd <=cReq.miliEnd
                     //
                     if ((cRev.getMiliStart() >= cReq.getMiliFromStart() && cRev.getMiliStart() <= cReqMiliEnd)
                             || (cRev.getMiliStart() <= cReq.getMiliFromStart() && cReqMiliEnd <= cRev.getMiliEnd())
                             || (cRev.getMiliEnd() >= cReq.getMiliFromStart() && cRev.getMiliEnd() <= cReqMiliEnd)){
                             
                         // in revocation interval.. delete
                         log.info("Revoked experiment occurred: " + cReq.toString());
 
                         iterator.remove();
                         isRevoked=true;
                         break;
                     }
                 } // end of revocation test
                 
                 if (isRevoked) {
                     continue;
                 }
                 
                 // now we got regular experiment cycle, next phase = bucketize
                 // create tx config
                 ExperimentRSSITxConfiguration tmpTxConfig = new ExperimentRSSITxConfiguration();
                 tmpTxConfig.setNode(nodeId);
                 tmpTxConfig.setTxpower(cReq.getTxpower());
                 tmpTxConfig.setPacketSize(cReq.getPacketSize());
                 
                 // create empty if does not exists in bucketmap
                 if (expBucket.containsKey(tmpTxConfig)==false){
                     expBucket.put(tmpTxConfig, new LinkedList<ExperimentMultiPingRequest>());
                 }
                 
                 // get, insert
                 LinkedList<ExperimentMultiPingRequest> tmpList = expBucket.get(tmpTxConfig);
                 tmpList.add(cReq);
             } // end of request iterating
             log.info("Revoked experiments check finished");
             
             // now we have removed revoked cycles, categorized requests by configuration
             // so iterate over configurations
             List<ExperimentRSSITxConfiguration> keyList = new ArrayList<ExperimentRSSITxConfiguration>(expBucket.keySet());
             Collections.sort(keyList, new TxComparator());
             for (ExperimentRSSITxConfiguration curTx : keyList){
                 try {
                     // now can open CSV file, gnu
                     String outputFile = CSVDIR + "rssiTotal-E"+experiment_id+"-Tx"+curTx.getNode()+".csv";
                    String outputFileDetail = CSVDIR + "rssiTotalDetail-E"+experiment_id+"-Tx"+curTx.getNode()+".csv";
                    if (joinTime){
                        outputFile = CSVDIR + "rssiJoin-E"+experiment_id+"-Tx"+curTx.getNode()+".csv";
                        outputFileDetail = CSVDIR + "rssiJoinDetail-E"+experiment_id+"-Tx"+curTx.getNode()+".csv";
                    }
                    
                     log.info("Output file for this record: " + outputFile);
                     log.info("Current configuration: " + curTx);
 
                     // before we open the file check to see if it already exists
                     boolean alreadyExists = new File(outputFile).exists();
                     boolean alreadyExistsDetail = new File(outputFileDetail).exists();
                     
                      // use FileWriter constructor that specifies open for appending
                     CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ';');
                     CsvWriter csvOutputDetail = new CsvWriter(new FileWriter(outputFileDetail, true), ';');
                     if (!alreadyExists){
                         // headers
                         csvOutput.write("txnode");
                         csvOutput.write("txpower");
                         csvOutput.write("plen");
                         csvOutput.write("rxnode");
                         csvOutput.write("request");
                         csvOutput.write("n");
                         csvOutput.write("mean");
                         csvOutput.write("min");
                         csvOutput.write("max");
                         csvOutput.write("q1");
                         csvOutput.write("median");
                         csvOutput.write("q3");
                         csvOutput.write("stddev");
                         csvOutput.write("loss");
                         csvOutput.write("noise_n");
                         csvOutput.write("noise_mean");
                         csvOutput.write("noise_stddev");
                         csvOutput.write("alive_n");
                         csvOutput.endRecord();
                     }
                     
                     if (!alreadyExistsDetail){
                         csvOutputDetail.write("txnode");
                         csvOutputDetail.write("txpower");
                         csvOutputDetail.write("plen");
                         csvOutputDetail.write("rxnode");
                         csvOutputDetail.write("rssi");
                         csvOutputDetail.endRecord();
                     }
                     
                     // get corresponding list
                     
                     LinkedList<ExperimentMultiPingRequest> cReqs = expBucket.get(curTx);
                     
                     int cReqsInBlock = 1;
                     List<Tuple<String, Long>> sqlTimeCriteria = new ArrayList<Tuple<String, Long>>();
                     
                     // time merging to one???
                     if (joinTime){
                         // time is joined here, merge all blocks to one
                         cReqsInBlock = cReqs.size();
                         StringBuilder sb = new StringBuilder();
                         sb.append("(");
                         int tmpC=0;
                         for(ExperimentMultiPingRequest cReq : cReqs){
                             if (tmpC>0){
                                 sb.append(" OR ");
                             }
                             sb.append("(miliFromStart BETWEEN ")
                                     .append(cReq.getMiliFromStart())
                                     .append(" AND ")
                                     .append(cReq.getMiliFromStart() + this.experimentBlockDuration)
                                     .append(") ");
                             tmpC+=1;
                         }
                         sb.append(")");
                         sqlTimeCriteria.add(new Tuple<String, Long>(sb.toString(), 0L));
                     } else {
                         // time is separate
                         cReqsInBlock = 1;
                         for(ExperimentMultiPingRequest cReq : cReqs){
                             StringBuilder sb = new StringBuilder();
                             sb.append("(");
                             sb.append("(miliFromStart BETWEEN ")
                                     .append(cReq.getMiliFromStart())
                                     .append(" AND ")
                                     .append(cReq.getMiliFromStart() + this.experimentBlockDuration)
                                     .append(") ");
                             sb.append(")");
                             sqlTimeCriteria.add(new Tuple<String, Long>(sb.toString(), cReq.getId()));
                         }
                     }
                     
                     
                     long curRequestNum = 0;
                     for(Tuple<String, Long> curTuple  : sqlTimeCriteria){
                         String curSqlTimeCriteria = curTuple.getX();
                         curRequestNum = curTuple.getY();
                         // Now we have one particular configuration, need to load data for
                         // it and compute statistical data
 
                         // end of foreach(rxNode)
                         // separate data block. 
 
                         // GET ALL RXNODES
                         log.info("Loading rxNodes for configuration " + curTx.toString() + "; reqNum: " + curRequestNum);
                         List<Integer> rxNodes = this.template.queryForList(
                                 "SELECT DISTINCT connectedNode FROM experimentDataRSSI WHERE experiment_id=" + experiment_id + " AND " + curSqlTimeCriteria + " ORDER BY connectedNode", Integer.class);
 
                         // now iterate over all RX nodes
                         for(Integer rxNode : rxNodes){
                             ExperimentRSSITxRxConfiguration txrxcon = new ExperimentRSSITxRxConfiguration(curTx, rxNode);
                             log.info("Loading RSSI data for configuration: " + txrxcon.toString());
 
                             // load noise measured by this node in specific interval
                            String sqlNoise = "SELECT noise FROM experimentDataNoise "
                                     + "WHERE experiment_id=" + experiment_id + " AND connectedNode="+rxNode+" AND " + curSqlTimeCriteria;
                             log.info("Loading NOISE data with SQL: " + sqlNoise);
                             List<Integer> noiseData = this.template.queryForList(sqlNoise, Integer.class);
                             
                             // load alive counters
                            String sqlAlive = "SELECT COUNT(*) FROM experimentDataAliveCheck "
                                    + "WHERE experiment_id=" + experiment_id + " AND node="+rxNode+" AND " + curSqlTimeCriteria;
                             log.info("Loading ALIVE data with SQL: " + sqlAlive);
                             Integer aliveN = this.template.queryForInt(sqlAlive);
                             
                             // select all data for RXnode here
                             String SQLData = "SELECT rssi FROM experimentDataRSSI "
                                     + "WHERE experiment_id=" + experiment_id + " AND connectedNode="+rxNode+" AND " + curSqlTimeCriteria;
                             log.info("Loading data with SQL: " + SQLData);
                             List<Integer> rssiData = this.template.queryForList(SQLData, Integer.class);
 
                             // now we finally have some experiment data loaded, compute needed statistical values
                             // build box and whisker item here, configuration
                             BoxAndWhiskerItem noiseItem = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(noiseData);
                             double noiseStdDev = Statistics.getStdDev(noiseData.toArray(new Integer[0]));
                             
                             BoxAndWhiskerItem statItem = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(rssiData);
                             computedStatData.put(txrxcon, statItem);
                             double stdDev = Statistics.getStdDev(rssiData.toArray(new Integer[0]));
                             double loss = 1.0 - (rssiData.size() / (cReqsInBlock * 100.0));
                             log.info("Computed statistical data, " + txrxcon.toString() 
                                     + "; \nStatItem: " + statItem.toString() 
                                     + "; N=" + rssiData.size()
                                     + "; stdDev=" + stdDev
                                     + "; loss=" + loss);
 
                             // here we can build CSV file for given txconfig
                             csvOutput.write(String.valueOf(txrxcon.getNode()));
                             csvOutput.write(String.valueOf(txrxcon.getTxpower()));
                             csvOutput.write(String.valueOf(txrxcon.getPacketSize()));
                             csvOutput.write(String.valueOf(txrxcon.getRxnode()));
                             csvOutput.write(String.valueOf(curRequestNum));
                             csvOutput.write(String.valueOf(rssiData.size()));
                             csvOutput.write(this.getRounded(statItem.getMean()));
                             csvOutput.write(String.valueOf(statItem.getMinRegularValue()));
                             csvOutput.write(String.valueOf(statItem.getMaxRegularValue()));
                             csvOutput.write(String.valueOf(statItem.getQ1()));
                             csvOutput.write(String.valueOf(statItem.getMedian()));
                             csvOutput.write(String.valueOf(statItem.getQ3()));
                             csvOutput.write(this.getRounded(stdDev));
                             csvOutput.write(this.getRounded(loss));
                             
                             csvOutput.write(String.valueOf(noiseData.size()));
                             csvOutput.write(this.getRounded(noiseItem.getMean()));
                             csvOutput.write(this.getRounded(noiseStdDev));
                             csvOutput.write(String.valueOf(aliveN));
                             
                             csvOutput.endRecord();
 
                             // detail CSV
                             for(Integer crssi : rssiData){
                                 csvOutputDetail.write(String.valueOf(txrxcon.getNode()));
                                 csvOutputDetail.write(String.valueOf(txrxcon.getTxpower()));
                                 csvOutputDetail.write(String.valueOf(txrxcon.getPacketSize()));
                                 csvOutputDetail.write(String.valueOf(txrxcon.getRxnode()));
                                 csvOutputDetail.write(String.valueOf(crssi));
                                 csvOutputDetail.endRecord();
                             }
                             csvOutputDetail.flush();
 
                         } // end of foreach(rxNode)
                     
                     } // foreach block
                     
                     csvOutput.flush();
                     csvOutput.close();
                     csvOutputDetail.close();
                     
                 } // end of foreach(txConfiguration)
                 catch (IOException ex) {
                     log.error("Cannot dump data to file, exception", ex);
                 }
             } // end of foreach(txConfiguration)
         } // end of foreach(txnodes)
     }
     
     
     public String getRounded(Number value){
 //        double result = value.doubleValue() * 100000;
 //        result = Math.round(result);
 //        result = result / 100000;
 //        return (result);
         if (Double.class.isInstance(value)){
             final Double v = (Double) value;
             if (Double.isNaN(v)){
                 return "NaN";
             }
         }
         
         return this.experimentDataFormat.format(value);
     }
     
     /**
      * Returns string inlined
      * @param list
      * @return 
      */
     public String listToString(List list, boolean inline){
         if (list==null){
             return "";
         }
         
         StringBuilder sb = new StringBuilder();
         sb.append("[");
         int i=0;
         
         for(Object obj : list){
             if (i>0){
                 sb.append(", ");
             }
             
             sb.append(obj.toString());
             if (inline==false){
                 sb.append("\n");
             }
             i+=1;
         }
         
         sb.append("]");
         return sb.toString();
     }
     
     /**
      * Order:
      * TXnode, txpower, len
      */
     public class TxComparator implements Comparator<ExperimentRSSITxConfiguration>{
         @Override
         public int compare(ExperimentRSSITxConfiguration o1, ExperimentRSSITxConfiguration o2) {
             if(o1.getNode() < o2.getNode()) return -1;
             if(o1.getNode() > o2.getNode()) return 1;
             
             if (o1.getTxpower() > o2.getTxpower()) return -1;
             if (o1.getTxpower() < o2.getTxpower()) return 1;
             
             if (o1.getPacketSize() < o2.getPacketSize()) return -1;
             if (o1.getPacketSize() > o2.getPacketSize()) return 1;
                 
             return 0;
         }
     }
     
     /**
      * Order:
      * TXnode, txpower, len
      */
     public class RequestComparator implements Comparator<ExperimentMultiPingRequest>{
         @Override
         public int compare(ExperimentMultiPingRequest o1, ExperimentMultiPingRequest o2) {
             if (o1.getNode() < o2.getNode()) return -1;
             if (o1.getNode() > o2.getNode()) return 1;
             
             if (o1.getTxpower() > o2.getTxpower()) return -1;
             if (o1.getTxpower() < o2.getTxpower()) return 1;
             
             if (o1.getPacketSize() < o2.getPacketSize()) return -1;
             if (o1.getPacketSize() > o2.getPacketSize()) return 1;
             
             return 0;
         }
     }
     
     public class Tuple<X, Y> {
 
         private X x;
         private Y y;
 
         public Tuple(X x, Y y) {
             this.x = x;
             this.y = y;
         }
 
         public X getX() {
             return x;
         }
 
         public void setX(X x) {
             this.x = x;
         }
 
         public Y getY() {
             return y;
         }
 
         public void setY(Y y) {
             this.y = y;
         }
     }
 }
