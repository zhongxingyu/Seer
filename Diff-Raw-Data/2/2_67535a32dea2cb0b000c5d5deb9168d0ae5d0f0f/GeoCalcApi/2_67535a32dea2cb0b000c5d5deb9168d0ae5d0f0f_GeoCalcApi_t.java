 package org.bg.server;
 
 import java.io.IOException;
 
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
 public class GeoCalcApi extends HttpServlet {
 
   private static final long serialVersionUID = -5043453429490500409L;
 
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
       throws IOException {
 
     // Extracts ll and e parameters and sends a 404 if error:
     double lat = 0, lon = 0, extent = 0;
     try {
       String ll = req.getParameter("ll");
       lat = Double.parseDouble(ll.split(",")[0]);
       lon = Double.parseDouble(ll.split(",")[1]);
       extent = Double.parseDouble(req.getParameter("e"));
     } catch (Exception e) {
       resp.sendError(404);
     }
 
     // Uses georef API to calculate PR:
     Coordinates c = new Coordinates.Builder(Coordinates.System.DD,
         Datum.WGS84_WORLD_GEODETIC_SYSTEM_1984, Coordinates.Source.GAZETTEER,
         DistanceUnit.METER, Coordinates.Precision.DD_ONE_THOUSANDTH).latitude(
         lat).longitude(lon).build();
     Locality l = Localities.namedPlaceOnly(c, extent);
 
    double error = l.getError(DistanceUnit.METER);
     LatLng ll = l.getCoordinates().getPoint();
     String json = new Gson().toJson(ImmutableMap.of("point", ll, "radius",
         error));
 
     resp.setContentType("application/json");
     resp.getWriter().println(json);
   }
 }
