 package org.bytewerk.namnam.twitterer;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bytewerk.namnam.importer.jxml.NamNamJXMLImporter;
 import org.bytewerk.namnam.model.Mensa;
 import org.bytewerk.namnam.model.Mensaessen;
 import org.bytewerk.namnam.model.Tagesmenue;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.http.AccessToken;
 
 /**
  * base class for twittering daily menues
  *
  * @author fake
  */
 public class NamNamTwitterer {
 
     protected static SimpleDateFormat df;
 
     protected boolean doTwitter = false;
 
     protected Twitter myTwitter;
     protected File xmlFile;
 
     public static final Logger logger = Logger.getLogger(NamNamTwitterer.class.getName());
 
    public NamNamTwitterer(String fileName, String cKey, String cSecret, String aToken, String aSecret) throws IOException {
         df = new SimpleDateFormat("E dd.MM.", Locale.GERMAN);
 
         xmlFile = new File(fileName);
         if (!xmlFile.canRead()) {
             throw new IOException("unable to read file!");
         }
 
         if(isDoTwitter()) {
             myTwitter = new TwitterFactory().getInstance();
             myTwitter.setOAuthConsumer(cKey, cSecret);
             AccessToken accessToken = new AccessToken(aToken, aSecret);
             myTwitter.setOAuthAccessToken(accessToken);
         }
     }
 
     public void setDoTwitter(boolean yesno) {
         doTwitter = yesno;
     }
 
     public boolean isDoTwitter() {
         return doTwitter;
     }
 
     public void sendMenue(Date theDate) {
         NamNamJXMLImporter imp = new NamNamJXMLImporter();
 
         try {
             Mensa result = imp.loadFromFile(xmlFile);
             if (result == null || result.getDayMenues() == null || result.getDayMenues().isEmpty()) {
                 logger.log(Level.SEVERE, "@fake666 Gar kein Happa gefunden! Irgendwas stimmt nicht! Hilfe!");
                 if (isDoTwitter())
                     myTwitter.updateStatus("@fake666 Gar kein Happa gefunden! Irgendwas stimmt nicht! Hilfe!");
 
                 return;
             }
 
             if (!result.hasMenuForDate(theDate)) {
                 logger.log(Level.SEVERE, "Konnte leider kein Happa für " + df.format(theDate) + " finden :(");
                 if (isDoTwitter())
                     myTwitter.updateStatus("Konnte leider kein Happa für " + df.format(theDate) + " finden :(");
 
                 return;
             }
 
 
             Tagesmenue t = result.getMenuForDate(theDate);
             if (t == null) {
                 logger.log(Level.SEVERE, "@fake666 Leider ist Happa für " + df.format(theDate) + " kaputt :(");
                 if (isDoTwitter())
                     myTwitter.updateStatus("@fake666 Leider ist Happa für " + df.format(theDate) + " kaputt :(");
             }
 
             Iterator<Mensaessen> mIt = t.getMenues().iterator();
             while (mIt.hasNext()) {
                 Mensaessen me = mIt.next();
                 String date = df.format(theDate) + ": ";
                 String end = " (" + me.getStudentenPreis() + " €/" + me.getPreis() + " €)";
                 if (me.getToken() != null) {
                     end = " - " + me.getToken().getDescription() + end;
                 }
                 String status = date + me.getBeschreibung() + end;
 
                 if (status.length() > 140) {
                     int toomuch = 140 - status.length();
 
                     status = date + me.getBeschreibung().substring(0, toomuch - 3) + "..." + end;
                 }
 
                 if (isDoTwitter()) {
                     while (true) {
                         try {
                             myTwitter.updateStatus(status);
                             break;
                         } catch (TwitterException tex) {
                             logger.log(Level.SEVERE, "tex: ", tex);
                             if (tex.getStatusCode() == 408) {
                                 logger.log(Level.SEVERE, "trying again in 30 seconds");
                                 Thread.sleep(30000);
                             } else {
                                 break;
                             }
                         }
                     }
                 }
 
                 logger.log(Level.INFO, status);
             }
         } catch (Exception ex) {
             logger.log(Level.SEVERE, "Heute gibt's wohl nix :(", ex);
 
             //try {
             //   if(doTwitter)
             //      myTwitter.updateStatus(df.format(theDate) + ": Heute gibt's wohl nix!");
             //} catch (TwitterException te) {
             //   logger.log(Level.SEVERE, "twitterexception while twittering exception!", te);
             //}
         }
 
 
     }
 }
