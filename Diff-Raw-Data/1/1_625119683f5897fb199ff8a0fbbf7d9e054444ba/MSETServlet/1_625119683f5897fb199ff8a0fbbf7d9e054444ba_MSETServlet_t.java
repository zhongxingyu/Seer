 package com.rathesh.codejam2012.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.rathesh.codejam2012.server.strategies.EMAStrategy;
 import com.rathesh.codejam2012.server.strategies.LWMAStrategy;
 import com.rathesh.codejam2012.server.strategies.SMAStrategy;
 import com.rathesh.codejam2012.server.strategies.Strategy;
 import com.rathesh.codejam2012.server.strategies.TMAStrategy;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class MSETServlet extends HttpServlet {
 
   public static final String DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">";
   private Socket tradeBookingSocket = null;
   private static PrintWriter outTradeBooking = null;
   private static BufferedReader inTradeBooking = null;
 
   public static String headWithTitle(String title) {
     return (DOCTYPE + "\n" + "<HTML>\n" + "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n");
   }
 
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException {
 
     // content type should probably depend on the parameter passed...
     // unless we have json no matter what
     response.setContentType("application/json");
     PrintWriter out = response.getWriter();
     if (request.getParameter("go") != null) {
       startStockExchange();
     }
     else if (request.getParameter("report") != null) {
 
     }
     out.println(headWithTitle("Hello WWW") + "<BODY>\n" + "<H1>Hello WWW</H1>\n" + "</BODY></HTML>");
   }
 
   private void startStockExchange() {
     // TODO in here we need to
     Socket priceSocket = null;
     PrintWriter out = null;
     BufferedReader in = null;
 
     // 0. Create strategies and managers
     int slowN = 20, fastN = 5;
     Strategy SMASlow = new SMAStrategy(slowN, false);
     Strategy SMAFast = new SMAStrategy(fastN, true);
     Strategy LWMASlow = new LWMAStrategy(slowN, false);
     Strategy LWMAFast = new LWMAStrategy(fastN, true);
     Strategy EMASlow = new EMAStrategy(slowN, false);
     Strategy EMAFast = new EMAStrategy(fastN, true);
     Strategy TMASlow = new TMAStrategy(slowN, false);
     Strategy TMAFast = new TMAStrategy(fastN, true);
 
     try {
       // Set sockets
       priceSocket = new Socket("localhost", 3000);
       tradeBookingSocket = new Socket("localhost", 3001);
       
       // Get streams
       out = new PrintWriter(priceSocket.getOutputStream(), true);
       in = new BufferedReader(new InputStreamReader(priceSocket.getInputStream()));
       outTradeBooking = new PrintWriter(tradeBookingSocket.getOutputStream(), true);
       inTradeBooking = new BufferedReader(new InputStreamReader(tradeBookingSocket.getInputStream()));
     }
     catch (UnknownHostException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
       return;
     }
     catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
       return;
     }
 
     // 2. Start price feed with 'H'
     // 3. While still receiving prices (not receiving 'C')
     // 4. Update strategies which will update managers, Managers will call
     // sendBuy or Sell
     // 5. Update clock
   }
 
   public static void sendSell(String name, String type) {
     // TODO in here we need to
     // Send 'S' through trade booking socket
     // The exchange responds with a price, keep note of it for silanis :)
     outTradeBooking.print('S');
   
     
   }
 
   public static void sendBuy(String name, String type) {
     // TODO in here we need to
     // Send 'B' through trade booking socket
     // The exchange responds with a price, keep note of it for silanis :)
 
   }
 
 }
