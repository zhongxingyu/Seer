 package resources.osm;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class OSMClient {
     private static final String OSMURL = "http://nominatim.openstreetmap.org/search?format=xml&q=";
     private String display_name;
     private String adresse;
     private float lat;
     private float lon;
 
     public float getLat() {
         return lat;
     }
 
     public String getDisplay_name() {
         return display_name;
     }
 
     public float getLon() {
         return lon;
     }
 
     public OSMClient(String Padresse) {
         adresse = Padresse;
         lat = 0;
         lon = 0;
     }
 
     /**
      * on suppose qu'une adresse ne retournera toujour qu'un resultat, non valide sinon
      *
      * @return false if Adresse not found on OSM
      */
     public boolean search() throws IOException {
         //adresse = Padresse;
        String requeteOSM = OSMURL + adresse.replaceAll(" ", "+");
 
         try {
             URL osm = new URL(requeteOSM);
 
             DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
 
             HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
             Document document = docBuilder.parse(connection.getInputStream());
            /*File f = new File("test.XML");
            FileOutputStream fis = new FileOutputStream(f);
            byte []b = new byte[4096];
            connection.getInputStream().read(b);
            fis.write(b);
            fis.close();    */


             connection.disconnect();
 
             NodeList ele = document.getDocumentElement().getElementsByTagName("place");
             NamedNodeMap att = ele.item(0).getAttributes();
 
            lat = Float.parseFloat(att.getNamedItem("lat").getNodeValue());
            lon = Float.parseFloat(att.getNamedItem("lon").getNodeValue());
             display_name = att.getNamedItem("display_name").getNodeValue();
 
         } catch (MalformedURLException e) {
             //e.printStackTrace();
             return false;//To change body of catch statement use File | Settings | File Templates.
         } catch (ParserConfigurationException e) {
             //e.printStackTrace();
             return false;//To change body of catch statement use File | Settings | File Templates.
         } catch (SAXException e) {
             //e.printStackTrace();
             return false;//To change body of catch statement use File | Settings | File Templates.
         } catch (java.lang.NullPointerException e) {
             //e.printStackTrace();
             return false;//To change body of catch statement use File | Settings | File Templates.
         }
         return true;
     }
 }
