 package gutenberg.workers;
 
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Properties;
 
 public class Config {
 
     public Config() throws Exception {
         Properties properties = new Properties();
         properties.loadFromXML(Files.newInputStream(Paths.get("/opt/gutenberg/config.xml")));
         bankRoot = properties.getProperty("BANK_ROOT");
     }
 
     public Path getPath(Resource id) {
         Path path = Paths.get(bankRoot);
         switch (id) {
         case bank:
             break;
         case mint:
             path = path.resolve("mint");
             break;
         case locker:
             path = path.resolve("locker");
             break;
         case atm:
             path = path.resolve("atm");
             break;
         case vault:
             path = path.resolve("vault");
             break;
         case shared:
             path = path.resolve("shared");
             break;
         case scantray:
             path = path.resolve("scantray");
             break;
         case frontdesk:
             path = path.resolve("front-desk");
             break;
         default:
         }
         return path;
     }
 
     private String bankRoot;
 }
