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
 package nl.surfnet.bod.web.noc;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import nl.surfnet.bod.event.LogEvent;
 import nl.surfnet.bod.service.AbstractFullTextSearchService;
 import nl.surfnet.bod.service.LogEventService;
 import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
 import nl.surfnet.bod.web.security.Security;
 
 import org.springframework.data.domain.Sort;
 import org.springframework.data.domain.Sort.Direction;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.google.common.collect.Lists;
 
 @Controller
 @RequestMapping(value = "/noc/" + LogEventController.PAGE_URL)
 public class LogEventController extends AbstractSearchableSortableListController<LogEvent, LogEvent> {
   public static final String PAGE_URL = "logevents";
   static final String MODEL_KEY = "list";
 
   @Resource
   private LogEventService logEventService;
 
   @Override
   protected String getDefaultSortProperty() {
     return "created";
   }
 
   @Override
   protected Direction getDefaultSortOrder() {
     return Direction.DESC;
   }
 
   @Override
   protected String listUrl() {
     return "noc/logevents";
   }
 
   @Override
   protected List<LogEvent> list(int firstPage, int maxItems, Sort sort, Model model) {
     List<LogEvent> logEvents = Lists.newArrayList();
 
     if (Security.isSelectedNocRole()) {
       logEvents = logEventService.findAll(firstPage, maxItems, sort);
     }
     return logEvents;
   }
 
   @Override
   protected long count() {
     return logEventService.count();
   }
 
   @Override
   protected Class<LogEvent> getEntityClass() {
     return LogEvent.class;
   }
 
   @Override
   protected AbstractFullTextSearchService<LogEvent, LogEvent> getFullTextSearchableService() {
     return logEventService;
   }
 
 }
