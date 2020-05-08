 package k.android.hellolocalization;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Button b;
         b = (Button)findViewById(R.id.button_id);
        b.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.usflag));
         
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.dialog1_text).
         	setCancelable(false).setTitle(R.string.dialog1_title).
         	setPositiveButton("Done", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.dismiss();
 				}
 			});
         final AlertDialog alert = builder.create();
         
         b.setOnClickListener(new View.OnClickListener(){
         	public void onClick(View v){
         		alert.show();
         	}
         });
     }
 }
 
 
 
 
 
 
 
 
 
 
 
 
