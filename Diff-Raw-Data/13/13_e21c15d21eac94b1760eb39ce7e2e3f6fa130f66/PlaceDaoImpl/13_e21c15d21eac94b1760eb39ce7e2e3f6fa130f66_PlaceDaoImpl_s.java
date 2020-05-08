 package toctep.skynet.backend.dal.dao.impl.mysql;
 
 import java.sql.Types;
 import java.util.List;
 
 import toctep.skynet.backend.dal.dao.PlaceDao;
 import toctep.skynet.backend.dal.domain.Domain;
 import toctep.skynet.backend.dal.domain.boundingbox.BoundingBox;
 import toctep.skynet.backend.dal.domain.country.Country;
 import toctep.skynet.backend.dal.domain.place.Place;
 import toctep.skynet.backend.dal.domain.place.PlaceType;
 import toctep.skynet.backend.dal.domain.url.Url;
 
 public class PlaceDaoImpl extends PlaceDao {
 
 	@Override
 	public void insert(Domain<String> domain) {
 		Place place = (Place) domain;
 		
 		String query = 
 			"INSERT INTO " + tableName + 
 				"(id, place_type_id, bounding_box_id, name, url_id, full_name, country_id, street_address, locality, region, iso3, postal_code, phone, twitter, appid) " +
 			"VALUES" +
 				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		
 		Param[] params = new Param[] {
 			new Param(place.getId(), Types.VARCHAR),
 			new Param(place.getType().getId(), Types.BIGINT),
 			new Param(place.getBoundingBox().getId(), Types.BIGINT),
 			new Param(place.getName(), Types.VARCHAR),
 			new Param(place.getUrl().getId(), Types.VARCHAR),
 			new Param(place.getFullName(), Types.VARCHAR),
 			new Param(place.getCountry().getId(), Types.VARCHAR),
 			new Param(place.getStreetAddress(), Types.VARCHAR),
 			new Param(place.getLocality(), Types.VARCHAR),
 			new Param(place.getRegion(), Types.VARCHAR),
 			new Param(place.getIso3(), Types.VARCHAR),
 			new Param(place.getPostalCode(), Types.VARCHAR),
 			new Param(place.getPhone(), Types.VARCHAR),
 			new Param(place.getTwitter(), Types.VARCHAR),
 			new Param(place.getAppId(), Types.VARCHAR)
 		};
 			
 		MySqlUtil.getInstance().insert(query, params);
 	}
 
 	@Override
 	public Place select(String id) {
 		Place place = new Place();
 		
 		String query = "SELECT * FROM " + tableName + " WHERE id=?";
 		
 		Param[] params = new Param[] {
 			new Param(id, Types.VARCHAR)
 		};
 		
 		List<Object> record = MySqlUtil.getInstance().selectRecord(query, params);
 		
 		place.setId(id);
 		place.setType(PlaceType.select((Integer) record.get(1)));
 		place.setBoundingBox(BoundingBox.select((Integer) record.get(2)));
 		place.setName((String) record.get(3));
 		place.setUrl(Url.select((String) record.get(4)));
 		place.setFullName((String) record.get(5));
 		place.setCountry(Country.select((String) record.get(6)));
 		place.setStreetAddress((String) record.get(7));
 		place.setLocality((String) record.get(8));
 		place.setRegion((String) record.get(9));
 		place.setIso3((String) record.get(10));
 		place.setPostalCode((String) record.get(11));
 		place.setPhone((String) record.get(12));
 		place.setTwitter((String) record.get(13));
 		place.setAppId((String) record.get(14));
 		
 		return place;
 	}
 
 	@Override
 	public void update(Domain<String> domain) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void delete(Domain<String> domain) {
 		Place place = (Place) domain;
 		MySqlUtil.getInstance().delete(
 			"DELETE FROM " + tableName + " WHERE id=?",
 			new Param[] { new Param(place.getId(), Types.VARCHAR) }
 		);
 	}
 
 	@Override
 	public boolean exists(Domain<String> domain) {
 		Place place = (Place) domain;
		return MySqlUtil.getInstance().exists(tableName, "text", new Param(place.getId(), Types.VARCHAR));
 	}
 	
 	@Override
 	public boolean exists(String id) {
 		return MySqlUtil.getInstance().exists(tableName, "id", new Param(id, Types.VARCHAR));
 	}
 
 	@Override
 	public int count() {
 		return MySqlUtil.getInstance().count(tableName);
 	}
 
 }
