 package com.codemonkey.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import org.hibernate.annotations.GenericGenerator;
 import org.hibernate.annotations.Index;
 import org.json.JSONObject;
 
 import com.codemonkey.annotation.Label;
 import com.codemonkey.utils.OgnlUtils;
 
 @MappedSuperclass
 public class AbsEntity implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
     @Id
     @GeneratedValue(generator="pkgenerator") 
     @GenericGenerator(name="pkgenerator", strategy = "increment") 
     @Label("自动编号")
     private Long id;
 	
     @Label("编码")
     @Index(name = "code_index")
     private String code;
     
     @Label("名称")
     @Index(name = "name_index")
 	private String name;
 	
     @Label("描述")
 	private String description;
 	
 	@Version
 	private Integer version;
 	
 	@Transient
 	private Integer originVersion;
 	
 	@Label("创建时间")
 	private Date creationDate;
 	
 	@Label("创建人")
 	private String createdBy;
 	
 	@Label("修改时间")
 	private Date modificationDate;
 	
 	@Label("修改人")
 	private String modifiedBy;
 	
 	
 	JSONObject json() {
 		JSONObject jo = new JSONObject();
 		jo.put("id", OgnlUtils.stringValue("id", this));
 		jo.put("originVersion", OgnlUtils.stringValue("originVersion", this));
		jo.put("code", OgnlUtils.stringValue("code", this));
 		jo.put("name", OgnlUtils.stringValue("name", this));
 		jo.put("description", OgnlUtils.stringValue("description", this));
 		jo.put("creationDate", OgnlUtils.stringValue("creationDate", this));
 		jo.put("creationDate", OgnlUtils.stringValue("creationDate", this));
 		jo.put("createdBy", OgnlUtils.stringValue("createdBy", this));
 		jo.put("modifiedBy", OgnlUtils.stringValue("modifiedBy", this));
 		
 		return jo;
 	}
 	
 	public boolean isOptimisticLockingFailure() {
 		if (getOriginVersion() == null){
 			return false;
 		}
 		return  !getOriginVersion().equals(this.version);
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getDescription() {
 	    return description;
 	}
 	
 	public void setDescription(String description) {
 	    this.description = description;
 	}
 	
 	public Integer getVersion() {
 		return version;
 	}
 
 	public void setVersion(Integer version) {
 		this.version = version;
 	}
 
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	public String getCreatedBy() {
 		return createdBy;
 	}
 
 	public void setCreatedBy(String createdBy) {
 		this.createdBy = createdBy;
 	}
 
 	public Date getModificationDate() {
 		return modificationDate;
 	}
 
 	public void setModificationDate(Date modificationDate) {
 		this.modificationDate = modificationDate;
 	}
 
 	public String getModifiedBy() {
 		return modifiedBy;
 	}
 
 	public void setModifiedBy(String modifiedBy) {
 		this.modifiedBy = modifiedBy;
 	}
 
 	public String getCode() {
 		return code;
 	}
 
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	public Integer getOriginVersion() {
 		if (originVersion == null) {
 			this.originVersion = this.version;
 		}
 		return originVersion;
 	}
 
 	public void setOriginVersion(Integer originVersion) {
 		this.originVersion = originVersion;
 	}
 	
 }
