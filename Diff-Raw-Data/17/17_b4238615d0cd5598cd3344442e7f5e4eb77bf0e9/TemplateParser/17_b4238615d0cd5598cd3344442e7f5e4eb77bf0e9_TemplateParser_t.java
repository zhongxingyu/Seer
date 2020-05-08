 package cl;
 
 import com.google.common.base.Preconditions;
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.pharmgkb.ExcelParser;
 import org.pharmgkb.util.CliHelper;
 import org.pharmgkb.util.HibernateUtils;
 import org.pharmgkb.util.IcpcUtils;
 
 import java.io.File;
 
 /**
  * Created by IntelliJ IDEA.
  * User: whaleyr
  * Date: 8/28/12
  */
 public class TemplateParser {
   private static final Logger sf_logger = Logger.getLogger(TemplateParser.class);
   private File m_templateFile = null;
   private File m_templateDirectory = null;
 
   public static void main(String args[]) {
     try {
       HibernateUtils.init();
 
       TemplateParser parser = new TemplateParser();
       parser.parseCommandLineArgs(args);
       parser.parse();
     }
     catch (Exception ex) {
       sf_logger.error("Couldn't run parser", ex);
     }
     finally {
       HibernateUtils.shutdown();
     }
   }
 
   private void parse() throws Exception {
     if (getTemplateFile() != null) {
       parseFile(getTemplateFile());
     }
     else if (getTemplateDirectory() != null) {
       parseDirectory(getTemplateDirectory());
     }
   }
 
   private void parseFile(File file) throws Exception {
     Preconditions.checkNotNull(file);
     Preconditions.checkArgument(file.exists(), "File does not exist: %s", file);
 
     File outputFile = IcpcUtils.getOutputFile(file);
 
     ExcelParser parser = new ExcelParser(file);
     parser.parse(outputFile);
   }
 
   private void parseDirectory(File directory) throws Exception {
     Preconditions.checkNotNull(directory);
     Preconditions.checkArgument(directory.exists(), "File does not exist: %s", directory);
     Preconditions.checkArgument(directory.isDirectory(), "File is not directory: %s", directory);
 
     for (File file : FileUtils.listFiles(directory, new String[]{"xlsx","xls"}, false)) {
       try {
        if (file.getName().startsWith("~")) {
          continue;
        }
         parseFile(file);
       }
       catch (IllegalArgumentException ex) {
         sf_logger.error("Couldn't parse file "+file, ex);
       }
     }
   }
 
   private void parseCommandLineArgs(String args[]) throws Exception{
     CliHelper cliHelper = new CliHelper(getClass(), false);
 
     cliHelper.addOption("f", "file", "ICPC excel template file to read", "pathToFile");
     cliHelper.addOption("d", "directory", "ICPC excel template directory to read", "pathToDirectory");
 
     try {
       cliHelper.parse(args);
       if (cliHelper.isHelpRequested()) {
         cliHelper.printHelp();
         System.exit(1);
       }
     } catch (Exception ex) {
       throw new Exception("Error parsing arguments", ex);
     }
 
     if (cliHelper.hasOption("-f")) {
       File templateFile = new File(cliHelper.getValue("-f"));
       if (templateFile.exists()) {
         setTemplateFile(templateFile);
       }
       else {
         throw new Exception("File not found "+cliHelper.getValue("-f"));
       }
     }
 
     if (cliHelper.hasOption("-d")) {
       File templateDirectory = new File(cliHelper.getValue("-d"));
       if (templateDirectory.exists()) {
         setTemplateDirectory(templateDirectory);
       }
       else {
         throw new Exception("Directory doesn't exist "+cliHelper.getValue("-d"));
       }
     }
   }
 
 
   public File getTemplateFile() {
     return m_templateFile;
   }
 
   public void setTemplateFile(File templateFile) {
     m_templateFile = templateFile;
   }
 
   public File getTemplateDirectory() {
     return m_templateDirectory;
   }
 
   public void setTemplateDirectory(File templateDirectory) {
     m_templateDirectory = templateDirectory;
   }
 }
