 package it.geosolutions.geobatch.destination;
 
 import java.io.File;
 import java.sql.Connection;
 import java.util.Arrays;
 import java.util.List;
 
 import org.geotools.data.Transaction;
 import org.geotools.data.memory.MemoryDataStore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.mockrunner.jdbc.FileResultSetFactory;
 import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
 import com.mockrunner.mock.jdbc.MockConnection;
 import com.mockrunner.mock.jdbc.MockResultSet;
 
 public class RiskTestMemoryMockDataStore extends MemoryDataStore{
 
 	protected static final Logger LOGGER = LoggerFactory.getLogger(RiskTestMemoryMockDataStore.class);
 
 	private Connection connection = new MockConnection();
 
 	public Connection getConnection(Transaction transaction){
 		return connection;
 	}
 
 	public RiskTestMemoryMockDataStore(){
 		try{
 			
 			PreparedStatementResultSetHandler statementHandler = ((MockConnection) connection).getPreparedStatementResultSetHandler();
 			statementHandler.setUseRegularExpressions(true);   
 
 			FileResultSetFactory frsf = new FileResultSetFactory(new File(getClass().getResource("/risk_test_sqlfuntions.txt").toURI()));
 			frsf.setDelimiter(";");
 			frsf.setFirstLineContainsColumnNames(true);
 
 			String sql = "select.*siig_mtd_t_formula.*";
 			MockResultSet rs = frsf.create("formula");
 			for(int i = 1 ; i <= rs.getRowCount() ; i++){
 				List<?> a = rs.getRow(i);
 				String id_formula = (String) a.get(0);
 				MockResultSet rsi = new MockResultSet(id_formula);
 				rsi.addRow(a.subList(1, a.size()));					
 				statementHandler.prepareResultSet(sql, rsi, new Object[]{Integer.parseInt(id_formula)});
 			}
 
 			for(int id_geo_arco = 1 ; id_geo_arco<=20 ; id_geo_arco++){
 				MockResultSet rsRisk = new MockResultSet("risk" + id_geo_arco);
				String sqlRisk = "select.*\\("+id_geo_arco+"\\)\\sgroup\\sby\\sid_geo_arco$";
 				rsRisk.addRow(Arrays.asList(new Object[]{id_geo_arco,Math.random()}));
 				statementHandler.prepareResultSet(sqlRisk, rsRisk);
 			}
 
 		}catch(Exception e){
 			LOGGER.error(e.getMessage(),e);
 		}
 
 	}
 
 
 }
