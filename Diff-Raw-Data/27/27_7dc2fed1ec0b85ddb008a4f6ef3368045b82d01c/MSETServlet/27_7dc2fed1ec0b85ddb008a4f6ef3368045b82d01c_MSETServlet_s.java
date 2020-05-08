 package com.rathesh.codejam2012.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.ThreadManager;
 
 /**
  * The server side implementation of the RPC service.
  */
 public class MSETServlet extends HttpServlet {
 
   private static final long serialVersionUID = -6640809899580890620L;
 
   public static final String DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">";
   public static Socket tradeBookingSocket = null;
   public static PrintWriter outTradeBooking = null;
   public static BufferedReader inTradeBooking = null;
   private static Report report = new Report();
   public static int time;
   public static DataDump dataDump;
   public static final int priceFeedPort = 8211;
   public static final int tradeBookingPort = 8212;
   public static final int WINDOW_SIZE = 100;
 
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException {
 
     // content type should probably depend on the parameter passed...
     // unless we have json no matter what
     response.setContentType("application/json");
     PrintWriter out = response.getWriter();
     if (request.getParameter("go") != null) {
       dataDump = new DataDump();
       time = 0;
       Thread t = ThreadManager.createBackgroundThread(new StockExchange());
       t.start();
     }
     else if (request.getParameter("report") != null) {
      createReportFile(report.toString());
       out.println(report.toString());
     }
     else if (request.getParameter("data") != null) {
       synchronized (dataDump) {
         out.println(dataDump);
       }
       out.flush();
     }
  }
 
  private void createReportFile(String reportJson) {
    /*
     * try { File file = new File("reportJson " + new Date() + ".json");
     * PrintWriter pw = new PrintWriter(file); pw.println(reportJson);
     * pw.flush(); pw.close(); } catch (IOException e) {
     * 
     * }
     */
   }
 
   public static void sendSell(String name, String type) {
     // TODO in here we need to
     // Send 'S' through trade booking socket
     // The exchange responds with a price, keep note of it for silanis :)
     outTradeBooking.println('S');
     outTradeBooking.flush();
 
     String line;
     try {
       line = inTradeBooking.readLine();
       double price = Double.parseDouble(line);
       Transaction transaction = new Transaction(time, "sell", price, name, type);
       report.add(transaction);
     }
     catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
   }
 
   public static void sendBuy(String name, String type) {
     // TODO in here we need to
     // Send 'B' through trade booking socket
     // The exchange responds with a price, keep note of it for silanis :)
     outTradeBooking.println('B');
     outTradeBooking.flush();
     String line;
     try {
       line = inTradeBooking.readLine();
       double price = Double.parseDouble(line);
       Transaction transaction = new Transaction(time, "buy", price, name, type);
       report.add(transaction);
     }
     catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 
 }
