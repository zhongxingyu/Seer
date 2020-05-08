 package dk.sst.snomedcave.dbgenerate;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.IOException;
 
 public class Initiator {
     private static Logger logger = Logger.getLogger(Initiator.class);
     public static void main(String[] args) throws IOException {
         for (int i = 0; i < args.length; i++) {
             System.out.println("args[" + i + "] = " + args[i]);
         }
 
         File db = new File(SnomedParser.STORE_DIR);
         logger.info("Using neo4j store path=" + db.getAbsolutePath());
         if (db.exists()) {
             logger.info("Path exists. Clearing before import");
             FileUtils.deleteDirectory(db);
         }
 
         SnomedParser snomedParser = new SnomedParser();
 
         if (!ArrayUtils.contains(args, "--skip-concepts")) {
             System.out.println("Will import concepts");
             snomedParser.importConcept();
         }
 
         if (!ArrayUtils.contains(args, "--skip-relationships")) {
             System.out.println("Will import relationships");
             snomedParser.importRelationships();
         }
 
         if (!ArrayUtils.contains(args, "--skip-drugs")) {
             System.out.println("Will import drugs");
             snomedParser.importSubtances();
         }
 
         snomedParser.finish();
     }
 }
