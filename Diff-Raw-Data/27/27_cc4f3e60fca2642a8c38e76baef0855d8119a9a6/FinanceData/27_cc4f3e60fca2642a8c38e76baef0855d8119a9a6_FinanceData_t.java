 package si.fri.iskalnikOseb.dataHelpers;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
 
 public class FinanceData {
 	/**
 	public static void main(String[] args) throws Exception {
 		
 		String[] clanki = getClankeOsebe("Janez Jan�a");
 		for (String clanek : clanki)
 			System.out.println(clanek);
 		/**
 		String line = "<a class=\"nitem\" title=\"Janez Jan�a\" href=\"/leksikon/50/Janez-Jan%C%Aa\">";
 		String pattern = "e=\"(\\p{L}+)(\\s(\\p{L})+)*\"";
 
 		// Create a Pattern object
 		Pattern r = Pattern.compile(pattern);
 
 		// Now create matcher object.
 		Matcher m = r.matcher(line);
 		if (m.find()) {
 			String zadetek = m.group(0);
 			String zadetek1 = zadetek.substring(3, zadetek.length() - 1);
 			String[] razdeli = line.split(">");
 			String rezultat = razdeli[0].concat(">").concat(zadetek1)
 					.concat("</a>");
 			System.out.println(rezultat);
 		} else {
 			System.out.println("NO MATCH");
 		}
 		
 	}*/
 
 	private static String pretvori(String line) {
 		String pattern = "e=\"(\\p{L}+)((\\s|-)(\\p{L})+)*\"";
 		// Izdelaj Pattern objekt
 		Pattern r = Pattern.compile(pattern);
 
 		// Izdelaj Matcher objekt
 		Matcher m = r.matcher(line);
 		if (m.find()) {
 			String zadetek = m.group(0);
 			String zadetek1 = zadetek.substring(3, zadetek.length() - 1);
 			String[] razdeli = line.split(">");
 			String rezultat = razdeli[0].concat(">").concat(zadetek1)
 					.concat("</a>");
 			return zadetek1;
 		}
 		return "";
 	}
 
 	/**
 	 * Vrne v obliki <a href="povezava do clanka">Ime clanka</a>
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public static ArrayList<FinanceStructure> getPovezaveOsebe(String name) throws Exception {
 		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_10);
 		webClient.getOptions().setJavaScriptEnabled(false);
 		// Get the first page
 		final HtmlPage page1 = webClient
 				.getPage("http://www.finance.si/leksikon/");
 		// Get the form that we are dealing with and within that form,
 		// find the submit button and the field that we want to change.
 		final HtmlTextInput nameField = page1
 				.getFirstByXPath("//*[@id=\"maincolumn\"]/table/tbody/tr/td/table/tbody/tr[2]/td/form/center/input[1]");
 		nameField.setValueAttribute(name);
 		final HtmlElement button = page1
 				.getFirstByXPath("//*[@id=\"maincolumn\"]/table/tbody/tr/td/table/tbody/tr[2]/td/form/center/input[2]");
 		final HtmlPage page2 = button.click();
 		List<HtmlElement> povezavaNaIme = (List<HtmlElement>) page2
 				.getByXPath("//*[@id=\"maincolumn\"]/p/ul/li[1]/a");
 		HtmlPage page3= null;
 		if (povezavaNaIme.size() != 0)
 			page3 = povezavaNaIme.get(0).click();
 		else 
 			page3 = page2;
 		// zdj smo na seznamu clankov
 		//treba klikniti ve�
 		final HtmlElement vecButton = page3
 				.getFirstByXPath("//*[@id=\"maincolumn\"]/table[1]/tbody/tr[2]/td/h3[1]/a");
 		if (vecButton== null)
 			return null;
 		HtmlPage page4 = vecButton.click();
 	//	HtmlElement elm = page4.getFirstByXPath("//*[@id=\"maincolumn\"]/p/dl/dd/a[2]");
 		ArrayList<Povezava> skupnePovezave = pridobiPovezave(page4);	
 		for (int j=0; j<5; j++) {
 			final List<HtmlElement> starejsiButton = (List<HtmlElement>) page4.
 					
 					getByXPath("//*[@id=\"maincolumn\"]/p/table/tbody/tr/td/table/tbody/tr/td/a[2]");
 			if (starejsiButton.size()!=0) {
 				page4 = starejsiButton.get(0).click();
 				skupnePovezave.addAll(pridobiPovezave(page4));
 			}
 			else 
 				break;
 		}
 
 		
 		
 		// add elements to al, including duplicates
 		
 		ArrayList<FinanceStructure> fs = new ArrayList<FinanceStructure>();	    
 		for (Povezava povezava : skupnePovezave) {
 			if (FinanceStructure.getFinanceStructure(povezava.imePovezave)!= null){
 				FinanceStructure.getFinanceStructure(povezava.imePovezave).addClanek(povezava.imeClanka);
 			}
 			else {
 				FinanceStructure structure = new FinanceStructure(povezava.imePovezave, povezava.imeClanka);
 				fs.add(structure);
 			}
 				
 				
 		}
 		Collections.sort(fs, new ValueComparator());
 		return fs;
 	}
 	
 	private static ArrayList<Povezava> pridobiPovezave(HtmlPage page) throws Exception{
 		// dobi linke od clankov:
 	//	List<HtmlElement> linkiClankov = (List<HtmlElement>) page
 		//		.getByXPath("//*[@id=\"maincolumn\"]/table[1]/tbody/tr[2]/td/table[1]/tbody/tr/td[2]/a");
		@SuppressWarnings("unchecked")
 		List<HtmlElement> linkiClankov = (List<HtmlElement>) page
 						.getByXPath("//*[@id=\"maincolumn\"]/p/dl/dd/a[2]");
 		ArrayList<Povezava> skupnePovezave = new ArrayList<Povezava>();
 		for (int i = 0; i < linkiClankov.size(); i++) {
 			HtmlElement linkClanka = linkiClankov.get(i);
 			HtmlPage page4 = linkClanka.click();
 
 			// pridobimo linke do firm + oseb
 			List<HtmlElement> linkiFirmOseb = (List<HtmlElement>) page4
 					.getByXPath("//*[@class=\"nitem\" and @title]");
 
 			// List<HtmlElement> linkiOseb =
 			// (List<HtmlElement>)page4.getByXPath("//a[@class=\"nitem\" and contains(@href, \"leksikon\")]");
 			// List<HtmlElement> linkiFirm =
 			// (List<HtmlElement>)page4.getByXPath("//a[@class=\"nitem\" and contains(@href, \"minifipo\")]");
 			for (int j = 0; j < linkiFirmOseb.size(); j++) {
 				String imePovezave = pretvori(linkiFirmOseb.get(j).asXml());
 				skupnePovezave.add(new Povezava(imePovezave, linkClanka.asXml()));
 			}
 		}
 		return skupnePovezave;
 	}
 	
 	/**
 	 * Vrne v obliki <a href="povezava do clanka">Ime clanka</a>
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public static String[] getClankeOsebe(String name) throws Exception {
 		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_10);
 		webClient.getOptions().setJavaScriptEnabled(false);
 		// Get the first page
 		final HtmlPage page1 = webClient
 				.getPage("http://www.finance.si/leksikon/");
 		// Get the form that we are dealing with and within that form,
 		// find the submit button and the field that we want to change.
 		final HtmlTextInput nameField = page1
 				.getFirstByXPath("//*[@id=\"maincolumn\"]/table/tbody/tr/td/table/tbody/tr[2]/td/form/center/input[1]");
 		nameField.setValueAttribute(name);
 		final HtmlElement button = page1
 				.getFirstByXPath("//*[@id=\"maincolumn\"]/table/tbody/tr/td/table/tbody/tr[2]/td/form/center/input[2]");
 		final HtmlPage page2 = button.click();
 		List<HtmlElement> povezavaNaIme = (List<HtmlElement>) page2
 				.getByXPath("//*[@id=\"maincolumn\"]/p/ul/li[1]/a");
 		HtmlPage page3 = null;
 		if (povezavaNaIme.size() != 0)
 			page3 = povezavaNaIme.get(0).click();
 		else 
 			page3 = page2;
 		// zdj smo na seznamu clankov
 
 		// dobi linke od clankov:
 		List<HtmlElement> linkiClankov = (List<HtmlElement>) page3
 				.getByXPath("//*[@id=\"maincolumn\"]/table[1]/tbody/tr[2]/td/table[1]/tbody/tr/td[2]/a");
 		String[] vsePovezave = new String[linkiClankov.size()];
 
 		for (int i = 0; i < vsePovezave.length; i++) {
 			vsePovezave[i] = linkiClankov.get(i).asXml().replace("href=\"", "href=\"http://finance.si");
 		}
 
 		// return page4.asXml();
 		return vsePovezave;
 	}
 	
 	
 	
 }
 class ValueComparator implements Comparator<FinanceStructure> {
 
 		@Override
 		public int compare(FinanceStructure a, FinanceStructure b) {
 				return b.getStClankov() - a.getStClankov();
 			
 		}
}
 
 class Povezava {
 	String imePovezave;
 	String imeClanka;
 	
 	public Povezava(String imePovezave, String imeClanka){
 		this.imePovezave = imePovezave;
 		this.imeClanka = imeClanka;
 	}
 }
