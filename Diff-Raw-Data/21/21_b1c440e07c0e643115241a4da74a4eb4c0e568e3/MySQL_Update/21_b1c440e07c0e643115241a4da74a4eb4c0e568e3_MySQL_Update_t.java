 package enderdom.eddie.database.updates;
 
 import java.sql.SQLException;
 import java.sql.Statement;
 
import enderdom.eddie.databases.bioSQL.interfaces.BioSQLExtended;
 import enderdom.eddie.databases.manager.DatabaseManager;
 import enderdom.eddie.tools.Tools_System;
 
 public class MySQL_Update{
 
 	public static boolean updbto24(DatabaseManager manager) throws SQLException {
 		String alters[] = new String[]{
 				"ALTER TABLE bioentry_dbxref DROP PRIMARY KEY, ADD PRIMARY KEY (bioentry_id,dbxref_id,rank,run_id);",
 				"ALTER TABLE bioentry_dbxref ADD CONSTRAINT FKdbxref_bioentry_run FOREIGN KEY (run_id) REFERENCES run(run_id) ON DELETE CASCADE;",
				"ALTER TABLE "+BioSQLExtended.runtable+" MODIFY source VARCHAR(80) BINARY;",
 				"UPDATE info SET DatabaseVersion='"+DatabaseManager.getDatabaseversion()+"', LastRevision='"+Tools_System.getDateNow("yyyy-MM-dd")+"';"
 		};
 		Statement st = manager.getCon().createStatement();
 		for(String s: alters)st.executeUpdate(s);
 		st.close();
 		return true;
 	}	
 	
 	
 }
