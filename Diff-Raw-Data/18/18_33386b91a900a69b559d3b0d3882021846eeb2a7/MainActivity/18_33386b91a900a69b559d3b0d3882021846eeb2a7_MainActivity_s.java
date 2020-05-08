 /*
 * Copyright (C) 2013 Juan Pons (see README for details)
 * This file is part of miAulaVirtual.
 *
 * miAulaVirtual is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * miAulaVirtual is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with miAulaVirtual. If not, see <http://www.gnu.org/licenses/agpl.txt>.
 *
 */
 
 package com.jp.miaulavirtual;
 
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.util.Log;
 
 
 public class MainActivity extends Activity {
 	
     // flag for Internet connection status
     Boolean isInternetPresent = false;
     // Connection detector class
     ConnectionDetector cd;
 	
 	// Service
 	CurlService cURL;
 	Intent i;
 	
 	// User data
 	String user;
 	String pass;
 	String url;
 	
 	String tag = "Lifecycle";
 	// Receiver from service
 	private DataUpdateReceiver dataUpdateReceiver;
 	
 	//BroadcastReceiver, recibe variables de nuestro servicio posteriormente ejecutado CurlService
 	private class DataUpdateReceiver extends BroadcastReceiver {
 	    @Override
 	    public void onReceive(Context context, Intent intent) {
 	        if (intent.getAction().equals(CurlService.LOGGED)) {
 	        	boolean logged = intent.getBooleanExtra("logged", true);
 	        	if(!logged) { 
 	        		setContentView(R.layout.activity_main); 
 	        		Toast.makeText(getBaseContext(),getString(R.string.bad_data), Toast.LENGTH_LONG).show();
 	        		stopService(i);
 	        		unbindService(CurlConnection);
 	        	} else {
 	        		stopService(i);
 	        		unbindService(CurlConnection);
 	        		finish();
 	        	}
 	        }
 	    }
 	}
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // creating connection detector class instance
         cd = new ConnectionDetector(getApplicationContext());
         isInternetPresent = cd.isConnectingToInternet();
         
         // SharedPreference instance
         PreferenceManager.setDefaultValues(this, R.xml.apppreferences, false);
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         
         // Just in case...we delete the cookie, again.
         Editor editor = prefs.edit();
     	editor.remove("cookies");
     	editor.commit();
         
     	// Getting user data
         user = prefs.getString("myuser", "Ninguno");
         pass = prefs.getString("mypass", "Ninguno");
         url = "/dotlrn/?page_num=2";
        
        if(user!="Ninguno" && user!=""){ //Si el usuario entra a la aplicación y ya está logueado
         	setContentView(R.layout.activity_main2); //Content si está logueado - Loader mientras espera el scrap
         	
         	Log.d("DATOS", user);
             Log.d("DATOS", pass);
             
             if(isInternetPresent) {
 	        	i = new Intent(this, CurlService.class);
 	            bindService(i, CurlConnection, Context.BIND_AUTO_CREATE); //conectamos el servicio
             } else {
             	setContentView(R.layout.activity_main);
             	Toast.makeText(getBaseContext(),getString(R.string.no_internet), Toast.LENGTH_LONG).show();
             }
 
         	/** String [] user_data;
         	user_data = new String[2];
         	user_data[0] = user;
         	user_data[1] = pass;
         	
         	intent.putExtra(USER_DATA, user_data); //message = qué mostrar en la nueva actividad al pulsar el boton de "Login" **/
         } else {
         setContentView(R.layout.activity_main); //Content si no está logueado
         }
         Log.d(tag, "In the onCreate() event");
         if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
         IntentFilter intentFilter = new IntentFilter(CurlService.LOGGED);
         registerReceiver(dataUpdateReceiver, intentFilter);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.activity_main, menu);
         return true;
     }
     
     /**
      * Acci�n para cada Item del Menu
      */
     public boolean onOptionsItemSelected(MenuItem item){
         /*El switch se encargar� de gestionar cada elemento del men� dependiendo de su id,
         por eso dijimos antes que ning�n id de los elementos del men� podia ser igual.
         */
         switch(item.getItemId()){
         case R.id.preferencias: //Nombre del id del men�, para combrobar que se ha pulsado
         	Log.d(tag, "Preferencias");
         	startActivity(new Intent(this, SettingsActivity.class));;
         }
         return true;
     }
     
     
     public void onStart()
     {
         super.onStart();
         Log.d(tag, "In the onStart() event");
     }
     
     public void onRestart()
     {
         super.onRestart();
         isInternetPresent = cd.isConnectingToInternet();
         Log.d(tag, "In the onRestart() event"); 
     }
     
     public void onResume()
     {
         super.onResume();
         Log.d(tag, "In the onResume() event");
     }
     
     public void onPause()
     {
         super.onPause();
         Log.d(tag, "In the onPause() event");
     }
     
     public void onStop()
     {
         super.onStop();
         Log.d(tag, "In the onStop() event");  
     }
     
     public void onDestroy()
     {
         super.onDestroy();
         Log.d(tag, "In the onDestroy() event");
         if(dataUpdateReceiver!=null) unregisterReceiver(dataUpdateReceiver);
     }
     
     /** Llamada cuando el usuario hace clic en 'Enviar' */
     public void sendMessage(View view) {
     	/**
          * Habr� que guardar el Usuario y Contrase�a si es la primera vez que loguea (es la primera vez que loguea porque se envia el SEND.
          * 
          */
     	isInternetPresent = cd.isConnectingToInternet();
     	if(isInternetPresent) {
 	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	        Editor editor = prefs.edit();
 	        EditText tuser = (EditText) findViewById(R.id.user);
 	    	EditText tpass = (EditText) findViewById(R.id.pass);
 	    	//Incertamos el usuario y la contrase�a en las preferencias
 	        editor.putString("myuser", tuser.getText().toString());
 	        editor.putString("mypass", tpass.getText().toString());
 	        editor.commit();
 	    	
 	        //El usuario ha logueado, ponemos la pantalla de carga
 	        setContentView(R.layout.activity_main2);
 	        
 	        //Actualizamos el usuario y contrase�a
 	        user = tuser.getText().toString();
 	        pass = tpass.getText().toString();
 	        url = "/dotlrn/?page_num=2";
 	        
 	        Log.d("DATOS2", user);
 	        Log.d("DATOS2", pass);
 	        
 	        // Iniciamos el servicio de Scrap
 	        i = new Intent(this, CurlService.class);
 	        bindService(i, CurlConnection, Context.BIND_AUTO_CREATE); //conectamos el servicio
 	        
 	        if (dataUpdateReceiver == null) dataUpdateReceiver = new DataUpdateReceiver();
 	        IntentFilter intentFilter = new IntentFilter(CurlService.LOGGED);
 	        registerReceiver(dataUpdateReceiver, intentFilter);
     	} else {
     		Toast.makeText(getBaseContext(),getString(R.string.no_internet), Toast.LENGTH_LONG).show();
     	}
     }
 
     /** Defines callbacks for service binding, passed to bindService() */
     ServiceConnection CurlConnection = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
         	cURL = ((CurlService.CurlBinder)service).getService();
         	// Pasamos variables al Servicio
         	cURL.user = user;
         	cURL.pass = pass;
         	cURL.url = url;
         	cURL.isDocument = false;
             startService(i);
             Log.d("Service", "Servicio Conectado");
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
         	cURL = null;
         	Log.d("Service", "Servicio Desconectado");
         }
     };
     
 }
