 package jipdbs.bean;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import org.datanucleus.util.StringUtils;
 
 public class BanInfo implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6801850365975055431L;
 
 	private Date created;
 	private String reason;
 	private Long duration;
 	private String type;
 	
 	public BanInfo() {
 	}
 	
 	public BanInfo(String data) {
 		String[] parts = StringUtils.split(data, "::");
 		setType(parts[0]);
 		setCreated(new Date(Long.parseLong(parts[1]) * 1000L));
 		setDuration(Long.parseLong(parts[2]));
		setReason(parts[3]);
 	}
 	
 	public Date getCreated() {
 		return created;
 	}
 	public void setCreated(Date created) {
 		this.created = created;
 	}
 	public String getReason() {
 		return reason;
 	}
 	public void setReason(String reason) {
 		this.reason = reason;
 	}
 	public Long getDuration() {
 		return duration;
 	}
 	public void setDuration(Long duration) {
 		this.duration = duration;
 	}
 	public String getType() {
 		return type;
 	}
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public Date getExpires() {
 		Calendar calendar = new GregorianCalendar();
 		calendar.setTime(this.created);
 		calendar.add(Calendar.MINUTE, this.getDuration().intValue());
 		return calendar.getTime();
 	}
 	
 	@Override
 	public String toString() {
 		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
 		format.setTimeZone(TimeZone.getTimeZone("GMT-3"));
 		DateFormat format2 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
 		format2.setTimeZone(TimeZone.getTimeZone("GMT-3"));
 		String s = "Baneado el " + format.format(this.getCreated()) + " por " + this.getReason();
 		if ("tb".equals(this.getType())) {
 			s = s + " hasta el " + format2.format(this.getExpires());
 		}
 		return s;
 	}
 	
 }
