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
 
 import static nl.surfnet.bod.web.WebUtils.FILTER_LIST;
 import static nl.surfnet.bod.web.WebUtils.FILTER_SELECT;
 import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.service.AbstractFullTextSearchService;
 import nl.surfnet.bod.service.ReservationService;
 import nl.surfnet.bod.support.ReservationFilterViewFactory;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.view.ReservationFilterView;
 import nl.surfnet.bod.web.view.ReservationView;
 
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.*;
 
 import com.google.common.collect.Lists;
 
 /**
  * Base controller for filtering and sorting {@link Reservation}s.
  *
  * @see AbstractSortableListController
  *
  */
 public abstract class AbstractFilteredReservationController extends
     AbstractSearchableSortableListController<ReservationView, Reservation> {
   public static final String FILTER_URL = "filter/";
 
   @Resource
   private ReservationService reservationService;
 
   @Resource
   private ReservationFilterViewFactory reservationFilterViewFactory;
 
   @Override
   public String getDefaultSortProperty() {
     return "startDateTime";
   }
 
   /**
    * Make sure we always filter never show all reservations at once, delegate to
    * {@link #filter(Integer, String, String, String, String, Model)}
    */
   @Override
   public String list(Integer page, String sort, String order, Model model) {
     return filter(page, sort, order, "", null, model);
   }
 
   /**
    * Make sure we always filter in combination with searching, delegate to
    * {@link #filter(Integer, String, String, String, String, Model)}
    */
   @Override
   public String search(Integer page, String sort, String order, String search, Model model) {
    return filter(page, sort, order, "", null, model);
   }
 
   @RequestMapping(value = FILTER_URL + "{filterId}/search", method = RequestMethod.GET)
   public String filterAndSearch(@RequestParam(value = PAGE_KEY, required = false) Integer page,
       @RequestParam(value = "sort", required = false) String sort,
       @RequestParam(value = "order", required = false) String order, //
       @RequestParam(value = "search", required = false) String search, //
       @PathVariable(value = "filterId") String filterId, //
       Model model) {
     return filter(page, sort, order, search, filterId, model);
   }
 
   /**
    * Retrieves a list and filters by applying the filter specified by the
    * filterId. After the user selects a filter a new Http get with the selected
    * filterId can be performed.
    *
    * @param page
    *          StartPage
    * @param sort
    *          Property names to sort on
    * @param order
    *          Order for the sort
    * @param filterId
    *          Id of the filter to apply. Nullable, will default to
    *          {@link ReservationFilterViewFactory#DEFAULT_FILTER} when empty or
    *          null.
    * @param model
    *          Model to place the state on {@link WebUtils#FILTER_SELECT} and
    *          {@link WebUtils#DATA_LIST}
    * @param request
    * @return
    */
   @RequestMapping(value = FILTER_URL + "{filterId}", method = RequestMethod.GET)
   public String filter(@RequestParam(value = PAGE_KEY, required = false) Integer page,
       @RequestParam(value = "sort", required = false) String sort,
       @RequestParam(value = "order", required = false) String order, //
       @RequestParam(value = "search", required = false) String search, //
       @PathVariable(value = "filterId") String filterId, //
       Model model) {
 
     ReservationFilterView reservationFilter;
     try {
       reservationFilter = reservationFilterViewFactory.create(filterId);
       model.addAttribute(FILTER_SELECT, reservationFilter);
     }
     catch (IllegalArgumentException e) {
       model.asMap().clear();
       return "redirect:../";
     }
 
     return super.search(page, sort, order, search, model);
   }
 
   @Override
   protected long count() {
     throw new UnsupportedOperationException("Only filtered lists are supported");
   }
 
   @ModelAttribute
   protected void populateFilter(Model model) {
 
     model.addAttribute(FILTER_LIST, determineFilters());
 
     // Remove the [list] part of the url
     model.addAttribute("baseFilterUrl", StringUtils.delete(listUrl(), WebUtils.LIST) + "/" + FILTER_URL);
   }
 
   protected ReservationService getReservationService() {
     return reservationService;
   }
 
   protected ReservationFilterViewFactory getReservationFilterViewFactory() {
     return reservationFilterViewFactory;
   }
 
   @Override
   protected AbstractFullTextSearchService<ReservationView, Reservation> getFullTextSearchableService() {
     return getReservationService();
   }
 
   @Override
   protected String mapLabelToTechnicalName(String search) {
     if (search.startsWith("team:")) {
       return search.replace("team:", "virtualResourceGroup.name:");
     }
     return super.mapLabelToTechnicalName(search);
   }
 
   private List<ReservationFilterView> determineFilters() {
     List<ReservationFilterView> filterViews = Lists.newArrayList();
 
     // Coming period
     filterViews.add(reservationFilterViewFactory.create(nl.surfnet.bod.support.ReservationFilterViewFactory.COMING));
 
     // Elapsed period
     filterViews.add(reservationFilterViewFactory.create(nl.surfnet.bod.support.ReservationFilterViewFactory.ELAPSED));
 
     filterViews.add(reservationFilterViewFactory.create(ReservationFilterViewFactory.ACTIVE));
 
     List<Integer> uniqueReservationYears = reservationService.findUniqueYearsFromReservations();
 
     filterViews.addAll(reservationFilterViewFactory.create(uniqueReservationYears));
 
     return filterViews;
   }
 
   @Override
   protected Class<Reservation> getEntityClass() {
     return Reservation.class;
   }
 
 }
