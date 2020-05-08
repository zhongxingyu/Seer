 package com.aminpy.phonebook.dao.contactnumber;
 
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import com.aminpy.phonebook.model.ContactNumber;
 
 @Stateless
 public class ContactNumberDAO implements ContactNumberDAOLocal {
 
 	@PersistenceContext(unitName = "manager1")
 	private EntityManager em;
 
 	@Override
 	public ContactNumber contactNumberCreate(ContactNumber contactNumber) {
 		em.persist(contactNumber);
 		return contactNumber;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<ContactNumber> contactNumberRead() {
 		return (List<ContactNumber>) em.createNamedQuery("ContactNumber.findAll")
 				.getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
	public List<ContactNumber> contactNumberRead(String nationalCode) {
 		return (List<ContactNumber>) em.createNamedQuery("ContactNumber.findByNumber")
				.setParameter("nationalCode", nationalCode).getResultList();
 	}
 
 	@Override
 	public ContactNumber contactNumberUpdate(ContactNumber contactNumber) {
 		return em.merge(contactNumber);
 	}
 
 	@Override
 	public ContactNumber contactNumberDelete(ContactNumber contactNumber) {
 		em.remove(em.find(ContactNumber.class, contactNumber.getContactNumberID()));
 		return contactNumber;
 	}
 }
