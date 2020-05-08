 package cz.muni.fi.pv243.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToOne;
import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 import cz.muni.fi.pv243.model.validation.ValidEmail;
 import cz.muni.fi.pv243.model.validation.ValidName;
 
 /**
  * Entity implementation class for Entity: User
  * 
  */
 @Entity
@Table(name="\"User\"")
 public class User implements Serializable, org.picketlink.idm.api.User {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 	@NotNull
 	@ValidName
 	private String name;
 	@NotNull
 	@Size(min = 1, max = 80)
 	private String address;
 	@NotNull
 	@Column(unique = true)
 	@ValidEmail
 	private String email;
 	@NotNull
 	@Size(min = 1, max = 250)
 	private String passwordHash;
 	@OneToOne(fetch = FetchType.EAGER, orphanRemoval = true)
 	private ShoppingCart cart;
 	@NotNull
 	@Size(min = 1, max = 50)
 	private String role;
 	private static final long serialVersionUID = 1L;
 
 	public User() {
 		super();
 	}
 
 	public Long getIdentificator() {
 		return this.id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getAddress() {
 		return this.address;
 	}
 
 	public String getRole() {
 		return role;
 	}
 
 	public void setRole(String role) {
 		this.role = role;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public String getEmail() {
 		return this.email;
 	}
 
 	public ShoppingCart getCart() {
 		return cart;
 	}
 
 	public void setCart(ShoppingCart cart) {
 		this.cart = cart;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getPasswordHash() {
 		return this.passwordHash;
 	}
 
 	public void setPasswordHash(String passwordHash) {
 		this.passwordHash = passwordHash;
 	}
 
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		User other = (User) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "User [id=" + id + ", name=" + name + ", address=" + address
 				+ ", email=" + email + "]";
 	}
 
 	@Override
 	public String getKey() {
 		return getId();
 	}
 
 	@Override
 	public String getId() {
 		return email;
 	}
 
 }
