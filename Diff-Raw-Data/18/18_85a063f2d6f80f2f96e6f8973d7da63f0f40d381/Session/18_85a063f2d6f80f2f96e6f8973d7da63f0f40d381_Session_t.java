 package com.matji.sandwich.session;
 
 import java.io.NotSerializableException;
 import java.util.ArrayList;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.ViewGroup;
 
 import com.matji.sandwich.Loginable;
 import com.matji.sandwich.R;
 import com.matji.sandwich.Requestable;
 import com.matji.sandwich.data.Badge;
 import com.matji.sandwich.data.MatjiData;
 import com.matji.sandwich.data.Me;
 import com.matji.sandwich.data.User;
 import com.matji.sandwich.data.provider.ConcretePreferenceProvider;
 import com.matji.sandwich.data.provider.DBProvider;
 import com.matji.sandwich.data.provider.PreferenceProvider;
 import com.matji.sandwich.exception.MatjiException;
 import com.matji.sandwich.http.HttpRequestManager;
 import com.matji.sandwich.http.request.MeHttpRequest;
 import com.matji.sandwich.http.request.NoticeHttpRequest;
 import com.matji.sandwich.util.MatjiConstants;
 
 public class Session implements Requestable {
 
     private volatile static Session session = null;
 
     private static final String keyForCurrentUser = "CurrentUser";
     private static final String keyForAccessToken = "AccessToken";
 
     private PreferenceProvider mPrefs;
     private ConcretePreferenceProvider mConcretePrefs;
     private SessionPrivateUtil mPrivateUtil;
     private Context mContext;
     private HttpRequestManager mManager;
     private Loginable mLoginable;
 
     private ArrayList<LoginListener> mLoginListeners; 
     private ArrayList<LogoutListener> mLogoutListeners;
 
     public interface LoginListener {
         public void preLogin();
         public void postLogin();
     }
 
     public interface LogoutListener {
         public void preLogout();
         public void postLogout();
     }
 
     public void addLoginListener(LoginListener listener) {
         mLoginListeners.add(listener);
     }
 
     public void addLogoutListener(LogoutListener listener) {
         mLogoutListeners.add(listener);
     }
 
     private Session(){}
 
     private Session(Context context){
         this.mContext = context;
         this.mPrefs = new PreferenceProvider(context);
         this.mConcretePrefs = new ConcretePreferenceProvider(context);        
         this.mPrivateUtil = new SessionPrivateUtil(context, this);
         this.mLoginListeners = new ArrayList<Session.LoginListener>();
         this.mLogoutListeners = new ArrayList<Session.LogoutListener>();
     }
 
 
     public static Session getInstance(Context context) {
         if(session == null) {
             synchronized(Session.class) {
                 if(session == null) {
                     session = new Session(context);
                 }
             }
         }
 
         return session;
     }
 
     public void sessionValidate(Loginable loginable, ViewGroup layout){
         preLogin();
         this.mLoginable = loginable;
         mManager = HttpRequestManager.getInstance(mContext);
         MeHttpRequest request = new MeHttpRequest(mContext);
         request.actionMe();
         mManager.request(layout, request, HttpRequestManager.AUTHORIZE, this);
         notificationValidate();
     }
 
     public void unsyncSessionValidate() {
         mManager = HttpRequestManager.getInstance(mContext);
         MeHttpRequest request = new MeHttpRequest(mContext);
         request.actionMe();
         ArrayList<MatjiData> data = null;
         try {
             data = request.request();
         } catch (MatjiException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } 
         
         if (data != null && !data.isEmpty() && data.size() > 0) {
             Me me = (Me) data.get(0);
             saveMe(me);
         } else {
             // TODO error
         }
     }
 
    public boolean login(String userid, String password) {
         mManager = HttpRequestManager.getInstance(mContext);
         MeHttpRequest request = new MeHttpRequest(mContext);
         request.actionAuthorize(userid, password);
         try {
             ArrayList<MatjiData> data = request.request();
             Me me = (Me)data.get(0);
             saveMe(me);
             return true;
        } catch (MatjiException e) {
             return false;
         }
         //        mManager.request(spinnerContainer, request, AUTHORIZE, this);
     }
 
 
     public boolean logout(){
         preLogout();
 
         mPrefs.clear();
         removePrivateDataFromDatabase();
 
         postLogout();
 
         return mPrefs.commit();
     }
 
 
     private void removePrivateDataFromDatabase(){
         DBProvider dbProvider = DBProvider.getInstance(mContext);
 
         dbProvider.deleteBookmarks();
         dbProvider.deleteLikes();
         dbProvider.deleteFollowers();
         dbProvider.deleteFollowings();
     }
 
     public boolean isLogin(){
         return (mPrefs.getObject(keyForCurrentUser) == null) ? false : true;	
     }
 
 
     public void saveMe(Me me){
         try {
             mPrefs.setObject(keyForCurrentUser, me.getUser());
         } catch (NotSerializableException e) {
             e.printStackTrace();
         }
 
         mPrefs.setString(keyForAccessToken, me.getToken());
         mPrefs.commit();
 
         removePrivateDataFromDatabase();
 
         DBProvider dbProvider = DBProvider.getInstance(mContext);               
         SQLiteDatabase db = dbProvider.getDatabase();
         try{
             db.beginTransaction();
             dbProvider.insertBookmarks(me.getBookmarks());
             dbProvider.insertLikes(me.getLikes());
             dbProvider.insertFollowers(me.getFollowers());
             dbProvider.insertFollowings(me.getFollowings());
             db.setTransactionSuccessful();
         } catch (SQLException e){
         } finally {
             db.endTransaction();
         }
     }
 
     public void requestCallBack(int tag, ArrayList<MatjiData> data) {
         switch (tag) {
         case HttpRequestManager.AUTHORIZE:
             Me me = (Me)data.get(0);
             saveMe(me);
 
             if (mLoginable != null) 
                 mLoginable.loginCompleted();
 
             postLogin();
             break;
         }
     }
 
     public void requestExceptionCallBack(int tag, MatjiException e) {
         if (tag == HttpRequestManager.AUTHORIZE){
             if (mLoginable != null)
                 mLoginable.loginFailed();
         }
     }
 
     public String getToken() {
         return mPrefs.getString(keyForAccessToken, null);
     }
 
     public PreferenceProvider getPreferenceProvider() {
         return mPrefs;
     }
 
     public ConcretePreferenceProvider getConcretePreferenceProvider() {
         return mConcretePrefs;
     }
 
     public User getCurrentUser(){
         Object obj = mPrefs.getObject(keyForCurrentUser);
         if (obj == null)
             return null;
 
         return (User)obj;
     }
 
     public SessionPrivateUtil getPrivateUtil() {
         return mPrivateUtil;
     }
 
     public void preLogin() {
         Log.d("Matji", "pre login");
         for (LoginListener listener : mLoginListeners) listener.preLogin();
     }
 
     public void postLogin() {
         Log.d("Matji", "post login");
         for (LoginListener listener : mLoginListeners) listener.postLogin();
     }
 
     public void preLogout() {
         Log.d("Matji", "pre logout");
         for (LogoutListener listener : mLogoutListeners) listener.preLogout();
     }
 
     public void postLogout() {
         Log.d("Matji", "post logout");
         for (LogoutListener listener : mLogoutListeners) listener.postLogout();
     }
 
     public void notificationValidate() {
         NoticeHttpRequest request = new NoticeHttpRequest(mContext);
         request.actionBadge(mPrivateUtil.getLastReadNoticeId());
         ArrayList<MatjiData> data = null;
         try {
             data = request.request();
         } catch (MatjiException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         if (data != null && !data.isEmpty() && data.size() > 0) {
             Badge badge = (Badge) data.get(0);
             mPrivateUtil.setNewNoticeCount(badge.getNewNoticeCount());
             mPrivateUtil.setNewAlarmCount(badge.getNewAlarmCount());
         }
     }
 
     public static class LoginAsyncTask extends AsyncTask<Object, Integer, Boolean> {
 
         private ProgressDialog dialog;
 
         private Context context;
         private Loginable loginable;
         private String userid;
         private String password;
         private boolean hasLogin;
 
         public LoginAsyncTask(Context context, Loginable loginable, String userid, String password) {
             this.context = context;
             this.loginable = loginable;
             this.userid = userid;
             this.password = password;
         }
 
         @Override
         protected void onPreExecute() {
             // TODO Auto-generated method stub
             super.onPreExecute();
 
             Session.getInstance(context).preLogin();
 
             dialog = new ProgressDialog(context);
             dialog.setMessage(MatjiConstants.string(R.string.login_loading));
             dialog.setIndeterminate(true);
             dialog.setCancelable(false);
             dialog.show();
         }
 
         @Override
         protected Boolean doInBackground(Object... arg0) {
             // TODO Auto-generated method stub
 
             Session session = Session.getInstance(context);
             hasLogin = session.login(userid, password);
             session.notificationValidate();
 
             return true;
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
             // TODO Auto-generated method stub
             super.onPostExecute(result);
             dialog.dismiss();
 
             if (hasLogin) {
                 loginable.loginCompleted();
                 Session.getInstance(context).postLogin();
             } else {
                 loginable.loginFailed();
             }
         }
 
         @Override
         protected void onProgressUpdate(Integer... values) {
             // TODO Auto-generated method stub
             super.onProgressUpdate(values);
         }
     }
 
     //
     //    /*
     //     *  Async write Me info to local database
     //     */	
     //    private class SaveMeAsyncTask extends AsyncTask<Me, Integer, Boolean>{
     //        ProgressDialog dialog;
     //
     //        @Override
     //        protected void onPreExecute() {
     //            // TODO Auto-generated method stub
     //            super.onPreExecute();
     //        }
     //
     //
     //        private boolean save(Me me){
     //            try {
     //                mPrefs.setObject(keyForCurrentUser, me.getUser());
     //            } catch (NotSerializableException e) {
     //                e.printStackTrace();
     //            }
     //
     //            mPrefs.setString(keyForAccessToken, me.getToken());
     //            mPrefs.commit();
     //
     //            removePrivateDataFromDatabase();
     //
     //            DBProvider dbProvider = DBProvider.getInstance(mContext);               
     //            SQLiteDatabase db = dbProvider.getDatabase();
     //            try{
     //                db.beginTransaction();
     //                dbProvider.insertBookmarks(me.getBookmarks());  
     //                dbProvider.insertLikes(me.getLikes());
     //                dbProvider.insertFollowers(me.getFollowers());
     //                dbProvider.insertFollowings(me.getFollowings());
     //                db.setTransactionSuccessful();
     //            } catch (SQLException e){
     //            } finally {
     //                db.endTransaction();
     //            }
     //
     //            return true;
     //        }
     //
     //
     //        @Override
     //        protected Boolean doInBackground(Me... args) {
     //            // TODO Auto-generated method stub
     //            if (args.length == 0) return false;
     //
     //            return save(args[0]);
     //        }
     //
     //        @Override
     //        protected void onPostExecute(Boolean result) {
     //            // TODO Auto-generated method stub
     //            super.onPostExecute(result);
     //
     //            Log.d("Matji", "Me info saved properly");
     //            dialog.dismiss();
     //        }
     //    }
 
 
 }
