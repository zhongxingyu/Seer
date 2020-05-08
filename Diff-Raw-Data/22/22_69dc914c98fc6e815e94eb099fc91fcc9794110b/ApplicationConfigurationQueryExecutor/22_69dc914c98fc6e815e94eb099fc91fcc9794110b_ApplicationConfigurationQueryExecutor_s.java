 package org.atlasapi.query.content;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.atlasapi.content.criteria.AtomicQuery;
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.content.criteria.attribute.Attribute;
 import org.atlasapi.content.criteria.attribute.Attributes;
 import org.atlasapi.content.criteria.operator.Operators;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.mongo.QueryConcernsTypeDecider;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 public class ApplicationConfigurationQueryExecutor implements
 		KnownTypeQueryExecutor {
 	
 	private final KnownTypeQueryExecutor delegate;
 
 	public ApplicationConfigurationQueryExecutor(KnownTypeQueryExecutor delegate) {
 		this.delegate = delegate;
 	}		
 		
 	@Override
 	public List<Item> executeItemQuery(ContentQuery query) {
 		return delegate.executeItemQuery(queryForItems(query));
 	}
 
 	@Override
 	public List<Playlist> executePlaylistQuery(ContentQuery query) {
 		return delegate.executePlaylistQuery(queryForPlaylists(query));
 	}
 
 	@Override
 	public List<Brand> executeBrandQuery(ContentQuery query) {
 		return delegate.executeBrandQuery(queryForBrands(query));
 	}
 
 	private ContentQuery queryForItems(ContentQuery query) {
 		AtomicQuery atom = mergeAttribute(Attributes.VERSION_PROVIDER, query);
 
 		query.setSoftConstraints(ImmutableSet.of(atom));
 		
 		Iterable<AtomicQuery> queryAtoms = ImmutableSet.<AtomicQuery>of(mergeAttribute(Attributes.ITEM_PUBLISHER,query));
 		return ContentQuery.joinTo(query, new ContentQuery(queryAtoms));
 	}
 	
 	
 	private ContentQuery queryForBrands(ContentQuery query) {
 		Iterable<AtomicQuery> softs = ImmutableList.of((AtomicQuery)
 			mergeAttribute(Attributes.VERSION_PROVIDER, query),
			mergeAttribute(Attributes.ITEM_PUBLISHER,query)
 		);
 		
 		if(!QueryConcernsTypeDecider.concernsItemOrBelow(query)) {
			query.setSoftConstraints(softs);
 			Iterable<AtomicQuery> queryAtoms = ImmutableSet.of((AtomicQuery)
 				mergeAttribute(Attributes.BRAND_PUBLISHER, query)
 			);
 			return ContentQuery.joinTo(query, new ContentQuery(queryAtoms));
		} else {
			query.setSoftConstraints(Iterables.concat(softs, ImmutableSet.<AtomicQuery>of(mergeAttribute(Attributes.BRAND_PUBLISHER, query))));
			return query;
		}
 	}
 	
 	private ContentQuery queryForPlaylists(ContentQuery query) {
 		Iterable<AtomicQuery> softs = ImmutableList.of((AtomicQuery)
 			mergeAttribute(Attributes.VERSION_PROVIDER, query),
 			mergeAttribute(Attributes.ITEM_PUBLISHER,query),
			mergeAttribute(Attributes.BRAND_PUBLISHER, query)
 		);
 		
 		query.setSoftConstraints(softs);
 		
 		Iterable<AtomicQuery> queryAtoms = ImmutableSet.<AtomicQuery>of(mergeAttribute(Attributes.PLAYLIST_PUBLISHER, query));
 		return ContentQuery.joinTo(query, new ContentQuery(queryAtoms));
 	}
 
 	private AtomicQuery mergeAttribute(Attribute<?> attr, ContentQuery query){
 		Set<Publisher> configPublishers = query.getConfiguration().getIncludedPublishers();
 		Map<Attribute<?>, List<?>> operandMap = query.operandMap();
 		
 		Set<?> values;
 		List<?> queryValues = operandMap.get(attr);
 		if (queryValues == null) {
 			values = configPublishers;
 		} else {
 			Set<Publisher> pubIntersection = Sets.intersection(configPublishers, ImmutableSet.copyOf(queryValues));
 			values = pubIntersection.isEmpty() ? configPublishers : pubIntersection;
 		}
 		
 		return attr.createQuery(Operators.EQUALS, values);
 	}
 }
