 package com.throrinstudio.android.stackexchange.libs.social.stackexchange.mappers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.JsonToken;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.throrinstudio.android.stackexchange.libs.social.stackexchange.entities.Site;
 import com.throrinstudio.android.stackexchange.libs.social.stackexchange.entities.Site.SiteState;
 import com.throrinstudio.android.stackexchange.libs.social.stackexchange.entities.Site.SiteType;
 
public class SiteMapper extends AbstractMapper<Site>{
 
 	public SiteMapper(ObjectMapper mapper) {
 		super(mapper);
 	}
 
 
 	@Override
 	public Site map(JsonParser jp) throws JsonProcessingException, IOException 
 	{
 		Site site = new Site();
 		
 		JsonNode elem = mObjectMapper.readTree(jp);
 			
 		if(elem.has("site_type")){
 			site.setSiteType(SiteType.valueOf(elem.get("site_type").getTextValue()));
 		}
 		if(elem.has("name")){
 			site.setName(elem.get("name").getTextValue());
 		}
 		if(elem.has("logo_url")){
 			site.setLogoUrl(elem.get("logo_url").getTextValue());
 		}
 		if(elem.has("api_site_parameter")){
 			site.setApiSiteParameter(elem.get("api_site_parameter").getTextValue());
 		}
 		if(elem.has("site_url")){
 			site.setSiteUrl(elem.get("site_url").getTextValue());
 		}
 		if(elem.has("audience")){
 			site.setAudience(elem.get("audience").getTextValue());
 		}
 		if(elem.has("aliases")){
 			Iterator<JsonNode> aliases = elem.get("aliases").getElements();
 			
 			List<String> aliasesList = new ArrayList<String>();
 			
 			while(aliases.hasNext()){
 				aliasesList.add(aliases.next().getTextValue());
 			}
 			site.setAliases(aliasesList);
 		}
 		if(elem.has("site_state")){
 			site.setSiteState(SiteState.valueOf(elem.get("site_state").getTextValue()));
 		}
 		if(elem.has("styling")){
 			JsonNode stylingNode = elem.get("styling");
 			Site.Styling styling = new Site.Styling();
 			
 			if(stylingNode.has("link_color")){
 				styling.setLinkColor(stylingNode.get("link_color").getTextValue());
 			}
 			if(stylingNode.has("tag_foreground_color")){
 				styling.setTagForegroundColor(stylingNode.get("tag_foreground_color").getTextValue());
 			}
 			if(stylingNode.has("tag_background_color")){
 				styling.setTagBackgroundColor(stylingNode.get("tag_background_color").getTextValue());
 			}
 			site.setStyling(styling);
 		}
 		if(elem.has("launch_date")){
 			site.setLaunchDate(new Date(elem.get("launch_date").getLongValue()*1000));
 		}
 		if(elem.has("favicon_url")){
 			site.setFaviconUrl(elem.get("favicon_url").getTextValue());
 		}
 		if(elem.has("icon_url")){
 			site.setIconUrl(elem.get("icon_url").getTextValue());
 		}
 		if(elem.has("related_sites")){
 			
 			Iterator<JsonNode> relateds = elem.get("related_sites").getElements();
 			
 			List<Site.RelatedSite> relatedArray = new ArrayList<Site.RelatedSite>();
 			
 			while(relateds.hasNext()){
 				JsonNode relatedNode = relateds.next();
 				
 				Site.RelatedSite relatedSite = new Site.RelatedSite();
 				
 				if(relatedNode.has("name")){
 					relatedSite.setName(relatedNode.get("name").getTextValue());
 				}
 				if(relatedNode.has("site_url")){
 					relatedSite.setSiteUrl(relatedNode.get("site_url").getTextValue());
 				}
 				if(relatedNode.has("relation")){
 					relatedSite.setRelation(relatedNode.get("relation").getTextValue());
 				}
 				
 				relatedArray.add(relatedSite);
 			}
 			
 			site.setRelatedSites(relatedArray);
 			
 		}
 		if(elem.has("markdown_extensions")){
 			Iterator<JsonNode> markdown = elem.get("markdown_extensions").getElements();
 			
 			List<String> markdownList = new ArrayList<String>();
 			
 			while(markdown.hasNext()){
 				markdownList.add(markdown.next().getTextValue());
 			}
 			site.setMarkdownExtensions(markdownList);
 		}
 		if(elem.has("high_resolution_icon_url")){
 			site.setHighResolutionIconUrl(elem.get("high_resolution_icon_url").getTextValue());
 		}
 		if(elem.has("open_beta_date")){
 			site.setOpenBetaDate(new Date(elem.get("open_beta_date").getLongValue()*1000));
 		}
 		if(elem.has("closed_beta_date")){
 			site.setClosedBetaDate(new Date(elem.get("closed_beta_date").getLongValue()*1000));
 		}
 		if(elem.has("twitter_account")){
 			site.setTwitterAccount(elem.get("twitter_account").getTextValue());
 		}
 		
 		
 		
 		return site;
 	}
 
 	@Override
 	public List<Site> mapList(JsonParser jp) throws JsonProcessingException, IOException 
 	{
 		List<Site> sites = new ArrayList<Site>();
 		
 		while(jp.nextToken() != JsonToken.END_OBJECT){
			if(jp.getCurrentName() != null){	
				if(jp.getCurrentName().equals("items")){
					while(jp.nextToken() != JsonToken.END_ARRAY){
						jp.nextToken();
						sites.add(map(jp));
					}
 				}
 			}
 		}
 		
 		return sites;
 	}
 
 }
