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
 
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.content.criteria.BooleanAttributeQuery;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.content.criteria.DateTimeAttributeQuery;
 import org.atlasapi.content.criteria.EnumAttributeQuery;
 import org.atlasapi.content.criteria.IntegerAttributeQuery;
 import org.atlasapi.content.criteria.MatchesNothing;
 import org.atlasapi.content.criteria.QueryVisitor;
 import org.atlasapi.content.criteria.StringAttributeQuery;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Version;
 
 import com.google.common.collect.ImmutableSet;
 
 public class QueryConcernsTypeDecider {
 	
 	public static boolean concernsVersionOrBelow(final ContentQuery query){
 		return concernsType(query, ImmutableSet.of(Version.class, Location.class, Broadcast.class, Encoding.class, Policy.class));
 	}
 	
 	public static boolean concernsItemOrBelow(final ContentQuery query){
		return concernsType(query, ImmutableSet.of(Item.class, Version.class, Location.class, Broadcast.class, Encoding.class));
 	}
 	
	public static boolean concernsBrandOrBelow(final ContentQuery query){
		return concernsType(query, ImmutableSet.of(Brand.class, Item.class, Version.class, Location.class, Broadcast.class, Encoding.class));
 	}
 
 	public static boolean concernsType(final ContentQuery query, Class<? extends Identified> type) {
 		return concernsType(query, ImmutableSet.<Class<? extends Identified>>of(type));
 	}
 	
 	public static boolean concernsType(final ContentQuery query, final Iterable<Class<? extends Identified>> type) {
 		
 		final Set<Class<? extends Identified>> typeLookup = ImmutableSet.copyOf(type);
 		
 		 List<Boolean> found = query.accept(new QueryVisitor<Boolean>() {
 
 			@Override
 			public Boolean visit(IntegerAttributeQuery query) {
 				return typeLookup.contains(query.getAttribute().target());
 			}
 
 			@Override
 			public Boolean visit(StringAttributeQuery query) {
 				return typeLookup.contains(query.getAttribute().target());
 			}
 
 			@Override
 			public Boolean visit(BooleanAttributeQuery query) {
 				return typeLookup.contains(query.getAttribute().target());
 			}
 
 			@Override
 			public Boolean visit(EnumAttributeQuery<?> query) {
 				return typeLookup.contains(query.getAttribute().target());
 			}
 
 			@Override
 			public Boolean visit(DateTimeAttributeQuery query) {
 				return typeLookup.contains(query.getAttribute().target());
 			}
 
 			@Override
 			public Boolean visit(MatchesNothing noOp) {
 				return false;
 			}
 		});
 
 	    return found.contains(Boolean.TRUE);
 	}
 }
