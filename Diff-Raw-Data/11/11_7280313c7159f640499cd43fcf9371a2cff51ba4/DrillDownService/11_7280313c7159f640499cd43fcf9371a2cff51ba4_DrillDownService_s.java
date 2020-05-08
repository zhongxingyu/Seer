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
 package com.amee.service.data;
 
 import com.amee.domain.cache.CacheHelper;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.ItemDefinition;
 import com.amee.domain.sheet.Choice;
 import com.amee.domain.sheet.Choices;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 @Service
 public class DrillDownService implements Serializable {
 
     @Autowired
     private DrillDownDAO drillDownDao;
 
     @Autowired
     private NuDrillDownDAO nuDrillDownDao;
 
     private CacheHelper cacheHelper = CacheHelper.getInstance();
 
     public Choices getChoices(DataCategory dataCategory, List<Choice> selections) {
 
         // we can do a drill down if an itemDefinition is attached to current dataCategory
         ItemDefinition itemDefinition = dataCategory.getItemDefinition();
         List<Choice> drillDownChoices = null;
         List<Choice> values = new ArrayList<Choice>();
         if (itemDefinition != null) {
 
             // obtain drill down choices
             drillDownChoices = itemDefinition.getDrillDownChoices();
 
             // fix-up selections and drill downs
             matchSelectionOrderToDrillDownChoices(drillDownChoices, selections);
             removeSelectionsNotInDrillDownChoices(drillDownChoices, selections);
             removeDrillDownChoicesThatHaveBeenSelected(drillDownChoices, selections);
 
             // get drill down values
             values = getDataItemChoices(dataCategory, selections, drillDownChoices);
         }
 
         // work out name
         String name;
         if ((drillDownChoices != null) && (drillDownChoices.size() > 0)) {
             name = drillDownChoices.get(0).getName();
         } else {
             name = "uid";
         }
 
         // skip ahead if we only have one value that is not "uid"
         if (!name.equals("uid") && (values.size() == 1)) {
             selections.add(new Choice(name, values.get(0).getValue()));
             return getChoices(dataCategory, selections);
         } else {
             // wrap result in Choices object
             return new Choices(name, values);
         }
     }
 
     public void clearDrillDownCache() {
         cacheHelper.clearCache("DrillDownChoices");
     }
 
     @SuppressWarnings("unchecked")
     protected List<Choice> getDataItemChoices(DataCategory dataCategory,
        List<Choice> selections, List<Choice> drillDownChoices) {
 
         List<Choice> choices = ((List<Choice>) cacheHelper.getCacheable(
                 new DrillDownFactory(drillDownDao, dataCategory, selections, drillDownChoices)));
         List<Choice> nuChoices = ((List<Choice>) cacheHelper.getCacheable(
                 new NuDrillDownFactory(nuDrillDownDao, dataCategory, selections, drillDownChoices)));
 
         // Deal with rows that occur in both tables
         for (Choice choice : nuChoices) {
             if (!choices.contains(choice)) {
                 choices.add(choice);
             }
         }
         return choices;
     }
 
     protected void matchSelectionOrderToDrillDownChoices(List<Choice> drillDownChoices, List<Choice> selections) {
         for (Choice c : drillDownChoices) {
             int selectionIndex = selections.indexOf(c);
             if (selectionIndex >= 0) {
                 selections.add(selections.remove(selectionIndex));
             }
         }
     }
 
     protected void removeDrillDownChoicesThatHaveBeenSelected(List<Choice> drillDownChoices, List<Choice> selections) {
         Iterator<Choice> iterator;
         Choice choice;
         iterator = drillDownChoices.iterator();
         while (iterator.hasNext()) {
             choice = iterator.next();
             if (selections.contains(choice)) {
                 iterator.remove();
             }
         }
     }
 
     protected void removeSelectionsNotInDrillDownChoices(List<Choice> drillDownChoices, List<Choice> selections) {
         Iterator<Choice> iterator;
         Choice choice;
         iterator = selections.iterator();
         while (iterator.hasNext()) {
             choice = iterator.next();
             if (!drillDownChoices.contains(choice)) {
                 iterator.remove();
             }
         }
     }
 }
