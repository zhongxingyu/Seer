 package me.footlights.android;
 
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 
import roboguice.inject.InjectView;

import me.footlights.core.Core;
import me.footlights.core.Preferences;

 
 public class FootlightsActivity extends Activity
 {
 	@InjectView(R.id.buttonA) Button buttonA;
 	@InjectView(R.id.buttonB) Button buttonB;
 	@InjectView(R.id.buttonC) Button buttonC;
 	@InjectView(R.id.buttonD) Button buttonD;
 
 	@InjectView(R.id.eventsList) ListView eventsList;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		initializeClass(PreferenceManager.getDefaultSharedPreferences(this));
 
 		final EventLog log = new EventLog(this);
 		eventsList.setAdapter(log.adapter());
 
 		Button[] buttons = { buttonA, buttonB, buttonC, buttonD };
 		for(final Button button : buttons)
 		{
 			button.setOnClickListener(new View.OnClickListener()
 			{
 				@Override public void onClick(View v)
 				{
 					log.log("Clicked '" + button.getText() + "'");
 				}
 			});
 		}
 
 		log.log("Created Footlights core: " + core + "\n");
 
 		for (Map.Entry<String,?> entry : prefs.getAll().entrySet())
 			log.log(entry.getKey() + ": " + entry.getValue().toString());
 	}
 
 	private static synchronized void initializeClass(SharedPreferences sharedPrefs)
 	{
 		if (classInitialized) return;
 
 		core = new Core();
 		prefs = Preferences.create(PreferenceAdapter.wrap(sharedPrefs));
 
 		classInitialized = true;
 	}
 
 	private static boolean classInitialized;
 	private static Core core;
 	private static Preferences prefs;
 }
