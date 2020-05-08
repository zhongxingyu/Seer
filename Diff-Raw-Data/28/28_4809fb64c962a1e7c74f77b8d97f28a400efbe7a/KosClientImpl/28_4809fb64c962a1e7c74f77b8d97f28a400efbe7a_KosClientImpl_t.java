 package cz.cvut.fel.kos.impl;
 
import static cz.cvut.fel.kos.impl.KosRestParams.*;

 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.client.ClientHttpRequestFactory;
 import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
 import org.springframework.web.client.HttpClientErrorException;
 import org.springframework.web.client.RestTemplate;
 
 import cz.cvut.fel.kos.Configuration;
 import cz.cvut.fel.kos.Configuration.Authentication;
 import cz.cvut.fel.kos.KosClient;
 import cz.cvut.fel.kos.KosSemesterCode;
 import cz.cvut.fel.kos.Translator;
 import cz.cvut.fel.kos.jaxb.Course;
 import cz.cvut.fel.kos.jaxb.Semester;
 import cz.cvut.fel.kos.jaxb.Student;
 import cz.cvut.fel.kos.jaxb.StudyState;
 import cz.cvut.fel.kos.jaxb.Teacher;
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
 
	private static String param(String name) {
		return name + "={" + name + "}";
	}

 	public KosClientImpl(RestTemplate template, Configuration configuration) {
 		this.template = template;
 		this.configuration = configuration;
 		configureKosCredentials();
 	}
 
 	private void configureKosCredentials() {
 		if (configuration.getAuthentication() == Authentication.OAUTH2) {
 			throw new UnsupportedOperationException("OAuth v2.0 is not supported yet");
 		}
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
 
 	private <T> Feed<T> getForFeed(String uri) {
 		Map<String, ?> urlVariables = Collections.emptyMap();
 		return getForFeed(uri, urlVariables);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> Entry<T> getForEntry(String uri, Map<String, ?> urlVariables) {
 		try {
 			return template.getForObject(uri, Entry.class, urlVariables);
 		} catch (HttpClientErrorException e) {
 			HttpStatus httpStatus = e.getStatusCode();
 			if (httpStatus == HttpStatus.NOT_FOUND) {
 				log.warn("KOS API entry " + uri + " Not Found!");
 				return null;
 			}
 			throw e;
 		}
 	}
 
 	private <T> Entry<T> getForEntry(String uri) {
 		Map<String, ?> urlVariables = Collections.emptyMap();
 		return getForEntry(uri, urlVariables);
 	}
 
 	/**
 	 * GET next Feed in sequence
 	 * 
 	 * @param feed
 	 * @return next Feed in sequence
 	 */
 	private <T> Feed<T> getNextFeed(Feed<T> feed) {
 		List<AtomLink> links = feed.getLinks();
 		for (AtomLink link : links) {
 			if ("next".equals(link.getRel())) {
 				try {
 					URI uri = UriUtils.mergeAsURL(feed.getBase(), link.getHref());
 					log.trace("Feed chain next URL " + uri);
 					return getForFeed(uri);
 				} catch (MalformedURLException e) {
 					log.error("Problem reading feed chain", e);
 				} catch (URISyntaxException e) {
 					log.error("Problem reading feed chain", e);
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * sequentially GETs series of Feed
 	 * 
 	 * @param feed
 	 *            first Feed obtained
 	 * @return all data on this URL starting with first Feed
 	 */
 	private <T> List<T> getChainedFeedsContent(Feed<T> feed) {
 		List<T> list = new LinkedList<T>();
 		while (feed != null) {
 			for (Entry<T> entry : feed.getEntries()) {
 				list.add(entry.getContent());
 			}
 			feed = getNextFeed(feed);
 		}
 		return list;
 	}
 
 	/**
 	 * GET feed result of RSQL expression. The offset is set to 0 and limit to
 	 * 10 (default).
 	 * 
 	 * @param uri
 	 *            REST URI String
 	 * @param rsql
 	 *            instance of {@link RsqlBuilder} representing expression
 	 * @return feed containing first page of result
 	 */
 	private <T> Feed<T> getForFeed(String uri, RsqlBuilder rsql) {
 		Map<String, Object> urlVariables = new HashMap<String, Object>();
		urlVariables.put(OFFSET, 0);
		urlVariables.put(LIMIT, 10);
		urlVariables.put(QUERY, rsql.toString());
		uri = uri + "?" + param(OFFSET) + "&" + param(LIMIT) + "&" + param(ORDERBY) + "&" + param(QUERY);
 		return getForFeed(uri, urlVariables);
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
		urlVariables.put(OFFSET, offset);
		urlVariables.put(LIMIT, limit);
		urlVariables.put(ORDERBY, "startDate@asc");
		String uri = configuration.getUri() + "semesters?" + param(OFFSET) + "&" + param(LIMIT) + "&" + param(ORDERBY);
		Feed<Semester> feed = getForFeed(uri, urlVariables);
 		return feed;
 	}
 
 	public List<Semester> getSemesters() {
 		int offset = 0;
 		int limit = 100;
 		Feed<Semester> feed = getSemestersFeed(offset, limit);
 		List<Semester> semesters = getChainedFeedsContent(feed);
		// FIXME workaround invalid semesters
 		Iterator<Semester> it = semesters.iterator();
 		while (it.hasNext()) {
 			Semester semester = it.next();
 			if (!KosSemesterCode.validate(semester.getCode())) {
 				it.remove();
 			}
 		}
 		Collections.sort(semesters, new Comparator<Semester>() {
 			public int compare(Semester o1, Semester o2) {
 				return o1.getCode().compareToIgnoreCase(o2.getCode());
 			}
 		});
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
 		if (semester == null) {
 			throw new NullPointerException();
 		}
 		return new Translator(locale).localizedString(semester.getName());
 	}
 
 	public List<Course> getCourses() {
 		// TODO impl feed chain, use query to remove obsolete courses
 		Feed<Course> feed = getForFeed(configuration.getUri() + "courses");
 		return feed.getContents();
 	}
 
 	public Course getCourse(String code) {
 		if (code == null) {
 			throw new NullPointerException();
 		}
 		Entry<Course> entry = getForEntry(configuration.getUri() + "courses/" + code);
 		return (entry == null) ? null : entry.getContent();
 	}
 
 	public List<Student> getStudents() {
 		RsqlBuilder rsql = new RsqlBuilder();
 		rsql.equal("studyState", StudyState.ACTIVE);
 		Feed<Student> feed = getForFeed(configuration.getUri() + "students", rsql);
 		return getChainedFeedsContent(feed);
 	}
 
 	public Student getStudent(String username) {
 		if (username == null) {
 			throw new NullPointerException();
 		}
 		Entry<Student> entry = getForEntry(configuration.getUri() + "students/" + username);
 		return (entry == null) ? null : entry.getContent();
 	}
 
 	public List<Student> getStudentByName(String firstName, String lastName) {
 		RsqlBuilder rsql = new RsqlBuilder();
 		rsql.startWith("firstName", firstName).or().startWith("lastName", lastName);
 		Feed<Student> feed = getForFeed(configuration.getUri(), rsql);
 		return getChainedFeedsContent(feed);
 	}
 
 	public Teacher getTeacher(String username) {
 		if (username == null) {
 			throw new NullPointerException();
 		}
 		Entry<Teacher> entry = getForEntry(configuration.getUri() + "teachers/" + username);
 		return (entry == null) ? null : entry.getContent();
 	}
 
 }
