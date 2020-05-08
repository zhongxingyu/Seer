 package com.taugame.tau.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.sun.grizzly.comet.CometContext;
 import com.sun.grizzly.comet.CometEngine;
 import com.sun.grizzly.comet.CometEvent;
 import com.sun.grizzly.comet.CometHandler;
 import com.taugame.tau.client.TauService;
 import com.taugame.tau.shared.Card;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class TauServiceImpl extends RemoteServiceServlet implements TauService, GameListener {
     private static final Logger logger = Logger.getLogger("grizzly");
     private static final String BEGIN_SCRIPT = "<script type='text/javascript'>\nwindow.parent.";
     private static final String END_SCRIPT = "</script>\n";
     private String contextPath;
     private GameMaster gm;
     private HashMap<String, String> names;
     private HashMap<String, TauCometHandler> handlers;
     private HashMap<TauCometHandler, Boolean> inGame;
     private Integer c = 0;
 
     private static final String JUNK =
         "<!-- Comet is a programming technique that enables web " +
         "servers to send data to the client without having any need " +
         "for the client to request it. -->\n";
 
     public class TauCometHandler implements CometHandler<HttpServletResponse> {
         HttpServletResponse response;
 
         @Override
         public void attach(HttpServletResponse attachment) {
             this.response = attachment;
         }
 
         @Override
         public void onEvent(CometEvent event) throws IOException {
             if (inGame.get(this)) {
                 String output = (String) event.attachment();
                 logger.info("CometEvent.NOTIFY => {}" + output);
                 PrintWriter writer = response.getWriter();
                 writer.println(output);
                 writer.flush();
             }
         }
 
         @Override
         public void onInitialize(CometEvent event) throws IOException {}
 
         @Override
         public void onInterrupt(CometEvent event) throws IOException {
             String script = BEGIN_SCRIPT + "l();\n" + END_SCRIPT;
             logger.info("CometEvent.INTERRUPT => {}" + script);
             PrintWriter writer = response.getWriter();
             writer.println(script);
             writer.flush();
             removeThisFromContext();
         }
 
         @Override
         public void onTerminate(CometEvent event) throws IOException {
             removeThisFromContext();
         }
 
         private void removeThisFromContext() throws IOException {
             response.getWriter().close();
             CometContext context = CometEngine.getEngine().getCometContext(contextPath);
             context.removeCometHandler(this);
         }
     }
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         gm = new GameMaster(this);
         names = new HashMap<String, String>();
         handlers = new HashMap<String, TauCometHandler>();
         inGame = new HashMap<TauCometHandler, Boolean>();
         contextPath = config.getServletContext().getContextPath() + "game";
         CometContext context = CometEngine.getEngine().register(contextPath);
         context.setExpirationDelay(60 * 60 * 1000);
     }
 
     @Override
     synchronized protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
         resp.setContentType("text/html");
         resp.setHeader("Cache-Control", "private");
         resp.setHeader("Pragma", "no-cache");
 
         // For IE, Safari and Chrome, we must output some junk to enable streaming
         PrintWriter writer = resp.getWriter();
         for (int i = 0; i < 10; i++) {
             resp.getWriter().write(JUNK);
         }
         writer.flush();
 
         TauCometHandler handler = new TauCometHandler();
         handler.attach(resp);
         handlers.put(req.getSession().getId(), handler);
         inGame.put(handler, false);
        CometContext context = CometEngine.getEngine().getCometContext(contextPath);
        context.addCometHandler(handler);
     }
 
     synchronized public String join() {
         String name = getName();
         if (name != null) {
            inGame.put(handlers.get(getID()), true);
            gm.joinAs(name);
         }
         return name;
     }
 
     @Override
     synchronized public Boolean joinAs(String name) {
         if (name != null && !name.equals("") && !names.containsValue(name)) {
             names.put(getID(), name);
             inGame.put(handlers.get(getID()), true);
             gm.joinAs(name);
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     synchronized public void setReady(boolean ready) {
         String name = getName();
         if (name != null) {
             gm.setReady(name, ready);
         }
     }
 
     @Override
     synchronized public void submit(Card card1, Card card2, Card card3) {
         String name = getName();
         if (name != null) {
             gm.submit(name, card1, card2, card3);
         }
     }
 
     private String getName() {
         return names.get(getID());
     }
 
     private String getID() {
         return this.getThreadLocalRequest().getSession().getId();
     }
 
     @Override
     public void statusChanged() {
         notifyUpdate(toJson("t", "l"));
     }
 
     @Override
     public void boardChanged(Board board) {
         notifyUpdate(
                 toJson("t", "b") +
                 toJson("b", board));
     }
 
     @Override
     public void gameEnded(List<Entry<String, Integer>> rankings) {
         StringBuilder sb = new StringBuilder("[");
         for (Entry<String, Integer> entry : rankings) {
            sb.append("[\"" + entry.getKey() + "\"," + entry.getValue() + "],");
         }
         sb.append("]");
         notifyUpdate(
                 toJson("t", "e") +
                 toJson("s", sb));
     }
 
     private void notifyUpdate(String json) {
         try {
             CometContext context = CometEngine.getEngine().getCometContext(contextPath);
 //            Set<CometHandler> handlers = context.getCometHandlers();
 //            for (CometHandler handler : handlers) {
                 context.notify(BEGIN_SCRIPT + "u({"
                         + toJson("c", c++)
                         + json
                         + "});\n" + END_SCRIPT);
 //            }
         } catch (IOException e) {
             logger.info(e.toString());
         }
     }
 
     private String toJson(String key, String value) {
         return toJson(key, (Object)("\"" + value + "\""));
     }
 
     private String toJson(String key, Object value) {
         return "\"" + key + "\":" + value + ",";
     }
 
 }
