 package com.example.smartnote;
 
 import android.app.Activity;
 
 
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.example.smartnote.R;
 
 public class CardCreator extends Activity {
 	
 	private SmartDBAdapter db;
 		
 	static final String TITLE = "mytitles";
 	static final String DEFINITION = "mydefinitions";
 	String definition = " ", title = " ", stack = " ";
 	private EditText sInput, tInput, defInput;
 		
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.creator);
 		
 		db = new SmartDBAdapter(this);
 		db.open();
 		
 		Typeface chinacat = Typeface.createFromAsset(getAssets(), "fonts/DroidSans-Bold.ttf");
 		
 		TextView titleTxt = (TextView)findViewById(R.id.title_header);
 		titleTxt.setTypeface(chinacat);
 		TextView defTxt = (TextView)findViewById(R.id.definition_header);
 		defTxt.setTypeface(chinacat);
 		TextView stackTxt = (TextView)findViewById(R.id.stack_header);
 		stackTxt.setTypeface(chinacat);
 		
 		Button cardCreator = (Button)findViewById(R.id.createCard);
 		cardCreator.setTypeface(chinacat);
 		Button stackChooser = (Button)findViewById(R.id.stackButton);
 		stackChooser.setTypeface(chinacat);
 		
 		
 		sInput = (EditText)findViewById(R.id.newStack);
 		sInput.setTypeface(chinacat);
 		tInput = (EditText)findViewById(R.id.title);
 		tInput.setTypeface(chinacat);
 		defInput = (EditText)findViewById(R.id.definition);
 		defInput.setTypeface(chinacat);
 		
 		Bundle extras = getIntent().getExtras();
 				
 		if (extras != null) {
 				sInput.setText(extras.getString("stack"));
 				tInput.setText(extras.getString("title"));
 				defInput.setText(extras.getString("definition"));
 		}
 	}
 	
 	public void onDestroy() {
 		super.onDestroy();
 		db.close();
 	}
 	
 	public void createCard(View view) {
 				
 		getText();
 						
 		if (title.isEmpty() || definition.isEmpty())
 			Toast.makeText(getApplicationContext(), "Your card contains unwritten side(s)", Toast.LENGTH_SHORT).show();
 		else {
 			String[] stacks = splitStacks(stack);
 			insertStacks(stacks);
 			
 			long insTester[] = new long[stacks.length];
 			for(int i=0; i < stacks.length; i++) {
				if (db.matchCard(title, definition, stacks[i])) {
 					insTester[i] = -1;
 				} else 
 					insTester[i] = db.insertCard(title, definition, stacks[i]);
 			}
 			
 			for (int i = 0; i < insTester.length; i++) {
 				if (insTester[i] == -1 && !stacks[i].equals("")) 
 					Toast.makeText(getApplicationContext(), "Not inserted in " + stacks[i],
 							500).show();
 			}
 			
 			Intent intent = new Intent(this, SmartNoteActivity.class);
 			startActivity(intent);
 		} 
 	}
 		
 	public void insertStacks(String[] stacks) {
 		for (String sName:stacks) {
 			if (!sName.equals("") && !db.matchStack(sName)) {
 				db.insertStack(sName);
 			}
 		}
 	}
 	public String[] splitStacks(String s) {
 		String[] stacks = s.split(";");
 		for (String string:stacks) {
 			string = string.trim();
 		}
 		return stacks;
 	}
 	
 	public void toStacks(View view) {
 		
 		getText();
 		
 		Intent intent = new Intent(CardCreator.this, StackMenu.class);
 		intent.putExtra("title", title);
 		intent.putExtra("definition", definition);
 		intent.putExtra("stack", stack);
 		
 		startActivity(intent);
 	}
 
 	protected void onStop() {
 		super.onStop();
 	}
 	
 	private void getText() {
 		EditText editText = (EditText)findViewById(R.id.title);
 		title = editText.getText().toString();
 		editText = (EditText)findViewById(R.id.definition);
 		definition = editText.getText().toString();
 		editText = (EditText)findViewById(R.id.newStack);
 		stack = editText.getText().toString();
 	}
 	
 }
