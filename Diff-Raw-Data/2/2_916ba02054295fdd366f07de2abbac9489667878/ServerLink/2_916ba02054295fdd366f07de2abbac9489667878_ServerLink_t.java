 package com.hyperactivity.android_app.network;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.util.Log;
 import com.facebook.Session;
 import com.facebook.model.GraphUser;
 import com.hyperactivity.android_app.Constants;
 import com.hyperactivity.android_app.activities.MainActivity;
 import com.hyperactivity.android_app.core.Engine;
 import com.hyperactivity.android_app.forum.ForumEventCallback;
 import com.hyperactivity.android_app.forum.models.*;
 import com.hyperactivity.android_app.forum.models.Thread;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 
 /*
 //TODO: enable the fb stuff
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.model.GraphUser;
 */
 
 public class ServerLink {
 
     Engine engine;
 
     public ServerLink(Engine engine) {
         this.engine = engine;
 
     }
 
     //---------------------------- ACCOUNT ----------------------------
 
     public void login(Session facebookSession, GraphUser facebookUser, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         String email = "TODO";
         int facebookID = Integer.parseInt(facebookUser.getId());
         String token = facebookSession.getAccessToken();
 //        params.put(Constants.Transfer.EMAIL, email);
         params.put(Constants.Transfer.TOKEN, token);
         sendRequest(Constants.Methods.LOGIN, facebookID, token, params, callback, true);
     }
 
     public void register(Session facebookSession, GraphUser facebookUser, String username, NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         int facebookID = Integer.parseInt(facebookUser.getId());
         String token = facebookSession.getAccessToken();
         params.put(Constants.Transfer.TOKEN, token);
         params.put(Constants.Transfer.USERNAME, username);
         sendRequest(Constants.Methods.REGISTER, facebookID, token, params, callback, true);
     }
 
     public void getAccount(int accountID, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.ACCOUNT_ID, accountID);
 
         sendRequest(Constants.Methods.GET_ACCOUNT, params, callback, lockWithLoadingScreen);
     }
 
     public void updateAccount(String description, boolean showBirthDate, Bitmap avatar, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.DESCRIPTION, description);
         params.put(Constants.Transfer.SHOW_BIRTHDATE, showBirthDate);
 
         //TODO: Also send avatar.
 
         sendRequest(Constants.Methods.UPDATE_PROFILE, params, callback, lockWithLoadingScreen);
     }
 
     public void loadAvatars(final Class callbackMethodType, final List<Account> accounts, final ForumEventCallback callback) {
 
         new AsyncTask<Void, Void, Void>() {
             @Override
             protected void onPostExecute(Void aVoid) {
                 super.onPostExecute(aVoid);
                 if(callbackMethodType.equals(Thread.class)){
                     callback.threadsLoaded();
 
                 }else if(callbackMethodType.equals(Reply.class)){
                     callback.repliesLoaded();
                 }
             }
 
             @Override
             protected Void doInBackground(Void... voids) {
                 for(Account account: accounts){
                    String imageURL = "http://graph.facebook.com/"+account.getFacebookId()+"/picture?width=100&height=100";
                     try {
                         account.setProfilePicture(BitmapFactory.decodeStream((InputStream) new URL(imageURL).getContent()));
                     } catch (IOException e) {
                         Log.d(Constants.Log.TAG, "Loading Picture FAILED");
                         e.printStackTrace();
                     }
                 }
                 return null;
             }
         }.execute();
 
     }
 
     //---------------------------- CATEGORY ----------------------------
 
     public void getForumContent(String type, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.TYPE, type);
 
         sendRequest(Constants.Methods.GET_FORUM_CONTENT, params, callback, lockWithLoadingScreen);
     }
 
     public void getCategoryContent(int categoryID, String type, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.CATEGORY_ID, categoryID);
         params.put(Constants.Transfer.TYPE, type);
 
         sendRequest(Constants.Methods.GET_CATEGORY_CONTENT, params, callback, lockWithLoadingScreen);
     }
 
     public void createCategory(String type, String headline, int colorCode, boolean lockWithLoadinScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.TYPE, type);
         params.put(Constants.Transfer.HEADLINE, headline);
         params.put(Constants.Transfer.COLOR_CODE, colorCode);
 
         sendRequest(Constants.Methods.CREATE_CATEGORY, params, callback, lockWithLoadinScreen);
     }
 
     public void modifyCategory(int categoryID, String type, String headline, int colorCode, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.CATEGORY_ID, categoryID);
         params.put(Constants.Transfer.TYPE, type);
         params.put(Constants.Transfer.HEADLINE, headline);
         params.put(Constants.Transfer.COLOR_CODE, colorCode);
 
         sendRequest(Constants.Methods.MODIFY_CATEGORY, params, callback, lockWithLoadingScreen);
     }
 
     public void deleteCategory(int categoryID, String type, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.CATEGORY_ID, categoryID);
         params.put(Constants.Transfer.TYPE, type);
 
         sendRequest(Constants.Methods.DELETE_CATEGORY, params, callback, lockWithLoadingScreen);
     }
 
     //---------------------------- THREAD ----------------------------
 
     public void getLatestThreads(int limit, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.LIMIT, limit);
 
         sendRequest(Constants.Methods.GET_LATEST_THREADS, params, callback, lockWithLoadingScreen);
     }
 
     public void getThreadContent(int threadID, int sortType, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.THREAD_ID, threadID);
         params.put(Constants.Transfer.SORT_TYPE, sortType);
 
         sendRequest(Constants.Methods.GET_THREAD_CONTENT, params, callback, lockWithLoadingScreen);
     }
 
     public void createThread(int categoryID, String headline, String text, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.CATEGORY_ID, categoryID);
         params.put(Constants.Transfer.HEADLINE, headline);
         params.put(Constants.Transfer.TEXT, text);
 
         sendRequest(Constants.Methods.CREATE_THREAD, params, callback, lockWithLoadingScreen);
     }
 
     public void modifyThread(int threadID, String headline, String text, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.THREAD_ID, threadID);
         params.put(Constants.Transfer.HEADLINE, headline);
         params.put(Constants.Transfer.TEXT, text);
 
         sendRequest(Constants.Methods.MODIFY_THREAD, params, callback, lockWithLoadingScreen);
     }
 
     public void deleteThread(int threadID, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.THREAD_ID, threadID);
 
         sendRequest(Constants.Methods.DELETE_THREAD, params, callback, lockWithLoadingScreen);
     }
 
     //---------------------------- REPLIES ----------------------------
 
     public void createReply(int threadID, String text, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.THREAD_ID, threadID);
         params.put(Constants.Transfer.TEXT, text);
 
         sendRequest(Constants.Methods.CREATE_REPLY, params, callback, lockWithLoadingScreen);
     }
 
     public void modifyReply(int replyID, String text, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.REPLY_ID, replyID);
         params.put(Constants.Transfer.TEXT, text);
 
         sendRequest(Constants.Methods.MODIFY_REPLY, params, callback, lockWithLoadingScreen);
     }
 
     public void deleteReply(int replyID, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.REPLY_ID, replyID);
 
         sendRequest(Constants.Methods.DELETE_REPLY, params, callback, lockWithLoadingScreen);
     }
 
     public void thumbUp(int replyID, boolean lockWithLoadingScreen, final NetworkCallback callback) {
         java.util.Map<String, Object> params = new HashMap<String, Object>();
         params.put(Constants.Transfer.REPLY_ID, replyID);
 
         sendRequest(Constants.Transfer.THUMB_UP, params, callback, lockWithLoadingScreen);
     }
 
     //---------------------------- HELPER METHODS ----------------------------
 
     private void sendRequest(String method, java.util.Map<String, Object> params, final NetworkCallback activityCallback, boolean lockWithLoadingScreen) {
         sendRequest(method, engine.getClientInfo().getAccount().getId(), engine.getClientInfo().getFacebookToken(), params, activityCallback, lockWithLoadingScreen);
     }
 
     private void sendRequest(String method, int id, String facebookToken, java.util.Map<String, Object> params, final NetworkCallback activityCallback, boolean lockWithLoadingScreen) {
         if(facebookToken != null){
             params.put(Constants.Transfer.TOKEN, facebookToken);
         }
         final JSONRPC2Request reqOut = new JSONRPC2Request(method, params, id);
         sendRequest(reqOut, activityCallback, lockWithLoadingScreen);
     }
 
     private void sendRequest(JSONRPC2Request request, final NetworkCallback activityCallback, boolean lockWithLoadingScreen) {
         try {
             AsyncTask asyncTask = new NetworkAsyncTask(activityCallback, lockWithLoadingScreen);
             asyncTask.execute(request);
         } catch (Exception e) {
             Log.e(Constants.Log.TAG, "exception: ", e);
         }
     }
 
 
 
 
 
     /*
     TODO: implement fb stuff
     private Response getFacebookUserInfo() {
         Session session = Session.getActiveSession();
 
         Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
 
             @Override
             public void onCompleted(GraphUser user, Response response) {
             }
         });
         return request.executeAndWait();
     }
     */
 }
