 package ch.cern.atlas.apvs.ptu.shared;
 
 import com.google.gwt.i18n.client.DateTimeFormat;
 
 public interface PtuClientConstants {
 	public static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd MMM yyyy HH:mm:ss");
 	public static final DateTimeFormat dateFormatNoSeconds = DateTimeFormat.getFormat("dd MMM yyyy HH:mm");
 	public static final DateTimeFormat dateFormatShort = DateTimeFormat.getFormat("dd MMM HH:mm");
	public static final DateTimeFormat dateFormatOnly = DateTimeFormat.getFormat("dd-MM-yy");
 	public static final DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm:ss");
 	public static final DateTimeFormat timeFormatNoSeconds = DateTimeFormat.getFormat("HH:mm");
 }
