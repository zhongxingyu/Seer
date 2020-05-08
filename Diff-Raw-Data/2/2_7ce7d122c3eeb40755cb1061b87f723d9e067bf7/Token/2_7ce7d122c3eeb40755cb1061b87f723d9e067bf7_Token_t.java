 package br.com.findplaces.jpa.entity;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 @NamedQueries({
	@NamedQuery(name=Token.FIND_TOKEN, query="SELECT t FROM Token t where t.token = :token and t.user.id = :userID")
 })
 @Entity
 @Table(name="TB_TOKEN")
 public class Token extends BaseEntity {
 
 	private static final long serialVersionUID = 8913221694909232985L;
 	
 	public static final String FIND_TOKEN = "findToken";
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	private Long id;
 	
 	@OneToOne
 	private User user;
 	
 	@Column
 	private String token;
 	
 	@Column
 	private Date valid;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public String getToken() {
 		return token;
 	}
 
 	public void setToken(String token) {
 		this.token = token;
 	}
 
 	public Date getValid() {
 		return valid;
 	}
 
 	public void setValid(Date valid) {
 		this.valid = valid;
 	}
 
 }
