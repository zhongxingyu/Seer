 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.apacheextras.camel.examples.rcode.builder;
 
 import org.apacheextras.camel.examples.rcode.aggregator.CalendarAgregationStrategy;
 import org.apacheextras.camel.examples.rcode.aggregator.ConcatenateAggregationStrategy;
 import org.apacheextras.camel.examples.rcode.aggregator.EnrichServiceResponseAggregationStrategy;
 import org.apache.camel.Exchange;
 import org.apache.camel.LoggingLevel;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.dataformat.csv.CsvDataFormat;
 
 import java.io.File;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.camel.model.dataformat.JsonLibrary;
 
 /**
  * @author cemmersb, Sebastian Rühl
  */
 public class RCodeRouteBuilder extends RouteBuilder {
 
   private final static String DEVICE_COMMAND = "jpeg('${exchangeId}.jpg',quality=90);";
   private final static String PLOT_COMMAND = "plot(quantity, type=\"l\");";
   private final static String RETRIEVE_PLOT_COMMAND = "r=readBin('${exchangeId}.jpg','raw',1024*1024); unlink('${exchangeId}.jpg'); r";
   private final static String FINAL_COMMAND = DEVICE_COMMAND + PLOT_COMMAND + "dev.off();" + RETRIEVE_PLOT_COMMAND;
   private final static String HTTP4_RS_CAL_ENDPOINT = "http4://kayaposoft.com/enrico/json/v1.0/";
   private File basePath;
 
   public RCodeRouteBuilder(File basePath) {
     this.basePath = basePath;
   }
 
   @Override
   public void configure() throws Exception {
     configureCsvRoute();
     configureRestCalendarRoute();
     configureRCodeRoute();
     configureGraphRoute();
     wireRoutes();
   }
 
   /**
    * Takes an input as bytes and writes it as an jpeg file.
    */
   private void configureGraphRoute() {
     from("direct:graph")
         .setHeader(Exchange.FILE_NAME, simple("graph${exchangeId}.jpeg"))
         .to("file://" + basePath.getParent() + "/output")
         .log("Generated graph file: ${header.CamelFileNameProduced}");
   }
 
   /**
    * Takes an incoming string argument containing monthly quantities and
    * generates an output graph.
    */
   private void configureRCodeRoute() {
     from("direct:rcode")
         //.setBody(simple("calendar <- c(${});\n")) Das muss sowieso wo anders passieren
         .setBody(simple("quantity <- c(${body});\n" + FINAL_COMMAND))
         .to("log://command?level=DEBUG")
         .to("rcode://localhost:6311/parse_and_eval?bufferSize=4194304")
         .to("log://r_output?level=INFO")
         .setBody(simple("${body.asBytes}"));
   }
 
   /**
    * Configures a CSV route that reads the quantity values from the route and
    * sends the result to the RCode route.
    */
   private void configureCsvRoute() {
     // Configure CSV data format with ';' as separator and skipping of the header
     final CsvDataFormat csv = new CsvDataFormat();
     csv.setDelimiter(";");
     csv.setSkipFirstLine(true);
     // Route takes a CSV file, splits the body and reads the actual values
     from(basePath.toURI() + "?noop=TRUE")
         .log("Unmarshalling CSV file.")
         .unmarshal(csv)
         .to("log://CSV?level=DEBUG")
         .setHeader("id", simple("${exchangeId}"))
         .split().body()
         .to("log://CSV?level=DEBUG")
         // TODO: Create monthly based output instead of taking the yearly figures
         .setBody(simple("${body[1]}"))
         .to("log://CSV?level=DEBUG")
         // Now we aggregate the retrived contents in a big string
         .aggregate(header("id"), new ConcatenateAggregationStrategy()).completionTimeout(3000)
         .log(LoggingLevel.INFO, "Finished the unmarshaling")
         .to("direct:CSV_sink");
   }
 
   private void configureRestCalendarRoute() {
 
     from("direct:REST_CALENDAR")
         // Configure Query Parameters
         .setHeader(Exchange.HTTP_QUERY, constant("action=getPublicHolidaysForYear&year=2012&country=ger&region=Bavaria"))
         .to(HTTP4_RS_CAL_ENDPOINT)
         .convertBodyTo(String.class)
         .to("log://rest_calendar?level=INFO")
         .unmarshal().json(JsonLibrary.Gson, List.class)
         .split().body()
        .setBody(simple("${body[date][year]}/${body[date][month]}/${body[date][day]}"))
         .convertBodyTo(Date.class)
         .aggregate(header("id"), new CalendarAgregationStrategy()).completionTimeout(3000)
         .to("log://date_calendar?level=INFO")
         .end();
   }
 
   /**
    * Wires together the routes.
    */
   private void wireRoutes() {
     from("direct:CSV_sink")
         .enrich("direct:REST_CALENDAR", new EnrichServiceResponseAggregationStrategy())
         .to("direct:rcode")
         .to("direct:graph");
   }
 }
