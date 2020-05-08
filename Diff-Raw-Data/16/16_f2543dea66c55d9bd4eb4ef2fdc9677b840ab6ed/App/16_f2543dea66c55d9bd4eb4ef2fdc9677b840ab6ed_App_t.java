 package net.anzix.snipsnap2xwiki;
 
 import net.anzix.snipsnap2xwiki.migrator.DirOutputter;
 import net.anzix.snipsnap2xwiki.migrator.GitOutputter;
 import net.anzix.snipsnap2xwiki.migrator.Outputter;
 import net.anzix.snipsnap2xwiki.migrator.SnipMigrator;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 
 /**
  * SnipSnap2Jekyll migration spaghetti edition.
  */
 public class App {
 
     public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
 
     @Argument(index = 0, metaVar = "dump_file", usage = "SnipSnap wiki dump file", required = true)
     private File oldFile;
 
     @Argument(index = 1, metaVar = "dest_dir", usage = "Destination directory", required = true)
     private File newDir;
 
     public void migrate() throws Exception {
         try {
             SAXBuilder builder = new SAXBuilder();
             Document d = builder.build(oldFile);
 
             Element snipSnapDumpRoot = d.getRootElement();
 
             MigrationContext context = new MigrationContext(newDir);
             context.init(snipSnapDumpRoot);
            Outputter out = new GitOutputter(context);
             //Migrate wiki pages
             SnipMigrator snipMigrator = new SnipMigrator(context, out);
             snipMigrator.setMigrateAttachments(false);
             snipMigrator.migrate(snipSnapDumpRoot);
 
         } catch (JDOMException ex) {
             ex.printStackTrace();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     public static void main(String[] args) throws Exception {
         App dump = new App();
         CmdLineParser parser = new CmdLineParser(dump);
         try {
             parser.parseArgument(args);
             dump.migrate();
         } catch (CmdLineException ex) {
             System.out.println("ERROR: " + ex.getMessage() + "\n");
             parser.printSingleLineUsage(System.out);
             System.out.println("\n\n");
             parser.printUsage(System.out);
         }
 
     }
 }
