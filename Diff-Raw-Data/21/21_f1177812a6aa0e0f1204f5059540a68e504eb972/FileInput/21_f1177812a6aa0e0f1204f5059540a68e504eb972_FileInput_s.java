 package org.nypl.mss.dgi;
 
 import java.io.*;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.maven.wagon.CommandExecutionException;
 import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;
 
 public class FileInput {
     private File file;
     private String sigFile = "/Users/dm/.droid6/signature_files/DROID_SignatureFile_V60.xml";
     private String host;
     private int port;
     private int timeout;
     
     FileInput(String fileIn) throws FileNotFoundException, IOException, CommandExecutionException{
         this.file = new File(fileIn);
         
         if(this.file.exists()){
             //getProps();
             droid6();
             clamav();
         } else {
             System.exit(0);
         }
         
     }
 
     private void droid6() throws FileNotFoundException, IOException, CommandExecutionException {
         BinarySignatureIdentification bin = new BinarySignatureIdentification(file, sigFile);
         List<IdentificationResult> resultList = bin.getResultList();
         List<IdentificationResult> extResultList = bin.getExtResultList();
         if(resultList.size() == 1){
             IdentificationResult result = resultList.get(0);
  
             
             System.out.println("droidMatch: true");
             System.out.println("droidPuid: " + result.getPuid());
             if(result.getMimeType() != null)
            System.out.println("droidMimeType: " + result.getMimeType());
             if(result.getName() != null)
            System.out.println("droidFileName: " + result.getName());
             if(result.getVersion() != null)
            System.out.println("droidFileVersion: " + result.getVersion());
             System.out.println("droidIdentificationMethod: binary signature" );
         }
         if(resultList.isEmpty()){
             if(extResultList.isEmpty()){
                 System.out.println("droidMatch: false");
             }
             else{
                 IdentificationResult result = extResultList.get(0);
                 System.out.println("droidMatch: true");
                 System.out.println("droidPuid: " + result.getPuid());
                    if(result.getMimeType() != null)
                System.out.println("droidMimeType: " + result.getMimeType());
                    if(result.getName() != null)
                System.out.println("droidFileName: " + result.getName());
                    if(result.getVersion() != null)
                System.out.println("droidFileVersion: " + result.getVersion());
                 System.out.println("droidIdentificationMethod: extension" );
             }
         }
     }
 
     private void clamav() throws FileNotFoundException {
         Logger logger = Logger.getLogger("org.nypl.mss.dgi.ClamScan");
         logger.setLevel(Level.OFF);
         BasicConfigurator.configure();
         
         InputStream fis = new FileInputStream(file);
         ClamScan c = new ClamScan("127.0.0.1", 3310, 600000000);
         
         ScanResult result = c.scan(fis);
                 Pattern pattern = Pattern.compile("FOUND");
         Matcher matcher = pattern.matcher(result.getResult());
         
         if(matcher.find()){
             System.out.println("virusFound: true");
             System.out.println("VirusSignature: " + result.getSignature());
         }
         else{
             System.out.println("virusFound: false");
         }
     }
     
     private void getProps() throws FileNotFoundException, IOException {
         Properties prop = new Properties();
         prop.load(new FileInputStream("nyplDgiProps.properties"));
         
         sigFile = prop.getProperty("droidSignatureFileLoc");
         host = prop.getProperty("clamavHost");
         port = Integer.parseInt(prop.getProperty("clamavPort"));
         timeout = Integer.parseInt(prop.getProperty("clamavTimeout"));
     }
     
     public static void main(String[] args) throws FileNotFoundException, IOException, CommandExecutionException{
         FileInput f = new FileInput(args[0]);
     }  
 }
