 package org.imirsel.nema.model;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.JoinColumn;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.GenerationTime;
 import org.hibernate.annotations.Proxy;
 
 /**
  * Represents a NEMA DIY job.
  * 
  * @author shirk
  * @since 1.0
  */
 @Entity
 @Table(name="job")
 @Proxy(lazy=false)
 public class Job implements Serializable, Cloneable { 
 
  	/**
 	 * Version of this class.
 	 */
 	private static final long serialVersionUID = 3383935885803288343L;
 	
 	static public enum JobStatus {
       UNKNOWN(-1), SCHEDULED(0), SUBMITTED(1), STARTED(2), FINISHED(3), FAILED(4), ABORTED(5);
 
     private final int code;
 
     private JobStatus(int code) { this.code = code; }
 
     public int getCode() { return code; }
 
     @Override
 	public String toString() {
          String name = null;
          switch (code) {
 
             case -1: {
                name = "Unknown";
                break;
             }
          
             case 0: {
                name = "Scheduled";
                break;
             }
 
             case 1: {
                name = "Submitted";
                break;
             }
 
             case 2: {
                name = "Started";
                break;
             }
 
             case 3: {
                name = "Finished";
                break;
             }
             
             case 4: {
                 name = "Failed";
                 break;
              }
 
             case 5: {
                 name = "Aborted";
                 break;
              }
             
          }
          return name;
       }
 
       static public JobStatus toJobStatus(int code) {
          JobStatus status = null;
          switch (code) {
 
             case -1: {
                status = JobStatus.UNKNOWN;
                break;
             }
 
             case 0: {
                status = JobStatus.SCHEDULED;
                break;
              }
             
             case 1: {
                status = JobStatus.SUBMITTED;
                break;
             }
 
             case 2: {
                status = JobStatus.STARTED;
                break;
             }
 
             case 3: {
                status = JobStatus.FINISHED;
                break;
             }
             
             case 4: {
                status = JobStatus.FAILED;
                break;
              }
             
             case 5: {
                status = JobStatus.ABORTED;
                break;
              }
             
          }
          return status;
       }
    }
 	
 	private Long id;
 	private String token;
 	private String name;
 	private String description;
 	private String host;
 	private Integer port;
 	private Integer execPort;
 	private Date submitTimestamp;
 	private Date startTimestamp;
 	private Date endTimestamp;
 	private Date updateTimestamp;
 	private Integer statusCode = -1;
 	private Long ownerId;
 	private String ownerEmail;
 	private Flow flow;
 	private Integer numTries = 0;
 	private String executionInstanceId;
 	private Set<JobResult> results;
 	
 	@Id
 	@Column(name="id")
     @GeneratedValue(strategy = GenerationType.IDENTITY)
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	@Column(name="description",length=20000000)
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	@Column(name="host")
 	public String getHost() {
 		return host;
 	}
 	public void setHost(String host) {
 		this.host = host;
 	}
 	@Column(name="port")
 	public Integer getPort() {
 		return port;
 	}
 	public void setPort(Integer port) {
 		this.port = port;
 	}
 	@Column(name="submitTimestamp")
 	public Date getSubmitTimestamp() {
 		return submitTimestamp;
 	}
 	public void setSubmitTimestamp(Date submitTimestamp) {
 		this.submitTimestamp = submitTimestamp;
 	}
 	@Column(name="startTimestamp",updatable=false,insertable=false)
 	public Date getStartTimestamp() {
 		return startTimestamp;
 	}
 	public void setStartTimestamp(Date startTimestamp) {
 		this.startTimestamp = startTimestamp;
 	}
 	@Column(name="endTimestamp")
 	public Date getEndTimestamp() {
 		return endTimestamp;
 	}
 	public void setEndTimestamp(Date endTimestamp) {
 		this.endTimestamp = endTimestamp;
 	}
 	@Column(name="statusCode",nullable=false)
 	public Integer getStatusCode() {
 		return statusCode;
 	}
 	
 	public void setStatusCode(Integer statusCode) {
 		// This is a nasty hack to work around the race condition that exists
 		// when the Meandre server updates the job as started, and then the
 		// flow service writes it as submitted.
 		if(this.statusCode==2 && statusCode < 2) {
 			return;
 		}
 		if(!this.statusCode.equals(statusCode)) {
 		  setUpdateTimestamp(new Date());
 		  this.statusCode = statusCode;
 		}
 	}
 	
 	@Column(name="token",nullable=false)
     public String getToken() {
 		return token;
 	}
 	public void setToken(String token) {
 		this.token = token;
 	}
 
 	@Column(name="name",nullable=false)
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	@Column(name="updateTimestamp",updatable=false,insertable=false)
 	@org.hibernate.annotations.Generated(GenerationTime.ALWAYS)
 	public Date getUpdateTimestamp() {
 		return updateTimestamp;
 	}
 	public void setUpdateTimestamp(Date updateTimestamp) {
 		this.updateTimestamp = updateTimestamp;
 	}
 	@Column(name="ownerId", nullable=false)
 	public Long getOwnerId() {
 		return ownerId;
 	}
 	public void setOwnerId(Long ownerId) {
 		this.ownerId = ownerId;
 	}
 	@Column(name="ownerEmail", nullable=false)
 	public String getOwnerEmail() {
 		return ownerEmail;
 	}
 	public void setOwnerEmail(String ownerEmail) {
 		this.ownerEmail = ownerEmail;
 	}
 	@JoinColumn(name = "flowInstanceId")
 	@ManyToOne(fetch=FetchType.EAGER,optional=false)
 	public Flow getFlow() {
 		return flow;
 	}
 	public void setFlow(Flow flow) {
 		this.flow = flow;
 	}
 	@Column(name="executionInstanceId",length=20000000)
 	public String getExecutionInstanceId() {
 		return executionInstanceId;
 	}
 	public void setExecutionInstanceId(String executionInstanceId) {
 		this.executionInstanceId = executionInstanceId;
 	}
 	@Transient
 	public JobStatus getJobStatus() {
 	      return JobStatus.toJobStatus(statusCode);
 	}
 	public void setJobStatus(JobStatus status) {
 		this.statusCode = status.getCode();
 	}
	@JoinColumn(name="jobId")
 	@OneToMany(mappedBy="job", fetch=FetchType.EAGER)
 	public Set<JobResult> getResults() {
 		return results;
 	}
 	public void setResults(Set<JobResult> results) {
 		this.results = results;
 	}
 	@Transient
 	public boolean isRunning() {
 	    return getJobStatus() == JobStatus.STARTED;
 	}
 	@Transient
 	public boolean isDone() {
 		if (statusCode == JobStatus.FINISHED.getCode() || 
 				statusCode == JobStatus.FAILED.getCode() || 
 				statusCode == JobStatus.ABORTED.getCode()) {
 		return true;
 		}
 		return false;
 	}
 	
 	@Column(name="numTries",nullable=false)
 	public Integer getNumTries() {
 		return numTries;
 	}
 	public void setNumTries(Integer numTries) {
 		this.numTries = numTries;
 	}
 	public void incrementNumTries() {
 		numTries++;
 	}
 	@Column(name="execPort")
 	public Integer getExecPort() {
 		return execPort;
 	}
 	public void setExecPort(Integer execPort) {
 		this.execPort = execPort;
 	}
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Job other = (Job) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 	
 	@Override
 	public Job clone() {
 		Job clone = new Job();
 		clone.setId(this.getId());
 		clone.setName(this.getName());
 		clone.setDescription(this.getDescription());
 		clone.setSubmitTimestamp(this.getSubmitTimestamp());
 		clone.setStartTimestamp(this.getStartTimestamp());
 		clone.setEndTimestamp(this.getEndTimestamp());
 		clone.setUpdateTimestamp(this.getUpdateTimestamp());
 		clone.setHost(this.getHost());
 		clone.setPort(this.getPort());
 		clone.setFlow(this.getFlow());
         clone.setExecPort(this.getExecPort());
         clone.setExecutionInstanceId(this.getExecutionInstanceId());
         clone.setStatusCode(this.getStatusCode());
         clone.setNumTries(this.getNumTries());
         clone.setOwnerEmail(this.getOwnerEmail());
         clone.setOwnerId(this.getOwnerId());
         clone.setResults(this.getResults());
         clone.setToken(this.getToken());
         return clone;
 	}
 
 }
