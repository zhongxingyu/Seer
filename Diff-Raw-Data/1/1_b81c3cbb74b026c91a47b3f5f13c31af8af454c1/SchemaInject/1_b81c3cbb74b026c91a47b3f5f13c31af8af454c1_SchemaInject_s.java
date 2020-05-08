 package at.r7r.schemaInject;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.thoughtworks.xstream.XStream;
 
 import at.r7r.schemaInject.dao.DatabaseHelper;
 import at.r7r.schemaInject.dao.SqlBuilder;
 import at.r7r.schemaInject.entity.Field;
 import at.r7r.schemaInject.entity.PrimaryKey;
 import at.r7r.schemaInject.entity.Schema;
 import at.r7r.schemaInject.entity.Table;
 
 public class SchemaInject {
 	private static Table createMetaTable(String tableName) {
 		List<Field> fields = new ArrayList<Field>();
 		List<String> pkeyFields = new LinkedList<String>();
 		fields.add(new Field(null, "revision", "INTEGER", false, null));
 		fields.add(new Field(null, "ts", "TIMESTAMP", false, null)); // not using a default value here as this is db-specific
 		pkeyFields.add("revision");
 		
 		PrimaryKey pkey = new PrimaryKey(null, tableName+"_pkey", pkeyFields);
 		return new Table(null, tableName, fields, pkey, null, null);
 	}
 	
 	static XStream getXStream() {
 		XStream xstream = new XStream();
 		xstream.processAnnotations(Schema.class);
 		return xstream;
 	}
 	
 	public void inject(Connection conn, Schema schema) throws SQLException {
 		DatabaseHelper dh = new DatabaseHelper(conn);
 
 		// inject each table
 		for (Table table: schema.getTables()) {
 			dh.createTable(table);
 		}
 
 		Table metaTable = createMetaTable(schema.getMetaTable());
 		dh.createTable(metaTable);
 		
 		SqlBuilder sql = new SqlBuilder(" ", false);
 		sql.append("INSERT INTO");
 		sql.appendIdentifier(schema.getMetaTable());
 		sql.append("(\"revision\", \"ts\") VALUES (?,?)");
 		PreparedStatement stmt = conn.prepareStatement(sql.join());
 		stmt.setInt(1, schema.getRevision());
 		stmt.setTimestamp(2, new Timestamp(Calendar.getInstance().getTimeInMillis()));
 	}
 
 	public Schema readSchema(InputStream is) {
 		XStream xstream = getXStream();
 		Schema rc = (Schema) xstream.fromXML(is);
 		rc.assignNamesToUnnamedIndices();
 		return rc;
 
 	}
 	
 	public Schema readSchema(String filename) throws FileNotFoundException {
 		File file = new File(filename);
 		return readSchema(new FileInputStream(file));
 	}
 }
