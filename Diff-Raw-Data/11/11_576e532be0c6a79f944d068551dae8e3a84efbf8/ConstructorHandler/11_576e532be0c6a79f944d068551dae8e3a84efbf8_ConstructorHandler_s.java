 package eo.frontend.httpserver;
 
 import java.lang.*;
 import java.util.*;
 import java.io.*;
 
 import javax.servlet.http.*;
 import javax.servlet.*;
 
 import org.eclipse.jetty.server.*;
 import org.eclipse.jetty.server.handler.*;
 
 // ================================================================================
 
 public class ConstructorHandler implements DynamicHandler {
 
 	private SessionManager sm_;
 
 	public void setSM(SessionManager sm) {
 		sm_ = sm;
 	}
 
 
     private static class Route {
         private static class POI {
             long id;
             String name;
             double lat;
             double lng;
 
             public POI(SessionManager.UserRoute.Point poi) {
                 id = poi.poi_id;
 				try {
 
 					Searcher.POI p = Searcher.queryById(poi.poi_id);
 					name = p.name;
                 	lat = p.lat;
                 	lng = p.lng;
 
 				} catch (Exception e) {
                     e.printStackTrace();
                     //id = null;
                 }
 				
             }
 
         }
 
         public int sid;
         public POI[] pois;        
 
         public Route(SessionManager.UserRoute r) {
             sid = r.sid;
             
             pois = new POI[r.ps.length];
 
             for (int i = 0; i < r.ps.length; ++i) {
                 try {
                     pois[i] = new POI(r.ps[i]);
                 } catch (Exception e) {
                     e.printStackTrace();
                     pois[i] = null;
                 }
             }
         }
 	
 
     }
 
     public Response handle(final Request request) {
         try {
             Response r = new Response();
 			
             if ((request.getParameterValues("sid") != null) && (request.getParameterValues("poi_id") != null)) {
 				//we have to add poi to existing user`s route
                 int poi_id = Integer.parseInt(request.getParameterValues("poi_id")[0]);
 				int sid = Integer.parseInt(request.getParameterValues("sid")[0]);
 				
 				//sm_.addPOI(sid, poi_id, 1);
 				SessionManager.UserRoute ur = sm_.getRoute(sid);
				ur.ps[ur.ps.length] = new SessionManager.UserRoute.Point(poi_id, 1);
				sm_.setRoute(ur);
                Route route = new Route(ur);
                 
                 r.result = route;
 				r.aliases.put("route", Route.class);
                 r.aliases.put("poi", Route.POI.class);            
 			} else {
 				if((request.getParameterValues("sid") == null) && (request.getParameterValues("poi_id") != null)) {
 					
 					int sid = sm_.createSession();
 					
 					int poi_id = Integer.parseInt(request.getParameterValues("poi_id")[0]);
 					sm_.addPOI(sid, poi_id, 1);
 					SessionManager.UserRoute ur = sm_.getRoute(sid);
                 	Route route = new Route(ur);
                 
                 	r.result = route;
 					r.aliases.put("route", Route.class);
                 	r.aliases.put("poi", Route.POI.class); 
 				}
 
 			}
 
             return r;
         } catch (Exception e) {
             e.printStackTrace();
             return new Response();
         }
     }
 }
