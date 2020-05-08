 /* Copyright 2009 British Broadcasting Corporation
    Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.query.v2;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.application.query.ApplicationConfigurationFetcher;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
 import org.atlasapi.persistence.servlet.ContentNotFoundException;
 import org.atlasapi.persistence.servlet.RequestNs;
 import org.atlasapi.query.content.parser.ApplicationConfigurationIncludingQueryBuilder;
 import org.atlasapi.query.content.parser.QueryStringBackedQueryBuilder;
 import org.atlasapi.query.content.parser.WebProfileDefaultQueryAttributesSetter;
 import org.atlasapi.remotesite.FetchException;
 import org.atlasapi.remotesite.NoMatchingAdapterException;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;
 
 /**
  * Controller to handle the query interface to UriPlay.
  *
  * @author Robert Chatley (robert@metabroadcast.com)
  * @author Lee Denison (lee@metabroadcast.com)
  */
 @Controller
 public class AnyTypeFetchingController {
 
 	private static final String VIEW = "contentModel";
 	
 	private final KnownTypeQueryExecutor executor;
 	private final ApplicationConfigurationIncludingQueryBuilder builder;
    private final ApplicationConfigurationFetcher configFetcher;
 
 	public AnyTypeFetchingController(KnownTypeQueryExecutor queryExecutor, ApplicationConfigurationFetcher configFetcher) {
 		this.executor = queryExecutor;
        this.configFetcher = configFetcher;
 		this.builder = new ApplicationConfigurationIncludingQueryBuilder(new QueryStringBackedQueryBuilder(new WebProfileDefaultQueryAttributesSetter()), configFetcher) ;
 	}
 
 	@RequestMapping(method=RequestMethod.GET)
 	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		
 		try {
 			List<Content> found = Lists.newArrayList();
 			
 			found.addAll(executor.executeBrandQuery(builder.build(request, Brand.class)));
 			found.addAll(executor.executeItemQuery(builder.build(request, Item.class)));
			
			// TODO: This is bad and there should be a better way of fixing it
			Maybe<ApplicationConfiguration> conf = configFetcher.configurationFor(request);
			if (conf.hasValue() && conf.requireValue().precedenceEnabled() && found.isEmpty()) {
			    found.addAll(executor.executePlaylistQuery(builder.build(request, Playlist.class)));
			}
 		
 			return new ModelAndView(VIEW, RequestNs.GRAPH, found);
 			
 		} catch (NoMatchingAdapterException nmae) {
 			throw new ContentNotFoundException(nmae);
 		} catch (FetchException fe) {
 			throw new ContentNotFoundException(fe);
 		} 
 	}
 }
