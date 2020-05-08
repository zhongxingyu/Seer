 package eshop.local.persistence;
 
 import eshop.local.exception.KeineEintraegeVorhandenException;
 import eshop.local.exception.KennNummerExistiertNichtException;
 import eshop.local.valueobjects.ArtikelBestandsGraph;
 
 import java.io.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Noshaba
  * Date: 12.05.13
  * Time: 22:56
  * To change this template use File | Settings | File Templates.
  */
 public class Log {
 
     public void writeLog (File dateiName, String text) throws IOException {
         BufferedWriter schreibeStrom = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateiName, true)));
         schreibeStrom.write(text);
         schreibeStrom.newLine();
         schreibeStrom.newLine();
         schreibeStrom.close();
     }
 
     public void writeGraphData (File dateiName, String text) throws IOException{
         BufferedWriter schreibeStrom = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateiName, true)));
         schreibeStrom.write(text);
         schreibeStrom.newLine();
         schreibeStrom.close();
     }
 
     public Vector<ArtikelBestandsGraph> getArtikelGraph(String fileName, int daysInPast, String kennNr, String name) throws FileNotFoundException, ParseException{
         SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'um' HH:mm:ss zzz':'");
         Calendar cal = new GregorianCalendar();
         Date today = new Date();
         // der ganze Log als String
         Vector<String> log = new Vector<String>();
         // der ganze Log als Tokens
         Vector<String[]> eintraege = new Vector<String[]>();
         // alle Datumseinträge als Datum geparst
         Vector<Date> convertedDate = new Vector<Date>();
         // alle geparsten Datumseinträge als Tag des Jahres
         Vector<Integer> daysOfYear = new Vector<Integer>();
         // alle Einträge mit der gewünschten Artikelnummer und dem gewünschten Artikelnamen
         Vector<String[]> neededEntries = new Vector<String[]>();
         // alle Tage des Jahres in dem was mit dem gewünschten Artikel gemacht wurde
         Vector<Integer> neededDaysOfYear = new Vector<Integer>();
         // der letzte Zeit/Datums Eintrag am jeweiligen Tag von den 'neededDaysOfYear'
         Vector<Date> lastDateEntryOfDay = new Vector<Date>();
         // fertige ArtikelGraphobjekte aus den gewünschten Daten
         Vector<ArtikelBestandsGraph> abgObjects = new Vector<ArtikelBestandsGraph>();
 
        //cal.setTime(today);
 
         Scanner filescan = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream("Eshop_BestandsGraph.txt")))).useDelimiter("\n");
 
         while (filescan.hasNext()){
             log.add(filescan.next());
         }
 
         // alle Einträge als Tokens
 
         for (int i = 0; i < log.size(); i++){
             String[] splitResult = log.elementAt(i).split("%");
             eintraege.add(splitResult);
         }
 
         // alle Datumseinträge als Datums-Objekte
 
         for (int i = 0; i < eintraege.size(); i++){
             convertedDate.add(formatter.parse(eintraege.elementAt(i)[0].toString()));
             // alle Datums-Objekte als Tag des Jahres
             cal.setTime(convertedDate.elementAt(i));
             daysOfYear.add(cal.get(Calendar.DAY_OF_YEAR));
         }
 
         // wie viele Tage zurück liegen sollen
 
         cal.setTime(today);
 
         if(daysInPast > 0){
             cal.add(Calendar.DAY_OF_YEAR, -daysInPast);
         } else {
             cal.add(Calendar.DAY_OF_YEAR, daysInPast);
         }
 
         /*
         *  wenn Einträge nach oder am selben Tag wie das eingegebene zurückiegende Datum liegen und den gewünschten
         *  Artikelnamen und Artikelnumemr haben, sollen diese Einträge und deren Datums und die jeweiligen Tage des
         *  Jahres in jeweils einen Vektor gespeichert werden
         */
 
         // alle benötigten Einträge, Datums und Tage des Jares
 
         for (int i = 0; i < eintraege.size(); i++){
             if(eintraege.elementAt(i)[2].equals(kennNr) && eintraege.elementAt(i)[1].equals(name) && convertedDate.elementAt(i).after(cal.getTime()) || eintraege.elementAt(i)[2].equals(kennNr) && eintraege.elementAt(i)[1].equals(name) && convertedDate.elementAt(i).equals(cal.getTime())){
                 neededDaysOfYear.add(daysOfYear.elementAt(i));
                 neededEntries.add(eintraege.elementAt(i));
                 lastDateEntryOfDay.add(convertedDate.elementAt(i));
 
             }
         }
 
         /*
         * der jeweils letzte Eintrag des Tges des jeweiligen Artikels, soll in ein ArtikelBestandsGraph-Objekt
         * gespeichert werden
         */
 
         for (int i = 0; i < neededDaysOfYear.size(); i++){
             if(i < neededDaysOfYear.size() - 1 && !neededDaysOfYear.elementAt(i).equals(neededDaysOfYear.elementAt(i+1)) || i == neededDaysOfYear.size() - 1){
                 ArtikelBestandsGraph abg = new ArtikelBestandsGraph(neededEntries.elementAt(i)[1], Integer.parseInt(kennNr), lastDateEntryOfDay.elementAt(i), Integer.parseInt(neededEntries.elementAt(i)[3].toString()));
                 abgObjects.add(abg);
             }
         }
 
         return abgObjects;
 
     }
 
     public Vector<ArtikelBestandsGraph> getArtikelGraph(String fileName, int daysInPast, String kennNr) throws FileNotFoundException, ParseException{
         SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'um' HH:mm:ss zzz':'");
         Calendar cal = new GregorianCalendar();
         Date today = new Date();
         // der ganze Log als String
         Vector<String> log = new Vector<String>();
         // der ganze Log als Tokens
         Vector<String[]> eintraege = new Vector<String[]>();
         // alle Datumseinträge als Datum geparst
         Vector<Date> convertedDate = new Vector<Date>();
         // alle geparsten Datumseinträge als Tg des Jahres
         Vector<Integer> daysOfYear = new Vector<Integer>();
         // alle Bestände als Integer geparst
         Vector<Integer> convertedValues = new Vector<Integer>();
         // der letzte Zeit/Datums Eintrag am jeweiligen Tag
         Vector<Date> lastDateEntryOfDay = new Vector<Date>();
         // die letzte Bestandsänderung am jeweiligen Tag
         Vector<Integer> lastValueEntryOfDay = new Vector<Integer>();
         // Artikelnummer mit der letzten Änderung des Tages
         Vector<String> neededNumbers = new Vector<String>();
         //Artikelname mit der letzten Änderung des Tages
         Vector<String> neededNames = new Vector<String>();
         // fertige ArtikelGraphobjekte aus den gewünschten Daten
         Vector<ArtikelBestandsGraph> abgObjects = new Vector<ArtikelBestandsGraph>();
 
         cal.setTime(today);
 
         Scanner filescan = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream("Eshop_BestandsGraph.txt")))).useDelimiter("\n");
 
         while (filescan.hasNext()){
             log.add(filescan.next());
         }
 
         // alle Einträge als Tokens
 
         for (int i = 0; i < log.size(); i++){
             String[] splitResult = log.elementAt(i).split("%");
             eintraege.add(splitResult);
         }
 
         // alle Datumseinträge als Datums-Objekte
 
         for (int i = 0; i < eintraege.size(); i++){
             convertedDate.add(formatter.parse(eintraege.elementAt(i)[0].toString()));
         }
 
         // alle Datumseinträge als Tag des Jahres
 
         for (int i = 0; i < convertedDate.size(); i++){
             cal.setTime(convertedDate.elementAt(i));
             daysOfYear.add(cal.get(Calendar.DAY_OF_YEAR));
         }
 
         // alle Bestände als Integer-Objekte
 
         for (int i = 0; i < eintraege.size(); i++){
             convertedValues.add(Integer.parseInt(eintraege.elementAt(i)[3].toString()));
         }
 
         // die letzten Einträge des jeweiligen Tages
 
         for (int i = 0; i < daysOfYear.size(); i++){
             if(i < daysOfYear.size() - 1 && !daysOfYear.elementAt(i).equals(daysOfYear.elementAt(i+1))){
                 lastDateEntryOfDay.add(convertedDate.elementAt(i));
                 lastValueEntryOfDay.add(convertedValues.elementAt(i));
                 neededNames.add(eintraege.elementAt(i)[1]);
                 neededNumbers.add(eintraege.elementAt(i)[2]);
             } else if(i == daysOfYear.size() - 1){
                 lastDateEntryOfDay.add(convertedDate.elementAt(i));
                 lastValueEntryOfDay.add(convertedValues.elementAt(i));
                 neededNames.add(eintraege.elementAt(i)[1]);
                 neededNumbers.add(eintraege.elementAt(i)[2]);
             }
         }
 
         // wie viele Tage zurück liegen sollen
 
         if(daysInPast > 0){
             cal.add(Calendar.DAY_OF_YEAR, -daysInPast);
         } else {
             cal.add(Calendar.DAY_OF_YEAR, daysInPast);
         }
 
         // wenn Einträge nach oder am selben Tag wie das eingegebene zurückiegende Datum liegen, sollen sie in einem
         // ArtikelBestandsGraphen gespeichert werden
 
         for (int i = 0; i < neededNumbers.size(); i++){
             if(neededNumbers.elementAt(i).equals(kennNr) && lastDateEntryOfDay.elementAt(i).after(cal.getTime()) || neededNumbers.elementAt(i).equals(kennNr) && lastDateEntryOfDay.elementAt(i).equals(cal.getTime())){
                 ArtikelBestandsGraph abg = new ArtikelBestandsGraph(neededNames.elementAt(i), Integer.parseInt(kennNr), lastDateEntryOfDay.elementAt(i), lastValueEntryOfDay.elementAt(i));
                 abgObjects.add(abg);
             }
         }
 
         return abgObjects;
 
     }
 
 
 
 
     public Vector<String> printLog (String fileName, int daysInPast, String kennNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
 
         SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'um' HH:mm:ss zzz':'");
         Calendar cal = new GregorianCalendar();
         Date today = new Date();
         Vector<String> log = new Vector<String>();
         Vector<Date> convertedDate = new Vector<Date>();
         Vector<String[]> eintraege = new Vector<String[]>();
         Vector<String> string = new Vector<String>();
 
         cal.setTime(today);
 
         if(daysInPast > 0){
             cal.add(Calendar.DAY_OF_YEAR, -daysInPast);
         } else {
             cal.add(Calendar.DAY_OF_YEAR, daysInPast);
         }
 
         Scanner filescan = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))).useDelimiter("\n");
 
         while (filescan.hasNext()){
             log.add(filescan.next());
         }
 
         filescan.close();
 
         // alle Datumseingaben
         for (int i = 0; i < log.size(); i+=3){
             convertedDate.add(formatter.parse(log.elementAt(i)));
         }
 
         // alle Einträge als Tokens
 
         for (int i = 1; i < log.size(); i+=3){
             String[] splitResult = log.elementAt(i).split(" ");
             eintraege.add(splitResult);
         }
 
         for (int i = 0; i < eintraege.size(); i++){
             for (int j = 0; j < eintraege.elementAt(i).length; j++){
                 if (fileName.equals("Eshop_ArtikelLog.txt") && eintraege.elementAt(i)[j].equals("Artikelnummer") || fileName.equals("Eshop_KundenLog.txt") && eintraege.elementAt(i)[j].equals("Kundennummer") || fileName.equals("Eshop_MitarbeiterLog.txt") && eintraege.elementAt(i)[j].equals("Mitarbeiternummer") || fileName.equals("Eshop_RechnungsLog.txt") && eintraege.elementAt(i)[j].equals("Rechnungsnummer")){
                     if(eintraege.elementAt(i)[j+1].equals(kennNr) && convertedDate.elementAt(i).after(cal.getTime()) || eintraege.elementAt(i)[j+1].equals(kennNr) && convertedDate.elementAt(i).equals(cal.getTime())){
                         string.add(formatter.format(convertedDate.elementAt(i)) + "\n" + log.elementAt(i * 3 + 1) + "\n");
                     }
                 }
             }
         }
 
         if (!string.isEmpty()){
             return string;
         } else {
             if (fileName.equals("Eshop_ArtikelLog.txt")){
                 throw new KennNummerExistiertNichtException("Artikelnummer", kennNr);
             } else if(fileName.equals("Eshop_KundenLog.txt")){
                 throw new KennNummerExistiertNichtException("Kundennummer", kennNr);
             } else if(fileName.equals("Eshop_MitarbeiterLog.txt")){
                 throw new KennNummerExistiertNichtException("Mitarbeiternummer", kennNr);
             } else if(fileName.equals("Eshop_RechnungsLog.txt")){
                 throw new KennNummerExistiertNichtException("Rechnungsnummer", kennNr);
             } else {
                 return null;
             }
         }
     }
 
 
 
 
 
 
     public Vector<String> printLog (String fileName, String kennNr) throws FileNotFoundException, ParseException, KennNummerExistiertNichtException{
 
         SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'um' HH:mm:ss zzz':'");
         Vector<String> log = new Vector<String>();
         Vector<Date> convertedDate = new Vector<Date>();
         Vector<String[]> eintraege = new Vector<String[]>();
         Vector<String> string = new Vector<String>();
 
         Scanner filescan = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))).useDelimiter("\n");
 
         while (filescan.hasNext()){
             log.add(filescan.next());
         }
 
         filescan.close();
 
         // alle Datumseingaben
         for (int i = 0; i < log.size(); i+=3){
             convertedDate.add(formatter.parse(log.elementAt(i)));
         }
 
         // alle Einträge als Tokens
 
         for (int i = 1; i < log.size(); i+=3){
             String[] splitResult = log.elementAt(i).split(" ");
             eintraege.add(splitResult);
         }
 
         for (int i = 0; i < eintraege.size(); i++){
             for (int j = 0; j < eintraege.elementAt(i).length; j++){
                 if (fileName.equals("Eshop_ArtikelLog.txt") && eintraege.elementAt(i)[j].equals("Artikelnummer") || fileName.equals("Eshop_KundenLog.txt") && eintraege.elementAt(i)[j].equals("Kundennummer") || fileName.equals("Eshop_MitarbeiterLog.txt") && eintraege.elementAt(i)[j].equals("Mitarbeiternummer") || fileName.equals("Eshop_RechnungsLog.txt") && eintraege.elementAt(i)[j].equals("Rechnungsnummer")){
                     if(eintraege.elementAt(i)[j+1].equals(kennNr)){
                         string.add(formatter.format(convertedDate.elementAt(i)) + "\n" + log.elementAt(i * 3 + 1) + "\n");
                     }
                 }
             }
         }
 
         if (!string.isEmpty()){
             return string;
         } else {
             if (fileName.equals("Eshop_ArtikelLog.txt")){
                 throw new KennNummerExistiertNichtException("Artikelnummer", kennNr);
             } else if(fileName.equals("Eshop_KundenLog.txt")){
                 throw new KennNummerExistiertNichtException("Kundennummer", kennNr);
             } else if(fileName.equals("Eshop_MitarbeiterLog.txt")){
                 throw new KennNummerExistiertNichtException("Mitarbeiternummer", kennNr);
             } else if(fileName.equals("Eshop_RechnungsLog.txt")){
                 throw new KennNummerExistiertNichtException("Rechnungsnummer", kennNr);
             } else {
                 return null;
             }
         }
     }
 
 
 
 
     public Vector<String> printLog(String fileName, int daysInPast)throws ParseException, FileNotFoundException, KeineEintraegeVorhandenException{
 
         SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'um' HH:mm:ss zzz':'");
         Calendar cal = new GregorianCalendar();
         Date today = new Date();
         Vector<String> log = new Vector<String>();
         Vector<Date> convertedDate = new Vector<Date>();;
         Vector<String> string = new Vector<String>();
 
         cal.setTime(today);
 
         if(daysInPast > 0){
             cal.add(Calendar.DAY_OF_YEAR, -daysInPast);
         } else {
             cal.add(Calendar.DAY_OF_YEAR, daysInPast);
         }
 
         Scanner filescan = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))).useDelimiter("\n");
 
         while (filescan.hasNext()){
             log.add(filescan.next());
         }
 
         filescan.close();
 
         // alle Datumseingaben
         for (int i = 0; i < log.size(); i+=3){
             convertedDate.add(formatter.parse(log.elementAt(i)));
         }
 
 
         for(int i = 0; i < convertedDate.size(); i++){
             if(convertedDate.elementAt(i).after(cal.getTime()) || convertedDate.elementAt(i).equals(cal.getTime())){
                 string.add(formatter.format(convertedDate.elementAt(i)) + "\n" + log.elementAt(i * 3 + 1) + "\n");
             }
         }
 
         if (!string.isEmpty()){
             return string;
         } else {
             throw new KeineEintraegeVorhandenException();
         }
     }
 
 
 
 
     public String printLog(String fileName) throws FileNotFoundException, KeineEintraegeVorhandenException{
 
         String string = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))).useDelimiter("\\A").next();
 
         if(!string.isEmpty()){
             return string;
         } else {
             throw new KeineEintraegeVorhandenException();
         }
     }
 }
