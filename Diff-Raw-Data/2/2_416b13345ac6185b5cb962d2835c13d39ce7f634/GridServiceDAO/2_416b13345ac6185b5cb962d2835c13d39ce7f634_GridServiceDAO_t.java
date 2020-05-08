 package gov.nih.nci.gss.util;
 
 import gov.nih.nci.gss.api.JSONDataService;
 import gov.nih.nci.gss.domain.DomainClass;
 import gov.nih.nci.gss.domain.GridService;
 import gov.nih.nci.gss.domain.HostingCenter;
 import gov.nih.nci.gss.support.LastRefresh;
 import gov.nih.nci.system.applicationservice.ApplicationException;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 /**
  * Common queries for retrieving services and hosts from GSS.
  * 
  * @author <a href="mailto:rokickik@mail.nih.gov">Konrad Rokicki</a>
  */
 public class GridServiceDAO {
     
     public static final String GET_LAST_REFRESH_HQL = 
        "select refresh from gov.nih.nci.gss.support.LastRefresh refresh"; 
         
     public static final String GET_COUNTS_HQL = 
         "select d.domainPackage, d.className, s.identifier, sum(d.count) " +
         "from gov.nih.nci.gss.domain.DomainClass d " +
         "left join d.model m " +
         "left join m.dataServices s " +
         "group by d.domainPackage, d.className, s.url " +
         "having sum(d.count) > 0 ";
 
     public static final String GET_COUNTS_BY_SERVICE_HQL = 
         "select s.identifier, d.domainPackage, d.className, d.count, d.countError " +
         "from gov.nih.nci.gss.domain.DomainClass d " +
         "left join d.model m " +
         "left join m.dataServices s ";
 
     public static final String GET_DATA_CLASS_HQL = 
         "select d " +
         "from gov.nih.nci.gss.domain.DomainClass d " +
         "left join d.model m " +
         "left join m.dataServices s " +
         "where s.identifier = ? ";
 
     public static final String GET_DATA_CLASS_WHERE_HQL = 
         "and d.domainPackage = ? " +
         "and d.className = ? ";
 
     public static final String GET_AGGR_COUNTS_HQL = 
         "select d.domainPackage, d.className, sum(d.count) " +
         "from gov.nih.nci.gss.domain.DomainClass d " +
         "group by d.domainPackage, d.className " +
         "having sum(d.count) > 0 ";
 
 	public static final String GET_SERVICE_HQL = 
         "select service from gov.nih.nci.gss.domain.GridService service " +
         "left join fetch service.hostingCenter ";
 
 	public static final String GET_HOST_HQL = 
         "select host from gov.nih.nci.gss.domain.HostingCenter host ";
 
     private static Logger log = Logger.getLogger(JSONDataService.class);
     
     public static Map<String,Map<String,Long>> getClassCounts(Session s) 
                 throws ApplicationException {
         
         Map<String,Map<String,Long>> counts = new HashMap<String,Map<String,Long>>();
         
         // Create the HQL query
         StringBuffer hql = new StringBuffer(GET_COUNTS_HQL);
         
         // Create the Hibernate Query
         Query q = s.createQuery(hql.toString());
         
         // Execute the query
         List<Object[]> rows = q.list();
 
         // Create count map
         for(Object[] row : rows) {
             Map<String,Long> classCounts = null;
             String className = row[0]+"."+row[1];
             if (counts.containsKey(className)) {
                 classCounts = counts.get(className);
             }
             else {
                 classCounts = new HashMap<String,Long>();
                 counts.put(className, classCounts);
             }
             classCounts.put((String)row[2],(Long)row[3]);
         }
         
         return counts;
     }
     
     public static List<DomainClass> getDomainClasses(Session s,
         String serviceId, String fullClassName) 
                 throws ApplicationException {
         
         // Create the HQL query
         StringBuffer hql = new StringBuffer(GET_DATA_CLASS_HQL);
         
         if (fullClassName != null) {
             hql.append(GET_DATA_CLASS_WHERE_HQL);
         }
         
         // Create the Hibernate Query
         Query q = s.createQuery(hql.toString());
         q.setString(0, serviceId);
         
         if (fullClassName != null) {
             int d = fullClassName.lastIndexOf('.');
             String packageName = fullClassName.substring(0, d);
             String className = fullClassName.substring(d+1);
             q.setString(1, packageName);
             q.setString(2, className);
         }
         
         // Execute the query
         List<DomainClass> rows = q.list();
         return rows;
     }
     
     public static Map<String,Map<String,Object>> getClassCountsByServer(Session s) 
                 throws ApplicationException {
         
         Map<String,Map<String,Object>> counts = new HashMap<String,Map<String,Object>>();
         
         // Create the HQL query
         StringBuffer hql = new StringBuffer(GET_COUNTS_BY_SERVICE_HQL);
         
         // Create the Hibernate Query
         Query q = s.createQuery(hql.toString());
         
         // Execute the query
         List<Object[]> rows = q.list();
 
         // Create count map
         for(Object[] row : rows) {
             Map<String,Object> serverCounts = null;
             String serverId = (String)row[0];
             String className = row[1]+"."+row[2];
             Long count = (Long)row[3];
             String countError = (String)row[4];
             if (counts.containsKey(serverId)) {
                 serverCounts = counts.get(serverId);
             }
             else {
                 serverCounts = new HashMap<String,Object>();
                 counts.put(serverId, serverCounts);
             }
             if (count != null) {
                 serverCounts.put(className,count);
             }
             else {
                 serverCounts.put(className,countError);
             }
         }
         
         return counts;
     }
     
     /**
      * TODO: This can probably be removed.
      * @param s
      * @return
      * @throws ApplicationException
      */
     public static Map<String,Long> getAggregateClassCounts(Session s) 
                 throws ApplicationException {
         
         Map<String,Long> counts = new HashMap<String,Long>();
         
         // Create the HQL query
         StringBuffer hql = new StringBuffer(GET_AGGR_COUNTS_HQL);
         
         // Create the Hibernate Query
         Query q = s.createQuery(hql.toString());
         
         // Execute the query
         List<Object[]> rows = q.list();
 
         // Create count map
         for(Object[] row : rows) {
             counts.put(row[0]+"."+row[1],(Long)row[2]);
         }
         
         return counts;
      }
     
     public static List<GridService> getServices(String serviceId, Session s) 
 	            throws ApplicationException {
 
         // Create the HQL query
         StringBuffer hql = new StringBuffer(GET_SERVICE_HQL);
         if (serviceId != null) hql.append("where service.identifier = ?");
         
         // Create the Hibernate Query
         Query q = s.createQuery(hql.toString());
         if (serviceId != null) q.setString(0, serviceId);
         
         // Execute the query
         return q.list();
      }
 
     public static List<HostingCenter> getHosts(String hostId, Session s) 
 	    		throws ApplicationException {
 	
         // Create the HQL query
         StringBuffer hql = new StringBuffer(GET_HOST_HQL);
         if (hostId != null) hql.append("where host.identifier = ?");
         
         // Create the Hibernate Query
         Query q = s.createQuery(hql.toString());
         if (hostId != null) q.setString(0, hostId);
         
         // Execute the query
 		return q.list();
 	}
 
     public static LastRefresh getLastRefreshObject(Session s) 
                 throws ApplicationException {
     
         // Create the HQL query
         StringBuffer hql = new StringBuffer(GET_LAST_REFRESH_HQL);
         
         // Create the Hibernate Query
         Query q = s.createQuery(hql.toString());
         
         // Execute the query
         List<LastRefresh> results = q.list();
         
         if (results.size() == 0) {
             return new LastRefresh();
         }
         
         if (results.size() > 1) {
             log.warn("More than one LastStatus object found.");
         }
         
         return results.get(0);
     }
 }
