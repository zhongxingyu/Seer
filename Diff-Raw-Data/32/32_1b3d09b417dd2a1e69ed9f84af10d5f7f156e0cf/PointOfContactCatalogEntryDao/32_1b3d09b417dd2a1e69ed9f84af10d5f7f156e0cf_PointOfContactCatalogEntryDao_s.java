 package gov.nih.nci.cagrid.portal.dao.catalog;
 
 import gov.nih.nci.cagrid.portal.domain.catalog.PointOfContactCatalogEntry;
 import gov.nih.nci.cagrid.portal.domain.metadata.common.PointOfContact;
 import gov.nih.nci.cagrid.portal.domain.metadata.common.ResearchCenterPointOfContact;
import gov.nih.nci.cagrid.portal.domain.Person;
import gov.nih.nci.cagrid.portal.domain.Address;
 import gov.nih.nci.cagrid.portal.util.BeanUtils;
import gov.nih.nci.cagrid.portal.annotation.UpdatesCatalogs;
 
 import javax.persistence.NonUniqueResultException;
 import java.util.List;
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 public class PointOfContactCatalogEntryDao extends
         AboutCatalogEntryDao<PointOfContactCatalogEntry, PointOfContact> {
 
     public PointOfContactCatalogEntryDao() {
     }
 
     /*
       * (non-Javadoc)
       *
       * @see gov.nih.nci.cagrid.portal.dao.AbstractDao#domainClass()
       */
     @Override
     public Class domainClass() {
         return PointOfContactCatalogEntry.class;
     }
 
     /**
      * Overriden. This does a search on email address. SO two CE's cannot
      * belong to POC's with the same email
      *
      * @param d
      * @return CatalogEntry from the domain object that the catalog is about
      */
     public PointOfContactCatalogEntry isAbout(PointOfContact d) {
 
         PointOfContactCatalogEntry catalog = null;
         List l = getHibernateTemplate().find("from " + domainClass().getSimpleName() + " where about.person.emailAddress = ?",
                 new Object[]{d.getPerson().getEmailAddress()});
         if (l.size() > 1) {
             throw new NonUniqueResultException("More than one CatalogEntry found for Domain Object with ID = " + d.getId());
         }
         if (l.size() == 1) {
             catalog = (PointOfContactCatalogEntry) l.iterator().next();
         }
         return catalog;
     }
 
    
 
     @UpdatesCatalogs
     public PointOfContactCatalogEntry createCatalogAbout(PointOfContact poc) {
         poc = (PointOfContact) getSession().load(PointOfContact.class, new Integer(poc.getId()));
 
         PointOfContactCatalogEntry entry = isAbout(poc);
         if (entry == null) {
             logger.debug("Will create a new Catalog for POC");
             entry = new PointOfContactCatalogEntry();
             entry.setAbout(poc);
             poc.setCatalog(entry);
 
             entry.setDescription("Grid Service Point of Contact");
             if (poc instanceof ResearchCenterPointOfContact)
                 entry.setDescription("Research Center Point of Contact");
            entry.setCreatedAt(getTimestampProvider().getTimestamp());
         } else
             logger
                     .debug("Catalog entry already exists for POC.");
 
         if (!entry.isPublished()) {
             logger
                     .debug("Catalog entry has not been published. Will sync with domain object");
 
             entry.setEmailAddress(BeanUtils.traverse(poc,
                     "person.emailAddress"));
             entry.setLastName(BeanUtils.traverse(poc, "person.lastName"));
             entry.setFirstName(BeanUtils.traverse(poc, "person.firstName"));
             entry.setName(entry.getFirstName() + " " + entry.getLastName());
             entry.setPhoneNumber(BeanUtils.traverse(poc, "person.phoneNumber"));
 
             Person p = poc.getPerson();
             if (p != null) {
                 if (p.getAddresses() != null && p.getAddresses().size() > 0) {
                     Address address = poc.getPerson().getAddresses().get(0);
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
         super.save(entry);
         return entry;
     }
 }
