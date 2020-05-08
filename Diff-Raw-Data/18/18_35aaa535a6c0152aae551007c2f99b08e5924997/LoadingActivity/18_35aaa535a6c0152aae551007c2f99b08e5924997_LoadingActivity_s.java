 package il.ac.shenkar.cadan;
 
 import il.ac.shenkar.in.bl.Controller;
 import il.ac.shenkar.in.bl.ControllerCallback;
 
 import com.parse.Parse;
 import com.parse.ParseFacebookUtils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.provider.Settings;
 
 public class LoadingActivity extends Activity
 {
     private AlertDialog alertDialog;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
 	// TODO Auto-generated method stub
 	super.onCreate(savedInstanceState);
 
 	if (!isInternetAvailable())
 	{
 	    // i need to display error massage to the user
 	    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 	    // set title
 	    alertDialogBuilder.setTitle(getString(R.string.no_internrt));
 	    // set dialog message
 	    alertDialogBuilder.setMessage(getString(R.string.no_internet_content)).setCancelable(false).setNegativeButton("Turn On WIFI", new DialogInterface.OnClickListener()
 	    {
 		public void onClick(DialogInterface dialog, int which)
 		{
 		    // this button will navigate you to the WIFI setting menu
 		    // so you could easy turn on the WIFI
 		    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
 		    dialog.cancel();
 		}
 	    }).setPositiveButton("OK", new DialogInterface.OnClickListener()
 	    {
 		public void onClick(DialogInterface dialog, int id)
 		{
 		    // if this button is clicked, close the dilaog
 		    // send data to google analytic
 		    dialog.cancel();
 		    return;
 		}
 	    });
 	    // create alert dialog
 	    alertDialog = alertDialogBuilder.create();
 	    // show it
 	    alertDialog.show();
 	    return; 
 	}
 	Parse.initialize(this, "3kRz2kNhNu5XxVs3mI4o3LfT1ySuQDhKM4I6EblE", "UmGc3flrvIervInFbzoqGxVKapErnd9PKnXy4uMC");
 	ParseFacebookUtils.initialize("635010643194002");
 	this.setContentView(R.layout.loading_page);
 	Controller controller = Controller.getInstance(this);
 	MessageHalper.showProgressDialog("Loading..", this);
 	controller.updateViewModel(new ControllerCallback<Integer>()
 	{
 
 	    @Override
 	    public void done(Integer retObject, Exception e)
 	    {
 		MessageHalper.closeProggresDialog();
 		startActivity(new Intent(LoadingActivity.this, Main.class));
 		finish();
 	    }
 	});
     }
 
     
     public boolean isInternetAvailable()
     {
 	ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
 	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
 	if (activeNetwork == null)
 	    return false;
 	return true;
 
     }
     @Override
     protected void onResume()
     {
         super.onResume();
        alertDialog.cancel();
        this.onCreate(null);
        
     }
 }
