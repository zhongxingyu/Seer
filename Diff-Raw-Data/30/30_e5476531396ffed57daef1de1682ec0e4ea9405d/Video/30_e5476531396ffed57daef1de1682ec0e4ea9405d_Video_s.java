 package br.ufmg.dcc.vod.spiderpig.jobs.youtube;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
 
 import br.ufmg.dcc.vod.spiderpig.common.Tuple;
 import br.ufmg.dcc.vod.spiderpig.common.URLGetter;
 import br.ufmg.dcc.vod.spiderpig.common.config.AbstractConfigurable;
 import br.ufmg.dcc.vod.spiderpig.filesaver.FileSaver;
 import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableJobExecutor;
 import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
 import br.ufmg.dcc.vod.spiderpig.jobs.ThroughputManager;
 import br.ufmg.dcc.vod.spiderpig.jobs.WorkerInterested;
 import br.ufmg.dcc.vod.spiderpig.jobs.youtube.api.VideoAPIRequester;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 
 import com.google.common.collect.Sets;
 
 public class Video extends AbstractConfigurable<Void> 
 		implements ConfigurableJobExecutor {
 
	private static final Logger LOG = Logger.getLogger(Video.class);
	
 	private static final String BKOFF_TIME = "worker.job.youtube.backofftime";
 	private static final String SLEEP_TIME = "worker.job.youtube.sleeptime";
 	private static final String DEV_KEY = "worker.job.youtube.devkey";
 	private static final String APP_NAME = "worker.job.youtube.appname";
 	
 	private static final String CRAWL_HTML = "worker.job.youtube.html";
 	private static final String CRAWL_API = "worker.job.youtube.api";
 	
 	private ThroughputManager throughputManager;
 	private MultiRequester requester;
 
 	@Override
 	public Set<String> getRequiredParameters() {
 		return Sets.newHashSet(SLEEP_TIME, DEV_KEY, APP_NAME, CRAWL_HTML,
				CRAWL_API);
 	}
 
 	@Override
 	public void crawl(CrawlID id, WorkerInterested interested, FileSaver saver) {
		LOG.info("Received id " + id + " " + id.getId());
 		try {
 			List<Tuple<String, byte[]>> result = 
 					this.throughputManager.sleepAndPerform(id.getId(), 
 							requester);
 			
 			for (Tuple<String, byte[]> t : result) {
 				String type = id.getResourceType();
 				if (type.length() > 0)
 					saver.save(id.getResourceType() + "-" + t.first, t.second);
 				else
 					saver.save(t.first, t.second);
 			}
 			interested.crawlDone(id, null);
			LOG.info("Sending result for id " + id);
 		} catch (Exception e) {
			LOG.error("Error at id " + id, e);
 			interested.crawlError(id, e.toString());
 		}
 	}
 
 	@Override
 	public Void realConfigurate(Configuration configuration) throws Exception {
 		long timeBetweenRequests = configuration.getLong(SLEEP_TIME);
 		long backOffTime = configuration.getLong(BKOFF_TIME);
 		
 		String devKey = configuration.getString(DEV_KEY);
 		String appName = configuration.getString(APP_NAME);
 		
 		boolean crawlHtml = configuration.getBoolean(CRAWL_HTML);
 		boolean crawlApi = configuration.getBoolean(CRAWL_API);
 		
 		boolean hasOne = crawlHtml || crawlApi;
 		if (!hasOne)
 			throw new ConfigurationException("Please set at least one option"
 					+ " to crawl");
 		
 		this.throughputManager = new ThroughputManager(timeBetweenRequests,
 				backOffTime);
 		this.requester = new MultiRequester(appName, devKey, crawlHtml, 
 				crawlApi);
 		return null;
 	}
 	
 	private static class MultiRequester implements 
 			Requester<List<Tuple<String, byte[]>>> {
 		
 		private URLGetter urlGetter;
 		private VideoAPIRequester apiRequester;
 		
 		private final boolean crawlHtml;
 		private final boolean crawlApi;
 
 		private DefaultHttpClient httpClient;
 		
 		public MultiRequester(String appName, String devKey, boolean crawlHtml,
 				boolean crawlApi) {
 			this.httpClient = new DefaultHttpClient();
 			this.httpClient.getParams().setParameter(
 			        ClientPNames.COOKIE_POLICY, 
 			        CookiePolicy.BROWSER_COMPATIBILITY);
 			this.urlGetter = new URLGetter("Research-Crawler-APIDEVKEY-" + 
 					devKey);
 			this.apiRequester = new VideoAPIRequester(appName, devKey);
 			this.crawlHtml = crawlHtml;
 			this.crawlApi = crawlApi;
 		}
 		
 		private URI createVideoHTMLUrl(String videoID) 
 				throws URISyntaxException {
 			URIBuilder builder = new URIBuilder();
 			builder.setScheme("http").
 					setHost("www.youtube.com").
 					setPath("/watch").
 					setParameter("v", videoID).
 					setParameter("gl", "US").
 					setParameter("hl", "en");
 			return builder.build();
 		}
 
 		private URI createVideoStatsUrl(String videoID) 
 				throws URISyntaxException {
 			URIBuilder builder = new URIBuilder();
 			builder.setScheme("http").
 					setHost("www.youtube.com").
 					setPath("/insight_ajax").
 					setParameter("action_get_statistics_and_data", "1").
 					setParameter("v", videoID).
 					setParameter("gl", "US").
 					setParameter("hl", "en");
 			return builder.build();
 		}
 		
 		private byte[] performRequest(HttpUriRequest request, String videoID) 
 				throws IOException {
 			String header = "<crawledvideoid = " + videoID + ">";
 			String footer = "</crawledvideoid>";
 			return this.urlGetter.getHtml(this.httpClient, request, 
 					header, footer);
 		}
 		
 		private String getSessionToken(byte[] vidHtml) throws IOException {
 			BufferedReader bis = 
 					new BufferedReader(new InputStreamReader(
 							new ByteArrayInputStream(vidHtml)));
 			String inputLine;
 			while ((inputLine = bis.readLine()) != null) {
 				if (inputLine.contains("insight_ajax")) {
 					String[] split = inputLine.split("\"");
 					return split[1];
 				}
 			}
 			
 			return "";
 		}
 		
 		@Override
 		public List<Tuple<String, byte[]>> performRequest(String id) 
 				throws Exception {
 
 			ArrayList<Tuple<String, byte[]>> returnVal = new ArrayList<>();
 			if (this.crawlHtml) {
                LOG.info("Collecting video page for id " + id);
                 HttpGet getMethod = new HttpGet(createVideoHTMLUrl(id));
 				byte[] vidHtml = performRequest(getMethod, id);
 				returnVal.add(new Tuple<String, byte[]>(id + "-content.html", 
 						vidHtml));
 
 				String ajaxToken = getSessionToken(vidHtml);
 				
                LOG.info("Collecting stats page for id " + id);
 				HttpPost postMethod = new HttpPost(createVideoStatsUrl(id));
 				List<NameValuePair> formParams = new ArrayList<NameValuePair>();
 				formParams.add(new BasicNameValuePair("session_token", 
 						ajaxToken));
 				UrlEncodedFormEntity entity = 
 						new UrlEncodedFormEntity(formParams, "UTF-8");
 				postMethod.setEntity(entity);
 				byte[] statsHtml = performRequest(postMethod, id);
 				returnVal.add(new Tuple<String, byte[]>(id + "-stats.html", 
 						statsHtml));
 			}
 			
 			if (this.crawlApi) {
                LOG.info("Collecting api page for id " + id);
 				byte[] apiJson = this.apiRequester.performRequest(id);
 				returnVal.add(new Tuple<String, byte[]>(id + "-api.json", 
 						apiJson));
 			}
 			
 			return returnVal;
 		}
 
 	}
 	
 	public static void main(String[] args) throws Exception {
 		MultiRequester multiRequester = 
 				new MultiRequester("", "", true, false);
 		List<Tuple<String, byte[]>> result = 
 				multiRequester.performRequest("M8dDb39MtpI");
 		
 		System.out.println(new String(result.get(1).second));
 		
 	}
 }
