 package com.zarcode.data.model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.IdentityType;
 import javax.jdo.annotations.NotPersistent;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import com.google.appengine.api.datastore.Text;
 import com.zarcode.app.AppCommon;
 import com.zarcode.platform.model.AbstractLoaderDO;
 
 @XmlRootElement(name = "Report") 
 @PersistenceCapable(identityType = IdentityType.APPLICATION)
 public class ReportDO extends AbstractLoaderDO implements Serializable {
 
 	@NotPersistent
 	private String reportBody = null;
 	
 	@NotPersistent
 	private String timeDisplay = null;
 	
 	@PrimaryKey 
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Long reportId;
 	
 	@Persistent
 	private Date reportDate = null;
 	
 	@Persistent
 	private String state = null;
 
 	@Persistent
 	private String keyword = null;
 	
 	@Persistent
 	private String reportKey = null;
 	
 	@Persistent
 	private Text reportBodyText = null;
 	
 	@Persistent
 	private String reportedBy = null;
 	
 	@Persistent
 	private Date lastUpdated = null;
 	
 	public void postCreation() {
 		reportBodyText = new Text(reportBody);
 		lastUpdated = new Date();
 	}
 	
 	public void postReturn() {
 		if (reportBodyText != null) {
 			this.reportBody = reportBodyText.getValue();
 		}
 		timeDisplay = AppCommon.generateTimeOffset(reportDate);
 	}
 	
 	public String toString() {
 		String str = "ReportDO::" + reportId + "\n" +
 			"	keyword=" + keyword + "\n" +
 			"	reportBody=" + reportBodyText.toString() + "\n" +
 			"	reportDate=" + reportDate + "\n" +
 			"	state=" + state + "\n";
 		return str;
 	}
 	
 	
 	public void preparePersistent() {
 		reportBodyText = new Text(reportBody);
 	}
 	
 	@XmlElement
 	public Long getReportId() {
 		return reportId;
 	}
 
 	public void setReportId(Long reportId) {
 		this.reportId = reportId;
 	}
 	
 	@XmlElement
 	public Date getReportDate() {
 		return reportDate;
 	}
 
 	public void setReportDate(Date reportDate) {
 		this.reportDate = reportDate;
 	}
 	
 	@XmlElement
 	public String getTimeDisplay() {
 		return timeDisplay;
 	}
 
 	public void setTimeDisplay(String timeDisplay) {
 		this.timeDisplay = timeDisplay;
 	}
 	
 	@XmlElement
 	public String getKeyword() {
 		return keyword;
 	}
 
 	public void setKeyword(String keyword) {
 		this.keyword = keyword;
 	}
 	
 	@XmlElement
 	public String getReportKey() {
 		return reportKey;
 	}
 
 	public void setReportKey(String reportKey) {
 		this.reportKey = reportKey;
 	}
 	
 	@XmlElement
 	public String getReportBody() {
		return (reportBodyText != null ? reportBodyText.getValue() : null);
 	}
 
 	public void setReportBody(String reportBody) {
 		this.reportBodyText = new Text(reportBody);
 	}
 
 	@XmlElement
 	public String getState() {
 		return state;
 	}
 
 	public void setState(String state) {
 		this.state = state;
 	}
 	
 	@XmlElement
 	public String getReportedBy() {
 		return reportedBy;
 	}
 
 	public void setReportedBy(String reportedBy) {
 		this.reportedBy = reportedBy;
 	}
 	
     @XmlElement
 	public Date getLastUpdated() {
 		return lastUpdated;
 	}
 
 	public void setLastUpdated(Date lastUpdated) {
 		this.lastUpdated = lastUpdated;
 	}
 
 }
