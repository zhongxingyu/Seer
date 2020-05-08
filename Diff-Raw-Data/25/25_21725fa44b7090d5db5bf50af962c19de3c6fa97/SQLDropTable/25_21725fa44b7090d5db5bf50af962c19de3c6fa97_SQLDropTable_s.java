 package net.eagledb.server.sql;
 
 import java.sql.SQLException;
 import net.eagledb.server.ClientConnection;
 import net.eagledb.server.Result;
 import net.eagledb.server.ResultCode;
 import net.eagledb.server.Server;
 import net.eagledb.server.storage.Database;
 import net.eagledb.server.storage.Schema;
 import net.eagledb.server.storage.Table;
 import net.sf.jsqlparser.statement.drop.Drop;
 
 public class SQLDropTable extends SQLAction {
 	
 	private Drop sql;
 
 	public SQLDropTable(Server server, ClientConnection conn, Drop sql) {
 		super(server, conn);
 		this.sql = sql;
 	}
 
 	public Result getResult() throws SQLException {
 		// we must have a selected database
 		Database selectedDatabase = conn.getSelectedDatabase();
 		if(selectedDatabase == null)
 			throw new SQLException("No database selected.");
 
 		// check the users permission
 		//if(!conn.getUser().canCreateTable)
 		//	throw new SQLException("Permission denied. You must have the CREATE TABLE privilege.");
 
 		// get schema
 		Schema schema = selectedDatabase.getSchema("public");
 		if(schema == null)
 			throw new SQLException("No such schema " + schema.getName());
 
 		// if this is a temporary table we need to translate its name
 		String tableName = sql.getName();
 
 		// see if the table exists
 		Table table = schema.getTable(tableName);
		if(table == null)
			throw new SQLException("Table " + schema.getName() + "." + tableName + " does not exist");
 
 		// drop the table
 		schema.dropTable(tableName);
 		server.dropTable(selectedDatabase.getName(), schema.getName(), table);
 
 		return new Result(ResultCode.SUCCESS);
 	}
 
 }
