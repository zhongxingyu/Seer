 package com.mapr.demo.storm;
 
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import net.minidev.json.JSONObject;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseRichBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import backtype.storm.tuple.Values;
 
 import com.google.protobuf.InvalidProtocolBufferException;
 import com.mapr.demo.storm.util.JSONWriter;
 import com.mapr.demo.storm.util.TupleHelpers;
 import com.mapr.demo.twitter.wire.Tweet;
 
 public class TweetBolt extends BaseRichBolt {
 
     private static final long serialVersionUID = -7548234692936382708L;
 
     private OutputCollector collector;
     private Logger log = LoggerFactory.getLogger(TweetBolt.class);
     private AtomicInteger tupleCount = new AtomicInteger();
     private String queryTerm = "{unknown}";
 
     @SuppressWarnings("rawtypes")
     public void prepare(Map stormConf, TopologyContext context,
             OutputCollector collector) {
         this.collector = collector;
     }
 
     public void execute(Tuple tuple) {
         int n = tupleCount.incrementAndGet();
         if (n % 1000 == 0) {
             log.warn("Processed {} tweets", n);
         }
 
         Tweet.TweetMsg t;
 
         try {
             // deserialize tweet
             byte[] msg = tuple.getBinary(0);
             t = Tweet.TweetMsg.parseFrom( msg );
 
             String qt = t.getQuery();
 
             // if first tuple, write out query term to json
             if( n == 1 ) {
                 this.queryTerm = qt;
                 flush(this.queryTerm);
             }
 
             // query changed?
             if( !qt.equals(this.queryTerm) ) {
                 log.info("query changed to '" + qt + "'");
                 this.queryTerm = qt;
                 // send newquery tuple to zero out counts in other bolts
                 collector.emit("tweets", tuple,
                         new Values(TupleHelpers.NEW_QUERY_TOKEN));
                 collector.emit("users", tuple,
                         new Values(TupleHelpers.NEW_QUERY_TOKEN));
             }
 
             // TODO: check t.getTime() here before emitting?
             collector.emit("tweets", tuple, new Values(t.getMsg()));
             collector.emit("users", tuple, new Values(t.getUser()));
 //            collector.emit(tuple, new Values(
 //                    t.getId(),
 //                    t.getMsg(),
 //                    t.getUser(),
 //                    t.getRt(),
 //                    t.getTime(),
 //                    t.getQuery()));
             collector.ack(tuple);
         } catch (InvalidProtocolBufferException ipbe) {
             log.error(ipbe.getLocalizedMessage());
             //ipbe.printStackTrace();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void declareOutputFields(OutputFieldsDeclarer declarer) {
         declarer.declareStream("tweets", new Fields("tweet"));
         declarer.declareStream("users", new Fields("user"));
         //declarer.declare(new Fields("id","tweet","user","rt","time","query"));
     }
 
     private void flush(String query) {
         JSONObject json = new JSONObject();
         json.put("q", query);
         JSONWriter.write(json, "query");
     }
 }
