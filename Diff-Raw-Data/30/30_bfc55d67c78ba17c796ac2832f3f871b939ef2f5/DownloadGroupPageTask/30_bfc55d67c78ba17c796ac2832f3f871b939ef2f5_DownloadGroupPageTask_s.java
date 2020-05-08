 package info.micdm.ftr.async;
 
 import info.micdm.ftr.Theme;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.util.Log;
 
 /**
  * Парсер страницы со списком тем.
  * @author Mic, 2011
  *
  */
 class GroupParser {
 	
 	/**
 	 * Возвращает регулярное выражение для разбора.
 	 */
 	protected String _getPattern() {
 		// Дата последнего сообщения в теме:
 		String result = "<td class=\"tdw1\">([^<]+)</td>\\s+";
 		// Идентификатор группы, идентификатор темы, название темы:
		result += "<td class=\"tdw3\"><a href=\"/forum/(\\d+)/(\\d+)/\"[^>]+>([^<]+).+?";
 		// Автор темы:
 		return result + "<td class=\"tdw2\">(?:\\s+<a[^>]+>)?([^<]+)<";
 	}
 	
 	/**
 	 * Парсит текст в список тем.
 	 */
 	public ArrayList<Theme> parse(String text) {
 		Log.d(toString(), "parsing group page");
 		ArrayList<Theme> themes = new ArrayList<Theme>();
 		Pattern pattern = Pattern.compile(_getPattern(), Pattern.DOTALL);
 		Matcher matcher = pattern.matcher(text);
 		while (matcher.find()) {
 			Integer groupId = new Integer(matcher.group(2));
 			Integer id = new Integer(matcher.group(3));
 			themes.add(new Theme(groupId, id, matcher.group(4)));
 		}
 		Log.d(toString(), "group page parsed");
 		return themes;
 	}
 }
 
 /**
  * Фоновый загрузчик страницы со списком тем.
  * @author Mic, 2011
  * 
  */
 public class DownloadGroupPageTask extends DownloadTask<Void, Void, ArrayList<Theme>> {
 
 	public interface OnLoadCommand {
 		public void callback(ArrayList<Theme> themes);
 	}
 
 	/**
 	 * Выполнится по окончании загрузки.
 	 */
 	protected OnLoadCommand _onLoad;
 
	public DownloadGroupPageTask(OnLoadCommand onLoad) {
 		_onLoad = onLoad;
 	}
 	
 	/**
 	 * Возвращает адрес страницы.
 	 */
 	@Override
 	protected String _getUri() {
		return "http://mic-dm.tom.ru/hot.html";
 	}
 
 	@Override
 	public ArrayList<Theme> doInBackground(Void... voids) {
 		try {
 			String body = _downloadPage();
 			return new GroupParser().parse(body);
 		} catch (IOException e) {
 			Log.e(toString(), e.toString());
 			return null;
 		}
 	}
 
 	@Override
 	public void onPostExecute(ArrayList<Theme> themes) {
 		_onLoad.callback(themes);
 	}
 }
