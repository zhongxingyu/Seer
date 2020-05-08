 package toctep.skynet.backend.dal.dao.impl.mysql;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import toctep.skynet.backend.dal.dao.CountryDao;
 import toctep.skynet.backend.dal.domain.Country;
 import toctep.skynet.backend.dal.domain.Domain;
 
 public class CountryDaoImpl extends CountryDao{
 
 	@Override
 	public void delete(Domain domain) {
 		Country country = (Country) domain;
		MySqlUtil.getInstance().delete("DELETE FROM " + tableName + " WHERE code = " + MySqlUtil.escape(country.getId()));
 	}
 
 	@Override
 	public void insert(Domain domain) {
 		Country country = (Country) domain;
 		
 		MySqlUtil.getInstance().insert("INSERT INTO " + tableName + " (code, text) " +
 					"VALUES ('" + country.getId() + "', '" + country.getText() + "')");
 	}
 
 	@Override
 	public Country select(String id) {
 		Country country = new Country();
 		
 		ResultSet rs = MySqlUtil.getInstance().select("SELECT * FROM " + tableName + " WHERE code = " + MySqlUtil.escape(id));
 		
 		country.setId(id);
 		try {
 			country.setText(rs.getString("text"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return country;
 	}
 
 	@Override
 	public void update(Domain domain) {
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
