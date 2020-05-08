 package org.atlasapi.feeds.lakeview.validation.rules;
 
 import static org.atlasapi.persistence.content.ContentCategory.ITEMS;
 import static org.atlasapi.persistence.content.listing.ContentListingCriteria.defaultCriteria;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBElement;
 
 import org.atlasapi.feeds.lakeview.validation.FeedItemStore;
 import org.atlasapi.feeds.lakeview.validation.rules.ValidationResult.ValidationResultType;
 import org.atlasapi.generated.ElementMovie;
 import org.atlasapi.generated.ElementTVEpisode;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy.Platform;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.listing.ContentLister;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Iterators;
 import com.google.inject.internal.Lists;
 
 /**
  * Validation rule to check all on-demands that we expect to be present, are.
  * 
  * @author tom
  * 
  */
 public class CompletenessValidationRule implements LakeviewFeedValidationRule {
 
 	private ContentLister contentLister;
 	private int tolerance;
	private static Pattern ONDEMAND_ID_IN_DB = Pattern.compile(".*/(\\d+)");
	private static Pattern ONDEMAND_ID_IN_OUTPUT = Pattern.compile(".*#(\\d+)");
 
 	public CompletenessValidationRule(ContentLister contentLister, int tolerance) {
 		this.contentLister = contentLister;
 		this.tolerance = tolerance;
 	}
 
 	@Override
 	public ValidationResult validate(FeedItemStore feedItemStore) {
 
 		Map<String, String> expectedOnDemands = getExpectedOnDemandsFromDatabase();
 		List<String> errors = Lists.newArrayList();
 
 		// Do a two-way comparison between feed items and expected items
 		for (Entry<String, String> expectedOnDemand : expectedOnDemands.entrySet()) {
 			boolean foundItemInFeed = false;
 			for (ElementTVEpisode feedEpisode : feedItemStore.getEpisodes()
 					.values()) {
			    Matcher matcher = ONDEMAND_ID_IN_OUTPUT
                        .matcher(getApplicationSpecificData(feedEpisode));
                if (matcher.matches() && matcher.group().equals(expectedOnDemand.getValue())) {
 					foundItemInFeed = true;
 				}
 			}
 			
 			for (ElementMovie feedMovie : feedItemStore.getMovies()
 					.values()) {
			    Matcher matcher = ONDEMAND_ID_IN_OUTPUT
                        .matcher(getApplicationSpecificData(feedMovie));
                if (matcher.matches() && matcher.group().equals(expectedOnDemand.getValue())) {
                    foundItemInFeed = true;
                }
 			}
			
 			if (!foundItemInFeed) {
 				errors.add(String.format("Valid ondemand in db, not in feed. URI %s Ondemand %s", 
 						expectedOnDemand.getKey(), expectedOnDemand.getValue()));
 			}
 		}
 
 		for (ElementTVEpisode feedEpisode : feedItemStore.getEpisodes()
 				.values()) {
 
 			if (!expectedOnDemands.values()
 					.contains(getApplicationSpecificData(feedEpisode))) {
 				errors.add("Item found in feed, not expected" + feedEpisode.getItemId());
 			}
 		}
 
 		// We expect a small number of differences because the data in the feed
 		// will be slightly stale
 		if (errors.size() < tolerance) {
 			return new ValidationResult(getRuleName(), ValidationResultType.SUCCESS,
 					String.format("%d items in feed, %d items in database", feedItemStore.getEpisodes().size(), expectedOnDemands.size()));
 		} else {
 			return new ValidationResult(getRuleName(), ValidationResultType.FAILURE,
 					String.format("%d errors: %d items in feed, %d items in database. Tolerance is %d. First missing item is %s", 
 							errors.size(), feedItemStore.getEpisodes().size(), expectedOnDemands.size(), tolerance, Iterables.getFirst(errors, null)));
 		}
 	}
 
 	private Map<String, String> getExpectedOnDemandsFromDatabase() {
 		Iterator<Item> listContent = Iterators.filter(
 				contentLister.listContent(defaultCriteria()
 						.forPublisher(Publisher.C4).forContent(ImmutableList.copyOf(ITEMS))
 						.build()), Item.class);
 
 		com.google.common.collect.ImmutableMap.Builder<String, String> expectedOnDemands = ImmutableMap.builder();
 
 		while (listContent.hasNext()) {
 			Item item = listContent.next();
 			String onDemandId = getFirstAvailableOnDemandId(item);
 
 			if (onDemandId != null) {
 				expectedOnDemands.put(item.getCanonicalUri(), onDemandId);
 			}
 		}
 
 		return expectedOnDemands.build();
 	}
 
 	private String getFirstAvailableOnDemandId(Item item) {
 		for (Version version : item.getVersions()) {
 			for (Encoding encoding : version.getManifestedAs()) {
 				for (Location location : encoding.getAvailableAt()) {
 					if(location.getPolicy() != null 
 							&& Platform.XBOX.equals(location.getPolicy().getPlatform()) 
 							&& location.getPolicy().getAvailabilityStart().isBeforeNow() 
 							&& location.getPolicy().getAvailabilityEnd().isAfterNow()) {
 
						Matcher matcher = ONDEMAND_ID_IN_DB
 								.matcher(location.getUri());
 						if (matcher.matches()) {
 							return matcher.group();
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	public static String getApplicationSpecificData(ElementTVEpisode episode) {
 		for (JAXBElement<?> restElement : episode.getRest()) {
 			if (restElement.getName().getLocalPart()
 					.equals("ApplicationSpecificData"))
 				return (String) restElement.getValue();
 		}
 		throw new RuntimeException("Element ApplicationSpecificData not found");
 	}
 	
 	public static String getApplicationSpecificData(ElementMovie movie) {
 		return movie.getApplicationSpecificData();
 	}
 
 	@Override
 	public String getRuleName() {
 		return "Completeness check";
 	}
 }
