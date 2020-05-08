 /*
  * This file is part of AMEE.
  *
  * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package com.amee.service.data;
 
 import com.amee.domain.LocaleHolder;
 import com.amee.domain.cache.CacheableFactory;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.sheet.Choice;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.Date;
 import java.util.List;
 
 public class DrillDownFactory implements CacheableFactory {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private DrillDownDAO drillDownDao;
     private DataCategory dataCategory;
     private Date startDate;
     private Date endDate;
     private List<Choice> selections;
     private List<Choice> drillDownChoices;
     private String key;
 
     private DrillDownFactory() {
         super();
     }
 
     public DrillDownFactory(
             DrillDownDAO drillDownDao,
             DataCategory dataCategory,
             Date startDate,
             Date endDate,
             List<Choice> selections,
             List<Choice> drillDownChoices) {
         this();
         this.drillDownDao = drillDownDao;
         this.dataCategory = dataCategory;
         this.startDate = startDate;
         this.endDate = endDate;
         this.selections = selections;
         this.drillDownChoices = drillDownChoices;
     }
 
     // TODO: give choices from itemValueDefinition priority?
     public Object create() {
         log.debug("create() cache: " + getCacheName() + " key: " + getKey());
         // have we reached the end of the choices list?
         if (drillDownChoices.size() > 0) {
             // get DataItem value choice list
             return drillDownDao.getDataItemValueChoices(
                     dataCategory,
                     drillDownChoices.get(0).getName(), selections, startDate,
                     endDate);
         } else {
             // get DataItem UID choice list
             return drillDownDao.getDataItemUIDChoices(
                     dataCategory,
                     selections, startDate,
                     endDate);
         }
     }
 
     public String getKey() {
         if (key == null) {
             StringBuilder keyBuilder = new StringBuilder();
             keyBuilder.append("DrillDown_");
             keyBuilder.append(dataCategory.getUid());
             for (Choice selection : selections) {
                keyBuilder.append("_SL_");
                 if (StringUtils.isNotBlank(selection.getValue())) {
                     keyBuilder.append(selection.getValue().hashCode());
                } else {
                    keyBuilder.append("BLANK");
                 }
             }
             if (startDate != null) {
                 keyBuilder.append("_SD_");
                 keyBuilder.append(startDate.getTime());
             }
             if (endDate != null) {
                 keyBuilder.append("_ED_");
                 keyBuilder.append(endDate.getTime());
             }
             keyBuilder.append("__L__");
             keyBuilder.append(LocaleHolder.getLocale());
 
             key = keyBuilder.toString();
         }
         return key;
     }
 
     public String getCacheName() {
         return "DrillDownChoices";
     }
 }
