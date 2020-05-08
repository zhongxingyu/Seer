 package org.raegdan.bbstalker;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.PopupWindow;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import android.content.Context;
 import android.content.Intent;
 
 public class MainActivity extends Activity implements OnClickListener {
 
     Button btnMAQuery;
     Button btnMAWatchDB;
     Button btnMAWatchCollection;
     Button btnMAHelp;
     EditText etMAQuery;
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         btnMAQuery = (Button) findViewById(R.id.btnMAQuery);
         btnMAQuery.setOnClickListener(this);
         btnMAWatchDB = (Button) findViewById(R.id.btnMAWatchDB);
         btnMAWatchDB.setOnClickListener(this);
         btnMAWatchCollection = (Button) findViewById(R.id.btnMAWatchCollection);
         btnMAWatchCollection.setOnClickListener(this);
         btnMAHelp = (Button) findViewById(R.id.btnMAHelp);
         btnMAHelp.setOnClickListener(this);
         etMAQuery = (EditText) findViewById(R.id.etMAQuery);
         etMAQuery.setOnClickListener(this);
 	}
 	
 	// Queries blind bag DB and shows DB activity
 	protected void QueryDatabase(String query) {
     	Intent intent = new Intent(this, DBListActivity.class);
     	intent.putExtra("query", query);
     	startActivity(intent);
 	}
 	
 	@Override
 	public void onClick(View v) {
 	    switch (v.getId()) {
 	    case R.id.btnMAQuery:
 	    	if (etMAQuery.getText().toString().equalsIgnoreCase(""))
 	    	{
 	    		Toast.makeText(getApplicationContext(), getString(R.string.nothing_to_query), Toast.LENGTH_LONG).show();
 	    	} else {
 	    		QueryDatabase(etMAQuery.getText().toString());
 	    	}
 	    	break;
 	    	
 	    case R.id.btnMAWatchDB:
	    	QueryDatabase("");
 	    	break;
 
 	    case R.id.btnMAWatchCollection:
 	    	QueryDatabase("$");
 	    	break;
 	    	
 	    case R.id.btnMAHelp:
 	    	ShowHelp();
 	    	break;
 	    	
 	    default:
 	    	break;
 	  }
 	}
 	
 	protected void ShowHelp()
 	{
 		PopupWindow pw;
 		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View vPWHelp = inflater.inflate(R.layout.pwhelp, null);
 		RelativeLayout rlPWHelp = (RelativeLayout) vPWHelp.findViewById(R.id.rlPWHelp);
 		
 		pw = new PopupWindow(this);
 		
 		pw.setContentView(rlPWHelp);
 		pw.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
 		pw.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
 		pw.setFocusable(true);
 		pw.showAtLocation(vPWHelp, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
 	}    
 }
