 package org.elasticsearch.flume;
 
import com.cloudera.flume.conf.Context;
 import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
 import com.cloudera.flume.core.Event;
 import com.cloudera.flume.core.EventSink;
 import com.cloudera.util.Pair;
 import org.elasticsearch.action.index.IndexResponse;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.client.transport.TransportClient;
 import org.elasticsearch.cluster.ClusterName;
 import org.elasticsearch.common.transport.InetSocketTransportAddress;
 import org.elasticsearch.node.Node;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.elasticsearch.common.xcontent.XContentType;
 import org.elasticsearch.common.xcontent.XContentBuilder;
 import org.elasticsearch.common.xcontent.XContentFactory;
 import org.elasticsearch.common.xcontent.XContentParser;
 
 import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
 import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
 
 public class ElasticSearchSink extends EventSink.Base {
 
     private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchSink.class);
     private static final String DEFAULT_INDEX_NAME = "flume";
     private static final String LOG_TYPE = "log";
     private static final int DEFAULT_ELASTICSEARCH_PORT = 9300;
 
     private Node node;
     private Client client;
     private String indexName = DEFAULT_INDEX_NAME;
 
     private Charset charset = Charset.defaultCharset();
 
 
     private String[] hostNames = new String[0];
     private String clusterName = ClusterName.DEFAULT.value();
 
     // Enabled only for testing
     private boolean localOnly = false;
 
 
     @Override
     public void append(Event e) throws IOException {
         // TODO strategize the name of the index, so that logs based on day can go to individula indexes, allowing simple cleanup by deleting older days indexes in ES
         XContentParser parser = null;
         try {
             byte[] data = e.getBody();
             XContentType contentType = XContentFactory.xContentType(data);
             XContentBuilder builder = jsonBuilder()
                     .startObject()
                     .field("timestamp", new Date(e.getTimestamp()))
                     .field("host", e.getHost())
                     .field("priority", e.getPriority().name());
 
             builder.startObject("message");
             addField(builder, "text", data);
             builder.endObject();
 
             builder.startObject("fields");
             for (Map.Entry<String, byte[]> entry : e.getAttrs().entrySet()) {
                 addField(builder, entry.getKey(), entry.getValue());
             }
             builder.endObject();
 
             IndexResponse response = client.prepareIndex(indexName, LOG_TYPE, null)
                     .setSource(builder)
                     .execute()
                     .actionGet();
         } finally {
             if (parser != null) parser.close();
         }
     }
 
     private void addField(XContentBuilder builder, String fieldName, byte[] data) throws IOException {
         XContentParser parser = null;
         try {
             XContentType contentType = XContentFactory.xContentType(data);
             if (contentType == null) {
                 builder.field(fieldName, new String(data, charset));
             } else {
                 parser = XContentFactory.xContent(contentType).createParser(data);
                 parser.nextToken();
                 builder.copyCurrentStructure(parser);
             }
         } finally {
             if (parser != null) {
                 parser.close();
             }
         }
     }
 
     @Override
     public void close() throws IOException, InterruptedException {
         super.close();
 
         if (client != null) {
             client.close();
         }
         if (node != null) {
             node.close();
         }
     }
 
     @Override
     public void open() throws IOException, InterruptedException {
         super.open();
 
         if (hostNames.length == 0) {
             LOG.info("Using ES AutoDiscovery mode");
             node = nodeBuilder().client(true).clusterName(clusterName).local(localOnly).node();
             client = node.client();
         } else {
             LOG.info("Using provided ES hostnames: " + hostNames.length);
             TransportClient transportClient = new TransportClient();
             for (String esHostName : hostNames) {
                 LOG.info("Adding TransportClient: " + esHostName);
                 transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress(esHostName, DEFAULT_ELASTICSEARCH_PORT));
             }
             client = transportClient;
         }
 
     }
 
 
     /**
      * This is a special function used by the SourceFactory to pull in this class
      * as a plugin sink.
      */
     public static List<Pair<String, SinkBuilder>> getSinkBuilders() {
         List<Pair<String, SinkBuilder>> builders =
                 new ArrayList<Pair<String, SinkBuilder>>();
 
         builders.add(new Pair<String, SinkBuilder>("elasticSearchSink", new ElasticSearchSinkBuilder()));
         return builders;
     }
 
 
     public String getClusterName() {
         return clusterName;
     }
 
     public void setClusterName(String clusterName) {
         this.clusterName = clusterName;
     }
 
     public String getIndexName() {
         return indexName;
     }
 
     public void setIndexName(String indexName) {
         this.indexName = indexName;
     }
 
     public void setHostNames(String[] hostNames) {
         this.hostNames = hostNames;
     }
 
     public String[] getHostNames() {
         return hostNames;
     }
 
     void setLocalOnly(boolean localOnly) {
         this.localOnly = localOnly;
     }
 
     boolean isLocalOnly() {
         return localOnly;
     }
 
 }
