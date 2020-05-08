 package com.onb.otp.domain;
 
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 
@XmlRootElement(name = "batch_response")
 @Entity
 @Table(name="one_time_password_list_batch")
 public class OneTimePasswordListBatch {
 	@Id
 	@Column(name="id")
 	@GeneratedValue
 	private Long id;
 	
 	@OneToMany(fetch = FetchType.LAZY, mappedBy = "batch")
 	@Cascade({CascadeType.SAVE_UPDATE})
 	private List<OneTimePasswordList> passwordLists;
 	
 	@Column(name="batch_size")
 	private int batchSize;
 	
 	public Long getId() {
 		return id;
 	}
	@XmlTransient
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public List<OneTimePasswordList> getPasswordLists() {
 		return passwordLists;
 	}
	@XmlElement(name="otp-list")
 	public void setPasswordLists(List<OneTimePasswordList> passwordLists) {
 		this.passwordLists = passwordLists;
 	}
 	public int getBatchSize() {
 		return batchSize;
 	}
	@XmlAttribute(name="batch-size")
 	public void setBatchSize(int batchSize) {
 		this.batchSize = batchSize;
 	}
 }
