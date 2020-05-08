 /*******************************************************************************
  * Copyright (C) 2010-2011 Dmitriy Nesterov
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package byku.traindroid;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 /*
  * Static class that processes saved data. 
  */
 public final class DataFacade 
 {
 	/*
 	 * Timetables and stations.
 	 */
 	private static ArrayList<TimeTable> _timeTables = new ArrayList<TimeTable>();
 	private static ArrayList<Station> _stations = new ArrayList<Station>();
 	
 	/*
 	 * Sources addresses.
 	 */
 	private static final String TUTU_ADRESS = "http://www.tutu.ru/rasp.php?st1=%s&st2=%s&date=%s&print=yes";
 	private static final String YANDEX_ADRESS = "http://www.rasp.yandex.ru/suburban_search?cityFrom=%s&cityTo=%s&dateForward=%s";
 	
 	/*
 	 * Regular expressions.
 	 */
 	private static final String TIME_REGEXP = "\\d\\d:\\d\\d";
 	public static String TUTU_REGEXP = "";
 	public static String YANDEX_REGEXP = "";
 	
 	public static void Init()
 	{
 		if (TUTU_REGEXP.equals(""))
 			TUTU_REGEXP = ">\\d\\d:\\d\\d</a";
 		if (YANDEX_REGEXP.equals(""))
 			YANDEX_REGEXP = "(>|\\s)\\d\\d:\\d\\d<";
 		
 		if (_stations.size() == 0)
 		{
 			Station station;
 			station = new Station("Крюково", "80710", "Крюково");
 			_stations.add(station);
 			station = new Station("Москва", "79310", "Москва");
 			_stations.add(station);
 			station = new Station("Петровско-Разумовское", "79610", "Петровско-Разумовское");
 			_stations.add(station);
 			
 //			station = new Station("Подсолнечная", "81310", "Подсолнечная");
 //			_stations.add(station);
 //			station = new Station("Поваровка", "81010", "Поваровка");
 //			_stations.add(station);
 //			station = new Station("Фроловское", "81710", "Фроловское");
 //			_stations.add(station);
 //			station = new Station("Поварово", "81110", "Поварово");
 //			_stations.add(station);
 //			station = new Station("Рижская", "79410", "Рижская");
 //			_stations.add(station);
 //			station = new Station("Останкино", "79510", "Останкино");
 //			_stations.add(station);
 //			station = new Station("Левобережная", "79910", "Левобережная");
 //			_stations.add(station);
 		}
 	}
 	
 	public static ArrayList<TimeTable> GetTimeTables()
 	{
 		return _timeTables;
 	}
 	
 	public static ArrayList<TimeTable> GetTimeTables(String date)
 	{
 		ArrayList<TimeTable> timeTables = new ArrayList<TimeTable>();
 		
 		for (TimeTable timeTable : _timeTables) 
 		{
 			if (timeTable.getDate().equals(date))
 			{
 				timeTables.add(timeTable);
 			}
 		}
 		
 		return timeTables;
 	}
 	
 	public static ArrayList<Station> GetStations()
 	{
 		return _stations;
 	}
 	
 	public static String[] GetStationNames()
 	{
 		String[] result = new String[_stations.size()];
 		for (int i = 0; i < _stations.size(); ++i)
 		{
 			result[i] = _stations.get(i).getName();
 		}
 		
 		return result;
 	}
 	
 	public static Station FindStation(String name)
 	{
 		for(int i = 0; i < _stations.size(); ++i)
 		{
 			if (_stations.get(i).getName().equals(name))
 			{
 				return _stations.get(i);
 			}
 		}
 		
 		return null;
 	}
 	
 	public static ArrayList<TimeTable> GetTimeTables(String date, Station stationFrom, Station stationTo)
 	{
 		ArrayList<TimeTable> timeTables = new ArrayList<TimeTable>();
 		
 		for (TimeTable timeTable : _timeTables) 
 		{
 			if (timeTable.getDate().equals(date) 
 				&& timeTable.getStationFrom().equals(stationFrom)
 				&& timeTable.getStationTo().equals(stationTo))
 			{
 				timeTables.add(timeTable);
 			}
 		}
 		
 		return timeTables;
 	}
 	
 	public static void Serialize(FileOutputStream stream) throws IOException
 	{
 		ByteUtils.WriteInt(stream, _stations.size());
 		for (Station station : _stations) 
 		{
 			station.Serialize(stream);
 		}
 		
 		ByteUtils.WriteInt(stream, _timeTables.size());
 		for (TimeTable timeTable : _timeTables) 
 		{
 			timeTable.Serialize(stream);
 		}
 		
 		ByteUtils.WriteString(stream, TUTU_REGEXP);
 		ByteUtils.WriteString(stream, YANDEX_REGEXP);
 	}
 	
 	public static void Deserialize(FileInputStream stream) throws IOException
 	{
 		int size = ByteUtils.ReadInt(stream);
 		_stations.clear();
 		for (int i = 0; i < size; ++i) 
 		{
 			_stations.add(new Station(stream));
 		}
 		
 		size = ByteUtils.ReadInt(stream);
 		_timeTables.clear();
 		for (int i = 0; i < size; ++i) 
 		{
 			_timeTables.add(new TimeTable(stream));
 		}
 		
 		TUTU_REGEXP = ByteUtils.ReadString(stream);
 		YANDEX_REGEXP = ByteUtils.ReadString(stream);
 	}
 	
 	/*
 	 * Gets new timetables for specified sources, stations, dates and saves it locally.
 	 * @return Error description if it occurred.
 	 */
 	public static String UpdateTimeTable(Station from, Station to, int dayFrom, int daysCount, 
 			Boolean updateYandex, Boolean updateTutu)
 	{
 		String result = "";
 		Calendar calendar = Calendar.getInstance();
 		int day = calendar.get(Calendar.DATE) + dayFrom;
 		int month = calendar.get(Calendar.MONTH) + 1;
 		int year = calendar.get(Calendar.YEAR);
 		
 		for (int i = 0; i < daysCount; ++i)
 		{
 			if (day > calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
 			{
 				day -= calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
 				++month;
 			}
 			String date = Utils.DateToString(day, month, year);
 			
 			if (updateTutu)
 			{
 				String url = String.format(TUTU_ADRESS, from.getTutuId(), to.getTutuId(), date);
 				String message = processSource(url, from, to, date, TUTU_REGEXP, "tutu");
 				result += (message != "") ? "\n" + message : "";
 			}
 
 			if (updateYandex)
 			{
 				String url = String.format(YANDEX_ADRESS, from.getYandexId(), to.getYandexId(), date);
 				String message = processSource(url, from, to, date, YANDEX_REGEXP, "yandex");
 				result += (message != "") ? "\n" + message : "";
 			}
 			
 			++day;
 		}
 		
 		return result;
 	}
 	
 	public static void ClearTimeTable(Boolean all)
 	{
 		if (all)
 		{
 			_timeTables.clear();
 			return;
 		}
 		
 		Date today = Utils.Today();
 		
 		for (int i = 0; i < _timeTables.size(); ++i) 
 		{
 			Date date = Utils.StringToDate(_timeTables.get(i).getDate(), true);
 			if (date.before(today))
 			{
 				_timeTables.remove(i);
 				--i;
 			}
 		}
 	}
 	
 	/*
 	 * Process request for new timetable for one source.
 	 * @return Error description if it occurred.
 	 */
 	private static String processSource(String url, Station from, Station to, String date, String regexp, String sourceName)
 	{
 		ArrayList<Pair<String, String>> times = new ArrayList<Pair<String, String>>();
 		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(URLEncoder.encode(url));
 		HttpResponse response;
 				
 		try 
 		{
 			response = client.execute(request);
 			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 			response.getEntity().writeTo(buffer);
 			
 			String message = parsePage(new String(buffer.toByteArray()), regexp, times);
 			if (!message.equals(""))
 			{
 				_timeTables.add(new TimeTable(from, to, date + "*", sourceName, times));
 				return message;
 			}
 			
 			removeTimeTableIfExists(from.getName(), to.getName(), date, sourceName);
 			
 			_timeTables.add(new TimeTable(from, to, date, sourceName, times));
 		} 
 		catch (Exception e) 
 		{
 			return e.getMessage();
 		}		
 		
 		return "";
 	}
 	
 	/*
 	 * Parses page content using regexp and fill array with times of trains.
 	 * @return Error description if it occurred. 
 	 */
 	private static String parsePage(String page, String regexp, ArrayList<Pair<String, String>> times)
 	{
 		ArrayList<String> matches = new ArrayList<String>();
 		
 		Pattern pattern = Pattern.compile(regexp);
 		Matcher matcher = pattern.matcher(page);
 		
 		Pattern timePattern = Pattern.compile(TIME_REGEXP);
 		Matcher timeMatcher = timePattern.matcher("");
 		
 		while(matcher.find())
 		{
 		    matches.add(matcher.group());
 		}
 		
 		if (matches.size() == 0)
 		{
 			return "Trains data not found. Maybe server changes it's format.";
 		}
 
 		for (int i = 0; i < matches.size(); i += 2)
 		{
 			if (i + 1 == matches.size())
 			{
 				return "Incorrect times count.";
 			}
 			
 			if (matches.get(i).equals(matches.get(i + 1)))
 			{
 				continue;
 			}
 			
 			timeMatcher.reset(matches.get(i));
 			String timeFrom = timeMatcher.find() ? timeMatcher.group() : "error";
 
 			timeMatcher.reset(matches.get(i + 1));
 			String timeTo = timeMatcher.find() ? timeMatcher.group() : "error";
 			
 			times.add(new Pair<String, String>(timeFrom, timeTo));
 		}
 		
 		return "";
 	}
 	
 	private static void removeTimeTableIfExists(String from, String to, String date, String sourceName)
 	{
 		for (int i = 0; i < _timeTables.size(); ++i)
 		{
 			TimeTable timeTable = _timeTables.get(i);
 			if (timeTable.getStationFrom().getName().equals(from)
 				&& timeTable.getStationTo().getName().equals(to)
 				&& timeTable.getDate().equals(date)
 				&& timeTable.getSource().equals(sourceName))
 			{
 				_timeTables.remove(i);
 			}
 		}
 	}
 }
