 package tourist2.storm;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseRichBolt;
 import backtype.storm.tuple.Tuple;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * 汇总并输出游客数
  */
 public class TouristCountBolt extends BaseRichBolt {
     private Logger countLogger = LoggerFactory.getLogger("tourist.count");
//    private AtomicInteger countTourist = new AtomicInteger();
//    private AtomicInteger countWorker = new AtomicInteger();
     private OutputCollector outputCollector;
     private Set touristImsi =  new HashSet();
     private Set workerImsi =  new HashSet();
 
     @Override
     public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
         this.outputCollector = outputCollector;
     }
 
     @Override
     public void execute(Tuple tuple) {
 //        int delta = tuple.getInteger(0);
 //        count.addAndGet(delta);
         long time = tuple.getLong(0);
         String imsi = tuple.getString(1);
         String identity = tuple.getString(2);
         if (identity.equals("tourist")){
             touristImsi.add(imsi);
         } else {
             touristImsi.remove(imsi);
         }
         if (identity.equals("worker")){
             workerImsi.add(imsi);
         } else {
             workerImsi.remove(imsi);
         }
//        System.out.println(String.format("tourist:%s,imsi:%s", touristImsi.size(), StringUtils.join(touristImsi.toArray(),",")));
//        System.out.println(String.format("worker:%s,imsi:%s", workerImsi.size(), StringUtils.join(workerImsi.toArray(),",")));
         if (countLogger.isInfoEnabled()){
            countLogger.info(String.format("tourist:%s,imsi:%s", touristImsi.size(), StringUtils.join(touristImsi.toArray(),",")));
            countLogger.info(String.format("worker:%s,imsi:%s", workerImsi.size(), StringUtils.join(workerImsi.toArray(),",")));
         }
         this.outputCollector.ack(tuple);
     }
 
     @Override
     public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
 
     }
     String getTime(long s) throws ParseException {
         return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(s- TimeZone.getDefault().getRawOffset()));
     }
 }
