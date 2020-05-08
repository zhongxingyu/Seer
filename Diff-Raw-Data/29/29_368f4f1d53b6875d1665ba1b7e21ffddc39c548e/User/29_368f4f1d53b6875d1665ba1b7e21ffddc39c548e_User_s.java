 package com.github.nagaseyasuhito.fatsia.entity;
 
 import java.sql.Blob;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  * ユーザーエンティティ
  * 
  * @author t.nagase
  */
 @Entity
 public class User {
 
 	/**
 	 * ID。
 	 */
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 
 	/**
 	 * 所属グループ。 必ず1つのグループに所属するためoptional = falseにしている。
 	 */
 	@ManyToOne(optional = false)
 	private Group group;
 
 	/**
 	 * ログインID。
 	 */
 	@Column(nullable = false, unique = true)
 	private String loginId;
 
 	/**
 	 * パスワード。
 	 */
 	@Column(nullable = false)
 	private String password;
 
 	/**
 	 * 生年月日。
 	 */
 	@Column
 	@Temporal(TemporalType.DATE)
 	private Date birthDate;
 
 	@Column
 	private Blob icon;
 
 	public Long getId() {
 		return this.id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Group getGroup() {
 		return this.group;
 	}
 
 	public void setGroup(Group group) {
 		this.group = group;
 	}
 
 	public String getLoginId() {
 		return this.loginId;
 	}
 
 	public void setLoginId(String loginId) {
 		this.loginId = loginId;
 	}
 
 	public String getPassword() {
 		return this.password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public Date getBirthDate() {
 		return this.birthDate;
 	}
 
 	public void setBirthDate(Date birthDate) {
 		this.birthDate = birthDate;
 	}
 
 	public Blob getIcon() {
 		return this.icon;
 	}
 
 	public void setIcon(Blob icon) {
 		this.icon = icon;
 	}
 }
