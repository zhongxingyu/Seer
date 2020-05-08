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
 
 package org.atlasapi.query.content;
 
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.content.criteria.ContentQuery;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Schedule;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.atlasapi.persistence.system.Fetcher;
 
 import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 /**
  * Finds any uris from a given {@link ContentQuery}, fetches them using a local/remote
  * fetcher (so either from the database or from the Internet), and uses the response
  * to replace the uris given in the query with the canonical versions of each, before passing
  * the updated query on to a delegate. 
  *  
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class UriFetchingQueryExecutor implements KnownTypeQueryExecutor {
 
 	private final Fetcher<Identified> fetcher;
 	private final KnownTypeQueryExecutor delegate;
 	
 	public UriFetchingQueryExecutor(Fetcher<Identified> fetcher, KnownTypeQueryExecutor delegate) {
 		this.fetcher = fetcher;
 		this.delegate = delegate;
 	}
 	
 	@Override
 	public List<Content> discover(ContentQuery query) {
 		return delegate.discover(query);
 	}
 	
 	@Override
 	public List<Identified> executeUriQuery(Iterable<String> uris, ContentQuery query) {
 		return executeContentQuery(uris, query);
 	}
 	
 	public List<Identified> executeContentQuery(Iterable<String> uris, ContentQuery query) {
 
 		List<Identified> found = delegate.executeUriQuery(uris, query);
 		
 		Set<String> missingUris = missingUris(found, uris);
 		
 		if (missingUris.isEmpty()) {
 			return found;
 		} 
 
 		boolean foundAtLeastOneUri = false;
 		
		List<String> resolvedUris = found.isEmpty() ? Lists.<String>newArrayList() : Lists.newArrayList(Iterables.transform(found, Identified.TO_URI));
 		
 		for (String missingUri : missingUris) {
 			Identified remoteContent = fetcher.fetch(missingUri);
 			if (remoteContent != null) {
 				foundAtLeastOneUri = true;
 				resolvedUris.add(remoteContent.getCanonicalUri());
 			} else {
 				resolvedUris.add(missingUri);
 			}
 		}
 		
		// If we couldn't resolve any of the missing uris then we should just return the results of the original query
 		if (!foundAtLeastOneUri) {
 			return found;
 		}
 
 		// re-attempt the query now the missing uris have been fetched
 		return delegate.executeUriQuery(resolvedUris, query);
 	}
 	
 	private static Set<String> missingUris(Iterable<? extends Identified> content, Iterable<String> uris) {
 		return Sets.difference(ImmutableSet.copyOf(uris), urisFrom(content));
 	}
 
 	private static Set<String> urisFrom(Iterable<? extends Identified> contents) {
 		Set<String> uris = Sets.newHashSet();
 		for (Identified content : contents) {
 			uris.addAll(content.getAllUris());
 		}
 		return uris;
 	}
 
 	@Override
 	public Schedule schedule(ContentQuery query) {
 		return delegate.schedule(query);
 	}
 }
