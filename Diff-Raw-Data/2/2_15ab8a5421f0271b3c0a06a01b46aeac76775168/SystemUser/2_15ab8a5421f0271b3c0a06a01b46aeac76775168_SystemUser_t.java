 package cz.muni.fi.pa165.pujcovnastroju.entity;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 /**
  * 
  * @author Vojtech Schlemmer
  */
 
 @Entity
 public class SystemUser implements Serializable {
 	/**
 	 * auto generated serial id
 	 */
 	private static final long serialVersionUID = -2299546650455665467L;
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Long id;
 	private String firstName; 
 	private String lastName;
 	private UserTypeEnum type;
 
 	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.REFRESH, CascadeType.REMOVE }, mappedBy="customer")
 	private List<Loan> loans;
 	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE,
 			CascadeType.REFRESH, CascadeType.REMOVE }, mappedBy="systemUser")
 	private List<Revision> revisions;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public UserTypeEnum getType() {
 		return type;
 	}
 
 	public void setType(UserTypeEnum type) {
 		this.type = type;
 	}
 
 	public List<Loan> getLoans() {
 		return loans;
 	}
 
 	public void setLoans(List<Loan> loans) {
 		this.loans = loans;
 	}
 
 	public List<Revision> getRevisions() {
 		return revisions;
 	}
 
 	public void setRevisions(List<Revision> revisions) {
 		this.revisions = revisions;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = 5;
 		hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);
 		return hash;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		final SystemUser other = (SystemUser) obj;
 		if (this.id != other.id
 				&& (this.id == null || !this.id.equals(other.id))) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "User{" + "id=" + id + ", firstName=" + firstName
 				+ ", lastName=" + lastName + ", type=" + type + '}';
 	}
 
 }
