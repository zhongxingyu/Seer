 package com.renault.eplatofrm.core.xls2json.elasticsearch;
 
 import com.renault.eplatofrm.core.xls2json.json.JsonObject;
 import com.renault.eplatofrm.core.xls2json.json.JsonQuestion;
 import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
 import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
 import org.elasticsearch.action.index.IndexResponse;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.client.transport.TransportClient;
 import org.elasticsearch.common.io.stream.StreamOutput;
 import org.elasticsearch.common.transport.InetSocketTransportAddress;
 import org.elasticsearch.common.xcontent.XContentBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 
 import static org.elasticsearch.common.xcontent.XContentFactory.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: phoenix
  * Date: 31/05/12
  * Time: 19:04
  * To change this template use File | Settings | File Templates.
  */
 public class Indexer {
     private final static Logger logger = LoggerFactory.getLogger(Indexer.class);
 
     private Client client;
 
     public Indexer(Client client) {
         this.client = client;
     }
 
     public void createIndex() throws IOException {
         CreateIndexResponse response = client.admin().indices().prepareCreate("faq-ze").setSettings(createSettings())
                 .addMapping("question", createMapping()).execute().actionGet();
         logger.info("Index and mapping created.");
     }
 
     public XContentBuilder createSettings() throws IOException {
         XContentBuilder settings = jsonBuilder()
             .startObject()
                 .startObject("analysis")
                     .startObject("analyzer")
                         .startObject("response_search_analyzer")
                             .field("type","custom")
                             .field("tokenizer", "responseTokenizer")
                             .field("filter", "lowercase")
                         .endObject()
                         .startObject("response_index_analyzer")
                             .field("type","custom")
                            .field("tokenizer", "standard")
                            .field("filter", "lowercase", "edgeNGram")
                         .endObject()
                     .endObject()
                     .startObject("tokenizer")
                         .startObject("responseTokenizer")
                            .field("type", "lowercase")
                         .endObject()
                     .endObject()
                     .startObject("filter")
                        .startObject("edgeNGram")
                            .field("type", "edgeNGram")
                            .field("side", "front")
                            .field("min_gram", 3)
                            .field("max_gram", 6)
                         .endObject()
                     .endObject()
                 .endObject()
             .endObject();
         return settings;
     }
 
     public XContentBuilder createMapping() throws IOException {
         XContentBuilder mapping = jsonBuilder()
             .startObject()
                 .startObject("question")
                     .startObject("properties")
                         .startObject("responseDescription")
                             .field("type", "string")
                            .field("search_analyzer", "response_search_analyzer")
                            .field("index_analyzer", "response_index_analyzer")
                         .endObject()
                         .startObject("categoryTitle")
                             .field("type", "string")
                             .field("index","not_analyzed")
                         .endObject()
                     .endObject()
                 .endObject()
             .endObject();
         return mapping;
     }
 
     public void index(String indexName, JsonObject object) throws IOException {
 
         XContentBuilder builder = jsonBuilder().startObject();
         object.buildJson(builder);
         builder.endObject();
 
         IndexResponse response = client.prepareIndex("faq-ze", object.getTypeName(), Integer.toString(object.getId()))
                 .setSource(builder).execute().actionGet();
     }
 }
 
 
 /*
 
 curl -XPUT 'http://192.168.6.159:9202/faq-ze/?pretty=1'  -d '
 {
    "settings" : {
       "analysis" : {
          "analyzer" : {
             "search_analyzer : {
                "tokenizer" : "responseTokenizer",
                "filter" : ["lowercase"]
             },
             "index_analyzer" : {
                "tokenizer" : "responseTokenizer",
                "filter" : ["lowercase","ngram"]
             }
          },
          "responseTokenizer" : {
             "filename" : {
                "type" : "whitespace"
             }
          },
          "filter" : {
             "ngram" : {
                "max_gram" : 6,
                "min_gram" : 3,
                "type" : "nGram"
             }
          }
       }
    },
    "mappings" : {
       "question" : {
          "properties" : {
             "responseDescription" : {
                "type" : "string",
                "search_analyzer" : "search_analyzer",
                "index_analyzer" : "index_analyzer"
             }
          }
       }
    }
 }
 '
 */
