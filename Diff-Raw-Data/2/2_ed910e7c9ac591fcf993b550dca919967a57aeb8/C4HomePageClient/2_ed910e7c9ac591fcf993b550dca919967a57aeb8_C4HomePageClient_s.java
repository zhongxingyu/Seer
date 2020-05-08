 /* Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.remotesite.channel4;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.atlasapi.remotesite.HttpClients;
 import org.atlasapi.remotesite.html.HtmlNavigator;
 import org.jaxen.JaxenException;
 import org.jdom.Element;
 
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.http.SimpleHttpClient;
 
 /**
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class C4HomePageClient implements RemoteSiteClient<BrandListingPage> {
 
	private static final String MOST_POPULAR_SECTION_TITLE = "Hot on Channel 4";
 
 	private static final String MOST_POPULAR_URI_FRAGMENT = "programmes/4od/most-popular";
 	
 	private static final String FOUR_OD_PROGRAMMES_PAGE = "http://www.channel4.com/programmes/4od";
 	
 	private final SimpleHttpClient client;
 
 	public C4HomePageClient(SimpleHttpClient client) {
 		this.client = client;
 	}
 
 	public C4HomePageClient() {
 		this(HttpClients.screenScrapingClient());
 	}
 	
 	public BrandListingPage get(String uri) throws Exception {
 		
 		HtmlNavigator html = new HtmlNavigator(client.getContentsOf(FOUR_OD_PROGRAMMES_PAGE));
 
 		if (uri.endsWith(MOST_POPULAR_URI_FRAGMENT)) {
 			return new BrandListingPage(extractMostPopularBrandsFrom(html));
 		} else {
 			return new BrandListingPage(extractHighlightBrandsFrom(html));
 		}
 
 	}
 
 	private List<HtmlBrandSummary> extractHighlightBrandsFrom(HtmlNavigator html) throws JaxenException {
 		return getBrandSummariesFrom(html, MOST_POPULAR_SECTION_TITLE, false);
 	}
 
 	private List<HtmlBrandSummary> extractMostPopularBrandsFrom(HtmlNavigator html) throws JaxenException {
 		return getBrandSummariesFrom(html, MOST_POPULAR_SECTION_TITLE, true);
 	}
 
 	private List<HtmlBrandSummary> getBrandSummariesFrom(HtmlNavigator html, String section, boolean matchMostPopularTitle) throws JaxenException {
 		
 		List<HtmlBrandSummary> brandSummaries = Lists.newArrayList();
 		
 		List<Element> itemSets = html.allElementsMatching("//li[@class='fourOnDemandSet']");
 		
 		for (Element itemSet : itemSets) {
 			
 			Element heading = html.firstElementOrNull("./h2[text() = '" + section + "']", itemSet);
 			
 			if ((heading == null) == matchMostPopularTitle) { continue; }
 			
 			List<Element> highlightBrands = html.allElementsMatching("./ul/li", itemSet);
 			
 			
 			for (Element brand : highlightBrands) {
 				
 				Element brandTitle = html.firstElementOrNull("./h3/a/span[@class='title']", brand);
 				String title = brandTitle.getText().trim();
 				
 				String brandId = brandIdFrom(brand, html);
 				
 				// Sometimes metagroups are in the hightlights, e.g. http://www.channel4.com/programmes/themes/comedy-on-4
 				// we ignore these for now
 				if (brandId == null) {
 					continue;
 				}
 				
 				HtmlBrandSummary summary = new HtmlBrandSummary().withTitle(title).withId(brandId);
 				
 				brandSummaries.add(summary);
 			}
 		}
 		
 		return brandSummaries;
 	}
 
 	private String brandIdFrom(Element brand, HtmlNavigator html) {
 		
 		Element brandPageLink = html.firstElementOrNull("./h3/a", brand);
 		String brandPageUri = brandPageLink.getAttributeValue("href");
 		
 		Pattern brandUriIdPattern = Pattern.compile("/programmes/(.+)/4od.*");
 		
 		Matcher brandIdMatcher = brandUriIdPattern.matcher(brandPageUri);
 		
 		String brandId = null;
 		if (brandIdMatcher.find()) {
 			brandId = brandIdMatcher.group(1);
 		}
 		return brandId;
 	}
 
 }
