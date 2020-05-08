 package uk.ac.cam.cl.dtg.teaching;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.hibernate.annotations.GenericGenerator;
 
 @Entity
 @Table(name = "LOG")
 public class RequestLog {
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="logIdSeq") 
	@SequenceGenerator(name="logIdSeq",sequenceName="LOG_ID")
 	private int id;
 	
 	private String crsid;
 	private String url;
 	
 	@Temporal(TemporalType.TIMESTAMP)
 	private Date timestamp;
 	
 	public RequestLog() {}
 	public RequestLog(String crsid, String url) {
 		this.crsid = crsid;
 		this.url = url;
 		this.timestamp = Calendar.getInstance().getTime();
 	}
 	
 	// Setters and getters
 	public int getId() {return id;}
 	
 	public String getCrsid() {return crsid;}
 	public void setCrsid(String crsid) {this.crsid = crsid;}
 	
 	public String getUrl() {return url;}
 	public void setUrl(String url) {this.url = url;}
 
 	public Date getTimestamp() {return timestamp;}
 	public void setTimestamp(Date timestamp) {this.timestamp = timestamp;}
 
 }
