 package travellinktech;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import javax.print.PrintService;
 import org.apache.log4j.Logger;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 import spark.*;
 import static spark.Spark.*;
 import travellinktech.ticket_printer.TicketPrintCommand;
 import travellinktech.ticket_printer.PrintServiceLocator;
 
 public class TicketPrintingWebServer {
 
   static final Logger logger = Logger.getLogger(TicketPrintingWebServer.class);
 
   public static void main(String[] args) {
 
     logger.debug("Starting up");
 
     // Set port
     int port = 1337;
     if (args.length > 0) {
       try {
         port = Integer.parseInt(args[0]);
       } catch (NumberFormatException e) {
         System.err.println("Only argument accepted is 'port' and is must be an integer");
         System.exit(1);
       }
       if (args.length > 2) {
         setSecure(args[1], args[2], null, null);
       }
     }
     setPort(port);
 
     get(new Route("/") {
       @Override
       public Object handle(Request request, Response response) {
         return "Hello - I am your ticket printer service!";
       }
     });
 
     get(new Route("/printers") {
       @Override
       public Object handle(Request request, Response response) {
         response.header("Access-Control-Allow-Origin", "*");
 
         List<String> printerList = new ArrayList<String>();
         PrintServiceLocator printService = new PrintServiceLocator();
         for (PrintService p : printService.getAll()) {
           printerList.add(p.getName());
         }
 
         return JSONValue.toJSONString(printerList);
       }
     });
 
     post(new Route("/print-tickets") {
       @Override
       public Object handle(Request request, Response response) {
         response.header("Access-Control-Allow-Origin", "*");
 
         Map commandData = (JSONObject) JSONValue.parse(request.body());
 
         TicketPrintCommand ticketPrintCommand = new TicketPrintCommand();
         ticketPrintCommand.setPrinter(((Long) commandData.get("printer")).intValue());
         ticketPrintCommand.setTicketPageSettingsFromMap((Map) commandData.get("page_format"));
 
         // Big bad ticket data comes in... unchecked, but dealt with in TicketPrintCommand -> TicketListFactory
         @SuppressWarnings("unchecked")
         List<List<Map<String, Object>>> ticketData = (List) commandData.get("tickets");
         ticketPrintCommand.setTicketsFromDataList(ticketData);
 
         try {
           ticketPrintCommand.execute();
         } catch (Exception ex) {
           java.util.logging.Logger.getLogger(TicketPrintingWebServer.class.getName()).log(Level.SEVERE, null, ex);
          halt(500, "Print failed: " + ex.getMessage());
         }
 
         return "";  // OK
       }
     });
 
 
   }
 }
