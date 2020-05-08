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
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import nl.surfnet.bod.event.LogEvent;
 import nl.surfnet.bod.service.AbstractFullTextSearchService;
 import nl.surfnet.bod.service.LogEventService;
 import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.springframework.data.domain.Sort.Direction;
 
 import com.google.common.collect.ImmutableList;
 
 public abstract class AbstractLogEventController extends AbstractSearchableSortableListController<LogEvent, LogEvent> {
   public static final String PAGE_URL = "logevents";
 
   static final String MODEL_KEY = "list";
 
   @Resource
   private LogEventService logEventService;
 
   @Override
   protected String getDefaultSortProperty() {
    return "createdAt";
   }
 
   @Override
   protected Direction getDefaultSortOrder() {
     return Direction.DESC;
   }
 
   @Override
   protected List<String> translateSortProperty(String sortProperty) {
     if ("eventType".equals(sortProperty)) {
       return ImmutableList.of("eventType", "correlationId");
     }
 
     return super.translateSortProperty(sortProperty);
   }
 
   @Override
   protected AbstractFullTextSearchService<LogEvent> getFullTextSearchableService() {
     return logEventService;
   }
 
   @Override
   protected List<? extends LogEvent> transformToView(List<? extends LogEvent> entities, RichUserDetails user) {
     return entities;
   }
 
   protected final LogEventService getLogEventService() {
     return logEventService;
   }
 
 }
