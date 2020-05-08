 package hu.sztaki.ilab.longneck.dns;
 
 import hu.sztaki.ilab.longneck.Record;
 import hu.sztaki.ilab.longneck.dns.db.DnsCache;
 import hu.sztaki.ilab.longneck.dns.db.LookupResult;
 import hu.sztaki.ilab.longneck.dns.db.ReverseData;
 import hu.sztaki.ilab.longneck.process.AbstractSourceInfoContainer;
 import hu.sztaki.ilab.longneck.process.CheckError;
 import hu.sztaki.ilab.longneck.process.VariableSpace;
 import hu.sztaki.ilab.longneck.process.block.Block;
 import hu.sztaki.ilab.longneck.process.block.BlockUtils;
 import hu.sztaki.ilab.longneck.process.constraint.CheckResult;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import org.apache.log4j.Logger;
 
 
 /**
  * Reverse DNS lookup block.
  *
  * @author Bendig Loránd <lbendig@ilab.sztaki.hu>
  * @author Molnár Péter <molnar.peter@sztaki.mta.hu>
  *
  */
 public class ReverseDns extends AbstractSourceInfoContainer implements Block {
     /** The log. */
 	private final Logger LOG = Logger.getLogger(ReverseDns.class);
 
     /** field names */
     private static String IPFIELD = "ipAddress" ;
     private static String HOSTNAMEFIELD = "domainName" ;
     private static String EXPIRYFIELD = "lookupExpiry" ;
 
     /** The date format used to output expiration date. */
     private final SimpleDateFormat dateFormat =
             new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH);
 
 	/** The ip address field in dotted-quad format. */
     private String ipAddress;
     /** The domain name which is resolved to an ip address. */
 	private String domainTo;
 	/** The expiration date of the resolved ip address. */
 	private String expirationDateTo;
 
     /** Lookup service factory. */
     private LookupServiceFactory lookupServiceFactory;
     /** Lookup service for dns queries. */
     private LookupService lookupService = null;
 
     /** Cache of dns query results. */
     private DnsCache dnsCache;
 
     @Override
     public void apply(final Record record, VariableSpace variables) throws CheckError {
 
         // Get thread-local lookup service
         if (lookupService == null) {
             lookupService = lookupServiceFactory.getLookupService();
         }
 
         try {
             String ipAddr = BlockUtils.getValue(IPFIELD, record, variables);
 
             if (ipAddr == null || "".equals(ipAddr)) {
                 LOG.warn("IP address (to be reverse resolved) is null or empty.");
                 throw new CheckError(new CheckResult(this, false, ipAddress, ipAddr,
                         "Cannot resolve empty ip address."));
             }
 
             // Query from cache
             ReverseData reverseData = dnsCache.getReverse(ipAddr);
 
             // Check result and do a dns lookup if necessary
             if (reverseData == null ||
                     Calendar.getInstance().getTimeInMillis() > reverseData.getExpirationDate()) {
                 reverseData = lookupService.getReverseDns(ipAddr);
 
                 // Check returned value
                 if (reverseData != null) {
                     dnsCache.add(reverseData);
                 }
             }
 
             if (reverseData != null && LookupResult.OK.equals(reverseData.getResult())) {
                 BlockUtils.setValue(HOSTNAMEFIELD, reverseData.getDomain(), record, variables);
                 BlockUtils.setValue(EXPIRYFIELD,
                     dateFormat.format(new Date(reverseData.getExpirationDate())),
                     record, variables);
             }
             
             // it is considered to be normal, if reverse lookup fails (no hostname)            
         } catch (RuntimeException ex) {
            LOG.error("DNS lookup failed.", ex);
         }
     }
 
     @Override
     public ReverseDns clone() {
         return (ReverseDns) super.clone();
     }
 
     public String getIpAddress() {
         return ipAddress;
     }
 
     public void setIpAddress(String ipAddress) {
         this.ipAddress = ipAddress;
     }
 
     public String getDomainTo() {
         return domainTo;
     }
 
     public void setDomainTo(String domainTo) {
         this.domainTo = domainTo;
     }
 
     public String getExpirationDateTo() {
         return expirationDateTo;
     }
 
     public void setExpirationDateTo(String expirationDateTo) {
         this.expirationDateTo = expirationDateTo;
     }
 
     public LookupServiceFactory getLookupServiceFactory() {
         return lookupServiceFactory;
     }
 
     public void setLookupServiceFactory(LookupServiceFactory lookupServiceFactory) {
         this.lookupServiceFactory = lookupServiceFactory;
     }
 
     public LookupService getLookupService() {
         return lookupService;
     }
 
     public void setLookupService(LookupService lookupService) {
         this.lookupService = lookupService;
     }
 
     public DnsCache getDnsCache() {
         return dnsCache;
     }
 
     public void setDnsCache(DnsCache dnsCache) {
         this.dnsCache = dnsCache;
     }
 }
