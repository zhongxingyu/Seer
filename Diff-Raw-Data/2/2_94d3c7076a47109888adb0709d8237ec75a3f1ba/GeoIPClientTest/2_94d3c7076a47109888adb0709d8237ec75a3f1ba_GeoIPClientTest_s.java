 // START SNIPPET: client
 package net.webservicex.geoip;
 
 import net.webservicex.GetGeoIP;
 import net.webservicex.geoip.jaxb.GeoIPServiceClient;
 import net.webservicex.geoip.jaxb.GeoIPServiceSoap;
 import junit.framework.TestCase;
 
 public class GeoIPClientTest extends TestCase
 {
     public void testClient() throws Exception
     {
         GeoIPServiceClient service = new GeoIPServiceClient();
         GeoIPServiceSoap geoIPClient = service.getGeoIPServiceSoap();
         
         System.out.println("The country is: " + 
                           geoIPClient.GetGeoIP("216.73.126.120").getCountryName());
     }
 }
 // END SNIPPET: client
