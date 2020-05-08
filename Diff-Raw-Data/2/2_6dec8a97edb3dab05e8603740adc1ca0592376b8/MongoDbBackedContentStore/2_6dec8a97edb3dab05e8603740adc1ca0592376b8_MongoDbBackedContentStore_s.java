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
 
 package org.atlasapi.persistence.content.mongo;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 import static com.metabroadcast.common.persistence.mongo.MongoBuilders.update;
 import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Clip;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Description;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.DefinitiveContentWriter;
 import org.atlasapi.persistence.content.RetrospectiveContentLister;
 import org.atlasapi.persistence.media.entity.ContentTranslator;
 import org.atlasapi.persistence.media.entity.DescriptionTranslator;
 import org.joda.time.DateTime;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.MongoConstants;
 import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
 import com.metabroadcast.common.persistence.mongo.MongoUpdateBuilder;
 import com.metabroadcast.common.query.Selection;
 import com.metabroadcast.common.time.Clock;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.SystemClock;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class MongoDbBackedContentStore extends MongoDBTemplate implements DefinitiveContentWriter, ContentResolver, RetrospectiveContentLister, AliasWriter {
 
     private final static int MAX_RESULTS = 20000;
 
     private static final Log LOG = LogFactory.getLog(MongoDbBackedContentStore.class);
 
     private final Clock clock;
     private final DBCollection itemCollection;
     private final DBCollection playlistCollection;
 
     public MongoDbBackedContentStore(DatabasedMongo mongo, Clock clock) {
         super(mongo);
 		this.clock = clock;
         itemCollection = table("items");
         playlistCollection = table("playlists");
     }
 
     public MongoDbBackedContentStore(DatabasedMongo mongo) {
     	this(mongo, new SystemClock());
 	}
 
 	@Override
     public void addAliases(String uri, Set<String> aliases) {
     	boolean wasItem = addAliasesTo(uri, aliases, itemCollection);
 		if (!wasItem) {
 			addAliasesTo(uri, aliases, playlistCollection);
 		}
     }
 
 	@SuppressWarnings("unchecked")
 	private boolean addAliasesTo(String uri, Set<String> aliases, DBCollection collection) {
 		MongoQueryBuilder findByCanonicalUri = findByCanonicalUriQuery(uri);
 		Iterable<DBObject> found = findByCanonicalUri.find(collection);
 		if (!Iterables.isEmpty(found)) {
 			DBObject dbo = Iterables.getOnlyElement(found);
 			Set<String> oldAliases = ImmutableSet.copyOf(((Iterable<String>) dbo.get(DescriptionTranslator.ALIASES)));
 			collection.update(findByCanonicalUri.build(), update().setField(DescriptionTranslator.ALIASES, Sets.union(aliases, oldAliases)).build(), false, false);
 			return true;
 		}
 		return false;
 	}
 
 	private MongoQueryBuilder findByCanonicalUriQuery(String uri) {
 		return where().fieldEquals(DescriptionTranslator.CANONICAL_URI, uri);
 	}
 
     @Override
     public void createOrUpdateItem(Item item) {
         createOrUpdateItem(item, null, false);
     }
     
     @Override
     public void createOrUpdateDefinitiveItem(Item item) {
         createOrUpdateItem(item, null, true);
     }
 
     private void createOrUpdateItem(Item item, Playlist parent, boolean markMissingItemsAsUnavailable) {
         try {
             Content content = findByUri(item.getCanonicalUri());
             if (content != null) {
                 if (!(content instanceof Item)) {
                     throw new IllegalArgumentException("Cannot update item with uri: " + item.getCanonicalUri()
                                     + "  since the old entity was not an item");
                 }
                 Item oldItem = (Item) content;
 
                 preserveContainedIn(item, oldItem);
 
                 if (!markMissingItemsAsUnavailable && oldItem instanceof Episode) {
                     writeContainedIn(itemCollection, item, item.getContainedInUris());
                     return;
                 }
 
                 preserveAliases(item, oldItem);
             } else {
                 item.setFirstSeen(clock.now());
             }
 
             addUriAndCurieToAliases(item);
             item.setLastFetched(new DateTime());
             setThisOrChildLastUpdated(item);
 
             DBObject query = new BasicDBObject();
             query.put(DescriptionTranslator.CANONICAL_URI, item.getCanonicalUri());
 
             
             if (parent == null && item instanceof Episode) {
             	Episode episode = (Episode) item;
             	Brand brand = episode.getBrand();
 				if (brand != null) {
             		Content dbContent = findByUri(brand.getCanonicalUri());
             		if (dbContent instanceof Brand) {
             			Brand dbBrand = (Brand) dbContent;
             			
             			if (!dbBrand.getItemUris().contains(item.getCanonicalUri())) {
             				dbBrand.addItem(item);
             				updateBasicPlaylistDetails(dbBrand);
             			}
             		} 
             	}
             }
             itemCollection.update(query, toDB(item), true, false);
             saveClipsFrom(item);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     @Override
     public void createOrUpdateDefinitivePlaylist(Playlist playlist) {
         createOrUpdatePlaylist(playlist, true);
     }
     
     @Override
     public void createOrUpdatePlaylist(Playlist playlist, boolean markMissingItemsAsUnavailable) {
         try {
 			Content oldContent = findByUri(playlist.getCanonicalUri());
 
 			if (oldContent != null) {
 				if (!(oldContent instanceof Playlist)) {
 					throw new IllegalStateException("Cannon saved playlist " + playlist.getCanonicalUri() + " because there's already an item with that uri");
 				}
 				Playlist oldPlaylist = (Playlist) oldContent;
 
             	
             	Set<String> oldItemUris = Sets.difference(ImmutableSet.copyOf(oldPlaylist.getItemUris()), ImmutableSet.copyOf(playlist.getItemUris()));
                 List<Item> oldItems = findItemsByCanonicalUri(Lists.newArrayList(oldItemUris));
                 if (markMissingItemsAsUnavailable) {
                     for (Item item : oldItems) {
                         for (Version version : item.getVersions()) {
                             for (Encoding encoding : version.getManifestedAs()) {
                                 for (Location location : encoding.getAvailableAt()) {
                                     location.setAvailable(false);
                                     location.setLastUpdated(new DateTime(DateTimeZones.UTC));
                                 }
                             }
                         }
                         playlist.addItem(item);
                     }
                 } 
                 
             	preservePlaylistAttributes(playlist, oldPlaylist);
             }
 
             for (Item item : playlist.getItems()) {
                 createOrUpdateItem(item, playlist, markMissingItemsAsUnavailable);
             }
 
             for (Playlist subPlaylist : playlist.getPlaylists()) {
                 createOrUpdatePlaylist(subPlaylist, markMissingItemsAsUnavailable);
             }
             
             if (playlist instanceof Brand) {
             	Brand brand = (Brand) playlist;
             	Set<Playlist> series = fullSeriesFrom(brand);
             	for (Playlist sery : series) {
             		createOrUpdatePlaylist(sery, false);
 				}
             }
 
             if (oldContent == null) {
                 playlist.setFirstSeen(new DateTime());
             }
             
             playlist.setLastFetched(new DateTime());
             setThisOrChildLastUpdated(playlist);
 
             updateBasicPlaylistDetails(playlist);
             
             saveClipsFrom(playlist);
             
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
 	private void preservePlaylistAttributes(Playlist playlist, Playlist oldPlaylist) {
 		Set<String> oldItemUris = Sets.difference(ImmutableSet.copyOf(oldPlaylist.getItemUris()), ImmutableSet.copyOf(playlist.getItemUris()));
 		
 		if (!(playlist instanceof Brand)) {
 		    for (String itemUri : oldItemUris) {
 		        removeContainedIn(itemCollection, itemUri, playlist.getCanonicalUri());
 		    }
 		}
 
 		Set<String> oldPlaylistUris = Sets.difference(ImmutableSet.copyOf(oldPlaylist.getPlaylistUris()), ImmutableSet.copyOf(playlist.getPlaylistUris()));
 		for (String oldSubPlaylistUri : oldPlaylistUris) {
 			removeContainedIn(playlistCollection, oldSubPlaylistUri, playlist.getCanonicalUri());
 		}
 		
 		preserveAliases(playlist, oldPlaylist);
 		preserveContainedIn(playlist, oldPlaylist);
 	}
     
     @Override
 	public void createOrUpdatePlaylistSkeleton(Playlist playlist) {
     	checkNotNull(playlist.getCanonicalUri(), "Cannot persist a playlist without a canonical uri");
 
     	checkArgument(checkThatSubElementsExist(playlist.getItems(), itemCollection), "Not all items exist in the database for playlist: " + playlist.getCanonicalUri());
     	checkArgument(checkThatSubElementsExist(playlist.getPlaylists(), playlistCollection), "Not all sub-playlists exist in the database for playlist: " + playlist.getCanonicalUri());
     	
     	Content previousValue = findByUri(playlist.getCanonicalUri());
     	
     	// subclasses (such as brands) should not use this method since they have more complex semantics
     	checkState(Playlist.class.equals(playlist.getClass()), "Can only persist Playlists (not subclasses)");
     	
     	if (previousValue != null) {
     		preservePlaylistAttributes(playlist, (Playlist) previousValue);
     	}
     	
     	addContainedIn(itemCollection, playlist.getItemUris(), playlist);
     	addContainedIn(playlistCollection, playlist.getPlaylistUris(), playlist);
     	
     	DateTime now = clock.now();
     	
         if (previousValue == null) {
             playlist.setFirstSeen(now);
         }
         
         playlist.setLastFetched(now);
         updateBasicPlaylistDetails(playlist);
 	}
 
 	private boolean checkThatSubElementsExist(List<? extends Content> content, DBCollection collection) {
 		ImmutableSet<String> uris = ImmutableSet.copyOf(Iterables.transform(content, Description.TO_URI));
 		return uris.size() == collection.count(where().idIn(uris).build());
 	}
 
 	private void saveClipsFrom(Content content) {
 		for (Clip clip : content.getClips()) {
 			createOrUpdateItem(clip, null, true);
 		}
 	}
 
 	private void updateBasicPlaylistDetails(Playlist playlist) {
         addUriAndCurieToAliases(playlist);
 		DBObject query = new BasicDBObject();
 		query.put(DescriptionTranslator.CANONICAL_URI, playlist.getCanonicalUri());
 		playlistCollection.update(query, toDB(playlist), true, false);
 	}
     
     private DateTime setThisOrChildLastUpdated(Playlist playlist) {
         DateTime thisOrChildLastUpdated = thisOrChildLastUpdated(null, playlist.getLastUpdated());
         for (Playlist subPlaylist: playlist.getPlaylists()) {
             thisOrChildLastUpdated = thisOrChildLastUpdated(thisOrChildLastUpdated, setThisOrChildLastUpdated(subPlaylist));
         }
         for (Item item: playlist.getItems()) {
             thisOrChildLastUpdated = thisOrChildLastUpdated(thisOrChildLastUpdated, setThisOrChildLastUpdated(item));
         }
         
         playlist.setThisOrChildLastUpdated(thisOrChildLastUpdated);
         return thisOrChildLastUpdated;
     }
     
     private DateTime setThisOrChildLastUpdated(Item item) {
         DateTime thisOrChildLastUpdated = thisOrChildLastUpdated(null, item.getLastUpdated());
         
         for (Version version: item.getVersions()) {
             thisOrChildLastUpdated = thisOrChildLastUpdated(thisOrChildLastUpdated, version.getLastUpdated());
             
             for (Broadcast broadcast: version.getBroadcasts()) {
                 thisOrChildLastUpdated = thisOrChildLastUpdated(thisOrChildLastUpdated, broadcast.getLastUpdated());
             }
             
             for (Encoding encoding: version.getManifestedAs()) {
                 thisOrChildLastUpdated = thisOrChildLastUpdated(thisOrChildLastUpdated, encoding.getLastUpdated());
                 
                 for (Location location: encoding.getAvailableAt()) {
                     thisOrChildLastUpdated = thisOrChildLastUpdated(thisOrChildLastUpdated, location.getLastUpdated());
                 }
             }
         }
         
         return thisOrChildLastUpdated;
     }
     
     private DateTime thisOrChildLastUpdated(DateTime current, DateTime candidate) {
         if (candidate != null && (current == null || candidate.isAfter(current))) {
             return candidate;
         }
         return current;
     }
 
     private Set<Playlist> fullSeriesFrom(Brand brand) {
     	Set<Playlist> series = Sets.newHashSet();
     	for (Item item : brand.getItems()) {
 			if (item instanceof Episode) {
 				Episode episode = (Episode) item;
 				Series sery = episode.getHydratedSeries();
 				if (sery != null) {
 					series.add(sery);
 				}
 			}
 		}
     	return series;
     }
 
 	private void removeContainedIn(DBCollection collection, String contentUri, String containedInUri) {
         collection.update(new BasicDBObject(DescriptionTranslator.CANONICAL_URI, contentUri),
                         new BasicDBObject("$pull", new BasicDBObject(ContentTranslator.CONTAINED_IN_URIS_KEY,
                                         containedInUri)));
     }
 
 	private void addContainedIn(DBCollection collection, Iterable<String> uris, Playlist container) {
 		collection.update(where().fieldIn(DescriptionTranslator.CANONICAL_URI, uris).build(), new BasicDBObject(MongoConstants.ADD_TO_SET, new BasicDBObject(ContentTranslator.CONTAINED_IN_URIS_KEY, container.getCanonicalUri())), false, true);
 	}
 
     private void writeContainedIn(DBCollection collection, Content content, Set<String> containedInUris) {
         MongoQueryBuilder findByUri = where().fieldEquals(DescriptionTranslator.CANONICAL_URI,
                         content.getCanonicalUri());
         MongoUpdateBuilder update = update().setField(ContentTranslator.CONTAINED_IN_URIS_KEY, containedInUris);
         collection.update(findByUri.build(), update.build());
     }
 
     private void preserveAliases(Description newDesc, Description oldDesc) {
         Set<String> oldAliases = Sets.difference(oldDesc.getAliases(), newDesc.getAliases());
 
         for (String alias : oldAliases) {
             newDesc.addAlias(alias);
         }
     }
 
     private void preserveContainedIn(Content newDesc, Content oldDesc) {
         newDesc.setContainedInUris(Sets.newHashSet(Sets.union(oldDesc.getContainedInUris(), newDesc.getContainedInUris())));
     }
 
     private void addUriAndCurieToAliases(Description desc) {
         desc.addAlias(desc.getCanonicalUri());
         desc.addAlias(desc.getCurie());
     }
 
     private void removeUriFromAliases(Description desc) {
         desc.getAliases().remove(desc.getCanonicalUri());
         desc.getAliases().remove(desc.getCurie());
     }
 
     @Override
     public Content findByUri(String uri) {
         Item item = extractCanonical(uri, findItemsByCanonicalUri(ImmutableList.of(uri)));
         if (item != null) {
             return item;
         }
         return extractCanonical(uri, findHydratedPlaylistsByCanonicalUri(ImmutableList.of(uri)));
     }
 
     private <T extends Content> T extractCanonical(String uri, Iterable<T> elems) {
     	for (T t : elems) {
 			if (uri.equals(t.getCanonicalUri()) || uri.equals(t.getCurie())) {
 				return t;
 			}
 		}
     	return null;
     }
 
 	List<Item> findItemsByCanonicalUri(Iterable<String> uris) {
         return executeItemQuery(where().fieldIn(DescriptionTranslator.CANONICAL_URI, ImmutableSet.copyOf(uris)).build(), null);
     }
 
     List<Item> executeItemQuery(DBObject query, Selection selection) {
 
         Iterator<DBObject> cur = cursor(itemCollection, query, selection);
 
         if (cur == null) {
             return Collections.emptyList();
         }
         int loaded = 0;
         List<Item> items = Lists.newArrayList();
         try {
             while (cur.hasNext()) {
                 DBObject current = cur.next();
                 items.add(toItem(current));
                 loaded++;
                 if (loaded > MAX_RESULTS) {
                     throw new IllegalArgumentException("Too many results for query");
                 }
             }
         } catch (IllegalArgumentException e) {
             LOG.error("IllegalArguementThrown: " + e.getMessage() + ". Query was: " + query + ", and Selection: " + selection);
             throw e;
         }
 
         return items;
     }
 
     private MongoDBQueryBuilder queryBuilder = new MongoDBQueryBuilder();
 
     public List<Item> itemsMatching(ContentQuery query) {
         return executeItemQuery(queryBuilder.buildItemQuery(query), query.getSelection());
     }
 
     @SuppressWarnings("unchecked")
     public List<Brand> dehydratedBrandsMatching(ContentQuery query) {
         return (List) executePlaylistQuery(queryBuilder.buildBrandQuery(query), Brand.class.getSimpleName(), query
                         .getSelection(), false);
     }
 
     public List<Playlist> dehydratedPlaylistsMatching(ContentQuery query) {
         return executePlaylistQuery(queryBuilder.buildPlaylistQuery(query), null, query.getSelection(), false);
     }
 
     List<Playlist> findHydratedPlaylistsByCanonicalUri(Iterable<String> uris) {
         return executePlaylistQuery(where().fieldIn(DescriptionTranslator.CANONICAL_URI, ImmutableSet.copyOf(uris)).build(), null, null, true);
     }
 
     List<Playlist> executePlaylistQuery(DBObject query, String type, Selection selection, boolean hydrate) {
         List<Playlist> playlists = Lists.newArrayList();
         try {
 
             if (type != null) {
                 query.put("type", type);
             }
             Iterator<DBObject> cur = cursor(playlistCollection, query, selection);
             if (cur == null) {
                 return Collections.emptyList();
             }
             int loaded = 0;
             try {
                 while (cur.hasNext()) {
                     DBObject current = cur.next();
                     playlists.add(toPlaylist(current, hydrate));
                     loaded++;
                     if (loaded > MAX_RESULTS) {
                         throw new IllegalArgumentException("Too many results for query");
                     }
                 }
             } catch (IllegalArgumentException e) {
                 LOG.error("IllegalArguementThrown: " + e.getMessage() + ". Query was: " + query + ", and Selection: " + selection);
                 throw e;
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
         return playlists;
     }
 
     @Override
     public Iterator<Item> listAllItems() {
     	return Iterators.transform(itemCollection.find(), TO_ITEM);
     }
 
 	private final Function<DBObject, Playlist> TO_PLAYIST = new Function<DBObject, Playlist>() {
 
 		@Override
 		public Playlist apply(DBObject dbo) {
 			return toPlaylist(dbo, true);
 		}
 	};
 	
 	private final Function<DBObject, Item> TO_ITEM = new Function<DBObject, Item>() {
 
 		@Override
 		public Item apply(DBObject dbo) {
 			return toItem(dbo);
 		}
 	};
     
     @Override
     public Iterator<Playlist> listAllPlaylists() {
         return Iterators.transform(playlistCollection.find(), TO_PLAYIST);
     }
 
     private Item toItem(DBObject object) {
         Item item = null;
         try {
             if (object.containsField("type") && Episode.class.getSimpleName().equals(object.get("type"))) {
                 item = fromDB(object, Episode.class);
             } else if (object.containsField("type") && Clip.class.getSimpleName().equals(object.get("type"))) {
             	item = fromDB(object, Clip.class);
             } else {
             	item = fromDB(object, Item.class);
             }
             removeUriFromAliases(item);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         return item;
     }
 
     private Playlist toPlaylist(DBObject object, boolean hydrate) {
         Playlist playlist = null;
 
         try {
             if (object.containsField("type") && Brand.class.getSimpleName().equals(object.get("type"))) {
                 playlist = fromDB(object, Brand.class);
             } else if (object.containsField("type") && Series.class.getSimpleName().equals(object.get("type"))) {
                 playlist = fromDB(object, Series.class);
             } else {
                 playlist = fromDB(object, Playlist.class);
             }
 
             if (hydrate) {
                 List<Item> items = findItemsByCanonicalUri(playlist.getItemUris());
                 playlist.setItems(items);
 
                 List<Playlist> subPlaylists = findHydratedPlaylistsByCanonicalUri(playlist.getPlaylistUris());
                 playlist.setPlaylists(subPlaylists);
             }
             removeUriFromAliases(playlist);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
         return playlist;
     }
 }
