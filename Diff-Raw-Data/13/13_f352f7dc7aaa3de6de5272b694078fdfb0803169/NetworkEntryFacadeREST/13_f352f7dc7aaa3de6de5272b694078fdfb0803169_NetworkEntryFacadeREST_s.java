 package eu.justas.geoip.rest;
 
 import au.com.bytecode.opencsv.CSVReader;
 import eu.justas.geoip.model.NetworkEntry;
 import eu.justas.geoip.utils.InjectedConfiguration;
 import eu.justas.geoip.utils.IpCalculator;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.ParameterExpression;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
 /**
  *
  * @author justas
  */
 @Stateless
 @Path("network")
 public class NetworkEntryFacadeREST extends AbstractFacade<NetworkEntry> {
     
     private static final Logger LOG = Logger.getLogger(NetworkEntryFacadeREST.class.getName());
     @PersistenceContext(unitName = "geoip-PU")
     private EntityManager em;
    @EJB
    LocationFacadeREST locationFacadeREST;
     @Inject
     @InjectedConfiguration(key = "import.blocks.file", defaultValue = "/tmp/GeoLiteCity-Blocks.csv")
     String importFile;
     
     public NetworkEntryFacadeREST() {
         super(NetworkEntry.class);
     }
     
     @GET
     @Path("{ip}")
     @Produces({"application/json", "application/xml"})
     public NetworkEntry find(@PathParam("ip") String ip) {
         
         Long ipNumber = 0L;
         try {
             ipNumber = IpCalculator.getLong(ip);
         } catch (UnknownHostException ex) {
             LOG.log(Level.SEVERE, ex.getMessage());
             return null;
         }
         
         CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
         
         CriteriaQuery<NetworkEntry> cq = cb.createQuery(NetworkEntry.class);
         
         Root<NetworkEntry> rootNetworkEntry = cq.from(NetworkEntry.class);
         
         ParameterExpression<Long> ipParam = cb.parameter(Long.class, "ipParam");
         
         Predicate predicate1 = cb.lt(rootNetworkEntry.<Long>get("startIp"), ipParam);
         Predicate predicate2 = cb.gt(rootNetworkEntry.<Long>get("endIp"), ipParam);
         
         cq.where(predicate1, predicate2);
         
         Query q = getEntityManager().createQuery(cq);
         q.setParameter("ipParam", (Long) ipNumber);
         NetworkEntry networkEntry = (NetworkEntry) q.getSingleResult();
         networkEntry.setStartIpString(IpCalculator.longToIp(networkEntry.getStartIp()));
         networkEntry.setEndIpString(IpCalculator.longToIp(networkEntry.getEndIp()));
         return networkEntry;
         
     }
     
     @GET
     @Path("import")
     public String doImport() {
         long startTime = System.currentTimeMillis();
         long count = 0;
         CSVReader reader = null;
         
         try {
             reader = new CSVReader(new FileReader(importFile));
             String[] string;
             reader.readNext();
             LOG.log(Level.INFO, "Started network import");
             
             reader.readNext();
             string = reader.readNext();
             while (string != null && string.length > 0) {
                 count++;
                 if ((count % 100000) == 0) {
                     LOG.log(Level.INFO, "Network import in progress: {0}", count);
                 }
                 Long startIp = Long.valueOf(string[0]);
                 Long endId = Long.valueOf(string[1]);
                 Long locationId = Long.valueOf(string[2]);
                 NetworkEntry networkEntry = new NetworkEntry(startIp, endId, null);
                 networkEntry.setLocationId(locationId);
                 super.create(networkEntry);
                 string = reader.readNext();
                 
             }
             
         } catch (Exception ex) {
             
             LOG.log(Level.SEVERE, null, ex);
         } finally {
             LOG.log(Level.INFO, "Network import count: {0}", count);
             
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException ex) {
                     LOG.log(Level.SEVERE, null, ex);
                 }
             }
         }
         long endTime = System.currentTimeMillis();
         long seconds = (endTime - startTime) / 1000;
         LOG.log(Level.INFO, "Network import took: {0} s", seconds);
         return "Done";
         
     }
     
     @GET
     @Path("count")
     @Produces("text/plain")
     public String countREST() {
         return String.valueOf(super.count());
     }
     
     @Override
     protected EntityManager getEntityManager() {
         return em;
     }
 }
