 package liquibase.ext.backupandrestore.test.util;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import liquibase.ext.backupandrestore.BackupRestore;
 
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 
 /**
  * Test util for several clean ups of the used database.
  * 
  * @author afinke
  *
  */
 public class LiquibaseTestUtil {
 	
 	public static void cleanBackups(DataSource dataSource, String user) {
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		List<String> tablesToRemove = jdbcTemplate.query(
 				"SELECT table_name " +
 				"FROM all_tables " +
 				"WHERE table_name LIKE ?",
 				new Object[] { BackupRestore.TABLE_PREFIX + "%" }, new RowMapper<String>() {
 					@Override
 					public String mapRow(ResultSet resultSet, int i) throws SQLException {
 						return resultSet.getString(1);
 					}
 				});
 		for (String table : tablesToRemove) {
 			jdbcTemplate.execute("DROP TABLE " + table + " CASCADE CONSTRAINTS");
 		}
 	}
 	
 	public static void cleanSchema(DataSource dataSource, String user) {
 		dropTables(dataSource, user);
 		dropViews(dataSource, user);
 		dropTriggers(dataSource, user);
 		dropSequences(dataSource, user);
 		dropTypes(dataSource, user);
 		purgeRecyclebin(dataSource, user);
 	}
 
 	public static void dropTables(DataSource dataSource, String user) {
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		List<String> tablesToRemove = jdbcTemplate.query(
 				"SELECT table_name " +
 				"FROM all_tables " +
 				"WHERE owner = ?",
				new Object[] { user }, new RowMapper<String>() {
 					@Override
 					public String mapRow(ResultSet resultSet, int i) throws SQLException {
 						return resultSet.getString(1);
 					}
 				});
 		for (String table : tablesToRemove) {
 			jdbcTemplate
 					.execute("DROP TABLE " + table + " CASCADE CONSTRAINTS");
 		}
 	}
 
 	public static void dropViews(DataSource dataSource, String user) {
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		List<String> viewsToRemove = jdbcTemplate.query(
 				"SELECT view_name " +
 				"FROM all_views " +
 				"WHERE owner = ?",
				new Object[] { user }, new RowMapper<String>() {
 					@Override
 					public String mapRow(ResultSet resultSet, int i) throws SQLException {
 						return resultSet.getString(1);
 					}
 				});
 		for (String view : viewsToRemove) {
 			jdbcTemplate.execute("DROP VIEW " + view);
 		}
 	}
 
 	public static void dropTriggers(DataSource dataSource, String user) {
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		List<String> triggersToRemove = jdbcTemplate.query(
 				"SELECT trigger_name " +
 				"FROM all_triggers " +
 				"WHERE owner = ?",
				new Object[] { user }, new RowMapper<String>() {
 					@Override
 					public String mapRow(ResultSet resultSet, int i) throws SQLException {
 						return resultSet.getString(1);
 					}
 				});
 		for (String trigger : triggersToRemove) {
 			jdbcTemplate.execute("DROP TRIGGER " + trigger);
 		}
 	}
 
 	public static void dropSequences(DataSource dataSource, String user) {
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		List<String> sequencesToRemove = jdbcTemplate
 				.query("SELECT sequence_name " +
 						"FROM  all_sequences " +
 						"WHERE sequence_owner = ?",
						new Object[] { user }, new RowMapper<String>() {
 							@Override
 							public String mapRow(ResultSet resultSet, int i) throws SQLException {
 								return resultSet.getString(1);
 							}
 						});
 		for (String sequence : sequencesToRemove) {
 			jdbcTemplate.execute("DROP SEQUENCE " + sequence);
 		}
 	}
 
 	public static void dropTypes(DataSource dataSource, String user) {
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		List<String> typesToRemove = jdbcTemplate.query(
 				"SELECT type_name " +
 				"FROM  all_types " +
 				"WHERE owner = ?",
				new Object[] { user }, new RowMapper<String>() {
 					@Override
 					public String mapRow(ResultSet resultSet, int i) throws SQLException {
 						return resultSet.getString(1);
 					}
 				});
 		for (String type : typesToRemove) {
 			jdbcTemplate.execute("DROP TYPE " + type + " FORCE");
 		}
 	}
 
 	public static void purgeRecyclebin(DataSource dataSource, String user) {
 		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		jdbcTemplate.execute("purge recyclebin");
 	}
 	
 }
