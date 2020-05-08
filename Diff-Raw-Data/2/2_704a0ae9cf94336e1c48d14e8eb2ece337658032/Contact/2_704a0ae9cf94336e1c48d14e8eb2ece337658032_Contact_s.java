 package au.org.scoutmaster.domain;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.PreRemove;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.validation.constraints.AssertTrue;
 import javax.validation.constraints.Past;
 
 import org.apache.log4j.Logger;
 import org.hibernate.validator.constraints.Email;
 import org.hibernate.validator.constraints.NotBlank;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.joda.time.format.DateTimeParser;
 
 import au.com.vaadinutils.crud.CrudEntity;
 import au.org.scoutmaster.dao.ContactDao;
 import au.org.scoutmaster.dao.DaoFactory;
 import au.org.scoutmaster.domain.validation.MemberChecks;
 
 @Entity(name = "Contact")
 @Table(name = "Contact")
 @Access(AccessType.FIELD)
 @NamedQueries(
 { @NamedQuery(name = Contact.FIND_BY_NAME, query = "SELECT contact FROM Contact contact WHERE contact.lastname like :lastname and contact.firstname like :firstname") })
 public class Contact extends BaseEntity implements Importable, CrudEntity
 {
 	static public final String FIND_BY_NAME = "Contact.findByName";
 
 	private static final long serialVersionUID = 1L;
 
 	@SuppressWarnings("unused")
 	static private Logger logger = Logger.getLogger(Contact.class);
 
 	public static final String PRIMARY_PHONE = "primaryPhone";
 
 	@FormField(displayName = "Active")
 	private Boolean active = true;
 
 	@FormField(displayName = "Prefix")
 	private String prefix = "";
 
 	@NotBlank
 	@javax.validation.constraints.Size(min = 1, max = 255)
 	@FormField(displayName = "Firstname")
 	private String firstname = "";
 
 	@FormField(displayName = "Middle Name")
 	private String middlename = "";
 
 	@NotBlank
 	@javax.validation.constraints.Size(min = 1, max = 255)
 	@FormField(displayName = "Lastname")
 	private String lastname = "";
 
 	/**
 	 * This is an amalgum of the firstname and lastname i.e firstname + " " + lastname
 	 * This is redundant but it makes it easier to create lists which are sorted by the full name
 	 * as the metamodel doesn't expose transient fields so we can filter or sort on a transient
 	 * field in many scenarios.
 	 */
 	private String fullname = "";
 
 	@FormField(displayName = "Birth Date")
 	@Past
 	private Date birthDate;
 
 	@FormField(displayName = "Gender")
 	private Gender gender = Gender.Male;
 
 	@Transient
 	private Age age;
 
 	@Transient
 	private Phone primaryPhone;
 
 	/**
 	 * Contact fields
 	 */
 
 	@FormField(displayName = "Phone 1")
 	@OneToOne(targetEntity = Phone.class, cascade = CascadeType.PERSIST)
 	@JoinColumn(name = "PHONE1_ID")
 	private Phone phone1 = new Phone();
 
 	@FormField(displayName = "Phone 2")
 	@OneToOne(targetEntity = Phone.class, cascade = CascadeType.PERSIST)
 	@JoinColumn(name = "PHONE2_ID")
 	private Phone phone2 = new Phone();
 
 	@FormField(displayName = "Phone 3")
 	@OneToOne(targetEntity = Phone.class, cascade = CascadeType.PERSIST)
 	@JoinColumn(name = "PHONE3_ID")
 	private Phone phone3 = new Phone();
 
 	@FormField(displayName = "Home Email")
 	@Email
 	private String homeEmail = "";
 
 	@FormField(displayName = "Work Email")
 	@Email
 	private String workEmail = "";
 
 	@FormField(displayName = "Preferred Email")
 	private PreferredEmail preferredEmail = PreferredEmail.HOME;
 
 	@FormField(displayName = "Preferred Communications")
 	private PreferredCommunications preferredCommunications = PreferredCommunications.EMAIL_SMS;
 
 	/**
 	 * Youth fields
 	 */
 	@FormField(displayName = "Allergies")
 	private String allergies = "";
 
 	@FormField(displayName = "Custody Order")
 	private Boolean custodyOrder = false;
 
 	@FormField(displayName = "Custody Order Details")
 	private String custodyOrderDetails = "";
 
 	@FormField(displayName = "School")
 	private String school = "";
 
 	@FormField(displayName = "Section Eligibility")
 	@Transient
 	private SectionType sectionEligibility;
 
 	@OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER, orphanRemoval = true, targetEntity = Address.class)
 	@FormField(displayName = "Address")
 	private Address address = new Address();
 
 	/**
 	 * Member fields
 	 */
 	@FormField(displayName = "Member")
 	@AssertTrue(groups = MemberChecks.class)
 	private Boolean isMember = false; // this should be derived from the
 										// member
 	// records.
 
 	@FormField(displayName = "Member No")
 	@NotEmpty(groups = MemberChecks.class)
 	private String memberNo = "";
 
 	@FormField(displayName = "Member Since")
 	@Past(groups = MemberChecks.class)
 	private Date memberSince = new Date(new java.util.Date().getTime()); // this
 																			// should
 	// be
 	// derived
 	// from the
 	// member
 	// records.
 
 	/** The actual section the Youth or Adult member is attached to. */
 	@FormField(displayName = "Section")
 	@ManyToOne(optional = true, targetEntity = SectionType.class)
 	private SectionType section;
 
 	/**
 	 * Affiliate - An Affiliate is any one that is actively associated with the
 	 * group including Youth doing the three for free and the the parents of
 	 * those Youth.
 	 * 
 	 * Prospects are not Affiliates.
 	 */
 	@FormField(displayName = "Hobbies")
 	private String hobbies = "";
 
 	@FormField(displayName = "Affiliated Since")
 	private Date affiliatedSince = new Date(new java.util.Date().getTime());
 
 	@FormField(displayName = "Group Role")
 	@ManyToOne(targetEntity = GroupRole.class)
 	private GroupRole groupRole;
 
 	@FormField(displayName = "Medicare No")
 	private String medicareNo = "";
 
 	@FormField(displayName = "Ambulance Subscriber")
 	private Boolean ambulanceSubscriber = false;
 
 	@FormField(displayName = "Private Medical Insurance")
 	private Boolean privateMedicalInsurance = false;
 
 	@FormField(displayName = "Private Medical Fund Name")
 	private String privateMedicalFundName = "";
 
 	@FormField(displayName = "Medical Fund No.")
 	private String medicalFundNo = "";
 
 	/**
 	 * Affiliated Adults
 	 */
 	@FormField(displayName = "Current Employer")
 	private String currentEmployer = "";
 
 	@FormField(displayName = "Job Title")
 	private String jobTitle = "";
 
 	@FormField(displayName = "Has License")
 	private Boolean hasLicense = false;
 
 	@FormField(displayName = "Has WWC")
 	private Boolean hasWWC = false;
 
 	@FormField(displayName = "WWC Expiry")
 	private Date wwcExpiry = new Date(new java.util.Date().getTime());
 
 	@FormField(displayName = "WWC No")
 	private String wwcNo = "";
 
 	@FormField(displayName = "Has Police Check")
 	private Boolean hasPoliceCheck = false;
 
 	@FormField(displayName = "Police Check Expiry")
 	private Date policeCheckExpiry = new Date(new java.util.Date().getTime());
 
 	@FormField(displayName = "Has Food Handling Certificate")
 	private Boolean hasFoodHandlingCertificate = false;
 
 	@FormField(displayName = "Has First Aid Certificate")
 	private Boolean hasFirstAidCertificate = false;
 
 	/**
 	 * Contacts this contact is related to on the Left Hand Side (LHS) of the
 	 * relationship type.
 	 * 
 	 * e.g. Brett 'Parent Of' Tristan
 	 * 
 	 * Brett is on the LHS of the relationship
 	 */
 	@OneToMany(mappedBy = "lhs", targetEntity = Relationship.class, cascade = CascadeType.PERSIST)
 	private final Set<Relationship> lhsrelationships = new HashSet<>();
 
 	/**
 	 * Contacts this contact is related to on the Right Hand Side (RHS) of the
 	 * relationship type.
 	 * 
 	 * e.g. Brett 'Parent Of' Tristan
 	 * 
 	 * Tristan is on the RHS of the relationship
 	 */
 	@OneToMany(mappedBy = "rhs", targetEntity = Relationship.class, cascade = CascadeType.PERSIST)
 	private final Set<Relationship> rhsrelationships = new HashSet<>();
 
 	/**
 	 * List of tags used to describe this Contact.
 	 */
 	// @ManyToMany(mappedBy = "contacts", cascade = CascadeType.ALL, fetch =
 	// FetchType.EAGER)
 	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER, targetEntity = Tag.class)
 	private Set<Tag> tags = new HashSet<>();
 
 	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "attachedContact")
 	@FormField(displayName = "")
 	private List<Note> notes = new ArrayList<>();
 
 	/**
 	 * List of interactions with this contact.
 	 */
 	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "withContact", targetEntity = Activity.class)
 	private List<Activity> activites = new ArrayList<>();
 
 	/**
 	 * Id imported along with the contact. Used to link the contact to an
 	 * external data source generally the one it was imported from. This can be
	 * used during susequent imports to link additional data entities to this
 	 * contact.
 	 */
 	@FormField(displayName = "Import ID")
 	private String importId;
 
 	public String getImportId()
 	{
 		return importId;
 	}
 
 	public void setImportId(String importId)
 	{
 		this.importId = importId;
 	}
 
 	public Contact()
 	{
 		Calendar TenYearsAgo = Calendar.getInstance();
 		TenYearsAgo.add(Calendar.YEAR, -10);
 		this.birthDate = new Date(TenYearsAgo.getTime().getTime());
 		this.wwcExpiry = new Date(TenYearsAgo.getTime().getTime());
 	}
 
 	@Override
 	public String toString()
 	{
 		return this.firstname + ", " + this.lastname;
 	}
 
 	public Tag getTag(String tagName)
 	{
 		Tag found = null;
 		for (Tag tag : this.tags)
 		{
 			if (tag.isTag(tagName))
 			{
 				found = tag;
 				break;
 			}
 		}
 		return found;
 	}
 
 	public List<Note> getNotes()
 	{
 		return this.notes;
 	}
 
 	public Set<Tag> getTags()
 	{
 		return this.tags;
 	}
 
 	public Date getBirthDate()
 	{
 		return this.birthDate;
 	}
 
 	public String getMiddlename()
 	{
 		return middlename;
 	}
 
 	public void setMiddlename(String middlename)
 	{
 		this.middlename = middlename;
 	}
 
 	@Access(value = AccessType.PROPERTY)
 	public SectionType getSectionEligibility()
 	{
 		ContactDao daoContact = new DaoFactory().getContactDao();
 		SectionType eligibility = daoContact.getSectionEligibilty(this.birthDate);
 		this.sectionEligibility = eligibility;
 
 		return this.sectionEligibility;
 	}
 
 	public void setSectionEligibility(SectionType sectionType)
 	{
 		// do nothing as this is transient and readonly
 	}
 
 	public SectionType getSection()
 	{
 		return section;
 	}
 
 	public void setSection(SectionType section)
 	{
 		this.section = section;
 	}
 
 	public Date getWwcExpiry()
 	{
 		return wwcExpiry;
 	}
 
 	public void setWwcExpiry(Date wwcExpiry)
 	{
 		this.wwcExpiry = wwcExpiry;
 	}
 
 	public Date getPoliceCheckExpiry()
 	{
 		return policeCheckExpiry;
 	}
 
 	public void setPoliceCheckExpiry(Date policeCheckExpiry)
 	{
 		this.policeCheckExpiry = policeCheckExpiry;
 	}
 
 	public Boolean getActive()
 	{
 		return active;
 	}
 
 	public String getPrefix()
 	{
 		return prefix;
 	}
 
 	public void setFirstname(String firstname)
 	{
 		this.firstname = firstname;
 	}
 
 	public String getFirstname()
 	{
 		return firstname;
 	}
 
 	public String getLastname()
 	{
 		return lastname;
 	}
 
 	public Gender getGender()
 	{
 		return gender;
 	}
 
 	public void setPhone1(String phoneNo)
 	{
 		this.phone1.setPhoneNo(phoneNo);
 	}
 
 	public void setPhone1(Phone phone1)
 	{
 		this.phone1 = phone1;
 	}
 
 	public Phone getPhone1()
 	{
 		return phone1;
 	}
 
 	public Phone getPhone2()
 	{
 		return phone2;
 	}
 
 	public Phone getPhone3()
 	{
 		return phone3;
 	}
 
 	public String getHomeEmail()
 	{
 		return homeEmail;
 	}
 
 	public Age getAge()
 	{
 		return age;
 	}
 
 	public Phone getPrimaryPhone()
 	{
 		Phone primary = null;
 		if (phone1.getPrimaryPhone())
 			primary = phone1;
 		else if (phone2.getPrimaryPhone())
 			primary = phone2;
 		else if (phone3.getPrimaryPhone())
 			primary = phone3;
 		return primary;
 
 	}
 
 	public void setPrimaryPhone(Phone phoneNo)
 	{
 		// No Op
 	}
 
 	public void setAge(Age age)
 	{
 		this.age = age;
 	}
 
 	public void setActive(Boolean active)
 	{
 		this.active = active;
 	}
 
 	public void setPrefix(String prefix)
 	{
 		this.prefix = prefix;
 	}
 
 	public void setBirthDate(Date birthDate)
 	{
 		this.birthDate = birthDate;
 	}
 
 	public void setGender(Gender gender)
 	{
 		this.gender = gender;
 	}
 
 	public void setPhone2(Phone workPhone)
 	{
 		this.phone2 = workPhone;
 	}
 
 	public void setPhone3(Phone mobile)
 	{
 		this.phone3 = mobile;
 	}
 
 	public void setHomeEmail(String homeEmail)
 	{
 		this.homeEmail = homeEmail;
 	}
 
 	public void setWorkEmail(String workEmail)
 	{
 		this.workEmail = workEmail;
 	}
 
 	public void setPreferredEmail(PreferredEmail preferredEmail)
 	{
 		this.preferredEmail = preferredEmail;
 	}
 
 	public void setPreferredCommunications(PreferredCommunications preferredCommunications)
 	{
 		this.preferredCommunications = preferredCommunications;
 	}
 
 	public void setAllergies(String allergies)
 	{
 		this.allergies = allergies;
 	}
 
 	public void setCustodyOrder(Boolean custodyOrder)
 	{
 		this.custodyOrder = custodyOrder;
 	}
 
 	public void setCustodyOrderDetails(String custodyOrderDetails)
 	{
 		this.custodyOrderDetails = custodyOrderDetails;
 	}
 
 	public void setSchool(String school)
 	{
 		this.school = school;
 	}
 
 	public void setIsMember(Boolean isMember)
 	{
 		this.isMember = isMember;
 	}
 
 	public void setMemberNo(String memberNo)
 	{
 		this.memberNo = memberNo;
 	}
 
 	public void setMemberSince(Date memberSince)
 	{
 		this.memberSince = memberSince;
 	}
 
 	public void setHobbies(String hobbies)
 	{
 		this.hobbies = hobbies;
 	}
 
 	public void setAffiliatedSince(Date affiliatedSince)
 	{
 		this.affiliatedSince = affiliatedSince;
 	}
 
 	public void setRole(GroupRole role)
 	{
 		this.groupRole = role;
 	}
 
 	public void setMedicareNo(String medicareNo)
 	{
 		this.medicareNo = medicareNo;
 	}
 
 	public void setAmbulanceSubscriber(Boolean ambulanceSubscriber)
 	{
 		this.ambulanceSubscriber = ambulanceSubscriber;
 	}
 
 	public void setPrivateMedicalInsurance(Boolean privateMedicalInsurance)
 	{
 		this.privateMedicalInsurance = privateMedicalInsurance;
 	}
 
 	public void setPrivateMedicalFundName(String privateMedicalFundName)
 	{
 		this.privateMedicalFundName = privateMedicalFundName;
 	}
 
 	public void setCurrentEmployer(String currentEmployer)
 	{
 		this.currentEmployer = currentEmployer;
 	}
 
 	public void setJobTitle(String jobTitle)
 	{
 		this.jobTitle = jobTitle;
 	}
 
 	public void setHasWWC(Boolean hasWWC)
 	{
 		this.hasWWC = hasWWC;
 	}
 
 	public void setWwcNo(String wwcNo)
 	{
 		this.wwcNo = wwcNo;
 	}
 
 	public void setHasPoliceCheck(Boolean hasPoliceCheck)
 	{
 		this.hasPoliceCheck = hasPoliceCheck;
 	}
 
 	public void setHasFoodHandlingCertificate(Boolean hasFoodHandlingCertificate)
 	{
 		this.hasFoodHandlingCertificate = hasFoodHandlingCertificate;
 	}
 
 	public void setHasFirstAidCertificate(Boolean hasFirstAidCertificate)
 	{
 		this.hasFirstAidCertificate = hasFirstAidCertificate;
 	}
 
 	public void setTags(Set<Tag> tags)
 	{
 		this.tags = tags;
 	}
 
 	public void setNotes(List<Note> notes)
 	{
 		this.notes = notes;
 	}
 
 	public void setActivites(List<Activity> activites)
 	{
 		this.activites = activites;
 	}
 
 
 	public String getWorkEmail()
 	{
 		return workEmail;
 	}
 
 	public PreferredEmail getPreferredEmail()
 	{
 		return preferredEmail;
 	}
 
 	public PreferredCommunications getPreferredCommunications()
 	{
 		return preferredCommunications;
 	}
 
 	public String getAllergies()
 	{
 		return allergies;
 	}
 
 	public Boolean getCustodyOrder()
 	{
 		return custodyOrder;
 	}
 
 	public String getCustodyOrderDetails()
 	{
 		return custodyOrderDetails;
 	}
 
 	public String getSchool()
 	{
 		return school;
 	}
 
 	public Boolean getIsMember()
 	{
 		return isMember;
 	}
 
 	public String getMemberNo()
 	{
 		return memberNo;
 	}
 
 	public Date getMemberSince()
 	{
 		return memberSince;
 	}
 
 	public String getHobbies()
 	{
 		return hobbies;
 	}
 
 	public Date getAffiliatedSince()
 	{
 		return affiliatedSince;
 	}
 
 	public GroupRole getRole()
 	{
 		return groupRole;
 	}
 
 	public String getMedicareNo()
 	{
 		return medicareNo;
 	}
 
 	public Boolean getAmbulanceSubscriber()
 	{
 		return ambulanceSubscriber;
 	}
 
 	public Boolean getPrivateMedicalInsurance()
 	{
 		return privateMedicalInsurance;
 	}
 
 	public String getPrivateMedicalFundName()
 	{
 		return privateMedicalFundName;
 	}
 
 	public String getCurrentEmployer()
 	{
 		return currentEmployer;
 	}
 
 	public String getJobTitle()
 	{
 		return jobTitle;
 	}
 
 	public Boolean getHasWWC()
 	{
 		return hasWWC;
 	}
 
 	public String getWwcNo()
 	{
 		return wwcNo;
 	}
 
 	public Boolean getHasPoliceCheck()
 	{
 		return hasPoliceCheck;
 	}
 
 	public Boolean getHasFoodHandlingCertificate()
 	{
 		return hasFoodHandlingCertificate;
 	}
 
 	public Boolean getHasFirstAidCertificate()
 	{
 		return hasFirstAidCertificate;
 	}
 
 	public List<Activity> getActivites()
 	{
 		return activites;
 	}
 
 	public void setAddress(Address address)
 	{
 		this.address = address;
 	}
 
 	public Address getAddress()
 	{
 		return address;
 	}
 
 	public void setLastname(String lastname)
 	{
 		this.lastname = lastname;
 	}
 
 	public void setStreet(String street)
 	{
 		this.address.setStreet(street);
 
 	}
 
 	public void setCity(String city)
 	{
 		this.address.setCity(city);
 
 	}
 
 	public void setState(String state)
 	{
 		this.address.setState(state);
 
 	}
 
 	public void setPostcode(String postcode)
 	{
 		this.address.setPostcode(postcode);
 	}
 
 	public void setBirthDate(String fieldValue)
 	{
 		DateTimeParser[] parsers =
 		{ DateTimeFormat.forPattern("yyyy-MM-dd").getParser(), DateTimeFormat.forPattern("yyyy/MM/dd").getParser() };
 		DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();
 
 		if ((fieldValue != null && fieldValue.length() > 0))
 		{
 			DateTime date1 = formatter.parseDateTime(fieldValue);
 			setBirthDate(new java.sql.Date(date1.toDate().getTime()));
 		}
 	}
 
 	public String getFullname()
 	{
 		return firstname + " " + lastname;
 	}
 
 	public void setFullname(String fullname)
 	{
 		// we ignore this argument as fullname is always an amalgam of the firstname and lastname;
 		this.fullname = firstname + " " + lastname;
 	}
 
 	@PreRemove
 	private void preRemove()
 	{
 		tags.clear();
 		// activites.isEmpty();
 		// activites.clear();
 		notes.clear();
 	}
 
 
 	@Override
 	public String getName()
 	{
 		return this.getFullname();
 	}
 
 	public void addNote(Note child)
 	{
 		this.notes.add(child);
 
 	}
 
 	public Set<Relationship> getLHSRelationships()
 	{
 		return this.lhsrelationships;
 	}
 }
