 package com.github.detro.rps.http.test;
 
 import com.github.detro.rps.Match;
 import com.github.detro.rps.Weapons;
 import com.github.detro.rps.http.Router;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.openqa.selenium.net.PortProber;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.testng.Assert.*;
 
 // TODO Definitely needs more tests
 public class RouterTest {
     private static final int PORT = PortProber.findFreePort();
     private static final Router ROUTER = new Router();
     private static final String BASEURL = "http://localhost:" + PORT + Router.API_PATH;
 
     @BeforeClass
     public static void startRouter() {
         ROUTER.listen(PORT);
         try {
             Thread.sleep(500);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     @Test
     public void shouldReturnListOfAllAvailableWeapons() {
         HttpClient client = new HttpClient();
         GetMethod getWeapons = new GetMethod(BASEURL + "/weapons");
         try {
             // execute and check status code
             int statusCode = client.executeMethod(getWeapons);
             assertEquals(statusCode, 200);
 
             // check response body
             String body = new String(getWeapons.getResponseBody());
             assertTrue(body.startsWith("["));
             assertTrue(body.endsWith("]"));
             for (int i = 0, ilen = Weapons.weaponsAmount(); i < ilen; ++i) {
                 assertTrue(body.contains(Weapons.getName(i)));
             }
         } catch (IOException ioe) {
             fail();
         } finally {
             getWeapons.releaseConnection();
         }
     }
 
     @Test
     public void shouldAllowToPlayAMatch() {
         HttpClient client1 = new HttpClient();
         HttpClient client2 = new HttpClient();
         PostMethod createMatch = null;
         PutMethod joinMatch = null;
         PutMethod setWeapon = null;
         PutMethod restartMatch = null;
         GetMethod getMatchInfo = null;
         String body;
         JsonObject jsonBody;
         String matchId;
 
         // Create a Match
         try {
             // First Player creates the match
             createMatch = new PostMethod(BASEURL + "/match");
             createMatch.setParameter("kind", "pvp");
             assertEquals(client1.executeMethod(createMatch), 200);
 
             // Extract Match ID from response body
             body = new String(createMatch.getResponseBody());
             jsonBody = new JsonParser().parse(body).getAsJsonObject();
             assertTrue(jsonBody.isJsonObject());
             assertTrue(jsonBody.has("id"));
             assertTrue(jsonBody.get("id").isJsonPrimitive());
             assertNotNull(jsonBody.get("id"));
             matchId = jsonBody.get("id").getAsString();
 
             // First Player joins the match
             joinMatch = new PutMethod(BASEURL + "/match/" + matchId);
             joinMatch.setQueryString(new NameValuePair[] { new NameValuePair("action", "join")});
             assertEquals(client1.executeMethod(joinMatch), 200);
 
             // Second Player joins the match
             assertEquals(client2.executeMethod(joinMatch), 200);
 
             // First and Second Player set the same weapon
             setWeapon = new PutMethod(BASEURL + "/match/" + matchId);
             setWeapon.setQueryString(new NameValuePair[] {
                     new NameValuePair("action", "weapon"),
                    new NameValuePair("weaponId", "1")
             });
             assertEquals(client1.executeMethod(setWeapon), 200);
             assertEquals(client2.executeMethod(setWeapon), 200);
 
             // Check Match is has now been played
             getMatchInfo = new GetMethod(BASEURL + "/match/" + matchId);
             assertEquals(client1.executeMethod(getMatchInfo), 200);
             body = new String(getMatchInfo.getResponseBody());
             jsonBody = new JsonParser().parse(body).getAsJsonObject();
 
             // Extract the Match status
             assertTrue(jsonBody.has("status"));
             assertTrue(jsonBody.get("status").isJsonPrimitive());
             assertEquals(jsonBody.get("status").getAsInt(), Match.PLAYED);
 
             // Extract the Match result
             assertTrue(jsonBody.has("result"));
             assertTrue(jsonBody.get("result").isJsonPrimitive());
             assertEquals(jsonBody.get("result").getAsString(), "draw");
 
             // Reset the Match
             restartMatch = new PutMethod(BASEURL + "/match/" + matchId);
             restartMatch.setQueryString(new NameValuePair[]{new NameValuePair("action", "restart")});
             assertEquals(client2.executeMethod(restartMatch), 200);
 
             // Check Match is has now been reset
             getMatchInfo = new GetMethod(BASEURL + "/match/" + matchId);
             assertEquals(client2.executeMethod(getMatchInfo), 200);
             body = new String(getMatchInfo.getResponseBody());
 
             // Extract the Match status
             jsonBody = new JsonParser().parse(body).getAsJsonObject();
             assertTrue(jsonBody.has("status"));
             assertTrue(jsonBody.get("status").isJsonPrimitive());
             assertEquals(jsonBody.get("status").getAsInt(), Match.WAITING_PLAYERS_WEAPONS);
         } catch (IOException ioe) {
             fail();
         } finally {
             if (createMatch != null) createMatch.releaseConnection();
             if (joinMatch != null) joinMatch.releaseConnection();
             if (setWeapon != null) setWeapon.releaseConnection();
             if (restartMatch != null) restartMatch.releaseConnection();
             if (getMatchInfo != null) getMatchInfo.releaseConnection();
         }
     }
 }
