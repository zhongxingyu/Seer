 package ru.terra.jbrss;
 
 import android.content.Context;
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 public class ProjectModule extends AbstractModule {
 	private Context context;
 
 	@Inject
 	public ProjectModule(Context context) {
 		this.context = context;
 	}
 
 	@Override
 	protected void configure() {
 	}
 
 	@Provides
 	@Named("jbrssServer")
 	public String provideJsonServerName() {
		return "http://terranout.ath.cx/jbrss/";
 	}
 
 	@Provides
 	@Named("dateFormat")
 	@Singleton
 	public DateFormat provideDateFormat() {
 		return new SimpleDateFormat("dd.MM.yyyy");
 	}
 
 	@Provides
 	@Named("exportDateFormat")
 	@Singleton
 	public DateFormat provideExportDateFormat() {
 		return new SimpleDateFormat("dd-MM-yyyy");
 	}
 
 	@Provides
 	@Named("dateFullFormat")
 	@Singleton
 	public DateFormat provideFullDateFormat() {
 		return new SimpleDateFormat("dd MMMMM yyyy");
 	}
 
 	@Provides
 	@Named("timeFormat")
 	@Singleton
 	public DateFormat provideTimeFormat() {
 		return new SimpleDateFormat("HH:mm");
 	}
 
 	@Provides
 	@Named("dateTimeFormat")
 	@Singleton
 	public DateFormat provideDateTimeFormat() {
 		return new SimpleDateFormat("dd.MM.yyyy HH:mm");
 	}
 
 	@Provides
 	@Named("exportDateTimeFormat")
 	@Singleton
 	public DateFormat provideExportDateTimeFormat() {
 		return new SimpleDateFormat("dd-MM-yyyy HH:mm");
 	}
 
 	@Provides
 	@Named("dayOfWeekFormat")
 	@Singleton
 	public DateFormat provideDayOfWeekFormat() {
 		return new SimpleDateFormat("dd MMMMM, EEEEEEE");
 	}
 
 	@Provides
 	@Named("dayOfMonthFormat")
 	@Singleton
 	public DateFormat provideDayOfMonthFormat() {
 		return new SimpleDateFormat("dd MMMMM");
 	}
 }
