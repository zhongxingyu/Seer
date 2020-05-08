 package info.micdm.ftr.activities;
 
 import info.micdm.ftr.Forum;
 import info.micdm.ftr.Group;
 import info.micdm.ftr.R;
 import info.micdm.ftr.Theme;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 /**
  * Экран с темами внутри конкретной группы.
  * @author Mic, 2011
  *
  */
 public class GroupActivity extends Activity {
 
 	/**
 	 * Вызывается, когда будет доступен список тем.
 	 */
 	protected void _onThemesAvailable(ArrayList<Theme> themes) {
 		Log.d(toString(), themes.size() + " themes available");
 		ArrayAdapter<Theme> adapter = new ArrayAdapter<Theme>(this, R.layout.list_item, themes);
 		ListView list = (ListView)findViewById(R.id.themes);
 		list.setAdapter(adapter);
 	}
 	
 	/**
 	 * Отображает темы внутри группы.
 	 */
 	protected void _showThemes(Group group) {
 		group.getThemes(new Group.Command() {
 			@Override
 			public void callback(ArrayList<Theme> themes) {
 				_onThemesAvailable(themes);
 			}
 		});
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.group);
		Integer groupId = getIntent().getExtras().getInt("groupId");
 		Group group = Forum.getInstance().getGroup(groupId);
 		_showThemes(group);
 	}
 }
