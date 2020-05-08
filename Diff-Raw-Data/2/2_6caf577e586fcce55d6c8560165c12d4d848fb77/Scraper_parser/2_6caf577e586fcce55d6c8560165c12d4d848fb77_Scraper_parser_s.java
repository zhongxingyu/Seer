 package cz.opendata.linked.buyer_profiles;
 import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
 import cz.mff.cuni.scraper.lib.template.ParseEntry;
 import cz.mff.cuni.scraper.lib.template.ScrapingTemplate;
 
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.Normalizer;
 import java.text.Normalizer.Form;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.UUID;
 
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 
 /**
  * Specificky scraper pro statni spravu.
  * 
  * @author Jakub Starka
  */
 
 public class Scraper_parser extends ScrapingTemplate{
     
     public PrintStream ps;
     public PrintStream zak_ps;
     public boolean AccessProfiles;
     public boolean CurrentYearOnly;
     
     public int numrows = 0;
     public int cancellednumrows = 0;
     public int numerrors = 0;
     public int numwarnings = 0;
     public int numprofiles = 0;
     public int missingIco = 0;
     public int missingIcoInProfile = 0;
     public int numzakazky = 0;
     public int numuchazeci = 0;
     public int numdodavatele = 0;
     public int numlists = 0;
     public int numdetails = 0;
     public int numCancelledDetails = 0;
     public int totalnumrows = 0;
     public int invalidXML = 0;
     public int multiDodavatel = 0;
     public int numsub = 0;
     public int totalcancellednumrows = 0;
     
     private static String guidBEprefix = "http://linked.opendata.cz/resource/domain/buyer-profiles/business-entity/cz/";
     private static String icoBEprefix = "http://linked.opendata.cz/resource/business-entity/CZ";
     
     @Override
     protected LinkedList<ParseEntry> getLinks(org.jsoup.nodes.Document doc, String docType) {
         LinkedList<ParseEntry> out = new LinkedList<>();
         if (docType.equals("list") || docType.equals("first") || docType.equals("firstCancelled") || docType.equals("cancelledList")) {
             /* Na strance se sezname si najdu linky na detaily */
             if (doc == null) return out;
         	Elements e = doc.select("div#SearchGrid div.t-grid-pager.t-grid-bottom div.t-pager.t-reset div~a.t-link ");
             Elements rows = doc.select("table tr");
             if (rows == null) return out;
             if (e.size() == 0)
             {
             	logger.warn("0 elements?? Clear cache and try again");
             	return out;
             	
             }
             try {
             	if (!("#".equals(e.get(0).attr("href"))))
             	{
 	            	URL u = new URL("http://www.vestnikverejnychzakazek.cz" + e.get(0).attr("href"));
 	            	if (docType.equals("list") || docType.equals("first")) out.add(new ParseEntry(u, "list", "html"));
                 	else if (docType.equals("firstCancelled") || docType.equals("cancelledList")) out.add(new ParseEntry(u, "cancelledList", "html"));
             	}
                 
             	for(int i = 1; i < rows.size(); i++)
                 {
                 	Element row = rows.get(i);
                 	Elements rowElements = row.getAllElements();
                 	String contractId = rowElements.get(1).text();
 	            	URL d = new URL("http://www.vestnikverejnychzakazek.cz/Views/Form/Display/" + contractId);
                 	
 	            	if (docType.equals("list") || docType.equals("first")) out.add(new ParseEntry(d, "detail", "html"));
                 	else if (docType.equals("firstCancelled") || docType.equals("cancelledList")) out.add(new ParseEntry(d, "cancelledDetail", "html"));
                 	
 	            	URL profilZadavatele = parseURL(rowElements.get(5).text(), rowElements.get(4).text(), rowElements.get(3).text(), "Profil zadavatele (link)");
                 	if (AccessProfiles && (docType.equals("list") || docType.equals("first")) && profilZadavatele != null)
                 	{
 	                	if (profilZadavatele.toString().endsWith("/"))
 	                	{
 	                		profilZadavatele = new URL(profilZadavatele.toString() + "XMLdataVZ");
 	                	}
 	                	else 
 	                	{
 	                		profilZadavatele = new URL(profilZadavatele.toString() + "/XMLdataVZ");
 	                	}
 	                	
 	                	if (CurrentYearOnly)
 	                	{
 	                		int i1 = Calendar.getInstance().get(Calendar.YEAR);
 	                		out.add(new ParseEntry(new URL(profilZadavatele + "?od=0101" + i1 + "&do=3112" + i1), "profil", "xml"));
 	                	}
 	                	else for (int i1 = 2010; i1 <= Calendar.getInstance().get(Calendar.YEAR); i1++)
 	                	{
 	                		
 		                	out.add(new ParseEntry(new URL(profilZadavatele + "?od=0101" + i1 + "&do=3112" + i1), "profil", "xml"));
 	                	}
                 	}
             	}
             } catch (MalformedURLException ex) {
                 logger.error(ex.getLocalizedMessage());
             }
         }
         else if (docType.equals("detail") || docType.equals("cancelledDetail"))
         {
         	
         }
         return out;
     }
     
     private String getStatString(String kod)
     {
     	switch (kod)
     	{
     	case "AD": return "Andorrské knížectví";
     	case "AE": return "Spojené arabské emiráty";
     	case "AF": return "Afghánistán";
     	case "AG": return "Antigua a Barbuda";
     	case "AI": return "Anguilla";
     	case "AL": return "Albánská republika";
     	case "AM": return "Arménská republika";
     	case "AN": return "Nizozemské Antily";
     	case "AO": return "Angolská republika";
     	case "AQ": return "Antarktida";
     	case "AR": return "Argentinská republika";
     	case "AS": return "Americká Samoa";
     	case "AT": return "Rakouská republika";
     	case "AU": return "Austrálie";
     	case "AW": return "Aruba";
     	case "AZ": return "Ázerbájdžánská republika";
     	case "BA": return "Bosna a Hercegovina";
     	case "BB": return "Barbados";
     	case "BD": return "Bangladéšská lidová republika";
     	case "BE": return "Belgické království";
     	case "BF": return "Burkina Faso";
     	case "BG": return "Bulharská republika";
     	case "BH": return "Bahrajnské království";
     	case "BI": return "Burundská republika";
     	case "BJ": return "Beninská republika";
     	case "BM": return "Bermudy";
     	case "BN": return "Brunej Darussalam";
     	case "BO": return "Bolivijská republika";
     	case "BR": return "Brazilská federativní republika";
     	case "BS": return "Bahamské spoleČenství";
     	case "BT": return "Bhútánské království";
     	case "BV": return "Bouvetův ostrov";
     	case "BW": return "Botswanská republika";
     	case "BY": return "Běloruská republika";
     	case "BZ": return "Belize";
     	case "CA": return "Kanada";
     	case "CC": return "Kokosové ostrovy";
     	case "CD": return "Konžská demokratická republika ";
     	case "CF": return "Středoafrická republika";
     	case "CG": return "Konžská republika";
     	case "CI": return "Republika Pobřeží slonoviny";
     	case "CK": return "Cookovy ostrovy";
     	case "CL": return "Chilská republika";
     	case "CM": return "Kamerunská republika";
     	case "CN": return "Čínská lidová republika";
     	case "CO": return "Kolumbijská republika";
     	case "CR": return "Kostarická republika";
     	case "CU": return "Kubánská republika";
     	case "CV": return "Kapverdská republika";
     	case "CX": return "VánoČní ostrov";
     	case "CY": return "Kyperská republika";
     	case "CZ": return "Česká republika";
     	case "DE": return "Spolková republika Německo";
     	case "DJ": return "Džibutská republika";
     	case "DK": return "Dánské království";
     	case "DM": return "Dominické společenství";
     	case "DO": return "Dominikánská republika";
     	case "DZ": return "Alžírská lidová demokratická republika";
     	case "EC": return "Ekvádorská republika";
     	case "EE": return "Estonská republika";
     	case "EG": return "Egyptská arabská republika";
     	case "EH": return "Západní Sahara";
     	case "ER": return "Eritrea";
     	case "ES": return "Španělské království";
     	case "ET": return "Etiopská federativní demokratická republika";
     	case "FI": return "Finská republika";
     	case "FJ": return "Republika Fidžijské ostrovy ";
     	case "FK": return "Falklandy (Malvíny)";
     	case "FM": return "Federativní státy Mikronésie";
     	case "FO": return "Faerské ostrovy";
     	case "FR": return "Francouzská republika";
     	case "GA": return "Gabonská republika";
     	case "GB": return "Spojené království Velké Británie a Severního Irska";
     	case "GD": return "Grenada";
     	case "GE": return "Gruzie";
     	case "GF": return "Francouzská Guyana";
     	case "GH": return "Ghanská republika";
     	case "GI": return "Gibraltar";
     	case "GL": return "Grónsko";
     	case "GM": return "Gambijská republika";
     	case "GN": return "Guinejská republika";
     	case "GP": return "Guadeloupe";
     	case "GQ": return "Republika Rovníková Guinea";
     	case "GR": return "řecká republika";
     	case "GS": return "Jižní Georgie a Jižní Sandwichovy ostrovy";
     	case "GT": return "Guatemalská republika";
     	case "GU": return "Guam";
     	case "GW": return "Republika Guinea-Bissau";
     	case "GY": return "Guyanská republika";
     	case "HK": return "Hongkong, zvláštní administrativní oblast Čínské lidové republiky ";
     	case "HM": return "Heardův ostrov a McDonaldovy ostrovy";
     	case "HN": return "Honduraská republika";
     	case "HR": return "Chorvatská republika";
     	case "HT": return "Haitská republika";
     	case "HU": return "Maďarská republika";
     	case "CH": return "Švýcarská konfederace";
     	case "ID": return "Indonéská republika";
     	case "IE": return "Irsko";
     	case "IL": return "Izraelský stát";
     	case "IN": return "Indická republika";
     	case "IO": return "Britské indickooceánské území";
     	case "IQ": return "Irácká republika";
     	case "IR": return "Íránská islámská republika";
     	case "IS": return "Islandská republika";
     	case "IT": return "Italská republika";
     	case "JM": return "Jamajka";
     	case "JO": return "Jordánské hášimovské království";
     	case "JP": return "Japonsko";
     	case "KE": return "Keòská republika";
     	case "KG": return "Republika Kyrgyzstán";
     	case "KH": return "Kambodžské království";
     	case "KI": return "Republika Kiribati";
     	case "KM": return "Komorský svaz";
     	case "KN": return "Svatý Kryštof a Nevis";
     	case "KP": return "Korejská lidově demokratická republika";
     	case "KR": return "Korejská republika";
     	case "KW": return "Kuvajtský stát";
     	case "KY": return "Kajmanské ostrovy";
     	case "KZ": return "Republika Kazachstán";
     	case "LA": return "Laoská lidově demokratická republika";
     	case "LB": return "Libanonská republika";
     	case "LC": return "Svatá Lucie";
     	case "LI": return "Lichtenštejnské knížectví";
     	case "LK": return "Srílanská demokratická socialistická republika";
     	case "LR": return "Liberijská republika";
     	case "LS": return "Lesothské království";
     	case "LT": return "Litevská republika";
     	case "LU": return "Lucemburské velkovévodství";
     	case "LV": return "Lotyšská republika";
     	case "LY": return "Libyjská arabská lidová socialistická džamáhírije";
     	case "MA": return "Marocké království";
     	case "MC": return "Monacké knížectví";
     	case "MD": return "Moldavská republika";
     	case "MG": return "Madagaskarská republika";
     	case "MH": return "Republika Marshallovy ostrovy";
     	case "MK": return "Bývalá jugoslávská republika Makedonie";
     	case "ML": return "Maliská republika";
     	case "MM": return "Myanmarský svaz";
     	case "MN": return "Mongolsko";
     	case "MO": return "Macao, zvláštní administrativní oblast Čínské lidové republiky ";
     	case "MP": return "SpoleČenství Severních Marian";
     	case "MQ": return "Martinik";
     	case "MR": return "Mauritánská islámská republika";
     	case "MS": return "Montserrat";
     	case "MT": return "Maltská republika";
     	case "MU": return "Mauricijská republika";
     	case "MV": return "Maledivská republika";
     	case "MW": return "Malawská republika";
     	case "MX": return "Spojené státy mexické";
     	case "MY": return "Malajsie";
     	case "MZ": return "Mosambická republika";
     	case "NA": return "Namibijská republika";
     	case "NC": return "Nová Kaledonie";
     	case "NE": return "Nigerská republika";
     	case "NF": return "Norfolk";
     	case "NG": return "Nigérijská federativní republika";
     	case "NI": return "Nikaragujská republika";
     	case "NL": return "Nizozemské království";
     	case "NO": return "Norské království";
     	case "NP": return "Nepálské království";
     	case "NR": return "Nauruská republika";
     	case "NU": return "Niue";
     	case "NZ": return "Nový Zéland";
     	case "OM": return "Sultanát Omán";
     	case "PA": return "Panamská republika";
     	case "PE": return "Peruánská republika";
     	case "PF": return "Francouzská Polynésie";
     	case "PG": return "Papua Nová Guinea";
     	case "PH": return "Filipínská republika";
     	case "PK": return "Pákistánská islámská republika";
     	case "PL": return "Polská republika";
     	case "PM": return "Saint Pierre a Miquelon";
     	case "PN": return "Pitcairn";
     	case "PR": return "Portoriko";
     	case "PS": return "Okupovaná palestinská území";
     	case "PT": return "Portugalská republika";
     	case "PW": return "Palauská republika";
     	case "PY": return "Paraguayská republika";
     	case "QA": return "Stát Katar";
     	case "RE": return "Réunion";
     	case "RO": return "Rumunsko";
     	case "RU": return "Ruská federace";
     	case "RW": return "Rwandská republika";
     	case "SA": return "Saúdskoarabské království";
     	case "SB": return "Šalamounovy ostrovy";
     	case "SC": return "Seychelská republika";
     	case "SD": return "Súdánská republika";
     	case "SE": return "Švédské království";
     	case "SG": return "Singapurská republika";
     	case "SH": return "Svatá Helena";
     	case "SI": return "Slovinská republika";
     	case "SJ": return "Svalbard a ostrov Jan Mayen";
     	case "SK": return "Slovenská republika";
     	case "SL": return "Republika Sierra Leone";
     	case "SM": return "Sanmarinská republika";
     	case "SN": return "Senegalská republika";
     	case "SO": return "Somálská republika";
     	case "SR": return "Surinamská republika";
     	case "ST": return "Demokratická republika Svatý Tomáš a Princův ostrov";
     	case "SV": return "Salvadorská republika";
     	case "SY": return "Syrská arabská republika";
     	case "SZ": return "Svazijské království";
     	case "TC": return "Turks a Caicos";
     	case "TD": return "Čadská republika";
     	case "TF": return "Francouzská jižní území";
     	case "TG": return "Tožská republika";
     	case "TH": return "Thajské království";
     	case "TJ": return "Republika Tádžikistán";
     	case "TK": return "Tokelau";
     	case "TM": return "Turkmenistán";
     	case "TN": return "Tuniská republika";
     	case "TO": return "Království Tonga";
     	case "TR": return "Turecká republika";
     	case "TT": return "Republika Trinidad a Tobago";
     	case "TV": return "Tuvalu";
     	case "TW": return "Tchaj-wan, Čínská provincie";
     	case "TZ": return "Sjednocená republika Tanzanie";
     	case "UA": return "Ukrajina";
     	case "UG": return "Ugandská republika";
     	case "UM": return "Menší odlehlé ostrovy USA";
     	case "US": return "Spojené státy americké";
     	case "UY": return "Uruguayská východní republika";
     	case "UZ": return "Republika Uzbekistán";
     	case "VA": return "Svatý stolec (Vatikánský městský stát)";
     	case "VC": return "Svatý Vincenc a Grenadiny";
     	case "VE": return "Bolívarovská republika Venezuela";
     	case "VG": return "Britské Panenské ostrovy";
     	case "VI": return "Americké Panenské ostrovy";
     	case "VN": return "Vietnamská socialistická republika";
     	case "VU": return "Vanuatská republika";
     	case "WF": return "Wallis a Futuna";
     	case "WS": return "Nezávislý stát Samoa";
     	case "YE": return "Jemenská republika";
     	case "YT": return "Mayotte";
     	case "ZA": return "Jihoafrická republika";
     	case "ZM": return "Zambijská republika";
     	case "ZW": return "Zimbabwská republika";
     	default: 
     		logger.warn("Varování: neznámý kód státu: " + kod + ". Vracím ČR");
     		return "Česká republika";
     	}
     }
     
     private String escapeString(String original)
     {
     	return original.replace("\n", " ").replace("\r", " ").replace("<","").replace(">","").replace("\\","\\\\").replace("\"", "\\\"").replace("„", "\\\"").replace("“", "\\\"");
     }
     
     private URL parseURL(String rawURL, String ico, String nazevZadavatele, String typURL)
     {
     	URL cleanURL = null;
     	try {
 			if (rawURL.isEmpty())
 			{
 				logger.debug("Varování (" + typURL + "): Prázdné URL: Zadavatel: " + ico + " " + nazevZadavatele);
 				return null;
 			}
 			if (rawURL.contains("\\"))
 			{
 				logger.debug("Varování (" + typURL + "): Zpětné lomítko: " + rawURL + " Zadavatel: " + ico + " " + nazevZadavatele + " Oprava: " + rawURL.replace('\\', '/'));
 				rawURL = rawURL.replace('\\', '/');
 				numwarnings++;
 			}
 			if (rawURL.contains(" "))
 			{
 				logger.debug("Chyba (" + typURL + "): Mezera v URL: " + rawURL + " Zadavatel: " + ico);
 				numerrors++;
 			}
 			else if (rawURL.contains("\""))
 			{
 				logger.debug("Chyba (" + typURL + "): Uvozovka v URL: " + rawURL + " Zadavatel: " + ico);
 				numerrors++;
 			}
 			else if (rawURL.contains("\n"))
 			{
 				logger.debug("Chyba (" + typURL + "): Nový řádek v URL: " + rawURL + " Zadavatel: " + ico);
 				numerrors++;
 			}
 			else if (!rawURL.contains(".")) 
 			{
 				logger.debug("Chyba (" + typURL + "): Pravděpodobně není URL (Nenalezena tečka): " + rawURL + ico + " " + nazevZadavatele + ".");
 				numerrors++;
 			}
 			else if (rawURL.toLowerCase().startsWith("http//:")) 
 			{
 				cleanURL = new URL("http://" + rawURL.substring(7));
 				logger.debug("Varování (" + typURL + "): \"http//:\": " + rawURL + " Zadavatel: " + ico + " " + nazevZadavatele + ". Oprava: " + cleanURL);
 				numwarnings++;
 			}
 			else if (rawURL.toLowerCase().startsWith("www:")) 
 			{
 				cleanURL = new URL("http://www." + rawURL.substring(5));
 				logger.debug("Varování (" + typURL + "): \"www:\": " + rawURL + " Zadavatel: " + ico + " " + nazevZadavatele + ". Oprava: " + cleanURL);
 				numwarnings++;
 			}
 			else if (rawURL.toLowerCase().startsWith("http://") || rawURL.toLowerCase().startsWith("https://"))
 			{
 				cleanURL = new URL(rawURL);
 			}
 			else if (rawURL.toLowerCase().startsWith("http:/"))
 			{
 				cleanURL = new URL("http://" + rawURL.substring(6));
 				logger.debug("Varování (" + typURL + "): Chybí lomítko v URL: " + rawURL + " Zadavatel: " + ico + " " + nazevZadavatele + ". Oprava: " + cleanURL);
 				numwarnings++;
 			}
 			else if (rawURL.toLowerCase().startsWith("https")) 
 			{
 				String tempURL =  rawURL.substring(5);
 				tempURL = tempURL.replaceAll("[^a-zA-Z0-9]*(.*)","$1");
 				cleanURL = new URL("https://" + tempURL);
 				logger.debug("Varování (" + typURL + "): Chybí dvojtečka v URL: " + rawURL + " Zadavatel: " + ico + " " + nazevZadavatele + ". Oprava: " + cleanURL);
 				numwarnings++;
 			}
 			else if (rawURL.toLowerCase().startsWith("http")) 
 			{
 				String tempURL =  rawURL.substring(4);
 				tempURL = tempURL.replaceAll("[^a-zA-Z0-9]*(.*)","$1");
 				cleanURL = new URL("http://" + tempURL);
 				logger.debug("Varování (" + typURL + "): Chybí dvojtečka v URL: " + rawURL + " Zadavatel: " + ico + " " + nazevZadavatele + ". Oprava: " + cleanURL);
 				numwarnings++;
 			}
 			else 
 			{
 				cleanURL = new URL("http://" + rawURL);
 				logger.debug("Varování: Missing protocol in URL: " + rawURL + " Zadavatel: " + ico + " " + nazevZadavatele + ". Fixed: " + cleanURL);
 			}
 		} catch (MalformedURLException e) {
 			logger.warn("Chyba (" + typURL + "): Špatné URL: " + rawURL + " Zadavatel: " + ico + " " + nazevZadavatele + ".");
 			numerrors++;
 			e.printStackTrace();
 		}
     	
     	if (cleanURL != null && cleanURL.getHost().contains("vhodne"))
 		{
     		try 
     		{
     			cleanURL = new URL("https://www.vhodne-uverejneni.cz/profil/" + cleanURL.getPath().replaceAll("/pr[^/]+/(.*)", "$1"));
 			} catch (MalformedURLException e) {
 				logger.warn("Chyba (" + typURL + "): Špatně upravené URL: " + rawURL + " Zadavatel: " + ico + " " + nazevZadavatele + ".");
 				numerrors++;
 				e.printStackTrace();			
 			} 
 		}
     	
     	if (cleanURL != null && cleanURL.toString().endsWith("/"))
     	{
     		try {
 				cleanURL = new URL(cleanURL.toString().substring(0, cleanURL.toString().length() - 1));
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
     	}
 
         logger.debug("URL " + rawURL + " parsed as " + cleanURL);
     	return cleanURL;
     	
     }
     
 	private URL getInternalURLProfiluZadavatele(URL external)
 	{
 	    if (external == null) return null;
 		/* Transform profilZadavatele URL*/
 		try {
 			URL URIProfiluZadavatele = new URL("http://linked.opendata.cz/resource/domain/buyer-profiles/profile/cz/" 
			+ external.getProtocol() + "/" + external.getHost() + external.getPath());
 			return URIProfiluZadavatele;
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}
 
     
     private String cleanStatKod(String statKod)
     {
     	switch (statKod)
     	{
     	case "CR": 
     		logger.debug("Varování: Použit hack Kostarika -> ČR");
     		//return "Kostarická republika";
     		return "CZ";
     	case "Česká republiky":
     	case "čr":
     	case "ĆR":
     	case "CZE":
     	case "CZK":
     	case "Česká republika":
     	case "česká republika":
     	case "česko":
     	case "Česká Republika":
     	case "ČR":
     		logger.debug("Info: Přepis \"" + statKod + "\" na CZ");
     		return "CZ";
     	
     	case "Spojené království Velké Británie a Severního Irska":
     	case "United Kingdom":
     		logger.debug("Info: Přepis \"" + statKod + "\" na GB");
     		return "GB";
     	
     	case "Netherlands":
     		logger.debug("Info: Přepis \"" + statKod + "\" na NL");
     		return "NL";
     	    	
     	case "Rakousko":
     		logger.debug("Info: Přepis \"" + statKod + "\" na AT");
     		return "AT";
 
     	case "Polsko":
     		logger.debug("Info: Přepis \"" + statKod + "\" na PL");
     		return "PL";
     	
     	case "SVK": 
     		logger.debug("Info: Přepis \"" + statKod + "\" na SK");
     		return "SK";
     	    	
     	case "SRN": 
     		logger.debug("Info: Přepis \"" + statKod + "\" na DE");
     		return "DE";
 
     	case "USA": 
     		logger.debug("Info: Přepis \"" + statKod + "\" na US");
     		return "US";
     	
     	default: 
     		if (statKod.contains(" ")) return "CZ";
     		else return statKod;
     	}    	
     }
     
     private String getStat(String statKod)
     {
     	return "countries:" + cleanStatKod(statKod);
     }
     
     private String getDruh(String druh)
     {
     	switch (druh)
     	{
     	case "CR":
     		return "authkinds:CR";
     	case "PRISPEV_ORG": 
     		return "authkinds:Prispevkova";
     	case "UZEMNI_CELEK": 
     		return "authkinds:UzemniCelek";
     	case "AUTHORITY_JINA_PO": 
     		return "authkinds:JinaPravnickaOsosba";
     	case "AUTHORITY_EU_INST": 
     		return "authkinds:EvropskaInstituce";
     	case "OTHER": 
     		return null;
     	case "":
     		return null;
     	default: 
     		logger.warn("Doplnit: neznámý druh zadavatele: " + druh);
     		return null;
     	}
     }
 
     private String getStav(String stav)
     {
     	if (stav == null || stav.isEmpty()) return null;
     	switch (stav)
     	{
     	case "VZ neukončena":
     	case "Veřejná zakázka neukončena":
     		return "czstatus:Neukoncena";
 
     	case "VZ byla zadána": 
     	case "Veřejná zakázka byla zadána": 
     	case "ZadanaVZ": 
     		return "czstatus:Zadana";
 
     	case "VZ byla zrušena": 
     	case "ZrusenaVZ": 
     	case "Veřejná zakázka byla zrušena":
     		return "czstatus:Zrusena";
 
     	case "Ukončeno plnění smlouvy na základě VZ": 
     	case "Ukončeno plnění smlouvy na základě veřejné zakázky":
     		return "czstatus:Ukoncena";
 
     	case "Neznámý": 
     		return "czstatus:Neznamy";
 
     	default: 
     		logger.warn("Doplnit: neznámý stav zakázky: " + stav);
     		return "czstatus:Neznamy";
     	}
     }
 
     private String getSluzby(String sluzbaKod)
     {
     	if (sluzbaKod.startsWith("0")) sluzbaKod = sluzbaKod.substring(1);
     	
     	switch (sluzbaKod)
     	{
     	case "1":
     		return "kinds:MaintenanceAndRepairDASServices";
     	case "2":
     		return "kinds:LandTransportServices";
     	case "3":
     		return "kinds:AirTransportServices";
     	case "4":
     		return "kinds:TransportOfMailServices";
     	case "5":
     		return "kinds:TelecommunicationServices";
     	case "6":
     		return "kinds:FinancialServices";
     	case "7":
     		return "kinds:ComputerServices";
     	case "8":
     		return "kinds:ResearchAndDevelopmentServices";
     	case "9":
     		return "kinds:AccountingServices";
     	case "10":
     		return "kinds:MarketResearchServices";
     	case "11":
     		return "kinds:ConsultingServices";
     	case "12":
     		return "kinds:ArchitecturalServices";
     	case "13":
     		return "kinds:AdvertisingServices";
     	case "14":
     		return "kinds:BuildingCleaningServices";
     	case "15":
     		return "kinds:PublishingServices";
     	case "16":
     		return "kinds:SewageServices";
     	case "17":
     		return "kinds:HotelAndRestaurantServices";
     	case "18":
     		return "kinds:RailTransportServices";
     	case "19":
     		return "kinds:WaterTransportServices";
     	case "20":
     		return "kinds:SupportingTransportServices";
     	case "21":
     		return "kinds:LegalServices";
     	case "22":
     		return "kinds:PersonnelPlacementServices";
     	case "23":
     		return "kinds:InvestigationAndSecurityServices";
     	case "24":
     		return "kinds:EducationServices";
     	case "25":
     		return "kinds:HealthServices";
     	case "26":
     		return "kinds:CulturalServices";
     	case "27":
     		return "kinds:Services";
     	case "":
     		return null;
     	default: 
     		logger.warn("Doplnit: neznámý kód služby: " + sluzbaKod);
     		return "\"" + sluzbaKod + "\"";
     	}
     }
 
     private String getStringFromElements(Elements elements)
     {
 		String out = null;
 		if (elements.size() > 0) out = escapeString(elements.first().text());
 		return out;
     }
     
     private String getDruhRizeni(String druh)
     {
     	switch (druh)
     	{
     	case "Zakázka malého rozsahu":
     		return "pccz:limit pccz:SmallAmount";
 
     	case "Zjednodušené podlimitní řízení":
     	case "zjednodušené podlimitní řízení":
     		return "pc:procedureType pccz:SimplifiedUnderLimit";
 
     	case "Jednací řízení s uveřejněním":
     		return "pc:procedureType proctypes:Negotiated";
 
     	case "Jednací řízení bez uveřejnění":
     		return "pc:procedureType proctypes:NegotiatedNotPublished";
 
     	case "Užší řízení":
     	case "Užší":
     		return "pc:procedureType proctypes:Restricted";
 
     	case "ZŘ8: Přímé zadání (podlimitní VZ)":
     		return "pccz:limit pccz:UnderLimit";
 
     	case "Podlimitní veřejná zakázka ve zjednodušeném řízení":
     		return "pccz:limit pccz:SimplifiedUnderLimit";
 
     	case "otevřené řízení":
     	case "Otevřené řízení":
     	case "Otevřené":
     	case "RizeniSUverejnenim":
     		return "pc:procedureType proctypes:Open";
 
     	//Následující je třeba zapracovat do onto
     	
     	case "Soutěžní dialog":
     		return "pc:procedureType proctypes:CompetitiveDialogue";
 
     	case "ZŘ7: VZ na základě RS s více uchazeči v režimu ZVZ":
     		return "pc:procedureType proctypes:ContractInFrameworkAgreementMultipleBidders";
     		
     	case "ZŘ9: Průzkum trhu - není zadávacím řízením ve smyslu ZVZ, ale způsobem oslovení dodavatelů prostřednictvím e-tržiště":
     		return "pc:procedureType proctypes:MarketAnalysis";
     		
     	case "On-line veřejná zakázka":
     		return "pc:procedureType proctypes:Online";
     		
     	case "Písemné oslovení dodavatelů, zveřejnění na profilu zadavatele":
     		return "pc:procedureType proctypes:PisemneOsloveniDodavatelu";
 
     	case "Soutěž o návrh":
     		return "pc:procedureType proctypes:CompetitionForSpecification";
     		
     	case "DNS cyklus":
     		return "pc:procedureType proctypes:DNSCycle";
     	
     	case "Výzva k podání nabídky":
     	case "Výzva k podání nabídek":
     		return "pc:procedureType proctypes:VyzvaKPodaniNabidek";
 
     	default:
     		
     		logger.warn("Doplnit druh řízení: " + druh);
     		return "pc:procedureType \"" + druh + "\"";
     	}
     }
     
     private String fixIC(String oldIC)
     {
 		if (oldIC == null) return null;
     	//.replaceAll("|.*", "") je hack za czbe:CZ62537741|00244732 zruseno 21.3.2013, <http://www.vestnikverejnychzakazek.cz/Views/Form/Display/395580>
 		String newIC = oldIC.replace(" ", "").replace("/", "").replace(" ", "").replaceAll("\\|.*", ""); //second is &nbsp; ASCII 160, first is space, ASCII 32
 		if (!oldIC.equals(newIC)) logger.info("Varování: IC/DIC obsahuje chyby: " + oldIC + " Oprava: " + newIC);
     	return newIC; 
     }
     
     private String getCurrency(String price)
     {
     	if (price == null) return null;
     	if (price.contains("€") || price.contains("EUR")) return "EUR";
     	else return "CZK";
     }
     
     private String fixPrice(String oldPrice)
     {
 		String newPrice = oldPrice;
     	if (newPrice == null) return null;
 		newPrice.replace("DPH", "");
 		newPrice.replace("včetně", "");
 		newPrice.replace(" s ", "");
     	if (newPrice.contains(" Kč")) {
 			newPrice = newPrice.substring(0, newPrice.indexOf(" Kč"));
 		}
 		if (newPrice.contains(".-")) {
 			newPrice = newPrice.substring(0, newPrice.indexOf(".-")).replace(".", "");
 		}
 		if (newPrice.contains(",-")) {
 			newPrice = newPrice.substring(0, newPrice.indexOf(",-")).replace(",","");
 		}
 		if (newPrice.contains("(")) newPrice = newPrice.substring(0, newPrice.indexOf("("));
 		if (newPrice.contains("/")) newPrice = newPrice.substring(0, newPrice.indexOf("/"));
 		if (newPrice.matches(".*[^0-9]{4,}.*")){
 			logger.info("Varování: Cena obsahuje text. Originál: " + oldPrice);
 			return null;
 		}
 		if (newPrice.contains(",") && newPrice.contains(".")) {
 			newPrice = newPrice.replace(".", "").replace(",",".");
 		}
 		if (newPrice.contains(",")) {
 			newPrice = newPrice.replace(",", ".");
 		}
 		if (newPrice.contains(".") && (newPrice.indexOf('.') != newPrice.lastIndexOf('.'))) {
 			if (newPrice.length() - newPrice.lastIndexOf('.') == 3) {
 				//na konci zřejmě haléře
 				newPrice = newPrice.substring(0, newPrice.lastIndexOf(".")).replace(".", "") + newPrice.substring(newPrice.lastIndexOf("."));
 			}
 			else {
 				newPrice = newPrice.replace(".", "");
 			}
 			logger.info("Varování: Cena obsahuje několik teček. Originál: " + oldPrice + " Aktuální: " + newPrice);
 		}
 		if (newPrice.contains(".") && newPrice.length() > 3 && (newPrice.length() - newPrice.indexOf('.') > 3)) {
 			if (newPrice.substring(0, newPrice.indexOf('.')).length() <= 3 && (newPrice.length() - newPrice.indexOf('.') == 4)) {
 				newPrice.replace(".", "");
 				logger.info("Varování: Cena obsahuje tečku a za ní 3 číslice - asi to nejsou haléře, odstraňuji. Originál: " + oldPrice + " Aktuální: " + newPrice);
 			}
 			else {
 				logger.info("Varování: Cena obsahuje tečku a za ní více jak 3 číslice - špatný formát, ale haláře? Nechávám. Originál: " + oldPrice + " Aktuální: " + newPrice);
 			}
 		}
 		if (newPrice.contains(" ")) {
 			newPrice = newPrice.replace(" ", "");
 		}
 		newPrice = newPrice.replaceAll("[^0-9\\.]", "");
 		if (newPrice.endsWith(".")) newPrice = newPrice.substring(0, newPrice.lastIndexOf('.'));
 		if (newPrice.equals(".")) {
 			logger.info("Varování: Cena zdegenerovala. Originál: " + oldPrice + " Aktulní: " + newPrice);
 			newPrice = null;
 		}
 		if (!oldPrice.equals(newPrice)) {
 			logger.info("Varování: Cena obsahuje chyby: " + oldPrice + " Oprava: " + newPrice);
 		}
 		return newPrice;
     }
 
     @Override
     protected void parse(org.jsoup.nodes.Document doc, String docType, URL url) {
         if (docType.equals("list") || docType.equals("first") || docType.equals("cancelledList") || docType.equals("firstCancelled")) {
         	logger.info("Parsing list #" + numlists++);
         	/* Na detailu si najdu nazev a ic a vyhodim jako nejaky element */
             Elements rows = doc.select("table tr");
             if (docType.equals("first"))
             {
             	totalnumrows = Integer.parseInt(doc.select("div.t-status-text").first().text().replaceAll(".* z ([0-9]+)", "$1"));
             }
             else if (docType.equals("firstCancelled"))
             {
             	totalcancellednumrows = Integer.parseInt(doc.select("div.t-status-text").first().text().replaceAll(".* z ([0-9]+)", "$1"));
             }
             for(int i = 1; i < rows.size(); i++)
             {
             	if (docType.equals("list") || docType.equals("first")) numrows++;
             	else if (docType.equals("cancelledList") || docType.equals("firstCancelled")) cancellednumrows++;
             	Element row = rows.get(i);
             	Elements rowElements = row.getAllElements();
             	String contractId = rowElements.get(1).text();
             	String evidencniCislo = rowElements.get(2).text();
             	String nazevZadavatele = escapeString(rowElements.get(3).text());
             	String ico = fixIC(rowElements.get(4).text());
             	
             	
             	String guid = null;
             	String BEuri;
             	
             	if (!ico.isEmpty())
         		{
             		BEuri = icoBEprefix + ico;
         		}
         		else
         		{
             		guid = UUID.randomUUID().toString();
             		logger.info("Varování: ICO nenalezeno v seznamu profilů: " + nazevZadavatele);
             		missingIco++;
         			BEuri = guidBEprefix + guid;
         		}
             	
             	
             	URL profilZadavatele = null;
             	URL externiProfilZadavatele = null;
             	String originalProfilZadavatele = escapeString(rowElements.get(5).text());
             	externiProfilZadavatele = parseURL(originalProfilZadavatele, ico, nazevZadavatele, "Profil zadavatele (seznam)");
 				
             	profilZadavatele = getInternalURLProfiluZadavatele(externiProfilZadavatele);
             	String datumUverejneni = escapeString(rowElements.get(7).text()).replaceAll("([0-9]{2}).([0-9]{2}).([0-9]{4})","$3-$2-$1");
             	
             	if (ico.isEmpty())
             	{
         			ps.println("<" + BEuri + "> a gr:BusinessEntity ;");
             	}
             	else
             	{
             		ps.println("czbe:CZ" + ico + " a gr:BusinessEntity ;");
         			ps.println("\tadms:identifier <" + icoBEprefix + ico + "/identifier> ;");
             	}
             	ps.println("\tgr:legalName \"" + nazevZadavatele + "\" ;");
             	ps.println("\tdcterms:title \"" + nazevZadavatele + "\" ;");
             	if (!contractId.isEmpty()) ps.println("\tdcterms:source <http://www.vestnikverejnychzakazek.cz/Views/Form/Display/" + contractId + "> ;");
             	if (profilZadavatele != null) ps.println("\tpc:buyerProfile <" + profilZadavatele + "> ;");
             	ps.println("\t.");
             	ps.println();
             	
             	if (!ico.isEmpty())
             	{
 		        	ps.println("<" + icoBEprefix + ico + "/identifier> a adms:Identifier ;");
 		        	ps.println("\tskos:notation \"" + ico + "\" ;");
 	    			ps.println("\tskos:inScheme <http://linked.opendata.cz/resource/concept-scheme/CZ-ICO> ;");
 		        	ps.println("\tskos:prefLabel \"" + ico + "\" ;");
 		        	ps.println("\tadms:schemeAgency \"Český statistický úřad\"");
 		        	ps.println("\t.");
 		        	ps.println();
             	}
             	
             	if (profilZadavatele != null) {
             		numprofiles++;
 	            	ps.println("<" + profilZadavatele + "> a pc:BuyerProfile ;");
 	            	ps.println("\tskos:notation \"" + evidencniCislo + "\" ;");
 	            	if (originalProfilZadavatele != null) ps.println("\tpc:originalBuyerProfileUrl \"\"\"" + originalProfilZadavatele + "\"\"\" ;");
 	            	ps.println("\ts:url <" + externiProfilZadavatele + "> ;");
 
 	            	if (docType.equals("cancelledList") || docType.equals("firstCancelled"))
 	            	{
 	            		String cancellationURI = profilZadavatele + "/cancellation/" + (ico.isEmpty()? guid : ico);
 		            	ps.println("\tpc:cancellation <" + cancellationURI + "> ");
 	            		ps.println("\t.");
 		            	ps.println();            	
 		            	ps.println("<" + cancellationURI + "> a pc:BuyerProfileCancellation ;");
 	            	}
 	            	else if (docType.equals("list") || docType.equals("first"))
 	            	{
 	            		String publicationURI = profilZadavatele + "/publication/" + (ico.isEmpty()? guid : ico);
 		            	ps.println("\tpc:publication <" + publicationURI + "> ");
 		            	ps.println("\t.");
 		            	ps.println();            	
 		            	ps.println("<" + publicationURI + "> a pc:BuyerProfilePublication ;");
 	            	}
             		ps.println("\tdcterms:published \"" + datumUverejneni + "\"^^xsd:date ;");
 	            	ps.println("\t.");
 	            	ps.println();            	
 
             	}
             }
         }
         else if (docType.equals("detail"))
         {
         	++numdetails;
         	
         	logger.debug("Phase 1/2: " + (int)Math.floor((double)numdetails*100/(double)totalnumrows) + "% Parsing detail #" + numdetails + "/" + totalnumrows);
         	String nazev = escapeString(doc.select("textarea#FormItems_Nazev_I").text());
         	String ulice = escapeString(doc.select("textarea#FormItems_UliceCP_I").text());
         	String obec = escapeString(doc.select("input#FormItems_Obec_I").attr("value"));
         	String psc = escapeString(doc.select("input#FormItems_Psc_I").attr("value").replace(" ", ""));
         	
         	String stat = escapeString(doc.select("select#FormItems_Stat_I option[selected=selected]").attr("value"));
         	
         	String kodPravniFormy = escapeString(doc.select("input#FormItems_KodPravniFormy_I").attr("value"));
         	String ico = fixIC(escapeString(doc.select("input#FormItems_IdentifikacniCislo_I").attr("value")));
         	String guid = null;
         	if (ico.isEmpty()) 
     		{
         		guid = UUID.randomUUID().toString();
         		logger.info("Varování: Nenalezeno IČ: " + nazev);
         		missingIco++;
     		}
         	String dic = fixIC(escapeString(doc.select("input#FormItems_DanoveIdentifikacniCislo_I").attr("value")));
         	String zujObce = escapeString(doc.select("input#FormItems_ZujObce_I").attr("value"));
         	String kodNuts = escapeString(doc.select("input#FormItems_KodNuts_I").attr("value"));
         	if (kodNuts.startsWith("CZ")) kodNuts = kodNuts.substring(2);
         	String opravnenaOsoba = escapeString(doc.select("input#FormItems_OpravnenaOsoba_I").attr("value"));
         	URL obecnaAdresaZadavatele = parseURL(doc.select("textarea#FormItems_ObecnaAdresaVerejnehoZadavatele_I").text(), ico, nazev, "URL zadavatele");
         	
         	String typ = doc.select("input[name=FormItems.DruhZadavatele_II][checked=checked]").attr("value");        	
         	String typJiny = escapeString(doc.select("textarea[id=FormItems.Jine_II]").text());        	
         	String hpcJine = escapeString(doc.select("textarea[id=FormItems.HpcJinySpecifikujte_II]").text());        	
         	
         	boolean SluzbyProSirokouVerejnost = doc.select("input#FormItems_HpcSluzbyProSirokouVerejnost_II[checked=checked]").size() > 0;
         	boolean Obrana = doc.select("input#FormItems_HpcObrana_II[checked=checked]").size() > 0;
         	boolean VerejnyPoradekABezpecnost = doc.select("input#FormItems_HpcVerejnyPoradekABezpecnost_II[checked=checked]").size() > 0;
         	boolean ZivotniProstredi = doc.select("input#FormItems_HpcZivotniProstredi_II[checked=checked]").size() > 0;
         	boolean HospodarskeAFinancniZalezitosti = doc.select("input[id=FormItems_HpcHospodarskeAFinancniZalezitosti_II][checked=checked]").size() > 0;
         	boolean Zdravotnictvi = doc.select("input[id=FormItems_HpcZdravotnictvi_II][checked=checked]").size() > 0;
         	boolean BydleniAObcanskaVybavenost = doc.select("input[id=FormItems_HpcBydleniAObcanskaVybavenost_II][checked=checked]").size() > 0;
         	boolean SocialniSluzby = doc.select("input[id=FormItems_HpcSocialniSluzby_II][checked=checked]").size() > 0;
         	boolean RekreaceKulturaANabozenstvi = doc.select("input[id=FormItems_HpcRekreaceKulturaANabozenstvi_II][checked=checked]").size() > 0;
         	boolean Skolstvi = doc.select("input[id=FormItems_HpcSkolstvi_II][checked=checked]").size() > 0;
         	boolean hpcJiny = doc.select("input[id=FormItems_HpcJiny_II][checked=checked]").size() > 0;
         	
         	String nazevProfiluZadavatele = escapeString(doc.select("textarea#FormItems_NazevProfilu_III").text());        	
         	String originalUrlProfiluZadavatele = doc.select("textarea#FormItems_UrlAdresaVerejnehoZadavatele_III").text();
         	URL ExterniURIProfiluZadavatele = parseURL(originalUrlProfiluZadavatele, ico, nazev, "Profil zadavatele (detail)");
         	
         	URL URIProfiluZadavatele = getInternalURLProfiluZadavatele(ExterniURIProfiluZadavatele);
 
         	originalUrlProfiluZadavatele = escapeString(originalUrlProfiluZadavatele);
         	String KontaktniOsobaOdpovedna = escapeString(doc.select("input#FormItems_KontaktniOsobaOdpovedna_III").attr("value"));        	
         	String JmenoKontaktniOsoby = escapeString(doc.select("input#FormItems_Jmeno_III").attr("value"));        	
         	String Email = escapeString(doc.select("input#FormItems_Email_III").attr("value").replace(" ", ""));        	
         	String PlatnostKeDni = doc.select("input#FormItems_PlatnostKeDni_III").attr("value").replaceAll("([0-9]{2}).([0-9]{2}).([0-9]{4})", "$3-$2-$1").replaceAll("([0-9]{4}).([0-9]{2}).([0-9]{2})", "$1-$2-$3");        	
         	String telefon = escapeString(doc.select("input#FormItems_Telefon_III").attr("value").replace(" ", ""));
         	String fax = escapeString(doc.select("input#FormItems_Fax_III").attr("value").replace(" ", ""));
         	
         	String ZakDruh = doc.select("input[name=FormItems.DruhZakazky_IV][checked=checked]").attr("value");        	
         	
         	boolean Provadeni = doc.select("input[id=FormItems_StavebniPraceProvadeni_IV][checked=checked]").size() > 0;
         	boolean ProjektAProvadeni = doc.select("input[id=FormItems_StavebniPraceProjektProvadeni_IV][checked=checked]").size() > 0;
         	boolean ProvadeniJine = doc.select("input[id=FormItems_StavebniPraceProvadeniSouladPozadavky_IV][checked=checked]").size() > 0;
        	
         	String TypDodavky = doc.select("input[name=FormItems.TypDodavky_IV][checked=checked]").attr("value");
         	
         	String KatSluzeb = doc.select("input#FormItems_KatSluzeb_IV").attr("value");
 
         	String CPV1 = doc.select("input#FormItems_HlavnipredmetCinnostiI_IV").attr("value");
         	String CPV2 = doc.select("input#FormItems_HlavnipredmetCinnostiII_IV").attr("value");
         	String CPV3 = doc.select("input#FormItems_HlavnipredmetCinnostiIII_IV").attr("value");
         	String CPV4 = doc.select("input#FormItems_HlavnipredmetCinnostiIV_IV").attr("value");
         	String CPV5 = doc.select("input#FormItems_HlavnipredmetCinnosti5_IV").attr("value");
 
         	String Odeslano = doc.select("input#FormItems_DatumOdeslaniTohotoOznameni_IV").attr("value").replaceAll("([0-9]{2}).([0-9]{2}).([0-9]{4})", "$3-$2-$1").replaceAll("([0-9]{4}).([0-9]{2}).([0-9]{2})", "$1-$2-$3");        	
         	
         	//To RDF
         	String BEuri;
         	
         	if (!ico.isEmpty())
     		{
         		BEuri = icoBEprefix + ico;
         		ps.println("czbe:CZ" + ico + " a gr:BusinessEntity ;");
     			ps.println("\tadms:identifier <" + icoBEprefix + ico + "/identifier> ;");
     		}
     		else
     		{
     			BEuri = guidBEprefix + guid;
     			ps.println("<" + BEuri + "> a gr:BusinessEntity ;");
     		}
         	
     		ps.println("\tgr:legalName \"" + nazev + "\" ;");
         	ps.println("\tdcterms:title \"" + nazev + "\" ;");
         	if (!kodNuts.isEmpty()) ps.println("\tpc:location <http://nuts.geovocab.org/id/CZ" + kodNuts + ">, <http://ec.europa.eu/eurostat/ramon/rdfdata/nuts2008/CZ" + kodNuts + "> ;");
         	if (!dic.isEmpty()) ps.println("\t<http://linked.opendata.cz/ontology/buyer-profiles/dic> \"" + dic + "\" ;");
         	if (!kodPravniFormy.isEmpty()) ps.println("\t<http://linked.opendata.cz/ontology/buyer-profiles/legalForm> <http://purl.org/procurement/legal-form#" + kodPravniFormy + "> ;");
         	if (!zujObce.isEmpty()) ps.println("\tpc:location <http://linked.opendata.cz/resource/region/" + zujObce + "> ;");        	
         	if (!stat.isEmpty()) ps.println("\tpc:location " + getStat(stat) + " ;");
         	if (getDruh(typ) != null) ps.println("\tpc:authorityKind " + getDruh(typ) + " ;");
         	if (!typJiny.isEmpty()) ps.println("\tpc:authorityKind \"" + typJiny + "\"");
     		
         	ps.println("\tv:vcard <" + BEuri + "/vcard/buyer> ;");
         	
         	if (!CPV1.isEmpty()) ps.println("\tgr:category <http://linked.opendata.cz/resource/cpv-2008/concept/" + CPV1.substring(0, 8) + "> ;");
         	if (!CPV2.isEmpty()) ps.println("\tgr:category <http://linked.opendata.cz/resource/cpv-2008/concept/" + CPV2.substring(0, 8) + "> ;");
         	if (!CPV3.isEmpty()) ps.println("\tgr:category <http://linked.opendata.cz/resource/cpv-2008/concept/" + CPV3.substring(0, 8) + "> ;");
         	if (!CPV4.isEmpty()) ps.println("\tgr:category <http://linked.opendata.cz/resource/cpv-2008/concept/" + CPV4.substring(0, 8) + "> ;");
         	if (!CPV5.isEmpty()) ps.println("\tgr:category <http://linked.opendata.cz/resource/cpv-2008/concept/" + CPV5.substring(0, 8) + "> ;");
         	
         	if (SluzbyProSirokouVerejnost) ps.println("\tpc:mainActivity activities:GeneralServices ;");
         	if (Obrana) ps.println("\tpc:mainActivity activities:Defence ;");
         	if (VerejnyPoradekABezpecnost) ps.println("\tpc:mainActivity activities:Safety ;");
         	if (ZivotniProstredi) ps.println("\tpc:mainActivity activities:Environment ;");
         	if (HospodarskeAFinancniZalezitosti) ps.println("\tpc:mainActivity activities:EconomicAffairs ;");
         	if (Zdravotnictvi) ps.println("\tpc:mainActivity activities:Health ;");
         	if (BydleniAObcanskaVybavenost) ps.println("\tpc:mainActivity activities:Housing ;");
         	if (SocialniSluzby) ps.println("\tpc:mainActivity activities:SocialProtection ;");
         	if (RekreaceKulturaANabozenstvi) ps.println("\tpc:mainActivity activities:Cultural ;");
         	if (Skolstvi) ps.println("\tpc:mainActivity activities:Educational ;");
         	if (hpcJiny && !hpcJine.isEmpty()) ps.println("\tpc:mainActivity \"" + hpcJine + "\" ;");
         	
         	switch (ZakDruh)
         	{
         	case "WORKS":
         		boolean found = false;
         		if (Provadeni)
     			{
         			ps.println("\tpc:kind kinds:WorksExecution ;");
         			found = true;
     			}
         		if (ProjektAProvadeni)
     			{
         			ps.println("\tpc:kind kinds:WorksDesignExecution ;");
         			found = true;
     			}
         		if (ProvadeniJine)
     			{
         			ps.println("\tpc:kind kinds:WorksRealisation ;");
         			found = true;
     			}
         		if (!found) ps.println("\tpc:kind kinds:Works ;");
         		break;
         	case "SUPPLIES":
         		switch (TypDodavky)
         		{
         		case "PURCHASE":
         			ps.println("\tpc:kind kinds:SuppliesPurchase ;");
         			break;
         		case "LEASE":
         			ps.println("\tpc:kind kinds:SuppliesLease ;");
         			break;
         		case "COMBINATION_THESE":
         			ps.println("\tpc:kind kinds:SuppliesPurchase ;");
         			ps.println("\tpc:kind kinds:SuppliesLease ;");
         			break;
         		default:
                 	ps.println("\tpc:kind kinds:Supplies ;");
         		}
         		break;
         	case "SERVICES":
         		ps.println("\tpc:kind kinds:Services ;");
         		if (getSluzby(KatSluzeb) != null) ps.println("\tpc:kind " + getSluzby(KatSluzeb) + " ;");
         		break;
         	}
         	
         	ps.println("\t.");
         	ps.println();
         	
         	if (!ico.isEmpty()) {
         		ps.println("<" + BEuri + "/identifier> a adms:Identifier ;");
 	        	ps.println("\tskos:notation \"" + ico + "\" ;");
     			ps.println("\tskos:inScheme <http://linked.opendata.cz/resource/concept-scheme/CZ-ICO> ;");
 	        	ps.println("\tskos:prefLabel \"" + ico + "\" ;");
 	        	ps.println("\tadms:schemeAgency \"Český statistický úřad\"");
 	        	ps.println("\t.");
 	        	ps.println();            	
         	}
         	
     		ps.println("<" + BEuri + "> a gr:BusinessEntity .");
     		ps.println();
     		ps.println("<" + BEuri + "/vcard/buyer> a v:VCard ;");
         	
     		if (!opravnenaOsoba.isEmpty()) ps.println("\tv:fn \"" + opravnenaOsoba + "\" ;");
         	if (obecnaAdresaZadavatele != null) ps.println("\tv:url <" + obecnaAdresaZadavatele + "> ;");
         	ps.println("\tv:org [");
         	ps.println("\t\ta v:Organization ;");
         	ps.println("\t\tv:organization-name \"" + nazev + "\" ;");
         	ps.println("\t] ;");
         	ps.println("\tv:hasAddress [");
         	ps.println("\t\ta v:Work ;");
         	ps.println("\t\tv:streetAddress \"" + ulice + "\" ;");
         	if (!psc.isEmpty()) ps.println("\t\tv:postalCode \"" + psc + "\" ;");
         	ps.println("\t\tv:locality \"" + obec + "\" ;");
         	if (!stat.isEmpty() && getStatString(stat) != null) ps.println("\t\tv:country \"" + getStatString(stat) + "\" ;");
         	ps.println("\t] ");        	
         	ps.println("\t.");
         	ps.println();            	
 
         	if (URIProfiluZadavatele != null)
         	{
         		ps.println("<" + BEuri + "> pc:buyerProfile <" + URIProfiluZadavatele + "> .");
         		
             	ps.println("<" + URIProfiluZadavatele + "> a pc:BuyerProfile ;");
         		ps.println("\tdcterms:title \"" + nazevProfiluZadavatele + "\" ;");
             	ps.println("\tv:vcard <" + BEuri + "/vcard/buyer-profile> ;");
             	if (originalUrlProfiluZadavatele != null && !originalUrlProfiluZadavatele.isEmpty()) ps.println("\tpc:originalBuyerProfileUrl \"\"\"" + originalUrlProfiluZadavatele + "\"\"\" ;");
             	ps.println("\ts:url <" + ExterniURIProfiluZadavatele + "> ;");
 
             	String publicationURI = URIProfiluZadavatele + "/publication/" + ico;
             	ps.println("\tpc:publication <" + publicationURI + "> ");
             	ps.println("\t.");
             	ps.println();
             	
             	ps.println("<" + publicationURI + "> a pc:BuyerProfilePublication ;");        		
             	ps.println("\tdcterms:valid \"" + PlatnostKeDni + "\"^^xsd:date ;");
         		ps.println("\tdcterms:issued \"" + Odeslano + "\"^^xsd:date ;");
         		ps.println("\tdcterms:publisher <" + BEuri + "> ;");
         		ps.println("\tdcterms:source <" + url + "> ;");
 	        	ps.println("\t.");
 	        	ps.println();        		
         		
             	ps.println("<" + BEuri + "/vcard/buyer-profile> a v:VCard ;");
 	        	if (!JmenoKontaktniOsoby.isEmpty()) ps.println("\tv:fn \"" + JmenoKontaktniOsoby + "\" ;");
 	        	ps.println("\tv:org [");
 	        	ps.println("\t\ta v:Organization ;");
 	        	ps.println("\t\tv:organization-name \"" + nazevProfiluZadavatele + "\" ;");
 	        	ps.println("\t] ;");
 	        	if (!KontaktniOsobaOdpovedna.isEmpty()) ps.println("\tv:extended-address \"" + KontaktniOsobaOdpovedna + "\" ;");
 	        	if (!telefon.isEmpty()) {
 		        	ps.println("\tv:tel [");
 		        	ps.println("\t\ta v:Tel, v:Work;");
 		        	ps.println("\t\trdf:value \"" + telefon + "\" ;");
 		        	ps.println("\t] ;");
 	        	}
 	        	if (!fax.isEmpty()) {
 		        	ps.println("\tv:tel [");
 		        	ps.println("\t\ta v:Fax, v:Work;");
 		        	ps.println("\t\trdf:value \"" + fax + "\" ;");
 		        	ps.println("\t] ;");
 	        	}
 	        	if (!Email.isEmpty())
 	        	{
 	        		ps.println("\tv:email <mailto:" + Email + "> ;");
 	        	}
 	        	ps.println("\t.");
 	        	ps.println();
         	}
         }
         else if (docType.equals("cancelledDetail"))
         {
         	numCancelledDetails++;
         	logger.debug("Phase 2/2: " + (int)Math.floor((double)numCancelledDetails*100/(double)totalcancellednumrows) + "% Parsing cancelled detail #" + numCancelledDetails + "/" + totalcancellednumrows);
         	
         	String nazev = escapeString(doc.select("textarea#FormItems_Nazev_I").text());
         	String ulice = escapeString(doc.select("textarea#FormItems_UliceCP_I").text());
         	String obec = escapeString(doc.select("input#FormItems_Obec_I").attr("value"));
         	String psc = escapeString(doc.select("input#FormItems_Psc_I").attr("value").replace(" ", ""));
         	
         	String stat = escapeString(doc.select("select#FormItems_Stat_I option[selected=selected]").attr("value"));
         	
         	String kodPravniFormy = escapeString(doc.select("input#FormItems_KodPravniFormy_I").attr("value"));
         	String ico = fixIC(escapeString(doc.select("input#FormItems_IdentifikacniCislo_I").attr("value")));
         	String guid = null;
         	if (ico.isEmpty()) 
     		{
         		guid = UUID.randomUUID().toString();
         		logger.info("Varování: Nenalezeno IČ: " + nazev);
         		missingIco++;
     		}
         	String dic = fixIC(escapeString(doc.select("input#FormItems_DanoveIdentifikacniCislo_I").attr("value")));
         	String zujObce = escapeString(doc.select("input#FormItems_ZujObce_I").attr("value"));
         	String kodNuts = escapeString(doc.select("input#FormItems_KodNuts_I").attr("value"));
         	if (kodNuts.startsWith("CZ")) kodNuts = kodNuts.substring(2);
         	String opravnenaOsoba = escapeString(doc.select("input#FormItems_OpravnenaOsoba_I").attr("value"));
         	URL obecnaAdresaZadavatele = parseURL(doc.select("textarea#FormItems_ObecnaAdresaVerejnehoZadavatele_I").text(), ico, nazev, "URL zadavatele");
         	
         	String typ = doc.select("input[name=FormItems.DruhZadavatele_II][checked=checked]").attr("value");        	
         	String typJiny = escapeString(doc.select("textarea[id=FormItems.Jine_II]").text());        	
         	String hpcJine = escapeString(doc.select("textarea[id=FormItems.HpcJinySpecifikujte_II]").text());        	
         	
         	boolean SluzbyProSirokouVerejnost = doc.select("input#FormItems_HpcSluzbyProSirokouVerejnost_II[checked=checked]").size() > 0;
         	boolean Obrana = doc.select("input#FormItems_HpcObrana_II[checked=checked]").size() > 0;
         	boolean VerejnyPoradekABezpecnost = doc.select("input#FormItems_HpcVerejnyPoradekABezpecnost_II[checked=checked]").size() > 0;
         	boolean ZivotniProstredi = doc.select("input#FormItems_HpcZivotniProstredi_II[checked=checked]").size() > 0;
         	boolean HospodarskeAFinancniZalezitosti = doc.select("input[id=FormItems_HpcHospodarskeAFinancniZalezitosti_II][checked=checked]").size() > 0;
         	boolean Zdravotnictvi = doc.select("input[id=FormItems_HpcZdravotnictvi_II][checked=checked]").size() > 0;
         	boolean BydleniAObcanskaVybavenost = doc.select("input[id=FormItems_HpcBydleniAObcanskaVybavenost_II][checked=checked]").size() > 0;
         	boolean SocialniSluzby = doc.select("input[id=FormItems_HpcSocialniSluzby_II][checked=checked]").size() > 0;
         	boolean RekreaceKulturaANabozenstvi = doc.select("input[id=FormItems_HpcRekreaceKulturaANabozenstvi_II][checked=checked]").size() > 0;
         	boolean Skolstvi = doc.select("input[id=FormItems_HpcSkolstvi_II][checked=checked]").size() > 0;
         	boolean hpcJiny = doc.select("input[id=FormItems_HpcJiny_II][checked=checked]").size() > 0;
         	
         	String nazevProfiluZadavatele = escapeString(doc.select("textarea#FormItems_NazevProfilu_III").text());        	
         	String originalUrlProfiluZadavatele = doc.select("textarea#FormItems_UrlAdresaVerejnehoZadavatele_III").text();
         	        	
         	URL ExterniURIProfiluZadavatele = parseURL(originalUrlProfiluZadavatele, ico, nazev, "Profil zadavatele (detail)");
         	URL URIProfiluZadavatele = getInternalURLProfiluZadavatele(ExterniURIProfiluZadavatele);
         	
         	originalUrlProfiluZadavatele = escapeString(originalUrlProfiluZadavatele);
         	
         	String KontaktniOsobaOdpovedna = escapeString(doc.select("input#FormItems_KontaktniOsobaOdpovedna_III").attr("value"));        	
         	String JmenoKontaktniOsoby = escapeString(doc.select("input#FormItems_Jmeno_III").attr("value"));        	
         	String Email = escapeString(doc.select("input#FormItems_Email_III").attr("value").replace(" ", ""));        	
         	String telefon = escapeString(doc.select("input#FormItems_Telefon_III").attr("value").replace(" ", ""));
         	String fax = escapeString(doc.select("input#FormItems_Fax_III").attr("value").replace(" ", ""));
         	
         	String datumZruseni = doc.select("input#FormItems_DatumZruseni_III").attr("value").replaceAll("([0-9]{2}).([0-9]{2}).([0-9]{4})", "$3-$2-$1").replaceAll("([0-9]{4}).([0-9]{2}).([0-9]{2})", "$1-$2-$3");
         	String Odeslano = doc.select("input#FormItems_DatumOdeslaniTohotoOznameni_III").attr("value").replaceAll("([0-9]{2}).([0-9]{2}).([0-9]{4})", "$3-$2-$1").replaceAll("([0-9]{4}).([0-9]{2}).([0-9]{2})", "$1-$2-$3");        	
         	
         	//To RDF
         	String BEuri;
         	
         	if (!ico.isEmpty())
     		{
         		BEuri = icoBEprefix + ico;
         		ps.println("czbe:CZ" + ico + " a gr:BusinessEntity ;");
     			ps.println("\tadms:identifier <" + icoBEprefix + ico + "/identifier> ;");
     		}
     		else
     		{
     			BEuri = guidBEprefix + guid;
     			ps.println("<" + BEuri + "> a gr:BusinessEntity ;");
     		}
         	
     		ps.println("\tgr:legalName \"" + nazev + "\" ;");
         	ps.println("\tdcterms:title \"" + nazev + "\" ;");
         	if (!kodNuts.isEmpty()) ps.println("\tpc:location <http://nuts.geovocab.org/id/CZ" + kodNuts + ">, <http://ec.europa.eu/eurostat/ramon/rdfdata/nuts2008/CZ" + kodNuts + "> ;");
         	if (!dic.isEmpty()) ps.println("\t<http://linked.opendata.cz/ontology/buyer-profiles/dic> \"" + dic + "\" ;");
         	if (!kodPravniFormy.isEmpty()) ps.println("\t<http://linked.opendata.cz/ontology/buyer-profiles/legalForm> <http://purl.org/procurement/legal-form#" + kodPravniFormy + "> ;");
         	if (!zujObce.isEmpty()) ps.println("\tpc:location <http://linked.opendata.cz/resource/region/" + zujObce + "> ;");        	
         	if (!stat.isEmpty()) ps.println("\tpc:location " + getStat(stat) + " ;");
         	if (getDruh(typ) != null) ps.println("\tpc:authorityKind " + getDruh(typ) + " ;");
         	if (!typJiny.isEmpty()) ps.println("\tpc:authorityKind \"" + typJiny + "\"");
     		
         	ps.println("\tv:vcard <" + BEuri + "/vcard/buyer> ;");
         	
         	if (SluzbyProSirokouVerejnost) ps.println("\tpc:mainActivity activities:GeneralServices ;");
         	if (Obrana) ps.println("\tpc:mainActivity activities:Defence ;");
         	if (VerejnyPoradekABezpecnost) ps.println("\tpc:mainActivity activities:Safety ;");
         	if (ZivotniProstredi) ps.println("\tpc:mainActivity activities:Environment ;");
         	if (HospodarskeAFinancniZalezitosti) ps.println("\tpc:mainActivity activities:EconomicAffairs ;");
         	if (Zdravotnictvi) ps.println("\tpc:mainActivity activities:Health ;");
         	if (BydleniAObcanskaVybavenost) ps.println("\tpc:mainActivity activities:Housing ;");
         	if (SocialniSluzby) ps.println("\tpc:mainActivity activities:SocialProtection ;");
         	if (RekreaceKulturaANabozenstvi) ps.println("\tpc:mainActivity activities:Cultural ;");
         	if (Skolstvi) ps.println("\tpc:mainActivity activities:Educational ;");
         	if (hpcJiny && !hpcJine.isEmpty()) ps.println("\tpc:mainActivity \"" + hpcJine + "\" ;");
         	
         	ps.println("\t.");
         	ps.println();
         	
         	if (!ico.isEmpty()) {
         		ps.println("<" + BEuri + "/identifier> a adms:Identifier ;");
         		ps.println("\tskos:prefLabel \"" + ico + "\" ;");
     			ps.println("\tskos:inScheme <http://linked.opendata.cz/resource/concept-scheme/CZ-ICO> ;");
         		ps.println("\tskos:notation \"" + ico + "\" ;");
 	        	ps.println("\tadms:schemeAgency \"Český statistický úřad\"");
 	        	ps.println("\t.");
 	        	ps.println();            	
         	}
         	
     		ps.println("<" + BEuri + "> a gr:BusinessEntity .");
     		ps.println();
     		ps.println("<" + BEuri + "/vcard/buyer> a v:VCard ;");
         	
     		if (!opravnenaOsoba.isEmpty()) ps.println("\tv:fn \"" + opravnenaOsoba + "\" ;");
         	if (obecnaAdresaZadavatele != null) ps.println("\tv:url <" + obecnaAdresaZadavatele + "> ;");
         	ps.println("\tv:org [");
         	ps.println("\t\ta v:Organization ;");
         	ps.println("\t\tv:organization-name \"" + nazev + "\" ;");
         	ps.println("\t] ;");
         	ps.println("\tv:hasAddress [");
         	ps.println("\t\ta v:Work ;");
         	ps.println("\t\tv:streetAddress \"" + ulice + "\" ;");
         	if (!psc.isEmpty()) ps.println("\t\tv:postalCode \"" + psc + "\" ;");
         	ps.println("\t\tv:locality \"" + obec + "\" ;");
         	if (!stat.isEmpty() && getStatString(stat) != null) ps.println("\t\tv:country \"" + getStatString(stat) + "\" ;");
         	ps.println("\t] ");        	
         	ps.println("\t.");
         	ps.println();            	
 
         	if (URIProfiluZadavatele != null)
         	{
         		ps.println("<" + BEuri + "> pc:buyerProfile <" + URIProfiluZadavatele + "> .");
         		ps.println();
         		
             	ps.println("<" + URIProfiluZadavatele + "> a pc:BuyerProfile ;");
         		ps.println("\tdcterms:title \"" + nazevProfiluZadavatele + "\" ;");
             	if (originalUrlProfiluZadavatele != null && !originalUrlProfiluZadavatele.isEmpty()) ps.println("\tpc:originalBuyerProfileUrl \"\"\"" + originalUrlProfiluZadavatele + "\"\"\" ;");
             	ps.println("\ts:url <" + ExterniURIProfiluZadavatele + "> ;");
             	
            		ps.println("\tv:vcard <" + BEuri + "/vcard/buyer-profile> ;");
 
         		String cancellationURI = URIProfiluZadavatele + "/cancellation/" + ico;
             	ps.println("\tpc:cancellation <" + cancellationURI + "> ");
             	ps.println("\t.");
             	ps.println();
             	
             	ps.println("<" + cancellationURI + "> a pc:BuyerProfileCancellation ;");        		
             	ps.println("\tdcterms:valid \"" + datumZruseni + "\"^^xsd:date ;");
         		ps.println("\tdcterms:issued \"" + Odeslano + "\"^^xsd:date ;");
         		ps.println("\tdcterms:publisher <" + BEuri + "> ;");
         		ps.println("\tdcterms:source <" + url + "> ;");
 	        	ps.println("\t.");
 	        	ps.println();        		
 
             	ps.println("<" + BEuri + "/vcard/buyer-profile> a v:VCard ;");
 	        	
             	if (!JmenoKontaktniOsoby.isEmpty()) ps.println("\tv:fn \"" + JmenoKontaktniOsoby + "\" ;");
 	        	ps.println("\tv:org [");
 	        	ps.println("\t\ta v:Organization ;");
 	        	ps.println("\t\tv:organization-name \"" + nazevProfiluZadavatele + "\" ;");
 	        	ps.println("\t] ;");
 	        	if (!KontaktniOsobaOdpovedna.isEmpty()) ps.println("\tv:extended-address \"" + KontaktniOsobaOdpovedna + "\" ;");
 	        	if (!telefon.isEmpty()) {
 		        	ps.println("\tv:tel [");
 		        	ps.println("\t\ta v:Tel, v:Work;");
 		        	ps.println("\t\trdf:value \"" + telefon + "\" ;");
 		        	ps.println("\t] ;");
 	        	}
 	        	if (!fax.isEmpty()) {
 		        	ps.println("\tv:tel [");
 		        	ps.println("\t\ta v:Fax, v:Work;");
 		        	ps.println("\t\trdf:value \"" + fax + "\" ;");
 		        	ps.println("\t] ;");
 	        	}
 	        	if (!Email.isEmpty())
 	        	{
 	        		ps.println("\tv:email <mailto:" + Email + "> ;");
 	        	}
 	        	ps.println("\t.");
 	        	ps.println();
         	}
         }
         else if (docType.equals("profil"))
         {
         	numprofiles++;
         	if (doc == null || doc.getAllElements().size() < 5)
         	{
         		logger.info("Prázdné XML: " + url);
         		invalidXML++;
         	}
         	else
         	{
         		URL urlProfilu = null;
 				try 
 				{
 		        	//Predpokladam ze URL dokumentu v sobe ma i dotaz na zakazky v danem obdobi, ten musim zase odebrat
 					urlProfilu = new URL(url.toString().substring(0, url.toString().indexOf("XMLdataVZ") - 1));
 				} catch (MalformedURLException e) {
 		        	logger.info("Profile #" + numprofiles + " has invalid URL: " + urlProfilu);
 					e.printStackTrace();
 				}
 				URL internalUrlProfilu = getInternalURLProfiluZadavatele(urlProfilu);
 	        	logger.debug("Parsing profile #" + numprofiles++ + ": " + urlProfilu);
 	        	logger.debug(url + " size " + doc.getAllElements().size());
 	        	boolean found = false;
 	        	
 	        	Elements profil_kod_element = doc.select("profil_kod");
 	        	String kodProfilu = null;
 	        	if (profil_kod_element.size() > 0)
         		{
         			found = true;
 	        		kodProfilu = escapeString(profil_kod_element.first().text().replace(" ", ""));
         		}
 	        	else logger.info("Chybí kód v XML profilu zadavatele: " + url);
 
 	        	Elements ic_element = doc.select("zadavatel ico_vlastni");
 	        	String IC = null;
 	        	if (ic_element.size() > 0)
 	        	{
 	        		found = true;
 	        		IC = fixIC(escapeString(ic_element.first().text()));
 	        	}
 	        	else logger.info("Chybí IČ v XML profilu zadavatele: " + url);
 	        	
 	        	Elements nazev_element = doc.select("zadavatel nazev_zadavatele");
 	        	String nazevZadavatele = null;
 	        	if (nazev_element.size() > 0)
 	        	{
 	        		found = true;
 	        		nazevZadavatele = escapeString(nazev_element.first().text());
 	        	}
 	        	else logger.info("Chybí název v XML profilu zadavatele: " + url);
 	        	
 	        	if (!found)
 	        	{
 	        		logger.info("Pravděpodobně není validní XML profil zadavatele: " + url);
 	        		invalidXML++;
 	        	}
 	        	else
 	        	{
 		        	String guid = null;
 		        	if (IC == null) 
 		    		{
 		        		guid = UUID.randomUUID().toString();
 		        		logger.info("Varování: Nenalezeno IČ v profilu zadavatele: " + nazevZadavatele + " URL: " + url);
 		        		missingIcoInProfile++;
 		    		}
 	
 		        	//RDF
 	        		ps.println("<" + internalUrlProfilu + "> a pc:BuyerProfile ;");
 	        		if (kodProfilu != null && !kodProfilu.isEmpty()) ps.println("\tskos:notation \"" + kodProfilu + "\" ;");
 	            	ps.println("\ts:url <" + urlProfilu + "> ;");
 	        		ps.println("\t.");
 	        		ps.println();
 	        		
 	        		if (!IC.isEmpty())
 	        		{
 	        			ps.println("<" + icoBEprefix + IC + "> a gr:BusinessEntity ;");
 	        			ps.println("\tpc:buyerProfile <" + urlProfilu + "> ;");
 	        		}
 	        		else
 	        		{
 	        			ps.println("<" + guidBEprefix + guid + "> a gr:BusinessEntity ;");
 	                	ps.println("\tpc:buyerProfile <" + urlProfilu + "> ;");
 	        		}
 	        		
 	        		ps.println("\tdcterms:title \"" + nazevZadavatele + "\" ;");
 	
 	            	if (!IC.isEmpty()) {
 	                	ps.println("\tadms:identifier <" + icoBEprefix + IC + "/identifier> ;");
 	    	        	ps.println("\t.");
 	    	        	ps.println();            	
 	            		
 	            		ps.println("<" + icoBEprefix + IC + "/identifier> a adms:Identifier ;");
 	    	        	ps.println("\tskos:notation \"" + IC + "\" ;");
 	    	        	ps.println("\tskos:prefLabel \"" + IC + "\" ;");
 	        			ps.println("\tskos:inScheme <http://linked.opendata.cz/resource/concept-scheme/CZ-ICO> ;");
 	    	        	ps.println("\tadms:schemeAgency \"Český statistický úřad\" ;");
 	    	        	ps.println("\t.");
 	    	        	ps.println();
 	            	}
 	            	else 
 	            	{
 	    	        	ps.println("\t.");
 	    	        	ps.println();
 	            	}
 	            	
 	            	Elements zakazky = doc.select("profil zakazka");
 	            	for (Element zakazka: zakazky)
 	            	{
 	            		numzakazky++;
 	            		
 	            		String kodVZprofil = getStringFromElements(zakazka.select("vz kod_vz_na_profilu"));
 	            		String kodVZusvzis = getStringFromElements(zakazka.select("vz kod_vz_na_usvzis"));
 	            		String nazevVZ = getStringFromElements(zakazka.select("vz nazev_vz"));
 	            		String stavVZ = getStav(getStringFromElements(zakazka.select("vz stav_vz")));
 	            		String druhRizeni = getStringFromElements(zakazka.select("vz druh_zadavaciho_rizeni"));
 	            		
 	            		String uriVZ;
 	            		String guidVZ;
 	            		if (kodVZusvzis != null && !kodVZusvzis.isEmpty() && !kodVZusvzis.contains("/") && !kodVZusvzis.contains(".") && !kodVZusvzis.contains("-"))
 	            		{
 	            			uriVZ = "http://linked.opendata.cz/resource/domain/buyer-profiles/contract/cz/" 
 	            					+ kodVZusvzis
 	            					.replace(" ", "")
 	            					.replace("VZ", "")
 	            					;
 	            		}
 	            		else if (kodVZprofil != null && !kodVZprofil.isEmpty())
             			{
 	            			uriVZ = internalUrlProfilu.toString().replaceFirst("/profile/", "/contract/") 
 	            					+ (internalUrlProfilu.toString().endsWith("/")? kodVZprofil.replace(" ", "") : "/" + kodVZprofil.replace(" ", ""));
             			}
 	            		else
 	            		{
 	                		guidVZ = UUID.randomUUID().toString();
 	                		uriVZ = "http://linked.opendata.cz/resource/domain/buyer-profiles/contract/cz/" + guidVZ;
 	                		logger.debug("Chybí kód zakázky na profilu: " + url);
 	            		}
 	            		
 	            		zak_ps.println("<" + uriVZ + "> a pc:Contract ;");
 	            		
 		        		if (!IC.isEmpty())
 		        		{
 		        			zak_ps.println("\tpc:contractingAuthority <" + icoBEprefix + IC + "> ;");		        		
 		        		}
 		        		else
 		        		{
 		        			zak_ps.println("\tpc:contractingAuthority <" + guidBEprefix + guid + "> ;");
 		        		}
 	            		
 	            		if (kodVZprofil != null && !kodVZprofil.isEmpty()) zak_ps.println("\tpccz:kodprofil \"" + kodVZprofil + "\" ;");
 	            		if (kodVZusvzis != null && !kodVZusvzis.isEmpty()) zak_ps.println("\tpccz:kodusvzis \"" + kodVZusvzis + "\" ;");
 	            		
 	            		if (nazevVZ != null && !nazevVZ.isEmpty()) zak_ps.println("\tdcterms:title \"" + nazevVZ + "\" ;");
 	            		if (stavVZ != null && !stavVZ.isEmpty()) zak_ps.println("\tpccz:status " + stavVZ + " ;");
 	            		if (druhRizeni != null && !druhRizeni.isEmpty()) zak_ps.println("\t" + getDruhRizeni(druhRizeni) + " ;");
             			
 	            		zak_ps.println("\t.\n");
 	            		
 	            		int currentUchazec = 0;
 	            		for (Element uchazec: zakazka.select("uchazec"))
 	            		{
 	            			if (uchazec == null) logger.warn("Uchazec null");
 	            			numuchazeci++;
 	            			String icUchazece = null;
 	            			if (uchazec.select("ico") != null) icUchazece = fixIC(getStringFromElements(uchazec.select("ico")));
 	            			String nazevUchazece = null;
 	            			if (uchazec.select("nazev_uchazece") != null) nazevUchazece = getStringFromElements(uchazec.select("nazev_uchazece"));
 	            			String zemeSidlaUchazece = null;
 	            			if (uchazec.select("zeme_sidla") != null) zemeSidlaUchazece = getStringFromElements(uchazec.select("zeme_sidla"));
 	            			String mistoPodnikani = null;
 	            			if (uchazec.select("misto_podnikani") != null) mistoPodnikani = getStringFromElements(uchazec.select("misto_podnikani"));
 	            			String cena_s_dph = null;
 	            			String currency = null;
 	            			if (uchazec.select("cena_s_dph") != null) {
 	            				String orig = getStringFromElements(uchazec.select("cena_s_dph"));
 	            				cena_s_dph = fixPrice(orig);
 	            				currency = getCurrency(orig);
 	            			}
 	            			else if (uchazec.select("cena_s_DPH") != null) {
 	            				String orig = getStringFromElements(uchazec.select("cena_s_DPH"));
 	            				cena_s_dph = fixPrice(orig);
 	            				cena_s_dph = fixPrice(orig);
 	            				currency = getCurrency(orig);
 	            			}
 	            			String bydliste = null;
 	            			if (uchazec.select("bydliste") != null) bydliste = getStringFromElements(uchazec.select("bydliste"));
 	            			
 	            			currentUchazec++;
 	            			String uriTender;
 	            			if (icUchazece != null && !icUchazece.isEmpty()) 
             				{
 	            				icUchazece = icUchazece.replaceAll("[^0-9]", "");
 	            				uriTender = uriVZ + "/tender/" + icUchazece;
             				}
 	            			else uriTender = uriVZ + "/tender/" + currentUchazec;
 	            			
 	            			zak_ps.println("<" + uriVZ + "> pc:tender <" + uriTender + "> .\n");
 	            			
 	            			zak_ps.println("<" + uriTender + "> a pc:Tender ;");
 	            			if (icUchazece != null && !icUchazece.isEmpty())
             				{
 	            				zak_ps.println("\tpc:supplier czbe:CZ" + icUchazece + " ;");
             				}
 	            			
 	            			if (cena_s_dph != null && !cena_s_dph.isEmpty())
             				{
 		            			String uriPriceSpec = uriTender + "/priceSpecification";
 		            			zak_ps.println("\tpc:offeredPrice <" + uriPriceSpec + "> ;");
 		            			zak_ps.println("\t.\n");
 		            			
 	            				zak_ps.println("<" + uriPriceSpec + "> a gr:PriceSpecification ;");	
 	            				zak_ps.println("\tgr:hasCurrencyValue \"" + cena_s_dph + "\"^^xsd:decimal ;");
 	            				zak_ps.println("\tgr:hasCurrency \"" + currency + "\" ;");
 	            				zak_ps.println("\tgr:valueAddedTaxIncluded true ;");
 	            				zak_ps.println("\t.\n");
             				}
 	            			else zak_ps.println("\t.\n");
 	            			
 	            			if (icUchazece != null && !icUchazece.isEmpty())
             				{
             				
 	            				ps.println("czbe:CZ" + icUchazece + " a gr:BusinessEntity ;");
 	            				ps.println("\tadms:identifier <" + icoBEprefix + icUchazece + "/identifier> ;");
 	            				if (nazevUchazece != null && !nazevUchazece.isEmpty()) ps.println("\tdcterms:title \"" + nazevUchazece + "\" ;");
 	            				if (nazevUchazece != null && !nazevUchazece.isEmpty()) ps.println("\tgr:legalName \"" + nazevUchazece + "\" ;");
 	            				if (zemeSidlaUchazece != null && !zemeSidlaUchazece.isEmpty() && getStat(zemeSidlaUchazece) != null) ps.println("\tdcterms:location " + getStat(zemeSidlaUchazece) + " ;");
 	            				if (mistoPodnikani != null && !mistoPodnikani.isEmpty() && getStat(mistoPodnikani) != null) ps.println("\tdcterms:location " + getStat(mistoPodnikani) + " ;");
 	            				if (bydliste != null && !bydliste.isEmpty() && getStat(bydliste) != null) ps.println("\tdcterms:location " + getStat(bydliste) + " ;");
 	            				ps.println("\t.\n");
 	            				
 	    	            		ps.println("<" + icoBEprefix + icUchazece + "/identifier> a adms:Identifier ;");
 	    	    	        	ps.println("\tskos:notation \"" + icUchazece + "\" ;");
 	    	    	        	ps.println("\tskos:prefLabel \"" + icUchazece + "\" ;");
 	    	        			ps.println("\tskos:inScheme <http://linked.opendata.cz/resource/concept-scheme/CZ-ICO> ;");
 	    	    	        	ps.println("\tadms:schemeAgency \"Český statistický úřad\" ;");
 	    	    	        	ps.println("\t.");
 	    	    	        	ps.println();
             				}
 	            			
 	            		}
 	            		
 	            		int pocetDodavatelu = zakazka.select("dodavatel").size();
 	            		if (pocetDodavatelu > 1)
             			{
 	            			logger.debug("Více než jeden dodavatel (" + pocetDodavatelu + "): " + uriVZ);
 	            			multiDodavatel++;
             			}
 	            		int currentDodavatel = 1;
 	            		for (Element dodavatel: zakazka.select("dodavatel"))
 	            		{
 	            			if (dodavatel == null) logger.warn("Dodavatel null");
 	            			numdodavatele++;
 	            			String icDodavatele = null;
 	            			if (dodavatel.select("ico") != null) icDodavatele = fixIC(getStringFromElements(dodavatel.select("ico")));
 	            			String nazevDodavatele = null;
 	            			if (dodavatel.select("nazev_dodavatele") != null) nazevDodavatele = getStringFromElements(dodavatel.select("nazev_dodavatele"));
 	            			String zemeSidlaDodavatele = null;
 	            			if (dodavatel.select("zeme_sidla_dodavatele") != null) zemeSidlaDodavatele = getStringFromElements(dodavatel.select("zeme_sidla_dodavatele"));
 	            			String mistoPodnikani = null;
 	            			if (dodavatel.select("misto_podnikani_dodavatele") != null) mistoPodnikani = getStringFromElements(dodavatel.select("misto_podnikani_dodavatele"));
 	            			String cena_s_dph = null;
 	            			String currency_s_dph = null;
 	            			if (dodavatel.select("cena_celkem_dle_smlouvy_dph") != null) {
 	            				String orig = getStringFromElements(dodavatel.select("cena_celkem_dle_smlouvy_dph"));
 	            				cena_s_dph = fixPrice(orig);
 	            				currency_s_dph = getCurrency(orig);
 	            			}
 	            			else if (dodavatel.select("cena_celkem_dle_smlouvy_DPH") != null) {
 	            				String orig = getStringFromElements(dodavatel.select("cena_celkem_dle_smlouvy_DPH"));
 	            				cena_s_dph = fixPrice(orig);
 	            				currency_s_dph = getCurrency(orig);
 	            			}
 	            			String bydliste = null;
 	            			if (dodavatel.select("bydliste_dodavatele") != null) bydliste = getStringFromElements(dodavatel.select("bydliste_dodavatele"));
 	            			String cena_bez_dph = null;
 	            			String currency_bez_dph = null;
 	            			if (dodavatel.select("cena_celkem_dle_smlouvy_bez_dph") != null) {
 	            				String orig = getStringFromElements(dodavatel.select("cena_celkem_dle_smlouvy_bez_dph"));
 	            				cena_bez_dph = fixPrice(orig);
 	            				currency_bez_dph = getCurrency(orig);
 	            			}
 	            			else if (dodavatel.select("cena_celkem_dle_smlouvy_bez_DPH") != null) {
 	            				String orig = getStringFromElements(dodavatel.select("cena_celkem_dle_smlouvy_bez_DPH"));
 	            				cena_bez_dph = fixPrice(orig);
 	            				currency_bez_dph = getCurrency(orig);
 	            			}
 	            			String rozpad = null;
 	            			if (dodavatel.select("rozpad") != null) rozpad = getStringFromElements(dodavatel.select("rozpad"));
 
 
 	            			if (rozpad != null && !rozpad.isEmpty()) logger.debug("Rozpad: " + rozpad);
 	            			
 	            			currentDodavatel++;
 	            			String uriTender;
 	            			if (icDodavatele != null && !icDodavatele.isEmpty())
             				{
 	            				icDodavatele = icDodavatele.replaceAll("[^0-9]", "");
 	            				uriTender = uriVZ + "/awardedTender/" + icDodavatele;
             				}
 	            			else uriTender = uriVZ + "/awardedTender/" + currentDodavatel;
 
 	            			zak_ps.println("<" + uriVZ + "> pc:awardedTender <" + uriTender + "> .\n");
 	            			
 	            			zak_ps.println("<" + uriTender + "> a pc:Tender ;");
 	            			if (icDodavatele != null && !icDodavatele.isEmpty()) zak_ps.println("\tpc:supplier czbe:CZ" + icDodavatele + " ;");
 
 	            			String uriDPHPriceSpec = uriTender + "/priceSpecification/VAT";
 	            			String urinoDPHPriceSpec = uriTender + "/priceSpecification/noVAT";
 
 	            			if (cena_s_dph != null && !cena_s_dph.isEmpty())
             				{
 		            			zak_ps.println("\tpc:offeredPrice <" + uriDPHPriceSpec + "> ;");
             				}
 	            			
 	            			if (cena_bez_dph != null && !cena_bez_dph.isEmpty())
             				{
 		            			zak_ps.println("\tpc:offeredPrice <" + urinoDPHPriceSpec + "> ;");
             				}
 
 	            			if (rozpad != null && !rozpad.isEmpty()) zak_ps.println("pccz:rozpad \"\"\"" + rozpad + "\"\"\" ;");
 	            			
 	            			zak_ps.println("\t.\n");
 		            			
 	            			if (cena_s_dph != null && !cena_s_dph.isEmpty())
 	            				{
 	            				zak_ps.println("<" + uriDPHPriceSpec + "> a gr:PriceSpecification ;");	
 	            				zak_ps.println("\tgr:hasCurrencyValue \"" + cena_s_dph + "\"^^xsd:decimal ;");
 	            				zak_ps.println("\tgr:hasCurrency \"" + currency_s_dph + "\" ;");
 	            				zak_ps.println("\tgr:valueAddedTaxIncluded true ;");
 	            				zak_ps.println("\t.\n");
             				}
 	            			if (cena_bez_dph != null && !cena_bez_dph.isEmpty())
             				{
 	            				zak_ps.println("<" + urinoDPHPriceSpec + "> a gr:PriceSpecification ;");	
 	            				zak_ps.println("\tgr:hasCurrencyValue \"" + cena_bez_dph + "\"^^xsd:decimal ;");
 	            				zak_ps.println("\tgr:hasCurrency \"" + currency_bez_dph + "\" ;");
 	            				zak_ps.println("\tgr:valueAddedTaxIncluded false ;");
 	            				zak_ps.println("\t.\n");
             				}
 	            			
 	            			if (icDodavatele != null && !icDodavatele.isEmpty())
             				{
 	            				ps.println("czbe:CZ" + icDodavatele + " a gr:BusinessEntity ;");
 	            				ps.println("\tadms:identifier <" + icoBEprefix + icDodavatele + "/identifier> ;");
 	            				if (nazevDodavatele != null && !nazevDodavatele.isEmpty()) ps.println("\tdcterms:title \"" + nazevDodavatele + "\" ;");
 	            				if (nazevDodavatele != null && !nazevDodavatele.isEmpty()) ps.println("\tgr:legalName \"" + nazevDodavatele + "\" ;");
 	            				if (zemeSidlaDodavatele != null && !zemeSidlaDodavatele.isEmpty() && getStat(zemeSidlaDodavatele) != null) ps.println("\tdcterms:location " + getStat(zemeSidlaDodavatele) + " ;");
 	            				if (mistoPodnikani != null && !mistoPodnikani.isEmpty() && getStat(mistoPodnikani) != null) ps.println("\tdcterms:location " + getStat(mistoPodnikani) + " ;");
 	            				if (bydliste != null && !bydliste.isEmpty() && getStat(bydliste) != null) ps.println("\tdcterms:location " + getStat(bydliste) + " ;");
 	            				ps.println("\t.\n");
 	            				
 	    	            		ps.println("<" + icoBEprefix + icDodavatele + "/identifier> a adms:Identifier ;");
 	    	    	        	ps.println("\tskos:notation \"" + icDodavatele + "\" ;");
 	    	        			ps.println("\tskos:inScheme <http://linked.opendata.cz/resource/concept-scheme/CZ-ICO> ;");
 	    	    	        	ps.println("\tskos:prefLabel \"" + icDodavatele + "\" ;");
 	    	    	        	ps.println("\tadms:schemeAgency \"Český statistický úřad\" ;");
 	    	    	        	ps.println("\t.");
 	    	    	        	ps.println();
 	            				
             				}
 	            			
 		            		for (Element subdodavatel: dodavatel.select("subdodavatel"))
 		            		{
 		            			numsub++;
 		            			String icSub = null;
 		            			if (subdodavatel.select("ico_sub") != null) icSub = fixIC(getStringFromElements(subdodavatel.select("ico_sub")));
 		            			String nazevSub = null; 
 		            			if (subdodavatel.select("nazev_sub") != null) nazevSub = getStringFromElements(subdodavatel.select("nazev_sub"));
 		            			String zemeSidlaSub = null;
 		            			if (subdodavatel.select("zeme_sidla_sub") != null) zemeSidlaSub = getStringFromElements(subdodavatel.select("zeme_sidla_sub"));
 		            			String mistoPodnikaniSub = null;
 		            			if (subdodavatel.select("misto_podnikani_sub") != null) mistoPodnikaniSub = getStringFromElements(subdodavatel.select("misto_podnikani_sub"));
 		            			String bydlisteSub = null;
 		            			if (subdodavatel.select("bydliste_sub") != null) bydlisteSub = getStringFromElements(subdodavatel.select("bydliste_sub"));
 
 		            			if (icSub != null && !icSub.isEmpty())
 	            				{
 		            				zak_ps.println("<" + uriTender + "> pc:subsupplier czbe:CZ" + icSub + " .\n");
 		            				
 		            				ps.println("czbe:CZ" + icSub + " a gr:BusinessEntity ;");
 		            				ps.println("\tadms:identifier <" + icoBEprefix + icSub + "/identifier> ;");
 		            				if (nazevSub != null && !nazevSub.isEmpty()) ps.println("\tdcterms:title \"" + nazevSub + "\" ;");
 		            				if (nazevSub != null && !nazevSub.isEmpty()) ps.println("\tgr:legalName \"" + nazevSub + "\" ;");
 		            				if (zemeSidlaSub != null && !zemeSidlaSub.isEmpty() && getStat(zemeSidlaSub) != null) ps.println("\tdcterms:location " + getStat(zemeSidlaSub) + " ;");
 		            				if (mistoPodnikaniSub != null && !mistoPodnikaniSub.isEmpty() && getStat(mistoPodnikaniSub) != null) ps.println("\tdcterms:location " + getStat(mistoPodnikaniSub) + " ;");
 		            				if (bydlisteSub != null && !bydlisteSub.isEmpty() && getStat(bydlisteSub) != null) ps.println("\tdcterms:location " + getStat(bydlisteSub) + " ;");
 		            				ps.println("\t.\n");
 		            				
 		    	            		ps.println("<" + icoBEprefix + icSub + "/identifier> a adms:Identifier ;");
 		    	    	        	ps.println("\tskos:notation \"" + icSub + "\" ;");
 		    	    	        	ps.println("\tskos:prefLabel \"" + icSub + "\" ;");
 		    	        			ps.println("\tskos:inScheme <http://linked.opendata.cz/resource/concept-scheme/CZ-ICO> ;");
 		    	    	        	ps.println("\tadms:schemeAgency \"Český statistický úřad\" ;");
 		    	    	        	ps.println("\t.");
 		    	    	        	ps.println();
 
 	            				}
 
 		            		}	            			
 
 	            		}
 	            		zak_ps.println();
 	            	}
 	        	}				
         	}
         }
     }
 }
