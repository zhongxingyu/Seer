 package name.stokito.service;
 
 import name.stokito.units.TableModel;
 
 import javax.annotation.Resource;
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author november
  * @since 5/27/12
  */
 public class DataBaseServiceImpl implements DataBaseService {
 
 	@Resource
 	private DataSource dataSource;
 
 	@Override
	public TableModel getSelect(String selectQuery) {
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
			throw new RuntimeException(e);
 		}
 
		return new TableModel(data, columnNames);
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
