package test;

 import db.mysql.MySQL_Connector;
 import helper.gitaxid.GI_TaxIDDeployer;
 import org.junit.Test;
 
 import java.io.*;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: alext
  * Date: 5/31/13
  * Time: 4:31 PM
  * To change this template use File | Settings | File Templates.
  */
 public class GI_TaxIDDeployerTest {
 
 
     //@Test
     public void testTeployGI_TaxIDTable() throws IOException, SQLException, ClassNotFoundException {
 
         MySQL_Connector mySQL_connector = MySQL_Connector.newDefaultInstance("jdbc:mysql://localhost/", "ocular", "ocular");
         mySQL_connector.connectToDatabase();
         Connection connection = mySQL_connector.getConnection();
 
         GI_TaxIDDeployer.deployGI_TaxIDTable(connection, new File("/home/alext/Downloads/NCBI/gi_taxid_nucl.dmp"));
 
     }
 
     //@Test
     public void countGI_TaxIDLines() throws IOException {
         BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("/home/alext/Downloads/NCBI/gi_taxid_nucl.dmp")));
         int counter = 0;
         String line1;
         while ((line1 = bufferedReader.readLine()) != null) {
             counter++;
             if (counter == 6442861) {
                 System.out.println("prev place found");
                 System.out.println("prev line is: " + counter);
             }
         }
         System.out.println("last line is: " + counter);
     }
 
     //@Test
     public void createBenchmarkInput() throws IOException {
         BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("/home/alext/Downloads/NCBI/gi_taxid_nucl.dmp")));
         StringBuilder stringBuilder = new StringBuilder();
         int counter = 0;
         String line1;
         while ((line1 = bufferedReader.readLine()) != null && counter < 10000) {
 
             counter++;
             stringBuilder.append(line1);
             stringBuilder.append("\n");
         }
         FileWriter fileWriter = new FileWriter(new File("/home/alext/Downloads/NCBI/gi_taxid_nucl_bench.dmp"));
         fileWriter.write(stringBuilder.toString());
         fileWriter.close();
         bufferedReader.close();
         System.out.println("done");
     }
 
     //@Test
     public void testFilterGI_TaxIDDmp() throws SQLException, ClassNotFoundException, IOException {
         MySQL_Connector mySQL_connector = MySQL_Connector.newDefaultInstance("jdbc:mysql://localhost/", "ocular", "ocular");
         mySQL_connector.connectToDatabase();
         Connection connection = mySQL_connector.getConnection();
 
         GI_TaxIDDeployer.filterGI_TaxIDDmp(connection, new File("/home/alext/Downloads/NCBI/gi_taxid_nucl.dmp"));
 
     }
 
     //@Test
     public void injectProcessedGI_TaxIDDmpFileTest() throws SQLException, ClassNotFoundException {
 
         MySQL_Connector mySQL_connector = MySQL_Connector.newDefaultInstance("jdbc:mysql://localhost/", "ocular", "ocular");
         mySQL_connector.connectToDatabase();
         Connection connection = mySQL_connector.getConnection();
 
         File gi_taxidFile = new File("/home/alext/Downloads/NCBI/gi_taxid_nucl.dmp.mod");
 
         GI_TaxIDDeployer.injectProcessedGI_TaxIDDmpFile(connection, gi_taxidFile);
     }
 }
