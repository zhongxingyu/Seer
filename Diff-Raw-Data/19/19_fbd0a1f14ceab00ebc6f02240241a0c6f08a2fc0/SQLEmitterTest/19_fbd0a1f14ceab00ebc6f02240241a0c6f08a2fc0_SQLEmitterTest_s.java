 package togos.codeemitter.sql;
 
 import java.io.StringWriter;
 import java.util.Arrays;
 
 import junit.framework.TestCase;
 import togos.codeemitter.TextWriter;
 import togos.codeemitter.structure.ScalarLiteral;
 import togos.codeemitter.structure.rdb.ColumnDefinition;
 import togos.codeemitter.structure.rdb.ForeignKeyConstraint;
 import togos.codeemitter.structure.rdb.IndexDefinition;
 import togos.codeemitter.structure.rdb.TableDefinition;
 import togos.lang.BaseSourceLocation;
 
 public class SQLEmitterTest extends TestCase
 {
 	public void testTableEmission() throws Exception {
 		TableDefinition td = new TableDefinition("Frog");
 		td.columns.add(new ColumnDefinition("FrogID", "INTEGER", true, null));
 		td.columns.add(new ColumnDefinition("ScoopID", "INTEGER", true, null));
 		td.columns.add(new ColumnDefinition("ScoopDate", "DATE", true, null));
 		td.columns.add(new ColumnDefinition("ToadName", "VARCHAR(50)", false, null));
 		td.columns.add(new ColumnDefinition("MuleName", "VARCHAR(10)", false, new ScalarLiteral("John Cusack", BaseSourceLocation.NONE)));
 		td.foreignKeyConstraints.add(new ForeignKeyConstraint(
 			"FrogScoop", Arrays.asList("ScoopID","ScoopDate"),
			"Scoop", Arrays.asList("IDOfScoop","DateOfScoppage")
 		));
 		td.indexes.add(new IndexDefinition("Toad", Arrays.asList("ToadName")));
 		
 		StringWriter sw = new StringWriter();
 		SQLEmitter sqlEmitter = new SQLEmitter(new TextWriter(sw));
 		
 		sqlEmitter.emitTableCreation(td);
 		
 		assertEquals(
 			"CREATE TABLE \"Frog\" (\n" +
 			"\t\"FrogID\" INTEGER,\n" +
 			"\t\"ScoopID\" INTEGER,\n" +
 			"\t\"ScoopDate\" DATE,\n" +
 			"\t\"ToadName\" VARCHAR(50) NOT NULL,\n" +
 			"\t\"MuleName\" VARCHAR(10) DEFAULT 'John Cusack' NOT NULL,\n" +
 			"\tPRIMARY KEY (\"FrogID\"),\n" +
 			"\tINDEX \"Toad\" (\"ToadName\"),\n" +
 			"\tFOREIGN KEY (\"ScoopID\", \"ScoopDate\") REFERENCES \"Scoop\" (\"IDOfScoop\", \"DateOfScoop\")\n" +
 			");\n",
 			sw.toString()
 		);
 	}
 }
