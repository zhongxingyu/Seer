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
 
 package org.uriplay.query.uri;
 
  import org.uriplay.media.entity.Content;
import org.uriplay.persistence.content.MutableContentStore;
 import org.uriplay.persistence.system.Fetcher;
 import org.uriplay.persistence.system.RequestTimer;
 
 
 /**
  * Decorator for a {@link Fetcher} that stores the results before passing them on.
  *  
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class SavingFetcher implements Fetcher<Object> {
 
	private final Fetcher<Content> delegateFetcher;
 	private final MutableContentStore store;
 
	public SavingFetcher(Fetcher<Content> delegateFetcher, MutableContentStore store) {
 		this.delegateFetcher = delegateFetcher;
 		this.store = store;
 	}
 
 	public Object fetch(String uri, RequestTimer timer) {
 		
		Content bean = delegateFetcher.fetch(uri, timer);
 		
 		if (bean == null) { return null; }
 		
		store.createOrUpdateContent(bean, false);
 		
 		return bean;
 	}
 }
