 package imdb.parser;
 
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public class ParseHandler {
 	private Document doc;
 	
 	public Title getTitle(String id) throws IllegalArgumentException, IOException {
 		/*if(!id.toLowerCase().startsWith("tt"))
 			id = "tt" + id;*/
 		if(!Pattern.compile("^tt\\d+$", Pattern.CASE_INSENSITIVE).matcher(id).matches()) {
 			throw new IllegalArgumentException(String.format("Bad ID \"%s\" supplied.", id));
 		}
 
 		this.doc = createDoc(id);
 		Title t;
 		TitleParser tp = new TitleParser(doc);
 		if(this.isTVShow()) {
 			TVShowParser tvsp = new TVShowParser(doc);
 			t = new TVShow(id);
 			tp.parse(t);
 			tvsp.parse((TVShow)t);
 		}
 		else if(this.isEpisode()) {
 			EpisodeParser ep = new EpisodeParser(doc);
 			t = new Episode(id);
 			tp.parse(t);
 			ep.parse((Episode)t);
 		}
 		else if(this.isMovie()) {
 			MovieParser mp = new MovieParser(doc);
 			t = new Movie(id);
 			tp.parse(t);
 			mp.parse((Movie)t);
 		}
 		else {
 			throw new IllegalArgumentException(String.format("Bad ID \"%s\" supplied.", id));
 		}
 		return t;
 	}
 	
 	public String getIdByName(String name) throws IOException {
         URL url = new URL(String.format("http://www.imdb.com/find?q=%s&s=tt", URLEncoder.encode(name, "UTF-8")));
         Document doc = createDoc(url);
         Elements els = doc.select(".findResult .result_text a");
         if(els.size() > 0) {
             String titleHref = els.get(0).attr("href");
             Matcher m = Pattern.compile("tt\\d+", Pattern.CASE_INSENSITIVE).matcher(titleHref);
             if(m.find()) {
                 return m.group();
             }
         }
         return null;
 	}
 	
 	//Returns an array containing IMDb IDs for all episodes of the specified season
 	protected static String[] parseSeasonPage(String showId, int season) throws IOException {
 		String[] ret;
 		Document doc = createDoc(showId, season);
 		Elements descs = doc.getElementsByAttributeValue("itemprop", "description");
 		ret = new String[descs.size()];
 		for (int i = 0; i < descs.size(); i++) {
 			String href = descs.get(i).previousElementSibling().child(0).attr("href");
 			Matcher m = Pattern.compile("tt\\d+", Pattern.CASE_INSENSITIVE).matcher(href);
 			m.find();
 			ret[i] = m.group();
 		}
 		
 		return ret;
 		
 	}
 
 	private boolean isEpisode() {
 		boolean ret;
 		Elements es = doc.getElementsByAttributeValue("property", "og:type");
 		if(es.size() > 0) {
 			ret = es.get(0).attr("content").toLowerCase().equals("video.episode");
 		}
 		else {
 			ret = doc.getElementsByClass("tv_header").size() > 0;
 		}
 
 		return ret;
 	}
 	private boolean isMovie() {
 		boolean ret;
 		Elements es = doc.getElementsByAttributeValue("property", "og:type");
 		if(es.size() > 0) {
 			ret = es.get(0).attr("content").toLowerCase().equals("video.movie");
 		}
 		else {
 			ret = !this.isTVShow() && !this.isEpisode() && doc.getElementsByClass("star-box-giga-star").size() > 0;
 		}
 
 		return ret;
 	}
 	private boolean isTVShow() {
 		boolean ret;
 		Elements es = doc.getElementsByAttributeValue("property", "og:type");
 		if(es.size() > 0) {
 			ret = es.get(0).attr("content").toLowerCase().equals("video.tv_show");
 		}
 		else {
 			final String TV_SERIES = "TV Series";
 			String infoBarText = doc.getElementsByClass("infobar").text().trim();
 			ret = infoBarText.startsWith(TV_SERIES);
 		}
 
 		return ret;
 
 	}
 	
 	public String[] getSuggestions(String query) throws Exception {
         ArrayList<String> suggestions = new ArrayList<String>();
 
        //IMDb seems to have a length restriction of 6 on search suggestion querys, not including spaces
 
        final int MAX_QUERY_LENGTH = 6;
        while((query = query.trim()).replace(" ", "").length() > MAX_QUERY_LENGTH) {
            query = query.substring(0, query.length() - 1);
         }
        query = query.replace(' ', '_');
 
        URL url = new URL(String.format("http://sg.media-imdb.com/suggests/%s/%s.json", Character.toLowerCase(query.charAt(0)), URLEncoder.encode(query.toLowerCase(), "UTF-8")));
 
         HttpURLConnection con = (HttpURLConnection)url.openConnection();
 
         con.setRequestProperty("Accept", "*/*");
         con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
 
         if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
 
             BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
             StringBuilder builder = new StringBuilder();
             String line;
             while((line = reader.readLine()) != null) {
                 builder.append(line);
             }
             String json = builder.toString();
             reader.close();
 
             Matcher m = Pattern.compile("\\{.*}").matcher(json);
             if(m.find()) {
                 json = m.group();
 
                 JSONObject jo = new JSONObject(json);
                 JSONArray ja = jo.getJSONArray("d");
 
                 suggestions.ensureCapacity(ja.length());
 
                 for (int i = 0; i < ja.length(); i++) {
                     // ID ja.getJSONObject(i).getString("id")
                     String name = ja.getJSONObject(i).getString("l");
 
                     suggestions.add(name);
                 }
 
             }
         }
         return suggestions.toArray(new String[suggestions.size()]);
 
 	}
 	
 	private static Document createDoc(String id) throws IOException {
 		return createDoc(new URL(String.format("http://imdb.com/title/%s", id)));
 	}
 	private static Document createDoc(String id, int season) throws IOException {
         return createDoc(new URL(String.format("http://imdb.com/title/%s/episodes?season=%s", id, season)));
     }
 
     private static Document createDoc(URL url) throws IOException {
         try {
             return Jsoup.connect(url.toString())
                     .timeout(5000)
                     .ignoreHttpErrors(true)
                     .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.162 Safari/535.19")
                     .get();
 
         } catch (IOException e) {
             throw new IOException("Could not connect. Try again later");
         }
     }
 	
 
 	
 }
 
