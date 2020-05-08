 package org.fNordeingang.util;
 
 import android.util.Log;
 import de.mastacode.http.Http;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.fNordeingang.util.dto.Cart;
 import org.fNordeingang.util.dto.EanArticle;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * User: vileda
  * Date: 21.10.11
  * Time: 17:34
  */
 public class ServiceClient {
   DefaultHttpClient httpclient = new DefaultHttpClient();
   String serviceHost = "services.fnordeingang.de";
 
   public enum Service {
     STATUS,
     PROFILE,
     CART,
     EAN
   }
   Map<Service, String> services = new HashMap<Service, String>();
 
   public ServiceClient() {
     init();
   }
 
   private void init() {
     services.put(Service.STATUS,"/status");
     services.put(Service.PROFILE,"/userCard/profile");
     services.put(Service.EAN,"/article/");
     services.put(Service.CART,"/cart");
   }
 
   public ServiceClient(String serviceHost) {
     this.serviceHost = serviceHost;
     init();
   }
 
   public String getServiceHost() {
     return serviceHost;
   }
 
   public String getUrl(Service service) {
     return getUrl(service,"");
   }
 
   public String getUrl(Service service, String params) {
     String serviceUrl = "http://"+serviceHost+"/services/api";
     return serviceUrl + services.get(service) + params;
   }
 
   public JSONObject getJSON(Service service, String params) throws JSONException, IOException {
     String jsonstring = Http.get(getUrl(service,params)).use(httpclient).asString();
     Log.v("Jsonstring",jsonstring);
     return new JSONObject(jsonstring);
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
 
   public EanArticle getArticleInfo(String ean) {
     EanArticle eanArticle = new EanArticle();
     try {
       JSONObject eanJson = getJSON(Service.EAN, ean);
       eanJson = eanJson.getJSONObject("article");
 
       eanArticle.setEan(ean);
       eanArticle.setName(eanJson.getString("name"));
      eanArticle.setDescription(eanJson.getString("description"));
      eanArticle.setPrice(BigDecimal.valueOf(eanJson.getDouble("price")));

      if(eanArticle.getName() != null && !"".equals(eanArticle.getName())) {
        eanArticle.setFound(true);
       }
     } catch(JSONException e) {
       e.printStackTrace();
     } catch(IOException e) {
       e.printStackTrace();
     }
     return eanArticle;
   }
 
   public Cart getCurrentCart() {
     Cart cart = new Cart();
     try{
       JSONObject o = getJSON(Service.CART).getJSONObject("cart");
       JSONArray oar = o.getJSONArray("articles");
       for(int i = 0; i < oar.length(); i++) {
         JSONObject artJSON = oar.getJSONObject(i);
         EanArticle article = new EanArticle();
         article.setEan(artJSON.getString("ean"));
         article.setName(artJSON.getString("name"));
         article.setDescription(artJSON.getString("description"));
         cart.addArticle(article);
       }
     }catch (Throwable th) {
       th.printStackTrace();
     }
     return cart;
   }
 
   public Cart addArticleToCart(EanArticle article) {
     try{
       JSONObject userdata = new JSONObject().put("ean", article.getEan());
       postJSON(Service.CART, userdata);
     }catch (Throwable th) {
       th.printStackTrace();
     }
     return getCurrentCart();
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
