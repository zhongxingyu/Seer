 package au.org.scoutmaster.domain;
 
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Table;
 
 import org.hibernate.validator.constraints.NotBlank;
 import org.pojomatic.Pojomatic;
 import org.pojomatic.annotations.AutoProperty;
 
 /**
  * Tags are used to group and identify certain attributes of a contact,
  * organisation, household or school
  * 
  * 
  * 
  * @author bsutton
  * 
  */
 @Entity(name="Tag")
 @Table(name="Tag")
 @Access(AccessType.FIELD)
 @NamedQueries(
 {
 		@NamedQuery(name = Tag.FIND_BY_NAME, query = "SELECT tag FROM Tag tag WHERE tag.name = :tagName") })
 @AutoProperty
 public class Tag extends BaseEntity
 {
 
 	static public final String FIND_BY_NAME = "Tag.findByName";
 
 	private static final long serialVersionUID = 1L;
 	public static final String DESCRIPTION = "description";
 
 	public static final String NAME = "name";
 
 
 	@Column(unique = true, length = 30)
 	@NotBlank
 	String name;
 
 
 	@Column(length = 250)
 	@NotBlank
 	String description;
 	
 	/**
 	 * Indicates that this is a builtin tag and therefore it may not be deleted
 	 */
 	Boolean builtin = new Boolean(false);
 	
 
 	/*
 	 * Non detachable tags are a special class of 
 	 * tags which are automatically assigned to an entity
 	 * based on some other entity property.
 	 * For instance a Contact which is a Youth Member would
 	 * also be automatically assigned to the tag Youth Member.
 	 * This is some what redundant but the idea is to make searching
	 * for entities by a tag all encompasing.
 	 * i.e. all major attributes of an entity are cross referenced
 	 * by a tag for the purposes of searching.
 	 * 
 	 * Only builtin tags may be NON-detachable.
 	 */
 	Boolean detachable = new Boolean(true);
 
 	// @ManyToMany
 	// private final List<Contact> contacts = new ArrayList<>();
 	//
 	// @ManyToMany
 	// private final List<School> schools = new ArrayList<>();
 	//
 	// @ManyToMany
 	// private final List<Organisation> organisations = new ArrayList<>();
 	//
 	// @ManyToMany
 	// private final List<Household> households = new ArrayList<>();
 
 	public Tag()
 	{
 
 	}
 
 	public Tag(String name)
 	{
 		this.name = name;
 		this.description = "";
 	}
 
 	public Tag(String name, String description)
 	{
 		this.name = name;
 		this.description = description;
 	}
 
 	// public void addContact(Contact contact)
 	// {
 	// this.contacts.add(contact);
 	// }
 
 	public Boolean isTag(String tagName)
 	{
 		return this.name.equals(tagName);
 	}
 
 	
 	public String getName()
 	{
 		return this.name;
 	}
 
 	public String getDescription()
 	{
 		return this.description;
 	}
 	
 	
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public void setDescription(String description)
 	{
 		this.description = description;
 	}
 
 	public Boolean getBuiltin()
 	{
 		return builtin;
 	}
 
 	public void setBuiltin(Boolean builtin)
 	{
 		this.builtin = builtin;
 	}
 
 	public Boolean getDetachable()
 	{
 		return detachable;
 	}
 
 	public void setDetachable(Boolean detachable)
 	{
 		this.detachable = detachable;
 	}
 
 
 	// public List<Contact> getContacts()
 	// {
 	// return this.contacts;
 	// }
 	//
 	// public List<School> getSchools()
 	// {
 	// return this.schools;
 	// }
 	//
 	// public List<Organisation> getOrganisations()
 	// {
 	// return this.organisations;
 	// }
 	//
 	// public List<Household> getHouseholds()
 	// {
 	// return this.households;
 	// }
 
 	@Override
 	public boolean equals(Object other)
 	{
 		return Pojomatic.equals(this, other);
 	}
 
 	@Override
 	public String toString()
 	{
 		return this.name;
 	}
 
 	@Override
 	public int hashCode()
 	{
 		return Pojomatic.hashCode(this);
 	}
 
 }
