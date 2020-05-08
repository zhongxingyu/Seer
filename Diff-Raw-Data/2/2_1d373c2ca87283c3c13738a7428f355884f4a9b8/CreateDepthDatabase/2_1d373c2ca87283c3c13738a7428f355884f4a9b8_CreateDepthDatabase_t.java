 package net.derkholm.nmica.extra.app.seq.nextgen;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.extra.app.seq.nextgen.CountDepths.Format;
 
 import org.biojava.bio.BioError;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 
 @NMExtraApp(launchName = "ngmkdepthdb", vm = VirtualMachine.SERVER)
 @App(overview = "Create a database to hold sequencing depth pileup data", generateStub = true)
 public class CreateDepthDatabase {
 	
 	private String dbHost;
 	private String dbUser;
 	private String dbPassword;
 	private String database;
 	private Connection connection;
 	private Format format = Format.MYSQL;
 	private File outputFile;
 	private boolean createDatabase;
 	private boolean dropDatabase;
 
 	@Option(help="Database host")
 	public void setHost(String str) {
 		this.dbHost = str;
 	}
 
 	@Option(help="Database username")
 	public void setUser(String str) {
 		this.dbUser = str;
 	}
 	
 	@Option(help="Database password")
 	public void setPassword(String str) {
 		this.dbPassword = str;
 	}
 	
 	@Option(help="Database schema name")
 	public void setDatabase(String str) {
 		this.database = str;
 	}
 	
 	@Option(help="Output format (default=mysql)", optional=true)
 	public void setFormat(Format format) {
 		this.format = format;
 	}
 	
 	@Option(help="Create the database, not just the tables", optional=true)
 	public void setCreateDatabase(boolean b) {
 		this.createDatabase = b;
 	}
 	
 	@Option(help="Drop database", optional=true)
 	public void setDropDatabase(boolean b) {
 		this.dropDatabase = b;
 	}
 	
 	private Connection connection() throws Exception {
 		if (this.connection == null) {
 			if (this.format == Format.MYSQL) {
 				if (this.createDatabase) {
 					this.connection = 
 						CountDepths.mysqlConnection(
 							dbHost,
 							"",
 							dbUser,
 							dbPassword);
 					
 					PreparedStatement statement;
 					if (dropDatabase) {
						statement = this.connection.prepareStatement("DROP DATABASE IF EXISTS " + this.database + ";");
 						statement.executeUpdate();
 						statement.close();
 					}
 					statement = this.connection.prepareStatement("CREATE DATABASE IF NOT EXISTS " + this.database + ";");
 					statement.executeUpdate();
 					statement.close();
 					statement = this.connection.prepareStatement("USE " + this.database + ";");
 					statement.executeUpdate();
 					statement.close();
 				} else {
 					this.connection = 
 						CountDepths.mysqlConnection(
 							dbHost,
 							database,
 							dbUser,
 							dbPassword);
 					
 				}
 			} else {
 				this.connection = 
 					CountDepths.connection(
 						this.format, 
 						this.outputFile);
 			}
 		}
 		return this.connection;
 	}
 	
 	public void main(String[] args) throws SQLException, Exception {
 		
 		
 		System.err.println("Creating output tables...");
 		if ((format == Format.HSQLDB) || (format == Format.SQLITE) || format == Format.MYSQL) {
 			CountDepths.createDepthTable(this.connection());
 			CountDepths.createRefSeqTable(this.connection());
 		} else {
 			throw new BioError("Unsupported format for database creation");
 		}
 	}
 
 }
