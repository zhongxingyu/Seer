 package io.msgs.v2;
 
 import io.msgs.v2.entity.Endpoint;
 import io.msgs.v2.entity.ItemList;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import ch.boye.httpclientandroidlib.NameValuePair;
 import ch.boye.httpclientandroidlib.client.utils.URLEncodedUtils;
 import ch.boye.httpclientandroidlib.message.BasicNameValuePair;
 
 import com.egeniq.BuildConfig;
 import com.egeniq.utils.api.APIException;
 import com.egeniq.utils.api.APIUtils;
 
 /**
  * Request Helper for User.
  * 
  */
 public class UserRequestHelper extends RequestHelper {
     private final static String TAG = UserRequestHelper.class.getSimpleName();
     private final static boolean DEBUG = BuildConfig.DEBUG;
 
     /**
      * Constructor
      */
     public UserRequestHelper(Client client, String userToken) {
         super(client, "/users/" + userToken);
         _userToken = userToken;
     }
 
     /**
      * Get Endpoints.
      * 
      * @param sort Optional. Pass <b>null</b> to use default value.
      * @param limit Optional. Pass <b>null</b> to use default value.
      */
     public ItemList<Endpoint> getEndpoints(Integer limit, Integer offset) throws APIException {
         try {
             List<NameValuePair> params = new ArrayList<NameValuePair>();
             if (limit != null) {
                 params.add(new BasicNameValuePair("limit", String.valueOf(limit)));
             }
             if (offset != null) {
                 params.add(new BasicNameValuePair("offset", String.valueOf(offset)));
             }
            String query = (!params.isEmpty() ? "?" + URLEncodedUtils.format(params, "utf-8") : URLEncodedUtils.format(params, "utf-8"));
 
             JSONObject object = _client._get(_getBasePath() + "/endpoints" + query, false);
 
             return _parseEndpointList(object);
         } catch (Exception e) {
             if (DEBUG) {
                 Log.e(TAG, "Error getting endpoints for user", e);
             }
 
             if (!(e instanceof APIException)) {
                 e = new APIException(e);
             }
 
             throw (APIException)e;
         }
     }
 
     /**
      * Get endpoint helper.
      * 
      * @param endpointToken
      */
     public EndpointRequestHelper endpoint(String endpointToken) {
         return new EndpointRequestHelper(_client, _userToken, endpointToken);
     }
 
     /**
      * Parse EndpointList.
      */
     private ItemList<Endpoint> _parseEndpointList(JSONObject object) {
         ItemList<Endpoint> list = new ItemList<Endpoint>();
         try {
             list.setTotal(APIUtils.getInt(object, "total", 0));
             list.setCount(APIUtils.getInt(object, "count", 0));
             list.setItems(_parseEndpoints(object.getJSONArray("items")));
         } catch (JSONException e) {
             if (DEBUG) {
                 Log.e(TAG, "Error parsing endpointlist", e);
             }
         }
 
         return list;
     }
 
     /**
      * Parse Endpoints.
      */
     private Endpoint[] _parseEndpoints(JSONArray array) {
         ArrayList<Endpoint> endpoints = new ArrayList<Endpoint>();
         for (int i = 0; i < array.length(); i++) {
             try {
                 endpoints.add(_parseEndpoint(array.getJSONObject(i)));
             } catch (JSONException e) {
                 if (DEBUG) {
                     Log.e(TAG, "Error parsing endpoint", e);
                 }
             }
         }
 
         return endpoints.toArray(new Endpoint[0]);
     }
 
     /**
      * Parse Endpoint.
      */
     private Endpoint _parseEndpoint(JSONObject object) {
         Endpoint endpoint = new Endpoint();
         endpoint.setToken(APIUtils.getString(object, "token", null));
         endpoint.setType(APIUtils.getString(object, "type", null));
         endpoint.setAddress(APIUtils.getString(object, "name", null));
         endpoint.setName(APIUtils.getString(object, "name", null));
         endpoint.setEndpointSubscriptionsActive(APIUtils.getBoolean(object, "endpointSubscriptionsActive", false));
         endpoint.setUserSubscriptionsActive(APIUtils.getBoolean(object, "userSubscriptionsActive", false));
         endpoint.setData(APIUtils.getObject(object, "data", null));
 
         return endpoint;
     }
 
 }
