 package com.halcyonwaves.apps.meinemediathek.activities;
 
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuInflater;
 
 import com.halcyonwaves.apps.meinemediathek.R;
 
 public abstract class BaseActivity extends Activity {
 
 	@Override
 	public boolean onCreateOptionsMenu( final Menu menu ) {
 		final MenuInflater usedInflater = this.getMenuInflater();
 		usedInflater.inflate( R.menu.main_menu, menu );
 		return true;
 	}
 
 }
