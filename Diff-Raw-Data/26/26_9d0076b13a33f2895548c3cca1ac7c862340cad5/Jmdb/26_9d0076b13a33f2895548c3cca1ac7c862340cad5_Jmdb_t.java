 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package sk.mortality.util.imdb;
 
 import HTTPClient.CookieModule;
 import HTTPClient.HTTPConnection;
 import HTTPClient.HTTPResponse;
 import HTTPClient.ModuleException;
 import HTTPClient.NVPair;
 import HTTPClient.ParseException;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author char0n
  */
 public class Jmdb {
 
     protected Criteria criteria;
     protected static final Logger log = Logger.getLogger(Jmdb.class);
 
     protected Integer movieID;
     protected String query;
     protected Map<Integer, String> matched;
     protected Status status;
     protected String response;
 
     protected String title;
     protected String year;
     protected String plot;
     protected String fullPlot;
     protected Set<Actor> cast;
     protected Set<Director> directors;
     protected Set<Writer> writers;
     protected String cover;
     protected byte[] coverData;
     protected String[] language;
     protected String[] country;
     protected Rating rating;
     protected String[] genre;
     protected String tagline;
     protected String[] aka;
     protected Set<Certificate> certifications;
     protected String runtime;
     protected String trivia;
     protected String goofs;
     protected String awards;
     protected Set<OfficialSite> officialSites;
 
     public Jmdb() {
         this.initDefaults();
     }
 
     public Jmdb(Criteria c) {
         this.initDefaults();
         this.criteria.append(c);
     }
 
     protected void initDefaults() {
         log.debug("Initializing default criteria");
         this.criteria = new Criteria();
         this.criteria.put("host"         , "http://www.imdb.com/");
         this.criteria.put("user_agent"   , "Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.7.12) Gecko/20050915 Firefox/1.0.7");
         this.criteria.put("referer"      , "http://www.imdb.com/");
         this.criteria.put("movieID_query", "title/tt");
         this.criteria.put("search_query" , "find?tt=on;mx=20;q=");
         this.criteria.put("query_str_enc", "UTF-8");
         this.criteria.put("timeout"      , "1000");
         this.criteria.put("auto_parse"   , "true");
     }
 
     public void search(int movieID) throws JmdbException {
         log.debug("MovieID search commenced");
 
         this.clean();        
         this.response = this.getHttpResponse(this.criteria.get("host")+this.criteria.get("movieID_query")+movieID);
         if (this.response.length() > 0 && this.response.indexOf("The URL (page) you requested could not be found.") == -1) {
             this.status   = Status.OK;
             this.movieID  = movieID;
         } else {
             log.debug("Received zero length response");
             this.status = Status.KO;
         }
     }
 
     public void search(String query) throws JmdbException {
         log.debug("Query search commenced");
         this.query = query;
 
         this.matched = this.getTitleMatches(query);
         log.debug(String.valueOf(this.matched.size())+" matches found");
 
         // No results found
         if (this.matched.size() == 0) {
             this.status = Status.KO;
             log.debug("No matches found");
             return;
         }
 
         // Fetch first result
         this.status = Status.OK;
         int imdbID = 0;
         // Don't fetch first result if auto_parse disabled
         if (!this.criteria.get("auto_parse").equals("true")) {
             return;
         }
         log.debug("Using first match as movie title");
         for (Integer key : this.matched.keySet()) {
             imdbID = key;
             break;
         }
         this.search(imdbID);
     }
 
     public Status getStatus() {
         return this.status;
     }
 
     public Map<Integer, String> getMatchedTitles() {
         return this.matched;
     }
 
     public int getMovieID() {
         return this.movieID;
     }
 
     public String getTitle() {
         if (this.title == null) {
             Map<String, String> titleResult = Parser.parseTitle(this.response);
             this.title      = titleResult.get("title");
             this.year       = titleResult.get("year");
         }
         return this.title;
     }
 
     public String getYear() {
         if (this.year == null) {
             this.getTitle();
         }
         return this.year;
     }
 
     public String getPlot() {
         if (this.plot == null) {
             this.plot = Parser.parsePlot(this.response);
         }
         return this.plot;
     }
 
     public Set<Actor> getCast() {
         if (this.cast == null) {
             this.cast = Parser.parseCast(this.response);
         }
         return this.cast;
     }
 
     public Set<Director> getDirectors() {
         if (this.directors == null) {
             this.directors = Parser.parseDirectors(this.response);
         }
         return this.directors;
     }
 
     public Set<Writer> getWriters() {
         if (this.writers == null) {
             this.writers = Parser.parseWriters(this.response);
         }
         return this.writers;
     }
 
     public String getCover() {
         if (this.cover == null) {
             this.cover = Parser.parseCover(this.response);
         }
         return this.cover;
     }
 
     public byte[] getCoverData() throws JmdbException {
         byte[] data = null;
         // parse cover URI
         this.getCover();
 
         // no cover awaillable
         if (this.cover == null) {
             return data;
         }
 
        HTTPConnection connection = null;
         try {
             URL requestURL  = new URL(this.cover);
             String host     = requestURL.getHost();
             String file     = requestURL.getFile();
             int port        = requestURL.getPort();
             port            = (port == -1) ? 80 : port;
 
             log.debug("Getting HTTP response for cover image: "+requestURL.toString());
            connection = new HTTPConnection(host, port);
             connection.setTimeout(Integer.parseInt(this.criteria.get("timeout")));
             connection.setDefaultHeaders(new NVPair[] {new NVPair("User-Agent", this.criteria.get("user_agent")), new NVPair("Referer", this.criteria.get("referer"))});
             connection.setAllowUserInteraction(false);
             connection.removeModule(CookieModule.class);
             HTTPResponse resp = connection.Get(file);
             data = resp.getData();
         } catch (MalformedURLException ex) {
             throw new JmdbException("URL supplied for IMDB request is not valid URL", ex);
         } catch (IOException ex) {
             throw new JmdbException("Error while loading HTTP response", ex);
         } catch (ModuleException ex) {
             throw new JmdbException("Error while loading modules in HTTPClient", ex);
        } finally {
            if (connection != null) connection.stop();
        }
 
         return data;
     }
 
     public String[] getLanguage() {
         if (this.language == null) {
             this.language = Parser.parseLanguage(this.response);
         }
         return this.language;
     }
 
     public String[] getCountry() {
         if (this.country == null) {
             this.country = Parser.parseCountry(this.response);
         }
         return this.country;
     }
 
     public Rating getRating() {
         if (this.rating == null) {
             this.rating = Parser.parseRating(this.response);
         }
         return this.rating;
     }
 
     public String[] getGenre() {
         if (this.genre == null) {
             this.genre = Parser.parseGenre(this.response);
         }
         return this.genre;
     }
 
     public String getTagline() {
         if (this.tagline == null) {
             this.tagline = Parser.parseTagline(this.response);
         }
         return this.tagline;
     }
 
     public String[] getAka() {
         if (this.aka == null) {
             this.aka = Parser.parseAka(this.response);
         }
         return this.aka;
     }
 
     public Set<Certificate> getCertifications() {
         if (this.certifications == null) {
             this.certifications = Parser.parseCertifications(this.response);
         }
         return this.certifications;
     }
 
     public String getRuntime() {
         if (this.runtime == null) {
             this.runtime = Parser.parseRuntime(this.response);
         }
         return this.runtime;
     }
 
     public String getFullPlot() throws JmdbException {
         if (this.fullPlot == null) {
             String url      = this.criteria.get("host")+this.criteria.get("movieID_query")+this.movieID+"/plotsummary";
             String resp     = this.getHttpResponse(url);
             this.fullPlot   = Parser.parseFullPlot(resp);
         }
         return this.fullPlot;
     }
 
     public String getTrivia() {
         if (this.trivia == null) {
             this.trivia = Parser.parseTrivia(this.response);
         }
         return this.trivia;
     }
 
     public String getGoofs() {
         if (this.goofs == null) {
             this.goofs = Parser.parseGoofs(this.response);
         }
         return this.goofs;
     }
 
     public String getAwards() {
         if (this.awards == null) {
             this.awards = Parser.parseAwards(this.response);
         }
         return this.awards;
     }
 
     public Set<OfficialSite> getOfficialSites() throws JmdbException {
         if (this.officialSites == null) {
             String url         = this.criteria.get("host")+this.criteria.get("movieID_query")+this.movieID+"/officialsites";
             String resp        = this.getHttpResponse(url);
             this.officialSites = Parser.parseOfficialSites(resp);
         }
         return this.officialSites;
     }
 
     protected Map<Integer, String> getTitleMatches(String query) throws JmdbException {
         log.debug("Getting possible IMDB title matches");
         Map<Integer, String> matches = null;
         String requestURL;
         String resp;
         int imdbId;
         String imdbTitle;
 
         try {
             requestURL = this.criteria.get("host") + this.criteria.get("search_query") + URLEncoder.encode(query, this.criteria.get("query_str_enc"));
         } catch (UnsupportedEncodingException ex) {
             requestURL = this.criteria.get("host") + this.criteria.get("search_query") + query;
         }
 
         resp = this.getHttpResponse(requestURL);
         if (resp.indexOf("<title>IMDb Title Search</title>") != -1) {
             log.debug("Search page reached. Getting list of possible matches");
             matches  = Parser.parseTitles(resp);
         } else {
             log.debug("Direct title match reached");
             imdbId    = Parser.parseImdbId(resp);
             imdbTitle = Parser.parseTitle(resp).get("title");
             matches  = new HashMap<Integer, String>();
             matches.put(imdbId, imdbTitle);
         }
 
         return matches;
     }
 
     protected String getHttpResponse(String url) throws JmdbException {
        String content            = null;
        HTTPConnection connection = null;
 
         try {
             URL requestURL  = new URL(url);
             String host     = requestURL.getHost();
             String file     = requestURL.getFile();
             int port        = requestURL.getPort();
             port            = (port == -1) ? 80 : port;
 
             log.debug("Getting HTTP response for: "+url);
            connection = new HTTPConnection(host, port);
             connection.setTimeout(Integer.parseInt(this.criteria.get("timeout")));
             connection.setDefaultHeaders(new NVPair[] {new NVPair("User-Agent", this.criteria.get("user_agent")), new NVPair("Referer", this.criteria.get("referer"))});
             connection.setAllowUserInteraction(false);
             connection.removeModule(CookieModule.class);
             HTTPResponse resp = connection.Get(file);
             content        = resp.getText();
         } catch (MalformedURLException ex) {
             throw new JmdbException("URL supplied for IMDB request is not valid URL", ex);
         } catch (IOException ex) {
             throw new JmdbException("Error while loading HTTP response", ex);
         } catch (ModuleException ex) {
             throw new JmdbException("Error while loading modules in HTTPClient", ex);
         } catch (ParseException ex) {
             throw new JmdbException("Error while parsing HTTP response", ex);
        } finally {
            if (connection != null) connection.stop();
         }
 
         return content;
     }
 
     protected void clean() {
         this.movieID        = null;
         this.query          = null;
         this.response       = null;
         this.title          = null;
         this.year           = null;
         this.plot           = null;
         this.fullPlot       = null;
         this.cast           = null;
         this.directors      = null;
         this.writers        = null;
         this.cover          = null;
         this.language       = null;
         this.country        = null;
         this.rating         = null;
         this.genre          = null;
         this.tagline        = null;
         this.aka            = null;
         this.certifications = null;
         this.runtime        = null;
         this.fullPlot       = null;
         this.trivia         = null;
         this.goofs          = null;
         this.awards         = null;
         this.officialSites  = null;
     }
 
     /**
      * Jmdb Status
      */
     public static enum Status {
         OK, KO;
     }
 
     /**
      * Jmdb Criteria holder
      */
     public static class Criteria {
         private Map<String, String> crit = new HashMap<String, String>();
 
         public Criteria() { }
 
         public Criteria(Map<String, String> crit) {
             this.crit = crit;
         }
 
         public void put(String key, String value) {
             this.crit.put(key, value);
         }
 
         public String get(String key) {
             return this.crit.get(key);
         }
 
         public Map<String, String> export() {
             return this.crit;
         }
 
         public void append(Criteria c) {
             Map<String, String> crits = c.export();
             for (String key : crits.keySet()) {
                 this.crit.put(key, crits.get(key));
             }
         }
     }
 
     /**
      * IMDB parsers
      */
     private static class Parser {
 
         private static Pattern imdbIdPattern        = Pattern.compile("<link rel=\"canonical\" href=\"http://www.imdb.com/title/tt([0-9]+)/\" />");
         private static Pattern titlesPattern        = Pattern.compile("title/tt([0-9]{7})/';\">([^<]+)?</a> \\(([0-9]+)\\)");
         private static Pattern titlePattern         = Pattern.compile("<title>(.+)[ ]\\(([0-9]{4})\\)([^<]+)?</title>");
         private static Pattern plotPattern          = Pattern.compile("<h5>Plot:</h5>\\s(.+?)\\s<a class=\"tn15more inline\"");
         private static Pattern fullPlotPattern      = Pattern.compile("<p class=\"plotpar\">([^<]+)<i>");
         private static Pattern castPattern          = Pattern.compile("<a href=\"/name/nm([0-9]{7})/\" onclick=\"[^\"]+\">([^<]+)</a></td><td class=\"ddd\"> ... </td><td class=\"char\">(<a href=\"/character/ch[0-9]{7}/\">)?([^<]+)");
         private static Pattern directorPattern      = Pattern.compile("/rg/directorlist/position\\-[0-9]+/images/b.gif\\?link=name/nm([0-9]{7})/';\">([^<]+)");
         private static Pattern writerPattern        = Pattern.compile("/rg/writerlist/position\\-[0-9]+/images/b.gif\\?link=name/nm([0-9]{7})/';\">([^<]+)");
         private static Pattern coverPattern         = Pattern.compile("<a name=\"poster\"[^>]+><img border=\"0\"[^>]+src=\"([^\"]+)\" /></a>");
         private static Pattern languagePattern      = Pattern.compile("<a href=\"/Sections/Languages/[^/]+/\">([^<]+)");
         private static Pattern countryPattern       = Pattern.compile("<a href=\"/Sections/Countries/[^/]+/\">([^<]+)");
         private static Pattern ratingPattern        = Pattern.compile("<b>([0-9]{1}\\.[0-9]{1})/10</b>\\s+&nbsp;&nbsp;<a href=\"ratings\" class=\"tn15more\">([^\\s]+) votes</a>");
         private static Pattern genrePattern         = Pattern.compile("<a href=\"/Sections/Genres/[^/]+/\">([^<]+)");
         private static Pattern taglinePattern       = Pattern.compile("<h5>Tagline:</h5>\\s([^<]+)");
         private static Pattern akaPattern           = Pattern.compile("<h5>Also Known As:</h5>(.+)?<br>");
         private static Pattern certificationPattern = Pattern.compile("<a href=\"/List\\?certificates=[^\\&]+\\&\\&heading=[0-9]+;[^\"]+\">\\s([^:]+):([^<]+)</a>");
         private static Pattern runtimePattern       = Pattern.compile("<h5>Runtime:</h5>\\s([^<]+)\\s</div>");
         private static Pattern triviaPattern        = Pattern.compile("<h5>Trivia:</h5>\\s(.+?)\\s<a class=\"tn15more inline\"");
         private static Pattern goofsPattern         = Pattern.compile("<h5>Goofs:</h5>\\s(.+?)\\s<a class=\"tn15more inline\"");
         private static Pattern awardsPattern        = Pattern.compile("<h5>Awards:</h5>([^<]+)", Pattern.MULTILINE);
         private static Pattern officialSitesPattern = Pattern.compile("<li><a href=\"([^\"]+)\">([^<]+)</a></li>");
 
         public static int parseImdbId(String data) {
             Jmdb.log.debug("Parsing Imdb ID");
             int imdbId = 0;
             Matcher matcher = imdbIdPattern.matcher(data);
             if (matcher.find()) {
                 imdbId = Integer.parseInt(matcher.group(1));
             }
             return imdbId;
         }
 
         public static Map<Integer, String> parseTitles(String data) {
             Jmdb.log.debug("Parsing possible title matches");
             Map<Integer, String> matches = new LinkedHashMap<Integer, String>();
             Matcher matcher = titlesPattern.matcher(data);
 
             while (matcher.find()) {
                 if (!matches.containsKey(matcher.group(1))) {
                     matches.put(Integer.parseInt(matcher.group(1)), htmlEntityDecode(matcher.group(2)));
                 }
             }
             return matches;
         }
 
         public static Map<String, String> parseTitle(String data) {
             Jmdb.log.debug("Parsing movie title and year");
             Map<String, String> title = new HashMap<String, String>();
             Matcher matcher = titlePattern.matcher(data);
 
             if (matcher.find()) {
                 title.put("title", htmlEntityDecode(matcher.group(1).trim()));
                 title.put("year" , matcher.group(2).trim());
             }
 
             return title;
         }
 
         public static String parsePlot(String data) {
             Jmdb.log.debug("Parsing movie plot");
             String plot = null;
             Matcher matcher = plotPattern.matcher(data);
 
             if (matcher.find()) {
                 plot = htmlEntityDecode(matcher.group(1).replaceAll("<[^>]+>", "").trim());
             }
 
             return plot;
         }
 
         public static Set<Actor> parseCast(String data) {
             Jmdb.log.debug("Parsing cast");
             Set<Actor> cast = new HashSet<Actor>();
             Matcher matcher = castPattern.matcher(data);
             Actor actor;
 
             while (matcher.find()) {
                 actor = new Actor(Integer.parseInt(matcher.group(1)), htmlEntityDecode(matcher.group(2).trim()), htmlEntityDecode(matcher.group(4).trim()));
                 cast.add(actor);
             }
             return cast;
         }
 
         public static Set<Director> parseDirectors(String data) {
             Jmdb.log.debug("Parsing directors");
             Set<Director> directors = new HashSet<Director>();
             Matcher matcher         = directorPattern.matcher(data);
             Director director;
 
             while (matcher.find()) {
                 director = new Director(Integer.parseInt(matcher.group(1)), htmlEntityDecode(matcher.group(2).trim()));
                 directors.add(director);
             }
             return directors;
         }
 
         public static Set<Writer> parseWriters(String data) {
             Jmdb.log.debug("Parsing writers");
             Set<Writer> writers = new HashSet<Writer>();
             Matcher matcher     = writerPattern.matcher(data);
             Writer writer;
 
             while (matcher.find()) {
                 writer = new Writer(Integer.parseInt(matcher.group(1)), htmlEntityDecode(matcher.group(2).trim()));
                 writers.add(writer);
             }
             return writers;
         }
 
         public static String parseCover(String data) {
             Jmdb.log.debug("Parsing cover image url");
             data = htmlEntityDecode(data);
             Matcher matcher = coverPattern.matcher(data);
             String cover    = null;
 
             if (matcher.find()) {
                 cover = matcher.group(1).trim();
             }
             return cover;
         }
 
         public static String[] parseLanguage(String data) {
             Jmdb.log.debug("Parsing movie languages");
             Matcher matcher       = languagePattern.matcher(data);
             Set<String> language  = new HashSet<String>();
             while (matcher.find()) {
                 language.add(htmlEntityDecode(matcher.group(1).trim()));
             }
             return language.toArray(new String[language.size()]);
         }
 
         public static String[] parseCountry(String data) {
             Jmdb.log.debug("Parsing movie countries");
             Matcher matcher     = countryPattern.matcher(data);
             Set<String> country = new HashSet<String>();
             while (matcher.find()) {
                 country.add(htmlEntityDecode(matcher.group(1).trim()));
             }
             return country.toArray(new String[country.size()]);
         }
 
         public static Rating parseRating(String data) {
             Jmdb.log.debug("Parsing movie rating and votes");
             Matcher matcher  = ratingPattern.matcher(data);
             Rating r = null;
 
             if (matcher.find()) {
                 r = new Rating(Float.parseFloat(matcher.group(1).trim()), Integer.parseInt(matcher.group(2).trim().replace(",", "")));
             }
             return r;
         }
 
         public static String[] parseGenre(String data) {
             Jmdb.log.debug("Parsing movie genres");
             Matcher matcher   = genrePattern.matcher(data);
             Set<String> genre = new HashSet<String>();
             while (matcher.find()) {
                 genre.add(htmlEntityDecode(matcher.group(1).trim()));
             }
             return genre.toArray(new String[genre.size()]);
         }
 
         public static String parseTagline(String data) {
             Jmdb.log.debug("Parsing movie tagline");
             Matcher matcher = taglinePattern.matcher(data);
             String tagline = null;
             if (matcher.find()) {
                 tagline = htmlEntityDecode(matcher.group(1).trim());
             }
             return tagline;
         }
 
         public static String[] parseAka(String data) {
             Jmdb.log.debug("Parsing movie AKAs");
             Matcher matcher = akaPattern.matcher(data);
             String akas;
             String[] aka = null;
             if (matcher.find()) {
                 akas = htmlEntityDecode(matcher.group(1).trim());
                 akas = akas.replaceAll("\\([^\\)]+\\)", ""); // Replace everything within ()
                 if (akas.indexOf("<br>") != -1) {
                     aka = akas.split("<br>");
                 } else {
                     aka    = new String[1];
                     aka[0] = akas;
                 }
             }
             return aka;
         }
 
         public static Set<Certificate> parseCertifications(String data) {
             Jmdb.log.debug("Parsing movie certifications");
             Matcher matcher = certificationPattern.matcher(data);
             Set<Certificate> certifications = new HashSet<Certificate>();
             Certificate cert;
             while (matcher.find()) {
                 cert = new Certificate(htmlEntityDecode(matcher.group(1).trim()), htmlEntityDecode(matcher.group(2)).trim());
                 certifications.add(cert);
             }
             return certifications;
         }
 
         public static String parseRuntime(String data) {
             Jmdb.log.debug("Parsing movie runtime");
             Matcher matcher = runtimePattern.matcher(data);
             String runtime = null;
             if (matcher.find()) {
                 runtime = htmlEntityDecode(matcher.group(1).trim().replaceAll("\\s+", " "));
             }
             return runtime;
         }
 
         public static String parseFullPlot(String data) {
             Jmdb.log.debug("Parsing movie full plot");
             Matcher matcher = fullPlotPattern.matcher(data);
             String plot = null;
             if (matcher.find()) {
                 plot = htmlEntityDecode(matcher.group(1).replaceAll("<[^>]+>", "").trim());
             }
             return plot;
         }
 
         public static String parseTrivia(String data) {
             Jmdb.log.debug("Parsing movie trivia");
             Matcher matcher = triviaPattern.matcher(data);
             String trivia = null;
             if (matcher.find()) {
                 trivia = htmlEntityDecode(matcher.group(1).trim().replaceAll("<[^>]+>", ""));
             }
             return trivia;
         }
 
         public static String parseGoofs(String data) {
             Jmdb.log.debug("Parsing movie goofs");
             Matcher matcher = goofsPattern.matcher(data);
             String goofs = null;
             if (matcher.find()) {
                 goofs = htmlEntityDecode(matcher.group(1).trim().replaceAll("<[^>]+>", ""));
             }
             return goofs;
         }
 
         public static String parseAwards(String data) {
             Jmdb.log.debug("Parsing movie awards");
             Matcher matcher = awardsPattern.matcher(data);
             String awards = null;
             if (matcher.find()) {
                 awards = matcher.group(1).trim().replaceAll("\\s+", " ");
                 awards = htmlEntityDecode(awards);
             }
             return awards;
         }
 
         public static Set<OfficialSite> parseOfficialSites(String data) {
             Jmdb.log.debug("Parsing movie official sites");
             Matcher matcher         = officialSitesPattern.matcher(data);
             Set<OfficialSite> sites = new HashSet<OfficialSite>();
             OfficialSite site;
             while (matcher.find()) {
                 site = new OfficialSite(matcher.group(1).trim(), htmlEntityDecode(matcher.group(2).trim()));
                 sites.add(site);
             }
             return sites;
         }
 
         private static String htmlEntityDecode(String data) {
 
             // HTML entity decoding
             data = data.replaceAll("\\&amp;", "&");
 
             // Nothing to decode
             if (data.indexOf("&#") == -1) {
                 return data;
             }
 
             // Decoding 16 base
             StringBuffer decoded = new StringBuffer();
             int fromIndex        = 0;
             int start            = 0;
             int end              = 0;
 
             while ((start = data.indexOf("&#x", fromIndex)) != -1) {
                 fromIndex = start;
                 for (int i = start; i < data.length(); i++) {
                     if (data.charAt(i) == ';') {
                         end = i;
                         break;
                     }
                 }
                 decoded.append(data.substring(0, start));
                 decoded.append((char) Integer.parseInt(data.substring(start + 3, end), 16));
                 decoded.append(data.substring(end + 1));
                 data = decoded.toString();
 
                 start = 0;
                 end   = 0;
                 decoded.delete(0, decoded.length());
             }
 
             // Decoding 10 base
             fromIndex        = 0;
             start            = 0;
             end              = 0;
             decoded.delete(0, decoded.length());
             while ((start = data.indexOf("&#", fromIndex)) != -1) {
                 fromIndex = start;
                 for (int i = start; i < data.length(); i++) {
                     if (data.charAt(i) == ';') {
                         end = i;
                         break;
                     }
                 }
                 decoded.append(data.substring(0, start));
                 decoded.append((char) Integer.parseInt(data.substring(start + 2, end), 10));
                 decoded.append(data.substring(end + 1));
                 data = decoded.toString();
 
                 start = 0;
                 end   = 0;
                 decoded.delete(0, decoded.length());
             }
 
             return data;
         }
     }
 
     /**
      * Imdb persons beans
      */
     public static class Person {
         protected int id;
         protected String name;
         protected String character;
 
         public Person(int id, String name, String character) {
             this.id        = id;
             this.name      = name;
             this.character = character;
         }
 
         public String getCharacter() {
             return character;
         }
 
         public int getId() {
             return id;
         }
 
         public String getName() {
             return name;
         }
 
         @Override
         public String toString() {
             return this.id+" - "+this.name+" - "+this.character;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final Person other = (Person) obj;
             if (this.id != other.id) {
                 return false;
             }
             if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                 return false;
             }
             if ((this.character == null) ? (other.character != null) : !this.character.equals(other.character)) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 7;
             hash = 47 * hash + this.id;
             hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
             hash = 47 * hash + (this.character != null ? this.character.hashCode() : 0);
             return hash;
         }
     }
 
     public static class Actor extends Person {
 
         public Actor(int id, String name, String character) {
             super(id, name, character);
         }
     }
 
     public static class Director extends Person {
 
         public Director(int id, String name) {
             super(id, name, null);
         }
 
         @Override
         public String toString() {
             return this.id+" - "+this.name;
         }
     }
 
     public static class Writer extends Person {
 
         public Writer(int id, String name) {
             super(id, name, null);
         }
 
         @Override
         public String toString() {
             return this.id+" - "+this.name;
         }
     }
 
     /**
      * Jmdb Rating bean
      */
     public static class Rating {
 
         private float rating;
         private int votes;
 
         public Rating(float rating, int votes) {
             this.rating = rating;
             this.votes  = votes;
         }
 
         public float getRating() {
             return rating;
         }
 
         public int getVotes() {
             return votes;
         }
 
         @Override
         public String toString() {
             return this.rating+"/10, "+this.votes+" votes";
         }
     }
 
     /**
      * Jmdb Certificate
      */
     public static class Certificate {
 
         private String country;
         private String certification;
 
         public Certificate(String country, String certification) {
             this.country       = country;
             this.certification = certification;
         }
 
         public String getCertification() {
             return certification;
         }
 
         public String getCountry() {
             return country;
         }
 
         @Override
         public String toString() {
             return this.country+":"+this.certification;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final Certificate other = (Certificate) obj;
             if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
                 return false;
             }
             if ((this.certification == null) ? (other.certification != null) : !this.certification.equals(other.certification)) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 5;
             hash = 41 * hash + (this.country != null ? this.country.hashCode() : 0);
             hash = 41 * hash + (this.certification != null ? this.certification.hashCode() : 0);
             return hash;
         }
     }
 
     /**
      * Jmdb Official site bean
      */
     public static class OfficialSite {
 
         private String url;
         private String title;
 
         public OfficialSite(String url, String title) {
             this.url   = url;
             this.title = title;
         }
 
         public String getTitle() {
             return title;
         }
 
         public String getUrl() {
             return url;
         }
 
         @Override
         public String toString() {
             return this.title+" - "+this.url;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final OfficialSite other = (OfficialSite) obj;
             if ((this.url == null) ? (other.url != null) : !this.url.equals(other.url)) {
                 return false;
             }
             if ((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 5;
             hash = 67 * hash + (this.url != null ? this.url.hashCode() : 0);
             hash = 67 * hash + (this.title != null ? this.title.hashCode() : 0);
             return hash;
         }
     }
 }
