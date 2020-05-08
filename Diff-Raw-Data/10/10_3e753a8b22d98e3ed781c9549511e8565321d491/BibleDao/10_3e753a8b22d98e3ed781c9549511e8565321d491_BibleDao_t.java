 package net.todd.biblestudy;
 
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class BibleDao extends BaseDao implements IBibleDao {
 	private static boolean isInitialized;
 
 	private void load() throws Exception {
 		if (!isInitialized) {
 			Connection connection = getSqlMapConfig().getDataSource()
 					.getConnection();
 			SqlImporter sqlImporter = new SqlImporter(connection,
 					new SqlBatchCreator());
 
 			InputStream content = getClass()
 					.getResourceAsStream("/content.sql");
 			sqlImporter.processSQLFile(content);
 
 			isInitialized = true;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<BibleVerse> getAllVerses() throws DataException {
 		try {
 			load();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		List<BibleVerse> allVerses = new ArrayList<BibleVerse>();
 
 		try {
 			allVerses.addAll(getSqlMapConfig().queryForList("allVerses"));
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 
 		return allVerses;
 	}
 }
