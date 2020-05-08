 package fr.utc.assos.payutc;
 
 import java.io.InputStream;
 import java.security.KeyStore;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.SSLSocketFactory;
 
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import fr.utc.assos.payutc.api.AdditionalKeyStoresSSLSocketFactory;
 import fr.utc.assos.payutc.api.ApiTask;
 import fr.utc.assos.payutc.api.POSS;
 import fr.utc.assos.payutc.api.ResponseHandler;
 import fr.utc.assos.payutc.api.responsehandler.DisplayDialogOnError;
 
 /**
  * Demande au seller de badger
  * @author thomas
  *
  */
 public class PaulineActivity extends BaseActivity {
 	public static final String LOG_TAG			= "PaulineActivity";
 	
	/** Fake service used to acquire and validate a CAS ticket */
 	final public static String CAS_SERVICE = "http://localhost";
 	
 	/** POSS Client */
 	public static POSS POSS;
 	
 	/** Image cache */
 	public static ImageCache imageCache;
 	
 	/** ID des Activity */
 	public static final int CASWEBVIEW	= 0;
 	public static final int HOME_ACTIVITY = 1;
 	public static final int SETUP_APP_ACTIVITY = 2;
 	
	/** URL to CAS server (will be retrieved from server) */
	private static String CAS_URL = null; 
 	
     /** Called when the activity is first created. */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(LOG_TAG, "onCreate PaulineActivity");
         setContentView(R.layout.main);
         
         HttpsURLConnection.setDefaultSSLSocketFactory(createAdditionalCertsSSLSocketFactory());
         HostnameVerifier v = new HostnameVerifier() {
         	public boolean verify(String hostname, SSLSession session) {
         		return true;
         	}
         };
         HttpsURLConnection.setDefaultHostnameVerifier(v);
         /*SchemeRegistry schemeRegistry = new SchemeRegistry();
         schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
         schemeRegistry.register(new Scheme("https", createAdditionalCertsSSLSocketFactory(), 443));
         */
         //PBUY = new PBuy(API_HOST, API_PATH, API_NAMESPACE, API_SSL);
         
 	    POSS = new POSS(getString(R.string.api_url) + "POSS3");
 	    new GetCasUrlTask(new GetCasUrlResponseHandler(this)).execute();
         
         imageCache = new ImageCache(getCacheDir());
         
         // uncomment to reset application key and name
         //resetStore(this);
         
        // uncomment to skip login
         //startHomeActivity();
     }
 
     public void onClickLogin(View _view) {
    	int resId = getResources().getIdentifier("debug_cas_ticket", "string", getPackageName());
    	if(resId != 0) {
    		new LoginCasTask(getString(resId), CAS_SERVICE, new LoginCasResHandler(this)).execute();
    	}
    	else {
    		LogByCas();
    	}
     }
     
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d(LOG_TAG, "requestCode:"+requestCode+" ,resultCode:"+resultCode);
 		switch (requestCode) {
 			case CASWEBVIEW:
 				if (resultCode == RESULT_OK) {
 					String ticket = data.getStringExtra("ticket");
 					Log.i(LOG_TAG, "ticket : "+ticket);
 					new LoginCasTask(ticket, CAS_SERVICE, new LoginCasResHandler(this)).execute();
 				}
 			break;
 			case SETUP_APP_ACTIVITY:
 				if (resultCode == RESULT_OK) {
 					new LoginAppTask(new LoginAppRespHandler(this)).execute();
 				}
 			default: break;
 		}
     }
 
     public void LogByCas() {
     	Log.d(LOG_TAG,"startCasWebView");
     	Intent intent = new Intent(this, fr.utc.assos.payutc.CasWebView.class);
     	Bundle b = new Bundle();
    	b.putString("casurl", CAS_URL + "/login?service=" + CAS_SERVICE);
     	intent.putExtras(b);
     	startActivityForResult(intent, CASWEBVIEW);
     }
     
     public void startSetupAppActivity() {
     	Log.d(LOG_TAG,"startHomeActivity");
     	Intent intent = new Intent(this, fr.utc.assos.payutc.SetupAppActivity.class);
     	startActivityForResult(intent,SETUP_APP_ACTIVITY);
     }
     
     public void startFundationsActivity() {
     	Log.d(LOG_TAG,"startFundationsActivity");
     	Intent intent = new Intent(this, fr.utc.assos.payutc.FundationsActivity.class);
     	startActivity(intent);
     }
     
     protected SSLSocketFactory createAdditionalCertsSSLSocketFactory() {
         try {
             final KeyStore ks = KeyStore.getInstance("BKS");
 
             // the bks file we generated above
             final InputStream in = getApplicationContext().getResources().openRawResource( R.raw.mystore);  
             try {
                 // don't forget to put the password used above in strings.xml/mystore_password
                 ks.load(in, getApplicationContext().getString( R.string.mystore_password ).toCharArray());
             } finally {
                 in.close();
             }
 
             return new AdditionalKeyStoresSSLSocketFactory(ks);
             
             /*TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
             tmf.init(ks);
             SSLContext context = SSLContext.getInstance("SSL");
             context.init(null, tmf.getTrustManagers(), null);
             return context.getSocketFactory();*/
 
         } catch( Exception e ) {
             throw new RuntimeException(e);
         }
         
         
     }
     
     /*
      * Allow retry
      * Exit Activity
      * 
      * If this function fails, the application can't go further, so we authorize
      * the user to retry, but we exit if the user does not want to retry.
      */
     protected class GetCasUrlResponseHandler extends DisplayDialogOnError<String> {
 		public GetCasUrlResponseHandler(Activity ctx) {
 			super(ctx, "Echec de la synchronisation avec le serveur", null, true);
 			againListener = new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 					new GetCasUrlTask(GetCasUrlResponseHandler.this).execute();
 				}
 	        };
 		}
 
 		@Override
 		public void onSuccess(String url) {
			CAS_URL = url;
 		}
     	
     }
     
     protected class GetCasUrlTask extends ApiTask<String> {
 
     	public GetCasUrlTask(ResponseHandler<String> handler) {
 			super(PaulineActivity.this, "Synchronization", 
 					"Synchronization avec le serveur en cours...", handler);
 		}
     	
     	@Override
     	protected String callSoap() throws Exception {
     		String url = POSS.getCasUrl();
     		if (url == null || url.isEmpty()) {
     			throw new Exception("L'url du CAS est vide");
     		}
     		return url;
     	}
     }
     
     /*
      * No retry
      * Stay in activity
      * 
      * In case of failure the user can push again the button "Connexion" to login.
      */
     protected class LoginCasResHandler extends DisplayDialogOnError<String> {
 
 		public LoginCasResHandler(Activity ctx) {
 			super(ctx, "Echec de l'identification");
 		}
 
 		@Override
 		public void onSuccess(String sellerLogin) {
 			mSession.setSellerLogin(sellerLogin);
 			if (getKey(PaulineActivity.this) != null) {
     			new LoginAppTask(new LoginAppRespHandler(ctx)).execute();
     		}
     		else {
     			startSetupAppActivity();
     		}
 		}
     	
     }
 
     private class LoginCasTask extends ApiTask<String> {
     	private String ticket, service;
     	
     	public LoginCasTask(String ticket, String service, ResponseHandler<String> handler) {
     		super(PaulineActivity.this, "Identification", 
     				"Connection au serveur en cour...", handler);
     		this.ticket = ticket;
     		this.service = service;
     	}
     	
     	@Override
     	protected String callSoap() throws Exception {
     		String seller = POSS.loginCas(ticket, service);
     		if (seller == null || seller.isEmpty()) {
     			throw new Exception("Erreur de login Cas, valeur retournée vide.");
     		}
     		return seller;
     	}
     }
     
     /*
      * TODO Comportement not fixed yet, maybe a retry should be allowed ? Shall we exit the
      * activity on failure ? Shall we allow here to go to the application setup ?
      */
     protected class LoginAppRespHandler extends DisplayDialogOnError<Boolean> {
 		public LoginAppRespHandler(Activity ctx) {
 			super(ctx, "Echec de l'identification");
 		}
 
 		@Override
 		public void onSuccess(Boolean _osef) {
 			startFundationsActivity();
 		}
     	
     }
 
     protected class LoginAppTask extends ApiTask<Boolean> {
     	public LoginAppTask(ResponseHandler<Boolean> handler) {
     		super(PaulineActivity.this, "Connexion", 
     				"Connexion au serveur...",  handler);
     	}
     	
     	@Override
     	protected Boolean callSoap() throws Exception {
     		String key = PaulineActivity.getKey(PaulineActivity.this);
     		return PaulineActivity.POSS.loginApp(key);
     	}
     }
     
     public static void storeRemove(Activity a, String key) {
     	SharedPreferences prefs = a.getSharedPreferences("prefs", MODE_PRIVATE);
     	SharedPreferences.Editor editor = prefs.edit();
     	editor.remove(key);
     	editor.commit();
     }
     
     public static void storeSet(Activity a, String key, String val) {
     	SharedPreferences prefs = a.getSharedPreferences("prefs", MODE_PRIVATE);
     	SharedPreferences.Editor editor = prefs.edit();
     	editor.putString(key, val);
     	editor.commit();
     }
 
     public static String storeGet(Activity a, String key) {
     	SharedPreferences prefs = a.getSharedPreferences("prefs", MODE_PRIVATE);
     	return prefs.getString(key, null);
     }
     
     public static String getKey(Activity a) {
     	return storeGet(a, "app_key");
     }
     
     public static void setKey(Activity a, String key) {
     	storeSet(a, "app_key", key);
     }
     
     public static String getAppName(Activity a) {
     	return storeGet(a, "app_name");
     }
     
     public static void setAppName(Activity a, String name) {
     	storeSet(a, "app_name", name);
     }
     
     public static void resetStore(Activity a) {
     	storeRemove(a, "app_key");
     	storeRemove(a, "app_name");
     }
 
 }
