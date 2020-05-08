 package com.souyibao.restlet;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.data.MediaType;
 
 import com.souyibao.freemarker.SearchDataModel;
 import com.souyibao.search.SearchResult;
 import com.souyibao.search.ctrl.Controller;
 import com.souyibao.shared.MedEntityManager;
 import com.souyibao.shared.entity.Keyword;
 import com.souyibao.shared.entity.Topic;
 import com.souyibao.shared.entity.TopicCategory;
 import com.souyibao.shared.util.MedUtil;
 import com.souyibao.web.model.RepresentationMeta;
 import com.souyibao.web.model.SimpleStringRepresentation;
 import com.souyibao.web.util.MedWebUtil;
 
 public class SearchRestlet extends BaseRestlet {
 
 	@Override
 	public SimpleStringRepresentation processRequest(Request request, Response response) {
 		String topicid = (String)request.getAttributes().get("topicid");
 		String categoryid = (String)request.getAttributes().get("categoryid");
 		String keywordid = (String)request.getAttributes().get("keywordid");
 //		String querystring = (String)request.getAttributes().get("querystring");
 		String querystring = request.getResourceRef().getQueryAsForm().getFirstValue("qs", null);
 		
 		// query String
 		String[] queryKeywordIds = null;
 		if (keywordid != null) {
 			queryKeywordIds = keywordid.split(",");
 		}
 		
 		// topic filters
 		String[] topicFilters = null;
 		if (topicid != null) {
 			topicFilters = topicid.split(",");
 		}
 		
 		// category filters
 		String[] cateFilters = null;
 		if (categoryid != null) {
 			cateFilters = categoryid.split(",");
 		}
 		
 		Map<Topic, TopicCategory> prefCatetories = null;
 		prefCatetories = MedWebUtil.supplementPrefCategory(cateFilters);
 		
 		// decoder the query string
 		if (querystring != null) {
 			try {
 				querystring = URLDecoder.decode(querystring, "UTF-8");
 			} catch (UnsupportedEncodingException e) {
 			}
 		}
 		
 		// query data model
 		SearchDataModel dataModel = search(querystring, queryKeywordIds,
 				topicFilters, prefCatetories);
 		
 		Map<String, Object> data = new HashMap<String, Object>();
 		data.put("searchData", dataModel);
 		String outValue = this.processData(request, data);
 		
 		RepresentationMeta meta = new RepresentationMeta(MediaType.TEXT_HTML.getName(), "UTF-8");
 		// output
 		return new SimpleStringRepresentation(meta, outValue);
 	}
 	
 	private SearchDataModel search(String querystring, String[] keywordIds,
 			String[] topicIds, Map<Topic, TopicCategory> categoryFilters) {
 		// for the input parameter
 		Set<Keyword> keywords = MedEntityManager.getInstance().getKeysWithIds(
 				keywordIds);
 
 		Set<Topic> topicFilters = MedEntityManager.getInstance()
 				.getTopicsWithIds(topicIds);
 
 		SearchResult searchResult =  Controller.getSearchResult(querystring, keywords, topicFilters,
 				categoryFilters);
 		
		SearchDataModel dataModel;
		if (searchResult == null) {
			dataModel = new SearchDataModel();
		} else {
			dataModel = new SearchDataModel(searchResult);
		}

 		dataModel.setUserQuery(querystring);
 		dataModel.setQueryKeywords(keywords);
 		
 		dataModel.setTopicResultURL();
 		
 		return dataModel;
 	}
 
 	
 	public static String getQueryURL(String topicid, String categoryid,
 			Collection<String> keywordids, String userQuery) {
 		String keywordString = null;
 		if (keywordids != null) {
 			keywordString = MedUtil.joinString(keywordids, ",");
 		}
 		
 		boolean keywordEmpty = ((keywordString != null) && (keywordString.length() > 0))? false : true;
 		boolean userInputEmpty = ((userQuery != null) && (userQuery.length() > 0))? false : true;
 		
 		String path = null;
 		if (!keywordEmpty && !userInputEmpty) {
 			path = "rs/s";
 		} else if (keywordEmpty && !userInputEmpty) {
 			path = "rs/ss";
 		} else if (!keywordEmpty && userInputEmpty) {
 			path = "rs/sk";
 		}
 		
 		if (path == null) {
 			// TODO: throw exception?
 		}
 				
 		StringBuffer result = new StringBuffer();
 		result.append(path);
 		if (topicid != null) {
 			result.append("/").append(topicid);
 		}
 		if (categoryid != null) {
 			result.append("/").append(categoryid);
 		}
 		if (keywordString != null) {
 			result.append("/").append(keywordString);
 		}
 		if (userQuery != null) {
 			try {
 				String encodedString = URLEncoder.encode(userQuery, "UTF-8");
 				result.append("/").append(encodedString).append("?qs=").append(encodedString);
 			} catch (UnsupportedEncodingException e) {
 				result.append("/").append(userQuery);			
 			}
 		}
 		
 		return result.toString();
 	}
 }
