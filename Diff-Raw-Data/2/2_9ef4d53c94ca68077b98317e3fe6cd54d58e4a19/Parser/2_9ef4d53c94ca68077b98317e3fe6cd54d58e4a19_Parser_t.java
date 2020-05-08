 package cz.opendata.linked.psp_cz.metadata;
 
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.Normalizer;
 import java.text.Normalizer.Form;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 
 import org.jsoup.nodes.Attributes;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 
 import cz.cuni.mff.xrg.scraper.lib.selector.CssSelector;
 import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
 import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;
 
 /**
  * Specificky scraper pro statni spravu.
  * 
  * @author Jakub Klímek
  */
 
 public class Parser extends ScrapingTemplate{
     
 	public Logger logger;
 	public PrintStream ps;
     private String currentId;
     @Override
     protected LinkedList<ParseEntry> getLinks(org.jsoup.nodes.Document doc, String docType) {
         /* Projedu si stranku a zkusim z ni ziskat linky */
         LinkedList<ParseEntry> out = new LinkedList<>();
         if (docType.equals("list")) {
             /* Na strance se sezname si najdu linky na detaily */
             Elements e = doc.select("div#main-content div.section table tbody tr td:eq(0) a");
             for (int i = 0; i < e.size(); i ++) {
                 try {
                     URL u = new URL("http://www.psp.cz/sqw/" + e.get(i).attr("href"));
                     out.add(new ParseEntry(u, "detail"));
                 } catch (MalformedURLException ex) {
                 	ex.printStackTrace();
                 }
             }
         }
         return out;
     }
     
     private String getLexTypeFromTitle(String title)
     {
         String type;
         if (title.startsWith("Zákon") || title.startsWith("Předseda vlády Československé socialistické republiky vyhlašuje úplné znění") || title.startsWith("Předsednictvo Federálního shromáždění vyhlašuje úplné znění zákona") || title.startsWith("Úplné znění zákona") || title.startsWith("Poslanecká sněmovna setrvává"))
         { 
             type="lex:Act";      
         }
         else if (title.startsWith("Vyháška") ||title.startsWith("Vyhláška") ||title.startsWith(" Vyhláška") ||title.startsWith("&nbsp;Vyhláška")|| title.startsWith("Výnos"))
         {
             type="lex:Regulation";
         }
         else if (title.startsWith("Nařízení vlády")) 
         {
             type="lex:Decree";
         }
         else if (title.startsWith("Rozhodnutí předsedy Senátu")) 
         {
             type="lex:Decree";
         }
         else if (title.startsWith("Rozhodnutí předsedy vlády")) 
         {
             type="lex:Decree";
         }
         else if (title.startsWith("Rozhodnutí vlády")) 
         {
             type="lex:Decree";
         }
         else if (title.startsWith("Vládní nařízení") || title.startsWith("Vládne nariadenie")) 
         {
             type="lex:Decree";
         }
         else if (title.startsWith("Vládní vyhláška") || title.startsWith("Vládní usnesení")) 
         {
             type="lex:Decree";
         }
         else if (title.startsWith("Dekret") || title.startsWith("Rozhodnutí presidenta")|| title.startsWith("Rozhodnutí prezidenta")) 
         {
             type="lex:Decree";
         }
         else if (title.startsWith("Ústavní dekret")) 
         {
             type="lex:Decree";
         }
         else if (title.startsWith("Nařízení soudu")) 
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Trestní zákon")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Finanční zákon")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Devizový zákon")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Rozpočtový zákon")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Nařízení") || title.startsWith("Směrnice")|| title.startsWith("Rámcové podmínky") ||title.startsWith("Pravidla") || title.startsWith("Změna zásad vlády") ||title.startsWith("Změna zásad") ||title.startsWith("Změna Zásad vlády") || title.startsWith("Zásady")|| title.startsWith("Metodické pokyny")|| title.startsWith("Pokyny"))
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Opatření parlamentu")) 
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Opatření vlády")) 
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Opatření České národní banky")) 
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Opatření pověřence")) 
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Opatření ministra")) 
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Opatření Ústřední komise")) 
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Opatření Státní plánovací komise")) 
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Opatření ministerstva")) 
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Ústava")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Ústavní zákon") || title.startsWith("Ústavný zákon")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Branný zákon")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Rozkaz")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Obecný zákoník")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Občanský zákoník")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Hospodářský zákoník")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Obchodní zákoník")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Celní zákon")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Nález Ústavního soudu") || title.startsWith("Nález Ústavního sousu") || title.startsWith("Nález Ůstavního soudu")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Úmluva")) 
         {
             type="lex:Agreement";
         }
         else if (title.startsWith("Smlouva") || title.startsWith("Smluva")) 
         {
             type="lex:Agreement";
         }
         else if (title.startsWith("Česko-Německá úmluva")) 
         {
             type="lex:Agreement";
         }
         else if (title.startsWith("Jednací řád")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Občanský právní řád")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Občanský soudní řád")) 
         {
             type="lex:Act";
         }
         else if (title.startsWith("Dojednanie") || title.startsWith("Dohoda") || title.startsWith("Londýnská dohoda")) 
         {
             type="lex:Agreement";
         }
         else if (title.startsWith("Mírová smlouva")) 
         {
             type="lex:PeaceAgreement";
         }
         else if (title.startsWith("Sdělení")|| title.startsWith("Oznámení")|| title.startsWith("Oznámenie")) 
         {
             type="lex:Notification";
         }
         else if (title.startsWith("Redakční sdělení")) 
         {
             type="lex:Notification";
         }
         else if (title.startsWith("Usnesení")) 
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Stanovy")) 
         {
             type="lex:Decision";
         }
         else if (title.startsWith("Císařský patent")) 
         {
             type="lex:Decree";
         }
         else 
         {
             logger.warn("Found unknown type in " + currentId + ": " + title);
             type="lex:Act";
         }
         return type;
     }
 
     private String getTypeFromTitle(String title)
     {
         return getLexTypeFromTitle(title).substring(4).toLowerCase();
     }
     
     private String getTypDerogace(String string)
     {
         String typ;
         if ("novelizuje".equals(string))
         {
             typ = "novelizuje" ;
         }
         else if ("ruší".equals(string))
         {
             typ = "rusi" ;            
         }
         else if ("úplné znění".equals(string))
         {
             typ = "uplne-zneni" ;            
         }
         else if ("na základě".equals(string))
         {
             typ = "na-zaklade" ;            
         }
         else if ("opravuje".equals(string))
         {
             typ = "opravuje" ;            
         }
         else if ("vztah k".equals(string))
         {
             typ = "vztah-k" ;            
         }
         else
         {
         	logger.warn("Found unknown type of derogace in " + currentId + ": " +  string);
             typ = removeDiacritics(string.replaceAll(" ","-"));
         }
         return typ;
     }
     
     private static String removeDiacritics(String string) {
         return Normalizer.normalize(string, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
     }
     
     @Override
     protected void parse(org.jsoup.nodes.Document doc, String docType) {
         if (docType == "detail") {
             	Elements t1rows = doc.select("div#main-content div.section table tbody tr");
                 String identifier;    
                 try {
                     identifier = t1rows.get(0).getElementsByTag("td").get(1).text();
                 } catch (NullPointerException e) {
                     return;
                 }
                 if (identifier == null) return;
                 currentId = identifier;
                 identifier = identifier.replaceAll("([^ ]*) Sb." , "$1");
                String title = t1rows.get(1).getElementsByTag("td").get(1).text().replace("\"", "\\\"");
                 String castka = t1rows.get(2).getElementsByTag("td").get(1).text();
                 String castka_cislo = castka.replaceAll("([0-9]*) \\((.*)\\)","$1");
                 //WARNING: space in replace is not a regular space but &nbsp;
                 String castka_datum_old = castka.replace(". ", ".").replaceAll("([0-9]*) \\((.*)\\)","$2");
                 String castka_datum_new;
                 try
                 {
                     Date castka_datum = new SimpleDateFormat("dd.MM.yyyy").parse(castka_datum_old);
                     castka_datum_new = new SimpleDateFormat("yyyy-MM-dd").format(castka_datum);
                 }
                 catch (Exception e)
                 {
                 	logger.info("Found unparsable castka date in " + identifier + ": " + castka_datum_old);
                     castka_datum_new = "other";
                 }
                 
                 String year = identifier.replaceAll("[^/]*/([0-9]{4}).*", "$1");
                 String cislo = identifier.replaceAll("([^/]*)/[0-9]{4}.*", "$1");
                 
                 String validFromNew;
                 //WARNING: space in replace is not a regular space but &nbsp;
                 String validFromOld = t1rows.get(3).getElementsByTag("td").get(1).text().replace(". ", ".").replaceAll("[od][od] ([^,]*),?.*", "$1");
                 Date validFromDate;
                 try {
                     validFromDate = new SimpleDateFormat("dd.MM.yyyy").parse(validFromOld);
                     validFromNew = new SimpleDateFormat("yyyy-MM-dd").format(validFromDate);
                 }
                 catch (Exception e)
                 {
                     logger.info("Found unparsable valid to and valid-from date in " + identifier + ": " + validFromOld);
                     validFromNew = "other";
                 }
                 
                 String type = getLexTypeFromTitle(title);
                 
                 String actUri = "http://linked.opendata.cz/resource/legislation/cz/" + getTypeFromTitle(title).toLowerCase() + "/" + year + "/" + cislo + "-" + year;
                 ps.println("<" + actUri + ">\n\ta " + type + " ;");
                 ps.println("\tdcterms:title \"" + title +"\"@cs ;");
                 ps.println("\tdcterms:identifier \"" + identifier + "\" ;");
                 ps.println("\tdcterms:creator <http://linked.opendata.cz/resource/cz/authority/parliament>");
                 ps.println("\t.");
                 ps.println("");
                 
                 String expressionUri = actUri + "/expression/cz/" + getTypeFromTitle(title).toLowerCase() + "/" + year + "/" + cislo + "-" + year + "/cs";
                 /*if ("other".equals(validFromNew))
                 {
                     if (!"other".equals(castka_datum_new))
                     {
                         expressionUri = actUri + "/version/cz/" + castka_datum_new;
                     }
                     else
                     {
                         expressionUri = actUri + "/version/cz/expression";
                     }
                 }
                 else
                 {
                     expressionUri = actUri + "/version/cz/" + validFromNew;
                 }
                 */
                 
                 ps.println("<" + expressionUri + ">\n\ta " + "frbr:Expression" + " ;");
                 ps.println("\tdcterms:title \"" + title +"\"@cs ;");
                 
                 if (!"other".equals(validFromNew)) ps.println("\tdcterms:valid \""+validFromNew+"\"^^xsd:date ;");
                 
                 ps.println("\todcs:castka-cislo \"" + castka_cislo + "\" ;");
                 if (!"other".equals(castka_datum_new)) ps.println("\todcs:castka-datum \"" + castka_datum_new + "\"^^xsd:date ;");
                 
                 Elements derogace_aktivni = doc.select("div#main-content div.section table:eq(3) tbody tr:not(tr:contains(Derogace pasivní)~tr)");
                 for (Element da : derogace_aktivni)
                 {
                     Elements cols = da.getElementsByTag("td");
                     if (cols.size() > 2) {
 	                	String typ_derogace = getTypDerogace(cols.get(1).text());
 	                    String nazev_derogace = cols.get(2).text();
 	                    String cil_derogace = cols.get(0).getElementsByTag("a").get(0).text();
 	                    String year_derogace = cil_derogace.replaceAll("[^/]*/([0-9]{4}).*", "$1");
 	                    String cislo_derogace = cil_derogace.replaceAll("([^/]*)/[0-9]{4}.*", "$1");
 	                    String uri_derogace = "http://linked.opendata.cz/resource/legislation/cz/" + getTypeFromTitle(nazev_derogace) + "/" + year_derogace + "/" + cislo_derogace + "-" + year_derogace;
 	
 	                    ps.println("\todcs:aktivni-" + typ_derogace + " <" + uri_derogace + "> ;");
                     }
                 }
 
                 Elements derogace_pasivni = doc.select("div#main-content div.section table:eq(3) tbody tr:contains(Derogace pasivní)~tr:not(tr:contains(Vztahováno k)~tr)");
                 for (Element dp : derogace_pasivni)
                 {
                     Elements cols = dp.getElementsByTag("td");
                     if (cols.size() > 2) {
 	                	String typ_derogace = getTypDerogace(cols.get(1).text());
 	                    String nazev_derogace = cols.get(2).text();
 	                    String cil_derogace = cols.get(0).getElementsByTag("a").get(0).text();
 	                    String year_derogace = cil_derogace.replaceAll("[^/]*/([0-9]{4}).*", "$1");
 	                    String cislo_derogace = cil_derogace.replaceAll("([^/]*)/[0-9]{4}.*", "$1");
 	                    String uri_derogace = "http://linked.opendata.cz/resource/legislation/cz/" + getTypeFromTitle(nazev_derogace) + "/" + year_derogace + "/" + cislo_derogace + "-" + year_derogace;
 	
 	                    ps.println("\todcs:pasivni-" + typ_derogace + " <" + uri_derogace + "> ;");
                     }
                 }
 
                 Elements vztahy_k = doc.select("div#main-content div.section table:eq(3) tbody tr:contains(Vztahováno k)~tr");
                 for (Element vk : vztahy_k)
                 {
                     Elements cols = vk.getElementsByTag("td");
                     if (cols.size() > 2) {
 	                	String typ_derogace = getTypDerogace(cols.get(1).text());
 	                    String nazev_derogace = cols.get(2).text();
 	                    String cil_derogace = cols.get(0).getElementsByTag("a").get(0).text();
 	                    String year_derogace = cil_derogace.replaceAll("[^/]*/([0-9]{4}).*", "$1");
 	                    String cislo_derogace = cil_derogace.replaceAll("([^/]*)/[0-9]{4}.*", "$1");
 	                    String uri_derogace = "http://linked.opendata.cz/resource/legislation/cz/" + getTypeFromTitle(nazev_derogace) + "/" + year_derogace + "/" + cislo_derogace + "-" + year_derogace;
 	
 	                    ps.println("\todcs:vztahovano-k-" + typ_derogace + " <" + uri_derogace + "> ;");
                     }
                 }
                 
                 ps.println("\tfrbr:realizationOf <" + actUri + ">");
                 ps.println("\t.");
                 ps.println("");
 
                 Elements embodyments = doc.select("div#main-content div.section ul li a");
                 for (Element e:embodyments)
                 {
                     ps.println("<" + e.attr("href") + ">\n\ta frbr:Manifestation ;");
                     ps.println("\tfrbr:embodymentOf <" + expressionUri + ">");
                     ps.println("\t.");
                     ps.println("");
                 }
         }
     }
 }
