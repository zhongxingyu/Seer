 package socio.rss;
 
import java.net.MalformedURLException;
import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 
 public class ActivityEntry {
 
 	private final static Logger LOGGER = Logger.getLogger(ActivityEntry.class);
 	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,S");
 
	private URL resource;
 	private Date date;
 	private String user;
 
 	public ActivityEntry(String resource, String date, String user) {
 		setResource(resource);
 		setDate(date);
 		setUser(user);
 	}
 
 	public String getResource() {
		return resource.toString();
 	}
 
 	public void setResource(String resource) {
		try {
			this.resource = new URL(resource);
		} catch (MalformedURLException e) {
			LOGGER.warn("Could not parse " + resource + " as URL: ", e);
		}
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(String date) {
 		try {
 			this.date = DATE_FORMAT.parse(date);
 		} catch (Exception e) {
 			LOGGER.warn("Could not parse " + date + " as date: ", e);
 			this.date = new Date(System.currentTimeMillis());
 		}
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 }
