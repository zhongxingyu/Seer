 package au.org.scoutmaster.dao;
 
 import java.sql.Date;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 
 import au.com.vaadinutils.dao.JpaBaseDao;
 import au.org.scoutmaster.domain.Activity;
 import au.org.scoutmaster.domain.Age;
 import au.org.scoutmaster.domain.Contact;
 import au.org.scoutmaster.domain.Contact_;
 import au.org.scoutmaster.domain.Note;
 import au.org.scoutmaster.domain.Relationship;
 import au.org.scoutmaster.domain.SectionType;
 import au.org.scoutmaster.domain.Section_;
 import au.org.scoutmaster.domain.Tag;
 
 import com.vaadin.addon.jpacontainer.JPAContainer;
 
 public class ContactDao extends JpaBaseDao<Contact, Long> implements Dao<Contact, Long>
 {
 	static private Logger logger = Logger.getLogger(ContactDao.class);
 
 	public ContactDao()
 	{
 		// inherit the default per request em. 
 	}
 	public ContactDao(EntityManager em)
 	{
 		super(em);
 	}
 
 	
 	@SuppressWarnings("unchecked")
 	public List<Contact> findByName(String firstname, String lastname)
 	{
 		Query query = entityManager.createNamedQuery(Contact.FIND_BY_NAME);
 		query.setParameter("firstname", firstname);
 		query.setParameter("lastname", lastname);
 		List<Contact> resultContacts = query.getResultList();
 		return resultContacts;
 	}
 
 	
 	public Long getAge(Contact contact)
 	{
 		long age = 0;
 		if (contact.getBirthDate() != null)
 		{
 			Calendar cal1 = new GregorianCalendar();
 			Calendar cal2 = new GregorianCalendar();
 			int factor = 0;
 			Date date1 = contact.getBirthDate();
 			Date date2 = new Date(new java.util.Date().getTime());
 			cal1.setTime(date1);
 			cal2.setTime(date2);
 			if (cal2.get(Calendar.DAY_OF_YEAR) < cal1.get(Calendar.DAY_OF_YEAR))
 			{
 				factor = -1;
 			}
 			age = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR) + factor;
 		}
 		return age;
 	}
 
 	public void addNote(Contact contact, String subject, String body)
 	{
 		Note note = new Note(subject, body);
 		// note.setContact(this);
 		contact.getNotes().add(note);
 	}
 
 	public void attachTag(Contact contact, Tag tag)
 	{
 //		tag.addContact(this);
 		contact.getTags().add(tag);
 	}
 	
 	public void addRelationship(Contact contact, Relationship child)
 	{
 		contact.getLHSRelationships().add(child);
 		
 	}
 	public void addActivity(Contact contact, Activity activity)
 	{
		contact.getActivites().add(activity);
 
 	}
 
 
 	/**
 	 * Detaches the tag from this contact. The tag entity is not actually deleted as
 	 * it may be used by other entities.
 	 *  
 	 * @param tagName
 	 */
 	public void detachTag(Contact contact, String tagName)
 	{
 		Tag tagToRemove = null;
 		for (Tag tag : contact.getTags())
 		{
 			if (tag.isTag(tagName))
 			{
 				tagToRemove = tag;
 			}
 		}
 		if (tagToRemove != null)
 			detachTag(contact, tagToRemove);
 		else
 			logger.warn("Attempt to detach non-existant tag. tagName=" + tagName);
 	}
 
 	public void detachTag(Contact contact, Tag tag)
 	{
 		if (tag != null)
 			contact.getTags().remove(tag);
 	}
 
 
 
 	public Note getNote(Contact contact, String noteSubject)
 	{
 		Note found = null;
 		for (Note note : contact.getNotes())
 		{
 			if (note.getSubject().equals(noteSubject))
 			{
 				found = note;
 				break;
 			}
 		}
 		return found;
 
 	}
 	public boolean hasTag(Contact contact, Tag tag)
 	{
 		boolean hasTag = false;
 		for (Tag aTag : contact.getTags())
 		{
 			if (aTag.equals(tag))
 			{
 				hasTag = true;
 				break;
 			}
 				
 		}
 		return hasTag;
 	}
 	public Age getAge(java.util.Date birthDate)
 	{
 		DateTime date = new DateTime(birthDate);
 		return new Age(date);
 	}
 	public SectionType getSectionEligibilty(java.util.Date birthDate)
 	{
 		DateTime date = new DateTime(new DateMidnight(birthDate));
 		
 		SectionTypeDao daoSectionType = new SectionTypeDao();
 		return daoSectionType.getEligibleSection(date);
 	}
 	public JPAContainer<Contact> createVaadinContainer()
 	{
 		JPAContainer<Contact> contactContainer = super.createVaadinContainer();
 		contactContainer.addNestedContainerProperty("phone1.phoneNo");
 		contactContainer.addNestedContainerProperty("phone1.primaryPhone");
 		contactContainer.addNestedContainerProperty("phone1.phoneType");
 
 		contactContainer.addNestedContainerProperty("phone2.phoneNo");
 		contactContainer.addNestedContainerProperty("phone2.primaryPhone");
 		contactContainer.addNestedContainerProperty("phone2.phoneType");
 
 		contactContainer.addNestedContainerProperty("phone3.phoneNo");
 		contactContainer.addNestedContainerProperty("phone3.primaryPhone");
 		contactContainer.addNestedContainerProperty("phone3.phoneType");
 
 		contactContainer.addNestedContainerProperty("address.street");
 		contactContainer.addNestedContainerProperty("address.city");
 		contactContainer.addNestedContainerProperty("address.postcode");
 		contactContainer.addNestedContainerProperty("address.state");
 		
 		contactContainer.addNestedContainerProperty("groupRole.name");
 		contactContainer.addNestedContainerProperty(new Path(Contact_.section, Section_.name).getName());
 		
 		return contactContainer;
 	}
 
 }
