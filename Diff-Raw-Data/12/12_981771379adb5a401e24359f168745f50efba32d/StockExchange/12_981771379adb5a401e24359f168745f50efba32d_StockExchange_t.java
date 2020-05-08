 /**
  * 
  */
 package com.rathesh.codejam2012.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.List;
 import java.util.Observable;
 
 import com.google.common.collect.Lists;
 import com.rathesh.codejam2012.server.manager.Manager;
 import com.rathesh.codejam2012.server.strategies.EMAStrategy;
 import com.rathesh.codejam2012.server.strategies.LWMAStrategy;
 import com.rathesh.codejam2012.server.strategies.SMAStrategy;
 import com.rathesh.codejam2012.server.strategies.Strategy;
 import com.rathesh.codejam2012.server.strategies.TMAStrategy;
 
 /**
  * @author Alex Bourgeois
  * 
  */
 public class StockExchange implements Runnable {
 
   private List<Double> prices;
 
   @Override
   public void run() {
     startStockExchange();
   }
 
   private void startStockExchange() {
     Socket priceSocket = null;
     PrintWriter out = null;
     BufferedReader in = null;
 
     // 0. Create strategies and managers
     int slowN = 20, fastN = 5;
     Strategy SMASlow = new SMAStrategy(slowN, MSETServlet.WINDOW_SIZE, false);
     Strategy SMAFast = new SMAStrategy(fastN, MSETServlet.WINDOW_SIZE, true);
     Strategy LWMASlow = new LWMAStrategy(slowN, MSETServlet.WINDOW_SIZE, false);
     Strategy LWMAFast = new LWMAStrategy(fastN, MSETServlet.WINDOW_SIZE, true);
     Strategy EMASlow = new EMAStrategy(slowN, MSETServlet.WINDOW_SIZE, false);
     Strategy EMAFast = new EMAStrategy(fastN, MSETServlet.WINDOW_SIZE, true);
     Strategy TMASlow = new TMAStrategy(slowN, MSETServlet.WINDOW_SIZE, false);
     Strategy TMAFast = new TMAStrategy(fastN, MSETServlet.WINDOW_SIZE, true);
 
     List<Manager> managers = Lists.newArrayList(new Manager("Manager1"), new Manager("Manager2"),
         new Manager("Manager3"), new Manager("Manager4"), new Manager("Manager5"), new Manager(
             "Manager6"), new Manager("Manager7"), new Manager("Manager8"));
 
     try {
       // 1. Initialize communication
       // Set sockets
       priceSocket = new Socket("localhost", MSETServlet.priceFeedPort);
       MSETServlet.tradeBookingSocket = new Socket("localhost", MSETServlet.tradeBookingPort);
 
       // Get streams
       out = new PrintWriter(priceSocket.getOutputStream(), true);
       in = new BufferedReader(new InputStreamReader(priceSocket.getInputStream()));
       MSETServlet.outTradeBooking = new PrintWriter(
           MSETServlet.tradeBookingSocket.getOutputStream(), true);
       MSETServlet.inTradeBooking = new BufferedReader(new InputStreamReader(
           MSETServlet.tradeBookingSocket.getInputStream()));
 
       // 2. Start price feed with 'H'
       out.println('H');
       out.flush();
       // 3. While still receiving prices (not receiving 'C')
       String token = "";
       char c;
       MSETServlet.dataDump = new DataDump();
       prices = Lists.newArrayList();
 
       while ((c = (char) in.read()) != 'C') {
         while (c != '|') {
           token += c;
           c = (char) in.read();
         }
         double price = Double.parseDouble(token);
 
         // TODO take care of WINDOW_SIZE
         addToPrices(price);
         // 4. Update strategies which will update managers, Managers will call
         // sendBuy or Sell
         SMASlow.update(price);
         SMAFast.update(price);
         LWMASlow.update(price);
         LWMAFast.update(price);
         EMASlow.update(price);
         EMAFast.update(price);
         TMASlow.update(price);
         TMAFast.update(price);
         // 5. Schedule Managers
         if (MSETServlet.time < getSecondsFromHour(11, 0)) {
           ((Observable) SMAFast).addObserver(managers.get(0));
           ((Observable) SMASlow).addObserver(managers.get(0));
           ((Observable) LWMAFast).addObserver(managers.get(0));
           ((Observable) LWMASlow).addObserver(managers.get(0));
           managers.get(0).setIdol(false);
 
           ((Observable) EMAFast).addObserver(managers.get(1));
           ((Observable) EMASlow).addObserver(managers.get(1));
           ((Observable) TMAFast).addObserver(managers.get(1));
           ((Observable) TMASlow).addObserver(managers.get(1));
           managers.get(1).setIdol(false);
 
         }
         else if (MSETServlet.time < getSecondsFromHour(11, 30)) {
           // Put first two managers on break
           ((Observable) SMAFast).deleteObserver(managers.get(0));
           ((Observable) SMASlow).deleteObserver(managers.get(0));
           ((Observable) LWMAFast).deleteObserver(managers.get(0));
           ((Observable) LWMASlow).deleteObserver(managers.get(0));
           managers.get(0).setIdol(true);
 
           ((Observable) EMAFast).deleteObserver(managers.get(1));
           ((Observable) EMASlow).deleteObserver(managers.get(1));
           ((Observable) TMAFast).deleteObserver(managers.get(1));
           ((Observable) TMASlow).deleteObserver(managers.get(1));
           managers.get(1).setIdol(true);
 
           // Manager 3 and 4 should take over their strategies
           ((Observable) SMAFast).addObserver(managers.get(2));
           ((Observable) SMASlow).addObserver(managers.get(2));
           ((Observable) LWMAFast).addObserver(managers.get(2));
           ((Observable) LWMASlow).addObserver(managers.get(2));
           managers.get(2).setIdol(false);
 
           ((Observable) EMAFast).addObserver(managers.get(3));
           ((Observable) EMASlow).addObserver(managers.get(3));
           ((Observable) TMAFast).addObserver(managers.get(3));
           ((Observable) TMASlow).addObserver(managers.get(3));
           managers.get(3).setIdol(false);
 
         }
         else if (MSETServlet.time < getSecondsFromHour(13, 0)) {
           // Manager 3 and 4 now share the load with Manager 1 and 2
           ((Observable) SMAFast).deleteObserver(managers.get(2));
           ((Observable) SMASlow).deleteObserver(managers.get(2));
           ((Observable) EMAFast).deleteObserver(managers.get(3));
           ((Observable) EMASlow).deleteObserver(managers.get(3));
 
           ((Observable) SMAFast).addObserver(managers.get(0));
           ((Observable) SMASlow).addObserver(managers.get(0));
           managers.get(0).setIdol(false);
           ((Observable) EMAFast).addObserver(managers.get(1));
           ((Observable) EMASlow).addObserver(managers.get(1));
           managers.get(1).setIdol(false);
 
         }
         else if (MSETServlet.time < getSecondsFromHour(13, 30)) {
           // Manager 3 and 4 take a break and go home
           ((Observable) LWMAFast).deleteObserver(managers.get(2));
           ((Observable) LWMASlow).deleteObserver(managers.get(2));
           managers.get(2).setIdol(true);
           ((Observable) TMAFast).deleteObserver(managers.get(3));
           ((Observable) TMASlow).deleteObserver(managers.get(3));
           managers.get(3).setIdol(true);
 
           // Manager 1 and 2 take up 3 and 4's load
           ((Observable) LWMAFast).addObserver(managers.get(0));
           ((Observable) LWMASlow).addObserver(managers.get(0));
           ((Observable) TMAFast).addObserver(managers.get(1));
           ((Observable) TMASlow).addObserver(managers.get(1));
 
         }
         else if (MSETServlet.time < getSecondsFromHour(15, 30)) {
           // Manager 1 and 2 finally go home
           ((Observable) SMAFast).deleteObserver(managers.get(0));
           ((Observable) SMASlow).deleteObserver(managers.get(0));
           ((Observable) LWMAFast).deleteObserver(managers.get(0));
           ((Observable) LWMASlow).deleteObserver(managers.get(0));
           managers.get(0).setIdol(true);
 
           ((Observable) EMAFast).deleteObserver(managers.get(1));
           ((Observable) EMASlow).deleteObserver(managers.get(1));
           ((Observable) TMAFast).deleteObserver(managers.get(1));
           ((Observable) TMASlow).deleteObserver(managers.get(1));
           managers.get(1).setIdol(true);
 
           // Manager 5 and 6 start their shift
           ((Observable) SMAFast).addObserver(managers.get(4));
           ((Observable) SMASlow).addObserver(managers.get(4));
           ((Observable) LWMAFast).addObserver(managers.get(4));
           ((Observable) LWMASlow).addObserver(managers.get(4));
           managers.get(4).setIdol(false);
 
           ((Observable) EMAFast).addObserver(managers.get(5));
           ((Observable) EMASlow).addObserver(managers.get(5));
           ((Observable) TMAFast).addObserver(managers.get(5));
           ((Observable) TMASlow).addObserver(managers.get(5));
           managers.get(5).setIdol(false);
 
         }
         else if (MSETServlet.time < getSecondsFromHour(16, 0)) {
           // Manager 5 and 6 takes a break of that Kit Kat bar
           ((Observable) SMAFast).deleteObserver(managers.get(4));
           ((Observable) SMASlow).deleteObserver(managers.get(4));
           ((Observable) LWMAFast).deleteObserver(managers.get(4));
           ((Observable) LWMASlow).deleteObserver(managers.get(4));
           managers.get(4).setIdol(true);
 
           ((Observable) EMAFast).deleteObserver(managers.get(5));
           ((Observable) EMASlow).deleteObserver(managers.get(5));
           ((Observable) TMAFast).deleteObserver(managers.get(5));
           ((Observable) TMASlow).deleteObserver(managers.get(5));
           managers.get(5).setIdol(true);
 
           // Manager 7 and 8 should take over their strategies
           ((Observable) SMAFast).addObserver(managers.get(6));
           ((Observable) SMASlow).addObserver(managers.get(6));
           ((Observable) LWMAFast).addObserver(managers.get(6));
           ((Observable) LWMASlow).addObserver(managers.get(6));
           managers.get(6).setIdol(false);
 
           ((Observable) EMAFast).addObserver(managers.get(7));
           ((Observable) EMASlow).addObserver(managers.get(7));
           ((Observable) TMAFast).addObserver(managers.get(7));
           ((Observable) TMASlow).addObserver(managers.get(7));
           managers.get(7).setIdol(false);
 
         }
         else if (MSETServlet.time < getSecondsFromHour(17, 30)) {
           // Manager 5 and 6 now share the load with Manager 7 and 8
           ((Observable) SMAFast).deleteObserver(managers.get(6));
           ((Observable) SMASlow).deleteObserver(managers.get(6));
           ((Observable) EMAFast).deleteObserver(managers.get(7));
           ((Observable) EMASlow).deleteObserver(managers.get(7));
 
           ((Observable) SMAFast).addObserver(managers.get(4));
           ((Observable) SMASlow).addObserver(managers.get(4));
           managers.get(4).setIdol(false);
           ((Observable) EMAFast).addObserver(managers.get(5));
           ((Observable) EMASlow).addObserver(managers.get(5));
           managers.get(5).setIdol(false);
 
         }
         else {
           // Manager 7 and 8 take a break and go home
           ((Observable) LWMAFast).deleteObserver(managers.get(6));
           ((Observable) LWMASlow).deleteObserver(managers.get(6));
           managers.get(6).setIdol(true);
           ((Observable) TMAFast).deleteObserver(managers.get(7));
           ((Observable) TMASlow).deleteObserver(managers.get(7));
           managers.get(7).setIdol(true);
 
           // Manager 5 and 6 take up 7 and 8's load
           ((Observable) LWMAFast).addObserver(managers.get(4));
           ((Observable) LWMASlow).addObserver(managers.get(4));
           ((Observable) TMAFast).addObserver(managers.get(5));
           ((Observable) TMASlow).addObserver(managers.get(5));
 
         }
 
         // 6. Update clock
         token = "";
 
         synchronized (MSETServlet.dataDump) {
           MSETServlet.dataDump.setTime(MSETServlet.time);
           MSETServlet.dataDump.setPrices(prices);
           MSETServlet.dataDump.setSmaSlow(SMASlow.getAverages());
           MSETServlet.dataDump.setSmaFast(SMAFast.getAverages());
           MSETServlet.dataDump.setLwmaSlow(LWMASlow.getAverages());
           MSETServlet.dataDump.setLwmaFast(LWMAFast.getAverages());
           MSETServlet.dataDump.setEmaSlow(EMASlow.getAverages());
           MSETServlet.dataDump.setEmaFast(EMAFast.getAverages());
           MSETServlet.dataDump.setTmaSlow(TMASlow.getAverages());
           MSETServlet.dataDump.setTmaFast(TMAFast.getAverages());
         }
         MSETServlet.time++;
       }
 
      // Manager 5 and 6 finally go home
      ((Observable) SMAFast).deleteObserver(managers.get(4));
      ((Observable) SMASlow).deleteObserver(managers.get(4));
      ((Observable) LWMAFast).deleteObserver(managers.get(4));
      ((Observable) LWMASlow).deleteObserver(managers.get(4));
      managers.get(4).setIdol(true);

      ((Observable) EMAFast).deleteObserver(managers.get(5));
      ((Observable) EMASlow).deleteObserver(managers.get(5));
      ((Observable) TMAFast).deleteObserver(managers.get(5));
      ((Observable) TMASlow).deleteObserver(managers.get(5));
      managers.get(5).setIdol(true);
       synchronized (MSETServlet.dataDump) {
         MSETServlet.dataDump.finished = true;
       }
     }
     catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 
   public void addToPrices(double d) {
     if (this.prices.size() >= MSETServlet.WINDOW_SIZE) {
       this.prices.remove(0);
     }
     this.prices.add(d);
   }
 
   public int getSecondsFromHour(int hours, int minutes) {
     return ((hours - 9) * 3600) + (minutes * 60);
   }
 }
