 
 package nz.gen.wellington.guardian.contentapiproxy.datasources;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import nz.gen.wellington.guardian.contentapi.parsing.ContentApiStyleJSONParser;
 import nz.gen.wellington.guardian.contentapi.urls.ContentApiStyleUrlBuilder;
 import nz.gen.wellington.guardian.contentapiproxy.datasources.contentapi.HttpForbiddenException;
 import nz.gen.wellington.guardian.contentapiproxy.model.SearchQuery;
 import nz.gen.wellington.guardian.contentapiproxy.utils.CachingHttpFetcher;
 import nz.gen.wellington.guardian.model.Article;
 import nz.gen.wellington.guardian.model.Refinement;
 import nz.gen.wellington.guardian.model.Section;
 import nz.gen.wellington.guardian.model.Tag;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.inject.Inject;
 
 public class ContentApi {
 	
 	public static final String API_HOST = "http://content.guardianapis.com";
 	
 	private static final String DEVELOPER_OVER_RATE = "403 Developer Over Rate";	
 	private final String[] permittedRefinementTypes = {"keyword", "blog", "contributor", "section", "type", "date"};
 
 	private static Logger log = Logger.getLogger(ContentApi.class);
 	
 	private CachingHttpFetcher httpFetcher;
 	private ContentApiStyleJSONParser contentApiJsonParser;
 	private ContentApiKeyPool contentApiKeyPool;
 	private ShortUrlDAO shortUrlDao;
 
 	private Map<String, Section>  sectionsMap;
 
 	
 	@Inject
 	public ContentApi(CachingHttpFetcher httpFetcher, ContentApiStyleJSONParser contentApiJsonParser, ContentApiKeyPool contentApiKeyPool, ShortUrlDAO shortUrlDao) {
 		this.httpFetcher = httpFetcher;
 		this.contentApiJsonParser = contentApiJsonParser;
 		this.contentApiKeyPool = contentApiKeyPool;
 		this.shortUrlDao = shortUrlDao;
 	}
 	
 	
 	public List<Article> getArticles(SearchQuery query) {
 		final String availableApiKey = contentApiKeyPool.getAvailableApiKey();
 		final boolean isOverRate = availableApiKey == null;
 		
 		final String content = getJSONContentForArticleQuery(query, availableApiKey);			
 		if (content != null) {				
 			try {
 				JSONObject json = new JSONObject(content);
 				if (json != null && contentApiJsonParser.isResponseOk(json)) {
 					List<Article> articles = contentApiJsonParser.extractContentItems(json, getSections());
 					if (isOverRate) {
 						for (Article article : articles) {
 							article.setDescription(makeOverQuotaMessageForContentItem(article.getId()));
 						}
 					}
 					
 					hooverUpPreviouslyUnknownShortUrls(articles);					
 					return articles;
 				}
 				
 			} catch (JSONException e) {
 				log.info("JSON error while parsing response", e);
 				log.info(e);
 				return null;
 			}
 		}		
 		return null;
 	}
 
 
 	private void hooverUpPreviouslyUnknownShortUrls(List<Article> articles) {
 		for (Article article : articles) {
 			final String contentId = article.getId();
 			final boolean isNewShortUrl = contentId != null && article.getShortUrl() != null && shortUrlDao.getShortUrlFor(article.getId()) == null;
 			if (isNewShortUrl) {
 				log.info("Caching short url for content item: " + article.getId());
 				shortUrlDao.storeShortUrl(article.getId(), article.getShortUrl());
 			}
 		}
 	}
 	
 	
 	public int getArticleCount(SearchQuery query) {		
 		SearchQuery articleCountQuery = new SearchQuery(query);
 		articleCountQuery.setPageSize(1);
 		articleCountQuery.setShowAllFields(false);
 
 		final String apiKey = contentApiKeyPool.getAvailableApiKey();
 		final String content = getJSONContentForArticleQuery(articleCountQuery, apiKey);
 		if (content != null) {				
 			try {
 				JSONObject json = new JSONObject(content);
 				if (json != null && contentApiJsonParser.isResponseOk(json)) {
 					return contentApiJsonParser.extractContentItemsCount(json);
 				}					
 			} catch (JSONException e) {
 				log.info("JSON error while parsing response", e);
 				log.info(e);
 				return 0;
 			}
 		}
 		return 0;
 	}
 	
 	
 	public Map<String, Section> getSections() {
 		if (sectionsMap != null) {
 			return sectionsMap;	// TODO put into the cache proper, so that it has a finite TTL
 		}
 		
 		log.info("Fetching section list from content api");
 		final String apiKey = contentApiKeyPool.getAvailableApiKey();
 		ContentApiStyleUrlBuilder urlBuilder = new ContentApiStyleUrlBuilder(API_HOST, apiKey);
 		urlBuilder.setFormat("json");
 		final String callUrl = urlBuilder.toSectionsQueryUrl();
 		final String content = getContentFromUrlSuppressingHttpExceptions(callUrl, apiKey);
 		if (content != null) {			
 			List<Section> sections = contentApiJsonParser.parseSectionsRequestResponse(content);
 			log.info("Found " + sections.size() + " good sections");
 			
 			Map<String, Section> sectionsMap = new HashMap<String, Section>();
 			for (Section section : sections) {
 				sectionsMap.put(section.getId(), section);
 			}
 			this.sectionsMap = sectionsMap;
 			return sectionsMap;
 		}
 		return null;
 	}
 	
 	
 	public Article getArticle(String contentId) {
 		log.info("Fetching content item: " + contentId);
 		
 		final String availableApiKey = contentApiKeyPool.getAvailableApiKey();
 		final boolean isOverRate = availableApiKey == null;
 		
 		ContentApiStyleUrlBuilder urlBuilder = new ContentApiStyleUrlBuilder(API_HOST, availableApiKey);
 		urlBuilder.setContentId(contentId);
 		urlBuilder.setFormat("json");
 		urlBuilder.setShowAll(true);
 		final String callUrl = urlBuilder.toContentItemUrl();
 	
 		String content;
 		try {
 			content = httpFetcher.fetchContent(callUrl, "UTF-8");
 		} catch (HttpForbiddenException e1) {
 			return null;
 		}
 		
 		if (content != null) {		
 			try {
 				JSONObject json = new JSONObject(content);
 				if (json != null && contentApiJsonParser.isResponseOk(json)) {			
 					Article article = contentApiJsonParser.extractContentItem(json, getSections());
 					if (isOverRate) {
 						article.setDescription(makeOverQuotaMessageForContentItem(contentId));
 					}
 					return article;
 				}
 			} catch (JSONException e) {
 				log.info("JSON error while processing call url: " + callUrl);
 				log.info(e);
 				return null;
 			}				
 		}		
 		return null;		
 	}
 
 
 	private String makeOverQuotaMessageForContentItem(String contentId) {
 		return "This article (" + contentId + ") could not be downloaded because the Guardian Lite application has temporarily exceeded it's Guardian Content API quota. In the meantime, you should still be able to use the open in browser option to view this content on the Guardian site.";
 	}
 	
 	
 	@Deprecated // TODO query for whole tags rather than interating over single records.
 	public String getShortUrlFor(String contentId) throws HttpForbiddenException {
 		log.info("Fetching short url for: " + contentId);
 		Article article = this.getArticle(contentId);	
 		if (article != null) {
 			return article.getShortUrl();
 		}
 		return null;
 	}
 	
 	public Map<String, List<Refinement>> getTagRefinements(Tag tag, DateTime fromDate, DateTime toDate) {
 		final String apiKey = contentApiKeyPool.getAvailableApiKey();
 		ContentApiStyleUrlBuilder urlBuilder = new ContentApiStyleUrlBuilder(API_HOST, apiKey);
 		urlBuilder.addTag(tag);
 		urlBuilder.setShowAll(false);
 		urlBuilder.setShowRefinements(true);
 		if (fromDate != null) {
 			urlBuilder.setFromDate(fromDate.toString("YYYY-MM-dd"));
 		}
 		if (toDate != null) {
			urlBuilder.setFromDate(toDate.toString("YYYY-MM-dd"));
 		}
 		urlBuilder.setFormat("json");	
 		final String callUrl = urlBuilder.toSearchQueryUrl();		
 		log.info("Fetching from: " + callUrl);
 		return processRefinements(callUrl, apiKey);
 	}
 
 	
 	private Map<String, List<Refinement>> processRefinements(String callUrl, String apiKey) {
 		final String content = getContentFromUrlSuppressingHttpExceptions(callUrl, apiKey);
 		if (content == null) {
 			log.warn("Failed to fetch url: " + callUrl);
 			return null;
 		}
 		
 		try {
 			JSONObject json = new JSONObject(content);
 			JSONObject response = json.getJSONObject("response");
 				
 			if (json != null && contentApiJsonParser.isResponseOk(json) && response.has("refinementGroups")) {				
 				Map<String, List<Refinement>> allParsedRefinementGroups = contentApiJsonParser.parseRefinementGroups(response);				
 				return filterOutPermittedRefinements(allParsedRefinementGroups);
 			}
 			
 		} catch (JSONException e) {
 			log.error(e.getMessage());
 		}		
 		return null;
 	}
 
 
 	private Map<String, List<Refinement>> filterOutPermittedRefinements(
 			Map<String, List<Refinement>> allParsedRefinementGroups) {
 		Map<String, List<Refinement>> permittedRefinements = new HashMap<String, List<Refinement>>();
 		for (String refinementType : allParsedRefinementGroups.keySet()) {					
 			boolean isPermittedRefinementType = Arrays.asList(permittedRefinementTypes).contains(refinementType);
 			if (isPermittedRefinementType) {
 				permittedRefinements.put(refinementType, allParsedRefinementGroups.get(refinementType));
 			}					
 		}
 		return permittedRefinements;
 	}
 	
 	private String getJSONContentForArticleQuery(SearchQuery query, String apiKey) {
 		ContentApiStyleUrlBuilder urlBuilder = new ContentApiStyleUrlBuilder(API_HOST, apiKey);		
 		urlBuilder.setFormat("json");
 		urlBuilder.setShowAll(query.isShowAllFields());
 		
 		if (query.getFromDate() != null) {
 			urlBuilder.setFromDate(query.getFromDate().toString("yyyy-MM-dd"));
 		}
 		if (query.getToDate() != null) {
 			urlBuilder.setToDate(query.getToDate().toString("yyyy-MM-dd"));
 		}
 		
 		urlBuilder.setPageSize(query.getPageSize());
 		
 		if (query.getTags() != null && !query.getTags().isEmpty()) {
 			for (Tag tag : query.getTags()) {
 				urlBuilder.addTag(tag);
 			}
 		}
 		
 		final String callUrl = urlBuilder.toSearchQueryUrl();
 		return getContentFromUrlSuppressingHttpExceptions(callUrl, apiKey);
 	}
 	
 	private String getContentFromUrlSuppressingHttpExceptions(final String callUrl, String apiKey) {
 		String content;
 		try {
 			content = httpFetcher.fetchContent(callUrl, "UTF-8");
 		} catch (HttpForbiddenException e) {
 			if (isOverRateException(e)) {
 				contentApiKeyPool.markKeyAsBeenOverRate(apiKey);
 			}
 			return null;
 		}
 		return content;
 	}
 
 
 	private boolean isOverRateException(HttpForbiddenException e) {
 		return e.getMessage().contains(DEVELOPER_OVER_RATE);
 	}
 	
 }
