 package gov.nih.nci.cagrid.portal.dao.catalog;
 
 import gov.nih.nci.cagrid.portal.annotation.UpdatesCatalogs;
 import gov.nih.nci.cagrid.portal.dao.AbstractDao;
 import gov.nih.nci.cagrid.portal.domain.AbstractDomainObject;
 import gov.nih.nci.cagrid.portal.domain.catalog.CatalogEntry;
 import gov.nih.nci.cagrid.portal.util.TimestampProvider;
 import org.springframework.beans.factory.annotation.Required;
 
 import java.util.Date;
 
 /**
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 public abstract class AbstractCatalogEntryDao<T extends AbstractDomainObject> extends AbstractDao<T> {
 
     TimestampProvider timestampProvider;
 
 
     @Override
     /**
      * Saves the catalog entry with the current timestamp
      */
     public void save(T entry) {
         if (entry instanceof CatalogEntry) {
             CatalogEntry ent = (CatalogEntry) entry;
             Date now = timestampProvider.getTimestamp();
             ent.setUpdatedAt(now);
            if (ent.getCreatedAt() == null)
                 logger.debug("No create date for catalog. Will add now");
            ent.setCreatedAt(now);
         }
         super.save(entry);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     @UpdatesCatalogs
     public void delete(T domainObject) {
         super.delete(domainObject);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     public TimestampProvider getTimestampProvider() {
         return timestampProvider;
     }
 
     @Required
     public void setTimestampProvider(TimestampProvider timestampProvider) {
         this.timestampProvider = timestampProvider;
     }
 }
