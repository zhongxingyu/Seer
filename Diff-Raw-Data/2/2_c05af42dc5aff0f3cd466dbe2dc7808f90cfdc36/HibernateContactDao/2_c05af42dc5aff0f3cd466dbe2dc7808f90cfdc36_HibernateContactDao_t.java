 package net.frontlinesms.data.repository.hibernate;
 
 import java.util.List;
 
 import net.frontlinesms.data.DuplicateKeyException;
 import net.frontlinesms.data.domain.Contact;
 import net.frontlinesms.data.domain.Contact.Field;
 import net.frontlinesms.data.repository.ContactDao;
 
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Hibernate implementation of {@link ContactDao}.
  * @author Alex
  */
 public class HibernateContactDao extends BaseHibernateDao<Contact> implements ContactDao {
 	/** Create a new instance of this DAO. */
 		
 	public HibernateContactDao() {
 		super(Contact.class);
 	}
 
 	/** @see ContactDao#saveContact(Contact) */
 	public void saveContact(Contact contact) throws DuplicateKeyException {
 		super.save(contact);
 	}
 	
 	/** @see ContactDao#updateContact(Contact) */
 	public void updateContact(Contact contact) throws DuplicateKeyException {
 		super.update(contact);
 	}
 
 	/** @see ContactDao#deleteContact(Contact) */
 	@Transactional
 	public void deleteContact(Contact contact) {
 		// Delete all group memberships associated with this contact
 		super.getHibernateTemplate().bulkUpdate("DELETE FROM GroupMembership WHERE contact_contact_id='" + contact.getId() + "'");
 		
 		// Delete the contact
 		super.delete(contact);
 	}
 	
 	/** @see ContactDao#getAllContacts() */
 	public List<Contact> getAllContacts() {
 		return super.getAll();
 	}
 
 	/** @see ContactDao#getAllContacts(int, int) */
 	public List<Contact> getAllContacts(int startIndex, int limit) {
 		return super.getAll(startIndex, limit);
 	}
 
 	/** @see ContactDao#getContactByName(String) */
 	public Contact getContactByName(String name) {
 		DetachedCriteria criteria = super.getCriterion();
 		criteria.add(Restrictions.eq(Field.NAME.getFieldName(), name));
 		return super.getUnique(criteria);
 	}
 	
 	public int getContactsFilteredByNameCount(String contactNameFilter) {
 		DetachedCriteria criteria = getNameFilterCriteria(contactNameFilter);
 		return super.getCount(criteria);
 	}
 	
 	public List<Contact> getContactsFilteredByName(String contactNameFilter, int start, int limit) {
 		DetachedCriteria criteria = getNameFilterCriteria(contactNameFilter);
 		return super.getList(criteria, start, limit);
 	}
 
 	private DetachedCriteria getNameFilterCriteria(String contactNameFilter) {
 		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.ilike(Contact.Field.NAME.getFieldName(), '%' + contactNameFilter + '%'));
 		return criteria;
 	}
 
 	/** @see ContactDao#getContactCount() */
 	public int getContactCount() {
 		return super.countAll();
 	}
 
 	/** @see ContactDao#getFromMsisdn(String) */
 	public Contact getFromMsisdn(String phoneNumber) {
 		DetachedCriteria criteria = super.getCriterion();
 		criteria.add(Restrictions.eq(Field.PHONE_NUMBER.getFieldName(), phoneNumber));
 		return super.getUnique(criteria);
 	}
 
 	/** @see ContactDao#getPageNumber(Contact, int) */
 	public int getPageNumber(Contact contact, int contactsPerPage) {
 		// TODO this method is pretty dumb, at least in its current form.  or perhaps hibernate can cope with such foolishness?
 		return this.getAllContacts().indexOf(contact) / contactsPerPage;
 	}
 }
