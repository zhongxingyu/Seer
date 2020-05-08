 package net.rdrei.android.wakimail.ui;
 
 import net.rdrei.android.wakimail.R;
 import roboguice.activity.RoboFragmentActivity;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 
 public class MailDetailActivity extends RoboFragmentActivity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (savedInstanceState == null) {
			setContentView(R.layout.activity_singlepane_empty);
			
			Uri uri = this.getIntent().getData();
 			// First-time init; create fragment to embed in activity.
 			FragmentTransaction ft = getSupportFragmentManager()
 					.beginTransaction();
 			MailDetailFragment newFragment = MailDetailFragment
 					.newInstance(uri);
 			ft.replace(R.id.root_container, newFragment);
 			ft.commit();
 		}
 	}
 }
