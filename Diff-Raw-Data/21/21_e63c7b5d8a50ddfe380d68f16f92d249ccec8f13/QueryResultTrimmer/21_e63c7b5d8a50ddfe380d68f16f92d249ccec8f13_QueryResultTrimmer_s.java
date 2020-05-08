 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.persistence.content.mongo;
 
import static org.atlasapi.persistence.content.mongo.QueryConcernsTypeDecider.concernsType;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.media.entity.Version;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.Maybe;
 
 /**
  * This class filters associations ({@link Version}s, {@link Encoding}s etc) that don't match the query.
  * Since searches are performed on denormalised representations of {@link Item}s, search results may include false
  * positives, which are filtered by this class.
  * 
  * @author John Ayres (john@metabroadcast.com)
  */
 public class QueryResultTrimmer {
 	
 	public List<Item> trimItems(Iterable<Item> items, ContentQuery query, boolean removeItemsThatDontMatch) {
 		List<Item> trimmed = Lists.newArrayListWithCapacity(Iterables.size(items));
 		for (Item item : items) {
 			if (trimItem(item, query) || !removeItemsThatDontMatch) {
 				trimmed.add(item);
 			}
 		}
 		return trimmed;
 	}
 	
 	/**
 	 * Trims associations that don't match the query
 	 * @return true if the item matches, false if the item should not be included in the result set 
 	 */
 	private boolean trimItem(Item item, ContentQuery query) {
 		Maybe<Set<Version>> versions = trimVersions(item.getVersions(), query);
 		item.setVersions(versions.valueOrDefault(Sets.<Version>newHashSet()));
 		if (versions.isNothing()) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private Maybe<Set<Version>> trimVersions(Iterable<Version> versions, ContentQuery query) {
 		final ContentQuery softQuery = new ContentQuery(query.getSoftConstraints());
 		versions = Iterables.filter(versions, new Predicate<Version>() {
 			@Override
 			public boolean apply(Version version) {
 				return check(softQuery, version);
 			}
 		});
 		if (Iterables.isEmpty(versions) && !concernsType(query, Version.class, Encoding.class, Location.class)) {
 			return Maybe.just(Collections.<Version>emptySet());
 		}
 		Set<Version> trimmedVersions = Sets.newHashSet();
 		for (Version version : versions) {
 			if (check(query,version)) {
 				Maybe<Set<Encoding>> trimmedEncodings = trimEncodings(version.getManifestedAs(), query);
 				if (trimmedEncodings.hasValue()) {
 					version.setManifestedAs(trimmedEncodings.requireValue());
 					trimmedVersions.add(version);
 				}
 			}
 		}
 		return nothingIfEmpty((Iterable)trimmedVersions);
 	}
 
 	@SuppressWarnings("unchecked")
 	private Maybe<Set<Encoding>> trimEncodings(Set<Encoding> encodings, ContentQuery query) {
 		if (encodings.isEmpty() && !concernsType(query, Encoding.class, Location.class)) {
 			return Maybe.just(Collections.<Encoding>emptySet());
 		}
 		Set<Encoding> trimmedEncodings = Sets.newHashSetWithExpectedSize(encodings.size());
 		for (Encoding encoding : encodings) {
 			if (check(query, encoding)) {
 				Maybe<Set<Location>> trimmedLocations = trimLocations(encoding.getAvailableAt(), query);
 				if (trimmedLocations.hasValue()) {
 					encoding.setAvailableAt(trimmedLocations.requireValue());
 					trimmedEncodings.add(encoding);
 				}
 			}
 		}
 		return nothingIfEmpty((Iterable)trimmedEncodings);
 	}
 
 	@SuppressWarnings("unchecked")
 	private Maybe<Set<Location>> trimLocations(Set<Location> locations, ContentQuery query) {
 		if (locations.isEmpty() && concernsType(query, Location.class)) {
 			return Maybe.<Set<Location>>just((Set)ImmutableSet.<Location>of());
 		}
 		Set<Location> trimmedLocations = Sets.newHashSetWithExpectedSize(locations.size());
 		for (Location location : locations) {
 			if (check(query, location)) {
 				trimmedLocations.add(location);
 			}
 		}
 		return nothingIfEmpty((Iterable)trimmedLocations);
 	}
 
 	public List<Brand> trimBrands(Iterable<Brand> brands, ContentQuery query, boolean removeItemsThatDontMatch) {
 		List<Brand> trimmed = Lists.newArrayListWithCapacity(Iterables.size(brands));
 		for (Brand brand : brands) {
 			if (trimBrand(brand, query) || !removeItemsThatDontMatch) {
 				trimmed.add(brand);
 			}
 		}
 		return trimmed;
 	}
 	
 	private boolean trimBrand(Playlist brand, ContentQuery query) {
 		Maybe<List<Item>> items = trimBrandItems(brand.getItems(), query);
 		brand.setItems(items.valueOrDefault(ImmutableList.<Item>of()));
 		if (items.isNothing()) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Maybe<List<Item>> trimBrandItems(Iterable<Item> items, ContentQuery query) {
 		final ContentQuery softQuery = new ContentQuery(query.getSoftConstraints());
 		items = Iterables.filter(items, new Predicate<Item>() {
 			@Override
 			public boolean apply(Item item) {
 				return check(softQuery, item);
 			}
 		});
 		if (Iterables.isEmpty(items) && !concernsType(query, Item.class, Version.class, Encoding.class, Location.class)) {
 			return Maybe.just(Collections.<Item>emptyList());
 		}
 		List<Item> trimmedItems = Lists.newArrayListWithExpectedSize(Iterables.size(items));
 		for (Item item : items) {
 			if (check(query,item)) {
 				Maybe<Set<Version>> trimmedVersions = trimVersions(item.getVersions(), query);
 				if (trimmedVersions.hasValue()) {
 					item.setVersions(trimmedVersions.requireValue());
 					trimmedItems.add(item);
 				}
 			}
 		}
 		return nothingIfEmpty((Iterable)trimmedItems);
 	}
 
 	public List<Playlist> trimPlaylists(Iterable<Playlist> playlists,ContentQuery query, boolean removeItemsThatDontMatch) {
 		List<Playlist> trimmed = Lists.newArrayListWithCapacity(Iterables.size(playlists));
 		for (Playlist playlist : playlists) {
 			if (trimPlaylist(playlist, query) || !removeItemsThatDontMatch) {
 				trimmed.add(playlist);
 			}
 		}
 		return trimmed;
 	}
 
 	private boolean trimPlaylist(Playlist playlist, final ContentQuery query) {
 		Maybe<List<Playlist>> subLists;
 		if (!playlist.getPlaylists().isEmpty()) {
 			subLists = trimSubPlaylists(playlist.getPlaylists(), query);
 		} else {
 			if (concernsType(query, Brand.class, Item.class, Version.class, Encoding.class, Location.class)){
 				subLists = Maybe.nothing();
 			} else {
 				subLists = Maybe.just(Collections.<Playlist>emptyList());
 			}
 		}
 		
 		Maybe<List<Item>> items = trimBrandItems(playlist.getItems(), query);
 		
 		playlist.setItems((items.valueOrDefault(ImmutableList.<Item>of())));
 		playlist.setPlaylists(subLists.valueOrDefault(ImmutableList.<Playlist>of()));
 		
		if (items.isNothing() && subLists.isNothing()) {
			return false;
		} else {
 			return true;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Maybe<List<Playlist>> trimSubPlaylists(Iterable<Playlist> playlists, ContentQuery query) {
 		final ContentQuery softQuery = new ContentQuery(query.getSoftConstraints());
 		playlists = Iterables.filter(playlists, new Predicate<Playlist>() {
 			@Override
 			public boolean apply(Playlist item) {
 				return check(softQuery, item);
 			}
 		});
		if (Iterables.isEmpty(playlists) && !concernsType(query, Item.class, Version.class, Encoding.class, Location.class)) {
 			return Maybe.just(Collections.<Playlist>emptyList());
 		}
 		List<Playlist> trimmedPlaylists = Lists.newArrayListWithExpectedSize(Iterables.size(playlists));
 		for (Playlist playlist : playlists) {
 			if (check(query,playlist) && trimPlaylist(playlist, query)) {
 				Maybe<List<Item>> trimmedItems = trimBrandItems(playlist.getItems(), query);
 				if (trimmedItems.hasValue()) {
 					playlist.setItems(trimmedItems.requireValue());
 					trimmedPlaylists.add(playlist);
 				}
 			}
 		}
 		return nothingIfEmpty((Iterable)trimmedPlaylists);
 	}
 	
 	private boolean check(ContentQuery query, Version version) {
 		return new InMemoryQueryResultChecker(version).check(query);
 	}
 	
 	private boolean check(ContentQuery query, Encoding encoding) {
 		return new InMemoryQueryResultChecker(encoding).check(query);
 	}
 	
 	private boolean check(ContentQuery query, Location location) {
 		return new InMemoryQueryResultChecker(location).check(query);
 	}
 	
 	private boolean check(ContentQuery query, Item item) {
 		return new InMemoryQueryResultChecker(item).check(query);
 	}
 	
 	private boolean check(ContentQuery query, Playlist playlist) {
 		return new InMemoryQueryResultChecker(playlist).check(query);
 	}
 
 	private static <T> Maybe<Iterable<T>> nothingIfEmpty(Iterable<T> elems) {
 		return Iterables.isEmpty(elems) ? Maybe.<Iterable<T>>nothing() : Maybe.just(elems);
 	}
 }
