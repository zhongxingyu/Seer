 package rex.login;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.parse.FindCallback;
 import com.parse.LogInCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseFacebookUtils;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.facebook.Facebook;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import rex.login.service.IAppService;
 //import stock.ticker.MyApps;
 //import stock.ticker.R;
 
 public class RexLoginActivity extends Activity implements OnClickListener
 {
     protected static final String LOGGING_TAG = "AppRex";
     private IAppService appService;
     private ArrayList<AppInfo> appInfo;
     private AppsDb db;
     private boolean bound = false;
     Button login;
     String currentUser = null;
     boolean userInfoProcessed = false;
     private Map<String, AppAttributes> appAttributes;
     private final String mAppAttributesName = "AppAttr";
 
     @Override
     public void onStart()
     {
         Log.d("RexLogin", "Started app");
         super.onStart();
         db = new AppsDb(this);
         AppInfoHelper.create(db);
         List<AppAttributes> attrs = db.getAttributes(null);
         for(AppAttributes aa: attrs)
         {
             appAttributes.put(aa.getPackageName(), aa);
         }
         // create initial list
         if (!bound)
         {
             bound = bindService(new Intent(RexLoginActivity.this,
                     MonitorService.class), connection, Context.BIND_AUTO_CREATE);
             Log.d(LOGGING_TAG, "Bound to service: " + bound);
             Toast.makeText(getApplicationContext(), "Bound to service",
                     2000).show();
         }
         if (!bound)
         {
             Log.e(LOGGING_TAG, "Failed to bind to service");
             throw new RuntimeException("Failed to find to service");
         }
         if(appService != null)
             processUserInfo();
     }
 
     // Connection to the stock service, handles lifecycle events
     private ServiceConnection connection = new ServiceConnection()
     {
 
         public void onServiceConnected(ComponentName className, IBinder service)
         {
             appService = IAppService.Stub.asInterface(service);
             Log.d(LOGGING_TAG, "Connected to service");
             Toast.makeText(getApplicationContext(), "Connected to service",
                     2000).show();
             try
             {
                 appInfo = (ArrayList<AppInfo>) appService.getAppInfo();
                 if (appInfo == null)
                 {
                     appInfo = new ArrayList<AppInfo>(0);
                     Log.d(LOGGING_TAG, "No appInfo returned from service");
                 } else
                 {
                     Log.d(LOGGING_TAG, "Got " + appInfo.size()
                             + " appInfo from service");
                 }
 
                 refresh();
             } catch (RemoteException e)
             {
                 Log.e(LOGGING_TAG,
                         "Exception retrieving portfolio from service", e);
             }
             catch(Exception e)
             {
                 Toast.makeText(getApplicationContext(), e.toString(),
                         2000).show();
             }
             if(userInfoProcessed == false)
                 processUserInfo();
         }
 
         public void onServiceDisconnected(ComponentName className)
         {
             appService = null;
             Log.d(LOGGING_TAG, "Disconnected from service");
         }
 
     };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         appAttributes = new HashMap<String, AppAttributes>();
         Log.d("RexLogin", "On Create called");
         super.onCreate(savedInstanceState);
 //        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
 //        boolean startService = true;
 //        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 //            if (MonitorService.class.getName() .equals(service.service.getClassName())) {
 //                startService = false;
 //                break;
 //            }
 //            
 //        }
 //        if(startService)
 //        {
 //          startService(new Intent(this, MonitorService.class));
 //          Toast.makeText(getApplicationContext(), "Starting Service",
 //                 2000).show();
 //        }
         
         setContentView(R.layout.main);
 
         Button button2 = (Button) findViewById(R.id.button2);
         button2.setOnClickListener(new View.OnClickListener(){
         	public void onClick(View view){
         		Intent myIntent = new Intent(view.getContext(), MyApps.class);
         		startActivityForResult(myIntent, 0);
         	}
         });
         
         Button createAccount = (Button) findViewById(R.id.createAccount);
         login = (Button) findViewById(R.id.login);
         createAccount.setOnClickListener(this);
         login.setOnClickListener(this);
         Parse.initialize(this, "gentv1lxXI5DEP7K3kQbfNOOIIUoVSdSwTu8RQ8d",
                 "S1BixT6dROphY1hYMn2JRv3BZuNSRZcEibpyeeaj");
         ParseFacebookUtils.initialize("254743831280356");
         ParseUser cur = ParseUser.getCurrentUser();
         boolean needLogin = true;
         if(cur != null)
         {
             if(ParseFacebookUtils.isLinked(cur))
             {
                 needLogin = false;
             }
         }
         if(needLogin)
             login.setText(R.string.Login);
         else
             login.setText(R.string.Logout);
         
     }
 
     public void onClick(View login)
     {
         
         EditText userName = (EditText) findViewById(R.id.userName);
         EditText passWord = (EditText) findViewById(R.id.passWord);
         Button clicked = (Button) login;
         if ((userName == null) || (passWord == null) || (clicked == null))
             return;
         String uName = userName.getText().toString();
         String pWord = passWord.getText().toString();
         switch (clicked.getId())
         {
         case R.id.login:
             if(clicked.getText().toString().equals(getResources().getString(R.string.Login)))
                 login(uName, pWord);
             else
                 logout();
             break;
         case R.id.createAccount:
             createUser(uName, pWord);
             break;
         }
     }
 
     class CategoryResult extends FindCallback
     {
         String pkgName;
         String appName;
         Context ctxt;
         CategoryResult(String pname, String aname, Context context)
         {
             ctxt = context;
             pkgName = pname;
             appName = aname;
         }
         @Override
         public void done(List<ParseObject> apps, ParseException e)
         {
             if(apps == null)
             {
                 if(e != null)
                     Toast.makeText(getApplicationContext(), e.toString(),
                             2000).show();
                 return;
             }
             if(apps.size() != 0)
             {
                 for(ParseObject app: apps)
                 {
                     String cat = app.getString("category");
                     String icon = app.getString("icon");
                     AppAttributes attr = appAttributes.get(pkgName);
                     if(cat != "na")
                     {
                         if(attr == null)
                             attr = new AppAttributes();
                         attr.setAppName(appName);
                         attr.setPackageName(pkgName);
                         attr.setCategory(cat);
                         attr.setIcon(icon);
                         appAttributes.put(pkgName, attr);
                         if(!icon.contentEquals("na"))
                         {
                             String extension = "";
                             int extensionIndex = icon.lastIndexOf(".");
                             if(extensionIndex >= 0)
                                 extension = icon.substring(extensionIndex);
                             File dirName = ctxt.getFilesDir();
                            File fname = new File(dirName, pkgName + extension);
                             ImageDownloader.DownloadFromUrl(icon, fname.toString());
                             db.setAttributes(pkgName, null, cat, fname.toString());
                         }
                         else
                             db.setAttributes(pkgName, null, cat, "na");
 
                     }
                     Toast.makeText(getApplicationContext(), "Got category " + cat + " icon " + icon + " for " + pkgName,
                             1000).show();
                 }
             }
             else
             {                
                 ParseObject catInfo = new ParseObject(mAppAttributesName);
                 Log.d("Parse", "New Object");
                 catInfo.put("pkgName", pkgName);
                 if(appName != null)
                     catInfo.put("appName", appName);
                 else
                     Toast.makeText(getApplicationContext(), "No appname for " + pkgName,
                             2000).show();
                 catInfo.put("category", "na");
                 catInfo.put("icon", "na");
                 catInfo.saveInBackground();
                 appAttributes.remove(pkgName);  // Ask again
                 Toast.makeText(getApplicationContext(), "No category for " + pkgName,
                         2000).show();
             }
         }
     }
     public void refreshList()
     {
         
     }
     public void createUser(String uname, String pword)
     {
         ListView v = (ListView) findViewById(R.id.applist);
         ArrayList<String> values = new ArrayList<String>();
         try
         {
             // Get app info from the db
             List<AppInfo> appInfo = db.getAppInfo(null);
             for(AppInfo app: appInfo)
             {
                 String pname = app.getPackageName();
                 Log.d("Parse", "Got " + app.getPackageName());
                 String aname = null;
                 String category = null;
                 AppAttributes attr = appAttributes.get(pname);
                 if(attr != null && attr.getCategory().length() != 0)
                 {
                     // If the app is already in the database, then use it
                     aname = attr.getAppName();
                 }
                 else
                 {
                     // Otherwise get the information from the cloud
                     // Put in as pending so we don't ask twice
                     List<AppAttributes> aas = db.getAttributes(pname);
                     if(aas.size() != 0)
                     {
                         AppAttributes aa = aas.get(0);
                         aa.setPackageName(pname);
                         aa.setAppName(aname);
                         aa.setCategory("pending");
                         appAttributes.put(pname, aa);
                         ParseQuery q = new ParseQuery(mAppAttributesName);
                         q.whereEqualTo("pkgName", pname);
                         q.findInBackground(new CategoryResult(pname, aname, this));
                     }
                 }
             }
         } 
         catch (Exception e)
         {
             Toast.makeText(getApplicationContext(), e.toString(),
                     2000).show();
             
         }
         //values.add(app.toString());
         
         List<String> cats = AppInfoHelper.instance().getCategories();
         for(String cat: cats)
         {
             values.add("Category: " + cat);
             List<AppInfoHelper.AppSummary>appsByUsage = AppInfoHelper.instance().getAppsSortedByUsage(cat);
             for(AppInfoHelper.AppSummary sum: appsByUsage)
             {
                 values.add(sum.appName);
                 
             }
         }
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_list_item_1, android.R.id.text1, values);
         v.setAdapter(adapter);
 
         
         Toast.makeText(getApplicationContext(), "Users created from Facebook",
                 2000).show();
     }
 
     public void logout()
     {
         ParseUser.logOut();
         login.setText(R.string.Login);
     }
     
     public String makeFbId(String id)
     {
         return "fb_" + id;
     }
     
     public void processUserInfo()
     {
         userInfoProcessed = true;
         Facebook fb = ParseFacebookUtils.getFacebook();
         try
         {
             String userData = fb.request("me");
             JSONObject res = new JSONObject(userData);
             String myName = res.getString("name");
             String myId = res.getString("id"); 
             UserInfo info = UserInfo.getUserInfo();
             currentUser = makeFbId(myId);
             info.setUser(new User(myName, currentUser));
             try
             {
                 Log.d("Parse", "Setting user name in service: " + currentUser);
                 this.appService.setUserName(currentUser);
             } catch (RemoteException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             catch (Exception e)
             {
                 Toast.makeText(getApplicationContext(), e.toString(),
                         2000).show();
             }
             Log.d("Parse", "Name: " + myName + " Id: " + myId);
             
             userData = fb.request("me/friends");
             res = new JSONObject(userData);
             JSONArray friends = (JSONArray) res.get("data");
             for(int n = 0; n < friends.length(); ++n)
             {
                 JSONObject friend = friends.getJSONObject(n);
                 String name = friend.getString("name");
                 String id = friend.getString("id");
                 info.addFriend(new User(name, makeFbId(id)));
             }
             Log.d("Parse", userData);
         } catch (MalformedURLException e1)
         {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         } catch (IOException e1)
         {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         } catch (JSONException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
     }
     
     public void login(String uname, String pword)
     {
         Log.d("Parse", "Starting Facebook Login");
         ParseFacebookUtils.logIn(this, new LogInCallback()
         {
             @Override
             public void done(ParseUser user, ParseException err)
             {
                 if (user == null)
                 {
                     Log.d("Parse",
                             "Uh oh. The user cancelled the Facebook login.");
                     login.setText(R.string.Login);
                 } else if (user.isNew())
                 {
                     Log.d("Parse",
                             "User signed up and logged in through Facebook!");
                     login.setText(R.string.Logout);
                     processUserInfo();
                 } else
                 {
                     Log.d("Parse", "User logged in through Facebook!");
                     login.setText(R.string.Logout);
                     processUserInfo();
                 }
             }
         });
 
         /*
          * ParseUser.logInInBackground(uname, pword, new LogInCallback() {
          * public void done(ParseUser user, ParseException e) { if (e == null &&
          * user != null) { } else if (user == null) { // Sign up didn't succeed.
          * The username or password was invalid. } else { // There was an error.
          * Look at the ParseException to see what happened. }
          * 
          * } });
          */
     }
 
     private void refresh()
     {
         Log.d(LOGGING_TAG, "Refreshing UI with new data");
         try
         {
             appInfo = db.getAppInfo(null);
             if (appInfo == null)
             {
                 appInfo = new ArrayList<AppInfo>(0);
                 // Log.d(LOGGING_TAG, "No appInfo returned from service");
             }
             // else
             // {
             // Log.d(LOGGING_TAG, "Got "+ appInfo.size()
             // +" appInfo from service");
             // }
 
         } catch (Exception e)
         {
 
         }
 
         // BaseAdapter adapter = (BaseAdapter) this.getListAdapter();
         // adapter.notifyDataSetChanged();
     }
 }
