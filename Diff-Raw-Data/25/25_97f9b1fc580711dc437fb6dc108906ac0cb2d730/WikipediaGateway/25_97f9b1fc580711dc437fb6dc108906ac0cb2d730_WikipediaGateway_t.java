 package dk.kea.si.movies.gateways;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import dk.kea.si.movies.domain.wiki.SearchSuggestion;
 import dk.kea.si.movies.domain.wiki.SearchSuggestion.Section.Item;
 
 public class WikipediaGateway {
 
 	private static final int LIMIT = 20;
 
 	private static final String ROOT = "http://en.wikipedia.org/w/";
 
 	private static final String LIST_TEMPLATE = ROOT
 			+ "api.php?action=opensearch&search=%s&limit=%s&namespace=0&format=xml";
 
 	private static final String PAGE_TEMPLATE = ROOT
 			+ "index.php?action=render&title=%s";
 
 	public static String getWikiPage(String title, String year) {
 		String result = "";
 		String url = String.format(LIST_TEMPLATE, title, LIMIT);
 		try {
 			JAXBContext jc = JAXBContext.newInstance(SearchSuggestion.class);
 			Unmarshaller u = jc.createUnmarshaller();
 			SearchSuggestion ss = (SearchSuggestion) u.unmarshal(new URL(url));
 			result = filterResults(title, year, ss);
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	private static String filterResults(String title, String year,
 			SearchSuggestion results) {
 		ArrayList<Item> pages = new ArrayList<Item>(LIMIT);
 		List<Item> section = results.getSection().getItem();
 		//System.out.println("section size:"+section.size());
 		if(section.size() == 1) {
 			return retrievePage(section.get(0).getText().getValue());
 		}
 		for (Iterator<Item> iterator = section.iterator(); iterator.hasNext();) {
 			Item item = (Item) iterator.next();
 			//System.out.println("title:"+item.getText().getValue());
 			if (item.getText().getValue().toLowerCase().contains("film")) {
 				pages.add(item);
 			}
 		}
 		if (pages.size() > 1) {
 			for (Iterator<Item> iterator = pages.iterator(); iterator.hasNext();) {
 				Item item = (Item) iterator.next();
 				if (item.getText().getValue().contains(year)) {
 					return retrievePage(item.getText().getValue());
 				}
 			}
 		} else if (pages.size() == 1) {
 			return retrievePage(pages.get(0).getText().getValue());
 		} else {
 			//System.out.println("page size is 0");
 			for (Iterator<Item> iterator = section.iterator(); iterator.hasNext();) {
 				Item item = (Item) iterator.next();
 				//System.out.println(item.getText().getValue());
 				if(item.getText().getValue().toLowerCase().equals(title.toLowerCase())) {
 					return retrievePage(item.getText().getValue());
 				}
 			}
 		}
 		return "";
 	}
 
 	private static String retrievePage(String title) {
 		String url;
 		try {
 			url = String.format(PAGE_TEMPLATE,
 					URLEncoder.encode(title, "UTF-8"));
 			return readUrlContents(url);
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	private static String readUrlContents(String url) throws IOException,
 			MalformedURLException {
 		Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(
 				new URL(url).openStream())));
 		String content = "";
 		while (scanner.hasNextLine()) {
 			content += scanner.nextLine();
 		}
 		return content;
 	}
}
