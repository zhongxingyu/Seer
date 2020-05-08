 package eu.comexis.napoleon.server.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.UUID;
 
 import com.googlecode.objectify.Key;
 
 import eu.comexis.napoleon.shared.model.City;
 import eu.comexis.napoleon.shared.model.Company;
 import eu.comexis.napoleon.shared.model.Country;
 import eu.comexis.napoleon.shared.model.JobTitle;
 import eu.comexis.napoleon.shared.model.Nationality;
 import eu.comexis.napoleon.shared.model.Owner;
 import eu.comexis.napoleon.shared.model.RealEstate;
 import eu.comexis.napoleon.shared.model.simple.SimpleOwner;
 
 public class OwnerDao extends NapoleonDao<Owner> {
   public OwnerDao() {
     super();
     // TODO Auto-generated constructor stub
   }
 
   public Owner create(String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     Owner owner = new Owner();
     System.out.println("Set company key " + companyKey.toString());
     owner.setCompany(companyKey);
     return owner;
   }
 
   /**
    * Retrieves the list of all owners stored in the database (with the minimum of data to be shown
    * in UI)
    * 
    * @return The list of owners
    */
   public ArrayList<SimpleOwner> getListSimpleOwners(String companyId) {
     LOG.info("Get list Owners (" + clazz + ") for company " + companyId);
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     Iterator<Owner> iterator = this.listAll(companyId).iterator();
     ArrayList<SimpleOwner> owners = new ArrayList<SimpleOwner>();
     while (iterator.hasNext()) {
       Owner owner = iterator.next();
       SimpleOwner o = new SimpleOwner();
       o.setId(owner.getId());
       o.setName(owner.getLastName());
       o.setCity(owner.getCity());
       o.setPostalCode(owner.getPostalCode());
      o.setAddress(owner.getStreet() + ", " + owner.getNumber() + (owner.getBox()!=null ? " bte " + owner.getBox():""));
       o.setMobileNumber(owner.getMobilePhoneNumber());
       o.setPhoneNumber(owner.getPhoneNumber());
       owners.add(o);
     }
     return owners;
   }
   @Override
   public Owner update(Owner owner) {
     if (owner.getCompany() != null){
       return update(owner,owner.getCompany());
     }else{
       // log error
       LOG.fatal("Parent Company is not set, cannot save owner");
       return null;
     }
   }
   public Owner update(Owner owner, String companyId) {
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     return update(owner,companyKey);
   }
   public Owner update(Owner owner, Key<Company> companyKey) {
     String ownerId = owner.getId();
     CountryDao countryData = new CountryDao();
     if (ownerId == null || ownerId.length() == 0) {
       UUID uuid = UUID.randomUUID();
       System.out.println("Creating Uuid " + uuid.toString());
       owner.setId(uuid.toString());
       owner.setCompany(companyKey);
       owner.setFlagActivated(true);
     }
     // if country does not exist, create it.
     Country country = countryData.getByName(owner.getCountry(), companyKey);
     if (country == null) {
       country = countryData.create(companyKey);
       country.setName(owner.getCountry());
       countryData.update(country);
     }
     City city = countryData.getCityByFullName(country.getId(), owner.getCity(),owner.getPostalCode());
     if (city == null) {
       city = countryData.addCity(country.getId(), owner.getCity(),owner.getPostalCode());
     }
     if (owner.getNationality()!= null && !owner.getNationality().isEmpty()){
       NationalityDao natDao = new NationalityDao();
       Nationality nat = new Nationality();
       nat.setName(owner.getNationality());
       nat.setCompany(companyKey);
       natDao.update(nat);
     }
     if (owner.getJobTitle()!= null && !owner.getJobTitle().isEmpty()){
       JobTitleDao jobDao = new JobTitleDao();
       JobTitle job = new JobTitle();
       job.setName(owner.getJobTitle());
       job.setCompany(companyKey);
       jobDao.update(job);
     }
     return super.update(owner);
   }
   @Override
   public Owner getById(String ownerId,String companyId){
     Owner o = super.getById(ownerId, companyId);
     RealEstateDao eDao = new RealEstateDao();
     o.setEstates(eDao.getListSimpleRealEstatesForOwner(companyId, ownerId));
     return o;
   }
   public Key<Owner> getOwnerKey(String ownerId,String companyId){
     Key<Company> companyKey = new Key<Company>(Company.class, companyId);
     Key<Owner> ownerKey = new Key<Owner>(companyKey,Owner.class, ownerId);
     return ownerKey;
   }
 }
