 /*
  *  This file is part of SWADroid.
  *
  *  Copyright (C) 2010 Juan Miguel Boyero Corral <juanmi1982@gmail.com>
  *
  *  SWADroid is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  SWADroid is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with SWADroid.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package es.ugr.swad.swadroid.modules;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 import es.ugr.swad.swadroid.Global;
 import es.ugr.swad.swadroid.Preferences;
 import es.ugr.swad.swadroid.R;
 import es.ugr.swad.swadroid.SWADMain;
 import es.ugr.swad.swadroid.model.DataBaseHelper;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.concurrent.TimeoutException;
 
 import org.ksoap2.SoapEnvelope;
 import org.ksoap2.SoapFault;
 import org.ksoap2.serialization.SoapObject;
 import org.ksoap2.serialization.SoapSerializationEnvelope;
 import org.ksoap2.transport.KeepAliveHttpsTransportSE;
 import org.xmlpull.v1.XmlPullParserException;
 
 /**
  * Superclass for encapsulate common behavior of all modules.
  * @author Juan Miguel Boyero Corral <juanmi1982@gmail.com>
  */
 public abstract class Module extends Activity {
     /**
      * SOAP_ACTION param for webservice request.
      */
     String SOAP_ACTION = "";
     /**
      * METHOD_NAME param for webservice request.
      */
     String METHOD_NAME = "";
     /**
      * NAMESPACE param for webservice request.
      */
     String NAMESPACE = "urn:swad";
     /**
      * URL param for webservice request.
      */
     String URL = "swad.ugr.es";
     /**
      * Preferences of the activity.
      */
     Preferences prefs = new Preferences();
     /**
      * Webservice request.
      */
     SoapObject request;
     /**
      * Webservice result.
      */
     protected Object result;
     /**
      * Shows error messages.
      */
     AlertDialog errorDialog = null;
     /**
      * Database Helper.
      */
     protected static DataBaseHelper dbHelper = SWADMain.getDbHelper();
     /**
      * Connection available flag
      */
     protected static boolean isConnected;
     /**
      * Connection timeout in milliseconds
      */
     private static int TIMEOUT = 10000;
     /**
      * Application debuggable flag
      */
     protected static boolean isDebuggable;
     /**
      * Class Module's tag name for Logcat
      */
     public static final String TAG = Global.APP_TAG + " Module";
     
     /**
      * Connects to SWAD and gets user data.
      * @throws NoSuchAlgorithmException
      * @throws IOException
      * @throws XmlPullParserException
      * @throws SoapFault
      * @throws InstantiationException 
      * @throws IllegalAccessException 
      */
     protected abstract void requestService()
     	throws NoSuchAlgorithmException, IOException, XmlPullParserException, SoapFault, IllegalAccessException, InstantiationException;
 
     /**
      * Launches action in a separate thread while shows a progress dialog
      * in UI thread.
      */
     protected abstract void connect();
     
     /**
      * Launches action after executing connect() method 
      */
     protected abstract void postConnect();
     
     /**
      * Gets METHOD_NAME parameter.
      * @return METHOD_NAME parameter.
      */
     public String getMETHOD_NAME() {
         return METHOD_NAME;
     }
 
     /**
      * Sets METHOD_NAME parameter.
      * @param METHOD_NAME METHOD_NAME parameter.
      */
     public void setMETHOD_NAME(String METHOD_NAME) {
         this.METHOD_NAME = METHOD_NAME;
     }
 
     /**
      * Gets NAMESPACE parameter.
      * @return NAMESPACE parameter.
      */
     public String getNAMESPACE() {
         return NAMESPACE;
     }
 
     /**
      * Sets NAMESPACE parameter.
      * @param NAMESPACE NAMESPACE parameter.
      */
     public void setNAMESPACE(String NAMESPACE) {
         this.NAMESPACE = NAMESPACE;
     }
 
     /**
      * Gets SOAP_ACTION parameter.
      * @return SOAP_ACTION parameter.
      */
     public String getSOAP_ACTION() {
         return SOAP_ACTION;
     }
 
     /**
      * Sets SOAP_ACTION parameter.
      * @param SOAP_ACTION SOAP_ACTION parameter.
      */
     public void setSOAP_ACTION(String SOAP_ACTION) {
         this.SOAP_ACTION = SOAP_ACTION;
     }
 
     /**
      * Gets URL parameter.
      * @return URL parameter.
      */
     public String getURL() {
         return URL;
     }
 
     /**
      * Sets URL parameter.
      * @param URL URL parameter.
      */
     public void setURL(String URL) {
         this.URL = URL;
     }
 
     /**
      * Gets preferences of activity.
      * @return Preferences of activity.
      */
     public Preferences getPrefs() {
         return prefs;
     }
 
     /**
      * Sets preferences of activity.
      * @param prefs Preferences of activity.
      */
     public void setPrefs(Preferences prefs) {
         this.prefs = prefs;
     }
 
     /**
      * Gets webservice request.
      * @return Webservice request.
      */
     public SoapObject getRequest() {
         return request;
     }
 
     /**
      * Sets webservice request.
      * @param request Webservice request.
      */
     public void setRequest(SoapObject request) {
         this.request = request;
     }
 
     /**
      * Gets webservice result.
      * @return Webservice result.
      */
     public Object getResult() {
         return result;
     }
 
     /**
      * Sets webservice result.
      * @param result Webservice result.
      */
     public void setResult(Object result) {
         this.result = result;
     }
     
     /**
      * Run connection. Launch Login activity when required
      */
     protected void runConnection()
     {
     	isConnected = connectionAvailable(this);
         if (!isConnected) { 
         	Toast.makeText(this, R.string.errorMsgNoConnection, Toast.LENGTH_SHORT).show();
         } else {
 	        //If this is not the Login module, launch login check
 	        if(!(this instanceof Login)) {
 	        	Intent loginActivity = new Intent(getBaseContext(),
 	        			Login.class);
 	        	startActivityForResult(loginActivity, Global.LOGIN_REQUEST_CODE);
 	        }
         }
     }
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate()
 	 */
 	@SuppressWarnings("static-access")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		//Check if debug mode is enabled
 		try {
 			isDebuggable = (getPackageManager().getApplicationInfo(getPackageName(), 0).FLAG_DEBUGGABLE != 0);
 		} catch (NameNotFoundException e1) {
 			e1.printStackTrace();
 		}
 		
 		if(isDebuggable)
 			Log.d(TAG, "onCreate()");
         
 		super.onCreate(savedInstanceState);
         prefs.getPreferences(getBaseContext());
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onPause()
 	 */
     @Override
     protected void onPause() { 
 		if(isDebuggable)			       
 			Log.d(TAG, "onPause()");
         
         super.onPause();
         if(errorDialog != null) {
             errorDialog.dismiss();
         }
     }
 
 	/* (non-Javadoc)
 	 * @see android.app.ListActivity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy() {
 		if(isDebuggable)
 			Log.d(TAG, "onDestroy()");
 		
 		super.onDestroy();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onRestart()
 	 */
 	@Override
 	protected void onRestart() {
 		if(isDebuggable)
 			Log.d(TAG, "onRestart()");
 		
 		super.onRestart();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		if(isDebuggable)
 			Log.d(TAG, "onResume()");
 		
 		super.onResume();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onStart()
 	 */
 	@Override
 	protected void onStart() {
 		if(isDebuggable)
 			Log.d(TAG, "onStart()");
 		
 		super.onStart();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		if(isDebuggable)
 			Log.d(TAG, "onStop()");
 		
 		super.onStop();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onActivityResult()
 	 */
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(isDebuggable)
 			Log.d(TAG, "onActivityResult()");
 		
         if (resultCode == Activity.RESULT_OK) {
             switch(requestCode) {
 	            case Global.LOGIN_REQUEST_CODE:
                     Global.setLogged(true);
                     //Toast.makeText(getBaseContext(), R.string.loginSuccessfulMsg, Toast.LENGTH_SHORT).show();
                     
             		if(isDebuggable)
             			Log.d(TAG, getString(R.string.loginSuccessfulMsg));
                     
                     if(!(this instanceof Login)) {
                     	connect();
                     }
                     
 	            	break;
             }
         } else {
         	setResult(RESULT_CANCELED);
         }
     }
 
 	/**
      * Creates webservice request.
      */
 	protected void createRequest() {
         request = new SoapObject(NAMESPACE, METHOD_NAME);
         result = null;
     }
 
     /**
      * Adds a parameter to webservice request.
      * @param param Parameter name.
      * @param value Parameter value.
      */
     protected void addParam(String param, Object value) {
         request.addProperty(param, value);
     }
 
     /**
      * Sends a request to the specified webservice in METHOD_NAME class constant.
      * @param cl Class to be mapped
      * @param simple Flag for select simple or complex response
      * @throws IOException
      * @throws SoapFault
      * @throws InstantiationException 
      * @throws IllegalAccessException 
      * @throws XmlPullParserException 
      */
     protected void sendRequest(Class<?> cl, boolean simple)
     	throws IOException, SoapFault, IllegalAccessException, InstantiationException, XmlPullParserException {
 
     	/**
     	 * Use of KeepAliveHttpsTransport deals with the problems with the Android ssl libraries having trouble
     	 * with certificates and certificate authorities somehow messing up connecting/needing reconnects.
     	 */
         KeepAliveHttpsTransportSE connection = new KeepAliveHttpsTransportSE(URL, 443, "", TIMEOUT);
         SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
         System.setProperty("http.keepAlive", "false");
         envelope.setOutputSoapObject(request);
         envelope.addMapping(NAMESPACE, cl.getSimpleName(), cl);
        //connection.debug = true;
     	connection.call(SOAP_ACTION, envelope);
        //Log.d(TAG, connection.requestDump.toString());
        //Log.d(TAG, connection.responseDump.toString());
     	
     	if(simple && !(envelope.getResponse() instanceof SoapFault)) {
     		result = envelope.bodyIn;
     	} else {
     		result = envelope.getResponse();
     	}
     }
     
     /**
      * Checks if any connection is available 
      * @param ctx Application context
      * @return true if there is a connection available, false in other case
      */
     public static boolean connectionAvailable(Context ctx){
         boolean connAvailable = false;
         ConnectivityManager connec =  (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
 
         //Survey all networks (wifi, gprs...)
         NetworkInfo[] networks = connec.getAllNetworkInfo();
         
         for(int i=0; i<2; i++){
             //If any of them has a connection available, put boolean to true
             if (networks[i].isConnected()){
                 connAvailable = true;
             }
         }
         
         //If boolean remains false there is no connection available        
         return connAvailable;
     }
 
     /**
      * Shows an error message.
      * @param message Error message to show.
      */
     protected void error(String message) {
         errorDialog = new AlertDialog
                 .Builder(this)
                 .setTitle(R.string.title_error_dialog)
                 .setMessage(message)
                 .setNeutralButton(R.string.close_dialog,
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         finish();
                     }
                 })
                 .setIcon(R.drawable.erroricon).show();
     }
     
     /**
      * Shows Preferences screen
      */
     protected void viewPreferences() {
     	Intent settingsActivity = new Intent(getBaseContext(),
                 Preferences.class);
         startActivity(settingsActivity);
     }
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu()
 	 */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu_main, menu);
         return true;
     }
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected()
 	 */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.preferences_menu:
             	viewPreferences();
                 return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
 	/**
      * Shows progress dialog when connecting to SWAD
      */
     protected class Connect extends AsyncTask<String, Void, Void> {
         /**
          * Progress dialog
          */
         ProgressDialog dialog = new ProgressDialog(Module.this);
         /**
          * Exception pointer
          */
         Exception e = null;
         String progressDescription;
         int progressTitle;
         boolean showDialog, isLoginModule;
 
 		/**
 		 * Shows progress dialog and connects to SWAD in background
 		 * @param showDialog Flag for show a progress dialog or not
 		 * @param progressDescription Description to be showed in dialog
 		 * @param progressTitle Title to be showed in dialog
 		 * @param isLogin Flag for detect if this is the login module
 		 */
 		public Connect(boolean showDialog, String progressDescription, int progressTitle, Boolean... isLogin) {
 			super();
 			this.progressDescription = progressDescription;
 			this.progressTitle = progressTitle;
 			this.showDialog = showDialog;
 			
 			assert isLogin.length <= 1;
 		    this.isLoginModule = isLogin.length > 0 ? isLogin[0].booleanValue() : false;
 		}
 
 		/* (non-Javadoc)
     	 * @see android.app.Activity#onPreExecute()
     	 */
         @Override
         protected void onPreExecute() { 
     		if(isDebuggable)
     			Log.d(TAG, "onPreExecute()");
         	
         	if(showDialog) {
 	            dialog.setMessage(progressDescription);
 	            dialog.setTitle(progressTitle);
 	            dialog.show();
         	}
         }
 
         /* (non-Javadoc)
     	 * @see android.app.Activity#doInBackground()
     	 */
         @Override
 		protected Void doInBackground(String... urls) {
     		if(isDebuggable)
     			Log.d(TAG, "doInBackground()");
         	
             try {
                 //Sends webservice request
                 requestService();
             /**
              * If an exception occurs, capture, points exception pointer
              * to it and shows error message according to exception type.
              */
             } catch (SoapFault ex) {
             	e = ex;
             } catch (Exception ex) {
             	e = ex;
             }
 
             return null;
         }
 
         /* (non-Javadoc)
     	 * @see android.app.Activity#onPostExecute()
     	 */
         @Override
         protected void onPostExecute(Void unused) {  
         	String errorMsg = "";
         	
     		if(isDebuggable)
     			Log.d(TAG, "onPostExecute()");
         	
         	if(dialog.isShowing()) {
         		dialog.dismiss();
         	}
         	
         	if(e != null) {
                 /**
                  * If an exception has occurred, shows error message according to
                  * exception type.
                  */
                 if(e instanceof SoapFault) {
                     SoapFault es = (SoapFault) e;
             		
             		if(isLoginModule)
             			errorMsg = getString(R.string.errorBadLoginMsg);
             		else
             			errorMsg = es.getMessage();
                 } else if (e instanceof XmlPullParserException) {
                 	errorMsg = getString(R.string.errorServerResponseMsg);
                 } else if (e instanceof IOException) {
                 	errorMsg = getString(R.string.errorConnectionMsg);
                 } else if (e instanceof TimeoutException) {
                 	errorMsg = getString(R.string.errorTimeoutMsg);
                 } else {
                 	errorMsg = e.getMessage();
                 }
 
                 //Request finalized with errors 
         		error(errorMsg);               
         		if(isDebuggable) {    		
         			e.printStackTrace();
         		}
         		
                 setResult(RESULT_CANCELED);
             } else {
         		postConnect();
         	}
         }        
     }    
 }
