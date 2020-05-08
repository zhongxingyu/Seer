 package info.micdm.ftr;
 
 import info.micdm.ftr.async.DownloadGroupPageTask;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 /**
  * Одна рубрика со списком тем.
  * 
  * @author Mic, 2011
  * 
  */
 public class Group implements Comparable<Group> {
 
 	/**
 	 * Коллбэк после загрузки списка тем.
 	 * 
 	 * @author Mic, 2011
 	 * 
 	 */
 	public interface Command {
 		public void callback(ArrayList<Theme> themes);
 	}
 
 	/**
 	 * Идентификатор группы.
 	 */
 	protected Integer _id;
 
 	/**
 	 * Заголовок группы.
 	 */
 	protected String _title;
 
 	/**
 	 * Список тем в группе.
 	 */
 	protected ArrayList<Theme> _themes;
 
 	public Group(Integer id, String title) {
 		_id = id;
 		_title = title;
 	}
 
 	@Override
 	public String toString() {
 		return _title;
 	}
 
 	@Override
 	public int compareTo(Group another) {
 		if (_id.equals(0)) {
 			return -1;
 		}
 		if (another.getId().equals(0)) {
 			return 1;
 		}
 		return _title.compareTo(another.getTitle());
 	}
 
 	/**
 	 * Возвращает идентификатор группы.
 	 */
 	public Integer getId() {
 		return _id;
 	}
 
 	/**
 	 * Возвращает заголовок группы.
 	 */
 	public String getTitle() {
 		return _title;
 	}
 
 	/**
 	 * Возвращает темы внутри группы.
 	 */
 	public void getThemes(final Command command) {
 		if (_themes == null) {
			DownloadGroupPageTask task = new DownloadGroupPageTask(new DownloadGroupPageTask.OnLoadCommand() {
 				@Override
 				public void callback(ArrayList<Theme> themes) {
 					Log.d(toString(), themes.size() + " themes loaded");
 					_themes = themes;
 					command.callback(themes);
 				}
 			});
 			task.execute();
 		} else {
 			command.callback(_themes);
 		}
 	}
 }
