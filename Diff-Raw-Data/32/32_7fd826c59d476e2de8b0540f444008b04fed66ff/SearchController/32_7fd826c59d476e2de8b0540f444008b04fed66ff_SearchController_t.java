 /*
  * Copyright 2007-2012 The Europeana Foundation
  *
  *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved 
  *  by the European Commission;
  *  You may not use this work except in compliance with the Licence.
  *  
  *  You may obtain a copy of the Licence at:
  *  http://joinup.ec.europa.eu/software/page/eupl
  *
  *  Unless required by applicable law or agreed to in writing, software distributed under 
  *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of 
  *  any kind, either express or implied.
  *  See the Licence for the specific language governing permissions and limitations under 
  *  the Licence.
  */
 
 package eu.europeana.api2.web.controller;
 
 import java.io.IOException;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.mongodb.Mongo;
 
 import eu.europeana.api2.exceptions.LimitReachedException;
 import eu.europeana.api2.web.model.ModelUtils;
 import eu.europeana.api2.web.model.json.ApiError;
 import eu.europeana.api2.web.model.json.ApiView;
 import eu.europeana.api2.web.model.json.BriefView;
 import eu.europeana.api2.web.model.json.SearchResults;
 import eu.europeana.api2.web.model.json.Suggestions;
 import eu.europeana.api2.web.model.json.abstracts.ApiResponse;
 import eu.europeana.api2.web.model.xml.kml.KmlResponse;
 import eu.europeana.api2.web.model.xml.rss.Channel;
 import eu.europeana.api2.web.model.xml.rss.Item;
 import eu.europeana.api2.web.model.xml.rss.RssResponse;
 import eu.europeana.api2.web.util.OptOutDatasetsUtil;
 import eu.europeana.corelib.db.exception.DatabaseException;
 import eu.europeana.corelib.db.logging.api.ApiLogger;
 import eu.europeana.corelib.db.logging.api.enums.RecordType;
 import eu.europeana.corelib.db.service.ApiKeyService;
 import eu.europeana.corelib.definitions.db.entity.relational.ApiKey;
 import eu.europeana.corelib.definitions.solr.beans.ApiBean;
 import eu.europeana.corelib.definitions.solr.beans.BriefBean;
 import eu.europeana.corelib.definitions.solr.beans.IdBean;
 import eu.europeana.corelib.definitions.solr.model.Query;
 import eu.europeana.corelib.solr.exceptions.SolrTypeException;
 import eu.europeana.corelib.solr.model.ResultSet;
 import eu.europeana.corelib.solr.service.SearchService;
 import eu.europeana.corelib.web.utils.NavigationUtils;
 
 /**
  * @author Willem-Jan Boogerd <www.eledge.net/contact>
  */
 @Controller
 public class SearchController {
 
 	private final Logger log = Logger.getLogger(getClass().getName());
 
 	@Resource(name = "corelib_db_mongo")
 	private Mongo mongo;
 
 	@Resource
 	private SearchService searchService;
 
 	@Resource
 	private ApiKeyService apiService;
 
 	@Value("#{europeanaProperties['api.rowLimit']}")
 	private String rowLimit = "96";
 
	@Value("#{europeanaProperties['portal.server']}")
	private String portalServer;

	@Value("#{europeanaProperties['portal.name']}")
	private String portalName;

 	@Value("#{europeanaProperties['api2.url']}")
 	private String apiUrl;
 
 	@Value("#{europeanaProperties['api.optOutList']}")
 	private String optOutList;
 
 	@Resource
 	private ApiLogger apiLogger;
 
	private static String portalUrl;

	private static int maxRows = -1;

 	@RequestMapping(value = "/v2/search.json", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody ApiResponse searchJson(
 		@RequestParam(value = "query", required = true) String q,
 		@RequestParam(value = "qf", required = false) String[] refinements,
 		@RequestParam(value = "profile", required = false, defaultValue="standard") String profile,
 		@RequestParam(value = "start", required = false, defaultValue="1") int start,
 		@RequestParam(value = "rows", required = false, defaultValue="12") int rows,
 		@RequestParam(value = "sort", required = false) String sort,
 		@RequestParam(value = "wskey", required = true) String wskey
 	) {
		if (maxRows == -1) {
			maxRows = Integer.parseInt(rowLimit);
		}
		rows = Math.min(rows, maxRows);
 		log.info("=== search.json: " + rows);
 		OptOutDatasetsUtil.setOptOutDatasets(optOutList);
 
 		Query query = new Query(q).setRefinements(refinements).setPageSize(rows).setStart(start - 1);
 		long usageLimit = 0;
 		ApiKey apiKey;
 		long requestNumber = 0;
 		try{
 			apiKey = apiService.findByID(wskey);
 			if (apiKey == null) {
 				return new ApiError(wskey, "search.json", "Unregistered user");
 			}
 			usageLimit = apiKey.getUsageLimit();
 			requestNumber = apiLogger.getRequestNumber(wskey);
 			if (apiLogger.getRequestNumber(wskey) > usageLimit) {
 				throw new LimitReachedException();
 			}
 		} catch (DatabaseException e){
 			apiLogger.saveApiRequest(wskey, query.getQuery(), RecordType.SEARCH, profile);
 			return new ApiError(wskey, "search.json", e.getMessage(), requestNumber);
 		} catch (LimitReachedException e){
 			apiLogger.saveApiRequest(wskey, query.getQuery(), RecordType.LIMIT, profile);
 			return new ApiError(wskey, "search.json", "Rate limit exceeded. " + usageLimit, requestNumber);
 		}
 
 		Class<? extends IdBean> clazz = ApiBean.class;
 		if (StringUtils.containsIgnoreCase(profile, "minimal")) {
 			clazz = BriefBean.class;
 		}
 		try {
 			SearchResults<? extends IdBean> response = createResults(wskey, profile, query, clazz);
 			response.requestNumber = requestNumber;
 			log.info("got response " + response.items.size());
 			/*
 			ObjectMapper objectMapper = new ObjectMapper();
 			objectMapper.setSerializationInclusion(Inclusion.NON_EMPTY);
 			try {
 				String json = objectMapper.writeValueAsString(response);
 				log.info("JSON: " + json);
 			} catch (JsonGenerationException e) {
 				log.info(e.getMessage());
 				e.printStackTrace();
 			} catch (JsonMappingException e) {
 				log.info(e.getMessage());
 				e.printStackTrace();
 			} catch (IOException e) {
 				log.info(e.getMessage());
 				e.printStackTrace();
 			}
 			*/
 			apiLogger.saveApiRequest(wskey, query.getQuery(), RecordType.SEARCH, profile);
 			return response;
 		} catch (SolrTypeException e) {
 			log.severe(wskey + " [search.json] " + e.getMessage());
 			e.printStackTrace();
 			return new ApiError(wskey, "search.json", e.getMessage());
 		}
 	}
 
 	private <T extends IdBean> SearchResults<T> createResults(String apiKey, String profile, Query q, Class<T> clazz) throws SolrTypeException {
 		SearchResults<T> response = new SearchResults<T>(apiKey, "search.json");
 		ResultSet<T> resultSet = searchService.search(clazz, q);
 		response.totalResults = resultSet.getResultSize();
 		response.itemsCount = resultSet.getResults().size();
 		response.items = resultSet.getResults();
 
 		BriefView.setApiUrl(apiUrl);
		BriefView.setPortalUrl(getPortalUrl());
 
 		List<T> beans = new ArrayList<T>();
 		for (T b : resultSet.getResults()) {
 			if (b instanceof ApiBean) {
 				ApiBean bean = (ApiBean)b;
 				ApiView view = new ApiView(bean, profile, apiKey);
 				//bean.setProfile(profile);
 				beans.add((T) view);
 			} else if (b instanceof BriefBean) {
 				BriefBean bean = (BriefBean)b;
 				BriefView view = new BriefView(bean, profile, apiKey);
 				beans.add((T) view);
 			}
 		}
 
 		log.info("beans: " + beans.size());
 		response.items = beans;
 		if (StringUtils.containsIgnoreCase(profile, "facets") || StringUtils.containsIgnoreCase(profile, "portal")) {
 			response.facets = ModelUtils.conventFacetList(resultSet.getFacetFields());
 		}
 		if (StringUtils.containsIgnoreCase(profile, "breadcrumb") || StringUtils.containsIgnoreCase(profile, "portal")) {
 			response.breadCrumbs = NavigationUtils.createBreadCrumbList(q);
 		}
 		if (StringUtils.containsIgnoreCase(profile, "spelling") || StringUtils.containsIgnoreCase(profile, "portal")) {
 			response.spellcheck = ModelUtils.convertSpellCheck(resultSet.getSpellcheck());
 		}
 //		if (StringUtils.containsIgnoreCase(profile, "suggestions") || StringUtils.containsIgnoreCase(profile, "portal")) {
 //		}
 		return response;
 	}
 
 	@RequestMapping(value = "/v2/search.kml", produces= MediaType.APPLICATION_XML_VALUE)//, produces = "application/vnd.google-earth.kml+xml")
 	public @ResponseBody KmlResponse searchKml(
 		Principal principal,
 		@RequestParam(value = "query", required = true) String q,
 		@RequestParam(value = "qf", required = false) String[] refinements,
 		@RequestParam(value = "start", required = false, defaultValue="1") int start,
 		@RequestParam(value = "rows", required = false, defaultValue="12") int rows,
 		@RequestParam(value = "sort", required = false) String sort,
 		@RequestParam(value = "wskey", required = true) String wskey
 	) {
 		long usageLimit = 0;
 		try{
 			usageLimit = apiService.findByID(wskey).getUsageLimit();
 			if(apiLogger.getRequestNumber(wskey) > usageLimit){
 				throw new LimitReachedException();
 			}
 		} catch (DatabaseException e){
 			
 		} catch (LimitReachedException e){
 			return null;
 		}
 		KmlResponse response = new KmlResponse();
 		Query query = new Query(q);
 		query.setRefinements("edm_place_latLon:[* TO *]");
 		try {
 			ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
 			response.document.extendedData.totalResults.value = Long.toString(resultSet.getResultSize());
 			response.document.extendedData.startIndex.value = Integer.toString(start);
 			response.setItems(resultSet.getResults());
 			apiLogger.saveApiRequest(wskey, query.getQuery(), RecordType.SEARCH, "kml");
 		} catch (SolrTypeException e) {
 //			ApiError error = new ApiError();
 //			error.error = e.getMessage();
 //			return error;
 		}
 		return response;
 	}
 
 	@RequestMapping(value = "/v2/opensearch.rss", produces= MediaType.APPLICATION_XML_VALUE) //, produces = "?rss?")
 	public @ResponseBody RssResponse openSearchRss(
 		@RequestParam(value = "searchTerms", required = true) String q,
 		@RequestParam(value = "startIndex", required = false, defaultValue="1") int start,
 		@RequestParam(value = "count", required = false, defaultValue="12") int count,
 		@RequestParam(value = "sort", required = false) String sort
 	) {
 		try {
 			
 			Query query = new Query(q).setPageSize(count).setStart(start);
 			ResultSet<BriefBean> resultSet = searchService.search(BriefBean.class, query);
 			RssResponse rss = new RssResponse();
 			Channel channel = rss.channel;
 			channel.totalResults.value = resultSet.getResultSize();
 			channel.startIndex.value = start;
 			channel.itemsPerPage.value = count;
 			channel.query.searchTerms = q;
 			channel.query.startPage = start;
 			for (BriefBean bean : resultSet.getResults()) {
 				Item item = new Item();
 				item.guid = bean.getId();
 				item.title = bean.getTitle()[0];
 				item.link = bean.getId();
 				log.info("item: " + item);
 				channel.items.add(item);
 			}
 			return rss;
 		} catch (SolrTypeException e) {
 			return null;
 		}
 	}
 
 	@RequestMapping(value = "/v2/suggestions.json")//, produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody ApiResponse suggestionsJson(
 		@RequestParam(value = "query", required = true) String query,
 		@RequestParam(value = "rows", required = false, defaultValue="10") int count,
 		@RequestParam(value = "phrases", required = false, defaultValue="false") boolean phrases // 0, no, false, 1 yes, true
 	) {
 		log.info("phrases: " + phrases);
 		Suggestions response = new Suggestions(null, "suggestions.json");
 		try {
 			response.items = searchService.suggestions(query, count);
 			response.itemsCount = response.items.size();
 		} catch (SolrTypeException e) {
 			return new ApiError(null, "suggestions.json", e.getMessage());
 		}
 		return response;
 	}
 
 	private void logException(Exception e) {
 		StringBuilder sb = new StringBuilder(e.getClass().getName());
 		sb.append(": ").append(e.getMessage()).append("\n");
 		StackTraceElement[] trace = e.getStackTrace();
 		for (StackTraceElement el : trace) {
 			sb.append(String.format("%s:%d %s()\n", el.getClassName(), el.getLineNumber(), el.getMethodName()));
 		}
 		log.severe(sb.toString());
 	}
	
	private String getPortalUrl() {
		if (portalUrl == null) {
			StringBuilder sb = new StringBuilder(portalServer);
			if (!portalServer.endsWith("/") && !portalName.startsWith("/")) {
				sb.append("/");
			}
			sb.append(portalName);
			portalUrl = sb.toString();
		}
		return portalUrl;
	}
 }
