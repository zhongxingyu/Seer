 package com.amee.calculation.service;
 
 import com.amee.domain.*;
 import com.amee.domain.algorithm.Algorithm;
 import com.amee.domain.data.ItemDefinition;
 import com.amee.domain.data.ItemValueDefinition;
 import com.amee.domain.data.ItemValueMap;
 import com.amee.domain.item.BaseItem;
 import com.amee.domain.item.BaseItemValue;
 import com.amee.domain.item.UsableValuePredicate;
 import com.amee.domain.item.data.DataItem;
 import com.amee.domain.item.profile.BaseProfileItemValue;
 import com.amee.domain.item.profile.ProfileItem;
 import com.amee.domain.item.profile.ProfileItemNumberValue;
 import com.amee.domain.item.profile.ProfileItemTextValue;
 import com.amee.domain.profile.CO2CalculationService;
 import com.amee.domain.sheet.Choices;
 import com.amee.platform.science.AlgorithmRunner;
 import com.amee.platform.science.ExternalGenericValue;
 import com.amee.platform.science.InternalValue;
 import com.amee.platform.science.ReturnValues;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import sun.org.mozilla.javascript.internal.JavaScriptException;
 
 import javax.script.ScriptException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Service
 public class CalculationService implements CO2CalculationService, BeanFactoryAware {
 
     private final Log log = LogFactory.getLog(getClass());
     private final Log scienceLog = LogFactory.getLog("science");
 
     @Autowired
     private AMEEStatistics ameeStatistics;
 
     @Autowired
     private DataItemService dataItemService;
 
     @Autowired
     private ProfileItemService profileItemService;
 
     private AlgorithmRunner algorithmRunner = new AlgorithmRunner();
 
     // Set by Spring context. The BeanFactory used to retrieve ProfileFinder and DataFinder instances.
     private BeanFactory beanFactory;
 
     /**
      * Calculate and always set the GHG amounts for a ProfileItem.
      *
      * @param profileItem - the ProfileItem for which to calculate GHG amounts
      */
     public void calculate(ProfileItem profileItem) {
 
         // End marker ProfileItems can only have zero amounts.
         // Calculate amounts for ProfileItem if an Algorithm is available.
         // Some ProfileItems are from ItemDefinitions which do not have Algorithms and
         // hence do not support calculations.
         if (!profileItem.isEnd() && profileItem.supportsCalculation()) {
             Algorithm algorithm = profileItem.getItemDefinition().getAlgorithm(Algorithm.DEFAULT);
             if (algorithm != null) {
                 Map<String, Object> values = getValues(profileItem);
                 profileItem.setAmounts(calculate(algorithm, values));
             }
         }
     }
 
     /**
      * Calculate and return the GHG amounts for a DataItem and a set of user specified values.
      *
      * @param dataItem         - the DataItem for the calculation
      * @param userValueChoices - user supplied value choices
      * @param version          - the APIVersion. This is used to determine the correct ItemValueDefinitions to load into the calculation
      * @return the calculated GHG amounts
      */
     public ReturnValues calculate(DataItem dataItem, Choices userValueChoices, APIVersion version) {
         Algorithm algorithm = dataItem.getItemDefinition().getAlgorithm(Algorithm.DEFAULT);
         if (algorithm != null) {
             Map<String, Object> values = getValues(dataItem, userValueChoices, version);
             return calculate(algorithm, values);
         }
         return new ReturnValues();
     }
 
     /**
      * Calculate and return the GHG amounts for given the provided algorithm and input values.
      * <p/>
      * Intended to be used publicly in test harnesses when passing the modified algorithm content and input values
      * for execution is desirable.
      *
      * @param algorithm the algorithm to use
      * @param values    input values for the algorithm
      * @return the algorithm result
      */
     public ReturnValues calculate(Algorithm algorithm, Map<String, Object> values) {
 
         if (log.isDebugEnabled()) {
             log.debug("calculate()");
             log.debug("calculate() - algorithm uid: " + algorithm.getUid());
             log.debug("calculate() - input values: " + values);
             log.debug("calculate() - starting calculation");
         }
 
         ReturnValues returnValues;
         final long startTime = System.nanoTime();
 
         try {
             returnValues = algorithmRunner.evaluate(algorithm, values);
         } catch (ScriptException e) {
 
             // Bubble up parameter missing or format exceptions from the
             // algorithms (the only place where these validations can be performed.
             IllegalArgumentException iae = AlgorithmRunner.getIllegalArgumentException(e);
             if (iae != null) {
                 throw iae;
             }
 
             // Throw CalculationException for Exceptions from the JavaScript 'throw' keyword.
             if ((e.getCause() != null) && e.getCause() instanceof JavaScriptException) {
 
                 // Writing java programs that rely on sun.* is risky: they are not portable, and are not supported.
                 // http://java.sun.com/products/jdk/faq/faq-sun-packages.html
                 JavaScriptException jse = (JavaScriptException) e.getCause();
                 throw new CalculationException(
                         "Caught Exception in Algorithm (" +
                                 algorithm.getItemDefinition().getName() +
                                 ", " +
                                 algorithm.getName() +
                                 ", " +
                                 jse.lineNumber() +
                                 ", " +
                                 jse.columnNumber() +
                                 "): " + jse.getValue());
 
             }
 
             // Log all other errors to the science log...
             scienceLog.warn(
                     "Caught ScriptException in Algorithm (" +
                             algorithm.getItemDefinition().getName() +
                             ", " +
                             algorithm.getName() +
                             "): " + e.getMessage());
 
             // ...and return an empty result by default.
             returnValues = new ReturnValues();
             returnValues.addNote("error", "Algorithm error.");
         } finally {
             ameeStatistics.addToThreadCalculationDuration(System.nanoTime() - startTime);
         }
 
         if (log.isDebugEnabled()) {
             log.debug("calculate() - finished calculation");
             log.debug("calculate() - Amounts: " + returnValues);
         }
 
         return returnValues;
     }
 
     /**
      * Collect all relevant algorithm input values for a ProfileItem calculation.
      *
      * Profile item values override data item values which override item definition values.
      *
      * @param profileItem
      * @return
      */
     private Map<String, Object> getValues(ProfileItem profileItem) {
 
         Map<ItemValueDefinition, InternalValue> collectedValues = new HashMap<ItemValueDefinition, InternalValue>();
 
         // Add ItemDefinition defaults.
         APIVersion apiVersion = profileItem.getProfile().getUser().getAPIVersion();
         profileItem.getItemDefinition().appendInternalValues(collectedValues, apiVersion);
 
         // Add DataItem values, filtered by start and end dates of the ProfileItem (factoring in the query date range).
         DataItem dataItem = profileItem.getDataItem();
         dataItem.setEffectiveStartDate(profileItem.getEffectiveStartDate());
         dataItem.setEffectiveEndDate(profileItem.getEffectiveEndDate());
         appendInternalValues(dataItem, dataItemService, collectedValues);
 
         // Add the ProfileItem values.
         appendInternalValues(profileItem, profileItemService, collectedValues);
 
         // Add actual values to return list based on InternalValues in values list.
         Map<String, Object> algorithmInputValues = new HashMap<String, Object>();
         for (Map.Entry<ItemValueDefinition, InternalValue> entry : collectedValues.entrySet()) {
             algorithmInputValues.put(entry.getKey().getCanonicalPath(), entry.getValue().getValue());
         }
 
         // Initialise finders for algorithm.
         initFinders(profileItem, algorithmInputValues);
 
         return algorithmInputValues;
     }
 
     /**
      * Add the Item's {@link com.amee.domain.item.BaseItemValue} collection to the passed {@link com.amee.platform.science.InternalValue} collection.
      *
      * @param item
      * @param values - the {@link com.amee.platform.science.InternalValue} collection
      */
     @SuppressWarnings("unchecked")
     public void appendInternalValues(BaseItem item, ItemService itemService, Map<ItemValueDefinition, InternalValue> values) {
         ItemValueMap itemValueMap = itemService.getItemValuesMap(item);
         for (Object path : itemValueMap.keySet()) {
             // Get all ItemValues with this ItemValueDefinition path.
             List<BaseItemValue> itemValues = itemService.getAllItemValues(item, (String) path);
             if (itemValues.size() > 1) {
                 appendTimeSeriesItemValue(item, values, itemValues);
             } else if (itemValues.size() == 1) {
                 appendSingleValuedItemValue(values, itemValues.get(0));
             }
         }
     }
 
     // Add a BaseItemValue timeseries to the InternalValue collection.
 
     @SuppressWarnings("unchecked")
     private void appendTimeSeriesItemValue(BaseItem item, Map<ItemValueDefinition, InternalValue> values, List<BaseItemValue> itemValues) {
         ItemValueDefinition ivd = itemValues.get(0).getItemValueDefinition();
 
         // Add all BaseItemValues with usable values
         List<ExternalGenericValue> usableSet = (List<ExternalGenericValue>) CollectionUtils.select(itemValues, new UsableValuePredicate());
 
         if (!usableSet.isEmpty()) {
             values.put(ivd, new InternalValue(usableSet, item.getEffectiveStartDate(), item.getEffectiveEndDate()));
             log.debug("appendTimeSeriesItemValue() - added timeseries value " + ivd.getPath());
         }
     }
 
     // Add a single-valued BaseItemValue to the InternalValue collection.
 
     private void appendSingleValuedItemValue(Map<ItemValueDefinition, InternalValue> values, BaseItemValue itemValue) {
         if (itemValue.isUsableValue()) {
             values.put(itemValue.getItemValueDefinition(), new InternalValue(itemValue));
             log.debug("appendSingleValuedItemValue() - added single value " + itemValue.getPath());
         }
     }
 
     /**
      * Add DataFinder, ProfileFinder and ServiceFinder to the algorithm values.
      *
      * @param profileItem to be used in finders
      * @param values      to place finders into
      */
     private void initFinders(ProfileItem profileItem, Map<String, Object> values) {
 
         // Configure and add DataFinder.
         DataFinder dataFinder = (DataFinder) beanFactory.getBean("dataFinder");
         dataFinder.setStartDate(profileItem.getStartDate());
         dataFinder.setEndDate(profileItem.getEndDate());
         values.put("dataFinder", dataFinder);
 
         // Configure and add ProfileFinder.
         ProfileFinder profileFinder = (ProfileFinder) beanFactory.getBean("profileFinder");
         profileFinder.setProfileItem(profileItem);
         profileFinder.setDataFinder(dataFinder);
         values.put("profileFinder", profileFinder);
 
         // Configure and add ServiceFinder.
         ServiceFinder serviceFinder = (ServiceFinder) beanFactory.getBean("serviceFinder");
         serviceFinder.setValues(values);
         serviceFinder.setProfileFinder(profileFinder);
         values.put("serviceFinder", serviceFinder);
     }
 
     /**
      * Collect all relevant algorithm input values for a DataItem + user Choices calculation.
      *
      * @param dataItem
      * @param userValueChoices
      * @param version
      * @return
      */
     private Map<String, Object> getValues(DataItem dataItem, Choices userValueChoices, APIVersion version) {
         Map<ItemValueDefinition, InternalValue> collectedValues = new HashMap<ItemValueDefinition, InternalValue>();
 
         // Add Item Definition default values.
         dataItem.getItemDefinition().appendInternalValues(collectedValues, version);
 
         // Add Data Item values.
         appendInternalValues(dataItem, dataItemService, collectedValues);
 
         // Add user supplied values.
         appendUserValueChoices(dataItem.getItemDefinition(), userValueChoices, collectedValues, version);
 
         // Add actual values to return list based on InternalValues in values list.
         Map<String, Object> algorithmInputValues = new HashMap<String, Object>();
         for (Map.Entry<ItemValueDefinition, InternalValue> entry : collectedValues.entrySet()) {
             algorithmInputValues.put(entry.getKey().getCanonicalPath(), entry.getValue().getValue());
         }
 
         // Finders for algorithm.
         DataFinder dataFinder = (DataFinder) beanFactory.getBean("dataFinder");
 
         ProfileFinder profileFinder = (ProfileFinder) beanFactory.getBean("profileFinder");
         profileFinder.setDataFinder(dataFinder);
 
         ServiceFinder serviceFinder = (ServiceFinder) beanFactory.getBean("serviceFinder");
         serviceFinder.setValues(algorithmInputValues);
         serviceFinder.setProfileFinder(profileFinder);
 
         algorithmInputValues.put("serviceFinder", serviceFinder);
         algorithmInputValues.put("dataFinder", dataFinder);
         algorithmInputValues.put("profileFinder", profileFinder);
 
         return algorithmInputValues;
     }
 
     private void appendUserValueChoices(
             ItemDefinition itemDefinition,
             Choices userValueChoices,
             Map<ItemValueDefinition, InternalValue> values,
             APIVersion version) {
         if (userValueChoices != null) {
             Map<ItemValueDefinition, InternalValue> userChoices = new HashMap<ItemValueDefinition, InternalValue>();
             for (ItemValueDefinition itemValueDefinition : itemDefinition.getItemValueDefinitions()) {
                 // Add each submitted user Choice that is available in the ItemDefinition and for the user's APIVersion
                 if (itemValueDefinition.isFromProfile() &&
                         userValueChoices.containsKey(itemValueDefinition.getPath()) &&
                         !userValueChoices.get(itemValueDefinition.getPath()).getValue().isEmpty() &&
                         itemValueDefinition.isValidInAPIVersion(version)) {
                     // Create transient ProfileItem & ItemValue.
                     ProfileItem profileItem = new ProfileItem();
                     BaseProfileItemValue profileItemValue;
                     if (itemValueDefinition.getValueDefinition().getValueType().equals(ValueType.INTEGER) ||
                             itemValueDefinition.getValueDefinition().getValueType().equals(ValueType.DOUBLE)) {

                         // Item is a number.
                         ProfileItemNumberValue pinv = new ProfileItemNumberValue(itemValueDefinition, profileItem, userValueChoices.get(itemValueDefinition.getPath()).getValue());
                         if (version.isNotVersionOne()) {
                            if (version.isVersionTwo()) {
                                if (userValueChoices.containsKey(itemValueDefinition.getPath() + "Unit")) {
                                    pinv.setUnit(userValueChoices.get(itemValueDefinition.getPath() + "Unit").getValue());
                                }
                                if (userValueChoices.containsKey(itemValueDefinition.getPath() + "PerUnit")) {
                                    pinv.setPerUnit(userValueChoices.get(itemValueDefinition.getPath() + "PerUnit").getValue());
                                }
                            } else {
                                
                                if (userValueChoices.containsKey("units." + itemValueDefinition.getPath())) {
                                    pinv.setUnit(userValueChoices.get("units." + itemValueDefinition.getPath()).getValue());
                                }
                                if (userValueChoices.containsKey("perUnits." + itemValueDefinition.getPath())) {
                                    pinv.setPerUnit(userValueChoices.get("perUnits." + itemValueDefinition.getPath()).getValue());
                                }
                             }
                         }
                         profileItemValue = pinv;
                     } else {
                         // Item is text.
                         profileItemValue = new ProfileItemTextValue(itemValueDefinition, profileItem, userValueChoices.get(itemValueDefinition.getPath()).getValue());
                     }
                     // Only add ItemValue value if it is usable.
                     if (profileItemValue.isUsableValue()) {
                         userChoices.put(itemValueDefinition, new InternalValue(profileItemValue));
                     }
                 }
             }
             values.putAll(userChoices);
         }
     }
 
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
         this.beanFactory = beanFactory;
     }
 }
