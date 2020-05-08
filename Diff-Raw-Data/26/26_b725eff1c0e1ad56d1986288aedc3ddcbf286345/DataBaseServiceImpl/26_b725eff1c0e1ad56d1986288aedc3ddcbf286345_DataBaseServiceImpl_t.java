 package name.stokito.service;
 
 import name.stokito.units.TableModel;
 
 import javax.annotation.Resource;
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 
 /**
  * @author november
  * @since 5/27/12
  */
 public class DataBaseServiceImpl implements DataBaseService {
 
 	@Resource
 	private DataSource dataSource;
 
 	@Override
	public Map<String, Object> getSelect(String selectQuery) {
		Map<String, Object> result= new HashMap<String, Object>();

 		List<String> columnNames = new ArrayList<String>();
 		List<List> data = new ArrayList<List>();
 
 		try {
 			try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(selectQuery)) {
 				ResultSetMetaData md = rs.getMetaData();
 				int n = md.getColumnCount();
 
 				for (int i = 1; i <= n; i++)
 					columnNames.add(md.getColumnName(i));
 
 				while (rs.next()) {
 					List<Object> v = new ArrayList<Object>();
 					for (int i = 1; i <= n; i++) {
 						v.add(rs.getObject(i));
 					}
 					data.add(v);
 				}
 			}
 		} catch (SQLException e) {
			result.put("Error", e.toString());
//			throw new RuntimeException(e);
 		}
 
		result.put("TableModel", new TableModel(data, columnNames));
		return result;
 	}
 
 	@Override
 	public int getFunc(String query) {
 		int result = 0;
 		try {
 			try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(query)) {
 				result = rs.next() ? rs.getInt(1) : 0;
 			}
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 		return result;
 	}
 }
