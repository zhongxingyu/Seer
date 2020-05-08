 package il.ac.tau.team3.uiutils; 
 
 import android.accounts.Account;
 import android.app.Dialog;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import il.ac.tau.team3.common.GeneralUser;
 import il.ac.tau.team3.shareaprayer.FacebookConnector;
 import il.ac.tau.team3.shareaprayer.FindPrayer;
 import il.ac.tau.team3.shareaprayer.IStatusWriter;
 import il.ac.tau.team3.shareaprayer.R;
 import il.ac.tau.team3.shareaprayer.StatusBarOverlay;
 
 public class MenuStatusUtils {
 	
 	public static void createEditStatusDialog(final GeneralUser user, final FindPrayer activity){
 			
 		 final IStatusWriter statusBar = activity.getStatusBar();
 		 final NoTitleDialog dialog = new NoTitleDialog(activity);
 		 dialog.setContentView(R.layout.dialog_set_status);
 		 dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
 		 final EditText status = (EditText)dialog.findViewById(R.id.dss_status);
 		 final String oldStatus = user.getStatus();
          status.setText(oldStatus);
          Button okButton = (Button) dialog.findViewById(R.id.dss_button_ok);
          
          
          
          okButton.setOnClickListener(new OnClickListener()
          {                
              public void onClick(View v)
              {
             	
             	if(!(oldStatus.equals(status.getText().toString()))){
             		try{
             			if(!(status.getText().toString().equals(""))){
             			FacebookConnector fc = activity.getFacebookConnector();
             			fc.publishOnFacebook(formatFacebookHeader_Status(status.getText().toString()) , formatFacebookDesc_Status(user));
             			}
             			statusBar.write("Status set.", R.drawable.status_bar_accept_icon, 2000);
             		}catch(Exception e){
             			
             			if(statusBar != null){
             				statusBar.write("Status failed to be published on facebook.", R.drawable.status_bar_accept_icon, 2000);
             			}
             			
             		}
             	}
             	activity.setStatus(status.getText().toString());
             	
             	dialog.dismiss();
              }
          });
       
      
          dialog.show();
 
      return;
 		
 	}
 	
 	
 	public static String formatFacebookHeader_Status(String status){
 		return status;
 	}
 	
 	static String formatFacebookDesc_Status(GeneralUser user){
 		return user.getFullName() + " just wrote a new status on Share-A-Prayer!";
 	}
 }
