 package smartpool.service;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import smartpool.domain.Buddy;
 import smartpool.domain.Carpool;
 import smartpool.persistence.dao.BuddyDao;
 import smartpool.persistence.dao.CarpoolDao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.apache.commons.lang3.StringUtils.isBlank;
 
 @Service
 public class CarpoolService {
     private final CarpoolDao carpoolDao;
     private final BuddyDao buddyDao;
     private final RouteService routeService;
 
     @Autowired
     public CarpoolService(CarpoolDao carpoolDao, BuddyDao buddyDao, RouteService routeService) {
         this.carpoolDao = carpoolDao;
         this.buddyDao = buddyDao;
         this.routeService = routeService;
     }
 
     public Carpool getByName(String name) {
         Carpool carpool = carpoolDao.get(name);
         if(carpool != null)
             carpool.setBuddies(buddyDao.getBuddyListByCarpoolName(carpool.getName()));
         return carpool;
     }
 
     public Carpool findCarpoolByName(String name) {
         Carpool carpool = carpoolDao.get(name);
         carpool.setBuddies(buddyDao.getBuddyListByCarpoolName(name));
         return carpool;
     }
 
     public List<Carpool> findAllCarpoolsByLocation(String location) {
         if (isBlank(location)) {
             return getAllCarpools();
         }
 
        List<String> carpoolNames = routeService.getCarpoolNameList(location.trim());
         List<Carpool> carpools = new ArrayList<Carpool>();
         for (String name : carpoolNames) {
             carpools.add(findCarpoolByName(name));
         }
         return carpools;
     }
 
     public void insert(Carpool carpool) {
         carpoolDao.insert(carpool);
         for (Buddy buddy : carpool.getBuddies()) {
             if(buddy == null) continue;
             buddyDao.addToCarpool(buddy, carpool);
         }
     }
 
     private List<Carpool> getAllCarpools() {
         List<Carpool> carpools = carpoolDao.selectAllCarpools();
 
         for (Carpool carpool : carpools) {
             carpool.setBuddies(buddyDao.getBuddyListByCarpoolName(carpool.getName()));
         }
         return carpools;
     }
 }
