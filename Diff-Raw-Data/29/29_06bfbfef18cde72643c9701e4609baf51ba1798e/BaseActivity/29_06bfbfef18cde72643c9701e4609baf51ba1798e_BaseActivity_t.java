 package com.halcyonwaves.apps.meinemediathek.activities;
 
import org.acra.ACRA;

 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuInflater;
import android.view.MenuItem;
 
import com.halcyonwaves.apps.meinemediathek.ApplicationEntryPoint;
 import com.halcyonwaves.apps.meinemediathek.R;
 
 public abstract class BaseActivity extends Activity {
 
 	@Override
 	public boolean onCreateOptionsMenu( final Menu menu ) {
		// inflate the basic menu
 		final MenuInflater usedInflater = this.getMenuInflater();
 		usedInflater.inflate( R.menu.main_menu, menu );

		// if this is a debug build, add an option to send a custom bug report
		if( ApplicationEntryPoint.isApplicationDebuggable( this.getApplicationContext() ) ) {
			final MenuItem bugreportMenu = (MenuItem) menu.findItem( R.id.mnu_send_bugreport );
			if( null != bugreportMenu ) {
				bugreportMenu.setVisible( true );
			}
		}

		// we succeeded
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected( int featureId, MenuItem item ) {
		switch( item.getItemId() ) {
			case R.id.mnu_send_bugreport:
				ACRA.getErrorReporter().handleException( null );
				break;
		}
 		return true;
 	}
 
 }
