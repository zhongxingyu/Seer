 package dk.itu.realms.services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.primefaces.model.map.LatLng;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import dk.itu.realms.model.dao.PhoneUserDAO;
 import dk.itu.realms.model.dao.RealmDAO;
 import dk.itu.realms.model.entity.Realm;
 import dk.itu.realms.services.util.LatLonCalculator;
 
 @Service("realms")
 public class RealmsService {
 
 	@Autowired
 	private RealmDAO realmDAO;
 
 	@Autowired
	private PhoneUserDAO userDAO;
 
 	public Realm getRealm(long realmId) {
 		return realmDAO.get(realmId);
 	}
 
 	public List<Realm> getRealms(String userId) {
 		return realmDAO.getAllByUserId(userId);
 	}
 
 	public List<Realm> getRealmsByLocation(String lat, String lon, String userId) {
 		List<Realm> result = new ArrayList<Realm>();
		if (userDAO.getUser(Long.parseLong(userId)) != null) {
 
 			LatLonCalculator cal = new LatLonCalculator();
 
 			LatLng pos = new LatLng(Double.parseDouble(lat),
 					Double.parseDouble(lon));
 
 			List<Realm> allRealms = realmDAO.listAll();
 
 			for (Realm realm : allRealms) {
 				if (cal.insideRealm(pos, realm))
 					result.add(realm);
 			}
 		}
 		return result;
 
 	}
 }
