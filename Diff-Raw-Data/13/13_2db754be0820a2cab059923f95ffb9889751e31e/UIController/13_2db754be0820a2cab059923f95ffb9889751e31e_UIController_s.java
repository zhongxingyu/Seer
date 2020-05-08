 /**
  * 
  * Notië Imberissëo
  * 
  * Copyright 2012 Joaquín Gatica (Erutulco Eruntano)
  * 
  * Contact:
  * 		Twitter: <http://twitter.com/joaquingatica>
  * 		Email: <erutulco@quenya101.com>
  * 
  * This file is part of "Notië Imberissëo".
  * 
  * "Notië Imberissëo" is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * "Notië Imberissëo" is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with "Notië Imberissëo".  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package main;
 
 import java.sql.Time;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 import java.util.prefs.Preferences;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JList;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 
 import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
 import com.luckycatlabs.sunrisesunset.dto.Location;
 
 import config.Config;
 import data.GregorianInfo;
 import data.ImladrisInfo;
 import erutulco.utils.ImladrisCalendar;
 import geo.google.GeoAddressStandardizer;
 import geo.google.GeoException;
 import geo.google.datamodel.GeoCoordinate;
 
 public class UIController {
 
 	private static final String FIELD_CITY = "city";
 	private static final String FIELD_COUNTRY = "country";
 	private static final String FIELD_TIMEZONE = "timezone";
 	private static final String DEF_CITY = "Montevideo";
 	private static final String DEF_COUNTRY = "Uruguay";
 	private static final String DEF_TIMEZONE = TimeZone.getDefault().getID();
 	
 	private UI ui;
 	private Preferences preferences = null;
 	public void setUi(UI ui) {
 		this.ui = ui;
 	}
 	public UI getUi() {
 		return ui;
 	}
 	public void setPreferences(Preferences preferences) {
 		this.preferences = preferences;
 	}
 	public Preferences getPreferences() {
 		return preferences;
 	}
 	
 	private static UIController instance = null;
 	public static UIController getInstance() {
 		if(instance == null) {
 			instance = new UIController();
 		}
 		return instance;
 	}
 	private UIController() {
 	}
 	
 	public void initializeWindow() {
 		this.createWindow();
 		this.fillData();
 	}
 	private void createWindow() {
 		UI ui = UI.getInstance();
 		ui.getFrame().setVisible(true);
 		this.setUi(ui);
 	}
 	private void fillData() {
 		this.showDateOfToday();
 		this.fillSettingsForm();
 	}
 	
 	private String makeLocationString(String city, String country) {
 		String place = "";
 		if(city.length() > 0 || country.length() > 0) {
 			if(city.length() > 0) {
 				place += city;
 			}
 			if(country.length() > 0) {
 				if(place.length() > 0) {
 					place += ", ";
 				}
 				place += country;
 			}
 		}
 		return place;
 	}
 	public Time calculateSunsetForActualLocationAndTime() {
		Time time = this.calculateSunsetForActualLocation(new GregorianCalendar());
 		return time;
 	}
 	public Time calculateSunsetForActualLocation(Calendar calendar) {
 		Preferences data = this.getCurrentPreferences();
 		String city = data.get(FIELD_CITY, DEF_CITY);
 		String country = data.get(FIELD_COUNTRY, DEF_COUNTRY);
		Time time = this.calculateSunset(Calendar.getInstance(), city, country);
 		return time;
 	}
 	public Time calculateSunset(Calendar calendar, String city, String country) {
 		Time time = null;
 		String place = this.makeLocationString(city, country);
 		if(place.length() > 0) {
 			GeoAddressStandardizer st = new GeoAddressStandardizer(new Config().getGoogleMapsApiKey());
 			Location location = null;
 			try {
 				GeoCoordinate geo = st.standardizeToGeoCoordinate(place);
 				double latitude = geo.getLatitude();
 				double longitude = geo.getLongitude();
 				location = new Location(Double.toString(latitude), Double.toString(longitude));
 				Preferences data = this.getCurrentPreferences();
 				String timezone = data.get(FIELD_TIMEZONE, DEF_TIMEZONE);
 				/*TimeZone tz;
 				if(timezone.length() == 0) {
 					tz = TimeZone.getDefault();
 				}
 				else {
 					tz = TimeZone.getTimeZone(timezone);
 				}*/
 				SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, timezone);
 				String sunset = calculator.getOfficialSunsetForDate(calendar)+":00";
 				time = Time.valueOf(sunset);
 			} catch (GeoException e) {
 				System.out.println(e.getMessage());
 			}
 		}
 		return time;
 	}
 	public void showDateOfToday() {
 		Preferences data = this.getCurrentPreferences();
 		String city = data.get(FIELD_CITY, DEF_CITY);
 		String country = data.get(FIELD_COUNTRY, DEF_COUNTRY);
		Time time = this.calculateSunset(Calendar.getInstance(), city, country);
		GregorianCalendar gcal = new GregorianCalendar();
 		ImladrisCalendar cal;
 		String sunsetStr = "";
 		String locationInfo = "";
 		if(time == null) {
 			cal = new ImladrisCalendar(gcal);
 		}
 		else {
 			cal = new ImladrisCalendar(time, gcal);
 			sunsetStr = " (Sunset at: "+time.toString()+")";
 			locationInfo = this.makeLocationString(city, country);
 			if(locationInfo.length() > 0) {
 				locationInfo = "(Loc.: "+locationInfo+" | TZ: "+data.get(FIELD_TIMEZONE, DEF_TIMEZONE)+")";
 			}
 		}
 		String gstr = this.gregorianToString(gcal);
 		String istr = cal.toString();
 		UI ui = this.getUi();
 		ui.getTodayGregorian().setText(gstr+sunsetStr);
 		ui.getTodayImladris().setText(istr);
 		ui.getLocationInfo().setText(locationInfo);
 	}
 	
 	
 	/******** FROM GREGORIAN **********/
 	
 	public void updateDayComboGregorian() {
 		UI window = this.getUi();
 		
 		JTextField year = window.getYear();
 		JComboBox month = window.getMonth();
 		JComboBox day = window.getDay();
 		JButton convert = window.getToImladris();
 		JTextPane result = window.getResImladris();
 		String value = year.getText();
 		if(!value.isEmpty()) {
 			try {
 				int yearNum = Integer.parseInt(value);
 				if(yearNum > 0 && yearNum <= GregorianInfo.MAX_SUPPORTED_YEAR) {
 					int monthNum = month.getSelectedIndex() + 1;
 					int daySel = 0;
 					if(day.isEnabled()) {
 						daySel = day.getSelectedIndex() + 1;
 					}
 					ArrayList<Integer> days = GregorianInfo.getInstance().getDaysArray(yearNum, monthNum);
 					day.setModel(new DefaultComboBoxModel(days.toArray()));
 					if(daySel > 0 && daySel <= days.size()) {
 						day.setSelectedIndex(daySel-1);
 					}
 					day.setEnabled(true);
 					convert.setEnabled(true);
 					result.setText("");
 				}
 				else {
 					day.setEnabled(false);
 					convert.setEnabled(false);
 					day.setModel(new DefaultComboBoxModel());
 					result.setText("");
 				}
 			}
 			catch(NumberFormatException e) {
 				day.setEnabled(false);
 				convert.setEnabled(false);
 				day.setModel(new DefaultComboBoxModel());
 				result.setText("");
 			}
 		}
 		else {
 			day.setEnabled(false);
 			convert.setEnabled(false);
 			day.setModel(new DefaultComboBoxModel());
 			result.setText("");
 		}
 	}
 	@SuppressWarnings("deprecation")
 	public void convertToImladris() {
 		UI window = this.getUi();
 		
 		String errorEmptyYear = "Please insert a year.";
 		String errorYearNotNumeric = "Please insert the year as a numeric value.";
 		String errorYearNotValid = "Please insert a valid year (from 1 to "+Integer.toString(GregorianInfo.MAX_SUPPORTED_YEAR)+").";
 		String errorDayNotRead = "Sorry, the day could not be read.";
 		
 		JTextField year = window.getYear();
 		JComboBox month = window.getMonth();
 		JComboBox day = window.getDay();
 		JCheckBox afterSunset = window.getAfterSunset();
 		JTextPane result = window.getResImladris();
 		String value = year.getText();
 		if(!value.isEmpty()) {
 			try {
 				int yearNum = Integer.parseInt(value);
 				if(yearNum > 0 && yearNum <= GregorianInfo.MAX_SUPPORTED_YEAR) {
 					int monthNum = month.getSelectedIndex() + 1;
 					int dayNum = 0;
 					if(day.isEnabled()) {
 						dayNum = day.getSelectedIndex() + 1;
 						ImladrisCalendar cal;
 						if(afterSunset.isSelected()) {
 							GregorianCalendar gcal = new GregorianCalendar(yearNum, monthNum, dayNum);
 							Time time = this.calculateSunsetForActualLocation(gcal);
 							cal = new ImladrisCalendar(time, yearNum, monthNum, dayNum, time.getHours(), time.getMinutes(), time.getSeconds());
 						}
 						else {
 							cal = new ImladrisCalendar(yearNum, monthNum, dayNum);
 						}
 						result.setText(cal.toString());
 					}
 					else {
 						result.setText(errorDayNotRead);
 					}
 				}
 				else {
 					result.setText(errorYearNotValid);
 				}
 			}
 			catch(NumberFormatException e) {
 				result.setText(errorYearNotNumeric);
 			}
 		}
 		else {
 			result.setText(errorEmptyYear);
 		}
 	}
 	
 	/************* TO GREGORIAN *************/
 
 	public void updateDayComboImladris() {
 		UI window = this.getUi();
 		
 		JComboBox yen = window.getYen();
 		JTextField loa = window.getLoa();
 		JComboBox period = window.getPeriod();
 		JComboBox day = window.getDayOfLoa();
 		JButton convert = window.getToGregorian();
 		JTextPane result = window.getResGregorian();
 		int yenNum = yen.getSelectedIndex() + 1;
 		String value = loa.getText();
 		if(!value.isEmpty()) {
 			try {
 				int loaNum = Integer.parseInt(value);
 				if(loaNum > 0 && loaNum <= 144) {
 					int periodNum = period.getSelectedIndex() + 1;
 					if(periodNum == ImladrisCalendar.YESTARE || periodNum == ImladrisCalendar.METTARE) {
 						day.setEnabled(false);
 						day.setModel(new DefaultComboBoxModel());
 						convert.setEnabled(true);
 						result.setText("");
 					}
 					else {
 						int daySel = 0;
 						if(day.isEnabled()) {
 							daySel = day.getSelectedIndex()+1;
 						}
 						ArrayList<Integer> days = ImladrisInfo.getInstance().getDaysArray(yenNum, loaNum, periodNum);
 						day.setModel(new DefaultComboBoxModel(days.toArray()));
 						if(daySel > 0 && daySel <= days.size()) {
 							day.setSelectedIndex(daySel-1);
 						}
 						day.setEnabled(true);
 						convert.setEnabled(true);
 						result.setText("");
 					}
 				}
 				else {
 					day.setEnabled(false);
 					convert.setEnabled(false);
 					day.setModel(new DefaultComboBoxModel());
 					result.setText("");
 				}
 			}
 			catch(NumberFormatException e) {
 				day.setEnabled(false);
 				convert.setEnabled(false);
 				day.setModel(new DefaultComboBoxModel());
 				result.setText("");
 			}
 		}
 		else {
 			day.setEnabled(false);
 			convert.setEnabled(false);
 			day.setModel(new DefaultComboBoxModel());
 			result.setText("");
 		}
 	}
 	public void convertToGregorian() {
 		UI window = this.getUi();
 		
 		String errorNoLoa = "Please insert a loa.";
 		String errorLoaNotNumeric = "Please insert the loa as a numeric value.";
 		String errorYearNotValid = "Please insert a valid loa (from 1 to 144).";
 		String errorDayNotRead = "Sorry, the day could not be read.";
 		
 		JComboBox yen = window.getYen();
 		JTextField loa = window.getLoa();
 		JComboBox period = window.getPeriod();
 		JComboBox day = window.getDayOfLoa();
 		JCheckBox beforeMidnight = window.getBeforeMidnight();
 		JTextPane result = window.getResGregorian();
 		int yenNum = yen.getSelectedIndex() + 1;
 		String value = loa.getText();
 		if(!value.isEmpty()) {
 			try {
 				int loaNum = Integer.parseInt(value);
 				if(loaNum > 0 && loaNum <= 144) {
 					int periodNum = period.getSelectedIndex() + 1;
 					ImladrisCalendar cal = new ImladrisCalendar();
 					boolean success = false;
 					if(periodNum == ImladrisCalendar.YESTARE || periodNum == ImladrisCalendar.METTARE) {
 						success = true;
 						cal = new ImladrisCalendar(cal.intToRoman(yenNum), loaNum, periodNum);
 					}
 					else {
 						int dayNum = 0;
 						if(day.isEnabled()) {
 							success = true;
 							dayNum = day.getSelectedIndex() + 1;
 							cal = new ImladrisCalendar(cal.intToRoman(yenNum), loaNum, periodNum, dayNum);
 						}
 						else {
 							result.setText(errorDayNotRead);
 						}
 					}
 					if(success) {
 						GregorianCalendar gcal = cal.getGregorian();
 						if(beforeMidnight.isSelected()) {
 							gcal.add(GregorianCalendar.DAY_OF_YEAR, -1);
 						}
 						String gstr = this.gregorianToString(gcal);
 						result.setText(gstr);
 					}
 				}
 				else {
 					result.setText(errorYearNotValid);
 				}
 			}
 			catch(NumberFormatException e) {
 				result.setText(errorLoaNotNumeric);
 			}
 		}
 		else {
 			result.setText(errorNoLoa);
 		}
 	}
 	
 	/************* SETTINGS ****************/
 	
 	private void fillSettingsForm() {
 		UI ui = this.getUi();
 		Preferences data = this.getCurrentPreferences();
 		/* Country and city */
 		String city = data.get(FIELD_CITY, DEF_CITY);
 		String country = data.get(FIELD_COUNTRY, DEF_COUNTRY);
 		ui.getCity().setText(city);
 		ui.getCountry().setText(country);
 		/* Time Zone */
 		String[] tzs = TimeZone.getAvailableIDs();
 		Arrays.sort(tzs);
 		JList list = ui.getTimeZone();
 		list.setListData(tzs);
 		String timezone = data.get(FIELD_TIMEZONE, DEF_TIMEZONE);
 		list.setSelectedValue(timezone, true);
 	}
 	public void saveSettings() {
 		UI ui = this.getUi();
 		Preferences data = this.getCurrentPreferences();
 		data.put(FIELD_CITY, ui.getCity().getText());
 		data.put(FIELD_COUNTRY, ui.getCountry().getText());
 		data.put(FIELD_TIMEZONE, (String)ui.getTimeZone().getSelectedValue());
 		this.showDateOfToday();
 	}
 	
 	/************ OTHER *****************/
 	
 	private String gregorianToString(GregorianCalendar gcal) {
 		SimpleDateFormat sdf = new SimpleDateFormat("EEEEEEEE, MMMMMMMMM d, yyyy");
 		String gstr = sdf.format(gcal.getTime());
 		return gstr;
 	}
 	private Preferences getCurrentPreferences() {
 		Preferences prefs = this.getPreferences();
 		if(prefs == null || !(prefs instanceof Preferences)) {
 			prefs = Preferences.userRoot().node(this.getClass().getName());
 			this.setPreferences(prefs);
 		}
 		return prefs;
 	}
 	
 	
 }
