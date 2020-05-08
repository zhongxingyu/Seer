 package de.metalab.namnam.twitterer;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import de.metalab.namnam.importer.jxml.NamNamJXMLImporter;
 import de.metalab.namnam.model.Mensa;
 import de.metalab.namnam.model.Mensaessen;
 import de.metalab.namnam.model.Tagesmenue;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 
 /**
  * base class for twittering daily menues
  * @author fake
  */
 public class NamNamTwitterer {
 
     protected static SimpleDateFormat df;
 
     protected boolean doTwitter = false;
 
     protected Twitter myTwitter;
     protected File    xmlFile;
 
     public static final Logger logger = Logger.getLogger(NamNamTwitterer.class.getName());
 
     public NamNamTwitterer(String fileName, String twitterUser, String twitterPw) throws IOException {
         df = new SimpleDateFormat("E dd.MM.", Locale.GERMAN);
 
         xmlFile = new File(fileName);
         if(!xmlFile.canRead()) {
             throw new IOException("unable to read file!");
         }
 
         myTwitter = new Twitter(twitterUser,twitterPw);
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
             if(result == null || result.getDayMenues() == null || result.getDayMenues().isEmpty()) {
                 logger.log(Level.SEVERE,"@fake666 Gar kein Happa gefunden! Irgendwas stimmt nicht! Hilfe!");
                 if(doTwitter)
                     myTwitter.updateStatus("@fake666 Gar kein Happa gefunden! Irgendwas stimmt nicht! Hilfe!");
 
                 return;
             }
 
             if(!result.hasMenuForDate(theDate)) {
                 logger.log(Level.SEVERE,"Konnte leider kein Happa für "+df.format(theDate)+" finden :(");
                 if(doTwitter)
                     myTwitter.updateStatus("Konnte leider kein Happa für "+df.format(theDate)+" finden :(");
 
                 return;
             }
 
 
             Tagesmenue t = result.getMenuForDate(theDate);
             if(t == null) {
                 logger.log(Level.SEVERE,"@fake666 Leider ist Happa für "+df.format(theDate)+" kaputt :(");
                 if(doTwitter)
                     myTwitter.updateStatus("@fake666 Leider ist Happa für "+df.format(theDate)+" kaputt :(");
             }
             
             Iterator<Mensaessen> mIt = t.getMenues().iterator();
             while(mIt.hasNext()) {
                 Mensaessen me = mIt.next();
                 String date = df.format(theDate) + ": ";
                 String end = " (" + me.getStudentenPreis() + " €/" + me.getPreis() + " €)" ;
                 if(me.isBeef()) {
                     end = " (mit Rindfl.)" + end;
                 } else if(me.isMoslem()) {
                     end = " (k. Schweinefl.)" + end;
                 } else if(me.isVegetarian()) {
                     end = " (vegetarisch)" + end;
                 }
                 String status = date + me.getBeschreibung() + end;
 
                 if(status.length() > 140) {
                     int toomuch = 140 - status.length();
 
                     status = date + me.getBeschreibung().substring(0,toomuch-3) + "..."+ end;
                 }
 
                if(doTwitter)
                    myTwitter.updateStatus(status);
 
                 logger.log(Level.INFO, status);
             }
         } catch (Exception ex) {
             logger.log(Level.SEVERE, "Heute gibt's wohl nix :(",ex);
 
            try {
                if(doTwitter)
                    myTwitter.updateStatus(df.format(theDate) + ": Heute gibt's wohl nix!");
            } catch (TwitterException te) {
                logger.log(Level.SEVERE, "twitterexception while twittering exception!", te);
            }
         }
 
 
     }
 }
