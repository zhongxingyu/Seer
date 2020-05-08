 package ee.itcollege.borderproject.model;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
import org.hibernate.validator.constraints.Length;
 import org.hibernate.validator.constraints.Range;
 
 import ee.itcollege.borderproject.common.BaseEntity;
 
 @Entity
 @Table(name = ("Piirivalvur"))
 @NamedQueries({ 
 	 @NamedQuery(name = "Guard.findAll", 
 	    query = "SELECT g FROM Guard g WHERE g.removed IS null")})
 public class Guard extends BaseEntity implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Column(name = "aadress")
 	@NotNull
 	@Size(min = 4, max = 255)
 	private String address;
 	
 	@Column(name = "eesnimi")
 	@NotNull
 	@Size(min = 2, max = 255)
 	private String firstName;
 	
 	@Column(name = "perekonnanimi")
 	@NotNull
 	@Size(min = 2, max = 255)
 	private String lastName;
 	
 	@NotNull
 	@Size(min = 4, max = 255)
 	private String email;
 	
 	@Column(name = "isikukood")
 	@NotNull
 	@Size(min = 6, max = 20)
 	private String socialSecurityNumber;
 	
 	@Column(name = "sodurikood")
 	@NotNull
 	@Size(min = 4, max = 50)
 	private String soldiersCode;
 	
 	@Column(name = "sugu")
 	@Range(min = 0, max = 1)
 	private int gender;
 	
 	@Column(name = "telefon")
 	@NotNull
 	@Size(min = 2, max = 255)
 	private String phoneNumber;
 	
 	@OneToMany(mappedBy = "guard")
 	private List<GuardInBorderStation> guardInBorderStation;
 
 	public String getAddress() {
 		return address;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public String getSocialSecurityNumber() {
 		return socialSecurityNumber;
 	}
 
 	public String getSoldiersCode() {
 		return soldiersCode;
 	}
 
 	public int getGender() {
 		return gender;
 	}
 
 	public String getPhoneNumber() {
 		return phoneNumber;
 	}
 	
 	public List<GuardInBorderStation> getGuardInBorderStation() {
 		return guardInBorderStation;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public void setSocialSecurityNumber(String socialSecurityNumber) {
 		this.socialSecurityNumber = socialSecurityNumber;
 	}
 
 	public void setSoldiersCode(String soldiersCode) {
 		this.soldiersCode = soldiersCode;
 	}
 
 	public void setGender(int gender) {
 		this.gender = gender;
 	}
 
 	public void setPhoneNumber(String phoneNumber) {
 		this.phoneNumber = phoneNumber;
 	}
 	
 	public void setGuardInBorderStation(
 			List<GuardInBorderStation> guardInBorderStation) {
 		this.guardInBorderStation = guardInBorderStation;
 	}
 }
