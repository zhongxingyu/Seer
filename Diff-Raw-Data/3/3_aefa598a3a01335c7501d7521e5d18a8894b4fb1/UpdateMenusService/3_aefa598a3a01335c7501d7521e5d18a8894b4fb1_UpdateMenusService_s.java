 package de.manuel_voegele.cafeteria.tue;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.IntentService;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 
 /**
  * A service updating the menu for a specified cafeteria
  * 
  * The id of the
  * 
  * @author Manuel Vögele
  */
 public class UpdateMenusService extends IntentService
 {
 	/**
 	 * The log tag
 	 */
 	public static final String LOG_TAG = UpdateMenusService.class.getSimpleName();
 
 	/**
 	 * Initializes a new {@link UpdateMenusService}
 	 */
 	public UpdateMenusService()
 	{
 		super(UpdateMenusService.class.getSimpleName());
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent)
 	{
 		int cafeteriaId = intent.getIntExtra("cafeteriaid", -1);
 		if (cafeteriaId == -1)
 		{
 			intent = new Intent();
 			intent.setAction(MainActivity.SHOW_ERROR_MESSAGE_ACTION);
 			intent.putExtra("message", R.string.error_unexpected);
 			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
 			Log.wtf(LOG_TAG, "The cafeteriaid may not be -1 (unset)");
 			return;
 		}
 		SQLiteDatabase db = SQLiteDatabase.openDatabase(new File(getFilesDir(), "database.db").getPath(), null, SQLiteDatabase.OPEN_READWRITE);
 		Calendar calendar = Calendar.getInstance();
 		try
 		{
 			for (int i = 0;true;i++)
 			{
 				String htmlCode = fetchMenuPage(cafeteriaId, calendar.getTime());
 				if (!parsePage(htmlCode, db, cafeteriaId) && i != 0)
 					break;
 				calendar.add(Calendar.WEEK_OF_YEAR, 1);
 			}
 		}
 		catch (MalformedURLException e)
 		{
 			intent = new Intent();
 			intent.setAction(MainActivity.SHOW_ERROR_MESSAGE_ACTION);
 			intent.putExtra("message", Integer.valueOf(R.string.error_unexpected));
 			intent.putExtra("exception", e);
 			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
 			Log.wtf(LOG_TAG, "Fetching data failed", e);
 			return;
 		}
 		catch (ParseException e)
 		{
 			intent = new Intent();
 			intent.setAction(MainActivity.SHOW_ERROR_MESSAGE_ACTION);
 			intent.putExtra("message", Integer.valueOf(R.string.error_unexpected));
 			intent.putExtra("exception", e);
 			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
 			Log.wtf(LOG_TAG, "Fetching data failed", e);
 			return;
 		}
 		catch (IOException e)
 		{
 			intent = new Intent();
 			intent.setAction(MainActivity.SHOW_ERROR_MESSAGE_ACTION);
 			intent.putExtra("message", Integer.valueOf(R.string.error_data_fetch));
 			intent.putExtra("exception", e);
 			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
 			Log.e(LOG_TAG, "Fetching data failed", e);
 			return;
 		}
 		finally
 		{
 			db.close();
 		}
 		intent = new Intent();
 		intent.setAction(MainActivity.REFRESH_MENU_SCREEN_ACTION);
 		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
 	}
 
 	/**
 	 * Loads the menu page for the for the specified week into a string
 	 * 
 	 * @param cafeteriaId
 	 *           the id of the cafeteria
 	 * @param week
 	 *           a date in the week for which the data should be fetched
 	 * @return the HTML code of the page
 	 * @throws MalformedURLException
 	 *            if fetching the menu page fails (this should never happen)
 	 * @throws IOException
 	 *            if fetching the menu page fails
 	 */
 	public static String fetchMenuPage(int cafeteriaId, Date week) throws MalformedURLException, IOException
 	{
 		URL url = new URL("http://www.my-stuwe.de/cms/80/1/1/art/WasgibtesheuteinderMensaSpeiseplaene.html");
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		connection.setDoOutput(true);
 		PostParameterWriter writer = new PostParameterWriter(new OutputStreamWriter(connection.getOutputStream()));
 		//			writer.write("selWeek=2013-11&ORT_ID=631&selView=liste&aktion=changeWeek&vbLoc=&lang=1&client=");
 		writer.putParameter("ORT_ID", String.valueOf(cafeteriaId));
 		writer.putParameter("selWeek", new SimpleDateFormat("y-w").format(week));
 		writer.putParameter("selView", "liste");
 		writer.putParameter("lang", "1");
 		writer.putParameter("aktion", "changeWeek");
 		writer.putParameter("vbLoc", "");
 		writer.putParameter("client", "");
 		writer.close();
 		String encoding = StringUtils.substringAfter(connection.getContentType(), "charset=");
 		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
 		StringBuilder pageSource = new StringBuilder();
 		String line;
 		while ((line = reader.readLine()) != null)
 		{
 			pageSource.append(line);
 		}
 		reader.close();
 		return pageSource.toString();
 	}
 
 	/**
 	 * Parses the HTML page and writes the new menus into the database. The old
 	 * menus of the refreshed days will be deleted.
 	 * 
 	 * @param htmlCode
 	 *           the HTML code of the menu page
 	 * @param db
 	 *           the database to store the menu in
 	 * @param cafeteriaid
 	 *           the id of the cafeteria
 	 * @return <code>true</code> if any menus were parsed
 	 * @throws ParseException
 	 *            if parsing the date fails - most likely the site has changed
 	 */
 	public static boolean parsePage(String htmlCode, SQLiteDatabase db, int cafeteriaid) throws ParseException
 	{
 		Integer cid = Integer.valueOf(cafeteriaid);
 		htmlCode = StringUtils.substringAfter(htmlCode, "<div class=\"\">");
 		htmlCode = StringUtils.substringBefore(htmlCode, "<table class");
 		htmlCode = htmlCode.replace("\n", " ");
 		SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.y");
 		Pattern dayPattern = Pattern.compile("<div.*?>.*?, (.*?)</div>.*?<table.*?>(.*?)</table>");
 		Matcher dayMatcher = dayPattern.matcher(htmlCode);
 
 		Pattern menuRowPattern = Pattern.compile("<tr.*?</tr>");
 		
 		Pattern menuTypePattern = Pattern.compile("<td.*?>(.*?)</td>");
 		Pattern menuMenuPattern = Pattern.compile("<td.*?>\\s*(.*?)\\s*&nbsp;");
 		Pattern priceNormalPattern = Pattern.compile("<td.*?Gäste: (.*?) ");
 		Pattern pricePupilPattern = Pattern.compile("Schüler:(.*?) ");
 		Pattern priceStudentPattern = Pattern.compile("&nbsp;\\s*(.*?) ");
 		Pattern[] patterns = new Pattern[] {menuTypePattern, menuMenuPattern, priceNormalPattern, pricePupilPattern, priceStudentPattern};
 		if (!dayMatcher.find())
 			return false;
 		do
 		{
 			Date day = dateFormat.parse(dayMatcher.group(1));
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTime(day);
 			Long timestamp = Long.valueOf(calendar.getTimeInMillis());
 			Matcher menuRowMatcher = menuRowPattern.matcher(dayMatcher.group(2).replaceAll("<tr.*?<th.*?</tr>", " "));
 			while (menuRowMatcher.find())
 			{
 				String menuRow = menuRowMatcher.group();
 				String[] results = matchPatterns(menuRow, patterns);
 				String menuType = results[0];
 				String menuMenu = results[1];
 				String priceNormal = results[2];
 				String pricePupil = results[3];
 				String priceStudent = results[4];
 				Double normalprice = priceNormal != null ? Double.valueOf(priceNormal.replace(',', '.')) : null;
 				Double pupilprice = pricePupil != null ? Double.valueOf(pricePupil.replace(',', '.')) : null;
 				Double studentprice = priceStudent != null ? Double.valueOf(priceStudent.replace(',', '.')) : null;
 				menuMenu = menuMenu.replace("<br />", ", ");
 				ContentValues values = new ContentValues();
 				values.put("cafeteriaid", cid);
 				values.put("type", menuType);
 				values.put("menu", menuMenu);
 				values.put("normalprice", normalprice);
 				values.put("pupilprice", pupilprice);
 				values.put("studentprice", studentprice);
 				values.put("day", timestamp);
 				db.insert("menus", null, values);
 			}
			db.delete("menus", "cafeteriaid = ? AND day = ?", new String[] { String.valueOf(cafeteriaid), timestamp.toString() });
 		} while (dayMatcher.find());
 		return true;
 	}
 
 	/**
 	 * Matches the specified patterns on a string. Each pattern has to occur
 	 * after the previous one. The found string will always be the first group
 	 * 
 	 * @param s
 	 *           the string
 	 * @param patterns
 	 *           an array of the patterns to match
 	 * @return an array of the found strings
 	 */
 	private static String[] matchPatterns(String s, Pattern[] patterns)
 	{
 		String[] result = new String[patterns.length];
 		int nextStart = 0;
 		for (int i = 0;i < patterns.length;i++)
 		{
 			Matcher matcher = patterns[i].matcher(s);
 			if (matcher.find(nextStart))
 			{
 				nextStart = matcher.end();
 				result[i] = matcher.group(1);
 			}
 		}
 		return result;
 	}
 }
