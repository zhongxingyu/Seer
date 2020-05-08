 package jp.ac.osaka_u.ist.sdl.mpanalyzer.db;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.Statement;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.Config;
 
 public abstract class DAO {
 
 	final protected Connection connector;
 
 	static public final String REVISION_SCHEMA = "number integer, date string, message string";
 	static public final String CODEFRAGMENT_SCHEMA = "id integer, text string, hash integer, start int, end int";
 	static public final String MODIFICATION_SCHEMA = "id integer primary key autoincrement, filepath string, beforeID integer, beforeHash integer, afterID integer, afterHash integer, revision integer, type integer";
 	static public final String PATTERN_SCHEMA = "id integer primary key autoincrement, beforeHash integer, afterHash integer, type integer, support integer, confidence real";
 	static public final String CLONE_SCHEMA = "id integer primary key autoincrement, filepath string, start inteter, end integer, revision integer, setID integer, groupID integer, changed boolean";
 
 	DAO(final boolean createRevisionTable,
 			final boolean createCodeFragmentTable,
 			final boolean createModificationTable,
 			final boolean createPatternTable, final boolean createProblemTable,
 			final boolean createCloneTable) throws Exception {
 
 		final String databaseLocation = Config.getDATABASELOCATION();
 		final String databaseName = Config.getDATABASENAME();
 
 		Class.forName("org.sqlite.JDBC");
 
 		final StringBuilder url = new StringBuilder();
 		url.append("jdbc:sqlite:");
 		url.append(databaseLocation);
 		if (url.charAt(url.length() - 1) != File.separatorChar) {
 			url.append(File.separator);
 		}
 		url.append(databaseName);
 		this.connector = DriverManager.getConnection(url.toString());
 
 		final Statement statement = this.connector.createStatement();
 
 		if (createRevisionTable) {
 			statement.executeUpdate("drop table if exists revision");
 			statement.executeUpdate("create table revision (" + REVISION_SCHEMA
 					+ ")");
 			statement
 					.executeUpdate("create index numberIndex on revision (number)");
 		}
 		if (createCodeFragmentTable) {
 			statement.executeUpdate("drop table if exists codefragment");
 			statement.executeUpdate("create table codefragment ("
 					+ CODEFRAGMENT_SCHEMA + ")");
 			statement
 					.executeUpdate("create index cfIDIndex on codefragment (id)");
 			statement
 					.executeUpdate("create index textIndex on codefragment (text)");
 			statement
 					.executeUpdate("create index hashIndex on codefragment (hash)");
 		}
 		if (createModificationTable) {
 			statement.executeUpdate("drop table if exists modification");
 			statement.executeUpdate("create table modification ("
 					+ MODIFICATION_SCHEMA + ")");
 			statement
 					.executeUpdate("create index mIDIndex on modification (id)");
 			statement
 					.executeUpdate("create index beforeIDIndex on modification (beforeID)");
 			statement
 					.executeUpdate("create index beforeHashIndex on modification (beforeHash)");
 			statement
 					.executeUpdate("create index afterIDIndex on modification (afterID)");
 			statement
 					.executeUpdate("create index afterHashIndex on modification (afterHash)");
 		}
 		if (createPatternTable) {
 			statement.executeUpdate("drop table if exists pattern");
 			statement.executeUpdate("create table pattern (" + PATTERN_SCHEMA
 					+ ")");
 			statement.executeUpdate("create index typeIndex on pattern (type)");
 			statement
 					.executeUpdate("create index supportIndex on pattern (support)");
 			statement
 					.executeUpdate("create index confidenceIndex on pattern (confidence)");
 		}
 		if (createCloneTable) {
 			statement.executeUpdate("drop table if exists clone");
 			statement
 					.executeUpdate("create table clone (" + CLONE_SCHEMA + ")");
 			statement.executeUpdate("create index setIDIndex on clone (setID)");
 		}
 		statement.close();
 	}
 
 	public void close() throws Exception {
 		this.connector.close();
 	}
 }
