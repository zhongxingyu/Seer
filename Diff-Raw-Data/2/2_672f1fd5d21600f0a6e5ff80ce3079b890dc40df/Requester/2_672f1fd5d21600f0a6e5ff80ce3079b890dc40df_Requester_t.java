 package br.ufmg.dcc.vod.spiderpig.jobs.topsy;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.log4j.Logger;
 
 import twitter4j.JSONArray;
 import twitter4j.JSONException;
 import twitter4j.JSONObject;
 import br.ufmg.dcc.vod.spiderpig.common.URLGetter;
 import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
 import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
 import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableRequester;
 import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResultFactory;
 import br.ufmg.dcc.vod.spiderpig.jobs.PayloadsFactory;
 import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
 import br.ufmg.dcc.vod.spiderpig.jobs.youtube.UnableToCrawlException;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;
 
 public class Requester implements ConfigurableRequester {
 	private static final Logger LOG = Logger.getLogger(Requester.class);
 	private DefaultHttpClient httpClient;
     private URLGetter urlGetter;
 	
 	@Override
 	public void configurate(Configuration configuration,
 			ConfigurableBuilder builder) throws BuildException {
 		this.httpClient = new DefaultHttpClient();
         this.httpClient.getParams().setParameter(
                 ClientPNames.COOKIE_POLICY, 
                 CookiePolicy.BROWSER_COMPATIBILITY);
         this.urlGetter = new URLGetter("Research-Crawler-contact-" + 
                 "flaviov at dcc.ufmg.br");
 	}
 
 	@Override
 	public Set<String> getRequiredParameters() {
 		return null;
 	}
 
 	@Override
 	public CrawlResult performRequest(CrawlID crawlID) throws QuotaException {
 		String query = crawlID.getId();
 		CrawlResultFactory crawlResult = new CrawlResultFactory(crawlID);
 		List<CrawlID> toQueue = new ArrayList<>();
 		
 		try {
 			PayloadsFactory payloadsFactory = new PayloadsFactory();
 			int numResults = 0;
 			int page = 1;
 			do {
 				HttpGet getMethod = new HttpGet(createUrl(query, page));
 				LOG.info(getMethod);
 				byte[] jsonResult = 
 						this.urlGetter.getHtml(this.httpClient, getMethod, 
 								"", "");
 				payloadsFactory.addPayload(crawlID, jsonResult, page + "");
 				
 				String jsonString = new String(jsonResult);
 				JSONObject json = new JSONObject(jsonString);
 				JSONArray results = 
 						json.getJSONObject("response").getJSONArray("list");
 				for (int i = 0; i < results.length(); i++) {
 					JSONObject aResult = results.getJSONObject(i);
 					long tstamp = aResult.getLong("firstpost_date");
 					String tstampStr = tstamp + "";
 					toQueue.add(
 							CrawlID.newBuilder().
 							setId(tstampStr).build());
 				}
 				
 				numResults = results.length();
 				page += 1;
 			} while (numResults > 0);
 			return crawlResult.buildOK(payloadsFactory.build(), toQueue);
 		} catch (IOException | URISyntaxException | JSONException e) {
 			UnableToCrawlException cause = new UnableToCrawlException(e);
             return crawlResult.buildNonQuotaError(cause);
 		}
 	}
 
 	private URI createUrl(String query, int page) throws URISyntaxException {
 		URIBuilder builder = new URIBuilder();
         builder.setScheme("http").
                 setHost("otter.topsy.com").
                 setPath("/search.js").
                 setParameter("window", "a").
                 setParameter("apikey", "09C43A9B270A470B8EB8F2946A9369F3").
                 setParameter("perpage", "100").
                 setParameter("sort_method", "date");
         
         if (page > 0) {
         	builder.setParameter("page", ""+page);
         }
         
 		String[] split = query.split("\t");
 		if (split.length == 2) {
 			builder.setParameter("q", split[0]);
			builder.setParameter("maxtime", split[1]);
 		} else {
 			builder.setParameter("q", query);
 		}
 		
 		return builder.build();
 	}
 	
 	public static void main(String[] args) throws BuildException, QuotaException {
 		Requester re = new Requester();
 		re.configurate(null, null);
 		CrawlResult res = re.performRequest(CrawlID.newBuilder().setId("#cscw2010").build());
 		System.out.println(res);
 	}
 }
