 package calendar.model;
 
 import java.util.Collection;
 
 import javax.persistence.CascadeType;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.tostring.RooToString;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 
 @SuppressWarnings("serial")
 @RooToString
 @RooEntity
 public class SystemUser implements UserDetails {
 
     @Id
     private String username;
 
     private String password;
 
     private Boolean accountNonExpired;
 
     private Boolean accountNonLocked;
 
     private Boolean credentialsNonExpired;
 
     private Boolean enabled;
     
     @OneToMany(cascade={CascadeType.REFRESH}, targetEntity=SystemAuthority.class, fetch= FetchType.EAGER)
     @JoinColumn(name="USER")
     private Collection<GrantedAuthority> authorities;
 
 	@Override
 	public Collection<GrantedAuthority> getAuthorities() {
 		return authorities;
 	}
 
 	@Override
 	public String getPassword() {
 		return password;
 	}
 
 	@Override
 	public String getUsername() {
 		return username;
 	}
 
 	@Override
 	public boolean isAccountNonExpired() {
 		return accountNonExpired;
 	}
 
 	@Override
 	public boolean isAccountNonLocked() {
 		return accountNonLocked;
 	}
 
 	@Override
 	public boolean isCredentialsNonExpired() {
 		return credentialsNonExpired;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public void setAccountNonExpired(Boolean accountNonExpired) {
 		this.accountNonExpired = accountNonExpired;
 	}
 
 	public void setAccountNonLocked(Boolean accountNonLocked) {
 		this.accountNonLocked = accountNonLocked;
 	}
 
 	public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
 		this.credentialsNonExpired = credentialsNonExpired;
 	}
 
 	public void setEnabled(Boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	public void setAuthorities(Collection<GrantedAuthority> authorities) {
 		this.authorities = authorities;
 	}
 }
