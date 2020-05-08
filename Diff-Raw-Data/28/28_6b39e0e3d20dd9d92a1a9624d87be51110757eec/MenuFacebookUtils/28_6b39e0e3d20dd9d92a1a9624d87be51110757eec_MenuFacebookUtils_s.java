 package il.ac.tau.team3.uiutils;
 
 import java.util.logging.Logger;
 
 import il.ac.tau.team3.shareaprayer.FindPrayer;
 import il.ac.tau.team3.shareaprayer.IStatusWriter;
 import il.ac.tau.team3.shareaprayer.R;
 import android.app.Activity;
 import android.app.Dialog;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Toast;
 
 public class MenuFacebookUtils {
 	public MenuFacebookUtils(final FindPrayer context)	{
 		try {
 			final NoTitleDialog dialog = new NoTitleDialog(context);
 			final IStatusWriter statusBar = context.getStatusBar();
 			dialog.setContentView(R.layout.dialog_facebook_settings);
 			dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 
			Button setup = (Button)dialog.findViewById(R.id.DFS_setup_button);
 			Button close = (Button)dialog.findViewById(R.id.DFS_Close);
 			CheckBox cb = (CheckBox)dialog.findViewById(R.id.DFS_share);
 			
 			if (!context.getFacebookConnector().isFacebook_configured())	{
 				cb.setEnabled(true);
 			}
 			
 			if (context.getFacebookConnector().isFacebook_configured())	{
 				setup.setEnabled(false);
 			}
 			
 			cb.setChecked(context.getFacebookConnector().isFacebook_share());
 			
 			cb.setOnCheckedChangeListener(new OnCheckedChangeListener()	{
 
 				public void onCheckedChanged(CompoundButton buttonView,
 						boolean isChecked) {
 					try{
 					context.getFacebookConnector().setFacebook_share(isChecked);
 					statusBar.write("facebook settings updated", R.drawable.status_bar_accept_icon, 2000);
 					}catch(Exception e){
 						e.printStackTrace();
 						if(statusBar != null){
 							statusBar.write("An error accoured. Settings wasn't updated", R.drawable.status_bar_error_icon, 2000);
 						}
 					}
 					
 				}
 				
 			});
 
 			setup.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					context.getFacebookConnector().connect();				
 					dialog.dismiss();
 				}
 			});
 			
 			close.setOnClickListener(new OnClickListener() {
 
 				public void onClick(View v) {
 					dialog.dismiss();
 				}
 			});
 			
 			dialog.show();
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 			Toast toast = new Toast(context);
 			toast.setDuration(3000);
 			toast.setText("Unable to show sub-menu.");
 		}
 
 	}
 
 }
