 package org.tak.servelets;
 
 import org.tak.Server;
 import org.tak.commons.Definable;
 import org.tak.commons.Filter;
 import org.tak.data.Mode;
 import org.tak.impl.ItemDef;
 import org.tak.util.DefinableSearch;
 import org.tak.util.JSON;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 
 /**
  * User: Tommy
  * 6/2/13
  */
 public class LoadName extends HttpServlet {
     private static final Filter<Definable> DEFAULT_FILTER = new Filter<Definable>() {
         @Override
         public boolean accept(Definable o) {
             return false;
         }
     };
     public enum LookupMode {
         NOTED,NOT_NOTED,ANY
     }
 
 
     @Override
     public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("LoadName");
         if (req.getParameter("names")!=null) {
             String[] lookups = req.getParameter("names").split(",");
             for (int i = 0; i < lookups.length; i++) {
                 try {
                     lookups[i] = URLDecoder.decode(lookups[i], "UTF-8");
                     System.out.println(lookups[i]);
                 } catch (UnsupportedEncodingException e) {
                     e.printStackTrace();
                 }
             }
             if (Server.getMode().equals(Mode.ITEM)) {
                 LookupMode lookupMode;
                 if (!req.getParameterMap().containsKey("noted")) {
                     lookupMode = LookupMode.ANY;
                 } else {
                     lookupMode = req.getParameter("noted").equalsIgnoreCase("true") ? LookupMode.NOTED : LookupMode.NOT_NOTED;
                 }
                 switch (lookupMode) {
                     case NOTED:
                         JSON.writeJSON(DefinableSearch.search(Server.getIdentifiables(), getNotedFilter(lookups, true)),resp.getWriter());
                         break;
                     case ANY:
                         JSON.writeJSON(DefinableSearch.searchByName(Server.getIdentifiables(), lookups), resp.getWriter());
                         break;
                     case NOT_NOTED:
                         JSON.writeJSON(DefinableSearch.search(Server.getIdentifiables(), getNotedFilter(lookups, false)),resp.getWriter());
                         break;
                 }
             } else {
                 JSON.writeJSON(DefinableSearch.searchByName(Server.getIdentifiables(), lookups), resp.getWriter());
             }
         } else {
            System.out.println("null");
         }
     }
 
     private Filter<Definable> getNotedFilter(final String[] lookups, final boolean noted) {
 
         if (Server.getMode().equals(Mode.ITEM)) {
             System.out.println("Noted name");
             return new Filter<Definable>() {
                 @Override
                 public boolean accept(Definable definable) {
                     return contains(lookups,definable.getName()) && ((ItemDef)definable).isNoted()==noted;
                 }
             };
         }
         return DEFAULT_FILTER;
     }
     private static boolean contains(String[] strings, String search) {
         for (String string : strings) {
             if (string.equalsIgnoreCase(search))
                 return true;
         }
         return false;
     }
 
 }
