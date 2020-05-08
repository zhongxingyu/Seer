 package com.storassa.android.scuolasci;
 
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.URL;
 import java.util.Random;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements HttpResultCallable {
 
    FragmentManager fm;
    boolean logged = false;
    boolean dataEnabled = false, dataAvailable = false;
    String result = "";
    private BroadcastReceiver networkChangeReceiver;
    String username, password;
 
    // get storage info
    SharedPreferences settings;
 
    // snow parameters and views
    double minSnow, maxSnow;
    String lastSnow;
    TextView minSnowText, maxSnowText, lastSnowText;
    FrameLayout fl;
    Button racingBtn, scuderiaBtn, instructorBtn, loginBtn, bookingBtn;
    ImageView adsContainer;
 
    // the enabled buttons
    Feature[] features;
 
    HttpConnectionHelper helper;
 
    ProgressDialog progressDialog;
    boolean rememberMe;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
 
       setContentView(R.layout.activity_main);
       username = "";
       password = "";
 
       // get all the views
       setViewMember();
       
       // initialize variables
       dataEnabled = false;
       dataAvailable = false;
 
 
       checkDataAvailable();
 
       // define the cookie manager to perform http requests
       CookieManager cookieManager = new CookieManager();
       CookieHandler.setDefault(cookieManager);
 
       // retrieve username and password
       settings = getPreferences(0);
 
      // TODO remove in production code
      SharedPreferences.Editor editor = settings.edit();
      editor.putBoolean("remembered", true).putString("username", "")
            .putString("password", "gualano").commit();


       // add the ads
       int[] res = { R.drawable.botteroski, R.drawable.bpn,
             R.drawable.chalet1400, R.drawable.delmonte, R.drawable.noberasco,
             R.drawable.nobilwood, R.drawable.salice,
             R.drawable.toppa_il_castagno, R.drawable.vergnano };
       Random rand = new Random();
       adsContainer.setBackgroundResource(res[rand.nextInt(res.length)]);
       ;
 
       // add the receiver for data availability
       // addNetworkChangeReceiver();
 
       if (dataAvailable) {
 
          // if the user is already known, retrieve username and password
          if (settings.getBoolean("remembered", false) == true) {
             username = settings.getString("username", "");
             password = settings.getString("password", "");
          }
 
          // login
          if (username != "")
             loginUser(username, password, false);
          else {
             LoginFragment loginDialog = new LoginFragment();
             loginDialog.show(getFragmentManager(), "loginDialog");
          }
 
          // get the meteo information, if data are available
          getMeteoFragment();
 
          // if data are available get the snow report
          getSnowReport();
       }
 
    }
 
    protected void setLogged(boolean _logged) {
       logged = _logged;
    }
 
    protected boolean isLogged() {
       return logged;
    }
 
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
       super.onSaveInstanceState(savedInstanceState);
 
       savedInstanceState.putBoolean("logged", logged);
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // Inflate the menu; this adds items to the action bar if it is present.
       getMenuInflater().inflate(R.menu.activity_main, menu);
       return true;
    }
 
    @Override
    protected void onPause() {
       super.onPause();
       try {
          // unregisterReceiver(networkChangeReceiver);
       } catch (IllegalArgumentException e) {
          e.printStackTrace();
       }
    }
 
    @Override
    protected void onStop() {
       super.onStop();
       if (isLogged())
          helper.exec.shutdown();
       finish();
    }
 
    @Override
    protected void onDestroy() {
       super.onDestroy();
       // try {
       // // unregisterReceiver(networkChangeReceiver);
       // helper.connection.disconnect();
       // } catch (IllegalArgumentException e) {
       // e.printStackTrace();
       // }
    }
 
    @Override
    protected void onResume() {
       super.onResume();
       try {
          // registerReceiver(networkChangeReceiver, filter);
       } catch (IllegalArgumentException e) {
          e.printStackTrace();
       }
    }
 
    public void setDataAvailable() {
       dataAvailable = true;
    }
 
    public void setLoginStatus(boolean status) {
       logged = status;
    }
 
    public void loginUser(String _username, String _password, boolean _rememberMe) {
       helper = HttpConnectionHelper.getHelper();
       helper.openConnection(this, _username, _password);
 
       if (_rememberMe) {
          SharedPreferences.Editor editor = settings.edit();
          editor.putBoolean("remembered", true).putString("username", _username)
                .putString("password", _password).commit();
       }
 
       CharSequence progressDialogTitle = getResources().getString(
             R.string.logging_in);
       progressDialog = ProgressDialog.show(this, null, progressDialogTitle,
             false);
    }
 
    /**
     * This function is the callback of the HttpCallable interface. It provides
     * with the result of the HTTP request
     * 
     * @param request
     *           the type of request; it can be one of the Request enum
     * @param result
     *           the result of the HTTP request: it depends upon the request:
     *           SNOW: result[0] = response code, result[1] = response body
     *           LOGIN: it has no meaning and it is always null
     * @param _features
     *           the features available for the logged user
     */
    @Override
    public void resultAvailable(Request request, String[] result,
          Feature[] _features) {
 
       // if there are problems logging into the server, show the exit dialog
       if (result == null)
          runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                CommonHelper.exitMessage(R.string.http_issue_dialog_title,
                      R.string.http_issue, MainActivity.this);
             }
          });
       switch (request) {
       case LOGIN:
          setLogged(true);
          progressDialog.dismiss();
          runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                loginBtn.setText(R.string.logout);
             }
          });
 
          features = _features;
          addButtons(features);
          break;
       case SNOW:
          // set the snow text
          ParseWeatherHelper whetherHelper = new ParseWeatherHelper(result[1]);
          minSnow = whetherHelper.getMinSnow();
          maxSnow = whetherHelper.getMaxSnow();
          lastSnow = whetherHelper.getLastSnow();
 
          runOnUiThread(new Runnable() {
             public void run() {
                minSnowText.setText(getResources().getString(
                      R.string.meteo_list_min_snow_label)
                      + ": " + String.valueOf(minSnow));
                maxSnowText.setText(getResources().getString(
                      R.string.meteo_list_max_snow_label)
                      + ": " + String.valueOf(maxSnow));
                lastSnowText.setText(getResources().getString(
                      R.string.meteo_list_last_snow_label)
                      + ": " + lastSnow);
             }
          });
          break;
 
       }
    }
 
    /*
     * ------------ PRIVATE METHODS ------------
     */
 
    private void getMeteoFragment() {
 
       // check that data is enabled on the device
       // checkDataAvailable();
 
       // if device is connected to Internet update the meteo
       if (dataAvailable) {
 
          ExecutorService exec = Executors.newCachedThreadPool();
          exec.execute(new Runnable() {
 
             @Override
             public void run() {
                try {
                   fm = getFragmentManager();
                   FragmentTransaction ft = fm.beginTransaction();
                   ft.replace(R.id.meteo_list_placeholder, new MeteoFragment())
                         .commitAllowingStateLoss();
                } catch (Exception e) {
                   e.printStackTrace();
                }
             }
          });
       }
    }
 
    private void getSnowReport() {
       // if device is connected to Internet update the meteo
       if (dataAvailable) {
 
          // get the Weather2 web page
          ExecutorService exec = Executors.newCachedThreadPool();
          exec.execute(new Runnable() {
 
             @Override
             public void run() {
                try {
                   helper.openGenericConnection(Request.SNOW, MainActivity.this,
                         new URL(WEATHER2_API));
                } catch (Exception e) {
                   e.printStackTrace();
                }
             }
          });
       }
    }
 
    private void addNetworkChangeReceiver() {
       IntentFilter filter = new IntentFilter();
       filter.addAction("com.storassa.android.scuolasci.NETWORK_CHANGE");
 
       networkChangeReceiver = new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
 
             // check data connection
             ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
             NetworkInfo netInfo = cm.getActiveNetworkInfo();
 
             // if data connection is now available, get info from Internet
             if (netInfo != null)
                if (netInfo.isConnected()) {
 
                   dataAvailable = true;
 
                   // get the meteo information, if data are available
                   getMeteoFragment();
 
                   // if data are available get the snow report
                   getSnowReport();
                }
          }
       };
 
       registerReceiver(networkChangeReceiver, filter);
 
    }
 
    private void checkDataAvailable() {
 
       // check whether data is enalbed and in case open DataDisabledDialog
       ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo netInfo = cm.getActiveNetworkInfo();
       // if (netInfo == null || !netInfo.isConnectedOrConnecting()) {
       // getNoDataDialog();
       // }
       // // else, if data is enabled but connection is not available, open an
       // // alert dialog
       // else
       if (netInfo == null) {
          // warn the user that Internet is not available
          runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                CommonHelper.exitMessage(R.string.connection_unavailable,
                      R.string.connection_unavailable_title, MainActivity.this);
             }
          });
       } else if (!netInfo.isConnected()) {
          runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                CommonHelper.exitMessage(R.string.http_issue,
                      R.string.http_issue_dialog_title, MainActivity.this);
             }
          });
       } else
          setDataAvailable();
    }
 
    private void getNoDataDialog() {
       DataDisabledDialog dialog = DataDisabledDialog.newInstance(
             "R.string.connection_unavailable", this);
 
       dialog.show(getFragmentManager(), "");
 
    }
 
    private void addButtons(final Feature[] features) {
 
       ExecutorService exec = Executors.newCachedThreadPool();
       exec.execute(new Runnable() {
 
          @Override
          public void run() {
             try {
                for (Feature f : features)
                   if (f != null) {
                      if (f.equals(Feature.RACING_TEAM))
                         racingBtn.setEnabled(true);
                      else if (f.equals(Feature.SCUDERIA))
                         scuderiaBtn.setEnabled(true);
                      else if (f.equals(Feature.INSTRUCTOR))
                         instructorBtn.setEnabled(true);
                   }
             } catch (Exception e) {
                e.printStackTrace();
             }
          }
       });
 
    }
 
    private void setViewMember() {
       minSnowText = (TextView) findViewById(R.id.min_snow_text);
       maxSnowText = (TextView) findViewById(R.id.max_snow_text);
       lastSnowText = (TextView) findViewById(R.id.last_snow_text);
 
       scuderiaBtn = (Button) findViewById(R.id.scuderia_btn);
       scuderiaBtn.setOnClickListener(new View.OnClickListener() {
 
          @Override
          public void onClick(View v) {
             Intent newIntent = new Intent(MainActivity.this,
                   WebRenderActivity.class);
             newIntent.putExtra("request", Request.SCUDERIA);
             startActivity(newIntent);
          }
       });
 
       racingBtn = (Button) findViewById(R.id.racing_team_btn);
       racingBtn.setOnClickListener(new View.OnClickListener() {
 
          @Override
          public void onClick(View v) {
             Intent newIntent = new Intent(MainActivity.this,
                   WebRenderActivity.class);
             newIntent.putExtra("request", Request.RACINGTEAM);
             startActivity(newIntent);
 
          }
       });
 
       instructorBtn = (Button) findViewById(R.id.instructor_btn);
       instructorBtn.setOnClickListener(new View.OnClickListener() {
 
          @Override
          public void onClick(View v) {
             Intent newIntent = new Intent(MainActivity.this,
                   WebRenderActivity.class);
             newIntent.putExtra("request", Request.INSTRUCTOR);
             startActivity(newIntent);
 
          }
       });
 
       loginBtn = (Button) findViewById(R.id.login_logout_btn);
       loginBtn.setOnClickListener(new View.OnClickListener() {
 
          @Override
          public void onClick(View v) {
             // TODO Auto-generated method stub
 
          }
       });
 
       bookingBtn = (Button) findViewById(R.id.booking_btn);
       bookingBtn.setOnClickListener(new View.OnClickListener() {
 
          @Override
          public void onClick(View arg0) {
             Intent newIntent = new Intent(MainActivity.this,
                   BookingActivity.class);
             startActivity(newIntent);
          }
       });
       adsContainer = (ImageView) findViewById(R.id.ads_container);
    }
 
    // private static final String WEATHER2_API =
    // "http://www.myweather2.com/Ski-Resorts/Italy/Limone-Piemonte/snow-report.aspx";
    private static final String WEATHER2_API = "http://www.myweather2.com/developer/weather.ashx?uac=Tax7vNwxqd&uref=bc13f25a-d9dc-4f89-9405-aa03b447a3c9";
    private static final int REPETITION_TIME = 1000;
    private static final int WAITING_TICKS = 10;
 
 }
