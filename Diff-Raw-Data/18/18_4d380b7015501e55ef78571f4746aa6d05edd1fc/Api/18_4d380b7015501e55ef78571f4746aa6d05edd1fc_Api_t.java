 package org.bg.server;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
 import com.google.gson.Gson;
 
 import edu.berkeley.mvz.georef.Coordinates;
 import edu.berkeley.mvz.georef.Datum;
 import edu.berkeley.mvz.georef.DistanceUnit;
 import edu.berkeley.mvz.georef.LatLng;
 import edu.berkeley.mvz.georef.Localities;
 import edu.berkeley.mvz.georef.Locality;
 
 /**
  * Servelt for API requests.
  * 
  */
 public class Api extends HttpServlet {
 
   private static final Logger log = Logger.getLogger(Api.class.getName());
 
   private static final long serialVersionUID = -5043453429490500409L;
 
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
       throws IOException {
 
     // Handles a constants request:
     if (req.getRequestURI().endsWith("constants")) {
       String json = new Gson().toJson(ImmutableMap.of("datum", Datum.values(),
           "precision", Coordinates.Precision.values(), "system",
           Coordinates.System.values(), "sources",
           Coordinates.Precision.values(), "units", DistanceUnit.values()));
       resp.setContentType("application/json");
       resp.getWriter().println(json);
       return;
     }
 
     // Required
     double lat = 0, lon = 0, extent = 0;
     Coordinates.Precision coordPrecision = null;
     String type;
 
     // Optional
     Datum datum = Datum.WGS84_WORLD_GEODETIC_SYSTEM_1984;
     Coordinates.Source coordSrc = Coordinates.Source.GAZETTEER;
 
     try {
       // Place type
       type = req.getParameter("type");
       if (type == null || type.trim().length() == 0
           || !type.equalsIgnoreCase("PNO")) {
         throw new IllegalArgumentException(
             "Only Place Name Only type supported");
       }
 
       // Lat/Lon
       String ll = req.getParameter("ll");
       lat = Double.parseDouble(ll.split(",")[0]);
       lon = Double.parseDouble(ll.split(",")[1]);
 
       // Extent
       extent = Double.parseDouble(req.getParameter("extent"));
       String d = req.getParameter("datum");
 
       // Datum
       if (d != null) {
         datum = Datum.fromName(d);
       }
       if (datum == null) {
         throw new IllegalArgumentException("Invalid datum: " + d);
       }
 
       // Coordinate source
       String cs = req.getParameter("source");
       log.warning("cs " + cs);
       if (cs != null) {
         coordSrc = Coordinates.Source.fromName(cs);
       }
       if (coordSrc == null) {
         throw new IllegalArgumentException("Invalid coordinate source: " + cs);
       }
 
       // Coordinate precision:
       coordPrecision = Coordinates.Precision.fromName(req.getParameter("precision"));
       if (coordPrecision == null) {
         throw new IllegalArgumentException("Coordinate precision required");
       }
 
     } catch (Exception e) {
       log.warning(e + "");
       resp.sendError(404);
       return;
     }
 
     // Uses georef API to calculate PR:
     Coordinates c = new Coordinates.Builder(Coordinates.System.DD, datum,
         coordSrc, DistanceUnit.METER, coordPrecision).latitude(lat).longitude(
         lon).build();
 
     log.warning(c + "");
 
     Locality l = Localities.namedPlaceOnly(c, extent);
 
     double error = l.getError(DistanceUnit.METER);
     LatLng ll = l.getCoordinates().getPoint();
     String json = new Gson().toJson(ImmutableMap.of("point", ll, "radius",
         error));
 
     resp.setContentType("application/json");
     resp.getWriter().println(json);
   }
 }
