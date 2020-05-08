 package org.oobium.persist.migrate.db.derby.embedded;
 
 import static org.oobium.persist.Relation.CASCADE;
 import static org.oobium.persist.Relation.NO_ACTION;
 import static org.oobium.persist.Relation.RESTRICT;
 import static org.oobium.persist.Relation.SET_DEFAULT;
 import static org.oobium.persist.Relation.SET_NULL;
 import static org.oobium.utils.SqlUtils.DERBY;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.oobium.logging.Logger;
 import org.oobium.persist.PersistService;
 import org.oobium.persist.PersistServiceProvider;
 import org.oobium.persist.migrate.db.DbMigrationService;
 import org.oobium.persist.migrate.db.defs.Index;
 import org.oobium.persist.migrate.db.defs.Table;
 import org.oobium.persist.migrate.db.defs.columns.ForeignKey;
 import org.oobium.persist.migrate.db.defs.columns.PrimaryKey;
 import org.oobium.utils.SqlUtils;
 
 public class DerbyEmbeddedMigrationService extends DbMigrationService {
 
 	// Derby data types: http://db.apache.org/derby/docs/10.6/ref/crefsqlj31068.html
 	private static final Map<String, String> sqlTypes;
 	static {
 		sqlTypes = new HashMap<String, String>();
 		sqlTypes.put("binary",		"BLOB");
 		sqlTypes.put("boolean", 	"BOOLEAN");
 		sqlTypes.put("date", 		"DATE");
 		sqlTypes.put("decimal", 	"DECIMAL");
 		sqlTypes.put("double", 		"DOUBLE");
 		sqlTypes.put("float", 		"FLOAT");
 		sqlTypes.put("integer", 	"INTEGER");
 		sqlTypes.put("long", 		"BIGINT");
 		sqlTypes.put("string", 		"VARCHAR(255)");
 		sqlTypes.put("text", 		"CLOB");
 		sqlTypes.put("time", 		"TIME");
 		sqlTypes.put("timestamp", 	"BIGINT");
 	}
 	
 	
 	public DerbyEmbeddedMigrationService() {
 		super();
 	}
 	
 	public DerbyEmbeddedMigrationService(String client, Logger logger) {
 		super(client, logger);
 	}
 	
 	@Override
 	protected String getRenameColumnSql(Table table, String from, String to) {
 		return "RENAME COLUMN " + getSqlSafe(table.name) + "." + getSqlSafe(from) + " TO " + getSqlSafe(to);
 	}
 	
 	@Override
 	protected void createIndex(Table table, Index index) throws SQLException {
 		if(index.unique && index.columns.length == 1) {
 			super.createIndex(table, index.setUnique(false));
 			createUniqueTrigger(table, index.columns[0]);
 		} else {
 			super.createIndex(table, index);
 		}
 	}
 	
 	private void createUniqueTrigger(Table table, String column) throws SQLException {
 		long start = -1;
 		if(logger.isLoggingInfo()) {
 			logger.info("creating unique trigger on " + table.name + "("+ column + ")...");
 			start = System.currentTimeMillis();
 		}
 		
 		Connection connection = persistor.getConnection();
 		Statement stmt = connection.createStatement();
 		try {
 			for(String action : new String[] { "INSERT", "UPDATE" }) {
 				String sql =
 					"CREATE TRIGGER " + table.name + "___" + column + "___u_" + action.toLowerCase() + "_trigger" +
					" NO CASCADE BEFORE " + action + " ON " + getSqlSafe(table.name) +
 					" REFERENCING NEW ROW AS NEWROW" +
 					" FOR EACH ROW" +
 					" CALL APP.CHECK_UNIQUE('" + getSqlSafe(table.name) + "', '" + getSqlSafe(column) + "', NEWROW." + getSqlSafe(column) + ")";
 				
 				logger.info(sql);
 				stmt.executeUpdate(sql);
 				if(logger.isLoggingInfo()) {
 					long total = System.currentTimeMillis() - start;
 					logger.info("created unique " + action.toLowerCase() + " trigger on " + table.name + "("+ column + ") in " + total + "ms");
 				}
 			}
 		} finally {
 			try {
 				stmt.close();
 			} catch(SQLException e) {
 				// discard
 			}
 		}
 	}
 	
 	@Override
 	protected String getCreateForeignKeyColumnSql(ForeignKey fk) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getSqlSafe(fk.column)).append(' ').append(getSqlType(fk.type)).append(" CONSTRAINT ");
 		sb.append(fk.name).append(" REFERENCES ").append(getSqlSafe(fk.reference)).append("(id)");
 		switch(fk.options.get("onDelete", -1)) {
 		case CASCADE:		sb.append(" ON DELETE CASCADE");	break;
 		case NO_ACTION:		sb.append(" ON DELETE NO ACTION");	break;
 		case RESTRICT:		sb.append(" ON DELETE RESTRICT");	break;
 		case SET_DEFAULT:	sb.append(" ON DELETE SET DEFAULT");break;
 		case SET_NULL:		sb.append(" ON DELETE SET NULL");	break;
 		}
 		switch(fk.options.get("onUpdate", -1)) {
 		case CASCADE:		sb.append(" ON UPDATE CASCADE");	break;
 		case NO_ACTION:		sb.append(" ON UPDATE NO ACTION");	break;
 		case RESTRICT:		sb.append(" ON UPDATE RESTRICT");	break;
 		case SET_DEFAULT:	sb.append(" ON UPDATE SET DEFAULT");break;
 		case SET_NULL:		sb.append(" ON UPDATE SET NULL");	break;
 		}
 		return sb.toString();
 	}
 
 	@Override
 	protected String getCreatePrimaryKeySql(PrimaryKey pk) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(getSqlSafe(pk.name)).append(' ').append(getSqlType(pk.type));
 		if(pk.autoIncrement) sb.append(" GENERATED ALWAYS");
 		sb.append(" AS IDENTITY PRIMARY KEY");
 		return sb.toString();
 	}
 	
 	@Override
 	protected String getCreateTableOptionsSql(Table table) {
 		return null;
 	}
 
 	@Override
 	protected String getSqlSafe(String rawString) {
 		return SqlUtils.safeSqlWord(DERBY, rawString);
 	}
 	
 	@Override
 	protected String getSqlType(String migrationType) {
 		String type = sqlTypes.get(migrationType);
 		if(type != null) {
 			return type;
 		}
 		return migrationType;
 	}
 
 	@Override
 	protected PersistService getPersistService(PersistServiceProvider provider) {
 		return provider.get("org.oobium.persist.db.derby.embedded");
 	}
 
 }
