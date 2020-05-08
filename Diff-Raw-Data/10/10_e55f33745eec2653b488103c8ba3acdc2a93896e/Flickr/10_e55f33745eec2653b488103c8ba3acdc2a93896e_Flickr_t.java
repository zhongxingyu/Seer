 package world_viewer;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.NodeList;
 
 /**
  * @notes We made the decision to have no tags perform a Parameterless search of
  *        Flickr
  */
 
 public class Flickr {
 
 	private static String api_key = "e7027a5e1ea23330d7d79603cf366e35";
 
 	public Flickr() {
 		// Does nothing
 	}
 
 	/**
 	 * @param tags
 	 *            - the tag(s) to look for in Flickr Database
 	 * @throws IOException
 	 * @throws DOMException
 	 * @throws XPathExpressionException
 	 */
 	public void cache(String tags, int maxPages) throws IOException, XPathExpressionException,
 			DOMException {
 		XPathReader xpath = null;
 		String output = "";
 
 		int page = 1, pages = 1;
 
 		long start = System.currentTimeMillis();
 		String flickResults = flickrSearch(tags, 1);
 		xpath = new XPathReader(flickResults);
 		pages = Integer.parseInt((String) xpath.read("//photos/@pages", XPathConstants.STRING));
 		if (pages > maxPages) {
 			pages = maxPages;
 		}
 		// System.out.println(flickResults);
 
 		while (page <= pages) {
 			output = output + flickResults + "\n";
 			long curr = System.currentTimeMillis();
 			System.out.println(page + " / " + pages + " collected. Elapsed time: " + (curr - start)
 					+ " milliseconds.");
 			page++;
 			if (page <= pages) {
				flickResults = flickrSearch(tags, page);
				long after = System.currentTimeMillis();
				System.out.println("Page # " + page + " took " + (after - curr)
						+ " milliseconds to request and retrieve.");
 			}
 		}
 		long end = System.currentTimeMillis();
 		System.out.println(pages + " pages collected in " + (end - start) + " milliseconds.");
 
 		File cache = new File("cache.txt");
 		if (cache != null) {
 			cache.delete();
 		}
 		cache.createNewFile();
 
 		FileWriter writer = new FileWriter("cache.txt");
 		BufferedWriter out = new BufferedWriter(writer);
 		out.write(output);
 		out.close();
 	}
 
 	public ArrayList<Photo> uncache() throws IOException {
 		ArrayList<Photo> al = new ArrayList<Photo>();
 		File cache = new File("cache.txt");
 		FileReader cacheReader = new FileReader(cache);
 		BufferedReader bufferedCache = new BufferedReader(cacheReader);
 		String page = bufferedCache.readLine();
 		int j = 1;
 		while (page != null) {
 			// System.out.println("Page: " + j);
 			XPathReader xpath = new XPathReader(page);
 			NodeList titles = (NodeList) xpath.read("//photo/@title", XPathConstants.NODESET);
 			NodeList lats = (NodeList) xpath.read("//photo/@latitude", XPathConstants.NODESET);
 			NodeList longs = (NodeList) xpath.read("//photo/@longitude", XPathConstants.NODESET);
 			NodeList urls = (NodeList) xpath.read("//photo/@url_m", XPathConstants.NODESET);
 
 			for (int i = 0; i < urls.getLength(); i++) {
 				double la, lg;
 				la = Double.parseDouble(lats.item(i).getNodeValue());
 				lg = Double.parseDouble(longs.item(i).getNodeValue());
 
 				al.add(new Photo((j - 1)*250 + i, titles.item(i).getNodeValue(), urls.item(i).getNodeValue(), la, lg));
 			}
 			j++;
 			page = bufferedCache.readLine();
 		}
 
 		bufferedCache.close();
 		System.out.println(al.size());
 		return al;
 	}
 
 	/**
 	 * To search for photos by tags
 	 * 
 	 * @param tags
 	 *            - tags to search for
 	 * @return the xml return from {@link #httpGet(String) httpGet(String)}
 	 * @throws IOException
 	 */
 	private static String flickrSearch(String tags, int page) throws IOException {
 		String url = "http://api.flickr.com/services/rest/?method=flickr.photos.search";
 		url += "&api_key=" + api_key;
 		url += "&tags=" + tags;
 		url += "&hasgeo=true";
 		url += "&extras=geo,url_m,title";
 		url += "&per_page=500";
 		url += "&page=" + page;
 		// System.out.println(url);
 		return httpGet(url);
 	}
 
 	/**
 	 * code for making a restful Get request
 	 * 
 	 * @param urlStr
 	 *            - the URL to get a connection too
 	 * @return the responce from the URL from urlStr
 	 * @throws IOException
 	 */
 	private static String httpGet(String urlStr) throws IOException {
 		URL url = new URL(urlStr);
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 
 		if (conn.getResponseCode() != 200) {
 			throw new IOException(conn.getResponseMessage());
 		}
 
 		// Buffer the result into a string
 		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		StringBuilder sb = new StringBuilder();
 		String line;
 		while ((line = rd.readLine()) != null) {
 			sb.append(line);
 		}
 		rd.close();
 
 		conn.disconnect();
 		return sb.toString();
 	}
 }
