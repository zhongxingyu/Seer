 package confdb.migrator;
 
 import confdb.data.*;
 
 import confdb.db.ConfDB;
 import confdb.db.DatabaseException;
 
 
 /**
  * DatabaseMigrator
  * ----------------
  * @author Philipp Schieferdecker
  *
  * Migrate a configuration from its current database to another database.
  */
 public class DatabaseMigrator
 {
     //
     // data members
     //
     
     /** configuration to be migrated */
     private Configuration sourceConfig = null;
 
     /** configuration to be migrated */
     private Configuration targetConfig = null;
 
     /** source database */
     private ConfDB sourceDB = null;
 
     /** target database */
     private ConfDB targetDB = null;
     
     /** release migrator to actually migrate the configuration */
     private ReleaseMigrator releaseMigrator = null;
     
 
     //
     // construction
     //
     
     /** standard constructor */
     public DatabaseMigrator(Configuration sourceConfig,
 			    ConfDB        sourceDB,
 			    ConfDB        targetDB)
     {
 	this.sourceConfig = sourceConfig;
 	this.sourceDB     = sourceDB;
 	this.targetDB     = targetDB;
     }
     
     
     //
     // member functions
     //
     
     /** migrate the configuration from sourceDB to targetDB */
     public void migrate(String targetName,Directory targetDir)
 	throws MigratorException
     {
 	SoftwareRelease sourceRelease = sourceConfig.release();
 	SoftwareRelease targetRelease = new SoftwareRelease();
 	String          releaseTag    = sourceRelease.releaseTag();
 	String          creator       = System.getProperty("user.name");
 	
 	try {
	    
	    targetDB.insertRelease(releaseTag,sourceRelease);

 	    targetDB.loadSoftwareRelease(releaseTag,targetRelease);
 	    ConfigInfo targetConfigInfo = new ConfigInfo(targetName,targetDir,
 							 releaseTag);
 	    targetConfig = new Configuration(targetConfigInfo,targetRelease);
 	    
 	    releaseMigrator = new ReleaseMigrator(sourceConfig,targetConfig);
 	    releaseMigrator.migrate();
 	    targetDB.insertConfiguration(targetConfig,
 					 creator,
 					 sourceConfig.processName(),
 					 "imported from external database.");
 	}
 	catch (DatabaseException e) {
 	    String errMsg =
 		"DatabaseMigrator::migrate(targetName="+targetName+
 		",targetDir="+targetDir.name()+") failed.";
 	    throw new MigratorException(errMsg,e);
 	}
     }
     
     /** retrieve the release-migrator */
     public ReleaseMigrator releaseMigrator() { return releaseMigrator; }
  
     /** get source configuration */
     public Configuration sourceConfig() { return sourceConfig; }
 
     /** get target configuration */
     public Configuration targetConfig() { return targetConfig; }
 }
