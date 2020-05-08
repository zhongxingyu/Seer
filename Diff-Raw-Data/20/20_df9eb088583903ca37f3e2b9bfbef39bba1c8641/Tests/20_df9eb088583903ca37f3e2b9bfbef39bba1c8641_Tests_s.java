 /**
  * Fahrgemeinschaft / Ridesharing App
  * Copyright (c) 2013 by it's authors.
  * Some rights reserved. See LICENSE..
  *
  */
 
 package de.fahrgemeinschaft;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import junit.framework.TestCase;
 
 import org.json.JSONObject;
 import org.teleportr.Place;
 import org.teleportr.Ride;
 
 public class Tests extends TestCase {
 
     private FahrgemeinschaftConnector con;
 
     @Override
     protected void setUp() throws Exception {
         con = new FahrgemeinschaftConnector();
         con.settings = new HashMap<String, String>();
         con.settings.put("radius_from", "15");
         con.settings.put("radius_to", "25");
        con.settings.put("username", "blablamail@gmx.net");
         con.settings.put("password", "blabla");
         con.endpoint =  "http://test.service.fahrgemeinschaft.de";
         super.setUp();
     }
 
     public void testAuth() {
         assertNotNull(con.getAuth());
         assertEquals("f9bb1cab-9793-8534-5d01-58c3d27d50fc", con.get("user"));
     }
 
     Place berlin = new Place(52.519171, 13.406092).address("Berlin, Deutschland");
     Place munich = new Place(48.1671, 11.6094).address("München, Deutschland");
     Place leipzig = new Place("u30u1d1g3sc").address("Leipzig, Deutschland");
     Place nürnberg = new Place("u0zck43yfyx").address("Nürnberg, Deutschland");
     
     Place stuttgart = new Place(48.775417, 9.181758).address("Stuttgart, Deutschland");
     Place muc_flughafen_nordallee = new Place(48.356820, 11.762299);
     
     public void testSearchRides() {
 //        connector.getRides(munich, berlin, new Date(), null);
         try {
             con.search(munich, berlin,
                     new Date(System.currentTimeMillis() + 24*3600000), null);
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         con.printResults();
     }
 
     public void testPublishRide() throws Exception {
         Ride offer = new Ride()
             .dep(new Date()).price(4200)
            .from(stuttgart).via(munich).via(nürnberg).to(berlin)
            .set("EMail", "foo@bar.baz")
             .set("Mobile", "01234567")
             .set("Landline", "001234")
             .set("NumberPlate", "MX-123C")
             .set("Comment", "Hi there..");
         offer.getDetails().put("Privacy", new JSONObject(
              "{ \"Name\": \"0\","       // request
              + "\"Landline\": \"4\","   // members
             + "\"Email\": \"5\","      // nobody
              + "\"Mobile\": \"1\","     // anybody
              + "\"NumberPlate\": \"1\" }"));
         offer.getDetails().put("Reoccur", new JSONObject(
                 "{ \"Saturday\": false,"
                 + "\"Monday\": false,"
                 + "\"Tuesday\": false,"
                 + "\"Wednesday\": false,"
                 + "\"Thursday\": false,"
                 + "\"Friday\": false,"
                 + "\"Sunday\": false }"));
         System.out.println(con.publish(offer));
         // go to test.fahrgemeinschaft and assert published
     }
 
     public void testUpdateRide() throws Exception {
         Ride offer = new Ride().ref("a373b9cf-1f7e-6664-8984-de31a9943738")
             .dep(new Date()).price(4242).seats(5)
             .from(stuttgart).via(leipzig).to(berlin)
             .set("EMail", "foo")
             .set("Mobile", "0123")
             .set("Landline", "001")
             .set("NumberPlate", "MX")
             .set("Comment", "Hi there update..");
         offer.getDetails().put("Privacy", new JSONObject(
              "{ \"Name\": \"1\","       // request
              + "\"Landline\": \"1\","   // members
              + "\"Email\": \"1\","      // nobody
              + "\"Mobile\": \"1\","     // anybody
              + "\"NumberPlate\": \"1\" }"));
         offer.getDetails().put("Reoccur", new JSONObject(
                 "{ \"Saturday\": false,"
                 + "\"Monday\": false,"
                 + "\"Tuesday\": true,"
                 + "\"Wednesday\": false,"
                 + "\"Thursday\": false,"
                 + "\"Friday\": true,"
                 + "\"Sunday\": false }"));
         con.publish(offer);
     }
 }
