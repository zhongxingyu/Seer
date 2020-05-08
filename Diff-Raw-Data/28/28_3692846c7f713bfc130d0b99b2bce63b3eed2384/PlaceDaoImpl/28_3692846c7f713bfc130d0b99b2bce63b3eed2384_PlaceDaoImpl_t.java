 package toctep.skynet.backend.dal.dao.impl.mysql;
 
 import toctep.skynet.backend.dal.dao.PlaceDao;
 import toctep.skynet.backend.dal.domain.Domain;
 import toctep.skynet.backend.dal.domain.DomainStringPk;
 import toctep.skynet.backend.dal.domain.Place;
 
 public class PlaceDaoImpl extends PlaceDao {
 
 	@Override
 	public void insert(Domain domain) {
 		Place place = (Place) domain;
 		
 		String query = "INSERT INTO " + tableName +	"VALUES ("
 		+ MySqlUtil.escape(place.getId()) + ", '"
 		+ place.getType().getId() + ", '"
 		+ place.getBoundingBox().getId() + ", '"
 		+ MySqlUtil.escape(place.getName()) + ", '"
 		+ MySqlUtil.escape(place.getUrl().getId()) + ", '"
 		+ MySqlUtil.escape(place.getFullName()) + ", '"
 		+ place.getCountry().getId() + ", '"
 		+ MySqlUtil.escape(place.getStreetAddress()) + ", '"
 		+ MySqlUtil.escape(place.getLocality()) + ", '"
 		+ MySqlUtil.escape(place.getRegion()) + ", '"
 		+ MySqlUtil.escape(place.getIso3()) + ", '"
 		+ MySqlUtil.escape(place.getPostalCode()) + ", '"
 		+ MySqlUtil.escape(place.getPhone()) + ", '"
 		+ MySqlUtil.escape(place.getTwitter()) + ", '"
 		+ MySqlUtil.escape(place.getAppId()) + "')";
 		
 		MySqlUtil.getInstance().insert(query);
 	}
 
 	@Override
 	public DomainStringPk select(String id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void update(Domain domain) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void delete(Domain domain) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean exists(Domain domain) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public int count() {
 		return MySqlUtil.getInstance().count(tableName);
 	}
 
 }
