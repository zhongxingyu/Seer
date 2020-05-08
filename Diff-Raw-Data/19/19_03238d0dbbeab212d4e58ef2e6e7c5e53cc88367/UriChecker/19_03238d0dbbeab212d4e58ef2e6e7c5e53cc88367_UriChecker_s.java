 package kupczynski.info.linkchecker.checker.impl;
 
 import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
 import java.util.Collection;
 
 import kupczynski.info.linkchecker.checker.api.UriService;
 import kupczynski.info.linkchecker.checker.api.UriStatusDTO;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpParams;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableList.Builder;
 
 public class UriChecker implements Runnable {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(UriChecker.class);
 
 	private final UriService uriService;
 	private final boolean follow;
 	private final String from;
 	private final String uri;
 	private final Integer depth;
 
 	public UriChecker(UriService uriService, boolean follow, String from,
 			String uri, Integer depth) {
 		this.uriService = uriService;
 		this.follow = follow;
 		this.from = from;
 		this.uri = uri;
 		this.depth = depth;
 	}
 
 	@Override
 	public void run() {
 		logger.info("Fetching {}", uri);
 		UriStatusDTO result = null;
 		Collection<String> children = null;
 
 		HttpResponse resp;
 		try {
 			resp = fetchUri(uri);
 			result = fromResponse(resp);
 
 			boolean isHtml = resp.getFirstHeader("Content-type").getValue()
 					.startsWith("text/html");
 			boolean isRedirect = isRedirect(resp.getStatusLine()
 					.getStatusCode());
 			if (follow && (isHtml || isRedirect)) {
 				children = extractChildren(resp);
 			}
 		} catch (RuntimeException e) {
 			logger.warn("Error fetching {}", uri);
 			result = new UriStatusDTO(from, depth, uri, -1,
 					"RuntimeException: " + e.getMessage(),
 					UriStatusDTO.Code.ERROR);
 		}
 
 		if (children != null) {
 			uriService.finished(result, children);
 		} else {
 			uriService.finished(result);
 		}
 	}
 
 	private HttpResponse fetchUri(String uri) {
 		// TODO: multithreading
 		HttpClient client = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(uri);
 
 		HttpParams params = httpget.getParams();
 		params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.FALSE);
 		httpget.setParams(params);
 
 		HttpResponse response;
 		try {
 			response = client.execute(httpget);
 		} catch (ClientProtocolException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		return response;
 	}
 
 	private UriStatusDTO fromResponse(HttpResponse resp) {
 		StatusLine statusLine = resp.getStatusLine();
 		return new UriStatusDTO(from, depth, uri, statusLine.getStatusCode(),
 				statusLine.getReasonPhrase(),
 				getStatusForHttpCode(statusLine.getStatusCode()));
 	}
 
 	private Collection<String> extractChildren(HttpResponse resp) {
 		Collection<String> result = null;
 
 		int httpCode = resp.getStatusLine().getStatusCode();
 		if (isRedirect(httpCode)) {
 			result = extractNextFromRedirect(resp);
 		} else {
 			result = extractChildrenFromHtml(resp);
 		}
 		return result;
 	}
 
 	private Collection<String> extractNextFromRedirect(HttpResponse resp) {
 		return ImmutableList.of(resp.getFirstHeader("Location").getValue());
 	}
 
 	private Collection<String> extractChildrenFromHtml(HttpResponse resp) {

		// TODO: base uri determination

		URL url;
		try {
			url = new URL(uri);
		} catch (MalformedURLException e) {
			throw new AssertionError(e);
		}

		String base = url.getProtocol() + "://" + url.getHost();

 		// TODO: encoding
 
 		Document document;
 		try {
			document = Jsoup
					.parse(resp.getEntity().getContent(), "UTF-8", base);
 
 		} catch (IllegalStateException e) {
 			throw new AssertionError(e);
 		} catch (IOException e) {
 			throw new AssertionError(e);
 		}
 
 		Builder<String> builder = ImmutableList.<String> builder();
 
 		if (document != null) {
 			Elements links = document.select("a[href]");
 			for (Element e : links) {
 				builder.add(e.absUrl("href"));
 			}
 
 			Elements imgs = document.select("img[src]");
 			for (Element e : imgs) {
 				builder.add(e.absUrl("src"));
 			}
 		}
 
 		return builder.build();
 	}
 
 	private UriStatusDTO.Code getStatusForHttpCode(int httpCode) {
 
 		UriStatusDTO.Code result;
 
 		switch (httpCode) {
 		case 200:
 			result = UriStatusDTO.Code.OK;
 			break;
 		case 301:
 		case 302:
 			result = UriStatusDTO.Code.WARNING;
 			break;
 		default:
 			result = UriStatusDTO.Code.ERROR;
 			break;
 		}
 
 		return result;
 	}
 
 	private boolean isRedirect(int httpCode) {
 		boolean result = (httpCode == 301 || httpCode == 302);
 		return result;
 	}
 
 }
