 package com.storassa.android.scuolasci;
 
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Random;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
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
    String username, password;
    String customerName;
 
    // get storage info
    SharedPreferences settings;
 
    // snow parameters and views
    double minSnow, maxSnow;
    String lastSnow;
    TextView minSnowText, maxSnowText, lastSnowText;
    FrameLayout fl;
    Button racingBtn, scuderiaBtn, instructorBtn, loginBtn, bookingBtn,
          campioniBtn;
    ImageView adsContainer;
 
    // the enabled buttons
    Feature[] features;
 
    // helper for the connection to the server
    HttpConnectionHelper helper;
 
    // progress dialog for the login
    ProgressDialog progressDialog;
 
    // flag for the recording of credentials
    boolean rememberMe;
 
    // chosen ad
    int ad;
 
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
 
       // add the ads
       Random rand = new Random();
       ad = rand.nextInt(ADS_RESOURCES.length);
       adsContainer.setBackgroundResource(ADS_RESOURCES[ad]);
 
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
             loginUser();
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
       // if (isLogged())
       // helper.exec.shutdown();
       // finish();
    }
 
    @Override
    protected void onDestroy() {
       super.onDestroy();
 
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
 
    public void loginUser() {
 
       LoginFragment loginDialog = new LoginFragment();
       loginDialog.show(getFragmentManager(), "loginDialog");
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
     *           LOGIN: if result[0] = "Incorrect", credentials are not correct,
     *           else it has no meaning
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
                if (progressDialog.isShowing())
                   progressDialog.dismiss();
                CommonHelper.exitMessage(R.string.http_issue_dialog_title,
                      R.string.http_issue, MainActivity.this);
             }
          });
       switch (request) {
       case LOGOUT:
          setLogged(false);
          runOnUiThread(new Runnable() {
 
             @Override
             public void run() {
                loginBtn.setText(R.string.login);
                progressDialog.dismiss();
                scuderiaBtn.setEnabled(false);
                racingBtn.setEnabled(false);
                instructorBtn.setEnabled(false);
                bookingBtn.setEnabled(false);
               campioniBtn.setEnabled(false);
             }
          });
 
          SharedPreferences.Editor editor = settings.edit();
          editor.remove("username").remove("password").remove("remembered")
                .putBoolean("remembered", false).commit();
          break;
       case LOGIN:
          if (result == null)
             runOnUiThread(new Runnable() {
 
                @Override
                public void run() {
                   CommonHelper.exitMessage(R.string.http_issue,
                         R.string.http_issue_dialog_title, MainActivity.this);
                }
             });
          else if (result[0].equals(FAILED_LOGIN_RESPONSE)) {
             runOnUiThread(new Runnable() {
 
                @Override
                public void run() {
                   progressDialog.dismiss();
                   AlertDialog.Builder builder = new AlertDialog.Builder(
                         MainActivity.this);
                   builder.setMessage(R.string.incorrect_login).setTitle(
                         R.string.incorrect_login_title);
                   builder.setPositiveButton(R.string.ok,
                         new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                               dialog.dismiss();
                            }
                         });
 
                   AlertDialog dialog = builder.create();
                   if (!isFinishing())
                      dialog.show();
                }
             });
 
          } else {
             customerName = result[2];
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
          }
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
                   helper = HttpConnectionHelper.getHelper();
                   helper.openGenericConnection(Request.SNOW, MainActivity.this,
                         new URL(WEATHER2_API));
                } catch (Exception e) {
                   e.printStackTrace();
                }
             }
          });
       }
    }
 
    private void checkDataAvailable() {
 
       // check whether data is enabled and in case open DataDisabledDialog
       ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo netInfo = cm.getActiveNetworkInfo();
 
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
 
    private void addButtons(final Feature[] features) {
 
       runOnUiThread(new Runnable() {
 
          @Override
          public void run() {
            bookingBtn.setEnabled(true);
             try {
                for (Feature f : features)
                   if (f != null) {
                      if (f.equals(Feature.RACING_TEAM))
                         racingBtn.setEnabled(true);
                      else if (f.equals(Feature.SCUDERIA))
                         scuderiaBtn.setEnabled(true);
                      else if (f.equals(Feature.INSTRUCTOR))
                         instructorBtn.setEnabled(true);
                     else if (f.equals(Feature.CAMPIONI))
                        campioniBtn.setEnabled(true);
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
 
       campioniBtn = (Button) findViewById(R.id.campioni_btn);
       campioniBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
             Intent newIntent = new Intent(MainActivity.this,
                   WebRenderActivity.class);
             newIntent.putExtra("request", Request.CAMPIONI);
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
             if (isLogged())
                logout();
             else
                loginUser();
 
          }
       });
 
       bookingBtn = (Button) findViewById(R.id.booking_btn);
       bookingBtn.setOnClickListener(new View.OnClickListener() {
 
          @Override
          public void onClick(View arg0) {
             Intent newIntent = new Intent(MainActivity.this,
                   BookingActivity.class);
             newIntent.putExtra("customer", customerName);
             startActivity(newIntent);
          }
       });
       adsContainer = (ImageView) findViewById(R.id.ads_container);
       adsContainer.setOnClickListener(new View.OnClickListener() {
 
          @Override
          public void onClick(View v) {
             Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
             myWebLink.setData(Uri.parse(ADS_URIS[ad]));
             startActivity(myWebLink);
 
          }
       });
    }
 
    private void logout() {
 
       try {
          URL url = new URL(LOGOUT_URI);
          helper = HttpConnectionHelper.getHelper();
          helper.openGenericConnection(Request.LOGOUT, this, url);
 
          CharSequence progressDialogTitle = getResources().getString(
                R.string.logging_out);
          progressDialog = ProgressDialog.show(this, null, progressDialogTitle,
                false);
 
       } catch (MalformedURLException e) {
          e.printStackTrace();
       }
    }
 
    // private static final String WEATHER2_API =
    // "http://www.myweather2.com/Ski-Resorts/Italy/Limone-Piemonte/snow-report.aspx";
    private static final String WEATHER2_API = "http://www.myweather2.com/developer/weather.ashx?uac=Tax7vNwxqd&uref=bc13f25a-d9dc-4f89-9405-aa03b447a3c9";
    private static final String LOGOUT_URI = "http://www.scuolascilimone.com/it/area-riservata/access/signout";
    private static final String FAILED_LOGIN_RESPONSE = "Incorrect";
    private static final int[] ADS_RESOURCES = { R.drawable.botteroski,
          R.drawable.bpn, R.drawable.chalet1400, R.drawable.delmonte,
          R.drawable.noberasco, R.drawable.nobilwood, R.drawable.salice,
          R.drawable.toppa_il_castagno, R.drawable.vergnano, R.drawable.peugeo };
   private static final String[] ADS_URIS = { "http://www.botteroski.com",
         "http://www.bpn.it", "http://chalet1400.baitelimone.it", "http://www.delmonte.com",
         "http://www.noberasco.it", "http://www.nobilwood.it", "http://www.saliceocchiali.it",
         "http://www.mobiliilcastagno.com", "http://www.caffefergnano.com", "http://www.cuneotre.peugeot.it" };
 
 }
