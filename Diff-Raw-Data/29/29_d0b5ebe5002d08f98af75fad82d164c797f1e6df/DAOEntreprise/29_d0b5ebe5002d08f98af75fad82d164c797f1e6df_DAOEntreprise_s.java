 package sessionBeans;
 
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 
 import util.ServerUtils;
 
 import daoInterface.IDAOEntreprise;
 import entityBeans.Address;
 import entityBeans.Contact;
 import entityBeans.ContactGroup;
 import entityBeans.Entreprise;
 import entityBeans.PhoneNumber;
 
 @Stateless(mappedName="DAOEntrepriseBean")
 public class DAOEntreprise implements IDAOEntreprise {
 	
 	@PersistenceContext
 	EntityManager em;
 
 	@Override
 	public String addEntreprise(String firstName, String lastName,
 			String email, String street, String city, String zip,
 			String country, String phoneKind, String phoneNumber,
 			long siretNumber,String group) {
 		// TODO Auto-generated method stub
 		Entreprise entreprise = new Entreprise();
 		entreprise.setFirstName(firstName);
 		entreprise.setLastName(lastName);
 		entreprise.setEmail(email);
 		entreprise.setNumSiret(siretNumber);
 		
 		Address address = new Address();
 		address.setStreet(street);
 		address.setCity(city);
 		address.setZip(zip);
 		address.setCountry(country);
 		entreprise.setAddress(address); // Uni birectionnel
 		
 		PhoneNumber phone = new PhoneNumber();
 		phone.setPhoneKind(phoneKind);
 		phone.setPhoneNumber(phoneNumber);
 		entreprise.getPhoneNumbers().add(phone);
 		phone.setContact(entreprise);
 		
 		String request = "from ContactGroup contactGroup";
 		Query query = em.createQuery(request);
 		List<ContactGroup> l = query.getResultList();
 		Iterator<ContactGroup> ite = l.iterator();
 		ContactGroup contactGroup = null;
 		String rvalue = null;
 		while (ite.hasNext()) {
 			contactGroup = ite.next();
 			if (contactGroup.getGroupName().equals(group)) {
 				entreprise.getContactGroups().add(contactGroup);
 				contactGroup.getContacts().add(entreprise);
 				em.persist(entreprise);
 				rvalue = ServerUtils.opFait;
 				return rvalue;
 			}
 		}
 		
 		// If new contact Group
 		contactGroup = new ContactGroup();
 		contactGroup.setGroupName(group);
 		entreprise.getContactGroups().add(contactGroup);
 		contactGroup.getContacts().add(entreprise);
 		
 		em.persist(entreprise);
 		rvalue = ServerUtils.opFait;
 		return rvalue;
 	}
 
 	@Override
 	public String searchEntreprise(long id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String deleteEntreprise(long id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getAllEntreprise() {
 		// TODO Auto-generated method stub
 		StringBuffer requeteS = new StringBuffer();
 		requeteS.append("from Entreprise entreprise")
 				.append(" left join entreprise.address as address")
 				.append(" left join entreprise.phoneNumbers as phoneNumber")
 				.append(" left join entreprise.contactGroups as contactGroup")
 				;
 
 		Query query = em.createQuery(requeteS.toString());
        
 		List l = query.getResultList();
 		System.out.println("reslt size=" + l.size());
 
 		String rvalue = null;
         if (l.size() == 0)
            rvalue = ServerUtils.opNoRecods;
         else
             rvalue = ServerUtils.generateEntrepriseTable(l, "Entreprise table");
 
 		return rvalue;
 	}
 
 	@Override
 	public String searchEntreprise(long id, String firstName, String lastName,
 			String email, String street, String city, String zip,
 			String country, String phoneKind, String phoneNumber,
 			String group, String numSiret) {
 	String rvalue = null;
 		
 		String s_q = "from Entreprise entreprise "
 				+ " left join entreprise.address as address"
 				+ " left join entreprise.phoneNumbers as phoneNumber"
 				+ " left join entreprise.contactGroups as contactGroup"
 				+ " WHERE entreprise.firstName LIKE :firstName"
 				+ " AND entreprise.lastName LIKE :lastName"
 				+ " AND entreprise.email LIKE :email"
 				+ " AND address.street LIKE :street"
 				+ " AND address.city LIKE :city"
 				+ " AND address.zip LIKE :zip"
 				+ " AND address.country LIKE :country"
 				+ " AND phonenumber.phoneKind LIKE :phoneKind"
 				+ " AND phonenumber.phoneNumber LIKE :phoneNumber"
 				+ " AND contactgroup.groupName LIKE :groupName"
 //				+ " AND entreprise.numSiret LIKE :numSiret"
 				;
 		
 		
 
 		//Long sir = new Long(numSiret);
 		Query q = em.createQuery(s_q);
 		q.setParameter("firstName", firstName + "%");
 		q.setParameter("lastName", lastName + "%");
 		q.setParameter("email", email + "%");
 		q.setParameter("street", street + "%");
 		q.setParameter("city", city + "%");
 		q.setParameter("zip", zip + "%");
 		q.setParameter("country", country + "%");
 		q.setParameter("phoneKind", phoneKind + "%");
 		q.setParameter("phoneNumber", phoneNumber + "%");
 		q.setParameter("groupName", group + "%");
 		
 //		q.setParameter("numSiret", sir );
 
 		
 				
 		
 
 		List l  = q.getResultList();
 		
 		System.out.println("list size=" + l.size());
 		if (l.size() != 0)
 			rvalue = ServerUtils.generateEntrepriseTable(l, "Contact table");
 		else
			rvalue = ServerUtils.opNoRecods;
 
 		return rvalue;
 	}
 
 	@Override
 	public String deleteEntreprise(long id, String firstName, String lastName,
 			String email, String street, String city, String zip,
 			String country, String phoneKind, String phoneNumber,
 			String numSiret) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String searchContactSimple(String id) {
 		StringBuffer requeteS = new StringBuffer();
 		requeteS.append("from Entreprise entreprise")
 				.append(" left join entreprise.address as address")
 				.append(" left join entreprise.phoneNumbers as phoneNumber")
 				.append(" left join entreprise.contactGroups as contactGroup")
 				.append(" WHERE entreprise.contactId = :id");
 		
 		Long idInt = new Long(id);
 
 		Query query = em.createQuery(requeteS.toString());
 		query.setParameter("id", idInt);
 
 		List<Object[]> l = query.getResultList();
 		System.out.println("reslt size=" + l.size());
 
 		String rvalue = null;
 		if (l.size() == 1){
 			Entreprise c = (Entreprise) l.get(0)[0];
 			if (c != null){
 				Address a = (Address) l.get(0)[1];
 				PhoneNumber p = (PhoneNumber)l.get(0)[2];
 				ContactGroup cg = (ContactGroup)l.get(0)[3];
 				rvalue = ServerUtils.generateFullEntrepriseRow(c, a, p, cg);
 			}
 		}
 		else{
			rvalue = ServerUtils.opNoRecods;
 		}
 
 		return rvalue;
 	}
 	
 
 
 }
