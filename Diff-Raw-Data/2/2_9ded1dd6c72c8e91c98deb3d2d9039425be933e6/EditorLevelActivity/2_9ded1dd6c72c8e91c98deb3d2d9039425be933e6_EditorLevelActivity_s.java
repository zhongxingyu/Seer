 package fr.umlv.escape.editor;
 
 import fr.umlv.escape.R;
 import fr.umlv.escape.game.Level;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class EditorLevelActivity extends Activity {
 	Level level;
	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.editor_level);
 		this.level = new Level("edited_level");
 
 		final EditText level_name = (EditText) findViewById(R.id.level_name);	
 		final Button button_level_builder = (Button) findViewById(R.id.plus);
 		final ListView lv = (ListView) findViewById(R.id.waves_list_editor);
 		
 		LevelAdapter levelAdapter=new LevelAdapter(getApplicationContext(),level);
 		lv.setAdapter(levelAdapter);
 		
 		button_level_builder.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				
 			}
 		});
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.save, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 //		LayoutInflater li = getLayoutInflater(); 
 //		switch (item.getItemId()) {
 //		case R.id.grid:
 //			fl.addView(li.inflate(R.layout.grid_layout, null));
 //			return true;
 //		default:
 			return super.onOptionsItemSelected(item);
 //		}	
 	}
 }
