 package cz.opendata.linked.ares.updates;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.LinkedList;
 
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 
 import cz.cuni.xrg.intlib.commons.dpu.DPUContext;
 import cz.mff.cuni.scraper.lib.template.ParseEntry;
 import cz.mff.cuni.scraper.lib.template.ScrapingTemplate;
 
 /**
  * Specificky scraper pro statni spravu.
  * 
  * @author Jakub Starka
  */
 
 public class Scraper_parser extends ScrapingTemplate{
     
 	private static String icoBEprefix = "http://linked.opendata.cz/resource/business-entity/CZ";
 	public Logger logger ;
 	public PrintStream ps;
 	public DPUContext ctx;
     
     @Override
     protected LinkedList<ParseEntry> getLinks(org.jsoup.nodes.Document doc, String docType) {
         LinkedList<ParseEntry> out = new LinkedList<>();
         if (docType.equals("init"))
         {
         	Elements elemsod = doc.select("are|ares_odpovedi are|odpoved d|zadani d|c_davky_od");
         	Elements elemsdo = doc.select("are|ares_odpovedi are|odpoved d|zadani d|c_davky_do");
         	int davod = Integer.parseInt(elemsod.text());
         	int davdo = Integer.parseInt(elemsdo.text());
         	try {
     			Path path = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/wwwinfo.mfcr.cz/cgi-bin/ares/darv_zm.cgi@cislo_zdroje=2&cislo_davky_od=" + davod + "&cislo_davky_do=" + davdo);
     			logger.info("Deleting " + path);
     			try {
 					Files.deleteIfExists(path);
 				} catch (IOException e) {
 					logger.info("Unable to delete IC list");
 				}
 				out.add(new ParseEntry(new URL("http://wwwinfo.mfcr.cz/cgi-bin/ares/darv_zm.cgi?cislo_zdroje=2&cislo_davky_od=" + davod + "&cislo_davky_do=" + davdo), "list",  "xml"));
 			} catch (MalformedURLException e) {
 				logger.error("Unexpected Malformed URI");
 				e.printStackTrace();
 			}
         }
         return out;
     }
     
     @Override
     protected void parse(org.jsoup.nodes.Document doc, String docType, URL url) {
     	if (docType.equals("list"))
     	{
     		Elements elems = doc.select("are|ares_odpovedi are|odpoved d|s d|ic");
     		int size = elems.size();
     		logger.info("Found " + size + " ICs");
     		int current = 0;
     		for (Element e: elems)
     		{
     			current++;
     			logger.debug("Processing " + current + "/" + size);
     			String currentIC = e.text();
     			ps.println("czbe:CZ" + currentIC + " a gr:BusinessEntity ;");
     			ps.println("\tadms:identifier <" + icoBEprefix + currentIC + "/identifier/" + currentIC + "> ;");
 	        	ps.println("\t.");
 	        	ps.println();
 
     			ps.println("<" + icoBEprefix + currentIC + "/identifier/" + currentIC + "> a adms:Identifier ;");
     			ps.println("\tskos:notation \"" + currentIC + "\" ;");
     			ps.println("\tskos:inScheme <http://linked.opendata.cz/resource/concept-scheme/CZ-ICO> ;");
 	        	ps.println("\tadms:schemeAgency \"Český statistický úřad\" ;");
 	        	ps.println("\t.");
 	        	ps.println();
     		}
     	}
     }
 }
