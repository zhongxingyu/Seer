 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.web.base;
 
 import static nl.surfnet.bod.web.WebUtils.MAX_ITEMS_PER_PAGE;
 import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
 import static nl.surfnet.bod.web.WebUtils.calculateFirstPage;
 import static nl.surfnet.bod.web.WebUtils.calculateMaxPages;
 import static nl.surfnet.bod.web.base.MessageManager.WARN_MESSAGES_KEY;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.List;
 
 import nl.surfnet.bod.domain.PersistableDomain;
 import nl.surfnet.bod.service.AbstractFullTextSearchService;
 import nl.surfnet.bod.util.FullTextSearchResult;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.security.Security;
 
 import org.apache.lucene.queryParser.ParseException;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.domain.Sort.Direction;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 /**
  * Base controller which adds full text search functionality to the
  * {@link AbstractSortableListController}
  */
 public abstract class AbstractSearchableSortableListController<VIEW, ENTITY extends PersistableDomain> {
 
   @RequestMapping(method = RequestMethod.GET)
   public String list(
     @RequestParam(value = PAGE_KEY, required = false) Integer page,
     @RequestParam(value = "sort", required = false) String sort,
     @RequestParam(value = "order", required = false) String order,
     Model model) {
 
     Sort sortOptions = prepareSortOptions(sort, order, model);
 
     model.addAttribute(WebUtils.MAX_PAGES_KEY, calculateMaxPages(count(model)));
     model.addAttribute(WebUtils.DATA_LIST, list(calculateFirstPage(page), MAX_ITEMS_PER_PAGE, sortOptions, model));
 
     return listUrl();
   }
 
   @RequestMapping(value = "search", method = RequestMethod.GET)
   public String search(
     @RequestParam(value = PAGE_KEY, required = false) Integer page,
     @RequestParam(value = "sort", required = false) String sort,
     @RequestParam(value = "order", required = false) String order,
     @RequestParam(value = "search") String search,
     Model model) {
 
     if (!StringUtils.hasText(search)) {
       return list(page, sort, order, model);
     }
 
     Integer firstItem = calculateFirstPage(page);
     List<Long> listFromController = getIdsOfAllAllowedEntries(model, prepareSortOptions(sort, order, model));
 
     String translatedSearchString = mapLabelToTechnicalName(search);
 
     try {
       FullTextSearchResult<ENTITY> searchResult = getFullTextSearchableService().searchForInFilteredList(
           getEntityClass(), translatedSearchString, firstItem, MAX_ITEMS_PER_PAGE, Security.getUserDetails(),
           listFromController);
 
       model.addAttribute(WebUtils.PARAM_SEARCH, search);
       model.addAttribute(WebUtils.DATA_LIST, transformToView(searchResult.getResultList(), Security.getUserDetails()));
       model.addAttribute(WebUtils.MAX_PAGES_KEY, calculateMaxPages(searchResult.getTotalCount()));
 
    } catch (ParseException e) {
       // Do not search, but show default list
      model.addAttribute(WARN_MESSAGES_KEY, Lists.newArrayList("Sorry, we could not process your search query."));
     }
 
     return listUrl();
   }
 
   protected abstract List<? extends VIEW> transformToView(List<? extends ENTITY> entities, RichUserDetails user);
 
   protected Sort prepareSortOptions(String sort, String order, Model model) {
     String sortProperty = sortProperty(sort);
     Direction sortDirection = sortDirection(order);
 
     model.addAttribute("sortProperty", sortProperty);
     model.addAttribute("sortDirection", sortDirection);
 
     return sortOrder(translateSortProperty(sortProperty), sortDirection);
   }
 
   protected abstract String listUrl();
 
   protected abstract List<? extends VIEW> list(int firstPage, int maxItems, Sort sort, Model model);
 
   protected abstract long count(Model model);
 
   protected String getDefaultSortProperty() {
     return "id";
   }
 
   protected Direction getDefaultSortOrder() {
     return Direction.ASC;
   }
 
   protected List<String> translateSortProperty(String sortProperty) {
     return ImmutableList.of(sortProperty);
   }
 
   protected Sort sortOrder(List<String> sortProperties, Direction direction) {
     return sort(direction, sortProperties);
   }
 
   private String sortProperty(String order) {
     if (Strings.emptyToNull(order) == null || !doesPropertyExist(order)) {
       return getDefaultSortProperty();
     }
 
     return order;
   }
 
   private boolean doesPropertyExist(String order) {
     try {
       ParameterizedType type;
       if (getClass().getGenericSuperclass() instanceof ParameterizedType) {
         type = (ParameterizedType) getClass().getGenericSuperclass();
      } else {
         type = (ParameterizedType) getClass().getSuperclass().getGenericSuperclass();
       }
 
       BeanInfo beanInfo = Introspector.getBeanInfo(((Class<?>) type.getActualTypeArguments()[0]));
 
       for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
         if (property.getName().equals(order)) {
           return true;
         }
       }
    } catch (IntrospectionException e) {
       return false;
     }
 
     return false;
   }
 
   private Direction sortDirection(String order) {
     if (Strings.isNullOrEmpty(order)) {
       return getDefaultSortOrder();
     }
 
     try {
       return Direction.fromString(order);
    } catch (IllegalArgumentException e) {
       return getDefaultSortOrder();
     }
   }
 
   private Sort sort(final Direction direction, List<String> properties) {
     return new Sort(direction, properties);
   }
 
   protected abstract List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort);
 
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
    search = WebUtils.replaceSearchWith(search, "physicalPort:", "physicalPort.nbiPort.nmsPortId:");

     return search;
   }
 
   @SuppressWarnings("unchecked")
   private Class<ENTITY> getEntityClass() {
     return (Class<ENTITY>) resolveParameterizedType(this.getClass(), AbstractSearchableSortableListController.class)
         .getActualTypeArguments()[1];
   }
 
   private ParameterizedType resolveParameterizedType(Type initialType, Class<?> targetType) {
     if (initialType instanceof ParameterizedType && ((ParameterizedType) initialType).getRawType().equals(targetType)) {
       return (ParameterizedType) initialType;
     }
 
     Class<?> rawType = (Class<?>) initialType;
     Type superType = rawType.getGenericSuperclass();
     if (superType != null && !superType.equals(Object.class)) {
       return resolveParameterizedType(superType, targetType);
     }
 
     throw new IllegalStateException();
   }
 
   protected abstract AbstractFullTextSearchService<ENTITY> getFullTextSearchableService();
 
 }
