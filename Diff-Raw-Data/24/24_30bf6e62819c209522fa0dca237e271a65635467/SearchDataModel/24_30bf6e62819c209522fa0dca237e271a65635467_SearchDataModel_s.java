 package com.souyibao.freemarker;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.souyibao.restlet.SearchRestlet;
 import com.souyibao.search.SearchResult;
 import com.souyibao.search.TopicResult;
 import com.souyibao.shared.MedEntityManager;
 import com.souyibao.shared.MedProperties;
 import com.souyibao.shared.entity.Keyword;
 import com.souyibao.shared.util.MedUtil;
 import com.souyibao.web.util.MedWebUtil;
 
 public class SearchDataModel {
 	public static final String FLAG_COVER_ALL_TOPIC = "-1";
 	
 	private String userQuery = null;
 
 	private Collection<TopicResult> topicSearchData = null;
 	private Collection<Keyword> keywordFromQueryString = null;
 	private Collection<Keyword> queryKeywords = null;
 
 	public SearchDataModel(SearchResult result) {
 		this.topicSearchData = result.getTopicResults().values();
 		this.keywordFromQueryString = result.getKeyworsFromQuery();		
 	}
 
 	public String getUserQuery() {
 		return userQuery;
 	}
 
 	public void setUserQuery(String userQuery) {
 		this.userQuery = userQuery;
 	}
 
 	public String getWebQuery() {
 		StringBuffer result = new StringBuffer();
 
 		if ((userQuery != null) && (!userQuery.isEmpty())) {
 			result.append(userQuery);
 		}
 
 		if (queryKeywords != null) {
 			for (Keyword keyword : queryKeywords) {
 				if (result.length() > 0) {
 					result.append(" ");
 				}
 
 				result.append(keyword.getName());
 			}
 		}
 		return result.toString();
 	}
 
 	public Collection<Keyword> getQueryKeywords() {
 		return queryKeywords;
 	}
 
 	public void setQueryKeywords(Collection<Keyword> keywords) {
 		this.queryKeywords = keywords;
 	}
 
 	// keywords are to show in search pane
 	public Collection<SearchPaneKeyword> getPaneKeywords() {
 		if (queryKeywords == null) {
 			return null;
 		}
 
 		Collection<SearchPaneKeyword> paneKeywords = new ArrayList<SearchPaneKeyword>();
 
 		for (Keyword keyword : queryKeywords) {
 			SearchPaneKeyword qryKeyword = new SearchPaneKeyword(keyword);
 
 			// other case:
 			qryKeyword.setChecked(true);
 
 			paneKeywords.add(qryKeyword);
 		}
 
 		return paneKeywords;
 	}
 
 	// keywords are for diagnose guide info
 	public Collection<DiagnoseGuide> getDiagnoseGuides() {
 		List<String> guideTopicIds = MedProperties.getInstance()
 				.getTopics4Guide();
 
 		Collection<Keyword> allKeywords = new ArrayList<Keyword>();
 		if (this.queryKeywords != null) {
 			allKeywords.addAll(this.queryKeywords);
 		}
 		
 		if (this.keywordFromQueryString != null) {
 			allKeywords.addAll(this.keywordFromQueryString);
 		}
 		
 		Collection<DiagnoseGuide> result = MedWebUtil.extractDiagnoseGuides(guideTopicIds, allKeywords);
 		// get the first keyword from the topic search result
 		if (result.isEmpty() && this.topicSearchData != null) {
 			for (TopicResult topicResult : this.topicSearchData) {
 				if (guideTopicIds.contains("" + topicResult.getTopic().getId())) {
 					if (topicResult.getData().size() > 0) {
 						String keywordId = topicResult.getData().get(0).getId();
 						Keyword keyword = MedEntityManager.getInstance().getKeywordById(keywordId);
 						
 						DiagnoseGuide guide = new DiagnoseGuide(keyword);
 						result.add(guide);
 					}
 				}
 			}
 		}
 		
 		return result;
 	}
 	
 	public String getKeywordIds4Diagnose() {
 		Collection<DiagnoseGuide> guides = getDiagnoseGuides();
 				
 		if ((guides != null) && (!guides.isEmpty())) {
 			Collection<String> result = new ArrayList<String>();
 			for (DiagnoseGuide guide : guides) {
 				result.add("" + guide.getKeyword().getId());
 			}
 			
 			return MedUtil.joinString(result, ",");
 		}
 		
 		return null;
 	}
 
 	public Collection<TopicResult> getTopicSearchData() {
 		List<TopicResult> result = new ArrayList<TopicResult>();
 
 		if (this.topicSearchData != null) {
 			List<String> showedTopicIds = MedProperties.getInstance()
 					.getTopics4Showed();
 			for (TopicResult topicResult : this.topicSearchData) {
 				if (showedTopicIds
 						.contains("" + topicResult.getTopic().getId())) {
 					result.add(topicResult);
 				}
 			}
 		}
 		
 		Collections.sort(result, new TopicComparator());
 		return result;
 	}
 		
 	public String getConverageTopic(){
 		if (topicSearchData == null) {
 			return FLAG_COVER_ALL_TOPIC;
 		}
 		
 		if (topicSearchData.size() == 1) {
 			return "" + topicSearchData.iterator().next().getTopic().getId();
 		}
 		
 		return FLAG_COVER_ALL_TOPIC;
 	}
 	
 	public boolean isSingleTopicResult() {
 		if ((topicSearchData != null) && (topicSearchData.size() == 1)) {
 			return true;
 		}
 
 		return false;
 	}
 	
 	public void setTopicResultURL() {
 		if (topicSearchData == null) {
 			return;
 		}
 		
 		Collection<String> keywordIds = new ArrayList<String>();
 		if (this.queryKeywords != null) {
 			for (Keyword keyword : queryKeywords) {
 				keywordIds.add("" + keyword.getId());
 			}
 		}
 		
 		if (isSingleTopicResult()) {
 			// single topic result page
 			// need the url to back to mutil topics search page
 			for (TopicResult topicResult: topicSearchData) {
 				String url = SearchRestlet.getQueryURL(null, null, keywordIds, userQuery);
 				topicResult.setCtxUrl(url);
 			}
 		} else {
 			// mutil topics result page
 			// need the url to see more search result for one specific topic
 			for (TopicResult topicResult: topicSearchData) {
 				String url = SearchRestlet.getQueryURL("" + topicResult.getTopic().getId(), "-1", keywordIds, userQuery);
 				topicResult.setCtxUrl(url);
 			}
 		}
 	}
 
 	public boolean isEmptyTopicResult() {
 		return ((this.topicSearchData == null) || (this.topicSearchData.isEmpty()));
 	}
 	
 	private static class TopicComparator implements Comparator<TopicResult>{
 
 		@Override
 		public int compare(TopicResult o1, TopicResult o2) {
 			return (o1.getTopic().getSequence() - o2.getTopic().getSequence());			
 		}
 	}
 }
