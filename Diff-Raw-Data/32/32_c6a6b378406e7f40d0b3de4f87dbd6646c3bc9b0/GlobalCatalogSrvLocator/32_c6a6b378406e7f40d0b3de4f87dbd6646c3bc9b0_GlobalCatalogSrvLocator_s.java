 /**
  *
  */
 package org.ovirt.engine.core.ldap;
 
import javax.naming.NamingException;

 import org.ovirt.engine.core.utils.log.Log;
 import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.dns.DnsSRVLocator;
 
 /**
  * This class is responsible for querying the AD global catalog
  *
  */
 public class GlobalCatalogSrvLocator extends DnsSRVLocator {
 
    private static final String GLOBAL_CATALOG_QUERY_PREFIX = "_ldap._tcp.gc._msdcs.";
 
     public GlobalCatalogSrvLocator() {
     }
 
     public DnsSRVResult getGlobalCatalog(String domainName) {
         try {
            return getService(GLOBAL_CATALOG_QUERY_PREFIX + domainName);
        } catch (NamingException e) {
             log.error("Error in getting global catalog for " + domainName);
             return null;
         }

     }
 
     private static Log log = LogFactory.getLog(GlobalCatalogSrvLocator.class);
 
 }
