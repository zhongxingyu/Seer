 /**
  * 
  */
 package eu.comexis.napoleon.server.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Query;
 import com.googlecode.objectify.util.DAOBase;
 
 import eu.comexis.napoleon.shared.model.AcademicYear;
 import eu.comexis.napoleon.shared.model.Company;
 import eu.comexis.napoleon.shared.model.FeeUnit;
 import eu.comexis.napoleon.shared.model.Lease;
 import eu.comexis.napoleon.shared.model.Owner;
 import eu.comexis.napoleon.shared.model.RealEstate;
 import eu.comexis.napoleon.shared.model.Tenant;
 import eu.comexis.napoleon.shared.model.simple.SimpleLease;
 import eu.comexis.napoleon.shared.model.simple.SimpleRealEstate;
 import eu.comexis.napoleon.shared.model.simple.SimpleTenant;
 
 /**
  * @author xavier
  * 
  */
 public class LeaseDao extends DAOBase {
   public static Log LOG = LogFactory.getLog(LeaseDao.class);
 
 
   public LeaseDao() {
     // TODO Auto-generated constructor stub
   }
 
   public List<SimpleLease> getAllLease(Key<Company> companyKey) {
     // create the list to return
     List<SimpleLease> leaseList = new ArrayList<SimpleLease>();
     // get a query with all the estates
     Query<RealEstate> q = ofy().query(RealEstate.class);
     // set the parent to the given company
     q.ancestor(companyKey);
     // loop on each keys and get the corresponding leases
     for (Key<RealEstate> estateKey : q.listKeys()) {
       for (SimpleLease sl : getSimpleLeaseForRE(estateKey)) {
         leaseList.add(sl);
       }
     }
     return leaseList;
   }
 
   public List<SimpleLease> getAllLease(String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     return getAllLease(companyKey);
   }
 
   public List<SimpleLease> getAllLease(String realEstateId, String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     Key<RealEstate> estateKey = new Key<RealEstate>(companyKey, RealEstate.class, realEstateId);
     return getSimpleLeaseForRE(estateKey);
   }
 
   public Lease getById(String leaseId, Key<RealEstate> estateKey) {
     Lease l = null;
     if (leaseId.equals("new")){
       l = new Lease();
     }else{
       l = ofy().find(new Key<Lease>(estateKey, Lease.class, leaseId));
     }
     if (l != null) {
       RealEstate realEstate = ofy().find(estateKey);
       Owner o = ofy().find(realEstate.getOwnerKey());
       
       SimpleRealEstate se = new SimpleRealEstate();
       se.setReference(realEstate.getReference());
       se.setId(realEstate.getId());
       se.setOwner(o.getLastName());
       l.setRealEstate(se);
      if (l.getFee()==null && o.getUnit().equals(FeeUnit.LUMP_SUM)){
         l.setFee(o.getFee().floatValue());
       }
       l.setFeeFromOwner(o.getFee().floatValue());
       l.setUnit(o.getUnit());
       if (l.getTenantKey()!=null){
         Tenant t = ofy().find(l.getTenantKey());
         SimpleTenant st = new SimpleTenant();
         st.setId(t.getId());
         st.setName(t.getLastName());
         st.setAddress(t.getStreet());
         st.setCity(t.getCity());
         st.setPostalCode(t.getPostalCode());
         st.setPhoneNumber(t.getPhoneNumber());
         st.setMobileNumber(t.getMobilePhoneNumber());
         l.setTenant(st);
       }
     }
     return l;
   }
 
   public Lease getById(String leaseId, String realEstateId, String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstateId);
     return getById(leaseId, estateKey);
   }
 
   public Lease getLastLeaseForRE(String realEstateId) {
     return null;
   }
 
   public List<Lease> getLeaseForRE(Key<RealEstate> estateKey) {
     // get the lease for the company
     Query<Lease> ql = ofy().query(Lease.class);
     ql.ancestor(estateKey);
     return ql.list();
   }
 
   public RealEstate getRealEstate(Lease l) {
     return ofy().get(l.getRealEstateKey());
   }
 
   public List<SimpleLease> getSimpleLeaseForRE(Key<RealEstate> estateKey) {
     // create the list to return
     List<SimpleLease> leaseList = new ArrayList<SimpleLease>();
     // get the owner
     RealEstate e = ofy().find(estateKey);
     Owner o = ofy().find(e.getOwnerKey());
     // get the lease for the company
     Query<Lease> ql = ofy().query(Lease.class);
     ql.ancestor(estateKey);
     // loop on each lease
     for (Lease l : ql.list()) {
       // create a SimpleLease and add it to the SimleLease list
       Tenant t = ofy().find(l.getTenantKey());
       SimpleLease sl = new SimpleLease();
       sl.setId(l.getId());
       sl.setRealEstateId(e.getId());
       sl.setRealEstateRef(e.getReference());
       sl.setOwnerId(o.getId());
       sl.setOwnerName(o.getLastName());
       sl.setAcademicYear(l.getAcademicYear());
       sl.setTenantId(t.getId());
       sl.setTenantName(t.getLastName());
       sl.setStartDate(l.getStartDate());
       sl.setEndDate(l.getEndDate());
       leaseList.add(sl);
     }
     return leaseList;
   }
 
   public Tenant getTenant(Lease l) {
     return ofy().get(l.getTenantKey());
   }
 
   public Lease isAlreadyRented(String id,Key<RealEstate> estateKey, Date startDate, Date endDate) {
     Query<Lease> q = ofy().query(Lease.class);
     q.ancestor(estateKey);
     List<Lease> leases = q.filter("endDate >=", startDate).order("endDate").list();
     for (Lease l:leases){
       if (!l.getId().equals(id) && l.getStartDate().compareTo(endDate) <= 0){
         return l;
       }
     }
     return null;
   }
 
   public Lease isAlreadyRented(String id,String realEstateId, Date startDate, Date endDate) {
     Key<RealEstate> estateKey = new Key<RealEstate>(RealEstate.class, realEstateId);
     return isAlreadyRented(id,estateKey, startDate, endDate);
   }
 
   public List<Lease> listAll(Key<Company> companyKey) {
     // create the list to return
     List<Lease> leaseList = new ArrayList<Lease>();
     // get a query with all the estates
     Query<RealEstate> q = ofy().query(RealEstate.class);
     // set the parent to the given company
     q.ancestor(companyKey);
     // loop on each keys and get the corresponding leases
     for (Key<RealEstate> estateKey : q.listKeys()) {
       for (Lease l : getLeaseForRE(estateKey)) {
         leaseList.add(l);
       }
     }
     return leaseList;
   }
 
   public List<Lease> listAll(String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     return listAll(companyKey);
   }
   public Lease update(Lease lease,String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     return update(lease,companyKey);
   }
   public Lease update(Lease lease,Key<Company> companyKey) {
     LOG.info("Update Lease");
     String leaseId = lease.getId();
     Key<RealEstate> estateKey = null;
     // create unique id if new entity
     
     if (leaseId == null || leaseId.length() == 0) {
       UUID uuid = UUID.randomUUID();
       System.out.println("Creating Uuid " + uuid.toString());
       lease.setId(uuid.toString());
     }
     // set parent
     if (lease.getRealEstateKey() == null) {
       if (lease.getRealEstate() != null) {
         estateKey =
             new Key<RealEstate>(companyKey,RealEstate.class, lease.getRealEstate().getId());
         lease.setRealEstateKey(estateKey);
         
       } else {
         LOG.fatal("Lease cannot be updated, missing parent RealEstate");
         return null;
       }
     }else{
       estateKey =lease.getRealEstateKey();
     }
     Lease actualLease =isAlreadyRented(lease.getId(),estateKey,lease.getStartDate(),lease.getEndDate());
     if (actualLease!=null){
       LOG.info("Lease cannot be updated because the real estate is already rented by " + actualLease.getId());
       return null;
     }
     // set tenant
     //if (lease.getTenantKey() == null) {
       if (lease.getTenant() != null) {
         Key<Tenant> tenantKey = new Key<Tenant>(companyKey,Tenant.class, lease.getTenant().getId());
         lease.setTenantKey(tenantKey);
       } else {
         LOG.error("Lease cannot be updated, missing tenant");
         return null;
       }
     //}
     if (lease.getAcademicYear()!=null && !lease.getAcademicYear().isEmpty()){
       AcademicYearDao ayDao = new AcademicYearDao();
       AcademicYear year = new AcademicYear();
       year.setCompany(companyKey);
       year.setName(lease.getAcademicYear());
       ayDao.update(year);
     }
     try {
       Key<Lease> leaseKey = ofy().put(lease);
       LOG.info("Lease has been updated");
       return getById(lease.getId(),lease.getRealEstateKey());
     } catch (Exception e) {
       LOG.fatal("Lease cannot be updated: ", e);
       return null;
     }
   }
 
 }
