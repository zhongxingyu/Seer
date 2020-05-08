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
 
 import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
 import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
 import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
 import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;
 
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
 
 import com.google.common.collect.Lists;
 
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
 
     if (StringUtils.hasText(search)) {
       String translatedSearchString = translateSearchString(search);
 
       List<VIEW> listFromController = list(0, Integer.MAX_VALUE, sortOptions, model);
      FullTextSearchResult<VIEW> searchResult;
       try {
        searchResult = getFullTextSearchableService().searchForInFilteredList(getEntityClass(), translatedSearchString,
            calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, Security.getUserDetails(), listFromController);
 
        model.addAttribute(WebUtils.PARAM_SEARCH, translatedSearchString);
         model.addAttribute(WebUtils.DATA_LIST, searchResult.getResultList());
         model.addAttribute(WebUtils.MAX_PAGES_KEY, calculateMaxPages(searchResult.getCount()));
 
         return listUrl();
       }
       catch (ParseException e) {
         // Do not search, but show default list
        model.addAttribute(WebUtils.WARN_MESSAGES_KEY,
            Lists.newArrayList("Sorry, we could not process your search query."));
       }
     }
     model.addAttribute(WebUtils.MAX_PAGES_KEY, calculateMaxPages(count()));
     model.addAttribute(WebUtils.DATA_LIST, list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, model));
 
     return listUrl();
   }
 
   protected String translateSearchString(String search) {
     return search;
   }
 
   protected abstract Class<ENTITY> getEntityClass();
 
   protected abstract AbstractFullTextSearchService<VIEW, ENTITY> getFullTextSearchableService();
 }
