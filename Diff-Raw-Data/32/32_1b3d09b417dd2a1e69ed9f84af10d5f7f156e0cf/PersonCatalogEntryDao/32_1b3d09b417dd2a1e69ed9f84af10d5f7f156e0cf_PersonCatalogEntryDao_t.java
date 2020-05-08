 package gov.nih.nci.cagrid.portal.dao.catalog;
 
 import gov.nih.nci.cagrid.portal.annotation.UpdatesCatalogs;
 import gov.nih.nci.cagrid.portal.dao.PersonDao;
 import gov.nih.nci.cagrid.portal.dao.PortalUserDao;
 import gov.nih.nci.cagrid.portal.domain.Address;
 import gov.nih.nci.cagrid.portal.domain.Person;
 import gov.nih.nci.cagrid.portal.domain.PortalUser;
 import gov.nih.nci.cagrid.portal.domain.catalog.PersonCatalogEntry;
 import gov.nih.nci.cagrid.portal.util.BeanUtils;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 public class PersonCatalogEntryDao extends
         AboutCatalogEntryDao<PersonCatalogEntry, PortalUser> {
     private PortalUserDao portalUserDao;
     private PersonDao personDao;
 
     public PersonCatalogEntryDao() {
     }
 
     /*
       * (non-Javadoc)
       *
       * @see gov.nih.nci.cagrid.portal.dao.AbstractDao#domainClass()
       */
     @Override
     public Class domainClass() {
         return PersonCatalogEntry.class;
     }
 
 
     @UpdatesCatalogs
     @Transactional
     public PersonCatalogEntry createCatalogAbout(PortalUser user) {
         user = getPortalUserDao().getById(user.getId());
         PersonCatalogEntry entry = isAbout(user);
         if (entry == null) {
             entry = new PersonCatalogEntry();
             entry.setAbout(user);
             user.setCatalog(entry);
             entry.setDescription("Portal User");

         } else
             logger
                     .debug("Catalog entry already exists. Will update the existing entry");
 
         if (!entry.isPublished()) {
             logger
                     .debug("Catalog entry has not been published. Will sync with domain object");
 
 
             entry.setEmailAddress(BeanUtils.traverse(user,
                     "person.emailAddress"));
             entry.setLastName(BeanUtils.traverse(user, "person.lastName"));
             entry.setFirstName(BeanUtils.traverse(user, "person.firstName"));
             entry.setName(entry.getFirstName() + " " + entry.getLastName());
             entry.setPhoneNumber(BeanUtils.traverse(user, "person.phoneNumber"));
 
             Person p = user.getPerson();
             if (p != null) {
                 if (p.getAddresses() != null && p.getAddresses().size() > 0) {
                     Address address = user.getPerson().getAddresses().get(0);
                     entry.setCountryCode(address.getCountry());
                     entry.setPostalCode(address.getPostalCode());
                     entry.setStreet1(address.getStreet1());
                     entry.setStreet2(address.getStreet2());
                     entry.setLatitude(address.getLatitude());
                     entry.setLongitude(address.getLongitude());
                     entry.setStateProvince(address.getStateProvince());
                 }
             }
         }
         save(entry);
         logger.debug("Saved Person catalog with ID" + entry.getId());
         return entry;
     }
 
     public List<PersonCatalogEntry> searchByNameAndEmail(String firstName,
                                                          String lastName, String emailAddress) {
         List<PersonCatalogEntry> l = getHibernateTemplate()
                 .find(
                         "from PersonCatalogEntry where firstName = ? and lastName = ? and emailAddress = ?",
                         new Object[]{firstName, lastName, emailAddress});
         return l;
     }
 
     public PortalUserDao getPortalUserDao() {
         return portalUserDao;
     }
 
     public void setPortalUserDao(PortalUserDao portalUserDao) {
         this.portalUserDao = portalUserDao;
     }
 
     public PersonDao getPersonDao() {
         return personDao;
     }
 
     public void setPersonDao(PersonDao personDao) {
         this.personDao = personDao;
     }
 }
