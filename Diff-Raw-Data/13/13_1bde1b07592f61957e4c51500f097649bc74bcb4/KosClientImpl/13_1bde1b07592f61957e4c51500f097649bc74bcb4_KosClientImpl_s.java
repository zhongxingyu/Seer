 package cz.cvut.fel.kos.impl;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.client.ClientHttpRequestFactory;
 import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
 import org.springframework.web.client.RestTemplate;
 
 import cz.cvut.fel.kos.Configuration;
 import cz.cvut.fel.kos.KosClient;
 import cz.cvut.fel.kos.KosSemesterCode;
import cz.cvut.fel.kos.jaxb.Name;
 import cz.cvut.fel.kos.jaxb.Semester;
 import cz.jirutka.atom.jaxb.AtomLink;
 import cz.jirutka.atom.jaxb.Entry;
 import cz.jirutka.atom.jaxb.Feed;
 
 /**
  * KOS Client API implementation built on Spring REST template
  */
 public class KosClientImpl implements KosClient {
 	private final Log log = LogFactory.getLog(getClass());
 
 	private final Configuration configuration;
 	private final RestTemplate template;
 
 	@Autowired
 	public KosClientImpl(RestTemplate template, Configuration configuration) {
 		this.template = template;
 		this.configuration = configuration;
 	}
 
 	@PostConstruct
 	public void configureKosCredentials() {
 		ClientHttpRequestFactory rf = template.getRequestFactory();
 		if (rf instanceof HttpComponentsClientHttpRequestFactory) {
 			log.info("RESTtemplate configuring http BASIC AUTH");
 			HttpComponentsClientHttpRequestFactory requestFactory = (HttpComponentsClientHttpRequestFactory) rf;
 			DefaultHttpClient httpClient = (DefaultHttpClient) requestFactory.getHttpClient();
 			URL url = null;
 			try {
 				url = new URL(configuration.getUri());
 			} catch (MalformedURLException e) {
 				log.error("Malformed KOS URL", e);
 			}
 			String host = (url == null) ? null : url.getHost();
 			AuthScope authScope = new AuthScope(host, -1, AuthScope.ANY_REALM);
 			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(configuration.getUsername(), configuration.getPassword());
 			httpClient.getCredentialsProvider().setCredentials(authScope, credentials);
 			log.info("RESTtemplate BASIC AUTH configured for host:" + host + " port:any");
 		} else {
 			log.warn("RESTtemplate request factory BASIC AUTH disabled!");
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> Feed<T> getForFeed(String uri, Map<String, ?> urlVariables) {
 		return template.getForObject(uri, Feed.class, urlVariables);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> Feed<T> getForFeed(URI uri) {
 		return template.getForObject(uri, Feed.class);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> Entry<T> getForEntry(String uri, Map<String, ?> urlVariables) {
 		return template.getForObject(uri, Entry.class, urlVariables);
 	}
 
 	private <T> Entry<T> getForEntry(String uri) {
 		Map<String, ?> urlVariables = Collections.emptyMap();
 		return getForEntry(uri, urlVariables);
 	}
 
 	public Semester getPreviousSemester() {
 		Entry<Semester> entry = getForEntry(configuration.getUri() + "semesters/prev");
 		return entry.getContent();
 	}
 
 	public Semester getSemester() {
 		Entry<Semester> entry = getForEntry(configuration.getUri() + "semesters/current");
 		return entry.getContent();
 	}
 
 	public Semester getNextSemester() {
 		Entry<Semester> entry = getForEntry(configuration.getUri() + "semesters/next");
 		return entry.getContent();
 	}
 
 	private Feed<Semester> getSemestersFeed(int offset, int limit) {
 		Map<String, Object> urlVariables = new HashMap<String, Object>();
 		urlVariables.put(KosRestParams.OFFSET, offset);
 		urlVariables.put(KosRestParams.LIMIT, limit);
 		urlVariables.put(KosRestParams.ORDERBY, "startDate@asc");
 		Feed<Semester> feed = getForFeed(configuration.getUri() + "semesters", urlVariables);
 		return feed;
 	}
 
 	private <T> Feed<T> getNextFeed(Feed<T> feed) {
 		List<AtomLink> links = feed.getLinks();
 		for (AtomLink link : links) {
 			if ("next".equals(link.getRel())) {
 				try {
 					URL url = new URL(feed.getBase().toURL(), link.getHref().toString());
 					log.trace("Feed chain next URL " + url);
 					return getForFeed(url.toURI());
 				} catch (MalformedURLException e) {
 					log.error("Problem reading feed chain", e);
 				} catch (URISyntaxException e) {
 					log.error("Problem reading feed chain", e);
 				}
 			}
 		}
 		return null;
 	}
 
 	public List<Semester> getSemesters() {
 		List<Semester> semesters = new LinkedList<Semester>();
 		int offset = 0;
 		int limit = 100;
 		Feed<Semester> feed = getSemestersFeed(offset, limit);
 		while (feed != null) {
 			for (Entry<Semester> entry : feed.getEntries()) {
 				semesters.add(entry.getContent());
 			}
 			feed = getNextFeed(feed);
 		}
 		;
 		log.info("Feed size " + semesters.size());
 		return semesters;
 	}
 
 	public Semester getSemester(String code) {
 		if (!KosSemesterCode.validate(code)) {
 			throw new IllegalArgumentException("Invalid semester code " + code);
 		}
 		Entry<Semester> entry = getForEntry(configuration.getUri() + "semesters/" + code);
 		return entry.getContent();
 	}
 
 	public String getSemesterName(String code, Locale locale) {
 		if (locale == null) {
 			throw new NullPointerException();
 		}
 		Semester semester = getSemester(code);
 		String english = null;
		for (Name name : semester.getName()) {
			Locale lang = new Locale(name.getLang());
 			if (locale.equals(lang)) {
				return name.getValue();
 			}
 			if (Locale.ENGLISH.equals(lang)) {
				english = name.getValue();
 			}
 		}
 		assert english != null;
 		return english;
 	}
 
 	public Semester getSemester(int index) {
 		Map<String, String> urlVariables = new HashMap<String, String>();
 		Entry<Semester> entry = getForEntry(configuration.getUri() + "semesters/current", urlVariables);
 		return entry.getContent();
 	}
 
 }
