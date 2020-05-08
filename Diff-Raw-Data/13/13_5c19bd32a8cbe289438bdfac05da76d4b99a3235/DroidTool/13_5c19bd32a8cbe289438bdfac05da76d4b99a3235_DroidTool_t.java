 package be.infogroep.droidtool;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.widget.Button;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class DroidTool extends Activity implements OnClickListener {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Button b = (Button)this.findViewById(R.id.btn_confirm);
         b.setOnClickListener(this);
     }
     @Override
     public void onClick(View v) {
     	TextView tv = (TextView)this.findViewById(R.id.tv_welcome);
     	EditText et_uname = (EditText)this.findViewById(R.id.txt_name);
     	EditText et_pw = (EditText)this.findViewById(R.id.txt_pw);
     	
     	String text = "Hello, " + et_uname.getText().toString() + ". \n\n";
     	String token;
     	String token2;
     	//String test ="Hello there \n\n ";
     	try {
     		String name = et_uname.getText().toString();
     		String pw = et_pw.getText().toString();
     		//test += URLReader.getToken("ig", "krnlP4N!C");
     		token = URLReader.getToken(name, pw);
     		StorageInterface.Save(token, getApplicationContext());
     		token2 = StorageInterface.Get(getApplicationContext());
     		text+= URLReader.checkToken(name, token)+"\n";
     		//text+= URLReader.getInterne(name, token)+"\n";
     		text += token +"\n" + token2;
 		} 
     	catch (TokenException e) {
 			//Popup: wrong Token; reprompt for username/password
     		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
    		dialogBuilder.setMessage(e.getError());
     		dialogBuilder.create().show();
     		
 		}
 		catch (Exception e) {
 			e.getMessage();
 		}
     		
     	tv.setText(text);	
     }
 }
 
