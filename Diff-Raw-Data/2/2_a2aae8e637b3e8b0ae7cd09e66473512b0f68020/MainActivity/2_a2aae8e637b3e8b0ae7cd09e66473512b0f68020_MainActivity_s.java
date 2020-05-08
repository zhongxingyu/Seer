 package com.bluebananagames.gametemplate;
 
 import com.badlogic.gdx.backends.android.AndroidApplication;
 import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
 
 public class MainActivity extends AndroidApplication {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
 		cfg.useGL20 = true;
 
 		initialize(new TemplateGame(), cfg);
 	}
 }
