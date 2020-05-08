 package de.fnordeingang.fnordapp.util;
 
 import android.util.Log;
 import de.fnordeingang.fnordapp.util.dto.Cart;
 import de.fnordeingang.fnordapp.util.dto.EanArticle;
 import de.fnordeingang.fnordapp.util.dto.UserProfile;
 import de.mastacode.http.Http;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: vileda
  * Date: 21.10.11
  * Time: 17:34
  */
 public class ServiceClient {
   DefaultHttpClient httpclient = new DefaultHttpClient();
   String serviceHost = "services.fnordeingang.de";
   //String serviceHost = "172.29.1.47:8080";
 
   public enum Service {
     STATUS,
     PROFILE,
     CART,
     EAN_ALL, EAN
   }
   Map<Service, String> services = new HashMap<Service, String>();
 
   public ServiceClient() {
     init();
   }
 
   private void init() {
     services.put(Service.STATUS,"/status");
     services.put(Service.PROFILE,"/userCard/profile");
     services.put(Service.EAN,"/article/");
     services.put(Service.EAN_ALL,"/article/all");
     services.put(Service.CART,"/cart");
   }
 
   public ServiceClient(String serviceHost) {
     this.serviceHost = serviceHost;
     init();
   }
 
   public String getUrl(Service service) {
     return getUrl(service,"");
   }
 
   public String getUrl(Service service, String params) {
     String serviceUrl = "http://"+serviceHost+"/services/api";
     return serviceUrl + services.get(service) + params;
   }
 
   public String getJSON(Service service, String params) {
     String jsonstring = "";
     try {
       jsonstring = Http.get(getUrl(service, params)).use(httpclient).asString();
     } catch(IOException e) {
       e.printStackTrace();
     }
     Log.v("get "+service.toString()+" response: ",jsonstring);
     return jsonstring;
   }
 
   public String getJSON(Service service) {
     return getJSON(service,"");
   }
 
   public JSONObject getJSONObject(Service service) {
     return getJSONObject(getJSON(service));
   }
 
   public JSONObject getJSONObject(Service service, String params) {
      return getJSONObject(getJSON(service,params));
    }
 
   public JSONObject getJSONObject(String json) {
     try {
       return new JSONObject(json);
     } catch(JSONException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public JSONObject postJSON(Service service, JSONObject data) throws IOException, JSONException {
     HttpPost httpost = new HttpPost(getUrl(service));
     Log.v("post Data:", data.toString());
     StringEntity se = new StringEntity(data.toString());
 
     httpost.setEntity(se);
     httpost.setHeader("Accept", "application/json");
     httpost.setHeader("Content-type", "application/json");
 
     ResponseHandler<String> responseHandler = new BasicResponseHandler();
     String response = httpclient.execute(httpost, responseHandler);
 
     if(response != null) {
       Log.v("post " + service.toString() + " Response:", response);
       // if this throws a JSONException - no json object returned
       // => maybe wrong password
       return new JSONObject(response);
     } else {
       return null;
     }
   }
 
   public JSONObject deleteJSON(Service service) {
     return deleteJSON(service,"");
   }
 
   public JSONObject deleteJSON(Service service, String params) {
     HttpDelete method = new HttpDelete(getUrl(service, params));
 
     method.setHeader("Accept", "application/json");
 
     ResponseHandler<String> responseHandler = new BasicResponseHandler();
     String response = null;
     try {
       response = httpclient.execute(method, responseHandler);
     } catch(IOException e) {
       e.printStackTrace();
     }
 
     if(response != null) {
       Log.v("delete " + service.toString() + " Response:", response);
     }
 
     // if this throws a JSONException - no json object returned
     // => maybe wrong password
     try {
       if(response != null)
         return new JSONObject(response);
     } catch(JSONException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   public int toggleStatus(final String username, final String password) {
     try {
       JSONObject userdata = new JSONObject().put("username", username).put("password", password);
       Log.v("posting data:", userdata.toString());
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
       JSONObject eanJson = getJSONObject(Service.EAN, ean);
 
       eanArticle.setEan(ean);
       eanArticle.setName(eanJson.getString("name"));
       try{
         eanArticle.setDescription(eanJson.getString("description"));
       } catch (Throwable th) {
         th.printStackTrace();
       }
       eanArticle.setPrice(BigDecimal.valueOf(eanJson.getDouble("price")));
     } catch(JSONException e) {
       e.printStackTrace();
     }
     if(eanArticle.getName() != null && !"".equals(eanArticle.getName())) {
       eanArticle.setFound(true);
     }
     return eanArticle;
   }
 
   public Cart getCurrentCart() {
     Cart cart = new Cart();
     try{
       JSONObject o = getJSONObject(Service.CART);
       if(o != null) {
         JSONArray oar = o.optJSONArray("articles");
         if(oar != null) {
           for(int i = 0; i < oar.length(); i++) {
             JSONObject artJSON = oar.getJSONObject(i);
             EanArticle article = jsonToArticle(artJSON);
             cart.addArticle(article);
           }
         } else {
           JSONObject jsonObject = o.optJSONObject("articles");
           if(jsonObject != null) {
             EanArticle article = jsonToArticle(o.optJSONObject("articles"));
             cart.addArticle(article);
           }
         }
       }
     }catch (Throwable th) {
       th.printStackTrace();
     }
     return cart;
   }
 
   private EanArticle jsonToArticle(JSONObject artJSON) throws JSONException {
     EanArticle article = new EanArticle();
     article.setEan(artJSON.optString("ean",""));
    article.setName(artJSON.optString("name",""));
    article.setDescription(artJSON.optString("description",""));
     return article;
   }
 
   public void addArticleToCart(EanArticle article) {
     try{
       JSONObject userdata = new JSONObject().put("ean", article.getEan());
       postJSON(Service.CART, userdata);
     }catch (Throwable th) {
       th.printStackTrace();
     }
   }
 
   public Cart emptyCart() {
     deleteJSON(Service.CART);
     return getCurrentCart();
   }
 
   public Cart removeArticleFromCart(String ean) {
     deleteJSON(Service.CART,"/"+ean);
     return getCurrentCart();
   }
 
   public List<EanArticle> getAllArticles() {
     List<EanArticle> articles = new ArrayList<EanArticle>();
     try {
       JSONArray articlesArray = getJSONObject(Service.EAN_ALL).getJSONArray("article");
       for(int i = 0; i < articlesArray.length(); i++) {
         articles.add(jsonToArticle(articlesArray.getJSONObject(i)));
       }
     } catch(Exception e) {
       e.printStackTrace();
     }
     return articles;
   }
 
   public UserProfile getProfile(final String username, final String password, String deviceId) {
     JSONObject userdata;
     UserProfile userProfile = new UserProfile(username);
     try {
       userdata = new JSONObject().put("username", username).put("password", password);
       Log.v("posting data:", userdata.toString());
       userdata = postJSON(Service.PROFILE, userdata);
       userProfile.setBalance(BigDecimal.valueOf(userdata.getDouble("balance")));
     } catch (IOException ioe) {
       Log.v("IOE: ", ioe.toString());
     } catch (JSONException jsone) {
       Log.v("JSONe: ", jsone.toString());
     } catch (Exception e) {
       Log.v("e: ", e.toString());
     }
     return userProfile;
   }
 }
