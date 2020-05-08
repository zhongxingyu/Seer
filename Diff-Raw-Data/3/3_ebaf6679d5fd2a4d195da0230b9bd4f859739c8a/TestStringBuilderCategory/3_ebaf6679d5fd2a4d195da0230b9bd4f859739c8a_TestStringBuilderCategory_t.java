 package uw.changecapstone.tweakthetweet;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class TestStringBuilderCategory extends CustomWindow {
 
 	//private TextView char_count;
 	//private EditText add_details;
 	private ListView category_list;
 	private String tweet, category_tag, custom_category_tag;
 	private TextView char_count;
 	private EditText crnt_tweet;
 	private ImageButton proceed_custom_category_tag;
 	private double lat;
 	private double longitude;
 	public final static String GPSLAT = "uw.changecapstone.tweakthetweet.gpslat";
 	public final static String GPSLONG = "uw.changecapstone.tweakthetweet.gpslong";
 	
 	
 //	private final TextWatcher charCountWatcher = new TextWatcher() {
 //		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 //			char_count.setText(String.valueOf(140 - tweet.length()) + " characters left in tweet");
 //		}
 //
 //		public void onTextChanged(CharSequence s, int start, int before, int count) {
 //			char_count.setText(String.valueOf(140 - tweet.length() - " #loc ".length() - s.length()) + " characters left in tweet");
 //		}
 //
 //		@Override
 //		public void afterTextChanged(Editable arg0) {
 //			// TODO Auto-generated method stub
 //		}
 //
 //	};
 	
 	private final TextWatcher createNewCategoryTag = new TextWatcher() {
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 		}
 	
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 			
 			//Handle tag creation and display in footer box
 			custom_category_tag = s.toString();
 			
 			//Add "#" character to hash tag if the user did not already enter it
 			if(!custom_category_tag.contains("#")){
 				custom_category_tag = "#" + custom_category_tag;
 			}
 			crnt_tweet.setText(tweet + " " + custom_category_tag);
 			
 			//Handle character count display
 			int crntLength = 140 - tweet.length() - custom_category_tag.length();
 			if(crntLength < 0){
 				char_count.setTextColor(Color.RED);
 			}else{
 				char_count.setTextColor(Color.BLACK);
 			}
 			
 			if(crntLength != 1){
 				char_count.setText(String.valueOf(crntLength) + " characters left");
 			}else{
 				char_count.setText(String.valueOf(crntLength) + " character left");
 			}
 		}
 	
 		@Override
 		public void afterTextChanged(Editable arg0) {
 		}
 	
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_test_string_builder_category);
 		this.title.setText("#category");
 		
 		Bundle bundle = getIntent().getExtras();
 		tweet = bundle.getString("tweet");
 		String disaster = bundle.getString("disaster");
 		lat = bundle.getDouble(GPSLAT);
 		longitude = bundle.getDouble(GPSLONG);
 		category_list = (ListView) findViewById(R.id.list);
 		category_list.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View v, int position,
 					long id) {
 				
 				category_tag = (String) adapter.getItemAtPosition(position);
 				nextViewDetails(v);
 			}
 		
 		});
 		
 		
 		//Add footer for entering your own hashtag and displaying tweet
 		View footerView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listview_footer, null, false);
 		category_list.addFooterView(footerView);
 		
 		//Set the enter your own label to match the category page
 		TextView footerLabel = (TextView) findViewById(R.id.enter_your_own_text);
 		footerLabel.setText("or enter your own #category");
 		
 		//Set up category text box
 		EditText categoryTextBox = (EditText) findViewById(R.id.custom_text_box);
 		categoryTextBox.addTextChangedListener(createNewCategoryTag);
 				
 		//Set up char count
 		char_count = (TextView) findViewById(R.id.footer_character_count);
		int initCharCount = 140 - tweet.length();
		char_count.setText(initCharCount + " characters left");
 		
 		//Set up tweet text box
 		crnt_tweet = (EditText) findViewById(R.id.tweet_display);
 		crnt_tweet.setText(tweet);
 		
 		//Set up "next" button for custom hash tag
 		proceed_custom_category_tag = (ImageButton) findViewById(R.id.proceed_with_custom);
 		proceed_custom_category_tag.setOnClickListener(new OnClickListener() {
 
 		    @Override
 		    public void onClick(View v) {
 				if(custom_category_tag != null){
 					category_tag = custom_category_tag;
 					nextViewDetails(v);
 				}else{
 					Toast.makeText(getApplicationContext(), "Please enter a custom category tag", Toast.LENGTH_SHORT).show();
 					
 				}
 		    }
 		});
 		
 		//Create adapter
 		ListAdapter adapter = createAdapter(disaster);
 		category_list.setAdapter(adapter);
 		
 		// Create adapter
 		//category_list.setAdapter(createAdapter(disaster));
 		
 //		char_count = (TextView) findViewById(R.id.char_count);
 //		add_details = (EditText) findViewById(R.id.additional_details);
 //		add_details.addTextChangedListener(charCountWatcher);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.test_string_builder, menu);
 		return true;
 	}
 	
 	
 	protected ListAdapter createAdapter(String disaster)
     {
     	//Test data for initial event list
     	//Read this in from somewhere else
     	String[] testData = new String[] {
     			"#shelter",
     			"#food",
     			"#water",
     			"#help",
     			"#medical",
     	};
  
     	// Create a String array adapter using the testData values
     	ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testData);
  
     	return adapter;
     }
 	
 	
 	public void nextViewDetails(View view){
 //		if (category_tag_spinner.getSelectedItem()==null){
 //		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 //		alertDialog.setTitle("Error");
 //		alertDialog.setMessage("Please select a category");
 //		// -1 = BUTTON_POSITIVE = a positive button?
 //		alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
 //			public void onClick(DialogInterface dialog, int which) {
 //				// here you can add functions
 //			}
 //		});
 //		alertDialog.show();
 //	} else {
 		// In case the user backed, we don't want to accidentally duplicate strings, so we pull from the bundle again
 		Bundle bundle = getIntent().getExtras();
 		tweet = bundle.getString("tweet");
 		tweet += " " + category_tag;
 		//tweet += " " + add_details.getText().toString();
 		Intent i = new Intent(this, TestStringBuilderConfirm.class);
 		i.putExtra("tweet", tweet);
 		i.putExtra("category", category_tag);
 		i.putExtra(GPSLAT, lat);
 		i.putExtra(GPSLONG, longitude);
 		startActivity(i);
 	}
 }
