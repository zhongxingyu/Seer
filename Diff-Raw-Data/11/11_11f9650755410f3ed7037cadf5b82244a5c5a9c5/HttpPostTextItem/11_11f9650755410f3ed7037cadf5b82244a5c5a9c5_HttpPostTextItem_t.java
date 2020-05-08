 /**
  * 
  */
 package com.terrapin.emwin.storm;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.httpclient.NoHttpResponseException;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.terrapin.emwin.object.TextItem;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseRichBolt;
 import backtype.storm.tuple.Tuple;
 
 /**
  * @author pcurtis
  *
  */
 public class HttpPostTextItem extends BaseRichBolt {
     public static final Logger log = LoggerFactory.getLogger(HttpPostTextItem.class);
     private Properties props;
     private String postURL = null;
 
     /* (non-Javadoc)
      * @see backtype.storm.task.IBolt#prepare(java.util.Map, backtype.storm.task.TopologyContext, backtype.storm.task.OutputCollector)
      */
     @Override
     public void prepare(Map stormConf, TopologyContext context,
             OutputCollector collector) {
         props = EMWINTopology.loadProperties();
         postURL = (String) props.get("postitem.url");
     }
 
     /* (non-Javadoc)
      * @see backtype.storm.task.IBolt#execute(backtype.storm.tuple.Tuple)
      */
     @Override
     public void execute(Tuple input) {
         TextItem t = (TextItem) input.getValueByField("item");
         HttpClient client = new DefaultHttpClient();
         HttpPost post = new HttpPost(postURL);
         HttpResponse response = null;
         StringEntity postData = null;
         try {
             postData = new StringEntity(new JSONObject(t).toString());
         } catch (UnsupportedEncodingException e) {
            log.warn("Could not create POST data: " + e.getMessage());
         }
         post.setEntity(postData);
         try {
             response = client.execute(post);
             log.info(t.getPacketFileName() + "." + t.getPacketFileType() + " POSTed");
         } catch (ClientProtocolException e) {
            log.warn("POST failed: " + e.getMessage());
         } catch (NoHttpResponseException e) {
            log.warn("Web Service Failure: " + e.getMessage());
         } catch (IOException e) {
            log.warn("I/O Exception: " + e.getMessage());
         }
         
         if (response.getStatusLine().getStatusCode() != 200) {
             log.warn("POST returned status = " + response.getStatusLine());
         }
     }
 
     /* (non-Javadoc)
      * @see backtype.storm.topology.IComponent#declareOutputFields(backtype.storm.topology.OutputFieldsDeclarer)
      */
     @Override
     public void declareOutputFields(OutputFieldsDeclarer declarer) {
  
     }
 
 }
