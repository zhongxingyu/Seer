 package com.amee.service.item;
 
 import com.amee.base.domain.ResultsWrapper;
 import com.amee.base.transaction.TransactionController;
 import com.amee.base.utils.UidGen;
 import com.amee.domain.*;
 import com.amee.domain.data.*;
 import com.amee.domain.item.BaseItem;
 import com.amee.domain.item.BaseItemValue;
 import com.amee.domain.item.HistoryValue;
 import com.amee.domain.item.data.BaseDataItemValue;
 import com.amee.domain.item.data.DataItem;
 import com.amee.domain.item.data.DataItemNumberValue;
 import com.amee.domain.item.data.DataItemTextValue;
 import com.amee.domain.sheet.Choice;
 import com.amee.domain.sheet.Choices;
 import com.amee.platform.science.ExternalHistoryValue;
 import com.amee.platform.science.StartEndDate;
 import com.amee.service.invalidation.InvalidationService;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeanUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 
 @Service
 public class DataItemService extends ItemService implements IDataItemService {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private TransactionController transactionController;
 
     @Autowired
     private InvalidationService invalidationService;
 
     @Autowired
     private DataItemServiceDAO dao;
 
     @Override
     public long getDataItemCount(IDataCategoryReference dataCategory) {
         return dao.getDataItemCount(dataCategory);
     }
 
     @Override
     public List<DataItem> getDataItems(IDataCategoryReference dataCategory) {
         return getDataItems(dataCategory, true);
     }
 
     @Override
     public List<DataItem> getDataItems(IDataCategoryReference dataCategory, boolean checkDataItems) {
         List<DataItem> dataItems = new ArrayList<DataItem>();
         for (DataItem dataItem : dao.getDataItems(dataCategory)) {
             dataItems.add(dataItem);
         }
         return activeDataItems(dataItems, checkDataItems);
     }
 
     private List<DataItem> activeDataItems(List<DataItem> dataItems, boolean checkDataItems) {
         List<DataItem> activeDataItems = new ArrayList<DataItem>();
         for (DataItem dataItem : dataItems) {
             if (!dataItem.isTrash()) {
                 if (checkDataItems) {
                     checkDataItem(dataItem);
                 }
                 activeDataItems.add(dataItem);
             }
         }
         loadItemValuesForItems((List) activeDataItems);
         localeService.loadLocaleNamesForDataItems(activeDataItems);
         return activeDataItems;
     }
 
     @Override
     @SuppressWarnings(value = "unchecked")
     public List<DataItem> getDataItems(Set<Long> dataItemIds) {
         List<DataItem> dataItems = dao.getDataItems(dataItemIds);
         loadItemValuesForItems((List) dataItems);
         localeService.loadLocaleNamesForDataItems(dataItems);
         return dataItems;
     }
 
     @Override
     @SuppressWarnings(value = "unchecked")
     public Map<String, DataItem> getDataItemMap(Set<Long> dataItemIds, boolean loadValues) {
         Map<String, DataItem> dataItemMap = new HashMap<String, DataItem>();
         Set<BaseItemValue> dataItemValues = new HashSet<BaseItemValue>();
         // Load all DataItems and BaseItemValues, if required.
         List<DataItem> dataItems = dao.getDataItems(dataItemIds);
         if (loadValues) {
             loadItemValuesForItems((List) dataItems);
         }
         // Add DataItems to map. Add BaseItemValue, if required.
         for (DataItem dataItem : dataItems) {
             dataItemMap.put(dataItem.getUid(), dataItem);
             if (loadValues) {
                 dataItemValues.addAll(this.getItemValues(dataItem));
             }
         }
         localeService.loadLocaleNamesForDataItems(dataItemMap.values(), dataItemValues);
         return dataItemMap;
     }
 
     @Override
     public DataItem getDataItemByIdentifier(DataCategory parent, String path) {
         DataItem dataItem = null;
         if (!StringUtils.isBlank(path)) {
             if (UidGen.INSTANCE_12.isValid(path)) {
                 dataItem = getDataItemByUid(parent, path);
             }
             if (dataItem == null) {
                 dataItem = getDataItemByPath(parent, path);
             }
         }
         return dataItem;
     }
 
     @Override
     public DataItem getDataItemByUid(DataCategory parent, String uid) {
         DataItem dataItem = getItemByUid(uid);
         if ((dataItem != null) && dataItem.getDataCategory().equals(parent)) {
             return dataItem;
         } else {
             return null;
         }
     }
 
     @Override
     public DataItem getItemByUid(String uid) {
         DataItem dataItem = dao.getItemByUid(uid);
         if ((dataItem != null) && (!dataItem.isTrash())) {
             checkDataItem(dataItem);
             return dataItem;
         } else {
             return null;
         }
     }
 
     @Override
     public DataItem getDataItemByPath(DataCategory parent, String path) {
         DataItem dataItem = dao.getDataItemByPath(parent, path);
         if ((dataItem != null) && !dataItem.isTrash()) {
             checkDataItem(dataItem);
             return dataItem;
         } else {
             return null;
         }
     }
 
     @Override
     public String getLabel(DataItem dataItem) {
         String label = "";
         BaseItemValue itemValue;
         ItemDefinition itemDefinition = dataItem.getItemDefinition();
         for (Choice choice : itemDefinition.getDrillDownChoices()) {
             itemValue = getItemValue(dataItem, choice.getName());
             if ((itemValue != null) &&
                     (itemValue.getValueAsString().length() > 0) &&
                     !itemValue.getValueAsString().equals("-")) {
                 if (label.length() > 0) {
                     label = label.concat(", ");
                 }
                 label = label.concat(itemValue.getValueAsString());
             }
         }
         if (label.length() == 0) {
             label = dataItem.getDisplayPath();
         }
         return label;
     }
 
     @Override
     public Choices getUserValueChoices(DataItem dataItem, APIVersion apiVersion) {
         List<Choice> userValueChoices = new ArrayList<Choice>();
         for (ItemValueDefinition ivd : dataItem.getItemDefinition().getItemValueDefinitions()) {
             if (ivd.isFromProfile() && ivd.isValidInAPIVersion(apiVersion)) {
                 // start default value with value from ItemValueDefinition
                 String defaultValue = ivd.getValue();
                 // next give DataItem a chance to set the default value, if appropriate
                 if (ivd.isFromData()) {
                     BaseItemValue dataItemValue = getItemValue(dataItem, ivd.getPath());
                     if ((dataItemValue != null) && (dataItemValue.getValueAsString().length() > 0)) {
                         defaultValue = dataItemValue.getValueAsString();
                     }
                 }
                 // create Choice
                 userValueChoices.add(new Choice(ivd.getPath(), defaultValue));
             }
         }
         return new Choices("userValueChoices", userValueChoices);
     }
 
     /**
      * Add to the {@link com.amee.domain.item.data.DataItem} any {@link com.amee.domain.item.data.BaseDataItemValue}s it is missing.
      * This will be the case on first persist (this method acting as a reification function), and between GETs if any
      * new {@link com.amee.domain.data.ItemValueDefinition}s have been added to the underlying
      * {@link com.amee.domain.data.ItemDefinition}.
      * <p/>
      * Any updates to the {@link com.amee.domain.item.data.DataItem} will be persisted to the database.
      *
      * @param dataItem - the DataItem to check
      */
     @SuppressWarnings(value = "unchecked")
     public void checkDataItem(DataItem dataItem) {
 
         if (dataItem == null) {
             return;
         }
 
         Set<ItemValueDefinition> existingItemValueDefinitions = getItemValueDefinitionsInUse(dataItem);
         Set<ItemValueDefinition> missingItemValueDefinitions = new HashSet<ItemValueDefinition>();
 
         // find ItemValueDefinitions not currently implemented in this Item
         for (ItemValueDefinition ivd : dataItem.getItemDefinition().getItemValueDefinitions()) {
             if (ivd.isFromData()) {
                 if (!existingItemValueDefinitions.contains(ivd)) {
                     missingItemValueDefinitions.add(ivd);
                 }
             }
         }
 
         // Do we need to add any ItemValueDefinitions?
         if (missingItemValueDefinitions.size() > 0) {
 
             // Ensure a transaction has been opened. The implementation of open-session-in-view we are using
             // does not open transactions for GETs. This method is called for certain GETs.
             transactionController.begin(true);
 
             // create missing ItemValues
             for (ItemValueDefinition ivd : missingItemValueDefinitions) {
                 BaseDataItemValue itemValue;
                 // Create a value.
                 if (ivd.getValueDefinition().getValueType().equals(ValueType.INTEGER) ||
                         ivd.getValueDefinition().getValueType().equals(ValueType.DOUBLE)) {
                     // Item is a number.
                     itemValue = new DataItemNumberValue(ivd, dataItem);
                 } else {
                     // Item is text.
                     itemValue = new DataItemTextValue(ivd, dataItem, "");
                 }
                 persist(itemValue);
             }
 
             // clear caches
             clearItemValues();
             invalidationService.add(dataItem.getDataCategory());
         }
     }
 
     /**
      * Returns the most recent modified timestamp of DataItems for the supplied DataCategory. Will return the
      * date of the epoch if there are no matching DataItems.
      *
      * @param dataCategory to get modified timestamp for
      * @return most recent modified timestamp or the epoch value if not available.
      */
     @Override
     public Date getDataItemsModified(DataCategory dataCategory) {
         Date modified = dao.getDataItemsModified(dataCategory);
         if (modified == null) {
             modified = IDataItemService.EPOCH;
         }
         return modified;
     }
 
 
     /**
      * Returns true if the path of the supplied DataItem is unique amongst peers with the same
      * DataCategory. Empty paths are always treated as 'unique'.
      *
      * @param dataItem to check for uniqueness
      * @return true if the DataItem has a unique path amongst peers or the path is simply empty
      */
     @Override
     public boolean isDataItemUniqueByPath(DataItem dataItem) {
         return dataItem.getPath().isEmpty() || dao.isDataItemUniqueByPath(dataItem);
     }
 
     @Override
     public void remove(DataItem dataItem) {
         dataItem.setStatus(AMEEStatus.TRASH);
     }
 
     @Override
     public void persist(DataItem dataItem) {
         persist(dataItem, true);
     }
 
     @Override
     public void persist(DataItem dataItem, boolean checkDataItem) {
         dao.persist(dataItem);
         if (checkDataItem) {
             checkDataItem(dataItem);
         }
     }
 
     // ItemValues.
 
     /**
      * Gets a {@link ResultsWrapper} of {@link BaseDataItemValue}s matching the supplied {@link DataItemValuesFilter}.
      * <p/>
      * TODO: This method is not designed for large amounts of DIVHs.
      * TODO: See https://jira.amee.com/browse/PL-2685.
      *
      * @param filter a {@link DataItemValuesFilter} to match {@link BaseDataItemValue}s against
      * @return a a {@link ResultsWrapper} of {@link BaseDataItemValue}s
      */
     public ResultsWrapper<BaseDataItemValue> getAllItemValues(DataItemValuesFilter filter) {
 
         boolean handledFirst = false;
         boolean truncated = false;
         int count = 0;
 
         // Get *all* item value for the current DataItem and ItemValueDefinition.
         List<BaseItemValue> itemValues =
                 new ArrayList<BaseItemValue>(
                         getAllItemValues(filter.getDataItem(), filter.getItemValueDefinition().getPath()));
 
         // Sort so earliest item value comes first, based on the startDate.
         Collections.sort(itemValues, new BaseItemValueStartDateComparator());
 
         // Create and populate a ResultsWrapper.
         List<BaseDataItemValue> results = new ArrayList<BaseDataItemValue>();
         for (BaseItemValue biv : itemValues) {
             BaseDataItemValue bdiv = (BaseDataItemValue) biv;
             if (BaseItemValueStartDateComparator.isHistoricValue(bdiv)) {
                 ExternalHistoryValue ehv = (ExternalHistoryValue) bdiv;
                 // At or beyond the start date?
                 if (ehv.getStartDate().compareTo(filter.getStartDate()) >= 0) {
                     // Before the end date?
                     if (ehv.getStartDate().before(filter.getEndDate())) {
                         // On or after the resultStart?
                         if (count >= filter.getResultStart()) {
                             // Before the resultLimit?
                             if (results.size() < filter.getResultLimit()) {
                                 // Safe to add this item value.
                                 results.add(bdiv);
                             } else {
                                 // Gone beyond the resultLimit.
                                 // The results are truncated and we can ignore the other item values.
                                 truncated = true;
                                 break;
                             }
                         }
                         // Increment count of eligible item values.
                         count++;
                     } else {
                         // Gone beyond the end date and we can ignore the other item values.
                         break;
                     }
                 } else {
                     // Before the start date.
                 }
             } else {
                 // We should only execute this section once.
                 if (handledFirst) {
                     // Should never get here. Implies that the list contains a non-historical item value
                     // is in the wrong place in the list.
                     throw new IllegalStateException("Unexpected non-historical item value: " + biv);
                 }
                 // On or after the resultStart? Filter at the epoch?
                 if ((count >= filter.getResultStart()) && filter.getStartDate().equals(IDataItemService.EPOCH)) {
                     // This *must* be the first item value.
                     results.add(bdiv);
                 }
                 // Increment count of eligible item values.
                 count++;
                 // We only work in this section once.
                 handledFirst = true;
             }
         }
 
         // Create the ResultsWrapper and return.
         return new ResultsWrapper<BaseDataItemValue>(results, truncated);
     }
 
     /**
      * Returns true if the {@link BaseDataItemValue} supplied has the same startDate as another
      * peer within the same {@link DataItem}.
      *
      * @param itemValue {@link BaseDataItemValue} to check
      * @return true if the {@link BaseDataItemValue} supplied has the same startDate as another {@link DataItem}
      */
     public boolean isDataItemValueUniqueByStartDate(BaseDataItemValue itemValue) {
         if (HistoryValue.class.isAssignableFrom(itemValue.getClass())) {
            HistoryValue hv = (HistoryValue) itemValue;
            BaseItemValue existingItemValue =
                    getItemValuesMap(itemValue.getDataItem()).get(
                            itemValue.getItemValueDefinition().getPath(),
                            hv.getStartDate());
            return (existingItemValue != null) && !existingItemValue.equals(hv);
         } else {
             throw new IllegalStateException("Should not be checking a non-historical DataItemValue.");
         }
     }
 
     /**
      * Get an {@link BaseItemValue} belonging to this Item using some identifier and prevailing datetime context.
      *
      * @param identifier - a value to be compared to the path and then the uid of the Item Values belonging
      *                   to this Item.
      * @return the matched {@link BaseItemValue} or NULL if no match is found.
      */
     @Override
     public BaseItemValue getItemValue(BaseItem item, String identifier) {
         if (!DataItem.class.isAssignableFrom(item.getClass()))
             throw new IllegalStateException("A DataItem instance was expected.");
         return getItemValue(item, identifier, item.getEffectiveStartDate());
     }
 
     /**
      * Updates the Data Item Values for the supplied DataItem based on the properties of the values
      * bean within the DataItem. Internally uses the Spring and Java beans API to access values in the
      * CGLIB created DataItem.values JavaBean.
      *
      * @param dataItem to update
      */
     public void updateDataItemValues(DataItem dataItem) {
         Object values = dataItem.getValues();
         ItemValueMap itemValues = getItemValuesMap(dataItem);
         for (String key : itemValues.keySet()) {
             BaseItemValue value = itemValues.get(key);
             PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(values.getClass(), key);
             if (pd != null) {
                 Method readMethod = pd.getReadMethod();
                 if (readMethod != null) {
                     try {
                         Object v = readMethod.invoke(values);
                         if (v != null) {
                             value.setValue(v.toString());
                         }
                     } catch (IllegalAccessException e) {
                         throw new RuntimeException("Caught IllegalAccessException: " + e.getMessage());
                     } catch (InvocationTargetException e) {
                         throw new RuntimeException("Caught InvocationTargetException: " + e.getMessage());
                     }
                 } else {
                     log.warn("updateDataItemValues() Write Method was null: " + key);
                 }
             } else {
                 log.warn("updateDataItemValues() PropertyDescriptor was null: " + key);
             }
         }
     }
 
     @Override
     public void persist(BaseItemValue itemValue) {
         dao.persist(itemValue);
     }
 
     @Override
     public void remove(BaseItemValue itemValue) {
         itemValue.setStatus(AMEEStatus.TRASH);
     }
 
     @Override
     public StartEndDate getStartDate(DataItem dataItem) {
         return null;
     }
 
     @Override
     public StartEndDate getEndDate(DataItem dataItem) {
         return null;
     }
 
     @Override
     protected DataItemServiceDAO getDao() {
         return dao;
     }
 }
