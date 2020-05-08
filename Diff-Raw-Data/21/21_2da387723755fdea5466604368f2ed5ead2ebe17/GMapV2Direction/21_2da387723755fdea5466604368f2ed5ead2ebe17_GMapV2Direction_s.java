 package com.example.mobicanavigate.gps;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class GMapV2Direction extends AsyncTask<Void, Void, Boolean> {
     private LatLng mStart;
     private LatLng mEnd;
     private Document mDoc;
     private GoogleMap mGoogleMap;
     private Polyline mPolyline;
     private Polyline mPolyline2;
 


     @Override
     protected Boolean doInBackground(Void... params) {
         String url = "http://maps.googleapis.com/maps/api/directions/xml?"
                 + "origin=" + mStart.latitude + "," + mStart.longitude
                 + "&destination=" + mEnd.latitude + "," + mEnd.longitude
                 + "&sensor=false&units=metric&mode=driving";
         try {
             HttpClient httpClient = new DefaultHttpClient();
             HttpContext localContext = new BasicHttpContext();
             HttpPost httpPost = new HttpPost(url);
             HttpResponse response = httpClient.execute(httpPost, localContext);
             InputStream in = response.getEntity().getContent();
             DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             mDoc = builder.parse(in);
             return true;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return false;
     }
 
     @Override
     protected void onPostExecute(Boolean successful) {
         if (successful) {
             ArrayList<LatLng> directionPoint = getDirection(mDoc);
             PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.BLACK);
             PolylineOptions rectLine2 = new PolylineOptions().width(1).color(Color.YELLOW);
             for (int i = 0; i < directionPoint.size(); i++) {
                 rectLine.add(directionPoint.get(i));
                 rectLine2.add(directionPoint.get(i));
             }
             if (mPolyline != null) {
                 mPolyline.remove();
                 mPolyline2.remove();
             }
             mPolyline = mGoogleMap.addPolyline(rectLine);
             mPolyline2 = mGoogleMap.addPolyline(rectLine2);
         }
     }
 
     public void setParams(LatLng start, LatLng end,GoogleMap googleMap) {
         this.mStart = start;
         this.mEnd = end;
         this.mGoogleMap = googleMap;
     }
 
     public String getDurationText(Document doc) {
         NodeList nl1 = doc.getElementsByTagName("duration");
         Node node1 = nl1.item(0);
         NodeList nl2 = node1.getChildNodes();
         Node node2 = nl2.item(getNodeIndex(nl2, "text"));
         Log.i("DurationText", node2.getTextContent());
         return node2.getTextContent();
     }
 
     public int getDurationValue(Document doc) {
         NodeList nl1 = doc.getElementsByTagName("duration");
         Node node1 = nl1.item(0);
         NodeList nl2 = node1.getChildNodes();
         Node node2 = nl2.item(getNodeIndex(nl2, "value"));
         Log.i("DurationValue", node2.getTextContent());
         return Integer.parseInt(node2.getTextContent());
     }
 
     public String getDistanceText(Document doc) {
         NodeList nl1 = doc.getElementsByTagName("distance");
         Node node1 = nl1.item(0);
         NodeList nl2 = node1.getChildNodes();
         Node node2 = nl2.item(getNodeIndex(nl2, "text"));
         Log.i("DistanceText", node2.getTextContent());
         return node2.getTextContent();
     }
 
     public int getDistanceValue(Document doc) {
         NodeList nl1 = doc.getElementsByTagName("distance");
         Node node1 = nl1.item(0);
         NodeList nl2 = node1.getChildNodes();
         Node node2 = nl2.item(getNodeIndex(nl2, "value"));
         Log.i("DistanceValue", node2.getTextContent());
         return Integer.parseInt(node2.getTextContent());
     }
 
     public String getStartAddress(Document doc) {
         NodeList nl1 = doc.getElementsByTagName("start_address");
         Node node1 = nl1.item(0);
         Log.i("StartAddress", node1.getTextContent());
         return node1.getTextContent();
     }
 
     public String getEndAddress(Document doc) {
         NodeList nl1 = doc.getElementsByTagName("end_address");
         Node node1 = nl1.item(0);
         Log.i("StartAddress", node1.getTextContent());
         return node1.getTextContent();
     }
 
     public String getCopyRights(Document doc) {
         NodeList nl1 = doc.getElementsByTagName("copyrights");
         Node node1 = nl1.item(0);
         Log.i("CopyRights", node1.getTextContent());
         return node1.getTextContent();
     }
 
     public ArrayList<LatLng> getDirection(Document doc) {
         NodeList nl1, nl2, nl3;
         ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();
         nl1 = doc.getElementsByTagName("step");
         if (nl1.getLength() > 0) {
             for (int i = 0; i < nl1.getLength(); i++) {
                 Node node1 = nl1.item(i);
                 nl2 = node1.getChildNodes();
 
                 Node locationNode = nl2.item(getNodeIndex(nl2, "start_location"));
                 nl3 = locationNode.getChildNodes();
                 Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
                 double lat = Double.parseDouble(latNode.getTextContent());
                 Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                 double lng = Double.parseDouble(lngNode.getTextContent());
                 listGeopoints.add(new LatLng(lat, lng));
 
                 locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
                 nl3 = locationNode.getChildNodes();
                 latNode = nl3.item(getNodeIndex(nl3, "points"));
                 ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
                 for (int j = 0; j < arr.size(); j++) {
                     listGeopoints.add(new LatLng(arr.get(j).latitude, arr.get(j).longitude));
                 }
 
                 locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
                 nl3 = locationNode.getChildNodes();
                 latNode = nl3.item(getNodeIndex(nl3, "lat"));
                 lat = Double.parseDouble(latNode.getTextContent());
                 lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                 lng = Double.parseDouble(lngNode.getTextContent());
                 listGeopoints.add(new LatLng(lat, lng));
             }
         }
         return listGeopoints;
     }
 
     private int getNodeIndex(NodeList nl, String nodename) {
         for (int i = 0; i < nl.getLength(); i++) {
             if (nl.item(i).getNodeName().equals(nodename))
                 return i;
         }
         return -1;
     }
 
     private ArrayList<LatLng> decodePoly(String encoded) {
         ArrayList<LatLng> poly = new ArrayList<LatLng>();
         int index = 0, len = encoded.length();
         int lat = 0, lng = 0;
         while (index < len) {
             int b, shift = 0, result = 0;
             do {
                 b = encoded.charAt(index++) - 63;
                 result |= (b & 0x1f) << shift;
                 shift += 5;
             } while (b >= 0x20);
             int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lat += dlat;
             shift = 0;
             result = 0;
             do {
                 b = encoded.charAt(index++) - 63;
                 result |= (b & 0x1f) << shift;
                 shift += 5;
             } while (b >= 0x20);
             int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
             lng += dlng;
 
             LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
             poly.add(position);
         }
         return poly;
     }
 }
