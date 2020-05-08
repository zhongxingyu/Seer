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
 package nl.surfnet.bod.web.base;
 
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import nl.surfnet.bod.service.AbstractFullTextSearchService;
 import nl.surfnet.bod.support.ReservationFilterViewFactory;
 import nl.surfnet.bod.util.FullTextSearchResult;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.security.Security;
 
 import org.apache.lucene.queryParser.ParseException;
 import org.springframework.data.domain.Sort;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.collect.Lists;
 
 import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
 import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
 import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
 import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;
 
 /**
  * Base controller which adds full text search functionality to the
  * {@link AbstractSortableListController}
  * 
  * @param <T>
  *          DomainObject
  * @param <K>
  */
 public abstract class AbstractSearchableSortableListController<VIEW, ENTITY> extends
     AbstractSortableListController<VIEW> {
 
   @Resource
   private ReservationFilterViewFactory reservationFilterViewFactory;
 
   @RequestMapping(value = "search", method = RequestMethod.GET)
   public String search(@RequestParam(value = PAGE_KEY, required = false) Integer page,
       @RequestParam(value = "sort", required = false) String sort,
       @RequestParam(value = "order", required = false) String order, //
       @RequestParam(value = "search") String search, //
       Model model) {
 
     Sort sortOptions = prepareSortOptions(sort, order, model);
     Integer firstItem = calculateFirstPage(page);
     List<VIEW> listFromController = handleListFromController(firstItem, model, sortOptions);
 
     // Do we need to search?
     if (StringUtils.hasText(search)) {
       String translatedSearchString = mapLabelToTechnicalName(search);
 
       try {
         FullTextSearchResult<VIEW> searchResult = getFullTextSearchableService().searchForInFilteredList(
             getEntityClass(), translatedSearchString, firstItem, MAX_ITEMS_PER_PAGE, sortOptions,
             Security.getUserDetails(), listFromController);
 
         model.addAttribute(WebUtils.PARAM_SEARCH, search);
         model.addAttribute(WebUtils.DATA_LIST, searchResult.getResultList());
         model.addAttribute(WebUtils.MAX_PAGES_KEY, calculateMaxPages(searchResult.getCount()));
 
       }
       catch (ParseException e) {
         // Do not search, but show default list
         model.addAttribute(WebUtils.WARN_MESSAGES_KEY,
             Lists.newArrayList("Sorry, we could not process your search query."));
       }
     }
 
     return listUrl();
   }
 
   /**
    * Handles the list from a specific controller and places the results an the
    * model. When a search must be performed, these result will be overridden by
    * the search results.
    * 
    * @param firstResult
    *          Integer page number
    * @param model
    *          UI Model
    * @param sortOptions
    *          Sort options
    * @return List of VIEW
    */
   private List<VIEW> handleListFromController(Integer firstResult, Model model, Sort sortOptions) {
     // Get full list from specific list from controller, can filter 'normal' or
     // filtered
     List<VIEW> listFromController = list(0, Integer.MAX_VALUE, sortOptions, model);
 
     // Place nr of pages based on size on model
     model.addAttribute(WebUtils.MAX_PAGES_KEY,
         calculateMaxPages(listFromController == null ? 0 : listFromController.size()));
 
     List<VIEW> pagedList = getFullTextSearchableService().pageList(firstResult, MAX_ITEMS_PER_PAGE, listFromController);
 
     model.addAttribute(WebUtils.DATA_LIST, pagedList);
     return listFromController;
   }
 
   /**
    * Maps a search string with a label from a column to a search with a field in
    * the domain model. Can be overridden for a specific implementation, this
    * default implementation maps to the most common used fields.
    * 
    * @param search
    *          The string to search for, may contain lucene specific syntax e.g.
    *          'name:test'
    * @return String replaced string
    */
   protected String mapLabelToTechnicalName(String search) {
    search = WebUtils.replaceSearchWith(search, "team:", "virtualResourceGroup.name:");
    search = WebUtils.replaceSearchWith(search, "institute:", "physicalResourceGroup.institute.name:");
    search = WebUtils.replaceSearchWith(search, "physicalPort:", "physicalPort.id:");
     return search;
   }
 
   @SuppressWarnings("unchecked")
   @VisibleForTesting
   private Class<ENTITY> getEntityClass() {
     return (Class<ENTITY>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
   }
 
   protected abstract AbstractFullTextSearchService<VIEW, ENTITY> getFullTextSearchableService();
 }
