 package com.example.myvirtualcardwallet;
 
 import android.app.Activity;
 import android.content.Intent;
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
 import com.parse.SaveCallback;
 
 public class Edit_card extends Activity {
 	
 	public ParseObject localCard;
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_card);
 		Parse.initialize(this, "HIWiddAFOEElxePoa7qHp72CHvwtNuqOXf1bXkjf", "aJpMrnM1WfUOpLGGSBUSl4pLPh4vVSdQrgL4VBi3"); 
 		ParseAnalytics.trackAppOpened(getIntent());
 		
 		Bundle b=this.getIntent().getExtras();
 		String username=b.getString("Username").toString();
 		Toast.makeText(getApplicationContext(),username, 
 				          Toast.LENGTH_SHORT).show();
 
 
 		
 		final ParseQuery query = new ParseQuery("Card");
 		query.whereEqualTo("Username", username);
 		query.getFirstInBackground(new GetCallback(){
 
 			public void done(ParseObject foundCard, ParseException arg1) {
 				localCard=foundCard;
 			
 				EditText name= (EditText)findViewById(R.id.editcard_name);
 				name.setText(foundCard.getString("Name").toString());
 
 				EditText address= (EditText)findViewById(R.id.editcard_address);
 				address.setText(foundCard.getString("Address").toString());
 
 				EditText company= (EditText)findViewById(R.id.editcard_company);
 				company.setText(foundCard.getString("Company"));
 
 				EditText designation= (EditText)findViewById(R.id.editcard_designation);
 				designation.setText(foundCard.getString("Designation"));
 
 				EditText email= (EditText)findViewById(R.id.editcard_email);
 				email.setText(foundCard.getString("Email"));
 
 				EditText telephone= (EditText)findViewById(R.id.editcard_telephone);
 				telephone.setText(foundCard.getString("Telephone").toString());
 
 			}
 		});
 
 		Button edit_accept= (Button) findViewById(R.id.editcard_save);
 
 		edit_accept.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				final EditText name= (EditText)findViewById(R.id.editcard_name);
 				final EditText address= (EditText)findViewById(R.id.editcard_address);
 				final EditText company= (EditText)findViewById(R.id.editcard_company);
 				final EditText designation= (EditText)findViewById(R.id.editcard_designation);
 				final EditText email= (EditText)findViewById(R.id.editcard_email);
 				final EditText telephone= (EditText)findViewById(R.id.editcard_telephone);
 				
 				final ParseObject updatedCard = localCard; 
 			
 				updatedCard.saveInBackground(new SaveCallback() {
 					public void done(ParseException e) {
 						updatedCard.put("Name",name.getText().toString() );
 						updatedCard.put("Address",address.getText().toString());
 						updatedCard.put("Company", company.getText().toString());
 						updatedCard.put("Designation", designation.getText().toString());
 						updatedCard.put("Email",email.getText().toString() );
 						updatedCard.put("Telephone",telephone.getText().toString() );
 						
 						updatedCard.saveInBackground();
 						
 						Toast.makeText(getApplicationContext(),"Update Successfull",Toast.LENGTH_SHORT).show();
 						
						/*Intent options=new Intent(getApplicationContext(), Options.class);
 						Bundle b = new Bundle();
 						b.putString("Username", localCard.getString("Username").toString());
 						options.putExtras(b);
						startActivity(options);*/
						
						finish();
 					}
 				});
 
 			}
 		});
 
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.edit_card, menu);
 		return true;
 	}
 
 }
