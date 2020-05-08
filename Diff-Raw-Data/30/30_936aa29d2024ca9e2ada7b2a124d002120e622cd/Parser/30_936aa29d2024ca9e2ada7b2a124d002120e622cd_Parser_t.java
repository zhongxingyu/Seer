 package cz.opendata.linked.cz.gov.agendy;
 
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 
 import cz.cuni.mff.xrg.scraper.lib.selector.CssSelector;
 import cz.cuni.mff.xrg.scraper.lib.template.ParseEntry;
 import cz.cuni.mff.xrg.scraper.lib.template.ScrapingTemplate;
 
 /**
  * Specificky scraper pro statni spravu.
  * 
  * @author Jakub Starka
  */
 
 public class Parser extends ScrapingTemplate{
     
 	public Logger logger;
 	public PrintStream ps;
     private int count = 0;
     private int total;
     
     private String turtleEscape(String input)
     {
     	return input.replace("\\", "\\\\").replace("\"", "\\\"");
     }
     
     @Override
     protected LinkedList<ParseEntry> getLinks(org.jsoup.nodes.Document doc, String docType) {
         /* Projedu si stranku a zkusim z ni ziskat linky */
         LinkedList<ParseEntry> out = new LinkedList<>();
         if (docType.equals("list")) {
             /* Na strance se sezname si najdu linky na detaily */
         	Elements e = doc.select("body table tbody tr td:eq(0) a");
             total = e.size() - 1;
         	logger.info("Found " + total + " agendas.");
             for (int i = 0; i < e.size(); i ++) {
                 try {
                     URL u = new URL("https://rpp-ais.egon.gov.cz/gen/agendy-detail/" + e.get(i).attr("href"));
                     out.add(new ParseEntry(u, "detail"));
                 } catch (MalformedURLException ex) {
                 	ex.printStackTrace();
                 }
             }
         }
         return out;
     }
     private String uriSlug(String input)
     {
     	return input.toLowerCase().replace(" ", "-").replace(".", "-").replace("–", "-").replace(",", "-").replace("(", "-").replace("§", "-").replace("*", "-").replace("/", "-").replace(")", "-").replace("--", "-").replace("--", "-");
     }
 
     private void printIfNotNullOrEmpty(String before, String what, String after)
     {
     	if (what != null && !what.isEmpty()) ps.println(before + what + after);
     }
     
     @Override
     protected void parse(org.jsoup.nodes.Document doc, String docType) {
         if (docType == "detail") {
                 count++;
                 logger.debug("Parsing agenda " + count + "/" + total);
 
                 // Parse text
                 String body = new CssSelector(doc, "body").getHtml();
                 
                 String[] parts = body.split(":");
                 String kodAgendy = parts[1].replaceAll(" ([^ ]+).*", "$1");
                 String nazevAgendy = parts[2].replaceAll(" (.*) Datum registrace agendy", "$1");
                 String datumRegistrace = parts[3].replaceAll(" ([^ ]*) Platnost od", "$1").replaceAll("([0-9]{2})\\.([0-9]{2})\\.([0-9]{4})", "$3-$2-$1");
                 String platnostOd = parts[4].replaceAll(" ([^ ]*) Platnost do", "$1").replaceAll("([0-9]{2})\\.([0-9]{2})\\.([0-9]{4})", "$3-$2-$1");
                 String platnostDo = parts[5].replaceAll(" ([^ ]*) Ohlašovatel agendy", "$1").replaceAll("([0-9]{2})\\.([0-9]{2})\\.([0-9]{4})", "$3-$2-$1");
                 if (platnostDo.equals(" Ohlašovatel agendy")) platnostDo = null;
                 
                 logger.debug("Kod agendy " + kodAgendy);
 
                //String ohlasovatelAgendy = parts[6].replaceAll(" (.*) Identifikace OVM", "$1");
                //if (ohlasovatelAgendy.equals(" Identifikace OVM")) ohlasovatelAgendy = null;
                 String identifikaceOVM = parts[7].replaceAll(" (.*) Název OVM", "$1");
                 String nazevOVM = parts[8].replaceAll(" (.*) A\\) Právní předpisy, na jejichž základě je agenda vykonávána", "$1");
                 
                 String URLagendy = "http://linked.opendata.cz/resource/domain/seznam.gov.cz/agendy/" + kodAgendy + "/" + platnostOd;
                 //Output general data to Turtle
                 ps.println("<" + URLagendy + "> a ovm-a:Agenda, skos:Concept ;");
                 ps.println("\tskos:inScheme ovm-r:AgendyConceptScheme ;");
                 printIfNotNullOrEmpty("\tskos:notation \"", kodAgendy, "\" ;");
                 printIfNotNullOrEmpty("\tskos:prefLabel \"", nazevAgendy, "\" ;");
                 printIfNotNullOrEmpty("\tovm-a:datumRegistrace \"", datumRegistrace, "\"^^xsd:date ;");
                 printIfNotNullOrEmpty("\tovm-a:platnostOd \"", platnostOd, "\"^^xsd:date ;");
                 printIfNotNullOrEmpty("\tovm-a:platnostDo \"", platnostDo, "\"^^xsd:date ;");
                //printIfNotNullOrEmpty("\tovm-a:ohlasovatelAgendy \"", ohlasovatelAgendy, "\" ;");
                //printIfNotNullOrEmpty("\tovm-a:identifikaceOVM \"", identifikaceOVM, "\" ;");
                printIfNotNullOrEmpty("\tovm-a:ohlasovatelAgendy <http://linked.opendata.cz/resource/business-entity/CZ", identifikaceOVM, "> ;");
                 printIfNotNullOrEmpty("\tovm-a:nazevOVM \"", nazevOVM, "\" ;");
                 
                 Elements tables = doc.select("body table");
                 
                 Element tableA = tables.get(0);
                 
                 for (Element e: tableA.select("tr:has(td:eq(1))"))
                 {
                 	String currentCislo = e.select("td:eq(0)").first().text();
                 	if (currentCislo.contains("/")) currentCislo = currentCislo.substring(0, currentCislo.indexOf("/"));
                 	String currentRok = e.select("td:eq(1)").first().text();
                 	String currentNazev = e.select("td:eq(2)").first().text(); 
                 	String currentParagraf = e.select("td:eq(3)").first().text().replace(" ", "").replace("čl.", ""); 
                 	String currentOdstavec = e.select("td:eq(4)").first().text().replace(" ", "");
                 	String currentPismeno = e.select("td:eq(5)").first().text().replace(" ", "");
                 	
                 	try {
 						StringBuilder uriBuilder = new StringBuilder("http://linked.opendata.cz/resource/legislation/cz/act");
 						if (currentRok != null && currentCislo != null && !currentRok.isEmpty() && ! currentCislo.isEmpty() && !currentCislo.equals("...")) {
 							uriBuilder.append("/" + currentRok + "/" + currentCislo + "-" + currentRok);
 							
 							if (currentParagraf != null && !currentParagraf.isEmpty())
 							{
 								if (currentParagraf.contains("-"))
 								{
 									int pod = Integer.parseInt(currentParagraf.substring(0, currentParagraf.indexOf("-")));
 									int pdo;
 									try {
 										pdo = Integer.parseInt(currentParagraf.substring(currentParagraf.indexOf("-") + 1));
 									}
 									catch (Exception e2){
 										pdo = pod;
 									}
 									String zakon = uriBuilder.toString();
 									for (int j = pod; j <= pdo; j++)
 									{
 						                printIfNotNullOrEmpty("\tovm-a:pravniZaklad <", zakon.toString() + "/section/" + j, "> ;");
 									}
 								}
 								else if (currentParagraf.contains(","))
 								{
 									String[] ps = currentParagraf.split(",");
 									String zakon = uriBuilder.toString();
 									for (String p : ps)
 									{
 						                printIfNotNullOrEmpty("\tovm-a:pravniZaklad <", zakon.toString() + "/section/" + p, "> ;");
 									}
 								}
 								else
 								{
 									uriBuilder.append("/section/" + currentParagraf);
 									
 									if (currentOdstavec != null && !currentOdstavec.isEmpty())
 									{
 										
 										if (currentOdstavec.contains("-"))
 										{
 											int pod = Integer.parseInt(currentOdstavec.substring(0, currentOdstavec.indexOf("-")));
 											int pdo;
 											try {
 												pdo = Integer.parseInt(currentOdstavec.substring(currentOdstavec.indexOf("-") + 1));
 											}
 											catch (Exception e2){
 												pdo = pod;
 											}
 											String zakon = uriBuilder.toString();
 											for (int j = pod; j <= pdo; j++)
 											{
 								                printIfNotNullOrEmpty("\tovm-a:pravniZaklad <", zakon.toString() + "/" + j, "> ;");
 											}
 										}
 										else if (currentOdstavec.contains(","))
 										{
 											String[] ps = currentOdstavec.split(",");
 											String zakon = uriBuilder.toString();
 											for (String p : ps)
 											{
 								                printIfNotNullOrEmpty("\tovm-a:pravniZaklad <", zakon.toString() + "/" + p, "> ;");
 											}
 										}
 										else
 										{
 											uriBuilder.append("/" + currentOdstavec);
 											if (currentPismeno != null && !currentPismeno.isEmpty())
 											{
 												if (currentPismeno.contains("-"))
 												{
 													char pod = currentPismeno.charAt(0);
 													char pdo = currentPismeno.charAt(2);
 													String zakon = uriBuilder.toString();
 													for (char j = pod; j <= pdo; j++)
 													{
 										                printIfNotNullOrEmpty("\tovm-a:pravniZaklad <", zakon.toString() + "/" + j, "> ;");
 													}
 												}
 												else if (currentPismeno.contains(","))
 												{
 													String[] ps = currentPismeno.split(",");
 													String zakon = uriBuilder.toString();
 													for (String p : ps)
 													{
 										                printIfNotNullOrEmpty("\tovm-a:pravniZaklad <", zakon.toString() + "/" + p, "> ;");
 													}
 												}
 												else
 												{
 													uriBuilder.append("/" + currentPismeno);
 													URL zakon = new URL(uriBuilder.toString());
 									                printIfNotNullOrEmpty("\tovm-a:pravniZaklad <", zakon.toString(), "> ;");
 												}
 											}
 											else 
 											{
 												URL zakon = new URL(uriBuilder.toString());
 								                printIfNotNullOrEmpty("\tovm-a:pravniZaklad <", zakon.toString(), "> ;");
 											}
 										}
 									}
 								}
 							}
 							else
 							{
 								URL zakon = new URL(uriBuilder.toString());
 				                printIfNotNullOrEmpty("\tovm-a:pravniZaklad <", zakon.toString(), "> ;");
 							}
 						}
 					} catch (MalformedURLException e1) {
 						logger.error(e1.getLocalizedMessage());
 					}
                 	
                 }
                 
                 Element tableAother = tables.get(1);
                 //Parse table Aother
                 
                 for (Element e: tableAother.select("tr:has(td:eq(1))"))
                 {
                 	String currentCislo = e.select("td:eq(0)").first().text();
                 	String currentRok = e.select("td:eq(1)").first().text();
                 	String currentNazev = e.select("td:eq(2)").first().text(); 
 
                 	try {
 						StringBuilder uriBuilder = new StringBuilder("http://linked.opendata.cz/resource/legislation/cz/act");
 						if (currentRok != null && currentCislo != null && !currentRok.isEmpty() && ! currentCislo.isEmpty() && !currentCislo.equals("null")) {
 							uriBuilder.append("/" + currentRok + "/" + currentCislo + "-" + currentRok);
 							
 							URL zakon = new URL(uriBuilder.toString());
 			                printIfNotNullOrEmpty("\tovm-a:ostatniPredpis <", zakon.toString(), "> ;");
 						}
 						else
 						{
 			                printIfNotNullOrEmpty("\tovm-a:ostatniPredpisText \"\"\"", currentNazev, "\"\"\" ;");
 						}
 					} catch (MalformedURLException e1) {
 						logger.error(e1.getLocalizedMessage());
 					}
                 	
                 }
                 
                 ps.println("\t.\n");
                 
                //ohlašovatel činnosti gr:BE
                String currentBEURI = "http://linked.opendata.cz/resource/business-entity/CZ" + identifikaceOVM;
            	
                ps.println("<" + currentBEURI + "> a gr:BusinessEntity ;");
                printIfNotNullOrEmpty("\tgr:legalName \"", nazevOVM, "\" ;");
                printIfNotNullOrEmpty("\tdcterms:title \"", nazevOVM, "\" ;");
                
            	String beidUri = currentBEURI + "/identifier/" + identifikaceOVM;
                printIfNotNullOrEmpty("\tadms:identifier <", beidUri ,"> ;");
                ps.println("\t.\n");

                printIfNotNullOrEmpty("<",beidUri,"> a adms:Identifier ;");
            	printIfNotNullOrEmpty("\tskos:notation \"",identifikaceOVM,"\" ;");
            	printIfNotNullOrEmpty("\tskos:prefLabel \"",identifikaceOVM,"\" ;");
            	ps.println("\tskos:inScheme <http://linked.opendata.cz/resource/concept-scheme/CZ-ICO> ;");
                ps.println("\t.\n");
                //konec ohlašovatele agendy
                
                 //Parse table B
                 //CssSelector tableB = new CssSelector(doc, "body table:has(caption:contains(B) Výčet činností vykonávaných v agendě:)");
                 Element tableB = tables.get(2);
                 
                 for (Element e: tableB.select("tr:has(td:eq(1))"))
                 {
                 	String currentKod = e.select("td:eq(0)").first().text();
                 	String currentNazev = turtleEscape(e.select("td:eq(1)").first().text()); 
                 	String currentPopis = turtleEscape(e.select("td:eq(2)").first().text()); 
                 	
                 	String currentCURIE = "ovm-c:" + currentKod;
                 	
                     ps.println("<" + URLagendy + "> ovm-a:cinnost " + currentCURIE + " .\n");
                 	
                     ps.println(currentCURIE + " a ovm-co:Cinnost, skos:Concept ;");
                 	ps.println("\tskos:inScheme ovm-c:CinnostiConceptScheme ;");
                 	printIfNotNullOrEmpty("\tskos:notation \"",currentKod,"\" ;");
                 	printIfNotNullOrEmpty("\tskos:prefLabel \"\"\"",currentNazev,"\"\"\" ;");
                 	printIfNotNullOrEmpty("\tskos:note \"\"\"",currentPopis,"\"\"\" ;");
                     ps.println("\t.\n");
 
                 }
                                 
                 Element tableC = tables.get(3);
 
                 for (Element e: tableC.select("tr:has(td:eq(0))"))
                 {
                 	String currentKod = e.select("td:eq(0)").first().text();
                 	
                 	if (currentKod.equals("(žádná data)")) break;
                 	
                 	String currentCURIE = "ovm-t:" + uriSlug(currentKod);
                 	
                     ps.println("<" + URLagendy + "> ovm-a:typOVM " + currentCURIE + " .\n");
                 	
                     ps.println(currentCURIE + " a ovm-a:TypOVM, skos:Concept ;");
                 	ps.println("\tskos:inScheme ovm-a:TypOVMConceptScheme ;");
                 	printIfNotNullOrEmpty("\tskos:prefLabel \"",currentKod,"\" ;");
                     ps.println("\t.\n");
 
                 }
 
                 Element tableOVMs = tables.get(4);
                 for (Element e: tableOVMs.select("tr:has(td:eq(1))"))
                 {
                 	String currentKod = e.select("td:eq(0)").first().text();
                 	String currentNazev = e.select("td:eq(1)").first().text();
                 	
                     String currentURI = "http://linked.opendata.cz/resource/business-entity/CZ" + currentKod;
                 	ps.println("<" + URLagendy + "> ovm-a:vykonava <" + currentURI + "> .\n");
                 	
                     ps.println("<" + currentURI + "> a gr:BusinessEntity ;");
                     printIfNotNullOrEmpty("\tgr:legalName \"", currentNazev, "\" ;");
                     printIfNotNullOrEmpty("\tdcterms:title \"", currentNazev, "\" ;");
                     
                 	String idUri = currentURI + "/identifier/" + currentKod;
                     if (idUri != null) {
                         printIfNotNullOrEmpty("\tadms:identifier <", idUri ,"> ;");
                         ps.println("\t.\n");
 
                         printIfNotNullOrEmpty("<",idUri,"> a adms:Identifier ;");
 	                	printIfNotNullOrEmpty("\tskos:notation \"",currentKod,"\" ;");
 	                	printIfNotNullOrEmpty("\tskos:prefLabel \"",currentKod,"\" ;");
 	                	ps.println("\tskos:inScheme <http://linked.opendata.cz/resource/concept-scheme/CZ-ICO> ;");
 	                    ps.println("\t.\n");
                     }
                     else {
                         ps.println("\t.\n");
                     }
 
                 }
                 
                 
         }
     }
 }
