 package org.fNordeingang.util;
 
 import android.util.Log;
 import de.mastacode.http.Http;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * User: vileda
  * Date: 21.10.11
  * Time: 17:34
  */
 public class ServiceClient {
   DefaultHttpClient httpclient = new DefaultHttpClient();
 
   public enum Service {
     STATUS,
     PROFILE,
     EAN
   }
   Map<Service, String> services = new HashMap<Service, String>();
 
   public ServiceClient() {
     services.put(Service.STATUS,"/status");
     services.put(Service.PROFILE,"/userCard/profile");
     services.put(Service.EAN,"/openean/");
   }
 
   public String getUrl(Service service) {
     return getUrl(service,"");
   }
 
   public String getUrl(Service service, String params) {
     String serviceUrl = "http://services.fnordeingang.de/services/api";
     return serviceUrl + services.get(service) + params;
   }
 
   public JSONObject getJSON(Service service, String params) throws JSONException, IOException {
     String jsonstring = Http.get(getUrl(service,params)).use(httpclient).asString();
     Log.v("Jsonstring",jsonstring);
     return (JSONObject) new JSONTokener(jsonstring).nextValue();
   }
 
   public JSONObject getJSON(Service service) throws JSONException, IOException {
     return getJSON(service,"");
   }
 
   public JSONObject postJSON(Service service, JSONObject data) throws IOException, JSONException {
     HttpPost httpost = new HttpPost(getUrl(service));
     Log.v("Data:", data.toString());
     StringEntity se = new StringEntity(data.toString());
 
     httpost.setEntity(se);
     httpost.setHeader("Accept", "application/json");
     httpost.setHeader("Content-type", "application/json");
 
     ResponseHandler<String> responseHandler = new BasicResponseHandler();
     String response = null;
 
     response = httpclient.execute(httpost, responseHandler);
 
     Log.v("Response:", response);
 
     // if this throws a JSONException - no json object returned
     // => maybe wrong password
     return new JSONObject(response);
   }
 
   public int toggleStatus(final String username, final String password) {
     try {
       JSONObject userdata = new JSONObject().put("username", username).put("password", password);
       Log.v("Data:", userdata.toString());
       postJSON(Service.STATUS, userdata);
     } catch (IOException ioe) {
       Log.v("IOE: ", ioe.toString());
       return -1;
     } catch (JSONException jsone) {
       Log.v("JSONe: ", jsone.toString());
       return 0;
     } catch (Exception e) {
       Log.v("e: ", e.toString());
       return -2;
     }
     return 1;
   }
 
   public JSONObject getArticleInfo(String ean) {
     try {
       return getJSON(Service.EAN,ean);
     } catch(JSONException e) {
       e.printStackTrace();
     } catch(IOException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public JSONObject getProfile(final String username, final String password, String deviceId) {
     JSONObject userdata = null;
     try {
      userdata = new JSONObject().put("username", username).put("password", password);
       Log.v("Data:", userdata.toString());
       return postJSON(Service.PROFILE, userdata);
     } catch (IOException ioe) {
       Log.v("IOE: ", ioe.toString());
       return userdata;
     } catch (JSONException jsone) {
       Log.v("JSONe: ", jsone.toString());
       return userdata;
     } catch (Exception e) {
       Log.v("e: ", e.toString());
       return userdata;
     }
   }
 }
