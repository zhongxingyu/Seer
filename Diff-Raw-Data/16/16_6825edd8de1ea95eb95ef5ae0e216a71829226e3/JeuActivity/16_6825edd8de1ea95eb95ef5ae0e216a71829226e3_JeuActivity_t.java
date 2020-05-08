 package fr.ups.m2dl.ter.challenge;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 public class JeuActivity extends Activity implements SensorEventListener {
 	
 	MediaPlayer _mp;
 	
 	private boolean _estInitialise = false;
 	
 	private SensorManager mSensorManager;
 	private Sensor mSensor;
 	
 	private int _etat;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.activity_jeu);
 		
 		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
 		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
 		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
 		super.onCreate(savedInstanceState);
 		MainActivity.__moteur.recapitulatifPartie();
 		afficherFenetre();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 //		getMenuInflater().inflate(R.menu.activity_jeu, menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.activity_jeu, menu);
 		return true;
 	}
 	
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 			
 			double x = event.values[0];
 			double y = event.values[1];
 			double z = event.values[2];
 			
 			String mouvement = "";
 			
 			if (x > 8) {
 				mouvement = "Gauche";
 			}
 			
 			if (x < -8) {
 				mouvement = "Droite";
 			}
 			
 			if (y > 8) {
 				mouvement = "Bas";
 			}
 			
 			if (y < -8) {
 				mouvement = "Haut";
 			}
 			
 			if (!_estInitialise && x < 2 && x > -2 && y < 2 && y > -2) {
 				Log.v("INFO", "INITIALISATION !");
 				_estInitialise = true;
 			}
 			
 			if (!mouvement.equals("") && _estInitialise) {
 				_estInitialise = false;
 				mouvement(mouvement);
 			}
 		}
 	}
 
 	public void afficherFenetre() {
 		// Recuperer joueur
 		int joueur = MainActivity.__moteur.joueurActuel();
 		TextView tvReprod;
 		// Generer string label_joueur
 		String label_joueur = "Joueur " + joueur;
 		// Recuperer action
 		String action = MainActivity.__moteur.action();
 		// Generer string label_action
 		String label_action = action;
 		// Si reproduire : label_reprod = mouvement / label_reprod = ""
 		String label_reprod = "";
 		if (action.equals("REPRODUIRE") && !MainActivity.__moteur.is_expert()) {
 			label_reprod = MainActivity.__moteur.mouvementARepeter();
 			//textView label_reprod
 			tvReprod = (TextView) findViewById(R.id.label_reprod);
 			tvReprod.setText(label_reprod);
 		}
 		else {
 			tvReprod = (TextView) findViewById(R.id.label_reprod);
 			tvReprod.setText("");
 		}
 		//textView label_joueur
 		TextView tvJoueur = (TextView) findViewById(R.id.label_joueur);
 		tvJoueur.setText(label_joueur);
 		//textView label_action
 		TextView tvAction = (TextView) findViewById(R.id.label_action);
 		tvAction.setText(label_action);
 		
 		
 	}
 
	public void mouvement(String mouvement) {		
 		MainActivity.__moteur.prochainMouvement(mouvement);
 		if (!MainActivity.__moteur.is_partieEnCours()) {
			_mp = MediaPlayer.create(this, R.raw.error);
			_mp.start();
 			Intent intent = new Intent(JeuActivity.this, GameOverActivity.class);
 			startActivity(intent);
 			finish();
		} else {
			_mp = MediaPlayer.create(this, R.raw.beep_seven);
			_mp.start();
			afficherFenetre();
 		}
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 	
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_settings:
             	System.exit(RESULT_OK);
             	return true;
             }
             return false;
      }
 }
