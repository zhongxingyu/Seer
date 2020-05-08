 package com.amazonaws.services.cloudsearch.main;
 
 import com.amazonaws.services.cloudsearch.integration.aggregation.SdfAggregationStrategy;
 import com.amazonaws.services.cloudsearch.model.sdf.SearchDocumentAdd;
 import com.amazonaws.services.cloudsearch.model.sdf.SearchDocumentAddJson;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.model.dataformat.JsonLibrary;
 import org.apache.camel.spring.Main;
 
 import static org.apache.camel.language.tokenizer.TokenizeLanguage.tokenizeXML;
 
 /**
  * A Main class to run our Camel application standalone
  *
  * @version $Revision: 156 $
  */
 public class SDFUploadMain {
 
     private Main main;
 
     public static void main(String[] args) throws Exception {
         SDFUploadMain example = new SDFUploadMain();
         example.boot();
     }
 
     public void boot() throws Exception {
         // create a Main instance
         main = new Main();
 
         // to load Spring XML file
         main.setApplicationContextUri("META-INF/spring/*.xml");
 
         // enable hangup support so you can press ctrl + c to terminate the JVM
         main.enableHangupSupport();
 
         // add routes
         //main.addRouteBuilder(new MyRouteBuilder());
 
         // run until you terminate the JVM
         System.out.println("Starting Camel. Use ctrl + c to terminate the JVM.\n");
         main.run();
     }
 
     private static class MyRouteBuilder extends RouteBuilder {

         @Override
         public void configure() {
 
             from("file:data/inbox?noop=true")
                     .log("Entering Camel Route for processing SDF file ------->")
                     .split(tokenizeXML("add", "batch"), new SdfAggregationStrategy()).streaming()
                     .unmarshal().jaxb("com.amazonaws.services.cloudsearch.model.sdf")
                     .transform(body(SearchDocumentAdd.class).convertTo(SearchDocumentAddJson.class))
                     .end()
                     .to("bean:cloudSearchService?method=batch")
                     .marshal().json(JsonLibrary.Jackson)
                     .to("stream:out")
                             //.to("activemq:queue:order")
                     .log("<------- Exiting Camel Route for processing SDF file");
         }
     }
 
 }
 
