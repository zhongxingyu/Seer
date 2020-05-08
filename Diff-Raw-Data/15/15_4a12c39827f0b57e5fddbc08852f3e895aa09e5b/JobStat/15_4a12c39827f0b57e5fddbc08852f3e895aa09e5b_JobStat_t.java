 package grisu.backend.model.job;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.simpleframework.xml.Root;
 
 @Entity
 @Table
 public class JobStat {
 
 	// for hibernate
 	private Long id;
 
 	private Long jobHibernateId;
 
 	private String jobname;
 
 	private String dn;
 
 	private String jsdl;
 
 	private String fqan;
 
 	private boolean active = true;
 
 	private String submittedJobDescription;
 
 	private int status = -1000;
 
 	private String submissionType = null;
 
 	private Map<String, String> properties = Collections
 			.synchronizedMap(new HashMap<String, String>());
 
 	private boolean isBatchJob = false;
 
 
 	private Map<Long, String> logMessages = Collections
 			.synchronizedMap(new TreeMap<Long, String>());
 
 	public String getDn() {
 		return dn;
 	}
 
 	public String getFqan() {
 		return fqan;
 	}
 
 	// hibernate
 	@Id
 	@GeneratedValue
 	private Long getId() {
 		return id;
 	}
 
 	@Column(nullable = false)
 	public Long getJobHibernateId() {
 		return jobHibernateId;
 	}
 
 	public String getJobname() {
 		return jobname;
 	}
 
 	@Column(length = 15000)
 	public String getJsdl() {
 		return jsdl;
 	}
 
	@ElementCollection(fetch = FetchType.LAZY)
 	public Map<Long, String> getLogMessages() {
 		return logMessages;
 	}
 
	@ElementCollection(fetch = FetchType.LAZY)
 	@Column(length = 1000)
 	public Map<String, String> getProperties() {
 		return properties;
 	}
 
 	public int getStatus() {
 		return status;
 	}
 
 	public String getSubmissionType() {
 		return submissionType;
 	}
 
 	/**
 	 * Returns the (JobSubmitter-specific) job description (like rsl for gt4).
 	 * 
 	 * @return the job description or null if the job was not submitted yet
 	 */
 	@Column(length = 2550)
 	public String getSubmittedJobDescription() {
 		return submittedJobDescription;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public boolean isBatchJob() {
 		return isBatchJob;
 	}
 
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 
 	public void setBatchJob(boolean isBatchJob) {
 		this.isBatchJob = isBatchJob;
 	}
 
 	public void setDn(String dn) {
 		this.dn = dn;
 	}
 
 	public void setFqan(String fqan) {
 		this.fqan = fqan;
 	}
 
 	// for hibernate
 	private void setId(Long id) {
 		this.id = id;
 	}
 
 	public void setJobHibernateId(Long jobHibernateId) {
 		this.jobHibernateId = jobHibernateId;
 	}
 
 	public void setJobname(String jobname) {
 		this.jobname = jobname;
 	}
 
 	public void setJsdl(final String jsdl_string) {
 		this.jsdl = jsdl_string;
 	}
 
 	public void setLogMessages(Map<Long, String> logMessages) {
 		this.logMessages = logMessages;
 	}
 
 	public void setProperties(Map<String, String> properties) {
 		this.properties = properties;
 	}
 
 	public void setStatus(int status) {
 		this.status = status;
 	}
 
 	public void setSubmissionType(String submissionType) {
 		this.submissionType = submissionType;
 	}
 
 	public void setSubmittedJobDescription(final String desc) {
 		this.submittedJobDescription = desc;
 	}
 
 }
