 package aweber.phandroid;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class LevelSelectionActivity extends Activity {
 
 	public static final int EXIT_RETURN_CODE = 2000;
 
 	private static final int SUB_ACTIVITY_REQUEST_CODE_L0 = 20;
 
 	private static final int SUB_ACTIVITY_REQUEST_CODE_L1 = 21;
 
 	private static final int SUB_ACTIVITY_REQUEST_CODE_L2 = 22;
 
 	private static final int SUB_ACTIVITY_REQUEST_CODE_L3 = 23;
 
 	private static final int SUB_ACTIVITY_REQUEST_CODE_L4 = 24;
 
 	private List<Map<String, String>> _levelSelectionData;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_level_selection);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		_levelSelectionData = new ArrayList<Map<String, String>>();
 		add("Level 0", "level0_text");
 		add("Level 1", "level1_text");
 		add("Level 2", "level2_text");
 		add("Level 3", "level3_text");
 		add("Level 4", "level4_text");
 
 		final String[] fromMapKey = new String[] { "level", "details" };
 		final int[] toLayoutId = new int[] { R.id.level_selection_row_text, R.id.level_selection_row_text_details };
 
 		ListAdapter listAdapter = new SimpleAdapter(this, _levelSelectionData, R.layout.level_selection_row,
 				fromMapKey, toLayoutId);
 
 		final ListView listview = (ListView) findViewById(R.id.listview_level_selection);
 		listview.setAdapter(listAdapter);
 
 		final Intent level0Intent = new Intent(this, Level0Activity.class);
 		final Intent level1Intent = new Intent(this, Level1Activity.class);
 		final Intent level2Intent = new Intent(this, Level2Activity.class);
 		final Intent level3Intent = new Intent(this, Level3Activity.class);
 		final Intent level4Intent = new Intent(this, Level4Activity.class);
 
 		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
 				final Map<String, String> item = ((Map<String, String>) parent.getItemAtPosition(position));
 				if (item != null && item.keySet().contains("level")) {
 					String level = item.get("level");
 					if ("Level 0".equals(level)) {
 						startActivityForResult(level0Intent, SUB_ACTIVITY_REQUEST_CODE_L0);
 					} else if ("Level 1".equals(level)) {
 						startActivityForResult(level1Intent, SUB_ACTIVITY_REQUEST_CODE_L1);
 					} else if ("Level 2".equals(level)) {
 						startActivityForResult(level2Intent, SUB_ACTIVITY_REQUEST_CODE_L2);
 					} else if ("Level 3".equals(level)) {
 						startActivityForResult(level3Intent, SUB_ACTIVITY_REQUEST_CODE_L3);
 					} else if ("Level 4".equals(level)) {
						startActivityForResult(level4Intent, SUB_ACTIVITY_REQUEST_CODE_L4);
 					}
 				}
 			}
 
 		});
 	}
 
 	private void add(String levelText, String levelDetailsId) {
 		int detailsId = getResources().getIdentifier(levelDetailsId, "string", getPackageName());
 		String detailsText = getResources().getString(detailsId);
 		Map<String, String> entry = new HashMap<String, String>();
 		entry.put("level", levelText);
 		entry.put("details", detailsText);
 		_levelSelectionData.add(entry);
 	}
 
 	/** Called when child activity finishes. */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if ((requestCode == SUB_ACTIVITY_REQUEST_CODE_L0 && resultCode == Level0Activity.EXIT_RETURN_CODE)
 				|| (requestCode == SUB_ACTIVITY_REQUEST_CODE_L1 && resultCode == Level1Activity.EXIT_RETURN_CODE)
 				|| (requestCode == SUB_ACTIVITY_REQUEST_CODE_L2 && resultCode == Level2Activity.EXIT_RETURN_CODE)
 				|| (requestCode == SUB_ACTIVITY_REQUEST_CODE_L3 && resultCode == Level3Activity.EXIT_RETURN_CODE)
 				|| (requestCode == SUB_ACTIVITY_REQUEST_CODE_L4 && resultCode == Level4Activity.EXIT_RETURN_CODE)) {
 			setResult(EXIT_RETURN_CODE, null);
 			finish(); // 'Exit' has been chosen in child activity's option menu -> also close parent(=this) activity
 		}
 	}
 }
