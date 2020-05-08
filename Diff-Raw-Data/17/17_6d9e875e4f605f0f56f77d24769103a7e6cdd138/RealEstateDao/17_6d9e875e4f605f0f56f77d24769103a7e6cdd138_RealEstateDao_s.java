 package eu.comexis.napoleon.server.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.UUID;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Query;
 
 import eu.comexis.napoleon.server.manager.UserManager;
 import eu.comexis.napoleon.shared.model.Association;
 import eu.comexis.napoleon.shared.model.City;
 import eu.comexis.napoleon.shared.model.Company;
 import eu.comexis.napoleon.shared.model.Condo;
 import eu.comexis.napoleon.shared.model.Country;
 import eu.comexis.napoleon.shared.model.Owner;
 import eu.comexis.napoleon.shared.model.Ownership;
 import eu.comexis.napoleon.shared.model.RealEstate;
 import eu.comexis.napoleon.shared.model.Tenant;
 import eu.comexis.napoleon.shared.model.simple.SimpleOwner;
 import eu.comexis.napoleon.shared.model.simple.SimpleRealEstate;
 
 public class RealEstateDao extends NapoleonDao<RealEstate> {
   public RealEstateDao() {
     super();
     // TODO Auto-generated constructor stub
   }
 
   public RealEstate create(Key<Company> companyKey) {
     RealEstate realEstate = new RealEstate();
     System.out.println("Set company key " + companyKey.toString());
     realEstate.setCompany(companyKey);
     return realEstate;
   }
   
  
 
   public RealEstate create(String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     return create(companyKey);
   }
   
   
 
   /**
    * Retrieves the list of all realEstates stored in the database (with the minimum of data to be
    * shown in UI)
    * 
    * @return The list of realEstates
    */
   public ArrayList<SimpleRealEstate> getListSimpleRealEstates(String companyId) {
     Iterator<RealEstate> iterator = this.listAll(companyId).iterator();
     ArrayList<SimpleRealEstate> realEstates = new ArrayList<SimpleRealEstate>();
     while (iterator.hasNext()) {
       RealEstate realEstate = iterator.next();
       SimpleRealEstate e = new SimpleRealEstate();
       e.setId(realEstate.getId());
       e.setReference(realEstate.getReference());
       e.setCity(realEstate.getCity());
      e.setAddress(realEstate.getStreet());
       e.setPostalCode(realEstate.getPostalCode());
       SimpleOwner o = getOwner(realEstate);
       e.setOwner(o.getName());
       e.setMobile(o.getMobileNumber());
       e.setPhoneNumber(o.getPhoneNumber());
       realEstates.add(e);
     }
     return realEstates;
   }
   @Override
   public RealEstate update(RealEstate realEstate) {
     if (realEstate.getCompany() != null){
       return update(realEstate,realEstate.getCompany());
     }else{
       // log error
       LOG.fatal("Parent Company is not set, cannot save realEstate");
       return null;
     }
   }
   public RealEstate update(RealEstate realEstate, String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     return update(realEstate,companyKey);
   }
   public RealEstate update(RealEstate realEstate, Key<Company> companyKey) {
     String realEstateId = realEstate.getId();
     CountryDao countryData = new CountryDao();
     if (realEstateId == null || realEstateId.length() == 0) {
       UUID uuid = UUID.randomUUID();
       System.out.println("Creating Uuid " + uuid.toString());
       realEstate.setId(uuid.toString());
       realEstate.setCompany(companyKey);
       realEstate.setFlagActivated(true);
     }
     // if country does not exist, create it.
     if (realEstate.getCountry()!=null && !realEstate.getCountry().isEmpty()){
       Country country = countryData.getByName(realEstate.getCountry(), companyKey);
       if (country == null) {
         country = countryData.create(companyKey);
         country.setName(realEstate.getCountry());
         countryData.update(country);
       }
       City city = countryData.getCityByFullName(country.getId(), realEstate.getCity(),realEstate.getPostalCode());
       // if city does not exist, create it.
       if (city == null) {
         city = countryData.addCity(country.getId(), realEstate.getCity(),realEstate.getPostalCode());
       }
       if (realEstate.getSquare()!=null && !realEstate.getSquare().isEmpty()){
         ArrayList<String> allSquares = city.getSquareList();
         if (allSquares != null){
           if (!allSquares.contains(realEstate.getSquare())){
             allSquares.add(realEstate.getSquare());
             city.setSquareList(allSquares);
             countryData.UpdateCity(city);
           }
         }else{
           allSquares = new ArrayList<String>();
           allSquares.add(realEstate.getSquare());
           city.setSquareList(allSquares);
           countryData.UpdateCity(city);
         }
       }
     }
     // Enrich condo + association storage for list
     String companyId = UserManager.INSTANCE.getCompanyId();
     if (realEstate.getCondominium()!=null && !realEstate.getCondominium().isEmpty()){
       CondoDao cdoDao = new CondoDao();
       Condo cdo = new Condo();
       cdo.setName(realEstate.getCondominium());
       cdo.setHomeownerAssociation(realEstate.getHomeownerAssociation());
       cdo.setCity(realEstate.getCity());
       cdo.setStreet(realEstate.getStreet());
       cdo.setCountry(realEstate.getCountry());
       cdo.setPostalCode(realEstate.getPostalCode());
       cdo.setSquare(realEstate.getSquare());
       cdo.setNumber(realEstate.getNumber());
       cdo = cdoDao.update(cdo,companyId);
     }
     if (realEstate.getHomeownerAssociation()!=null && !realEstate.getHomeownerAssociation().isEmpty()){
       HomeownerAssocDao assocDao = new HomeownerAssocDao();
       Association assoc = new Association();
       assoc.setName(realEstate.getHomeownerAssociation());
       assoc.setStreet(realEstate.getAssocAdresss());
       assoc.setEmail(realEstate.getAssocEmail());
       assoc.setMobilePhoneNumber(realEstate.getAssocMobilePhoneNumber());
       assoc.setPhoneNumber(realEstate.getAssocPhoneNumber());
       assocDao.update(assoc, companyId);
     }
     if (realEstate.getOwner()!=null){
       setOwner(realEstate, realEstate.getOwner().getId(),companyId,realEstate.getOwnershipDate());
     }
     return super.update(realEstate);
   }
   public Ownership getCurrentOwnership(RealEstate realEstate){
     // get the last ownership
     // return the Ownership
     Query<Ownership> q = ofy().query(Ownership.class);
     q.ancestor(realEstate);
     for(Ownership oship:q.list()){
       if (oship.getToDate()==null){
         return oship;
       }
     }
     return null;
   }
   public void setOwner(RealEstate realEstate,String ownerId,String companyId, Date fromDate){
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     Key<Owner> ownerKey = new Key<Owner>(companyKey,Owner.class, ownerId);
     // get the currentOwner
     Ownership currentOwner = getCurrentOwnership(realEstate);
     if (currentOwner!=null){
       // if the owner is already the current Owner, we will allow the change the from date if that date is
       // not before the "from date" of the previous owner.
       if (currentOwner.getOwnerKey().equals(ownerKey)){
         //currentOwner.setFromDate(fromDate);
         ofy().put(currentOwner);
         //realEstate.setOwnershipDate(fromDate);
       }else{
         // if owner is different and the ownership date is more recent than the previous one
         // we stop the ownership of the previous owner and add a new ownership for the current
         if (currentOwner.getFromDate().before(fromDate)){
           currentOwner.setToDate(fromDate);
           ofy().put(currentOwner);
           currentOwner = new Ownership();
           currentOwner.setOwnerKey(ownerKey);
           Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstate.getId());
           currentOwner.setEstate(estateKey);
           currentOwner.setFromDate(fromDate);
           UUID uuid = UUID.randomUUID();
           currentOwner.setId(uuid.toString());
           ofy().put(currentOwner);
           realEstate.setOwnerKey(ownerKey);
           realEstate.setOwnershipDate(fromDate);
         }else{
           // this should be considered as an error
           
         }
       }
     }else{
       currentOwner = new Ownership();
       currentOwner.setOwnerKey(ownerKey);
       Key<RealEstate> estateKey = new Key<RealEstate>(companyKey,RealEstate.class, realEstate.getId());
       currentOwner.setEstate(estateKey);
       currentOwner.setFromDate(fromDate);
       UUID uuid = UUID.randomUUID();
       currentOwner.setId(uuid.toString());
       ofy().put(currentOwner);
       realEstate.setOwnerKey(ownerKey);
       realEstate.setOwnershipDate(fromDate);
     }
   }
   public SimpleOwner getOwner(RealEstate realEstate){
     SimpleOwner o = new SimpleOwner();
     Owner own = ofy().get(realEstate.getOwnerKey());
     o.setId(own.getId());
     o.setName(own.getLastName());
     o.setMobileNumber(own.getMobilePhoneNumber());
     o.setPhoneNumber(own.getPhoneNumber());
     o.setEmail(own.getEmail());
     return o;
   }
   public ArrayList<SimpleRealEstate> getListSimpleRealEstatesForOwner(String companyId,String ownerId) {
     try {
       Key<Company> companyKey = new Key<Company>(Company.class, companyId);
       Key<Owner> ownerKey = new Key<Owner>(companyKey,Owner.class, ownerId);
       Query<RealEstate> q = ofy().query(RealEstate.class);
       q.ancestor(companyKey);
       ArrayList<SimpleRealEstate> realEstates = new ArrayList<SimpleRealEstate>();
       for(RealEstate e:q.filter("ownerKey", ownerKey).list()){
         SimpleRealEstate se = new SimpleRealEstate();
         se.setId(e.getId());
         se.setReference(e.getReference());
         se.setCity(e.getCity());
         se.setAddress(e.getStreet());
         realEstates.add(se);
       }
       return realEstates;
     } catch (Exception e) {
       return null;
     }
   }
   @Override
   public RealEstate getById(String estateId,String companyId){
     RealEstate e = super.getById(estateId, companyId);
     // load the owner (not supposed to be null)
     SimpleOwner o = getOwner(e);
     e.setOwner(o);
     return e;
   }
 }
