 package com.github.yolozlave.atractivoschile;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 
 public class Splash extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_splash);
        Thread timer = new Thread() {
        	public void run() {
        		try {
					sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
						Bundle caja = new Bundle();
						caja.putInt("numero", 10);
						caja.putBoolean("activo", true);
						caja.putString("texto", "Este es un texto");
						
						Intent principal = new Intent(Splash.this,Regiones.class);
						principal.putExtras(caja);
						
						startActivity(principal);
						finish();
				}
        	}
        };
        	timer.start();
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.splash, menu);
         return true;
     }
     
 }
