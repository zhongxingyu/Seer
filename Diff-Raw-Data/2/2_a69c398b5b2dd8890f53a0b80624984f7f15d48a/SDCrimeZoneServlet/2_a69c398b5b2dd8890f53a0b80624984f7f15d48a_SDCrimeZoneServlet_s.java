 package com.crimezone.sd.server.service;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONArray;
 
 import com.beoui.geocell.GeocellManager;
 import com.beoui.geocell.JPALocationCapableRepositorySearchImpl;
 import com.beoui.geocell.LocationCapableRepositorySearch;
 import com.beoui.geocell.model.GeocellQuery;
 import com.beoui.geocell.model.Point;
 import com.crimezone.sd.server.domain.Incident;
 import com.crimezone.sd.server.domain.IncidentSet;
 import com.crimezone.sd.server.domain.IncidentSetTypeEnum;
 import com.crimezone.sd.server.domain.IncidentsOneMile;
 import com.crimezone.sd.server.logic.CrimeDataLoader;
 import com.crimezone.sd.server.logic.CrimeDataReader;
 import com.crimezone.sd.server.logic.JSONUtils;
 import com.crimezone.sd.server.persistence.PMF;
 
 @SuppressWarnings("serial")
 public class SDCrimeZoneServlet extends HttpServlet {
 
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     resp.setContentType("text/plain");
 
     String latString = checkNull(req.getParameter("lat"));
     String lonString = checkNull(req.getParameter("lng"));
     String radString = checkNull(req.getParameter("rad"));
     String yearString = checkNull(req.getParameter("year"));
     String setTypeString = checkNull(req.getParameter("setType"));
     String action = checkNull(req.getParameter("action"));
 
     // TODO: temp
     if (action.compareToIgnoreCase("load") == 0) {
       String serverName = req.getServerName();
       String url;
       if (serverName.compareToIgnoreCase("127.0.0.1") == 0
           || serverName.compareToIgnoreCase("localhost") == 0)
         url = String.format("http://%s:8888/resources/input1.txt", serverName);
       else
         url = "http://sdcrimezone.appspot.com/resources/complete.txt";
       loadIncidentFile(url);
       // TODO: precomputed incident set call here:
       // loadIncidentSetFile(url, IncidentSetTypeEnum.ONE_MILE_ALL_YEAR);
     }
 
     try {
       if (!latString.isEmpty() && !lonString.isEmpty() && !radString.isEmpty()) {
         CrimeDataReader dataReader = CrimeDataReader.getInstance();
 
         Integer radius = Integer.valueOf(radString);
         Double latitude = Double.valueOf(latString);
         Double longitude = Double.valueOf(lonString);
 
         if (!yearString.isEmpty()) {
           Integer year = Integer.valueOf(yearString);
           // TODO: not being used
           // dataReader.findIncidentsByYearAndRadius(year, latitude, longitude,
           // radius);
         }
 
         // Transform it to a point
         Point p = new Point((double) latitude, (double) longitude);
 
         PersistenceManager pm = PMF.get().getPersistenceManager();
         List<Object> params = new ArrayList<Object>();
         GeocellQuery baseQuery = new GeocellQuery("", "", params);
         JSONArray jsonArray = null;
 
         if (setTypeString != null && !setTypeString.isEmpty()) {
           List<IncidentsOneMile> objects = null;
          if (IncidentSetTypeEnum.ONE_MILE_ALL_YEAR.equals(setTypeString)) {
             try {
               LocationCapableRepositorySearch<IncidentsOneMile> searchImpl = new JPALocationCapableRepositorySearchImpl<IncidentsOneMile>(
                   baseQuery, pm, IncidentsOneMile.class);
               objects = GeocellManager
                   .proximityFetch(p, 1000, radius.intValue() * 1609, searchImpl);
               // objects = GeocellManager.proximitySearch(p, 40, 0,
               // IncidentsOneMile.class,
               // baseQuery, pm);
             } catch (Exception e) {
               e.printStackTrace();
               // We catch exception here because we have not configured the
               // PersistentManager (and so the queries won't work)
             }
           }
           jsonArray = JSONUtils
               .convertIncidentSetToJSONArray((List<IncidentSet>) (List<?>) objects);
         } else {
           List<Incident> objects = null;
           try {
             LocationCapableRepositorySearch<Incident> searchImpl = new JPALocationCapableRepositorySearchImpl<Incident>(
                 baseQuery, pm, Incident.class);
             objects = GeocellManager.proximityFetch(p, 1000, radius.intValue() * 1609, searchImpl);
             // objects = GeocellManager.proximitySearch(p, 40, 0,
             // Incident.class,
             // baseQuery, pm);
           } catch (Exception e) {
             e.printStackTrace();
             // We catch exception here because we have not configured the
             // PersistentManager (and so the queries won't work)
           }
           jsonArray = JSONUtils.convertIncidentsToJSONArray(objects);
         }
         if (jsonArray != null) {
           resp.getWriter().println(jsonArray.toString());
         }
       }
     } catch (Exception e) {
       e.printStackTrace();
       resp.getWriter().println("ERROR processing request");
     }
   }
 
   @Override
   public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     String action = checkNull(req.getParameter("action"));
 
     if (action.compareToIgnoreCase("load") == 0) {
       String serverName = req.getServerName();
       String url;
       if (serverName.compareToIgnoreCase("127.0.0.1") == 0
           || serverName.compareToIgnoreCase("localhost") == 0)
         url = String.format("http://%s:8888/resources/complete.txt", serverName);
       else
         url = "http://sdcrimezone.appspot.com/resources/complete.txt";
       loadIncidentFile(url);
     }
 
   }
 
   private void loadIncidentFile(String url) {
     try {
       URL inputData = new URL(url);
       URLConnection urlConn = inputData.openConnection();
       InputStreamReader is = new InputStreamReader(urlConn.getInputStream(), "UTF8");
       BufferedReader in = new BufferedReader(is);
       CrimeDataLoader dataLoader = CrimeDataLoader.getInstance();
       dataLoader.deleteAllIncidents();
       dataLoader.insertIncidents(in);
     } catch (Exception e) {
       e.printStackTrace();
     } finally {
     }
   }
 
   private void loadIncidentSetFile(String url, IncidentSetTypeEnum setType) {
     try {
       URL inputData = new URL(url);
       URLConnection urlConn = inputData.openConnection();
       InputStreamReader is = new InputStreamReader(urlConn.getInputStream(), "UTF8");
       BufferedReader in = new BufferedReader(is);
       CrimeDataLoader dataLoader = CrimeDataLoader.getInstance();
       dataLoader.deleteAllIncidentSets(setType);
       dataLoader.insertIncidentSet(in, setType);
     } catch (Exception e) {
       e.printStackTrace();
     } finally {
     }
   }
 
   private String checkNull(String s) {
     if (s == null) {
       return "";
     }
     return s;
   }
 }
