 package nz.gen.wellington.guardian.android.api.openplatfrom;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import nz.gen.wellington.guardian.android.model.Section;
 import nz.gen.wellington.guardian.android.model.Tag;
 
 public class ContentApiStyleUrlBuilder {
 	
 	private static final String SEARCH_QUERY = "search";
 	private static final String SECTIONS_QUERY = "sections";
 	private static final String TAGS = "tags";
 	private static final String OR = "|";
 	
 	private String apiHost;
 	private String apiKey;
 	private String format = "xml";
 
 	private List<Tag> tags;
 	private List<Tag> contentTypes;
 	private List<String> tagTypes;
 	
 	private boolean showAll;
 	private boolean showRefinements;
 	private Integer pageSize;
 	private String searchTerm;
 	private String fromDate;
 	private String toDate;
 	
 	public ContentApiStyleUrlBuilder(String apiHost, String apiKey) {
 		this.apiHost = apiHost;
 		this.apiKey = apiKey;
 		this.tags = new ArrayList<Tag>();
 		this.contentTypes = new ArrayList<Tag>();
 		this.showAll = false;
 		this.showRefinements = false;
 	}
 	
 	public String toSearchQueryUrl() {
 		StringBuilder uri = new StringBuilder("/" + SEARCH_QUERY);
 		appendCoreParameters(uri);
 		
 		StringBuilder tagsParameter = new StringBuilder();			
 		if (!tags.isEmpty()) {
 			appendTagsToBracketedCommaSeperatedList(tagsParameter, tags);
 			
 			if (!contentTypes.isEmpty()) {
 				tagsParameter.append(",");
 				appendTagsToBracketedCommaSeperatedList(tagsParameter, contentTypes);		
 			}
 		}
 		
 		if (tagsParameter.length() > 0) {
 			String tags = tagsParameter.toString();
 			try {
 				tags = URLEncoder.encode(tagsParameter.toString(), "UTF8");
 			} catch (UnsupportedEncodingException e) {
 			}
 			uri.append("&tag=");
 			uri.append(tags);				
 		}
 		
 		if (fromDate != null) {
 			uri.append("&from-date=" + fromDate);
 		}
 		
 		if (toDate != null) {
 			uri.append("&to-date=" + toDate);
 		}
 		
 		if (searchTerm != null) {
 			uri.append("&q=" + URLEncoder.encode(searchTerm));
 		}
 		
 		return prependHost(uri.toString());
 	}
 
 	private void appendTagsToBracketedCommaSeperatedList(StringBuilder tagsParameter, List<Tag> tags) {
 		boolean isFirst = true;
 		tagsParameter.append("(");
 		for (Tag tag : tags) {
 			if (!isFirst) {
 				tagsParameter.append(OR);
 			}
 			tagsParameter.append(tag.getId());
 			isFirst = false;
 		}
 		tagsParameter.append(")");
 	}
 	
 	
 	public String toTagSearchQueryUrl() {		
 		StringBuilder uri = new StringBuilder("/" + TAGS);
 		appendCoreParameters(uri);		
 		appendAllowedTagTypes(uri);		
 		uri.append("&q=" + URLEncoder.encode(searchTerm));		
 		return prependHost(uri.toString());
 	}
 	
 
 	private void appendAllowedTagTypes(StringBuilder uri) {
 		if (!contentTypes.isEmpty()) {
 			uri.append("&type=");
 			for (Iterator<String> iterator = tagTypes.iterator(); iterator.hasNext();) {
 				uri.append(iterator.next());
 				if (iterator.hasNext()) {
 					uri.append(URLEncoder.encode(","));
 				}				
 			}
 		}
 	}
 	
 
 	public String toSectionsQueryUrl() {
 		StringBuilder uri = new StringBuilder("/" + SECTIONS_QUERY);
 		appendCoreParameters(uri);
 		return prependHost(uri.toString());
 	}
 	
 	private String prependHost(String uri) {
 		return apiHost + uri;
 	}
 
 	public void addSection(Section section) {
 		tags.add(section.getTag());
 	}
 
 	public void addTag(Tag tag) {
 		tags.add(tag);
 	}
 	
 	public void addContentType(Tag contentTypeTag) {
 		contentTypes.add(contentTypeTag);
 	}
 	
 	public void addTagType(String tagType) {
 		tagTypes.add(tagType);
 	}
 	
 	public void setPageSize(int pageSize) {
 		this.pageSize = pageSize;
 	}
 	
 	public void setShowAll(boolean showAll) {
 		this.showAll = showAll;
 	}
 	
 	public void setShowRefinements(boolean showRefinements) {
 		this.showRefinements = showRefinements;
 	}
 	
 	public void setFormat(String format) {
 		this.format = format;
 	}
 	
 	private void appendCoreParameters(StringBuilder url) {
 		url.append("?format=" + format);
 		if (pageSize != null) {
 			url.append("&page-size=" + pageSize);		
 		}
 		if (showAll) {
 			url.append("&show-fields=all");
 			url.append("&show-tags=all");
 		}
 		
 		if (apiKey != null && !apiKey.trim().equals("")) {
 			url.append("&api-key=" + apiKey);
 		}
 		
 		if (showRefinements) {
 			url.append("&show-refinements=all");
 		}
 	}
 
 	public void setSearchTerm(String searchTerm) {
 		this.searchTerm = searchTerm;
 	}
 
 	public void setFromDate(String date) {
 		this.fromDate = date;
 	}
 
 	public void setToDate(String date) {
 		this.toDate = date;		
 	}
 	
 }
