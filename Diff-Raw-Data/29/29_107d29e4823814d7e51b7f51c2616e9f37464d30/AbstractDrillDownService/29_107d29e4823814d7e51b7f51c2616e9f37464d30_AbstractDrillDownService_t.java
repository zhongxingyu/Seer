 package com.amee.service.data;
 
 import com.amee.domain.IDataCategoryReference;
 import com.amee.domain.cache.CacheHelper;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.ItemDefinition;
 import com.amee.domain.sheet.Choice;
 import com.amee.domain.sheet.Choices;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public abstract class AbstractDrillDownService implements DrillDownService {
 
     @Autowired
     private DataServiceDAO dataServiceDao;
 
     protected CacheHelper cacheHelper = CacheHelper.getInstance();
 
     @Override
     public Choices getChoices(IDataCategoryReference dc, List<Choice> selections) {
 
         // Get Data Category.
         DataCategory dataCategory = dataServiceDao.getDataCategory(dc);
 
         // we can do a drill down if an itemDefinition is attached to current dataCategory
         ItemDefinition itemDefinition = dataCategory.getItemDefinition();
         List<Choice> drillDownChoices = null;
         List<Choice> values = new ArrayList<Choice>();
         if (itemDefinition != null) {
 
             // get all drill down choices for this item definition.
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
 
             // The next drill down choice.
             name = drillDownChoices.get(0).getName();
         } else {
 
             // No more drill down choices so we should be able to get the UID.
             name = "uid";
         }
 
        // If this drill down has no available values (and it wasn't the last drill down choice), set it to empty string.
         if (values.isEmpty() & !name.equals("uid")) {
            selections.add(new Choice(name, ""));
             return getChoices(dataCategory, selections);
         } else if (!name.equals("uid") && (values.size() == 1)) {
 
             // We only have one possible drill down value and it isn't the UID so select it and skip ahead to next choice.
             selections.add(new Choice(name, values.get(0).getValue()));
             return getChoices(dataCategory, selections);
         } else {
             // wrap result in Choices object
             return new Choices(name, values);
         }
     }
 
     @Override
     public void clearDrillDownCache() {
         cacheHelper.clearCache("DrillDownChoices");
     }
 
     private void matchSelectionOrderToDrillDownChoices(List<Choice> drillDownChoices, List<Choice> selections) {
         for (Choice c : drillDownChoices) {
             int selectionIndex = selections.indexOf(c);
             if (selectionIndex >= 0) {
                 selections.add(selections.remove(selectionIndex));
             }
         }
     }
 
     /**
      * @param dataCategory the DataCategory to perform drill down within.
      * @param selections Choices that have already been made for the drill down.
      * @param drillDownChoices Choices that remain to be chosen within the drill down
      *
      * @return a list of Choices for the next level of drill down available
      */
     protected abstract List<Choice> getDataItemChoices(
         IDataCategoryReference dataCategory, List<Choice> selections, List<Choice> drillDownChoices);
 
     private void removeDrillDownChoicesThatHaveBeenSelected(List<Choice> drillDownChoices, List<Choice> selections) {
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
 
     private void removeSelectionsNotInDrillDownChoices(List<Choice> drillDownChoices, List<Choice> selections) {
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
