 package org.walkingarchive.app.api;
 
 import helpers.WebHelper;
 
 import java.net.MalformedURLException;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.walkingarchive.app.AsyncTaskCallback;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class WalkingArchiveApi {
     String urlBase;
     private final String TAG = "WalkingArchiveApi";
     
     public WalkingArchiveApi(String urlBase)
     {
         this.urlBase = urlBase;
     }
     
     public WalkingArchiveApi()
     {
         // TODO: I can't decide whether this should be hardcoded or not.
         // I mean, obviously not, but you can't really configure an app
         // the same way as editing a config file (where it should go).
         this("http://dev.mtgwalkingarchive.com:8080");
     }
     
     public String getAbsoluteUrl(String path)
     {
         return urlBase + path;
     }
     
     //**********************************************************************************************
         // CARD & SEARCH
         //**********************************************************************************************
     
     public String getCardByName(String name, int page)
     {
         try
         {
             return WebHelper.GET(getAbsoluteUrl("/card/name/" + WebHelper.sanitize(name) + "/" + page));
         }
         catch(MalformedURLException e)
         {
             // TODO: Log it
             return null;
         }
     }
     
     public void getCardByNameAsync(final String name, final int page, final AsyncTaskCallback callback)
     {
         Runnable asyncRunner = new Runnable()
         {
             public void run()
             {
                 String result = getCardByName(name, page);
                 if(callback != null) callback.run(result);
             }
         };
 
         AsyncTask.execute(asyncRunner);
     }
     
     public void getCardByNameManaTypeAsync(final String name, final String type, 
             final String mana, final int page, final AsyncTaskCallback callback)
     {
         Runnable asyncRunner = new Runnable()
         {
             public void run()
             {
                 String result = getCardByNameManaType(name, type, mana, page);
                 if(callback != null) callback.run(result);
             }
         };
 
         AsyncTask.execute(asyncRunner);
     }
     
     public String getCardByNameManaType(String name, String type, String mana, int page)
     {
         try
         {
             if (name.isEmpty() || name == null) {
                 if (type.equalsIgnoreCase("any type") && mana.equalsIgnoreCase("any color")) {
                     return null;
                 }
                 else if (type.equalsIgnoreCase("any type")) {
                     return WebHelper.GET(getAbsoluteUrl("/card/mana/" + WebHelper.sanitize(mana) + "/" + page));
                 }
                 else if (mana.equalsIgnoreCase("any color")) {
                     return WebHelper.GET(getAbsoluteUrl("/card/type/" + WebHelper.sanitize(type) + "/" + page));
                 }
                 else if (!type.equalsIgnoreCase("any type") && !mana.equalsIgnoreCase("any color")) {
                     return WebHelper.GET(getAbsoluteUrl("/card/type/" + WebHelper.sanitize(type) + "/mana/" + WebHelper.sanitize(mana) + "/" + page)); 
                 }
                 else {
                     //unexpected, return null
                     Log.e(TAG, "Unexpected State");
                     return null;
                 }
             }
             else {
                 if (type.equalsIgnoreCase("any type") && mana.equalsIgnoreCase("any color")) {
                    return WebHelper.GET(getAbsoluteUrl("/card/name/" + WebHelper.sanitize(name)));
                 }
                 else if (type.equalsIgnoreCase("any type")) {
                     return WebHelper.GET(getAbsoluteUrl("/card/name/" + WebHelper.sanitize(name) + "/mana/" + WebHelper.sanitize(mana) + "/" + page));
                 }
                 else if (mana.equalsIgnoreCase("any color")) {
                     return WebHelper.GET(getAbsoluteUrl("/card/name/" + WebHelper.sanitize(name) + "/type/" + WebHelper.sanitize(type) + "/" + page));
                 }
                 else if (!type.equalsIgnoreCase("any type") && !mana.equalsIgnoreCase("any color")) {
                     return WebHelper.GET(getAbsoluteUrl("/card/name/" + WebHelper.sanitize(name) + "/type/" + WebHelper.sanitize(type) + "/mana/" + WebHelper.sanitize(mana) + "/" + page));
                 }
                 else {
                     //unexpected, return null
                     Log.e(TAG, "Unexpected State");
                     return null;
                 }
             }
         }
         catch(MalformedURLException e)
         {
             Log.e(TAG, "Caught MalformedURLException attempting to get a card");
             e.printStackTrace();
             return null;
         }
     }
     
     public String search(String query)
     {
         try
         {
             return WebHelper.GET(getAbsoluteUrl("/card/search/" + WebHelper.sanitize(query)));
         }
         catch(MalformedURLException e)
         {
             // TODO: Log this
             return null;
         }
     }
     
     public void searchAsync(final String query, final AsyncTaskCallback callback)
     {
         Runnable asyncRunner = new Runnable()
         {
             public void run()
             {
                 String result = search(query);
                 if(callback != null) callback.run(result);
             }
         };
         
         AsyncTask.execute(asyncRunner);
     }
     
     
     //**********************************************************************************************
     // TRADE
     //**********************************************************************************************
     
     public String getTradeHistory(Integer userId)
     {
         try
         {
             return WebHelper.GET(getAbsoluteUrl("/trade/user/" +userId.toString()));
         }
         catch (MalformedURLException e)
         {
             // TODO: handle exception
             return null;
         }
     }
     
     public void getTradeHistoryAsync(final Integer userId, final AsyncTaskCallback callback)
     {
         Runnable asyncRunner = new Runnable()
         {
             public void run()
             {
                 String result = getTradeHistory(userId);
                 if(callback != null) callback.run(result);
             }
         };
         
         AsyncTask.execute(asyncRunner);
     }
     
     public String updateTrade(Integer id, List<Integer> giving, List<Integer> receiving)
     {        
         JSONObject json = new JSONObject();
         try
         {
             json.put("id", id);
             json.put("givingCards", new JSONArray(giving));
             json.put("receivingCards", new JSONArray(receiving));
         }
         catch (JSONException e) { }
         
         try
         {
             return WebHelper.POST(getAbsoluteUrl("/trade/update"), json.toString());
         }
         catch (MalformedURLException e)
         {
             return null;
         }
     }
     
     public void updateTradeAsync(final Integer id, final List<Integer> giving, final List<Integer> receiving, final AsyncTaskCallback callback)
     {
         Runnable asyncRunner = new Runnable()
         {
             public void run()
             {
                 String response = updateTrade(id, giving, receiving);
                 if(callback != null) callback.run(response);
             }
         };
         
         AsyncTask.execute(asyncRunner);
     }
     
     public String createTrade(int userId)
     {
         JSONObject json = new JSONObject();
         try
         {
             json.put("user", userId);
         }
         catch (JSONException e) { }
         
         try
         {
             return WebHelper.PUT(getAbsoluteUrl("/trade/add"), json.toString());
         }
         catch (MalformedURLException e)
         {
             return null;
         }
     }
     
     public void createTradeAsync(final int userId, final AsyncTaskCallback callback)
     {
         Runnable asyncRunner = new Runnable()
         {
             public void run()
             {
                 String response = createTrade(userId);
                 if(callback != null) callback.run(response);
             }
         };
         
         AsyncTask.execute(asyncRunner);
     }
     
     //**********************************************************************************************
     // DECK
     //**********************************************************************************************
     
     public String getDecks(Integer userId) {
         try {
             return WebHelper.GET(getAbsoluteUrl("/deck/user/" +userId.toString()));
         }
         catch (MalformedURLException e) {
             // TODO: handle exception
             return null;
         }
     }
     
     public void getDecksAsync(final int userId, final AsyncTaskCallback callback) {
         Runnable asyncRunner = new Runnable() {
             public void run() {
                 String result = getDecks(userId);
                 if(callback != null) callback.run(result);
             }
         };
         
         AsyncTask.execute(asyncRunner);
     }
 
     public String createDeck(int userId, String name) {
         JSONObject json = new JSONObject();
         try {
             json.put("user", userId);
             json.put("name", name);
         }
         catch (JSONException e) {
             //TODO - handle exception
         }
         
         try {
             return WebHelper.PUT(getAbsoluteUrl("/deck/add"), json.toString());
         }
         catch (MalformedURLException e) {
             //log
             return null;
         }
     }
     
     public void createDeckAsync(final Integer userId, final String name, final AsyncTaskCallback callback) {
         Runnable asyncRunner = new Runnable()
         {
             public void run()
             {
                 String response = createDeck(userId, name);
                 if(callback != null) callback.run(response);
             }
         };
         
         AsyncTask.execute(asyncRunner);
     }
 
     public void updateDeckAsync(final int deckId, final String name, final List<Integer> cards, final AsyncTaskCallback callback) {
         Runnable asyncRunner = new Runnable() {
             public void run() {
                 String response = updateDeck(deckId, name, cards);
                 if(callback != null) callback.run(response);
             }
         };
         
         AsyncTask.execute(asyncRunner);
     }
     
     public String updateDeck(Integer id, String name, List<Integer> cards) {
         JSONObject json = new JSONObject();
         try {
             json.put("id", id);
             json.put("name", name);
             json.put("collection", new JSONArray(cards));
         }
         catch (JSONException e) { }
         
         try {
             return WebHelper.POST(getAbsoluteUrl("/deck/update"), json.toString());
         }
         catch (MalformedURLException e) {
             return null;
         }
     }
 }
