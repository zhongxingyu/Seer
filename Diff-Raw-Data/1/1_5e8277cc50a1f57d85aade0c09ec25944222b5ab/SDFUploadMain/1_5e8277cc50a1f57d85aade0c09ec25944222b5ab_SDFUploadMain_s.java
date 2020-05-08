 package com.amazonaws.services.cloudsearch.main;
 
 import com.amazonaws.services.cloudsearch.mediation.aggregation.SdfAggregationStrategy;
 import com.amazonaws.services.cloudsearch.model.sdf.SearchDocumentAdd;
 import com.amazonaws.services.cloudsearch.model.sdf.SearchDocumentAddJson;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.model.dataformat.JsonLibrary;
 import org.apache.camel.spring.Main;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import static org.apache.camel.LoggingLevel.*;
 import static org.apache.camel.language.tokenizer.TokenizeLanguage.tokenizeXML;
 
 /**
  * A Main class to run our Camel application standalone
  *
  * @version $Revision: 156 $
  */
 public class SDFUploadMain {
 
     /** The log. */
    //private static Logger LOG = Logger.getLogger(SDFUploadMain.class);
     private static Log LOG = LogFactory.getLog(SDFUploadMain.class);
 
     public static void main(String[] args) throws Exception {
         SDFUploadMain example = new SDFUploadMain();
         example.boot();
     }
 
     public void boot() throws Exception {
         // create a Main instance
         Main main = new Main();
 
         // to load Spring XML file
         main.setApplicationContextUri("META-INF/spring/*.xml");
 
         // enable hangup support so you can press ctrl + c to terminate the JVM
         main.enableHangupSupport();
 
         // add routes
         main.addRouteBuilder(new MyRouteBuilder());
 
         // run until you terminate the JVM
         LOG.info("Starting Camel. Use ctrl + c to terminate the JVM.\n");
         main.run();
     }
 
     private static class MyRouteBuilder extends RouteBuilder {
 
         public void configure() {
 
             from("file:data/inbox?noop=true")
             .log(INFO, "com.amazonaws.services.cloudsearch", "Entering Camel Route for processing SDF file ------->")
             .to("log:com.amazonaws.services.cloudsearch?showAll=true&multiline=true")
             .split(tokenizeXML("add", "batch"), new SdfAggregationStrategy()).streaming()
                 .unmarshal().jaxb("com.amazonaws.services.cloudsearch.model.sdf")
                 .transform(body(SearchDocumentAdd.class).convertTo(SearchDocumentAddJson.class))
             .end()
             .to("bean:cloudSearchService?method=batch")
             .marshal().json(JsonLibrary.Jackson)
             .to("log:com.amazonaws.services.cloudsearch?showBody=true&multiline=true")
             .log(INFO, "com.amazonaws.services.cloudsearch", "<------- Exiting Camel Route for processing SDF file");
         }
     }
 
 }
 
