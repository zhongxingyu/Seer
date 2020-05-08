 package nz.gen.wellington.guardian.contentapi.parsing;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import nz.gen.wellington.guardian.contentapi.cleaning.HtmlCleaner;
 import nz.gen.wellington.guardian.contentapi.dates.ContentApiDateHelper;
 import nz.gen.wellington.guardian.model.Article;
 import nz.gen.wellington.guardian.model.Refinement;
 import nz.gen.wellington.guardian.model.Section;
 import nz.gen.wellington.guardian.model.Tag;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class ContentApiStyleJSONParser {
 	
 	private HtmlCleaner htmlCleaner;
 	
 	public ContentApiStyleJSONParser() {
 		htmlCleaner = new HtmlCleaner();
 	}
 	
 	public List<Section> parseSectionsRequestResponse(String content) {
 		try {		
 			JSONObject json = new JSONObject(content.toString());
 			if (!isResponseOk(json)) {
 				return null;
 			}
 			JSONObject response = json.getJSONObject("response");
 			JSONArray results = response.getJSONArray("results");
 
 			List<Section> sections = new LinkedList<Section>();
 			for (int i = 0; i < results.length(); i++) {
 				JSONObject section = results.getJSONObject(i);
 				final String sectionName = htmlCleaner.stripHtml(section.getString("webTitle"));
 				final String id = section.getString("id");
 				sections.add(new Section(id, sectionName));
 			}
 			return sections;
 
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 	
 	
 	public List<Tag> parseTagSearchResponse(String content, Map<String, Section> sections) {
 		try {
 			JSONObject json = new JSONObject(content);
 			if (!isResponseOk(json)) {
 				return null;
 			}
 			JSONObject response = json.getJSONObject("response");
 			JSONArray results = response.getJSONArray("results");
 
 			List<Tag> tags = new LinkedList<Tag>();
 			for (int i = 0; i < results.length(); i++) {
 				JSONObject tag = results.getJSONObject(i);
 				final String id = tag.getString("id");
 				final String tagName = tag.getString("webTitle");
 				final String type = tag.getString("type");
 
 				Section section = null;
 				final String sectionId = tag.getString("sectionId");
 				if (tag.has("sectionId")) {
 					section = sections.get(sectionId);
 				}
 				tags.add(new Tag(tagName, id, section, type));			
 			}
 			return tags;
 
 		} catch (JSONException e) {
 			return null;
 		}
 	}
 	
 	
 	public boolean isResponseOk(JSONObject json) {
 		try {
 			JSONObject response = json.getJSONObject("response");
 			String status = response.getString("status");
 			return status != null && status.equals("ok");
 		} catch (JSONException e) {
 			return false;
 		}
 	}
 	
 	
 	public int extractContentItemsCount(JSONObject json) throws JSONException {
 		if (isResponseOk(json)) {
 			JSONObject jsonResponse = json.getJSONObject("response");
 			if (jsonResponse.has("total")) {
 				return jsonResponse.getInt("total");
 			}
 		}
 		return 0;
 	}
 	
 	
 	public List<Article> extractContentItems(JSONObject json, Map<String, Section> sections) throws JSONException {
 		JSONObject jsonResponse = json.getJSONObject("response");
 		JSONArray results = jsonResponse.getJSONArray("results");
 		
 		List<Article> articles = new ArrayList<Article>();
 		for (int i = 0; i < results.length(); i++) {
 			JSONObject content = results.getJSONObject(i);
 			articles.add(parseContentItem(content, sections));
 		}
 		return articles;
 	}
 	
 	public Article extractContentItem(JSONObject json, Map<String, Section> sections) throws JSONException {
 		JSONObject jsonResponse = json.getJSONObject("response");
 		JSONObject content = jsonResponse.getJSONObject("content");		
 		return parseContentItem(content, sections);
 	}
 	
 	
 	public Map<String, List<Refinement>> parseRefinementGroups(JSONObject response) throws JSONException {
 		Map<String, List<Refinement>> refinements = new HashMap<String, List<Refinement>>();
 		JSONArray refinementGroups = response.getJSONArray("refinementGroups");
 		for (int i = 0; i < refinementGroups.length(); i++) {
 			JSONObject refinementGroup = refinementGroups.getJSONObject(i);
 			extractRefinement(refinements, refinementGroup);						
 		}
 		return refinements;
 	}
 
 	
 	private void extractRefinement(Map<String, List<Refinement>> refinements, JSONObject refinementGroup) throws JSONException {
 		String type = refinementGroup.getString("type");		
 		List<Refinement> tagRefinements = refinements.get(type);
 		if (tagRefinements == null) {
 			tagRefinements = new ArrayList<Refinement>();
 			refinements.put(type, tagRefinements);
 		}
 		
 		JSONArray refinementsJSON = refinementGroup.getJSONArray("refinements");
 		for (int j = 0; j < refinementsJSON.length(); j++) {
 			JSONObject refinement = refinementsJSON.getJSONObject(j);
 			tagRefinements.add(
 					new Refinement(type, 
 						refinement.getString("id"), 
 						refinement.getString("displayName"), 
 						refinement.getString("refinedUrl"),
 						refinement.getInt("count"))
 			);
 		}
 		return;
 	}
 	
 	
 	private Article parseContentItem(JSONObject content, Map<String, Section> sections) throws JSONException {
 		Article article = new Article();
 		article.setId(getJsonStringIfPresent(content, "id"));
 		article.setPubDate(ContentApiDateHelper.parseDate(getJsonStringIfPresent(content, "webPublicationDate")));
 		article.setWebUrl(getJsonStringIfPresent(content, "webUrl"));

		article.setHeadline(getJsonStringIfPresent(content, "webTitle"));
 		
 		Section section = new Section(
			getJsonStringIfPresent(content, "sectionId"), 
			getJsonStringIfPresent(content, "sectionName"));
 		article.setSection(section);
 		
		if (content.has("fields")) {
			JSONObject fields = content.getJSONObject("fields");		
			article.setHeadline(getJsonStringIfPresent(fields, "headline"));
			article.setByline(getJsonStringIfPresent(fields, "byline"));		
			article.setStandfirst(getJsonStringIfPresent(fields, "standfirst"));
			article.setThumbnailUrl(getJsonStringIfPresent(fields, "thumbnail"));
			article.setDescription(getJsonStringIfPresent(fields, "body"));
			article.setShortUrl(getJsonStringIfPresent(fields, "shortUrl"));
		}
 		
 		if (content.has("tags")) {
 			JSONArray tags = content.getJSONArray("tags");
 			for (int i = 0; i < tags.length(); i++) {
 				JSONObject tag = tags.getJSONObject(i);				
 
 				Section tagSection = null;
 				if (tag.has("sectionId")) {
 					final String sectionId = tag.getString("sectionId");
 					tagSection = sections.get(sectionId);
 				}
 				article.addTag(new Tag(tag.getString("webTitle"), tag.getString("id"), tagSection, tag.getString("type")));
 			}
 		}
 		
 		return article;
 	}
 	
 	private String getJsonStringIfPresent(JSONObject json, String field) throws JSONException {
 		if (json.has(field)) {
 			return (String) json.get(field);
 		}
 		return null;
 	}
 
 }
