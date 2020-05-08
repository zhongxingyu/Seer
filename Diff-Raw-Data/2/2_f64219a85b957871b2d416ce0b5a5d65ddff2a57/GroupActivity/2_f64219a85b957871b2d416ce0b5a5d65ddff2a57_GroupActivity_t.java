 package info.micdm.ftr.activities;
 
 import info.micdm.ftr.Forum;
 import info.micdm.ftr.Group;
 import info.micdm.ftr.R;
 import info.micdm.ftr.Theme;
 import info.micdm.ftr.utils.Log;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * Адаптер для списка тем.
  * @author Mic, 2011
  *
  */
 class ThemeListAdapter extends BaseAdapter {
 	
 	/**
 	 * Класс для кэширования, как предлагают в официальных примерах.
 	 * @author Mic, 2011
 	 *
 	 */
 	static class ViewHolder {
 		TextView author;
 		TextView updated;
 		TextView title;
 	}
 	
 	/**
 	 * Этот товарищ будет создавать новый элемент из XML.
 	 */
 	protected LayoutInflater _inflater;
 	
 	/**
 	 * Список тем.
 	 */
 	protected ArrayList<Theme> _themes;
 	
 	public ThemeListAdapter(Context context, ArrayList<Theme> themes) {
 		_inflater = LayoutInflater.from(context);
 		_themes = themes;
 	}
 	
 	/**
 	 * Возвращает количество элементов.
 	 */
 	public int getCount() {
 		return _themes.size();
 	}
 	
 	/**
 	 * Возвращает элемент в указанной позиции.
 	 */
 	public Object getItem(int position) {
 		return _themes.get(position);
 	}
 
 	/**
 	 * Возвращает идентификатор элемента в указанной позиции.
 	 */
 	public long getItemId(int position) {
 		return position;
 	}
 
 	/**
 	 * Возвращает заполненный элемент списка.
 	 */
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder;
 		if (convertView == null) {
 			convertView = _inflater.inflate(R.layout.theme_list_item, null);
 			holder = new ViewHolder();
 			holder.author = (TextView)convertView.findViewById(R.id.themeAuthor);
 			holder.updated = (TextView)convertView.findViewById(R.id.themeUpdated);
 			holder.title = (TextView)convertView.findViewById(R.id.themeTitle);
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder)convertView.getTag();
 		}
 		Theme theme = (Theme)getItem(position);
 		holder.author.setText(theme.getAuthor());
		holder.updated.setText(", " + theme.getUpdatedAsString());
 		holder.title.setText(theme.getTitle());
 		return convertView;
 	}
 }
 
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
 		// TODO: не пересоздавать адаптер
 		Log.debug(themes.size() + " themes available");
 		ThemeListAdapter adapter = new ThemeListAdapter(this, themes);
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
 	
 	/**
 	 * Слушает клик по кнопке "Обновить".
 	 */
 	protected void _listenForReload(final Group group) {
 		ImageButton reload = (ImageButton)findViewById(R.id.reloadGroup);
 		reload.setOnClickListener(new ImageButton.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				_showThemes(group);
 			}
 		});
 	}
 	
 	/**
 	 * Вызывается при выборе темы.
 	 */
 	protected void _onThemeSelected(Theme theme) {
 		Log.debug("theme \"" + theme.getTitle() + "\" (" + theme.getId() + ") selected");
 		Intent intent = new Intent(this, ThemeActivity.class);
 		intent.putExtra("groupId", theme.getGroupId());
 		intent.putExtra("themeId", theme.getId());
 		startActivity(intent);
 	}
 	
 	/**
 	 * Слушает клик по элементу списка, чтобы перейти в соответствующую тему.
 	 */
 	protected void _listenForThemeSelected() {
 		getListView().setOnItemClickListener(new ListView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
 				Theme theme = (Theme)adapter.getItemAtPosition(position);
 				_onThemeSelected(theme);
 			}
 		});
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.group);
 
 		Integer groupId = getIntent().getExtras().getInt("groupId");
 		Group group = Forum.INSTANCE.getGroup(groupId);
 
 		TextView title = (TextView)findViewById(R.id.groupTitle);
 		title.setText(group.getTitle());
 
 		_listenForReload(group);
 		_listenForThemeSelected();
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
