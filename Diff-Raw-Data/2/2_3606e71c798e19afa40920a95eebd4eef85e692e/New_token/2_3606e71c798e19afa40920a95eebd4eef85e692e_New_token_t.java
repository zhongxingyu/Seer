 package atoken.tworealities.eu;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.view.View;
 import android.view.View.OnClickListener;
 import atoken.tworealities.eu.classes.EventToken;
 import atoken.tworealities.eu.classes.TimeToken;
 import atoken.tworealities.eu.classes.Token;
 import atoken.tworealities.eu.classes.Utils;
 
 public class New_token extends Activity {
 	private Token token;
 	private final int SEED_LENGTH=40;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.new_token);
 
 		//filling spinner with token types
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 				this, R.array.new_token_text_token_types, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		((Spinner) findViewById(R.id.token_type)).setAdapter(adapter);
 
 		//listener for radio buttons
 		findViewById(R.id.event_token).setOnClickListener(this.radio_listener);
 		findViewById(R.id.time_token).setOnClickListener(this.radio_listener);
 
 		//listener for create button
 		findViewById(R.id.button_create).setOnClickListener(this.button_listener);
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) { // Edit mode
 			token = (Token) extras.getSerializable("token");
 			((EditText) findViewById(R.id.token_name)).setText(token.getName());
 			((EditText) findViewById(R.id.token_serial)).setText(token.getSerial());
 			findViewById(R.id.token_type_radiogroup).setVisibility(View.GONE);
 			findViewById(R.id.token_layout_seed).setVisibility(View.GONE);
 			((Button) findViewById(R.id.button_create)).setText(getString(R.string.new_token_button_edit));
 			setTitle(getString(R.string.new_token_edit_title));
 		}
 		
 	}
 
 	private OnClickListener radio_listener = new OnClickListener() {
 
 		public void onClick(View v) {
 			switch(v.getId()){
 			case R.id.event_token:
 				findViewById(R.id.time_token_details).setVisibility(View.GONE);
 				break;
 			case R.id.time_token:
 				findViewById(R.id.time_token_details).setVisibility(View.VISIBLE);
 				break;
 			}
 		}
 
 	};
 
 	private OnClickListener button_listener = new OnClickListener() {
 
 		public void onClick(View v) {
 			String name = ((EditText) findViewById(R.id.token_name)).getText().toString();
 			String serial = ((EditText) findViewById(R.id.token_serial)).getText().toString();
 			String seed = ((EditText) findViewById(R.id.token_seed)).getText().toString();
 			
 			//checks
 			if(name.length()==0){
 				Toast.makeText(v.getContext(),getString(R.string.new_token_toast_empty_name), Toast.LENGTH_SHORT).show();
 				return;
 			}
 				
 
 			if (token==null){//new token
 				//checks
 				if(seed.length()<SEED_LENGTH){
 					Toast.makeText(v.getContext(),getString(R.string.new_token_toast_short_seed), Toast.LENGTH_SHORT).show();
 					return;
 				}
 				if(seed.length()>SEED_LENGTH){
 					Toast.makeText(v.getContext(),getString(R.string.new_token_toast_long_seed), Toast.LENGTH_SHORT).show();
 					return;
 				}
 				if(!Utils.isHex(seed)){
 					Toast.makeText(v.getContext(),getString(R.string.new_token_toast_seed_not_hex), Toast.LENGTH_SHORT).show();
 					return;
 				}
 				
 				if(((RadioButton) findViewById(R.id.event_token)).isChecked()){
 					token = new EventToken(name,serial,seed);
 				}else{
 					token = new TimeToken(name,serial,seed,0);
 				}
 			}else
 			{
 				token.setName(name);
 				token.setSerial(serial);
				//token.setSeed(seed);				
 			}
 			
 
 			
 			/*** Probably an idea for change token types **/
 			/*if(((RadioButton) findViewById(R.id.event_token)).isChecked()){
 				token = new EventToken(name,serial,seed);
 			}else{
 				token = new TimeToken(name,serial,seed,0);
 			}*/
 
 			Intent i = new Intent();
 			i.putExtra("token", token);
 			
 			setResult(RESULT_OK,i);
 			finish();
 		}
 
 	};
 
 }
