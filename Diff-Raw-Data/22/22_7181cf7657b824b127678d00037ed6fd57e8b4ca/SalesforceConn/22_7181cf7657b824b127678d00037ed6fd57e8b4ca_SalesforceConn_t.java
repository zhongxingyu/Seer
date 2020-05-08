 package edu.channel4.mm.db.android.network;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import roboguice.inject.ContextSingleton;
 import android.content.Context;
 import android.content.SharedPreferences;
 
 import com.google.inject.Inject;
 
 import edu.channel4.mm.db.android.model.callback.AppDescriptionCallback;
 import edu.channel4.mm.db.android.model.callback.EventDescriptionCallback;
 import edu.channel4.mm.db.android.model.callback.EventNameDescriptionCallback;
 import edu.channel4.mm.db.android.model.callback.GraphLoadCallback;
 import edu.channel4.mm.db.android.model.graph.Graph;
 import edu.channel4.mm.db.android.model.graph.GraphFactory;
 import edu.channel4.mm.db.android.model.request.GraphRequest;
 import edu.channel4.mm.db.android.util.Keys;
 import edu.channel4.mm.db.android.util.Log;
 
 // TODO: Change AsyncTasks to be fields of this class. That way, we don't repeat an AsyncTask
 // multiple times before the its first calls finish.
 // TODO: Implement a "cancelAllAsyncTasks()" method that cancels any in-progress AsyncTasks.
 @ContextSingleton
 final public class SalesforceConn {
   public static final String CHANNEL4_REST_URL = "/services/apexrest/";
 
    @Inject private RestClientAccess restClientAccess; // singleton
    @Inject private SharedPreferences prefs;
    @Inject private Context context;
 
    /**
     * Gets the app list from Salesforce. Don't call this method directly?
     */
    // TODO: Figure out how to auto-inject the logic to retrieve the BaseURI and
    // AccessToken, instead of repeatedly retrieving those 3 strings in each of
    // these methods.
    public void getAppListViaNetwork(AppDescriptionCallback callback) {
 
       // Grab the most up-to-date field values
       // TODO: Type-safe these all of these strings using Guice's Binding
       // Annotations:
       // http://youtu.be/hBVJbzAagfs?t=20m39s
       String baseUri = getCurrentBaseUri();
       String accessToken = restClientAccess.getAccessToken();
 
       new GetAppListAsyncTask(context, baseUri, accessToken, callback)
                .execute();
    }
 
    /**
     * Call this when you want to update your list of "attributes"! It will
     * update the list of EventDescriptions, which contains the list of
     * AttributeDescriptions.
     */
    public void getEventListViaNetwork(EventDescriptionCallback callback) {
 
       // Grab the most up-to-date field values
       String baseUri = getCurrentBaseUri();
       String accessToken = restClientAccess.getAccessToken();
       String appLabel = prefs.getString(Keys.APP_LABEL, null);
 
       new GetEventListAsyncTask(context, baseUri, accessToken, appLabel,
                callback).execute();
    }
 
    /**
     * Call this when you want to update your list of "event names"! It will
     * update the list of EventNameDescriptions.
     */
    public void
             getEventNameListViaNetwork(EventNameDescriptionCallback callback) {
 
       // Grab the most up-to-date field values
       String baseUri = getCurrentBaseUri();
       String accessToken = restClientAccess.getAccessToken();
       String appLabel = prefs.getString(Keys.APP_LABEL, null);
 
       new GetEventNameListAsyncTask(context, baseUri, accessToken, appLabel,
                callback).execute();
    }
 
    /**
     * Gets the {@link Graph} for the given {@link GraphRequest} from Salesforce.
     * Don't call this method directly. Instead, call the {@link GraphFactory}.
     * 
     * TODO: Enforce that we shouldn't call this method directly.
     */
    public void getGraphViaNetwork(GraphRequest graphRequest,
             GraphLoadCallback callback) {
 
       // Grab the most up-to-date field values
       String baseUri = getCurrentBaseUri();
       String accessToken = restClientAccess.getAccessToken();
       String appLabel = prefs.getString(Keys.APP_LABEL, null);
 
       new GraphRequestAsyncTask(context, baseUri, accessToken, appLabel,
                graphRequest, callback).execute();
    }
 
    /**
     * Helper method to retrieve the current BaseURI, constructed using the
     * current Instance URL
     */
    private String getCurrentBaseUri() {
       String baseUriString = "";
 
       baseUriString += restClientAccess.getInstanceURL().toString();
       baseUriString += CHANNEL4_REST_URL;
 
       // Sanity check the URI
       try {
          URI uri = new URI(baseUriString);
          return uri.toString();
       }
       catch (URISyntaxException e) {
          Log.toastE(context, e.getMessage());
          return null;
       }
    }
 
 }
