 package com.nbcedu.function.schoolmaster2.data.model;
 
 import java.util.Date;
 
 
 public class TSm2Progress implements java.io.Serializable {
 
 	// Fields
 	private static final long serialVersionUID = 1L;
 	private String id;
 	private String name;
 	private String createrId;
 	private Date createTime;
 	private Date lastUpdateTime;
 	private String stepId;
 
 	// Constructors
 
 	/** default constructor */
 	public TSm2Progress() {
 	}
 
 	/** full constructor */
 	public TSm2Progress(String name, String createrId, Date createTime,
 			Date lastUpdateTime, String stepId) {
 		this.name = name;
 		this.createrId = createrId;
 		this.createTime = createTime;
 		this.lastUpdateTime = lastUpdateTime;
 		this.stepId = stepId;
 	}
 
 	// Property accessors
 
 	public String getId() {
 		return this.id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getCreaterId() {
 		return this.createrId;
 	}
 
 	public void setCreaterId(String createrId) {
 		this.createrId = createrId;
 	}
 
 	public Date getCreateTime() {
 		return this.createTime;
 	}
 
 	public void setCreateTime(Date createTime) {
 		this.createTime = createTime;
 	}
 
 	public Date getLastUpdateTime() {
 		return this.lastUpdateTime;
 	}
 
 	public void setLastUpdateTime(Date lastUpdateTime) {
 		this.lastUpdateTime = lastUpdateTime;
 	}
 
 	public String getStepId() {
 		return this.stepId;
 	}
 
 	public void setStepId(String stepId) {
 		this.stepId = stepId;
 	}
 
 }
