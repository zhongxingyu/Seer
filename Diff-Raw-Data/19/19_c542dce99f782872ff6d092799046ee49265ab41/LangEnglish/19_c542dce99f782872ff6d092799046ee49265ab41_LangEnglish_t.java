 package lang.available;
 
 import lang.Lang;
 
 public class LangEnglish extends Lang {
 	
 	private String name = "English";
 	private String arrayName = "English";
 	private String shortName = "eng";
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setArrayName(String arrayName) {
 		this.arrayName = arrayName;
 	}
 	public String getArrayName() {
 		return arrayName;
 	}
 	public void setShortName(String shortName) {
 		this.shortName = shortName;
 	}
 	public String getShortName() {
 		return shortName;
 	}
 	
 	public LangEnglish() {
 	}
 	
 	public void defineLang() {
 		Lang.name = this.getName();
 		Lang.short_name = this.getShortName();
 		Lang.uses_tengwar = false;
 		/* Punctuation */
 		Lang.punctuation.dot = ".";
 		Lang.punctuation.comma = ",";
 		Lang.punctuation.semicolon = ";";
 		Lang.punctuation.double_dot = ":";
 		Lang.punctuation.parenthesis_open = "(";
 		Lang.punctuation.parenthesis_close = ")";
 		Lang.punctuation.pipe = "|";
 		/* Common */
 		Lang.common.app_title = "Notië Imberissëo";
 		Lang.common.app_version = "2.4";
 		Lang.common.app_version_date = "Lairë 70, XIV 140";
 		Lang.common.current_date_label = "Current date";
 		Lang.common.location_label = "Loc.";
 		Lang.common.timezone_label = "TZ";
 		Lang.common.gregorian_label = "Gregorian";
 		Lang.common.imladris_label = "Imladris";
 		/* To Imladris tab */
 		Lang.to_imladris_tab.title = "To Imladris Reckoning";
 		Lang.to_imladris_tab.tooltip = "Convert from Gregorian to Imladris calendar";
 		Lang.to_imladris_tab.choose_label = "Choose Gregorian date";
 		Lang.to_imladris_tab.resulting_label = "Resulting Imladris date";
 		Lang.to_imladris_tab.year_label = "Year";
 		Lang.to_imladris_tab.year_tooltip = "Valid years: 1 - 2299";
 		Lang.to_imladris_tab.month_label = "Month";
 		Lang.to_imladris_tab.day_label = "Day";
 		Lang.to_imladris_tab.after_sunset_label = "After sunset";
 		Lang.to_imladris_tab.resulting_format = "Format: Weekday, Period [Day#], Yén Loa";
 		/* From Imladris tab */
		Lang.from_imladris_tab.title = "From Imladris Reckoning";
 		Lang.from_imladris_tab.tooltip = "Convert from Imladris to Gregorian calendar";
 		Lang.from_imladris_tab.choose_label = "Choose Imladris date";
 		Lang.from_imladris_tab.resulting_label = "Resulting Gregorian date";
 		Lang.from_imladris_tab.yen_label = "Yén";
 		Lang.from_imladris_tab.loa_label = "Loa";
 		Lang.from_imladris_tab.loa_tooltip = "Valid loar: 1 - 144";
 		Lang.from_imladris_tab.period_label = "Period";
 		Lang.from_imladris_tab.day_label = "Day";
 		Lang.from_imladris_tab.before_midnight_label = "Before midnight";
 		Lang.from_imladris_tab.resulting_format = "Format: Weekday, Month Day, Year";
 		/* Settings tab */
 		Lang.settings_tab.title = "Settings";
 		Lang.settings_tab.tooltip = "Edit configurations";
 		Lang.settings_tab.location = "Location";
 		Lang.settings_tab.city_label = "City";
 		Lang.settings_tab.country_label = "Country";
 		Lang.settings_tab.language_label = "Language";
 		Lang.settings_tab.timezone = "Timezone";
 		Lang.settings_tab.save = "Save";
 		/* About tab */
 		Lang.about_tab.title = "About";
 		Lang.about_tab.tooltip = "About the application and author.";
 		Lang.about_tab.app_name = "Notië Imberissëo";
 		Lang.about_tab.tengwar_name = "5^1T`V `Bw$7T,R`H";
 		Lang.about_tab.info = "<font size=\"3\">By <b>Erutulco Eruntano</b><br />" +
 							  "Contact: <a href=\"http://twitter.com/joaquingatica\">@joaquingatica</a> or <a href=\"mailto:erutulco@quenya101.com\">erutulco@quenya101.com</a><br />" +
 							  "&copy; "+Lang.common.app_version_date+"<br />" +
 					  		  "</font><br />" +
 						  	  "<font size=\"2\">Open Source Software hosted at: <a href=\"http://github.com/joaquingatica/Notie-Imberisseo\">github.com/joaquingatica/Notie-Imberisseo</a><br />" +
 						  	  "For reference about the <b>calendar</b>, visit Quenya101 Language Institute:<br />" +
 						  	  "<a href=\"http://www.quenya101.com\">www.quenya101.com</a><br />" +
 						  	  "<br />" +
 						  	  "Special thanks to <b><a href=\"http://twitter.com/Quenya101\">Erunno Alcarinollo</a></b>.<br />" +
 						  	  "Russian translation: <b><a href=\"http://www.facebook.com/tienoren\">Aen Oroniel Tiënoren</a></b>" +
 						  	  "</font>";
 	}
 	
 }
