 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.xpn.xwiki.plugin.lucene.searcherProvider;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.lucene.search.Searcher;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.observation.EventListener;
 import org.xwiki.observation.event.ActionExecutionEvent;
 import org.xwiki.observation.event.Event;
 
 
 @Component
 public class SearcherProviderManager implements ISearcherProviderRole, EventListener {
 
   private static final Logger LOGGER = LoggerFactory.getLogger(
       SearcherProviderManager.class);
 
   private Vector<SearcherProvider> allSearcherProvider;
 
   public String getName() {
     return "SearcherProviderService";
   }
 
   public List<Event> getEvents() {
     return Arrays.asList((Event)new ActionExecutionEvent("view"),
         (Event)new ActionExecutionEvent("edit"));
   }
 
   public void onEvent(Event event, Object source, Object data) {
     LOGGER.debug("onEvent called for event [" + event + "], source [" + source
         + "], data [" + data + "].");
     Vector<SearcherProvider> searcherProviderToRemove = new Vector<SearcherProvider>();
     for (SearcherProvider searcherProvider : getAllSearcherProvider()) {
       try {
         searcherProvider.disconnect();
         searcherProvider.cleanUpAllSearchResultsForThread();
       } catch (IOException exp) {
         LOGGER.error("Failed to disconnect searcherProvider from thread.", exp);
       }
       if (searcherProvider.isClosed()) {
         searcherProviderToRemove.add(searcherProvider);
       }
     }
     for (SearcherProvider removeSP : searcherProviderToRemove) {
       getAllSearcherProvider().remove(removeSP);
     }
     LOGGER.info("onEvent finish: remaining [" + getAllSearcherProvider().size()
        + "] searchProviders.");
   }
 
   public Vector<SearcherProvider> getAllSearcherProvider() {
     if (allSearcherProvider == null) {
       allSearcherProvider = new Vector<SearcherProvider>();
     }
     return allSearcherProvider;
   }
 
   public SearcherProvider createSearchProvider(Searcher[] theSearchers) {
     SearcherProvider newSearcherProvider = new SearcherProvider(theSearchers);
     getAllSearcherProvider().add(newSearcherProvider);
     return newSearcherProvider;
   }
 
 }
