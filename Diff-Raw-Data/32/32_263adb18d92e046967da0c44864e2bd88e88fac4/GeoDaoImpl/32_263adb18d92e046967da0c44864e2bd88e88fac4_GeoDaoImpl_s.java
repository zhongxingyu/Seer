 package toctep.skynet.backend.dal.dao.impl.mysql;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import toctep.skynet.backend.dal.dao.GeoDao;
 import toctep.skynet.backend.dal.domain.Domain;
 import toctep.skynet.backend.dal.domain.Geo;
import toctep.skynet.backend.dal.domain.GeoType;
 
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 
 public class GeoDaoImpl extends GeoDao{
 
 	@Override
 	public void delete(Domain domain) {
 		Connection conn = (Connection) this.getConnection();
 		
 		Geo geo = (Geo) domain;
 		
 		Statement stmt = null;
 		
 		try {
 			stmt = (Statement) conn.createStatement();
 			stmt.executeUpdate("DELETE FROM " + tableName + " WHERE id = " + geo.getId());
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}		
 	}
 	@Override
 	public void insert(Domain domain) {
 		Connection conn = (Connection) this.getConnection();
 		
 		Geo geo = (Geo) domain;
 		
 		Statement stmt = null;
 		
 		try {
 			stmt = (Statement) conn.createStatement();
 			int id = stmt.executeUpdate(
 					"INSERT INTO " + tableName + " (geo_type_id, coordinates) " +
 					"VALUES (" + geo.getType() + ", '" 
 								+ geo.getCoordinates() + "')",					
 					Statement.RETURN_GENERATED_KEYS
 				);
 			geo.setId(id);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public Geo select(long id) {
 		Connection conn = (Connection) this.getConnection();
 		
 		Geo geo = null;
 		
 		Statement stmt = null;
 		ResultSet rs = null;
 		
 		try {
 			stmt = (Statement) conn.createStatement();
 			rs = stmt.executeQuery("SELECT * FROM " + tableName + " WHERE id = " + id);
 			rs.first();
 			geo = new Geo();
 			geo.setId(id);
			geo.setType((GeoType) daoFacade.getGeoTypeDao().select(rs.getInt("geo_type_id")));
 			geo.setCoordinates(rs.getString("coordinates"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				stmt.close();
 				rs.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return geo;
 	}
 
 	@Override
 	public void update(Domain domain) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
