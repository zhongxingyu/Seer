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
 
 package org.uriplay.persistence.content.mongodb;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.uriplay.content.criteria.Queries.*;
 import static org.uriplay.content.criteria.attribute.Attributes.*;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Set;
 
 import org.jherd.util.Selection;
 import org.uriplay.content.criteria.ContentQuery;
 import org.uriplay.content.criteria.attribute.Attributes;
 import org.uriplay.media.TransportType;
 import org.uriplay.media.entity.Brand;
 import org.uriplay.media.entity.Description;
 import org.uriplay.media.entity.Item;
 import org.uriplay.media.entity.Playlist;
 import org.uriplay.media.entity.Version;
 import org.uriplay.persistence.content.MongoDbBackedContentStore;
 import org.uriplay.persistence.testing.DummyContentData;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 public class MongoDbBackedQueryExecutorTest extends BaseMongoDBTest {
 	
     private DummyContentData data = new DummyContentData();
 
     private MongoDbBackedContentStore store = new MongoDbBackedContentStore(mongo(), "uriplay");
     private MongoDBQueryExecutor queryExecutor = new MongoDBQueryExecutor(store);
     
     @Override
     protected void setUp() throws Exception {
     	super.setUp();
 		
     	store.createOrUpdatePlaylist(data.eastenders, true);
     	store.createOrUpdatePlaylist(data.apprentice, true);
     	store.createOrUpdatePlaylist(data.newsNight, true);
     	store.createOrUpdatePlaylist(data.ER, true);
     	
     	store.createOrUpdateItem(data.englishForCats);
     	store.createOrUpdateItem(data.eggsForBreakfast);
     	store.createOrUpdateItem(data.everyoneNeedsAnEel);
     	
     	store.createOrUpdatePlaylist(data.goodEastendersEpisodes, false);
     	store.createOrUpdatePlaylist(data.mentionedOnTwitter, false);
     }
 
     public void testFindingItemsByUri() throws Exception {
 		checkItemQuery(equalTo(Attributes.ITEM_URI, data.englishForCats.getCanonicalUri()), data.englishForCats);
 		
 		// NewsNight is not an item, so this query should match no items
 		checkItemQueryMatchesNothing(equalTo(Attributes.ITEM_URI, data.newsNight.getCanonicalUri()));
 		
 		// check an alias
 		checkItemQuery(equalTo(Attributes.ITEM_URI, "http://dot.cotton"), data.dotCottonsBigAdventure);
 	}
     
     public void testThatIfAnItemIsFoundItIsNotFilteredOut() throws Exception {
 		ContentQuery matchesNoSubElements = and(equalTo(Attributes.ITEM_URI, data.englishForCats.getCanonicalUri()), equalTo(Attributes.LOCATION_URI, "nothing here"));
 		Item item = Iterables.getOnlyElement(queryExecutor.executeItemQuery(matchesNoSubElements));
 		assertThat(item.getCanonicalUri(), is(data.englishForCats.getCanonicalUri()));
 		assertThat(item.getVersions(), is(Collections.<Version>emptySet()));
 	}
 	
 	public void testFindingBrandsByUri() throws Exception {
 		checkBrandQuery(equalTo(Attributes.BRAND_URI, data.eastenders.getCanonicalUri()), data.eastenders);
 
 		// Eastenders is not an item
 		checkBrandQueryMatchesNothing(equalTo(Attributes.ITEM_URI, data.eastenders.getCanonicalUri()));
 
 		// Eastenders shoudn't be treated like a playlist
 		checkBrandQueryMatchesNothing(equalTo(Attributes.PLAYLIST_URI, data.eastenders.getCanonicalUri()));
 	
 		// check an alias
 		checkBrandQuery(equalTo(Attributes.BRAND_URI, "http://eastenders.bbc"), data.eastenders);
 	}
 	
 	public void testSelections() throws Exception {
 		checkBrandQuery(equalTo(Attributes.BRAND_PUBLISHER, "bbc.co.uk").withSelection(new Selection(0, 3)), data.eastenders, data.apprentice, data.newsNight);
 		checkBrandQuery(equalTo(Attributes.BRAND_PUBLISHER, "bbc.co.uk").withSelection(new Selection(1, 3)), data.apprentice, data.newsNight);
 		checkBrandQuery(equalTo(Attributes.BRAND_PUBLISHER, "bbc.co.uk").withSelection(new Selection(0, 1)), data.eastenders);
 		checkBrandQuery(equalTo(Attributes.BRAND_PUBLISHER, "bbc.co.uk").withSelection(new Selection(1, 2)), data.apprentice, data.newsNight);

		checkItemQuery(equalTo(Attributes.BRAND_URI, data.eastenders.getCanonicalUri()).withSelection(new Selection(0, 1)), data.dotCottonsBigAdventure);
		checkBrandQuery(equalTo(Attributes.PLAYLIST_URI, data.mentionedOnTwitter.getCanonicalUri()).withSelection(new Selection(0, 1)), data.eastenders);
 	}
 	
 	public void testThatIfAnBrandIsFoundByUriOrCurieItIsNotFilteredOut() throws Exception {
 		ContentQuery matchesNoSubElements = and(equalTo(Attributes.BRAND_URI, data.eastenders.getCanonicalUri()), equalTo(Attributes.LOCATION_URI, "nothing here"));
 		Brand brand = Iterables.getOnlyElement(queryExecutor.executeBrandQuery(matchesNoSubElements));
 		assertThat(brand.getCanonicalUri(), is(data.eastenders.getCanonicalUri()));
 		assertThat(brand.getItems(), is(Collections.<Item>emptyList()));
 	}
 	
 	public void testFindingPlaylistsByUri() throws Exception {
 
 		ContentQuery playlistByUri = equalTo(Attributes.PLAYLIST_URI, data.mentionedOnTwitter.getCanonicalUri());
 		Playlist found = Iterables.getOnlyElement(queryExecutor.executePlaylistQuery(playlistByUri));
 		assertThat(found.getCanonicalUri(), is(data.mentionedOnTwitter.getCanonicalUri()));
 		assertThat(found.getItems(), is(Arrays.asList(data.englishForCats, data.eggsForBreakfast)));
 		assertThat(found.getPlaylists(), is(Arrays.<Playlist>asList(data.eastenders, data.newsNight)));
 		assertThat(Iterables.get(found.getPlaylists(), 0).getItems(), is(Arrays.<Item>asList(data.dotCottonsBigAdventure, data.peggySlapsFrank)));
 		assertThat(Iterables.get(found.getPlaylists(), 1).getItems(), is(Arrays.<Item>asList(data.interviewWithMp)));
 
 		// check that when a playlist is mentioned by uri and sub-query (e.g. item.publisher) doesn't match the playlist is still returned but with empty sublists
 		ContentQuery matchesNoSubElements = and(equalTo(Attributes.PLAYLIST_URI, data.goodEastendersEpisodes.getCanonicalUri()), equalTo(Attributes.ITEM_PUBLISHER, "a made up publisher"));
 		Playlist emptyPlaylist = Iterables.getOnlyElement(queryExecutor.executePlaylistQuery(matchesNoSubElements));
 		assertThat(emptyPlaylist.getCanonicalUri(), is(data.goodEastendersEpisodes.getCanonicalUri()));
 		assertThat(emptyPlaylist.getItems(), is(Collections.<Item>emptyList()));
 		assertThat(emptyPlaylist.getPlaylists(), is(Collections.<Playlist>emptyList()));
 	}
 	
 	
 	public void testFilteringPlaylistsSubItems() throws Exception {
 		ContentQuery query = and(equalTo(Attributes.PLAYLIST_URI, data.mentionedOnTwitter.getCanonicalUri()), equalTo(Attributes.ITEM_IS_LONG_FORM, true));
 		Playlist found = Iterables.getOnlyElement(queryExecutor.executePlaylistQuery(query));
 		assertThat(found.getCanonicalUri(), is(data.mentionedOnTwitter.getCanonicalUri()));
 		assertThat(found.getItems(), is(Arrays.asList(data.englishForCats)));
 		assertThat(found.getPlaylists(), is(Arrays.<Playlist>asList(data.eastenders, data.newsNight)));
 		
 		
 		query = and(equalTo(Attributes.PLAYLIST_URI, data.mentionedOnTwitter.getCanonicalUri()), equalTo(Attributes.ITEM_URI, data.englishForCats.getCanonicalUri()), equalTo(Attributes.LOCATION_URI, "made up uri"));
 		found = Iterables.getOnlyElement(queryExecutor.executePlaylistQuery(query));
 		assertThat(found.getCanonicalUri(), is(data.mentionedOnTwitter.getCanonicalUri()));
 		Item item = Iterables.getOnlyElement(found.getItems());
 		assertThat(item.getVersions(), is(Collections.<Version>emptySet()));
 		assertThat(found.getPlaylists(), is(Collections.<Playlist>emptyList()));
 	}
 	
 	/**
 	 * {@link Playlist}s may contain brands that may contain items.  Item filters should be 
 	 * applied to the items in the brands.  
 	 */
 	public void testFilteringAPlaylistsSubBrands() throws Exception {
 		ContentQuery query = and(equalTo(Attributes.PLAYLIST_URI, data.mentionedOnTwitter.getCanonicalUri()), equalTo(Attributes.ITEM_URI, data.dotCottonsBigAdventure.getCanonicalUri()));
 		Playlist found = Iterables.getOnlyElement(queryExecutor.executePlaylistQuery(query));
 		assertThat(found.getCanonicalUri(), is(data.mentionedOnTwitter.getCanonicalUri()));
 		assertThat(found.getItems(), is(Arrays.<Item>asList()));
 
 		// 1) Newsnight should have been filtered since it does not contain 'dotCottonsBigAdventure'
 		// 2) Eastenders be present but should only contain one item: 'dotCottonsBigAdventure'
 		Brand eastenders = (Brand) Iterables.getOnlyElement(found.getPlaylists());
 		assertThat(eastenders.getCanonicalUri(), is(data.eastenders.getCanonicalUri()));
 		assertThat(Iterables.getOnlyElement(eastenders.getItems()).getCanonicalUri(), is(data.dotCottonsBigAdventure.getCanonicalUri()));
 	}
 	
 	public void testFindingItemsByCurie() throws Exception {
 		checkItemQuery(equalTo(Attributes.ITEM_CURIE, data.englishForCats.getCurie()), data.englishForCats);
 	}
 	
 	public void testBrandTitleStartsWith() throws Exception {
 		checkBrandQuery(beginning(Attributes.BRAND_TITLE, "e"), data.eastenders, data.ER);
 	}
 	
 	public void testFindingBrandsByCurie() throws Exception {
 		checkBrandQuery(equalTo(Attributes.BRAND_CURIE, data.eastenders.getCurie()), data.eastenders);
 	}
 	
 	public void testFindingItemsInABrand() throws Exception {
 		checkItemQuery(equalTo(Attributes.BRAND_URI, data.eastenders.getCanonicalUri()), data.peggySlapsFrank, data.dotCottonsBigAdventure);
 		checkItemQuery(equalTo(Attributes.BRAND_CURIE, data.eastenders.getCurie()), data.peggySlapsFrank, data.dotCottonsBigAdventure);
 	}
 	
 	public void testFindingBrandsInAPlaylist() throws Exception {
 		checkBrandQuery(equalTo(Attributes.PLAYLIST_URI, data.mentionedOnTwitter.getCanonicalUri()), data.eastenders, data.newsNight);
 		checkBrandQuery(equalTo(Attributes.PLAYLIST_CURIE, data.mentionedOnTwitter.getCurie()), data.eastenders, data.newsNight);
 	}
 	
 	public void testFindingItemsInAPlaylist() throws Exception {
 		checkItemQuery(equalTo(Attributes.PLAYLIST_URI, data.goodEastendersEpisodes.getCanonicalUri()), data.dotCottonsBigAdventure);
 		checkItemQuery(equalTo(Attributes.PLAYLIST_CURIE, data.goodEastendersEpisodes.getCurie()), data.dotCottonsBigAdventure);
 	}
 	
 	public void testFindingAvailableItems() throws Exception {
 		checkItemQuery(equalTo(Attributes.LOCATION_AVAILABLE, true), data.brainSurgery, data.englishForCats, data.eggsForBreakfast, data.everyoneNeedsAnEel, data.interviewWithMp, data.peggySlapsFrank, data.dotCottonsBigAdventure, data.sellingStuff);
 	}
 	
 	public void testFindingAvailableAndLongFromItems() throws Exception {
 		checkItemQuery(and(equalTo(Attributes.LOCATION_AVAILABLE, true),equalTo(Attributes.ITEM_IS_LONG_FORM, true)), data.englishForCats, data.dotCottonsBigAdventure, data.peggySlapsFrank, data.interviewWithMp);
 	}
 
 	public void testItemPublisherEqualsForItems() throws Exception {
 		ContentQuery query = equalTo(ITEM_PUBLISHER, "youtube.com"); 
 		checkItemQuery(query, data.englishForCats, data.eggsForBreakfast);
 		
 		query = equalTo(ITEM_PUBLISHER, "youtube.com"); 
 		checkItemQuery(query, data.englishForCats, data.eggsForBreakfast); 
 
 		query = equalTo(ITEM_PUBLISHER, "channel4.com", "youtube.com"); 
 		checkItemQuery(query, data.englishForCats, data.eggsForBreakfast,  data.brainSurgery);
 	}
 	
 	
 	public void testEpisodeNumberForItems() throws Exception {
 		ContentQuery query = equalTo(EPISODE_POSITION, 2);
 		checkItemQuery(query, data.peggySlapsFrank);
 		
 		query = lessThan(EPISODE_POSITION, 2); 
 		checkItemQuery(query, data.dotCottonsBigAdventure);
 	}
 	
 	public void testTitleEqualsForItems() throws Exception {
 		ContentQuery query = equalTo(ITEM_TITLE, "English for Cats"); 
 		checkItemQuery(query, data.englishForCats);
 	}
 	
 	public void testTransportTypeEqualsForItems() throws Exception {
 		ContentQuery query = equalTo(LOCATION_TRANSPORT_TYPE, TransportType.STREAM); 
 		checkItemQuery(query, data.dotCottonsBigAdventure, data.peggySlapsFrank);
 	}
 		
 	public void testGenreEqualsForItems() throws Exception {
 		ContentQuery query = equalTo(ITEM_GENRE, "http://uriplay.org/genres/uriplay/drama");
 		checkItemQuery(query,  data.englishForCats, data.dotCottonsBigAdventure, data.peggySlapsFrank);
 		
 		query = equalTo(ITEM_GENRE, "eels");
 		checkItemQuery(query,  data.everyoneNeedsAnEel);
 	}
 	
 
 	public void testDurationGreaterThanForItems() throws Exception {
 		ContentQuery query = greaterThan(VERSION_DURATION, 20); 
 		checkItemQuery(query,   data.dotCottonsBigAdventure, data.peggySlapsFrank, data.interviewWithMp);
 		
 		query = greaterThan(VERSION_DURATION, 30); 
 		checkItemQuery(query, data.interviewWithMp);
 	}
 	
 	public void testTransmittedBeforeForItems() throws Exception {
 		ContentQuery query = before(BROADCAST_TRANSMISSION_TIME, DummyContentData.april22nd1930); 
 		checkItemQuery(query);
 		
 		query = before(BROADCAST_TRANSMISSION_TIME, DummyContentData.april23rd); 
 		checkItemQuery(query, data.dotCottonsBigAdventure);
 	}
 	
 	public void testTransmittedAfterForItems() throws Exception {
 		ContentQuery query = after(BROADCAST_TRANSMISSION_TIME, DummyContentData.april22nd1930); 
 		checkItemQuery(query, data.englishForCats, data.eggsForBreakfast, data.everyoneNeedsAnEel, data.peggySlapsFrank, data.interviewWithMp, data.brainSurgery, data.sellingStuff);
 		
 		query = after(BROADCAST_TRANSMISSION_TIME, DummyContentData.april23rd); 
 		checkItemQuery(query, data.englishForCats, data.eggsForBreakfast, data.everyoneNeedsAnEel, data.interviewWithMp, data.brainSurgery, data.sellingStuff);
 	}
 	
 	private void checkItemQueryMatchesNothing(ContentQuery query) {
 		checkItemQuery(query);
 	}
 	
 	private void checkBrandQueryMatchesNothing(ContentQuery query) {
 		checkBrandQuery(query);
 	}
 
 	private void checkItemQuery(ContentQuery query, Item... content) {
 		assertThat(toUris(queryExecutor.executeItemQuery(query)), is(toUris(Arrays.asList(content))));
 	}
 	
 	private void checkBrandQuery(ContentQuery query, Brand... content) {
 		assertThat(toUris(queryExecutor.executeBrandQuery(query)), is(toUris(Arrays.asList(content))));
 	}
 
 	private Set<String> toUris(Collection<? extends Description> content) {
 		Set<String> uris = Sets.newHashSet();
 		for (Description description : content) {
 			uris.add(description.getCanonicalUri());
 		}
 		return uris;
 	}
 }
