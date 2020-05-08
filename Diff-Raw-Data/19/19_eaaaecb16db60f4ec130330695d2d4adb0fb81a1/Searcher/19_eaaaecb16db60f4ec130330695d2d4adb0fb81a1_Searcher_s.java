 package eo.frontend.httpserver;
 
 import java.lang.*;
 import java.util.*;
 
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.jdbc.core.JdbcOperations;
 import org.springframework.jdbc.support.rowset.SqlRowSet;
 
 import org.sphx.api.*;
 
 // ================================================================================
 
 public class Searcher {
     private final static String sphinx_host_ = "localhost";
     private final static int sphinx_port_ = 9312;
 
     public static JdbcOperations ops;
 
     public static class POI {
         public long id;
         public String name;
         public String address;
         public String description;
         public String type;
         public String img_url;
 	public double lat, lng;
 	public String src_url;
 
         private final static int ID = 0;
         private final static int NAME = 1;
         private final static int TYPE = 2;
         private final static int ADDRESS = 3;
         private final static int DESCRIPTION = 4;
        private final static int IMG_URL = 5;
        private final static int LAT = 6;
        private final static int LNG = 7;
 		
 
 
         public POI() {
         }
 
         @SuppressWarnings("unchecked")
         public POI(final SphinxMatch match) {
             ArrayList<String> inf = match.attrValues;
 
             id = Integer.parseInt(inf.get(ID));
             name = inf.get(NAME);
             type = inf.get(TYPE);
             address = inf.get(ADDRESS);
 			int lenDescr = inf.get(DESCRIPTION).length();
             description = inf.get(DESCRIPTION).substring(0, lenDescr/2);
 			if (inf.get(IMG_URL).equals(""))  {
 	            img_url = "img/no_image.gif";
 			} else {
 				img_url = inf.get(IMG_URL);
 			}
 			
 
             if (inf.get(LAT).length() > 1) {
                 lat = Double.parseDouble(inf.get(LAT));
                 lng = Double.parseDouble(inf.get(LNG));
             }
 
             /*
             String q = String.format("SELECT src_url FROM poi_raw_descr WHERE poi_id=%d;", id);
 
             System.out.println(String.valueOf(id));
 
             src_url = (String)ops.queryForObject(q, String.class);
             */
 
            src_url = "";
         }
     }
 
     public static class Route {
         public int id;
         public int[] pois;
         public String descr;
 
         public Route(int route_id) {
             String q = String.format("SELECT poi_id FROM route_poi WHERE route_id = %d ORDER BY order_num;",
                                      route_id);
 
             List<Integer> r = ops.queryForList(q, Integer.class);
             pois = new int[r.size()];
 
             for (int i = 0; i < r.size(); ++i) {
                 pois[i] = r.get(i).intValue();
             }
 
             q = String.format("SELECT descr FROM route_recommended WHERE id=%d;", route_id);
             descr = (String)ops.queryForObject(q, String.class);
 
             id = route_id;
         }
     }
 
 
     public static SphinxClient getClient() throws Exception {
         SphinxClient c = new SphinxClient(sphinx_host_, sphinx_port_);
         c.SetMatchMode(SphinxClient.SPH_MATCH_EXTENDED);
         return c;
     }
 
     public static POI[] query(final String keyword) throws Exception {
         SphinxResult qr = getClient().Query(keyword);
         POI[] r = new POI[qr.matches.length];
 
         for (int i = 0; i < r.length; ++i) {
             r[i] = new POI(qr.matches[i]);
         }
 
         return r;
     }
 
     public static POI queryById(int id) throws Exception {
         String q = String.format("@id %d", id);
 
         SphinxResult qr = getClient().Query(q);
 
 		return new POI(qr.matches[0]);
     }
 
     public static int[] queryByType(final String type) throws Exception {
         String q = String.format("@type %s", type);
         SphinxResult qr = getClient().Query(q);
 
         int[] poi_ids = new int[qr.matches.length];
         for (int i = 0; i < qr.matches.length; ++i) {
             poi_ids[i] = Integer.parseInt(qr.matches[i].attrValues.get(0).toString());
         }
 
         return poi_ids;
     }
 
     public static String[] queryTypes() throws Exception {
         String q = String.format("SELECT name FROM poi_type;");
 
         List<String> r = ops.queryForList(q, String.class);
         return r.toArray(new String[1]);
     }
 
     public static Route queryRoute(int id) {
         return new Route(id);
     }
 
     public static Route[] queryRoutes() {
         String q = String.format("SELECT id FROM route_recommended;");
 
         List<Integer> r = ops.queryForList(q, Integer.class);
         Route[] routes = new Route[r.size()];
         for (int i = 0; i < r.size(); ++i) {
             routes[i] = queryRoute(r.get(i));
         }
 
         return routes;
     }
 }
