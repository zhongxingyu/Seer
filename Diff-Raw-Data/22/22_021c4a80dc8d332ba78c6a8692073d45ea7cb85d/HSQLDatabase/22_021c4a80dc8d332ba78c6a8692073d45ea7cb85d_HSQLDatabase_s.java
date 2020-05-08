 package db;
 
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 public class HSQLDatabase extends JDBCDatabase {
 
 	private static String[] createStmts = {
 		// map (map name), seed (seed to use for map), win (1 - team a, 0 - team b), win_condition (0 - team destroyed, 1 - points, 2 - time/other)
 		// height (height of map), width (width of map), rounds (map's hardcoded rounds), points (map's hardcoded points), data (replay file)
 		"CREATE TABLE matches (id IDENTITY, run_id INTEGER NOT NULL, map varchar(30) NOT NULL, seed INTEGER NOT NULL, win TINYINT, win_condition INTEGER, " +
 		"height INTEGER NOT NULL, width INTEGER NOT NULL, rounds INTEGER NOT NULL, points INTEGER NOT NULL, a_points INTEGER, b_points INTEGER, data BLOB)",
 		// status (0 - queued, 1 - running, 2 - complete, 3 - error, 4 - canceled)
 		"CREATE TABLE runs (id IDENTITY,team_a varchar(45) NOT NULL, team_b varchar(45) NOT NULL,status TINYINT DEFAULT '0', started timestamp,ended timestamp)",
 		// allows svn users to easily make an alias for a specific revision of code
 		"CREATE TABLE tags (tag varchar(45) NOT NULL,alias varchar(20) DEFAULT NULL,UNIQUE (tag))",
 		// session (cookie auth value), status (0 - unapproved, 1 - active, 2 - admin)
 		"CREATE TABLE users (username varchar(20) NOT NULL, password varchar(40) NOT NULL, salt varchar(40) NOT NULL, session varchar(40), status TINYINT NOT NULL, UNIQUE(username))",
 	};
 	
 	private static String[] dropStmts = {
 		"DROP TABLE IF EXISTS matches",
 		"DROP TABLE IF EXISTS runs",
 		"DROP TABLE IF EXISTS tags",
 		"DROP TABLE IF EXISTS users",
 	};
 
 	@Override
 	public void connect() throws Exception {
 		try {
			Class.forName ("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:hsqldb:file:" + config.install_dir + "/hsqldb" + config.db_host,config.db_user,config.db_pass);
 		} catch (Exception e) {
 			_log.severe("Cannot connect to database");
 			throw e;
 		}
 		if (config.reset_db) {
 			drop();
 		}
 		init();
 	}
 	
 	private void drop() throws SQLException {
         for (String stmt: dropStmts) {
 			update(stmt, false);
 		}
 	}
 	
 	private void init() throws SQLException {
         for (String stmt: createStmts) {
 			update(stmt, false);
 		}
 	}
 
 }
