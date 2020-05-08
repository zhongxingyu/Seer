 package ch.cern.atlas.apvs.domain;
 
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.TimeZone;
 
 public interface ClientConstants {
	public static final TimeZone timeZone = TimeZone.createTimeZone("Europe/Zurich");
 	public static final DateTimeFormat simpleDateFormat = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm:ss");
 	public static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd MMM yyyy HH:mm:ss");
 	public static final DateTimeFormat dateFormatNoSeconds = DateTimeFormat.getFormat("dd MMM yyyy HH:mm");
 	public static final DateTimeFormat dateFormatShort = DateTimeFormat.getFormat("dd MMM HH:mm");
 	public static final DateTimeFormat dateFormatOnly = DateTimeFormat.getFormat("dd-MM-yy");
 	public static final DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm:ss");
 	public static final DateTimeFormat timeFormatNoSeconds = DateTimeFormat.getFormat("HH:mm");
 }
