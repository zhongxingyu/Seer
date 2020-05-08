 package org.calculator.models;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  * JPA entity to encapsulate the web http param in the WEB calculation process
  * 
  * @author khalil.amdouni
  * 
  */
 @Entity
 @Table(name = "WEB_PARAMS")
 @NamedQueries({
 		@NamedQuery(name = "WebParam.getParamsByRequestId", query = "SELECT wp FROM WebParam wp WHERE webRequest.id=:requestId"),
 		@NamedQuery(name = "WebParam.getParamsCount", query = "SELECT count(wp) FROM WebParam wp WHERE webRequest.id=:requestId") })
 public class WebParam extends AbstractModel {
 
 	@Id
 	@Column(name = "ID")
 	private long id;
 
 	@Column(name = "NAME")
 	private String name;
 
 	@Column(name = "value")
 	private String value;
 
 	@JsonIgnore
 	@ManyToOne(fetch = FetchType.EAGER)
 	@JoinColumn(name = "REQUEST_ID", nullable = false, updatable = false)
 	private WebRequest webRequest;
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 
 	public WebRequest getWebRequest() {
 		return webRequest;
 	}
 
 	public void setWebRequest(WebRequest webRequest) {
 		this.webRequest = webRequest;
 	}
 
 	public void setRequestId(long requestId) {
 		this.webRequest = new WebRequest();
 		this.webRequest.setId(requestId);
 	}
 	
 	@Override
 	public String toString() {
 		return this.name + "=" + this.value;
 	}
 
 }
