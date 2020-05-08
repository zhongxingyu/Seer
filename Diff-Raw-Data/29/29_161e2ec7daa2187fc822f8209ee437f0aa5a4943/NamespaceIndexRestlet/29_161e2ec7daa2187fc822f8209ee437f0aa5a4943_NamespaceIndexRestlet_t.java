 package org.geoserver.geosearch;
 
 import org.restlet.Restlet;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.resource.OutputRepresentation;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.jdom.output.XMLOutputter;
 import org.jdom.output.Format;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Map;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.config.CoverageStoreConfig;
 import org.vfny.geoserver.config.DataStoreConfig;
 import org.vfny.geoserver.config.CoverageConfig;
 import org.vfny.geoserver.config.FeatureTypeConfig;
 
 import org.geotools.geometry.GeneralEnvelope;
 import org.geotools.geometry.DirectPosition2D;
 import org.geotools.util.logging.Logging;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 public class NamespaceIndexRestlet extends GeoServerProxyAwareRestlet{
 
     private static Logger LOGGER = Logging.getLogger("org.geoserver.geosearch");
 
     private Data myData;
     private DataConfig myDataConfig;
     private GeoServer myGeoserver;
     private String GEOSERVER_URL;
 
     private static Namespace KML = Namespace.getNamespace("http://earth.google.com/kml/2.2");
     private static Namespace ATOM = Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom");
 
     public void setData(Data d){
         myData = d;
     }
 
     public void setDataConfig(DataConfig dc){
         myDataConfig = dc;
     }
 
     public void setGeoServer(GeoServer gs){
         myGeoserver = gs;
     }
 
     public Data getData(){
         return myData;
     }
 
     public DataConfig getDataConfig(){
         return myDataConfig;
     }
 
     public GeoServer getGeoServer(){
         return myGeoserver;
     }
 
     public NamespaceIndexRestlet() {
     }
 
     public void handle(Request request, Response response){
         GEOSERVER_URL = getBaseURL(request);
         if (request.getMethod().equals(Method.GET)){
             doGet(request, response);
         } else {
             response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         }
     }
 
     public void doGet(Request request, Response response){
         String namespace = (String)request.getAttributes().get("namespace");
         List layers = getLayers(namespace);
         if (layers.size() > 0) {
             Document d = buildKML(namespace, layers);
             response.setEntity(new JDOMRepresentation(d));
         } else {
             response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
         }
     }
 
     public Document buildKML(String namespace, List layerList){
         final Document d = new Document();
         // xmlns="http://earth.google.com/kml/2.2"
         // xmlns:atom="http://www.w3.org/2005/Atom"
         Element kml = new Element("kml", KML);
         kml.addNamespaceDeclaration(ATOM);
         Element folder = new Element("folder", KML);
         kml.addContent(folder);
         Element name = (new Element("name", KML));
         name.setText(namespace + " Namespace");
         folder.addContent(name);
         Element author = new Element("author", ATOM);
         author.setText(getGeoServer().getContactPerson());
         folder.addContent(author);
         Element link = new Element("link", ATOM);
         link.setText("http://example.org/");
         folder.addContent(link);
         Element address = new Element("address", KML);
         address.setText(getGeoServer().getAddress() + " " 
                 + getGeoServer().getAddressCountry() + " " 
                 + getGeoServer().getAddressCity() + " " 
                 + getGeoServer().getAddressPostalCode());
         folder.addContent(address);
         Element phoneNumber = new Element("phoneNumber", KML);
         phoneNumber.setText(getGeoServer().getContactVoice());
         folder.addContent(phoneNumber);
         Element snippet = new Element("Snippet", KML);
         snippet.setText("All " + layerList.size() + " layers in the " + namespace + " namespace");
         folder.addContent(snippet);
         Element open = new Element("open", KML);
         open.setText("1");
         folder.addContent(open);
 
         try{
             Envelope bbox = accumulateBBox(layerList);
 
             Element lookAt = getLookAt(bbox);
 
             lookAt.setAttribute("id", name.getText() + "_lookat");
             folder.addContent(lookAt);
         } catch (IOException ioe){
             // just don't add the lookat? 
             LOGGER.severe("Failure while calculating LookAt coordinates for layer index");
         }
 
         Iterator it = layerList.iterator();
         while (it.hasNext()){
             MapLayerInfo l = (MapLayerInfo)it.next();
             folder.addContent(createNetworkLink(namespace, l));
         }
 
         d.setRootElement(kml);
         return d;
 
     }
 
     private Element createNetworkLink(String namespace, MapLayerInfo layer){
         Element networkLink = new Element("NetworkLink", KML);
         Element name = new Element("name", KML);
         name.setText(layer.getLabel());
         networkLink.addContent(name);
         Element open = new Element("open", KML);
         open.setText("1");
         networkLink.addContent(open);
         Element snippet = new Element("Snippet", KML);
         snippet.setText(layer.getName());
         networkLink.addContent(snippet);
         Element url = new Element("Url", KML);
         Element href = new Element("href", KML);
        href.setText(GEOSERVER_URL + "/" + namespace + "/" + layer.getName().split(":",2)[1]);
         url.addContent(href);
         networkLink.addContent(url);
         return networkLink;
     }
 
     private Layer getLayer(String namespace, String layer){
         Object o = getDataConfig().getCoverages().get(namespace+":"+layer);
         if (o == null){
             o = getDataConfig().getFeaturesTypes().get(namespace+":"+layer);
         }
 
         if (o == null){
         } else if (o instanceof CoverageConfig){
             return new CoverageLayer((CoverageConfig)o);
         } else if (o instanceof FeatureTypeConfig){
             return new FeatureTypeLayer((FeatureTypeConfig)o);
         }
         return null;
     }
 
     private List getLayers(String namespace){
         List results = new ArrayList();
 
         Iterator it = getData().getLayerNames().iterator();
         while (it.hasNext()){
             String name = it.next().toString();
             if (name.startsWith(namespace)){
                 results.add(getData().getMapLayerInfo(name));
             }
         }
 
         return results;
     }
 
 /*
     private List getLayers(String namespace){
         // ugh really? 
         List results = new ArrayList();
 
         List storesInNameSpace = new ArrayList();
 
         Iterator it = getDataConfig().getDataFormats().entrySet().iterator();
         while(it.hasNext()){
             Map.Entry entry = (Map.Entry)it.next();
             CoverageStoreConfig csc = (CoverageStoreConfig)entry.getValue();
             if (csc.getNameSpaceId().equals(namespace)){
                 storesInNameSpace.add(entry.getKey());
             }
         }
 
         it = getDataConfig().getDataStores().entrySet().iterator();
         while(it.hasNext()){
             Map.Entry entry = (Map.Entry)it.next();
             DataStoreConfig dsc = (DataStoreConfig)entry.getValue();
             if (dsc.getNameSpaceId().equals(namespace)){
                 storesInNameSpace.add(entry.getKey());
             }
         }
 
         it = getDataConfig().getCoverages().entrySet().iterator();
         while (it.hasNext()){
             Map.Entry entry = (Map.Entry) it.next();
             String qualifiedName = entry.getKey().toString();
             CoverageConfig cc = (CoverageConfig)entry.getValue();
             Iterator it2 = storesInNameSpace.iterator();
             while (it2.hasNext()){
                 String dsName = (String)(it2.next()) + ":";
                 if (qualifiedName.startsWith(dsName)){
                     results.add(new CoverageLayer((CoverageConfig)cc));
                     break;
                 }
             }
         }
 
         it = getDataConfig().getFeaturesTypes().entrySet().iterator();
         while (it.hasNext()){
             Map.Entry entry = (Map.Entry) it.next();
             String qualifiedName = entry.getKey().toString();
             FeatureTypeConfig ftc = (FeatureTypeConfig)entry.getValue();
             Iterator it2 = storesInNameSpace.iterator();
             while (it2.hasNext()){
                 String dsName = (String)(it2.next()) + ":";
                 if (qualifiedName.startsWith(dsName)){
                     results.add(new FeatureTypeLayer((FeatureTypeConfig)ftc));
                     break;
                 }
             }
         }
 
         return results;
     }
 
 
     private GeneralEnvelope accumulateBBox(List layers){
         GeneralEnvelope result = null;
         Iterator it = layers.iterator();
         while (it.hasNext()){
             GeneralEnvelope genv = ((Layer)it.next()).getBounds();
             if (result == null){
                 result = genv;
             } else {
                 result.add(genv);
             }
         }
         return result;
     }
 
     */
     
     private Envelope accumulateBBox(List layers) throws IOException{
         Envelope result = new Envelope();
         result.setToNull();
         Iterator it = layers.iterator();
         while (it.hasNext()){
             Envelope env = ((MapLayerInfo)it.next()).getLatLongBoundingBox();
             result.expandToInclude(env);
         }
         return result;
     }
 
     private Element getLookAt(Envelope e){
         return getLookAt(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY());
     }
 
 
     private Element getLookAt(GeneralEnvelope e){
         return getLookAt(e.getMinimum(1), e.getMinimum(0), e.getMaximum(1), e.getMaximum(0));
     }
 
     public static Element getLookAt(double lon1, double lat1, double lon2, double lat2){
         double R_EARTH = 6.371 * 1000000; // meters
         double VIEWER_WIDTH = 22 * Math.PI / 180; // The field of view of the google maps camera, in radians
 
         double[] p1 = getRect(lon1, lat1, R_EARTH);
         double[] p2 = getRect(lon2, lat2, R_EARTH);
         double[] midpoint = new double[]{
             (p1[0] + p2[0])/2,
                 (p1[1] + p2[1])/2,
                 (p1[2] + p2[2])/2
         };
 
         midpoint = getGeographic(midpoint[0], midpoint[1], midpoint[2]);
 
         // averaging the longitudes; using the rectangular coordinates makes the calculated center tend toward the corner that's closer to the equator. 
         midpoint[0] = ((lon1 + lon2)/2); 
 
         double distance = distance(p1, p2);
 
         double height = distance/ (2 * Math.tan(VIEWER_WIDTH));
 
         LOGGER.fine("lat1: " + lat1 + "; lon1: " + lon1);
         LOGGER.fine("lat2: " + lat2 + "; lon2: " + lon2);
         LOGGER.fine("latmid: " + midpoint[1] + "; lonmid: " + midpoint[0]);
 
         Element lookAt = new Element("LookAt", KML);
         Element lon = new Element("longitude", KML).setText(Double.toString((lon1+lon2)/2));
         Element lat = new Element("latitude", KML);
         lat.setText(Double.toString(midpoint[1]));
         Element alt = new Element("altitude", KML);
         alt.setText("0");
         Element range = new Element("range", KML);
         range.setText(Double.toString(distance));
         Element tilt = new Element("tilt", KML);
         tilt.setText("0");
         Element heading = new Element("heading", KML);
         heading.setText("0");
         Element altitudeMode = new Element("altitudeMode", KML).setText("clampToGround");
         lookAt.addContent(lon);
         lookAt.addContent(lat);
         lookAt.addContent(alt);
         lookAt.addContent(range);
         lookAt.addContent(tilt);
         lookAt.addContent(heading);
 
 
         return lookAt;
        /*  "<LookAt id=\"geoserver\">" + 
             "  <longitude>" + ((lon1 + lon2)/2) +  "</longitude>      <!-- kml:angle180 -->" +
             "  <latitude>"+midpoint[1]+"</latitude>        <!-- kml:angle90 -->" +
             "  <altitude>0</altitude>       <!-- double --> " +
             "  <range>"+distance+"</range>              <!-- double -->" +
             "  <tilt>0</tilt>               <!-- float -->" +
             "  <heading>0</heading>         <!-- float -->" +
             "  <altitudeMode>clampToGround</altitudeMode> " +
             "  <!--kml:altitudeModeEnum:clampToGround, relativeToGround, absolute -->" +
             "</LookAt>"; */
     }
 
     private static double[] getRect(double lat, double lon, double radius){
         double theta = (90 - lat) * Math.PI/180;
         double phi   = (90 - lon) * Math.PI/180;
 
         double x = radius * Math.sin(phi) * Math.cos(theta);
         double y = radius * Math.sin(phi) * Math.sin(theta);
         double z = radius * Math.cos(phi);
         return new double[]{x, y, z};
     }
 
     private static double[] getGeographic(double x, double y, double z){
         double theta, phi, radius;
         radius = distance(new double[]{x, y, z}, new double[]{0,0,0});
         theta = Math.atan2(Math.sqrt(x * x + y * y) , z);
         phi = Math.atan2(y , x);
 
         double lat = 90 - (theta * 180 / Math.PI);
         double lon = 90 - (phi * 180 / Math.PI);
 
         return new double[]{(lon > 180 ? lon - 360 : lon), lat, radius};
     }
 
     private static double distance(double[] p1, double[] p2){
         double dx = p1[0] - p2[0];
         double dy = p1[1] - p2[1];
         double dz = p1[2] - p2[2];
         return Math.sqrt(dx * dx + dy * dy + dz * dz);
     }
 
 }
 
