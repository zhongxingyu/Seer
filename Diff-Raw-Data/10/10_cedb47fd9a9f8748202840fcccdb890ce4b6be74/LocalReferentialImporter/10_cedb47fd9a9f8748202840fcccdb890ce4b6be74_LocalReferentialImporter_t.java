 package fr.cg95.cvq.util.admin;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import au.com.bytecode.opencsv.CSVReader;
 import fr.cg95.cvq.business.authority.LocalReferentialEntry;
 import fr.cg95.cvq.business.authority.LocalReferentialType;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqLocalReferentialException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.ILocalReferentialService;
 import fr.cg95.cvq.service.authority.impl.LocalAuthorityRegistry;
 
 public class LocalReferentialImporter {
     private static Logger logger = Logger.getLogger(LocalReferentialImporter.class);
     
     private static ILocalAuthorityRegistry localAuthorityRegistry;
     private static ILocalReferentialService localReferentialService;
 
     private static Set<String> authorizedLrTypeDataNames = new HashSet<String>();
     static {
         authorizedLrTypeDataNames.add("TaxHouseholdCity");
         authorizedLrTypeDataNames.add("CurrentSchoolName");
     }
     
     public void csvToLocalReferential(String csvFileName, String lrTypeDataName) {
         String localAuthorityName = SecurityContext.getCurrentSite().getName();
         logger.info("local authority= " + localAuthorityName);
         logger.info("csv file= " + csvFileName);
         
         if (!authorizedLrTypeDataNames.contains(lrTypeDataName)) {
             System.out.println(" ERROR - args[2] must be one of : ");
             for(String dataName : authorizedLrTypeDataNames)
                 System.out.println(" . " + dataName);
             System.exit(0);
         }
         
         File csvFile = new File(csvFileName);
         if (!csvFile.exists()) {
             System.out.println(" ERROR - " + csvFileName + " does not exist");
             System.exit(0);
         }
                 
         try {
             LocalReferentialType lrType = 
                 localReferentialService.getLocalReferentialDataByName(lrTypeDataName);
             if (lrType.getEntries() != null) {
                 Set<LocalReferentialEntry> lrEntriesCopy = new HashSet<LocalReferentialEntry>(lrType.getEntries());
                 Iterator<LocalReferentialEntry> it = lrEntriesCopy.iterator();
                 while(it.hasNext()) {
                     LocalReferentialEntry lrEntry = it.next();
                     lrType.removeEntry(lrEntry, null);
                 }
             }
             
             Reader csvFileReader = new StringReader(new String(getBytesFromFile(csvFile)));
             CSVReader csvReader = new CSVReader(csvFileReader,';','"',1);
             
             for (Object o : csvReader.readAll()) {
                 String[] line = (String[])o;
                 logger.info(line[0]);
                 LocalReferentialEntry lrEntry = new LocalReferentialEntry();
                 lrEntry.addLabel("fr", line[0]);
                 lrType.addEntry(lrEntry, null);
             }
             
             localReferentialService.setLocalReferentialData(lrType);
         } catch (IOException ioe) {
             ioe.printStackTrace();
         } catch (CvqLocalReferentialException cvqlre) {
             cvqlre.printStackTrace();
         } catch (CvqException cvqe) {
             cvqe.printStackTrace();
         }
         logger.info("Local referential import OK");
     }
     
     public static void main(final String[] args) throws Exception {
         
         Logger rootLogger = Logger.getRootLogger();
         rootLogger.setLevel(Level.OFF);
         logger.setLevel(Level.INFO);
         
         if (args.length == 0 || args[0].equals("help") || args.length < 3){
             System.out.println(" USAGE - . ./invoke_localreferential_importer.sh [MODE] [CSV_FILE] [DATANAME]");
             System.out.println("  - [MODE] : One of {deployment | dev | help }");
             System.out.println("  - [CSV_FILE] : Csv file to import as local referential (separator=';' / quotechar='\"' / first line ignored)");
             System.out.println("  - [DATANAME] : LocalReferential data to update. One of");
             for(String dataName : authorizedLrTypeDataNames)
                 System.out.println("     . " + dataName);
             
             System.exit(0);
         }
         
         String config = args[0];
         String csvFileName = args[1];
         String lrTypeDataName = args[2];
         
        ClassPathXmlApplicationContext cpxa = SpringApplicationContextLoader.loadContext(config);
         localAuthorityRegistry = (LocalAuthorityRegistry)cpxa.getBean("localAuthorityRegistry");
         localReferentialService = (ILocalReferentialService)cpxa.getBean("localReferentialService");
         
         LocalReferentialImporter lrImporter = new LocalReferentialImporter();
         localAuthorityRegistry.browseAndCallback(lrImporter, "csvToLocalReferential", new Object[]{csvFileName,lrTypeDataName});
     }
     
     // File Util method (copy from web)
     private byte[] getBytesFromFile(File file) throws IOException {
         InputStream is = new FileInputStream(file);
         // Get the size of the file
         long length = file.length();
         if (length > Integer.MAX_VALUE) {
             // File is too large
         }
         // Create the byte array to hold the data
         byte[] bytes = new byte[(int)length];
         // Read in the bytes
         int offset = 0;
         int numRead = 0;
         while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
             offset += numRead;
         }
         // Ensure all the bytes have been read in
         if (offset < bytes.length) {
             throw new IOException("Could not completely read file "+file.getName());
         }
         // Close the input stream and return bytes
         is.close();
         return bytes;
     }
     
 }
