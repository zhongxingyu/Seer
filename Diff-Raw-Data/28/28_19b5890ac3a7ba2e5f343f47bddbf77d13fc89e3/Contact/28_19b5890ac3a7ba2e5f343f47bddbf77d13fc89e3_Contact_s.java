 package entityBeans;
 
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.DiscriminatorColumn;
 import javax.persistence.DiscriminatorType;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "Contact")
 @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
 @DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.STRING)
 @DiscriminatorValue("Contact")
 public class Contact
 {
 	
 //	private long id;   
 //	  private String firstName;
 //	  private String lastName;
 //	  private String email;
 //	
 //	@Id@GeneratedValue(strategy=GenerationType.AUTO)
 //	  public long getId() {return id;}
 //
 //	  public void setId(long l) {id = l;}
 //	  public String getEmail() {return email;}
 //
 //	  public String getFirstName() {return firstName; }
 //
 //	  public String getLastName() {return lastName; }
 //
 //	  public void setEmail(String string) {email = string; }
 //
 //	  public void setFirstName(String string) {firstName = string; }
 //
 //	  public void setLastName(String string) {lastName = string; }
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	private String firstName;
 	private String lastName;
 	private long contactId;
 	private String email;
 	private Set<PhoneNumber> phoneNumbers = new HashSet<PhoneNumber>();
 	private Set<ContactGroup> contactGroups = new HashSet<ContactGroup>();
 	private Address address;
 //	
 //	public Contact(String firstName, String lastName, String email)
 //	{
 //		this.firstName = firstName;
 //		this.lastName = lastName;
 //		this.email = email;
 //	}
 //	// default constructer
 //	public Contact()
 //	{
 //		this("", "", "");
 //	}
 //	
 	@Id 
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	public long getContactId() {
 		return contactId;
 	}
 	public void setContactId(long contactId) {
 		this.contactId = contactId;
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
 	public String getEmail() {
 		return email;
 	}
 	public void setEmail(String email) {
 		this.email = email;
 	}
	@OneToMany(mappedBy = "contact")
 	public Set<PhoneNumber> getPhoneNumbers() {
 		return phoneNumbers;
 	}
 	public void setPhoneNumbers(Set<PhoneNumber> phoneNumbers) {
 		this.phoneNumbers = phoneNumbers;
 	}
 	
	@ManyToMany(mappedBy = "contacts")
 	public Set<ContactGroup> getContactGroups() {
 		return contactGroups;
 	}
 	public void setContactGroups(Set<ContactGroup> contactGroups) {
 		this.contactGroups = contactGroups;
 	}
	@OneToOne
 	public Address getAddress() {
 		return address;
 	}
 	public void setAddress(Address address) {
 		this.address = address;
 	}
 	
 	
 	
 	
 }
