 package fr.umlv.escape;
 
import java.util.ArrayList;

 import fr.umlv.escape.game.Level;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 
 public class EditorActivity extends Activity {
 	private int state = 0;
 	private String levelName;
 	private Level level;
	ArrayAdapter adapter;
	ArrayList<Button> listWaves=new ArrayList<Button>();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.editor_map);
 	}
 
 	public void onPushButton(View v) {
 		switch(state){
 		case 0 : performState0(v); break;
 		case 1 : performState1(v); break;
 		case 2 : performState2(v); break;
 		case 3 : performState3(v); break;
 		}
 	}
 	
 	public void performState0(View v) {
 		ImageView map = (ImageView)findViewById(R.id.mapImageEditor);
 		switch(v.getId())
 		{
 			case R.id.map1Button : {
 			map.setImageResource(R.drawable.level1);
 			this.levelName = "level1";
 			break;
 			}
 			case R.id.map2Button : {
 			map.setImageResource(R.drawable.level2);
 			this.levelName = "level2";
 			break;
 			}
 			case R.id.map3Button : {
 			map.setImageResource(R.drawable.level3);
 			this.levelName = "level3";
 			break;
 			}
 			case R.id.nextButton : {
 			this.level = new Level(this.levelName);
 			state= 1;
			adapter=new ArrayAdapter<Button>(this,android.R.layout.simple_list_item_1,listWaves);
 			setContentView(R.layout.editor_waves);
 			break;
 			}
 		}
 		v.invalidate();
 	}
 	
 	public void performState1(View v) {
	
	    switch(v.getId())
 		{
 			case R.id.backButton : {
 				state = 0;
 				setContentView(R.layout.editor_map);
 				break;
 			}
 			case R.id.plus : {
				listWaves.add(new Button(this));
				adapter.notifyDataSetChanged();
 			}
 			case R.id.moins : {
				if(listWaves.size()>0){					
					listWaves.remove(listWaves.size()-1);
				}
				adapter.notifyDataSetChanged();
 			}
 		}
 		v.invalidate();
 	}
 
 	public void performState2(View v) {
 	
 	}
 
 	public void performState3(View v) {
 	
 	}
 }
