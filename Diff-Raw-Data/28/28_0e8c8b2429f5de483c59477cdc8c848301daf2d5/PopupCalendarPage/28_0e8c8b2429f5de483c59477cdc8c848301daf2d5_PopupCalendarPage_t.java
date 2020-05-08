 package examples.teeda.web.calendar;
 
import java.sql.Timestamp;
 import java.util.Date;
 
import org.seasar.teeda.extension.annotation.convert.TimestampConverter;
 import org.seasar.teeda.extension.annotation.validator.Required;
 
 public class PopupCalendarPage {
 
 	public Date aaaa;

	@TimestampConverter(pattern="yyyy/MM/dd", target="doShow2")
 	@Required
	public Timestamp bbbb;
 
 	public Class doShow() {
 		return null;
 	}
 
	public Class doShow2() {
		return null;
	}

 	public Class initialize() {
 		return null;
 	}
 
 	public Class prerender() {
 		return null;
 	}
 
 }
