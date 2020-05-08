 package toctep.skynet.backend.dal.dao.impl.mysql;
 
 import java.sql.Types;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import toctep.skynet.backend.dal.dao.GeoDao;
 import toctep.skynet.backend.dal.domain.Domain;
 import toctep.skynet.backend.dal.domain.geo.Geo;
 import toctep.skynet.backend.dal.domain.geo.GeoType;
 
 public class GeoDaoImpl extends GeoDao {
 	
 	@Override
 	public void insert(Domain<Integer> domain) {
 		Geo geo = (Geo) domain;
 		
 		String query = "INSERT INTO " + tableName + "(geo_type_id, coordinates) VALUES(?, ?)";
 		
 		Param[] params = new Param[] {
 			new Param(geo.getType().getId(), Types.BIGINT),
 			new Param(geo.getCoordinates(), Types.VARCHAR)
 		};
 			
 		int id = MySqlUtil.getInstance().insert(query, params);
 		
 		geo.setId(id);
 	}
 
 	@Override
 	public Geo select(Integer id) {
 		Geo geo = new Geo();
 		
 		String query = "SELECT * FROM " + tableName + " WHERE id=?";
 		
 		Param[] params = new Param[] {
 			new Param(id, Types.BIGINT)
 		};
 		
 		List<Object> record = MySqlUtil.getInstance().selectRecord(query, params);
 		
 		geo.setId(id);
 		geo.setType(GeoType.select((Integer) record.get(1)));
 		geo.setCoordinates((String) record.get(2));
 		
 		return geo;
 	}
 
 	@Override
 	public void update(Domain<Integer> domain) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void delete(Domain<Integer> domain) {
 		Geo geo = (Geo) domain;
 		MySqlUtil.getInstance().delete(
 			"DELETE FROM " + tableName + " WHERE id=?",
 			new Param[] { new Param(geo.getId(), Types.INTEGER) }
 		);
 	}
 	
 	@Override
 	public boolean exists(Domain<Integer> domain) {
 		Geo geo = (Geo) domain;
 		Map<String, Param> params = new HashMap<String, Param>();
 		params.put("coordinates", new Param(geo.getCoordinates(), Types.VARCHAR));
 		return MySqlUtil.getInstance().exists(tableName, params);
 	}
 	
 	@Override
 	public boolean exists(Integer id) {
 		return MySqlUtil.getInstance().exists(tableName, "id", new Param(id, Types.INTEGER));
 	}
 	
 	@Override
 	public int count() {
 		return MySqlUtil.getInstance().count(tableName);
 	}
 
 }
