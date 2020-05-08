 package com.leopin.parkfifty.shared.domain;
 
 import static com.leopin.parkfifty.shared.AppRegExp.EMAIL;
 import static com.leopin.parkfifty.shared.AppRegExp.EMPTY_STRING;
 import static com.leopin.parkfifty.shared.AppRegExp.MIDDLEINITIAL;
 import static com.leopin.parkfifty.shared.AppRegExp.NAME;
 import static com.leopin.parkfifty.shared.AppRegExp.PASSWORD;
 import static com.leopin.parkfifty.shared.AppRegExp.PHONE_NUM;
 import static com.leopin.parkfifty.shared.AppRegExp.SUFFIX;
 import static com.leopin.parkfifty.shared.AppRegExp.TITLE;
 import static com.leopin.parkfifty.shared.AppRegExp.USER_ID;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Id;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.map.annotate.JsonDeserialize;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 
 import com.google.common.base.Objects;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Indexed;
 import com.googlecode.objectify.annotation.Parent;
 import com.googlecode.objectify.annotation.Serialized;
 import com.googlecode.objectify.annotation.Unindexed;
 import com.leopin.parkfifty.shared.utils.Utils;
 
 /**
  * This domain object defines the Company User information
  * @author Glenn Pinto
  */
 @Entity
 @Unindexed
 public class CompanyUser {
 
 	public CompanyUser() {
 		this.timestamp = new Date();
 		this.entitlements = new ArrayList<Entitlements>();
 	}
 	
 	@Id
 	private Long id;
 	
 	private Long companyId;
 	
 	/**
 	 * Reference to the Company Entity
 	 */
 	@JsonIgnore
 	@Parent
 	private Key<Company> companyKey;
 	
 	@Indexed
 	@NotNull(message="{com.leopin.contraints.userid.invalid}")
 	@Pattern(regexp=USER_ID, message="{com.leopin.contraints.userid.invalid}")
 	private String userId;
 
 	@NotNull(message="{com.leopin.contraints.password.invalid}")
 	@Pattern(regexp=PASSWORD, message="{com.leopin.contraints.password.invalid}")
 	private String password;
 	
 	/**
 	 * OWNER(1) - Owner, SUPER_ADMIN(2) - Super Admin, ADMIN(3)- Admin, USER(4) - User
 	 */
 //	@NotNull(message="{com.leopin.contraints.role.invalid}")
 //	@Pattern(regexp=ROLE, message="{com.leopin.contraints.role.invalid}")
 	private Roles role;
 	
 	/**
 	 * Applicable only for Admin and User's
 	 */
 	
 	@Serialized private List<Entitlements> entitlements;
 
 	@NotNull(message="{com.leopin.contraints.title.invalid}")
 	@Pattern(regexp=TITLE, message="{com.leopin.contraints.title.invalid}")	
 	private String title;
 	
 	@NotNull(message="{com.leopin.contraints.firstname.invalid}")
 	@Pattern(regexp=NAME, message="{com.leopin.contraints.firstname.invalid}")		
 	private String firstName;
 
 	@NotNull(message="{com.leopin.contraints.lastname.invalid}")
 	@Pattern(regexp=NAME, message="{com.leopin.contraints.lastname.invalid}")		
 	private String lastName;
 	
 	@NotNull(message="{com.leopin.contraints.middleinitial.invalid}")
 	@Pattern(regexp=MIDDLEINITIAL, message="{com.leopin.contraints.middleinitial.invalid}")			
 	private String middleInitial;
 
 	@NotNull(message="{com.leopin.contraints.suffix.invalid}")
 	@Pattern(regexp=SUFFIX + "|" + EMPTY_STRING, message="{com.leopin.contraints.suffix.invalid}")			
 	private String suffix;
 
 	@NotNull(message="{com.leopin.contraints.primary.phone.invalid}")
 	@Pattern(regexp=PHONE_NUM, message="{com.leopin.contraints.primary.phone.invalid}")
 	private String priPhone;
 	
 	@NotNull(message="{com.leopin.contraints.secondary.phone.invalid}")
 	@Pattern(regexp=PHONE_NUM + "|" + EMPTY_STRING, message="{com.leopin.contraints.secondary.phone.invalid}")
 	private String secPhone;
 	
 	@NotNull(message="{com.leopin.contraints.fax.invalid}")
 	@Pattern(regexp=PHONE_NUM + "|" + EMPTY_STRING, message="{com.leopin.contraints.fax.invalid}")
 	private String fax;
 
 	/**
 	 * Is the user still active or disabled
 	 */
 	private boolean active;
 	
 	/**
 	 * Is the user an approved user by the owner
 	 * Default is always false for user created by any other user except for the owner
 	 */
 	private boolean approved;
 	
 	@NotNull(message="{com.leopin.contraints.email.invalid}")
 	@Pattern(regexp=EMAIL, message="{com.leopin.contraints.email.invalid}")
 	private String email;
 	
 	@NotNull(message="{com.leopin.contraints.timestamp.invalid}")
 	private Date timestamp;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		id = id;
 	}
 
 
 	public String getUserId() {
 		return userId;
 	}
 
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public Roles getRole() {
 		return role;
 	}
 
 	public void setRole(Roles role) {
 		this.role = role;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
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
 
 	public String getMiddleInitial() {
 		return middleInitial;
 	}
 
 	public void setMiddleInitial(String middleInitial) {
 		this.middleInitial = middleInitial;
 	}
 
 	public String getSuffix() {
 		return suffix;
 	}
 
 	public void setSuffix(String suffix) {
 		this.suffix = suffix;
 	}
 
 	public String getPriPhone() {
 		return priPhone;
 	}
 
 	public void setPriPhone(String priPhone) {
 		this.priPhone = Utils.scrubPhoneNum(priPhone);
 	}
 
 	public String getSecPhone() {
 		return secPhone;
 	}
 
 	public void setSecPhone(String secPhone) {
 		this.secPhone = Utils.scrubPhoneNum(secPhone);
 	}
 
 	public String getFax() {
 		return fax;
 	}
 
 	public void setFax(String fax) {
 		this.fax = Utils.scrubPhoneNum(fax);
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public Date getTimestamp() {
 		return timestamp;
 	}
 
 	public void setTimestamp(Date timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	@JsonIgnore
	public Key<Company> getCompanyKey() {
 		if (companyKey == null) {
 			this.companyKey = new Key<Company>(Company.class, companyId);
 		}
 		return companyKey;
 	}
 
	public void setCompanyKey(Key<Company> companyKey) {
 		this.companyKey = companyKey;
 	}
 
 	@JsonDeserialize(contentAs=Entitlements.class)
 	public void setEntitlements(List<Entitlements> entitlements) {
 		this.entitlements = entitlements;
 	}
 	
	public void setEntitlements(Entitlements entitlement) {
 		this.entitlements.add(entitlement);
 	}
 	
 	public List<Entitlements> getEntitlements() {
 		return entitlements;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 
 	public boolean isApproved() {
 		return approved;
 	}
 
 	public void setApproved(boolean approved) {
 		this.approved = approved;
 	}
 	
 	public Long getCompanyId() {
 		return companyId;
 	}
 	
 	@JsonIgnore
 	public Long getCompanyIdFromKey() {
 		return companyKey.getId();
 	}
 
 	public void setCompanyId(Long companyId) {
 		this.companyId = companyId;
 	}
 	
 	public void setCompanyKey() {
 		if (this.companyId > 0L) {
 			this.companyKey = new Key<Company>(Company.class, this.companyId);
 		}
 	}
 
 	
 	@Override
 	public boolean equals(Object object) {
 		
 		if (object instanceof CompanyUser) {
 			CompanyUser that = (CompanyUser) object;
 			return Objects.equal(this.userId, that.companyId);
 		}
 		
 		return false;
 		
 	}
 	
 	@Override
 	public int hashCode() {
 		return Objects.hashCode(this.userId, this.companyId);
 	}
 	
 	@Override
 	public String toString() {
 		return Objects.toStringHelper(this)
 				.add("Id", this.id)
 				.add("Company Id", this.companyId)				
 				.add("User Id", this.userId)
 				.add("Role", this.role)
 				.add("Title", this.title)
 				.add("First Name", this.firstName)
 				.add("Middle Initial", this.middleInitial)
 				.add("Last Name", this.lastName)
 				.add("Suffix", this.suffix)
 				.add("Pri Phone", this.priPhone)
 				.add("Sec Phone", this.secPhone)
 				.add("Fax", this.fax)
 				.add("Email", this.email)
 				.toString();
 	}	
 	
 	
 }
