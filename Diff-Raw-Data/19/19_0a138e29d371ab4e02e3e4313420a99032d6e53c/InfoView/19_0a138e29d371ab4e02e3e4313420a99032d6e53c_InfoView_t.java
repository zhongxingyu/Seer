 package gov.cdc;
 
 import gov.cdc.R;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 
 /**
  * A simple activity to display the info.html page.  All this does is set the webView's content to assets/info.html.
  * @author Will
  *
  */
 public class InfoView extends Activity {
 
 	private Activity mActivity;
 	
 	// set the the layout's infoView to webView and set its content to assets/info.html.
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_info_view);
 		mActivity = this;
 		
		// Registering the "finish" action to the "Done" button
				final Button doneButton = (Button)findViewById(R.id.doneButton);
		        doneButton.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View view) {
		            	finish();
		            }
		        });
		
 		String version = "Version " + getVersionNumber();
 		TextView tv = (TextView)findViewById(R.id.infoVersionNum);
         tv.setText(version);
 		
         Button eulaButton = (Button) findViewById(R.id.eulaButton);
         eulaButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View view) {
         		new Eula(mActivity).show(true);
         	}
         });
 	}
 	
 	/**
 	 * Retrieve and return the version name of the app
 	 * @return
 	 */
 	private String getVersionNumber() {
 		PackageInfo pi = null;
         try {
              pi = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
         } catch (PackageManager.NameNotFoundException e) {
             e.printStackTrace();
         }
         return pi.versionName;
     }
 }
