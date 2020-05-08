 package com.financial.tools.recorderserver.entity;
 
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "FR_FINANCIAL_RECORD")
 public class FinancialRecord {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private long id;
 
 	@Column(name = "NAME")
 	private String name;
 
 	@Column(name = "TOTAL_FEE")
 	private long totalFee;
 
 	@OneToMany
 	@JoinTable(name = "FR_RECORD_USER", joinColumns = { @JoinColumn(name = "RECORD_ID") }, inverseJoinColumns = { @JoinColumn(name = "USER_ID") })
 	private List<User> userList;
 
 	public FinancialRecord() {
 	}
 
 	public FinancialRecord(long id, String name, long totalFee, List<User> userList) {
 		super();
 		this.id = id;
 		this.name = name;
 		this.totalFee = totalFee;
 		this.userList = userList;
 	}
 
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
 
 	public long getTotalFee() {
 		return totalFee;
 	}
 
 	public void setTotalFee(long totalFee) {
 		this.totalFee = totalFee;
 	}
 
 	public List<User> getUserList() {
 		return userList;
 	}
 
 	public void setUserList(List<User> userList) {
 		this.userList = userList;
 	}
 
 }
