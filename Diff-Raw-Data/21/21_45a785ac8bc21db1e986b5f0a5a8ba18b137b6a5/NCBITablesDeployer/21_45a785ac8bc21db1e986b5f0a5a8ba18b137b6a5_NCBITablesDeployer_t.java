 package helper;
 
 
 import helper.gitaxid.GI_TaxIDDeployer;
 import helper.names.NamesDeployer;
 import helper.nodes.NodesDBDeployer;
 import logger.Log;
 import util.SystemUtil;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 /**
  * A master class that contains functions that deploy all databases form a given set of dmp files.
  * Standard login is "tuit", password "tuit"
  */
 public class NCBITablesDeployer {
     /**
      * Database login
      */
     public static final String login="tuit";
     /**
      * Database password
      */
     public static final String password="tuit";
 
     /**
      * Constructor grants non-instantiability
      */
     private NCBITablesDeployer() {
         throw new AssertionError();
     }
 
     /**
      * Deploys the full set of files for the NCBI taxonomic database
      * <b>Deprecated due to the use of deprecated inefficient database deployment methods.</b>
      *
      * @param connection      {@link Connection} to the database
      * @param gi_taxidDmpFile {@link File} gi_taxid.dmp
      * @param namesDmpFile    {@link File} names.dmp
      * @param nodesDmpFile    {@link File} nodes.dmp
      * @throws IOException  in case something goes wrong during file read
      * @throws SQLException in case something goes wrong upon database communication
      */
     @Deprecated
     public static void deployNCBIDatabasesFromFiles(final Connection connection, final File gi_taxidDmpFile, final File namesDmpFile, final File nodesDmpFile) throws IOException, SQLException {
 
         //Deploy the names table
         Log.getInstance().getLogger().info("Deploying Names Database..");
         NamesDeployer.deployNamesTable(connection, namesDmpFile);
         Log.getInstance().getLogger().info("Names Database deployed");
         //Deploy the gi_taxid table
         Log.getInstance().getLogger().info("Deploying GI_TaxID Database..");
         GI_TaxIDDeployer.deployGI_TaxIDTable(connection, gi_taxidDmpFile);
         Log.getInstance().getLogger().info("GI_TaxID Database deployed");
         //Read and create a validation table for the nodes.dmp
         Log.getInstance().getLogger().info("Preparing Rank-validation table..");
         NodesDBDeployer.deployRanksValidataionTable(connection);
         Log.getInstance().getLogger().info("Rank-validation table deployed");
         //Deploy the nodes table
         Log.getInstance().getLogger().info("Deploying Nodes Database..");
         NodesDBDeployer.deployNodesDatabase(connection, nodesDmpFile);
         Log.getInstance().getLogger().info("Nodes Database deployed");
         Log.getInstance().getLogger().info("NCBI database tables are ready.");
     }
 
     /**
      * Deploys the full set of files for the NCBI taxonomic database (faster implementation)
      *
      * @param connection      {@link Connection} to the database
      * @param gi_taxidDmpFile {@link File} gi_taxid.dmp
      * @param namesDmpFile    {@link File} names.dmp
      * @param nodesDmpFile    {@link File} nodes.dmp
      * @throws IOException  in case something goes wrong during file read
      * @throws SQLException in case something goes wrong upon database communication
      */
     public static void fastDeployNCBIDatabasesFromFiles(final Connection connection, final File gi_taxidDmpFile, final File namesDmpFile, final File nodesDmpFile) throws IOException, SQLException {
 
         //Deploy the names table
         Log.getInstance().getLogger().info("Deploying Names Database..");
         NamesDeployer.injectProcessedNamesDmpFile(connection, NamesDeployer.filterNamesDmpFile(namesDmpFile));
         Log.getInstance().getLogger().info("Names Database deployed");
         //Deploy the gi_taxid table
         Log.getInstance().getLogger().info("Deploying GI_TaxID Database..");
         GI_TaxIDDeployer.injectProcessedGI_TaxIDDmpFile(connection, GI_TaxIDDeployer.filterGI_TaxIDDmp(connection, gi_taxidDmpFile));
         Log.getInstance().getLogger().info("GI_TaxID Database deployed");
         //Read and create a validation table for the nodes.dmp
         Log.getInstance().getLogger().info("Preparing Rank-validation table..");
         NodesDBDeployer.deployRanksValidataionTable(connection);
         Log.getInstance().getLogger().info("Rank-validation table deployed");
         //Deploy the nodes table
         Log.getInstance().getLogger().info("Deploying Nodes Database..");
         NodesDBDeployer.injectProcessedNodesDmpFile(connection, NodesDBDeployer.filterNodesDmpFile(connection, nodesDmpFile));
         Log.getInstance().getLogger().info("Nodes Database deployed");
         Log.getInstance().getLogger().info("NCBI database tables are ready.");
 
     }
 
     /**
      * Fully deploys the NCBI taxonomic database directly from the NCBI FTP server
      * @param connection  {@link Connection} to the database
      * @param tmpDir a {@link File} directory that the temporary update files will be downloaded to
      * @throws IOException upon file read/write errors
      * @throws SQLException in case a database communication error occurs
      */
     public static void fastDeployNCBIDatabasesFromNCBI(final Connection connection, final File tmpDir)throws IOException, SQLException{
         //Downloading files
         Log.getInstance().getLogger().info("Downloading files..");
         Log.getInstance().getLogger().info("Downloading " + SystemUtil.TAXDUMP_ARCH);
         File taxdump_tar_gz=SystemUtil.downloadFileFromNCBIFTP(tmpDir, new File(SystemUtil.NCBI_TAXONOMY),new File(SystemUtil.TAXDUMP_ARCH));
         Log.getInstance().getLogger().info("Downloading "+SystemUtil.GI_TAXID_DMP_ARCH+" updates..");
         File gi_taxid_dmp= SystemUtil.downloadFileFromNCBIFTP(tmpDir, new File(SystemUtil.NCBI_TAXONOMY),new File(SystemUtil.GI_TAXID_DMP_ARCH));
         //Extracting files
         Log.getInstance().getLogger().info("Extracting "+SystemUtil.TAXDUMP_ARCH);
         File taxdump_dir=SystemUtil.unArchiveTarGZFile(taxdump_tar_gz,tmpDir);
         Log.getInstance().getLogger().info("Extracting "+SystemUtil.GI_TAXID_DMP_ARCH);
         File gi_taxid_deploy_dir=SystemUtil.unArchiveGZFile(gi_taxid_dmp,tmpDir);
         //Deploying the database
         Log.getInstance().getLogger().info("Deploying Names Database..");
         NamesDeployer.injectProcessedNamesDmpFile(connection, NamesDeployer.filterNamesDmpFile(new File(taxdump_dir, SystemUtil.NAMES_FILE)));
         Log.getInstance().getLogger().info("Deploying GI_TAXID Database..");
        GI_TaxIDDeployer.injectProcessedGI_TaxIDDmpFile(connection, GI_TaxIDDeployer.filterGI_TaxIDDmp(connection, new File(gi_taxid_deploy_dir, SystemUtil.GI_TAXID_NUCL)));
         Log.getInstance().getLogger().info("Deploying Nodes Database..");
         NodesDBDeployer.injectProcessedNodesDmpFile(connection, NodesDBDeployer.filterNodesDmpFile(connection, new File(taxdump_dir,SystemUtil.NODES_FILE)));
         //Reporting
         Log.getInstance().getLogger().info("Database deployed successfully..");
 
     }
     /**
      * Fully updates the NCBI taxonomic database directly from the NCBI FTP server
      * @param connection  {@link Connection} to the database
      * @param tmpDir a {@link File} directory that the temporary update files will be downloaded to
      * @throws IOException upon file read/write errors
      * @throws SQLException in case a database communication error occurs
      */
     public static void updateDatabasesFromNCBI(final Connection connection, final File tmpDir) throws IOException, SQLException {
         //Downloading files
         Log.getInstance().getLogger().info("Downloading updates..");
         Log.getInstance().getLogger().info("Downloading "+SystemUtil.TAXDUMP_ARCH);
         File taxdump_tar_gz=SystemUtil.downloadFileFromNCBIFTP(tmpDir, new File(SystemUtil.NCBI_TAXONOMY),new File(SystemUtil.TAXDUMP_ARCH));
         Log.getInstance().getLogger().info("Downloading "+SystemUtil.GI_TAXID_UPD_FILE_ARCH+" updates..");
         File gi_taxid_update= SystemUtil.downloadFileFromNCBIFTP(tmpDir, new File(SystemUtil.NCBI_TAXONOMY),new File(SystemUtil.GI_TAXID_UPD_FILE_ARCH));
         //Extracting files
         Log.getInstance().getLogger().info("Extracting "+SystemUtil.TAXDUMP_ARCH);
         File taxdump_dir=SystemUtil.unArchiveTarGZFile(taxdump_tar_gz,tmpDir);
         Log.getInstance().getLogger().info("Extracting "+SystemUtil.GI_TAXID_UPD_FILE_ARCH);
         File gi_taxid_update_dir=SystemUtil.unArchiveGZFile(gi_taxid_update,tmpDir);
         //Updating the database
         Log.getInstance().getLogger().info("Updating Names Database..");
         NamesDeployer.injectProcessedNamesDmpFile(connection, NamesDeployer.filterNamesDmpFile(new File(taxdump_dir,SystemUtil.NAMES_FILE)));
         Log.getInstance().getLogger().info("Updating GI_TAXID Database..");
         GI_TaxIDDeployer.injectProcessedGI_TaxIDDmpFile(connection, GI_TaxIDDeployer.filterGI_TaxIDDmp(connection, new File(gi_taxid_update_dir,SystemUtil.GI_TAXID_UPD_FILE)));
         Log.getInstance().getLogger().info("Updating Nodes Database..");
         NodesDBDeployer.injectProcessedNodesDmpFile(connection, NodesDBDeployer.filterNodesDmpFile(connection, new File(taxdump_dir,SystemUtil.NODES_FILE)));
         //Reporting
         Log.getInstance().getLogger().info("Database update completed successfully..");
     }
 
 }
