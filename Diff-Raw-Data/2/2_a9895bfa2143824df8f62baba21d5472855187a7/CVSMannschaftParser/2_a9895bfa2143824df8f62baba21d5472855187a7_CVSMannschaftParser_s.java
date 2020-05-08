 package com.googlecode.madschuelerturnier.business.dataloader;
 
 import au.com.bytecode.opencsv.CSVReader;
 import com.google.common.base.Charsets;
 import com.google.common.io.Resources;
 import com.googlecode.madschuelerturnier.model.Mannschaft;
 import com.googlecode.madschuelerturnier.model.enums.GeschlechtEnum;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Component;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author $Author: marthaler.worb@gmail.com $
  * @since 0.7
  */
 @Component
 public class CVSMannschaftParser {
 
     private static final Logger LOG = Logger.getLogger(CVSMannschaftParser.class);
 
     public synchronized List<Mannschaft> loadMannschaften4Jahr(String jahr, Boolean knaben, Integer klasse) {
         List<Mannschaft> result = new ArrayList<Mannschaft>();
         List<Mannschaft> alle = parseFileContent(loadCSVFile(jahr));
 
         if (knaben == null && klasse == null) {
             return alle;
         }
 
         if (knaben != null) {
 
             for (Mannschaft mannschaft : alle) {
                 if (knaben) {
                     if (mannschaft.getGeschlecht() == GeschlechtEnum.K) {
                         result.add(mannschaft);
                     }
                 } else {
                     if (mannschaft.getGeschlecht() == GeschlechtEnum.M) {
                         result.add(mannschaft);
                     }
                 }
 
             }
             alle = result;
         }
 
         if (klasse != null) {
 
             for (Mannschaft mannschaft : alle) {
                 if (mannschaft.getKlasse() == klasse) {
                     result.add(mannschaft);
                 }
             }
         }
         return result;
     }
 
     public String loadCSVFile(String jahr) {
 
         String text = "";
         URL url = Resources.getResource("testmannschaften-orig-" + jahr + ".csv");
         try {
             text = Resources.toString(url, Charsets.UTF_8);
         } catch (IOException e) {
             LOG.error(e.getMessage(), e);
         }
         return text;
 
     }
 
     public List<Mannschaft> parseFileContent(String text) {
         List<Mannschaft> result = new ArrayList<Mannschaft>();
         LOG.info("parse: " + text);
         CSVReader reader = new CSVReader(new StringReader(text), ',', '\"', 1);
 
         try {
             List<String[]> lines = reader.readAll();
             for (String[] line : lines) {
                 Mannschaft m = parseLine(line);
                 if (m != null) {
                     result.add(m);
                 }
             }
         } catch (IOException e) {
             LOG.error(e.getMessage(), e);
         }
         return result;
     }
 
    public Mannschaft parseLine(String[] myEntry) {
         Long id = null;
         Mannschaft mann = new Mannschaft();
 
         // 0  Id
         if (myEntry[0] != null && !"".equals(myEntry[0])) {
             id = Long.parseLong(myEntry[0]);
         }
 
         // 1  Spieljahr
         try {
             mann.setSpielJahr(Integer.parseInt(myEntry[1].trim()));
         } catch (Exception e) {
             LOG.error("beim parsen, zeile ohne jahrzahl " + myEntry[1]);
             return null;
         }
         // 3  Captain Name
         mann.setCaptainName(myEntry[3].trim());
 
         // 4  Captain Telefon
         mann.setCaptainTelefon(myEntry[4].trim());
 
         // 5  Captain Email
         mann.setCaptainEmail(myEntry[5].trim());
 
         // 6  Begleitperson Name
         mann.setBegleitpersonName(myEntry[6].trim());
 
         // 7  Begleitperson Telefon
         mann.setBegleitpersonTelefon(myEntry[7].trim());
 
         // 8  Begleitperson Email
         mann.setBegleitpersonEmail(myEntry[8].trim());
 
         // 9  Schulhaus
         mann.setSchulhaus(myEntry[9].trim());
 
         // 10 M / K
         if (myEntry[10].equals("K")) {
             mann.setGeschlecht(GeschlechtEnum.K);
         } else {
             mann.setGeschlecht(GeschlechtEnum.M);
         }
 
         // 11 Klasse
         mann.setKlasse(Integer.parseInt(myEntry[11].trim()));
 
         // 12 Klasse Bez.
         mann.setKlassenBezeichnung(myEntry[12].trim());
 
         // 13 Spieler
         try {
             mann.setAnzahlSpieler(Integer.parseInt(myEntry[13].trim()));
         } catch (Exception e) {
             mann.setAnzahlSpieler(20);
         }
 
         // 14 Notizen
         mann.setNotizen(myEntry[14]);
         if (myEntry.length > 15) {
             mann.setSpielWunschHint(myEntry[15]);
         }
 
         return mann;
     }
 
 }
