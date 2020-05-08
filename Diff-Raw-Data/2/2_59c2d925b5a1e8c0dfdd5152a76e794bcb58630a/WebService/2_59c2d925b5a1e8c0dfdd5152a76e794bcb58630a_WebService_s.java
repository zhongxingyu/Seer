 package database.entity;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 @Entity
 public class WebService {
 
 	@Id
 	@GeneratedValue (strategy = GenerationType.IDENTITY)
 	private Long id;
 	
 	@Column (unique = true)
 	private String url;
 	
 	@Temporal (TemporalType.TIMESTAMP)
 	private Date timestamp;
 	
 	@Column(columnDefinition = "MEDIUMTEXT", nullable = false)
 	private String wsdl;
 	
	@Column(columnDefinition = "MEDIUMTEXT", nullable = false)
 	private String generatedWSDL;
 	
 	private String version;
 	
 	@Column(columnDefinition = "MEDIUMTEXT")
 	private String HWinfo;
 	
 	@Column(columnDefinition = "MEDIUMTEXT")
 	private String SWinfo;
 	
 	@OneToMany (mappedBy = "webservice")
 	private List<Log> logs;
 	
 	@OneToMany (fetch = FetchType.EAGER, mappedBy = "webservice", cascade=CascadeType.ALL)
 	private List<Data> data;
 	
 	public WebService() { }
 	
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Date getTimestamp() {
 		return timestamp;
 	}
 
 	public void setTimestamp(Date timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	public String getWsdl() {
 		return wsdl;
 	}
 
 	public void setWsdl(String wsdl) {
 		this.wsdl = wsdl;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	public String getHWinfo() {
 		return HWinfo;
 	}
 
 	public void setHWinfo(String hWinfo) {
 		HWinfo = hWinfo;
 	}
 
 	public String getSWinfo() {
 		return SWinfo;
 	}
 
 	public void setSWinfo(String sWinfo) {
 		SWinfo = sWinfo;
 	}
 	
 	public String getUrl() {
 		return url;
 	}
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public void addLog(Log log) {
 		if (logs == null) {
 			logs = new ArrayList<>();
 		}
 		logs.add(log);
 	}
 	
 	public List<Log> getLogs() {
 		return logs;
 	}
 
 	public void setLogs(List<Log> logs) {
 		this.logs = logs;
 	}
 	
 	public void addData(Data entry) {
 		if (data == null) {
 			data = new ArrayList<>();
 		}
 		data.add(entry);
 	}
 
 	public List<Data> getData() {
 		return data;
 	}
 
 	public void setData(List<Data> data) {
 		this.data = data;
 	}
 
 	public String getGeneratedWSDL() {
 		return generatedWSDL;
 	}
 
 	public void setGeneratedWSDL(String generatedWSDL) {
 		this.generatedWSDL = generatedWSDL;
 	}	
 	
 }
