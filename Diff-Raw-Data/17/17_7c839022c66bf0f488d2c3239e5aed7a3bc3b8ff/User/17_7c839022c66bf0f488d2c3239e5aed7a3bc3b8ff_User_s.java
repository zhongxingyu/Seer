 package com.fauxwerd.model;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
 import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.TableGenerator;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.NaturalId;
 import org.hibernate.annotations.Sort;
 import org.hibernate.annotations.SortType;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.userdetails.UserDetails;
 
 import com.fauxwerd.util.HashCodeUtil;
 
 @Entity
 @Table(name = "user")
 public class User implements UserDetails {
 
 	private static final long serialVersionUID = 1251530774635471804L;
 
 	private Long id;	
 	private Integer version;
 	private String password;		
 	private String email;
 	private boolean enabled;	
 	private SortedSet<UserContent> userContent = new TreeSet<UserContent>();
 	private String fullName;
 	
 	//empty constructor required by hibernate
 	public User() { }
 	
 	public User(String password, String email, boolean enabled, String fullName) {		
 		this.password = password;
 		this.email = email;
 		this.enabled = enabled;		
 		this.fullName = fullName;
 	}	
 
 	@Id 
 	@GeneratedValue(generator = "USER_TABLE_GEN")
 	@TableGenerator(
 			name = "USER_TABLE_GEN",
 			pkColumnValue = "USER",
 			allocationSize = 5
 	)
 	@Column(name = "user_id")	
 	public Long getId() {
 		return id;
 	}
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
     @Version
     @Column(name="optlock")
 	public Integer getVersion() {
 		return version;
 	}
 
 	public void setVersion(Integer version) {
 		this.version = version;
 	}
 	
 	@NotNull
 	@Size(min=1, max=50)
 	public String getPassword() {
 		return password;
 	}
 	
 	public void setPassword(String password) {
 		this.password = password;
 	}
 

	@Pattern(regexp="(\\w+)@(\\w+\\.)(\\w+)(\\.\\w+)*")
 //	TODO re-enable NaturalId (breaks hbm2ddl on mysql)
 //	@NaturalId(mutable = true)
 	@Column(columnDefinition="varchar(256)")	
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 	
 	@NotNull	
 	public boolean isEnabled() {
 		return enabled;
 	}
 		
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	@OneToMany(mappedBy = "user")
 	@Sort(type = SortType.NATURAL)
 	public SortedSet<UserContent> getUserContent() {	
 		return userContent;
 	}
 
 	public void setUserContent(SortedSet<UserContent> userContent) {
 		this.userContent = userContent;
 	}
 	
 	@NotNull	
 	@Size(min=1, max=128)
 	@Column(name="full_name")	
 	public String getFullName() {
 		return fullName;
 	}
 
 	public void setFullName(String fullName) {
 		this.fullName = fullName;
 	}
 
 	//required by interface, returning email address instead
 	@Transient
 	public String getUsername() {
 		return email;
 	}
 	
 	//these booleans are required by UserDetails interface, using enabled state for all
 	@Transient
 	public boolean isAccountNonExpired() {
 		return enabled;
 	}
 
 	@Transient
 	public boolean isAccountNonLocked() {
 		return enabled;
 	}
 
 	@Transient
 	public boolean isCredentialsNonExpired() {
 		return enabled;
 	}
 
 	//currently only one user role so we're always returning ROLE_USER
 	@Transient	
 	public List<GrantedAuthority> getAuthorities() {
 		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
 		authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
 
 		return authorities;
 	}
 	
 	public void fullyEnable() {
 		enabled = true;
 	}
 	
 	public String toString() {
 		return String.format("id = [%s], email = [%s], password = [%s], fullName = [%s]", this.id, this.email, this.password, this.fullName);
 	}
 	
 	public boolean equals(Object other) {
 		if (this == other) return true;
 		if (!(other instanceof User)) return false;
 		
 		final User user = (User)other;
 		if (!user.getEmail().equals( getEmail())) return false;
 		
 		return true;
 	}
 	
 	public int hashCode() {
 		int result = HashCodeUtil.SEED;
 
 		result = HashCodeUtil.hash(result, getEmail());
 		
 		return result;		
 	}
 	
 	
 }
