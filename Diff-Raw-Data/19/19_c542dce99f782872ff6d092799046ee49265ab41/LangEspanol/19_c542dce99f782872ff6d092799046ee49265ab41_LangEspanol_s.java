 package lang.available;
 
 import lang.Lang;
 
 public class LangEspanol extends Lang {
 	
 	private String name = "Español";
 	private String arrayName = "Espanol";
 	private String shortName = "esp";
 	
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
 	
 	public LangEspanol() {
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
 		Lang.common.current_date_label = "Fecha actual";
 		Lang.common.location_label = "Ubic.";
 		Lang.common.timezone_label = "ZH";
 		Lang.common.gregorian_label = "Gregoriano";
 		Lang.common.imladris_label = "Imladris";
 		/* To Imladris tab */
 		Lang.to_imladris_tab.title = "A fecha de Imladris";
 		Lang.to_imladris_tab.tooltip = "Convertir del calendario Gregoriano al de Imladris";
		Lang.to_imladris_tab.choose_label = "Elije fecha Gregoriana";
 		Lang.to_imladris_tab.resulting_label = "Fecha de Imladris resultante";
 		Lang.to_imladris_tab.year_label = "Año";
 		Lang.to_imladris_tab.year_tooltip = "Años válidos: 1 - 2299";
 		Lang.to_imladris_tab.month_label = "Mes";
 		Lang.to_imladris_tab.day_label = "Día";
 		Lang.to_imladris_tab.after_sunset_label = "Después del ocaso";
 		Lang.to_imladris_tab.resulting_format = "Formato: Dia de semana, Período [#dia], Yén Loa";
 		/* From Imladris tab */
 		Lang.from_imladris_tab.title = "Desde fecha de Imladris";
 		Lang.from_imladris_tab.tooltip = "Convertir del calendario de Imladris al Gregoriano";
		Lang.from_imladris_tab.choose_label = "Elije fecha de Imladris";
 		Lang.from_imladris_tab.resulting_label = "Fecha Gregoriana resultante";
 		Lang.from_imladris_tab.yen_label = "Yén";
 		Lang.from_imladris_tab.loa_label = "Loa";
 		Lang.from_imladris_tab.loa_tooltip = "Loar válidos: 1 - 144";
 		Lang.from_imladris_tab.period_label = "Período";
 		Lang.from_imladris_tab.day_label = "Día";
 		Lang.from_imladris_tab.before_midnight_label = "Antes de medianoche";
 		Lang.from_imladris_tab.resulting_format = "Formato: Dia de semana, Mes Día, Año";
 		/* Settings tab */
 		Lang.settings_tab.title = "Configuración";
 		Lang.settings_tab.tooltip = "Editar preferencias";
 		Lang.settings_tab.location = "Ubicación";
 		Lang.settings_tab.city_label = "Ciudad";
 		Lang.settings_tab.country_label = "País";
 		Lang.settings_tab.language_label = "Idioma";
 		Lang.settings_tab.timezone = "Zona horaria";
 		Lang.settings_tab.save = "Guardar";
 		/* About tab */
 		Lang.about_tab.title = "Información";
 		Lang.about_tab.tooltip = "Sobre la application y el autor";
 		Lang.about_tab.app_name = "Notië Imberissëo";
 		Lang.about_tab.tengwar_name = "5^1T`V `Bw$7T,R`H";
 		Lang.about_tab.info = "<font size=\"3\">Por <b>Erutulco Eruntano</b><br />" +
 							  "Contacto: <a href=\"http://twitter.com/joaquingatica\">@joaquingatica</a> o <a href=\"mailto:erutulco@quenya101.com\">erutulco@quenya101.com</a><br />" +
 							  "&copy; "+Lang.common.app_version_date+"<br />" +
 						  	  "</font><br />" +
 						  	  "<font size=\"2\">Software Open Source alojado en: <a href=\"http://github.com/joaquingatica/Notie-Imberisseo\">github.com/joaquingatica/Notie-Imberisseo</a><br />" +
 						  	  "Por información sobre el <b>calendario</b>, visita Quenya101 Language Institute:<br />" +
 						  	  "<a href=\"http://www.quenya101.com\">www.quenya101.com</a><br />" +
 						  	  "<br />" +
 						  	  "Agradecimientos en especial a <b>Erunno Alcarinollo</b>.<br />" +
 						  	  "Traducción al Ruso: <b><a href=\"http://www.facebook.com/tienoren\">Aen Oroniel Tiënoren</a></b>" +
 						  	  "</font>";
 	}
 	
 }
