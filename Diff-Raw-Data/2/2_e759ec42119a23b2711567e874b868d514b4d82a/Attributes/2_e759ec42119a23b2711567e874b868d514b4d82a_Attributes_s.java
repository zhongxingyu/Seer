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
 
 package org.atlasapi.content.criteria.attribute;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.ContentType;
 import org.atlasapi.media.entity.Description;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.joda.time.DateTime;
 
 import com.google.common.collect.Maps;
 import com.metabroadcast.common.media.MimeType;
 
 public class Attributes {
 
 	// Simple string-valued attributes
     public static final Attribute<String> DESCRIPTION_TITLE = stringAttribute("title", Description.class);
 	public static final Attribute<String> ITEM_TITLE = stringAttribute("title", Item.class);
 	public static final Attribute<String> PLAYLIST_TITLE = stringAttribute("title", Playlist.class);
 	public static final Attribute<String> BRAND_TITLE = stringAttribute("title", Brand.class);
 	
 	public static final Attribute<String> DESCRIPTION_URI = stringAttribute("uri", "canonicalUri", Description.class);
 	public static final Attribute<String> ITEM_URI = stringAttribute("uri", "canonicalUri", Item.class);
 	public static final Attribute<String> PLAYLIST_URI = stringAttribute("uri", "canonicalUri", Playlist.class);
 	public static final Attribute<String> EPISODE_URI = stringAttribute("uri", "canonicalUri", Episode.class);
 	public static final Attribute<String> BRAND_URI = stringAttribute("uri", "canonicalUri", Brand.class);
 	public static final Attribute<String> LOCATION_URI = stringAttribute("uri", Location.class);
 	
 	public static final Attribute<Boolean> ITEM_IS_LONG_FORM = new BooleanValuedAttribute("isLongForm", Item.class).allowShortMatches();
 	
 	public static final Attribute<Enum<Publisher>> ITEM_PUBLISHER = new EnumValuedAttribute<Publisher>("publisher", Publisher.class, Item.class);
 	public static final Attribute<Enum<Publisher>> PLAYLIST_PUBLISHER = new EnumValuedAttribute<Publisher>("publisher", Publisher.class, Playlist.class);
 	public static final Attribute<Enum<Publisher>> BRAND_PUBLISHER = new EnumValuedAttribute<Publisher>("publisher", Publisher.class, Brand.class);
 	public static final Attribute<Enum<Publisher>> DESCRIPTION_PUBLISHER = new EnumValuedAttribute<Publisher>("publisher", Publisher.class, Description.class);
 	
 	public static final Attribute<Enum<MimeType>> ENCODING_DATA_CONTAINER_FORMAT = new EnumValuedAttribute<MimeType>("dataContainerFormat", MimeType.class, Encoding.class).allowShortMatches();
 	
 	public static final Attribute<Enum<ContentType>> ITEM_TYPE = new EnumValuedAttribute<ContentType>("contentType", ContentType.class, Item.class).withAlias("type"); 
 	public static final Attribute<Enum<ContentType>> BRAND_TYPE = new EnumValuedAttribute<ContentType>("contentType", ContentType.class, Brand.class).withAlias("type"); 
 	public static final Attribute<Enum<ContentType>> PLAYLIST_TYPE = new EnumValuedAttribute<ContentType>("contentType", ContentType.class, Playlist.class).withAlias("type"); 
 	
 	// Lists of strings
 	public static final Attribute<String> DESCRIPTION_GENRE = stringListAttribute("genre",  Description.class);
 	public static final Attribute<String> ITEM_GENRE = stringListAttribute("genre",  Item.class);
 	public static final Attribute<String> ITEM_TAG = stringListAttribute("tag", Item.class);
 	
 	public static final Attribute<String> PLAYLIST_GENRE = stringListAttribute("genre",  Playlist.class);
     public static final Attribute<String> PLAYLIST_TAG = stringListAttribute("tag", Playlist.class);
 	public static final Attribute<String> BRAND_GENRE = stringListAttribute("genre", Brand.class);
     public static final Attribute<String> BRAND_TAG = stringListAttribute("tag", Brand.class);
 	
 	// enums
 	public static final Attribute<Enum<TransportType>> LOCATION_TRANSPORT_TYPE = new EnumValuedAttribute<TransportType>("transportType", TransportType.class, Location.class).allowShortMatches();
 
 	// Simple integer-valued attributes
 	public static final Attribute<Integer> EPISODE_POSITION = integerAttribute("position", "episodeNumber",  Episode.class).allowShortMatches();
 	
 	public static final Attribute<Integer> EPISODE_SEASON_POSITION = integerAttribute("seasonPosition", "seriesNumber",  Episode.class).allowShortMatches();
 	
 	public static final Attribute<Integer> VERSION_DURATION = integerAttribute("duration", Version.class).allowShortMatches();
 	public static final Attribute<Enum<Publisher>> VERSION_PROVIDER = new EnumValuedAttribute<Publisher>("provider", Publisher.class, Version.class);
 
 	// Time based attributes
 	public static final Attribute<DateTime> BROADCAST_TRANSMISSION_TIME = dateTimeAttribute("transmissionTime", Broadcast.class).allowShortMatches();
 	public static final Attribute<DateTime> BROADCAST_TRANSMISSION_END_TIME = dateTimeAttribute("transmissionEndTime", Broadcast.class).allowShortMatches();
 	public static final Attribute<DateTime> BRAND_THIS_OR_CHILD_LAST_UPDATED = dateTimeAttribute("thisOrChildLastUpdated", Brand.class).allowShortMatches();
 	public static final Attribute<String> BROADCAST_ON = stringAttribute("broadcastOn", Broadcast.class).allowShortMatches();
 	
 	public static final Attribute<Boolean> LOCATION_AVAILABLE = new BooleanValuedAttribute("available", Location.class).allowShortMatches();
 
 	public static final Attribute<String> POLICY_AVAILABLE_COUNTRY = new StringValuedAttribute("availableCountries", Policy.class, true).allowShortMatches();
 	
 	private static List<Attribute<?>> ALL_ATTRIBUTES = 
 		Arrays.<Attribute<?>>asList(ITEM_TITLE, BRAND_TITLE, PLAYLIST_TITLE, DESCRIPTION_TITLE,
								    ITEM_URI, BRAND_URI, PLAYLIST_URI, DESCRIPTION_URI, LOCATION_URI, ITEM_GENRE, 
 								    ITEM_TAG, PLAYLIST_TAG, PLAYLIST_GENRE, BRAND_TAG, BRAND_GENRE,
 								    ITEM_PUBLISHER, BRAND_PUBLISHER, PLAYLIST_PUBLISHER,
 								    ITEM_TYPE, BRAND_TYPE, PLAYLIST_TYPE,
 								    BRAND_THIS_OR_CHILD_LAST_UPDATED,
 								    VERSION_DURATION,
 								    VERSION_PROVIDER,
 								    BROADCAST_TRANSMISSION_TIME,
 								    BROADCAST_ON,
 								    LOCATION_TRANSPORT_TYPE,
 								    POLICY_AVAILABLE_COUNTRY,
 								    EPISODE_POSITION,
 								    EPISODE_SEASON_POSITION,
 								    LOCATION_AVAILABLE,
 								    ENCODING_DATA_CONTAINER_FORMAT,
 								    ITEM_IS_LONG_FORM);
 	
 	public static final Map<String, Attribute<?>> lookup = lookupTable();
 	
 	public static Attribute<?> lookup(String name, Class<? extends Description> queryContext) {
 		Attribute<?> attribute = lookup.get(name);
 		if (attribute == null && name.indexOf('.') < 0) {
 			attribute = lookup.get(queryContext.getSimpleName().toLowerCase() + "." + name);
 		}
 		if (attribute == null && name.startsWith("description.")) {
 		    name = name.replace("description.", "");
 		    attribute = lookup.get(queryContext.getSimpleName().toLowerCase() + "." + name);
 		}
 		return attribute;
 	}
 
 	private static Map<String, Attribute<?>> lookupTable() {
 		Map<String, Attribute<?>> table = Maps.newHashMap();
 		
 		for (Attribute<?> attribute : ALL_ATTRIBUTES) {
 			addToTable(table, attribute.externalName(), attribute);
 			if (attribute.hasAlias()) {
 				table.put(attribute.alias(), attribute);
 			}
 		}
 		return table;
 	}
 
 	
 	private static void addToTable(Map<String, Attribute<?>> table, String key, Attribute<?> attribute) {
 		if (table.containsKey(key)) {
 			throw new IllegalArgumentException("Duplicate name: " + key);
 		}
 		table.put(key, attribute);
 		
 	}
 
 	private static StringValuedAttribute stringAttribute(String name,  String javaAttribute, Class<? extends Description> target) {
 		StringValuedAttribute attribute = new StringValuedAttribute(name, target);
 		attribute.withJavaAttribute(javaAttribute);
 		return attribute;
 	}
 	
 	private static StringValuedAttribute stringAttribute(String name, Class<? extends Description> target) {
 		return new StringValuedAttribute(name, target);
 	}
 	
 	private static IntegerValuedAttribute integerAttribute(String name,  String javaAttribute, Class<? extends Description> target) {
 		IntegerValuedAttribute attribute = new IntegerValuedAttribute(name, target);
 		attribute.withJavaAttribute(javaAttribute);
 		return attribute;
 	}
 	
 	private static IntegerValuedAttribute integerAttribute(String name, Class<? extends Description> target) {
 		return new IntegerValuedAttribute(name, target);
 	}
 	
 	private static DateTimeValuedAttribute dateTimeAttribute(String name, Class<? extends Description> target) {
 		return new DateTimeValuedAttribute(name, target);
 	}
 
 	private static StringValuedAttribute stringListAttribute(String name, Class<? extends Description> target) {
 		return new StringValuedAttribute(name, target, true);
 	}
 }
