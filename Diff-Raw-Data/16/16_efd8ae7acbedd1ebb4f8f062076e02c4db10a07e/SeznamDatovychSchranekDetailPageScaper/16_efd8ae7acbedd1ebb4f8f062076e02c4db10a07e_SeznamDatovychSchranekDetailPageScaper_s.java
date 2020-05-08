 package cz.rhok.prague.osf.governmentcontacts.scraper;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import models.Address;
 import models.Organization;
 
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.google.common.collect.Maps;
 
 //TODO : michal pridat plneni dat pro e-podatelnu (email v v zalozce zakladni info neni vzdy vyplnen)
 public class SeznamDatovychSchranekDetailPageScaper {
 
 	private static final Pattern MAIL_REGEX_PATTERN = Pattern.compile("\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b", Pattern.CASE_INSENSITIVE);
 	private static Logger log = play.Logger.log4j; //org.apache.log4j.Logger.getLogger("another.logger");
 	
 	/**
 	 * @param pageUrl page which contains information to be scraped 
 	 * 		  eg.http://seznam.gov.cz/ovm/regionDetail.do?path=KPRAHA&ref=obcan
 	 * @return 
 	 */
 	public Organization scrape(String pageUrl) {
 		
 		log.debug("Start scraping of: " + pageUrl);
 		
 		Long startTime = System.currentTimeMillis();
 		
 		Document doc = null;
 		try {
 			doc = Jsoup.connect(pageUrl).get();
 		} catch (IOException ex) {
 			log.error("Unable to parse: " + pageUrl, ex);
 			throw new RuntimeException("Unable to parse: " + pageUrl, ex);
 		}
 
 		Map<String, String> scrappedData = Maps.newHashMap();
 
 		Elements dataRows = doc.select("#colData.institution .data tr");
 
 		for (Element dataRow : dataRows) {
 
 			String label = dataRow.select("th").text();
 
 			// remove trailing ":" from label so 
 			// instead of "Identifikátor datové schránky:" its just
 			// "Identifikátor datové schránky"
 			label = label.replace(":", "");
 			
 			String value;
 			if ("Adresa sídla".equals(label)) {
 				// replace <br/> tags with newline
 				value = dataRow.select("td").html();
 				value = value.replace("<br />", "\n");
 				value = value.replace("<br/>", "\n");
 			} else {
 				value = dataRow.select("td").text();
 			}
 			
 			scrappedData.put(label, value);
 
 		}
 		
 		Long endTime = System.currentTimeMillis();
 
 		Organization organization = new Organization();
 		
 		organization.dataBoxId = scrappedData.get("Identifikátor datové schránky");
 		organization.name = scrappedData.get("Název");
 		organization.code = scrappedData.get("Kód organizace");
 		organization.taxId = scrappedData.get("DIČ");
 		organization.organizationId = scrappedData.get("IČ");
 		organization.email = parseEmail(scrappedData);
 		organization.phone = scrappedData.get("Telefon");
 		organization.bankAccount = scrappedData.get("Bankovní spojení");
 		organization.type = scrappedData.get("Typ instituce");
 		organization.officeHours = scrappedData.get("Úřední hodiny");
 		organization.www = scrappedData.get("WWW");
 		
 		Address address = parseAddress(scrappedData);
 		organization.addressStreet = address.street;
 		organization.addressCity = address.city;
 		organization.addressZipCode = address.zipCode;
 		
 		long timeElapsed = endTime - startTime;
 		log.debug("Scraping of " + pageUrl + " succesfully done in " + timeElapsed + " ms");
 		
 		return organization;
 		
 	}
 
 	private Address parseAddress(Map<String, String> scrappedData) {
 		String addressText = scrappedData.get("Adresa sídla");
 		
 		Address address = new Address();
 		String[] lines = addressText.split("\n");
 		
 		if (lines.length == 3) {
 			address.street = lines[0].trim();
 			address.city = lines[1].trim();
 			address.zipCode = lines[2].trim();
 		} else {
 			address.street = addressText;
 			log.warn("Unknown address format: " + addressText);
 		}
 		
 		return address;
 	}
 	
 	private String parseEmail(Map<String, String> scrappedData) {
 		String rawEmailData = scrappedData.get("E-mail"); // eg. posta@cityofprague.cz (podatelna)
 
 		// extract only mail part from string if there are something else
 		String email = "";
		
		Matcher matcher = MAIL_REGEX_PATTERN.matcher(rawEmailData);
		if (matcher.find()) {
			email = matcher.group(0);
		} else {			
			log.warn("Unable to parse e-mail. Parsed text : " + rawEmailData);
			//TODO: michal : log message doesn't contain context information (which input data causes this)
 		}
 						
 		return email;
 	}
 
 }
