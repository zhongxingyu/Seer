 package exist.practice;
 
 import java.util.*;
 
 import javax.persistence.*;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.joda.time.*;
 
 @Entity
 public class User {
 
 	@Id
 	@GeneratedValue
 	private long userId = -1;
 	@Column(unique = true)
 	private String userName;
 	private String password;
 	private String lastName;
 	private String firstName;
 	private String mi;
 	private String emailAddress;
 	private String gender;
 
	//@Column(nullable = false)
	@Column(nullable = true)
 	//@Temporal(TemporalType.DATE)
 	private DateTime birthDate;

 	private String homeAddress;
 	private String contactNumber;
 	private boolean isEnabled = true;
 	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
 	@JoinTable(name = "OrgUser", joinColumns = @JoinColumn(name = "userId"), inverseJoinColumns = @JoinColumn(name = "orgId"))
 	private Set<Org> orgs;
 	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
 	@JoinTable(name = "UserRole", joinColumns = @JoinColumn(name = "userId"), inverseJoinColumns = @JoinColumn(name = "roleId"))
 	private Set<Role> roleList;
 	/**
 	 * @return the userId
 	 */
 	public long getUserId() {
 		return userId;
 	}
 
 	/**
 	 * @param userId
 	 *            the userId to set
 	 */
 	public void setUserId(long userId) {
 		this.userId = userId;
 	}
 
 	/**
 	 * @return the userName
 	 */
 	public String getUserName() {
 		return userName;
 	}
 
 	/**
 	 * @param userName
 	 *            the userName to set
 	 */
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 
 	/**
 	 * @return the password
 	 */
 	public String getPassword() {
 		return password;
 	}
 
 	/**
 	 * @param password
 	 *            the password to set
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	/**
 	 * @return the lastName
 	 */
 	public String getLastName() {
 		return lastName;
 	}
 
 	/**
 	 * @param lastName
 	 *            the lastName to set
 	 */
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	/**
 	 * @return the firstName
 	 */
 	public String getFirstName() {
 		return firstName;
 	}
 
 	/**
 	 * @param firstName
 	 *            the firstName to set
 	 */
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	/**
 	 * @return the mi
 	 */
 	public String getMi() {
 		return mi;
 	}
 
 	/**
 	 * @param mi
 	 *            the mi to set
 	 */
 	public void setMi(String mi) {
 		this.mi = mi;
 	}
 
 	/**
 	 * @return the emailAddress
 	 */
 	public String getEmailAddress() {
 		return emailAddress;
 	}
 
 	/**
 	 * @param emailAddress
 	 *            the emailAddress to set
 	 */
 	public void setEmailAddress(String emailAddress) {
 		this.emailAddress = emailAddress;
 	}
 
 	/**
 	 * @return the gender
 	 */
 	public String getGender() {
 		return gender;
 	}
 
 	/**
 	 * @param gender
 	 *            the gender to set
 	 */
 	public void setGender(String gender) {
 		this.gender = gender;
 	}
 
 	/**
 	 * @return the birthDate
 	 */
 	public DateTime getBirthDate() {
 		return birthDate;
 	}
 
 	/**
 	 * @param birthDate
 	 *            the birthDate to set
 	 */
 	public void setBirthDate(String birthDate) {
 		this.birthDate = new DateTime(birthDate);
 	}
 
 	/**
 	 * @return the homeAddress
 	 */
 	public String getHomeAddress() {
 		return homeAddress;
 	}
 
 	/**
 	 * @param homeAddress
 	 *            the homeAddress to set
 	 */
 	public void setHomeAddress(String homeAddress) {
 		this.homeAddress = homeAddress;
 	}
 
 	/**
 	 * @return the contactNumber
 	 */
 	public String getContactNumber() {
 		return contactNumber;
 	}
 
 	/**
 	 * @param contactNumber
 	 *            the contactNumber to set
 	 */
 	public void setContactNumber(String contactNumber) {
 		this.contactNumber = contactNumber;
 	}
 
 	public void setOrgs(Set<Org> orgs) {
 		this.orgs = orgs;
 	}
 
 	public Set<Org> getOrgs() {
 		return orgs;
 	}
 
 	public void setEnabled(boolean isEnabled) {
 		this.isEnabled = isEnabled;
 	}
 
 	public boolean isEnabled() {
 		return isEnabled;
 	}
 
 	public void setRoleList(Set<Role> roleList) {
 		this.roleList = roleList;
 	}
 
 	@JsonIgnore
 	public Set<Role> getRoleList() {
 		return roleList;
 	}
 
 }
