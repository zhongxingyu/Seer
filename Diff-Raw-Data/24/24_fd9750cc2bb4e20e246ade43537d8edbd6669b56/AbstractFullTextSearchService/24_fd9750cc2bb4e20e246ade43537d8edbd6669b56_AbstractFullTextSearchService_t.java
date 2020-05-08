 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import nl.surfnet.bod.util.FullTextSearchContext;
 import nl.surfnet.bod.util.FullTextSearchResult;
 import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.apache.lucene.queryParser.ParseException;
 import org.springframework.data.domain.Sort;
 import org.springframework.util.CollectionUtils;
 
 import com.google.common.annotations.VisibleForTesting;
 
 /**
  * Service interface to abstract full text search functionality
  * 
  * @param <ENTITY>
  *          DomainObject
  */
 public abstract class AbstractFullTextSearchService<VIEW, ENTITY> {
 
   /**
    * Performs a full text search on the given searchText.
    * 
    * @param searchText
    *          String text to search for
    * @param firstResult
    *          int startItem
    * @param maxResults
    *          int max amount of items
    * @param sort
    *          {@link Sort} sorting options
    * 
    * @return List<ENTITY> result list
    * @throws ParseException
    */
   @SuppressWarnings("unchecked")
   private List<ENTITY> searchFor(Class<ENTITY> entityClass, String searchText, Sort sort) throws ParseException {
    FullTextSearchContext<ENTITY> fullTextSearchContext = new FullTextSearchContext<ENTITY>(getEntityManager(),
        entityClass);

    return fullTextSearchContext.getFullTextQueryForKeywordOnAllAnnotedFields(searchText, sort).getResultList();
   }
 
   /**
    * Performs a full text search on the given searchText and combines it with
    * the specified filteredItems. The intersection of both lists will be
    * returned.
    * 
    * @param searchText
    *          String text to search for
    * @param firstResult
    *          int startItem
    * @param maxResults
    *          int max amount of items
    * @param sort
    *          {@link Sort} sorting options
    * @param filteredItems
    *          list of already found items
    * 
    * @return List<ENTITY> result list
    * @throws ParseException
    */
   public FullTextSearchResult<VIEW> searchForInFilteredList(Class<ENTITY> entityClass, String searchText,
       int firstResult, int maxResults, Sort sort, RichUserDetails userDetails, List<VIEW> filterResult)
       throws ParseException {
 
     List<VIEW> searchResult = transformToView(searchFor(entityClass, searchText, sort), userDetails);
 
     List<VIEW> intersectedList = intersectFullTextResultAndFilterResult(searchResult, filterResult);
 
     // Determine count and page list
     int intersectedSize = intersectedList.size();
     int lastResult = Math.min(firstResult + maxResults, intersectedSize);
     return new FullTextSearchResult<VIEW>(intersectedSize, intersectedList.subList(firstResult, lastResult));
   }
 
   /**
    * Transforms the given list to the corresponding view and applies some user
    * specific restrictions if appropriate.
    * 
    * @param List
    *          <ENTITY> listToTransform list to be transformed
    * @param user
    *          {@link RichUserDetails} to check user specific restrictions
    * 
    * @return {@link List<VIEW>} transformed reservations
    */
   public abstract List<VIEW> transformToView(List<ENTITY> listToTransform, final RichUserDetails user);
 
   protected abstract EntityManager getEntityManager();
 
   /**
    * FInds objects that both list have in common. When one or both lists are
    * empty, no elements are found
    * 
    * @param searchResults
    *          List<ENTITY> List with result of filter
    * @param filterResults
    *          List<ENTITY> List with result of search
    * @return List<ENTITY> List with common objects
    */
   @VisibleForTesting
   List<VIEW> intersectFullTextResultAndFilterResult(List<VIEW> searchResults, List<VIEW> filterResults) {
    List<VIEW> intersectedList = new ArrayList<>(filterResults);
     if (!CollectionUtils.isEmpty(intersectedList)) {
      intersectedList.retainAll(searchResults);
     }
 
     return intersectedList;
   }
 
 }
