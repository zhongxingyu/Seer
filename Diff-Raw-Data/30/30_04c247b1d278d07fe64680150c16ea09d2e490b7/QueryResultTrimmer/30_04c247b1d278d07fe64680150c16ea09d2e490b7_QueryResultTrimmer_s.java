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
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.ContentGroup;
 import org.atlasapi.media.entity.Described;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
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
 	
 	public <T extends Identified> List<T> trim(Iterable<T> contents, ContentQuery query, boolean removeItemsThatDontMatch) {
 		List<T> trimmed = Lists.newArrayListWithCapacity(Iterables.size(contents));
 		for (T content : contents) {
 			if ((trim(content, query) || !removeItemsThatDontMatch) && publisherPermitted(query, (Described) content)) {
 				trimmed.add(content);
 			}
 		}
 		return trimmed;
 	}
 	
 	private boolean publisherPermitted(ContentQuery query, Described found) {
 		ApplicationConfiguration config = query.getConfiguration();
 		return config.getIncludedPublishers().contains(found.getPublisher());
 	}
 	
 	private boolean trim(Identified content, ContentQuery filter) {
 		if (content instanceof Item) {
 			return trimItem((Item) content, filter);
 		} else if (content instanceof Container<?>) {
 			return trimContainer((Container<?>) content, filter);
 		} else if (content instanceof ContentGroup) {
 			return trimGroup((ContentGroup) content, filter);
 		} else {
 			throw new IllegalArgumentException("Cannot trim content with type " + content.getClass().getSimpleName() + " as I don't know about it");
 		}
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
		if (Iterables.isEmpty(versions) && !concernsType(query, ImmutableSet.of(Version.class, Encoding.class, Location.class, Broadcast.class))) {
			return Maybe.just(Collections.<Version>emptySet());
		}
 		Set<Version> trimmedVersions = Sets.newHashSet();
 		for (Version version : versions) {
 			if (check(query, version)) {
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
		if (encodings.isEmpty() && !concernsType(query, ImmutableSet.of(Encoding.class, Location.class))) {
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
 	
 	private <T extends Item> boolean trimContainer(Container<T> brand, ContentQuery query) {
 		Maybe<List<T>> items = trimContainerSubItems(brand.getContents(), query);
 		brand.setContents(items.valueOrDefault(ImmutableList.<T>of()));
 		if (items.isNothing()) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	private boolean trimGroup(ContentGroup group, ContentQuery query) {
 		Maybe<List<Content>> items = trimContainerSubItems(group.getContents(), query);
 		group.setContents(items.valueOrDefault(ImmutableList.<Content>of()));
 		if (items.isNothing()) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 
 	@SuppressWarnings("unchecked")
 	private <T extends Content> Maybe<List<T>> trimContainerSubItems(Iterable<T> items, ContentQuery query) {
 		final ContentQuery softQuery = new ContentQuery(query.getSoftConstraints());
 		items = Iterables.filter(items, new Predicate<Content>() {
 			@Override
 			public boolean apply(Content item) {
 				if (item instanceof Container<?>) {
 					return trimContainer((Container<?>) item, softQuery);
 				} else {
 					return check(softQuery, item);
 				}
 			}
 		});
 		List<T> trimmedItems = Lists.newArrayListWithExpectedSize(Iterables.size(items));
 		for (T content : items) {
 			if (content instanceof Item) {
 				Item item = (Item) content;
 				if (check(query, item)) {
 					Maybe<Set<Version>> trimmedVersions = trimVersions(item.getVersions(), query);
 					if (trimmedVersions.hasValue()) {
 						item.setVersions(trimmedVersions.requireValue());
 						trimmedItems.add(content);
 					}
 				}
 			} else if (content instanceof Container<?>) {
 				trimmedItems.add(content);
 			}
 		}
 		if (Iterables.isEmpty(trimmedItems) && !concernsType(query, ImmutableSet.of(Item.class, Episode.class, Version.class, Encoding.class, Location.class, Broadcast.class))) {
             return Maybe.just(Collections.<T>emptyList());
         }
 		return nothingIfEmpty((Iterable) trimmedItems);
 	}
 
 	private boolean check(ContentQuery query, Version version) {
 		boolean versionMatches = new InMemoryQueryResultChecker(version).check(query);
 		if (!versionMatches) {
 			return false;
 		}
 		if (!concernsType(query, ImmutableSet.<Class<? extends Identified>>of(Broadcast.class))) {
 			return true;
 		}
 		for (Broadcast broadcast : version.getBroadcasts()) {
 			if (check(query, broadcast)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean check(ContentQuery query, Broadcast broadcast) {
 		return new InMemoryQueryResultChecker(broadcast).check(query);
 	}
 	
 	private boolean check(ContentQuery query, Encoding encoding) {
 		return new InMemoryQueryResultChecker(encoding).check(query);
 	}
 	
 	private boolean check(ContentQuery query, Location location) {
 		return new InMemoryQueryResultChecker(location).check(query);
 	}
 	
 	private boolean check(ContentQuery query, Content content) {
 		return new InMemoryQueryResultChecker(content).check(query);
 	}
 
 	private static <T> Maybe<Iterable<T>> nothingIfEmpty(Iterable<T> elems) {
 		return Iterables.isEmpty(elems) ? Maybe.<Iterable<T>>nothing() : Maybe.just(elems);
 	}
 }
