 package org.failedprojects.anjaroot;
 
 import org.failedprojects.anjaroot.library.AnJaRoot;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 
 public class MainActivity extends FragmentActivity {
 	@Override
 	protected void onCreate(Bundle arg0) {
 		super.onCreate(arg0);
 
 		setContentView(R.layout.activity_main);
 
 		Fragment fragment;
 		boolean installed = AnJaRoot.isAccessPossible();
 		if (installed) {
			fragment = PackagesFragment.newInstance();
 		} else {
			fragment = InstallFragment.newInstance();
 		}
 
 		getSupportFragmentManager().beginTransaction()
				.replace(R.id.main_activity_layout, fragment).commit();
 	}
 }
