 package net.sf.anathema.tools.conversion;
 
 import java.io.File;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.sf.anathema.basics.importexport.XSLDocumentConverter;
 import net.sf.anathema.lib.xml.DocumentUtilities;
 
 import org.dom4j.Document;
 
 public class CharmTraitConverter {
 
   public static void main(String[] args) throws Exception {
     File sourceFolder = new File(args[0]);
     File targetFolder = new File(args[1]);
     Map<String, String> parameters = new HashMap<String, String>();
    URL sheet = ClassLoader.getSystemResource("net/sf/anathema/tools/conversion/charmTrait.xslt");
     XSLDocumentConverter converter = new XSLDocumentConverter(sheet, parameters);
     File[] allFiles = sourceFolder.listFiles();
     System.err.print(sourceFolder.getCanonicalPath());
     for (File charmFile : allFiles) {
       if (!charmFile.getName().endsWith("xml")) {
         continue;
       }
       Document sourceDocument = DocumentUtilities.read(charmFile);
       Document targetDocument = converter.run(sourceDocument);
       File targetFile = createTargetFile(targetFolder, charmFile);
       DocumentUtilities.save(targetDocument, targetFile);
     }
   }
 
   private static File createTargetFile(File targetFolder, File sourceFile) {
     String sourceName = sourceFile.getName();
     String targetName = "Traits" + sourceName.substring(0, 1).toUpperCase() + sourceName.substring(1);
     return new File(targetFolder, targetName);
   }
 }
