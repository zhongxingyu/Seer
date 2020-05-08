 /**
  * This file is part of AMEE.
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
 package com.amee.service.path;
 
 import com.amee.domain.cache.CacheableFactory;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.environment.Environment;
 import com.amee.domain.path.PathItem;
 import com.amee.domain.path.PathItemGroup;
 import com.amee.service.data.DataService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public class EnvironmentPIGFactory implements CacheableFactory {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private DataService dataService;
     private Environment environment;
 
     private EnvironmentPIGFactory() {
         super();
     }
 
     public EnvironmentPIGFactory(DataService dataService, Environment environment) {
         this();
         this.dataService = dataService;
         this.environment = environment;
     }
 
     public Object create() {
         log.debug("create()");
         PathItemGroup pathItemGroup = null;
         List<DataCategory> dataCategories = dataService.getDataCategories(environment);
         DataCategory rootDataCategory = findRootDataCategory(dataCategories);
         if (rootDataCategory != null) {
             pathItemGroup = new PathItemGroup(new PathItem(rootDataCategory));
             while (!dataCategories.isEmpty()) {
                 log.debug("create() - dataCategories.size: " + dataCategories.size());
                 addDataCategories(pathItemGroup, dataCategories);
             }
         } else {
             log.error("create() - Root DataCategory not found.");
         }
         return pathItemGroup;
     }
 
     public String getKey() {
         return environment.getUid();
     }
 
     public String getCacheName() {
         return "EnvironmentPIGs";
     }
 
     // TODO: Why not just use a for loop, not an Iterator?
     private DataCategory findRootDataCategory(List<DataCategory> dataCategories) {
         Iterator<DataCategory> iterator = dataCategories.iterator();
         while (iterator.hasNext()) {
             DataCategory dataCategory = iterator.next();
             if (dataCategory.getDataCategory() == null) {
                 iterator.remove();
                 return dataCategory;
             }
         }
         return null;
     }
 
     private void addDataCategories(PathItemGroup pathItemGroup, List<DataCategory> dataCategories) {
         log.debug("addDataCategories()");
         PathItem parentPathItem;
         Map<String, PathItem> pathItems = pathItemGroup.getPathItems();
         Iterator<DataCategory> iterator = dataCategories.iterator();
         while (iterator.hasNext()) {
             DataCategory dataCategory = iterator.next();
             log.debug("addDataCategories() Looking up parent PathItem for DC '" + dataCategory.getPath() + "' with UID '" + dataCategory.getUid() + "'");
             parentPathItem = pathItems.get(dataCategory.getDataCategory().getUid());
             if (parentPathItem != null) {
                 log.debug("addDataCategories() Adding new child PathItem.");
                 iterator.remove();
                 parentPathItem.add(new PathItem(dataCategory));
             } else {
                 log.warn("addDataCategories() Parent PathItem not found.");
             }
         }
     }
 }
