 package com.liketivist.fithack;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.util.Log;
 
 public abstract class MapMyRunQuery {
    
    private String _jsonResponse;
    private String _result;
    private Route _route;
    private Activity _activity;
    
    public MapMyRunQuery(Activity a) {
       _activity = a;
    }
    
    public void getRoute(final double routeDistance, final double searchRadius, final double latitude, final double longitude) {
       new Thread(new Runnable() {
          @Override
          public void run() {
             
             try {
                boolean donePaging = false;
                boolean doneRadiusExpansion = false;
                int limit = 100;
                int start_record = 0;
                double radius = searchRadius;
                ArrayList<Route> qualifyingRoutes = new ArrayList<Route>();
                while(!doneRadiusExpansion) {
                   while(!donePaging) {
                      // http://api.mapmyfitness.com/3.1/routes/search_routes?o=json&center_longitude=-122.5471496&center_latitude=45.42289057&radius=0.5&route_type_id=1&start_record=0&limit=100
                      String response = MyHttpGet
                      .get("http://api.mapmyfitness.com/3.1/routes/search_routes?o=json&center_longitude=" + longitude
                            + "&center_latitude=" + latitude + "&radius=" + radius
                            + "&route_type_id=1&start_record="+start_record+"&limit="+limit);
                      JSONObject json = new JSONObject(response);
                      int routeCount = json.getJSONObject("result").getJSONObject("output").getInt("count");
                      JSONArray routes = json.getJSONObject("result").getJSONObject("output").getJSONArray("routes");
                      Log.d("FitHack",String.format("count: %d; length: %d", routeCount, routes.length()));
                      for (int i = 0; i < routes.length(); i++) {
                         double d = routes.getJSONObject(i).getDouble("total_distance");
                         String route_key = routes.getJSONObject(i).getString("route_key");
                         if (d > routeDistance * 0.9 && d < routeDistance * 1.1) {
                            Route route = new Route(d, route_key);
                            qualifyingRoutes.add(route);
                            Log.d("FitHack", String.format("distance: %.2f", d));
                         }
                      }
                      Log.d("FitHack", String.format("routes processed: %d", start_record + routes.length()));
                      if(qualifyingRoutes.size() > 0 || radius > searchRadius * 4) {
                         donePaging = true;
                         doneRadiusExpansion = true;
                      } else if((start_record + routes.length()) < routeCount) {
                         start_record+=limit;
                      } else {
 //                        donePaging = true;
                         start_record = 0;
                         radius *= 2;
                      }
                   }
                }
                Log.d("FitHack", String.format("Found %d qualifying routes", qualifyingRoutes.size()));
                Route theRoute = null;
                if(qualifyingRoutes.size() > 0) {
                  Route r = qualifyingRoutes.get((int) Math.round(Math.random()*qualifyingRoutes.size()));
                   addRoutePoints(r);
                   theRoute = r;
                }
                final Route theRouteFinal = theRoute;
                _activity.runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                      onDone(theRouteFinal);
                   }
                });
             } catch (JSONException e) {
                e.printStackTrace();
             }
          }
 
       }).start();
    }
    
    private Route addRoutePoints(Route route) {
       String response = MyHttpGet.get("http://api.mapmyfitness.com/3.1/routes/get_route?&o=json&route_key="+route.getRouteKey());
       try {
          JSONObject json = new JSONObject(response);
          JSONArray routePoints = json.getJSONObject("result").getJSONObject("output").getJSONArray("points");
          int routePointCnt = 0;
          for (int i = 0; i < routePoints.length(); i++) {
             String lat = routePoints.getJSONObject(i).getString("lat");
             String lng = routePoints.getJSONObject(i).getString("lng");
             route.addRoutePoint(lat, lng);
             routePointCnt++;
             //Log.d("FitHack",String.format("latlng: %s,%s", lat, lng));
          }
          Log.d("FitHack", String.format("route point count: %d", routePointCnt));
       } catch (JSONException e) {
          e.printStackTrace();
       }
       return route;
    }
    
 //   private Route getRouteByKeyOld(String route_key) {
 //      Route route = new Route(0,"");
 //      String response = MyHttpGet.get("http://www.mapmyrun.com/kml?r="+route_key);
 //      boolean inCoordinates = false;
 //      boolean inRoute = false;
 //      String coordinatesString = null;
 //      final ArrayList<RoutePoint> routePoints = new ArrayList<RoutePoint>();
 //      
 //      try {
 //         XmlPullParser parser = Xml.newPullParser();
 //         parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
 ////         parser.setInput(new ByteArrayInputStream(response.getBytes()), null);
 //         parser.setInput(new StringReader(response));
 //         
 //         int eventType = parser.getEventType();
 //         while (eventType != XmlPullParser.END_DOCUMENT) {
 //          if(eventType == XmlPullParser.START_DOCUMENT) {
 ////              Log.d("FitHack","Start document");
 //          } else if(eventType == XmlPullParser.START_TAG) {
 //             if(parser.getName().equals("coordinates")) inCoordinates = true;
 //             if(parser.getName().equals("name")) {
 //                parser.next();
 //                if(parser.getText().equals("Route")) inRoute = true;
 //             }
 ////              Log.d("FitHack","Start tag: "+parser.getName());
 //          } else if(eventType == XmlPullParser.END_TAG) {
 //             if(parser.getName().equals("coordinates")) {
 //                inCoordinates = false;
 //                inRoute = false;
 //             }
 ////              Log.d("FitHack","End tag: "+parser.getName());
 //          } else if(eventType == XmlPullParser.TEXT) {
 //             if(inCoordinates && inRoute) {
 //                coordinatesString = parser.getText();
 ////                Log.d("FitHack","Text: "+coordinatesString);
 //             }
 //          }
 //          eventType = parser.next();
 //         }
 ////         Log.d("FitHack","End document");
 //      } catch (Exception e) {
 //         
 //      }
 //
 //      coordinatesString = coordinatesString.replaceAll("\n", "|").replaceAll("\\s+", "");
 //      String[] cArray = coordinatesString.split("\\|");
 //      for(String s : cArray) {
 //         String[] llArray = s.split(",");
 ////         Log.d("FitHack",s);
 //         if(llArray.length > 1) {
 //            Log.d("FitHack", String.format("%s,%s", llArray[1], llArray[0]));
 //            route.addRoutePoint(llArray[1], llArray[0]);
 //            routePoints.add(new RoutePoint(llArray[1], llArray[0]));
 //         }
 //      }
 //      return route;
 //   }
 
    
    
    public abstract void onDone(Route route);
    
    
 // new MyHttpGet("http://api.mapmyfitness.com/3.1/geocode/get_location?&o=json&location=97015") {
 // @Override
 // public void onResponseReceived(String response) {
 //    // TODO Auto-generated method stub
 //    Log.d("FitHack", response);
 //    _jsonResponse = response;
 //    JSONObject json=null;
 //    try {
 //       json=new JSONObject(_jsonResponse);
 //       double la = json.getJSONObject("result").getJSONObject("output").getJSONObject("geocoded_address").getDouble("Latitude");
 //       double lo = json.getJSONObject("result").getJSONObject("output").getJSONObject("geocoded_address").getDouble("Longitude");
 //       onDone(String.format("%.2f,%.2f", la, lo));
 //       //Log.d("FitHack", String.format("%.2f,%.2f", la, lo));
 //    } catch (JSONException e) {
 //       // TODO Auto-generated catch block
 //       e.printStackTrace();
 //    }
 // }
 //}.execute();
 //
 
    
 }
