 package no.bekk.bigdata.elasticsearch;
 
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.client.transport.TransportClient;
 import org.elasticsearch.common.settings.ImmutableSettings;
 import org.elasticsearch.common.settings.Settings;
 import org.elasticsearch.common.transport.InetSocketTransportAddress;
 import org.elasticsearch.index.query.QueryBuilders;
 import org.elasticsearch.search.SearchHit;
 import org.elasticsearch.search.SearchHitField;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: andre b. amundsen
  * Date: 15/07-13
  * Time: 10:17
  * To change this template use File | Settings | File Templates.
  */
 public class FieldsToCSV implements Util {
     Parameters parameters;
     TransportClient client;
     FileWriter fileWriter;
 
     @Override
     public void initialize(Parameters parameters) {
         this.parameters = parameters;
 
         Settings settings = ImmutableSettings.settingsBuilder()
                 .put("cluster.name", parameters.clusterName)
                 .build();
         client = new TransportClient(settings);
         System.out.println("Connecting to: " + parameters.hosts);
         String[] hosts = parameters.hosts.split(",");
 
         for (String s : hosts)
             client.addTransportAddress(new InetSocketTransportAddress(s, parameters.port));
 
         try {
             fileWriter = new FileWriter(parameters.fileName);
         } catch (IOException exception) {
             System.out.print(exception.getMessage());
             throw new RuntimeException("Could not open file for writing!");
         }
     }
 
     @Override
     public void run() {
         if (parameters.logging) {
             System.out.printf(
                     "Starting search with fields: %s. Size: %d.%n",
                     parameters.fields,
                     parameters.size
             );
         }
         SearchResponse response = client.prepareSearch()
                                         .setQuery(QueryBuilders.matchAllQuery())
                                        .addFields(parameters.fields.split(","))
                                         .setSize(parameters.size)
                                         .execute()
                                         .actionGet();
 
         if (parameters.logging) {
             System.out.printf(
                     "Search took %s and returned %d results. Writing to file %s.%n",
                     response.getTook(),
                     response.getHits().hits().length,
                     parameters.fileName
             );
         }
 
         writeToFile(response);
     }
 
     boolean writeToFile(SearchResponse response) {
         try {
             int counter = parameters.repeatCount;
             //Write CSV file headers
             String[] fields = parameters.fields.split(",");
             for (; counter > 0; counter--) {
                 for (int i = 0; i<fields.length; i++){
                     fileWriter.write(fields[i] + counter);
                     if (i+1 != fields.length) fileWriter.write(",");
                 }
                 if (counter > 1) fileWriter.write(",");
             }
             fileWriter.write("\n");
 
             //Write data
             counter = parameters.repeatCount;
             for(SearchHit hit : response.getHits()) {
                 Set<Map.Entry<String, SearchHitField>> set = hit.getFields().entrySet();
                 Iterator<Map.Entry<String, SearchHitField>> iter = set.iterator();
                 while (iter.hasNext()) {
                     SearchHitField field = iter.next().getValue();
                     fileWriter.write(field.getValue().toString());
                     if(iter.hasNext()) fileWriter.write(",");
                 }
                 if (counter > 1) {
                     fileWriter.write(",");
                     counter--;
                 }else {
                     fileWriter.write("\n");
                     counter = parameters.repeatCount;
                 }
             }
             fileWriter.close();
         } catch (IOException exception) {
             System.out.print(exception.getMessage());
             return false;
         }
         return true;
     }
 }
