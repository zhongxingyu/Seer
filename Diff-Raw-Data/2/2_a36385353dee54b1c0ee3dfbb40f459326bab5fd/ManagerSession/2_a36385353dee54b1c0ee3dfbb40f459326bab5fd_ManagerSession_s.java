 package session;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import rental.Car;
 import rental.CarRentalCompany;
 import rental.CarType;
 import rental.Reservation;
 
 @Stateless
 public class ManagerSession implements ManagerSessionRemote {
     
     @PersistenceContext
     EntityManager em;
     
     @Override
     public Set<CarType> getCarTypes(String companyName) {
         CarRentalCompany company = em.find(CarRentalCompany.class, companyName);
         if(company == null) {
             Logger.getLogger(ManagerSession.class.getName())
                     .log(Level.SEVERE, null, "Company not found: "+companyName);
             return null;
         }
         return new HashSet<CarType>(company.getAllTypes());
     }
 
     @Override
     public Set<Integer> getCars(String companyName, String type) {
         CarRentalCompany company = em.find(CarRentalCompany.class, companyName);
         if(company == null) {
             Logger.getLogger(ManagerSession.class.getName())
                     .log(Level.SEVERE, null, "Company not found: "+companyName);
             return null;
         }
         Set<Integer> out = new HashSet<Integer>();
         for(Car car : company.getCars(type)) {
             out.add(car.getId());
         }
         return out;
     }
 
     @Override
     public Set<Reservation> getReservations(String companyName, String type, int id) {
         CarRentalCompany company = em.find(CarRentalCompany.class, companyName);
         if(company == null) {
             Logger.getLogger(ManagerSession.class.getName())
                     .log(Level.SEVERE, null, "Company not found: "+companyName);
             return null;
         }
         return company.getCar(id).getReservations();
     }
 
     @Override
     public Set<Reservation> getReservations(String companyName, String type) {
         CarRentalCompany company = em.find(CarRentalCompany.class, companyName);
         if(company == null) {
             Logger.getLogger(ManagerSession.class.getName())
                     .log(Level.SEVERE, null, "Company not found: "+companyName);
             return null;
         }
         
         Set<Reservation> out = new HashSet<Reservation>();
         for(Car c: company.getCars(type)){
             out.addAll(c.getReservations());
         }
         return out;
     }
 
     @Override
     public Set<Reservation> getReservationsBy(String renter) {
         List<CarRentalCompany> companies = em.createQuery(
                "SELECT comp FROM Company comp", CarRentalCompany.class)
                 .getResultList();
         Set<Reservation> out = new HashSet<Reservation>();
         for(CarRentalCompany crc : companies) {
             out.addAll(crc.getReservationsBy(renter));
         }
         return out;
     }
 
     @Override
     public void addCompany(String companyName) {
         CarRentalCompany company = new CarRentalCompany(companyName, Collections.<Car>emptyList());
         em.persist(company);
     }
 
     @Override
     public void addCarType(CarType carType) {
         em.persist(carType);
     }
 
     @Override
     public void addCar(String carTypeName, String ownerCompanyName) {
         CarType carType = em.find(CarType.class, carTypeName);
         Car car = new Car(0, carType);
         CarRentalCompany ownerCompany = em.find(CarRentalCompany.class, ownerCompanyName);
         if(ownerCompany == null) {
             Logger.getLogger(ManagerSession.class.getName())
                     .log(Level.SEVERE, null, "Company not found: "+ownerCompanyName);
             return;
         }
         ownerCompany.addCar(car);
         em.persist(ownerCompany);
     }
     
 }
