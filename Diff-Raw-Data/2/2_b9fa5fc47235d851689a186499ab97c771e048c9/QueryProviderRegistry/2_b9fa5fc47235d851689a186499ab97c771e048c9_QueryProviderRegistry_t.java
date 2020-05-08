 // © Maastro Clinic, 2013
 package nl.maastro.eureca.aida.search.zylabpatisclient;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.ServiceLoader;
 import java.util.Set;
 import java.util.WeakHashMap;
 import javax.xml.namespace.QName;
 import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
 import org.apache.lucene.search.Query;
 
 /**
  * Use all registered {@link QueryProvider}s to resolve query IDs to queries.
  * 
  * <p><i>NOTE: Although {@code QueryProviderRegistry} implements {@link QueryProvider}
  * defines, do <em>not</em> register it as a service provider to avoid infinite
  * recursion.  As an extra precaution against infinite recursion 
  * {@code QueryProviderRegistry} will not call itself.</i></p>
  * 
  * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
  */
 public class QueryProviderRegistry implements QueryProvider {
 	private final ServiceLoader<QueryProvider> detectedQueryProviders;
 	private final List<QueryProvider> manualQueryProviders = new ArrayList<>();
 	private final WeakHashMap<QName, QueryProvider> queryCache = new WeakHashMap<>();
 	private Set<QName> queryIDs = null;
 
 	public QueryProviderRegistry() {
 		detectedQueryProviders = ServiceLoader.load(QueryProvider.class);
 	}
 
 	/**
 	 * Retrieve all available query IDs that the combined {@link QueryProvider}s
 	 * provide.
 	 * 
 	 * @see QueryProvider#getQueryIds() 
 	 * 
 	 * @return	an immutable {@link Set} of {@link QName}s identifying provided
 	 * 		queries.
 	 */
 	@Override
 	public Collection<QName> getQueryIds() {
 		if(queryIDs == null) {
 			Set<QName> tmp = new HashSet<>();
 			List<Iterator<QueryProvider>> sources = getAllProviderCollectionIterators();
 			for (Iterator<QueryProvider> it : sources) {
 				addProvidedQueryIds(tmp, it);
 			}
 			queryIDs = Collections.unmodifiableSet(tmp);
 		}
 		return queryIDs;
 	}
 
 	/**
 	 * Forward to {@link QueryProvider#hasString(javax.xml.namespace.QName)} of
 	 * the {@link QueryProvider provider} that provides {@code id}.
 	 * 
 	 * @see QueryProvider#hasString(javax.xml.namespace.QName) 
 	 * 
 	 * @param id	the {@link QName} that identifies the query
 	 * 
 	 * @return	<ul><li>{@code true}, this provider can provide the query in
 	 * 			a string format; or</li>
 	 * 		<li>{@code false}, this provider can not provide the query in a 
 	 * 			string format, i.e. the provider has the query only as
 	 * 			{@link org.apache.lucene.search.Query} or the query is unknown
 	 * 			to this provider.</li></ul>
 	 */
 	@Override
 	public boolean hasString(QName id) {
 		try {
 			return findProvider(id).hasString(id);
 		} catch (NoSuchElementException ex) {
 			return false;
 		}
 	}
 
 	/**
 	 * Forward to {@link QueryProvider#hasObject(java.net.URI)} of the
 	 * {@link QueryProvider provider} that provides {@code id}.
 	 * 
 	 * @see QueryProvider#hasObject(java.net.URI) 
 	 * 
 	 * @param id	the {@link QName} that identifies the query
 	 * 
 	 * @return	<ul><li>{@code true}, this provider can provide the query as a
 	 * 			Lucene {@code Query}-object; or</li>
 	 * 		<li>{@code false}, this provides can not provide the query as a 
 	 * 			{@code Query}-object, i.e. the provider has the query only in
 	 * 			String format or the query is unknown to this provider.
 	 * 		</li></ul>
 	 */
 	@Override
 	public boolean hasObject(QName id) {
 		try {
 			return findProvider(id).hasObject(id);
 		} catch (NoSuchElementException ex) {
 			return false;
 		}
 	}
 
 	/**
 	 * Forward to {@link QueryProvider#getAsString(javax.xml.namespace.QName)} of the 
 	 * {@link QueryProvider provider} that provides {@code id}.
 	 * 
 	 * @see QueryProvider#getAsString(javax.xml.namespace.QName) 
 	 * 
 	 * @param id	the URI that identifies the query
 	 * 
 	 * @return	the string representation of the query, in a format that 
 	 * 		{@link org.vle.aid.lucene.SearcherWS} accepts.
 	 * 
 	 * @throws NoSuchElementException	when the {@link QueryProvider} of 
 	 * 		{@code id} can not provide the query represented as string; or
 	 * 		when no provider provides {@code id}.
 	 */
 	@Override
 	public String getAsString(final QName id) throws NoSuchElementException {
 		return findProvider(id).getAsString(id);
 	}
 
 	/**
 	 * Forward to {@link QueryProvider#getAsObject(javax.xml.namespace.QName)} of the
 	 * {@link QueryProvider provider} that provides {@code id}.
 	 * 
 	 * @see QueryProvider#getAsObject(java.net.URI) 
 	 * 
 	 * @param id	the URI that identifies the query
 	 * 
 	 * @return	the {@link org.apache.lucene.search.Query} that corresponds to
 	 * 		{@code id}
 	 * 
 	 * @throws NoSuchElementException	when the {@link QueryProvider} of 
 	 * 		{@code id} can not provide the query as a {@code Query}-object; or
 	 * 		when no provider provides(id).
 	 */
 	@Override
 	public Query getAsObject(final QName id) throws NoSuchElementException {
 		return findProvider(id).getAsObject(id);
 	}
 
 	/**
 	 * Manually register {@code instance} as a {@link QueryProvider}.  Manually
 	 * registering a {@code QueryProvider} allows registering providers that
 	 * cannot be constructed be a default constructor, for example {@link Config}.
 	 * 
 	 * @param instance 
 	 */
 	public void registerProvider(final QueryProvider instance) {
 		manualQueryProviders.add(instance);
 		queryIDs = null;
 	}
 
 	/**
 	 * Retrieve the {@link QueryProvider} that provides the query identified by
 	 * {@code id}.
 	 * 
 	 * @param id	identifier of the query
 	 * 
 	 * @return		the {@link QueryProvider} that provides {@code id}
 	 * 
 	 * @throws NoSuchElementException 	when no registered {@link QueryProvider}
 	 * 		provides a query identified by {@code id}.
 	 */
 	private QueryProvider findProvider(final QName id) throws NoSuchElementException {
 		if(queryCache.containsKey(id)) {
 			return queryCache.get(id);
 		} else {
 			List<Iterator<QueryProvider>> sources = getAllProviderCollectionIterators();
 			for (Iterator<QueryProvider> it : sources) {
 				QueryProvider result = findProvider(it, id);
 				if(result != null) {
 					return result;
 				}
 			}
 			throw new NoSuchElementException(String.format(
 					"Query identified by %s not found", id.toString()));
 		}
 	}
 
 	/**
 	 * @return a list contain an iterator over {@link #manualQueryProviders} and
 	 * 		over {@link #detectedQueryProviders}, both iterators at the start 
 	 * 		of their collection.
 	 */
 	private List<Iterator<QueryProvider>> getAllProviderCollectionIterators() {
 		List<Iterator<QueryProvider>> result = Arrays.asList(
 				manualQueryProviders.iterator(),
 				detectedQueryProviders.iterator());
 		return result;
 	}
 	
 	/**
 	 * Iterate over the {@link QueryProvider}s in {@code iter} return the first
 	 * one that provides queries identified by {@id}.
 	 * 
 	 * @see #findProvider(java.net.URI) 
 	 * 
 	 * @param iter	the {@link Iterator} over a collection of {@link QueryProvider}s
 	 * 		to use.
 	 * @param id	the query id to search for.
 	 * 
 	 * @return 	<ul><li>the {@link QueryProvider} that provides {2code id}; or</li>
 	 * 		<li>{@code null}, when {@code iter} does not contain any 
 	 * 			{@code QueryProvider} that provides {@code id}.</li></ul>
 	 */
 	private QueryProvider findProvider(
 			Iterator<QueryProvider> iter, final QName id) {
 		while(iter.hasNext()) {
 			QueryProvider provider = iter.next();
 			if(provider != this ? provider.getQueryIds().contains(id) : false) {
 				queryCache.put(id, provider);
 				return provider;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Iterate over {@code iter} adding all query ids that the 
 	 * {@link QueryProvider}s in {@code iter} provide.
 	 * 
 	 * @param target	collection to add the ids to
 	 * @param iter		{@link Iterator} over a collection of 
 	 * 		{@link QueryProvider}s 
 	 */
 	private void addProvidedQueryIds(Collection<QName> target,
 			Iterator<QueryProvider> iter) {
 		
 			while(iter.hasNext()) {
 				QueryProvider provider = iter.next();
 				if(provider != this) {
					target.addAll(provider.getQueryIds());
 				}
 			}
 	}
 }
