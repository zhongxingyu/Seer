 package gary.cregan.urbanquiz.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import gary.cregan.urbanquiz.R;
 
 public class MainMenuActivity extends Activity 
 {
 	private Button storyButton;
 	private Button creditsButton;
 	public AlertDialog alert;
 	public Toast toast;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) 
     {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.mainmenu);
         
         AlertDialog.Builder builder;
     	
     	Context mContext = this;
     	LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
     	View layout = inflater.inflate(R.layout.about_dialog,
     	                               (ViewGroup) findViewById(R.id.aboutlayout));
     	
     	TextView textView = (TextView) layout.findViewById(R.id.about);
     	textView.setText("This is a work in progress app. Since I had some minor functionality working, I said 'Sure why not let people try it!'. You can email me at support@blueaura.ie with any feedback! Thanks.'");
     	
     	builder = new AlertDialog.Builder(mContext);
     	builder.setView(layout);
     	builder.setCancelable(false)
 	           .setPositiveButton("Close", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	   dialog.cancel();
 		           }
 	           });
     	this.alert = builder.create();
         
     	this.toast = Toast.makeText(this, "You need internet access to play.", 2000);
     	
         this.storyButton = (Button)this.findViewById(R.id.storyModeButton);
         this.storyButton.setOnClickListener(new OnClickListener() {
           public void onClick(View v) 
           {
        	  if(Settings.Secure.getInt(getContentResolver(), "mobile_data", 1) == 1)
         	  {
         		  Intent myIntent = new Intent(v.getContext(), UrbanQuizActivity.class);
         		  startActivityForResult(myIntent, 0);        		  
         	  }
         	  else
         	  {
         		  toast.setGravity(Gravity.TOP, -30, 50);
         		  toast.show();
         	  }
           }
         });
         this.creditsButton = (Button)this.findViewById(R.id.creditsButton);
         this.creditsButton.setOnClickListener(new OnClickListener() {
             public void onClick(View v) 
             {
             	alert.show();
             }
          });
     }
 }
