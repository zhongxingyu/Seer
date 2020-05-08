 package polly;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import de.skuzzle.polly.sdk.Configuration;
 
 import polly.configuration.ConfigurationFileException;
 import polly.configuration.PollyConfiguration;
 import polly.util.Hashes;
 
 public class MD5Tool {
 
     public static void main(String[] args) throws IOException, ConfigurationFileException {
         String pw = "";
         if (args.length != 1) {
             System.out.println("Bitte gib dein gew�nschtes Password ein:");
             BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
             pw = r.readLine();
         } else {
             pw = args[0];
         }
 
         String hash = Hashes.md5(pw);
         System.out.println(hash);
         System.out.println(
                 "Soll das Passwort in der Konfigurationsdatei gespeichert werden (y/n)?");
         BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
         String line = r.readLine();
        if (line != null && line.equals("y")) {
             PollyConfiguration cfg = new PollyConfiguration("./cfg/polly.cfg");
             cfg.setProperty(Configuration.ADMIN_PASSWORD_HASH, hash);
             cfg.store();
             System.out.println("Passwort wurde gespeichert.");
         } else {
             System.out.println("Passwort wurde nicht gespeichert.");
         }
     }
 }
