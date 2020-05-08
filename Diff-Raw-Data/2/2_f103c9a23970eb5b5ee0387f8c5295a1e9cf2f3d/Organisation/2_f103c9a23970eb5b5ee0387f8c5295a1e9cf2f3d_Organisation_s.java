 package au.org.scoutmaster.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.validator.constraints.NotBlank;
 
 /**
  * An organisation (company, government body) that the scout group interacts
  * with in some way.
  * 
  * There should also be one special organisation created for each system which
  * is the one that represents the Scout Group.
  * 
  * e.g. supplier of uniforms.
  * 
  * @author bsutton
  * 
  */
 @Entity(name="Organisation")
 @Table(name="Organisation")
 @Access(AccessType.FIELD)
 
 @NamedQueries(
 {
 	@NamedQuery(name = Organisation.FIND_OUR_SCOUT_GROUP, query = "SELECT organisation FROM Organisation organisation where organisation.isOurScoutGroup = true"),
 })
 public class Organisation extends BaseEntity
 {
 	private static final long serialVersionUID = 1L;
 
 	public static final String FIND_OUR_SCOUT_GROUP = "Organisation.findOurScoutGroup";
 
 	public static final String PRIMARY_PHONE = "primaryPhone";
 
 	/**
 	 * If true then this organisation represents our local scout group. This
 	 * organisations should be created by the setup wizard.
 	 * 
 	 */
	private Boolean isOurScoutGroup;
 
 	/**
 	 * The name of the organisation
 	 */
 	@NotBlank
 	@Column(unique=true)
 	private String name;
 	
 	/**
 	 * The primary role an organisation takes in connection to the group.
 	 */
 	@ManyToOne(targetEntity=OrganisationType.class)
 	private OrganisationType organisationType;
 
 	/**
 	 * A description of the organisation and how the group interacts with it.
 	 */
 	private String description;
 
 	/**
 	 * The list of contacts at the organsiation that the group associates with.
 	 */
 	@OneToMany(cascade = CascadeType.ALL, targetEntity=Contact.class)
 	private List<Contact> contacts = new ArrayList<>();
 
 	/**
 	 * The location of the organisation.
 	 */
 	@OneToOne(targetEntity=Address.class)
 	private Address location = new Address();
 	
 	
 	@Transient
 	private Phone primaryPhone;
 
 	@OneToOne(targetEntity=Phone.class)
 	private Phone phone1 = new Phone();
 
 	@OneToOne(targetEntity=Phone.class)
 	private Phone phone2 = new Phone();
 
 	@OneToOne(targetEntity=Phone.class)
 	private Phone phone3 = new Phone();
 	
 
 	/**
 	 * The list of tags used to describe the organisation.
 	 */
 	@ManyToMany(targetEntity=Tag.class)
 	private List<Tag> tags = new ArrayList<>();
 
 	@OneToMany(cascade = CascadeType.ALL,targetEntity=Note.class)
 	private List<Note> notes = new ArrayList<>();
 
 	/**
 	 * List of interactions with this contact.
 	 */
 	@OneToMany(targetEntity=Activity.class)
 	private List<Activity> activites = new ArrayList<>();
 
 
 
 	public Boolean isOurScoutGroup()
 	{
 		return isOurScoutGroup;
 	}
 
 	public void setOurScoutGroup(Boolean isOurScoutGroup)
 	{
 		this.isOurScoutGroup = isOurScoutGroup;
 	}
 
 	public String getDescription()
 	{
 		return description;
 	}
 
 	public void setDescription(String description)
 	{
 		this.description = description;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public Address getLocation()
 	{
 		return location;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 
 	}
 
 	public void setLocation(Address location)
 	{
 		this.location = location;
 	}
 
 	public void addTag(Tag tag)
 	{
 		this.tags.add(tag);
 	}
 
 	public Phone getPhone1()
 	{
 		return phone1;
 	}
 
 	public void setPhone1(Phone phone1)
 	{
 		this.phone1 = phone1;
 	}
 
 	public Phone getPhone2()
 	{
 		return phone2;
 	}
 
 	public void setPhone2(Phone phone2)
 	{
 		this.phone2 = phone2;
 	}
 
 	public Phone getPhone3()
 	{
 		return phone3;
 	}
 
 	public void setPhone3(Phone phone3)
 	{
 		this.phone3 = phone3;
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
 
 	public OrganisationType getType()
 	{
 		return organisationType;
 	}
 
 	public void setType(OrganisationType type)
 	{
 		this.organisationType = type;
 	}
 
 
 
 }
