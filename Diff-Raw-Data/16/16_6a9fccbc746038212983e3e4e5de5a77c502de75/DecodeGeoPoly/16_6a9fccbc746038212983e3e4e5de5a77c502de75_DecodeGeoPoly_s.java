 package org.geo.core.utils;
 
 import org.geo.core.db.DbConnection;
import org.geo.core.db.Geo;
 import org.geo.core.db.GeoPoint;
 import org.geo.core.db.Select;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * https://github.com/scoutant/polyline-decoder
  */
 
 public class DecodeGeoPoly {
 
     private static List<GeoPoint> decodePoly(String encoded) {
         List<GeoPoint> track = new ArrayList<GeoPoint>();
         int index = 0;
         int lat = 0, lng = 0;
 
         while (index < encoded.length()) {
             int b, shift = 0, result = 0;
             try {
                 do {
                     b = encoded.charAt(index++) - 63;
                     result |= (b & 0x1f) << shift;
                     shift += 5;
                 } while (b >= 0x20);
             } catch (StringIndexOutOfBoundsException e) {}
             int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lat += dlat;
 
             shift = 0;
             result = 0;
             try {
                 do {
                     b = encoded.charAt(index++) - 63;
                     result |= (b & 0x1f) << shift;
                     shift += 5;
                 } while (b >= 0x20);
             } catch (StringIndexOutOfBoundsException e) {}
             int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lng += dlng;
 
             GeoPoint p = new GeoPoint( (double)lat/1E5, (double)lng/1E5 );
             track.add(p);
         }
         return track;
     }
 
 
     public static void main(String[] args) {
         final String overlaysTemplate = "_Overlays";
 
         Select type = new Select();
        Geo geoType = type.read("Type");
         int t = 0;
        Connection connection;

        connection = new DbConnection().getConnection();
         for (int i=0; i<geoType.getRowCount(); i++) {
             String typeName = geoType.getValueForKey("Type", i);
             Select overlay = new Select();
             String tableName = typeName + overlaysTemplate;
            Geo geoOverlay = overlay.read(tableName, "Overlay_ID,Points");
 
             for (int o=0; o<geoOverlay.getRowCount(); o++) {
                 t++;
                 String encodedString = geoOverlay.getValueForKey("Points", o);
                 String overlayId = geoOverlay.getValueForKey("Overlay_ID", o);
                 System.out.println(overlayId + ": " + encodedString);
                 String sql = "";
 
                 try {
                     if (encodedString != null && encodedString.length() > 0) {
                         sql = "UPDATE " + tableName + " SET Points=? WHERE Overlay_ID=?";
 
                         System.out.println(sql);
                         PreparedStatement statement = connection.prepareStatement(sql);
                         statement.setString(1, decodePoly(encodedString).toString());
                         statement.setInt(2, Integer.parseInt(overlayId));
                         statement.execute();
                     }
                 } catch (SQLException e) {
                     System.out.println(e.getMessage());
                 }
 
             }
             System.out.println(typeName + ": " + geoOverlay.getRowCount());
         }
         System.out.println(t);
     }

 }
