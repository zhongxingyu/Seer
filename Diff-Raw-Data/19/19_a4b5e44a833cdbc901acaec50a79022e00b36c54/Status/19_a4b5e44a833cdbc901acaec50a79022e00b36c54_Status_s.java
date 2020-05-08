 package org.kth.cos.android.sw.data;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Status {
 	private String value;
 	private String profileName;
 	private Date postedAt;
 
 	public Status() {
 	}
 
 	public Status(String value, String profileName, String postedAtStr) throws ParseException {
 		super();
 		this.value = value;
 		this.profileName = profileName;
 		this.postedAt = parseDate(postedAtStr);
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	public String getProfileName() {
 		return profileName;
 	}
 
 	public void setProfileName(String profileName) {
 		this.profileName = profileName;
 	}
 
 	public Date getPostedAt() {
 		return postedAt;
 	}
 
 	public void setPostedAt(Date postedAt) {
 		this.postedAt = postedAt;
 	}
 
 	public Date parseDate(String dateStr) throws ParseException {
 		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateStr);
 	}
 
 }
