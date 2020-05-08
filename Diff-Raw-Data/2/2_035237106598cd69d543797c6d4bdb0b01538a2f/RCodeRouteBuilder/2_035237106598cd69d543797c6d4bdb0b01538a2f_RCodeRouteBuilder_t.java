 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.apacheextras.camel.examples.rcode.builder;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.LoggingLevel;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.dataformat.csv.CsvDataFormat;
 
 import java.io.File;
 import org.apacheextras.camel.examples.rcode.processor.MonthlySalesFigureCalcProcessor;
 
 /**
  * @author cemmersb, Sebastian Rühl
  */
 public class RCodeRouteBuilder extends RouteBuilder {
 
   private final static String LIBRARY_COMMAND = "library(forecast);\n"
       + "library(ggplot2);\n"
      + "library(reshape2);\n";
   private final static String GRAPH_COMMAND = "HWplot <- function(ts_object,  n.ahead=4,  CI=.95,  error.ribbon='green', line.size=1){\n"
       + "hw_object <- HoltWinters(ts_object);\n"
       + "forecast <- predict(hw_object, n.ahead=n.ahead, prediction.interval=T, level=CI);\n"
       + "for_values <- data.frame(time=round(time(forecast),  3),\n"
       + " value_forecast=as.data.frame(forecast)$fit,  \n"
       + " dev=as.data.frame(forecast)$upr-as.data.frame(forecast)$fit);\n"
       + "fitted_values <- data.frame(time=round(time(hw_object$fitted),  3),\n"
       + " value_fitted=as.data.frame(hw_object$fitted)$xhat);\n"
       + "actual_values <- data.frame(time=round(time(hw_object$x), 3), Actual=c(hw_object$x));\n"
       + "graphset <- merge(actual_values,  fitted_values,  by='time',  all=TRUE);\n"
       + "graphset <- merge(graphset,  for_values,  all=TRUE,  by='time');\n"
       + "graphset[is.na(graphset$dev),  ]$dev<-0;\n"
       + "graphset$Fitted <- c(rep(NA,  NROW(graphset)-(NROW(for_values) + NROW(fitted_values))), \n"
       + " fitted_values$value_fitted,  for_values$value_forecast);\n"
       + "graphset.melt <- melt(graphset[, c('time', 'Actual', 'Fitted')], id='time');\n"
       + "p <- ggplot(graphset.melt,  aes(x=time,  y=value)) + geom_ribbon(data=graphset, aes(x=time, y=Fitted, ymin=Fitted-dev,  ymax=Fitted + dev),  alpha=.2,  fill=error.ribbon) + geom_line(aes(colour=variable), size=line.size) + geom_vline(x=max(actual_values$time),  lty=2) + xlab('Time') + ylab('Value') + theme(legend.position='bottom') + scale_colour_hue('')\n"
       + "return(p)\n"
       + "}\n";
   private final static String TIME_SERIES_COMMAND = "demand <- ts(sales, start=c(2011,1), frequency=12);\n";
   private final static String PLOT_COMMAND = "graph <- HWplot(demand, n.ahead = 24, error.ribbon = \"red\");\n"
       + "graph <- graph + ggtitle(\"A forecast example based on Holt-Winters\") + theme(plot.title = element_text(lineheight=.8, face=\"bold\"));\n"
       + "graph <- graph + scale_x_continuous(breaks = seq(2011, 2015));\n"
       + "graph <- graph + ylab(\"Demand (Pieces)\");"
       + "plot(graph);\n";
   private final static String DEVICE_COMMAND = "jpeg('${exchangeId}.jpg',quality=100);\n";
   private final static String RETRIEVE_PLOT_COMMAND = "dev.off();r=readBin('${exchangeId}.jpg','raw',1024*1024); unlink('${exchangeId}.jpg'); r";
   private File basePath;
   private static final String DIRECT_CSV_SINK_URI = "direct://csv_sink";
   private static final String DIRECT_RCODE_SOURCE_URI = "direct://rcode_source";
   private static final String DIRECT_GRAPH_FILE_SOURCE_URI = "seda://graph_file_source";
   private static final String DIRECT_GRAPH_JSON_SOURCE_URI = "seda://graph_json_source";
 
   public RCodeRouteBuilder(File basePath) {
     this.basePath = basePath;
   }
 
   @Override
   public void configure() throws Exception {
     configureCsvRoute();
     configureRCodeRoute();
     configureGraphFileRoute();
     configureGraphJsonRoute();
     wireRoutes();
   }
 
   private void configureGraphJsonRoute() {
     // TODO: Export the binary file in a JSON rendert object and write to output folder
     from(DIRECT_GRAPH_JSON_SOURCE_URI)
         // TODO: missing JSON conversion implementation
         //.to("log://graph_json?level=INFO"); // prints currently some awkward byte code
         .log("JSON graph generated")
         .end();
   }
 
   /**
    * Takes an input as bytes and writes it as an jpeg file.
    */
   private void configureGraphFileRoute() {
     from(DIRECT_GRAPH_FILE_SOURCE_URI)
         .setHeader(Exchange.FILE_NAME, simple("graph${exchangeId}.jpeg"))
         .to("file://" + basePath.getParent() + "/output")
         .log("Generated graph file: ${header.CamelFileNameProduced}")
         .end();
   }
 
   /**
    * Takes an incoming string argument containing monthly quantities and
    * generates an output graph.
    */
   private void configureRCodeRoute() {
 
     from(DIRECT_RCODE_SOURCE_URI)
         .setBody(
         simple(LIBRARY_COMMAND
         + GRAPH_COMMAND
         + "sales <- c(${body});\n"
         + TIME_SERIES_COMMAND
         + DEVICE_COMMAND
         + PLOT_COMMAND
         + RETRIEVE_PLOT_COMMAND))
         .to("log://command?level=INFO")
         .to("rcode://localhost:6311/parse_and_eval?bufferSize=4194304")
         .to("log://r_output?level=INFO")
         .setBody(simple("${body.asBytes}"))
         .end();
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
     from(basePath.toURI() + "?noop=TRUE")
         .log("Unmarshalling CSV file.")
         .unmarshal(csv)
         .to("log://CSV?level=DEBUG")
         .process(new MonthlySalesFigureCalcProcessor())
         .to("log://CSV?level=INFO")
         .log(LoggingLevel.INFO, "Finished the unmarshaling")
         .to(DIRECT_CSV_SINK_URI)
         .end();
   }
 
   /**
    * Wires together the routes.
    */
   private void wireRoutes() {
     from(DIRECT_CSV_SINK_URI)
         .to(DIRECT_RCODE_SOURCE_URI)
         .multicast()
         .to(DIRECT_GRAPH_FILE_SOURCE_URI, DIRECT_GRAPH_JSON_SOURCE_URI)
         .end();
   }
 }
