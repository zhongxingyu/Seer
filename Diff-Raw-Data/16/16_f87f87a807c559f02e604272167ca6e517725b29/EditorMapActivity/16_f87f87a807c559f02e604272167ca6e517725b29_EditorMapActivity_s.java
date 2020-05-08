 package fr.umlv.escape.editor;
 
 import fr.umlv.escape.R;
 import fr.umlv.escape.game.Level;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Spinner;
 
 public class EditorMapActivity extends Activity implements OnItemSelectedListener{
 	Spinner spinner_map;
 	String map = "level1";
 	Bitmap background;
     ImageView image_map;
     EditedLevel editedLevel;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.editor_map);
 		
 		spinner_map = (Spinner) findViewById(R.id.wave_list_select);
 		image_map = (ImageView)findViewById(R.id.mapImageEditor);
		editedLevel = new EditedLevel(new Level("empty_level"),BitmapFactory.decodeResource(this.getResources(), R.drawable.level1));
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 		        R.array.maps_array, android.R.layout.simple_spinner_item);
 		
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner_map.setAdapter(adapter);
 		spinner_map.setOnItemSelectedListener(this);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.save, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_save:
 			Intent intent = new Intent(this, EditorLevelActivity.class);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}	
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos,
 			long id) {
 		map = (String) parent.getItemAtPosition(pos);
 		background = BitmapFactory.decodeResource(this.getResources(), getResources().getIdentifier(map, "drawable", "fr.umlv.escape"));
 		EditedLevel.background = background;
 		image_map.setImageBitmap(background);
 		view.invalidate();
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 		
 	}
 }
