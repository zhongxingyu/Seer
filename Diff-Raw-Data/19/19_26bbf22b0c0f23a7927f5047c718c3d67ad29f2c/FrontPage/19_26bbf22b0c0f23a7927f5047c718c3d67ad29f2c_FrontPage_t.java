 package org.pticlic;
 
 import org.pticlic.games.BaseGame;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class FrontPage extends Activity implements OnClickListener{
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.frontpage);
		
 		// Écoute des clics sur les différents boutons
 		((ImageView)findViewById(R.id.prefs)).setOnClickListener(this);
 		((ImageView)findViewById(R.id.play)).setOnClickListener(this);
 		((ImageView)findViewById(R.id.infoButton)).setOnClickListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
 		// On récupère le nom du joueur des préférences.
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
 		String loginPref = sp.getString("login", "joueur");
 		// On l'ajoute dans le TextView prévu à cet effet
 		((TextView)findViewById(R.id.login)).setText("Login : " + loginPref);
		System.out.println("FrontPage.onStart()");
 	}
 
 	/* (non-Javadoc)
 	 * @see android.view.View.OnClickListener#onClick(android.view.View)
 	 */
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case (R.id.prefs) : startActivity(new Intent(this, Preference.class)); break;
 		case (R.id.play) : startActivity(new Intent(this, BaseGame.class)); break;
 		case (R.id.infoButton) : startActivity(new Intent(this, Information.class)); break;
 		}
 	}
 	
 	@Override
 	public void onBackPressed() {
 		System.exit(0);
 	}
 	
 }
