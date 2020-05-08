 package zipcodeman.glookup.authentication;
 
 import zipcodeman.glookup.GlookupFrontendActivity;
 import zipcodeman.glookup.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 
 public class AddUserActivity extends Activity {
     private int uid;
    private String oldUname = "";
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.add_user);
         
         Intent incoming = this.getIntent();
         uid = incoming.getIntExtra(GlookupFrontendActivity.ADD_USER_ID, -1);
         if(uid != -1){
         	TextView uname = (TextView) findViewById(R.id.addUserUnameText);  
         	// TextView pass = (TextView) findViewById(R.id.addUserPasswordText);
         	Spinner server = (Spinner) findViewById(R.id.addUserServerSelect);
 
         	oldUname = incoming.getStringExtra(GlookupFrontendActivity.ADD_USER_USERNAME);
         	uname.setText(oldUname);
         	// pass.setText(incoming.getStringExtra(GlookupFrontendActivity.ADD_USER_PASSWORD));
         	
         	String[] servers = this.getResources().getStringArray(R.array.server_list);
         	String serv = incoming.getStringExtra(GlookupFrontendActivity.ADD_USER_SERVER);
         	int pos = 0;
         	for(int i = 0; i < servers.length; i++){
         		if(servers[i] == serv){
         			pos = i;
         			break;
         		}
         	}
         	server.setSelection(pos);
         }
     }
     
     public void submit(View button){
     	Intent res = new Intent(this, GlookupFrontendActivity.class);
     	TextView uname = (TextView) findViewById(R.id.addUserUnameText);  
     	TextView pass = (TextView) findViewById(R.id.addUserPasswordText);
     	if (pass.getText().length() == 0) {
     		AlertDialog ad = new AlertDialog.Builder(this) 
     							.setIcon(R.drawable.icon)
     							.setMessage("You must enter a password")
     							.setPositiveButton("OK", new DialogInterface.OnClickListener() {
     								public void onClick(DialogInterface dialog, int id) {
     									dialog.cancel();
     								}
     							})
     							.create();
     		ad.show();
     		return;
     	}
     	if (oldUname.equals(uname.getText().toString())) {
    		DSAKeys.removeKeys(this, oldUname);
     	}
     	Spinner server = (Spinner) findViewById(R.id.addUserServerSelect);
     	TextView server_selected = (TextView) server.getSelectedView().findViewById(android.R.id.text1);
     	res.putExtra(GlookupFrontendActivity.ADD_USER_USERNAME, uname.getText().toString());
     	res.putExtra(GlookupFrontendActivity.ADD_USER_PASSWORD, pass.getText().toString());
     	res.putExtra(GlookupFrontendActivity.ADD_USER_SERVER, server_selected.getText().toString());
     	if(uid >= 0){
     		res.putExtra(GlookupFrontendActivity.ADD_USER_ID, uid);
     	}
     	setResult(Activity.RESULT_OK, res);
     	finish();
     }
 }
