 package com.andreaszeiser.jalousiesamples;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class ExampleActivity extends SherlockFragmentActivity {
 
 	public static final String EXTRA_CLASS_NAME = "extraClassName";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		final String extraClassName = getIntent().getStringExtra(
 				EXTRA_CLASS_NAME);
 		if (extraClassName == null || extraClassName.equals("")) {
 			exit();
 		}
 
 		Class<? extends SherlockFragment> className;
 		try {
 			className = (Class<? extends SherlockFragment>) Class
 					.forName(extraClassName);
 
 			setContentView(R.layout.act_example);
 
 			try {
 				final FragmentTransaction fragmentTransaction = getSupportFragmentManager()
 						.beginTransaction();
 
 				fragmentTransaction.replace(R.id.fragment_container,
 						className.newInstance(), className.toString());
 
 				fragmentTransaction.commit();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 				exit();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 				exit();
 			}
 		} catch (ClassNotFoundException e) {
 			exit();
 		}
 
 		// actionbar
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	private void exit() {
 		Toast.makeText(this, "valid value for class name is required",
 				Toast.LENGTH_SHORT).show();
 
 		finish();
 	}
 
	//	@Override
	//	public boolean onOptionsItemSelected(MenuItem item) {
	//		if (item.getItemId() == android.R.id.home) {
	//			finish();
	//			return true;
	//		}
	//		return super.onOptionsItemSelected(item);
	//	}
 
 }
