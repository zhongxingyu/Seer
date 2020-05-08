 package com.hades.grid;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class SelectLevelActivity extends Activity{
 	 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_select_level);
 		
 		Button back = (Button) findViewById(R.id.backButton);
 		back.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				finishActivity();
 			}
 		});
 		
 		final ListView lv = (ListView) findViewById(R.id.levelList);
 		ArrayList<String> levelList = new ArrayList<String>(); 
 		levelList.add("One   (5 X 5)");
 		levelList.add("Two   (6 X 6)");
		levelList.add("Three (7 X 4)");
 		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, 
 				android.R.layout.simple_list_item_1, levelList);
 		lv.setAdapter(arrayAdapter);
 		
 		lv.setClickable(true);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View view, int position,
 					long id) {
 				if (position == 0){
 					Intent i = new Intent(SelectLevelActivity.this, Level1.class);
 					startActivityForResult(i, 0);
 				}
 				else if (position == 1){
 					Intent i = new Intent(SelectLevelActivity.this, Level2.class);
 					startActivityForResult(i,1);
 				}
 				else if (position == 2){
 					Intent i = new Intent(SelectLevelActivity.this, Level3.class);
 					startActivityForResult(i,2);
 				}
				
 			}
 		});
 		
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if (requestCode == 0 && resultCode == RESULT_OK){
 			/* Save level achievement */
 			finishActivity();
 		}
 		else if (requestCode == 1 && resultCode == RESULT_OK){
 			/* Save level achievement */
 			finishActivity();
 
 		}
 		else if (requestCode == 2 && resultCode == RESULT_OK){
 			/* Save level achievement */
 			finishActivity();
 		}
 	}
 	
 	private void finishActivity(){
 		setResult(RESULT_OK);
 		super.finish();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main_menu, menu);
 		return true;
 	}
 
 }
