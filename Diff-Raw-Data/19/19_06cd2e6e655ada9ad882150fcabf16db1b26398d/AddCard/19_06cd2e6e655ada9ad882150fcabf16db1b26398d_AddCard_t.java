 package com.example.myvirtualcardwallet;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.parse.GetCallback;
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 public class AddCard extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_card);
 		Parse.initialize(this, "HIWiddAFOEElxePoa7qHp72CHvwtNuqOXf1bXkjf",
 				"aJpMrnM1WfUOpLGGSBUSl4pLPh4vVSdQrgL4VBi3");
 		ParseAnalytics.trackAppOpened(getIntent());
 
 		final EditText toAddusername = (EditText) findViewById(R.id.user_to_add);
 		final Bundle b=this.getIntent().getExtras();
 		final String username=b.getString("Username");
 		Toast.makeText(getApplicationContext(),username,
 				Toast.LENGTH_SHORT).show();
 		
 		Button add = (Button) findViewById(R.id.add_button);
 
 		add.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				final String usertoadd = toAddusername.getText().toString();
 				ParseQuery query = new ParseQuery("Card");
 				query.whereEqualTo("Username", usertoadd);
 				
 				query.getFirstInBackground(new GetCallback() {
 
 					public void done(ParseObject foundCard, ParseException arg1) {
						if(foundCard!=null){
 						ParseObject membership=new ParseObject("Memberships");
 						membership.put("Owner", b.getString("Username"));
 						membership.put("Contact", usertoadd);
 						membership.saveInBackground();
 						Toast.makeText(getApplicationContext(), "Card Added",
 								Toast.LENGTH_SHORT).show();
						finish();
 						}
 						else{
 							Toast.makeText(getApplicationContext(),"Username enterned does not exist.",
									Toast.LENGTH_LONG).show();
 						}
 					}
 				});
 
 			}
 		});
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.add_card, menu);
 		return true;
 	}
 
 }
