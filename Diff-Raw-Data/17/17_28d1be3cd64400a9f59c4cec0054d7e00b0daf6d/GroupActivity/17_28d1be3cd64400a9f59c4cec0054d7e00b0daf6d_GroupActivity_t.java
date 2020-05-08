 package info.micdm.ftr.activities;
 
 import info.micdm.ftr.Forum;
 import info.micdm.ftr.Group;
 import info.micdm.ftr.R;
 import info.micdm.ftr.Theme;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * Экран с темами внутри конкретной группы.
  * @author Mic, 2011
  *
  */
public class GroupActivity extends ListActivity {
 
 	/**
 	 * Диалог про загрузку списка тем.
 	 */
 	protected ProgressDialog _loadingThemesDialog = null;
 	
 	/**
 	 * Вызывается, когда будет доступен список тем.
 	 */
 	protected void _onThemesAvailable(ArrayList<Theme> themes) {
 		Log.d(toString(), themes.size() + " themes available");
 		ArrayAdapter<Theme> adapter = new ArrayAdapter<Theme>(this, R.layout.list_item, themes);
		getListView().setAdapter(adapter);
 	}
 	
 	/**
 	 * Отображает темы внутри группы.
 	 */
 	protected void _showThemes(Group group) {
 		_loadingThemesDialog = ProgressDialog.show(this, "", "Загружается список тем");
 		group.getThemes(new Group.Command() {
 			@Override
 			public void callback(ArrayList<Theme> themes) {
 				_onThemesAvailable(themes);
 				if (_loadingThemesDialog != null) {
 					_loadingThemesDialog.dismiss();
 				}
 			}
 		});
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.group);
 		// Узнаем, что за группу надо показать:
 		Integer groupId = getIntent().getExtras().getInt("groupId");
 		final Group group = Forum.getInstance().getGroup(groupId);
 		// Заполняем заголовок:
 		TextView title = (TextView)findViewById(R.id.groupTitle);
 		title.setText(group.getTitle());
 		// При клике по "Обновить" перезагружаем список:
 		ImageButton reload = (ImageButton)findViewById(R.id.reloadGroup);
 		reload.setOnClickListener(new ImageButton.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				_showThemes(group);
 			}
 		});
 		_showThemes(group);
 	}
 	
 	@Override
 	public void onDestroy() {
 		if (_loadingThemesDialog.isShowing()) {
 			_loadingThemesDialog.dismiss();
 			_loadingThemesDialog = null;
 		}
 		super.onDestroy();
 	}
 }
